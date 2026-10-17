package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynastybridge.refit.FixedLaserRenderer;
import com.donopot.voiddynasty.client.VoidDynastyModClient;
import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Replace the original factories at their registration sites, without duplicate registrations. */
@Mixin(value=VoidDynastyModClient.class,remap=false)
public abstract class ClientRegistrationMixin {
    @ModifyArg(method="onInitializeClient",at=@At(value="INVOKE",target="Lnet/fabricmc/fabric/api/client/rendering/v1/EntityRendererRegistry;register(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/client/renderer/entity/EntityRendererProvider;)V",ordinal=1),index=1)
    private EntityRendererProvider<LaserBeamEntity> vd$laser(EntityRendererProvider<LaserBeamEntity> ignored){return FixedLaserRenderer::new;}
}
