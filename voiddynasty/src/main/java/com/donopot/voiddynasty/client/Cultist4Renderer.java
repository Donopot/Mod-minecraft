package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist4Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de cultist_4.
 */
@Environment(EnvType.CLIENT)
public class Cultist4Renderer extends GeoEntityRenderer<Cultist4Entity, EntityRenderState> {

    public Cultist4Renderer(EntityRendererProvider.Context context) {
        super(context, new Cultist4Model());
    }
}
