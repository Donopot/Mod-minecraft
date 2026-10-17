"""Retain an explicitly selected, strictly verified fruit scope from a larger capture run."""
from pathlib import Path
import argparse,collections,json,re
P=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('lab',type=Path);ap.add_argument('--fruits',required=True);a=ap.parse_args()
catalogue={'mochi_mochi':['mochipistol','mochigatling','yakimochi','zangirimochi','musodonatsu','mochibuild'],'mori_mori':['root','rootgiant','assault','rootsurround'],'riki_riki':['attackoverhead','hadoelbow','galleonlariat','stonethrowing','stonethrowinglarge']}
chosen=a.fruits.split(',');assert set(chosen)<=catalogue.keys()
meta=json.loads((a.lab/'visual-lab.local.json').read_text(encoding='utf-8'));assert meta['stopped']
meta['is_verified_subset_of']=str(a.lab.resolve());meta['retake']=a.fruits
expected=collections.Counter(f'b03_{f.split("_")[0]}_{move}{phase}' for f in chosen for move in catalogue[f] for phase in ['', '_release'])
if 'mochi_mochi' in chosen:
    for phase in ['', '_release']:expected['b03_mochi_mochipistol'+phase]+=1
for who in ['actor','observer']:
    directory=Path(meta['client' if who=='actor' else 'observer']);body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
    required={f'{f}/{move}-render' for f in chosen for move in catalogue[f]}
    if 'mochi_mochi' in chosen:required.add('mochi_mochi/musodonatsu-reactive')
    if who=='actor':required|={f'{f}/{move}-movement' for f in chosen for move in catalogue[f] if move in ['zangirimochi','galleonlariat']}
    rows=[(cid,state,detail) for cid,state,detail in re.findall(r'\[batch03-client-check\] (\S+) (PASS|FAIL) (.*)',body) if cid in required]
    assert len(rows)==len(required) and {cid for cid,state,_ in rows if state=='PASS'}==required and all(state=='PASS' for _,state,_ in rows)
    poses=[(cid,state) for cid,state in re.findall(r'\[batch03-pal\] (\S+) (PASS|FAIL)',body) if cid in expected]
    assert collections.Counter(cid for cid,state in poses if state=='PASS')==expected and all(state=='PASS' for _,state in poses)
    images=[str(p) for p in sorted((directory/'screenshots').glob(who+'-*.png')) if any('-'+f.split('_')[0]+'-' in p.name for f in chosen)]
    assert len(images)==5*sum(len(catalogue[f]) for f in chosen)
    meta[who+'_images']=images;meta[who+'_checks']=[{'id':cid,'passed':True,'detail':detail} for cid,_,detail in rows];meta[who+'_pal_checks']=[{'id':cid,'passed':True} for cid,_ in poses]
meta['native_player_checks']=[];meta['rendered']=True
dest=P/'build/b03-mochi-mori-riki-proof';dest.mkdir(exist_ok=True)
(dest/'visual-lab.local.json').write_text(json.dumps(meta,indent=2),encoding='utf-8')
print('VERIFIED_SCOPE',a.fruits,dest)
