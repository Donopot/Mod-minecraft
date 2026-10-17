#!/usr/bin/env python3
"""Phase A1 — remplacer des textures par des textures du pack Astartes.

⚠️ LIRE CECI AVANT DE S'EN SERVIR — leçon du 16/10/2025.

Ce script a été écrit pour « remplacer les textures pauvres » en prenant pour critère la **taille du
fichier** (< 250 octets). Ce critère est FAUX : un sprite 16×16 net et bien dessiné pèse légitimement
100 à 250 octets. Les 43 remplacements produits ont tous été annulés — une planche avant/après
(`tools/planche_avant_apres.py`) a montré que les textures d'origine étaient des sprites propres et
lisibles, et que réduire une plaque HD de 128×128 ou 512×512 en 16×16 donne de la **bouillie grise**.

Donc : ce script ne sert PAS à « améliorer » une icône existante. Il sert quand on a une raison
précise de remplacer une texture par une autre — par exemple convertir un prop du pack en bloc du mod
(Phase C), où le motif doit venir de la source parce qu'il n'existe chez nous sous aucune forme.

Le tableau d'appariement est écrit à la main, et il doit le rester : les noms ne se correspondent pas
(`metalplate` vs `ceramite_ingot`), donc aucun appariement automatique n'est possible.

Méthode
  - réduction en 16×16 par moyenne de surface (`BOX`) : garde le motif et la palette, sans le halo
    du filtre Lanczos ;
  - teinte optionnelle (multiplication RGB) pour décliner une même texture en plusieurs couleurs ;
  - léger boost de saturation et de contraste, sinon une texture 128×128 réduite en 16×16 devient
    grise.

Ce script ne supprime rien : il écrit les nouvelles textures par-dessus, et les anciennes sont
conservées dans `/tmp/anciennes-textures/` (git les a de toute façon).

Usage :  python3 tools/remplacer_textures_pack.py [--simulation] [--seuil 250]
         --seuil 0 force le remplacement de toutes les entrées du tableau.
"""
from __future__ import annotations

import argparse
import os
import shutil
import sys

from PIL import Image, ImageEnhance

PACK = ("/opt/data/workspace/astartes-extract/resourcepack/"
        "ASTARTES ARMOURY Resource Pack 2025/assets/minecraft/textures/item/custom")
ASSETS = "/opt/data/workspace/um-test-mod/src/main/resources/assets/voiddynasty/textures"
SAUVEGARDE = "/tmp/anciennes-textures"

# (notre texture, texture du pack, teinte RGB ou None)
# La teinte s'applique en multipliant : (255,255,255) = intacte.
TABLE: list[tuple[str, str, tuple[int, int, int] | None]] = [
    # --- munitions ---------------------------------------------------------
    ("item/bolt_shell",             "greencasing",             None),
    ("item/heavy_bolt_shell",       "greencasing",             (150, 110, 110)),
    ("item/stalker_bolt_round",     "cannonshell",             None),
    ("item/shotgun_shell",          "gold",                    (255, 190, 180)),
    ("item/missile_ammo",           "cannonshell",             (200, 200, 210)),
    # --- cellules et carburant ---------------------------------------------
    ("item/las_cell",               "screens",                 (190, 255, 200)),
    ("item/plasma_cell",            "plasmacoil",              None),
    ("item/melta_cell",             "meltabarrel",             None),
    ("item/promethium",             "meltabarrel",             (255, 230, 150)),
    ("item/plasma_coil",            "plasmacoil",              None),
    # --- composants --------------------------------------------------------
    ("item/adamantium_plate",       "metalplate",              None),
    ("item/wires",                  "wiring",                  None),
    ("item/wax_seal",               "waxseal",                 None),
    ("item/warhead",                "cannonshell",             (230, 170, 170)),
    ("item/focus_lens",             "powerblade",              None),
    # --- lingots -----------------------------------------------------------
    ("item/titanium_ingot",         "whitemetal",              None),
    ("item/silicon_carbide",        "darkmetal",               None),
    ("item/ceramite_ingot",         "metalplate",             None),
    ("item/blue_ceramite_ingot",    "metalplate",              (150, 170, 255)),
    ("item/red_ceramite_ingot",     "metalplate",              (255, 150, 150)),
    ("item/black_ceramite_ingot",   "darkmetal",               (170, 170, 180)),
    ("item/green_ceramite_ingot",   "metalplate",              (150, 230, 160)),
    ("item/dark_green_ceramite_ingot", "darkmetal",            (140, 210, 150)),
    ("item/white_ceramite_ingot",   "whitemetal",              None),
    ("item/yellow_ceramite_ingot",  "gold",                    None),
    ("item/light_blue_ceramite_ingot", "whitemetal",           (170, 220, 255)),
    ("item/pink_ceramite_ingot",    "metalplate",              (255, 180, 220)),
    # --- pièces d'armure ---------------------------------------------------
    ("item/space_marine_helmet",     "astarteshelmet",          None),
    ("item/space_marine_chestplate", "astracorpsearmour",       None),
    ("item/space_marine_leggings",   "astracorpsecloth",        None),
    ("item/space_marine_boots",      "astracorpseboots",        None),
    ("item/terminator_helmet",       "astracorpsehead",         None),
    ("item/terminator_chestplate",   "astracorpsepaldron",      None),
    ("item/terminator_leggings",     "astracorpsecloth",        (200, 200, 210)),
    ("item/terminator_boots",        "astracorpseboots",        (200, 200, 210)),
    # --- appareils et divers ----------------------------------------------
    ("item/control_pad",            "doorbuttonmoniter",       None),
    ("item/energy_wave",            "powerblade",              None),
    ("item/gellar_shard",           "powerblade",              (220, 200, 255)),
    ("item/laser_drill",            "doorbuttonframe",         (255, 220, 180)),
    ("item/servo_skull",            "skulls",                  None),
    ("item/plasma_cannon",          "doorbuttonmoniter",       (170, 230, 255)),
    ("item/voiddynasty_item",       "metalplate",              None),
    # --- blocs -------------------------------------------------------------
    ("block/super_chest",              "ammocratebaserusty",   (200, 200, 210)),
    ("block/super_container",          "floorgrating",         None),
    ("block/voiddynasty_block",        "metalplate",           None),
    ("block/promethium_furnace",       "rustedsupports_1",     None),
    ("block/promethium_blast_furnace", "rustedsupports_1",     (255, 190, 170)),
    ("block/promethium_smoker",        "rustedsupports_1",     (190, 190, 200)),
    ("block/promethium_brewing_stand", "wallpannel1details",   None),
    ("block/mining_rig",               "doorbuttonframe",      (255, 210, 170)),
    ("block/sandbag",                  "sandbag",              None),
    # --- blocs de céramite --------------------------------------------------
    ("block/ceramite_block",           "metalplate",           None),
    ("block/blue_ceramite_block",      "metalplate",           (150, 170, 255)),
    ("block/red_ceramite_block",       "metalplate",           (255, 150, 150)),
    ("block/black_ceramite_block",     "darkmetal",            (170, 170, 180)),
    ("block/green_ceramite_block",     "metalplate",           (150, 230, 160)),
    ("block/dark_green_ceramite_block", "darkmetal",           (140, 210, 150)),
    ("block/white_ceramite_block",     "whitemetal",           None),
    ("block/yellow_ceramite_block",    "gold",                 None),
    ("block/light_blue_ceramite_block", "whitemetal",          (170, 220, 255)),
    ("block/pink_ceramite_block",      "metalplate",           (255, 180, 220)),
    ("block/gothic_wall",              "wallpannel1frame",     (220, 210, 200)),
]

