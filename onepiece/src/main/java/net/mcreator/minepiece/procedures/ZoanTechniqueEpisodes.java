package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;

/** Source-shaped Rokushiki/Zoan episodes, with exact owner/world/fruit authority and per-cast quotas. */
public final class ZoanTechniqueEpisodes {
 public static final Item[] FRUITS={MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF,MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD,MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_SABERTIGER,MinepieceModItems.ITEM_DEVIL_FRUIT_USHI_USHI_GIRAFFE,MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO};
 public static final String[] NAMES={"wolf","leopard","saber","giraffe","batto"};
 public enum Kind { THRUST,DASH,SLASH,WOLF_SLASH,BIRD_SLASH,LEAP,RUSH,COMBO,CLAW_SHOT,FANG,NET,SPIN,FAN,PRESSURE,FINGER,GUARD,CHASE }
 private static final Map<String,Kind> MOVES=Map.ofEntries(Map.entry("shigan",Kind.THRUST),Map.entry("soru",Kind.DASH),Map.entry("rankyaku",Kind.SLASH),Map.entry("lupusfall",Kind.WOLF_SLASH),Map.entry("gaicho",Kind.BIRD_SLASH),Map.entry("attackjump",Kind.LEAP),Map.entry("attackspeed",Kind.RUSH),Map.entry("shugan",Kind.RUSH),Map.entry("okamihajiki",Kind.RUSH),Map.entry("jushigan",Kind.THRUST),Map.entry("shiganmadara",Kind.COMBO),Map.entry("gagan",Kind.CLAW_SHOT),Map.entry("kibasen",Kind.FANG),Map.entry("localareanetwork",Kind.NET),Map.entry("rankyakuran",Kind.FAN),Map.entry("amanedachi",Kind.SPIN),Map.entry("rokuogan",Kind.PRESSURE),Map.entry("tobushigan",Kind.FINGER),Map.entry("kamiezanshin",Kind.GUARD),Map.entry("yurushitene",Kind.RUSH),Map.entry("poursuite",Kind.CHASE));
 private static final class Action {
  final ServerPlayer owner;final ServerLevel world;final Item fruit;final DevilFruitMove move;final Kind kind;final TechniqueTerrain.Budget terrain;
  final Set<UUID> hits=new HashSet<>();Vec3 look,start;LivingEntity chaseTarget;int age,contacts;long until;boolean stopped,landed;
  Action(ServerPlayer p,Item f,DevilFruitMove m){owner=p;world=(ServerLevel)p.level();fruit=f;move=m;kind=f==FRUITS[0]&&m.id().equals("rankyaku")?Kind.WOLF_SLASH:MOVES.get(m.id());terrain=TechniqueTerrain.begin(p,m);look=p.getLookAngle();start=p.position();}
  boolean valid(){return BatchRules.usable(owner,world,fruit);}
 }
 public static final class Shot {private final Action action;final double radius;Shot(Action a,double r){action=a;radius=r;}public boolean valid(){return action.valid();}}
 private static final Map<UUID,Action> ACTIVE=new HashMap<>(),GUARDS=new HashMap<>();
 private static final Map<EntityGomuArmEntity,Action> VISUALS=new HashMap<>();
 private static final List<SkillProjectileEntity> SHOTS=new ArrayList<>();
 private static final Map<UUID,Long> LAST_DODGE=new HashMap<>();
 private ZoanTechniqueEpisodes(){}
 public static boolean handles(ItemStack f){return Arrays.asList(FRUITS).contains(f.getItem());}
 public static String shortName(Item f){return NAMES[Arrays.asList(FRUITS).indexOf(f)];}
 public static Kind kind(String id){return MOVES.get(id);}
 public static String animationFor(ServerPlayer p,String id){var f=DevilFruitPowerLogic.getDevilFruit(p);return handles(f)?"b06_"+shortName(f.getItem())+"_"+id:"";}
 public static int windup(String id){return Set.of("rokuogan","amanedachi","kamiezanshin").contains(id)?20:id.equals("soru")?8:10;}
 public static int activeCount(){return ACTIVE.size();}
 public static boolean active(ServerPlayer p){var a=ACTIVE.get(p.getUUID());return a!=null&&a.owner==p;}
 public static int shotCount(){return (int)SHOTS.stream().filter(e->!e.isRemoved()).count();}
 public static boolean guard(ServerPlayer p){var a=GUARDS.get(p.getUUID());return a!=null&&a.owner==p&&a.valid();}
 public static boolean cast(ServerPlayer p,ItemStack f,DevilFruitMove m){
  if(!handles(f)||!MOVES.containsKey(m.id())||!BatchRules.usable(p,(ServerLevel)p.level(),f.getItem())||ACTIVE.containsKey(p.getUUID()))return false;
  if(m.id().equals("kamiezanshin")&&guard(p)||m.id().equals("poursuite")&&WolfPursuit.isPursuing(p))return false;
  if(m.id().equals("poursuite")){var t=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(24)).stream().filter(e->BatchRules.target(p,e)&&!e.isInvulnerable()&&e.getBoundingBox().getCenter().subtract(p.getEyePosition()).normalize().dot(p.getLookAngle())>=.35).min(Comparator.comparingDouble(p::distanceToSqr));if(t.isEmpty())return false;var a=new Action(p,f.getItem(),m);a.chaseTarget=t.get();ACTIVE.put(p.getUUID(),a);fx(a,"b06_charge",p.getEyePosition().add(p.getLookAngle()),windup(m.id())+1,.15);return true;}
  var a=new Action(p,f.getItem(),m);ACTIVE.put(p.getUUID(),a);fx(a,"b06_charge",p.getEyePosition().add(p.getLookAngle()),windup(m.id())+1,.15);return true;
 }
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->cancel(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clear());}
 public static void cancel(ServerPlayer p){var a=ACTIVE.get(p.getUUID());if(a!=null&&a.owner==p)ACTIVE.remove(p.getUUID(),a);var g=GUARDS.get(p.getUUID());if(g!=null&&g.owner==p)GUARDS.remove(p.getUUID(),g);LAST_DODGE.remove(p.getUUID());VISUALS.entrySet().removeIf(e->{if(e.getValue().owner!=p)return false;e.getKey().discard();return true;});for(var e:SHOTS)if(e.zoanShot.action.owner==p)e.discard();WolfPursuit.end(p,"retrait");ImuCommand.cancel(p);ZoanCounterState.cancel(p);}
 public static void clear(){var owners=new HashSet<ServerPlayer>();ACTIVE.values().forEach(a->owners.add(a.owner));GUARDS.values().forEach(a->owners.add(a.owner));VISUALS.values().forEach(a->owners.add(a.owner));SHOTS.forEach(e->owners.add(e.zoanShot.action.owner));owners.forEach(ZoanTechniqueEpisodes::cancel);SHOTS.clear();ZoanCounterState.clear();}
 /** A finite evasion/counter stance. Source's 999999 is guard context, never health damage. */
 public static boolean dodge(ServerPlayer p,net.minecraft.world.damagesource.DamageSource source){var a=GUARDS.get(p.getUUID());if(a==null||!a.valid()||!(source.getEntity() instanceof LivingEntity attacker)||!BatchRules.target(p,attacker)||p.distanceTo(attacker)>6||p.level().getGameTime()-LAST_DODGE.getOrDefault(p.getUUID(),-999L)<8)return false;
  var side=flat(attacker.position().subtract(p.position())).cross(new Vec3(0,1,0));double distance=ElementalBatchTechniques.blink(p,side,.8);if(distance<.25)return false;LAST_DODGE.put(p.getUUID(),p.level().getGameTime());ZoanCounterState.apply(p,attacker);fx(a,"b06_evade",p.position(),12,.8);return true;
 }
 public static void tick(){ZoanCounterState.tick();ZoanForms.tick();VISUALS.entrySet().removeIf(e->{if(!e.getValue().valid())e.getKey().discard();return e.getKey().isRemoved();});SHOTS.removeIf(e->{if(!e.zoanShot.valid())e.discard();return e.isRemoved();});GUARDS.entrySet().removeIf(e->{var a=e.getValue();return !a.valid()||a.world.getGameTime()>=a.until;});
  for(var a:new ArrayList<>(ACTIVE.values())){if(!a.valid()){cancel(a.owner);continue;}int t=++a.age-windup(a.move.id());if(t<0)continue;
   if(t==0){a.look=a.owner.getLookAngle();a.start=a.owner.position();TechniqueAnimations.playNamed(a.owner,animationFor(a.owner,a.move.id())+"_release");release(a);}
   if(a.kind==Kind.COMBO&&t%12==0&&t<=24){a.hits.clear();contact(a,a.owner.getEyePosition().add(a.look.scale(2)),2.5,a.move.damage()/3,true);var ray=a.world.clip(new ClipContext(a.owner.getEyePosition(),a.owner.getEyePosition().add(a.look.scale(3)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));a.terrain.impact(ray.getLocation());fx(a,"b06_claw",a.owner.getEyePosition().add(a.look.scale(1.5)),10,.8);a.contacts++;}
   if(a.kind==Kind.FAN&&t%6==0&&t<=12)shoot(a,"b06_slash",1.1,1.2,(t/6-1)*.28);
   if(a.kind==Kind.NET&&t%6==0&&t<=12)shoot(a,"b06_net",.75,1,(t/6-1)*.2);
   if((a.kind==Kind.RUSH||a.kind==Kind.FANG)&&t<10&&!a.stopped){var old=a.owner.position();double moved=ElementalBatchTechniques.blink(a.owner,flat(a.look),.55);contact(a,a.owner.getEyePosition().add(a.look.scale(1.25)),2,a.move.damage(),true);if(moved<.45||t==9){a.stopped=true;a.terrain.impact(a.owner.getEyePosition().add(a.look.scale(.5)));}if(t%2==0)fx(a,a.kind==Kind.FANG?"b06_fangs":"b06_rush",old.add(0,.8,0),12,1);}
   if(a.kind==Kind.LEAP&&t<28&&!a.landed){var next=a.start.add(flat(a.look).scale((t+1)*.25)).add(0,Math.sin((t+1)*Math.PI/28)*3,0);var delta=next.subtract(a.owner.position());double moved=ElementalBatchTechniques.blink(a.owner,delta,delta.length());if(t==27||moved+.05<delta.length()&&t>=14){a.landed=true;contact(a,a.owner.position(),4,15,false);a.terrain.impact(a.owner.position().add(0,-.1,0));fx(a,"b06_slam",a.owner.position(),20,3);}}
   if(a.kind==Kind.SPIN&&t<16){double d=(t+1)*.3;var at=a.start.add(0,1,0).add(flat(a.look).scale(d));contact(a,at,3,a.move.damage(),false);a.terrain.impact(at);if(t%3==0)fx(a,"b06_spin",at,14,3);}
   if(a.kind==Kind.PRESSURE&&t<12&&!a.stopped){Vec3 from=a.start.add(0,1.2,0),to=from.add(a.look.scale(t+1));var ray=a.world.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));if(ray.getType()==HitResult.Type.BLOCK){a.stopped=true;a.terrain.impact(ray.getLocation());}else{contact(a,to,2,a.move.damage(),false);a.terrain.impact(to);if(t%2==0)fx(a,"b06_pressure",to,12,1.8);}}
   if(t>=(a.kind==Kind.COMBO?25:60))ACTIVE.remove(a.owner.getUUID(),a);
  }
 }
 private static void release(Action a){switch(a.kind){
  case THRUST->{contact(a,a.owner.getEyePosition().add(a.look.scale(1.2)),a.move.id().equals("jushigan")?2.5:1.7,a.move.damage(),true);var ray=a.world.clip(new ClipContext(a.owner.getEyePosition(),a.owner.getEyePosition().add(a.look.scale(2.5)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));a.terrain.impact(ray.getLocation());fx(a,"b06_thrust",a.owner.getEyePosition().add(a.look.scale(1.2)),12,.6);}
  case DASH->{ElementalBatchTechniques.blink(a.owner,flat(a.look),6);fx(a,"b06_evade",a.start.add(0,1,0),16,1);}
  case SLASH->shoot(a,"b06_slash",.8,1.1,0);
  case WOLF_SLASH->shoot(a,"b06_wolf_slash",1.1,1,0);
  case BIRD_SLASH->shoot(a,"b06_bird_slash",1.6,.9,0);
  case CLAW_SHOT->shoot(a,a.move.id().equals("gagan")?"b06_gagan":"b06_claw",.9,1.1,0);
  case FINGER->shoot(a,"b06_finger",.25,2,0);
  case CHASE->{if(a.chaseTarget!=null&&a.chaseTarget.level()==a.world&&BatchRules.target(a.owner,a.chaseTarget))WolfPursuit.begin(a.owner,a.chaseTarget);fx(a,"b06_rush",a.start.add(0,1,0),16,1);}
  case GUARD->{a.until=a.world.getGameTime()+60;GUARDS.put(a.owner.getUUID(),a);ACTIVE.remove(a.owner.getUUID(),a);fx(a,"b06_guard",a.owner.position(),60,1);}
  default->{}
 }}
 private static Vec3 flat(Vec3 v){var f=new Vec3(v.x,0,v.z);return f.lengthSqr()<.0001?new Vec3(0,0,1):f.normalize();}
 private static EntityGomuArmEntity fx(Action a,String profile,Vec3 at,int ttl,double size){var e=GoroTechniques.visual(a.owner,profile,at,ttl);e.setStretch((float)size);VISUALS.put(e,a);return e;}
 private static void contact(Action a,Vec3 at,double radius,double damage,boolean cone){var source=a.world.damageSources().playerAttack(a.owner);for(var t:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(radius))){if(a.hits.contains(t.getUUID())||t.getBoundingBox().distanceToSqr(at)>radius*radius||!BatchRules.target(a.owner,t))continue;if(cone&&t.getBoundingBox().getCenter().subtract(a.owner.getEyePosition()).normalize().dot(a.look)<.35)continue;if(CombatSkillLogic.hurtAndConfirm(t,a.world,source,(float)damage)){a.hits.add(t.getUUID());CombatSkillLogic.applySkillKnockback(t,a.look,source,a.move.knockback());}}}
 private static void shoot(Action a,String profile,double radius,double speed,double angle){Vec3 dir=a.look.yRot((float)angle),at=a.owner.getEyePosition().add(dir.scale(.35));var e=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,at.x,at.y,at.z,dir,a.world);e.setOwner(a.owner);e.model("note_3d").texture("note_3d").visualProfile(profile).damage(a.move.damage()).element(ElementType.PHYSICAL).lifetime(45).range(32).scale(.3F).trail(ParticleTypes.SWEEP_ATTACK);e.zoanShot=new Shot(a,radius);e.accelerationPower=0;e.shoot(dir.x,dir.y,dir.z,(float)speed,0);a.world.addFreshEntity(e);SHOTS.add(e);}
 public static void impact(SkillProjectileEntity e,Vec3 at,LivingEntity direct){var s=e.zoanShot;if(s==null||!s.valid())return;var a=s.action;var source=CombatSkillLogic.elementalDamageSource(a.world,ElementType.PHYSICAL,a.owner,e);for(var t:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(s.radius))){if(a.hits.contains(t.getUUID())||!BatchRules.offensiveTarget(a.owner,t)||t.getBoundingBox().distanceToSqr(at)>s.radius*s.radius)continue;var ray=a.world.clip(new ClipContext(at,t.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a.owner));if(ray.getType()!=HitResult.Type.MISS)continue;if(CombatSkillLogic.hurtAndConfirm(t,a.world,source,(float)a.move.damage())){a.hits.add(t.getUUID());if(a.kind==Kind.NET)ImuCommand.apply(a.owner,t,20);CombatSkillLogic.applySkillKnockback(t,e.getDeltaMovement(),source,a.move.knockback());}}a.terrain.impact(at);fx(a,"b06_impact",at,12,.7);}
}
