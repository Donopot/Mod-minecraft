package net.mcreator.minepiece.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Une destination est-elle sûre et autorisée ?</b> — prédicat partagé.
 *
 * <p>Écrit d'abord dans {@code OpeRoom} pour l'échange de positions (FRUIT-OPE_OPE-04), puis
 * <b>réclamé une seconde fois</b> par le déplacement lumineux de Pika Pika (FRUIT-PIKA_PIKA-04). C'est
 * exactement le moment où il faut factoriser : deux copies d'une règle de sûreté divergeraient au
 * premier ajustement, et l'une des deux finirait par téléporter dans un mur.</p>
 *
 * <p>Trois refus, chacun pour une raison distincte :</p>
 * <ol>
 *   <li><b>hors des zones chargées</b> — on ne déplace personne dans le vide ;</li>
 *   <li><b>zone protégée</b> — le prédicat <b>déjà en service</b> pour les explosions
 *       ({@link DevilFruitExplosions#isProtectedPosition}) : « protégé » n'a qu'<b>une</b> définition
 *       dans tout le mod ; une téléportation qui l'ignorerait serait un contournement, pas un pouvoir ;</li>
 *   <li><b>pas la place</b> — la boîte de collision de l'entité doit tenir à l'arrivée
 *       ({@code level.noCollision}). Ce test compte : une entité de 1,95 bloc posée dans un plafond d'un
 *       bloc est « déplacée » puis <b>étouffe</b> — le déplacement a techniquement eu lieu et la victime
 *       est morte dedans.</li>
 * </ol>
 *
 * <p>{@link #refusalReason} existe pour que l'échec d'un test soit <b>lisible</b> : « refusé » sans
 * raison oblige à reconstruire le mod pour comprendre.</p>
 */
public final class SafeDestination {

   private SafeDestination() {
   }

   /** La destination est-elle sûre <b>et</b> autorisée pour cette entité ? */
   public static boolean isSafe(ServerLevel level, Entity entite, Vec3 destination) {
      return refusalReason(level, entite, destination).isEmpty();
   }

   /**
    * Raison du refus, ou chaîne vide si la destination est acceptée.
    *
    * <p>Les valeurs possibles sont stables et lisibles par un test : {@code "hors-charge"},
    * {@code "zone-protegee"}, {@code "collision"}, {@code ""}.</p>
    */
   public static String refusalReason(ServerLevel level, Entity entite, Vec3 destination) {
      if (level == null || entite == null || destination == null) {
         return "argument-nul";
      }
      BlockPos pos = BlockPos.containing(destination.x, destination.y, destination.z);
      AABB boite = entite.getBoundingBox().move(destination.subtract(entite.position()));
      if(boite.minY<level.getMinY() || boite.maxY>level.getMaxY() || !level.getWorldBorder().isWithinBounds(boite))return "hors-limites";
      for(BlockPos block:BlockPos.betweenClosed(BlockPos.containing(boite.minX,boite.minY,boite.minZ),BlockPos.containing(boite.maxX-.00001,boite.maxY-.00001,boite.maxZ-.00001))){
         if(!level.isLoaded(block))return "hors-charge";
         if(DevilFruitExplosions.isProtectedPosition(level,block))return "zone-protegee";
         if(!level.getFluidState(block).isEmpty())return "fluide";
      }
      if (!level.noCollision(entite, boite)) {
         return "collision";
      }
      return "";
   }

   /**
    * <b>Dernier point sûr le long du regard</b>, par pas de 0,5 bloc.
    *
    * <p>La lumière de Pika Pika marchait déjà ainsi pour son déplacement ; les bonds des Fruits Zoan
    * ont besoin <b>exactement</b> de la même règle — « s'arrêter au mur, ne jamais se poser dans la
    * pierre, ne pas franchir une zone protégée ». À la deuxième copie, on factorise : deux versions de
    * cette marche auraient fini par diverger, et l'une des deux aurait traversé un mur.</p>
    *
    * @return la position (aux pieds) du dernier point sûr, ou {@code null} si le premier pas est déjà
    *         refusé
    */
   public static Vec3 walkAlong(ServerLevel level, Entity entite, double distanceMax, double pas) {
      if(!Double.isFinite(distanceMax) || !Double.isFinite(pas) || pas<=0 || distanceMax<=0)return null;
      distanceMax=Math.min(64,distanceMax);pas=Math.min(.5,Math.max(.05,pas));
      Vec3 origine = entite.getEyePosition();
      Vec3 direction = entite.getLookAngle().normalize();
      double decalageYeux = entite.getEyeY() - entite.getY();
      Vec3 derniereSure = null;
      for (double d = pas; d <= distanceMax; d += pas) {
         Vec3 pieds = origine.add(direction.scale(d)).subtract(0.0, decalageYeux, 0.0);
         if (!isSafe(level, entite, pieds)) {
            break;
         }
         derniereSure = pieds;
      }
      return derniereSure;
   }

   /**
    * Distance franchissable le long du regard, bornée par {@code distanceMax}.
    *
    * <p>Même marche que {@link #walkAlong}, mais on mesure au lieu de décider : c'est la grandeur qu'un
    * test peut opposer à « la destination reste bornée ».</p>
    */
   public static double clearDistance(ServerLevel level, Entity entite, double distanceMax, double pas) {
      if(!Double.isFinite(distanceMax) || !Double.isFinite(pas) || pas<=0 || distanceMax<=0)return 0;
      distanceMax=Math.min(64,distanceMax);pas=Math.min(.5,Math.max(.05,pas));
      Vec3 origine = entite.getEyePosition();
      Vec3 direction = entite.getLookAngle().normalize();
      double decalageYeux = entite.getEyeY() - entite.getY();
      double libre = 0.0;
      for (double d = pas; d <= distanceMax; d += pas) {
         Vec3 pieds = origine.add(direction.scale(d)).subtract(0.0, decalageYeux, 0.0);
         if (!isSafe(level, entite, pieds)) {
            break;
         }
         libre = d;
      }
      return libre;
   }

   /** Résumé lisible d'un verdict, pour les messages de preuve. */
   public static String describe(ServerLevel level, Entity entite, Vec3 destination) {
      String raison = refusalReason(level, entite, destination);
      return raison.isEmpty() ? "sûre" : "refusée (" + raison + ")";
   }
}
