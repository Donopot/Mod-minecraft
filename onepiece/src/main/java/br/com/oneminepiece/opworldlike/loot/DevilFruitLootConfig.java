package br.com.oneminepiece.opworldlike.loot;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Configuration du loot des Fruits du Démon en survie (CORE-023).
 *
 * <p>Contrôle la présence des Fruits dans les coffres de structures (rareté par palier), l'unicité
 * (un seul exemplaire par type en circulation) et la réincarnation à la mort. Fichier :
 * {@code config/opworldlike/devilfruit-loot.toml}.</p>
 */
public final class DevilFruitLootConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-loot.toml");

   private static boolean enabled = true;
   private static double chestDropChance = 0.02;
   private static boolean unique = true;
   private static boolean reincarnationOnDeath = true;
   private static int weightLegendary = 6;
   private static int weightRare = 25;
   private static int weightCommon = 60;

   private DevilFruitLootConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load devil fruit loot config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static double chestDropChance() {
      return chestDropChance;
   }

   public static boolean unique() {
      return unique;
   }

   public static boolean reincarnationOnDeath() {
      return reincarnationOnDeath;
   }

   public static int weightLegendary() {
      return weightLegendary;
   }

   public static int weightRare() {
      return weightRare;
   }

   public static int weightCommon() {
      return weightCommon;
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (!line.isBlank() && line.contains("=")) {
            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");
            switch (key) {
               case "enabled" -> enabled = Boolean.parseBoolean(value);
               case "chest-drop-chance" -> chestDropChance = clampDouble(value, 0.0, 1.0);
               case "unique" -> unique = Boolean.parseBoolean(value);
               case "reincarnation-on-death" -> reincarnationOnDeath = Boolean.parseBoolean(value);
               case "weight-legendary" -> weightLegendary = clampInt(value, 1, 1000);
               case "weight-rare" -> weightRare = clampInt(value, 1, 1000);
               case "weight-common" -> weightCommon = clampInt(value, 1, 1000);
               default -> {
                  // clé inconnue ignorée
               }
            }
         }
      }
   }

   private static double clampDouble(String raw, double min, double max) {
      double value = Double.parseDouble(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static int clampInt(String raw, int min, int max) {
      int value = Integer.parseInt(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static String defaultConfig() {
      return "# Loot des Fruits du Demon en survie (CORE-023).\n"
         + "# Ajoute les Fruits aux coffres de structures vanilla.\n"
         + "enabled = true\n\n"
         + "# Chance qu'un coffre de structure contienne un Fruit (0.0 - 1.0).\n"
         + "chest-drop-chance = 0.02\n\n"
         + "# Unicite : un seul exemplaire de chaque Fruit en circulation (aucun joueur ne le possede).\n"
         + "unique = true\n\n"
         + "# Reincarnation : la mort d'un utilisateur libere son Fruit (redevient lootable).\n"
         + "reincarnation-on-death = true\n\n"
         + "# Poids de rarete par palier (plus le poids est bas, plus le Fruit est rare).\n"
         + "weight-legendary = 6\n"
         + "weight-rare = 25\n"
         + "weight-common = 60\n";
   }
}
