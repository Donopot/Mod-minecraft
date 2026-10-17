#!/usr/bin/env python3
"""Audit des assets des Fruits : catalogue reçu vs jar source vs port Fabric.

Le catalogue (`MinePiece_Catalogue_Fruits_Techniques_Assets_Exhaustif.csv`) donne, par Fruit, les
NOMS des assets. Ce script les RÉSOUT vers leurs chemins réels dans le jar source, puis vérifie
lesquels sont déjà dans le port. Rien n'est deviné : un nom introuvable est signalé, pas rangé.

Sortie :
  * `docs/audit-assets.csv`   — un enregistrement par (fruit, asset, statut, chemin source, chemin port)
  * un résumé par fruit sur la sortie standard

Usage : python3 tools/asset_audit.py [fruit ...]
"""
import csv
import json
import os
import re
import sys
import zipfile
from collections import defaultdict

JAR = "/opt/data/workspace/minepiece/MinePiece-ver15-forge-1.20.1.jar"
PORT = "/opt/data/workspace/opworldlike-fabric/src/main/resources"
CATALOGUE = ("/opt/data/cache/documents/"
             "doc_3796beb7ad8b_MinePiece_Catalogue_Fruits_Techniques_Assets_Exhaustif.csv")
SORTIE = "/opt/data/workspace/opworldlike-fabric/docs/audit-assets.csv"

COL_TECHNIQUES = "Techniques & Classes Associées"
COL_CONFIGS = "Configurations & Balances"
COL_MODELES = "Modèles & Géométries 3D (.json / .geo.json)"
COL_ANIMATIONS = "Animations Associées (.animation.json)"
COL_TEXTURES = "Textures Associées (.png)"


def index_jar():
    """Chemin -> ensemble des noms de base du jar, et nom de base -> chemins candidats."""
    z = zipfile.ZipFile(JAR)
    par_nom = defaultdict(list)
    for chemin in z.namelist():
        if chemin.endswith("/"):
            continue
        par_nom[os.path.basename(chemin)].append(chemin)
    return z, par_nom


def chemins_port():
    """Nom de base -> chemins dans le port (relatifs à src/main/resources)."""
    par_nom = defaultdict(list)
    for dp, _, fs in os.walk(PORT):
        for f in fs:
            par_nom[f].append(os.path.relpath(os.path.join(dp, f), PORT))
    return par_nom


def dossiers_source(chemin):
    """Ordre de préférence selon le type d'asset (le nom seul est ambigu dans un gros jar)."""
    if chemin.endswith(".geo.json"):
        return 0 if "/geo/" in chemin else 2
    if chemin.endswith(".animation.json"):
        return 0 if "/animations/" in chemin else 2
    if chemin.endswith(".png"):
        if "/textures/entities/" in chemin:
            return 0
        if "/textures/item/" in chemin:
            return 1
        if "/textures/block/" in chemin:
            return 2
        return 3
    if "/models/item/" in chemin:
        return 0
    return 1


def choisir(candidats):
    if not candidats:
        return None
    return sorted(candidats, key=dossiers_source)[0]


def destination_port(chemin_source, nom):
    """Où ranger l'asset dans le port, en gardant l'arborescence d'origine."""
    if "/geo/" in chemin_source:
        return f"assets/minepiece/geo/{nom}"
    if "/animations/" in chemin_source:
        return f"assets/minepiece/animations/{nom}"
    if "/textures/" in chemin_source:
        apres = chemin_source.split("/textures/", 1)[1]
        return f"assets/minepiece/textures/{apres}"
    if "/models/" in chemin_source:
        apres = chemin_source.split("/models/", 1)[1]
        return f"assets/minepiece/models/{apres}"
    if "/blockstates/" in chemin_source:
        apres = chemin_source.split("/blockstates/", 1)[1]
        return f"assets/minepiece/blockstates/{apres}"
    return None


