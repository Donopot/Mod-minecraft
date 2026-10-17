# VOID DYNASTY prototype work log

## 2026-10-15 — first implementation

- Read the supplied plan as a specification; verified version and API claims independently where possible.
- Created `codex/void-dynasty-prototype` in the initially empty repository.
- Located the installed Rogue Trader game through its existing player log; read assembly metadata without executing game code.
- Confirmed downloaded VOID DYNASTY JAR metadata; source project requested from the user.
- Compiled a read-only RT diagnostic using local references and Windows C# compiler. No game installation or launch performed.
- Implemented a standard-library-only development simulator, strict versioned messages, fixed 64/10 mission, persisted reservation/receipt state, deduplication, rollback checks and failure injection.
- Added actual TCP and domain tests, generated schema and example fixtures, installation instructions, environment inventory and explicit validation boundaries.
- Discovered a newly present `universal-modder-main/` toolkit while working; inspected its identity and left it unchanged. It is not the missing Fabric mod project.

## 2026-10-15 — standalone read-only companion 0.2.0

- User supplied Desktop VOID DYNASTY JAR and RT path, and confirmed no original mod source project is available.
- Verified the JAR matches the previous copy by SHA-256. Located the actual Minecraft instance and confirmed loader/API/GeckoLib versions from its log.
- Found a Java 25 JDK archive in Downloads, extracted it into ignored workspace tooling, and compiled a separate Fabric client companion against installed library signatures. Windows sandbox file access required compiler approval; no new toolchain download was needed.
- Added an authenticated, bounded read-only C# diagnostic stream. Unity reads remain on the game callback; Java worker reads immutable snapshots. Added `/voidbridge status`, reconnection, timeout expiry and explicit campaign clearing.
- Added compiled Java/C# transport tests, corrected issues exposed by malformed-input and runtime dependency tests, and removed the diagnostic's Json.NET dependency.
- Added reversible installer with copy verification, duplicate-mod checks, backup manifest and preview mode. No game or save is launched/modified by the installer.
- Initial installation stopped before copying because a Java game process was running; asked the user to close Minecraft before retrying.
- After the user confirmed Minecraft was closed, installed all four planned files and verified hashes. Manifest: `.runtime/install-de8b4222-18c7-4413-891f-a513e23774b8/manifest.local.json`. Original JARs and all saves remain unchanged. In-game validation is the next step.

## 2026-10-15 — Universal Modder review

- At the user's suggestion, applied the local mod-any-game and reverse-engineering guides, searched the knowledge index and bridge-contract notes, and ran the toolkit's scanner and package checker.
- Scanner confirmed Unity Mono 6000.0.64f1 but missed RT's bundled UMM; keep the existing verified loader route.
- Package checker reported 46 files, zero failures, four local-path documentation warnings. No dedicated RT per-save recipe was found. Offline dependency setup could not resolve uncached packages; no dependency download was performed.
- Findings and next-use workflow recorded in `docs/universal-modder.md`. No game input, launch, save mutation or publication performed.

## 2026-10-15 — first live game-link proof

- User tested both installed mods and supplied a screenshot showing Connected, a native RT campaign ID, zone Passerelle du vaisseau and mode Default in Minecraft 26.3 single-player.
- Confirmed independently in local logs: RT UMM activated 0.2.0 at 12:15:37; Minecraft loaded `voiddynasty_bridge 0.2.0` and logged connected command output at 12:19:15 and 12:20:13.
- Marked adapter loading and the initial read-only cross-game display as confirmed. Receipt persistence, real deliveries, ship invariants and game-level recovery tests remain unverified. No production code or game settings were changed for this evidence update.

## 2026-10-15 — requested delivery activation: persistence gate

- User requested delivery activation. Inspected current game metadata and relevant IL without executing game assemblies; derived output kept in private temporary storage.
- Verified `InGameSettings.List` is serialized per save in `settings.json`. Chosen extension is one namespaced string entry, leaving native settings/economy untouched.
- Inspected native `CreateNewSave`, `SaveRoutine`, `LoadRoutine` and operation states. `SaveInfo.IsActuallySaved` only checks whether a path is assigned, so it is not treated as proof of durable success. New-save archive content is read back and compared instead.
- Added 0.3.0 user-triggered persistence probe: new baseline copy, receipt copy, isolation reload, receipt reload, verified copy and final reload. Reserve remains zero. Delivery remains gated on real proof, not a label change.
- Added per-save codec/archive tests. User confirmed RT closed for the update; Minecraft can stay open because its read-only protocol is unchanged.

## 2026-10-15 — live persistence write proof and load correction 0.3.1

- Backed up all 165 RT save files (2679.6 MiB) with the toolkit before the live probe; private archive: `.runtime/um-state/backups/void-dynasty-pre-persistence/20261015-123346.zip`.
- First 0.3.0 live attempt created baseline and receipt copies. Archive inspection confirmed the namespaced key absent/present as intended, reserve zero and matching native campaign IDs. Reload failed because direct invocation of the internal deserializer bypassed normal area loading. No persistence PASS or delivery activation is claimed.
- Replaced low-level routine calls with `Game.SaveGame` and `Game.LoadGame`; require callback completion and idle engine/loading state, then validate actual saved/restored content. Added a compiled lifecycle harness covering both independent completion conditions.
- All 49 automated tests and the lost-response simulator demo pass. Rebuilt RT 0.3.1 and installed only its DLL/manifest after the user confirmed the game closed. Verified hashes; retained previous files in `.runtime/install-6c01d639-9877-4dfe-82b1-6308206e4442/manifest.local.json`.
- Asked the user to rerun the corrected probe from a normal pre-test save. Real reload/isolation proof is pending; logistics remains zero and the Minecraft client stays at 0.2.0.

