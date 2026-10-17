#!/usr/bin/env python3
"""Leman Russ — generateur repris du debut, avec tout ce qui a ete mesure.

Sources, et ce que chacune apporte. Aucune valeur n'est recopiee de memoire.

  planche technique (docs/REFERENCE-LEMAN-RUSS.md) — les COTES
      caisse 7,08 m · largeur 4,86 m · hauteur 4,42 m · canon 3,07 m · garde au sol 0,45 m
      blindage 45-200 mm · 120 mm a ame lisse · equivalence 6 hommes

  image de profil + vue de dessus de Donopot — la DISPOSITION
      tourelle a 45 % de la longueur depuis l'avant (mesure : 47 % sur son image)
      axe du canon a 78 % de la hauteur (mesure : 77 %)
      canon ne depassant que de 0,96 m (mesure : 0,9 m)
      quart arriere haut de 2,40 m (mesure : 2,40 m)

  image de chenille seule de Donopot — le CONTOUR, releve par 20e de longueur
      queue effilee a l'ARRIERE (cote echappement) · barbotin rond a l'AVANT (sous le canon)

  profil detaille de Donopot — la JUPE
      « les chenilles font le tour de la jupe » : la chenille est un ANNEAU au pourtour,
      la jupe est le PANNEAU interieur, avec deux portes rectangulaires, une ligne de rivets,
      et les galets NUS sous elle · lame a dents a l'avant · frein de bouche · lascanon de caisse

  rendu 3D tournant de Donopot, 61 vues — l'ETALONNAGE
      elevation de camera 11 deg (enveloppe des hauteurs, residu 10,7 px)
      echelle rendu/modele 0,998 : les cotes de la planche sont confirmees par son rendu

Repere : X largeur (0 a gauche), Y longueur (l'AVANT vers les PETITS Y), Z hauteur (0 = sol).
0,12 m par voxel. L'ecriture .vox echange Y et Z : MagicaVoxel veut Y vers le haut.

Decision declaree : pas de sponsons. Aucune des images de Donopot n'en montre sur les flancs,
et la planche ne les donne pas en cote. A remettre si Donopot les veut.
"""
import argparse
import os
import struct
import sys

VOX = 0.05                     # 5 cm par voxel : le guide de Donopot, cotes sur 97/74/142/88

# ------------------------------------------------------------------ cotes de la planche
# La caisse seule fait 6,15 m ; les 7,08 m de la longueur totale incluent le debord du canon
# (7,08 - 6,15 = 0,93 m, ce que Donopot a mesure a 0,90 m). Garde au sol : 0,45 m (deux sources
# sur trois ; un document dit 0,85 m, desaccord signale).
CAISSE_M, LARGEUR_M, HAUTEUR_M, CANON_M, GARDE_M = 6.15, 4.86, 4.42, 3.07, 0.45
DECK_AR_M = 2.40                     # mesure sur l'image de Donopot
CANON_HAUT_PCT = 0.78                # axe du canon, en fraction de la hauteur (mesure : 0,77)
CANON_DEBORD_M = 0.96                # ce que le canon depasse de la caisse (mesure : 0,9)
TOURELLE_PCT = 0.45                  # centre de la tourelle depuis l'avant (mesure : 0,47)


def v(m):
    """Des metres en voxels, au plus proche, jamais zero."""
    return max(1, int(round(m / VOX)))


