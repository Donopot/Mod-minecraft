package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class OmpStructurePlacer {
   private static final int VILLAGE_SEARCH_PADDING = 192;
   private static final int EXPLORATION_SEARCH_PADDING = 96;
   private static final Identifier VILLAGER = Identifier.fromNamespaceAndPath("minecraft", "villager");
   private static final Identifier PIRATE_SUPPLIES_LOOT = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_food_ship");
   private static final Identifier PIRATE_TREASURE_LOOT = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_treasure");
   private static final Identifier MARINE_SUPPLIES_LOOT = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_food_village");
   private static final Identifier SKY_HOUSE_LOOT = Identifier.fromNamespaceAndPath("minepiece", "chests/loot_food_village");
   private static final Identifier FRUIT_CACHE_LOOT = OmpWorld.id("chests/fruit_cache");
   private static final Identifier[] PLAINS_VILLAGE = ids(
      "minecraft",
      "village/plains/town_centers/plains_fountain_01",
      "village/plains/houses/plains_small_house_1",
      "village/plains/houses/plains_small_house_2",
      "village/plains/houses/plains_medium_house_1",
      "village/plains/houses/plains_library_1",
      "village/plains/houses/plains_weaponsmith_1",
      "village/plains/houses/plains_large_farm_1"
   );
   private static final Identifier[] DESERT_VILLAGE = ids(
      "minecraft",
      "village/desert/town_centers/desert_meeting_point_1",
      "village/desert/houses/desert_small_house_1",
      "village/desert/houses/desert_small_house_2",
      "village/desert/houses/desert_medium_house_1",
      "village/desert/houses/desert_temple_1",
      "village/desert/houses/desert_weaponsmith_1",
      "village/desert/houses/desert_large_farm_1"
   );
   private static final Identifier[] SAVANNA_VILLAGE = ids(
      "minecraft",
      "village/savanna/town_centers/savanna_meeting_point_1",
      "village/savanna/houses/savanna_small_house_1",
      "village/savanna/houses/savanna_small_house_2",
      "village/savanna/houses/savanna_medium_house_1",
      "village/savanna/houses/savanna_butchers_shop_1",
      "village/savanna/houses/savanna_weaponsmith_1"
   );
   private static final Identifier[] SNOWY_VILLAGE = ids(
      "minecraft",
      "village/snowy/town_centers/snowy_meeting_point_1",
      "village/snowy/houses/snowy_small_house_1",
      "village/snowy/houses/snowy_small_house_2",
      "village/snowy/houses/snowy_medium_house_1",
      "village/snowy/houses/snowy_library_1",
      "village/snowy/houses/snowy_weaponsmith_1"
   );
   private static final Identifier[] TAIGA_VILLAGE = ids(
      "minecraft",
      "village/taiga/town_centers/taiga_meeting_point_1",
      "village/taiga/houses/taiga_small_house_1",
      "village/taiga/houses/taiga_small_house_2",
      "village/taiga/houses/taiga_medium_house_1",
      "village/taiga/houses/taiga_armorer_house_1",
      "village/taiga/houses/taiga_weaponsmith_1"
   );
   private static final Identifier[] MARINE_BASE = ids(
      "minepiece",
      "marine/large_base/ground_floors_1",
      "marine/large_base/middle_floors_1",
      "marine/large_base/upper_floors_1",
      "marine/large_base/top_floors_1"
   );
   private static final Identifier[] MARINE_MOB_TEMPLATES = ids(
      "minepiece", "marine/mobs/grunts_10", "marine/mobs/snipers_3", "marine/mobs/captain", "marine/mobs/trader"
   );
   private static final Identifier[] MARINE_MOBS = ids(
      "minepiece",
      "marine_grunt",
      "marine_with_sword",
      "marine_with_gun",
      "marine_sniper",
      "marine_captain",
      "marine_trader",
      "marine_brute",
      "marine_vice_admiral"
   );
   private static final int[][] VILLAGE_OFFSETS = new int[][]{{0, 0}, {42, 18}, {-42, -16}, {22, -48}, {-28, 52}, {66, -38}};
   private static final int[][] REVERSE_BASES = new int[][]{{0, 2180}, {0, -2180}, {-455, 900}, {455, 900}, {-455, -900}, {455, -900}};

   private OmpStructurePlacer() {
   }

   public static void place(WorldGenLevel level, ChunkAccess chunk, long seed) {
      if (level instanceof WorldGenRegion region) {
         StructureTemplateManager templates = region.getLevel().getStructureTemplateManager();
         ChunkPos chunkPos = chunk.getPos();
         placeIslandVillages(level, templates, chunkPos, seed);
         placeIslandExplorationSites(level, chunkPos, seed);
         placeDimensionGateways(level, chunkPos, seed);
         placeSpecialIslandStructures(level, templates, chunkPos, seed);
         placeSeaExplorationSites(level, chunkPos, seed);
         placeFishmanIsland(level, chunkPos, seed);
         placeReverseMarineBases(level, templates, chunkPos, seed);
      }
   }

   private static void placeIslandVillages(WorldGenLevel level, StructureTemplateManager templates, ChunkPos chunkPos, long seed) {
      int minX = chunkPos.getMinBlockX() - 192;
      int minZ = chunkPos.getMinBlockZ() - 192;
      int maxX = chunkPos.getMaxBlockX() + 192;
      int maxZ = chunkPos.getMaxBlockZ() + 192;

      for (IslandDefinition island : OnePieceGeography.islandsNear(seed, minX, minZ, maxX, maxZ)) {
         int villages = OnePieceGeography.villageCount(island);
         if (villages > 0) {
            Identifier[] palette = villagePalette(island);

            for (int village = 0; village < villages; village++) {
               BlockPos anchor = OnePieceGeography.villageAnchor(seed, island, village);

               for (int part = 0; part < VILLAGE_OFFSETS.length; part++) {
                  int x = anchor.getX() + VILLAGE_OFFSETS[part][0];
                  int z = anchor.getZ() + VILLAGE_OFFSETS[part][1];
                  if (island.contains(x, z) && !(island.distanceRatio(x, z) > 0.78)) {
                     Identifier template = palette[Math.floorMod(part + village, palette.length)];
                     Rotation rotation = rotation(seed, island.id().hashCode(), village, part);
                     if (mightIntersectChunk(templates, chunkPos, template, rotation, x, z)) {
                        BlockPos pos = surfacePos(seed, x, z, 0);
                        if (pos.getY() > 64 && placeTemplate(level, templates, chunkPos, template, pos, rotation, seed) && contains(chunkPos, x, z)) {
                           spawnEntity(level, VILLAGER, pos.above(), seed + part * 31L + village * 997L);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void placeIslandExplorationSites(WorldGenLevel level, ChunkPos chunkPos, long seed) {
      int minX = chunkPos.getMinBlockX() - 96;
      int minZ = chunkPos.getMinBlockZ() - 96;
      int maxX = chunkPos.getMaxBlockX() + 96;
      int maxZ = chunkPos.getMaxBlockZ() + 96;

      for (IslandDefinition island : OnePieceGeography.islandsNear(seed, minX, minZ, maxX, maxZ)) {
         if (!island.starterIsland()) {
            for (int site = 0; site < OnePieceGeography.explorationSiteCount(island); site++) {
               BlockPos anchor = OnePieceGeography.explorationAnchor(seed, island, site);
               int x = anchor.getX();
               int z = anchor.getZ();
               if (island.contains(x, z) && !(island.distanceRatio(x, z) > 0.84) && intersectsBox(chunkPos, x - 18, z - 18, x + 18, z + 18)) {
                  int variant = (int)Math.floor(unit(mix(seed + island.id().hashCode(), site, 91)) * 4.0);
                  BlockPos ground = groundPos(seed, x, z);
                  if (ground.getY() > 64) {
                     placeThemedExplorationSite(level, chunkPos, island, ground, variant, seed + site * 193L);
                  }
               }
            }
         }
      }
   }

   private static void placeThemedExplorationSite(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, BlockPos ground, int variant, long seed) {
      switch (island.theme()) {
         case ALABASTA:
            if (variant <= 1) {
               buildCoastalRuin(level, chunkPos, ground, island, seed);
            } else {
               buildSupplyCave(level, chunkPos, ground, seed);
            }
            break;
         case DRUM:
            if (variant == 0) {
               buildSupplyCave(level, chunkPos, ground, seed);
            } else {
               buildCoastalRuin(level, chunkPos, ground, island, seed);
            }
            break;
         case PUNK_HAZARD:
            if (variant <= 1) {
               buildMarineOutpost(level, chunkPos, ground, seed);
            } else {
               buildSupplyCave(level, chunkPos, ground, seed);
            }
            break;
         case SABAODY:
         case SKYPIEA:
         case WANO:
         case WHOLE_CAKE:
         case ZOU:
            if (variant == 0) {
               buildPirateCamp(level, chunkPos, ground, seed);
            } else if (variant == 1) {
               buildCoastalRuin(level, chunkPos, ground, island, seed);
            } else {
               buildSupplyCave(level, chunkPos, ground, seed);
            }
            break;
         default:
            switch (variant) {
               case 0:
                  buildPirateCamp(level, chunkPos, ground, seed);
                  break;
               case 1:
                  buildMarineOutpost(level, chunkPos, ground, seed);
                  break;
               case 2:
                  buildCoastalRuin(level, chunkPos, ground, island, seed);
                  break;
               default:
                  buildSupplyCave(level, chunkPos, ground, seed);
            }
      }
   }

   private static void placeSeaExplorationSites(WorldGenLevel level, ChunkPos chunkPos, long seed) {
      int minX = chunkPos.getMinBlockX() - 96;
      int minZ = chunkPos.getMinBlockZ() - 96;
      int maxX = chunkPos.getMaxBlockX() + 96;
      int maxZ = chunkPos.getMaxBlockZ() + 96;

      for (OmpStructureDefinition structure : OmpStructureCatalog.seaStructuresNear(seed, minX, minZ, maxX, maxZ)) {
         if (intersectsBox(chunkPos, structure.centerX() - 30, structure.centerZ() - 30, structure.centerX() + 30, structure.centerZ() + 30)) {
            if (structure.kind() == OmpStructureKind.SEA_RUIN) {
               buildSeaRuin(level, chunkPos, structure, seed);
            } else {
               buildShallowWreck(level, chunkPos, structure, seed);
            }
         }
      }
   }

   private static void placeDimensionGateways(WorldGenLevel level, ChunkPos chunkPos, long seed) {
      DimensionGatewayLayout.netherIsland().ifPresent(island -> {
         if (intersectsBox(chunkPos, island.centerX() - 92, island.centerZ() - 92, island.centerX() + 92, island.centerZ() + 92)) {
            BlockPos center = groundPos(seed, island.centerX(), island.centerZ());
            if (center.getY() > 64) {
               buildNetherGatewayFortress(level, chunkPos, center, seed);
            }
         }
      });
      DimensionGatewayLayout.endIsland().ifPresent(island -> {
         if (intersectsBox(chunkPos, island.centerX() - 84, island.centerZ() - 84, island.centerX() + 84, island.centerZ() + 84)) {
            BlockPos center = groundPos(seed, island.centerX(), island.centerZ());
            if (center.getY() > 64) {
               buildEndGatewayDungeon(level, chunkPos, center, seed);
            }
         }
      });
   }

   private static void placeSpecialIslandStructures(WorldGenLevel level, StructureTemplateManager templates, ChunkPos chunkPos, long seed) {
      UniqueIslandCatalog.byId("marineford")
         .filter(island -> !island.extracted())
         .ifPresent(island -> placeMarineford(level, templates, chunkPos, island, seed));
      UniqueIslandCatalog.byId("mariejois").ifPresent(island -> placeMariejois(level, chunkPos, island, seed));
      UniqueIslandCatalog.byId("baltigo").ifPresent(island -> placeBaltigo(level, chunkPos, island, seed));
      UniqueIslandCatalog.byId("skypiea").filter(island -> !island.extracted()).ifPresent(island -> placeSkypiea(level, chunkPos, island, seed));
   }

   private static void placeMarineford(WorldGenLevel level, StructureTemplateManager templates, ChunkPos chunkPos, IslandDefinition island, long seed) {
      if (intersectsBox(
         chunkPos,
         island.centerX() - island.radius() - 120,
         island.centerZ() - island.radius() - 120,
         island.centerX() + island.radius() + 120,
         island.centerZ() + island.radius() + 120
      )) {
         int[][] bases = new int[][]{{0, 0}, {-140, -35}, {140, -35}, {-90, 135}, {90, 135}, {-230, 70}, {230, 70}, {-180, -185}, {180, -185}};

         for (int i = 0; i < bases.length; i++) {
            int x = island.centerX() + bases[i][0];
            int z = island.centerZ() + bases[i][1];
            if (intersectsBox(chunkPos, x - 88, z - 88, x + 88, z + 88) && island.contains(x, z)) {
               BlockPos ground = groundPos(seed, x, z);
               if (ground.getY() > 64) {
                  buildMarinefordParadePad(level, chunkPos, ground, i);
                  Rotation rotation = rotation(seed, island.id().hashCode(), i, 0);

                  for (int floor = 0; floor < MARINE_BASE.length; floor++) {
                     placeTemplate(level, templates, chunkPos, MARINE_BASE[floor], ground.above(1 + floor * 8), rotation, seed + i * 977L + floor);
                  }

                  BlockPos tower = ground.offset(i % 2 == 0 ? 32 : -32, 1, i % 3 == 0 ? 32 : -32);
                  buildMarineWatchPost(level, chunkPos, tower);
                  if (contains(chunkPos, x, z)) {
                     spawnEntity(level, MARINE_MOBS[Math.floorMod(i, MARINE_MOBS.length)], ground.above(2), seed + i * 31L);
                     spawnEntity(level, MARINE_MOBS[Math.floorMod(i + 3, MARINE_MOBS.length)], ground.offset(6, 2, -5), seed + i * 37L);
                  }
               }
            }
         }

         BlockPos harbor = new BlockPos(island.centerX(), 64, island.centerZ() - 360);
         if (intersectsBox(chunkPos, harbor.getX() - 160, harbor.getZ() - 60, harbor.getX() + 160, harbor.getZ() + 60)) {
            buildMarinefordHarbor(level, chunkPos, harbor);
         }
      }
   }

   private static void placeMariejois(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, long seed) {
      if (intersectsBox(chunkPos, island.centerX() - 360, island.centerZ() - 980, island.centerX() + 330, island.centerZ() + 980)) {
         int[][] palaces = new int[][]{
            {0, 0, 44, 30, 26},
            {-155, -210, 34, 24, 21},
            {-155, 210, 34, 24, 21},
            {145, -190, 32, 23, 20},
            {145, 190, 32, 23, 20},
            {170, 0, 34, 24, 22},
            {-220, 0, 30, 22, 18},
            {10, -520, 32, 23, 20},
            {10, 520, 32, 23, 20},
            {-190, -650, 28, 20, 18},
            {-190, 650, 28, 20, 18}
         };

         for (int i = 0; i < palaces.length; i++) {
            int x = island.centerX() + palaces[i][0];
            int z = island.centerZ() + palaces[i][1];
            int halfX = palaces[i][2] + 22;
            int halfZ = palaces[i][3] + 22;
            if (intersectsBox(chunkPos, x - halfX, z - halfZ, x + halfX, z + halfZ)) {
               BlockPos ground = groundPos(seed, x, z);
               if (ground.getY() > 143) {
                  buildMariejoisPalace(level, chunkPos, ground.above(), palaces[i][2], palaces[i][3], palaces[i][4], i);
                  buildMariejoisGarden(level, chunkPos, ground.above(), palaces[i][2] + 14, palaces[i][3] + 12, i);
                  if (contains(chunkPos, x, z)) {
                     spawnEntity(level, VILLAGER, ground.offset(3, 2, 3), seed + i * 71L);
                     spawnEntity(level, VILLAGER, ground.offset(-3, 2, -3), seed + i * 73L);
                  }
               }
            }
         }

         int[][] villas = new int[][]{
            {-285, -430, 16, 14, 12},
            {-285, -250, 15, 13, 11},
            {-285, 250, 15, 13, 11},
            {-285, 430, 16, 14, 12},
            {-90, -780, 18, 14, 12},
            {90, -780, 18, 14, 12},
            {-90, 780, 18, 14, 12},
            {90, 780, 18, 14, 12},
            {235, -480, 16, 13, 12},
            {235, -310, 15, 12, 11},
            {235, 310, 15, 12, 11},
            {235, 480, 16, 13, 12},
            {-20, -330, 15, 12, 11},
            {-20, 330, 15, 12, 11},
            {-245, -70, 14, 12, 11},
            {-245, 70, 14, 12, 11}
         };

         for (int i = 0; i < villas.length; i++) {
            int x = island.centerX() + villas[i][0];
            int z = island.centerZ() + villas[i][1];
            if (intersectsBox(chunkPos, x - villas[i][2] - 10, z - villas[i][3] - 10, x + villas[i][2] + 10, z + villas[i][3] + 10)) {
               BlockPos ground = groundPos(seed, x, z);
               if (ground.getY() > 143) {
                  buildMariejoisVilla(level, chunkPos, ground.above(), villas[i][2], villas[i][3], villas[i][4], i);
               }
            }
         }

         buildMariejoisGrandPlaza(level, chunkPos, island, seed);
         buildMariejoisAvenue(level, chunkPos, island, seed);
      }
   }

   private static void placeBaltigo(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, long seed) {
      int[] layers = baltigoLayers();

      for (int layer = 0; layer < layers.length; layer++) {
         if (baltigoLayerIntersects(chunkPos, island, layer)) {
            buildBaltigoLayer(level, chunkPos, island, layers[layer], layer, seed);
         }
      }

      if (intersectsBox(chunkPos, island.centerX() - 10, island.centerZ() - 10, island.centerX() + 10, island.centerZ() + 10)) {
         buildBaltigoCentralShaft(level, chunkPos, island, layers[0], layers[layers.length - 1] + 18);
      }

      for (int[] connector : baltigoConnectorOffsets(island)) {
         int x = island.centerX() + connector[0];
         int z = island.centerZ() + connector[1];
         if (intersectsBox(chunkPos, x - 6, z - 6, x + 6, z + 6)) {
            buildBaltigoVerticalConnector(level, chunkPos, new BlockPos(x, layers[0] - 2, z), layers[layers.length - 1] + 18);
         }
      }
   }

   private static int[] baltigoLayers() {
      return new int[]{97, 123, 149, 175};
   }

   private static boolean baltigoLayerIntersects(ChunkPos chunkPos, IslandDefinition island, int layer) {
      int centerX = island.centerX();
      int centerZ = island.centerZ();
      int bridgeLength = baltigoBridgeLength(island);
      int ring = baltigoRingRadius(island);
      if (!intersectsBox(chunkPos, centerX - bridgeLength, centerZ - 4, centerX + bridgeLength, centerZ + 4)
         && !intersectsBox(chunkPos, centerX - 4, centerZ - bridgeLength, centerX + 4, centerZ + bridgeLength)
         && !intersectsBox(chunkPos, centerX - ring, centerZ - ring - 4, centerX + ring, centerZ - ring + 4)
         && !intersectsBox(chunkPos, centerX - ring, centerZ + ring - 4, centerX + ring, centerZ + ring + 4)
         && !intersectsBox(chunkPos, centerX - ring - 4, centerZ - ring, centerX - ring + 4, centerZ + ring)
         && !intersectsBox(chunkPos, centerX + ring - 4, centerZ - ring, centerX + ring + 4, centerZ + ring)) {
         for (int[] room : baltigoRooms(layer)) {
            int x = centerX + room[0];
            int z = centerZ + room[1];
            if (intersectsBox(chunkPos, x - room[2] - 2, z - room[3] - 2, x + room[2] + 2, z + room[3] + 2)) {
               return true;
            }
         }

         for (int[] lamp : new int[][]{{-150, 0}, {150, 0}, {0, -150}, {0, 150}, {-92, -92}, {92, 92}}) {
            if (contains(chunkPos, centerX + lamp[0], centerZ + lamp[1])) {
               return true;
            }
         }

         int chestX = centerX + (layer % 2 == 0 ? 18 : -18);
         int chestZ = centerZ + (layer % 2 == 0 ? -18 : 18);
         return contains(chunkPos, chestX, chestZ);
      } else {
         return true;
      }
   }

   private static int baltigoBridgeLength(IslandDefinition island) {
      return Math.min(island.radius() - 52, Math.max(360, (int)Math.round(island.radius() * 0.9)));
   }

   private static int baltigoRingRadius(IslandDefinition island) {
      return (int)Math.round(island.radius() * 0.31);
   }

   private static int[][] baltigoConnectorOffsets(IslandDefinition island) {
      int offset = (int)Math.round(island.radius() * 0.13);
      return new int[][]{{offset, offset}, {-offset, offset}, {offset, -offset}, {-offset, -offset}};
   }

   private static void placeSkypiea(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, long seed) {
      if (intersectsBox(
         chunkPos,
         island.centerX() - island.radius() - 80,
         island.centerZ() - island.radius() - 80,
         island.centerX() + island.radius() + 80,
         island.centerZ() + island.radius() + 80
      )) {
         int poolX = SkypieaLayout.landingPoolX(island);
         int poolZ = SkypieaLayout.landingPoolZ(island);
         if (intersectsBox(chunkPos, island.centerX() - 48 - 12, island.centerZ() - 48 - 12, poolX + 18 + 14, island.centerZ() + 48 + 12)) {
            buildSkypieaLandingPool(level, chunkPos, island, seed);
         }

         int[][] temples = new int[][]{{128, 86, 24, 18}, {0, 220, 18, 14}};

         for (int i = 0; i < temples.length; i++) {
            int x = island.centerX() + temples[i][0];
            int z = island.centerZ() + temples[i][1];
            if (island.contains(x, z) && intersectsBox(chunkPos, x - temples[i][2] - 8, z - temples[i][3] - 8, x + temples[i][2] + 8, z + temples[i][3] + 8)) {
               BlockPos ground = groundPos(seed, x, z).above();
               buildSkypieaTemple(level, chunkPos, ground, temples[i][2], temples[i][3], seed + i * 281L);
            }
         }

         int[][] houses = new int[][]{{-150, 82}, {-118, -126}, {128, -155}, {226, -42}, {-238, -18}, {46, 312}};

         for (int i = 0; i < houses.length; i++) {
            int x = island.centerX() + houses[i][0];
            int z = island.centerZ() + houses[i][1];
            if (island.contains(x, z) && !SkypieaLayout.isStreamHole(island, x, z) && intersectsBox(chunkPos, x - 10, z - 10, x + 10, z + 10)) {
               BlockPos ground = groundPos(seed, x, z).above();
               buildSkypieaCloudHouse(level, chunkPos, ground, seed + i * 337L);
               if (contains(chunkPos, x, z)) {
                  spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "skypiean_civilian"), ground.above(), seed + i * 541L);
                  spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "skypiean_trader"), ground.offset(2, 1, 1), seed + i * 547L);
               }
            }
         }
      }
   }

   private static void placeReverseMarineBases(WorldGenLevel level, StructureTemplateManager templates, ChunkPos chunkPos, long seed) {
      if (intersectsBox(chunkPos, -3300, -3300, 3300, 3300)) {
         for (int base = 0; base < REVERSE_BASES.length; base++) {
            int x = REVERSE_BASES[base][0];
            int z = REVERSE_BASES[base][1];
            if (!OnePieceGeography.isReverseMountainChannel(x, z)) {
               Rotation rotation = rotation(seed, 1296126537, base, 0);
               int baseY = Math.min(276, OnePieceGeography.terrainHeight(seed, x, z) + 1);

               for (int floor = 0; floor < MARINE_BASE.length; floor++) {
                  BlockPos pos = new BlockPos(x, baseY + floor * 8, z);
                  placeTemplate(level, templates, chunkPos, MARINE_BASE[floor], pos, rotation, seed + base * 97L + floor);
               }

               int towerX = x + (x == 0 ? 90 : (x > 0 ? 90 : -90));
               int towerZ = z + (z == 0 ? 90 : (z > 0 ? 90 : -90));
               placeTemplate(
                  level,
                  templates,
                  chunkPos,
                  Identifier.fromNamespaceAndPath("minepiece", "marine/watch_tower"),
                  new BlockPos(towerX, Math.min(286, OnePieceGeography.terrainHeight(seed, towerX, towerZ) + 1), towerZ),
                  rotation,
                  seed + base * 193L
               );

               for (int mobs = 0; mobs < MARINE_MOB_TEMPLATES.length; mobs++) {
                  int mobX = x + (mobs - 1) * 14;
                  int mobZ = z + (mobs % 2 == 0 ? 16 : -16);
                  if (contains(chunkPos, mobX, mobZ)) {
                     BlockPos pos = new BlockPos(mobX, Math.min(286, OnePieceGeography.terrainHeight(seed, mobX, mobZ) + 2), mobZ);
                     placeTemplate(level, templates, chunkPos, MARINE_MOB_TEMPLATES[mobs], pos, rotation, seed + base * 251L + mobs);
                     spawnEntity(level, MARINE_MOBS[Math.floorMod(base + mobs, MARINE_MOBS.length)], pos, seed + base * 409L + mobs);
                  }
               }
            }
         }
      }
   }

   private static void placeFishmanIsland(WorldGenLevel level, ChunkPos chunkPos, long seed) {
      if (FishmanIslandLayout.domeIntersectsChunk(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), chunkPos.getMaxBlockZ())) {
         int centerX = FishmanIslandLayout.domeCenterX();
         int centerZ = FishmanIslandLayout.domeCenterZ();
         BlockPos plaza = new BlockPos(centerX, FishmanIslandLayout.fishmanFloorHeight(seed, centerX, centerZ) + 1, centerZ);
         buildFishmanPlaza(level, chunkPos, plaza, seed);
         int index = 0;

         for (BlockPos anchor : FishmanIslandLayout.villageAnchors(seed)) {
            buildFishmanHouse(level, chunkPos, anchor, seed + index * 251L);
            if (contains(chunkPos, anchor.getX(), anchor.getZ())) {
               spawnEntity(level, VILLAGER, anchor.above(), seed + index * 503L);
               spawnEntity(level, VILLAGER, anchor.offset(2, 1, 1), seed + index * 509L);
            }

            index++;
         }

         BlockPos stream = new BlockPos(
            FishmanIslandLayout.hotStreamX(),
            FishmanIslandLayout.fishmanFloorHeight(seed, FishmanIslandLayout.hotStreamX(), FishmanIslandLayout.hotStreamZ()) + 1,
            FishmanIslandLayout.hotStreamZ()
         );
         buildHotStreamPlatform(level, chunkPos, stream);
      }
   }

   private static void buildFishmanPlaza(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      for (int dx = -18; dx <= 18; dx++) {
         for (int dz = -18; dz <= 18; dz++) {
            int dist = dx * dx + dz * dz;
            if (dist <= 324) {
               BlockPos pos = center.offset(dx, 0, dz);
               BlockState top = dist > 225 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState();
               placeSupported(level, chunkPos, pos, top, Blocks.PRISMARINE.defaultBlockState());
               if (Math.abs(dx) % 8 == 0 && Math.abs(dz) % 8 == 0 && dist > 55 && dist < 260) {
                  setBlock(level, chunkPos, pos.above(), Blocks.SEA_LANTERN.defaultBlockState());
               }
            }
         }
      }

      clearAbove(level, chunkPos, center, 17, 7);
      placeLootChest(level, chunkPos, center.offset(0, 1, 4), FRUIT_CACHE_LOOT, seed + 71L);

      for (int[] offset : new int[][]{{-14, 0}, {14, 0}, {0, -14}, {0, 14}}) {
         buildGlowstoneLamp(level, chunkPos, center.offset(offset[0], 1, offset[1]));
      }
   }

   private static void buildFishmanHouse(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      int halfX = 6;
      int halfZ = 5;

      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            BlockPos floor = center.offset(dx, 0, dz);
            placeSupported(level, chunkPos, floor, Blocks.PRISMARINE_BRICKS.defaultBlockState(), Blocks.PRISMARINE.defaultBlockState());
            boolean wall = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;

            for (int y = 1; y <= 4; y++) {
               BlockPos pos = floor.above(y);
               if (wall && (dz != -halfZ || Math.abs(dx) > 1 || y > 2)) {
                  if (y == 2 && Math.abs(dx) != halfX && Math.abs(dz) == halfZ) {
                     setBlock(level, chunkPos, pos, Blocks.GLASS.defaultBlockState());
                  } else {
                     setBlock(level, chunkPos, pos, Blocks.PRISMARINE.defaultBlockState());
                  }
               } else {
                  setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
               }
            }

            if (Math.abs(dx) <= halfX + 1 && Math.abs(dz) <= halfZ + 1) {
               setBlock(level, chunkPos, center.offset(dx, 5, dz), Blocks.DARK_PRISMARINE.defaultBlockState());
            }
         }
      }

      placeLootChest(level, chunkPos, center.offset(3, 1, 2), PIRATE_SUPPLIES_LOOT, seed + 17L);
      buildGlowstoneLamp(level, chunkPos, center.offset(-3, 1, -2));
   }

   private static void buildHotStreamPlatform(WorldGenLevel level, ChunkPos chunkPos, BlockPos center) {
      for (int dx = -10; dx <= 10; dx++) {
         for (int dz = -10; dz <= 10; dz++) {
            int dist = dx * dx + dz * dz;
            if (dist <= 100 && dist >= 49) {
               placeSupported(
                  level,
                  chunkPos,
                  center.offset(dx, 0, dz),
                  variant(dx, dz, 5) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.DARK_PRISMARINE.defaultBlockState(),
                  Blocks.PRISMARINE.defaultBlockState()
               );
            }
         }
      }

      clearAbove(level, chunkPos, center, 11, 5);
   }

   private static void buildMarinefordParadePad(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int index) {
      for (int dx = -34; dx <= 34; dx++) {
         for (int dz = -30; dz <= 30; dz++) {
            boolean stripe = Math.floorMod(dx + index * 3, 9) == 0 || Math.floorMod(dz, 11) == 0;
            BlockState top = stripe ? Blocks.DIORITE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
            if (Math.abs(dx) == 34 || Math.abs(dz) == 30) {
               top = Blocks.STONE_BRICKS.defaultBlockState();
            }

            placeSupported(level, chunkPos, center.offset(dx, 0, dz), top, Blocks.STONE.defaultBlockState());
         }
      }

      clearAbove(level, chunkPos, center, 36, 16);

      for (int dx = -34; dx <= 34; dx += 8) {
         setBlock(level, chunkPos, center.offset(dx, 1, -30), Blocks.IRON_BARS.defaultBlockState());
         setBlock(level, chunkPos, center.offset(dx, 1, 30), Blocks.IRON_BARS.defaultBlockState());
      }

      for (int dz = -30; dz <= 30; dz += 8) {
         setBlock(level, chunkPos, center.offset(-34, 1, dz), Blocks.IRON_BARS.defaultBlockState());
         setBlock(level, chunkPos, center.offset(34, 1, dz), Blocks.IRON_BARS.defaultBlockState());
      }
   }

   private static void buildMarineWatchPost(WorldGenLevel level, ChunkPos chunkPos, BlockPos base) {
      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -4; dz <= 4; dz++) {
            for (int y = 0; y <= 11; y++) {
               boolean wall = Math.abs(dx) == 4 || Math.abs(dz) == 4;
               BlockPos pos = base.offset(dx, y, dz);
               if (!wall && y > 0 && y < 10) {
                  setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
               } else if (y == 10 || y == 11) {
                  setBlock(level, chunkPos, pos, Blocks.STONE_BRICKS.defaultBlockState());
               } else if (wall) {
                  setBlock(level, chunkPos, pos, y % 4 == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
               }
            }
         }
      }

      setBlock(level, chunkPos, base.above(12), Blocks.SEA_LANTERN.defaultBlockState());
   }

   private static void buildMarinefordHarbor(WorldGenLevel level, ChunkPos chunkPos, BlockPos center) {
      int minDx = Math.max(-150, chunkPos.getMinBlockX() - center.getX());
      int maxDx = Math.min(150, chunkPos.getMaxBlockX() - center.getX());
      int minDz = Math.max(-54, chunkPos.getMinBlockZ() - center.getZ());
      int maxDz = Math.min(54, chunkPos.getMaxBlockZ() - center.getZ());

      for (int dx = minDx; dx <= maxDx; dx++) {
         for (int dz = Math.max(-12, minDz); dz <= Math.min(12, maxDz); dz++) {
            if (Math.abs(dx) % 45 < 30 || Math.abs(dz) > 8) {
               setBlock(level, chunkPos, center.offset(dx, 0, dz), Blocks.SMOOTH_STONE.defaultBlockState());
               setBlock(level, chunkPos, center.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
            }
         }
      }

      for (int dx = -130; dx <= 130; dx += 65) {
         if (center.getX() + dx >= chunkPos.getMinBlockX() && center.getX() + dx <= chunkPos.getMaxBlockX()) {
            for (int dz = minDz; dz <= maxDz; dz++) {
               setBlock(level, chunkPos, center.offset(dx, 0, dz), Blocks.OAK_PLANKS.defaultBlockState());
               if (dz % 8 == 0) {
                  setBlock(level, chunkPos, center.offset(dx, 1, dz), Blocks.OAK_FENCE.defaultBlockState());
               }
            }
         }
      }
   }

   private static void buildMariejoisPalace(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int halfX, int halfZ, int height, int index) {
      for (int dx = -halfX - 8; dx <= halfX + 8; dx++) {
         for (int dz = -halfZ - 8; dz <= halfZ + 8; dz++) {
            boolean border = Math.abs(dx) == halfX + 8 || Math.abs(dz) == halfZ + 8;
            BlockState top = border ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState();
            placeSupported(level, chunkPos, center.offset(dx, 0, dz), top, Blocks.CALCITE.defaultBlockState());
         }
      }

      clearBox(level, chunkPos, center.offset(-halfX - 4, 1, -halfZ - 4), center.offset(halfX + 4, height + 9, halfZ + 4));

      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            boolean wall = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;

            for (int y = 1; y <= height; y++) {
               BlockPos pos = center.offset(dx, y, dz);
               if (!wall) {
                  if (y != 1 && y != height) {
                     setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
                  } else {
                     setBlock(level, chunkPos, pos, Blocks.SMOOTH_QUARTZ.defaultBlockState());
                  }
               } else if (y % 5 == 2 && Math.abs(dx) < halfX - 3 && Math.abs(dz) == halfZ) {
                  setBlock(level, chunkPos, pos, Blocks.STAINED_GLASS.white().defaultBlockState());
               } else if (y % 6 == 0) {
                  setBlock(level, chunkPos, pos, Blocks.GOLD_BLOCK.defaultBlockState());
               } else {
                  setBlock(level, chunkPos, pos, Blocks.QUARTZ_BLOCK.defaultBlockState());
               }
            }
         }
      }

      for (int dx = -halfX; dx <= halfX; dx += Math.max(8, halfX / 2)) {
         buildMariejoisColumn(level, chunkPos, center.offset(dx, 1, -halfZ - 3), height + 2);
         buildMariejoisColumn(level, chunkPos, center.offset(dx, 1, halfZ + 3), height + 2);
      }

      for (int dz = -halfZ; dz <= halfZ; dz += Math.max(8, halfZ / 2)) {
         buildMariejoisColumn(level, chunkPos, center.offset(-halfX - 3, 1, dz), height + 2);
         buildMariejoisColumn(level, chunkPos, center.offset(halfX + 3, 1, dz), height + 2);
      }

      buildMariejoisRoof(level, chunkPos, center.above(height + 1), halfX + 5, halfZ + 5, index);
      placeLootChest(level, chunkPos, center.offset(0, 2, 0), FRUIT_CACHE_LOOT, 5566821089055231817L + index);
   }

   private static void buildMariejoisColumn(WorldGenLevel level, ChunkPos chunkPos, BlockPos base, int height) {
      for (int y = 0; y <= height; y++) {
         setBlock(level, chunkPos, base.above(y), y % 5 == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.QUARTZ_PILLAR.defaultBlockState());
      }
   }

   private static void buildMariejoisRoof(WorldGenLevel level, ChunkPos chunkPos, BlockPos base, int halfX, int halfZ, int index) {
      for (int layer = 0; layer <= 5; layer++) {
         int lx = halfX - layer * 3;
         int lz = halfZ - layer * 3;

         for (int dx = -lx; dx <= lx; dx++) {
            for (int dz = -lz; dz <= lz; dz++) {
               if (Math.abs(dx) == lx || Math.abs(dz) == lz || layer >= 5) {
                  BlockState state = layer != 5 && variant(dx + index, dz, 7) != 0 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
                  setBlock(level, chunkPos, base.offset(dx, layer, dz), state);
               }
            }
         }
      }

      setBlock(level, chunkPos, base.above(7), Blocks.SEA_LANTERN.defaultBlockState());
   }

   private static void buildMariejoisGarden(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int halfX, int halfZ, int index) {
      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            if (Math.abs(dx) > halfX - 5 || Math.abs(dz) > halfZ - 5) {
               BlockState state = variant(dx + index, dz, 9) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.QUARTZ_BLOCK.defaultBlockState();
               setBlock(level, chunkPos, center.offset(dx, 1, dz), state);
            }
         }
      }

      for (int dx = -halfX + 8; dx <= halfX - 8; dx += 16) {
         for (int dz = -halfZ + 8; dz <= halfZ - 8; dz += 16) {
            setBlock(level, chunkPos, center.offset(dx, 1, dz), Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState());
            setBlock(level, chunkPos, center.offset(dx, 2, dz), Blocks.GLOWSTONE.defaultBlockState());
         }
      }
   }

   private static void buildMariejoisVilla(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int halfX, int halfZ, int height, int index) {
      for (int dx = -halfX - 5; dx <= halfX + 5; dx++) {
         for (int dz = -halfZ - 5; dz <= halfZ + 5; dz++) {
            boolean border = Math.abs(dx) == halfX + 5 || Math.abs(dz) == halfZ + 5;
            BlockState top = !border && variant(dx + index, dz, 11) != 0 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
            placeSupported(level, chunkPos, center.offset(dx, 0, dz), top, Blocks.CALCITE.defaultBlockState());
         }
      }

      clearBox(level, chunkPos, center.offset(-halfX - 1, 1, -halfZ - 1), center.offset(halfX + 1, height + 5, halfZ + 1));

      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            boolean wall = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;

            for (int y = 1; y <= height; y++) {
               BlockPos pos = center.offset(dx, y, dz);
               if (!wall) {
                  setBlock(level, chunkPos, pos, y != 1 && y != height ? Blocks.AIR.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState());
               } else if (y % 4 == 2 && Math.abs(dx) < halfX - 2 && Math.abs(dz) == halfZ) {
                  setBlock(level, chunkPos, pos, Blocks.STAINED_GLASS.white().defaultBlockState());
               } else {
                  setBlock(level, chunkPos, pos, y % 5 == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.QUARTZ_BLOCK.defaultBlockState());
               }
            }
         }
      }

      buildMariejoisRoof(level, chunkPos, center.above(height + 1), halfX + 3, halfZ + 3, index + 31);
      setBlock(level, chunkPos, center.offset(0, height + 8, 0), Blocks.SEA_LANTERN.defaultBlockState());
   }

   private static void buildMariejoisGrandPlaza(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, long seed) {
      int centerX = island.centerX();
      int centerZ = island.centerZ();
      if (intersectsBox(chunkPos, centerX - 112, centerZ - 112, centerX + 112, centerZ + 112)) {
         int minDx = Math.max(-104, chunkPos.getMinBlockX() - centerX);
         int maxDx = Math.min(104, chunkPos.getMaxBlockX() - centerX);
         int minDz = Math.max(-104, chunkPos.getMinBlockZ() - centerZ);
         int maxDz = Math.min(104, chunkPos.getMaxBlockZ() - centerZ);

         for (int dx = minDx; dx <= maxDx; dx++) {
            for (int dz = minDz; dz <= maxDz; dz++) {
               double oval = dx * dx / 10816.0 + dz * dz / 5776.0;
               if (!(oval > 1.0)) {
                  int x = centerX + dx;
                  int z = centerZ + dz;
                  int y = OnePieceGeography.terrainHeight(seed, x, z) + 1;
                  boolean ring = oval > 0.86 || Math.abs(dx) <= 3 || Math.abs(dz) <= 3;
                  BlockState state = !ring && variant(dx, dz, 13) != 0 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
                  placeSupported(level, chunkPos, new BlockPos(x, y, z), state, Blocks.CALCITE.defaultBlockState());
                  if (Math.abs(dx) == 58 && Math.abs(dz) <= 4 || Math.abs(dz) == 42 && Math.abs(dx) <= 4) {
                     setBlock(level, chunkPos, new BlockPos(x, y + 1, z), Blocks.SEA_LANTERN.defaultBlockState());
                  }

                  if (Math.abs(dx) <= 9 && Math.abs(dz) <= 9 && Math.abs(dx) + Math.abs(dz) > 8) {
                     setBlock(level, chunkPos, new BlockPos(x, y + 1, z), Blocks.WATER.defaultBlockState());
                  }
               }
            }
         }

         BlockPos center = new BlockPos(centerX, OnePieceGeography.terrainHeight(seed, centerX, centerZ) + 2, centerZ);

         for (int y = 0; y <= 18; y++) {
            setBlock(level, chunkPos, center.above(y), y % 5 == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.QUARTZ_PILLAR.defaultBlockState());
         }

         setBlock(level, chunkPos, center.above(20), Blocks.GLOWSTONE.defaultBlockState());
      }
   }

   private static void buildMariejoisAvenue(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, long seed) {
      if (intersectsBox(chunkPos, island.centerX() - 32, island.centerZ() - 660, island.centerX() + 32, island.centerZ() + 660)) {
         int minZ = Math.max(island.centerZ() - 650, chunkPos.getMinBlockZ());
         int maxZ = Math.min(island.centerZ() + 650, chunkPos.getMaxBlockZ());

         for (int z = minZ; z <= maxZ; z++) {
            int y = OnePieceGeography.terrainHeight(seed, island.centerX(), z) + 1;
            BlockPos center = new BlockPos(island.centerX(), y, z);

            for (int dx = -24; dx <= 24; dx++) {
               BlockState state = Math.abs(dx) != 24 && z % 32 != 0 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
               placeSupported(level, chunkPos, center.offset(dx, 0, 0), state, Blocks.CALCITE.defaultBlockState());
            }

            if (z % 24 == 0) {
               buildGlowstoneLamp(level, chunkPos, center.offset(-30, 1, 0));
               buildGlowstoneLamp(level, chunkPos, center.offset(30, 1, 0));
            }
         }
      }
   }

   private static void buildBaltigoLayer(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, int floorY, int layer, long seed) {
      BlockPos center = new BlockPos(island.centerX(), floorY, island.centerZ());
      int bridgeLength = baltigoBridgeLength(island);
      int minXOffset = Math.max(-bridgeLength, chunkPos.getMinBlockX() - center.getX());
      int maxXOffset = Math.min(bridgeLength, chunkPos.getMaxBlockX() - center.getX());

      for (int offset = minXOffset; offset <= maxXOffset; offset++) {
         for (int width = -3; width <= 3; width++) {
            BlockState bridge = baltigoBridgeState(offset, width, layer);
            setBlock(level, chunkPos, center.offset(offset, 0, width), bridge);
         }
      }

      int minZOffset = Math.max(-bridgeLength, chunkPos.getMinBlockZ() - center.getZ());
      int maxZOffset = Math.min(bridgeLength, chunkPos.getMaxBlockZ() - center.getZ());

      for (int offset = minZOffset; offset <= maxZOffset; offset++) {
         for (int width = -3; width <= 3; width++) {
            BlockState bridge = baltigoBridgeState(offset, width, layer);
            setBlock(level, chunkPos, center.offset(width, 0, offset), bridge);
         }
      }

      buildBaltigoRing(level, chunkPos, center, island, layer);

      for (int[] gate : new int[][]{{bridgeLength, 0}, {-bridgeLength, 0}, {0, bridgeLength}, {0, -bridgeLength}}) {
         if (intersectsBox(
            chunkPos, center.getX() + gate[0] - 9, center.getZ() + gate[1] - 9, center.getX() + gate[0] + 9, center.getZ() + gate[1] + 9
         )) {
            buildBaltigoExteriorGate(level, chunkPos, center.offset(gate[0], 0, gate[1]), gate[0], gate[1], layer);
         }
      }

      for (int[] room : baltigoRooms(layer)) {
         BlockPos roomCenter = center.offset(room[0], 0, room[1]);
         if (intersectsBox(
            chunkPos,
            roomCenter.getX() - room[2] - 2,
            roomCenter.getZ() - room[3] - 2,
            roomCenter.getX() + room[2] + 2,
            roomCenter.getZ() + room[3] + 2
         )) {
            buildBaltigoRoom(level, chunkPos, roomCenter, room[2], room[3], layer);
            if (contains(chunkPos, roomCenter.getX(), roomCenter.getZ())) {
               spawnEntity(level, VILLAGER, roomCenter.offset(2, 1, 1), seed + layer * 101L + room[0]);
            }
         }
      }

      for (int[] lamp : new int[][]{{-150, 0}, {150, 0}, {0, -150}, {0, 150}, {-92, -92}, {92, 92}}) {
         if (contains(chunkPos, center.getX() + lamp[0], center.getZ() + lamp[1])) {
            buildGlowstoneLamp(level, chunkPos, center.offset(lamp[0], 1, lamp[1]));
         }
      }

      BlockPos chest = center.offset(layer % 2 == 0 ? 18 : -18, 1, layer % 2 == 0 ? -18 : 18);
      if (contains(chunkPos, chest.getX(), chest.getZ())) {
         placeLootChest(level, chunkPos, chest, FRUIT_CACHE_LOOT, seed + layer * 541L);
      }
   }

   private static BlockState baltigoBridgeState(int offset, int width, int layer) {
      return Math.abs(width) == 3
         ? Blocks.STONE_BRICK_WALL.defaultBlockState()
         : (variant(offset, width + layer, 7) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.DIORITE.defaultBlockState());
   }

   private static void buildBaltigoRing(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, IslandDefinition island, int layer) {
      int ring = baltigoRingRadius(island);
      int minXOffset = Math.max(-ring, chunkPos.getMinBlockX() - center.getX());
      int maxXOffset = Math.min(ring, chunkPos.getMaxBlockX() - center.getX());

      for (int offset = minXOffset; offset <= maxXOffset; offset++) {
         for (int width = -3; width <= 3; width++) {
            setBlock(level, chunkPos, center.offset(offset, 0, ring + width), baltigoBridgeState(offset, width, layer));
            setBlock(level, chunkPos, center.offset(offset, 0, -ring + width), baltigoBridgeState(offset, width, layer));
         }
      }

      int minZOffset = Math.max(-ring, chunkPos.getMinBlockZ() - center.getZ());
      int maxZOffset = Math.min(ring, chunkPos.getMaxBlockZ() - center.getZ());

      for (int offset = minZOffset; offset <= maxZOffset; offset++) {
         for (int width = -3; width <= 3; width++) {
            setBlock(level, chunkPos, center.offset(ring + width, 0, offset), baltigoBridgeState(offset, width, layer));
            setBlock(level, chunkPos, center.offset(-ring + width, 0, offset), baltigoBridgeState(offset, width, layer));
         }
      }
   }

   private static void buildBaltigoExteriorGate(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int directionX, int directionZ, int layer) {
      int halfX = directionX == 0 ? 7 : 3;
      int halfZ = directionZ == 0 ? 7 : 3;

      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            BlockPos floor = center.offset(dx, 0, dz);
            boolean edge = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;
            setBlock(level, chunkPos, floor, edge ? Blocks.STONE_BRICKS.defaultBlockState() : baltigoBridgeState(dx + directionX + layer, dz + directionZ, layer));

            for (int y = 1; y <= 5; y++) {
               boolean doorway = directionX != 0 ? Math.abs(dz) <= 2 : Math.abs(dx) <= 2;
               if (doorway) {
                  setBlock(level, chunkPos, floor.above(y), Blocks.AIR.defaultBlockState());
               } else if (edge && y <= 4) {
                  setBlock(level, chunkPos, floor.above(y), baltigoWallState(dx, dz, y, layer));
               }
            }
         }
      }

      if (directionX != 0) {
         buildGlowstoneLamp(level, chunkPos, center.offset(0, 1, -halfZ));
         buildGlowstoneLamp(level, chunkPos, center.offset(0, 1, halfZ));
      } else {
         buildGlowstoneLamp(level, chunkPos, center.offset(-halfX, 1, 0));
         buildGlowstoneLamp(level, chunkPos, center.offset(halfX, 1, 0));
      }
   }

   private static int[][] baltigoRooms(int layer) {
      int shift = layer % 2 == 0 ? 1 : -1;
      return new int[][]{
         {shift * 86, 64, 18, 13},
         {-shift * 118, -76, 16, 12},
         {shift * 172, -30, 14, 10},
         {-shift * 58, 150, 15, 11},
         {shift * 228, 98, 13, 10},
         {-shift * 214, 118, 12, 10}
      };
   }

   private static void buildBaltigoRoom(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int halfX, int halfZ, int layer) {
      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            boolean wall = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;
            BlockState floor = variant(center.getX() + dx, center.getZ() + dz, 11) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
            setBlock(level, chunkPos, center.offset(dx, 0, dz), floor);

            for (int y = 1; y <= 5; y++) {
               BlockPos pos = center.offset(dx, y, dz);
               if (!wall) {
                  setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
               } else if (y <= 4) {
                  boolean doorway = Math.abs(dx) <= 2 && dz == -halfZ && y <= 3;
                  setBlock(level, chunkPos, pos, doorway ? Blocks.AIR.defaultBlockState() : baltigoWallState(dx, dz, y, layer));
               }
            }

            if (wall || Math.abs(dx) == halfX - 1 || Math.abs(dz) == halfZ - 1) {
               setBlock(level, chunkPos, center.offset(dx, 6, dz), Blocks.DIORITE.defaultBlockState());
            }
         }
      }

      buildGlowstoneLamp(level, chunkPos, center.offset(-halfX + 3, 1, -halfZ + 3));
      buildGlowstoneLamp(level, chunkPos, center.offset(halfX - 3, 1, halfZ - 3));
      buildBaltigoRoomPartitions(level, chunkPos, center, halfX, halfZ, layer);
   }

   private static void buildBaltigoRoomPartitions(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int halfX, int halfZ, int layer) {
      for (int dx = -halfX + 2; dx <= halfX - 2; dx++) {
         if (Math.abs(dx) > 2 && Math.abs(dx) < halfX - 3) {
            for (int y = 1; y <= 3; y++) {
               setBlock(level, chunkPos, center.offset(dx, y, 0), baltigoWallState(dx, 0, y, layer));
            }
         }
      }

      for (int dz = -halfZ + 2; dz <= halfZ - 2; dz++) {
         if (Math.abs(dz) > 2 && Math.abs(dz) < halfZ - 3) {
            for (int y = 1; y <= 3; y++) {
               setBlock(level, chunkPos, center.offset(0, y, dz), baltigoWallState(0, dz, y, layer));
            }
         }
      }

      setBlock(level, chunkPos, center.above(), Blocks.AIR.defaultBlockState());
      setBlock(level, chunkPos, center.above(2), Blocks.AIR.defaultBlockState());
      setBlock(level, chunkPos, center.above(3), Blocks.AIR.defaultBlockState());
   }

   private static BlockState baltigoWallState(int dx, int dz, int y, int layer) {
      if ((Math.abs(dx) + Math.abs(dz) + y + layer) % 9 == 0) {
         return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
      } else {
         return y == 4 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
      }
   }

   private static void buildBaltigoCentralShaft(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, int minY, int maxY) {
      BlockPos center = new BlockPos(island.centerX(), minY, island.centerZ());

      for (int y = minY; y <= maxY; y++) {
         for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
               int dist = dx * dx + dz * dz;
               BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
               if (dist <= 25) {
                  setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
               } else if (dist <= 81 && (Math.abs(dx) == 9 || Math.abs(dz) == 9 || y % 13 == 0)) {
                  setBlock(level, chunkPos, pos, variant(dx + y, dz, 11) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
               }
            }
         }
      }
   }

   private static void buildBaltigoVerticalConnector(WorldGenLevel level, ChunkPos chunkPos, BlockPos base, int maxY) {
      BlockState ladder = (BlockState)Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);

      for (int y = base.getY(); y <= maxY; y++) {
         int dy = y - base.getY();

         for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
               int dist = dx * dx + dz * dz;
               BlockPos pos = new BlockPos(base.getX() + dx, y, base.getZ() + dz);
               if (dist <= 5) {
                  setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
               } else if (dist <= 16 && (dy % 13 == 0 || y == base.getY() || y == maxY)) {
                  setBlock(level, chunkPos, pos, variant(dx + y, dz, 6) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
               }
            }
         }

         setBlock(level, chunkPos, new BlockPos(base.getX(), y, base.getZ() - 3), ladder);
      }
   }

   private static void buildSkypieaLandingPool(WorldGenLevel level, ChunkPos chunkPos, IslandDefinition island, long seed) {
      int poolY = SkypieaLayout.streamTopY(seed);
      int poolX = SkypieaLayout.landingPoolX(island);
      int poolZ = SkypieaLayout.landingPoolZ(island);
      int maxDx = 108;
      int minDx = Math.max(-56, chunkPos.getMinBlockX() - island.centerX());
      int clampedMaxDx = Math.min(maxDx, chunkPos.getMaxBlockX() - island.centerX());
      int minDz = Math.max(-56, chunkPos.getMinBlockZ() - island.centerZ());
      int maxDz = Math.min(56, chunkPos.getMaxBlockZ() - island.centerZ());

      for (int dx = minDx; dx <= clampedMaxDx; dx++) {
         for (int dz = minDz; dz <= maxDz; dz++) {
            int x = island.centerX() + dx;
            int z = island.centerZ() + dz;
            int holeDist = dx * dx + dz * dz;
            int poolDx = x - poolX;
            int poolDz = z - poolZ;
            int poolDist = poolDx * poolDx + poolDz * poolDz;
            boolean stream = holeDist <= 576;
            boolean water = stream || poolDist <= 324 || dx >= 0 && dx <= 78 && Math.abs(dz) <= 7;
            boolean walkway = holeDist <= 3136 && holeDist >= 1849 || poolDist <= 676 || dx >= 0 && dx <= 78 && Math.abs(dz) <= 14;
            if (water || walkway) {
               BlockPos surface = new BlockPos(x, poolY, z);
               if (water) {
                  if (!stream) {
                     setBlock(level, chunkPos, surface.below(), variant(x, z, 5) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState());
                  }

                  setBlock(level, chunkPos, surface, Blocks.WATER.defaultBlockState());
                  setBlock(level, chunkPos, surface.above(), Blocks.AIR.defaultBlockState());
                  setBlock(level, chunkPos, surface.above(2), Blocks.AIR.defaultBlockState());
               } else {
                  placeSupported(
                     level, chunkPos, surface, variant(x, z, 4) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.CALCITE.defaultBlockState()
                  );
                  setBlock(level, chunkPos, surface.above(), Blocks.AIR.defaultBlockState());
                  if (variant(x + dz, z, 23) == 0) {
                     buildGlowstoneLamp(level, chunkPos, surface.above());
                  }
               }
            }
         }
      }
   }

   private static void buildSkypieaTemple(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int halfX, int halfZ, long seed) {
      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            boolean edge = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;
            BlockPos floor = center.offset(dx, 0, dz);
            placeSupported(level, chunkPos, floor, edge ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.CALCITE.defaultBlockState());

            for (int y = 1; y <= 7; y++) {
               setBlock(level, chunkPos, floor.above(y), Blocks.AIR.defaultBlockState());
            }
         }
      }

      for (int dx : new int[]{-halfX + 3, halfX - 3}) {
         for (int dz : new int[]{-halfZ + 3, halfZ - 3}) {
            for (int y = 1; y <= 8; y++) {
               setBlock(level, chunkPos, center.offset(dx, y, dz), y == 8 ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.QUARTZ_PILLAR.defaultBlockState());
            }
         }
      }

      for (int dx = -halfX + 2; dx <= halfX - 2; dx++) {
         for (int dz = -halfZ + 2; dz <= halfZ - 2; dz++) {
            if (Math.abs(dx) > halfX - 6 || Math.abs(dz) > halfZ - 6 || variant(dx, dz, 6) == 0) {
               setBlock(level, chunkPos, center.offset(dx, 8, dz), Blocks.QUARTZ_BLOCK.defaultBlockState());
            }
         }
      }

      placeLootChest(level, chunkPos, center.offset(0, 1, halfZ - 5), SKY_HOUSE_LOOT, seed + 1397446992L);
      if (contains(chunkPos, center.getX(), center.getZ())) {
         spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "skypiean_trader"), center.offset(0, 2, 0), seed + 17L);
      }
   }

   private static void buildSkypieaCloudHouse(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      int halfX = 6;
      int halfZ = 5;

      for (int dx = -halfX; dx <= halfX; dx++) {
         for (int dz = -halfZ; dz <= halfZ; dz++) {
            BlockPos floor = center.offset(dx, 0, dz);
            boolean wall = Math.abs(dx) == halfX || Math.abs(dz) == halfZ;
            placeSupported(
               level, chunkPos, floor, variant(dx, dz, 4) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.WOOL.white().defaultBlockState()
            );

            for (int y = 1; y <= 4; y++) {
               boolean door = dz == -halfZ && Math.abs(dx) <= 1 && y <= 2;
               if (wall && !door) {
                  if (y == 2 && Math.abs(dx) < halfX && Math.abs(dz) == halfZ) {
                     setBlock(level, chunkPos, floor.above(y), Blocks.GLASS.defaultBlockState());
                  } else {
                     setBlock(level, chunkPos, floor.above(y), y == 4 ? Blocks.CALCITE.defaultBlockState() : Blocks.WOOL.white().defaultBlockState());
                  }
               } else {
                  setBlock(level, chunkPos, floor.above(y), Blocks.AIR.defaultBlockState());
               }
            }

            if (Math.abs(dx) <= halfX - 1 && Math.abs(dz) <= halfZ - 1) {
               setBlock(level, chunkPos, floor.above(5), Blocks.WOOL.white().defaultBlockState());
            }
         }
      }

      buildGlowstoneLamp(level, chunkPos, center.offset(-halfX + 2, 1, halfZ - 2));
      placeLootChest(level, chunkPos, center.offset(2, 1, 1), SKY_HOUSE_LOOT, seed + 1213158739L);
   }

   private static void buildNetherGatewayFortress(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      for (int dx = -30; dx <= 30; dx++) {
         for (int dz = -24; dz <= 24; dz++) {
            BlockPos floor = center.offset(dx, 0, dz);
            boolean outer = Math.abs(dx) == 30 || Math.abs(dz) == 24;
            boolean bridge = Math.abs(dx) <= 5 || Math.abs(dz) <= 4;
            BlockState top = outer
               ? Blocks.NETHER_BRICKS.defaultBlockState()
               : (bridge ? Blocks.RED_NETHER_BRICKS.defaultBlockState() : (variant(dx, dz, 7) == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState()));
            placeSupported(level, chunkPos, floor, top, Blocks.BLACKSTONE.defaultBlockState());
            if (!bridge && !outer && Math.abs(dx) > 17 && Math.abs(dz) > 12 && variant(center.getX() + dx, center.getZ() + dz, 6) == 0) {
               setBlock(level, chunkPos, floor.above(), Blocks.LAVA.defaultBlockState());
            }
         }
      }

      clearAbove(level, chunkPos, center, 31, 10);

      for (int dx = -30; dx <= 30; dx++) {
         for (int dz : new int[]{-24, 24}) {
            buildNetherWall(level, chunkPos, center.offset(dx, 1, dz), Math.abs(dx) % 8 == 0);
         }
      }

      for (int dz = -24; dz <= 24; dz++) {
         for (int dx : new int[]{-30, 30}) {
            buildNetherWall(level, chunkPos, center.offset(dx, 1, dz), Math.abs(dz) % 8 == 0);
         }
      }

      for (int[] tower : new int[][]{{-22, -16}, {22, -16}, {-22, 16}, {22, 16}}) {
         buildNetherTower(level, chunkPos, center.offset(tower[0], 1, tower[1]));
      }

      buildNetherPortalFrame(level, chunkPos, center.offset(0, 1, -8));
      placeLootChest(level, chunkPos, center.offset(9, 1, 7), PIRATE_TREASURE_LOOT, seed + 1313166408L);
      if (contains(chunkPos, center.getX(), center.getZ())) {
         spawnEntity(level, Identifier.fromNamespaceAndPath("minecraft", "blaze"), center.offset(-6, 2, 9), seed + 13L);
         spawnEntity(level, Identifier.fromNamespaceAndPath("minecraft", "wither_skeleton"), center.offset(6, 2, 9), seed + 17L);
      }
   }

   private static void buildNetherWall(WorldGenLevel level, ChunkPos chunkPos, BlockPos pos, boolean pillar) {
      setBlock(level, chunkPos, pos, pillar ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.NETHER_BRICK_FENCE.defaultBlockState());
      if (pillar) {
         setBlock(level, chunkPos, pos.above(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
         setBlock(level, chunkPos, pos.above(2), Blocks.GLOWSTONE.defaultBlockState());
      }
   }

   private static void buildNetherTower(WorldGenLevel level, ChunkPos chunkPos, BlockPos base) {
      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            boolean wall = Math.abs(dx) == 3 || Math.abs(dz) == 3;

            for (int y = 0; y <= 7; y++) {
               BlockPos pos = base.offset(dx, y, dz);
               if (!wall && y > 0) {
                  setBlock(level, chunkPos, pos, Blocks.AIR.defaultBlockState());
               } else {
                  setBlock(level, chunkPos, pos, y == 7 ? Blocks.NETHER_BRICKS.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
               }
            }
         }
      }

      setBlock(level, chunkPos, base.above(8), Blocks.GLOWSTONE.defaultBlockState());
   }

   private static void buildNetherPortalFrame(WorldGenLevel level, ChunkPos chunkPos, BlockPos base) {
      for (int dx = -2; dx <= 2; dx++) {
         for (int y = 0; y <= 5; y++) {
            boolean frame = Math.abs(dx) == 2 || y == 0 || y == 5;
            BlockPos pos = base.offset(dx, y, 0);
            setBlock(level, chunkPos, pos, frame ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.NETHER_PORTAL.defaultBlockState());
         }
      }

      setBlock(level, chunkPos, base.offset(0, -1, 0), Blocks.MAGMA_BLOCK.defaultBlockState());
      setBlock(level, chunkPos, base.offset(0, 6, 0), Blocks.GLOWSTONE.defaultBlockState());
   }

   private static void buildEndGatewayDungeon(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      for (int dx = -26; dx <= 26; dx++) {
         for (int dz = -26; dz <= 26; dz++) {
            int distance = dx * dx + dz * dz;
            if (distance <= 676) {
               BlockPos floor = center.offset(dx, 0, dz);
               BlockState top = distance > 484
                  ? Blocks.OBSIDIAN.defaultBlockState()
                  : (variant(dx, dz, 5) == 0 ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.END_STONE_BRICKS.defaultBlockState());
               placeSupported(level, chunkPos, floor, top, Blocks.END_STONE.defaultBlockState());
            }
         }
      }

      clearAbove(level, chunkPos, center, 27, 9);

      for (int dx = -26; dx <= 26; dx++) {
         for (int dz = -26; dz <= 26; dz++) {
            if ((Math.abs(dx) == 26 || Math.abs(dz) == 26) && Math.abs(dx) + Math.abs(dz) <= 48) {
               for (int y = 1; y <= 4; y++) {
                  setBlock(level, chunkPos, center.offset(dx, y, dz), Blocks.END_STONE_BRICKS.defaultBlockState());
               }
            }
         }
      }

      buildEndPortalRoom(level, chunkPos, center.offset(0, 1, 0));

      for (int[] pillar : new int[][]{{-18, -18}, {18, -18}, {-18, 18}, {18, 18}}) {
         buildEndPillar(level, chunkPos, center.offset(pillar[0], 1, pillar[1]));
      }

      placeLootChest(level, chunkPos, center.offset(-10, 1, 12), FRUIT_CACHE_LOOT, seed + 4542020L);
      if (contains(chunkPos, center.getX(), center.getZ())) {
         spawnEntity(level, Identifier.fromNamespaceAndPath("minecraft", "enderman"), center.offset(10, 2, -10), seed + 23L);
         spawnEntity(level, Identifier.fromNamespaceAndPath("minecraft", "enderman"), center.offset(-10, 2, -10), seed + 29L);
      }
   }

   private static void buildEndPortalRoom(WorldGenLevel level, ChunkPos chunkPos, BlockPos center) {
      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            BlockPos pos = center.offset(dx, 0, dz);
            setBlock(level, chunkPos, pos, Blocks.OBSIDIAN.defaultBlockState());
            if (Math.abs(dx) == 5 || Math.abs(dz) == 5) {
               setBlock(level, chunkPos, pos.above(), Blocks.PURPUR_PILLAR.defaultBlockState());
               setBlock(level, chunkPos, pos.above(2), Blocks.PURPUR_PILLAR.defaultBlockState());
            }
         }
      }

      BlockState frame = (BlockState)Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, true);

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            boolean portal = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
            boolean frameRing = Math.abs(dx) == 2 || Math.abs(dz) == 2;
            if (portal) {
               setBlock(level, chunkPos, center.offset(dx, 1, dz), Blocks.END_PORTAL.defaultBlockState());
            } else if (frameRing) {
               setBlock(level, chunkPos, center.offset(dx, 1, dz), frame);
            }
         }
      }

      setBlock(level, chunkPos, center.above(4), Blocks.GLOWSTONE.defaultBlockState());
   }

   private static void buildEndPillar(WorldGenLevel level, ChunkPos chunkPos, BlockPos base) {
      for (int y = 0; y <= 8; y++) {
         setBlock(level, chunkPos, base.above(y), y == 8 ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
      }
   }

   private static void buildGlowstoneLamp(WorldGenLevel level, ChunkPos chunkPos, BlockPos pos) {
      setBlock(level, chunkPos, pos, Blocks.PRISMARINE_WALL.defaultBlockState());
      setBlock(level, chunkPos, pos.above(), Blocks.GLOWSTONE.defaultBlockState());
   }

   private static boolean placeTemplate(
      WorldGenLevel level, StructureTemplateManager templates, ChunkPos chunkPos, Identifier id, BlockPos center, Rotation rotation, long seed
   ) {
      Optional<StructureTemplate> optional = templates.get(id);
      if (optional.isEmpty()) {
         return false;
      }

      StructureTemplate template = optional.get();
      Vec3i size = template.getSize(rotation);
      BlockPos origin = center.offset(-size.getX() / 2, 0, -size.getZ() / 2);
      int minX = origin.getX();
      int minY = origin.getY();
      int minZ = origin.getZ();
      int maxX = origin.getX() + size.getX() - 1;
      int maxY = origin.getY() + size.getY() + 2;
      int maxZ = origin.getZ() + size.getZ() - 1;
      if (!contains(chunkPos, center.getX(), center.getZ())) {
         return false;
      }

      BoundingBox bounds = canWriteBox(level, minX, minY, minZ, maxX, maxY, maxZ)
         ? new BoundingBox(minX, Math.max(level.getMinY(), minY), minZ, maxX, Math.min(level.getMaxY() - 1, maxY), maxZ)
         : new BoundingBox(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), level.getMaxY() - 1, chunkPos.getMaxBlockZ());
      StructurePlaceSettings settings = new StructurePlaceSettings()
         .setMirror(Mirror.NONE)
         .setRotation(rotation)
         .setIgnoreEntities(true)
         .setBoundingBox(bounds)
         .setKnownShape(false)
         .setFinalizeEntities(true);
      boolean placed = template.placeInWorld(level, origin, origin, settings, RandomSource.create(seed ^ id.hashCode()), 2);
      if (placed) {
         cleanupTechnicalBlocks(level, bounds, origin, size);
      }

      return placed;
   }

   private static void spawnEntity(WorldGenLevel level, Identifier id, BlockPos pos, long seed) {
      if (level instanceof WorldGenRegion region) {
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(id);
         if (type != null) {
            ServerLevel serverLevel = region.getLevel();
            Entity entity = type.create(serverLevel, EntitySpawnReason.STRUCTURE);
            if (entity != null) {
               RandomSource random = RandomSource.create(seed ^ id.hashCode());
               entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
               entity.setYRot(random.nextFloat() * 360.0F);
               entity.setXRot(0.0F);
               if (entity instanceof Mob mob) {
                  mob.setBaby(true);
               }

               serverLevel.addFreshEntity(entity);
            }
         }
      }
   }

   private static BlockPos surfacePos(long seed, int x, int z, int yOffset) {
      int y = OnePieceGeography.terrainHeight(seed, x, z) + 1 + yOffset;
      return new BlockPos(x, y, z);
   }

   private static BlockPos groundPos(long seed, int x, int z) {
      int y = OnePieceGeography.terrainHeight(seed, x, z);
      return new BlockPos(x, y, z);
   }

   private static Identifier[] villagePalette(IslandDefinition island) {
      return switch (island.theme()) {
         case ALABASTA -> DESERT_VILLAGE;
         case DRUM -> SNOWY_VILLAGE;
         default -> {
            switch (island.region()) {
               case SOUTH_BLUE:
               case WEST_BLUE:
                  yield SAVANNA_VILLAGE;
               case NORTH_BLUE:
                  yield TAIGA_VILLAGE;
               default:
                  yield PLAINS_VILLAGE;
            }
         }
         case SABAODY -> SAVANNA_VILLAGE;
         case WANO, WHOLE_CAKE, ZOU -> TAIGA_VILLAGE;
      };
   }

   private static boolean contains(ChunkPos chunkPos, int x, int z) {
      return x >= chunkPos.getMinBlockX() && x <= chunkPos.getMaxBlockX() && z >= chunkPos.getMinBlockZ() && z <= chunkPos.getMaxBlockZ();
   }

   private static boolean mightIntersectChunk(
      StructureTemplateManager templates, ChunkPos chunkPos, Identifier id, Rotation rotation, int centerX, int centerZ
   ) {
      Optional<StructureTemplate> optional = templates.get(id);
      if (optional.isEmpty()) {
         return false;
      }

      Vec3i size = optional.get().getSize(rotation);
      int minX = centerX - size.getX() / 2;
      int minZ = centerZ - size.getZ() / 2;
      return intersectsBox(chunkPos, minX, minZ, minX + size.getX() - 1, minZ + size.getZ() - 1);
   }

   private static boolean intersectsBox(ChunkPos chunkPos, int minX, int minZ, int maxX, int maxZ) {
      return maxX >= chunkPos.getMinBlockX() && minX <= chunkPos.getMaxBlockX() && maxZ >= chunkPos.getMinBlockZ() && minZ <= chunkPos.getMaxBlockZ();
   }

   private static boolean canWriteBox(WorldGenLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      if (level instanceof WorldGenRegion region && maxY >= level.getMinY() && minY < level.getMaxY()) {
         int minChunkX = Math.floorDiv(minX, 16);
         int maxChunkX = Math.floorDiv(maxX, 16);
         int minChunkZ = Math.floorDiv(minZ, 16);
         int maxChunkZ = Math.floorDiv(maxZ, 16);

         for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
               if (!region.hasChunk(chunkX, chunkZ)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static Rotation rotation(long seed, int a, int b, int c) {
      Rotation[] values = Rotation.values();
      return values[Math.floorMod(mix(seed + a, b, c), values.length)];
   }

   private static void buildPirateCamp(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      for (int dx = -6; dx <= 6; dx++) {
         for (int dz = -6; dz <= 6; dz++) {
            if (dx * dx + dz * dz <= 42) {
               BlockPos pos = center.offset(dx, 0, dz);
               BlockState top = Math.abs(dx) != 6 && Math.abs(dz) != 6 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
               placeSupported(level, chunkPos, pos, top, Blocks.DIRT.defaultBlockState());
            }
         }
      }

      clearAbove(level, chunkPos, center, 6, 4);
      setBlock(level, chunkPos, center.above(), Blocks.CAMPFIRE.defaultBlockState());
      placeLootChest(level, chunkPos, center.offset(3, 1, 2), PIRATE_SUPPLIES_LOOT, seed);
      setBlock(level, chunkPos, center.offset(-3, 1, -2), Blocks.OAK_LOG.defaultBlockState());
      setBlock(level, chunkPos, center.offset(-2, 1, -3), Blocks.OAK_LOG.defaultBlockState());
      setBlock(level, chunkPos, center.offset(5, 1, 0), Blocks.OAK_FENCE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(-5, 1, 0), Blocks.OAK_FENCE.defaultBlockState());
      if (contains(chunkPos, center.getX(), center.getZ())) {
         spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "pirate_grunt"), center.above(), seed + 1L);
         spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "pirate_with_sword"), center.offset(2, 1, -2), seed + 2L);
      }
   }

   private static void buildMarineOutpost(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            if (Math.abs(dx) != 5 || Math.abs(dz) != 5) {
               BlockPos pos = center.offset(dx, 0, dz);
               BlockState top = Math.abs(dx) != 5 && Math.abs(dz) != 5 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
               placeSupported(level, chunkPos, pos, top, Blocks.STONE.defaultBlockState());
            }
         }
      }

      clearAbove(level, chunkPos, center, 6, 5);

      for (int dx : new int[]{-5, 5}) {
         for (int dz : new int[]{-5, 5}) {
            setBlock(level, chunkPos, center.offset(dx, 1, dz), Blocks.STONE_BRICK_WALL.defaultBlockState());
            setBlock(level, chunkPos, center.offset(dx, 2, dz), Blocks.STONE_BRICK_WALL.defaultBlockState());
         }
      }

      placeLootChest(level, chunkPos, center.offset(0, 1, 4), MARINE_SUPPLIES_LOOT, seed);
      if (contains(chunkPos, center.getX(), center.getZ())) {
         spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "marine_grunt"), center.above(), seed + 1L);
         spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "marine_with_gun"), center.offset(-2, 1, 1), seed + 2L);
      }
   }

   private static void buildCoastalRuin(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, IslandDefinition island, long seed) {
      BlockState brick = island.theme() == IslandTheme.ALABASTA ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState();

      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            if (Math.abs(dx) + Math.abs(dz) <= 8) {
               BlockPos pos = center.offset(dx, 0, dz);
               placeSupported(
                  level,
                  chunkPos,
                  pos,
                  variant(center.getX() + dx, center.getZ() + dz, 4) == 0 ? Blocks.GRAVEL.defaultBlockState() : brick,
                  Blocks.STONE.defaultBlockState()
               );
               if ((Math.abs(dx) == 5 || Math.abs(dz) == 5) && variant(dx, dz, 3) != 0) {
                  setBlock(level, chunkPos, pos.above(), brick);
                  if (variant(dx + center.getX(), dz + center.getZ(), 5) == 0) {
                     setBlock(level, chunkPos, pos.above(2), brick);
                  }
               }
            }
         }
      }

      clearAbove(level, chunkPos, center, 4, 3);
      placeLootChest(level, chunkPos, center.offset(1, 1, -1), FRUIT_CACHE_LOOT, seed);
      if (contains(chunkPos, center.getX(), center.getZ())) {
         spawnEntity(level, Identifier.fromNamespaceAndPath("minepiece", "bandit_grunt"), center.offset(-2, 1, 2), seed + 1L);
      }
   }

   private static void buildSupplyCave(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, long seed) {
      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -4; dz <= 4; dz++) {
            if (dx * dx + dz * dz <= 24) {
               placeSupported(level, chunkPos, center.offset(dx, 0, dz), Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState());
            }
         }
      }

      clearAbove(level, chunkPos, center, 3, 3);

      for (int y = 1; y <= 3; y++) {
         setBlock(level, chunkPos, center.offset(-4, y, 0), Blocks.STONE.defaultBlockState());
         setBlock(level, chunkPos, center.offset(4, y, 0), Blocks.STONE.defaultBlockState());
      }

      setBlock(level, chunkPos, center.offset(-3, 4, 0), Blocks.STONE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(-2, 4, 0), Blocks.STONE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(-1, 4, 0), Blocks.STONE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(0, 4, 0), Blocks.STONE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(1, 4, 0), Blocks.STONE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(2, 4, 0), Blocks.STONE.defaultBlockState());
      setBlock(level, chunkPos, center.offset(3, 4, 0), Blocks.STONE.defaultBlockState());
      placeLootChest(level, chunkPos, center.offset(0, 1, 2), PIRATE_TREASURE_LOOT, seed);
   }

   private static void buildShallowWreck(WorldGenLevel level, ChunkPos chunkPos, OmpStructureDefinition structure, long seed) {
      BlockPos center = new BlockPos(structure.centerX(), 61, structure.centerZ());

      for (int i = -9; i <= 9; i++) {
         int offset = Math.floorDiv(i, 3);
         setBlock(level, chunkPos, center.offset(i, 0, offset), Blocks.OAK_PLANKS.defaultBlockState());
         if (Math.abs(i) < 7) {
            setBlock(level, chunkPos, center.offset(i, 1, offset + 2), Blocks.OAK_LOG.defaultBlockState());
         }

         if (i % 4 == 0) {
            setBlock(level, chunkPos, center.offset(i, 1, offset - 2), Blocks.OAK_FENCE.defaultBlockState());
         }
      }

      placeLootChest(
         level,
         chunkPos,
         center.offset(1, 1, 1),
         structure.kind() == OmpStructureKind.MARINE_BATTLESHIP ? MARINE_SUPPLIES_LOOT : PIRATE_TREASURE_LOOT,
         seed + structure.id().hashCode()
      );
      spawnStructureMobs(level, chunkPos, structure, center.above(), seed);
   }

   private static void buildSeaRuin(WorldGenLevel level, ChunkPos chunkPos, OmpStructureDefinition structure, long seed) {
      BlockPos center = new BlockPos(structure.centerX(), 58, structure.centerZ());

      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            if (Math.abs(dx) + Math.abs(dz) <= 8 && variant(dx + structure.centerX(), dz + structure.centerZ(), 4) != 0) {
               setBlock(level, chunkPos, center.offset(dx, 0, dz), Blocks.PRISMARINE_BRICKS.defaultBlockState());
               if ((Math.abs(dx) == 5 || Math.abs(dz) == 5) && variant(dx, dz, 3) == 0) {
                  setBlock(level, chunkPos, center.offset(dx, 1, dz), Blocks.PRISMARINE.defaultBlockState());
               }
            }
         }
      }

      placeLootChest(level, chunkPos, center.offset(0, 1, 0), FRUIT_CACHE_LOOT, seed + structure.id().hashCode());
      spawnStructureMobs(level, chunkPos, structure, center.above(), seed);
   }

   private static void spawnStructureMobs(WorldGenLevel level, ChunkPos chunkPos, OmpStructureDefinition structure, BlockPos center, long seed) {
      if (contains(chunkPos, structure.centerX(), structure.centerZ())) {
         for (int i = 0; i < Math.min(2, structure.mobSpawns().size()); i++) {
            spawnEntity(level, structure.mobSpawns().get(i), center.offset(i * 2, 0, -i * 2), seed + structure.id().hashCode() + i * 37L);
         }
      }
   }

   private static void placeSupported(WorldGenLevel level, ChunkPos chunkPos, BlockPos pos, BlockState top, BlockState foundation) {
      for (int y = pos.getY() - 3; y < pos.getY(); y++) {
         setBlock(level, chunkPos, new BlockPos(pos.getX(), y, pos.getZ()), foundation);
      }

      setBlock(level, chunkPos, pos, top);
   }

   private static void clearAbove(WorldGenLevel level, ChunkPos chunkPos, BlockPos center, int radius, int height) {
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (dx * dx + dz * dz <= radius * radius) {
               for (int y = 1; y <= height; y++) {
                  setBlock(level, chunkPos, center.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
               }
            }
         }
      }
   }

   private static void clearBox(WorldGenLevel level, ChunkPos chunkPos, BlockPos from, BlockPos to) {
      int minX = Math.min(from.getX(), to.getX());
      int maxX = Math.max(from.getX(), to.getX());
      int minY = Math.min(from.getY(), to.getY());
      int maxY = Math.max(from.getY(), to.getY());
      int minZ = Math.min(from.getZ(), to.getZ());
      int maxZ = Math.max(from.getZ(), to.getZ());

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               setBlock(level, chunkPos, new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
            }
         }
      }
   }

   private static void cleanupTechnicalBlocks(WorldGenLevel level, BoundingBox bounds, BlockPos origin, Vec3i size) {
      int minX = Math.max(origin.getX(), bounds.minX());
      int maxX = Math.min(origin.getX() + size.getX() - 1, bounds.maxX());
      int minZ = Math.max(origin.getZ(), bounds.minZ());
      int maxZ = Math.min(origin.getZ() + size.getZ() - 1, bounds.maxZ());
      int minY = Math.max(origin.getY(), level.getMinY());
      int maxY = Math.min(origin.getY() + size.getY() + 2, level.getMaxY() - 1);
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               pos.set(x, y, z);
               BlockState state = level.getBlockState(pos);
               if (state.getBlock() == Blocks.JIGSAW || state.getBlock() == Blocks.STRUCTURE_BLOCK || state.getBlock() == Blocks.STRUCTURE_VOID) {
                  level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2, 0);
               }
            }
         }
      }
   }

   private static void placeLootChest(WorldGenLevel level, ChunkPos chunkPos, BlockPos pos, Identifier loot, long seed) {
      setBlock(level, chunkPos, pos, Blocks.CHEST.defaultBlockState());
      if (contains(chunkPos, pos.getX(), pos.getZ())) {
         if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, loot));
            container.setLootTableSeed(seed);
         }
      }
   }

   private static void setBlock(WorldGenLevel level, ChunkPos chunkPos, BlockPos pos, BlockState state) {
      if (pos.getY() >= level.getMinY() && pos.getY() < level.getMaxY()) {
         if (contains(chunkPos, pos.getX(), pos.getZ())) {
            level.setBlock(pos, state, 2, 0);
         }
      }
   }

   private static Identifier[] ids(String namespace, String... paths) {
      Identifier[] ids = new Identifier[paths.length];

      for (int i = 0; i < paths.length; i++) {
         ids[i] = Identifier.fromNamespaceAndPath(namespace, paths[i]);
      }

      return ids;
   }

   private static long mix(long seed, int x, int z) {
      long h = seed ^ -2960836687051489901L;
      h ^= x * -7046029254386353131L;
      h = Long.rotateLeft(h, 29);
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

   private static int variant(int x, int z, int modulo) {
      return Math.floorMod(x * 73428767 ^ z * 91293481, modulo);
   }
}
