"""Differentiate moves sharing a family using their actual physical gesture, not just a new label."""
from pathlib import Path
import json
P=Path(__file__).resolve().parents[1];A=P/'src/main/resources/assets/minepiece/player_animations'
poses={
 'lupusfall':{'body':[28,-35,15],'right_leg':[-130,0,-35],'right_arm':[-40,0,-70],'left_arm':[25,0,65]},
 'okamihajiki':{'body':[24,-10,0],'right_arm':[-100,-25,-10],'left_arm':[-90,25,10]},
 'localareanetwork':{'body':[-12,0,0],'right_arm':[-65,-55,-70],'left_arm':[-65,55,70]},
 'poursuite':{'body':[38,-8,0],'right_arm':[48,0,-12],'left_arm':[-75,0,10],'right_leg':[-65,0,-5]},
 'gaicho':{'body':[12,0,32],'right_leg':[-70,0,-105],'right_arm':[25,0,-95],'left_arm':[-15,0,85]},
 'shugan':{'body':[30,-20,0],'right_arm':[-100,-35,5],'left_arm':[45,0,25]},
 'shiganmadara':{'body':[17,5,0],'right_arm':[-80,-12,-30],'left_arm':[-75,12,30]},
 'kibasen':{'body':[42,0,0],'head':[35,0,0],'right_arm':[50,0,-30],'left_arm':[50,0,30]},
 'rankyakuran':{'body':[-8,-55,10],'right_leg':[-105,20,60],'right_arm':[-10,0,-70],'left_arm':[40,0,65]},
 'tobushigan':{'body':[-4,18,0],'right_arm':[-88,0,-5],'left_arm':[-60,15,40]},
 'yurushitene':{'body':[38,-10,0],'head':[25,0,-10],'right_arm':[-125,0,-55],'left_arm':[-100,0,45]},
}
count=0
for path in A.glob('b06_*.json'):
 name=path.stem;data=json.loads(path.read_text(encoding='utf-8'));anim=data['animations'][name];release=name.endswith('_release');base=name[:-8] if release else name;move=next((m for m in poses if base.endswith('_'+m)),None)
 if move is None:continue
 fruit=base.split('_')[1];factor=1+['wolf','leopard','saber','giraffe','batto'].index(fruit)*.025;length=anim['animation_length'];bones={}
 for bone,vector in poses[move].items():
  v=[round(x*factor*(1 if release else -.58 if bone=='body' else .55),4) for x in vector];keys={'0':{'vector':[0,0,0]}}
  if move=='shiganmadara' and release:
   for t in [.05,.2,.65,.8,1.25,1.4]:keys[str(t)]={'vector':v if t in [.05,.65,1.25] else [round(x*.15,4) for x in v]}
  else:keys['0.1']={'vector':v};keys[str(round(length-.15,3))]={'vector':v}
  keys[str(length)]={'vector':[0,0,0]};bones[bone]={'rotation':keys}
 anim['bones']=bones;path.write_text(json.dumps(data),encoding='utf-8');count+=1
print('B06_POLISHED',count,'gestures')
