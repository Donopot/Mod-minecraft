"""Executable reference semantics for simulator saves; not a game adapter."""
import copy
import hmac

from .protocol import (BridgeError, IDENTITIES, MISSION, MISSION_HASH, digest,
                       envelope, payload, uid, validate_request, validate_response)


def initial_campaign(identities):
    return {**identities, "campaign_name": "SIMULATION / Test dynasty", "zone": "SIMULATION / Voidship bridge",
            "available": True, "reserve": 0, "deliveries": {}, "completed": False}


class Receiver:
    def __init__(self, store, token):
        self.store = store
        self.state = store.load()
        self.token = token
        self.session_id = uid()  # Never persisted as a durable identity.

    def identities(self):
        return {key: self.session_id if key == "session_id" else self.state[key] for key in IDENTITIES}

    def snapshot(self):
        return {"campaign_name": self.state["campaign_name"], "zone": self.state["zone"],
                "available": self.state["available"], "reserve": self.state["reserve"],
                "receipts": sorted(k for k, v in self.state["deliveries"].items() if v["state"] == "REWARD_COMMITTED"),
                "completed": self.state["completed"], "mission_hash": MISSION_HASH}

    def _persist(self, updated):
        try:
            self.store.save(updated)
        except OSError:
            # The outcome of a failing disk write is uncertain. Stop mutations.
            self.state["available"] = False
            raise BridgeError("PERSISTENCE_FAILED") from None
        self.state = updated

    def handle(self, message, authenticated=False):
        validate_request(message)
        kind = message["type"]
        if kind == "hello":
            if not hmac.compare_digest(message["token"].encode("utf-8"), self.token.encode("utf-8")):
                raise BridgeError("AUTH_FAILED")
            if message["mission_hash"] != MISSION_HASH:
                raise BridgeError("MISSION_MISMATCH")
        elif not authenticated:
            raise BridgeError("HANDSHAKE_REQUIRED")
        for key in ("campaign_id", "world_id", "pair_id"):
            if message[key] != self.state[key]:
                raise BridgeError("IDENTITY_MISMATCH")
        if message["checkpoint_id"] != self.state["checkpoint_id"]:
            raise BridgeError("RECOVERY_REQUIRED")
        if kind == "hello":
            if not set(message["known_receipts"]).issubset(self.snapshot()["receipts"]):
                raise BridgeError("RECOVERY_REQUIRED")
            return envelope("hello_ack", self.identities(), reply_to=message["message_id"], **self.snapshot())
        if message["session_id"] != self.session_id:
            raise BridgeError("STALE_SESSION")
        if kind == "heartbeat":
            return envelope("state_snapshot", self.identities(), reply_to=message["message_id"], **self.snapshot())
        delivery_id = message["delivery_id"]
        existing = self.state["deliveries"].get(delivery_id)
        fingerprint = digest(payload(message))
        if existing and existing["payload_hash"] != fingerprint:
            raise BridgeError("PAYLOAD_MISMATCH")
        if any(message[key] != MISSION[key] for key in ("mission_id", "mission_version", "cargo")):
            raise BridgeError("MISSION_MISMATCH")
        if kind == "delivery_status":
            return self._result(message, existing)
        if existing and existing["state"] in {"REWARD_COMMITTED", "CANCELLED"}:
            return self._result(message, existing)
        if not self.state["available"]:
            raise BridgeError("UNAVAILABLE")
        updated = copy.deepcopy(self.state)
        if kind == "delivery_prepare":
            if not existing:
                if self.state["completed"]:
                    raise BridgeError("MISSION_COMPLETED")
                if any(v["state"] == "PREPARED" for v in self.state["deliveries"].values()):
                    raise BridgeError("DELIVERY_IN_PROGRESS")
                if len(updated["deliveries"]) >= 128:
                    raise BridgeError("LEDGER_FULL")
                updated["deliveries"][delivery_id] = {"state": "PREPARED", "payload_hash": fingerprint}
                self._persist(updated)
        elif kind == "delivery_commit":
            if not existing:
                raise BridgeError("NOT_PREPARED")
            updated["reserve"] += MISSION["logistics_reward"]
            updated["completed"] = True
            updated["deliveries"][delivery_id] = {**existing, "state": "REWARD_COMMITTED", "reserve": updated["reserve"]}
            self._persist(updated)
        elif kind == "delivery_cancel":
            # Tombstone even when prepare response was lost; subsequent prepare cannot resurrect it.
            if not existing and len(updated["deliveries"]) >= 128:
                raise BridgeError("LEDGER_FULL")
            updated["deliveries"][delivery_id] = {"state": "CANCELLED", "payload_hash": fingerprint}
            self._persist(updated)
        return self._result(message, self.state["deliveries"].get(delivery_id))

    def _result(self, message, record):
        return envelope("delivery_prepared" if message["type"] == "delivery_prepare" else "delivery_result",
                        self.identities(), reply_to=message["message_id"], delivery_id=message["delivery_id"],
                        state=record["state"] if record else "UNKNOWN", payload_hash=digest(payload(message)),
                        reserve=self.state["reserve"])


