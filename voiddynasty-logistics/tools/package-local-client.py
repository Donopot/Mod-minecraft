"""Publish the four exact client JARs; Rogue Trader is optional at runtime."""
from pathlib import Path
import argparse
import json
import os
import zipfile
from bridge_project import ROOT, sha256, validated_payload, version


def metadata(path):
    with zipfile.ZipFile(path) as archive:
        return json.loads(archive.read("fabric.mod.json"))


def dependency(mods, mod_id, expected):
    matches = []
    for path in mods.glob("*.jar"):
        try:
            data = metadata(path)
        except (KeyError, ValueError, zipfile.BadZipFile):
            continue
        if data.get("id") == mod_id and data.get("version") == expected:
            matches.append(path)
    if len(matches) != 1:
        raise ValueError(f"Expected exactly one local {mod_id} {expected} JAR")
    return matches[0]


def write_once(path, data):
    if path.exists() and path.read_bytes() != data:
        raise ValueError(f"Versioned client file already differs: {path}; create a new pack version")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--minecraft-path", type=Path, default=Path(os.environ.get("APPDATA", "")) / ".minecraft")
    parser.add_argument("--base-jar", type=Path, required=True)
    args = parser.parse_args()
    release = version()
    companion = ROOT / f"dist/voiddynasty-bridge-{release}.jar"
    validated_payload(ROOT / ".runtime/fabric-build/build.local.json", companion, base_jar=args.base_jar)
    sources = [args.base_jar, companion,
               dependency(args.minecraft_path / "mods", "fabric-api", "0.162.0+26.3"),
               dependency(args.minecraft_path / "mods", "geckolib", "5.5.7")]
    destination = ROOT.parent / "client-local" / f"voiddynasty-{release}"
    rows = []
    for source in sources:
        data = metadata(source)
        target = destination / "mods" / source.name
        write_once(target, source.read_bytes())
        rows.append({"path": "mods/" + target.name, "id": data["id"], "version": data["version"],
                     "bytes": target.stat().st_size, "sha256": sha256(target), "license_metadata": data.get("license")})
        with zipfile.ZipFile(target) as archive:
            for name in archive.namelist():
                if "/" not in name and "license" in name.lower():
                    write_once(destination / "licences" / (data["id"] + "-" + name), archive.read(name))
    manifest = {"pack_version": release, "minecraft": "26.3", "fabric_loader": "0.19.5", "java": 25,
                "rogue_trader_required": False, "files": rows}
    write_once(destination / "manifest.json", (json.dumps(manifest, indent=2, ensure_ascii=False) + "\n").encode("utf-8"))
    hashes = "".join(row["sha256"] + "  " + row["path"] + "\n" for row in rows)
    write_once(destination / "SHA256.txt", hashes.encode("utf-8"))
    # The guide is maintained in Git; include it in the optional local download ZIP.
    if not (destination / "README.md").is_file():
        raise SystemExit("JARs prepared; add the client README before creating its download ZIP")
    output = ROOT / f"dist/VOID-DYNASTY-Client-local-{release}.zip"
    output.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(destination.rglob("*")):
            if path.is_file():
                archive.write(path, Path("VOID-DYNASTY-Client-local-" + release) / path.relative_to(destination))
    output.with_suffix(".zip.sha256").write_text(sha256(output) + "  " + output.name + "\n", encoding="utf-8")
    print(f"Prepared {len(rows)} client mods: {destination}")
    print(f"Local download ZIP: {output}")


if __name__ == "__main__":
    main()
