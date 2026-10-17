#!/usr/bin/env python3
"""work_scan.py — scan d'une zone du monde et détection des dangers avant chantier.

Première brique de l'agent de chantier (docs/AGENT-CHANTIER.md, §2 et §4).
**Lecture seule** : ne modifie ni le monde ni les fichiers.

Usage :
    work_scan.py <monde> <x1> <y1> <z1> <x2> <y2> <z2> [--json sortie.json] [--dim overworld]

Exemple :
    work_scan.py /opt/data/workspace/mc-server/world8 1200 150 1200 1253 273 1273

Lecteur NBT/PalettedContainer repris de titan_parse.py, qui a déjà lu et vérifié
70 235 blocs sur une sauvegarde réelle : ne pas réécrire, ça marche.
"""
import collections
import gzip
import json
import os
import struct
import sys
import zlib

MASK64 = 0xFFFFFFFFFFFFFFFF
SKIP = ('minecraft:air', 'minecraft:cave_air', 'minecraft:void_air')

# ---- dangers : (motif, catégorie) — réactions dans REACTION ----
DANGERS = [
    ('minecraft:bedrock', 'incassable'), ('minecraft:barrier', 'incassable'),
    ('minecraft:command_block', 'incassable'), ('minecraft:structure_block', 'incassable'),
    ('minecraft:jigsaw', 'incassable'), ('minecraft:end_portal', 'incassable'),
    ('minecraft:lava', 'lave'), ('minecraft:magma_block', 'lave'),
    ('minecraft:water', 'eau'), ('minecraft:bubble_column', 'eau'),
    ('minecraft:sand', 'gravite'), ('minecraft:red_sand', 'gravite'),
    ('minecraft:gravel', 'gravite'), ('minecraft:suspicious_sand', 'gravite'),
    ('minecraft:concrete_powder', 'gravite'), ('minecraft:anvil', 'gravite'),
    ('minecraft:scaffolding', 'gravite'),
    ('minecraft:spawner', 'spawner'), ('minecraft:trial_spawner', 'spawner'),
    ('minecraft:vault', 'spawner'),
    ('minecraft:chest', 'butin'), ('minecraft:trapped_chest', 'butin'),
    ('minecraft:barrel', 'butin'), ('minecraft:shulker_box', 'butin'),
    ('minecraft:hopper', 'butin'), ('minecraft:dispenser', 'butin'),
    ('minecraft:dropper', 'butin'),
    ('minecraft:furnace', 'bloc_entite'), ('minecraft:blast_furnace', 'bloc_entite'),
    ('minecraft:smoker', 'bloc_entite'), ('minecraft:brewing_stand', 'bloc_entite'),
    ('minecraft:torch', 'support'), ('minecraft:wall_torch', 'support'),
    ('minecraft:lantern', 'support'), ('minecraft:rail', 'support'),
    ('minecraft:ladder', 'support'), ('minecraft:cactus', 'support'),
    ('minecraft:sugar_cane', 'support'), ('minecraft:chorus_flower', 'support'),
    ('minecraft:vine', 'support'), ('minecraft:sign', 'support'),
    ('minecraft:flower_pot', 'support'), ('minecraft:lever', 'support'),
    ('minecraft:redstone_wire', 'support'), ('minecraft:tripwire', 'support'),
    ('voiddynasty:promethium_furnace', 'machine'),
    ('voiddynasty:promethium_blast_furnace', 'machine'),
    ('voiddynasty:promethium_smoker', 'machine'),
    ('voiddynasty:promethium_brewing_stand', 'machine'),
    ('voiddynasty:mining_rig', 'machine'),
    ('voiddynasty:super_chest', 'machine'),
    ('voiddynasty:super_container', 'machine'),
]

REACTION = {
    'incassable': 'marquer et contourner, ne jamais boucler',
    'lave': "ne jamais ouvrir une poche sans être au-dessus d'elle",
    'eau': 'cloisonner avant de creuser, sinon la fouille se noie',
    'gravite': 'miner par couches descendantes, soutenir au-dessus',
    'spawner': 'casser en priorité : repeuple la zone pendant le chantier',
    'butin': 'ouvrir, inventorier, mettre de côté sans consommer',
    'bloc_entite': 'casser proprement, récupérer le contenu',
    'support': 'casser le support fait tomber l\'objet : ramasser',
    'machine': 'machine du mod : demander avant de détruire un atelier',
}
PREFIX = [(pat, cat) for pat, cat in DANGERS]


def classify(name):
    base = name.split('[')[0]
    for pat, cat in PREFIX:
        if base == pat or base.startswith(pat):
            return cat
    return None


