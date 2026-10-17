package br.com.oneminepiece.opworldlike.quake;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages des ondes sismiques (FRUIT-GURA_GURA-03/05).
 *
 * <p>Le plan demande des « ondes sismiques qui se propagent » et des « fissures visuelles ou
 * temporaires, avec <b>destruction configurable</b> », critère « les fissures expirent et la
 * protection du terrain est respectée ». Tout ce qui est réglable l'est donc <b>ici</b>, dans un seul
 * fichier, et jamais par des constantes dispersées dans le code des techniques.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-quake.toml}</p>
 */
public final class DevilFruitQuakeConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-quake.toml");

   private static double waveSpeed = 2.0;
   private static double waveThickness = 1.5;
   private static double waveHalfHeight = 2.0;
   private static int crackLifetimeTicks = 200;
   private static boolean terrainDestruction = true;
   private static int maxCracksPerWave = 40;

   private DevilFruitQuakeConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load quake config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /**
    * Vitesse du front d'onde, en blocs par tick.
    *
    * <p>C'est ce qui transforme la frappe instantanée de ver15 en une onde : à 2 blocs/tick, une onde
    * de 48 blocs met 24 ticks à parcourir son chemin — le temps de la voir venir et de s'écarter.</p>
    */
   public static double waveSpeed() {
      return waveSpeed;
   }

   /** Épaisseur du front, en blocs : ce qui se trouve à cette distance du front est fauché. */
   public static double waveThickness() {
      return waveThickness;
   }

   /** Demi-hauteur du front : un joueur en hauteur est-il fauché ? */
   public static double waveHalfHeight() {
      return waveHalfHeight;
   }

   /** Durée de vie d'une fissure, en ticks. */
   public static int crackLifetimeTicks() {
      return crackLifetimeTicks;
   }

   /**
    * Destruction du terrain par les fissures.
    *
    * <p>{@code true} (défaut, décision de Donopot) : la fissure ouvre un <b>vrai trou</b> dans le
    * terrain — le tremblement de terre doit casser le sol, pas le décorer. {@code false} : simple
    * bloc fissuré visible, aucun trou. Les deux modifications sont <b>définitives</b>.
    * Seule l’attribution du pouvoir expire ; les <b>zones protégées sont refusées</b>.
    * Les budgets de fissures restent bornés.</p>
    */
   public static boolean terrainDestruction() {
      return terrainDestruction;
   }

   /** Plafond de fissures par onde — borne le coût d'une technique à longue portée. */
   public static int maxCracksPerWave() {
      return maxCracksPerWave;
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
               case "wave_speed" -> waveSpeed = Math.max(0.5, Double.parseDouble(value));
               case "wave_thickness" -> waveThickness = Math.max(0.25, Double.parseDouble(value));
               case "wave_half_height" -> waveHalfHeight = Math.max(0.25, Double.parseDouble(value));
               case "crack_lifetime_ticks" -> crackLifetimeTicks = Math.max(20, Integer.parseInt(value));
               case "terrain_destruction" -> terrainDestruction = Boolean.parseBoolean(value);
               case "max_cracks_per_wave" -> maxCracksPerWave = Math.max(0, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Quake config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Ondes sismiques du Fruit Gura Gura (FRUIT-GURA_GURA-03/05)

         # Vitesse du front d'onde, en blocs par tick. C'est ce qui rend l'onde esquivable.
         wave_speed = 2.0

         # Épaisseur du front et demi-hauteur : ce qui se trouve à cette distance est fauché.
         wave_thickness = 1.5
         wave_half_height = 2.0

         # Durée de vie d'une fissure, en ticks (20 ticks = 1 seconde).
         crack_lifetime_ticks = 200

         # true (défaut) : la fissure ouvre un VRAI TROU dans le sol, rebouché à l'expiration.
         # false : simple bloc fissuré visible, aucun trou.
         terrain_destruction = true

         # Plafond de fissures par onde.
         max_cracks_per_wave = 40

         # Dans les deux cas, les zones protégées (claims d'île et zones déclarées dans
         # devilfruit-explosion.toml) sont refusées : la protection du terrain passe avant.
         """;
   }
}
