import asyncio
import copy
import json
from pathlib import Path
import tempfile
import unittest

from void_bridge.domain import Receiver, Sender, initial_campaign, initial_world
from void_bridge.network import BridgeClient, BridgeServer, deliver
from void_bridge.protocol import (BridgeError, Frames, MAX_FRAME, MISSION_HASH, decode,
                                  encode, envelope, uid, validate_request, validate_response)
from void_bridge.storage import JsonStore


class DomainTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.identities = {key: uid() for key in ("campaign_id", "world_id", "pair_id", "checkpoint_id")}
        self.campaign = JsonStore(Path(self.temp.name) / "campaign.json")
        self.world = JsonStore(Path(self.temp.name) / "world.json")
        self.campaign.save(initial_campaign(self.identities))
        self.world.save(initial_world(self.identities, 65))
        self.token = "test-secret-" + "a" * 32
        self.rt = Receiver(self.campaign, self.token)
        self.mc = Sender(self.world)
        self.connect()

    def connect(self):
        self.mc.accept_hello(self.rt.handle(self.mc.hello(self.token)))

    def exchange(self, kind):
        return self.rt.handle(self.mc.request(kind), authenticated=True)

    def prepare(self):
        self.mc.draft()
        self.mc.apply_result(self.exchange("delivery_prepare"))

    def settle(self):
        self.prepare()
        self.mc.apply_result(self.exchange("delivery_commit"))

    def assertCode(self, code, function, *args, **kwargs):
        with self.assertRaises(BridgeError) as raised:
            function(*args, **kwargs)
        self.assertEqual(raised.exception.code, code)

    def test_delivery_reload_surplus_and_duplicate_commits(self):
        self.prepare()
        commit = self.mc.request("delivery_commit")
        self.mc.apply_result(self.rt.handle(commit, True))
        for _ in range(20):
            self.assertEqual(self.rt.handle(commit, True)["state"], "REWARD_COMMITTED")
        self.rt = Receiver(self.campaign, self.token)
        self.mc = Sender(self.world)
        self.connect()
        self.assertEqual(self.rt.state["reserve"], 10)
        self.assertEqual(len(self.rt.snapshot()["receipts"]), 1)
        self.assertEqual(self.mc.state["available_ingots"], 1)
        self.assertEqual(self.mc.state["reserved_ingots"], 0)
        self.assertCode("MISSION_COMPLETED", self.mc.draft)

    def test_63_ingots_rejected_64_accepted(self):
        state = self.world.load()
        state["available_ingots"] = 63
        self.world.save(state)
        self.mc = Sender(self.world)
        self.connect()
        self.assertCode("INSUFFICIENT_CARGO", self.mc.draft)
        state["available_ingots"] = 64
        self.world.save(state)
        self.mc = Sender(self.world)
        self.connect()
        self.settle()
        self.assertEqual(self.mc.state["available_ingots"], 0)

    def test_double_click_preserves_delivery_id(self):
        self.assertEqual(self.mc.draft(), self.mc.draft())

    def test_no_commit_without_local_reservation(self):
        self.mc.draft()
        self.assertCode("CARGO_NOT_RESERVED", self.mc.request, "delivery_commit")

    def test_remote_commit_requires_prepare(self):
        self.mc.draft()
        request = self.mc.request("delivery_prepare")
        request["type"] = "delivery_commit"
        self.assertCode("NOT_PREPARED", self.rt.handle, request, True)

    def test_payload_mismatch_for_same_delivery(self):
        self.prepare()
        request = self.mc.request("delivery_commit")
        request["cargo"][0]["count"] = 63
        self.assertCode("PAYLOAD_MISMATCH", self.rt.handle, request, True)

    def test_client_cannot_set_reward(self):
        self.prepare()
        request = self.mc.request("delivery_commit")
        request["reward"] = 100000
        self.assertCode("INVALID_MESSAGE", self.rt.handle, request, True)

    def test_lost_prepare_reply_retries_same_id(self):
        self.mc.draft()
        first = self.exchange("delivery_prepare")
        again = self.exchange("delivery_prepare")
        self.assertEqual(first["delivery_id"], again["delivery_id"])
        self.mc.apply_result(again)
        self.assertEqual(self.mc.state["reserved_ingots"], 64)
        self.assertEqual(self.rt.state["reserve"], 0)

    def test_lost_commit_reply_recovered_after_restart(self):
        self.prepare()
        self.exchange("delivery_commit")
        self.rt = Receiver(self.campaign, self.token)
        self.mc = Sender(self.world)
        self.connect()
        self.mc.apply_result(self.exchange("delivery_status"))
        self.assertTrue(self.mc.state["completed"])
        self.assertEqual(self.rt.state["reserve"], 10)

    def test_lost_prepare_record_with_reserved_cargo_requires_recovery(self):
        self.prepare()
        self.campaign.save(initial_campaign(self.identities))
        self.rt = Receiver(self.campaign, self.token)
        self.connect()
        self.assertCode("RECOVERY_REQUIRED", self.mc.apply_result, self.exchange("delivery_status"))
        self.assertEqual(self.mc.state["reserved_ingots"], 64)

    def test_loading_old_rt_save_blocks_known_receipt(self):
        self.settle()
        self.campaign.save(initial_campaign(self.identities))
        self.rt = Receiver(self.campaign, self.token)
        self.assertCode("RECOVERY_REQUIRED", self.rt.handle, self.mc.hello(self.token))

    def test_loading_old_mc_save_blocks_remote_receipt(self):
        self.settle()
        self.world.save(initial_world(self.identities, 65))
        self.mc = Sender(self.world)
        self.assertCode("RECOVERY_REQUIRED", self.connect)
        self.assertTrue(self.mc.state["recovery_required"])
        self.assertEqual(self.rt.state["reserve"], 10)

    def test_old_checkpoint_blocked(self):
        self.mc.state["checkpoint_id"] = uid()
        self.assertCode("RECOVERY_REQUIRED", self.rt.handle, self.mc.hello(self.token))

    def test_wrong_identity_blocked(self):
        for field in ("campaign_id", "world_id", "pair_id"):
            request = self.mc.hello(self.token)
            request[field] = uid()
            self.assertCode("IDENTITY_MISMATCH", self.rt.handle, request)

    def test_old_session_after_reload_blocked(self):
        self.prepare()
        old = self.mc.request("delivery_commit")
        self.rt = Receiver(self.campaign, self.token)
        self.assertCode("STALE_SESSION", self.rt.handle, old, True)

    def test_wrong_token_and_mission_hash(self):
        request = self.mc.hello("wrong-token-" + "b" * 32)
        self.assertCode("AUTH_FAILED", self.rt.handle, request)
        request = self.mc.hello(self.token)
        request["mission_hash"] = "0" * 64
        self.assertCode("MISSION_MISMATCH", self.rt.handle, request)

    def test_unavailable_blocks_new_prepare(self):
        self.mc.draft()
        self.rt.state["available"] = False
        self.assertCode("UNAVAILABLE", self.exchange, "delivery_prepare")
        self.assertEqual(self.mc.state["reserved_ingots"], 0)

    def test_only_one_prepared_delivery(self):
        self.prepare()
        request = self.mc.request("delivery_prepare")
        request["delivery_id"] = uid()
        self.assertCode("DELIVERY_IN_PROGRESS", self.rt.handle, request, True)

    def test_failed_receiver_save_does_not_acknowledge_reward(self):
        self.prepare()
        original = self.campaign.save
        self.campaign.save = lambda _: (_ for _ in ()).throw(OSError("disk full"))
        self.assertCode("PERSISTENCE_FAILED", self.exchange, "delivery_commit")
        self.assertEqual(self.rt.state["reserve"], 0)
        self.assertEqual(self.mc.state["reserved_ingots"], 64)
        self.campaign.save = original

    def test_failed_reservation_save_prevents_commit(self):
        self.mc.draft()
        response = self.exchange("delivery_prepare")
        self.world.save = lambda _: (_ for _ in ()).throw(OSError("disk full"))
        self.assertCode("PERSISTENCE_FAILED", self.mc.apply_result, response)
        self.assertEqual(self.rt.state["reserve"], 0)
        self.assertFalse(self.mc.connected)

    def test_cancel_is_durable_before_refund(self):
        self.prepare()
        response = self.exchange("delivery_cancel")
        self.assertEqual(self.campaign.load()["deliveries"][response["delivery_id"]]["state"], "CANCELLED")
        self.mc.apply_result(response)
        self.assertEqual(self.mc.state["available_ingots"], 65)
        self.assertEqual(self.rt.state["reserve"], 0)

    def test_cancel_after_commit_cannot_refund(self):
        self.prepare()
        self.exchange("delivery_commit")
        self.mc.apply_result(self.exchange("delivery_cancel"))
        self.assertEqual(self.mc.state["available_ingots"], 1)
        self.assertTrue(self.mc.state["completed"])

    def test_unknown_status_does_not_refund(self):
        self.mc.draft()
        response = self.exchange("delivery_status")
        self.mc.apply_result(response)
        self.assertEqual(self.mc.state["available_ingots"], 65)

    def test_cancel_tombstone_prevents_delayed_prepare(self):
        self.mc.draft()
        self.assertEqual(self.exchange("delivery_cancel")["state"], "CANCELLED")
        self.assertEqual(self.exchange("delivery_prepare")["state"], "CANCELLED")
        self.assertEqual(self.rt.state["reserve"], 0)

    def test_commit_before_authenticated_handshake_rejected(self):
        self.mc.draft()
        self.assertCode("HANDSHAKE_REQUIRED", self.rt.handle, self.mc.request("delivery_prepare"))

    def test_ledger_does_not_grow_without_bound(self):
        self.mc.draft()
        self.rt.state["deliveries"] = {uid(): {"state": "CANCELLED", "payload_hash": "0" * 64} for _ in range(128)}
        self.assertCode("LEDGER_FULL", self.exchange, "delivery_prepare")
        self.assertEqual(len(self.rt.state["deliveries"]), 128)

    def test_unicode_token_rejected_without_crashing(self):
        self.assertCode("AUTH_FAILED", self.rt.handle, self.mc.hello("é" * 64))


