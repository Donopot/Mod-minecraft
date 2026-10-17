#!/usr/bin/env python3
"""Leman Russ voxel — version 2, repartie du script de Donopot.

Donopot a renvoye un script complet en disant « recommence en partant de ça ». Cette fois sa
convention d'axes TIENT : X = largeur, Y = longueur (l'avant vers les petits Y), Z = hauteur.
Sont conserves de son script :

  * la palette Cadian a 6 tons (vert armee, beige, chenilles sombres, acier du canon, rouge des
    details, coque ombree) ;
  * son motif de camouflage diagonal ;
  * les CHENILLES RHOMBOIDALES, retrecies aux deux extremites — la forme caracteristique ;
  * la tourelle CYLINDRIQUE (rayon), le masque de canon renforce, le frein de bouche ;
  * le glacis incline, les sponsons et leurs bolters, le canon de coque, la trappe du chef.

Ce qui est remis d'aplomb :
  * les DIMENSIONS viennent de la planche technique (caisse 7,08 m, largeur 4,86, hauteur 4,42,
    canon 3,07, garde au sol 0,45) — sa grille 42x52x34 ne respectait pas ces rapports
    (longueur/largeur 1,24 chez lui, 1,46 sur la planche) ;
  * le fichier .vox est ecrit avec Y vers le HAUT (convention de MagicaVoxel), donc ses Z de
    hauteur partent en Y : sinon son modele s'ouvre couche sur le flanc ;
  * un controle remesure le fichier ecrit et une epreuve negative verifie qu'il sait echouer.

Usage :
  generer_voxel_v2.py [--resolution 0.12] [--sortie voxels/leman_russ_exact.vox] [--epreuve-negative]
"""
import argparse
import os
import struct
import sys

# ------------------------------------------------------------------ planche technique (metres)
CAISSE_M, LARGEUR_M, HAUTEUR_M = 7.08, 4.86, 4.42
CANON_M, GARDE_M = 3.07, 0.45

# palette du script de Donopot, index 1..6
PALETTE = [
    (45, 75, 40, 255),      # 1 vert armee (camouflage)
    (195, 170, 125, 255),   # 2 beige / tan (camouflage)
    (35, 35, 40, 255),      # 3 chenilles & metal sombre
    (150, 150, 155, 255),   # 4 canon principal / acier
    (180, 40, 40, 255),     # 5 details : optiques, insigne
    (90, 85, 75, 255),      # 6 ombrage / coque sombre
]
VERT, BEIGE, CHENILLE, ACIER, DETAIL, OMBRE = 1, 2, 3, 4, 5, 6

VOX = 0.12                                   # metres par voxel


def v(metres):
    return max(1, int(round(metres / VOX)))


