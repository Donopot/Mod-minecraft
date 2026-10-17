package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.KeeperOfSecretsEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Gardien des Secrets (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class KeeperOfSecretsModel extends GeoModel<KeeperOfSecretsEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "keeper_of_secrets");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/keeper_of_secrets.png");
    }

    @Override
    public Identifier getAnimationResource(KeeperOfSecretsEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "keeper_of_secrets");
    }
}
