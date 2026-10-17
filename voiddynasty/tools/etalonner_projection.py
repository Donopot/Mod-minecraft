#!/usr/bin/env python3
"""Etalonner la camera du rendu 3D de Donopot en projetant le modele v1 (dont on sait qu'il est bon).

Le rendu est une rotation du char autour d'un axe vertical : 61 vues. On projette les voxels du
modele v1 dans chaque vue et on compare la BOITE ENGLOBANTE projetee a celle de la silhouette.

Pourquoi la boite et pas l'aire : un score d'aire est truque par une projection trop petite —
tous les points se refugient au centre du masque et le score monte. La boite, elle, punit dans
les deux sens (trop grande comme trop petite). On l'evalue sur la coque convexe des voxels et
non sur les 50 000 points : les extremes d'une projection sont des sommets de la coque, donc
la boite est EXACTE et le calcul 200 fois plus rapide.

Parametres ajustes :
  x0, y0   axe de rotation (le char n'est pas forcement pose au centre du plateau)
  pxm      echelle, pixels par metre
  cx, cy   centre de l'image (ou se projette l'axe)
  elev     elevation de la camera, en degres
  phase    orientation du char sur la vue 0
  sens     sens de rotation
  u_sens, v_sens  orientation des axes de l'image (un miroir n'est pas absorbable par le sens)

Sortie : voxels/etalonnage_rendu.json + une planche de superposition a l'oeil.

Usage :
  etalonner_projection.py --gif docs/references/lemunruss-rendu-3d.gif \
      --masques voxels/masques_rendu.npz [--vues 0,15,23,30,45,60]
"""
import argparse
import json
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageSequence

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import leman_russ_v1 as MODELE

try:
    from scipy.spatial import ConvexHull
except ImportError:
    ConvexHull = None

Z_CIBLE = 2.2                      # hauteur que vise la camera (2 m : le centre du char)


def points_modele():
    """Les voxels du modele v1 en metres absolus, ET leur coque convexe.

    Deux jeux distincts, ne pas les confondre : la COQUE sert a la boite englobante (les extremes
    d'une projection sont des sommets de coque, donc la boite est exacte et le calcul rapide) ;
    les VOXELS servent au recouvrement et a la superposition (rasteriser 37 sommets ne dessine
    pas une silhouette — c'est l'erreur qui a donne un recouvrement de 0 %).
    """
    V = MODELE.construire(list(MODELE.PIECES))
    P = np.array([[(x + 0.5) * MODELE.VOX, (y + 0.5) * MODELE.VOX, (z + 0.5) * MODELE.VOX]
                  for (x, y, z) in V], dtype=np.float64)
    coque = P[np.unique(ConvexHull(P).vertices)] if ConvexHull is not None else P
    return P, coque


def boites_masques(masques):
    out = []
    for m in masques:
        ys, xs = np.nonzero(m)
        out.append((float(xs.min()), float(ys.min()), float(xs.max()), float(ys.max())))
    return np.array(out, dtype=np.float64)


def boites_projetees(P, p, n):
    """Boite englobante projetee de chaque vue (x0, y0, x1, y1) en pixels."""
    elev = math.radians(p["elev"])
    R = P[:, :2] - np.array([p["x0"], p["y0"]])
    z = P[:, 2] - Z_CIBLE
    out = np.empty((n, 4))
    for i in range(n):
        a = p["phase"] + p["sens"] * 2 * math.pi * i / n
        ca, sa = math.cos(a), math.sin(a)
        x = R[:, 0] * ca - R[:, 1] * sa
        y = R[:, 0] * sa + R[:, 1] * ca
        u = p["u_sens"] * y
        v = p["v_sens"] * (-x * math.sin(elev) + z * math.cos(elev))
        out[i] = (p["cx"] + u.min() * p["pxm"], p["cy"] - v.max() * p["pxm"],
                  p["cx"] + u.max() * p["pxm"], p["cy"] - v.min() * p["pxm"])
    return out


def ecart(P, cible, p):
    return float(np.abs(boites_projetees(P, p, len(cible)) - cible).mean())


