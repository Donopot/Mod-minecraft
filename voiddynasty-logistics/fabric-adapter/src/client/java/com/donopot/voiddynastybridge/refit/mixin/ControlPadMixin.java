package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynastybridge.refit.FleetPadScreen;
import java.util.List;
import com.donopot.voiddynasty.ControlPadScreen;
import com.donopot.voiddynasty.network.ShipInfo;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ControlPadScreen.class,remap=false)
public abstract class ControlPadMixin {
    @Inject(method="updateShips",at=@At("HEAD"),cancellable=true)
    private void vd$list(List<ShipInfo> ships,CallbackInfo ci){if(Minecraft.getInstance().gui.screen() instanceof FleetPadScreen pad){pad.updateShips(ships);ci.cancel();}}
}
