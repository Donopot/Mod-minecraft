"""One-shot initial B07 wiring/assets; do not rerun after manual hardening."""
from pathlib import Path
import json,zipfile,re,hashlib,math
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';A=P/'src/main/resources/assets/minepiece'
proc='net/mcreator/minepiece/procedures/'
catalogue={'makami':['attackoverhead','attacksweep','raimeihakke','narikabura','namujihyoga','himorogiri','hyogaheki'],'daibutsu':['attackoverhead','brilliantpunk','impactball','overheadsuper','impactwave'],'phoenix':['attackspeed','ongle','bluebird','marcotornado','hooin','fujiazami'],'seiryu':['attackoverhead','attacksweep','raimeihakke','kongokabura','kaifu','borobreath','kosanze','ikoku'],'ptero':['attackspeed','tankyudon','barizodon']}
(P/'build/b07-moves.local.json').write_text(json.dumps(catalogue,indent=2),encoding='utf-8')
def patch(rel,old,new):
 p=J/rel;s=p.read_text(encoding='utf-8');assert old in s,(rel,old[:60]);p.write_text(s.replace(old,new,1),encoding='utf-8')
patch('br/com/oneminepiece/opworldlike/OmpWorld.java','net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.register();','net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.register();\n      net.mcreator.minepiece.procedures.MythicalZoanTechniques.register();')
patch(proc+'DevilFruitBehaviours.java','if(ZoanTechniqueEpisodes.handles(fruit))','if(MythicalZoanTechniques.handles(fruit))return MythicalZoanTechniques.cast(player,fruit,move);\n      if(ZoanTechniqueEpisodes.handles(fruit))')
patch(proc+'CustomTechniqueRoutes.java','if(ZoanTechniqueEpisodes.handles(fruit))','if(MythicalZoanTechniques.handles(fruit))return true;\n      if(ZoanTechniqueEpisodes.handles(fruit))')
patch(proc+'DevilFruitPowerLogic.java','ZoanTechniqueEpisodes.cancel(player);','ZoanTechniqueEpisodes.cancel(player);MythicalZoanTechniques.cancel(player);')
patch(proc+'TechniqueAnimations.java','String zoan=ZoanTechniqueEpisodes','String mythical=MythicalZoanTechniques.animationFor(player,move.id());if(!mythical.isEmpty())return mythical;\n      String zoan=ZoanTechniqueEpisodes')
patch(proc+'DevilFruitRegistry.java','DevilFruitMove.melee("attackspeed", "Attack Speed", 0.0, 4.0, 1.0, MobEffects.INVISIBILITY, 20, 0, 40.0, 80, MinepieceModParticles.FLAME_BLUE)','DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.FLAME_BLUE)')
patch(proc+'DevilFruitRegistry.java','DevilFruitMove.buff("attackspeed", "Attack Speed", MobEffects.SPEED, 60, 1, 40.0, 80, MinepieceModParticles.SLASH)','DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.SLASH)')
terrain='''  // B07 source helper traces and bounded permanent adaptations.
  for(String f:List.of("inu_inu_okuchi_no_makami","hito_hito_daibutsu","uo_uo_seiryu"))destructive(f,"attackoverhead",2,48,false,"AttackOverhead actual contact");
  for(String f:List.of("inu_inu_okuchi_no_makami","uo_uo_seiryu")){destructive(f,"attacksweep",1.5,32,false,"AttackSweep contact");destructive(f,"raimeihakke",2,48,false,"RaimeiHakke strong sword finishing contact");}
  destructive("inu_inu_okuchi_no_makami","narikabura",2,48,false,"AIEnergy actual collision");transform("inu_inu_okuchi_no_makami","namujihyoga","NamujiHyoga -> BlockToIce permanent path");destructive("inu_inu_okuchi_no_makami","himorogiri",3,80,false,"Himorogiri explicit BlockDestroy on landing");none("inu_inu_okuchi_no_makami","hyogaheki","Port-added bounded personal armor, no terrain");
  destructive("hito_hito_daibutsu","brilliantpunk",2,48,false,"Strong punch finishing contact");destructive("hito_hito_daibutsu","impactball",2.5,64,false,"AI energy collision");destructive("hito_hito_daibutsu","overheadsuper",3,96,false,"AttackOverheadSuper contact");destructive("hito_hito_daibutsu","impactwave",3,96,false,"ImpactWave -> AIImpactWave -> BlockDestroy");
  destructive("tori_tori_phoenix","attackspeed,ongle",1.5,32,false,"AttackSpeed/AttackStrongPunch ending contact");destructive("tori_tori_phoenix","bluebird",2.5,64,false,"BlueBird DaiFunka -> AIFist actual impact");destructive("tori_tori_phoenix","marcotornado",2,80,false,"MarcoTornado explicit BlockDestroy; shared quota");destructive("tori_tori_phoenix","hooin",3,80,false,"HooIn explicit landing BlockDestroy");destructive("tori_tori_phoenix","fujiazami",2,48,false,"Fujiazami actual sweep BlockDestroy");
  destructive("uo_uo_seiryu","kongokabura",2.5,64,false,"AIEnergy collision");destructive("uo_uo_seiryu","kaifu",2,96,false,"Three SkillProjectileSlash siblings share quota");destructive("uo_uo_seiryu","borobreath",3,128,false,"BoroBreath explicit BlockDestroy along active ray");destructive("uo_uo_seiryu","kosanze",4,128,false,"Kosanze explicit landing BlockDestroy");destructive("uo_uo_seiryu","ikoku",3,128,false,"Ikoku traveling pressure wave");
  destructive("ryu_ryu_pteranodon","attackspeed",1.5,32,false,"AttackSpeed strong punch finishing contact");none("ryu_ryu_pteranodon","tankyudon","StepBlock + RangeAttack; beak lunge never tunnels");destructive("ryu_ryu_pteranodon","barizodon",2,80,false,"SkillProjectileSlash -> AIProjectileSlash actual collision");
'''
patch(proc+'TechniqueTerrain.java','static {','static {\n'+terrain)
# Distinct preparation/release poses, asymmetry and recovery. No infinite loops.
names=[]
for f,ms in catalogue.items():
 for ix,m in enumerate(ms):
  for release in (False,True):
   n='b07_'+f+'_'+m+('_release' if release else '');names.append(n)
   phase=ix+list(catalogue).index(f)*.3
   if m in ('attackoverhead','overheadsuper','himorogiri','kosanze'):r=[-160,8,-18] if not release else [18,-12,-5];l=[-100,10,35] if not release else [-25,15,10];b=[-20,-12,0] if not release else [28,12,3];leg=[-25,0,-8]
   elif m in ('attacksweep','fujiazami','barizodon','kaifu','ikoku'):r=[-60,65,-65] if not release else [-80,-75,-20];l=[-35,-25,35];b=[6,45 if not release else -45,0];leg=[-15,0,18]
   elif m in ('borobreath','namujihyoga'):r=[20,0,-28];l=[20,0,28];b=[-18 if not release else 22,0,0];leg=[10,0,-5]
   elif m in ('attackspeed','brilliantpunk','raimeihakke','ongle','tankyudon'):r=[35,0,15] if not release else [-100,10,-10];l=[-85,0,25] if not release else [35,0,15];b=[20,12,0];leg=[-65,0,-10]
   elif m=='hyogaheki':r=[-60,-30,-5];l=[-60,30,5];b=[-5,0,0];leg=[-8,0,8]
   elif m=='marcotornado':r=[-70,0,-75];l=[-65,0,75];b=[-5,90 if release else -25,5];leg=[-75,0,-15]
   elif m=='hooin':r=[35,0,-45];l=[35,0,45];b=[15,0,8];leg=[-145 if release else -65,0,-5]
   else:r=[-75,-12,-10] if not release else [-100,10,0];l=[-50,20,35];b=[-5 if not release else 18,-10,0];leg=[-15,0,-5]
   length=1.3 if release and m in ('borobreath','namujihyoga','marcotornado') else .85 if release else 1.05 if m in ('borobreath','namujihyoga','overheadsuper','himorogiri','kosanze','hyogaheki','impactwave','ikoku') else .55
   bones={}
   for bone,v in [('right_arm',r),('left_arm',l),('body',b),('right_leg',leg),('head',[-b[0]*.25,phase*2,0])]:
    # Technique-specific anticipation, follow-through and neutral recovery.
    v=[round(x*(1+phase*.013),4) for x in v];keys={'0':{'vector':[0,0,0]},str(round(length*.2,3)):{'vector':[round(x*.85,4) for x in v]},str(round(length*.5,3)):{'vector':v},str(round(length*.78,3)):{'vector':[round(x*.35,4) for x in v]},str(length):{'vector':[0,0,0]}};bones[bone]={'rotation':keys}
   (A/'player_animations'/f'{n}.json').write_text(json.dumps({'format_version':'1.8.0','animations':{n:{'animation_length':length,'bones':bones}}}),encoding='utf-8')
