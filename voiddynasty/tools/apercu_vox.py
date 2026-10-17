#!/usr/bin/env python3
"""Apercu d'un fichier .voxel : rendu isometrique en PNG, sans dependance 3D.

Le rendu LIT le fichier .vox (il ne regarde pas les donnees en memoire du generateur) : ce qu'on
voit a l'ecran est donc bien ce que contient le fichier.

Usage :
  apercu_vox.py --vox voxels/leman_russ_model.vox --sortie apercu.png --vues profil,troisquarts,face
"""
import argparse
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generer_voxel_lemanruss import lire_vox          # meme lecture que la verification
from PIL import Image, ImageDraw

# les six directions de face : normale, et les 4 coins en fractions de voxel
FACES = {
    (1, 0, 0): ((1, 0, 0), (1, 0, 1), (1, 1, 1), (1, 1, 0)),
    (-1, 0, 0): ((0, 0, 0), (0, 1, 0), (0, 1, 1), (0, 0, 1)),
    (0, 1, 0): ((0, 1, 0), (0, 1, 1), (1, 1, 1), (1, 1, 0)),
    (0, -1, 0): ((0, 0, 0), (1, 0, 0), (1, 0, 1), (0, 0, 1)),
    (0, 0, 1): ((0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)),
    (0, 0, -1): ((0, 0, 0), (0, 1, 0), (1, 1, 0), (1, 0, 0)),
}
LUMIERE = {(0, 1, 0): 1.00, (1, 0, 0): 0.74, (-1, 0, 0): 0.60,
           (0, 0, 1): 0.52, (0, 0, -1): 0.46, (0, -1, 0): 0.34}


class Vue:
    """Projection orthographique : azimut autour de la verticale, elevation."""

    def __init__(self, azimut_deg, elevation_deg):
        self.az = math.radians(azimut_deg)
        self.el = math.radians(elevation_deg)

    def point(self, p):
        x, y, z = p
        ca, sa = math.cos(self.az), math.sin(self.az)
        X = x * ca + z * sa
        Z = -x * sa + z * ca
        ce, se = math.cos(self.el), math.sin(self.el)
        return X, y * ce - Z * se, y * se + Z * ce      # (droite, haut, profondeur)

    def profondeur(self, normale):
        """Une face est visible si sa normale pointe vers la camera."""
        ca, sa = math.cos(self.az), math.sin(self.az)
        ce, se = math.cos(self.el), math.sin(self.el)
        g = (-sa * ce, se, ca * ce)                     # sens d'eloignement
        return sum(n * q for n, q in zip(normale, g)) < 0


def rendre(voxels, vue, palette, px_par_voxel=6, fond=(250, 250, 248)):
    faces = []
    for (x, y, z), couleur in voxels.items():
        for normale, coins in FACES.items():
            if not vue.profondeur(normale):
                continue
            voisin = (x + normale[0], y + normale[1], z + normale[2])
            if voisin in voxels:                        # face masquee par le voxel voisin
                continue
            pts = [vue.point((x + cx, y + cy, z + cz)) for cx, cy, cz in coins]
            prof = sum(q[2] for q in pts) / 4.0
            faces.append((prof, palette[couleur - 1], pts, normale))
    if not faces:
        raise SystemExit("aucune face visible : fichier vide ?")
    faces.sort(key=lambda f: -f[0])                     # les plus lointaines d'abord

    xs = [p[0] for f in faces for p in f[2]]
    ys = [p[1] for f in faces for p in f[2]]
    marge = 2 * px_par_voxel
    larg = int((max(xs) - min(xs)) * px_par_voxel) + 2 * marge
    haut = int((max(ys) - min(ys)) * px_par_voxel) + 2 * marge
    img = Image.new("RGB", (larg, haut), fond)
    d = ImageDraw.Draw(img)
    ox, oy = min(xs), max(ys)

    def ecran(p):
        return ((p[0] - ox) * px_par_voxel + marge,
                (oy - p[1]) * px_par_voxel + marge)

    for _, couleur, pts, normale in faces:
        f = LUMIERE[normale]
        rgb = (int(couleur[0] * f), int(couleur[1] * f), int(couleur[2] * f))
        d.polygon([ecran(p) for p in pts], fill=rgb)
    return img


# l'avant du char est vers les Z croissants : une vue de face se prend donc a azimut 180
VUES = {"profil": (90, 0), "troisquarts": (135, 20), "face": (180, 8), "dessus": (200, 62),
        "arriere": (0, 12)}


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--vox", required=True)
    p.add_argument("--sortie", default="apercu_vox.png")
    p.add_argument("--vues", default="troisquarts")
    p.add_argument("--echelle", type=int, default=6, help="pixels par voxel")
    a = p.parse_args()
    _, taille, voxels, palette = lire_vox(a.vox)
    if not any(c[3] for c in palette):
        raise SystemExit("palette illisible dans le fichier")
    images = []
    for nom in a.vues.split(","):
        az, el = VUES[nom.strip()]
        images.append(rendre(voxels, Vue(az, el), palette, a.echelle))
        print(f"  vue {nom} : {images[-1].size[0]}x{images[-1].size[1]} px")
    # planche : les vues cote a cote sur fond commun
    h = max(i.size[1] for i in images)
    l = sum(i.size[0] for i in images) + 8 * (len(images) - 1)
    feuille = Image.new("RGB", (l, h), (250, 250, 248))
    x = 0
    for i in images:
        feuille.paste(i, (x, h - i.size[1]))
        x += i.size[0] + 8
    feuille.save(a.sortie)
    print(f"{a.sortie} ecrit ({feuille.size[0]}x{feuille.size[1]})")


if __name__ == "__main__":
    main()
