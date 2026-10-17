package br.com.oneminepiece.opworldlike.flight;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Configuration du vol des Fruits ailés (FRUIT-BATTO_BATTO-04).
 *
 * <p>ver15 accordait le vol sans coût ni coupure ; le plan demande un vol
 * « consommant l'énergie avec atterrissage garanti », qui « cesse à suppression ou manque d'énergie
 * sans laisser le joueur bloqué ». Les réglages vivent dans
 * {@code config/opworldlike/devilfruit-flight.toml} pour être ajustables sans recompiler.</p>
 */
public final class DevilFruitFlightConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-flight.toml");

   private static boolean enabled = true;
   private static boolean requireHybridForm = true;
   private static double costPerSecond = 4.0;
   private static double minimumEnergy = 1.0;
   private static boolean echolocationEnabled = true;
   private static double echolocationRadius = 12.0;
   private static int echolocationInterval = 40;
   private static int echolocationGlowingTicks = 60;

   private DevilFruitFlightConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load flight config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   /** Le vol n'est accordé qu'en forme hybride : hors forme, le joueur est ramené au sol. */
   public static boolean requireHybridForm() {
      return requireHybridForm;
   }

   public static double costPerSecond() {
      return costPerSecond;
   }

   public static double minimumEnergy() {
      return minimumEnergy;
   }

   public static boolean echolocationEnabled() {
      return echolocationEnabled;
   }

   public static double echolocationRadius() {
      return echolocationRadius;
   }

   public static int echolocationInterval() {
      return echolocationInterval;
   }

   public static int echolocationGlowingTicks() {
      return echolocationGlowingTicks;
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.trim();
         if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
            continue;
         }
         int cut = line.indexOf('=');
         String key = line.substring(0, cut).trim();
         String value = line.substring(cut + 1).trim();
         int hash = value.indexOf('#');
         if (hash >= 0) {
            value = value.substring(0, hash).trim();
         }
         value = value.replace("\"", "");
         try {
            switch (key) {
               case "enabled" -> enabled = Boolean.parseBoolean(value);
               case "require-hybrid-form" -> requireHybridForm = Boolean.parseBoolean(value);
               case "cost-per-second" -> costPerSecond = Double.parseDouble(value);
               case "minimum-energy" -> minimumEnergy = Double.parseDouble(value);
               case "echolocation-enabled" -> echolocationEnabled = Boolean.parseBoolean(value);
               case "echolocation-radius" -> echolocationRadius = Double.parseDouble(value);
               case "echolocation-interval" -> echolocationInterval = Integer.parseInt(value);
               case "echolocation-glowing-ticks" -> echolocationGlowingTicks = Integer.parseInt(value);
               default -> { }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Valeur invalide pour {} dans {} : {}", key, CONFIG_PATH, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Vol des Fruits ailés — FRUIT-BATTO_BATTO-04
         #
         # Le vol existait dans ver15 sans coût ni coupure. Ici il consomme de l'Énergie et s'arrête
         # proprement quand l'Énergie manque ou que le porteur est scellé par la mer, sans jamais
         # laisser le joueur bloqué en l'air.

         enabled = true

         # Le vol n'est disponible qu'en forme hybride (le joueur retombe sinon).
         require-hybrid-form = true

         # Énergie consommée par seconde de vol (20 ticks). 4.0 → 50 s de vol sur une réserve pleine.
         cost-per-second = 4.0

         # En dessous de ce seuil, l'Énergie ne permet plus de tenir en l'air : atterrissage.
         minimum-energy = 1.0

         # Écholocation de chauve-souris : impulsion sensorielle bornée, passive.
         # Elle révèle (Glowing) les créatures proches — ce n'est pas une attaque de dégâts.
         echolocation-enabled = true
         echolocation-radius = 12.0
         echolocation-interval = 40
         echolocation-glowing-ticks = 60
         """;
   }
}