class ProtocolTests(unittest.TestCase):
    def test_tcp_fragmentation_and_coalescing(self):
        data = encode({"x": "é"}) + encode({"y": 2})
        frames, messages = Frames(), []
        for byte in data:
            messages.extend(frames.feed(bytes([byte])))
        self.assertEqual(messages, [{"x": "é"}, {"y": 2}])
        self.assertEqual(Frames().feed(data), messages)

    def test_size_boundary_and_oversize(self):
        message = {"x": "a" * (MAX_FRAME - 9)}
        self.assertEqual(len(encode(message)), MAX_FRAME)
        self.assertEqual(Frames().feed(encode(message)), [message])
        with self.assertRaises(BridgeError):
            Frames().feed(b"x" * MAX_FRAME)
        with self.assertRaises(BridgeError):
            encode({"x": "a" * MAX_FRAME})

    def test_malformed_json_duplicate_keys_non_object_and_bad_utf8(self):
        for data in (b'{"x":1,"x":2}', b"[]", b"NaN", b"{", b'"\\xff"', b'{"x":NaN}'):
            with self.subTest(data=data), self.assertRaises(BridgeError):
                decode(data)

    def test_incompatible_version(self):
        with self.assertRaisesRegex(BridgeError, "PROTOCOL_MISMATCH"):
            validate_request({"protocol_version": 2})

    def test_invalid_response_types_and_correlations(self):
        for message in (
                {"protocol_version": 1, "message_id": uid(), "type": []},
                {"protocol_version": 1, "message_id": uid(), "type": "error", "code": "AUTH_FAILED", "reply_to": []},
                {"protocol_version": 1, "message_id": uid(), "type": "delivery_result"}):
            with self.subTest(message=message), self.assertRaises(BridgeError):
                validate_response(message)

    def test_boolean_count_and_float_mission_version_rejected(self):
        example = Path(__file__).resolve().parents[1] / "protocol/examples/delivery_prepare.json"
        message = json.loads(example.read_text(encoding="utf-8"))
        validate_request(message)
        message["cargo"][0]["count"] = True
        with self.assertRaisesRegex(BridgeError, "INVALID_CARGO"):
            validate_request(message)
        message["cargo"][0]["count"] = 64
        message["mission_version"] = 1.0
        with self.assertRaisesRegex(BridgeError, "INVALID_MESSAGE"):
            validate_request(message)


