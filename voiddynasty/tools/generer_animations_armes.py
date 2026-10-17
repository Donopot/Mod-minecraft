#!/usr/bin/env python3
"""Phase D — animations d'armes visibles en main, sans renderer ni mixin.

Le problème : la cadence de tir est dérivée des coups par seconde (`fireIntervalTicks = 20 / rps`),
donc un recul proportionnel au cooldown dure moins d'un tick sur un bolter — invisible. Il faut un
recul à **durée fixe**, ce que le cooldown vanilla ne peut pas exprimer.

La solution, 100 % donnée :
  1. deux composants d'état transitoires (`voiddynasty:recoil`, `voiddynasty:reloading`) posés par le
     serveur au tir et au rechargement, décrémentés chaque tick ;
  2. une définition d'objet en `minecraft:condition` sur la présence de ces composants ;
  3. une variante de modèle par état — le même modèle, reculé/tourné.

Aucun renderer d'objet, aucune mixin, aucun paquet réseau : le jeu synchronise déjà les composants
d'objet d'un joueur vers son client.

Vérifié dans le jar 26.3 avant écriture :
  - ConditionalItemModel(property, onTrue, onFalse) → clés JSON `component`, `on_true`, `on_false`
    (forme relevée sur assets/minecraft/items/compass.json, qui utilise has_component) ;
  - HasComponent(componentType, ignoreDefault) → clé JSON `component`.

Usage : python3 tools/generer_animations_armes.py [--simulation]
"""
from __future__ import annotations

import json
import os
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(RACINE, "src/main/resources/assets/voiddynasty")
NS = "voiddynasty"

# Les 15 armes (identifiants d'objet = identifiants de modèle). Relevés sur assets/voiddynasty/items/,
# pas devinés : `lasgun`/`meltagun` n'existent pas, ce sont `las_gun` et `melta_gun`.
ARMES = ["boltgun", "bolt_pistol", "heavy_bolter", "stalker_bolt_rifle", "las_gun", "laz_pistol",
         "plasma_pistol", "plasma_incinerator", "shotgun", "flamethrower", "melta_gun",
         "hunter_killer_missile", "chainsword", "powersword", "power_hammer"]
ATTENDU = 15

# Recul : on recule l'arme vers le tireur et on la relève légèrement.
# Les canons s'étendent vers +Z (relevé sur les modèles : boltgun Z -1,7..16,2), donc reculer = -Z.
RECUL = {"translation": [0.0, 0.0, -2.2], "rotation_x": -5.0}
# Rechargement : arme basculée vers le bas, reculée contre le corps.
RECHARGEMENT = {"translation": [0.0, -1.0, -1.0], "rotation_x": -32.0}


def transformer(modele: dict, reglage: dict) -> dict:
    """Recule/penche le modèle. Les éléments tournent autour de la crosse (pivot bas-arrière)."""
    dx, dy, dz = reglage["translation"]
    angle = reglage["rotation_x"]
    import math
    ca, sa = math.cos(math.radians(angle)), math.sin(math.radians(angle))

    # pivot de rotation : bas du modèle (Y min) et origine du canon (Z min)
    ys = [e["from"][1] for e in modele.get("elements", [])] + \
         [e["to"][1] for e in modele.get("elements", [])]
    zs = [e["from"][2] for e in modele.get("elements", [])] + \
         [e["to"][2] for e in modele.get("elements", [])]
    pivot_y, pivot_z = (min(ys) if ys else 0.0), (min(zs) if zs else 0.0)

    def point(p):
        x, y, z = p[0], p[1] - pivot_y, p[2] - pivot_z
        # rotation autour de l'axe X (tangage) puis translation
        return [x + dx, round(y * ca - z * sa + pivot_y + dy, 3), round(y * sa + z * ca + pivot_z + dz, 3)]

    sortie = {"textures": modele.get("textures", {}), "elements": []}
    for e in modele.get("elements", []):
        n = json.loads(json.dumps(e))          # copie profonde
        n["from"], n["to"] = point(e["from"]), point(e["to"])
        # un élément tourné sur l'axe X doit voir ses bornes Y/Z remises dans l'ordre
        for i in (1, 2):
            if n["from"][i] > n["to"][i]:
                n["from"][i], n["to"][i] = n["to"][i], n["from"][i]
        # la rotation propre de l'élément est conservée : elle s'applique avant la nôtre
        sortie["elements"].append(n)
    if "display" in modele:
        sortie["display"] = modele["display"]
    return sortie


def construire_condition(base: str, recul: str, rechargement: str) -> dict:
    """Définition d'objet : rechargement prioritaire, puis recul, puis état normal."""
    return {"model": {
        "type": "minecraft:condition",
        "component": f"{NS}:reloading",
        "on_true": {"type": "minecraft:model", "model": rechargement},
        "on_false": {
            "type": "minecraft:condition",
            "component": f"{NS}:recoil",
            "on_true": {"type": "minecraft:model", "model": recul},
            "on_false": {"type": "minecraft:model", "model": base},
        },
    }}


def ecrire(chemin: str, contenu: dict, simulation: bool) -> None:
    if not simulation:
        os.makedirs(os.path.dirname(chemin), exist_ok=True)
        with open(chemin, "w", encoding="utf-8") as f:
            json.dump(contenu, f, indent=2)
            f.write("\n")


def main() -> int:
    simulation = "--simulation" in sys.argv
    faits, ignores = [], []
    for arme in ARMES:
        dossier_models = os.path.join(ASSETS, "models/item")
        base = os.path.join(dossier_models, f"{arme}.json")
        if not os.path.exists(base):
            ignores.append(f"{arme} : modèle introuvable")
            continue
        modele = json.load(open(base, encoding="utf-8"))
        if "elements" not in modele:
            ignores.append(f"{arme} : modèle sans éléments (parent seul), laissé tel quel")
            continue

        recul = transformer(modele, RECUL)
        rechargement = transformer(modele, RECHARGEMENT)
        ecrire(os.path.join(dossier_models, f"{arme}_recoil.json"), recul, simulation)
        ecrire(os.path.join(dossier_models, f"{arme}_reloading.json"), rechargement, simulation)
        ecrire(os.path.join(ASSETS, f"items/{arme}.json"),
               construire_condition(f"{NS}:item/{arme}", f"{NS}:item/{arme}_recoil",
                                    f"{NS}:item/{arme}_reloading"), simulation)
        faits.append(arme)

    print(f"{len(faits)} armes animées : {', '.join(faits)}")
    for i in ignores:
        print(f"  [ignoré] {i}")
    # GARDE-FOU : produire 13 animations sur 15 attendues sans le dire serait un échec silencieux.
    if len(faits) != ATTENDU:
        print(f"ÉCHEC : {len(faits)} armes traitées sur {ATTENDU} attendues — "
              f"l'écart doit être expliqué avant de poursuivre")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
