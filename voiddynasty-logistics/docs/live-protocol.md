Current version: **0.6.0 native cargo missions, protocol v4**. See [the current guide](native-cargo-missions.md). The description below records the earlier logistics version.

# Live adapter protocols

The current adapters **0.5.0 use protocol v3**, described in [repeatable deliveries](repeatable-deliveries.md), with `live-v3-request.schema.json` and `live-v3-response.schema.json`. Both adapters must be updated together. The v2 description below is retained as history for 0.4.x.

# Live adapter protocol v2 — 0.4.0

This deliberately separate transport accompanies the simulator v1 contract. It reuses the fixed mission and canonical payload hash, but does not claim wire compatibility with the simulator. Both receivers default to loopback port 28765 and must not run together.

One authenticated request and one response per TCP connection, UTF-8 NDJSON, at most 4096 bytes including LF. The C# worker authenticates the local shared token and strictly parses every primitive field, rejecting duplicates, unknown fields and wrong types. A single bounded queued job is handled on Unity's main thread. Replies are completed only after that handler returns; mutations additionally wait for the native save callback, idle engine and matching saved archive contents. Connections carry no arbitrary commands or code.

The Minecraft integrated server creates each request, then delegates only byte I/O to its worker. Responses return to the same server instance; disposal closes its socket and prevents replies affecting another world. Normal status polling is every two seconds. The UI expires connection readiness after six seconds without a fresh response; an in-flight native save can take longer and disables new actions. Disconnects retain the delivery ID and escrow. This prototype uses a 130-second response timeout while native saves have a 120-second limit.

## Requests

Every request contains all fields in `protocol/schemas/live-v2-request.schema.json`. `inspect` obtains a fresh session and read-only state. `propose_pair` requests GUI approval, with no reward; approval creates a new RT save before the next status can report a matching pair. Other operations require the loaded bridge campaign UUID, world UUID, pair UUID, initial checkpoint UUID and current session.

| Type | Effect |
|---|---|
| `inspect` | Read current campaign/session/state; never pairs or mutates. |
| `propose_pair` | Present the world's explicit proposal for local RT GUI approval. |
| `status` | Reconcile local delivery/receipt with the saved receiver state. |
| `prepare` | Persist the single delivery ID and canonical hash with reserve zero. |
| `commit` | Require a persisted local reservation, then save receipt and custom reserve 10 together. Repeats retain 10. |

The first integration supports one pair per RT bridge state, one fixed mission and one delivery ID. The receiver derives the hash from known identities and the fixed 64-iron mission; it never accepts a client-provided reward. All other item/count/hash combinations are rejected. Mission and payload hashing use the same fixture in Java, C# and Python tests.

## Responses and recovery

Replies contain protocol version, correlated `reply_to`, bounded code, session/campaign/world/pair/checkpoint identities, receipt ID/state/hash, reserve, zone, campaign label and availability. A complete strict Java parser rejects duplicate/extra fields and invalid numeric values. `PAIR_APPROVAL_REQUIRED` directs the player to the RT panel; only `OK` with matching identities allows a transaction to progress.

The world journal phases are `IDLE`, `DRAFT`, `RESERVING`, `RESERVED`, `SETTLING`, `SETTLED`, `RECOVERY_REQUIRED`. Receiver states are `IDLE`, `PREPARED`, `REWARD_COMMITTED`. A missing preparation under reserved cargo, settled sender with a missing receiver receipt, committed receiver under an unreserved sender, identity mismatch, copied/missing container or interrupted intent blocks progress. Nothing refunds on timeout. `Reprendre / recu` queries the existing ID and resumes only when the saved evidence agrees.

Cancellation/tombstone messages, multiple pairs, automatic paired-checkpoint creation, dynamic mission configuration and atomic crash recovery across all game files remain outside this live slice. The simulator v1 exercises a broader cancellation contract. Real end-to-end acceptance is recorded separately; compilation and harness tests do not prove engine behavior.

## Availability clarification in 0.4.1

A successful inspect/status may return PAUSED_MODE or PAUSED_COMBAT with fresh identities and saved receipt data. The connection remains established; available=false prevents writes. Main-menu/unloading returns UNAVAILABLE. Polling does not count as a delivery mutation or invalidate the UI confirmation. No wire fields or protocol version changed.
