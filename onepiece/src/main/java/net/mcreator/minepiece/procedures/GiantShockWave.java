package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Onde de choc du Daibutsu</b> — Hito Hito Daibutsu (FRUIT-HITO_HITO_DAIBUTSU-04).
 *
 * <p>Le plan : « créer des <b>ondes de choc visibles</b> avec <b>progression</b> et <b>dégâts
 * bornés</b> », critère « le <b>front d'onde détermine l'impact</b> et <b>respecte les protections</b> ».</p>
 *
 * <h2>Pourquoi un front qui voyage, et pas un rayon</h2>
 * <p>Une frappe de zone classique touche tout le monde au même instant. Ici le critère demande
 * explicitement que ce soit <b>le front</b> qui décide : l'onde avance, et ce qu'elle atteint dépend de
 * <b>où elle est</b> à cet instant. Quelqu'un qui a le temps de sortir de la trajectoire n'est pas
 * touché — l'attaque est esquivable, donc lisible.</p>
 *
 * <h2>Les trois garanties</h2>
 * <ol>
 *   <li><b>Dégâts bornés</b> — chaque cible est frappée <b>une seule fois</b>, à son passage
 *       ({@code alreadyHit}) ; le front ne martyrise pas ce qu'il a déjà traversé.</li>
 *   <li><b>Pas de saut de cible</b> — le segment parcouru est <b>échantillonné</b> tous les 0,5 bloc.
 *       Sans cet échantillonnage, une vitesse de 1,5 bloc/tick ferait passer le front « à travers » une
 *       cible étroite entre deux ticks, et l'onde raterait un adversaire qu'elle a visiblement touché.</li>
 *   <li><b>Protections respectées</b> — l'onde ne modifie <b>aucun bloc</b> (une onde de pression ne
 *       creuse pas le sol), et les occupants d'une <b>zone protégée</b> ne sont pas atteints.</li>
 * </ol>
 *
 * <p><b>Pourquoi ne pas réutiliser {@link SeismicWave} (Gura Gura)</b> : celle-ci fend le terrain, donc
 * elle dépend du service de blocs temporaires et du réglage de destruction de Gura. Une onde de
 * Daibutsu est un front de <b>pression pure</b>, sans effet sur le sol. Les coupler forcerait à
 * partager un réglage qui, pour l'une des deux, est faux. Elles partagent le <i>principe</i>
 * (front voyageur, frappe unique, échantillonnage), pas le <i>réglage</i>.</p>
 */
public final class GiantShockWave {

   /** Une onde en cours de propagation. */
   private static final class Wave {
      final ServerPlayer owner;
      final ServerLevel level;
      final Vec3 origin;
      final Vec3 direction;
      final double maxDistance;
      final Set<UUID> alreadyHit = new HashSet<>();
      double travelled;
      int hits;

      Wave(ServerPlayer owner, ServerLevel level, Vec3 origin, Vec3 direction, double maxDistance) {
         this.owner = owner;
         this.level = level;
         this.origin = origin;
         this.direction = direction;
         this.maxDistance = maxDistance;
      }
   }

   private static final List<Wave> ACTIVE = new ArrayList<>();

   // --- Observabilité du balayage (sert aux preuves : « l'onde a-t-elle VU la cible ? ») ---
   private static int sweepFound;
   private static int sweepOutOfRange;
   private static int sweepInvalidTarget;
   private static int sweepProtected;
   private static int sweepDamageRefused;

   /** Détail du dernier balayage : créatures vues, écartées, frappées. */
   public static String sweepReport() {
      return "vues=" + sweepFound + ", hors-portée=" + sweepOutOfRange
         + ", cible-invalide=" + sweepInvalidTarget + ", protégée=" + sweepProtected
         + ", dégâts-refusés=" + sweepDamageRefused;
   }

   private GiantShockWave() {
   }

   /** Branche l'avancement des ondes sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
   }

   /**
    * Lance une onde depuis le porteur, dans sa direction horizontale.
    *
    * @return vrai si l'onde a été créée (nombre d'ondes simultanées borné)
    */
   public static boolean cast(ServerPlayer player) {
      long enCours = ACTIVE.stream().filter(w -> w.owner == player).count();
      if (enCours >= DevilFruitDaibutsuConfig.waveMaxConcurrent()) {
         return false;
      }
      Vec3 look = player.getLookAngle();
      Vec3 direction = new Vec3(look.x, 0.0, look.z);
      if (direction.lengthSqr() < 1.0E-6) {
         direction = new Vec3(0.0, 0.0, 1.0);
      }
      ACTIVE.add(new Wave(player, (ServerLevel) player.level(), player.position(),
         direction.normalize(), DevilFruitDaibutsuConfig.waveMaxDistance()));
      return true;
   }

