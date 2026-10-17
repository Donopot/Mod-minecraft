package net.mcreator.minepiece.procedures;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;

/** Five dedicated magma techniques; bounded local-port adaptations are documented in MAGU_MAGU.md. */
public final class MaguTechniques {
   private static final class Action {
      final ServerPlayer owner;final DevilFruitMove move;EntityGomuArmEntity fx;final Vec3 aim;final TechniqueTerrain.Budget terrain;
      int age;boolean released;
      Action(ServerPlayer p,DevilFruitMove m) {
         owner=p;move=m;aim=aim(p);terrain=TechniqueTerrain.begin(p,m);fx=MeraTechniques.visual(p,initialVisual(m.id()),p.position(),130);
      }
   }
   private static final Map<UUID,Action> ACTIVE=new HashMap<>();
   private MaguTechniques() { }
   private static String initialVisual(String id) {return switch(id) {
      case "daifunka"->"magu_fist_charge";case "inugamiguren"->"magu_hound_charge";case "kageromagumagu"->"magu_dash";case "volcanicexplosion"->"magu_volcanic";default->"magu_meteor_charge";
   };}
   public static String animationFor(ServerPlayer player,String id) {
      Action a=ACTIVE.get(player.getUUID());boolean charge=a!=null && a.move.id().equals(id) && charging(player);
      return switch(id) {
         case "daifunka" -> charge?"magu_fist_charge":"magu_fist_release";
         case "inugamiguren" -> charge?"magu_hound_charge":"magu_hound_release";
         case "kageromagumagu" -> a!=null && a.age>10?"magu_kagero_finish":"magu_kagero_dash";
         case "volcanicexplosion" -> "magu_volcanic_cast";
         case "ryuseikazan" -> "magu_meteors_cast";
         default -> "";
      };
   }
   public static boolean ownsVisual(String id) {return Set.of("daifunka","inugamiguren","ryuseikazan","kageromagumagu","volcanicexplosion","magu_charge","magu_eruption","magu_trail","magu_fist_charge","magu_hound_charge","magu_dash","magu_wake","magu_volcanic","magu_meteor_charge").contains(id);}
   public static boolean usable(ServerPlayer p) {return p.isAlive() && !p.isRemoved() && !p.isSpectator() && !DevilFruitPowerLogic.isSuppressed(p) && DevilFruitPowerLogic.getDevilFruit(p).is(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU);}
   public static boolean charging(ServerPlayer p) {var a=ACTIVE.get(p.getUUID());return a!=null && Set.of("daifunka","inugamiguren").contains(a.move.id());}
   public static void release(ServerPlayer p) {var a=ACTIVE.get(p.getUUID());if(a!=null)a.released=true;}
   public static boolean cast(ServerPlayer p,DevilFruitMove move) {
      if(ACTIVE.containsKey(p.getUUID()))return false;
      ACTIVE.put(p.getUUID(),new Action(p,move));return true;
   }
   private static void end(Action a) {
      a.fx.discard();ACTIVE.remove(a.owner.getUUID());
      if(a.move.id().equals("kageromagumagu")){a.owner.setDeltaMovement(Vec3.ZERO);a.owner.syncVelocity=true;}
   }
   public static void cancel(ServerPlayer p) {var a=ACTIVE.get(p.getUUID());if(a!=null){end(a);TechniqueAnimations.playNamed(p,"cancel");}}
   public static int activeCount() {return ACTIVE.size();}
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->tick());
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->cancel(handler.player));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->{for(Action a:new ArrayList<>(ACTIVE.values()))end(a);});
   }
   public static void tick() {
      for(Action a:new ArrayList<>(ACTIVE.values())) {
         if(!usable(a.owner) || a.owner.level()!=a.fx.level()){cancel(a.owner);continue;}
         a.age++;
         if(charging(a.owner)) {
            a.fx.setPos(a.owner.getEyePosition());
            a.fx.setYRot(a.owner.getYRot());a.fx.setXRot(a.owner.getXRot());
            if(a.age>=100 || a.released && a.age>=10){end(a);shoot(a);}
         } else if(a.move.id().equals("ryuseikazan")) {
            a.fx.setPos(a.owner.getEyePosition());
            if(a.age>=20 && a.age<40)meteor(a);
            if(a.age>=40)end(a);
         } else if(a.move.id().equals("kageromagumagu") && a.age<=10) {
            Vec3 delta=a.owner.getLookAngle().scale(1.2);
            if(!a.owner.level().noCollision(a.owner,a.owner.getBoundingBox().expandTowards(delta))) {a.age=11;a.owner.setDeltaMovement(Vec3.ZERO);a.owner.syncVelocity=true;}
            else {a.owner.setDeltaMovement(delta);a.owner.syncVelocity=true;}
            a.fx.setPos(a.owner.position());a.fx.setYRot(a.owner.getYRot());a.fx.setVisualProfile("magu_dash");
            a.terrain.impact(a.owner.position().add(0,.3,0),1.5,3);
         } else {
            int age=a.move.id().equals("kageromagumagu")?a.age-10:a.age;
            if(a.move.id().equals("kageromagumagu")){a.owner.setDeltaMovement(Vec3.ZERO);a.owner.syncVelocity=true;}
            if(a.move.id().equals("kageromagumagu") && !a.fx.getVisualProfile().equals("magu_wake")) {
               a.fx.discard();a.fx=MeraTechniques.visual(a.owner,"magu_wake",a.owner.position(),41);
               TechniqueAnimations.playNamed(a.owner,"magu_kagero_finish");
            }
            a.fx.setPos(a.owner.position());a.fx.setStretch(age<20?4:12);
            if(age==15) {
               strike(a.owner,a.owner.position(),4,8,a.move.damage(),.5);
               a.terrain.impact(a.owner.position().add(0,.5,0));
               MagmaPools.create((ServerLevel)a.owner.level(),a.owner.blockPosition().below(),a.owner,br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.poolRadius());
            }
            if(age>=20 && age<=30 && age%10==0)strike(a.owner,a.owner.position(),12,3,a.move.damage()*.75,1);
            if(age>=40)end(a);
         }
      }
   }
   private static void shoot(Action a) {
      TechniqueAnimations.play(a.owner,a.move);
      double charge=Math.clamp((a.age-10)/10.0,0,5);
      boolean dog=a.move.id().equals("inugamiguren");
      double damage=Math.min(a.move.damage(),18*(1+.1*(charge+(dog?2:0))));
      var shot=projectile(a.owner,damage,60,1.8,dog?"inugami_guren":"fist_small",dog?"inugami_guren":"tex_magma");
      shot.terrain(a.terrain).visualProfile(a.move.id()).scale((float)((dog?1:2.5)*(1+charge*.1))).explodeOnImpact(dog?2:2.5,MinepieceModParticles.MAGMA);
      a.owner.level().addFreshEntity(shot);
   }
   private static SkillProjectileEntity projectile(ServerPlayer owner,double damage,int lifetime,double speed,String model,String texture) {
      Vec3 eye=owner.getEyePosition(),look=owner.getLookAngle();
      var shot=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,eye.x,eye.y,eye.z,look,owner.level());
      shot.setOwner(owner);shot.damage(damage).knockback(1).lifetime(lifetime).element(ElementType.MAGMA).maguOrigin(owner).model(model).texture(texture).trail(MinepieceModParticles.MAGMA);
      shot.shoot(look.x,look.y,look.z,(float)speed,0);
      // Caller finishes profile/scale/trajectory BEFORE entity publication and its initial metadata packet.
      return shot;
   }
   private static Vec3 aim(ServerPlayer p) {
      Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(32));
      var hit=((ServerLevel)p.level()).clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
      Vec3 result=hit.getLocation();double nearest=32*32;
      for(LivingEntity target:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(32))) {
         Vec3 to=target.getBoundingBox().getCenter().subtract(eye);
         if(CombatSkillLogic.isValidTarget(p,target) && p.hasLineOfSight(target) && to.normalize().dot(p.getLookAngle())>.95 && to.lengthSqr()<nearest){nearest=to.lengthSqr();result=target.position();}
      }
      return result;
   }
   private static void meteor(Action a) {
      // A bounded twenty-shot barrage arcs upward before falling around the selected point.
      int i=a.age-20;double theta=i*2.39996,r=i%5==0?0:Math.sqrt(i/20.0)*3;
      Vec3 goal=a.aim.add(Math.cos(theta)*r,0,Math.sin(theta)*r);
      Vec3 start=a.owner.getEyePosition().add(0,.5,0);
      Vec3 delta=goal.subtract(start);double time=40.5;
      var shot=projectile(a.owner,40,140,1,"fist_small","tex_magma");
      shot.setPos(start);shot.setDeltaMovement(new Vec3(delta.x/time,2.6,delta.z/time));
      shot.terrain(a.terrain).visualProfile("ryuseikazan").magmaMeteor(true).scale(2).explodeOnImpact(4,MinepieceModParticles.MAGMA);
      a.owner.level().addFreshEntity(shot);
   }
   public static int strike(ServerPlayer owner,Vec3 center,double radius,double height,double damage,double kb) {
      ServerLevel level=(ServerLevel)owner.level();int hits=0;
      var source=CombatSkillLogic.elementalDamageSource(level,ElementType.MAGMA,owner,null);
      for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(center.x-radius,center.y,center.z-radius,center.x+radius,center.y+height,center.z+radius))) {
         double dx=target.getX()-center.x,dz=target.getZ()-center.z;
         if(dx*dx+dz*dz>radius*radius || !CombatSkillLogic.isValidTarget(owner,target) || DevilFruitExplosions.isProtectedPosition(level,target.blockPosition()))continue;
         if(level.clip(new ClipContext(center.add(0,.8,0),target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner)).getType()!=HitResult.Type.MISS)continue;
         if(CombatSkillLogic.hurtAndConfirm(target,level,source,(float)damage)) {
            MagmaBurn.ignite(target,br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.magmaBurnTicks(),owner);
            CombatSkillLogic.applySkillKnockback(target,target.position().subtract(center),source,kb);hits++;
         }
      }
      return hits;
   }
   public static void blast(ServerPlayer owner,ServerLevel level,Vec3 center,double radius,double damage,double kb) {
      blast(owner,level,center,radius,damage,kb,true);
   }
   public static void blast(ServerPlayer owner,ServerLevel level,Vec3 center,double radius,double damage,double kb,boolean pool) {
      if(owner.level()!=level)return;
      // Ground-relative cylinder avoids a near-floor impact excluding standing targets.
      strike(owner,center.add(0,-.5,0),radius,Math.max(3,radius*2),damage,kb);
      if(pool)MagmaPools.create(level,net.minecraft.core.BlockPos.containing(center),owner,br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.poolRadius());
      level.sendParticles(MinepieceModParticles.MAGMA,center.x,center.y,center.z,12,.6,.3,.6,.03);
      level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,center.x,center.y,center.z,10,.6,.4,.6,.02);
   }
}
