"""Initial B06 resource/wiring authoring. Only B06 resources are regenerated."""
from pathlib import Path
import json,re,zipfile
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';A=P/'src/main/resources/assets/minepiece'
catalogue={'wolf':['shigan','soru','rankyaku','lupusfall','okamihajiki','jushigan','localareanetwork','poursuite'],'leopard':['shigan','soru','rankyaku','gaicho','attackjump','shugan','rokuogan'],'saber':['shigan','attackspeed','shiganmadara','gagan','kibasen'],'giraffe':['shigan','soru','rankyaku','rankyakuran','amanedachi'],'batto':['shigan','soru','rankyaku','tobushigan','kamiezanshin','yurushitene']}
(P/'build/b06-moves.local.json').write_text(json.dumps(catalogue,indent=2),encoding='utf-8')
def replace(rel,old,new):
 p=J/rel;s=p.read_text(encoding='utf-8')
 if new in s:return
 assert old in s,(rel,old[:80]);p.write_text(s.replace(old,new,1),encoding='utf-8')
proc='net/mcreator/minepiece/procedures/'
names=[]
for f,ms in catalogue.items():
 for m in ms:
  for suffix in ['', '_release']:
   name='b06_'+f+'_'+m+suffix;names.append(name)
   # Different physical families use different silhouettes, not renamed repetitions.
   stride={'wolf':-8,'leopard':4,'saber':12,'giraffe':-3,'batto':-12}[f]
   right=[-70,0,-12];left=[-30,0,18];body=[-8,stride,0];leg=[0,0,0]
   if m in ['rankyaku','lupusfall','gaicho','rankyakuran','amanedachi']:right=[-30,0,-35];left=[-25,0,35];body=[12,-30,0];leg=[-100,0,-20 if m=='amanedachi' else 10]
   elif m in ['soru','attackspeed','poursuite']:right=[35,0,20];left=[-65,0,0];body=[25,stride,0];leg=[-55,0,5]
   elif m=='attackjump':right=[-155,0,-20];left=[-135,0,20];body=[-20,0,0];leg=[-50,0,0]
   elif m in ['jushigan','rokuogan','localareanetwork']:right=[-90,-12,-5];left=[-90,12,5];body=[15,stride,0]
   elif m in ['kamiezanshin','yurushitene']:right=[-50,0,-35];left=[-60,0,35];body=[-15,stride,12]
   elif m in ['gagan','kibasen']:right=[-115,0,-55];left=[-110,0,55];body=[15,stride,0]
   if not suffix:right=[-v*.55 for v in right];left=[v*.65 for v in left];body=[-body[0],stride,0];leg=[v*.3 for v in leg]
   length=(1.65 if m=='shiganmadara' else .9 if m in ['amanedachi','rankyakuran','localareanetwork'] else .65) if suffix else (1.12 if m in ['rokuogan','amanedachi','kamiezanshin'] else .32 if m=='soru' else .62)
   bones={};factor=1+list(catalogue).index(f)*.025
   for bone,vector in [('right_arm',right),('left_arm',left),('body',body),('right_leg',leg)]:
    vector=[round(v*factor,4) for v in vector];keys={'0':{'vector':[0,0,0]}}
    if m=='shiganmadara' and suffix:
     for t in [.05,.2,.65,.8,1.25,1.4]:keys[str(t)]={'vector':vector if t in [.05,.65,1.25] else [v*.15 for v in vector]}
    else:
     keys['0.10']={'vector':vector};keys[str(round(length-.15,3))]={'vector':vector}
    keys[str(length)]={'vector':[0,0,0]};bones[bone]={'rotation':keys}
   (A/'player_animations'/f'{name}.json').write_text(json.dumps({'format_version':'1.8.0','animations':{name:{'animation_length':length,'bones':bones}}}),encoding='utf-8')