## 2026-10-15 — screenshot triage after 0.3.1 install

- User supplied a screenshot of a fatal `RealTimeController.Tick` null reference. That stack is recorded in the previous 0.3.0 session at 12:37:03. The current 0.3.1 session started at 12:43:58 and contains no matching fatal stack at inspection time.
- Current probe attempts at 12:44:42 and 12:44:47 were rejected before saving/loading: the loaded campaign already contains the prior test receipt. No new test archives were created. Preserved both session logs privately under `.runtime/evidence/persistence-031/` before another launch can rotate them.
- The corrected reload path has therefore not yet been exercised. Next action is to load a pre-probe save without the namespaced key and rerun once.

## 2026-10-15 — real persistence PASS, delivery integration 0.4.0

- User screenshot and logs confirm all six probe stages, ending PASS at 12:48:56. Read final archive independently: schema 1, persistence_verified=true, reserve 0. Preserved evidence privately in `.runtime/evidence/persistence-pass/`. This completes the pre-reward persistence gate; the old failure entries above remain historical.
- Implemented a separate flat live protocol v2, explicit GUI-approved world/campaign pairing and a single durable RT receipt/preparation/reward in the existing per-save extension. Native save callback and archive readback precede acknowledgment; no native economy or quest fields are changed.
- Implemented the Fabric integrated-server depot, iron-only inventory, disabled automation/break/explosion/piston movement, anchored UUID/position validation, chunk escrow and world journal. Intent markers guard reservation/settlement gaps; ambiguity blocks without refunds. Client terminal uses immutable server views and server-dispatched actions.
- Both builds compile against the installed APIs. All 51 tests pass, including the real C# ledger, main-thread acknowledgment boundary, strict Java/C# transport, interrupted world-journal markers and a shared canonical fingerprint checked against Python. Full engine delivery/rendering/recovery remains to test.
- User confirmed both games closed. Installed four files with verified hashes; previous files retained under `.runtime/install-65d39c94-f525-48f0-b5ac-1588b8bf2468/manifest.local.json`. The upgraded installed Fabric JAR retains its prior filename but declares 0.4.0 internally. Existing original mod contents are unchanged.
- Complete Minecraft saves backup finished: `.runtime/um-state/backups/void-dynasty-pre-delivery-minecraft/20261015-130617.zip`, 28679 files, 6439.2 MiB uncompressed. RT pre-probe backup is retained. User can now launch the delivery acceptance test on a fresh world and the PERSISTANCE VALIDEE copy.


## 2026-10-15 — first real delivery, button and menu fixes 0.4.1

- User reports a received RT delivery, unstable send button and Minecraft UNAVAILABLE after opening a RT menu. Independently verified RT committed archive reserve=10 and Minecraft world testatst journal SETTLED, with the same identities. Preserved both under private first-delivery evidence.
- Root cause of unstable confirmation: every periodic inspect disabled canShip and reset the first confirmation click; action dispatch also rejected clicks during a poll. Separated mutation/read busy state and retained one deferred click, validated again after poll completion.
- Allowed only read-only receipt queries during stable loaded RT menus/combat. Writes remain restricted to Default/no-combat/no-loading/no-probe. Paused mode is explained separately from transport connectivity; settlement remains visible. Dynamic engine readiness avoids a stale one-second availability cache immediately after a native save.
- Moved the legacy persistence test behind an advanced toggle and derived verified labels from the loaded save, preventing old probe messages from suggesting a new test is needed after delivery.
- Built both 0.4.1 artifacts and passed all 51 tests with additional regression assertions for polling clicks, duplicate suppression, menus, loading, unknown modes, probe activity and readiness restoration. No ledger identities, receipts, reserve or cargo were reset. Awaiting Minecraft closure for installation; RT was observed closed.

- User confirmed Minecraft closed; verified both target game processes absent. Created a complete 65-file, 57.1 MiB world backup: `.runtime/um-state/backups/void-dynasty-first-delivery-world/20261015-134815.zip`.
- Validated a paired completed-delivery checkpoint: world journal SETTLED, RT archive REWARD_COMMITTED reserve=10, matching world/campaign/pair/checkpoint/delivery/payload hash. Copied the matching RT save, checked all world ZIP entries and recorded SHA-256 in `.runtime/checkpoints/first-delivery-20261015-134815/manifest.local.json`.
- Installed both 0.4.1 adapters with verified hashes and previous mod-file backups: `.runtime/install-07ee8e75-3e8a-4db8-8184-918acd06a424/manifest.local.json`. User should reload world testatst and RT REWARD_COMMITTED 133057; existing pairing/receipt is preserved. No game launch performed.

