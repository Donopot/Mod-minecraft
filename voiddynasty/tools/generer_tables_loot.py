#!/usr/bin/env python3
"""Phase E — tables de loot, transcrites des datapacks de la source.

La source est un datapack : elle ne peut pas créer d'objets. Elle simule donc ses munitions avec des
objets vanilla renommés (`iron_nugget` → « Bolt Ammo », `coal` → « Battery Pack », …) et un
`custom_model_data`. Notre mod, lui, EST un mod : les vraies munitions existent déjà. On transcrit donc
chaque entrée vers l'objet qui la représente réellement, sans changer ni les poids ni les quantités.

Entrées de la source → objet réel :
    iron_nugget « Bolt Ammo »            → voiddynasty:bolt_shell
    gold_nugget « Plasma Ammo »          → voiddynasty:plasma_cell
    gold_nugget « Kerosene »             → voiddynasty:melta_cell
    coal        « Battery Pack »         → voiddynasty:las_cell
    carrot_on_a_stick « Hunter-Killer »  → voiddynasty:missile_ammo
    bread       « Corpse Starch »        → minecraft:bread      (c'est DÉJÀ du pain dans la source)
    golden_apple (sans nom)              → minecraft:golden_apple

Deux mobs de la source — `cultistboss` et `forgefiend` — pointent vers une table qui n'existe pas
(`DeathLootTable:"cultistboss"`, sans namespace). C'est une référence morte dans la source : ils ne
laissent rien. On ne leur invente pas de butin ; l'écart est signalé.
"""
import json
import os
import re
import sys

SOURCE = ("/opt/data/workspace/astartes-extract/Astartes Warhammer Game World/datapacks/"
          "AstartesEnemies/data/mj.whenemies")
DEST = "/opt/data/workspace/um-test-mod/src/main/resources/data/voiddynasty/loot_table"

# (item source, custom_model_data) → objet réel. Le modèle sert à distinguer deux usages d'un même
# objet vanilla : `gold_nugget` porte AUSSI BIEN du plasma que du kérosène.
CORRESPONDANCE = {
    ("iron_nugget", 211111): "voiddynasty:bolt_shell",        # « Bolt Ammo »
    ("gold_nugget", 211112): "voiddynasty:plasma_cell",       # « Plasma Ammo »
    ("gold_nugget", 211116): "voiddynasty:melta_cell",        # « Kerosene »
    ("coal", 211113): "voiddynasty:las_cell",                 # « Battery Pack »
    ("carrot_on_a_stick", 111131): "voiddynasty:missile_ammo",  # « Hunter-Killer Missile »
    ("bread", 511111): "minecraft:bread",                     # « Corpse Starch »
    ("golden_apple", None): "minecraft:golden_apple",
}

# mob → table, relevé dans les DeathLootTable des <mob>summon.mcfunction
#
# DEUX CAS À PART, décidés par Donopot le 16/10/2025 (la source ne fournit rien pour eux) :
# `cultistboss` et `forgefiend` portent `DeathLootTable:"cultistboss"` et `"forgefiend"` — des chemins
# SANS namespace, donc des références mortes : ces deux boss ne laissent rien dans la source. Le
# contenu ci-dessous est donc une DÉCISION, pas un portage. On n'invente aucun objet ni aucune
# quantité : on réutilise les tables de la source en suivant sa propre échelle de paliers.
#   · cultistboss : premier boss de la campagne, un cran au-dessus des cultistes et un cran sous les
#     marines → PALIER 4 (le palier 3 est celui des marines et des servitors).
#   · forgefiend  : boss de la dernière partie, aux côtés du capitaine, du dreadnought, du hellbrute
#     et du prince démon, tous au PALIER 5 → PALIER 5.
# Le palier 6 reste non attribué, exactement comme dans la source.
DECISIONS_HORS_SOURCE = {
    "cultist_boss": "ammo_tier_4",
    "chaos_forgefiend": "ammo_tier_5",
}

AFFECTATION = {
    "cultist_1": "cultist_death", "cultist_2": "cultist_death",
    "cultist_3": "cultist_death", "cultist_4": "cultist_death",
    "chaos_marine_1": "ammo_tier_3", "chaos_marine_2": "ammo_tier_3", "chaos_marine_3": "ammo_tier_3",
    "chaos_raptor": "ammo_tier_3",
    "servitor_1": "ammo_tier_3", "servitor_2": "ammo_tier_3", "servitor_3": "ammo_tier_3",
    "chaos_captain": "ammo_tier_5", "chaos_dreadnought": "ammo_tier_5",
    "chaos_hellbrute": "ammo_tier_5", "daemon_prince": "ammo_tier_5",
}


