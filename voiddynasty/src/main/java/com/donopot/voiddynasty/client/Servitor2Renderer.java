package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Servitor2Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de servitor_2. */
@Environment(EnvType.CLIENT)
public class Servitor2Renderer extends GeoEntityRenderer<Servitor2Entity, EntityRenderState> {

    public Servitor2Renderer(EntityRendererProvider.Context context) {
        super(context, new Servitor2Model());
    }
}