patch(proc+'TechniqueAnimations.java','private static final String[] CONNUES = {','private static final String[] CONNUES = {\n      '+','.join('"'+n+'"' for n in names)+',')
profiles=[]
for f,ms in catalogue.items():
 color={'makami':'0xFFB9F2FF','daibutsu':'0xFFFDE08A','phoenix':'0xFF38DBFA','seiryu':'0xFFCCBBFF','ptero':'0xFFDEDAEF'}[f]
 profiles.extend([f'         case "b07_{f}_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,{color},.2F);',f'         case "b07_{f}_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,{color},.9F);'])
 for m in ms:
  kind=('B06_BIRD' if m=='bluebird' else 'FIRE_BREATH' if m=='borobreath' else 'B07_FROST' if m=='namujihyoga' else 'B07_TORNADO' if m=='marcotornado' else 'ICE_BALL' if m=='hyogaheki' else 'ELECTRIC_ARC' if m=='raimeihakke' else 'B02_SWEEP' if m in ('attacksweep','fujiazami','kaifu','barizodon') else 'PRESSURE' if m in ('impactwave','ikoku','impactball','narikabura','kongokabura') else 'B04_KICK' if m in ('ongle','hooin') else 'B01_BEAM' if m=='tankyudon' else 'B03_PUNCH')
  c='0xFFFF7129' if m=='borobreath' else color
  profiles.append(f'         case "b07_{f}_{m}" -> new TechniqueVisualProfile(Kind.{kind},{c},1);')
