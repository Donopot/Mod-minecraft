"""Version 1 NDJSON contract; deliberately independent of either game's APIs."""
import copy
import hashlib
import json
import re
import uuid

VERSION = 1
MAX_FRAME = 65_536  # Includes LF; measured in UTF-8 bytes.
IDENTITIES = ("session_id", "pair_id", "campaign_id", "world_id", "checkpoint_id")
DELIVERY_TYPES = {"delivery_prepare", "delivery_commit", "delivery_status", "delivery_cancel"}
REQUEST_TYPES = DELIVERY_TYPES | {"hello", "heartbeat"}
ERRORS = ("AUTH_FAILED", "PROTOCOL_MISMATCH", "INVALID_JSON", "INVALID_MESSAGE", "UNKNOWN_MESSAGE",
          "FRAME_TOO_LARGE", "HANDSHAKE_REQUIRED", "MISSION_MISMATCH", "INVALID_CARGO", "IDENTITY_MISMATCH",
          "STALE_SESSION", "RECOVERY_REQUIRED", "PAYLOAD_MISMATCH", "UNAVAILABLE", "NOT_PREPARED",
          "MISSION_COMPLETED", "DELIVERY_IN_PROGRESS", "PERSISTENCE_FAILED", "CONNECTION_BUSY",
          "HEARTBEAT_EXPIRED", "LEDGER_FULL")
MISSION = {
    "mission_id": "logistics_first_delivery", "mission_version": 1,
    "cargo": [{"item_id": "minecraft:iron_ingot", "count": 64}], "logistics_reward": 10,
}


class BridgeError(Exception):
    def __init__(self, code):
        super().__init__(code)
        self.code = code


def uid():
    return str(uuid.uuid4())


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(",", ":"), ensure_ascii=False, allow_nan=False)


def digest(value):
    return hashlib.sha256(canonical(value).encode("utf-8")).hexdigest()


MISSION_HASH = digest(MISSION)


def _object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise BridgeError("INVALID_JSON")
        result[key] = value
    return result


def decode(line):
    if len(line) + 1 > MAX_FRAME:
        raise BridgeError("FRAME_TOO_LARGE")
    try:
        value = json.loads(line.decode("utf-8"), object_pairs_hook=_object,
                           parse_constant=lambda _: (_ for _ in ()).throw(BridgeError("INVALID_JSON")))
    except (ValueError, UnicodeError, RecursionError):
        raise BridgeError("INVALID_JSON") from None
    if not isinstance(value, dict):
        raise BridgeError("INVALID_MESSAGE")
    return value


def encode(message):
    data = (canonical(message) + "\n").encode("utf-8")
    if len(data) > MAX_FRAME:
        raise BridgeError("FRAME_TOO_LARGE")
    return data


class Frames:
    """Accept fragmented or coalesced TCP bytes without an unbounded buffer."""
    def __init__(self):
        self.buffer = bytearray()

    def feed(self, data):
        messages = []
        for fragment in data.splitlines(keepends=True):
            self.buffer.extend(fragment)
            if len(self.buffer) >= MAX_FRAME and not self.buffer.endswith(b"\n"):
                raise BridgeError("FRAME_TOO_LARGE")
            if self.buffer.endswith(b"\n"):
                messages.append(decode(bytes(self.buffer[:-1])))
                self.buffer.clear()
        return messages


def valid_uuid(value):
    try:
        return isinstance(value, str) and str(uuid.UUID(value)) == value
    except ValueError:
        return False


