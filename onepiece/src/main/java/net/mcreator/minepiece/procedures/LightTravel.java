package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Déplacement lumineux</b> — Pika Pika no Mi (FRUIT-PIKA_PIKA-04).
 *
 * <p>Le plan : « Implémenter le déplacement lumineux avec <b>destination calculée côté serveur</b> »,
 * critère « <b>aucune téléportation dans un bloc</b>, <b>hors des limites</b> ou <b>vers un chunk non
 * autorisé</b> ».</p>
 *
 * <h2>La destination est calculée, pas demandée</h2>
 * <p>Le serveur ne fait pas confiance à une position envoyée par le client (un client modifié
 * demanderait n'importe où). Il <b>marche le long du regard</b>, par pas de 0,5 bloc, et retient le
 * <b>dernier point sûr</b> rencontré. Conséquences directes, et c'est tout l'intérêt : la lumière
 * <b>s'arrête au mur</b> au lieu de le traverser, elle ne sort jamais des zones chargées, et elle ne
 * franchit pas une <b>zone protégée</b> — vérifié par le prédicat partagé {@link SafeDestination},
 * celui-là même qui garde l'échange de positions d'Ope Ope. Une seconde copie de cette règle aurait
 * fini par diverger, et l'une des deux aurait téléporté dans la pierre.</p>
 *
 * <h2>Un refus ne déplace personne</h2>
 * <p>Si même le premier pas est refusé (le porteur est déjà collé à un mur, ou se trouve dans une zone
 * protégée), la méthode retourne {@code false} <b>sans avoir bougé</b> : le déplacement est
 * « tout ou rien », comme l'échange d'Ope Ope. La raison du refus est conservée et lisible
 * ({@link #lastRefusal}), pour qu'un échec de test soit diagnosticable sans reconstruire le mod.</p>
 */
public final class LightTravel {

   private static String dernierRefus = "aucun";
   private static int deplacements;
   private static int refus;

   private LightTravel() {
   }

   /**
    * Destination du déplacement lumineux : le <b>dernier point sûr</b> le long du regard.
    *
    * @return la position (aux pieds) où le porteur peut se tenir, ou {@code null} si rien n'est sûr
    */
   public static Vec3 destinationFor(ServerPlayer player) {
      return destinationFor(player,0);
   }
   public static Vec3 destinationFor(ServerPlayer player,double requested) {
      if (!(player.level() instanceof ServerLevel level)) {
         dernierRefus = "niveau-invalide";
         return null;
      }
      double distanceMax = DevilFruitPikaConfig.travelMaxDistanceBlocks();
      if(requested>0)distanceMax=Math.min(distanceMax,requested);
      // La marche est désormais partagée avec les bonds des Fruits Zoan (SafeDestination.walkAlong) :
      // une seule définition de « jusqu'où je peux aller sans traverser un mur ni une zone protégée ».
      Vec3 destination = SafeDestination.walkAlong(level, player, distanceMax, 0.5);
      if (destination == null) {
         String refus = SafeDestination.refusalReason(level, player, player.position());
         dernierRefus = refus.isEmpty() ? "collision" : refus;
      } else {
         dernierRefus = "aucun";
      }
      return destination;
   }

   /**
    * Déplace le porteur à la vitesse de la lumière.
    *
    * @param distanceMax distance maximale autorisée (0 = celle de la configuration)
    * @return vrai si le déplacement a eu lieu
    */
   public static boolean travel(ServerPlayer player, double distanceMax) {
      if (!(player.level() instanceof ServerLevel level)) {
         dernierRefus = "niveau-invalide";
         refus++;
         return false;
      }
      Vec3 destination = destinationFor(player,distanceMax);
      if (destination == null) {
         refus++;
         return false; // AUCUN déplacement : le refus n'a rien coûté
      }
      Vec3 depart = player.position();
      if (depart.distanceTo(destination) < 0.5) {
         // Déjà au bout : rien à faire, et le déplacement ne doit pas être compté comme un succès.
         dernierRefus = "deja-sur-place";
         refus++;
         return false;
      }
      // Traînée visible : la lumière laisse une trace entre le départ et l'arrivée.
      tracer(level, depart.add(0.0, player.getEyeY() - player.getY(), 0.0), destination, 12);
      player.teleportTo(destination.x, destination.y, destination.z);
      // La rotation « précédente » suit : une mesure de visée juste après un déplacement reste
      // déterministe (leçon apprise sur les mesures de cône).
      player.setOldPosAndRot();
      level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
         0.7F, 1.6F);
      deplacements++;
      dernierRefus = "aucun";
      return true;
   }

   /** Déplacement borné, utilisé par « Light Speed Kick ». */
   public static boolean dash(ServerPlayer player, double distanceMax) {
      return travel(player, distanceMax);
   }

   private static void tracer(ServerLevel level, Vec3 depart, Vec3 arrivee, int points) {
      for (int i = 0; i <= points; i++) {
         Vec3 p = depart.lerp(arrivee.add(0.0, 1.0, 0.0), i / (double) points);
         // Particule de lumière du JAR source (particle_light_large.json).
         level.sendParticles(net.mcreator.minepiece.init.MinepieceModParticles.LIGHT_LARGE,
            p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
      }
   }

   /** Raison du dernier refus (ou « aucun »). */
   public static String lastRefusal() {
      return dernierRefus;
   }

   /** Nombre de déplacements réussis. */
   public static int travelCount() {
      return deplacements;
   }

   /** Nombre de refus. */
   public static int refusalCount() {
      return refus;
   }

   /** Recommence les compteurs (tests répétables). */
   public static void resetReport() {
      deplacements = 0;
      refus = 0;
      dernierRefus = "aucun";
   }

   /** Points d'un trajet, exposé pour qu'un test puisse vérifier ce qui a été traversé. */
   public static List<Vec3> pathPoints(Vec3 depart, Vec3 arrivee, int points) {
      List<Vec3> liste = new ArrayList<>();
      for (int i = 0; i <= points; i++) {
         liste.add(depart.lerp(arrivee, i / (double) points));
      }
      return liste;
   }
}
