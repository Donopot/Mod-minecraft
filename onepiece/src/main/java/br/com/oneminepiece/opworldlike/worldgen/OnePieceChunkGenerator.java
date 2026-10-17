package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class OnePieceChunkGenerator extends ChunkGenerator {
   private static final int REGISTRY_STRUCTURE_ISLAND_CLEARANCE = 512;
   public static final MapCodec<OnePieceChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
         )
         .apply(instance, OnePieceChunkGenerator::new)
   );
   private final Holder<NoiseGeneratorSettings> settings;
   private final NoiseBasedChunkGenerator delegate;
   private volatile long worldSeed;

   public OnePieceChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
      super(biomeSource);
      this.settings = settings;
      this.delegate = new NoiseBasedChunkGenerator(biomeSource, settings);
   }

   @Override
   protected MapCodec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   @Override
   public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long seed) {
      this.worldSeed = seed;
      if (this.getBiomeSource() instanceof OnePieceBiomeSource source) {
         source.setWorldSeed(seed);
      }

      return this.delegate.createState(structureSets, randomState, seed);
   }

   @Override
   public void createStructures(
      RegistryAccess registryAccess,
      ChunkGeneratorStructureState structureState,
      StructureManager structureManager,
      ChunkAccess chunk,
      StructureTemplateManager templates,
      ResourceKey<Level> dimension
   ) {
      ChunkPos pos = chunk.getPos();
      int centerX = (pos.getMinBlockX() + pos.getMaxBlockX()) / 2;
      int centerZ = (pos.getMinBlockZ() + pos.getMaxBlockZ()) / 2;
      if (!FishmanIslandLayout.reservedAreaIntersects(centerX, centerZ, 528)
         && OnePieceGeography.islandsNear(this.worldSeed, pos.getMinBlockX() - 512, pos.getMinBlockZ() - 512, pos.getMaxBlockX() + 512, pos.getMaxBlockZ() + 512).isEmpty()) {
         this.delegate.createStructures(registryAccess, structureState, structureManager, chunk, templates, dimension);
      }
   }

   @Override
   public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
      return this.delegate.createBiomes(randomState, blender, structureManager, chunk);
   }

   @Override
   public CompletableFuture<ChunkAccess> buildTerrain(
      ChunkAccess chunk,
      Blender blender,
      RandomState randomState,
      StructureManager structureManager,
      BiomeManager biomeManager,
      WorldGenRegion region,
      Set<Holder<Biome>> biomes
   ) {
      return this.delegate.buildTerrain(chunk, blender, randomState, structureManager, biomeManager, region, biomes).thenApply(generated -> {
         this.applyOnePieceMask(generated, true);
         this.applyOnePieceMask(generated, false);
         this.clearHighOceanWater(generated);
         this.applyIslandThemeSurface(generated);
         Heightmap.primeHeightmaps(generated, EnumSet.of(Types.OCEAN_FLOOR_WG, Types.WORLD_SURFACE_WG));
         return generated;
      });
   }

   @Override
   public void spawnOriginalMobs(WorldGenRegion region) {
      this.delegate.spawnOriginalMobs(region);
   }

   @Override
   public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
      if (this.reinforceProceduralIslandTerrain(chunk)) {
         Heightmap.primeHeightmaps(chunk, EnumSet.of(Types.OCEAN_FLOOR_WG, Types.WORLD_SURFACE_WG));
      }

      this.delegate.applyBiomeDecoration(level, chunk, structureManager);
      this.clearReverseMountainChannels(chunk);
      OmpStructurePlacer.place(level, chunk, this.worldSeed);
      this.clearHighOceanWater(chunk);
      Heightmap.primeHeightmaps(chunk, EnumSet.of(Types.OCEAN_FLOOR_WG, Types.WORLD_SURFACE_WG));
   }

   @Override
   public int getGenDepth() {
      return 864;
   }

   @Override
   public int getSeaLevel() {
      return 63;
   }

   @Override
   public int getMinY() {
      return -352;
   }

   @Override
   public int getBaseHeight(int x, int z, Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
      int vanillaSurface = this.delegate.getBaseHeight(x, z, type, heightAccessor, randomState) - 1;
      return this.maskedSurfaceHeight(x, z, vanillaSurface) + 1;
   }

   @Override
   public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
      int minY = heightAccessor.getMinY();
      int height = heightAccessor.getHeight();
      BlockState[] states = new BlockState[height];
      NoiseColumn vanilla = this.delegate.getBaseColumn(x, z, heightAccessor, randomState);
      int delegateMinY = this.delegate.getMinY();
      int delegateMaxY = delegateMinY + this.delegate.getGenDepth() - 1;

      for (int i = 0; i < height; i++) {
         int y = minY + i;
         states[i] = y >= delegateMinY && y <= delegateMaxY ? vanilla.getBlock(y) : Blocks.AIR.defaultBlockState();
      }

      this.applyColumnMask(states, minY, x, z, true);
      return new NoiseColumn(minY, states);
   }

   @Override
   public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos, SamplerContext samplerContext) {
      this.delegate.addDebugScreenInfo(lines, randomState, pos, samplerContext);
      lines.add("OMP region: " + OnePieceGeography.regionAt(pos.getX(), pos.getZ()));
      OnePieceGeography.islandAt(this.worldSeed, pos.getX(), pos.getZ()).ifPresent(island -> lines.add("OMP island: " + island.id()));
   }

   private void applyOnePieceMask(ChunkAccess chunk, boolean raiseIslands) {
      ChunkPos chunkPos = chunk.getPos();
      MutableBlockPos pos = new MutableBlockPos();

      for (int localX = 0; localX < 16; localX++) {
         for (int localZ = 0; localZ < 16; localZ++) {
            int x = chunkPos.getMinBlockX() + localX;
            int z = chunkPos.getMinBlockZ() + localZ;
            this.maskColumn(chunk, pos, x, z, raiseIslands);
         }
      }
   }

   private void maskColumn(ChunkAccess chunk, MutableBlockPos pos, int x, int z, boolean raiseIslands) {
      if (FishmanIslandLayout.affectsColumn(x, z)) {
         this.shapeFishmanColumn(chunk, pos, x, z);
      } else if (OnePieceGeography.isReverseMountainChannel(x, z)) {
         this.overwriteColumn(
            chunk, pos, x, z, OnePieceGeography.reverseChannelFloor(this.worldSeed, x, z), OnePieceChunkGenerator.ColumnProfile.REVERSE_CHANNEL
         );
      } else {
         RegionType region = OnePieceGeography.regionAt(x, z);
         if (!OnePieceGeography.isReverseMountainArea(x, z) && region != RegionType.RED_LINE) {
            IslandDefinition island = OnePieceGeography.islandAt(this.worldSeed, x, z).orElse(null);
            if (island == null) {
               this.shapeOceanColumn(chunk, pos, x, z);
            } else if (island.extracted()) {
               this.shapeOceanColumn(chunk, pos, x, z);
            } else if (island.theme() == IslandTheme.ZOU) {
               this.shapeZouColumn(chunk, pos, island, x, z);
            } else if (island.theme() == IslandTheme.SKYPIEA) {
               this.shapeSkypieaColumn(chunk, pos, island, x, z);
            } else if (island.layer() == IslandLayer.SKY) {
               this.shapeSkyIslandColumn(chunk, pos, island, x, z);
            } else {
               if (raiseIslands) {
                  this.shapeIslandColumn(chunk, pos, island, x, z);
               }
            }
         } else {
            this.overwriteColumn(chunk, pos, x, z, OnePieceGeography.terrainHeight(this.worldSeed, x, z), OnePieceChunkGenerator.ColumnProfile.RED_LINE);
         }
      }
   }

   private void overwriteColumn(ChunkAccess chunk, MutableBlockPos pos, int x, int z, int surface, OnePieceChunkGenerator.ColumnProfile profile) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         chunk.setBlockState(pos, this.stateForProfile(profile, x, y, z, surface), 0);
      }
   }

   private void shapeIslandColumn(ChunkAccess chunk, MutableBlockPos pos, IslandDefinition island, int x, int z) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;
      int vanillaSurface = this.highestTerrainBlock(chunk, x, z, false);
      int surface = this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         BlockState current = chunk.getBlockState(pos);
         if (y == minY) {
            chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), 0);
         } else if (y > surface) {
            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), 0);
         } else if (y > vanillaSurface || this.isReplaceableTerrain(current)) {
            chunk.setBlockState(pos, this.islandState(island, x, y, z, surface), 0);
         } else if (this.shouldThemeReplaceSurface(island, y, surface)) {
            chunk.setBlockState(pos, y == surface ? this.islandTopState(island, x, z, surface) : this.islandFillerState(island, x, z, surface), 0);
         }
      }
   }

   private void applyColumnMask(BlockState[] states, int minY, int x, int z, boolean raiseIslands) {
      if (FishmanIslandLayout.affectsColumn(x, z)) {
         this.shapeFishmanColumn(states, minY, x, z);
      } else if (OnePieceGeography.isReverseMountainChannel(x, z)) {
         this.fillColumn(states, minY, x, z, OnePieceGeography.reverseChannelFloor(this.worldSeed, x, z), OnePieceChunkGenerator.ColumnProfile.REVERSE_CHANNEL);
      } else {
         RegionType region = OnePieceGeography.regionAt(x, z);
         if (!OnePieceGeography.isReverseMountainArea(x, z) && region != RegionType.RED_LINE) {
            IslandDefinition island = OnePieceGeography.islandAt(this.worldSeed, x, z).orElse(null);
            if (island == null) {
               this.shapeOceanColumn(states, minY, x, z);
            } else if (island.extracted()) {
               this.shapeOceanColumn(states, minY, x, z);
            } else if (island.theme() == IslandTheme.ZOU) {
               this.shapeZouColumn(states, minY, island, x, z);
            } else if (island.theme() == IslandTheme.SKYPIEA) {
               this.shapeSkypieaColumn(states, minY, island, x, z);
            } else if (island.layer() == IslandLayer.SKY) {
               this.shapeSkyIslandColumn(states, minY, island, x, z);
            } else {
               if (raiseIslands) {
                  this.shapeIslandColumn(states, minY, island, x, z);
               }
            }
         } else {
            this.fillColumn(states, minY, x, z, OnePieceGeography.terrainHeight(this.worldSeed, x, z), OnePieceChunkGenerator.ColumnProfile.RED_LINE);
         }
      }
   }

   private void shapeIslandColumn(BlockState[] states, int minY, IslandDefinition island, int x, int z) {
      int vanillaSurface = this.highestTerrainBlock(states, minY, false);
      int surface = this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);

      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         BlockState current = states[i];
         if (y == minY) {
            states[i] = Blocks.BEDROCK.defaultBlockState();
         } else if (y > surface) {
            states[i] = Blocks.AIR.defaultBlockState();
         } else if (y > vanillaSurface || this.isReplaceableTerrain(current)) {
            states[i] = this.islandState(island, x, y, z, surface);
         } else if (this.shouldThemeReplaceSurface(island, y, surface)) {
            states[i] = y == surface ? this.islandTopState(island, x, z, surface) : this.islandFillerState(island, x, z, surface);
         }
      }
   }

   private void shapeSkypieaColumn(ChunkAccess chunk, MutableBlockPos pos, IslandDefinition island, int x, int z) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;
      int vanillaSurface = this.highestTerrainBlock(chunk, x, z, false);
      int oceanSurface = this.oceanSurfaceHeight(x, z, vanillaSurface);
      int surface = this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);
      int cloudBottom = SkypieaLayout.cloudBottomY(this.worldSeed, island, x, z, surface);
      int streamTop = SkypieaLayout.streamTopY(this.worldSeed);

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         chunk.setBlockState(pos, this.skypieaColumnState(island, x, y, z, minY, oceanSurface, surface, cloudBottom, streamTop), 0);
      }
   }

   private void shapeSkypieaColumn(BlockState[] states, int minY, IslandDefinition island, int x, int z) {
      int vanillaSurface = this.highestTerrainBlock(states, minY, false);
      int oceanSurface = this.oceanSurfaceHeight(x, z, vanillaSurface);
      int surface = this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);
      int cloudBottom = SkypieaLayout.cloudBottomY(this.worldSeed, island, x, z, surface);
      int streamTop = SkypieaLayout.streamTopY(this.worldSeed);

      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         states[i] = this.skypieaColumnState(island, x, y, z, minY, oceanSurface, surface, cloudBottom, streamTop);
      }
   }

   private void shapeSkyIslandColumn(ChunkAccess chunk, MutableBlockPos pos, IslandDefinition island, int x, int z) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;
      int vanillaSurface = this.highestTerrainBlock(chunk, x, z, false);
      int oceanSurface = this.oceanSurfaceHeight(x, z, vanillaSurface);
      int surface = this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);
      int cloudBottom = this.genericSkyCloudBottom(island, x, z, surface);

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         chunk.setBlockState(pos, this.genericSkyColumnState(island, x, y, z, minY, oceanSurface, surface, cloudBottom), 0);
      }
   }

   private void shapeSkyIslandColumn(BlockState[] states, int minY, IslandDefinition island, int x, int z) {
      int vanillaSurface = this.highestTerrainBlock(states, minY, false);
      int oceanSurface = this.oceanSurfaceHeight(x, z, vanillaSurface);
      int surface = this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);
      int cloudBottom = this.genericSkyCloudBottom(island, x, z, surface);

      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         states[i] = this.genericSkyColumnState(island, x, y, z, minY, oceanSurface, surface, cloudBottom);
      }
   }

   private void shapeZouColumn(ChunkAccess chunk, MutableBlockPos pos, IslandDefinition island, int x, int z) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;
      int vanillaSurface = this.highestTerrainBlock(chunk, x, z, false);
      int oceanSurface = this.oceanSurfaceHeight(x, z, vanillaSurface);

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         chunk.setBlockState(pos, this.zouColumnState(island, x, y, z, minY, oceanSurface), 0);
      }
   }

   private void shapeZouColumn(BlockState[] states, int minY, IslandDefinition island, int x, int z) {
      int vanillaSurface = this.highestTerrainBlock(states, minY, false);
      int oceanSurface = this.oceanSurfaceHeight(x, z, vanillaSurface);

      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         states[i] = this.zouColumnState(island, x, y, z, minY, oceanSurface);
      }
   }

   private void shapeOceanColumn(ChunkAccess chunk, MutableBlockPos pos, int x, int z) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;
      int vanillaSurface = this.highestTerrainBlock(chunk, x, z, false);
      int surface = this.oceanSurfaceHeight(x, z, vanillaSurface);

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         BlockState current = chunk.getBlockState(pos);
         if (y == minY) {
            chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), 0);
         } else if (y > 63) {
            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), 0);
         } else if (y > surface) {
            chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), 0);
         } else if (vanillaSurface > surface || this.isReplaceableTerrain(current) || y >= surface - 2) {
            chunk.setBlockState(pos, this.oceanState(x, y, z, surface), 0);
         }
      }
   }

   private void shapeOceanColumn(BlockState[] states, int minY, int x, int z) {
      int vanillaSurface = this.highestTerrainBlock(states, minY, false);
      int surface = this.oceanSurfaceHeight(x, z, vanillaSurface);

      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         BlockState current = states[i];
         if (y == minY) {
            states[i] = Blocks.BEDROCK.defaultBlockState();
         } else if (y > 63) {
            states[i] = Blocks.AIR.defaultBlockState();
         } else if (y > surface) {
            states[i] = Blocks.WATER.defaultBlockState();
         } else if (vanillaSurface > surface || this.isReplaceableTerrain(current) || y >= surface - 2) {
            states[i] = this.oceanState(x, y, z, surface);
         }
      }
   }

   private void shapeFishmanColumn(ChunkAccess chunk, MutableBlockPos pos, int x, int z) {
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;
      int vanillaSurface = this.highestTerrainBlock(chunk, x, z, false);
      int surface = this.oceanSurfaceHeight(x, z, vanillaSurface);
      int redLineSurface = this.fishmanRoutePreservesRedLine(x, z) ? OnePieceGeography.terrainHeight(this.worldSeed, x, z) : surface;

      for (int y = minY; y <= maxY; y++) {
         pos.set(x, y, z);
         BlockState override = FishmanIslandLayout.overrideState(this.worldSeed, x, y, z);
         chunk.setBlockState(pos, override != null ? override : this.fishmanFallbackState(x, y, z, minY, surface, redLineSurface), 0);
      }
   }

   private void shapeFishmanColumn(BlockState[] states, int minY, int x, int z) {
      int vanillaSurface = this.highestTerrainBlock(states, minY, false);
      int surface = this.oceanSurfaceHeight(x, z, vanillaSurface);
      int redLineSurface = this.fishmanRoutePreservesRedLine(x, z) ? OnePieceGeography.terrainHeight(this.worldSeed, x, z) : surface;

      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         BlockState override = FishmanIslandLayout.overrideState(this.worldSeed, x, y, z);
         states[i] = override != null ? override : this.fishmanFallbackState(x, y, z, minY, surface, redLineSurface);
      }
   }

   private BlockState fishmanFallbackState(int x, int y, int z, int minY, int oceanSurface, int redLineSurface) {
      return this.fishmanRoutePreservesRedLine(x, z)
         ? this.stateForProfile(OnePieceChunkGenerator.ColumnProfile.RED_LINE, x, y, z, redLineSurface)
         : this.oceanColumnState(x, y, z, minY, oceanSurface);
   }

   private boolean fishmanRoutePreservesRedLine(int x, int z) {
      return OnePieceGeography.isReverseMountainArea(x, z) || OnePieceGeography.regionAt(x, z) == RegionType.RED_LINE;
   }

   private void clearReverseMountainChannels(ChunkAccess chunk) {
      ChunkPos chunkPos = chunk.getPos();
      int minX = chunkPos.getMinBlockX();
      int minZ = chunkPos.getMinBlockZ();
      int maxX = minX + 15;
      int maxZ = minZ + 15;
      int radius = 9000;
      if (maxX >= -radius && minX <= radius && maxZ >= -radius && minZ <= radius) {
         MutableBlockPos pos = new MutableBlockPos();
         int minY = chunk.getMinY();
         int maxY = minY + chunk.getHeight() - 1;

         for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
               int x = minX + localX;
               int z = minZ + localZ;
               if (OnePieceGeography.isReverseMountainChannel(x, z)) {
                  for (int y = minY; y <= maxY; y++) {
                     pos.set(x, y, z);
                     chunk.removeBlockEntity(pos);
                     chunk.setBlockState(pos, this.reverseChannelState(x, y, z, minY), 0);
                  }
               }
            }
         }
      }
   }

   private BlockState reverseChannelState(int x, int y, int z, int minY) {
      if (y == minY) {
         return Blocks.BEDROCK.defaultBlockState();
      } else {
         int floor = OnePieceGeography.reverseChannelFloor(this.worldSeed, x, z);
         int waterLevel = OnePieceGeography.reverseChannelWaterLevel(this.worldSeed, x, z);
         if (y > waterLevel) {
            return Blocks.AIR.defaultBlockState();
         } else if (y > floor) {
            return Blocks.WATER.defaultBlockState();
         } else {
            return y >= floor - 2 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState();
         }
      }
   }

   private void fillColumn(BlockState[] states, int minY, int x, int z, int surface, OnePieceChunkGenerator.ColumnProfile profile) {
      for (int i = 0; i < states.length; i++) {
         int y = minY + i;
         states[i] = this.stateForProfile(profile, x, y, z, surface);
      }
   }

   private int maskedSurfaceHeight(int x, int z, int vanillaSurface) {
      if (FishmanIslandLayout.affectsColumn(x, z)) {
         return this.fishmanRoutePreservesRedLine(x, z)
            ? OnePieceGeography.terrainHeight(this.worldSeed, x, z)
            : Math.max(63, FishmanIslandLayout.topShellHeight(x, z));
      }

      if (OnePieceGeography.isReverseMountainChannel(x, z)) {
         return OnePieceGeography.reverseChannelWaterLevel(this.worldSeed, x, z);
      }

      RegionType region = OnePieceGeography.regionAt(x, z);
      if (!OnePieceGeography.isReverseMountainArea(x, z) && region != RegionType.RED_LINE) {
         IslandDefinition island = OnePieceGeography.islandAt(this.worldSeed, x, z).orElse(null);
         if (island != null) {
            return island.theme() == IslandTheme.ZOU
               ? ZouLayout.surfaceHeight(this.worldSeed, island, x, z)
               : this.finalIslandSurfaceHeight(island, x, z, vanillaSurface);
         } else {
            return Math.max(this.oceanFloorHeight(x, z), 63);
         }
      } else {
         return OnePieceGeography.terrainHeight(this.worldSeed, x, z);
      }
   }

   private BlockState skypieaColumnState(IslandDefinition island, int x, int y, int z, int minY, int oceanSurface, int surface, int cloudBottom, int streamTop) {
      if (y == minY) {
         return Blocks.BEDROCK.defaultBlockState();
      }

      if (SkypieaLayout.isStreamColumn(island, x, z) && y >= 63 && y <= streamTop) {
         return Blocks.WATER.defaultBlockState();
      }

      if (SkypieaLayout.isStreamHole(island, x, z) && y >= cloudBottom - 6 && y <= Math.max(surface + 8, streamTop)) {
         return Blocks.AIR.defaultBlockState();
      }

      if (SkypieaLayout.isLandingPool(island, x, z)) {
         int poolY = streamTop;
         if (y == poolY - 1) {
            return this.variant(x, z, 4) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState();
         }

         if (y == poolY) {
            return Blocks.WATER.defaultBlockState();
         }

         if (y > poolY && y <= poolY + 4) {
            return Blocks.AIR.defaultBlockState();
         }
      }

      if (y < cloudBottom) {
         return this.oceanColumnState(x, y, z, minY, oceanSurface);
      } else if (y > surface) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return y > cloudBottom + 9 && !(island.distanceRatio(x, z) > 0.82) ? this.islandState(island, x, y, z, surface) : this.skypieaCloudState(x, y, z);
      }
   }

   private int genericSkyCloudBottom(IslandDefinition island, int x, int z, int surface) {
      double inland = Math.max(0.0, 1.0 - island.distanceRatio(x, z));
      return surface - 16 - (int)Math.round(inland * 26.0);
   }

   private BlockState genericSkyColumnState(IslandDefinition island, int x, int y, int z, int minY, int oceanSurface, int surface, int cloudBottom) {
      if (y == minY) {
         return Blocks.BEDROCK.defaultBlockState();
      } else if (y < cloudBottom) {
         return this.oceanColumnState(x, y, z, minY, oceanSurface);
      } else if (y > surface) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return y > cloudBottom + 6 && !(island.distanceRatio(x, z) > 0.82) ? this.islandState(island, x, y, z, surface) : this.skypieaCloudState(x, y, z);
      }
   }

   private BlockState skypieaCloudState(int x, int y, int z) {
      int variant = this.variant(x + y, z, 9);
      if (variant == 0) {
         return Blocks.SEA_LANTERN.defaultBlockState();
      } else if (variant <= 2) {
         return Blocks.CALCITE.defaultBlockState();
      } else {
         return variant <= 5 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.WOOL.white().defaultBlockState();
      }
   }

   private BlockState zouColumnState(IslandDefinition island, int x, int y, int z, int minY, int oceanSurface) {
      if (y == minY) {
         return Blocks.BEDROCK.defaultBlockState();
      }

      if (ZouLayout.isTuskAt(island, x, y, z)) {
         return this.variant(x + y, z, 4) == 0 ? Blocks.QUARTZ_BLOCK.defaultBlockState() : Blocks.BONE_BLOCK.defaultBlockState();
      }

      if (ZouLayout.isBackIsland(island, x, z)) {
         int bodyTop = ZouLayout.bodyTopY(this.worldSeed, island, x, z);
         int surface = ZouLayout.backIslandSurfaceY(this.worldSeed, island, x, z);
         if (y == surface) {
            return this.zouBackTopState(x, z);
         }

         if (y > bodyTop && y < surface) {
            return y >= surface - 4 ? this.zouBackFillerState(x, z) : Blocks.STONE.defaultBlockState();
         }
      }

      int headTop = ZouLayout.headTopY(this.worldSeed, island, x, z);
      if (ZouLayout.isEyeColumn(island, x, z) && y >= headTop - 2 && y <= headTop) {
         return Blocks.CONCRETE.black().defaultBlockState();
      } else {
         return !ZouLayout.isBodyAt(this.worldSeed, island, x, y, z)
               && !ZouLayout.isHeadAt(this.worldSeed, island, x, y, z)
               && !ZouLayout.isEarAt(this.worldSeed, island, x, y, z)
               && !ZouLayout.isTrunkAt(island, x, y, z)
               && !ZouLayout.isTailAt(island, x, y, z)
               && !ZouLayout.isLegAt(this.worldSeed, island, x, y, z)
            ? this.oceanColumnState(x, y, z, minY, oceanSurface)
            : this.zouHideState(x, y, z);
      }
   }

   private BlockState zouBackTopState(int x, int z) {
      int variant = this.variant(x, z, 9);
      if (variant == 0) {
         return Blocks.MOSS_BLOCK.defaultBlockState();
      } else {
         return variant <= 2 ? Blocks.PODZOL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
      }
   }

   private BlockState zouBackFillerState(int x, int z) {
      int variant = this.variant(x, z, 7);
      if (variant == 0) {
         return Blocks.ROOTED_DIRT.defaultBlockState();
      } else {
         return variant == 1 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState();
      }
   }

   private BlockState zouHideState(int x, int y, int z) {
      int band = Math.floorMod(Math.floorDiv(x + z + y * 3, 19), 6);
      if (y < 45) {
         return band == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
      }

      return switch (band) {
         case 0 -> Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState();
         case 1 -> Blocks.CONCRETE.gray().defaultBlockState();
         case 2 -> Blocks.DIORITE.defaultBlockState();
         default -> Blocks.DYED_TERRACOTTA.gray().defaultBlockState();
      };
   }

   private BlockState islandState(IslandDefinition island, int x, int y, int z, int surface) {
      if (y == this.getMinY()) {
         return Blocks.BEDROCK.defaultBlockState();
      }

      if (y > surface) {
         return Blocks.AIR.defaultBlockState();
      }

      if (y == surface) {
         return this.islandTopState(island, x, z, surface);
      }

      BlockState loreInterior = this.loreInteriorState(island, x, y, z, surface);
      if (loreInterior != null) {
         return loreInterior;
      }

      if (island.theme() == IslandTheme.BALTIGO) {
         BlockState interior = this.baltigoInteriorState(island, x, y, z, surface);
         if (interior != null) {
            return interior;
         }
      }

      if (y >= surface - 10 && this.isSteepSurface(island, x, z, surface)) {
         return this.exposedRockState(island, x, z, surface);
      } else if (y >= surface - 4) {
         return this.islandFillerState(island, x, z, surface);
      } else if (y < 0) {
         return Blocks.DEEPSLATE.defaultBlockState();
      } else if (island.theme() == IslandTheme.ALABASTA && y >= surface - 12) {
         return Blocks.SANDSTONE.defaultBlockState();
      } else if (island.theme() == IslandTheme.PUNK_HAZARD && x >= island.centerX() && y >= surface - 14) {
         return Blocks.BASALT.defaultBlockState();
      } else if (island.theme() == IslandTheme.NETHER_GATEWAY && y >= surface - 18) {
         return this.variant(x + y, z, 4) == 0 ? Blocks.BASALT.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
      } else if (island.theme() == IslandTheme.END_GATEWAY && y >= surface - 18) {
         return this.variant(x + y, z, 5) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
      } else {
         return Blocks.STONE.defaultBlockState();
      }
   }

   private BlockState islandTopState(IslandDefinition island, int x, int z, int surface) {
      BlockState loreTop = this.loreTopState(island, x, z, surface);
      if (loreTop != null) {
         return loreTop;
      }

      if (this.isBeach(island, x, z)) {
         return island.theme() == IslandTheme.ALABASTA ? Blocks.RED_SAND.defaultBlockState() : Blocks.SAND.defaultBlockState();
      }

      if (this.isSteepSurface(island, x, z, surface)) {
         return this.exposedRockState(island, x, z, surface);
      }

      return switch (island.theme()) {
         case ALABASTA -> this.alabastaTopState(x, z);
         case DRUM -> surface > 137 ? (this.variant(x, z, 5) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState()) : Blocks.GRASS_BLOCK.defaultBlockState();
         case PUNK_HAZARD -> x < island.centerX() ? this.punkHazardIceTopState(x, z, surface) : Blocks.BLACKSTONE.defaultBlockState();
         case BALTIGO -> this.variant(x, z, 5) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case MARINEFORD -> Blocks.STONE.defaultBlockState();
         case SABAODY -> this.variant(x, z, 5) == 0 ? Blocks.MUD.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         case SKYPIEA -> this.variant(x, z, 6) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         case ZOU -> this.zouBackTopState(x, z);
         case WANO -> this.variant(x, z, 7) == 0
            ? Blocks.PODZOL.defaultBlockState()
            : (this.variant(x + 13, z, 9) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
         case WHOLE_CAKE -> this.variant(x, z, 8) == 0 ? Blocks.DYED_TERRACOTTA.pink().defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         case NETHER_GATEWAY -> this.variant(x, z, 6) == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
         case END_GATEWAY -> this.variant(x, z, 8) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
         default -> Blocks.GRASS_BLOCK.defaultBlockState();
      };
   }

   private BlockState islandFillerState(IslandDefinition island, int x, int z, int surface) {
      BlockState loreFiller = this.loreFillerState(island, x, z, surface);
      if (loreFiller != null) {
         return loreFiller;
      }

      if (this.isBeach(island, x, z)) {
         return Blocks.SANDSTONE.defaultBlockState();
      }

      if (this.isSteepSurface(island, x, z, surface)) {
         return this.exposedRockState(island, x, z, surface);
      }

      return switch (island.theme()) {
         case ALABASTA -> this.alabastaFillerState(x, z);
         case DRUM -> Blocks.DIRT.defaultBlockState();
         case PUNK_HAZARD -> x < island.centerX()
            ? (this.variant(x, z, 4) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.DIRT.defaultBlockState())
            : Blocks.BASALT.defaultBlockState();
         case BALTIGO -> this.variant(x, z, 5) == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case MARINEFORD -> Blocks.STONE.defaultBlockState();
         case SABAODY -> this.variant(x, z, 4) == 0 ? Blocks.MUD.defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case SKYPIEA -> this.variant(x, z, 5) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case ZOU -> this.zouBackFillerState(x, z);
         case WANO -> this.variant(x, z, 5) == 0 ? Blocks.TUFF.defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case WHOLE_CAKE -> this.variant(x, z, 6) == 0 ? Blocks.DYED_TERRACOTTA.white().defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case NETHER_GATEWAY -> this.variant(x, z, 4) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
         case END_GATEWAY -> Blocks.END_STONE.defaultBlockState();
         default -> Blocks.DIRT.defaultBlockState();
      };
   }

   private BlockState baltigoInteriorState(IslandDefinition island, int x, int y, int z, int surface) {
      if (y > 75 && y < surface - 14) {
         int localX = x - island.centerX();
         int localZ = z - island.centerZ();
         boolean externalPassage = this.baltigoExternalPassage(localX, localZ, island.radius());
         if (this.isBeach(island, x, z) && !externalPassage) {
            return null;
         }

         double ratio = island.distanceRatio(x, z);
         double maxRatio = externalPassage ? 0.96 : 0.72;
         if (ratio > maxRatio) {
            return null;
         }

         double core = ratio < 0.56 ? 1.0 : 0.0;
         boolean crossBridge = Math.abs(localX) <= 15 && Math.abs(localZ) <= island.radius() * 0.58
            || Math.abs(localZ) <= 15 && Math.abs(localX) <= island.radius() * 0.58;
         boolean ringPassage = this.baltigoRingPassage(localX, localZ, island.radius());
         boolean verticalConnector = this.baltigoVerticalConnector(localX, localZ, island.radius());
         boolean sideRoom = this.hollowRoom(localX, localZ, island.radius(), 0.32, 0.18) || this.hollowRoom(localZ, localX, island.radius(), 0.34, 0.2);
         boolean openPlan = core > 0.0 || crossBridge || sideRoom || externalPassage || ringPassage || verticalConnector;
         if (!openPlan) {
            return null;
         }

         int[] levels = new int[]{95, 121, 147, 173};
         if (verticalConnector && y > levels[0] - 1 && y < levels[levels.length - 1] + 17) {
            return Blocks.AIR.defaultBlockState();
         }

         for (int level : levels) {
            int dy = y - level;
            if (Math.abs(dy) <= 1) {
               return crossBridge && Math.abs(dy) == 1 ? Blocks.SMOOTH_STONE.defaultBlockState() : this.baltigoFloorState(x, z);
            }

            if (dy > 1 && dy <= 16) {
               return Blocks.AIR.defaultBlockState();
            }
         }

         return ratio < 0.16 && y > levels[0] && y < levels[levels.length - 1] + 16 ? Blocks.AIR.defaultBlockState() : null;
      } else {
         return null;
      }
   }

   private boolean baltigoExternalPassage(int localX, int localZ, int radius) {
      int reach = (int)Math.round(radius * 0.93);
      return Math.abs(localZ) <= 18 && Math.abs(localX) <= reach || Math.abs(localX) <= 18 && Math.abs(localZ) <= reach;
   }

   private boolean baltigoRingPassage(int localX, int localZ, int radius) {
      int ring = (int)Math.round(radius * 0.31);
      return Math.abs(Math.abs(localX) - ring) <= 12 && Math.abs(localZ) <= ring || Math.abs(Math.abs(localZ) - ring) <= 12 && Math.abs(localX) <= ring;
   }

   private boolean baltigoVerticalConnector(int localX, int localZ, int radius) {
      int offset = (int)Math.round(radius * 0.13);
      return Math.abs(Math.abs(localX) - offset) <= 4 && Math.abs(Math.abs(localZ) - offset) <= 4;
   }

   private boolean hollowRoom(int along, int across, int radius, double centerRatio, double widthRatio) {
      double center = radius * centerRatio;
      double length = radius * 0.18;
      double width = radius * widthRatio;
      return Math.abs(Math.abs(along) - center) <= length && Math.abs(across) <= width;
   }

   private BlockState baltigoFloorState(int x, int z) {
      int variant = this.variant(x, z, 9);
      if (variant == 0) {
         return Blocks.SEA_LANTERN.defaultBlockState();
      } else {
         return variant <= 2 ? Blocks.DIORITE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
      }
   }

   private boolean isBeach(IslandDefinition island, int x, int z) {
      if (island.theme() == IslandTheme.SKYPIEA) {
         return false;
      } else {
         return this.loreIslandHasNoBeach(island) ? false : island.distanceRatio(x, z) > 0.86;
      }
   }

   private boolean isSteepSurface(IslandDefinition island, int x, int z, int surface) {
      if (this.isBeach(island, x, z)) {
         return false;
      }

      int step = 6;
      int east = this.targetIslandSurfaceHeight(island, x + step, z);
      int west = this.targetIslandSurfaceHeight(island, x - step, z);
      int south = this.targetIslandSurfaceHeight(island, x, z + step);
      int north = this.targetIslandSurfaceHeight(island, x, z - step);
      int maxDelta = Math.max(Math.max(Math.abs(surface - east), Math.abs(surface - west)), Math.max(Math.abs(surface - south), Math.abs(surface - north)));
      return maxDelta >= 5 || surface >= 155 && this.variant(x, z, 4) == 0;
   }

   private BlockState exposedRockState(IslandDefinition island, int x, int z, int surface) {
      BlockState loreRock = this.loreRockState(island, x, z, surface);
      if (loreRock != null) {
         return loreRock;
      }

      return switch (island.theme()) {
         case ALABASTA -> this.alabastaRockState(x, z);
         case DRUM -> surface > 149 ? (this.variant(x, z, 4) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.STONE.defaultBlockState()) : Blocks.STONE.defaultBlockState();
         case PUNK_HAZARD -> x < island.centerX()
            ? (this.variant(x, z, 3) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.STONE.defaultBlockState())
            : (this.variant(x, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState());
         case BALTIGO -> this.variant(x, z, 4) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.ANDESITE.defaultBlockState();
         case MARINEFORD, MARIEJOIS -> Blocks.STONE_BRICKS.defaultBlockState();
         case SABAODY -> this.variant(x, z, 4) == 0 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case SKYPIEA -> this.variant(x, z, 5) == 0 ? Blocks.CALCITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case ZOU -> this.variant(x, z, 4) == 0 ? Blocks.DIORITE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
         case WANO -> this.variant(x, z, 4) == 0 ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case WHOLE_CAKE -> this.variant(x, z, 5) == 0 ? Blocks.DYED_TERRACOTTA.pink().defaultBlockState() : Blocks.CALCITE.defaultBlockState();
         case NETHER_GATEWAY -> this.variant(x, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
         case END_GATEWAY -> this.variant(x, z, 4) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
         default -> this.variant(x, z, 5) == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
      };
   }

   private BlockState punkHazardIceTopState(int x, int z, int surface) {
      if (surface >= 159) {
         return this.variant(x, z, 4) == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
      } else if (surface >= 133) {
         return this.variant(x, z, 3) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
      } else {
         return this.variant(x, z, 6) == 0 ? Blocks.ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
      }
   }

   private BlockState alabastaTopState(int x, int z) {
      int band = Math.floorMod(Math.floorDiv(x + z, 96), 5);
      if (band == 0) {
         return Blocks.RED_SAND.defaultBlockState();
      } else {
         return this.variant(x, z, 11) == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.SAND.defaultBlockState();
      }
   }

   private BlockState alabastaFillerState(int x, int z) {
      int band = Math.floorMod(Math.floorDiv(x - z, 128), 6);
      if (band == 0 || band == 1) {
         return Blocks.RED_SANDSTONE.defaultBlockState();
      } else {
         return this.variant(x, z, 7) == 0 ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
      }
   }

   private BlockState alabastaRockState(int x, int z) {
      int band = Math.floorMod(Math.floorDiv(x + z, 72), 6);

      return switch (band) {
         case 0 -> Blocks.TERRACOTTA.defaultBlockState();
         case 1 -> Blocks.DYED_TERRACOTTA.orange().defaultBlockState();
         case 2 -> Blocks.RED_SANDSTONE.defaultBlockState();
         default -> Blocks.SANDSTONE.defaultBlockState();
      };
   }

   private BlockState loreInteriorState(IslandDefinition island, int x, int y, int z, int surface) {
      if (y < surface - 18) {
         return null;
      }

      return switch (island.id()) {
         case "tequila_wolf", "water_7", "enies_lobby" -> y >= surface - 8 ? Blocks.STONE_BRICKS.defaultBlockState() : null;
         case "baratie" -> y >= surface - 5 && this.baratieDeck(island, x, z) ? Blocks.OAK_PLANKS.defaultBlockState() : null;
         case "thriller_bark", "hachinosu" -> y >= surface - 12
            ? (this.variant(x + y, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState())
            : null;
         case "egghead" -> y >= surface - 10 ? (this.variant(x + y, z, 4) == 0 ? Blocks.QUARTZ_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState()) : null;
         case "end_gateway" -> y >= surface - 12 ? (this.variant(x + y, z, 5) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState()) : null;
         default -> null;
      };
   }

   private BlockState loreTopState(IslandDefinition island, int x, int z, int surface) {
      double ratio = island.distanceRatio(x, z);

      return switch (island.id()) {
         case "baratie" -> this.baratieDeck(island, x, z) ? Blocks.OAK_PLANKS.defaultBlockState() : Blocks.SAND.defaultBlockState();
         case "tequila_wolf" -> this.tequilaWolfBridge(island, x, z)
            ? Blocks.STONE_BRICKS.defaultBlockState()
            : (this.variant(x, z, 5) == 0 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState());
         case "cactus_island" -> ratio > 0.86
            ? Blocks.RED_SAND.defaultBlockState()
            : (this.variant(x, z, 9) == 0 ? Blocks.TERRACOTTA.defaultBlockState() : Blocks.SAND.defaultBlockState());
         case "little_garden", "jaya" -> surface > 169 && this.variant(x, z, 4) == 0
            ? Blocks.BASALT.defaultBlockState()
            : (this.variant(x, z, 5) == 0 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
         case "drum" -> surface > 149 ? (this.variant(x, z, 4) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState()) : Blocks.SNOW_BLOCK.defaultBlockState();
         case "alabasta" -> this.alabastaRiver(island, x, z) > 0.72 ? Blocks.WATER.defaultBlockState() : this.alabastaTopState(x, z);
         case "long_ring_long_land" -> this.variant(x, z, 7) == 0 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         case "water_7" -> this.waterSevenCanal(island, x, z)
            ? Blocks.WATER.defaultBlockState()
            : (ratio < 0.28 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
         case "enies_lobby" -> ratio < 0.42 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
         case "thriller_bark" -> this.variant(x, z, 5) == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
         case "amazon_lily", "green_bit", "elbaph" -> this.variant(x, z, 5) == 0
            ? Blocks.ROOTED_DIRT.defaultBlockState()
            : (this.variant(x + 11, z, 7) == 0 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
         case "kuraigana" -> this.variant(x, z, 4) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "dressrosa" -> this.variant(x, z, 6) == 0 ? Blocks.DYED_TERRACOTTA.pink().defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
         case "whole_cake" -> this.wholeCakeTopState(x, z);
         case "wano" -> this.variant(x, z, 7) == 0
            ? Blocks.PODZOL.defaultBlockState()
            : (this.variant(x + 13, z, 9) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
         case "egghead" -> ratio < 0.4 ? Blocks.QUARTZ_BLOCK.defaultBlockState() : (this.variant(x, z, 5) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState());
         case "hachinosu" -> this.variant(x, z, 4) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
         case "lodestar" -> this.variant(x, z, 4) == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "end_gateway" -> this.variant(x, z, 6) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
         default -> null;
      };
   }

   private BlockState loreFillerState(IslandDefinition island, int x, int z, int surface) {
      return switch (island.id()) {
         case "baratie" -> this.baratieDeck(island, x, z) ? Blocks.OAK_PLANKS.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
         case "tequila_wolf", "water_7", "enies_lobby" -> Blocks.STONE_BRICKS.defaultBlockState();
         case "cactus_island", "alabasta" -> this.alabastaFillerState(x, z);
         case "drum", "egghead" -> this.variant(x, z, 4) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
         case "little_garden", "jaya", "amazon_lily", "green_bit", "elbaph" -> this.variant(x, z, 5) == 0
            ? Blocks.ROOTED_DIRT.defaultBlockState()
            : Blocks.DIRT.defaultBlockState();
         case "long_ring_long_land", "dressrosa", "wano" -> Blocks.DIRT.defaultBlockState();
         case "thriller_bark", "kuraigana" -> this.variant(x, z, 5) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "whole_cake" -> this.variant(x, z, 5) == 0 ? Blocks.DYED_TERRACOTTA.white().defaultBlockState() : Blocks.TERRACOTTA.defaultBlockState();
         case "hachinosu" -> this.variant(x, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
         case "lodestar" -> this.variant(x, z, 4) == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "end_gateway" -> Blocks.END_STONE.defaultBlockState();
         default -> null;
      };
   }

   private BlockState loreRockState(IslandDefinition island, int x, int z, int surface) {
      return switch (island.id()) {
         case "tequila_wolf", "water_7", "enies_lobby", "lodestar" -> Blocks.STONE_BRICKS.defaultBlockState();
         case "cactus_island", "alabasta" -> this.alabastaRockState(x, z);
         case "drum", "egghead" -> surface > 153 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "little_garden", "jaya", "amazon_lily", "green_bit", "elbaph" -> this.variant(x, z, 5) == 0
            ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
            : Blocks.STONE.defaultBlockState();
         case "thriller_bark", "hachinosu" -> this.variant(x, z, 4) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
         case "kuraigana" -> this.variant(x, z, 4) == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState();
         case "dressrosa" -> this.variant(x, z, 4) == 0 ? Blocks.DYED_TERRACOTTA.pink().defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "whole_cake" -> this.variant(x, z, 5) == 0 ? Blocks.DYED_TERRACOTTA.pink().defaultBlockState() : Blocks.CALCITE.defaultBlockState();
         case "wano" -> this.variant(x, z, 4) == 0 ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState();
         case "end_gateway" -> this.variant(x, z, 4) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
         default -> null;
      };
   }

   private boolean loreIslandHasNoBeach(IslandDefinition island) {
      return switch (island.id()) {
         case "baratie", "tequila_wolf", "water_7", "enies_lobby", "thriller_bark", "end_gateway" -> true;
         default -> false;
      };
   }

   private BlockState wholeCakeTopState(int x, int z) {
      return switch (this.variant(x, z, 9)) {
         case 0 -> Blocks.DYED_TERRACOTTA.pink().defaultBlockState();
         case 1 -> Blocks.DYED_TERRACOTTA.white().defaultBlockState();
         case 2 -> Blocks.DYED_TERRACOTTA.yellow().defaultBlockState();
         case 3 -> Blocks.DYED_TERRACOTTA.lightBlue().defaultBlockState();
         default -> Blocks.GRASS_BLOCK.defaultBlockState();
      };
   }

   private boolean baratieDeck(IslandDefinition island, int x, int z) {
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();
      return Math.abs(lz) <= 0.13 && Math.abs(lx) <= 0.8 || lx * lx / 0.2 + lz * lz / 0.1 <= 1.0;
   }

   private boolean tequilaWolfBridge(IslandDefinition island, int x, int z) {
      double lx = (double)(x - island.centerX()) / island.radius();
      double lz = (double)(z - island.centerZ()) / island.radius();
      return Math.abs(lz) <= 0.055 && Math.abs(lx) <= 0.96;
   }

   private boolean waterSevenCanal(IslandDefinition island, int x, int z) {
      double dx = (double)(x - island.centerX()) / island.radius();
      double dz = (double)(z - island.centerZ()) / island.radius();
      if (!(Math.hypot(dx, dz) < 0.18) && !(Math.hypot(dx, dz) > 0.86)) {
         for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3.0;
            double cross = Math.abs(dx * Math.sin(angle) - dz * Math.cos(angle));
            double along = Math.abs(dx * Math.cos(angle) + dz * Math.sin(angle));
            if (cross < 0.032 && along < 0.82) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private double alabastaRiver(IslandDefinition island, int x, int z) {
      double localX = x - island.centerX();
      double riverZ = island.centerZ() + Math.sin(localX / 190.0) * 58.0;
      double width = island.radius() * 0.035;
      return Math.exp(-Math.pow((z - riverZ) / width, 2.0)) * Math.max(0.0, Math.min(1.0, 1.0 - Math.abs(localX) / (island.radius() * 0.88)));
   }

   private BlockState stateForProfile(OnePieceChunkGenerator.ColumnProfile profile, int x, int y, int z, int surface) {
      if (y == this.getMinY()) {
         return Blocks.BEDROCK.defaultBlockState();
      }

      if (profile == OnePieceChunkGenerator.ColumnProfile.OCEAN) {
         if (y > 63) {
            return Blocks.AIR.defaultBlockState();
         } else if (y > surface) {
            return Blocks.WATER.defaultBlockState();
         } else if (y >= surface - 2) {
            return this.oceanState(x, y, z, surface);
         } else {
            return y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
         }
      } else if (profile == OnePieceChunkGenerator.ColumnProfile.REVERSE_CHANNEL) {
         int waterLevel = OnePieceGeography.reverseChannelWaterLevel(this.worldSeed, x, z);
         if (y > waterLevel) {
            return Blocks.AIR.defaultBlockState();
         } else if (y > surface) {
            return Blocks.WATER.defaultBlockState();
         } else {
            return y >= surface - 2 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState();
         }
      } else {
         return y > surface ? Blocks.AIR.defaultBlockState() : this.redLineState(y, surface, x, z);
      }
   }

   private BlockState redLineState(int y, int surface, int x, int z) {
      if (y == surface) {
         return this.variant(x, z, 5) == 0 ? Blocks.RED_SAND.defaultBlockState() : Blocks.DYED_TERRACOTTA.red().defaultBlockState();
      } else if (y >= surface - 5) {
         return this.variant(x + y, z, 5) == 0 ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.DYED_TERRACOTTA.red().defaultBlockState();
      } else if (y < 18) {
         return Blocks.DEEPSLATE.defaultBlockState();
      } else if (y < 55) {
         return this.variant(x, z, 5) == 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
      } else if (y >= surface - 12) {
         return this.variant(x, z, 3) == 0 ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.DYED_TERRACOTTA.red().defaultBlockState();
      } else {
         return this.variant(x, z, 4) == 0 ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.DYED_TERRACOTTA.red().defaultBlockState();
      }
   }

   private int oceanFloorHeight(int x, int z) {
      int base = OnePieceGeography.regionAt(x, z) == RegionType.GRAND_LINE ? 38 : 41;
      if (OnePieceGeography.isCalmBelt(x, z)) {
         base = 34;
      }

      return Math.min(58, base + OnePieceGeography.noise(this.worldSeed + 9001L, x, z, 12));
   }

   private int targetIslandSurfaceHeight(IslandDefinition island, int x, int z) {
      return OnePieceGeography.islandSurfaceHeight(this.worldSeed, island, x, z);
   }

   private int finalIslandSurfaceHeight(IslandDefinition island, int x, int z, int vanillaSurface) {
      int target = this.targetIslandSurfaceHeight(island, x, z);
      if (this.isLoreSurfaceIsland(island)) {
         return target;
      } else if (vanillaSurface <= this.getMinY()) {
         return target;
      } else {
         return vanillaSurface <= 63 ? Math.max(target, 65) : Math.max(vanillaSurface, target);
      }
   }

   private int oceanSurfaceHeight(int x, int z, int vanillaSurface) {
      int maxSurface = this.oceanFloorHeight(x, z);
      int minSurface = this.minimumOceanFloorHeight(x, z);
      return vanillaSurface <= this.getMinY() ? maxSurface : Math.min(Math.max(vanillaSurface, minSurface), maxSurface);
   }

   private int minimumOceanFloorHeight(int x, int z) {
      if (OnePieceGeography.isCalmBelt(x, z)) {
         return 23;
      } else {
         return OnePieceGeography.regionAt(x, z) == RegionType.GRAND_LINE ? 29 : 33;
      }
   }

   private BlockState oceanState(int x, int y, int z, int surface) {
      if (y >= surface - 1) {
         if (OnePieceGeography.isCalmBelt(x, z)) {
            return this.variant(x, z, 4) == 0 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
         } else {
            return this.variant(x, z, 5) == 0 ? Blocks.GRAVEL.defaultBlockState() : Blocks.SAND.defaultBlockState();
         }
      } else {
         return y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
      }
   }

   private BlockState oceanColumnState(int x, int y, int z, int minY, int surface) {
      if (y == minY) {
         return Blocks.BEDROCK.defaultBlockState();
      } else if (y > 63) {
         return Blocks.AIR.defaultBlockState();
      } else if (y > surface) {
         return Blocks.WATER.defaultBlockState();
      } else if (y >= surface - 2) {
         return this.oceanState(x, y, z, surface);
      } else {
         return y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
      }
   }

   private void applyIslandThemeSurface(ChunkAccess chunk) {
      ChunkPos chunkPos = chunk.getPos();
      MutableBlockPos pos = new MutableBlockPos();

      for (int localX = 0; localX < 16; localX++) {
         for (int localZ = 0; localZ < 16; localZ++) {
            int x = chunkPos.getMinBlockX() + localX;
            int z = chunkPos.getMinBlockZ() + localZ;
            IslandDefinition island = OnePieceGeography.islandAt(this.worldSeed, x, z).orElse(null);
            if (island != null) {
               int surface = this.highestTerrainBlock(chunk, x, z, false);
               if (surface > 63) {
                  if (island.theme() == IslandTheme.PUNK_HAZARD) {
                     this.applyPunkHazardSurface(chunk, pos, island, x, z, surface);
                  } else if (island.theme() == IslandTheme.NETHER_GATEWAY) {
                     this.applyNetherGatewaySurface(chunk, pos, island, x, z, surface);
                  } else if (island.theme() == IslandTheme.END_GATEWAY) {
                     this.applyEndGatewaySurface(chunk, pos, x, z, surface);
                  }
               }
            }
         }
      }
   }

   private void applyPunkHazardSurface(ChunkAccess chunk, MutableBlockPos pos, IslandDefinition island, int x, int z, int surface) {
      boolean hotSide = x >= island.centerX();
      double interior = 1.0 - island.distanceRatio(x, z);
      pos.set(x, surface, z);
      if (hotSide) {
         if (interior > 0.18 && this.variant(x, z, 41) == 0) {
            chunk.setBlockState(pos, Blocks.LAVA.defaultBlockState(), 0);
            pos.set(x, surface - 1, z);
            chunk.setBlockState(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 0);
            return;
         }

         chunk.setBlockState(pos, this.variant(x, z, 5) == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState(), 0);

         for (int y = surface - 1; y >= surface - 4; y--) {
            pos.set(x, y, z);
            chunk.setBlockState(pos, this.variant(x + y, z, 3) == 0 ? Blocks.NETHERRACK.defaultBlockState() : Blocks.BASALT.defaultBlockState(), 0);
         }
      } else {
         chunk.setBlockState(pos, this.punkHazardIceTopState(x, z, surface), 0);

         for (int y = surface - 1; y >= surface - 4; y--) {
            pos.set(x, y, z);
            BlockState state = surface >= 141
               ? (this.variant(x + y, z, 5) == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState())
               : (this.variant(x + y, z, 4) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.DIRT.defaultBlockState());
            chunk.setBlockState(pos, state, 0);
         }

         int spikeHeight = this.punkHazardIceSpikeHeight(island, x, z, surface);

         for (int y = 1; y <= spikeHeight; y++) {
            pos.set(x, surface + y, z);
            chunk.setBlockState(pos, y > spikeHeight - 3 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState(), 0);
         }
      }
   }

   private int punkHazardIceSpikeHeight(IslandDefinition island, int x, int z, int surface) {
      if (x < island.centerX() && surface >= 149 && !(island.distanceRatio(x, z) > 0.76)) {
         int cluster = this.variant(Math.floorDiv(x, 9), Math.floorDiv(z, 9), 37);
         if (cluster <= 2 && this.variant(x, z, 5) <= 1) {
            int mountainBonus = Math.max(0, surface - 159) / 4;
            return Math.min(30, 8 + mountainBonus + this.variant(x + surface, z, 14));
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private void applyNetherGatewaySurface(ChunkAccess chunk, MutableBlockPos pos, IslandDefinition island, int x, int z, int surface) {
      double interior = 1.0 - island.distanceRatio(x, z);
      pos.set(x, surface, z);
      if (interior > 0.24 && this.variant(x, z, 29) == 0) {
         chunk.setBlockState(pos, Blocks.LAVA.defaultBlockState(), 0);
         pos.set(x, surface - 1, z);
         chunk.setBlockState(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 0);
      } else {
         chunk.setBlockState(pos, this.variant(x, z, 4) == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState(), 0);

         for (int y = surface - 1; y >= surface - 5; y--) {
            pos.set(x, y, z);
            chunk.setBlockState(pos, this.variant(x + y, z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState(), 0);
         }
      }
   }

   private void applyEndGatewaySurface(ChunkAccess chunk, MutableBlockPos pos, int x, int z, int surface) {
      pos.set(x, surface, z);
      chunk.setBlockState(pos, this.variant(x, z, 9) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState(), 0);

      for (int y = surface - 1; y >= surface - 4; y--) {
         pos.set(x, y, z);
         chunk.setBlockState(pos, this.variant(x + y, z, 5) == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState(), 0);
      }
   }

   private boolean shouldThemeReplaceSurface(IslandDefinition island, int y, int surface) {
      if (y < surface - 4) {
         return false;
      }

      if (this.isLoreSurfaceIsland(island)) {
         return true;
      }

      return switch (island.theme()) {
         case ALABASTA, DRUM, PUNK_HAZARD, MARINEFORD, NETHER_GATEWAY, END_GATEWAY -> true;
         default -> false;
      };
   }

   private boolean isLoreSurfaceIsland(IslandDefinition island) {
      return switch (island.id()) {
         case "baratie", "tequila_wolf", "cactus_island", "little_garden", "drum", "alabasta", "jaya", "long_ring_long_land", "water_7", "enies_lobby", "thriller_bark", "amazon_lily", "kuraigana", "dressrosa", "green_bit", "whole_cake", "wano", "egghead", "hachinosu", "elbaph", "lodestar", "end_gateway" -> true;
         default -> false;
      };
   }

   private int highestTerrainBlock(ChunkAccess chunk, int x, int z, boolean includeWater) {
      MutableBlockPos pos = new MutableBlockPos();
      int minY = chunk.getMinY();
      int maxY = minY + chunk.getHeight() - 1;

      for (int y = maxY; y >= minY; y--) {
         pos.set(x, y, z);
         BlockState state = chunk.getBlockState(pos);
         if (!this.isAir(state) && (includeWater || !this.isWater(state))) {
            return y;
         }
      }

      return minY;
   }

   private int highestTerrainBlock(BlockState[] states, int minY, boolean includeWater) {
      for (int i = states.length - 1; i >= 0; i--) {
         BlockState state = states[i];
         if (!this.isAir(state) && (includeWater || !this.isWater(state))) {
            return minY + i;
         }
      }

      return minY;
   }

   private boolean isReplaceableTerrain(BlockState state) {
      return this.isAir(state) || this.isWater(state) || state.getBlock() == Blocks.LAVA;
   }

   private boolean isAir(BlockState state) {
      return state.isAir();
   }

   private boolean isWater(BlockState state) {
      return state.getFluidState().is(FluidTags.WATER);
   }

   private boolean reinforceProceduralIslandTerrain(ChunkAccess chunk) {
      ChunkPos chunkPos = chunk.getPos();
      MutableBlockPos pos = new MutableBlockPos();
      boolean changed = false;

      for (int localX = 0; localX < 16; localX++) {
         for (int localZ = 0; localZ < 16; localZ++) {
            int x = chunkPos.getMinBlockX() + localX;
            int z = chunkPos.getMinBlockZ() + localZ;
            IslandDefinition island = OnePieceGeography.islandAt(this.worldSeed, x, z).orElse(null);
            if (island != null && !island.extracted()) {
               this.shapeIslandColumn(chunk, pos, island, x, z);
               changed = true;
            }
         }
      }

      return changed;
   }

   private void clearHighOceanWater(ChunkAccess chunk) {
      ChunkPos chunkPos = chunk.getPos();
      MutableBlockPos pos = new MutableBlockPos();
      int maxY = Math.min(chunk.getMinY() + chunk.getHeight() - 1, 95);

      for (int localX = 0; localX < 16; localX++) {
         for (int localZ = 0; localZ < 16; localZ++) {
            int x = chunkPos.getMinBlockX() + localX;
            int z = chunkPos.getMinBlockZ() + localZ;
            pos.set(x, 63, z);
            if (chunk.getBlockState(pos).getBlock() == Blocks.WATER) {
               pos.set(x, 64, z);
               if (chunk.getBlockState(pos).getBlock() == Blocks.WATER && this.isCanonicalOceanColumn(x, z)) {
                  for (int y = 64; y <= maxY; y++) {
                     pos.set(x, y, z);
                     if (chunk.getBlockState(pos).getBlock() == Blocks.WATER) {
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), 0);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isCanonicalOceanColumn(int x, int z) {
      return OnePieceGeography.isCanonicalOceanColumn(this.worldSeed, x, z);
   }

   private int variant(int x, int z, int modulo) {
      return Math.floorMod(x * 73428767 ^ z * 91293481, modulo);
   }

   private enum ColumnProfile {
      OCEAN,
      RED_LINE,
      REVERSE_CHANNEL;
   }
}
