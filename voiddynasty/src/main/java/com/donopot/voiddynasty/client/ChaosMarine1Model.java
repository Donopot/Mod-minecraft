package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosMarine1Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de chaos_marine_1, converti depuis un blueprint Animated Java
 * (tools/convert_ajblueprint.py).
 */
@Environment(EnvType.CLIENT)
public class ChaosMarine1Model extends GeoModel<ChaosMarine1Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_marine_1");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_marine_1.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosMarine1Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_marine_1");
    }
}
