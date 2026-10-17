#!/usr/bin/env python3
"""mcp_vd.py — serveur MCP (stdio) exposant les outils VOID DYNASTY à un client IA.

Chaque outil est **déterministe** et renvoie ce qu'il a observé, jamais une intention.
Aucune commande arbitraire n'est exposée : `command` est limité à une liste blanche.

Lancement manuel :   python3 mcp_vd.py
Exécuter avec le Python de l'environnement MCP et VD_SERVER_DIR pointant vers
le dossier du serveur Minecraft local. Voir docs/SERVITOR-POLYVALENT.md pour
les quatre outils du rôle servitor et les limites de cette première version.

Compatible avec MCP Python SDK 1.30.0 (voir requirements-mcp.txt).
"""
import sys

from mcp.server.fastmcp import FastMCP
from pydantic import StrictInt

import vd_core

server = FastMCP("voiddynasty", log_level="WARNING")


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


def tool_servitor_capabilities(dimension: str = "overworld") -> dict:
    """Return the server's supported mission types, resources and limits before planning work."""
    return vd_core.servitor_capabilities(dimension)


def tool_servitor_gather(request_id: str, item: str, amount: StrictInt, zone_min: list[StrictInt],
                         zone_max: list[StrictInt], destination: list[StrictInt], origin: list[StrictInt],
                         dimension: str = "overworld") -> dict:
    """Have the physical servitor gather a quantity from matching blocks in an explicit zone and deliver it
    to a single chest/barrel. No tunnels/stairs yet. Reuse request_id unchanged after timeout; poll the returned
    mission UUID until SUCCEEDED, INCOMPLETE or PAUSED. Acceptance alone is not success."""
    return vd_core.servitor_gather(request_id,item,amount,zone_min,zone_max,destination,origin,dimension)


def tool_servitor_mission_status(mission_id: str = "current", dimension: str = "overworld") -> dict:
    """Read persistent mission state, actual collected/delivered counters and reason for any pause."""
    return vd_core.servitor_mission_status(mission_id,dimension)


def tool_servitor_mission_control(mission_id: str, action: str, dimension: str = "overworld") -> dict:
    """Pause, resume, cancel, deposit, rollback or close the specified mission. Cancel keeps resources and receipts.
    After cancellation, deposit recovers carried resources in the worker depot. Rollback stops work and recovers drops before restoring blocks; poll rollback_state until COMPLETE or FAILED. Close requires a terminal mission and empty carry; it explicitly releases rollback history."""
    return vd_core.servitor_mission_control(mission_id,action,dimension)


for fn, nom in ((tool_server_status, "server_status"), (tool_vdyntest, "vdyntest"),
                (tool_scan_zone, "scan_zone"), (tool_place_ship, "place_ship"),
                (tool_fleet, "fleet"), (tool_command, "command"),
                (tool_servitor_capabilities, "servitor_capabilities"), (tool_servitor_gather, "servitor_gather"),
                (tool_servitor_mission_status, "servitor_mission_status"), (tool_servitor_mission_control, "servitor_mission_control")):
    server.add_tool(fn, name=nom)


if __name__ == "__main__":
    server.run("stdio")
