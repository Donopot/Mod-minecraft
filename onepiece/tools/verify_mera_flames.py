"""Audit the completed alpha.18 Mera capture without changing its original manifest."""
from pathlib import Path
import hashlib,json,re,zipfile

P=Path(__file__).resolve().parents[1]
LAB=P.parent/'.runtime/onepiece-lab/run-ba126e16c8a14163b8f05de9da9d959d'
MANUAL=P.parent/'.runtime/onepiece-lab/run-0bc4af4b64d5422b82cada6c52684237'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=P/'build/libs/opworldlike-1.0.1-alpha.18.jar'
v=json.loads((LAB/'visual-lab.local.json').read_text(encoding='utf-8'))
assert v['stopped'] and v['capture'] and v['scenario']=='mera'
assert sha(jar)==v['build_sha256']=='a8abfbba24ee3ce759b6a46f7dd0bd832e18392be10ed2191e9342c4f49888cc'
profiles={'hiken','higan','hibashira','mera_dash','mera_fireflies','kaenryuo','daienkai'}
roles={}
for role,key,n in [('actor','client',48),('observer','observer',24)]:
 directory=Path(v[key]);body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
 shots=sorted((directory/'screenshots').glob(role+'-*.png'))
 assert len(shots)==n and all(p.stat().st_size>0 for p in shots)
 assert sha(directory/'mods'/jar.name)==sha(jar)
 checks=re.findall(r'\[mera-client-check\] (\S+) (PASS|FAIL)',body)
 if role=='actor':assert checks==[('propulsion-real-distance','PASS')]
 else:assert len(checks)==2 and set(checks)=={('hibashira-after-15-ticks','PASS'),('kaenryuo-after-15-ticks','PASS')}
 flames=re.findall(r'\[mera-flame\] (\S+) sprites=(\d+) native-propulsion-flames',body)
 assert profiles<={p for p,count in flames if int(count)>0}
 roles[role]={'image_count':n,'images':[{'path':str(p),'sha256':sha(p)} for p in shots],'native_checks':checks,'emitted_flame_profiles':dict(flames)}
native=json.loads((P/'build/final-working-proof.local.json').read_text(encoding='utf-8'))
nativejar=Path(native['native_lab'])/'mods/opworldlike-1.0.1-alpha.17.jar'
assert sha(nativejar)==native['native_sha256']
with zipfile.ZipFile(nativejar) as a,zipfile.ZipFile(jar) as b:
 delta=[n for n in sorted(set(a.namelist())|set(b.namelist())) if n not in a.namelist() or n not in b.namelist() or a.read(n)!=b.read(n)]
 assert all(n.startswith(('assets/','net/mcreator/minepiece/client/')) or n=='fabric.mod.json' for n in delta),delta
manual=json.loads((MANUAL/'visual-lab.local.json').read_text(encoding='utf-8'))
assert not manual['launched'] and not manual['capture'] and manual['build_sha256']==sha(jar)
report={'build_sha256':sha(jar),'lab':str(LAB),'roles':roles,
 'post_capture_validation':True,'original_manifest_sha256':sha(LAB/'visual-lab.local.json'),
 'original_rendered_flag':v['rendered'],'original_flag_note':'Generic capture branch omitted setting rendered; original manifest retained. Existing evidence revalidated offline, not a new run.',
 'client_only_delta_from_native':delta,'server_classes_identical':True,
 'native_proof':str(P/'build/final-working-proof.local.json'),
 'inspected_sheets':[str(P/'build/mera-all-flames-review'/f) for f in ['actor-mera.jpg','observer-mera.jpg']],
 'manual_lab':str(MANUAL),'flame_budget':{'max_per_two_client_ticks':96,'particle_lifetime_ticks':15},
 'known_limitations':['Existing opaque Entei mesh can intersect the camera.'],
 'human_art_accepted':False,'all40_asset_review_complete':False,'normal_installation_changed':False}
out=P/'build/mera-flames-alpha18-proof.local.json'
out.write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps({'proof':str(out),'actor_images':48,'observer_images':24,'mera_attacks_with_flames':7,'server_classes_identical':True}))
