"""Verify ver15 provenance, packaged references and exact native-test parity."""
from pathlib import Path
from copy import deepcopy
import argparse, hashlib, json, zipfile

P=Path(__file__).resolve().parents[1]
R=P.parent
ap=argparse.ArgumentParser()
ap.add_argument('--summon-native',type=Path,help='Additional final-package native report covering the ownership metadata')
args=ap.parse_args()
def read(p): return json.loads(p.read_text(encoding='utf-8'))
def sha(data): return hashlib.sha256(data).hexdigest()
build=read(P/'build/build-local.json')
reference=R/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar'
assert sha(reference.read_bytes())=='f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704'
assert sha(Path(build['jar']).read_bytes())==build['sha256']
provenance=read(P/'build/source-visuals-provenance.local.json')
native_lab=R/'.runtime/onepiece-lab/run-a46a38b2bf664f388439223630f6520c'
old_lab=R/'.runtime/onepiece-lab/run-9ffbd06d22d0478787462b1d99e70db4'
native=read(native_lab/'report.local.json')
assert native['status']=='passed' and native['process_stopped']
for key,count in [('checks',59),('batch06_checks',535)]:
    assert len(native[key])==count and all(r['passed'] for r in native[key])

with zipfile.ZipFile(reference) as source, zipfile.ZipFile(build['jar']) as packed:
    names=set(packed.namelist())
    bindings=json.loads(packed.read('assets/minepiece/source_visuals.json'))['bindings']
    ids=[]
    for binding in bindings:
        ids+=binding['profiles']
        for field,folder,ext in [('model','geckolib/models','.geo.json'),('animation','geckolib/animations','.animation.json'),('texture','textures/entities','.png')]:
            stem=binding.get(field,binding['model'])
            path='assets/minepiece/'+(stem if field=='texture' and stem.startswith('textures/') else folder+'/'+stem)+ext
            assert path in names, path
    assert len(ids)==len(set(ids))==53
    original_files=set(); alias_files=[]
    for row in provenance:
        target='assets/minepiece/'+row['target']
        assert target in names, target
        if row['kind']=='original_bytes':
            before=source.read(row['source'])
            assert sha(before)==row['sha256']
            assert before==packed.read(target), target
            original_files.add(target)
        elif row['kind']=='animation_alias':
            raw=source.read(row['source'])
            assert sha(raw)==row['source_sha256']
            track=deepcopy(json.loads(raw)['animations'][row['track']])
            factor=row.get('body_position_scale',1)
            if factor!=1:
                for frame in track['bones'].get('body',{}).get('position',{}).values():
                    if isinstance(frame,dict) and 'vector' in frame:
                        frame['vector']=[v*factor for v in frame['vector']]
            aliases=json.loads(packed.read(target))['animations']
            assert aliases=={'idle':track,'vertical':track}, target
            alias_files.append(target)
    def server(n): return n.endswith('.class') and '/client/' not in n and '/entity/model/' not in n
    server_names=[n for n in names if server(n)]
    with zipfile.ZipFile(next((native_lab/'mods').glob('opworldlike*.jar'))) as tested:
        assert sha(Path(tested.filename).read_bytes())==native['mods']['opworldlike']['sha256']
        native_delta=[n for n in server_names if n not in tested.namelist() or packed.read(n)!=tested.read(n)]
        assert all(n.startswith('net/mcreator/minepiece/entity/SummonEntity') and (n.endswith('/SummonEntity.class') or '/SummonEntity$' in n) for n in native_delta), native_delta
    ownership_proof=None
    if native_delta:
        assert args.summon_native, 'The new summon ownership marker requires its final-package native report'
        ownership_proof=read(args.summon_native)
        assert ownership_proof['status']=='passed' and ownership_proof['process_stopped']
        for key,count in [('checks',59),('batch04_checks',365),('batch05_checks',458)]:
            assert len(ownership_proof[key])==count and all(r['passed'] for r in ownership_proof[key]),key
        with zipfile.ZipFile(next((Path(ownership_proof['lab'])/'mods').glob('opworldlike*.jar'))) as tested:
            assert sha(Path(tested.filename).read_bytes())==ownership_proof['mods']['opworldlike']['sha256']
            assert all(n in tested.namelist() and packed.read(n)==tested.read(n) for n in server_names)
    with zipfile.ZipFile(next((old_lab/'mods').glob('opworldlike*.jar'))) as tested:
        previous_delta=sorted(n for n in server_names if n not in tested.namelist() or packed.read(n)!=tested.read(n))
        expected=['net/mcreator/minepiece/procedures/ZoanTechniqueEpisodes.class']+native_delta
        assert previous_delta==sorted(expected), previous_delta
    # Preserve the previously corrected full Kaido dragon, including exact face UVs.
    frozen=R/'.runtime/onepiece/after-b08-alpha17'
    with zipfile.ZipFile(next(frozen.glob('opworldlike*.jar'))) as tested:
        kaido=['assets/minepiece/geo/kaidou_beast.geo.json','assets/minepiece/animations/kaidou_beast.animation.json',
               'assets/minepiece/textures/entities/kaidou_beast.png','net/mcreator/minepiece/client/model/KaidoDragonModel.class']
        assert all(packed.read(n)==tested.read(n) for n in kaido)
report=dict(build_sha256=build['sha256'],source_sha256=sha(reference.read_bytes()),
    reviewed_visual_profiles=len(ids),original_files_unchanged=sorted(original_files),
    verified_animation_aliases=alias_files,server_class_count=len(server_names),
    native_594_report=str(native_lab/'report.local.json'),native_594_sha256=native['mods']['opworldlike']['sha256'],
    native_594_server_delta=native_delta,native_1760_server_delta=previous_delta,
    ownership_native_report=str(args.summon_native) if ownership_proof else None,
    ownership_native_checks=882 if ownership_proof else 0,
    kaido_original_preserved=kaido,
    native_delta_explanation='Relative to1760native: Gagan has its own visual profile (covered by535B06); SummonEntity additionally synchronizes a render-only owner UUID, covered by365B04+458B05+59foundation. All other server classes are byte-identical.',
    rendered=False,human_art_accepted=False)
(P/'build/source-assets-structural-proof.local.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print('SOURCE_ASSETS_VERIFIED',len(ids),'profiles;',len(original_files),'original files;',len(alias_files),'track adapters;',len(server_names),'server classes verified against recorded native scopes')
