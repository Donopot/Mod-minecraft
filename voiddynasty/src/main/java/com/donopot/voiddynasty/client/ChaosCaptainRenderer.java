package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosCaptainEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de chaos_captain.
 */
@Environment(EnvType.CLIENT)
public class ChaosCaptainRenderer extends GeoEntityRenderer<ChaosCaptainEntity, EntityRenderState> {

    public ChaosCaptainRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosCaptainModel());
    }
}
