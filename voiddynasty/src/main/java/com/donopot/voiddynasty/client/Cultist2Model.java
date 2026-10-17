package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist2Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de cultist_2 (géométrie + texture + animations), converti depuis un blueprint
 * Animated Java par tools/convert_ajblueprint.py.
 */
@Environment(EnvType.CLIENT)
public class Cultist2Model extends GeoModel<Cultist2Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_2");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/cultist_2.png");
    }

    @Override
    public Identifier getAnimationResource(Cultist2Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_2");
    }
}
