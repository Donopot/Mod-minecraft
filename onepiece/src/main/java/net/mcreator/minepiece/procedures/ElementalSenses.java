package net.mcreator.minepiece.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>Perceptions élémentaires partagées</b> — une seule règle, un seul endroit.
 *
 * <p>Ces prédicats étaient écrits dans chaque pouvoir qui en avait besoin. C'est exactement le schéma
 * qui a laissé survivre le recul d'explosion faux de Bomu pendant plusieurs fruits : la même décision
 * recopiée ailleurs dérive, et la copie qu'on oublie est celle qui casse. Ils vivent donc ici, et les
 * pouvoirs les appellent.</p>
 */
public final class ElementalSenses {

   private ElementalSenses() {
   }

   /**
    * Cible <b>mouillée</b> — eau, ou bloc d'eau occupé.
    *
    * <p>Deux mesures, pas une : le drapeau de fluide du jeu ({@code isInWater}) n'est mis à jour qu'au
    * tick <b>suivant</b> l'apparition d'une entité — une créature surgissant dans la mer est encore
    * jugée au sec. Sans la seconde lecture, le feu <b>brûle sous l'eau</b> et le sable <b>dessèche dans
    * la mer</b>. Les deux cas ont été observés pour de vrai.</p>
    */
   public static boolean isWet(LivingEntity target) {
      if (target == null) {
         return false;
      }
      if (target.isInWater()) {
         return true;
      }
      if (target.level() instanceof ServerLevel level) {
         if (level.getFluidState(target.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)) {
            return true;
         }
         // La boîte de collision couvre deux blocs : on regarde aussi celui du dessus.
         return level.getFluidState(target.blockPosition().above()).is(net.minecraft.tags.FluidTags.WATER);
      }
      return false;
   }
}
