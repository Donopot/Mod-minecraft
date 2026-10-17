# Installation and removal — delivery prototype 0.7.0

**Recommended for players:** use `dist/VOID-DYNASTY-Assistant-0.7.0.exe` or the player ZIP. It embeds both adapters and provides folder selection, compatibility checks, save copies, updates, restoration and removal. Follow [the Windows setup guide](windows-setup.md). The script instructions below remain for development and manual installation.


The simulator requires Python 3.12+ and no packages. Run commands from the repository root as shown in the README. It opens a loopback port and writes only its selected simulation directory. Stop it with Ctrl+C. Delete only the disposable simulation directory when you deliberately want to reset a simulation.

## Rogue Trader diagnostic

The built folder is `dist/VoidDynastyBridge/`, containing `VoidDynastyBridge.dll`, `Info.json`, and `sha256.txt`. Build it with the local game path using `tools/build-rt-diagnostic.ps1`. The build script reads installed references and writes output in the workspace. It does not install a loader or launch the game.

The Fabric output is `dist/voiddynasty-bridge-0.7.0.jar`, built with `python tools/build-fabric-diagnostic.py`. Keep your original VOID DYNASTY JAR alongside the companion.

## Install both modules

With both games closed:

```powershell
.\tools\install-diagnostics.ps1 -VoidDynastyJar 'C:\Users\Anwender\Desktop\MOD\voiddynasty-1.0.0.jar' -WhatIf
.\tools\install-diagnostics.ps1 -VoidDynastyJar 'C:\Users\Anwender\Desktop\MOD\voiddynasty-1.0.0.jar'
```

The first command previews the destinations. The second copies the original JAR and companion into the existing Minecraft instance, and the DLL/manifest into RT's bundled UMM directory. It backs up any existing target files into `.runtime/install-<UUID>/` before replacement and records `manifest.local.json` with paths and installed hashes. It refuses duplicate mod IDs and rolls back written targets on copy failure. It does not launch either game or modify a save.

Default Minecraft location: `%APPDATA%\.minecraft`. Default RT mod location: `%USERPROFILE%\AppData\LocalLow\Owlcat Games\Warhammer 40000 Rogue Trader\UnityModManager\VoidDynastyBridge`. Optional script parameters support a different Minecraft instance or RT mod root.

Manual test procedure:

1. Close Rogue Trader. Back up the **test** save you intend to use. Do not use your sole campaign copy.
2. Copy the entire `VoidDynastyBridge` output folder to `%USERPROFILE%\AppData\LocalLow\Owlcat Games\Warhammer 40000 Rogue Trader\UnityModManager\VoidDynastyBridge`.
3. Launch the game and open its bundled Unity Mod Manager with Ctrl+F10. Enable the VOID DYNASTY diagnostic and open its panel.
4. Load **VOID DYNASTY - PERSISTANCE VALIDEE**, the copy whose receipt round trip already passed. Do not rerun the persistence probe.
5. Start Minecraft with the matching Fabric profile, create a disposable solo world without LAN, then run `/voidbridge assistant`. Follow [native cargo missions](native-cargo-missions.md) for the current rewards and restart test.
6. The live receiver uses loopback 28765; the diagnostic remains on 28766. Stop any development simulator before starting the real receiver. No Python service is needed during play.

RT creates `%LOCALAPPDATA%\VoidDynastyBridge\diagnostic.local.json` with an automatically generated local authentication token. Minecraft reads it on this same Windows account. Do not share that file. It contains no campaign state or receipts. See [diagnostic channel](diagnostic-link.md) for its scope.

To disable, close both games and restore mod files from the installer backups. Keep the companion installed when opening a world containing its new cargo block; removing it can discard unknown block data. To fully revert a test world, restore its complete pre-mod backup on a separate copy. RT's namespaced receipt state accompanies the native cargo saved in the campaign; removing the mod does not undo native cargo awards. Never copy game-owned Unity, UMM or other DLLs into the distributable.

An upgrade may retain the installed companion's original filename. Fabric uses its internal version, which is 0.7.0.

The diagnostic link, per-save round trip and first historical delivery passed in game. Native cargo awards and the new setup guide still require live acceptance.

The original JAR remains at the user-supplied Desktop path without changes. The distributed ZIP contains the companion and our diagnostic only, not the original mod, game libraries or Java runtime. No new texture/model/sound assets are needed for this slice.

For native rewards, follow [native cargo missions](native-cargo-missions.md). Existing pairs keep their world and latest matching committed RT save. Updates require a stable state without an old in-flight transaction.
