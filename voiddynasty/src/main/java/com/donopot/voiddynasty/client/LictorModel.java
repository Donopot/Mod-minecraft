package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LictorEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Lictor (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class LictorModel extends GeoModel<LictorEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "lictor");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/lictor.png");
    }

    @Override
    public Identifier getAnimationResource(LictorEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "lictor");
    }
}
