#!/usr/bin/env python3
"""Genere le forceload EXACT de la campagne, calcule depuis ses coordonnees.

Pourquoi : les remises a zero des portes et les compteurs de zone vivent dans des chunks repartis sur
toute la carte. Sans forceload, ils ne tournent que si un joueur est a proximite — donc une porte peut
rester ouverte « pour rien ». La plage livree au depart (-160 0 500 160) etait une estimation : elle
rate les coordonnees en dessous de X -160 et au-dela de X 500, et tout ce qui est en Z negatif.

Ici on ne devine pas : on lit les coordonnees des zones, des portes et des mecanismes, on prend les
chunks qui les contiennent, on fusionne les suites contigues, et on VERIFIE que chaque coordonnee de
la campagne est couverte.

    python3 tools/forceload_campagne.py [--ecrire /chemin/carte] [--verifier]
"""
from __future__ import annotations

import argparse
import json
import os
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CAMPAGNE = os.path.join(RACINE, "src/main/resources/data/voiddynasty/campagne")
# Plages plausibles pour une coordonnee de la carte. Un triplet de nombres qui tombe dedans est
# traite comme une position ; les tailles, rotations et durees tombent dehors. On ratisse LARGE et on
# le dit : mieux vaut garder charges quelques chunks de trop que rater une porte.
X_MIN, X_MAX = -2048, 2048
Y_MIN, Y_MAX = -256, 512
Z_MIN, Z_MAX = -2048, 2048


def coordonnees(objet, acc: list) -> list:
    """Toutes les positions de l'arbre, dans les DEUX ecritures utilisees par la campagne :
    le triplet [x, y, z] et la paire de cles {"x": …, "z": …}. N'en lire qu'une rate la moitie
    des portes — c'est arrive, d'ou ce commentaire."""
    if isinstance(objet, dict):
        if "x" in objet and "z" in objet and isinstance(objet["x"], (int, float)) \
                and isinstance(objet["z"], (int, float)):
            y = objet.get("y", 0) if isinstance(objet.get("y", 0), (int, float)) else 0
            if X_MIN <= objet["x"] <= X_MAX and Z_MIN <= objet["z"] <= Z_MAX:
                acc.append((objet["x"], y, objet["z"]))
        for valeur in objet.values():
            coordonnees(valeur, acc)
    elif isinstance(objet, list):
        if (len(objet) >= 3 and all(isinstance(v, (int, float)) for v in objet[:3])
                and X_MIN <= objet[0] <= X_MAX and Y_MIN <= objet[1] <= Y_MAX
                and Z_MIN <= objet[2] <= Z_MAX):
            acc.append(tuple(objet[:3]))
        for valeur in objet:
            coordonnees(valeur, acc)
    return acc


def chunks_des_coordonnees(positions) -> set:
    """Un chunk par coordonnee, plus ses voisins immediats : une porte occupe souvent deux chunks."""
    chunks = set()
    for x, _, z in positions:
        cx, cz = int(x) >> 4, int(z) >> 4
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                chunks.add((cx + dx, cz + dz))
    return chunks


def fusionner(chunks) -> list:
    """Fusionne les chunks contigus d'une meme ligne Z en plages : beaucoup moins de commandes."""
    par_ligne: dict = {}
    for cx, cz in chunks:
        par_ligne.setdefault(cz, []).append(cx)
    plages = []
    for cz, cxs in sorted(par_ligne.items()):
        cxs.sort()
        debut = precedent = cxs[0]
        for cx in cxs[1:]:
            if cx == precedent + 1:
                precedent = cx
                continue
            plages.append((debut, cz, precedent, cz))
            debut = precedent = cx
        plages.append((debut, cz, precedent, cz))
    return plages


