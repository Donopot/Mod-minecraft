package com.donopot.voiddynastybridge.refit;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LaserRay {
   private LaserRay() {
   }

   public static LaserRay.Hit closest(Vec3 var0, Vec3 var1, List<AABB> var2) {
      double var3 = var0.distanceToSqr(var1);
      int var5 = -1;
      Vec3 var6 = var1;

      for (int var7 = 0; var7 < var2.size(); var7++) {
         AABB var8 = (AABB)var2.get(var7);
         Optional var9 = var8.contains(var0) ? Optional.of(var0) : var8.clip(var0, var1);
         if (var9.isPresent() && var0.distanceToSqr((Vec3)var9.get()) < var3) {
            var3 = var0.distanceToSqr((Vec3)var9.get());
            var6 = (Vec3)var9.get();
            var5 = var7;
         }
      }

      return new LaserRay.Hit(var5, var6);
   }

   public static record Hit(int index, Vec3 point) {
   }
}