SIZE_X = v(LARGEUR_M)                                  # 41
SIZE_Z = v(HAUTEUR_M)                                  # 37
Y_AV = v(1.20)                                         # avant de caisse (marge canon + lame)
Y_AR = Y_AV + v(CAISSE_M) - 1                          # arriere de caisse
Y_AV_CH = Y_AR - v(6.20)              # bloc de chenille de 6,20 m, fini a l'arriere
SIZE_Y = Y_AR + v(0.48)
Z_GARDE = v(GARDE_M)                                   # 4 : dessous de caisse
# Hauteur de coque portee a 3,00 m comme les vues techniques de Donopot. Le compte tombe juste :
# 3,00 de coque + 1,20 de tourelle + 0,20 de coupole = 4,40 m, pour 4,42 m hors tout annonces.
Z_TOIT = v(3.00)                                       # toit de caisse
Z_DECK_AR = v(DECK_AR_M)                               # 20 : toit a l'arriere (2,40 m)
Z_CANON = int(round(CANON_HAUT_PCT * SIZE_Z))          # 29 : axe du canon
X_CH = v(0.78)                                         # 10 : largeur d'une chenille
# Les chenilles sont DANS la largeur du chassis (3,70 m) ; les sponsons debordent DE ce chassis
# jusqu'aux 4,86 m officiels. Avant, les chenilles etaient au bord extreme et les sponsons
# debordaient au-dela de la grille : deux pieces pour une seule place.
X_G, X_D = None, None   # fixes apres XC_G, voir plus bas
L_CAISSE_M = 3.70                                      # largeur du chassis seul (sans sponsons)
SPONSON_M, SPONSON_H_M = 0.58, 1.10                    # debord et hauteur de la cage de sponson
CHENILLE_LONG_M = 6.20                                 # longueur du bloc de chenille                            # 0..9 et 31..40
XC_G = (SIZE_X - v(L_CAISSE_M)) // 2                   # la caisse deborde AU-DESSUS des chenilles
XC_D = XC_G + v(L_CAISSE_M) - 1
X_G = XC_G
X_D = XC_D - X_CH + 1
H_ENSEMBLE = v(2.80)                                   # 21 : hauteur de l'ensemble chenille+jupe

# 6 tons Cadian du script de Donopot, plus l'acier des maillons et l'ombre des creux
VERT, BEIGE, CHENILLE, ACIER, DETAIL, OMBRE = 1, 2, 3, 4, 5, 6
PALETTE = [(45, 75, 40, 255), (195, 170, 125, 255), (35, 35, 40, 255),
           (150, 150, 155, 255), (180, 40, 40, 255), (90, 85, 75, 255)]

POSES_PERDUES = 0        # une piece posee hors grille disparait sans bruit : on compte
POSES_PAR_PIECE = {}
PIECE_COURANTE = "?"


def poser(V, x, y, z, c):
    global POSES_PERDUES
    if 0 <= x < SIZE_X and 0 <= y < SIZE_Y and 0 <= z < SIZE_Z:
        V[(x, y, z)] = c
    else:
        POSES_PERDUES += 1
        POSES_PAR_PIECE[PIECE_COURANTE] = POSES_PAR_PIECE.get(PIECE_COURANTE, 0) + 1


