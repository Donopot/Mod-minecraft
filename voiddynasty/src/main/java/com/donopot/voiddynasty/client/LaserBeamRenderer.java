package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer « no-op » du rayon laser : le rendu visuel est assuré par les
 * particules de poussière spawnées côté client dans {@code LaserBeamEntity.tick()}.
 * Il existe uniquement pour que {@code EntityRenderDispatcher} trouve un renderer.
 */
@Environment(EnvType.CLIENT)
public class LaserBeamRenderer extends EntityRenderer<LaserBeamEntity, EntityRenderState> {

    public LaserBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
