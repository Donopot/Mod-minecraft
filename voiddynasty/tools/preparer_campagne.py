#!/usr/bin/env python3
"""Relève la campagne de la source et l'écrit en données exploitables par le mod.

La source pilote sa campagne avec des commandes et des coordonnées en dur. On transcrit ce qu'elle fait
en données, pour que de vraies classes Java l'exécutent — comme le recommandait le rapport de mécaniques
lui-même (« à reprendre comme système de niveau, pas des commandes fill/tp hardcodées »).

Ce qui est lu, et où :
  règles      leveldetect.mcfunction   position + condition + compteur → actions
  zones       levels/*.mcfunction      répliques, waypoints, créatures, objets donnés, appels différés,
                                       compteurs posés, niveau posé
  portes      doors/dooropen.mcfunction  tag, condition, régions à ouvrir, son, fonction appelée
  panneaux    doors/dooropen.mcfunction  les 4 panneaux du réacteur et leur effet sur le compte du cœur
  réapparition death.mcfunction        index de niveau → coordonnées de réapparition
  portes      load.mcfunction          régions de verre rouge au chargement

Rien n'est inventé. Les noms de créatures de la source sont traduits vers NOS identifiants, et une
correspondance manquante fait ÉCHOUER l'outil au lieu de passer inaperçue. Les règles qui testent
`type=zombie` sont relevées TELLES QUELLES : la source testait le zombie parce que ses ennemis en
étaient ; le moteur du mod traduira ça en « aucun ennemi de la campagne », et le dit.
"""
import json
import os
import re
import sys

SOURCE = ("/opt/data/workspace/astartes-extract/Astartes Warhammer Game World/datapacks/GameEngine/"
          "data/mj.gameengine/function")
RELEVE = "/opt/data/workspace/um-test-mod/docs/campagne-source.json"
DEST = "/opt/data/workspace/um-test-mod/src/main/resources/data/voiddynasty/campagne/campagne.json"

# Noms de créatures de la source → nos identifiants d'entités.
CORRESPONDANCE = {
    "cultist1": "cultist_1", "cultist2": "cultist_2", "cultist3": "cultist_3", "cultist4": "cultist_4",
    "cultistboss": "cultist_boss",
    "marine1": "chaos_marine_1", "marine2": "chaos_marine_2", "marine3": "chaos_marine_3",
    "marinecaptain": "chaos_captain", "raptor": "chaos_raptor",
    "servitor1": "servitor_1", "servitor2": "servitor_2", "servitor3": "servitor_3",
    "chaosdreadnought": "chaos_dreadnought", "hellbrute": "chaos_hellbrute",
    "forgefiend": "chaos_forgefiend", "demonprince": "daemon_prince", "helldrake": "chaos_helldrake",
    "servoskull": "servo_skull",
}

# Noms d'objets de la source → NOS objets. La source donnait un bâton de carotte au modèle personnalisé ;
# notre port possède les vraies armes, donc on donne les NÔTRES. « Plasma Rifle » → plasma_incinerator est
# une DÉDUCTION : la source a deux armes à plasma, nous aussi (le pistolet et l'incinérateur).
OBJETS = {
    "Bolt Ammo": "bolt_shell", "Bolt Gun": "boltgun", "Bolt Pistol": "bolt_pistol",
    "ChainSword": "chainsword", "Flame Thrower": "flamethrower",
    "Hunter-Killer Missile": "hunter_killer_missile", "Laz Gun": "las_gun", "Laz Pistol": "laz_pistol",
    "Plasma Ammo": "plasma_cell", "Plasma Pistol": "plasma_pistol", "Plasma Rifle": "plasma_incinerator",
    "Power Hammer": "power_hammer", "Power Sword": "powersword", "ShotGun": "shotgun",
    "Stalker Bolt Rifle": "stalker_bolt_rifle",
}

# Pièces d'armure de la source (diamant) → notre première armure. La source n'a QU'UNE tenue : c'est le
# premier modèle du port qui y correspond.
ARMURE = {"feet": "space_marine_boots", "legs": "space_marine_leggings",
          "chest": "space_marine_chestplate", "head": "space_marine_helmet"}

