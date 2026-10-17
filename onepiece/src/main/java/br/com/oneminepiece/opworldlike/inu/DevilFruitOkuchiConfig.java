package br.com.oneminepiece.opworldlike.inu;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Réglages d'Inu Inu Okuchi No Makami (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-04/05).
 *
 * <p>Le plan demande un « <b>souffle glacé dirigé</b> et des <b>états de gel bornés</b> », critère
 * « souffle, gel et <b>collision</b> correspondent au contrat et <b>la cible est libérée</b> » ; puis une
 * « <b>défense de glace temporaire</b> avec <b>coût</b> et <b>exceptions</b> », critère « la défense
 * <b>expire sans supprimer d'autres bonus</b> du joueur ».</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-inu_okuchi.toml}</p>
 */
public final class DevilFruitOkuchiConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-inu_okuchi.toml");

   private static double breathRange = 10.0;
   private static double breathHalfAngleDegrees = 22.5;
   private static int breathFreezeTicks = 60;
   private static int breathRaySamples = 5;
   private static boolean breathBlockedByWalls = true;

   private static int defenseDurationTicks = 200;
   private static double defenseArmorBonus = 6.0;
   private static double defenseToughnessBonus = 2.0;
   private static boolean defenseMeltsInFire = true;

   private DevilFruitOkuchiConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load okuchi config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Portée du souffle, en blocs. */
   public static double breathRange() {
      return breathRange;
   }

   /** Demi-angle du cône de souffle, en degrés. */
   public static double breathHalfAngleDegrees() {
      return breathHalfAngleDegrees;
   }

   /** Durée de gel appliquée par le souffle, en ticks (bornée par le service de gel). */
   public static int breathFreezeTicks() {
      return breathFreezeTicks;
   }

   /** Nombre de rayons échantillonnés dans le cône (dessin et frappe utilisent les mêmes). */
   public static int breathRaySamples() {
      return breathRaySamples;
   }

   /**
    * Un mur arrête-t-il le souffle ?
    *
    * <p>C'est la « collision » du critère : un souffle est une matière, il ne traverse pas la pierre.
    * Sans cela, on gèlerait des cibles à travers les murs — visiblement faux, et injuste.</p>
    */
   public static boolean breathBlockedByWalls() {
      return breathBlockedByWalls;
   }

   /** Durée de la défense de glace, en ticks. */
   public static int defenseDurationTicks() {
      return defenseDurationTicks;
   }

   /** Bonus d'armure de la défense. */
   public static double defenseArmorBonus() {
      return defenseArmorBonus;
   }

   /** Bonus de résistance de la défense. */
   public static double defenseToughnessBonus() {
      return defenseToughnessBonus;
   }

   /**
    * Exception : la glace <b>fond au feu</b>.
    *
    * <p>Une défense sans exception serait une invulnérabilité ; le plan demande explicitement des
    * <b>exceptions</b>. Celle-ci est thématique et vérifiable : le porteur en feu perd sa défense.</p>
    */
   public static boolean defenseMeltsInFire() {
      return defenseMeltsInFire;
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
               case "breath_range" -> breathRange = Math.max(1.0, Double.parseDouble(value));
               case "breath_half_angle_degrees" ->
                  breathHalfAngleDegrees = Math.min(89.0, Math.max(1.0, Double.parseDouble(value)));
               case "breath_freeze_ticks" -> breathFreezeTicks = Math.max(20, Integer.parseInt(value));
               case "breath_ray_samples" -> breathRaySamples = Math.max(1, Integer.parseInt(value));
               case "breath_blocked_by_walls" -> breathBlockedByWalls = Boolean.parseBoolean(value);
               case "defense_duration_ticks" -> defenseDurationTicks = Math.max(20, Integer.parseInt(value));
               case "defense_armor_bonus" -> defenseArmorBonus = Math.max(0.0, Double.parseDouble(value));
               case "defense_toughness_bonus" -> defenseToughnessBonus = Math.max(0.0, Double.parseDouble(value));
               case "defense_melts_in_fire" -> defenseMeltsInFire = Boolean.parseBoolean(value);
               default -> {
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Okuchi config: valeur illisible pour « {} » ({}) — ligne ignorée.", key, value);
         }
      }
   }

   private static String defaultConfig() {
      return """
         # Inu Inu Okuchi No Makami (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-04/05)

         # SOUFFLE GLACE : cone dirige. Le meme cone sert a DESSINER et a FRAPPER, et un mur l'arrete
         # (c'est la "collision" demandee par le critere : un souffle ne traverse pas la pierre).
         breath_range = 10.0
         breath_half_angle_degrees = 22.5
         breath_ray_samples = 5
         breath_blocked_by_walls = true

         # Gel applique par le souffle. La duree reelle est bornee par le service de gel partage
         # (plafond dur + fenetre de liberte garantie) : la cible est TOUJOURS liberee.
         breath_freeze_ticks = 60

         # DEFENSE DE GLACE : temporaire, avec cout et EXCEPTIONS.
         defense_duration_ticks = 200
         defense_armor_bonus = 6.0
         defense_toughness_bonus = 2.0

         # Exception : la glace fond au feu. Une defense sans exception serait une invulnerabilite.
         defense_melts_in_fire = true
         """;
   }
}
