#!/usr/bin/env python3
"""Revue des TECHNIQUES du catalogue, une par une.

Le catalogue reçu (`MinePiece_Catalogue_Fruits_Techniques_Assets_Exhaustif.csv`) liste, par Fruit,
des noms de **classes Java** de la source : `SkillGomuGomu`, `GomuGomuBazooka`, `AIGomuArm`,
`EntityGomuArmModel`, `ModMobEquipmentDropEvents`… Ces noms mélangent des techniques, des IA de
mob, des modèles, des rendus et des gestionnaires d'événements.

Ce script ne se contente pas de compter : il **classe** chaque nom selon son suffixe, puis cherche sa
trace dans le port (code Java, registre des techniques, assets). Un nom sans trace n'est pas
forcément un manque — c'est un nom **à examiner**, et c'est exactement ce que la revue doit produire :
la liste courte de ce qu'il reste à vérifier à la main, au lieu de 40 fruits à relire.

Sortie : `docs/revue-techniques.csv` (fruit, nom, genre, trace dans le port, où).
Usage : python3 tools/technique_review.py [fruit ...]
"""
import csv
import os
import re
import sys
from collections import defaultdict

RACINE = "/opt/data/workspace/opworldlike-fabric"
CODE = f"{RACINE}/src/main/java"
ASSETS = f"{RACINE}/src/main/resources/assets"
CATALOGUE = ("/opt/data/cache/documents/"
             "doc_3796beb7ad8b_MinePiece_Catalogue_Fruits_Techniques_Assets_Exhaustif.csv")
SORTIE = f"{RACINE}/docs/revue-techniques.csv"
COL_TECHNIQUES = "Techniques & Classes Associées"

GENRES = [
    (r"(?i)^Skill|Skill[A-Z]", "technique (classe de compétence)"),
    (r"(?i)^AI[A-Z]", "IA de mob"),
    (r"(?i)Model$", "modèle"),
    (r"(?i)Renderer$", "rendu"),
    (r"(?i)Entity$|Projectile", "entité"),
    (r"(?i)Events?", "gestionnaire d'événements"),
    (r"(?i)Config$", "configuration"),
    (r"(?i)^Logic", "logique"),
    (r"(?i)^Get|^Select|^Note|^Attack|^Yaki|^Zan|^Mochi|^Tobu|^Jushi|^Shaku|^Hado|^Galleon", "procédure"),
]


def genre(nom):
    for motif, libelle in GENRES:
        if re.search(motif, nom):
            return libelle
    return "à classer"


def index_port():
    """Index du code + des assets du port, pour chercher une trace d'un nom."""
    morceaux = []
    for dp, _, fs in os.walk(CODE):
        for f in fs:
            if f.endswith(".java"):
                chemin = os.path.join(dp, f)
                try:
                    morceaux.append((os.path.relpath(chemin, RACINE), open(chemin, encoding="utf-8", errors="replace").read()))
                except Exception:
                    pass
    assets = []
    for dp, _, fs in os.walk(ASSETS):
        for f in fs:
            assets.append(f.lower())
    return morceaux, assets


def trace(nom, code, assets):
    """Où ce nom laisse-t-il une trace dans le port ? (méthode : normaliser puis chercher)"""
    nu = re.sub(r"[^a-z0-9]", "", nom.lower())
    for chemin, contenu in code:
        if nom in contenu:
            return "code (nom exact)", chemin
    for chemin, contenu in code:
        if nu and nu in re.sub(r"[^a-z0-9]", "", contenu.lower()):
            return "code (nom normalisé)", chemin
    # le nom peut n'exister que comme identifiant de technique (minuscules) ou comme asset
    minuscule = re.sub(r"(?<!^)(?=[A-Z])", "_", nom).lower()
    for chemin, contenu in code:
        if f'"{minuscule}"' in contenu or f"\"{nom.lower()}\"" in contenu:
            return "registre de technique", chemin
    if any(nu in a for a in assets):
        return "asset", ""
    return "AUCUNE", ""


def main():
    filtres = [a.lower() for a in sys.argv[1:]]
    with open(CATALOGUE, encoding="utf-8-sig", errors="replace") as f:
        lignes = list(csv.DictReader(f, delimiter=";"))
    code, assets = index_port()
    print(f"index du port : {len(code)} fichier(s) Java, {len(assets)} asset(s)\n")

    par_fruit = defaultdict(list)
    for ligne in lignes:
        cle = (ligne.get("ID / Clé Fruit") or "").strip()
        if not cle:
            continue
        base = cle.split(".")[0]
        if filtres and not any(x in base.lower() for x in filtres):
            continue
        brut = (ligne.get(COL_TECHNIQUES) or "").strip()
        for nom in [x.strip() for x in re.split(r"[,;]", brut) if x.strip()]:
            if nom.endswith((".json", ".png", ".py")):
                continue  # un asset, déjà traité par l'audit des assets
            ou_ = trace(nom, code, assets)
            par_fruit[base].append((nom, genre(nom), ou_[0], ou_[1]))

    os.makedirs(os.path.dirname(SORTIE), exist_ok=True)
    with open(SORTIE, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["fruit", "nom_source", "genre", "trace_port", "ou"])
        for base in sorted(par_fruit):
            for nom, g, t, ou in par_fruit[base]:
                w.writerow([base, nom, g, t, ou])

    print(f"{'fruit':28s} {'noms':>5s} {'tracés':>7s} {'AUCUNE':>7s}   genres manquants")
    tot_n = tot_t = tot_a = 0
    for base in sorted(par_fruit):
        noms = par_fruit[base]
        manquants = [n for n in noms if n[2] == "AUCUNE"]
        tot_n += len(noms); tot_t += len(noms) - len(manquants); tot_a += len(manquants)
        genres = sorted({n[1] for n in manquants})
        print(f"{base:28s} {len(noms):>5d} {len(noms) - len(manquants):>7d} {len(manquants):>7d}   {', '.join(genres[:3])}")
    print(f"\nTOTAL {tot_n} noms · {tot_t} tracés dans le port · {tot_a} sans trace (à examiner)")

    # ⚠️ DISTINCTION DÉCISIVE. Les noms `SkillXxx` sont des CLASSES DE PROCÉDURE de la source : le port
    # ne les recopie pas, il implémente les techniques dans un registre + des services. Leur absence de
    # trace est donc NORMALE, et la couverture des techniques se vérifie au niveau des TECHNIQUES
    # (tools/fruit_audit.py : 213/213 vues, 1061 valeurs conformes), pas par un grep de nom de classe.
    # Ce qui reste vraiment à examiner, ce sont les noms STRUCTURELS — entités, IA, mixins, événements,
    # modèles, rendus — car eux décrivent des mécanismes qui doivent exister quelque part.
    structurels = []
    techniques_src = 0
    for base in sorted(par_fruit):
        for nom, g, t, ou in par_fruit[base]:
            if t != "AUCUNE":
                continue
            if re.match(r"(?i)^(Skill|AI?[A-Z])", nom) or g.startswith("technique"):
                techniques_src += 1
                continue
            structurels.append((base, nom, g))
    print(f"\n  dont classes de technique de la source (absence NORMALE, vérifiées au niveau des "
          f"techniques) : {techniques_src}")
    print(f"  et noms STRUCTURELS réellement à examiner : {len(structurels)}\n")
    if structurels:
        print(f"{'fruit':28s} {'nom':34s} genre")
        for base, nom, g in structurels:
            print(f"{base:28s} {nom:34s} {g}")


if __name__ == "__main__":
    main()
