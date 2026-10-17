package br.com.oneminepiece.opworldlike.sand;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages du sable (FRUIT-SUNA_SUNA-03/04/05).
 *
 * <p>Le plan demande une « <b>tempête</b> avec volume visible », un « <b>état de dessiccation</b> avec
 * accumulation et plafond », et de définir les « <b>interactions avec l'eau et les cibles humides</b> ».
 * Tout est réglé ici.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-sand.toml}</p>
 */
public final class DevilFruitSandConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-sand.toml");

   private static int dessicationMaxLevel = 3;
   private static int dessicationLevelTicks = 80;
   private static int dessicationSlownessAmplifierPerLevel = 1;
   private static double dessicationDamageAtMaxPerSecond = 2.0;
   private static boolean wetResists = true;

   private static double stormRadius = 4.0;
   private static double stormHeight = 3.0;
   private static int stormLifetimeTicks = 160;
   private static double stormDamagePerSecond = 3.0;

   private DevilFruitSandConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load sand config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Palier maximal de dessiccation — le plafond de l'accumulation. */
   public static int dessicationMaxLevel() {
      return dessicationMaxLevel;
   }

   /** Durée d'un palier de dessiccation, en ticks. */
   public static int dessicationLevelTicks() {
      return dessicationLevelTicks;
   }

   /** Amplificateur de ralentissement par palier. */
   public static int dessicationSlownessAmplifierPerLevel() {
      return dessicationSlownessAmplifierPerLevel;
   }

   /** Dégâts de déshydratation au palier maximal, par seconde. */
   public static double dessicationDamageAtMaxPerSecond() {
      return dessicationDamageAtMaxPerSecond;
   }

   /**
    * Les cibles <b>humides</b> résistent-elles à la dessiccation ?
    *
    * <p>C'est l'interaction avec l'eau demandée par le plan : du sable ne dessèche pas ce qui est
    * déjà mouillé. {@code false} rend la règle inopérante sans toucher au reste du pouvoir.</p>
    */
   public static boolean wetResists() {
      return wetResists;
   }

   /** Rayon du volume de la tempête, en blocs — le même sert au rendu et aux effets. */
   public static double stormRadius() {
      return stormRadius;
   }

   /** Hauteur du volume de la tempête, en blocs. */
   public static double stormHeight() {
      return stormHeight;
   }

   /** Durée de vie d'une tempête, en ticks. */
   public static int stormLifetimeTicks() {
      return stormLifetimeTicks;
   }

   /** Dégâts de la tempête, par seconde. */
   public static double stormDamagePerSecond() {
      return stormDamagePerSecond;
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
               case "dessication_max_level" -> dessicationMaxLevel = Math.max(1, Integer.parseInt(value));
               case "dessication_level_ticks" -> dessicationLevelTicks = Math.max(20, Integer.parseInt(value));
               case "dessication_slowness_amplifier_per_level" ->
                  dessicationSlownessAmplifierPerLevel = Math.max(0, Integer.parseInt(value));
               case "dessication_damage_at_max_per_second" ->
                  dessicationDamageAtMaxPerSecond = Math.max(0.0, Double.parseDouble(value));
               case "wet_resists" -> wetResists = Boolean.parseBoolean(value);
               case "storm_radius" -> stormRadius = Math.max(1.0, Double.parseDouble(value));
               case "storm_height" -> stormHeight = Math.max(1.0, Double.parseDouble(value));
               case "storm_lifetime_ticks" -> stormLifetimeTicks = Math.max(20, Integer.parseInt(value));
               case "storm_damage_per_second" -> stormDamagePerSecond = Math.max(0.0, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Sand config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Sable du Fruit Suna Suna (FRUIT-SUNA_SUNA-03/04/05)

         # Dessiccation : accumulation par paliers, PLAFONNEE.
         dessication_max_level = 3
         dessication_level_ticks = 80
         dessication_slowness_amplifier_per_level = 1
         dessication_damage_at_max_per_second = 2.0

         # Interaction avec l'eau : une cible HUMIDE resiste a la dessiccation.
         # Du sable ne desseche pas ce qui est deja mouille.
         wet_resists = true

         # Tempete de sable : le MEME rayon sert au rendu et aux effets, donc ce qui se voit
         # est exactement ce qui agit.
         storm_radius = 4.0
         storm_height = 3.0
         storm_lifetime_ticks = 160
         storm_damage_per_second = 3.0

         # La tempete ne modifie AUCUN bloc : rien a restaurer, aucune zone protegee touchee.
         """;
   }
}