def main() -> int:
    p = argparse.ArgumentParser(description="Forceload exact de la campagne.")
    p.add_argument("--ecrire", help="dossier de la carte où écrire les commandes")
    p.add_argument("--eprouver", action="store_true",
                   help="epreuve : retire une plage de la couverture et verifie que le controle echoue")
    a = p.parse_args()

    positions = []
    for nom in ("zones.json", "campagne.json"):
        with open(os.path.join(CAMPAGNE, nom), encoding="utf-8") as f:
            coordonnees(json.load(f), positions)
    # on ne garde que la surface de campagne : la carte tient dans ces bornes
    positions = [(x, y, z) for x, y, z in positions if -400 <= x <= 800 and -500 <= z <= 400]
    print(f"{len(positions)} coordonnees de campagne lues")

    chunks = chunks_des_coordonnees(positions)
    plages = fusionner(chunks)
    print(f"{len(chunks)} chunks couverts, en {len(plages)} plage(s) contigues")

    commandes = [f"forceload add {x1 * 16} {z1 * 16} {x2 * 16 + 15} {z2 * 16 + 15}"
                 for x1, z1, x2, z2 in plages]
    # VERIFICATION : chaque coordonnee doit tomber dans une plage
    if a.eprouver:
        # on sabote en retirant la plage qui contient REELLEMENT la premiere coordonnee : sinon
        # l'epreuve ne prouve rien (retirer une plage vide ne decouvre personne).
        cx0, cz0 = int(positions[0][0]) >> 4, int(positions[0][2]) >> 4
        avant = len(plages)
        plages = [pl for pl in plages if not (pl[0] <= cx0 <= pl[2] and pl[1] <= cz0 <= pl[3])]
        print(f"EPREUVE : la plage contenant le chunk ({cx0}, {cz0}) est retiree "
              f"({avant - len(plages)} plage(s) en moins)")
    couverts = {(cx, cz) for x1, z1, x2, z2 in plages for cx in range(x1, x2 + 1)
                for cz in range(z1, z2 + 1)}
    ratees = [pos for pos in positions if ((int(pos[0]) >> 4), (int(pos[2]) >> 4)) not in couverts]
    if ratees:
        print(f"ECHEC : {len(ratees)} coordonnees non couvertes, dont {ratees[:3]}")
        return 1
    print(f"verification : les {len(positions)} coordonnees sont couvertes")

    if a.ecrire:
        cible = os.path.join(a.ecrire, "FORCELOAD-CAMPAGNE.mcfunction")
        with open(cible, "w", encoding="utf-8") as f:
            f.write("# Forceload exact de la campagne VOID DYNASTY, genere depuis ses coordonnees.\n")
            f.write("# A executer une fois (par exemple depuis un bloc-commande ou /function).\n")
            f.write("# Pour tout retirer : /forceload remove all\n\n")
            for c in commandes:
                f.write(c + "\n")
        print(f"ecrit : {cible} ({len(commandes)} commandes)")
        note = os.path.join(a.ecrire, "FORCELOAD-A-LIRE.txt")
        with open(note, "w", encoding="utf-8") as f:
            f.write(
                "FORCELOAD DE LA CAMPAGNE\n"
                "========================\n\n"
                "Les mecanismes de la campagne (ouverture des portes, compteurs de zone) ne tournent\n"
                "que dans les chunks charges. Pour qu'ils fonctionnent meme sans joueur a proximite,\n"
                "il faut garder leurs chunks charges.\n\n"
                f"{len(plages)} plages contigues couvrent les {len(positions)} coordonnees de la campagne,\n"
                "verifie par tools/forceload_campagne.py.\n\n"
                "DEUX FACONS DE FAIRE\n"
                "  1. une fois, a la main, en collant les lignes de FORCELOAD-CAMPAGNE.mcfunction\n"
                "     dans le chat (elles commencent toutes par /forceload add) ;\n"
                "  2. par datapack : mettez le fichier dans\n"
                "     saves/astartes-map/datapacks/<un_nom>/data/<un_nom>/function/ et appelez-le.\n\n"
                "Pour tout arreter : /forceload remove all\n\n"
                "ATTENTION : garder des chunks charges consomme de la memoire. La plage livree avant\n"
                "(-160 0 500 160) etait une estimation : elle ratait les portes au-dela de X 500\n"
                "et tout ce qui est en Z negatif. Celle-ci est calculee, et verifiee.\n"
            )
        print(f"ecrit : {note}")
    else:
        for c in commandes[:6]:
            print("   /" + c)
        print(f"   … et {max(0, len(commandes) - 6)} autres")
    return 0


if __name__ == "__main__":
    sys.exit(main())
