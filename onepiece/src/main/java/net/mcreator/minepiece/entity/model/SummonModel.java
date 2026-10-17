package net.mcreator.minepiece.entity.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.mcreator.minepiece.entity.SummonEntity;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib générique de l'invocation (CORE-021). Modèle + texture lus dynamiquement via
 * {@link DataTicket} (réutilise les assets ver15), comme {@link EntityGomuArmModel}.
 */
public class SummonModel extends GeoModel<SummonEntity> {

   public static final DataTicket<String> MODEL_TICKET = DataTicket.create("summon_model", String.class);
   public static final DataTicket<String> TEXTURE_TICKET = DataTicket.create("summon_texture", String.class);

   @Override
   public Identifier getModelResource(GeoRenderState state) {
      return Identifier.fromNamespaceAndPath("minepiece",
         state.getOrDefaultGeckolibData(MODEL_TICKET, "entity_gomu_arm"));
   }

   @Override
   public Identifier getTextureResource(GeoRenderState state) {
      String texture = state.getOrDefaultGeckolibData(TEXTURE_TICKET, "luffy");
      return Identifier.fromNamespaceAndPath("minepiece", "textures/entities/" + texture + ".png");
   }

   @Override
   public Identifier getAnimationResource(SummonEntity entity) {
      return Identifier.fromNamespaceAndPath("minepiece", entity.getModel().equals("gecko_moria")?"biped":entity.getModel());
   }
}
