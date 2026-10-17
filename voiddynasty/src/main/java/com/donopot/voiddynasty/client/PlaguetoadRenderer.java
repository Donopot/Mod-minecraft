package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.PlaguetoadEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Crapaud de la Peste.
 */
@Environment(EnvType.CLIENT)
public class PlaguetoadRenderer extends GeoEntityRenderer<PlaguetoadEntity, EntityRenderState> {

    public PlaguetoadRenderer(EntityRendererProvider.Context context) {
        super(context, new PlaguetoadModel());
    }
}
