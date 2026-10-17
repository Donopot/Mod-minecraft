package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;

/** B01's 26 fruit-scoped episodes. Port budgets and explicit adaptations: docs/BATCH-01.md. */
public final class ElementalBatchTechniques {
 private static final Set<Item> FRUITS=Set.of(MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU,MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA,MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI);
 private static final class Action {
  final ServerPlayer owner;final ServerLevel world;final ItemStack fruit;final DevilFruitMove move;Vec3 start,direction;
  final TechniqueTerrain.Budget terrain;final Set<UUID> hit=new HashSet<>();final List<EntityGomuArmEntity> effects=new ArrayList<>();int age;
  Action(ServerPlayer p,ItemStack f,DevilFruitMove m){owner=p;world=(ServerLevel)p.level();fruit=f.copy();move=m;terrain=TechniqueTerrain.begin(p,m);start=p.position();direction=p.getLookAngle();}
 }
 private static final Map<UUID,Action> ACTIVE=new HashMap<>();
 private static final Map<String,Integer> RELEASES=new HashMap<>();
 private ElementalBatchTechniques(){}
 public static boolean handles(ItemStack f){return FRUITS.contains(f.getItem());}
 public static int activeCount(){return ACTIVE.size();}
 public static int releases(String id){return RELEASES.getOrDefault(id,0);}
 public static String animationFor(ServerPlayer p,String id){return handles(DevilFruitPowerLogic.getDevilFruit(p))?"b01_"+id:"";}
 public static boolean cast(ServerPlayer p,ItemStack fruit,DevilFruitMove move){
  if(!(p.level() instanceof ServerLevel l) || !handles(fruit) || !BatchRules.usable(p,l,fruit.getItem()) || ACTIVE.containsKey(p.getUUID()))return false;
  if(move.id().equals("laser2"))return LightCharge.begin(p,fruit,move);
  if(move.id().equals("amanomurakumo")){
   if(!LightSwords.grant(p))return false;
  }
  Action a=new Action(p,fruit,move);ACTIVE.put(p.getUUID(),a);
  String profile=fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE)?"b01_ice_charge":fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA)?"b01_light_charge":fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU)?"b01_smoke_charge":fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA)?"b01_sand_charge":"b01_dark_charge";
  a.effects.add(fx(p,profile,p.getEyePosition().add(p.getLookAngle().scale(1.2)).add(.3,-.3,0),windup(move.id())+2));return true;
 }
 public static int windup(String id){return switch(id){case "passive","yatanokagami"->2;case "iceage","grounddeath","blackhole","darkmatter"->20;case "sables","whiteout","pheasantbeak"->14;default->10;};}
 public static void cancel(ServerPlayer p){Action a=ACTIVE.remove(p.getUUID());if(a!=null)for(var fx:a.effects)fx.discard();}
 public static void clearAll(){for(var a:new ArrayList<>(ACTIVE.values()))cancel(a.owner);}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->cancel(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());}
 private static void released(String id){RELEASES.merge(id,1,Integer::sum);}
 public static EntityGomuArmEntity fx(ServerPlayer p,String profile,Vec3 point,int life){return GoroTechniques.visual(p,profile,point,life);}
 private static EntityGomuArmEntity fx(Action a,String profile,Vec3 point,int life){var e=fx(a.owner,profile,point,life);a.effects.add(e);return e;}
 public static void tick(){for(Action a:new ArrayList<>(ACTIVE.values())){
  if(!BatchRules.usable(a.owner,a.world,a.fruit.getItem())){cancel(a.owner);continue;}
  int w=windup(a.move.id()),t=++a.age-w;String id=a.move.id();
  if(t<0){for(var f:a.effects)if(!f.isRemoved())f.setPos(a.owner.getEyePosition().add(a.owner.getLookAngle().scale(1.2)).add(.3,-.3,0));continue;}
  if(t==0){for(var f:a.effects)f.discard();a.effects.clear();a.start=a.owner.position();a.direction=a.owner.getLookAngle();released(id);if(!Set.of("passive","yatanokagami").contains(id))TechniqueAnimations.playNamed(a.owner,"b01_"+id+"_release");release(a);}
  if(id.equals("magatama") && t%4==0 && t<4*br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.magatamaPerVolley())projectile(a,a.owner.getLookAngle().yRot((float)Math.toRadians((t/4-(br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.magatamaPerVolley()-1)/2.)*7)),"magatama",LightBeams.magatamaRange(),.7F);
  if(id.equals("iceage") && t%4==0 && t<=28){double r=2+14*t/28.;a.terrain.freezeWater(a.start,Math.min(8,r));area(a,a.start,r,false,true);fx(a,"b01_ice_wave",a.start,5).setStretch((float)r);}
  if(id.equals("grounddeath") && t%4==0 && t<=28){double r=2+14*t/28.;area(a,a.start,r,false,true);a.terrain.replaceSurface(a.start,Math.min(12,r),net.minecraft.world.level.block.Blocks.SANDSTONE.defaultBlockState());fx(a,"b01_sand_wave",a.start,5).setStretch((float)r);}
  if(id.equals("icespikes") && t%3==0 && t<=15){double d=1+t/3.;Vec3 pos=a.start.add(flat(a.direction).scale(d));if(!LightBeams.blocked(a.world,a.start.add(0,1,0),flat(a.direction),d)){
   fx(a,"b01_ice_spike",pos,30).setStretch(2.5F);area(a,pos,1.4,false,true);a.terrain.replaceSurface(pos,0,net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState());
  }}
  if(Set.of("whitelauncher","barjan","kick","speedispower").contains(id)){
   if(t<6){blink(a.owner,flat(a.direction),.5);fx(a,id.equals("barjan")?"b01_sand_dash":id.equals("whitelauncher")?"b01_smoke_dash":"b01_light_dash",a.owner.position().add(0,.7,0),5);}
   if(t==6){area(a,a.owner.position(),id.equals("barjan")?8:Math.max(4,a.move.range()),true,true);fx(a,id.equals("barjan")?"b01_sand_slash":id.equals("whitelauncher")?"b01_smoke_finish":"b01_light_kick",a.owner.getEyePosition().add(flat(a.direction).scale(1.2)),12);a.terrain.impact(a.owner.position().add(0,.3,0).add(flat(a.direction).scale(1.2)));}
  }
  if(id.equals("whiteout") && t>0 && t<24){for(var target:a.world.getEntitiesOfClass(LivingEntity.class,a.owner.getBoundingBox().inflate(32))){if(!BatchRules.target(a.owner,target) || target.position().distanceTo(a.start)>32 || target.position().distanceTo(a.start)<2)continue;Vec3 toward=a.start.subtract(target.position()).normalize().scale(.18);Vec3 velocity=new Vec3(toward.x,target.getDeltaMovement().y,toward.z);if(SafeDestination.isSafe(a.world,target,target.position().add(velocity))){target.setDeltaMovement(velocity);target.syncVelocity=true;}}}
  int end=switch(id){case "iceage","grounddeath"->36;case "icespikes"->42;case "magatama"->4*br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.magatamaPerVolley()+12;case "blackhole","kurouzu"->40;case "whiteout"->32;default->18;};
  if(t>=end)cancel(a.owner);
 }}
 public static double blink(ServerPlayer p,Vec3 direction,double distance){Vec3 start=p.position(),last=start;ServerLevel l=(ServerLevel)p.level();if(direction.lengthSqr()<.00001)return 0;for(double d=.25;d<=Math.min(48,distance);d+=.25){Vec3 next=start.add(direction.normalize().scale(d));if(!SafeDestination.isSafe(l,p,next))break;last=next;}p.teleportTo(last.x,last.y,last.z);p.setDeltaMovement(Vec3.ZERO);p.syncVelocity=true;return last.distanceTo(start);}
 private static Vec3 flat(Vec3 v){Vec3 f=new Vec3(v.x,0,v.z);return f.lengthSqr()<.0001?new Vec3(0,0,1):f.normalize();}
 private static void release(Action a){var p=a.owner;String id=a.move.id();switch(id){
  case "icepartisan","pheasantbeak","whiteblow","whitevine","desertspada","desertlaspada","darkball","darkmatter"->projectile(a,p.getLookAngle(),id,32,id.equals("darkmatter")?3:id.equals("darkball")?1.1F:1.3F);
  case "amanomurakumo"->fx(a,"b01_light_sword",p.getEyePosition().add(p.getLookAngle()),16);
  case "laser1"->LightBeams.fireRay(p,a.fruit,a.move,LightBeams.laserRange()).batchOrigin(p).terrain(a.terrain);
  case "iceball"->{a.terrain.freezePath(p.position(),a.direction,a.move.range());area(a,p.position(),a.move.range(),true,true);fx(a,"b01_ice_ball",p.getEyePosition().add(p.getLookAngle().scale(2)),14).setStretch(2);}
  case "yatanokagami"->{Vec3 from=p.getEyePosition();blink(p,p.getLookAngle(),br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.travelMaxDistanceBlocks());GoroTechniques.link(p,from,p.getEyePosition(),"b01_light_dash",10);LightBuffs.apply(p,net.minecraft.world.effect.MobEffects.SPEED,40,2);}
  case "passive"->{Vec3 from=p.getEyePosition();blink(p,p.getLookAngle().scale(p.isShiftKeyDown()?-1:1),8);GoroTechniques.link(p,from,p.getEyePosition(),"b01_light_dash",8);}
  case "whiteout"->{area(a,p.position(),32,false,true);a.terrain.impact(p.position().add(0,.5,0));SmokeCloud.create(p);fx(a,"b01_smoke_wave",p.position(),32).setStretch(32);}
  case "sables"->{area(a,p.position(),6,false,true);a.terrain.impact(p.position().add(0,.5,0));SandStorm.create(p);}
  case "kurouzu","blackhole"->{YamiDarkness.startPull(p,id.equals("kurouzu")?10:24);if(id.equals("blackhole"))YamiDarkness.startAbsorbing(p);area(a,p.position(),id.equals("kurouzu")?10:24,id.equals("kurouzu"),true);fx(a,id.equals("kurouzu")?"b01_dark_grab":"b01_dark_field",p.position(),40).setStretch(id.equals("kurouzu")?3:12);YamiDarkness.playDarknessSound(p);a.terrain.impact(p.position().add(0,.5,0));}
  default->{}
 }}
 public static SkillProjectileEntity projectile(Action a,Vec3 direction,String id,double range,float scale){
  String[] visual=DevilFruitRegistry.projectileVisual(id);if(visual==null)visual=DevilFruitElement.projectileVisualFallback(DevilFruitElement.of(a.fruit));
  var shot=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,a.owner.getEyePosition().x,a.owner.getEyePosition().y,a.owner.getEyePosition().z,direction,a.world);
  shot.setOwner(a.owner);shot.batchOrigin(a.owner).terrain(a.terrain).visualProfile(id).damage(a.move.damage()).knockback(a.move.knockback()).element(DevilFruitElement.forTechnique(a.fruit,id)).lifetime(Math.max(20,a.move.lifetime())).range(range).model(visual[0]).texture(visual[1]).scale(scale).trail(net.minecraft.core.particles.ParticleTypes.SMOKE);
  if(id.equals("desertspada") || id.equals("desertlaspada"))shot.animation("vertical");if(id.equals("whitevine"))shot.animation("idle");
  shot.shoot(direction.x,direction.y,direction.z,(float)a.move.speed(),0);a.world.addFreshEntity(shot);return shot;
 }
 private static void area(Action a,Vec3 center,double radius,boolean cone,boolean apply){
  radius=Math.min(32,Math.max(1,radius));var p=a.owner;var source=CombatSkillLogic.elementalDamageSource(a.world,DevilFruitElement.forTechnique(a.fruit,a.move.id()),p,null);
  for(LivingEntity t:a.world.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius))){
   if(!BatchRules.target(p,t) || a.hit.contains(t.getUUID()) || t.position().distanceTo(center)>radius)continue;
   Vec3 toward=t.getBoundingBox().getCenter().subtract(p.getEyePosition()).normalize();if(cone && toward.dot(a.direction)<CombatSkillLogic.MELEE_CONE_COS)continue;
   double damage=a.move.damage();
   if(CombatSkillLogic.hurtAndConfirm(t,a.world,source,(float)damage)){a.hit.add(t.getUUID());onConfirmed(p,t,a.fruit.getItem(),a.move.id());CombatSkillLogic.applySkillKnockback(t,t.position().subtract(center),source,a.move.knockback());}
  }
 }
 public static void onConfirmed(ServerPlayer p,LivingEntity t,Item fruit,String id){
  if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU && id.equals("whitevine"))SmokeCloud.createAt(p,t.position());
  if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE){FreezeService.apply(t,60,p);FlameBurn.extinguish(t);MagmaBurn.extinguish(t);}
  if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA)Desiccation.apply(t,p);
  if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI)YamiSeal.apply(t,0);
 }
}
