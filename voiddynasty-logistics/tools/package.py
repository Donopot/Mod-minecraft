"""Package logistics and its sibling canonical bridge in a reconstructible layout."""
import hashlib
from pathlib import Path
import zipfile
from bridge_project import ROOT, BRIDGE, validated_payload, version


def source_files():
    files = [ROOT / name for name in ("README.md", "MODLOG.md")]
    files += [ROOT.parent / ".gitignore", ROOT.parent / "docs/CENTRALISATION.md",
              ROOT.parent / "docs/BRIDGE-UNIQUE-0.7.1.md"]
    for directory in ("void_bridge", "protocol", "rt-adapter", "tests", "tools", "docs", "installer", "fabric-adapter"):
        files += [p for p in (ROOT / directory).rglob("*") if p.is_file()
                  and "__pycache__" not in p.parts and p.suffix != ".pyc" and ".local." not in p.name]
    files += [BRIDGE / name for name in ("README.md", "build.gradle", "settings.gradle", "gradle.properties", "gradlew")]
    for directory in ("src", "gradle"):
        files += [p for p in (BRIDGE / directory).rglob("*") if p.is_file()]
    files += [BRIDGE / "libs/README.md"]
    return files


def main():
    release = version()
    name = "void-dynasty-prototype-" + release
    output = ROOT / "dist"
    diagnostic = output / "VoidDynastyBridge"
    for required in ("VoidDynastyBridge.dll", "Info.json", "sha256.txt"):
        if not (diagnostic / required).is_file():
            raise SystemExit("Build the Rogue Trader adapter first; missing " + required)
    if not (ROOT / "docs/test-results.txt").is_file():
        raise SystemExit("Run python tools/verify.py before packaging")
    companion = output / f"voiddynasty-bridge-{release}.jar"
    validated_payload(ROOT / ".runtime/fabric-build/build.local.json", companion)
    files = source_files() + [diagnostic / p for p in ("VoidDynastyBridge.dll", "Info.json", "sha256.txt")]
    files += [companion, companion.with_suffix(".jar.sha256")]
    archive_path = output / (name + ".zip")
    with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr(name + "/README.md", "Sources : voiddynasty-bridge/ et voiddynasty-logistics/. "
                         "Conserver ces deux dossiers voisins pour reconstruire le compagnon et l'assistant.\n")
        for path in sorted(files):
            archive.write(path, Path(name) / path.relative_to(ROOT.parent))
    digest = hashlib.sha256(archive_path.read_bytes()).hexdigest()
    (output / (name + ".sha256")).write_text(digest + "  " + archive_path.name + "\n", encoding="utf-8")
    print(f"Packaged {len(files)} project files: {archive_path}")


if __name__ == "__main__":
    main()
