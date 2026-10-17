package br.com.oneminepiece.opworldlike.mixin;

import net.mcreator.minepiece.procedures.GoroAmaru;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** LivingEntity applies SCALE after this base shape, yielding 2.4 x 6.5 with eyes at 5.5. */
@Mixin(Avatar.class)
public class AmaruDimensionsMixin {
   @Inject(method="getDefaultDimensions",at=@At("RETURN"),cancellable=true)
   private void minepiece$amaruDimensions(Pose pose,CallbackInfoReturnable<EntityDimensions> cir){
      if(GoroAmaru.scaled((LivingEntity)(Object)this))cir.setReturnValue(GoroAmaru.dimensions(pose));
   }
}
