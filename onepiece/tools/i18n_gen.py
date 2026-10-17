#!/usr/bin/env python3
"""CORE-028 — génération et vérification des traductions du périmètre Fruits.

Deux usages :

    python3 tools/i18n_gen.py --generate     # écrit en_us.json (complété) + fr_fr.json (créé)
    python3 tools/i18n_gen.py --check        # vérifie la couverture, sans rien écrire

Règles de traduction retenues (documentées dans docs/CORE-028-langue-et-hud.md) :
  * nom de technique descriptif en anglais  -> traduit en français (« Fire Fist » → « Poing de Feu ») ;
  * nom de technique romaji / nom propre    -> conservé tel quel (« Hiken », « Raigo », « Soru »),
    comme le fait la localisation officielle française du manga ;
  * noms de Fruits                          -> nomenclature officielle FR (« Fruit Flamme-Flamme ») ;
  * vocabulaire de la ressource d'énergie   -> « Énergie », terme déjà présent dans le lang (ver15),
    ce qui supprime le « Curse Power » codé en dur.
"""
import argparse
import json
import os
import re
import sys

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(BASE, "src/main/resources/assets/minepiece/lang")
EN = os.path.join(LANG, "en_us.json")
FR = os.path.join(LANG, "fr_fr.json")
REGISTRY = os.path.join(BASE, "src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java")

TECHNIQUE_PREFIX = "minepiece.technique."

# ---------------------------------------------------------------- noms de techniques → français
# Table explicite, rédigée (pas de traduction mot à mot : l'ordre des mots français diffère).
# Tout id ABSENT de cette table conserve son nom d'origine — c'est le cas des noms propres / romaji
# (Hiken, Raigo, Soru, Rankyaku…), que la localisation officielle française conserve également.
TECHNIQUES_FR = {
    "arrow": "Flèche Aro",
    "assault": "Assaut Mori Mori",
    "atomicspurt": "Jet Atomique",
    "attackjump": "Saut d'Attaque",
    "attackoverhead": "Attaque Verticale",
    "attackspeed": "Vitesse d'Attaque",
    "attacksweep": "Attaque Balayée",
    "blackcofee": "Café Noir",
    "blackhole": "Trou Noir",
    "bluebird": "Oiseau Bleu",
    "bomboverhead": "Bombe Verticale",
    "borobreath": "Souffle Boro",
    "brachiolauncher": "Lanceur Brachio",
    "brickbat": "Chauve-souris Brique",
    "brilliantpunk": "Punk Éclatant",
    "combo": "Combo Aro",
    "crashpretzel": "Bretzel Écrasant",
    "daienkai": "Grand Commandement de Flamme",
    "darkball": "Boule Obscure",
    "darkmatter": "Matière Obscure",
    "fire": "Feu",
    "gear_change": "Changement de Gear",
    "grounddeath": "Mort Terrestre",
    "hadoelbow": "Coude Hado",
    "headbutt": "Coup de Tête",
    "hebunrifire": "Feu Hebunri",
    "hibashira": "Pilier de Feu",
    "higan": "Pistolet de Feu",
    "hiken": "Poing de Feu",
    "hotarubi": "Luciole",
    "iceage": "Ère Glaciaire",
    "iceball": "Boule de Glace",
    "icepartisan": "Bloc de Glace : Partisan",
    "icespikes": "Pics de Glace",
    "impactball": "Boule d'Impact",
    "impactwave": "Onde d'Impact",
    "imuarrow": "Flèche d'Imu",
    "islandshatterpunch": "Poing Brise-Île",
    "kaenryuo": "Roi Dragon de Flamme",
    "kagero": "Mirages de Chaleur",
    "kick": "Coup de Pied Vitesse Lumière",
    "kickbomb": "Coup de Pied Bombe",
    "kirooverhead": "Kiro Verticale",
    "kumapunch": "Poing de Kuma",
    "laser2": "Barrage Laser",
    "localareanetwork": "Réseau Local",
    "lupusfall": "Rankyaku : Chute du Loup",
    "magatama": "Yasakani no Magatama",
    "mamaraid": "Raid de Mama",
    "marcotornado": "Tornade de Marco",
    "meteorsmall": "Météore (petit)",
    "minihollow": "Hollow Mini",
    "mochigatling": "Gatling Mochi",
    "mochipistol": "Pistolet Mochi",
    "move": "Déplacement Aro",
    "musodonatsu": "Muso Donatsu",
    "negativehollow": "Hollow Négatif",
    "nosefancycannon": "Canon Nasal",
    "noteattack": "Attaque Note",
    "notelaser": "Laser Note",
    "noteshield": "Bouclier Note",
    "notewarrior": "Guerrier Note",
    "overheadsuper": "Attaque Verticale Suprême",
    "perfumefemur": "Fémur Parfumé Magna",
    "pheasantbeak": "Bloc de Glace : Bec du Faisan",
    "pistolkiss": "Baiser Pistolet",
    "posing": "Pose",
    "projectileslash": "Entaille Projectile",
    "punchbomb": "Poing Bombe",
    "root": "Racine Mori Mori",
    "rootgiant": "Racine Géante",
    "rootsurround": "Racines Enveloppantes",
    "rotatekick": "Coup de Pied Tournant",
    "roundhousekick": "Coup de Pied Circulaire Brique",
    "rubble": "Décombres",
    "shadowasgard": "Asgard de l'Ombre",
    "sparbreak": "Brise-Spar",
    "sparkingqueen": "Reine Étincelante",
    "sparklingdaisy": "Marguerite Scintillante",
    "speedispower": "La Vitesse est la Puissance",
    "stoneopeope": "Pierre Opé-Opé",
    "stonethrowing": "Lancer de Pierre",
    "stonethrowinglarge": "Lancer de Pierre (gros)",
    "summontotmusica": "Invocation Tot Musica",
    "totmusicapunch": "Poing Tot Musica",
    "ulmeteor": "Ul Météore",
    "ursusshock": "Choc Ursus",
    "volcanicexplosion": "Explosion Volcanique",
    "weightmanipulation": "Manipulation du Poids",
    "whiteblow": "Souffle Blanc",
    "whitelauncher": "Lanceur Blanc",
    "whiteout": "Voile Blanc",
    "whitevine": "Liane Blanche",
}

