package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosForgefiendEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de chaos_forgefiend. */
@Environment(EnvType.CLIENT)
public class ChaosForgefiendRenderer extends GeoEntityRenderer<ChaosForgefiendEntity, EntityRenderState> {

    public ChaosForgefiendRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosForgefiendModel());
    }
}
