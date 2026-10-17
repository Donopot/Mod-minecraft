"""Validate the actual packed finite B07 poses; protects neutral recovery and distinct tracks."""
from pathlib import Path
import hashlib,json,zipfile,math
R=Path(__file__).resolve().parents[1];b=json.loads((R/'build/build-local.json').read_text(encoding='utf-8'))
catalogue=json.loads((R/'build/b07-moves.local.json').read_text(encoding='utf-8'))
ids=[f+'_'+m for f,ms in catalogue.items() for m in ms]
rows=[];digests=set();duplicates=[]
with zipfile.ZipFile(b['jar']) as z:
 for move in ids:
  for suffix in ['', '_release']:
   name='b07_'+move+suffix;data=json.loads(z.read('assets/minepiece/player_animations/'+name+'.json'))['animations'][name]
   length=data['animation_length'];assert 0<length<=4 and not data.get('loop',False),name
   changing=False
   for bone,channels in data['bones'].items():
    for channel,keys in channels.items():
     ordered=sorted((float(t),v['vector']) for t,v in keys.items())
     assert abs(ordered[-1][0]-length)<.00001 and all(abs(v)<.00001 for v in ordered[-1][1]),(name,bone,'non-neutral recovery')
     for t,vector in ordered:
      assert all(math.isfinite(v) for v in vector),(name,bone,'non-finite')
      changing |= any(abs(v)>.001 for v in vector)
   assert changing,name
   digest=hashlib.sha256(json.dumps(data['bones'],sort_keys=True).encode()).hexdigest()
   if digest in digests:duplicates.append(name)
   digests.add(digest);rows.append({'animation':name,'seconds':length,'track_sha256':digest})
assert not duplicates,duplicates
report={'jar_sha256':b['sha256'],'distinct_finite_tracks':len(rows),'tracks':rows,'duplicate_tracks':duplicates,'unique_tracks':len(digests)}
(R/'build/batch07-animation-audit.local.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print('BATCH07_ANIMATIONS_PASS',len(rows),'distinct finite tracks, all recover neutral')
