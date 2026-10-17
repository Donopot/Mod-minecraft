"""Reviewed ver15 visual bindings. Original bytes and adapted animation aliases have separate provenance."""
from pathlib import Path
import hashlib,json,math,re,zipfile
P=Path(__file__).resolve().parents[1];R=P.parent
J=R/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar'
assert hashlib.sha256(J.read_bytes()).hexdigest()=='f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704'
OUT=P/'src/main/resources/assets/minepiece';bindings=[];provenance=[]
def bind(ids,model,texture,source,**extra):
 bindings.append(dict(profiles=ids.split(','),model=model,texture=texture,source=source,**extra))
def write(path,data):
 p=OUT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
def adapted_animation(source,dest,track,body_position_scale=1):
 with zipfile.ZipFile(J) as z:
  path=f'assets/minepiece/animations/{source}.animation.json';raw=z.read(path);a=json.loads(raw)
 anim=a['animations'][track]
 if body_position_scale!=1:
  for frame in anim['bones'].get('body',{}).get('position',{}).values():
   if isinstance(frame,dict) and 'vector' in frame:frame['vector']=[v*body_position_scale for v in frame['vector']]
 # Existing generic controllers request idle (arms) or vertical (sword projectiles).
 data=json.dumps({'format_version':a['format_version'],'animations':{'idle':anim,'vertical':anim}},indent=2).encode()
 write(f'geckolib/animations/{dest}.animation.json',data)
 provenance.append({'kind':'animation_alias','source':path,'source_sha256':hashlib.sha256(raw).hexdigest(),'track':track,'body_position_scale':body_position_scale,'target':f'geckolib/animations/{dest}.animation.json'})

# Phase01: previously inspected original meshes. Preserve the proven anchor and scale.
bind('hiken','fist_small','flame_tiger','HikenProcedure -> FIST; FistLayer DATA_move>=0')
bind('icepartisan','partisan','tex_ice','IceBlockPartisanProcedure -> ENTITY_PARTISAN; EntityPartisanEntity texture')
bind('pheasantbeak','phoenix','tex_ice','IceBlockPheasantBeakProcedure -> ENTITY_BIRD; EntityBirdEntity texture',scale=.35,y=-1.9)
bind('goro_raigo','ball_maximum','tex_white2','Raigo -> METEOR DATA_mode=1 setTexture(tex_white2); original radius19.05 normalised to1.905 blocks',scale=.1)
bind('daienkai,mera_charge_ball','rubble','flame_tiger','DaiEnkaiEntei -> ENTITY_FLAME_BALL DATA_mode=0 default DATA_model=0 -> EntityFlameBallLayer flame_tiger; radius matched to the animated flame envelope',scale=2,y=-.5)

# Phase02: source texture setters, not name similarity. Existing sand projectiles and White Vine
# already use these exact meshes; listing them protects them from future procedural replacement.
bind('desertspada,desertlaspada','projectile_slash','slash_sand','DesertSpada/DesertLaSpada -> PROJECTILE_SLASH.setTexture(slash_sand)')
bind('whitevine','tornado','slash_white','WhiteVine -> TORNADO.setTexture(slash_white)',yaw=0)
bind('darkmatter','rubble','tex_black','DarkMatter -> ENTITY_DARK_BALL / EntityDarkBallModel',y=-.5)
bind('b01_sand_slash','entity_slash','slash_sand','Barjan -> ENTITY_SLASH.setTexture(slash_sand)',animation='source_slash_attack',scale=.7)
bind('b01_sand_wave','tornado','slash_sand','GroundDeath -> TORNADO.setTexture(slash_sand)',scale=.18,yaw=0)
bind('b02_air_crack,b02_quake_radial','crack','crack','Gekishin/Kabutowari/Kaishin -> ENTITY_CRACK / crack',animation='source_crack_attack',scale=.5)
bind('b02_quake_sweep','entity_slash','slash_white','AttackSweep -> ENTITY_SWEEP.setTexture(slash_white); source weapon texture branch remains explicit white adaptation',animation='source_slash_attack',scale=.5)
adapted_animation('entity_slash','source_slash_attack','idle1')
adapted_animation('crack','source_crack_attack','attack')