# ---------------- NBT (repris de titan_parse.py, éprouvé) ----------------
def parse_payload(d, i, t):
    if t == 1:
        return struct.unpack_from('>b', d, i)[0], i + 1
    if t == 2:
        return struct.unpack_from('>h', d, i)[0], i + 2
    if t == 3:
        return struct.unpack_from('>i', d, i)[0], i + 4
    if t == 4:
        return struct.unpack_from('>q', d, i)[0], i + 8
    if t == 5:
        return struct.unpack_from('>f', d, i)[0], i + 4
    if t == 6:
        return struct.unpack_from('>d', d, i)[0], i + 8
    if t == 7:
        n = struct.unpack_from('>i', d, i)[0]
        i += 4
        return d[i:i + n], i + n
    if t == 8:
        n = struct.unpack_from('>H', d, i)[0]
        i += 2
        return d[i:i + n].decode('utf-8', 'replace'), i + n
    if t == 9:
        et = d[i]
        n = struct.unpack_from('>i', d, i + 1)[0]
        i += 5
        out = []
        for _ in range(n):
            v, i = parse_payload(d, i, et)
            out.append(v)
        return out, i
    if t == 10:
        out = {}
        while True:
            tt = d[i]
            i += 1
            if tt == 0:
                return out, i
            nl = struct.unpack_from('>H', d, i)[0]
            i += 2
            nm = d[i:i + nl].decode('utf-8', 'replace')
            i += nl
            v, i = parse_payload(d, i, tt)
            out[nm] = v
    if t == 11:
        n = struct.unpack_from('>i', d, i)[0]
        i += 4
        return list(struct.unpack_from('>%di' % n, d, i)), i + 4 * n
    if t == 12:
        n = struct.unpack_from('>i', d, i)[0]
        i += 4
        return list(struct.unpack_from('>%dq' % n, d, i)), i + 8 * n
    raise ValueError('tag inconnu %d' % t)


def parse_root(d):
    t = d[0]
    i = 1
    nl = struct.unpack_from('>H', d, i)[0]
    i += 2 + nl
    v, _ = parse_payload(d, i, t)
    return v


def read_region(path):
    raw = open(path, 'rb').read()
    out = []
    if len(raw) < 8192:
        return out
    for ci in range(1024):
        off = struct.unpack_from('>I', raw, ci * 4)[0]
        if off == 0:
            continue
        so = (off >> 8) * 4096
        if so + 5 > len(raw):
            continue
        ln = struct.unpack_from('>I', raw, so)[0]
        comp = raw[so + 4]
        blob = raw[so + 5:so + 5 + ln]
        try:
            if comp == 1:
                blob = gzip.decompress(blob)
            elif comp == 2:
                blob = zlib.decompress(blob)
            elif comp != 3:
                continue
        except Exception:
            continue
        out.append(blob)
    return out


def props_str(p):
    if not p:
        return ''
    return '[' + ','.join('%s=%s' % (k, v) for k, v in sorted(p.items())) + ']'


def palette_names(plt):
    out = []
    for p in plt:
        if isinstance(p, str):
            out.append(p)
        elif isinstance(p, dict):
            if 'id' in p:
                out.append(p['id'] + props_str(p.get('properties')))
            elif len(p) == 1 and '' in p:
                out.append(p[''])
            else:
                out.append(p.get('Name', 'minecraft:air') + props_str(p.get('Properties')))
        else:
            out.append('minecraft:air')
    return out


def region_dir(world, dim='overworld'):
    """26.3 range les régions sous dimensions/<dim>/region ; avant, sous region/."""
    for cand in (os.path.join(world, 'dimensions', 'minecraft', dim, 'region'),
                 os.path.join(world, 'region')):
        if os.path.isdir(cand) and any(f.endswith('.mca') for f in os.listdir(cand)):
            return cand
    return os.path.join(world, 'dimensions', 'minecraft', dim, 'region')