ID_ZONE = re.compile(r"levels/([a-z0-9_/]+)")
POSITION = re.compile(r"positioned (-?[\d.]+) (-?[\d.]+) (-?[\d.]+)")
JOUEUR_PROCHE = re.compile(r"if entity @e\[type=player,distance=0\.\.([\d.]+)\]")
SANS_MOB = re.compile(r"unless entity @e\[type=([a-z_]+),distance=0\.\.([\d.]+)\]")
COMPTEUR = re.compile(r"if score \.([a-z0-9_]+) active matches (\d+)")
CLE_ZONE = lambda p: p.replace("/", "_")


def lire(chemin):
    return open(f"{SOURCE}/{chemin}", encoding="utf-8", errors="ignore").read()


# ----------------------------------------------------------------------------- règles de progression
def lire_regles():
    """Chaque ligne de leveldetect est une règle. Les lignes qui partagent position, rayon, condition et
    compteur sont réunies : c'est UNE règle, avec plusieurs actions."""
    regles = {}
    for ligne in lire("leveldetect.mcfunction").splitlines():
        ligne = ligne.strip()
        if not ligne.startswith("execute positioned"):
            continue
        pos = POSITION.search(ligne)
        action = re.search(r" run (.+)$", ligne)
        if not pos or not action:
            continue
        condition = {"type": "aucune"}
        joueur = JOUEUR_PROCHE.search(ligne)
        if joueur:
            condition = {"type": "joueur_proche", "rayon": float(joueur.group(1))}
        sans = SANS_MOB.search(ligne)
        if sans:
            condition = {"type": "sans_ennemi", "type_source": sans.group(1),
                         "rayon": float(sans.group(2))}
        compteur = COMPTEUR.search(ligne)
        cle = (round(float(pos.group(1)), 1), round(float(pos.group(2)), 1),
               round(float(pos.group(3)), 1), json.dumps(condition, sort_keys=True),
               compteur.group(1) if compteur else None, int(compteur.group(2)) if compteur else None)
        if cle not in regles:
            regles[cle] = {"position": [float(pos.group(1)), float(pos.group(2)), float(pos.group(3))],
                           "condition": condition,
                           "compteur_zone": compteur.group(1) if compteur else None,
                           "compteur_valeur": int(compteur.group(2)) if compteur else None,
                           "appelle": [], "pose_compteur": [], "pose_niveau": [], "tue": []}
        regle = regles[cle]
        a = action.group(1)
        appel = ID_ZONE.search(a)
        if appel:
            regle["appelle"].append(appel.group(1))
            continue
        pose = re.search(r"scoreboard players set \.([a-z0-9_]+) active (\d+)", a)
        if pose:
            regle["pose_compteur"].append([pose.group(1), int(pose.group(2))])
            continue
        niv = re.search(r"scoreboard players set \.level level (\d+)", a)
        if niv:
            regle["pose_niveau"].append(int(niv.group(1)))
            continue
        tue = re.search(r"kill @e\[tag=([a-z0-9_]+)\]", a)
        if tue:
            regle["tue"].append(tue.group(1))
    return sorted(regles.values(), key=lambda r: (r["position"][2], r["position"][0]))


