#!/usr/bin/env python3
"""Modele voxel -> geometrie GeckoLib, par fusion des voxels coplanaires (greedy meshing).

Pourquoi : le modele voxel du Leman Russ compte plus de 21 000 voxels. Un cube par voxel ferait
ramer le serveur. On fusionne les voxels voisins de meme couleur en BOITES, ce qui donne une
geometrie de quelques centaines de cubes — sans perdre un seul voxel.

Le controle ne se contente pas de compter les boites : il **re-voxelise** les boites produites et
les compare voxel par voxel au fichier d'origine. Une seule case manquante ou en trop = echec.

Usage :
  voxel_vers_geckolib.py --vox voxels/leman_russ_model.vox --geo voxels/lemanruss_voxel.geo.json \\
                         --texture voxels/lemanruss_voxel.png [--epreuve-negative]
"""
import argparse
import json
import os
import struct
import sys
from collections import defaultdict

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generer_voxel_lemanruss import lire_vox, PALETTE, VOX
from PIL import Image

TAILLE_CELLULE = 16          # chaque couleur occupe une cellule de 16x16 pixels
MARGE = 2                    # marge interieure : sans elle, l'echantillonnage bave sur
                             # la cellule voisine et le rendu sort strie (constate)


def fusionner(voxels):
    """Fusion en boites (x0,x1,y0,y1,z0,z1,couleur), par passes le long de X, puis Z, puis Y."""
    # passe 1 : segments le long de X, dans chaque tranche (y,z)
    tranches = defaultdict(dict)
    for (x, y, z), c in voxels.items():
        tranches[(y, z)][x] = c
    segments = defaultdict(list)
    for (y, z), d in tranches.items():
        xs = sorted(d)
        debut = precedent = xs[0]
        couleur = d[xs[0]]
        for x in xs[1:]:
            if x != precedent + 1 or d[x] != couleur:
                segments[(y, z)].append((debut, precedent, couleur))
                debut, couleur = x, d[x]
            precedent = x
        segments[(y, z)].append((debut, precedent, couleur))

    # passe 2 : fusion le long de Z, a segment identique
    empreintes = defaultdict(list)
    for (y, z), liste in segments.items():
        for (x0, x1, c) in liste:
            empreintes[(y, x0, x1, c)].append(z)
    plaques = []
    for (y, x0, x1, c), zs in empreintes.items():
        zs.sort()
        debut = precedent = zs[0]
        for z in zs[1:]:
            if z != precedent + 1:
                plaques.append([x0, x1, y, y, debut, precedent, c])
                debut = z
            precedent = z
        plaques.append([x0, x1, y, y, debut, precedent, c])

    # passe 3 : fusion le long de Y, a empreinte identique
    colonnes = defaultdict(list)
    for b in plaques:
        colonnes[(b[0], b[1], b[4], b[5], b[6])].append(b[2])
    boites = []
    for (x0, x1, z0, z1, c), ys in colonnes.items():
        ys.sort()
        debut = precedent = ys[0]
        for y in ys[1:]:
            if y != precedent + 1:
                boites.append((x0, debut, x1, precedent, z0, z1, c))
                debut = y
            precedent = y
        boites.append((x0, debut, x1, precedent, z0, z1, c))
    return boites


def revoxeliser(boites):
    """Reconstruit l'ensemble des voxels couverts par les boites."""
    retour = {}
    for x0, y0, x1, y1, z0, z1, c in boites:
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    retour[(x, y, z)] = c
    return retour


def ecrire_texture(chemin, palette):
    """Une cellule pleine par couleur : une cellule d'un seul pixel bave a l'echantillonnage."""
    larg, haut = TAILLE_CELLULE * len(palette), TAILLE_CELLULE
    img = Image.new("RGBA", (larg, haut), (0, 0, 0, 0))
    for i, coul in enumerate(palette):
        for x in range(i * TAILLE_CELLULE, (i + 1) * TAILLE_CELLULE):
            for y in range(haut):
                img.putpixel((x, y), coul)
    img.save(chemin)
    return larg, haut


