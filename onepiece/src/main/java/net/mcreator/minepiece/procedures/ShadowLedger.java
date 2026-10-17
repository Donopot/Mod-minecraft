package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.kage.DevilFruitKageConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * <b>Le coffre d'ombres</b> — Kage Kage no Mi (FRUIT-KAGE_KAGE-03).
 *
 * <p>Le plan : « créer des <b>ombres temporaires</b> avec <b>règles d'acquisition et restitution</b> »,
 * critère « une ombre <b>perdue est restituée dans tous les cas de fin prévus</b> ».</p>
 *
 * <h2>Pourquoi ce service existe : une ombre volée appartient encore à quelqu'un</h2>
 * <p>C'est la différence de nature avec tout le reste du projet. Une flamme qu'on éteint, une glace
 * qu'on fond : personne ne les réclame. Une <b>ombre prise</b> est retirée à un <b>autre joueur</b> —
 * elle a un propriétaire légitime, absent de la décision, et qui n'a aucun moyen de la reprendre. Si le
 * mécanisme oublie de la rendre, ce joueur reste <b>diminué pour toujours</b> : sa pénalité survit à la
 * mort du voleur, à sa déconnexion, au retrait du Fruit, au redémarrage du serveur. Aucun message, aucun
 * recours — il ne saura même pas pourquoi.</p>
 *
 * <p>Le critère est donc volontairement écrit au superlatif : « dans <b>tous</b> les cas de fin prévus ».
 * Chaque cas est <b>énuméré</b>, et chacun a son test. Un oubli dans cette liste est un bug silencieux
 * qui ne se manifeste que des heures plus tard, chez un joueur qui n'a rien fait.</p>
 *
 * <h2>Les cas de fin, énumérés (aucun n'est implicite)</h2>
 * <table border="1">
 *   <caption>Chaque ligne a son test (section Y)</caption>
 *   <tr><th>Cas</th><th>Ce qui est rendu</th></tr>
 *   <tr><td><code>duree</code></td><td>fin de Shadow Asgard : le pouvoir retombe</td></tr>
 *   <tr><td><code>retrait</code></td><td>le porteur perd le Fruit</td></tr>
 *   <tr><td><code>suppression</code></td><td>pouvoir scellé (mer, prison)</td></tr>
 *   <tr><td><code>mort</code></td><td>le voleur meurt</td></tr>
 *   <tr><td><code>deconnexion</code></td><td>le voleur quitte le serveur</td></tr>
 *   <tr><td><code>cible-perdue</code></td><td>la victime meurt ou disparaît</td></tr>
 *   <tr><td><code>reset</code></td><td>reset global / fin de test</td></tr>
 * </table>
 *
 * <h2>On ne retire jamais un état qu'on n'a pas posé</h2>
 * <p>La victime porte un marqueur dédié (<code>shadowless</code>) et une pénalité d'armure portée par
 * <b>nos</b> modificateurs identifiés par ressource. La restitution retire <b>nominativement</b> ce que
 * nous avons posé — jamais « les modificateurs d'armure », jamais un effet vanille. Une victime qui
 * aurait par ailleurs une potion ou un équipement ne perd rien de ce qu'elle doit à d'autres.</p>
 */
public final class ShadowLedger {

   private static final Identifier MOD_VICTIM_ARMOR =
      Identifier.fromNamespaceAndPath("minepiece", "shadowless_armor");
   private static final Identifier MOD_HOLDER_ATTACK =
      Identifier.fromNamespaceAndPath("minepiece", "shadow_asgard_attack");

   /** Voleur → ombres qu'il détient (une par victime, ordre d'acquisition conservé). */
   private static final Map<UUID, Set<UUID>> HELD = new HashMap<>();

   /** Victime → voleur (pour retrouver à qui rendre). */
   private static final Map<UUID, UUID> OWNER_OF = new HashMap<>();

   /**
    * Cache de références vers les victimes.
    *
    * <p><b>Pourquoi ce cache est nécessaire et pourquoi l'UUID reste la source de vérité.</b> Certains
    * joueurs — dont les {@code FakePlayer} des preuves — sont <b>virtuels</b> : Fabric les construit
    * sans les inscrire dans la liste d'entités du monde, donc <b>aucune</b> recherche par identifiant ne
    * les retrouve. Sans ce cache, la restitution était comptée comme faite <b>sans retirer le marqueur
    * ni la pénalité</b> : la victime restait dépouillée alors que tout indiquait le contraire.</p>
    *
    * <p>Le cache est <b>revalidé</b> à chaque usage ({@code isRemoved()}, {@code isAlive()}) et n'est
    * qu'un raccourci : dès qu'il est périmé, on retombe sur les recherches par identifiant. Et si la
    * comptabilité disparaissait entièrement (redémarrage du serveur), l'état de la victime est <b>borné
    * par sa durée</b> — elle se rétablit d'elle-même, ce qui est la seule garantie qui ne dépende pas de
    * notre bon vouloir.</p>
    */
   private static final Map<UUID, LivingEntity> VICTIM_REFS = new HashMap<>();

   /** Raison de la dernière restitution, par voleur (lisible par les preuves). */
   private static final Map<UUID, String> LAST_RESTORE = new HashMap<>();

   /** Nombre de restitutions effectuées depuis le démarrage (observable). */
   private static int restitutions = 0;

   private ShadowLedger() {
   }

   /** Branche l'entretien, la mort, la déconnexion et la perte de cible. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            tick(player);
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            restoreAll(player, "deconnexion");
         }
      });
   }

   /** Combien d'ombres ce joueur détient-il ? */
   public static int heldCount(ServerPlayer holder) {
      Set<UUID> set = holder == null ? null : HELD.get(holder.getUUID());
      return set == null ? 0 : set.size();
   }

   /** Les victimes dont ce joueur détient l'ombre. */
   public static List<UUID> victims(ServerPlayer holder) {
      Set<UUID> set = holder == null ? null : HELD.get(holder.getUUID());
      return set == null ? List.of() : List.copyOf(set);
   }

   /** L'ombre de cette victime est-elle détenue ? */
   public static boolean isShadowless(UUID victim) {
      return victim != null && OWNER_OF.containsKey(victim);
   }

   /** Le détenteur de l'ombre d'une victime, ou {@code null}. */
   public static UUID holderOf(UUID victim) {
      return victim == null ? null : OWNER_OF.get(victim);
   }

   /** Raison de la dernière restitution. */
   public static String lastRestoreReason(ServerPlayer holder) {
      return holder == null ? "aucune" : LAST_RESTORE.getOrDefault(holder.getUUID(), "aucune");
   }

   /** Nombre total de restitutions (observable). */
   public static int restitutionCount() {
      return restitutions;
   }

   /**
    * <b>Acquisition</b> : prend l'ombre d'une victime vivante et la range dans le coffre.
    *
    * <p>Refus (sans rien consommer) : victime déjà dépouillée, plafond atteint, victime non éligible.
    * Le plafond est ce qui rend la décision finie — sans lui, l'avantage serait proportionnel au nombre
    * de cibles présentes, et le pouvoir n'aurait plus de forme.</p>
    *
    * @return vrai si l'ombre a été prise
    */
   public static boolean steal(ServerPlayer holder, LivingEntity victim) {
      if (holder == null || victim == null || holder == victim || !victim.isAlive()) {
         return false;
      }
      if (!holder.isAlive() || DevilFruitPowerLogic.isSuppressed(holder)) {
         return false;
      }
      if (OWNER_OF.containsKey(victim.getUUID())) {
         return false; // déjà dépouillée : on ne prend pas deux fois
      }
      // ALLIANCES : la règle vit ICI, à l'endroit de l'effet — pas seulement chez l'appelant, sinon elle
      // est contournable. On n'appauvrit ni ses propres invocations, ni ses alliés : sans cette règle,
      // un porteur au milieu de son groupe se renforcerait en appauvrissant les siens.
      if (victim instanceof SummonEntity summon && DevilFruitSummons.isOwnedBy(summon, holder)) {
         return false;
      }
      if (holder.isAlliedTo(victim)) {
         return false;
      }
      Set<UUID> set = HELD.computeIfAbsent(holder.getUUID(), k -> new LinkedHashSet<>());
      if (set.size() >= DevilFruitKageConfig.shadowCap()) {
         return false; // plafond : refus sans rien consommer
      }
      set.add(victim.getUUID());
      OWNER_OF.put(victim.getUUID(), holder.getUUID());
      VICTIM_REFS.put(victim.getUUID(), victim);
      markVictim(victim);
      applyHolderBonus(holder);
      if (holder.level() instanceof ServerLevel level) {
         level.playSound(null, victim.blockPosition(), SoundEvents.SOUL_ESCAPE.value(),
            SoundSource.PLAYERS, 0.8F, 0.6F);
         level.sendParticles(MinepieceModParticles.DARK, victim.getX(), victim.getY() + 1.0, victim.getZ(),
            25, 0.4, 0.7, 0.4, 0.02);
      }
      return true;
   }

   /**
    * <b>Restitution d'une ombre</b> — le chemin emprunté par l'expiration d'un Doppelman.
    *
    * @return vrai si une ombre a été rendue
    */
   public static boolean restore(ServerPlayer holder, UUID victimId, String reason) {
      if (holder == null || victimId == null) {
         return false;
      }
      Set<UUID> set = HELD.get(holder.getUUID());
      if (set == null || !set.remove(victimId)) {
         return false;
      }
      if (set.isEmpty()) {
         HELD.remove(holder.getUUID());
      }
      OWNER_OF.remove(victimId);
      unmarkVictim(victimId);
      VICTIM_REFS.remove(victimId);
      applyHolderBonus(holder);
      LAST_RESTORE.put(holder.getUUID(), reason);
      restitutions++;
      return true;
   }

   /**
    * <b>Restitution totale</b> : rend TOUTES les ombres détenues.
    *
    * <p>C'est le chemin de tous les cas de fin. Il est appelé par l'entretien (échéance, mort,
    * suppression, victime perdue) et par la déconnexion, et exposé pour le retrait du Fruit et le reset.</p>
    *
    * @return nombre d'ombres rendues
    */
   public static int restoreAll(ServerPlayer holder, String reason) {
      if (holder == null) {
         return 0;
      }
      Set<UUID> set = HELD.remove(holder.getUUID());
      if (set == null || set.isEmpty()) {
         return 0;
      }
      int n = 0;
      for (UUID victimId : new ArrayList<>(set)) {
         OWNER_OF.remove(victimId);
         unmarkVictim(victimId);
         VICTIM_REFS.remove(victimId);
         n++;
         restitutions++;
      }
      LAST_RESTORE.put(holder.getUUID(), reason);
      removeHolderBonus(holder);
      return n;
   }

   /**
    * Rend <b>toutes</b> les ombres détenues (reset global, fin de test).
    *
    * <p><b>Le balayage se fait par VICTIME, pas par voleur.</b> C'est une correction de fond, et non un
    * détail : un voleur n'est pas toujours résolvable (les joueurs virtuels ne figurent dans aucune
    * liste), et un balayage par voleur laissait alors des entrées <b>sales</b> — la victime restait
    * marquée, et surtout la comptabilité refusait ensuite tout nouveau vol sur elle, indéfiniment. Une
    * victime, elle, est toujours joignable par sa référence : c'est donc par elle qu'il faut balayer.</p>
    *
    * @return nombre d'ombres rendues
    */
   public static int restoreEverything(String reason) {
      int n = 0;
      for (UUID victimId : new ArrayList<>(OWNER_OF.keySet())) {
         UUID holderId = OWNER_OF.get(victimId);
         // Retrait inconditionnel, sans dépendre du voleur.
         unmarkVictim(victimId);
         VICTIM_REFS.remove(victimId);
         OWNER_OF.remove(victimId);
         if (holderId != null) {
            LAST_RESTORE.put(holderId, reason);
         }
         n++;
         restitutions++;
      }
      // Retrait des bonus des voleurs encore joignables, puis oubli complet.
      for (UUID holderId : new ArrayList<>(HELD.keySet())) {
         ServerPlayer holder = br.com.oneminepiece.opworldlike.OmpWorld.currentServer == null ? null
            : br.com.oneminepiece.opworldlike.OmpWorld.currentServer.getPlayerList().getPlayer(holderId);
         if (holder != null) {
            removeHolderBonus(holder);
         }
      }
      HELD.clear();
      return n;
   }

   /** Le porteur a-t-il l'état « sans ombre » ? (lecture, pour les preuves) */
   public static boolean victimMarked(LivingEntity victim) {
      return victim != null && victim.hasEffect(MinepieceModMobEffects.SHADOWLESS);
   }

   /**
    * Entretien : échéance, mort, suppression, victime perdue. Exposé pour les tests.
    *
    * <p>Une ombre dont la victime a disparu n'est plus une ombre : la garder serait conserver une
    * créance sur quelqu'un qui n'existe plus, et bloquer une place du plafond pour rien.</p>
    */
   public static void tick(ServerPlayer holder) {
      Set<UUID> set = HELD.get(holder.getUUID());
      if (set == null || set.isEmpty()) {
         return;
      }
      if (!holder.isAlive()) {
         restoreAll(holder, "mort");
         return;
      }
      if (DevilFruitPowerLogic.isSuppressed(holder)) {
         restoreAll(holder, "suppression");
         return;
      }
      // Une victime disparue ou morte libère sa place.
      List<UUID> perdues = new ArrayList<>();
      for (UUID victimId : set) {
         LivingEntity victim = find(victimId);
         if (victim == null || !victim.isAlive()) {
            perdues.add(victimId);
         } else if (!victimMarked(victim)) {
            // Le marqueur a disparu (expiration naturelle de l'état) : on ne détient plus rien.
            perdues.add(victimId);
         }
      }
      for (UUID victimId : perdues) {
         restore(holder, victimId, "cible-perdue");
      }
   }

   /** Applique le marqueur et la pénalité à la victime — uniquement nos propres états. */
   private static void markVictim(LivingEntity victim) {
      victim.addEffect(new MobEffectInstance(MinepieceModMobEffects.SHADOWLESS,
         DevilFruitKageConfig.shadowlessDurationTicks(), 0, false, false, true));
      AttributeInstance armor = victim.getAttribute(Attributes.ARMOR);
      if (armor != null) {
         armor.removeModifier(MOD_VICTIM_ARMOR);
         armor.addTransientModifier(new AttributeModifier(MOD_VICTIM_ARMOR,
            -DevilFruitKageConfig.shadowlessArmorPenalty(), AttributeModifier.Operation.ADD_VALUE));
      }
   }

   /** Retire EXACTEMENT ce que {@link #markVictim} a posé, par identifiant de ressource. */
   private static void unmarkVictim(UUID victimId) {
      LivingEntity victim = find(victimId);
      if (victim == null) {
         return;
      }
      if (MinepieceModMobEffects.SHADOWLESS != null) {
         victim.removeEffect(MinepieceModMobEffects.SHADOWLESS);
      }
      AttributeInstance armor = victim.getAttribute(Attributes.ARMOR);
      if (armor != null) {
         armor.removeModifier(MOD_VICTIM_ARMOR);
      }
   }

   /** Bonus du voleur : proportionnel au nombre d'ombres, porté par notre propre modificateur. */
   private static void applyHolderBonus(ServerPlayer holder) {
      int n = heldCount(holder);
      AttributeInstance attack = holder.getAttribute(Attributes.ATTACK_DAMAGE);
      if (attack == null) {
         return;
      }
      attack.removeModifier(MOD_HOLDER_ATTACK);
      double bonus = DevilFruitKageConfig.attackPerShadow() * n;
      if (bonus > 0.0) {
         attack.addTransientModifier(new AttributeModifier(MOD_HOLDER_ATTACK, bonus,
            AttributeModifier.Operation.ADD_VALUE));
      }
   }

   /** Retire le bonus du voleur (et lui seul). */
   private static void removeHolderBonus(ServerPlayer holder) {
      AttributeInstance attack = holder.getAttribute(Attributes.ATTACK_DAMAGE);
      if (attack != null) {
         attack.removeModifier(MOD_HOLDER_ATTACK);
      }
   }

   /**
    * Résout une entité par identifiant, dans toutes les dimensions chargées.
    *
    * <p><b>Trois passes, et ce n'est pas du zèle.</b> La résolution par identifiant ne suffit pas :
    * un joueur — dont le {@code FakePlayer} des preuves — n'est pas toujours trouvé par le registre
    * d'entités. Le service comptait alors la restitution comme faite <b>sans retirer le marqueur ni la
    * pénalité</b> : la victime restait dépouillée alors que tout le monde croyait l'ombre rendue. Un
    * nettoyage qui échoue en silence est pire qu'un nettoyage absent, car il donne le sentiment du
    * contraire.</p>
    */
   private static LivingEntity find(UUID id) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return null;
      }
      // 0. la référence gardée à l'acquisition — indispensable pour les joueurs virtuels, et revalidée
      //    pour ne jamais rendre un état à une entité qui n'est plus là.
      LivingEntity connu = VICTIM_REFS.get(id);
      if (connu != null && !connu.isRemoved() && connu.isAlive()) {
         return connu;
      }
      // 1. joueurs connectés / connus
      ServerPlayer connecte = server.getPlayerList().getPlayer(id);
      if (connecte != null) {
         return connecte;
      }
      for (ServerLevel level : server.getAllLevels()) {
         // 2. entités par identifiant (créatures, invocations)
         if (level.getEntity(id) instanceof LivingEntity living) {
            return living;
         }
      }
      // 3. balayage des joueurs du monde : couvre les porteurs absents du registre d'entités.
      for (ServerLevel level : server.getAllLevels()) {
         for (ServerPlayer candidat : level.getEntitiesOfClass(ServerPlayer.class,
               new net.minecraft.world.phys.AABB(-30000000, level.getMinY(), -30000000,
                  30000000, level.getMaxY(), 30000000))) {
            if (candidat.getUUID().equals(id)) {
               return candidat;
            }
         }
      }
      return null;
   }
}
