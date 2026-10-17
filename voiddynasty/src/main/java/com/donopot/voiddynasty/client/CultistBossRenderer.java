package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.CultistBossEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de cultist_boss.
 */
@Environment(EnvType.CLIENT)
public class CultistBossRenderer extends GeoEntityRenderer<CultistBossEntity, EntityRenderState> {

    public CultistBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CultistBossModel());
    }
}
