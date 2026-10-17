"""Audit the actual private release JAR, including frozen Magu animation content."""
from pathlib import Path
import hashlib, json, math, zipfile

p=Path(__file__).resolve().parents[1]
b=json.loads((p/'build/build-local.json').read_text(encoding='utf-8'))
names=[f'goro_{move}_{phase}' for move in ['vari','elthor','sango','karikrash'] for phase in ['charge','release']]
names+=['goro_amaru_transform','goro_storm_cast','goro_blink','goro_raigo_cast']
rows=[];seen=set()
with zipfile.ZipFile(b['jar']) as archive:
    for name in names:
        track=json.loads(archive.read('assets/minepiece/player_animations/'+name+'.json'))['animations'][name]
        length=track['animation_length'];bones=track['bones']
        assert 0<length<=5.2 and len(bones)==7
        moving=False
        for channels in bones.values():
            for keys in channels.values():
                times=sorted(keys,key=float)
                assert float(times[0])==0 and float(times[-1])==length
                assert all(abs(v)<.0001 for v in keys[times[-1]]['vector'])
                for t,key in keys.items():
                    assert 0<=float(t)<=length and len(key['vector'])==3
                    assert all(isinstance(v,(float,int)) and math.isfinite(v) for v in key['vector'])
                    moving |= any(abs(v)>.01 for v in key['vector'])
        assert moving
        digest=hashlib.sha256(json.dumps(bones,sort_keys=True).encode()).hexdigest()
        assert digest not in seen,(name,'duplicate bone track')
        seen.add(digest);rows.append(dict(animation=name,length_seconds=length,ends_neutral=True,bone_tracks_sha256=digest))
    frozen=p/'build/libs/opworldlike-1.0.1-alpha.5.jar'
    with zipfile.ZipFile(frozen) as old:
        magu=[name for name in old.namelist() if name.startswith('assets/minepiece/player_animations/magu_')]
        assert len(magu)==8 and all(archive.read(name)==old.read(name) for name in magu),'Magu animation content drift'
report=dict(jar_sha256=b['sha256'],distinct_finite_tracks=len(rows),tracks=rows,frozen_magu_tracks_identical=8)
(p/'build/goro-animation-audit.local.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print('GORO_ANIMATIONS_PASS',len(rows),'distinct finite neutral-ending tracks; 8 Magu tracks unchanged')
