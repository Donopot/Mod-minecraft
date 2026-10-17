package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.ope.DevilFruitOpeConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * <b>Découpe temporaire</b> — Ope Ope no Mi (FRUIT-OPE_OPE-05).
 *
 * <p>Le plan : « Définir une <b>découpe temporaire</b> ou un effet équivalent <b>sans corruption des
 * entités</b> », critère : « Les cibles sont <b>restaurées</b> après <b>expiration, mort ou suppression
 * du pouvoir</b> ».</p>
 *
 * <h2>Ce que « sans corruption » impose</h2>
 * <p>La facilité serait de supprimer les membres, les objets ou des composants de l'entité : on
 * obtiendrait une découpe spectaculaire et une <b>entité abîmée</b> que rien ne sait remettre dans son
 * état d'origine. L'effet retenu porte donc sur <b>une seule chose</b>, réversible par construction :
 * les <b>membres qui frappent</b> ne portent plus de coup. Concrètement, la valeur <b>effective</b> de
 * {@code ATTACK_DAMAGE} est ramenée à zéro par un <b>modificateur</b> — jamais en modifiant la valeur de
 * base. C'est la différence entre « j'ai ajouté quelque chose que je peux retirer » et « j'ai écrasé la
 * valeur de quelqu'un d'autre ».</p>
 *
 * <h2>L'ordre des opérations <i>est</i> la garantie</h2>
 * <p>On demande d'abord au <b>moteur</b> d'accepter le marqueur de section ({@code addEffect} →
 * booléen) et <b>seulement s'il l'accepte</b> on touche à l'attribut. L'inverse retirerait la force de
 * frappe à une cible qui a <b>résisté</b> — l'exact comportement que le contrat interdit.</p>
 *
 * <h2>Toutes les fins sont prévues</h2>
 * <p>Comme tout maintien du mod : <b>échéance</b> (durée bornée, donc auto-réparatrice même si le
 * registre s'égare), <b>mort</b> de la cible, <b>suppression</b> du pouvoir chez le porteur,
 * <b>déconnexion</b> du porteur, <b>retrait</b> du Fruit (reset central). Après restauration, la valeur
 * de frappe est <b>recomparée</b> à celle d'avant : le rapport distingue les restaurations exactes des
 * inexactes, pour qu'un « c'est restauré » ne soit jamais une parole.</p>
 */
public final class Amputation {

   private static final Identifier MOD_SECTION =
      Identifier.fromNamespaceAndPath("minepiece", "section_temporaire");

   /** L'état d'une cible sectionnée. */
   private static final class Section {
      final ServerPlayer reference;final LivingEntity target;final ServerLevel world;MobEffectInstance marker;
      final UUID owner;
      final UUID cible;
      /** Valeur de frappe AVANT la section — la référence de la restauration. */
      final double frappeAvant;
      /** Le modificateur a-t-il réellement été posé ? (une cible peut refuser l'effet) */
      final boolean modificateurPose;
      int ttl;

      Section(ServerPlayer reference,LivingEntity target,UUID owner, UUID cible, double frappeAvant, boolean modificateurPose, int ttl) {
         this.reference=reference;this.target=target;this.world=(ServerLevel)target.level();
         this.owner = owner;
         this.cible = cible;
         this.frappeAvant = frappeAvant;
         this.modificateurPose = modificateurPose;
         this.ttl = ttl;
      }
   }

   private static final Map<UUID, Section> SECTIONS = new HashMap<>();

   /** Références revalidées : les entités virtuelles échappent aux recherches par identifiant. */
   private static final Map<UUID, LivingEntity> REFS = new HashMap<>();
   private static final Map<UUID, ServerPlayer> OWNER_REFS = new HashMap<>();
   private static final Map<UUID, String> LAST_RESTORE = new HashMap<>();

   private static int coupees;
   private static int refusees;
   private static int restaurees;
   private static int restaurationsExactes;
   private static int restaurationsInexactes;

   private Amputation() {
   }

   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         tick();
         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            if(!OWNER_REFS.containsKey(player.getUUID()))OWNER_REFS.put(player.getUUID(),player);
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            restoreAllFrom(player, "deconnexion");
            OWNER_REFS.remove(player.getUUID());
         }
      });
   }

   // ============================ pose ============================

   /**
    * Sectionne temporairement une cible : ses coups ne portent plus pendant la durée prévue.
    *
    * @return vrai si la section a été appliquée
    */
   public static boolean amputate(ServerLevel level, ServerPlayer caster, LivingEntity cible) {
      if (caster == null || cible == null || !BatchRules.usable(caster,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE) || !BatchRules.target(caster,cible) || cible.isInvulnerable()) {
         refusees++;
         return false;
      }
      if (!OpeRoom.contains(caster, cible)) {
         refusees++;
         return false; // hors ROOM : la technique est refusée (FRUIT-OPE_OPE-03)
      }
      int ttl = DevilFruitOpeConfig.amputationDurationTicks();
      Section existante = SECTIONS.get(cible.getUUID());
      if (existante != null) {
         // Déjà sectionnée : on PROLONGE, on ne raccourcit jamais un état en cours et on ne pose pas
         // un second modificateur.
         if(existante.reference!=caster || existante.target!=cible || existante.world!=level)return false;
         existante.ttl = Math.max(existante.ttl, ttl);
         return true;
      }
      if (countFor(caster.getUUID()) >= DevilFruitOpeConfig.amputationMaxTargets()) {
         refusees++;
         return false; // plafond de cibles simultanées
      }
      // 1. La porte du MOTEUR d'abord. Une cible qui refuse l'effet n'est pas sectionnée du tout.
      if(cible.hasEffect(MinepieceModMobEffects.AMPUTATED))return false;
      boolean accepte = cible.addEffect(new MobEffectInstance(MinepieceModMobEffects.AMPUTATED, ttl, 0, false, false, true));
      if (!accepte) {
         refusees++;
         return false;
      }
      // 2. L'attribut, seulement maintenant. On AJOUTE un modificateur, la base n'est pas touchée.
      AttributeInstance attaque = cible.getAttribute(Attributes.ATTACK_DAMAGE);
      double frappeAvant = 0.0;
      boolean pose = false;
      if (attaque != null) {
         frappeAvant = attaque.getValue();
         attaque.removeModifier(MOD_SECTION);
         attaque.addTransientModifier(new AttributeModifier(MOD_SECTION, -1,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
         pose = true;
      }
      SECTIONS.put(cible.getUUID(), new Section(caster,cible,caster.getUUID(), cible.getUUID(), frappeAvant, pose, ttl));
      SECTIONS.get(cible.getUUID()).marker=cible.getEffect(MinepieceModMobEffects.AMPUTATED);
      REFS.put(cible.getUUID(), cible);
      OWNER_REFS.put(caster.getUUID(), caster);
      coupees++;
      return true;
   }

   // ============================ état ============================

   /** La cible est-elle actuellement sectionnée ? */
   public static boolean isCut(LivingEntity cible) {
      return cible != null && SECTIONS.containsKey(cible.getUUID());
   }

   /** Nombre de cibles sectionnées (observable). */
   public static int cutCount() {
      return SECTIONS.size();
   }

   /** Durée restante de la section, ou 0. */
   public static int remainingTicks(LivingEntity cible) {
      Section s = cible == null ? null : SECTIONS.get(cible.getUUID());
      return s == null ? 0 : s.ttl;
   }

   /** Valeur de frappe mémorisée avant la section (référence de restauration). */
   public static double attackBefore(LivingEntity cible) {
      Section s = cible == null ? null : SECTIONS.get(cible.getUUID());
      return s == null ? -1.0 : s.frappeAvant;
   }

   /** Raison de la dernière restauration pour cette cible. */
   public static String lastRestoreReason(LivingEntity cible) {
      return cible == null ? "aucune" : LAST_RESTORE.getOrDefault(cible.getUUID(), "aucune");
   }

   private static int countFor(UUID owner) {
      int n = 0;
      for (Section s : SECTIONS.values()) {
         if (s.owner.equals(owner)) {
            n++;
         }
      }
      return n;
   }

   // ============================ restauration ============================

   /**
    * Restaure une cible : retire le modificateur, retire le marqueur, et <b>vérifie</b> que la valeur de
    * frappe est bien revenue à celle d'avant.
    *
    * @return vrai si quelque chose a été restauré
    */
   public static boolean restore(LivingEntity cible, String reason) {
      if (cible == null) {
         return false;
      }
      return restoreByUuid(cible.getUUID(), cible, reason);
   }

   private static boolean restoreByUuid(UUID identifiant, LivingEntity reference, String reason) {
      Section s = SECTIONS.remove(identifiant);
      if (s == null) {
         return false;
      }
      LivingEntity entite = reference != null && !reference.isRemoved() ? reference : REFS.get(identifiant);
      if (entite != null && !entite.isRemoved()) {
         if (s.modificateurPose) {
            AttributeInstance attaque = entite.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attaque != null) {
               attaque.removeModifier(MOD_SECTION);
               // Vérification APRÈS retrait : on ne déclare pas « restauré », on le CONSTATE.
               if (Math.abs(attaque.getValue() - s.frappeAvant) < 0.001) {
                  restaurationsExactes++;
               } else {
                  restaurationsInexactes++;
               }
            }
         }
         if(entite.getEffect(MinepieceModMobEffects.AMPUTATED)==s.marker)entite.removeEffect(MinepieceModMobEffects.AMPUTATED);
      }
      REFS.remove(identifiant);
      LAST_RESTORE.put(identifiant, reason);
      restaurees++;
      return true;
   }

   /** Restaure toutes les sections posées par ce porteur. */
   public static void restoreAllFrom(ServerPlayer owner, String reason) {
      if (owner == null) {
         return;
      }
      for (UUID cible : new ArrayList<>(SECTIONS.keySet())) {
         Section s = SECTIONS.get(cible);
         if (s != null && s.owner.equals(owner.getUUID())) {
            restoreByUuid(cible, REFS.get(cible), reason);
         }
      }
   }

   /** Restaure tout — utilisé par le reset de Fruit (FRUIT-RESET-01). */
   public static void clearAll() {
      for (UUID cible : new ArrayList<>(SECTIONS.keySet())) {
         restoreByUuid(cible, REFS.get(cible), "reset");
      }
   }

   /** Fait avancer les sections : échéance, mort de la cible, disparition du porteur. */
   private static void tick() {
      List<UUID> aRestaurer = new ArrayList<>();
      Map<UUID, String> raisons = new HashMap<>();
      for (Map.Entry<UUID, Section> entree : SECTIONS.entrySet()) {
         Section s = entree.getValue();
         s.ttl--;
         LivingEntity entite = REFS.get(entree.getKey());
         if (s.ttl <= 0) {
            aRestaurer.add(entree.getKey());
            raisons.put(entree.getKey(), "echeance");
            continue;
         }
         if (entite == null || entite.isRemoved() || !entite.isAlive()) {
            aRestaurer.add(entree.getKey());
            raisons.put(entree.getKey(), "mort");
            continue;
         }
         ServerPlayer porteur = OWNER_REFS.get(s.owner);
         if (porteur!=s.reference || entite.level()!=s.world || !BatchRules.usable(s.reference,s.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE)) {
            aRestaurer.add(entree.getKey());
            raisons.put(entree.getKey(), "suppression");
         }
      }
      for (UUID cible : aRestaurer) {
         restoreByUuid(cible, REFS.get(cible), raisons.getOrDefault(cible, "echeance"));
      }
   }

   // ============================ diagnostic ============================

   /**
    * Rapport de diagnostic : « ça n'a pas marché » doit être lisible sans reconstruire le mod.
    *
    * <p>{@code coupees}, {@code refusees}, {@code restaurees}, et surtout
    * {@code restaurations_inexactes} — le compteur qui distingue « j'ai retiré le modificateur » de
    * « la valeur est revenue exactement à celle d'avant ».</p>
    */
   public static String amputationReport() {
      return "coupees=" + coupees + " refusees=" + refusees + " restaurees=" + restaurees
         + " restaurations_exactes=" + restaurationsExactes
         + " restaurations_inexactes=" + restaurationsInexactes;
   }

   /** Nombre de restaurations dont la valeur de frappe est revenue EXACTEMENT à celle d'avant. */
   public static int exactRestorations() {
      return restaurationsExactes;
   }

   /** Nombre de restaurations INEXACTES — doit rester à zéro : c'est le compteur anti-corruption. */
   public static int inexactRestorations() {
      return restaurationsInexactes;
   }

   /** Nombre de sections posées. */
   public static int cutTotal() {
      return coupees;
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      coupees = 0;
      refusees = 0;
      restaurees = 0;
      restaurationsExactes = 0;
      restaurationsInexactes = 0;
   }
}
