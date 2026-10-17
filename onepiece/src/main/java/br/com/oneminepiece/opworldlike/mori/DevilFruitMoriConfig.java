package br.com.oneminepiece.opworldlike.mori;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages de Mori Mori no Mi (FRUIT-MORI_MORI-03/04/05).
 *
 * <p>Le plan : « créer des <b>racines qui immobilisent temporairement</b> les cibles », critère
 * « l'immobilisation <b>respecte les résistances</b> et se <b>termine proprement</b> » ; « créer de la
 * <b>végétation temporaire</b> avec limites et <b>restauration</b> », critère « <b>aucun bloc permanent
 * involontaire</b> ni <b>modification de zone protégée</b> » ; « ajouter un <b>drainage de vie ou
 * énergie</b> avec <b>coût et plafond</b> », critère « <b>aucun gain infini</b> sur les invocations,
 * alliés ou cibles invulnérables ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-mori.toml}</p>
 */
public final class DevilFruitMoriConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-mori.toml");

   private static int rootDurationTicks = 100;
   private static int rootMaxTargets = 6;
   private static int vegetationMaxBlocks = 40;
   private static int vegetationLifetimeTicks = 300;
   private static double drainMaxHealth = 20.0;
   private static double drainRatio = 0.5;
   private static double drainEnergyCap = 60.0;

   private DevilFruitMoriConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load mori config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Durée MAXIMALE d'une immobilisation par racines. */
   public static int rootDurationTicks() {
      return rootDurationTicks;
   }

   /** Nombre maximal de cibles immobilisées à la fois. */
   public static int rootMaxTargets() {
      return rootMaxTargets;
   }

   /** Plafond de blocs de végétation par pousse. */
   public static int vegetationMaxBlocks() {
      return vegetationMaxBlocks;
   }

   /** Durée de vie de la végétation. */
   public static int vegetationLifetimeTicks() {
      return vegetationLifetimeTicks;
   }

   /**
    * <b>Plafond de vie drainable par cible et par usage.</b>
    *
    * <p>Le plan exige un « drainage de vie ou énergie avec coût et <b>plafond</b> » et « <b>aucun gain
    * infini</b> ». Ce plafond est ce qui empêche le drainage de devenir une pompe : sans lui, un porteur
    * pourrait vider une cible entière en un usage, et sur une cible qui se régénère (ou une invocation
    * bon marché), le gain serait sans fin.</p>
    */
   public static double drainMaxHealth() {
      return drainMaxHealth;
   }

   /** Part de la vie drainée convertie en énergie pour le porteur. */
   public static double drainRatio() {
      return drainRatio;
   }

   /** <b>Plafond d'énergie gagnable par usage</b> — la borne qui rend le gain fini. */
   public static double drainEnergyCap() {
      return drainEnergyCap;
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
               case "root_duration_ticks" -> rootDurationTicks = Math.max(20, Integer.parseInt(value));
               case "root_max_targets" -> rootMaxTargets = Math.max(1, Integer.parseInt(value));
               case "vegetation_max_blocks" ->
                  vegetationMaxBlocks = Math.max(1, Integer.parseInt(value));
               case "vegetation_lifetime_ticks" ->
                  vegetationLifetimeTicks = Math.max(20, Integer.parseInt(value));
               case "drain_max_health" -> drainMaxHealth = Math.max(0.0, Double.parseDouble(value));
               case "drain_ratio" -> drainRatio = Math.max(0.0, Double.parseDouble(value));
               case "drain_energy_cap" -> drainEnergyCap = Math.max(0.0, Double.parseDouble(value));
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Mori config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Mori Mori no Mi (FRUIT-MORI_MORI-03/04/05)

         # RACINES : duree MAXIMALE (l'immobilisation se TERMINE proprement) et respect des
         # resistances (l'effet est refuse si la cible ne peut pas etre affectee).
         root_duration_ticks = 100
         root_max_targets = 6

         # VEGETATION TEMPORAIRE : plafond et duree de vie. Aucun bloc PERMANENT involontaire,
         # aucune modification de zone protegee.
         vegetation_max_blocks = 40
         vegetation_lifetime_ticks = 300

         # DRAINAGE : coute, et surtout PLAFONNE. Sans plafond, le drainage deviendrait une pompe.
         drain_max_health = 20.0
         drain_ratio = 0.5
         drain_energy_cap = 60.0
         """;
   }
}
