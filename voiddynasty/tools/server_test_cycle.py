#!/usr/bin/env python3
"""Generic headless Fabric server-test cycle: fresh boot -> scopes -> restart -> verify.

Proves a mod's player-driven layer across a REAL reboot, with no Minecraft client. The mod
under test exposes a permission-gated command that logs lines like:

    [mytest] [PASS] A1-catalogue — 40 fruits, 204 techniques
    [mytest] ===== RÉSULTAT all : 18/20 PASS, 2 FAIL =====

Usage
-----
  python3 server_test_cycle.py \
      --server /path/to/fabric-server \
      --jar    /path/to/build/libs/mymod-1.0.0.jar \
      --rcon-password secret \
      --marker mytest \
      --scopes all,persistwrite \
      --verify-scope persistverify \
      --state-dirs config/mymod \
      --report docs/TEST-REPORT.md \
      [--java /path/to/jdk-25/bin/java] \
      [--pre-state 'config/mymod/loot.toml=chance = 1.0'] \
      [--extra-validator 'python3 tools/validate_assets.py']

Notes
-----
* `--pre-state path=content` overwrites a state file right before the fresh boot (deterministic
  probabilities, fixed seeds). Mention it in the report — a forced value is not the shipped default.
* `--state-dirs` are wiped before boot 1 so the run is honest, and restored from a backup afterwards.
* Deduped harvesting: the log accumulates across phases, so check ids are only counted once.
"""
import argparse
import os
import re
import shutil
import socket
import struct
import subprocess
import sys
import time

HOST = "127.0.0.1"


