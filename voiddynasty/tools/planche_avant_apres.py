#!/usr/bin/env python3
"""Planche « avant / après » pour juger un remplacement de textures à l'œil.

Usage :  python3 tools/planche_avant_apres.py <dossier_avant> <dossier_apres> <sortie.png> [--echelle 6] [--paires 4]

Les deux dossiers sont comparés par chemin relatif. Chaque paire est affichée côte à côte :
l'ancienne à gauche, la nouvelle à droite, avec le nom au-dessus.
"""
import argparse
import glob
import os
import sys

from PIL import Image, ImageDraw


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("avant")
    ap.add_argument("apres")
    ap.add_argument("sortie")
    ap.add_argument("--echelle", type=int, default=6)
    ap.add_argument("--paires", type=int, default=4, help="paires par ligne")
    ap.add_argument("--cellule", type=int, default=128)
    args = ap.parse_args()

    # les sauvegardes aplatissent le chemin : item/bolt_shell -> item_bolt_shell
    # (et peuvent avoir gardé ou perdu l'extension .png selon l'outil qui les a écrites)
    anciennes = {}
    for f in glob.glob(os.path.join(args.avant, "*")):
        base = os.path.basename(f)
        if base.endswith(".png"):
            base = base[:-4]
        if "_" in base:
            cat, nom = base.split("_", 1)
            anciennes[f"{cat}/{nom}"] = f
        else:
            anciennes[base] = f

    paires = []
    identiques = 0
    for rel, p_av in sorted(anciennes.items()):
        p_ap = os.path.join(args.apres, rel + ".png")
        if not os.path.exists(p_ap):
            continue
        # une paire dont les octets sont identiques n'apprend rien : c'est une texture non touchée
        if open(p_av, "rb").read() == open(p_ap, "rb").read():
            identiques += 1
            continue
        a = Image.open(p_av).convert("RGBA")
        b = Image.open(p_ap).convert("RGBA")
        e = max(1, min(args.echelle, args.cellule // max(a.width, a.height, b.width, b.height)))
        a = a.resize((a.width * e, a.height * e), Image.Resampling.NEAREST)
        b = b.resize((b.width * e, b.height * e), Image.Resampling.NEAREST)
        paires.append((rel, a, b, e))
    if identiques:
        print(f"{identiques} paires identiques (texture non touchée) — écartées de la planche")
    if not paires:
        print("aucune paire trouvée")
        return 1

    cote = max(max(a.width, a.height, b.width, b.height) for _, a, b, _ in paires)
    cw = cote * 2 + 26
    ch = cote + 34
    cols = args.paires
    lignes = (len(paires) + cols - 1) // cols
    planche = Image.new("RGBA", (cw * cols, ch * lignes), (26, 26, 32, 255))
    d = ImageDraw.Draw(planche)
    for i, (rel, a, b, _) in enumerate(paires):
        x, y = (i % cols) * cw, (i // cols) * ch
        d.text((x + 4, y + 6), rel.split("/")[-1][:20], fill=(235, 235, 240, 255))
        d.text((x + 4, y + 18), "avant", fill=(200, 120, 120, 255))
        d.text((x + cote + 16, y + 18), "après", fill=(120, 220, 140, 255))
        planche.paste(a, (x + 4, y + 30), a)
        planche.paste(b, (x + cote + 16, y + 30), b)
    planche.save(args.sortie)
    print(f"planche : {args.sortie} · {len(paires)} paires · {planche.width}×{planche.height}")
    for rel, a, b, e in paires:
        print(f"  {rel:40s} ×{e}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
