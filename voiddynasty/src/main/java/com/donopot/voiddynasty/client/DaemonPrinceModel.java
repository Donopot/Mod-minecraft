package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.DaemonPrinceEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de daemon_prince (blueprint Animated Java « bloodletter »), converti par tools/convert_ajblueprint.py. */
@Environment(EnvType.CLIENT)
public class DaemonPrinceModel extends GeoModel<DaemonPrinceEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "daemon_prince");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/daemon_prince.png");
    }

    @Override
    public Identifier getAnimationResource(DaemonPrinceEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "daemon_prince");
    }
}
