package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ReverseMountainSpawnConfig {
   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "reverse-mountain-spawns.toml");
   private static boolean enabled = true;
   private static int attemptCooldownSeconds = 5;
   private static double attemptChance = 0.95;
   private static int minGroup = 4;
   private static int maxGroup = 9;
   private static int maxNearby = 60;
   private static int minRadius = 14;
   private static int maxRadius = 52;

   private ReverseMountainSpawnConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }

         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load Reverse Mountain spawn config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static int attemptCooldownTicks() {
      return Math.max(20, attemptCooldownSeconds * 20);
   }

   public static double attemptChance() {
      return attemptChance;
   }

   public static int minGroup() {
      return minGroup;
   }

   public static int maxGroup() {
      return maxGroup;
   }

   public static int maxNearby() {
      return maxNearby;
   }

   public static int minRadius() {
      return minRadius;
   }

   public static int maxRadius() {
      return maxRadius;
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (!line.isBlank() && line.contains("=")) {
            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");
            switch (key) {
               case "enabled":
                  enabled = Boolean.parseBoolean(value);
                  break;
               case "attempt-cooldown-seconds":
                  attemptCooldownSeconds = clampInt(value, 2, 60);
                  break;
               case "attempt-chance":
                  attemptChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "min-group":
                  minGroup = clampInt(value, 1, 30);
                  break;
               case "max-group":
                  maxGroup = clampInt(value, minGroup, 40);
                  break;
               case "max-nearby":
                  maxNearby = clampInt(value, 5, 200);
                  break;
               case "min-radius":
                  minRadius = clampInt(value, 4, 96);
                  break;
               case "max-radius":
                  maxRadius = clampInt(value, minRadius, 128);
            }
         }
      }
   }

   private static int clampInt(String raw, int min, int max) {
      int value = Integer.parseInt(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static double clampDouble(String raw, double min, double max) {
      double value = Double.parseDouble(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static String defaultConfig() {
      return "# Spawns extras nos cinco canais da Reverse Mountain.\nenabled = true\n\n# Quanto menor, mais frequente. 5s com chance alta deixa o local bem vivo.\nattempt-cooldown-seconds = 5\nattempt-chance = 0.95\n\n# Quantidade por tentativa, limitada por max-nearby.\nmin-group = 4\nmax-group = 9\nmax-nearby = 60\n\n# Distancia ao redor do jogador para gerar os mobs dentro da agua do canal.\nmin-radius = 14\nmax-radius = 52\n";
   }
}
