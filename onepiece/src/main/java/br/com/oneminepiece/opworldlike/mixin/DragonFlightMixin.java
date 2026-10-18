package br.com.oneminepiece.opworldlike.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-only mixin; other players and other forms retain vanilla travel. */
@Mixin(Player.class)
public abstract class DragonFlightMixin {
   @Inject(method="travel(Lnet/minecraft/world/phys/Vec3;)V",at=@At("HEAD"),cancellable=true)
   private void minepiece$dragonTravel(Vec3 relative,CallbackInfo ci) {
      if((Object)this instanceof net.minecraft.client.player.LocalPlayer player
            && net.mcreator.minepiece.client.DragonFlightControl.travel(player))ci.cancel();
   }
}
