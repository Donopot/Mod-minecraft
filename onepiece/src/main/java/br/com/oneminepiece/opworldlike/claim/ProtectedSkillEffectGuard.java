package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.OmpFeatureGates;
import br.com.oneminepiece.opworldlike.compat.MinePieceAddonCompat;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ProtectedSkillEffectGuard {
   private ProtectedSkillEffectGuard() {
   }

   public static boolean shouldBlockSkillEffect(Level rawLevel, BlockPos pos) {
      if (OmpFeatureGates.serverCompetition() && rawLevel instanceof ServerLevel level && level.dimension() == Level.OVERWORLD && pos != null) {
         Optional<IslandDefinition> island = IslandAccess.islandAt(level, pos);
         IslandClaim claim = island.<IslandClaim>map(value -> IslandClaimsData.get(level).get(value.id())).orElse(null);
         return island.isPresent()
            && island.get().claimable()
            && claim != null
            && claim.hasOwner()
            && GriefProtectionConfig.enabled()
            && claim.isProtectedNear(pos, GriefProtectionConfig.protectedBuildRadius());
      } else {
         return false;
      }
   }

   public static boolean shouldBlockStackMutation(Level level, BlockPos pos) {
      return shouldBlockSkillEffect(level, pos) && isAbilityMutationStack();
   }

   private static boolean isAbilityMutationStack() {
      for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
         String className = element.getClassName();
         if (MinePieceAddonCompat.isMinePieceFamilyClass(className)
            || className.startsWith("com.inovactio.awakenawakennomi.abilities.")
            || className.startsWith("com.inovactio.awakenawakennomi.entities.projectiles.")
            || className.startsWith("com.inovactio.awakenawakennomi.api.abilities.")
            || className.startsWith("com.inovactio.awakenawakennomi.effects.")
            || className.startsWith("com.inovactio.awakenawakennomi.util.DroughtBlockMapper")
            || className.startsWith("com.solmochi.mmnmaddon.")
            || className.startsWith("com.yumixlove.blackblades.events.")) {
            return true;
         }
      }

      return false;
   }
}
