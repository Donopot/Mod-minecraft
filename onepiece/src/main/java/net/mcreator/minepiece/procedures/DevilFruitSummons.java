package net.mcreator.minepiece.procedures;

import net.mcreator.minepiece.entity.SummonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Gestion des invocations d'un joueur (CORE-021, complété par FRUIT-BISU_BISU-04/05).
 *
 * <p>Rassemblé ici — et non dans la commande — pour que la règle de propriété et la borne de portée
 * soient exercées par les mêmes tests que la commande utilise : une règle qui ne vit que dans une
 * commande ne se teste pas sans joueur connecté.</p>
 */
public final class DevilFruitSummons {

   private DevilFruitSummons() {
   }

   /** Nombre d'invocations vivantes appartenant au joueur, dans la portée de suivi. */
   public static int countOwn(ServerPlayer player, double radius) {
      if (!(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      int count = 0;
      for (SummonEntity summon : level.getEntitiesOfClass(SummonEntity.class,
            player.getBoundingBox().inflate(radius))) {
         if (isOwnedBy(summon, player)) {
            count++;
         }
      }
      return count;
   }

   /**
    * Applique un ordre aux invocations <b>du joueur</b> situées dans {@code radius} blocs.
    *
    * <p>Deux garanties, qui sont le critère du plan : les ordres ne portent que sur ses propres
    * invocations (celles d'un autre joueur ne sont jamais touchées) et seulement dans une portée
    * bornée.</p>
    *
    * @return nombre d'invocations effectivement commandées
    */
   public static int applyOrder(ServerPlayer player, SummonEntity.Order order, double radius) {
      if (!(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      int applied = 0;
      for (SummonEntity summon : level.getEntitiesOfClass(SummonEntity.class,
            player.getBoundingBox().inflate(radius))) {
         if (isOwnedBy(summon, player)) {
            summon.applyOrder(order);
            applied++;
         }
      }
      return applied;
   }

   /** Propriété stricte : l'invocation doit avoir le joueur pour propriétaire. */
   public static boolean isOwnedBy(SummonEntity summon, ServerPlayer player) {
      return summon.getOwner() != null && player.getUUID().equals(summon.getOwner().getUUID());
   }
}