# ----------------------------------------------------------------------------- portes et réacteur
def lire_portes():
    """Une porte : un tag, une condition (compteur d'une zone), et des actions. La source la frappe ;
    le mod reproduit ce comportement."""
    portes = {}
    for ligne in lire("doors/dooropen.mcfunction").splitlines():
        ligne = ligne.strip()
        if not ligne.startswith("execute as @"):
            continue
        cible = re.search(r"execute as (@s\[tag=([a-z0-9_]+)\]|@e\[tag=([a-z0-9_]+)\])", ligne)
        if not cible:
            continue
        tag = cible.group(2) or cible.group(3)
        action = re.search(r" run (.+)$", ligne)
        if not action:
            continue
        a = action.group(1)
        if tag not in portes:
            portes[tag] = {"tag": tag, "condition_zone": None, "condition_valeur": None,
                           "regions": [], "sons": [], "appelle": [], "tue": [], "compte_retire": None}
        porte = portes[tag]
        compte = COMPTEUR.search(ligne)
        if compte:
            # `matches 4` = la porte s'ouvre à partir de 4 ; `matches 1..3` = elle se réarme
            borne = re.match(r"(\d+)", compte.group(2))
            if ".." not in compte.group(2) and compte.group(2).isdigit():
                porte["condition_zone"] = compte.group(1)
                porte["condition_valeur"] = int(compte.group(2))
            elif ".." in compte.group(2) and compte.group(2).startswith("1.."):
                porte["rearmement"] = True
        fill = re.search(r"fill (-?\d+) (-?\d+) (-?\d+) (-?\d+) (-?\d+) (-?\d+) air", a)
        if fill:
            porte["regions"].append([int(v) for v in fill.groups()])
            continue
        son = re.search(r"playsound ([\w.]+)", a)
        if son:
            porte["sons"].append(son.group(1))
            continue
        appel = ID_ZONE.search(a)
        if appel:
            porte["appelle"].append(appel.group(1))
            continue
        tue = re.search(r"kill @e\[(?:type=shulker,)?tag=([a-z0-9_]+)\]", a)
        if tue:
            porte["tue"].append(tue.group(1))
            continue
        retire = re.search(r"scoreboard players remove \.([a-z0-9_]+) (\w+) (\d+)", a)
        if retire:
            porte["compte_retire"] = [retire.group(1), retire.group(2), int(retire.group(3))]
    return sorted(portes.values(), key=lambda p: p["tag"])


def lire_tick():
    """tick.mcfunction est le cerveau PAR TICK : trois familles de mécanismes que leveldetect ne porte pas,
    plus la Power Armour, la barre du réacteur et l'écran de fin."""
    t = lire("tick.mcfunction")
    resultat = {"regles_sans_position": [], "regles_entite": [], "reinitialisation": None,
                "power_armour": None, "reacteur": None, "fin": None}
    for ligne in t.splitlines():
        ligne = ligne.strip()
        # reset : un joueur au point de spawn de la carte relance la campagne
        if "positioned 380 105 -366" in ligne:
            resultat["reinitialisation"] = {"position": [380.0, 105.0, -366.0], "rayon": 10.0}
            continue
        # règle déclenchée par une entité-guide approchée
        entite = re.search(r"as @e\[tag=([a-z0-9_]+)\] at @s if entity @e\[type=player,distance=0\.\.(\d+)\]"
                           r" run scoreboard players set \.([a-z0-9_]+) active (\d+)", ligne)
        if entite:
            resultat["regles_entite"].append({"tag": entite.group(1), "rayon": float(entite.group(2)),
                                              "pose_compteur": [entite.group(3), int(entite.group(4))]})
            continue
        # règle sans position : seul le compteur décide
        global_ = re.search(r"^execute if score \.([a-z0-9_]+) active matches (\d+) "
                            r"run function mj\.gameengine:levels/([a-z0-9_/]+)", ligne)
        if global_:
            resultat["regles_sans_position"].append({"compteur_zone": global_.group(1),
                                                     "compteur_valeur": int(global_.group(2)),
                                                     "appelle": global_.group(3)})
            continue
        # Power Armour : la source regarde un casque et pose la vie maximale
        armure = re.search(r"as @p\[nbt=\{Inventory:\[\{Slot:(\d+)b,id:\"([a-z:_]+)\"\}\]\}\]"
                           r" run attribute @s max_health base set (\d+)", ligne)
        if armure:
            resultat["power_armour"] = {"slot": int(armure.group(1)), "objet_source": armure.group(2),
                                        "max_health": float(armure.group(3))}
            continue
        # barre du réacteur : elle suit le compteur du coeur, et zéro déclenche la fin
        barre = re.search(r"store result bossbar (\w+) value run scoreboard players get "
                          r"\.(\w+) (\w+)", ligne)
        if barre:
            resultat.setdefault("reacteur", {})
            resultat["reacteur"] = {"barre": barre.group(1), "compteur": barre.group(2),
                                    "objectif": barre.group(3)}
            continue
        fin = re.search(r"if score \.(\w+) (\w+) matches (\d+) run function "
                        r"mj\.gameengine:levels/([a-z0-9_/]+)", ligne)
        if fin and resultat.get("reacteur"):
            resultat["fin"] = {"compteur": fin.group(1), "objectif": fin.group(2),
                               "seuil": int(fin.group(3)), "zone": fin.group(4)}
    # la zone de fin porte la téléportation, les sons, l'aveuglement et le titre
    try:
        f = lire("levels/part2/part3/part4/endgame.mcfunction")
        tp = re.search(r"tp @e\[type=player\] (-?[\d.]+) (-?[\d.]+) (-?[\d.]+)", f)
        if resultat["fin"]:
            resultat["fin"]["teleport"] = [float(tp.group(1)), float(tp.group(2)), float(tp.group(3))] if tp else None
            resultat["fin"]["sons"] = re.findall(r"playsound ([\w.]+)", f)
            resultat["fin"]["aveuglement"] = bool(re.search(r"effect give .* blindness", f))
            titre = re.search(r"title @a title .*?\"text\"\s*:\s*\"([^\"]+)\"", f)
            resultat["fin"]["titre"] = titre.group(1) if titre else None
            reset = re.search(r"scoreboard players set \.(\w+) (\w+) (\d+)", f)
            resultat["fin"]["remet_compteur"] = [reset.group(1), reset.group(2), int(reset.group(3))] if reset else None
    except OSError:
        pass
    # la barre : son nom, sa taille, et où elle apparaît
    if resultat.get("reacteur"):
        try:
            r = lire("levels/part2/part3/part4/reactor.mcfunction")
            nom = re.search(r"bossbar add \w+ .*?\"text\"\s*:\s*\"([^\"]+)\"", r)
            taille = re.search(r"bossbar set [\w:]+ max (\d+)", r)
            resultat["reacteur"]["nom"] = nom.group(1) if nom else None
            resultat["reacteur"]["max"] = int(taille.group(1)) if taille else None
        except OSError:
            pass
    # valeur de départ du compteur : la source la repose à la fin
    if resultat["fin"] and resultat["fin"].get("remet_compteur"):
        resultat["reacteur"]["depart"] = resultat["fin"]["remet_compteur"][2]
    return resultat


