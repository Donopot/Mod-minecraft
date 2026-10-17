package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.mori.DevilFruitMoriConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;

/**
 * <b>Racines, végétation et drainage</b> — Mori Mori no Mi (FRUIT-MORI_MORI-03/04/05).
 *
 * <p>Trois mécaniques, trois façons précises de nuire sans le vouloir, et donc trois garde-fous.</p>
 *
 * <h2>1. Les racines immobilisent — mais « l'immobilisation respecte les résistances » (-03)</h2>
 * <p>Le critère ne dit pas « immobilise les cibles » : il dit « <b>respecte les résistances</b> ». La
 * différence est le cœur du contrat. Une immobilisation qui s'applique à tout le monde transforme un
 * pouvoir en <b>réglage du jeu</b> : plus personne ne peut décider de résister. On interroge donc le
 * <b>jeu lui-même</b> ({@code canBeAffected}) au lieu de réécrire une liste d'exceptions — c'est la même
 * discipline que partout ailleurs : <b>lire</b> la règle du moteur, ne pas la réinventer. Et
 * l'immobilisation <b>se termine proprement</b> : durée bornée, retrait nominatif du modificateur et du
 * marqueur.</p>
 *
 * <h2>2. Végétation définitive</h2>
 * <p>Feuilles persistantes via PowerBlockService : protection des zones et inventaires, attribution bornée sans restitution du terrain.</p>
 *
 * <h2>3. Le drainage coûte et PLAFONNE — « aucun gain infini » (-05)</h2>
 * <p>C'est la garantie la plus facile à croire acquise et la plus facile à manquer. Un drainage sans
 * plafond est une <b>pompe</b> : sur une invocation bon marché, ou une cible qui se régénère, le porteur
 * gagnerait sans fin. Trois exclusions et deux plafonds : les <b>invocations du porteur</b>, ses
 * <b>alliés</b> et les <b>cibles invulnérables</b> ne rapportent <b>rien</b> ; et même contre une cible
 * valide, la vie drainée <b>par usage</b> et l'énergie <b>gagnée par usage</b> sont bornées.</p>
 *
 * <p><b>Dette connue et assumée.</b> Ce service partage une <i>forme</i> avec {@link StickyState} (Mochi)
 * et la capture de {@link SmokeCloud} (Moku) : un maintien temporaire avec libération garantie. Les
 * <i>politiques</i> diffèrent réellement (gradation ; nuage ; résistances), mais le <i>mécanisme</i> est
 * proche. Un service commun de « maintien » serait plus propre — il toucherait deux Fruits déjà livrés et
 * testés, donc il est proposé plutôt qu'imposé (cf. §4 du contrat).</p>
 */
public final class Roots {

   private static final Identifier MOD_ROOT =
      Identifier.fromNamespaceAndPath("minepiece", "root_hold");

   /** Toutes les cibles retenues, avec leur raison de fin attendue. */
   private static final Map<UUID, Integer> HELD = new HashMap<>();

   /** Cible → porteur, pour libérer si le porteur disparaît. */
   private static final Map<UUID, UUID> HELD_BY = new HashMap<>();

   /** Références revalidées (les entités virtuelles échappent aux recherches par identifiant). */
   private static final Map<UUID, LivingEntity> REFS = new HashMap<>();

   /** Raison de la dernière libération, par cible. */
   private static final Map<UUID, String> LAST_RELEASE = new HashMap<>();

   /** Porteurs de racines, pour résoudre les exclusions même avec des joueurs virtuels. */
   private static final Map<UUID, ServerPlayer> OWNER_REFS = new HashMap<>();


   private record Binding(LivingEntity owner,net.minecraft.server.level.ServerLevel level,net.minecraft.world.item.Item fruit){}
   private static final Map<UUID,Binding> BINDINGS=new HashMap<>();

   private Roots() {
   }