patch('net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.java','public enum Kind {','public enum Kind { B07_FROST,B07_TORNADO,')
patch('net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.java','return switch (id) {','return switch (id) {\n'+'\n'.join(profiles)+'\n')
r=J/'net/mcreator/minepiece/client/renderer/EntityGomuArmRenderer.java';s=r.read_text(encoding='utf-8');s=s.replace('id.startsWith("b06_")','id.startsWith("b07_") || id.startsWith("b06_")').replace('entity.getVisualProfile().startsWith("b06_")','entity.getVisualProfile().startsWith("b07_") || entity.getVisualProfile().startsWith("b06_")').replace('profile.startsWith("b06_")','profile.startsWith("b07_") || profile.startsWith("b06_")');r.write_text(s,encoding='utf-8')
# Five complete rigs, private source texture reuse. No external gameplay code.
with zipfile.ZipFile(P.parent/'.runtime/onepiece/reference-jars/mine-mine-no-mi-1.20.1-0.11.6.jar') as z:
 for n in ['daibutsu','phoenix_fly','pteranodon_fly']:(A/'textures/entities'/f'b07_{n}.png').write_bytes(z.read(f'assets/mineminenomi/textures/models/morphs/{n}.png'))
 for i in range(8):(A/'textures/entities'/f'b07_phoenix_flame_{i}.png').write_bytes(z.read(f'assets/mineminenomi/textures/models/morphs/phoenix_flames/fly_{i}.png'))
