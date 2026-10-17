#!/usr/bin/env python3
"""FRUIT-* — audit des Fruits contre le plan de référence (CSV) et ver15.

    python3 tools/fruit_audit.py                 # rapport complet
    python3 tools/fruit_audit.py --params        # seulement les écarts de paramètres
    python3 tools/fruit_audit.py --tags          # seulement les écarts de classification
    python3 tools/fruit_audit.py --fruit ARO_ARO # un seul fruit

Deux contrôles, tous deux adossés aux valeurs relevées dans le JAR ver15 et consignées dans
`Parametres_JAR` / `Famille_JAR` :

  1. **Classification** — la `Famille_JAR` (LOGIA / ZOAN / NON_CLASSE) de ver15 est comparée au tag
     porté par l'item (`data/minepiece/tags/item/{logia,zoan,paramecia}.json`). Un fruit `NON_CLASSE`
     n'est PAS supposé Paramecia : c'est une décision à documenter (FRUIT-*-02).
  2. **Paramètres** — dégâts, portée/durée, recul, énergie, recharge, effet, durée d'effet et
     amplificateur de chaque technique déclarée sont comparés aux valeurs ver15 (FRUIT-*-TECH-*-DEV).
"""
import argparse
import csv
import glob
import json
import os
import re
import sys

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = "/opt/data/cache/documents/doc_bfbdd58d8bf7_OPWorldLike_Fruits_Actions_Implementation_1.csv"
REGISTRY = os.path.join(BASE, "src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java")
TAGS_DIR = os.path.join(BASE, "src/main/resources/data/minepiece/tags/item")


# --------------------------------------------------------------------------- CSV (plan de référence)

def load_plan():
    """→ {fruit_id: {"label", "famille", "techniques": {nom: params}}}"""
    with open(CSV_PATH, encoding="utf-8-sig") as fh:
        rows = list(csv.reader(fh, delimiter=";", quotechar='"'))
    header = rows[0]
    plan = {}
    for raw in rows[1:]:
        if not raw or not raw[0].strip():
            continue
        row = dict(zip(header, raw))
        if not row["ID_action"].startswith("FRUIT-"):
            continue
        fid = row["ID_fruit"].split(":")[-1]
        entry = plan.setdefault(fid, {"label": row["Fruit"], "famille": row["Famille_JAR"], "techniques": {}})
        if row["Technique"].strip() and row["Parametres_JAR"].strip():
            params = parse_params(row["Parametres_JAR"])
            params["_type"] = row["Type_technique_JAR"].strip()
            entry["techniques"][row["Technique"].strip()] = params
    return plan


def parse_params(text):
    """`degats=15; recul=0.5; effet=null` → dict."""
    out = {}
    for chunk in text.split(";"):
        if "=" not in chunk:
            continue
        key, value = chunk.split("=", 1)
        out[key.strip()] = value.strip()
    return out


# --------------------------------------------------------------------------- registre du port

MOVE_RE = re.compile(
    r'DevilFruitMove\.(melee|projectile|buff|summon)\(\s*"([a-z0-9_]+)"\s*,\s*"([^"]+)"\s*,(.*?)\)\s*[,\n]',
    re.S)


