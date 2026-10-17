#!/usr/bin/env python3
"""Le convertisseur du HellDrake dit vrai ? On compare sa formule a celle de la source, os par os.

Le convertisseur affirme : « la source rend T + R·S·p ; GeckoLib rend P + d + R·s·(v - P) ; en
egalant, d = T - P + R·s·P, ce qui est exact pour n'importe quel pivot P ». C'est une affirmation
verifiable — et si elle est fausse, le corps du HellDrake se detache de ses ailes, ce que Donopot a
constate en jeu.

Ce script prend les memes donnees que le convertisseur (les matrices du rig, la geometrie du resource
pack) et compare, os par os, la position des coins de chaque cube :
    source  :  T + R·(S·p)
    port    :  P + R·(v - P)        avec v = la geometrie qui part dans le .geo.json
L'ecart est en unites de modele (16 par bloc).

    python3 tools/eprouver_conversion.py [--os body] [--blocs]
"""
from __future__ import annotations

import argparse
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import convert_helldrake as C   # noqa: E402


def coins(cube):
    ox, oy, oz = cube["from"]
    sx, sy, sz = cube["to"]
    return [[ox + (sx - ox) * (i & 1), oy + (sy - oy) * ((i >> 1) & 1), oz + (sz - oz) * ((i >> 2) & 1)]
            for i in range(8)]


def rotation_depuis_euler(x, y, z):
    """Angles XYZ en degres -> matrice 3x3. L'inverse de rotation_vers_euler_degres."""
    rx, ry, rz = (math.radians(v) for v in (x, y, z))
    cx, sx = math.cos(rx), math.sin(rx)
    cy, sy = math.cos(ry), math.sin(ry)
    cz, sz = math.cos(rz), math.sin(rz)
    mz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    my = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    mx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    return C.mat_mul3(mz, C.mat_mul3(my, mx))


def verifier_aller_retour_euler(par_nom):
    """rotation_vers_euler_degres puis rotation_depuis_euler doit redonner la matrice de depart.

    Sans cette verification, une algebre juste peut echouer en jeu : GeckoLib ne recoit que des
    angles, pas des matrices.
    """
    pire = 0.0
    ou = ""
    for nom in C.OS_ORDRE:
        r0 = par_nom[nom]["_r_repos"]
        e = C.rotation_vers_euler_degres(r0)
        r2 = rotation_depuis_euler(*e)
        ecart = max(abs(r0[i][j] - r2[i][j]) for i in range(3) for j in range(3))
        if ecart > pire:
            pire, ou = ecart, nom
    print(f"aller-retour Euler -> matrice : pire ecart {pire:.6f} (os {ou})")
    print("   un ecart nul veut dire que les angles suffisent a transmettre la rotation.")
    return pire


def tester_nouvelle_formule(par_nom, os_geo):
    """L'autre derivation, sans pivot : o = R0^-1 * T0 cuit dans la geometrie, P = 0.

    La source rend  X = T + R*(S*p).  En cuisant v = S0*p + o et en posant  d = T - R*(S/S0)*o,
    on obtient exactement la meme position, pour TOUTES les images, sans jamais resoudre
    P - R*P = T (equation qui n'a pas toujours de solution : si R est une rotation de 180 degres,
    I - R est singuliere et aucun pivot n'existe).
    """
    images = C.lire_animation()
    numeros = sorted(images)
    pire = 0.0
    pire_os = None
    for nom in C.OS_ORDRE:
        o_geo = par_nom[nom]
        t0, r0, s0 = o_geo["_t_repos"], o_geo["_r_repos"], o_geo["_s_repos"]
        inv0 = C.mat_inverse([[r0[i][j] for j in range(3)] + [0.0] for i in range(3)]
                             + [[0.0, 0.0, 0.0, 1.0]])
        decalage = [sum(inv0[i][j] * t0[j] for j in range(3)) for i in range(3)]   # o = R0^-1 * T0
        for cube in C.lire_geometrie(nom):
            for p in coins(cube):
                for numero in numeros:
                    m = images[numero].get(nom)
                    if m is None:
                        continue
                    t, r, sc, _ = C.vers_t_r_s(m)
                    # source
                    src = [t[i] + sum(r[i][j] * sc * p[j] for j in range(3)) for i in range(3)]
                    # port : v = S0*p + o ; d = T - R*(S/S0)*o ; rendu = d + R*(S/S0)*v
                    v = [s0 * p[i] + decalage[i] for i in range(3)]
                    s_rel = sc / s0 if s0 else 1.0
                    ro = [sum(r[i][j] * s_rel * decalage[j] for j in range(3)) for i in range(3)]
                    d = [t[i] - ro[i] for i in range(3)]
                    rendu = [d[i] + sum(r[i][j] * s_rel * v[j] for j in range(3)) for i in range(3)]
                    ecart = sum((src[i] - rendu[i]) ** 2 for i in range(3)) ** 0.5
                    if ecart > pire:
                        pire, pire_os = ecart, f"{nom} image {numero}"
    print(f"NOUVELLE FORMULE — pire ecart sur les 19 images et tous les coins : {pire:.6f} unite(s)")
    print(f"   (os le plus fautif : {pire_os})")
    return pire


