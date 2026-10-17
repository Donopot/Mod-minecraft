package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.GreatUncleanOneEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Grand Immonde (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class GreatUncleanOneModel extends GeoModel<GreatUncleanOneEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "great_unclean_one");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/great_unclean_one.png");
    }

    @Override
    public Identifier getAnimationResource(GreatUncleanOneEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "great_unclean_one");
    }
}
