#!/usr/bin/env python3
"""Localise les zones CONSTRUITES d'un monde Minecraft, les regroupe en clusters et exporte un CSV.

Usage: python3 locate_builds.py <dossier_monde> [chemin_csv]

Approche : on decomprime chaque chunk et on compte les *entrees de palette* correspondant a des blocs
poses par un joueur (planches, verre, beton, cuivre, coffres, rails, tuff bricks...). Les blocs
naturels piegeux (terracotta des badlands) sont volontairement EXCLUS. Sortie : bounding boxes
utilisables directement pour `//pos1 //pos2 //copy //schem save` dans WorldEdit.
"""
import os, re, sys, csv, zlib, glob, json
from collections import Counter

world = sys.argv[1] if len(sys.argv) > 1 else "world"
out_csv = sys.argv[2] if len(sys.argv) > 2 else None

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
print(f"chunks construits detectes: {len(built)}")

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
print(f"zones (clusters) detectees: {len(clusters)}")

rows = []
for idx, comp in enumerate(clusters, 1):
    xs = [c[0] for c in comp]
    zs = [c[1] for c in comp]
    x1, x2 = min(xs) * 16, (max(xs) + 1) * 16
    z1, z2 = min(zs) * 16, (max(zs) + 1) * 16
    blocks = Counter()
    for c in comp:
        blocks.update(built[c][1])
    sig = " ".join(f"{k}={v}" for k, v in blocks.most_common(5))
    rows.append({
        "zone": f"zone_{idx:03d}",
        "chunks": len(comp),
        "x1": x1, "x2": x2, "z1": z1, "z2": z2,
        "y1": -64, "y2": 320,
        "size_x": x2 - x1, "size_z": z2 - z1,
        "blocs_dominants": sig,
    })

print("\n=== TOP 10 ===")
for r in rows[:10]:
    print(f"  {r['zone']} {r['chunks']:5d} chunks X[{r['x1']}..{r['x2']}] Z[{r['z1']}..{r['z2']}]  {r['blocs_dominants'][:70]}")

if out_csv:
    with open(out_csv, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)
    print(f"\nCSV ecrit: {out_csv} ({len(rows)} lignes)")
else:
    print("\nJSON:", json.dumps(rows[:3], ensure_ascii=False))
