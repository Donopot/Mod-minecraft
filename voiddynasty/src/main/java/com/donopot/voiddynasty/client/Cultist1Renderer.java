package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist1Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de cultist_1.
 */
@Environment(EnvType.CLIENT)
public class Cultist1Renderer extends GeoEntityRenderer<Cultist1Entity, EntityRenderState> {

    public Cultist1Renderer(EntityRendererProvider.Context context) {
        super(context, new Cultist1Model());
    }
}
