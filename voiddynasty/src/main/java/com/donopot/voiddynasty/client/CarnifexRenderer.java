package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.CarnifexEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Carnifex.
 */
@Environment(EnvType.CLIENT)
public class CarnifexRenderer extends GeoEntityRenderer<CarnifexEntity, EntityRenderState> {

    public CarnifexRenderer(EntityRendererProvider.Context context) {
        super(context, new CarnifexModel());
    }
}