def distances_source(par_nom):
    """Les morceaux de la SOURCE sont-ils proches ? (la position que la source rend vraiment)"""
    positions = {}
    for nom in C.OS_ORDRE:
        o = par_nom[nom]
        t0, r0, s0 = o["_t_repos"], o["_r_repos"], o["_s_repos"]
        points = []
        for cube in C.lire_geometrie(nom):
            for p in coins(cube):
                points.append([t0[i] + sum(r0[i][j] * s0 * p[j] for j in range(3)) for i in range(3)])
        positions[nom] = points
    print("distances entre os, calculees avec la formule de la SOURCE (unites de modele, 16 = 1 bloc) :")
    paires = []
    for i, a in enumerate(C.OS_ORDRE):
        for b in C.OS_ORDRE[i + 1:]:
            d = min(sum((pa[k] - pb[k]) ** 2 for k in range(3)) ** 0.5
                    for pa in positions[a] for pb in positions[b])
            paires.append((d, a, b))
    paires.sort()
    for d, a, b in paires[:4]:
        print(f"   {a:12} <-> {b:12} {d:8.2f}")
    proches = sum(1 for d, _, _ in paires if d < 8)
    print(f"   {proches}/{len(paires)} liaisons a moins de 8 unites (0,5 bloc)")
    return proches, len(paires)


def verifier_fichiers_livres(chemin_geo, chemin_anim):
    """Les FICHIERS LIVRES reproduisent-ils la source ? (pas ma formule : les fichiers)

    On relit le .geo.json et l'animation tels qu'ils partiront dans le jar, on refait le calcul que
    fera GeckoLib — hierarchie des os, pivot, position/rotation/echelle de l'image — et on compare
    l'origine de chaque cube a la position que la source calcule. C'est la seule verification qui
    porte sur le livrable et non sur mon intention.
    """
    import json as _json

    import numpy as np

    geo = _json.load(open(chemin_geo, encoding="utf-8"))["minecraft:geometry"][0]
    anim = _json.load(open(chemin_anim, encoding="utf-8"))
    clip = next(iter(anim["animations"].values()))
    os_par_nom = {b["name"]: b for b in geo["bones"]}
    source = C.construire()[0]
    par_nom = {o["name"]: o for o in source}
    images = C.lire_animation()

    def rotation(angles):
        rx, ry, rz = (math.radians(v) for v in angles)
        cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
        mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
        my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
        mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
        return mz @ my @ mx

    def matrice_os(nom, image):
        """T(pivot) · T(d) · R · S · T(-pivot), puis compose avec le parent — comme GeckoLib."""
        b = os_par_nom[nom]
        pivot = np.array(b.get("pivot") or [0.0, 0.0, 0.0], dtype=float)
        angles = list(b.get("rotation") or [0.0, 0.0, 0.0])
        canaux = clip["bones"].get(nom, {})
        pos = canaux.get("position", {}).get(str(image), [0.0, 0.0, 0.0])
        rot = canaux.get("rotation", {}).get(str(image), [0.0, 0.0, 0.0])
        echelle = canaux.get("scale", {}).get(str(image), [1.0])
        echelle = float((echelle[0] if isinstance(echelle, list) else echelle))
        total = [angles[i] + rot[i] for i in range(3)]
        m = np.eye(4)
        m[:3, :3] = rotation(total) * echelle
        m[:3, 3] = np.array(pos, dtype=float) * 16.0
        t_piv = np.eye(4)
        t_piv[:3, 3] = pivot
        inv_piv = np.eye(4)
        inv_piv[:3, 3] = -pivot
        m = t_piv @ m @ inv_piv
        parent = b.get("parent")
        if parent and parent in os_par_nom:
            return matrice_os(parent, image) @ m
        return m

    pire, ou = 0.0, ""
    for image in sorted(images):
        for nom in C.OS_ORDRE:
            m_bone = matrice_os(nom, image)
            for cs, cg in zip(C.lire_geometrie(nom), os_par_nom[nom]["cubes"]):
                m_src = images[image].get(nom)
                if m_src is None:
                    continue
                t, r, sc, _ = C.vers_t_r_s(m_src)
                attendu = np.array([t[i] + sum(r[i][j] * sc * cs["from"][j] for j in range(3))
                                    for i in range(3)])
                v = np.array([*cg["origin"], 1.0])
                obtenu = (m_bone @ v)[:3]
                ecart = float(np.linalg.norm(attendu - obtenu))
                if ecart > pire:
                    pire, ou = ecart, f"{nom} image {image}"
    print(f"FICHIERS LIVRES contre SOURCE — pire ecart : {pire:.4f} unite(s) soit {pire / 16:.4f} bloc")
    print(f"   (os le plus fautif : {ou})")
    print("   C'est la verification du livrable : fichiers relus, hierarchie composee comme GeckoLib.")
    return pire


