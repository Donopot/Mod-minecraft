package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Ténèbres : attraction progressive et absorption bornée</b> — Yami Yami no Mi
 * (FRUIT-YAMI_YAMI-03/04).
 *
 * <h2>-03 · Attraction : progressive, bornée, et jamais à travers un mur</h2>
 * <p>Le plan demande une « <b>attraction progressive</b> avec <b>force maximale</b> et <b>rayon
 * borné</b> », critère « les cibles suivent une <b>trajectoire cohérente</b> sans traverser les murs
 * <b>de façon involontaire</b> ». Trois garde-fous, chacun répondant à une moitié du critère :</p>
 * <ol>
 *   <li><b>Progressive</b> — la force part d'un dixième de sa valeur et croît jusqu'au maximum sur la
 *       durée déclarée. Une attraction à pleine force dès le premier tick ne serait pas une attraction,
 *       ce serait une téléportation lente.</li>
 *   <li><b>Bornée</b> — rayon et force sont plafonnés <b>dans le code</b>. Et la traction s'arrête à
 *       1,5 bloc du porteur : sans ce seuil, la cible traverse le porteur et ressort derrière lui, ce
 *       qui est exactement la « trajectoire incohérente » que le critère interdit.</li>
 *   <li><b>Jamais à travers un mur</b> — la cible doit être <b>en vue</b> du porteur. La ligne de vue
 *       est celle du moteur, donc la même règle que la frappe et que l'arrêt des rayons de Pika Pika :
 *       « à couvert » n'a qu'une définition dans tout le mod.</li>
 * </ol>
 *
 * <h2>-04 · Absorption : une capacité dépassée ne supprime plus rien</h2>
 * <p>Le plan exige de définir les <b>attaques absorbables</b>, la <b>capacité</b> et le <b>coût</b>, avec
 * le critère « une capacité dépassée ne supprime pas tous les projectiles ou dégâts <b>sans
 * limite</b> ».</p>
 * <p><b>Absorbable</b> = les coups <b>physiques</b> et les <b>projectiles</b>, et rien d'autre : la
 * lumière, le feu, les explosions et la magie traversent les ténèbres — c'est la règle de la source, et
 * elle est <b>mesurée source par source</b> dans les tests, contre un témoin sans Fruit.</p>
 * <p><b>Capacité et coût</b> : un nombre de coups <b>fixé</b> par la configuration, chaque absorption
 * coûtant de l'énergie au porteur. Sans énergie, ou capacité épuisée, <b>les dégâts passent
 * normalement</b> : c'est la seule lecture possible de « pas sans limite », et un test la vérifie en
 * tirant <b>un coup de plus</b> que la capacité.</p>
 */
public final class YamiDarkness {

   private static final class Pull {
      final UUID porteur;
      final ServerPlayer owner;final ServerLevel world;
      final double rayon;
      final double forceMax;
      final int dureeTotale;
      int ttl;
      int ciblesTouchees;
      final java.util.Set<LivingEntity> controlled=new java.util.HashSet<>();

      Pull(ServerPlayer owner,UUID porteur, double rayon, double forceMax, int ttl) {
         this.owner=owner;this.world=(ServerLevel)owner.level();
         this.porteur = porteur;
         this.rayon = rayon;
         this.forceMax = forceMax;
         this.dureeTotale = ttl;
         this.ttl = ttl;
      }
   }

   private static final Map<UUID, Pull> PULLS = new HashMap<>();
   private static final Map<UUID, ServerPlayer> OWNER_REFS = new HashMap<>();
   /** Capacité d'absorption restante par porteur. */
   private static final Map<UUID, Integer> ABSORB_REMAINING = new HashMap<>();


   private static final Map<UUID,Integer> ABSORB_TTL=new HashMap<>();
   private static final Map<UUID,ServerPlayer> ABSORB_OWNERS=new HashMap<>();
   private static final Map<UUID,ServerLevel> ABSORB_WORLD=new HashMap<>();
   private static boolean valid(ServerPlayer p,ServerLevel l){return BatchRules.usable(p,l,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI);}
   private static int attractionsLancees;
   private static int attractionsTerminees;
   private static int absorptionsReussies;
   private static int absorptionsRefuseesCapacite;
   private static int absorptionsRefuseesEnergie;

