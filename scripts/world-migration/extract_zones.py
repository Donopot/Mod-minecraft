#!/usr/bin/env python3
"""Extrait des zones d'un monde Minecraft 26.3 vers des fichiers .schem (Sponge Schematic v3).

Pourquoi : transplanter la map 26.3 vers un serveur 1.21.1 sans passer par 173 exports manuels
dans WorldEdit. Sortie de prévisualisation uniquement : entités et biomes non exportés.
Ne constitue jamais une validation P1 ni une autorisation de collage.

- Lecture directe des fichiers .mca (aucune dépendance, stdlib seule)
- Palette de section -> BlockState string, BlockData en varint (YZX)
- BlockEntities conservés (inventaires des super_chest / super_container)
- Bornes Y calculées sur le contenu reel (pas -64..320 aveugle)
- Refuse de réutiliser les anciens exports : utiliser un nouveau dossier de sortie.

Usage:
  python3 extract_zones.py <monde> <zones.csv> <dossier_sortie> [--zone NOM] [--limit N]
"""
import os, sys, csv, gzip, zlib, time
from pathlib import Path
from array import array
from collections import OrderedDict
from schematic_nbt import decode, plain, payload as nbt_payload, string_bytes

# ---------------------------------------------------------------- NBT lecture

def read_nbt_bytes(raw):
    return plain(decode(raw)[1])


# ---------------------------------------------------------------- NBT ecriture

TYPES = {"byte": 1, "short": 2, "int": 3, "long": 4, "float": 5, "double": 6,
         "bytearray": 7, "string": 8, "list": 9, "compound": 10, "intarray": 11, "longarray": 12}


def w_str(s):
    return string_bytes(s)


def tag(name, value):
    """value = (type_name, payload_bytes)"""
    t, p = value
    return bytes([TYPES[t]]) + w_str(name) + p


def comp_body(d):
    """Payload d'un compound : enfants + TAG_End. Sans type ni nom (le parent les ecrit)."""
    return b"".join(tag(k, v) for k, v in d.items()) + b"\x00"


def t_comp(name, d):
    """Compound complet (type + nom + payload) : reserve a la racine."""
    return b"\x0a" + w_str(name) + comp_body(d)


def t_int(v): return int(v).to_bytes(4, "big", signed=True)


def t_short(v): return int(v).to_bytes(2, "big", signed=True)


def t_byte(v): return int(v).to_bytes(1, "big", signed=True)


def t_byte_array(b): return len(b).to_bytes(4, "big") + b


def t_int_array(vals): return len(vals).to_bytes(4, "big") + b"".join(int(v).to_bytes(4, "big", signed=True) for v in vals)


def t_string(s): return w_str(s)


def t_list(items):
    """liste homogene de (type_name, payload)"""
    if not items:
        return b"\x00\x00\x00\x00\x00"
    tid = items[0][0]
    if any(i[0] != tid for i in items):
        raise ValueError("Liste NBT hétérogène : aucune conversion silencieuse")
    return bytes([TYPES[tid]]) + len(items).to_bytes(4, "big") + b"".join(i[1] for i in items)


def t_list_comp(items):
    """liste de compounds donnes en dict {nom: (type, payload)} -> elements sans type ni nom"""
    return t_list([("compound", comp_body(d)) for d in items])


# ---------------------------------------------------------------- monde

def read_level_dataversion(world):
    for cand in ("level.dat", os.path.join("world", "level.dat")):
        p = os.path.join(world, cand) if cand == "level.dat" else os.path.join(world, cand)
        if os.path.exists(p):
            raw = gzip.decompress(Path(p).read_bytes())
            d = read_nbt_bytes(raw)
            data = d.get("Data", d)
            return int(data.get("DataVersion", 0))
    return 0


def region_path(world, cx, cz):
    rx, rz = cx >> 5, cz >> 5
    return os.path.join(world, "dimensions", "minecraft", "overworld", "region", f"r.{rx}.{rz}.mca")


AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}


def state_string(entry):
    """26.x : la palette est une liste de chaines 'minecraft:stone' / 'minecraft:oak_stairs[facing=north,...]'.
    Les anciens formats utilisaient un compound {Name, Properties} : on gere les deux."""
    if isinstance(entry, str):
        return entry
    name = entry.get("Name", "minecraft:air")
    props = entry.get("Properties") or {}
    if not props:
        return name
    inner = ",".join(f"{k}={props[k]}" for k in sorted(props))
    return f"{name}[{inner}]"


