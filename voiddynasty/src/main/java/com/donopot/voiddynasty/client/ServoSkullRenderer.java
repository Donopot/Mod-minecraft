package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.entity.ServoSkullEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.vex.VexModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;

/**
 * Rendu du servo-crâne (réutilise le modèle du Vex vanilla).
 */
public class ServoSkullRenderer extends MobRenderer<ServoSkullEntity, VexRenderState, VexModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/vex.png");

    public ServoSkullRenderer(EntityRendererProvider.Context context) {
        super(context, new VexModel(context.bakeLayer(ModelLayers.VEX)), 0.3f);
    }

    @Override
    public VexRenderState createRenderState() {
        return new VexRenderState();
    }

    @Override
    public void extractRenderState(ServoSkullEntity entity, VexRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public Identifier getTextureLocation(VexRenderState state) {
        return TEXTURE;
    }
}
