#!/usr/bin/env python3
"""Rend un modele GeckoLib en image — pour que l'OEIL juge ce que le harnais ne peut pas dire :
l'echelle, la silhouette, les cubes plats, la pose. Les controles prouvent qu'un clip se declenche ;
ils ne disent pas si le mob est beau.

    python3 tools/rendu_modeles.py modele.geo.json texture.png sortie.png [--vues face,troisquarts]
    python3 tools/rendu_modeles.py --planche DOSSIER_MODELES DOSSIER_TEXTURES sortie.png
    python3 tools/rendu_modeles.py ... --surligner-plats     # cubes d'epaisseur nulle en magenta

Une projection orthographique et un rasteriseur a z-buffer : pas de moteur 3D, et chaque ligne est
verifiable. Le damier au sol fait un bloc de cote — c'est le repere qui permet de juger une echelle.

Tourne avec le python du venv voxel (PIL + numpy), pas celui du systeme :
    /opt/data/voxel-venv/bin/python tools/rendu_modeles.py ...
"""
from __future__ import annotations

import argparse
import glob
import json
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

# Faces du cube : nom Bedrock -> les 4 coins dans l'ordre haut-gauche, haut-droit, bas-droit,
# bas-gauche, du point de vue de l'observateur place DEVANT cette face.
# Un coin vaut (cote_x, cote_y, cote_z) : 0 = origine, 1 = origine + taille.
FACES = {
    # u va vers la DROITE du spectateur place devant la face, y vers le haut (schema Bedrock).
    # north : spectateur a -z regardant +z -> sa droite est -x. south : a +z regardant -z -> +x.
    "north": ((1, 1, 0), (0, 1, 0), (0, 0, 0), (1, 0, 0)),
    "south": ((0, 1, 1), (1, 1, 1), (1, 0, 1), (0, 0, 1)),
    "east":  ((1, 1, 1), (1, 1, 0), (1, 0, 0), (1, 0, 1)),
    "west":  ((0, 1, 0), (0, 1, 1), (0, 0, 1), (0, 0, 0)),
    "up":    ((0, 1, 0), (1, 1, 0), (1, 1, 1), (0, 1, 1)),
    "down":  ((0, 0, 1), (1, 0, 1), (1, 0, 0), (0, 0, 0)),
}
# Eclairage par face, facon Minecraft : le dessus est clair, le dessous sombre.
LUMIERE = {"up": 1.0, "down": 0.55, "north": 0.88, "south": 0.88, "east": 0.74, "west": 0.74}
MAGENTA = np.array([255.0, 0.0, 200.0])


def charger_modele(chemin: str) -> dict:
    """Lit un .geo.json GeckoLib/Bedrock et rend ses cubes."""
    with open(chemin, encoding="utf-8") as f:
        data = json.load(f)
    geo = data["minecraft:geometry"][0]
    desc = geo.get("description", {})
    cubes = []
    for bone in geo.get("bones", []):
        for cube in bone.get("cubes", []):
            cubes.append({
                "os": bone["name"],
                "nom": cube.get("name", bone["name"]),
                "origine": [float(v) for v in cube["origin"]],
                "taille": [float(v) for v in cube["size"]],
                "uv": cube.get("uv"),
                "rotation": cube.get("rotation"),
                "pivot": cube.get("pivot"),
                "inflate": float(cube.get("inflate", 0) or 0),
                # le drapeau `mirror` de Bedrock : miroir des UV en X (voir miroir_uv)
                "miroir": bool(cube.get("mirror")),
            })
    return {
        "identifiant": desc.get("identifier", os.path.basename(chemin)),
        "texture": (int(desc.get("texture_width", 64)), int(desc.get("texture_height", 64))),
        "cubes": cubes,
        "matrices_os": matrices_des_os(geo),
    }


def coins_cube(cube) -> list[tuple[float, float, float]]:
    """Les 8 coins en coordonnees du modele. Attention : dans le format Bedrock, un cube n'est PAS
    porte par son os — l'origine est deja en coordonnees du modele. L'os ne sert qu'a animer."""
    ox, oy, oz = cube["origine"]
    sx, sy, sz = cube["taille"]
    inf = cube["inflate"]
    ox, oy, oz = ox - inf, oy - inf, oz - inf
    sx, sy, sz = sx + 2 * inf, sy + 2 * inf, sz + 2 * inf
    return [
        (ox + (sx if i & 1 else 0.0), oy + (sy if i & 2 else 0.0), oz + (sz if i & 4 else 0.0))
        for i in range(8)
    ]


