"""Initial B08 authoring after the alpha16 source/JAR snapshot. Never rerun after manual fixes."""
from pathlib import Path
import json,zipfile
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';A=P/'src/main/resources/assets/minepiece';proc=J/'net/mcreator/minepiece/procedures'
assert (P.parent/'.runtime/onepiece/after-b07-alpha16/source.zip').is_file()
p=P/'gradle.properties';s=p.read_text().replace('1.0.1-alpha.16','1.0.1-alpha.17');p.write_text(s,encoding='utf-8')
cat={'brachio':['attackoverhead','brachiobober','blackcofee','brachiolauncher','sparkingqueen'],'pachy':['attackjump','attacksweep','headbutt','ulzugan','ulmeteor']};(P/'build/b08-moves.local.json').write_text(json.dumps(cat,indent=2),encoding='utf-8')
p=proc/'MythicalZoanTechniques.java';s=p.read_text(encoding='utf-8')
s=s.replace(' public enum Kind {',' public static final Item[] ANCIENT_FRUITS={MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS,MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS};\n public static final String[] ANCIENT_NAMES={"brachio","pachy"};\n public enum Kind { MISSILE,RED_LASER,BEAM,HEADLONG,',1)
s=s.replace('return Arrays.asList(FRUITS).contains(fruit.getItem());','return Arrays.asList(FRUITS).contains(fruit.getItem())||Arrays.asList(ANCIENT_FRUITS).contains(fruit.getItem());',1)
s=s.replace('return NAMES[Arrays.asList(FRUITS).indexOf(fruit)];','return Arrays.asList(ANCIENT_FRUITS).contains(fruit)?ANCIENT_NAMES[Arrays.asList(ANCIENT_FRUITS).indexOf(fruit)]:NAMES[Arrays.asList(FRUITS).indexOf(fruit)];',1)
s=s.replace('public static String animationFor','private static String batch(Item f){return Arrays.asList(ANCIENT_FRUITS).contains(f)?"b08_":"b07_";}\n public static String animationFor',1)
s=s.replace('"b07_"+shortName(f.getItem())','batch(f.getItem())+shortName(f.getItem())').replace('"b07_"+shortName(a.fruit)','batch(a.fruit)+shortName(a.fruit)')
s=s.replace('return switch(id){','return switch(id){case "brachiolauncher"->Kind.MISSILE;case "sparkingqueen"->Kind.RED_LASER;case "blackcofee"->Kind.BEAM;case "headbutt"->Kind.BEAK;case "ulzugan"->Kind.HEADLONG;case "brachiobober","attackjump","ulmeteor"->Kind.SLAM;',1)
s=s.replace('Set.of("borobreath","namujihyoga","impactwave"','Set.of("brachiobober","attackjump","ulmeteor","ulzugan","blackcofee","borobreath","namujihyoga","impactwave"',1)
s=s.replace('a.kind==Kind.BEAK)&&t<12','a.kind==Kind.BEAK||a.kind==Kind.HEADLONG)&&t<(a.kind==Kind.HEADLONG?14:12)',1)
s=s.replace('flat(a.look),.55);','flat(a.look),a.kind==Kind.HEADLONG?.9:.55);',1).replace('if(moved<.45||t==11)','if(moved<(a.kind==Kind.HEADLONG?.8:.45)||t==(a.kind==Kind.HEADLONG?13:11))',1)
s=s.replace('scale((t+1)*.25)','scale((t+1)*(a.move.id().equals("ulmeteor")?12.0/28:.25))',1)
s=s.replace('case ORB->shoot','case BEAM->beam(a);\n  case MISSILE->shoot(a,profile(a),2.5,1.05,0);\n  case RED_LASER->shoot(a,profile(a),.55,2,0);\n  case ORB->shoot',1)
s=s.replace('private static ElementType element(Action a){return','private static ElementType element(Action a){if(a.kind==Kind.BEAM||a.kind==Kind.RED_LASER)return ElementType.LIGHT;return',1)
beam=''' private static void beam(Action a){Vec3 from=a.owner.getEyePosition();var block=ray(a,from,a.look,24);double length=from.distanceTo(block.getLocation());for(var target:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(from,block.getLocation()).inflate(1))){Vec3 relative=target.getBoundingBox().getCenter().subtract(from);double along=relative.dot(a.look);if(along>=0&&along<=length&&relative.subtract(a.look.scale(along)).length()<=1&&BatchRules.target(a.owner,target))hit(a,target.getBoundingBox().getCenter(),.1,false,a.move.damage());}for(double d=.5;d<=length;d+=1.25)fx(a,profile(a),from.add(a.look.scale(d)),12,.09);if(block.getType()==HitResult.Type.BLOCK)a.terrain.impact(block.getLocation());}
'''
s=s.replace(' private static void shoot(Action a,',beam+' private static void shoot(Action a,',1)
s=s.replace('.trail(a.fruit==FRUITS[2]?MinepieceModParticles.FLAME_BLUE:ParticleTypes.END_ROD)','.trail(a.kind==Kind.MISSILE?ParticleTypes.SMOKE:a.fruit==FRUITS[2]?MinepieceModParticles.FLAME_BLUE:ParticleTypes.END_ROD)',1)
s=s.replace('(float)a.move.damage())){a.hits.add(t.getUUID());confirmed(a,t);','(float)(a.kind==Kind.MISSILE?13.5+(t==direct?a.move.damage():0):a.move.damage()))){a.hits.add(t.getUUID());confirmed(a,t);',1)
p.write_text(s,encoding='utf-8')
p=proc/'DevilFruitRegistry.java';s=p.read_text(encoding='utf-8').replace('DevilFruitMove.buff("attackjump", "Attack Jump", MobEffects.SPEED, 60, 1, 40.0, 80, MinepieceModParticles.SHOCK_HIT)','DevilFruitMove.melee("attackjump", "Attack Jump", 15.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.SHOCK_HIT)',1);p.write_text(s,encoding='utf-8')
p=proc/'TechniqueTerrain.java';s=p.read_text(encoding='utf-8').replace(' static {',''' static {
  destructive("ryu_ryu_brachiosaurus","attackoverhead",2,48,false,"AttackOverhead actual contact");destructive("ryu_ryu_brachiosaurus","brachiobober",3.5,96,false,"BrachioBober explicit BlockDestroy at landing");destructive("ryu_ryu_brachiosaurus","blackcofee",2.5,80,false,"BlackCofee beam explicit BlockDestroy");destructive("ryu_ryu_brachiosaurus","brachiolauncher",2.5,64,false,"MissileProjectileHitsBlock -> BlockDestroy;13.5 blast plus direct2.2");destructive("ryu_ryu_brachiosaurus","sparkingqueen",2,48,false,"ProjectileLaserRed uses ProjectileLaserProjectileHitsBlock");
  destructive("ryu_ryu_pachycephalosaurus","attackjump",3,64,false,"AttackJump -> AttackOverhead at actual landing");destructive("ryu_ryu_pachycephalosaurus","attacksweep",1.5,32,false,"AttackSweep");destructive("ryu_ryu_pachycephalosaurus","headbutt",2,48,false,"Headbutt -> StrongPunch contact");none("ryu_ryu_pachycephalosaurus","ulzugan","UlZugan RangeAttack moving contact; no BlockDestroy");destructive("ryu_ryu_pachycephalosaurus","ulmeteor",4,128,false,"UlMeteor actual landing explicit BlockDestroy");
''',1);p.write_text(s,encoding='utf-8')
p=proc/'ZoanForms.java';s=p.read_text(encoding='utf-8');s=s.replace('new Body("ryu_ryu_pteranodon",2.4,1.8,1.35,1.5)));','new Body("ryu_ryu_pteranodon",2.4,1.8,1.35,1.5)),Map.entry("ryu_ryu_brachiosaurus",new Body("ryu_ryu_brachiosaurus",2.6,5.8,5.1,2)),Map.entry("ryu_ryu_pachycephalosaurus",new Body("ryu_ryu_pachycephalosaurus",2,2.9,2.3,1.8)));',1)
s=s.replace('case "ushi_ushi_giraffe","uo_uo_seiryu"','case "ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus","ushi_ushi_giraffe","uo_uo_seiryu"',1).replace('"hito_hito_daibutsu","uo_uo_seiryu","inu_inu_okuchi_no_makami"','"ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus","hito_hito_daibutsu","uo_uo_seiryu","inu_inu_okuchi_no_makami"',1);p.write_text(s,encoding='utf-8')
# B08 poses are individually authored, with neutral recovery and separate anticipation/follow-through.
names=[]
for f,ms in cat.items():
 for ix,m in enumerate(ms):
  for release in (False,True):
   n='b08_'+f+'_'+m+('_release' if release else '');names.append(n);phase=ix+(0 if f=='brachio' else .37)
   if m in ('attackjump','brachiobober','ulmeteor'):r=[-155,0,-25] if not release else [30,0,-15];l=[-145,0,30] if not release else [35,0,20];body=[-25,10,0] if not release else [50,0,0];leg=[-80,0,-10]
   elif m in ('headbutt','ulzugan'):r=[55,0,-30];l=[55,0,30];body=[-15,0,0] if not release else [45,0,0];leg=[-60,0,-5]
   elif m in ('blackcofee','sparkingqueen'):r=[-85,10,-12];l=[-25,-15,20];body=[-12,15,0] if not release else [15,-8,0];leg=[-15,0,-8]
   elif m=='brachiolauncher':r=[-95,0,-70];l=[-90,0,70];body=[-18,0,8];leg=[-25,0,10]
   elif m=='attacksweep':r=[-50,70,-65] if not release else [-65,-65,-10];l=[-45,-30,30];body=[10,50 if not release else -45,0];leg=[-20,0,-15]
   else:r=[-160,0,-15] if not release else [30,0,0];l=[-65,5,30];body=[-25,10,0] if not release else [35,0,0];leg=[-25,0,-10]
   length=.9 if release else 1.05 if m in ('blackcofee','brachiobober','attackjump','ulmeteor','ulzugan') else .55;bones={}
   for bone,v in [('right_arm',r),('left_arm',l),('body',body),('right_leg',leg),('head',[(-25 if m in ('headbutt','ulzugan') else -body[0]*.4),phase*2,0])]:
    v=[round(x*(1+phase*.017),4) for x in v];bones[bone]={'rotation':{'0':{'vector':[0,0,0]},str(round(length*.2,3)):{'vector':v},str(round(length*.6,3)):{'vector':[round(x*.8,4) for x in v]},str(round(length*.8,3)):{'vector':[round(x*.25,4) for x in v]},str(length):{'vector':[0,0,0]}}}
   (A/'player_animations'/f'{n}.json').write_text(json.dumps({'format_version':'1.8.0','animations':{n:{'animation_length':length,'bones':bones}}}),encoding='utf-8')
