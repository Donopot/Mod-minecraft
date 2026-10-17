package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Servitor2Entity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** Modèle GeckoLib de servitor_2, converti depuis un blueprint Animated Java. */
@Environment(EnvType.CLIENT)
public class Servitor2Model extends GeoModel<Servitor2Entity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "servitor_2");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/servitor_2.png");
    }

    @Override
    public Identifier getAnimationResource(Servitor2Entity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "servitor_2");
    }
}
