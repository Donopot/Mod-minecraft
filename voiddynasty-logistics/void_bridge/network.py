"""Loopback-only transport for the development simulator, with bounded I/O."""
import asyncio
import contextlib
import time

from .protocol import BridgeError, Frames, encode, envelope, MAX_FRAME, uid, validate_response


class BridgeServer:
    def __init__(self, receiver, drop_commit_reply_once=False, heartbeat_timeout=6):
        self.receiver = receiver
        self.drop_commit_reply_once = drop_commit_reply_once
        self.active = False
        self.tasks = set()
        self.heartbeat_timeout = heartbeat_timeout

    async def start(self, port=28765):
        self.server = await asyncio.start_server(self._connection, "127.0.0.1", port, limit=MAX_FRAME)
        return self.server.sockets[0].getsockname()[1]

    async def close(self):
        self.server.close()
        await self.server.wait_closed()
        for task in list(self.tasks):
            task.cancel()
        await asyncio.gather(*list(self.tasks), return_exceptions=True)

    async def _connection(self, reader, writer):
        task = asyncio.current_task()
        self.tasks.add(task)
        acquired = False
        authenticated = False
        try:
            if self.active:
                raise BridgeError("CONNECTION_BUSY")
            self.active = acquired = True
            frames = Frames()
            last_valid = time.monotonic()
            while True:
                try:
                    chunk = await asyncio.wait_for(reader.read(8192), max(0, self.heartbeat_timeout - (time.monotonic() - last_valid)))
                except TimeoutError:
                    raise BridgeError("HEARTBEAT_EXPIRED") from None
                if not chunk:
                    break
                for message in frames.feed(chunk):
                    try:
                        response = self.receiver.handle(message, authenticated)
                        last_valid = time.monotonic()
                        if message["type"] == "hello":
                            authenticated = True
                        if (self.drop_commit_reply_once and message["type"] == "delivery_commit"
                                and response.get("state") == "REWARD_COMMITTED"):
                            self.drop_commit_reply_once = False
                            return  # Durable write succeeded, connection dies before result.
                    except BridgeError as error:
                        response = {"protocol_version": 1, "type": "error", "message_id": uid(),
                                    "reply_to": message.get("message_id"), "code": error.code}
                    writer.write(encode(response))
                    await asyncio.wait_for(writer.drain(), 2)
                    if response.get("code") in {"AUTH_FAILED", "PROTOCOL_MISMATCH", "HANDSHAKE_REQUIRED"}:
                        return
        except BridgeError as error:
            writer.write(encode({"protocol_version": 1, "type": "error", "message_id": uid(), "code": error.code}))
            with contextlib.suppress(ConnectionError, TimeoutError):
                await asyncio.wait_for(writer.drain(), 2)
        except (ConnectionError, TimeoutError):
            pass
        finally:
            if acquired:
                self.active = False
            writer.close()
            with contextlib.suppress(ConnectionError):
                await writer.wait_closed()
            self.tasks.discard(task)


class BridgeClient:
    def __init__(self, sender):
        self.sender = sender
        self.pending = {}
        self.writer = None
        self.tasks = []
        self.last_valid = 0

    async def connect(self, token, port=28765):
        self.reader, self.writer = await asyncio.wait_for(asyncio.open_connection("127.0.0.1", port), 3)
        self.last_valid = time.monotonic()
        self.tasks = [asyncio.create_task(self._read())]
        response = await self.exchange(self.sender.hello(token))
        self.sender.accept_hello(response)
        self.tasks.append(asyncio.create_task(self._heartbeat()))
        return response

    async def close(self):
        self.sender.connected = False
        for task in self.tasks:
            task.cancel()
        await asyncio.gather(*self.tasks, return_exceptions=True)
        self.tasks.clear()
        if self.writer:
            self.writer.close()
            with contextlib.suppress(ConnectionError):
                await self.writer.wait_closed()

    async def exchange(self, message):
        if not self.writer or self.writer.is_closing():
            raise ConnectionError("Bridge unavailable")
        if len(self.pending) >= 8:
            raise BridgeError("QUEUE_FULL")
        future = asyncio.get_running_loop().create_future()
        self.pending[message["message_id"]] = future
        try:
            self.writer.write(encode(message))
            await asyncio.wait_for(self.writer.drain(), 2)
            response = await asyncio.wait_for(future, 6)
            if response["type"] == "error":
                raise BridgeError(response["code"])
            return response
        finally:
            self.pending.pop(message["message_id"], None)

    async def _read(self):
        error = ConnectionError("Bridge disconnected; cargo remains locked")
        try:
            frames = Frames()
            while True:
                chunk = await asyncio.wait_for(self.reader.read(8192), max(0, 6 - (time.monotonic() - self.last_valid)))
                if not chunk:
                    return
                for message in frames.feed(chunk):
                    validate_response(message)
                    self.last_valid = time.monotonic()
                    future = self.pending.get(message.get("reply_to"))
                    if future and not future.done():
                        future.set_result(message)
        except (BridgeError, ConnectionError, TimeoutError) as failure:
            error = failure
        finally:
            self.sender.connected = False
            for future in list(self.pending.values()):
                if not future.done():
                    future.set_exception(error)

    async def _heartbeat(self):
        try:
            while True:
                await asyncio.sleep(2)
                response = await self.exchange(envelope("heartbeat", self.sender.identities()))
                if any(response[key] != self.sender.identities()[key] for key in self.sender.identities()):
                    raise BridgeError("STALE_SESSION")
        except (BridgeError, ConnectionError, TimeoutError):
            self.sender.connected = False
            if self.writer:
                self.writer.close()


async def deliver(client, sender):
    """Resume from persisted state. A timeout never cancels or refunds anything."""
    sender.draft()
    status = await client.exchange(sender.request("delivery_status"))
    sender.apply_result(status)
    if sender.state["completed"]:
        return status
    if status["state"] == "CANCELLED":
        raise BridgeError("CANCELLED")
    if status["state"] == "UNKNOWN":
        sender.apply_result(await client.exchange(sender.request("delivery_prepare")))
    if sender.state["delivery"]["state"] != "CARGO_RESERVED":
        raise BridgeError("RECOVERY_REQUIRED")
    result = await client.exchange(sender.request("delivery_commit"))
    sender.apply_result(result)
    return result

