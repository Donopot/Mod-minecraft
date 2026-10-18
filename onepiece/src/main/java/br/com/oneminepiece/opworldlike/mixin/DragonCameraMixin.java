package br.com.oneminepiece.opworldlike.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import net.mcreator.minepiece.client.DragonCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class DragonCameraMixin {
   @Shadow protected abstract void setPosition(Vec3 position);

   // Runs before Camera.update prepares its frustum and projection from this position.
   @Inject(method="alignWithEntity",at=@At("RETURN"))
   private void minepiece$dragonRearView(float partialTick,CallbackInfo ci) {
      Vec3 position=DragonCamera.rearPosition((Camera)(Object)this,partialTick);
      if(position!=null)setPosition(position);
   }
}
