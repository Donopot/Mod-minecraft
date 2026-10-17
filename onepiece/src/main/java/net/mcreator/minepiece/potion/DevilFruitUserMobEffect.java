package net.mcreator.minepiece.potion;

import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * « Devil Fruit User » — le marqueur permanent de « tu as un pouvoir ».
 * Port Fabric 26.3 de {@code DevilFruitUserMobEffect} (ver15) : HARMFUL, couleur -16777216.
 * Chaque tick applique la malédiction de la mer (voir {@link DevilFruitPowerLogic#onDevilFruitUserTick}).
 * En 26.3 le hook Forge {@code applyEffectTick} devient {@link #applyEffectTick(ServerLevel, LivingEntity, int)}
 * et {@code isDurationEffectTick} devient {@link #shouldApplyEffectTickThisTick(int, int)}.
 */
public class DevilFruitUserMobEffect extends MobEffect {
   public DevilFruitUserMobEffect() {
      super(MobEffectCategory.HARMFUL, -16777216);
   }

   @Override
   public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
      DevilFruitPowerLogic.onDevilFruitUserTick(entity);
      return true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
      return true; // tick à chaque tick (malédiction continue)
   }
}
