package br.com.oneminepiece.opworldlike.ope;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Ope Ope no Mi (FRUIT-OPE_OPE-03/04/05).
 *
 * <p>Le plan demande trois choses que <b>ver15 ne contient pas</b> : « <b>Implémenter ROOM</b> avec
 * centre, rayon, durée et affichage pour les observateurs », « Ajouter un <b>échange de positions</b> à
 * l'intérieur de ROOM avec <b>validation des destinations</b> », et « Définir une <b>découpe
 * temporaire</b> ou un effet équivalent <b>sans corruption des entités</b> ». La source ne déclare
 * qu'une technique ({@code stoneopeope}, mêlée), donc <b>aucune valeur n'est extraite d'un portage</b> :
 * les nombres ci-dessous sont <b>choisis</b>, déclarés comme tels, et tous réglables ici sans
 * recompiler. Ils sont calibrés sur les échelles déjà en vigueur dans le mod (réserve d'énergie 200,
 * techniques à 40–150, durées de maintien de 100 à 300 ticks).</p>
 *
 * <p>Chaque grandeur soumise au plan a sa <b>borne dure dans le code</b> : un rayon ou une durée
 * délirants tapés dans ce fichier ne peuvent pas produire une ROOM infinie ni une section éternelle.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-ope.toml}</p>
 */
public final class DevilFruitOpeConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-ope.toml");

   private static double roomRadiusBlocks = 8.0;
   private static int roomLifetimeTicks = 200;
   private static int roomParticlesPerTick = 48;

   private static double shamblesMaxDistanceBlocks = 8.0;

   private static int amputationDurationTicks = 160;
   private static int amputationMaxTargets = 4;

   // ---- Plafonds durs, DANS LE CODE : une valeur absurde du fichier ne doit pas passer ----
   /** Rayon maximal absolu d'une ROOM, quoi qu'on écrive dans le fichier. */
   public static final double HARD_MAX_RADIUS = 24.0;

   /** Durée de vie maximale absolue d'une ROOM. */
   public static final int HARD_MAX_LIFETIME = 600;

   /** Durée maximale absolue d'une section temporaire. */
   public static final int HARD_MAX_AMPUTATION = 400;

   private DevilFruitOpeConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load ope config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Rayon de la ROOM, borné par {@link #HARD_MAX_RADIUS}. */
   public static double roomRadiusBlocks() {
      return Math.max(1.0, Math.min(HARD_MAX_RADIUS, roomRadiusBlocks));
   }

   /** Durée de vie de la ROOM, bornée par {@link #HARD_MAX_LIFETIME}. */
   public static int roomLifetimeTicks() {
      return Math.max(20, Math.min(HARD_MAX_LIFETIME, roomLifetimeTicks));
   }

   /** Nombre de particules de contour émises par tick — l'affichage reste borné. */
   public static int roomParticlesPerTick() {
      return Math.max(4, Math.min(200, roomParticlesPerTick));
   }

   /** Distance maximale entre les deux positions échangées. */
   public static double shamblesMaxDistanceBlocks() {
      return Math.max(1.0, Math.min(HARD_MAX_RADIUS, shamblesMaxDistanceBlocks));
   }

   /** Durée d'une section temporaire, bornée par {@link #HARD_MAX_AMPUTATION}. */
   public static int amputationDurationTicks() {
      return Math.max(20, Math.min(HARD_MAX_AMPUTATION, amputationDurationTicks));
   }

   /** Nombre maximal de cibles sectionnées à la fois par un même porteur. */
   public static int amputationMaxTargets() {
      return Math.max(1, Math.min(16, amputationMaxTargets));
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
               case "room_radius_blocks" -> roomRadiusBlocks = Double.parseDouble(value);
               case "room_lifetime_ticks" -> roomLifetimeTicks = Integer.parseInt(value);
               case "room_particles_per_tick" -> roomParticlesPerTick = Integer.parseInt(value);
               case "shambles_max_distance_blocks" -> shamblesMaxDistanceBlocks = Double.parseDouble(value);
               case "amputation_duration_ticks" -> amputationDurationTicks = Integer.parseInt(value);
               case "amputation_max_targets" -> amputationMaxTargets = Integer.parseInt(value);
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Ope config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Ope Ope no Mi (FRUIT-OPE_OPE-03/04/05)
         #
         # ATTENTION : ces valeurs sont CHOISIES (la source ver15 ne declare que Stone Ope Ope, une
         # melee : aucune ROOM, aucun echange, aucune decoupe). Elles sont bornees DANS LE CODE :
         # rayon <= 24, duree de ROOM <= 600, duree de section <= 400.

         # ROOM : centre sur le porteur, rayon et duree de vie.
         room_radius_blocks = 8.0
         room_lifetime_ticks = 200
         # Affichage borne : particules de contour emises par tick (visibles par les observateurs).
         room_particles_per_tick = 48

         # ECHANGE (Shambles) : distance maximale entre les deux positions echangees.
         shambles_max_distance_blocks = 8.0

         # DECOUPE TEMPORAIRE : duree de la section et nombre de cibles simultanees.
         # Les cibles sont RESTAUREES a l'expiration, a la mort ou a la suppression du pouvoir.
         amputation_duration_ticks = 160
         amputation_max_targets = 4
         """;
   }
}
