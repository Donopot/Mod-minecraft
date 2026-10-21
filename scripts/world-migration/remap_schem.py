#!/usr/bin/env python3
"""Remappe les palettes de .schem vers une version MC cible.

- etat inconnu + substitution declaree  -> bloc de remplacement
- etat inconnu sans substitution        -> air (le collage doit se faire avec //paste -a)
- les etats connus sont conserves tels quels

Usage:
  python3 remap_schem.py --version 1.21.1 --out DIR [--subst JSON] fichier.schem [...]
  python3 remap_schem.py --version 1.21.1 --out DIR --dir SRC
"""
import argparse, gzip, json, os, sys, urllib.request

sys.path.insert(0, "/opt/data/workspace/scripts")
from extract_zones import read_nbt_bytes, comp_body, t_comp, t_list_comp, t_int, t_short, t_int_array, t_byte_array, t_string, _wrap  # noqa: E402

UA = {"User-Agent": "hermes/1.0"}

DEFAULT_SUBST = {
    "minecraft:iron_chain": "minecraft:chain",
    "minecraft:bush": "minecraft:short_grass",
    "minecraft:potent_sulfur": "minecraft:sulfur",
}


def target_blocks(version):
    url = f"https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/{version}/blocks.json"
    data = json.loads(urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60).read())
    return {("minecraft:" + b["name"]) if not b["name"].startswith("minecraft:") else b["name"] for b in data}


def read_varints(b):
    out, v, shift = [], 0, 0
    for byte in b:
        v |= (byte & 0x7F) << shift
        if byte & 0x80:
            shift += 7
        else:
            out.append(v); v = shift = 0
    return out


def varint(v, out):
    while True:
        b = v & 0x7F
        v >>= 7
        if v:
            out.append(b | 0x80)
        else:
            out.append(b)
            return


def rewrap_be(be):
    """Re-encode une block entity relue depuis un .schem (dict simple) en (type, payload) NBT."""
    out = {}
    for k, v in be.items():
        if k == "Pos" and isinstance(v, list):
            out[k] = ("intarray", t_int_array(v))
        elif k in ("Id", "id") and isinstance(v, str):
            out[k] = ("string", t_string(v))
        else:
            out[k] = _wrap(v)
    return out


def remap_file(path, out_path, known, subst):
    d = read_nbt_bytes(gzip.decompress(open(path, "rb").read()))
    palette = d.get("Palette") or {}
    blockdata = d.get("BlockData") or b""
    idx = read_varints(blockdata)

    new_palette = {}
    mapping = {}
    replaced = {}
    dropped = 0
    for state, old_i in palette.items():
        base = state.split("[", 1)[0]
        if base in known:
            target = state
        elif base in subst and subst[base].split("[", 1)[0] in known:
            target = subst[base]
            replaced[base] = replaced.get(base, 0) + 0
        else:
            target = "minecraft:air"
            dropped += 1
        if target not in new_palette:
            new_palette[target] = len(new_palette)
        mapping[old_i] = new_palette[target]

    # re-ecriture du BlockData avec la nouvelle palette
    out = bytearray()
    n = len(new_palette)
    fast = n <= 127
    for i in idx:
        v = mapping.get(i, new_palette.get("minecraft:air", 0))
        if fast:
            out.append(v)
        else:
            varint(v, out)

    # block entities : on retire celles dont le bloc n'existe plus dans la cible
    bes = []
    for be in (d.get("BlockEntities") or []):
        bid = be.get("Id") or be.get("id") or ""
        if bid.split("[", 1)[0] in known:
            bes.append(rewrap_be(be))

    root = {
        "Version": ("int", t_int(3)),
        "DataVersion": ("int", t_int(int(d.get("DataVersion", 0)))),
        "Width": ("short", t_short(d["Width"])),
        "Height": ("short", t_short(d["Height"])),
        "Length": ("short", t_short(d["Length"])),
        "Offset": ("intarray", t_int_array([0, 0, 0])),
        "PaletteMax": ("int", t_int(n)),
        "Palette": ("compound", comp_body({k: ("int", t_int(v)) for k, v in new_palette.items()})),
        "BlockData": ("bytearray", t_byte_array(bytes(out))),
        "BlockEntities": ("list", t_list_comp(bes)),
    }
    with gzip.open(out_path, "wb", compresslevel=6) as f:
        f.write(t_comp("", root))
    return {"file": os.path.basename(path), "palette_avant": len(palette), "palette_apres": n,
            "etats_supprimes": dropped, "substitutions": replaced}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--dir")
    ap.add_argument("--subst")
    ap.add_argument("files", nargs="*")
    a = ap.parse_args()

    known = target_blocks(a.version)
    subst = dict(DEFAULT_SUBST)
    if a.subst:
        subst.update(json.loads(a.subst))
    files = list(a.files)
    if a.dir:
        files += [os.path.join(a.dir, f) for f in sorted(os.listdir(a.dir)) if f.endswith(".schem")]
    os.makedirs(a.out, exist_ok=True)
    print(f"registry {a.version}: {len(known)} blocs | {len(files)} fichier(s)")
    tot_states = tot_dropped = 0
    for f in files:
        r = remap_file(f, os.path.join(a.out, os.path.basename(f)), known, subst)
        tot_states += r["etats_supprimes"]
        tot_dropped += 1
        print(f"  {r['file']:24s} palette {r['palette_avant']:4d} -> {r['palette_apres']:4d} "
              f"| etats inconnus neutralises: {r['etats_supprimes']}")
    print(f"\nTotal: {tot_dropped} fichier(s) remappes vers {a.out}")


if __name__ == "__main__":
    main()