def lire_reapparition():
    t = lire("death.mcfunction")
    return {int(n): [float(x), float(y), float(z)]
            for n, x, y, z in re.findall(
                r"if score \.level level matches (\d+) run tp @s (-?[\d.]+) (-?[\d.]+) (-?[\d.]+)", t)}


def lire_verre_rouge():
    return [[int(v) for v in p] for p in re.findall(
        r"fill (-?\d+) (-?\d+) (-?\d+) (-?\d+) (-?\d+) (-?\d+) red_stained_glass_pane",
        lire("load.mcfunction"))]


# ----------------------------------------------------------------------------- contenu des zones
W = SOURCE.replace("GameEngine/data/mj.gameengine", "AstartesEnemies/data/mj.whenemies")
NON_RESOLUES = []
APPELS = {}


def vus_du_chemin(chemin):
    """Nombre de fois que ce chemin a déjà été résolu, pour arrêter une éventuelle boucle."""
    APPELS[chemin] = APPELS.get(chemin, 0) + 1
    return APPELS[chemin]
PREFIXE = "mj.whenemies:"


def offsets_invocation(chemin_invocation):
    """Les décalages d'une fonction d'invocation : ses `summon` se font en relatif (~ ~ ~). On lit les
    décalages réels au lieu de supposer qu'ils sont nuls.

    `chemin_invocation` est le chemin COMPLET (« cultist1/cultistsummon ») : le fichier ne porte pas
    toujours le nom du dossier, et supposer l'inverse faisait disparaître trois cultistes de l'armurerie."""
    chemin = f"{W}/{chemin_invocation}.mcfunction"
    if not os.path.exists(chemin):
        return [(0.0, 0.0, 0.0)]
    decalages = []
    for ligne in open(chemin, encoding="utf-8", errors="ignore"):
        m = re.match(r"summon (\w+) (~|~?-?[\d.]*) (~|~?-?[\d.]*) (~|~?-?[\d.]*)", ligne.strip())
        if m and m.group(1) not in ("shulker", "item", "armor_stand"):
            decalages.append(tuple(0.0 if v in ("~", "") else float(v) for v in m.groups()[1:]))
    # une invocation de la source = UNE créature dans notre port (son HellDrake, lui, est un phantom
    # PLUS un ghast invisibles ; le nôtre est une seule entité).
    return decalages[:1] or [(0.0, 0.0, 0.0)]


