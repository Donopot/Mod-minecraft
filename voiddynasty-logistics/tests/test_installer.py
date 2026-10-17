"""Compile and exercise the real installer engine in isolated fake installations."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


@unittest.skipUnless(os.name == "nt", "Windows .NET Framework installer")
class InstallerTests(unittest.TestCase):
    def test_real_install_restore_failure_guards(self):
        compiler = Path(os.environ["WINDIR"]) / "Microsoft.NET/Framework64/v4.0.30319/csc.exe"
        with tempfile.TemporaryDirectory(prefix="voiddynasty-setup-") as tmp:
            exe = Path(tmp) / "InstallerTests.exe"
            command = [str(compiler), "/nologo", "/utf8output", f"/out:{exe}",
                       "/reference:System.Web.Extensions.dll", "/reference:System.IO.Compression.dll",
                       "/reference:System.IO.Compression.FileSystem.dll",
                       str(ROOT / "installer/InstallerCore.cs"), str(ROOT / "tests/InstallerHarness.cs")]
            build = subprocess.run(command, capture_output=True, text=True)
            self.assertEqual(build.returncode, 0, build.stdout + build.stderr)
            run = subprocess.run([str(exe), tmp], capture_output=True, text=True)
            self.assertEqual(run.returncode, 0, run.stdout + run.stderr)
            self.assertIn("installer assertions", run.stdout)


if __name__ == "__main__":
    unittest.main()
