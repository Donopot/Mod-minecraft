#!/usr/bin/env python3
"""Extrait des zones d'un monde Minecraft 26.3 vers des fichiers .schem (Sponge Schematic v3).

Pourquoi : transplanter la map 26.3 vers un serveur 1.21.1 sans passer par 173 exports manuels
dans WorldEdit. Le .schem produit se charge avec `//schem load <nom>` puis `//paste -e`.

- Lecture directe des fichiers .mca (aucune dépendance, stdlib seule)
- Palette de section -> BlockState string, BlockData en varint (YZX)
- BlockEntities conservés (inventaires des super_chest / super_container)
- Bornes Y calculées sur le contenu reel (pas -64..320 aveugle)
- Idempotent : un .schem deja present et non vide est ignore (reprise apres interruption)

Usage:
  python3 extract_zones.py <monde> <zones.csv> <dossier_sortie> [--zone NOM] [--limit N]
"""
import os, sys, csv, gzip, zlib, json, time

# ---------------------------------------------------------------- NBT lecture

class R:
    def __init__(self, data):
        self.d = data; self.i = 0

    def u(self, n):
        v = int.from_bytes(self.d[self.i:self.i + n], "big"); self.i += n; return v

    def s(self, n):
        v = self.d[self.i:self.i + n].decode("utf-8", "replace"); self.i += n; return v

    def payload(self, t):
        import struct
        if t == 1:
            v = self.u(1)
            return v - 256 if v > 127 else v
        if t == 2:
            v = self.u(2)
            return v - 65536 if v > 32767 else v
        if t == 3:
            v = self.u(4)
            return v - 4294967296 if v > 2147483647 else v
        if t == 4:
            v = self.u(8)
            return v - 18446744073709551616 if v > 9223372036854775807 else v
        if t == 5:
            v = struct.unpack(">f", self.d[self.i:self.i + 4])[0]; self.i += 4; return v
        if t == 6:
            v = struct.unpack(">d", self.d[self.i:self.i + 8])[0]; self.i += 8; return v
        if t == 7:
            n = self.u(4); v = self.d[self.i:self.i + n]; self.i += n; return v
        if t == 8:
            n = self.u(2); return self.s(n)
        if t == 9:
            et = self.u(1); n = self.u(4); return [self.payload(et) for _ in range(n)]
        if t == 10:
            out = {}
            while True:
                tt = self.u(1)
                if tt == 0: return out
                n = self.u(2); name = self.s(n)
                out[name] = self.payload(tt)
        if t == 11: n = self.u(4); return [self.payload(3) for _ in range(n)]
        if t == 12: n = self.u(4); return [self.u(8) for _ in range(n)]
        raise ValueError(f"tag inconnu {t} @ {self.i}")


def read_nbt_bytes(raw):
    r = R(raw)
    t = r.u(1)
    if t != 10:
        raise ValueError("NBT racine non-compound")
    r.u(2)  # nom racine
    return r.payload(10)


# ---------------------------------------------------------------- NBT ecriture

TYPES = {"byte": 1, "short": 2, "int": 3, "long": 4, "float": 5, "double": 6,
         "bytearray": 7, "string": 8, "list": 9, "compound": 10, "intarray": 11, "longarray": 12}


def w_str(s):
    b = s.encode("utf-8"); return len(b).to_bytes(2, "big") + b


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
        items = [("string", t_string(str(x))) for x in range(len(items))]
        tid = "string"
    return bytes([TYPES[tid]]) + len(items).to_bytes(4, "big") + b"".join(i[1] for i in items)


def t_list_comp(items):
    """liste de compounds donnes en dict {nom: (type, payload)} -> elements sans type ni nom"""
    return t_list([("compound", comp_body(d)) for d in items])


# ---------------------------------------------------------------- monde

