package com.donopot.voiddynastybridge.refit;

import com.donopot.voiddynasty.ControlPadScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public final class RefitClient {
   private RefitClient() {
   }

   public static void init() {
      ModContainer var0 = (ModContainer)FabricLoader.getInstance().getModContainer("voiddynasty_bridge").orElseThrow();
      ResourceManagerHelper.registerBuiltinResourcePack(
         Identifier.fromNamespaceAndPath("voiddynasty_bridge", "refit"), var0, ResourcePackActivationType.ALWAYS_ENABLED
      );
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0x -> {
         if (var0x.gui.screen() instanceof ControlPadScreen && !(var0x.gui.screen() instanceof FleetPadScreen)) {
            var0x.setScreenAndShow(new FleetPadScreen());
         }
      });
   }
}
