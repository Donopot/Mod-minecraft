package com.donopot.voiddynasty.mixin;

import com.donopot.voiddynasty.LaserDrillItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Raise both arms while drilling, without applying a bow transform in first person. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarDrillPoseMixin {
    @Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
            at = @At("HEAD"), cancellable = true)
    private static void voiddynasty$drillPose(Avatar avatar, ItemStack stack, InteractionHand hand,
                                             CallbackInfoReturnable<HumanoidModel.ArmPose> result) {
        if (stack.getItem() instanceof LaserDrillItem && avatar.isUsingItem() && avatar.getUsedItemHand() == hand)
            result.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
    }
}
