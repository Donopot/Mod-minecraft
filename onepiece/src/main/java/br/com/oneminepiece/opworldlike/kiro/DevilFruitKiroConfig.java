package br.com.oneminepiece.opworldlike.kiro;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Kiro Kiro no Mi (FRUIT-KIRO_KIRO-03/04/05/06).
 *
 * <p>Le plan : « ajouter un <b>poids réglable</b> avec <b>limites</b> et <b>interface</b> », critère « le
 * <b>serveur borne la valeur</b> et l'<b>interface suit l'état confirmé</b> » ; puis « relier poids,
 * saut, chute et vitesse de façon <b>réversible</b> », critère « revenir au poids normal restaure les
 * valeurs <b>sans accumulation</b> » ; puis une « <b>attaque de chute bornée</b> », critère « un
 * <b>seul impact</b> est appliqué et le <b>coût interdit le spam gratuit</b> ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-kiro.toml}</p>
 */
public final class DevilFruitKiroConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-kiro.toml");

   private static double minWeight = 0.1;
   private static double maxWeight = 100.0;
   private static double defaultWeight = 1.0;
   private static double speedAtMinWeight = 0.0;
   private static double speedAtMaxWeight = -0.25;
   private static double fallMultiplierAtMaxWeight = 3.0;
   private static int slamCooldownTicks = 80;
   private static double slamDamagePerWeight = 0.4;
   private static double slamMaxDamage = 40.0;
   private static double slamMinWeight = 20.0;

   private DevilFruitKiroConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load kiro config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Poids minimal autorisé. */
   public static double minWeight() {
      return minWeight;
   }

   /** Poids maximal autorisé — la borne que le SERVEUR applique, quoi que demande le client. */
   public static double maxWeight() {
      return maxWeight;
   }

   /** Poids normal (aucun effet). */
   public static double defaultWeight() {
      return defaultWeight;
   }

   /** Vitesse relative au poids minimal (le plus léger va le plus vite). */
   public static double speedAtMinWeight() {
      return speedAtMinWeight;
   }

   /** Vitesse relative au poids maximal (le plus lourd est le plus lent). */
   public static double speedAtMaxWeight() {
      return speedAtMaxWeight;
   }

   /** Multiplicateur de gravité au poids maximal (le lourd tombe plus vite). */
   public static double fallMultiplierAtMaxWeight() {
      return fallMultiplierAtMaxWeight;
   }

   /** Recharge de l'attaque de chute. */
   public static int slamCooldownTicks() {
      return slamCooldownTicks;
   }

   /** Dégâts par unité de poids. */
   public static double slamDamagePerWeight() {
      return slamDamagePerWeight;
   }

   /** Plafond de dégâts de l'attaque de chute. */
   public static double slamMaxDamage() {
      return slamMaxDamage;
   }

   /** Poids minimal pour qu'un impact soit possible. */
   public static double slamMinWeight() {
      return slamMinWeight;
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
               case "min_weight" -> minWeight = Math.max(0.01, Double.parseDouble(value));
               case "max_weight" -> maxWeight = Math.max(1.0, Double.parseDouble(value));
               case "default_weight" -> defaultWeight = Math.max(0.01, Double.parseDouble(value));
               case "speed_at_min_weight" -> speedAtMinWeight = Double.parseDouble(value);
               case "speed_at_max_weight" -> speedAtMaxWeight = Double.parseDouble(value);
               case "fall_multiplier_at_max_weight" ->
                  fallMultiplierAtMaxWeight = Math.max(1.0, Double.parseDouble(value));
               case "slam_cooldown_ticks" -> slamCooldownTicks = Math.max(10, Integer.parseInt(value));
               case "slam_damage_per_weight" ->
                  slamDamagePerWeight = Math.max(0.0, Double.parseDouble(value));
               case "slam_max_damage" -> slamMaxDamage = Math.max(0.0, Double.parseDouble(value));
               case "slam_min_weight" -> slamMinWeight = Math.max(0.0, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Kiro config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
      if (maxWeight < minWeight) {
         maxWeight = minWeight;
      }
      defaultWeight = Math.min(Math.max(defaultWeight, minWeight), maxWeight);
   }

   private static String defaultConfig() {
      return """
         # Kiro Kiro no Mi (FRUIT-KIRO_KIRO-03/04/05/06)

         # POIDS REGLABLE. Les bornes sont appliquees par le SERVEUR, quoi que demande le client :
         # c'est le sens du critere "le serveur borne la valeur".
         min_weight = 0.1
         max_weight = 100.0
         default_weight = 1.0

         # POIDS -> VITESSE. Le leger va plus vite, le lourd plus lentement.
         # La valeur est INTERPOLEE lineairement entre ces deux ancres.
         speed_at_min_weight = 0.0
         speed_at_max_weight = -0.25

         # POIDS -> CHUTE : le lourd tombe plus vite (multiplicateur de gravite au poids max).
         fall_multiplier_at_max_weight = 3.0

         # ATTAQUE DE CHUTE : bornee, UN SEUL impact, et un cout qui interdit le spam gratuit.
         slam_min_weight = 20.0
         slam_damage_per_weight = 0.4
         slam_max_damage = 40.0
         slam_cooldown_ticks = 80
         """;
   }
}
