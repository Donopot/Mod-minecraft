#!/usr/bin/env python3
"""Arrête (ou liste) les serveurs Minecraft de test, sans jamais toucher au shell appelant.

Piège que ce script existe pour éviter : `ps | awk '/java -Xmx2G/'` puis `kill` matche la
**propre ligne de commande** de l'appelant (qui contient le texte `java -Xmx2G`) et le tue
lui-même — la commande s'arrête en SIGTERM sans avoir rien fait.

Usage :
    mc_kill_server.py            # liste
    mc_kill_server.py --kill     # arrête (SIGTERM puis SIGKILL si nécessaire)
"""
import os
import signal
import sys
import time

SERVER_MARKERS = ("fabric-server-launch", "minecraft_server", "server.jar")


def ancestors():
    """PIDs de ce processus et de tous ses ancêtres — jamais à tuer."""
    chain = set()
    pid = os.getpid()
    guard = 0
    while pid and pid > 1 and guard < 50:
        chain.add(pid)
        try:
            with open(f"/proc/{pid}/stat", encoding="utf-8", errors="replace") as handle:
                fields = handle.read().rsplit(")", 1)[1].split()
            pid = int(fields[1])
        except (OSError, IndexError, ValueError):
            break
        guard += 1
    return chain


def server_processes():
    protected = ancestors()
    found = []
    for entry in os.listdir("/proc"):
        if not entry.isdigit():
            continue
        pid = int(entry)
        if pid in protected:
            continue
        try:
            with open(f"/proc/{pid}/cmdline", "rb") as handle:
                raw = handle.read().split(b"\0")
        except OSError:
            continue
        args = [a.decode("utf-8", "replace") for a in raw if a]
        if not args:
            continue
        # Le binaire doit être un java, et la ligne doit désigner un serveur de test.
        if "java" not in os.path.basename(args[0]).lower():
            continue
        joined = " ".join(args)
        if any(marker in joined for marker in SERVER_MARKERS):
            found.append((pid, joined[:120]))
    return found


def main():
    do_kill = "--kill" in sys.argv
    procs = server_processes()
    if not procs:
        print("aucun serveur de test actif")
        return 0
    for pid, cmd in procs:
        print(f"  pid={pid} {cmd}")
    if not do_kill:
        print(f"{len(procs)} serveur(s) trouvé(s) — relancer avec --kill pour arrêter")
        return 0
    for pid, _ in procs:
        try:
            os.kill(pid, signal.SIGTERM)
        except OSError as exc:
            print(f"  SIGTERM {pid} : {exc}")
    time.sleep(6)
    remaining = server_processes()
    for pid, _ in remaining:
        try:
            os.kill(pid, signal.SIGKILL)
            print(f"  SIGKILL {pid} (SIGTERM ignoré — le verrou de monde peut subsister)")
        except OSError as exc:
            print(f"  SIGKILL {pid} : {exc}")
    print(f"arrêtés : {len(procs)} (dont {len(remaining)} par SIGKILL)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
