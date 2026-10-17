package br.com.oneminepiece.opworldlike.worldgen;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class SkypieaLayout {
   public static final int STREAM_RADIUS = 24;
   public static final int STREAM_HOLE_RADIUS = 48;
   public static final int STREAM_POOL_RADIUS = 18;
   public static final int STREAM_POOL_OFFSET_X = 78;
   private static final int STREAM_TOP_PADDING = 1;

   private SkypieaLayout() {
   }

   public static Optional<IslandDefinition> island() {
      return UniqueIslandCatalog.byId("skypiea");
   }

   public static int landingPoolX(IslandDefinition island) {
      return island.centerX() + 78;
   }

   public static int landingPoolZ(IslandDefinition island) {
      return island.centerZ();
   }

   public static BlockPos landingPos(long seed) {
      IslandDefinition island = island().orElse(UniqueIslandCatalog.SOUTH_BLUE_STARTER);
      return new BlockPos(landingPoolX(island), streamTopY(seed) + 1, landingPoolZ(island));
   }

   public static int streamTopY(long seed) {
      IslandDefinition island = island().orElse(UniqueIslandCatalog.SOUTH_BLUE_STARTER);
      int x = landingPoolX(island);
      int z = landingPoolZ(island);
      return OnePieceGeography.terrainHeight(seed, x, z) + 1;
   }

   public static int cloudBottomY(long seed, IslandDefinition island, int x, int z, int surface) {
      double ratio = Math.min(1.0, island.distanceRatio(x, z));
      double center = 1.0 - ratio;
      int thickness = 24 + (int)Math.round(center * 26.0) + Math.max(0, OnePieceGeography.noise(seed + island.id().hashCode() * 601L, x, z, 4));
      return Math.max(181, surface - thickness);
   }

   public static boolean isStreamColumn(IslandDefinition island, int x, int z) {
      return horizontalDistanceSquared(x, z, island.centerX(), island.centerZ()) <= 576L;
   }

   public static boolean isStreamHole(IslandDefinition island, int x, int z) {
      return horizontalDistanceSquared(x, z, island.centerX(), island.centerZ()) <= 2304L;
   }

   public static boolean isLandingPool(IslandDefinition island, int x, int z) {
      int poolX = landingPoolX(island);
      int poolZ = landingPoolZ(island);
      long dx = (long)x - poolX;
      long dz = (long)z - poolZ;
      boolean pool = dx * dx + dz * dz <= 324L;
      boolean canal = x >= island.centerX() && x <= poolX && Math.abs(z - island.centerZ()) <= 7;
      return pool || canal;
   }

   public static boolean isInKnockUpStream(Entity entity, long seed) {
      IslandDefinition island = island().orElse(null);
      if (island == null) {
         return false;
      }

      double dx = entity.getX() - (island.centerX() + 0.5);
      double dz = entity.getZ() - (island.centerZ() + 0.5);
      return dx * dx + dz * dz <= 841.0 && entity.getY() >= 59.0 && entity.getY() <= streamTopY(seed) + 10.0;
   }

   public static boolean reachedSkyTop(Entity entity, long seed) {
      return entity.getY() >= streamTopY(seed) - 2.0;
   }

   public static Vec3 streamAssistVector(Entity entity, long seed) {
      IslandDefinition island = island().orElse(UniqueIslandCatalog.SOUTH_BLUE_STARTER);
      double centerPullX = clamp((island.centerX() + 0.5 - entity.getX()) * 0.018, -0.18, 0.18);
      double centerPullZ = clamp((island.centerZ() + 0.5 - entity.getZ()) * 0.018, -0.18, 0.18);
      double remaining = Math.max(0.0, streamTopY(seed) - entity.getY());
      double lift = remaining > 36.0 ? 0.78 : 0.42;
      return new Vec3(centerPullX, lift, centerPullZ);
   }

   private static long horizontalDistanceSquared(int x, int z, int centerX, int centerZ) {
      long dx = (long)x - centerX;
      long dz = (long)z - centerZ;
      return dx * dx + dz * dz;
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }
}
