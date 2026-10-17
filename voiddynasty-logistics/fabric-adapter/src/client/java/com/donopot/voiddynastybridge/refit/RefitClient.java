package com.donopot.voiddynastybridge.refit;
import com.donopot.voiddynasty.ControlPadScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
public final class RefitClient {
    private RefitClient(){}
    public static void init(){
        var mod=FabricLoader.getInstance().getModContainer("voiddynasty_bridge").orElseThrow();
        ResourceManagerHelper.registerBuiltinResourcePack(Identifier.fromNamespaceAndPath("voiddynasty_bridge","refit"),mod,ResourcePackActivationType.ALWAYS_ENABLED);
        ClientTickEvents.END_CLIENT_TICK.register(client->{
        if(client.gui.screen() instanceof ControlPadScreen)client.setScreenAndShow(new FleetPadScreen());
    });}
}
