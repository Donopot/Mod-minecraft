#!/usr/bin/env python3
"""Ecart entre le profil de MON modele et le profil DESSINE de la planche.

C'est la note de ressemblance qui sert de juge etape par etape. Elle donne deux nombres :
  * MANQUANT : cellules du dessin que mon modele ne remplit pas (il lui manque de la matiere) ;
  * EN TROP  : cellules remplies chez moi mais vides dans le dessin (matiere en trop).

But : faire baisser les deux, etape par etape, jusqu'a ce que le profil soit le dessin.

Usage : ecart_profil.py --vox voxels/leman_russ_exact.vox [--planche docs/references/...jpeg]
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from PIL import Image

PLANCHE = "docs/references/lemunruss-planche-technique.jpeg"
L_CIBLE, H_CIBLE = 59, 37          # grille de reference : cotes de la planche a 0,12 m


def profil_planche(chemin):
    """Le profil dessine, ramene a la grille de reference."""
    from copier_croquis import (VUE_PROFIL, plus_grande_forme, recadrer_et_redimensionner,
                                silhouette)
    img = Image.open(chemin).convert("L")
    m = plus_grande_forme(silhouette(img.crop(VUE_PROFIL)))
    grille, boite = recadrer_et_redimensionner(m, L_CIBLE, H_CIBLE)
    return grille


def profil_modele(voxels):
    """Le profil de mon modele : pour chaque (longueur, hauteur), y a-t-il de la matiere ?

    Repere du modele : X largeur, Y longueur, Z hauteur.
    """
    ys = [p[1] for p in voxels]
    zs = [p[2] for p in voxels]
    y0, z0 = min(ys), min(zs)
    grille = Image.new("L", (L_CIBLE, H_CIBLE), 0)
    g = grille.load()
    presents = {(y - y0, z - z0) for (_, y, z) in voxels}
    ny = max(presents)[0] + 1 if presents else 1
    nz = max(p[1] for p in presents) + 1 if presents else 1
    for (y, z) in presents:
        # remise a l'echelle vers la grille de reference
        cx = min(L_CIBLE - 1, int(y * L_CIBLE / ny))
        cz = min(H_CIBLE - 1, int(z * H_CIBLE / nz))
        g[cx, H_CIBLE - 1 - cz] = 255
    return grille


def comparer(a, b):
    pa, pb = a.load(), b.load()
    manquant = en_trop = commun = 0
    par_tranche = {}
    for x in range(L_CIBLE):
        for y in range(H_CIBLE):
            va, vb = pa[x, y] > 0, pb[x, y] > 0
            if va and vb:
                commun += 1
            elif va:
                manquant += 1
                par_tranche.setdefault(x * 10 // L_CIBLE, []).append("M")
            elif vb:
                en_trop += 1
                par_tranche.setdefault(x * 10 // L_CIBLE, []).append("T")
    total = L_CIBLE * H_CIBLE
    return manquant, en_trop, commun, total, par_tranche


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--vox", required=True)
    p.add_argument("--planche", default=PLANCHE)
    a = p.parse_args()
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    from generer_voxel_v2 import lire_vox
    _, voxels = lire_vox(a.vox)
    # le modele est ecrit en (X largeur, Y hauteur, Z longueur) : retour au repere interne
    V = {(x, z, y): c for (x, y, z), c in voxels.items()}
    dessin = profil_planche(a.planche)
    modele = profil_modele(V)
    manquant, en_trop, commun, total, tranches = comparer(dessin, modele)
    dessine = manquant + commun
    print(f"{os.path.basename(a.vox)} : {len(V)} voxels")
    print(f"  profil du dessin : {dessine} cellules pleines")
    print(f"  recouvertes      : {commun}  ({100.0 * commun / max(1, dessine):.1f} % du dessin)")
    print(f"  MANQUANT         : {manquant} ({100.0 * manquant / max(1, dessine):.1f} %)")
    print(f"  EN TROP          : {en_trop}")
    print("  par dixieme de longueur (avant -> arriere), M = manque, T = en trop :")
    for d in range(10):
        liste = tranches.get(d, [])
        print(f"    {d:2d}/10 : {liste.count('M'):3d} M · {liste.count('T'):3d} T")
    return 0


if __name__ == "__main__":
    sys.exit(main())
