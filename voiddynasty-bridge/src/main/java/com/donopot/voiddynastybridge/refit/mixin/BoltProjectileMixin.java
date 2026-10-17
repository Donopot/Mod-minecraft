package com.donopot.voiddynastybridge.refit.mixin;

import com.donopot.voiddynasty.weapon.BoltProjectile;
import com.donopot.voiddynasty.weapon.WeaponDefinition;
import com.donopot.voiddynastybridge.refit.ImpactEffects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {BoltProjectile.class},
   remap = false
)
public abstract class BoltProjectileMixin {
   @Shadow
   private WeaponDefinition def;

   @Redirect(
      method = {"impact"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
      )}
   )
   private int vd$impact(
      ServerLevel var1, ParticleOptions var2, double var3, double var5, double var7, int var9, double var10, double var12, double var14, double var16
   ) {
      return ImpactEffects.send(var1, var2, var3, var5, var7, var9, var10, var12, var14, var16, this.def == null ? "" : this.def.family());
   }

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   private void vd$plasma(CallbackInfo var1) {
      Entity var2 = ((Entity)(Object)this);
      if (var2.level().isClientSide() && !var2.isRemoved() && this.def != null && this.def.family().equals("plasma")) {
         Vec3 var3 = var2.position();
         Vec3 var4 = var2.getDeltaMovement();
         int var5 = Math.clamp((long)((int)Math.ceil(var4.length() / 0.18)), 1, 24);
         DustParticleOptions var6 = new DustParticleOptions(-12333057, 2.2F);
         DustParticleOptions var7 = new DustParticleOptions(-2162689, 1.25F);

         for (int var8 = 0; var8 < var5; var8++) {
            Vec3 var9 = var3.subtract(var4.scale((double)var8 / (double)var5));
            var2.level().addParticle(var6, var9.x, var9.y, var9.z, 0.0, 0.0, 0.0);
            if (var8 % 2 == 0) {
               var2.level().addParticle(var7, var9.x, var9.y, var9.z, 0.0, 0.0, 0.0);
            }
         }
      }
   }
}
