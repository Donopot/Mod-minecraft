package br.com.oneminepiece.opworldlike.yami;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Yami Yami no Mi (FRUIT-YAMI_YAMI-03/04/05/06).
 *
 * <p>Le plan demande cinq mécaniques : une <b>attraction progressive à force maximale et rayon
 * bornés</b> (-03), une <b>absorption à capacité et coût explicites</b> (-04), une <b>suppression
 * temporaire des fruits</b> avec son exception d'intangibilité <b>décidée</b> (-05), les
 * <b>exceptions mesurées</b> de ce Logia (-06) et la classification <b>cohérente avec la résistance aux
 * dégâts</b> (-02).</p>
 *
 * <p>Les <b>quatre techniques et leurs valeurs viennent de ver15</b> et ne changent pas : Kurouzu
 * (6/10/1), Black Hole (24/48/2), Dark Ball (40, 80 ticks), Dark Matter (200, 80 ticks). Ce fichier ne
 * règle que ce que ver15 ne déclarait pas — et chaque grandeur a sa <b>borne dure dans le code</b>. Un
 * TOML mal édité ne peut pas produire une attraction de 10 000 blocs, une capacité d'absorption
 * infinie ni un scellement éternel.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-yami.toml}</p>
 */
public final class DevilFruitYamiConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-yami.toml");

   // ---- Attraction (-03) ----
   private static double pullRadiusBlocks = 32.0;
   private static double pullForceMax = 0.8;
   private static int pullTicks = 20;

   // ---- Absorption (-04) ----
   private static int absorbCapacityHits = 6;
   private static double absorbCostEnergy = 20.0;

   // ---- Scellement (-05) ----
   private static int sealTicks = 100;

   // ---- Douleur des ténèbres (-02/-06, décision déclarée) ----
   private static double painMultiplier = 2.0;

   /** Plafonds DURS, dans le code : une valeur absurde du fichier ne doit pas passer. */
   public static final double HARD_MAX_RADIUS = 64.0;

   public static final double HARD_MAX_FORCE = 2.0;

   public static final int HARD_MAX_PULL_TICKS = 100;

   public static final int HARD_MAX_ABSORB = 24;

   public static final int HARD_MAX_SEAL_TICKS = 300;

   public static final double HARD_MAX_PAIN = 3.0;

   private DevilFruitYamiConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load yami config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Rayon d'attraction, borné par {@link #HARD_MAX_RADIUS}. */
   public static double pullRadiusBlocks() {
      return Math.max(2.0, Math.min(HARD_MAX_RADIUS, pullRadiusBlocks));
   }

   /** Force maximale d'attraction par tick, bornée par {@link #HARD_MAX_FORCE}. */
   public static double pullForceMax() {
      return Math.max(0.05, Math.min(HARD_MAX_FORCE, pullForceMax));
   }

   /** Durée de l'attraction progressive, bornée par {@link #HARD_MAX_PULL_TICKS}. */
   public static int pullTicks() {
      return Math.max(1, Math.min(HARD_MAX_PULL_TICKS, pullTicks));
   }

   /** Nombre de coups absorbables avant saturation, borné par {@link #HARD_MAX_ABSORB}. */
   public static int absorbCapacityHits() {
      return Math.max(0, Math.min(HARD_MAX_ABSORB, absorbCapacityHits));
   }

   /** Coût en énergie de chaque absorption. */
   public static double absorbCostEnergy() {
      return Math.max(0.0, absorbCostEnergy);
   }

   /** Durée du scellement d'un pouvoir touché, bornée par {@link #HARD_MAX_SEAL_TICKS}. */
   public static int sealTicks() {
      return Math.max(1, Math.min(HARD_MAX_SEAL_TICKS, sealTicks));
   }

   /**
    * Multiplicateur de <b>douleur</b> des ténèbres : ce fruit est le seul Logia qui <b>ne rend pas
    * intangible</b>, et sa contrepartie assumée est qu'il <b>subit plus</b>. Valeur <b>décidée</b> (elle
    * n'est ni dans ver15 ni dans le plan) : 2,0. La mettre à 1,0 désactive la contrepartie sans rien
    * casser d'autre.
    */
   public static double painMultiplier() {
      return Math.max(1.0, Math.min(HARD_MAX_PAIN, painMultiplier));
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
               case "pull_radius_blocks" -> pullRadiusBlocks = Double.parseDouble(value);
               case "pull_force_max" -> pullForceMax = Double.parseDouble(value);
               case "pull_ticks" -> pullTicks = Integer.parseInt(value);
               case "absorb_capacity_hits" -> absorbCapacityHits = Integer.parseInt(value);
               case "absorb_cost_energy" -> absorbCostEnergy = Double.parseDouble(value);
               case "seal_ticks" -> sealTicks = Integer.parseInt(value);
               case "pain_multiplier" -> painMultiplier = Double.parseDouble(value);
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Yami config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Yami Yami no Mi (FRUIT-YAMI_YAMI-03/04/05/06)
         #
         # Les 4 techniques et leurs degats/couts/recharges viennent de ver15 et ne sont PAS regles ici
         # (Kurouzu 6/10/1, Black Hole 24/48/2, Dark Ball 40, Dark Matter 200).
         # Toutes les bornes sont DURES dans le code : rayon <= 64, force <= 2.0, attraction <= 100
         # ticks, capacite <= 24 coups, scellement <= 300 ticks, douleur <= x3.

         # ATTRACTION (-03) : progressive, rayon et force maximale bornes.
         pull_radius_blocks = 32.0
         pull_force_max = 0.8
         pull_ticks = 20

         # ABSORPTION (-04) : capacite et cout explicites. Une capacite depassee ne supprime plus rien.
         absorb_capacity_hits = 6
         absorb_cost_energy = 20.0

         # SCELLEMENT (-05) : duree pendant laquelle la cible perd ses capacites, puis les retrouve.
         seal_ticks = 100

         # DOULEUR DES TENEBRES (decision declaree) : ce Logia n'est PAS intangible, et subit plus.
         pain_multiplier = 2.0
         """;
   }
}
