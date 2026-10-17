package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LemanRussEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib du char Leman Russ. */
@Environment(EnvType.CLIENT)
public class LemanRussRenderer extends GeoEntityRenderer<LemanRussEntity, EntityRenderState> {

    public LemanRussRenderer(EntityRendererProvider.Context context) {
        super(context, new LemanRussModel());
    }
}