def rotation_en_triplet(rotation, pivot_secours):
    """Bedrock connait DEUX ecritures : une liste [x, y, z] sur l'os, et un objet
    {"angle", "axis", "origin"} sur le CUBE (c'est la forme des geometries de datapack, donc du
    HellDrake). On ramene la seconde a la premiere, sans la perdre en route."""
    if rotation is None:
        return None, pivot_secours
    if isinstance(rotation, dict):
        angle = float(rotation.get("angle", 0.0))
        axe = rotation.get("axis", "x")
        pivot = rotation.get("origin", pivot_secours)
        return {"x": (angle, 0.0, 0.0), "y": (0.0, angle, 0.0), "z": (0.0, 0.0, angle)}.get(
            axe, (0.0, 0.0, 0.0)), pivot
    if isinstance(rotation, (list, tuple)) and all(isinstance(v, (int, float)) for v in rotation):
        return [float(v) for v in rotation], pivot_secours
    return None, pivot_secours


ORDRE_BOITE = ("east", "north", "west", "south")   # ordre des faces sur la rangee du milieu
SENS_ROTATIONS = 1.0          # Bedrock tourne dans le sens des aiguilles ; modifiable pour l'epreuve
IGNORER_ROTATIONS_CUBES = False
MASQUER_PLATS = False


def rotation_bedrock(coins, rotation, pivot):
    """Rotation Bedrock en degres (ordre Z, puis Y, puis X) autour d'un pivot. La plupart des modeles
    convertis sont au repos : on ne l'applique que s'il y a vraiment une rotation."""
    rotation, pivot = rotation_en_triplet(rotation, pivot)
    if not rotation or all(abs(v) < 1e-9 for v in rotation):
        return coins
    rotation = [SENS_ROTATIONS * float(v) for v in rotation]
    rx, ry, rz = (math.radians(SENS_ROTATIONS * v) for v in rotation)
    cx, cy, cz = math.cos(rx), math.cos(ry), math.cos(rz)
    sx, sy, sz = math.sin(rx), math.sin(ry), math.sin(rz)
    m = (
        (cy * cz, cy * sz, -sy),
        (sx * sy * cz - cx * sz, sx * sy * sz + cx * cz, sx * cy),
        (cx * sy * cz + sx * sz, cx * sy * sz - sx * cz, cx * cy),
    )
    px, py, pz = pivot
    sortie = []
    for x, y, z in coins:
        x, y, z = x - px, y - py, z - pz
        sortie.append(tuple(
            m[i][0] * x + m[i][1] * y + m[i][2] * z + (px, py, pz)[i] for i in range(3)
        ))
    return sortie


def _matrice_rotation(rotation):
    """Matrice 3x3 d'une rotation Bedrock (degres, ordre Z puis Y puis X)."""
    rx, ry, rz = (math.radians(v) for v in rotation)
    cx, cy, cz = math.cos(rx), math.cos(ry), math.cos(rz)
    sx, sy, sz = math.sin(rx), math.sin(ry), math.sin(rz)
    return np.array([
        [cy * cz, cy * sz, -sy],
        [sx * sy * cz - cx * sz, sx * sy * sz + cx * cz, sx * cy],
        [cx * sy * cz + sx * sz, cx * sy * sz - sx * cz, cx * cy],
    ])


def _translation(v):
    m = np.eye(4)
    m[:3, 3] = v
    return m


def _matrice_os(rotation, pivot):
    m = np.eye(4)
    m[:3, :3] = _matrice_rotation(rotation)
    return _translation(pivot) @ m @ _translation(-np.asarray(pivot, dtype=float))


def matrices_des_os(geo: dict) -> dict:
    """Matrice MONDE de chaque os : rotation autour de son pivot, composee avec son parent.

    C'est ainsi que GeckoLib pose un modele (et le convertisseur du HellDrake a justement bati les
    pivots pour cela : `P + d + R·s·(v − P)`). Les ignorer fait apparaitre un modele en morceaux —
    ce qui est arrive, et c'est ce qui a mis sur la piste.
    """
    os_par_nom = {b["name"]: b for b in geo.get("bones", [])}
    cache: dict = {}

    def matrice(nom, chaine=()):
        if nom in cache:
            return cache[nom]
        b = os_par_nom.get(nom)
        if b is None or nom in chaine:          # os absent, ou cycle : on ne tourne pas en rond
            return np.eye(4)
        pivot = b.get("pivot") or [0.0, 0.0, 0.0]
        rotation, pivot_reel = rotation_en_triplet(b.get("rotation"), pivot)
        if rotation is None:
            rotation = (0.0, 0.0, 0.0)
        m = _matrice_os(rotation, pivot_reel or pivot)
        parent = b.get("parent")
        if parent:
            m = matrice(parent, chaine + (nom,)) @ m
        cache[nom] = m
        return m

    return {nom: matrice(nom) for nom in os_par_nom}