# ---------------------------------------------------------------- noms de Fruits (nomenclature FR)
FRUITS_FR = {
    "item_devil_fruit": "Fruit du Vide",
    "item_devil_fruit_aro_aro": "Fruit Flèche-Flèche",
    "item_devil_fruit_batto_batto": "Fruit Chauve-souris-Chauve-souris",
    "item_devil_fruit_bisu_bisu": "Fruit Biscuit-Biscuit",
    "item_devil_fruit_bomu_bomu": "Fruit Bombe-Bombe",
    "item_devil_fruit_gomu_gomu": "Fruit Gum-Gum",
    "item_devil_fruit_goro_goro": "Fruit Foudre-Foudre",
    "item_devil_fruit_gura_gura": "Fruit Séisme-Séisme",
    "item_devil_fruit_hie_hie": "Fruit Glace-Glace",
    "item_devil_fruit_hito_hito_daibutsu": "Fruit Humain-Humain",
    "item_devil_fruit_horo_horo": "Fruit Fantôme-Fantôme",
    "item_devil_fruit_imu": "Fruit ????",
    "item_devil_fruit_inu_inu_okuchi_no_makami": "Fruit Chien-Chien",
    "item_devil_fruit_inu_inu_wolf": "Fruit Chien-Chien",
    "item_devil_fruit_kage_kage": "Fruit Ombre-Ombre",
    "item_devil_fruit_kira_kira": "Fruit Étincelle-Étincelle",
    "item_devil_fruit_kiro_kiro": "Fruit Kilo-Kilo",
    "item_devil_fruit_magu_magu": "Fruit Magma-Magma",
    "item_devil_fruit_mera_mera": "Fruit Flamme-Flamme",
    "item_devil_fruit_mero_mero": "Fruit Amour-Amour",
    "item_devil_fruit_mochi_mochi": "Fruit Mochi-Mochi",
    "item_devil_fruit_moku_moku": "Fruit Fumée-Fumée",
    "item_devil_fruit_mori_mori": "Fruit Forêt-Forêt",
    "item_devil_fruit_neko_neko_leopard": "Fruit Félin-Félin",
    "item_devil_fruit_neko_neko_sabertiger": "Fruit Félin-Félin",
    "item_devil_fruit_nikyu_nikyu": "Fruit Patte-Patte",
    "item_devil_fruit_ope_ope": "Fruit Opé-Opé",
    "item_devil_fruit_pika_pika": "Fruit Lumière-Lumière",
    "item_devil_fruit_riki_riki": "Fruit Force-Force",
    "item_devil_fruit_ryu_ryu_brachiosaurus": "Fruit Dragon-Dragon",
    "item_devil_fruit_ryu_ryu_pachycephalosaurus": "Fruit Dragon-Dragon",
    "item_devil_fruit_ryu_ryu_pteranodon": "Fruit Dragon-Dragon",
    "item_devil_fruit_soru_soru": "Fruit Âme-Âme",
    "item_devil_fruit_suna_suna": "Fruit Sable-Sable",
    "item_devil_fruit_supa_supa": "Fruit Découpe-Découpe",
    "item_devil_fruit_tori_tori_phoenix": "Fruit Oiseau-Oiseau",
    "item_devil_fruit_uo_uo_seiryu": "Fruit Poisson-Poisson",
    "item_devil_fruit_ushi_ushi_giraffe": "Fruit Bœuf-Bœuf",
    "item_devil_fruit_uta_uta": "Fruit Chant-Chant",
    "item_devil_fruit_yami_yami": "Fruit Ténèbres-Ténèbres",
    "item_devil_fruit_zushi_zushi": "Fruit Pression-Pression",
}

