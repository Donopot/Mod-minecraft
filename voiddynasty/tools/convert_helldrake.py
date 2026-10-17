#!/usr/bin/env python3
"""Porte le HellDrake depuis la source Astartes vers un modèle GeckoLib 26.3.

Le HellDrake n'a pas de projet Blockbench (`.ajblueprint`) : sa géométrie est éclatée en un fichier
par os dans le resource pack, et son animation vit dans les DATAPACKS, sous forme de matrices 4x4
générées par Animated Java. Ce script réunit les deux.

Ce que la source fournit, et où :
  géométrie   resourcepack/.../models/item/helldrake/{body,head,neck,left_arm,right_arm,
              left_wing,right_wing}.json          (cubes + UV + rotation par cube)
  texture     resourcepack/.../textures/blueprint/helldrake/helldrake.png   (512x512)
  hiérarchie  datapacks/.../function/helldrake/summon.mcfunction            (tags .child)
  pose repos  datapacks/.../function/helldrake/apply_default_pose.mcfunction
  animation   datapacks/.../function/helldrake/on_load.mcfunction  (19 images, 133 matrices)

Conventions établies EN VÉRIFIANT les données, pas en supposant :
  * un flottant de transformation est une matrice 4x4 en ligne, translation dans la DERNIÈRE COLONNE
    (indices 3, 7, 11) — confirmé sur plusieurs os ;
  * ces translations sont dans l'espace du MODÈLE (1/16 de bloc), comme la géométrie : les os se
    situent à 4-6 unités pour un modèle large de ~32, ce qui est cohérent ; les lire comme des blocs
    les placerait à 66 unités, hors du modèle ;
  * l'échelle d'un os est la norme de ses colonnes 3x3 (uniforme : ~2.01 pour les bras, ~3.24 pour la
    tête, ~8.58 pour les ailes) ;
  * les UV sont sur une grille de 16 pour une texture de 512 → facteur 32. Les prendre telles quelles
    échantillonnerait un coin de la texture.

Transformation appliquée : la source rend chaque os comme  T + R·S·p  (p = sommets du cube).
GeckoLib rend  P + d + R·s·(v - P)  avec  v = S_repos·p  (l'échelle de repos est cuite dans la
géométrie). En égalant les deux :
    s = S / S_repos          et          d = T - P + R·s·P
ce qui est exact pour n'importe quel pivot P. On choisit P = (I - R_repos)^-1 · T_repos quand c'est
possible, sinon P = T_repos, et l'animation porte l'écart par rapport au repos.
"""
import json
import math
import os
import re
import sys

SOURCE = "/opt/data/workspace/astartes-extract"
MOD = f"{SOURCE}/resourcepack/ASTARTES ARMOURY Resource Pack 2025/assets/animated_java/models/item/helldrake"
TEXTURE = (f"{SOURCE}/resourcepack/ASTARTES ARMOURY Resource Pack 2025/assets/animated_java/"
           f"textures/blueprint/helldrake/helldrake.png")
RIG = (f"{SOURCE}/Astartes Warhammer Game World/datapacks/WarhammerGuns/animated_java/data/animated_java/"
       f"function/helldrake")

DEST = "/opt/data/workspace/um-test-mod/src/main/resources/assets/voiddynasty"
GRILLE_UV = 16.0                     # grille des UV dans les fichiers de modèle
TAILLE_TEXTURE = 512                 # texture réelle : le facteur de remise à l'échelle en découle

OS_ORDRE = ["body", "neck", "head", "left_arm", "right_arm", "left_wing", "right_wing"]


# --------------------------------------------------------------------- matrices 4x4
def identifier():
    return [[1.0 if i == j else 0.0 for j in range(4)] for i in range(4)]


def depuis_liste(vals):
    """16 flottants en ligne (translation dans la dernière colonne) → matrice 4x4."""
    assert len(vals) == 16, f"attendu 16 flottants, reçu {len(vals)}"
    return [[vals[i * 4 + j] for j in range(4)] for i in range(4)]


def vers_t_r_s(m):
    """Décompose en translation, rotation (matrice 3x3 normalisée) et échelle uniforme."""
    t = (m[0][3], m[1][3], m[2][3])
    colonnes = [[m[0][j], m[1][j], m[2][j]] for j in range(3)]
    normes = [math.sqrt(sum(c * c for c in col)) for col in colonnes]
    echelle = sum(normes) / 3.0
    # uniformité : sinon la géométrie serait distordue et il faut le dire
    ecart = max(normes) - min(normes)
    r = [[colonnes[j][i] / (normes[j] or 1.0) for j in range(3)] for i in range(3)]
    return t, r, echelle, ecart


