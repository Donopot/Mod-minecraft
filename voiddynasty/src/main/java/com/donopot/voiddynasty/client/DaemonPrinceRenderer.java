package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.DaemonPrinceEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de daemon_prince. */
@Environment(EnvType.CLIENT)
public class DaemonPrinceRenderer extends GeoEntityRenderer<DaemonPrinceEntity, EntityRenderState> {

    public DaemonPrinceRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemonPrinceModel());
    }
}
