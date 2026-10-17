package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist4Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de cultist_4 (géométrie + texture + animations), converti depuis un blueprint
 * Animated Java par tools/convert_ajblueprint.py.
 */
@Environment(EnvType.CLIENT)
public class Cultist4Model extends GeoModel<Cultist4Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_4");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/cultist_4.png");
    }

    @Override
    public Identifier getAnimationResource(Cultist4Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_4");
    }
}
