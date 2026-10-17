#!/usr/bin/env python3
"""Inventaire des blocs d'un monde Minecraft, en lisant les fichiers de région.

Usage :  python3 tools/region_blocks.py "/chemin/du monde" [--regions 8]

Pourquoi : savoir si une carte est faite de blocs **vanilla** (donc reprenable telle quelle) ou de
blocs d'un mod (donc dépendante de ce mod). Un simple `strings` sur un `.mca` ne répond pas : les
chunks sont compressés en zlib à l'intérieur. Ce script décompresse réellement.

Format d'un `.mca` : en-tête de 8 Ko (1 024 emplacements de 4 octets), puis, à l'offset indiqué,
4 octets de longueur + 1 octet de compression (2 = zlib) + les données NBT du chunk.
"""
import argparse
import collections
import glob
import os
import re
import struct
import sys
import zlib


def blocs_du_fichier(chemin, limite_chunks=None):
    with open(chemin, "rb") as fh:
        entete = fh.read(4096)
        trouve = collections.Counter()
        lus = 0
        for i in range(1024):
            brut = entete[i * 4:i * 4 + 3]
            if not brut:
                break
            decalage = struct.unpack(">I", b"\x00" + entete[i * 4:i * 4 + 3])[0]
            if decalage == 0:
                continue
            fh.seek(decalage * 4096)
            tete = fh.read(5)
            if len(tete) < 5:
                continue
            longueur = struct.unpack(">I", tete[:4])[0]
            compression = tete[4]
            donnees = fh.read(longueur - 1)
            try:
                if compression == 2:
                    donnees = zlib.decompress(donnees)
                elif compression != 3:      # 1 = gzip (rare), 3 = non compressé
                    continue
            except zlib.error:
                continue
            for nom in re.findall(rb"[a-z0-9_]{2,}:[a-z0-9_/]{2,}", donnees):
                trouve[nom.decode("ascii", "ignore")] += 1
            lus += 1
            if limite_chunks and lus >= limite_chunks:
                break
        return trouve, lus


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("monde")
    ap.add_argument("--regions", type=int, default=8, help="nombre de régions à échantillonner")
    ap.add_argument("--chunks", type=int, default=120, help="chunks lus par région")
    args = ap.parse_args()

    fichiers = sorted(glob.glob(os.path.join(args.monde, "region/*.mca")))[:args.regions]
    if not fichiers:
        print(f"aucune région trouvée dans {args.monde}/region")
        return 1
    total = collections.Counter()
    chunks = 0
    for f in fichiers:
        trouve, lus = blocs_du_fichier(f, args.chunks)
        total.update(trouve)
        chunks += lus
        print(f"  {os.path.basename(f):16s} {lus:4d} chunks lus")

    espaces = collections.Counter()
    for nom, n in total.items():
        espaces[nom.split(":")[0]] += n
    print(f"\nchunks analysés : {chunks}")
    print("espaces de noms rencontrés :")
    for ns, n in espaces.most_common(12):
        marque = "  (vanilla)" if ns == "minecraft" else "  ← HORS VANILLA"
        print(f"  {ns:22s} {n:8d}{marque}")

    hors = {ns: n for ns, n in espaces.items() if ns != "minecraft"}
    print()
    if hors:
        print("VERDICT : la carte dépend de contenus externes :", sorted(hors))
    else:
        print("VERDICT : uniquement des blocs/entités vanilla → la carte se reprend sans le mod source.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
