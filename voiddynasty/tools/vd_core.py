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
import time

MC_DIR = os.environ.get("VD_SERVER_DIR", "/opt/data/workspace/mc-server")
PROPS = os.path.join(MC_DIR, "server.properties")
WORLD = os.environ.get("VD_WORLD_DIR", os.path.join(MC_DIR, "world8"))
TOOLS = os.path.dirname(os.path.abspath(__file__))
# Serveur distant : on garde 127.0.0.1 par défaut, donc rien ne change pour le banc local.
RCON_HOST = os.environ.get("VD_RCON_HOST", "127.0.0.1")

# commandes acceptées par l'outil générique : volontairement étroit.
ALLOWED = ("fleet ", "vdyntest ", "work ", "list", "save-all", "say ", "time ", "weather ", "forceload ")


def _props():
    """Mot de passe et port RCON, avec surcharge par l'environnement.

    Par défaut on lit le server.properties local (banc d'essai). Pour piloter un
    serveur distant — typiquement via un tunnel SSH — on fournit le secret par
    VD_RCON_PASSWORD_FILE (chemin d'un fichier 600) ou VD_RCON_PASSWORD, et le
    port par VD_RCON_PORT. Le mot de passe n'a alors jamais besoin d'être copié
    dans un fichier de configuration versionné.
    """
    pw = os.environ.get("VD_RCON_PASSWORD")
    port = os.environ.get("VD_RCON_PORT")
    secret = os.environ.get("VD_RCON_PASSWORD_FILE")
    if not pw and secret:
        with open(secret, encoding="utf-8", errors="replace") as stream:
            pw = stream.read().strip()
    if not pw or not port:
        with open(PROPS, encoding="utf-8", errors="replace") as stream:
            txt = stream.read()
        if not pw:
            m_pw = re.search(r"^rcon\.password=(.*)$", txt, re.M)
            pw = m_pw.group(1) if m_pw else None
        if not port:
            m_port = re.search(r"^rcon\.port=(\d+)$", txt, re.M)
            port = m_port.group(1) if m_port else None
    if not pw or not port:
        raise RuntimeError("rcon.password ou rcon.port absent de %s (et absent de l'environnement)" % PROPS)
    return pw, int(port)


class Rcon:
    """Client RCON minimal, patient (les poses de vaisseaux géants dépassent 4 s)."""

    def __init__(self, timeout=600):
        pw, port = _props()
        self.timeout=timeout
        self.s = socket.create_connection((RCON_HOST, port), min(15,timeout))
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
        command_id=self.rid
        # Vanilla RCON splits long replies into several packets with the same ID.
        # A subsequent read-only command marks their end without guessing packet length.
        # Send the marker only after the first reply. Some vanilla RCON versions
        # consume one request per socket read and can drop a pipelined second frame.
        sentinel_id=None;parts=[];size=0;deadline=time.monotonic()+self.timeout
        while True:
            remaining=deadline-time.monotonic()
            if remaining<=0:raise RuntimeError("delai total RCON depasse")
            self.s.settimeout(remaining)
            rid, kind, text = self._recv()
            if rid==sentinel_id and kind==0:return ''.join(parts)
            if rid==command_id and kind==0:
                size+=len(text.encode('utf-8'))
                if size>1024*1024:raise RuntimeError("reponse RCON trop grande")
                parts.append(text)
                if sentinel_id is None:
                    self._send(2,"list")
                    sentinel_id=self.rid

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


def _mission_call(tail, dimension, timeout=30):
    try:
        dim = _dimension(dimension)
    except ValueError as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    try:
        reply = rcon("execute in " + dim + " run work mission " + tail, timeout=timeout)[0]
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


