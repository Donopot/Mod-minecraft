package br.com.oneminepiece.opworldlike.pika;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Pika Pika no Mi (FRUIT-PIKA_PIKA-03/04/05/06).
 *
 * <p>Le plan demande quatre mécaniques que le moteur générique ne prouve pas : des <b>rayons lumineux
 * à collision et portée explicites</b> (-03), un <b>déplacement lumineux à destination calculée côté
 * serveur</b> (-04), une <b>charge ou un avertissement visible</b> devant les attaques rapides avec un
 * <b>nombre d'impacts borné</b> (-05), et les <b>exceptions propres de ce Logia</b> (-06).</p>
 *
 * <p>Les huit techniques et leurs valeurs viennent de ver15 et <b>ne changent pas</b> : ce fichier ne
 * règle que ce que ver15 ne déclarait pas — la portée des rayons, la distance de déplacement, le délai
 * d'avertissement, la taille de la volée et les plafonds de bonus. Chaque grandeur a sa <b>borne dure
 * dans le code</b> : un TOML mal édité ne peut pas produire un rayon de 10 000 blocs, une volée de
 * 500 impacts ni un bonus sans plafond.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-pika.toml}</p>
 */
public final class DevilFruitPikaConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-pika.toml");

   // ---- Rayons (-03) ----
   private static double laserRangeBlocks = 40.0;
   private static double magatamaRangeBlocks = 24.0;
   private static int magatamaPerVolley = 3;

   // ---- Déplacement lumineux (-04) ----
   private static double travelMaxDistanceBlocks = 24.0;

   // ---- Avertissement et volée (-05) ----
   private static int chargeTicks = 20;
   private static int barrageMaxImpacts = 6;

   // ---- Bonus de « Speed is Power » et plafonds de buff (-06) ----
   private static double speedPowerBonusMax = 12.0;
   private static int buffMaxAmplifier = 2;

   /** Plafonds DURS, dans le code : une valeur absurde du fichier ne doit pas passer. */
   public static final double HARD_MAX_RANGE = 96.0;

   public static final double HARD_MAX_TRAVEL = 48.0;

   public static final int HARD_MAX_CHARGE = 60;

   public static final int HARD_MAX_BARRAGE = 12;

   public static final int HARD_MAX_AMPLIFIER = 4;

   private DevilFruitPikaConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load pika config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Portée du rayon Laser, bornée par {@link #HARD_MAX_RANGE}. */
   public static double laserRangeBlocks() {
      return Math.max(2.0, Math.min(HARD_MAX_RANGE, laserRangeBlocks));
   }

   /** Portée des perles de Yasakani no Magatama, bornée. */
   public static double magatamaRangeBlocks() {
      return Math.max(2.0, Math.min(HARD_MAX_RANGE, magatamaRangeBlocks));
   }

   /** Nombre de perles par usage — la « nuée » est bornée par construction. */
   public static int magatamaPerVolley() {
      return Math.max(1, Math.min(8, magatamaPerVolley));
   }

   /** Distance maximale du déplacement lumineux, bornée par {@link #HARD_MAX_TRAVEL}. */
   public static double travelMaxDistanceBlocks() {
      return Math.max(1.0, Math.min(HARD_MAX_TRAVEL, travelMaxDistanceBlocks));
   }

   /** Délai d'avertissement avant l'impact, borné par {@link #HARD_MAX_CHARGE}. */
   public static int chargeTicks() {
      return Math.max(1, Math.min(HARD_MAX_CHARGE, chargeTicks));
   }

   /** Nombre maximal d'impacts d'une volée, borné par {@link #HARD_MAX_BARRAGE}. */
   public static int barrageMaxImpacts() {
      return Math.max(1, Math.min(HARD_MAX_BARRAGE, barrageMaxImpacts));
   }

   /** Bonus maximal de « Speed is Power ». */
   public static double speedPowerBonusMax() {
      return Math.max(0.0, Math.min(40.0, speedPowerBonusMax));
   }

   /** Amplificateur maximal d'un bonus de lumière, borné par {@link #HARD_MAX_AMPLIFIER}. */
   public static int buffMaxAmplifier() {
      return Math.max(0, Math.min(HARD_MAX_AMPLIFIER, buffMaxAmplifier));
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
               case "laser_range_blocks" -> laserRangeBlocks = Double.parseDouble(value);
               case "magatama_range_blocks" -> magatamaRangeBlocks = Double.parseDouble(value);
               case "magatama_per_volley" -> magatamaPerVolley = Integer.parseInt(value);
               case "travel_max_distance_blocks" -> travelMaxDistanceBlocks = Double.parseDouble(value);
               case "charge_ticks" -> chargeTicks = Integer.parseInt(value);
               case "barrage_max_impacts" -> barrageMaxImpacts = Integer.parseInt(value);
               case "speed_power_bonus_max" -> speedPowerBonusMax = Double.parseDouble(value);
               case "buff_max_amplifier" -> buffMaxAmplifier = Integer.parseInt(value);
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Pika config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Pika Pika no Mi (FRUIT-PIKA_PIKA-03/04/05/06)
         #
         # Les 8 techniques et leurs degats/couts/recharges viennent de ver15 et ne sont PAS regles ici.
         # Seules les mecaniques que ver15 ne declarait pas sont reglees, et toutes sont bornees DANS LE
         # CODE : portee <= 96, deplacement <= 48, avertissement <= 60 ticks, volee <= 12 impacts,
         # amplificateur <= 4.

         # RAYONS (-03) : portee EXPLICITE en blocs. Un rayon s'arrete a sa portee, il ne continue pas
         # parce que sa duree de vie n'est pas ecoulee.
         laser_range_blocks = 40.0
         magatama_range_blocks = 24.0
         # Nombre de perles par usage : la nuee est bornee par construction.
         magatama_per_volley = 3

         # DEPLACEMENT LUMINEUX (-04) : distance maximale, destination calculee cote serveur puis
         # validee (aucun bloc, aucune limite franchie, aucune zone protegee).
         travel_max_distance_blocks = 24.0

         # AVERTISSEMENT ET VOLEE (-05) : l'adversaire doit VOIR le signal avant l'impact, et la
         # rafale a un nombre d'impacts BORNE.
         charge_ticks = 20
         barrage_max_impacts = 6

         # BONUS (-06) : « Speed is Power » gagne un bonus borne quand la lumiere est active.
         speed_power_bonus_max = 12.0
         # Plafond d'amplificateur des bonus de lumiere : pas de cumul abusif.
         buff_max_amplifier = 2
         """;
   }
}
