package br.com.oneminepiece.opworldlike.mixin;

import net.mcreator.minepiece.client.TransformRenderData;
import net.mcreator.minepiece.client.layer.PlayerTransformLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Suppress only the replaced body submission; equipment layers and first-person hands stay live. */
@Mixin(LivingEntityRenderer.class)
public class TransformBodyMixin {
   @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
   private void minepiece$replaceBody(LivingEntityRenderState state, boolean visible, boolean translucent,
                                      boolean glowing, CallbackInfoReturnable<RenderType> cir) {
      if ((Object)this instanceof AvatarRenderer && state instanceof TransformRenderData amaru
          && amaru.minepiece$form()>0 && net.mcreator.minepiece.procedures.GoroAmaru.FRUIT.equals(amaru.minepiece$fruit())) {cir.setReturnValue(null);return;}
      if ((Object) this instanceof AvatarRenderer && state instanceof TransformRenderData data
          && data.minepiece$form() > 0 && visible && !translucent && !glowing
          && (PlayerTransformLayer.replacesBody(data.minepiece$fruit()) || (data.minepiece$form()==2 && (data.minepiece$fruit().equals("minepiece:item_devil_fruit_batto_batto") || data.minepiece$fruit().equals("minepiece:item_devil_fruit_tori_tori_phoenix") || data.minepiece$fruit().equals("minepiece:item_devil_fruit_ryu_ryu_pteranodon"))))) {
         cir.setReturnValue(null);
      }
   }
   @Inject(method="getRenderType",at=@At("RETURN"))
   private void minepiece$proveHybridBody(LivingEntityRenderState state,boolean visible,boolean translucent,boolean glowing,CallbackInfoReturnable<RenderType> cir){if((Object)this instanceof AvatarRenderer&&state instanceof TransformRenderData data&&data.minepiece$form()==1&&data.minepiece$fruit().equals("minepiece:item_devil_fruit_ryu_ryu_pteranodon")&&cir.getReturnValue()!=null)net.mcreator.minepiece.client.Batch07VisualLab.rendered("ptero-hybrid-body");}

}
