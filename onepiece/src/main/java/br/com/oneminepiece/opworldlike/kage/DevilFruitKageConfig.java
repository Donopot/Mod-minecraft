package br.com.oneminepiece.opworldlike.kage;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Kage Kage no Mi (FRUIT-KAGE_KAGE-03/05/06).
 *
 * <p>Le plan : « créer des <b>ombres temporaires</b> avec <b>règles d'acquisition et restitution</b> »,
 * critère « une ombre <b>perdue est restituée dans tous les cas de fin prévus</b> ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-kage.toml}</p>
 */
public final class DevilFruitKageConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-kage.toml");

   private static int shadowCap = 3;
   private static double stealRadius = 12.0;
   private static int asgardDurationTicks = 100;
   private static double attackPerShadow = 3.0;
   private static int shadowlessDurationTicks = 600;
   private static double shadowlessArmorPenalty = 2.0;
   private static int doppelmanLifetimeTicks = 200;

   private DevilFruitKageConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load kage config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /**
    * Plafond d'ombres détenues — le « plafonner » de -05.
    *
    * <p>Un plafond n'est pas un réglage de confort : sans lui, l'acquisition serait illimitée, donc
    * l'avantage proportionnel au nombre de cibles présentes. Le plafond rend la décision <b>finie</b> :
    * quelles trois ombres garder ?</p>
    */
   public static int shadowCap() {
      return shadowCap;
   }

   /** Rayon d'acquisition lors de Shadow Asgard. */
   public static double stealRadius() {
      return stealRadius;
   }

   /** Durée de Shadow Asgard, en ticks. */
   public static int asgardDurationTicks() {
      return asgardDurationTicks;
   }

   /** Bonus d'attaque par ombre détenue. */
   public static double attackPerShadow() {
      return attackPerShadow;
   }

   /** Durée de l'état « sans ombre » chez la victime. */
   public static int shadowlessDurationTicks() {
      return shadowlessDurationTicks;
   }

   /** Pénalité d'armure infligée à la victime — appliquée par NOS modificateurs, retirée nominativement. */
   public static double shadowlessArmorPenalty() {
      return shadowlessArmorPenalty;
   }

   /** Durée de vie d'un Doppelman, en ticks. */
   public static int doppelmanLifetimeTicks() {
      return doppelmanLifetimeTicks;
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
               case "shadow_cap" -> shadowCap = Math.max(1, Integer.parseInt(value));
               case "steal_radius" -> stealRadius = Math.max(1.0, Double.parseDouble(value));
               case "asgard_duration_ticks" -> asgardDurationTicks = Math.max(20, Integer.parseInt(value));
               case "attack_per_shadow" -> attackPerShadow = Math.max(0.0, Double.parseDouble(value));
               case "shadowless_duration_ticks" ->
                  shadowlessDurationTicks = Math.max(20, Integer.parseInt(value));
               case "shadowless_armor_penalty" ->
                  shadowlessArmorPenalty = Math.max(0.0, Double.parseDouble(value));
               case "doppelman_lifetime_ticks" ->
                  doppelmanLifetimeTicks = Math.max(20, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Kage config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Kage Kage no Mi (FRUIT-KAGE_KAGE-03/05/06)

         # OMBRES TEMPORAIRES : acquisition (Shadow Asgard) et RESTITUTION garantie.
         # Le plafond rend la decision finie : quelles trois ombres garder ?
         shadow_cap = 3
         steal_radius = 12.0
         asgard_duration_ticks = 100
         attack_per_shadow = 3.0

         # Etat de la VICTIME. La penalite passe par NOS modificateurs d attribut, retires
         # nominativement : on ne retire jamais un effet vanille qu on n a pas pose.
         shadowless_duration_ticks = 600
         shadowless_armor_penalty = 2.0

         # Doppelman : invocation reelle (proprietaire, IA, duree de vie).
         doppelman_lifetime_ticks = 200
         """;
   }
}