def _observation(kind, zone_min, zone_max, limit, dimension, item=None):
    try:
        _dimension(dimension)
        a,b=_position(zone_min),_position(zone_max)
        low=[min(x,y) for x,y in zip(a,b)];high=[max(x,y) for x,y in zip(a,b)]
        volume=1
        for lo,hi in zip(low,high):volume*=hi-lo+1
        if volume>32768:raise ValueError("Observation exceeds 32768 cells")
        if type(limit) is not int or not 1<=limit<=64:raise ValueError("limit must be an integer in 1..64")
        payload={"schema":1,"type":kind,"min":low,"max":high,"limit":limit}
        if kind=="FIND":
            if not isinstance(item,str) or not re.fullmatch(r"(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+",item):raise ValueError("Invalid resource identifier")
            payload['item']=item if ':' in item else 'minecraft:'+item
    except (ValueError,TypeError) as error:return {"ok":False,"error":"INVALID_ARGUMENT","detail":str(error)}
    encoded=base64.urlsafe_b64encode(json.dumps(payload,separators=(',',':')).encode()).decode().rstrip('=')
    return _mission_call('observe '+encoded,dimension)


def servitor_observe(zone_min,zone_max,dimension="overworld",limit=16):
    """Live read-only coverage and hazards; unknown/protected cells are never treated as air."""
    return _observation('OBSERVE',zone_min,zone_max,limit,dimension)


def servitor_find(item,zone_min,zone_max,dimension="overworld",limit=16):
    """Observe source blocks for a supported gather item; counts blocks, never promises item drops."""
    return _observation('FIND',zone_min,zone_max,limit,dimension,item)


def _view_call(kind, dimension="overworld", radius=None, timeout=30):
    """Perception du compagnon : etat, joueurs connectes, environnement immediat.

    Le mod repond sur une seule ligne prefixee par VDVIEW, comme pour les missions.
    """
    try:
        dim = _dimension(dimension)
    except ValueError as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    tail = "view " + kind
    if radius is not None:
        if type(radius) is not int or not 2 <= radius <= 24:
            return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "radius must be an integer in 2..24"}
        tail += " " + str(radius)
    try:
        reply = rcon("execute in " + dim + " run servitor " + tail, timeout=timeout)[0]
    except (RuntimeError, OSError) as error:
        # Premier appel apres un demarrage : le serveur accuse le coup et la reponse depasse
        # le delai. On rejoue une fois sur une connexion neuve avant de declarer l'echec.
        if "timed out" in str(error).lower():
            try:
                import time as _time
                _time.sleep(2)
                reply = rcon("execute in " + dim + " run servitor " + tail, timeout=timeout)[0]
            except (RuntimeError, OSError) as second:
                return {"ok": False, "error": "SERVER_UNAVAILABLE", "detail": str(second)}
        else:
            return {"ok": False, "error": "SERVER_UNAVAILABLE", "detail": str(error)}
    marker = "VDVIEW "
    if marker not in reply:
        return {"ok": False, "error": "PROTOCOL_ERROR",
                "detail": "Missing view JSON response; server may need the perception build"}
    # Le mod renvoie "VDVIEW <kind> <json>" : on verifie le kind, sinon une reponse en retard
    # (RCON peut decaler une reponse apres un timeout) serait attribuee au mauvais outil.
    payload = reply.split(marker, 1)[1].lstrip()
    echoed, _, rest = payload.partition(" ")
    if echoed != kind:
        return {"ok": False, "error": "PROTOCOL_ERROR",
                "detail": "Reponse pour '%s' alors que '%s' etait demande (reponse RCON desalignee)" % (echoed, kind)}
    try:
        result, _ = json.JSONDecoder().raw_decode(rest)
        if not isinstance(result, dict) or type(result.get("ok")) is not bool:
            raise ValueError("Invalid response")
        return result
    except (ValueError, TypeError):
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": "Invalid view JSON response"}


def servitor_status(dimension="overworld"):
    """Ou est le compagnon, dans quel etat, et existe-t-il seulement."""
    return _view_call("status", dimension)


def servitor_players(dimension="overworld"):
    """Joueurs connectes : nom, dimension, position, distance au compagnon."""
    return _view_call("players", dimension)


def servitor_work(dimension="overworld"):
    """Etat interne du moteur de chantier : has_depot, depots, compteurs, et last_stop (raison d'arret)."""
    return _view_call("work", dimension)


def servitor_look(radius=8, dimension="overworld"):
    """Environnement immediat du compagnon : blocs, dangers, entites vivantes autour."""
    return _view_call("look", dimension, radius)


