import argparse
import asyncio
import json
from pathlib import Path
import secrets
import tempfile

from .domain import Receiver, Sender, initial_campaign, initial_world
from .network import BridgeClient, BridgeServer, deliver
from .protocol import BridgeError, uid
from .storage import JsonStore


def initialize(directory, ingots=64):
    directory = Path(directory)
    if any((directory / name).exists() for name in ("config.local.json", "campaign.json", "world.json")):
        raise BridgeError("SIMULATION_ALREADY_EXISTS")
    identities = {key: uid() for key in ("campaign_id", "world_id", "pair_id", "checkpoint_id")}
    JsonStore(directory / "campaign.json").save(initial_campaign(identities))
    JsonStore(directory / "world.json").save(initial_world(identities, ingots))
    JsonStore(directory / "config.local.json").save({"token": secrets.token_hex(32), "port": 28765})


def load(directory):
    directory = Path(directory)
    return (JsonStore(directory / "config.local.json").load(),
            JsonStore(directory / "campaign.json"), JsonStore(directory / "world.json"))


async def run_server(directory, drop):
    config, campaign, _ = load(directory)
    receiver = Receiver(campaign, config["token"])
    server = BridgeServer(receiver, drop)
    port = await server.start(config["port"])
    print(f"SIMULATION ONLY | listening on 127.0.0.1:{port} | Ctrl+C to stop", flush=True)
    try:
        await asyncio.Event().wait()
    finally:
        await server.close()


async def run_delivery(directory):
    config, _, world = load(directory)
    for delay in (0, 1, 2, 5, 10):
        if delay:
            await asyncio.sleep(delay)
        sender = Sender(world)
        client = BridgeClient(sender)
        try:
            snapshot = await client.connect(config["token"], config["port"])
            print(f"SIMULATION | {snapshot['campaign_name']} | {snapshot['zone']}", flush=True)
            result = await deliver(client, sender)
            print(json.dumps({"state": sender.state["delivery"]["state"], "reserve": result["reserve"],
                              "available_ingots": sender.state["available_ingots"],
                              "reserved_ingots": sender.state["reserved_ingots"]}))
            return
        except (ConnectionError, TimeoutError, OSError):
            print("Connection unavailable; saved cargo stays locked. Reconnecting...", flush=True)
        finally:
            await client.close()
    raise BridgeError("UNAVAILABLE")


async def demo(directory):
    initialize(directory, 65)
    config, campaign, world = load(directory)
    server = BridgeServer(Receiver(campaign, config["token"]), drop_commit_reply_once=True)
    port = await server.start(0)
    print("SIMULATION ONLY | 65 iron ingots | deliberate lost commit reply")
    try:
        for attempt in range(2):
            sender = Sender(world)
            client = BridgeClient(sender)
            try:
                await client.connect(config["token"], port)
                await deliver(client, sender)
            except ConnectionError:
                print("Reply lost after reward saved. Cargo locked; reconnecting with same delivery ID.")
                assert sender.state["reserved_ingots"] == 64
            finally:
                await client.close()
            await asyncio.sleep(0.01)
        # Restart both endpoints and read persisted state.
        reloaded = Sender(world)
        receiver = Receiver(campaign, config["token"])
        assert reloaded.state["completed"] and reloaded.state["available_ingots"] == 1
        assert reloaded.state["reserved_ingots"] == 0 and receiver.state["reserve"] == 10
        print("PASS | reload: 1 surplus ingot, 0 reserved, 10 logistics points, one receipt")
    finally:
        await server.close()


def main():
    parser = argparse.ArgumentParser(description="VOID DYNASTY development simulator; does not connect to games")
    parser.add_argument("command", choices=["init", "server", "deliver", "status", "demo"])
    parser.add_argument("--directory", default=".runtime/simulation")
    parser.add_argument("--ingots", type=int, default=64)
    parser.add_argument("--drop-commit-reply-once", action="store_true")
    args = parser.parse_args()
    try:
        if args.command == "init":
            if not 0 <= args.ingots <= 4096:
                parser.error("--ingots must be between 0 and 4096")
            initialize(args.directory, args.ingots)
            print("Created an explicitly paired SIMULATION. Config token is local and ignored by Git.")
        elif args.command == "server":
            asyncio.run(run_server(args.directory, args.drop_commit_reply_once))
        elif args.command == "deliver":
            asyncio.run(run_delivery(args.directory))
        elif args.command == "status":
            _, campaign, world = load(args.directory)
            print(json.dumps({"simulation": True, "campaign": campaign.load(), "world": world.load()}, indent=2))
        else:
            with tempfile.TemporaryDirectory(prefix="void-dynasty-") as directory:
                asyncio.run(demo(directory))
    except BridgeError as error:
        parser.exit(1, f"{error.code}\n")
    except (OSError, ValueError):
        parser.exit(1, "Cannot open simulation state. Run init or check the directory.\n")
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
