#!/usr/bin/env python3
"""Remap explicitly approved block states; never drops unknown blocks or block entities.

Requires exact target states (reports/blocks.json including custom mod states), not
vanilla-only block names. Inventory/entity/biome downgrades still require P5/P10.
"""
import argparse
from collections import Counter
import gzip
import json
from pathlib import Path
import zlib
from schematic_nbt import Tag, encode, load, validate, varints


def canonical(state):
    if '[' not in state:
        return state
    base, properties = state.split('[', 1)
    if not properties.endswith(']'):
        raise ValueError('Malformed block state: ' + state)
    pairs = properties[:-1].split(',')
    if any('=' not in pair for pair in pairs):
        raise ValueError('Malformed block properties: ' + state)
    if len({pair.split('=',1)[0] for pair in pairs}) != len(pairs):
        raise ValueError('Duplicate block property: ' + state)
    return base + '[' + ','.join(sorted(pairs)) + ']'


def target_states(path):
    report = json.loads(Path(path).read_text(encoding='utf-8'))
    result = set()
    for block, info in report.items():
        if ':' not in block or not isinstance(info, dict) or not info.get('states'):
            raise ValueError('Expected reports/blocks.json with exact states')
        for state in info['states']:
            props = state.get('properties', {})
            result.add(canonical(block + ('[' + ','.join(k+'='+v for k,v in props.items()) + ']' if props else '')))
    return result


def write_varint(value, out):
    while value >= 128:
        out.append((value & 127) | 128)
        value >>= 7
    out.append(value)


def remap_file(path, out_path, known, subst):
    path, out_path = Path(path), Path(out_path)
    if path.resolve() == out_path.resolve() or out_path.exists():
        raise ValueError('Refusing to overwrite a source or existing export')
    name, root = load(path)
    summary = validate(root)
    schematic = root.value['Schematic']
    blocks = schematic.value['Blocks']
    palette = blocks.value['Palette'].value
    known = {canonical(state) for state in known}
    subst = {canonical(source):canonical(target) for source,target in subst.items()}
    targets, unknown = {}, []
    for state, old_index in palette.items():
        source = canonical(state)
        target = subst.get(source, source)
        if target not in known:
            unknown.append(state)
        targets[old_index.value] = target
    if unknown:
        raise ValueError('Unknown target states; original retained, no output: ' + ', '.join(unknown))
    air = {'minecraft:air','minecraft:cave_air','minecraft:void_air'}
    for source, index in palette.items():
        if source.split('[',1)[0] not in air and targets[index.value].split('[',1)[0] in air:
            raise ValueError('Destructive air substitution refused: ' + source)
    positions = set()
    entries = blocks.value.get('BlockEntities')
    if entries:
        width, _, length = summary['dimensions']
        for entry in entries.value[1]:
            x,y,z = entry.value['Pos'].value
            positions.add(x + z*width + y*width*length)
    original_states = {index.value:state for state,index in palette.items()}
    new_palette, mapping = {}, {}
    for old_index, state in targets.items():
        new_palette.setdefault(state, len(new_palette))
        mapping[old_index] = new_palette[state]
    output, replacements = bytearray(), Counter()
    for position, index in enumerate(varints(blocks.value['Data'].value)):
        old, target = canonical(original_states[index]), targets[index]
        if old != target:
            if position in positions:
                raise ValueError('Block entity substitution requires an NBT adapter: ' + old)
            replacements[old + ' -> ' + target] += 1
        write_varint(mapping[index], output)
    # Rebuild only changed ancestors; all other serialized payloads remain intact.
    new_blocks = dict(blocks.value)
    new_blocks['Palette'] = Tag(10, {state:Tag(3,index) for state,index in new_palette.items()})
    new_blocks['Data'] = Tag(7, bytes(output))
    new_schematic = dict(schematic.value)
    new_schematic['Blocks'] = Tag(10, new_blocks)
    new_root = dict(root.value)
    new_root['Schematic'] = Tag(10, new_schematic)
    changed = Tag(10, new_root)
    validate(changed)
    with out_path.open('xb') as destination:
        with gzip.GzipFile(fileobj=destination, mode='wb', mtime=0) as compressed:
            compressed.write(encode(name, changed))
    return {'file':path.name, 'palette_before':len(palette), 'palette_after':len(new_palette),
            'substitutions':dict(replacements), 'block_entities_preserved':summary['block_entities'],
            'data_version_changed':False, 'p1_pass':False, 'paste_allowed':False}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--version', required=True, choices=['1.21.1'])
    parser.add_argument('--registry', required=True, help='Target reports/blocks.json including mod states')
    parser.add_argument('--out', required=True)
    parser.add_argument('--dir')
    parser.add_argument('--subst', help='Path to reviewed JSON map of exact source -> target states')
    parser.add_argument('files', nargs='*')
    args = parser.parse_args()
    known = target_states(args.registry)
    subst = json.loads(Path(args.subst).read_text(encoding='utf-8')) if args.subst else {}
    files = list(map(Path,args.files))
    if args.dir:
        files.extend(sorted(Path(args.dir).glob('*.schem')))
    if not files:
        parser.error('No schematics supplied')
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    failed = False
    for source in files:
        try:
            print(json.dumps(remap_file(source, out/source.name, known, subst), ensure_ascii=False))
        except (ValueError, OSError, EOFError, zlib.error) as error:
            print(json.dumps({'file':source.name, 'error':str(error), 'p1_pass':False}, ensure_ascii=False))
            failed = True
    return 1 if failed else 0


if __name__ == '__main__':
    raise SystemExit(main())
