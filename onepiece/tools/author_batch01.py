"""Finite authored B01 PAL phases and private source resource adapters. No other fruit is retargeted."""
from pathlib import Path
import json,zipfile
from author_magu_animations import pose,write,neutral
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/minepiece'
# Distinct gestures by actual action: spear, bird, roots, throw, ground wave; light finger etc.
poses={
'icepartisan':(pose(body=[-8,25,0],right_arm=[20,-25,-25],left_arm=[-60,0,15]),pose(body=[12,-20,0],right_arm=[-95,0,-5],left_arm=[15,0,20])),
'pheasantbeak':(pose(body=[-12,0,0],right_arm=[-50,0,-65],left_arm=[-50,0,65]),pose(body=[15,0,0],right_arm=[-100,15,-20],left_arm=[-100,-15,20])),
'icespikes':(pose(-.7,body=[20,15,0],right_arm=[-115,0,-15],left_arm=[-30,0,25]),pose(-1,body=[40,-15,0],right_arm=[-35,0,-10],left_arm=[-55,0,20])),
'iceball':(pose(body=[-6,25,0],right_arm=[-140,15,-40],left_arm=[-45,0,30]),pose(body=[15,-25,0],right_arm=[-100,-10,-15],left_arm=[5,0,25])),
'iceage':(pose(-1,body=[38,0,0],right_arm=[-30,0,-8],left_arm=[-30,0,8]),pose(-1.5,body=[54,0,0],right_arm=[-45,0,-22],left_arm=[-45,0,22])),
'laser1':(pose(body=[-3,16,0],right_arm=[-50,-20,-10],left_arm=[10,0,15]),pose(body=[5,-8,0],right_arm=[-91,0,-4],left_arm=[5,0,12])),
'laser2':(pose(body=[-8,0,0],right_arm=[-65,-15,-35],left_arm=[-65,15,35]),pose(body=[8,0,0],right_arm=[-95,-5,-16],left_arm=[-95,5,16])),
'kick':(pose(-.5,body=[-12,-15,-8],right_leg=[-65,0,-5],left_leg=[12,0,0],right_arm=[-15,0,-30]),pose(body=[-25,25,-12],right_leg=[-105,0,-10],left_leg=[15,0,5],left_arm=[-45,0,35])),
'yatanokagami':(pose(body=[-2,-25,0],right_arm=[-95,-35,-25],left_arm=[-95,35,25]),pose(-.4,body=[28,0,0],right_arm=[40,0,-12],left_arm=[40,0,12],right_leg=[-20,0,0],left_leg=[20,0,0])),
'magatama':(pose(body=[-6,12,0],right_arm=[-145,0,-45],left_arm=[-145,0,45]),pose(body=[6,-12,0],right_arm=[-110,-10,-55],left_arm=[-110,10,55])),
'amanomurakumo':(pose(body=[-5,-20,0],right_arm=[-80,35,-10],left_arm=[-75,-30,15]),pose(body=[10,30,0],right_arm=[-150,0,-15],left_arm=[-40,0,20])),
'speedispower':(pose(-.8,body=[30,-35,0],right_arm=[40,0,-18],right_leg=[-30,0,0],left_leg=[28,0,0]),pose(body=[-18,55,8],left_leg=[-100,0,12],right_leg=[10,0,0],left_arm=[-25,0,55])),
'passive':(pose(-.3,body=[24,5,0],right_arm=[35,0,-10],left_arm=[25,0,10]),pose(body=[15,-5,0],right_leg=[-28,0,0],left_leg=[28,0,0])),
'whiteblow':(pose(body=[-6,32,0],right_arm=[40,-15,-20],left_arm=[-45,0,25]),pose(body=[12,-30,0],right_arm=[-95,0,-12],left_arm=[25,0,22])),
'whitevine':(pose(-.3,body=[25,0,0],right_arm=[35,0,-20],left_arm=[35,0,20]),pose(body=[-4,20,0],right_arm=[-70,-35,-25],left_arm=[-35,0,45])),
'whitelauncher':(pose(-.6,body=[38,-18,0],right_arm=[45,0,-15],left_arm=[-70,0,20]),pose(body=[12,-40,0],right_arm=[-120,0,-15],left_arm=[20,0,35])),
'whiteout':(pose(body=[-10,0,0],right_arm=[-35,0,-80],left_arm=[-35,0,80]),pose(-.7,body=[25,0,0],right_arm=[-70,0,-40],left_arm=[-70,0,40])),
'desertspada':(pose(body=[-8,25,0],right_arm=[-165,0,-15],left_arm=[10,0,25]),pose(body=[30,-25,0],right_arm=[-35,0,-12],left_arm=[20,0,20])),
'barjan':(pose(-.5,body=[28,-32,0],right_arm=[20,0,-30],left_arm=[-35,0,25]),pose(body=[15,65,0],right_arm=[-85,0,75],left_arm=[35,0,15])),
'sables':(pose(body=[-5,-18,0],right_arm=[-65,-15,-25],left_arm=[-30,0,20]),pose(body=[8,18,0],right_arm=[-100,0,-40],left_arm=[10,0,30])),
'desertlaspada':(pose(body=[-10,-30,0],right_arm=[-130,0,-35],left_arm=[-120,0,35]),pose(body=[28,30,0],right_arm=[-25,0,-25],left_arm=[-30,0,25])),
'grounddeath':(pose(-1.2,body=[42,25,0],right_arm=[-40,0,-15],left_arm=[-20,0,18]),pose(-1.7,body=[57,-10,0],right_arm=[-60,0,-30],left_arm=[-45,0,25])),
'kurouzu':(pose(body=[-5,22,0],right_arm=[-95,-12,-15],left_arm=[15,0,20]),pose(body=[15,40,0],right_arm=[-60,-25,-35],left_arm=[-20,0,25])),
'blackhole':(pose(body=[-12,0,0],right_arm=[-30,0,-65],left_arm=[-30,0,65]),pose(-1,body=[30,0,0],right_arm=[-55,0,-50],left_arm=[-55,0,50])),
'darkball':(pose(body=[-8,-22,0],right_arm=[-70,-25,-20],left_arm=[-70,25,20]),pose(body=[18,18,0],right_arm=[-105,0,-20],left_arm=[-105,0,20])),
'darkmatter':(pose(body=[-18,0,0],head=[-20,0,0],right_arm=[-170,0,-30],left_arm=[-170,0,30]),pose(-.8,body=[30,10,0],right_arm=[-60,0,-40],left_arm=[-60,0,40]))}
for name,(charge,release) in poses.items():
 duration=1 if name in ['iceage','grounddeath','blackhole','darkmatter','laser2'] else .7 if name in ['sables','whiteout','pheasantbeak'] else .1 if name in ['passive','yatanokagami'] else .5
 write('b01_'+name,[(0,neutral),(min(.15,duration*.5),charge),(duration,charge),(duration+.12,neutral)])
 write('b01_'+name+'_release',[(0,charge),(.12,release),(.23,release),(.45,pose(body=[3,0,0],right_arm=[-18,0,-5],left_arm=[-12,0,5])),(.7,neutral)])
