#!/usr/bin/env python3
"""Porte les PARTICULES du jar source : définitions + textures, octet pour octet.

Convention du jar source (et de Minecraft) : une définition `assets/<ns>/particles/<nom>.json`
déclare ses images par `"textures": ["minepiece:particle_x_1", …]`, résolues sous
`assets/minepiece/textures/particle/particle_x_1.png`. Le préfixe `minepiece:` n'est PAS un dossier :
c'est le namespace. Un contrôle qui ajoute naïvement `.png` à la chaîne complète conclut que les
449 images sont introuvables alors qu'elles sont toutes là.

Ce script :
  * copie les définitions sous leur **nom d'origine** (le port en avait 3 renommées — elles sont
    remplacées pour que l'identifiant enregistré soit celui de la source) ;
  * copie les textures **réclamées** par ces définitions, et signale celles du dossier source qui ne
    sont réclamées par personne (poids mort potentiel : on les nomme au lieu de les copier en silence) ;
  * écrit un manifeste et **relit chaque fichier** après écriture.

Usage : python3 tools/particle_port.py [--list]
"""
import argparse
import json
import os
import re
import zipfile

JAR = "/opt/data/workspace/minepiece/MinePiece-ver15-forge-1.20.1.jar"
RES = "/opt/data/workspace/opworldlike-fabric/src/main/resources/assets/minepiece"
MANIFESTE = "/opt/data/workspace/opworldlike-fabric/docs/particules-portees.json"

# Définitions renommées lors de l'extraction initiale des 3 particules : remplacées par les noms source.
ANCIENS_NOMS = {
    "dark.json": "particle_dark.json",
    "light_large.json": "particle_light_large.json",
    "light_small.json": "particle_light_small.json",
}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--list", action="store_true")
    args = ap.parse_args()
    z = zipfile.ZipFile(JAR)
    definitions = sorted(n for n in z.namelist()
                         if n.startswith("assets/minepiece/particles/") and n.endswith(".json"))

    # résolution des images : namespace retiré, préfixe « particle » ajouté, .png
    images = {}
    for d in definitions:
        contenu = json.loads(z.read(d).decode("utf-8"))
        for ref in contenu.get("textures", []):
            nom = ref.split(":", 1)[-1]
            images.setdefault(nom + ".png", set()).add(os.path.basename(d))
    toutes = {os.path.basename(n) for n in z.namelist()
              if "/textures/particle/" in n and n.endswith(".png")}
    orphelines = sorted(toutes - set(images))
    introuvables = sorted(i for i in images if i not in toutes)

    print(f"définitions source : {len(definitions)}")
    print(f"images réclamées    : {len(images)}   (présentes dans la source : {len(images) - len(introuvables)})")
    print(f"images du dossier jamais réclamées par une définition : {len(orphelines)}")
    if introuvables:
        print("  INTROUVABLES :", ", ".join(introuvables[:8]))
    if args.list:
        return

    # 1) retirer les définitions renommées
    for ancien, nouveau in ANCIENS_NOMS.items():
        c = os.path.join(RES, "particles", ancien)
        if os.path.exists(c):
            os.remove(c)
            print(f"  retiré (renommé) : particles/{ancien} -> remplacé par {nouveau}")

    # 2) copier les définitions sous leur nom source
    os.makedirs(os.path.join(RES, "particles"), exist_ok=True)
    defs_ecrites = []
    for d in definitions:
        cible = os.path.join(RES, "particles", os.path.basename(d))
        donnees = z.read(d)
        if os.path.exists(cible) and open(cible, "rb").read() == donnees:
            defs_ecrites.append(os.path.basename(d))
            continue
        with open(cible, "wb") as f:
            f.write(donnees)
        assert open(cible, "rb").read() == donnees, f"copie non conforme : {cible}"
        defs_ecrites.append(os.path.basename(d))

    # 3) copier les textures réclamées
    os.makedirs(os.path.join(RES, "textures", "particle"), exist_ok=True)
    tex_ecrites, tex_deja, octets = [], 0, 0
    for nom in sorted(images):
        source = next(n for n in z.namelist() if n.endswith("/textures/particle/" + nom))
        cible = os.path.join(RES, "textures", "particle", nom)
        donnees = z.read(source)
        if os.path.exists(cible) and open(cible, "rb").read() == donnees:
            tex_deja += 1
            continue
        with open(cible, "wb") as f:
            f.write(donnees)
        assert open(cible, "rb").read() == donnees, f"copie non conforme : {cible}"
        tex_ecrites.append(nom)
        octets += len(donnees)

    manifeste = {
        "definitions": defs_ecrites,
        "textures_portees": tex_ecrites,
        "textures_deja_presentes": tex_deja,
        "textures_non_reclamees_dans_la_source": orphelines,
        "octets_textures": octets,
    }
    os.makedirs(os.path.dirname(MANIFESTE), exist_ok=True)
    json.dump(manifeste, open(MANIFESTE, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"\ndéfinitions en place : {len(defs_ecrites)}")
    print(f"textures copiées     : {len(tex_ecrites)}   (déjà présentes : {tex_deja})   {octets / 1024:.0f} Ko")
    print(f"manifeste : {MANIFESTE}")


if __name__ == "__main__":
    main()
