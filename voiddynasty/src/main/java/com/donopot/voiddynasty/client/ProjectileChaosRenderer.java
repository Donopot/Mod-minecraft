package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.weapon.ProjectileChaos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer « no-op » : le rendu est assuré par les particules du projectile. Il existe pour que
 * {@code EntityRenderDispatcher} trouve un renderer, sinon le client plante au rendu.
 */
@Environment(EnvType.CLIENT)
public class ProjectileChaosRenderer extends EntityRenderer<ProjectileChaos, EntityRenderState> {

    public ProjectileChaosRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
