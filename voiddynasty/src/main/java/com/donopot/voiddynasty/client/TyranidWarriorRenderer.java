package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.TyranidWarriorEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du Tyranid Warrior.
 */
@Environment(EnvType.CLIENT)
public class TyranidWarriorRenderer extends GeoEntityRenderer<TyranidWarriorEntity, EntityRenderState> {

    public TyranidWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new TyranidWarriorModel());
    }
}
