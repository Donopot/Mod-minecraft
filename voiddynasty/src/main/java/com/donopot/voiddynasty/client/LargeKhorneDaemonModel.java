package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.LargeKhorneDaemonEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Grand démon de Khorne (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class LargeKhorneDaemonModel extends GeoModel<LargeKhorneDaemonEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "large_khorne_daemon");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/large_khorne_daemon.png");
    }

    @Override
    public Identifier getAnimationResource(LargeKhorneDaemonEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "large_khorne_daemon");
    }
}
