package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.CultistBossEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de cultist_boss (géométrie + texture + animations), converti depuis un blueprint
 * Animated Java par tools/convert_ajblueprint.py.
 */
@Environment(EnvType.CLIENT)
public class CultistBossModel extends GeoModel<CultistBossEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_boss");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/cultist_boss.png");
    }

    @Override
    public Identifier getAnimationResource(CultistBossEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "cultist_boss");
    }
}
