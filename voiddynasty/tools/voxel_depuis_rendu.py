#!/usr/bin/env python3
"""Affiner le modele avec TOUTES les images du rendu de Donopot (decoupage multi-vues).

Principe (reconstruction par silhouettes, ou « visual hull ») : un voxel n'appartient au char que
s'il tombe DANS la silhouette du char sur TOUS les angles de la rotation. Un voxel qui sort de la
silhouette sur une seule vue est forcement en dehors du char : on le retire. Chaque image
supplementaire enleve donc de la matiere fausse — c'est exactement pourquoi les 61 valent mieux
que 2.

Traitement des images :
  * fond = MINIMUM pixel a pixel sur les 61 images (le fond ne bouge pas, le char tourne) ;
  * char = difference POSITIVE avec ce fond : le char est clair, son ombre portee est plus SOMBRE
    que le sol, donc le signe de la difference les separe ;
  * plus grande tache connexe, pour ecarter les reflets.

Usage :
  voxel_depuis_rendu.py --gif docs/references/lemunruss-rendu-3d.gif \\
      --sortie voxels/leman_russ_carve.vox [--elevation 12] [--seuil-vues 0.9]
"""
import argparse
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from PIL import Image, ImageChops, ImageSequence

VOX = 0.12
SIZE_X, SIZE_Z, SIZE_L = 41, 37, 80            # largeur, hauteur, longueur (0,12 m par voxel)
PALETTE = [(45, 75, 40, 255), (195, 170, 125, 255), (35, 35, 40, 255),
           (150, 150, 155, 255), (180, 40, 40, 255), (90, 85, 75, 255)]


def silhouettes(images, seuil=22):
    """Le char dans chaque image : difference positive avec le fond fixe, plus grande tache."""
    fond = images[0]
    for im in images[1:]:
        fond = ImageChops.darker(fond, im)
    sortie = []
    for im in images:
        d = ImageChops.subtract(im, fond).point(lambda v: 255 if v > seuil else 0)
        l, h = d.size
        px = d.load()
        vus = bytearray(l * h)
        meilleure = []
        for y0 in range(0, h, 3):
            for x0 in range(0, l, 3):
                if px[x0, y0] and not vus[y0 * l + x0]:
                    tas, pile = [], [(x0, y0)]
                    vus[y0 * l + x0] = 1
                    while pile:
                        x, y = pile.pop()
                        tas.append((x, y))
                        for dx, dy in ((2, 0), (-2, 0), (0, 2), (0, -2), (2, 2), (-2, -2)):
                            nx, ny = x + dx, y + dy
                            if 0 <= nx < l and 0 <= ny < h and not vus[ny * l + nx] and px[nx, ny]:
                                vus[ny * l + nx] = 1
                                pile.append((nx, ny))
                    if len(tas) > len(meilleure):
                        meilleure = tas
        boite = None
        if meilleure:
            xs = [q[0] for q in meilleure]
            ys = [q[1] for q in meilleure]
            boite = (min(xs), min(ys), max(xs), max(ys))
        sortie.append((meilleure, boite, l, h))
    return sortie


def calibrer(sils):
    """Echelle et centre communs : le char tourne sur place, sa hauteur ne change pas."""
    hauteurs = [b[3] - b[1] for _, b, _, _ in sils if b]
    haut_max = max(hauteurs)
    echelle = haut_max / (SIZE_Z * VOX)          # pixels par metre, cale sur la hauteur du char
    centres = [((b[0] + b[2]) / 2.0, (b[1] + b[3]) / 2.0) for _, b, _, _ in sils if b]
    cx = sum(c[0] for c in centres) / len(centres)
    cy = sum(c[1] for c in centres) / len(centres)
    return echelle, cx, cy


def decouper(sils, echelle, cx, cy, elevation, seuil_vues):
    """Un voxel survit s'il tombe dans la silhouette sur au moins `seuil_vues` des vues."""
    el = math.radians(elevation)
    vues = []
    for i, (pts, boite, l, h) in enumerate(sils):
        if not boite:
            continue
        az = 2 * math.pi * i / len(sils)
        px = Image.new("1", (l, h), 0)
        p = px.load()
        for x, y in pts:
            p[x, y] = 1
        vues.append((math.cos(az), math.sin(az), p, l, h))   # p = acces aux pixels
    garde = {}
    mini = int(len(vues) * seuil_vues)
    for z in range(SIZE_Z):
        zc = (z + 0.5) * VOX - SIZE_Z * VOX / 2        # hauteur centree
        for y in range(SIZE_L):
            yc = (y + 0.5) * VOX
            for x in range(SIZE_X):
                xc = (x + 0.5) * VOX - SIZE_X * VOX / 2
                dedans = 0
                for ca, sa, p, l, h in vues:
                    u = -xc * sa + yc * ca             # axe horizontal de l'image
                    v = -xc * ca * math.sin(el) - yc * sa * math.sin(el) + zc * math.cos(el)
                    ix = int(cx + u * -echelle) if False else int(cx - u * echelle)
                    iy = int(cy - v * echelle)
                    if 0 <= ix < l and 0 <= iy < h and p[ix, iy]:
                        dedans += 1
                        if len(vues) - (len(vues) - dedans) < mini:
                            break
                if dedans >= mini:
                    garde[(x, y, z)] = 1
    return garde


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--gif", required=True)
    p.add_argument("--sortie", default="voxels/leman_russ_carve.vox")
    p.add_argument("--elevation", type=float, default=12.0)
    p.add_argument("--seuil-vues", type=float, default=0.9)
    a = p.parse_args()
    images = [f.copy().convert("L") for f in ImageSequence.Iterator(Image.open(a.gif))]
    print(f"{len(images)} images")
    sils = silhouettes(images)
    tailles = [len(s[0]) for s in sils]
    print(f"  silhouettes : {min(tailles)} a {max(tailles)} pixels "
          f"(la plus grande = le char de profil)")
    echelle, cx, cy = calibrer(sils)
    print(f"  echelle {echelle:.2f} px/m · centre image ({cx:.0f}, {cy:.0f}) · "
          f"elevation supposee {a.elevation:.0f} deg")
    V = decouper(sils, echelle, cx, cy, a.elevation, a.seuil_vues)
    print(f"  voxels retenus : {len(V)}")
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    from voxel_donopot import ecrire_vox
    # repere interne (x largeur, y longueur, z hauteur) -> ecriture (x, longueur, hauteur)
    taille = ecrire_vox(a.sortie, {(x, y, z): 1 for (x, y, z) in V})
    print(f"  {a.sortie} ecrit · grille {taille}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
