package br.com.oneminepiece.opworldlike.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.mcreator.minepiece.procedures.*;

/** Observe the actual landing before vanilla clears fallDistance. Never cancels vanilla fall damage. */
@Mixin(Player.class)
public class KiroLandingMixin {
 @Inject(method="causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z",at=@At("HEAD"))
 private void minepiece$heavyLanding(double distance,float multiplier,DamageSource damage,CallbackInfoReturnable<Boolean> cir){if((Object)this instanceof ServerPlayer player && !ParameciaBatchTechniques.scriptedSlam(player))WeightSlam.tenter(player,distance);}
}
