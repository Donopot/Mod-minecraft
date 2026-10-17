package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ChaosHellDrakeEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib de chaos_helldrake, reconstruit depuis la source par tools/convert_helldrake.py.
 */
@Environment(EnvType.CLIENT)
public class ChaosHellDrakeModel extends GeoModel<ChaosHellDrakeEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_helldrake");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/chaos_helldrake.png");
    }

    @Override
    public Identifier getAnimationResource(ChaosHellDrakeEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "chaos_helldrake");
    }
}