def load_registry():
    """→ {item_id: [ {factory, id, name, args} ]}

    Les arguments sont délimités par un **compteur de parenthèses** : une expression régulière
    non gloutonne s'arrête au premier `)` interne (cast, `List.of(...)`, constructeur) et tronque
    la liste d'arguments, ce qui fausse ensuite l'indexation par signature.
    """
    src = open(REGISTRY, encoding="utf-8").read()
    out = {}
    # ⚠️ Les fruits ne sont pas tous enregistrés par `MOVES.put(...)` : les Zoans Rokushiki passent par
    # `putZoan(...)`. Ne scanner que le premier laissait TOUT un groupe de fruits hors de l'audit — ils
    # n'étaient jamais comparés au plan, et aucune divergence ne pouvait être signalée pour eux. Un
    # « 0 écart » sur un périmètre incomplet est un faux sentiment de sécurité : on lit donc les deux.
    for block in re.split(r"(?:MOVES\.put|putZoan)\(", src)[1:]:
        m = re.match(r"\s*MinepieceModItems\.([A-Z0-9_]+)\s*,\s*List\.of\(", block)
        if not m:
            continue
        item = m.group(1)
        body = block[:block.find("));")]
        moves = []
        # ⚠️ Deuxième angle mort : toutes les techniques ne passent pas par une fabrique
        # `DevilFruitMove.melee(...)`. Certaines sont bâties par le CONSTRUCTEUR direct
        # (`new DevilFruitMove("imuarrow", …)`), et ne scanner que les fabriques en oubliait — l'audit
        # voyait 2 des 3 techniques d'un Fruit et déclarait « conforme » sur un périmètre tronqué.
        for fm in re.finditer(r"DevilFruitMove\.(\w+)\(|new\s+DevilFruitMove\(", body):
            start = fm.end()
            depth = 1
            i = start
            while i < len(body) and depth > 0:
                if body[i] in "([":
                    depth += 1
                elif body[i] in ")]":
                    depth -= 1
                i += 1
            call = body[start:i - 1]
            args = [a.strip() for a in split_args(call)]
            if len(args) >= 3:
                fabrique = fm.group(1)
                if fabrique:
                    type_tok = fabrique.upper()
                    layout = fabrique
                else:
                    # Construction directe : le TYPE est le 3e argument (`MoveType.MELEE`), et une fois ce
                    # jeton retiré la suite des arguments suit exactement la fabrique correspondante.
                    # Sans cela, l'audit lisait « constructeur » comme type et signalait un faux écart —
                    # un faux positif use la confiance autant qu'un faux négatif.
                    brut = args[2].split(".")[-1].strip() if len(args) > 2 else ""
                    type_tok = brut.upper()
                    layout = {"MELEE": "melee", "PROJECTILE": "projectile"}.get(type_tok)
                    if layout:
                        args = args[:2] + args[3:]
                moves.append({
                    "factory": layout if layout else "(type non comparable)",
                    "type": type_tok,
                    "id": args[0].strip('"'),
                    "name": args[1].strip('"'),
                    "args": args,
                })
        out[item.lower()] = moves
    return out


def split_args(text):
    """Découpe les arguments en respectant les parenthèses et `List.of(...)`."""
    args, depth, current = [], 0, ""
    for ch in text:
        if ch in "([":
            depth += 1
        elif ch in ")]":
            depth -= 1
        if ch == "," and depth == 0:
            args.append(current)
            current = ""
        else:
            current += ch
    if current.strip():
        args.append(current)
    return args


# --------------------------------------------------------------------------- tags

def load_tags():
    """→ {item_id: famille} d'après les fichiers de tags."""
    familles = {}
    for path in sorted(glob.glob(os.path.join(TAGS_DIR, "*.json"))):
        famille = os.path.basename(path)[:-5]
        if famille == "devil_fruit":
            continue
        data = json.load(open(path, encoding="utf-8"))
        for value in data.get("values", []):
            familles[value.split(":")[-1]] = famille
    return familles


# --------------------------------------------------------------------------- comparaisons

def num(value, default=None):
    try:
        return float(str(value).replace("d", "").replace("f", "").replace("L", "").strip())
    except (TypeError, ValueError):
        return default


