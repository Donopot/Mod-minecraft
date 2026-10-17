package net.mcreator.minepiece.entity.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib générique de l'entité visuelle skill (bras/poing/tête). Le modèle ET la texture sont
 * lus dynamiquement via des {@link DataTicket} remplis par le renderer (réutilise les assets ver15).
 */
public class EntityGomuArmModel extends GeoModel<EntityGomuArmEntity> {

   public static final DataTicket<String> MODEL_TICKET = DataTicket.create("gva_model", String.class);
   public static final DataTicket<String> TEXTURE_TICKET = DataTicket.create("gomu_texture", String.class);
   /** Étirement du bras jusqu'au point d'impact (FRUIT-GOMU_GOMU-03), rempli par le renderer. */
   public static final DataTicket<Float> STRETCH_TICKET = DataTicket.create("gomu_stretch", Float.class);

   @Override
   public Identifier getModelResource(GeoRenderState state) {
      return Identifier.fromNamespaceAndPath("minepiece",
         state.getOrDefaultGeckolibData(MODEL_TICKET, "entity_gomu_arm"));
   }

   @Override
   public Identifier getTextureResource(GeoRenderState state) {
      String texture = state.getOrDefaultGeckolibData(TEXTURE_TICKET, "luffy");
      return Identifier.fromNamespaceAndPath("minepiece", net.mcreator.minepiece.client.renderer.OriginalAssetVisuals.texturePath(texture));
   }

   @Override
   public Identifier getAnimationResource(EntityGomuArmEntity entity) {
      return Identifier.fromNamespaceAndPath("minepiece", net.mcreator.minepiece.client.renderer.OriginalAssetVisuals.animation(entity.getVisualProfile(),entity.getModel(),entity.getStretch()));
   }
}
