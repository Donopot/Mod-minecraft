#!/usr/bin/env python3
"""Quel sens de rotation recolle le modele ? Mesure des contacts entre cubes, variante par variante.

Pourquoi cette mesure : devant les rendus des quatre sens de rotation, je n'arrivais pas a trancher a
l'oeil — le modele restait disloque dans tous les cas. Un modele de char est fait de cubes qui SE
TOUCHENT : la caisse, les chenilles, la tourelle, le canon sont jointifs. Le bon sens de rotation est
donc celui qui maximise le nombre de paires de cubes en contact. C'est mesurable, et ca ne depend pas
de mon jugement.

    python3 tools/choisir_sens_rotation.py --modele lemunruss [--contact 0.02]
"""
from __future__ import annotations

import argparse
import itertools
import json
import os
import subprocess
import sys
import tempfile

import numpy as np

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def coins_cube(cube):
    """Les 8 coins d'un cube, rotation EVENTUELLE appliquee autour de son origine de rotation."""
    o = np.array(cube["origin"], dtype=float)
    t = np.array(cube["size"], dtype=float)
    coins = np.array([[o[0] + (t[0] if i & 1 else 0),
                       o[1] + (t[1] if (i >> 1) & 1 else 0),
                       o[2] + (t[2] if (i >> 2) & 1 else 0)] for i in range(8)])
    r = cube.get("rotation")
    if not r:
        return coins
    # MEME rotation que le rendu du projet : une mesure qui tourne autrement que le rendu ne mesure
    # pas ce qu'on voit.
    sys.path.insert(0, os.path.join(RACINE, "tools"))
    import rendu_modeles as RM
    return np.array(RM.rotation_bedrock([tuple(c) for c in coins], r, r.get("origin")))


def surfaces_opposees(coins_a, coins_b, tolerance):
    """Y a-t-il un contact ? On regarde si les deux volumes se chevauchent ou se touchent."""
    amin, amax = coins_a.min(axis=0), coins_a.max(axis=0)
    bmin, bmax = coins_b.min(axis=0), coins_b.max(axis=0)
    recouvrement = np.minimum(amax, bmax) - np.maximum(amin, bmin)
    return int(np.all(recouvrement > -tolerance))


def compter_contacts(chemin, tolerance):
    geo = json.load(open(chemin, encoding="utf-8"))["minecraft:geometry"][0]
    cubes = [c for b in geo["bones"] for c in b.get("cubes", [])]
    surfaces = [coins_cube(c) for c in cubes]
    contacts = 0
    for i, j in itertools.combinations(range(len(cubes)), 2):
        contacts += surfaces_opposees(surfaces[i], surfaces[j], tolerance)
    return contacts, len(cubes)


def main() -> int:
    p = argparse.ArgumentParser(description="Choisit le sens de rotation en comptant les contacts.")
    p.add_argument("--modele", default="lemunruss")
    p.add_argument("--contact", type=float, default=0.03, help="tolerance de contact, en blocs")
    a = p.parse_args()
    sens = ["tel-quel", "inverse", "inverse-x", "inverse-y", "inverse-z"]
    resultats = []
    dossier = tempfile.mkdtemp(prefix="sens-rot-")
    try:
        for s in sens:
            geo = os.path.join(dossier, f"{s}.geo.json")
            subprocess.run([sys.executable, os.path.join(RACINE, "tools/convert_modele_java.py"),
                            "--modele", a.modele, "--nom-geo", a.modele, "--centrer",
                            "--sens-rotation", s, "--sortie", geo], capture_output=True, timeout=300)
            if not os.path.exists(geo):
                continue
            contacts, nb = compter_contacts(geo, a.contact * 16.0)
            resultats.append((contacts, s, nb))
        # et la reference : le modele SANS aucune rotation
        geo_sans = os.path.join(dossier, "sans.geo.json")
        subprocess.run([sys.executable, os.path.join(RACINE, "tools/convert_modele_java.py"),
                        "--modele", a.modele, "--nom-geo", a.modele, "--centrer",
                        "--sans-rotations", "--sortie", geo_sans], capture_output=True, timeout=300)
        if os.path.exists(geo_sans):
            contacts, nb = compter_contacts(geo_sans, a.contact * 16.0)
            resultats.append((contacts, "sans rotations (temoin)", nb))
    finally:
        import shutil
        shutil.rmtree(dossier, ignore_errors=True)

    resultats.sort(reverse=True)
    print(f"contacts entre cubes, tolerance {a.contact} bloc — le plus de contacts = le plus coherent")
    for contacts, s, nb in resultats:
        part = 100.0 * contacts / max(1, nb * (nb - 1) // 2)
        print(f"   {s:26} {contacts:4d} contact(s) sur {nb * (nb - 1) // 2} paires possibles ({part:.1f} %)")
    if resultats:
        print(f"\nmeilleur : {resultats[0][1]} — c'est lui qui recolle le plus de morceaux.")
        print("A confirmer a l'oeil : la mesure dit la coherence, pas la ressemblance.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
