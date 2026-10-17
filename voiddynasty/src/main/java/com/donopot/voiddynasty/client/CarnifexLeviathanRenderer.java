package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.CarnifexLeviathanEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Carnifex Léviathan.
 */
@Environment(EnvType.CLIENT)
public class CarnifexLeviathanRenderer extends GeoEntityRenderer<CarnifexLeviathanEntity, EntityRenderState> {

    public CarnifexLeviathanRenderer(EntityRendererProvider.Context context) {
        super(context, new CarnifexLeviathanModel());
    }
}
