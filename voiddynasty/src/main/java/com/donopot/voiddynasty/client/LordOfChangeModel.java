package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LordOfChangeEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Seigneur du Changement (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class LordOfChangeModel extends GeoModel<LordOfChangeEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "lord_of_change");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/lord_of_change.png");
    }

    @Override
    public Identifier getAnimationResource(LordOfChangeEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "lord_of_change");
    }
}
