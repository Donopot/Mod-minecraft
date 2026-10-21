"""Check the frozen P0 contract against the original source; never loads a Minecraft world."""
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
CONTRACT = ROOT / 'plans/neoforge-1.21.1/contrat-ids.json'


def validate(contract, root=ROOT):
    if contract.get('schema') != 1 or contract.get('status') != 'frozen-p0':
        raise ValueError('Unsupported or unfrozen contract')
    if contract['target'] != dict(minecraft='1.21.1', neoforge='21.1.264', java=21,
                                   moddevgradle='2.0.148', geckolib='4.9.3'):
        raise ValueError('Target versions drifted from the approved plan')
    checked = 0
    for registry, identifiers in contract['runtime_checks'].items():
        if not identifiers or len(identifiers) != len(set(identifiers)):
            raise ValueError('Empty or duplicate registry: ' + registry)
        for identifier in identifiers:
            if not re.fullmatch(r'(voiddynasty|minecraft):[a-z0-9_./-]+', identifier):
                raise ValueError('Invalid identifier: ' + identifier)
        checked += len(identifiers)
    expected_blocks = {'voiddynasty:super_chest','voiddynasty:super_container',
                       'voiddynasty:promethium_furnace','voiddynasty:promethium_blast_furnace',
                       'voiddynasty:promethium_smoker','voiddynasty:promethium_brewing_stand'}
    if set(contract['map_required']['block']) != expected_blocks:
        raise ValueError('The six map-required blocks must remain explicit')
    if set(contract['map_required']['item']) != {'voiddynasty:control_pad','voiddynasty:laser_drill'}:
        raise ValueError('Map inventory IDs missing')
    if contract['map_required']['entity_type'] != ['voiddynasty:servitor_2']:
        raise ValueError('Map companion ID missing')
    for registry, identifiers in contract['map_required'].items():
        if not set(identifiers) <= set(contract['runtime_checks'][registry]):
            raise ValueError('Map contract absent from runtime test: ' + registry)
    machines = {entry['block']:entry for entry in contract['block_entities']}
    expected = {'super_chest':('voiddynasty:super_chest',81),
                'super_container':('voiddynasty:super_container',54),
                'mining_rig':('voiddynasty:mining_rig',3),
                'promethium_furnace':('minecraft:furnace',3),
                'promethium_blast_furnace':('minecraft:blast_furnace',3),
                'promethium_smoker':('minecraft:smoker',3),
                'promethium_brewing_stand':('minecraft:brewing_stand',5)}
    for name, (be, slots) in expected.items():
        entry = machines['voiddynasty:'+name]
        if (entry['type'],entry['slots']) != (be,slots):
            raise ValueError('Block entity identity/capacity changed: ' + name)
    inventory = contract['nbt_profiles']['inventory']
    if inventory['keys'] != ['Items'] or inventory['item_keys'] != ['Slot','id','count','components']:
        raise ValueError('Inventory NBT keys changed')
    if contract['menu']['persistent_nbt'] or contract['menu']['visible_slots'] != 54:
        raise ValueError('Transient menu state must not replace persistent item slots')
    if contract['import_policy']['worldedit_paste_allowed']:
        raise ValueError('P0 must not permit pasting with an incomplete mod')
    if not contract['external_data']['dimension_saved_data']:
        raise ValueError('Companion world data must have a separate export plan')
    for path, expected_hash in contract['source_files_sha256'].items():
        resolved = (root/path).resolve()
        if not resolved.is_relative_to(root.resolve()):
            raise ValueError('Source outside repository')
        actual = hashlib.sha256(resolved.read_bytes().replace(b'\r\n',b'\n')).hexdigest()
        if actual != expected_hash:
            raise ValueError('Frozen reference source changed: ' + path)
    sound_source = (root/'voiddynasty/src/main/java/com/donopot/voiddynasty/registry/ModSounds.java').read_text(encoding='utf-8')
    sounds = re.findall(r'public static final SoundEvent \w+\s*=\s*fire\("([a-z0-9_.]+)"',sound_source)
    if set(contract['runtime_checks']['sound_event']) != {'voiddynasty:'+v for v in sounds}:
        raise ValueError('Actual sound registrations differ from contract')
    if checked != 88:
        raise ValueError('Truncated or extended contract requires explicit review')
    return checked


if __name__ == '__main__':
    value = json.loads(CONTRACT.read_text(encoding='utf-8'))
    print('P0 CONTRACT PASS:', validate(value), 'runtime identifiers;',
          len(value['source_files_sha256']), 'reference source hashes verified')
