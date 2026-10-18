package br.com.oneminepiece.opworldlike.mixin;

import java.lang.reflect.Method;
import net.mcreator.minepiece.client.layer.PlayerTransformLayer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ajoute la couche de transformation (humanbeast) au renderer du joueur (AvatarRenderer en 26.3).
 * Injecte la couche dans le constructeur. Utilise la réflexion pour appeler
 * {@code LivingEntityRenderer#addLayer} (protected final), ce qui évite tout souci de résolution
 * de membre de superclasse par Mixin.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

   @Inject(method = "<init>", at = @At("RETURN"))
   private void minepiece$addTransformLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
      try {
         Method addLayer = LivingEntityRenderer.class.getDeclaredMethod("addLayer", RenderLayer.class);
         addLayer.setAccessible(true);
         @SuppressWarnings("unchecked")
         RenderLayerParent<AvatarRenderState, PlayerModel> parent = (RenderLayerParent<AvatarRenderState, PlayerModel>) (Object) this;
         addLayer.invoke(this, new PlayerTransformLayer(parent));
      } catch (Exception e) {
         // Ne pas faire crasher le jeu si l'ajout de la couche échoue.
         e.printStackTrace();
      }
   }
   @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("RETURN"))
   private void minepiece$extractForm(net.minecraft.world.entity.Avatar avatar, AvatarRenderState state,
                                     float partialTick, CallbackInfo ci) {
      net.mcreator.minepiece.client.TransformClientState.useLevel(net.minecraft.client.Minecraft.getInstance().level);
      var form = net.mcreator.minepiece.client.TransformClientState.get(avatar.getUUID());
      ((net.mcreator.minepiece.client.TransformRenderData) state).minepiece$setForm(form.value(), form.fruit());
      ((net.mcreator.minepiece.client.TransformRenderData) state).minepiece$setAirborne(!avatar.onGround());
      ((net.mcreator.minepiece.client.DragonRenderData) state).minepiece$setDragonPose(
         net.mcreator.minepiece.client.DragonBodyTrail.capture(avatar,state,partialTick));
      net.mcreator.minepiece.client.MaterialClientState.useLevel(net.minecraft.client.Minecraft.getInstance().level);
      ((net.mcreator.minepiece.client.TransformRenderData)state).minepiece$setMaterial(net.mcreator.minepiece.client.MaterialClientState.get(avatar.getUUID()));
   }

}
