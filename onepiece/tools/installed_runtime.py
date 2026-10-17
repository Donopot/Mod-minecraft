"""Resolve the installed launch profile, rather than mixing every cached library version."""
from pathlib import Path
import hashlib, json, re

def allowed(rules):
    result = not rules
    for rule in rules:
        os_rule = rule.get('os', {})
        matches = os_rule.get('name', 'windows') == 'windows'
        if 'arch' in os_rule:
            matches &= os_rule['arch'] in ('amd64', 'x86_64')
        if rule.get('features'):
            matches = False
        if matches: result = rule['action'] == 'allow'
    return result

def resolve(minecraft, profile='fabric-loader-0.19.5-26.3'):
    minecraft = Path(minecraft)
    chain = []
    current = profile
    while current:
        if current in {row['id'] for row in chain}: raise RuntimeError('Cyclic launch profile')
        row = json.loads((minecraft / 'versions' / current / (current + '.json')).read_text(encoding='utf-8'))
        chain.append(row)
        current = row.get('inheritsFrom')
    artifacts = {}
    for row in reversed(chain):
        for library in row.get('libraries', []):
            if not allowed(library.get('rules', [])): continue
            parts = library['name'].split(':')
            group, name, version = parts[:3]
            classifier = parts[3] if len(parts) > 3 else ''
            key = (group, name, classifier)
            filename = name + '-' + version + ('-' + classifier if classifier else '') + '.jar'
            derived = '/'.join([group.replace('.', '/'), name, version, filename])
            artifact = library.get('downloads', {}).get('artifact', {})
            path = minecraft / 'libraries' / artifact.get('path', derived)
            if not path.is_file(): raise RuntimeError('Missing installed launch library: ' + str(path))
            expected = artifact.get('sha1')
            if expected and hashlib.sha1(path.read_bytes()).hexdigest() != expected:
                raise RuntimeError('Installed launch library hash mismatch: ' + str(path))
            artifacts[key] = path
    base = chain[-1]
    candidates = [minecraft / 'versions' / row['id'] / (row['id'] + '.jar') for row in chain]
    game = next((p for p in reversed(candidates) if p.is_file()), None)
    if game is None: raise RuntimeError('Installed game archive is missing')
    expected = base.get('downloads', {}).get('client', {}).get('sha1')
    if expected and hashlib.sha1(game.read_bytes()).hexdigest() != expected:
        raise RuntimeError('Installed game hash differs from its launch manifest')
    return {'game': game, 'libraries': list(artifacts.values()), 'asset_index': base['assetIndex']['id']}

if __name__ == '__main__':
    root = Path(__file__).resolve().parents[2]
    config = json.loads((root / '.runtime/fabric-build/build.local.json').read_text())
    runtime = resolve(config['minecraft'])
    print('INSTALLED_RUNTIME', len(runtime['libraries']), 'verified profile libraries; assets', runtime['asset_index'])
