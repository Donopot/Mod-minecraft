# Universal Modder review — 15 October 2026

User-provided toolkit: `universal-modder-main/`, version 0.2.0. Guides used: `skills/mod-any-game/SKILL.md`, its Unity/Minecraft references, `skills/reverse-engineering/SKILL.md`, and the local bridge-contract technique note.

## Results from actual tools

From the toolkit directory:

```powershell
python -m um scan 'C:\Users\Anwender\Desktop\JEU\rogue trader\Warhammer.40000.Rogue.Trader.Voidfarer.Edition-InsaneRamZes'
python -m um publish check '..\.runtime\universal-modder-review\void-dynasty-prototype-0.2.0' --game 'C:\Users\Anwender\Desktop\JEU\rogue trader\Warhammer.40000.Rogue.Trader.Voidfarer.Edition-InsaneRamZes'
```

- Scanner correctly identified Unity Mono 6000.0.64f1, Owlcat Games and the user-data product name.
- Scanner reported no installed loader and suggested generic BepInEx/MelonLoader routes. That detection misses this game's **bundled UMM archive and bridge**; retain the locally verified UMM integration. Do not install a second loader based on that generic suggestion.
- Package check on the extracted 0.2.0 distributable: **46 files, zero failures, four warnings**, all for documented absolute user paths. These paths are useful for this PC's local setup; redact them before a public release. No public publishing was performed.
- `um kb search` requires PyYAML, absent from the current interpreter. The offline uv attempt could not resolve uncached NumPy; no dependencies were downloaded. Read `knowledge/INDEX.md` and searched the local notes directly instead.
- No dedicated Rogue Trader note or verified recipe for its current per-save extension was found.
- Current game logs predate the new diagnostic installation. They do not prove that the companion or RT diagnostic has loaded in either game.

## Useful additions to the project workflow

1. **Inspect actual code:** the toolkit recommends metadata/decompilation inspection before using undocumented Unity APIs. Continue that work around the installed `Code.dll` entity parts, serialization and save completion. The toolkit does not itself provide those APIs.
2. **Back up and isolate:** `um backup` can snapshot saves and profiles. It supports `UM_HOME` to keep private development backups under ignored workspace storage. Choose a test world/campaign before the next modded test. A snapshot alone is not proof of a consistent cross-game checkpoint.
3. **Use real game evidence:** verify UMM/Fabric loading, campaign/zone display and save/reload through game logs and a repeatable test scenario. Standalone Java/C# tests remain transport evidence only.
4. **Maintain ownership and lifecycle contracts:** the toolkit's bridge note supports the current separate authorities, stale-session handling and bounded network workers. The existing delivery protocol and read-only channel document these rules.
5. **Check distributables:** use `um publish check` on staged packages, keeping tokens, proprietary references, original game assets and development caches out of them.

No game input was driven, game launched, new loader installed, registry setting changed, or material published during this review. The original source project remains unavailable; the independent Fabric companion is still the integration route. Real per-save RT receipt persistence remains unverified.
