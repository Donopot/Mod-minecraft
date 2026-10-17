package net.mcreator.minepiece.procedures;

import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.init.MinepieceModEntities;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Mera's seven explicit routes. Visuals share their attack's measured geometry. */
public final class MeraTechniques {
   private MeraTechniques() { }
   private static final class Charge {
      final ServerPlayer owner; final DevilFruitMove move;
      final EntityGomuArmEntity fx;
      int age; boolean released;
      Charge(ServerPlayer p,DevilFruitMove m) {
         owner=p; move=m;
         fx=visual(p,m.id().equals("daienkai")?"mera_charge_ball":"mera_charge",p.position(),110);
      }
   }
   private static final class Action {
      final ServerPlayer owner; final DevilFruitMove move; final Vec3 center;
      final java.util.Set<java.util.UUID> hit=new java.util.HashSet<>();
      final EntityGomuArmEntity fx; final TechniqueTerrain.Budget terrain; int age;
      Action(ServerPlayer p,DevilFruitMove m,Vec3 c) {
         owner=p;move=m;center=c;terrain=TechniqueTerrain.begin(p,m);
         fx=visual(p,m.id().equals("hotarubi")?"mera_fireflies":m.id().equals("kagero")?"mera_dash":m.id(),c,41);
      }
   }
   private static final java.util.Map<java.util.UUID,Charge> CHARGES=new java.util.HashMap<>();
   private static final java.util.List<Action> ACTIONS=new java.util.ArrayList<>();
   public static boolean ownsVisual(String id) {
      return java.util.Set.of("hiken","higan","hibashira","kagero","hotarubi","kaenryuo","daienkai",
         "mera_charge","mera_charge_ball","mera_fireflies","mera_dash").contains(id);
   }
   public static boolean usable(ServerPlayer p) {
      return p.isAlive() && !p.isRemoved() && !p.isSpectator() && !DevilFruitPowerLogic.isSuppressed(p)
         && DevilFruitPowerLogic.getDevilFruit(p).is(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);
   }
   public static boolean isDashing(ServerPlayer p) {return ACTIONS.stream().anyMatch(action->action.owner==p && action.move.id().equals("kagero"));}
   public static boolean charging(ServerPlayer p) { return CHARGES.containsKey(p.getUUID()); }
   public static void release(ServerPlayer p) {
      Charge charge=CHARGES.get(p.getUUID()); if(charge!=null) charge.released=true;
   }
   private static void endAction(Action action) {
      if(action.move.id().equals("kagero")) {action.owner.setDeltaMovement(Vec3.ZERO);action.owner.syncVelocity=true;}
      action.fx.discard();ACTIONS.remove(action);
   }
   public static void cancel(ServerPlayer p) {
      Charge charge=CHARGES.remove(p.getUUID()); if(charge!=null) charge.fx.discard();
      for(Action action:new java.util.ArrayList<>(ACTIONS)) if(action.owner.getUUID().equals(p.getUUID())) endAction(action);
   }
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->tick());
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->cancel(handler.player));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->{
         for(Charge charge:CHARGES.values()) charge.fx.discard(); CHARGES.clear();
         for(Action action:ACTIONS) action.fx.discard(); ACTIONS.clear();
      });
   }
   public static void tick() {
      for(Charge charge:new java.util.ArrayList<>(CHARGES.values())) {
         if(!usable(charge.owner) || charge.fx.level()!=charge.owner.level()) {cancel(charge.owner);continue;}
         charge.age++;
         charge.fx.setPos(charge.owner.position().add(0,charge.move.id().equals("daienkai")?6:1,0));
         charge.fx.setStretch((float)Math.min(1,charge.age/40.0));
         int minimum=charge.move.id().equals("daienkai")?20:charge.move.id().equals("higan")?1:10;
         if(charge.age>=100 || charge.released && charge.age>=minimum) {
            CHARGES.remove(charge.owner.getUUID()); charge.fx.discard();
            launch(charge.owner,charge.move,charge.age);
         }
      }
      for(Action action:new java.util.ArrayList<>(ACTIONS)) {
         if(!usable(action.owner) || action.fx.level()!=action.owner.level()) {endAction(action);continue;}
         ServerLevel level=(ServerLevel)action.owner.level();
         if(action.move.id().equals("hibashira")) {
            double radius=action.age<20?4:12;
            action.fx.setStretch((float)(action.age<10?4:radius));
            if(action.age>=10 && action.age<35 && action.age%10==0)
               strikeVolume(action.owner,action.center,radius,action.age<20?8:3,action.age<20?24:18,action.move.knockback());
         } else if(action.move.id().equals("kaenryuo")) {
            action.fx.setPos(action.owner.getEyePosition());
            action.fx.setYRot(action.owner.getYRot());action.fx.setXRot(action.owner.getXRot());
            action.fx.setStretch(6);
            if(action.age==0) {
               for(LivingEntity target:CombatSkillLogic.strikeTargets(action.owner,new net.minecraft.world.item.ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA),action.move,0))
                  CombatSkillLogic.applyElementalEffect(target,ElementType.FIRE,action.owner);
            }
            if(action.age>=10 && action.age<=30 && action.age%10==0) strikeVolume(action.owner,action.owner.position(),6,4,26+action.age*.01,action.age>15?2:.1);
         } else if(action.move.id().equals("hotarubi")) {
            double radius=4+action.age*.15;
            action.fx.setStretch((float)radius);
            strikeVolume(action.owner,action.center,radius,4,action.move.damage()+action.age*.05,.1);
         } else {
            Vec3 look=action.owner.getLookAngle();
            Vec3 delta=look.scale(1.8);
            if(!level.noCollision(action.owner,action.owner.getBoundingBox().expandTowards(delta))) {
               action.owner.setDeltaMovement(Vec3.ZERO);action.owner.syncVelocity=true;
               action.fx.discard();ACTIONS.remove(action);continue;
            }
            action.owner.setDeltaMovement(delta);action.owner.syncVelocity=true;
            action.fx.setPos(action.owner.position());action.fx.setYRot(action.owner.getYRot());
            for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,action.owner.getBoundingBox().expandTowards(delta).inflate(1))) {
               if(action.hit.contains(target.getUUID()) || !CombatSkillLogic.isValidTarget(action.owner,target)
                     || !action.owner.hasLineOfSight(target)) continue;
               var source=CombatSkillLogic.elementalDamageSource(level,ElementType.FIRE,action.owner,null);
               if(CombatSkillLogic.hurtAndConfirm(target,level,source,15)) {
                  CombatSkillLogic.applyElementalEffect(target,ElementType.FIRE,action.owner);action.hit.add(target.getUUID());
               }
            }
         }
         if(action.move.id().equals("kagero"))action.terrain.impact(action.owner.position().add(0,.3,0),1,2);
         if(action.move.id().equals("hotarubi") && action.age%5==0)action.terrain.impact(action.center.add(0,.3,0),2.5,12);
         if(action.move.id().equals("hibashira") && action.age==10)action.terrain.impact(action.center.add(0,.5,0));
         if(action.move.id().equals("kaenryuo") && action.age==20)action.terrain.impact(action.owner.position().add(0,.5,0));
         if(++action.age>=(action.move.id().equals("hotarubi")?15:action.move.id().equals("kagero")?10:40)) {
            if(action.move.id().equals("kagero")) {action.owner.setDeltaMovement(Vec3.ZERO);action.owner.syncVelocity=true;}
            action.fx.discard();ACTIONS.remove(action);
         }
      }
   }
   private static void launch(ServerPlayer player,DevilFruitMove move,int age) {
      if(move.id().equals("hotarubi")) {
         TechniqueAnimations.play(player,move);
         Vec3 eye=player.getEyePosition(),end=eye.add(player.getLookAngle().scale(30));
         var hit=((ServerLevel)player.level()).clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
         Vec3 seed=hit.getLocation();
         LivingEntity nearest=null;double distance=Double.MAX_VALUE;
         for(LivingEntity target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(30))) {
            Vec3 to=target.getBoundingBox().getCenter().subtract(eye);
            if(CombatSkillLogic.isValidTarget(player,target) && player.hasLineOfSight(target)
                  && to.normalize().dot(player.getLookAngle())>.95 && to.lengthSqr()<distance) {nearest=target;distance=to.lengthSqr();}
         }
         if(nearest!=null) seed=nearest.position();
         ACTIONS.add(new Action(player,move,new Vec3(seed.x,seed.y,seed.z)));return;
      }
      TechniqueAnimations.play(player,move);
      TechniqueTerrain.Budget terrain=TechniqueTerrain.begin(player,move);
      int shots=move.id().equals("higan") && age>=10?4:1;
      for(int i=0;i<shots;i++) {
         String model=move.id().equals("daienkai")?"ball_maximum":"fist_small";
         var projectile=CombatSkillLogic.fireProjectile(player,move.damage(),move.knockback(),null,0,0,
            move.id().equals("hiken")?20:move.id().equals("higan")?15:60,move.id().equals("daienkai")?3:8,model,"flame_tiger",MinepieceModParticles.FLAME_SMALL);
         projectile.visualProfile(move.id()).meraOrigin(player).terrain(terrain);
         double charge=Math.clamp((age-10)/10.0,0,5);
         if(move.id().equals("hiken")) projectile.damage(Math.min(move.damage(),18*(1+.1*charge))).scale((float)(1+.1*charge)).knockback(1);
         if(move.id().equals("daienkai")) projectile.damage(Math.min(move.damage(),35*(1+.1*charge))).scale((float)(1+.1*charge)).knockback(1);
         if(move.id().equals("higan")) projectile.knockback(.65);
         if(move.id().equals("higan")) projectile.explodeOnImpact(4,MinepieceModParticles.FLAME_SMALL);
         if(shots>1) {
            Vec3 look=player.getLookAngle();
            projectile.shoot(look.x,look.y,look.z,8,10);
         }
         if(move.id().equals("daienkai")) {
            Vec3 start=player.position().add(0,6,0),aim=player.getEyePosition().add(player.getLookAngle().scale(48));
            double nearest=48*48;
            for(LivingEntity target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(48))) {
               Vec3 to=target.getBoundingBox().getCenter().subtract(player.getEyePosition());
               if(CombatSkillLogic.isValidTarget(player,target) && player.hasLineOfSight(target)
                     && to.normalize().dot(player.getLookAngle())>.97 && to.lengthSqr()<nearest) {
                  aim=target.getBoundingBox().getCenter();nearest=to.lengthSqr();
               }
            }
            projectile.setPos(start);
            Vec3 direction=aim.subtract(start).normalize();projectile.shoot(direction.x,direction.y,direction.z,3,0);
            projectile.explodeOnImpact(move.range()/2*(1+.1*charge),MinepieceModParticles.FLAME);
         }
      }
   }

   public static boolean cast(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      switch (move.id()) {
         case "hiken", "higan", "hotarubi", "daienkai" -> {
            if(charging(player)) return false;
            CHARGES.put(player.getUUID(),new Charge(player,move));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Maintenir R pour charger, relacher pour lancer."),true);
            return true;
         }
         case "hibashira", "kagero", "kaenryuo" -> {
            if(move.id().equals("kagero")) FirePropulsion.stop(player,"technique");
            ACTIONS.add(new Action(player,move,player.position()));return true;
         }
         default -> { return false; }
      }
   }

   public static EntityGomuArmEntity visual(ServerPlayer owner, String id, Vec3 position, int lifetime) {
      var level = (ServerLevel) owner.level();
      var fx = new EntityGomuArmEntity(MinepieceModEntities.ENTITY_GOMU_ARM, level);
      fx.setOwnerEntity(owner); fx.followOwner(false); fx.lifetime(lifetime);
      fx.setVisualProfile(id); fx.setModel("fist_small"); fx.setTexture("flame_tiger");
      fx.setPos(position); fx.setYRot(owner.getYRot()); fx.setXRot(owner.getXRot());
      level.addFreshEntity(fx);
      return fx;
   }

   public static void blast(ServerPlayer owner,ServerLevel level,Vec3 center,double radius,double damage,double knockback) {
      if(owner.level()!=level) return;
      var source=CombatSkillLogic.elementalDamageSource(level,ElementType.FIRE,owner,null);
      for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(center,center).inflate(radius))) {
         if(!CombatSkillLogic.isValidTarget(owner,target) || target.getBoundingBox().getCenter().distanceTo(center)>radius
               || DevilFruitExplosions.isProtectedPosition(level,target.blockPosition())) continue;
         if(level.clip(new ClipContext(center,target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner)).getType()!=HitResult.Type.MISS) continue;
         if(CombatSkillLogic.hurtAndConfirm(target,level,source,(float)damage)) {
            CombatSkillLogic.applyElementalEffect(target,ElementType.FIRE,owner);
            CombatSkillLogic.applySkillKnockback(target,target.position().subtract(center),source,knockback);
         }
      }
      // Damage resolves against existing cover; terrain changes are resolved by the owning cast afterward.
      for(int i=0;i<64;i++) {
         double angle=i*Math.PI*2/64;
         level.sendParticles(MinepieceModParticles.FLAME_SMALL,center.x+Math.cos(angle)*radius,center.y,center.z+Math.sin(angle)*radius,2,.1,.5,.1,.02);
      }
   }

   /** Circular damage cylinder; the owning action resolves its separate terrain budget afterward. */
   public static int strikeVolume(ServerPlayer owner, Vec3 center, double radius, double height,
                                  double damage, double knockback) {
      ServerLevel level = (ServerLevel) owner.level();
      int hits = 0;
      Vec3 origin = center.add(0, .8, 0);
      var source = CombatSkillLogic.elementalDamageSource(level, ElementType.FIRE, owner, null);
      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
            new net.minecraft.world.phys.AABB(center.x-radius, center.y, center.z-radius,
               center.x+radius, center.y+height, center.z+radius))) {
         double dx = target.getX()-center.x, dz = target.getZ()-center.z;
         if (dx*dx+dz*dz > radius*radius || !CombatSkillLogic.isValidTarget(owner, target)
               || DevilFruitExplosions.isProtectedPosition(level,target.blockPosition())) continue;
         Vec3 aim = target.getBoundingBox().getCenter();
         if (level.clip(new ClipContext(origin, aim, ClipContext.Block.COLLIDER,
               ClipContext.Fluid.NONE, owner)).getType() != HitResult.Type.MISS) continue;
         if (CombatSkillLogic.hurtAndConfirm(target, level, source, (float)damage)) {
            CombatSkillLogic.applyElementalEffect(target, ElementType.FIRE, owner);
            CombatSkillLogic.applySkillKnockback(target, target.position().subtract(center), source, knockback);
            hits++;
         }
      }
      return hits;
   }
}
