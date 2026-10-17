"""Private B04 assets; only this lot's files are authored. No historical generators are run."""
from pathlib import Path
import json,zipfile,hashlib
import ast,bisect,math
helper=Path(__file__).with_name('author_magu_animations.py')
module=ast.parse(helper.read_text(encoding='utf-8'))
namespace={'Path':Path,'bisect':bisect,'math':math,'json':json,'__file__':str(helper)}
for node in module.body:
 if isinstance(node,(ast.FunctionDef,)) or isinstance(node,ast.Assign) and any(isinstance(t,ast.Name) and t.id in ['DEST','BONES','neutral'] for t in node.targets):
  exec(compile(ast.Module(body=[node],type_ignores=[]),str(helper),'exec'),namespace)
pose,write,neutral=(namespace[n] for n in ['pose','write','neutral'])
P=Path(__file__).resolve().parents[1]
R=P/'src/main/resources/assets/minepiece'
reference=P.parent/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar'
assert hashlib.sha256(reference.read_bytes()).hexdigest()=='f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704'
with zipfile.ZipFile(reference) as z:
 for origin,destination in [('geo/biscuit_warrrior.geo.json','geckolib/models/biscuit_warrrior.geo.json'),('animations/biscuit_warrrior.animation.json','geckolib/animations/biscuit_warrrior.animation.json'),('textures/entities/biscuit_warrrior.png','textures/entities/biscuit_warrrior.png'),('textures/entities/ghost1_2.png','textures/entities/ghost1_2.png'),('textures/item/biscuit.png','textures/item/biscuit.png')]:
  target=R/destination;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read('assets/minepiece/'+origin))
 for model in ['biscuit_warrrior','kumac','ghost1','ghost2','aroaro_arrow']:
  source='assets/minepiece/animations/'+model+'.animation.json'
  data=json.loads(z.read(source));print('NATIVE',model,list(data['animations']))
# Native head east UV starts at-2; clamp this two-pixel source defect inside its256atlas.
mesh=R/'geckolib/models/biscuit_warrrior.geo.json';data=json.loads(mesh.read_text(encoding='utf-8'))
for geometry in data['minecraft:geometry']:
 for bone in geometry['bones']:
  for cube in bone.get('cubes',[]):
   if bone['name']=='head' and isinstance(cube.get('uv'),dict) and 'east' in cube['uv']:
    uv=cube['uv']['east'].get('uv')
    if uv and uv[0]<0:uv[0]=0
