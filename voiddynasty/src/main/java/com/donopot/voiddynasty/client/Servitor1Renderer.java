package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de servitor_1. */
@Environment(EnvType.CLIENT)
public class Servitor1Renderer extends GeoEntityRenderer<Servitor1Entity, EntityRenderState> {

    public Servitor1Renderer(EntityRendererProvider.Context context) {
        super(context, new Servitor1Model());
    }
}
