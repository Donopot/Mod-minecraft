package br.com.oneminepiece.opworldlike;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class WorldLayoutConfig {
   public static final int DEFAULT_WORLD_RADIUS = 64000;
   public static final int DEFAULT_WORLD_HALF_HEIGHT = 48000;
   public static final int DEFAULT_WORLD_CORNER_RADIUS = 12000;
   private static final List<Integer> LEGACY_DEFAULT_WORLD_RADII = List.of(12500, 15000, 48000);
   public static final int MIN_WORLD_RADIUS = 64000;
   public static final int MIN_WORLD_HALF_HEIGHT = 48000;
   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "world-layout.toml");
   private static int worldRadius = 64000;
   private static int worldHalfHeight = 48000;
   private static int worldCornerRadius = 12000;

   private WorldLayoutConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }

         List<String> lines = Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8);
         if (!migrateGeneratedDefault(lines)) {
            parse(lines);
         }
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load world layout config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static int worldRadius() {
      return worldRadius;
   }

   public static int worldHalfHeight() {
      return worldHalfHeight;
   }

   public static int worldCornerRadius() {
      return worldCornerRadius;
   }

   private static void parse(List<String> lines) {
      int configuredRadius = 64000;
      int configuredHalfHeight = 48000;
      int configuredCornerRadius = 12000;

      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (!line.isBlank() && line.contains("=")) {
            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");
            if ("world-radius".equals(key)) {
               configuredRadius = Integer.parseInt(value);
            } else if ("world-half-height".equals(key)) {
               configuredHalfHeight = Integer.parseInt(value);
            } else if ("world-corner-radius".equals(key)) {
               configuredCornerRadius = Integer.parseInt(value);
            }
         }
      }

      if (configuredRadius < 64000) {
         OmpWorld.LOGGER.warn("world-radius={} is below the minimum {}; using {}.", new Object[]{configuredRadius, 64000, 64000});
         configuredRadius = 64000;
      }

      if (configuredHalfHeight < 48000) {
         OmpWorld.LOGGER.warn("world-half-height={} is below the minimum {}; using {}.", new Object[]{configuredHalfHeight, 48000, 48000});
         configuredHalfHeight = 48000;
      }

      configuredHalfHeight = Math.min(configuredHalfHeight, configuredRadius);
      configuredCornerRadius = Math.max(0, Math.min(configuredCornerRadius, Math.min(configuredRadius, configuredHalfHeight)));
      worldRadius = configuredRadius;
      worldHalfHeight = configuredHalfHeight;
      worldCornerRadius = configuredCornerRadius;
   }

   private static boolean migrateGeneratedDefault(List<String> lines) throws IOException {
      Integer legacyRadius = generatedLegacyRadius(lines);
      if (legacyRadius == null) {
         return false;
      }

      worldRadius = 64000;
      worldHalfHeight = 48000;
      worldCornerRadius = 12000;
      Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
      OmpWorld.LOGGER.info("Updated generated world-radius from {} to {}.", legacyRadius, 64000);
      return true;
   }

   private static Integer generatedLegacyRadius(List<String> lines) {
      Integer foundRadius = null;

      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (!line.isBlank()) {
            if (!line.startsWith("world-radius")) {
               return null;
            }

            String compact = line.replace(" ", "");

            for (int legacyRadius : LEGACY_DEFAULT_WORLD_RADII) {
               if (compact.equals("world-radius=" + legacyRadius)) {
                  foundRadius = legacyRadius;
               }
            }
         }
      }

      return foundRadius;
   }

   private static String defaultConfig() {
      return "# One Piece world dimensions in blocks. Width remains 128000 blocks.\n# The compact v11 navigable area is 128000 x 96000 with rounded corners.\nworld-radius = 64000\nworld-half-height = 48000\nworld-corner-radius = 12000\n";
   }
}
