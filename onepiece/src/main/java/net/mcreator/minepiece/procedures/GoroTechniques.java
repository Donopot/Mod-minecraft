package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.potion.MamaraganMobEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Seven electric routes. Bounded adaptations and ver15 differences live in GORO_GORO.md. */
public final class GoroTechniques {
   private static final Set<String> CHARGED=Set.of("vari","elthor","sango","karikrash");
   private static final class Action {
      final ServerPlayer owner;final ServerLevel level;final DevilFruitMove move;final TechniqueTerrain.Budget terrain;
      EntityGomuArmEntity fx;Vec3 point;int age,phase;boolean released,firing;
      Action(ServerPlayer p,DevilFruitMove m) {
         owner=p;level=(ServerLevel)p.level();move=m;terrain=TechniqueTerrain.begin(p,m);
         point=aim(p,m.id().equals("raigo")?24:Math.max(3,m.range()));
         fx=visual(p,m.id().equals("mamaragan")?"goro_cloud":m.id().equals("raigo")?"goro_raigo_charge":"goro_charge",p.getEyePosition(),m.id().equals("mamaragan")?65:105);
      }
   }
   private static final Map<UUID,Action> ACTIVE=new HashMap<>();
   private GoroTechniques() {}
   public static boolean usable(ServerPlayer p) {
      return p.isAlive() && !p.isRemoved() && !p.isSpectator() && !DevilFruitPowerLogic.isSuppressed(p)
         && DevilFruitPowerLogic.getDevilFruit(p).is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO);
   }
   public static boolean charging(ServerPlayer p) {
      Action a=ACTIVE.get(p.getUUID());return a!=null && CHARGED.contains(a.move.id()) && !a.firing;
   }
   public static int activeCount() {return ACTIVE.size();}
   public static void release(ServerPlayer p) {Action a=ACTIVE.get(p.getUUID());if(a!=null)a.released=true;}
   public static String animationFor(ServerPlayer p,String id) {
      if(!DevilFruitPowerLogic.getDevilFruit(p).is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO))return "";
      return switch(id) {
         case "vari","elthor","sango","karikrash" -> "goro_"+id+(charging(p)?"_charge":"_release");
         case "mamaragan" -> "goro_storm_cast";case "passive" -> "goro_blink";case "raigo" -> "goro_raigo_cast";
         default -> "";
      };
   }
   public static boolean ownsVisual(String id) {return id.startsWith("goro_");}
   public static boolean cast(ServerPlayer p,DevilFruitMove move) {
      if(!usable(p) || ACTIVE.containsKey(p.getUUID()))return false;
      if(move.id().equals("passive")) {
         // Source has a blink, not a speed potion. Shift selects the backward variant.
         blink(p,p.getLookAngle().scale(p.isShiftKeyDown()?-1:1),32);return true;
      }
      Action a=new Action(p,move);ACTIVE.put(p.getUUID(),a);
      if(move.id().equals("mamaragan"))p.addEffect(new MobEffectInstance(MinepieceModMobEffects.MAMARAGAN,60));
      return true;
   }
   private static void end(Action a) {a.fx.discard();ACTIVE.remove(a.owner.getUUID());}
   public static void cancel(ServerPlayer p) {
      Action a=ACTIVE.get(p.getUUID());if(a!=null) {end(a);TechniqueAnimations.playNamed(p,"cancel");}
      p.removeEffect(MinepieceModMobEffects.MAMARAGAN);MamaraganMobEffect.clearState(p.getUUID());
   }
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server->{tick();MamaraganMobEffect.prune();});
      ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->cancel(handler.player));
      ServerLifecycleEvents.SERVER_STOPPED.register(server->{for(Action a:new ArrayList<>(ACTIVE.values()))cancel(a.owner);MamaraganMobEffect.clearAll();});
   }
   public static void tick() {
      for(Action a:new ArrayList<>(ACTIVE.values())) {
         if(a.owner.level()!=a.level || !usable(a.owner)) {cancel(a.owner);continue;}
         a.age++;
         String id=a.move.id();
         if(id.equals("mamaragan")) {
            a.fx.setPos(a.owner.position().add(0,7,0));
            if(!a.owner.hasEffect(MinepieceModMobEffects.MAMARAGAN) || a.age>=60)cancel(a.owner);
            continue;
         }
         if(id.equals("raigo")) {
            Vec3 high=ceilingLimited(a.owner,a.owner.position().add(0,8,0));
            a.fx.setPos(high);a.fx.setStretch(.3F+Math.min(1,a.age/20F)*1.7F);
            if(a.age==20){a.point=aim(a.owner,24);launchRaigo(a,high);end(a);}continue;
         }
         if(!a.firing) {
            a.fx.setPos(a.owner.getEyePosition());a.fx.setYRot(a.owner.getYRot());a.fx.setXRot(a.owner.getXRot());
            if(a.age>=100 || a.released && a.age>=10) {
               a.firing=true;a.phase=0;a.fx.discard();
               a.point=aim(a.owner,Math.max(3,a.move.range()));
               TechniqueAnimations.playNamed(a.owner,"goro_"+id+"_release");
               if(id.equals("vari")) {blink(a.owner,a.owner.getLookAngle(),24);a.point=a.owner.position();}
               if(id.equals("sango"))ChainLightning.cast(a.owner,a.move,a.terrain);
               if(id.equals("elthor")){a.fx=visual(a.owner,"goro_warning",a.point,11);a.fx.setStretch(4);}
               if(id.equals("karikrash"))a.point=front(a.owner,3);
            }
            continue;
         }
         a.phase++;
         if(id.equals("elthor") && a.phase==10) {
            a.fx.discard();column(a.owner,a.point,12);blast(a.owner,a.level,a.point,4,a.move.damage(),a.move.knockback(),true);a.terrain.impact(terrainContact(a.owner,a.point));
         }
         if(id.equals("vari") && a.phase==5) {blast(a.owner,a.level,a.point,12,a.move.damage(),a.move.knockback(),true);a.terrain.impact(a.point.add(0,.4,0));visual(a.owner,"goro_impact",a.point,12).setStretch(12);}
         if(id.equals("karikrash") && a.phase==4) {link(a.owner,a.owner.getEyePosition(),a.point.add(0,1,0),"goro_arc",10);blast(a.owner,a.level,a.point,6,a.move.damage(),a.move.knockback(),true);a.terrain.impact(terrainContact(a.owner,a.point));visual(a.owner,"goro_impact",a.point,12).setStretch(6);}
         if(a.phase>=18)end(a);
      }
   }
   public static EntityGomuArmEntity visual(ServerPlayer p,String profile,Vec3 point,int life) {
      var fx=new EntityGomuArmEntity(MinepieceModEntities.ENTITY_GOMU_ARM,p.level());
      fx.setOwnerEntity(p);fx.followOwner(false);fx.lifetime(life);fx.setModel("fist_small");fx.setTexture("clear");
      fx.setVisualProfile(profile);fx.setPos(point);fx.setYRot(p.getYRot());fx.setXRot(p.getXRot());
      p.level().addFreshEntity(fx);return fx;
   }
   public static void link(ServerPlayer p,Vec3 start,Vec3 end,String profile,int life) {
      Vec3 delta=end.subtract(start);if(delta.lengthSqr()<.001)return;
      var fx=visual(p,profile,start,life);fx.setStretch((float)delta.length());
      fx.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
      fx.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));
   }
   public static void column(ServerPlayer p,Vec3 point,double height) {
      Vec3 top=point.add(0,height,0);
      var hit=((ServerLevel)p.level()).clip(new ClipContext(point.add(0,.2,0),top,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
      if(hit.getType()!=HitResult.Type.MISS)top=hit.getLocation();
      link(p,top,point,"goro_arc",10);visual(p,"goro_impact",point,10).setStretch(2);
   }
   public static void blast(ServerPlayer p,ServerLevel level,Vec3 point,double radius,double damage,double knockback) {
      blast(p,level,point,radius,damage,knockback,false);
   }
   private static void blast(ServerPlayer p,ServerLevel level,Vec3 point,double radius,double damage,double knockback,boolean slow) {
      // Reflected Raigo belongs to its deflector, who need not possess Goro. Origin is checked by the projectile.
      if(p.level()!=level || !p.isAlive() || p.isRemoved() || p.isSpectator() || DevilFruitPowerLogic.isSuppressed(p) || DevilFruitExplosions.isProtectedPosition(level,BlockPos.containing(point)))return;
      var source=CombatSkillLogic.elementalDamageSource(level,ElementType.LIGHTNING,p,null);
      for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(radius))) {
         if(!CombatSkillLogic.isValidTarget(p,target) || target.position().distanceTo(point)>radius
               || DevilFruitExplosions.isProtectedPosition(level,target.blockPosition()))continue;
         if(level.clip(new ClipContext(point.add(0,1,0),target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)continue;
         if(CombatSkillLogic.hurtAndConfirm(target,level,source,(float)damage)) {
            CombatSkillLogic.applyElementalEffect(target,ElementType.LIGHTNING,p);
            if(slow)target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS,20,1));
            CombatSkillLogic.applySkillKnockback(target,target.position().subtract(point),source,knockback);
         }
      }
      level.playSound(null,BlockPos.containing(point),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,net.minecraft.sounds.SoundSource.PLAYERS,.5F,1.2F);
   }
   public static double blink(ServerPlayer p,Vec3 direction,double distance) {
      Vec3 start=p.position(),last=start,unit=direction.normalize();ServerLevel level=(ServerLevel)p.level();
      for(double d=.25;d<=Math.min(32,distance);d+=.25) {
         Vec3 next=start.add(unit.scale(d));AABB box=p.getBoundingBox().move(next.subtract(start));
         if(GoroAmaru.scaled(p) && !GoroAmaru.safeVolume(level,box))break;
         boolean loaded=true;
         for(double x:new double[]{box.minX,box.maxX})for(double z:new double[]{box.minZ,box.maxZ})
            loaded &= level.hasChunkAt(BlockPos.containing(x,next.y,z));
         if(!loaded || !level.getWorldBorder().isWithinBounds(BlockPos.containing(next)) || next.y<level.getMinY()+1 || next.y+2>=level.getMaxY()
               || DevilFruitExplosions.isProtectedPosition(level,BlockPos.containing(next)) || !level.noCollision(p,box)
               || !level.getFluidState(BlockPos.containing(next)).isEmpty())break;
         last=next;
      }
      p.teleportTo(last.x,last.y,last.z);p.setDeltaMovement(Vec3.ZERO);p.syncVelocity=true;
      link(p,start.add(0,1,0),last.add(0,1,0),"goro_blink_trail",8);
      return last.distanceTo(start);
   }
   private static Vec3 terrainContact(ServerPlayer p,Vec3 target){Vec3 from=p.getEyePosition(),to=target.add(0,.8,0);var hit=((ServerLevel)p.level()).clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));return hit.getType()==HitResult.Type.BLOCK?hit.getLocation().subtract(to.subtract(from).normalize().scale(.02)):target.add(0,.4,0);}
   private static Vec3 front(ServerPlayer p,double distance) {
      Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(distance));
      var hit=((ServerLevel)p.level()).clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
      Vec3 point=hit.getLocation().subtract(p.getLookAngle().scale(.05));return point.add(0,-1,0);
   }
   private static Vec3 ceilingLimited(ServerPlayer p,Vec3 point) {
      var hit=((ServerLevel)p.level()).clip(new ClipContext(p.getEyePosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
      return hit.getLocation().subtract(0,.1,0);
   }
   private static Vec3 aim(ServerPlayer p,double range) {
      Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
      Vec3 result=front(p,range);double nearest=range*range;
      for(LivingEntity target:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range))) {
         Vec3 to=target.getBoundingBox().getCenter().subtract(eye);
         if(CombatSkillLogic.isValidTarget(p,target) && p.hasLineOfSight(target) && to.normalize().dot(look)>.94 && to.lengthSqr()<nearest) {
            nearest=to.lengthSqr();result=target.position();
         }
      }
      return result;
   }
   private static void launchRaigo(Action a,Vec3 start) {
      Vec3 direction=a.point.add(0,.8,0).subtract(start).normalize();
      var shot=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,start.x,start.y,start.z,direction,a.level);
      shot.setOwner(a.owner);shot.goroOrigin(a.owner).terrain(a.terrain).damage(a.move.damage()).knockback(a.move.knockback()).element(ElementType.LIGHTNING)
         .model("ball_maximum").texture("clear").visualProfile("goro_raigo").scale(1).lifetime(80).range(48)
         .trail(MinepieceModParticles.THUNDER_LIGHT_BLUE).explodeOnImpact(4,MinepieceModParticles.THUNDER_LIGHT_BLUE);
      shot.shoot(direction.x,direction.y,direction.z,1,0);a.level.addFreshEntity(shot);
   }
}

