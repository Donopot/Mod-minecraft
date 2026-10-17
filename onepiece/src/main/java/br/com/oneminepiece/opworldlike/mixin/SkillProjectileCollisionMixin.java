package br.com.oneminepiece.opworldlike.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Predicate;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.procedures.DevilFruitExplosions;
import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Adjust only our skills, after vanilla inertia and before its collision/movement calculation. */
@Mixin(AbstractHurtingProjectile.class)
public abstract class SkillProjectileCollisionMixin {
   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target =
      "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lnet/minecraft/world/phys/HitResult;"))
   private HitResult minepiece$skillTrace(Entity entity, Predicate<Entity> predicate, ClipContext.Block clip,
                                         Operation<HitResult> original) {
      if (!(entity instanceof SkillProjectileEntity skill)) return original.call(entity, predicate, clip);
      skill.clipMovementToRange();
      var start = skill.position();
      var movement = skill.getDeltaMovement();
      var end = start.add(movement);
      if (skill.isPhasing() && skill.level() instanceof ServerLevel level && DevilFruitHollowConfig.ghostsStopAtClaims()) {
         int steps = Math.max(1, (int) Math.ceil(movement.length() * 4));
         for (int i = 0; i <= steps; i++) {
            if (DevilFruitExplosions.isProtectedPosition(level, BlockPos.containing(start.lerp(end, i / (double) steps)))) {
               skill.discard(); skill.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
               return BlockHitResult.miss(start, Direction.UP, BlockPos.containing(start));
            }
         }
      }
      HitResult regular=skill.isPhasing()?null:original.call(entity,predicate,clip);
      // 26.3's margin grows between ticks. A point just outside last tick's box can
      // start inside this tick's expanded box; AABB.clip then returns no entry face.
      // Preserve the native predicate/margin and cover, and resolve this contact at start.
      double margin=ProjectileUtil.computeMargin(skill);
      Entity containing=null;double nearest=Double.MAX_VALUE;
      for(Entity candidate:skill.level().getEntities(skill,skill.getBoundingBox().inflate(1),predicate)){
         if(!candidate.getBoundingBox().inflate(margin).contains(start))continue;
         if(!skill.isPhasing() && skill.level().clipIncludingBorder(new ClipContext(start,candidate.getBoundingBox().getCenter(),clip,ClipContext.Fluid.NONE,skill)).getType()==HitResult.Type.BLOCK)continue;
         double distance=candidate.getBoundingBox().getCenter().distanceToSqr(start);
         if(distance<nearest){nearest=distance;containing=candidate;}
      }
      if(containing!=null)return new EntityHitResult(containing,start);
      if(regular!=null)return regular;
      // Returning from onHitBlock is too late: vanilla has already shortened the
      // movement to the wall face. Trace entities along the full step instead.
      var hit = ProjectileUtil.getEntityHitResult(skill.level(), skill, start, end,
         skill.getBoundingBox().expandTowards(movement).inflate(1), predicate);
      return hit != null ? hit : BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
   }
}
