package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosRaptorEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de chaos_raptor.
 */
@Environment(EnvType.CLIENT)
public class ChaosRaptorRenderer extends GeoEntityRenderer<ChaosRaptorEntity, EntityRenderState> {

    public ChaosRaptorRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosRaptorModel());
    }
}
