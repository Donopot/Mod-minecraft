package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de servitor_1, converti depuis un blueprint Animated Java. */
@Environment(EnvType.CLIENT)
public class Servitor1Model extends GeoModel<Servitor1Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "servitor_1");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/servitor_1.png");
    }

    @Override
    public Identifier getAnimationResource(Servitor1Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "servitor_1");
    }
}