def ecrire_geo(chemin, boites, palette, nom="lemanruss_voxel"):
    """Geometrie Bedrock/GeckoLib : un cube par boite, UV posees sur la cellule de la couleur."""
    unités = 16.0                       # une geometrie s'exprime en 1/16 de bloc
    cubes = []
    for x0, y0, x1, y1, z0, z1, c in boites:
        # un voxel mesure VOX metre = VOX bloc = VOX*16 unites
        o = [round(x0 * VOX * unités, 4), round(y0 * VOX * unités, 4), round(z0 * VOX * unités, 4)]
        t = [round((x1 - x0 + 1) * VOX * unités, 4),
             round((y1 - y0 + 1) * VOX * unités, 4),
             round((z1 - z0 + 1) * VOX * unités, 4)]
        u1 = (c - 1) * TAILLE_CELLULE + MARGE
        u2 = c * TAILLE_CELLULE - MARGE
        v1, v2 = float(MARGE), float(TAILLE_CELLULE - MARGE)
        cube = {"origin": o, "size": t,
                "uv": {f: [u1, v1, u2, v2] for f in
                       ("north", "east", "south", "west", "up", "down")}}
        cubes.append(cube)
    geo = {"format_version": "1.12.0",
           "minecraft:geometry": [{
               "description": {"identifier": f"geometry.{nom}",
                               "texture_width": TAILLE_CELLULE * len(palette),
                               "texture_height": TAILLE_CELLULE,
                               "visible_bounds_width": 6, "visible_bounds_height": 5,
                               "visible_bounds_offset": [0, 2, 0]},
               "bones": [{"name": nom, "pivot": [0, 0, 0], "cubes": cubes}]}]}
    with open(chemin, "w", encoding="utf-8") as f:
        json.dump(geo, f, ensure_ascii=False, indent=1)
    return len(cubes)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--vox", required=True)
    p.add_argument("--geo", default="voxels/lemanruss_voxel.geo.json")
    p.add_argument("--texture", default="voxels/lemanruss_voxel.png")
    p.add_argument("--epreuve-negative", action="store_true",
                   help="retrecit une boite d'un voxel : le controle DOIT alors echouer")
    a = p.parse_args()

    _, taille, voxels, palette = lire_vox(a.vox)
    # une cellule de texture par couleur REELLEMENT utilisee (la palette du .vox est
    # completee a 256 entrees : sans cette coupe, la texture ferait 4080 pixels de large)
    palette = palette[:max(voxels.values())]
    print(f"source : {len(voxels)} voxels, grille {taille}")

    boites = fusionner(voxels)
    if a.epreuve_negative:
        x0, y0, x1, y1, z0, z1, c = boites[len(boites) // 2]
        boites[len(boites) // 2] = (x0, y0, x1 - 1, y1, z0, z1, c)
        print("EPREUVE NEGATIVE : une boite retrecie d'un voxel")

    retour = revoxeliser(boites)
    manquants = set(voxels) - set(retour)
    en_trop = set(retour) - set(voxels)
    faux = {q for q in set(voxels) & set(retour) if voxels[q] != retour[q]}
    exact = not (manquants or en_trop or faux)

    print(f"fusion : {len(boites)} boites pour {len(voxels)} voxels "
          f"({len(voxels) / max(1, len(boites)):.1f} voxels par boite)")
    print(f"  voxels manquants : {len(manquants)} · en trop : {len(en_trop)} · "
          f"couleur fausse : {len(faux)}")
    print("  exactitude :", "EXACT" if exact else "FAUX")

    if a.epreuve_negative:
        print("=> epreuve negative :", "le controle SAIT echouer (correct)" if not exact
              else "PROBLEME : le controle passe alors qu'il devrait echouer")
        return 1 if not exact else 3

    if exact:
        os.makedirs(os.path.dirname(a.geo) or ".", exist_ok=True)
        n = ecrire_geo(a.geo, boites, palette)
        l, h = ecrire_texture(a.texture, palette)
        print(f"  {a.geo} ecrit ({n} cubes) · {a.texture} ecrit ({l}x{h})")
    return 0 if exact else 2


if __name__ == "__main__":
    sys.exit(main())