   private YamiDarkness() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            clearFor(player, "deconnexion");
            OWNER_REFS.remove(player.getUUID());
         }
      });
   }

   // ============================== -03 · ATTRACTION ==============================

   /**
    * Lance une attraction progressive autour du porteur.
    *
    * @param rayon rayon demandé (0 = celui de la configuration) ; toujours plafonné dans le code
    */
   public static boolean startPull(ServerPlayer porteur, double rayon) {
      if (porteur == null || !(porteur.level() instanceof ServerLevel)) {
         return false;
      }
      if (!(porteur.level() instanceof ServerLevel current) || !valid(porteur,current)) {
         return false; // un pouvoir scellé n'attire rien
      }
      double r = rayon <= 0.0 ? DevilFruitYamiConfig.pullRadiusBlocks()
         : Math.min(DevilFruitYamiConfig.HARD_MAX_RADIUS, rayon);
      PULLS.put(porteur.getUUID(), new Pull(porteur,porteur.getUUID(), r,
         DevilFruitYamiConfig.pullForceMax(), DevilFruitYamiConfig.pullTicks()));
      OWNER_REFS.put(porteur.getUUID(), porteur);
      attractionsLancees++;
      return true;
   }

   /** Une attraction est-elle en cours ? */
   public static boolean isPulling(ServerPlayer porteur) {
      return porteur != null && PULLS.containsKey(porteur.getUUID());
   }

   /** Ticks d'attraction restants (0 si aucune). */
   public static int remainingPullTicks(ServerPlayer porteur) {
      Pull p = porteur == null ? null : PULLS.get(porteur.getUUID());
      return p == null ? 0 : p.ttl;
   }

   /** Nombre de cibles actuellement retenues par l'attraction du porteur. */
   public static int pulledTargets(ServerPlayer porteur) {
      Pull p = porteur == null ? null : PULLS.get(porteur.getUUID());
      return p == null ? 0 : p.ciblesTouchees;
   }

   // ============================== -04 · ABSORPTION ==============================

   /** Ouvre une fenêtre d'absorption : la capacité est réarmée à sa valeur de configuration. */
   public static boolean startAbsorbing(ServerPlayer porteur) {
      if (porteur == null) {
         return false;
      }
      if (!(porteur.level() instanceof ServerLevel current) || !valid(porteur,current)) {
         return false;
      }
      ABSORB_REMAINING.put(porteur.getUUID(), DevilFruitYamiConfig.absorbCapacityHits());
      ABSORB_OWNERS.put(porteur.getUUID(),porteur);ABSORB_TTL.put(porteur.getUUID(),600);ABSORB_WORLD.put(porteur.getUUID(),(ServerLevel)porteur.level());
      // Marqueur VISIBLE de la fenêtre : le rendu élémentaire demandé par -06. La fenêtre n'est pas
      // borne par capacite ET par les 600 ticks du marqueur, sans prolongation aux impacts.
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.YAMI_ABSORBING, 600, 0, false, false));
      OWNER_REFS.put(porteur.getUUID(), porteur);
      return true;
   }

   /** Le porteur absorbe-t-il en ce moment ? */
   public static boolean isAbsorbing(ServerPlayer porteur) {
      return porteur != null && ABSORB_REMAINING.containsKey(porteur.getUUID());
   }

   /** Coups encore absorbables, ou -1 si le porteur n'absorbe pas. */
   public static int absorbRemaining(ServerPlayer porteur) {
      return porteur == null ? -1 : ABSORB_REMAINING.getOrDefault(porteur.getUUID(), -1);
   }

   /** Coups absorbés depuis l'ouverture de la fenêtre. */
   public static int absorbedHits(ServerPlayer porteur) {
      Integer reste = porteur == null ? null : ABSORB_REMAINING.get(porteur.getUUID());
      return reste == null ? 0 : DevilFruitYamiConfig.absorbCapacityHits() - reste;
   }

   /**
    * Décide si un coup est absorbé — appelée <b>une seule fois par événement de dégâts</b>, depuis le
    * calcul de magnification, qui est le point de passage unique de la couche pouvoir.
    *
    * @return vrai si le coup est absorbé (les dégâts doivent alors être annulés)
    */
   public static boolean absorbIfAble(LivingEntity victime) {
      if (!(victime instanceof ServerPlayer porteur)) {
         return false;
      }
      Integer reste = ABSORB_REMAINING.get(porteur.getUUID());
      if (reste == null) {
         return false; // pas de fenêtre ouverte
      }
      if(ABSORB_OWNERS.get(porteur.getUUID())!=porteur){closeAbsorption(porteur.getUUID());return false;}
      if(!valid(porteur,ABSORB_WORLD.get(porteur.getUUID()))){clearFor(porteur,"invalide");return false;}
      if(ABSORB_TTL.getOrDefault(porteur.getUUID(),0)<=0){closeAbsorption(porteur.getUUID());return false;}
      if (reste <= 0) {
         // CAPACITÉ DÉPASSÉE : les ténèbres sont pleines, les dégâts passent normalement.
         absorptionsRefuseesCapacite++;
         return false;
      }
      double cout = DevilFruitYamiConfig.absorbCostEnergy();
      var vars = MinepieceModVariables.get(porteur);
      if (cout > 0.0 && vars.PlayerEnergy < cout) {
         absorptionsRefuseesEnergie++;
         return false;
      }
      vars.PlayerEnergy = Math.max(0.0, vars.PlayerEnergy - cout);
      ABSORB_REMAINING.put(porteur.getUUID(), reste - 1);
      absorptionsReussies++;
      porteur.addEffect(new net.minecraft.world.effect.MobEffectInstance(
         MinepieceModMobEffects.YAMI_ABSORBING, ABSORB_TTL.getOrDefault(porteur.getUUID(),1), 0, false, false));
      return true;
   }

   // ============================== COMMUN ==============================

   /** Ferme attraction et absorption d'un porteur (retrait du Fruit, mort, reset). */
   public static void clearFor(ServerPlayer porteur, String raison) {
      if (porteur == null) {
         return;
      }
      var pull=PULLS.remove(porteur.getUUID());if(pull!=null)releaseMotion(pull);
      closeAbsorption(porteur.getUUID());OWNER_REFS.remove(porteur.getUUID());
      ABSORB_REMAINING.remove(porteur.getUUID());
      porteur.removeEffect(MinepieceModMobEffects.YAMI_ABSORBING);
   }

   /** Ferme tout — reset de Fruit et fin de test. */
   public static void clearAll() {
      for(ServerPlayer p:new ArrayList<>(OWNER_REFS.values()))clearFor(p,"reset");
      PULLS.clear();ABSORB_REMAINING.clear();ABSORB_TTL.clear();ABSORB_WORLD.clear();ABSORB_OWNERS.clear();OWNER_REFS.clear();
   }

   private static void closeAbsorption(UUID id){var owner=ABSORB_OWNERS.remove(id);if(owner!=null)owner.removeEffect(MinepieceModMobEffects.YAMI_ABSORBING);ABSORB_TTL.remove(id);ABSORB_WORLD.remove(id);ABSORB_REMAINING.remove(id);if(!PULLS.containsKey(id))OWNER_REFS.remove(id);}
   private static void tick() {
      for(var id:new ArrayList<>(ABSORB_TTL.keySet())){var p=ABSORB_OWNERS.get(id);int left=ABSORB_TTL.get(id)-1;if(p==null || !valid(p,ABSORB_WORLD.get(id))){if(p!=null)clearFor(p,"invalide");else closeAbsorption(id);continue;}if(left<=0){closeAbsorption(id);continue;}ABSORB_TTL.put(id,left);}
      for (Map.Entry<UUID, Pull> entree : new ArrayList<>(PULLS.entrySet())) {
         Pull p = entree.getValue();
         ServerPlayer porteur = p.owner;
         if (!valid(porteur,p.world) || porteur.isRemoved() || !porteur.isAlive()
               || DevilFruitPowerLogic.isSuppressed(porteur)
               || !(porteur.level() instanceof ServerLevel level)) {
            releaseMotion(p);PULLS.remove(entree.getKey());
            attractionsTerminees++;
            continue;
         }
         p.ttl--;
         if (p.ttl <= 0) {
            releaseMotion(p);PULLS.remove(entree.getKey());
            attractionsTerminees++;
            continue;
         }
         appliquerAttraction(level, porteur, p);
      }
   }

   private static void releaseMotion(Pull p){for(var target:p.controlled)if(!target.isRemoved() && target.level()==p.world){target.setDeltaMovement(0,target.getDeltaMovement().y,0);target.syncVelocity=true;}p.controlled.clear();}
   private static void appliquerAttraction(ServerLevel level, ServerPlayer porteur, Pull p) {
      // Progression : la force croît avec le temps. Un dixième au premier tick, le maximum au dernier.
      double progression = 1.0 - (p.ttl / (double) p.dureeTotale);
      double force = (0.1 + 0.9 * progression) * p.forceMax;
      int retenues = 0;
      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
            porteur.getBoundingBox().inflate(p.rayon),
            e -> e != porteur && CombatSkillLogic.isValidTarget(porteur, e))) {
         double distance = cible.position().distanceTo(porteur.position());
         if(distance<1.5){if(p.controlled.contains(cible)){cible.setDeltaMovement(0,cible.getDeltaMovement().y,0);cible.syncVelocity=true;}continue;}
         if (distance > p.rayon) {
            // Hors du rayon borné, ou déjà au contact : on ne traîne pas la cible À TRAVERS le porteur.
            continue;
         }
         if (!BatchRules.target(porteur,cible)) {
            // À COUVERT : les ténèbres n'attirent pas à travers la pierre.
            continue;
         }
         Vec3 direction = porteur.position().subtract(cible.position()).normalize();
         Vec3 movement=new Vec3(direction.x*force,cible.getDeltaMovement().y,direction.z*force);
         if(!SafeDestination.isSafe(level,cible,cible.position().add(movement)))continue;
         cible.setDeltaMovement(movement);cible.syncVelocity=true;p.controlled.add(cible);
         cible.fallDistance = 0.0F;
         retenues++;
      }
      p.ciblesTouchees = retenues;
      if (retenues > 0) {
         // Small smoke accents; the procedural purple field carries the elemental identity.
         level.sendParticles(ParticleTypes.SMOKE,
            porteur.getX(), porteur.getY() + 1.0, porteur.getZ(), 2, 1.5, 0.5, 1.5, 0.01);
      }
   }

   /**
    * Fait avancer l'attraction d'un tick, <b>à la demande</b>.
    *
    * <p>Sonde de harnais : elle permet de savoir si le MÉCANISME de traction fonctionne, au lieu de
    * l'espérer. Sans elle, un témoin qui n'a pas bougé à la relecture ne dit pas si la traction est
    * fautive ou si l'observation a manqué — même leçon que la sonde d'impact des rayons de Pika
    * Pika.</p>
    */
   public static void tickOnceForTest() {
      tick();
   }

   /** Rapport de diagnostic. */
   public static String darknessReport() {
      return "attractions=" + attractionsLancees + " terminees=" + attractionsTerminees
         + " absorptions=" + absorptionsReussies + " refusees_capacite=" + absorptionsRefuseesCapacite
         + " refusees_energie=" + absorptionsRefuseesEnergie;
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      attractionsLancees = 0;
      attractionsTerminees = 0;
      absorptionsReussies = 0;
      absorptionsRefuseesCapacite = 0;
      absorptionsRefuseesEnergie = 0;
   }

   /** Nombre d'absorptions refusées faute de capacité — le compteur « borné ». */
   public static int refusedByCapacityCount() {
      return absorptionsRefuseesCapacite;
   }

   /** Nombre d'absorptions réussies. */
   public static int absorbedCount() {
      return absorptionsReussies;
   }

   /** Petit bruit de ténèbres, réutilisé par les techniques. */
   public static void playDarknessSound(ServerPlayer porteur) {
      if (porteur != null && porteur.level() instanceof ServerLevel level) {
         level.playSound(null, porteur.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS,
            0.5F, 0.7F);
      }
   }
}
