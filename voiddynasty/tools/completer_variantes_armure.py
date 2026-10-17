#!/usr/bin/env python3
"""Remplit les variantes `_s` (bras fins) et `_n` des textures d'armure.

DECISION DE DONOPOT (16/10/2025) : « a) je remplis ». Le jeu cherche une variante `_s` pour les
joueurs au modele fin. Nos fichiers existaient mais etaient VIDES (0,9 % de pixels peints contre
62,5 % pour la texture de base) : comme le fichier existe, le jeu ne retombe pas sur la base, et
l'armure disparaissait sur ces joueurs.

Ici on recopie la texture de base dans les variantes vides. C'est une solution franche, pas une
retouche d'artiste : le bras fin a une colonne de moins que le bras normal, donc le rendu peut etre
decale d'un pixel sur le bras. C'est dit, et c'est visible a l'oeil sur un skin fin.

    python3 tools/completer_variantes_armure.py [--racine .] [--verifier]
"""
from __future__ import annotations

import argparse
import glob
import os
import shutil
import sys
import zlib


def part_peinte(chemin: str) -> float:
    """Part de pixels non transparents, lue sans PIL (lecture PNG minimale)."""
    try:
        with open(chemin, "rb") as f:
            donnees = f.read()
        if donnees[:8] != b"\x89PNG\r\n\x1a\n":
            return -1.0
        pos, idat, taille = 8, b"", None
        while pos < len(donnees):
            longueur = int.from_bytes(donnees[pos:pos + 4], "big")
            type_ = donnees[pos + 4:pos + 8]
            corps = donnees[pos + 8:pos + 8 + longueur]
            if type_ == b"IHDR":
                largeur = int.from_bytes(corps[0:4], "big")
                hauteur = int.from_bytes(corps[4:8], "big")
                profondeur = corps[8]
                type_couleur = corps[9]
                taille = (largeur, hauteur, profondeur, type_couleur)
            elif type_ == b"IDAT":
                idat += corps
            elif type_ == b"IEND":
                break
            pos += 12 + longueur
        if taille is None or taille[3] not in (0, 4, 6):
            return -1.0
        brut = zlib.decompress(idat)
        canaux = {0: 1, 4: 2, 6: 4}[taille[3]]
        octets_par_ligne = taille[0] * canaux
        non_transparents = total = 0
        precedent = bytearray(octets_par_ligne)
        pos = 0
        for _ in range(taille[1]):
            filtre = brut[pos]
            ligne = bytearray(brut[pos + 1:pos + 1 + octets_par_ligne])
            pos += 1 + octets_par_ligne
            for i in range(octets_par_ligne):          # defiltrage minimal (None/Sub/Up)
                a = ligne[i - canaux] if i >= canaux else 0
                b = precedent[i]
                c = precedent[i - canaux] if i >= canaux else 0
                if filtre == 1:
                    ligne[i] = (ligne[i] + a) & 0xFF
                elif filtre == 2:
                    ligne[i] = (ligne[i] + b) & 0xFF
                elif filtre == 3:
                    ligne[i] = (ligne[i] + (a + b) // 2) & 0xFF
                elif filtre == 4:
                    p = a + b - c
                    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                    pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                    ligne[i] = (ligne[i] + pr) & 0xFF
            for x in range(taille[0]):
                pixel = ligne[x * canaux:(x + 1) * canaux]
                total += 1
                if canaux < 4 or pixel[3] > 40:
                    non_transparents += 1
            precedent = ligne
        return 100.0 * non_transparents / max(1, total)
    except Exception:  # noqa: BLE001
        return -1.0


def main() -> int:
    p = argparse.ArgumentParser(description="Remplit les variantes vides des textures d'armure.")
    p.add_argument("--racine", default=".")
    p.add_argument("--verifier", action="store_true", help="ne rien ecrire : signaler les variantes vides")
    a = p.parse_args()
    dossier = os.path.join(os.path.abspath(a.racine),
                           "src/main/resources/assets/voiddynasty/textures/entity/equipment")
    if not os.path.isdir(dossier):
        print(f"ERREUR : {dossier} introuvable")
        return 2

    vides, remplis = [], []
    for base in sorted(glob.glob(os.path.join(dossier, "*", "*.png"))):
        if base.endswith("_s.png") or base.endswith("_n.png"):
            continue
        part_base = part_peinte(base)
        for suffixe in ("_s", "_n"):
            variante = base[:-4] + suffixe + ".png"
            if not os.path.exists(variante):
                continue
            part = part_peinte(variante)
            if part >= 0 and part_base >= 0 and part < 0.25 * part_base:
                vides.append((variante, part, part_base))
                if not a.verifier:
                    shutil.copy(base, variante)
                    remplis.append(variante)

    if vides:
        for chemin, part, part_base in vides:
            nom = chemin.split("/equipment/")[1]
            etat = "vide" if a.verifier else "rempli"
            print(f"   {etat:6} : {nom}  ({part:.1f} % peint → base a {part_base:.1f} %)")
    if a.verifier:
        print(f"\n{len(vides)} variante(s) quasi vide(s)"
              + (" — a remplir" if vides else " — rien a faire"))
        return 1 if vides else 0
    print(f"\n{len(remplis)} variante(s) recopiee(s) depuis la texture de base.")
    print("Limite assumee : le bras fin a une colonne de moins, le rendu peut etre decale d'un pixel.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
