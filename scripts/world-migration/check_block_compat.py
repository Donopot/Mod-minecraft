#!/usr/bin/env python3
"""Compare les palettes des .schem a la registry de blocs d'une version cible.

But : mesurer ce qui ne collera PAS lors de la migration 26.3 -> 1.21.1.
Usage: python3 check_block_compat.py <version_mc> <fichier.schem> [...]
"""
import gzip, json, sys, urllib.request
from collections import Counter

sys.path.insert(0, "/opt/data/workspace/scripts")
from extract_zones import read_nbt_bytes  # noqa: E402

UA = {"User-Agent": "hermes/1.0"}


def target_blocks(version):
    for url in (f"https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/{version}/blocks.json",
                f"https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/{version}/blocks.json.gz"):
        try:
            raw = urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60).read()
            if url.endswith(".gz"):
                import io, gzip as gz
                raw = gz.decompress(raw)
            data = json.loads(raw)
            names = {("minecraft:" + b["name"]) if not b["name"].startswith("minecraft:") else b["name"] for b in data}
            return names
        except Exception as e:
            last = e
    raise RuntimeError(f"impossible de recuperer la liste {version}: {last}")


def read_varints(b):
    out, v, shift = [], 0, 0
    for byte in b:
        v |= (byte & 0x7F) << shift
        if byte & 0x80:
            shift += 7
        else:
            out.append(v); v = shift = 0
    return out


def main():
    version = sys.argv[1]
    files = sys.argv[2:]
    known = target_blocks(version)
    print(f"registry {version}: {len(known)} blocs\n")
    tot_unknown = Counter()
    tot_blocks = 0
    for path in files:
        d = read_nbt_bytes(gzip.decompress(open(path, "rb").read()))
        palette = d.get("Palette") or {}
        idx = read_varints(d.get("BlockData") or b"")
        counts = Counter(idx)
        total = sum(counts.values())
        tot_blocks += total
        unknown = {}
        for state, i in palette.items():
            base = state.split("[", 1)[0]
            if base not in known:
                unknown[state] = counts.get(i, 0)
                tot_unknown[base] += counts.get(i, 0)
        n_unknown_blocks = sum(unknown.values())
        pct = 100.0 * n_unknown_blocks / total if total else 0
        print(f"{path.split('/')[-1]:24s} {total:9d} blocs | etats inconnus: {len(unknown):3d} "
              f"| blocs concernes: {n_unknown_blocks:8d} ({pct:.2f}%)")
        for s, n in sorted(unknown.items(), key=lambda kv: -kv[1])[:5]:
            print(f"    {n:8d}  {s}")
    print(f"\nTOTAL blocs analyses: {tot_blocks}")
    print("Blocs absents de la cible (toutes zones) :")
    for b, n in tot_unknown.most_common(15):
        print(f"  {n:9d}  {b}")
    if not tot_unknown:
        print("  aucun")


if __name__ == "__main__":
    main()
