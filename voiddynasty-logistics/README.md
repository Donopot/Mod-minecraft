# Pack Minecraft seul

**Distribution actuelle : [VOID DYNASTY alpha 8 unifié](../voiddynasty/docs/BUILD-UNIFIE-ALPHA8.md).** Le code Minecraft de cette suite est inclus dans le JAR principal ; seul l'adaptateur Rogue Trader reste séparé côté RT. L'assistant 0.7.1 décrit ci-dessous est historique et ne sert pas à installer le nouveau JAR unique.

Pour jouer sans Rogue Trader, utiliser [le dossier client-local](../client-local/voiddynasty-0.7.1) : quatre JAR, dépendances incluses, à copier dans `mods`. L’assistant Windows décrit ci-dessous sert à l’installation des deux jeux ; il n’est pas nécessaire pour ce mode local.

# Update 0.7.1 — one Minecraft bridge source

The Minecraft sources live only in [../voiddynasty-bridge](../voiddynasty-bridge). Build both adapters, verify them, then build the assistant from this directory. Keep the sibling folders together when cloning or extracting the source package. The dynamic fleet list and its network relay are retained. Delivery protocol and receipts are unchanged. This patch is built and tested locally; in-game visual acceptance remains pending.

# VOID DYNASTY — Rogue Trader × Minecraft prototype

Delivery prototype **RT 0.7.1 / Minecraft 0.7.1**, with a Windows setup wizard, 15 October 2026. Work is on `codex/void-dynasty-prototype`.

The first logistics delivery and save round trip passed in the real games. **Version 0.6.0 introduced 14 selectable, repeatable missions that award actual Rogue Trader cargo**: fuel, provisions, textiles, mechanisms, ship components, weapons, armour, sacred/xeno/Chaos goods, jewellery, torpedoes and an evacuation commission. Existing receipts and logistics points are retained. [Run the native cargo test](docs/native-cargo-missions.md); the first native award still needs game validation.

**Available:** both compiled delivery adapters, a proven read-only game link, a proven RT per-save round trip, compiled ledger/transport/journal tests, and a development simulator with fault injection.

**The first real two-game diagnostic test passed.** The user's screenshot and game logs confirm campaign, `Passerelle du vaisseau` zone and `Default` mode in Minecraft. The first real delivery is also confirmed. Clean restart and failure recovery still require acceptance evidence. Python is development tooling, not a runtime dependency for either game mod.

## Refit 0.7.1

The companion also adds a fleet pad with tabs and pagination, synchronized custom chest windows, visible laser geometry/current-direction target selection, compact bolt impacts, brighter plasma and own industrial block models. The original JAR and container save formats are retained. [Refit changes and in-game test](docs/refit-0.7.md). The 0.7.1 verification covers 65 distinct successful tests (63 in the main invocation and two historical-save tests in a targeted invocation), including actual Fabric transformations, real Minecraft inventory fixtures and model reference checks; visual and combat acceptance remains pending.

## Try the delivery prototype

The companion runs alongside your existing `voiddynasty-1.0.0.jar`; it does not rebuild or alter that JAR. Both outputs are under `dist/`:

- `voiddynasty-bridge-0.7.1.jar`
- `VoidDynastyBridge/VoidDynastyBridge.dll` and `Info.json`

Use **`dist/VOID-DYNASTY-Assistant-0.7.1.exe`** or extract **`dist/VOID-DYNASTY-Joueur-0.7.1.zip`**. The standalone Windows wizard embeds both adapters, checks the selected installations and dependencies, previews the destinations, backs up overwritten mod files and optionally copies the selected world and RT save. It supports updates, repair, rollback and removal of the bridge while keeping the original mod. No Python or separate server is needed to play. See [the player installer guide](docs/windows-setup.md).

With an existing pair, keep the same world and its latest corresponding **REWARD_COMMITTED** save. For a new pair, open **Assistant de premiere utilisation** in the RT mod panel, prepare the loaded campaign on copies, then follow **`/voidbridge assistant`** in Minecraft. The guide uses the same validated server actions; approval still happens in RT. It never resets an existing pair or receipt. `/voidbridge` opens this guide automatically until a depot and pair exist; the terminal's **?** button reopens it.

