#!/usr/bin/env python3
"""Leman Russ — générateur, repris du début (étape 1 : blocage général).

Voir docs/LEMAN-RUSS-PROJET.md. Repère : X largeur, Y longueur (l'avant vers les petits Y),
Z hauteur. 0,12 m par voxel. L'écriture .vox échange Y et Z (MagicaVoxel veut Y vers le haut).

Palette : les 6 tons Cadian du script de Donopot.
"""
import argparse
import os
import struct
import sys

VOX = 0.12
CAISSE_M, LARGEUR_M, HAUTEUR_M, CANON_M, GARDE_M = 7.08, 4.86, 4.42, 3.07, 0.45
CHENILLE_M, CHENILLE_L_M = 2.30, 1.20


def v(m):
    return max(1, int(round(m / VOX)))


SIZE_X = v(LARGEUR_M)                    # 41
SIZE_Z = v(HAUTEUR_M)                    # 37
SIZE_Y = v(CANON_M) + 4 + v(CAISSE_M) + 3    # 92 : du canon (Y petit) a l'arriere de caisse + marge
# La grille DOIT couvrir Y_AV..Y_AR : elle valait 80 alors que la caisse finit a Y_AR=88, donc les
# 9 derniers voxels de caisse (1,08 m) etaient refuses EN SILENCE par poser(). Grille dimensionnee
# sur le trace, plus un compteur de poses perdues, pour que ce silence ne puisse plus arriver.

PALETTE = [(45, 75, 40, 255), (195, 170, 125, 255), (35, 35, 40, 255),
           (150, 150, 155, 255), (180, 40, 40, 255), (90, 85, 75, 255)]
VERT, BEIGE, CHENILLE, ACIER, DETAIL, OMBRE = 1, 2, 3, 4, 5, 6

Y_AV = v(CANON_M) + 4                    # avant de caisse : le canon déborde devant
Y_AR = Y_AV + v(CAISSE_M) - 1            # arrière de caisse
Z_SOL = 0
Z_GARDE = v(GARDE_M)                     # 4 : dessous de caisse
Z_TOIT = v(2.30) + Z_GARDE               # 23 : toit de caisse
X_CH = v(CHENILLE_L_M)                   # 10 : largeur de chenille
X_G, X_D = 0, SIZE_X - X_CH              # 0..9 et 31..40
XC_G, XC_D = X_CH, SIZE_X - X_CH - 1     # 10..30 : la caisse entre les chenilles