def pose_animation(chemin: str, image: int):
    """Pose d'une animation a une image donnee : {os: (position en blocs, rotation en degres, echelle)}.

    GeckoLib ne rend pas la geometrie au repos : il rend  P + d + R·s·(v - P). Rendre le repos
    revient donc a montrer autre chose que ce que le joueur voit — et c'est exactement ce qui m'a
    fait tourner en rond sur le HellDrake.
    """
    with open(chemin, encoding="utf-8") as f:
        data = json.load(f)
    clip = next(iter(data.get("animations", {}).values()), {})
    pose = {}
    for nom, os_ in clip.get("bones", {}).items():
        def a_limage(canal, defaut):
            valeurs = os_.get(canal)
            if not isinstance(valeurs, dict):
                return defaut
            cle = str(image)
            if cle in valeurs:
                v = valeurs[cle]
                if isinstance(v, dict):          # forme {"pre": [...], "post": [...]}
                    v = v.get("post", v.get("pre", defaut))
                return [float(x) for x in v]
            return defaut
        pose[nom] = (a_limage("position", [0.0, 0.0, 0.0]),
                     a_limage("rotation", [0.0, 0.0, 0.0]),
                     (a_limage("scale", [1.0]) or [1.0])[0])
    return pose


def matrices_des_os_animees(geo: dict, pose: dict) -> dict:
    """Matrices d'os avec la pose d'animation appliquee : T(P)·T(d·16)·R(repos+anim)·S·T(-P)."""
    os_par_nom = {b["name"]: b for b in geo.get("bones", [])}
    cache: dict = {}

    def matrice(nom, chaine=()):
        if nom in cache:
            return cache[nom]
        b = os_par_nom.get(nom)
        if b is None or nom in chaine:
            return np.eye(4)
        pivot = b.get("pivot") or [0.0, 0.0, 0.0]
        rotation_repos, pivot_reel = rotation_en_triplet(b.get("rotation"), pivot)
        if rotation_repos is None:
            rotation_repos = (0.0, 0.0, 0.0)
        d_pos, d_rot, echelle = pose.get(nom, ([0.0, 0.0, 0.0], [0.0, 0.0, 0.0], 1.0))
        rotation = [rotation_repos[i] + d_rot[i] for i in range(3)]
        m = _matrice_os(rotation, pivot_reel or pivot)
        m = _translation(np.asarray(d_pos, dtype=float) * 16.0) @ m
        if abs(echelle - 1.0) > 1e-9:
            e = np.eye(4)
            e[0, 0] = e[1, 1] = e[2, 2] = echelle
            m = _translation(pivot_reel or pivot) @ e @ _translation(-np.asarray(
                pivot_reel or pivot, dtype=float)) @ m
        parent = b.get("parent")
        if parent:
            m = matrice(parent, chaine + (nom,)) @ m
        cache[nom] = m
        return m

    return {nom: matrice(nom) for nom in os_par_nom}


def camera(yaw_deg: float, pitch_deg: float):
    ry, rp = math.radians(yaw_deg), math.radians(pitch_deg)
    cy, sy, cp, sp = math.cos(ry), math.sin(ry), math.cos(rp), math.sin(rp)

    def projeter(p):
        x, y, z = p
        xr = x * cy + z * sy
        zr = -x * sy + z * cy
        return xr, y * cp - zr * sp, y * sp + zr * cp   # le 3e = profondeur

    return projeter


def miroir_uv(rect):
    """Le schema Bedrock pour `mirror` : « Mirrors the UV's of the unrotated cubes along the x axis,
    also causes the east/west faces to get flipped. » Autrement dit, on echange les bornes en u.
    Mon rendu l'ignorait : les cubes marques mirror sortaient a l'envers."""
    if rect is None:
        return None
    u0, v0, u1, v1 = rect
    return u1, v0, u0, v1


