package br.com.oneminepiece.opworldlike.haki;


import net.mcreator.minepiece.init.MinepieceModParticles;
import net.mcreator.minepiece.init.MinepieceModAttributes;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.AABB;

/**
 * Progression et port du Haki (CORE-029) — port de {@code GainHakiEXPProcedure} et
 * {@code SetStatusProcedure} de ver15.
 *
 * <p>Deux types entraînables, comme dans ver15 :</p>
 * <ul>
 *   <li><b>Armement</b> — gagne de l'EXP en <i>portant</i> des coups ; perce l'intangibilité Logia et
 *       l'immunité Gomu (lu par {@code DevilFruitPowerLogic.computeDamageMagnification}).</li>
 *   <li><b>Observation</b> — gagne de l'EXP en <i>subissant</i> des dégâts.</li>
 * </ul>
 * <p>Et le <b>Haki Souverain</b>, accessible mais volontairement réduit (voir la documentation
 * CORE-029) : il applique l'effet et met hors de combat les mobs faibles autour du joueur.</p>
 *
 * <p><b>Écart assumé vis-à-vis de ver15 :</b> ver15 appliquait l'effet d'Armement dès le niveau 0
 * ({@code level >= 0}), ce qui rendait le contre universel. Ici l'effet n'est porté qu'à partir du
 * <b>niveau 1</b> : sans entraînement, l'intangibilité Logia protège réellement. C'est ce qui rend la
 * contre-mesure significative.</p>
 *
 * <p>Les niveaux sont stockés dans les variables joueur persistées (CORE-003) et répliqués dans les
 * attributs {@code armament_haki_level} / {@code observation_haki_level} quand ils sont disponibles
 * (ils ne sont pas garantis sur le joueur, cf {@code MinePieceBridge}).</p>
 */
public final class HakiProgression {

   /** Les deux Haki entraînables (+ le Souverain, non entraînable). */
   public enum Type {
      ARMAMENT, OBSERVATION, SOVEREIGN
   }

   /** Amplificateur à partir duquel ver15 considère le Haki « porté » visuellement. */
   public static final int WEAR_AMPLIFIER = 8;

   private HakiProgression() {
   }

   // ------------------------------------------------------------------ niveaux

   public static int level(ServerPlayer player, Type type) {
      PlayerVariables vars = MinepieceModVariables.get(player);
      return switch (type) {
         case ARMAMENT -> vars.hakiArmamentLevel;
         case OBSERVATION -> vars.hakiObservationLevel;
         case SOVEREIGN -> Math.max(vars.hakiArmamentLevel, vars.hakiObservationLevel);
      };
   }

   public static double exp(ServerPlayer player, Type type) {
      PlayerVariables vars = MinepieceModVariables.get(player);
      return switch (type) {
         case OBSERVATION -> vars.ExpObservation;
         default -> vars.ExpArmament;
      };
   }

   /** Fixe le niveau (0..max) et réplique dans l'attribut s'il existe. Retourne vrai si modifié. */
   public static boolean setLevel(ServerPlayer player, Type type, int newLevel) {
      int clamped = Math.max(0, Math.min(HakiConfig.maxLevel(), newLevel));
      PlayerVariables vars = MinepieceModVariables.get(player);
      boolean changed;
      if (type == Type.OBSERVATION) {
         changed = vars.hakiObservationLevel != clamped;
         vars.hakiObservationLevel = clamped;
         mirror(player, MinepieceModAttributes.OBSERVATION_HAKI_LEVEL, clamped);
      } else {
         changed = vars.hakiArmamentLevel != clamped;
         vars.hakiArmamentLevel = clamped;
         mirror(player, MinepieceModAttributes.ARMAMENT_HAKI_LEVEL, clamped);
      }
      if (changed) {
         vars.syncPlayerVariables(player);
         wear(player);
      }
      return changed;
   }

   private static void mirror(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, int value) {
      AttributeInstance instance = player.getAttribute(attribute);
      if (instance != null) {
         instance.setBaseValue(value);
      }
   }

   /** Attributs réellement attachés au joueur ? (diagnostic de la contre-mesure, cf CORE-029). */
   public static boolean attributesAttached(ServerPlayer player) {
      return player.getAttribute(MinepieceModAttributes.ARMAMENT_HAKI_LEVEL) != null;
   }

   // ------------------------------------------------------------------ gains d'EXP

