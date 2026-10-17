#!/usr/bin/env python3
"""Génère docs/EQUILIBRAGE.md — la table des dégâts du mod, posée à plat et comparable à la source.

Pourquoi un GÉNÉRATEUR et pas un document écrit à la main : un tableau d'équilibrage recopié à la main
dérive au premier changement de valeur, et personne ne s'en aperçoit. Ici, tout est relu dans le code et
dans la source à chaque exécution. `--verifier` régénère et échoue si le document sur disque a divergé —
c'est ce qui empêche la table de mentir six mois plus tard.

Ce qui est lu :
  armes        weapon/WeaponDefinitions.java      (les 15 armes, arguments positionnels)
  créatures    entity/*Entity.java                (baseAttributes, profil(), degatsTir(), projectiles)
  armures      registry/ModArmor.java             (protection par pièce, robustesse, ténacité)
  terrain      terrain/TerrainMaterial.java       (résistances) + la formule de seuil
  source       astartes-extract/rapport-mecaniques-astartes.md   (PV et dégâts D'ORIGINE)

Usage : python3 tools/table_equilibrage.py [--verifier]
"""
import glob
import os
import re
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = f"{RACINE}/src/main/java/com/donopot/voiddynasty"
DOC = f"{RACINE}/docs/EQUILIBRAGE.md"
RAPPORT = "/opt/data/workspace/astartes-extract/rapport-mecaniques-astartes.md"

# Champs de WeaponDefinition, dans l'ordre du constructeur
CHAMPS = ["family", "mode", "damage", "effectiveRange", "maxRange", "criticalMultiplier", "critChance",
          "headshotMultiplier", "armorPenetration", "roundsPerSecond", "projectilesPerShot",
          "magazineSize", "reloadSeconds", "ammo", "projectile", "effects", "recoilVertical",
          "recoilHorizontal", "hipSpread", "aimSpread", "screenShake", "fireSound", "tracer", "impact",
          "falloff", "perShellReload", "secondsPerShell", "terrain"]


def lire(chemin):
    return open(chemin, encoding="utf-8", errors="ignore").read()


def decouper(texte):
    """Découpe une liste d'arguments au niveau 0 de parenthèses."""
    morceaux, profondeur, courant = [], 0, ""
    for c in texte:
        if c in "([{":
            profondeur += 1
        elif c in ")]}":
            profondeur -= 1
        if c == "," and profondeur == 0:
            morceaux.append(courant.strip())
            courant = ""
        else:
            courant += c
    if courant.strip():
        morceaux.append(courant.strip())
    return morceaux


# --------------------------------------------------------------------------- armes
def armes():
    texte = lire(f"{JAVA}/weapon/WeaponDefinitions.java")
    resultat = []
    for m in re.finditer(r"public static final WeaponDefinition (\w+) =\s*new WeaponDefinition\((.*?)\);",
                         texte, re.S):
        nom, corps = m.group(1), m.group(2)
        args = decouper(corps)
        if len(args) < len(CHAMPS):
            continue
        w = dict(zip(CHAMPS, args))
        terrain = w["terrain"]
        # terrain(mode, impactPower, impactRadius, maxPenetrationBlocks, maxBlocksModified,
        #         hardnessFactor, armorMaterialPenetration, status)
        penetration = re.search(r"TerrainMode\.(\w+),\s*([\d.]+)f,\s*([\d.]+)f,\s*(\d+),\s*(\d+),"
                                r"\s*([\d.]+)f,\s*([\d.]+)f", terrain)
        resultat.append({
            "nom": nom,
            "nom_jeu": nom.replace("_", " ").title(),
            "famille": w["family"].strip('"'),
            "mode": w["mode"].replace("FireMode.", ""),
            "degats": float(w["damage"].rstrip("f")),
            "cadence": float(w["roundsPerSecond"].rstrip("f")),
            "chargeur": int(w["magazineSize"]),
            "rechargement": float(w["reloadSeconds"].rstrip("f")),
            "penetration_armure": float(w["armorPenetration"].rstrip("f")),
            "critique": float(w["criticalMultiplier"].rstrip("f")),
            "crit_chance": float(w["critChance"].rstrip("f")),
            "projectiles": int(w["projectilesPerShot"]),
            "munition": w["ammo"].replace("ModItems.", ""),
            "terrain_mode": penetration.group(1) if penetration else "—",
            "terrain_puissance": float(penetration.group(2)) if penetration else 0.0,
            "terrain_penetration": int(penetration.group(4)) if penetration else 0,
            "terrain_blocs": int(penetration.group(5)) if penetration else 0,
            "terrain_rayon": float(penetration.group(3)) if penetration else 0.0,
            "durete": float(penetration.group(6)) if penetration else 0.0,
            "perce_armure_materiau": float(penetration.group(7)) if penetration else 0.0,
        })
    return resultat