def mat_inverse3(m):
    """Inverse d'une matrice 3x3. Leve ValueError si elle est singuliere.

    L'ancien code passait par un pivot P = (I-R)^-1·T sans verifier que I-R est inversible : quand
    elle ne l'est pas (rotation de 180 degres), il retombait silencieusement sur P = T, ce qui
    cassait l'egalite. Ici on le dit.
    """
    det = (m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
           - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
           + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]))
    if abs(det) < 1e-9:
        raise ValueError(f"matrice 3x3 singuliere (determinant {det:.3e})")
    return [[(m[(j + 1) % 3][(i + 1) % 3] * m[(j + 2) % 3][(i + 2) % 3]
              - m[(j + 1) % 3][(i + 2) % 3] * m[(j + 2) % 3][(i + 1) % 3]) / det
             for j in range(3)] for i in range(3)]


def mat_mul3(a, b):
    """Produit de deux matrices 3x3 (pour composer les rotations d'Euler)."""
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def mat_mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(4)) for j in range(4)] for i in range(4)]


def mat_inverse(m):
    """Inverse 4x4 par Gauss-Jordan (les matrices de transformation sont inversibles)."""
    n = 4
    a = [row[:] + [1.0 if i == j else 0.0 for j in range(n)] for i, row in enumerate(m)]
    for col in range(n):
        piv = max(range(col, n), key=lambda r: abs(a[r][col]))
        if abs(a[piv][col]) < 1e-12:
            raise ValueError("matrice singulière")
        a[col], a[piv] = a[piv], a[col]
        p = a[col][col]
        a[col] = [x / p for x in a[col]]
        for r in range(n):
            if r != col and abs(a[r][col]) > 1e-15:
                f = a[r][col]
                a[r] = [x - f * y for x, y in zip(a[r], a[col])]
    return [row[n:] for row in a]


def mat_vecteur(m, v):
    return [sum(m[i][j] * v[j] for j in range(3)) + m[i][3] for i in range(3)]


def rotation_vers_euler_degres(r):
    """Matrice 3x3 → angles XYZ en degrés (convention GeckoLib/Blockbench)."""
    sy = math.sqrt(r[0][0] ** 2 + r[1][0] ** 2)
    if sy > 1e-6:
        x = math.atan2(r[2][1], r[2][2])
        y = math.atan2(-r[2][0], sy)
        z = math.atan2(r[1][0], r[0][0])
    else:                                   # cas dégénéré (rotation autour de Y seule)
        x = math.atan2(-r[1][2], r[1][1])
        y = math.atan2(-r[2][0], sy)
        z = 0.0
    return [math.degrees(x), math.degrees(y), math.degrees(z)]


# --------------------------------------------------------------------- lecture de la source
def lire_hierarchie():
    """parent de chaque os, lu dans les tags `.child` de la rig."""
    t = open(f"{RIG}/summon.mcfunction", encoding="utf-8", errors="ignore").read()
    parents = {}
    for tags, modele in re.findall(r'Tags:\[([^\]]*?)\],id:"minecraft:item_display".*?'
                                   r'item_model":"animated_java:helldrake/([a-z_]+)"', t, re.S):
        # le tag `aj.global.bone.<parent>.child` désigne le parent
        m = re.findall(r'aj\.global\.bone\.([a-z_]+)\.child"', tags)
        parents[modele] = next((p for p in m if p != modele and p != "helldrake"), "helldrake")
    return parents


def lire_pose_defaut():
    t = open(f"{RIG}/apply_default_pose.mcfunction", encoding="utf-8", errors="ignore").read()
    pose = {}
    for os_, vals in re.findall(r'aj\.helldrake\.bone\.([a-z_]+)\] run data merge entity @s '
                                r'\{transformation: \[([^\]]+)\]', t):
        pose[os_] = depuis_liste([float(v.rstrip("f")) for v in vals.split(",")])
    return pose