## 2026-10-15 — user-selected repeatable deliveries 0.5.0

- User requested the next stage after seeing the disabled button on SETTLED and explicitly selected repeated 64-iron deliveries for +10 each. This supersedes the original single-use mission scope. No reset of the existing +10 receipt is authorized or performed.
- Added live protocol v3, repeatable mission v2, append-only RT receipts, committed prefix count and last receipt IDs. Fingerprints now bind the next sequence and preceding receipt. A new UUID and new persisted reservation are required for every fresh reward; repeats of an old commit remain idempotent.
- Added stable legacy migration that retains the old receipt/hash, campaign/world/pair/checkpoint and reserve. Old in-flight transactions are rejected for migration and must be finished in 0.4.1. Full ledgers block at 256 receipts without eviction.
- Updated the physical depot to reserve again from a settled state. The world journal retains its committed prefix while a new delivery proceeds; intent markers still fail closed. UI now shows count and readiness for the next actual delivery, replacing the misleading generic availability text.
- Both 0.5.0 modules compile. All 52 tests pass, including real saved +10 migration without identity/reward changes, repeated totals through 256, commit replays, lost-response reconciliation, mismatched/old prefixes and capacity.
- Verified games closed and backed up the 65-file world before migration: `.runtime/um-state/backups/void-dynasty-pre-repeatable-world/20261015-140229.zip` (57.2 MiB uncompressed). The prior paired RT/world checkpoint remains retained. Real repeated delivery remains to test using the same world and committed RT save.

- Installed both 0.5.0 modules with verified hashes and retained prior modules: `.runtime/install-85ec3c5c-9fd6-4b9a-a25c-c283f169b35b/manifest.local.json`. Games were not launched.


## 2026-10-15 — user-requested missions by resource with native rewards 0.6.0

- User asked for one mission per Rogue Trader cargo resource and explicitly chose native cargo rewards. This authorizes the new native reward path; previous logistics receipts and points are retained.
- Applied existing modding/reverse-engineering workflow. Inspected installed cargo origin enum, full-cargo blueprint references, ResourcesLibrary lookup, CargoState.Create, quest reward use, full-volume checks and player cargo serialization. Derived IL/metadata remains under the private OS temporary research folder.
- Added 14 fixed repeatable missions with resource selection and per-recipe counts. Generated C#/Java catalogues share one reviewed JSON contract. Updated the depot to accept catalogue materials and reserve only the frozen selected recipe. Confirmation is bound to the selected mission; changing a selection cannot silently change a confirmed shipment.
- Added protocol v4, mission/blueprint-bound fingerprints, native reward receipts and a durable REWARD_PENDING step before native creation. The native full/ID checks and final native archive verification precede acknowledgment. Interrupted intents block rather than recreating cargo. Legacy logistics receipts keep their old points/hashes.
- Both 0.6.0 modules compile. All 54 tests pass, including cross-language hashes for all recipes, legacy real-save migration, NativeCargoArchive checks on synthetic and real saves, pending intent recovery and repeated commits. First real native award remains to validate.
- Attempted pre-install backup and installation after previously observing games closed; a game had been relaunched. Both process guards stopped before copying or modifying saves. Asked user to close both games. No 0.6.0 installation is claimed yet.

- User confirmed both games closed; verified target processes absent. Backed up world testatst (65 files, 58.4 MB uncompressed): `.runtime/um-state/backups/void-dynasty-pre-native-cargo-world/20261015-143619.zip`; all ZIP entries passed CRC verification.
- Installed both 0.6.0 adapters and verified hashes and internal versions. Original voiddynasty 1.0.0 retained unchanged. Previous module files and manifest: `.runtime/install-0123db5a-2fa7-4098-a196-c30940699b3e/manifest.local.json`. Games were not launched; first native cargo creation and in-game selector validation remain pending.

## 2026-10-15 — Windows wizard and in-game onboarding 0.6.1

- User requested creation of the assistant and installer. Continued the existing modding workflow. Implemented a standalone Windows Forms executable with embedded own adapters, five pages, selected-folder validation, exact tested dependency/original/RT compatibility checks, destination preview, optional selected world/RT save copies, update/repair, previous-mod restoration and removal. No loader, original JAR or proprietary library is embedded or downloaded.
- Installer snapshots and hashes preimages, writes a durable prepared journal, performs per-file atomic replacements and verifies hashes. Copy failures roll back; interrupted journals block new installation until restored. Modified targets, corrupted backups, forged paths, links/junctions and duplicate mod IDs are rejected. Removal keeps the original mod and all game saves. Selected world ZIP entries are compared against their source SHA-256 values and archive hashes are retained.
- Added Minecraft /voidbridge assistant, default onboarding for unpaired worlds and a terminal help button. Guide actions use the existing server validation. RT adds a first-use panel over the existing copy-based persistence preparation; existing bridge state is preserved and never prepared again. Protocol v4 and cargo transactions are unchanged.
- Both 0.6.1 adapters compile. Full suite: 55 tests PASS plus lost-response demo. Installer harness covers isolated synthetic installations, upgrade/rollback, uninstall/undo, save preservation, injected copy failure, game-start races, incompatible versions, duplicates, altered files/backups, forged journals and partial-install recovery. Targeted installer test rerun passed after final actionable-message and layout adjustments.
- Built dist/VOID-DYNASTY-Assistant-0.6.1.exe and separate dist/VOID-DYNASTY-Joueur-0.6.1.zip. Verified package entries and executable identity; package contains only the executable, LIRE-MOI.txt and SHA256.txt. Developer package excludes user/toolkit/game files. Executable is unsigned.
- Rendered and inspected the Windows wizard pages; corrected footer navigation, field margins and stale review invalidation. Read-only preflight against the actual user installations passed after providing the RT installation/data folders. Earlier preflight correctly refused an active Minecraft process. No game processes were ended or launched.
- This turn prepares the release for installation through its wizard. Existing game modules and saves were not modified by this work. Interactive user acceptance and actual Minecraft/RT guide rendering remain pending, as does complete native-cargo acceptance.