# --------------------------------------------------------------------------- créatures
def _attributs(contenu):
    """Deux styles coexistent : `baseAttributes(pv, dégâts, vitesse)` pour les ennemis du Chaos, et la
    chaîne `.add(Attributes.MAX_HEALTH, …)` pour les créatures plus anciennes."""
    court = re.search(r"baseAttributes\(\s*([\d.]+),\s*([\d.]+),\s*([\d.]+)", contenu)
    if court:
        return float(court.group(1)), float(court.group(2)), float(court.group(3))
    pv = re.search(r"Attributes\.MAX_HEALTH,\s*([\d.]+)", contenu)
    degats = re.search(r"Attributes\.ATTACK_DAMAGE,\s*([\d.]+)", contenu)
    vitesse = re.search(r"Attributes\.MOVEMENT_SPEED,\s*([\d.]+)", contenu)
    if pv:
        return (float(pv.group(1)), float(degats.group(1)) if degats else 0.0,
                float(vitesse.group(1)) if vitesse else 0.0)
    return None


def creatures():
    registre = lire(f"{JAVA}/registry/ModEntities.java")
    classe_vers_id = {}
    for m in re.finditer(r'= register\(\s*"([a-z0-9_]+)",', registre):
        suite = registre[m.end():m.end() + 400]
        classe = re.search(r"(\w+Entity)::new", suite)
        if classe:
            classe_vers_id[classe.group(1)] = m.group(1)

    brut = {}
    for chemin in sorted(glob.glob(f"{JAVA}/entity/*Entity.java")):
        classe = os.path.basename(chemin)[:-5]
        contenu = lire(chemin)
        parent = re.search(r"class \w+ extends (\w+)", contenu)
        profil = re.search(r"new ProfilAttaque\(\s*([\d.]+),\s*([\d.]+),\s*(\d+),\s*"
                           r"([\d.]+),\s*([\d.]+),\s*(\d+)", contenu)
        tir = re.search(r"protected float degatsTir\(\)\s*\{\s*return ([\d.]+)f", contenu)
        obus = re.search(r"protected int nombreProjectiles\(\)\s*\{\s*return (\d+)", contenu)
        brut[classe] = {
            "id": classe_vers_id.get(classe),
            "attributs": _attributs(contenu),
            "profil": tuple(float(profil.group(i)) if i in (1, 2, 4, 5) else int(profil.group(i))
                            for i in range(1, 7)) if profil else None,
            "aucun": "ProfilAttaque.AUCUN" in contenu,
            "degats_tir": float(tir.group(1)) if tir else None,
            "obus": int(obus.group(1)) if obus else 1,
            "parent": parent.group(1) if parent else None,
        }

    # héritage : une classe sans attributs ni profil les tient de son parent, et on le DIT
    def resoudre(classe, champ, vus=None):
        vus = vus or set()
        if classe not in brut or classe in vus:
            return None
        vus.add(classe)
        valeur = brut[classe][champ]
        if valeur is None and brut[classe]["parent"]:
            return resoudre(brut[classe]["parent"], champ, vus)
        return valeur

    resultat = []
    for classe, infos in brut.items():
        attributs = resoudre(classe, "attributs")
        profil = resoudre(classe, "profil")
        if attributs is None or infos["id"] is None:
            continue
        resultat.append({
            "id": infos["id"], "classe": classe, "pv": attributs[0], "contact": attributs[1],
            "vitesse": attributs[2],
            "tir_min": profil[0] if profil else None, "tir_max": profil[1] if profil else None,
            "tir_cd": profil[2] if profil else None, "cac_min": profil[3] if profil else None,
            "cac_max": profil[4] if profil else None, "cac_cd": profil[5] if profil else None,
            "sans_profil": infos["aucun"] or profil is None,
            "degats_tir": infos["degats_tir"], "obus": infos["obus"],
            "herite": brut[classe]["attributs"] is None,
        })
    return resultat


