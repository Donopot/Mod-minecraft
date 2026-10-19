#!/usr/bin/env python3
"""mcp_vd.py — serveur MCP (stdio) exposant les outils VOID DYNASTY à un client IA.

Chaque outil est **déterministe** et renvoie ce qu'il a observé, jamais une intention.
Aucune commande arbitraire n'est exposée : `command` est limité à une liste blanche.

Lancement manuel :   python3 mcp_vd.py
Exécuter avec le Python de l'environnement MCP et VD_SERVER_DIR pointant vers
le dossier du serveur Minecraft local. Voir docs/SERVITOR-POLYVALENT.md pour
les neuf outils restreints du rôle servitor et docs/SERVITOR-IA-ALPHA6.md pour la perception.

Compatible avec MCP Python SDK 1.30.0 (voir requirements-mcp.txt).
"""
import sys

from mcp.server.fastmcp import FastMCP
from pydantic import StrictFloat, StrictInt
from typing import List

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


def tool_servitor_observe(zone_min: list[StrictInt],zone_max: list[StrictInt],dimension: str="overworld",limit: StrictInt=16) -> dict:
    """Observe live blocks and hazards in a bounded loaded area, without editing or loading chunks.
    Incomplete coverage is unknown terrain, not empty or safe terrain. No container contents are read."""
    return vd_core.servitor_observe(zone_min,zone_max,dimension,limit)


def tool_servitor_find(item: str,zone_min: list[StrictInt],zone_max: list[StrictInt],dimension: str="overworld",limit: StrictInt=16) -> dict:
    """Find source blocks for a supported gather item in an authorized box. Positions are sampled facts,
    not a route or a promise of reachable drops. Check coverage before concluding there are no resources."""
    return vd_core.servitor_find(item,zone_min,zone_max,dimension,limit)


async def tool_servitor_mission_wait(mission_id: str,timeout_seconds: StrictInt=10,dimension: str="overworld") -> dict:
    """Wait up to 30 seconds for this exact mission UUID. Pending means work continues; retry observation
    rather than duplicating a submission. Paused/attention require a decision; acceptance is never completion."""
    return await vd_core.servitor_mission_wait(mission_id,timeout_seconds,dimension)


def tool_servitor_move(mode: str = "status", player: str = "", x: StrictInt = 0, y: StrictInt = 0,
                       z: StrictInt = 0, distance: StrictFloat = 3.0, dimension: str = "overworld") -> dict:
    """Deplacer le compagnon : mode "come" (rejoindre le joueur), "follow" (le suivre),
    "goto" (coordonnees x,y,z) ou "status" (ou il en est). Un deplacement dure plusieurs
    secondes : rappeler le status pour savoir s'il est arrive avant d'enchainer une mission.
    """
    return vd_core.servitor_move(mode=mode, player=player, x=x, y=y, z=z, distance=distance, dimension=dimension)


def tool_servitor_excavate(request_id: str, zone_min: List[StrictInt], zone_max: List[StrictInt],
                           destination: List[StrictInt], origin: List[StrictInt],
                           access: str = "excavate", dimension: str = "overworld") -> dict:
    """Vider entierement une zone (mine a ciel ouvert, tranchee, cavite) et deposer les objets au coffre.
    Pas d'item ni d'amount : le but est la zone videe. Volume max 32768 blocs, donc une grande
    mine se decoupe en tranches successives ; relire servitor_mission_status entre chaque tranche.
    """
    return vd_core.servitor_excavate(request_id=request_id, zone_min=zone_min, zone_max=zone_max,
                                     destination=destination, origin=origin, access=access, dimension=dimension)


def tool_servitor_gather(request_id: str, item: str, amount: StrictInt, zone_min: list[StrictInt],
                         zone_max: list[StrictInt], destination: list[StrictInt], origin: list[StrictInt],
                         dimension: str = "overworld", access: str = "surface") -> dict:
    """Have the physical servitor gather a quantity from matching blocks in an explicit zone and deliver it
    to a single chest/barrel. access=excavate opts into up to 256 horizontal access blocks inside the zone;
    existing floors support excavated 3-block landings with one-block steps (up to 8 blocks of rise).
    Reuse request_id unchanged after timeout; poll the returned
    mission UUID until SUCCEEDED, INCOMPLETE or PAUSED. Acceptance alone is not success."""
    return vd_core.servitor_gather(request_id,item,amount,zone_min,zone_max,destination,origin,dimension,access)


