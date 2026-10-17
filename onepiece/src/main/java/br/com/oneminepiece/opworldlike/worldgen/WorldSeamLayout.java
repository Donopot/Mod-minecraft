package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;

public final class WorldSeamLayout {
   public static final int EDGE_WRAP_TRIGGER_MARGIN = 16;
   public static final int EDGE_SEAM_CENTER_INSET = 0;
   public static final int EDGE_CALM_BELT_WIDTH = 3500;
   public static final int EDGE_WRAP_EXIT_BUFFER = 20;

   private WorldSeamLayout() {
   }

   public static boolean isRedLine(int x, int z) {
      return isCentralRedLine(x) || isEdgeRedLine(x);
   }

   public static boolean isCentralRedLine(int x) {
      return Math.abs(x) <= 1900;
   }

   public static boolean isEdgeRedLine(int x) {
      return Math.abs(Math.abs(x) - edgeSeamCenter(OmpWorld.worldRadius())) <= 1900;
   }

   public static boolean isCalmBelt(int x, int z) {
      return !isRedLine(x, z) && (isCentralCalmBelt(z) || isEdgeCalmBelt(x, z));
   }

   public static boolean isCentralCalmBelt(int z) {
      int absZ = Math.abs(z);
      return absZ > 3500 && absZ <= 7000;
   }

   public static boolean isEdgeCalmBelt(int x, int z) {
      return isRedLine(x, z) ? false : Math.abs(RoundedWorldBoundary.distanceToNorthSouthEdge(x, z)) <= 1750.0;
   }

   public static boolean intersectsCalmBeltBuffer(int x, int z, int radius) {
      int minZ = z - radius;
      int maxZ = z + radius;
      return rangesIntersect(minZ, maxZ, 3500, 7000)
         || rangesIntersect(minZ, maxZ, -7000, -3500)
         || RoundedWorldBoundary.distanceToNorthSouthEdge(x, z) <= 1750.0 + radius;
   }

   public static boolean shouldWrapX(double x, double z) {
      return Math.abs(x) >= RoundedWorldBoundary.maxXAt(z) - 16.0;
   }

   public static boolean shouldWrapZ(double x, double z) {
      return Math.abs(z) >= RoundedWorldBoundary.maxZAt(x) - 16.0;
   }

   public static double wrappedX(double x, double z) {
      double limit = RoundedWorldBoundary.maxXAt(z);
      return x >= 0.0 ? -limit + 20.0 : limit - 20.0;
   }

   public static double wrappedZ(double x, double z) {
      double limit = RoundedWorldBoundary.maxZAt(x);
      return z >= 0.0 ? -limit + 20.0 : limit - 20.0;
   }

   public static int redLineDistance(int x) {
      int absX = Math.abs(x);
      return Math.min(absX, Math.abs(absX - edgeSeamCenter(OmpWorld.worldRadius())));
   }

   public static int edgeSeamCenter(int radius) {
      return Math.max(0, radius - 0);
   }

   private static boolean rangesIntersect(int leftMin, int leftMax, int rightMin, int rightMax) {
      return leftMax >= rightMin && leftMin <= rightMax;
   }
}
