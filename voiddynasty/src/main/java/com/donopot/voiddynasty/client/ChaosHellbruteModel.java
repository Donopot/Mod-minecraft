package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosHellbruteEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de chaos_hellbrute, converti depuis un blueprint Animated Java. */
@Environment(EnvType.CLIENT)
public class ChaosHellbruteModel extends GeoModel<ChaosHellbruteEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_hellbrute");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_hellbrute.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosHellbruteEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_hellbrute");
    }
}
