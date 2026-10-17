#!/usr/bin/env python3
"""Phase C — convertir les props du pack Astartes en blocs décoratifs.

Le pack implémente ses props comme des **modèles d'objets** portés par des items vanilla
(`custom_model_data`). On les réutilise comme blocs posables.

Pas de cuisson en atlas ici, contrairement aux armes : ce sont des BLOCS, et un modèle de bloc
accepte plusieurs textures nativement (`{"0":…,"1":…}` + faces `"texture":"#0"`). La limite
« Indigo ne rend pas le multi-textures » ne concerne que les modèles d'OBJETS. C'est le principal
gain de cette méthode : on copie le modèle source presque tel quel.

Ce que le script corrige automatiquement, modèle par modèle :
  - **mise à l'échelle** : un modèle de prop est plus grand qu'un bloc (une « grande caisse » fait
    43 unités de large, soit 2,7 blocs). Réduit pour tenir dans 16 unités, sinon le bloc déborde
    de tous les côtés et le rendu est incohérent. Le facteur est calculé, pas choisi au hasard.
  - **recentrage vertical** : les cadavres du pack descendent jusqu'à Y = -7, donc ils
    s'enterreraient. Remontés pour que le minimum soit à 0.
  - **textures** : copiées sous leur nom de base dans `textures/block/` et référencées en
    `<modid>:block/<nom>`. Les props partagent des textures, la déduplication est faite.

Écrit 4 fichiers par prop (modèle de bloc, blockstate, modèle d'item, définition d'item 26.3),
puis imprime les lignes Java et les entrées de langue à reporter dans le code.

Usage :  python3 tools/convert_astartes_props.py [--simulation]
"""
from __future__ import annotations

import argparse
import json
import os
import shutil

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACK = ("/opt/data/workspace/astartes-extract/resourcepack/"
        "ASTARTES ARMOURY Resource Pack 2025")
SRC_MODELS = os.path.join(PACK, "assets/minecraft/models/item")
SRC_TEXTURES = [os.path.join(PACK, "assets/minecraft/textures/item/custom"),
                os.path.join(PACK, "assets/minecraft/textures/item"),
                os.path.join(PACK, "assets/minecraft/textures/block")]

NS = "voiddynasty"
DEST_ASSETS = os.path.join(RACINE, "src/main/resources/assets", NS)

# (identifiant du mod, modèle source, échelle, décalage Y, nom français, nom anglais)
#
# échelle None = « réduit juste assez pour tenir dans un bloc » (calculé).
# Les véhicules (lemunruss, fightership) ne sont PAS ici : ils font 3 blocs et plus, leur place est
# dans une structure, pas dans un bloc.
PROPS = [
    # caisses : 43 unités de large → réduites, sinon un bloc de 2,7 blocs de large
    ("crate",             "largecrate",           None, 0, "Caisse de stockage",  "Storage crate"),
    ("crate_green",       "largecrategreen",      None, 0, "Caisse de stockage verte", "Green storage crate"),
    ("crate_red",         "largecratered",        None, 0, "Caisse de stockage rouge", "Red storage crate"),
    ("crate_shut",        "largecrateshut",       None, 0, "Caisse de stockage fermée", "Shut storage crate"),
    # gothique
    ("gothic_panel",      "gothicbutton",         None, 0, "Panneau gothique",    "Gothic panel"),
    # cadavres : Y descend à -7 → remontés, sinon enterrés
    ("corpse_1",          "astracorpse1",         None, 0, "Cadavre",             "Corpse"),
    ("corpse_2",          "astracorpse2",         None, 0, "Cadavre mutilé",      "Mangled corpse"),
    ("corpse_3",          "astracorpse3",         None, 0, "Cadavre décharné",    "Bony corpse"),
    ("corpse_4",          "astracorpse4",         None, 0, "Cadavre retourné",    "Fallen corpse"),
    ("corpse_5",          "astracorpse5",         None, 0, "Cadavre affaissé",    "Slumped corpse"),
    ("corpse_6",          "astracorpse6",         None, 0, "Cadavre démembré",    "Dismembered corpse"),
    # sang / gore : décals plats au sol
    ("blood_stain",       "bloodsplatter",        1.0,  0, "Tache de sang",       "Blood stain"),
    ("blood_arrow",       "bloodarrow",           1.0,  0, "Flèche de sang",      "Blood arrow"),
    ("blast_mark",        "blastmark",            None, 0, "Marque d'explosion",  "Blast mark"),
    ("blood_god",         "bloodforbloodgod",     None, 0, "Marque du Dieu du Sang", "Blood God mark"),
    # divers
    ("spent_shell",       "cannonshellspent",     1.0,  0, "Obus usagé",          "Spent shell"),
    ("battery_pack",      "battery_pack",         1.0,  0, "Bloc-batterie",       "Battery pack"),
    ("wax_seal",          "seal",                 1.0,  0, "Sceau de cire",       "Wax seal"),
    ("corpse_starch",     "corpsestarch",         None, 0, "Amidon de cadavre",   "Corpse starch"),
    # armes lourdes posées
    ("laz_cannon",        "lazcannon",            None, 0, "Canon laser",         "Lascannon"),
    ("multimelta_turret", "multimelta",           None, 0, "Tourelle multifuseur", "Multimelta turret"),
]