# Modèles / descriptions de Fruits (FR)
FRUITS_INFO_FR = {
    "item.minepiece.item_devil_fruit.description_0": "Manger ce Fruit réinitialise les pouvoirs du Fruit du Démon.",
    "item.minepiece.item_devil_fruit_hito_hito_daibutsu.info": "Modèle : Bouddha",
    "item.minepiece.item_devil_fruit_inu_inu_okuchi_no_makami.info": "Modèle : Okuchi-no-Makami",
    "item.minepiece.item_devil_fruit_inu_inu_wolf.info": "Modèle : Loup",
    "item.minepiece.item_devil_fruit_neko_neko_leopard.info": "Modèle : Léopard",
    "item.minepiece.item_devil_fruit_neko_neko_sabertiger.description_0": "Modèle : Tigre à dents de sabre",
    "item.minepiece.item_devil_fruit_ryu_ryu_brachiosaurus.description_0": "Modèle : Brachiosaure",
    "item.minepiece.item_devil_fruit_ryu_ryu_pachycephalosaurus.info": "Modèle : Pachycéphalosaure",
    "item.minepiece.item_devil_fruit_ryu_ryu_pteranodon.description_0": "Modèle : Ptéranodon",
    "item.minepiece.item_devil_fruit_tori_tori_phoenix.info": "Modèle : Phénix",
    "item.minepiece.item_devil_fruit_uo_uo_seiryu.description_0": "Modèle : Dragon Azur",
    "item.minepiece.item_devil_fruit_ushi_ushi_giraffe.info": "Modèle : Girafe",
}

