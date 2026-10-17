package net.mcreator.minepiece.procedures;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/** Shared authority checks for B01 episodes; no lookup may silently substitute a respawned owner. */
public final class BatchRules {
   private BatchRules(){}
   public static boolean usable(ServerPlayer p,ServerLevel level,Item fruit){
      return p!=null && p.level()==level && p.isAlive() && !p.isRemoved() && !p.isSpectator()
         && !DevilFruitPowerLogic.isSuppressed(p) && DevilFruitPowerLogic.getDevilFruit(p).is(fruit);
   }
   public static boolean target(ServerPlayer p,LivingEntity t){return offensiveTarget(p,t) && p.hasLineOfSight(t);}
   public static boolean offensiveTarget(ServerPlayer p,LivingEntity t){
      return t!=p && t.isAlive() && !t.isRemoved() && t.level()==p.level() && !p.isAlliedTo(t) && CombatSkillLogic.isValidTarget(p,t)
         && !DevilFruitExplosions.isProtectedPosition((ServerLevel)p.level(),t.blockPosition());
   }
}
