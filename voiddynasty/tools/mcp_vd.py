#!/usr/bin/env python3
"""mcp_vd.py — serveur MCP (stdio) exposant les outils VOID DYNASTY à un client IA.

Chaque outil est **déterministe** et renvoie ce qu'il a observé, jamais une intention.
Aucune commande arbitraire n'est exposée : `command` est limité à une liste blanche.

Lancement manuel :   python3 mcp_vd.py
Entrée de configuration Hermes (config.yaml) :

    mcp_servers:
      voiddynasty:
        command: "/opt/hermes/.venv/bin/python3"
        args: ["/opt/data/workspace/an-Ee6I54/r/voiddynasty/tools/mcp_vd.py"]
        timeout: 300

Écrit contre MCP Python SDK 2.0 (`mcp.server.MCPServer` + `add_tool`).
"""
import sys

from mcp.server import MCPServer

import vd_core

server = MCPServer("voiddynasty")


def tool_server_status() -> dict:
    """État du serveur Minecraft : joignable ou non, joueurs connectés, monde surveillé."""
    return vd_core.server_status()


def tool_vdyntest(scope: str = "all") -> dict:
    """Lance le harnais du mod (/vdyntest <scope>) et renvoie le compte PASS/FAIL lu dans la sortie."""
    return vd_core.vdyntest(scope)


def tool_scan_zone(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, dimension: str = "overworld") -> dict:
    """Scan en LECTURE SEULE d'une zone : nombre de blocs, dangers (lave, eau, gravité, butin,
    spawners, machines du mod), blocs les plus fréquents. Ne modifie rien."""
    return vd_core.scan_zone(x1, y1, z1, x2, y2, z2, dimension)


def tool_place_ship(ship_type: str, x: int, y: int, z: int) -> dict:
    """Pose un vaisseau (emperor, frigate, ruin, titan, andromeda, executor) et renvoie la
    relecture immédiate faite par le mod : blocs identiques, vides, autres."""
    return vd_core.place_ship(ship_type, x, y, z)


def tool_fleet(action: str, player: str, name: str = "", extra: str = "") -> dict:
    """Commande /fleet exécutée en tant que joueur : list, add, deploy, stow, teleport, rename, delete.
    `name` = type (pour add) ou nom du vaisseau, `extra` = arguments supplémentaires."""
    return vd_core.fleet(action, player, name or None, extra or None)


def tool_command(cmd: str) -> dict:
    """Commande serveur limitée à une liste blanche (fleet, vdyntest, list, save-all, say, time,
    weather, forceload). Refusée sinon."""
    return vd_core.command(cmd)


for fn, nom in ((tool_server_status, "server_status"), (tool_vdyntest, "vdyntest"),
                (tool_scan_zone, "scan_zone"), (tool_place_ship, "place_ship"),
                (tool_fleet, "fleet"), (tool_command, "command")):
    server.add_tool(fn, name=nom)


if __name__ == "__main__":
    server.run("stdio")