## 2026-10-15 — original mod refit and installed update 0.7.0

- User reported clipped pad content, unreadable custom chest contents, unsatisfactory block models, a laser without beam/damage, oversized bolt impacts and faint plasma. Continued the existing modding and reverse-engineering workflow using the installed original MIT-labelled JAR and actual Minecraft 26.3 APIs. Derived inspection output remains outside the repository. The original 1.0.0 JAR is retained byte-for-byte.
- Added a responsive three-tab fleet pad with pagination, original server fleet requests/commands and confirmation for stow, move and delete. Fixed repeated empty-fleet requests and selection/confirmation invalidation. The original fleet registry, templates and ship physics are retained.
- Fixed the original client/server chest window mismatch with a 54-slot server projection, stable packet indices and capacity/row synchronization in four 16-bit menu words. Added a new chest screen and paging controls, acknowledged-page input protection and ordinary helper classes outside the Mixin package. Existing block entity inventory/NBT formats are unchanged.
- The original laser renderer had no geometry. Added a full-bright beam renderer and current-direction block/entity selection calling the original damage calculator; retained ammo, cadence and reload. Limited bolt impact particle count/dispersion without changing blast damage/radius and added a bright cyan plasma trail/core.
- Generated own geometry for 22 original blocks plus the bridge depot, referencing existing textures without copied bitmap assets. Built-in pack metadata passes the real 26.3 codec; models and variants are checked against installed resources. Pack is registered always-enabled, with user texture-pack priority still applicable.
- Both adapters and the standalone installer/player ZIP compile as 0.7.0. Full automated suite: 57 PASS, plus lost-response demo. After final helper extraction, both refit tests passed again. The actual Fabric Knot harness successfully transforms all seven original target classes without launching a game or loading a world. Real Minecraft inventory fixtures check quantities, slot/page mapping, wide capacity words, small inventories and ray/obstacle selection. Visual rendering, live transfers and damage with other installed mods remain pending user acceptance; see docs/refit-0.7.md.
- First installation attempt stopped after copying selected saves because Minecraft was relaunched during the operation. No module was replaced. After a second explicit closed-game confirmation, refreshed UM backups: `.runtime/um-state/backups/void-dynasty-pre-refit-recent/20261015-163132.zip` (New World (2), 66 files, 72.0 MB) and `.runtime/um-state/backups/void-dynasty-pre-refit-paired/20261015-163134.zip` (testatst, 65 files, 59.1 MB). Both pass CRC checks.
- Installed using the production installer engine and its embedded 0.7.0 payload, called by a temporary console host. All four destination hashes and both internal module versions are verified. The original JAR hash is unchanged. Comparing all files in both worlds and the selected paired RT Auto_4.zks before/after proves all 132 save files unchanged. The installer additionally copied and hashed the paired world and RT archive and retained all previous module files.
- Completed installation/rollback journal: `%LOCALAPPDATA%/VoidDynastyBridge/Installer/backups/20261015-143137-5221035-fb35bcedd7d7486c9acb4c503f6a1f17/installation.json`. Private audit: `.runtime/installer-apply/installation-proof.json`. Installer preferences were saved for the same Windows account. The installed companion retains its old filename but declares version 0.7.0. No game process was ended or launched by us, and no pairing, receipt, reserve or native cargo was reset.

## 2026-10-15 — installed six-mod compatibility audit

