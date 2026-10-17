package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.CarnifexEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Carnifex (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class CarnifexModel extends GeoModel<CarnifexEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "carnifex");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/carnifex.png");
    }

    @Override
    public Identifier getAnimationResource(CarnifexEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "carnifex");
    }
}
