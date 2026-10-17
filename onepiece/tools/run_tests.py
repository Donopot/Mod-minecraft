#!/usr/bin/env python3
"""CORE-030 — recette de tests automatisés de la couche Fruits du Démon.

Enchaîne un vrai cycle serveur : démarrage dédié → `/dftest all` → écriture d'état →
arrêt → redémarrage → vérification de la persistance → contrôle du loot, puis produit un
rapport PASS/FAIL dans docs/CORE-030-rapport-tests.md.

Aucun client Minecraft n'est requis : les scénarios joueur utilisent des FakePlayer Fabric
côté serveur et les commandes passent par RCON.

Usage :  python3 tools/run_tests.py
"""
import json
import os
import re
import shutil
import socket
import struct
import subprocess
import sys
import time

PROJECT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SERVER = "/opt/data/workspace/mc-server"
JAR = os.path.join(PROJECT, "build/libs/opworldlike-1.0.0.jar")
LOG = os.path.join(SERVER, "logs/latest.log")
REPORT = os.path.join(PROJECT, "docs/CORE-030-rapport-tests.md")
RESTORE = "/opt/data/workspace/.mc-server-restore"

RCON_HOST, RCON_PORT = "127.0.0.1", 25575
RCON_PASS = "umtest-rcon-2026"
JAVA = "/opt/data/jdk/jdk-25.0.4.1+1/bin/java"

evidence = []          # (phase, scope, ok, id, detail)
rcon_results = []      # (label, ok, detail)


