package br.com.oneminepiece.opworldlike.haki;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Branchements du Haki (CORE-029) : gains d'EXP en combat et port automatique de l'effet.
 *
 * <p>Reprend les déclencheurs de ver15 : {@code WhenEntityTakesDamageProcedure} (dégâts subis →
 * Observation) et {@code Effect5Procedure} (coups portés → Armement). Les gains sont réservés aux
 * joueurs, et un mob ne progresse pas.</p>
 */
public final class HakiEvents {

   /** Cadence du port automatique (ticks) — l'effet dure indéfiniment, on le rafraîchit périodiquement. */
   private static final int WEAR_INTERVAL = 40;

   private static int tickCounter;

   private HakiEvents() {
   }

   public static void register() {
      ServerLivingEntityEvents.AFTER_DAMAGE.register(HakiEvents::afterDamage);
      ServerTickEvents.END_SERVER_TICK.register(HakiEvents::onServerTick);
   }

   private static void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken,
                                   float damageTaken, boolean blocked) {
      if (!HakiConfig.enabled() || !HakiConfig.gainExperience()) {
         return;
      }
      // Dégâts subis par un joueur → Haki de l'Observation.
      if (entity instanceof ServerPlayer victim && damageTaken > 0.0F) {
         HakiProgression.gainExp(victim, HakiProgression.Type.OBSERVATION);
      }
      // Coup porté par un joueur → Haki de l'Armement.
      Entity attacker = source.getEntity();
      if (attacker instanceof ServerPlayer striker && striker != entity && damageTaken > 0.0F) {
         HakiProgression.gainExp(striker, HakiProgression.Type.ARMAMENT);
      }
   }

   private static void onServerTick(net.minecraft.server.MinecraftServer server) {
      if (++tickCounter < WEAR_INTERVAL) {
         return;
      }
      tickCounter = 0;
      if (!HakiConfig.enabled() || !HakiConfig.autoWear()) {
         return;
      }
      for (ServerLevel level : server.getAllLevels()) {
         for (ServerPlayer player : level.players()) {
            HakiProgression.wear(player);
         }
      }
   }
}
