package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.weapon.BoltProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer « no-op » du projectile : le rendu visuel est assuré par les
 * particules côté serveur. Il existe uniquement pour que
 * {@code EntityRenderDispatcher} trouve un renderer (sinon NPE au rendu).
 */
@Environment(EnvType.CLIENT)
public class BoltProjectileRenderer extends EntityRenderer<BoltProjectile, EntityRenderState> {

    public BoltProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
