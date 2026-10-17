"""Generate reviewable JSON Schema and example messages; no external packages."""
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from void_bridge.protocol import ERRORS, IDENTITIES, MISSION, MISSION_HASH, envelope

UUID = {"type": "string", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", "format": "uuid"}
HASH = {"type": "string", "pattern": "^[0-9a-f]{64}$"}
CARGO = {"type": "array", "minItems": 1, "maxItems": 1, "items": {
    "type": "object", "additionalProperties": False, "required": ["item_id", "count"],
    "properties": {"item_id": {"type": "string", "maxLength": 128},
                   "count": {"type": "integer", "minimum": 1, "maximum": 4096}}}}


def schema(kind, extra=None, identity=True):
    properties = {"protocol_version": {"const": 1, "type": "integer"}, "type": {"const": kind}, "message_id": UUID}
    if identity:
        properties.update({key: UUID for key in IDENTITIES})
    properties.update(extra or {})
    return {"type": "object", "additionalProperties": False, "required": list(properties), "properties": properties}


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def main():
    definitions = {}
    delivery = {"delivery_id": UUID, "mission_id": {"type": "string"}, "mission_version": {"type": "integer"}, "cargo": CARGO}
    definitions["hello"] = schema("hello", {"token": {"type": "string", "minLength": 32, "maxLength": 256},
        "mission_hash": HASH, "known_receipts": {"type": "array", "maxItems": 1, "items": UUID}})
    definitions["heartbeat"] = schema("heartbeat")
    for kind in ("delivery_prepare", "delivery_commit", "delivery_status", "delivery_cancel"):
        definitions[kind] = schema(kind, delivery)
    snapshot = {"reply_to": UUID, "campaign_name": {"type": "string"}, "zone": {"type": "string"},
        "available": {"type": "boolean"}, "reserve": {"type": "integer", "minimum": 0},
        "receipts": {"type": "array", "maxItems": 1, "items": UUID}, "completed": {"type": "boolean"}, "mission_hash": HASH}
    for kind in ("hello_ack", "state_snapshot"):
        definitions[kind] = schema(kind, snapshot)
    result = {"reply_to": UUID, "delivery_id": UUID, "state": {"enum": ["UNKNOWN", "PREPARED", "REWARD_COMMITTED", "CANCELLED"]},
              "payload_hash": HASH, "reserve": {"type": "integer", "minimum": 0}}
    for kind in ("delivery_prepared", "delivery_result"):
        definitions[kind] = schema(kind, result)
    definitions["error"] = schema("error", {"code": {"enum": ERRORS}}, identity=False)
    definitions["error"]["properties"]["reply_to"] = {"anyOf": [UUID, {"type": "null"}]}
    document = {"$schema": "https://json-schema.org/draft/2020-12/schema", "$id": "urn:void-dynasty:protocol:1",
        "title": "VOID DYNASTY bridge protocol v1", "$comment": "UTF-8 NDJSON; max 65536 bytes including LF. Semantic identity, authentication and state checks are mandatory beyond schema validation.",
        "oneOf": [{"$ref": "#/$defs/" + kind} for kind in definitions], "$defs": definitions}
    write(ROOT / "protocol/schemas/message.schema.json", document)
    diagnostic_base = {"protocol_version": {"type": "integer", "const": 1}}
    hello_fields = {**diagnostic_base, "type": {"const": "diagnostic_hello"},
                    "token": {"type": "string", "pattern": "^[0-9a-f]{64}$", "writeOnly": True}}
    snapshot_fields = {**diagnostic_base, "type": {"const": "diagnostic_snapshot"},
                       "diagnostic_only": {"const": True}, "delivery_enabled": {"const": False},
                       "session_id": UUID, "sequence": {"type": "integer", "minimum": 1},
                       "campaign_loaded": {"type": "boolean"},
                       "native_campaign_id": {"type": ["string", "null"], "maxLength": 256},
                       "zone": {"type": ["string", "null"], "maxLength": 1024},
                       "game_mode": {"type": "string", "maxLength": 128},
                       "save_state": {"type": "string", "maxLength": 128},
                       "status": {"type": "string", "maxLength": 1024}}
    diagnostic = {"$schema": "https://json-schema.org/draft/2020-12/schema", "$id": "urn:void-dynasty:diagnostic:1",
                  "title": "VOID DYNASTY read-only diagnostic channel v1",
                  "$comment": "Separate from delivery protocol. Hello max 4096 UTF-8 bytes including LF; snapshots max 65536.",
                  "oneOf": [{"type": "object", "additionalProperties": False, "required": list(properties),
                             "properties": properties} for properties in (hello_fields, snapshot_fields)]}
    write(ROOT / "protocol/schemas/diagnostic.schema.json", diagnostic)
    write(ROOT / "protocol/mission.v1.json", MISSION)
    identities = {key: f"00000000-0000-4000-8000-{i:012d}" for i, key in enumerate(IDENTITIES, 1)}
    hello = envelope("hello", identities, token="EXAMPLE_ONLY_GENERATE_A_LOCAL_RANDOM_TOKEN", mission_hash=MISSION_HASH, known_receipts=[])
    hello["message_id"] = "00000000-0000-4000-8000-000000000010"
    write(ROOT / "protocol/examples/hello.json", hello)
    prepare = envelope("delivery_prepare", identities, delivery_id="00000000-0000-4000-8000-000000000020",
                       **{key: MISSION[key] for key in ("mission_id", "mission_version", "cargo")})
    prepare["message_id"] = "00000000-0000-4000-8000-000000000011"
    write(ROOT / "protocol/examples/delivery_prepare.json", prepare)


if __name__ == "__main__":
    main()