The older script remains available for development: `tools/install-diagnostics.ps1 -VoidDynastyJar 'C:\path\to\voiddynasty-1.0.0.jar'`. Use `-WhatIf` to preview. The wizard does not download or install Fabric, GeckoLib, Fabric API, the original mod or a game loader. The generated package accepts the exact base JAR used to compile it, alongside the required dependency versions and RT assembly. It rejects an old bridge build, a changed source tree or an altered artifact before packaging. Missing prerequisites are explained before any copy.

## Try the transaction simulation

Python 3.12+, standard library only:

```powershell
python -m void_bridge demo
python -m unittest discover -s tests -v
```

The demo starts with 65 iron ingots, sends 64 for 10 logistics points, drops the commit response after saving the reward, reconnects, settles the locked cargo, and checks reloaded data. One surplus ingot remains.

For a persistent two-process experiment:

```powershell
python -m void_bridge init --ingots 65
python -m void_bridge server --drop-commit-reply-once
# In another terminal in this directory:
python -m void_bridge deliver
python -m void_bridge status
```

`init` explicitly pairs the two simulation fixtures. It refuses to overwrite existing fixtures. `.runtime/` holds disposable development data and a generated secret, and is ignored by Git. `status` omits the secret. The simulator accepts one authenticated connection on `127.0.0.1:28765`. A second completed delivery is rejected.

## Build the Rogue Trader diagnostic

```powershell
.\tools\build-rt-diagnostic.ps1 -GamePath 'C:\path\to\Rogue Trader'
```

This uses the Windows .NET Framework C# compiler, the installed game's Unity references, and its bundled UMM archive. Output: `dist/VoidDynastyBridge/`. No SDK download is required. Proprietary references stay under `.runtime/` and are not included in the output.

Build the companion from the sibling `../voiddynasty-bridge/` with `python tools/build-fabric-diagnostic.py --java-home 'C:\path\to\jdk-25' --original-jar 'C:\path\to\base.jar'`. It compiles against the confirmed local Minecraft 26.3/Fabric 0.19.5/API 0.162.0 libraries. No Gradle project, reconstructed original source or bytecode patch is required for this standalone client companion.

See [installation](docs/install.md), [confirmed environment](docs/environment.md), [protocol](docs/protocol.md), [persistence boundaries](docs/persistence.md), and [validation results and next steps](docs/acceptance.md).

Source layout:

- `rt-adapter/`: diagnostic, persistence probe, delivery receiver and UMM manifest.
- `protocol/`: generated JSON Schema, fixed v1 mission and valid examples.
- `void_bridge/`: reference simulator domain, TCP transport and CLI.
- `tests/`: transaction, persistence failure, rollback and real TCP tests.
- `../voiddynasty-bridge/`: the single Minecraft terminal, container, escrow, world journal and refit source.
- `fabric-adapter/`: migration README only; no duplicate source tree.
- `tools/`: local build, assembly inspection and protocol generation.

Regenerate the schema and examples with `python tools/generate-protocol.py` after a protocol change. No code from the reference projects has been copied into this repository.

Run `python tools/verify.py` after both builds to save test and demo results, including actual compiled Java/C# transport tests. Then run `python tools/package.py` to produce a ZIP with sources, documentation, tests, companion JAR and diagnostic DLL. The package excludes local tokens, original VOID DYNASTY, game references, JDK binaries and independently supplied toolkit files.

Version 0.7.1 uses live protocol **v4** and the native cargo catalogue introduced in 0.6.0. The refit and installer do not change delivery transaction semantics. The first saved +10 receipt remains historical; new deliveries award native cargo. The simulator remains v1 and demonstrates its original single mission.

Build the Windows executable and player ZIP after both adapters with `python tools/build-installer.py`. Its source is under `installer/`; it uses Windows .NET Framework and embeds only our own artifacts. The distribution contains no original JAR, game assemblies or extracted assets. The installer engine is tested against isolated synthetic installations, including injected failures and interrupted-operation recovery. First interactive user acceptance and in-game guide rendering remain to verify.

Current reward: one full native cargo per selected mission, saved and verified alongside its receipt. The old +10 logistics missions remain historical data and simulator behavior. The installed game references were inspected locally; proprietary assemblies, blueprint packs and extracted assets are excluded from the package.