async def servitor_mission_wait(mission_id,timeout_seconds=10,dimension="overworld"):
    """Bounded observation of a fixed mission ID. Timeout/cancellation does not stop server work."""
    import asyncio
    if type(timeout_seconds) is not int or not 0<=timeout_seconds<=30:
        return {"ok":False,"error":"INVALID_ARGUMENT","detail":"timeout_seconds must be an integer in 0..30"}
    try:
        if str(uuid.UUID(mission_id))!=mission_id:raise ValueError()
        _dimension(dimension)
    except (ValueError,TypeError,AttributeError):return {"ok":False,"error":"INVALID_ARGUMENT","detail":"A fixed mission UUID and valid dimension are required"}
    started=asyncio.get_running_loop().time();deadline=started+timeout_seconds
    while True:
        status_timeout=min(3.0,max(.25,deadline-asyncio.get_running_loop().time()))
        result=await asyncio.to_thread(_mission_call,"status "+mission_id,dimension,status_timeout)
        if not result.get('ok'):return {**result,'wait_state':'unknown','mission_id':mission_id}
        mission=result.get('mission')
        if not isinstance(mission,dict) or mission.get('id')!=mission_id or not isinstance(mission.get('status'),str):
            return {'ok':False,'error':'PROTOCOL_ERROR','detail':'Missing or mismatched mission identity/status','wait_state':'unknown','mission_id':mission_id}
        if result.get('rollback_state') in ('FAILED','PARTIAL'):state='attention'
        elif result.get('rollback_state')=='RUNNING' or result.get('worker_active') and mission['status'] in ('CANCELLED','INCOMPLETE','SUCCEEDED'):state='pending'
        elif mission['status']=='PAUSED':state='paused'
        elif mission['status'] in ('SUCCEEDED','INCOMPLETE','CANCELLED'):state='settled'
        else:state='pending'
        now=asyncio.get_running_loop().time()
        if state!='pending' or now>=deadline:return {**result,'wait_state':state,'waited_seconds':round(now-started,3)}
        await asyncio.sleep(min(.5,deadline-now))


def _craft_call(tail, dimension="overworld", timeout=60, root="craft"):
    """Appelle les commandes craft/smelt du mod et lit le marqueur VDCRAFT."""
    try:
        dim = _dimension(dimension)
    except ValueError as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    try:
        reply = rcon("execute in " + dim + " run servitor " + root + " " + tail, timeout=timeout)[0]
    except (RuntimeError, OSError) as error:
        return {"ok": False, "error": "SERVER_UNAVAILABLE", "detail": str(error)}
    if "VDCRAFT " not in reply:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": reply[:200]}
    try:
        return json.loads(reply.split("VDCRAFT ", 1)[1].strip())
    except ValueError:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": reply[:200]}


def servitor_recipes(filter="", dimension="overworld"):
    """Livre de recettes du serveur : resultat, ingredients, station (bag = 2x2, table = 3x3).

    Ne rien inventer : cette liste est la seule verite sur ce qui est craftable.
    """
    if filter is None:
        filter = ""
    if not isinstance(filter, str) or not re.fullmatch(r"[A-Za-z0-9_:./-]{0,64}", filter):
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "Invalid filter"}
    return _craft_call("list " + filter if filter else "list", dimension)


def servitor_materials(item, count, chest, radius=16, dimension="overworld"):
    """Plan d'approvisionnement pour fabriquer `count` de `item` depuis le coffre `chest`.

    Rend, dans l'ordre : les etapes de craft (ingredients d'abord), ce qui est deja dans le coffre,
    et pour chaque matiere premiere manquante les blocs sources presents dans un rayon autour du
    compagnon. Aucun bloc n'est casse et rien n'est fabrique : c'est le plan, pas l'action.
    """
    if not isinstance(item, str) or not re.fullmatch(r"(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+", item):
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "Invalid item identifier"}
    if type(count) is not int or not 1 <= count <= 4096:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "count must be an integer in 1..4096"}
    if type(radius) is not int or not 4 <= radius <= 32:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "radius must be an integer in 4..32"}
    try:
        pos = _position(chest)
    except (ValueError, TypeError) as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    return _craft_call('plan "%s" %d %d %d %d %d' % (
        item if ":" in item else "minecraft:" + item, count, pos[0], pos[1], pos[2], radius), dimension)


