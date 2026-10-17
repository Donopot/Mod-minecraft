package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.kira.DevilFruitKiraConfig;
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
 * <b>Corps de diamant</b> — Kira Kira no Mi (FRUIT-KIRA_KIRA-03/04/06).
 *
 * <p>Le plan demande un « <b>rendu de corps ou armure de diamant</b> » dont « <b>activation et
 * désactivation sont visibles pour les observateurs</b> », puis une « <b>réduction de dégâts</b> » dont
 * « le <b>plafond</b> et les <b>exceptions</b> évitent l'<b>invulnérabilité absolue</b> non prévue ».</p>
 *
 * <h2>« Visibles pour les OBSERVATEURS » — le mot de trop, et il change tout</h2>
 * <p>Le critère ne dit pas « visible pour le porteur ». Un pouvoir qui ne se voit que de celui qui le
 * lance ne communique rien : les autres joueurs ne peuvent ni le reconnaître, ni l'anticiper, ni
 * comprendre pourquoi leurs coups ne portent pas. Chaque transition est donc <b>diffusée au monde</b>
 * (particules + son) et pas seulement au porteur — c'est la différence entre un pouvoir et un secret.</p>
 *
 * <h2>Pas d'invulnérabilité absolue : le plafond est DÉCLARÉ, donc vérifiable</h2>
 * <p>Un diamant réellement indestructible ne serait plus un pouvoir : ce serait la <b>suppression du
 * jeu</b> pour tout le monde sauf son porteur. Le plafond de réduction ({@code maxReductionPercent}) est
 * donc une valeur <b>déclarée en configuration</b>, lisible et vérifiable — pas un effet de bord de la
 * formule d'armure qu'on constaterait après coup.</p>
 *
 * <p>Les <b>exceptions</b> ne sont pas une liste d'exceptions ajoutées à la main : ce sont les
 * <b>dégâts qui ignorent l'armure</b>, sémantique portée par la source de dégâts elle-même
 * ({@code DamageTypeTags.BYPASSES_ARMOR}). Une armure de diamant protège des coups, pas de la noyade,
 * pas d'une chute, pas d'un sort : c'est exactement ce que le jeu sait déjà dire, et il vaut mieux lire
 * cette règle que la réécrire.</p>
 *
 * <h2>On ne retire jamais un état qu'on n'a pas posé</h2>
 * <p>Marqueur dédié ({@code diamond_body}) et modificateurs identifiés par ressource, retirés
 * <b>nominativement</b>. Un joueur qui a par ailleurs une potion ou un équipement garde tout.</p>
 */
public final class DiamondBody {

   private static final Identifier MOD_ARMOR =
      Identifier.fromNamespaceAndPath("minepiece", "diamond_body_armor");
   private static final Identifier MOD_TOUGHNESS =
      Identifier.fromNamespaceAndPath("minepiece", "diamond_body_toughness");

   private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
   private record Scope(ServerPlayer player,ServerLevel level,net.minecraft.world.item.Item fruit){}
   private static final Map<UUID,Scope> OWNERS=new HashMap<>();

   /** Raison de la dernière fin, par joueur (lisible par les preuves). */
   private static final Map<UUID, String> LAST_END = new HashMap<>();

   private DiamondBody() {
   }

   /** Branche l'entretien et la sortie sur déconnexion. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (Scope scope : new ArrayList<>(OWNERS.values())) {
            tick(scope.player);
         }
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->endAll("arret"));
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            end(player, "deconnexion");
         }
      });
   }

   /** Le corps de diamant est-il actif ? */
   public static boolean isActive(ServerPlayer player) {
      return player != null && ACTIVE.containsKey(player.getUUID());
   }

   /** Ticks restants. */
   public static int remainingTicks(ServerPlayer player) {
      return player == null ? 0 : ACTIVE.getOrDefault(player.getUUID(), 0);
   }

   /** Raison de la dernière fin. */
   public static String lastEndReason(ServerPlayer player) {
      return player == null ? "aucune" : LAST_END.getOrDefault(player.getUUID(), "aucune");
   }

   /** Nombre de corps actifs (observable). */
   public static int activeCount() {
      return ACTIVE.size();
   }

   /**
    * Active (ou désactive) le corps de diamant. L'<b>activation</b> comme la <b>désactivation</b> sont
    * diffusées au monde entier : le critère parle des <b>observateurs</b>, pas du porteur.
    *
    * @return vrai si l'état a changé
    */
   public static boolean toggle(ServerPlayer player) {
      if (player == null || !player.isAlive()) {
         return false;
      }
      if (isActive(player)) {
         end(player, "desactivee");
         return true;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return false; // pouvoir scellé : refus sans coût
      }
      int duration = DevilFruitKiraConfig.bodyDurationTicks();
      ACTIVE.put(player.getUUID(), duration);
      OWNERS.put(player.getUUID(),new Scope(player,(ServerLevel)player.level(),DevilFruitPowerLogic.getDevilFruit(player).getItem()));
      player.addEffect(new MobEffectInstance(MinepieceModMobEffects.DIAMOND_BODY, duration, 0,
         false, false, true));
      applyModifiers(player);
      broadcast(player, true);
      net.mcreator.minepiece.network.MaterialBodyState.send(player);
      return true;
   }

   /** Active sans basculer (chemins internes). */
   public static boolean begin(ServerPlayer player) {
      if (player == null || !player.isAlive() || isActive(player)
            || DevilFruitPowerLogic.isSuppressed(player)) {
         return false;
      }
      return toggle(player);
   }