- User requested checking compatibility/conflicts in the actual mods folder. Read-only inspection found Fabric API 0.162.0+26.3, GeckoLib 5.5.7, Player Animation Library 1.2.8+mc.26.3, OP World Like 1.0.0, original VOID DYNASTY 1.0.0 and companion 0.7.0. Verified CRC of all 54 archives including nested dependencies, no duplicate IDs/classes/root assets/data resources, and all 136 declared dependency/conflict predicates with Fabric's real VersionPredicate.
- Isolated real Fabric transformation harness with all six JARs passed 16 sensitive target classes. Initial harness referenced a pre-26.3 first-person renderer name; checked the installed game archive and corrected it to FirstPersonHandsAndItemsRenderer, then all targets passed. No mod entrypoints, game window or world were launched by the harness. The user's real 16:33–16:35 session also starts a world and stops normally with no Mixin failure/FATAL.
- Confirmed combat interaction in installed bytecode: original DamageCalculator.applyDirect uses playerAttack, including the new laser path; OPWL LivingHurtMixin applies MinePiece physical resistances to it. Logia can reach zero and Gomu 0.0001 under their respective no-Haki/non-suppressed conditions. No live comparative damage measurement was performed. Report recommends defining energy weapon compatibility rather than globally bypassing fruit defenses.
- Found OPWL resource errors: Marco sword_to_left nan expressions, Murakumogiri UV bounds exceeding a 64x64 texture, and missing guard.ogg. Its combat recursion guard logs 20 blocked environmental bat hits; no evidence yet ties those suppressions to a VOID DYNASTY shot. The guard's ALLOW_DAMAGE/AFTER_DAMAGE balance needs targeted verification. Shared default R is already avoided by user's MinePiece skill/transform remaps to up/down arrows.
- Saved French report in docs/compatibilite-mods-2026-10-15.md and private inventory/runtime/log evidence under .runtime/compatibility. Derived bytecode inspection stays in OS temp. No installed JAR, option or save was changed; this request was an audit, and none of the identified behavior/resource changes has been installed.

## 2026-10-15 — fruit foundation review and work estimate

- User requested repairing widespread fruit combat visual/effect problems, then clarified that almost everything is affected and the foundation should be reviewed before estimating the full work. User does not know the source project location. Latest examined client packages contain JARs/guide only. Applied the existing modding/reverse-engineering workflow and read the local port-a-mod guidance for preserving source behavior; private work only, no publication.
- Pinned actual OPWL JAR SHA256 533f67283d551b37af2a074dc180326a191c31a8e9e5aaba0c1de31bd167c256. The old downloaded implementation CSV refers to a different hash, so its completed-test claims are not adopted. Bytecode inspection remains in OS temp, outside the repository.
- Built and ran an own isolated Fabric catalogue probe with the six mods, vanilla registry bootstrap and reflective readout, without mod entrypoints, a game window or world. Measured 40 fruits/213 techniques: MELEE 120, PROJECTILE 65, BUFF 27, EFFECT 1. There are 178 bare IDs, with 13 shared names affecting 48 entries; the mod's 35 warning counts excess occurrences. Exported a private 213-row validation matrix, all effect/render entries A_TESTER. Runtime data precedes startup balance application and is not combat proof.
- All 82 declared projectile/melee visual pairs resolve models/textures; they use 25 distinct geometries with animation files. Two ghost models lack the generic idle key and need state-specific checks. Confirmed global static player storage path config/opworldlike/devilfruit-players.dat, no map clear in loadAll, and direct writes. Confirmed generic melee/projectile impact paths ignore hurtServer's result before secondary effects, unlike strikeTargets. Animation/transform messages are sent to the owner; animation reception already supports entityId. Bool fallback/energy exception handling and temporary lifecycle need structured tests.
- Wrote docs/fruits-socle-et-chantier.md with retained mechanisms, confirmed issues versus risks, target pipeline, save migration boundaries, acceptance gates, and a provisional 30–55 developer/test-day estimate for a coherent 40-fruit alpha, to revise after three representative fruits. Source recovery and fidelity to all original effects remain uncertainties. This phase is review and estimation as steered by the user; no repair build, installation or modification to mods, options or saves is claimed.

## 2026-10-15 — port status PDF compared with installed code

- User supplied OP-World-Like-etat-du-portage.pdf without a new explicit action. Read all 15 pages with the PDF skill, rendered all pages to a contact sheet and inspected the relevant animation/testing/open-issues/source-location pages individually. Document instructions are context, not user commands.
- PDF declares 312/312 server checks (310 distinct), no fruit visually accepted (0/40), FakePlayer limits, incomplete transformation hiding, no complete quadruped source form and an open invisible-technique report. It lists relative project/docs/test paths but no actual repository/project location. The reported test recipe was not rerun or counted as locally proven.
- Extended isolated catalogue probe to call installed animation counters: nombreDePaires=151, pairesConnues=155, couvertureExacte=145. The last implementation includes NOMS in its count, so the prose's 145-plus-four interpretation needs reconciliation. No world/mod entrypoints used. PDF's claim of observer animation broadcast differs from installed sendAnimation's owner-only send.
- Updated docs/fruits-socle-et-chantier.md to reuse the documented work, distinguish persistence from world isolation, begin visual diagnosis on Leopard/Goro/Pika, and keep Gomu/Mera/Hie for mechanics. The 30–55-day full-scope range remains provisional, not an asserted cost for a targeted fix or a reason for wholesale rewrite. No installed files changed.

## 2026-10-15 — One Piece source recovered, foundation repaired and tested privately

