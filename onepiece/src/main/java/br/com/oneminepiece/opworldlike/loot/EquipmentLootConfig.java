package br.com.oneminepiece.opworldlike.loot;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EquipmentLootConfig {
   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "equipment-loot.toml");
   private static boolean enabled = true;
   private static double modMobDropChance = 0.025;
   private static double chestDropChance = 0.025;
   private static double grandLineLootMultiplier = 1.5;
   private static double newWorldLootMultiplier = 2.0;
   private static double mmnmLeadDropChance = 0.2;
   private static String mmnmLeadDropItem = "minecraft:lead";
   private static double mmnmCharacterCreatorDropChance = 0.05;
   private static String mmnmCharacterCreatorDropItem = "minepiece:character_creator";

   private EquipmentLootConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }

         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load equipment loot config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static double modMobDropChance() {
      return modMobDropChance;
   }

   public static double chestDropChance() {
      return chestDropChance;
   }

   public static double grandLineLootMultiplier() {
      return grandLineLootMultiplier;
   }

   public static double newWorldLootMultiplier() {
      return newWorldLootMultiplier;
   }

   public static double mmnmLeadDropChance() {
      return mmnmLeadDropChance;
   }

   public static String mmnmLeadDropItem() {
      return mmnmLeadDropItem;
   }

   public static double mmnmCharacterCreatorDropChance() {
      return mmnmCharacterCreatorDropChance;
   }

   public static String mmnmCharacterCreatorDropItem() {
      return mmnmCharacterCreatorDropItem;
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
               case "mod-mob-drop-chance":
                  modMobDropChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "lootr-chest-drop-chance":
                  chestDropChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "grand-line-loot-multiplier":
                  grandLineLootMultiplier = clampDouble(value, 0.0, 10.0);
                  break;
               case "new-world-loot-multiplier":
                  newWorldLootMultiplier = clampDouble(value, 0.0, 10.0);
                  break;
               case "mmnm-lead-drop-chance":
                  mmnmLeadDropChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "mmnm-lead-drop-item":
                  mmnmLeadDropItem = value;
                  break;
               case "mmnm-character-creator-drop-chance":
                  mmnmCharacterCreatorDropChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "mmnm-character-creator-drop-item":
                  mmnmCharacterCreatorDropItem = value;
            }
         }
      }
   }

   private static double clampDouble(String raw, double min, double max) {
      double value = Double.parseDouble(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static String defaultConfig() {
      return "# Adiciona roupas, armas e espadas do Mine Piece como loot raro.\nenabled = true\n\n# Chance por mob do Mine Piece morto por jogador.\nmod-mob-drop-chance = 0.025\n\n# Chance por bau gerado por loot table. Lootr usa essas tabelas ao abrir o bau.\nlootr-chest-drop-chance = 0.025\n\n# Multiplicadores regionais aplicados aos drops extras do OMPWorld.\ngrand-line-loot-multiplier = 1.5\nnew-world-loot-multiplier = 2.0\n\n# Drops fixos de mobs do Mine Piece mortos por jogador.\nmmnm-lead-drop-chance = 0.20\nmmnm-lead-drop-item = \"minecraft:lead\"\nmmnm-character-creator-drop-chance = 0.05\nmmnm-character-creator-drop-item = \"minepiece:character_creator\"\n";
   }
}
