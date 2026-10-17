"""Combine disjoint, verified B03 retakes on exactly the same JAR; never turn failures green."""
from pathlib import Path
import argparse,json,zipfile
P=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('labs',type=Path,nargs='+');a=ap.parse_args()
roles=['actor','observer'];all_fruits={'mochi_mochi','mori_mori','kira_kira','supa_supa','riki_riki'}
parts=[json.loads((p/'visual-lab.local.json').read_text(encoding='utf-8')) for p in a.labs]
# Two lab-only diagnostic classes may differ; all gameplay/render resources must match.
diagnostic={'net/mcreator/minepiece/command/RepairTestCommands.class','net/mcreator/minepiece/client/Batch03VisualLab.class'}
anchor=next((Path(parts[0]['client'])/'mods').glob('opworldlike*.jar'))
with zipfile.ZipFile(anchor) as reference:
    for part in parts[1:]:
        jar=next((Path(part['client'])/'mods').glob('opworldlike*.jar'))
        with zipfile.ZipFile(jar) as other:
            assert set(reference.namelist())==set(other.namelist())
            assert {n for n in reference.namelist() if reference.read(n)!=other.read(n)}<=diagnostic

seen=set()
for part in parts:
    assert part['rendered'] and part['stopped'] and part['scenario']=='batch03'
    fruits=set(part['retake'].split(',')) if part['retake'] else all_fruits
    assert not seen.intersection(fruits),'overlapping retakes'
    seen.update(fruits)
assert seen==all_fruits
result={**parts[0],'retake':'','composite':True,'component_labs':[str(p.resolve()) for p in a.labs]}
for who in roles:
    for suffix in ['images','checks','pal_checks']:result[who+'_'+suffix]=sum((part[who+'_'+suffix] for part in parts),[])
    assert len(result[who+'_images'])==120
    assert len(result[who+'_pal_checks'])==50 and all(c['passed'] for c in result[who+'_pal_checks'])
    assert all(c['passed'] for c in result[who+'_checks'])
    assert len({c['id'] for c in result[who+'_checks']})==len(result[who+'_checks'])
result['native_player_checks']=sum((p.get('native_player_checks',[]) for p in parts),[])
assert len(result['native_player_checks'])==2 and all(c['passed'] for c in result['native_player_checks'])
dest=P/'build/b03-composite';dest.mkdir(exist_ok=True)
(dest/'visual-lab.local.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print('COMBINED_VERIFIED_B03',dest)
