"""Check the shipped Magu tracks themselves, independently of Java dispatch names."""
from pathlib import Path
import hashlib, json, math, zipfile

project=Path(__file__).resolve().parents[1]
build=json.loads((project/'build/build-local.json').read_text())
names=['magu_fist_charge','magu_fist_release','magu_hound_charge','magu_hound_release',
       'magu_kagero_dash','magu_kagero_finish','magu_volcanic_cast','magu_meteors_cast']
fingerprints={}; rows=[]
with zipfile.ZipFile(build['jar']) as archive:
    for name in names:
        data=json.loads(archive.read('assets/minepiece/player_animations/'+name+'.json'))
        track=data['animations'][name]; length=track['animation_length']; bones=track['bones']
        assert 0<length<=5.2, (name,'unbounded animation')
        assert {'body','right_arm','left_arm','right_leg','left_leg'}<=bones.keys()
        movement=False
        for bone in bones.values():
            for channel,keys in bone.items():
                ordered=sorted(keys,key=float)
                assert float(ordered[0])==0 and float(ordered[-1])==length, (name,'missing lifecycle endpoint')
                assert all(abs(v)<.0001 for v in keys[ordered[-1]]['vector']), (name,'pose stays frozen after completion')
                for time,key in keys.items():
                    assert 0<=float(time)<=length, (name,'key beyond duration')
                    vector=key['vector']
                    assert len(vector)==3 and all(isinstance(v,(float,int)) and math.isfinite(v) for v in vector)
                    movement|=any(abs(v)>.01 for v in vector)
        assert movement, (name,'neutral track')
        # Ignore the animation identifier: relabeling an identical pose must fail this audit.
        digest=hashlib.sha256(json.dumps(bones,sort_keys=True).encode()).hexdigest()
        assert digest not in fingerprints, (name,'duplicate bone tracks',fingerprints.get(digest))
        fingerprints[digest]=name
        rows.append({'animation':name,'length_seconds':length,'bone_tracks_sha256':digest,'ends_neutral':True})
report={'jar_sha256':build['sha256'],'distinct_finite_tracks':len(rows),'tracks':rows}
(project/'build/magu-animation-audit.local.json').write_text(json.dumps(report,indent=2)+'\n')
print('MAGU_ANIMATIONS_PASS',len(rows),'distinct non-neutral bone timelines; durations bounded')
