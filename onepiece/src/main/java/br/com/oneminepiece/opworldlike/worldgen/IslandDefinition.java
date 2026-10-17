package br.com.oneminepiece.opworldlike.worldgen;

import net.minecraft.core.BlockPos;

public record IslandDefinition(
   String id,
   String displayName,
   IslandTheme theme,
   int centerX,
   int centerZ,
   int radius,
   IslandSize size,
   RegionType region,
   boolean claimable,
   boolean starterIsland,
   boolean extracted,
   BiomeProfile biomeProfile,
   IslandKind kind,
   IslandLayer layer
) {
   public boolean contains(int x, int z) {
      return this.distanceRatio(x, z) <= 1.0;
   }

   public double distanceRatio(int x, int z) {
      double edge = 0.91 + this.smoothEdgeNoise(x, z, 96) * 0.12 + this.smoothEdgeNoise(x, z, 224) * 0.06;
      return this.baseDistanceRatio(x, z) / edge;
   }

   private double baseDistanceRatio(int x, int z) {
      double dx = x - this.centerX;
      double dz = z - this.centerZ;
      double angle = unit(mix(this.id.hashCode() + 11L, 3, 7)) * Math.PI;
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double rx = dx * cos - dz * sin;
      double rz = dx * sin + dz * cos;
      double aspect = 0.72 + unit(mix(this.id.hashCode() + 29L, 5, 13)) * 0.68;
      double majorAxisScale = Math.max(aspect, 1.0 / aspect);
      return Math.sqrt(rx / aspect * (rx / aspect) + rz * aspect * (rz * aspect)) * majorAxisScale / this.radius;
   }

   public BlockPos centerAtSurface() {
      return new BlockPos(this.centerX, 70, this.centerZ);
   }

   public boolean unique() {
      return this.kind != IslandKind.MINOR_PROCEDURAL;
   }

   private double smoothEdgeNoise(int x, int z, int scale) {
      int cellX = Math.floorDiv(x, scale);
      int cellZ = Math.floorDiv(z, scale);
      double tx = smoothStep((double)Math.floorMod(x, scale) / scale);
      double tz = smoothStep((double)Math.floorMod(z, scale) / scale);
      double northWest = this.edgeNoiseAt(cellX, cellZ);
      double northEast = this.edgeNoiseAt(cellX + 1, cellZ);
      double southWest = this.edgeNoiseAt(cellX, cellZ + 1);
      double southEast = this.edgeNoiseAt(cellX + 1, cellZ + 1);
      return lerp(lerp(northWest, northEast, tx), lerp(southWest, southEast, tx), tz);
   }

   private double edgeNoiseAt(int cellX, int cellZ) {
      return unit(mix(this.id.hashCode(), cellX, cellZ)) - 0.5;
   }

   private static double smoothStep(double value) {
      return value * value * (3.0 - 2.0 * value);
   }

   private static double lerp(double start, double end, double progress) {
      return start + (end - start) * progress;
   }

   private static long mix(long seed, int x, int z) {
      long h = seed ^ -7046029254386353131L;
      h ^= x * -7046029254386353131L;
      h = Long.rotateLeft(h, 27);
      h ^= z * -4658895280553007687L;
      h ^= h >>> 30;
      h *= -4658895280553007687L;
      h ^= h >>> 27;
      h *= -7723592293110705685L;
      return h ^ h >>> 31;
   }

   private static double unit(long value) {
      return (value >>> 11) * 1.110223E-16F;
   }
}
