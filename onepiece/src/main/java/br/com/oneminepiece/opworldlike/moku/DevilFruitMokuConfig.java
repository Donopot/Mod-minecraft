package br.com.oneminepiece.opworldlike.moku;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Moku Moku no Mi (FRUIT-MOKU_MOKU-03/04/05/06).
 *
 * <p>Le plan : « créer un <b>nuage de fumée temporaire</b> et un rendu lisible », critère « le nuage est
 * <b>borné</b> et <b>visible pour les autres joueurs</b> » ; « permettre une <b>mobilité spécifique dans
 * la fumée</b> avec coût », critère « <b>sortie du nuage ou suppression remet la mobilité à sa valeur
 * normale</b> » ; « ajouter une <b>capture temporaire</b> dans la fumée avec <b>règles de libération</b> »,
 * critère « la cible <b>retrouve toujours le contrôle à expiration</b> et les <b>cibles protégées sont
 * exclues</b> ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-moku.toml}</p>
 */
public final class DevilFruitMokuConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-moku.toml");

   private static int cloudDurationTicks = 200;
   private static int cloudMaxActive = 3;
   private static double cloudRadius = 6.0;
   private static double mobilitySpeedBonus = 0.35;
   private static double mobilityEnergyPerSecond = 4.0;
   private static int captureDurationTicks = 80;
   private static int captureMaxTargets = 4;

   private DevilFruitMokuConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load moku config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée MAXIMALE d'un nuage. */
   public static int cloudDurationTicks() {
      return cloudDurationTicks;
   }

   /** Population maximale de nuages simultanés. */
   public static int cloudMaxActive() {
      return cloudMaxActive;
   }

   /** Rayon du nuage. */
   public static double cloudRadius() {
      return cloudRadius;
   }

   /** Bonus de vitesse DANS la fumée. */
   public static double mobilitySpeedBonus() {
      return mobilitySpeedBonus;
   }

   /**
    * <b>Coût de la mobilité dans la fumée</b>, en énergie par seconde.
    *
    * <p>Le plan exige « une mobilité spécifique dans la fumée <b>avec coût</b> » : se déplacer plus vite
    * dans son propre nuage n'est pas un cadeau permanent, c'est une dépense. Sans coût, le nuage serait
    * une amélioration gratuite, et le porteur n'aurait aucune raison d'en sortir.</p>
    */
   public static double mobilityEnergyPerSecond() {
      return mobilityEnergyPerSecond;
   }

   /** Durée MAXIMALE d'une capture. */
   public static int captureDurationTicks() {
      return captureDurationTicks;
   }

   /** Nombre maximal de cibles capturées à la fois. */
   public static int captureMaxTargets() {
      return captureMaxTargets;
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
               case "cloud_duration_ticks" -> cloudDurationTicks = Math.max(20, Integer.parseInt(value));
               case "cloud_max_active" -> cloudMaxActive = Math.max(1, Integer.parseInt(value));
               case "cloud_radius" -> cloudRadius = Math.max(1.0, Double.parseDouble(value));
               case "mobility_speed_bonus" ->
                  mobilitySpeedBonus = Math.max(0.0, Double.parseDouble(value));
               case "mobility_energy_per_second" ->
                  mobilityEnergyPerSecond = Math.max(0.0, Double.parseDouble(value));
               case "capture_duration_ticks" ->
                  captureDurationTicks = Math.max(20, Integer.parseInt(value));
               case "capture_max_targets" -> captureMaxTargets = Math.max(1, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Moku config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Moku Moku no Mi (FRUIT-MOKU_MOKU-03/04/05/06)

         # NUAGE DE FUMEE : borne et VISIBLE des observateurs (le critere parle des autres joueurs).
         cloud_duration_ticks = 200
         cloud_max_active = 3
         cloud_radius = 6.0

         # MOBILITE DANS LA FUMEE, AVEC COUT. Sortir du nuage (ou la suppression du pouvoir) doit
         # remettre la mobilite a sa valeur NORMALE, sans residu.
         mobility_speed_bonus = 0.35
         mobility_energy_per_second = 4.0

         # CAPTURE TEMPORAIRE : duree MAXIMALE (la cible retrouve TOUJOURS le controle)
         # et cibles protegees exclues.
         capture_duration_ticks = 80
         capture_max_targets = 4
         """;
   }
}
