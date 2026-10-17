package br.com.oneminepiece.opworldlike.worldgen;

public final class ZouLayout {
   private static final int BODY_RADIUS_X = 950;
   private static final int BODY_RADIUS_Z = 1120;
   private static final int BACK_RADIUS_X = 860;
   private static final int BACK_RADIUS_Z = 1010;
   private static final int HEAD_CENTER_Z = -1260;
   private static final int HEAD_RADIUS_X = 450;
   private static final int HEAD_RADIUS_Z = 420;
   private static final int EAR_CENTER_Z = -1200;
   private static final int EAR_OFFSET_X = 500;
   private static final int EAR_RADIUS_X = 260;
   private static final int EAR_RADIUS_Z = 240;
   private static final int TRUNK_NEAR_Z = -1510;
   private static final int TRUNK_FAR_Z = -2150;
   private static final int TAIL_NEAR_Z = 1080;
   private static final int TAIL_FAR_Z = 1510;

   private ZouLayout() {
   }

   public static boolean affectsColumn(IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inBody(localX, localZ)
         || inHead(localX, localZ)
         || inEar(localX, localZ)
         || inTrunk(localX, localZ)
         || inTail(localX, localZ)
         || inLeg(localX, localZ);
   }

   public static int surfaceHeight(long seed, IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      int surface = 63;
      if (inBody(localX, localZ)) {
         surface = Math.max(surface, bodyTopY(seed, island, x, z));
      }

      if (isBackIsland(island, x, z)) {
         surface = Math.max(surface, backIslandSurfaceY(seed, island, x, z));
      }

      if (inHead(localX, localZ)) {
         surface = Math.max(surface, headTopY(seed, island, x, z));
      }

      if (inEar(localX, localZ)) {
         surface = Math.max(surface, earTopY(island, x, z));
      }

      if (inTrunk(localX, localZ)) {
         surface = Math.max(surface, trunkTopY(island, x, z));
      }

      if (inTail(localX, localZ)) {
         surface = Math.max(surface, tailTopY(island, x, z));
      }

      if (inLeg(localX, localZ)) {
         surface = Math.max(surface, legTopY());
      }

      return surface;
   }

   public static boolean isBackIsland(IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inBody(localX, localZ) && ellipseValue(localX, localZ, 860.0, 1010.0) <= 1.0;
   }

   public static int backIslandSurfaceY(long seed, IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      double value = ellipseValue(localX, localZ, 860.0, 1010.0);
      double dome = Math.sqrt(Math.max(0.0, 1.0 - value));
      return 162 + (int)Math.round(dome * 4.0);
   }

   public static int bodyTopY(long seed, IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      double dome = dome(localX, localZ, 950.0, 1120.0);
      return 145 + (int)Math.round(dome * 14.0);
   }

   public static int bodyBottomY(long seed, IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      double dome = dome(localX, localZ, 950.0, 1120.0);
      return 108 + (int)Math.round(dome * 10.0);
   }

   public static int headTopY(long seed, IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ() - -1260;
      double dome = dome(localX, localZ, 450.0, 420.0);
      return 162 + (int)Math.round(dome * 43.0);
   }

   public static int headBottomY(IslandDefinition island, int x, int z) {
      return 108;
   }

   public static int earTopY(IslandDefinition island, int x, int z) {
      double dome = earDome(island, x, z);
      return 165 + (int)Math.round(dome * 22.0);
   }

   public static int earBottomY(IslandDefinition island, int x, int z) {
      double dome = earDome(island, x, z);
      return 145 - (int)Math.round(dome * 18.0);
   }

   public static int trunkTopY(IslandDefinition island, int x, int z) {
      double t = trunkT(z - island.centerZ());
      return (int)Math.round(trunkCenterY(t) + trunkRadiusY(t));
   }

   public static int trunkBottomY(IslandDefinition island, int x, int z) {
      double t = trunkT(z - island.centerZ());
      return (int)Math.round(trunkCenterY(t) - trunkRadiusY(t));
   }

   public static int tailTopY(IslandDefinition island, int x, int z) {
      double t = tailT(z - island.centerZ());
      return (int)Math.round(tailCenterY(t) + tailRadius(t));
   }

   public static int tailBottomY(IslandDefinition island, int x, int z) {
      double t = tailT(z - island.centerZ());
      return (int)Math.round(tailCenterY(t) - tailRadius(t));
   }

   public static boolean isBodyAt(long seed, IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inBody(localX, localZ) && y >= bodyBottomY(seed, island, x, z) && y <= bodyTopY(seed, island, x, z);
   }

   public static boolean isHeadAt(long seed, IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inHead(localX, localZ) && y >= headBottomY(island, x, z) && y <= headTopY(seed, island, x, z);
   }