# EntityBallRenderer uses a Java Modelball, a sphere assembled from 57 rotated rods, not rubble.
# Convert the actual cube/pose constants. Living-renderer root y=24 becomes the projectile centre.
dump=R/'.runtime/onepiece/asset-source-audit/phase02/Modelball.txt'
parts=re.split(r'// String (ball_r\d+)\n',dump.read_text(encoding='utf-8-sig'));bones=[]
for i in range(1,len(parts),2):
 body=parts[i+1].split('Method net/minecraft/client/model/geom/PartPose.m_171423_',1)[0];v=[]
 for line in body.splitlines():
  if ' // float ' in line:v.append(float(line.split('// float ')[1].removesuffix('f')))
  elif m:=re.search(r': fconst_(\d)',line):v.append(float(m[1]))
 assert len(v)==13 and v[6]==0,(parts[i],v)
 bones.append({'name':parts[i],'pivot':[v[7],-v[8],v[9]],'rotation':[-math.degrees(v[10]),-math.degrees(v[11]),math.degrees(v[12])],
  'cubes':[{'origin':[v[0]+v[7],-v[1]-v[8]-v[4],v[2]+v[9]],'size':v[3:6],'uv':[0,0]}]})
assert len(bones)==57
write('geckolib/models/source_dark_ball.geo.json',json.dumps({'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.source_dark_ball','texture_width':6,'texture_height':6,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,0,0]},'bones':bones}]},indent=2).encode())
write('geckolib/animations/source_dark_ball.animation.json',json.dumps({'format_version':'1.8.0','animations':{'idle':{'loop':True,'animation_length':1}}}).encode())
provenance.append({'kind':'java_geometry_conversion','source_class':'net/mcreator/minepiece/client/model/Modelball','dump_sha256':hashlib.sha256(dump.read_bytes()).hexdigest(),'cubes':57,'root_translation':'Living root y=24 removed; geometry centred on projectile','target':'geckolib/models/source_dark_ball.geo.json'})
bind('darkball','source_dark_ball','tex_black','DarkBall -> ENTITY_BALL / EntityBallRenderer / Modelball',scale=6)

# Phase03: meteors, pads and rubble are shared source meshes, with source layers flattened to
# the visible material. The existing finite entities still determine their dimensions and expiry.
bind('b02_meteor,b02_meteorsmall','ball_maximum','meteor','MeteorSmall/FujitoraMeteor -> METEOR / MeteorModel; original radius19.05 normalised',scale=.1)
bind('b02_rubble,b02_stoneopeope','rubble','tex_stone','FujitoraRubble/StoneOpeOpe -> RUBBLE.setTexture(tex_stone)',y=-.5)
bind('b02_padho,b02_ursusshock','pad','tex_white','PadHo/UrsusShock -> PAD; PadLayer tex_white',scale=1.2)
bind('b03_mochi_donut','muso_donatsu','tex_white3','MusoDonatsu -> ENTITY_MUSO_DONATSU default texture',scale=.55)
bind('b03_mochi_yakimochi','fist_small','tex_black','YakiMochi -> FIST.setTexture(tex_black)')
bind('b03_riki_sweep','projectile_slash','slash_white','GalleonLariat -> PROJECTILE_SWEEP; clear base replaced by visible white material adaptation',scale=.55)