# ------------------------------------------------------------------ RCON (no dependency)
class Rcon:
    def __init__(self, host, port, password, timeout=20):
        self.sock = socket.create_connection((host, port), timeout=timeout)
        self.sock.settimeout(timeout)
        self.rid = 0
        self._send(3, password)
        r = self._recv()
        if not r or r[0] == -1:
            raise RuntimeError("RCON auth refused")

    def _send(self, kind, body):
        self.rid += 1
        payload = struct.pack("<ii", self.rid, kind) + body.encode("utf-8") + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(payload)) + payload)

    def _read_exact(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                break
            buf += chunk
        return buf

    def _recv(self):
        head = self._read_exact(4)
        if not head:
            return None
        (length,) = struct.unpack("<i", head)
        data = self._read_exact(length)
        rid, kind = struct.unpack("<ii", data[:8])
        return rid, kind, data[8:-2].decode("utf-8", "replace")

    def cmd(self, command, settle=2.0):
        self._send(2, command)
        out, deadline = [], time.time() + settle
        while time.time() < deadline:
            try:
                r = self._recv()
            except socket.timeout:
                break
            if r is None:
                break
            out.append(r[2])
            if out:
                break
        return "\n".join(out)

    def close(self):
        try:
            self.sock.close()
        except OSError:
            pass


def port_open(host, port, timeout=1.0):
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except OSError:
        return False


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--server", required=True)
    ap.add_argument("--jar", required=True)
    ap.add_argument("--rcon-password", required=True)
    ap.add_argument("--rcon-port", type=int, default=25575)
    ap.add_argument("--marker", required=True, help="log prefix, e.g. 'mytest'")
    ap.add_argument("--scopes", default="all")
    ap.add_argument("--verify-scope", default="")
    ap.add_argument("--state-dirs", default="", help="comma-separated, relative to --server")
    ap.add_argument("--report", default="")
    ap.add_argument("--java", default=os.environ.get("JAVA", "java"))
    ap.add_argument("--server-jar", default="fabric-server-launch.jar")
    ap.add_argument("--mods-dir", default="mods")
    ap.add_argument("--pre-state", action="append", default=[], help="relpath=content")
    ap.add_argument("--extra-validator", default="")
    ap.add_argument("--boot-timeout", type=int, default=180)
    a = ap.parse_args()

    server = os.path.abspath(a.server)
    log = os.path.join(server, "logs/latest.log")
    mods = os.path.join(server, a.mods_dir)
    state_dirs = [d for d in a.state_dirs.split(",") if d]
    restore = os.path.join(os.path.dirname(server.rstrip("/")), "." + os.path.basename(server) + "-restore")

    evidence, extra = [], []
    seen = set()

    def read_log():
        if not os.path.exists(log):
            return ""
        return open(log, encoding="utf-8", errors="replace").read()

    def harvest(scope):
        for status, cid, detail in re.findall(
                rf"\[{re.escape(a.marker)}\] \[(PASS|FAIL)\] (\S+) — (.*)", read_log()):
            if cid in seen:          # the log accumulates: never double-count
                continue
            seen.add(cid)
            evidence.append((scope, status == "PASS", cid, detail.strip()))

    def boot(tag):
        if os.path.exists(log):
            os.remove(log)
        env = dict(os.environ)
        env["PATH"] = os.path.dirname(a.java) + os.pathsep + env.get("PATH", "")
        proc = subprocess.Popen([a.java, "-Xmx2G", "-jar", a.server_jar, "nogui"], cwd=server, env=env,
                                stdout=open(f"/tmp/{a.marker}-{tag}.log", "w"), stderr=subprocess.STDOUT)
        deadline = time.time() + a.boot_timeout
        while time.time() < deadline:
            if "Done (" in read_log():
                return proc, True
            if proc.poll() is not None:
                return proc, False
            time.sleep(2)
        return proc, False

    def stop():
        if port_open(HOST, a.rcon_port):
            try:
                Rcon(HOST, a.rcon_port, a.rcon_password).cmd("stop")
            except Exception:  # noqa: BLE001
                pass
        deadline = time.time() + 60
        while time.time() < deadline and port_open(HOST, a.rcon_port):
            time.sleep(2)
        return not port_open(HOST, a.rcon_port)

    # ---------------------------------------------------------------- pre-flight
    if port_open(HOST, a.rcon_port):
        try:
            Rcon(HOST, a.rcon_port, a.rcon_password).cmd("stop")
            time.sleep(10)
        except Exception:  # noqa: BLE001
            pass
    if not os.path.exists(a.jar):
        print("jar not found:", a.jar)
        return 1
    if os.path.exists(restore):
        shutil.rmtree(restore)
    os.makedirs(restore)
    for rel in [a.mods_dir] + state_dirs:
        src = os.path.join(server, rel)
        if os.path.exists(src):
            shutil.copytree(src, os.path.join(restore, rel.replace("/", "__")))
    shutil.copy2(a.jar, os.path.join(mods, os.path.basename(a.jar)))
    for rel in state_dirs:                       # honest fresh boot
        p = os.path.join(server, rel)
        if os.path.exists(p):
            shutil.rmtree(p)
        os.makedirs(p, exist_ok=True)
    for item in a.pre_state:                     # deterministic state for the run
        rel, _, content = item.partition("=")
        fp = os.path.join(server, rel)
        os.makedirs(os.path.dirname(fp), exist_ok=True)
        open(fp, "w", encoding="utf-8").write(content)
        print(f"  pre-state written: {rel}")

    try:
        # ------------------------------------------------------------ boot 1
        print("== boot 1 (fresh) ==")
        proc, ok = boot("boot1")
        errs = len(re.findall(r"\b(ERROR|FATAL|Exception)\b", read_log()))
        evidence.append(("boot1", ok, "S0-demarrage", f"server ready={ok}"))
        evidence.append(("boot1", errs == 0, "S1-sans-erreur", f"{errs} ERROR/FATAL/Exception"))
        if not ok:
            raise RuntimeError("server did not start")
        rc = Rcon(HOST, a.rcon_port, a.rcon_password)
        for scope in [s for s in a.scopes.split(",") if s]:
            print(f"== {a.marker} {scope} ==")
            print("  ", rc.cmd(f"{a.marker} {scope}", settle=20)[:200])
            time.sleep(3)
            harvest(scope)

        # ------------------------------------------------------------ restart
        print("== stop ==")
        print("  stopped:", stop())
        rc = proc = None
        print("== boot 2 (restart) ==")
        proc, ok = boot("boot2")
        evidence.append(("boot2", ok, "S2-redemarrage", f"2nd boot ok={ok}"))
        if not ok:
            raise RuntimeError("server did not restart")
        if a.verify_scope:
            rc = Rcon(HOST, a.rcon_port, a.rcon_password)
            print(f"== {a.marker} {a.verify_scope} ==")
            print("  ", rc.cmd(f"{a.marker} {a.verify_scope}", settle=10)[:200])
            time.sleep(3)
            harvest(a.verify_scope)
        stop()
        rc = proc = None
    finally:
        stop()
        for rel in [a.mods_dir] + state_dirs:
            dst = os.path.join(server, rel)
            backup = os.path.join(restore, rel.replace("/", "__"))
            if os.path.exists(dst):
                shutil.rmtree(dst)
            if os.path.exists(backup):
                shutil.copytree(backup, dst)
        shutil.rmtree(restore, ignore_errors=True)
        print("server state restored")

    if a.extra_validator:
        r = subprocess.run(a.extra_validator, shell=True, capture_output=True, text=True)
        extra.append((a.extra_validator.split()[0], r.returncode == 0,
                      (r.stdout.strip().splitlines() or ["(no output)"])[-1]))

    total = len(evidence) + len(extra)
    passed = sum(1 for e in evidence if e[1]) + sum(1 for e in extra if e[1])
    print(f"\nGLOBAL: {passed}/{total} PASS, {total - passed} FAIL")

    if a.report:
        lines = [f"# Test report — {a.marker}", "",
                 f"- Date: {time.strftime('%Y-%m-%d %H:%M:%S')}",
                 f"- Server: `{server}`", f"- Jar: `{a.jar}` ({os.path.getsize(a.jar)} bytes)",
                 f"- Result: **{passed}/{total} PASS**, {total - passed} FAIL", "",
                 "| phase | check | status | detail |", "|---|---|---|---|"]
        for phase, ok, cid, detail in evidence:
            lines.append(f"| {phase} | `{cid}` | {'PASS' if ok else '**FAIL**'} | {detail.replace('|', '/')} |")
        for label, ok, detail in extra:
            lines.append(f"| validator | `{label}` | {'PASS' if ok else '**FAIL**'} | {detail.replace('|', '/')} |")
        lines += ["", "## Not automatable headless (manual pass required)", "",
                  "Visual rendering, keybinds, mouse immersion, combat feel, physical death animation.",
                  "See the manual checklist shipped with the mod."]
        os.makedirs(os.path.dirname(os.path.abspath(a.report)), exist_ok=True)
        open(a.report, "w", encoding="utf-8").write("\n".join(lines) + "\n")
        print("report:", a.report)

    for phase, ok, cid, detail in evidence:
        if not ok:
            print(f"  FAIL {cid}: {detail}")
    for label, ok, detail in extra:
        if not ok:
            print(f"  FAIL {label}: {detail}")
    return 0 if total - passed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
