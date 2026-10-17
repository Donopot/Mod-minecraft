"""Locate the single Minecraft bridge and verify installer build provenance."""
from pathlib import Path
import hashlib
import json
import zipfile

ROOT = Path(__file__).resolve().parents[1]
BRIDGE = ROOT.parent / "voiddynasty-bridge"
RESOURCES = BRIDGE / "src/main/resources"


def version(project=BRIDGE):
    properties = (project / "gradle.properties").read_text(encoding="utf-8")
    return next(line.split("=", 1)[1].strip() for line in properties.splitlines()
                if line.startswith("version="))


def fingerprint(project=BRIDGE):
    files = [project / "gradle.properties"]
    files += sorted(p for p in (project / "src").rglob("*") if p.is_file())
    digest = hashlib.sha256()
    for path in files:
        digest.update(path.relative_to(project).as_posix().encode("utf-8") + b"\0")
        digest.update(hashlib.sha256(path.read_bytes()).digest())
    return digest.hexdigest()


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def validate_base_jar(path):
    with zipfile.ZipFile(path) as archive:
        metadata = json.loads(archive.read("fabric.mod.json"))
    if metadata.get("id") != "voiddynasty" or metadata.get("version") != "1.0.0":
        raise ValueError("Select a VOID DYNASTY 1.0.0 base JAR; no other mod is accepted")
    return sha256(path)


def validated_payload(build_file, artifact, project=BRIDGE):
    """Reject an old copy, stale sources, altered JAR or changed base before packaging."""
    build = json.loads(build_file.read_text(encoding="utf-8"))
    if Path(build.get("bridge_project", "")).resolve() != project.resolve():
        raise ValueError("Rebuild the companion from the canonical voiddynasty-bridge project")
    if build.get("version") != version(project) or build.get("source_sha256") != fingerprint(project):
        raise ValueError("Bridge sources changed since compilation; rebuild before packaging")
    if Path(build["artifact"]).resolve() != artifact.resolve() or build["sha256"] != sha256(artifact):
        raise ValueError("Companion artifact differs from its verified build")
    with zipfile.ZipFile(artifact) as archive:
        metadata = json.loads(archive.read("fabric.mod.json"))
    if metadata.get("id") != "voiddynasty_bridge" or metadata.get("version") != version(project):
        raise ValueError("Companion metadata differs from the canonical project")
    original = Path(build["original"])
    if validate_base_jar(original) != build["original_sha256"]:
        raise ValueError("The base mod changed since companion compilation")
    return build
