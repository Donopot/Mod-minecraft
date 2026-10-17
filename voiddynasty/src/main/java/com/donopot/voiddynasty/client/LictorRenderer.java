package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LictorEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Lictor.
 */
@Environment(EnvType.CLIENT)
public class LictorRenderer extends GeoEntityRenderer<LictorEntity, EntityRenderState> {

    public LictorRenderer(EntityRendererProvider.Context context) {
        super(context, new LictorModel());
    }
}
