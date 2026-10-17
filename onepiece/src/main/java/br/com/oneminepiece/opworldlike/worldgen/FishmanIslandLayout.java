package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class FishmanIslandLayout {
   private static final int PORTAL_INSET = 46;
   private static final int SPIRAL_START_INSET = 1250;
   private static final int MIN_ROUTE_LENGTH = 880;
   private static final int SPIRAL_AIR_RADIUS = 26;
   private static final int SPIRAL_SHELL_RADIUS = 33;
   private static final int SPIRAL_WATER_RADIUS = 42;
   private static final int SPIRAL_ASSIST_RADIUS = 18;
   private static final int SPIRAL_START_Z = 940;
   private static final int SPIRAL_PORTAL_Z = 940;
   private static final int SPIRAL_ROUTE_Z_PADDING = 360;
   private static final int SPIRAL_WATER_DEPTH = 4;
   private static final int SPIRAL_HEADROOM = 18;
   private static final int SPIRAL_SHELL_PADDING_Y = 2;
   private static final int PORTAL_Y = Math.max(-340, -45);
   private static final int PORTAL_AIR_RADIUS = 30;
   private static final int PORTAL_WATER_RADIUS = 44;
   private static final int JUNCTION_AIR_RADIUS = 34;
   private static final int JUNCTION_SHELL_RADIUS = 43;
   private static final int JUNCTION_WATER_RADIUS = 52;
   private static final double SPIRAL_PHASE = 0.58;
   private static final double SPIRAL_COIL_RADIUS = 128.0;
   private static final double SPIRAL_TURNS = 3.0;
   private static final int DOME_INSET = 1100;
   private static final int DOME_CENTER_Z = -2500;
   private static final int DOME_CENTER_Y = -132;
   private static final int DOME_RADIUS_X = 1000;
   private static final int DOME_RADIUS_Z = 950;
   private static final int DOME_RADIUS_Y = 193;
   private static final int DOME_CLAIM_RADIUS = 1300;
   private static final int HOT_STREAM_OFFSET_X = 118;
   private static final int HOT_STREAM_OFFSET_Z = 34;
   private static final int HOT_STREAM_WATER_RADIUS = 9;
   private static final int HOT_STREAM_SHELL_RADIUS = 14;

   private FishmanIslandLayout() {
   }

   public static int portalX() {
      return OmpWorld.worldRadius() - 46;
   }

   public static int entranceX() {
      return spiralStartX();
   }

   public static int portalZ() {
      return 940;
   }

   public static int portalY() {
      return PORTAL_Y;
   }

   public static int domeCenterX() {
      return -OmpWorld.worldRadius() + 1100;
   }

   public static int domeCenterZ() {
      return -2500;
   }

   public static int domeClaimRadius() {
      return 1300;
   }

   public static boolean isInsideDome(int x, int z) {
      return insideDomeColumn(x, z);
   }

   public static int hotStreamX() {
      return domeCenterX() + 118;
   }

   public static int hotStreamZ() {
      return domeCenterZ() + 34;
   }

   public static boolean affectsColumn(int x, int z) {
      if (!insideDomeColumn(x, z) && !insideHotStreamColumn(x, z)) {
         int routeMargin = Math.max(52, 44);
         int minX = Math.min(portalX(), spiralStartX()) - 42 - 2;
         int maxX = Math.max(portalX(), spiralStartX()) + 42 + 2;
         return x >= minX - routeMargin && x <= maxX + routeMargin && z >= routeMinZ() - routeMargin && z <= routeMaxZ() + routeMargin
            ? true
            : horizontalDistanceSquared(x, z, portalX(), portalZ()) <= 2116L;
      } else {
         return true;
      }
   }

   public static boolean reservedSurface(int x, int z) {
      if (!insideDomeColumn(x, z) && !insideHotStreamColumn(x, z)) {
         int minX = Math.min(portalX(), spiralStartX()) - 90;
         int maxX = Math.max(portalX(), spiralStartX()) + 90;
         return x >= minX && x <= maxX && z >= routeMinZ() - 70 && z <= routeMaxZ() + 70;
      } else {
         return true;
      }
   }

   public static boolean reservedAreaIntersects(int centerX, int centerZ, int radius) {
      long domeDx = (long)centerX - domeCenterX();
      long domeDz = (long)centerZ - domeCenterZ();
      long domeRadius = (long)Math.max(1000, 950) + radius + 260L;
      if (domeDx * domeDx + domeDz * domeDz <= domeRadius * domeRadius) {
         return true;
      }

      int minX = Math.min(portalX(), spiralStartX()) - radius - 160;
      int maxX = Math.max(portalX(), spiralStartX()) + radius + 160;
      return centerX >= minX && centerX <= maxX && centerZ >= routeMinZ() - radius - 160 && centerZ <= routeMaxZ() + radius + 160;
   }

   public static BlockPos entryPos(long seed) {
      int x = domeCenterX() - 230;
      int z = domeCenterZ();
      return new BlockPos(x, fishmanFloorHeight(seed, x, z) + 2, z);
   }

   public static BlockPos oceanExitPos() {
      return new BlockPos(hotStreamX() + 28, 65, hotStreamZ());
   }

   public static boolean isPortalTrigger(Entity entity) {
      double dx = entity.getX() - (portalX() + 0.5);
      double dy = entity.getY() - PORTAL_Y;
      double dz = entity.getZ() - (portalZ() + 0.5);
      return dx * dx + dz * dz <= 121.0 && Math.abs(dy) <= 8.0;
   }

   public static boolean isInApproachRoute(Entity entity) {
      FishmanIslandLayout.RouteSample sample = nearestSpiralSample(entity.getX(), entity.getY() + 0.6, entity.getZ());
      double minX = Math.min(spiralStartX(), portalX()) - 18.0;
      double maxX = Math.max(spiralStartX(), portalX()) + 18.0;
      int routeLevel = (int)Math.floor(sample.centerY);
      return sample.horizontalDistanceSq <= 324.0
         && entity.getY() >= routeLevel - 4 - 2.0
         && entity.getY() <= routeLevel + 18 + 2.0
         && entity.getX() >= minX
         && entity.getX() <= maxX;
   }

   public static Vec3 routeAssistVector(Entity entity) {
      FishmanIslandLayout.RouteSample sample = nearestSpiralSample(entity.getX(), entity.getY() + 0.6, entity.getZ());
      double[] here = spiralPoint(sample.t);
      double[] next = spiralPoint(clamp(sample.t + 0.014, 0.0, 1.0));
      Vec3 horizontal = new Vec3(next[0] - here[0], 0.0, next[2] - here[2]);
      if (horizontal.lengthSqr() < 1.0E-4) {
         horizontal = new Vec3(Math.signum(portalX() - spiralStartX()), 0.0, 0.0);
      }

      Vec3 forward = horizontal.normalize().scale(0.024);
      double routeY = Math.floor(sample.centerY) + 0.2;
      double vertical = Math.max(-0.08, Math.min(0.08, (routeY - entity.getY()) * 0.028));
      return new Vec3(forward.z, vertical, forward.x);
   }

   public static boolean isInHotStream(Entity entity, long seed) {
      double dx = entity.getX() - (hotStreamX() + 0.5);
      double dz = entity.getZ() - (hotStreamZ() + 0.5);
      int floor = fishmanFloorHeight(seed, hotStreamX(), hotStreamZ());
      return dx * dx + dz * dz <= 81.0 && entity.getY() >= floor + 1.0 && entity.getY() <= 69.0;
   }

   public static boolean reachedHotStreamTop(Entity entity) {
      return entity.getY() >= 64.0;
   }

   public static boolean domeIntersectsChunk(int minX, int minZ, int maxX, int maxZ) {
      return maxX >= domeCenterX() - 1000 - 8 && minX <= domeCenterX() + 1000 + 8 && maxZ >= domeCenterZ() - 950 - 8 && minZ <= domeCenterZ() + 950 + 8;
   }

   public static BlockPos[] villageAnchors(long seed) {
      return new BlockPos[]{anchored(seed, -92, -42), anchored(seed, -36, 68), anchored(seed, 58, -54), anchored(seed, 122, 50)};
   }

   public static int fishmanFloorHeight(long seed, int x, int z) {
      double dx = (x - domeCenterX()) / 1000.0;
      double dz = (z - domeCenterZ()) / 950.0;
      double center = Math.max(0.0, 1.0 - Math.sqrt(dx * dx + dz * dz));
      int terraces = (int)Math.floor(center * 4.0) * 2;
      return -307 + terraces + OnePieceGeography.noise(seed + 1179210568L, x, z, 2);
   }

   static BlockState overrideState(long seed, int x, int y, int z) {
      BlockState spiral = spiralState(x, y, z);
      if (spiral != null) {
         return spiral;
      }

      BlockState hotStream = hotStreamState(seed, x, y, z);
      return hotStream != null ? hotStream : domeState(seed, x, y, z);
   }

   static int topShellHeight(int x, int z) {
      double dx = (x - domeCenterX()) / 1000.0;
      double dz = (z - domeCenterZ()) / 950.0;
      double horizontal = dx * dx + dz * dz;
      if (horizontal > 1.1) {
         return 63;
      }

      double yRadius = Math.sqrt(Math.max(0.0, 1.0 - horizontal)) * 193.0;
      return (int)Math.round(-132.0 + yRadius + 2.0);
   }

   private static BlockState domeState(long seed, int x, int y, int z) {
      double normalized = domeNormalized(x, y, z);
      if (normalized > 1.08) {
         return null;
      } else if (normalized >= 0.93) {
         return shellState(x, y, z);
      } else {
         int floor = fishmanFloorHeight(seed, x, z);
         if (y > floor) {
            return Blocks.AIR.defaultBlockState();
         } else if (y == floor) {
            return fishmanTopState(x, z);
         } else if (y >= floor - 4) {
            return variant(x, z, 5) == 0 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
         } else {
            return y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         }
      }
   }

   private static BlockState hotStreamState(long seed, int x, int y, int z) {
      int floor = fishmanFloorHeight(seed, hotStreamX(), hotStreamZ());
      if (y >= floor + 1 && y <= 68) {
         long distSq = horizontalDistanceSquared(x, z, hotStreamX(), hotStreamZ());
         if (distSq <= 81L) {
            return Blocks.WATER.defaultBlockState();
         } else if (distSq <= 196L) {
            return variant(x + y, z, 7) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState();
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static BlockState spiralState(int x, int y, int z) {
      FishmanIslandLayout.RouteSample sample = nearestSpiralSample(x + 0.5, y + 0.5, z + 0.5);
      int routeLevel = (int)Math.floor(sample.centerY);
      if (sample.horizontalDistanceSq <= 676.0 && insideCanalY(y, routeLevel, 18)) {
         return canalState(y, routeLevel, x, z);
      } else if (sample.horizontalDistanceSq <= 1089.0 && insideShellY(y, routeLevel, 18)) {
         return routeShellState(x, y, z);
      } else if (sample.horizontalDistanceSq <= 1764.0 && insideOuterWaterY(y, routeLevel, 18)) {
         return outerRouteWaterState(y);
      } else {
         double[] junction = spiralPoint(0.58);
         double junctionDx = x - junction[0];
         double junctionDy = y - junction[1];
         double junctionDz = z - junction[2];
         double junctionDistSq = junctionDx * junctionDx + junctionDy * junctionDy + junctionDz * junctionDz;
         if (junctionDistSq <= 1156.0) {
            return Blocks.AIR.defaultBlockState();
         } else if (junctionDistSq <= 1849.0) {
            return routeShellState(x, y, z);
         } else if (junctionDistSq <= 2704.0) {
            return outerRouteWaterState(y);
         } else {
            double portalDx = x - portalX();
            double portalDy = y - PORTAL_Y;
            double portalDz = z - portalZ();
            double portalDistSq = portalDx * portalDx + portalDy * portalDy + portalDz * portalDz;
            if (portalDistSq <= 900.0) {
               return Blocks.AIR.defaultBlockState();
            } else if (portalDistSq <= 1444.0) {
               return routeShellState(x, y, z);
            } else {
               return portalDistSq <= 1936.0 ? outerRouteWaterState(y) : null;
            }
         }
      }
   }

   private static FishmanIslandLayout.RouteSample nearestSpiralSample(double x, double y, double z) {
      FishmanIslandLayout.RouteSample best = nearestCoilSample(x, y, z);
      return best.min(nearestStraightSample(x, y, z));
   }

   private static FishmanIslandLayout.RouteSample sampleSpiralPoint(double x, double y, double z, double t) {
      double[] point = spiralPoint(t);
      double dx = x - point[0];
      double dy = y - point[1];
      double dz = z - point[2];
      double horizontal = dx * dx + dz * dz;
      return new FishmanIslandLayout.RouteSample(horizontal + dy * dy * 0.42, horizontal, point[1], t);
   }

   private static FishmanIslandLayout.RouteSample nearestCoilSample(double x, double y, double z) {
      double dx = x - coilCenterX();
      double dz = z - coilCenterZ();
      double angle = Math.atan2(dz, dx);
      double start = Math.PI;
      double end = start + (Math.PI * 6);
      FishmanIslandLayout.RouteSample best = sampleSpiralPoint(x, y, z, 0.0).min(sampleSpiralPoint(x, y, z, 0.58));

      for (int turn = -1; turn <= (int)Math.ceil(3.0) + 1; turn++) {
         double candidate = angle + (Math.PI * 2) * turn;

         while (candidate < start) {
            candidate += Math.PI * 2;
         }

         if (!(candidate > end)) {
            double u = (candidate - start) / (Math.PI * 6);
            double t = clamp(u, 0.0, 1.0) * 0.58;
            best = best.min(sampleSpiralPoint(x, y, z, t));
            best = best.min(sampleSpiralPoint(x, y, z, clamp(t - 0.01, 0.0, 0.58)));
            best = best.min(sampleSpiralPoint(x, y, z, clamp(t + 0.01, 0.0, 0.58)));
         }
      }

      return best;
   }

   private static FishmanIslandLayout.RouteSample nearestStraightSample(double x, double y, double z) {
      double startX = spiralStartX();
      double endX = portalX();
      double u = clamp((x - startX) / Math.max(1.0, endX - startX), 0.0, 1.0);
      double centerX = startX + (endX - startX) * u;
      double dx = x - centerX;
      double dy = y - PORTAL_Y;
      double dz = z - 940.0;
      double horizontal = dx * dx + dz * dz;
      double t = 0.58 + u * 0.42000000000000004;
      return new FishmanIslandLayout.RouteSample(horizontal + dy * dy * 0.42, horizontal, PORTAL_Y, t);
   }

   private static double[] spiralPoint(double t) {
      if (t <= 0.58) {
         double u = clamp(t / 0.58, 0.0, 1.0);
         double angle = Math.PI + u * Math.PI * 2.0 * 3.0;
         double x = coilCenterX() + Math.cos(angle) * 128.0;
         double y = 63.0 + (PORTAL_Y - 63) * smoothStep(u);
         double z = coilCenterZ() + Math.sin(angle) * 128.0;
         return new double[]{x, y, z};
      } else {
         double u = clamp((t - 0.58) / 0.42000000000000004, 0.0, 1.0);
         double x = spiralStartX() + (portalX() - spiralStartX()) * u;
         double y = PORTAL_Y;
         double z = 940.0;
         return new double[]{x, y, z};
      }
   }

   private static int spiralStartX() {
      int portalX = portalX();
      int redLineStart = OmpWorld.worldRadius() - 0 - 1900;
      int desired = OmpWorld.worldRadius() - 1250;
      int beforeRedLine = redLineStart - 80;
      return Math.min(desired, Math.min(beforeRedLine, portalX - 880));
   }

   private static double coilCenterX() {
      return spiralStartX() + 128.0;
   }

   private static double coilCenterZ() {
      return 940.0;
   }

   private static int routeMinZ() {
      return Math.min(940, 940) - 360;
   }

   private static int routeMaxZ() {
      return Math.max(940, 940) + 360;
   }

   private static boolean insideDomeColumn(int x, int z) {
      double dx = (x - domeCenterX()) / 1006.0;
      double dz = (z - domeCenterZ()) / 956.0;
      return dx * dx + dz * dz <= 1.12;
   }

   private static boolean insideHotStreamColumn(int x, int z) {
      return horizontalDistanceSquared(x, z, hotStreamX(), hotStreamZ()) <= 256L;
   }

   private static double domeNormalized(int x, int y, int z) {
      double dx = (x - domeCenterX()) / 1000.0;
      double dy = (y - -132) / 193.0;
      double dz = (z - domeCenterZ()) / 950.0;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private static BlockState shellState(int x, int y, int z) {
      int variant = variant(x + y, z, 17);
      if (variant(x + y, z, 29) == 0) {
         return Blocks.SEA_LANTERN.defaultBlockState();
      } else {
         return variant <= 1 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.GLASS.defaultBlockState();
      }
   }

   private static BlockState canalState(int y, int routeLevel, int x, int z) {
      return y <= routeLevel ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   private static BlockState routeShellState(int x, int y, int z) {
      return variant(x + y * 31, z, 17) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : waterStopState();
   }

   private static BlockState waterStopState() {
      return (BlockState)Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15);
   }

   private static BlockState outerRouteWaterState(int y) {
      return y <= 63 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   private static boolean insideCanalY(int y, int waterLevel, int headroom) {
      return y >= waterLevel - 4 && y <= waterLevel + headroom;
   }

   private static boolean insideShellY(int y, int waterLevel, int headroom) {
      return y >= waterLevel - 4 - 2 && y <= waterLevel + headroom + 2;
   }

   private static boolean insideOuterWaterY(int y, int waterLevel, int headroom) {
      return y >= waterLevel - 4 - 2 - 1 && y <= waterLevel + headroom + 2 + 1;
   }

   private static BlockState fishmanTopState(int x, int z) {
      int variant = variant(x, z, 9);
      if (variant == 0) {
         return Blocks.SEA_LANTERN.defaultBlockState();
      } else {
         return variant <= 3 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.SAND.defaultBlockState();
      }
   }

   private static BlockPos anchored(long seed, int offsetX, int offsetZ) {
      int x = domeCenterX() + offsetX;
      int z = domeCenterZ() + offsetZ;
      return new BlockPos(x, fishmanFloorHeight(seed, x, z) + 1, z);
   }

   private static long horizontalDistanceSquared(int x, int z, int centerX, int centerZ) {
      long dx = (long)x - centerX;
      long dz = (long)z - centerZ;
      return dx * dx + dz * dz;
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double smoothStep(double value) {
      return value * value * (3.0 - 2.0 * value);
   }

   private static int variant(int x, int z, int modulo) {
      return Math.floorMod(x * 73428767 ^ z * 91293481, modulo);
   }

   private record RouteSample(double distanceSq, double horizontalDistanceSq, double centerY, double t) {
      private FishmanIslandLayout.RouteSample min(FishmanIslandLayout.RouteSample other) {
         return other.distanceSq < this.distanceSq ? other : this;
      }
   }
}
