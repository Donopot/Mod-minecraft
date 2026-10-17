#!/usr/bin/env python3
"""Construire le modele voxel a partir du RENDU 3D de Donopot (pas du croquis).

Donopot : « non base toi sur le rendu que je t'ai donne ». Son fichier est une rotation de 61
images d'un Leman Russ rendu en 3D. On en extrait deux vues, on releve leurs silhouettes et on les
croise — meme methode que pour le croquis, mais la source est nettement meilleure : le fond est
sombre et le modele clair, donc la silhouette se detache seule, sans rafistolage.

Un voxel n'existe que s'il est dans les DEUX silhouettes. Les cotes cibles restent celles de la
planche technique (caisse 7,08 m, largeur 4,86, hauteur 4,42, canon 3,07).

Usage :
  copier_rendu.py --gif docs/references/lemunruss-rendu-3d.gif --sortie voxels/leman_russ_rendu.vox
                  [--profil 0] [--face 23] [--resolution 0.12]
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from PIL import Image, ImageChops, ImageSequence, ImageFilter

CAISSE_M, LARGEUR_M, HAUTEUR_M, CANON_M = 7.08, 4.86, 4.42, 3.07
SEUIL_MODELE = 90          # au-dessus de ce niveau de gris : le modele (le fond est sombre)
# palette de Donopot
PALETTE = [(45, 75, 40, 255), (195, 170, 125, 255), (35, 35, 40, 255),
           (150, 150, 155, 255), (180, 40, 40, 255), (90, 85, 75, 255)]
VERT, BEIGE, CHENILLE, ACIER, DETAIL, OMBRE = 1, 2, 3, 4, 5, 6


def fond_du_rendu(images):
    """Le fond fixe du rendu : le minimum, pixel par pixel, sur toutes les images du tour.

    Le fond (sol, degrade, ombre) ne bouge pas ; le char, si. Le minimum de chaque pixel sur le
    tour retient donc le fond seul — c'est ce qui permet de separer le char de sa propre ombre
    portee, que le simple seuil de luminosite laissait passer.
    """
    fond = images[0]
    for im in images[1:]:
        fond = ImageChops.darker(fond, im)
    return fond


def silhouette(img, fond=None):
    """Le modele : ce qui est plus clair que le fond, puis plus grande forme et remplissage."""
    if fond is not None:
        diff = ImageChops.subtract(img, fond)
        m = diff.point(lambda v: 255 if v > 18 else 0)
        return m
    _ = ImageFilter
    l, h = img.size
    px = img.load()
    fond = bytearray(l * h)
    pile = []
    for x in range(l):
        for y in (0, h - 1):
            if px[x, y] <= SEUIL_MODELE and not fond[y * l + x]:
                fond[y * l + x] = 1
                pile.append((x, y))
    for y in range(h):
        for x in (0, l - 1):
            if px[x, y] <= SEUIL_MODELE and not fond[y * l + x]:
                fond[y * l + x] = 1
                pile.append((x, y))
    while pile:
        x, y = pile.pop()
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < l and 0 <= ny < h and not fond[ny * l + nx] and px[nx, ny] <= SEUIL_MODELE:
                fond[ny * l + nx] = 1
                pile.append((nx, ny))
    m = Image.new("L", (l, h), 0)
    mm = m.load()
    for y in range(h):
        for x in range(l):
            if not fond[y * l + x]:
                mm[x, y] = 255
    return m


def plus_grande(m, seuil=64):
    """Ne garder que la plus grande tache blanche."""
    l, h = m.size
    px = m.load()
    vus = bytearray(l * h)
    meilleure = []
    for y0 in range(h):
        for x0 in range(l):
            if px[x0, y0] > seuil and not vus[y0 * l + x0]:
                tas, pile = [], [(x0, y0)]
                vus[y0 * l + x0] = 1
                while pile:
                    x, y = pile.pop()
                    tas.append((x, y))
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < l and 0 <= ny < h and not vus[ny * l + nx] and px[nx, ny] > seuil:
                            vus[ny * l + nx] = 1
                            pile.append((nx, ny))
                if len(tas) > len(meilleure):
                    meilleure = tas
    g = Image.new("L", (l, h), 0)
    gg = g.load()
    for x, y in meilleure:
        gg[x, y] = 255
    return g


def grille(m, nx, ny):
    """Boite serree puis mise a la grille voulue."""
    boite = m.getbbox()
    m = m.crop(boite).resize((nx, ny), Image.LANCZOS)
    return m.point(lambda v: 255 if v >= 128 else 0), boite


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--gif", required=True)
    p.add_argument("--sortie", default="voxels/leman_russ_rendu.vox")
    p.add_argument("--profil", type=int, default=0)
    p.add_argument("--face", type=int, default=23)
    p.add_argument("--resolution", type=float, default=0.12)
    a = p.parse_args()

    toutes = [f.copy().convert("L") for f in ImageSequence.Iterator(Image.open(a.gif))]
    fond = fond_du_rendu(toutes)
    images = toutes
    res = a.resolution
    nx_l, ny_h = round(CAISSE_M / res), round(HAUTEUR_M / res)
    nx_w = round(LARGEUR_M / res)

    sp = silhouette(images[a.profil], fond)
    sf = silhouette(images[a.face], fond)
    # plus grande forme : les taches isolees (reflets, poussiere du rendu) sont ecartees
    sp, sf = plus_grande(sp), plus_grande(sf)
    gp, bp = grille(sp, nx_l, ny_h)
    gf, bf = grille(sf, nx_w, ny_h)
    print(f"vue de profil (image {a.profil}) : boite {bp} -> {nx_l} x {ny_h} cellules"
          f"   rapport {(bp[2]-bp[0])/(bp[3]-bp[1]):.2f} (planche {CAISSE_M/HAUTEUR_M:.2f})")
    print(f"vue de face   (image {a.face}) : boite {bf} -> {nx_w} x {ny_h} cellules"
          f"   rapport {(bf[2]-bf[0])/(bf[3]-bf[1]):.2f} (planche {LARGEUR_M/HAUTEUR_M:.2f})")

    pp, pf = gp.load(), gf.load()
    V = {}
    for z in range(nx_l):
        for y in range(ny_h):
            if not pp[z, ny_h - 1 - y]:
                continue
            for x in range(nx_w):
                if pf[x, ny_h - 1 - y]:
                    V[(x, y, z)] = 1

    # couleur : on relit la nuance du rendu dans la cellule, puis on garde la teinte DOMINANTE du
    # voisinage (les ombres portees du rendu tachent des cellules isolees)
    src = images[a.profil].crop(bp).resize((nx_l, ny_h), Image.LANCZOS)
    ps = src.load()
    teintes = {}
    for z in range(nx_l):
        for y in range(ny_h):
            nuance = ps[z, ny_h - 1 - y]
            teintes[(y, z)] = (VERT if nuance > 150 else (ACIER if nuance > 95 else CHENILLE))
    lisse = {}
    for z in range(nx_l):
        for y in range(ny_h):
            v = [teintes[(yy, zz)] for zz in range(max(0, z - 1), min(nx_l, z + 2))
                 for yy in range(max(0, y - 1), min(ny_h, y + 2))]
            lisse[(y, z)] = max(set(v), key=v.count)
    V = {(x, y, z): lisse[(y, z)] for (x, y, z) in V}

    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    from voxel_donopot import ecrire_vox
    # le repere ici est (x largeur, y hauteur, z longueur) ; l'ecriture attend (x, longueur, hauteur)
    taille = ecrire_vox(a.sortie, {(x, z, y): c for (x, y, z), c in V.items()})
    print(f"{a.sortie} ecrit · grille {taille} (X largeur, Y hauteur, Z longueur) · {len(V)} voxels")
    presents = {(y, z) for (_, y, z) in V}
    acc = sum(1 for z in range(nx_l) for y in range(ny_h)
              if bool(pp[z, ny_h - 1 - y]) == ((y, z) in presents))
    print(f"  accord avec la silhouette du rendu : {acc}/{nx_l * ny_h} cellules")
    return 0


if __name__ == "__main__":
    sys.exit(main())
