package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosHellDrakeEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de chaos_helldrake.
 */
@Environment(EnvType.CLIENT)
public class ChaosHellDrakeRenderer extends GeoEntityRenderer<ChaosHellDrakeEntity, EntityRenderState> {

    public ChaosHellDrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosHellDrakeModel());
    }
}
