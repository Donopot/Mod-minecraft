package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.BlockPos;

public final class OnePieceGeography {
   public static final int SEA_LEVEL = 63;
   public static final int MIN_Y = -352;
   public static final int GEN_DEPTH = 864;
   public static final int MAX_Y = 511;
   public static final int RED_LINE_HALF_WIDTH = 1900;
   public static final int GRAND_LINE_HALF_WIDTH = 3500;
   public static final int CALM_BELT_OUTER = 7000;
   private static final int BLUES_CELL_SIZE = 5000;
   private static final int GRAND_LINE_CELL_SIZE = 4000;
   private static final int TINY_ISLAND_CELL_SIZE = 1800;
   public static final int REVERSE_CHANNEL_SCAN_RADIUS = 9000;
   public static final int REVERSE_CENTER_WATER_LEVEL = 176;
   public static final int ISLAND_NAVIGATION_BUFFER = 500;
   public static final int REVERSE_CHANNEL_HALF_WIDTH = 160;
   private static final int REVERSE_ENTRY_Z = 7600;
   private static final int REVERSE_ENTRY_X = 1450;
   private static final int REVERSE_SEA_MOUTH_Z = 8400;
   private static final int REVERSE_SEA_MOUTH_X = 2200;
   private static final int REVERSE_CENTER_BASIN_RADIUS = 350;
   private static final int REVERSE_SUMMIT_RADIUS = 1300;
   private static final int GRAND_LINE_OUTLET_HALF_WIDTH = 170;
   private static final int GRAND_LINE_OUTLET_LENGTH = 2200;
   private static final int NORMAL_ISLAND_CELL_MARGIN = 500;
   private static final int BLUES_ISLAND_PADDING = 1000;
   private static final int GRAND_LINE_ISLAND_PADDING = 600;
   private static final int MINOR_ISLAND_CELL_SIZE = 5000;
   private static final int MINOR_ISLAND_TOTAL = 25;
   private static final int[] NORMAL_GRID_SIZES = new int[]{5000, 4000, 1800};
   private static final String[] MINOR_NAME_START = new String[]{
      "A",
      "Be",
      "Ca",
      "Dra",
      "E",
      "Fa",
      "Ga",
      "Ha",
      "I",
      "Ka",
      "La",
      "Ma",
      "Na",
      "O",
      "Pa",
      "Qua",
      "Ra",
      "Sa",
      "Ta",
      "U",
      "Va",
      "Wa",
      "Ya",
      "Za",
      "Bra",
      "Ce",
      "Flo",
      "Gri",
      "Lu",
      "Pra",
      "Se",
      "Tri"
   };
   private static final String[] MINOR_NAME_MIDDLE = new String[]{
      "la", "re", "mi", "no", "va", "ri", "to", "sa", "len", "mar", "do", "fen", "kai", "bel", "cor", "wyn"
   };
   private static final String[] MINOR_NAME_END = new String[]{"a", "en", "is", "or", "um", "ia", "os", "ar", "eth", "on", "el", "ir", "ys", "an", "eo", "une"};
   private static final ConcurrentMap<OnePieceGeography.CellKey, Optional<IslandDefinition>> ISLAND_CELL_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentMap<OnePieceGeography.MinorCatalogKey, List<IslandDefinition>> MINOR_CATALOG_CACHE = new ConcurrentHashMap<>();
   public static final BlockPos STARTER_SPAWN = new BlockPos(
      UniqueIslandCatalog.EAST_BLUE_STARTER.centerX(), 94, UniqueIslandCatalog.EAST_BLUE_STARTER.centerZ()
   );

   private OnePieceGeography() {
   }

   public static RegionType regionAt(int x, int z) {
      int absZ = Math.abs(z);
      if (WorldSeamLayout.isRedLine(x, z)) {
         return RegionType.RED_LINE;
      } else if (absZ <= 3500) {
         return RegionType.GRAND_LINE;
      } else if (WorldSeamLayout.isCalmBelt(x, z)) {
         return RegionType.CALM_BELT;
      } else if (x > 0 && z > 0) {
         return RegionType.EAST_BLUE;
      } else if (x < 0 && z > 0) {
         return RegionType.NORTH_BLUE;
      } else {
         return x > 0 ? RegionType.SOUTH_BLUE : RegionType.WEST_BLUE;
      }
   }

   public static Optional<IslandDefinition> islandAt(long seed, int x, int z) {
      if (claimDisabledAt(x, z)) {
         return Optional.empty();
      }

      Optional<IslandDefinition> fixed = UniqueIslandCatalog.at(x, z);
      return fixed.isPresent()
         ? fixed
         : minorIslands(seed).stream().filter(island -> island.contains(x, z)).min(Comparator.comparingDouble(island -> island.distanceRatio(x, z)));
   }

   public static Optional<IslandDefinition> claimIslandAt(long seed, int x, int z) {
      Optional<IslandDefinition> claimOnly = UniqueIslandCatalog.claimOnlyAt(x, z);
      return claimOnly.isPresent() ? claimOnly : islandAt(seed, x, z);
   }

   public static List<IslandDefinition> islandsNear(long seed, int minX, int minZ, int maxX, int maxZ) {
      List<IslandDefinition> islands = new ArrayList<>();

      for (IslandDefinition island : UniqueIslandCatalog.all()) {
         if (UniqueIslandCatalog.intersectsRect(minX, minZ, maxX, maxZ, island)) {
            islands.add(island);
         }
      }

      minorIslands(seed).stream().filter(islandx -> UniqueIslandCatalog.intersectsRect(minX, minZ, maxX, maxZ, islandx)).forEach(islands::add);
      return islands;
   }

   public static List<IslandDefinition> allIslands(long seed) {
      Map<String, IslandDefinition> islands = new LinkedHashMap<>();

      for (IslandDefinition island : UniqueIslandCatalog.all()) {
         islands.put(island.id(), island);
      }

      minorIslands(seed).forEach(islandx -> islands.putIfAbsent(islandx.id(), islandx));
      List<IslandDefinition> sorted = new ArrayList<>(islands.values());
      sorted.sort(Comparator.comparing(IslandDefinition::region).thenComparingInt(IslandDefinition::centerX).thenComparingInt(IslandDefinition::centerZ));
      return sorted;
   }

   public static List<IslandDefinition> minorIslands(long seed) {
      OnePieceGeography.MinorCatalogKey key = new OnePieceGeography.MinorCatalogKey(
         seed, RoundedWorldBoundary.halfWidth(), RoundedWorldBoundary.halfHeight(), RoundedWorldBoundary.cornerRadius()
      );
      return MINOR_CATALOG_CACHE.computeIfAbsent(key, ignored -> buildMinorCatalog(seed));
   }