   /** Nombre d'ondes actives. */
   public static int activeWaves() {
      return ACTIVE.size();
   }

   /** Nombre cumulé de cibles frappées par les ondes en cours. */
   public static int hits() {
      return ACTIVE.stream().mapToInt(w -> w.hits).sum();
   }

   /** Distance parcourue par la première onde (observable — prouve la progression). */
   public static double travelledByFirst() {
      return ACTIVE.isEmpty() ? 0.0 : ACTIVE.get(0).travelled;
   }

   /** Oublie tout (fin de test, reset). */
   public static void clearAll() {
      ACTIVE.clear();
      sweepFound = 0;
      sweepOutOfRange = 0;
      sweepInvalidTarget = 0;
      sweepProtected = 0;
      sweepDamageRefused = 0;
   }

   /**
    * Fait avancer toutes les ondes d'un pas. Exposé pour que les tests déroulent le temps sans serveur.
    */
   public static void tick() {
      if (ACTIVE.isEmpty()) {
         return;
      }
      List<Wave> finished = new ArrayList<>();
      for (Wave wave : ACTIVE) {
         if (advance(wave) || !wave.owner.isAlive() || wave.owner.isRemoved()) {
            finished.add(wave);
         }
      }
      ACTIVE.removeAll(finished);
   }

   /** Avance une onde ; renvoie {@code true} si elle est terminée. */
   private static boolean advance(Wave wave) {
      double speed = DevilFruitDaibutsuConfig.waveSpeed();
      double from = wave.travelled;
      double to = Math.min(wave.travelled + speed, wave.maxDistance);
      wave.travelled = to;

      // Échantillonnage du segment : le front ne saute pas par-dessus une cible étroite.
      int samples = Math.max(1, (int) Math.ceil((to - from) / 0.5));
      for (int i = 0; i <= samples; i++) {
         double along = from + (to - from) * (i / (double) samples);
         sweep(wave, wave.origin.add(wave.direction.scale(along)));
      }

      Vec3 front = wave.origin.add(wave.direction.scale(to));
      // Le front est VISIBLE : deux nappes de particules qui montent.
      wave.level.sendParticles(MinepieceModParticles.SHOCK_HIT, front.x, front.y + 0.3, front.z,
         14, 0.5, 0.4, 0.5, 0.02);
      wave.level.sendParticles(MinepieceModParticles.SHOCK_HIT, front.x, front.y + 0.2, front.z,
         6, 0.4, 0.2, 0.4, 0.01);
      return to >= wave.maxDistance;
   }

   /** Fauche ce qui se trouve dans l'épaisseur du front, une seule fois par cible. */
   private static void sweep(Wave wave, Vec3 front) {
      double thickness = DevilFruitDaibutsuConfig.waveThickness();
      double halfHeight = DevilFruitDaibutsuConfig.waveHalfHeight();
      AABB zone = new AABB(front.x - thickness, front.y - halfHeight, front.z - thickness,
         front.x + thickness, front.y + halfHeight, front.z + thickness);
      for (LivingEntity target : wave.level.getEntitiesOfClass(LivingEntity.class, zone)) {
         if (target == wave.owner || !target.isAlive()) {
            continue;
         }
         sweepFound++;
         if (wave.alreadyHit.contains(target.getUUID())) {
            continue; // dégâts bornés : une seule frappe par cible
         }
         if (!CombatSkillLogic.isValidTarget(wave.owner, target)) {
            sweepInvalidTarget++;
            continue;
         }
         if (target.getBoundingBox().getCenter().distanceTo(front) > thickness + halfHeight) {
            sweepOutOfRange++;
            continue;
         }
         // Protections : un occupant d'une zone protégée n'est pas atteint par l'onde.
         if (DevilFruitExplosions.isProtectedPosition(wave.level, target.blockPosition())) {
            sweepProtected++;
            continue;
         }
         var source = CombatSkillLogic.elementalDamageSource(wave.level, ElementType.EXPLOSIVE, wave.owner, null);
         if (target.hurtServer(wave.level, source, (float) DevilFruitDaibutsuConfig.waveDamage())) {
            wave.alreadyHit.add(target.getUUID());
            wave.hits++;
         } else {
            sweepDamageRefused++;
         }
      }
   }
}
