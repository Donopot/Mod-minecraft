"""Build a standalone Windows wizard from our code and our two compiled adapters."""
from pathlib import Path
import hashlib
import json
import os
import subprocess
import zipfile
from bridge_project import validated_payload, version as bridge_version

ROOT = Path(__file__).resolve().parents[1]


def main():
    version = bridge_version()
    if json.loads((ROOT / "rt-adapter/Info.json").read_text(encoding="utf-8"))["Version"] != version:
        raise SystemExit("Bridge and RT package versions must match")
    work = ROOT / ".runtime/installer-build"
    work.mkdir(parents=True, exist_ok=True)
    rt = ROOT / "dist/VoidDynastyBridge"
    mc = ROOT / f"dist/voiddynasty-bridge-{version}.jar"
    bridge = validated_payload(ROOT / ".runtime/fabric-build/build.local.json", mc)
    if json.loads((rt / "Info.json").read_text(encoding="utf-8"))["Version"] != version:
        raise SystemExit("Rebuild the RT adapter before packaging this release")
    local = json.loads((ROOT / ".runtime/rt-build.local.json").read_text(encoding="utf-8-sig"))
    code = Path(local["managed"]) / "Code.dll"
    digest = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
    manifest = work / "payload.json"
    manifest.write_text(json.dumps({
        "version": version,
        "original_sha256": bridge["original_sha256"],
        "bridge_source_sha256": bridge["source_sha256"],
        "game_code_sha256": digest(code),
        "minecraft_sha256": digest(mc),
        "dll_sha256": digest(rt / "VoidDynastyBridge.dll"),
        "info_sha256": digest(rt / "Info.json"),
    }, indent=2), encoding="utf-8")
    compiler = Path(os.environ["WINDIR"]) / "Microsoft.NET/Framework64/v4.0.30319/csc.exe"
    exe = ROOT / f"dist/VOID-DYNASTY-Assistant-{version}.exe"
    command = [str(compiler), "/nologo", "/target:winexe", "/platform:anycpu", "/optimize+", "/utf8output",
               f"/out:{exe}", f"/win32manifest:{ROOT / 'installer/app.manifest'}"]
    command += ["/reference:" + r for r in (
        "System.Windows.Forms.dll", "System.Drawing.dll", "System.Web.Extensions.dll",
        "System.IO.Compression.dll", "System.IO.Compression.FileSystem.dll")]
    for path, name in [(mc, "minecraft"), (rt / "VoidDynastyBridge.dll", "dll"),
                       (rt / "Info.json", "info"), (manifest, "manifest")]:
        command.append(f"/resource:{path},payload.{name}")
    command += [str(ROOT / "installer" / name) for name in ("InstallerCore.cs", "Program.cs", "AssemblyInfo.cs")]
    subprocess.run(command, cwd=ROOT, check=True)
    readme = (ROOT / "installer/LIRE-MOI.txt").read_text(encoding="utf-8")
    archive = ROOT / f"dist/VOID-DYNASTY-Joueur-{version}.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as z:
        z.write(exe, exe.name)
        z.writestr("LIRE-MOI.txt", readme.encode("utf-8-sig"))
        z.writestr("SHA256.txt", f"{digest(exe)}  {exe.name}\n")
    for p in (exe, archive):
        p.with_suffix(p.suffix + ".sha256").write_text(f"{digest(p)}  {p.name}\n", encoding="utf-8")
    print(f"Built {exe}\nPlayer package: {archive}")


if __name__ == "__main__":
    main()
