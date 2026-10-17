#!/usr/bin/env python3
"""vd_core.py — outils déterministes de pilotage du serveur VOID DYNASTY.

Cœur commun utilisé par la CLI (vdctl.py) et par le serveur MCP (mcp_vd.py).
Règle : chaque outil **vérifie** ce qu'il a fait et renvoie une preuve, pas une intention.

Aucun secret n'est affiché : le mot de passe RCON est lu dans server.properties et n'apparaît
dans aucune sortie.
"""
import os
import json
import base64
import uuid
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
    with open(PROPS, encoding="utf-8", errors="replace") as stream:
        txt = stream.read()
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
        try:
            while True:
                rid, kind, _ = self._recv()
                if rid == -1:
                    raise RuntimeError("authentification RCON refusee")
                if rid == self.rid and kind == 2:
                    break
        except Exception:
            self.s.close()
            raise

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
        if not 10 <= ln <= 1024 * 1024:
            raise RuntimeError("taille de paquet RCON invalide")
        d = self._recvn(ln)
        rid, t = struct.unpack("<ii", d[:8])
        return rid, t, d[8:-2].decode("utf-8", "replace")

    def cmd(self, c):
        self._send(2, c)
        while True:
            rid, kind, text = self._recv()
            if rid == self.rid and kind == 0:
                return text

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


# Structured mission API. Only these typed calls should be exposed to the Hermes worker role.
def _dimension(value):
    if not isinstance(value, str) or len(value) > 128:
        raise ValueError("Invalid dimension")
    value = value if ":" in value else "minecraft:" + value
    if not re.fullmatch(r"[a-z0-9_.-]+:[a-z0-9_./-]+", value):
        raise ValueError("Invalid dimension")
    return value


def _mission_call(tail, dimension):
    try:
        dim = _dimension(dimension)
    except ValueError as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    try:
        reply = rcon("execute in " + dim + " run work mission " + tail, timeout=30)[0]
    except (RuntimeError, OSError) as error:
        return {"ok": False, "error": "SERVER_UNAVAILABLE", "detail": str(error)}
    marker = "VDMISSION "
    if marker not in reply:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": "Missing mission JSON response; server may need the mission build"}
    try:
        result, _ = json.JSONDecoder().raw_decode(reply.split(marker, 1)[1])
        if not isinstance(result, dict) or type(result.get("ok")) is not bool:
            raise ValueError("Invalid response")
        return result
    except (ValueError, TypeError):
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": "Invalid mission JSON response"}


def _position(value):
    if not isinstance(value, (tuple, list)) or len(value) != 3 or any(type(n) is not int for n in value):
        raise ValueError("Position requires exactly three integers")
    x, y, z = value
    if abs(x) > 29999984 or abs(z) > 29999984 or not -2048 <= y <= 2047:
        raise ValueError("Position out of bounds")
    return list(value)


def servitor_capabilities(dimension="overworld"):
    return _mission_call("capabilities", dimension)


def servitor_gather(request_id, item, amount, zone_min, zone_max, destination, origin, dimension="overworld", access="surface"):
    """Submit a bounded gather mission. Reuse request_id for retries of the exact same request."""
    try:
        _dimension(dimension)
        if access not in ("surface","excavate"): raise ValueError("Use surface or excavate access")
        if not isinstance(request_id, str) or not re.fullmatch(r"[A-Za-z0-9_-]{1,64}", request_id):
            raise ValueError("request_id must have 1..64 letters, digits, underscores or hyphens")
        if not isinstance(item, str) or not re.fullmatch(r"(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+", item):
            raise ValueError("Invalid resource identifier")
        if type(amount) is not int or not 1 <= amount <= 4096:
            raise ValueError("amount must be an integer in 1..4096")
        a, b = _position(zone_min), _position(zone_max)
        mn = [min(x,y) for x,y in zip(a,b)]; mx = [max(x,y) for x,y in zip(a,b)]
        volume = 1
        for lo, hi in zip(mn, mx): volume *= hi - lo + 1
        if volume > 32768: raise ValueError("Zone exceeds 32768 blocks")
        target, start = _position(destination), _position(origin)
        for point in (target, [target[0], target[1]-1, target[2]]):
            if all(lo <= n <= hi for lo,n,hi in zip(mn,point,mx)):
                raise ValueError("Destination and support must be outside mining zone")
        payload = {"schema":1,"type":"GATHER","request_id":request_id,"item":item if ":" in item else "minecraft:"+item,
                   "amount":amount,"min":mn,"max":mx,"destination":target,"origin":start,"access":access}
    except (ValueError, TypeError) as error:
        return {"ok":False,"error":"INVALID_ARGUMENT","detail":str(error)}
    encoded = base64.urlsafe_b64encode(json.dumps(payload,separators=(",", ":")).encode()).decode().rstrip("=")
    return _mission_call("submit " + encoded, dimension)