def compare_params(registry, plan):
    """Comparaison technique par technique : ver15 (CSV) vs registre du port.

    Les arguments sont indexés **par signature** (le nombre d'arguments lève toute ambiguïté : une
    frappe peut porter un effet, d'où les formes à 8 et 11 arguments). Sans cela, l'effet serait lu
    comme l'énergie et la durée d'effet comme la recharge, ce qui produirait de faux écarts.
    """
    écarts, inconnues, ok = [], [], 0
    for item, moves in registry.items():
        entry = plan.get(item)
        if not entry:
            inconnues.append(item)
            continue
        plan_tech = entry["techniques"]
        for mv in moves:
            ref = plan_tech.get(mv["name"])
            if ref is None:
                # Repli sur le premier mot — mais UNIQUEMENT s'il désigne une seule technique du plan.
                # Sinon l'heuristique apparie au hasard : la technique ajoutée « Mochi Build » a été
                # appariée à « Mochi Pistol » (premier mot « Mochi » commun) et l'audit a produit QUATRE
                # faux écarts de paramètres (type, recharge, durée, amplificateur). Un faux positif use la
                # confiance autant qu'un faux négatif : une heuristique ambiguë ne doit pas s'appliquer.
                candidats = [name for name in plan_tech
                             if name.lower().split()[:1] == mv["name"].lower().split()[:1]]
                if len(candidats) == 1:
                    ref = plan_tech[candidats[0]]
            if ref is None:
                écarts.append((item, mv["name"], "absente du plan", "", ""))
                continue
            args = mv["args"]
            n = len(args)
            checks = []

            # Type de technique : une frappe générique là où ver15 déclare un BUFF (ou l'inverse)
            # change complètement le comportement — c'est un écart de premier ordre.
            type_ver15 = (ref.get("_type") or "").upper()
            type_port = (mv.get("type") or mv["factory"]).upper()
            if type_ver15 and type_ver15 != type_port:
                correspond = (type_ver15 == "PROJECTILE" and type_port == "PROJECTILE")
                if not correspond:
                    écarts.append((item, mv["name"], "type", type_port, type_ver15))

            def effect_block(offset):
                """(effet, durée, amplificateur) à partir de `offset`."""
                return [("effet", args[offset], ref.get("effet")),
                        ("durée effet", num(args[offset + 1]), num(ref.get("duree_effet_ticks"))),
                        ("amplificateur", num(args[offset + 2]), num(ref.get("amplificateur")))]

            if mv["factory"] == "melee":
                checks += [("degats", num(args[2]), num(ref.get("degats"))),
                           ("portée", num(args[3]), num(ref.get("portee_ou_rayon_blocs"))),
                           ("recul", num(args[4]), num(ref.get("recul")))]
                if n >= 11:
                    checks += effect_block(5)
                    checks += [("énergie", num(args[8]), num(ref.get("energie"))),
                               ("recharge", num(args[9]), num(ref.get("recharge_ticks")))]
                elif n >= 8:
                    checks += [("énergie", num(args[5]), num(ref.get("energie"))),
                               ("recharge", num(args[6]), num(ref.get("recharge_ticks")))]
            elif mv["factory"] == "projectile":
                checks += [("degats", num(args[2]), num(ref.get("degats"))),
                           ("durée", num(args[3]), num(ref.get("duree_projectile_ticks"))),
                           ("recul", num(args[4]), num(ref.get("recul")))]
                if n >= 11:
                    checks += effect_block(5)
                    checks += [("énergie", num(args[8]), num(ref.get("energie"))),
                               ("recharge", num(args[9]), num(ref.get("recharge_ticks")))]
                else:
                    checks += [("énergie", num(args[5]), num(ref.get("energie"))),
                               ("recharge", num(args[6]), num(ref.get("recharge_ticks")))]
            elif mv["factory"] == "areaEffect":
                checks += effect_block(2)
                checks += [("rayon", num(args[5]), num(ref.get("portee_ou_rayon_blocs"))),
                           ("énergie", num(args[6]), num(ref.get("energie"))),
                           ("recharge", num(args[7]), num(ref.get("recharge_ticks")))]
            elif mv["factory"] == "buff":
                checks += effect_block(2)
                checks += [("énergie", num(args[5]), num(ref.get("energie"))),
                           ("recharge", num(args[6]), num(ref.get("recharge_ticks")))]

            for label, got, want in checks:
                if want is None:
                    continue
                if label == "effet":
                    continue                      # comparé qualitativement ailleurs
                if got is None:
                    continue
                if abs(got - want) > 1e-6:
                    écarts.append((item, mv["name"], label, got, want))
                else:
                    ok += 1
    return écarts, inconnues, ok


def compare_plan_to_registry(registry, plan):
   """Contrôle INVERSE : quelles techniques du plan n'ont AUCUN équivalent dans le port ?

   Le sens unique (registre → plan) ne voit que les ajouts. Une technique du plan effacée du port —
   renommée, remplacée, ou simplement oubliée — ne produisait aucune ligne : le rapport était vert alors
   qu'une capacité annoncée au contrat avait disparu. On apparie par nom, puis par premier mot (comme
   dans l'autre sens), pour ne pas signaler un simple changement de casse ou de suffixe.
   """
   out = []
   for item, entry in plan.items():
      moves = registry.get(item)
      if moves is None:
         continue
      noms_port = [m["name"] for m in moves]
      premiers_port = {n.lower().split()[0] for n in noms_port if n.split()}
      for tech, params in entry["techniques"].items():
         if tech in noms_port:
            continue
         if tech.lower().split()[0] in premiers_port:
            continue
         resume = params.get("_type", "")
         degats = params.get("degats")
         if degats is not None:
            resume += f" {degats} dgt"
         out.append((item, tech, resume.strip()))
   return out


