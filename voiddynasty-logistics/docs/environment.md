# Environment inventory — 15 October 2026

Confirmed by local reads, archive metadata, compiler output and assembly metadata; game behavior has not been tested in this session.

| Component | Evidence / result |
|---|---|
| Workspace | `C:\Users\Anwender\Documents\ChatGPT\codexmod` |
| Git | Initially empty repository on unborn `master`, no remote, no commits. Work branch: `codex/void-dynasty-prototype`. |
| Python | 3.12.10, executable in `AppData\Local\Programs\Python\Python312` |
| .NET SDK | None (`dotnet --info`). Installed .NET runtimes: 6.0.36 and 8.0.13. |
| C# compiler | `C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe`; successfully compiled the diagnostic after adding the game's `netstandard.dll` reference. |
| Java / Gradle | Not on PATH initially. Later found Java 25 JRE in Modrinth metadata and `Downloads\OpenJDK25U-jdk_x64_windows_hotspot_25.0.4.1_1.zip`; extracted JDK into `.runtime/jdk/jdk-25.0.4.1+1`. Companion successfully compiled with that JDK. |
| Rogue Trader root | `C:\Users\Anwender\Desktop\JEU\rogue trader\Warhammer.40000.Rogue.Trader.Voidfarer.Edition-InsaneRamZes` |
| RT build indication | Installation `.current-version-index` points to `wh_release_1.6.1_DLC3_AfterRelease` / `release514.lsi`. Inferred build: 1.6.1.514; executable's running version not independently checked. |
| Unity | `Player.log`: 6000.0.64f1 (5360b7cd7953), Mono runtime. Older template's Unity 2021 instructions do not match this installation. |
| RT managed references | `WH40KRT_Data\Managed\Code.dll`, Unity Core/IMGUI modules, `netstandard.dll`, Newtonsoft.Json and Harmony present. |
| UMM | Bundled in `WH40KRT_Data\StreamingAssets\OwlcatUnityModManager.zip`; `UnityModManager.dll` extracted locally for compilation only. |
| RT user data | `C:\Users\Anwender\AppData\LocalLow\Owlcat Games\Warhammer 40000 Rogue Trader`; save and UMM directories exist; no installed UMM mod subfolders were returned by the inventory. |
| Existing VOID DYNASTY artifact | `C:\Users\Anwender\Downloads\voiddynasty-1.0.0 (17).jar`; inspected as ZIP without modifying it. |
| JAR metadata | ID `voiddynasty`, version 1.0.0, Minecraft `~26.3`, Fabric Loader `>=0.19.5`, Java `>=25`, Fabric API and GeckoLib required. |
| User-confirmed JAR | `C:\Users\Anwender\Desktop\MOD\voiddynasty-1.0.0.jar`, byte-identical to Downloads `(17)`; SHA-256 `9d98712d9fe37133dcea81d992e99fcf1a13618ebbb589350b60ee0c4159e98e`. No sources/build files. User confirmed no original source project is available; integration now uses a separate companion. |
| Minecraft instance | `%APPDATA%\.minecraft`; local `latest.log` confirms 26.3, Loader 0.19.5, API 0.162.0+26.3 and GeckoLib 5.5.7. Original VOID DYNASTY was absent from its `mods` folder during discovery. |
| Handoff document | Downloads `voiddynasty-HANDOFF.md` describes a remote `/opt/data/workspace/um-test-mod` build; this is documentary evidence, not a locally verified source folder. |
| Toolkit | `universal-modder-main/` appeared during work; contains a modding toolkit, no VOID DYNASTY sources found. Left unchanged. |

## Installed API metadata confirmed

`tools/InspectAssemblies.cs` uses reflection-only loading: it does not execute game code. Its output stays under `.runtime/`.

- `Kingmaker.Game`: `HasInstance`, `Instance`, `Player`, `CurrentlyLoadedArea`, `CurrentMode`, `IsInMainMenu`, `IsUnloading`, `SaveManager`.
- `Kingmaker.Player`: native string `GameId`, `IsInCombat`, `Campaign`; inherits the current entity implementation.
- `Kingmaker.Blueprints.Area.BlueprintArea`: `AreaDisplayName`.
- `SaveManager`: `CurrentState`, `CommitInProgress`, `SaveRoutine`, and save-list APIs.
- `SaveInfo`: `SaveId`, `GameId`, `OperationState`, `IsActuallySaved`.
- `Entity`: generic `GetOptional<TPart>()`, `GetOrCreate<TPart>()`; the older `Ensure<...>` example does not match these declared APIs.
- No type with `KeyValue` in its name was returned from installed `Code.dll`. The old `EntityPartKeyValueStorage` route is not established for this build.
- UMM `ModEntry`: typed `OnGUI`, `OnUpdate`, `OnUnload`, logger and normal entrypoint support.

Some framework metadata cannot be formatted by the older reflection-only runtime (`System.IAsyncDisposable`), and the inspector reports those individual members as unavailable. Diagnostic members above were successfully resolved.

## Still needed

1. In-game baseline for the supplied VOID DYNASTY JAR and a disposable Minecraft world. A full original rebuild is unavailable; use the standalone companion.
2. Manual launch/display verification for the compiled diagnostics in both games.
3. A designated Rogue Trader **test** campaign/save and manual diagnostic launch.
4. Working per-save custom-state serialization and confirmed save completion for RT 1.6.1.514, including two-save isolation and reload proof.
5. Ship movement API and whether it preserves, clones or drops block-entity data.

After the user closed Minecraft, the original JAR and companion were copied into the Minecraft mods folder and our diagnostic DLL/manifest into RT's UMM folder. Installed hashes were verified and an installation manifest was saved under ignored `.runtime/`. Original Desktop/Downloads JARs, game-owned files and saves remain unchanged. No game was launched by this work. Existing save archive entries and a save header were read; their contents are not distributed.

Reference documentation checked: [Owlcat official template](https://github.com/OwlcatOpenSource/RTModificationTemplate/blob/main/README.md) describes older UMM/per-save extension examples; [ToyBox Rogue Trader](https://github.com/xADDBx/ToyBox-RogueTrader) documents the game-bundled loader and user mod path. Local assembly metadata takes precedence for this diagnostic build.

The user subsequently launched both games and supplied a screenshot confirming the first live diagnostic connection. Current logs confirm UMM activation and Fabric loading of 0.2.0 and connected command output. See `docs/acceptance.md` for the scope of that real-game proof; save persistence remains unverified.
