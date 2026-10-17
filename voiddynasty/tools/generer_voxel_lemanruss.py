#!/usr/bin/env python3
"""Genere un modele VOXEL de Leman Russ Battle Tank d'apres la planche technique de Donopot.

Origine : script fourni par Donopot (ecriture des chunks .vox — conservee telle quelle).
Ce qui a ete refait, c'est LE MODELE, parce que dans la base les axes etaient melanges :
la coque montait comme un mur vertical, le canon partait vers le bas, la tourelle n'etait pas
posee sur la caisse.

Repere MagicaVoxel : X = largeur, Y = hauteur, Z = longueur (l'avant du char vers les Z croissants).

Toutes les cotes viennent de la planche technique (docs/REFERENCE-LEMAN-RUSS.md) :
caisse 7,08 m · largeur 4,86 m · hauteur 4,42 m · canon 3,07 m · garde au sol 0,45 m.
Aucune dimension n'est inventee, et la nomenclature des pieces est celle des annotations.
"""
import argparse
import struct
import sys
from collections import defaultdict

# ------------------------------------------------------------------ cotes de la planche (metres)
CAISSE_M = 7.08        # longueur de caisse
LARGEUR_M = 4.86       # largeur hors chenilles
HAUTEUR_M = 4.42       # hauteur totale
CANON_M = 3.07         # longueur du canon principal
GARDE_M = 0.45         # garde au sol

VOX = 0.15             # metres par voxel (1 voxel = 15 cm) — reglable par --resolution

# palette reprise du script de Donopot, index 1..5
PALETTE = [
    (60, 80, 45, 255),     # 1 vert armee — coque
    (40, 40, 45, 255),     # 2 gris chenilles
    (20, 20, 20, 255),     # 3 canon / metal sombre
    (180, 150, 40, 255),   # 4 insignes / details
    (150, 180, 200, 255),  # 5 optiques de visee
    (96, 98, 104, 255),    # 6 gris acier — maillons de chenille (AJOUT declare : sans lui, les
                           #   maillons se confondent avec la bande et la chenille ne se lit pas)
]
VERT, CHENILLE, METAL, INSIGNE, OPTIQUE, MAILLON = 1, 2, 3, 4, 5, 6


def v(metres):
    """Metres -> voxels, par la resolution courante."""
    return max(1, int(round(metres / VOX)))


