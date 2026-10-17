#!/usr/bin/env python3
"""Phase E — icônes d'œufs d'apparition, dans le style des œufs déjà présents.

Les œufs du mod sont des sprites 16x16 de ~290 octets : une silhouette d'œuf, un contour sombre, deux
tons de coque et un accent clair, sur fond transparent. On reproduit exactement cette recette, avec une
teinte par famille de mobs, plutôt que de dessiner à la main cinq fois la même forme.

Ce sont des icônes d'interface, pas du contenu porté : rien ici ne prétend venir de la source.

Usage : python3 tools/generer_oeufs.py [--simulation]
"""
from __future__ import annotations

import json
import os
import sys

from PIL import Image

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(RACINE, "src/main/resources/assets/voiddynasty")
NS = "voiddynasty"

# identifiant -> couleur de coque (les œufs du Chaos : rouge sang, laiton, vert bilieux, pourpre)
OEUFS = {
    # lot 1 — cultistes
    "cultist_1_spawn_egg": (139, 47, 47),
    "cultist_2_spawn_egg": (150, 96, 38),
    "cultist_3_spawn_egg": (92, 108, 46),
    "cultist_4_spawn_egg": (104, 52, 104),
    "cultist_boss_spawn_egg": (168, 32, 32),
    # lot 2 — Spaces Marines du Chaos (noir et laiton des Légions Renégates)
    "chaos_marine_1_spawn_egg": (58, 58, 66),
    "chaos_marine_2_spawn_egg": (72, 46, 46),
    "chaos_marine_3_spawn_egg": (46, 52, 66),
    "chaos_captain_spawn_egg": (86, 62, 30),
    "chaos_raptor_spawn_egg": (64, 40, 58),
    # lot 3 — servitors (acier et cuivre terni)
    "servitor_1_spawn_egg": (96, 96, 104),
    "servitor_2_spawn_egg": (110, 88, 60),
    "servitor_3_spawn_egg": (128, 72, 48),
    # lot 4 — machines (métal sombre, rouille)
    "chaos_dreadnought_spawn_egg": (70, 74, 84),
    "chaos_hellbrute_spawn_egg": (92, 60, 48),
    "chaos_forgefiend_spawn_egg": (128, 48, 32),
    # lot 5 — prince démon (pourpre et laiton)
    # couleur choisie par l’assistant : la source ne donne pas de couleur d’œuf pour le HellDrake
    "chaos_helldrake_spawn_egg": (122, 26, 34),
    "daemon_prince_spawn_egg": (120, 44, 140),
    # lot 6 — vehicules : vert olive du char, releve sur sa propre texture (24,56,56), eclairci pour
    # qu'une icone de 16 pixels reste lisible
    "lemunruss_spawn_egg": (58, 92, 72),
}

# silhouette relevée sur textures/item/tyranid_warrior_spawn_egg.png (16x16)
SILHOUETTE = [
    "................",
    "................",
    "................",
    "......####......",
    "....########....",
    "...##########...",
    "...###########..",
    "..############..",
    "..############..",
    "..#############.",
    "..#############.",
    "..############..",
    "..############..",
    "..############..",
    "...##########...",
    ".....######.....",
]

CONTOUR = (20, 20, 26, 255)


def assombrir(c, f=0.66):
    return tuple(int(v * f) for v in c[:3]) + (255,)


def eclaircir(c, f=1.28):
    return tuple(min(255, int(v * f)) for v in c[:3]) + (255,)


def dessiner(couleur) -> Image.Image:
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for y, ligne in enumerate(SILHOUETTE):
        for x, c in enumerate(ligne):
            if c != "#":
                continue
            # contour sur le bord de la silhouette
            bord = any(
                0 <= y + dy < 16 and 0 <= x + dx < 16 and SILHOUETTE[y + dy][x + dx] != "#"
                for dy, dx in ((-1, 0), (1, 0), (0, -1), (0, 1))
            )
            if bord:
                px[x, y] = CONTOUR
            elif x >= 10 or y <= 7:              # moitié droite/basse plus sombre, comme les œufs existants
                px[x, y] = assombrir(couleur)
            else:
                px[x, y] = couleur + (255,)
    # accent clair : petite tache en haut à gauche
    for x, y in ((5, 5), (6, 5), (5, 6)):
        if px[x, y][3]:
            px[x, y] = eclaircir(couleur)
    return im


def main() -> int:
    simulation = "--simulation" in sys.argv
    faits = []
    for ident, couleur in OEUFS.items():
        image = dessiner(couleur)
        couleurs = len(image.getcolors(100000) or [])
        if couleurs < 4:
            print(f"ÉCHEC {ident} : {couleurs} couleurs, l'œuf serait vide")
            return 1
        if not simulation:
            image.save(os.path.join(ASSETS, "textures/item", f"{ident}.png"))
            for rel, contenu in (
                (f"models/item/{ident}.json",
                 {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{ident}"}}),
                (f"items/{ident}.json",
                 {"model": {"type": "minecraft:model", "model": f"{NS}:item/{ident}"}}),
            ):
                chemin = os.path.join(ASSETS, rel)
                os.makedirs(os.path.dirname(chemin), exist_ok=True)
                with open(chemin, "w", encoding="utf-8") as f:
                    json.dump(contenu, f, indent=2)
                    f.write("\n")
        faits.append((ident, couleurs))
    print(f"{len(faits)} œufs {'simulés' if simulation else 'écrits'}, icône + modèle + définition :")
    for ident, couleurs in faits:
        print(f"  {ident:26s} {couleurs} couleurs")
    if len(faits) != len(OEUFS):
        print(f"ÉCHEC : {len(faits)} œufs sur {len(OEUFS)} attendus")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
