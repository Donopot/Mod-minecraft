#!/usr/bin/env python3
"""Vérifie que les assets portés sont bien DANS le jar livré.

Une copie dans `src/main/resources` ne prouve rien : ce qui compte est ce que le jeu charge, donc ce
qui se trouve dans le `.jar`. Ce script compte les assets par catégorie et contrôle des témoins
nommés — l'équivalent, côté assets, du « vérifier dans le jar livré » déjà appliqué aux particules.
"""
import zipfile
from collections import Counter

JAR = "/opt/data/workspace/opworldlike-fabric/build/libs/opworldlike-1.0.0.jar"
TEMOINS = [
    "geckolib/models/aroaro_arrow.geo.json",
    "geckolib/animations/aroaro_arrow.animation.json",
    "geckolib/models/uta.geo.json",
    "geckolib/animations/uta.animation.json",
    "geckolib/models/clothes_uta2.geo.json",
    "textures/entities/uta.png",
    "textures/item/devil_fruit_uta_uta.png",
    "textures/item/devil_fruit_aro_aro.png",
    "textures/entities/aroaro_arrow.png",
]

z = zipfile.ZipFile(JAR)
noms = [n for n in z.namelist() if not n.endswith("/")]
print(f"jar : {len(noms)} entrées")
for categorie, motif in [("geo (geckolib/models)", "geckolib/models/"),
                         ("animations", "geckolib/animations/"),
                         ("textures", "assets/minepiece/textures/"),
                         ("définitions d'objet", "assets/minepiece/items/")]:
    n = sum(1 for x in noms if motif in x)
    print(f"  {categorie:24s} {n}")
print()
manquants = 0
for t in TEMOINS:
    ok = any(x.endswith(t) for x in noms)
    manquants += 0 if ok else 1
    print(f"  {'OK    ' if ok else 'MANQUE'} {t}")
print(f"\n{len(TEMOINS) - manquants}/{len(TEMOINS)} témoins présents dans le jar")