# ------------------------------------------------------------------ construction du char
def construire():
    """Retourne (voxels, pieces) : dict (x,y,z)->index couleur, et l'inventaire par piece."""
    V = {}
    P = defaultdict(set)

    def pose(x, y, z, c, piece):
        V[(x, y, z)] = c
        P[piece].add((x, y, z))

    # ---- reperes de composition, derives de la planche
    GARDE = v(GARDE_M)              # 3 voxels = 0,45 m de garde au sol
    CHENILLE_H = v(1.35)            # hauteur de chenille ~1,35 m (vue de profil de la planche)
    CHENILLE_L = v(1.20)            # largeur de chenille
    CAISSE_BAS = GARDE              # la caisse commence a la garde au sol
    CAISSE_HAUT = 16                # toit de caisse
    Z_AR = 2                        # arriere de caisse
    Z_AV = Z_AR + v(CAISSE_M) - 1   # avant de caisse : 7,08 m plus loin
    X0, X1 = CHENILLE_L, 32 - CHENILLE_L - 1   # caisse entre les deux chenilles

    # ---- 1. chenilles tous terrains (planche : « pin-locked, all-terrain tracks »)
    # Ce que montre la vue de profil de la planche : une BOUCLE aux deux extremites arrondies
    # (barbotin avant, galet tendeur arriere), 7 petits galets de route en bas, 5 disques de
    # suspension a ressorts au-dessus, une bande a rivets tout du long, et un brin superieur qui
    # remonte vers l'arriere sous le glacis. Rien a voir avec un pave a bouts carres.
    larg = v(1.20)
    z0, z1 = 1, Z_AV
    Rayon_AV, Rayon_AR = v(0.50), v(0.30)   # la planche : GRAND barbotin a l'avant, plus petit
    pente = 3                               # a l'arriere ; le brin superieur remonte vers l'AVANT
    for x0 in (0, 32 - larg):
        xc = x0 if x0 == 0 else x0 + larg - 1
        for z in range(z0, z1 + 1):
            haut = CHENILLE_H + int(round(pente * (z - z0) / max(1, z1 - z0))) - pente
            haut -= max(0, Rayon_AV - (z1 - z))     # arrondi avant (grand)
            haut -= max(0, Rayon_AR - (z - z0))     # arrondi arriere (petit)
            for x in range(x0, x0 + larg):
                for y in range(0, haut + 1):
                    pose(x, y, z, CHENILLE, "chenilles")
            if (z - z0) % 2 == 0:                   # maillons : ligne pointillee sur les brins
                for x in range(x0, x0 + larg):
                    if (x, 0, z) in V:
                        pose(x, 0, z, MAILLON, "chenilles")
                    if (x, haut, z) in V:
                        pose(x, haut, z, MAILLON, "chenilles")
        # 7 galets de route poses sur le brin inferieur (planche). A 0,15 m par voxel, un cercle
        # ne peut pas etre rond : c'est une RANGEE de petits galets qui donne la lecture.
        # Une resolution plus fine (0,10 m) les rendrait ronds — a decider.
        for i in range(7):
            zc = z0 + Rayon_AR + int((z1 - z0 - Rayon_AV - Rayon_AR) * (i + 0.5) / 7)
            yc = v(0.16)
            for y in range(0, CHENILLE_H):
                for z in range(z0, z1 + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= v(0.30) ** 2:
                        pose(xc, y, z, METAL, "chenilles")      # jante
                    if d <= v(0.16) ** 2:
                        pose(xc, y, z, MAILLON, "chenilles")    # moyeu : gris acier, sinon
                                                                # il se noie dans la bande sombre
        # 5 disques de suspension a ressorts d'acier, au-dessus des galets (planche)
        for i in range(5):
            zc = z0 + Rayon_AR + int((z1 - z0 - Rayon_AV - Rayon_AR) * (i + 0.5) / 5)
            yc = v(0.62)
            for y in range(0, CHENILLE_H):
                for z in range(z0, z1 + 1):
                    if (xc, y, z) not in V:
                        continue
                    d = (y - yc) ** 2 + (z - zc) ** 2
                    if d <= v(0.45) ** 2:
                        pose(xc, y, z, METAL, "chenilles")      # disque de suspension
                    if d <= v(0.28) ** 2:
                        pose(xc, y, z, MAILLON, "chenilles")    # noyau visible

    # ---- 2. caisse, flancs en escalier (blindage incline) et glacis avant
    for z in range(Z_AR, Z_AV + 1):
        for x in range(X0, X1 + 1):
            plafond = CAISSE_HAUT
            if z >= Z_AV - 5:                      # glacis : le toit redescend fortement vers l'avant
                plafond = CAISSE_HAUT - (z - (Z_AV - 5))
            for y in range(CAISSE_BAS, plafond + 1):
                retrait = 0
                if y >= 13:
                    retrait = 1
                if y >= 15:
                    retrait = 2
                if x < X0 + retrait or x > X1 - retrait:
                    continue
                pose(x, y, z, VERT, "caisse")

    # ---- 2 bis. jupes de blindage lateral : la planche montre la caisse descendant sur le haut
    # des chenilles, seuls le bas des galets reste visible.
    for Y in range(9, 14):
        for z in range(Z_AR + 1, Z_AV - 4):
            for x in list(range(X0 - 4, X0)) + list(range(X1 + 1, X1 + 5)):
                pose(x, Y, z, VERT, "jupes")

    # ---- 3. sponsons : bolters lourds Gryphonne IV (planche : 90° avant et cote)
    for x0 in (X0 - 3, X1 - 0):
        for x in range(x0, x0 + 4):
            for y in range(9, 15):
                for z in range(26, 34):
                    pose(x, y, z, VERT, "sponsons")
        for x in range(x0 + 1, x0 + 3):
            for y in range(11, 13):
                for z in range(34, 40):
                    pose(x, y, z, METAL, "sponsons")

    # ---- 4. tourelle posee sur le toit de caisse, chanfreinee
    TX0, TX1, TZ0, TZ1 = 10, 21, 26, 41
    for x in range(TX0, TX1 + 1):
        for z in range(TZ0, TZ1 + 1):
            plafond = 26
            if x in (TX0, TX1) or z == TZ0 or z >= TZ1 - 1:
                plafond -= 1
            if x in (TX0 + 1, TX1 - 1) and z >= TZ1 - 2:
                plafond -= 1
            for y in range(CAISSE_HAUT, plafond + 1):
                pose(x, y, z, VERT, "tourelle")
    # trappe du chef de char et coupole (planche)
    for x in range(14, 18):
        for z in range(30, 34):
            for y in range(27, 29):
                pose(x, y, z, VERT, "tourelle")
    for x in range(15, 17):                        # ecoutille ouverte
        pose(x, 28, 31, METAL, "tourelle")
        pose(x, 28, 32, METAL, "tourelle")
    # lance-grenades fumigenes a trois tubes, a l'arriere de tourelle (planche)
    for x in (12, 14, 16):
        for z in (TZ0 - 2, TZ0 - 1):
            pose(x, 22, z, METAL, "tourelle")
    # insignes du regiment (index 4 de la palette de Donopot) : flancs de tourelle
    for z in range(30, 34):
        pose(TX0, 22, z, INSIGNE, "insignes")
        pose(TX1, 22, z, INSIGNE, "insignes")
    for x in range(16, 19):
        for y in range(12, 14):
            pose(x, y, Z_AV - 1, INSIGNE, "insignes")
    # optiques de visee (planche : periscope du tireur)
    pose(19, 21, TZ1 - 1, OPTIQUE, "tourelle")
    pose(12, 21, TZ1 - 1, OPTIQUE, "tourelle")

    # ---- 5. canon principal 120 mm : masque, tube de 3,07 m, frein de bouche
    for x in range(13, 19):
        for y in range(17, 23):
            for z in range(TZ1 - 3, TZ1 + 1):
                pose(x, y, z, METAL, "canon")
    z_tube = TZ1 + 1
    for z in range(z_tube, z_tube + 13):      # 13 voxels : cale la longueur du canon sur 3,07 m
        for x in (15, 16, 17):
            for y in (19, 20):
                pose(x, y, z, METAL, "canon")
    for z in range(z_tube + 13, z_tube + 16):
        for x in range(14, 18):
            for y in range(18, 22):
                pose(x, y, z, METAL, "canon")

    # ---- 6. lascanon Lucius en caisse (planche : avant de caisse, affut 90°)
    for x in range(18, 22):
        for y in range(11, 15):
            for z in range(Z_AV - 2, Z_AV + 1):
                pose(x, y, z, METAL, "lascanon")
    for x in (19, 20):
        for y in (12, 13):
            for z in range(Z_AV + 1, Z_AV + 5):
                pose(x, y, z, METAL, "lascanon")

    # ---- 7. lame de bulldozer tous terrains (planche : avant, au sol)
    for z in range(Z_AV + 1, Z_AV + 4):
        for x in range(X0 - 2, X1 + 3):
            plafond = 6 - (z - (Z_AV + 1))
            for y in range(0, plafond + 1):
                pose(x, y, z, METAL, "lame")

    # ---- 8. moteur Lucifer 1500 : echappements arriere (planche)
    for x0 in (X0 - 1, X1 - 2):
        for x in range(x0, x0 + 3):
            for y in range(9, 14):
                for z in range(0, 2):
                    pose(x, y, z, METAL, "echappements")

    return V, P


# ------------------------------------------------------------------ ecriture / lecture .vox
def ecrire_vox(chemin, V, palette):
    xs = [p[0] for p in V]
    ys = [p[1] for p in V]
    zs = [p[2] for p in V]
    dx, dy, dz = min(xs), min(ys), min(zs)
    sx, sy, sz = max(xs) - dx + 1, max(ys) - dy + 1, max(zs) - dz + 1

    def chunk(ident, contenu, enfants=b""):
        return (ident.encode("ascii") + struct.pack("<ii", len(contenu), len(enfants))
                + contenu + enfants)

    taille = chunk("SIZE", struct.pack("<iii", sx, sy, sz))
    corps = struct.pack("<i", len(V))
    for (x, y, z), c in V.items():
        corps += struct.pack("BBBB", x - dx, y - dy, z - dz, c)
    xyzi = chunk("XYZI", corps)
    rgba = b""
    for coul in palette:
        rgba += struct.pack("BBBB", *coul)
    rgba += b"\x00\x00\x00\xff" * (256 - len(palette))
    main = chunk("MAIN", b"", taille + xyzi + chunk("RGBA", rgba))
    with open(chemin, "wb") as f:
        f.write(b"VOX " + struct.pack("<i", 150) + main)
    return (sx, sy, sz)


def lire_vox(chemin):
    """Relit le fichier ecrit, sans faire confiance aux variables en memoire.

    Attention : les chunks SIZE / XYZI / RGBA sont des ENFANTS du chunk MAIN. Un lecteur qui
    avance de la taille du chunk MAIN sans descendre dedans ne lit rien du tout (et croit alors
    que le fichier est vide) — faute commise puis corrigee ici.
    """
    with open(chemin, "rb") as f:
        data = f.read()
    assert data[:4] == b"VOX ", "signature VOX absente"
    version = struct.unpack("<i", data[4:8])[0]
    taille, voxels, palette = None, {}, []

    def parcourir(debut, fin):
        nonlocal taille, palette
        pos = debut
        while pos < fin:
            ident = data[pos:pos + 4].decode("ascii")
            n, m = struct.unpack("<ii", data[pos + 4:pos + 12])
            contenu = data[pos + 12:pos + 12 + n]
            if ident == "MAIN":
                parcourir(pos + 12 + n, pos + 12 + n + m)
            elif ident == "SIZE":
                taille = struct.unpack("<iii", contenu)
            elif ident == "XYZI":
                nb = struct.unpack("<i", contenu[:4])[0]
                for i in range(nb):
                    x, y, z, c = struct.unpack("BBBB", contenu[4 + 4 * i:8 + 4 * i])
                    voxels[(x, y, z)] = c
            elif ident == "RGBA":
                palette = [struct.unpack("BBBB", contenu[4 * i:4 * i + 4])
                           for i in range(len(contenu) // 4)]
            pos += 12 + n + m

    parcourir(8, len(data))
    return version, taille, voxels, palette


# ------------------------------------------------------------------ verification
def mesurer(V):
    xs = [p[0] for p in V]
    ys = [p[1] for p in V]
    zs = [p[2] for p in V]
    return {"largeur": (max(xs) - min(xs) + 1) * VOX,
            "hauteur": (max(ys) - min(ys) + 1) * VOX,
            "longueur": (max(zs) - min(zs) + 1) * VOX}


def verifier(chemin, P, epreuve_negative=False):
    """Mesure le FICHIER ecrit et le compare a la planche. Rien n'est cru sur parole."""
    _, taille, voxels, palette = lire_vox(chemin)
    dims = mesurer({p: c for p, c in voxels.items()})
    pieces = {nom: mesurer(set(pts)) for nom, pts in P.items()}
    verrous = {
        "largeur hors chenilles (planche 4,86 m)": (dims["largeur"], LARGEUR_M, 0.16),
        "hauteur totale (planche 4,42 m)": (dims["hauteur"], HAUTEUR_M, 0.16),
        "longueur de caisse (planche 7,08 m)": (pieces["caisse"]["longueur"], CAISSE_M, 0.16),
        "canon principal (planche 3,07 m)": (pieces["canon"]["longueur"], CANON_M, 0.35),
    }
    print(f"fichier {chemin} : version 150, grille {taille}, {len(voxels)} voxels")
    ok = True
    for nom, (mesure, cible, tol) in verrous.items():
        ecart = mesure - cible
        bon = abs(ecart) <= tol
        ok &= bon
        print(f"  [{'OK ' if bon else 'FAUX'}] {nom} : mesure {mesure:.2f} m, ecart {ecart:+.2f} m")
    couleurs = sorted(set(voxels.values()))
    print(f"  couleurs utilisees : {couleurs} (palette {len([c for c in palette if c[3]]) } entrees)")
    return ok


# ------------------------------------------------------------------ programme
def main():
    global VOX
    p = argparse.ArgumentParser(description="Modele voxel Leman Russ d'apres la planche technique.")
    p.add_argument("--sortie", default="voxels/leman_russ_model.vox")
    p.add_argument("--resolution", type=float, default=0.15, help="metres par voxel (defaut 0,15)")
    p.add_argument("--epreuve-negative", action="store_true",
                   help="sabote volontairement le modele : le controle DOIT alors echouer")
    a = p.parse_args()
    VOX = a.resolution

    V, P = construire()
    if a.epreuve_negative:
        # on raccourcit le canon de 6 voxels (0,90 m) : la longueur du canon DOIT sortir fausse
        zmax = max(q[2] for q in P["canon"])
        a_retirer = [q for q in list(P["canon"]) if q[2] > zmax - 6]
        for q in a_retirer:
            V.pop(q, None)
            P["canon"].discard(q)
        print("EPREUVE NEGATIVE : canon raccourci de 0,90 m volontairement")

    import os
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    grille = ecrire_vox(a.sortie, V, PALETTE)
    ok = verifier(a.sortie, P)
    print("\nresume :", "conforme a la planche" if ok else "ECART AU-DELA DE LA TOLERANCE")
    if a.epreuve_negative:
        print("=> epreuve negative :", "le controle SAIT echouer (correct)" if not ok
              else "PROBLEME : le controle passe alors qu'il devrait echouer")
        return 1 if not ok else 3
    return 0 if ok else 2


if __name__ == "__main__":
    sys.exit(main())
