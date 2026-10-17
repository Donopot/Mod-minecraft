package br.com.oneminepiece.opworldlike.flame;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages du feu (FRUIT-MERA_MERA-03/04/05).
 *
 * <p>Le plan demande une « <b>brûlure bornée</b> », un « <b>pilier ou anneau de feu temporaire avec
 * rayon et durée configurables</b> » et une « <b>propulsion de feu</b> consommant de l'énergie ».
 * Tout est réglé ici, jamais par des constantes dispersées dans les techniques.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-flame.toml}</p>
 */
public final class DevilFruitFlameConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-flame.toml");

   private static int burnTicks = 60;
   private static int burnCapTicks = 120;
   private static double burnDamagePerSecond = 1.0;
   private static boolean waterExtinguishes = true;

   private static boolean ringEnabled = true;
   private static double ringRadius = 3.0;
   private static int ringLifetimeTicks = 160;
   private static double ringDamagePerSecond = 4.0;
   private static int ringMaxResistance = 10;

   private static double propulsionCostPerTick = 0.6;
   private static int propulsionMaxTicks = 60;
   private static double propulsionPower = 0.5;

   private DevilFruitFlameConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load flame config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée d'une application de brûlure, en ticks. */
   public static int burnTicks() {
      return burnTicks;
   }

   /** Plafond dur de brûlure, en ticks, depuis la première application — la borne du plan. */
   public static int burnCapTicks() {
      return burnCapTicks;
   }

   /** Dégâts de la brûlure, par seconde. */
   public static double burnDamagePerSecond() {
      return burnDamagePerSecond;
   }

   /** L'eau éteint-elle la brûlure ? C'est la différence avec le magma (qui se rallume). */
   public static boolean waterExtinguishes() {
      return waterExtinguishes;
   }

   /** Anneau de feu activé ? {@code false} : aucune modification du terrain. */
   public static boolean ringEnabled() {
      return ringEnabled;
   }

   /** Rayon de l'anneau, en blocs — les dégâts ne doivent pas le dépasser. */
   public static double ringRadius() {
      return ringRadius;
   }

   /** Durée de vie de l'anneau, en ticks. */
   public static int ringLifetimeTicks() {
      return ringLifetimeTicks;
   }

   /** Dégâts de l'anneau, par seconde. */
   public static double ringDamagePerSecond() {
      return ringDamagePerSecond;
   }

   /** Résistance maximale d'un bloc remplaçable par le feu. */
   public static int ringMaxResistance() {
      return ringMaxResistance;
   }

   /** Énergie consommée par tick de propulsion. */
   public static double propulsionCostPerTick() {
      return propulsionCostPerTick;
   }

   /** Durée maximale d'une propulsion, en ticks. */
   public static int propulsionMaxTicks() {
      return propulsionMaxTicks;
   }

   /** Poussée appliquée à chaque tick de propulsion. */
   public static double propulsionPower() {
      return propulsionPower;
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
               case "burn_ticks" -> burnTicks = Math.max(20, Integer.parseInt(value));
               case "burn_cap_ticks" -> burnCapTicks = Math.max(20, Integer.parseInt(value));
               case "burn_damage_per_second" -> burnDamagePerSecond = Math.max(0.0, Double.parseDouble(value));
               case "water_extinguishes" -> waterExtinguishes = Boolean.parseBoolean(value);
               case "ring_enabled" -> ringEnabled = Boolean.parseBoolean(value);
               case "ring_radius" -> ringRadius = Math.max(1.0, Double.parseDouble(value));
               case "ring_lifetime_ticks" -> ringLifetimeTicks = Math.max(20, Integer.parseInt(value));
               case "ring_damage_per_second" -> ringDamagePerSecond = Math.max(0.0, Double.parseDouble(value));
               case "ring_max_resistance" -> ringMaxResistance = Math.max(0, Integer.parseInt(value));
               case "propulsion_cost_per_tick" -> propulsionCostPerTick = Math.max(0.0, Double.parseDouble(value));
               case "propulsion_max_ticks" -> propulsionMaxTicks = Math.max(1, Integer.parseInt(value));
               case "propulsion_power" -> propulsionPower = Math.max(0.0, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Flame config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Feu du Fruit Mera Mera (FRUIT-MERA_MERA-03/04/05)

         # Brulure BORNEE : une application dure burn_ticks, et le total depuis la premiere
         # application ne depasse JAMAIS burn_cap_ticks, meme martele.
         burn_ticks = 60
         burn_cap_ticks = 120
         burn_damage_per_second = 1.0

         # L'eau eteint le feu (contrairement au magma, qui se rallume) : c'est la difference
         # entre les deux pouvoirs.
         water_extinguishes = true

         # Anneau / pilier de feu temporaire. false : aucune modification du terrain.
         ring_enabled = true
         ring_radius = 3.0
         ring_lifetime_ticks = 160
         ring_damage_per_second = 4.0
         ring_max_resistance = 10

         # Propulsion de feu : energie par tick, duree maximale, poussee.
         propulsion_cost_per_tick = 0.6
         propulsion_max_ticks = 60
         propulsion_power = 0.5

         # Les zones protegees sont refusees dans tous les cas, et la propulsion s'arrete
         # sur un obstacle, a la suppression du pouvoir ou au manque d'energie.
         """;
   }
}
