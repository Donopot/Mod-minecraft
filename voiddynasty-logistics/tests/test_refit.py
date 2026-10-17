"""Real Fabric weaving, real Minecraft container tests and resource reference validation."""
from pathlib import Path
import json
import os
import shutil
import subprocess
import tempfile
import unittest
import zipfile
import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools"))
from bridge_project import RESOURCES, version

ROOT = Path(__file__).resolve().parents[1]
PACK = RESOURCES / "resourcepacks/refit"


@unittest.skipUnless(os.name == "nt", "Installed Windows Minecraft test environment")
class RefitTests(unittest.TestCase):
    def setUp(self):
        self.mc = Path(os.environ["APPDATA"]) / ".minecraft"
        build_file = ROOT / ".runtime/fabric-build/build.local.json"
        if not build_file.is_file():
            self.skipTest("Build the canonical bridge before runtime validation")
        build = json.loads(build_file.read_text(encoding="utf-8"))
        self.original = Path(build["original"])
        self.artifact = ROOT / f"dist/voiddynasty-bridge-{version()}.jar"
        if not self.original.exists() or not self.artifact.exists():
            self.skipTest("Build the current bridge and supply its exact base mod before runtime validation")

    def test_fabric_weaving_and_real_inventories(self):
        build = json.loads((ROOT / ".runtime/fabric-build/build.local.json").read_text(encoding="utf-8"))
        home = Path(build["jdk"])
        with tempfile.TemporaryDirectory(prefix="voiddynasty-refit-") as tmp:
            work = Path(tmp)
            mods = work / "mods"
            mods.mkdir()
            for path in [self.original, self.artifact, self.mc / "mods/fabric-api-0.162.0+26.3.jar",
                         *self.mc.glob("mods/*geckolib*.jar")]:
                shutil.copy2(path, mods / path.name)
            classes = work / "test-classes"
            classes.mkdir()
            dependencies = [Path(p) for p in build["classpath"]]
            cp = os.pathsep.join(map(str, [classes, self.artifact, *dependencies]))
            args = work / "compile.args"
            arguments = ["--release", "25", "-encoding", "UTF-8", "-proc:none", "-classpath", cp,
                         "-d", str(classes), str(ROOT / "tests/RefitKnotHarness.java"), str(ROOT / "tests/RefitInventoryHarness.java")]
            args.write_text("\n".join('"' + p.replace("\\", "/").replace('"', '\\"') + '"' for p in arguments), encoding="utf-8")
            compile_result = subprocess.run([str(home / "bin/javac.exe"), "@" + str(args)], cwd=work, capture_output=True, text=True)
            self.assertEqual(compile_result.returncode, 0, compile_result.stdout + compile_result.stderr)
            launch = [str(home / "bin/java.exe"), "-Djava.awt.headless=true", "-Dmixin.debug.export=true",
                      "-Dfabric.gameJarPath=" + str(dependencies[0]), "-cp", cp,
                      "RefitKnotHarness", str(work), str(classes), str(PACK)]
            run = subprocess.run(launch, cwd=work, capture_output=True, text=True, timeout=90)
            # Keep only textual evidence in the project; transformed/game files remain in OS temporary storage.
            evidence = ROOT / ".runtime/refit-runtime-tests.txt"
            evidence.write_text(run.stdout + run.stderr, encoding="utf-8")
            self.assertEqual(run.returncode, 0, (run.stdout + run.stderr)[-18000:])
            self.assertIn("PASS FABRIC TRANSFORMS", run.stdout)

    def test_refit_models_resolve_and_remain_bounded(self):
        game = self.mc / "versions/fabric-loader-0.19.5-26.3/fabric-loader-0.19.5-26.3.jar"
        with zipfile.ZipFile(self.original) as original, zipfile.ZipFile(game) as vanilla:
            archives = {"voiddynasty": original, "minecraft": vanilla}
            for path in PACK.glob("assets/*/models/**/*.json"):
                model = json.loads(path.read_text(encoding="utf-8"))
                for texture in model.get("textures", {}).values():
                    if texture.startswith("#"):
                        continue
                    namespace, key = texture.split(":", 1)
                    self.assertIn(f"assets/{namespace}/textures/{key}.png", archives[namespace].namelist(), path.name + " -> " + texture)
                for element in model.get("elements", []):
                    self.assertTrue(all(0 <= a < b <= 16 for a, b in zip(element["from"], element["to"])), path.name)
                    for face in element["faces"].values():
                        self.assertIn(face["texture"].lstrip("#"), model["textures"], path.name)
                if "parent" in model:
                    namespace, parent = model["parent"].split(":", 1)
                    own = PACK / "assets" / namespace / "models" / (parent + ".json")
                    self.assertTrue(own.exists() or f"assets/{namespace}/models/{parent}.json" in archives.get(namespace, vanilla).namelist(), path.name)
            self.assertFalse(list(PACK.rglob("*.png")), "No copied bitmaps in the refit pack")
            for path in PACK.glob("assets/*/blockstates/*.json"):
                states = json.loads(path.read_text(encoding="utf-8"))["variants"]
                old = json.loads(original.read("assets/voiddynasty/blockstates/" + path.name))["variants"]
                self.assertEqual(set(states), set(old), path.name + " must preserve actual block properties")
                for variant in states.values():
                    namespace, model = variant["model"].split(":", 1)
                    self.assertTrue((PACK / "assets" / namespace / "models" / (model + ".json")).exists())


if __name__ == "__main__":
    unittest.main()