def _supply_call(tail, dimension="overworld", timeout=60):
    """Appelle les commandes d'approvisionnement autonome et lit le marqueur VDSUPPLY."""
    try:
        dim = _dimension(dimension)
    except ValueError as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    try:
        reply = rcon("execute in " + dim + " run servitor supply " + tail, timeout=timeout)[0]
    except (RuntimeError, OSError) as error:
        return {"ok": False, "error": "SERVER_UNAVAILABLE", "detail": str(error)}
    if "VDSUPPLY " not in reply:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": reply[:200]}
    try:
        return json.loads(reply.split("VDSUPPLY ", 1)[1].strip())
    except ValueError:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": reply[:200]}


def servitor_supply(item, count, chest, dimension="overworld"):
    """UNE demande, et le moteur enchaine seul recolte -> fonte -> craft jusqu'au coffre.

    Rien a orchestrer ensuite : lance-le, puis interroge servitor_supply_status. Le job s'arrete
    seul (DONE / FAILED avec la raison) ou sur servitor_supply_cancel. Garde-fous moteur :
    16 actions, 4 recoltes, 15 minutes.
    """
    if not isinstance(item, str) or not re.fullmatch(r"(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+", item):
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "Invalid item identifier"}
    if type(count) is not int or not 1 <= count <= 1024:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "count must be an integer in 1..1024"}
    try:
        pos = _position(chest)
    except (ValueError, TypeError) as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    return _supply_call('start "%s" %d %d %d %d' % (
        item if ":" in item else "minecraft:" + item, count, pos[0], pos[1], pos[2]), dimension)


def servitor_supply_status(dimension="overworld"):
    """Etat de l'approvisionnement en cours : statut, note, journal des actions, mission de recolte."""
    return _supply_call("status", dimension)


def servitor_supply_cancel(dimension="overworld"):
    """Annule l'approvisionnement en cours (la mission de recolte deja lancee n'est pas annulee)."""
    return _supply_call("cancel", dimension)


def servitor_smelt_recipes(filter="", dimension="overworld"):
    """Recettes de fonte du serveur : entree, sortie, temps de cuisson (200 ticks = 10 s), experience.

    Aucune recette n'est codee en dur : c'est le livre du serveur. Une recette absente ici ne se
    fond pas, meme si l'item existe.
    """
    tail = "list" + (" " + filter.strip() if filter and filter.strip() else "")
    return _craft_call(tail, dimension, root="smelt")


def servitor_smelt_fuels(chest, dimension="overworld"):
    """Carburants presents dans un coffre, avec leur pouvoir calorifique reel (ticks par piece)."""
    try:
        pos = _position(chest)
    except (ValueError, TypeError) as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    return _craft_call("fuels %d %d %d" % (pos[0], pos[1], pos[2]), dimension, root="smelt")


def servitor_smelt(item, count, chest, dimension="overworld"):
    """Fond `count` de `item` dans un four a portee, depuis un coffre.

    Consomme l'entree ET le carburant dans le coffre, remet le resultat dans le meme coffre.
    Erreurs : MISSING_INGREDIENTS, NO_FUEL, NOT_ENOUGH_FUEL, NEEDS_FURNACE, CHEST_FULL, NO_RECIPE.
    """
    if not isinstance(item, str) or not re.fullmatch(r"(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+", item):
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "Invalid item identifier"}
    if type(count) is not int or not 1 <= count <= 4096:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "count must be an integer in 1..4096"}
    try:
        pos = _position(chest)
    except (ValueError, TypeError) as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    return _craft_call('make "%s" %d %d %d %d' % (
        item if ":" in item else "minecraft:" + item, count, pos[0], pos[1], pos[2]), dimension, root="smelt")


def servitor_craft(item, count, chest, dimension="overworld"):
    """Fabrique `count` exemplaires de `item` depuis le contenu du coffre `chest`.

    Les ingredients sont pris dans le coffre et le resultat y est remis. Une recette 3x3 exige
    une table de craft a moins de 5 blocs du compagnon ; une recette 2x2 se fait sans table.
    Le plan de dependance (planches -> batons -> etabli) reste a la charge de l'appelant.
    """
    if not isinstance(item, str) or not re.fullmatch(r"(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+", item):
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "Invalid item identifier"}
    if type(count) is not int or not 1 <= count <= 4096:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "count must be an integer in 1..4096"}
    try:
        pos = _position(chest)
    except (ValueError, TypeError) as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    # Brigadier n'accepte pas ':' hors guillemets : on cite toujours l'identifiant.
    return _craft_call('make "%s" %d %d %d %d' % (item if ":" in item else "minecraft:" + item, count, pos[0], pos[1], pos[2]), dimension)