# ---------------------------------------------------------------- HUD + messages (nouvelles clés)
# clé -> (anglais, français)
NEW_KEYS = {
    "minepiece.hud.title": ("Devil Fruit", "Fruit du Démon"),
    "minepiece.hud.technique": ("Technique", "Technique"),
    "minepiece.hud.form": ("Form", "Forme"),
    "minepiece.hud.form.base": ("Base", "Base"),
    "minepiece.hud.form.hybrid": ("Hybrid", "Hybride"),
    "minepiece.hud.none": ("None", "Aucun"),
    "minepiece.hud.hint": ("R: use · V: transform · B: change", "R : utiliser · V : transformer · B : changer"),

    "minepiece.message.no_fruit": ("You have not eaten a Devil Fruit.", "Tu n'as pas mangé de Fruit du Démon."),
    "minepiece.message.not_ported": ("Fruit registered, but its techniques are not ported yet.",
                                     "Fruit enregistré, mais ses techniques ne sont pas encore portées."),
    "minepiece.message.sea_suppressed": ("Your powers are sealed by the sea!",
                                         "Tes pouvoirs sont scellés par la mer !"),
    "minepiece.message.cooldown": ("Technique on cooldown.", "Technique en recharge."),
    "minepiece.message.no_energy": ("Not enough Energy (%s required, %s available).",
                                    "Pas assez d'Énergie (%s requis, %s disponible)."),
    "minepiece.message.technique_selected": ("Technique: %s", "Technique : %s"),
    "minepiece.message.technique_cast": ("%s!", "%s !"),
    "minepiece.message.no_hybrid": ("This fruit has no hybrid form.",
                                    "Ce fruit n'a pas de forme hybride."),
    "minepiece.message.transformed": ("Transformation! (hybrid form)",
                                      "Transformation ! (forme hybride)"),
    "minepiece.message.reverted": ("Back to base form.", "Retour à la forme de base."),
    "minepiece.message.unknown_fruit": ("Unknown fruit: %s", "Fruit inconnu : %s"),
    "minepiece.message.not_a_fruit": ("Not a Devil Fruit: %s", "Ce n'est pas un Fruit du Démon : %s"),
    "minepiece.message.fruit_given": ("Fruit given: %s", "Fruit donné : %s"),
    "minepiece.message.spawn_failed": ("Summon spawn failed.", "Échec de l'invocation."),
    "minepiece.message.visual_failed": ("Visual spawn failed.", "Échec du visuel."),
    "minepiece.message.temp_block": ("Temporary block placed at %s (%s ticks) — active: %s",
                                     "Bloc temporaire posé à %s (%s ticks) — actifs : %s"),
    "minepiece.message.haki_disabled": ("Haki is disabled by the configuration.",
                                        "Le Haki est désactivé par la configuration."),
    "minepiece.message.haki_type_expected": ("Expected type: armament or observation.",
                                             "Type attendu : armement ou observation."),
    "minepiece.message.haki_status": ("Haki — %s", "Haki — %s"),
    "minepiece.message.haki_set": ("Haki set — %s", "Haki réglé — %s"),
    "minepiece.message.haki_gained": ("EXP gained. %s", "EXP gagnée. %s"),
    "minepiece.message.haki_levelup": ("Level up! %s", "Niveau supérieur ! %s"),
    "minepiece.message.haki_armament_level": ("Armament Haki — level %s!",
                                              "Haki de l'Armement — niveau %s !"),
    "minepiece.message.haki_observation_level": ("Observation Haki — level %s!",
                                                 "Haki de l'Observation — niveau %s !"),
    "minepiece.message.sovereign": ("Sovereign Haki! %s entities down within %s blocks.",
                                    "Haki Souverain ! %s entité(s) hors de combat dans un rayon de %s blocs."),
    "minepiece.message.haki_attributes_note": (
        "(note: Haki attributes are not attached to the player — levels persist in player variables)",
        "(note : attributs de Haki non attachés au joueur — les niveaux persistent via les variables joueur)"),
    "minepiece.message.power_status": ("Fruit: %s  Energy: %s/%s",
                                       "Fruit : %s  Énergie : %s/%s"),
    "minepiece.message.power_family": ("Family: %s (transformable: %s)",
                                       "Famille : %s (transformable : %s)"),
    "minepiece.message.power_removed": ("Power removed.", "Pouvoir retiré."),
    "minepiece.message.summon_limit": ("Summon limit reached (%s).",
                                       "Limite d'invocations atteinte (%s)."),
    "minepiece.message.power_reset": ("The Devil Fruit power has been removed.",
                                      "Le pouvoir du Fruit du Démon a été retiré."),
    "minepiece.message.fruit_eaten": ("You ate a Devil Fruit!", "Tu as mangé un Fruit du Démon !"),
    "minepiece.message.two_fruits": ("Eating two Devil Fruits destroys you!",
                                     "Manger deux Fruits du Démon te détruit !"),
    "minepiece.message.reset_report": ("Reset: form=%s, fruit=%s, technique=%s, energy=%s, summons=%s",
                                       "Reset : forme=%s, fruit=%s, technique=%s, énergie=%s, invocations=%s"),
    "minepiece.message.flight_needs_form": ("Flight requires the hybrid form — you land.",
                                            "Le vol exige la forme hybride — tu atterris."),
    "minepiece.message.flight_suppressed": ("Sealed by the sea — you land.",
                                            "Scellé par la mer — tu atterris."),
    "minepiece.message.flight_no_energy": ("Not enough Energy to keep flying — you land.",
                                           "Pas assez d'Énergie pour rester en vol — tu atterris."),
    "minepiece.message.order_applied": ("%s summon(s) → order \"%s\".",
                                        "%s invocation(s) → ordre « %s »."),
    "minepiece.message.balance_forbidden": ("Base form does not use techniques.",
                                            "La forme de base n'utilise pas de techniques."),
}


def technique_ids():
    """Identifiants de techniques + nom anglais, extraits du registre.

    Toutes les formes d'appel commencent par (id, nom, …) — constructeur complet comme fabriques.
    """
    src = open(REGISTRY, encoding="utf-8").read()
    pairs = {}
    for m in re.finditer(r'"([a-z0-9_]+)"\s*,\s*"([^"]+)"\s*,', src):
        pairs.setdefault(m.group(1), m.group(2))
    return pairs


def translate(name):
    """Nom anglais → français.

    Traduit uniquement si l'id figure dans la table explicite ; sinon le nom est un nom propre /
    romaji et il est conservé tel quel, comme dans la localisation officielle française.
    """
    return name


