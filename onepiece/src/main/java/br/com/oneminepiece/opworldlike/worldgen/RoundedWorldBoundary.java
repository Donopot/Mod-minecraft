package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;

public final class RoundedWorldBoundary {
   private RoundedWorldBoundary() {
   }

   public static int halfWidth() {
      return OmpWorld.worldRadius();
   }

   public static int halfHeight() {
      return OmpWorld.worldHalfHeight();
   }

   public static int cornerRadius() {
      return OmpWorld.worldCornerRadius();
   }

   public static boolean contains(double x, double z) {
      return contains(x, z, 0.0);
   }

   public static boolean contains(double x, double z, double inset) {
      double width = halfWidth() - inset;
      double height = halfHeight() - inset;
      double corner = Math.max(0.0, cornerRadius() - inset);
      if (!(width < 0.0) && !(height < 0.0)) {
         double absX = Math.abs(x);
         double absZ = Math.abs(z);
         if (absX > width || absZ > height) {
            return false;
         }

         if (corner <= 0.0) {
            return true;
         }

         double dx = Math.max(0.0, absX - (width - corner));
         double dz = Math.max(0.0, absZ - (height - corner));
         return dx * dx + dz * dz <= corner * corner;
      } else {
         return false;
      }
   }

   public static double maxXAt(double z) {
      return axisLimit(Math.abs(z), halfWidth(), halfHeight(), cornerRadius());
   }

   public static double maxZAt(double x) {
      return axisLimit(Math.abs(x), halfHeight(), halfWidth(), cornerRadius());
   }

   public static double distanceToNorthSouthEdge(double x, double z) {
      return maxZAt(x) - Math.abs(z);
   }

   private static double axisLimit(double crossAxis, double primaryHalfExtent, double crossHalfExtent, double corner) {
      if (corner <= 0.0 || crossAxis <= crossHalfExtent - corner) {
         return primaryHalfExtent;
      }

      if (crossAxis >= crossHalfExtent) {
         return primaryHalfExtent - corner;
      }

      double delta = crossAxis - (crossHalfExtent - corner);
      return primaryHalfExtent - corner + Math.sqrt(Math.max(0.0, corner * corner - delta * delta));
   }
}