replace(proc+'TechniqueAnimations.java','private static final String[] CONNUES = {','private static final String[] CONNUES = {\n      '+','.join('"'+n+'"' for n in names)+',')
profiles='''         case "b06_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xE7B5DFFF,.15F);
         case "b06_thrust","b06_finger" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xFFE9F4FF,id.equals("b06_finger")?.12F:.35F);
         case "b06_slash" -> new TechniqueVisualProfile(Kind.SAND_SLASH,0xF2C9F7FF,.9F);
         case "b06_wolf_slash" -> new TechniqueVisualProfile(Kind.B06_WOLF,0xF0DCEAFF,1.1F);
         case "b06_bird_slash" -> new TechniqueVisualProfile(Kind.B06_BIRD,0xF5CCF4FF,1.5F);
         case "b06_claw","b06_fangs" -> new TechniqueVisualProfile(Kind.B03_FAN,0xFFE9B88B,1.1F);
         case "b06_net" -> new TechniqueVisualProfile(Kind.B06_NET,0xE9B8DEFF,.9F);
         case "b06_rush","b06_evade" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xBEC4D8EC,.8F);
         case "b06_guard" -> new TechniqueVisualProfile(Kind.B02_ORBIT,0xB5BA85E6,.5F);
         case "b06_spin" -> new TechniqueVisualProfile(Kind.B04_KICK,0xF2D3FBFF,3);
         case "b06_pressure","b06_slam","b06_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xD9D9F3FF,id.equals("b06_slam")?3:id.equals("b06_pressure")?1.5F:.6F);
'''
replace('net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.java','public enum Kind {','public enum Kind { B06_WOLF,B06_BIRD,B06_NET,')
replace('net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.java','return switch (id) {','return switch (id) {\n'+profiles)
terrain='''  // B06: permanent bounded contacts; source trace and playability adaptations in BATCH-06.md.
  for(String f:List.of("inu_inu_wolf","neko_neko_leopard","neko_neko_sabertiger","ushi_ushi_giraffe","batto_batto")){
   destructive(f,"shigan",1.2,24,false,"Shigan RangeAttack + BlockDestroy; contact only");
   if(!f.equals("neko_neko_sabertiger")){none(f,"soru","StepBlock movement never tunnels");destructive(f,"rankyaku",1.5,40,false,"Finite physical slash impact; projectile helper adaptation");}
  }
  destructive("inu_inu_wolf","lupusfall",2,48,false,"Wolf projectile impact, bounded permanent adaptation");destructive("inu_inu_wolf","okamihajiki",1.5,32,false,"AttackStrongPunch contact");destructive("inu_inu_wolf","jushigan",1.5,32,false,"Strong/weak punch helpers");destructive("inu_inu_wolf","localareanetwork",1.5,40,false,"Wolf projectiles share one permanent quota");none("inu_inu_wolf","poursuite","Mod-designed chase never tunnels");
  destructive("neko_neko_leopard","gaicho",2,64,false,"AIProjectileSlash actual collision");destructive("neko_neko_leopard","attackjump",3,64,false,"AttackJump delegates AttackOverhead at landing");none("neko_neko_leopard","shugan","Source range/dash has no block destruction");destructive("neko_neko_leopard","rokuogan",3,80,false,"Rokuogan moving pressure wave");
  destructive("neko_neko_sabertiger","attackspeed",1.5,32,false,"Source AttackStrongPunch/Sword finishing contact");destructive("neko_neko_sabertiger","shiganmadara",1,32,false,"Timed Shigan contacts share one quota");destructive("neko_neko_sabertiger","gagan",2,48,false,"Energy/strong punch physical impact");destructive("neko_neko_sabertiger","kibasen",1.5,32,false,"Strong punch finishing contact");
  destructive("ushi_ushi_giraffe","rankyakuran",1.5,56,false,"Three emitted slashes share a quota");destructive("ushi_ushi_giraffe","amanedachi",3,80,false,"Source RangeAttack + explicit BlockDestroy");none("batto_batto","tobushigan,kamiezanshin,yurushitene","Finger projectile, evasion/counter stance and range dash without source block destruction");
'''
replace(proc+'TechniqueTerrain.java','static {','static {\n'+terrain)
reg=J/proc/'DevilFruitRegistry.java';s=reg.read_text(encoding='utf-8')
s=s.replace('DevilFruitMove.buff("attackjump", "Attack Jump", MobEffects.SPEED, 60, 1, 40.0, 80, MinepieceModParticles.CLAWS)','DevilFruitMove.melee("attackjump", "Attack Jump", 15.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.CLAWS)',1)
s=s.replace('DevilFruitMove.buff("attackspeed", "Attack Speed", MobEffects.SPEED, 60, 1, 40.0, 80, MinepieceModParticles.CLAWS)','DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.CLAWS)',1)
s=s.replace('DevilFruitMove.melee("tobushigan", "Tobu Shigan", 2.2, 4.0, 0.5, 40.0, 80, MinepieceModParticles.BAT)','DevilFruitMove.projectile("tobushigan", "Tobu Shigan", 2.2, 45, 0.5, 40.0, 80, MinepieceModParticles.BAT)',1)
s=s.replace('DevilFruitMove.melee("kamiezanshin", "Kamie Zanshin", 999999.0, 4.0, 1.0, MobEffects.SLOWNESS, 20, 1, 100.0, 300, MinepieceModParticles.BAT)','DevilFruitMove.areaEffect("kamiezanshin", "Kamie Zanshin", null, 60, 0, 4.0, 100.0, 300, MinepieceModParticles.BAT)',1)
reg.write_text(s,encoding='utf-8')
replace('net/mcreator/minepiece/client/hud/DevilFruitHud.java','// Forme','// Forme')
hud=J/'net/mcreator/minepiece/client/hud/DevilFruitHud.java';s=hud.read_text(encoding='utf-8');line='      if(DevilFruitClientState.form==2&&!DevilFruitClientState.fruitId.equals(net.mcreator.minepiece.procedures.GomuGears.FRUIT))formValue=Component.literal("Animale");\n';anchor='      graphics.text(client.font, form, cx + 4, cy, DIM);';s=s if line in s else s.replace(anchor,line+anchor);hud.write_text(s,encoding='utf-8')
with zipfile.ZipFile(P.parent/'.runtime/onepiece/reference-jars/mine-mine-no-mi-1.20.1-0.11.6.jar') as z:
 for name in ['leopard_walk','giraffe_walk']:(A/'textures/entities'/('b06_'+name+'.png')).write_bytes(z.read('assets/mineminenomi/textures/models/morphs/'+name+'.png'))
print('B06_AUTHORED',sum(map(len,catalogue.values())),'moves',len(names),'finite poses')