def textures_reclamees_par(z, chemin_geo):
    """Un .geo.json (ou un modèle Java) déclare les textures qu'il utilise : on les lit."""
    try:
        brut = z.read(chemin_geo).decode("utf-8", "replace")
    except Exception:
        return []
    trouvees = set()
    for m in re.finditer(r'"texture[s]?"\s*:\s*\{([^}]*)\}', brut):
        for v in re.findall(r'"\w+"\s*:\s*"([^"]+)"', m.group(1)):
            trouvees.add(os.path.basename(v))
    if not trouvees:
        trouvees = {os.path.basename(x) for x in re.findall(r'"textures?"\s*:\s*"([^"]+)"', brut)}
    return sorted(trouvees)


def main():
    filtres = [a.lower() for a in sys.argv[1:]]
    z, par_nom_jar = index_jar()
    par_nom_port = chemins_port()
    with open(CATALOGUE, encoding="utf-8-sig", errors="replace") as f:
        lignes = list(csv.DictReader(f, delimiter=";"))

    enregistrements = []
    for ligne in lignes:
        cle = (ligne.get("ID / Clé Fruit") or "").strip()
        if not cle:
            continue
        base = cle.split(".")[0]
        if filtres and not any(fil in base.lower() for fil in filtres):
            continue
        colonnes = [("technique", COL_TECHNIQUES, "classe"), ("config", COL_CONFIGS, "config"),
                    ("modele", COL_MODELES, "asset"), ("animation", COL_ANIMATIONS, "asset"),
                    ("texture", COL_TEXTURES, "asset")]
        for categorie, colonne, genre in colonnes:
            brut = (ligne.get(colonne) or "").strip()
            if not brut or brut.startswith("Configurations par défaut") or brut.startswith("Dispatcher"):
                continue
            # ⚠️ Les colonnes Modèles / Animations / Textures sont séparées par des VIRGULES,
            # tandis que la colonne Techniques mélange des classes Java et des noms d'assets.
            # Un premier parseur qui coupait tout sur « ; » ne voyait qu'un nom par colonne et
            # annonçait 51 assets à porter au lieu de plusieurs centaines.
            morceaux = [x.strip() for x in re.split(r"[,;]", brut) if x.strip()]
            for nom in morceaux:
                if genre == "classe":
                    enregistrements.append((base, categorie, nom, "classe", "", ""))
                    continue
                chemins = par_nom_jar.get(nom, [])
                chemin_source = choisir(chemins)
                ports = par_nom_port.get(nom, [])
                if chemin_source is None:
                    statut = "ABSENT-SOURCE"
                    cible = ""
                elif ports:
                    statut = "PRESENT-PORT"
                    cible = ports[0]
                else:
                    statut = "A-PORTER"
                    cible = destination_port(chemin_source, nom) or ""
                enregistrements.append((base, categorie, nom, statut, chemin_source or "", cible))

    os.makedirs(os.path.dirname(SORTIE), exist_ok=True)
    with open(SORTIE, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["fruit", "categorie", "nom", "statut", "chemin_source", "chemin_port"])
        w.writerows(enregistrements)

    par_fruit = defaultdict(lambda: defaultdict(int))
    manquants = defaultdict(list)
    for base, categorie, nom, statut, src, cible in enregistrements:
        par_fruit[base][statut] += 1
        if statut == "A-PORTER":
            manquants[base].append((nom, categorie, cible))
    total = defaultdict(int)
    for statuts in par_fruit.values():
        for s, n in statuts.items():
            total[s] += n
    print(f"=== AUDIT ASSETS — {len(par_fruit)} fruit(s) ===\n")
    print(f"{'fruit':30s} {'A-PORTER':>9s} {'PRESENT':>8s} {'ABSENT-SRC':>10s}")
    for base in sorted(par_fruit, key=lambda b: (-par_fruit[b]["A-PORTER"], b)):
        s = par_fruit[base]
        print(f"{base:30s} {s['A-PORTER']:>9d} {s['PRESENT-PORT']:>8d} {s['ABSENT-SOURCE']:>10d}")
    print(f"\n{'TOTAL':30s} {total['A-PORTER']:>9d} {total['PRESENT-PORT']:>8d} {total['ABSENT-SOURCE']:>10d}")
    print(f"\ndétail par fruit : {SORTIE}")
    return enregistrements, manquants, par_fruit


if __name__ == "__main__":
    main()
