package net.mcreator.minepiece.procedures;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;

/**
 * Type de primitive de combat d'un move de Fruit du Démon.
 */
public enum MoveType {
   /** Coup de mêlée : cône de dégâts + knockback devant l'attaquant. */
   MELEE,
   /** Projectile : tir en ligne droite, dégâts + effet optionnel à l'impact. */
   PROJECTILE,
   /** Effet de zone : applique un effet de statut dans un rayon autour de l'attaquant. */
   EFFECT,
   /** Buff personnel : applique un effet de statut au lanceur (ex. transformation Zoan). */
   BUFF
}
