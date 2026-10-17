package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosCaptainEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de chaos_captain, converti depuis un blueprint Animated Java
 * (tools/convert_ajblueprint.py).
 */
@Environment(EnvType.CLIENT)
public class ChaosCaptainModel extends GeoModel<ChaosCaptainEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_captain");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_captain.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosCaptainEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_captain");
    }
}
