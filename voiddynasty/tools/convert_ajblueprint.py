#!/usr/bin/env python3
"""Phase E — convertir un blueprint Animated Java (.ajblueprint) en mob GeckoLib.

Un `.ajblueprint` est un projet **Blockbench** (Animated Java n'est qu'une extension) : on y trouve
`elements` (les cubes, chacun avec son uuid), `outliner` (la hiérarchie des os, avec leurs pivots),
`textures` (PNG en base64) et `animations` (keyframes, indexées par **uuid d'os** mais qui portent le
nom de l'os). GeckoLib, lui, veut :
  - `geckolib/models/<nom>.geo.json`       — géométrie Bedrock 1.12 (os + `parent` + cubes + UV)
  - `geckolib/animations/<nom>.animation.json` — Bedrock 1.8 (clips nommés, os par NOM)
  - une **seule** texture par mob  → d'où l'atlas.

Le point délicat est l'atlas : ces cultistes sont découpés en 7 textures (torse, tête, bras, tissu,
jambes, deux variantes de blessure). GeckoLib n'en accepte qu'une, donc on assemble une grille et on
décale chaque UV de l'origine de sa texture dans la grille. Sans ce décalage, tout le mob afficherait
le torse.

Vérifié avant écriture : GeckoLib normalise le chemin (`BakedModelCache.stripLegacyPath`) et attend
`assets/<ns>/geckolib/models/<nom>.geo.json` ; l'API est celle de GeckoLib 5.5.7 (voir la référence
`geckolib-mobs-26.3.md` du skill).

Usage : python3 tools/convert_ajblueprint.py --source <dossier blueprints> [--simulation]
"""
from __future__ import annotations

import base64
import io
import json
import math
import os
import re
import sys

from PIL import Image

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEST_MODELES = os.path.join(RACINE, "src/main/resources/assets/voiddynasty/geckolib/models")
DEST_ANIMS = os.path.join(RACINE, "src/main/resources/assets/voiddynasty/geckolib/animations")
DEST_TEXTURES = os.path.join(RACINE, "src/main/resources/assets/voiddynasty/textures/entity")

# blueprint source -> (identifiant dans le mod, nom lisible)
A_CONVERTIR = {
    # lot 1 — cultistes
    "Cultist1": "cultist_1",
    "Cultist2": "cultist_2",
    "Cultist3": "cultist_3",
    "Cultist4": "cultist_4",
    "Cultistboss": "cultist_boss",
    # lot 2 — Spaces Marines du Chaos (les noms de fichiers contiennent des espaces)
    "chaos space marine": "chaos_marine_1",
    "chaos space marine 2": "chaos_marine_2",
    "chaos space marine 3": "chaos_marine_3",
    "chaos space marine captain": "chaos_captain",
    "chaos space marine raptor": "chaos_raptor",
    # lot 3 — servitors (base squelette/zombie dans la source, mais modèles propres)
    "servitor1": "servitor_1",
    "servitor2": "servitor_2",
    "servitor3": "servitor_3",
    # lot 4 — machines du Chaos
    "ChaosDreadnought": "chaos_dreadnought",
    "hellbrute": "chaos_hellbrute",
    "forgefiend": "chaos_forgefiend",
    # lot 5 — les deux derniers : prince démon (modèle bloodletter) et helldrake
    "bloodletter": "daemon_prince",
    "helldrake": "chaos_helldrake",
}

FACES = ("north", "east", "south", "west", "up", "down")


def nombre(v) -> float:
    """Les keyframes mélangent nombres et chaînes (« 0 ») : on normalise."""
    if isinstance(v, str):
        try:
            return float(v)
        except ValueError:
            return 0.0
    return float(v)


def image_depuis_data_url(source: str) -> Image.Image:
    if not source or "," not in source:
        raise ValueError("texture sans données base64")
    return Image.open(io.BytesIO(base64.b64decode(source.split(",", 1)[1]))).convert("RGBA")


