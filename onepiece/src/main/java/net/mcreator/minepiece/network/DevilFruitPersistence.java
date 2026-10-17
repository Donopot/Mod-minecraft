package net.mcreator.minepiece.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.mcreator.minepiece.procedures.DevilFruitTransformation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Persistance + synchronisation des variables de Fruit du Démon (CORE-003 / CORE-004).
 *
 * <ul>
 *   <li>{@code SERVER_STARTED} → charge les variables depuis le disque ;</li>
 *   <li>{@code JOIN} → synchronise l'état complet vers le client qui vient de se connecter ;</li>
 *   <li>{@code DISCONNECT} → sauvegarde ;</li>
 *   <li>{@code SERVER_STOPPING} → sauvegarde finale.</li>
 * </ul>
 */
public final class DevilFruitPersistence {

   private DevilFruitPersistence() {
   }

   public static void register() {
      ServerLifecycleEvents.SERVER_STARTING.register(server -> {
         FruitWorldStorage.bind(server);
         MinepieceModVariables.clearAll();
      });
      ServerLifecycleEvents.SERVER_STARTED.register(server -> MinepieceModVariables.loadAll());
      ServerLifecycleEvents.SERVER_STOPPING.register(server -> MinepieceModVariables.saveAll());
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
         MinepieceModVariables.clearAll();
         FruitWorldStorage.unbind(server);
      });

      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
         ServerPlayer player = handler.player;
         DevilFruitTransformation.restorePersistedForm(player);
         MinepieceModVariables.get(player).syncPlayerVariables(player);
      });
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
         DevilFruitTransformation.restorePersistedForm(newPlayer);
         MinepieceModVariables.get(newPlayer).syncPlayerVariables(newPlayer);
      });

      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> MinepieceModVariables.saveAll());
   }
}
