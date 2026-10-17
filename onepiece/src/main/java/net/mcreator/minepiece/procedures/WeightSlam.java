package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.kiro.DevilFruitKiroConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>Attaque de chute bornée</b> — Kiro Kiro no Mi (FRUIT-KIRO_KIRO-05).
 *
 * <p>Le plan : « calculer une <b>attaque de chute bornée</b> selon le <b>poids</b> et la <b>vitesse</b> »,
 * critère « un <b>seul impact</b> est appliqué et le <b>coût interdit le spam gratuit</b> ».</p>
 *
 * <h2>« Un SEUL impact » — le piège du contact prolongé</h2>
 * <p>Un impact de chute se détecte au <b>contact avec le sol</b>. Or ce contact dure : un joueur à
 * l'arrêt reste « au sol » image après image, et une détection naïve d'« est au sol et lourd » frappe
 * <b>vingt fois par seconde</b>. Le pouvoir le plus lourd du jeu deviendrait alors le plus fort de tous,
 * non par sa puissance mais par un défaut de détection — et personne ne le verrait dans un test rapide,
 * puisque la première frappe est correcte.</p>
 *
 * <p>L'impact est donc <b>lié à une chute</b>, pas à un état : il faut avoir chuté, être lourd, et
 * <b>ne pas avoir déjà frappé pour cette chute</b>. La frappe désarme jusqu'à ce que le joueur quitte le
 * sol : une chute = un impact, quelle que soit la durée du contact.</p>
 *
 * <h2>« Le coût interdit le spam gratuit »</h2>
 * <p>Même avec un seul impact par chute, il suffirait de sauter en boucle sur place. La recharge
 * ({@code slam_cooldown_ticks}) ferme cette porte : c'est le coût, et il rend l'attaque <b>délibérée</b>
 * au lieu d'être un état permanent.</p>
 */
public final class WeightSlam {

   /** Joueurs ayant déjà frappé pour leur chute en cours. */
   private static final Map<UUID, Boolean> DEJA_FRAPPE = new HashMap<>();

   /** Dégâts du dernier impact, par joueur (lisible par les preuves). */
   private static final Map<UUID, Double> DERNIER_IMPACT = new HashMap<>();

   /** Raison du dernier refus, par joueur (lisible par les preuves). */
   private static final Map<UUID, String> DERNIER_REFUS = new HashMap<>();

   private WeightSlam() {
   }

   /** Table par joueur : nombre d'impacts appliqués (observable). */
   private static final Map<UUID, Integer> IMPACTS = new HashMap<>();

   /** Branche le suivi des chutes. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : new java.util.ArrayList<>(server.getPlayerList().getPlayers())) {
            if (!player.onGround()) {
               // En l'air : la chute suivante pourra frapper de nouveau.
               DEJA_FRAPPE.remove(player.getUUID());
            }
         }
      });
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->clear(handler.player));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
   }

   /** Dégâts du dernier impact, ou 0. */
   public static double lastImpact(ServerPlayer player) {
      return player == null ? 0.0 : DERNIER_IMPACT.getOrDefault(player.getUUID(), 0.0);
   }

   /** Raison du dernier refus, ou « aucune ». */
   public static String lastRefusal(ServerPlayer player) {
      return player == null ? "aucune" : DERNIER_REFUS.getOrDefault(player.getUUID(), "aucune");
   }

   /** Nombre d'impacts appliqués à ce joueur. */
   public static int impactCount(ServerPlayer player) {
      return player == null ? 0 : IMPACTS.getOrDefault(player.getUUID(), 0);
   }

   /**
    * Tente un impact. <b>Point d'entrée réel</b> : l'appelant fournit la hauteur de chute, la décision
    * (poids, unicité, coût, bornage) est prise ici.
    *
    * @return les dégâts appliqués (0 si refus)
    */
   public static double tenter(ServerPlayer player, double hauteurChute) {
      if (player == null || !Double.isFinite(hauteurChute) || !(player.level() instanceof ServerLevel world) || !BatchRules.usable(player,world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO)) {
         return 0.0;
      }
      double poids = WeightState.current(player);
      if (poids < DevilFruitKiroConfig.slamMinWeight()) {
         DERNIER_REFUS.put(player.getUUID(), "trop-leger");
         return 0.0;
      }
      if (hauteurChute < 1.0) {
         DERNIER_REFUS.put(player.getUUID(), "chute-insuffisante");
         return 0.0;
      }
      // UN SEUL impact par chute, quelle que soit la durée du contact avec le sol.
      if (Boolean.TRUE.equals(DEJA_FRAPPE.get(player.getUUID()))) {
         DERNIER_REFUS.put(player.getUUID(), "deja-frappe-pour-cette-chute");
         return 0.0;
      }
      // Le coût : sans lui, sauter en boucle sur place suffirait.
      if (WeightState.slamCooldownLeft(player) > 0) {
         DERNIER_REFUS.put(player.getUUID(), "recharge");
         return 0.0;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         DERNIER_REFUS.put(player.getUUID(), "suppression");
         return 0.0;
      }
      // Dégâts bornés : poids × hauteur, plafonnés par la configuration. Une chute de 100 blocs à
      // 100 de poids ne doit pas produire un nombre absurde.
      double brut = poids * hauteurChute * DevilFruitKiroConfig.slamDamagePerWeight();
      double degats = Math.min(brut, DevilFruitKiroConfig.slamMaxDamage());
      DEJA_FRAPPE.put(player.getUUID(), Boolean.TRUE);
      WeightState.armSlam(player);
      IMPACTS.merge(player.getUUID(), 1, Integer::sum);
      DERNIER_IMPACT.put(player.getUUID(), degats);

      if (!(player.level() instanceof ServerLevel level)) {
         return degats;
      }
      double rayon = 2.0 + poids / 25.0;
      int touches = 0;
      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(rayon, 1.5, rayon))) {
         if (!BatchRules.target(player,cible) || cible.position().distanceTo(player.position())>rayon) {
            continue;
         }
         var source = CombatSkillLogic.elementalDamageSource(level, ElementType.PHYSICAL, player, null);
         if (CombatSkillLogic.hurtAndConfirm(cible,level, source, (float) degats)) {
            touches++;
         }
      }
      TechniqueTerrain.begin(player,"weightmanipulation").impact(player.position().add(0,-.1,0));
      level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.6F);
      level.sendParticles(MinepieceModParticles.SHOCK_HIT, player.getX(), player.getY() + 0.1, player.getZ(),
         6, rayon * 0.3, 0.1, rayon * 0.3, 0.05);
      LOGGER_IMPACT(player, degats, touches);
      return degats;
   }

   private static void LOGGER_IMPACT(ServerPlayer player, double degats, int touches) {
      br.com.oneminepiece.opworldlike.OmpWorld.LOGGER.info(
         "[MinePiece] Impact de poids : {} dégât(s), {} cible(s), poids {}.",
         degats, touches, WeightState.current(player));
   }

   /** Oublie un joueur (retrait, reset, fin de test). */
   public static void clear(ServerPlayer player) {
      if (player == null) {
         return;
      }
      DEJA_FRAPPE.remove(player.getUUID());
      DERNIER_IMPACT.remove(player.getUUID());
      DERNIER_REFUS.remove(player.getUUID());
      IMPACTS.remove(player.getUUID());
   }

   /** Oublie tout le monde. */
   public static void clearAll() {
      DEJA_FRAPPE.clear();
      DERNIER_IMPACT.clear();
      DERNIER_REFUS.clear();
      IMPACTS.clear();
   }
}
