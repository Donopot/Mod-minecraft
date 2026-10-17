package com.donopot.voiddynastybridge.refit.mixin;

import com.donopot.voiddynasty.weapon.WeaponDefinition;
import com.donopot.voiddynasty.weapon.WeaponService;
import com.donopot.voiddynastybridge.refit.ImpactEffects;
import com.donopot.voiddynastybridge.refit.LaserShot;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {WeaponService.class},
   remap = false
)
public abstract class WeaponServiceMixin {
   @Inject(
      method = {"hitscanShot"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void vd$laser(ServerPlayer var0, ServerLevel var1, WeaponDefinition var2, Vec3 var3, Vec3 var4, CallbackInfo var5) {
      if ("las".equals(var2.family())) {
         LaserShot.fire(var0, var1, var2, var3, var4);
         var5.cancel();
      }
   }

   @Redirect(
      method = {"explode"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
      )}
   )
   private static int vd$impact(
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
      ServerLevel var17,
      Vec3 var18,
      float var19,
      float var20,
      Entity var21,
      String var22
   ) {
      return ImpactEffects.send(var0, var1, var2, var4, var6, var8, var9, var11, var13, var15, var22);
   }
}
