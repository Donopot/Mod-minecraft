"""Author the private Magu poses; bake smooth curves into PAL's supported vector keys.

No runtime interpolation extension is required. Times are seconds, rotations degrees.
The impact keys retain the server's existing release/eruption timing.
"""
from pathlib import Path
import bisect
import json
import math

DEST = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/minepiece/player_animations'
BONES = ['body', 'torso', 'head', 'right_arm', 'left_arm', 'right_leg', 'left_leg']


def pose(height=0, **bones):
    return {b: bones.get(b, [0, 0, 0]) for b in BONES} | {'height': height}


neutral = pose()
wind = pose(body=[0,-22,0], torso=[8,-12,0], head=[-2,12,0], right_arm=[45,-30,-12],
            left_arm=[-35,15,25], right_leg=[-8,0,0], left_leg=[10,0,0])
brace = pose(body=[4,14,0], torso=[0,10,0], head=[-3,-10,0], right_arm=[-65,-15,-8],
             left_arm=[-50,-35,50], right_leg=[12,0,0], left_leg=[-12,0,0])
run = pose(-1, body=[28,0,0], head=[-12,0,0], right_arm=[45,0,-10], left_arm=[35,0,10],
           right_leg=[-40,0,0], left_leg=[40,0,0])


def offset(base, **changes):
    result = {k: list(v) if isinstance(v,list) else v for k,v in base.items()}
    result.update(changes)
    return result


def write(name, frames, breathing=False):
    times = [t for t,p in frames]
    length = times[-1]
    # Monotone Hermite: continuous velocity without inventing overshoots between poses.
    def sample(values, t):
        i = min(len(times)-2, max(0,bisect.bisect_right(times,t)-1))
        slopes = [(values[j+1]-values[j])/(times[j+1]-times[j]) for j in range(len(times)-1)]
        def tangent(j):
            if j in (0,len(times)-1): return 0
            a,b=slopes[j-1],slopes[j]
            return 0 if a*b<=0 else 2*a*b/(a+b)
        h=times[i+1]-times[i]; u=(t-times[i])/h
        return ((2*u**3-3*u*u+1)*values[i]+(u**3-2*u*u+u)*h*tangent(i)
                +(-2*u**3+3*u*u)*values[i+1]+(u**3-u*u)*h*tangent(i+1))
    samples=sorted(set(times+[round(i*.025,4) for i in range(math.ceil(length/.025))]))
    bones={}
    for bone in BONES:
        channels={}
        for channel in ('rotation','position') if bone=='body' else ('rotation',):
            keys={}
            for t in samples:
                vector=[]
                for axis in range(3):
                    values=[p[bone][axis] if channel=='rotation' else (p['height'] if axis==1 else 0) for _,p in frames]
                    value=sample(values,t)
                    # Charge has a subtle living hold; envelope returns to the authored endpoints.
                    envelope=min(1,max(0,(t-.25)*4),max(0,(length-.15-t)*5)) if breathing else 0
                    if channel=='rotation' and bone in ('torso','left_arm','right_arm') and axis==0:
                        value+=envelope*math.sin((t-.25)*math.pi*2/1.4+(0 if bone=='torso' else .4))*(.8 if bone=='torso' else 1.3)
                    if channel=='position' and axis==1: value+=envelope*.12*math.sin((t-.25)*math.pi*2/1.4)
                    vector.append(round(value,4))
                keys[f'{t:g}']={'vector':vector}
            channels[channel]=keys
        bones[bone]=channels
    data={'format_version':'1.8.0','animations':{name:{'animation_length':length,'bones':bones}}}
    (DEST/(name+'.json')).write_text(json.dumps(data,separators=(',',':'))+'\n',encoding='utf-8')


