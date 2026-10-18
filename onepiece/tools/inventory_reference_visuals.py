"""Inspect reference ZIP/JAR resources and class constants without loading their code.

Class strings are search evidence, not proof that a particular method executes.
Outputs geometry/animation pairing, texture candidates and Photon dependencies.
"""
from pathlib import Path
import argparse, collections, gzip, hashlib, io, json, re, struct, zipfile


def class_strings(data):
    stream = io.BytesIO(data)
    if stream.read(4) != b'\xca\xfe\xba\xbe':
        return []
    stream.read(4)
    count = struct.unpack('>H', stream.read(2))[0]
    index, strings = 1, []
    widths = {3:4, 4:4, 5:8, 6:8, 7:2, 8:2, 9:4, 10:4, 11:4,
              12:4, 15:3, 16:2, 17:4, 18:4, 19:2, 20:2}
    while index < count:
        tag = stream.read(1)[0]
        if tag == 1:
            length = struct.unpack('>H', stream.read(2))[0]
            strings.append(stream.read(length).decode('utf-8', errors='replace'))
        else:
            stream.read(widths[tag])
            if tag in (5, 6):
                index += 1
        index += 1
    return strings


def nbt_strings(data):
    """Read standard big-endian Photon NBT; retain strings only, never evaluate expressions."""
    stream = io.BytesIO(gzip.decompress(data) if data.startswith(b'\x1f\x8b') else data)
    strings = []
    def number(fmt):
        return struct.unpack('>'+fmt, stream.read(struct.calcsize('>'+fmt)))[0]
    def string():
        value = stream.read(number('H')).decode('utf-8', errors='replace')
        strings.append(value)
        return value
    def payload(tag, depth=0):
        if depth > 80:
            raise ValueError('NBT nesting too deep')
        if tag in (1, 2, 3, 4, 5, 6):
            stream.read({1:1, 2:2, 3:4, 4:8, 5:4, 6:8}[tag])
        elif tag in (7, 11, 12):
            size = number('i')
            if not 0 <= size <= 10_000_000:
                raise ValueError('Invalid NBT array length')
            stream.read(size*{7:1, 11:4, 12:8}[tag])
        elif tag == 8:
            string()
        elif tag == 9:
            item, size = number('B'), number('i')
            if not 0 <= size <= 100_000:
                raise ValueError('Invalid NBT list length')
            for _ in range(size):
                payload(item, depth+1)
        elif tag == 10:
            while True:
                item = number('B')
                if item == 0:
                    break
                string()
                payload(item, depth+1)
        else:
            raise ValueError('Unknown NBT tag '+str(tag))
    root = number('B')
    string()
    payload(root)
    if stream.read(1):
        raise ValueError('Trailing NBT bytes')
    return sorted(set(strings))