def base_id(state):
    return state.split("[", 1)[0]


class ChunkCache:
    """Accès direct par slot ; cache borné pour les grandes zones."""

    MAX_FILES = 6
    MAX_CHUNKS = 128

    def __init__(self, world):
        self.world = world
        self.files = {}
        self.order = []
        self.chunks = OrderedDict()

    def _file(self, rp):
        if rp in self.files:
            return self.files[rp]
        try:
            data = Path(rp).read_bytes()
        except FileNotFoundError:
            data = b""
        self.files[rp] = data
        self.order.append(rp)
        while len(self.order) > self.MAX_FILES:
            old = self.order.pop(0)
            self.files.pop(old, None)
        return data

    def get(self, cx, cz):
        k = (cx, cz)
        if k in self.chunks:
            self.chunks.move_to_end(k)
            return self.chunks[k]
        nbt = None
        data = self._file(region_path(self.world, cx, cz))
        if data and len(data) < 8192:
            raise ValueError(f"Région tronquée : {region_path(self.world, cx, cz)}")
        if len(data) >= 8192:
            i = (cx & 31) + (cz & 31) * 32
            off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
            cnt = data[i * 4 + 3]
            if bool(off) != bool(cnt):
                raise ValueError(f"Entrée de localisation de chunk invalide : {cx},{cz}")
            if off and cnt:
                s = off * 4096
                if s < 8192 or s + cnt * 4096 > len(data):
                    raise ValueError(f"Secteurs de chunk invalides : {cx},{cz}")
                ln = int.from_bytes(data[s:s + 4], "big")
                comp = data[s + 4]
                payload = data[s + 5:s + 4 + ln]
                if not 1 < ln <= cnt * 4096 - 4 or comp not in (1, 2, 3):
                    raise ValueError(f"Compression ou longueur de chunk non prise en charge : {cx},{cz}")
                raw = gzip.decompress(payload) if comp == 1 else (
                    zlib.decompress(payload) if comp == 2 else payload)
                nbt = read_nbt_bytes(raw)
        self.chunks[k] = nbt
        while len(self.chunks) > self.MAX_CHUNKS:
            self.chunks.popitem(last=False)
        return nbt



