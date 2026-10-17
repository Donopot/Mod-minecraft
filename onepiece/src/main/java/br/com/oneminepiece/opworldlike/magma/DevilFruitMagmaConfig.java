package br.com.oneminepiece.opworldlike.magma;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages du magma (FRUIT-MAGU_MAGU-03/04/05).
 *
 * <p>Le plan demande des « nappes temporaires de magma avec <b>limites de durée et population</b> »,
 * critère « zones nettoyées à expiration et <b>limites respectées lors d'utilisations simultanées</b> »,
 * et de rendre « toute destruction du terrain <b>configurable</b> et compatible avec les protections ».
 * Tout est donc réglé <b>ici</b>, jamais par des constantes dispersées dans les techniques.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-magma.toml}</p>
 */
public final class DevilFruitMagmaConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-magma.toml");

   private static boolean poolsEnabled = true;
   private static int maxPoolsPerPlayer = 3;
   private static int poolLifetimeTicks = 200;
   private static double poolRadius = 3.0;
   private static double poolMaxResistance = 10.0;
   private static int magmaBurnTicks = 160;
   private static double magmaBurnDamagePerSecond = 2.0;

   private DevilFruitMagmaConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load magma config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /**
    * Les nappes de magma sont-elles posées ?
    *
    * <p>Interrupteur de destruction du terrain demandé par le plan (-05) : {@code false} et aucune
    * technique ne modifie le sol — la brûlure reste, la nappe non.</p>
    */
   public static boolean poolsEnabled() {
      return poolsEnabled;
   }

   /**
    * Nombre de nappes simultanées par porteur.
    *
    * <p>C'est la « limite de population » du plan : au-delà, la plus ancienne est <b>nettoyée
    * immédiatement</b> — le terrain de la première est rendu avant que la nouvelle n'apparaisse.</p>
    */
   public static int maxPoolsPerPlayer() {
      return maxPoolsPerPlayer;
   }

   /** Durée de vie d'une nappe, en ticks. */
   public static int poolLifetimeTicks() {
      return poolLifetimeTicks;
   }

   /** Rayon d'une nappe, en blocs. */
   public static double poolRadius() {
      return poolRadius;
   }

   /** Résistance maximale d'un bloc encore remplaçable par du magma. */
   public static double poolMaxResistance() {
      return poolMaxResistance;
   }

   /** Durée de la brûlure de magma, en ticks — la valeur qui la distingue d'une brûlure de feu. */
   public static int magmaBurnTicks() {
      return magmaBurnTicks;
   }

   /** Dégâts de la brûlure de magma, par seconde (20 ticks). */
   public static double magmaBurnDamagePerSecond() {
      return magmaBurnDamagePerSecond;
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
               case "pools_enabled" -> poolsEnabled = Boolean.parseBoolean(value);
               case "max_pools_per_player" -> maxPoolsPerPlayer = Math.max(0, Integer.parseInt(value));
               case "pool_lifetime_ticks" -> poolLifetimeTicks = Math.max(20, Integer.parseInt(value));
               case "pool_radius" -> poolRadius = Math.max(1.0, Double.parseDouble(value));
               case "pool_max_resistance" -> poolMaxResistance = Math.max(0.0, Double.parseDouble(value));
               case "magma_burn_ticks" -> magmaBurnTicks = Math.max(20, Integer.parseInt(value));
               case "magma_burn_damage_per_second" -> magmaBurnDamagePerSecond = Math.max(0.0, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Magma config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Magma du Fruit Magu Magu (FRUIT-MAGU_MAGU-03/04/05)

         # Destruction du terrain : false et AUCUNE technique ne modifie le sol.
         pools_enabled = true

         # Nombre de nappes simultanees par porteur ; au-dela, la plus ancienne est nettoyee.
         max_pools_per_player = 3

         # Duree de vie d'une nappe, en ticks (20 ticks = 1 seconde).
         pool_lifetime_ticks = 200

         # Rayon d'une nappe, en blocs.
         pool_radius = 3.0

         # Resistance maximale d'un bloc encore remplacable par du magma.
         # 10 accepte la pierre (6) et la terre (0.5) -- le terrain normal -- et refuse
         # l'obsidienne (1200) et le bedrock : la nappe ne perce pas une base fortifiee.
         pool_max_resistance = 10.0

         # Brulure de magma : plus longue que le feu, et surtout NON eteinte par l'eau.
         magma_burn_ticks = 160
         magma_burn_damage_per_second = 2.0

         # Dans tous les cas, les zones protegees (claims d'ile et zones declarees dans
         # devilfruit-explosion.toml) sont refusees : la protection passe avant la technique.
         """;
   }
}