def main() -> int:
    p = argparse.ArgumentParser(description="Epreuve de la conversion HellDrake.")
    p.add_argument("--os", help="n'examiner qu'un os")
    p.add_argument("--blocs", action="store_true", help="afficher en blocs plutot qu'en unites")
    a = p.parse_args()

    os_geo, parents, images, rapport = C.construire()
    par_nom = {o["name"]: o for o in os_geo}
    facteur = 16.0 if a.blocs else 1.0
    unite = "bloc(s)" if a.blocs else "unite(s)"

    # l'equation que le convertisseur affirme : P - R·P doit valoir T. Si elle tombe, la formule
    # n'est plus exacte et la geometrie ecrite ne vaut que pour le pivot choisi.
    print("os            |T|      pivot P        |P-R.P-T| (doit valoir 0)   ecart max")
    for nom in C.OS_ORDRE:
        o = par_nom[nom]
        t0, r0, piv = o["_t_repos"], o["_r_repos"], o["pivot"]
        # R est 3x3 ici : mat_vecteur attend une 4x4 (translation dans m[i][3])
        rp = [sum(r0[i][j] * piv[j] for j in range(3)) for i in range(3)]
        residu = [piv[i] - rp[i] - t0[i] for i in range(3)]
        print(f"   {nom:12} {sum(x*x for x in t0)**0.5:6.2f}  "
              f"[{piv[0]:7.2f},{piv[1]:7.2f},{piv[2]:7.2f}]  "
              f"{max(abs(x) for x in residu):8.3f}   {sum(x*x for x in t0)**0.5:6.2f}")
    print()
    print("os            cubes   ecart moyen   ecart max   (le port se deplace-t-il ?)")
    totaux = []
    for nom in C.OS_ORDRE:
        if a.os and nom != a.os:
            continue
        o = par_nom.get(nom)
        if o is None:
            continue
        t0, r0, s0 = o["_t_repos"], o["_r_repos"], o["_s_repos"]
        pivot = o["pivot"]
        cubes_source = C.lire_geometrie(nom)
        cubes_port = o["cubes"]
        if len(cubes_source) != len(cubes_port):
            print(f"   {nom:12} nombre de cubes different : source {len(cubes_source)}, port {len(cubes_port)}")
            continue
        ecarts = []
        for cs, cp in zip(cubes_source, cubes_port):
            for coin in coins(cs):
                # la source : T + R·(S·p)
                # 3x3 : mat_vecteur attend une 4x4, on calcule directement
                source = [sum(r0[i][j] * s0 * coin[j] for j in range(3)) + t0[i] for i in range(3)]
                # le port : P + R·(v - P), avec v tel qu'ecrit dans le .geo.json
                v = cp["origin"]
                port = [sum(r0[i][j] * (v[j] - pivot[j]) for j in range(3)) for i in range(3)]
                port = [port[i] + pivot[i] for i in range(3)]
                ecarts.append(sum((source[i] - port[i]) ** 2 for i in range(3)) ** 0.5)
        if not ecarts:
            continue
        moyen = sum(ecarts) / len(ecarts)
        pire = max(ecarts)
        totaux.append((moyen, nom, pire, len(cubes_source)))
        print(f"   {nom:12} {len(cubes_source):4d}   {moyen / facteur:10.2f}   {pire / facteur:9.2f}   {unite}")

    if not totaux:
        print("aucun os examine")
        return 2
    print()
    pire_moyen, nom_pire, pire_max, _ = max(totaux)
    print(f"os le plus faux : {nom_pire} — ecart moyen {pire_moyen / facteur:.2f}, pire {pire_max / facteur:.2f} {unite}")
    print()
    verifier_fichiers_livres(
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                     "src/main/resources/assets/voiddynasty/geckolib/models/chaos_helldrake.geo.json"),
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                     "src/main/resources/assets/voiddynasty/geckolib/animations/chaos_helldrake.animation.json"))
    print()
    verifier_aller_retour_euler(par_nom)
    print()
    distances_source(par_nom)
    print()
    tester_nouvelle_formule(par_nom, os_geo)
    print()
    print("Lecture : un ecart proche de zero valide la formule du convertisseur pour cet os. Un ecart")
    print("de plusieurs unites veut dire que la geometrie ecrite ne correspond pas a la pose de la")
    print("source — c'est exactement ce qui detache un membre.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
