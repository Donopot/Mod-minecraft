package net.mcreator.minepiece.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * « Sea Power » — l'affaiblissement de la mer. Appliqué aux utilisateurs de Fruit du Démon
 * lorsqu'ils nagent / sont dans l'eau. Port de {@code SeaPowerMobEffect} (ver15) : NEUTRAL,
 * couleur -10053121. La pénalité réelle (lenteur, anti-fly, anti-double-saut) est portée par
 * {@link net.mcreator.minepiece.procedures.DevilFruitPowerLogic#onDevilFruitUserTick}.
 */
public class SeaPowerMobEffect extends MobEffect {
   public SeaPowerMobEffect() {
      super(MobEffectCategory.NEUTRAL, -10053121);
   }
}
