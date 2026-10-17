package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Servitor3Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de servitor_3, converti depuis un blueprint Animated Java. */
@Environment(EnvType.CLIENT)
public class Servitor3Model extends GeoModel<Servitor3Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "servitor_3");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/servitor_3.png");
    }

    @Override
    public Identifier getAnimationResource(Servitor3Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "servitor_3");
    }
}