def tool_servitor_build_estimate(blueprint: str, material: str, anchor: list[StrictInt], source: list[StrictInt], origin: list[StrictInt], dimension: str="overworld") -> dict:
    """Read-only estimate: required real materials and site problems for a supported versioned blueprint."""
    return vd_core.servitor_build_estimate(blueprint,material,anchor,source,origin,dimension)


def tool_servitor_build(request_id: str, blueprint: str, material: str, anchor: list[StrictInt], source: list[StrictInt], origin: list[StrictInt], dimension: str="overworld") -> dict:
    """Reserve all real plain materials from a single source chest/barrel, build the blueprint physically,
    then verify every block. Source must be outside the site. Cancellation retains unspent carry;
    rollback removes unchanged placed blocks and refunds their exact costs. Never close before rollback review."""
    return vd_core.servitor_build(request_id,blueprint,material,anchor,source,origin,dimension)


def tool_servitor_mission_status(mission_id: str = "current", dimension: str = "overworld") -> dict:
    """Read persistent mission state, actual collected/delivered counters and reason for any pause."""
    return vd_core.servitor_mission_status(mission_id,dimension)


def tool_servitor_mission_control(mission_id: str, action: str, dimension: str = "overworld") -> dict:
    """Pause, resume, cancel, deposit, rollback or close the specified mission. Cancel keeps resources and receipts.
    After cancellation, deposit recovers carried resources in the worker depot. Rollback stops work and recovers drops before restoring blocks; poll rollback_state until COMPLETE or FAILED. Close requires a terminal mission and empty carry; it explicitly releases rollback history."""
    return vd_core.servitor_mission_control(mission_id,action,dimension)


def tool_servitor_status(dimension: str = "overworld") -> dict:
    """Where the companion is, whether it exists at all, its health, ground and inventory state.
    Call this first when a mission seems to target the wrong place: it tells you if the servitor is
    present in this dimension and where, instead of guessing from unloaded observations."""
    return vd_core.servitor_status(dimension)


def tool_servitor_players(dimension: str = "overworld") -> dict:
    """Online players: name, dimension, position, health, game mode, op flag and distance to the companion.
    Use it to work near a player or to report who is where. Distance is null when the companion is in
    another dimension."""
    return vd_core.servitor_players(dimension)


def tool_servitor_work(dimension: str = "overworld") -> dict:
    """Internal job state: whether a depot exists, depot count, done/total counters, and last_stop —
    the engine's own reason for waiting. Use it when a mission stays PREPARING or PAUSED."""
    return vd_core.servitor_work(dimension)


def tool_servitor_look(radius: StrictInt = 8, dimension: str = "overworld") -> dict:
    """The companion's immediate surroundings: block histogram, hazards, and every living entity within
    radius (2..24) blocks with type, position, distance, hostility and health. Read-only."""
    return vd_core.servitor_look(radius, dimension)


for fn, nom in ((tool_server_status, "server_status"), (tool_vdyntest, "vdyntest"),
                (tool_scan_zone, "scan_zone"), (tool_place_ship, "place_ship"),
                (tool_fleet, "fleet"), (tool_command, "command"),
                (tool_servitor_capabilities, "servitor_capabilities"), (tool_servitor_gather, "servitor_gather"),
                (tool_servitor_observe,"servitor_observe"),(tool_servitor_find,"servitor_find"),(tool_servitor_mission_wait,"servitor_mission_wait"),
                (tool_servitor_build_estimate,"servitor_build_estimate"),(tool_servitor_build,"servitor_build"),
                (tool_servitor_status, "servitor_status"), (tool_servitor_players, "servitor_players"),
                (tool_servitor_look, "servitor_look"), (tool_servitor_work, "servitor_work"),
                (tool_servitor_move, "servitor_move"), (tool_servitor_excavate, "servitor_excavate"),
                (tool_servitor_mission_status, "servitor_mission_status"), (tool_servitor_mission_control, "servitor_mission_control")):
    if "--servitor-only" not in sys.argv or nom.startswith("servitor_"):
        server.add_tool(fn, name=nom)


if __name__ == "__main__":
    server.run("stdio")
