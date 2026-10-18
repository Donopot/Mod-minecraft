"""Restore reserved transformation and technique resources, never a Forge runtime."""
from pathlib import Path
import argparse, hashlib, json, zipfile

PROJECT = Path(__file__).resolve().parents[1]
REFERENCE_SHA256 = '328b8ea70071750b654b07e8a03bccbcec66a155680269254b03a01de7de9db0'
SETS = {
    'amaru': ['textures/entities/amaru.png', 'textures/entities/amaruu.png'],
    'torifullform': ['textures/entities/torifullform2new.png'],
    'buddha': ['textures/item/buddha.png'],
    'wolfbeast': ['textures/item/wolfbeast.png'],
    'leopardbeastrework': ['textures/entities/leopardbeastrework.png', 'textures/entities/leopardbeastreworkglow.png'],
}
EFFECTS = {
    'gumpistol': None, 'gumgatling': None,
    'guracrack': 'guracracks1', 'guracrackground': 'guracracks1',
    'guracracksea': 'guracracks1', 'gurauppercut': 'guracracks1',
    **{f'takt{i}': 'taktrocks' for i in range(1,7)},
    'buzzcut': 'buzzcut', 'ursusshock': 'ursusshock',
}
TEXTURES = ['mochifist', 'mochifisthaki', 'yakimochi', 'paweffect', 'cloudring2']

def restore(reference, project, check=False):
    if hashlib.sha256(reference.read_bytes()).hexdigest() != REFERENCE_SHA256:
        raise ValueError('The reserved reference differs from the audited archive')
    manifest = {'reference_sha256': REFERENCE_SHA256, 'files': []}
    with zipfile.ZipFile(reference) as archive:
        for key, textures in SETS.items():
            resources = [(f'geo/{key}.geo.json', f'{key}.geo.json'),
                         (f'animations/{key}.animation.json', f'{key}.animation.json')]
            resources += [(texture, key + ('_glow' if i else '') + '.png') for i, texture in enumerate(textures)]
            for source, destination in resources:
                source = 'assets/trueprimepiecetwo/' + source
                data = archive.read(source)
                if source.endswith('.json'):
                    json.loads(data)
                destination = 'assets/minepiece/upgrade24/' + destination
                manifest['files'].append({'source': source, 'path': destination,
                    'sha256': hashlib.sha256(data).hexdigest(), 'bytes': len(data)})
                if not check:
                    target = project / 'src/main/resources' / destination
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(data)
        for key, texture in EFFECTS.items():
            resources=[(f'geo/{key}.geo.json',f'{key}.geo.json'),(f'animations/{key}.animation.json',f'{key}.animation.json')]
            if texture:resources.append((f'textures/entities/{texture}.png',key+'.png'))
            for source,destination in resources:
                source='assets/trueprimepiecetwo/'+source;data=archive.read(source)
                if source.endswith('.json'):json.loads(data)
                destination='assets/minepiece/upgrade24/'+destination
                manifest['files'].append({'source':source,'path':destination,'sha256':hashlib.sha256(data).hexdigest(),'bytes':len(data)})
                if not check:
                    target=project/'src/main/resources'/destination;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
        for texture in TEXTURES:
            source='assets/trueprimepiecetwo/textures/entities/'+texture+'.png';data=archive.read(source)
            destination='assets/minepiece/upgrade24/'+texture+'.png'
            manifest['files'].append({'source':source,'path':destination,'sha256':hashlib.sha256(data).hexdigest(),'bytes':len(data)})
            if not check:
                target=project/'src/main/resources'/destination;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
    if not check:
        (project/'docs/PRIORITY-ASSETS-MANIFEST.json').write_text(
            json.dumps(manifest, indent=2)+'\n', encoding='utf-8')
    return manifest

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--reference', required=True, type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    result = restore(args.reference, PROJECT, args.check)
    print(('CHECKED' if args.check else 'RESTORED'), len(result['files']), 'reserved resources')
