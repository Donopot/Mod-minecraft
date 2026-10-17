package br.com.oneminepiece.opworldlike.worldgen;

import br.com.oneminepiece.opworldlike.OmpWorld;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.ChunkPos;

final class ExtractedIslandChunkIndex {
   private static final String RESOURCE = "data/opworldlike/extracted_islands/chunks.csv";

   private ExtractedIslandChunkIndex() {
   }

   static boolean available() {
      return ExtractedIslandChunkIndex.Holder.INDEX.available();
   }

   static boolean contains(ChunkPos pos) {
      return ExtractedIslandChunkIndex.Holder.INDEX.chunks().contains(pos.pack());
   }

   private static ExtractedIslandChunkIndex.Index load() {
      LongOpenHashSet chunks = new LongOpenHashSet(180000);
      ClassLoader loader = ExtractedIslandChunkIndex.class.getClassLoader();

      try (InputStream stream = loader.getResourceAsStream("data/opworldlike/extracted_islands/chunks.csv")) {
         if (stream == null) {
            OmpWorld.LOGGER
               .error("Extracted-island chunk index is missing: {}. Ocean normalization is disabled.", "data/opworldlike/extracted_islands/chunks.csv");
            return new ExtractedIslandChunkIndex.Index(chunks, false);
         }

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            String[] header = line == null ? new String[0] : line.split(",", -1);
            int chunkXColumn = columnIndex(header, "target_chunk_x");
            int chunkZColumn = columnIndex(header, "target_chunk_z");
            if (chunkXColumn < 0 || chunkZColumn < 0) {
               throw new IOException("Extracted-island chunk index lacks target coordinates");
            }

            while ((line = reader.readLine()) != null) {
               String[] fields = line.split(",", -1);
               if (fields.length <= Math.max(chunkXColumn, chunkZColumn)) {
                  throw new IOException("Malformed extracted-island chunk index row: " + line);
               }

               int chunkX = Integer.parseInt(fields[chunkXColumn]);
               int chunkZ = Integer.parseInt(fields[chunkZColumn]);
               chunks.add(ChunkPos.pack(chunkX, chunkZ));
            }
         }
      } catch (IOException | NumberFormatException ex) {
         OmpWorld.LOGGER.error("Failed to load extracted-island chunk index. Ocean normalization is disabled.", ex);
         return new ExtractedIslandChunkIndex.Index(new LongOpenHashSet(), false);
      }

      OmpWorld.LOGGER.info("Loaded {} protected extracted-island chunks for canonical ocean normalization.", chunks.size());
      return new ExtractedIslandChunkIndex.Index(chunks, true);
   }

   private static int columnIndex(String[] header, String name) {
      for (int index = 0; index < header.length; index++) {
         if (name.equals(header[index])) {
            return index;
         }
      }

      return -1;
   }

   private static final class Holder {
      private static final ExtractedIslandChunkIndex.Index INDEX = ExtractedIslandChunkIndex.load();
   }

   private record Index(LongOpenHashSet chunks, boolean available) {
   }
}
