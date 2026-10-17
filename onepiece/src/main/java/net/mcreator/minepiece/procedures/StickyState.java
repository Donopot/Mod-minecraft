package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.mochi.DevilFruitMochiConfig;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * <b>Mochi collant</b> — Mochi Mochi no Mi (FRUIT-MOCHI_MOCHI-04/06).
 *
 * <p>Le plan : « ajouter un <b>état collant</b> avec <b>libération</b> et <b>durée maximale</b> »,
 * critère « une cible touchée est <b>ralentie ou immobilisée</b> selon le contrat puis
 * <b>libérée</b> ».</p>
 *
 * <h2>La contrainte graduée, puis la libération</h2>
 * <p>Coller quelqu'un n'est pas le tuer : c'est <b>décider à sa place</b> qu'il ne se déplacera pas. À ce
 * titre, deux règles s'imposent. D'abord la <b>gradation</b> : ralentissement d'abord, immobilisation
 * seulement au-delà d'un délai — une contrainte brutale et immédiate laisserait la victime sans aucune
 * marge. Ensuite la <b>libération</b>, qui doit être <b>inconditionnelle</b> : la durée est bornée, donc
 * même si le porteur se déconnecte, meurt, change de dimension ou perd son Fruit, la cible se décolle
 * <b>d'elle-même</b>. C'est la seule garantie qui ne dépende pas du bon vouloir de quelqu'un.</p>
 *
 * <h2>On ne retire que ce qu'on a posé</h2>
 * <p>Marqueur dédié et modificateur identifié par ressource, retirés nominativement. Une cible qui a par
 * ailleurs une potion ou un équipement <b>garde tout</b>.</p>
 */
public final class StickyState {

   private static final Identifier MOD_SLOW =
      Identifier.fromNamespaceAndPath("minepiece", "mochi_sticky_slow");

   /** Cible → ticks restants. */
   private static final Map<UUID, Integer> STICKY = new HashMap<>();

   /** Cible → porteur d'origine (pour la traçabilité). */
   private static final Map<UUID, UUID> SOURCE = new HashMap<>();

   /** Raison de la dernière libération, par cible (lisible par les preuves). */
   private static final Map<UUID, String> LAST_RELEASE = new HashMap<>();

   /** Références revalidées vers les cibles — les joueurs virtuels échappent aux recherches par id. */
   private static final Map<UUID, LivingEntity> REFS = new HashMap<>();


   private record Binding(LivingEntity owner,net.minecraft.server.level.ServerLevel level,net.minecraft.world.item.Item fruit){}
   private static final Map<UUID,Binding> BINDINGS=new HashMap<>();

   private StickyState() {
   }

