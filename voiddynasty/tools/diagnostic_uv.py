#!/usr/bin/env python3
"""Diagnostic d'UV : quelle part de ce qu'on affiche est du VIOLET (magenta) ou du vide ?

Le magenta est la couleur-repere des textures de modeles : Blockbench et les atlas s'en servent pour
marquer une zone non peinte. Donc si un rendu montre du violet, ce n'est pas un gout : c'est la
preuve que les UV echantillonnent une zone vide de la texture. Cet outil mesure cette part, modele par
modele, et compare les dispositions possibles d'une boite depliee.

Il ne rend rien : il lit les rectangles d'UV et echantillonne la texture directement. C'est plus
rapide et plus precis qu'une capture.

    /opt/data/voxel-venv/bin/python tools/diagnostic_uv.py DOSSIER_MODELES DOSSIER_TEXTURES
    /opt/data/voxel-venv/bin/python tools/diagnostic_uv.py ... --essayer-ordres   # boites depliees
"""
from __future__ import annotations

import argparse
import glob
import itertools
import json
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import rendu_modeles as rm  # noqa: E402

FACES = list(rm.FACES.keys())


def lire_texture(chemin):
    return np.asarray(Image.open(chemin).convert("RGBA"), dtype=np.int16)


def est_magenta(px) -> bool:
    """Le repere de zone non peinte : magenta franc."""
    r, g, b, a = px
    return a > 40 and r > 180 and b > 140 and g < 90 and abs(int(r) - int(b)) < 110


def echantillonner(uv, nom_face, taille_cube, texture, pas=2):
    """Tous les pixels qu'une face va lire. Renvoie (magenta, transparents, total)."""
    rect = rm.rectangle_uv(uv, nom_face, taille_cube)
    if rect is None:
        return 0, 0, 0
    u0, v0, u1, v1 = rect
    h, w = texture.shape[:2]
    i0, i1 = int(round(min(u0, u1))), int(round(max(u0, u1)))
    j0, j1 = int(round(min(v0, v1))), int(round(max(v0, v1)))
    i0, i1 = max(0, i0), min(w, i1)
    j0, j1 = max(0, j0), min(h, j1)
    if i1 <= i0 or j1 <= j0:
        return 0, 0, 0
    zone = texture[j0:j1:pas, i0:i1:pas]
    if zone.size == 0:
        return 0, 0, 0
    magenta = int(sum(1 for px in zone.reshape(-1, 4) if est_magenta(px)))
    transparents = int((zone[:, :, 3] < 40).sum())
    return magenta, transparents, int(zone.shape[0] * zone.shape[1])


def examiner(modele, texture):
    """Bilan d'un modele : part de magenta et de transparent dans tout ce qu'il affiche."""
    magenta = transparents = total = 0
    pires = []
    for cube in modele["cubes"]:
        cm = ct = tot = 0
        for face in FACES:
            m, t, n = echantillonner(cube["uv"], face, cube["taille"], texture)
            cm += m; ct += t; tot += n
        magenta += cm; transparents += ct; total += tot
        if cm or ct:
            pires.append((cm + ct, cube["os"], cm, ct))
    pires.sort(reverse=True)
    return {"magenta": magenta, "transparents": transparents, "total": max(1, total),
            "pires": pires[:3]}


def pixels_echantillonnes(modele, texture, pas=1):
    """Tous les pixels que le modele lit vraiment, toutes faces confondues."""
    pixels = []
    for cube in modele["cubes"]:
        for face in FACES:
            rect = rm.rectangle_uv(cube["uv"], face, cube["taille"])
            if cube.get("miroir"):
                rect = rm.miroir_uv(rect)
            if rect is None:
                continue
            u0, v0, u1, v1 = rect
            h, w = texture.shape[:2]
            i0, i1 = max(0, int(round(min(u0, u1)))), min(w, int(round(max(u0, u1))))
            j0, j1 = max(0, int(round(min(v0, v1)))), min(h, int(round(max(v0, v1))))
            if i1 <= i0 or j1 <= j0:
                continue
            pixels.append(texture[j0:j1:pas, i0:i1:pas].reshape(-1, 4))
    return np.concatenate(pixels) if pixels else np.zeros((0, 4), dtype=np.int16)