def textures_utilisees(blueprint: dict) -> set[int]:
    """Indices de texture réellement référencés par au moins une face.

    Constat sur les cultistes : le blueprint embarque 7 textures (torse, tête, bras, tissu, jambes,
    deux variantes de blessure) mais TOUTES les 504 faces n'en référencent qu'UNE. Les autres sont des
    restes d'itération. Les inclure dans un atlas ferait un pavé inutile — et de tailles différentes,
    ce qui interdit une grille simple.
    """
    utilises = set()
    for e in blueprint.get("elements", []):
        for données in (e.get("faces") or {}).values():
            if "texture" in données:
                utilises.add(données["texture"])
    return utilises


def construire_atlas(blueprint: dict) -> tuple[Image.Image, dict[int, tuple[int, int, float, float]]]:
    """Assemble les textures utilisées et rend, pour chacune : origine dans l'atlas + facteur d'échelle.

    Le facteur est indispensable : les UV sont exprimés dans la grille `resolution` du projet (64x64
    ici) alors que le PNG fait 512x512. Sans mise à l'échelle, le mob échantillonnerait le coin
    supérieur gauche de sa texture.
    """
    indices = sorted(textures_utilisees(blueprint))
    if not indices:
        raise ValueError("aucune face ne référence de texture")
    images = {i: image_depuis_data_url(blueprint["textures"][i].get("source", "")) for i in indices}
    grosseur = (max(im.width for im in images.values()), max(im.height for im in images.values()))
    colonnes = max(1, math.ceil(math.sqrt(len(images))))
    lignes = math.ceil(len(images) / colonnes)
    atlas = Image.new("RGBA", (grosseur[0] * colonnes, grosseur[1] * lignes), (0, 0, 0, 0))
    res_l, res_h = blueprint["resolution"]["width"], blueprint["resolution"]["height"]
    placements = {}
    for rang, i in enumerate(indices):
        im = images[i]
        cx, cy = (rang % colonnes) * grosseur[0], (rang // colonnes) * grosseur[1]
        atlas.paste(im, (cx, cy))
        placements[i] = (cx, cy, im.width / res_l, im.height / res_h)
    return atlas, placements


def indexer_elements(blueprint: dict) -> dict[str, dict]:
    return {e["uuid"]: e for e in blueprint.get("elements", []) if "uuid" in e}


def parcourir_os(entree: dict, elements: dict[str, dict], parent: str | None, sortie: list[dict]) -> None:
    """Aplatit l'arbre Blockbench en liste d'os GeckoLib (avec `parent`)."""
    cubes = []
    for enfant in entree.get("children", []):
        if isinstance(enfant, str):                       # référence à un cube
            e = elements.get(enfant)
            if e is None:
                raise KeyError(f"cube référencé mais absent : {enfant}")
            if "from" in e and "to" in e:
                cubes.append(e)
            else:                               # locator / élément non géométrique
                cle = str(e.get("type", "sans type"))
                NON_CUBES[cle] = NON_CUBES.get(cle, 0) + 1
        elif isinstance(enfant, dict):                    # sous-groupe
            parcourir_os(enfant, elements, entree.get("name"), sortie)
    os_ = {"name": entree.get("name", "os"), "pivot": [nombre(v) for v in entree.get("origin", [0, 0, 0])]}
    if parent:
        os_["parent"] = parent
    os_["cubes"] = cubes
    sortie.append(os_)


NON_CUBES: dict[str, int] = {}


def cube_vers_gecko(cube: dict, origines: list[tuple[int, int]]) -> dict:
    # Un blueprint peut contenir des `locator` (points d'ancrage) : ni `from` ni `to`, donc pas de la
    # géométrie. On les écarte en les COMPTANT, jamais en silence.
    if "from" not in cube or "to" not in cube:
        cle = str(cube.get("type", "sans type"))
        NON_CUBES[cle] = NON_CUBES.get(cle, 0) + 1
        return {}
    x1, y1, z1 = (nombre(v) for v in cube["from"])
    x2, y2, z2 = (nombre(v) for v in cube["to"])
    origine = [min(x1, x2), min(y1, y2), min(z1, z2)]
    taille = [abs(x2 - x1), abs(y2 - y1), abs(z2 - z1)]
    uv = {}
    for face, donnees in (cube.get("faces") or {}).items():
        if face not in FACES or "uv" not in donnees:
            continue
        u1, v1, u2, v2 = (nombre(v) for v in donnees["uv"])
        cx, cy, sx, sy = origines.get(donnees.get("texture"), (0, 0, 1.0, 1.0))
        uv[face] = {"uv": [round(cx + u1 * sx, 3), round(cy + v1 * sy, 3)],
                    "uv_size": [round((u2 - u1) * sx, 3), round((v2 - v1) * sy, 3)]}
    sortie = {"origin": origine, "size": taille}
    if uv:
        sortie["uv"] = uv
    if cube.get("rotation"):
        r = [nombre(v) for v in cube["rotation"]]
        if any(r):
            sortie["rotation"] = r
            sortie["pivot"] = [nombre(v) for v in cube.get("origin", origine)]
    return sortie


def convertir_geometrie(blueprint: dict, ident: str, origines: dict,
                        atlas_size: tuple[int, int] | None = None) -> dict:
    elements = indexer_elements(blueprint)
    os_bruts: list[dict] = []
    for entree in blueprint.get("outliner", []):
        if isinstance(entree, dict):
            parcourir_os(entree, elements, None, os_bruts)
    if not os_bruts:
        raise ValueError("aucun os : l'outliner est vide ou d'un format inattendu")
    total_cubes = sum(len(o["cubes"]) for o in os_bruts)
    if total_cubes == 0:
        raise ValueError("aucun cube rattaché à un os : l'arbre est mal relié")
    w, h = atlas_size if atlas_size else (blueprint["resolution"]["width"], blueprint["resolution"]["height"])
    os_gecko = []
    for o in os_bruts:
        os_gecko.append({"name": o["name"], "pivot": o["pivot"],
                         **({"parent": o["parent"]} if "parent" in o else {}),
                         "cubes": [c for c in (cube_vers_gecko(c, origines) for c in o["cubes"]) if c]})
    return {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": f"geometry.{ident}", "texture_width": w, "texture_height": h,
                        "visible_bounds_width": 3, "visible_bounds_height": 4,
                        "visible_bounds_offset": [0, 1, 0]},
        "bones": os_gecko}]}


