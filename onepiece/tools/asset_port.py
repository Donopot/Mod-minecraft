#!/usr/bin/env python3
"""Porte les assets des Fruits du jar source vers le port Fabric, FRUIT PAR FRUIT.

Principes (tirés de la méthode du portage) :
  * on **copie les octets** du jar source, sans retoucher, sans redessiner, sans renommer ;
  * on **respecte l'arborescence d'accueil du port** : `geckolib/models/` pour les .geo.json,
    `geckolib/animations/` pour les .animation.json, `textures/<sous-dossier>/` pour les png ;
  * on **suit les dépendances** : un .geo.json déclare ses textures, qui sont portées même si le
    catalogue ne les liste pas ;
  * on **ne porte pas** les modèles d'objet 1.20.1 (`models/item/*.json`) : le port les a réécrits au
    format 1.21+ (`items/*.json` + `models/item/*.json`), et copier les anciens serait du poids mort ;
  * on est **idempotent** : un fichier déjà présent à la même taille n'est pas réécrit ;
  * on écrit un **manifeste** par fruit (chemins, tailles) — une preuve, pas une impression.

Usage :
  python3 tools/asset_port.py --list                 # ce qui reste à porter, par fruit
  python3 tools/asset_port.py aro_aro                # porte les assets d'un fruit
  python3 tools/asset_port.py --all                  # porte tout (fruit par fruit)
"""
import argparse
import csv
import json
import os
import re
import sys
import zipfile
from collections import defaultdict

JAR = "/opt/data/workspace/minepiece/MinePiece-ver15-forge-1.20.1.jar"
PORT = "/opt/data/workspace/opworldlike-fabric/src/main/resources"
AUDIT = "/opt/data/workspace/opworldlike-fabric/docs/audit-assets.csv"
MANIFESTES = "/opt/data/workspace/opworldlike-fabric/docs/assets-portes.json"

# Ce que l'on ne copie pas, et pourquoi — écrit noir sur blanc plutôt que filtré en silence.
EXCLUS_MOTIFS = [
    (r"^models/item/", "modèle d'objet 1.20.1 : réécrit au format 1.21+ dans le port"),
    (r"^items/", "définition d'objet déjà fournie par le port"),
]


def charger_audit():
    with open(AUDIT, encoding="utf-8") as f:
        return list(csv.DictReader(f, delimiter=";"))


def nom_propre(fruit, nom):
    """L'asset appartient-il au fruit, ou vient-il d'un autre contenu (PNJ, vêtements) ?

    ⚠️ La racine doit être un MOT ENTIER, pas une sous-chaîne : « ethan_b**aro**n_v_nasujuro »
    contient « aro », et une première version de cette fonction a donc rangé deux textures d'un
    autre personnage dans le Fruit Aro Aro. Un test de sous-chaîne sur des noms composés est
    toujours trop permissif.
    """
    tokens = [x for x in re.split(r"[^a-z0-9]+", nom.lower()) if x]
    racine = fruit.split("_")[0]
    plat = re.sub(r"[^a-z0-9]", "", nom.lower())
    suffixe_numerote = any(x.startswith(racine) and x[len(racine):].isdigit() for x in tokens)
    return racine in tokens or suffixe_numerote or re.sub(r"[^a-z0-9]", "", fruit.lower()) in plat


def destination(chemin_source, nom):
    if "/geo/" in chemin_source:
        return f"assets/minepiece/geckolib/models/{nom}"
    if "/animations/" in chemin_source:
        return f"assets/minepiece/geckolib/animations/{nom}"
    if "/textures/" in chemin_source:
        return "assets/minepiece/textures/" + chemin_source.split("/textures/", 1)[1]
    if chemin_source.endswith(".mcmeta"):
        return "assets/minepiece/textures/" + os.path.basename(chemin_source)
    return None


def textures_reclamees(z, chemin_geo):
    try:
        brut = z.read(chemin_geo).decode("utf-8", "replace")
    except Exception:
        return []
    noms = set()
    for bloc in re.findall(r'"textures?"\s*:\s*\{([^}]*)\}', brut):
        noms |= set(re.findall(r'"\w+"\s*:\s*"([^"]+)"', bloc))
    if not noms:
        noms |= set(re.findall(r'"textures?"\s*:\s*"([^"]+)"', brut))
    return sorted(os.path.basename(x) for x in noms)


