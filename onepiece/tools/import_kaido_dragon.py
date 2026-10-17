"""Import the exact user-supplied MinePiece dragon, keeping private provenance."""
from pathlib import Path
import hashlib,json,re,zipfile
P=Path(__file__).resolve().parents[1]
src=Path('C:/Users/Anwender/Downloads/MinePiece-ver15-forge-1.20.1 (3).jar')
assert hashlib.sha256(src.read_bytes()).hexdigest()=='f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704'
root=P/'src/main/resources/assets/minepiece'; records=[]
with zipfile.ZipFile(src) as z:
 for name in ['geo/kaidou_beast.geo.json','animations/kaidou_beast.animation.json','textures/entities/kaidou_beast.png']:
  data=z.read('assets/minepiece/'+name);target=root/name;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data);records.append({'asset':name,'sha256':hashlib.sha256(data).hexdigest()})
 a=json.loads(z.read('assets/minepiece/animations/kaidou_beast.animation.json'))['animations']['idle']['bones']
def expr(v):
 if isinstance(v,(float,int)):return str(float(v))+'F'
 match=re.fullmatch(r'([+-]?[\d.]+)([+-])math\.(sin|cos)\(query.anim_time\*90\)\*([+-]?[\d.]+)',v)
 assert match,v
 n,sign,fn,amp=match.groups();return f'({n}F{sign}(float)Math.{fn}(seconds*Math.PI/2)*({amp}F))'
lines=[]
for name,channels in a.items():
 if 'rotation' in channels:
  v=channels['rotation']['vector'];lines.append('  rotate("'+name+'",'+','.join(expr(x) for x in v)+');')
 if 'position' in channels:
  v=channels['position']['vector'];lines.append('  position("'+name+'",'+','.join(expr(x) for x in v)+');')
out=P/'build/kaido-dragon-idle.generated.txt';out.write_text('\n'.join(lines),encoding='utf-8')
(P/'build/kaido-dragon-provenance.local.json').write_text(json.dumps({'source':str(src),'jar_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),'assets':records,'bones':51,'cubes':346,'gameplay_imported':False},indent=2),encoding='utf-8')
print('Imported original dragon geometry, texture and animation; generated idle channels',len(lines))