- User resumed One Piece with the supplied source archive and requested all 40 fruits/213 techniques and transformations. User selected anime/manga references for missing forms and explicitly accepted the Minecraft EULA for local tests. The source, derived resources and outputs remain private/ignored; no publication or live installation performed.
- Final private alpha `1.0.1-alpha.1`, SHA256 `35e3c14f1c9d815fff2d8d812b13275c5f8bdb7a2a109a75c928b8fcb41faf66`, compiles 252 sources. Offline build uses real Fabric ClassTweakers for compile-only headers, excluded from runtime/artifact. Runtime resolves 81 actual profile libraries and available hashes. Source and private resource locations are in `.runtime/onepiece/paths.local.json`.
- Same JAR passed 59 targeted server checks and 307 distinct legacy checks, including 213 prepared launches, expiry, restart and loot. Evidence `.runtime/onepiece-lab/run-0fe26ac800f7469d97747a4d0ee0b0d1/report.local.json`. A launch is not proof of complete technique semantics. Separate helper test passed 128 assertions; final CLIENT bytecode probe passed 17 targets with the installed VOID pack present, per-player state isolation and 19 effect profiles. Packed resource audit: 616 JSON/82 visual pairs, zero blocking issues and four degenerate-face UV warnings.
- Fixed cast refusal/fallback/energy handling, five unreachable IDs, accepted-hit consequences, damage-call-stack lifetime, elemental attribution and per-technique overrides, recoil, spaced Zoan combos, Riki single-hit grab, power cleanup, maintained-form restoration/cost/expiry, per-world atomic storage, ghost wall traversal and ray bounds before collision, Goro link LOS/refused counts, temporary entity persistence and animation progression.
- Actual graphical captures exposed the major common visual bug: 42 particle providers used the block atlas. Fixed layer choice and source frame cadence in Java/template. Before/after Shigan shows atlas fragments replaced by actual claw sprites and moving hybrid pose. Also removed inherited fireball flames from lasers/ghosts and fixed resource NaN, missing guard audio and Murakumogiri UV baking error.
- Final graphical lab `.runtime/onepiece-lab/run-ad5491e5587044b7a43bcc3ae22126d6`: nine images captured, four inspected; partial Leopard/Shigan, Pika laser and Goro Sango evidence only. Manual profile prepared in `run-06468a09a122442c91386c2e995a5e8d`, via `onepiece/Tester-OnePiece.cmd`. Captures use the game's API, without desktop input; all automated owned processes stopped. Standard VOID mod hashes unchanged. Old One Piece global progress not migrated; user's world choice pending. New animal forms/Gears and full per-technique/observer/first-person acceptance remain. Full acceptance stays 0/40. Private handoff/report/matrices are in `onepiece/docs`.


## 2026-10-15 — Fresh test progression confirmed

The user chose to start from zero in a test world. Do not import the legacy global fruit progression into the test profile. Verified the prepared manual lab remains unlaunched, with no repair-world or client saves yet. No existing world was reset; no game was launched for this preference update. This resolves the earlier optional migration question.


## 2026-10-16 — Private One Piece Mera alpha.2

Seven dedicated Mera techniques, bounded fire and synchronized propulsion implemented. Final private lab:92 Mera+59 foundation+307 historical checks PASS; two graphical clients,48+24 captures and real propulsion distance19.39 blocks. Art/human play acceptance pending; no full-fruit acceptance claim. Launcher onepiece/Tester-Mera.cmd; report onepiece/docs/MERA-MERA-2026-10-16.md. No normal Minecraft mods/saves or VOID modules changed. All automated owned processes stopped.


## 2026-10-16 — Private Magu Magu alpha.3

Five Magu routes, safe attributed magma/pools and visual fixes. Final private lab66Magu+92Mera+59foundation+307historical PASS; two clients35+20captures and dash12blocks. Tester-Magu and report onepiece/docs/MAGU-MAGU-2026-10-16.md; human/art acceptance and source variants pending. No normal mods/worlds/VOID changed; automated child processes stopped.


## 2026-10-16 — Magu animation correction alpha.4

Confirmed shared/replayed gestures and duplicated Kagero/Volcanic FX; replaced with8distinct finite PAL tracks and separate phase FX.74Magu+92Mera+59foundation+307historical PASS;8real pose/time/UUID checks on each of two clients,45+30captures/12inspected. Private Tester-Magu updated; no normal game/world/VOID changes. Detailed report onepiece/docs/MAGU-ANIMATIONS-2026-10-16.md.


## 2026-10-16 — Magu animation polish alpha.5

User approved alpha.4 then requested polish. Eight tracks now bake smooth curves, secondary movement and recovery; impacts retain timing. Charge follows look. Curved low wake with stable inner radius, tapered volcanic jets with smooth expansion, continuous sky preparation and trails reduced 864→84 quads per projectile; no measured FPS claim. Gameplay unchanged. Read-only layered reviews informed timing/radius refinements.

Final JAR `b991ff05a4b3dca6fc92dfb4d0c1e4788c2e8302cff947a1c645c953546fc35a`; server `run-7ea32bb936ba46989f35d65ca80c29da` 74 Magu+92 Mera+59 foundation+307 historical PASS, loot/restart PASS. Graphics `run-e6f94cfc104643cdbab9a6a0585abc8c` 55+40 captures/15 inspected, 8 PAL gestures evaluated on each client, dash/lifetime PASS. Audits 8 tracks, 625 JSON/83 pairs/0 errors/4 old warnings; probe17 PASS. Automated children stopped. Tester-Magu targets unlaunched `run-cc3436916e724597b1221682609a1bbc`. No normal worlds/mods/VOID changed. Report onepiece/docs/MAGU-POLISH-2026-10-16.md; human polish trial pending.


## 2026-10-16 — Goro techniques alpha.6 verified

