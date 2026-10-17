"""Compile a separate companion against verified, locally installed Minecraft JARs."""
import argparse
import hashlib
import json
import os
import shutil
from pathlib import Path
import subprocess
import zipfile
from bridge_project import BRIDGE, RESOURCES, fingerprint, validate_base_jar, version

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
    if not original.is_file():
        parser.error("Provide --original-jar pointing to the selected VOID DYNASTY base JAR")
    try:
        original_hash = validate_base_jar(original)
    except (ValueError, KeyError, zipfile.BadZipFile) as error:
        parser.error(str(error))
    release = version()
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
    sources = sorted((BRIDGE / "src/main/java").rglob("*.java"))
    source_hash = fingerprint()
    arguments = ["--release", "25", "-encoding", "UTF-8", "-proc:none", "-d", classes.as_posix(),
                 "-classpath", os.pathsep.join(path.as_posix() for path in dependencies), *[path.as_posix() for path in sources]]
    argfile = work / "javac.args"
    argfile.write_text("\n".join('"' + arg.replace('"', '\\"') + '"' for arg in arguments), encoding="utf-8")
    subprocess.run([str(home / "bin/javac.exe"), "@" + str(argfile)], cwd=ROOT, check=True)
    if validate_base_jar(original) != original_hash:
        raise SystemExit("Base mod changed while compiling; rebuild against a stable file")
    if fingerprint() != source_hash:
        raise SystemExit("Bridge sources changed while compiling; rebuild before packaging")
    output = ROOT / f"dist/voiddynasty-bridge-{release}.jar"
    output.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for source in sorted((classes / "com/donopot/voiddynastybridge").rglob("*.class")):
            archive.write(source, source.relative_to(classes).as_posix())
        resources = RESOURCES
        for source in sorted(resources.rglob("*")):
            if source.is_file():
                name = source.relative_to(resources).as_posix()
                data = source.read_bytes()
                if name == "fabric.mod.json":
                    data = data.replace(b"${version}", release.encode("utf-8"))
                archive.writestr(name, data)
    digest = hashlib.sha256(output.read_bytes()).hexdigest()
    output.with_suffix(".jar.sha256").write_text(digest + "  " + output.name + "\n", encoding="utf-8")
    manifest = {"jdk": str(home.resolve()), "minecraft": str(minecraft), "classpath": [str(p) for p in dependencies],
                "classes": str(classes), "artifact": str(output), "sha256": digest,
                "bridge_project": str(BRIDGE.resolve()), "version": release, "source_sha256": source_hash,
                "original": str(original.resolve()), "original_sha256": original_hash}
    (work / "build.local.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    print("Built separate delivery companion: " + str(output))


if __name__ == "__main__":
    main()
