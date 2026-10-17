package com.donopot.voiddynastybridge;

import com.donopot.voiddynastybridge.refit.RefitClient;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VoidDynastyBridgeClient implements ClientModInitializer {
   private final DiagnosticConnection connection = new DiagnosticConnection();

   public void onInitializeClient() {
      RefitClient.init();
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted)var1 -> this.connection.start());
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)var1 -> this.connection.close());
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(var1, var2) -> var1.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal("voidbridge")
                              .executes(var1x -> this.open((FabricClientCommandSource)var1x.getSource())))
                           .then(ClientCommands.literal("terminal").executes(var1x -> this.terminal((FabricClientCommandSource)var1x.getSource()))))
                        .then(ClientCommands.literal("assistant").executes(var1x -> this.guide((FabricClientCommandSource)var1x.getSource()))))
                     .then(ClientCommands.literal("status").executes(var1x -> this.show((FabricClientCommandSource)var1x.getSource())))
               )
         );
   }

   private int open(FabricClientCommandSource var1) {
      if (!var1.getClient().hasSingleplayerServer()) {
         var1.sendError(Component.literal("Charger un monde solo local."));
         return 0;
      } else {
         var1.getClient().execute(() -> {
            DeliveryService var1x = BridgeMod.service;
            DeliveryService.View var2 = var1x == null ? null : var1x.view();
            var1.getClient().setScreenAndShow((Screen)(var2 != null && var2.paired() && var2.hasCrate() ? new DeliveryScreen() : new SetupScreen()));
         });
         return 1;
      }
   }

   private int guide(FabricClientCommandSource var1) {
      if (!var1.getClient().hasSingleplayerServer()) {
         var1.sendError(Component.literal("Charger un monde solo local."));
         return 0;
      } else {
         var1.getClient().execute(() -> var1.getClient().setScreenAndShow(new SetupScreen()));
         return 1;
      }
   }

   private int terminal(FabricClientCommandSource var1) {
      if (!var1.getClient().hasSingleplayerServer()) {
         var1.sendError(Component.literal("Charger un monde solo local."));
         return 0;
      } else {
         var1.getClient().execute(() -> var1.getClient().setScreenAndShow(new DeliveryScreen()));
         return 1;
      }
   }

   private int show(FabricClientCommandSource var1) {
      if (!var1.getClient().hasSingleplayerServer()) {
         var1.sendError(Component.literal("VOID DYNASTY bridge prototype requires a local single-player world."));
         return 0;
      } else {
         DiagnosticConnection.Snapshot var2 = this.connection.snapshot();
         var1.sendFeedback(Component.literal("VOID DYNASTY | " + this.connection.status()));
         if (var2 != null) {
            if (var2.campaignLoaded()) {
               var1.sendFeedback(Component.literal("Campaign (native ID): " + var2.campaignId()));
               var1.sendFeedback(Component.literal("Zone: " + var2.zone() + " | Mode: " + var2.mode()));
            } else {
               var1.sendFeedback(Component.literal(var2.status()));
            }
         }

         DeliveryService var3 = BridgeMod.service;
         if (var3 != null) {
            DeliveryService.View var4 = var3.view();
            var1.sendFeedback(Component.literal("Livraison: " + var4.phase() + " | " + var4.status()));
         }

         return 1;
      }
   }
}
