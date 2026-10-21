#!/usr/bin/env python3
"""Localise les zones CONSTRUITES d'un monde Minecraft et les regroupe en clusters.

Approche : on decomprime chaque chunk et on compte les *entrees de palette* qui correspondent
a des blocs poses par un joueur (planches, verre, beton, cuivre, coffres, rails...).
Les blocs naturels piegeux (terracotta des badlands, pierre, terre) sont volontairement EXCLUS.
Sortie : bounding boxes utilisables pour `//pos1 //pos2 //copy //schem save` dans WorldEdit.

Usage: python3 locate_builds.py <dossier_monde>
"""
import os, re, sys, zlib, glob
from collections import Counter

world = sys.argv[1] if len(sys.argv) > 1 else "world"

FRAGS = [
    "planks", "_concrete", "_concrete_powder", "_wool", "_stained_glass", "_glass", "glass_pane",
    "_bricks", "_stairs", "_slab", "carpet", "chest", "barrel", "crafting_table", "ladder", "torch",
    "lantern", "_door", "_trapdoor", "_fence", "iron_block", "gold_block", "diamond_block", "bookshelf",
    "quartz", "smooth_stone", "polished_[a-z_]+", "anvil", "furnace", "smoker", "blast_furnace",
    "campfire", "scaffolding", "rail", "_sign", "item_frame", "painting", "brewing_stand", "beacon",
    "hopper", "dropper", "dispenser", "observer", "piston", "redstone_lamp", "sea_lantern", "glowstone",
    "shroomlight", "_banner", "flower_pot", "cut_copper", "tuff_bricks", "mud_brick", "deepslate_tiles",
    "blackstone_bricks", "brick",
]
MANMADE = re.compile(rb"minecraft:([a-z_]*(?:" + b"|".join(f.encode() for f in FRAGS) + rb"))")


def chunks_of(region_path):
    with open(region_path, "rb") as f:
        data = f.read()
    if len(data) < 8192:
        return
    name = os.path.basename(region_path)
    try:
        parts = name[:-4].split(".")          # r.0.0.mca -> ['r','0','0']
        rx, rz = int(parts[1]), int(parts[2])
    except Exception:
        return
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
        cnt = data[i * 4 + 3]
        if not off or not cnt:
            continue
        s = off * 4096
        if s + 5 > len(data):
            continue
        ln = int.from_bytes(data[s:s + 4], "big")
        comp = data[s + 4]
        payload = data[s + 5:s + 4 + ln]
        if ln <= 0 or ln > 8_000_000:
            continue
        try:
            raw = zlib.decompress(payload) if comp == 2 else (payload if comp == 1 else None)
        except Exception:
            raw = None
        if raw is None:
            continue
        hits = MANMADE.findall(raw)
        if len(hits) >= 10:                   # 10 blocs artificiels distincts dans la palette
            cx, cz = rx * 32 + (i % 32), rz * 32 + (i // 32)
            yield cx, cz, len(hits), Counter(h.decode() for h in hits)


built = {}
for region in glob.glob(os.path.join(world, "dimensions", "minecraft", "overworld", "region", "*.mca")):
    for cx, cz, n, ids in chunks_of(region):
        built[(cx, cz)] = (n, ids)
print(f"chunks construits detectes (>=10 blocs artificiels distincts): {len(built)}")

seen, clusters = set(), []
for start in built:
    if start in seen:
        continue
    stack, comp = [start], []
    seen.add(start)
    while stack:
        cur = stack.pop()
        comp.append(cur)
        x, z = cur
        for dx in range(-3, 4):
            for dz in range(-3, 4):
                nb = (x + dx, z + dz)
                if nb in built and nb not in seen:
                    seen.add(nb)
                    stack.append(nb)
    clusters.append(comp)

clusters.sort(key=len, reverse=True)
print(f"zones (clusters) detectees: {len(clusters)}\n")

print("=== TOP 12 zones construites (candidat schematics) ===")
zone_rows = []
for idx, comp in enumerate(clusters[:12], 1):
    xs = [c[0] for c in comp]
    zs = [c[1] for c in comp]
    x1, x2 = min(xs) * 16, (max(xs) + 1) * 16
    z1, z2 = min(zs) * 16, (max(zs) + 1) * 16
    blocks = Counter()
    for c in comp:
        blocks.update(built[c][1])
    top = ", ".join(f"{k}x{v}" for k, v in blocks.most_common(4))
    zone_rows.append((idx, len(comp), x1, x2, z1, z2, top))
    print(f"  #{idx:<2d} {len(comp):5d} chunks  X[{x1}..{x2}] Z[{z1}..{z2}]  ({x2-x1}x{z2-z1})")
    print(f"        {top}")

tot = Counter()
for comp in clusters:
    for c in comp:
        tot.update(built[c][1])
print("\n=== blocs artificiels dominants (toutes zones) ===")
for k, v in tot.most_common(12):
    print(f"  {v:6d}  {k}")

# sortie machine-readable pour le plan
import json
print("\nJSON:", json.dumps([{"zone": r[0], "chunks": r[1], "x1": r[2], "x2": r[3], "z1": r[4], "z2": r[5]}
                            for r in zone_rows]))
