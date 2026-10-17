"""Authored finite B02 gestures. Private player animation resources only."""
from pathlib import Path
from author_magu_animations import pose,write,neutral
R=Path(__file__).resolve().parents[1]
poses={
'attacksweep':(pose(body=[-5,-40,0],right_arm=[-40,0,-80],left_arm=[-20,0,25]),pose(body=[10,50,0],right_arm=[-80,0,65],left_arm=[20,0,20])),
'overheadsuper':(pose(-.4,body=[-18,0,0],right_arm=[-170,0,-15],left_arm=[-130,0,20]),pose(-1,body=[44,0,0],right_arm=[-35,0,-20],left_arm=[-40,0,20])),
'gekishin':(pose(body=[-8,38,0],right_arm=[40,0,-20],left_arm=[-40,0,30]),pose(body=[14,-40,0],right_arm=[-95,0,-10],left_arm=[25,0,20])),
'kabutowari':(pose(body=[18,15,0],right_arm=[-95,-20,-5],left_arm=[-55,0,20]),pose(-.6,body=[35,-20,0],right_arm=[-55,0,-5],left_arm=[-100,0,25])),
'tenchimeido':(pose(body=[-12,0,0],right_arm=[-40,0,-95],left_arm=[-40,0,95]),pose(body=[15,35,18],right_arm=[-55,0,-55],left_arm=[-115,0,80])),
'kaishin':(pose(-.6,body=[28,0,0],right_arm=[-35,0,-45],left_arm=[-35,0,45]),pose(body=[-12,0,0],right_arm=[-120,0,-65],left_arm=[-120,0,65])),
'nosefancycannon':(pose(body=[-3,-20,0],right_arm=[-130,0,10],left_arm=[10,0,15]),pose(body=[8,25,0],right_arm=[-85,0,-35],left_arm=[20,0,20])),
'kickbomb':(pose(-.4,body=[-12,10,0],left_leg=[-55,0,10],right_arm=[-25,0,-30]),pose(body=[-28,-32,8],left_leg=[-105,0,12],right_leg=[15,0,0],right_arm=[-55,0,-35])),
'punchbomb':(pose(body=[-6,35,0],right_arm=[55,0,-18],left_arm=[-55,0,25]),pose(body=[18,-45,0],right_arm=[-110,0,-15],left_arm=[15,0,20])),
'bomboverhead':(pose(-1,body=[32,0,0],right_arm=[30,0,-25],left_arm=[30,0,25]),pose(body=[22,0,0],right_arm=[-165,0,-22],left_arm=[-165,0,22],right_leg=[-40,0,0],left_leg=[-35,0,0])),
'zenshinkibaku':(pose(-.3,body=[20,0,0],right_arm=[-85,-25,-10],left_arm=[-85,25,10]),pose(body=[-18,0,0],right_arm=[-25,0,-100],left_arm=[-25,0,100],right_leg=[10,0,-8],left_leg=[10,0,8])),
'attackspeed':(pose(-.5,body=[35,-20,0],right_arm=[30,0,-15],left_arm=[40,0,18]),pose(body=[15,42,0],right_arm=[-100,0,-30],left_arm=[15,0,20])),
'gravito':(pose(body=[-8,-25,0],right_arm=[-155,0,-20],left_arm=[-30,0,30]),pose(-.5,body=[32,15,0],right_arm=[-45,0,-15],left_arm=[15,0,15])),
'meteorsmall':(pose(body=[-12,15,0],right_arm=[-160,0,-10],left_arm=[10,0,20],head=[-15,0,0]),pose(body=[10,-25,0],right_arm=[-80,0,-20],left_arm=[20,0,15])),
'moko':(pose(body=[-5,-45,0],right_arm=[-70,0,55],left_arm=[-60,0,20]),pose(body=[15,55,0],right_arm=[-90,0,-65],left_arm=[25,0,25])),
'rubble':(pose(-.3,body=[22,15,0],right_arm=[-55,0,-25],left_arm=[-45,0,30]),pose(body=[-5,-20,0],right_arm=[-120,-15,-35],left_arm=[-105,15,35])),
'meteor':(pose(body=[-20,0,0],head=[-28,0,0],right_arm=[-170,0,-40],left_arm=[-170,0,40]),pose(-.4,body=[32,20,0],right_arm=[-40,0,-35],left_arm=[-50,0,30])),
'padho':(pose(body=[-5,25,0],right_arm=[-50,-35,-10],left_arm=[-35,0,25]),pose(body=[12,-20,0],right_arm=[-95,0,-3],left_arm=[25,0,20])),
'ursusshock':(pose(body=[-8,0,0],right_arm=[-75,-35,-25],left_arm=[-75,35,25]),pose(body=[14,0,0],right_arm=[-105,-5,-15],left_arm=[-105,5,15])),
'assault':(pose(-.5,body=[35,12,0],right_arm=[40,0,-15],left_arm=[-65,0,20]),pose(body=[18,-40,0],right_arm=[-100,-5,-10],left_arm=[20,0,30])),
'kumapunch':(pose(body=[-6,45,0],right_arm=[45,0,-35],left_arm=[-70,0,30]),pose(body=[18,-55,0],right_arm=[-110,0,-18],left_arm=[35,0,25])),
'passive':(pose(-.4,body=[25,-8,0],right_arm=[30,0,-15],left_arm=[25,0,15]),pose(body=[-5,12,0],right_arm=[-65,0,-30],left_arm=[-65,0,30],right_leg=[-25,0,0],left_leg=[25,0,0])),
'stoneopeope':(pose(body=[-8,0,0],right_arm=[-55,-25,-60],left_arm=[-55,25,60]),pose(body=[10,18,0],right_arm=[-105,-10,-20],left_arm=[-95,10,20])),
'room':(pose(-.6,body=[32,0,0],right_arm=[-70,-25,-15],left_arm=[-35,0,30]),pose(body=[-6,0,0],right_arm=[-110,0,-60],left_arm=[-110,0,60])),
'shambles':(pose(body=[-4,20,0],right_arm=[-70,-40,-20],left_arm=[-70,40,20]),pose(body=[6,-20,0],right_arm=[-105,20,-15],left_arm=[-105,-20,15])),
'amputation':(pose(body=[-6,-40,0],right_arm=[-150,0,-20],left_arm=[-50,0,15]),pose(body=[28,50,0],right_arm=[-75,0,65],left_arm=[20,0,35]))}
for name,(charge,release) in poses.items():
 ticks=40 if name=='ursusshock' else 20 if name in ['tenchimeido','zenshinkibaku','meteor','meteorsmall','room'] else 14 if name in ['stoneopeope','kaishin','kabutowari','moko'] else 8 if name=='passive' else 10
 duration=ticks/20
 write('b02_'+name,[(0,neutral),(.12,charge),(duration,charge),(duration+.12,neutral)])
 write('b02_'+name+'_release',[(0,charge),(.12,release),(.28,release),(.5,pose(body=[3,0,0],right_arm=[-15,0,-5],left_arm=[-10,0,5])),(.75,neutral)])
p=R/'src/main/java/net/mcreator/minepiece/procedures/TechniqueAnimations.java'
s=p.read_text(encoding='utf-8');needle='   private static final String[] CONNUES = {'
if '"b02_attacksweep"' not in s:s=s.replace(needle,needle+'\n      '+','.join('"b02_'+n+suffix+'"' for n in poses for suffix in ['', '_release'])+',')
p.write_text(s,encoding='utf-8')
print('B02 authored52 distinct finite phases')
