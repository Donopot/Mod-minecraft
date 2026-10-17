package net.mcreator.minepiece.entity.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib générique du projectile skill. Le modèle et la texture sont lus dynamiquement via
 * des {@link DataTicket} remplis par {@code SkillProjectileRenderer} (réutilise les assets ver15).
 */
public class SkillProjectileModel extends GeoModel<SkillProjectileEntity> {

   public static final DataTicket<String> MODEL_TICKET = DataTicket.create("sp_model", String.class);
   public static final DataTicket<String> TEXTURE_TICKET = DataTicket.create("sp_texture", String.class);
   public static final DataTicket<Float> SCALE_TICKET = DataTicket.create("sp_scale", Float.class);

   @Override
   public Identifier getModelResource(GeoRenderState state) {
      return Identifier.fromNamespaceAndPath("minepiece",
         state.getOrDefaultGeckolibData(MODEL_TICKET, "ball_maximum"));
   }

   @Override
   public Identifier getTextureResource(GeoRenderState state) {
      return Identifier.fromNamespaceAndPath("minepiece",
         net.mcreator.minepiece.client.renderer.OriginalAssetVisuals.texturePath(state.getOrDefaultGeckolibData(TEXTURE_TICKET, "meteor")));
   }

   @Override
   public Identifier getAnimationResource(SkillProjectileEntity entity) {
      return Identifier.fromNamespaceAndPath("minepiece", net.mcreator.minepiece.client.renderer.OriginalAssetVisuals.animation(entity.getVisualProfile(),entity.getModel()));
   }
}