   public static boolean isEarAt(long seed, IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inEar(localX, localZ) && y >= earBottomY(island, x, z) && y <= earTopY(island, x, z);
   }

   public static boolean isTrunkAt(IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inTrunk(localX, localZ) && y >= trunkBottomY(island, x, z) && y <= trunkTopY(island, x, z);
   }

   public static boolean isTailAt(IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inTail(localX, localZ) && y >= tailBottomY(island, x, z) && y <= tailTopY(island, x, z);
   }

   public static boolean isLegAt(long seed, IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return inLeg(localX, localZ) && y >= -48 && y <= legTopY();
   }

   public static boolean isEyeColumn(IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      return ellipseValue(localX - 145, localZ + 1510, 22.0, 18.0) <= 1.0 || ellipseValue(localX + 145, localZ + 1510, 22.0, 18.0) <= 1.0;
   }

   public static boolean isTuskAt(IslandDefinition island, int x, int y, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ();
      if (localZ >= -1770 && localZ <= -1430) {
         double t = (-1430.0 - localZ) / 340.0;
         int offsetX = 170 + (int)Math.round(t * 45.0);
         int radius = Math.max(5, (int)Math.round(15.0 - t * 8.0));
         int centerY = (int)Math.round(142.0 - t * 52.0);
         return circleValue(localX - offsetX, y - centerY, radius) <= 1.0 || circleValue(localX + offsetX, y - centerY, radius) <= 1.0;
      } else {
         return false;
      }
   }

   private static boolean inBody(int localX, int localZ) {
      return ellipseValue(localX, localZ, 950.0, 1120.0) <= 1.0;
   }

   private static boolean inHead(int localX, int localZ) {
      return ellipseValue(localX, localZ - -1260, 450.0, 420.0) <= 1.0;
   }

   private static boolean inEar(int localX, int localZ) {
      return ellipseValue(localX - 500, localZ - -1200, 260.0, 240.0) <= 1.0 || ellipseValue(localX + 500, localZ - -1200, 260.0, 240.0) <= 1.0;
   }

   private static boolean inTrunk(int localX, int localZ) {
      if (localZ >= -2150 && localZ <= -1510) {
         double t = trunkT(localZ);
         double centerX = Math.sin(t * Math.PI * 1.15) * 70.0;
         double radiusX = 76.0 - t * 38.0;
         return Math.abs(localX - centerX) <= radiusX;
      } else {
         return false;
      }
   }

   private static boolean inTail(int localX, int localZ) {
      if (localZ >= 1080 && localZ <= 1510) {
         double t = tailT(localZ);
         double centerX = Math.sin(t * Math.PI * 1.4) * 34.0;
         return Math.abs(localX - centerX) <= tailRadius(t);
      } else {
         return false;
      }
   }

   private static boolean inLeg(int localX, int localZ) {
      return legValue(localX + 520, localZ + 580) <= 1.0
         || legValue(localX + 520, localZ - 580) <= 1.0
         || legValue(localX - 520, localZ + 580) <= 1.0
         || legValue(localX - 520, localZ - 580) <= 1.0;
   }

   private static double legValue(int x, int z) {
      return ellipseValue(x, z, 150.0, 170.0);
   }

   private static int legTopY() {
      return 124;
   }

   private static double earDome(IslandDefinition island, int x, int z) {
      int localX = x - island.centerX();
      int localZ = z - island.centerZ() - -1200;
      double left = ellipseValue(localX - 500, localZ, 260.0, 240.0);
      double right = ellipseValue(localX + 500, localZ, 260.0, 240.0);
      return Math.sqrt(Math.max(0.0, 1.0 - Math.min(left, right)));
   }

   private static double trunkT(int localZ) {
      return clamp01((-1510 - localZ) / 640.0);
   }

   private static double trunkCenterY(double t) {
      return 150.0 - t * 94.0;
   }

   private static double trunkRadiusY(double t) {
      return 58.0 - t * 30.0;
   }

   private static double tailT(int localZ) {
      return clamp01((localZ - 1080) / 430.0);
   }

   private static double tailCenterY(double t) {
      return 145.0 - t * 48.0;
   }

   private static double tailRadius(double t) {
      return t < 0.82 ? 21.0 : 42.0;
   }

   private static double dome(double x, double z, double radiusX, double radiusZ) {
      return Math.sqrt(Math.max(0.0, 1.0 - ellipseValue(x, z, radiusX, radiusZ)));
   }

   private static double ellipseValue(double x, double z, double radiusX, double radiusZ) {
      double nx = x / radiusX;
      double nz = z / radiusZ;
      return nx * nx + nz * nz;
   }

   private static double circleValue(double x, double y, double radius) {
      double nx = x / radius;
      double ny = y / radius;
      return nx * nx + ny * ny;
   }

   private static double clamp01(double value) {
      return Math.max(0.0, Math.min(1.0, value));
   }
}
