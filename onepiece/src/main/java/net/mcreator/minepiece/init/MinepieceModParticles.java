package net.mcreator.minepiece.init;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * <b>Les 42 particules du mod, reprises du JAR source</b> — définitions et textures copiées à
 * l'identique (tools/particle_port.py), aucune redessinée.
 *
 * <h2>Pourquoi elles existent toutes ici</h2>
 * <p>Le portage initial remplaçait les particules du mod par des particules vanilla, faute de les avoir
 * reprises : une perte de fidélité gratuite, les fichiers étant présents dans le jar source. Trois
 * seulement (dark, light_large, light_small) avaient été extraites au moment où Yami et Pika en avaient
 * besoin. L'ensemble est désormais là, sous les <b>NOMS D'ORIGINE</b>, pour que chaque technique puisse
 * employer la particule de la source au lieu d'un substitut.</p>
 *
 * <p>Les identifiants sont ceux des définitions du jar (minepiece:particle_dark), pas des noms
 * raccourcis : un nom inventé obligerait à tenir une table de correspondance, et c'est exactement le
 * genre d'écart qui fait diverger un portage.</p>
 *
 * <p>Enregistrement côté <b>serveur</b> (le type doit exister des deux côtés pour circuler dans un
 * paquet) ; l'apparence est enregistrée côté client dans client/MinepieceParticlesClient, avec les
 * paramètres LUS dans le bytecode des classes source.</p>
 */
public final class MinepieceModParticles {

   public static final String NAMESPACE = "minepiece";

   /** Tous les types déclarés, dans l'ordre de création — compté par l'auto-contrôle client. */
   private static final java.util.List<SimpleParticleType> TYPES = new java.util.ArrayList<>();

   /** particle_bat — durée 24, gravité 0.00, taille 0.20. */
   public static final SimpleParticleType BAT = register("particle_bat");

   /** particle_big_smoke — durée 59, gravité 0.00, taille 1.20. */
   public static final SimpleParticleType BIG_SMOKE = register("particle_big_smoke");

   /** particle_blue — durée 30, gravité 0.00, taille 0.10. */
   public static final SimpleParticleType BLUE = register("particle_blue");

   /** particle_broken_glass — durée 100, gravité 0.10, taille 0.15. */
   public static final SimpleParticleType BROKEN_GLASS = register("particle_broken_glass");

   /** particle_broken_glass_small — durée 100, gravité 0.30, taille 0.07. */
   public static final SimpleParticleType BROKEN_GLASS_SMALL = register("particle_broken_glass_small");

   /** particle_claws — durée 7, gravité 0.00, taille 1.20. */
   public static final SimpleParticleType CLAWS = register("particle_claws");

   /** particle_dark — durée 15, gravité 0.00, taille 0.80. */
   public static final SimpleParticleType DARK = register("particle_dark");

   /** particle_fangs — durée 7, gravité 0.00, taille 1.20. */
   public static final SimpleParticleType FANGS = register("particle_fangs");

   /** particle_fire_fly — durée 10, gravité 0.00, taille 0.10. */
   public static final SimpleParticleType FIRE_FLY = register("particle_fire_fly");

   /** particle_fire_spark — durée Math.max(1, 10 + level.getRandom().nextInt(6) - 3), gravité 0.10, taille 0.20. */
   public static final SimpleParticleType FIRE_SPARK = register("particle_fire_spark");

   /** particle_flame — durée 15, gravité 0.00, taille 1.60. */
   public static final SimpleParticleType FLAME = register("particle_flame");

   /** particle_flame_blue — durée 15, gravité 0.00, taille 1.60. */
   public static final SimpleParticleType FLAME_BLUE = register("particle_flame_blue");

   /** particle_flame_green — durée 15, gravité 0.00, taille 1.60. */
   public static final SimpleParticleType FLAME_GREEN = register("particle_flame_green");

   /** particle_flame_purple — durée 15, gravité 0.00, taille 0.60. */
   public static final SimpleParticleType FLAME_PURPLE = register("particle_flame_purple");

   /** particle_flame_red — durée 15, gravité 0.00, taille 0.60. */
   public static final SimpleParticleType FLAME_RED = register("particle_flame_red");