   /** Branche l'entretien et la libération à la déconnexion du PORTEUR. */
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->releaseEverything("arret"));
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            tickHolder(player);
         }
         tickAll();
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            // Le porteur s'en va : ce qu'il collait doit se décoller.
            releaseAllFrom(player, "deconnexion");
         }
      });
   }

   /** Colle une cible (ou renouvelle sans jamais dépasser la durée maximale). */
   public static boolean stick(LivingEntity cible, LivingEntity source) {
      if (cible == null || !cible.isAlive() || cible instanceof ServerPlayer p && p.isSpectator()) {
         return false;
      }
      if (source != null && (cible == source || source.isAlliedTo(cible))) {
         return false; // on ne colle pas les siens
      }
      if(source!=null && (source.level()!=cible.level() || !source.isAlive() || source.isRemoved() || source instanceof ServerPlayer player && !BatchRules.target(player,cible)))return false;
      if(STICKY.containsKey(cible.getUUID()))return REFS.get(cible.getUUID())==cible;
      if(source!=null)BINDINGS.put(cible.getUUID(),new Binding(source,(ServerLevel)source.level(),source instanceof ServerPlayer player?DevilFruitPowerLogic.getDevilFruit(player).getItem():null));
      STICKY.put(cible.getUUID(), DevilFruitMochiConfig.stickyDurationTicks());
      if (source != null) {
         SOURCE.put(cible.getUUID(), source.getUUID());
      }
      REFS.put(cible.getUUID(), cible);
      apply(cible, 0);
      if (cible.level() instanceof ServerLevel level) {
         level.sendParticles(ParticleTypes.CLOUD, cible.getX(), cible.getY() + 0.6, cible.getZ(),
            18, 0.5, 0.8, 0.5, 0.02);
         level.playSound(null, cible.blockPosition(), SoundEvents.SLIME_BLOCK_PLACE,
            SoundSource.PLAYERS, 0.9F, 0.8F);
      }
      return true;
   }

   /** La cible est-elle collante ? */
   public static boolean isSticky(LivingEntity cible) {
      return cible != null && REFS.get(cible.getUUID())==cible && STICKY.containsKey(cible.getUUID());
   }

   /** Ticks restants. */
   public static int remaining(LivingEntity cible) {
      return cible == null ? 0 : STICKY.getOrDefault(cible.getUUID(), 0);
   }

   /** La cible est-elle immobilisée (au-delà du délai de gradation) ? */
   public static boolean isImmobilized(LivingEntity cible) {
      if (!isSticky(cible)) {
         return false;
      }
      return DevilFruitMochiConfig.stickyDurationTicks() - remaining(cible)
         >= DevilFruitMochiConfig.stickyImmobilizeAfterTicks();
   }

   /** Raison de la dernière libération, ou « aucune ». */
   public static String lastReleaseReason(LivingEntity cible) {
      return cible == null ? "aucune" : LAST_RELEASE.getOrDefault(cible.getUUID(), "aucune");
   }

   /** Nombre de cibles collées (observable). */
   public static int stickyCount() {
      return STICKY.size();
   }

   /**
    * Libère une cible. <b>Inconditionnelle</b> : elle retire le marqueur et le modificateur
    * <b>nominativement</b>, sans jamais toucher aux états externes.
    */
   public static void release(LivingEntity cible, String reason) {
      if (cible == null || STICKY.remove(cible.getUUID()) == null) {
         return;
      }
      LAST_RELEASE.put(cible.getUUID(), reason);
      SOURCE.remove(cible.getUUID());
      REFS.remove(cible.getUUID());
      BINDINGS.remove(cible.getUUID());
      remove(cible);
   }

   /** Libère tout ce qu'un porteur avait collé (mort, retrait, déconnexion, fin de test). */
   public static int releaseAllFrom(LivingEntity holder, String reason) {
      if (holder == null) {
         return 0;
      }
      int n = 0;
      for (UUID cibleId : new ArrayList<>(SOURCE.keySet())) {
         if (holder.getUUID().equals(SOURCE.get(cibleId))) {
            release(resolve(cibleId), reason);
            n++;
         }
      }
      return n;
   }

   /** Libère toutes les cibles (reset, fin de test). */
   public static int releaseEverything(String reason) {
      int n = 0;
      for (UUID id : new ArrayList<>(STICKY.keySet())) {
         release(resolve(id), reason);
         n++;
      }
      STICKY.clear();
      BINDINGS.clear();REFS.clear();
      SOURCE.clear();
      return n;
   }

   /** Entretien du porteur : ce qu'il a collé se libère s'il meurt ou si son pouvoir est scellé. */
   public static void tickHolder(LivingEntity holder) {
      if (holder == null) {
         return;
      }
      if (!holder.isAlive()) {
         releaseAllFrom(holder, "mort");
         return;
      }
      if (holder instanceof ServerPlayer player && DevilFruitPowerLogic.isSuppressed(player)) {
         releaseAllFrom(holder, "suppression");
      }
   }

   /** Entretien des cibles : échéance de la durée MAXIMALE. */
   public static void tickAll() {
      for (UUID id : new ArrayList<>(STICKY.keySet())) {
         Integer reste = STICKY.get(id);
         if (reste == null) {
            continue;
         }
         LivingEntity cible = resolve(id);
         Binding binding=BINDINGS.get(id);
         if(binding!=null && (binding.owner.isRemoved() || !binding.owner.isAlive() || binding.owner.level()!=binding.level || cible==null || cible.isRemoved() || cible.level()!=binding.level || binding.owner instanceof ServerPlayer player && (DevilFruitPowerLogic.isSuppressed(player) || !DevilFruitPowerLogic.getDevilFruit(player).is(binding.fruit)))){
            if(cible!=null)release(cible,"retrait");else {STICKY.remove(id);SOURCE.remove(id);BINDINGS.remove(id);REFS.remove(id);}continue;
         }

         if (cible == null || !cible.isAlive() || cible.isRemoved()) {
            release(cible, "cible-perdue");
            STICKY.remove(id);
            continue;
         }
         if (reste <= 1) {
            release(cible, "echeance");
            continue;
         }
         STICKY.put(id, reste - 1);
         // Gradation : on réapplique au passage du seuil d'immobilisation.
         if (DevilFruitMochiConfig.stickyDurationTicks() - (reste - 1)
               == DevilFruitMochiConfig.stickyImmobilizeAfterTicks()) {
            apply(cible, DevilFruitMochiConfig.stickyImmobilizeAfterTicks());
         }
      }
   }

   /** Applique ralentissement puis immobilisation — jamais les deux à la fois. */
   private static void apply(LivingEntity cible, int elapsed) {
      AttributeInstance speed = cible.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed == null) {
         return;
      }
      speed.removeModifier(MOD_SLOW);
      boolean immobile = elapsed >= DevilFruitMochiConfig.stickyImmobilizeAfterTicks();
      // Immobilisation : −100 % (facteur 1 → 0). Ralentissement : −60 %.
      double facteur = immobile ? 1.0 : 0.6;
      speed.addTransientModifier(new AttributeModifier(MOD_SLOW, -facteur,
         AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
      if (MinepieceModMobEffects.STICKY != null) {
         cible.removeEffect(MinepieceModMobEffects.STICKY);
         cible.addEffect(new MobEffectInstance(MinepieceModMobEffects.STICKY,
            DevilFruitMochiConfig.stickyDurationTicks(), immobile ? 1 : 0, false, false, true));
      }
   }

   /** Retire nominativement notre modificateur et notre marqueur. */
   private static void remove(LivingEntity cible) {
      AttributeInstance speed = cible.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(MOD_SLOW);
      }
      if (MinepieceModMobEffects.STICKY != null) {
         cible.removeEffect(MinepieceModMobEffects.STICKY);
      }
   }

   /** Résolution robuste : référence revalidée d'abord, puis les recherches par identifiant. */
   private static LivingEntity resolve(UUID id) {return REFS.get(id);
   }
}