# le nom réel dans le pack pour les fichiers dont l'orthographe diffère
ALIAS = {"rustedsupports_1": "rustedsupports (1)", "rustedsupports_1.png": "rustedsupports (1).png"}


def source_pack(nom: str) -> str | None:
    for candidat in (nom, ALIAS.get(nom, nom)):
        p = os.path.join(PACK, candidat + ".png")
        if os.path.exists(p):
            return p
    return None


def reduire(p: str, teinte: tuple[int, int, int] | None) -> Image.Image:
    im = Image.open(p).convert("RGBA")
    im = im.resize((16, 16), Image.Resampling.BOX)
    if teinte:
        r, g, b, a = im.split()
        r = r.point(lambda v: min(255, v * teinte[0] // 255))
        g = g.point(lambda v: min(255, v * teinte[1] // 255))
        b = b.point(lambda v: min(255, v * teinte[2] // 255))
        im = Image.merge("RGBA", (r, g, b, a))
    im = ImageEnhance.Color(im).enhance(1.15)
    im = ImageEnhance.Contrast(im).enhance(1.08)
    return im


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--simulation", action="store_true", help="montrer sans écrire")
    ap.add_argument("--seuil", type=int, default=250,
                    help="ne remplacer que les textures à ou sous cette taille en octets "
                         "(250 = le seuil de pauvreté de l'audit) ; 0 = tout remplacer")
    args = ap.parse_args()

    os.makedirs(SAUVEGARDE, exist_ok=True)
    faits, absents_pack, absents_nous, deja_bonnes = [], [], [], []
    for notre, src, teinte in TABLE:
        cible = os.path.join(ASSETS, notre + ".png")
        if not os.path.exists(cible):
            absents_nous.append(notre)
            continue
        avant = os.path.getsize(cible)
        # on ne touche PAS à une texture déjà correcte : la remplacer par une texture dérivée
        # n'est pas un gain, et peut même être une régression de style
        if args.seuil and avant > args.seuil:
            deja_bonnes.append((notre, avant))
            continue
        p_src = source_pack(src)
        if not p_src:
            absents_pack.append(f"{notre} ← {src}")
            continue
        if args.simulation:
            faits.append((notre, src, avant, -1))
            continue
        shutil.copy2(cible, os.path.join(SAUVEGARDE, notre.replace("/", "_")))
        reduire(p_src, teinte).save(cible)
        faits.append((notre, src, avant, os.path.getsize(cible)))

    print(f"textures remplacées : {len(faits)}")
    if absents_pack:
        print(f"  source absente du pack ({len(absents_pack)}) : {', '.join(absents_pack)}")
    if absents_nous:
        print(f"  texture absente chez nous ({len(absents_nous)}) : {', '.join(absents_nous)}")
    if deja_bonnes:
        print(f"  laissées telles quelles, déjà correctes ({len(deja_bonnes)}) : "
              f"{', '.join(f'{n} ({t} o)' for n, t in sorted(deja_bonnes))}")
    faibles = [(n, s, a, b) for n, s, a, b in faits if b > 0 and b < a]
    if faibles:
        print(f"  ⚠ plus légères après remplacement ({len(faibles)}) — à regarder de près : "
              f"{', '.join(f'{n} {a}→{b}' for n, _, a, b in faibles)}")
    if not args.simulation:
        print(f"\nanciennes textures conservées dans {SAUVEGARDE}")
        print("\ndétail (notre texture ← source, poids avant → après) :")
        for notre, src, avant, apres in sorted(faits):
            print(f"  {notre:38s} ← {src:24s} {avant:5d} → {apres:5d} o")
    return 0


if __name__ == "__main__":
    sys.exit(main())
