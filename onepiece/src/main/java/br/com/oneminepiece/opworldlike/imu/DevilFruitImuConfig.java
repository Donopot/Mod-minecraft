package br.com.oneminepiece.opworldlike.imu;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages du Fruit Imu (FRUIT-IMU-04/05/06).
 *
 * <p><b>Avertissement de conception, volontairement en tête de fichier</b> : l'identité de ce Fruit
 * n'est <b>pas révélée</b> dans l'œuvre. Rien ici ne prétend reconstituer le canon. Le plan demande
 * explicitement de « définir la nature du pouvoir souhaité » et de « <b>ne pas inventer une fidélité
 * au canon</b> » : tout ce qui suit est une <b>décision de mod</b>, documentée dans
 * {@code docs/fruits/IMU.md}.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-imu.toml}</p>
 */
public final class DevilFruitImuConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-imu.toml");

   private static int veilDurationTicks = 200;
   private static int veilRefreshTicks = 160;
   private static double veilArmorBonus = 4.0;
   private static boolean veilEnabled = true;

   private DevilFruitImuConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load imu config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée d'une application du voile, en ticks — chaque application reste bornée. */
   public static int veilDurationTicks() {
      return veilDurationTicks;
   }

   /** Intervalle de rafraîchissement du voile, en ticks. */
   public static int veilRefreshTicks() {
      return Math.min(veilRefreshTicks,Math.max(1,veilDurationTicks-1));
   }

   /** Bonus d'armure du voile. */
   public static double veilArmorBonus() {
      return veilArmorBonus;
   }

   /** Le passif est-il actif ? {@code false} le désactive sans toucher au reste du Fruit. */
   public static boolean veilEnabled() {
      return veilEnabled;
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
               case "veil_duration_ticks" -> veilDurationTicks = Math.min(12000, Math.max(20, Integer.parseInt(value)));
               case "veil_refresh_ticks" -> veilRefreshTicks = Math.min(12000, Math.max(20, Integer.parseInt(value)));
               case "veil_armor_bonus" -> { double parsed=Double.parseDouble(value);if(Double.isFinite(parsed))veilArmorBonus=Math.min(40,Math.max(0,parsed)); }
               case "veil_enabled" -> veilEnabled = Boolean.parseBoolean(value);
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Imu config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Fruit Imu (FRUIT-IMU-04/05/06)

         # L'identite de ce Fruit n'est PAS revelee dans l'oeuvre. Rien dans ce fichier ne pretend
         # reconstituer le canon : le plan demande explicitement de ne pas inventer une fidelite
         # au canon. Tout est une DECISION DE MOD, documentee dans docs/fruits/IMU.md.

         # Passif unique du contrat : un VOILE qui protege le porteur tant qu'il detient le Fruit.
         # Chaque application est BORNEE en duree, et rafraichie cycliquement.
         veil_enabled = true
         veil_duration_ticks = 200
         veil_refresh_ticks = 160
         veil_armor_bonus = 4.0

         # Le voile utilise un effet MARQUEUR dedie et un modificateur d'attribut IDENTIFIE :
         # la revocation ne peut donc retirer que ce que ce passif a pose, jamais la resistance
         # d'une autre origine.
         """;
   }
}
