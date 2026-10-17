#!/usr/bin/env python3
"""Convertit un modele Java (Blockbench, `models/item/*.json`) en geometrie GeckoLib.

Pourquoi : les trois decors que la source n'a pas portes comme blocs — le Leman Russ et les deux
chasseurs — existent bel et bien en modele (33 et 25 cubes, avec leurs textures). Ils ont ete ecartes
comme DECORS parce qu'ils font 2 a 3 blocs de haut : leur place est une ENTITE. Donopot a demande de
les importer en entite utilisable, priorite au Leman Russ.

Conventions, a eprouver au rendu et non supposees :
  * un element Java = un cube (from/to en 1/16 de bloc, comme les unites de modele GeckoLib) ;
  * l'espace Java a Y vers le BAS, l'espace Bedrock a Y vers le HAUT  -> --y-haut (defaut) reflechit ;
  * l'axe Z est inverse entre les deux espaces                        -> --z-avant ;
  * les UV Java sont sur une grille de 16 quelle que soit la texture  -> facteur texture/16 ;
  * une rotation Java = UN axe + un angle + une origine ; Bedrock veut un triplet d'Euler + un pivot.

    python3 tools/convert_modele_java.py --modele lemunruss --nom-geo lemunruss
    python3 tools/convert_modele_java.py --modele lemunruss --sans-y-haut --sans-z-avant   (essai)
"""
from __future__ import annotations

import argparse
import json
import os
import shutil
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE = "/opt/data/workspace/astartes-extract/resourcepack/ASTARTES ARMOURY Resource Pack 2025/assets/minecraft"
DEST = os.path.join(RACINE, "src/main/resources/assets/voiddynasty")

# Java -> Bedrock : l'ordre des faces change de nom et/ou de sens selon la convention des deux formats.
# Ces tables disent, pour chaque face Bedrock, quelle face Java la remplit et si la tuile est retournee.
FACE_JAVA = {
    "north": ("south", False),   # Bedrock « north » regarde vers -Z, comme le « south » de Java
    "south": ("north", False),
    "east": ("east", True),
    "west": ("west", True),
    "up": ("up", False),
    "down": ("down", False),
}


def uv_bedrock(uv_java, taille_texture, retourne):
    """Rectangle UV Java (grille de 16) -> rectangle UV Bedrock (pixels de texture).

    Java et Bedrock ne decrivent pas la meme origine de tuile : Bedrock part du coin haut-gauche vu de
    l'exterieur, Y vers le haut. On convertit explicitement au lieu d'esperer que ca tombe juste.
    """
    u1, v1, u2, v2 = uv_java
    facteur = taille_texture / 16.0
    u1, v1, u2, v2 = (min(u1, u2) * facteur, min(v1, v2) * facteur,
                      max(u1, u2) * facteur, max(v1, v2) * facteur)
    if retourne:
        return [u2, v1, u1, v2]
    return [u1, v1, u2, v2]


