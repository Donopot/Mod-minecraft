#!/usr/bin/env python3
"""Lance le serveur de test avec le jar frais, invoque UNE portée dftest, imprime les résultats.

Usage : python3 tools/run_scope.py <scope> [<scope2> ...]

Utilise la classe Rcon de run_tests.py. Le serveur est arrêté à la fin. Le build est vérifié AVANT
d'écrire le jar dans mods/ — un build raté laisserait le jar précédent en place et les mesures
porteraient sur du code qui n'existe plus.
"""
import os
import re
import shutil
import socket
import subprocess
import sys
import time

ROOT = "/opt/data/workspace/opworldlike-fabric"
SERVER = "/opt/data/workspace/mc-server"
JAR = os.path.join(ROOT, "build/libs/opworldlike-1.0.0.jar")
LOG = os.path.join(SERVER, "logs/latest.log")


def sh(cmd, **kw):
    return subprocess.run(["bash", "-lc", cmd], capture_output=True, text=True, **kw)


def port_open(port=25575):
    try:
        with socket.create_connection(("127.0.0.1", port), timeout=1):
            return True
    except OSError:
        return False


def main():
    scopes = sys.argv[1:] or ["all"]
    env = dict(os.environ)
    env["JAVA_HOME"] = "/opt/data/jdk/jdk-25.0.4.1+1"
    env["PATH"] = env["JAVA_HOME"] + "/bin:" + env["PATH"]

    print("== build ==", flush=True)
    build = subprocess.run(["./gradlew", "jar", "--no-daemon"], cwd=ROOT, env=env,
                           capture_output=True, text=True)
    if "BUILD SUCCESSFUL" not in build.stdout:
        print("BUILD FAILED — on NE teste PAS : le jar précédent serait utilisé.")
        print("\n".join(l for l in build.stdout.splitlines() if "error:" in l)[:2000])
        sys.exit(1)
    print("BUILD OK", flush=True)

    sh("python3 %s/tools/mc_kill_server.py --kill" % ROOT)
    sh("cd %s && rm -f world8/session.lock && rm -f logs/latest.log" % SERVER)
    shutil.copy2(JAR, os.path.join(SERVER, "mods/opworldlike-1.0.0.jar"))

    proc = subprocess.Popen(["java", "-Xmx2G", "-jar", "fabric-server-launch.jar", "nogui"],
                            cwd=SERVER, env=env, stdout=open("/tmp/mc-scope.log", "w"),
                            stderr=subprocess.STDOUT)
    for _ in range(180):
        if port_open():
            break
        time.sleep(2)
    else:
        print("serveur injoignable")
        proc.kill()
        sys.exit(1)
    time.sleep(8)

    sys.path.insert(0, os.path.join(ROOT, "tools"))
    spec = __import__("importlib.util", fromlist=["util"]).spec_from_file_location(
        "rt", os.path.join(ROOT, "tools/run_tests.py"))
    rt = __import__("importlib.util", fromlist=["util"]).module_from_spec(spec)
    spec.loader.exec_module(rt)
    rcon = rt.Rcon("127.0.0.1", 25575, "umtest-rcon-2026")

    for scope in scopes:
        print("\n== dftest %s ==" % scope, flush=True)
        print("  ", rcon.cmd("dftest %s" % scope, settle=8)[:200], flush=True)
        time.sleep(2)
        with open(LOG, encoding="utf-8", errors="replace") as f:
            txt = f.read()
        lignes = [l for l in txt.splitlines() if "[dftest]" in l]
        for l in lignes:
            print("   ", l.split("]: ", 1)[-1][:700], flush=True)

    rcon.cmd("stop")
    time.sleep(8)
    proc.wait(timeout=60)
    print("\nserveur arrêté")


if __name__ == "__main__":
    main()
