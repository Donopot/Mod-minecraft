package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LordOfChangeEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Seigneur du Changement.
 */
@Environment(EnvType.CLIENT)
public class LordOfChangeRenderer extends GeoEntityRenderer<LordOfChangeEntity, EntityRenderState> {

    public LordOfChangeRenderer(EntityRendererProvider.Context context) {
        super(context, new LordOfChangeModel());
    }
}
