#!/usr/bin/env python3
"""Lecture légère d'un level.dat (NBT gzippé) sans dépendance externe.

Usage :  python3 tools/level_info.py "/chemin/vers/le monde/level.dat"

Sert à savoir d'où vient une carte (version, spawn, nom) avant de décider si elle se reprend
telle quelle ou demande une conversion.
"""
import gzip
import re
import struct
import sys

VERSIONS = {
    3463: "1.20", 3465: "1.20.1", 3700: "1.20.5/1.20.6", 3837: "1.21", 3839: "1.21.1",
    3953: "1.21.2/1.21.3", 3955: "1.21.4", 4080: "1.21.5", 4189: "1.21.6", 4325: "1.21.9",
}


def chaine(brut, cle, taille=48):
    i = brut.find(cle.encode())
    if i < 0:
        return None
    seg = brut[i:i + taille]
    return re.sub(rb"[^\x20-\x7e]", b" ", seg).decode("ascii", "ignore").strip()


def entier(brut, cle):
    i = brut.find(cle.encode())
    if i < 0:
        return None
    debut = i + len(cle)
    return struct.unpack(">i", brut[debut:debut + 4])[0]


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 1
    brut = gzip.open(sys.argv[1], "rb").read()
    print(f"fichier : {sys.argv[1]}  ({len(brut)} o décompressés)")
    for cle in ["LevelName", "LastPlayed", "Player"]:
        print(f"  {cle:12s} → {chaine(brut, cle)}")
    for cle in ["SpawnX", "SpawnY", "SpawnZ", "DataVersion"]:
        print(f"  {cle:12s} → {entier(brut, cle)}")
    dv = entier(brut, "DataVersion")
    if dv:
        print(f"  version estimée de Minecraft → {VERSIONS.get(dv, 'inconnue (DataVersion ' + str(dv) + ')')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
