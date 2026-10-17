#!/usr/bin/env python3
"""Phase E/F — relève l'apparition RÉELLE des ennemis dans la source.

Constat de départ : la source n'a AUCUNE apparition naturelle. Son `load.mcfunction` pose
`gamerule doMobSpawning false`, et il n'existe ni données de biome, ni règle d'apparition, ni notion de
faction dans tout l'extrait. Tous les ennemis arrivent par des `summon` scriptés, zone par zone, dans
la campagne (`GameEngine/data/mj.gameengine/function/levels/...`).

Ce que le script relève, pour chaque zone : son index de niveau, son nom affiché, et la liste des
créatures invoquées avec leurs coordonnées, plus les points de réapparition.

Les vagues passent parfois par une fonction LOCALE (`reactor` appelle `marinesummon`, qui invoque 4
créatures en position relative `~ ~ ~`). Ces appels sont donc SUIVIS : sans cela, 5 vagues entières
disparaîtraient du relevé sans que rien ne le signale. Ce qui n'a pas pu être résolu est listé à part —
un extracteur silencieux ferait passer un oubli pour une absence.

Sortie : un JSON exploitable (phase F) et un document lisible. Aucune valeur n'est inventée.
"""
import json
import os
import re
import sys

RACINE = ("/opt/data/workspace/astartes-extract/Astartes Warhammer Game World/datapacks/GameEngine/"
          "data/mj.gameengine/function")
SORTIE_JSON = "/opt/data/workspace/um-test-mod/docs/campagne-source.json"
SORTIE_MD = "/opt/data/workspace/um-test-mod/docs/CAMPAGNE-SOURCE.md"

RE_SPAWN_ABS = re.compile(r"execute positioned (-?[\d.]+) (-?[\d.]+) (-?[\d.]+) run function "
                          r"mj\.whenemies:([a-zA-Z0-9_]+)/")
RE_SPAWN_REL = re.compile(r"execute positioned (?:~ ~ ~|~) run function mj\.whenemies:([a-zA-Z0-9_]+)/")
RE_APPEL = re.compile(r"execute positioned (~?[\d.~-]*) (~?[\d.~-]*) (~?[\d.~-]*) "
                      r"run function mj\.gameengine:([a-zA-Z0-9_/]+)")
RE_FONCTION_NUE = re.compile(r"^function mj\.gameengine:([a-zA-Z0-9_/]+)", re.M)
RE_NIVEAU = re.compile(r"scoreboard players set \.level level (\d+)")
RE_ETAT = re.compile(r"set \.([a-zA-Z0-9_]+) active 1")
RE_ACTIONBAR = re.compile(r'title @a actionbar \{"text":\s*"([^"]+)"')
RE_WAYPOINT = re.compile(r"summon shulker (-?[\d.]+) (-?[\d.]+) (-?[\d.]+) .*Tags:\[([^\]]+)\]")
RE_RESPAWN = re.compile(r"if score \.level level matches (\d+) run tp @s (-?[\d.]+) (-?[\d.]+) (-?[\d.]+)")


def indexer() -> dict:
    """Toutes les fonctions de la campagne, indexées par chemin relatif."""
    index = {}
    for racine, _, fichiers in os.walk(RACINE):
        for f in sorted(fichiers):
            if not f.endswith(".mcfunction"):
                continue
            chemin = os.path.join(racine, f)
            relatif = os.path.relpath(chemin, RACINE).replace(".mcfunction", "")
            texte = open(chemin, encoding="utf-8", errors="ignore").read()
            index[relatif] = {
                "absolus": [{"x": float(x), "y": float(y), "z": float(z), "mob": mob}
                            for x, y, z, mob in RE_SPAWN_ABS.findall(texte)],
                "relatifs": RE_SPAWN_REL.findall(texte),
                "appels": [{"x": x, "y": y, "z": z, "cible": c} for x, y, z, c in RE_APPEL.findall(texte)]
                          + [{"x": "~", "y": "~", "z": "~", "cible": c} for c in RE_FONCTION_NUE.findall(texte)],
                "waypoints": [{"x": float(x), "y": float(y), "z": float(z), "tags": tags.strip()}
                              for x, y, z, tags in RE_WAYPOINT.findall(texte)],
                "titre": (RE_ACTIONBAR.findall(texte) or [None])[0],
                "niveau": int(RE_NIVEAU.findall(texte)[0]) if RE_NIVEAU.findall(texte) else None,
                "etats": RE_ETAT.findall(texte),
                "texte": texte,
            }
    return index


def resoudre(index: dict, nom: str, position: str, profondeur: int, problemes: list) -> list:
    """Créatures invoquées par une fonction, appels locaux dépliés. `position` = point d'ancrage."""
    if profondeur > 6:
        problemes.append(f"récursion trop profonde sur {nom}")
        return []
    if nom not in index:
        problemes.append(f"fonction introuvable : {nom}")
        return []
    fonction = index[nom]
    trouves = [dict(s) | {"ancrage": position} for s in fonction["absolus"]]
    for mob in fonction["relatifs"]:
        trouves.append({"x": position, "y": position, "z": position, "mob": mob, "ancrage": position})
    for appel in fonction["appels"]:
        ancre = (f"{appel['x']} {appel['y']} {appel['z']}" if "~" not in (appel["x"], appel["y"], appel["z"])
                 else position)
        trouves += resoudre(index, appel["cible"], ancre, profondeur + 1, problemes)
    return trouves