def rectangle_uv(uv, nom_face, taille_cube=None):
    """Rectangle de texture d'une face, en pixels. Deux conventions cohabitent dans le mod :

    - par face (GeckoLib) : {"north": {"uv": [u, v], "uv_size": [du, dv]}, ...} ;
    - en boite (Bedrock historique, ce que produisent les geometries de datapack comme celle du
      HellDrake) : un seul [u, v], et la place de chaque face se deduit des dimensions du cube.
    """
    if uv is None:
        return None
    if isinstance(uv, dict):
        face = uv.get(nom_face)
        if isinstance(face, list) and len(face) == 4:
            # forme {face: [u0, v0, u1, v1]} — celle des geometries de datapack (le HellDrake).
            # Sans cette branche, la face n'a pas d'UV du tout : elle sortait GRISE.
            return float(face[0]), float(face[1]), float(face[2]), float(face[3])
        if not isinstance(face, dict):
            return None
        u, v = face["uv"]
        du, dv = face["uv_size"]
        return float(u), float(v), float(u) + float(du), float(v) + float(dv)
    if isinstance(uv, (list, tuple)) and len(uv) == 2 and taille_cube is not None:
        u, v = float(uv[0]), float(uv[1])
        lx, ly, lz = (float(t) for t in taille_cube)
        # Boite depliee : la rangee du haut porte up puis down (chacune w x d), la rangee du
        # milieu les quatre cotes. L'ORDRE de ces quatre-la se regle : c'est lui qui decalait les
        # textures. Voir --ordre-boite et tools/diagnostic_uv.py.
        place = {"up": (u + lz, v, lx, lz), "down": (u + lz + lx, v, lx, lz)}
        curseur = u
        for cote in ORDRE_BOITE:
            largeur = lz if cote in ("east", "west") else lx
            place[cote] = (curseur, v + lz, largeur, ly)
            curseur += largeur
        place = place.get(nom_face)
        if place is None:
            return None
        pu, pv, du, dv = place
        return pu, pv, pu + du, pv + dv
    return None


def rasteriser(couleurs, zbuf, p0, p1, p2, uv0, uv1, uv2, texture, facteur, plat, surligner):
    """Un triangle : test barycentrique, z-buffer, echantillonnage de texture. Vectorise."""
    h, w = zbuf.shape
    xmin = max(0, int(math.floor(min(p0[0], p1[0], p2[0]))))
    xmax = min(w - 1, int(math.ceil(max(p0[0], p1[0], p2[0]))))
    ymin = max(0, int(math.floor(min(p0[1], p1[1], p2[1]))))
    ymax = min(h - 1, int(math.ceil(max(p0[1], p1[1], p2[1]))))
    if xmax < xmin or ymax < ymin:
        return
    (x0, y0, z0), (x1, y1, z1), (x2, y2, z2) = p0, p1, p2
    denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
    if abs(denom) < 1e-9:
        return
    ys, xs = np.mgrid[ymin:ymax + 1, xmin:xmax + 1].astype(np.float64)
    l0 = ((y1 - y2) * (xs - x2) + (x2 - x1) * (ys - y2)) / denom
    l1 = ((y2 - y0) * (xs - x2) + (x0 - x2) * (ys - y2)) / denom
    l2 = 1.0 - l0 - l1
    dedans = (l0 >= -1e-6) & (l1 >= -1e-6) & (l2 >= -1e-6)
    if not dedans.any():
        return
    z = l0 * z0 + l1 * z1 + l2 * z2
    if surligner and plat:
        teinte = np.broadcast_to(MAGENTA, (dedans.shape[0], dedans.shape[1], 3))
    else:
        u = l0 * uv0[0] + l1 * uv1[0] + l2 * uv2[0]
        v = l0 * uv0[1] + l1 * uv1[1] + l2 * uv2[1]
        ui = np.clip(np.rint(u).astype(np.int32), 0, texture.shape[1] - 1)
        vi = np.clip(np.rint(v).astype(np.int32), 0, texture.shape[0] - 1)
        teinte = texture[vi, ui] * facteur
    zone_z = zbuf[ymin:ymax + 1, xmin:xmax + 1]
    zone_c = couleurs[ymin:ymax + 1, xmin:xmax + 1]
    meilleur = dedans & (z < zone_z)
    zone_z[meilleur] = z[meilleur]
    zone_c[meilleur] = np.broadcast_to(teinte, zone_c.shape)[meilleur]


