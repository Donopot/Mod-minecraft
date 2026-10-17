package br.com.oneminepiece.opworldlike.kira;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Kira Kira no Mi (FRUIT-KIRA_KIRA-03/04/06).
 *
 * <p>Le plan : « créer un <b>rendu de corps ou armure de diamant</b> », critère « <b>activation et
 * désactivation sont visibles pour les observateurs</b> » ; puis « ajouter une <b>réduction de
 * dégâts</b> avec coût ou conditions », critère « le <b>plafond de défense</b> et les <b>exceptions</b>
 * évitent l'<b>invulnérabilité absolue</b> non prévue ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-kira.toml}</p>
 */
public final class DevilFruitKiraConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-kira.toml");

   private static int bodyDurationTicks = 200;
   private static double bodyArmorBonus = 12.0;
   private static double bodyToughnessBonus = 4.0;
   private static double maxReductionPercent = 60.0;
   private static int activationCooldownTicks = 200;

   private DevilFruitKiraConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load kira config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée du corps de diamant, en ticks. */
   public static int bodyDurationTicks() {
      return bodyDurationTicks;
   }

   /** Bonus d'armure. */
   public static double bodyArmorBonus() {
      return bodyArmorBonus;
   }

   /** Bonus de résistance. */
   public static double bodyToughnessBonus() {
      return bodyToughnessBonus;
   }

   /**
    * <b>Plafond de réduction</b>, en pourcentage — la pièce qui empêche l'invulnérabilité.
    *
    * <p>Le plan est explicite : « le plafond de défense et les exceptions évitent l'<b>invulnérabilité
    * absolue</b> non prévue ». Un diamant réellement indestructible ne serait plus un pouvoir, ce serait
    * une suppression du jeu pour tout le monde sauf son porteur. Le plafond est donc une valeur
    * <b>déclarée</b>, pas un effet de bord de la formule d'armure : on peut la lire et la vérifier.</p>
    */
   public static double maxReductionPercent() {
      return maxReductionPercent;
   }

   /** Recharge de l'activation. */
   public static int activationCooldownTicks() {
      return activationCooldownTicks;
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
               case "body_duration_ticks" -> bodyDurationTicks = Math.max(20, Integer.parseInt(value));
               case "body_armor_bonus" -> bodyArmorBonus = Math.max(0.0, Double.parseDouble(value));
               case "body_toughness_bonus" ->
                  bodyToughnessBonus = Math.max(0.0, Double.parseDouble(value));
               case "max_reduction_percent" ->
                  maxReductionPercent = Math.min(95.0, Math.max(0.0, Double.parseDouble(value)));
               case "activation_cooldown_ticks" ->
                  activationCooldownTicks = Math.max(20, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Kira config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Kira Kira no Mi (FRUIT-KIRA_KIRA-03/04/06)

         # CORPS DE DIAMANT : active, borne, et VISIBLE des observateurs.
         body_duration_ticks = 200
         body_armor_bonus = 12.0
         body_toughness_bonus = 4.0
         activation_cooldown_ticks = 200

         # PLAFOND DE REDUCTION. Le plan exige que le plafond et les exceptions evitent
         # l'invulnerabilite absolue : un diamant reellement indestructible ne serait plus un
         # pouvoir, ce serait la suppression du jeu pour tout le monde sauf son porteur.
         # La valeur est DECLAREE, donc lisible et verifiable.
         max_reduction_percent = 60.0
         """;
   }
}
