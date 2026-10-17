#!/usr/bin/env python3
"""Masques du char dans le rendu 3D de Donopot (61 images) — version 2, par clarte.

Ce qui a echoue avant, et pourquoi (mesure, pas supposition) :
  * « difference avec un fond fixe » : le char couvre a lui seul la moitie de l'image
    (bbox 1113x759 px sur 1280x960) et son ombre portee TOUCHE ses chenilles, donc elle
    entre dans la meme tache connexe : le masque rempli avale l'ombre, le bas devient un
    gros arc qui n'existe pas dans le char ;
  * « difference positive » (plus clair que le fond) : le char est troue a l'interieur,
    ses creux sombres (interstices des maillons, ombres sous la jupe) passent sous le seuil.

Ce qui marche : le fond est SOMBRE (coins a 23-34 sur 255) et le char est CLAIR (jusqu'a
254). Un simple seuil de clarte separe le char du decor ET de son ombre (l'ombre est plus
sombre que le sol, donc tres en dessous du seuil). Les creux sombres A L'INTERIEUR du char
sont ensuite rebouches par remplissage des trous, ce qui ne touche pas au contour.

Le contour de la silhouette est la seule chose qui contraint le decoupage : c'est donc lui
qu'il faut juste.

Usage :
  masques_rendu.py --gif docs/references/lemunruss-rendu-3d.gif [--seuil 95] [--sortie ...]
"""
import argparse
import os
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageSequence

try:
    from scipy import ndimage
    A_SCIPY = True
except ImportError:                                    # repli sans scipy
    A_SCIPY = False


def charger(gif):
    return np.stack([np.asarray(f.convert("L"), dtype=np.uint8)
                     for f in ImageSequence.Iterator(Image.open(gif))])


def _etendre(m, limite=4000):
    """Dilatation 4-connexes d'un masque (repli quand scipy est absent)."""
    for _ in range(limite):
        e = m.copy()
        e[1:, :] |= m[:-1, :]
        e[:-1, :] |= m[1:, :]
        e[:, 1:] |= m[:, :-1]
        e[:, :-1] |= m[:, 1:]
        if e.sum() == m.sum():
            return e
        m = e
    return m


def _remplir(m):
    if A_SCIPY:
        return ndimage.binary_fill_holes(m)
    libre = ~m
    ext = np.zeros_like(m)
    ext[0, :] = libre[0, :]
    ext[-1, :] = libre[-1, :]
    ext[:, 0] = libre[:, 0]
    ext[:, -1] = libre[:, -1]
    return ~_etendre(ext) & ~libre | m


def masque(image, seuil=95, fermeture=3, aire_mini=150):
    """Le char : clair, referme les interstices fins, garde les vraies taches, trous rebouches."""
    m = image > seuil
    if A_SCIPY:
        m = ndimage.binary_closing(m, structure=np.ones((fermeture, fermeture), bool))
        etiquettes, n = ndimage.label(m, structure=np.ones((3, 3), int))
        if n:
            tailles = ndimage.sum(m, etiquettes, range(1, n + 1))
            garder = np.zeros(n + 1, bool)
            garder[1:] = tailles >= aire_mini
            m = garder[etiquettes]
        m = ndimage.binary_fill_holes(m)
    else:
        m = _remplir(m)
    return m


def mesures(m):
    ys, xs = np.nonzero(m)
    if len(ys) == 0:
        return None
    return {"largeur": int(xs.max() - xs.min() + 1), "hauteur": int(ys.max() - ys.min() + 1),
            "aire": int(m.sum()), "x0": int(xs.min()), "x1": int(xs.max()),
            "y0": int(ys.min()), "y1": int(ys.max())}


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--gif", required=True)
    p.add_argument("--seuil", type=int, default=95)
    p.add_argument("--fermeture", type=int, default=3)
    p.add_argument("--aire-mini", type=int, default=150)
    p.add_argument("--mesures", default="/opt/data/workspace")
    p.add_argument("--npz", default="voxels/masques_rendu.npz")
    p.add_argument("--apercu-vues", default="0,15,23,30,45")
    a = p.parse_args()

    A = charger(a.gif)
    n, h, l = A.shape
    print(f"{n} images · {l}x{h} px · seuil de clarte {a.seuil}"
          f" · scipy {'oui' if A_SCIPY else 'NON (repli lent)'}")

    masques = np.stack([masque(A[i], a.seuil, a.fermeture, a.aire_mini) for i in range(n)])
    tailles = [int(m.sum()) for m in masques]
    print(f"aire des silhouettes : {min(tailles)} a {max(tailles)} px "
          f"(moyenne {sum(tailles) // n})")

    ms = [mesures(m) for m in masques]
    print("\n  vue   largeur  hauteur     aire     x0    x1    y0    y1")
    for i in range(n):
        if i % 5 == 0 or i in (23, 60):
            d = ms[i]
            print(f"  {i:3d}   {d['largeur']:6d}  {d['hauteur']:6d}  {d['aire']:7d}"
                  f"  {d['x0']:5d} {d['x1']:5d} {d['y0']:5d} {d['y1']:5d}")
    plat = [m["largeur"] for m in ms]
    print(f"\n  largeur : min {min(plat)} (le char de bout) · max {max(plat)} "
          f"(le char de profil = sa LONGUEUR) · vue {plat.index(max(plat))}")
    haut = [m["hauteur"] for m in ms]
    print(f"  hauteur : min {min(haut)} · max {max(haut)} · vue {haut.index(max(haut))}")

    os.makedirs(os.path.dirname(a.npz) or ".", exist_ok=True)
    np.savez_compressed(a.npz, masques=masques, seuil=a.seuil)
    print(f"\n{a.npz} ecrit ({os.path.getsize(a.npz) // 1024} Ko)")

    vues = [int(x) for x in a.apercu_vues.split(",") if int(x) < n]
    W, H = l // 2, h // 2
    planche = Image.new("L", (W * 2, H * len(vues)), 255)
    for r, i in enumerate(vues):
        planche.paste(Image.fromarray(A[i]).resize((W, H)), (0, r * H))
        planche.paste(Image.fromarray((masques[i] * 255).astype(np.uint8)).resize((W, H)), (W, r * H))
    d = ImageDraw.Draw(planche)
    for r, i in enumerate(vues):
        mm = ms[i]
        d.text((6, r * H + 6), f"vue {i} : {mm['largeur']}x{mm['hauteur']} px", fill=255)
    chemin = f"{a.mesures}/VOID-DYNASTY-masques-clarte.png"
    planche.save(chemin)
    print(f"{chemin}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
