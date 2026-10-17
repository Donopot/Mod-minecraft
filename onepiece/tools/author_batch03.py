from pathlib import Path
from author_magu_animations import pose,write,neutral
P=Path(__file__).resolve().parents[1]
POSES={
'mochi_mochipistol':(pose(body=[-8,40,0],right_arm=[45,0,-20],left_arm=[-50,0,20]),pose(body=[18,-35,0],right_arm=[-95,0,-5],left_arm=[10,0,15])),
'mochi_mochigatling':(pose(body=[-5,0,0],right_arm=[-60,-15,-20],left_arm=[-70,15,20]),pose(body=[20,12,0],right_arm=[-110,0,-8],left_arm=[40,0,15])),
'mochi_yakimochi':(pose(body=[-12,30,0],right_arm=[-10,-40,-40],left_arm=[-50,0,25]),pose(body=[25,-45,0],right_arm=[-110,0,-20],left_arm=[10,0,20])),
'mochi_zangirimochi':(pose(-.6,body=[65,-20,0],right_arm=[30,0,-45],left_arm=[30,0,45],right_leg=[-40,0,0]),pose(body=[25,110,25],right_arm=[-150,0,-70],left_arm=[-40,0,30])),
'mochi_musodonatsu':(pose(body=[-10,0,0],right_arm=[-70,-30,-60],left_arm=[-70,30,60]),pose(body=[8,15,0],right_arm=[-130,0,-55],left_arm=[-130,0,55])),
'mochi_mochibuild':(pose(-.6,body=[35,0,0],right_arm=[-65,0,-25],left_arm=[-65,0,25]),pose(body=[12,0,0],right_arm=[-110,0,-5],left_arm=[-110,0,5])),
'mori_root':(pose(body=[15,25,0],right_arm=[-45,-35,-15],left_arm=[10,0,20]),pose(body=[-5,-15,0],right_arm=[-110,0,-30],left_arm=[-30,0,25])),
'mori_rootgiant':(pose(-.5,body=[45,-10,0],right_arm=[-35,0,-30],left_arm=[-35,0,30]),pose(body=[-18,10,0],right_arm=[-165,0,-40],left_arm=[-165,0,40])),
'mori_assault':(pose(body=[-15,20,0],right_arm=[-165,0,-15],left_arm=[-140,0,25]),pose(-.7,body=[50,-20,0],right_arm=[-55,0,-30],left_arm=[-65,0,35])),
'mori_rootsurround':(pose(-.7,body=[45,0,0],right_arm=[-70,-20,-10],left_arm=[-70,20,10]),pose(body=[-10,0,0],right_arm=[-95,0,-100],left_arm=[-95,0,100])),
'kira_attackoverhead':(pose(body=[-20,5,0],right_arm=[-175,0,-8],left_arm=[-65,0,20]),pose(body=[50,-5,0],right_arm=[-40,0,-30],left_arm=[-80,0,25])),
'kira_brilliantpunk':(pose(-.3,body=[25,60,0],right_arm=[-55,0,-25],left_arm=[-60,0,20]),pose(body=[35,-55,-20],right_arm=[-85,0,-70],left_arm=[25,0,20])),
'kira_diamondshard':(pose(body=[-4,35,0],right_arm=[-125,-20,-35],left_arm=[-25,0,20]),pose(body=[18,-25,0],right_arm=[-90,0,-10],left_arm=[20,0,30])),
'kira_diamondavalanche':(pose(body=[-25,0,0],right_arm=[-155,0,-45],left_arm=[-155,0,45]),pose(body=[35,0,0],right_arm=[-70,0,-15],left_arm=[-70,0,15])),
'kira_diamondbody':(pose(body=[-8,0,0],right_arm=[-80,15,-25],left_arm=[-80,-15,25]),pose(body=[5,0,0],right_arm=[-30,0,-45],left_arm=[-30,0,45])),
'supa_projectileslash':(pose(body=[-5,-60,0],right_arm=[-70,0,50],left_arm=[-50,0,25]),pose(body=[20,65,0],right_arm=[-95,0,-75],left_arm=[25,0,25])),
'supa_atomicspurt':(pose(-.5,body=[40,0,0],right_arm=[20,0,-25],left_arm=[15,0,25]),pose(body=[25,-15,0],right_arm=[-110,0,-35],left_arm=[-110,0,35])),
'supa_sparklingdaisy':(pose(body=[-4,20,0],right_arm=[-65,-40,-20],left_arm=[-65,40,20]),pose(body=[20,-15,0],right_arm=[-95,0,-75],left_arm=[-95,0,75])),
'supa_sparbreak':(pose(body=[-12,0,0],right_arm=[-160,25,35],left_arm=[-160,-25,-35]),pose(-.4,body=[45,0,0],right_arm=[-45,0,-55],left_arm=[-45,0,55])),
'riki_attackoverhead':(pose(body=[-28,0,0],right_arm=[-170,0,-20],left_arm=[-170,0,20]),pose(-.8,body=[55,0,0],right_arm=[-30,0,-15],left_arm=[-30,0,15])),
'riki_hadoelbow':(pose(body=[-5,55,0],right_arm=[-75,-65,-10],left_arm=[-55,0,20]),pose(body=[20,-65,0],right_arm=[-100,-85,-20],left_arm=[25,0,20])),
'riki_galleonlariat':(pose(-.4,body=[30,-45,0],right_arm=[-20,0,-100],left_arm=[30,0,15]),pose(body=[18,90,15],right_arm=[-80,0,-100],left_arm=[35,0,30])),
'riki_stonethrowing':(pose(body=[-8,35,0],right_arm=[-135,0,-25],left_arm=[15,0,25]),pose(body=[25,-30,0],right_arm=[-55,0,-15],left_arm=[30,0,25])),
'riki_stonethrowinglarge':(pose(-.7,body=[25,0,0],right_arm=[-145,0,-55],left_arm=[-145,0,55]),pose(body=[40,0,0],right_arm=[-65,0,-20],left_arm=[-65,0,20]))}
for key,(charge,release) in POSES.items():
 id=key.split('_',1)[1];windup=20 if id in ['musodonatsu','mochibuild','diamondbody','stonethrowinglarge','diamondavalanche','rootgiant','rootsurround'] else 14 if id in ['attackoverhead','sparbreak'] else 10
 t=windup/20;write('b03_'+key,[(0,neutral),(.12,charge),(t,charge),(t+.12,neutral)])
 frames=[(0,charge),(.12,release),(.3,release)]
 if id=='mochigatling':
  other=pose(body=[18,-12,0],right_arm=[40,0,-15],left_arm=[-110,0,8]);frames=[(0,charge),(.12,release),(.48,release),(.6,other),(.98,other),(1.1,release),(1.48,release),(1.6,other),(1.98,other),(2.1,release),(2.48,release),(2.6,other),(2.8,other),(3.1,neutral)]
 else:frames+=[(.55,pose(body=[3,0,0],right_arm=[-15,0,-5],left_arm=[-10,0,5])),(.8,neutral)]
 write('b03_'+key+'_release',frames)
p=P/'src/main/java/net/mcreator/minepiece/procedures/TechniqueAnimations.java';s=p.read_text(encoding='utf-8');needle='   private static final String[] CONNUES = {'
if '"b03_mochi_mochipistol"' not in s:s=s.replace(needle,needle+'\n      '+','.join('"b03_'+n+suffix+'"' for n in POSES for suffix in ['', '_release'])+',')
p.write_text(s,encoding='utf-8');print('B03 authored48 distinct finite phases')
