from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java'
def change(path,old,new):
 p=J/path;s=p.read_text(encoding='utf-8-sig');assert old in s,(path,old);p.write_text(s.replace(old,new),encoding='utf-8')
base='net/mcreator/minepiece/'
change(base+'procedures/DevilFruitBehaviours.java','if(ControlledBatchTechniques.handles(fruit))','if(MaterialBatchTechniques.handles(fruit))return MaterialBatchTechniques.cast(player,fruit,move);\n      if(ControlledBatchTechniques.handles(fruit))')
change(base+'procedures/DevilFruitPowerLogic.java','ControlledBatchTechniques.cancel(player);','ControlledBatchTechniques.cancel(player);MaterialBatchTechniques.cancel(player);')
change(base+'procedures/TechniqueAnimations.java','String controlled=ControlledBatchTechniques.animationFor','String material=MaterialBatchTechniques.animationFor(player,move.id());if(!material.isEmpty())return material;\n      String controlled=ControlledBatchTechniques.animationFor')
change('br/com/oneminepiece/opworldlike/OmpWorld.java','ControlledBatchTechniques.register();','ControlledBatchTechniques.register();\n      net.mcreator.minepiece.procedures.MaterialBatchTechniques.register();')
change(base+'entity/SkillProjectileEntity.java','public net.mcreator.minepiece.procedures.ControlledBatchTechniques.Shot controlledShot;','public net.mcreator.minepiece.procedures.ControlledBatchTechniques.Shot controlledShot;\n   public net.mcreator.minepiece.procedures.MaterialBatchTechniques.Shot materialShot;')
change(base+'entity/SkillProjectileEntity.java','if(controlledShot!=null){net.mcreator.minepiece.procedures.ControlledBatchTechniques.impact(this,result.getLocation(),living);','if(materialShot!=null){net.mcreator.minepiece.procedures.MaterialBatchTechniques.impact(this,result.getLocation(),living);discard();return;}\n         if(controlledShot!=null){net.mcreator.minepiece.procedures.ControlledBatchTechniques.impact(this,result.getLocation(),living);')
change(base+'entity/SkillProjectileEntity.java','if(controlledShot!=null){net.mcreator.minepiece.procedures.ControlledBatchTechniques.impact(this,result.getLocation().subtract','if(materialShot!=null){net.mcreator.minepiece.procedures.MaterialBatchTechniques.impact(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);discard();return;}\n      if(controlledShot!=null){net.mcreator.minepiece.procedures.ControlledBatchTechniques.impact(this,result.getLocation().subtract')
change(base+'entity/SkillProjectileEntity.java','if(controlledShot!=null && !controlledShot.valid())','if(materialShot!=null && !materialShot.valid()){discard();return;}\n      if(controlledShot!=null && !controlledShot.valid())')
change(base+'procedures/NikyuRepulsion.java','if(projectile.controlledShot!=null)','if(projectile.materialShot!=null)projectile.materialShot.reflect(porteur);\n      if(projectile.controlledShot!=null)')
p=P/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('version=1.0.1-alpha.11','version=1.0.1-alpha.12'),encoding='utf-8')
change(base+'procedures/MaterialBatchTechniques.java','LightBeams.clippedDistance(', 'clippedDistance(')
change(base+'procedures/MaterialBatchTechniques.java',' private static Vec3 flat(Vec3 d)', ''' private static double clippedDistance(ServerLevel level,Vec3 from,Vec3 direction,double range){var hit=level.clip(new net.minecraft.world.level.ClipContext(from,from.add(direction.normalize().scale(range)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,null));return hit.getType()==HitResult.Type.MISS?range:Math.max(.05,from.distanceTo(hit.getLocation())-.02);}
 private static Vec3 flat(Vec3 d)''')
rules='''
  destructive("mochi_mochi","mochipistol",1.25,24,false,"AttackStrongPunch true contact, bounded adaptation");
  destructive("mochi_mochi","mochigatling",1,48,false,"AttackWeakPunch six native pulses share quota");
  none("mochi_mochi","yakimochi","AIFist mode -2 has no mode1 block destruction");
  destructive("mochi_mochi","zangirimochi",1.5,48,false,"AttackSpeed finishing contact");
  destructive("mochi_mochi","musodonatsu",1.25,64,false,"Two reactive rings share a lifetime terrain quota");
  transform("mochi_mochi","mochibuild","Plan-added honey construction, permanent24blocks");
  transform("mori_mori","root,rootgiant,assault,rootsurround","TreeRoot and BlockToGrass, permanent selective grass conversion");
  destructive("kira_kira","attackoverhead",4.5,128,false,"AttackOverhead actual active contact");
  destructive("kira_kira","brilliantpunk",1.5,48,false,"AttackStrongPunch after shoulder rush");
  destructive("kira_kira","diamondshard",1.25,48,false,"RockFragment handler; diamond identity adaptation");
  destructive("kira_kira","diamondavalanche",4.5,192,false,"Large RockFragment handler, bounded adaptation");
  none("kira_kira","diamondbody","Body coat never changes terrain");
  destructive("supa_supa","projectileslash",1.5,64,false,"AIProjectileSlash true collision");
  none("supa_supa","atomicspurt","Source StepBlock dash, charge explosion interaction NONE");
  destructive("supa_supa","sparklingdaisy",1,96,false,"Six source fan phases share quota, port bounded contact");
  destructive("supa_supa","sparbreak",1,64,false,"Bilateral slash active contact");
  destructive("riki_riki","attackoverhead",4.5,128,false,"AttackOverhead active impact");
  destructive("riki_riki","hadoelbow",3.5,128,false,"HadoElbow active impact");
  destructive("riki_riki","galleonlariat",3,96,false,"AIProjectileSweep actual collision");
  destructive("riki_riki","stonethrowing",1.25,48,false,"RockFragment actual collision; no held target required");
  destructive("riki_riki","stonethrowinglarge",4.5,192,false,"Large RockFragment actual collision");
'''
change(base+'procedures/TechniqueTerrain.java','  destructive("yami_yami","blackhole"',rules+'  destructive("yami_yami","blackhole"')
print('B03 wired24 routes and24 terrain decisions')
