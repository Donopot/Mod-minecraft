package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist1Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de cultist_1 (géométrie + texture + animations), converti depuis un blueprint
 * Animated Java par tools/convert_ajblueprint.py.
 */
@Environment(EnvType.CLIENT)
public class Cultist1Model extends GeoModel<Cultist1Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_1");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/cultist_1.png");
    }

    @Override
    public Identifier getAnimationResource(Cultist1Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_1");
    }
}
