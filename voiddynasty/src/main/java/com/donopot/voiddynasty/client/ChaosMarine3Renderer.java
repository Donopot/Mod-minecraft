package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosMarine3Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de chaos_marine_3.
 */
@Environment(EnvType.CLIENT)
public class ChaosMarine3Renderer extends GeoEntityRenderer<ChaosMarine3Entity, EntityRenderState> {

    public ChaosMarine3Renderer(EntityRendererProvider.Context context) {
        super(context, new ChaosMarine3Model());
    }
}