mesh.write_text(json.dumps(data,separators=(',',':')),encoding='utf-8')
(R/'geckolib/animations/heart_bullet.animation.json').write_text(json.dumps({'format_version':'1.8.0','animations':{'idle':{'loop':True,'animation_length':1,'bones':{}}}},indent=2)+'\n',encoding='utf-8')
(R/'items/biscuit.json').write_text(json.dumps({'model':{'type':'minecraft:model','model':'minepiece:item/biscuit'}}),encoding='utf-8')
(R/'models/item/biscuit.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'minepiece:item/biscuit'}}),encoding='utf-8')
POSES={
'aro_arrow':(pose(body=[-8,35,0],right_arm=[-110,-45,-12],left_arm=[-45,0,35]),pose(body=[15,-30,0],right_arm=[-90,0,-5],left_arm=[25,0,20])),
'aro_move':(pose(-.4,body=[35,-25,0],right_arm=[25,0,-25],left_arm=[-80,0,20]),pose(body=[20,30,0],right_arm=[-110,0,-25],left_arm=[35,0,10])),
'aro_combo':(pose(body=[-8,-40,0],right_arm=[-35,0,-60],left_arm=[-70,0,30]),pose(body=[20,55,0],right_arm=[-105,0,-15],left_arm=[40,0,25])),
'aro_passive':(pose(-.5,body=[45,0,0],right_arm=[45,0,-20],left_arm=[45,0,20]),pose(body=[22,0,-15],right_arm=[-25,-30,-75],left_arm=[-25,30,75])),
'horo_negativehollow':(pose(body=[-8,20,0],right_arm=[-80,-25,-15],left_arm=[-65,25,20]),pose(body=[5,-10,0],right_arm=[-105,0,-35],left_arm=[-55,0,45])),
'horo_minihollow':(pose(body=[-5,-15,0],right_arm=[-70,-35,-60],left_arm=[-70,35,60]),pose(body=[12,15,0],right_arm=[-90,0,-75],left_arm=[-90,0,75])),
'horo_tokuhoro':(pose(-.4,body=[-25,0,0],right_arm=[-155,0,-35],left_arm=[-155,0,35]),pose(body=[30,0,0],right_arm=[-80,0,-20],left_arm=[-80,0,20])),
'horo_rap':(pose(body=[0,15,0],right_arm=[-95,0,-40],left_arm=[-80,0,40]),pose(body=[10,-15,0],right_arm=[-75,-45,35],left_arm=[-75,45,-35])),
'horo_kumac':(pose(body=[5,-35,0],right_arm=[-65,-15,-50],left_arm=[-90,35,10]),pose(body=[-10,25,0],right_arm=[-130,0,-50],left_arm=[-45,0,35])),
'mero_pistolkiss':(pose(body=[-2,25,0],right_arm=[-110,-65,10],left_arm=[5,0,15]),pose(body=[10,-15,0],right_arm=[-90,0,-8],left_arm=[15,0,20])),
'mero_roundhousekick':(pose(body=[-5,-70,10],right_leg=[-20,10,-15],left_leg=[15,0,5],right_arm=[-35,0,-20]),pose(body=[5,75,-25],right_leg=[-95,35,-35],left_leg=[8,0,8],left_arm=[-45,0,50])),
'mero_rotatekick':(pose(-.3,body=[25,-90,0],right_leg=[-50,0,-35],left_leg=[10,0,10]),pose(body=[10,140,-30],right_leg=[-95,0,-80],left_leg=[15,0,10],right_arm=[-45,0,-65])),
'mero_perfumefemur':(pose(body=[-10,40,0],right_leg=[-75,0,-10],left_leg=[5,0,0],right_arm=[-35,0,-45]),pose(body=[25,-25,10],right_leg=[-110,0,-20],left_leg=[15,0,0],left_arm=[-45,0,40])),
'kiro_attackoverhead':(pose(body=[-25,15,0],right_arm=[-170,0,-10],left_arm=[-95,0,25]),pose(-.5,body=[50,-15,0],right_arm=[-30,0,-25],left_arm=[-55,0,20])),
'kiro_attackjump':(pose(-.7,body=[40,0,0],right_leg=[-45,0,-10],left_leg=[40,0,10],right_arm=[40,0,-20]),pose(.3,body=[-20,10,0],right_leg=[45,0,-15],left_leg=[-35,0,15],right_arm=[-155,0,-25])),
'kiro_kirooverhead':(pose(-.7,body=[25,0,0],right_leg=[-50,0,0],left_leg=[-50,0,0],right_arm=[-165,0,-45],left_arm=[-165,0,45]),pose(-.4,body=[45,0,0],right_leg=[-80,0,-15],left_leg=[-80,0,15],right_arm=[-35,0,-30],left_arm=[-35,0,30])),
'kiro_weightmanipulation':(pose(body=[-5,0,0],right_arm=[-75,0,-35],left_arm=[-75,0,35]),pose(-.3,body=[18,0,0],right_arm=[15,0,-20],left_arm=[15,0,20],right_leg=[-12,0,-8],left_leg=[-12,0,8])),
'bisu_crashpretzel':(pose(body=[-5,55,0],right_arm=[-65,-45,-15],left_arm=[-55,20,20]),pose(body=[20,-45,0],right_arm=[-95,0,-5],left_arm=[20,0,30])),
'bisu_biscuitwarrior':(pose(body=[-5,15,0],right_arm=[-75,-35,15],left_arm=[-75,35,-15]),pose(body=[12,-15,0],right_arm=[-95,0,-55],left_arm=[-95,0,55])),
'bisu_biscuitwarrior2':(pose(-.4,body=[-15,-15,0],right_arm=[-140,-20,-45],left_arm=[-140,20,45]),pose(body=[20,15,0],right_arm=[-70,0,-70],left_arm=[-70,0,70])),
'bisu_biscuit':(pose(body=[-3,0,0],right_arm=[-80,0,-45],left_arm=[-80,0,45]),pose(body=[8,0,0],right_arm=[-80,-40,30],left_arm=[-80,40,-30]))}
for key,(charge,release) in POSES.items():
 id=key.split('_',1)[1];windup=20 if id in ['tokuhoro','kirooverhead','biscuitwarrior2'] else 14 if id=='attackoverhead' else 10;t=windup/20
 write('b04_'+key,[(0,neutral),(.12,charge),(t,charge),(t+.12,neutral)])
 frames=[(0,charge),(.1,release),(.25,release)]
 if id=='combo':frames=[(0,charge),(.1,release),(.35,charge),(.7,pose(body=[20,-65,0],right_arm=[35,0,-30],left_arm=[-110,0,15])),(.85,charge),(1,neutral)]
 elif id=='biscuit':frames=[(0,charge),(.15,release),(.22,charge),(.3,release),(.5,charge),(.65,release),(.85,neutral)]
 elif id in ['attackjump','kirooverhead']:frames=[(0,charge),(.2,release),(.6,release),(1.15,pose(-.4,body=[45,0,0],right_leg=[-85,0,0],left_leg=[-85,0,0])),(1.4,neutral)]
 else:frames+=[(.55,neutral)]
 write('b04_'+key+'_release',frames)
path=P/'src/main/java/net/mcreator/minepiece/procedures/TechniqueAnimations.java';s=path.read_text(encoding='utf-8');needle='   private static final String[] CONNUES = {'
if '"b04_aro_arrow"' not in s:s=s.replace(needle,needle+'\n      '+','.join('"b04_'+n+suffix+'"' for n in POSES for suffix in ['', '_release'])+',')
path.write_text(s,encoding='utf-8')
print('B04 authored42 finite phases and native biscuit assets')
