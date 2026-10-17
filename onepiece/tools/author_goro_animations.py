"""Private authored Goro gestures, using the same standard PAL curve baker as Magu.

Import regenerates the unchanged Magu assets too; frozen Magu profiles are never touched.
"""
from author_magu_animations import pose, offset, write, neutral

vari=pose(-.7,body=[18,-12,0],head=[-7,8,0],right_arm=[38,-25,-15],left_arm=[-25,15,20],right_leg=[-20,0,0],left_leg=[18,0,0])
thor=pose(-.2,body=[-5,0,0],head=[-18,0,0],right_arm=[-145,0,-25],left_arm=[-145,0,25],right_leg=[-8,0,-5],left_leg=[-8,0,5])
sango=pose(body=[-4,-10,0],head=[-12,8,0],right_arm=[-160,-8,-12],left_arm=[-50,-30,40])
kari=pose(-.4,body=[10,15,0],head=[-5,-10,0],right_arm=[-58,-22,-25],left_arm=[-40,25,35],right_leg=[10,0,0],left_leg=[-14,0,0])
for name,hold in [('vari',vari),('elthor',thor),('sango',sango),('karikrash',kari)]:
    write('goro_'+name+'_charge',[(0,neutral),(.15,offset(hold,height=hold['height']-.2)),(.32,hold),(5,hold),(5.1,neutral)],True)
write('goro_vari_release',[(0,vari),(.12,pose(-.8,body=[32,0,0],head=[-15,0,0],right_arm=[45,0,-10],left_arm=[45,0,10],right_leg=[-32,0,0],left_leg=[32,0,0])),(.25,pose(body=[9,22,0],right_arm=[-92,0,-10],left_arm=[25,0,20],left_leg=[-15,0,0])),(.42,pose(body=[7,12,0],right_arm=[-75,0,-18])),(.72,pose(right_arm=[8,0,0])),(.9,neutral)])
write('goro_elthor_release',[(0,thor),(.3,offset(thor,body=[-10,0,0],right_arm=[-175,0,-15],left_arm=[-165,0,18])),(.5,pose(-1.4,body=[32,0,0],head=[-14,0,0],right_arm=[-45,0,-8],left_arm=[-55,0,15],right_leg=[-18,0,0],left_leg=[-18,0,0])),(.61,pose(-1.6,body=[36,0,0],right_arm=[-35,0,-10],left_arm=[-40,0,15])),(.82,pose(body=[6,0,0],right_arm=[-15,0,-5])),(1.05,neutral)])
write('goro_sango_release',[(0,sango),(.12,pose(body=[-12,-14,0],head=[-22,8,0],right_arm=[-178,0,-6],left_arm=[-65,0,25])),(.3,pose(body=[8,18,0],right_arm=[-100,0,-14],left_arm=[15,0,25])),(.55,pose(body=[3,6,0],right_arm=[-45,0,-10])),(.85,neutral)])
write('goro_karikrash_release',[(0,kari),(.2,pose(-.6,body=[18,-18,0],head=[-10,12,0],right_arm=[-100,5,-15],left_arm=[-95,-12,20],right_leg=[-12,0,0],left_leg=[10,0,0])),(.28,pose(-.8,body=[24,-22,0],right_arm=[-106,0,-12],left_arm=[-100,0,18])),(.48,pose(body=[6,-5,0],right_arm=[-48,0,-15],left_arm=[-35,0,22])),(.75,neutral)])
write('goro_storm_cast',[(0,neutral),(.2,pose(body=[-6,-12,0],head=[-15,8,0],right_arm=[-110,-10,-35],left_arm=[-35,0,35])),(.45,pose(body=[-8,8,0],head=[-18,-6,0],right_arm=[-150,0,-50],left_arm=[-65,0,55])),(.8,pose(body=[-3,0,0],right_arm=[-95,0,-35],left_arm=[-35,0,25])),(1.25,neutral)])
write('goro_blink',[(0,neutral),(.07,pose(-.6,body=[32,0,0],head=[-14,0,0],right_arm=[50,0,-12],left_arm=[50,0,12],right_leg=[-30,0,0],left_leg=[30,0,0])),(.2,pose(-.3,body=[20,0,0],head=[-8,0,0],right_arm=[25,0,-8],left_arm=[25,0,8])),(.34,pose(body=[-3,0,0],right_arm=[-8,0,0],left_arm=[-8,0,0])),(.5,neutral)])
write('goro_raigo_cast',[(0,neutral),(.3,pose(body=[-8,0,0],head=[-24,0,0],right_arm=[-160,0,-18],left_arm=[-160,0,18])),(.8,pose(body=[-12,0,0],head=[-25,0,0],right_arm=[-175,0,-12],left_arm=[-175,0,12])),(1,pose(-.6,body=[25,0,0],head=[-12,0,0],right_arm=[-65,0,-20],left_arm=[-65,0,20])),(1.15,pose(body=[12,0,0],right_arm=[-35,0,-15],left_arm=[-35,0,15])),(1.5,neutral)])
print('Authored 11 distinct finite Goro gestures')
