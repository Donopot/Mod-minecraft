#!/usr/bin/env python3
"""Donne un clip `idle` aux cultistes qui n'en ont pas — en REUTILISANT celui du cultiste 1.

DECISION DECLAREE (passe de finition, 16/10/2025). La source ne fournit aucun clip d'arret pour les
cultistes 2, 3 et 4 ni pour la Pretresse : le port retombait sur la marche a l'arret. Or les cinq
cultistes sont le MEME squelette avec des variantes (14 a 15 os communs, verifie) : l'idle du
cultiste 1 nomme dix os, tous presents chez les autres, sauf `gun` — que GeckoLib ignore simplement
quand l'os n'existe pas. Reutiliser une animation DE LA SOURCE sur le meme rig n'est pas en inventer
une ; c'est ce qui a motive le choix. A defaire d'une ligne si Donopot prefere la marche a l'arret.

    python3 tools/ajouter_idle_cultistes.py [--verifier]
"""
from __future__ import annotations

import argparse
import json
import os
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ANIMATIONS = os.path.join(RACINE, "src/main/resources/assets/voiddynasty/geckolib/animations")
SOURCE = "cultist_1.animation.json"
CIBLES = ["cultist_2.animation.json", "cultist_3.animation.json", "cultist_4.animation.json",
          "cultist_boss.animation.json"]
CLIP = "idle"


def main() -> int:
    p = argparse.ArgumentParser(description="Ajoute le clip idle du cultiste 1 aux autres cultistes.")
    p.add_argument("--verifier", action="store_true",
                   help="ne rien ecrire : echouer si un culte n'a pas le clip")
    a = p.parse_args()

    chemin_source = os.path.join(ANIMATIONS, SOURCE)
    with open(chemin_source, encoding="utf-8") as f:
        source = json.load(f)
    if CLIP not in source.get("animations", {}):
        print(f"ECHEC : {SOURCE} ne contient pas de clip {CLIP}")
        return 1
    idle = source["animations"][CLIP]

    manquants = []
    for nom in CIBLES:
        chemin = os.path.join(ANIMATIONS, nom)
        with open(chemin, encoding="utf-8") as f:
            data = json.load(f)
        if CLIP in data.get("animations", {}):
            print(f"   deja la : {nom}")
            continue
        manquants.append(nom)
        if a.verifier:
            print(f"   MANQUE  : {nom}")
            continue
        data.setdefault("animations", {})[CLIP] = idle
        with open(chemin, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(f"   ajoute  : {nom} (clip {CLIP}, repris de {SOURCE})")

    if a.verifier:
        print(f"\n{len(manquants)} fichier(s) sans clip {CLIP}" + (f" : {', '.join(manquants)}"
                                                                  if manquants else " — a jour"))
        return 1 if manquants else 0
    print(f"\n{len(manquants)} fichier(s) modifie(s). Les os absents d'un rig sont ignores par GeckoLib.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
