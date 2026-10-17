package br.com.oneminepiece.opworldlike.mochi;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Mochi Mochi no Mi (FRUIT-MOCHI_MOCHI-04/05).
 *
 * <p>Le plan : « ajouter un <b>état collant</b> avec <b>libération</b> et <b>durée maximale</b> »,
 * critère « une cible touchée est <b>ralentie ou immobilisée</b> selon le contrat puis <b>libérée</b> » ;
 * puis « créer des <b>constructions temporaires</b> de mochi avec <b>population limitée</b> », critère
 * « les constructions <b>expirent</b> et ne permettent pas de <b>bloquer indéfiniment</b> des
 * joueurs ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-mochi.toml}</p>
 */
public final class DevilFruitMochiConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-mochi.toml");

   private static int stickyDurationTicks = 100;
   private static int stickyImmobilizeAfterTicks = 60;
   private static double stickyRadius = 3.0;
   private static int constructionMaxBlocks = 24;
   private static int constructionMaxActive = 3;
   private static int constructionLifetimeTicks = 200;

   private DevilFruitMochiConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load mochi config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /**
    * <b>Durée MAXIMALE de l'état collant.</b>
    *
    * <p>Le mot « maximale » est le cœur du critère : une cible collante doit être <b>libérée</b>, et une
    * durée est le seul mécanisme qui le garantit sans dépendre du bon vouloir de quelqu'un. Si le
    * porteur se déconnecte, meurt, change de dimension ou perd son Fruit, la cible se décolle
    * <b>d'elle-même</b>.</p>
    */
   public static int stickyDurationTicks() {
      return stickyDurationTicks;
   }

   /** Délai avant immobilisation complète (avant : ralentissement). */
   public static int stickyImmobilizeAfterTicks() {
      return stickyImmobilizeAfterTicks;
   }

   /** Rayon de la zone collante. */
   public static double stickyRadius() {
      return stickyRadius;
   }

   /** Plafond de blocs posés par une construction. */
   public static int constructionMaxBlocks() {
      return constructionMaxBlocks;
   }

   /**
    * <b>Population maximale de constructions simultanées.</b>
    *
    * <p>Le plan demande une « population limitée » : sans plafond, un porteur patient pourrait couvrir
    * une zone de blocs indestructibles et <b>enfermer</b> des joueurs. Le plafond rend la construction
    * <b>tactique</b> au lieu d'être un chantier.</p>
    */
   public static int constructionMaxActive() {
      return constructionMaxActive;
   }

   /** Durée de vie d'une construction. */
   public static int constructionLifetimeTicks() {
      return constructionLifetimeTicks;
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
               case "sticky_duration_ticks" -> stickyDurationTicks = Math.max(20, Integer.parseInt(value));
               case "sticky_immobilize_after_ticks" ->
                  stickyImmobilizeAfterTicks = Math.max(0, Integer.parseInt(value));
               case "sticky_radius" -> stickyRadius = Math.max(0.5, Double.parseDouble(value));
               case "construction_max_blocks" ->
                  constructionMaxBlocks = Math.max(1, Integer.parseInt(value));
               case "construction_max_active" ->
                  constructionMaxActive = Math.max(1, Integer.parseInt(value));
               case "construction_lifetime_ticks" ->
                  constructionLifetimeTicks = Math.max(20, Integer.parseInt(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Mochi config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
      stickyImmobilizeAfterTicks = Math.min(stickyImmobilizeAfterTicks, stickyDurationTicks);
   }

   private static String defaultConfig() {
      return """
         # Mochi Mochi no Mi (FRUIT-MOCHI_MOCHI-04/05)

         # ETAT COLLANT : duree MAXIMALE (la cible est TOUJOURS liberee) puis liberation.
         # Ralentissement d'abord, immobilisation ensuite : la contrainte est graduee.
         sticky_duration_ticks = 100
         sticky_immobilize_after_ticks = 60
         sticky_radius = 3.0

         # CONSTRUCTIONS TEMPORAIRES : population limitee + expiration. Sans plafond, un porteur
         # patient couvrirait une zone de blocs et pourrait ENFERMER des joueurs.
         construction_max_blocks = 24
         construction_max_active = 3
         construction_lifetime_ticks = 200
         """;
   }
}