def normaliser_clip(brut: str) -> str:
    """Nom de clip utilisable en Java, dérivé SANS DEVINER.

    Les noms de la source sont bruts : `cultist1idol` (coquille pour idle), `3attacj2`, `marine1shoot`,
    `marine2attack`. On classe par MOT D'ACTION, car la source distingue le tir (`shoot`) de la mêlée
    (`attack`) — les confondre ferait jouer la mauvaise animation. Le numéro d'origine ne sert qu'à
    départager deux clips de même mot dans un même mob. Le rapport imprime le résultat : ce qui a été
    renommé est visible.
    """
    n = re.sub(r"[^a-z0-9]", "", brut.lower())
    for prefixe in ("cultist", "chaos", "spacemarine", "servitor", "hellbrute", "forgefiend",
                    "bloodletter", "dreadnought", "marine", "raptor", "nurgling"):
        n = re.sub("^" + prefixe + "+", "", n)
    n = re.sub(r"^[0-9]+", "", n)
    if "idol" in n or "idle" in n:
        return "idle"
    if "walk" in n or "run" in n:
        return "walk"
    if "death" in n or "die" in n:
        return "death"
    if "shoot" in n or "fire" in n:
        return "shoot"
    if "attack" in n or "attacj" in n or "melee" in n or "spin" in n:
        return "attack"
    return n or "anim"


