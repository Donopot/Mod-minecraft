package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.NornEmissaryEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Émissaire Norn.
 */
@Environment(EnvType.CLIENT)
public class NornEmissaryRenderer extends GeoEntityRenderer<NornEmissaryEntity, EntityRenderState> {

    public NornEmissaryRenderer(EntityRendererProvider.Context context) {
        super(context, new NornEmissaryModel());
    }
}
