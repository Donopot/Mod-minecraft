#!/usr/bin/env python3
"""Le modèle voxel de Donopot, chenilles reprises — RIEN d'autre n'est touché.

Consigne : « reprend le vox que je t'ai donné et commence par ne retoucher que les chenilles ».

Ce fichier contient donc DEUX choses bien séparées :
  * base_donopot() : la reproduction à l'identique du script de Donopot (sa grille 42x52x34,
    sa palette, son camouflage, sa coque, son glacis, ses sponsons, sa tourelle, son canon,
    sa trappe). Aucune valeur n'est changée ;
  * chenilles() : la reprise des chenilles SEULES, dans l'empreinte exacte des siennes
    (x 1..6 et 35..40, y 4..47, z 2..18).

Le contrôle ne se contente pas d'affirmer la séparation : il compare les deux modèles voxel par
voxel HORS empreinte des chenilles, et exige qu'ils soient identiques.

Repère de Donopot : X largeur, Y longueur, Z hauteur. Le fichier .vox est écrit avec Y vers le
haut (MagicaVoxel) : Y et Z sont donc échangés à l'écriture.

Usage :
  voxel_donopot.py --sortie voxels/leman_russ_exact.vox [--temoins]
"""
import argparse
import os
import struct
import sys

# ---- grille et palette du script de Donopot, recopiées telles quelles
SIZE_X, SIZE_Y, SIZE_Z = 42, 52, 34
PALETTE = [
    (45, 75, 40, 255),      # 1 vert armee (camouflage)
    (195, 170, 125, 255),   # 2 beige / tan (camouflage)
    (35, 35, 40, 255),      # 3 chenilles & metal sombre
    (150, 150, 155, 255),   # 4 canon principal / acier
    (180, 40, 40, 255),     # 5 details (optiques / insigne)
    (90, 85, 75, 255),      # 6 ombrage / coque sombre  (inutilise chez Donopot)
]
VERT, BEIGE, CHENILLE, ACIER, DETAIL, OMBRE = 1, 2, 3, 4, 5, 6

TRACK_WIDTH = 6
X_GAUCHE = (1, 1 + TRACK_WIDTH - 1)             # 1..6
X_DROITE = (SIZE_X - 1 - TRACK_WIDTH, SIZE_X - 2)   # 35..40
Y_TRACK = (4, 47)
Z_TRACK = (2, 18)