def planifier(fruit, audit):
    """Renvoie (a_copier, ignores, absents) pour un fruit. Rien n'est copié ici."""
    z = zipfile.ZipFile(JAR)
    par_nom = defaultdict(list)
    for chemin in z.namelist():
        if not chemin.endswith("/"):
            par_nom[os.path.basename(chemin)].append(chemin)

    def choisir(cands):
        if not cands:
            return None
        def rang(c):
            if "/geo/" in c:
                return 0
            if "/animations/" in c:
                return 0
            if "/textures/entities/" in c:
                return 0
            if "/textures/item/" in c:
                return 1
            if "/textures/block/" in c:
                return 2
            return 3
        return sorted(cands, key=rang)[0]

    a_copier, ignores = [], []
    lignes = [l for l in audit if l["fruit"] == fruit]
    for l in lignes:
        if l["statut"] != "A-PORTER":
            continue
        motif = next((m for m, _ in EXCLUS_MOTIFS if re.search(m, l["nom"]) or re.search(m, l["chemin_source"])), None)
        if motif:
            ignores.append((l["nom"], next(r for m, r in EXCLUS_MOTIFS if m == motif)))
            continue
        if not nom_propre(fruit, l["nom"]):
            ignores.append((l["nom"], "asset d'un autre contenu (PNJ, vêtement) : hors du fruit"))
            continue
        src = l["chemin_source"]
        nom = os.path.basename(src) if src else l["nom"]
        cible = destination(src, nom) if src else None
        if not cible:
            ignores.append((l["nom"], "type sans destination connue dans le port"))
            continue
        a_copier.append((src, cible, nom))
        # dépendances : les textures déclarées PAR le geo
        if src.endswith(".geo.json"):
            for t in textures_reclamees(z, src):
                cands = [c for c in par_nom.get(t, []) if "/textures/" in c]
                if cands:
                    c = choisir(cands)
                    a_copier.append((c, destination(c, t), t))
    # dédoublonnage en gardant l'ordre
    vus, propres = set(), []
    for s, c, n in a_copier:
        if c in vus:
            continue
        vus.add(c)
        propres.append((s, c, n))
    return propres, ignores


def porter(fruit, audit, journal):
    z = zipfile.ZipFile(JAR)
    a_copier, ignores = planifier(fruit, audit)
    ajoutes, deja, octets = [], [], 0
    for src, cible, nom in a_copier:
        chemin_port = os.path.join(PORT, cible)
        donnees = z.read(src)
        if os.path.exists(chemin_port) and os.path.getsize(chemin_port) == len(donnees):
            deja.append(cible)
            continue
        os.makedirs(os.path.dirname(chemin_port), exist_ok=True)
        with open(chemin_port, "wb") as f:
            f.write(donnees)
        with open(chemin_port, "rb") as f:
            assert f.read() == donnees, f"copie non conforme : {cible}"
        ajoutes.append(cible)
        octets += len(donnees)
    journal[fruit] = {"ajoutes": sorted(ajoutes), "deja_presents": sorted(deja),
                      "ignores": sorted(f"{n} ({r})" for n, r in ignores),
                      "octets": octets}
    print(f"{fruit:30s} +{len(ajoutes):3d} portés · {len(deja):3d} déjà là · {len(ignores):3d} ignorés"
          f" · {octets / 1024:8.1f} Ko")
    return len(ajoutes)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("fruits", nargs="*")
    ap.add_argument("--list", action="store_true")
    ap.add_argument("--all", action="store_true")
    args = ap.parse_args()
    audit = charger_audit()
    restants = defaultdict(int)
    for l in audit:
        if l["statut"] == "A-PORTER":
            restants[l["fruit"]] += 1
    if args.list:
        for f_, n in sorted(restants.items(), key=lambda x: -x[1]):
            print(f"  {f_:30s} {n:3d} asset(s) à porter")
        print(f"  TOTAL {sum(restants.values())} sur {len(restants)} fruit(s)")
        return
    cibles = sorted(restants) if args.all else args.fruits
    if not cibles:
        ap.error("indique un fruit, --all ou --list")
    journal = {}
    if os.path.exists(MANIFESTES):
        journal = json.load(open(MANIFESTES, encoding="utf-8"))
    total = 0
    for f_ in cibles:
        if f_ not in restants:
            print(f"{f_:30s} rien à porter (fruit inconnu de l'audit ou déjà complet)")
            continue
        total += porter(f_, audit, journal)
    os.makedirs(os.path.dirname(MANIFESTES), exist_ok=True)
    json.dump(journal, open(MANIFESTES, "w", encoding="utf-8"), ensure_ascii=False, indent=1, sort_keys=True)
    print(f"\ntotal porté sur cette exécution : {total} fichier(s)")
    print(f"manifeste : {MANIFESTES}")


if __name__ == "__main__":
    main()