def rendre(modele: dict, texture: Image.Image, taille=560, yaw=200.0, pitch=18.0, grille=True,
           surligner_plats=False, fond=(24, 24, 32), facteur_echelle=1.0, cible=None):
    """Rend une vue. `cible` (hauteur de reference en blocs) force la meme echelle entre modeles."""
    cubes = modele["cubes"]
    matrices = modele.get("matrices_os", {})
    geometrie = []
    for cube in cubes:
        plat = any(abs(v) < 1e-6 for v in cube["taille"])
        if MASQUER_PLATS and plat:
            continue
        rotation = None if IGNORER_ROTATIONS_CUBES else cube.get("rotation")
        coins = rotation_bedrock(coins_cube(cube), rotation, cube.get("pivot"))
        m = matrices.get(cube.get("os"))
        if m is not None and not IGNORER_ROTATIONS_CUBES:
            coins = [tuple((m @ np.array([*p, 1.0]))[:3]) for p in coins]
        geometrie.append((cube, coins, plat))

    xs = [c[0] for _, coins, _ in geometrie for c in coins]
    ys = [c[1] for _, coins, _ in geometrie for c in coins]
    zs = [c[2] for _, coins, _ in geometrie for c in coins]
    if not xs:
        return Image.new("RGB", (taille, taille), fond), 0, 0
    bas, haut = min(ys), max(ys)
    centre = ((min(xs) + max(xs)) / 2, (bas + haut) / 2, (min(zs) + max(zs)) / 2)
    grandeur = cible if cible else max(max(xs) - min(xs), haut - bas, max(zs) - min(zs), 1.0)

    projeter = camera(yaw, pitch)
    demi = taille / 2.0
    echelle = (taille * 0.86) / (grandeur * facteur_echelle)

    def ecran(p):
        vx, vy, vz = projeter((p[0] - centre[0], p[1] - centre[1], p[2] - centre[2]))
        return vx * echelle + demi, -vy * echelle + demi, vz

    couleurs = np.zeros((taille, taille, 3), dtype=np.float64)
    couleurs[:, :] = fond
    zbuf = np.full((taille, taille), np.inf)

    tex = np.asarray(texture.convert("RGB"), dtype=np.float64)

    # le damier au sol : un cube de cote, pour juger l'echelle a l'oeil
    if grille:
        for i in range(-6, 7):
            for j in range(-6, 7):
                if (i + j) % 2:
                    continue
                coin = [(i, bas, j), (i + 1, bas, j), (i + 1, bas, j + 1), (i, bas, j + 1)]
                pts = [ecran(c) for c in coin]
                # un damier gris, ecrit a la meme profondeur que le sol : les cubes le recouvrent
                gris = np.array([1.0, 1.0, 1.0])
                tex_damier = (gris * (72.0 if (i + j) % 4 == 0 else 50.0)).reshape(1, 1, 3)
                rasteriser(couleurs, zbuf, pts[0], pts[1], pts[2], (0, 0), (0, 0), (0, 0),
                           tex_damier, 1.0, False, False)
                rasteriser(couleurs, zbuf, pts[0], pts[2], pts[3], (0, 0), (0, 0), (0, 0),
                           tex_damier, 1.0, False, False)
    faces = []
    for cube, coins, plat in geometrie:
        for nom_face, indices in FACES.items():
            # les indices des faces sont des COORDONNEES de coin (0/1), pas des numeros : on les
            # convertit avec le meme encodage que coins_cube (bit 0 = x, bit 1 = y, bit 2 = z)
            quad = [coins[c[0] | (c[1] << 1) | (c[2] << 2)] for c in indices]
            pts = [ecran(p) for p in quad]
            profondeur = sum(p[2] for p in pts) / 4.0
            faces.append((profondeur, quad, pts, cube, nom_face, plat))
    faces.sort(key=lambda f: -f[0])

    for _, quad, pts, cube, nom_face, plat in faces:
        rect = rectangle_uv(cube["uv"], nom_face, cube["taille"])
        if cube.get("miroir"):
            rect = miroir_uv(rect)
        if rect is None:
            # pas d'uv pour cette face : on la peint en gris moyen plutot que de l'oublier
            u0 = v0 = u1 = v1 = 0.0
            facteur = LUMIERE.get(nom_face, 0.8) * 0.0
            tex_face = np.full((1, 1, 3), 150.0)
            rasteriser(couleurs, zbuf, pts[0], pts[1], pts[2], (0, 0), (0, 0), (0, 0), tex_face, 1.0,
                       plat, surligner_plats)
            rasteriser(couleurs, zbuf, pts[0], pts[2], pts[3], (0, 0), (0, 0), (0, 0), tex_face, 1.0,
                       plat, surligner_plats)
            continue
        u0, v0, u1, v1 = rect
        facteur = LUMIERE.get(nom_face, 0.8)
        uv = ((u0, v0), (u1, v0), (u1, v1), (u0, v1))   # haut-gauche, haut-droit, bas-droit, bas-gauche
        rasteriser(couleurs, zbuf, pts[0], pts[1], pts[2], uv[0], uv[1], uv[2], tex, facteur, plat,
                   surligner_plats)
        rasteriser(couleurs, zbuf, pts[0], pts[2], pts[3], uv[0], uv[2], uv[3], tex, facteur, plat,
                   surligner_plats)

    return Image.fromarray(np.clip(couleurs, 0, 255).astype(np.uint8)), bas, haut


def chercher_texture(dossier: str, nom: str):
    for motif in (f"{nom}.png", f"{nom}_*.png", f"*{nom}*.png"):
        trouves = sorted(glob.glob(os.path.join(dossier, motif)))
        if trouves:
            return Image.open(trouves[0]).convert("RGB"), trouves[0]
    return None, None


