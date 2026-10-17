package br.com.oneminepiece.opworldlike.zoan;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages communs des Fruits <b>Zoan</b> (FRUIT-{NEKO_NEKO_*,RYU_RYU_*,TORI_TORI,UO_UO,USHI_USHI}-04/05).
 *
 * <p>Les huit Zoan restants partagent <b>deux</b> mécaniques que le plan réclame Fruit par Fruit :</p>
 * <ul>
 *   <li><b>-04 « bonds et déplacements rapides avec arrêt à obstacle »</b>, critère « la vitesse et les
 *       destinations restent <b>bornées</b> » ;</li>
 *   <li><b>-05 « griffes et combos rapides »</b>, critère « le <b>nombre d'impacts et le coût</b> sont
 *       documentés et ne se <b>multiplient pas avec le spam</b> ».</li>
 * </ul>
 *
 * <p>Ces deux mécaniques sont donc écrites <b>une fois</b>, avec des tables explicites de techniques
 * (voir {@code ZoanMovement} et {@code ZoanClaw}) : recopier huit fois la même règle aurait produit huit
 * règles légèrement différentes. Les valeurs ver15 de chaque technique (dégâts, portée, énergie,
 * recharge) ne sont <b>pas</b> touchées.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-zoan.toml}</p>
 */
public final class DevilFruitZoanConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-zoan.toml");

   /** Distance d'un bond, en blocs (plafonnée dans le code). */
   private static double dashDistanceBlocks = 8.0;

   /** Nombre d'impacts d'un combo (plafonné dans le code). */
   private static int comboImpacts = 3;

   /** Plafonds DURS, dans le code. */
   public static final double HARD_MAX_DASH = 16.0;

   public static final int HARD_MAX_COMBO = 6;

   private DevilFruitZoanConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load zoan config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Distance d'un bond, bornée par {@link #HARD_MAX_DASH}. */
   public static double dashDistanceBlocks() {
      return Math.max(1.0, Math.min(HARD_MAX_DASH, dashDistanceBlocks));
   }

   /** Impressions d'un combo, bornées par {@link #HARD_MAX_COMBO}. */
   public static int comboImpacts() {
      return Math.max(1, Math.min(HARD_MAX_COMBO, comboImpacts));
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
               case "dash_distance_blocks" -> dashDistanceBlocks = Double.parseDouble(value);
               case "combo_impacts" -> comboImpacts = Integer.parseInt(value);
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Zoan config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Fruits Zoan (FRUIT-{NEKO_NEKO_*,RYU_RYU_*,TORI_TORI,UO_UO,USHI_USHI}-04/05)
         #
         # Les techniques et leurs valeurs viennent de ver15 et ne sont PAS reglees ici.
         # Bornes DURES dans le code : bond <= 16 blocs, combo <= 6 impacts.

         # BONDS (-04) : distance d'un deplacement borne, qui s'arrete au premier obstacle.
         dash_distance_blocks = 8.0

         # COMBOS (-05) : nombre d'impacts. Les degats de ver15 sont REPARTIS entre ces impacts,
         # jamais multiplies : spammer ne multiplie rien, la recharge s'en charge.
         combo_impacts = 3
         """;
   }
}
