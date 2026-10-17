package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.PlaguetoadEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Crapaud de la Peste (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class PlaguetoadModel extends GeoModel<PlaguetoadEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "plaguetoad");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/plaguetoad.png");
    }

    @Override
    public Identifier getAnimationResource(PlaguetoadEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "plaguetoad");
    }
}
