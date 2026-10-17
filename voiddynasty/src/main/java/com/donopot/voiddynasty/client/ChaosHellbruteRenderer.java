package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosHellbruteEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de chaos_hellbrute. */
@Environment(EnvType.CLIENT)
public class ChaosHellbruteRenderer extends GeoEntityRenderer<ChaosHellbruteEntity, EntityRenderState> {

    public ChaosHellbruteRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosHellbruteModel());
    }
}
