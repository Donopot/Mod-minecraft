package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.TyranidWarriorEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * Modèle GeckoLib du Tyranid Warrior (geo + texture + animation).
 */
@Environment(EnvType.CLIENT)
public class TyranidWarriorModel extends GeoModel<TyranidWarriorEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "tyranid_warrior");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "textures/entity/tyranid_warrior.png");
    }

    @Override
    public Identifier getAnimationResource(TyranidWarriorEntity entity) {
        return Identifier.fromNamespaceAndPath("voiddynasty", "tyranid_warrior");
    }
}
