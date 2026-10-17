package br.com.oneminepiece.opworldlike.mixin;

import net.mcreator.minepiece.client.TransformRenderData;
import net.mcreator.minepiece.procedures.GoroAmaru;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adjust the animated parent too: held items must follow the enlarged hands. */
@Mixin(PlayerModel.class)
public class AmaruPlayerPoseMixin {
   @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",at=@At("TAIL"))
   private void minepiece$amaruPose(AvatarRenderState state,CallbackInfo ci){
      if(!(state instanceof TransformRenderData d) || d.minepiece$form()<=0 || !GoroAmaru.FRUIT.equals(d.minepiece$fruit()))return;
      PlayerModel m=(PlayerModel)(Object)this;
      m.head.y-=7;m.body.y-=4;m.body.z-=1;m.body.xScale=1.25F;m.body.zScale=1.5F;
      m.rightArm.x=-12.5F;m.leftArm.x=12.5F;m.rightArm.y-=4;m.leftArm.y-=4;
      m.rightLeg.y=11;m.leftLeg.y=11;
   }
}
