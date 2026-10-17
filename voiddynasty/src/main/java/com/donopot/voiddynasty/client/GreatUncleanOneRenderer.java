package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.GreatUncleanOneEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Grand Immonde.
 */
@Environment(EnvType.CLIENT)
public class GreatUncleanOneRenderer extends GeoEntityRenderer<GreatUncleanOneEntity, EntityRenderState> {

    public GreatUncleanOneRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatUncleanOneModel());
    }
}
