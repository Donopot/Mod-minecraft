package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.Servitor3Entity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Renderer GeckoLib de servitor_3. */
@Environment(EnvType.CLIENT)
public class Servitor3Renderer extends GeoEntityRenderer<Servitor3Entity, EntityRenderState> {

    public Servitor3Renderer(EntityRendererProvider.Context context) {
        super(context, new Servitor3Model());
    }
}
