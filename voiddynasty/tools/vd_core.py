#!/usr/bin/env python3
"""vd_core.py — outils déterministes de pilotage du serveur VOID DYNASTY.

Cœur commun utilisé par la CLI (vdctl.py) et par le serveur MCP (mcp_vd.py).
Règle : chaque outil **vérifie** ce qu'il a fait et renvoie une preuve, pas une intention.

Aucun secret n'est affiché : le mot de passe RCON est lu dans server.properties et n'apparaît
dans aucune sortie.
"""
import os
import re
import socket
import struct
import subprocess
import sys

MC_DIR = os.environ.get("VD_SERVER_DIR", "/opt/data/workspace/mc-server")
PROPS = os.path.join(MC_DIR, "server.properties")
WORLD = os.environ.get("VD_WORLD_DIR", os.path.join(MC_DIR, "world8"))
TOOLS = os.path.dirname(os.path.abspath(__file__))

# commandes acceptées par l'outil générique : volontairement étroit.
ALLOWED = ("fleet ", "vdyntest ", "work ", "list", "save-all", "say ", "time ", "weather ", "forceload ")


def _props():
    txt = open(PROPS, encoding="utf-8", errors="replace").read()
    m_pw = re.search(r"^rcon\.password=(.*)$", txt, re.M)
    m_port = re.search(r"^rcon\.port=(\d+)$", txt, re.M)
    if not m_pw or not m_port:
        raise RuntimeError("rcon.password ou rcon.port absent de %s" % PROPS)
    return m_pw.group(1), int(m_port.group(1))


class Rcon:
    """Client RCON minimal, patient (les poses de vaisseaux géants dépassent 4 s)."""

    def __init__(self, timeout=600):
        pw, port = _props()
        self.s = socket.create_connection(("127.0.0.1", port), 15)
        self.s.settimeout(timeout)
        self.rid = 0
        self._send(3, pw)
        if self._recv()[0] == -1:
            raise RuntimeError("authentification RCON refusee (mot de passe invalide)")

    def _recvn(self, n):
        b = b""
        while len(b) < n:
            c = self.s.recv(n - len(b))
            if not c:
                raise RuntimeError("connexion RCON fermee par le serveur")
            b += c
        return b

    def _send(self, t, body):
        self.rid += 1
        d = struct.pack("<ii", self.rid, t) + body.encode("utf-8") + b"\x00\x00"
        self.s.sendall(struct.pack("<i", len(d)) + d)

    def _recv(self):
        ln = struct.unpack("<i", self._recvn(4))[0]
        d = self._recvn(ln)
        rid, t = struct.unpack("<ii", d[:8])
        return rid, t, d[8:-2].decode("utf-8", "replace")

    def cmd(self, c):
        self._send(2, c)
        return self._recv()[2]

    def close(self):
        try:
            self.s.close()
        except Exception:
            pass


def rcon(*commands, timeout=600):
    """Exécute des commandes et renvoie la liste des réponses. Lève si le serveur ne répond pas."""
    try:
        r = Rcon(timeout=timeout)
    except (ConnectionRefusedError, socket.timeout, OSError) as e:
        raise RuntimeError("serveur Minecraft injoignable en RCON (%s) — est-il démarré ?" % type(e).__name__)
    out = []
    try:
        for c in commands:
            out.append(r.cmd(c))
    finally:
        r.close()
    return out


# ---------------- outils ----------------

def server_status():
    """État du serveur : joueurs connectés, et si le harnais répond."""
    try:
        players = rcon("list")[0]
    except RuntimeError as e:
        return {"online": False, "detail": str(e)}
    return {"online": True, "players": players.strip(), "world": WORLD}


def vdyntest(scope="all"):
    """Lance /vdyntest <scope> et renvoie le compte PASS/FAIL lu dans la sortie."""
    rep = rcon("vdyntest " + scope)[0]
    m = re.search(r"(\d+)\s*PASS,\s*(\d+)\s*FAIL", rep)
    passed, failed = (int(m.group(1)), int(m.group(2))) if m else (None, None)
    return {"scope": scope, "pass": passed, "fail": failed, "raw": rep.strip()[-400:]}


def _as_player(player, command):
    return rcon('execute as %s run %s' % (player, command))[0]


def fleet(action, player, name=None, extra=None):
    """Commandes /fleet exécutées en tant que joueur (RCON n'a pas de joueur)."""
    cmd = "fleet " + action
    if name:
        cmd += " " + name
    if extra:
        cmd += " " + extra
    rep = _as_player(player, cmd)
    return {"command": cmd, "player": player, "result": rep.strip()[-500:]}


def fleet_add(player, ship_type, name):
    return fleet("add", player, ship_type, name)


def place_ship(ship_type, x, y, z):
    """Pose un vaisseau depuis son template et lit ce qui a été posé (le mod relit aussitôt)."""
    rep = rcon("fleet check %s place %d %d %d" % (ship_type, x, y, z), timeout=900)[0]
    ident = re.search(r"relu aussit[oô]t\s*:\s*(\d+)\s*identiques,\s*(\d+)\s*vides,\s*(\d+)\s*autres", rep)
    total = re.search(r"(\d+)\s*blocs", rep)
    return {
        "type": ship_type, "position": [x, y, z],
        "placed": int(total.group(1)) if total else None,
        "verified": ({"identiques": int(ident.group(1)), "vides": int(ident.group(2)),
                      "autres": int(ident.group(3))} if ident else None),
        "raw": rep.strip()[-600:],
    }


def scan_zone(x1, y1, z1, x2, y2, z2, dimension="overworld"):
    """Scan en lecture seule d'une zone : recensement des blocs et dangers."""
    code = (
        "import json,sys;"
        "sys.path.insert(0,%r);"
        "import work_scan as w;"
        "t,c,f,l,rd,ch=w.scan(%r,%d,%d,%d,%d,%d,%d,%r);"
        "import collections;"
        "cats=collections.Counter();"
        "[cats.update({k.split(' ')[0][7:]:v}) for k,v in c.items() if k.startswith('danger:')];"
        "print(json.dumps({'total':t,'chunks':ch,'regions':l,"
        "'dangers':dict(cats),'blocs':{k:v for k,v in c.most_common(15) if not k.startswith('danger:')},"
        "'exemples':{k:v[:5] for k,v in f.items()}}))"
        % (TOOLS, WORLD, x1, y1, z1, x2, y2, z2, dimension)
    )
    r = subprocess.run([sys.executable, "-c", code], capture_output=True, text=True, timeout=600)
    if r.returncode != 0:
        return {"error": (r.stderr or r.stdout)[-400:]}
    import json as _json
    return _json.loads(r.stdout.strip().splitlines()[-1])


def command(cmd):
    """Commande générique, limitée à une liste blanche stricte."""
    if not any(cmd.startswith(a) for a in ALLOWED):
        return {"refused": True,
                "reason": "commande hors liste blanche (autorisé : %s)" % ", ".join(a.strip() for a in ALLOWED)}
    return {"command": cmd, "result": rcon(cmd)[0].strip()[-800:]}


TOOLS_MAP = {
    "server_status": server_status,
    "vdyntest": vdyntest,
    "fleet": fleet,
    "fleet_add": fleet_add,
    "place_ship": place_ship,
    "scan_zone": scan_zone,
    "command": command,
}
