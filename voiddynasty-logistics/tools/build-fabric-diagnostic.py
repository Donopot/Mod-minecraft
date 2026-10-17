"""Compile a separate companion against verified, locally installed Minecraft JARs."""
import argparse
import hashlib
import json
import os
import shutil
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--minecraft-path", type=Path, default=Path(os.environ["APPDATA"]) / ".minecraft")
    parser.add_argument("--java-home", type=Path)
    parser.add_argument("--original-jar", type=Path)
    args = parser.parse_args()
    homes = list((ROOT / ".runtime/jdk").glob("*"))
    home = args.java_home or (homes[0] if len(homes) == 1 else None)
    if home is None or not (home / "bin/javac.exe").is_file():
        parser.error("Provide --java-home pointing to a JDK 25 installation")
    minecraft = args.minecraft_path.resolve()
    original = args.original_jar or minecraft / "mods/voiddynasty-1.0.0.jar"
    if not original.is_file() or hashlib.sha256(original.read_bytes()).hexdigest() != "9d98712d9fe37133dcea81d992e99fcf1a13618ebbb589350b60ee0c4159e98e":
        parser.error("Provide the verified original VOID DYNASTY 1.0.0 JAR for the refit API references")
    game = minecraft / "versions/fabric-loader-0.19.5-26.3/fabric-loader-0.19.5-26.3.jar"
    loader = minecraft / "libraries/net/fabricmc/fabric-loader/0.19.5/fabric-loader-0.19.5.jar"
    api = minecraft / "mods/fabric-api-0.162.0+26.3.jar"
    for path in (game, loader, api):
        if not path.is_file():
            parser.error("Missing confirmed local dependency: " + str(path))
    work = ROOT / ".runtime/fabric-build"
    modules, classes = work / "modules", work / "classes"
    modules.mkdir(parents=True, exist_ok=True)
    classes.mkdir(parents=True, exist_ok=True)
    namespace = classes / "com/donopot/voiddynastybridge"
    if namespace.exists():
        expected = ROOT.resolve() / ".runtime/fabric-build/classes/com/donopot/voiddynastybridge"
        if namespace.resolve() != expected:
            parser.error("Refusing to clear a compiler output path outside the intended workspace directory")
        shutil.rmtree(namespace)
    subprocess.run([os.sys.executable, str(ROOT / "tools/generate-refit-models.py")], cwd=ROOT, check=True)
    with zipfile.ZipFile(api) as archive:
        for name in archive.namelist():
            if name.startswith("META-INF/jars/") and name.endswith(".jar"):
                (modules / Path(name).name).write_bytes(archive.read(name))
    dependencies = [game, loader, original, *sorted(modules.glob("*.jar")), *sorted((minecraft / "libraries").rglob("*.jar")), *sorted((minecraft / "mods").glob("*geckolib*.jar"))]
    sources = sorted((ROOT / "fabric-adapter/src/client/java").rglob("*.java")) + sorted((ROOT / "fabric-adapter/src/main/java").rglob("*.java"))
    arguments = ["--release", "25", "-encoding", "UTF-8", "-proc:none", "-d", classes.as_posix(),
                 "-classpath", ";".join(path.as_posix() for path in dependencies), *[path.as_posix() for path in sources]]
    argfile = work / "javac.args"
    argfile.write_text("\n".join('"' + arg.replace('"', '\\"') + '"' for arg in arguments), encoding="utf-8")
    subprocess.run([str(home / "bin/javac.exe"), "@" + str(argfile)], cwd=ROOT, check=True)
    output = ROOT / "dist/voiddynasty-bridge-0.7.0.jar"
    output.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for source in sorted((classes / "com/donopot/voiddynastybridge").rglob("*.class")):
            archive.write(source, source.relative_to(classes).as_posix())
        resources = ROOT / "fabric-adapter/src/client/resources"
        for source in sorted(resources.rglob("*")):
            if source.is_file():
                archive.write(source, source.relative_to(resources).as_posix())
    digest = hashlib.sha256(output.read_bytes()).hexdigest()
    output.with_suffix(".jar.sha256").write_text(digest + "  " + output.name + "\n", encoding="utf-8")
    manifest = {"jdk": str(home.resolve()), "minecraft": str(minecraft), "classpath": [str(p) for p in dependencies],
                "classes": str(classes), "artifact": str(output), "sha256": digest}
    (work / "build.local.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    print("Built separate delivery companion: " + str(output))


if __name__ == "__main__":
    main()
