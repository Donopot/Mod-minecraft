#!/usr/bin/env python3
"""Verifie la structure d'un .schem (Sponge v3) produit par extract_zones.py.

Controles : en-tete, coherence BlockData vs Width*Height*Length, palette, block entities.
Usage: python3 verify_schem.py <fichier.schem> [--top N]
"""
import sys, gzip
from collections import Counter

sys.path.insert(0, __file__.rsplit("/", 1)[0])
from extract_zones import read_nbt_bytes  # noqa: E402


def read_varints(b):
    out = []
    v = shift = 0
    for byte in b:
        v |= (byte & 0x7F) << shift
        if byte & 0x80:
            shift += 7
        else:
            out.append(v); v = shift = 0
    return out


def main():
    path = sys.argv[1]
    top = int(sys.argv[sys.argv.index("--top") + 1]) if "--top" in sys.argv else 8
    d = read_nbt_bytes(gzip.decompress(open(path, "rb").read()))
    w, h, l = d.get("Width"), d.get("Height"), d.get("Length")
    ver, dv = d.get("Version"), d.get("DataVersion")
    palette = d.get("Palette") or {}
    data = d.get("BlockData") or b""
    bes = d.get("BlockEntities") or []
    idx = read_varints(data)
    expected = w * h * l
    print(f"fichier   : {path}")
    print(f"Sponge v  : {ver}   DataVersion: {dv}")
    print(f"dimensions: {w} x {h}(h) x {l}  = {expected} blocs")
    print(f"palette   : {len(palette)} etats   PaletteMax: {d.get('PaletteMax')}")
    print(f"BlockData : {len(idx)} indices  -> {'OK' if len(idx) == expected else 'INCOHERENT'}")
    inv = {v: k for k, v in palette.items()}
    cnt = Counter(inv.get(i, f"??{i}") for i in idx)
    print(f"block entities: {len(bes)}")
    print(f"\ntop {top} blocs :")
    for state, n in cnt.most_common(top):
        print(f"  {n:9d}  {state}")
    if bes:
        print("\n3 premieres block entities :")
        for be in bes[:3]:
            print(f"  {be.get('Id')} @ {be.get('Pos')}  cles={[k for k in be if k not in ('Id','Pos')][:6]}")
    bad = [s for s in palette if not s.startswith("minecraft:") and not s.startswith("voiddynasty:")]
    if bad:
        print(f"\nATTENTION palette non-standard ({len(bad)}): {bad[:5]}")
    ok = len(idx) == expected and ver == 3 and len(palette) > 1
    print("\nVERDICT:", "PASS" if ok else "FAIL")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