def validate_request(message):
    if type(message.get("protocol_version")) is not int or message["protocol_version"] != VERSION:
        raise BridgeError("PROTOCOL_MISMATCH")
    if not valid_uuid(message.get("message_id")):
        raise BridgeError("INVALID_MESSAGE")
    kind = message.get("type")
    if not isinstance(kind, str) or kind not in REQUEST_TYPES:
        raise BridgeError("UNKNOWN_MESSAGE")
    fields = {"protocol_version", "type", "message_id"} | set(IDENTITIES)
    if kind == "hello":
        fields |= {"token", "mission_hash", "known_receipts"}
        if not isinstance(message.get("token"), str) or not 32 <= len(message["token"]) <= 256:
            raise BridgeError("AUTH_FAILED")
        receipts = message.get("known_receipts")
        if not isinstance(receipts, list) or len(receipts) > 1 or any(not valid_uuid(x) for x in receipts):
            raise BridgeError("INVALID_MESSAGE")
        if not isinstance(message.get("mission_hash"), str):
            raise BridgeError("INVALID_MESSAGE")
    for key in IDENTITIES:
        if not valid_uuid(message.get(key)):
            raise BridgeError("INVALID_MESSAGE")
    if kind in DELIVERY_TYPES:
        fields |= {"delivery_id", "mission_id", "mission_version", "cargo"}
        if not valid_uuid(message.get("delivery_id")):
            raise BridgeError("INVALID_MESSAGE")
        if type(message.get("mission_version")) is not int or not isinstance(message.get("mission_id"), str):
            raise BridgeError("INVALID_MESSAGE")
        cargo = message.get("cargo")
        if not isinstance(cargo, list) or len(cargo) != 1:
            raise BridgeError("INVALID_CARGO")
        item = cargo[0]
        if (not isinstance(item, dict) or set(item) != {"item_id", "count"}
                or not isinstance(item["item_id"], str) or len(item["item_id"]) > 128
                or type(item["count"]) is not int or not 1 <= item["count"] <= 4096):
            raise BridgeError("INVALID_CARGO")
    if set(message) != fields:
        raise BridgeError("INVALID_MESSAGE")


def validate_response(message):
    if type(message.get("protocol_version")) is not int or message["protocol_version"] != VERSION:
        raise BridgeError("INVALID_RESPONSE")
    if not valid_uuid(message.get("message_id")):
        raise BridgeError("INVALID_RESPONSE")
    kind = message.get("type")
    if not isinstance(kind, str):
        raise BridgeError("INVALID_RESPONSE")
    if kind == "error":
        if message.get("code") not in ERRORS:
            raise BridgeError("INVALID_RESPONSE")
        if message.get("reply_to") is not None and not valid_uuid(message["reply_to"]):
            raise BridgeError("INVALID_RESPONSE")
        if set(message) - {"protocol_version", "type", "message_id", "reply_to", "code"}:
            raise BridgeError("INVALID_RESPONSE")
        return
    if kind not in {"hello_ack", "state_snapshot", "delivery_prepared", "delivery_result"}:
        raise BridgeError("INVALID_RESPONSE")
    fields = {"protocol_version", "type", "message_id", "reply_to", "reserve"} | set(IDENTITIES)
    if any(not valid_uuid(message.get(key)) for key in (*IDENTITIES, "reply_to")):
        raise BridgeError("INVALID_RESPONSE")
    if type(message.get("reserve")) is not int or message["reserve"] < 0:
        raise BridgeError("INVALID_RESPONSE")
    if kind in {"hello_ack", "state_snapshot"}:
        fields |= {"campaign_name", "zone", "available", "receipts", "completed", "mission_hash"}
        if (not isinstance(message.get("campaign_name"), str) or not isinstance(message.get("zone"), str)
                or type(message.get("available")) is not bool or type(message.get("completed")) is not bool
                or not isinstance(message.get("mission_hash"), str)
                or not re.fullmatch("[0-9a-f]{64}", message["mission_hash"])):
            raise BridgeError("INVALID_RESPONSE")
        receipts = message.get("receipts")
        if not isinstance(receipts, list) or len(receipts) > 1 or any(not valid_uuid(x) for x in receipts):
            raise BridgeError("INVALID_RESPONSE")
    else:
        fields |= {"delivery_id", "state", "payload_hash"}
        if (not valid_uuid(message.get("delivery_id"))
                or message.get("state") not in ("UNKNOWN", "PREPARED", "REWARD_COMMITTED", "CANCELLED")
                or not isinstance(message.get("payload_hash"), str)
                or not re.fullmatch("[0-9a-f]{64}", message["payload_hash"])):
            raise BridgeError("INVALID_RESPONSE")
    if set(message) != fields:
        raise BridgeError("INVALID_RESPONSE")


def payload(message):
    return {key: message[key] for key in ("pair_id", "campaign_id", "world_id", "mission_id", "mission_version", "cargo")}


def envelope(kind, identities, **extra):
    return copy.deepcopy({"protocol_version": VERSION, "type": kind, "message_id": uid(), **identities, **extra})