p=proc/'TechniqueAnimations.java';s=p.read_text(encoding='utf-8').replace('private static final String[] CONNUES = {','private static final String[] CONNUES = {\n      '+','.join('"'+n+'"' for n in names)+',',1);p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.java';s=p.read_text(encoding='utf-8').replace('public enum Kind {','public enum Kind { B08_MISSILE,B08_HEAD,B08_COIL,',1);profiles=[]
for f,ms in cat.items():
 color='0xFFFFC18A' if f=='brachio' else '0xFFA4EBF5'
 profiles.extend([f'         case "b08_{f}_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,{color},.2F);',f'         case "b08_{f}_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,{color},1);'])
 for m in ms:
  k='LIGHT' if m in ('blackcofee','sparkingqueen') else 'B08_MISSILE' if m=='brachiolauncher' else 'B08_COIL' if m=='brachiobober' else 'B08_HEAD' if m in ('headbutt','ulzugan','ulmeteor') else 'B02_SWEEP' if m=='attacksweep' else 'B03_PUNCH';c='0xFFFF403A' if m=='sparkingqueen' else '0xFFFFF1B3' if m=='blackcofee' else color
  profiles.append(f'         case "b08_{f}_{m}" -> new TechniqueVisualProfile(Kind.{k},{c},'+('.09F' if k=='LIGHT' else '.45F' if k=='B08_MISSILE' else '1')+');')
s=s.replace('return switch (id) {','return switch (id) {\n'+'\n'.join(profiles)+'\n',1);p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/client/renderer/EntityGomuArmRenderer.java';s=p.read_text(encoding='utf-8').replace('id.startsWith("b07_")','id.startsWith("b08_") || id.startsWith("b07_")').replace('entity.getVisualProfile().startsWith("b07_")','entity.getVisualProfile().startsWith("b08_") || entity.getVisualProfile().startsWith("b07_")').replace('profile.startsWith("b07_")','profile.startsWith("b08_") || profile.startsWith("b07_")')
# Parentheses keep the radius override inside the non-charge branch.
s=s.replace('if(id.startsWith("b08_") || id.startsWith("b07_")&&!id.endsWith("charge"))','if((id.startsWith("b08_") || id.startsWith("b07_"))&&!id.endsWith("charge"))');p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/client/renderer/TechniqueEffectGeometry.java';s=p.read_text(encoding='utf-8').replace('switch (profile.kind()) {','''switch (profile.kind()) {
            case B08_MISSILE -> {tapered(out,pose,direction.scale(-radius),direction.scale(radius),radius*.25,radius*.12,0xFFD1D5DD);tapered(out,pose,direction.scale(radius),direction.scale(radius*1.55),radius*.12,.001,0xFFEB4A43);for(int side:new int[]{-1,1}){tapered(out,pose,direction.scale(-radius*.6),direction.scale(-radius).add(u.scale(side*radius*.65)),.05,.01,0xFF665A68);tapered(out,pose,direction.scale(-radius*.6),direction.scale(-radius).add(v.scale(side*radius*.65)),.05,.01,0xFF665A68);}tapered(out,pose,direction.scale(-radius),direction.scale(-radius*(1.7+.2*Math.sin(age))),.09,.001,0xFFFFAD3B);}
            case B08_HEAD -> {sphere(out,pose,Vec3.ZERO,radius*.45,profile.color());sphere(out,pose,direction.scale(radius*.3),radius*.3,0xFFF0DDEB);for(int side:new int[]{-1,1})tapered(out,pose,u.scale(side*radius*.35),u.scale(side*radius*.7).add(v.scale(radius*.55)),.08,.002,0xFFF7EEE3);}
            case B08_COIL -> {Vec3 last=null;for(int i=0;i<=40;i++){double t=i/40.,a=t*Math.PI*8+age*.05;Vec3 at=u.scale(Math.cos(a)*radius*.4).add(v.scale(Math.sin(a)*radius*.4)).add(direction.scale(t*radius*2));if(last!=null)tapered(out,pose,last,at,.055,.04,profile.color());last=at;}}
''',1);p.write_text(s,encoding='utf-8')
with zipfile.ZipFile(P.parent/'.runtime/onepiece/reference-jars/mine-mine-no-mi-1.20.1-0.11.6.jar') as z:(A/'textures/entities/b08_brachiosaurus_guard.png').write_bytes(z.read('assets/mineminenomi/textures/models/morphs/brachiosaurus_guard.png'))
print('B08 initial10mechanics20poses authored; compilation and verification pending')