# ======================================================================================
# AUTOTEST — un rendu qu'on n'a pas eprouve ne prouve rien. On lui donne une geometrie
# CONNUE et on verifie ce qui doit apparaitre. C'est ce test qui autorise a croire ensuite
# ce que le rendu montre d'un vrai modele.
# ======================================================================================
def _modele_synthetique(cubes, cote=16):
    os_ = [{"name": "os", "cubes": cubes}]
    return {"minecraft:geometry": [{"description": {
        "identifier": "geometry.test", "texture_width": cote, "texture_height": cote},
        "bones": os_}]}


def _cube(origine, taille, couleur_uv=None):
    return {"origin": origine, "size": taille, "name": "c", "uv": couleur_uv or {}} 


def autotest() -> int:
    """Geometrie connue -> ce qui doit apparaitre apparait. Renvoie 0 si tout passe."""
    import tempfile
    echecs = []

    def verifier(nom, condition, detail=""):
        print(f"   [{'PASS' if condition else 'FAIL'}] {nom} {detail}")
        if not condition:
            echecs.append(nom)

    dossier = tempfile.mkdtemp(prefix="rendu-modeles-")
    # texture de controle : rouge pur (cote droit), vert pur (cote gauche), bleu au centre
    cote = 16
    tex = Image.new("RGB", (cote, cote), (255, 0, 0))
    ImageDraw.Draw(tex).rectangle([0, 0, cote - 1, cote - 1], fill=(255, 0, 0))
    tex.save(os.path.join(dossier, "t.png"))

    # --- 1. un cube seul doit remplir le cadre et etre de la couleur de sa texture
    uv_face = {f: {"uv": [0, 0], "uv_size": [cote, cote]} for f in FACES}
    chemin = os.path.join(dossier, "uns.geo.json")
    with open(chemin, "w", encoding="utf-8") as f:
        json.dump(_modele_synthetique([_cube([0, 0, 0], [16, 16, 16], uv_face)], cote), f)
    modele = charger_modele(chemin)
    image, bas, haut = rendre(modele, tex, taille=200, yaw=180.0, pitch=0.0, grille=False)
    pixels = np.asarray(image)
    centre = pixels[100, 100]
    # la camera regarde le cote nord : la couleur doit etre celle de la texture, modulee par
    # l'eclairage de CETTE face. Le test dit la regle, il ne recopie pas un nombre magique.
    attendu = (int(255 * LUMIERE["north"]), 0, 0)
    verifier("un cube se voit au centre, eclaire comme sa face",
             tuple(int(v) for v in centre) == attendu, f"centre={tuple(int(v) for v in centre)} attendu={attendu}")
    verifier("le cube fait la hauteur du cadre",
             abs((haut - bas) - 16.0) < 0.001, f"hauteur={haut - bas:.2f} blocs (attendu 16)")
    verifier("le fond reste visible sur les bords",
             tuple(pixels[3, 3]) == (24, 24, 32), f"coin={tuple(pixels[3, 3])}")

    # --- 2. occlusion : un cube plus PROCHE doit cacher l'autre
    chemin2 = os.path.join(dossier, "deux.geo.json")
    cube_proche = {"origin": [0, 0, -10], "size": [16, 16, 8], "name": "proche",
                   "uv": {f: {"uv": [0, 0], "uv_size": [cote, cote]} for f in FACES}}
    cube_loin = {"origin": [0, 0, 0], "size": [16, 16, 8], "name": "loin",
                 "uv": {f: {"uv": [0, 0], "uv_size": [cote, cote]} for f in FACES}}
    with open(chemin2, "w", encoding="utf-8") as f:
        json.dump(_modele_synthetique([cube_proche, cube_loin], cote), f)
    m2 = charger_modele(chemin2)
    # texture ou le cote nord (face -Z, celle que voit la camera) est vert
    tex2 = Image.new("RGB", (cote, cote), (255, 0, 0))
    tex2.save(os.path.join(dossier, "t2.png"))
    image2, _, _ = rendre(m2, tex2, taille=200, yaw=180.0, pitch=0.0, grille=False)
    p2 = np.asarray(image2)
    verifier("le cube proche occupe tout le cadre",
             tuple(int(v) for v in p2[100, 100]) == attendu,
             f"centre={tuple(int(v) for v in p2[100, 100])}")

    # --- 3. un cube PLAT doit etre signale quand on le demande
    chemin3 = os.path.join(dossier, "plat.geo.json")
    plat = {"origin": [0, 8, 0], "size": [16, 0, 16], "name": "plat",
            "uv": {f: {"uv": [0, 0], "uv_size": [cote, cote]} for f in FACES}}
    with open(chemin3, "w", encoding="utf-8") as f:
        json.dump(_modele_synthetique([plat], cote), f)
    m3 = charger_modele(chemin3)
    image3, _, _ = rendre(m3, tex, taille=200, yaw=180.0, pitch=70.0, grille=False,
                          surligner_plats=True)
    magenta = (np.asarray(image3) == np.array([255, 0, 200])).all(axis=2).sum()
    verifier("un cube plat est surligne en magenta", magenta > 200, f"{magenta} pixels magenta")

    # --- 4. la rotation d'un os deplace bien la geometrie
    chemin4 = os.path.join(dossier, "tourne.geo.json")
    tourne = {"origin": [0, 0, 0], "size": [16, 16, 16], "name": "t",
              "uv": {f: {"uv": [0, 0], "uv_size": [cote, cote]} for f in FACES}}
    os_tourne = [{"name": "os", "pivot": [0, 0, 0], "rotation": [0, 0, 45], "cubes": [tourne]}]
    brut = {"minecraft:geometry": [{"description": {"identifier": "geometry.test",
            "texture_width": cote, "texture_height": cote}, "bones": os_tourne}]}
    with open(chemin4, "w", encoding="utf-8") as f:
        json.dump(brut, f)
    m4 = charger_modele(chemin4)
    coins_bruts = coins_cube(m4["cubes"][0])
    coins_tournes = rotation_bedrock(coins_bruts, [0, 0, 45], [0, 0, 0])
    largeur_brute = max(c[0] for c in coins_bruts) - min(c[0] for c in coins_bruts)
    largeur_tournee = max(c[0] for c in coins_tournes) - min(c[0] for c in coins_tournes)
    verifier("une rotation de 45 degres elargit l'emprise en X",
             largeur_tournee > largeur_brute * 1.3,
             f"{largeur_brute:.1f} -> {largeur_tournee:.1f} blocs")

    # --- 5. une rotation d'OS de 90 degres doit amener le cube de (1..3) vers un autre axe
    chemin5 = os.path.join(dossier, "os-tourne.geo.json")
    cube5 = {"origin": [16, 0, 0], "size": [16, 16, 16], "name": "c",
             "uv": {f: {"uv": [0, 0], "uv_size": [cote, cote]} for f in FACES}}
    brut5 = {"minecraft:geometry": [{"description": {"identifier": "geometry.test",
             "texture_width": cote, "texture_height": cote},
             "bones": [{"name": "bras", "pivot": [0, 0, 0], "rotation": [0, 0, 90], "cubes": [cube5]}]}]}
    with open(chemin5, "w", encoding="utf-8") as f:
        json.dump(brut5, f)
    m5 = charger_modele(chemin5)
    m_matrice = m5["matrices_os"]["bras"]
    coin = np.array([16.0, 0.0, 0.0, 1.0])
    obtenu = (m_matrice @ coin)[:3]
    # Sens Bedrock : vu depuis l'axe positif vers l'origine, la rotation est HORAIRE.
    # Pour +90 degres sur Z, +X part donc vers -Y. C'est ce que la matrice doit faire.
    attendu5 = np.array([0.0, -16.0, 0.0])
    verifier("une rotation d'os de 90 degres deplace bien le cube",
             np.allclose(obtenu, attendu5, atol=1e-6), f"{tuple(round(v, 2) for v in obtenu)}")

    print(f"\nAUTOTEST RENDU : {'OK' if not echecs else 'ECHEC — ' + ', '.join(echecs)}")
    return 0 if not echecs else 1