write('magu_fist_charge',[(0,neutral),(.09,offset(wind,body=[-3,-14,0],right_arm=[25,-25,-18])),(.24,wind),(5,wind),(5.1,neutral)],True)
write('magu_hound_charge',[(0,neutral),(.1,offset(brace,body=[9,10,0],left_arm=[-38,-25,58])),(.27,brace),(5,brace),(5.1,neutral)],True)
punch=pose(-.35,body=[8,24,-3],torso=[0,12,0],head=[-4,-14,0],right_arm=[-96,-5,-8],left_arm=[25,0,20],left_leg=[-12,0,0])
write('magu_fist_release',[(0,wind),(.1,punch),(.2,offset(punch,body=[12,29,-4],right_arm=[-102,-2,-7])),(.34,offset(punch,body=[6,15,-1],right_arm=[-70,0,-12])),(.53,pose(body=[-2,-3,0],right_arm=[8,0,-3])),(.65,neutral)])
palm=pose(-.5,body=[18,-12,0],torso=[8,-8,0],head=[-8,12,0],right_arm=[-100,12,-15],left_arm=[-85,-12,20],right_leg=[-20,0,0],left_leg=[15,0,0])
write('magu_hound_release',[(0,brace),(.05,offset(brace,body=[9,18,0])),(.1,palm),(.28,offset(palm,body=[22,-16,0],left_arm=[-95,-8,18])),(.47,offset(palm,body=[8,-6,0],right_arm=[-65,8,-18],left_arm=[-45,-8,25])),(.64,pose(body=[-2,2,0],right_arm=[5,0,-5],left_arm=[4,0,5])),(.75,neutral)])
run2=offset(run,right_leg=[38,0,0],left_leg=[-38,0,0],right_arm=[32,0,-12],left_arm=[48,0,12],height=-.65,body=[26,3,0])
# The finish replaces this track at tick 11; its entry pose matches this dash's last stride.
write('magu_kagero_dash',[(0,neutral),(.08,run),(.21,run2),(.34,run),(.47,run2),(.55,run),(.6,neutral)])
slam=pose(-3,body=[42,12,0],torso=[15,0,0],head=[-18,-6,0],right_arm=[-32,0,-5],left_arm=[35,0,40],right_leg=[-25,0,0],left_leg=[-15,0,0])
recover=pose(-1,body=[12,-8,0],head=[-4,4,0],right_arm=[-55,0,-35],left_arm=[20,0,20])
write('magu_kagero_finish',[(0,run),(.12,offset(slam,height=-1.7,right_arm=[-85,0,-12])),(.25,offset(slam,height=-2.2,right_arm=[-88,0,-12])),(.6,offset(slam,height=-2.5,right_arm=[-95,0,-15])),(.75,slam),(.84,offset(slam,height=-3.4,body=[46,15,0])),(1,recover),(1.45,offset(recover,height=-.6,body=[7,-4,0])),(1.82,pose(body=[-2,2,0],right_arm=[6,0,-4])),(2,neutral)])
ground=pose(-3,body=[28,0,0],torso=[15,0,0],head=[-12,0,0],right_arm=[-35,0,-18],left_arm=[-35,0,18],right_leg=[-25,0,-8],left_leg=[-25,0,8])
erupt=pose(body=[-10,0,0],head=[-8,0,0],right_arm=[-100,0,-65],left_arm=[-100,0,65],right_leg=[-8,0,-8],left_leg=[-8,0,8])
write('magu_volcanic_cast',[(0,neutral),(.18,ground),(.52,offset(ground,height=-3.3,body=[31,0,0])),(.68,offset(ground,height=-2,right_arm=[-58,0,-30],left_arm=[-58,0,30])),(.75,erupt),(1,offset(erupt,body=[-14,0,0],right_arm=[-110,0,-68],left_arm=[-110,0,68])),(1.28,erupt),(1.5,offset(erupt,body=[-12,0,0],right_arm=[-106,0,-68],left_arm=[-106,0,68])),(1.7,offset(erupt,body=[-4,0,0],right_arm=[-65,0,-40],left_arm=[-65,0,40])),(2,neutral)])
sky=pose(body=[-8,0,0],head=[-20,0,0],right_arm=[-165,0,-15],left_arm=[-165,0,15])
r=pose(body=[-6,-12,-2],head=[-20,6,0],right_arm=[-175,0,-8],left_arm=[-120,0,25])
l=pose(body=[-6,12,2],head=[-20,-6,0],right_arm=[-120,0,-25],left_arm=[-175,0,8])
write('magu_meteors_cast',[(0,neutral),(.15,pose(-.5,body=[8,0,0],right_arm=[-50,0,-30],left_arm=[-50,0,30])),(.4,sky),(.83,offset(sky,body=[-10,0,0])),(.95,sky),(1.08,r),(1.24,l),(1.4,r),(1.56,l),(1.72,r),(1.9,l),(2.02,offset(sky,body=[-3,0,0],right_arm=[-95,0,-18],left_arm=[-95,0,18])),(2.15,neutral)])
print('Authored 8 Magu tracks with sampled smooth curves and secondary motion')
