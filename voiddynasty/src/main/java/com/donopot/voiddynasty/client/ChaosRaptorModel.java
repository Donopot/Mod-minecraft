package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosRaptorEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de chaos_raptor, converti depuis un blueprint Animated Java
 * (tools/convert_ajblueprint.py).
 */
@Environment(EnvType.CLIENT)
public class ChaosRaptorModel extends GeoModel<ChaosRaptorEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_raptor");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_raptor.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosRaptorEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_raptor");
    }
}