def lire_table_source(nom: str) -> dict:
    return json.load(open(f"{SOURCE}/loot_table/{nom}.json", encoding="utf-8"))


def transcrire(nom_source: str) -> tuple[dict, list[str]]:
    """Rend la table au format de notre mod, plus la trace des correspondances utilisées."""
    src = lire_table_source(nom_source)
    trace: list[str] = []
    pools = []
    for pool in src.get("pools", []):
        entrees = []
        for e in pool.get("entries", []):
            if e.get("type") != "item":
                raise SystemExit(f"entrée non gérée dans {nom_source} : {e.get('type')}")
            cmd = None
            for f in e.get("functions", []):
                if "custom_model_data" in f.get("function", ""):
                    cmd = f.get("value")
            cle = (e["name"].replace("minecraft:", ""), cmd)
            if cle not in CORRESPONDANCE:
                raise SystemExit(f"correspondance absente pour {cle} dans {nom_source}")
            cible = CORRESPONDANCE[cle]
            trace.append(f"{e['name']}+{cmd} → {cible}")
            entrees.append({"type": "minecraft:item", "name": cible, "weight": e.get("weight", 1)})
        # Quantité : la source la pose sur le POOL (`functions`), le format 26.3 la pose sur CHAQUE
        # entrée sous le nom `modifier`. Vérifié sur les tables vanilla : 0 sur 400 utilise encore
        # `functions`. Recopier la forme de la source aurait produit des tables ignorées en silence.
        for f in pool.get("functions", []):
            if f.get("function") == "set_count":
                c = f["count"]
                modificateur = ({"type": "minecraft:set_count", "count": {
                    "type": "minecraft:uniform", "min": c.get("min"), "max": c.get("max")}}
                    if isinstance(c, dict)
                    else {"type": "minecraft:set_count", "count": c})
                for entree in entrees:
                    entree["modifier"] = modificateur
        pools.append({"rolls": pool.get("rolls", 1), "entries": entrees})
    return {"type": "minecraft:entity", "pools": pools}, trace


def main() -> int:
    ecrits, traces = [], {}
    # 1) les 7 tables de la source
    for nom in ["cultist_death"] + [f"ammo_tier_{i}" for i in range(1, 7)]:
        chemin_source = "death/cultist_death" if nom == "cultist_death" else f"ammo_crate_loot/{nom}"
        table, trace = transcrire(chemin_source)
        dossier = "objects" if nom == "cultist_death" else "ammo_crate"
        os.makedirs(f"{DEST}/{dossier}", exist_ok=True)
        with open(f"{DEST}/{dossier}/{nom}.json", "w", encoding="utf-8") as f:
            json.dump(table, f, indent=2, ensure_ascii=False)
            f.write("\n")
        ecrits.append(f"{dossier}/{nom}.json")
        traces[nom] = sorted(set(trace))

    # 2) une table par mob, par convention de nom (data/<ns>/loot_table/entities/<id>.json)
    os.makedirs(f"{DEST}/entities", exist_ok=True)
    for mob, nom_table in {**AFFECTATION, **DECISIONS_HORS_SOURCE}.items():
        dossier = "objects" if nom_table == "cultist_death" else "ammo_crate"
        contenu = json.load(open(f"{DEST}/{dossier}/{nom_table}.json", encoding="utf-8"))
        with open(f"{DEST}/entities/{mob}.json", "w", encoding="utf-8") as f:
            json.dump(contenu, f, indent=2, ensure_ascii=False)
            f.write("\n")
        ecrits.append(f"entities/{mob}.json")

    print(f"{len(ecrits)} tables écrites dans {DEST}")
    for nom, t in sorted(traces.items()):
        print(f"  {nom}: " + " · ".join(t))
    print("\ndécisions hors source (références mortes côté source) : "
          + ", ".join(f"{m} → {t}" for m, t in DECISIONS_HORS_SOURCE.items()))
    print(f"total : {len(ecrits)} fichiers")
    return 0


if __name__ == "__main__":
    sys.exit(main())
