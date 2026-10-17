"""Prevent the obsolete bridge copy or stale build from entering player packages."""
from pathlib import Path
import json
import sys
import tempfile
import unittest
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools"))
from bridge_project import BRIDGE, ROOT, fingerprint, sha256, validated_payload
from package import source_files


def jar(path, mod_id, version):
    with zipfile.ZipFile(path, "w") as archive:
        archive.writestr("fabric.mod.json", json.dumps({"id": mod_id, "version": version}))


class BridgeBuildTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.project = self.root / "voiddynasty-bridge"
        self.project.mkdir()
        (self.project / "gradle.properties").write_text("version=0.7.1\n", encoding="utf-8")
        self.source = self.project / "src/main/java/Example.java"
        self.source.parent.mkdir(parents=True)
        self.source.write_text("class Example {}\n", encoding="utf-8")
        self.original = self.root / "base.jar"
        self.artifact = self.root / "bridge.jar"
        jar(self.original, "voiddynasty", "1.0.0")
        jar(self.artifact, "voiddynasty_bridge", "0.7.1")
        self.build = self.root / "build.json"
        self.metadata = {"bridge_project": str(self.project), "version": "0.7.1",
                         "source_sha256": fingerprint(self.project), "artifact": str(self.artifact),
                         "sha256": sha256(self.artifact), "original": str(self.original),
                         "original_sha256": sha256(self.original)}
        self.save()

    def save(self):
        self.build.write_text(json.dumps(self.metadata), encoding="utf-8")

    def validate(self):
        return validated_payload(self.build, self.artifact, self.project)

    def test_verified_base_is_bound_to_the_package(self):
        self.assertEqual(self.validate()["original_sha256"], sha256(self.original))

    def test_legacy_source_tree_is_rejected(self):
        self.metadata["bridge_project"] = str(self.root / "fabric-adapter")
        self.save()
        with self.assertRaisesRegex(ValueError, "canonical"):
            self.validate()

    def test_source_edit_requires_rebuild(self):
        self.source.write_text("class Example { int changed; }\n", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "sources changed"):
            self.validate()

    def test_resource_edit_requires_rebuild(self):
        resource = self.project / "src/main/resources/models/block.json"
        resource.parent.mkdir(parents=True)
        resource.write_text("{}", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "sources changed"):
            self.validate()

    def test_altered_artifact_is_rejected(self):
        with self.artifact.open("ab") as stream:
            stream.write(b"changed")
        with self.assertRaisesRegex(ValueError, "artifact differs"):
            self.validate()

    def test_metadata_disagreement_is_rejected(self):
        jar(self.artifact, "voiddynasty_bridge", "0.7.0")
        self.metadata["sha256"] = sha256(self.artifact)
        self.save()
        with self.assertRaisesRegex(ValueError, "metadata differs"):
            self.validate()

    def test_changed_base_is_rejected(self):
        with self.original.open("ab") as stream:
            stream.write(b"changed base")
        with self.assertRaisesRegex(ValueError, "base mod changed"):
            self.validate()

    def test_renamed_base_requires_identical_bytes(self):
        renamed = self.root / "renamed-base.jar"
        renamed.write_bytes(self.original.read_bytes())
        self.original.unlink()
        result = validated_payload(self.build, self.artifact, self.project, base_jar=renamed)
        self.assertEqual(result["original_sha256"], sha256(renamed))
        with renamed.open("ab") as stream:
            stream.write(b"modified")
        with self.assertRaisesRegex(ValueError, "base mod changed"):
            validated_payload(self.build, self.artifact, self.project, base_jar=renamed)

    def test_package_sources_have_one_bridge_and_no_private_outputs(self):
        files = source_files()
        self.assertFalse((ROOT / "fabric-adapter/src").exists())
        self.assertIn(BRIDGE / "src/main/java/com/donopot/voiddynastybridge/refit/mixin/ControlPadMixin.java", files)
        self.assertFalse(any(".runtime" in p.relative_to(ROOT.parent).parts for p in files))
        self.assertFalse(any(p.suffix in {".dll", ".class", ".exe"} for p in files))
        self.assertTrue(all(p.is_file() for p in files))


if __name__ == "__main__":
    unittest.main()