def build():
    en = json.load(open(EN, encoding="utf-8"))
    tech = technique_ids()

    # 1. clés de techniques manquantes dans en_us (nom anglais du registre)
    added_tech = 0
    for tid, en_name in tech.items():
        key = TECHNIQUE_PREFIX + tid
        if key not in en:
            en[key] = en_name
            added_tech += 1

    # 2. clés HUD / messages
    for key, (eng, _fr) in NEW_KEYS.items():
        en.setdefault(key, eng)

    # 3. fr_fr = copie de en_us + surcharges françaises
    fr = dict(en)
    for tid, en_name in tech.items():
        fr[TECHNIQUE_PREFIX + tid] = TECHNIQUES_FR.get(tid, en_name)
    for fid, fr_name in FRUITS_FR.items():
        fr[f"item.minepiece.{fid}"] = fr_name
    fr.update(FRUITS_INFO_FR)
    for key, (_eng, frv) in NEW_KEYS.items():
        fr[key] = frv

    # vocabulaire : plus aucun « Curse Power » côté joueur
    fr = {k: (v.replace("Curse Power", "Énergie") if isinstance(v, str) else v) for k, v in fr.items()}
    en = {k: (v.replace("Curse Power", "Energy") if isinstance(v, str) else v) for k, v in en.items()}

    return en, fr, added_tech, tech


HARDCODED_SCAN = [
    "src/main/java/net/mcreator/minepiece/procedures/DevilFruitSkillDispatcher.java",
    "src/main/java/net/mcreator/minepiece/procedures/DevilFruitTransformation.java",
    "src/main/java/net/mcreator/minepiece/command/DevilFruitCommands.java",
    "src/main/java/net/mcreator/minepiece/command/HakiCommands.java",
    "src/main/java/br/com/oneminepiece/opworldlike/haki/HakiProgression.java",
]

# Un littéral « joueur » contient du texte : on exclut ceux qui ne portent qu'un code couleur
# (ex. Component.literal("§c") suivi d'un .append(translatable)).
LITERAL_RE = re.compile(r'Component\.literal\("§[0-9a-flmnor]*[A-Za-zÀ-ÿ]')
COLOR_ONLY_RE = re.compile(r'Component\.literal\("§[0-9a-fk-orA-FK-OR]*"\)')


def hardcoded_literals():
    """Littéraux visibles joueur encore codés en dur dans les fichiers du périmètre Fruits."""
    hits = []
    for rel in HARDCODED_SCAN:
        path = os.path.join(BASE, rel)
        if not os.path.exists(path):
            continue
        for i, line in enumerate(open(path, encoding="utf-8").read().splitlines(), 1):
            if (LITERAL_RE.search(line) and "minepiece.message" not in line
                    and not COLOR_ONLY_RE.search(line)):
                hits.append(f"{os.path.basename(rel)}:{i} {line.strip()[:90]}")
    return hits


def check():
    en, fr, _added, tech = build()
    en_disk = json.load(open(EN, encoding="utf-8"))
    ok = True
    missing_en = [t for t in tech if TECHNIQUE_PREFIX + t not in en_disk]
    missing_fr = [t for t in tech if TECHNIQUE_PREFIX + t not in fr]
    print(f"techniques déclarées         : {len(tech)}")
    print(f"clés absentes de en_us.json  : {len(missing_en)}")
    print(f"clés absentes du fr_fr généré: {len(missing_fr)}")
    print(f"clés HUD/messages attendues  : {len(NEW_KEYS)}")
    if os.path.exists(FR):
        fr_disk = json.load(open(FR, encoding="utf-8"))
        print(f"fr_fr.json sur disque        : {len(fr_disk)} clés")
        print(f"fr_fr.json à jour            : {fr_disk == fr}")
        if fr_disk != fr:
            ok = False
    else:
        print("fr_fr.json sur disque        : ABSENT")
        ok = False
    if missing_en or missing_fr:
        ok = False

    hits = hardcoded_literals()
    print(f"littéraux joueur codés en dur: {len(hits)}")
    for h in hits[:10]:
        print("   ", h)
    if hits:
        ok = False

    print("RÉSULTAT :", "à jour" if ok else "à corriger (--generate puis câblage des littéraux)")
    return 0 if ok else 1


def generate():
    en, fr, added, tech = build()
    json.dump(en, open(EN, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    json.dump(fr, open(FR, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print(f"en_us.json : {len(en)} clés (+{added} clés de technique)")
    print(f"fr_fr.json : {len(fr)} clés")
    fr_tech = [k for k in fr if k.startswith(TECHNIQUE_PREFIX)]
    translated = [k for k in fr_tech if fr[k] != en.get(k)]
    print(f"techniques : {len(fr_tech)} clés — {len(translated)} traduites, "
          f"{len(fr_tech) - len(translated)} conservées (noms propres romaji)")
    return 0


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--generate", action="store_true")
    ap.add_argument("--check", action="store_true")
    a = ap.parse_args()
    sys.exit(generate() if a.generate else check())
