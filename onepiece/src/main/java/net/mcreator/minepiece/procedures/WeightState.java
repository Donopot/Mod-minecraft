package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.kiro.DevilFruitKiroConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * <b>Poids réglable</b> — Kiro Kiro no Mi (FRUIT-KIRO_KIRO-03/04/06).
 *
 * <p>Le plan demande un « <b>poids réglable</b> avec <b>limites</b> et <b>interface</b> », critère « le
 * <b>serveur borne la valeur</b> et l'<b>interface suit l'état confirmé</b> », puis de « relier poids,
 * saut, chute et vitesse de façon <b>réversible</b> », critère « revenir au poids normal restaure les
 * valeurs <b>sans accumulation</b> ».</p>
 *
 * <h2>« Le serveur borne la valeur » — pourquoi ce n'est pas un détail</h2>
 * <p>Le poids est une valeur que le joueur <b>demande</b>. Comme toute valeur venue d'un client, elle
 * peut être fausse : un client modifié demandera 1 000 000, et si le serveur l'accepte, le joueur
 * traversera le monde, écrasera tout le monde, et il n'y aura eu <b>aucune erreur visible nulle
 * part</b> — le jeu aura simplement obéi. La borne n'est donc pas une politesse d'interface : c'est le
 * seul endroit où la règle existe vraiment.</p>
 *
 * <p>Et parce que le critère parle de l'<b>interface</b>, la valeur <b>confirmée</b> — celle que le
 * serveur stocke après bornage — est <b>la seule</b> diffusée au client ({@link #confirmedForClient}).
 * L'interface ne peut donc pas afficher une valeur que le serveur a refusée : elle montre ce qui est
 * vrai, pas ce qui a été demandé.</p>
 *
 * <h2>« Sans accumulation » — l'invariant le plus facile à casser</h2>
 * <p>Relier le poids à la vitesse, à la chute et au saut invite à <b>ajouter</b> un modificateur à
 * chaque changement. Après vingt réglages, le joueur serait vingt fois modifié, et revenir au poids
 * normal ne rendrait rien : le pouvoir laisserait une <b>trace permanente</b> sur un personnage qui ne
 * s'en apercevrait que des heures plus tard. La règle est donc : <b>une seule dérivation</b>, et
 * <b>retrait avant pose</b> à chaque fois. {@link #applyDerived} est le propriétaire unique des quatre
 * conséquences, et le test {@code A3} vérifie qu'après quinze réglages le retour au poids normal rend
 * <b>exactement</b> les valeurs d'origine.</p>
 */
public final class WeightState {

   private static final Identifier MOD_SPEED =
      Identifier.fromNamespaceAndPath("minepiece", "weight_speed");
   private static final Identifier MOD_GRAVITY =
      Identifier.fromNamespaceAndPath("minepiece", "weight_gravity");

   /** Poids demandé/confirmé par joueur, en unités arbitraires (1,0 = normal). */
   private static final Map<UUID, Double> WEIGHT = new HashMap<>();

   /** Dernier poids effectivement CONFIRMÉ (après bornage), par joueur. */
   private static final Map<UUID, Double> CONFIRMED = new HashMap<>();

   /** Dernière demande REFUSÉE (hors bornes), par joueur — lisible par les preuves. */
   private static final Map<UUID, Double> LAST_REJECTED = new HashMap<>();

   private static final Map<UUID, Integer> SLAM_COOLDOWN = new HashMap<>();
   private record Binding(ServerPlayer player,ServerLevel world){}
   private static final Map<UUID,Binding> BINDINGS=new HashMap<>();

   private WeightState() {
   }

   /** Branche l'entretien (cooldown) et la déconnexion. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (var binding : new ArrayList<>(BINDINGS.values())) {
            tick(binding.player);
         }
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->resetAll("arret"));
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            reset(player);
         }
      });
   }

   /**
    * <b>Demande</b> de réglage du poids. Le serveur borne, puis renvoie la valeur <b>confirmée</b>.
    *
    * @return la valeur confirmée (jamais la valeur demandée)
    */
   public static double request(ServerPlayer player, double requested) {
      if (player == null) return DevilFruitKiroConfig.defaultWeight();
      if(!Double.isFinite(requested) || !(player.level() instanceof ServerLevel level) || !BatchRules.usable(player,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO)) {
         LAST_REJECTED.put(player.getUUID(),requested);
         return confirmedForClient(player);
      }
      var previous=BINDINGS.get(player.getUUID());if(previous!=null && previous.player!=player)reset(previous.player);
      BINDINGS.put(player.getUUID(),new Binding(player,(ServerLevel)player.level()));
      double borne = clamp(requested);
      if (Math.abs(borne - requested) > 1.0E-9) {
         LAST_REJECTED.put(player.getUUID(), requested);
      }
      WEIGHT.put(player.getUUID(), borne);
      CONFIRMED.put(player.getUUID(), borne);
      applyDerived(player, borne);
      broadcast(player, borne);
      return borne;
   }

   /**
    * Valeur <b>confirmée</b>, telle qu'elle doit être affichée par l'interface.
    *
    * <p>C'est la seule valeur diffusée au client : l'interface suit l'état confirmé, jamais la demande.</p>
    */
   public static double confirmedForClient(ServerPlayer player) {
      if (player == null) {
         return DevilFruitKiroConfig.defaultWeight();
      }
      var binding=BINDINGS.get(player.getUUID());
      return binding!=null && binding.player==player?CONFIRMED.getOrDefault(player.getUUID(), DevilFruitKiroConfig.defaultWeight()):DevilFruitKiroConfig.defaultWeight();
   }

   /** Poids courant. */
   public static double current(ServerPlayer player) {
      return confirmedForClient(player);
   }

   /** Dernière demande rejetée (hors bornes), ou {@code null}. */
   public static Double lastRejected(ServerPlayer player) {
      return player == null ? null : LAST_REJECTED.get(player.getUUID());
   }

   /** Le joueur a-t-il un poids réglé (différent du normal) ? */
   public static boolean isModified(ServerPlayer player) {
      return player != null
         && Math.abs(confirmedForClient(player) - DevilFruitKiroConfig.defaultWeight()) > 1.0E-9;
   }

   /** Cooldown restant de l'attaque de chute. */
   public static int slamCooldownLeft(ServerPlayer player) {
      return player == null ? 0 : SLAM_COOLDOWN.getOrDefault(player.getUUID(), 0);
   }

   /** Borne une valeur : le seul endroit où la limite existe. Exposé pour les preuves. */
   public static double clamp(double requested) {
      if(!Double.isFinite(requested))return DevilFruitKiroConfig.defaultWeight();
      return Math.min(Math.max(requested, DevilFruitKiroConfig.minWeight()),
         DevilFruitKiroConfig.maxWeight());
   }

   /** Remet le poids normal et retire TOUT ce que nous avons posé. */
   public static void reset(ServerPlayer player) {
      if (player == null) {
         return;
      }
      var binding=BINDINGS.get(player.getUUID());if(binding!=null && binding.player!=player)return;
      BINDINGS.remove(player.getUUID());WEIGHT.remove(player.getUUID());
      CONFIRMED.remove(player.getUUID());LAST_REJECTED.remove(player.getUUID());
      removeDerived(player);
      SLAM_COOLDOWN.remove(player.getUUID());
   }

   /** Retire ce fruit de tous les joueurs (retrait, reset, fin de test). */
   public static void resetAll(String reason) {
      for(var binding:new ArrayList<>(BINDINGS.values()))reset(binding.player);
      BINDINGS.clear();WEIGHT.clear();CONFIRMED.clear();LAST_REJECTED.clear();SLAM_COOLDOWN.clear();
   }

   /**
    * <b>Propriétaire unique</b> des quatre conséquences du poids : vitesse, gravité (chute), saut
    * (par la gravité) et plafond d'attaque. Retrait AVANT pose, à chaque appel — c'est ce qui garantit
    * « sans accumulation ».
    */
   private static void applyDerived(ServerPlayer player, double weight) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
      removeDerived(player);

      // ⚠️ L'ANCRAGE est le poids NORMAL, pas le poids minimal. C'est le critère qui l'impose :
      // « revenir au poids normal restaure les valeurs ». Interpoler depuis le poids minimal laissait un
      // modificateur résiduel (−0,0025) au poids normal — invisible à l'œil, mais c'était exactement le
      // « sans accumulation » que le plan interdit. Deux branches, donc, de part et d'autre du normal.
      double normal = DevilFruitKiroConfig.defaultWeight();
      double bonus = 0.0;
      double gravite = 1.0;
      if (weight < normal) {
         double span = normal - DevilFruitKiroConfig.minWeight();
         double t = span <= 0.0 ? 0.0 : (normal - weight) / span;
         t = Math.min(Math.max(t, 0.0), 1.0);
         bonus = t * DevilFruitKiroConfig.speedAtMinWeight();
         gravite = Math.max(.15,1-t*.75);
      } else if (weight > normal) {
         double span = DevilFruitKiroConfig.maxWeight() - normal;
         double t = span <= 0.0 ? 0.0 : (weight - normal) / span;
         t = Math.min(Math.max(t, 0.0), 1.0);
         bonus = t * DevilFruitKiroConfig.speedAtMaxWeight();
         gravite = 1.0 + t * (DevilFruitKiroConfig.fallMultiplierAtMaxWeight() - 1.0);
      }
      // Au poids normal, AUCUN modificateur n'est posé : la valeur nulle n'existe pas dans la liste.
      if (Math.abs(bonus) > 1.0E-9 && speed != null) {
         speed.addTransientModifier(new AttributeModifier(MOD_SPEED, bonus,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
      }
      if (Math.abs(gravite - 1.0) > 1.0E-9 && gravity != null) {
         gravity.addTransientModifier(new AttributeModifier(MOD_GRAVITY, gravite - 1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
      }
      if (MinepieceModMobEffects.WEIGHT_STATE != null) {
         player.removeEffect(MinepieceModMobEffects.WEIGHT_STATE);
         if (isModified(player)) {
            player.addEffect(new MobEffectInstance(MinepieceModMobEffects.WEIGHT_STATE, 6000, 0,
               false, false, true));
         }
      }
   }

   /** Retire nos modificateurs et notre marqueur, nominativement. */
   private static void removeDerived(ServerPlayer player) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(MOD_SPEED);
      }
      AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
      if (gravity != null) {
         gravity.removeModifier(MOD_GRAVITY);
      }
      if (MinepieceModMobEffects.WEIGHT_STATE != null) {
         player.removeEffect(MinepieceModMobEffects.WEIGHT_STATE);
      }
   }

   private static void broadcast(ServerPlayer player, double weight) {
      if (!(player.level() instanceof ServerLevel level)) {
         return;
      }
      boolean lourd = weight > DevilFruitKiroConfig.defaultWeight();
      // CLOUD et SMOKE sont tous deux des particules simples : un ternaire entre deux types
      // incompatibles (FALLING_DUST est un BlockParticleOption) ne compile pas.
      level.sendParticles(ParticleTypes.CLOUD,
         player.getX(), player.getY() + 0.2, player.getZ(), 20, 0.4, 0.2, 0.4, 0.03);
      level.playSound(null, player.blockPosition(),
         lourd ? SoundEvents.ANVIL_LAND : SoundEvents.AMETHYST_BLOCK_CHIME,
         SoundSource.PLAYERS, 0.8F, lourd ? 0.7F : 1.4F);
   }

   /** Entretien : cooldown de l'attaque de chute. Exposé pour les tests. */
   public static void tick(ServerPlayer player) {
      var binding=BINDINGS.get(player.getUUID());if(binding!=null && (binding.player!=player || !BatchRules.usable(player,binding.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO))){reset(binding.player);WeightSlam.clear(binding.player);return;}
      Integer cd = SLAM_COOLDOWN.get(player.getUUID());
      if (cd == null) {
         return;
      }
      if (cd <= 1) {
         SLAM_COOLDOWN.remove(player.getUUID());
      } else {
         SLAM_COOLDOWN.put(player.getUUID(), cd - 1);
      }
   }

   /** Arme le cooldown de l'attaque de chute. Exposé pour les preuves et pour le service d'impact. */
   public static void armSlam(ServerPlayer player) {
      if (player != null) {
         SLAM_COOLDOWN.put(player.getUUID(), DevilFruitKiroConfig.slamCooldownTicks());
      }
   }
}
