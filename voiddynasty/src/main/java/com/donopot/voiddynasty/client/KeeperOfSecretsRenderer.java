package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.KeeperOfSecretsEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Gardien des Secrets.
 */
@Environment(EnvType.CLIENT)
public class KeeperOfSecretsRenderer extends GeoEntityRenderer<KeeperOfSecretsEntity, EntityRenderState> {

    public KeeperOfSecretsRenderer(EntityRendererProvider.Context context) {
        super(context, new KeeperOfSecretsModel());
    }
}