class NetworkTests(unittest.IsolatedAsyncioTestCase):
    async def asyncSetUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        identities = {key: uid() for key in ("campaign_id", "world_id", "pair_id", "checkpoint_id")}
        self.campaign = JsonStore(Path(self.temp.name) / "campaign.json")
        self.world = JsonStore(Path(self.temp.name) / "world.json")
        self.campaign.save(initial_campaign(identities))
        self.world.save(initial_world(identities, 64))
        self.token = "a" * 64
        self.server = BridgeServer(Receiver(self.campaign, self.token))
        self.port = await self.server.start(0)
        self.addAsyncCleanup(self.server.close)

    async def client(self):
        sender = Sender(self.world)
        client = BridgeClient(sender)
        self.addAsyncCleanup(client.close)
        await client.connect(self.token, self.port)
        return sender, client

    async def test_real_tcp_delivery(self):
        sender, client = await self.client()
        await deliver(client, sender)
        self.assertEqual(self.campaign.load()["reserve"], 10)
        self.assertTrue(self.world.load()["completed"])

    async def test_lost_commit_reply_over_tcp_then_reconnect(self):
        self.server.drop_commit_reply_once = True
        sender, client = await self.client()
        with self.assertRaises(ConnectionError):
            await deliver(client, sender)
        self.assertEqual(self.world.load()["reserved_ingots"], 64)
        await client.close()
        await asyncio.sleep(0.01)
        sender, client = await self.client()
        await deliver(client, sender)
        self.assertEqual(self.campaign.load()["reserve"], 10)
        self.assertEqual(self.world.load()["reserved_ingots"], 0)

    async def test_rejects_second_connection(self):
        _, client = await self.client()
        reader, writer = await asyncio.open_connection("127.0.0.1", self.port)
        try:
            response = json.loads(await asyncio.wait_for(reader.readline(), 1))
            self.assertEqual(response["code"], "CONNECTION_BUSY")
        finally:
            writer.close()
            await writer.wait_closed()

    async def test_tcp_fragmented_hello_and_batched_requests(self):
        sender = Sender(self.world)
        reader, writer = await asyncio.open_connection("127.0.0.1", self.port)
        try:
            data = encode(sender.hello(self.token))
            for i in range(0, len(data), 7):
                writer.write(data[i:i + 7])
                await writer.drain()
            sender.accept_hello(json.loads(await reader.readline()))
            heart = envelope("heartbeat", sender.identities())
            writer.write(encode(heart) + encode(heart))
            await writer.drain()
            for _ in range(2):
                self.assertEqual(json.loads(await reader.readline())["type"], "state_snapshot")
        finally:
            writer.close()
            await writer.wait_closed()

    async def test_wrong_token_is_rejected_and_connection_closes(self):
        sender = Sender(self.world)
        reader, writer = await asyncio.open_connection("127.0.0.1", self.port)
        try:
            writer.write(encode(sender.hello("b" * 64)))
            await writer.drain()
            self.assertEqual(json.loads(await reader.readline())["code"], "AUTH_FAILED")
            self.assertEqual(await asyncio.wait_for(reader.read(1), 1), b"")
        finally:
            writer.close()
            await writer.wait_closed()

    async def test_partial_bytes_do_not_extend_valid_message_deadline(self):
        self.server.heartbeat_timeout = 0.15
        reader, writer = await asyncio.open_connection("127.0.0.1", self.port)
        try:
            writer.write(b'{"protocol_version":')
            await writer.drain()
            await asyncio.sleep(0.1)
            writer.write(b'1')
            await writer.drain()
            self.assertEqual(json.loads(await asyncio.wait_for(reader.readline(), 1))["code"], "HEARTBEAT_EXPIRED")
        finally:
            writer.close()
            await writer.wait_closed()


if __name__ == "__main__":
    unittest.main()
