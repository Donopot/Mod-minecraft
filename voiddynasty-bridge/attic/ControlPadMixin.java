package com.donopot.voiddynastybridge.refit.mixin;

import com.donopot.voiddynasty.ControlPadScreen;
import com.donopot.voiddynasty.network.ShipInfo;
import com.donopot.voiddynastybridge.refit.FleetPadScreen;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ControlPadScreen.class},
   remap = false
)
public abstract class ControlPadMixin {
   @Inject(
      method = {"updateShips"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vd$list(List<ShipInfo> var1, CallbackInfo var2) {
      if (Minecraft.getInstance().gui.screen() instanceof FleetPadScreen var3) {
         var3.updateShips(var1);
         var2.cancel();
      }
   }
}
