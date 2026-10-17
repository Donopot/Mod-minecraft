#!/usr/bin/env python3
"""Phase A2 — compléter les blocs « display » des 15 armes.

Constat de départ : chaque arme a un bloc `display`, mais de 3 à 8 clés selon l'arme. Les clés
manquantes ne sont pas décoratives :
  - `head`      : l'arme portée sur la tête (13 armes sur 15 n'en avaient pas) ;
  - `ground`    : l'arme posée au sol ;
  - `fixed`     : l'arme dans un cadre (item frame) ;
  - `thirdperson_*` / `firstperson_*` : l'arme vue sur un autre joueur / tenue en main.

Méthode — et ce que ce script NE fait PAS
  Les valeurs existantes sont réglées sur la géométrie de chaque modèle (une épée longue n'a pas la
  même translation qu'un pistolet). Elles sont donc **conservées telles quelles** : ce script ne
  touche jamais à une clé déjà présente.

  Les clés manquantes sont recopiées depuis l'arme la plus complète de la **même famille** — donc
  depuis les valeurs du projet lui-même, jamais inventées.

  L'échelle `gui` n'est PAS uniformisée, et c'est délibéré : elle va de 0,35 (épée énergétique) à 2,0
  (pistolet plasma) précisément pour que chaque modèle remplisse la case de l'inventaire. La mettre
  à une valeur unique ferait déborder les armes longues et rétrécirait les petites. C'est une
  cohérence de RÉSULTAT, qui se juge à l'œil, pas d'écriture.

Usage :  python3 tools/uniformiser_display.py [--simulation]
"""
from __future__ import annotations

import argparse
import copy
import json
import os
import sys

MODELS = "/opt/data/workspace/um-test-mod/src/main/resources/assets/voiddynasty/models/item"

CLES = ["thirdperson_righthand", "thirdperson_lefthand", "firstperson_righthand", "firstperson_lefthand",
        "gui", "ground", "head", "fixed"]

FAMILLES = {
    "pistolet": ["bolt_pistol", "laz_pistol", "plasma_pistol"],
    "fusil": ["boltgun", "las_gun", "stalker_bolt_rifle", "shotgun", "flamethrower", "melta_gun",
              "plasma_incinerator"],
    "lourde": ["heavy_bolter", "hunter_killer_missile"],
    "melee": ["chainsword", "powersword", "power_hammer"],
}
# référence par famille = l'arme qui a le plus de clés ; `head` est pris sur boltgun si la
# référence de famille ne l'a pas (cas du mêlée)
REFERENCE = {"pistolet": "bolt_pistol", "fusil": "boltgun", "lourde": "heavy_bolter", "melee": "powersword"}
REFERENCE_SECOURS = "boltgun"


def lire(nom: str) -> dict:
    with open(os.path.join(MODELS, nom + ".json"), encoding="utf-8") as f:
        return json.load(f)


def ecrire(nom: str, data: dict) -> None:
    with open(os.path.join(MODELS, nom + ".json"), "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--simulation", action="store_true")
    args = ap.parse_args()

    refs = {fam: lire(ref).get("display", {}) for fam, ref in REFERENCE.items()}
    secours = lire(REFERENCE_SECOURS).get("display", {})

    total_ajouts, rapport = 0, []
    for famille, armes in FAMILLES.items():
        ref = refs[famille]
        for nom in armes:
            d = lire(nom)
            disp = d.setdefault("display", {})
            manquantes = [k for k in CLES if k not in disp]
            ajouts = []
            for cle in manquantes:
                source = ref.get(cle) or secours.get(cle)
                if not source:
                    continue
                disp[cle] = copy.deepcopy(source)
                ajouts.append(cle)
            if ajouts and not args.simulation:
                ecrire(nom, d)
            total_ajouts += len(ajouts)
            rapport.append((famille, nom, ajouts, len(disp)))

    print(f"clés ajoutées : {total_ajouts}")
    for famille, nom, ajouts, total in rapport:
        etat = f"+{len(ajouts)} → {total}/8 clés" if ajouts else f"{total}/8 clés (rien à faire)"
        detail = f"  ({', '.join(ajouts)})" if ajouts else ""
        print(f"  {famille:9s} {nom:24s} {etat}{detail}")
    if args.simulation:
        print("\n(simulation : aucun fichier écrit)")
    else:
        restants = [(n, [k for k in CLES if k not in lire(n).get('display', {})])
                    for _, n, _, _ in rapport]
        incompletes = [(n, m) for n, m in restants if m]
        print(f"\nvérification : {len(restants) - len(incompletes)}/{len(restants)} armes ont les 8 clés")
        for n, m in incompletes:
            print(f"  ⚠ {n} : manque encore {m}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
