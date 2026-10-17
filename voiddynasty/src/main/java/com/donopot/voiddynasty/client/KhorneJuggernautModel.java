package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.KhorneJuggernautEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Juggernaut de Khorne (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class KhorneJuggernautModel extends GeoModel<KhorneJuggernautEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "khorne_juggernaut");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/khorne_juggernaut.png");
    }

    @Override
    public Identifier getAnimationResource(KhorneJuggernautEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "khorne_juggernaut");
    }
}
