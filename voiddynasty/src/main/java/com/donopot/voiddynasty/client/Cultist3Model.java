package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Cultist3Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de cultist_3 (géométrie + texture + animations), converti depuis un blueprint
 * Animated Java par tools/convert_ajblueprint.py.
 */
@Environment(EnvType.CLIENT)
public class Cultist3Model extends GeoModel<Cultist3Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_3");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/cultist_3.png");
    }

    @Override
    public Identifier getAnimationResource(Cultist3Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_3");
    }
}
