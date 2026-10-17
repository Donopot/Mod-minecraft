"""Test compiled production Java and C# diagnostic transport together, without games."""
import json
import os
from pathlib import Path
import socket
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class DiagnosticInteropTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        rt_build = ROOT / ".runtime/rt-build.local.json"
        java_build = ROOT / ".runtime/fabric-build/build.local.json"
        if not rt_build.is_file() or not java_build.is_file():
            raise unittest.SkipTest("Build both game diagnostics first to enable Java/C# interop tests")
        rt = json.loads(rt_build.read_text(encoding="utf-8-sig"))
        java = json.loads(java_build.read_text(encoding="utf-8"))
        cls.work = ROOT / ".runtime/diagnostic-tests"
        cls.work.mkdir(exist_ok=True)
        cls.server_exe = cls.work / "DiagnosticServerHarness.exe"
        result = subprocess.run([rt["compiler"], "/nologo", "/target:exe", "/out:" + str(cls.server_exe),
                                 "/reference:System.Runtime.Serialization.dll", "/reference:System.Xml.dll",
                                 str(ROOT / "rt-adapter/DiagnosticServer.cs"), str(ROOT / "rt-adapter/DiagnosticJson.cs"),
                                 str(ROOT / "tests/DiagnosticServerHarness.cs")],
                                capture_output=True, text=True)
        if result.returncode:
            raise RuntimeError(result.stdout + result.stderr)
        cls.operations_exe = cls.work / "GameSaveOperationsHarness.exe"
        result = subprocess.run([rt["compiler"], "/nologo", "/target:exe", "/out:" + str(cls.operations_exe),
                                 str(ROOT / "rt-adapter/GameSaveOperations.cs"), str(ROOT / "tests/GameSaveOperationsHarness.cs")],
                                capture_output=True, text=True)
        if result.returncode:
            raise RuntimeError(result.stdout + result.stderr)
        cls.state_exe = cls.work / "PerSaveStateHarness.exe"
        result = subprocess.run([rt["compiler"], "/nologo", "/target:exe", "/out:" + str(cls.state_exe),
                                 "/reference:System.Runtime.Serialization.dll", "/reference:System.Xml.dll",
                                 "/reference:System.IO.Compression.dll", "/reference:System.IO.Compression.FileSystem.dll",
                                 str(ROOT / "rt-adapter/DiagnosticJson.cs"), str(ROOT / "rt-adapter/DiagnosticServer.cs"),
                                 str(ROOT / "rt-adapter/PerSaveState.cs"), str(ROOT / "tests/PerSaveStateHarness.cs")],
                                capture_output=True, text=True)
        if result.returncode:
            raise RuntimeError(result.stdout + result.stderr)
        cls.java = str(Path(java["jdk"]) / "bin/java.exe")
        cls.classpath = ";".join([str(cls.work), java["classes"], *java["classpath"]])
        # JDK classpath closes fail in the Windows sandbox. The verify script must
        # be granted the same compiler access as the Fabric build command.
        result = subprocess.run([str(Path(java["jdk"]) / "bin/javac.exe"), "--release", "25", "-proc:none",
                                 "-classpath", cls.classpath, "-d", str(cls.work), str(ROOT / "tests/DiagnosticClientHarness.java")],
                                capture_output=True, text=True)
        if result.returncode:
            raise RuntimeError(result.stdout + result.stderr)

    def setUp(self):
        self.token = "a" * 64
        with socket.socket() as sock:
            sock.bind(("127.0.0.1", 0))
            self.port = sock.getsockname()[1]
        self.frame = {"protocol_version": 1, "type": "diagnostic_snapshot", "diagnostic_only": True,
                      "delivery_enabled": False, "session_id": "00000000-0000-4000-8000-000000000001",
                      "sequence": 1, "campaign_loaded": True, "native_campaign_id": "TEST-CAMPAIGN", "zone": "TEST-ZONE",
                      "game_mode": "TEST", "save_state": "TEST", "status": "TEST"}
        self.server = subprocess.Popen([str(self.server_exe), self.token, str(self.port)], cwd=self.work,
                                       stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        self.addCleanup(self.stop_server)
        self.server.stdin.write("SERIALIZE\n")
        self.server.stdin.flush()
        self.assertEqual(self.server.stdout.readline().strip(), "READY")

    def stop_server(self):
        if self.server.poll() is None:
            self.server.stdin.write("STOP\n")
            self.server.stdin.flush()
        try:
            _, stderr = self.server.communicate(timeout=4)
        except subprocess.TimeoutExpired:
            self.server.kill()
            _, stderr = self.server.communicate()
        self.assertEqual(self.server.returncode, 0, stderr)

    def hello(self, token=None):
        sock = socket.create_connection(("127.0.0.1", self.port), timeout=3)
        sock.settimeout(3)
        sock.sendall((json.dumps({"protocol_version": 1, "type": "diagnostic_hello", "token": token or self.token}) + "\n").encode())
        return sock

    def test_csharp_serves_only_published_snapshot_after_auth(self):
        with self.hello() as connection, connection.makefile("rb") as stream:
            self.assertEqual(json.loads(stream.readline()), self.frame)

    def test_csharp_rejects_wrong_token_then_accepts_new_client(self):
        with self.hello("b" * 64) as connection:
            self.assertEqual(connection.recv(1), b"")
        with self.hello() as connection, connection.makefile("rb") as stream:
            self.assertEqual(json.loads(stream.readline())["native_campaign_id"], "TEST-CAMPAIGN")

    def test_csharp_rejects_duplicate_keys_and_delivery_commands(self):
        for data in (b'{"protocol_version":1,"type":"diagnostic_hello","token":"' + self.token.encode() + b'","token":"x"}\n',
                     b'{"protocol_version":1,"type":"delivery_commit","token":"' + self.token.encode() + b'"}\n',
                     b'{"protocol_version":99999999999999999999999999999999999999999999999999999999,"type":"diagnostic_hello","token":"' + self.token.encode() + b'"}\n'):
            with socket.create_connection(("127.0.0.1", self.port), timeout=3) as connection:
                connection.settimeout(3)
                try:
                    connection.sendall(data)
                    self.assertEqual(connection.recv(1), b"")
                except ConnectionResetError:
                    pass  # Windows may report the intentional rejection as a TCP reset.
        self.assertIsNone(self.server.poll())
        with self.hello() as connection, connection.makefile("rb") as stream:
            self.assertEqual(json.loads(stream.readline())["native_campaign_id"], "TEST-CAMPAIGN")

    def run_java(self, config, *extra):
        return subprocess.run([self.java, "-classpath", self.classpath,
                               "com.donopot.voiddynastybridge.DiagnosticClientHarness", str(config), str(self.port), *extra],
                              cwd=ROOT, capture_output=True, text=True, timeout=16)

    def test_actual_java_client_reads_actual_csharp_server(self):
        with tempfile.TemporaryDirectory() as directory:
            config = Path(directory) / "diagnostic.local.json"
            config.write_text(json.dumps({"protocol_version": 1, "port": self.port, "token": self.token}), encoding="utf-8")
            result = self.run_java(config)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("LIVE TEST-CAMPAIGN TEST-ZONE", result.stdout)

    def test_actual_java_client_expires_repeated_game_snapshot(self):
        # Server repeats the same serialized state every second, as would happen
        # if Unity stopped updating. Repeated network bytes must not refresh it.
        with tempfile.TemporaryDirectory() as directory:
            config = Path(directory) / "diagnostic.local.json"
            config.write_text(json.dumps({"protocol_version": 1, "port": self.port, "token": self.token}), encoding="utf-8")
            result = self.run_java(config, "await-expiry")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("LIVE TEST-CAMPAIGN TEST-ZONE", result.stdout)
        self.assertIn("CLEARED", result.stdout)

    def test_actual_java_client_clears_campaign_on_menu_snapshot(self):
        with tempfile.TemporaryDirectory() as directory:
            config = Path(directory) / "diagnostic.local.json"
            config.write_text(json.dumps({"protocol_version": 1, "port": self.port, "token": self.token}), encoding="utf-8")
            process = subprocess.Popen([self.java, "-classpath", self.classpath,
                                       "com.donopot.voiddynastybridge.DiagnosticClientHarness", str(config), str(self.port), "await-menu"],
                                      cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            try:
                self.assertIn("LIVE TEST-CAMPAIGN TEST-ZONE", process.stdout.readline())
                menu = {**self.frame, "sequence": 2, "campaign_loaded": False, "native_campaign_id": None, "zone": None,
                        "status": "Main menu", "session_id": "00000000-0000-4000-8000-000000000002"}
                self.server.stdin.write(json.dumps(menu) + "\n")
                self.server.stdin.flush()
                stdout, stderr = process.communicate(timeout=10)
                self.assertEqual(process.returncode, 0, stdout + stderr)
                self.assertIn("CLEARED_CAMPAIGN", stdout)
            finally:
                if process.poll() is None:
                    process.kill()
                    process.communicate()

    def test_per_save_state_round_trip_preserves_native_settings(self):
        result = subprocess.run([str(self.state_exe), "roundtrip"], capture_output=True, text=True, timeout=3)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.strip(), "PASS")

    def test_native_save_load_use_ui_entrypoints_and_wait_for_callback_and_ready(self):
        result = subprocess.run([str(self.operations_exe)], capture_output=True, text=True, timeout=3)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.strip(), "PASS")

    def test_saved_archive_reads_namespaced_state_and_missing_baseline(self):
        import zipfile
        result = subprocess.run([str(self.state_exe), "emit"], capture_output=True, text=True, timeout=3)
        self.assertEqual(result.returncode, 0, result.stderr)
        state = result.stdout.strip()
        with tempfile.TemporaryDirectory() as directory:
            for present in (False, True):
                path = Path(directory) / ("test.zks" if present else "baseline.zks")
                settings = {"$id": "1", "native_setting": {"nested": True}}
                if present:
                    settings["voiddynasty_bridge_state_v1"] = state
                with zipfile.ZipFile(path, "w") as archive:
                    archive.writestr("settings.json", json.dumps({"List": settings}))
                read = subprocess.run([str(self.state_exe), "read", str(path)], capture_output=True, text=True, timeout=3)
                self.assertEqual(read.returncode, 0, read.stderr)
                self.assertEqual(read.stdout.strip(), state if present else "<MISSING>")

    def test_archive_with_duplicate_bridge_key_is_rejected(self):
        import zipfile
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "invalid.zks"
            with zipfile.ZipFile(path, "w") as archive:
                archive.writestr("settings.json", '{"List":{"voiddynasty_bridge_state_v1":"one","voiddynasty_bridge_state_v1":"two"}}')
            result = subprocess.run([str(self.state_exe), "read", str(path)], capture_output=True, text=True, timeout=3)
            self.assertNotEqual(result.returncode, 0)


if __name__ == "__main__":
    unittest.main()