def unpack(longs, bpe, mask, n=4096):
    if len(longs) != (n + (64 // bpe) - 1) // (64 // bpe):
        raise ValueError("Nombre de longs de section incohérent")
    out = [0] * n
    per = 64 // bpe
    idx = 0
    for v in longs:
        for k in range(per):
            if idx >= n:
                return out
            out[idx] = (v >> (k * bpe)) & mask
            idx += 1
        if idx >= n:
            return out
    return out


def sections_of(nbt):
    return nbt.get("sections") or nbt.get("Sections") or []


def analyze_zone(cache, x1, x2, z1, z2):
    """Pass 1 : bornes Y reelles, chunks a contenu, et palette globale (sans desempiler les longs)."""
    ymin, ymax = 4096, -4096
    has_content = False
    states = set()
    cx1, cx2 = x1 >> 4, (x2 - 1) >> 4
    cz1, cz2 = z1 >> 4, (z2 - 1) >> 4
    for cx in range(cx1, cx2 + 1):
        for cz in range(cz1, cz2 + 1):
            nbt = cache.get(cx, cz)
            if not nbt:
                continue
            if nbt.get("Status", "minecraft:full") != "minecraft:full":
                continue
            for sec in sections_of(nbt):
                pal = (sec.get("block_states") or {}).get("palette") or []
                if not pal:
                    continue
                ids = {state_string(e) for e in pal}
                if ids and all(base_id(i) in AIR for i in ids):
                    continue
                states |= ids
                sy = int(sec.get("Y", 0))
                ymin = min(ymin, sy * 16)
                ymax = max(ymax, sy * 16 + 15)
                has_content = True
    return has_content, ymin, ymax, states


def varint(v, out):
    while True:
        b = v & 0x7F
        v >>= 7
        if v:
            out.append(b | 0x80)
        else:
            out.append(b)
            return


def export_zone(world, cache, zone, x1, x2, z1, z2, ymin, ymax, out_path, dataversion, states):
    width, length, height = x2 - x1, z2 - z1, ymax - ymin + 1

    # palette deterministe : air en 0, puis tri alphabetique
    ordered = ["minecraft:air"] + sorted(set(states) - {"minecraft:air"})
    palette = {s: i for i, s in enumerate(ordered)}
    fast = len(palette) <= 127 and not os.environ.get("FORCE_SLOW")   # 1 octet par bloc = varint identique
    blockdata = bytearray()
    # Store by global x + z*width + y*width*length, not by chunk traversal order.
    # A byte per cell for small palettes; fixed-width unsigned ints otherwise.
    indices = bytearray(width * height * length) if len(palette) <= 256 else array('I', [0]) * (width * height * length)
    block_entities = []

    cx1, cx2 = x1 >> 4, (x2 - 1) >> 4
    cz1, cz2 = z1 >> 4, (z2 - 1) >> 4
    air = palette.get("minecraft:air", 0)

    for cy in range(ymin >> 4, (ymax >> 4) + 1):
        y_lo, y_hi = max(cy * 16, ymin), min(cy * 16 + 16, ymax + 1)
        for cz in range(cz1, cz2 + 1):
            z_lo, z_hi = max(z1, cz * 16), min(z2, cz * 16 + 16)
            if z_hi <= z_lo:
                continue
            for cx in range(cx1, cx2 + 1):
                x_lo, x_hi = max(x1, cx * 16), min(x2, cx * 16 + 16)
                if x_hi <= x_lo:
                    continue
                n_row = x_hi - x_lo
                nbt = cache.get(cx, cz)
                sec = None
                if nbt:
                    for s in sections_of(nbt):
                        if int(s.get("Y", 0)) == cy:
                            sec = s
                            break
                if sec is None:
                    continue
                bs = sec.get("block_states") or {}
                pal = bs.get("palette") or []
                sec_states = [state_string(e) for e in pal]
                if len(sec_states) <= 1:
                    mapped = [palette[sec_states[0]] if sec_states else air] * 4096
                else:
                    bpe = max(4, (len(sec_states) - 1).bit_length())
                    mask = (1 << bpe) - 1
                    data = bs.get("data") or []
                    raw = unpack([int(v) for v in data], bpe, mask)
                    lut = [palette[s] for s in sec_states]
                    if any(i >= len(lut) for i in raw):
                        raise ValueError("Index de palette de section inconnu")
                    mapped = [lut[i] for i in raw]
                for y in range(y_lo, y_hi):
                    yl = (y & 15) * 256
                    for z in range(z_lo, z_hi):
                        base = yl + (z & 15) * 16 + (x_lo & 15)
                        row = mapped[base:base + n_row]
                        dest = (x_lo-x1) + (z-z1)*width + (y-ymin)*width*length
                        indices[dest:dest+n_row] = bytes(row) if isinstance(indices, bytearray) else array('I', row)

    if fast:
        blockdata = bytes(indices)
    else:
        for index in indices:
            varint(index, blockdata)
    del indices

    # block entities
    for cx in range(cx1, cx2 + 1):
        for cz in range(cz1, cz2 + 1):
            nbt = cache.get(cx, cz)
            if not nbt:
                continue
            for be in (nbt.get("block_entities") or nbt.get("BlockEntities") or []):
                bx, by, bz = be.get("x"), be.get("y"), be.get("z")
                if bx is None:
                    continue
                if x1 <= bx < x2 and z1 <= bz < z2 and ymin <= by <= ymax:
                    d = {"Id": ("string", t_string(be.get("id", "minecraft:chest"))),
                         "Pos": ("intarray", t_int_array([bx - x1, by - ymin, bz - z1]))}
                    extra = {}
                    for k, v in be.items():
                        if k in ("x", "y", "z", "id", "keepPacked", "Pos", "Id"):
                            continue
                        extra[k] = _wrap(v)
                    d['Data'] = ('compound', comp_body(extra))
                    block_entities.append(d)

    root = {
        "Version": ("int", t_int(3)),
        "DataVersion": ("int", t_int(dataversion)),
        "Width": ("short", t_short(width)),
        "Height": ("short", t_short(height)),
        "Length": ("short", t_short(length)),
        "Offset": ("intarray", t_int_array([0, 0, 0])),
        "Metadata": ("compound", comp_body({
            "VoidDynastyExtractionComplete": ("byte", t_byte(0)),
            "MissingData": ("string", t_string("Entities,Biomes; P1 pending")),
            "SourceOrigin": ("intarray", t_int_array([x1, ymin, z1])),
        })),
        "Blocks": ("compound", comp_body({
            "Palette": ("compound", comp_body({k: ("int", t_int(v)) for k, v in palette.items()})),
            "Data": ("bytearray", t_byte_array(bytes(blockdata))),
            "BlockEntities": ("list", t_list_comp(block_entities)),
        })),
    }
    body = t_comp("", {"Schematic": ("compound", comp_body(root))})
    with open(out_path, "xb") as destination:
        with gzip.GzipFile(fileobj=destination, mode="wb", compresslevel=6) as f:
            f.write(body)
    return {"zone": zone, "width": width, "height": height, "length": length,
            "palette": len(palette), "blocks": width * height * length,
            "block_entities": len(block_entities), "bytes": os.path.getsize(out_path)}


def _wrap(v):
    import struct
    if hasattr(v, 'nbt_tag'):
        names = {value:key for key,value in TYPES.items()}
        return names[v.nbt_tag.kind], nbt_payload(v.nbt_tag)
    if isinstance(v, bool):
        return ("byte", t_byte(1 if v else 0))
    if isinstance(v, int):
        if -(2 ** 31) <= v < 2 ** 31:
            return ("int", t_int(v))
        if -(2 ** 63) <= v < 2 ** 63:
            return ("long", int(v).to_bytes(8, "big", signed=True))
        raise ValueError("Entier hors limites NBT")
    if isinstance(v, float):
        return ("double", struct.pack(">d", v))
    if isinstance(v, str):
        return ("string", t_string(v))
    if isinstance(v, (bytes, bytearray)):
        return ("bytearray", t_byte_array(bytes(v)))
    if isinstance(v, list):
        return ("list", t_list([_wrap(x) for x in v]))
    if isinstance(v, dict):
        return ("compound", comp_body({k: _wrap(x) for k, x in v.items()}))
    raise ValueError("Type NBT inconnu")


def main():
    world, csv_path, outdir = sys.argv[1], sys.argv[2], sys.argv[3]
    only = None
    limit = None
    args = sys.argv[4:]
    for i, a in enumerate(args):
        if a == "--zone": only = args[i + 1]
        if a == "--limit": limit = int(args[i + 1])
    os.makedirs(outdir, exist_ok=True)
    dataversion = read_level_dataversion(world)
    if not dataversion:
        raise ValueError("DataVersion source introuvable ; aucun export généré")
    print("PREVIEW ONLY: entités et biomes non exportés. P1 NON VALIDÉE.", flush=True)
    print(f"DataVersion du monde: {dataversion}", flush=True)
    cache = ChunkCache(world)
    with open(csv_path, encoding="utf-8", newline="") as source:
        rows = list(csv.DictReader(source))
    if only:
        rows = [r for r in rows if r["zone"] == only]
    if limit:
        rows = rows[:limit]
    done = 0
    for r in rows:
        zone = r["zone"]
        out_path = os.path.join(outdir, zone + ".schem")
        if os.path.exists(out_path):
            raise ValueError(f"{zone} existe déjà ; conserver l'original et utiliser un nouveau dossier")
        x1, x2, z1, z2 = int(r["x1"]), int(r["x2"]), int(r["z1"]), int(r["z2"])
        t0 = time.time()
        has, ymin, ymax, states = analyze_zone(cache, x1, x2, z1, z2)
        if not has:
            print(f"[A VERIFIER] {zone} sans contenu lu ; ne prouve pas une zone vide", flush=True)
            continue
        info = export_zone(world, cache, zone, x1, x2, z1, z2, ymin, ymax, out_path, dataversion, states)
        done += 1
        print(f"[ok] {info['zone']} {info['blocks']//1000}k blocs Y[{ymin}..{ymax}] "
              f"palette={info['palette']} BE={info['block_entities']} "
              f"{info['bytes']//1024}ko {time.time()-t0:.1f}s", flush=True)
    print(f"\nTermine: {done} zone(s) exportee(s) vers {outdir}", flush=True)


if __name__ == "__main__":
    main()