def bilan_couleurs(modele, texture) -> dict:
    """Un modele qui sort GRIS se voit ici : teinte uniforme et peu saturee sur les pixels lus."""
    px = pixels_echantillonnes(modele, texture)
    if len(px) == 0:
        return {"pixels": 0}
    opaques = px[px[:, 3] > 40]
    if len(opaques) == 0:
        return {"pixels": 0}
    ecart_max = np.maximum(np.abs(opaques[:, 0] - opaques[:, 1]),
                           np.abs(opaques[:, 1] - opaques[:, 2]))
    return {
        "pixels": int(len(opaques)),
        "moyenne": opaques[:, :3].mean(axis=0),
        "part_gris": float((ecart_max < 12).mean()),
        "magenta_vif": int(((opaques[:, 0] > 170) & (opaques[:, 2] > 120)
                            & (opaques[:, 1] < 110)).sum()),
    }


def est_boite(chemin) -> bool:
    geo = json.load(open(chemin, encoding="utf-8"))["minecraft:geometry"][0]
    for b in geo.get("bones", []):
        for c in b.get("cubes", []):
            if isinstance(c.get("uv"), list):
                return True
    return False


def main() -> int:
    p = argparse.ArgumentParser(description="Ou est le violet dans les rendus, et pourquoi.")
    p.add_argument("modeles")
    p.add_argument("textures")
    p.add_argument("--couleurs", action="store_true",
                   help="ce que chaque modele echantillonne VRAIMENT : couleur moyenne, part de gris, magenta vif")
    p.add_argument("--modele", help="detailler un seul modele : quel cube, quelle face, combien de magenta")
    p.add_argument("--essayer-ordres", action="store_true",
                   help="pour les boites depliees, comparer les dispositions possibles")
    a = p.parse_args()

    fichiers = sorted(glob.glob(os.path.join(a.modeles, "*.geo.json")))
    if a.modele:
        chemins = [c for c in fichiers if a.modele in os.path.basename(c)]
        for chemin in chemins:
            nom = os.path.basename(chemin).replace(".geo.json", "")
            texture, _ = rm.chercher_texture(a.textures, nom)
            tex = np.asarray(texture.convert("RGBA"), dtype=np.int16)
            modele = rm.charger_modele(chemin)
            plat = tex.reshape(-1, 4)
            n_tot = int(sum(1 for px in plat if est_magenta(px)))
            print(f"{nom} : la texture compte {n_tot} pixels magenta "
                  f"({100.0 * n_tot / len(plat):.2f} % de la surface)")
            for cube in modele["cubes"]:
                for face in FACES:
                    m, t, n = echantillonner(cube["uv"], face, cube["taille"], tex)
                    if m or t:
                        rect = rm.rectangle_uv(cube["uv"], face, cube["taille"])
                        print(f"   os={cube['os']:14} face={face:6} rect={tuple(round(v) for v in rect)}"
                              f" magenta={m} vide={t} sur {n}")
        return 0
    if a.couleurs:
        print(f"{'modele':24} {'pixels':>8} {'couleur moyenne':>18} {'gris':>7}  magenta vif "
              f"(modele vs texture)")
        for chemin in fichiers:
            nom = os.path.basename(chemin).replace(".geo.json", "")
            texture, _ = rm.chercher_texture(a.textures, nom)
            if texture is None:
                continue
            tex = np.asarray(texture.convert("RGBA"), dtype=np.int16)
            b = bilan_couleurs(rm.charger_modele(chemin), tex)
            if not b.get("pixels"):
                print(f"{nom:24} aucun pixel echantillonne  <<<")
                continue
            m = b["moyenne"]
            # comparaison utile : la part de magenta du MODELE contre celle de TOUTE la texture.
            # Si le modele en lit beaucoup plus que la texture n'en contient, c'est l'UV. Sinon,
            # c'est le dessin.
            plat = tex.reshape(-1, 4)
            opaques = plat[plat[:, 3] > 40]
            mag_tex = int(((opaques[:, 0] > 170) & (opaques[:, 2] > 120)
                           & (opaques[:, 1] < 110)).sum())
            part_modele = 100.0 * b["magenta_vif"] / b["pixels"]
            part_tex = 100.0 * mag_tex / max(1, len(opaques))
            note = "  <<< UV SUSPECT" if part_modele > 2 * part_tex + 0.5 else ""
            alerte = "  <<< GRIS" if b["part_gris"] > 0.6 else ""
            print(f"{nom:24} {b['pixels']:8} ({m[0]:5.0f},{m[1]:5.0f},{m[2]:5.0f})      "
                  f"{100 * b['part_gris']:6.1f}% {b['magenta_vif']:8} "
                  f"({part_modele:4.1f}% du modele vs {part_tex:4.1f}% de la texture){note}{alerte}")
        return 0
    print(f"{'modele':24} {'UV':6} {'magenta':>9} {'vide':>7}  pire cube")
    lignes = []
    for chemin in fichiers:
        nom = os.path.basename(chemin).replace(".geo.json", "")
        texture, _ = rm.chercher_texture(a.textures, nom)
        if texture is None:
            continue
        tex = np.asarray(texture.convert("RGBA"), dtype=np.int16)
        modele = rm.charger_modele(chemin)
        r = examiner(modele, tex)
        forme = "boite" if est_boite(chemin) else "facette"
        pm = 100.0 * r["magenta"] / r["total"]
        pv = 100.0 * r["transparents"] / r["total"]
        pire = r["pires"][0] if r["pires"] else None
        detail = (f"{pire[1]} (magenta {pire[2]}, vide {pire[3]})" if pire else "")
        alerte = "  <<<" if pm > 3 or pv > 25 else ""
        print(f"{nom:24} {forme:6} {pm:8.2f}% {pv:6.2f}%  {detail}{alerte}")
        lignes.append((pm + pv, nom, pm, pv))

    lignes.sort(reverse=True)
    print("\nles trois pires :", ", ".join(f"{n} ({m:.1f}% magenta, {v:.1f}% vide)"
                                           for _, n, m, v in lignes[:3]))

    boites = [c for c in fichiers if est_boite(c)]
    if a.essayer_ordres and boites:
        print(f"\n=== dispositions comparées sur {len(boites)} boîte(s) dépliée(s) ===")
        ordres = [("east", "north", "west", "south"), ("east", "north", "south", "west"),
                  ("west", "north", "east", "south"), ("north", "east", "west", "south"),
                  ("west", "south", "east", "north")]
        scores = []
        for ordre in ordres:
            rm.ORDRE_BOITE = ordre
            m = t = tot = 0
            for chemin in boites:
                nom = os.path.basename(chemin).replace(".geo.json", "")
                texture, _ = rm.chercher_texture(a.textures, nom)
                if texture is None:
                    continue
                tex = np.asarray(texture.convert("RGBA"), dtype=np.int16)
                r = examiner(rm.charger_modele(chemin), tex)
                m += r["magenta"]; t += r["transparents"]; tot += r["total"]
            scores.append((100.0 * (m + t) / max(1, tot), ordre, 100.0 * m / max(1, tot)))
        for score, ordre, pm in sorted(scores):
            print(f"   {' > '.join(ordre):34} magenta {pm:6.2f}%  ·  magenta+vide {score:6.2f}%")
        print("   la disposition qui échantillonne le MOINS de zones vides est la bonne.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