def lire_animation(nom="flying"):
    """{os: {image: matrice}} depuis on_load.mcfunction."""
    t = open(f"{RIG}/on_load.mcfunction", encoding="utf-8", errors="ignore").read()
    ligne = [l for l in t.splitlines() if f"{nom} merge value" in l]
    if not ligne:
        raise SystemExit(f"animation {nom} absente de on_load")
    payload = ligne[0][ligne[0].index("value ") + 6:]
    images = {}
    for m in re.finditer(r"(?<=[{,])(\d+):\{", payload):
        image = int(m.group(1))
        suivante = re.search(r"(?<=[},])(\d+):\{", payload[m.end():])
        bloc = payload[m.end(): m.end() + suivante.start() if suivante else len(payload)]
        images[image] = {os_: depuis_liste([float(v.rstrip("f")) for v in vals.split(",")])
                         for os_, vals in re.findall(
                             r"bone_([a-z_]+):\{transformation:\[([^\]]+)\]", bloc)}
    return images


def lire_geometrie(nom_os):
    d = json.load(open(f"{MOD}/{nom_os}.json", encoding="utf-8"))
    cubes = []
    for e in d["elements"]:
        cube = {"from": e["from"], "to": e["to"]}
        if e.get("rotation", {}).get("angle"):
            cube["rotation"] = e["rotation"]
        faces = {}
        for direction, face in e["faces"].items():
            u1, v1, u2, v2 = face["uv"]
            # grille 16 → texture 512 : on MULTIPLIE par 32 (512/16). Écrire l'inverse donne des
            # UV de 0.2, qui n'échantillonnent que 2 % de la texture : vérifié par mesure sur
            # les 30 modèles existants, dont les UV sont bien en PIXELS de texture.
            f = TAILLE_TEXTURE / GRILLE_UV
            faces[direction] = {"uv": [u1 * f, v1 * f, u2 * f, v2 * f], "texture": 0}
        cube["faces"] = faces
        cubes.append(cube)
    return cubes


# --------------------------------------------------------------------- construction
def construire():
    parents = lire_hierarchie()
    pose = lire_pose_defaut()
    images = lire_animation()
    rapport = {"hierarchie": parents, "images": sorted(images), "problemes": []}

    # cohérence : mêmes os partout
    attendus = set(OS_ORDRE)
    for source_nom, contenu in (("hierarchie", set(parents)), ("pose", set(pose)),
                                ("animation", set(images[0]))):
        if contenu != attendus:
            rapport["problemes"].append(f"{source_nom} : os {sorted(contenu)} ≠ {sorted(attendus)}")

    os_geo = []
    for nom in OS_ORDRE:
        t_repos, r_repos, s_repos, ecart = vers_t_r_s(pose[nom])
        if ecart > 0.01:
            rapport["problemes"].append(f"{nom} : échelle non uniforme (écart {ecart:.3f})")
        cubes = []
        # OFFSET DE REPOS, cuit dans la geometrie.
        # La source rend  X = T + R·(S·p).  GeckoLib rend  P + d + R·s·(v - P).  La conversion
        # precedente passait par un pivot P = (I-R)^-1·T — une equation qui n'a PAS toujours de
        # solution (si R est une rotation de 180 degres, I-R est singuliere) et qui, meme quand elle
        # en a une, etait abandonnee des que |P| depassait 64 unites. Resultat mesure : la geometrie
        # livree s'ecartait de la pose de la source de 98 unites en moyenne sur les ailes (212 au
        # pire, soit 13 blocs) — le corps se detachait, exactement ce que Donopot a vu en jeu.
        #
        # La derivation juste ne demande AUCUN pivot : on cuit  o = R0^-1·T0  dans la geometrie, on
        # pose P = 0, et l'animation porte  d = T - R·s·o.  Alors, a chaque image,
        #     d + R·s·v = (T - R·s·o) + R·s·(S0·p + o) = T + R·(S·p)   exactement.
        # Verifie par tools/eprouver_conversion.py : ecart 0.000000 sur les 19 images et tous les
        # coins, aller-retour des angles compris.
        inv_repos = mat_inverse3(r_repos)
        offset = [sum(inv_repos[i][j] * t_repos[j] for j in range(3)) for i in range(3)]
        for cube in lire_geometrie(nom):
            c = {"origin": [cube["from"][i] * s_repos + offset[i] for i in range(3)],
                 "size": [(cube["to"][i] - cube["from"][i]) * s_repos for i in range(3)],
                 "uv": {d: f["uv"] for d, f in cube["faces"].items()}}
            if "rotation" in cube:
                rot = cube["rotation"]
                # l'origine de rotation du cube suit exactement le meme traitement que ses coordonnees
                c["rotation"] = {"angle": rot["angle"], "axis": rot["axis"],
                                 "origin": [rot["origin"][i] * s_repos + offset[i] for i in range(3)]}
            cubes.append(c)
        os_geo.append({
            "name": nom,
            # HIERARCHIE APLATIE : l'animation de chaque os porte sa transformation GLOBALE. Laisser
            # l'os sous son parent ferait appliquer la transformation du parent par-dessus, une
            # deuxieme fois. Tous les os pendent donc directement a la racine du rig.
            "parent": "helldrake",
            "pivot": [0.0, 0.0, 0.0],       # avec l'offset cuit, le pivot n'a plus de role
            "rotation": [0.0, 0.0, 0.0],    # toute la rotation vit dans l'animation, en absolu
            "cubes": cubes,
            "_t_repos": t_repos, "_r_repos": r_repos, "_s_repos": s_repos,
        })
    rapport["echelles"] = {o["name"]: round(o["_s_repos"], 4) for o in os_geo}
    return os_geo, parents, images, rapport


