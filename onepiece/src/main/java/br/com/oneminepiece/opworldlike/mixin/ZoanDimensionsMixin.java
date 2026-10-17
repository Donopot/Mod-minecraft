package br.com.oneminepiece.opworldlike.mixin;
import net.minecraft.world.entity.*;
import net.mcreator.minepiece.procedures.ZoanForms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Synced scale markers identify native beast bodies on both server and tracking clients. */
@Mixin(Avatar.class)
public class ZoanDimensionsMixin {
 @Inject(method="getDefaultDimensions",at=@At("RETURN"),cancellable=true)
 private void minepiece$beast(Pose pose,CallbackInfoReturnable<EntityDimensions> cir){var b=ZoanForms.body((LivingEntity)(Object)this);if(b!=null)cir.setReturnValue(EntityDimensions.scalable((float)(b.width()/b.scale()),(float)(b.height()/b.scale())).withEyeHeight((float)(b.eye()/b.scale())));}
}