defs={'MAKAMI':'MythicalBeastModel::makami','MAKAMI_MANE':'MythicalBeastModel::makamiMane','DAIBUTSU':'ReferenceBeastGeometry::daibutsu','PHOENIX':'ReferenceBeastGeometry::phoenixFly','PTERO':'ReferenceBeastGeometry::pteranodonFly','DRAGON':'MythicalBeastModel::dragon','DRAGON_TRIM':'MythicalBeastModel::dragonTrim'}
ns='net.mcreator.minepiece.client.model.'
patch('net/mcreator/minepiece/client/DevilFruitClientInit.java','ClientVisualLab.register();','ClientVisualLab.register();\n'+'\n'.join(f'      ModelLayerRegistry.registerModelLayer({ns}MythicalBeastModel.{k},{ns}{v});' for k,v in defs.items()))
layer='net/mcreator/minepiece/client/layer/PlayerTransformLayer.java'
patch(layer,'private final Map<String,net.mcreator.minepiece.client.model.LandBeastModel> beasts','private final Map<String,net.mcreator.minepiece.client.model.MythicalBeastModel> mythicalBeasts=new HashMap<>();\n   private final net.mcreator.minepiece.client.model.MythicalBeastModel makamiMane,dragonTrim,phoenixFlames;\n   private final Map<String,net.mcreator.minepiece.client.model.LandBeastModel> beasts')
init=[]
for key,lay,k in [('inu_inu_okuchi_no_makami','MAKAMI',0),('hito_hito_daibutsu','DAIBUTSU',1),('tori_tori_phoenix','PHOENIX',2),('uo_uo_seiryu','DRAGON',4),('ryu_ryu_pteranodon','PTERO',3)]:init.append(f'      mythicalBeasts.put("{key}",new {ns}MythicalBeastModel(es.bakeLayer({ns}MythicalBeastModel.{lay}),{k}));')
for field,lay,k in [('makamiMane','MAKAMI_MANE',6),('dragonTrim','DRAGON_TRIM',7),('phoenixFlames','PHOENIX',5)]:init.append(f'      {field}=new {ns}MythicalBeastModel(es.bakeLayer({ns}MythicalBeastModel.{lay}),{k});')
patch(layer,'EntityModelSet es = Minecraft.getInstance().getEntityModels();','EntityModelSet es = Minecraft.getInstance().getEntityModels();\n'+'\n'.join(init))
render='''      if(playerForm==2 && mythicalBeasts.containsKey(beastKey)){
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         Identifier texture=switch(beastKey){case "hito_hito_daibutsu"->tex("b07_daibutsu");case "tori_tori_phoenix"->tex("b07_phoenix_fly");case "ryu_ryu_pteranodon"->tex("b07_pteranodon_fly");default->tex("tex_white3");};
         int color=beastKey.equals("uo_uo_seiryu")?0xFF438DC6:-1;
         renderColoredCutoutModel(mythicalBeasts.get(beastKey),texture,poseStack,collector,lightCoords,state,color,0);
         if(beastKey.equals("inu_inu_okuchi_no_makami"))renderColoredCutoutModel(makamiMane,tex("tex_white3"),poseStack,collector,15728880,state,0xFF8AE6F7,1);
         if(beastKey.equals("uo_uo_seiryu"))renderColoredCutoutModel(dragonTrim,tex("tex_white3"),poseStack,collector,lightCoords,state,0xFFFFDE96,1);
         if(beastKey.equals("tori_tori_phoenix"))renderColoredCutoutModel(phoenixFlames,tex("b07_phoenix_flame_"+((int)(state.ageInTicks/3)%8)),poseStack,collector,15728880,state,-1,1);
         return;
      }
'''
patch(layer,'if(playerForm==2 && beasts.containsKey(beastKey)){',render+'      if(playerForm==2 && beasts.containsKey(beastKey)){')
patch('br/com/oneminepiece/opworldlike/mixin/TransformBodyMixin.java','data.minepiece$fruit().equals("minepiece:item_devil_fruit_batto_batto")','(data.minepiece$fruit().equals("minepiece:item_devil_fruit_batto_batto") || data.minepiece$fruit().equals("minepiece:item_devil_fruit_tori_tori_phoenix"))')
patch(proc+'ZoanForms.java','new Body("uo_uo_seiryu",4,4.5,3.8,2)','new Body("uo_uo_seiryu",6,4.5,3.8,2)')
print('B07 authored29routes58finiteposes5completeforms; not yet verified')
