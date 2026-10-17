package br.com.oneminepiece.opworldlike.loot;

import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import br.com.oneminepiece.opworldlike.worldgen.RegionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class RegionalLootMultipliers {
   private RegionalLootMultipliers() {
   }

   public static double multiplierAt(ServerLevel level, double x, double z) {
      if (level.dimension() != Level.OVERWORLD) {
         return 1.0;
      } else {
         int blockX = (int)Math.floor(x);
         int blockZ = (int)Math.floor(z);
         IslandDefinition island = OnePieceGeography.islandAt(level.getSeed(), blockX, blockZ).orElse(null);
         if (!isNewWorldIsland(island) && !isNewWorldZone(blockX, blockZ)) {
            return !isGrandLineIsland(island) && OnePieceGeography.regionAt(blockX, blockZ) != RegionType.GRAND_LINE
               ? 1.0
               : EquipmentLootConfig.grandLineLootMultiplier();
         } else {
            return EquipmentLootConfig.newWorldLootMultiplier();
         }
      }
   }

   private static boolean isGrandLineIsland(IslandDefinition island) {
      return island != null && island.region() == RegionType.GRAND_LINE;
   }

   private static boolean isNewWorldZone(int x, int z) {
      return OnePieceGeography.regionAt(x, z) == RegionType.GRAND_LINE && x < 0;
   }

   private static boolean isNewWorldIsland(IslandDefinition island) {
      return island != null && island.region() == RegionType.GRAND_LINE && island.centerX() < 0;
   }
}
