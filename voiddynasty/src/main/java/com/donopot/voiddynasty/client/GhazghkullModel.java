package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.GhazghkullEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Ghazghkull Thraka (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class GhazghkullModel extends GeoModel<GhazghkullEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "ghazghkull");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/ghazghkull.png");
    }

    @Override
    public Identifier getAnimationResource(GhazghkullEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "ghazghkull");
    }
}
