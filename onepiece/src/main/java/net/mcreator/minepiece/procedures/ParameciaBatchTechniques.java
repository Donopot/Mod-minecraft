package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;

/** Source-shaped B04 casts; bounded port budgets and differences are recorded in BATCH-04.md. */
public final class ParameciaBatchTechniques {
 public static final Set<Item> FRUITS=Set.of(MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO,MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO,MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO,MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO,MinepieceModItems.ITEM_DEVIL_FRUIT_BISU_BISU);
 private static final class Action {
  final ServerPlayer owner;final ServerLevel world;final Item fruit;final DevilFruitMove move;final TechniqueTerrain.Budget terrain;final Set<UUID> hits=new HashSet<>();final List<EntityGomuArmEntity> visuals=new ArrayList<>();
  Vec3 look,start;int age;boolean landed;
  Action(ServerPlayer p,Item f,DevilFruitMove m){owner=p;world=(ServerLevel)p.level();fruit=f;move=m;terrain=TechniqueTerrain.begin(p,m);look=p.getLookAngle();start=p.position();}
 }
 public static final class Shot {
  final Action action;final boolean waiting;final double radius;int age;boolean parked;
  Shot(Action a,boolean wait,double r){action=a;waiting=wait;radius=r;}
  public boolean valid(){return BatchRules.usable(action.owner,action.world,action.fruit);}
 }
 private static final Map<UUID,Action> ACTIVE=new HashMap<>();
 private static final List<SkillProjectileEntity> SHOTS=new ArrayList<>();
 private static final Map<SummonEntity,Action> SUMMONS=new HashMap<>();
 private static final Map<EntityGomuArmEntity,Action> VISUALS=new HashMap<>();
 private ParameciaBatchTechniques(){}
 public static boolean handles(ItemStack fruit){return FRUITS.contains(fruit.getItem());}
 public static String shortName(Item f){return f==MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO?"aro":f==MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO?"horo":f==MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO?"mero":f==MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO?"kiro":"bisu";}
 public static String animationFor(ServerPlayer p,String id){var f=DevilFruitPowerLogic.getDevilFruit(p);return handles(f)?"b04_"+shortName(f.getItem())+"_"+id:"";}
 public static int windup(String id){return Set.of("tokuhoro","kirooverhead","biscuitwarrior2").contains(id)?20:id.equals("attackoverhead")?14:10;}
 public static int activeCount(){return ACTIVE.size();}
 public static boolean scriptedSlam(ServerPlayer p){var a=ACTIVE.get(p.getUUID());return a!=null && a.owner==p && a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO && Set.of("attackjump","kirooverhead").contains(a.move.id());}
 public static int shotCount(){SHOTS.removeIf(Entity::isRemoved);return SHOTS.size();}
 public static int miniCount(ServerPlayer p){return (int)SHOTS.stream().filter(e->!e.isRemoved() && e.parameciaShot.action.owner==p && e.parameciaShot.waiting).count();}
 public static int summonCount(ServerPlayer p){return (int)SUMMONS.entrySet().stream().filter(e->!e.getKey().isRemoved() && e.getValue().owner==p).count();}
 public static boolean summonValid(SummonEntity summon){var a=SUMMONS.get(summon);return a!=null && BatchRules.usable(a.owner,a.world,a.fruit);}
 public static boolean summonTarget(SummonEntity summon,LivingEntity target){var a=SUMMONS.get(summon);return a!=null && summonValid(summon) && BatchRules.offensiveTarget(a.owner,target) && summon.hasLineOfSight(target);}
 public static boolean owned(SummonEntity summon){return SUMMONS.containsKey(summon);}
 public static boolean cast(ServerPlayer p,ItemStack f,DevilFruitMove m){
  if(!(p.level() instanceof ServerLevel l) || !handles(f) || !BatchRules.usable(p,l,f.getItem()) || ACTIVE.containsKey(p.getUUID()))return false;
  if(m.id().equals("rap") && miniCount(p)==0 || m.id().equals("minihollow") && miniCount(p)>=9 || Set.of("kumac","biscuitwarrior","biscuitwarrior2").contains(m.id()) && summonCount(p)>=4)return false;
  var a=new Action(p,f.getItem(),m);ACTIVE.put(p.getUUID(),a);fx(a,"b04_"+shortName(a.fruit)+"_charge",p.getEyePosition().add(p.getLookAngle().scale(1)),windup(m.id())+1,.25);return true;
 }
 private static EntityGomuArmEntity fx(Action a,String id,Vec3 at,int ttl,double size){var e=GoroTechniques.visual(a.owner,id,at,ttl);e.setStretch((float)size);VISUALS.put(e,a);a.visuals.add(e);return e;}
 private static Vec3 flat(Vec3 look){var v=new Vec3(look.x,0,look.z);return v.lengthSqr()<.001?new Vec3(0,0,1):v.normalize();}
 private static String profile(Action a){return "b04_"+shortName(a.fruit)+"_"+a.move.id();}
 public static void cancel(ServerPlayer p){
  var active=ACTIVE.get(p.getUUID());if(active!=null && active.owner==p)ACTIVE.remove(p.getUUID(),active);for(var e:new ArrayList<>(VISUALS.entrySet()))if(e.getValue().owner==p){e.getKey().discard();VISUALS.remove(e.getKey());}
  for(var e:new ArrayList<>(SHOTS))if(e.parameciaShot.action.owner==p)e.discard();
  for(var e:new ArrayList<>(SUMMONS.entrySet()))if(e.getValue().owner==p){e.getKey().discard();SUMMONS.remove(e.getKey());}
  ParameciaControl.cancel(p);
 }
 public static void clearAll(){var owners=new HashSet<ServerPlayer>();ACTIVE.values().forEach(a->owners.add(a.owner));VISUALS.values().forEach(a->owners.add(a.owner));SUMMONS.values().forEach(a->owners.add(a.owner));SHOTS.forEach(e->owners.add(e.parameciaShot.action.owner));owners.forEach(ParameciaBatchTechniques::cancel);SHOTS.clear();}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->cancel(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());}
 public static void tick(){
  VISUALS.entrySet().removeIf(e->{if(!BatchRules.usable(e.getValue().owner,e.getValue().world,e.getValue().fruit))e.getKey().discard();return e.getKey().isRemoved();});
  SUMMONS.entrySet().removeIf(e->{if(!summonValid(e.getKey()))e.getKey().discard();return e.getKey().isRemoved();});
  for(var e:new ArrayList<>(SHOTS)){if(e.isRemoved()){SHOTS.remove(e);continue;}var s=e.parameciaShot;if(!s.valid()){e.discard();continue;}s.age++;
   if(s.waiting && s.age>=30){s.parked=true;e.setDeltaMovement(Vec3.ZERO);}if(!s.waiting && s.action.move.id().equals("tokuhoro") && s.age==75){impact(e,e.position(),null);e.discard();}
  }
  for(var a:new ArrayList<>(ACTIVE.values())){
   if(!BatchRules.usable(a.owner,a.world,a.fruit)){cancel(a.owner);continue;}
   int t=++a.age-windup(a.move.id());String id=a.move.id();if(t<0){for(var e:a.visuals)if(!e.isRemoved())e.setPos(a.owner.getEyePosition().add(a.owner.getLookAngle()));continue;}
   if(t==0){a.visuals.forEach(Entity::discard);a.visuals.clear();a.look=a.owner.getLookAngle();a.start=a.owner.position();TechniqueAnimations.playNamed(a.owner,animationFor(a.owner,id)+"_release");release(a);}
   if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO){
    if(id.equals("move") && t<8){ElementalBatchTechniques.blink(a.owner,flat(a.look),.5);fx(a,profile(a),a.owner.position().add(0,.8,0),8,.5);strike(a,a.owner.position(),3,true,a.move.damage());}
    if(id.equals("combo") && (t==2 || t==14)){a.hits.clear();strike(a,a.owner.position(),a.move.range(),true,t==2?18:20);fx(a,profile(a),a.owner.getEyePosition().add(flat(a.look).scale(2)),12,t==2?1:1.4);}
   }
   if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO && id.equals("perfumefemur") && t<8){ElementalBatchTechniques.blink(a.owner,flat(a.look),.5);strike(a,a.owner.position(),3,true,a.move.damage());if(t%2==0)fx(a,profile(a),a.owner.position().add(0,.8,0),12,1.2);}
   if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO && (id.equals("attackjump") || id.equals("kirooverhead")) && t<24 && !a.landed){
    Vec3 next=a.start.add(flat(a.look).scale((t+1)*.25)).add(0,Math.sin((t+1)*Math.PI/24)*3,0),delta=next.subtract(a.owner.position());
    double moved=ElementalBatchTechniques.blink(a.owner,delta,delta.length());
    if(t==23 || moved+.05<delta.length() && t>=12){a.landed=true;strike(a,a.owner.position(),id.equals("kirooverhead")?5:4,false,id.equals("attackjump")?15:a.move.damage());a.terrain.impact(a.owner.position().add(0,-.1,0));fx(a,"b04_kiro_impact",a.owner.position(),22,3);}
   }
   if(id.equals("biscuit") && (t==3 || t==6 || t==13)){var stack=new ItemStack(MinepieceModItems.BISCUIT);a.owner.getInventory().add(stack);if(!stack.isEmpty())a.world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(a.world,a.owner.getX(),a.owner.getY()+1,a.owner.getZ(),stack));fx(a,"b04_bisu_biscuit",a.owner.position().add(0,1,0),8,.4);}
   if(t>=40)ACTIVE.remove(a.owner.getUUID(),a);
  }
 }
 private static void release(Action a){String id=a.move.id();
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO){switch(id){
   case "arrow"->shoot(a,"aroaro_arrow","aroaro_arrow",false,.5,1.6,1);
   case "passive"->{ElementalBatchTechniques.blink(a.owner,flat(a.look).scale(a.owner.isShiftKeyDown()?-1:1),5);fx(a,profile(a),a.owner.position().add(0,.4,0),18,.8);}
   case "move","combo"->{}default->throw new IllegalArgumentException(id);
  }return;}
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO){switch(id){
   case "negativehollow"->shoot(a,"ghost1","ghost1_1",false,.6,.7,.8);
   case "minihollow"->{for(int i=-1;i<=1;i++)shoot(a,"ghost2","ghost1_2",true,1.7,.35,.3,i*.22);}
   case "tokuhoro"->shoot(a,"ghost2","ghost1_2",false,3,.45,1.4);
   case "rap"->{for(var shot:new ArrayList<>(SHOTS))if(!shot.isRemoved() && shot.parameciaShot.action.owner==a.owner && shot.parameciaShot.waiting){impact(shot,shot.position(),null);shot.discard();}fx(a,profile(a),a.owner.getEyePosition().add(a.look),18,.8);}
   case "kumac"->summon(a,"kumac","kumac",300,15);
   default->throw new IllegalArgumentException(id);
  }return;}
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO){
   if(id.equals("pistolkiss"))shoot(a,"heart_bullet","clear",false,.4,1.6,.45);
   else if(!id.equals("perfumefemur")){strike(a,a.owner.position(),a.move.range(),!id.equals("rotatekick"),a.move.damage());
    fx(a,profile(a),a.owner.position().add(flat(a.look).scale(id.equals("roundhousekick")?1.5:0)).add(0,.8,0),22,id.equals("rotatekick")?3:1.5);
    a.terrain.impact(a.owner.position().add(flat(a.look).scale(id.equals("roundhousekick")?2:0)));
   }return;
  }
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO){switch(id){
   case "attackoverhead"->{strike(a,a.owner.position(),a.move.range(),true,a.move.damage());a.terrain.impact(a.owner.position().add(flat(a.look).scale(2)));fx(a,profile(a),a.owner.position().add(flat(a.look).scale(1.5)).add(0,.5,0),22,1.8);}
   case "weightmanipulation"->{double current=WeightState.current(a.owner),next=current<.5?1:current<10?50:current<75?100:.1;double value=WeightState.request(a.owner,next);a.owner.sendSystemMessage(net.minecraft.network.chat.Component.literal("Poids confirmé : "+value+"×"),true);fx(a,profile(a),a.owner.position(),24,.8);}
   case "attackjump","kirooverhead"->{}default->throw new IllegalArgumentException(id);
  }return;}
  switch(id){
   case "crashpretzel"->{strike(a,a.owner.position(),a.move.range(),true,a.move.damage());Vec3 from=a.owner.getEyePosition(),end=from.add(a.look.scale(Math.min(8,a.move.range())));var hit=a.world.clip(new net.minecraft.world.level.ClipContext(from,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,a.owner));a.terrain.impact(hit.getLocation());fx(a,profile(a),from.add(a.look.scale(2)),24,2);}
   case "biscuitwarrior"->summon(a,"biscuit_warrrior","biscuit_warrrior",240,15);
   case "biscuitwarrior2"->{summon(a,"biscuit_warrrior","biscuit_warrrior",320,20);fx(a,"b04_bisu_reinforced",a.owner.position().add(a.look.scale(2)),24,1);}
   case "biscuit"->{}default->throw new IllegalArgumentException(id);
  }
 }
 private static void strike(Action a,Vec3 center,double range,boolean cone,double damage){
  var source=CombatSkillLogic.elementalDamageSource(a.world,ElementType.PHYSICAL,a.owner,null);
  for(var target:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(range))){
   if(a.hits.contains(target.getUUID()) || target.position().distanceTo(center)>range || !BatchRules.target(a.owner,target))continue;
   if(cone && target.getBoundingBox().getCenter().subtract(a.owner.getEyePosition()).normalize().dot(a.look)<CombatSkillLogic.MELEE_CONE_COS)continue;
   if(CombatSkillLogic.hurtAndConfirm(target,a.world,source,(float)damage)){a.hits.add(target.getUUID());confirmed(a,target);CombatSkillLogic.applySkillKnockback(target,target.position().subtract(a.owner.position()),source,a.move.knockback());}
  }
 }
 private static void confirmed(Action a,LivingEntity target){
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO && a.move.id().equals("negativehollow"))ParameciaControl.apply(a.owner,target,ParameciaControl.Kind.NEGATIVE);
  if(a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO && (a.move.id().equals("pistolkiss") || a.move.id().equals("perfumefemur")))ParameciaControl.apply(a.owner,target,ParameciaControl.Kind.STONE);
 }
 private static void shoot(Action a,String model,String texture,boolean wait,double radius,double speed,double scale){shoot(a,model,texture,wait,radius,speed,scale,0);}
 private static void shoot(Action a,String model,String texture,boolean wait,double radius,double speed,double scale,double offset){
  Vec3 direction=a.look.yRot((float)offset),at=a.owner.getEyePosition().add(direction.scale(.4));var e=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,at.x,at.y,at.z,direction,a.world);e.setOwner(a.owner);
  e.model(model).texture(texture).visualProfile(profile(a)).damage(a.move.damage()).element(ElementType.PHYSICAL).lifetime(wait?240:100).range(wait?32:48).scale((float)scale).trail(ParticleTypes.ASH);
  e.parameciaShot=new Shot(a,wait,radius);if(wait)e.accelerationPower=0;e.shoot(direction.x,direction.y,direction.z,(float)speed,0);a.world.addFreshEntity(e);SHOTS.add(e);
 }
 public static void collision(SkillProjectileEntity e,Vec3 at,LivingEntity direct){
  var s=e.parameciaShot;if(s==null || !s.valid()){e.discard();return;}
  if(s.waiting){s.parked=true;e.setPos(at);e.setDeltaMovement(Vec3.ZERO);return;}impact(e,at,direct);e.discard();
 }
 public static void impact(SkillProjectileEntity e,Vec3 at,LivingEntity direct){
  var s=e.parameciaShot;if(s==null || !s.valid())return;var a=s.action;
  var source=CombatSkillLogic.elementalDamageSource(a.world,ElementType.PHYSICAL,a.owner,e);
  for(var target:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(s.radius))){
   if(a.hits.contains(target.getUUID()) || !BatchRules.offensiveTarget(a.owner,target) || target.getBoundingBox().distanceToSqr(at)>s.radius*s.radius)continue;
   var ray=a.world.clip(new net.minecraft.world.level.ClipContext(at,target.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,a.owner));if(ray.getType()!=HitResult.Type.MISS)continue;
   if(CombatSkillLogic.hurtAndConfirm(target,a.world,source,(float)a.move.damage())){a.hits.add(target.getUUID());confirmed(a,target);CombatSkillLogic.applySkillKnockback(target,e.getDeltaMovement(),source,a.move.knockback());}
  }
  a.terrain.impact(at);fx(a,a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO?"b04_horo_burst":a.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO?"b04_mero_impact":"b04_aro_impact",at,20,s.radius);
 }
 private static void summon(Action a,String model,String texture,int ttl,double damage){
  Vec3 look=flat(a.look),at=a.owner.position().add(look.scale(2));if(!SafeDestination.isSafe(a.world,a.owner,at))at=a.owner.position();
  var e=new SummonEntity(MinepieceModEntities.SUMMON,a.world);e.setOwnerEntity(a.owner);e.setModel(model);e.setTexture(texture);e.lifetime(ttl);e.setDamage(damage);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(70);e.setHealth(70);e.getAttribute(Attributes.ARMOR).setBaseValue(a.move.id().equals("biscuitwarrior2")?20:10);e.setPos(at);e.setYRot(a.owner.getYRot());SUMMONS.put(e,a);a.world.addFreshEntity(e);e.setOrder(SummonEntity.Order.ATTAQUER);
 }
}