Seven dedicated routes and eleven finite neutral-ending PAL tracks, charged Vari/ElThor/Sango/Kari, safe blink, announced storm and bounded electric Raigo. Hardened storm ownership/level/lifecycle/LOS/protections and confirmed damage. Invulnerable chain candidates cannot extend path, slow retained, reflected Raigo credits Nikyu, Gomu/Goro electric counters. Old Goro source assumptions corrected; adaptations and absent Amaru model documented, resource question sent. Initial fixture failures led to explicit removal/variable clearing of dimension-inserted FakePlayer, preventing historical loot reservation and monster despawn leaks.

Final JAR `b4737e646a3cbd3d1a10501299136530ab3621b1d35b304389c803b6b4463dd5`; server `run-9a6d0912c06843aeb98f30dbd714c870` 104Goro+74Magu+92Mera+59foundation+307historical PASS, loot/restart PASS. Graphics `run-e9ee337d990c4ee9833cde8b1107acbd`49+49 captures/17 distinct inspected,11 actual PAL UUID/time/pose checks per client, real blink32,3 native storm hits each delay10 and hurt1. Assets636JSON/83pairs/0errors/4oldwarnings;11animation tracks audited,8Magu tracks unchanged;probe17PASS. Automated children stopped. Tester-Goro and alias target fresh unlaunched `run-f8cc0aaf09134e6996a00064a06fb7c5`; Mera/Magu launchers untouched. No normal mods/saves/VOID changed. Full fruit acceptance remains pending (Amaru and human art/play).


## 2026-10-16 — Five-fruit batch workflow adopted

User explicitly requested five fruits before visual tests. Replaced old one-fruit workflow in AGENTS; B01 = Hie/Pika/Moku/Suna/Yami, 26 techniques. Audit/develop and server checks first, one grouped graphical session after all five are ready, targeted retakes on failure. Prior frozen Mera/Magu/Goro profiles and evidence preserved. B01 source/layer audits begun; findings in docs/BATCH-01.md, no new implementation/test claims and no game launched for this workflow update.


## 2026-10-16 — Amaru resource search in user-provided JARs

Read-only inventory and class/model review of Downloads MinePiece ver15 (same f9ecbb... reference) and ompworld1.3.48 (e5cc7523...). MinePiece contains Enel NPC/clothes/drums but no identified giant Amaru form; Java drum roots have no humanoid geometry and source Goro has no transform route. Ompworld has only lang/log_pose assets,391region files, no character geometry/textures or nested JAR; mandatory dependency mineminenomi [0.11.5,0.12). Asked for that dependency JAR path as next candidate, not proof it contains Amaru. No games launched/installed files changed, gameplay reference unchanged, B01 workflow preserved. Report onepiece/docs/AMARU-RESOURCE-AUDIT-2026-10-16.md.

## 2026-10-16 — Volt Amaru reference located

User supplied Mine Mine no Mi1.20.1-0.11.6 (SHA256 ceab4866a28236c5f74fc0245dc8a079ce9284b93eee7865ec8414c487de30cc). Bytecode review plus independent read-only model audit confirms VOLT_AMARU uses shared DaibutsuModel through PlayerMorphRenderer, scale2.5, standing2.4x6.5, player skin and textureless ENERGY overlay. Humanoid animations inherited/adjusted; no dedicated Amaru animation file or body PNG. Resource question resolved. Forge1.20.1 reference requires adaptation to Fabric26.3; not implemented or visually tested. No game launch/install, normal saves/mods/VOID untouched; B01 five-fruit workflow and MinePiece gameplay baseline preserved. Report onepiece/docs/AMARU-RESOURCE-AUDIT-2026-10-16.md.

## 2026-10-16 — Goro priority completed: Amaru alpha.7

User requested finish Goro before B01 and retain reference JARs. Three JARs copied to .runtime/onepiece/reference-jars with verified SHA256. Added privately adapted Daibutsu/Volt Amaru mesh, electric skin+energySwirl, Enel drums, finite transform gesture, UUID-scoped dimensions2.4x6.5/eye5.5/SCALE2.5 and native camera10. Owned native flight, 20tick charge,40 activation,.4/tick maintenance+.6 flying,1200tick duration, no combat bonus and no regen during form. Ownership saved before granting flight and reconciled on login/respawn; full-volume deployment/blink checks, lifecycle cleanup and external attributes/equipment preserved.

Read-only reviews caught regen/charge-scale guards, stale saved flight, and a too-weak pose oracle; corrected with durable ownership and multi-tick UUID-scoped mesh checks. Visual witness ran ahead of actor; added actor clock without relaxing assertions. Transformable tag initially replaced baseline entries at packaging; 59-check preflight rejected it. Rebuilt tag from frozen13 baseline entries plus Goro14 and strengthened F2 to assert Goro remains Logia. Final complete rerun passed on SHA256 57264a351417d5962051d62ce5682cf2524b0dc03df12b043fca7829481987a6.

