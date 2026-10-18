package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.network.ReloadPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.resources.Identifier;
import com.donopot.voiddynasty.weapon.WeaponItem;

/**
 * Touche R (recharger). Détecte la pression côté client et envoie la demande au serveur.
 */
@Environment(EnvType.CLIENT)
public final class BolterKeybindings {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("voiddynasty", "controls"));
    public static final KeyMapping RELOAD_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.voiddynasty.reload",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            CATEGORY
    ));
    public static final KeyMapping HUD_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.voiddynasty.hud", InputConstants.Type.KEYBOARD, InputConstants.KEY_J, CATEGORY));
    public static boolean hudVisible = true;

    private BolterKeybindings() {}

    public static void init() {
        try {if(java.nio.file.Files.isRegularFile(file())) hudVisible=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(file())).getAsJsonObject().get("visible").getAsBoolean();}
        catch(RuntimeException|java.io.IOException error) {org.slf4j.LoggerFactory.getLogger("voiddynasty").warn("Invalid HUD preference; using visible HUD",error);}
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player==null || !client.player.isAlive() || client.player.isSpectator() || client.gui.screen()!=null) {
                while(RELOAD_KEY.consumeClick()) {} while(HUD_KEY.consumeClick()) {} return;
            }
            while(HUD_KEY.consumeClick()) {
                hudVisible=!hudVisible;
                try {java.nio.file.Files.writeString(file(),"{\"visible\":"+hudVisible+"}\n");}
                catch(java.io.IOException error) {org.slf4j.LoggerFactory.getLogger("voiddynasty").warn("HUD preference could not be saved",error);}
            }
            while (RELOAD_KEY.consumeClick()) {
                // Garde anti-crash : n'envoie que si le client est connecté à un serveur.
                if (client.player.getMainHandItem().getItem() instanceof WeaponItem weapon && weapon.definition().ammo()!=null
                        && ClientPlayNetworking.canSend(ReloadPayload.TYPE)) {
                    ClientPlayNetworking.send(new ReloadPayload());
                }
            }
        });
    }
    private static java.nio.file.Path file() {return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("voiddynasty-hud.json");}
}
