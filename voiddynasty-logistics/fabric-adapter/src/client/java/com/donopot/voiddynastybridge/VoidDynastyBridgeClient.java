package com.donopot.voiddynastybridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.network.chat.Component;

/** Local client UI. Inventory mutations are dispatched to the integrated server. */
public final class VoidDynastyBridgeClient implements ClientModInitializer {
    private final DiagnosticConnection connection = new DiagnosticConnection();

    @Override public void onInitializeClient() {
        com.donopot.voiddynastybridge.refit.RefitClient.init();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> connection.start());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> connection.close());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("voidbridge")
                        .executes(context -> open(context.getSource()))
                        .then(ClientCommands.literal("terminal").executes(context -> terminal(context.getSource())))
                        .then(ClientCommands.literal("assistant").executes(context -> guide(context.getSource())))
                        .then(ClientCommands.literal("status").executes(context -> show(context.getSource())))));
    }
    private int open(FabricClientCommandSource source) {
        if(!source.getClient().hasSingleplayerServer()){source.sendError(Component.literal("Charger un monde solo local."));return 0;}
        source.getClient().execute(()->{var service=BridgeMod.service;var v=service==null?null:service.view();source.getClient().setScreenAndShow(v==null||!v.paired()||!v.hasCrate()?new SetupScreen():new DeliveryScreen());});return 1;
    }

    private int guide(FabricClientCommandSource source) {
        if(!source.getClient().hasSingleplayerServer()){source.sendError(Component.literal("Charger un monde solo local."));return 0;}
        source.getClient().execute(()->source.getClient().setScreenAndShow(new SetupScreen()));return 1;
    }

    private int terminal(FabricClientCommandSource source) {
        if(!source.getClient().hasSingleplayerServer()){source.sendError(Component.literal("Charger un monde solo local."));return 0;}
        source.getClient().execute(()->source.getClient().setScreenAndShow(new DeliveryScreen()));return 1;
    }

    private int show(FabricClientCommandSource source) {
        if (!source.getClient().hasSingleplayerServer()) {
            source.sendError(Component.literal("VOID DYNASTY bridge prototype requires a local single-player world."));
            return 0;
        }
        DiagnosticConnection.Snapshot current = connection.snapshot();
        source.sendFeedback(Component.literal("VOID DYNASTY | " + connection.status()));
        if (current != null) {
            if (current.campaignLoaded()) {
                source.sendFeedback(Component.literal("Campaign (native ID): " + current.campaignId()));
                source.sendFeedback(Component.literal("Zone: " + current.zone() + " | Mode: " + current.mode()));
            } else source.sendFeedback(Component.literal(current.status()));
        }
        var service=BridgeMod.service;
        if(service!=null){var v=service.view();source.sendFeedback(Component.literal("Livraison: "+v.phase()+" | "+v.status()));}
        return 1;
    }
}