RAPPORT = {"ok": [], "deja": [], "textures_copiees": [], "textures_absentes": []}


def nombre(v: float) -> float:
    return round(float(v), 3)


def transformer(element: dict, k: float, dy: float) -> dict:
    """Applique échelle + décalage vertical à un élément du modèle source."""
    e = json.loads(json.dumps(element))
    e["from"] = [nombre(e["from"][0] * k), nombre(e["from"][1] * k + dy), nombre(e["from"][2] * k)]
    e["to"] = [nombre(e["to"][0] * k), nombre(e["to"][1] * k + dy), nombre(e["to"][2] * k)]
    for face in e.get("faces", {}).values():
        # attention : sur une FACE, `rotation` est un entier (0/90/180/270). Seule la forme
        # Blockbench étendue {'origin': [...], 'axis': …, 'angle': …} porte une origine à
        # transformer — un `if rot and "origin" in rot` plante sur l'entier.
        rot = face.get("rotation")
        if isinstance(rot, dict) and isinstance(rot.get("origin"), list):
            o = rot["origin"]
            rot["origin"] = [nombre(o[0] * k), nombre(o[1] * k + dy), nombre(o[2] * k)]
    return e


def source_texture(base: str) -> str | None:
    for dossier in SRC_TEXTURES:
        p = os.path.join(dossier, base + ".png")
        if os.path.exists(p):
            return p
    return None


