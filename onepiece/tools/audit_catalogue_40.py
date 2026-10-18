"""Read-only catalogue/resource audit. Requires a local built JAR; never starts a game.

python tools/audit_catalogue_40.py --jar path/to/opworldlike.jar --output report.json
Optional --runtime-report accepts the JSON produced by the separate CatalogueAuditMod test mod.
Missing legacy animation declarations remain visible as warnings; missing selected animations
and missing terrain decisions in a supplied runtime report are errors.
"""
from pathlib import Path
import argparse
import collections
import csv
import hashlib
import json
import re
import zipfile


def audit(project, jar, runtime=None):
    errors, warnings = [], []
    with (project / 'docs/matrice-techniques-213.csv').open(encoding='utf-8', newline='') as stream:
        rows = list(csv.DictReader(stream))
    catalogue = collections.Counter(row['fruit'] for row in rows)
    expected = {(row['fruit'], row['technique']) for row in rows}
    if len(rows) != 213 or len(catalogue) != 40 or len(expected) != 213:
        errors.append('Expected 40 fruits and 213 unique catalogue entries')
    registry = (project / 'src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java').read_text(encoding='utf-8')
    declarations = re.findall(
        r'(?:MOVES\.put|putZoan)\(MinepieceModItems\.ITEM_DEVIL_FRUIT_(\w+), List\.of\((.*?)\n\s*\)\);', registry, re.S)
    actual = {(fruit.lower(), move) for fruit, body in declarations
              for move in re.findall(r'(?:DevilFruitMove\.\w+|new DevilFruitMove)\("([^"]+)"', body)}
    if actual != expected:
        errors.append({'registry_only': sorted(actual - expected), 'matrix_only': sorted(expected - actual)})
    animation_source = (project / 'src/main/java/net/mcreator/minepiece/procedures/TechniqueAnimations.java').read_text(encoding='utf-8')
    block = re.search(r'CONNUES = \{(.*?)\};', animation_source, re.S)
    if block is None:
        raise ValueError('Animation declaration format changed; audit needs updating')
    declared = re.findall(r'"([^"]+)"', block.group(1))
    with zipfile.ZipFile(jar) as archive:
        names = archive.namelist()
        duplicates = [name for name, count in collections.Counter(names).items() if count > 1]
        if duplicates:
            errors.append({'duplicate_archive_entries': duplicates})
        if archive.testzip() is not None:
            errors.append('Archive CRC failure')
        parsed = {}
        for name in names:
            if name.endswith('.json'):
                try:
                    parsed[name] = json.loads(archive.read(name).decode('utf-8-sig'),
                        parse_constant=lambda token: (_ for _ in ()).throw(ValueError(token)))
                except (ValueError, UnicodeError) as failure:
                    errors.append({'invalid_json': name, 'detail': str(failure)})
        bindings = parsed.get('assets/minepiece/source_visuals.json', {}).get('bindings', [])
        profiles = [profile for binding in bindings for profile in binding['profiles']]
        if len(profiles) != len(set(profiles)):
            errors.append('Duplicate source visual profile')
        for binding in bindings:
            for field, folder, suffix in [('model', 'geckolib/models', '.geo.json'),
                    ('animation', 'geckolib/animations', '.animation.json'), ('texture', 'textures/entities', '.png')]:
                value = binding.get(field, binding['model'])
                path = 'assets/minepiece/' + (value if field == 'texture' and value.startswith('textures/')
                                             else folder + '/' + value) + suffix
                if path not in names:
                    errors.append({'missing_source_resource': path})
        missing = [name for name in declared if 'assets/minepiece/player_animations/' + name + '.json' not in names]
        if missing:
            warnings.append({'declared_animation_files_missing': missing,
                'scope': 'Declaration list; does not by itself prove a current fruit selects these names'})
        metadata = parsed.get('fabric.mod.json', {})
    if runtime is not None:
        loaded = json.loads(runtime.read_text(encoding='utf-8'))
        if loaded.get('fruit_count') != 40 or loaded.get('technique_count') != 213:
            errors.append('Runtime catalogue totals differ')
        errors.extend(loaded.get('errors', []))
        selected = {(fruit['fruit'], move['id']) for fruit in loaded.get('fruits', []) for move in fruit['techniques']}
        if selected != expected:
            errors.append('Runtime techniques differ from catalogue')
        for fruit in loaded.get('fruits', []):
            for move in fruit['techniques']:
                for key in ['animation_empty_hand', 'animation_sword']:
                    name = move.get(key, '')
                    if not name or 'assets/minepiece/player_animations/' + name + '.json' not in names:
                        errors.append(f"{fruit['fruit']}/{move['id']}: unavailable {key}={name}")
                if move.get('terrain') not in ['NONE', 'TRANSFORM', 'DESTRUCTIVE']:
                    errors.append(f"{fruit['fruit']}/{move['id']}: missing terrain decision")
    return {'version': metadata.get('version'), 'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
        'fruit_count': len(catalogue), 'technique_count': len(rows), 'fruit_techniques': dict(catalogue),
        'source_visual_profiles': len(profiles), 'registered_player_animations': len(declared),
        'runtime_report_supplied': runtime is not None, 'errors': errors, 'warnings': warnings,
        'passed': not errors, 'visual_acceptance': False}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', required=True, type=Path)
    parser.add_argument('--output', type=Path)
    parser.add_argument('--runtime-report', type=Path)
    args = parser.parse_args()
    result = audit(Path(__file__).resolve().parents[1], args.jar, args.runtime_report)
    text = json.dumps(result, ensure_ascii=False, indent=2) + '\n'
    if args.output:
        args.output.write_text(text, encoding='utf-8')
    print(f"Catalogue: {result['fruit_count']} fruits, {result['technique_count']} techniques; "
          f"errors={len(result['errors'])}, warnings={len(result['warnings'])}")
    raise SystemExit(0 if result['passed'] else 1)