def scan(world, x1, y1, z1, x2, y2, z2, dim='overworld'):
    xa, xb = sorted((x1, x2))
    ya, yb = sorted((y1, y2))
    za, zb = sorted((z1, z2))
    census = collections.Counter()
    found = collections.defaultdict(list)
    total = 0
    rdir = region_dir(world, dim)
    lues = []
    chunks_vus = 0
    for rx in range(xa >> 9, (xb >> 9) + 1):
        for rz in range(za >> 9, (zb >> 9) + 1):
            path = os.path.join(rdir, 'r.%d.%d.mca' % (rx, rz))
            if not os.path.exists(path):
                continue
            lues.append((rx, rz))
            for blob in read_region(path):
                try:
                    root = parse_root(blob)
                except Exception:
                    continue
                if not isinstance(root, dict) or 'sections' not in root:
                    continue
                cx, cz = root.get('xPos'), root.get('zPos')
                if cx is None or cz is None:
                    continue
                ox, oz = cx * 16, cz * 16
                if ox + 15 < xa or ox > xb or oz + 15 < za or oz > zb:
                    continue
                chunks_vus += 1
                for sec in root['sections']:
                    bs = sec.get('block_states')
                    if not bs:
                        continue
                    names = palette_names(bs.get('palette') or [])
                    if not names:
                        continue
                    sy = sec.get('Y', 0) * 16
                    if sy + 15 < ya or sy > yb:
                        continue
                    n = len(names)
                    if n == 1:
                        for idx in range(4096):
                            X = ox + (idx & 15)
                            Y = sy + (idx >> 8)
                            Z = oz + ((idx >> 4) & 15)
                            if not (xa <= X <= xb and ya <= Y <= yb and za <= Z <= zb):
                                continue
                            nm = names[0]
                            if nm.split('[')[0] in SKIP:
                                continue
                            total += 1
                            cat = classify(nm)
                            if cat:
                                census['danger:' + cat + ' ' + nm.split('[')[0]] += 1
                                if len(found[cat]) < 40:
                                    found[cat].append((X, Y, Z, nm))
                            else:
                                census[nm.split('[')[0]] += 1
                        continue
                    longs = bs.get('data') or []
                    if not longs:
                        continue
                    bits = max(4, (n - 1).bit_length())
                    per = 64 // bits
                    mask = (1 << bits) - 1
                    u = [v & MASK64 for v in longs]
                    for idx in range(4096):
                        v = (u[idx // per] >> ((idx % per) * bits)) & mask
                        if v >= n:
                            continue
                        X = ox + (idx & 15)
                        Y = sy + (idx >> 8)
                        Z = oz + ((idx >> 4) & 15)
                        if not (xa <= X <= xb and ya <= Y <= yb and za <= Z <= zb):
                            continue
                        nm = names[v]
                        base = nm.split('[')[0]
                        if base in SKIP:
                            continue
                        total += 1
                        cat = classify(nm)
                        if cat:
                            census['danger:' + cat + ' ' + base] += 1
                            if len(found[cat]) < 40:
                                found[cat].append((X, Y, Z, nm))
                        else:
                            census[base] += 1
    return total, census, found, lues, rdir, chunks_vus


def main():
    if len(sys.argv) < 8:
        print(__doc__)
        return 2
    world = sys.argv[1]
    x1, y1, z1, x2, y2, z2 = map(int, sys.argv[2:8])
    dim = sys.argv[sys.argv.index('--dim') + 1] if '--dim' in sys.argv else 'overworld'
    out = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None

    total, census, found, lues, rdir, chunks = scan(world, x1, y1, z1, x2, y2, z2, dim)

    print('=== ZONE %d..%d / %d..%d / %d..%d ===' % (min(x1, x2), max(x1, x2), min(y1, y2),
                                                      max(y1, y2), min(z1, z2), max(z1, z2)))
    print('regions lues : %s' % (lues,))
    print('chunks dans la zone : %d' % chunks)
    print('blocs non-vides : %d' % total)
    print()

    par_cat = collections.Counter()
    for k, v in census.items():
        if k.startswith('danger:'):
            par_cat[k.split(' ')[0][7:]] += v
    if par_cat:
        print('=== DANGERS DETECTES ===')
        for cat, n in par_cat.most_common():
            print('  %-12s %6d blocs   -> %s' % (cat, n, REACTION.get(cat, '')))
            for (X, Y, Z, nm) in found[cat][:5]:
                print('       (%d, %d, %d) %s' % (X, Y, Z, nm))
            if len(found[cat]) > 5:
                print('       ... et d\'autres')
    else:
        print('=== AUCUN DANGER DETECTE ===')
    print()
    print('=== BLOCS LES PLUS FREQUENTS ===')
    for k, v in census.most_common(35):
        if not k.startswith('danger:'):
            print('  %-46s %7d' % (k, v))

    if out:
        json.dump({'zone': [x1, y1, z1, x2, y2, z2], 'dimension': dim, 'total': total,
                   'chunks': chunks, 'census': dict(census),
                   'dangers': {k: v for k, v in found.items()}, 'reactions': REACTION},
                  open(out, 'w', encoding='utf-8'), indent=2, ensure_ascii=False)
        print('\nJSON ecrit : %s' % out)
    return 0


if __name__ == '__main__':
    sys.exit(main())
