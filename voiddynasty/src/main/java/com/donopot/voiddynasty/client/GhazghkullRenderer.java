package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.GhazghkullEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Ghazghkull Thraka.
 */
@Environment(EnvType.CLIENT)
public class GhazghkullRenderer extends GeoEntityRenderer<GhazghkullEntity, EntityRenderState> {

    public GhazghkullRenderer(EntityRendererProvider.Context context) {
        super(context, new GhazghkullModel());
    }
}
