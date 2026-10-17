#!/usr/bin/env python3
"""Rendu GIF 3D : rotation du modele voxel, une image par pas d'angle.

Le cadre est FIXE d'une image a l'autre (il est cale sur le rayon de la boule englobante du
modele) : sinon chaque image se recadrerait sur sa propre silhouette et le char gigoterait au lieu
de tourner proprement.

Usage :
  rendu_gif.py --vox voxels/leman_russ_exact.vox --sortie rotation.gif \\
               [--images 48] [--elevation 20] [--echelle 6] [--sens 1]
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from apercu_vox import FACES, LUMIERE, Vue          # meme projection que l'apercu fixe
from generer_voxel_v2 import PALETTE, lire_vox      # meme lecture de .vox, meme palette
from PIL import Image, ImageDraw


def cadre(voxels):
    """Centre et rayon de la boule englobante : c'est ce qui fixe le cadre de toutes les images."""
    xs = [p[0] for p in voxels]
    ys = [p[1] for p in voxels]
    zs = [p[2] for p in voxels]
    cx = (min(xs) + max(xs)) / 2.0
    cy = (min(ys) + max(ys)) / 2.0
    cz = (min(zs) + max(zs)) / 2.0
    rayon = max(((x - cx) ** 2 + (y - cy) ** 2 + (z - cz) ** 2) ** 0.5 for x, y, z in voxels)
    return (cx, cy, cz), rayon


def rendre(voxels, palette, vue, centre, rayon, px, fond=(248, 248, 246)):
    """Une image, cadre fixe : les coordonnees sont ramenees au centre puis mises a l'echelle."""
    cote = int(2 * rayon * px) + 2 * px + 4
    img = Image.new("RGB", (cote, cote), fond)
    d = ImageDraw.Draw(img)
    cx, cy, cz = centre
    faces = []
    for (x, y, z), couleur in voxels.items():
        for normale, coins in FACES.items():
            if not vue.profondeur(normale):
                continue
            if (x + normale[0], y + normale[1], z + normale[2]) in voxels:
                continue
            pts = [vue.point((x + a - cx, y + b - cy, z + c - cz)) for a, b, c in coins]
            faces.append((sum(q[2] for q in pts) / 4.0, palette[couleur - 1], pts, normale))
    faces.sort(key=lambda f: -f[0])
    demi = cote / 2.0
    for _, rgb, pts, normale in faces:
        f = LUMIERE[normale]
        teinte = (int(rgb[0] * f), int(rgb[1] * f), int(rgb[2] * f))
        d.polygon([(demi + p[0] * px, demi - p[1] * px) for p in pts], fill=teinte)
    return img


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--vox", required=True)
    p.add_argument("--sortie", default="rotation.gif")
    p.add_argument("--images", type=int, default=48, help="nombre d'images sur le tour complet")
    p.add_argument("--elevation", type=float, default=20.0)
    p.add_argument("--echelle", type=int, default=6, help="pixels par voxel")
    p.add_argument("--duree", type=int, default=90, help="millisecondes par image")
    p.add_argument("--sens", type=float, default=1.0, help="1 = sens horaire, -1 = inverse")
    a = p.parse_args()

    _, voxels = lire_vox(a.vox)
    palette = PALETTE
    centre, rayon = cadre(voxels)
    print(f"{len(voxels)} voxels · boule englobante de rayon {rayon:.1f} voxels "
          f"· {a.images} images a {a.elevation:.0f} deg d'elevation")
    images = []
    for i in range(a.images):
        az = a.sens * 360.0 * i / a.images
        images.append(rendre(voxels, palette, Vue(az, a.elevation), centre, rayon, a.echelle))
    images[0].save(a.sortie, save_all=True, append_images=images[1:],
                   duration=a.duree, loop=0, optimize=True)
    print(f"{a.sortie} ecrit · {images[0].size[0]}x{images[0].size[1]} px "
          f"· {os.path.getsize(a.sortie) // 1024} Ko")


if __name__ == "__main__":
    main()
