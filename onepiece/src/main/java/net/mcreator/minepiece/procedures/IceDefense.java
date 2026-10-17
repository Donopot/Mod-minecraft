package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.inu.DevilFruitOkuchiConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.init.MinepieceModParticles;
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
 * <b>Défense de glace temporaire</b> — Inu Inu Okuchi No Makami
 * (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-05).
 *
 * <p>Le plan : « créer une <b>défense de glace temporaire</b> avec <b>coût</b> et <b>exceptions</b> »,
 * critère « la défense <b>expire sans supprimer d'autres bonus</b> du joueur ».</p>
 *
 * <h2>« Sans supprimer d'autres bonus » — la phrase qui impose l'implémentation</h2>
 * <p>La tentation est d'appliquer {@code RESISTANCE} au porteur. <b>Mauvaise idée</b> : à l'expiration,
 * on ne saurait plus distinguer <i>notre</i> résistance de celle d'une autre source, et on la lui
 * retirerait. La défense utilise donc :</p>
 * <ol>
 *   <li>un <b>effet marqueur dédié</b> ({@code ice_defense}) ;</li>
 *   <li>des <b>modificateurs d'attribut identifiés par ressource</b> ({@code ice_defense_armor},
 *       {@code ice_defense_toughness}), retirés <b>nominativement</b>.</li>
 * </ol>
 * <p>La preuve {@code V6} pose un bonus <b>étranger</b> avant l'expiration et vérifie qu'il survit :
 * la défense retire <b>ses</b> bonus, jamais « tous ».</p>
 *
 * <h2>Les exceptions, parce qu'une défense sans exception est une invulnérabilité</h2>
 * <table border="1">
 *   <caption>Ce que la défense ne couvre pas</caption>
 *   <tr><th>Exception</th><th>Pourquoi</th></tr>
 *   <tr><td><b>Le feu et la lave la font fondre</b></td><td>c'est de la glace ; le plan demande des
 *       exceptions, et celle-ci est la plus lisible du fruit</td></tr>
 *   <tr><td><b>Le pouvoir scellé (mer) la dissout</b></td><td>règle commune : un pouvoir scellé ne
 *       protège plus</td></tr>
 *   <tr><td><b>La mort et la déconnexion l'emportent</b></td><td>un joueur qui reste protégé après
 *       déconnexion est un état fantôme</td></tr>
 * </table>
 */
public final class IceDefense {

   private static final Identifier MOD_ARMOR =
      Identifier.fromNamespaceAndPath("minepiece", "ice_defense_armor");
   private static final Identifier MOD_TOUGHNESS =
      Identifier.fromNamespaceAndPath("minepiece", "ice_defense_toughness");

   private static final class Defense { final ServerPlayer owner;final ServerLevel world;final MobEffectInstance marker;int left;Defense(ServerPlayer p,int duration,MobEffectInstance m){owner=p;world=(ServerLevel)p.level();left=duration;marker=m;} }
   private static final Map<UUID, Defense> ACTIVE = new HashMap<>();

   /** Raison de la dernière fin, par joueur (lisible par les preuves). */
   private static final Map<UUID, String> LAST_END = new HashMap<>();

   private IceDefense() {
   }

   /** Branche l'expiration, la fonte au feu, la suppression et la sortie sur déconnexion. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (Defense state : new ArrayList<>(ACTIVE.values())) {
            tick(state.owner);
         }
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s -> endAll("arret"));
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            end(player, "deconnexion");
         }
      });
   }

   /** La défense est-elle active ? */
   public static boolean isActive(ServerPlayer player) {
      return player != null && ACTIVE.get(player.getUUID())!=null && ACTIVE.get(player.getUUID()).owner==player;
   }

   /** Ticks restants de défense. */
   public static int remainingTicks(ServerPlayer player) {
      return !isActive(player) ? 0 : ACTIVE.get(player.getUUID()).left;
   }

   /** Raison de la dernière fin. */
   public static String lastEndReason(ServerPlayer player) {
      return player == null ? "aucune" : LAST_END.getOrDefault(player.getUUID(), "aucune");
   }

   /** Nombre de défenses actives (observable). */
   public static int activeCount() {
      return ACTIVE.size();
   }

