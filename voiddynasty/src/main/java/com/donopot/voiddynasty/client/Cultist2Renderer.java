package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist2Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de cultist_2.
 */
@Environment(EnvType.CLIENT)
public class Cultist2Renderer extends GeoEntityRenderer<Cultist2Entity, EntityRenderState> {

    public Cultist2Renderer(EntityRendererProvider.Context context) {
        super(context, new Cultist2Model());
    }
}