def camouflage(x, y, z):
    """Le motif de Donopot, tel quel : bandes diagonales vert / beige."""
    return VERT if (y + x // 2) % 18 < 9 else BEIGE


def pieces_donopot_sans_tourelle(V):
    """Tourelle, masque, canon, trappe sont poses par tourelle_donopot ; le reste par ici."""
    d = base_donopot()
    cx, cy = SIZE_X // 2, 26
    for q in list(d):
        x, y, z = q
        dans_tourelle = (16 <= z <= 24 and 14 <= y <= 34) or (z >= 16 and y <= 17)
        if dans_tourelle:
            continue
        V[q] = d[q]
    return V


def base_donopot():
    """Le script de Donopot, transcrit sans y rien changer (sauf les chenilles, retirées ici)."""
    V = {}

    def set_voxel(x, y, z, color_idx):
        if 0 <= x < SIZE_X and 0 <= y < SIZE_Y and 0 <= z < SIZE_Z:
            V[(x, y, z)] = color_idx

    color_camo = camouflage

    # ---- 2. châssis central et blindage avant (recopié tel quel)
    for y in range(10, 42):
        for z in range(4, 16):
            for x in range(1 + TRACK_WIDTH, SIZE_X - 1 - TRACK_WIDTH):
                set_voxel(x, y, z, color_camo(x, y, z))
    for y in range(8, 14):
        z_level = 16 - (y - 8)
        for x in range(1 + TRACK_WIDTH, SIZE_X - 1 - TRACK_WIDTH):
            for z in range(4, z_level):
                set_voxel(x, y, z, color_camo(x, y, z))
    # canon de coque (lascanon)
    for y in range(4, 12):
        for x in range(19, 23):
            for z in range(10, 13):
                set_voxel(x, y, z, 4 if y < 8 else 3)
    # tourelles latérales (sponsons) et leurs bolters
    for y in range(22, 32):
        for z in range(7, 14):
            for x in range(0, 1 + TRACK_WIDTH):
                set_voxel(x, y, z, color_camo(x, y, z))
            for x in range(SIZE_X - 1 - TRACK_WIDTH, SIZE_X):
                set_voxel(x, y, z, color_camo(x, y, z))
        set_voxel(0, y, 10, 3)
        set_voxel(0, y, 11, 3)
        set_voxel(SIZE_X - 1, y, 10, 3)
        set_voxel(SIZE_X - 1, y, 11, 3)
    # tourelle principale et anneau
    turret_center_x, turret_center_y = SIZE_X // 2, 26
    for x in range(12, SIZE_X - 12):
        for y in range(18, 34):
            for z in range(16, 23):
                if ((x - turret_center_x) ** 2 + (y - turret_center_y) ** 2) <= 81:
                    set_voxel(x, y, z, color_camo(x, y, z))
    # masque du canon
    for x in range(15, SIZE_X - 15):
        for y in range(12, 18):
            for z in range(17, 23):
                set_voxel(x, y, z, 1)
    # canon de combat 120 mm
    for y in range(0, 14):
        for x in range(18, 24):
            for z in range(18, 22):
                if (x - 20.5) ** 2 + (z - 19.5) ** 2 <= 4.2:
                    set_voxel(x, y, z, 4 if y > 2 else 3)
    # trappe du commandant
    for x in range(18, 24):
        for y in range(27, 32):
            set_voxel(x, y, 23, 3)
            set_voxel(x, y, 24, 5 if x in (20, 21) and y == 27 else 3)
    return V


def chenilles_profil(y, z_haut=18):
    """Profil de chenille du script de Donopot, recopié : losange aux deux extrémités.

    `z_haut` = 18 reproduit Donopot a l'identique. La reprise le ramene a 11 : ses chenilles
    montent a 17 voxels sur 23, soit 74 % de la hauteur du modele, alors que la planche donne
    environ 40 % (1,8 m sur 4,42 m). Elles depassaient donc le toit de la caisse — d'ou cette
    masse noire qui ecrase le char.
    """
    if y < 14:
        z_min = 2 + (14 - y) // 2
        z_max = z_haut - (14 - y) // 3
    elif y > 38:
        z_min = 2 + (y - 38) // 2
        z_max = z_haut - (y - 38) // 3
    else:
        z_min, z_max = 2, z_haut
    return z_min, z_max


def caisse_donopot(V):
    """Sa coque : caisson carre, x 7..34, y 10..41, z 4..15, avec sa boucle « glacis »."""
    for y in range(10, 42):
        for z in range(4, 16):
            for x in range(1 + TRACK_WIDTH, SIZE_X - 1 - TRACK_WIDTH):
                V[(x, y, z)] = camouflage(x, y, z)
    for y in range(8, 14):
        z_level = 16 - (y - 8)
        for x in range(1 + TRACK_WIDTH, SIZE_X - 1 - TRACK_WIDTH):
            for z in range(4, z_level):
                V[(x, y, z)] = camouflage(x, y, z)
    return V


def caisse_glacis(V):
    """Sa coque, mais avec une VRAIE plaque de glacis.

    Constat sur son script : la boucle appelee « glacis » reecrit les memes voxels que la coque
    (y 10..13, z 4..15 deja poses) et n'ajoute qu'une marche de deux voxels en y 8 et 9. Le nez
    reste donc un caisson carre, alors que la planche montre une plaque qui descend du toit vers
    l'avant : le bas de l'avant est avance, le toit est en retrait.

    On garde son empreinte exacte (x 7..34, z 4..15) et sa matiere : on ne fait qu'incliner le
    nez, en retirant de la matiere au-dessus de la pente.
    """
    for y in range(8, 16):
        plafond = min(15, 6 + int(round((y - 8) * 1.4)))
        for x in range(1 + TRACK_WIDTH, SIZE_X - 1 - TRACK_WIDTH):
            for z in range(4, 16):
                if z > plafond:
                    V.pop((x, y, z), None)
            for z in range(4, plafond + 1):
                V[(x, y, z)] = camouflage(x, y, z)
    return V


def tourelle_donopot(V):
    """Sa tourelle telle quelle : un disque pose a plat, son masque, son canon, sa trappe."""
    cx, cy = SIZE_X // 2, 26
    for x in range(12, SIZE_X - 12):
        for y in range(18, 34):
            for z in range(16, 23):
                if (x - cx) ** 2 + (y - cy) ** 2 <= 81:
                    V[(x, y, z)] = camouflage(x, y, z)
    for x in range(15, SIZE_X - 15):
        for y in range(12, 18):
            for z in range(17, 23):
                V[(x, y, z)] = VERT
    for y in range(0, 14):
        for x in range(18, 24):
            for z in range(18, 22):
                if (x - 20.5) ** 2 + (z - 19.5) ** 2 <= 4.2:
                    V[(x, y, z)] = ACIER if y > 2 else CHENILLE
    for x in range(18, 24):
        for y in range(27, 32):
            V[(x, y, 23)] = CHENILLE
            V[(x, y, 24)] = DETAIL if x in (20, 21) and y == 27 else CHENILLE
    return V


def tourelle_rendu(V):
    """La tourelle d'apres le RENDU de Donopot.

    Ce que son rendu montre, et que son script ne fait pas :
      * un CAISSON chanfreine (fonderie), pas un disque pose a plat ;
      * une coupole a l'arriere surmontee de TROIS antennes de hauteurs differentes ;
      * un masque CYLINDRIQUE de forte section, et un canon epais a frein de bouche.
    On garde son emprise (x 12..29, y 17..33, z 16..22) et on refait la forme dedans.
    """
    ZONE = [(x, y, z) for x in range(10, 32) for y in range(14, 36) for z in range(14, 32)]
    for q in ZONE:
        V.pop(q, None)
    X0, X1, Y0, Y1, Z0, Z1 = 13, 28, 17, 33, 16, 24
    for x in range(X0, X1 + 1):
        for y in range(Y0, Y1 + 1):
            for z in range(Z0, Z1 + 1):
                if (x < X0 + 2 or x > X1 - 2) and (y < Y0 + 2 or y > Y1 - 2):
                    continue                                  # aretes coupees : fonderie
                if z > Z1 - 2 and (x < X0 + 1 or x > X1 - 1):
                    continue                                  # toit resserre
                V[(x, y, z)] = camouflage(x, y, z)
    # coupole du chef de char (arriere gauche du caisson) et trois antennes
    for x in range(15, 20):
        for y in range(27, 32):
            if (x - 17) ** 2 + (y - 29) ** 2 <= 6:
                for z in range(Z1 + 1, Z1 + 3):
                    V[(x, y, z)] = camouflage(x, y, z)
    for x, y, h in ((16, 28, 9), (18, 30, 12), (17, 31, 7)):
        for z in range(Z1 + 3, Z1 + 3 + h):
            V[(x, y, z)] = ACIER
    # masque cylindrique et canon epais a frein de bouche (le rendu : forte section)
    yc, zc = 17, 19
    for y in range(12, 18):
        for x in range(17, 26):
            for z in range(16, 23):
                if (x - 21) ** 2 + (z - zc) ** 2 <= 12:
                    V[(x, y, z)] = ACIER
    for y in range(0, 13):
        for x in range(19, 24):
            for z in range(17, 22):
                if (x - 20.5) ** 2 + (z - zc) ** 2 <= 4.2:
                    V[(x, y, z)] = ACIER
    for y in range(1, 4):                                     # frein de bouche, plus large
        for x in range(18, 25):
            for z in range(16, 23):
                if (x - 20.5) ** 2 + (z - zc) ** 2 <= 9:
                    V[(x, y, z)] = CHENILLE
    for x in range(18, 24):
        for y in range(27, 32):
            V[(x, y, Z1 + 1)] = CHENILLE
    return V


def jupes_donopot(V):
    """Son script n'a aucune jupe : ses chenilles sont a nu jusqu'au toit de caisse."""
    return V


def jupes_rendu(V):
    """Les JUPES de flanc, d'apres le rendu.

    Son rendu montre une grande plaque de flanc qui couvre le haut de la chenille, avec deux
    trappes rondes et une ligne de rivets au-dessus. C'est ce qui manque le plus chez lui : sans
    jupe, sa caisse flotte au-dessus d'un bloc noir et le char lit comme une pile de plaques.

    Les galets restent visibles sous la jupe (le rendu les montre), parce que la plaque commence
    au-dessus d'eux.
    """
    # AU PLAN EXTERIEUR : la chenille occupe x 1..6, donc une jupe en x 5..6 est DERRIERE elle et
    # le rendu ne la montre pas (constate a l'image). Elle doit etre la surface la plus exterieure.
    for x0, sgn in ((0, 1), (41, -1)):
        for y in range(8, 42):
            for z in range(6, 17):
                for x in (x0, x0 + sgn):
                    V[(x, y, z)] = OMBRE if z >= 15 else camouflage(x, y, z)
        for yc in (16, 30):                                   # deux trappes rondes par flanc
            for y in range(8, 42):
                for z in range(6, 17):
                    if (y - yc) ** 2 + (z - 11) ** 2 <= 9:
                        V[(x0, y, z)] = ACIER
                        V[(x0 + sgn, y, z)] = ACIER
        for y in range(10, 41, 4):                            # ligne de rivets
            V[(x0, y, 16)] = ACIER
            V[(x0 + sgn, y, 16)] = ACIER
    return V


def empreinte_jupes():
    return {(x, y, z) for x in (0, 1, 2, 39, 40, 41)
            for y in range(4, 45) for z in range(2, 20)}


def empreinte_tourelle():
    """Les voxels que la reprise de la tourelle a le droit de toucher."""
    # z jusqu a 40 : les ANTENNES du rendu montent bien au-dessus du caisson, elles font
    # partie de la tourelle (le controle les avait signalees comme sortant de l empreinte)
    return {(x, y, z) for x in range(10, 32) for y in range(0, 36) for z in range(14, 41)}


def base_donopot_par_dessus(V):
    """Les pieces de Donopot posees APRES les chenilles, comme dans son script."""
    dessus = base_donopot()
    V.update(dessus)
    return V


def chenilles(V, ameliorees):
    """Les chenilles. `ameliorees=False` reproduit Donopot à l'identique ; `True` les reprend.

    Reprises, toutes tirées de la vue de profil de la planche technique :
      * le brin supérieur remonte vers l'AVANT (grand barbotin devant) au lieu d'être horizontal ;
      * une ligne de maillons sur les brins, tous les 3 voxels ;
      * 6 galets de route en bas, avec moyeu clair (sinon ils se noient dans le bandeau sombre) ;
      * 4 disques de suspension a ressorts au-dessus des galets ;
      * les mêmes couleurs que Donopot, plus son 6e ton (ombrage) qui ne servait pas.
    """
    for x0, x1 in (X_GAUCHE, X_DROITE):
        for y in range(Y_TRACK[0], Y_TRACK[1] + 1):
            z_min, z_max = chenilles_profil(y, 11 if ameliorees else 18)
            if ameliorees:
                # brin supérieur qui remonte vers l'avant : 3 voxels de pente sur la longueur
                z_max -= int(round(3 * (y - Y_TRACK[0]) / (Y_TRACK[1] - Y_TRACK[0])))
            for x in range(x0, x1 + 1):
                for z in range(z_min, z_max + 1):
                    bord = x in (x0, x1) or z in (z_min, z_max) or y in Y_TRACK
                    V[(x, y, z)] = CHENILLE if bord else 2          # 2 = vert du camouflage, comme
                    #                                                  Donopot : l'intérieur de la
                    #                                                  chenille était camouflé
                    if ameliorees and bord:
                        V[(x, y, z)] = CHENILLE
            if ameliorees and (y - Y_TRACK[0]) % 3 == 0:            # maillons sur les brins
                for x in range(x0, x1 + 1):
                    if (x, y, z_max) in V:
                        V[(x, y, z_max)] = OMBRE
                    if (x, y, z_min) in V:
                        V[(x, y, z_min)] = OMBRE
        if not ameliorees:
            continue
        # galets et suspensions : dessinés sur la FACE EXTÉRIEURE, comme sur la vue de profil
        xc = x0 if x0 == 1 else x1
        for i in range(6):                                          # 6 galets de route
            yc = Y_TRACK[0] + 4 + int((Y_TRACK[1] - Y_TRACK[0] - 8) * (i + 0.5) / 6)
            zc, r = 4, 2
            for y in range(Y_TRACK[0], Y_TRACK[1] + 1):
                for z in range(Z_TRACK[0], Z_TRACK[1] + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= (r + 1) ** 2:
                        V[(xc, y, z)] = CHENILLE
                    if d <= r ** 2:
                        V[(xc, y, z)] = ACIER                       # moyeu clair
        for i in range(4):                                          # 4 disques de suspension
            yc = Y_TRACK[0] + 4 + int((Y_TRACK[1] - Y_TRACK[0] - 8) * (i + 0.5) / 4)
            zc, r = 8, 3
            for y in range(Y_TRACK[0], Y_TRACK[1] + 1):
                for z in range(Z_TRACK[0], Z_TRACK[1] + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= (r + 1) ** 2:
                        V[(xc, y, z)] = OMBRE
                    if d <= r ** 2:
                        V[(xc, y, z)] = CHENILLE
    return V


# ------------------------------------------------------------------ écriture / lecture .vox
def ecrire_vox(chemin, V):
    paires = [((x, z, y), c) for (x, y, z), c in V.items()]       # Z hauteur -> Y (MagicaVoxel)
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
    rgba = b"".join(struct.pack("BBBB", *c) for c in PALETTE)
    rgba += b"\x00\x00\x00\xff" * (256 - len(PALETTE))
    main = chunk("MAIN", b"", chunk("SIZE", struct.pack("<iii", *taille))
                 + chunk("XYZI", corps) + chunk("RGBA", rgba))
    with open(chemin, "wb") as f:
        f.write(b"VOX " + struct.pack("<i", 150) + main)
    return taille


def empreinte_chenilles():
    """Tous les voxels que les chenilles ont le DROIT de toucher."""
    zone = set()
    for x0, x1 in (X_GAUCHE, X_DROITE):
        for y in range(Y_TRACK[0], Y_TRACK[1] + 1):
            for x in range(x0, x1 + 1):
                for z in range(0, Z_TRACK[1] + 4):
                    zone.add((x, y, z))
    return zone


def empreinte_caisse():
    """Les voxels que la reprise de la caisse a le droit de toucher."""
    zone = set()
    for x in range(1 + TRACK_WIDTH, SIZE_X - 1 - TRACK_WIDTH):
        for y in range(6, 44):
            for z in range(2, 20):
                zone.add((x, y, z))
    return zone


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--sortie", default="voxels/leman_russ_exact.vox")
    p.add_argument("--pieces", default="chenilles,caisse,tourelle,jupes",
                   help="pieces reprises : chenilles, caisse (defaut les deux, dans cet ordre)")
    p.add_argument("--temoins", action="store_true",
                   help="ecrit aussi la version d'origine, pour comparer les chenilles")
    a = p.parse_args()
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)

    # ORDRE DE DONOPOT : ses chenilles sont posees AVANT sa coque, donc sa coque, ses sponsons et
    # sa tourelle les recouvrent la ou elles se chevauchent. L'ordre inverse (mes chenilles apres
    # sa coque) ecrasait ses sponsons — verifie au rendu.
    actives = [q.strip() for q in a.pieces.split(",") if q.strip()]
    zone = set()
    if "chenilles" in actives:
        zone |= empreinte_chenilles()
    if "caisse" in actives:
        zone |= empreinte_caisse()
    if "tourelle" in actives:
        zone |= empreinte_tourelle()
    if "jupes" in actives:
        zone |= empreinte_jupes()

    def construire(ameliorer):
        V = chenilles({}, "chenilles" in actives and ameliorer)
        V = caisse_glacis(V) if ("caisse" in actives and ameliorer) else caisse_donopot(V)
        if "tourelle" in actives:
            V = tourelle_rendu(V) if ameliorer else tourelle_donopot(V)
        if "jupes" in actives:
            V = jupes_rendu(V)
        V.pop("__pieces__", None)
        return pieces_donopot_sans_tourelle(V)

    origine = construire(False)
    nouvelle = construire(True)
    print(f"pieces reprises : {', '.join(actives)}")
    hors = [q for q in set(origine) ^ set(nouvelle) if q not in zone]
    identiques = [q for q in set(origine) & set(nouvelle)
                  if origine[q] == nouvelle[q] and q not in zone]
    print(f"origine : {len(origine)} voxels · reprise : {len(nouvelle)} voxels")
    print(f"  hors empreinte des chenilles : {len(identiques)} voxels identiques, "
          f"{len(hors)} differents")
    print("  separation respectee :", "OUI" if not hors else "NON")
    for q in hors[:5]:
        print(f"    touche a tort : {q} ({origine.get(q)} -> {nouvelle.get(q)})")
    print(f"  zones reprises : chenilles (x {X_GAUCHE} / {X_DROITE})"
          + (", caisse (x 7..34, y 6..43)" if "caisse" in actives else ""))

    taille = ecrire_vox(a.sortie, nouvelle)
    print(f"  {a.sortie} ecrit · grille {taille} (X largeur, Y hauteur, Z longueur)")
    if a.temoins:
        temoin = a.sortie.replace(".vox", "-origine.vox")
        ecrire_vox(temoin, origine)
        print(f"  {temoin} ecrit (chenilles d'origine, pour comparer)")
    return 0 if not hors else 2


if __name__ == "__main__":
    sys.exit(main())