def apparitions_zone(chemin, ancre=None, profondeur=0):
    """Résout les créatures d'une zone EN SUIVANT les appels imbriqués. La source invoque en relatif
    (`~ ~ ~`) depuis l'ancrage de l'appelant : sans ce suivi, 20 marines du réacteur disparaissaient du
    relevé, et des coordonnées se retrouvaient en texte au lieu de nombres.

    On ne mémorise PAS les sous-appels déjà vus : la source appelle `marinesummon` cinq fois, à cinq
    ancrages différents, et chacun compte. Seule la profondeur est bornée, pour arrêter une boucle."""
    if profondeur > 4:
        return []
    if vus_du_chemin(chemin) > 6:
        return []
    try:
        t = lire(f"levels/{chemin}.mcfunction")
    except OSError:
        return []
    resultat = []
    for ligne in t.splitlines():
        ligne = ligne.strip()
        pos = POSITION.search(ligne)
        if pos:
            x, y, z = float(pos.group(1)), float(pos.group(2)), float(pos.group(3))
        elif ancre:
            x, y, z = ancre
        else:
            x = y = z = None
        sous = re.search(r"run function mj\.gameengine:levels/([a-z0-9_/]+)", ligne)
        if sous:
            resultat += apparitions_zone(sous.group(1), (x, y, z), profondeur + 1)
            continue
        appel = re.search(r"run function mj\.whenemies:([a-z0-9_]+)/([a-z0-9_]+)", ligne)
        if appel:
            nom = appel.group(1)                 # le DOSSIER porte le nom de la créature
            invocation = f"{appel.group(1)}/{appel.group(2)}"
            if x is None:
                NON_RESOLUES.append(f"{nom} (depuis {chemin})")
                continue
            for dx, dy, dz in offsets_invocation(invocation):
                resultat.append({"mob_source": nom, "x": x + dx, "y": y + dy, "z": z + dz})
            continue
        direct = re.match(r"summon (\w+) (~|~?-?[\d.]*) (~|~?-?[\d.]*) (~|~?-?[\d.]*)", ligne)
        if direct and direct.group(1) not in ("shulker", "item", "armor_stand"):
            tags = re.search(r'Tags:\[([^\]]*)\]', ligne)
            noms = [t.strip().strip('"') for t in tags.group(1).split(",")] if tags else []
            nom = next((n for n in noms if n in CORRESPONDANCE), noms[0] if noms else None)
            if nom is None or x is None:
                NON_RESOLUES.append(f"{nom or ligne[:40]} (depuis {chemin})")
                continue
            dx, dy, dz = (0.0 if v in ("~", "") else float(v) for v in direct.groups()[1:])
            resultat.append({"mob_source": nom, "x": x + dx, "y": y + dy, "z": z + dz})
    return resultat