   /** Branche l'entretien, les bornes et les libérations. */
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->releaseEverything("arret"));
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         tickHeld();
         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            tickOwner(player);
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            releaseAllFrom(player, "deconnexion");
         }
      });
   }

   /** Nombre de cibles retenues (observable). */
   public static int heldCount() {
      return HELD.size();
   }

   /** La cible est-elle retenue par des racines ? */
   public static boolean isHeld(LivingEntity cible) {
      return cible != null && REFS.get(cible.getUUID())==cible && HELD.containsKey(cible.getUUID());
   }

   /** Raison de la dernière libération, ou « aucune ». */
   public static String lastReleaseReason(LivingEntity cible) {
      return cible == null ? "aucune" : LAST_RELEASE.getOrDefault(cible.getUUID(), "aucune");
   }

   /**
    * Enracine une cible.
    *
    * @return vrai si la cible a été retenue
    */
   public static boolean entangle(LivingEntity cible, ServerPlayer source) {
      if (!canHold(cible, source)) {
         return false;
      }
      BINDINGS.put(cible.getUUID(),new Binding(source,(ServerLevel)source.level(),DevilFruitPowerLogic.getDevilFruit(source).getItem()));
      // L'ORDRE DES OPÉRATIONS est ici la garantie : on demande d'abord au MOTEUR d'accepter l'effet,
      // et on n'applique le maintien QUE s'il l'accepte. Un pouvoir qui poserait le ralentissement avant
      // de vérifier immobiliserait une cible qui a résisté — exactement ce que le critère interdit.
      if (MinepieceModMobEffects.ROOTED != null) {
         cible.removeEffect(MinepieceModMobEffects.ROOTED);
         boolean accepte = cible.addEffect(new MobEffectInstance(MinepieceModMobEffects.ROOTED,
            DevilFruitMoriConfig.rootDurationTicks(), 0, false, false, true));
         if (!accepte) {
            BINDINGS.remove(cible.getUUID());return false;
         }
      }
      OWNER_REFS.put(source.getUUID(), source);
      HELD.put(cible.getUUID(), DevilFruitMoriConfig.rootDurationTicks());
      HELD_BY.put(cible.getUUID(), source.getUUID());
      REFS.put(cible.getUUID(), cible);
      applyModifier(cible);
      if (cible.level() instanceof ServerLevel level) {
         level.sendParticles(MinepieceModParticles.FLOWER, cible.getX(), cible.getY() + 0.3, cible.getZ(),
            24, 0.4, 0.4, 0.4, 0.02);
         level.playSound(null, cible.blockPosition(), SoundEvents.ROOTED_DIRT_PLACE, SoundSource.PLAYERS,
            0.9F, 0.7F);
      }
      return true;
   }

   /**
    * <b>L'immobilisation respecte les résistances.</b>
    *
    * <p>Deux règles, et la seconde compte autant que la première :</p>
    * <ul>
    *   <li>on interroge le <b>jeu</b> ({@code canBeAffected}) : une cible que le moteur déclare
    *       insensible n'est pas retenue, sans liste d'exceptions à maintenir ;</li>
    *   <li>les <b>invocations</b> et les <b>alliés</b> du porteur, ainsi que les joueurs en spectateur,
    *       sont exclus — un pouvoir qui enracine les siens est un piège.</li>
    * </ul>
    */
   public static boolean canHold(LivingEntity cible, ServerPlayer source) {
      if (cible==null || source==null || !BatchRules.target(source,cible))return false;
      if (cible == null || !cible.isAlive()) {
         return false;
      }
      if (source != null && cible.getUUID().equals(source.getUUID())) {
         return false;
      }
      if (cible instanceof ServerPlayer p && (p.isSpectator() || p.isCreative())) {
         return false;
      }
      if (HELD.containsKey(cible.getUUID()) || HELD.size() >= DevilFruitMoriConfig.rootMaxTargets()) {
         return false;
      }
      ServerPlayer porteur = resolveOwner(source == null ? null : source.getUUID());
      // Invocations du porteur : on n'enracine pas les siens.
      if (cible instanceof SummonEntity somme && source != null
            && DevilFruitSummons.isOwnedBy(somme, source)) {
         return false;
      }
      if (porteur != null && porteur.isAlliedTo(cible)) {
         return false;
      }
      // RÉSISTANCES, lecture honnête de ce que le moteur reconnaît vraiment :
      //   1. l'IMMUNITÉ AUX DÉGÂTS est une résistance réelle et vérifiable — une cible invulnérable
      //      n'est pas retenue (même règle que le drainage : `isInvulnerableTo`) ;
      //   2. l'IMMUNITÉ AUX EFFETS est déléguée à `addEffect`, la porte du jeu lui-même (cf. entangle).
      // Ce que j'ai écrit d'abord était un contrôle VACANT : un `canBeAffected` préalable, qui ne
      // refusait rien — vérifié empiriquement sur 8 types de cibles (zombie, squelette, wither,
      // ender dragon, golem de fer, villageois, blaze, slime) : toutes l'acceptent. Un contrôle qui ne
      // refuse jamais n'est pas un contrôle ; c'est la même erreur que l'« exception » du fruit
      // précédent, et elle ne se voit qu'en cherchant un cas où le contrôle dit NON.
      if (cible.level() instanceof ServerLevel level && source != null) {
         var source2 = level.damageSources().indirectMagic(source, source);
         if (cible.isInvulnerableTo(level, source2)) {
            return false;
         }
      }
      // Rien ne s'oppose : l'effectivité de l'effet est demandée dans `entangle`, qui n'applique le
      // maintien que si le moteur l'accepte.
      return true;
   }

   /**
    * Pose le maintien MÉCANIQUE (le ralentissement). Appelé <b>uniquement</b> après que le moteur a
    * accepté l'effet : c'est ce qui garantit qu'une cible résistante n'est pas ralentie.
    */
   private static void applyModifier(LivingEntity cible) {
      AttributeInstance a = cible.getAttribute(Attributes.MOVEMENT_SPEED);
      if (a != null) {
         a.removeModifier(MOD_ROOT);
         a.addTransientModifier(new AttributeModifier(MOD_ROOT, -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
      }
   }

   /** Entretien : la durée est bornée, donc la fin arrive toujours. */
   public static void tickHeld() {
      for (UUID id : new ArrayList<>(HELD.keySet())) {
         Integer reste = HELD.get(id);
         if (reste == null) {
            continue;
         }
         LivingEntity cible = resolve(id);
         Binding binding=BINDINGS.get(id);
         if(binding!=null && (binding.owner.isRemoved() || !binding.owner.isAlive() || binding.owner.level()!=binding.level || cible==null || cible.isRemoved() || cible.level()!=binding.level || binding.owner instanceof ServerPlayer player && (DevilFruitPowerLogic.isSuppressed(player) || !DevilFruitPowerLogic.getDevilFruit(player).is(binding.fruit)))){
            if(cible!=null)release(cible,"retrait");else {HELD.remove(id);HELD_BY.remove(id);BINDINGS.remove(id);REFS.remove(id);}continue;
         }

         if (cible == null || !cible.isAlive() || cible.isRemoved()) {
            release(cible, "cible-perdue");
            HELD.remove(id);
            HELD_BY.remove(id);
            REFS.remove(id);
            continue;
         }
         if (reste <= 1) {
            release(cible, "echeance");
            continue;
         }
         HELD.put(id, reste - 1);
      }
   }

   /** Le porteur a-t-il disparu, perdu son Fruit ou été scellé ? */
   public static void tickOwner(ServerPlayer porteur) {
      if (!porteur.isAlive()) {
         releaseAllFrom(porteur, "mort");
         return;
      }
      if (DevilFruitPowerLogic.isSuppressed(porteur)) {
         releaseAllFrom(porteur, "suppression");
         return;
      }
      if (DevilFruitPowerLogic.getDevilFruit(porteur).isEmpty()) {
         releaseAllFrom(porteur, "retrait");
      }
   }

   /** Libère une cible : retire NOMINATIVEMENT notre modificateur et notre marqueur. */
   public static void release(LivingEntity cible, String raison) {
      if (cible == null || HELD.remove(cible.getUUID()) == null) {
         return;
      }
      LAST_RELEASE.put(cible.getUUID(), raison);
      HELD_BY.remove(cible.getUUID());
      REFS.remove(cible.getUUID());
      BINDINGS.remove(cible.getUUID());
      AttributeInstance a = cible.getAttribute(Attributes.MOVEMENT_SPEED);
      if (a != null) {
         a.removeModifier(MOD_ROOT);
      }
      if (MinepieceModMobEffects.ROOTED != null) {
         cible.removeEffect(MinepieceModMobEffects.ROOTED);
      }
   }

   /** Libère tout ce qu'un porteur retenait. */
   public static int releaseAllFrom(LivingEntity porteur, String raison) {
      if (porteur == null) {
         return 0;
      }
      int n = 0;
      for (UUID id : new ArrayList<>(HELD_BY.keySet())) {
         if (porteur.getUUID().equals(HELD_BY.get(id))) {
            release(resolve(id), raison);
            n++;
         }
      }
      return n;
   }

   /** Libère tout le monde (reset, fin de test). */
   public static int releaseEverything(String raison) {
      int n = 0;
      for (UUID id : new ArrayList<>(HELD.keySet())) {
         release(resolve(id), raison);
         n++;
      }
      HELD.clear();
      BINDINGS.clear();REFS.clear();
      HELD_BY.clear();
      return n;
   }

   /** Permanent leaves; finite attribution only, no decay or terrain restoration. */
   public static int growVegetation(ServerPlayer porteur, BlockPos centre) {
      if (porteur == null || !porteur.isAlive() || !(porteur.level() instanceof ServerLevel level)) {
         return 0;
      }
      if (DevilFruitPowerLogic.isSuppressed(porteur)) {
         return 0;
      }
      int max = DevilFruitMoriConfig.vegetationMaxBlocks();
      int duree = DevilFruitMoriConfig.vegetationLifetimeTicks();
      int poses = 0;
      outer:
      for (int y = 0; y < 3; y++) {
         for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
               if (poses >= max) {
                  break outer;
               }
               BlockPos p = centre.offset(x, y, z);
               int avant = PowerBlockService.activeCountFor(porteur.getUUID());
               PowerBlockService.place(level, p, Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.PERSISTENT,true), duree,
                  porteur.getUUID());
               if (PowerBlockService.activeCountFor(porteur.getUUID()) > avant) {
                  poses++;
                  level.sendParticles(MinepieceModParticles.FLOWER, p.getX() + 0.5, p.getY() + 0.5,
                     p.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.01);
               }
            }
         }
      }
      if (poses > 0) {
         level.playSound(null, centre, SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
      }
      return poses;
   }

   /** Résout le porteur : référence d'abord (les joueurs virtuels ne sont pas dans la liste). */
   public static ServerPlayer resolveOwner(UUID owner) {
      if (owner == null) {
         return null;
      }
      ServerPlayer connu = OWNER_REFS.get(owner);
      if (connu != null && !connu.isRemoved() && connu.isAlive()) {
         return connu;
      }
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return null;
      }
      ServerPlayer connecte = server.getPlayerList().getPlayer(owner);
      if (connecte != null) {
         return connecte;
      }
      for (ServerLevel level : server.getAllLevels()) {
         for (ServerPlayer candidat : level.getEntitiesOfClass(ServerPlayer.class,
               new net.minecraft.world.phys.AABB(-30000000, level.getMinY(), -30000000,
                  30000000, level.getMaxY(), 30000000))) {
            if (candidat.getUUID().equals(owner)) {
               return candidat;
            }
         }
      }
      return null;
   }

   /** Résolution robuste d'une cible retenue. */
   private static LivingEntity resolve(UUID id) {return REFS.get(id);
   }
}
