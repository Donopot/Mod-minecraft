package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.network.ReloadPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

/**
 * Touche R (recharger). Détecte la pression côté client et envoie la demande au serveur.
 */
@Environment(EnvType.CLIENT)
public final class BolterKeybindings {

    public static final KeyMapping RELOAD_KEY = new KeyMapping(
            "key.voiddynasty.reload",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            KeyMapping.Category.GAMEPLAY
    );

    private BolterKeybindings() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (RELOAD_KEY.consumeClick()) {
                // Garde anti-crash : n'envoie que si le client est connecté à un serveur.
                if (client.getConnection() != null) {
                    ClientPlayNetworking.send(new ReloadPayload());
                }
            }
        });
    }
}