   private static List<IslandDefinition> buildMinorCatalog(long seed) {
      Map<RegionType, List<OnePieceGeography.MinorCandidate>> candidates = new EnumMap<>(RegionType.class);

      for (RegionType region : List.of(RegionType.EAST_BLUE, RegionType.NORTH_BLUE, RegionType.SOUTH_BLUE, RegionType.WEST_BLUE)) {
         candidates.put(region, new ArrayList<>());
      }

      int minCellX = Math.floorDiv(-RoundedWorldBoundary.halfWidth(), 5000) - 1;
      int maxCellX = Math.floorDiv(RoundedWorldBoundary.halfWidth(), 5000) + 1;
      int minCellZ = Math.floorDiv(-RoundedWorldBoundary.halfHeight(), 5000) - 1;
      int maxCellZ = Math.floorDiv(RoundedWorldBoundary.halfHeight(), 5000) + 1;

      for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
         for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
            IslandDefinition island = minorCandidate(seed, cellX, cellZ);
            if (island != null) {
               long priority = mix(seed + -3335678366873096957L, cellX, cellZ);
               candidates.get(island.region()).add(new OnePieceGeography.MinorCandidate(island, priority));
            }
         }
      }

      List<IslandDefinition> selected = new ArrayList<>(25);
      Set<String> names = new HashSet<>();
      UniqueIslandCatalog.all().stream().map(islandx -> islandx.displayName().toLowerCase(Locale.ROOT)).forEach(names::add);

      for (RegionType region : List.of(RegionType.EAST_BLUE, RegionType.NORTH_BLUE, RegionType.SOUTH_BLUE, RegionType.WEST_BLUE)) {
         List<OnePieceGeography.MinorCandidate> regional = candidates.get(region);
         regional.sort((left, right) -> Long.compareUnsigned(left.priority(), right.priority()));
         int quota = region == RegionType.EAST_BLUE ? 7 : 6;
         int accepted = 0;

         for (OnePieceGeography.MinorCandidate candidate : regional) {
            IslandDefinition island = candidate.island();
            String displayKey = island.displayName().toLowerCase(Locale.ROOT);
            if (!names.contains(displayKey) && !selected.stream().anyMatch(other -> intersects(island, other, 1000))) {
               selected.add(island);
               names.add(displayKey);
               if (++accepted == quota) {
                  break;
               }
            }
         }

         if (accepted != quota) {
            throw new IllegalStateException("Could not place compact minor-island quota for " + region + ": expected " + quota + ", got " + accepted);
         }
      }

      selected.sort(Comparator.comparing(IslandDefinition::region).thenComparingInt(IslandDefinition::centerX).thenComparingInt(IslandDefinition::centerZ));
      return List.copyOf(selected);
   }

   private static IslandDefinition minorCandidate(long seed, int cellX, int cellZ) {
      int margin = 700;
      int usableSize = 5000 - margin * 2;
      int centerX = cellX * 5000 + margin + (int)(cellUnit(seed, 5000, cellX, cellZ, 1) * usableSize);
      int centerZ = cellZ * 5000 + margin + (int)(cellUnit(seed, 5000, cellX, cellZ, 2) * usableSize);
      RegionType region = regionAt(centerX, centerZ);
      if (!List.of(RegionType.EAST_BLUE, RegionType.NORTH_BLUE, RegionType.SOUTH_BLUE, RegionType.WEST_BLUE).contains(region)) {
         return null;
      }

      int radius = IslandSize.TINY.minRadius + (int)(cellUnit(seed, 5000, cellX, cellZ, 4) * (IslandSize.TINY.maxRadius - IslandSize.TINY.minRadius + 1));
      String id = region.name().toLowerCase() + "_minor_" + cellX + "_" + cellZ;
      IslandDefinition island = new IslandDefinition(
         id,
         normalIslandDisplayName(region, 5000, cellX, cellZ),
         IslandTheme.NORMAL,
         centerX,
         centerZ,
         radius,
         IslandSize.TINY,
         region,
         true,
         false,
         false,
         normalProfile(region, IslandSize.TINY),
         IslandKind.MINOR_PROCEDURAL,
         IslandLayer.SURFACE
      );
      return violatesNavigationBuffer(island) ? null : island;
   }

   public static List<IslandDefinition> allClaimIslands(long seed) {
      Map<String, IslandDefinition> islands = new LinkedHashMap<>();

      for (IslandDefinition island : allIslands(seed)) {
         islands.put(island.id(), island);
      }

      IslandDefinition fishman = UniqueIslandCatalog.fishmanIsland();
      islands.putIfAbsent(fishman.id(), fishman);
      List<IslandDefinition> sorted = new ArrayList<>(islands.values());
      sorted.sort(Comparator.comparing(IslandDefinition::region).thenComparingInt(IslandDefinition::centerX).thenComparingInt(IslandDefinition::centerZ));
      return sorted;
   }

   public static int terrainHeight(long seed, int x, int z) {
      if (isReverseMountainChannel(x, z)) {
         return reverseChannelFloor(seed, x, z);
      } else if (isReverseMountainArea(x, z)) {
         double dist = Math.sqrt((double)x * x + (double)z * z);
         int redLine = redLineHeight(seed, x, z);
         double summit = smoothStep(clamp(1.0 - dist / 1300.0, 0.0, 1.0));
         double shoulder = smoothStep(clamp(1.0 - Math.abs(z) / 7600.0, 0.0, 1.0)) * (1.0 - Math.min(1.0, Math.abs(x) / 1900.0));
         return clampInt(redLine + (int)Math.round(summit * 82.0 + shoulder * 28.0), 163, 456);
      } else {
         RegionType region = regionAt(x, z);
         if (region == RegionType.RED_LINE) {
            return redLineHeight(seed, x, z);
         } else {
            Optional<IslandDefinition> island = islandAt(seed, x, z);
            if (island.isPresent() && !island.get().extracted()) {
               return islandSurfaceHeight(seed, island.get(), x, z);
            } else if (isCalmBelt(x, z)) {
               return 30 + noise(seed, x, z, 3);
            } else {
               return region == RegionType.GRAND_LINE ? 32 + noise(seed, x, z, 4) : 35 + noise(seed, x, z, 4);
            }
         }
      }
   }

   public static boolean isCalmBelt(int x, int z) {
      return WorldSeamLayout.isCalmBelt(x, z);
   }

   public static boolean isCalmBelt(int z) {
      return WorldSeamLayout.isCentralCalmBelt(z);
   }

   public static boolean isReverseMountainArea(int x, int z) {
      return WorldSeamLayout.isCentralRedLine(x) && Math.abs(z) <= 7600;
   }

   public static boolean isReverseMountainChannel(int x, int z) {
      int absX = Math.abs(x);
      int absZ = Math.abs(z);
      double dist = Math.sqrt((double)x * x + (double)z * z);
      if (dist <= 350.0) {
         return true;
      }

      if (x >= 0 && x <= 2200 && absZ <= 170) {
         return true;
      }

      if (absZ > 8400) {
         return false;
      }

      if (absZ > 7600) {
         double approach = reverseSeaApproachProgress(absZ);
         double targetX = 1450.0 + approach * 750.0;
         int widening = 24 + (int)Math.round(approach * 22.0);
         return Math.abs(absX - targetX) <= 160 + widening;
      }

      if (absX > 1900) {
         return false;
      }

      double progress = absZ / 7600.0;
      double targetX = progress * 1450.0;
      int widening = (int)Math.round(progress * 24.0);
      return Math.abs(absX - targetX) <= 160 + widening;
   }

   public static int reverseChannelWaterLevel(long seed, int x, int z) {
      double climb;
      if (x >= 0 && Math.abs(z) <= 170) {
         climb = 1.0 - clamp(x / 2200.0, 0.0, 1.0);
      } else {
         double routeProgress = 1.0 - clamp(Math.abs(z) / 7600.0, 0.0, 1.0);
         climb = Math.pow(smoothStep(routeProgress), 0.78);
      }

      return clampInt(63 + (int)Math.round(113.0 * climb) + noise(seed + 887L, x, z, 1), 63, 176);
   }

   public static int reverseChannelFloor(long seed, int x, int z) {
      int waterLevel = reverseChannelWaterLevel(seed, x, z);
      if (Math.abs(z) > 7600) {
         double approach = smoothStep(reverseSeaApproachProgress(Math.abs(z)));
         int oceanFloor = 35 + noise(seed + 1019L, x, z, 3);
         int rampFloor = (int)Math.round((waterLevel - 9) * (1.0 - approach) + oceanFloor * approach);
         return clampInt(rampFloor, 30, waterLevel - 5);
      } else {
         return clampInt(waterLevel - 9 + noise(seed + 991L, x, z, 2), 49, 171);
      }
   }

   private static double reverseSeaApproachProgress(int absZ) {
      return clamp((absZ - 7600) / 800.0, 0.0, 1.0);
   }

   public static boolean claimDisabledAt(int x, int z) {
      return isReverseMountainArea(x, z) || isReverseMountainChannel(x, z) || WorldSeamLayout.isEdgeCalmBelt(x, z) || FishmanIslandLayout.reservedSurface(x, z);
   }

   public static boolean isCanonicalOceanColumn(long seed, int x, int z) {
      if (!FishmanIslandLayout.affectsColumn(x, z) && !isReverseMountainArea(x, z) && !isReverseMountainChannel(x, z) && regionAt(x, z) != RegionType.RED_LINE) {
         IslandDefinition island = islandAt(seed, x, z).orElse(null);
         return island == null || island.layer() != IslandLayer.SKY;
      } else {
         return false;
      }
   }

   public static boolean isHighRedLine(int x, int z, int y) {
      if (y < 181) {
         return false;
      }

      RegionType region = regionAt(x, z);
      return region == RegionType.RED_LINE || isReverseMountainArea(x, z);
   }

   public static BlockPos safeSpawnFor(long seed, IslandDefinition island) {
      return UniqueIslandCatalog.safeSpawn(seed, island);
   }

   public static int islandSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      if (def.theme() == IslandTheme.ZOU) {
         return ZouLayout.surfaceHeight(seed, def, x, z);
      }

      int natural = naturalIslandSurfaceHeight(seed, def, x, z);
      return applyStructurePads(seed, def, x, z, natural);
   }

   private static int naturalIslandSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      if (def.theme() == IslandTheme.SKYPIEA) {
         return skypieaSurfaceHeight(seed, def, x, z);
      }

      if (def.layer() == IslandLayer.SKY) {
         return genericSkySurfaceHeight(seed, def, x, z);
      }

      int loreHeight = loreIslandSurfaceHeight(seed, def, x, z);
      if (loreHeight != Integer.MIN_VALUE) {
         return loreHeight;
      }

      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.2);
      double coast = smoothStep(clamp((1.0 - ratio) / 0.16, 0.0, 1.0));
      double shelf = smoothStep(clamp((0.91 - ratio) / 0.18, 0.0, 1.0));
      double inland = smoothStep(clamp((0.84 - ratio) / 0.62, 0.0, 1.0));
      int beach = 64 + Math.max(0, noise(seed + def.id().hashCode() * 13L, x, z, 2));
      if (ratio > 0.91) {
         return beach + (int)Math.round(coast * 3.0);
      }

      if (ratio > 0.84) {
         return beach + (int)Math.round(shelf * dramaticShoreHeight(def));
      }

      double sizeBase = switch (def.size()) {
         case TINY -> 5.0;
         case SMALL -> 8.0;
         case MEDIUM -> 14.0;
         case LARGE -> 21.0;
         case HUGE -> 28.0;
      };

      double relief = switch (def.size()) {
         case TINY -> 10.0;
         case SMALL -> 20.0;
         case MEDIUM -> 38.0;
         case LARGE -> 66.0;
         case HUGE -> 92.0;
      };

      double themeBoost = switch (def.theme()) {
         case SKYPIEA -> 48.0;
         case DRUM -> 18.0;
         case WANO -> 22.0;
         case PUNK_HAZARD -> 22.0;
         case MARINEFORD -> 8.0;
         case BALTIGO -> 24.0;
         case ALABASTA -> 6.0;
         case NETHER_GATEWAY -> 18.0;
         case END_GATEWAY -> 12.0;
         default -> 0.0;
      };
      double broadHills = smoothTerrainNoise(seed + def.id().hashCode() * 97L, x, z, 230.0);
      double smallHills = smoothTerrainNoise(seed + def.id().hashCode() * 211L, x, z, 82.0);
      double ridges = islandRidge(seed, def, x, z);
      double peaks = islandPeaks(seed, def, x, z);
      double valley = islandValley(seed, def, x, z);
      double ruggedNoise = broadHills * 12.0 + smallHills * 6.0 + noise(seed + def.id().hashCode() * 331L, x, z, 5);
      double floorNoise = ruggedNoise * inland;
      if (def.theme() == IslandTheme.PUNK_HAZARD && x >= def.centerX()) {
         double volcano = Math.max(0.0, 1.0 - def.distanceRatio(x, z) / 0.62);
         peaks += volcano * 0.95;
         themeBoost += volcano * 22.0;
      }

      if (def.theme() == IslandTheme.PUNK_HAZARD && x < def.centerX()) {
         double coldPeaks = Math.max(0.0, 1.0 - def.distanceRatio(x, z) / 0.7);
         double iceSpikes = punkHazardIceSpikes(seed, def, x, z);
         peaks += coldPeaks * 1.18 + iceSpikes * 1.75;
         ridges = Math.max(ridges, coldPeaks * 0.78 + iceSpikes * 0.62);
         themeBoost += coldPeaks * 28.0 + iceSpikes * 50.0;
         floorNoise += Math.max(0.0, smallHills) * 18.0 + iceSpikes * 22.0;
      }

      if (def.theme() == IslandTheme.ALABASTA) {
         double mesa = Math.max(0.0, Math.sin((x + def.centerX()) / 95.0) + Math.cos((z - def.centerZ()) / 120.0)) * 0.5;
         ridges = Math.max(ridges * 0.55, mesa * 0.45);
         floorNoise += Math.max(0.0, smallHills) * 9.0;
         double dune = Math.max(0.0, Math.sin((x - z) / 155.0) + Math.cos((x + z) / 180.0)) * 0.5;
         floorNoise += dune * 7.0;
      }

      if (def.theme() == IslandTheme.WANO) {
         double terrace = Math.floor(Math.max(0.0, inland * 5.0 + broadHills * 1.1)) * 3.0;
         floorNoise += terrace;
      }

      if (def.theme() == IslandTheme.DRUM) {
         peaks *= 1.35;
      }

      if (def.theme() == IslandTheme.BALTIGO) {
         ridges = Math.max(ridges, 0.48 + Math.max(0.0, broadHills) * 0.32);
         peaks += Math.max(0.0, 1.0 - def.distanceRatio(x, z) / 0.74) * 0.42;
         floorNoise += Math.max(0.0, smallHills) * 8.0;
      }

      if (def.theme() == IslandTheme.NETHER_GATEWAY) {
         double crater = Math.max(0.0, 1.0 - def.distanceRatio(x, z) / 0.58);
         ridges = Math.max(ridges, crater * 0.72);
         peaks += crater * 0.65;
         floorNoise += Math.max(0.0, smallHills) * 13.0;
      }

      if (def.theme() == IslandTheme.END_GATEWAY) {
         double plateau = Math.max(0.0, 1.0 - def.distanceRatio(x, z) / 0.72);
         ridges *= 0.45;
         peaks *= 0.38;
         floorNoise += Math.floor(plateau * 4.0) * 4.0;
      }

      double dramaticLift = relief * (0.24 + ridges * 0.55 + peaks * 0.62 - valley * 0.28) * inland;
      double height = 66.0 + shelf * dramaticShoreHeight(def) + inland * (sizeBase + themeBoost) + floorNoise + dramaticLift;

      int max = switch (def.theme()) {
         case SKYPIEA -> 236;
         case DRUM -> 260;
         default -> 218;
         case PUNK_HAZARD -> 292;
         case BALTIGO -> 246;
      };
      return clampInt((int)Math.round(height), 65, max);
   }

   private static int loreIslandSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      return switch (def.id()) {
         case "baratie" -> baratieSurfaceHeight(seed, def, x, z);
         case "tequila_wolf" -> tequilaWolfSurfaceHeight(seed, def, x, z);
         case "cactus_island" -> cactusIslandSurfaceHeight(seed, def, x, z);
         case "little_garden" -> littleGardenSurfaceHeight(seed, def, x, z);
         case "drum" -> drumSurfaceHeight(seed, def, x, z);
         case "alabasta" -> alabastaSurfaceHeight(seed, def, x, z);
         case "jaya" -> jayaSurfaceHeight(seed, def, x, z);
         case "long_ring_long_land" -> longRingSurfaceHeight(seed, def, x, z);
         case "water_7" -> waterSevenSurfaceHeight(seed, def, x, z);
         case "enies_lobby" -> eniesLobbySurfaceHeight(seed, def, x, z);
         case "thriller_bark" -> thrillerBarkSurfaceHeight(seed, def, x, z);
         case "amazon_lily" -> amazonLilySurfaceHeight(seed, def, x, z);
         case "kuraigana" -> kuraiganaSurfaceHeight(seed, def, x, z);
         case "dressrosa" -> dressrosaSurfaceHeight(seed, def, x, z);
         case "green_bit" -> greenBitSurfaceHeight(seed, def, x, z);
         case "whole_cake" -> wholeCakeSurfaceHeight(seed, def, x, z);
         case "wano" -> wanoSurfaceHeight(seed, def, x, z);
         case "egghead" -> eggheadSurfaceHeight(seed, def, x, z);
         case "hachinosu" -> hachinosuSurfaceHeight(seed, def, x, z);
         case "elbaph" -> elbaphSurfaceHeight(seed, def, x, z);
         case "lodestar" -> lodestarSurfaceHeight(seed, def, x, z);
         case "end_gateway" -> laughTaleSurfaceHeight(seed, def, x, z);
         default -> Integer.MIN_VALUE;
      };
   }

   private static int baratieSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.15);
      double inland = smoothStep(clamp((0.78 - ratio) / 0.4, 0.0, 1.0));
      double lx = (double)(x - def.centerX()) / def.radius();
      double lz = (double)(z - def.centerZ()) / def.radius();
      double deck = Math.exp(-(lx * lx * 5.0 + lz * lz * 18.0));
      double dock = Math.exp(-lz * lz * 160.0) * clamp(1.0 - Math.abs(lx) * 0.88, 0.0, 1.0);
      double height = 64.0 + inland * 3.0 + Math.max(deck, dock) * 8.0 + noise(seed + 195798814L, x, z, 1);
      return clampInt((int)Math.round(height), 64, 76);
   }

   private static int tequilaWolfSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.15);
      double lx = (double)(x - def.centerX()) / def.radius();
      double lz = (double)(z - def.centerZ()) / def.radius();
      double bridge = Math.exp(-lz * lz * 420.0) * clamp(1.0 - Math.abs(lx) * 0.18, 0.0, 1.0);
      double camp = Math.exp(-(lx * lx + lz * lz) * 7.5);
      double banks = smoothStep(clamp((0.86 - ratio) / 0.28, 0.0, 1.0)) * 5.0;
      double pierPulse = Math.max(0.0, Math.cos((x - def.centerX()) / 28.0)) * bridge;
      double height = 65.0 + banks + bridge * 28.0 + camp * 8.0 + pierPulse * 4.0 + noise(seed + 2113935959L, x, z, 2);
      return clampInt((int)Math.round(height), 64, 105);
   }

   private static int cactusIslandSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.18);
      double inland = islandInland(def, x, z, 0.86, 0.58);
      double dunes = Math.max(0.0, Math.sin((x + z) / 70.0) + Math.cos((x - z) / 96.0)) * 0.5;
      double cactusPeaks = radialPeak(seed, def, x, z, 11, 0.3, -0.12, 0.09)
         + radialPeak(seed, def, x, z, 12, -0.22, 0.18, 0.08)
         + radialPeak(seed, def, x, z, 13, 0.06, 0.3, 0.07);
      double height = 66.0 + inland * (12.0 + dunes * 9.0 + cactusPeaks * 58.0) + islandShelf(def, x, z) * 7.0 + noise(seed + 13289221L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 160);
   }

   private static int littleGardenSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.88, 0.64);
      double prehistoricRoll = Math.max(0.0, smoothTerrainNoise(seed + 514837246L, x, z, 180.0));
      double volcanoA = radialPeak(seed, def, x, z, 21, -0.28, -0.08, 0.18);
      double volcanoB = radialPeak(seed, def, x, z, 22, 0.28, 0.1, 0.18);
      double basin = Math.exp(-Math.pow((x - def.centerX()) / (def.radius() * 0.18), 2.0) - Math.pow((z - def.centerZ()) / (def.radius() * 0.26), 2.0));
      double height = 67.0
         + islandShelf(def, x, z) * 9.0
         + inland * (22.0 + prehistoricRoll * 18.0 + (volcanoA + volcanoB) * 70.0 - basin * 8.0)
         + noise(seed + 438975050L, x, z, 4);
      return clampInt((int)Math.round(height), 65, 228);
   }

   private static int drumSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.9, 0.7);
      double rockies = 0.0;

      for (int i = 0; i < 7; i++) {
         double angle = i * Math.PI * 2.0 / 7.0;
         double px = Math.cos(angle) * 0.36;
         double pz = Math.sin(angle) * 0.36;
         rockies += radialPeak(seed, def, x, z, 40 + i, px, pz, 0.105);
      }

      double centerCastle = radialPeak(seed, def, x, z, 50, 0.0, 0.0, 0.16);
      double height = 68.0 + islandShelf(def, x, z) * 10.0 + inland * (20.0 + rockies * 58.0 + centerCastle * 48.0) + noise(seed + 13765920L, x, z, 4);
      return clampInt((int)Math.round(height), 65, 286);
   }

   private static int alabastaSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.88, 0.68);
      double river = alabastaRiver(def, x, z);
      double capital = radialPeak(seed, def, x, z, 61, 0.1, 0.08, 0.18);
      double rainbase = radialPeak(seed, def, x, z, 62, -0.34, -0.16, 0.11);
      double desertWaves = Math.max(0.0, Math.sin((x - z) / 130.0) + Math.cos((x + z) / 170.0)) * 0.5;
      double mesa = Math.max(0.0, Math.sin((x + def.centerX()) / 92.0) + Math.cos((z - def.centerZ()) / 118.0)) * 0.5;
      double height = 66.0
         + islandShelf(def, x, z) * 9.0
         + inland * (13.0 + desertWaves * 10.0 + mesa * 18.0 + capital * 32.0 + rainbase * 10.0)
         - river * 11.0
         + noise(seed + 2712380794L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 168);
   }

   private static int jayaSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.86, 0.58);
      double skullRidge = Math.max(radialPeak(seed, def, x, z, 71, -0.2, 0.1, 0.16), radialPeak(seed, def, x, z, 72, 0.22, 0.08, 0.16));
      double jaw = Math.exp(-Math.pow((z - def.centerZ() - def.radius() * 0.26) / (def.radius() * 0.1), 2.0))
         * clamp(1.0 - Math.abs(x - def.centerX()) / (def.radius() * 0.62), 0.0, 1.0);
      double height = 67.0
         + islandShelf(def, x, z) * 8.0
         + inland * (18.0 + skullRidge * 44.0 + jaw * 14.0 + Math.max(0.0, smoothTerrainNoise(seed + 2586L, x, z, 140.0)) * 14.0)
         + noise(seed + 16435934L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 178);
   }

   private static int longRingSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.16);
      double ring = Math.exp(-Math.pow((ratio - 0.6) * 8.5, 2.0));
      double meadow = islandInland(def, x, z, 0.92, 0.7);
      double lagoon = Math.exp(-Math.pow(ratio / 0.28, 2.0));
      double height = 65.0 + meadow * 5.0 + ring * 13.0 - lagoon * 4.0 + noise(seed + 4262L, x, z, 1);
      return clampInt((int)Math.round(height), 64, 83);
   }

   private static int waterSevenSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.12);
      double radial = clamp(1.0 - ratio, 0.0, 1.0);
      double cone = Math.pow(radial, 0.62);
      double terraces = Math.floor(cone * 7.0) * 3.0;
      double canals = Math.max(radialCanal(def, x, z, 0.0), radialCanal(def, x, z, Math.PI / 3));
      canals = Math.max(canals, radialCanal(def, x, z, Math.PI * 2.0 / 3.0));
      double aquaLagunaRim = Math.exp(-Math.pow((ratio - 0.8) * 9.0, 2.0));
      double height = 68.0 + cone * 48.0 + terraces + aquaLagunaRim * 8.0 - canals * 13.0 + noise(seed + 28679L, x, z, 2);
      return clampInt((int)Math.round(height), 66, 144);
   }

   private static int eniesLobbySurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.12);
      double plateau = smoothStep(clamp((0.86 - ratio) / 0.26, 0.0, 1.0));
      double courthouse = radialPeak(seed, def, x, z, 81, 0.0, -0.08, 0.2);
      double gateLine = Math.exp(-Math.pow((x - def.centerX()) / (def.radius() * 0.055), 2.0))
         * clamp(1.0 - Math.abs(z - def.centerZ()) / (def.radius() * 0.88), 0.0, 1.0);
      double fallsEdge = Math.exp(-Math.pow((ratio - 0.92) * 13.0, 2.0));
      double height = 73.0 + plateau * 46.0 + courthouse * 28.0 + gateLine * 8.0 + fallsEdge * 16.0 + noise(seed + 922085L, x, z, 2);
      return clampInt((int)Math.round(height), 67, 156);
   }

   private static int thrillerBarkSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.88, 0.58);
      double lx = (double)(x - def.centerX()) / def.radius();
      double lz = (double)(z - def.centerZ()) / def.radius();
      double hull = Math.exp(-lz * lz * 4.4) * clamp(1.0 - Math.abs(lx) * 0.48, 0.0, 1.0);
      double mastHill = radialPeak(seed, def, x, z, 91, 0.02, -0.18, 0.18);
      double graveyard = Math.max(0.0, smoothTerrainNoise(seed + 7408554L, x, z, 82.0));
      double height = 67.0 + islandShelf(def, x, z) * 5.0 + inland * (8.0 + hull * 20.0 + mastHill * 42.0 + graveyard * 12.0) + noise(seed + 7453345L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 142);
   }

   private static int amazonLilySurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.15);
      double inland = islandInland(def, x, z, 0.9, 0.68);
      double ring = Math.exp(-Math.pow((ratio - 0.58) * 7.5, 2.0));
      double basin = Math.exp(-Math.pow(ratio / 0.3, 2.0));
      double serpentRidge = islandRidge(seed + 2577L, def, x, z);
      double height = 68.0
         + islandShelf(def, x, z) * 8.0
         + inland * (20.0 + ring * 38.0 + serpentRidge * 14.0 - basin * 9.0)
         + noise(seed + 10658317L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 180);
   }

   private static int kuraiganaSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.86, 0.58);
      double ruinedCastle = radialPeak(seed, def, x, z, 101, -0.06, -0.04, 0.18);
      double gothicRidges = islandRidge(seed + 23070L, def, x, z) * 0.72 + islandPeaks(seed + 31L, def, x, z) * 0.6;
      double height = 68.0 + islandShelf(def, x, z) * 8.0 + inland * (18.0 + ruinedCastle * 46.0 + gothicRidges * 34.0) + noise(seed + 11602477L, x, z, 4);
      return clampInt((int)Math.round(height), 65, 186);
   }

   private static int dressrosaSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.12);
      double inland = islandInland(def, x, z, 0.88, 0.62);
      double royalPlateau = radialPeak(seed, def, x, z, 111, -0.06, 0.06, 0.2);
      double sunflowerFields = Math.max(0.0, Math.sin((x + z) / 84.0)) * inland;
      double harborCliff = Math.exp(-Math.pow((ratio - 0.8) * 8.0, 2.0));
      double height = 67.0
         + islandShelf(def, x, z) * 9.0
         + inland * (20.0 + royalPlateau * 42.0 + sunflowerFields * 12.0 + harborCliff * 18.0)
         + noise(seed + 13821274L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 174);
   }

   private static int greenBitSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.86, 0.5);
      double rootCrown = radialPeak(seed, def, x, z, 121, 0.0, 0.0, 0.26);
      double roots = islandRidge(seed + 6483617L, def, x, z);
      double height = 68.0 + islandShelf(def, x, z) * 7.0 + inland * (16.0 + rootCrown * 30.0 + roots * 18.0) + noise(seed + 103738135L, x, z, 2);
      return clampInt((int)Math.round(height), 65, 138);
   }

   private static int wholeCakeSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.14);
      double inland = islandInland(def, x, z, 0.88, 0.62);
      double cakeTower = radialPeak(seed, def, x, z, 131, 0.02, 0.02, 0.22);
      double candyHills = 0.0;

      for (int i = 0; i < 8; i++) {
         double angle = i * Math.PI * 2.0 / 8.0 + 0.2;
         candyHills += radialPeak(seed, def, x, z, 140 + i, Math.cos(angle) * 0.42, Math.sin(angle) * 0.42, 0.11);
      }

      double creamRing = Math.exp(-Math.pow((ratio - 0.64) * 8.0, 2.0));
      double height = 67.0
         + islandShelf(def, x, z) * 7.0
         + inland * (18.0 + cakeTower * 78.0 + candyHills * 16.0 + creamRing * 10.0)
         + noise(seed + 3397306958L, x, z, 3);
      return clampInt((int)Math.round(height), 65, 220);
   }

   private static int wanoSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.12);
      double inland = islandInland(def, x, z, 0.9, 0.7);
      double ringCliffs = Math.exp(-Math.pow((ratio - 0.82) * 9.5, 2.0));
      double terraces = Math.floor(Math.max(0.0, (1.0 - ratio) * 8.0 + smoothTerrainNoise(seed + 2720L, x, z, 180.0) * 1.4)) * 4.0;
      double mountFuji = radialPeak(seed, def, x, z, 151, 0.08, -0.1, 0.2);
      double waterfallCut = Math.exp(-Math.pow((x - def.centerX() - def.radius() * 0.68) / (def.radius() * 0.06), 2.0))
         * clamp(1.0 - Math.abs(z - def.centerZ()) / (def.radius() * 0.4), 0.0, 1.0);
      double height = 71.0
         + islandShelf(def, x, z) * 10.0
         + inland * (28.0 + ringCliffs * 58.0 + terraces + mountFuji * 86.0)
         - waterfallCut * 18.0
         + noise(seed + 11143840L, x, z, 3);
      return clampInt((int)Math.round(height), 66, 262);
   }

   private static int eggheadSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.88, 0.62);
      double labDome = radialPeak(seed, def, x, z, 161, 0.0, -0.05, 0.24);
      double satellitePads = 0.0;

      for (int i = 0; i < 6; i++) {
         double angle = i * Math.PI * 2.0 / 6.0;
         satellitePads = Math.max(satellitePads, radialPeak(seed, def, x, z, 170 + i, Math.cos(angle) * 0.44, Math.sin(angle) * 0.34, 0.085));
      }

      double frost = Math.max(0.0, smoothTerrainNoise(seed + 3686L, x, z, 130.0));
      double height = 68.0
         + islandShelf(def, x, z) * 8.0
         + inland * (18.0 + labDome * 48.0 + satellitePads * 18.0 + frost * 8.0)
         + noise(seed + 15101613L, x, z, 2);
      return clampInt((int)Math.round(height), 65, 164);
   }

   private static int hachinosuSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.88, 0.58);
      double skull = radialPeak(seed, def, x, z, 181, 0.0, -0.05, 0.26);
      double leftHorn = radialPeak(seed, def, x, z, 182, -0.36, -0.18, 0.12);
      double rightHorn = radialPeak(seed, def, x, z, 183, 0.36, -0.18, 0.12);
      double harborBite = Math.exp(-Math.pow((z - def.centerZ() - def.radius() * 0.56) / (def.radius() * 0.16), 2.0))
         * clamp(1.0 - Math.abs(x - def.centerX()) / (def.radius() * 0.48), 0.0, 1.0);
      double height = 67.0
         + islandShelf(def, x, z) * 8.0
         + inland * (18.0 + skull * 54.0 + (leftHorn + rightHorn) * 44.0 - harborBite * 10.0)
         + noise(seed + 195838213L, x, z, 4);
      return clampInt((int)Math.round(height), 65, 208);
   }

   private static int elbaphSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double inland = islandInland(def, x, z, 0.9, 0.7);
      double worldTree = radialPeak(seed, def, x, z, 191, 0.0, 0.0, 0.24);
      double roots = islandRidge(seed + 924591L, def, x, z);
      double giantSteps = Math.floor(Math.max(0.0, (1.0 - def.distanceRatio(x, z)) * 6.0)) * 4.0;
      double height = 69.0 + islandShelf(def, x, z) * 10.0 + inland * (26.0 + worldTree * 92.0 + roots * 28.0 + giantSteps) + noise(seed + 14793456L, x, z, 4);
      return clampInt((int)Math.round(height), 65, 270);
   }

   private static int lodestarSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.12);
      double inland = islandInland(def, x, z, 0.84, 0.5);
      double angle = Math.atan2(z - def.centerZ(), x - def.centerX());
      double star = Math.pow(Math.max(0.0, Math.cos(angle * 5.0)), 8.0) * clamp(1.0 - ratio * 0.78, 0.0, 1.0);
      double finalHill = radialPeak(seed, def, x, z, 201, 0.0, 0.0, 0.22);
      double height = 67.0 + islandShelf(def, x, z) * 7.0 + inland * (14.0 + star * 26.0 + finalHill * 20.0) + noise(seed + 283006882L, x, z, 2);
      return clampInt((int)Math.round(height), 65, 136);
   }

   private static int laughTaleSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.14);
      double inland = islandInland(def, x, z, 0.86, 0.58);
      double hiddenPlateau = radialPeak(seed, def, x, z, 211, 0.0, 0.0, 0.24);
      double glyphRidges = Math.pow(Math.max(0.0, Math.cos(Math.atan2(z - def.centerZ(), x - def.centerX()) * 4.0)), 6.0) * clamp(1.0 - ratio, 0.0, 1.0);
      double height = 68.0 + islandShelf(def, x, z) * 8.0 + inland * (18.0 + hiddenPlateau * 44.0 + glyphRidges * 22.0) + noise(seed + 7053408798L, x, z, 2);
      return clampInt((int)Math.round(height), 65, 158);
   }

   private static double islandInland(IslandDefinition def, int x, int z, double start, double width) {
      return smoothStep(clamp((start - def.distanceRatio(x, z)) / width, 0.0, 1.0));
   }

   private static double islandShelf(IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.2);
      return smoothStep(clamp((0.92 - ratio) / 0.18, 0.0, 1.0));
   }

   private static double radialPeak(long seed, IslandDefinition def, int x, int z, int salt, double localX, double localZ, double width) {
      double jitterX = (unit(mix(seed + def.id().hashCode() * 13L, salt, 1)) - 0.5) * width * 0.3;
      double jitterZ = (unit(mix(seed + def.id().hashCode() * 17L, salt, 2)) - 0.5) * width * 0.3;
      double px = def.centerX() + def.radius() * (localX + jitterX);
      double pz = def.centerZ() + def.radius() * (localZ + jitterZ);
      double dx = (x - px) / (def.radius() * width);
      double dz = (z - pz) / (def.radius() * width);
      return Math.exp(-(dx * dx + dz * dz));
   }

   private static double radialCanal(IslandDefinition def, int x, int z, double angle) {
      double dx = (double)(x - def.centerX()) / def.radius();
      double dz = (double)(z - def.centerZ()) / def.radius();
      double cross = Math.abs(dx * Math.sin(angle) - dz * Math.cos(angle));
      double along = Math.abs(dx * Math.cos(angle) + dz * Math.sin(angle));
      return Math.exp(-Math.pow(cross / 0.03, 2.0)) * clamp(1.0 - along * 1.25, 0.0, 1.0);
   }

   private static double alabastaRiver(IslandDefinition def, int x, int z) {
      double localX = x - def.centerX();
      double riverZ = def.centerZ() + Math.sin(localX / 190.0) * 58.0;
      double width = def.radius() * 0.035;
      return Math.exp(-Math.pow((z - riverZ) / width, 2.0)) * clamp(1.0 - Math.abs(localX) / (def.radius() * 0.88), 0.0, 1.0);
   }

   public static BlockPos villageAnchor(long seed, IslandDefinition island, int index) {
      double angle = unit(mix(seed + island.id().hashCode(), index, 17)) * Math.PI * 2.0;
      double distance = island.radius() * (0.18 + unit(mix(seed + island.id().hashCode(), index, 23)) * 0.36);
      int x = island.centerX() + (int)Math.round(Math.cos(angle) * distance);
      int z = island.centerZ() + (int)Math.round(Math.sin(angle) * distance);
      return new BlockPos(x, 0, z);
   }

   public static BlockPos explorationAnchor(long seed, IslandDefinition island, int index) {
      double angle = unit(mix(seed + island.id().hashCode() * 31L, index, 41)) * Math.PI * 2.0;
      double distance = island.radius() * (0.34 + unit(mix(seed + island.id().hashCode() * 43L, index, 47)) * 0.4);
      int x = island.centerX() + (int)Math.round(Math.cos(angle) * distance);
      int z = island.centerZ() + (int)Math.round(Math.sin(angle) * distance);
      return new BlockPos(x, 0, z);
   }

   public static int villageCount(IslandDefinition island) {
      if (!island.extracted() && !isGatewayIsland(island) && !hasSpecialStructurePlan(island)) {
         return switch (island.size()) {
            case TINY -> 0;
            case SMALL -> 0;
            case MEDIUM -> 1;
            case LARGE -> 2;
            case HUGE -> 3;
         };
      } else {
         return 0;
      }
   }

   public static int explorationSiteCount(IslandDefinition island) {
      if (!island.extracted()
         && !isGatewayIsland(island)
         && island.theme() != IslandTheme.MARIEJOIS
         && island.theme() != IslandTheme.MARINEFORD
         && island.theme() != IslandTheme.SKYPIEA
         && island.theme() != IslandTheme.ZOU) {
         int base = switch (island.size()) {
            case TINY -> 0;
            case SMALL -> 1;
            case MEDIUM -> 2;
            case LARGE -> 3;
            case HUGE -> 4;
         };
         return island.unique() ? base + 1 : base;
      } else {
         return 0;
      }
   }

   private static int applyStructurePads(long seed, IslandDefinition def, int x, int z, int natural) {
      int height = natural;
      int centerTarget = naturalIslandSurfaceHeight(seed, def, def.centerX(), def.centerZ());
      if (def.theme() != IslandTheme.SKYPIEA) {
         height = blendPad(seed, def, x, z, height, def.centerX(), def.centerZ(), 22, 62, centerTarget);
      }

      if (def.theme() == IslandTheme.SKYPIEA) {
         int poolX = SkypieaLayout.landingPoolX(def);
         int poolZ = SkypieaLayout.landingPoolZ(def);
         height = blendPad(seed, def, x, z, height, poolX, poolZ, 26, 64, naturalIslandSurfaceHeight(seed, def, poolX, poolZ));
         int[][] anchors = new int[][]{{120, 86}, {-150, 82}, {128, -155}, {-118, -126}, {0, 220}};

         for (int i = 0; i < anchors.length; i++) {
            int anchorX = def.centerX() + anchors[i][0];
            int anchorZ = def.centerZ() + anchors[i][1];
            height = blendPad(seed, def, x, z, height, anchorX, anchorZ, 36, 92, naturalIslandSurfaceHeight(seed, def, anchorX, anchorZ));
         }
      }

      if (isGatewayIsland(def)) {
         height = blendPad(seed, def, x, z, height, def.centerX(), def.centerZ(), 72, 150, Math.max(81, centerTarget));
      }

      if (def.starterIsland()) {
         int dockX = def.centerX() + def.radius() / 3;
         int dockZ = def.centerZ();
         height = blendPad(seed, def, x, z, height, dockX, dockZ, 30, 74, 67);
      }

      for (int i = 0; i < villageCount(def); i++) {
         BlockPos anchor = villageAnchor(seed, def, i);
         int target = naturalIslandSurfaceHeight(seed, def, anchor.getX(), anchor.getZ());
         height = blendPad(seed, def, x, z, height, anchor.getX(), anchor.getZ(), 54, 112, target);
      }

      for (int i = 0; i < explorationSiteCount(def); i++) {
         BlockPos anchor = explorationAnchor(seed, def, i);
         int target = naturalIslandSurfaceHeight(seed, def, anchor.getX(), anchor.getZ());
         height = blendPad(seed, def, x, z, height, anchor.getX(), anchor.getZ(), 16, 42, target);
      }

      return height;
   }

   private static int blendPad(
      long seed, IslandDefinition def, int x, int z, int current, int padX, int padZ, int innerRadius, int outerRadius, int targetHeight
   ) {
      int dx = x - padX;
      int dz = z - padZ;
      double distance = Math.sqrt((double)dx * dx + (double)dz * dz);
      if (distance > outerRadius) {
         return current;
      }

      double weight = distance <= innerRadius ? 1.0 : 1.0 - smoothStep((distance - innerRadius) / Math.max(1.0, outerRadius - innerRadius));
      int stableTarget = targetHeight + noise(seed + def.id().hashCode() * 503L + padX * 31L + padZ, x, z, 1);
      return (int)Math.round(current * (1.0 - weight) + stableTarget * weight);
   }

   private static int dramaticShoreHeight(IslandDefinition def) {
      return switch (def.size()) {
         case TINY -> 4;
         case SMALL -> 6;
         case MEDIUM -> 8;
         case LARGE -> 11;
         case HUGE -> 14;
      };
   }

   private static boolean isGatewayIsland(IslandDefinition island) {
      return island.theme() == IslandTheme.NETHER_GATEWAY || island.theme() == IslandTheme.END_GATEWAY;
   }

   private static boolean hasSpecialStructurePlan(IslandDefinition island) {
      return island.theme() == IslandTheme.MARINEFORD
         || island.theme() == IslandTheme.MARIEJOIS
         || island.theme() == IslandTheme.BALTIGO
         || island.theme() == IslandTheme.PUNK_HAZARD
         || island.theme() == IslandTheme.SKYPIEA
         || island.theme() == IslandTheme.ZOU;
   }

   private static int skypieaSurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.18);
      double island = smoothStep(clamp((1.05 - ratio) / 0.32, 0.0, 1.0));
      double inland = smoothStep(clamp((0.88 - ratio) / 0.72, 0.0, 1.0));
      double broad = smoothTerrainNoise(seed + def.id().hashCode() * 701L, x, z, 260.0);
      double small = smoothTerrainNoise(seed + def.id().hashCode() * 709L, x, z, 92.0);
      double ridges = islandRidge(seed + 6001989129762849100L, def, x, z);
      double peaks = islandPeaks(seed + 4706349869266714962L, def, x, z);
      double plains = Math.max(0.0, 1.0 - Math.abs(broad) * 0.65) * inland;
      double height = 232.0
         + island * 16.0
         + inland * (22.0 + Math.max(0.0, broad) * 18.0 + small * 8.0)
         + ridges * 42.0 * inland
         + peaks * 58.0 * inland
         - plains * 10.0;
      if (SkypieaLayout.isStreamHole(def, x, z)) {
         height += 3.0;
      }

      return clampInt((int)Math.round(height), 218, 356);
   }

   private static int genericSkySurfaceHeight(long seed, IslandDefinition def, int x, int z) {
      double ratio = clamp(def.distanceRatio(x, z), 0.0, 1.18);
      double island = smoothStep(clamp((1.05 - ratio) / 0.3, 0.0, 1.0));
      double inland = smoothStep(clamp((0.88 - ratio) / 0.7, 0.0, 1.0));
      double broad = smoothTerrainNoise(seed + def.id().hashCode() * 733L, x, z, 210.0);
      double detail = smoothTerrainNoise(seed + def.id().hashCode() * 739L, x, z, 76.0);
      double height = 218.0 + island * 12.0 + inland * (16.0 + Math.max(0.0, broad) * 20.0 + detail * 7.0);
      return clampInt((int)Math.round(height), 214, 292);
   }

   private static double islandRidge(long seed, IslandDefinition def, int x, int z) {
      double[] rotated = islandLocal(seed + def.id().hashCode() * 17L, def, x, z, 53);
      double along = Math.max(0.0, 1.0 - Math.abs(rotated[0]) * 0.74);
      double across = Math.exp(-Math.abs(rotated[1]) * 4.2);
      double secondary = Math.exp(-Math.abs(rotated[0] + rotated[1] * 0.42) * 5.0) * Math.max(0.0, 1.0 - Math.abs(rotated[1]) * 0.88);
      return clamp(along * across + secondary * 0.45, 0.0, 1.0);
   }

   private static double islandValley(long seed, IslandDefinition def, int x, int z) {
      double[] rotated = islandLocal(seed + def.id().hashCode() * 19L, def, x, z, 67);
      return Math.exp(-Math.abs(rotated[1] - 0.28) * 7.0) * Math.max(0.0, 1.0 - Math.abs(rotated[0]) * 0.95);
   }

   private static double islandPeaks(long seed, IslandDefinition def, int x, int z) {
      double first = islandPeak(seed, def, x, z, 71, 0.22);
      double second = def.size() != IslandSize.TINY && def.size() != IslandSize.SMALL ? islandPeak(seed, def, x, z, 83, 0.17) : 0.0;
      return clamp(first + second * 0.72, 0.0, 1.25);
   }

   private static double punkHazardIceSpikes(long seed, IslandDefinition def, int x, int z) {
      if (x >= def.centerX()) {
         return 0.0;
      }

      double combined = 0.0;

      for (int i = 0; i < 9; i++) {
         long salt = seed + def.id().hashCode() * 113L + i * 40503L;
         double px = def.centerX() - def.radius() * (0.18 + unit(mix(salt, i, 11)) * 0.54);
         double pz = def.centerZ() + def.radius() * ((unit(mix(salt, i, 23)) - 0.5) * 1.18);
         double width = def.radius() * (0.035 + unit(mix(salt, i, 37)) * 0.045);
         double dx = (x - px) / width;
         double dz = (z - pz) / width;
         combined += Math.exp(-(dx * dx + dz * dz)) * (0.72 + unit(mix(salt, i, 41)) * 0.48);
      }

      double coldSide = clamp((def.centerX() - x) / (def.radius() * 0.72), 0.0, 1.0);
      double coastFade = smoothStep(clamp((0.92 - def.distanceRatio(x, z)) / 0.28, 0.0, 1.0));
      return clamp(combined * coldSide * coastFade, 0.0, 1.35);
   }

   private static double islandPeak(long seed, IslandDefinition def, int x, int z, int salt, double width) {
      double angle = unit(mix(seed + def.id().hashCode() * 29L, salt, 5)) * Math.PI * 2.0;
      double distance = def.radius() * (0.1 + unit(mix(seed + def.id().hashCode() * 31L, salt, 9)) * 0.28);
      double px = def.centerX() + Math.cos(angle) * distance;
      double pz = def.centerZ() + Math.sin(angle) * distance;
      double dx = (x - px) / (def.radius() * width);
      double dz = (z - pz) / (def.radius() * width);
      return Math.exp(-(dx * dx + dz * dz));
   }

   private static double[] islandLocal(long seed, IslandDefinition def, int x, int z, int salt) {
      double angle = unit(mix(seed, salt, def.id().hashCode())) * Math.PI * 2.0;
      double dx = (double)(x - def.centerX()) / def.radius();
      double dz = (double)(z - def.centerZ()) / def.radius();
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      return new double[]{dx * cos - dz * sin, dx * sin + dz * cos};
   }

   static int noise(long seed, int x, int z, int amplitude) {
      long a = mix(seed + 31L, Math.floorDiv(x, 24), Math.floorDiv(z, 24));
      long b = mix(seed + 79L, Math.floorDiv(x, 96), Math.floorDiv(z, 96));
      double value = (unit(a) - 0.5) * amplitude + (unit(b) - 0.5) * amplitude;
      return (int)Math.round(value);
   }

   private static IslandDefinition islandForCell(long seed, int cellSize, int cellX, int cellZ) {
      return ISLAND_CELL_CACHE.computeIfAbsent(
            new OnePieceGeography.CellKey(seed, OmpWorld.worldRadius(), cellSize, cellX, cellZ),
            key -> Optional.ofNullable(computeIslandForCell(key.seed, key.cellSize, key.cellX, key.cellZ))
         )
         .orElse(null);
   }

   private static IslandDefinition computeIslandForCell(long seed, int cellSize, int cellX, int cellZ) {
      IslandDefinition island = rawIslandForCell(seed, cellSize, cellX, cellZ);
      return island != null
            && !violatesNavigationBuffer(island)
            && !UniqueIslandCatalog.intersectsReservedArea(island, normalIslandPadding(island.region()))
            && !losesToNeighbor(seed, cellSize, cellX, cellZ, island)
         ? island
         : null;
   }

   private static boolean violatesNavigationBuffer(IslandDefinition island) {
      int radius = island.radius() + 500;
      if (!crossesWorldBorder(island.centerX(), island.centerZ(), radius) && !intersectsRedLineBuffer(island.centerX(), radius)) {
         if (intersectsCalmBeltBuffer(island.centerX(), island.centerZ(), radius)) {
            return true;
         } else {
            return tooCloseToReverseMountain(island.centerX(), island.centerZ(), radius)
               ? true
               : tooCloseToReverseChannel(island.centerX(), island.centerZ(), radius)
                  || FishmanIslandLayout.reservedAreaIntersects(island.centerX(), island.centerZ(), radius);
         }
      } else {
         return true;
      }
   }

   private static boolean intersectsCalmBeltBuffer(int x, int z, int radius) {
      return WorldSeamLayout.intersectsCalmBeltBuffer(x, z, radius);
   }

   private static boolean crossesWorldBorder(int x, int z, int radius) {
      return !RoundedWorldBoundary.contains(x, z, radius);
   }

   private static boolean intersectsRedLineBuffer(int x, int radius) {
      int absX = Math.abs(x);
      int worldRadius = OmpWorld.worldRadius();
      return absX <= 1900 + radius || absX >= worldRadius - 1900 - radius;
   }

   private static boolean tooCloseToReverseMountain(int x, int z, int radius) {
      return Math.abs(x) <= 1900 + radius && Math.abs(z) <= 7600 + radius;
   }

   private static boolean tooCloseToReverseChannel(int x, int z, int radius) {
      boolean entryZone = Math.abs(x) <= 1900 + radius && Math.abs(z) <= 7600 + radius;
      boolean outletZone = x >= -radius && x <= 2200 + radius && Math.abs(z) <= 170 + radius;
      return entryZone || outletZone;
   }

   private static int redLineHeight(long seed, int x, int z) {
      double ridge = 1.0 - Math.min(1.0, WorldSeamLayout.redLineDistance(x) / 1900.0);
      double coastalBreak = Math.sin(z / 230.0) * 7.0 + Math.sin(x / 170.0) * 5.0;
      return clampInt(205 + (int)(ridge * 248.0) + (int)coastalBreak + noise(seed, x, z, 30), 159, 456);
   }

   private static boolean rangesIntersect(int leftMin, int leftMax, int rightMin, int rightMax) {
      return leftMax >= rightMin && leftMin <= rightMax;
   }

   private static IslandDefinition rawIslandForCell(long seed, int cellSize, int cellX, int cellZ) {
      double roll = cellUnit(seed, cellSize, cellX, cellZ, 0);
      int baseX = cellX * cellSize;
      int baseZ = cellZ * cellSize;
      int margin = Math.min(500, Math.max(140, cellSize / 6));
      int usableSize = cellSize - margin * 2;
      int centerX = baseX + margin + (int)(cellUnit(seed, cellSize, cellX, cellZ, 1) * usableSize);
      int centerZ = baseZ + margin + (int)(cellUnit(seed, cellSize, cellX, cellZ, 2) * usableSize);
      if (cellSize == 4000 && baseZ < 3500 && baseZ + cellSize > -3500) {
         int grandLineMargin = 170;
         int minZ = Math.max(baseZ + margin, -3500 + grandLineMargin);
         int maxZ = Math.min(baseZ + cellSize - margin, 3500 - grandLineMargin);
         if (minZ >= maxZ) {
            return null;
         }

         centerZ = minZ + (int)(cellUnit(seed, cellSize, cellX, cellZ, 2) * (maxZ - minZ));
      }

      if (!RoundedWorldBoundary.contains(centerX, centerZ, 300.0)) {
         return null;
      }

      RegionType region = regionAt(centerX, centerZ);
      if (cellSize != 1800 && preferredCellSize(region) != cellSize) {
         return null;
      }

      if (cellSize != 1800 || region != RegionType.RED_LINE && region != RegionType.CALM_BELT && !claimDisabledAt(centerX, centerZ)) {
         if (region != RegionType.RED_LINE && !claimDisabledAt(centerX, centerZ)) {
            double var10000;
            if (cellSize == 1800) {
               var10000 = tinyIslandChance(region, centerX);
            } else {
               switch (region) {
                  case GRAND_LINE:
                     var10000 = 0.9;
                     break;
                  case CALM_BELT:
                     var10000 = 0.0;
                     break;
                  case RED_LINE:
                     var10000 = 0.0;
                     break;
                  default:
                     var10000 = 0.58;
               }
            }

            double chance = var10000;
            if (roll > chance) {
               return null;
            }

            IslandSize size = cellSize == 1800 ? IslandSize.TINY : pickSize(region, cellUnit(seed, cellSize, cellX, cellZ, 3));
            int radius = size.minRadius + (int)(cellUnit(seed, cellSize, cellX, cellZ, 4) * (size.maxRadius - size.minRadius + 1));
            String id = region.name().toLowerCase() + "_" + cellSize + "_" + cellX + "_" + cellZ;
            String displayName = normalIslandDisplayName(region, cellSize, cellX, cellZ);
            return new IslandDefinition(
               id,
               displayName,
               IslandTheme.NORMAL,
               centerX,
               centerZ,
               radius,
               size,
               region,
               true,
               false,
               false,
               normalProfile(region, size),
               IslandKind.MINOR_PROCEDURAL,
               IslandLayer.SURFACE
            );
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static double tinyIslandChance(RegionType region, int centerX) {
      if (region == RegionType.GRAND_LINE) {
         return centerX < 0 ? 0.54 : 0.18;
      } else {
         return 0.08;
      }
   }

   private static String normalIslandDisplayName(RegionType region, int cellSize, int cellX, int cellZ) {
      int ordinal = normalIslandNameOrdinal(cellSize, cellX, cellZ);
      int value = Math.floorMod(ordinal, MINOR_NAME_START.length * MINOR_NAME_MIDDLE.length * MINOR_NAME_END.length);
      String start = MINOR_NAME_START[value % MINOR_NAME_START.length];
      value /= MINOR_NAME_START.length;
      String middle = MINOR_NAME_MIDDLE[value % MINOR_NAME_MIDDLE.length];
      value /= MINOR_NAME_MIDDLE.length;
      String end = MINOR_NAME_END[value % MINOR_NAME_END.length];

      String suffix = switch (region) {
         case GRAND_LINE -> "Isle";
         default -> "Shoal";
         case EAST_BLUE -> "Cay";
         case NORTH_BLUE -> "Key";
         case SOUTH_BLUE -> "Atoll";
         case WEST_BLUE -> "Haven";
      };
      return start + middle + end + " " + suffix;
   }

   private static int normalIslandNameOrdinal(int cellSize, int cellX, int cellZ) {
      int offset = 0;

      for (int candidateCellSize : NORMAL_GRID_SIZES) {
         int minCell = Math.floorDiv(-OmpWorld.worldRadius() - candidateCellSize, candidateCellSize);
         int maxCell = Math.floorDiv(OmpWorld.worldRadius() + candidateCellSize, candidateCellSize);
         int width = maxCell - minCell + 1;
         if (candidateCellSize == cellSize) {
            return offset + (cellX - minCell) * width + (cellZ - minCell);
         }

         offset += width * width;
      }

      return Math.floorMod(cellSize * 31 + cellX * 131 + cellZ * 521, 10000);
   }

   private static BiomeProfile normalProfile(RegionType region, IslandSize size) {
      if (region == RegionType.SOUTH_BLUE || region == RegionType.WEST_BLUE) {
         return BiomeProfile.DESERT;
      } else {
         return size != IslandSize.LARGE && size != IslandSize.HUGE ? BiomeProfile.NORMAL : BiomeProfile.JUNGLE;
      }
   }

   private static IslandSize pickSize(RegionType region, double roll) {
      return IslandSize.SMALL;
   }

   private static boolean losesToNeighbor(long seed, int cellSize, int cellX, int cellZ, IslandDefinition island) {
      double priority = cellUnit(seed, cellSize, cellX, cellZ, 5);

      for (int otherSize : NORMAL_GRID_SIZES) {
         int originCellX = Math.floorDiv(island.centerX(), otherSize);
         int originCellZ = Math.floorDiv(island.centerZ(), otherSize);

         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               int otherCellX = originCellX + dx;
               int otherCellZ = originCellZ + dz;
               if (otherSize != cellSize || otherCellX != cellX || otherCellZ != cellZ) {
                  IslandDefinition other = rawIslandForCell(seed, otherSize, otherCellX, otherCellZ);
                  if (other != null
                     && intersects(island, other, normalIslandPadding(island.region()))
                     && (island.size() == IslandSize.TINY || other.size() != IslandSize.TINY)) {
                     if (island.size() == IslandSize.TINY && other.size() != IslandSize.TINY) {
                        return true;
                     }

                     double otherPriority = cellUnit(seed, otherSize, otherCellX, otherCellZ, 5);
                     if (otherPriority < priority || otherPriority == priority && tieBreaksBefore(otherSize, otherCellX, otherCellZ, cellSize, cellX, cellZ)) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean tieBreaksBefore(int otherSize, int otherCellX, int otherCellZ, int cellSize, int cellX, int cellZ) {
      return otherSize != cellSize ? otherSize < cellSize : otherCellX < cellX || otherCellX == cellX && otherCellZ < cellZ;
   }

   private static boolean intersects(IslandDefinition left, IslandDefinition right, int padding) {
      long dx = (long)left.centerX() - right.centerX();
      long dz = (long)left.centerZ() - right.centerZ();
      long radius = (long)left.radius() + right.radius() + padding;
      return dx * dx + dz * dz <= radius * radius;
   }

   private static int preferredCellSize(RegionType region) {
      return region == RegionType.GRAND_LINE ? 4000 : 5000;
   }

   private static int normalIslandPadding(RegionType region) {
      return region == RegionType.GRAND_LINE ? 600 : 1000;
   }

   private static double smoothTerrainNoise(long seed, int x, int z, double scale) {
      double a = (seed >>> 9 & 2047L) - 1024.0;
      double b = (seed >>> 21 & 2047L) - 1024.0;
      double c = (seed >>> 33 & 2047L) - 1024.0;
      return (Math.sin((x + a) / scale) + Math.cos((z + b) / (scale * 0.83)) + Math.sin((x + z + c) / (scale * 1.37))) / 3.0;
   }

   private static double smoothStep(double value) {
      return value * value * (3.0 - 2.0 * value);
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double cellUnit(long seed, int cellSize, int x, int z, int salt) {
      return unit(mix(seed + 7146057691288625177L * (salt + 1L) + cellSize * -7046029254386353131L, x, z));
   }

   private static long mix(long seed, int x, int z) {
      long h = seed ^ -7046029254386353131L;
      h ^= x * -4658895280553007687L;
      h = Long.rotateLeft(h, 27);
      h ^= z * -7723592293110705685L;
      h ^= h >>> 30;
      h *= -4658895280553007687L;
      h ^= h >>> 27;
      h *= -7723592293110705685L;
      return h ^ h >>> 31;
   }

   private static double unit(long value) {
      return (value >>> 11) * 1.110223E-16F;
   }

   private record CellKey(long seed, int worldRadius, int cellSize, int cellX, int cellZ) {
   }

   private record MinorCandidate(IslandDefinition island, long priority) {
   }

   private record MinorCatalogKey(long seed, int halfWidth, int halfHeight, int cornerRadius) {
   }
}
