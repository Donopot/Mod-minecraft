package br.com.oneminepiece.opworldlike.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import net.mcreator.minepiece.client.ParameciaRenderData;
import net.mcreator.minepiece.init.MinepieceModMobEffects;

/** Dedicated synced marker, extracted per entity; never a UUID-global tint. */
@Mixin(LivingEntityRenderer.class)
public abstract class ParameciaLivingRendererMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
 private void minepiece$stoneState(LivingEntity entity,LivingEntityRenderState state,float partial,CallbackInfo ci){((ParameciaRenderData)state).minepiece$stone(((net.mcreator.minepiece.procedures.ParameciaStoneData)entity).minepiece$stoneFlag());}
 @ModifyReturnValue(method="getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I",at=@At("RETURN"))
 private int minepiece$stoneTint(int original,LivingEntityRenderState state){return ((ParameciaRenderData)state).minepiece$stone()?0xFFBFC2C8:original;}
 @ModifyReturnValue(method="getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",at=@At("RETURN"))
 private RenderType minepiece$stoneTexture(RenderType original,LivingEntityRenderState state,boolean visible,boolean translucent,boolean glowing){if(original!=null && ((ParameciaRenderData)state).minepiece$stone()){net.mcreator.minepiece.client.Batch04VisualLab.rendered("b04_stone_statue");return RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("minepiece","textures/entities/tex_stone.png"));}return original;}
}