def contenu_zone(chemin):
    """Ce qu'une fonction de zone fait : répliques, titres, waypoints, créatures, objets donnés,
    appels différés, compteurs posés, niveau posé."""
    try:
        t = lire(f"levels/{chemin}.mcfunction")
    except OSError:
        return None
    zone = {"zone": chemin, "repliques": [], "titres": [], "waypoints": [], "apparitions": [],
            "objets": [], "appels_differes": [], "pose_compteur": [], "pose_niveau": []}
    for ligne in t.splitlines():
        ligne = ligne.strip()
        if ligne.startswith("tellraw @a"):
            for texte in re.findall(r'"text"\s*:\s*"((?:[^"\\]|\\.)*)"', ligne):
                if texte.strip("- "):
                    zone["repliques"].append(texte)
        titre = re.search(r"title @a actionbar .*?\"text\"\s*:\s*\"((?:[^\"\\]|\\.)*)\"", ligne)
        if titre and titre.group(1).strip("- "):
            zone["titres"].append(titre.group(1))
        wp = re.search(r"summon shulker (-?[\d.]+) (-?[\d.]+) (-?[\d.]+) .*?Tags:\[([^\]]*)\]", ligne)
        if wp:
            zone["waypoints"].append({"x": float(wp.group(1)), "y": float(wp.group(2)),
                                      "z": float(wp.group(3)),
                                      "tags": [x.strip().strip('"') for x in wp.group(4).split(",")]})
        obj = re.search(r"summon item (-?[\d.]+) (-?[\d.]+) (-?[\d.]+) .*?"
                        r'custom_name"\s*:\s*\'?\{\s*"bold"[^}]*"text"\s*:\s*"([^"]+)"', ligne)
        if obj:
            zone["objets"].append({"x": float(obj.group(1)), "y": float(obj.group(2)),
                                   "z": float(obj.group(3)), "nom": obj.group(4)})
        mannequin = re.search(r"summon armor_stand (-?[\d.]+) (-?[\d.]+) (-?[\d.]+)", ligne)
        if mannequin:
            for emplacement in re.findall(r'"minecraft:equippable":\{slot:"(feet|legs|chest|head)"', ligne):
                if emplacement in ARMURE:
                    zone["objets"].append({"x": float(mannequin.group(1)), "y": float(mannequin.group(2)),
                                           "z": float(mannequin.group(3)),
                                           "nom_source": "armure de puissance",
                                           "objet": ARMURE[emplacement]})
        diff = re.search(r"schedule function mj\.gameengine:levels/([a-z0-9_/]+) (\d+)([st])", ligne)
        if diff:
            zone["appels_differes"].append([diff.group(1), int(diff.group(2)), diff.group(3)])
        pose = re.search(r"scoreboard players set \.([a-z0-9_]+) active (\d+)", ligne)
        if pose:
            zone["pose_compteur"].append([pose.group(1), int(pose.group(2))])
        niv = re.search(r"scoreboard players set \.level level (\d+)", ligne)
        if niv:
            zone["pose_niveau"].append(int(niv.group(1)))
    APPELS.clear()
    zone["apparitions"] = apparitions_zone(chemin)
    return zone