   /**
    * Active la défense. Le <b>coût</b> est acquitté par l'appelant (moteur de technique) : ici on ne
    * fait que poser un état borné.
    *
    * @return vrai si la défense a été posée
    */
   public static boolean begin(ServerPlayer player) {
      if (player == null || !BatchRules.usable(player,(ServerLevel)player.level(),net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI) || isActive(player) || player.hasEffect(MinepieceModMobEffects.ICE_DEFENSE)) {
         return false;
      }
      int duration = DevilFruitOkuchiConfig.defenseDurationTicks();
      player.addEffect(new MobEffectInstance(MinepieceModMobEffects.ICE_DEFENSE, duration, 0,
         false, false, true));
      ACTIVE.put(player.getUUID(),new Defense(player,duration,player.getEffect(MinepieceModMobEffects.ICE_DEFENSE)));
      AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
      if (armor != null) {
         armor.removeModifier(MOD_ARMOR);
         armor.addTransientModifier(new AttributeModifier(MOD_ARMOR,
            DevilFruitOkuchiConfig.defenseArmorBonus(), AttributeModifier.Operation.ADD_VALUE));
      }
      AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
      if (toughness != null) {
         toughness.removeModifier(MOD_TOUGHNESS);
         toughness.addTransientModifier(new AttributeModifier(MOD_TOUGHNESS,
            DevilFruitOkuchiConfig.defenseToughnessBonus(), AttributeModifier.Operation.ADD_VALUE));
      }
      if (player.level() instanceof ServerLevel level) {
         level.playSound(null, player.blockPosition(), SoundEvents.GLASS_PLACE,
            SoundSource.PLAYERS, 0.7F, 0.8F);
         level.sendParticles(MinepieceModParticles.ICE, player.getX(), player.getY() + 1.0, player.getZ(),
            30, 0.5, 0.9, 0.5, 0.02);
      }
      return true;
   }

   /**
    * Termine la défense — <b>uniquement</b> ses propres effets et modificateurs.
    *
    * @param reason {@code echeance}, {@code fonte}, {@code suppression}, {@code mort},
    *               {@code deconnexion}
    */
   public static void end(ServerPlayer player, String reason) {
      Defense state=player==null?null:ACTIVE.get(player.getUUID());
      if (state==null || state.owner!=player || !ACTIVE.remove(player.getUUID(),state)) {
         return;
      }
      LAST_END.put(player.getUUID(), reason);
      if (MinepieceModMobEffects.ICE_DEFENSE != null && player.getEffect(MinepieceModMobEffects.ICE_DEFENSE)==state.marker) {
         player.removeEffect(MinepieceModMobEffects.ICE_DEFENSE);
      }
      AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
      if (armor != null) {
         armor.removeModifier(MOD_ARMOR);
      }
      AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
      if (toughness != null) {
         toughness.removeModifier(MOD_TOUGHNESS);
      }
      if (player.level() instanceof ServerLevel level && player.isAlive()) {
         level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK,
            SoundSource.PLAYERS, 0.6F, 1.3F);
      }
   }

   /** Termine toutes les défenses (retrait du Fruit, reset, fin de test). */
   public static void endAll(String reason) {
      for(Defense state:new ArrayList<>(ACTIVE.values()))end(state.owner,reason);
   }

   /** Exposé pour les tests : force la fonte (le feu a touché le porteur). */
   public static void melt(ServerPlayer player) {
      end(player, "fonte");
   }

   /** Entretien : échéance, fonte au feu, suppression, mort. Exposé pour les tests. */
   public static void tick(ServerPlayer player) {
      Defense state = ACTIVE.get(player.getUUID());
      Integer remaining=state==null||state.owner!=player?null:state.left;
      if (remaining == null) {
         return;
      }
      if(player.isRemoved() || player.level()!=state.world || !DevilFruitPowerLogic.getDevilFruit(player).is(net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI)){end(player,"autorite");return;}
      if (!player.isAlive()) {
         end(player, "mort");
         return;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         end(player, "suppression");
         return;
      }
      // Exception : la glace fond au feu.
      if (DevilFruitOkuchiConfig.defenseMeltsInFire()
            && (player.isOnFire() || player.getRemainingFireTicks() > 0 || player.isInLava())) {
         end(player, "fonte");
         return;
      }
      if (remaining <= 1) {
         end(player, "echeance");
         return;
      }
      state.left=remaining-1;
   }
}
