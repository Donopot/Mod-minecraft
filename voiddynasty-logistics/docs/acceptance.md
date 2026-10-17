# Validation report — delivery 0.4.0 and fixes 0.4.1, 15 October 2026

## Status

| Milestone | Result |
|---|---|
| P0 inventory | Both installations and dependencies confirmed; user supplied original JAR and confirmed sources are unavailable. Standalone companion replaces source integration. Test worlds/saves and ship baseline remain to be verified. |
| P1 RT diagnostic/persistence | Real link confirmed. RT 0.3.1 completed all six persistence steps and logged PASS at 12:48:56. User screenshot and final archive independently confirm proof=true and reserve=0. Earlier 0.3.0 load error was corrected. |
| P2 contract and simulation | Schema, fixed mission, local TCP client/server, durable reference state machine, fault injection and automated tests implemented. Production Java/C# compatibility and all game-thread boundaries remain pending. |
| P3 read-only display | First real two-game connection confirmed by user screenshot and both game logs: Minecraft displays Connected, native campaign ID, Passerelle du vaisseau and mode Default. Menu/disconnect/reload behavior still needs real-game checks. |
| P4–P7 delivery integration | Implemented in 0.4.0: one explicit pair, fixed depot, escrow, save-backed RT prepare/commit and reconciliation. Both adapters compile; live ledger/transport/world-journal tests pass. First real receipt/settlement is now confirmed below; clean restart, surplus count, UI fixes, ship invariants and crash behavior remain to test. |

## Commands actually run

```powershell
python -m unittest discover -s tests -v
python -m void_bridge demo
python tools/generate-protocol.py
python tools/build-fabric-diagnostic.py
python tools/verify.py
.\tools\build-rt-diagnostic.ps1 -GamePath 'C:\Users\Anwender\Desktop\JEU\rogue trader\Warhammer.40000.Rogue.Trader.Voidfarer.Edition-InsaneRamZes'
```

The C# build initially required an additional reference to the installed `netstandard.dll`; the build script includes that correction. Output is the diagnostic DLL and manifest under `dist/VoidDynastyBridge/`.

The simulator demo passed with a deliberately lost commit response. Persisted reload result: 1 surplus ingot from 65, no reserved cargo, reserve 10, one receipt. This is a **simulated** save result, not proof about either game's saves.

Compiled Java/C# tests additionally verify the production C# serializer and server, valid authentication, rejection of wrong tokens/duplicate fields/delivery messages, actual Java client reads, six-second expiry when the game snapshot stops advancing, and clearing of the previous campaign when a menu snapshot arrives. The first test runs exposed a strong-name incompatibility in the game's Json.NET under the standalone CLR and an unhandled malformed-input exception; the diagnostic now uses the built-in runtime JSON serializer and a protected request boundary. These tests run without either game engine.

Automated tests cover normal delivery/reload, 63/64/65 cargo, repeated commits, double-click identity reuse, missing prepare, cargo reservation before commit, altered payload, client reward injection, unavailable receiver, stale session, wrong identities/checkpoint/token/mission hash, one in-flight delivery, write failures, durable cancellation, lost prepare/commit responses, old saves on either side, malformed/fractured/coalesced JSON, frame-size boundaries, real TCP exchange, reconnection and concurrent-client rejection. The latest exact count and run output are recorded in `docs/test-results.txt`.

## Required next proof

1. Validate the existing JAR's ship behavior and the new companion in a disposable Minecraft world. Original sources are unavailable and are no longer required for this separate companion.
2. Verify campaign clearing on menu, disconnection and reconnection in the real games; the first connected display is now confirmed.
3. Establish per-save custom-state serialization for the installed RT build, verify A/B save isolation and fake-receipt reload. Keep game rewards disabled until then.
4. Implement the C# receiver and Java client using shared schema/hash fixtures, with Unity-thread/integrated-server dispatch.
5. Add stationary terminal/container, then prove real cargo reservation, save/reload, rollback detection and ship/container invariants.

Installer preview verified the four exact destination files. Installation status is recorded separately in the local install manifest. The user subsequently launched the games and the persistence probe created new test copies as described below. No proprietary DLLs are in the diagnostic distributable.

The first installation attempt stopped at its process check because Minecraft was running. After the user confirmed it was closed, installation succeeded: original VOID DYNASTY and companion JARs in `%APPDATA%\.minecraft\mods`, diagnostic DLL/manifest in RT's UMM `VoidDynastyBridge` folder. All four installed hashes were verified. The local manifest is `.runtime/install-de8b4222-18c7-4413-891f-a513e23774b8/manifest.local.json`. The user subsequently launched both games for the test below.