def read_level_dataversion(world):
    for cand in ("level.dat", os.path.join("world", "level.dat")):
        p = os.path.join(world, cand) if cand == "level.dat" else os.path.join(world, cand)
        if os.path.exists(p):
            raw = gzip.decompress(open(p, "rb").read())
            d = read_nbt_bytes(raw)
            data = d.get("Data", d)
            return int(data.get("DataVersion", 0))
    return 0


def region_path(world, cx, cz):
    rx, rz = cx >> 5, cz >> 5
    return os.path.join(world, "dimensions", "minecraft", "overworld", "region", f"r.{rx}.{rz}.mca")


def iter_region_chunks(path):
    """yield (cx, cz, nbt) pour chaque chunk valide du fichier de region."""
    try:
        data = open(path, "rb").read()
    except FileNotFoundError:
        return
    if len(data) < 8192:
        return
    parts = os.path.basename(path)[:-4].split(".")
    rx, rz = int(parts[1]), int(parts[2])
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
        if ln <= 0 or ln > 32_000_000:
            continue
        try:
            raw = gzip.decompress(payload) if comp == 1 else (zlib.decompress(payload) if comp == 2 else (payload if comp == 3 else None))
        except Exception:
            continue
        if raw is None:
            continue
        try:
            nbt = read_nbt_bytes(raw)
        except Exception:
            continue
        cx = rx * 32 + (i % 32)
        cz = rz * 32 + (i // 32)
        yield cx, cz, nbt


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
    """Acces direct a un chunk par son slot dans le fichier de region (pas de scan, pas de re-parsing)."""

    MAX_FILES = 6

    def __init__(self, world):
        self.world = world
        self.files = {}
        self.order = []
        self.chunks = {}

    def _file(self, rp):
        if rp in self.files:
            return self.files[rp]
        try:
            data = open(rp, "rb").read()
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
            return self.chunks[k]
        nbt = None
        data = self._file(region_path(self.world, cx, cz))
        if len(data) >= 8192:
            i = (cx & 31) + (cz & 31) * 32
            off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
            cnt = data[i * 4 + 3]
            if off and cnt:
                s = off * 4096
                ln = int.from_bytes(data[s:s + 4], "big")
                comp = data[s + 4]
                payload = data[s + 5:s + 4 + ln]
                if 0 < ln < 32_000_000:
                    try:
                        raw = gzip.decompress(payload) if comp == 1 else (
                            zlib.decompress(payload) if comp == 2 else (payload if comp == 3 else None))
                        if raw:
                            nbt = read_nbt_bytes(raw)
                    except Exception:
                        nbt = None
        self.chunks[k] = nbt
        return nbt



def unpack(longs, bpe, mask, n=4096):
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
    ordered = sorted(states)
    if "minecraft:air" in ordered:
        ordered.remove("minecraft:air")
        ordered.insert(0, "minecraft:air")
    palette = {s: i for i, s in enumerate(ordered)}
    fast = len(palette) <= 127 and not os.environ.get("FORCE_SLOW")   # 1 octet par bloc = varint identique
    blockdata = bytearray()
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
                n_rows = (y_hi - y_lo) * (z_hi - z_lo)
                nbt = cache.get(cx, cz)
                sec = None
                if nbt:
                    for s in sections_of(nbt):
                        if int(s.get("Y", 0)) == cy:
                            sec = s
                            break
                if sec is None:
                    if fast:
                        blockdata += bytes([air]) * (n_row * n_rows)
                    else:
                        _vints(air, n_row * n_rows, blockdata)
                    continue
                bs = sec.get("block_states") or {}
                pal = bs.get("palette") or []
                sec_states = [state_string(e) for e in pal]
                if len(sec_states) <= 1:
                    single = palette.get(sec_states[0], air) if sec_states else air
                    if fast:
                        blockdata += bytes([single]) * (n_row * n_rows)
                    else:
                        _vints(single, n_row * n_rows, blockdata)
                    continue
                bpe = max(4, (len(sec_states) - 1).bit_length())
                mask = (1 << bpe) - 1
                data = bs.get("data") or []
                raw = unpack([int(v) for v in data], bpe, mask) if data else [0] * 4096
                lut = [palette.get(s, air) for s in sec_states]
                mapped = [lut[i] if i < len(lut) else air for i in raw]
                for y in range(y_lo, y_hi):
                    yl = (y & 15) * 256
                    for z in range(z_lo, z_hi):
                        base = yl + (z & 15) * 16 + (x_lo & 15)
                        row = mapped[base:base + n_row]
                        if fast:
                            blockdata += bytes(row)
                        else:
                            for v in row:
                                varint(v, blockdata)

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
                    for k, v in be.items():
                        if k in ("x", "y", "z", "id", "keepPacked", "Pos", "Id"):
                            continue
                        d[k] = _wrap(v)
                    block_entities.append(d)

    root = {
        "Version": ("int", t_int(3)),
        "DataVersion": ("int", t_int(dataversion)),
        "Width": ("short", t_short(width)),
        "Height": ("short", t_short(height)),
        "Length": ("short", t_short(length)),
        "Offset": ("intarray", t_int_array([0, 0, 0])),
        "PaletteMax": ("int", t_int(len(palette))),
        "Palette": ("compound", comp_body({k: ("int", t_int(v)) for k, v in palette.items()})),
        "BlockData": ("bytearray", t_byte_array(bytes(blockdata))),
        "BlockEntities": ("list", t_list_comp(block_entities)),
    }
    body = t_comp("", root)
    with gzip.open(out_path, "wb", compresslevel=6) as f:
        f.write(body)
    return {"zone": zone, "width": width, "height": height, "length": length,
            "palette": len(palette), "blocks": width * height * length,
            "block_entities": len(block_entities), "bytes": os.path.getsize(out_path)}


def _vints(value, count, out):
    for _ in range(count):
        varint(value, out)



def _wrap(v):
    import struct
    if isinstance(v, bool):
        return ("byte", t_byte(1 if v else 0))
    if isinstance(v, int):
        if -(2 ** 31) <= v < 2 ** 31:
            return ("int", t_int(v))
        if -(2 ** 63) <= v < 2 ** 63:
            return ("long", int(v).to_bytes(8, "big", signed=True))
        return ("string", t_string(str(v)))
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
    return ("string", t_string(str(v)))


def _span(x1, x2, z1, z2, cx, cz):
    xs = min(x2, cx * 16 + 16) - max(x1, cx * 16)
    zs = min(z2, cz * 16 + 16) - max(z1, cz * 16)
    return max(0, xs) * max(0, zs)


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
    print(f"DataVersion du monde: {dataversion}", flush=True)
    cache = ChunkCache(world)
    rows = list(csv.DictReader(open(csv_path)))
    if only:
        rows = [r for r in rows if r["zone"] == only]
    if limit:
        rows = rows[:limit]
    done = 0
    for r in rows:
        zone = r["zone"]
        out_path = os.path.join(outdir, zone + ".schem")
        if os.path.exists(out_path) and os.path.getsize(out_path) > 200:
            print(f"[skip] {zone} deja extrait", flush=True)
            continue
        x1, x2, z1, z2 = int(r["x1"]), int(r["x2"]), int(r["z1"]), int(r["z2"])
        t0 = time.time()
        has, ymin, ymax, states = analyze_zone(cache, x1, x2, z1, z2)
        if not has:
            print(f"[vide] {zone}", flush=True)
            continue
        info = export_zone(world, cache, zone, x1, x2, z1, z2, ymin, ymax, out_path, dataversion, states)
        done += 1
        print(f"[ok] {info['zone']} {info['blocks']//1000}k blocs Y[{ymin}..{ymax}] "
              f"palette={info['palette']} BE={info['block_entities']} "
              f"{info['bytes']//1024}ko {time.time()-t0:.1f}s", flush=True)
    print(f"\nTermine: {done} zone(s) exportee(s) vers {outdir}", flush=True)


if __name__ == "__main__":
    main()