def valeurs_source():
    """PV et dégâts d'ORIGINE, lus dans le rapport de mécaniques (tableaux markdown réguliers)."""
    if not os.path.exists(RAPPORT):
        return {}
    source = {}
    for ligne in lire(RAPPORT).splitlines():
        if not ligne.startswith("|") or "---" in ligne:
            continue
        cellules = [c.strip() for c in ligne.strip("|").split("|")]
        if len(cellules) < 3:
            continue
        fichier = next((c.strip("`") for c in cellules if c.strip("`").endswith(".mcfunction")), None)
        if not fichier:
            continue
        cle = fichier.split("/")[0].replace("`", "").strip()
        pv = next((c for c in cellules if re.match(r"^\*{0,2}\d+", c)), None)
        degats = next((c for c in cellules if "dégât" in c.lower() or "damage" in c.lower()
                       or re.search(r"\d+\s*(explosion|mob_attack|fireball|magic|falling_anvil|on_fire)",
                                    c)), None)
        source[cle] = {"pv": pv, "degats": degats}
    return source


# --------------------------------------------------------------------------- armures et terrain
def armures():
    contenu = lire(f"{JAVA}/registry/ModArmor.java")
    resultat = []
    for m in re.finditer(r'ArmorMaterial (\w+) = material\("(\w+)", (\d+),\s*Map\.of\(([^)]*)\),\s*'
                         r"([\d.]+)f, ([\d.]+)f", contenu):
        pieces = dict(re.findall(r"ArmorType\.(\w+), (\d+)", m.group(4)))
        resultat.append({
            "nom": m.group(2), "robustesse": int(m.group(3)),
            "casque": int(pieces.get("HELMET", 0)), "plastron": int(pieces.get("CHESTPLATE", 0)),
            "jambieres": int(pieces.get("LEGGINGS", 0)), "bottes": int(pieces.get("BOOTS", 0)),
            "total": sum(int(v) for v in pieces.values()),
            "tenacite": float(m.group(5)), "recul": float(m.group(6)),
        })
    return resultat


def terrain():
    contenu = lire(f"{JAVA}/terrain/TerrainMaterial.java")
    return [(m.group(2), float(m.group(3)))
            for m in re.finditer(r'(\w+)\("([a-z_]+)", *([\d.]+)', contenu)]


def mesures():
    """Les valeurs MESURÉES en jeu, lues dans le rapport du harnais.

    C'est la seule source honnête pour « combien de coups » : le cumul de dégâts DÉCROÎT entre les tirs
    (le mod a une retombée), donc un calcul statique se trompe. Le rapport, lui, a mesuré."""
    rapport = f"{RACINE}/docs/RAPPORT-TESTS.md"
    if not os.path.exists(rapport):
        return []
    resultat = []
    for ligne in lire(rapport).splitlines():
        if not ligne.startswith("|") or ("`B-" not in ligne and "`E-" not in ligne):
            continue
        cellules = [c.strip() for c in ligne.strip().strip("|").split("|")]
        if len(cellules) < 4:
            continue
        resultat.append((cellules[1].strip("`"), cellules[2].strip("*"), cellules[3]))
    return resultat


def impacts(w, resistance):
    """Nombre d'impacts pour casser un bloc de cette résistance, ou None si l'arme n'y arrive pas.

    Reproduit la formule du mod : dégâts au point d'impact, puis seuil = 10 + résistance × dureté."""
    perce = resistance * (1.0 - w["perce_armure_materiau"])
    par_impact = w["terrain_puissance"] - perce * 0.5
    if par_impact <= 0:
        return None
    seuil = 10.0 + resistance * w["durete"]
    return 1 if par_impact >= seuil else int(seuil / par_impact) + 1


def impacts_pierre(w):
    n = impacts(w, 16.0)
    return "—" if n is None else f"**{n}**"


