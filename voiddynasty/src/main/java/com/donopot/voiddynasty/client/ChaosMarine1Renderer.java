package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosMarine1Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de chaos_marine_1.
 */
@Environment(EnvType.CLIENT)
public class ChaosMarine1Renderer extends GeoEntityRenderer<ChaosMarine1Entity, EntityRenderState> {

    public ChaosMarine1Renderer(EntityRendererProvider.Context context) {
        super(context, new ChaosMarine1Model());
    }
}
