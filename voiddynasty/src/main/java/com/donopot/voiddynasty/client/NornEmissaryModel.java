package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.NornEmissaryEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Émissaire Norn (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class NornEmissaryModel extends GeoModel<NornEmissaryEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "norn_emissary");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/norn_emissary.png");
    }

    @Override
    public Identifier getAnimationResource(NornEmissaryEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "norn_emissary");
    }
}