## First real two-game test — 15 October 2026

User-provided screenshot shows Minecraft 26.3 single-player in front of a loaded Rogue Trader campaign and UMM 0.25.0. The command output appears twice and displays:

- `VOID DYNASTY | Connected (read-only diagnostic)`
- A non-empty native campaign ID, omitted here because it is not needed in a shared report.
- `Zone: Passerelle du vaisseau | Mode: Default`
- `Read-only diagnostic. Delivery rewards are disabled.`

Local logs independently confirm:

- RT `GameLogFull.txt` lines 144–147: version 0.2.0 loading, diagnostic loaded, mod active; timestamp 12:15:36–12:15:37.
- Minecraft `logs/latest.log` line 56: `voiddynasty_bridge 0.2.0` loaded.
- Minecraft lines 174 and 194: connected diagnostic command output at 12:19:15 and 12:20:13.

This validates loading both real adapters, local authentication/communication, native game-state reading and Minecraft command display for this campaign and zone. It does **not** validate custom save persistence, deliveries, consumption/rewards, ship movement, campaign changes or recovery behavior. No new automated test run was needed for this documentation-only evidence update; the 45-test report remains the last automated result.

## Persistence probe correction — RT 0.3.1

The first real 0.3.0 attempt wrote a baseline and a zero-reserve receipt copy. Read-only archive inspection confirmed the intended key difference and matching native campaign IDs. At reload, direct invocation of `SaveManager.LoadRoutine` bypassed the game's area-loading orchestration and failed. This is partial write evidence, not a successful persistence proof.

Version 0.3.1 uses `Game.SaveGame` / `Game.LoadGame`, waits for each completion callback and engine readiness, then checks archive content and restored objects. A compiled C# harness verifies that callback alone or readiness alone cannot finish an operation and that low-level methods are never called. The full automatic suite now passes **49 tests** plus the lost-response simulator demo; see `test-results.txt`. The corrected live reload cycle and delivery integration are still pending.

The complete save directory was backed up before the first probe. RT 0.3.1 was installed with verified hashes and retained previous mod files. Minecraft remains on the compatible 0.2.0 diagnostic client.

## Real persistence PASS and delivery build 0.4.0

The user supplied a PASS screenshot. RT logs show the complete sequence from 12:48:20 through 12:48:56, including baseline isolation and two receipt reloads. Independent archive inspection of `Manual_163_VOID_DYNASTY___PERSISTANCE_VALIDEE.zks` confirms `persistence_verified=true`, schema 1 and logistics reserve 0. Private evidence is retained under `.runtime/evidence/persistence-pass/`.

The 0.4.0 implementation follows this proof. It uses a separate live flat protocol v2, documented in `live-protocol.md`; the simulator remains v1. The 51-test suite includes actual compiled C# ledger deduplication/rollback checks, real C# transport withholding its response until main-thread completion, Java reply parsing, world journal round trips and interrupted-intent persistence. Both adapters also use the same delivery fingerprint fixture as Python. Those tests do not run Unity or the Minecraft world engine. The acceptance procedure for the new build is in `delivery-test.md`.


## First real delivery and interaction fixes

User reports RT received the delivery. Logs show PAIRED at 13:30:23, PREPARED at 13:30:56 and REWARD_COMMITTED reserve=10 at 13:30:58. Read-only inspection confirms the new RT save contains the committed receipt and the Minecraft world testatst journal is SETTLED with matching world/delivery IDs. Evidence copies are retained privately under `.runtime/evidence/first-delivery/`. The surplus ingot count and clean reload of both games are not inferred from that journal.

Version 0.4.0 used every in-flight poll to disable send, resetting confirmation and rejecting clicks arriving during that poll. Version 0.4.1 separates polling from mutation busy state and defers at most one click until the poll completes. Regression checks exercise click retention, duplicate suppression and readiness.

The original RT handler rejected even inspect/status outside Default mode. Version 0.4.1 permits read-only receipt access in stable loaded menus/combat, while writes still require Default, no combat/loading/probe and a healthy save state. It reports PAUSED_MODE/PAUSED_COMBAT, retains the completed delivery message, and distinguishes authentication from permission to send. Main-menu/unloading reads and unknown-mode mutations remain blocked. Compiled regression checks pass; corrected UI behavior awaits game confirmation.

