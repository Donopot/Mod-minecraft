package br.com.oneminepiece.opworldlike.gravity;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de la gravité (FRUIT-ZUSHI_ZUSHI-03/04/05).
 *
 * <p>Le plan demande des « champs de gravité avec <b>intensité, rayon et durée bornés</b> », une
 * « <b>lévitation temporaire</b> avec permissions de cible » et des « <b>météores réellement
 * descendants</b> avec avertissement préalable ». Tout est réglé ici.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-gravity.toml}</p>
 */
public final class DevilFruitGravityConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-gravity.toml");

   private static double maxVelocity = 1.5;
   private static double fieldIntensity = 0.35;
   private static double fieldRadius = 6.0;
   private static int fieldLifetimeTicks = 100;

   private static int levitationTicks = 80;
   private static int levitationMaxAmplifier = 2;

   private static double meteorLaunchHeight = 28.0;
   private static double meteorSpeed = 2.5;
   private static double meteorRadius = 4.0;
   private static int meteorWarningTicks = 40;
   private static boolean meteorTerrainDamage = false;
   private static int meteorMaxConcurrent = 3;

   private DevilFruitGravityConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load gravity config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /**
    * Vitesse maximale qu'un champ de gravité peut imprimer à une cible, en blocs par tick.
    *
    * <p>C'est la borne exigée par le critère : « les cibles ne reçoivent <b>pas des vitesses
    * infinies</b> ». Elle est appliquée <b>après</b> le calcul d'intensité, donc une intensité absurde
    * dans le fichier de configuration ne peut pas la franchir.</p>
    */
   public static double maxVelocity() {
      return maxVelocity;
   }

   /** Intensité nominale d'un champ (blocs par tick, avant plafonnement). */
   public static double fieldIntensity() {
      return fieldIntensity;
   }

   /** Rayon d'un champ, en blocs. */
   public static double fieldRadius() {
      return fieldRadius;
   }

   /** Durée de vie d'un champ, en ticks. */
   public static int fieldLifetimeTicks() {
      return fieldLifetimeTicks;
   }

   /** Durée d'une lévitation, en ticks. */
   public static int levitationTicks() {
      return levitationTicks;
   }

   /** Amplificateur maximal de lévitation (plafond du cumul). */
   public static int levitationMaxAmplifier() {
      return levitationMaxAmplifier;
   }

   /** Hauteur d'apparition d'un météore, au-dessus du point d'impact. */
   public static double meteorLaunchHeight() {
      return meteorLaunchHeight;
   }

   /** Vitesse de descente d'un météore. */
   public static double meteorSpeed() {
      return meteorSpeed;
   }

   /** Rayon d'impact d'un météore. */
   public static double meteorRadius() {
      return meteorRadius;
   }

   /**
    * Durée de l'<b>avertissement</b> avant l'impact, en ticks.
    *
    * <p>C'est l'exigence « avertissement préalable » : la cible doit pouvoir voir venir le météore.
    * Un météore qui frappe sans prévenir n'est pas une attaque, c'est une surprise.</p>
    */
   public static int meteorWarningTicks() {
      return meteorWarningTicks;
   }

   /** Destruction du terrain par les météores — {@code false} par défaut. */
   public static boolean meteorTerrainDamage() {
      return meteorTerrainDamage;
   }

   /** Nombre de météores en vol simultanément par porteur. */
   public static int meteorMaxConcurrent() {
      return meteorMaxConcurrent;
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
               case "max_velocity" -> maxVelocity = Math.max(0.05, Double.parseDouble(value));
               case "field_intensity" -> fieldIntensity = Double.parseDouble(value);
               case "field_radius" -> fieldRadius = Math.max(1.0, Double.parseDouble(value));
               case "field_lifetime_ticks" -> fieldLifetimeTicks = Math.max(20, Integer.parseInt(value));
               case "levitation_ticks" -> levitationTicks = Math.max(20, Integer.parseInt(value));
               case "levitation_max_amplifier" -> levitationMaxAmplifier = Math.max(0, Integer.parseInt(value));
               case "meteor_launch_height" -> meteorLaunchHeight = Math.max(8.0, Double.parseDouble(value));
               case "meteor_speed" -> meteorSpeed = Math.max(0.5, Double.parseDouble(value));
               case "meteor_radius" -> meteorRadius = Math.max(1.0, Double.parseDouble(value));
               case "meteor_warning_ticks" -> meteorWarningTicks = Math.max(1, Integer.parseInt(value));
               case "meteor_terrain_damage" -> meteorTerrainDamage = Boolean.parseBoolean(value);
               case "meteor_max_concurrent" -> meteorMaxConcurrent = Math.max(0, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Gravity config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Gravite du Fruit Zushi Zushi (FRUIT-ZUSHI_ZUSHI-03/04/05)

         # PLAFOND DUR de vitesse. Aucune intensite, meme absurde, ne le franchit :
         # "les cibles ne recoivent pas des vitesses infinies".
         max_velocity = 1.5

         # Champ de gravite : intensite nominale (blocs/tick, plafonnee ci-dessus), rayon, duree.
         field_intensity = 0.35
         field_radius = 6.0
         field_lifetime_ticks = 100

         # Levitation temporaire : duree et plafond du cumul.
         levitation_ticks = 80
         levitation_max_amplifier = 2

         # Meteores : hauteur d'apparition, vitesse de descente, rayon d'impact.
         meteor_launch_height = 28.0
         meteor_speed = 2.5
         meteor_radius = 4.0

         # Avertissement AVANT l'impact, en ticks : la cible doit voir venir le meteore.
         meteor_warning_ticks = 40

         # Destruction du terrain : false par defaut. Si activee, elle passe par le moteur
         # d'explosion sensible aux zones protegees -- jamais de trou dans un claim.
         meteor_terrain_damage = false

         meteor_max_concurrent = 3
         """;
   }
}
