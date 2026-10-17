#!/usr/bin/env python3
"""Verifie la SILHOUETTE d'un modele pose : ses morceaux se touchent-ils, apres transformation ?

Un mob dont les os ne se rejoignent pas apparaitra en morceaux dans le jeu — et aucun test de harnais
ne le dira, parce que le harnais ne regarde pas la geometrie. C'est le genre de defaut qui ne se voit
qu'a l'oeil... a condition de savoir ou regarder. Cet outil le dit en chiffres.

    /opt/data/voxel-venv/bin/python tools/verifier_silhouette.py [dossier ou fichier] [--marge 1.0]

Sortie : pour chaque os, sa boite englobante (en blocs) et le nombre d'os qui ne touchent PERSONNE.
Un os isole n'est pas forcement un defaut (une arme tenue, un tentacule) : c'est un endroit a regarder.
"""
from __future__ import annotations

import argparse
import glob
import itertools
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import rendu_modeles as rm  # noqa: E402


def dist_surfaces(cubes_a, cubes_b, pas=6):
    """Distance minimale entre deux ensembles de cubes, echantillonnes dans leur volume."""
    def points(cubes):
        pts = []
        for c in cubes:
            bas, haut = c.min(axis=0), c.max(axis=0)
            axes = [np.linspace(bas[i], haut[i], max(2, min(10, int(abs(haut[i] - bas[i]) // 4) + 2)))
                    for i in range(3)]
            for x in axes[0]:
                for y in axes[1]:
                    for z in axes[2]:
                        pts.append((x, y, z))
        return np.array(pts) if pts else np.zeros((0, 3))

    pa, pb = points(cubes_a), points(cubes_b)
    if len(pa) == 0 or len(pb) == 0:
        return float("inf")
    d = float("inf")
    for debut in range(0, len(pa), 400):
        bloc = pa[debut:debut + 400]
        d = min(d, float(np.sqrt(((bloc[:, None, :] - pb[None, :, :]) ** 2).sum(axis=2)).min()))
    return d


def boites_par_os(chemin: str) -> dict:
    """Boite englobante de chaque os, une fois la pose appliquee (rotations + hierarchie)."""
    modeles = rm.charger_modele(chemin)
    matrices = modeles["matrices_os"]
    par_os: dict = {}
    for cube in modeles["cubes"]:
        coins = rm.rotation_bedrock(rm.coins_cube(cube), cube.get("rotation"), cube.get("pivot"))
        m = matrices.get(cube["os"])
        if m is not None:
            coins = [tuple((m @ np.array([*p, 1.0]))[:3]) for p in coins]
        a = np.array(coins)
        bas, haut = a.min(axis=0), a.max(axis=0)
        if cube["os"] in par_os:
            bas = np.minimum(par_os[cube["os"]][0], bas)
            haut = np.maximum(par_os[cube["os"]][1], haut)
        par_os[cube["os"]] = (bas, haut)
    return par_os


def se_touchent(a, b, marge: float) -> bool:
    """Deux boites se touchent si elles se recouvrent sur les TROIS axes."""
    return all(a[0][i] - marge <= b[1][i] and b[0][i] - marge <= a[1][i] for i in range(3))


def examiner(chemin: str, marge: float = 1.0) -> dict:
    par_os = boites_par_os(chemin)
    noms = list(par_os)
    isoles = [n for n in noms
              if not any(se_touchent(par_os[n], par_os[m], marge) for m in noms if m != n)]
    tous = np.array([v for b in par_os.values() for v in b])
    taille = (tous.max(axis=0) - tous.min(axis=0)) / 16.0
    return {"os": len(noms), "isoles": isoles, "boites": par_os, "taille": taille}


def main() -> int:
    p = argparse.ArgumentParser(description="Verifie qu'un modele pose tient d'un seul tenant.")
    p.add_argument("chemin", help="fichier .geo.json ou dossier de modeles")
    p.add_argument("--marge", type=float, default=1.0, help="tolerance de contact, en unites du modele")
    p.add_argument("--detail", action="store_true", help="affiche la boite de chaque os")
    p.add_argument("--liaisons", action="store_true",
                   help="distance REELLE entre les cubes de deux os differents (les boites englobantes mentent)")
    a = p.parse_args()

    fichiers = (sorted(glob.glob(os.path.join(a.chemin, "*.geo.json")))
                if os.path.isdir(a.chemin) else [a.chemin])
    total_isoles = 0
    if a.liaisons:
        for chemin in fichiers:
            nom = os.path.basename(chemin).replace(".geo.json", "")
            print(f"\n=== liaisons entre os : {nom} ===")
            modeles = rm.charger_modele(chemin)
            matrices = modeles["matrices_os"]
            par_os: dict = {}
            for c in modeles["cubes"]:
                coins = rm.rotation_bedrock(rm.coins_cube(c), c.get("rotation"), c.get("pivot"))
                m = matrices.get(c["os"])
                if m is not None:
                    coins = [tuple((m @ np.array([*p, 1.0]))[:3]) for p in coins]
                par_os.setdefault(c["os"], []).append(np.array(coins))
            distances = []
            for a1, b1 in itertools.combinations(par_os, 2):
                d = min(dist_surfaces(par_os[a1], par_os[b1]) for _ in [0])
                distances.append((d, a1, b1))
            distances.sort()
            for d, a1, b1 in distances[:4]:
                print(f"   {a1:14} <-> {b1:14} {d / 16:6.2f} bloc(s)")
            proches = sum(1 for d, _, _ in distances if d / 16 < 0.5)
            print(f"   {proches}/{len(distances)} liaisons à moins de 0,5 bloc"
                  + ("   <<< DES MORCEAUX SONT DETACHES" if proches < len(distances) else ""))
        return 0
    total_isoles = 0
    for chemin in fichiers:
        nom = os.path.basename(chemin).replace(".geo.json", "")
        r = examiner(chemin, a.marge)
        marque = "  " if not r["isoles"] else "!!"
        print(f"{marque} {nom:24} {r['os']:3} os · {r['taille'][0]:5.1f} x {r['taille'][1]:5.1f} x "
              f"{r['taille'][2]:5.1f} blocs · {len(r['isoles'])} os isole(s)"
              + (f" -> {', '.join(r['isoles'])}" if r["isoles"] else ""))
        if a.detail:
            for n, (bas, haut) in r["boites"].items():
                print(f"      {n:22} X[{bas[0]/16:7.2f},{haut[0]/16:7.2f}] "
                      f"Y[{bas[1]/16:7.2f},{haut[1]/16:7.2f}] Z[{bas[2]/16:7.2f},{haut[2]/16:7.2f}]")
        total_isoles += len(r["isoles"])
    print(f"\n{len(fichiers)} modele(s) examine(s), {total_isoles} os isole(s) au total.")
    print("Un os isole n'est pas forcement un defaut : c'est un endroit a regarder de pres.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
