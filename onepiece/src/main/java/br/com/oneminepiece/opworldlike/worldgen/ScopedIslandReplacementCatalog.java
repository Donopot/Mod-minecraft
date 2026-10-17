package br.com.oneminepiece.opworldlike.worldgen;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class ScopedIslandReplacementCatalog {
   private static final Set<String> REPLACED_ISLANDS = Set.of(
      "alabasta",
      "amazon_lily",
      "rare_animals",
      "conomi_islands",
      "baratie",
      "drum",
      "starter_east_blue",
      "nether_gateway",
      "jaya",
      "kuraigana",
      "little_garden",
      "polestar_islands",
      "long_ring_long_land",
      "marineford",
      "mirror_ball_island",
      "starter_west_blue",
      "organ_islands",
      "oykot_kingdom",
      "punk_hazard",
      "sabaody",
      "shells_town",
      "shimotsuki_village",
      "gecko_islands",
      "tequila_wolf",
      "thriller_bark",
      "wano",
      "water_7",
      "cactus_island"
   );
   private static final Map<String, ScopedIslandReplacementCatalog.SourceReference> SOURCE_REFERENCES = Map.ofEntries(
      Map.entry("alabasta", ref("Alabasta", 9794, 3159, 1040)),
      Map.entry("amazon_lily", ref("Amazon Lily", 19495, 2274, 1040)),
      Map.entry("rare_animals", ref("Animal Island", 2045, -652, 1040)),
      Map.entry("conomi_islands", ref("Arlong Park", 2821, -2902, 1040)),
      Map.entry("baratie", ref("Baratie", 3348, -1160, 1040)),
      Map.entry("drum", ref("Drum", 7889, 2110, 1040)),
      Map.entry("starter_east_blue", ref("Fushia / Dawn Island", 7815, -1606, 1040)),
      Map.entry("nether_gateway", ref("Impel Down", 18760, 5304, 1040)),
      Map.entry("jaya", ref("Jaya", 10305, 2113, 1040)),
      Map.entry("kuraigana", ref("Kuraigana", 22662, 3749, 1040)),
      Map.entry("little_garden", ref("Little Garden", 4891, 2794, 1040)),
      Map.entry("polestar_islands", ref("Logue Town", 808, -1017, 1040)),
      Map.entry("long_ring_long_land", ref("Long Ring Long Land", 13103, 3990, 1040)),
      Map.entry("marineford", ref("Marineford", 21102, 4145, 1040)),
      Map.entry("mirror_ball_island", ref("Mirror Ball Island", 7687, 7, 1040)),
      Map.entry("starter_west_blue", ref("Ohara", 12747, 1162, 1040)),
      Map.entry("organ_islands", ref("Orange Town", 4736, -782, 1040)),
      Map.entry("oykot_kingdom", ref("Oykot Kingdom", 5290, -3051, 1040)),
      Map.entry("punk_hazard", ref("Punk Hazard", 27144, 1588, 112)),
      Map.entry("sabaody", ref("Sabaody", 23067, 1238, 1040)),
      Map.entry("shells_town", ref("Shells Town", 5466, -1942, 1040)),
      Map.entry("shimotsuki_village", ref("Shimotsuki", 9023, -1292, 1040)),
      Map.entry("gecko_islands", ref("Syrup", 3864, -3090, 1040)),
      Map.entry("tequila_wolf", ref("Tequila Wolf", 12357, -718, 1040)),
      Map.entry("thriller_bark", ref("Thriller Bark", 17116, 1541, 1040)),
      Map.entry("wano", ref("Wa no Kuni", 37254, 992, 112)),
      Map.entry("water_7", ref("Water 7", 14877, 3057, 1040)),
      Map.entry("cactus_island", ref("Whiskey Peak", 893, 1492, 1040))
   );

   private ScopedIslandReplacementCatalog() {
   }

   static boolean replaces(IslandDefinition island) {
      return island != null && REPLACED_ISLANDS.contains(island.id());
   }

   static Optional<ScopedIslandReplacementCatalog.SourceReference> sourceReference(IslandDefinition island) {
      return island == null ? Optional.empty() : Optional.ofNullable(SOURCE_REFERENCES.get(island.id()));
   }

   static int surfaceHeight(long seed, IslandDefinition island, int x, int z, int fallback) {
      if (!replaces(island)) {
         return fallback;
      }

      double ratio = clamp(island.distanceRatio(x, z), 0.0, 1.2);
      double inland = smoothStep(clamp((0.9 - ratio) / 0.62, 0.0, 1.0));
      double shelf = smoothStep(clamp((0.94 - ratio) / 0.2, 0.0, 1.0));
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();
      double localNoise = noise(seed + island.id().hashCode() * 71L, x, z, 3);

      double height = switch (island.id()) {
         case "starter_east_blue" -> dawnSurface(island, x, z, inland, shelf, localNoise);
         case "starter_west_blue" -> oharaSurface(island, x, z, inland, shelf, localNoise);
         case "shells_town" -> marineTownSurface(lx, lz, inland, shelf, localNoise, 22.0, 0.18);
         case "shimotsuki_village" -> villageSurface(lx, lz, inland, shelf, localNoise, 13.0, 0.26);
         case "organ_islands" -> townIslandSurface(lx, lz, inland, shelf, localNoise, 17.0);
         case "rare_animals" -> jungleAnimalSurface(seed, island, x, z, inland, shelf, localNoise);
         case "gecko_islands" -> villageSurface(lx, lz, inland, shelf, localNoise, 15.0, -0.18);
         case "conomi_islands" -> arlongParkSurface(lx, lz, inland, shelf, localNoise);
         case "oykot_kingdom" -> oykotSurface(seed, island, x, z, inland, shelf, localNoise);
         case "polestar_islands" -> logueTownSurface(lx, lz, inland, shelf, localNoise);
         case "mirror_ball_island" -> mirrorBallSurface(lx, lz, inland, shelf, localNoise);
         case "marineford" -> marinefordSurface(lx, lz, ratio, inland, shelf, localNoise);
         case "sabaody" -> sabaodySurface(seed, island, x, z, ratio, inland, shelf, localNoise);
         case "nether_gateway" -> impelDownSurface(lx, lz, ratio, inland, shelf, localNoise);
         case "punk_hazard" -> punkHazardSurface(island, x, ratio, inland, shelf, localNoise);
         default -> fallback;
      };
      return clampInt((int)Math.round(height), 64, 292);
   }

   static BlockState interiorState(IslandDefinition island, int x, int y, int z, int surface) {
      if (replaces(island) && y >= surface - 14) {
         double ratio = island.distanceRatio(x, z);
         double lx = (double)(x - island.centerX()) / island.radius();
         double lz = (double)(z - island.centerZ()) / island.radius();

         return switch (island.id()) {
            case "shells_town", "marineford" -> ratio < 0.62 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.STONE.defaultBlockState();
            case "polestar_islands" -> ratio < 0.7 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
            case "mirror_ball_island" -> ratio < 0.64 ? Blocks.QUARTZ_BLOCK.defaultBlockState() : Blocks.CALCITE.defaultBlockState();
            case "conomi_islands" -> arlongPlatform(lx, lz) ? Blocks.STONE_BRICKS.defaultBlockState() : null;
            case "organ_islands" -> townCore(lx, lz) ? Blocks.OAK_PLANKS.defaultBlockState() : null;
            case "nether_gateway" -> ratio < 0.62 ? (variant(x + y, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState()) : null;
            default -> null;
         };
      } else {
         return null;
      }
   }

   static BlockState topState(IslandDefinition island, int x, int z, int surface) {
      if (!replaces(island)) {
         return null;
      }

      double ratio = island.distanceRatio(x, z);
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();

      return switch (island.id()) {
         case "starter_east_blue" -> dawnTop(lx, lz, x, z);
         case "starter_west_blue" -> oharaTop(ratio, x, z);
         case "shells_town" -> marineTownTop(lx, lz, ratio, x, z);
         case "shimotsuki_village" -> shimotsukiTop(lx, lz, x, z);
         case "organ_islands" -> townCore(lx, lz) ? Blocks.OAK_PLANKS.defaultBlockState() : grassTownTop(x, z);
         case "rare_animals" -> jungleTop(x, z);
         case "gecko_islands" -> villageRoad(lx, lz) ? Blocks.COARSE_DIRT.defaultBlockState() : grassTownTop(x, z);
         case "conomi_islands" -> arlongPlatform(lx, lz) ? Blocks.STONE_BRICKS.defaultBlockState() : mangroveTop(x, z);
         case "oykot_kingdom" -> variant(x, z, 8) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         case "polestar_islands" -> logueTownRoad(lx, lz) ? Blocks.SMOOTH_STONE.defaultBlockState() : (ratio < 0.68 ? Blocks.STONE_BRICKS.defaultBlockState() : grassTownTop(x, z));
         case "mirror_ball_island" -> mirrorPlaza(lx, lz)
            ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
            : (variant(x, z, 6) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
         case "marineford" -> marinefordTop(lx, lz, ratio, x, z);
         case "sabaody" -> sabaodyTop(lx, lz, x, z);
         case "nether_gateway" -> impelDownTop(lx, lz, ratio, x, z);
         case "punk_hazard" -> punkHazardTop(island, x, z, surface);
         default -> null;
      };
   }

   static BlockState fillerState(IslandDefinition island, int x, int z, int surface) {
      if (!replaces(island)) {
         return null;
      }

      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();

      return switch (island.id()) {
         case "starter_west_blue", "mirror_ball_island" -> variant(x, z, 5) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case "shells_town", "polestar_islands", "marineford" -> Blocks.STONE_BRICKS.defaultBlockState();
         case "conomi_islands" -> arlongPlatform(lx, lz)
            ? Blocks.STONE_BRICKS.defaultBlockState()
            : (variant(x, z, 4) == 0 ? Blocks.MUD.defaultBlockState() : Blocks.DIRT.defaultBlockState());
         case "rare_animals", "sabaody" -> variant(x, z, 4) == 0 ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case "nether_gateway" -> variant(x, z, 4) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
         case "punk_hazard" -> x < island.centerX()
            ? (variant(x, z, 4) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.DIRT.defaultBlockState())
            : Blocks.BASALT.defaultBlockState();
         default -> Blocks.DIRT.defaultBlockState();
      };
   }

   static BlockState rockState(IslandDefinition island, int x, int z, int surface) {
      if (!replaces(island)) {
         return null;
      }

      return switch (island.id()) {
         case "shells_town", "polestar_islands", "marineford", "mirror_ball_island" -> Blocks.STONE_BRICKS.defaultBlockState();
         case "conomi_islands" -> variant(x, z, 4) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
         case "rare_animals", "sabaody", "starter_west_blue" -> variant(x, z, 5) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "nether_gateway" -> variant(x, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
         case "punk_hazard" -> x < island.centerX()
            ? (variant(x, z, 3) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.STONE.defaultBlockState())
            : (variant(x, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState());
         default -> null;
      };
   }

   static boolean noBeach(IslandDefinition island) {
      if (!replaces(island)) {
         return false;
      }

      return switch (island.id()) {
         case "baratie", "tequila_wolf", "water_7", "thriller_bark", "nether_gateway", "marineford", "polestar_islands", "mirror_ball_island" -> true;
         default -> false;
      };
   }

   private static double dawnSurface(IslandDefinition island, int x, int z, double inland, double shelf, double noise) {
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();
      double windmillHill = radial(lx, lz, -0.2, -0.1, 0.2);
      double harbor = radial(lx, lz, 0.46, 0.02, 0.13);
      return 66.0 + shelf * 5.0 + inland * (10.0 + windmillHill * 14.0) - harbor * 4.0 + noise;
   }

   private static double oharaSurface(IslandDefinition island, int x, int z, double inland, double shelf, double noise) {
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();
      double tree = radial(lx, lz, 0.0, 0.0, 0.24);
      double libraryRing = Math.exp(-Math.pow((Math.hypot(lx, lz) - 0.36) * 7.0, 2.0));
      return 67.0 + shelf * 7.0 + inland * (17.0 + tree * 35.0 + libraryRing * 6.0) + noise;
   }

   private static double marineTownSurface(double lx, double lz, double inland, double shelf, double noise, double plateauHeight, double plazaOffset) {
      double plaza = radial(lx, lz, 0.0, plazaOffset, 0.24);
      double harbor = radial(lx, lz, 0.42, 0.0, 0.16);
      return 67.0 + shelf * 6.0 + inland * (12.0 + plaza * plateauHeight) - harbor * 3.0 + noise;
   }

   private static double villageSurface(double lx, double lz, double inland, double shelf, double noise, double hillHeight, double roadOffset) {
      double dojo = radial(lx, lz, -0.12, roadOffset, 0.2);
      double cove = radial(lx, lz, 0.52, -0.1, 0.13);
      return 66.0 + shelf * 5.0 + inland * (9.0 + dojo * hillHeight) - cove * 3.0 + noise;
   }

   private static double townIslandSurface(double lx, double lz, double inland, double shelf, double noise, double townHeight) {
      double town = radial(lx, lz, 0.0, 0.0, 0.28);
      double sideHarbor = radial(lx, lz, -0.5, 0.06, 0.14);
      return 66.0 + shelf * 6.0 + inland * (10.0 + town * townHeight) - sideHarbor * 4.0 + noise;
   }

   private static double jungleAnimalSurface(long seed, IslandDefinition island, int x, int z, double inland, double shelf, double noise) {
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();
      double crown = radial(lx, lz, -0.04, 0.06, 0.3);
      double rough = Math.max(0.0, smoothNoise(seed + 41233L, x, z, 130.0));
      return 68.0 + shelf * 7.0 + inland * (18.0 + crown * 28.0 + rough * 12.0) + noise;
   }

   private static double arlongParkSurface(double lx, double lz, double inland, double shelf, double noise) {
      double park = arlongPlatform(lx, lz) ? 1.0 : radial(lx, lz, 0.0, -0.04, 0.18);
      double lagoon = radial(lx, lz, 0.34, 0.16, 0.15);
      return 66.0 + shelf * 6.0 + inland * (11.0 + park * 18.0) - lagoon * 4.0 + noise;
   }

   private static double oykotSurface(long seed, IslandDefinition island, int x, int z, double inland, double shelf, double noise) {
      double rolling = Math.max(0.0, smoothNoise(seed + 2576L, x, z, 180.0));
      return 67.0 + shelf * 7.0 + inland * (13.0 + rolling * 13.0) + noise;
   }

   private static double logueTownSurface(double lx, double lz, double inland, double shelf, double noise) {
      double town = radial(lx, lz, 0.0, 0.0, 0.36);
      double executionPlaza = radial(lx, lz, -0.1, -0.04, 0.14);
      double harbor = radial(lx, lz, 0.54, 0.0, 0.16);
      return 68.0 + shelf * 6.0 + inland * (12.0 + town * 16.0 + executionPlaza * 8.0) - harbor * 3.0 + noise;
   }

   private static double mirrorBallSurface(double lx, double lz, double inland, double shelf, double noise) {
      double plaza = radial(lx, lz, 0.0, 0.0, 0.32);
      double stage = radial(lx, lz, 0.0, -0.12, 0.13);
      return 68.0 + shelf * 5.0 + inland * (10.0 + plaza * 12.0 + stage * 8.0) + noise;
   }

   private static double marinefordSurface(double lx, double lz, double ratio, double inland, double shelf, double noise) {
      double crescent = Math.exp(-Math.pow((Math.hypot(lx, lz + 0.1) - 0.42) * 8.0, 2.0));
      double fortress = radial(lx, lz, 0.0, -0.08, 0.24);
      double bay = radial(lx, lz, 0.0, 0.48, 0.22);
      return 70.0 + shelf * 8.0 + inland * (20.0 + crescent * 18.0 + fortress * 30.0) - bay * 11.0 - Math.max(0.0, ratio - 0.82) * 8.0 + noise;
   }

   private static double sabaodySurface(long seed, IslandDefinition island, int x, int z, double ratio, double inland, double shelf, double noise) {
      double grove = 0.0;

      for (int i = 0; i < 7; i++) {
         double angle = i * Math.PI * 2.0 / 7.0 + 0.25;
         double px = Math.cos(angle) * 0.4;
         double pz = Math.sin(angle) * 0.32;
         grove = Math.max(grove, radial((double)(x - island.centerX()) / island.radius(), (double)(z - island.centerZ()) / island.radius(), px, pz, 0.12));
      }

      double mangroveRoll = Math.max(0.0, smoothNoise(seed + 5945869L, x, z, 95.0));
      return 66.0 + shelf * 4.0 + inland * (8.0 + grove * 26.0 + mangroveRoll * 7.0) - Math.max(0.0, ratio - 0.84) * 4.0 + noise;
   }

   private static double impelDownSurface(double lx, double lz, double ratio, double inland, double shelf, double noise) {
      double prisonCore = radial(lx, lz, 0.0, 0.0, 0.2);
      double ring = Math.exp(-Math.pow((Math.hypot(lx, lz) - 0.42) * 8.0, 2.0));
      double trench = Math.exp(-Math.pow((ratio - 0.78) * 9.0, 2.0));
      return 68.0 + shelf * 7.0 + inland * (14.0 + prisonCore * 44.0 + ring * 16.0) - trench * 8.0 + noise;
   }

   private static double punkHazardSurface(IslandDefinition island, int x, double ratio, double inland, double shelf, double noise) {
      double side = x < island.centerX() ? -1.0 : 1.0;
      double core = Math.max(0.0, 1.0 - ratio / 0.62);
      double ridge = Math.exp(-Math.pow((ratio - 0.44) * 7.0, 2.0));
      double sideBoost = side > 0.0 ? core * 52.0 + ridge * 18.0 : core * 40.0 + ridge * 12.0;
      return 68.0 + shelf * 9.0 + inland * (22.0 + sideBoost) + noise;
   }

   private static BlockState dawnTop(double lx, double lz, int x, int z) {
      return Math.abs(lz) < 0.025 && lx > -0.54 && lx < 0.5 ? Blocks.COARSE_DIRT.defaultBlockState() : grassTownTop(x, z);
   }

   private static BlockState oharaTop(double ratio, int x, int z) {
      if (ratio < 0.24) {
         return variant(x, z, 5) == 0 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      } else {
         return variant(x, z, 8) == 0 ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      }
   }

   private static BlockState marineTownTop(double lx, double lz, double ratio, int x, int z) {
      if (!(Math.abs(lx) < 0.1) && !(Math.abs(lz) < 0.08) && !(ratio < 0.36)) {
         return grassTownTop(x, z);
      } else {
         return variant(x, z, 5) == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
      }
   }

   private static BlockState shimotsukiTop(double lx, double lz, int x, int z) {
      if (Math.abs(lz - 0.26) < 0.045 && Math.abs(lx) < 0.54) {
         return Blocks.COARSE_DIRT.defaultBlockState();
      } else {
         return variant(x, z, 9) == 0 ? Blocks.PODZOL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      }
   }

   private static BlockState grassTownTop(int x, int z) {
      int v = variant(x, z, 12);
      if (v == 0) {
         return Blocks.COARSE_DIRT.defaultBlockState();
      } else {
         return v == 1 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      }
   }

   private static BlockState jungleTop(int x, int z) {
      int v = variant(x, z, 10);
      if (v <= 1) {
         return Blocks.PODZOL.defaultBlockState();
      } else {
         return v <= 3 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      }
   }

   private static BlockState mangroveTop(int x, int z) {
      int v = variant(x, z, 9);
      if (v <= 1) {
         return Blocks.MUD.defaultBlockState();
      } else {
         return v == 2 ? Blocks.MANGROVE_ROOTS.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      }
   }

   private static BlockState marinefordTop(double lx, double lz, double ratio, int x, int z) {
      if (!(ratio < 0.7) && !(Math.abs(lx) < 0.12) && !(Math.abs(lz + 0.1) < 0.1)) {
         return Blocks.STONE.defaultBlockState();
      } else {
         return variant(x, z, 6) == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
      }
   }

   private static BlockState sabaodyTop(double lx, double lz, int x, int z) {
      double rootLine = Math.min(Math.abs(lx), Math.abs(lz));
      if (rootLine < 0.025 && Math.hypot(lx, lz) < 0.74) {
         return Blocks.MANGROVE_ROOTS.defaultBlockState();
      } else {
         int v = variant(x, z, 9);
         if (v <= 1) {
            return Blocks.MUD.defaultBlockState();
         } else {
            return v <= 3 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         }
      }
   }

   private static BlockState impelDownTop(double lx, double lz, double ratio, int x, int z) {
      if (!(ratio < 0.52) && !(Math.abs(Math.hypot(lx, lz) - 0.42) < 0.045)) {
         return variant(x, z, 5) == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
      } else {
         return Blocks.BLACKSTONE.defaultBlockState();
      }
   }

   private static BlockState punkHazardTop(IslandDefinition island, int x, int z, int surface) {
      if (x < island.centerX()) {
         if (surface >= 155) {
            return variant(x, z, 4) == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
         } else {
            return variant(x, z, 5) == 0 ? Blocks.ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
         }
      } else {
         return variant(x, z, 6) == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
      }
   }

   private static boolean townCore(double lx, double lz) {
      return Math.abs(lx) < 0.25 && Math.abs(lz) < 0.18;
   }

   private static boolean villageRoad(double lx, double lz) {
      return Math.abs(lz) < 0.04 && Math.abs(lx) < 0.7 || Math.abs(lx + 0.16) < 0.035 && Math.abs(lz) < 0.48;
   }

   private static boolean logueTownRoad(double lx, double lz) {
      return Math.abs(lx) < 0.055 && Math.abs(lz) < 0.72 || Math.abs(lz) < 0.055 && Math.abs(lx) < 0.72;
   }

   private static boolean mirrorPlaza(double lx, double lz) {
      return Math.hypot(lx, lz) < 0.42 || Math.abs(lx) < 0.06 && Math.abs(lz) < 0.72;
   }

   private static boolean arlongPlatform(double lx, double lz) {
      return Math.abs(lx) < 0.26 && Math.abs(lz + 0.04) < 0.18 || Math.hypot(lx - 0.18, lz + 0.02) < 0.18;
   }

   private static double radial(double lx, double lz, double cx, double cz, double width) {
      double dx = (lx - cx) / width;
      double dz = (lz - cz) / width;
      return Math.exp(-(dx * dx + dz * dz));
   }

   private static double smoothNoise(long seed, int x, int z, double scale) {
      return Math.sin((x + (seed & 1023L)) / scale) * 0.55 + Math.cos((z - (seed >>> 10 & 1023L)) / (scale * 1.27)) * 0.45;
   }

   private static double noise(long seed, int x, int z, int amplitude) {
      return (unit(mix(seed, Math.floorDiv(x, 19), Math.floorDiv(z, 19))) * 2.0 - 1.0) * amplitude;
   }

   private static int variant(int x, int z, int modulo) {
      return Math.floorMod(x * 73428767 ^ z * 91293481, modulo);
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

   private static ScopedIslandReplacementCatalog.SourceReference ref(String label, int x, int z, int estimatedDiameter) {
      return new ScopedIslandReplacementCatalog.SourceReference(label, x, z, estimatedDiameter);
   }

   record SourceReference(String label, int sourceX, int sourceZ, int estimatedDiameter) {
   }
}
