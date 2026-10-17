package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LargeKhorneDaemonEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Grand démon de Khorne.
 */
@Environment(EnvType.CLIENT)
public class LargeKhorneDaemonRenderer extends GeoEntityRenderer<LargeKhorneDaemonEntity, EntityRenderState> {

    public LargeKhorneDaemonRenderer(EntityRendererProvider.Context context) {
        super(context, new LargeKhorneDaemonModel());
    }
}
