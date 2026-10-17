package net.mcreator.minepiece.procedures;

/** Pure outcome policy shared by damaging melee and projectiles; no second immunity calculation. */
public final class CombatHitPolicy {
   private CombatHitPolicy() { }

   public static boolean confirmed(boolean accepted, double healthAndAbsorptionBefore,
                                    double healthAndAbsorptionAfter) {
      // hurtServer may return true even when a later modifier turns damage into zero.
      // Absorption counts as a real hit. A shield/invulnerability/Logia-zero hit does not.
      return accepted && Double.isFinite(healthAndAbsorptionBefore)
         && Double.isFinite(healthAndAbsorptionAfter)
         && healthAndAbsorptionAfter < healthAndAbsorptionBefore;
   }
}