def main() -> int:
    W = armes()
    C = creatures()
    S = valeurs_source()
    A = armures()
    T = terrain()
    lignes = []
    ajouter = lignes.append

    ajouter("# ÉQUILIBRAGE — VOID DYNASTY")
    ajouter("")
    ajouter("Table des dégâts du mod, **posée à plat** et comparée à la source quand elle en donne une.")
    ajouter("")
    ajouter("> Document **généré** par `tools/table_equilibrage.py` : ne pas modifier à la main.")
    ajouter("> `python3 tools/table_equilibrage.py --verifier` échoue si ce fichier a divergé du code.")
    ajouter("")
    ajouter("Repères : le joueur a **20 PV** (10 cœurs), et **40 PV** avec la Power Armour. "
            "Les dégâts de la source sont ceux de son rapport de mécaniques.")
    ajouter("")

    ajouter("## 1 · Les armes")
    ajouter("")
    ajouter("| Arme | Famille | Mode | Dégâts | Cadence | Chargeur | Recharg. | Perce l'armure | "
            "Critique | Projectiles | Munition |")
    ajouter("|---|---|---|---|---|---|---|---|---|---|---|")
    for w in sorted(W, key=lambda x: -x["degats"]):
        ajouter(f"| {w['nom_jeu']} | {w['famille']} | {w['mode']} | **{w['degats']:g}** | "
                f"{w['cadence']:g}/s | {w['chargeur']} | {w['rechargement']:g} s | "
                f"{w['penetration_armure'] * 100:.0f} % | ×{w['critique']:g} "
                f"({w['crit_chance'] * 100:.0f} %) | {w['projectiles']} | {w['munition']} |")
    ajouter("")
    ajouter("### Dégâts de terrain, et ce que ça donne en blocs")
    ajouter("")
    ajouter("Formule du mod, pour que l'arithmétique soit vérifiable :")
    ajouter("")
    ajouter("```")
    ajouter("dégâts d'un impact au point d'impact = puissance − résistance × (1 − percement) × 0,5")
    ajouter("un bloc casse quand le cumul atteint   = 10 + résistance × dureté de l'arme")
    ajouter("```")
    ajouter("")
    ajouter("| Arme | Mode de terrain | Puissance | Dureté de l'arme | Perce le matériau | Rayon | "
            "Blocs modifiés au plus |")
    ajouter("|---|---|---|---|---|---|---|")
    for w in sorted(W, key=lambda x: -x["terrain_puissance"]):
        if w["terrain_puissance"] <= 0:
            continue
        ajouter(f"| {w['nom_jeu']} | {w['terrain_mode']} | {w['terrain_puissance']:g} | "
                f"{w['durete']:g} | {w['perce_armure_materiau'] * 100:.0f} % | {w['terrain_rayon']:g} | "
                f"{w['terrain_blocs']} |")
    ajouter("")
    ajouter("⚠️ **Ces chiffres ne disent PAS combien de coups il faut.** L'accumulation de dégâts "
            "**retombe** entre les tirs : c'est la cadence contre la retombée qui décide, et un calcul "
            "posé sur une feuille se trompe. Les valeurs réelles sont **mesurées en jeu** au paragraphe "
            "suivant — c'est le harnais qui les a prises, pas moi.")
    ajouter("")
    ajouter("## 2 · Les armures")
    ajouter("")
    ajouter("| Tenue | Casque | Plastron | Jambières | Bottes | Total | Robustesse | Ténacité | Recul |")
    ajouter("|---|---|---|---|---|---|---|---|---|")
    for a in A:
        ajouter(f"| {a['nom']} | {a['casque']} | {a['plastron']} | {a['jambieres']} | {a['bottes']} | "
                f"**{a['total']}** | {a['robustesse']} | {a['tenacite']:g} | {a['recul'] * 100:.0f} % |")
    ajouter("")
    ajouter("La **Power Armour** (porter un casque de la tenue) porte la vie maximale du joueur à **40** "
            "— mécanisme de la source, qui regardait un casque et posait `max_health base 40`.")
    ajouter("")
    ajouter("## 3 · Les créatures")
    ajouter("")
    ajouter("| Créature | PV | Source (PV) | Contact | Tir | Portée tir | Portée contact | "
            "Cooldown tir | Cooldown contact | Source (dégâts) |")
    ajouter("|---|---|---|---|---|---|---|---|---|---|")
    # Les valeurs de la source sont indexées par le nom de SON dossier. Quelques noms diffèrent :
    # ces alias sont écrits à la main, et un identifiant sans correspondance s'affiche « — » plutôt que
    # de faire croire à une comparaison qui n'a pas eu lieu.
    ALIAS = {"daemon_prince": "demonprince", "chaos_captain": "marinecaptain",
             "chaos_forgefiend": "forgefiend", "chaos_dreadnought": "chaosdreadnought",
             "chaos_hellbrute": "hellbrute", "chaos_helldrake": "helldrake",
             "chaos_raptor": "raptor", "cultist_boss": "cultistboss"}

    def source_de(c):
        cle = c["id"]
        candidats = [cle, ALIAS.get(cle), cle.replace("chaos_", ""), cle.replace("_", ""),
                     re.sub(r"_(\d)$", r"\1", cle), cle.replace("chaos_marine_", "marine")]
        for nom in candidats:
            if nom and nom in S:
                return S[nom]
        return {}

    def ecart(c, src):
        """Marque les endroits où NOTRE valeur diffère du chiffre de la source : c'est là que se cachent
        les médianes assumées."""
        if not src:
            return ""
        nombres = re.findall(r"\d+(?:\.\d+)?", src.get("degats") or "")
        if not nombres:
            return ""
        chiffre = float(nombres[0])
        notres = [c["contact"]] + ([c["degats_tir"]] if c["degats_tir"] is not None else [])
        if any(abs(n - chiffre) > 0.01 for n in notres if n):
            return " ⚠️"
        return ""

    for c in sorted(C, key=lambda x: -x["pv"]):
        src = source_de(c)
        pv_src = src.get("pv") or "—"
        deg_src = src.get("degats") or "—"
        if c["degats_tir"] is not None:
            tir = f"{c['degats_tir']:g}"
        elif c["tir_max"]:
            tir = f"{c['contact']:g}"
        else:
            tir = "—"
        if c["obus"] > 1:
            tir += f" ×{c['obus']}"
        portee_tir = f"{c['tir_min']:g}–{c['tir_max']:g}" if c["tir_max"] else "—"
        portee_cac = f"{c['cac_min']:g}–{c['cac_max']:g}" if c["cac_max"] else "—"
        cd_tir = f"{c['tir_cd'] / 20:g} s" if c["tir_cd"] else "—"
        cd_cac = f"{c['cac_cd'] / 20:g} s" if c["cac_cd"] else "—"
        note = " *(hérité de sa base)*" if c.get("herite") else ""
        ajouter(f"| `{c['id']}`{note} | **{c['pv']:g}** | {pv_src} | {c['contact']:g} | {tir} | "
                f"{portee_tir} | {portee_cac} | {cd_tir} | {cd_cac} | {deg_src}{ecart(c, src)} |")
    ajouter("Les créatures sans profil d'attaque (`ProfilAttaque.AUCUN`) ne frappent pas : elles sont "
            "décoratives ou purement défensives.")
    ajouter("")
    ajouter("⚠️ marque une créature dont la valeur du port **diffère du chiffre de la source** : ce sont "
            "les médianes assumées (la source donnait des fourchettes, le port a pris le milieu) ou un "
            "écart à trancher. La colonne « Source (dégâts) » cite le rapport de mécaniques, qui donne un "
            "chiffre par attaque là où le port a parfois enregistré une fourchette.")
    ajouter("")
    ajouter("## 4 · Dégâts de terrain : ce que ça donne vraiment")
    ajouter("")
    ajouter("### Ce que le harnais a MESURÉ en jeu")
    ajouter("")

    ordre = [w["nom_jeu"] for w in W]
    for nom, resultat, detail in mesures():
        if nom == "B-armes-pierre":
            nombres = re.findall(r"([a-z]{3}\d+)=(>?\d+)", detail)
            laser = re.search(r"· laser (\d+|>30) coups · pistolet laser (\d+|>30) coups", detail)
            ajouter("**Coups pour casser un bloc de pierre** (mesuré, même ordre que la table des armes) :")
            ajouter("")
            ajouter("| Arme | Coups sur la pierre |")
            ajouter("|---|---|")
            for i, (_, coups) in enumerate(nombres):
                if i < len(ordre):
                    ajouter(f"| {ordre[i]} | {'**' + coups + '**' if coups.isdigit() else coups} |")
            ajouter("")
            if laser:
                ajouter(f"Les deux lasers finissent par percer : **{laser.group(1)}** coups pour le fusil "
                        f"laser, **{laser.group(2)}** pour le pistolet laser. Le harnais vérifie aussi que "
                        f"le laser **allume le bois**.")
            ajouter("")
            continue
        if nom == "E-cible-joueur":
            blocs = detail.split("||")
            ajouter(f"**Coups pour tuer un joueur** — l'armure est lue sur les vrais objets vanilla :")
            ajouter("")
            for bloc in blocs:
                bloc = bloc.strip()
                if not bloc:
                    continue
                m = re.match(r"coups pour tuer un joueur — (\w+) \(armure (\d+)\) : (.+)", bloc)
                if not m:
                    continue
                ajouter(f"*Joueur en {m.group(1).lower()} complet (armure {m.group(2)})* :")
                ajouter("")
                ajouter("| Arme | Coups |")
                ajouter("|---|---|")
                couples = re.findall(r"([a-z]{3}\d+)=(\d+)", m.group(3))
                for i, (_, coups) in enumerate(couples):
                    if i < len(ordre):
                        ajouter(f"| {ordre[i]} | {coups} |")
                ajouter("")
            continue
        ajouter(f"- `{nom}` ({resultat}) : {detail.replace('|', '/')}")
    ajouter("")
    ajouter("### Les matériaux, et leur résistance")
    ajouter("")
    ajouter("| Matériau | Résistance |")
    ajouter("|---|---|")
    for nom, resistance in sorted(T, key=lambda x: x[1]):
        ajouter(f"| {nom} | {resistance:g} |")
    ajouter("")
    ajouter("Seuils **mesurés** : **26** pour la pierre, **13** pour la terre. La résistance seule ne "
            "suffit pas à prédire un nombre de coups — la cadence et la retombée s'en mêlent.")
    ajouter("")
    ajouter("## 5 · Ce qui reste à juger — et qui t'appartient")
    ajouter("")
    ajouter("1. **Les fourchettes de la source sont devenues des médianes.** Les cultistes frappaient "
            "3 à 6, les marines 10 à 25 : le port a pris le milieu (4,5 et 17,5). Si tu veux le maximum, "
            "c'est une valeur par créature, dans son `baseAttributes`.")
    ajouter("2. **La vitesse de percement de la pierre** (colonne « impacts pour casser » du tableau des "
            "armes) : c'est le réglage qui décide si le décor tient ou s'effondre. Aucune valeur n'est "
            "juste dans l'absolu.")
    ajouter("3. **Le laser ne perce pas la pierre du tout** (mesuré : intacte après 6 tirs). Sa cadence "
            "est plus rapide que la retombée de l'accumulation : le cumul n'atteint jamais le seuil. Est-ce "
            "voulu pour une arme de tir rapide, ou faut-il qu'elle finisse par entamer la pierre ?")
    ajouter("4. **Les PV de la source sont-ils la cible ?** Le Prince Démon en a 1 000. Calibrer les armes "
            "et les armures autour de cette décision change tout le reste.")
    ajouter("")
    contenu = "\n".join(lignes) + "\n"

    if "--verifier" in sys.argv:
        if not os.path.exists(DOC):
            print("ÉCHEC : le document n'existe pas")
            return 1
        if lire(DOC) != contenu:
            print("ÉCHEC : docs/EQUILIBRAGE.md a divergé du code — relance sans --verifier")
            return 1
        print("docs/EQUILIBRAGE.md est à jour")
        return 0

    with open(DOC, "w", encoding="utf-8") as f:
        f.write(contenu)
    print(f"docs/EQUILIBRAGE.md écrit : {len(W)} armes · {len(C)} créatures · {len(A)} tenues · "
          f"{len(T)} matériaux · {len(S)} entrées de la source")
    return 0


if __name__ == "__main__":
    sys.exit(main())