def initial_world(identities, ingots=64):
    return {**identities, "available_ingots": ingots, "reserved_ingots": 0, "delivery": None,
            "receipts": [], "completed": False, "recovery_required": False}


class Sender:
    def __init__(self, store):
        self.store = store
        self.state = store.load()
        self.session_id = uid()
        self.connected = False

    def identities(self):
        return {key: self.session_id if key == "session_id" else self.state[key] for key in IDENTITIES}

    def _persist(self, updated):
        try:
            self.store.save(updated)
        except OSError:
            self.connected = False
            self.state["recovery_required"] = True
            raise BridgeError("PERSISTENCE_FAILED") from None
        self.state = updated

    def hello(self, token):
        return envelope("hello", self.identities(), token=token, mission_hash=MISSION_HASH,
                        known_receipts=self.state["receipts"])

    def accept_hello(self, response):
        self.connected = False
        validate_response(response)
        if response["type"] != "hello_ack":
            raise BridgeError("INVALID_RESPONSE")
        for key in ("campaign_id", "world_id", "pair_id", "checkpoint_id"):
            if response[key] != self.state[key]:
                self._recover()
        if response["mission_hash"] != MISSION_HASH:
            raise BridgeError("MISSION_MISMATCH")
        remote = set(response["receipts"])
        expected = set(self.state["receipts"])
        delivery = self.state["delivery"]
        if delivery and self.state["reserved_ingots"] == 64:
            expected.add(delivery["delivery_id"])
        if not set(self.state["receipts"]).issubset(remote) or not remote.issubset(expected):
            self._recover()
        self.session_id = response["session_id"]
        self.connected = True

    def _recover(self):
        self.connected = False
        updated = copy.deepcopy(self.state)
        updated["recovery_required"] = True
        self._persist(updated)
        raise BridgeError("RECOVERY_REQUIRED")

    def draft(self):
        if not self.connected:
            raise BridgeError("UNAVAILABLE")
        if self.state["recovery_required"]:
            raise BridgeError("RECOVERY_REQUIRED")
        if self.state["completed"]:
            raise BridgeError("MISSION_COMPLETED")
        if self.state["delivery"]:
            return self.state["delivery"]["delivery_id"]
        if self.state["available_ingots"] < 64:
            raise BridgeError("INSUFFICIENT_CARGO")
        updated = copy.deepcopy(self.state)
        updated["delivery"] = {"delivery_id": uid(), "state": "DRAFT"}
        self._persist(updated)
        return updated["delivery"]["delivery_id"]

    def request(self, kind):
        if not self.connected or self.state["recovery_required"]:
            raise BridgeError("UNAVAILABLE")
        if kind == "delivery_commit" and self.state["reserved_ingots"] != 64:
            raise BridgeError("CARGO_NOT_RESERVED")
        if not self.state["delivery"]:
            raise BridgeError("NO_DELIVERY")
        return envelope(kind, self.identities(), delivery_id=self.state["delivery"]["delivery_id"],
                        **{key: MISSION[key] for key in ("mission_id", "mission_version", "cargo")})

    def apply_result(self, response):
        validate_response(response)
        if response["type"] not in {"delivery_result", "delivery_prepared"}:
            raise BridgeError("INVALID_RESPONSE")
        delivery = self.state["delivery"]
        if not delivery:
            self._recover()
        expected = self.request("delivery_status")
        if (any(response[key] != self.identities()[key] for key in IDENTITIES)
                or response["delivery_id"] != delivery["delivery_id"]
                or response["payload_hash"] != digest(payload(expected))):
            self._recover()
        updated = copy.deepcopy(self.state)
        state = response["state"]
        if state == "PREPARED":
            if delivery["state"] == "DRAFT":
                if updated["available_ingots"] < 64:
                    self._recover()
                updated["available_ingots"] -= 64
                updated["reserved_ingots"] = 64
                updated["delivery"]["state"] = "CARGO_RESERVED"
            elif delivery["state"] != "CARGO_RESERVED":
                self._recover()
        elif state == "REWARD_COMMITTED":
            if delivery["state"] == "SETTLED":
                return
            if updated["reserved_ingots"] != 64:
                self._recover()
            updated["reserved_ingots"] = 0
            updated["completed"] = True
            updated["receipts"] = [delivery["delivery_id"]]
            updated["delivery"]["state"] = "SETTLED"
        elif state == "CANCELLED":
            if delivery["state"] == "SETTLED":
                self._recover()
            updated["available_ingots"] += updated["reserved_ingots"]
            updated["reserved_ingots"] = 0
            updated["delivery"] = None
        elif state == "UNKNOWN":
            # Safe retry before reservation; never release a locked cargo on uncertainty.
            if delivery["state"] != "DRAFT":
                self._recover()
            return
        else:
            self._recover()
        self._persist(updated)