def main() -> int:
    p = argparse.ArgumentParser(description="Modele Java -> geometrie GeckoLib.")
    p.add_argument("--modele", required=True, help="nom du fichier dans models/item/")
    p.add_argument("--nom-geo", help="identifiant de la geometrie (defaut : le nom du modele)")
    p.add_argument("--sans-y-haut", action="store_true", help="ne PAS reflechir Y")
    p.add_argument("--sans-z-avant", action="store_true", help="ne PAS inverser Z")
    p.add_argument("--texture-dest", help="chemin de la texture dans notre namespace")
    p.add_argument("--sans-rotations", action="store_true",
                   help="temoin : ecrire la geometrie sans aucune rotation de cube")
    p.add_argument("--sens-rotation", default="tel-quel",
                   choices=["tel-quel", "inverse", "inverse-x", "inverse-y", "inverse-z"],
                   help="sens des rotations : Java et Bedrock ne tournent pas forcement du meme cote")
    p.add_argument("--sortie", help="chemin de sortie (pour comparer des variantes)")
    p.add_argument("--echelle", type=float, default=1.0,
                   help="homothetie uniforme en blocs (1 bloc = 1 m). Leman Russ : 4.42/1.83 = 2.4153 "
                        "d'apres la planche technique fournie par Donopot (docs/REFERENCE-LEMAN-RUSS.md)")
    p.add_argument("--centrer", action="store_true",
                   help="centrer le modele sur l'origine (x=0, z=0) et poser son bas sur y=0 — "
                        "indispensable pour une ENTITE : sinon le modele est decale d'un demi-bloc")
    a = p.parse_args()

    chemin = os.path.join(SOURCE, "models/item", f"{a.modele}.json")
    if not os.path.exists(chemin):
        print(f"ERREUR : {chemin} introuvable")
        return 2
    modele = json.load(open(chemin, encoding="utf-8"))
    elements = modele.get("elements") or []
    if not elements:
        print(f"ERREUR : {a.modele} n'a aucun element (modele parent uniquement ?)")
        return 2
    taille = modele.get("texture_size") or [16, 16]
    tx = taille[0] if isinstance(taille, list) else taille
    nom_geo = a.nom_geo or a.modele

    y_min = min(e["from"][1] for e in elements)
    y_max = max(e["to"][1] for e in elements)
    y_retour = y_min + y_max          # l'axe du miroir : y' = y_retour - y

    def transformer(p, decalage=(0.0, 0.0, 0.0)):
        """UNE SEULE fonction transforme TOUS les points : coins des cubes ET origines de rotation.

        La faute qui a coute le plus cher : le recentrage etait calcule AVANT les changements d'axe,
        donc avec le mauvais signe en Z — mesure sur une figurine de reference, le modele s'etendait
        de 2,50 blocs d'un cote et 0,50 de l'autre au lieu d'etre centre sur sa caisse.
        """
        x, y, z = p
        if not a.sans_y_haut:
            y = y_retour - y
        if not a.sans_z_avant:
            z = -z
        # homothetie UNIFORME : 1 bloc = 1 m (le joueur mesure 1,8 bloc). Appliquee ici, donc aux
        # coins des cubes ET aux origines de rotation — et sans anisotropie, les angles restent justes.
        x, y, z = x * a.echelle, y * a.echelle, z * a.echelle
        return [x + decalage[0], y + decalage[1], z + decalage[2]]

    # centrage : une entite est placee sur son origine, un modele d'objet est centre sur sa boite.
    # Sans cela, le char apparait a cote de sa propre hitbox. Calcul fait sur les points DEJA
    # transformes, c'est tout l'interet de passer par une seule fonction.
    xs, ys, zs = [], [], []
    for e in elements:
        for sommet in (e["from"], e["to"]):
            q = transformer(sommet)
            xs.append(q[0])
            ys.append(q[1])
            zs.append(q[2])
    if a.centrer:
        decalage = [-(min(xs) + max(xs)) / 2.0, -min(ys), -(min(zs) + max(zs)) / 2.0]
    else:
        decalage = [0.0, 0.0, 0.0]

    cubes = []
    for e in elements:
        x1, y1, z1 = transformer(e["from"], decalage)
        x2, y2, z2 = transformer(e["to"], decalage)
        origine = [min(x1, x2), min(y1, y2), min(z1, z2)]
        taille_cube = [abs(x2 - x1), abs(y2 - y1), abs(z2 - z1)]
        uv: dict[str, list[float]] = {}
        for face_bedrock, (face_java, retourne) in FACE_JAVA.items():
            f = (e.get("faces") or {}).get(face_java)
            if f is None:
                continue
            uv[face_bedrock] = uv_bedrock(f["uv"], tx, retourne)
        cube = {"origin": [round(v, 5) for v in origine],
                "size": [round(v, 5) for v in taille_cube],
                "uv": uv}
        r = e.get("rotation")
        if a.sans_rotations:
            r = None
        if r and abs(r.get("angle", 0)) > 1e-9:
            axe = r.get("axis", "y")
            angle = r["angle"]
            # Java et Bedrock ne tournent pas forcement du meme cote : le sens est EPROUVE au rendu
            # contre une reference, il n'est pas suppose. Voir docs/DESIGN.md.
            if a.sens_rotation != "tel-quel":
                axes = {"inverse": "xyz", "inverse-x": "x", "inverse-y": "y", "inverse-z": "z"}[a.sens_rotation]
                if axe in axes:
                    angle = -angle
            if axe == "y" and not a.sans_y_haut:
                angle = -angle                      # Y est reflechi : le sens de rotation s'inverse
            if axe == "z" and not a.sans_z_avant:
                angle = -angle
            ox, oy, oz = transformer(r["origin"], decalage)
            if not a.sans_z_avant:
                oz = -oz
            # les geometries du projet ecrivent la rotation d'un cube comme Java le fait :
            # {angle, axis, origin}. On garde la meme forme, le rendu la lit deja.
            cube["rotation"] = {"angle": round(angle, 4), "axis": axe,
                                "origin": [round(ox, 5), round(oy, 5), round(oz, 5)]}
        cubes.append(cube)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": f"geometry.{nom_geo}",
                "texture_width": tx, "texture_height": tx,
                "visible_bounds_width": 6, "visible_bounds_height": 4,
                "visible_bounds_offset": [0, 1, 0],
            },
            "bones": [{
                "name": nom_geo,
                "pivot": [0, 0, 0],
                "cubes": cubes,
            }],
        }],
    }
    os.makedirs(os.path.join(DEST, "geckolib/models"), exist_ok=True)
    sortie = a.sortie or os.path.join(DEST, "geckolib/models", f"{nom_geo}.geo.json")
    with open(sortie, "w", encoding="utf-8") as f:
        json.dump(geo, f, ensure_ascii=False, indent=1)
    largeur = (max(xs) - min(xs)) / 16.0
    profondeur = (max(zs) - min(zs)) / 16.0
    hauteur = (max(ys) - min(ys)) / 16.0
    print(f"{sortie} ecrit · {len(cubes)} cubes · texture {tx}x{tx}")
    print(f"   gabarit : {largeur:.2f} x {hauteur:.2f} x {profondeur:.2f} bloc(s)"
          + ("  (centre sur l'origine, bas sur y=0)" if a.centrer else "  (brut, non centre)"))

    # la texture, copiee sous notre espace de noms
    nom_tex = modele.get("textures", {}).get("0", "")
    if nom_tex and not nom_tex.endswith(".png"):
        nom_tex += ".png"                      # les modeles Java ecrivent le chemin sans extension
    source_tex = os.path.join(SOURCE, "textures", *nom_tex.split("/")) if nom_tex else ""
    if source_tex and os.path.exists(source_tex):
        cible_dir = os.path.join(DEST, "textures/entity")
        os.makedirs(cible_dir, exist_ok=True)
        cible = os.path.join(cible_dir, f"{nom_geo}.png")
        if not a.sortie:                       # en comparaison, on ne retouche pas la texture
            shutil.copy(source_tex, cible)
            print(f"texture copiee : {cible}")
    else:
        print(f"ATTENTION : texture introuvable ({nom_tex})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
