package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosDreadnoughtEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de chaos_dreadnought, converti depuis un blueprint Animated Java. */
@Environment(EnvType.CLIENT)
public class ChaosDreadnoughtModel extends GeoModel<ChaosDreadnoughtEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_dreadnought");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_dreadnought.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosDreadnoughtEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_dreadnought");
    }
}
