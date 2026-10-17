package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public final class UniqueIslandCatalog {
   private static final List<IslandDefinition> CATALOG = CanonicalIslandCatalogV11.islands();
   public static final IslandDefinition EAST_BLUE_STARTER = catalogIsland("starter_east_blue");
   public static final IslandDefinition NORTH_BLUE_STARTER = catalogIsland("starter_north_blue");
   public static final IslandDefinition SOUTH_BLUE_STARTER = catalogIsland("starter_south_blue");
   public static final IslandDefinition WEST_BLUE_STARTER = catalogIsland("starter_west_blue");
   private static final List<IslandDefinition> STARTERS = CATALOG.stream().filter(IslandDefinition::starterIsland).toList();
   private static final List<IslandDefinition> UNIQUE = CATALOG.stream()
      .filter(island -> !island.starterIsland())
      .filter(island -> !"fishman_island".equals(island.id()))
      .toList();
   private static final int MARIEJOIS_CENTER_Z = 0;
   private static final int SKYPIEA_TARGET_X = catalogIsland("skypiea").centerX();
   private static final int SKYPIEA_TARGET_Z = catalogIsland("skypiea").centerZ();
   private static final int ZOU_TARGET_X = catalogIsland("zou").centerX();
   private static final int ZOU_TARGET_Z = catalogIsland("zou").centerZ();
   private static final int WHOLE_CAKE_TARGET_X = catalogIsland("whole_cake").centerX();
   private static final int WHOLE_CAKE_TARGET_Z = catalogIsland("whole_cake").centerZ();
   private static final int WANO_TARGET_X = catalogIsland("wano").centerX();
   private static final int WANO_TARGET_Z = catalogIsland("wano").centerZ();
   private static final int ONIGASHIMA_OFFSET_Z = 1100;

   private UniqueIslandCatalog() {
   }

   public static List<IslandDefinition> starters() {
      return STARTERS;
   }

   public static List<IslandDefinition> uniqueIslands() {
      return UNIQUE.stream().map(UniqueIslandCatalog::fitToWorldRadius).toList();
   }

   public static List<IslandDefinition> all() {
      List<IslandDefinition> islands = new ArrayList<>(STARTERS);
      islands.addAll(uniqueIslands());
      return List.copyOf(islands);
   }

   public static Optional<IslandDefinition> byId(String islandId) {
      return "fishman_island".equals(islandId) ? Optional.of(fishmanIsland()) : all().stream().filter(island -> island.id().equals(islandId)).findFirst();
   }

   public static Optional<IslandDefinition> claimOnlyAt(int x, int z) {
      return FishmanIslandLayout.isInsideDome(x, z) ? Optional.of(fishmanIsland()) : Optional.empty();
   }

   public static IslandDefinition fishmanIsland() {
      return new IslandDefinition(
         "fishman_island",
         "Fish-Man Island",
         IslandTheme.FISHMAN_ISLAND,
         FishmanIslandLayout.domeCenterX(),
         FishmanIslandLayout.domeCenterZ(),
         FishmanIslandLayout.domeClaimRadius(),
         IslandSize.LARGE,
         RegionType.GRAND_LINE,
         true,
         false,
         true,
         BiomeProfile.MANGROVE,
         IslandKind.CANONICAL_IMPORTED,
         IslandLayer.SEA_FLOOR
      );
   }

   public static Optional<IslandDefinition> at(int x, int z) {
      if (isMariejoisTerritory(x, z)) {
         return byId("mariejois");
      }

      List<IslandDefinition> islands = all();
      Optional<IslandDefinition> wano = islands.stream().filter(island -> island.theme() == IslandTheme.WANO).findFirst();
      if (wano.isPresent() && isWanoImportedTerritory(wano.get(), x, z)) {
         return wano;
      }

      Optional<IslandDefinition> skypiea = islands.stream().filter(island -> island.theme() == IslandTheme.SKYPIEA).findFirst();
      if (skypiea.isPresent() && isSkypieaImportedTerritory(skypiea.get(), x, z)) {
         return skypiea;
      }

      Optional<IslandDefinition> zou = islands.stream().filter(island -> island.theme() == IslandTheme.ZOU).findFirst();
      if (zou.isPresent() && ZouLayout.affectsColumn(zou.get(), x, z)) {
         return zou;
      }

      Optional<IslandDefinition> wholeCake = islands.stream().filter(island -> island.theme() == IslandTheme.WHOLE_CAKE).findFirst();
      return wholeCake.isPresent() && isWholeCakeImportedTerritory(wholeCake.get(), x, z)
         ? wholeCake
         : islands.stream()
            .filter(island -> island.theme() != IslandTheme.MARIEJOIS)
            .filter(island -> island.theme() != IslandTheme.ZOU)
            .filter(island -> island.theme() != IslandTheme.SKYPIEA)
            .filter(island -> island.theme() != IslandTheme.WANO)
            .filter(island -> island.theme() != IslandTheme.WHOLE_CAKE)
            .filter(island -> island.extracted() ? insideClaimRadius(island, x, z) : island.contains(x, z))
            .min(Comparator.comparingDouble(island -> island.distanceRatio(x, z)));
   }

   public static boolean territoryContains(IslandDefinition island, int x, int z) {
      if (island.id().equals("fishman_island")) {
         return FishmanIslandLayout.isInsideDome(x, z);
      } else if (island.theme() == IslandTheme.MARIEJOIS) {
         return isMariejoisTerritory(x, z);
      } else if (island.theme() == IslandTheme.WANO) {
         return isWanoImportedTerritory(island, x, z);
      } else if (island.theme() == IslandTheme.SKYPIEA) {
         return isSkypieaImportedTerritory(island, x, z);
      } else if (island.theme() == IslandTheme.ZOU) {
         return ZouLayout.affectsColumn(island, x, z);
      } else if (island.theme() == IslandTheme.WHOLE_CAKE) {
         return isWholeCakeImportedTerritory(island, x, z);
      } else {
         return island.extracted() ? insideClaimRadius(island, x, z) : island.contains(x, z);
      }
   }

   public static boolean intersectsReservedArea(IslandDefinition island, int padding) {
      return all().stream().anyMatch(unique -> intersects(island, unique, padding))
         || byId("wano").map(wano -> intersectsOnigashima(island, wano, padding)).orElse(false)
         || intersectsSkypieaJourney(island, padding)
         || intersectsImportedSpecials(island, padding)
         || FishmanIslandLayout.reservedAreaIntersects(island.centerX(), island.centerZ(), island.radius() + padding);
   }

   public static boolean intersectsRect(int minX, int minZ, int maxX, int maxZ, IslandDefinition island) {
      if (island.theme() == IslandTheme.SKYPIEA) {
         return rectanglesOverlap(minX, minZ, maxX, maxZ, island.centerX() - 1100, island.centerZ() - 860, island.centerX() + 1100, island.centerZ() + 860);
      }

      if (island.theme() == IslandTheme.ZOU) {
         return rectanglesOverlap(minX, minZ, maxX, maxZ, island.centerX() - 1050, island.centerZ() - 2200, island.centerX() + 1050, island.centerZ() + 1600);
      }

      if (island.theme() == IslandTheme.WHOLE_CAKE) {
         return rectanglesOverlap(minX, minZ, maxX, maxZ, island.centerX() - 850, island.centerZ() - 850, island.centerX() + 850, island.centerZ() + 850);
      }

      if (island.theme() == IslandTheme.WANO) {
         return rectanglesOverlap(minX, minZ, maxX, maxZ, island.centerX() - 1300, island.centerZ() - 700, island.centerX() + 1300, island.centerZ() + 1700);
      }

      int nearestX = Math.max(minX, Math.min(island.centerX(), maxX));
      int nearestZ = Math.max(minZ, Math.min(island.centerZ(), maxZ));
      return island.contains(nearestX, nearestZ);
   }

   public static BlockPos safeSpawn(long seed, IslandDefinition island) {
      int y = Math.max(68, OnePieceGeography.terrainHeight(seed, island.centerX(), island.centerZ()) + 3);
      return new BlockPos(island.centerX(), y, island.centerZ());
   }

   private static IslandDefinition catalogIsland(String id) {
      return CATALOG.stream()
         .filter(island -> island.id().equals(id))
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("Missing canonical island " + id));
   }

   private static IslandDefinition fitToWorldRadius(IslandDefinition island) {
      if (island.theme() == IslandTheme.MARIEJOIS) {
         return new IslandDefinition(
            island.id(),
            island.displayName(),
            island.theme(),
            mariejoisCenterX(),
            0,
            island.radius(),
            island.size(),
            island.region(),
            island.claimable(),
            island.starterIsland(),
            island.extracted(),
            island.biomeProfile(),
            island.kind(),
            island.layer()
         );
      }

      int x = clampToWorldRadius(island.centerX(), island.radius());
      int z = clampToWorldRadius(island.centerZ(), island.radius());
      return x == island.centerX() && z == island.centerZ()
         ? island
         : new IslandDefinition(
            island.id(),
            island.displayName(),
            island.theme(),
            x,
            z,
            island.radius(),
            island.size(),
            island.region(),
            island.claimable(),
            island.starterIsland(),
            island.extracted(),
            island.biomeProfile(),
            island.kind(),
            island.layer()
         );
   }

   private static int clampToWorldRadius(int coordinate, int islandRadius) {
      int maxAbs = Math.max(0, OmpWorld.worldRadius() - islandRadius - 500);
      return Math.max(-maxAbs, Math.min(maxAbs, coordinate));
   }

   private static boolean intersects(IslandDefinition left, IslandDefinition right, int padding) {
      long dx = (long)left.centerX() - right.centerX();
      long dz = (long)left.centerZ() - right.centerZ();
      long radius = (long)left.radius() + right.radius() + padding;
      return dx * dx + dz * dz <= radius * radius;
   }

   private static boolean insideClaimRadius(IslandDefinition island, int x, int z) {
      long dx = (long)x - island.centerX();
      long dz = (long)z - island.centerZ();
      long radius = island.radius();
      return dx * dx + dz * dz <= radius * radius;
   }

   private static boolean isOnigashimaTerritory(IslandDefinition wano, int x, int z) {
      double dx = (x - wano.centerX()) / 260.0;
      double dz = (z - (wano.centerZ() + 1100)) / 260.0;
      return dx * dx + dz * dz <= 1.0;
   }

   private static boolean isWanoImportedTerritory(IslandDefinition wano, int x, int z) {
      double dx = (x - wano.centerX()) / 1200.0;
      double dz = (z - wano.centerZ()) / 700.0;
      boolean main = dx * dx + dz * dz <= 1.0;
      boolean bridge = Math.abs(x - wano.centerX()) <= 96 && z >= wano.centerZ() + 500 && z <= wano.centerZ() + 1100 - 160;
      return main || bridge || isOnigashimaTerritory(wano, x, z);
   }

   private static boolean intersectsOnigashima(IslandDefinition island, IslandDefinition wano, int padding) {
      long dx = (long)island.centerX() - wano.centerX();
      long dz = (long)island.centerZ() - (wano.centerZ() + 1100);
      long radius = island.radius() + 260L + padding;
      return dx * dx + dz * dz <= radius * radius;
   }

   private static boolean isSkypieaImportedTerritory(IslandDefinition skypiea, int x, int z) {
      double dx = (x - skypiea.centerX()) / 1100.0;
      double dz = (z - skypiea.centerZ()) / 880.0;
      return dx * dx + dz * dz <= 1.0;
   }

   private static boolean intersectsSkypieaJourney(IslandDefinition island, int padding) {
      long dx = (long)island.centerX() - SKYPIEA_TARGET_X;
      long dz = (long)island.centerZ() - SKYPIEA_TARGET_Z;
      long radius = island.radius() + 1100L + padding;
      return dx * dx + dz * dz <= radius * radius;
   }

   private static boolean isWholeCakeImportedTerritory(IslandDefinition wholeCake, int x, int z) {
      double dx = (x - wholeCake.centerX()) / 850.0;
      double dz = (z - wholeCake.centerZ()) / 850.0;
      return dx * dx + dz * dz <= 1.0;
   }

   private static boolean intersectsImportedSpecials(IslandDefinition island, int padding) {
      return intersectsCircle(island, ZOU_TARGET_X, ZOU_TARGET_Z, 2400, padding)
         || intersectsCircle(island, WHOLE_CAKE_TARGET_X, WHOLE_CAKE_TARGET_Z, 1400, padding)
         || intersectsCircle(island, WANO_TARGET_X, WANO_TARGET_Z + 550, 1800, padding);
   }

   private static boolean intersectsCircle(IslandDefinition island, int centerX, int centerZ, int footprintRadius, int padding) {
      long dx = (long)island.centerX() - centerX;
      long dz = (long)island.centerZ() - centerZ;
      long radius = (long)island.radius() + footprintRadius + padding;
      return dx * dx + dz * dz <= radius * radius;
   }

   private static boolean rectanglesOverlap(int minX, int minZ, int maxX, int maxZ, int otherMinX, int otherMinZ, int otherMaxX, int otherMaxZ) {
      return minX <= otherMaxX && maxX >= otherMinX && minZ <= otherMaxZ && maxZ >= otherMinZ;
   }

   public static boolean isMariejoisTerritory(int x, int z) {
      return x < 0 && WorldSeamLayout.isEdgeRedLine(x) && Math.abs(x - mariejoisCenterX()) <= 2080 && z >= -1350 && z <= 1350;
   }

   public static int mariejoisCenterX() {
      return -WorldSeamLayout.edgeSeamCenter(OmpWorld.worldRadius()) + 950;
   }
}
