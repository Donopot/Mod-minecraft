"""Inspect the actual private JAR, including resource overrides, without launching Minecraft."""
from pathlib import Path
import hashlib, json, re, zipfile

PROJECT = Path(__file__).resolve().parents[1]
build = json.loads((PROJECT / 'build/build-local.json').read_text())
jar = Path(build['jar'])
issues, warnings = [], []
with zipfile.ZipFile(jar) as archive:
    names = set(archive.namelist())
    bad_crc = archive.testzip()
    if bad_crc: issues.append({'kind': 'crc', 'file': bad_crc})
    parsed = {}
    for name in sorted(names):
        if not name.endswith('.json'): continue
        try:
            data = archive.read(name).decode('utf-8-sig')
            parsed[name] = json.loads(data, parse_constant=lambda token: (_ for _ in ()).throw(ValueError(token)))
            if 'animations/' in name and re.search(r'"[+-]?[Nn][Aa][Nn]"', data):
                issues.append({'kind': 'nonfinite-animation-expression', 'file': name})
        except (ValueError, UnicodeError) as error:
            issues.append({'kind': 'invalid-json', 'file': name, 'detail': str(error)})
    for name, data in parsed.items():
        if not name.endswith('/sounds.json'): continue
        namespace = name.split('/')[1]
        for event, definition in data.items():
            for sound in definition.get('sounds', []):
                if isinstance(sound, dict) and sound.get('type') == 'event': continue
                sound = sound if isinstance(sound, str) else sound.get('name', '')
                ns, path = sound.split(':', 1) if ':' in sound else (namespace, sound)
                target = f'assets/{ns}/sounds/{path}.ogg'
                if ns == namespace and target not in names:
                    issues.append({'kind': 'missing-sound', 'event': event, 'file': target})
    effects=(PROJECT/'src/main/java/net/mcreator/minepiece/init/MinepieceModMobEffects.java').read_text(encoding='utf-8')
    for effect in re.findall(r'= register\("([^"]+)"',effects):
        icon=f'assets/minepiece/textures/mob_effect/{effect}.png'
        if icon not in names: issues.append({'kind':'missing-effect-icon','file':icon})
    registry = (PROJECT / 'src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java').read_text(encoding='utf-8')
    visual_refs = re.findall(r'\b(?:v|mv)\("([^"]+)",\s*"([^"]+)",\s*"([^"]+)"\)', registry)
    # B02 runtime routes carry explicit pairs outside the registry fallback table.
    visual_refs += [('b02_rubble','rubble','tex_stone'),('b02_stoneopeope','rubble','tex_stone'),('b02_meteor','ball_maximum','meteor')]
    visual_refs += [('b03_mochi','entity_gomu_arm','mochi'),('b03_mochi_fist','fist_small','tex_black'),('b03_riki_rock','rock_fragment','rock_fragment'),('b03_supa_slash','projectile_slash','slash_white')]
    visual_refs += [('b04_soldier','biscuit_warrrior','biscuit_warrrior'),('b04_bear','kumac','kumac'),('b04_arrow','aroaro_arrow','aroaro_arrow'),('b04_negative','ghost1','ghost1_1'),('b04_mini','ghost2','ghost1_2')]
    for model in ['biscuit_warrrior','kumac','ghost1','ghost2','aroaro_arrow','heart_bullet']:
        target=f'assets/minepiece/geckolib/animations/{model}.animation.json'
        if target not in names:issues.append({'kind':'missing-b04-animation-resource','file':target})
    for technique, model, texture in visual_refs:
        for target in [f'assets/minepiece/geckolib/models/{model}.geo.json', f'assets/minepiece/textures/entities/{texture}.png']:
            if target not in names: issues.append({'kind': 'missing-technique-resource', 'technique': technique, 'file': target})
    layer = (PROJECT / 'src/main/java/net/mcreator/minepiece/client/layer/PlayerTransformLayer.java').read_text(encoding='utf-8')
    for texture in set(re.findall(r'tex\("([^"]+)"\)', layer)):
        target = f'assets/minepiece/textures/entities/{texture}.png'
        if target not in names: issues.append({'kind': 'missing-form-texture', 'file': target})
    for name, data in parsed.items():
        if name.startswith('assets/minepiece/models/'):
            for element in data.get('elements', []):
                for face, definition in element.get('faces', {}).items():
                    uv = definition.get('uv', [])
                    if uv and (min(uv) < 0 or max(uv) > 16):
                        issues.append({'kind': 'item-face-outside-texture', 'file': name, 'face': face, 'uv': uv})
        if not name.endswith('.geo.json'): continue
        for geometry in data.get('minecraft:geometry', []):
            description = geometry.get('description', {})
            width, height = description.get('texture_width'), description.get('texture_height')
            for bone in geometry.get('bones', []):
                for cube in bone.get('cubes', []):
                    faces = cube.get('uv')
                    if isinstance(faces, dict) and width and height:
                        for face, uv in faces.items():
                            if not isinstance(uv, dict) or 'uv_size' not in uv: continue
                            x, y = uv['uv']; dx, dy = uv['uv_size']
                            if min(x, x+dx) < 0 or max(x, x+dx) > width or min(y, y+dy) < 0 or max(y, y+dy) > height:
                                warnings.append({'kind': 'uv-outside-atlas', 'file': name, 'bone': bone['name'], 'face': face,
                                                 'uv': [x, y, x+dx, y+dy], 'atlas': [width, height]})
report = {'jar': str(jar), 'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
          'json_count': len(parsed), 'technique_visual_references': len(visual_refs),
          'issues': issues, 'warnings': warnings, 'rendered': False}
path = PROJECT / 'build/asset-audit.local.json'
path.write_text(json.dumps(report, indent=2), encoding='utf-8')
print(f'ASSETS json={len(parsed)} technique-pairs={len(visual_refs)} issues={len(issues)} warnings={len(warnings)}')
for issue in issues: print(issue['kind'], issue['file'])
print('REPORT', path)
raise SystemExit(1 if issues else 0)
