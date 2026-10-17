package br.com.oneminepiece.opworldlike.mixin;
import net.minecraft.world.entity.*;
import net.mcreator.minepiece.procedures.GomuGears;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Native synced scale marker supplies the same GearFourth body on server and tracking clients. */
@Mixin(Avatar.class)
public class GomuGearDimensionsMixin {
 @Inject(method="getDefaultDimensions",at=@At("RETURN"),cancellable=true)
 private void minepiece$gearDimensions(Pose pose,CallbackInfoReturnable<EntityDimensions> cir){if(GomuGears.fourth((LivingEntity)(Object)this))cir.setReturnValue(EntityDimensions.scalable(1.2F,pose==Pose.CROUCHING?1.7F:1.9F).withEyeHeight(pose==Pose.CROUCHING?1.4F:1.6F));}
}