def camouflage(x, y, z):
    return VERT if (y + x // 2) % 18 < 9 else BEIGE


POSES_PERDUES = 0        # une piece posee hors grille disparait sans bruit : on compte


def poser(V, x, y, z, c):
    global POSES_PERDUES
    if 0 <= x < SIZE_X and 0 <= y < SIZE_Y and 0 <= z < SIZE_Z:
        V[(x, y, z)] = c
    else:
        POSES_PERDUES += 1


# ---------------------------------------------------------------- pièces, dans l'ordre du projet
CONTOUR_IMAGE = [                    # (position, haut, bas) en % — 0 % = le GAUCHE de l'image
    (0, 53.5, 95.8), (5, 47.2, 99.3), (10, 37.3, 100.0), (15, 28.9, 100.0), (20, 19.7, 100.0),
    (25, 15.5, 100.0), (30, 4.9, 100.0), (35, 0.7, 100.0), (40, 0.7, 98.6), (45, 0.7, 98.6),
    (50, 0.0, 97.9), (55, 0.0, 97.9), (60, 0.0, 98.6), (65, 0.0, 99.3), (70, 0.0, 97.9),
    (75, 0.7, 96.5), (80, 1.4, 86.6), (85, 6.3, 78.9), (90, 14.8, 70.4), (95, 20.4, 63.4),
]
HAUTEUR_ENSEMBLE = 21                # hauteur de l'ensemble chenille + jupe, en voxels


def contour_chenille(t):
    """(haut, bas) en % de la hauteur de l'ensemble, pour t = 0 = ARRIERE, 1 = AVANT.

    L'image est retournee : son bout gauche, qui s'affine vers le bas, est l'ARRIERE ; son bout
    droit, qui porte le petit ergot a mi-hauteur, est l'AVANT. C'est la vue de dessus de Donopot
    qui tranche : le canon y pointe du cote de l'ergot, donc ce cote est l'avant.
    """
    # La QUEUE EFFILEE du profil (longue pente du haut de chenille) est a l'ARRIERE, du cote de
    # l'echappement ; le BARBOTIN rond est a l'AVANT, sous le canon. Le bout GAUCHE de l'image
    # est donc l'arriere : x = t * 95. La formule precedente (1 - t) mettait la queue a l'avant.
    x = t * 95.0
    for i in range(len(CONTOUR_IMAGE) - 1):
        p0, h0, b0 = CONTOUR_IMAGE[i]
        p1, h1, b1 = CONTOUR_IMAGE[i + 1]
        if p0 <= x <= p1:
            f = (x - p0) / (p1 - p0)
            return h0 + f * (h1 - h0), b0 + f * (b1 - b0)
    return CONTOUR_IMAGE[-1][1], CONTOUR_IMAGE[-1][2]


def appliquer_contour(V):
    # Decision Donopot (16/10) : « les chenilles font le tour de la jupe ».
    # La silhouette EXTERIEURE est le contour mesure sur son image. La chenille est un ANNEAU
    # qui suit ce pourtour sur 3 voxels, et la jupe est le PANNEAU INTERIEUR, en retrait, avec
    # ses deux portes. Avant, la jupe etait posee derriere la chenille : c'etait faux.
    y_av, y_ar = Y_AV - 3, Y_AR
    span = max(1, y_ar - y_av)
    E = 3
    ajoutes = retires = 0
    for y in range(y_av, y_ar + 1):
        t = 1.0 - (y - y_av) / span
        haut, bas = contour_chenille(t)
        z_h = int(round((1.0 - haut / 100.0) * HAUTEUR_ENSEMBLE - 0.5))
        z_b = int(round((1.0 - bas / 100.0) * HAUTEUR_ENSEMBLE + 0.5))
        for x in range(0, SIZE_X):
            externe = x < X_G + X_CH or x >= X_D
            if not externe:
                continue                          # entre les chenilles : la coque
            for z in range(SIZE_Z):
                dedans = z_b <= z <= z_h
                present = (x, y, z) in V
                if not dedans:
                    if present:
                        del V[(x, y, z)]; retires += 1
                    continue
                if z < z_b + E or z > z_h - E:      # le pourtour : la chenille, pleine largeur
                    if not present:
                        V[(x, y, z)] = CHENILLE if (x + y) % 7 < 5 else ACIER; ajoutes += 1
                elif (X_G + 4 <= x <= X_G + X_CH - 1) or (X_D <= x <= X_D + X_CH - 5):
                    # la jupe est un PANNEAU : d'apres le profil detaille de Donopot elle ne
                    # descend pas sur les galets (ils restent nus sous elle) et elle porte DEUX
                    # portes rectangulaires, plus une ligne de rivets sur le pourtour.
                    if z >= z_b + 6 and not present:
                        u = (y - y_av) / span
                        porte = ((0.36 < u < 0.48) or (0.53 < u < 0.65)) and (z_b + 8 < z < z_h - 5)
                        rivet = (z == z_h - 4) or (z == z_b + 6) or (y - y_av) % 12 == 0
                        V[(x, y, z)] = OMBRE if porte else (ACIER if rivet else camouflage(x, y, z))
                        ajoutes += 1
    return ajoutes, retires


def chenilles(V, details=True):
    z_haut = v(CHENILLE_M)               # 15 : bandeau d'environ 40 % de la hauteur
    for x0 in (X_G, X_D):
        for y in range(Y_AV - 3, Y_AR + 1):
            d_av, d_ar = y - (Y_AV - 3), Y_AR - y
            zm, zM = Z_SOL + 1, z_haut
            if d_av < 8:                 # arrondi avant (barbotin)
                zm += max(0, (8 - d_av) // 2)
                zM -= max(0, (8 - d_av) // 3)
            if d_ar < 5:                 # arrondi arrière (galet tendeur)
                zm += max(0, (5 - d_ar) // 2)
                zM -= max(0, (5 - d_ar) // 3)
            for x in range(x0, x0 + X_CH):
                for z in range(zm, zM + 1):
                    poser(V, x, y, z, CHENILLE)
            if details and (y - Y_AV) % 3 == 0:
                for x in range(x0, x0 + X_CH):
                    poser(V, x, y, zM, OMBRE)
        if not details:
            continue
        xc = x0                            # face extérieure : galets et suspensions
        for i in range(7):
            yc = Y_AV + 2 + int((v(CAISSE_M) - 8) * (i + 0.5) / 7)
            zc, r = v(0.35), v(0.30)
            for y in range(Y_AV - 3, Y_AR + 1):
                for z in range(0, z_haut + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= (r + 1) ** 2:
                        poser(V, xc, y, z, CHENILLE)
                    if d <= r ** 2:
                        poser(V, xc, y, z, ACIER)
        for i in range(5):
            yc = Y_AV + 2 + int((v(CAISSE_M) - 8) * (i + 0.5) / 5)
            zc, r = v(0.75), v(0.42)
            for y in range(Y_AV - 3, Y_AR + 1):
                for z in range(0, z_haut + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= (r + 1) ** 2:
                        poser(V, xc, y, z, OMBRE)
                    if d <= r ** 2:
                        poser(V, xc, y, z, CHENILLE)


    appliquer_contour(V)

def caisse(V):
    """Caisse avec glacis : le toit est horizontal, c'est le BAS de l'avant qui est coupé."""
    for y in range(Y_AV, Y_AR + 1):
        bas = Z_GARDE
        if y < Y_AV + v(1.6):            # glacis : la plaque monte vers l'arrière
            bas = Z_GARDE + (v(1.6) - (y - Y_AV))
        for x in range(XC_G, XC_D + 1):
            retrait = 1 if x in (XC_G, XC_D) else 0
            for z in range(bas, Z_TOIT + 1):
                if z > Z_TOIT - 3 and retrait:
                    continue             # le toit se resserre d'un voxel
                poser(V, x, y, z, camouflage(x, y, z))


def jupes(V):
    """Plaque de flanc au plan EXTERIEUR (sinon la chenille la masque), 2 trappes, rivets."""
    z_bas, z_haut = v(0.75), Z_TOIT
    # DECOUPAGE : la planche et le rendu montrent une jupe faite de PANNEAUX separes, pas d'une
    # plaque continue. Les extremites suivent le glacis a l'avant et le pont moteur a l'arriere.
    def plafond_j(y):
        haut = z_haut
        if y < Y_AV + 6:
            haut -= (Y_AV + 6 - y)
        if y > Y_AR - 7:
            haut -= (y - (Y_AR - 7))
        return haut
    for x0, sgn in ((X_G, 1), (X_D + X_CH - 1, -1)):
        for y in range(Y_AV + 2, Y_AR - 1):
            for z in range(z_bas, plafond_j(y) + 1):
                for x in (x0, x0 + sgn):
                    if x > SIZE_X - 1:
                        continue
                    poser(V, x, y, z, camouflage(x, y, z))
        for yc in (Y_AV + 9, Y_AV + 31):
            for y in range(Y_AV + 2, Y_AR - 1):
                for z in range(z_bas, z_haut + 1):
                    if (y - yc) ** 2 + (z - (z_bas + z_haut) // 2) ** 2 <= 9:
                        poser(V, x0, y, z, ACIER)
                        if x0 + sgn < SIZE_X:
                            poser(V, x0 + sgn, y, z, ACIER)
        for y in range(Y_AV + 4, Y_AR - 2, 5):
            poser(V, x0, y, z_haut, ACIER)
            if x0 + sgn < SIZE_X:
                poser(V, x0 + sgn, y, z_haut, ACIER)
        # fentes entre panneaux : deux coupures verticales d'un voxel, comme sur le rendu
        for yc in (Y_AV + 16, Y_AV + 34):
            for y in (yc, yc + 1):
                for z in range(z_bas, z_haut + 1):
                    V.pop((x0, y, z), None)
                    V.pop((x0 + sgn, y, z), None)


def sponsons(V):
    for x0 in (XC_G - 2, XC_D):
        for x in range(x0, x0 + 3):
            for y in range(Y_AV + v(2.4), Y_AV + v(4.4)):
                for z in range(Z_GARDE + v(1.2), Z_GARDE + v(2.2)):
                    poser(V, x, y, z, camouflage(x, y, z))
            for y in range(Y_AV + v(1.6), Y_AV + v(2.4)):      # canon du bolter
                for z in range(Z_GARDE + v(1.5), Z_GARDE + v(1.9)):
                    for x in (x0 + 1, x0 + 2):
                        poser(V, x, y, z, CHENILLE)


def tourelle(V):
    X0, X1 = XC_G + 4, XC_D - 4
    Y0, Y1 = Y_AV + v(1.8), Y_AV + v(4.6)
    Z0, Z1 = Z_TOIT - 2, Z_TOIT + v(1.0)
    for x in range(X0, X1 + 1):
        for y in range(Y0, Y1 + 1):
            for z in range(Z0, Z1 + 1):
                if (x < X0 + 2 or x > X1 - 2) and (y < Y0 + 2 or y > Y1 - 2):
                    continue
                if z > Z1 - 2 and (x < X0 + 1 or x > X1 - 1):
                    continue
                poser(V, x, y, z, camouflage(x, y, z))
    for x in range(X0 + 3, X0 + 8):                        # coupole du chef de char
        for y in range(Y1 - 5, Y1 - 1):
            if (x - (X0 + 5)) ** 2 + (y - (Y1 - 3)) ** 2 <= 6:
                for z in range(Z1 + 1, Z1 + 3):
                    poser(V, x, y, z, camouflage(x, y, z))
    for x, y, h in ((X0 + 4, Y1 - 5, 9), (X0 + 6, Y1 - 3, 12), (X0 + 5, Y1 - 2, 7)):   # 3 antennes
        for z in range(Z1 + 3, min(SIZE_Z, Z1 + 3 + h)):
            poser(V, x, y, z, ACIER)
    for x in range(X0 + 3, X0 + 8):                        # fumigènes à l'arrière
        poser(V, x, Y1 + 1, Z1 - 1, CHENILLE)


def canon(V):
    """Canon : masque + baril. Deux retouches mesurees contre l'image de Donopot (vue de profil) :
    l'axe du canon remonte (70 % -> 78 % de la hauteur du char, mesure 77 % sur son image) et le
    baril depasse de 0,96 m au lieu de 1,80 m (son image : ~0,9 m). Le rendu 3D et cette image
    disent la meme chose, ils sont deux references independantes."""
    X0, X1 = XC_G + 4, XC_D - 4
    zc = Z_TOIT + v(0.70)               # axe du canon a 78 % de la hauteur (etait v(0.4) = 70 %)
    y_masque = Y_AV + v(1.8) - 2
    for y in range(y_masque - 3, y_masque + 1):            # masque cylindrique
        for x in range(X0 + 1, X1):
            for z in range(zc - v(0.55), zc + v(0.55)):
                if (x - (X0 + X1) / 2) ** 2 + (z - zc) ** 2 <= 12:
                    poser(V, x, y, z, ACIER)
    y_bout = Y_AV - 8                   # bout du canon a 0,96 m devant la coque (etait 1,80 m)
    for y in range(max(1, y_bout), y_masque - 2):
        epais = v(0.30) if y > y_bout + 3 else v(0.45)     # frein de bouche au bout
        for x in range(X0 + 4, X1 - 3):
            for z in range(zc - epais, zc + epais + 1):
                if (x - (X0 + X1) / 2) ** 2 + (z - zc) ** 2 <= epais ** 2 + 2:
                    poser(V, x, y, z, ACIER)


def lascanon(V):
    """Lascanon Lucius de caisse : à l'avant, sur un affût rond, décalé d'un bord."""
    x0 = XC_G + 3
    for y in range(Y_AV - 2, Y_AV + 5):
        for x in range(x0, x0 + 5):
            for z in range(Z_GARDE + 4, Z_GARDE + 8):
                if (x - (x0 + 2)) ** 2 + (z - (Z_GARDE + 6)) ** 2 <= 6:
                    poser(V, x, y, z, ACIER)
    for y in range(Y_AV - 6, Y_AV - 2):
        for x in range(x0 + 1, x0 + 4):
            for z in range(Z_GARDE + 5, Z_GARDE + 7):
                poser(V, x, y, z, ACIER)


def lame(V):
    """Lame de bulldozer : plaque inclinée à l'avant, au sol."""
    for y in range(Y_AV - v(1.4), Y_AV - 1):
        plafond = v(0.9) - (Y_AV - 1 - y)
        for x in range(XC_G - 1, XC_D + 2):
            for z in range(0, max(1, plafond) + 1):
                poser(V, x, y, z, OMBRE)


def echappements(V):
    for x0 in (XC_G + 1, XC_D - 3):
        for x in range(x0, x0 + 3):
            for y in range(Y_AR - 2, Y_AR + 1):
                for z in range(Z_TOIT - v(0.9), Z_TOIT + 1):
                    poser(V, x, y, z, CHENILLE)


PIECES = {"chenilles": chenilles, "caisse": caisse, "jupes": jupes, "sponsons": sponsons,
          "tourelle": tourelle, "canon": canon, "lascanon": lascanon, "lame": lame,
          "echappements": echappements}


def rabattre_pont_arriere(V):
    """Rabat le pont arriere : deux references le reclament, independamment.

    L'image de Donopot donne 2,40 m de haut sur le dernier dixieme de longueur, le modele 2,76 m.
    La planche le dit aussi, a sa facon : ses 103 puis 106 cellules « en trop » sont concentrees
    sur les deux derniers dixiemes — son plus gros reste. On abaisse donc le toit de caisse et le
    haut de jupe en pente douce sur le dernier metre et demi, comme un pont moteur. Le rabattement
    ne touche ni la tourelle ni les echappements (ils sont devant, ou ils doivent rester hauts).
    """
    y_d = Y_AV + int(0.74 * v(CAISSE_M))         # debut de la pente
    cible = v(2.40)                              # hauteur mesuree sur l'image de Donopot
    touche = {k for k in V}
    caisse, jupes = {}, {}
    PIECES["caisse"](caisse)
    PIECES["jupes"](jupes)
    proteges = set()
    for nom in ("tourelle", "canon", "lascanon", "echappements"):
        q = {}
        PIECES[nom](q)
        proteges |= set(q)
    retires = 0
    for (x, y, z) in touche & (set(caisse) | set(jupes)):
        if y < y_d or (x, y, z) in proteges:
            continue
        f = (y - y_d) / max(1, Y_AR - y_d)
        z_max = Z_TOIT - round((Z_TOIT - cible) * f)
        if z > z_max:
            del V[(x, y, z)]
            retires += 1
    return retires


def construire(actives):
    V = {}
    for nom in ("chenilles", "caisse", "sponsons", "tourelle", "canon", "lascanon",
                "lame", "echappements"):
        if nom in actives:
            PIECES[nom](V)
    rabattre_pont_arriere(V)
    return V


def ecrire_vox(chemin, V):
    paires = [((x, z, y), c) for (x, y, z), c in V.items()]
    xs = [p[0] for p, _ in paires]
    ys = [p[1] for p, _ in paires]
    zs = [p[2] for p, _ in paires]
    dx, dy, dz = min(xs), min(ys), min(zs)
    taille = (max(xs) - dx + 1, max(ys) - dy + 1, max(zs) - dz + 1)

    def chunk(i, c, e=b""):
        return i.encode() + struct.pack("<ii", len(c), len(e)) + c + e
    corps = struct.pack("<i", len(paires))
    for (x, y, z), c in paires:
        corps += struct.pack("BBBB", x - dx, y - dy, z - dz, c)
    rgba = b"".join(struct.pack("BBBB", *c) for c in PALETTE)
    rgba += b"\x00\x00\x00\xff" * (256 - len(PALETTE))
    main = chunk("MAIN", b"", chunk("SIZE", struct.pack("<iii", *taille))
                 + chunk("XYZI", corps) + chunk("RGBA", rgba))
    with open(chemin, "wb") as f:
        f.write(b"VOX " + struct.pack("<i", 150) + main)
    return taille


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--sortie", default="voxels/leman_russ_v1.vox")
    p.add_argument("--pieces", default=",".join(k for k in PIECES if k != "jupes"))
    a = p.parse_args()
    actives = [q.strip() for q in a.pieces.split(",") if q.strip()]
    V = construire(actives)
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    taille = ecrire_vox(a.sortie, V)
    xs = [p[0] for p in V]
    ys = [p[1] for p in V]
    zs = [p[2] for p in V]
    print(f"{a.sortie} · pièces : {', '.join(actives)}")
    print(f"  {len(V)} voxels · grille {taille}")
    # la caisse est mesuree, pas recopiee depuis Y_AR : un chiffre declae ne prouve rien
    caisse = {}
    PIECES["caisse"](caisse)
    lc = (max(q[1] for q in caisse) - min(q[1] for q in caisse) + 1) * VOX
    print(f"  gabarit : largeur {(max(xs)-min(xs)+1)*VOX:.2f} m · "
          f"hauteur {(max(zs)-min(zs)+1)*VOX:.2f} m · "
          f"caisse mesuree {lc:.2f} m (planche {CAISSE_M:.2f} m) · "
          f"encombrement total {(max(ys)-min(ys)+1)*VOX:.2f} m")
    if POSES_PERDUES:
        print(f"  ARRET : {POSES_PERDUES} poses hors grille — de la matiere est perdue. "
              f"Agrandir SIZE_Y (caisse {Y_AV}..{Y_AR}, grille {SIZE_Y}).")
        return 1
    print(f"  poses hors grille : 0 (aucune matiere perdue)")
    for nom in actives:
        pts = {}
        PIECES[nom](pts)
        if pts:
            px = [q[0] for q in pts]
            pz = [q[2] for q in pts]
            print(f"    {nom:14s} : {(max(px)-min(px)+1)*VOX:5.2f} m de large · "
                  f"{(max(pz)-min(pz)+1)*VOX:5.2f} m de haut")
    return 0


if __name__ == "__main__":
    sys.exit(main())