   /**
    * Termine le corps de diamant — <b>uniquement</b> ses propres effets et modificateurs.
    *
    * @param reason {@code echeance}, {@code desactivee}, {@code suppression}, {@code mort},
    *               {@code deconnexion}, {@code retrait}
    */
   public static void end(ServerPlayer player, String reason) {
      if (player == null || ACTIVE.remove(player.getUUID()) == null) {
         return;
      }
      OWNERS.remove(player.getUUID());
      LAST_END.put(player.getUUID(), reason);
      if (MinepieceModMobEffects.DIAMOND_BODY != null) {
         player.removeEffect(MinepieceModMobEffects.DIAMOND_BODY);
      }
      removeModifiers(player);
      // La DÉSACTIVATION aussi doit se voir : un observateur doit comprendre que la protection est
      // tombée, sinon il continue de jouer contre un pouvoir qui n'existe plus.
      broadcast(player, false);
      net.mcreator.minepiece.network.MaterialBodyState.send(player);
   }

   /** Termine tous les corps (retrait du Fruit, reset, fin de test). */
   public static void endAll(String reason) {
      for(var scope:new ArrayList<>(OWNERS.values()))end(scope.player,reason);
      ACTIVE.clear();OWNERS.clear();
   }

   /**
    * Diffusion de la transition <b>à tout le monde</b>.
    *
    * <p>{@code sendParticles} sur le niveau est reçu par tous les joueurs qui chargent la zone, et
    * {@code playSound} avec un volume normal porte au-delà du porteur : c'est ce qui rend la transition
    * <b>visible pour les observateurs</b> au lieu d'être un effet privé.</p>
    */
   private static void broadcast(ServerPlayer player, boolean activation) {
      if (!(player.level() instanceof ServerLevel level)) {
         return;
      }
      level.sendParticles(MinepieceModParticles.LIGHT_SMALL, player.getX(), player.getY() + 1.0, player.getZ(),
         activation ? 60 : 25, 0.6, 1.0, 0.6, activation ? 0.05 : 0.02);
      level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.8, player.getZ(),
         30, 0.5, 0.8, 0.5, 0.01);
      level.playSound(null, player.blockPosition(),
         activation ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.GLASS_BREAK,
         SoundSource.PLAYERS, 1.0F, activation ? 1.6F : 0.7F);
   }

   /** Pose nos modificateurs, plafonnés par la configuration. */
   private static void applyModifiers(ServerPlayer player) {
      double armor = Math.min(DevilFruitKiraConfig.bodyArmorBonus(), cappedArmorBonus());
      double toughness = Math.min(DevilFruitKiraConfig.bodyToughnessBonus(), cappedToughnessBonus());
      AttributeInstance a = player.getAttribute(Attributes.ARMOR);
      if (a != null && armor > 0.0) {
         a.removeModifier(MOD_ARMOR);
         a.addTransientModifier(new AttributeModifier(MOD_ARMOR, armor,
            AttributeModifier.Operation.ADD_VALUE));
      }
      AttributeInstance t = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
      if (t != null && toughness > 0.0) {
         t.removeModifier(MOD_TOUGHNESS);
         t.addTransientModifier(new AttributeModifier(MOD_TOUGHNESS, toughness,
            AttributeModifier.Operation.ADD_VALUE));
      }
   }

   /** Retire EXACTEMENT ce que nous avons posé, par identifiant de ressource. */
   private static void removeModifiers(ServerPlayer player) {
      AttributeInstance a = player.getAttribute(Attributes.ARMOR);
      if (a != null) {
         a.removeModifier(MOD_ARMOR);
      }
      AttributeInstance t = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
      if (t != null) {
         t.removeModifier(MOD_TOUGHNESS);
      }
   }

   /**
    * Plafond de réduction, ramené à un bonus d'armure.
    *
    * <p>On ne peut pas demander à l'armure de garantir un pourcentage : sa formule est non linéaire. Mais
    * on peut <b>borner le bonus</b> de façon à ce que la réduction maximale théorique reste sous le
    * plafond déclaré. Le calcul est délibérément conservateur : mieux vaut une protection un peu plus
    * faible qu'une protection qui dépasse ce que le contrat annonce.</p>
    */
   private static double cappedArmorBonus() {
      // Vanilla armor reduction never exceeds armor/25. Bound this coat bonus,
      // not armor supplied by external equipment or potions.
      return Math.max(0,DevilFruitKiraConfig.maxReductionPercent())/4.0;
   }

   private static double cappedToughnessBonus() {
      return DevilFruitKiraConfig.bodyToughnessBonus();
   }

   /** Entretien : échéance, mort, suppression. Exposé pour les tests. */
   public static void tick(ServerPlayer player) {
      Integer remaining = ACTIVE.get(player.getUUID());
      if (remaining == null) {
         return;
      }
      var scope=OWNERS.get(player.getUUID());
      if(scope!=null && (scope.player!=player || player.level()!=scope.level || !DevilFruitPowerLogic.getDevilFruit(player).is(scope.fruit) || player.isRemoved() || player.isSpectator())){end(player,"retrait");return;}
      if (!player.isAlive()) {
         end(player, "mort");
         return;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         end(player, "suppression");
         return;
      }
      if (remaining <= 1) {
         end(player, "echeance");
         return;
      }
      ACTIVE.put(player.getUUID(), remaining - 1);
   }
}
