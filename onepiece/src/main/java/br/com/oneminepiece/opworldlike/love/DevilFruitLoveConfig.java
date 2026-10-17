package br.com.oneminepiece.opworldlike.love;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages du charme (FRUIT-MERO_MERO-03/04/05).
 *
 * <p>Le plan demande des « conditions de pétrification compréhensibles et testables », un « état
 * temporaire avec rendu, durée et résistances », et de « différencier projectile, coup physique et
 * attaque de zone ». Les durées et les rayons des trois modes sont ici.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-love.toml}</p>
 */
public final class DevilFruitLoveConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-love.toml");

   private static int petrificationTicks = 120;
   private static int petrificationResistanceAmplifier = 2;
   private static boolean undeadImmune = true;

   private static double areaRadius = 4.0;
   private static int areaDurationTicks = 100;

   private DevilFruitLoveConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load love config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée d'une pétrification, en ticks. */
   public static int petrificationTicks() {
      return petrificationTicks;
   }

   /** Amplificateur de résistance pendant la pétrification (une statue est solide). */
   public static int petrificationResistanceAmplifier() {
      return petrificationResistanceAmplifier;
   }

   /**
    * Les morts-vivants sont-ils insensibles ?
    *
    * <p>C'est la condition de pétrification rendue réglable : {@code false} la rend inopérante sans
    * toucher au reste du pouvoir.</p>
    */
   public static boolean undeadImmune() {
      return undeadImmune;
   }

   /** Rayon de l'attaque de zone (Perfume Femur Magna). */
   public static double areaRadius() {
      return areaRadius;
   }

   /** Durée de l'onde de charme de zone, en ticks. */
   public static int areaDurationTicks() {
      return areaDurationTicks;
   }

   private static void parse(java.util.List<String> lines) {
      for (String raw : lines) {
         String line = raw.trim();
         if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
            continue;
         }
         int cut = line.indexOf('=');
         String key = line.substring(0, cut).trim().toLowerCase(Locale.ROOT);
         String value = line.substring(cut + 1).trim();
         int hash = value.indexOf('#');
         if (hash >= 0) {
            value = value.substring(0, hash).trim();
         }
         try {
            switch (key) {
               case "petrification_ticks" -> petrificationTicks = Math.max(20, Integer.parseInt(value));
               case "petrification_resistance_amplifier" ->
                  petrificationResistanceAmplifier = Math.max(0, Integer.parseInt(value));
               case "undead_immune" -> undeadImmune = Boolean.parseBoolean(value);
               case "area_radius" -> areaRadius = Math.max(1.0, Double.parseDouble(value));
               case "area_duration_ticks" -> areaDurationTicks = Math.max(20, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Love config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Charme du Fruit Mero Mero (FRUIT-MERO_MERO-03/04/05)

         # Duree d'une petrification, en ticks.
         petrification_ticks = 120

         # Une statue est solide : resistance pendant la petrification.
         petrification_resistance_amplifier = 2

         # CONDITION de petrification : les morts-vivants ne ressentent rien.
         # false rend la condition inoperante sans toucher au reste du pouvoir.
         undead_immune = true

         # Attaque de zone (Perfume Femur Magna) : rayon et duree de l'onde de charme.
         area_radius = 4.0
         area_duration_ticks = 100

         # Les trois modes du fruit ont leur ciblage, leur cout et leur duree propres, declares
         # dans le registre des techniques : projectile (Pistol Kiss), coup physique (les kicks),
         # attaque de zone (Perfume Femur Magna).
         """;
   }
}
