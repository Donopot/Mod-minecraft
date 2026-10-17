#!/usr/bin/env python3
"""Planche de contact des textures — pour les juger à l'œil (et les apparier par intention).

Usage :  python3 tools/planche_textures.py <dossier_de_textures> <sortie.png> [--echelle 8] [--max 60]

Pourquoi : les textures du pack Astartes ne portent pas les mêmes noms que les nôtres
(`metalplate` vs `ceramite_ingot`). Aucun appariement automatique par nom ne peut marcher — il faut
les VOIR. Cette planche les colle en grille agrandie (NEAREST, pas d'interpolation) avec un numéro,
et imprime la correspondance numéro → fichier.
"""
import argparse
import glob
import os
import sys

from PIL import Image, ImageDraw

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("dossier")
    ap.add_argument("sortie")
    ap.add_argument("--echelle", type=int, default=8)
    ap.add_argument("--max", type=int, default=60)
    ap.add_argument("--colonnes", type=int, default=10)
    ap.add_argument("--seuil", type=int, default=0, help="ne garder que les fichiers sous N octets (0 = tous)")
    ap.add_argument("--cellule", type=int, default=128, help="côté maximal d'une cellule, en pixels")
    ap.add_argument("--maxdim", type=int, default=512,
                    help="écarter les images dont un côté dépasse cette taille (planches, diagrammes)")
    args = ap.parse_args()

    fichiers = sorted(glob.glob(os.path.join(args.dossier, "**/*.png"), recursive=True))
    if args.seuil:
        fichiers = [f for f in fichiers if os.path.getsize(f) < args.seuil]
    ecartes = []
    gardes = []
    for f in fichiers:
        with Image.open(f) as o:
            if max(o.size) > args.maxdim:
                ecartes.append(f"{os.path.basename(f)} ({o.width}×{o.height})")
                continue
        gardes.append(f)
    if ecartes:
        print(f"écartés ({len(ecartes)}) car plus grands que {args.maxdim} px — planches, pas textures :")
        for e in ecartes:
            print("   -", e)
    fichiers = gardes[: args.max]
    if not fichiers:
        print("aucun PNG trouvé")
        return 1

    cellules = []
    for f in fichiers:
        o = Image.open(f).convert("RGBA")
        # agrandissement NEAREST, mais borné : une texture 256×256 ×6 ferait exploser la planche
        # (et se faire tuer le processus). On grossit d'un facteur entier tel que la cellule tienne
        # dans --cellule pixels de côté, jamais en dessous de ×1.
        e = max(1, min(args.echelle, args.cellule // max(o.width, o.height)))
        if o.width * e > args.cellule or o.height * e > args.cellule:
            e = 1
        im = o.resize((o.width * e, o.height * e), Image.Resampling.NEAREST)
        cellules.append((f, im, o.size, e))
    cw = max(im.width for _, im, _, _ in cellules) + 12
    ch = max(im.height for _, im, _, _ in cellules) + 30
    cols = min(args.colonnes, len(cellules))
    lignes = (len(cellules) + cols - 1) // cols
    planche = Image.new("RGBA", (cw * cols, ch * lignes), (28, 28, 34, 255))
    d = ImageDraw.Draw(planche)
    for i, (f, im, dims, e) in enumerate(cellules):
        x, y = (i % cols) * cw, (i // cols) * ch
        planche.paste(im, (x + 6, y + 22), im)
        nom = os.path.basename(f)[:-4]
        d.text((x + 6, y + 6), f"{i + 1}. {nom[:22]}", fill=(230, 230, 235, 255))
    planche.save(args.sortie)
    print(f"planche : {args.sortie} · {len(cellules)} textures · {planche.width}×{planche.height}")
    print(f"dossier : {args.dossier}")
    print("\ncorrespondance numéro → fichier (dimensions d'origine, facteur) :")
    for i, (f, im, dims, e) in enumerate(cellules, 1):
        print(f"  {i:3d}. {os.path.basename(f):34s} {dims[0]}×{dims[1]}  ×{e}  {os.path.getsize(f)} o")
    return 0

if __name__ == "__main__":
    sys.exit(main())
