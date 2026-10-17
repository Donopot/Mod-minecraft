package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosMarine3Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de chaos_marine_3, converti depuis un blueprint Animated Java
 * (tools/convert_ajblueprint.py).
 */
@Environment(EnvType.CLIENT)
public class ChaosMarine3Model extends GeoModel<ChaosMarine3Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_marine_3");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_marine_3.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosMarine3Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_marine_3");
    }
}
