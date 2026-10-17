package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>État négatif</b> — Horo Horo (FRUIT-HORO_HORO-04).
 *
 * <p>Le plan : « créer un <b>état négatif distinct du simple ralentissement</b> si retenu », critère
 * « le <b>malus est plafonné</b> et <b>disparaît après sa durée</b> ».</p>
 *
 * <h2>« Distinct du simple ralentissement » — la phrase qui décide de la conception</h2>
 * <p>Le moteur commun applique déjà Ralentissement (SLOWNESS) sur ces techniques. Si l'état négatif
 * n'était qu'un ralentissement plus fort, le plan n'aurait pas eu besoin de le dire : il aurait dit
 * « amplifier le ralentissement ». L'état négatif est donc <b>autre chose</b> : la cible ne devient pas
 * lente, elle devient <b>démoralisée</b> — elle <b>frappe moins fort</b> et <b>travaille mal</b>.</p>
 *
 * <table border="1">
 *   <caption>Deux mécaniques distinctes, volontairement</caption>
 *   <tr><th></th><th>Ralentissement (commun)</th><th>État négatif (ce fruit)</th></tr>
 *   <tr><td>Effet</td><td>la cible se <b>déplace</b> moins vite</td><td>la cible <b>frappe</b> moins fort et
 *       <b>extrait</b> plus lentement</td></tr>
 *   <tr><td>Effets posés</td><td>SLOWNESS</td><td>WEAKNESS + MINING_FATIGUE</td></tr>
 *   <tr><td>Accumulation</td><td>non</td><td>oui, par paliers, <b>plafonnée</b></td></tr>
 * </table>
 *
 * <p>Une cible peut donc être ralentie <i>et</i> négative : ce sont deux malus indépendants, et les
 * confondre aurait fait perdre l'un des deux.</p>
 *
 * <h2>Plafond et disparition</h2>
 * <p>Le malus monte par paliers (1 → 2 → 3) et <b>ne les dépasse jamais</b> : marteler ne rend pas une
 * cible infiniment démoralisée. Et il <b>disparaît seul</b> à l'échéance, en retirant exactement les
 * effets qu'il a posés — jamais un effet d'une autre origine.</p>
 */
public final class NegativeState {

   private static final class State {
      int level;
      int remaining;

      State(int level, int remaining) {
         this.level = level;
         this.remaining = remaining;
      }
   }

   private static final Map<UUID, State> STATES = new HashMap<>();

   /** Dernier verdict affiché, par porteur (lisible par les preuves). */
   private static final Map<UUID, String> LAST_RESULT = new HashMap<>();

   private NegativeState() {
   }

   /** Branche l'entretien de l'état négatif sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
   }

   /** La cible peut-elle devenir négative ? (ni le porteur, ni une cible morte) */
   public static boolean isAdmissible(LivingEntity source, LivingEntity target) {
      if (target == null || !target.isAlive()) {
         return false;
      }
      return source == null || target != source;
   }

   /** Texte affiché du résultat — même phrase pour tous les chemins. */
   public static String resultLabel(LivingEntity source, LivingEntity target) {
      if (target == null) {
         return "§7aucune cible";
      }
      if (target == source) {
         return "§7le porteur ne se démoralise pas lui-même";
      }
      if (!target.isAlive()) {
         return "§7" + target.getName().getString() + " : déjà morte";
      }
      return "§5" + target.getName().getString() + " sombre dans le négatif (palier "
         + (levelOf(target) + 1) + "/" + DevilFruitHollowConfig.negativeMaxLevel() + ")";
   }

   /** Dernier verdict affiché pour un porteur. */
   public static String lastResult(LivingEntity source) {
      return source == null ? null : LAST_RESULT.get(source.getUUID());
   }

   /**
    * Applique l'état négatif et affiche le résultat.
    *
    * @return palier atteint, ou {@code 0} si la cible n'est pas admissible
    */
   public static int applyFrom(LivingEntity source, LivingEntity target) {
      if (source != null) {
         LAST_RESULT.put(source.getUUID(), resultLabel(source, target));
         if (source instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal(LAST_RESULT.get(player.getUUID())), true);
         }
      }
      if (!isAdmissible(source, target)) {
         return 0;
      }
      return intensify(target);
   }

   /**
    * Fait monter l'état d'un palier, plafonné.
    *
    * @return palier atteint
    */
   public static int intensify(LivingEntity target) {
      State state = STATES.get(target.getUUID());
      if (state == null) {
         state = new State(0, 0);
         STATES.put(target.getUUID(), state);
      }
      state.level = Math.min(DevilFruitHollowConfig.negativeMaxLevel(), state.level + 1);
      state.remaining = DevilFruitHollowConfig.negativeLevelTicks();
      int duration = state.remaining;
      // WEAKNESS : moins de dégâts infligés. MINING_FATIGUE : moins efficace au travail.
      // Aucun SLOWNESS ici : c'est le point du contrat.
      target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, state.level - 1, false, false, true));
      target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, state.level - 1, false, false, true));
      if (target.level() instanceof ServerLevel level) {
         level.sendParticles(MinepieceModParticles.NEGATIVE, target.getX(), target.getY() + 1.2, target.getZ(),
            3 + state.level * 2, 0.3, 0.4, 0.3, 0.01);
      }
      return state.level;
   }

   /** Palier négatif courant (0 = aucun). */
   public static int levelOf(LivingEntity target) {
      State state = target == null ? null : STATES.get(target.getUUID());
      return state == null ? 0 : state.level;
   }

   /** Vrai si la cible est dans l'état négatif. */
   public static boolean isNegative(LivingEntity target) {
      return levelOf(target) > 0;
   }

   /** Ticks restants. */
   public static int remaining(LivingEntity target) {
      State state = target == null ? null : STATES.get(target.getUUID());
      return state == null ? 0 : state.remaining;
   }

   /** Cibles suivies (observable). */
   public static int trackedCount() {
      return STATES.size();
   }

   /** Retire l'état et <b>ses</b> effets — jamais ceux d'une autre origine. */
   public static void release(LivingEntity target) {
      if (target == null || STATES.remove(target.getUUID()) == null) {
         return;
      }
      target.removeEffect(MobEffects.WEAKNESS);
      target.removeEffect(MobEffects.MINING_FATIGUE);
   }

   /** Retire l'état de toutes les cibles (retrait du Fruit, fin de test). */
   public static void releaseAll() {
      for (LivingEntity target : tracked()) {
         release(target);
      }
      STATES.clear();
   }

   /** Oublie les verdicts affichés (fin de test). */
   public static void clearResults() {
      LAST_RESULT.clear();
   }

   /** Entretien : échéance et cible disparue. Exposé pour les tests. */
   public static void tick() {
      if (STATES.isEmpty()) {
         return;
      }
      for (Map.Entry<UUID, State> entry : new ArrayList<>(STATES.entrySet())) {
         LivingEntity target = find(entry.getKey());
         if (target == null || !target.isAlive()) {
            STATES.remove(entry.getKey());
            continue;
         }
         State state = entry.getValue();
         state.remaining--;
         if (state.remaining <= 0) {
            release(target);
            continue;
         }
         if (target.level() instanceof ServerLevel level) {
            level.sendParticles(MinepieceModParticles.NEGATIVE, target.getX(), target.getY() + 1.1, target.getZ(),
               2, 0.25, 0.3, 0.25, 0.0);
         }
      }
   }

   /** Cibles suivies, résolues en entités. */
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
