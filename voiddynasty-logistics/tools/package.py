"""Package this project's sources and diagnostic only; exclude user-supplied folders."""
import hashlib
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NAME = "void-dynasty-prototype-0.7.0"


def main():
    output = ROOT / "dist"
    diagnostic = output / "VoidDynastyBridge"
    for required in ("VoidDynastyBridge.dll", "Info.json", "sha256.txt"):
        if not (diagnostic / required).is_file():
            raise SystemExit("Build the Rogue Trader diagnostic first; missing " + required)
    if not (ROOT / "docs/test-results.txt").is_file():
        raise SystemExit("Run python tools/verify.py before packaging")
    files = [ROOT / name for name in (".gitignore", "README.md", "MODLOG.md")]
    for directory in ("void_bridge", "protocol", "rt-adapter", "fabric-adapter", "tests", "tools", "docs", "installer"):
        files += [path for path in (ROOT / directory).rglob("*") if path.is_file()
                  and "__pycache__" not in path.parts and path.suffix != ".pyc"]
    files += list(diagnostic.glob("*"))
    companion = output / "voiddynasty-bridge-0.7.0.jar"
    if not companion.is_file():
        raise SystemExit("Build the Fabric diagnostic companion first")
    files += [companion, companion.with_suffix(".jar.sha256")]
    archive_path = output / (NAME + ".zip")
    with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(files):
            archive.write(path, Path(NAME) / path.relative_to(ROOT))
    digest = hashlib.sha256(archive_path.read_bytes()).hexdigest()
    (output / (NAME + ".sha256")).write_text(digest + "  " + archive_path.name + "\n", encoding="utf-8")
    print(f"Packaged {len(files)} project files: {archive_path}")


if __name__ == "__main__":
    main()
