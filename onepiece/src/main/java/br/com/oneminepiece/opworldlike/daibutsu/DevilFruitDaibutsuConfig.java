package br.com.oneminepiece.opworldlike.daibutsu;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages du Daibutsu (FRUIT-HITO_HITO_DAIBUTSU-03/04/05).
 *
 * <p>Le plan demande une « forme géante visible avec <b>limite de taille</b> » dont « caméra,
 * collision, portée et <b>espace nécessaire</b> sont cohérents », des « ondes de choc visibles avec
 * progression et dégâts bornés », et de « définir mobilité et <b>coût de maintien</b> de la forme ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-daibutsu.toml}</p>
 */
public final class DevilFruitDaibutsuConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-daibutsu.toml");

   private static double maxScale = 3.5;
   private static double maintenanceCostPerSecond = 3.0;
   private static double mobilityPenalty = -0.15;
   private static int formMaxTicks = 1200;

   private static double waveSpeed = 1.5;
   private static double waveThickness = 1.2;
   private static double waveHalfHeight = 1.5;
   private static double waveDamage = 28.0;
   private static double waveMaxDistance = 12.0;
   private static int waveMaxConcurrent = 2;

   private DevilFruitDaibutsuConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load daibutsu config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /**
    * Échelle maximale de la forme géante — la <b>limite de taille</b> exigée par le plan.
    *
    * <p>Elle est appliquée au moment de l'application, jamais par la table de transformation : une
    * valeur aberrante dans le fichier ne peut pas produire un géant démesuré.</p>
    */
   public static double maxScale() {
      return maxScale;
   }

   /** Énergie consommée par seconde pour <b>maintenir</b> la forme. */
   public static double maintenanceCostPerSecond() {
      return maintenanceCostPerSecond;
   }

   /** Pénalité de mobilité de la forme (fraction de vitesse, négative). */
   public static double mobilityPenalty() {
      return mobilityPenalty;
   }

   /** Durée maximale de la forme, en ticks — borne dure indépendante de l'énergie. */
   public static int formMaxTicks() {
      return formMaxTicks;
   }

   /** Vitesse d'avancement du front d'onde, en blocs par tick. */
   public static double waveSpeed() {
      return waveSpeed;
   }

   /** Épaisseur du front, en blocs. */
   public static double waveThickness() {
      return waveThickness;
   }

   /** Demi-hauteur balayée par le front, en blocs. */
   public static double waveHalfHeight() {
      return waveHalfHeight;
   }

   /** Dégâts de l'onde (bornés : une frappe par cible). */
   public static double waveDamage() {
      return waveDamage;
   }

   /** Distance maximale de propagation. */
   public static double waveMaxDistance() {
      return waveMaxDistance;
   }

   /** Ondes simultanées par porteur. */
   public static int waveMaxConcurrent() {
      return waveMaxConcurrent;
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
               case "max_scale" -> maxScale = Math.max(1.0, Double.parseDouble(value));
               case "maintenance_cost_per_second" ->
                  maintenanceCostPerSecond = Math.max(0.0, Double.parseDouble(value));
               case "mobility_penalty" -> mobilityPenalty = Double.parseDouble(value);
               case "form_max_ticks" -> formMaxTicks = Math.max(20, Integer.parseInt(value));
               case "wave_speed" -> waveSpeed = Math.max(0.25, Double.parseDouble(value));
               case "wave_thickness" -> waveThickness = Math.max(0.25, Double.parseDouble(value));
               case "wave_half_height" -> waveHalfHeight = Math.max(0.25, Double.parseDouble(value));
               case "wave_damage" -> waveDamage = Math.max(0.0, Double.parseDouble(value));
               case "wave_max_distance" -> waveMaxDistance = Math.max(1.0, Double.parseDouble(value));
               case "wave_max_concurrent" -> waveMaxConcurrent = Math.max(0, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Daibutsu config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Daibutsu du Fruit Hito Hito (FRUIT-HITO_HITO_DAIBUTSU-03/04/05)

         # LIMITE DE TAILLE de la forme geante. Appliquee au moment de l'application, jamais
         # par la table de transformation : une valeur aberrante ne peut pas produire un geant
         # demesure.
         max_scale = 3.5

         # Cout de MAINTIEN de la forme, par seconde. A zero, la forme retombe.
         maintenance_cost_per_second = 3.0

         # Mobilite : le geant est plus lent (fraction de vitesse, negative).
         mobility_penalty = -0.15

         # Borne dure de duree, independante de l'energie.
         form_max_ticks = 1200

         # Onde de choc : le FRONT determine l'impact (une frappe par cible, sur son passage).
         wave_speed = 1.5
         wave_thickness = 1.2
         wave_half_height = 1.5
         wave_damage = 28.0
         wave_max_distance = 12.0
         wave_max_concurrent = 2

         # L'onde ne modifie AUCUN bloc : elle ne peut donc pas toucher une zone protegee.
         # Le passage dans une zone protegee n'atteint pas non plus ses occupants.
         """;
   }
}
