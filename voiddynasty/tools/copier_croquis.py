#!/usr/bin/env python3
"""Copier le croquis : construire le modele voxel a partir des SILHOUETTES de la planche.

Demande de Donopot : « essai de faire un copier coller du croquis ». Methode : au lieu
d'interpreter le dessin a la main, on extrait les silhouettes des deux vues dessinees (vue de
profil et vue de dessus), on les remet a l'echelle des cotes de la planche, et on les croise.

Un voxel n'existe que s'il est DANS la silhouette de profil ET DANS celle de dessus : c'est
l'intersection des deux volumes extrudes, la reconstruction classique a deux vues.

La couleur vient de la silhouette de profil : le dessin est en niveaux de gris, plus il est
sombre (chenilles, ombres, canon) plus le voxel est metal ; plus il est clair (coque) plus il est
vert.

Usage :
  copier_croquis.py --planche docs/references/lemunruss-planche-technique.jpeg \\
                    --sortie voxels/leman_russ_model.vox [--resolution 0.15]
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generer_voxel_v2 import (CAISSE_M, CANON_M, GARDE_M, HAUTEUR_M, LARGEUR_M, PALETTE,
                              camouflage, ecrire_vox)
from PIL import Image, ImageFilter

# Boites des deux vues sur la planche (800 x 565), larges : on garde ensuite la plus grande
# forme pleine, les etiquettes de texte etant des taches separees.
VUE_PROFIL = (85, 268, 712, 562)      # bas-gauche, l'avant du char vers la DROITE
VUE_FACE = (470, 45, 795, 286)         # haut-droite : vue de FACE (la planche n'a pas de
                                       # vue de dessus — verifie au remplissage des regions)
SEUIL_ENCRE = 150                      # en dessous = trait dessine


def silhouette(crop, seuil=SEUIL_ENCRE, fermeture=3, ouverture=2):
    """Interieur de la forme dessinee.

    Deux pieges trouves en essayant :
    - le fond de la planche est blanc a 77 %, mais le CONTOUR du dessin a des trous d'un pixel :
      un remplissage depuis les bords fuit vers l'interieur et la silhouette sort a l'envers
      (c'est le trait qui ressort, pas le char). D'ou la fermeture morphologique AVANT de remplir.
    - les lignes de renvoi des etiquettes traversent le dessin : ouvertue APRES, pour les couper.
    """
    l0, h0 = crop.size
    marge = 8                              # bord blanc : le remplissage doit pouvoir demarrer
    bord = Image.new("L", (l0 + 2 * marge, h0 + 2 * marge), 255)
    bord.paste(crop, (marge, marge))
    l, h = bord.size
    encre = bord.point(lambda v: 0 if v < seuil else 255)
    # fermeture du TRAIT : sur un masque ou le trait est NOIR, dilater le noir c'est MinFilter et
    # l'eroder c'est MaxFilter. Inverse (ce que j'avais fait), la fermeture EFFACE le dessin.
    encre = encre.filter(ImageFilter.MinFilter(2 * fermeture + 1)).filter(
        ImageFilter.MaxFilter(2 * fermeture + 1))
    pe = encre.load()
    dehors = bytearray(l * h)
    pile = []
    for x in range(l):
        for y in (0, h - 1):
            if pe[x, y] >= 128 and not dehors[y * l + x]:
                dehors[y * l + x] = 1
                pile.append((x, y))
    for y in range(h):
        for x in (0, l - 1):
            if pe[x, y] >= 128 and not dehors[y * l + x]:
                dehors[y * l + x] = 1
                pile.append((x, y))
    while pile:
        x, y = pile.pop()
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < l and 0 <= ny < h and not dehors[ny * l + nx] and pe[nx, ny] >= 128:
                dehors[ny * l + nx] = 1
                pile.append((nx, ny))
    m = Image.new("L", (l, h), 0)
    mm = m.load()
    for y in range(h):
        for x in range(l):
            if not dehors[y * l + x]:
                mm[x, y] = 255
    if ouverture:
        m = m.filter(ImageFilter.MinFilter(2 * ouverture + 1)).filter(
            ImageFilter.MaxFilter(2 * ouverture + 1))
    return m.crop((marge, marge, marge + l0, marge + h0))


def plus_grande_forme(m):
    """Ne garder que la plus grande tache : ecarte les etiquettes de texte."""
    l, h = m.size
    px = m.load()
    vus = bytearray(l * h)
    meilleure = []
    for y0 in range(h):
        for x0 in range(l):
            if px[x0, y0] and not vus[y0 * l + x0]:
                tas, pile = [], [(x0, y0)]
                vus[y0 * l + x0] = 1
                while pile:
                    x, y = pile.pop()
                    tas.append((x, y))
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < l and 0 <= ny < h and not vus[ny * l + nx] and px[nx, ny]:
                            vus[ny * l + nx] = 1
                            pile.append((nx, ny))
                if len(tas) > len(meilleure):
                    meilleure = tas
    garde = Image.new("L", (l, h), 0)
    gg = garde.load()
    for x, y in meilleure:
        gg[x, y] = 255
    return garde


def recadrer_et_redimensionner(m, nx, ny):
    """Boite serree autour de la silhouette, puis mise a la grille voulue (seuil a 50 %)."""
    boite = m.getbbox()
    m = m.crop(boite)
    m = m.resize((nx, ny), Image.LANCZOS)
    return m.point(lambda v: 255 if v >= 128 else 0), boite


def construire(planche, res):
    """Croise les DEUX vues dessinees : le profil (Z,Y) et la face (X,Y).

    Une voxel n'existe que s'il est dans les deux silhouettes : c'est l'intersection des deux
    volumes extrudes. Le profil donne la hauteur a chaque longueur, la face donne la largeur a
    chaque hauteur — donc la largeur des chenilles, de la caisse et de la tourelle.
    """
    img = Image.open(planche).convert("L")
    prof = plus_grande_forme(silhouette(img.crop(VUE_PROFIL)))
    face = plus_grande_forme(silhouette(img.crop(VUE_FACE)))
    nx_l = max(1, round(CAISSE_M / res))
    ny_h = max(1, round(HAUTEUR_M / res))
    nx_w = max(1, round(LARGEUR_M / res))
    prof_g, boite_prof = recadrer_et_redimensionner(prof, nx_l, ny_h)
    face_g, boite_face = recadrer_et_redimensionner(face, nx_w, ny_h)
    larg_p = boite_prof[2] - boite_prof[0]
    haut_p = boite_prof[3] - boite_prof[1]
    larg_f = boite_face[2] - boite_face[0]
    haut_f = boite_face[3] - boite_face[1]
    print(f"vue de profil : boite {boite_prof} -> {nx_l} x {ny_h} cellules"
          f"  (rapport dessine {larg_p/haut_p:.2f}, planche {CAISSE_M/HAUTEUR_M:.2f})")
    print(f"vue de face   : boite {boite_face} -> {nx_w} x {ny_h} cellules"
          f"  (rapport dessine {larg_f/haut_f:.2f}, planche {LARGEUR_M/HAUTEUR_M:.2f})")

    pp, pf = prof_g.load(), face_g.load()
    V = {}
    for z in range(nx_l):
        for y in range(ny_h):
            if not pp[z, ny_h - 1 - y]:                # Y du dessin va vers le bas
                continue
            for x in range(nx_w):
                if pf[x, ny_h - 1 - y]:
                    V[(x, y, z)] = 1
    return V, (prof_g, face_g, prof, img)


def colorer(V, masque, source, res):
    """Couleur d'une cellule = nuance MOYENNE du dessin dans cette cellule.

    Premiere version : je lisais un seul pixel par cellule. Resultat moucheté de taches sombres,
    parce que les traits interieurs du dessin (rivets, grilles, hachures) tombaient au hasard dans
    les cellules. La moyenne par cellule donne des aplats lisibles.
    """
    boite = masque.getbbox()
    m = masque.crop(boite)
    src = source.crop(boite)
    l, h = m.size
    pm, ps = m.load(), src.load()
    nx_l, ny_h = max(z for (_, _, z) in V) + 1, max(y for (_, y, _) in V) + 1
    cell_l, cell_h = l / nx_l, h / ny_h
    couleurs = {}
    for z in range(nx_l):
        for y in range(ny_h):
            x0, x1 = int(z * cell_l), max(int(z * cell_l) + 1, int((z + 1) * cell_l))
            # Y du dessin va vers le bas, le modele monte
            y0 = int((ny_h - 1 - y) * cell_h)
            y1 = max(y0 + 1, int((ny_h - y) * cell_h))
            valeurs = [ps[px, py] for py in range(y0, min(y1, h))
                       for px in range(x0, min(x1, l)) if pm[px, py]]
            if not valeurs:
                couleurs[(y, z)] = 1
                continue
            valeurs.sort()
            nuance = valeurs[len(valeurs) // 2]              # mediane de la cellule
            # palette de Donopot : clair = camouflage de coque, moyen = acier, sombre = chenilles
            couleurs[(y, z)] = (camouflage(z, y, 0) if nuance > 190 else
                                (4 if nuance > 120 else 3))
    # Filtre de mode 3x3 : les traits interieurs du dessin (lignes de rivets, hachures) tombent
    # dans des cellules isolees et rayaient le modele. On garde la teinte DOMINANTE du voisinage,
    # ce qui conserve les grandes regions (chenilles, caisse, canon) et efface les rayures.
    lisse = {}
    for z in range(nx_l):
        for y in range(ny_h):
            voisinage = [couleurs[(yy, zz)]
                         for zz in range(max(0, z - 1), min(nx_l, z + 2))
                         for yy in range(max(0, y - 1), min(ny_h, y + 2))]
            lisse[(y, z)] = max(set(voisinage), key=voisinage.count)
    return {(x, y, z): lisse[(y, z)] for (x, y, z) in V}


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--planche", required=True)
    p.add_argument("--sortie", default="voxels/leman_russ_model.vox")
    p.add_argument("--resolution", type=float, default=0.15)
    p.add_argument("--apercu-silhouettes", default="")
    a = p.parse_args()

    V, (prof_g, face_g, prof_plein, img_src) = construire(a.planche, a.resolution)
    if not V:
        print("aucun voxel : les silhouettes n'ont pas ete extraites")
        return 2
    V = colorer(V, prof_plein, img_src.crop(VUE_PROFIL), a.resolution)
    os.makedirs(os.path.dirname(a.sortie) or ".", exist_ok=True)
    # le repere d'ecriture de v2 echange les deux derniers axes (son Y est la longueur) :
    # ici Y est la hauteur, donc on echange avant pour ne pas coucher le modele sur le flanc
    grille = ecrire_vox(a.sortie, {(x, z, y): c for (x, y, z), c in V.items()}, PALETTE)
    print(f"{a.sortie} ecrit · grille {grille} · {len(V)} voxels")
    def etendue(pts):
        xs = [q[0] for q in pts]; ys = [q[1] for q in pts]; zs = [q[2] for q in pts]
        return ((max(xs) - min(xs) + 1) * a.resolution, (max(ys) - min(ys) + 1) * a.resolution,
                (max(zs) - min(zs) + 1) * a.resolution)
    l, h, p_ = etendue(set(V))
    print(f"  gabarit modele : {l:.2f} x {h:.2f} x {p_:.2f}"
          f"   (planche {LARGEUR_M} x {HAUTEUR_M} x {CAISSE_M})")
    # recouvrement avec la silhouette dessinee : la mesure du « copier-coller »
    nx_l, ny_h = prof_g.size
    pp = prof_g.load()
    dedans = dehors = 0
    presents = {(y, z) for (_, y, z) in V}          # silhouette de profil du modele
    for z in range(nx_l):
        for y in range(ny_h):
            present = (y, z) in presents
            dessine = bool(pp[z, ny_h - 1 - y])
            if dessine and present:
                dedans += 1
            elif dessine != present:
                dehors += 1
    total = nx_l * ny_h
    print(f"  profil : {dedans}/{total} cellules en accord avec le dessin "
          f"({100.0 * dedans / total:.1f} %), {dehors} en desaccord")
    if a.apercu_silhouettes:
        prof_g.resize((prof_g.size[0] * 4, prof_g.size[1] * 4), Image.NEAREST).save(
            a.apercu_silhouettes.replace(".png", "-profil.png"))
        face_g.resize((face_g.size[0] * 4, face_g.size[1] * 4), Image.NEAREST).save(
            a.apercu_silhouettes.replace(".png", "-face.png"))
        print(f"  silhouettes ecrites : {a.apercu_silhouettes.replace('.png', '-*.png')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
