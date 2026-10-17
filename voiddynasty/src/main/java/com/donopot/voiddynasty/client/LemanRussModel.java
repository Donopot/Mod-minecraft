package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LemanRussEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modele GeckoLib du char Leman Russ (geo + texture + animation). */
@Environment(EnvType.CLIENT)
public class LemanRussModel extends GeoModel<LemanRussEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "lemunruss");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/lemunruss.png");
    }

    @Override
    public Identifier getAnimationResource(LemanRussEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "lemunruss");
    }
}