Final server run-a1d3b558c2cc4c45a875a72887225243:142Goro(104+38)+92Mera+74Magu+59foundation+307historical=674PASS; loot/restartPASS. Two-client run-c97e96d93b04464f98804120676525be:13checks each,12+12captures,17distinct actually inspected, six actual PAL IDs each, native flight/camera10/retracking/disconnect cleanupPASS. Twelve distinct finite Goro tracks audited,8Magu tracks unchanged;637JSON/0errors/4oldwarnings;clientprobe17PASS. All owned children stopped. Tester-Goro/alias target fresh unlaunched run-1f29a788ca1346d88867d16c2dfe2a9c with same hash. Mera/Magu launchers and normal mods/saves/VOID unchanged. Human artistic acceptance pending, no B01 implementation or public publication.

## 2026-10-16 — B01 five-fruit delivery alpha.8

Completed Hie5/Pika8/Moku4/Suna5/Yami4:26 owned routes,52 finite distinct authored tracks and bounded fruit-specific geometry/native meshes. Reworked target/owner/world/lifecycle guards, confirmed-hit statuses, independent own speed modifiers, inventory-safe ice terrain, smoke capture, cylindrical attributed sand storm, safe full-body travel, real finite Amano sword and independent Yami pull/absorption. Source ver15 budgets and playability adaptations documented in onepiece/docs/B01-IMPLEMENTATION-2026-10-16.md.

Grouped two-client QA exposed excessive source particle sprites and camera intersections. Small particle accents, front/lower charge, golden kick arc, local FPS mesh guards and fruit-scoped Pika passive HUD fixed. Grouped run-61db2816dadc457a98ab2c5ba70b4476:104+104captures,50PAL phases/client,33+26live checks and5native YamiPASS. Targeted Pika/Moku/Yami run-391cf870fa5448a4b1b154a243d6211a:64+64captures,30PAL/client,22+16checks,5YamiPASS. All16 contact sheets opened. Human artistic acceptance pending; prominent close launch meshes remain documented.

Final SHA256 a3d61c1dc092bc3fe99c3de32f94ac145a1cee14a592b05f5f63945ac3b507c4,275sources. Final run-c3fe24997a6f48c9bbbad409cda27a28:345B01+59foundation+307historical=711PASS, loot/restartPASS. Prior92Mera+74Magu+142GoroPASS on e6bbac; full JAR diff proves gameplay classes unchanged, but these are not claimed as rerun on final hash. Client classes/assets identical to targeted retake; final delta only diagnostic test-command class. Initial native Yami fixture reset wrong invulnerability timer; uses damageCooldownTime reset, actual hurtServer but not natural cadence. Intermittent historical A7c identified as sunlight during long suites; noon fault test traced onFire and reproduced covered16HP/open death. Runner resets midnight before deferred historical fixtures, preserving strict oracle. Resources691JSON/83pairs/0issues/4oldwarnings;52B01+12Goro tracks audited,8Magu unchanged.

Tester-Lot-01 choice1..5 and Tester-OnePiece alias target fresh unlaunched run-c7fb4cef17cc4fd6ad31e5d44b855bb2. Prior alias backed up; Goro/Mera/Magu launchers frozen,3reference JAR hashes reverified. Normal mods/worlds/VOID untouched, no publication, all owned automated test processes stopped. Report onepiece/docs/B01-VALIDATION-2026-10-16.md and build/verification-summary.local.json.

## 2026-10-16 — priorité terrain avant B02

- Demande utilisateur : météores détruisent les toits, certaines capacités Gura détruisent le terrain ; décider chaque technique ; actualiser les lots précédents puis reprendreB02.
- Audits lecture seule Mera/Magu/Goro/B01 :33destructrices,4temporaires,8sans mutation, plus Amaru sans mutation. Largeurs BlockDestroy et rayons BlockToIce/Sand distincts. Voir onepiece/docs/TERRAIN-POLICY.md.
- Budget par lancement partagé entre enfants, source exacte fruit+propriétaire+dimension, opt-out par technique, protections/inventaires/résistance/temporaire. Glace et grès restaurés200ticks, pas de sable tombant.
- B02 préliminaire ef9fd010 :328PASS/9FAIL/337,307historiques+59soclePASS ; non livré. Défauts connus : BombOverhead, Rubble/Stone sans dégâts, contrôles de placement à corriger. Aucun renduB02.
- Premier build terrain0e2e05aa :59socle+92Mera+74Magu+142GoroPASS. Testslabdd686abce7164e6887e26f4e9457d899 encore actifs ; sources ont depuis corrigé point Vari/contact Goro, basekick, gel partagé et tests de quota/config. Attendre build et preuves finaux.

- Terrain alpha.9 d20b7251 livré aux lanceurs privés :267terrain+59soclePASS,1019régressions321940df transférables aux classes production strictement identiques (2classes diagnostics seules différentes). Manifest onepiece/build/terrain-alpha9-verification.local.json. Profil8838b065 neuf et non lancé ; vieuxlanceurs sauvegardés. RepriseB02 autorisée/priorité active.

- RepriseB02 alpha10 :22politiques destructives+4NONE, terrain au vrai contact et damageOnly sans ancien breakBlocks. Run11971694 :26terrainPASS, BombOverhead rétabli,5FAIL Rubble/Stone. Cause bytecode: marge native grandissante et AABB.clip inside-start vide. Fix ciblé, pas de changement vitesse/dégâts. Test257ff1e0 en cours.