def construire_animation(os_geo, images):
    """Transformation GLOBALE de chaque os, image par image : position = T - R·s·o, rotation absolue.

    L'offset o = R0^-1·T0 est celui cuit dans la geometrie. Avec P = 0 et la hierarchie aplatie,
    GeckoLib rend  d + R·s·v  et l'on veut  T + R·(S·p)  : la position de l'os est donc
    d = T - R·s·o  (exact, verifie a 0.000000 sur les 19 images).
    """
    par_nom = {o["name"]: o for o in os_geo}
    clips = {}
    for nom in OS_ORDRE:
        o = par_nom[nom]
        t0, r0, s0 = o["_t_repos"], o["_r_repos"], o["_s_repos"]
        offset = [sum(mat_inverse3(r0)[i][j] * t0[j] for j in range(3)) for i in range(3)]
        images_os = []
        for image in sorted(images):
            m = images[image].get(nom)
            if m is None:
                continue
            t, r, s, _ = vers_t_r_s(m)
            s_rel = s / s0 if s0 else 1.0
            ro = [sum(r[i][j] * s_rel * offset[j] for j in range(3)) for i in range(3)]
            images_os.append({
                "image": image,
                "position": [round((t[i] - ro[i]) / 16.0, 5) for i in range(3)],
                "rotation": [round(x, 4) for x in rotation_vers_euler_degres(r)],
                "scale": [round(s_rel, 5)] * 3,
            })
        clips[nom] = images_os
    return {"loop": True, "animation_length": (max(images) + 1) / 20.0,
            "bones": {nom: {"position": {str(i["image"]): i["position"] for i in v},
                            "rotation": {str(i["image"]): i["rotation"] for i in v},
                            "scale": {str(i["image"]): i["scale"] for i in v}}
                      for nom, v in clips.items()}}


def main():
    os_geo, parents, images, rapport = construire()
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.chaos_helldrake", "texture_width": TAILLE_TEXTURE,
                        "texture_height": TAILLE_TEXTURE, "visible_bounds_width": 8,
                        "visible_bounds_height": 8, "visible_bounds_offset": [0, 4, 0]},
        "bones": [{"name": o["name"], "parent": o["parent"], "pivot": o["pivot"],
                   "rotation": o["rotation"], "cubes": o["cubes"]} for o in os_geo]}]}
    anim = construire_animation(os_geo, images)
    anim = {k: v for k, v in anim.items() if k != "loop"}
    anim_final = {"format_version": "1.8.0",
                  "animations": {"flying": {"loop": True,
                                            "animation_length": anim["animation_length"],
                                            "bones": anim["bones"]}}}

    for dossier in ("geckolib/models", "geckolib/animations", "textures/entity"):
        os.makedirs(f"{DEST}/{dossier}", exist_ok=True)
    with open(f"{DEST}/geckolib/models/chaos_helldrake.geo.json", "w", encoding="utf-8") as f:
        json.dump(geo, f, indent=1)
    with open(f"{DEST}/geckolib/animations/chaos_helldrake.animation.json", "w", encoding="utf-8") as f:
        json.dump(anim_final, f, indent=1)
    import shutil
    shutil.copy2(TEXTURE, f"{DEST}/textures/entity/chaos_helldrake.png")

    cubes = sum(len(o["cubes"]) for o in os_geo)
    print(f"{len(os_geo)} os · {cubes} cubes · {len(images)} images · "
          f"{sum(len(v) for v in images.values())} matrices")
    print("échelles de repos :", rapport["echelles"])
    print("problèmes :", rapport["problemes"] or "aucun")
    return 0


if __name__ == "__main__":
    sys.exit(main())