def main() -> int:
    reapparition = lire_reapparition()
    zones, problemes, inconnus = {}, [], set()
    racine = f"{SOURCE}/levels"
    chemins = []
    for dossier, _, fichiers in os.walk(racine):
        for f in fichiers:
            if f.endswith(".mcfunction"):
                chemins.append(os.path.relpath(os.path.join(dossier, f), racine)[:-len(".mcfunction")])
    for chemin in sorted(chemins):
        zone = contenu_zone(chemin)
        if zone is None:
            problemes.append(f"zone illisible : {chemin}")
            continue
        mobs = []
        for a in zone["apparitions"]:
            mob = CORRESPONDANCE.get(a["mob_source"], a["mob_source"])
            if a["mob_source"] not in CORRESPONDANCE:
                inconnus.add(a["mob_source"])
            mobs.append({"mob": mob, "mob_source": a["mob_source"],
                         "x": round(a["x"], 1), "y": round(a["y"], 1), "z": round(a["z"], 1)})
        zone["apparitions"] = mobs
        zones[CLE_ZONE(chemin)] = zone

    regles = lire_regles()
    portes = lire_portes()
    for regle in regles:
        for appel in regle["appelle"]:
            if CLE_ZONE(appel) not in zones:
                problemes.append(f"regle a {regle['position']} appelle une zone inconnue : {appel}")
    for porte in portes:
        for appel in porte["appelle"]:
            if CLE_ZONE(appel) not in zones:
                problemes.append(f"mecanisme {porte['tag']} appelle une zone inconnue : {appel}")

    tick = lire_tick()
    for regle in tick["regles_sans_position"]:
        if CLE_ZONE(regle["appelle"]) not in zones:
            problemes.append(f"regle par tick sans position appelle une zone inconnue : {regle['appelle']}")
    for regle in tick["regles_entite"]:
        if CLE_ZONE(regle["pose_compteur"][0]) is None:
            problemes.append(f"regle par tick sur entite {regle['tag']} : compteur sans zone")
    if tick["fin"] and CLE_ZONE(tick["fin"]["zone"]) not in zones:
        problemes.append(f"la fin appelle une zone inconnue : {tick['fin']['zone']}")
    for zone in zones.values():
        for objet in zone["objets"]:
            if "objet" not in objet:
                source = objet.get("nom_source", objet["nom"])
                if source in OBJETS:
                    objet["objet"] = OBJETS[source]
                else:
                    problemes.append(f"objet donne sans correspondance : {source}")

    donnees = {"zones": list(zones.values()), "regles": regles, "portes": portes, "tick": tick,
               "verre_rouge_chargement": lire_verre_rouge(), "reapparitions": reapparition,
               "gamerules_source": {"spawn_mobs": False, "mob_griefing": False,
                                    "keep_inventory": True}}
    os.makedirs(os.path.dirname(DEST), exist_ok=True)
    with open(DEST, "w", encoding="utf-8") as f:
        json.dump(donnees, f, indent=1, ensure_ascii=False)

    total_mobs = sum(len(z["apparitions"]) for z in zones.values())
    total_repl = sum(len(z["repliques"]) for z in zones.values())
    print(f"{len(zones)} zones · {len(regles)} regles · {len(portes)} mecanismes · "
          f"{total_mobs} creatures · {total_repl} repliques")
    print(f"  waypoints {sum(len(z['waypoints']) for z in zones.values())} · "
          f"objets donnes {sum(len(z['objets']) for z in zones.values())} · "
          f"appels differes {sum(len(z['appels_differes']) for z in zones.values())}")
    betweenes = [r for r in regles if r["condition"]["type"] == "joueur_proche"]
    print(f"  regles d'entree {len(betweenes)} · regles « zone nettoyee » "
          f"{len(regles) - len(betweenes)}")
    print(f"  tick : {len(tick['regles_sans_position'])} regles sans position · "
          f"{len(tick['regles_entite'])} regles sur entite · reset au spawn "
          f"{tick['reinitialisation'] is not None} · power armour {tick['power_armour'] is not None}")
    if tick.get("reacteur"):
        print(f"  barre du reacteur : « {tick['reacteur'].get('nom')} » max {tick['reacteur'].get('max')} "
              f"· depart {tick['reacteur'].get('depart')} · fin a {tick['fin']['seuil']} "
              f"→ tp {tick['fin'].get('teleport')} · titre « {tick['fin'].get('titre')} »")
    reacteur = zones.get("part2_part3_part4_reactor", {}).get("apparitions", [])
    print(f"  reacteur : {len(reacteur)} creatures aux ancrages de la source")
    # Un utilitaire appelé par une autre zone voit ses créatures comptées CHEZ SON APPELANT : ce n'est
    # pas une perte. On n'alerte donc que pour une zone de premier niveau qui, elle, ne serait résolue
    # par personne — c'est-à-dire une vraie créature oubliée.
    appelees = set()
    for chemin in chemins:
        t = lire(f"levels/{chemin}.mcfunction") if os.path.exists(f"{SOURCE}/levels/{chemin}.mcfunction") else ""
        for sous in re.findall(r"run function mj\.gameengine:levels/([a-z0-9_/]+)", t):
            appelees.add(sous)
    orphelines = [n for n in NON_RESOLUES if n.split(" (depuis ")[-1].rstrip(")") not in appelees]
    if orphelines:
        print(f"  ATTENTION : {len(orphelines)} invocation(s) sans ancrage ET sans appelant — "
              f"{sorted(set(orphelines))}")
        problemes.append("creatures non resolues : " + str(sorted(set(orphelines))))
    elif NON_RESOLUES:
        print(f"  ({len(NON_RESOLUES)} invocation(s) relatives dans {len({n.split(' (depuis ')[-1] for n in NON_RESOLUES})} "
              f"utilitaire(s) : comptees chez leurs appelants)")
    if inconnus:
        problemes.append("creatures sans correspondance : " + ", ".join(sorted(inconnus)))
    if problemes:
        print("\nPROBLEMES :")
        for p in problemes:
            print("  ·", p)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
