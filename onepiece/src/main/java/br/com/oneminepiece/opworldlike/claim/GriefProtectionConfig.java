package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class GriefProtectionConfig {
   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "grief-protection.toml");
   private static boolean enabled = true;
   private static int protectedBuildRadius = 64;

   private GriefProtectionConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }

         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException ex) {
         OmpWorld.LOGGER.warn("Could not load grief protection config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static int protectedBuildRadius() {
      return protectedBuildRadius;
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (!line.isBlank() && line.contains("=")) {
            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");
            if ("enabled".equals(key)) {
               enabled = Boolean.parseBoolean(value);
            } else if ("protected-build-radius".equals(key)) {
               protectedBuildRadius = Math.max(4, Math.min(128, Integer.parseInt(value)));
            }
         }
      }
   }

   private static String defaultConfig() {
      return "# Protege construcoes de jogadores em ilhas dominadas contra habilidades destrutivas.\nenabled = true\nprotected-build-radius = 64\n";
   }
}
