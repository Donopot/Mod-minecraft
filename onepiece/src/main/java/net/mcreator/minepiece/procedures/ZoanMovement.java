package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig;
import java.util.List;
import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Bonds et déplacements rapides des Fruits Zoan</b> — FRUIT-{NEKO_NEKO_*,RYU_RYU_*,TORI_TORI,UO_UO,
 * USHI_USHI}-04.
 *
 * <p>Le plan, pour chacun de ces Fruits : « Créer <b>bonds et déplacements rapides avec arrêt à
 * obstacle</b> », critère « la <b>vitesse</b> et les <b>destinations</b> restent bornées ».</p>
 *
 * <h2>Ce qui est commun, et ce qui ne l'est pas</h2>
 * <p>Le corps de chaque Zoan diffère ; la <b>règle</b> du bond, non. Elle est donc écrite <b>une
 * fois</b> — « on marche le long du regard, on retient le dernier point sûr, on s'arrête au mur » — et
 * <b>partagée</b> avec le déplacement lumineux de Pika Pika via {@link SafeDestination#walkAlong}. Un bond
 * de Zoan et un déplacement de lumière répondent exactement aux mêmes trois questions : hors zone
 * chargée ? zone protégée ? de la place pour la boîte de collision ?</p>
 *
 * <h2>Table des techniques concernées, et pourquoi elle est explicite</h2>
 * <p>Ce sont les techniques dont le <b>nom de la source désigne un déplacement</b> — on ne devine pas
 * d'après le type, puisqu'un « Attack Speed » de Zoan est un <b>élan</b>, pas un saut :</p>
 * <ul>
 *   <li>{@code soru} — « Soru », le pas de côté de l'école Rokushiki (Neko Leopard, Ushi Giraffe) ;</li>
 *   <li>{@code attackjump} — « Attack Jump », le bond d'attaque (Neko Leopard, Ryu Pachycéphalosaure) ;</li>
 *   <li>{@code hooin} — « Hoo In », la ruée du Phénix ;</li>
 *   <li>{@code kaifu} — « Kaifu », la ruée du dragon de mer.</li>
 * </ul>
 * <p>Toute autre technique garde le comportement générique : ce service ne s'applique qu'à sa table.</p>
 */
public final class ZoanMovement {

   private static final Set<String> TECHNIQUES_DE_DEPLACEMENT =
      Set.of("soru", "attackjump", "hooin", "kaifu");

   private static int bonds;
   private static int refus;
   private static double distanceTotale;

   private ZoanMovement() {
   }

   /** Cette technique est-elle un déplacement ? */
   public static boolean isMovementMove(String moveId) {
      return moveId != null && TECHNIQUES_DE_DEPLACEMENT.contains(moveId);
   }

   /** Table lisible, pour les contrats et les preuves. */
   public static List<String> movementMoves() {
      return List.copyOf(TECHNIQUES_DE_DEPLACEMENT);
   }

   /**
    * Bond borné : la destination est <b>calculée côté serveur</b> (dernier point sûr le long du regard)
    * et validée avant d'être appliquée.
    *
    * @return vrai si un déplacement a eu lieu
    */
   public static boolean leap(ServerPlayer porteur, double distanceDemandee) {
      if (porteur == null || !(porteur.level() instanceof ServerLevel level)) {
         refus++;
         return false;
      }
      if (DevilFruitPowerLogic.isSuppressed(porteur)) {
         refus++;
         return false; // un pouvoir scellé ne bondit pas
      }
      double distance = distanceDemandee <= 0.0 ? DevilFruitZoanConfig.dashDistanceBlocks()
         : Math.min(DevilFruitZoanConfig.HARD_MAX_DASH, distanceDemandee);
      Vec3 depart = porteur.position();
      Vec3 destination = SafeDestination.walkAlong(level, porteur, distance, 0.5);
      if (destination == null || depart.distanceTo(destination) < 0.5) {
         // Aucun point sûr, ou déjà au bout : « tout ou rien », le refus ne déplace personne.
         refus++;
         return false;
      }
      tracer(level, depart, destination);
      porteur.teleportTo(destination.x, destination.y, destination.z);
      porteur.setOldPosAndRot();
      porteur.fallDistance = 0.0F;
      level.playSound(null, porteur.blockPosition(), SoundEvents.RABBIT_JUMP, SoundSource.PLAYERS, 0.7F, 1.3F);
      bonds++;
      distanceTotale += depart.distanceTo(destination);
      return true;
   }

   private static void tracer(ServerLevel level, Vec3 depart, Vec3 arrivee) {
      int points = 8;
      for (int i = 0; i <= points; i++) {
         Vec3 p = depart.lerp(arrivee, i / (double) points).add(0.0, 0.9, 0.0);
         level.sendParticles(MinepieceModParticles.BIG_SMOKE, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.0);
      }
   }

   /** Rapport de diagnostic. */
   public static String movementReport() {
      return "bonds=" + bonds + " refusees=" + refus + " distance_totale=" + Math.round(distanceTotale);
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      bonds = 0;
      refus = 0;
      distanceTotale = 0.0;
   }

   /** Nombre de bonds réussis. */
   public static int leapCount() {
      return bonds;
   }

   /** Nombre de bonds refusés. */
   public static int refusalCount() {
      return refus;
   }
}
