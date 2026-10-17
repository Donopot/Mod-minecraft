#!/usr/bin/env python3
"""Affiner le modele v1 avec TOUTES les images du rendu de Donopot — comparaison de forme.

Pourquoi pas le decoupage direct (« visual hull ») : il exige une camera exacte, et la camera
est impossible a etalonner finement sur une image (l'enveloppe « boite » des largeurs laisse
53 px de residu : un char n'est pas une boite). Une erreur d'echelle de 5 % ferait un hull de
travers sans qu'on le voie.

Ce qui marche : comparer FORME contre FORME. En recalant a chaque vue les deux silhouettes sur
leur propre largeur, l'echelle absolue du rendu (arbitraire : l'artiste a rendu un char de
10 m, pas 7,08 m) disparait. Ce qui reste est exactement ce que le rendu peut apprendre :
la forme. Ce qui est en trop chez moi, ce qui me manque, et ou.

Deux jeux de nombres, sur les 61 vues :
  * par voxel : sur combien de vues son point projete tombe dans la silhouette du rendu.
    Un voxel constamment dehors n'existe pas sur le rendu -> a retirer ;
  * par vue : l'aire en trop et l'aire manquante, en pourcentage de la silhouette du rendu.

Ce qui est ajuste : l'orientation du char sur la vue 0 et le sens de rotation (balayage sur
l'accord moyen). Ce qui vient de la mesure : elevation 11 deg (enveloppe des hauteurs, residu
10,7 px), axe au centre du char (le centre de la silhouette ne bouge que de 27 px sur un tour).

Usage :
  comparer_rendu.py [--masques voxels/masques_rendu.npz] [--vues 0,15,23,30,45,60]
                    [--fraction 0.5] [--planche /opt/data/workspace]
"""
import argparse
import json
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw
import scipy.ndimage as ndi

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import leman_russ_v2 as MODELE

ELEV = 11.0                 # degres, mesure sur l'enveloppe des hauteurs des 61 vues
RAYON = 7                   # un voxel de 0,12 m fait ~15 px a l'echelle du rendu


def points_modele():
    V = MODELE.construire(list(MODELE.PIECES))
    P = np.array([[(x + 0.5) * MODELE.VOX, (y + 0.5) * MODELE.VOX, (z + 0.5) * MODELE.VOX]
                  for (x, y, z) in V], dtype=np.float64)
    cle = np.array([[x, y, z] for (x, y, z) in V], dtype=np.int32)
    return P, cle


def cacher(P, p, n, i, centre=None):
    """Points projetes (u, v) en pixels de l'image, axe au centre du char.

    Le centre de l'image est ajoute ICI : sans lui la projection est centree sur zero, tombe
    hors cadre et il ne reste qu'un fragment de silhouette — ce qui a produit une « echelle »
    d'alignement de 2,1 et un recouvrement sans signification.
    """
    cx = centre[0] if centre else 640.0
    cy = centre[1] if centre else 480.0
    R = P[:, :2] - np.array([p["x0"], p["y0"]])
    z = P[:, 2] - p["z_cible"]
    a = p["phase"] + p["sens"] * 2 * math.pi * i / n
    ca, sa = math.cos(a), math.sin(a)
    el = math.radians(p["elev"])
    x = R[:, 0] * ca - R[:, 1] * sa
    y = R[:, 0] * sa + R[:, 1] * ca
    u = p["u_sens"] * y
    v = p["v_sens"] * (-x * math.sin(el) + z * math.cos(el))
    return cx + u * p["pxm"], cy - v * p["pxm"]


def rasteriser(u, v, forme, rayon=RAYON):
    """Un disque par point (l'epaisseur du voxel), pour une silhouette pleine et franche.

    `rayon` plus petit pendant le balayage d'orientation : le disque n'y sert qu'a comparer des
    formes, le resultat final est calcule au rayon complet.
    """
    H, W = forme
    m = np.zeros((H, W), bool)
    ix = np.round(u).astype(int)
    iy = np.round(v).astype(int)
    for ox in range(-rayon, rayon + 1):
        for oy in range(-rayon, rayon + 1):
            if ox * ox + oy * oy > rayon * rayon:
                continue
            X, Y = ix + ox, iy + oy
            ok = (X >= 0) & (X < W) & (Y >= 0) & (Y < H)
            m[Y[ok], X[ok]] = True
    return m


def boite_de(m):
    ys, xs = np.nonzero(m)
    if len(ys) == 0:
        return None
    return xs.min(), ys.min(), xs.max(), ys.max()


def accorder(modele_m, rendu_m):
    """Recale le modele sur le rendu par la largeur, puis par le centre. Echelle absolue effacee."""
    b1, b2 = boite_de(modele_m), boite_de(rendu_m)
    if not b1 or not b2:
        return None, None
    ech = (b2[2] - b2[0] + 1) / max(1, b1[2] - b1[0] + 1)
    dy = ((b2[1] + b2[3]) - (b1[1] + b1[3])) / 2
    dx = ((b2[0] + b2[2]) - (b1[0] + b1[2])) / 2
    return ech, (dx, dy)