   /**
    * Gain d'EXP de combat — port de {@code GainHakiEXPProcedure} : montée de niveau au seuil, message
    * et plafond. Sans effet si {@code gain-experience} est désactivé.
    */
   public static boolean gainExp(ServerPlayer player, Type type) {
      if (!HakiConfig.enabled() || !HakiConfig.gainExperience() || type == Type.SOVEREIGN) {
         return false;
      }
      int current = level(player, type);
      if (current >= HakiConfig.maxLevel()) {
         return false;
      }
      double perLevel = type == Type.OBSERVATION ? HakiConfig.observationExpPerLevel()
         : HakiConfig.armamentExpPerLevel();
      double cost = type == Type.OBSERVATION ? HakiConfig.observationExpCost()
         : HakiConfig.armamentExpCost();
      PlayerVariables vars = MinepieceModVariables.get(player);
      if (type == Type.OBSERVATION) {
         vars.ExpObservation += HakiConfig.hakiMultiplier();
         if (vars.ExpObservation > perLevel) {
            vars.ExpObservation -= cost;
            setLevel(player, type, current + 1);
            player.sendSystemMessage(Component.literal("§l§3").append(Component.translatable("minepiece.message.haki_observation_level", current + 1)), false);
            return true;
         }
      } else {
         vars.ExpArmament += HakiConfig.hakiMultiplier();
         if (vars.ExpArmament > perLevel) {
            vars.ExpArmament -= cost;
            setLevel(player, type, current + 1);
            player.sendSystemMessage(Component.literal("§l§4").append(Component.translatable("minepiece.message.haki_armament_level", current + 1)), false);
            return true;
         }
      }
      return false;
   }

   // ------------------------------------------------------------------ port de l'effet

   /** Port automatique : applique les effets au niveau du joueur (port de {@code SetStatusProcedure}). */
   public static void wear(ServerPlayer player) {
      if (!HakiConfig.enabled() || !HakiConfig.autoWear()) {
         return;
      }
      int armament = level(player, Type.ARMAMENT);
      int observation = level(player, Type.OBSERVATION);
      if (armament >= 1) {
         player.addEffect(new MobEffectInstance(MinepieceModMobEffects.ARMAMENT_HAKI, -1,
            Math.max(0, armament - 1), false, false, true));
      } else {
         player.removeEffect(MinepieceModMobEffects.ARMAMENT_HAKI);
      }
      if (observation >= 1) {
         player.addEffect(new MobEffectInstance(MinepieceModMobEffects.OBSERVATION_HAKI, -1,
            Math.max(0, observation - 1), false, false, true));
      } else {
         player.removeEffect(MinepieceModMobEffects.OBSERVATION_HAKI);
      }
   }

   /** Le joueur perce-t-il l'intangibilité Logia / l'immunité Gomu ? (même test que la couche dégâts) */
   public static boolean countersDevilFruit(ServerPlayer player) {
      return player.hasEffect(MinepieceModMobEffects.ARMAMENT_HAKI)
         || player.hasEffect(MinepieceModMobEffects.SOVEREIGN_HAKI);
   }

   // ------------------------------------------------------------------ Haki Souverain

   /**
    * Décharge de Haki Souverain : applique l'effet au joueur et met hors de combat les mobs faibles
    * autour de lui.
    *
    * <p>Réduction volontaire de la portée fonctionnelle : ver15 produisait une cinématique complète
    * (entités visuelles dédiées, effets {@code COLOR}/{@code DAMAGE_EFFECT}, mise à terre) qui n'est pas
    * portée. Ici : effet + rayon + mobs sous le seuil de PV ralentis/affaiblis (l'état « à terre »).</p>
    *
    * @return nombre d'entités mises hors de combat
    */
   public static int sovereignBurst(ServerPlayer player) {
      if (!HakiConfig.enabled()) {
         return 0;
      }
      int amplifier = Math.max(0, level(player, Type.SOVEREIGN) - 1);
      player.addEffect(new MobEffectInstance(MinepieceModMobEffects.SOVEREIGN_HAKI, 6000, amplifier, true, true));

      ServerLevel level = (ServerLevel) player.level();
      double radius = HakiConfig.sovereignRadius();
      AABB area = player.getBoundingBox().inflate(radius);
      int affected = 0;
      for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
         if (mob.getMaxHealth() > HakiConfig.sovereignMaxMobHealth()) {
            continue;
         }
         mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, HakiConfig.sovereignDuration(), 4, false, false));
         mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, HakiConfig.sovereignDuration(), 2, false, false));
         affected++;
      }
      level.sendParticles(MinepieceModParticles.BIG_SMOKE,
         player.getX(), player.getY() + 1.0, player.getZ(), 80, radius * 0.4, 0.5, radius * 0.4, 0.02);
      return affected;
   }

   /** Ligne d'état pour les commandes et les tests. */
   public static String status(ServerPlayer player) {
      return "Armement " + level(player, Type.ARMAMENT) + "/" + HakiConfig.maxLevel()
         + " (EXP " + (int) exp(player, Type.ARMAMENT) + "/" + (int) HakiConfig.armamentExpPerLevel() + ")"
         + " · Observation " + level(player, Type.OBSERVATION) + "/" + HakiConfig.maxLevel()
         + " (EXP " + (int) exp(player, Type.OBSERVATION) + "/" + (int) HakiConfig.observationExpPerLevel() + ")"
         + " · contre actif : " + (countersDevilFruit(player) ? "oui" : "non");
   }
}
