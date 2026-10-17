package com.donopot.voiddynastybridge.refit;

import com.donopot.voiddynasty.terrain.TerrainDamageManager;
import com.donopot.voiddynasty.weapon.DamageCalculator;
import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import com.donopot.voiddynasty.weapon.WeaponDefinition;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class LaserShot {
   private LaserShot() {
   }

   public static void fire(ServerPlayer var0, ServerLevel var1, WeaponDefinition var2, Vec3 var3, Vec3 var4) {
      Vec3 var5 = var3.add(var4.normalize().scale(Math.clamp(var2.maxRange(), 1.0, 512.0)));
      BlockHitResult var6 = var1.clip(new ClipContext(var3, var5, Block.COLLIDER, Fluid.NONE, var0));
      List<Entity> var7 = var1.getEntities(
         var0,
         new AABB(var3, var6.getLocation()).inflate(0.3),
         var0x -> var0x instanceof LivingEntity && var0x.isAlive() && var0x.isPickable() && !var0x.isSpectator()
      );
      LaserRay.Hit var8 = LaserRay.closest(var3, var6.getLocation(), var7.stream().map(var0x -> var0x.getBoundingBox().inflate(0.05)).toList());
      Vec3 var9 = var8.point();
      LivingEntity var10 = var8.index() < 0 ? null : (LivingEntity)var7.get(var8.index());
      var1.addFreshEntity(new LaserBeamEntity(var1, var3, var9, -50107));
      if (var10 != null) {
         DamageCalculator.applyDirect(var0, var10, var2, var9);
      } else if (var6.getType() == Type.BLOCK) {
         TerrainDamageManager.onImpact(var1, var6.getBlockPos(), var3, var4, var2.terrain());
      }

      if (var10 != null || var6.getType() == Type.BLOCK) {
         var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, var9.x, var9.y, var9.z, 5, 0.06, 0.06, 0.06, 0.025);
      }
   }
}