Both 0.4.1 adapters were installed after the user closed both games, with verified hashes and retained previous mod files. A complete world backup and matching committed RT save were validated as a closed-game paired checkpoint, including matching identities/payload hash and ZIP integrity. Private manifests: `.runtime/install-07ee8e75-3e8a-4db8-8184-918acd06a424/manifest.local.json` and `.runtime/checkpoints/first-delivery-20261015-134815/manifest.local.json`. No world, receipt or reserve was reset.


## Repeatable delivery implementation 0.5.0

User explicitly chose repeated 64-iron deliveries for +10 each. Version 0.5.0 introduces a bounded append-only receipt history, a completed count and previous receipt identity in protocol v3. Mission v2's canonical fingerprint includes the next sequence and preceding receipt. One transaction remains in flight at a time. Replayed commits cannot append another receipt; old worlds/saves or inconsistent prefixes block.

Stable 0.4.x zero-receipt or completed-receipt saves migrate without resetting identities or rewards. The real saved +10 archive is checked read-only by a compiled migration regression. Pending old transactions must be completed using 0.4.1 before migrating. The registry retains up to 256 receipts and blocks explicitly at capacity; it never evicts deduplication evidence. Tests cover legacy +10 migration, repeated totals through 256, duplicate prepares/commits, lost commit response reconciliation, old branches, tampered hashes and capacity. Live repeated delivery is still awaiting game proof.


## Native cargo missions 0.6.0

User requested one mission per RT cargo resource and explicitly chose native cargo rewards. Inspected the installed cargo enums, quest reward path, creation/serialization APIs and blueprint metadata. The catalogue contains 14 own recipes bound to verified local native blueprint references; no extracted assets or game code is distributed. Both builds compile and 54 tests pass. The tests cover all mission hashes across Python/Java/C#, historical reward migration, native receipt replay protection, durable intent state, archive identity/blueprint checks, and parsing the actual saved player cargo format.

The native factory runs only after a saved REWARD_PENDING marker. It checks full volume and origin, records the native entity ID and verifies that entity in the final save archive before acknowledgment. Reloading a pending intent fails closed. Existing logistics receipts remain historical and their points are retained; new receipts award native cargo. Actual first creation, menu selection and native cargo reload remain pending live acceptance.


## Windows assistant and guided onboarding 0.6.1

Built a standalone Windows Forms installer embedding both own adapters, with path/prerequisite checks, previews, optional selected-party backups and transactional mod-file maintenance. Full automated suite: 55 tests PASS. The production installer engine is exercised in isolated synthetic installations with normal/update/remove/restore operations, failed writes, process races, foreign/altered targets, backup corruption and partial installation recovery. Final installer-specific follow-up passed after error-message/layout adjustments.

Both game adapters compile. The Minecraft assistant and RT preparation panel reuse existing guarded actions without changing protocol v4 or native rewards. Five Windows wizard pages were rendered and inspected. Actual-installation read-only preflight passed with explicit RT paths; package contents and hashes were checked. Existing installed modules and saves were not changed during this work. First interactive use of the executable and in-game rendering of the onboarding screens still need user acceptance. See windows-setup.md for the player procedure and limitations.

## Pad, chest, weapon and model refit 0.7.0

Both adapters, the installer and player package are built as 0.7.0. All 57 automated tests passed; the two refit tests also passed after final helper extraction. The real Fabric loader accepts transformations of all seven original target classes. Real Minecraft inventory fixtures validate stable slot indices, page offsets, quantity conservation and menu capacity words. The model graph and texture references are checked against installed resources, and the game's pack metadata codec accepts the generated pack. These checks run without opening a game window or loading a world.

After the user confirmed both games closed, installed through the production installer engine with verified module preimages, selected save copies and a completed rollback journal. An initial attempt stopped before module replacement when Minecraft relaunched during the save copies; installation succeeded after the second closure. Final UM backups cover the recently used world and paired world, and the installer additionally preserves the paired world and selected RT archive. All four installed hashes match; the original mod remains byte-identical. All 132 checked save files are unchanged before/after installation. Local journal and exact backup paths are recorded in MODLOG.md and excluded from distributed private data.

User acceptance remains for the pad at different window sizes, chest clicks/transfers across pages and after reload, the visible laser and actual damage/obstacle behavior, bolt/plasma effects, and the replacement block models. Native cargo creation/reload acceptance from 0.6.0 also remains outstanding. See [refit-0.7.md](refit-0.7.md) for the repeatable in-game procedure. Existing pair and receipts must be retained.
