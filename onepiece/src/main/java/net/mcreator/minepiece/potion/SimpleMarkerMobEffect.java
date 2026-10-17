package net.mcreator.minepiece.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Effet « marqueur » (aucune logique de tick) — utilisé pour les effets utilitaires
 * du système de combat Mine Piece (cooldowns, haki, gear, fly, double jump, etc.).
 * Port Fabric 26.3 : la vraie logique de ces effets (double jump, fly) est différée ;
 * ils servent ici de cibles d'état que les procédures vérifient/appliquent.
 */
public class SimpleMarkerMobEffect extends MobEffect {
   public SimpleMarkerMobEffect(MobEffectCategory category, int color) {
      super(category, color);
   }
}
