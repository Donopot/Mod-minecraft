package br.com.oneminepiece.opworldlike.hollow;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages des fantômes (FRUIT-HORO_HORO-03/04/05).
 *
 * <p>Le plan demande des « fantômes avec trajectoires et <b>collisions spécifiques</b> », un « état
 * négatif <b>distinct du simple ralentissement</b> », et un « <b>contrôle à distance borné</b> avec
 * sortie garantie ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-hollow.toml}</p>
 */
public final class DevilFruitHollowConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-hollow.toml");

   private static int ghostLifetimeTicks = 80;
   private static boolean ghostsStopAtClaims = true;
   private static boolean ghostsBoundedLifetime = true;

   private static int negativeMaxLevel = 3;
   private static int negativeLevelTicks = 100;

   private static int controlMaxTicks = 120;
   private static double controlCostPerSecond = 2.0;
   private static double controlMaxDistance = 16.0;

   private DevilFruitHollowConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load hollow config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée de vie d'un fantôme, en ticks — bornée, même à l'intérieur d'un mur. */
   public static int ghostLifetimeTicks() {
      return ghostLifetimeTicks;
   }

   /**
    * Les fantômes s'arrêtent-ils aux frontières des zones protégées ?
    *
    * <p>C'est la « règle retenue » de traversée : un fantôme traverse les murs, mais pas un claim. Un
    * pouvoir qui ignore les protections serait un trou de sécurité, pas un pouvoir.</p>
    */
   public static boolean ghostsStopAtClaims() {
      return ghostsStopAtClaims;
   }

   /** La durée de vie est-elle appliquée ? ({@code false} laisserait un fantôme errer sans fin.) */
   public static boolean ghostsBoundedLifetime() {
      return ghostsBoundedLifetime;
   }

   /** Palier maximal de l'état négatif — le plafond du malus. */
   public static int negativeMaxLevel() {
      return negativeMaxLevel;
   }

   /** Durée d'un palier négatif, en ticks. */
   public static int negativeLevelTicks() {
      return negativeLevelTicks;
   }

   /** Durée maximale du contrôle à distance, en ticks. */
   public static int controlMaxTicks() {
      return controlMaxTicks;
   }

   /** Énergie consommée par seconde de contrôle. */
   public static double controlCostPerSecond() {
      return controlCostPerSecond;
   }

   /** Distance maximale à laquelle une cible peut être conduite. */
   public static double controlMaxDistance() {
      return controlMaxDistance;
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
               case "ghost_lifetime_ticks" -> ghostLifetimeTicks = Math.max(10, Integer.parseInt(value));
               case "ghosts_stop_at_claims" -> ghostsStopAtClaims = Boolean.parseBoolean(value);
               case "ghosts_bounded_lifetime" -> ghostsBoundedLifetime = Boolean.parseBoolean(value);
               case "negative_max_level" -> negativeMaxLevel = Math.max(1, Integer.parseInt(value));
               case "negative_level_ticks" -> negativeLevelTicks = Math.max(20, Integer.parseInt(value));
               case "control_max_ticks" -> controlMaxTicks = Math.max(20, Integer.parseInt(value));
               case "control_cost_per_second" -> controlCostPerSecond = Math.max(0.0, Double.parseDouble(value));
               case "control_max_distance" -> controlMaxDistance = Math.max(1.0, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Hollow config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Fantomes du Fruit Horo Horo (FRUIT-HORO_HORO-03/04/05)

         # Duree de vie BORNEE d'un fantome, meme a l'interieur d'un mur.
         ghost_lifetime_ticks = 80

         # REGLE RETENUE de traversee : un fantome traverse les murs (c'est un fantome), mais
         # PAS une zone protegee -- un pouvoir qui ignore les protections serait un trou de
         # securite, pas un pouvoir.
         ghosts_stop_at_claims = true

         # Appliquer la borne de duree de vie. false laisserait un fantome errer sans fin.
         ghosts_bounded_lifetime = true

         # Etat NEGATIF : accumulation par paliers, PLAFONNEE. Il n'est pas un simple
         # ralentissement : il porte aussi faiblesse et fatigue.
         negative_max_level = 3
         negative_level_ticks = 100

         # Controle a distance : duree, cout, portee. Sortie GARANTIE sur echeance, energie,
         # suppression, mort de la cible et DECONNEXION du joueur.
         control_max_ticks = 120
         control_cost_per_second = 2.0
         control_max_distance = 16.0
         """;
   }
}