def camouflage(x, y, z):
    """Le motif diagonal du script de Donopot : bandes vert / beige."""
    return VERT if (y + x // 2) % 18 < 9 else BEIGE


def construire():
    """Retourne (voxels, pieces). Repere interne : X largeur, Y longueur (avant = petits Y),
    Z hauteur. C'est la convention du script de Donopot."""
    V = {}
    P = {}

    def pose(x, y, z, c, piece):
        V[(x, y, z)] = c
        P.setdefault(piece, set()).add((x, y, z))

    LARG = v(LARGEUR_M)          # 40
    LONG = v(CAISSE_M)           # 59
    HAUT = v(HAUTEUR_M)          # 37
    CH_L = v(1.20)               # largeur de chenille 1,20 m
    CH_H = v(1.35)               # hauteur de chenille 1,35 m
    GARDE = v(GARDE_M)           # garde au sol 0,45 m
    # La caisse fait 7,08 m ET le canon deborde de 3,07 m devant elle : la grille doit donc
    # commencer par le canon, sinon il est rogne (c'etait le cas : 2,64 m au lieu de 3,07).
    Y_AV = v(CANON_M) + 3
    Y_AR = Y_AV + LONG - 1
    X0, X1 = CH_L, LARG - CH_L - 1       # caisse entre les deux chenilles

    # ---- 1. chenilles rhomboidales (forme du script de Donopot : retrecies aux extremites)
    for x0 in (0, LARG - CH_L):
        for y in range(Y_AV, Y_AR + 1):
            d_av, d_ar = y - Y_AV, Y_AR - y
            # brin superieur qui remonte vers l'AVANT (planche : grand barbotin a l'avant, galet
            # tendeur plus petit a l'arriere)
            z_max = CH_H - int(round(v(0.45) * d_av / max(1, LONG)))
            z_min = 0
            if d_av < v(1.1):                        # arrondi avant, grand rayon
                z_min = max(z_min, (v(1.1) - d_av) // 2)
                z_max = min(z_max, CH_H - (v(1.1) - d_av) // 3)
            if d_ar < v(0.8):                        # arrondi arriere, plus court
                z_min = max(z_min, (v(0.8) - d_ar) // 2)
                z_max = min(z_max, CH_H - (v(0.8) - d_ar) // 3)
            for x in range(x0, x0 + CH_L):
                for z in range(z_min, z_max + 1):
                    bord = x in (x0, x0 + CH_L - 1) or z in (z_min, z_max)
                    pose(x, y, z, CHENILLE if bord else OMBRE, "chenilles")
            # maillons : ligne claire sur le brin superieur, tous les 3 voxels
            if (y - Y_AV) % 3 == 0:
                for x in range(x0, x0 + CH_L):
                    if (x, y, CH_H) in V:
                        pose(x, y, CH_H, ACIER, "chenilles")
        # galets : 7 disques sur la face exterieure, poses en bas
        xc = x0 if x0 == 0 else x0 + CH_L - 1
        for i in range(7):
            yc = Y_AV + v(0.9) + int((LONG - v(1.8)) * (i + 0.5) / 7)
            zc = v(0.30)
            r = v(0.30)
            for y in range(Y_AV, Y_AR + 1):
                for z in range(0, CH_H + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= (r + 1) ** 2:
                        pose(xc, y, z, CHENILLE, "chenilles")
                    if d <= r ** 2:
                        pose(xc, y, z, ACIER, "chenilles")

    # ---- 2. caisse, flancs en escalier et glacis incline (comme le script de Donopot)
    for y in range(Y_AV, Y_AR + 1):
        for x in range(X0, X1 + 1):
            toit = HAUT - v(2.3)                     # toit de caisse : 2,30 m, horizontal
            bas = GARDE
            # GLACIS : la planche montre une plaque qui MONTE vers l'avant. Le toit ne bouge pas,
            # c'est le BAS de l'avant qui est coupe : le nez du char est donc plus haut que bas.
            # (Premiere version : je faisais descendre le toit -> caisse en bequille, faute.)
            if y < Y_AV + v(1.6):
                bas = GARDE + (v(1.6) - (y - Y_AV))
            for z in range(bas, toit + 1):
                retrait = 1 if z > toit - 6 else 0
                retrait = 2 if z > toit - 3 else retrait
                if x < X0 + retrait or x > X1 - retrait:
                    continue
                pose(x, y, z, camouflage(x, y, z), "caisse")

    # ---- 3. sponsons et bolters lourds Gryphonne IV, sur les flancs
    for x0 in (X0 - v(0.7), X1 - 0):
        for y in range(v(2.6), v(3.9)):
            for z in range(v(1.6), v(2.3)):
                for x in range(x0, x0 + v(0.7) + 2):
                    pose(x, y, z, camouflage(x, y, z), "sponsons")
            for x in range(x0 + 1, x0 + 3):          # canon du bolter vers l'avant
                for z in range(v(1.8), v(2.1)):
                    pose(x, y - 1, z, CHENILLE, "sponsons")
                    pose(x, y - 2, z, CHENILLE, "sponsons")

    # ---- 4. tourelle cylindrique (rayon) posee sur le toit de caisse
    cx, cy = LARG // 2, Y_AV + v(3.1)            # centree, vers l'avant
    rt = v(0.95)
    zt0 = HAUT - v(2.3)
    zt1 = HAUT - v(0.35) - 1        # la coupole couronne la hauteur de la planche
    for y in range(Y_AV, Y_AR + 1):
        for x in range(LARG):
            if (x - cx) ** 2 + (y - cy) ** 2 > rt ** 2:
                continue
            for z in range(zt0, zt1 + 1):
                pose(x, y, z, camouflage(x, y, z), "tourelle")
    # trappe du chef de char, coupole et lance-grenades fumigenes
    for x in range(cx - 3, cx + 3):
        for y in range(cy + 2, cy + 6):
            for z in range(zt1, zt1 + v(0.35)):
                pose(x, y, z, camouflage(x, y, z), "tourelle")
    for x in range(cx - 1, cx + 2):
        pose(x, cy + 3, zt1 + v(0.35), DETAIL, "tourelle")     # optique rouge
    for x in (cx - v(0.6), cx, cx + v(0.6)):
        for y in (cy - rt - 2, cy - rt - 1):
            pose(x, y, zt1 - 1, CHENILLE, "tourelle")          # tubes fumigenes

    # ---- 5. masque et canon principal de 3,07 m, frein de bouche (script de Donopot)
    y_masque = cy - rt
    z_canon = zt0 + v(0.45)
    y_masque = cy - rt
    for x in range(cx - v(0.75), cx + v(0.75)):          # masque : piece distincte du canon
        for z in range(zt0, zt0 + v(1.1)):
            for y in range(y_masque - v(0.6), y_masque + 1):
                pose(x, y, z, ACIER, "masque")
    # TUBE : c'est lui qui porte les 3,07 m de la planche (le masque est compte a part)
    y_tip = y_masque - v(0.6) - v(CANON_M) + 1
    for y in range(y_tip, y_masque - v(0.6) + 1):
        epais = v(0.45) if y < y_tip + 3 else v(0.30)     # frein de bouche au bout
        for x in range(cx - epais, cx + epais):
            for z in range(z_canon - epais, z_canon + epais):
                pose(x, y, z, ACIER, "canon")

    # ---- 6. canon de coque (lascanon) a l'avant, decale sur un bord
    for y in range(Y_AV, Y_AV + v(1.0)):
        for x in range(X0 + v(1.2), X0 + v(1.8)):
            for z in range(v(1.2), v(1.7)):
                pose(x, y, z, ACIER, "lascanon")

    # ---- 7. lame de bulldozer tous terrains a l'avant (nomenclature de la planche)
    for y in range(max(0, Y_AV - v(0.5)), Y_AV + 1):
        for x in range(X0 - 2, X1 + 3):
            plafond = v(0.8) - (Y_AV - y)
            for z in range(0, max(1, plafond) + 1):
                pose(x, y, z, OMBRE, "lame")

    # ---- 8. echappements du moteur Lucifer, a l'arriere
    for x0 in (X0, X1 - v(0.5)):
        for y in range(Y_AR - 2, Y_AR + 1):
            for z in range(v(1.3), v(1.9)):
                pose(x0, y, z, CHENILLE, "echappements")

    return V, P, (LARG, LONG, HAUT)


# ------------------------------------------------------------------ ecriture et lecture .vox
def ecrire_vox(chemin, V, palette):
    """Ecrit au format MagicaVoxel : Y vers le HAUT. Le repere interne a Z pour hauteur, donc on
    echange Y et Z — sinon le modele s'ouvre couche sur le flanc."""
    paires = [((x, z, y), c) for (x, y, z), c in V.items()]
    xs = [p[0] for p, _ in paires]
    ys = [p[1] for p, _ in paires]
    zs = [p[2] for p, _ in paires]
    dx, dy, dz = min(xs), min(ys), min(zs)
    taille = (max(xs) - dx + 1, max(ys) - dy + 1, max(zs) - dz + 1)

    def chunk(ident, contenu, enfants=b""):
        return (ident.encode("ascii") + struct.pack("<ii", len(contenu), len(enfants))
                + contenu + enfants)

    corps = struct.pack("<i", len(paires))
    for (x, y, z), c in paires:
        corps += struct.pack("BBBB", x - dx, y - dy, z - dz, c)
    rgba = b""
    for coul in palette:
        rgba += struct.pack("BBBB", *coul)
    rgba += b"\x00\x00\x00\xff" * (256 - len(palette))
    main = chunk("MAIN", b"", chunk("SIZE", struct.pack("<iii", *taille))
                 + chunk("XYZI", corps) + chunk("RGBA", rgba))
    with open(chemin, "wb") as f:
        f.write(b"VOX " + struct.pack("<i", 150) + main)
    return taille


def lire_vox(chemin):
    with open(chemin, "rb") as f:
        data = f.read()
    assert data[:4] == b"VOX ", "signature VOX absente"
    taille, voxels = None, {}
    pos = 8
    # SIZE / XYZI / RGBA sont des ENFANTS du chunk MAIN : il faut descendre dedans
    def parcourir(debut, fin):
        nonlocal taille
        p = debut
        while p < fin:
            ident = data[p:p + 4].decode("ascii")
            n, m = struct.unpack("<ii", data[p + 4:p + 12])
            contenu = data[p + 12:p + 12 + n]
            if ident == "MAIN":
                parcourir(p + 12 + n, p + 12 + n + m)
            elif ident == "SIZE":
                taille = struct.unpack("<iii", contenu)
            elif ident == "XYZI":
                for i in range(struct.unpack("<i", contenu[:4])[0]):
                    x, y, z, c = struct.unpack("BBBB", contenu[4 + 4 * i:8 + 4 * i])
                    voxels[(x, y, z)] = c
            p += 12 + n + m
    parcourir(8, len(data))
    return taille, voxels


def mesurer(pts):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    zs = [p[2] for p in pts]
    return (max(xs) - min(xs) + 1) * VOX, (max(ys) - min(ys) + 1) * VOX, (max(zs) - min(zs) + 1) * VOX


def verifier(chemin, P, epreuve=False):
    """Le controle relit le FICHIER ecrit (avec ses axes echanges) et remesure chaque piece."""
    taille, voxels = lire_vox(chemin)
    # retour au repere interne : X largeur, Y longueur, Z hauteur
    vx = {(x, z, y): c for (x, y, z), c in voxels.items()}
    pieces = {nom: mesurer(pts) for nom, pts in P.items()}
    tout = mesurer(set(vx))
    verrous = {
        "largeur hors chenilles (planche 4,86 m)": (tout[0], LARGEUR_M, 0.16),
        "hauteur totale (planche 4,42 m)": (tout[2], HAUTEUR_M, 0.16),
        "longueur de caisse (planche 7,08 m)": (pieces["caisse"][1], CAISSE_M, 0.16),
        "canon principal (planche 3,07 m)": (pieces["canon"][1], CANON_M, 0.30),
    }
    print(f"fichier {chemin} : grille {taille} (X largeur, Y hauteur, Z longueur), "
          f"{len(voxels)} voxels")
    ok = True
    for nom, (mesure, cible, tol) in verrous.items():
        bon = abs(mesure - cible) <= tol
        ok &= bon
        print(f"  [{'OK ' if bon else 'FAUX'}] {nom} : {mesure:.2f} m, ecart {mesure - cible:+.2f} m")
    print(f"  tons utilises : {sorted(set(voxels.values()))} sur {len(PALETTE)}")
    return ok


def main():
    global VOX
    p = argparse.ArgumentParser()
    p.add_argument("--sortie", default="voxels/leman_russ_exact.vox")
    p.add_argument("--resolution", type=float, default=0.12)
    p.add_argument("--epreuve-negative", action="store_true")
    a = p.parse_args()
    VOX = a.resolution

    V, P, grille = construire()
    if a.epreuve_negative:
        zmax = max(q[1] for q in P["canon"])
        retires = [q for q in list(P["canon"]) if q[1] < zmax - v(0.9)]
        for q in retires:
            V.pop(q, None)
            P["canon"].discard(q)
        print("EPREUVE NEGATIVE : canon raccourci de 0,90 m")
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    taille = ecrire_vox(a.sortie, V, PALETTE)
    ok = verifier(a.sortie, P, a.epreuve_negative)
    print(f"  pieces : " + " · ".join(f"{n}={len(s)}" for n, s in sorted(P.items())))
    if a.epreuve_negative:
        print("=> epreuve negative :", "le controle SAIT echouer (correct)" if not ok
              else "PROBLEME : le controle passe alors qu'il devrait echouer")
        return 1 if not ok else 3
    print("resume :", "conforme a la planche" if ok else "ECART AU-DELA DE LA TOLERANCE")
    return 0 if ok else 2


if __name__ == "__main__":
    sys.exit(main())
