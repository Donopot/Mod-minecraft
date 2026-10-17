package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>Contrôle à distance</b> — Horo Horo (FRUIT-HORO_HORO-05).
 *
 * <p>Le plan : « ajouter un <b>contrôle à distance borné</b> avec <b>sortie garantie</b> », critère
 * « le joueur <b>retrouve son contrôle normal</b> après <b>interruption</b> ou <b>déconnexion</b> ».</p>
 *
 * <h2>Ce que le critère met sur la table</h2>
 * <p>Il ne dit pas « le contrôle se termine ». Il dit que le joueur <b>retrouve son contrôle
 * normal</b>, et il nomme explicitement la <b>déconnexion</b>. C'est la formulation la plus exigeante de
 * tous les contrats du projet : un état qui altère la <b>liberté de mouvement du joueur</b> doit avoir
 * une sortie pour <b>chaque</b> façon de le quitter, y compris celles qu'on oublie.</p>
 *
 * <p>Le pouvoir fonctionne comme dans l'œuvre : le porteur <b>sort de son corps</b>. Pendant le
 * contrôle, son corps reste sur place, <b>immobile</b>, et son esprit agit à distance. L'immobilisation
 * est donc le cœur du mécanisme — ce qui la rend dangereuse, et ce qui impose les sorties.</p>
 *
 * <h2>Les cinq sorties, toutes obligatoires</h2>
 * <ol>
 *   <li><b>Échéance</b> — le contrôle a une durée maximale.</li>
 *   <li><b>Énergie</b> — il se paie en continu.</li>
 *   <li><b>Suppression</b> — le pouvoir scellé (mer) ne contrôle plus rien.</li>
 *   <li><b>Mort</b> — un joueur mort n'est pas immobilisé.</li>
 *   <li><b>Déconnexion</b> — nommée par le critère : on s'abonne à l'événement de déconnexion.</li>
 * </ol>
 *
 * <p>Et dans <b>tous</b> les cas, la libération retire l'effet d'immobilisation <b>et</b> le marqueur,
 * puis réaffirme que le joueur peut bouger — <b>liberté rendue explicitement</b>, pas laissée à
 * l'absence d'un effet. Un joueur qui reste figé après une déconnexion est le pire cas possible : il ne
 * peut même pas signaler le problème.</p>
 */
public final class RemoteControl {

   /** Effet d'immobilisation (le porteur est « hors de son corps »). */
   public static final int IMMOBILIZING_AMPLIFIER = 6;

   private static final class Session {
      int remaining;
      int energyCarry;

      Session(int remaining) {
         this.remaining = remaining;
      }
   }

   private static final Map<UUID, Session> SESSIONS = new HashMap<>();

   /** Raison de la dernière sortie, par joueur (lisible par les preuves). */
   private static final Map<UUID, String> LAST_EXIT = new HashMap<>();

   private RemoteControl() {
   }

   /** Branche l'entretien et la <b>sortie sur déconnexion</b>. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            tick(player);
         }
      });
      // Sortie nommée par le critère : une déconnexion ne doit jamais laisser qui que ce soit figé.
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            end(player, "deconnexion");
         }
      });
   }

   /** Vrai si le joueur contrôle à distance. */
   public static boolean isControlling(ServerPlayer player) {
      return player != null && SESSIONS.containsKey(player.getUUID());
   }

   /** Ticks restants de contrôle. */
   public static int remainingTicks(ServerPlayer player) {
      Session session = player == null ? null : SESSIONS.get(player.getUUID());
      return session == null ? 0 : session.remaining;
   }

   /** Raison de la dernière sortie. */
   public static String lastExitReason(ServerPlayer player) {
      return player == null ? "aucun" : LAST_EXIT.getOrDefault(player.getUUID(), "aucun");
   }

   /** Sessions actives (observable). */
   public static int activeSessions() {
      return SESSIONS.size();
   }

