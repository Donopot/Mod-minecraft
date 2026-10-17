package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.love.DevilFruitLoveConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>Pétrification</b> — Mero Mero (FRUIT-MERO_MERO-03/04/06).
 *
 * <p>Le plan demande de « définir des <b>conditions de pétrification</b> compréhensibles et
 * <b>testables</b> », critère « la condition est <b>affichée</b> et les <b>cibles non admissibles
 * restent libres</b> » ; puis « créer un <b>état temporaire</b> avec <b>rendu, durée et résistances</b> »,
 * critère « la cible est <b>libérée à expiration ou suppression sans corruption</b> ».</p>
 *
 * <h2>La condition, et pourquoi elle doit être affichée</h2>
 * <p>Un pouvoir qui pétrifie « parfois » sans que personne ne sache pourquoi est injouable. La condition
 * retenue est donc <b>nommée, affichée et vérifiable</b> :</p>
 * <blockquote><b>Une cible est admissible si elle est vivante, si elle est une cible légitime, et si
 * elle ressent quelque chose — les morts-vivants ne ressentent rien.</b></blockquote>
 * <p>Les morts-vivants (tag {@code minecraft:undead}) sont donc <b>non admissibles</b> : ils
 * <b>restent libres</b>, jamais figés. Et le porteur <b>lit le résultat</b> à chaque tentative
 * ({@link #conditionLabel}) : la condition est affichée, pas devinée.</p>
 *
 * <h2>« Libérée sans corruption » — le mot le plus important du contrat</h2>
 * <p>Libérer ne suffit pas : il faut rendre la cible <b>exactement</b> dans l'état où elle était. Une
 * pétrification qui laisserait l'IA coupée, l'apesanteur active ou une vitesse résiduelle
 * <i>corromprait</i> la créature — elle serait libre mais cassée. On mémorise donc l'état d'IA
 * d'origine et on le restitue, on remet la gravité, et on annule la vitesse résiduelle. Trois chemins
 * de sortie, tous obligatoires : <b>l'échéance</b>, la <b>suppression du pouvoir</b>, et la
 * <b>disparition de la cible</b>.</p>
 */
public final class Petrification {

   /** Amplificateur de ralentissement qui immobilise réellement (vanille : ≫ 100 % de réduction). */
   public static final int IMMOBILIZING_AMPLIFIER = 6;

   private static final class State {
      int remaining;
      boolean aiWasDisabled;
      boolean wasMob;
      boolean gravityWasDisabled;

      State(int remaining, boolean aiWasDisabled, boolean wasMob, boolean gravityWasDisabled) {
         this.remaining = remaining;
         this.aiWasDisabled = aiWasDisabled;
         this.wasMob = wasMob;
         this.gravityWasDisabled = gravityWasDisabled;
      }
   }

   private static final Map<UUID, State> STATES = new HashMap<>();

   /** Dernière condition affichée, par porteur — lisible par les preuves. */
   private static final Map<UUID, String> LAST_CONDITION = new HashMap<>();

   private Petrification() {
   }

   /** Branche l'entretien de la pétrification sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
   }

   // ------------------------------------------------------------------ Condition de pétrification

   /**
    * La cible est-elle <b>admissible</b> à la pétrification ?
    *
    * @return vrai si la condition est remplie (cible vivante, légitime, et capable de ressentir)
    */
   public static boolean isAdmissible(LivingEntity source, LivingEntity target) {
      if (target == null || !target.isAlive()) {
         return false;
      }
      if (source != null && target == source) {
         return false; // le porteur se pétrifierait lui-même : refusé
      }
      // Les morts-vivants ne ressentent rien : c'est la condition, et elle est explicite.
      return !isUndead(target);
   }

   /**
    * Vrai si la cible appartient au tag vanille {@code undead} — <b>et</b> si la condition est active.
    *
    * <p>Le réglage {@code undead_immune = false} rend la condition inopérante sans toucher au reste du
    * pouvoir, ce qui est exactement ce qu'un contrat doit permettre.</p>
    */
   public static boolean isUndead(LivingEntity target) {
      return DevilFruitLoveConfig.undeadImmune() && target != null
         && target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD);
   }

   /**
    * Texte <b>affiché</b> de la condition — la même phrase pour tous les chemins, pour que la règle
    * soit unique.
    */
   public static String conditionLabel(LivingEntity source, LivingEntity target) {
      if (target == null) {
         return "§7aucune cible";
      }
      if (target == source) {
         return "§7le porteur ne se pétrifie pas lui-même";
      }
      if (!target.isAlive()) {
         return "§7" + target.getName().getString() + " : déjà morte";
      }
      if (isUndead(target)) {
         return "§7" + target.getName().getString()
            + " y est insensible — les morts-vivants ne ressentent rien (condition non remplie)";
      }
      return "§d" + target.getName().getString() + " : condition remplie → pétrification";
   }

   /** Dernière condition affichée pour un porteur ({@code null} si aucune). */
   public static String lastCondition(LivingEntity source) {
      return source == null ? null : LAST_CONDITION.get(source.getUUID());
   }

   // ------------------------------------------------------------------ Application

   /**
    * Tente de pétrifier une cible et <b>affiche la condition</b> au porteur.
    *
    * @return ticks de pétrification, ou {@code 0} si la condition n'est pas remplie
    */
   public static int applyFrom(LivingEntity source, LivingEntity target) {
      if (source == null && target == null) {
         return 0;
      }
      if (source != null) {
         LAST_CONDITION.put(source.getUUID(), conditionLabel(source, target));
      }
      if (!isAdmissible(source, target)) {
         displayCondition(source);
         return 0; // cible non admissible : elle reste LIBRE, aucun état posé
      }
      int ticks = petrify(target);
      displayCondition(source);
      return ticks;
   }

   /** Affiche la condition au porteur, s'il est joueur. */
   private static void displayCondition(LivingEntity source) {
      if (source instanceof ServerPlayer player) {
         String label = LAST_CONDITION.get(player.getUUID());
         if (label != null) {
            player.sendSystemMessage(Component.literal(label), true);
         }
      }
   }

   /**
    * Pétrifie une cible déjà reconnue admissible.
    *
    * @return ticks de pétrification
    */
   public static int petrify(LivingEntity target) {
      int ticks = DevilFruitLoveConfig.petrificationTicks();
      State state = STATES.get(target.getUUID());
      if (state == null) {
         // On mémorise l'état d'origine : c'est ce qui garantit une libération SANS CORRUPTION.
         // L'IA n'existe que sur les créatures (Mob) : on mémorise aussi ce fait, pour ne rien
         // « restaurer » sur une entité qui n'en avait pas.
         state = new State(0, target instanceof net.minecraft.world.entity.Mob mob && mob.isNoAi(),
            target instanceof net.minecraft.world.entity.Mob, target.isNoGravity());
         STATES.put(target.getUUID(), state);
      }
      state.remaining = ticks;
      // Immobilisation réelle + effet de rendu (couleur pierre) + résistance aux dégâts de chute.
      target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, IMMOBILIZING_AMPLIFIER,
         false, false, true));
      if (MinepieceModMobEffects.PETRIFICATION != null) {
         target.addEffect(new MobEffectInstance(MinepieceModMobEffects.PETRIFICATION, ticks, 0,
            false, false, true));
      }
      target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, ticks,
         DevilFruitLoveConfig.petrificationResistanceAmplifier(), false, false, true));
      target.setDeltaMovement(0.0, 0.0, 0.0);
      return ticks;
   }

   // ------------------------------------------------------------------ Lecture

   /** Vrai si la cible est pétrifiée par ce pouvoir. */
   public static boolean isPetrified(LivingEntity target) {
      return target != null && STATES.containsKey(target.getUUID());
   }

   /** Ticks de pétrification restants. */
   public static int remaining(LivingEntity target) {
      State state = target == null ? null : STATES.get(target.getUUID());
      return state == null ? 0 : state.remaining;
   }

   /** Nombre de cibles pétrifiées (observable). */
   public static int petrifiedCount() {
      return STATES.size();
   }

   // ------------------------------------------------------------------ Libération

   /**
    * Libère une cible <b>sans corruption</b> : l'état d'origine est restitué, pas approximé.
    */
   public static void release(LivingEntity target) {
      if (target == null) {
         return;
      }
      State state = STATES.remove(target.getUUID());
      if (state == null) {
         return;
      }
      target.removeEffect(MobEffects.SLOWNESS);
      target.removeEffect(MobEffects.RESISTANCE);
      if (MinepieceModMobEffects.PETRIFICATION != null) {
         target.removeEffect(MinepieceModMobEffects.PETRIFICATION);
      }
      // Restitution EXACTE : on ne rend pas l'IA à une créature qu'on n'avait pas mise en pause,
      // et on ne laisse pas une vitesse résiduelle qui la ferait glisser.
      if (state.wasMob && target instanceof net.minecraft.world.entity.Mob mob) {
         mob.setNoAi(state.aiWasDisabled);
      }
      target.setNoGravity(state.gravityWasDisabled);
      target.setDeltaMovement(0.0, 0.0, 0.0);
   }

   /** Libère toutes les cibles (retrait du Fruit, suppression, fin de test). */
   public static void releaseAll() {
      for (LivingEntity target : tracked()) {
         release(target);
      }
      STATES.clear();
   }

   /** Oublie les conditions affichées (fin de test). */
   public static void clearConditions() {
      LAST_CONDITION.clear();
   }

   /**
    * Entretien : échéance, cible disparue, porteur scellé. Exposé pour les tests.
    */
   public static void tick() {
      if (STATES.isEmpty()) {
         return;
      }
      for (Map.Entry<UUID, State> entry : new ArrayList<>(STATES.entrySet())) {
         LivingEntity target = find(entry.getKey());
         if (target == null || !target.isAlive()) {
            STATES.remove(entry.getKey()); // cible disparue : rien à libérer, rien à corrompre
            continue;
         }
         // Un porteur scellé ne maintient plus de pétrification : personne ne reste figé.
         if (target instanceof ServerPlayer sp && DevilFruitPowerLogic.isSuppressed(sp)) {
            release(target);
            continue;
         }
         State state = entry.getValue();
         state.remaining--;
         if (state.remaining <= 0) {
            release(target);
            continue;
         }
         // Rendus : poussière de pierre + maintien de l'immobilisation.
         target.setDeltaMovement(0.0, 0.0, 0.0);
         if (target.level() instanceof ServerLevel level) {
            level.sendParticles(MinepieceModParticles.FLOWER_PINK, target.getX(), target.getY() + 0.9, target.getZ(),
               3, 0.25, 0.5, 0.25, 0.0);
         }
      }
   }

   /** Cibles actuellement suivies. */
   public static java.util.List<LivingEntity> tracked() {
      java.util.List<LivingEntity> out = new ArrayList<>();
      for (UUID id : new ArrayList<>(STATES.keySet())) {
         LivingEntity target = find(id);
         if (target != null) {
            out.add(target);
         }
      }
      return out;
   }

   private static LivingEntity find(UUID id) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return null;
      }
      for (ServerLevel level : server.getAllLevels()) {
         if (level.getEntity(id) instanceof LivingEntity living) {
            return living;
         }
      }
      return null;
   }
}
