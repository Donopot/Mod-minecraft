package br.com.oneminepiece.opworldlike.worldgen;

import java.util.Optional;
import net.minecraft.core.BlockPos;

public final class DimensionGatewayLayout {
   public static final String NETHER_GATEWAY_ID = "nether_gateway";
   public static final String END_GATEWAY_ID = "end_gateway";
   private static final int END_DUNGEON_HALF_SIZE = 54;

   private DimensionGatewayLayout() {
   }

   public static Optional<IslandDefinition> netherIsland() {
      return UniqueIslandCatalog.byId("nether_gateway");
   }

   public static Optional<IslandDefinition> endIsland() {
      return UniqueIslandCatalog.byId("end_gateway");
   }

   public static boolean isInsideNetherGateway(int x, int z) {
      return netherIsland().filter(island -> island.contains(x, z)).isPresent();
   }

   public static boolean isInsideEndGateway(int x, int z) {
      return endIsland().filter(island -> island.contains(x, z)).isPresent();
   }

   public static boolean isInsideEndDungeon(int x, int y, int z) {
      IslandDefinition island = endIsland().orElse(null);
      return island != null && island.contains(x, z) ? Math.abs(x - island.centerX()) <= 54 && Math.abs(z - island.centerZ()) <= 54 && y >= 67 : false;
   }

   public static BlockPos netherReturnPos(long seed) {
      IslandDefinition island = netherIsland().orElse(UniqueIslandCatalog.SOUTH_BLUE_STARTER);
      int x = island.centerX() + 38;
      int z = island.centerZ() + 22;
      return new BlockPos(x, OnePieceGeography.terrainHeight(seed, x, z) + 3, z);
   }

   public static BlockPos endReturnPos(long seed) {
      IslandDefinition island = endIsland().orElse(UniqueIslandCatalog.NORTH_BLUE_STARTER);
      int x = island.centerX() - 40;
      int z = island.centerZ() - 24;
      return new BlockPos(x, OnePieceGeography.terrainHeight(seed, x, z) + 3, z);
   }
}