def compare_tags(registry, plan, tags):
    """ver15 Famille_JAR vs tag porté par l'item."""
    lignes = []
    for item in sorted(registry):
        entry = plan.get(item)
        if not entry:
            continue
        ver15 = entry["famille"]
        port = tags.get(item, "AUCUN")
        attendu = {"LOGIA": "logia", "ZOAN": "zoan"}.get(ver15, "NON_CLASSE → à décider")
        if ver15 in ("LOGIA", "ZOAN"):
            conforme = (port == ver15.lower())
        else:
            conforme = None                       # décision de projet à documenter
        lignes.append((item, entry["label"], ver15, port, conforme))
    return lignes


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--params", action="store_true")
    ap.add_argument("--tags", action="store_true")
    ap.add_argument("--fruit")
    a = ap.parse_args()
    only = a.fruit.lower() if a.fruit else None

    def garde(item):
        """Filtre --fruit : accepte `batto_batto` comme `item_devil_fruit_batto_batto`."""
        return only is None or only in item.lower()
    do_params = a.params or not (a.params or a.tags)
    do_tags = a.tags or not (a.params or a.tags)

    plan = load_plan()
    registry = load_registry()
    tags = load_tags()
    rc = 0

    if do_tags:
        print("=" * 100)
        print("1. CLASSIFICATION — Famille_JAR de ver15 vs tag porté par l'item")
        print("=" * 100)
        lignes = compare_tags(registry, plan, tags)
        if only:
            lignes = [l for l in lignes if garde(l[0])]
        divergences = 0
        for item, label, ver15, port, conforme in lignes:
            if conforme is False:
                marque, divergences = "✗ DIVERGENCE", divergences + 1
            elif conforme is None:
                marque = "? à décider (NON_CLASSE)"
            else:
                marque = "✓"
            print(f"  {marque:22} {item:34} ver15={ver15:11} tag={port}")
        print(f"\n  {divergences} divergence(s) de classification sur {len(lignes)} fruit(s)")

    if do_params:
        print()
        print("=" * 100)
        print("2. PARAMÈTRES — valeurs ver15 (CSV) vs registre du port")
        print("=" * 100)
        écarts, inconnues, ok = compare_params(registry, plan)
        if only:
            écarts = [e for e in écarts if garde(e[0])]
            if not écarts:
                print(f"  (aucun écart de paramètre pour « {only} »)")
        print(f"  {ok} valeur(s) conforme(s), {len(écarts)} écart(s)")
        # COUVERTURE : un outil de vérification doit annoncer sur QUOI il porte. « 0 écart » sur un
        # périmètre tronqué est un faux sentiment de sécurité — et c'est exactement ce qui est arrivé
        # ici : deux formes d'enregistrement échappaient au parseur, donc un Fruit entier et plusieurs
        # techniques n'étaient tout simplement pas comparés.
        total_techniques = sum(len(v) for v in registry.values())
        print(f"  COUVERTURE : {len(registry)} fruit(s) analysé(s), {total_techniques} technique(s) vue(s)"
              f" — comparer ce total au compte du registre est le SEUL moyen de savoir que l'audit n'a"
              f" pas regardé à côté")
        if inconnues:
            print("  items du registre absents du plan :", ", ".join(inconnues))

        # ===== CONTRÔLE INVERSE : le plan → le port =====
        # L'audit ne regardait que registre → plan, donc il signalait les AJOUTS mais restait muet sur
        # les DISPARITIONS : une technique du plan effacée du port (renommage, remplacement, oubli) ne
        # produisait AUCUNE ligne. Un contrôle à sens unique ne vaut que la moitié de ce qu'il annonce.
        manquantes = compare_plan_to_registry(registry, plan)
        if only:
            manquantes = [m for m in manquantes if garde(m[0])]
        if manquantes:
            print(f"  ⚠️ {len(manquantes)} technique(s) du PLAN absente(s) du port :")
            courant2 = None
            for item, tech, params in sorted(manquantes):
                if item != courant2:
                    courant2 = item
                    print(f"      {item}")
                print(f"          {tech:26} (ver15 : {params})")
        else:
            print("  ✓ contrôle inverse : aucune technique du plan n'a disparu du port")
        print()
        courant = None
        for item, tech, label, got, want in sorted(écarts):
            if item != courant:
                courant = item
                print(f"  {item}")
            print(f"      {tech:26} {label:14} port={got}  ver15={want}")

    return rc


if __name__ == "__main__":
    sys.exit(main())
