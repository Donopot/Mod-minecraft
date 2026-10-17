package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosDreadnoughtEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de chaos_dreadnought. */
@Environment(EnvType.CLIENT)
public class ChaosDreadnoughtRenderer extends GeoEntityRenderer<ChaosDreadnoughtEntity, EntityRenderState> {

    public ChaosDreadnoughtRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosDreadnoughtModel());
    }
}