def _build_request(request_id, blueprint, material, anchor, source, origin):
    if not isinstance(request_id,str) or not re.fullmatch(r"[A-Za-z0-9_-]{1,64}",request_id): raise ValueError("Invalid request id")
    if blueprint not in ("platform_3x3_v1","shelter_5x5_v1"): raise ValueError("Unsupported blueprint")
    if material not in ("minecraft:cobblestone","minecraft:stone","minecraft:oak_planks","minecraft:spruce_planks"): raise ValueError("Unsupported building material")
    return {"schema":1,"type":"BUILD","request_id":request_id,"blueprint":blueprint,"material":material,
            "anchor":_position(anchor),"source":_position(source),"origin":_position(origin)}


def _build_call(operation,request_id,blueprint,material,anchor,source,origin,dimension):
    try:
        _dimension(dimension)
        payload=_build_request(request_id,blueprint,material,anchor,source,origin)
    except (ValueError,TypeError) as error: return {"ok":False,"error":"INVALID_ARGUMENT","detail":str(error)}
    encoded=base64.urlsafe_b64encode(json.dumps(payload,separators=(",",":")).encode()).decode().rstrip("=")
    return _mission_call(operation+" "+encoded,dimension)


def servitor_build_estimate(blueprint,material,anchor,source,origin,dimension="overworld"):
    """Read-only estimate and site validation, without reserving items or creating a mission."""
    return _build_call("estimate","estimate",blueprint,material,anchor,source,origin,dimension)


def servitor_build(request_id,blueprint,material,anchor,source,origin,dimension="overworld"):
    """Reserve real materials from source, physically build the versioned plan and verify it."""
    return _build_call("submit",request_id,blueprint,material,anchor,source,origin,dimension)


def servitor_mission_status(mission_id="current", dimension="overworld"):
    if mission_id != "current":
        try:
            if str(uuid.UUID(mission_id)) != mission_id: raise ValueError()
        except (ValueError, TypeError, AttributeError):
            return {"ok":False,"error":"INVALID_ARGUMENT","detail":"Invalid mission UUID"}
    return _mission_call("status " + mission_id, dimension)


def servitor_mission_control(mission_id, action, dimension="overworld"):
    try:
        if str(uuid.UUID(mission_id)) != mission_id: raise ValueError()
    except (ValueError, TypeError, AttributeError):
        return {"ok":False,"error":"INVALID_ARGUMENT","detail":"Invalid mission UUID"}
    if action not in ("pause", "resume", "cancel", "deposit", "rollback", "close"):
        return {"ok":False,"error":"INVALID_ARGUMENT","detail":"Use pause, resume, cancel, deposit, rollback or close"}
    return _mission_call("control " + mission_id + " " + action, dimension)


TOOLS_MAP.update({"servitor_capabilities":servitor_capabilities,"servitor_gather":servitor_gather,
                  "servitor_build_estimate":servitor_build_estimate,"servitor_build":servitor_build,
                  "servitor_mission_status":servitor_mission_status,"servitor_mission_control":servitor_mission_control})