   /** particle_flame_small — durée 15, gravité 0.00, taille 0.40. */
   public static final SimpleParticleType FLAME_SMALL = register("particle_flame_small");

   /** particle_flame_yellow — durée 15, gravité 0.00, taille 0.60. */
   public static final SimpleParticleType FLAME_YELLOW = register("particle_flame_yellow");

   /** particle_flash_red — durée 5, gravité 0.00, taille 8.00. */
   public static final SimpleParticleType FLASH_RED = register("particle_flash_red");

   /** particle_flash_red_small — durée 5, gravité 0.00, taille 0.80. */
   public static final SimpleParticleType FLASH_RED_SMALL = register("particle_flash_red_small");

   /** particle_flower — durée Math.max(1, 60 + level.getRandom().nextInt(40) - 20), gravité 0.01, taille 0.05. */
   public static final SimpleParticleType FLOWER = register("particle_flower");

   /** particle_flower_pink — durée Math.max(1, 60 + level.getRandom().nextInt(40) - 20), gravité 0.01, taille 0.05. */
   public static final SimpleParticleType FLOWER_PINK = register("particle_flower_pink");

   /** particle_gravity — durée Math.max(1, 16 + level.getRandom().nextInt(8) - 4), gravité 10.00, taille 0.60. */
   public static final SimpleParticleType GRAVITY = register("particle_gravity");

   /** particle_ice — durée 24, gravité 0.00, taille 0.60. */
   public static final SimpleParticleType ICE = register("particle_ice");

   /** particle_light_large — durée 10, gravité 0.00, taille 6.40. */
   public static final SimpleParticleType LIGHT_LARGE = register("particle_light_large");

   /** particle_light_large_red — durée 11, gravité 0.00, taille 0.10. */
   public static final SimpleParticleType LIGHT_LARGE_RED = register("particle_light_large_red");

   /** particle_light_small — durée 5, gravité 0.00, taille 0.50. */
   public static final SimpleParticleType LIGHT_SMALL = register("particle_light_small");

   /** particle_magma — durée 24, gravité 0.10, taille 0.80. */
   public static final SimpleParticleType MAGMA = register("particle_magma");

   /** particle_negative — durée 20, gravité 0.10, taille 0.50. */
   public static final SimpleParticleType NEGATIVE = register("particle_negative");

   /** particle_note — durée 20, gravité 0.00, taille 0.50. */
   public static final SimpleParticleType NOTE = register("particle_note");

   /** particle_note_black — durée 21, gravité 0.00, taille 1.00. */
   public static final SimpleParticleType NOTE_BLACK = register("particle_note_black");

   /** particle_purple — durée Math.max(1, 16 + level.getRandom().nextInt(8) - 4), gravité 0.00, taille 0.60. */
   public static final SimpleParticleType PURPLE = register("particle_purple");

   /** particle_red — durée 30, gravité 0.00, taille 0.10. */
   public static final SimpleParticleType RED = register("particle_red");

   /** particle_sand — durée 24, gravité 0.00, taille 0.40. */
   public static final SimpleParticleType SAND = register("particle_sand");

   /** particle_shock_hit — durée 6, gravité 0.00, taille 0.80. */
   public static final SimpleParticleType SHOCK_HIT = register("particle_shock_hit");

   /** particle_shockwave — durée 7, gravité 0.00, taille 6.40. */
   public static final SimpleParticleType SHOCKWAVE = register("particle_shockwave");

   /** particle_slash — durée 10, gravité 0.00, taille 2.50. */
   public static final SimpleParticleType SLASH = register("particle_slash");

   /** particle_thunder_black — durée Math.max(1, 16 + level.getRandom().nextInt(8) - 4), gravité 0.00, taille 0.40. */
   public static final SimpleParticleType THUNDER_BLACK = register("particle_thunder_black");

   /** particle_thunder_blue — durée Math.max(1, 16 + level.getRandom().nextInt(8) - 4), gravité 0.00, taille 0.40. */
   public static final SimpleParticleType THUNDER_BLUE = register("particle_thunder_blue");

   /** particle_thunder_dark_blue — durée Math.max(1, 16 + level.getRandom().nextInt(8) - 4), gravité 0.00, taille 0.40. */
   public static final SimpleParticleType THUNDER_DARK_BLUE = register("particle_thunder_dark_blue");

