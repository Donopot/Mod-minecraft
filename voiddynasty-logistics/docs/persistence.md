# Persistence and recovery boundaries

## What is implemented

The development simulator has one campaign JSON document holding identities, checkpoint, reserve, completion and receiver delivery ledger, and one world JSON document holding available/reserved cargo, active delivery and receipts. Each update writes a same-directory temporary file, flushes, calls `fsync`, then atomically replaces that single document. In-memory state advances only after save returns. A write exception disables further mutations for that endpoint; it does not acknowledge a reward or release a reservation.

These files are simulation fixtures. **A global JSON file is not used as a substitute for Rogue Trader per-save state.** The real RT persistence probe has now saved and reloaded a zero-reserve receipt successfully. Version 0.4.0 stores pairing, preparation, reward and receipt in the same namespaced per-save string, saves through `Game.SaveGame`, waits for callback/idle engine, and reads the new save archive before responding.

The live Minecraft adapter uses `data/voiddynasty-bridge.json` inside its world for identities, receipt and transaction phase. It writes a flushed temporary file and atomic replacement. Real inventory and the reserved 64 ingots are stored separately in the dedicated block entity. It writes `RESERVING` before the chunk mutation, forces a native world save, then records `RESERVED`; settlement similarly uses `SETTLING` before `SETTLED`. A leftover intent or mismatch on restart requires recovery. The container UUID, world UUID, dimension and position must match the world registry. These engine operations still need full real-world acceptance testing.

The separate read-only diagnostic channel uses a global **configuration** file for its random local token and port. That file has no campaign identity, checkpoint, reserve or receipt ledger. Native campaign ID and zone exist only in current diagnostic snapshots and are never treated as durable transaction identities.

Reconnection compares pair/campaign/world IDs, checkpoint and receipt evidence. An old sender save missing a remote committed receipt blocks. An old receiver save missing a sender's known receipt blocks. If a reserved delivery is unknown to the receiver, cargo stays locked and the sender enters recovery. A receiver receipt is accepted on reconnect only if the sender already knows it or has the corresponding locked reservation.

## What is not proved

Atomic replacement of one simulation JSON document does not establish atomicity between two games, Minecraft regions/inventory/world state, or the physical disk. Sudden power loss, corruption during replacement, both fixtures being rolled back together, and shared mutable stores opened by multiple processes are outside the demonstrated guarantees. Run one simulation server and one sender; fixture files are not a multi-process database. An externally modified or corrupt fixture must be restored from a known paired checkpoint, not guessed from names.

The installed Rogue Trader build has changed entity and persistence APIs. The zero-reserve baseline/receipt/final-proof cycle has passed. The remaining delivery-level failure tests extend that evidence; they must not be treated as already proved:

1. On A, initialize a bridge campaign UUID and serialize a single test state object containing a fake receipt and reserve.
2. Save A, close the game, reload A, verify all three values.
3. Load B and confirm A's data is absent.
4. Save B with distinct values, reload A, and confirm A is unchanged.
5. Fail or interrupt a save and verify no durable delivery result can escape before completion.
6. Load an older branch of A and verify checkpoint/receipt reconciliation blocks mutation.

Do not claim game transaction validation if any of these fails. Native campaign `GameId` is a diagnostic clue and is not a bridge campaign UUID or proof of timeline freshness.

## Paired checkpoint procedure for future in-game testing

1. Reach a stable state with no prepared delivery or reserved cargo. Record mission version/hash and checkpoint UUID in both game saves.
2. Save both games and close them cleanly.
3. Copy the entire disposable Minecraft world, the matching RT save, bridge data and relevant configurations into a new checkpoint directory. Preserve local tokens privately; omit them from shared reports.
4. Write a manifest containing game/mod versions, identities and SHA-256 of copied files. Verify copied hashes. Never overwrite the only known-good checkpoint.
5. Restore both sides together if reconciliation cannot prove the outcome. No generic force-reward or timeout-refund command is provided.

A future checkpoint tool must verify both saves contain the same marker before calling the pair consistent. Copying unrelated files or merely assigning a new UUID does not create consistency.
