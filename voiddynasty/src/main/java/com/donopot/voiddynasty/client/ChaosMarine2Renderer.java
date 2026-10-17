package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosMarine2Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de chaos_marine_2.
 */
@Environment(EnvType.CLIENT)
public class ChaosMarine2Renderer extends GeoEntityRenderer<ChaosMarine2Entity, EntityRenderState> {

    public ChaosMarine2Renderer(EntityRendererProvider.Context context) {
        super(context, new ChaosMarine2Model());
    }
}
