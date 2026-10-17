package br.com.oneminepiece.opworldlike.explosion;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Réglages des explosions des Fruits (FRUIT-BOMU_BOMU-05).
 *
 * <p>Le plan demande de « configurer dégâts aux blocs, alliés et zones protégées » et que « les
 * réglages [soient] respectés lors de toutes les explosions du fruit ». Tout passe donc par ce
 * fichier, jamais par des constantes dispersées dans le code des techniques : une seule source de
 * vérité pour toutes les explosions du Fruit.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-explosion.toml}</p>
 */
public final class DevilFruitExplosionConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-explosion.toml");

   private static boolean blockDamage = false;
   private static boolean spareAllies = true;
   private static boolean ownerImmuneToOwn = true;
   private static double blockDamageResistanceLimit = 3.0;
   private static final List<String> PROTECTED_ZONES = new ArrayList<>();

   /**
    * Zones ajoutées par les <b>tests</b> uniquement — jamais persistées, jamais écrites dans le
    * fichier. Ce point d'entrée existe pour qu'un test puisse prouver qu'une zone protégée refuse
    * bien une écriture, sans modifier la configuration livrée.
    */
   private static final List<String> TEST_ZONES = new ArrayList<>();

   private DevilFruitExplosionConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load explosion config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   /** Les explosions du Fruit cassent-elles des blocs ? Faux par défaut : pas de grief involontaire. */
   public static boolean blockDamage() {
      return blockDamage;
   }

   /** Les coéquipiers et familiers sont-ils épargnés ? */
   public static boolean spareAllies() {
      return spareAllies;
   }

   /** Le porteur est-il épargné par <b>ses propres</b> explosions ? (Rien d'autre : cf. -04.) */
   public static boolean ownerImmuneToOwn() {
      return ownerImmuneToOwn;
   }

   /** Résistance maximale d'un bloc encore destructible par une explosion du Fruit. */
   public static double blockDamageResistanceLimit() {
      return blockDamageResistanceLimit;
   }

   /**
    * Zones protégées, au format {@code dimension:x1,y1,z1,x2,y2,z2}.
    *
    * <p>Aucune explosion du Fruit n'y détruit de bloc, quel que soit {@link #blockDamage()}.</p>
    */
   public static List<String> protectedZones() {
      return List.copyOf(PROTECTED_ZONES);
   }

   /** Ajoute une zone protégée pour la durée d'un test ({@code dimension@x1,y1,z1,x2,y2,z2}). */
   public static void addZoneForTest(String zone) {
      TEST_ZONES.add(zone);
   }

   /** Retire toutes les zones de test. */
   public static void clearTestZones() {
      TEST_ZONES.clear();
   }

   /** Vrai si cette position tombe dans une zone protégée déclarée par l'administrateur. */
   public static boolean isProtected(ServerLevel level, BlockPos pos) {
      String dimension = level.dimension().identifier().toString();
      for (String zone : PROTECTED_ZONES) {
         if (matches(dimension, pos, zone)) {
            return true;
         }
      }
      for (String zone : TEST_ZONES) {
         if (matches(dimension, pos, zone)) {
            return true;
         }
      }
      return false;
   }

   private static boolean matches(String dimension, BlockPos pos, String zone) {
      // Séparateur '@' et non ':' : l'identifiant de dimension contient déjà un deux-points
      // (minecraft:overworld), un découpage sur ':' lisait « minecraft » comme dimension.
      int cut = zone.indexOf('@');
      if (cut <= 0) {
         return false;
      }
      if (!zone.substring(0, cut).trim().equals(dimension)) {
         return false;
      }
      String[] parts = zone.substring(cut + 1).split(",");
      if (parts.length != 6) {
         return false;
      }
      try {
         int[] v = new int[6];
         for (int i = 0; i < 6; i++) {
            v[i] = Integer.parseInt(parts[i].trim());
         }
         return pos.getX() >= Math.min(v[0], v[3]) && pos.getX() <= Math.max(v[0], v[3])
            && pos.getY() >= Math.min(v[1], v[4]) && pos.getY() <= Math.max(v[1], v[4])
            && pos.getZ() >= Math.min(v[2], v[5]) && pos.getZ() <= Math.max(v[2], v[5]);
      } catch (NumberFormatException ex) {
         OmpWorld.LOGGER.warn("Zone protégée illisible : {}", zone);
         return false;
      }
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.trim();
         if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
            continue;
         }
         int cut = line.indexOf('=');
         String key = line.substring(0, cut).trim();
         String value = unquote(line.substring(cut + 1).trim());
         try {
            switch (key) {
               case "block-damage" -> blockDamage = Boolean.parseBoolean(value);
               case "spare-allies" -> spareAllies = Boolean.parseBoolean(value);
               case "owner-immune-to-own" -> ownerImmuneToOwn = Boolean.parseBoolean(value);
               case "block-damage-resistance-limit" -> blockDamageResistanceLimit = Double.parseDouble(value);
               case "protected-zones" -> {
                  PROTECTED_ZONES.clear();
                  for (String zone : value.split(";")) {
                     if (!zone.isBlank()) {
                        PROTECTED_ZONES.add(zone.trim());
                     }
                  }
               }
               default -> { }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Valeur invalide pour {} dans {} : {}", key, CONFIG_PATH, value);
         }
      }
   }

   private static String unquote(String value) {
      String v = value.trim();
      if ((v.startsWith("'") && v.endsWith("'")) || (v.startsWith("\"") && v.endsWith("\""))) {
         v = v.substring(1, v.length() - 1);
      }
      return v.trim();
   }

   private static String defaultConfig() {
      return """
         # Explosions des Fruits — FRUIT-BOMU_BOMU-05
         #
         # Un seul endroit règle TOUTES les explosions du fruit (contact, projectile, corporelle).

         # Les explosions cassent-elles des blocs ? Faux par défaut : une technique de combat ne doit
         # pas trouer le terrain sans que l'administrateur l'ait voulu.
         block-damage = false

         # Résistance maximale d'un bloc encore destructible quand block-damage = true.
         # 3.0 laisse passer terre/pierre/bois et épargne les blocs durs (obsidienne 1200).
         block-damage-resistance-limit = 3.0

         # Les coéquipiers (même équipe) et familiers du porteur sont épargnés.
         spare-allies = true

         # Le porteur est épargné par SES PROPRES explosions uniquement.
         # Attention : ceci n'accorde AUCUNE immunité générale — une explosion extérieure
         # (TNT, creeper, autre Fruit) le blesse normalement.
         owner-immune-to-own = true

         # Zones protégées supplémentaires, EN PLUS de la protection d'ile existante
         # (ProtectedSkillEffectGuard, qui protege deja les constructions des competences).
         # Format : "dimension@x1,y1,z1,x2,y2,z2" separees par ';'. Le separateur est '@'
         # car l'identifiant de dimension contient deja un ':'.
         protected-zones = ""
         """;
   }
}