def main() -> int:
    index = indexer()
    problemes: list[str] = []
    zones = []
    for nom in sorted(index):
        if not (nom.startswith("levels/") or nom.startswith("levels\\")):
            continue                     # les helpers ne sont pas des zones : ils sont dépliés dans leurs appelants
        f = index[nom]
        apparitions = resoudre(index, nom, "position de la zone", 0, problemes)
        zones.append({
            "zone": nom.replace("levels/", "").replace("levels\\", "").replace("\\", "/"),
            "niveau": f["niveau"], "titre": f["titre"], "etats": f["etats"],
            "waypoints": f["waypoints"], "apparitions": apparitions,
        })

    respawns = {}
    death = index.get("death")
    if death:
        for niveau, x, y, z in RE_RESPAWN.findall(death["texte"]):
            respawns[int(niveau)] = {"x": float(x), "y": float(y), "z": float(z)}

    # contrôle de non-régression : toute fonction locale appelée doit exister
    for nom, f in index.items():
        for appel in f["appels"]:
            if appel["cible"] not in index:
                problemes.append(f"{nom} appelle {appel['cible']}, introuvable")

    donnees = {"zones": zones, "respawns": respawns, "problemes": problemes}
    os.makedirs(os.path.dirname(SORTIE_JSON), exist_ok=True)
    with open(SORTIE_JSON, "w", encoding="utf-8") as f:
        json.dump(donnees, f, indent=2, ensure_ascii=False)

    total = sum(len(z["apparitions"]) for z in zones)
    mobs: dict[str, int] = {}
    for z in zones:
        for a in z["apparitions"]:
            mobs[a["mob"]] = mobs.get(a["mob"], 0) + 1
    zones.sort(key=lambda z: (z["niveau"] is None, z["niveau"] or 0, z["zone"]))

    lignes = [
        "# La campagne de la source — relevé des apparitions", "",
        "> Document **extrait**, pas écrit : chaque valeur vient des fonctions `mcfunction` de la source.",
        "> Il remplace la sous-tâche « apparition naturelle par biome et factions », qui **n'existe pas**",
        "> dans la source.",
        "",
        "## Pourquoi il n'y a pas d'apparition naturelle", "",
        "`gamerule doMobSpawning false` est posée au chargement du datapack GameEngine. L'extrait complet",
        "ne contient **aucune** donnée de biome, **aucune** règle d'apparition, **aucune** notion de",
        "faction. Tous les ennemis arrivent par des `summon` scriptés. Le seul « système d'apparition »",
        "de la source **est** la campagne — c'est donc la campagne qu'on relève.",
        "",
        "## Chiffres", "",
        f"- **{len(zones)} zones** scriptées",
        f"- **{total} apparitions** d'ennemis, à des coordonnées fixes",
        f"- **{len(respawns)} points de réapparition** (index de niveau → coordonnées)",
        f"- **{len(mobs)} types de créatures** invoquées",
        "",
        "### Créatures invoquées, par nombre d'apparitions", "",
        "| créature | apparitions |", "|---|---|",
    ]
    for mob, n in sorted(mobs.items(), key=lambda kv: -kv[1]):
        lignes.append(f"| `{mob}` | {n} |")

    lignes += ["", "## Les zones, dans l'ordre de la campagne", "",
               "| niveau | zone | titre affiché | ennemis invoqués |", "|---|---|---|---|"]
    for z in zones:
        detail = " · ".join(f"{a['mob']} ({a['x']} {a['y']} {a['z']})" if isinstance(a["x"], float)
                            else f"{a['mob']} (vague en {a['ancrage']})" for a in z["apparitions"])
        lignes.append(f"| {z['niveau'] if z['niveau'] is not None else '—'} | `{z['zone']}` | "
                      f"{z['titre'] or '—'} | {detail or '—'} |")

    lignes += ["", "## Points de réapparition (index de niveau → coordonnées)", "",
               "| niveau | x | y | z |", "|---|---|---|---|"]
    for niveau, p in sorted(respawns.items()):
        lignes.append(f"| {niveau} | {p['x']:.0f} | {p['y']:.0f} | {p['z']:.0f} |")

    if problemes:
        lignes += ["", "## Ce que l'extracteur n'a PAS pu résoudre", ""]
        lignes += [f"- {p}" for p in problemes[:40]]
    else:
        lignes += ["", "Toutes les vagues locales ont été dépliées : chaque appel `function",
                   "mj.gameengine:…` pointe vers une fonction existante du relevé."]

    with open(SORTIE_MD, "w", encoding="utf-8") as f:
        f.write("\n".join(lignes) + "\n")

    print(f"{len(zones)} zones · {total} apparitions · {len(respawns)} points de réapparition · "
          f"{len(problemes)} problème(s)")
    print("créatures :", ", ".join(f"{m}×{n}" for m, n in sorted(mobs.items(), key=lambda kv: -kv[1])))
    print(f"→ {SORTIE_MD}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