f=R/'src/main/java/net/mcreator/minepiece/procedures/TechniqueAnimations.java';s=f.read_text(encoding='utf-8');needle='   private static final String[] CONNUES = {';s=s if '"b01_icepartisan"' in s else s.replace(needle,needle+'\n      '+','.join('"b01_'+n+suffix+'"' for n in poses for suffix in ['','_release'])+',');f.write_text(s,encoding='utf-8')
with zipfile.ZipFile(R.parent/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar') as z:
 model=json.loads(z.read('assets/minepiece/models/custom/amano_murakumo_sword.json'));model.pop('parent',None);model['textures']={'1':'minepiece:block/tex_pikapika_animation','particle':'minepiece:block/tex_pikapika_animation'}
 for dest,obj in [('models/item/amano_murakumo.json',model),('items/amano_murakumo.json',{'model':{'type':'minecraft:model','model':'minepiece:item/amano_murakumo'}})]:
  p=A/dest;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj),encoding='utf-8')
 for n in z.namelist():
  if n.startswith('assets/minepiece/textures/block/tex_pikapika_animation.png'):
   dest=R/'src/main/resources'/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
with zipfile.ZipFile(R/'build/libs/opworldlike-1.0.1-alpha.7.jar') as z:
 for language,title in [('fr_fr','Ame no Murakumo — sabre de lumière'),('en_us','Ame no Murakumo — light sword')]:
  n='assets/minepiece/lang/'+language+'.json';data=json.loads(z.read(n));data['item.minepiece.amano_murakumo']=title;dest=R/'src/main/resources'/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(data,ensure_ascii=False),encoding='utf-8')
print('Authored52 finite B01 phases and source light sword')
