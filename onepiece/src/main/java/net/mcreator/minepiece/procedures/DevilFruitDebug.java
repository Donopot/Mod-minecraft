package net.mcreator.minepiece.procedures;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * <b>Témoin de diagnostic, allumé à la demande.</b>
 *
 * <p>Quand on lui demande « pourquoi je ne vois rien ? », un mod qui n'écrit rien rend la réponse
 * impossible : la technique a-t-elle été reçue ? refusée ? exécutée ? avec quel geste ? Le témoin
 * répond dans le chat, uniquement pour le joueur qui l'a allumé ({@code /devilfruit debug}), et
 * uniquement sur sa propre machine : il n'écrit rien quand il est éteint.</p>
 */
public final class DevilFruitDebug {

   private static final Set<UUID> ACTIFS = ConcurrentHashMap.newKeySet();

   private DevilFruitDebug() {
   }

   /** Allume ou éteint le témoin pour ce joueur ; renvoie le nouvel état. */
   public static boolean basculer(ServerPlayer player) {
      UUID id = player.getUUID();
      if (ACTIFS.remove(id)) {
         return false;
      }
      ACTIFS.add(id);
      return true;
   }

   public static boolean actif(ServerPlayer player) {
      return ACTIFS.contains(player.getUUID());
   }

   /** Écrit une ligne de diagnostic dans le chat du joueur, s'il a allumé le témoin. */
   public static void dire(ServerPlayer player, String texte) {
      if (actif(player)) {
         player.sendSystemMessage(Component.literal("§8[diagnostic] §7" + texte), false);
      }
   }
}
