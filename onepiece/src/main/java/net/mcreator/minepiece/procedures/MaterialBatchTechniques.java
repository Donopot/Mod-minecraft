package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** B03 finite casts. The explicit port adaptations and source paths live in docs/BATCH-03.md. */
public final class MaterialBatchTechniques {
 public static final Set<Item> FRUITS=Set.of(MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI,MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI,MinepieceModItems.ITEM_DEVIL_FRUIT_KIRA_KIRA,MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA,MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI);
 private static final class Action {
  final ServerPlayer owner;final ServerLevel world;final Item fruit;final DevilFruitMove move;final TechniqueTerrain.Budget terrain;
  final Set<UUID> hit=new HashSet<>();final List<EntityGomuArmEntity> effects=new ArrayList<>();Vec3 look,start;int age;
  Action(ServerPlayer p,Item f,DevilFruitMove m){owner=p;world=(ServerLevel)p.level();fruit=f;move=m;terrain=TechniqueTerrain.begin(p,m);look=p.getLookAngle();start=p.position();}
 }
 public static final class Shot {
  ServerPlayer owner;ServerLevel world;Item fruit;final DevilFruitMove move;TechniqueTerrain.Budget terrain;final Item behaviourFruit;Set<UUID> hit;final double damage,radius;final String profile;
  Shot(Action a,String profile,double radius,Set<UUID> hit){owner=a.owner;world=a.world;fruit=a.fruit;behaviourFruit=a.fruit;move=a.move;terrain=a.terrain;damage=a.move.damage();this.radius=radius;this.hit=hit;this.profile=profile;}
  public boolean valid(){return BatchRules.usable(owner,world,fruit);}
  public void reflect(ServerPlayer p){owner=p;world=(ServerLevel)p.level();fruit=DevilFruitPowerLogic.getDevilFruit(p).getItem();hit=new HashSet<>();terrain=null; /* Reflections preserve the projectile, never the former owner's terrain authority. */}
 }
 private static final class Rings {
  final Action source;final List<EntityGomuArmEntity> rings=new ArrayList<>();int age,lastTrigger=-100;
  Rings(Action a){source=a;for(int side:new int[]{-1,1})rings.add(fx(a,"b03_mochi_donut",a.owner.position(),1201,1));}
 }
 private static final Map<UUID,Action> ACTIVE=new HashMap<>();private static final Map<UUID,Rings> RINGS=new HashMap<>();
 private static final List<SkillProjectileEntity> SHOTS=new ArrayList<>();
 private static final Map<EntityGomuArmEntity,Action> VISUALS=new HashMap<>();
 private MaterialBatchTechniques(){}
 public static boolean handles(ItemStack f){return FRUITS.contains(f.getItem());}
 public static String shortName(Item f){return f==MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI?"mochi":f==MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI?"mori":f==MinepieceModItems.ITEM_DEVIL_FRUIT_KIRA_KIRA?"kira":f==MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA?"supa":"riki";}
 public static String animationFor(ServerPlayer p,String id){var f=DevilFruitPowerLogic.getDevilFruit(p);return handles(f)?"b03_"+shortName(f.getItem())+"_"+id:"";}
 public static int windup(String id){return switch(id){case "musodonatsu","mochibuild","diamondbody","stonethrowinglarge","diamondavalanche","rootgiant","rootsurround"->20;case "attackoverhead","sparbreak"->14;default->10;};}
 public static int activeCount(){return ACTIVE.size();}
 public static int shotCount(){SHOTS.removeIf(e->e.isRemoved());return SHOTS.size();}
 public static int ringCount(ServerPlayer p){var r=RINGS.get(p.getUUID());return r==null?0:r.rings.size();}
 public static boolean cast(ServerPlayer p,ItemStack f,DevilFruitMove m){
  if(!(p.level() instanceof ServerLevel l) || !handles(f) || !BatchRules.usable(p,l,f.getItem()) || ACTIVE.containsKey(p.getUUID()))return false;
  Action a=new Action(p,f.getItem(),m);ACTIVE.put(p.getUUID(),a);
  fx(a,"b03_"+shortName(a.fruit)+"_charge",p.getEyePosition().add(p.getLookAngle().scale(1.2)),windup(m.id())+2,.2);
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA){SupaBlades.activate(p,windup(m.id())+45);SupaBlades.setStance(p,SupaBlades.Stance.OFFENSIVE);}
  return true;
 }
 private static EntityGomuArmEntity fx(Action a,String profile,Vec3 at,int ttl,double size){var e=GoroTechniques.visual(a.owner,profile,at,ttl);e.setStretch((float)size);a.effects.add(e);VISUALS.put(e,a);return e;}
 private static EntityGomuArmEntity nativeArm(Action a,String profile,String animation,Vec3 at,double distance,int ttl){
  var e=fx(a,profile,at,ttl,Math.max(.05,distance/(a.move.id().equals("mochipistol")?12.25:10.25)));e.setModel("b05_elastic_arm");e.setTexture("mochi");e.setAnimation(animation);return e;
 }
 private static void end(Action a){ACTIVE.remove(a.owner.getUUID());}
 public static void cancel(ServerPlayer p){
  for(var entry:new ArrayList<>(VISUALS.entrySet()))if(entry.getValue().owner==p){entry.getKey().discard();VISUALS.remove(entry.getKey());}
  var a=ACTIVE.remove(p.getUUID());if(a!=null)for(var e:a.effects)e.discard();
  var rings=RINGS.remove(p.getUUID());if(rings!=null)for(var e:rings.rings)e.discard();
  for(var e:new ArrayList<>(SHOTS))if(e.materialShot!=null && e.materialShot.owner==p)e.discard();
  StickyState.releaseAllFrom(p,"retrait");Roots.releaseAllFrom(p,"retrait");SupaBlades.retract(p);DiamondBody.end(p,"retrait");RikiForce.release(p,"retrait");
 }
 public static void clearAll(){for(var a:new ArrayList<>(ACTIVE.values()))cancel(a.owner);for(var r:new ArrayList<>(RINGS.values()))cancel(r.source.owner);for(var e:SHOTS)e.discard();SHOTS.clear();for(var e:VISUALS.keySet())e.discard();VISUALS.clear();}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->cancel(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());}
 private static double clippedDistance(ServerLevel level,Vec3 from,Vec3 direction,double range){var hit=level.clip(new net.minecraft.world.level.ClipContext(from,from.add(direction.normalize().scale(range)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));return hit.getType()==HitResult.Type.MISS?range:Math.max(.05,from.distanceTo(hit.getLocation())-.02);}
 private static Vec3 flat(Vec3 d){var f=new Vec3(d.x,0,d.z);return f.lengthSqr()<.0001?new Vec3(0,0,1):f.normalize();}
 public static void tick(){
  VISUALS.entrySet().removeIf(entry->{var e=entry.getKey();var a=entry.getValue();if(e.isRemoved())return true;if(!BatchRules.usable(a.owner,a.world,a.fruit)){e.discard();return true;}return false;});
  SHOTS.removeIf(e->{if(e.isRemoved())return true;if(e.materialShot!=null && !e.materialShot.valid()){e.discard();return true;}return false;});
  for(var r:new ArrayList<>(RINGS.values())){
   var a=r.source;if(!BatchRules.usable(a.owner,a.world,a.fruit) || ++r.age>1200){RINGS.remove(a.owner.getUUID());for(var e:r.rings)e.discard();continue;}
   Vec3 right=flat(a.owner.getLookAngle()).cross(new Vec3(0,1,0));int i=0;for(var e:r.rings){e.setPos(a.owner.position().add(right.scale(i++==0?-1.4:1.4)).add(0,1.5,0).subtract(flat(a.owner.getLookAngle()).scale(.5)));e.setYRot(a.owner.getYRot());e.setXRot(a.owner.getXRot());}
  }
  for(var a:new ArrayList<>(ACTIVE.values())){
   if(!BatchRules.usable(a.owner,a.world,a.fruit)){cancel(a.owner);continue;}
   int t=++a.age-windup(a.move.id());String id=a.move.id();
   if(t<0){for(var e:a.effects)if(!e.isRemoved())e.setPos(a.owner.getEyePosition().add(a.owner.getLookAngle().scale(1.2)));continue;}
   if(t==0){for(var e:a.effects)e.discard();a.effects.clear();a.look=a.owner.getLookAngle();a.start=a.owner.position();TechniqueAnimations.playNamed(a.owner,animationFor(a.owner,id)+"_release");release(a);if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI && !Set.of("musodonatsu","mochibuild").contains(id))reactRings(a);}
   if(id.equals("mochigatling") && t%10==0 && t<=50){a.hit.clear();punch(a,t/10%2==0);}
   if(id.equals("rootgiant") && (t==4 || t==8))projectile(a,"b03_mori_root_branch",.7,0.6F,a.owner.getEyePosition().add(flat(a.look).cross(new Vec3(0,1,0)).scale(t==4?-.7:.7)),a.look,a.hit);
   if(Set.of("zangirimochi","brilliantpunk","atomicspurt","galleonlariat").contains(id) && t<6){ElementalBatchTechniques.blink(a.owner,flat(a.look),.65);fx(a,"b03_"+shortName(a.fruit)+"_dash",a.owner.position().add(0,.8,0),6,1);}
   if(t==6 && Set.of("zangirimochi","brilliantpunk","atomicspurt","galleonlariat").contains(id)){
    if(id.equals("galleonlariat"))projectile(a,"b03_riki_sweep",2,1,a.owner.getEyePosition(),flat(a.look),a.hit);
    else strike(a,a.owner.position(),Math.min(6,a.move.range()),true,a.move.damage());
    if(!Set.of("atomicspurt","galleonlariat").contains(id))a.terrain.impact(a.owner.position().add(flat(a.look).scale(.8)));
    fx(a,profile(a),a.owner.position().add(flat(a.look)).add(0,.8,0),14,1.7);
   }
   if(id.equals("rootsurround") && (t==0 || t==8 || t==16)){
    strike(a,a.owner.position(),6,false,a.move.damage());
    for(int j=0;j<8;j++){double angle=j*Math.PI/4;Vec3 at=a.owner.position().add(Math.cos(angle)*(2+t*.12),.05,Math.sin(angle)*(2+t*.12));if(!LightBeams.blocked(a.world,a.owner.getEyePosition(),at.subtract(a.owner.getEyePosition()).normalize(),at.distanceTo(a.owner.getEyePosition())))fx(a,"b03_mori_cage",at,25,1.6);}
    a.terrain.replaceSurface(a.owner.position(),6,Blocks.GRASS_BLOCK.defaultBlockState());
   }
   if(t>= (id.equals("mochigatling")?62:30))end(a);
  }
 }
 private static String profile(Action a){return "b03_"+shortName(a.fruit)+"_"+a.move.id();}
 private static void release(Action a){String id=a.move.id();
  switch(id){
   case "mochipistol"->punch(a,true);
   case "mochigatling","rootsurround","zangirimochi","brilliantpunk","atomicspurt","galleonlariat"->{}
   case "musodonatsu"->{var old=RINGS.remove(a.owner.getUUID());if(old!=null)for(var e:old.rings)e.discard();var rings=new Rings(a);RINGS.put(a.owner.getUUID(),rings);a.effects.clear();}
   case "mochibuild"->{var p=a.owner.blockPosition().offset((int)Math.round(flat(a.look).x*4),0,(int)Math.round(flat(a.look).z*4));MochiBuild.build(a.owner,p);fx(a,"b03_mochi_build",a.owner.position().add(flat(a.look).scale(4)).add(0,1,0),24,1);}
   case "diamondbody"->{DiamondBody.toggle(a.owner);fx(a,"b03_kira_body",a.owner.position().add(0,1,0),22,.8);}
   case "root","rootgiant"->projectile(a,profile(a),id.equals("root")?.6:1.2,id.equals("root")?.45F:1.1F,a.owner.getEyePosition().add(0,-.5,0),a.look,a.hit);
   case "yakimochi"->projectile(a,profile(a),1.6,.75F,a.owner.getEyePosition(),a.look,a.hit);
   case "diamondshard","stonethrowing"->projectile(a,profile(a),.65,.55F,a.owner.getEyePosition(),a.look,a.hit);
   case "diamondavalanche","stonethrowinglarge"->projectile(a,profile(a),2.5,1.5F,a.owner.getEyePosition(),a.look,a.hit);
   case "projectileslash"->projectile(a,profile(a),1.25,1,a.owner.getEyePosition(),a.look,a.hit);
   case "assault"->{strike(a,a.owner.position(),a.move.range(),true,a.move.damage());a.terrain.replaceSurface(a.owner.position(),6,Blocks.GRASS_BLOCK.defaultBlockState());fx(a,"b03_mori_assault",a.owner.position().add(flat(a.look).scale(2)),26,2.5);}
   case "attackoverhead","hadoelbow","sparklingdaisy","sparbreak"->{strike(a,a.owner.position(),a.move.range(),true,a.move.damage());
    double distance=clippedDistance(a.world,a.owner.getEyePosition(),a.look,Math.min(4,a.move.range()));
    a.terrain.impact(a.owner.getEyePosition().add(a.look.scale(distance)));
    fx(a,profile(a),a.owner.position().add(flat(a.look).scale(1.4)).add(0,.9,0),25,id.equals("sparklingdaisy")?2:1.5);}
   default->throw new IllegalArgumentException("Unreviewed B03 move "+id);
  }
 }
 private static void punch(Action a,boolean right){
  double distance=clippedDistance(a.world,a.owner.getEyePosition(),a.look,a.move.range());
  Vec3 side=flat(a.look).cross(new Vec3(0,1,0)).scale(right?.32:-.32);
  nativeArm(a,"b03_native_"+a.move.id(),a.move.id().equals("mochipistol")?"punch_gomu_pistol":right?"punch_gomu_gatling_right":"punch_gomu_gatling_left",a.owner.getEyePosition().add(side),distance,12);
  strike(a,a.owner.position(),a.move.range(),true,a.move.damage());a.terrain.impact(a.owner.getEyePosition().add(a.look.scale(distance)));
 }
 private static void reactRings(Action attack){var r=RINGS.get(attack.owner.getUUID());if(r==null || r.age-r.lastTrigger<20)return;r.lastTrigger=r.age;var hit=new HashSet<UUID>();r.source.hit.clear();Vec3 eye=attack.owner.getEyePosition(),end=eye.add(attack.owner.getLookAngle().scale(clippedDistance(attack.world,eye,attack.owner.getLookAngle(),32))),aim=end;double nearest=eye.distanceToSqr(end);
 for(var target:attack.world.getEntitiesOfClass(LivingEntity.class,new AABB(eye,end).inflate(1))){if(!BatchRules.target(attack.owner,target))continue;var intersection=target.getBoundingBox().inflate(.15).clip(eye,end);if(intersection.isPresent() && intersection.get().distanceToSqr(eye)<nearest){aim=intersection.get();nearest=aim.distanceToSqr(eye);}}
 for(var e:r.rings)projectile(r.source,"b03_mochi_ringfist",1,.75F,e.position(),aim.subtract(e.position()).normalize(),hit);}
 private static void strike(Action a,Vec3 center,double radius,boolean cone,double damage){
  var source=CombatSkillLogic.elementalDamageSource(a.world,ElementType.PHYSICAL,a.owner,null);
  for(var target:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius))){
   if(a.hit.contains(target.getUUID()) || !BatchRules.target(a.owner,target) || target.position().distanceTo(center)>radius)continue;
   var toward=target.getBoundingBox().getCenter().subtract(a.owner.getEyePosition()).normalize();if(cone && toward.dot(a.look)<CombatSkillLogic.MELEE_CONE_COS)continue;
   if(CombatSkillLogic.hurtAndConfirm(target,a.world,source,(float)damage)){a.hit.add(target.getUUID());confirmed(a.owner,target,a.fruit,a.move.id());CombatSkillLogic.applySkillKnockback(target,target.position().subtract(a.owner.position()),source,a.move.knockback());}
  }
 }
 private static void confirmed(ServerPlayer owner,LivingEntity target,Item fruit,String id){
  if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI)StickyState.stick(target,owner);
  if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI)Roots.entangle(target,owner);
 }
 private static void projectile(Action a,String profile,double radius,float scale,Vec3 at,Vec3 direction,Set<UUID> hit){
  var shot=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,at.x,at.y,at.z,direction,a.world);shot.setOwner(a.owner);
  String model=profile.contains("stonethrowing")?"rock_fragment":profile.equals("b03_supa_projectileslash")?"projectile_slash":"fist_small";
  String texture=profile.contains("stonethrowing")?"rock_fragment":profile.contains("yakimochi")?"tex_black":profile.equals("b03_supa_projectileslash")?"slash_white":"mochi";
  shot.model(model).texture(texture).visualProfile(profile).damage(a.move.damage()).element(ElementType.PHYSICAL).knockback(a.move.knockback()).lifetime(Math.max(60,a.move.lifetime())).range(32).scale(scale).trail(net.minecraft.core.particles.ParticleTypes.CRIT);
  shot.materialShot=new Shot(a,profile,radius,hit);shot.shoot(direction.x,direction.y,direction.z,(float)Math.max(.5,a.move.speed()),0);a.world.addFreshEntity(shot);SHOTS.add(shot);
 }
 public static void impact(SkillProjectileEntity projectile,Vec3 at,LivingEntity direct){
  var shot=projectile.materialShot;if(shot==null || !shot.valid())return;if(Boolean.getBoolean("opworldlike.repairTests") && shot.profile.equals("b03_mochi_ringfist"))org.slf4j.LoggerFactory.getLogger("B03Ring").info("[b03-ring-impact] at={} direct={} sharedHits={}",at,direct,shot.hit.size());var source=CombatSkillLogic.elementalDamageSource(shot.world,ElementType.PHYSICAL,shot.owner,projectile);
  for(var target:shot.world.getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(shot.radius))){
   if(shot.hit.contains(target.getUUID()) || !BatchRules.offensiveTarget(shot.owner,target) || target.getBoundingBox().distanceToSqr(at)>shot.radius*shot.radius)continue;
   Vec3 end=target.getBoundingBox().getCenter();var cover=shot.world.clip(new net.minecraft.world.level.ClipContext(at,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,shot.owner));if(cover.getType()!=HitResult.Type.MISS)continue;
   if(CombatSkillLogic.hurtAndConfirm(target,shot.world,source,(float)shot.damage)){shot.hit.add(target.getUUID());confirmed(shot.owner,target,shot.behaviourFruit,shot.move.id());CombatSkillLogic.applySkillKnockback(target,projectile.getDeltaMovement(),source,shot.move.knockback());}
  }
  if(shot.terrain!=null && shot.behaviourFruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI){var ground=net.minecraft.core.BlockPos.containing(at);for(int dy=0;dy<=4;dy++){var cell=ground.below(dy);if(!shot.world.getBlockState(cell).isAir()){shot.terrain.replaceSurface(new Vec3(at.x,cell.getY()+1,at.z),3,Blocks.GRASS_BLOCK.defaultBlockState());break;}}}else if(shot.terrain!=null)shot.terrain.impact(at);
  var effect=GoroTechniques.visual(shot.owner,shot.behaviourFruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI?"b03_mori_impact":shot.behaviourFruit==MinepieceModItems.ITEM_DEVIL_FRUIT_KIRA_KIRA?"b03_kira_impact":shot.behaviourFruit==MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA?"b03_supa_impact":shot.behaviourFruit==MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI?"b03_riki_impact":"b03_mochi_impact",at,16);effect.setStretch((float)Math.max(.5,shot.radius));
 }
}