   /** particle_thunder_light_blue — durée Math.max(1, 16 + level.getRandom().nextInt(8) - 4), gravité 0.00, taille 0.80. */
   public static final SimpleParticleType THUNDER_LIGHT_BLUE = register("particle_thunder_light_blue");

   /** particle_water — durée Math.max(1, 12 + level.getRandom().nextInt(8) - 4), gravité 1.00, taille 0.40. */
   public static final SimpleParticleType WATER = register("particle_water");

   /** particle_water_2 — durée Math.max(1, 12 + level.getRandom().nextInt(8) - 4), gravité 0.10, taille 1.20. */
   public static final SimpleParticleType WATER_2 = register("particle_water_2");

   private MinepieceModParticles() {
   }

   /** Force le chargement de la classe (donc l'enregistrement des types). */
   public static void register() {
      // Les champs s'enregistrent d'eux-mêmes à la charge de la classe.
   }

   /**
    * Le constructeur de SimpleParticleType est <b>protégé</b> : on l'atteint par une sous-classe
    * anonyme, idiome habituel, plutôt que de réimplémenter codec et streamCodec.
    */
   private static SimpleParticleType register(String chemin) {
      SimpleParticleType type = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
         Identifier.fromNamespaceAndPath(NAMESPACE, chemin), new SimpleParticleType(false) {
         });
      TYPES.add(type);
      return type;
   }

   /** Type brut par identifiant de définition, ou null si l'identifiant est inconnu. */
   public static ParticleType<?> byName(String nom) {
      return switch (nom) {
         case "particle_bat" -> BAT;
         case "particle_big_smoke" -> BIG_SMOKE;
         case "particle_blue" -> BLUE;
         case "particle_broken_glass" -> BROKEN_GLASS;
         case "particle_broken_glass_small" -> BROKEN_GLASS_SMALL;
         case "particle_claws" -> CLAWS;
         case "particle_dark" -> DARK;
         case "particle_fangs" -> FANGS;
         case "particle_fire_fly" -> FIRE_FLY;
         case "particle_fire_spark" -> FIRE_SPARK;
         case "particle_flame" -> FLAME;
         case "particle_flame_blue" -> FLAME_BLUE;
         case "particle_flame_green" -> FLAME_GREEN;
         case "particle_flame_purple" -> FLAME_PURPLE;
         case "particle_flame_red" -> FLAME_RED;
         case "particle_flame_small" -> FLAME_SMALL;
         case "particle_flame_yellow" -> FLAME_YELLOW;
         case "particle_flash_red" -> FLASH_RED;
         case "particle_flash_red_small" -> FLASH_RED_SMALL;
         case "particle_flower" -> FLOWER;
         case "particle_flower_pink" -> FLOWER_PINK;
         case "particle_gravity" -> GRAVITY;
         case "particle_ice" -> ICE;
         case "particle_light_large" -> LIGHT_LARGE;
         case "particle_light_large_red" -> LIGHT_LARGE_RED;
         case "particle_light_small" -> LIGHT_SMALL;
         case "particle_magma" -> MAGMA;
         case "particle_negative" -> NEGATIVE;
         case "particle_note" -> NOTE;
         case "particle_note_black" -> NOTE_BLACK;
         case "particle_purple" -> PURPLE;
         case "particle_red" -> RED;
         case "particle_sand" -> SAND;
         case "particle_shock_hit" -> SHOCK_HIT;
         case "particle_shockwave" -> SHOCKWAVE;
         case "particle_slash" -> SLASH;
         case "particle_thunder_black" -> THUNDER_BLACK;
         case "particle_thunder_blue" -> THUNDER_BLUE;
         case "particle_thunder_dark_blue" -> THUNDER_DARK_BLUE;
         case "particle_thunder_light_blue" -> THUNDER_LIGHT_BLUE;
         case "particle_water" -> WATER;
         case "particle_water_2" -> WATER_2;
         default -> null;
      };
   }
   /** Tous les types déclarés — l'auto-contrôle client en compte l'enregistrement. */
   public static java.util.List<SimpleParticleType> tousLesTypes() {
      return TYPES;
   }

}