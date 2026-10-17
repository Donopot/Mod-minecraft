package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosForgefiendEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de chaos_forgefiend, converti depuis un blueprint Animated Java. */
@Environment(EnvType.CLIENT)
public class ChaosForgefiendModel extends GeoModel<ChaosForgefiendEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_forgefiend");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_forgefiend.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosForgefiendEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_forgefiend");
    }
}
