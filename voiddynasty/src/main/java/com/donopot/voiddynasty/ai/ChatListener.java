package com.donopot.voiddynasty.ai;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Interface en langage naturel : parler au servitor dans le chat du jeu.
 *
 * <p>Tout message qui contient « servitor » (ou qui commence par {@code !s }) devient une
 * consigne pour le cerveau. C'est l'interface la plus simple pour l'opérateur : pas de
 * commande à retenir, pas de menu.</p>
 *
 * <p>Les messages sont filtrés une fois : la consigne ne part qu'au prochain cycle, et elle est
 * journalisée comme les autres. Rien n'est exécuté directement depuis le texte du chat — il
 * passe par le modèle, puis par la validation serveur.</p>
 */
public final class ChatListener {

    private ChatListener() {}

    public static void init() {
        ServerMessageEvents.CHAT_MESSAGE.register(ChatListener::onChat);
    }

    private static void onChat(net.minecraft.network.chat.PlayerChatMessage message,
                               ServerPlayer sender,
                               net.minecraft.network.chat.ChatType.Bound bound) {
        String raw;
        try {
            raw = message.signedContent();
        } catch (RuntimeException e) {
            return;
        }
        if (raw == null || raw.isBlank()) return;

        BrainConfig cfg = BrainConfig.get();
        String lower = raw.toLowerCase(java.util.Locale.ROOT);
        String consigne = null;
        int idx = lower.indexOf("servitor");
        int len = "servitor".length();
        if (idx < 0) {
            idx = lower.indexOf("serviteur");
            len = "serviteur".length();
        }
        if (idx >= 0) {
            consigne = (raw.substring(0, idx) + " " + raw.substring(idx + len)).trim();
        } else if (lower.startsWith("!s ")) {
            consigne = raw.substring(3).trim();
        } else if (cfg.chatAll && !raw.startsWith("!")) {
            // Mode « tout le chat » : en solo, parler au servitor ne demande aucun mot-cle.
            consigne = raw.trim();
        }
        if (consigne == null) {
            hintMissingPrefix(sender, lower, cfg);
            return;
        }
        if (consigne.isBlank()) {
            sender.sendSystemMessage(Component.literal("[Servitor] Oui ? Precise ce que tu veux."));
            return;
        }

        if (!cfg.enabled) {
            sender.sendSystemMessage(Component.literal(
                    "[Servitor] Mon cerveau est eteint. Un operateur doit faire /servitor brain on."));
            return;
        }
        if (BrainSpawn.find(sender.level()) == null) {
            sender.sendSystemMessage(Component.literal(
                    "[Servitor] Je ne suis pas en jeu. Fais /servitor spawn."));
            return;
        }

        ServitorBrain.instruct(consigne, sender.getName().getString());
        sender.sendSystemMessage(Component.literal("[Servitor] Consigne recue : " + consigne));
    }

    /**
     * Le joueur parle au servitor sans le mot-cle : le message n'arrive pas au cerveau. On le
     * signale une fois de temps en temps, sinon il croit avoir donne un ordre qui a ete ignore.
     */
    private static void hintMissingPrefix(ServerPlayer sender, String lower, BrainConfig cfg) {
        if (!cfg.enabled) return;
        boolean looksLikeOrder = false;
        for (String verb : ACTION_WORDS) {
            if (lower.contains(verb)) {
                looksLikeOrder = true;
                break;
            }
        }
        if (!looksLikeOrder) return;
        long now = System.currentTimeMillis();
        if (now - lastHintMs < 20_000L) return;
        lastHintMs = now;
        sender.sendSystemMessage(Component.literal(
                "[Servitor] Ce message ne m'est pas parvenu : je n'obeis qu'aux ordres qui me"
                        + " nomment, par exemple « servitor mine du fer »."
                        + " Pour que tout le chat me parle : /servitor brain chat all"));
    }

    private static final String[] ACTION_WORDS = {
            "mine", "creuse", "creuser", "trou", "viens", "suis", "suivre", "attaque", "tue",
            "ramasse", "recolte", "récolte", "monte", "descend", "arrete", "arrête", "stop"
    };
    private static long lastHintMs = 0L;
}
