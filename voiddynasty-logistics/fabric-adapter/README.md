# Fabric diagnostic companion — 0.2.0

The user confirmed that the original JAR is all that remains of VOID DYNASTY. This is an independent client companion, `voiddynasty_bridge`, requiring `voiddynasty` alongside it. It neither imports original mod classes nor changes ship generation or movement. No reconstruction of the original project is claimed.

Confirmed local target: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, and Java 25. GeckoLib 5.5.7 is installed for the original mod. A Downloads JDK archive was extracted into ignored `.runtime/jdk/` for development.

`tools/build-fabric-diagnostic.py` compiles with Java 25 against the actual installed game and Fabric module JARs. These game classes already expose the named interfaces used here. `javap` and compilation confirmed `ClientCommands.literal`, command registration, lifecycle callbacks, `FabricClientCommandSource.sendFeedback`, `Minecraft.hasSingleplayerServer`, and `Component.literal`. Build output contains only the companion's classes and metadata.

## Current behavior

`/voidbridge` or `/voidbridge status` prints connection state, native RT campaign ID, zone and mode in a solo world. A daemon worker reads authenticated snapshots from loopback port 28766. No Minecraft API is accessed from that worker; the command displays an immutable snapshot on the client thread.

Disconnects clear the cached campaign. State expires after six seconds without an advancing game snapshot, even if the RT worker retransmits the same data. Reconnect uses delays of 1, 2, 5 and 10 seconds. Return-to-menu snapshots explicitly clear campaign and zone. Client shutdown closes the socket and worker. There are no inventories, server mutations or ship hooks in this slice.

The original JAR was verified at `C:\Users\Anwender\Desktop\MOD\voiddynasty-1.0.0.jar`, SHA-256 `9d98712d9fe37133dcea81d992e99fcf1a13618ebbb589350b60ee0c4159e98e`. It matches the previously inspected Downloads copy. No Java sources or build files are present in that JAR.

## Next integration work

1. Prove RT per-save state and receipt reload before enabling business messages.
2. Add a server-side companion entrypoint, world identity, explicit pairing and durable world registry using verified 26.3 APIs.
3. Add a stationary terminal and iron-only inventory with confirmation, then implement persistent cargo reservation.
4. Verify destruction, hopper, explosion, copying and ship movement before placing a reserved container aboard a moving ship. Original source is unavailable, so use verified public interfaces or keep the terminal docked.
5. Port and validate the delivery protocol/domain in Java and C#, then run real game save/reload and failure tests.

No new artwork is required for this diagnostic slice. The full delivery terminal is not implemented.
