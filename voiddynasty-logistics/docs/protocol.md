# Protocol v1 — executable reference contract

This page describes the development simulator. The game adapters in 0.4.0 use the explicitly separate [live protocol v2](live-protocol.md), with the same fixed mission and canonical delivery fingerprint. Do not run the simulator server alongside the real receiver: both default to port 28765.

`protocol/schemas/message.schema.json` formalizes requests and responses. `protocol/mission.v1.json` is the fixed first mission: 64 `minecraft:iron_ingot`, reward 10 custom logistics points, once per pair. This milestone fixes the mission in source; configurable off-session mission versions remain future work. No native Rogue Trader currency is touched.

TCP NDJSON, UTF-8, exactly one compact JSON object per LF. Maximum **65,536 bytes including LF**. Unknown fields, duplicate JSON keys, non-finite numbers, invalid UTF-8, invalid IDs and incompatible protocol versions are rejected. A frame may span packets; multiple frames may share a packet. A trailing partial frame at disconnect has no effect. IDs are lowercase canonical UUID strings.

The simulator binds only `127.0.0.1`, defaults to port 28765, admits one connection, bounds receive frames, and serializes mutations on one asyncio event loop. There is no unbounded outbound queue. The client permits at most eight pending requests. Backpressure has a two-second drain timeout.

## Handshake and state

The simulation `init` command explicitly creates paired world and campaign fixtures, identities, checkpoint and random token. It is development tooling; real in-game pairing confirmation is not implemented.

`hello` supplies token, mission hash, pair/campaign/world/checkpoint IDs, client's provisional session and known committed receipts. The receiver verifies configured identities and returns `hello_ack` with the current session, campaign/zone labels, availability, reserve, completion and receipt set. Every response carries its own `message_id`; `reply_to` correlates it with a request.

The receiver generates a new `session_id` on each receiver load. Every later request must use that session. The sender adopts it only after verifying the handshake. `heartbeat` requests occur every two seconds; the response is a fresh `state_snapshot`. Six seconds without a valid request or response invalidates the connection. Invalid or incomplete bytes do not extend the server's deadline. Snapshots are polled by heartbeat in this simulator; production adapters still need on-change publication (at most once a second in normal operation).

`mission_hash` is lowercase SHA-256 of the full mission JSON, encoded UTF-8 with keys sorted, no spaces, and no trailing LF. Values are ASCII in v1. Cross-language implementations must run shared hash fixtures before claiming compatibility.

## Delivery

| Request | Successful result | Durable effect |
|---|---|---|
| `delivery_status` | `UNKNOWN`, `PREPARED`, `REWARD_COMMITTED`, `CANCELLED` | None |
| `delivery_prepare` | `delivery_prepared` | Save preparation; no reward |
| `delivery_commit` | `delivery_result` | Save receipt, completed mission and reserve together |
| `delivery_cancel` | `delivery_result` | Save cancellation tombstone before sender may release cargo |

Every delivery request includes fixed cargo, mission identity/version and the same `delivery_id`. The receiver calculates the reward itself. Sending an extra `reward` field is rejected. A delivery fingerprint hashes the canonical object containing `pair_id`, `campaign_id`, `world_id`, `mission_id`, `mission_version`, `cargo`. Session, checkpoint and message IDs are excluded; those are checked separately.

Only one prepared delivery is permitted per pair. A repeated commit with identical content returns the existing receipt. Changed content under the same delivery ID yields `PAYLOAD_MISMATCH`. A committed delivery cannot be cancelled. A cancellation received before preparation saves a tombstone so delayed prepare cannot revive it. Simulation ledgers are capped at 128 entries; no silent eviction of receipts or cancellation evidence occurs.

The sender saves `DRAFT` before sending prepare and `CARGO_RESERVED` before commit. `PREPARED` is a receiver state; the local durable reservation records the transition after preparation. A received durable commit settles the reservation. A timeout or disconnect keeps it locked. Reconnect uses the saved delivery ID, queries status, and either settles, resumes a known preparation, or requires recovery. It never creates a replacement delivery to resolve uncertainty.

## Principal errors

Authentication/version/validation: `AUTH_FAILED`, `PROTOCOL_MISMATCH`, `INVALID_MESSAGE`, `INVALID_JSON`, `INVALID_CARGO`, `UNKNOWN_MESSAGE`, `FRAME_TOO_LARGE`, `HANDSHAKE_REQUIRED`.

Identity/recovery: `IDENTITY_MISMATCH`, `STALE_SESSION`, `RECOVERY_REQUIRED`, `PAYLOAD_MISMATCH`, `MISSION_MISMATCH`.

Transaction/availability: `UNAVAILABLE`, `NOT_PREPARED`, `MISSION_COMPLETED`, `DELIVERY_IN_PROGRESS`, `PERSISTENCE_FAILED`, `LEDGER_FULL`, `CONNECTION_BUSY`, `HEARTBEAT_EXPIRED`.

Errors contain bounded codes, not supplied payloads, tokens or exception dumps. Tokens are generated into ignored local configuration, sent only during hello, and never printed by the simulator. The CLI retries transient connections at 1, 2, 5 and 10 seconds, then exits with cargo still locked. Long-running in-game reconnect loops remain adapter work.