def main() -> int:
    p = argparse.ArgumentParser(description="Rend un modele GeckoLib pour l'oeil humain.")
    p.add_argument("modele", nargs="?")
    p.add_argument("texture", nargs="?")
    p.add_argument("sortie", nargs="?")
    p.add_argument("--planche", nargs=2, metavar=("MODELES", "TEXTURES"))
    p.add_argument("--vues", default="troisquarts", help="face,profil,troisquarts,dessus")
    p.add_argument("--taille", type=int, default=560)
    p.add_argument("--echelle-commune", type=float, default=0.0,
                   help="hauteur de reference en blocs, identique pour tous les modeles")
    p.add_argument("--surligner-plats", action="store_true")
    p.add_argument("--sans-grille", action="store_true")
    p.add_argument("--sens-rotations", type=float, default=1.0,
                   help="+1 (defaut, sens Bedrock) ou -1 : sert a eprouver la convention")
    p.add_argument("--animation", help="fichier .animation.json : rendre la pose jouee, pas le repos")
    p.add_argument("--image", type=int, default=0, help="numero d'image dans l'animation")
    p.add_argument("--ordre-boite", default="east,north,west,south",
                   help="ordre des faces sur la rangee du milieu d'une boite depliee")
    p.add_argument("--masquer-plats", action="store_true",
                   help="epreuve : retirer les cubes d'epaisseur nulle, pour voir ce qu'ils portent")
    p.add_argument("--ignorer-rotations-cubes", action="store_true",
                   help="epreuve : rend le modele sans ses rotations par cube")
    p.add_argument("--autotest", action="store_true",
                   help="eprouve le rendu sur une geometrie connue, avant de s'y fier")
    a = p.parse_args()

    if a.autotest:
        return autotest()
    global SENS_ROTATIONS, IGNORER_ROTATIONS_CUBES, MASQUER_PLATS, ORDRE_BOITE
    ORDRE_BOITE = tuple(x.strip() for x in a.ordre_boite.split(",") if x.strip())
    SENS_ROTATIONS = a.sens_rotations
    IGNORER_ROTATIONS_CUBES = a.ignorer_rotations_cubes
    MASQUER_PLATS = a.masquer_plats

    angles = {"face": (180.0, 0.0), "troisquarts": (200.0, 18.0), "profil": (270.0, 0.0),
              "dessus": (180.0, 70.0)}

    if a.planche:
        # --planche consomme ses deux dossiers : la sortie arrive donc en 1er positionnel
        a.sortie = a.sortie or a.modele
        dossier_modeles, dossier_textures = a.planche
        fichiers = sorted(glob.glob(os.path.join(dossier_modeles, "*.geo.json")))
        colonnes, vignette = 5, max(200, a.taille // 2)
        lignes = (len(fichiers) + colonnes - 1) // colonnes
        feuille = Image.new("RGB", (colonnes * vignette, lignes * vignette), (14, 14, 18))
        dessin = ImageDraw.Draw(feuille)
        manquants = []
        for i, chemin in enumerate(fichiers):
            nom = os.path.basename(chemin).replace(".geo.json", "")
            texture, trouvée = chercher_texture(dossier_textures, nom)
            if texture is None:
                manquants.append(nom)
                continue
            modele = charger_modele(chemin)
            image, bas, haut = rendre(modele, texture, taille=vignette - 28,
                                      cible=None,   # chaque modele cadre entier : rien n'est tronque
                                      grille=not a.sans_grille,
                                      surligner_plats=a.surligner_plats)
            cx, cy = (i % colonnes) * vignette, (i // colonnes) * vignette
            feuille.paste(image, (cx + 14, cy + 22))
            # la taille REELLE en blocs (1 bloc = 16 unites) : c'est elle qui permet de comparer
            dessin.text((cx + 14, cy + 6), f"{nom}  —  {haut / 16:.1f} blocs de haut",
                        fill=(235, 235, 245))
        feuille.save(a.sortie)
        print(f"{a.sortie} ecrit · {len(fichiers) - len(manquants)} modeles rendus")
        if manquants:
            print("   sans texture :", ", ".join(manquants))
        return 0

    if not (a.modele and a.texture and a.sortie):
        p.error("il faut modele, texture et sortie (ou --planche)")
    modele = charger_modele(a.modele)
    if a.animation:
        geo_brut = json.load(open(a.modele, encoding="utf-8"))["minecraft:geometry"][0]
        modele["matrices_os"] = matrices_des_os_animees(geo_brut, pose_animation(a.animation, a.image))
        print(f"   pose lue dans {os.path.basename(a.animation)}, image {a.image}")
    texture = Image.open(a.texture).convert("RGB")
    vues = [v.strip() for v in a.vues.split(",") if v.strip() in angles]
    images = [rendre(modele, texture, taille=a.taille, yaw=angles[v][0], pitch=angles[v][1],
                     grille=not a.sans_grille, surligner_plats=a.surligner_plats)[0] for v in vues]
    if len(images) == 1:
        images[0].save(a.sortie)
    else:
        feuille = Image.new("RGB", (a.taille * len(images), a.taille), (14, 14, 18))
        for i, im in enumerate(images):
            feuille.paste(im, (i * a.taille, 0))
        feuille.save(a.sortie)
    plats = [c for c in modele["cubes"] if any(abs(v) < 1e-6 for v in c["taille"])]
    print(f"{a.sortie} ecrit · {len(modele['cubes'])} cubes · {len(plats)} plat(s) · {', '.join(vues)}")
    for c in plats:
        print(f"   PLAT : os={c['os']} cube={c['nom']} taille={c['taille']} origine={c['origine']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