# Phase04: source projectile meshes were already packed but suppressed by generic geometry.
bind('b03_supa_projectileslash','projectile_slash','slash_white','SkillProjectileSlash -> PROJECTILE_SLASH; non-Haki visible slash adaptation')
bind('b03_riki_stonethrowing,b03_riki_stonethrowinglarge','rock_fragment','rock_fragment','StoneThrowing -> ROCK_FRAGMENT default texture',y=-.5)
bind('b04_aro_arrow','aroaro_arrow','aroaro_arrow','AroAroSkillArrow -> ENTITY_ARROW default model/texture; fifteen-degree presentation tilt for first-person readability',scale=.45,pitch=15)
bind('b04_aro_move,b04_aro_passive','aroaro_arrow','aroaro_arrow','AroAroSkillMove/Passive -> ENTITY_ARROW arrow1, body translation bounded',scale=.4,animation='source_aro_move')
bind('b04_aro_combo','aroaro_arrow','aroaro_arrow','AroAroSkillCombo -> ENTITY_ARROW attack1_1/attack1_2, body translation bounded',scale=.3,animation='source_aro_attack1')
adapted_animation('aroaro_arrow','source_aro_move','arrow1',.15)
adapted_animation('aroaro_arrow','source_aro_attack1','attack1_1',.15)
adapted_animation('aroaro_arrow','source_aro_attack2','attack1_2',.15)
bind('b04_horo_negativehollow','ghost1','ghost1_1','SkillNegativeHollow -> ENTITY_NEGATIVE_HOLLOW / ghost1 variant 1; centre at flying projectile origin',yaw=0,scale=.85,y=-.95)
bind('b04_horo_minihollow,b04_horo_tokuhoro','ghost2','ghost1_2','SkillMiniHollow/SkillTokuHoro -> ENTITY_MINI_HOLLOW / ghost1 random variant retained as 2; centred on projectile',yaw=0,scale=.85,y=-.62)

# Phase05: RotateKick's source flavour branch uses slash_white outside its fire/blue forms.
bind('b04_mero_rotatekick','entity_slash','slash_white','RotateKick -> ENTITY_SWEEP.setTexture(slash_white)',animation='source_slash_attack',scale=.45)
bind('b05_soru_attacksweep','entity_slash','slash_white','AttackSweep -> ENTITY_SWEEP.setTexture(slash_white)',animation='source_slash_attack',scale=.6)
bind('b05_uta_noteattack,b05_uta_notelaser','note_3d','haki_purple','NoteAttack/NoteLaser -> ENTITY_NOTE; five source colours selected stably by client entity ID; compact projectile presentation',scale=.8,y=-.5)
bind('b05_uta_posing,b05_uta_guard','rubble','haki_yellow','Posing/NoteShield -> ENTITY_IMPACT_WAVE.setTexture(haki_yellow); compact native core plus readable score ring',scale=.18,additive=True)

# Phase06/07: the wolf head and Gaicho's distinct texture are source-confirmed. Gomu and Imu
# already use the original arm cube layout with rebased pivots, so retain those animation adapters.
bind('b06_wolf_slash','jabra_wolf_projectile','jabra_wolf_projectile','Wolf Rankyaku/LupusFall -> ENTITY_WOLF_PROJECTILE default',scale=3,y=-.5)
bind('b06_bird_slash','projectile_slash','projectile_slash_gaicho','RankyakuGaicho -> PROJECTILE_SLASH.setTexture(projectile_slash_gaicho)',scale=3)
bind('b06_net','jabra_wolf_projectile','jabra_wolf_projectile','LocalAreaNetWork -> ENTITY_WOLF_PROJECTILE',scale=1)
bind('b06_gagan','projectile_tooth','haki_red','Gagan -> ENTITY_ENERGY DATA_number1=4; EntityEnergyLayer haki_red',scale=3)
bind('b06_spin','entity_slash','slash_blue','RankyakuAmaneDachi -> ENTITY_SLASH slash_blue idle4',animation='source_amane_dachi',scale=.45)
adapted_animation('entity_slash','source_amane_dachi','idle4')

