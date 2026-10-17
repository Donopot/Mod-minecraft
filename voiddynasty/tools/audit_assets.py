#!/usr/bin/env python3
"""Contrôle de complétude des assets de VOID DYNASTY.

Répond à : « nos assets sont-ils complets ? » — sans lancer le jeu.

Un serveur headless ne charge JAMAIS les assets client : une référence cassée passe tous les tests
serveur et ne se voit que sur l'écran du joueur. Ce script fait donc le contrôle statique, dans les
DEUX SENS :
  - ce que le code déclare (objets, blocs, entités) → le fichier existe-t-il ?
  - ce qui existe sur le disque → est-ce déclaré quelque part ?

Principe : on ne DEVINE pas les chemins, on les LIT là où le code les écrit — les classes de modèle
GeckoLib déclarent `getModelResource` / `getTextureResource` / `getAnimationResource`, et les
registres déclarent chaque identifiant. Un contrôle qui suppose le nom d'un fichier produit des faux
positifs (trois dans la première version de cet outil) et des faux positifs détruisent la confiance
autant que les faux négatifs.

Contrôles
  1. Objets      : items/<id>.json (26.3) + models/item/<id>.json + textures résolues
  2. Blocs       : blockstates/<id>.json + modèles + textures + objet de bloc
  3. Entités     : ressources réellement déclarées par chaque modèle GeckoLib + renderer client
                   + clips demandés par l'entité
  4. Langues     : en_us ET fr_fr couvrent tout, et sont cohérentes entre elles
  5. Recettes    : chaque recette vise un objet réel ; ingrédients au bon format
  6. Sons        : chaque entrée de sounds.json a son .ogg
  7. Textures    : dimensions multiples de 16, alpha pour les icônes, fichiers suspects
  8. Orphelins   : fichiers présents mais référencés nulle part
  9. Couverture  : ce que le contrôle N'A PAS pu vérifier (dit franchement)

Usage :  python3 tools/audit_assets.py [--racine /chemin/vers/le/mod]
Code de sortie 1 si au moins une anomalie bloquante.
"""
from __future__ import annotations

import argparse
import glob
import json
import os
import re
import struct
import sys

NS = "voiddynasty"
# Armes dotées d'une chaîne d'animation (phase D, tools/generer_animations_armes.py).
ARMES_ANIMEES = ["boltgun", "bolt_pistol", "heavy_bolter", "stalker_bolt_rifle", "las_gun", "laz_pistol",
                 "plasma_pistol", "plasma_incinerator", "shotgun", "flamethrower", "melta_gun",
                 "hunter_killer_missile", "chainsword", "powersword", "power_hammer"]
VERT, ROUGE, JAUNE, GRIS, FIN = "\033[32m", "\033[31m", "\033[33m", "\033[90m", "\033[0m"
bloquants: list[str] = []
avertissements: list[str] = []
couverture: list[str] = []


def ok(label: str, detail: str = "") -> None:
    print(f"  {VERT}[OK]{FIN}   {label}" + (f"  {GRIS}{detail}{FIN}" if detail else ""))


def fail(label: str, detail: str = "") -> None:
    print(f"  {ROUGE}[MANQUE]{FIN} {label}" + (f"  → {detail}" if detail else ""))
    bloquants.append(f"{label} : {detail}")


def warn(label: str, detail: str = "") -> None:
    print(f"  {JAUNE}[ATTENTION]{FIN} {label}" + (f"  → {detail}" if detail else ""))
    avertissements.append(f"{label} : {detail}")


def info(label: str) -> None:
    print(f"  {GRIS}[info]{FIN} {label}")


