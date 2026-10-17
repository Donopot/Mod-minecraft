package br.com.oneminepiece.opworldlike.wolf;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages d'Inu Inu Wolf (FRUIT-INU_INU_WOLF-04).
 *
 * <p>Le plan : « ajouter <b>bonds</b>, <b>morsures</b> et <b>poursuite</b> avec <b>endurance</b> »,
 * critère « les trois fonctions respectent <b>collisions, coûts et permissions</b> ».</p>
 *
 * <p>Le critère nomme trois garde-fous, et il faut les lire comme trois : une fonction peut respecter le
 * coût et ignorer les collisions, ou respecter les collisions et ignorer les permissions. Chaque
 * fonction les vérifie donc tous les trois.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-wolf.toml}</p>
 */
public final class DevilFruitWolfConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-wolf.toml");

   private static double leapDistance = 6.0;
   private static double leapUpward = 0.45;
   private static int leapCooldownGrace = 10;

   private static double biteRange = 4.0;
   private static double biteKnockback = 2.0;

   private static int pursuitEnduranceTicks = 160;
   private static double pursuitSpeed = 0.42;
   private static double pursuitCostPerSecond = 4.0;
   private static double pursuitMaxRange = 24.0;
   private static double pursuitStopDistance = 2.0;

   private DevilFruitWolfConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load wolf config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Distance maximale d'un bond, en blocs. */
   public static double leapDistance() {
      return leapDistance;
   }

   /** Part verticale du bond (une trajectoire purement horizontale serait plate et peu lisible). */
   public static double leapUpward() {
      return leapUpward;
   }

   /** Ticks de grâce après un bond, avant de pouvoir en relancer un. */
   public static int leapCooldownGrace() {
      return leapCooldownGrace;
   }

   /** Portée de la morsure, en blocs. */
   public static double biteRange() {
      return biteRange;
   }

   /** Recul infligé par la morsure. */
   public static double biteKnockback() {
      return biteKnockback;
   }

   /**
    * <b>Endurance</b> de la poursuite, en ticks.
    *
    * <p>C'est le cœur du critère : une poursuite <b>sans endurance</b> serait une course infinie, donc
    * une prison pour la cible. L'endurance est un budget qui se vide et ne se recharge pas en courant.</p>
    */
   public static int pursuitEnduranceTicks() {
      return pursuitEnduranceTicks;
   }

   /** Vitesse appliquée pendant la poursuite. */
   public static double pursuitSpeed() {
      return pursuitSpeed;
   }

   /** Énergie consommée par seconde de poursuite. */
   public static double pursuitCostPerSecond() {
      return pursuitCostPerSecond;
   }

   /** Distance maximale à laquelle une poursuite peut s'accrocher. */
   public static double pursuitMaxRange() {
      return pursuitMaxRange;
   }

   /** Distance à laquelle la poursuite considère avoir rattrapé sa cible. */
   public static double pursuitStopDistance() {
      return pursuitStopDistance;
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
               case "leap_distance" -> leapDistance = Math.max(1.0, Double.parseDouble(value));
               case "leap_upward" -> leapUpward = Math.max(0.0, Double.parseDouble(value));
               case "leap_cooldown_grace" -> leapCooldownGrace = Math.max(0, Integer.parseInt(value));
               case "bite_range" -> biteRange = Math.max(1.0, Double.parseDouble(value));
               case "bite_knockback" -> biteKnockback = Math.max(0.0, Double.parseDouble(value));
               case "pursuit_endurance_ticks" ->
                  pursuitEnduranceTicks = Math.max(20, Integer.parseInt(value));
               case "pursuit_speed" -> pursuitSpeed = Math.max(0.05, Double.parseDouble(value));
               case "pursuit_cost_per_second" ->
                  pursuitCostPerSecond = Math.max(0.0, Double.parseDouble(value));
               case "pursuit_max_range" -> pursuitMaxRange = Math.max(2.0, Double.parseDouble(value));
               case "pursuit_stop_distance" ->
                  pursuitStopDistance = Math.max(0.5, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Wolf config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Inu Inu Wolf (FRUIT-INU_INU_WOLF-04)

         # BOND : distance, part verticale, et delai avant de pouvoir relancer.
         # Le bond respecte les COLLISIONS : il est refuse si la destination est dans un bloc.
         leap_distance = 6.0
         leap_upward = 0.45
         leap_cooldown_grace = 10

         # MORSURE : portee courte, recul fort (le loup secoue sa prise).
         bite_range = 4.0
         bite_knockback = 2.0

         # POURSUITE AVEC ENDURANCE : c'est le coeur du critere.
         # Une poursuite SANS endurance serait une course infinie, donc une prison pour la cible.
         # L'endurance est un budget qui se vide et ne se recharge PAS en courant.
         pursuit_endurance_ticks = 160
         pursuit_speed = 0.42
         pursuit_cost_per_second = 4.0
         pursuit_max_range = 24.0
         pursuit_stop_distance = 2.0
         """;
   }
}