def convertir(ident: str, modele: str, echelle, dy_force: float, fr: str, en: str, simulation: bool) -> dict | None:
    src = os.path.join(SRC_MODELS, modele + ".json")
    if not os.path.exists(src):
        RAPPORT["textures_absentes"].append(f"{ident} : modèle source {modele}.json introuvable")
        return None
    d = json.load(open(src, encoding="utf-8"))
    elements = d.get("elements")
    if not elements:
        RAPPORT["textures_absentes"].append(f"{ident} : {modele} n'a pas d'éléments (défini par parent ?)")
        return None

    # encombrement réel du modèle, toutes faces confondues
    xs = [v for e in elements for v in (e["from"][0], e["to"][0])]
    ys = [v for e in elements for v in (e["from"][1], e["to"][1])]
    zs = [v for e in elements for v in (e["from"][2], e["to"][2])]
    encombrement = max(max(xs) - min(xs), max(ys) - min(ys), max(zs) - min(zs))

    # échelle : réduit juste assez pour tenir dans 16 unités (un bloc)
    k = echelle if echelle is not None else min(1.0, 16.0 / encombrement) if encombrement > 16 else 1.0
    dy = -min(ys) * k + dy_force          # le minimum repasse à 0 → le prop repose sur le bloc

    # textures : copiées par nom de base, référencées en <ns>:block/<base>
    textures, remap = {}, {}
    for cle, valeur in d.get("textures", {}).items():
        if not isinstance(valeur, str) or valeur.startswith("minecraft:"):
            textures[cle] = valeur
            continue
        base = valeur.split("/")[-1]
        remap[cle] = f"{NS}:block/{base}"
        textures[cle] = remap[cle]
        cible = os.path.join(DEST_ASSETS, "textures/block", base + ".png")
        src_tex = source_texture(base)
        if src_tex is None:
            RAPPORT["textures_absentes"].append(f"{ident} : texture {base}.png introuvable dans le pack")
        elif not os.path.exists(cible):
            if not simulation:
                os.makedirs(os.path.dirname(cible), exist_ok=True)
                shutil.copy2(src_tex, cible)
            RAPPORT["textures_copiees"].append(base)

    nouveau = {"textures": textures, "elements": [transformer(e, k, dy) for e in elements]}

    fichiers = {
        f"models/block/{ident}.json": nouveau,
        f"blockstates/{ident}.json": {"variants": {"": {"model": f"{NS}:block/{ident}"}}},
        f"models/item/{ident}.json": {"parent": f"{NS}:block/{ident}"},
        f"items/{ident}.json": {"model": {"type": "minecraft:model", "model": f"{NS}:item/{ident}"}},
    }

    # GARDE-FOU : ne jamais écraser un asset qui existe déjà dans le dépôt.
    # Leçon vécue : un prop nommé `wax_seal` a écrasé le modèle et la définition de l'OBJET
    # `wax_seal` (du contenu sans rapport, déjà livré) — et comme un identifiant d'objet ne peut pas
    # être enregistré deux fois, le mod ne démarrait plus du tout. Un convertisseur qui écrit dans le
    # dépôt doit refuser d'écraser, pas espérer que les noms ne se croiseront jamais.
    collisions = [rel for rel in fichiers
                  if os.path.exists(os.path.join(DEST_ASSETS, rel))
                  and rel.split("/")[0] in ("items", "models/item") ]
    if collisions:
        RAPPORT["textures_absentes"].append(
            f"{ident} : REFUSÉ, {len(collisions)} fichier(s) déjà présent(s) — {', '.join(collisions)}")
        return None

    if not simulation:
        for rel, contenu in fichiers.items():
            p = os.path.join(DEST_ASSETS, rel)
            os.makedirs(os.path.dirname(p), exist_ok=True)
            with open(p, "w", encoding="utf-8") as f:
                json.dump(contenu, f, indent=2, ensure_ascii=False)
                f.write("\n")

    nouveau_min_y = min(v for e in nouveau["elements"] for v in (e["from"][1], e["to"][1]))
    nouveau_max_y = max(v for e in nouveau["elements"] for v in (e["from"][1], e["to"][1]))
    RAPPORT["ok"].append(ident)
    return {"ident": ident, "elements": len(elements), "k": round(k, 3), "dy": round(dy, 2),
            "y": f"{nouveau_min_y:.1f}..{nouveau_max_y:.1f}", "fr": fr, "en": en}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--simulation", action="store_true")
    a = ap.parse_args()

    resultats = []
    for ident, modele, echelle, dy, fr, en in PROPS:
        r = convertir(ident, modele, echelle, dy, fr, en, a.simulation)
        if r:
            resultats.append(r)

    print(f"{'bloc':20s} {'élém':>5s} {'échelle':>8s} {'décal.':>7s} {'Y final':>10s}")
    for r in resultats:
        print(f"  {r['ident']:20s} {r['elements']:5d} {r['k']:8.3f} {r['dy']:7.2f} {r['y']:>10s}")
    print(f"\n{len(resultats)} props convertis · {len(set(RAPPORT['textures_copiees']))} textures copiées")
    if RAPPORT["textures_absentes"]:
        print("à examiner :")
        for x in RAPPORT["textures_absentes"]:
            print("  -", x)

    print("\n--- lignes à ajouter dans ModBlocks.java ---")
    for r in resultats:
        const = r["ident"].upper()
        print(f'    public static final Block {const} = block("{r["ident"]}", SoundType.METAL);')
        print(f'    public static final Item {const}_ITEM = blockItem("{r["ident"]}", {const});')
    print("\n--- entrées de langue ---")
    for r in resultats:
        print(f'  fr_fr  "block.voiddynasty.{r["ident"]}": "{r["fr"]}"')
        print(f'  en_us  "block.voiddynasty.{r["ident"]}": "{r["en"]}"')
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
