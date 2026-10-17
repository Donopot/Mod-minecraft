package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;

/** B02: finite source-shaped casts. Budgets and deliberate adaptations: docs/BATCH-02.md. */
public final class ControlledBatchTechniques {
 public static final Set<Item> FRUITS=Set.of(MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA,MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU,MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI,MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU,MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE);
 private static final class Action {
  final ServerPlayer owner;final ServerLevel world;final Item fruit;final DevilFruitMove move;
  final TechniqueTerrain.Budget terrain;final Set<UUID> hit=new HashSet<>();final List<EntityGomuArmEntity> effects=new ArrayList<>();
  int age;Vec3 start,look;LivingEntity held;net.minecraft.world.entity.ai.attributes.AttributeModifier holdModifier;boolean struck;
  Action(ServerPlayer p,Item f,DevilFruitMove m){owner=p;world=(ServerLevel)p.level();fruit=f;move=m;terrain=TechniqueTerrain.begin(p,m);start=p.position();look=p.getLookAngle();}
 }
 public static final class Shot {
  ServerPlayer owner;ServerLevel world;Item fruit;final TechniqueTerrain.Budget terrain;EntityGomuArmEntity warning;final String id;final double damage,radius;Set<UUID> hit;
  Shot(Action a,String id,double radius){owner=a.owner;world=a.world;fruit=a.fruit;this.id=id;damage=a.move.damage();this.radius=radius;hit=a.hit;terrain=a.terrain;}
  public boolean valid(){return BatchRules.usable(owner,world,fruit);}
  public void reflect(ServerPlayer p){owner=p;world=(ServerLevel)p.level();fruit=DevilFruitPowerLogic.getDevilFruit(p).getItem();hit=new HashSet<>();}
 }
 private static final Map<UUID,Action> ACTIVE=new HashMap<>();
 private static final List<SkillProjectileEntity> SHOTS=new ArrayList<>();
 private record Cooldowns(ServerPlayer owner,ServerLevel world,Map<String,Long> until){}
 private static final Map<UUID,Cooldowns> COOLDOWNS=new HashMap<>();
 private static final net.minecraft.resources.Identifier HOLD=net.minecraft.resources.Identifier.fromNamespaceAndPath("minepiece","b02_hold");
 public static boolean handles(ItemStack f){return FRUITS.contains(f.getItem());}
 public static boolean usable(ServerPlayer p,ServerLevel l,Item f){return BatchRules.usable(p,l,f);}
 public static String animationFor(ServerPlayer p,String id){return handles(DevilFruitPowerLogic.getDevilFruit(p))?"b02_"+id:"";}
 public static int activeCount(){return ACTIVE.size();}
 public static int shotCount(){SHOTS.removeIf(e->e.isRemoved());return SHOTS.size();}
 public static int windup(String id){return switch(id){case "passive"->8;case "ursusshock"->40;case "tenchimeido","zenshinkibaku","meteor","meteorsmall","room"->20;case "stoneopeope","kaishin","kabutowari","moko"->14;default->10;};}
 public static boolean onCooldown(ServerPlayer p,String id){var c=COOLDOWNS.get(p.getUUID());return c!=null && c.owner==p && c.world==p.level() && c.until.getOrDefault(id,0L)>c.world.getGameTime();}
 public static void cooldown(ServerPlayer p,DevilFruitMove m){var c=COOLDOWNS.computeIfAbsent(p.getUUID(),k->new Cooldowns(p,(ServerLevel)p.level(),new HashMap<>()));c.until.put(m.id(),c.world.getGameTime()+m.cooldownTicks());}
 public static boolean cast(ServerPlayer p,ItemStack f,DevilFruitMove m){
  if(!(p.level() instanceof ServerLevel l) || !handles(f) || !usable(p,l,f.getItem()) || ACTIVE.containsKey(p.getUUID()))return false;
  if(m.id().equals("room") && OpeRoom.isOpen(p))return false;
  if(m.id().equals("shambles") && !OpeRoom.canShambles(p))return false;
  if(m.id().equals("amputation") && (!OpeRoom.contains(p,p) || OpeRoom.targetInRoom(p)==null || OpeRoom.targetInRoom(p).isInvulnerable()))return false;
  Action a=new Action(p,f.getItem(),m);ACTIVE.put(p.getUUID(),a);
  fx(a,chargeProfile(f.getItem()),p.getEyePosition().add(p.getLookAngle().scale(1.3)).add(0,-.3,0),windup(m.id())+2);
  return true;
 }
 private static String chargeProfile(Item f){return f==MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA?"b02_quake_charge":f==MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU?"b02_bomb_charge":f==MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI?"b02_gravity_charge":f==MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU?"b02_paw_charge":"b02_ope_charge";}
 public static EntityGomuArmEntity visual(ServerPlayer p,String profile,Vec3 at,int ttl){return GoroTechniques.visual(p,profile,at,ttl);}
 private static EntityGomuArmEntity fx(Action a,String id,Vec3 at,int ttl){var e=visual(a.owner,id,at,ttl);a.effects.add(e);return e;}
 private static void interrupt(ServerPlayer p){Action a=ACTIVE.remove(p.getUUID());if(a!=null){releaseHeld(a);for(var e:a.effects)e.discard();}for(var e:new ArrayList<>(SHOTS))if(e.controlledShot!=null && e.controlledShot.owner==p)e.discard();GravityField.clearFrom(p);Levitation.releaseFrom(p);}
 public static void cancel(ServerPlayer p){interrupt(p);COOLDOWNS.remove(p.getUUID());}
 private static void end(Action a){ACTIVE.remove(a.owner.getUUID());releaseHeld(a);}
 public static void clearAll(){for(var a:new ArrayList<>(ACTIVE.values()))cancel(a.owner);for(var e:SHOTS)e.discard();SHOTS.clear();COOLDOWNS.clear();}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->cancel(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());}
 private static Vec3 flat(Vec3 v){Vec3 f=new Vec3(v.x,0,v.z);return f.lengthSqr()<.0001?new Vec3(0,0,1):f.normalize();}
 public static void tick(){
  SHOTS.removeIf(e->{if(e.isRemoved()){if(e.controlledShot!=null && e.controlledShot.warning!=null)e.controlledShot.warning.discard();return true;}if(e.controlledShot!=null && !e.controlledShot.valid()){if(e.controlledShot.warning!=null)e.controlledShot.warning.discard();e.discard();return true;}return false;});
  COOLDOWNS.values().removeIf(c->c.owner.isRemoved() || !c.owner.isAlive() || c.owner.level()!=c.world || !DevilFruitPowerLogic.getDevilFruit(c.owner).is(MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE));
  for(var a:new ArrayList<>(ACTIVE.values())){
   if(!usable(a.owner,a.world,a.fruit)){interrupt(a.owner);continue;}
   int t=++a.age-windup(a.move.id());String id=a.move.id();
   if(t<0){for(var e:a.effects)if(!e.isRemoved())e.setPos(a.owner.getEyePosition().add(a.owner.getLookAngle().scale(1.3)).add(0,-.3,0));continue;}
   if(t==0){for(var e:a.effects)e.discard();a.effects.clear();a.start=a.owner.position();a.look=a.owner.getLookAngle();TechniqueAnimations.playNamed(a.owner,"b02_"+id+"_release");release(a);}
   if(Set.of("attackspeed","assault","kumapunch","kickbomb").contains(id) && t<6){ElementalBatchTechniques.blink(a.owner,flat(a.look),.5);fx(a,"b02_dash",a.owner.position().add(0,.7,0),5);}
   if(t==6 && Set.of("attackspeed","assault","kumapunch","kickbomb").contains(id)){
    if(id.equals("kickbomb"))bomb(a,a.owner.getEyePosition().add(flat(a.look)),DevilFruitExplosions.radius(id));
    else area(a,a.owner.position(),Math.max(4,a.move.range()),true,a.move.damage(),id.equals("attackspeed")?"b02_gravity_horizontal":"b02_paw_impact");
   }
   if(id.equals("stoneopeope") && t%3==0 && t<12)projectile(a,id,a.owner.getEyePosition().add(flat(a.look).cross(new Vec3(0,1,0)).scale((t/3-1.5)*.35)),a.owner.getLookAngle(),.5F,0,1.5);
   if(id.equals("ursusshock")){
    if(t==0)fx(a,"b02_paw_warning",a.start,10).setStretch(20);
    if(t==10)projectile(a,"ursusshock",a.owner.getEyePosition(),a.look,.7F,20,1.4);
   }
   if(id.equals("kabutowari") || id.equals("zenshinkibaku")){
    if(t==0)hold(a);
    if(a.held!=null && (!BatchRules.target(a.owner,a.held) || a.held.level()!=a.world || a.held.position().distanceTo(a.owner.position())>8))releaseHeld(a);
    if(t==10){LivingEntity held=a.held;Vec3 heldAt=held==null?null:held.position();releaseHeld(a);if(id.equals("zenshinkibaku"))bomb(a,a.owner.position().add(0,.9,0),6);else {area(a,a.owner.position(),6,true,a.move.damage(),"b02_air_crack");if(heldAt!=null && a.hit.contains(held.getUUID()))a.terrain.impact(heldAt.add(0,.2,0));}}
   }
   if(id.equals("tenchimeido") && (t==4 || t==24)){Vec3 d=flat(a.look).yRot(t==4?-.12F:.12F);SeismicWave.castDirected(a.owner,a.move,d,a.hit,a.terrain);fx(a,t==4?"b02_tilt":"b02_tilt_reverse",a.start.add(d.scale(3)),20).setStretch(3);}
   if(id.equals("overheadsuper") && t==12)area(a,a.owner.position(),a.move.range(),true,a.move.damage(),"b02_quake_overhead");
   if(id.equals("bomboverhead") && t>=6 && !a.struck && (a.owner.onGround() || t>=30)){
    Vec3 foot=a.owner.position();var ground=a.world.clip(new ClipContext(foot.add(0,.1,0),foot.add(0,a.owner.onGround()?-.7:-8,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));
    if(ground.getType()==HitResult.Type.BLOCK){bomb(a,ground.getLocation().add(0,.08,0),4);a.struck=true;}
   }
   int finish=switch(id){case "tenchimeido"->54;case "kabutowari","zenshinkibaku","ursusshock"->30;case "bomboverhead"->44;case "stoneopeope"->28;default->22;};
   if(t>=finish)end(a);
  }
 }
 private static void release(Action a){var p=a.owner;String id=a.move.id();switch(id){
  case "attacksweep"->area(a,p.position(),a.move.range(),true,a.move.damage(),"b02_quake_sweep");
  case "overheadsuper"->{if(SafeDestination.isSafe(a.world,p,p.position().add(0,.5,0))){p.setDeltaMovement(0,.5,0);p.syncVelocity=true;}fx(a,"b02_quake_charge",p.getEyePosition(),12);}
  case "gekishin"->{SeismicWave.castDirected(p,a.move,flat(a.look),a.hit,a.terrain);fx(a,"b02_air_crack",p.getEyePosition().add(flat(a.look).scale(2)),18);}
  case "kaishin"->area(a,p.position(),6,false,a.move.damage(),"b02_quake_radial");
  case "punchbomb"->bomb(a,DevilFruitExplosions.resolveOriginFor(p,a.move),3);
  case "nosefancycannon"->projectile(a,id,p.getEyePosition(),a.look,.15F,2.5,2.2);
  case "bomboverhead"->{Vec3 next=p.position().add(0,.65,0);if(SafeDestination.isSafe(a.world,p,next)){p.setDeltaMovement(0,.65,0);p.syncVelocity=true;}fx(a,"b02_bomb_drop",p.position(),12);}
  case "gravito"->{area(a,p.position(),6,true,11,"b02_gravity_down");GravityField.create(a.world,p.position().add(flat(a.look).scale(3)),p,6,.3,40,false);}
  case "moko"->{area(a,p.position(),16,true,26,"b02_gravity_horizontal");}
  case "rubble"->projectile(a,id,p.getEyePosition(),a.look,1.2F,0,1.5);
  case "meteor","meteorsmall"->{
   Vec3 to=p.getEyePosition().add(a.look.scale(id.equals("meteor")?18:12));var clip=a.world.clip(new ClipContext(p.getEyePosition(),to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(clip.getType()==HitResult.Type.BLOCK)to=clip.getLocation();
   Vec3 impact=new Vec3(to.x,p.getY(),to.z);var warning=fx(a,"b02_meteor_warning",impact,40);warning.setStretch(id.equals("meteor")?5:3);
   projectile(a,id,impact.add(0,28,0),new Vec3(0,-1,0),id.equals("meteor")?1.8F:1F,id.equals("meteor")?5:3,.7);SHOTS.getLast().controlledShot.warning=warning;
  }
  case "padho"->projectile(a,id,p.getEyePosition(),a.look,.5F,0,2.5);
  case "passive"->{Vec3 from=p.getEyePosition();ElementalBatchTechniques.blink(p,p.getLookAngle().scale(p.isShiftKeyDown()?-1:1),8);GoroTechniques.link(p,from,p.getEyePosition(),"b02_paw_travel",10);}
  case "room"->OpeRoom.open(p);
  case "shambles"->{Vec3 from=p.position();if(OpeRoom.shambles(p)){GoroTechniques.link(p,from,p.position(),"b02_ope_swap",10);}}
  case "amputation"->{LivingEntity t=OpeRoom.targetInRoom(p);if(t!=null && t.position().distanceTo(p.position())<=5 && hit(a,t,12)){Amputation.amputate(a.world,p,t);fx(a,"b02_ope_cut",t.position().add(0,1,0),20);}}
  default->{}
 }}
 private static void hold(Action a){LivingEntity nearest=null;double best=6;for(var t:a.world.getEntitiesOfClass(LivingEntity.class,a.owner.getBoundingBox().inflate(6))){double d=t.distanceTo(a.owner);if(BatchRules.target(a.owner,t) && !t.isInvulnerable() && d<best){nearest=t;best=d;}}if(nearest!=null){a.held=nearest;var speed=nearest.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);if(speed!=null && speed.getModifier(HOLD)==null){a.holdModifier=new net.minecraft.world.entity.ai.attributes.AttributeModifier(HOLD,-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);speed.addTransientModifier(a.holdModifier);}}}
 private static void releaseHeld(Action a){if(a.held!=null){var speed=a.held.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);if(speed!=null && a.holdModifier!=null && speed.getModifier(HOLD)==a.holdModifier)speed.removeModifier(HOLD);a.held=null;a.holdModifier=null;}}
 private static boolean hit(Action a,LivingEntity target,double damage){if(a.hit.contains(target.getUUID()) || !BatchRules.target(a.owner,target))return false;var source=CombatSkillLogic.elementalDamageSource(a.world,a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU?ElementType.EXPLOSIVE:ElementType.PHYSICAL,a.owner,null);if(!CombatSkillLogic.hurtAndConfirm(target,a.world,source,(float)damage))return false;a.hit.add(target.getUUID());CombatSkillLogic.applySkillKnockback(target,target.position().subtract(a.owner.position()),source,a.move.knockback());return true;}
 private static void area(Action a,Vec3 at,double range,boolean cone,double damage,String profile){for(var t:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(range))){Vec3 delta=t.getBoundingBox().getCenter().subtract(a.owner.getEyePosition());if(t.position().distanceTo(at)>range || (cone && delta.normalize().dot(a.look)<CombatSkillLogic.MELEE_CONE_COS))continue;hit(a,t,damage);}fx(a,profile,at.add(0,.8,0),16).setStretch((float)Math.min(range,5));if(!a.move.id().equals("kabutowari")){var contact=a.world.clip(new ClipContext(a.owner.getEyePosition(),a.owner.getEyePosition().add(a.look.scale(Math.min(range,6))),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));if(!a.move.id().equals("attacksweep") || contact.getType()==HitResult.Type.BLOCK){Vec3 point=contact.getType()==HitResult.Type.BLOCK?contact.getLocation().subtract(a.look.scale(.02)):at.add(0,.3,0);if(a.move.id().equals("kaishin"))point=at.add(0,.3,0);if(a.move.id().equals("overheadsuper")){Vec3 down=at.add(flat(a.look).scale(2));var ground=a.world.clip(new ClipContext(down.add(0,.1,0),down.add(0,-8,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));point=ground.getType()==HitResult.Type.BLOCK?ground.getLocation().add(0,.02,0):down;}a.terrain.impact(point);}}}
 private static void bomb(Action a,Vec3 at,double radius){if(at==null)return;DevilFruitExplosions.damageOnly(a.owner,at,radius,a.move.damage(),ParticleTypes.SMOKE);a.terrain.impact(at);fx(a,"b02_bomb_blast",at,16).setStretch((float)radius);}
 private static void projectile(Action a,String id,Vec3 from,Vec3 direction,float scale,double radius,double speed){
  var shot=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,from.x,from.y,from.z,direction,a.world);shot.controlledShot=new Shot(a,id,radius);shot.setOwner(a.owner);shot.visualProfile("b02_"+id).damage(0).lifetime(100).range(64).model(id.contains("meteor")?"ball_maximum":"rubble").texture(id.equals("nosefancycannon")?"clear":id.contains("meteor")?"meteor":"tex_stone").scale(scale).trail(ParticleTypes.SMOKE);shot.shoot(direction.x,direction.y,direction.z,(float)speed,0);a.world.addFreshEntity(shot);SHOTS.add(shot);
 }
 public static void impact(SkillProjectileEntity e,Vec3 at,LivingEntity target){Shot s=e.controlledShot;if(Boolean.getBoolean("opworldlike.repairTests"))org.slf4j.LoggerFactory.getLogger("B02Shot").info("[b02-shot] impact id={} at={} target={} valid={} damage={}",s==null?"null":s.id,at,target==null?"block":target.getName().getString(),s!=null && s.valid(),s==null?0:s.damage);if(s==null || !s.valid())return;
  if(s.radius>0){DevilFruitExplosions.damageOnly(s.owner,at,s.radius,s.damage,ParticleTypes.SMOKE);visual(s.owner,s.id.contains("meteor")?"b02_meteor_impact":s.id.equals("ursusshock")?"b02_paw_blast":"b02_bomb_blast",at,16).setStretch((float)s.radius);}
  else if(target!=null && BatchRules.offensiveTarget(s.owner,target) && !s.hit.contains(target.getUUID())){var source=CombatSkillLogic.elementalDamageSource(s.world,ElementType.PHYSICAL,s.owner,e);if(CombatSkillLogic.hurtAndConfirm(target,s.world,source,(float)s.damage)){s.hit.add(target.getUUID());CombatSkillLogic.applySkillKnockback(target,e.getDeltaMovement(),source,s.id.equals("padho")?2:.5);}visual(s.owner,"b02_rock_impact",at,10);}
  if(s.warning!=null)s.warning.discard();s.terrain.impact(at);
 }
}