def lire(p: str) -> str:
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def png_taille(p: str) -> tuple[int, int, int] | None:
    try:
        with open(p, "rb") as f:
            e = f.read(33)
        if e[:8] != b"\x89PNG\r\n\x1a\n":
            return None
        return struct.unpack(">II", e[16:24]) + (e[25],)
    except Exception:  # noqa: BLE001
        return None


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--racine", default=".")
    args = ap.parse_args()
    racine = os.path.abspath(args.racine)
    assets = os.path.join(racine, "src/main/resources/assets", NS)
    data = os.path.join(racine, "src/main/resources/data", NS)
    java = os.path.join(racine, "src/main/java/com/donopot", NS)
    if not os.path.isdir(assets):
        print(f"ERREUR : assets introuvables sous {assets}")
        return 2
    print(f"mod    : {racine}\nassets : {assets}")

    def tex_ok(ref: str) -> bool:
        """Une référence de texture résout-elle ? (les textures vanilla viennent du jeu, pas de nous)

        Les références n'ont pas de forme unique, et surtout pas la même base :
          - modèle d'objet   : `voiddynasty:item/boltgun`          → base `assets/<ns>/textures/`
          - modèle GeckoLib  : `voiddynasty:textures/entity/x.png` → base `assets/<ns>/`
        Comparer l'une avec la convention de l'autre produit un faux positif garanti.
        """
        if not ref or not isinstance(ref, str):
            return True
        if ref.startswith("minecraft:"):
            return True
        chemin = ref.split(":", 1)[-1]
        if chemin.endswith(".png"):
            chemin = chemin[:-4]
        base = assets if chemin.startswith("textures/") else os.path.join(assets, "textures")
        return os.path.exists(os.path.join(base, chemin + ".png"))

    # ---------------------------------------------------------- ce que le code déclare
    def ids_de(chemin: str, aides: str) -> set[str]:
        """Identifiants passés à une aide d'enregistrement : aide("id", …)."""
        if not os.path.exists(chemin):
            return set()
        return set(re.findall(rf'\b(?:{aides})\(\s*"([a-z0-9_]+)"', lire(chemin)))

    items = ids_de(os.path.join(java, "registry/ModItems.java"),
                   "ammo|weapon|tool|spawnEgg|promethiumFuel")
    items |= ids_de(os.path.join(java, "registry/ModArmor.java"), "armor")
    # la classe principale déclare aussi des identifiants (constants Identifier + registres directs)
    for f in glob.glob(os.path.join(java, "*.java")) + glob.glob(os.path.join(java, "**/*.java"), recursive=True):
        t = lire(f)
        items |= set(re.findall(r'fromNamespaceAndPath\(\s*MOD_ID\s*,\s*"([a-z0-9_]+)"', t))
    blocs = ids_de(os.path.join(java, "registry/ModBlocks.java"),
                   "block|metalBlock|machine|promethiumFurnace|promethiumBrewing|superContainer")
    blocs_items = ids_de(os.path.join(java, "registry/ModBlocks.java"), "blockItem")
    src_entities = lire(os.path.join(java, "registry/ModEntities.java")) if os.path.exists(
        os.path.join(java, "registry/ModEntities.java")) else ""
    entites = set(re.findall(r'=\s*register\(\s*"([a-z0-9_]+)"', src_entities))
    constantes = dict(re.findall(r'EntityType<[^>]+>\s+([A-Z0-9_]+)\s*=\s*register\(\s*"([a-z0-9_]+)"', src_entities))

    # renderers : la déclaration vit dans le client init, pas dans les fichiers de renderer
    renderers: dict[str, str] = {}
    for f in glob.glob(os.path.join(java, "client/*.java")):
        for const, classe in re.findall(
                r'EntityRendererRegistry\.register\(\s*ModEntities\.([A-Z0-9_]+)\s*,\s*(\w+)::new', lire(f)):
            renderers[const] = classe
    # un renderer GeckoLib se reconnaît dans SA classe, pas par convention de nom
    geo_renderers: set[str] = set()
    for const, classe in renderers.items():
        p = os.path.join(java, "client", classe + ".java")
        if os.path.exists(p) and re.search(r'extends\s+\w*Geo\w*Renderer', lire(p)):
            geo_renderers.add(const)
    # les 6 objets de la classe principale sont des objets : on les sépare des blocs
    blocs |= {b for b in ids_de(os.path.join(java, "registry/ModBlocks.java"), "block") if b in blocs_items}


    # ressources déclarées par les modèles GeckoLib (source exacte, rien de deviné)
    ressources: dict[str, dict[str, str]] = {}
    for f in glob.glob(os.path.join(java, "client/*Model.java")):
        t = lire(f)
        # la clé est le NOM DE CLASSE déclaré (c'est ainsi que le renderer l'instancie)
        m_classe = re.search(r'class\s+(\w+)', t)
        nom = m_classe.group(1) if m_classe else os.path.basename(f)[: -len(".java")]
        r: dict[str, str] = {}
        for cle, res in (
            ("geo", r'getModelResource\([^)]*\)\s*\{[^}]*?fromNamespaceAndPath\(\s*"[^"]+"\s*,\s*"([^"]+)"'),
            ("texture", r'getTextureResource\([^)]*\)\s*\{[^}]*?fromNamespaceAndPath\(\s*"[^"]+"\s*,\s*"([^"]+)"'),
            ("animation", r'getAnimationResource\([^)]*\)\s*\{[^}]*?fromNamespaceAndPath\(\s*"[^"]+"\s*,\s*"([^"]+)"'),
        ):
            m = re.search(res, t, re.S)
            if m:
                r[cle] = m.group(1)
        if r:
            ressources[nom] = r

    print(f"\ncode : {len(items)} objets · {len(blocs)} blocs · {len(entites)} entités "
          f"· {len(renderers)} renderers (dont {len(geo_renderers)} GeckoLib)")

    defs_items = {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(assets, "items/*.json"))}
    mods_items = {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(assets, "models/item/*.json"))}
    mods_blocs = {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(assets, "models/block/*.json"))}
    etats = {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(assets, "blockstates/*.json"))}

    # ---------------------------------------------------------- 1. objets
    print("\n1. Objets")
    tous_items = items | blocs_items
    m_def = sorted(i for i in tous_items if i not in defs_items)
    m_mod = sorted(i for i in tous_items if i not in mods_items)
    (fail if m_def else ok)(f"{len(tous_items) - len(m_def)}/{len(tous_items)} objets ont leur items/<id>.json",
                            ", ".join(m_def) + (" — sans ce fichier, damier violet/noir" if m_def else ""))
    (fail if m_mod else ok)(f"{len(tous_items) - len(m_mod)}/{len(tous_items)} objets ont leur models/item/<id>.json",
                            ", ".join(m_mod))
    refs, absentes = 0, []
    for p in sorted(glob.glob(os.path.join(assets, "models/item/*.json"))):
        try:
            d = json.loads(lire(p))
        except Exception:  # noqa: BLE001
            continue
        for v in d.get("textures", {}).values():
            if isinstance(v, str) and not v.startswith("minecraft:"):
                refs += 1
                if not tex_ok(v):
                    absentes.append(f"{os.path.basename(p)} → {v}")
    (fail if absentes else ok)(f"{refs - len(absentes)}/{refs} textures d'objets résolues", "; ".join(absentes))

    # ---------------------------------------------------------- 2. blocs
    print("\n2. Blocs")
    for liste, quoi in ((sorted(b for b in blocs if b not in etats), "ont leur blockstates/<id>.json"),
                        (sorted(b for b in blocs if b not in mods_blocs), "ont leur models/block/<id>.json"),
                        (sorted(b for b in blocs if b not in blocs_items), "ont leur objet de bloc")):
        (fail if liste else ok)(f"{len(blocs) - len(liste)}/{len(blocs)} blocs {quoi}", ", ".join(liste))
    # chaque blockstate pointe vers un modèle existant
    etats_casses = []
    for p in sorted(glob.glob(os.path.join(assets, "blockstates/*.json"))):
        t = lire(p)
        for m in re.findall(rf'"{NS}:([a-z0-9_/]+)"', t):
            if not os.path.exists(os.path.join(assets, "models", m + ".json")):
                etats_casses.append(f"{os.path.basename(p)} → {m}")
    (fail if etats_casses else ok)(
        f"{len(etats) - len(etats_casses)}/{len(etats)} blockstates pointent vers un modèle existant" if etats_casses
        else f"{len(etats)}/{len(etats)} blockstates pointent vers un modèle existant", "; ".join(etats_casses))
    bt_refs, bt_abs = 0, []
    for p in sorted(glob.glob(os.path.join(assets, "models/block/*.json"))):
        try:
            d = json.loads(lire(p))
        except Exception:  # noqa: BLE001
            continue
        for v in d.get("textures", {}).values():
            if isinstance(v, str) and v and not v.startswith("minecraft:"):
                bt_refs += 1
                if not tex_ok(v):
                    bt_abs.append(f"{os.path.basename(p)} → {v}")
    (fail if bt_abs else ok)(f"{bt_refs - len(bt_abs)}/{bt_refs} textures de blocs résolues", "; ".join(bt_abs))

    # ---------------------------------------------------------- 3. entités
    print("\n3. Entités (GeckoLib)")
    # chaîne exacte : constante de registre → renderer → CLASSE DE MODÈLE → ressources déclarées
    def modele_pour(const: str) -> str | None:
        p = os.path.join(java, "client", renderers.get(const, "_") + ".java")
        if not os.path.exists(p):
            return None
        m = re.search(r'new\s+(\w+Model)\s*\(', lire(p))
        return m.group(1) if m else None

    m_geo, m_tex, m_anim, sans_renderer, mobiles = [], [], [], [], []
    for const, nom in sorted(constantes.items()):
        if const not in renderers:
            sans_renderer.append(nom)
            continue
        if const not in geo_renderers:
            continue                      # renderer classique (Vex, projectile) : pas de geo attendu
        classe_modele = modele_pour(const)
        r = ressources.get(classe_modele or "", {})
        if not r:
            mobiles.append(f"{nom} (modèle {classe_modele or 'introuvable'})")
            continue
        if not r.get("geo"):
            mobiles.append(f"{nom} (getModelResource illisible)")
            continue
        if not os.path.exists(os.path.join(assets, "geckolib/models", r["geo"] + ".geo.json")):
            m_geo.append(f"{nom} → {r['geo']}.geo.json")
        if not r.get("animation"):
            mobiles.append(f"{nom} (getAnimationResource illisible)")
        elif not os.path.exists(os.path.join(assets, "geckolib/animations", r["animation"] + ".animation.json")):
            m_anim.append(f"{nom} → {r['animation']}.animation.json")
        if not r.get("texture"):
            mobiles.append(f"{nom} (getTextureResource illisible)")
        elif not tex_ok(f"{NS}:{r['texture']}"):
            m_tex.append(f"{nom} → {r['texture']}.png")
    n_geo = len(geo_renderers)
    verifiables = n_geo - len(mobiles)
    (fail if m_geo else ok)(f"{verifiables - len(m_geo)}/{verifiables} créatures GeckoLib ont leur modèle géométrique", ", ".join(m_geo))
    (fail if m_anim else ok)(f"{verifiables - len(m_anim)}/{verifiables} créatures GeckoLib ont leur fichier d'animation", ", ".join(m_anim))
    (fail if m_tex else ok)(f"{verifiables - len(m_tex)}/{verifiables} créatures GeckoLib ont leur texture", ", ".join(m_tex))
    (fail if sans_renderer else ok)(f"{len(constantes) - len(sans_renderer)}/{len(constantes)} entités ont un renderer client",
                                    ", ".join(sans_renderer) + " — une entité sans renderer FAIT PLANTER le client" if sans_renderer else "")
    if mobiles:
        warn(f"{len(mobiles)} créatures GeckoLib non vérifiables statiquement sur leurs ressources",
             "; ".join(mobiles))
        couverture.append(f"{len(mobiles)} créatures GeckoLib non vérifiables (chaîne renderer→modèle)")


    # ---------------------------------------------------------- 3c. la recette de test
    # Un identifiant faux dans la recette de test envoie Donopot dans un mur : « Unknown item » en
    # plein test. On verifie donc que CHAQUE `voiddynasty:...` cite dans la recette existe vraiment.
    recette = os.path.join(racine, "docs/RECETTE-TEST.md")
    if os.path.exists(recette):
        print("\n   identifiants cités par la recette de test")
        texte = lire(recette)
        cites = sorted(set(re.findall(r"voiddynasty:([a-z0-9_]+)", texte)))
        connus = set()
        connus |= items                                     # armes, munitions, outils, oeufs, armures
        connus |= set(re.findall(r'"([a-z0-9_]+)"',
                                 lire(os.path.join(java, "registry/ModBlocks.java"))))
        connus |= set(re.findall(r'"([a-z0-9_]+)"',
                                 lire(os.path.join(java, "registry/ModEntities.java"))))
        inconnus = [c for c in cites if c not in connus]
        (fail if inconnus else ok)(
            f"{len(cites) - len(inconnus)}/{len(cites)} identifiants de la recette existent",
            "introuvables : " + ", ".join(inconnus))
    else:
        info("pas de docs/RECETTE-TEST.md : contrôle sauté")

    # ---------------------------------------------------------- 3a. états d'arme
    # Un recul ou un rechargement dont la variante est IDENTIQUE à l'arme au repos ne se verrait
    # jamais en jeu : le control doit donc exiger qu'elle DIFFERE, pas seulement qu'elle existe.
    print("\n   états des armes (recul, rechargement)")
    etats_absents, etats_identiques, armes_ok = [], [], 0
    for arme in ARMES_ANIMEES:
        p_normale = os.path.join(assets, "models/item", arme + ".json")
        if not os.path.exists(p_normale):
            etats_absents.append(f"{arme} (modèle de repos absent)")
            continue
        normale = lire(p_normale)
        souci_arme = False
        for suffixe in ("_recoil", "_reloading"):
            chemin = os.path.join(assets, "models/item", arme + suffixe + ".json")
            if not os.path.exists(chemin):
                etats_absents.append(f"{arme}{suffixe}")
                souci_arme = True
            elif lire(chemin) == normale:
                etats_identiques.append(f"{arme}{suffixe}")
                souci_arme = True
        if not souci_arme:
            armes_ok += 1
    (fail if etats_absents or etats_identiques else ok)(
        f"{armes_ok}/{len(ARMES_ANIMEES)} armes ont un recul ET un rechargement distincts",
        " · ".join(etats_absents + [f"{x} identique au repos" for x in etats_identiques]))

    # ---------------------------------------------------------- 3b. clips
    print("\n   clips d'animation demandés par chaque créature")
    clips_absents, clips_verifies, non_verifies = [], 0, []

    # chaîne exacte : classe d'entité → constante de registre → renderer → classe de modèle → fichier d'animation
    def animation_pour(classe_entite: str) -> str | None:
        base = re.sub(r'Entity$', '', classe_entite)
        const = next((c for c in constantes
                      if c.replace("_", "").lower() == base.replace("_", "").lower()), None)
        if not const or const not in renderers:
            return None
        return ressources.get(modele_pour(const) or "", {}).get("animation")

    sans_clip_litteral, fichier_introuvable = [], []
    for f in sorted(glob.glob(os.path.join(java, "entity/*Entity.java"))):
        t = lire(f)
        nom_classe = os.path.basename(f)[: -len(".java")]
        demandes = set(re.findall(r'then(?:Loop|Play)\("([^"]+)"\)', t))
        # Les clips RENDUS par clipIdle()/clipWalk()/clipAttack()/… sont des litteraux consommes
        # plus loin par une variable : le motif ci-dessus ne les voit pas. C'est par ce trou que
        # les cultistes 2, 3, 4 et la Pretresse ont pu demander un clip « idle » qui n'existait
        # dans aucun de leurs fichiers — sans qu'aucun controle ne bronche.
        rendus = set(re.findall(r'protected String clip\w+\(\)\s*\{\s*return\s*"([^"]+)"', t))
        demandes |= rendus
        if not demandes:
            sans_clip_litteral.append(nom_classe)
            continue
        fichier = animation_pour(nom_classe)
        p = os.path.join(assets, "geckolib/animations", (fichier or "") + ".animation.json")
        if not fichier or not os.path.exists(p):
            fichier_introuvable.append(nom_classe)
            continue
        dispo = set(json.loads(lire(p)).get("animations", {}).keys())
        for c in sorted(demandes):
            clips_verifies += 1
            if c not in dispo:
                clips_absents.append(f"{nom_classe} demande « {c} » ; {fichier}.animation.json contient {sorted(dispo)}")
    (fail if clips_absents else ok)(
        f"{clips_verifies - len(clips_absents)}/{clips_verifies} clips demandés existent", " · ".join(clips_absents))
    if fichier_introuvable:
        warn(f"{len(fichier_introuvable)} créatures dont le fichier d'animation n'a pas pu être relié",
             ", ".join(fichier_introuvable))
    if sans_clip_litteral:
        info(f"{len(sans_clip_litteral)} entités sans clip littéral dans le code "
             f"(animation pilotée dynamiquement) : {', '.join(sans_clip_litteral)}")
        couverture.append(f"{len(sans_clip_litteral)} entités sans clip littéral (non vérifiables)")

    # ---------------------------------------------------------- 4. langues
    print("\n4. Langues")
    langues = {l: (json.loads(lire(os.path.join(assets, f"lang/{l}.json")))
                   if os.path.exists(os.path.join(assets, f"lang/{l}.json")) else {})
               for l in ("en_us", "fr_fr")}
    if not all(langues.values()):
        fail("les deux fichiers de langue existent", ", ".join(l for l, v in langues.items() if not v))
    else:
        for prefixe, ids, quoi in (("item", tous_items, "objets"), ("block", blocs, "blocs")):
            for l, d in langues.items():
                # un OBJET DE BLOC est nommé par `block.<ns>.<id>` : le jeu ne consulte
                # `item.<ns>.<id>` que si la clé `block.` est absente. Exiger la clé `item.` pour un
                # identifiant de bloc produisait 20 faux « non traduits » sur les props de la phase C.
                manque = sorted(i for i in ids
                                if f"{prefixe}.{NS}.{i}" not in d
                                and not (prefixe == "item" and f"block.{NS}.{i}" in d))
                (fail if manque else ok)(f"{l} : {len(ids) - len(manque)}/{len(ids)} {quoi} traduits",
                                         ", ".join(manque[:10]))
        mobs = [e for e in entites if e not in ("bolt_projectile", "laser_beam")]
        for l, d in langues.items():
            manque = sorted(e for e in mobs if f"entity.{NS}.{e}" not in d)
            (warn if manque else ok)(f"{l} : {len(mobs) - len(manque)}/{len(mobs)} créatures nommées", ", ".join(manque))
        en, fr = set(langues["en_us"]), set(langues["fr_fr"])
        (warn if en != fr else ok)(
            f"les deux langues ont les mêmes {len(en)} clés" if en == fr
            else "les deux langues diffèrent",
            f"seulement en_us : {sorted(en - fr)[:5]} · seulement en fr_fr : {sorted(fr - en)[:5]}")

    # ---------------------------------------------------------- 5. recettes
    print("\n5. Recettes")
    recettes = sorted(glob.glob(os.path.join(data, "recipe/*.json")))
    cibles, cassees = set(), []
    for p in recettes:
        try:
            d = json.loads(lire(p))
        except Exception as e:  # noqa: BLE001
            cassees.append(f"{os.path.basename(p)} illisible ({e})")
            continue
        res = d.get("result")
        if isinstance(res, dict):
            cibles.add(str(res.get("id", "")).split(":")[-1])
        for ing in (d.get("ingredients") or []) + list((d.get("key") or {}).values()):
            if isinstance(ing, dict):
                cassees.append(f"{os.path.basename(p)} : ingrédient en objet au lieu de chaîne (26.3 refuse)")
                break
    (fail if cassees else ok)(f"{len(recettes) - len(cassees)}/{len(recettes)} recettes valides", "; ".join(cassees[:5]))
    inconnues = sorted(c for c in cibles if c not in (tous_items | blocs))
    (warn if inconnues else ok)("chaque recette produit un objet déclaré" if not inconnues else "recettes visant un objet non déclaré",
                                ", ".join(inconnues))
    info(f"{len(cibles)} objets produits par les recettes")

    # ---------------------------------------------------------- 6. sons
    print("\n6. Sons")
    p_sounds = os.path.join(assets, "sounds.json")
    if os.path.exists(p_sounds):
        d = json.loads(lire(p_sounds))
        refs = [s if isinstance(s, str) else s.get("name", "")
                for entree in d.values() for s in (entree.get("sounds") or [])]
        absents = [s for s in refs if not os.path.exists(
            os.path.join(assets, "sounds", s.split(":", 1)[-1] + ".ogg"))]
        (fail if absents else ok)(f"{len(refs) - len(absents)}/{len(refs)} sons présents", ", ".join(absents))
    else:
        warn("sounds.json absent")

    # ---------------------------------------------------------- 7. textures
    print("\n7. Textures — dimensions, alpha, suspects")
    pngs = glob.glob(os.path.join(assets, "textures/**/*.png"), recursive=True)
    mauvaises, sans_alpha, plates = [], [], []
    for p in pngs:
        rel = os.path.relpath(p, assets)
        t = png_taille(p)
        if not t:
            mauvaises.append(rel + " (PNG illisible)")
            continue
        larg, haut, couleur = t
        if larg % 16 or haut % 16:
            mauvaises.append(f"{rel} ({larg}×{haut}, pas multiple de 16)")
        # l'alpha n'est exigé que là où il compte : icônes d'objets, interface, particules
        if couleur != 6 and any(x in rel for x in ("textures/item/", "textures/gui/", "textures/particle/")):
            sans_alpha.append(f"{rel} (type {couleur})")
        if os.path.getsize(p) < 250:
            plates.append(f"{rel} ({os.path.getsize(p)} o)")
    (fail if mauvaises else ok)(f"{len(pngs) - len(mauvaises)}/{len(pngs)} textures en dimensions multiples de 16",
                                "; ".join(mauvaises[:8]))
    (fail if sans_alpha else ok)(
        f"icônes d'objets avec canal alpha ({len(sans_alpha) and 'anomalies' or 'aucune anomalie'})"
        if sans_alpha else "toutes les icônes d'objets ont un canal alpha", ", ".join(sans_alpha))
    if plates:
        # ATTENTION : la taille d'un fichier PNG ne dit RIEN de la qualité d'un sprite. Un 16×16 net
        # et bien dessiné pèse légitimement 100 à 250 octets ; un aplat uniforme peut peser 600.
        # Ce contrôle a été écrit en croyant l'inverse, et une planche avant/après a prouvé le
        # contraire : les « petites » textures étaient des sprites propres, et les remplacer par des
        # textures HD réduites les a enlaidis. D'où : information, jamais avertissement.
        info(f"{len(plates)} textures sous 250 octets (sur {len(pngs)}) — ce n'est pas un défaut : "
             f"un sprite 16×16 net est petit. À juger à l'œil, pas au poids du fichier "
             f"(tools/planche_textures.py fabrique une planche agrandie)")
    else:
        ok(f"{len(pngs)}/{len(pngs)} textures au-dessus de 250 octets")

    # ---------------------------------------------------------- 7bis. couverture UV des modèles
    # TROUVÉ PAR ÉPREUVE NÉGATIVE, pas par relecture. Un modèle dont les UV ne couvrent que 2 % de la
    # texture s'affiche quand même : il échantillonne un coin uni et paraît simplement mal texturé.
    # C'est arrivé en portant le HellDrake, où le facteur de remise à l'échelle des UV avait été
    # INVERSÉ (16/512 au lieu de 512/16). Le premier contrôle écrit ici ne testait que « UV > taille
    # de texture » et laissait donc passer l'erreur : on teste maintenant la COUVERTURE.
    print("\n7bis. Couverture des UV (les UV ne peuvent pas être des fractions)")
    uv_faibles = []
    for chemin in sorted(glob.glob(os.path.join(assets, "geckolib/models/*.geo.json"))):
        try:
            geo = json.load(open(chemin, encoding="utf-8"))["minecraft:geometry"][0]
        except (json.JSONDecodeError, KeyError, IndexError) as e:
            uv_faibles.append(f"{os.path.basename(chemin)} (illisible : {e})")
            continue
        desc = geo.get("description", {})
        tw = desc.get("texture_width", 64)
        th = desc.get("texture_height", 64)
        maxi = 0.0
        for os_ in geo.get("bones", []):
            for cube in os_.get("cubes", []):
                uv = cube.get("uv")
                if isinstance(uv, dict):            # UV par face ; la face peut être une liste
                    for face in uv.values():        # [u1,v1,u2,v2] OU un objet {uv: [...], texture:}
                        if isinstance(face, dict):
                            face = face.get("uv", [])
                        maxi = max(maxi, *[float(x) for x in face])
                elif isinstance(uv, list):          # UV de boîte : [u, v]
                    maxi = max(maxi, *[float(x) for x in uv])
        taux = maxi / max(tw, th) if max(tw, th) else 0.0     # nom distinct : `couverture` est déjà
        if taux < 0.5:                                        # une liste ailleurs dans ce fichier
            uv_faibles.append(f"{os.path.basename(chemin)} (couvre {taux:.0%} de la texture)")
    (fail if uv_faibles else ok)(
        f"{len(uv_faibles)} modèle(s) avec des UV trop serrés" if uv_faibles
        else "tous les modèles couvrent au moins la moitié de leur texture",
        "; ".join(uv_faibles[:6]))

    # ---------------------------------------------------------- 8. orphelins
    print("\n8. Fichiers non déclarés")
    orph_items = sorted(i for i in defs_items if i not in tous_items)
    (warn if orph_items else ok)(f"{len(orph_items)} fichiers items/ sans objet déclaré" if orph_items
                                 else "aucun fichier items/ orphelin", ", ".join(orph_items))
    orph_mods = sorted(m for m in mods_items if m not in tous_items)
    if orph_mods:
        info(f"{len(orph_mods)} modèles d'objets non déclarés (résidus ou sous-modèles) : {', '.join(orph_mods[:8])}")
    orph_blocs = sorted(b for b in mods_blocs if b not in blocs)
    if orph_blocs:
        info(f"{len(orph_blocs)} modèles de blocs non déclarés : {', '.join(orph_blocs[:8])}")

    # ---------------------------------------------------------- 9. onglet créatif
    print("\n9. Onglet créatif")
    p_groupes = os.path.join(java, "registry/ModItemGroups.java")
    if os.path.exists(p_groupes):
        t = lire(p_groupes)
        # les trois formes possibles : ModItems.X, ModBlocks.X_ITEM, ModArmor.X (toutes à lire,
        # sinon on déclare « absent » un objet simplement cité autrement)
        cites = set(re.findall(r'ModItems\.([A-Z0-9_]+)', t))
        cites |= set(re.findall(r'ModBlocks\.([A-Z0-9_]+)_ITEM', t))
        cites |= {c for c in re.findall(r'ModArmor\.([A-Z0-9_]+)', t)}
        # correspondance constante -> identifiant, pour les trois registres
        c2id = dict(re.findall(r'Item\s+([A-Z0-9_]+)\s*=\s*(?:ammo|weapon|tool|spawnEgg|promethiumFuel)\("([a-z0-9_]+)"',
                               lire(os.path.join(java, "registry/ModItems.java"))))
        c2id |= dict(re.findall(r'Item\s+([A-Z0-9_]+)\s*=\s*armor\("([a-z0-9_]+)"',
                                lire(os.path.join(java, "registry/ModArmor.java"))))
        c2id |= dict(re.findall(r'Item\s+([A-Z0-9_]+)_ITEM\s*=\s*blockItem\("([a-z0-9_]+)"',
                                lire(os.path.join(java, "registry/ModBlocks.java"))))
        absents = sorted(c2id[c] for c in c2id if c not in cites)
        (warn if absents else ok)(
            f"{len(c2id) - len(absents)}/{len(c2id)} objets atteignables en créatif" if absents
            else f"{len(c2id)}/{len(c2id)} objets atteignables dans l'onglet créatif",
            ", ".join(absents) + " (enregistrés et craftables, mais introuvables en créatif)" if absents else "")
    else:
        couverture.append("aucun ModItemGroups.java trouvé : atteignabilité créatif non vérifiée")

    # ---------------------------------------------------------- 10. armures (équipements)
    print("\n10. Définitions d'armure")
    eqs = sorted(glob.glob(os.path.join(assets, "equipment/*.json")))
    eq_casses = []
    for p in eqs:
        try:
            d = json.loads(lire(p))
        except Exception as e:  # noqa: BLE001
            eq_casses.append(f"{os.path.basename(p)} illisible ({e})")
            continue
        for couche, entrees in (d.get("layers") or {}).items():
            for e in entrees:
                ref = e.get("texture", "")
                if not ref:
                    continue
                chemin = os.path.join(assets, "textures/entity/equipment", couche,
                                      ref.split(":")[-1] + ".png")
                if not os.path.exists(chemin):
                    eq_casses.append(f"{os.path.basename(p)} : {couche}/{ref.split(':')[-1]}.png absent")
    (fail if eq_casses else ok)(
        f"{len(eqs)} définitions d'armure, toutes leurs textures présentes" if eqs and not eq_casses
        else f"{len(eqs)} définitions d'armure, {len(eq_casses)} référence(s) cassée(s)" if eqs
        else "aucune définition d'armure", "; ".join(eq_casses))

    # ------------------------------------------------- 11. définitions d'objets (26.3)
    # Un modèle d'objet mal formé ne plante PAS : le jeu retombe silencieusement sur le modèle
    # « manquant ». D'où ce contrôle statique de la chaîne d'animation des armes (phase D) :
    # condition sur la présence du composant de rechargement, puis du recul, puis modèle de base.
    print("\n11. Définitions d'objets — chaîne d'animation des armes")
    dossier_items = os.path.join(assets, "items")
    armes = [f[:-5] for f in sorted(os.listdir(dossier_items))
             if f.endswith(".json") and f[:-5] in ARMES_ANIMEES] if os.path.isdir(dossier_items) else []
    if not armes:
        couverture.append("aucune définition d'objet d'arme trouvée : contrôle non exécuté")
        info("aucune arme animée à vérifier")
    else:
        casses = []
        for a in armes:
            try:
                d = json.loads(lire(os.path.join(dossier_items, f"{a}.json")))
            except Exception as e:                                    # noqa: BLE001
                casses.append(f"{a} : JSON illisible ({e})")
                continue
            m = d.get("model", {})
            if m.get("type") != "minecraft:condition":
                casses.append(f"{a} : type {m.get('type')} au lieu de minecraft:condition")
                continue
            if m.get("component") != f"{NS}:reloading":
                casses.append(f"{a} : composant racine {m.get('component')}")
            if "property" in m:
                casses.append(f"{a} : clé « property » en trop (la forme vanilla 26.3 est « component »)")
            recul = m.get("on_false", {})
            if recul.get("component") != f"{NS}:recoil":
                casses.append(f"{a} : branche recul absente")
            for ref in (m.get("on_true", {}).get("model"), recul.get("on_true", {}).get("model"),
                        recul.get("on_false", {}).get("model")):
                if not isinstance(ref, str):
                    casses.append(f"{a} : référence de modèle absente dans une branche")
                    continue
                if not os.path.exists(os.path.join(assets, "models/item", f"{ref.split('/')[-1]}.json")):
                    casses.append(f"{a} : modèle référencé introuvable ({ref})")
        (fail if casses else ok)(
            f"{len(armes)} armes : chaîne rechargement → recul → base, modèles présents"
            if not casses else f"{len(casses)} problème(s) dans les définitions d'objets",
            "; ".join(casses[:8]))
        if len(armes) != len(ARMES_ANIMEES):
            couverture.append(f"{len(armes)} définitions d'objets vérifiées sur {len(ARMES_ANIMEES)} armes attendues")

    # ------------------------------------ 12. clips déclarés par le code d'entité
    # Le contrôle le plus utile de cette famille : un clip écrit dans le code mais ABSENT du fichier
    # d'animation ne produit AUCUNE erreur — GeckoLib journalise « unable to find animation » et la
    # créature attaque sans animation, indéfiniment. Vécu : après une reconversion, les clips des
    # cultistes ont été renommés (attack1 → attack, le tir devenant shoot) et le code ne suivait plus.
    print("\n12. Clips d'animation déclarés dans le code")
    dossier_java = os.path.join(racine, "src/main/java/com/donopot/voiddynasty")
    modeles_client = glob.glob(os.path.join(dossier_java, "client/*Model.java"))
    paires, incoherences = [], []
    for chemin_modele in sorted(modeles_client):
        src = lire(chemin_modele)
        m_id = re.search(r'getModelResource[^}]*?fromNamespaceAndPath\(\s*"voiddynasty"\s*,\s*"([a-z0-9_]+)"', src, re.S)
        m_ent = re.search(r"GeoModel<([A-Za-z0-9_]+)>", src)
        if not (m_id and m_ent):
            continue
        anim_path = os.path.join(assets, "geckolib/animations", f"{m_id.group(1)}.animation.json")
        if not os.path.exists(anim_path):
            infer_a = os.path.join(dossier_java, "entity", f"{m_ent.group(1)}.java")
            paires.append((m_id.group(1), m_ent.group(1), set(), None))
            continue
        dispo = set(json.loads(lire(anim_path)).get("animations", {}))
        ent = os.path.join(dossier_java, "entity", f"{m_ent.group(1)}.java")
        demandes = set()
        if os.path.exists(ent):
            # uniquement les retours des méthodes clip*() : ailleurs, des chaînes comme "walk"
            # apparaîtraient pour de mauvaises raisons.
            for corps in re.findall(r"protected String clip[A-Za-z]*\(\)\s*\{(.*?)\n    \}", lire(ent), re.S):
                demandes |= set(re.findall(r'return\s+"([^"]+)"', corps))
        paires.append((m_id.group(1), m_ent.group(1), demandes, dispo))
        manquants = sorted(demandes - dispo)
        if manquants:
            incoherences.append(f"{m_ent.group(1)} : clips absents {manquants} (disponibles {sorted(dispo)})")
    if not paires:
        couverture.append("aucun modèle client analysable : contrôle des clips non exécuté")
        info("aucun modèle client à vérifier")
    else:
        (fail if incoherences else ok)(
            f"{len(paires)} entités : tous les clips déclarés existent"
            if not incoherences else f"{len(incoherences)} entité(s) réclament un clip inexistant",
            "; ".join(incoherences[:6]))

    # ---------------------------------------------------------- bilan
    print("\n" + "=" * 78)
    print(f"bilan : {len(bloquants)} anomalie(s) bloquante(s) · {len(avertissements)} avertissement(s)")
    for b in bloquants:
        print(f"  {ROUGE}✗{FIN} {b}")
    for a in avertissements:
        print(f"  {JAUNE}!{FIN} {a}")
    if couverture:
        print("couverture (ce que ce contrôle n'a pas pu vérifier) :")
        for c in couverture:
            print(f"  {GRIS}·{FIN} {c}")
    if bloquants:
        return 1
    print(f"{VERT}assets complets sur tous les contrôles bloquants.{FIN}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
