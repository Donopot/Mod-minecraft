#!/usr/bin/env python3
"""Scanne les fichiers de region Minecraft pour compter le contenu du namespace voiddynasty.
Decompresse chaque chunk et cherche les chaines 'voiddynasty:<id>' (palettes de blocs + entites + BE).
"""
import os, re, sys, zlib, glob
from collections import Counter

world = sys.argv[1] if len(sys.argv) > 1 else "."
pat = re.compile(rb"voiddynasty:([a-z0-9_]+)")

tot_chunks = 0
chunks_with_mod = 0
ids = Counter()
per_dim = {}

for region in glob.glob(os.path.join(world, "**", "region", "*.mca"), recursive=True):
    dim = os.path.basename(os.path.dirname(os.path.dirname(region)))
    dim = os.path.join(os.path.basename(os.path.dirname(os.path.dirname(os.path.dirname(region)))), dim)
    st = per_dim.setdefault(dim, {"chunks": 0, "with_mod": 0, "hits": 0})
    with open(region, "rb") as f:
        data = f.read()
    if len(data) < 8192:
        continue
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
        cnt = data[i * 4 + 3]
        if off == 0 or cnt == 0:
            continue
        start = off * 4096
        if start + 5 > len(data):
            continue
        length = int.from_bytes(data[start:start + 4], "big")
        comp = data[start + 4]
        payload = data[start + 5:start + 4 + length]
        if length <= 0 or length > 8_000_000:
            continue
        try:
            raw = zlib.decompress(payload) if comp == 2 else (payload if comp == 1 else None)
        except Exception:
            raw = None
        if raw is None:
            continue
        tot_chunks += 1
        st["chunks"] += 1
        found = pat.findall(raw)
        if found:
            chunks_with_mod += 1
            st["with_mod"] += 1
            st["hits"] += len(found)
            ids.update(x.decode() for x in found)

print(f"chunks lus: {tot_chunks}")
print(f"chunks contenant du voiddynasty: {chunks_with_mod}")
print()
print("=== par dimension ===")
for d, s in sorted(per_dim.items(), key=lambda kv: -kv[1]["hits"]):
    if s["chunks"]:
        print(f"  {d:35s} chunks={s['chunks']:6d} avec_mod={s['with_mod']:5d} occurrences={s['hits']}")
print()
print(f"=== ids distincts: {len(ids)} ===")
for k, v in ids.most_common(40):
    print(f"  {v:6d}  voiddynasty:{k}")