def projeter_recale(u, v, ech, dec, forme, rayon=RAYON):
    return rasteriser(u * ech + dec[0], v * ech + dec[1], forme, rayon)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--masques", default="voxels/masques_rendu.npz")
    p.add_argument("--sortie", default="voxels/accord_rendu.json")
    p.add_argument("--planche", default="/opt/data/workspace")
    p.add_argument("--vues", default="0,15,23,30,45,60")
    p.add_argument("--fraction", type=float, default=0.5)
    a = p.parse_args()

    masques = np.load(a.masques)["masques"]
    n, H, W = masques.shape
    formes = (H, W)
    P, cle = points_modele()
    print(f"{n} vues de {W}x{H} · modele v1 : {len(P)} voxels")

    base = {"x0": MODELE.SIZE_X * MODELE.VOX / 2,
            "y0": (min(q[1] for q in cle) + max(q[1] for q in cle) + 1) / 2 * MODELE.VOX,
            "pxm": 118.0, "z_cible": 2.2, "elev": ELEV, "u_sens": 1, "v_sens": 1}
    print(f"axe ({base['x0']:.2f}, {base['y0']:.2f}) m · {base['pxm']:.0f} px/m · "
          f"elevation {ELEV:.0f} deg (mesuree)")

    # --- orientation : balayage phase / sens de rotation / sens de l'axe horizontal.
    # Une mauvaise orientation ne peut plus gagner en etirant la boite : l'echelle d'alignement
    # doit rester plausible (le modele est projete a l'echelle du rendu, donc ~1).
    ech_vues = list(range(0, n, 5))
    P_inf = P[::4]
    meilleur = None
    for sens in (1, -1):
        for u_sens in (1, -1):
            for k in range(0, 360, 15):
                essai = dict(base, sens=sens, u_sens=u_sens, phase=math.radians(k))
                total, vues_ok = 0.0, 0
                for i in ech_vues:
                    u, v = cacher(P_inf, essai, n, i)
                    m = rasteriser(u, v, formes, rayon=3)
                    ech, dec = accorder(m, masques[i])
                    if ech is None or not 0.7 < ech < 1.5:
                        continue
                    r = projeter_recale(u, v, ech, dec, formes, rayon=3)
                    inter = (r & masques[i]).sum()
                    union = (r | masques[i]).sum()
                    total += inter / union if union else 0.0
                    vues_ok += 1
                total = total / len(ech_vues)
                if meilleur is None or total > meilleur[0]:
                    meilleur = (total, sens, u_sens, k, vues_ok)
    accord, sens, u_sens, deg, vues_ok = meilleur
    print(f"orientation : vue 0 a {deg} deg · sens {sens:+d} · axe horizontal {u_sens:+d} · "
          f"accord moyen {accord * 100:.1f} % ({vues_ok}/{len(ech_vues)} vues retenues)")
    base = dict(base, sens=sens, u_sens=u_sens, phase=math.radians(deg))

    # --- echelle figee : la MEDIANE des 61 vues.
    # Recaler vue par vue rend le verdict instable : un baril mince au bout du char passe dedans
    # ou dehors selon 5 % d'echelle. On fige l'echelle sur la mediane, seuls la forme et le
    # decalage varient ensuite. C'est la seule facon que le resultat veuille dire quelque chose.
    echelles = []
    P_inf2 = P[::4]
    for i in range(n):
        ui, vi = cacher(P_inf2, base, n, i)
        mi = rasteriser(ui, vi, formes, rayon=3)
        e, _ = accorder(mi, masques[i])
        if e is not None and 0.6 < e < 1.6:
            echelles.append(e)
    ech_figee = float(np.median(echelles))
    print(f"echelle figee (mediane de {len(echelles)} vues) : {ech_figee:.4f} "
          f"[{min(echelles):.3f} .. {max(echelles):.3f}]")

    # --- passe complete : votes par voxel + aires en trop / manquantes par vue
    votes = np.zeros(len(P), np.int32)
    lignes = []
    images = {}
    for i in range(n):
        u, v = cacher(P, base, n, i)
        m = rasteriser(u, v, formes, rayon=3)
        _, dec = accorder(m, masques[i])
        ech = ech_figee
        r = projeter_recale(u, v, ech, dec, formes)
        ix = np.clip(np.round(u * ech + dec[0]).astype(int), 0, W - 1)
        iy = np.clip(np.round(v * ech + dec[1]).astype(int), 0, H - 1)
        votes += masques[i][iy, ix]
        inter = (r & masques[i]).sum()
        a_r, a_m = r.sum(), masques[i].sum()
        union = (r | masques[i]).sum()
        lignes.append((i, a_r, a_m, inter, a_r - inter, a_m - inter,
                       inter / union if union else 0.0, ech))
        if i in [int(q) for q in a.vues.split(",")]:
            images[i] = (masques[i], r)

    frac = votes / n
    t = np.array(lignes)
    print(f"\naccord (IoU) sur les 61 vues : moyenne {t[:, 6].mean() * 100:.1f} % · "
          f"min {t[:, 6].min() * 100:.1f} % (vue {int(t[t[:, 6].argmin(), 0])}) · "
          f"max {t[:, 6].max() * 100:.1f} % (vue {int(t[t[:, 6].argmax(), 0])})")
    print(f"aire en trop : moyenne {t[:, 4].mean() / t[:, 2].mean() * 100:+.1f} % de la "
          f"silhouette · aire manquante : {t[:, 5].mean() / t[:, 2].mean() * 100:+.1f} %")
    print(f"echelle appliquee (rendu/modele) : {t[:, 7].mean():.3f} "
          f"(le rendu est {t[:, 7].mean():.2f}x plus grand que le modele)")

    print(f"\nrepartition des voxels selon le nombre de vues ou ils tombent dans le rendu :")
    for lo, hi in ((0.0, 0.1), (0.1, 0.3), (0.3, 0.5), (0.5, 0.7), (0.7, 0.9), (0.9, 1.01)):
        k = ((frac >= lo) & (frac < hi)).sum()
        print(f"  {lo * 100:3.0f}-{hi * 100:3.0f} % des vues : {k:6d} voxels "
              f"({k / len(frac) * 100:5.1f} %)")

    # ou sont ces voxels ? par piece et par zone de la caisse
    print("\nvoxels douteux (dans moins de 50 % des vues) par piece :")
    douteux = frac < a.fraction
    for nom in MODELE.PIECES:
        V = {}
        MODELE.PIECES[nom](V)
        idx = [q for q in range(len(cle)) if tuple(cle[q]) in V]
        if not idx:
            continue
        d = douteux[idx].mean() * 100
        print(f"  {nom:14s} : {d:5.1f} % de la piece ({douteux[idx].sum():6d} voxels)")

    zones = [("canon (y<30)", cle[:, 1] < 30), ("avant de caisse (30-45)", (cle[:, 1] >= 30) & (cle[:, 1] < 45)),
             ("milieu (45-65)", (cle[:, 1] >= 45) & (cle[:, 1] < 65)), ("arriere (65-80)", (cle[:, 1] >= 65) & (cle[:, 1] < 80)),
             ("fin arriere (>=80)", cle[:, 1] >= 80)]
    print("\nvoxels douteux par zone de longueur :")
    for nom, sel in zones:
        if sel.sum():
            print(f"  {nom:22s} : {douteux[sel].mean() * 100:5.1f} % ({douteux[sel].sum():6d} / {sel.sum()})")
    print("\nvoxels douteux par hauteur (hauteur du voxel en metres) :")
    for z0 in range(0, int(MODELE.SIZE_Z * MODELE.VOX / 0.5) * 1, 1):
        sel = (P[:, 2] >= z0 * 0.5) & (P[:, 2] < (z0 + 1) * 0.5)
        if sel.sum():
            print(f"  z {z0 * 0.5:4.1f}-{(z0 + 1) * 0.5:4.1f} m : {douteux[sel].mean() * 100:5.1f} % "
                  f"({douteux[sel].sum():6d} / {sel.sum()})")

    with open(a.sortie, "w") as f:
        json.dump({"elev": ELEV, "phase_deg": deg, "sens": sens, "accord_iou": float(t[:, 6].mean()),
                   "echelle_rendu_sur_modele": float(t[:, 7].mean()),
                   "aire_en_trop_pct": float(t[:, 4].mean() / t[:, 2].mean() * 100),
                   "aire_manquante_pct": float(t[:, 5].mean() / t[:, 2].mean() * 100),
                   "voxels_douteux": int(douteux.sum()), "voxels_total": int(len(frac))},
                  f, indent=1)
    print(f"\n{a.sortie} ecrit")

    # --- planche : bleu = rendu, rouge = modele recale, blanc = accord
    vues = sorted(images)
    Wv, Hv = W // 2, H // 2
    planche = Image.new("RGB", (Wv * 2, Hv * ((len(vues) + 1) // 2)), (15, 15, 18))
    for k, i in enumerate(vues):
        masque, r = images[i]
        img = np.zeros((H, W, 3), np.uint8)
        img[masque] = (60, 120, 255)
        img[r] = (255, 60, 60)
        img[masque & r] = (245, 245, 245)
        t_bis = Image.fromarray(img).resize((Wv, Hv))
        ImageDraw.Draw(t_bis).text((6, 6), f"vue {i} · accord {t[i, 6] * 100:.0f} %",
                                   fill=(255, 255, 0))
        planche.paste(t_bis, ((k % 2) * Wv, (k // 2) * Hv))
    chemin = f"{a.planche}/VOID-DYNASTY-accord-forme.png"
    planche.save(chemin)
    print(f"{chemin}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