def servitor_move(mode="status", player="", x=0, y=0, z=0, distance=3, dimension="overworld"):
    """Deplacement du compagnon : rejoindre un joueur, aller a des coordonnees, suivre, ou lire l'etat.

    mode = "come" (rejoindre le joueur), "follow" (le suivre), "goto" (coordonnees), "status".
    Le mod repond VDMOVE <json> ; un deplacement dure plusieurs secondes : relire le status ensuite.
    """
    if mode not in ("come", "follow", "goto", "status"):
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "mode must be come, follow, goto or status"}
    try:
        dim = _dimension(dimension)
    except ValueError as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    if mode == "status":
        tail = "move status"
    elif mode == "goto":
        if not all(type(v) is int for v in (x, y, z)):
            return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "x, y, z must be integers"}
        tail = "move goto %d %d %d" % (x, y, z)
    else:
        if not isinstance(player, str) or not player.strip():
            return {"ok": False, "error": "INVALID_ARGUMENT", "detail": "player name is required"}
        tail = "move " + mode + " " + player.strip()
        if mode == "follow":
            tail += " " + str(int(distance))
    try:
        reply = rcon("execute in " + dim + " run servitor " + tail, timeout=60)[0]
    except (RuntimeError, OSError) as error:
        return {"ok": False, "error": "SERVER_UNAVAILABLE", "detail": str(error)}
    if "VDMOVE " not in reply:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": reply[:200]}
    try:
        return json.loads(reply.split("VDMOVE ", 1)[1].strip())
    except ValueError:
        return {"ok": False, "error": "PROTOCOL_ERROR", "detail": reply[:200]}


def servitor_excavate(request_id, zone_min, zone_max, destination, origin, dimension="overworld", access="excavate"):
    """Vide entierement une zone (mine a ciel ouvert, tranchee, cavite) et depose les objets au coffre.

    Pas d'item ni d'amount : le but est la zone videe. Volume maximum 32768 blocs, donc une
    grande mine se decoupe en tranches successives de 8192 blocs ou moins.
    """
    try:
        _dimension(dimension)
        if access not in ("surface", "excavate"):
            raise ValueError("Use surface or excavate access")
        if not isinstance(request_id, str) or not re.fullmatch(r"[A-Za-z0-9_-]{1,64}", request_id):
            raise ValueError("request_id must have 1..64 letters, digits, underscores or hyphens")
        a, b = _position(zone_min), _position(zone_max)
        mn = [min(x, y) for x, y in zip(a, b)]
        mx = [max(x, y) for x, y in zip(a, b)]
        volume = 1
        for lo, hi in zip(mn, mx):
            volume *= hi - lo + 1
        if volume > 32768:
            raise ValueError("Zone exceeds 32768 blocks: decoupe la mine en tranches")
        target, start = _position(destination), _position(origin)
        for point in (target, [target[0], target[1] - 1, target[2]]):
            if all(lo <= n <= hi for lo, n, hi in zip(mn, point, mx)):
                raise ValueError("Destination and support must be outside the excavation zone")
        payload = {"schema": 1, "type": "EXCAVATE", "request_id": request_id, "min": mn, "max": mx,
                   "destination": target, "origin": start, "access": access}
    except (ValueError, TypeError) as error:
        return {"ok": False, "error": "INVALID_ARGUMENT", "detail": str(error)}
    encoded = base64.urlsafe_b64encode(json.dumps(payload, separators=(",", ":")).encode()).decode().rstrip("=")
    return _mission_call("submit " + encoded, dimension)


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
                  "servitor_observe":servitor_observe,"servitor_find":servitor_find,
                  "servitor_build_estimate":servitor_build_estimate,"servitor_build":servitor_build,
                  "servitor_mission_status":servitor_mission_status,"servitor_mission_control":servitor_mission_control})
