package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.KhorneJuggernautEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Juggernaut de Khorne.
 */
@Environment(EnvType.CLIENT)
public class KhorneJuggernautRenderer extends GeoEntityRenderer<KhorneJuggernautEntity, EntityRenderState> {

    public KhorneJuggernautRenderer(EntityRendererProvider.Context context) {
        super(context, new KhorneJuggernautModel());
    }
}