def projetees(P, p, n, i):
    """Points projetes (u, v) en pixels pour la vue i."""
    elev = math.radians(p["elev"])
    R = P[:, :2] - np.array([p["x0"], p["y0"]])
    z = P[:, 2] - Z_CIBLE
    a = p["phase"] + p["sens"] * 2 * math.pi * i / n
    ca, sa = math.cos(a), math.sin(a)
    x = R[:, 0] * ca - R[:, 1] * sa
    y = R[:, 0] * sa + R[:, 1] * ca
    u = p["u_sens"] * y
    v = p["v_sens"] * (-x * math.sin(elev) + z * math.cos(elev))
    return p["cx"] + u * p["pxm"], p["cy"] - v * p["pxm"]


def etalonner(P, cible, depart):
    """Balayage des choix discrets, puis descente par coordonnee."""
    n = len(cible)
    meilleur, meilleur_ecart = None, None
    for sens in (-1, 1):
        for us in (-1, 1):
            for vs in (-1, 1):
                for phase in np.linspace(0, 2 * math.pi, 24, endpoint=False):
                    p = dict(depart, sens=sens, u_sens=us, v_sens=vs, phase=float(phase))
                    e = ecart(P, cible, p)
                    if meilleur_ecart is None or e < meilleur_ecart:
                        meilleur, meilleur_ecart = p, e
    print(f"  choix discrets : sens {meilleur['sens']:+d} · u {meilleur['u_sens']:+d} · "
          f"v {meilleur['v_sens']:+d} · phase {math.degrees(meilleur['phase']):.0f} deg "
          f"-> ecart {meilleur_ecart:.1f} px")

    pas = {"x0": 0.6, "y0": 0.6, "pxm": 8.0, "cx": 40.0, "cy": 40.0, "elev": 4.0, "phase": 0.2}
    for tour in range(4):
        progresse = False
        for nom, d in pas.items():
            for signe in (-1, 1):
                essai = dict(meilleur)
                essai[nom] = meilleur[nom] + signe * d * (0.5 ** tour)
                e = ecart(P, cible, essai)
                if e < meilleur_ecart - 1e-4:
                    meilleur, meilleur_ecart, progresse = essai, e, True
        print(f"  tour {tour + 1} : ecart {meilleur_ecart:6.1f} px"
              + ("" if progresse else "  (stabilise)"))
        if not progresse:
            break
    return meilleur, meilleur_ecart


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--gif", required=True)
    p.add_argument("--masques", default="voxels/masques_rendu.npz")
    p.add_argument("--sortie", default="voxels/etalonnage_rendu.json")
    p.add_argument("--planche", default="/opt/data/workspace")
    p.add_argument("--vues", default="0,15,23,30,45,60")
    a = p.parse_args()

    d = np.load(a.masques)
    masques = d["masques"]
    n = len(masques)
    cible = boites_masques(masques)
    print(f"{n} silhouettes chargees · boite vue 0 : {cible[0]}")

    P, coque = points_modele()
    print(f"modele v1 : {len(P)} voxels · coque convexe {len(coque)} sommets (pour la boite)")

    depart = {"x0": MODELE.SIZE_X * MODELE.VOX / 2,
              "y0": (MODELE.Y_AV + MODELE.Y_AR) / 2 * MODELE.VOX,
              "pxm": 117.0, "cx": (cible[:, 0].min() + cible[:, 2].max()) / 2,
              "cy": (cible[:, 1].min() + cible[:, 3].max()) / 2,
              "elev": 12.0, "phase": 0.0, "sens": 1, "u_sens": 1, "v_sens": 1}
    print(f"depart : axe ({depart['x0']:.2f}, {depart['y0']:.2f}) m · {depart['pxm']:.0f} px/m · "
          f"centre ({depart['cx']:.0f}, {depart['cy']:.0f}) · elev {depart['elev']:.0f} deg")
    print(f"  ecart de depart : {ecart(coque, cible, depart):.1f} px")

    p_final, e_final = etalonner(coque, cible, depart)
    print(f"\netalonnage : axe ({p_final['x0']:.2f}, {p_final['y0']:.2f}) m · "
          f"{p_final['pxm']:.2f} px/m · centre ({p_final['cx']:.1f}, {p_final['cy']:.1f}) · "
          f"elev {p_final['elev']:.1f} deg · phase {math.degrees(p_final['phase']) % 360:.0f} deg · "
          f"sens {p_final['sens']:+d}")
    print(f"  ecart moyen sur les 61 vues : {e_final:.1f} px "
          f"({e_final / p_final['pxm'] * 100:.1f} cm sur les bords de la silhouette)")
    print("  (cet ecart melange l'erreur de camera ET les differences de forme du modele v1 :"
          "\n   le modele mesure 8,88 m d'encombrement la ou le rendu en montre davantage)")

    boites = boites_projetees(coque, p_final, n)
    err = np.abs(boites - cible)
    print("\n  vues les plus justes / les plus fausses (ecart max des 4 bords, px) :")
    rang = np.argsort(err.max(1))
    for k in list(rang[:3]) + list(rang[-3:]):
        print(f"    vue {k:2d} : projete {boites[k][0]:6.0f} {boites[k][1]:6.0f} "
              f"{boites[k][2]:6.0f} {boites[k][3]:6.0f} | masque {cible[k][0]:6.0f} "
              f"{cible[k][1]:6.0f} {cible[k][2]:6.0f} {cible[k][3]:6.0f} | err {err[k].max():5.0f}")

    # recouvrement reel : silhouette projetee contre masque, vue par vue
    import scipy.ndimage as ndi
    ious = []
    for i in range(n):
        u, v = projetees(P, p_final, n, i)
        H, W = masques[i].shape
        ix = np.round(u).astype(int)
        iy = np.round(v).astype(int)
        ok = (ix >= 0) & (ix < W) & (iy >= 0) & (iy < H)
        raster = np.zeros((H, W), bool)
        raster[iy[ok], ix[ok]] = True
        raster = ndi.binary_closing(raster, np.ones((7, 7), bool))
        raster = ndi.binary_fill_holes(raster)
        inter = (raster & masques[i]).sum()
        union = (raster | masques[i]).sum()
        ious.append(inter / union if union else 0.0)
    ious = np.array(ious)
    print(f"\n  recouvrement (IoU) modele v1 / rendu : moyenne {ious.mean() * 100:.1f} % · "
          f"min {ious.min() * 100:.1f} % (vue {int(ious.argmin())}) · "
          f"max {ious.max() * 100:.1f} % (vue {int(ious.argmax())})")

    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    with open(a.sortie, "w") as f:
        json.dump({k: (float(v) if not isinstance(v, int) else v) for k, v in p_final.items()}
                  | {"ecart_px": e_final, "z_cible": Z_CIBLE, "iou_moyen": float(ious.mean())},
                  f, indent=1)
    print(f"  {a.sortie} ecrit")

    # superposition a l'oeil : trame en gris, contour du masque en bleu, modele projete en rouge
    vues = [int(q) for q in a.vues.split(",") if int(q) < n]
    images = [np.asarray(f.copy().convert("L")) for f in ImageSequence.Iterator(Image.open(a.gif))]
    W, H = images[0].shape[1] // 2, images[0].shape[0] // 2
    planche = Image.new("RGB", (W * 2, H * ((len(vues) + 1) // 2)), (20, 20, 24))
    for k, i in enumerate(vues):
        trame = Image.fromarray(np.clip(images[i].astype(int) * 2, 0, 255).astype(np.uint8))
        trame = trame.convert("RGB").resize((W, H))
        bord = ndi.binary_dilation(masques[i], np.ones((3, 3), bool)) & ~ndi.binary_erosion(
            masques[i], np.ones((3, 3), bool))
        trame.paste(Image.new("RGB", (W, H), (60, 120, 255)), (0, 0),
                    Image.fromarray((bord * 255).astype(np.uint8)).resize((W, H)).point(
                        lambda q: 255 if q else 0))
        u, v = projetees(P, p_final, n, i)
        dr = ImageDraw.Draw(trame)
        for uu, vv in zip(u[::3], v[::3]):
            x, y = uu / 2, vv / 2
            if 0 <= x < W and 0 <= y < H:
                dr.point((x, y), fill=(255, 40, 40))
        dr.text((6, 6), f"vue {i} · IoU {ious[i] * 100:.1f} %", fill=(255, 255, 255))
        planche.paste(trame, ((k % 2) * W, (k // 2) * H))
    chemin = f"{a.planche}/VOID-DYNASTY-etalonnage-rendu.png"
    planche.save(chemin)
    print(f"  {chemin}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