# --------------------------------------------------------------------- RCON client
class Rcon:
    def __init__(self, host, port, password, timeout=20):
        self.sock = socket.create_connection((host, port), timeout=timeout)
        self.sock.settimeout(timeout)
        self.rid = 0
        self._send(3, password)
        resp = self._recv()
        if not resp or resp[0] == -1:
            raise RuntimeError("authentification RCON refusée")

    def _send(self, kind, body):
        self.rid += 1
        payload = struct.pack("<ii", self.rid, kind) + body.encode("utf-8") + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(payload)) + payload)

    def _recv(self):
        head = self._read_exact(4)
        if not head:
            return None
        (length,) = struct.unpack("<i", head)
        data = self._read_exact(length)
        rid, kind = struct.unpack("<ii", data[:8])
        return rid, kind, data[8:-2].decode("utf-8", "replace")

    def _read_exact(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                break
            buf += chunk
        return buf

    def cmd(self, command, settle=1.5):
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


def start_server(tag):
    env = dict(os.environ, JAVA_HOME="/opt/data/jdk/jdk-25.0.4.1+1",
               PATH="/opt/data/jdk/jdk-25.0.4.1+1/bin:" + os.environ["PATH"])
    if os.path.exists(LOG):
        os.remove(LOG)
    proc = subprocess.Popen(
        [JAVA, "-Xmx2G", "-jar", "fabric-server-launch.jar", "nogui"],
        cwd=SERVER, env=env, stdout=open(f"/tmp/mc-{tag}.log", "w"), stderr=subprocess.STDOUT)
    deadline = time.time() + 180
    while time.time() < deadline:
        if os.path.exists(LOG) and "Done (" in open(LOG, encoding="utf-8", errors="replace").read():
            return proc, True
        if proc.poll() is not None:
            return proc, False
        time.sleep(2)
    return proc, False


def read_log():
    if not os.path.exists(LOG):
        return ""
    return open(LOG, encoding="utf-8", errors="replace").read()


_harvested = set()


def harvest(scope):
    """Récupère les lignes [dftest] du log et les classe en PASS/FAIL (sans doublon entre phases)."""
    lines = re.findall(r"\[dftest\] \[(PASS|FAIL)\] (\S+) — (.*)", read_log())
    for status, cid, detail in lines:
        if cid in _harvested:
            continue
        _harvested.add(cid)
        evidence.append((scope, status == "PASS", cid, detail.strip()))
    summary = re.findall(r"\[dftest\] ===== RÉSULTAT \S+ : (\d+)/(\d+) PASS, (\d+) FAIL", read_log())
    return summary[-1] if summary else None


def stop_server(rcon):
    try:
        rcon.cmd("stop")
    except Exception:  # noqa: BLE001
        pass
    deadline = time.time() + 60
    while time.time() < deadline:
        if not port_open(RCON_HOST, RCON_PORT):
            return True
        time.sleep(2)
    return not port_open(RCON_HOST, RCON_PORT)


def main():
    # ---------------------------------------------------------- pré-vol
    print("== pré-vol ==")
    if port_open(RCON_HOST, RCON_PORT):
        try:
            Rcon(RCON_HOST, RCON_PORT, RCON_PASS).cmd("stop")
            time.sleep(10)
        except Exception:  # noqa: BLE001
            pass
    if not os.path.exists(JAR):
        print("jar absent : lancer ./gradlew jar d'abord")
        return 1

    # sauvegarde de l'état existant du serveur de test (non destructif)
    if os.path.exists(RESTORE):
        shutil.rmtree(RESTORE)
    os.makedirs(RESTORE)
    for rel in ("mods", "config/opworldlike"):
        src = os.path.join(SERVER, rel)
        if os.path.exists(src):
            dst = os.path.join(RESTORE, rel.replace("/", "__"))
            shutil.copytree(src, dst)
    print(f"  sauvegarde de l'état existant -> {RESTORE}")

    # état vierge pour un « démarrage dédié » honnête
    shutil.copy2(JAR, os.path.join(SERVER, "mods/opworldlike-1.0.0.jar"))
    state = os.path.join(SERVER, "config/opworldlike")
    if os.path.exists(state):
        shutil.rmtree(state)
    os.makedirs(state, exist_ok=True)
    # chance forcée à 100 % : rend le tirage de loot déterministe (sinon 2 % par coffre rendrait
    # le contrôle non concluant — le comportement par défaut reste 2 % dans le mod).
    open(os.path.join(state, "devilfruit-loot.toml"), "w", encoding="utf-8").write(
        "# CORE-030 : chance forcée à 100 % pour un tirage déterministe en test.\n"
        "enabled = true\nchest-drop-chance = 1.0\nunique = true\nreincarnation-on-death = true\n"
        "weight-legendary = 6\nweight-rare = 25\nweight-common = 60\n")
    print("  jar installé, état des fruits réinitialisé, loot de test à 100 %")

    proc = rcon = None
    try:
        # ------------------------------------------------------- 1. démarrage dédié
        print("\n== 1. démarrage dédié ==")
        proc, ok = start_server("boot1")
        log = read_log()
        selftest = re.search(r"Devil Fruit power layer: ([^\n]+)", log)
        errors = len(re.findall(r"\b(ERROR|FATAL|Exception)\b", log))
        evidence.append(("demarrage", ok, "S0-demarrage", f"serveur prêt={ok}, catalogue={selftest.group(1) if selftest else 'absent'}"))
        evidence.append(("demarrage", errors == 0, "S1-sans-erreur", f"{errors} ERROR/FATAL/Exception au démarrage"))
        print(f"  prêt={ok} | {selftest.group(1) if selftest else '—'} | erreurs={errors}")
        if not ok:
            raise RuntimeError("le serveur n'a pas démarré")

        rcon = Rcon(RCON_HOST, RCON_PORT, RCON_PASS)

        # ------------------------------------------------------- 2. tests in-process
        print("\n== 2. dftest all ==")
        print("  réponse:", rcon.cmd("dftest all", settle=20)[:200])
        time.sleep(3)
        print("  résumé:", harvest("all"))

        # ------------------------------------------------------- 3. écriture d'état
        print("\n== 3. dftest persistwrite ==")
        print("  réponse:", rcon.cmd("dftest persistwrite", settle=5)[:200])
        time.sleep(2)
        print("  résumé:", harvest("persistwrite"))

        # ------------------------------------------------------- 4. arrêt
        print("\n== 3 bis. dftest ope_expiry (A6e — échéance réelle de la ROOM et de la section) ==")
        # Une invocation de test tient dans UN SEUL tick : elle ne peut pas observer une durée. La
        # portée « ope » (dans le dftest all de la phase 2) a armé une ROOM et une section à durée
        # COURTE (70 ticks, écrites en config) ; on les relit ICI, plus de 70 ticks SERVEUR plus tard,
        # et AVANT tout redémarrage — l'état est en mémoire, il ne traverse pas un reboot. La ROOM doit
        # être fermée par « echeance » et la force de frappe de la victime doit être revenue d'elle-même :
        # c'est la seule preuve que le maintien est BORNÉ et auto-réparateur.
        print("  réponse:", rcon.cmd("dftest ope_expiry", settle=6)[:200])
        print("  résumé:", harvest("ope_expiry"))

        print("\n== 3 ter. dftest pika_impact (A7c/A7e — impact réel d'un rayon et volée bornée) ==")
        # Même raison que pour l'échéance d'Ope : le VOL d'un rayon demande du TEMPS, et une invocation
        # de test tient dans un seul tick. La portée « pika » (fin du dftest all) a tiré deux rayons —
        # un vers une cible en air libre, un vers une cible derrière un mur — et armé une charge. On
        # relit ici, avant tout redémarrage : la cible libre doit avoir été touchée, la cible abritée
        # NON (le rayon s'arrête à l'obstacle), et la volée doit avoir été tirée avec un nombre
        # d'impacts borné.
        print("  réponse:", rcon.cmd("dftest pika_impact", settle=6)[:200])
        print("  résumé:", harvest("pika_impact"))
        print("\n== 3 quater. dftest yami_impact (A8b/A8d — traction bornée et restitution du scellement) ==")
        # Mêmes raisons : la traction doit s'être ARRÊTÉE toute seule (état borné, tick réel du service)
        # et le scellement doit avoir EXPIRÉ (la cible retrouve ses capacités). Les deux se constatent à
        # un tick ultérieur, avant tout redémarrage.
        print("  réponse:", rcon.cmd("dftest yami_impact", settle=6)[:200])
        print("  résumé:", harvest("yami_impact"))

        print("\n== 3 quinquies. dftest nikyu_impact (A12d — l'onde frappe APRÈS l'annonce) ==")
        # Même raison que pour Pika et Yami : l'ONDRE d'Ursus Shock annonce sa zone pendant 12 ticks
        # avant de frapper, et une invocation de test tient dans un seul tick. On relit donc ici :
        # l'annonce faite pendant le dftest all doit avoir été suivie de l'impact.
        print("  réponse:", rcon.cmd("dftest nikyu_impact", settle=6)[:200])
        print("  résumé:", harvest("nikyu_impact"))

        print("\n== 4. arrêt du serveur ==")
        stopped = stop_server(rcon)
        print("  arrêté:", stopped)
        rcon = proc = None

        # ------------------------------------------------------- 5. redémarrage
        print("\n== 5. redémarrage ==")
        proc, ok = start_server("boot2")
        datsize = os.path.getsize(os.path.join(SERVER, "config/opworldlike/devilfruit-players.dat")) \
            if os.path.exists(os.path.join(SERVER, "config/opworldlike/devilfruit-players.dat")) else 0
        print(f"  prêt={ok} | devilfruit-players.dat={datsize} octets")
        evidence.append(("redemarrage", ok, "S2-redemarrage", f"2e démarrage réussi={ok}, .dat={datsize} octets"))
        if not ok:
            raise RuntimeError("le serveur n'a pas redémarré")

        rcon = Rcon(RCON_HOST, RCON_PORT, RCON_PASS)

        # ------------------------------------------------------- 6. persistance après restart
        print("\n== 6. dftest persistverify ==")
        print("  réponse:", rcon.cmd("dftest persistverify", settle=5)[:200])
        time.sleep(2)
        print("  résumé:", harvest("persistverify"))

        # ------------------------------------------------------- 7. loot (CORE-023)
        print("\n== 6 bis. dftest mochi_expiry (A3f — expiration par le temps) ==")
        # Preuve qu'un bloc temporaire EXPIRE vraiment : il a été posé lors de la phase 2 avec 60 ticks
        # de durée de vie, et il est relu ICI, après plusieurs phases réelles et un REDÉMARRAGE. Une
        # invocation de test tient dans un seul tick : elle ne peut pas observer une durée. Seule une
        # relecture plus tard, à une position fixe, le peut — et le fait que le service recharge son
        # état au démarrage rend la preuve plus forte : le bloc a survécu au redémarrage PUIS expiré.
        print("  réponse:", rcon.cmd("dftest mochi_expiry", settle=6)[:200])
        print("  résumé:", harvest("mochi_expiry"))

        print("\n== 6 ter. dftest ope_restart (A6g — après redémarrage : aucun état fantôme) ==")
        # L'échéance TEMPORELLE du fruit est prouvée en 3 bis, AVANT le redémarrage — un état en
        # mémoire ne traverse pas un redémarrage, donc la preuve d'une durée doit se faire dans la même
        # vie du serveur. Ici on vérifie l'inverse et c'est une garantie en soi : après un redémarrage,
        # il ne reste AUCUNE aire ni section fantôme, et la configuration empruntée par le harnais a
        # bien été rendue — deux exécutions successives mesurent donc la même chose.
        print("  réponse:", rcon.cmd("dftest ope_restart", settle=6)[:200])
        print("  résumé:", harvest("ope_restart"))

        print("\n== 7. loot en coffre de structure (CORE-023) ==")
        # Rendre d'abord ce que le harnais a emprunté : la table de loot EXCLUT les Fruits déjà
        # possédés, et les fixtures des sections A1..A10 en détiennent. Sans cette remise à zéro, le
        # coffre se vide et la mesure accuse le loot au lieu du harnais (échec réel de deux exécutions).
        print("  réponse:", rcon.cmd("dftest lootprep", settle=4)[:200])
        print("  résumé:", harvest("lootprep"))
        rcon.cmd("setblock 100 64 100 minecraft:stone replace")
        rcon.cmd("setblock 100 64 100 minecraft:chest")
        rcon.cmd("loot insert 100 64 100 loot minecraft:chests/simple_dungeon")
        data = rcon.cmd("data get block 100 64 100 Items")
        fruits = re.findall(r"minepiece:(item_devil_fruit[a-z_]*)", data)
        ok_loot = len(fruits) > 0
        rcon_results.append(("loot-coffre", ok_loot,
                             f"{len(fruits)} fruit(s) dans le coffre : {sorted(set(fruits))}"))
        print(f"  fruits={sorted(set(fruits))}")

        # ------------------------------------------------------- 8. arrêt final
        print("\n== 8. arrêt final ==")
        stop_server(rcon)
        rcon = proc = None
    finally:
        if rcon is not None:
            try:
                rcon.cmd("stop")
            except Exception:  # noqa: BLE001
                pass
        # restauration de l'état d'origine
        for rel in ("mods", "config/opworldlike"):
            dst = os.path.join(SERVER, rel)
            backup = os.path.join(RESTORE, rel.replace("/", "__"))
            if os.path.exists(dst):
                shutil.rmtree(dst)
            if os.path.exists(backup):
                shutil.copytree(backup, dst)
        shutil.rmtree(RESTORE, ignore_errors=True)
        print(f"\nétat du serveur de test restauré")

    # ---------------------------------------------------------- 9. ressources (CORE-024)
    print("\n== 9. validation des ressources (CORE-024) ==")
    v = subprocess.run([sys.executable, os.path.join(PROJECT, "tools/validate_assets.py")],
                       capture_output=True, text=True)
    res_last = v.stdout.strip().splitlines()[-1]
    rcon_results.append(("ressources-CORE-024", v.returncode == 0, res_last))
    print("  ", res_last)

    # ---------------------------------------------------------- 10. rapport
    total = len(evidence) + len(rcon_results)
    passed = sum(1 for e in evidence if e[1]) + sum(1 for r in rcon_results if r[1])
    failed = total - passed

    lines = ["# CORE-030 — rapport de tests automatisés", "",
             f"- Date : {time.strftime('%Y-%m-%d %H:%M:%S')}",
             f"- Serveur dédié : `{SERVER}` (Minecraft 26.3, Fabric)",
             f"- Jar testé : `{JAR}` ({os.path.getsize(JAR)} octets)",
             f"- Résultat global : **{passed}/{total} PASS**, {failed} FAIL", "",
             "## Résultats", "",
             "| phase | contrôle | statut | détail |", "|---|---|---|---|"]
    for phase, ok, cid, detail in evidence:
        lines.append(f"| {phase} | `{cid}` | {'PASS' if ok else '**FAIL**'} | {detail.replace('|', '/')} |")
    for label, ok, detail in rcon_results:
        lines.append(f"| controle | `{label}` | {'PASS' if ok else '**FAIL**'} | {detail.replace('|', '/')} |")
    lines += ["", "## Périmètre", "",
              "Automatisé (ce rapport) : démarrage, catalogue, immersion des 40 fruits, exécution des 206",
              "techniques, transformation et échelle, changement de fruit, mort/réincarnation, deux joueurs,",
              "persistance après redémarrage, blocs temporaires, invocations, loot en coffre, ressources.", "",
              "Note : le contrôle de loot tourne avec `chest-drop-chance = 1.0` pour être déterministe",
              "(la valeur livrée par défaut reste 0.02). Le reste des tests utilise la configuration livrée.", "",
              "Non automatisable sans client graphique (à faire par un joueur) : rendu visuel des modèles,",
              "keybinds (R/V/B), immersion souris/clavier, ressenti en combat. Voir",
              "`docs/CORE-030-recette-de-tests.md`."]
    os.makedirs(os.path.dirname(REPORT), exist_ok=True)
    open(REPORT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
    print(f"\nrapport écrit : {REPORT}")
    print(f"RÉSULTAT GLOBAL : {passed}/{total} PASS, {failed} FAIL")

    for phase, ok, cid, detail in evidence:
        if not ok:
            print(f"  ÉCHEC {cid}: {detail}")
    for label, ok, detail in rcon_results:
        if not ok:
            print(f"  ÉCHEC {label}: {detail}")
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
