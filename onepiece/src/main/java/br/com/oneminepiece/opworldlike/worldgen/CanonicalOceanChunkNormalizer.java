package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Port Fabric 26.3 de CanonicalOceanChunkNormalizer (Forge 1.20.1).
 *
 * À la charge d'un chunk, rabote les hautes colonnes d'eau « océan canonique » (y 64-95)
 * pour lisser le niveau de l'eau hors des îles extraites. ChunkEvent.Load → ServerChunkEvents.CHUNK_LOAD.
 */
public final class CanonicalOceanChunkNormalizer {
   private static final int MAX_NORMALIZED_Y = 95;
   private static final AtomicLong NORMALIZED_CHUNKS = new AtomicLong();

   private CanonicalOceanChunkNormalizer() {
   }

   public static void register() {
      ServerChunkEvents.CHUNK_LOAD.register(CanonicalOceanChunkNormalizer::onChunkLoad);
   }

   private static void onChunkLoad(ServerLevel level, LevelChunk chunk, boolean newChunk) {
      if (level.dimension() == Level.OVERWORLD
         && level.getChunkSource().getGenerator() instanceof OnePieceChunkGenerator
         && ExtractedIslandChunkIndex.available()
         && !ExtractedIslandChunkIndex.contains(chunk.getPos())) {
         normalize(level, chunk);
      }
   }

   private static void normalize(ServerLevel level, LevelChunk chunk) {
      MutableBlockPos pos = new MutableBlockPos();
      int changedBlocks = 0;
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();
      int maxY = Math.min(chunk.getMaxY() - 1, MAX_NORMALIZED_Y);
      long seed = level.getSeed();

      for (int localX = 0; localX < 16; localX++) {
         for (int localZ = 0; localZ < 16; localZ++) {
            int x = minX + localX;
            int z = minZ + localZ;
            pos.set(x, 63, z);
            if (chunk.getBlockState(pos).getBlock() == Blocks.WATER && OnePieceGeography.isCanonicalOceanColumn(seed, x, z)) {
               pos.set(x, 64, z);
               if (chunk.getBlockState(pos).getBlock() == Blocks.WATER) {
                  for (int y = 64; y <= maxY; y++) {
                     pos.set(x, y, z);
                     if (chunk.getBlockState(pos).getBlock() == Blocks.WATER) {
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), 0);
                        changedBlocks++;
                     }
                  }
               }
            }
         }
      }

      if (changedBlocks != 0) {
         Heightmap.primeHeightmaps(chunk, EnumSet.allOf(Heightmap.Types.class));
         // NOTE : l'original appelait chunk.fillBiomesFromNoise(true). Sauté ici — le rabotage d'eau ne
         // modifie pas les biomes, et 26.3 exige un BiomeResolver dérivé d'un Climate.Sampler (non trivial).
         long count = NORMALIZED_CHUNKS.incrementAndGet();
         if (count <= 3L || count % 1000L == 0L) {
            OmpWorld.LOGGER.info("Normalized {} high-ocean blocks in chunk {} ({} chunks this run).", changedBlocks, chunk.getPos(), count);
         }
      }
   }
}
