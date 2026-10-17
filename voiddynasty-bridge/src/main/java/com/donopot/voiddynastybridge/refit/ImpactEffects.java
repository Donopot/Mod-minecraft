package com.donopot.voiddynastybridge.refit;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public final class ImpactEffects {
   private ImpactEffects() {
   }

   public static int send(
      ServerLevel var0,
      ParticleOptions var1,
      double var2,
      double var4,
      double var6,
      int var8,
      double var9,
      double var11,
      double var13,
      double var15,
      String var17
   ) {
      if ("bolt".equals(var17)) {
         if (var1 == ParticleTypes.EXPLOSION_EMITTER || var1 == ParticleTypes.EXPLOSION) {
            var1 = ParticleTypes.CRIT;
         }

         var8 = Math.min(var8, 7);
         var9 = Math.min(var9, 0.12);
         var11 = Math.min(var11, 0.12);
         var13 = Math.min(var13, 0.12);
         var15 = Math.min(var15, 0.03);
      } else if ("plasma".equals(var17)) {
         if (var1 == ParticleTypes.EXPLOSION_EMITTER) {
            var1 = ParticleTypes.ELECTRIC_SPARK;
            var8 = 12;
         } else if (var1 instanceof DustParticleOptions) {
            var1 = new DustParticleOptions(-10096641, 2.0F);
            var8 = Math.min(40, Math.max(16, var8));
         }
      }

      return var0.sendParticles((ParticleOptions)var1, var2, var4, var6, var8, var9, var11, var13, var15);
   }
}
