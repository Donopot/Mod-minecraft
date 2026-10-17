package br.com.oneminepiece.opworldlike.haki;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Configuration du Haki (CORE-029).
 *
 * <p>Reprend les réglages de ver15 (gamerules {@code minepieceGainExperience} et
 * {@code minepieceHakiMultiplier}, paliers d'EXP 3000/−1000 pour l'Armement et 300/−200 pour
 * l'Observation, niveau maximum 10) sous forme de fichier éditable :
 * {@code config/opworldlike/devilfruit-haki.toml}.</p>
 *
 * <p>Les contre-mesures sont rendues accessibles par deux voies : les gains d'EXP en combat
 * (fidèles à ver15) et la commande d'administration {@code /haki}.</p>
 */
public final class HakiConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-haki.toml");

   private static boolean enabled = true;
   private static boolean gainExperience = true;
   private static double hakiMultiplier = 5.0;
   private static int maxLevel = 10;
   private static boolean autoWear = true;

   private static double armamentExpPerLevel = 3000.0;
   private static double armamentExpCost = 1000.0;
   private static double observationExpPerLevel = 300.0;
   private static double observationExpCost = 200.0;

   private static double sovereignRadius = 12.0;
   private static double sovereignMaxMobHealth = 60.0;
   private static int sovereignDuration = 200;

   private HakiConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load haki config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static boolean gainExperience() {
      return gainExperience;
   }

   public static double hakiMultiplier() {
      return hakiMultiplier;
   }

   public static int maxLevel() {
      return maxLevel;
   }

   public static boolean autoWear() {
      return autoWear;
   }

   public static double armamentExpPerLevel() {
      return armamentExpPerLevel;
   }

   public static double armamentExpCost() {
      return armamentExpCost;
   }

   public static double observationExpPerLevel() {
      return observationExpPerLevel;
   }

   public static double observationExpCost() {
      return observationExpCost;
   }

   public static double sovereignRadius() {
      return sovereignRadius;
   }

   public static double sovereignMaxMobHealth() {
      return sovereignMaxMobHealth;
   }

   public static int sovereignDuration() {
      return sovereignDuration;
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (line.isBlank() || !line.contains("=")) {
            continue;
         }
         String[] parts = line.split("=", 2);
         String key = parts[0].trim();
         String value = parts[1].trim().replace("\"", "");
         try {
            switch (key) {
               case "enabled" -> enabled = Boolean.parseBoolean(value);
               case "gain-experience" -> gainExperience = Boolean.parseBoolean(value);
               case "haki-multiplier" -> hakiMultiplier = clamp(value, 0.0, 100000.0);
               case "max-level" -> maxLevel = (int) clamp(value, 1.0, 100.0);
               case "auto-wear" -> autoWear = Boolean.parseBoolean(value);
               case "armament-exp-per-level" -> armamentExpPerLevel = clamp(value, 1.0, 1000000.0);
               case "armament-exp-cost" -> armamentExpCost = clamp(value, 0.0, 1000000.0);
               case "observation-exp-per-level" -> observationExpPerLevel = clamp(value, 1.0, 1000000.0);
               case "observation-exp-cost" -> observationExpCost = clamp(value, 0.0, 1000000.0);
               case "sovereign-radius" -> sovereignRadius = clamp(value, 1.0, 64.0);
               case "sovereign-max-mob-health" -> sovereignMaxMobHealth = clamp(value, 1.0, 10000.0);
               case "sovereign-duration" -> sovereignDuration = (int) clamp(value, 20.0, 24000.0);
               default -> {
                  // clé inconnue ignorée
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Haki config: valeur invalide pour '{}' ({}).", key, value);
         }
      }
   }

   private static double clamp(String raw, double min, double max) {
      return Math.max(min, Math.min(max, Double.parseDouble(raw)));
   }

   private static String defaultConfig() {
      return "# Haki (CORE-029) — contre-mesures du systeme de Fruits du Demon.\n"
         + "# Reprend les reglages de ver15 (gamerules minepieceGainExperience / minepieceHakiMultiplier).\n"
         + "\n"
         + "enabled = true\n"
         + "\n"
         + "# Exp gagnee en combat (coups portes => Armement, degats subis => Observation).\n"
         + "gain-experience = true\n"
         + "haki-multiplier = 5.0\n"
         + "max-level = 10\n"
         + "\n"
         + "# Port automatique de l'effet Haki au niveau du joueur (port de SetStatusProcedure).\n"
         + "auto-wear = true\n"
         + "\n"
         + "# Paliers d'EXP par niveau (valeurs ver15 : 3000/-1000 Armement, 300/-200 Observation).\n"
         + "armament-exp-per-level = 3000.0\n"
         + "armament-exp-cost = 1000.0\n"
         + "observation-exp-per-level = 300.0\n"
         + "observation-exp-cost = 200.0\n"
         + "\n"
         + "# Haki Souverain : rayon et PV max des mobs mis hors de combat.\n"
         + "sovereign-radius = 12.0\n"
         + "sovereign-max-mob-health = 60.0\n"
         + "sovereign-duration = 200\n";
   }
}