# Phase08: BlueBird is a feather-robe fist in ver15, NOT Hie's phoenix ice bird.
bind('b07_phoenix_bluebird','fist_small','tex_blue','BlueBird -> ENTITY_DAI_FUNKA DATA_mode=-1; EntityDaiFunkaLayer tex_blue (base setter feather_robe names a missing texture)',scale=2.5)
bind('b07_seiryu_kaifu','projectile_slash','slash_green','Kaifu -> PROJECTILE_SLASH.setTexture(slash_green) PlayAnimationEntity2(vertical)',scale=2.5,animation='source_kaifu')
adapted_animation('projectile_slash','source_kaifu','vertical')
bind('b07_seiryu_kongokabura','projectile_slash','slash_purple','KongoKabura -> ENTITY_ENERGY DATA_number1=1 slash_purple idle1; slight presentation tilt for frontal readability',scale=2,pitch=12,animation='source_energy_slash')
adapted_animation('projectile_slash','source_energy_slash','idle1')
bind('b07_daibutsu_impactwave','rubble','tex_white_trans','ImpactWave -> ENTITY_IMPACT_WAVE.setTexture(tex_white_trans); compact additive core on the finite ray',scale=.4,additive=True)
bind('b07_seiryu_borobreath','rubble','flame_tiger','BoroBreath -> ENTITY_IMPACT_WAVE.setTexture(flame_tiger); compact additive core on the finite ray',scale=.4,additive=True)
bind('b08_brachio_blackcofee','rubble','haki_yellow','BlackCofee -> ENTITY_IMPACT_WAVE.setTexture(haki_yellow)',scale=4,additive=True)

# Item projectile: preserve the actual heart sprite, adapted to a thin double-sided world mesh.
heart={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.source_heart','texture_width':16,'texture_height':16,'visible_bounds_width':2,'visible_bounds_height':2,'visible_bounds_offset':[0,0,0]},'bones':[{'name':'heart','pivot':[0,0,0],'cubes':[{'origin':[-8,-8,-.05],'size':[16,16,.1],'uv':{face:{'uv':[0,0],'uv_size':[16,16]} for face in ['north','south']}}]}]}]}
write('geckolib/models/source_heart.geo.json',json.dumps(heart,indent=2).encode())
write('geckolib/animations/source_heart.animation.json',json.dumps({'format_version':'1.8.0','animations':{'idle':{'loop':True,'animation_length':1}}}).encode())
bind('b04_mero_pistolkiss','source_heart','textures/item/heart','HeartBulletEntity PROJECTILE_ITEM=ITEM_HEART; item_heart.json -> textures/item/heart.png',scale=1)

with zipfile.ZipFile(J) as z:
 for b in bindings:
  for kind,ext,sourcefolder,targetfolder in [('model','.geo.json','geo','geckolib/models'),('animation','.animation.json','animations','geckolib/animations'),('texture','.png','textures/entities','textures/entities')]:
   stem=b.get(kind,b['model'])
   if kind=='texture' and stem.startswith('textures/'):sourcefolder=targetfolder=str(Path(stem).parent).replace('\\','/');stem=Path(stem).name
   path=f'assets/minepiece/{sourcefolder}/{stem}{ext}'
   if path in z.namelist():
    raw=z.read(path);write(f'{targetfolder}/{stem}{ext}',raw)
    provenance.append({'kind':'original_bytes','source':path,'sha256':hashlib.sha256(raw).hexdigest(),'target':f'{targetfolder}/{stem}{ext}'})
   else:assert (OUT/f'{targetfolder}/{stem}{ext}').is_file(),path
write('source_visuals.json',json.dumps({'source_jar_sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'bindings':bindings},indent=2).encode())
(P/'build/source-visuals-provenance.local.json').write_text(json.dumps(provenance,indent=2),encoding='utf-8')
print('REVIEWED_BINDINGS',sum(len(b['profiles']) for b in bindings),'PROVENANCE',len(provenance))