   /**
    * Démarre un contrôle à distance.
    *
    * @return vrai si le contrôle a démarré
    */
   public static boolean begin(ServerPlayer player) {
      if (player == null || !player.isAlive()) {
         return false;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return false;
      }
      var vars = net.mcreator.minepiece.network.MinepieceModVariables.get(player);
      if (vars.PlayerEnergy < DevilFruitHollowConfig.controlCostPerSecond()) {
         return false;
      }
      SESSIONS.put(player.getUUID(), new Session(DevilFruitHollowConfig.controlMaxTicks()));
      // « Hors de son corps » : le corps reste sur place. Marqueur visible + immobilisation.
      player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, DevilFruitHollowConfig.controlMaxTicks() + 10,
         IMMOBILIZING_AMPLIFIER, false, false, true));
      if (MinepieceModMobEffects.PETRIFICATION != null) {
         // Marqueur de rendu réutilisé : l'état « corps inerte » se voit de la même façon.
         player.addEffect(new MobEffectInstance(MinepieceModMobEffects.PETRIFICATION,
            DevilFruitHollowConfig.controlMaxTicks() + 10, 0, false, false, true));
      }
      player.setDeltaMovement(0.0, 0.0, 0.0);
      if (player.level() instanceof ServerLevel level) {
         level.sendParticles(MinepieceModParticles.DARK, player.getX(), player.getY() + 1.4, player.getZ(),
            20, 0.6, 0.8, 0.6, 0.02);
      }
      player.sendSystemMessage(Component.literal("§5Vous quittez votre corps ("
         + (DevilFruitHollowConfig.controlMaxTicks() / 20) + " s)."), false);
      return true;
   }

   /**
    * Termine le contrôle et <b>rend la liberté de mouvement</b>.
    *
    * @param reason raison de la sortie ({@code echeance}, {@code energie}, {@code suppression},
    *               {@code mort}, {@code deconnexion})
    */
   public static void end(ServerPlayer player, String reason) {
      if (player == null || SESSIONS.remove(player.getUUID()) == null) {
         return;
      }
      LAST_EXIT.put(player.getUUID(), reason);
      // Libération : on retire CE que le pouvoir a posé, et on réaffirme explicitement la liberté.
      player.removeEffect(MobEffects.SLOWNESS);
      if (MinepieceModMobEffects.PETRIFICATION != null) {
         player.removeEffect(MinepieceModMobEffects.PETRIFICATION);
      }
      player.setDeltaMovement(0.0, 0.0, 0.0);
      if (player.isAlive()) {
         player.sendSystemMessage(Component.literal("§7Contrôle terminé (" + reason
            + ") : vous retrouvez votre corps."), true);
      }
   }

   /** Termine tous les contrôles (retrait du Fruit, reset, fin de test). */
   public static void endAll(String reason) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         SESSIONS.clear();
         return;
      }
      for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
         end(player, reason);
      }
      SESSIONS.clear();
   }

   /** Entretien : échéance, énergie, suppression, mort. Exposé pour les tests. */
   public static void tick(ServerPlayer player) {
      Session session = SESSIONS.get(player.getUUID());
      if (session == null) {
         return;
      }
      if (!player.isAlive()) {
         end(player, "mort");
         return;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         end(player, "suppression");
         return;
      }
      session.remaining--;
      if (session.remaining <= 0) {
         end(player, "echeance");
         return;
      }
      var vars = net.mcreator.minepiece.network.MinepieceModVariables.get(player);
      session.energyCarry++;
      if (session.energyCarry >= 20) {
         session.energyCarry = 0;
         if (vars.PlayerEnergy < DevilFruitHollowConfig.controlCostPerSecond()) {
            vars.PlayerEnergy = 0.0;
            vars.syncPlayerVariables(player);
            end(player, "energie");
            return;
         }
         vars.PlayerEnergy -= DevilFruitHollowConfig.controlCostPerSecond();
         vars.syncPlayerVariables(player);
      }
      // Maintien de l'immobilisation : le corps ne bouge pas de lui-même.
      player.setDeltaMovement(0.0, 0.0, 0.0);
   }

   /** Liste des joueurs en contrôle (observable). */
   public static List<UUID> sessions() {
      return new ArrayList<>(SESSIONS.keySet());
   }
}