def camouflage(x, y, z):
    return VERT if (y + x // 2) % v(2.16) < v(1.08) else BEIGE


def disque(V, x0, x1, y0, y1, zc, yc, rayon, c, dans=True):
    """Un disque (ou un anneau si dans=False) dans le plan (y, z), sur une bande de x."""
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            for z in range(zc - rayon, zc + rayon + 1):
                d = (y - yc) ** 2 + (z - zc) ** 2
                if (d <= rayon ** 2) if dans else (rayon ** 2 - rayon * 2 <= d <= rayon ** 2):
                    poser(V, x, y, z, c)


# ------------------------------------------------ contour de chenille mesure sur Donopot
# (position, haut, bas) en % — 0 % = le bout GAUCHE de son image, c'est-a-dire l'ARRIERE.
def contour(y):
    """Demi-profil de la chenille : un LOSANGE ECRASE, en % de la hauteur de l'ensemble.

    0 % = le bord du haut, 100 % = le sol. Trois morceaux, comme le veut un Leman Russ :
      * le milieu   : dessous plat (98 %) et dessus plat (0 %) ;
      * la queue    : a l'ARRIERE, le dessus s'abaisse (58 %) et le dessous remonte un peu,
                      c'est la queue effilee du cote de l'echappement ;
      * le bec      : a l'AVANT, le dessous remonte sur le barbotin (62 %) et le dessus
                      redescend un peu, la ou passe le canon.
    Construit par segments, il n'est plus releve point par point : c'est la forme voulue.
    """
    u = (Y_AR - y) / max(1, Y_AR - Y_AV_CH)          # 0 = arriere, 1 = avant
    A, B = 0.13, 0.87                                # debut et fin des pentes
    if u <= A:                                       # queue effilee
        f = (A - u) / A
        return 0.0 + f * 58.0, 98.0 - f * 3.0
    if u >= B:                                       # bec avant
        f = (u - B) / (1.0 - B)
        return 0.0 + f * 20.0, 98.0 - f * 36.0
    return 0.0, 98.0

def chenilles_et_jupe(V):
    """La chenille est un ANNEAU qui FAIT LE TOUR de la jupe ; la jupe est le panneau interieur.

    « les chenilles font le tour de la jupe » — Donopot, 16/10. Trois voxels de chenille sur tout
    le pourtour, pleine largeur de voie ; a l'interieur, en retrait, le panneau de jupe avec ses
    deux portes rectangulaires et sa ligne de rivets, et les galets NUS sous lui.
    """
    E = v(0.36)
    for y in range(Y_AV_CH, Y_AR + 1):
        haut, bas = contour(y)
        z_h = int(round((1.0 - haut / 100.0) * H_ENSEMBLE - 0.5))
        z_b = int(round((1.0 - bas / 100.0) * H_ENSEMBLE + 0.5))
        u = (y - Y_AV_CH) / max(1, Y_AR - Y_AV_CH)   # 0 = avant
        for x in range(SIZE_X):
            exterieur = x < X_G + X_CH or x >= X_D
            if not exterieur:
                continue                              # entre les deux voies : la caisse
            for z in range(z_b, z_h + 1):
                if z < z_b + E or z > z_h - E:        # le pourtour : la chenille
                    poser(V, x, y, z, CHENILLE if (x + y) % v(0.84) < v(0.60) else ACIER)
                elif z >= z_b + v(0.72) and ((X_G + v(0.48) <= x <= X_G + X_CH - 1)
                                       or (X_D <= x <= X_D + X_CH - v(0.48) - 1)):
                    porte = (0.36 < u < 0.48 or 0.53 < u < 0.65) and z_b + v(0.96) < z < z_h - v(0.60)
                    rivet = (z >= z_h - v(0.48) or z == z_b + v(0.72) or (y - Y_AV_CH) % v(0.15) == 0)
                    poser(V, x, y, z, OMBRE if porte else (ACIER if rivet else camouflage(x, y, z)))
    # les galets, nus sous la jupe : 6 roues a moyeu clair
    for k in range(5):
        yc = Y_AV_CH + v(0.90) + k * v(1.10)
        for x0 in (X_G, X_D):
            disque(V, x0 + 1, x0 + X_CH - 2, yc - v(0.48), yc + v(0.48), v(0.60), yc, v(0.48), CHENILLE)
            disque(V, x0 + 1, x0 + X_CH - 2, yc - v(0.24), yc + v(0.24), v(0.60), yc, v(0.24), ACIER)


def caisse(V):
    """Coque : glacis qui monte vers l'avant, toit plat, pont arriere rabattu a 2,40 m."""
    for y in range(Y_AV, Y_AR + 1):
        f = (y - Y_AV) / max(1, Y_AR - Y_AV)
        toit = Z_TOIT
        if y < Y_AV + v(0.84):                        # glacis
            toit = Z_TOIT - int((v(0.84) - (y - Y_AV)) * 0.5)
        elif f > 0.74:                                # pont arriere en pente douce
            toit = Z_TOIT - round((Z_TOIT - Z_DECK_AR) * (f - 0.74) / 0.26)
        for x in range(XC_G, XC_D + 1):
            for z in range(Z_GARDE, toit + 1):
                poser(V, x, y, z, camouflage(x, y, z))
    for x in range(XC_G, XC_D + 1):                   # plancher
        for y in range(Y_AV, Y_AR + 1):
            poser(V, x, y, Z_GARDE - 1, CHENILLE)


def tourelle(V):
    """Tourelle a 45 % de la longueur depuis l'avant, coupole et trois antennes."""
    y_t = Y_AV + int(TOURELLE_PCT * v(CAISSE_M))
    y0, y1 = y_t - v(0.96), y_t + v(1.08)
    xc = SIZE_X // 2                                  # tourelle de 2,10 m de large, centree
    x0, x1 = xc - v(1.05), xc + v(1.05)
    z0 = Z_TOIT - v(0.24)
    z1 = z0 + v(1.20)                                 # tourelle de 1,20 m de haut (vue de cote)
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            for z in range(z0, z1 + 1):
                if (x < x0 + v(0.24) or x > x1 - v(0.24)) and (y < y0 + v(0.24) or y > y1 - v(0.24)):
                    continue
                if z > z1 - v(0.24) and (x < x0 + v(0.12) or x > x1 - v(0.12)):
                    continue
                poser(V, x, y, z, camouflage(x, y, z))
    disque(V, x0 + v(0.36), x0 + v(0.84), y0 + v(0.36), y0 + v(0.84), z1 + v(0.12), y0 + v(0.60), v(0.36), camouflage(x0 + v(0.36), y0, z1), dans=False)
    for y in range(y0 + v(0.36), y0 + v(0.96)):
        for x in range(x0 + v(0.36), x0 + v(0.96)):
            if (x - (x0 + v(0.60))) ** 2 + (y - (y0 + v(0.60))) ** 2 <= (v(0.36) + 1) ** 2:
                poser(V, x, y, z1 + v(0.24), camouflage(x, y, z1))
    for x, y, h in ((x0 + v(0.36), y0 + v(0.48), v(1.08)), (x0 + v(0.60), y0 + v(0.72), v(1.44)), (x0 + v(0.84), y0 + v(0.60), v(0.84))):
        for z in range(z1 + v(0.36), min(SIZE_Z, z1 + v(0.36) + h)):
            poser(V, x, y, z, ACIER)
    for x in range(x0 + v(0.36), x0 + v(0.96)):       # fumigenes a l'arriere de la tourelle
        poser(V, x, y1 + v(0.12), z1 - v(0.24), CHENILLE)


def canon(V):
    """Canon de 120 mm : axe a 78 % de la hauteur, depassant de 0,96 m, frein de bouche au bout.

    Epaisseur declaree : le tube fait 0,48 m de diametre au lieu de 0,12 m. A 0,12 m par voxel,
    un canon a l'echelle serait un fil d'un voxel, illisible. Choix de lisibilite, pas de cote.
    """
    y_bout = Y_AV - v(CANON_DEBORD_M)
    y_tube = y_bout + v(CANON_M)
    xc = SIZE_X // 2
    # le manchon est un bloc CENTRE de 0,60 m, pas une tranche de toute la largeur (faute corrigee)
    disque(V, xc - v(0.30), xc + v(0.30), y_bout + v(0.12), y_bout + v(0.60), Z_CANON, y_bout + v(0.36), v(0.30), ACIER)
    for y in range(y_bout, y_tube):
        frein = y < y_bout + v(0.48)                     # frein de bouche de 0,50 m
        rb = v(0.18) if frein else v(0.09)               # tube de 0,18 m, frein de 0,36 m
        xb = v(0.18) if frein else v(0.09)
        disque(V, xc - xb, xc + xb, y, y, Z_CANON, y, rb, ACIER)


def lascanon(V):
    """Lascanon de caisse : petit canon sur affut, a l'avant, decale d'un bord."""
    x0 = XC_G + v(0.36)
    disque(V, x0, x0 + v(0.60), Y_AV - v(0.24), Y_AV + v(0.48), Z_GARDE + v(0.72), Y_AV + v(0.12), v(0.36), ACIER)
    for y in range(Y_AV - v(0.72), Y_AV - v(0.12)):
        disque(V, x0 + v(0.24), x0 + v(0.36), y, y, Z_GARDE + v(0.72), y, v(0.12), ACIER)


def lame(V):
    """Lame de bulldozer a dents, a l'avant, au sol. Ce que Donopot montre sur son profil."""
    for y in range(Y_AV - v(0.96), Y_AV - v(0.12)):
        plafond = v(0.90) - v(0.12) * (Y_AV - v(0.12) - y)
        for x in range(XC_G - v(0.24), XC_D + v(0.36)):
            for z in range(0, max(1, plafond) + 1):
                poser(V, x, y, z, OMBRE)
    for k in range(5):                                # les dents
        x = XC_G + v(0.24) + k * v(0.60)
        for z in range(0, v(0.36)):
            poser(V, x, Y_AV - v(0.96), z, ACIER)


def echappements(V):
    """Deux echappements verticaux a l'arriere, au-dessus du pont."""
    for x0 in (XC_G + v(0.24), XC_D - v(0.48)):
        for x in range(x0, x0 + v(0.24)):
            for y in range(Y_AR - v(0.72), Y_AR - v(0.36)):
                for z in range(Z_DECK_AR, Z_DECK_AR + v(0.72)):
                    poser(V, x, y, z, ACIER)


def sponsons(V):
    """Tourelles de flanc : elles font la largeur officielle de 4,86 m. Cage de 1,10 m de haut,
    debordant de 0,58 m de chaque cote du chassis de 3,70 m."""
    z0 = Z_GARDE + v(0.60)
    y0, y1 = Y_AV + v(1.60), Y_AV + v(3.20)
    for cote in (0, 1):
        xs = (range(max(0, XC_G - v(SPONSON_M)), XC_G) if cote == 0
              else range(XC_D + 1, min(SIZE_X, XC_D + 1 + v(SPONSON_M))))
        for x in xs:
            for y in range(y0, y1):
                for z in range(z0, z0 + v(SPONSON_H_M)):
                    poser(V, x, y, z, camouflage(x, y, z))
        for x in (max(0, xs.start), min(SIZE_X - 1, xs.stop - 1)):
            for y in range(y0 + v(0.24), y1 - v(0.24)):
                poser(V, x, y, z0 + v(0.72), ACIER)


def grille_arriere(V):
    """Grille d'aeration du compartiment moteur, en bas a l'arriere (vue de cote)."""
    for x in range(XC_G + v(0.30), XC_D - v(0.30)):
        for y in range(Y_AR - v(0.90), Y_AR - v(0.18)):
            for z in range(Z_GARDE + v(0.24), Z_GARDE + v(0.60)):
                poser(V, x, y, z, ACIER if (y // 2) % 2 == 0 else OMBRE)

PIECES = {"chenilles": chenilles_et_jupe, "caisse": caisse, "sponsons": sponsons, "tourelle": tourelle,
          "canon": canon, "lascanon": lascanon, "lame": lame, "echappements": echappements, "grille": grille_arriere}


def chanfreiner(V):
    """Abat les aretes vives : le Leman Russ n'est pas une pile de cubes.

    Donopot : « c'est bien mais trop cubique ». Son document dit « tourelle polygonale /
    trapezoïdale » et decrit des plaques assemblees a angles bruts. On abat donc les aretes a
    45 degres sur une largeur de CH metres, en ne RETIRANT que de la matiere : aucune pose hors
    grille n'est possible dans cette passe, donc elle ne peut pas recreer de matiere perdue.
    """
    CH = v(0.30)
    y_t = Y_AV + int(TOURELLE_PCT * v(CAISSE_M))
    xc = SIZE_X // 2
    boites = [
        (xc - v(1.05), xc + v(1.05), y_t - v(0.96), y_t + v(1.08), Z_TOIT - v(0.24), Z_TOIT + v(1.20)),  # tourelle
        (XC_G, XC_D, Y_AV, Y_AR, Z_TOIT - v(0.60), Z_TOIT),                                            # haut de coque
        (max(0, XC_G - v(SPONSON_M)), XC_G - 1, Y_AV + v(1.60), Y_AV + v(3.20), Z_GARDE + v(0.60),
         Z_GARDE + v(0.60) + v(SPONSON_H_M)),
        (XC_D + 1, min(SIZE_X - 1, XC_D + v(SPONSON_M)), Y_AV + v(1.60), Y_AV + v(3.20),
         Z_GARDE + v(0.60), Z_GARDE + v(0.60) + v(SPONSON_H_M)),
    ]
    retires = 0
    (tx0, tx1, ty0, ty1, tz0, tz1) = boites[0]
    for (x, y, z) in list(V):                      # tourelle trapezoïdale : flancs inclines
        if tx0 <= x <= tx1 and ty0 <= y <= ty1 and tz0 <= z <= tz1:
            dx = min(x - tx0, tx1 - x)
            if dx < (z - tz0) * 0.45:
                del V[(x, y, z)]; retires += 1
    for (x0, x1, y0, y1, z0, z1) in boites:
        for (x, y, z) in list(V):
            if not (x0 <= x <= x1 and y0 <= y <= y1 and z0 <= z <= z1):
                continue
            dx = min(x - x0, x1 - x)
            dy = min(y - y0, y1 - y)
            if dx + dy < CH:                     # arete verticale abattue (vue de dessus)
                del V[(x, y, z)]; retires += 1
    return retires

def construire(actives):
    global PIECE_COURANTE
    V = {}
    for nom in ("chenilles", "caisse", "sponsons", "tourelle", "canon", "lascanon", "lame",
                "echappements", "grille"):
        if nom in actives:
            PIECE_COURANTE = nom
            PIECES[nom](V)
    chanfreiner(V)          # aretes abattues : moins cubique, sur decision de Donopot
    return V


def ecrire_vox(chemin, V):
    """MagicaVoxel veut Y vers le haut : on echange Y et Z a l'ecriture."""
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
    p.add_argument("--sortie", default="voxels/leman_russ_v2.vox")
    p.add_argument("--pieces", default=",".join(PIECES))
    a = p.parse_args()
    actives = [q.strip() for q in a.pieces.split(",") if q.strip()]
    V = construire(actives)
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    taille = ecrire_vox(a.sortie, V)
    xs = [q[0] for q in V]
    ys = [q[1] for q in V]
    zs = [q[2] for q in V]
    caisse = {}
    PIECES["caisse"](caisse)
    lc = (max(q[1] for q in caisse) - min(q[1] for q in caisse) + 1) * VOX
    print(f"{a.sortie} · pieces : {', '.join(actives)}")
    print(f"  {len(V)} voxels · grille {taille}")
    print(f"  largeur {(max(xs) - min(xs) + 1) * VOX:.2f} m (planche {LARGEUR_M:.2f}) · "
          f"hauteur {(max(zs) - min(zs) + 1) * VOX:.2f} m (planche {HAUTEUR_M:.2f}) · "
          f"caisse mesuree {lc:.2f} m (planche {CAISSE_M:.2f})")
    print(f"  encombrement total {(max(ys) - min(ys) + 1) * VOX:.2f} m · "
          f"canon depassant {(Y_AV - min(ys)) * VOX:.2f} m (mesure Donopot 0,90)")
    global PIECE_COURANTE
    for nom in actives:
        pts = {}
        PIECE_COURANTE = nom
        PIECES[nom](pts)
        if pts:
            px = [q[0] for q in pts]
            pz = [q[2] for q in pts]
            print(f"    {nom:14s} : {(max(px) - min(px) + 1) * VOX:5.2f} m de large · "
                  f"{(max(pz) - min(pz) + 1) * VOX:5.2f} m de haut")
    if POSES_PERDUES:
        print(f"  ARRET : {POSES_PERDUES} poses hors grille, de la matiere est perdue.")
        print(f"  par piece : {POSES_PAR_PIECE}")
        return 1
    print("  poses hors grille : 0 (aucune matiere perdue)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