def convertir_animations(blueprint: dict) -> dict:
    """Bedrock 1.8 : les os sont désignés par leur NOM, pas par leur uuid."""
    clips = {}
    for anim in blueprint.get("animations", []):
        nom = normaliser_clip(anim.get("name") or f"anim{len(clips)}")
        if nom in clips:                       # deux clips de même mot dans un mob : on numérote
            rang = 2
            while f"{nom}{rang}" in clips:
                rang += 1
            nom = f"{nom}{rang}"
        clips[nom] = anim
    sortie = {}
    for nom, anim in clips.items():
        os_animes, dernier = {}, 0.0
        for _, animator in (anim.get("animators") or {}).items():
            if animator.get("type") != "bone":
                continue
            canaux: dict = {}
            for kf in animator.get("keyframes", []):
                canal = kf.get("channel")
                if canal not in ("rotation", "position", "scale"):
                    continue
                t = round(nombre(kf.get("time", 0)), 3)
                dernier = max(dernier, t)
                point = (kf.get("data_points") or [{}])[0]
                canaux.setdefault(canal, {})[f"{t:.3f}"] = [nombre(point.get("x", 0)),
                                                            nombre(point.get("y", 0)),
                                                            nombre(point.get("z", 0))]
            if canaux:
                os_animes[animator.get("name", "os")] = canaux
        if not os_animes:
            continue
        boucle = {"loop": True, "once": False, "hold": False}.get(anim.get("loop", "once"), False)
        sortie[nom] = {"loop": boucle, "animation_length": round(max(dernier, 0.05), 3), "bones": os_animes}
    return {"format_version": "1.8.0", "animations": sortie}


def ecrire(chemin: str, contenu, simulation: bool) -> None:
    if simulation:
        return
    os.makedirs(os.path.dirname(chemin), exist_ok=True)
    if chemin.endswith(".png"):
        contenu.save(chemin)
    else:
        with open(chemin, "w", encoding="utf-8") as f:
            json.dump(contenu, f, indent=1)


def main() -> int:
    if "--source" not in sys.argv:
        print("usage : --source <dossier des .ajblueprint> [--simulation]")
        return 2
    source = sys.argv[sys.argv.index("--source") + 1]
    simulation = "--simulation" in sys.argv
    rapport, echecs = [], []
    for fichier, ident in A_CONVERTIR.items():
        chemin = os.path.join(source, f"{fichier}.ajblueprint")
        if not os.path.exists(chemin):
            echecs.append(f"{fichier} : blueprint introuvable")
            continue
        blueprint = json.load(open(chemin, encoding="utf-8"))
        try:
            atlas, placements = construire_atlas(blueprint)
            geo = convertir_geometrie(blueprint, ident, placements, atlas.size)
            anims = convertir_animations(blueprint)
        except Exception as e:                                      # noqa: BLE001
            echecs.append(f"{fichier} : {e}")
            continue
        nb_cubes = sum(len(b["cubes"]) for b in geo["minecraft:geometry"][0]["bones"])
        if nb_cubes == 0 or not anims["animations"]:
            echecs.append(f"{fichier} : conversion vide (cubes={nb_cubes}, clips={len(anims['animations'])})")
            continue
        ecrire(os.path.join(DEST_MODELES, f"{ident}.geo.json"), geo, simulation)
        ecrire(os.path.join(DEST_ANIMS, f"{ident}.animation.json"), anims, simulation)
        ecrire(os.path.join(DEST_TEXTURES, f"{ident}.png"), atlas, simulation)
        rapport.append((ident, nb_cubes, len(geo["minecraft:geometry"][0]["bones"]),
                        len(anims["animations"]), atlas.size, sorted(anims["animations"])))

    print(f"{len(rapport)} mobs convertis" + (" (simulation)" if simulation else "") + " :")
    for ident, cubes, os_, clips, taille, noms in rapport:
        print(f"  {ident:14s} {cubes:3d} cubes · {os_:2d} os · {clips} clips · atlas {taille[0]}x{taille[1]}")
        print(f"  {'':14s} clips : {', '.join(noms)}")
    if NON_CUBES:
        print("  éléments non géométriques écartés (locators d'ancrage) : "
              + ", ".join(f"{k}×{v}" for k, v in sorted(NON_CUBES.items())))
    for e in echecs:
        print(f"  [ÉCHEC] {e}")
    if len(rapport) != len(A_CONVERTIR):
        print(f"ÉCHEC : {len(rapport)} mobs sur {len(A_CONVERTIR)} attendus — l'écart doit être expliqué")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
