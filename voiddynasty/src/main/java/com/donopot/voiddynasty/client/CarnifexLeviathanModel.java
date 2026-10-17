package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.CarnifexLeviathanEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Carnifex Léviathan (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class CarnifexLeviathanModel extends GeoModel<CarnifexLeviathanEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "carnifex_leviathan");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/carnifex_leviathan.png");
    }

    @Override
    public Identifier getAnimationResource(CarnifexLeviathanEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "carnifex_leviathan");
    }
}