def inspect(path, current_hashes):
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        classes = {n:class_strings(archive.read(n)) for n in sorted(names) if n.endswith('.class')}
        references = collections.defaultdict(list)
        for owner, strings in classes.items():
            for value in strings:
                if re.match(r'^(?:[a-z0-9_.-]+:)?(?:geo|animations|textures)/', value) and '\x01' not in value:
                    references[value].append(owner)
        png = {}
        for name in sorted(names):
            if name.endswith('.png'):
                raw = archive.read(name)
                if raw.startswith(b'\x89PNG\r\n\x1a\n'):
                    png[name] = {'size':list(struct.unpack('>II', raw[16:24])),
                                 'sha256':hashlib.sha256(raw).hexdigest()}
        geometries = []
        for name in sorted(names):
            if not name.endswith('.geo.json'):
                continue
            raw = archive.read(name)
            value = json.loads(raw)
            for geometry in value.get('minecraft:geometry', []):
                bones = geometry['bones']
                bone_names = [b['name'] for b in bones]
                relative = name.split('/', 2)[2]
                namespace = name.split('/')[1]
                owners = references.get(relative, [])
                paths, missing_declared = set(), set()
                related = set(owners)
                for owner in owners:
                    # Follow the model's entity/item type once to discover dynamic texture defaults.
                    for token in classes[owner]:
                        for target in re.findall(r'net/mcreator/[A-Za-z0-9_/$]+(?:Entity|Item)', token):
                            if target+'.class' in classes:
                                related.add(target+'.class')
                for owner in related:
                    for token in classes[owner]:
                        if token.startswith(('textures/', 'animations/')) and '\x01' not in token:
                            full = 'assets/'+namespace+'/'+token
                            if full in names:
                                paths.add(full)
                            else:
                                missing_declared.add(full)
                        for folder in ('entities', 'entity', 'item'):
                            full = 'assets/'+namespace+'/textures/'+folder+'/'+token+'.png'
                            if full in png:
                                paths.add(full)
                animations = []
                for animation in sorted(p for p in paths if p.endswith('.animation.json')):
                    for track, motion in json.loads(archive.read(animation)).get('animations', {}).items():
                        targets = set(motion.get('bones', {}))
                        animations.append({'file':animation, 'track':track, 'loop':motion.get('loop', False),
                                           'length':motion.get('animation_length'),
                                           'missing_bones':sorted(targets-set(bone_names)),
                                           'animated_bones':len(targets)})
                geometries.append({'file':name, 'sha256':hashlib.sha256(raw).hexdigest(),
                    'already_in_current':hashlib.sha256(raw).hexdigest() in current_hashes,
                    'identifier':geometry.get('description', {}).get('identifier'),
                    'atlas':[geometry.get('description', {}).get('texture_width'),geometry.get('description', {}).get('texture_height')],
                    'bones':len(bones), 'cubes':sum(len(b.get('cubes', [])) for b in bones),
                    'duplicate_bones':sorted(n for n,c in collections.Counter(bone_names).items() if c>1),
                    'missing_parents':sorted({b['parent'] for b in bones if b.get('parent') and b['parent'] not in bone_names}),
                    'model_classes':owners, 'related_classes':sorted(related-set(owners)),
                    'missing_declared_resources':sorted(missing_declared),
                    'textures':{p:png[p] for p in sorted(paths) if p in png}, 'animations':animations})
        effects = []
        for name in sorted(n for n in names if n.endswith('.fx')):
            try:
                strings = nbt_strings(archive.read(name))
                resource_ids = sorted(s for s in strings if re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_./-]+', s))
                resources = []
                for value in resource_ids:
                    namespace, part = value.split(':', 1)
                    target = 'assets/'+namespace+'/'+part
                    if target in names or part.endswith(('.png', '.json')):
                        resources.append({'id':value, 'file':target, 'in_reference':target in names})
                stem = Path(name).stem
                callers = [owner for owner, values in classes.items()
                           if any(s == stem or s.endswith('/'+stem+'.fx') for s in values)]
                effects.append({'file':name, 'readable_nbt':True, 'resources':resources,
                                'class_reference_candidates':callers,
                                'strings':strings})
            except (ValueError, KeyError, struct.error, EOFError, OSError) as error:
                effects.append({'file':name, 'readable_nbt':False, 'error':str(error)})
        return {'reference':path.name, 'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),
                'entries':len(names), 'classes_inspected':len(classes), 'geometries':geometries,
                'photon_effects':effects, 'java_model_classes':sorted(n for n in classes if '/model/' in n or '/models/' in n),
                'exact_resource_class_links':dict(sorted(references.items())), 'textures':png}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--reference', type=Path, action='append', required=True)
    parser.add_argument('--current', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    with zipfile.ZipFile(args.current) as archive:
        current_hashes = {hashlib.sha256(archive.read(n)).hexdigest() for n in archive.namelist() if n.startswith('assets/')}
    report = {'current':args.current.name, 'current_sha256':hashlib.sha256(args.current.read_bytes()).hexdigest(),
              'method':'Static ZIP, JSON, class constant pool and NBT inspection; reference code never loaded',
              'references':[inspect(path, current_hashes) for path in args.reference]}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2), encoding='utf-8')
    for ref in report['references']:
        print(ref['reference'], 'models', len(ref['geometries']), 'Photon', len(ref['photon_effects']),
              'unreadable', sum(not x['readable_nbt'] for x in ref['photon_effects']))


if __name__ == '__main__':
    main()
