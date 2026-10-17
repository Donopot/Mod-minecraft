package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class OmpStructureCatalog {
   private static final int SEA_CELL_SIZE = 1600;
   private static final int SEA_ISLAND_CLEARANCE = 384;
   private static final Identifier PIRATE_TREASURE = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_treasure");
   private static final Identifier PIRATE_SUPPLIES = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_food_ship");
   private static final Identifier MARINE_CAPTAIN = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_weapon");
   private static final Identifier MARINE_SUPPLIES = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_food_village");
   private static final Identifier SKY_HOUSE = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_food_village");
   private static final Identifier FRUIT_CACHE = OmpWorld.id("chests/fruit_cache");

   private OmpStructureCatalog() {
   }

   public static List<OmpStructureDefinition> islandStructures(IslandDefinition island) {
      List<OmpStructureDefinition> structures = new ArrayList<>();
      if (island.starterIsland()) {
         structures.add(
            new OmpStructureDefinition(
               island.id() + "_dock",
               OmpStructureKind.STARTER_DOCK,
               island.centerX() + island.radius() / 3,
               island.centerZ(),
               34,
               18,
               PIRATE_SUPPLIES,
               List.of()
            )
         );
         return structures;
      }

      switch (island.theme()) {
         case PUNK_HAZARD:
            structures.add(
               new OmpStructureDefinition("punk_hazard_lab", OmpStructureKind.PUNK_LAB, island.centerX(), island.centerZ(), 52, 42, FRUIT_CACHE, pirateMobs())
            );
            break;
         case WHOLE_CAKE:
            structures.add(
               new OmpStructureDefinition(
                  "whole_cake_castle", OmpStructureKind.CANDY_CASTLE, island.centerX(), island.centerZ(), 64, 54, FRUIT_CACHE, pirateMobs()
               )
            );
            break;
         case ALABASTA:
            structures.add(
               new OmpStructureDefinition(
                  "alabasta_castle", OmpStructureKind.DESERT_CASTLE, island.centerX() - 90, island.centerZ() - 40, 66, 58, FRUIT_CACHE, banditMobs()
               )
            );
            break;
         case DRUM:
            structures.add(
               new OmpStructureDefinition(
                  "drum_snow_fort", OmpStructureKind.SNOW_FORT, island.centerX(), island.centerZ() + 70, 48, 42, SKY_HOUSE, winterMobs()
               )
            );
            break;
         case SABAODY:
            structures.add(
               new OmpStructureDefinition(
                  "sabaody_market", OmpStructureKind.BUBBLE_MARKET, island.centerX(), island.centerZ(), 58, 48, FRUIT_CACHE, pirateMobs()
               )
            );
            break;
         case SKYPIEA:
            structures.add(
               new OmpStructureDefinition("skypiea_temple", OmpStructureKind.SKY_TEMPLE, island.centerX(), island.centerZ(), 48, 48, SKY_HOUSE, skypieaMobs())
            );
            break;
         case MARINEFORD:
            structures.add(
               new OmpStructureDefinition(
                  "marineford_fortress", OmpStructureKind.MARINE_FORT, island.centerX(), island.centerZ(), 82, 66, MARINE_CAPTAIN, marineMobs()
               )
            );
            break;
         case WANO:
            structures.add(
               new OmpStructureDefinition(
                  "wano_castle", OmpStructureKind.WANO_CASTLE, island.centerX(), island.centerZ() - 80, 70, 60, FRUIT_CACHE, pirateMobs()
               )
            );
         case ZOU:
         case BALTIGO:
         case STARTER:
         default:
            break;
         case NORMAL:
            addNormalIslandStructures(structures, island);
      }

      return structures;
   }

   public static List<OmpStructureDefinition> seaStructuresNear(long seed, int minX, int minZ, int maxX, int maxZ) {
      List<OmpStructureDefinition> structures = new ArrayList<>();
      int minCellX = Math.floorDiv(minX, 1600) - 1;
      int maxCellX = Math.floorDiv(maxX, 1600) + 1;
      int minCellZ = Math.floorDiv(minZ, 1600) - 1;
      int maxCellZ = Math.floorDiv(maxZ, 1600) + 1;

      for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
         for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
            OmpStructureDefinition structure = seaStructureForCell(seed, cellX, cellZ);
            if (structure != null && structure.intersects(minX, minZ, maxX, maxZ)) {
               structures.add(structure);
            }
         }
      }

      return structures;
   }

   private static void addNormalIslandStructures(List<OmpStructureDefinition> structures, IslandDefinition island) {
      if (island.size() != IslandSize.TINY && island.size() != IslandSize.SMALL) {
         structures.add(
            new OmpStructureDefinition(
               island.id() + "_pirate_base", OmpStructureKind.PIRATE_BASE, island.centerX(), island.centerZ(), 54, 44, PIRATE_TREASURE, pirateMobs()
            )
         );
         if (island.size() == IslandSize.HUGE) {
            structures.add(
               new OmpStructureDefinition(
                  island.id() + "_ruin",
                  OmpStructureKind.DESERT_CASTLE,
                  island.centerX() + island.radius() / 4,
                  island.centerZ() - island.radius() / 4,
                  46,
                  40,
                  FRUIT_CACHE,
                  banditMobs()
               )
            );
         }
      }
   }

   private static OmpStructureDefinition seaStructureForCell(long seed, int cellX, int cellZ) {
      long mixed = mix(seed, cellX, cellZ);
      if (unit(mixed) > 0.34) {
         return null;
      }

      int centerX = cellX * 1600 + 220 + (int)(unit(mixed >>> 8) * 1160.0);
      int centerZ = cellZ * 1600 + 220 + (int)(unit(mixed >>> 16) * 1160.0);
      if (!RoundedWorldBoundary.contains(centerX, centerZ, 300.0)) {
         return null;
      }

      if (FishmanIslandLayout.reservedAreaIntersects(centerX, centerZ, 384)) {
         return null;
      }

      if (!OnePieceGeography.claimDisabledAt(centerX, centerZ)
         && OnePieceGeography.regionAt(centerX, centerZ) != RegionType.RED_LINE
         && OnePieceGeography.islandsNear(seed, centerX - 384, centerZ - 384, centerX + 384, centerZ + 384).isEmpty()) {
         double variant = unit(mixed >>> 24);
         if (variant > 0.7) {
            return new OmpStructureDefinition(
               "sea_marine_" + cellX + "_" + cellZ, OmpStructureKind.MARINE_BATTLESHIP, centerX, centerZ, 52, 24, MARINE_SUPPLIES, marineMobs()
            );
         } else {
            return variant > 0.18
               ? new OmpStructureDefinition(
                  "sea_pirate_" + cellX + "_" + cellZ, OmpStructureKind.PIRATE_SHIP, centerX, centerZ, 50, 22, PIRATE_TREASURE, pirateMobs()
               )
               : new OmpStructureDefinition(
                  "sea_ruin_" + cellX + "_" + cellZ,
                  OmpStructureKind.SEA_RUIN,
                  centerX,
                  centerZ,
                  28,
                  28,
                  FRUIT_CACHE,
                  List.of(Identifier.fromNamespaceAndPath("minepiece", "fighting_fish"))
               );
         }
      } else {
         return null;
      }
   }

   private static List<Identifier> pirateMobs() {
      return List.of(
         Identifier.fromNamespaceAndPath("minepiece", "pirate_grunt"),
         Identifier.fromNamespaceAndPath("minepiece", "pirate_with_sword"),
         Identifier.fromNamespaceAndPath("minepiece", "pirate_brute"),
         Identifier.fromNamespaceAndPath("minepiece", "pirate_captain")
      );
   }

   private static List<Identifier> marineMobs() {
      return List.of(
         Identifier.fromNamespaceAndPath("minepiece", "marine_grunt"),
         Identifier.fromNamespaceAndPath("minepiece", "marine_with_sword"),
         Identifier.fromNamespaceAndPath("minepiece", "marine_with_gun"),
         Identifier.fromNamespaceAndPath("minepiece", "marine_captain")
      );
   }

   private static List<Identifier> banditMobs() {
      return List.of(
         Identifier.fromNamespaceAndPath("minepiece", "bandit_grunt"),
         Identifier.fromNamespaceAndPath("minepiece", "bandit_with_sword"),
         Identifier.fromNamespaceAndPath("minepiece", "bandit_brute"),
         Identifier.fromNamespaceAndPath("minepiece", "bandit_leader")
      );
   }

   private static List<Identifier> winterMobs() {
      return List.of(Identifier.fromNamespaceAndPath("minepiece", "lapahn"), Identifier.fromNamespaceAndPath("minecraft", "stray"));
   }

   private static List<Identifier> skypieaMobs() {
      return List.of(Identifier.fromNamespaceAndPath("minepiece", "skypiean_civilian"), Identifier.fromNamespaceAndPath("minepiece", "skypiean_trader"));
   }

   private static long mix(long seed, int x, int z) {
      long h = seed ^ 7640891576956012809L;
      h ^= x * -7046029254386353131L;
      h = Long.rotateLeft(h, 31);
      h ^= z * -4417276706812531889L;
      h ^= h >>> 33;
      h *= -49064778989728563L;
      h ^= h >>> 33;
      h *= -4265267296055464877L;
      return h ^ h >>> 33;
   }

   private static double unit(long value) {
      return (value >>> 11) * 1.110223E-16F;
   }
}
