package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class MobBalanceConfig {
   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "mob-balance.toml");
   private static boolean enabled = true;
   private static double vanillaHostileKeepChance = 0.65;
   private static boolean weakFactionSpawnsEnabled = true;
   private static int weakFactionDayCooldownSeconds = 18;
   private static int weakFactionNightCooldownSeconds = 7;
   private static double weakFactionDayAttemptChance = 0.35;
   private static double weakFactionNightAttemptChance = 0.85;
   private static int weakFactionMaxNearby = 18;
   private static int weakFactionMinGroup = 3;
   private static int weakFactionMaxGroup = 6;
   private static int weakFactionMinRadius = 24;
   private static int weakFactionMaxRadius = 52;
   private static boolean spawnBalanceEnabled = true;
   private static int spawnBalanceRadius = 96;
   private static double spawnBalanceMmnmShare = 0.49;
   private static double spawnBalanceAquacultureShare = 0.21;
   private static double spawnBalanceVanillaShare = 0.3;
   private static int spawnBalanceMaxManagedNearby = 80;
   private static boolean passiveCapsEnabled = true;
   private static int passiveCapRadius = 96;
   private static int vanillaPassiveMaxNearby = 12;
   private static int dugongMaxNearby = 10;
   private static boolean blockMariejoisVanillaPassives = true;
   private static boolean blockMariejoisDugongs = true;
   private static boolean mariejoisSpawnsEnabled = true;
   private static int mariejoisCooldownSeconds = 12;
   private static double mariejoisAttemptChance = 0.55;
   private static int mariejoisMinRadius = 24;
   private static int mariejoisMaxRadius = 56;
   private static int mariejoisVillagerMaxNearby = 12;
   private static int mariejoisCelestialDragonMaxNearby = 4;
   private static int mariejoisVillagerWeight = 8;
   private static int mariejoisCelestialDragonWeight = 1;
   private static int mariejoisRedLineCooldownSeconds = 4;
   private static double mariejoisRedLineAttemptChance = 0.95;
   private static int mariejoisRedLineVillagerMaxNearby = 16;
   private static int mariejoisRedLineCelestialDragonMaxNearby = 24;
   private static int mariejoisRedLineVillagerWeight = 1;
   private static int mariejoisRedLineCelestialDragonWeight = 9;

   private MobBalanceConfig() {
   }

   public static void loadOrCreate() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }

         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load mob balance config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static double vanillaHostileKeepChance() {
      return vanillaHostileKeepChance;
   }

   public static boolean weakFactionSpawnsEnabled() {
      return weakFactionSpawnsEnabled;
   }

   public static int weakFactionCooldownTicks(boolean night) {
      int seconds = night ? weakFactionNightCooldownSeconds : weakFactionDayCooldownSeconds;
      return Math.max(20, seconds * 20);
   }

   public static double weakFactionAttemptChance(boolean night) {
      return night ? weakFactionNightAttemptChance : weakFactionDayAttemptChance;
   }

   public static int weakFactionMaxNearby() {
      return weakFactionMaxNearby;
   }

   public static int weakFactionMinGroup() {
      return weakFactionMinGroup;
   }

   public static int weakFactionMaxGroup() {
      return weakFactionMaxGroup;
   }

   public static int weakFactionMinRadius() {
      return weakFactionMinRadius;
   }

   public static int weakFactionMaxRadius() {
      return weakFactionMaxRadius;
   }

   public static boolean spawnBalanceEnabled() {
      return spawnBalanceEnabled;
   }

   public static int spawnBalanceRadius() {
      return spawnBalanceRadius;
   }

   public static double spawnBalanceMmnmShare() {
      return spawnBalanceMmnmShare;
   }

   public static double spawnBalanceAquacultureShare() {
      return spawnBalanceAquacultureShare;
   }

   public static double spawnBalanceVanillaShare() {
      return spawnBalanceVanillaShare;
   }

   public static int spawnBalanceMaxManagedNearby() {
      return spawnBalanceMaxManagedNearby;
   }

   public static boolean passiveCapsEnabled() {
      return passiveCapsEnabled;
   }

   public static int passiveCapRadius() {
      return passiveCapRadius;
   }

   public static int vanillaPassiveMaxNearby() {
      return vanillaPassiveMaxNearby;
   }

   public static int dugongMaxNearby() {
      return dugongMaxNearby;
   }

   public static boolean blockMariejoisVanillaPassives() {
      return blockMariejoisVanillaPassives;
   }

   public static boolean blockMariejoisDugongs() {
      return blockMariejoisDugongs;
   }

   public static boolean mariejoisSpawnsEnabled() {
      return mariejoisSpawnsEnabled;
   }

   public static int mariejoisCooldownTicks() {
      return Math.max(20, mariejoisCooldownSeconds * 20);
   }

   public static int mariejoisCooldownTicks(boolean redLineBoosted) {
      int seconds = redLineBoosted ? mariejoisRedLineCooldownSeconds : mariejoisCooldownSeconds;
      return Math.max(20, seconds * 20);
   }

   public static double mariejoisAttemptChance() {
      return mariejoisAttemptChance;
   }

   public static double mariejoisAttemptChance(boolean redLineBoosted) {
      return redLineBoosted ? mariejoisRedLineAttemptChance : mariejoisAttemptChance;
   }

   public static int mariejoisMinRadius() {
      return mariejoisMinRadius;
   }

   public static int mariejoisMaxRadius() {
      return mariejoisMaxRadius;
   }

   public static int mariejoisVillagerMaxNearby() {
      return mariejoisVillagerMaxNearby;
   }

   public static int mariejoisVillagerMaxNearby(boolean redLineBoosted) {
      return redLineBoosted ? mariejoisRedLineVillagerMaxNearby : mariejoisVillagerMaxNearby;
   }

   public static int mariejoisCelestialDragonMaxNearby() {
      return mariejoisCelestialDragonMaxNearby;
   }

   public static int mariejoisCelestialDragonMaxNearby(boolean redLineBoosted) {
      return redLineBoosted ? mariejoisRedLineCelestialDragonMaxNearby : mariejoisCelestialDragonMaxNearby;
   }

   public static int mariejoisVillagerWeight() {
      return mariejoisVillagerWeight;
   }

   public static int mariejoisVillagerWeight(boolean redLineBoosted) {
      return redLineBoosted ? mariejoisRedLineVillagerWeight : mariejoisVillagerWeight;
   }

   public static int mariejoisCelestialDragonWeight() {
      return mariejoisCelestialDragonWeight;
   }

   public static int mariejoisCelestialDragonWeight(boolean redLineBoosted) {
      return redLineBoosted ? mariejoisRedLineCelestialDragonWeight : mariejoisCelestialDragonWeight;
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (!line.isBlank() && line.contains("=")) {
            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");
            switch (key) {
               case "enabled":
                  enabled = Boolean.parseBoolean(value);
                  break;
               case "vanilla-hostile-keep-chance":
                  vanillaHostileKeepChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "weak-faction-spawns-enabled":
                  weakFactionSpawnsEnabled = Boolean.parseBoolean(value);
                  break;
               case "weak-faction-cooldown-seconds":
                  int seconds = clampInt(value, 5, 180);
                  weakFactionDayCooldownSeconds = seconds;
                  weakFactionNightCooldownSeconds = seconds;
                  break;
               case "weak-faction-day-cooldown-seconds":
                  weakFactionDayCooldownSeconds = clampInt(value, 5, 180);
                  break;
               case "weak-faction-night-cooldown-seconds":
                  weakFactionNightCooldownSeconds = clampInt(value, 5, 180);
                  break;
               case "weak-faction-attempt-chance":
                  double chance = clampDouble(value, 0.0, 1.0);
                  weakFactionDayAttemptChance = chance;
                  weakFactionNightAttemptChance = chance;
                  break;
               case "weak-faction-day-attempt-chance":
                  weakFactionDayAttemptChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "weak-faction-night-attempt-chance":
                  weakFactionNightAttemptChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "weak-faction-max-nearby":
                  weakFactionMaxNearby = clampInt(value, 1, 40);
                  break;
               case "weak-faction-min-group":
                  weakFactionMinGroup = clampInt(value, 1, 12);
                  break;
               case "weak-faction-max-group":
                  weakFactionMaxGroup = clampInt(value, weakFactionMinGroup, 16);
                  break;
               case "weak-faction-min-radius":
                  weakFactionMinRadius = clampInt(value, 12, 96);
                  break;
               case "weak-faction-max-radius":
                  weakFactionMaxRadius = clampInt(value, weakFactionMinRadius, 128);
                  break;
               case "spawn-balance-enabled":
                  spawnBalanceEnabled = Boolean.parseBoolean(value);
                  break;
               case "spawn-balance-radius":
                  spawnBalanceRadius = clampInt(value, 16, 192);
                  break;
               case "spawn-balance-mmnm-share":
                  spawnBalanceMmnmShare = clampDouble(value, 0.0, 1.0);
                  break;
               case "spawn-balance-aquaculture-share":
                  spawnBalanceAquacultureShare = clampDouble(value, 0.0, 1.0);
                  break;
               case "spawn-balance-vanilla-share":
                  spawnBalanceVanillaShare = clampDouble(value, 0.0, 1.0);
                  break;
               case "spawn-balance-max-managed-nearby":
                  spawnBalanceMaxManagedNearby = clampInt(value, 1, 300);
                  break;
               case "passive-caps-enabled":
                  passiveCapsEnabled = Boolean.parseBoolean(value);
                  break;
               case "passive-cap-radius":
                  passiveCapRadius = clampInt(value, 16, 192);
                  break;
               case "vanilla-passive-max-nearby":
                  vanillaPassiveMaxNearby = clampInt(value, 1, 200);
                  break;
               case "dugong-max-nearby":
                  dugongMaxNearby = clampInt(value, 1, 100);
                  break;
               case "block-mariejois-vanilla-passives":
                  blockMariejoisVanillaPassives = Boolean.parseBoolean(value);
                  break;
               case "block-mariejois-dugongs":
                  blockMariejoisDugongs = Boolean.parseBoolean(value);
                  break;
               case "mariejois-spawns-enabled":
                  mariejoisSpawnsEnabled = Boolean.parseBoolean(value);
                  break;
               case "mariejois-cooldown-seconds":
                  mariejoisCooldownSeconds = clampInt(value, 5, 180);
                  break;
               case "mariejois-attempt-chance":
                  mariejoisAttemptChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "mariejois-min-radius":
                  mariejoisMinRadius = clampInt(value, 12, 96);
                  break;
               case "mariejois-max-radius":
                  mariejoisMaxRadius = clampInt(value, mariejoisMinRadius, 128);
                  break;
               case "mariejois-villager-max-nearby":
                  mariejoisVillagerMaxNearby = clampInt(value, 1, 80);
                  break;
               case "mariejois-celestial-dragon-max-nearby":
                  mariejoisCelestialDragonMaxNearby = clampInt(value, 1, 40);
                  break;
               case "mariejois-villager-weight":
                  mariejoisVillagerWeight = clampInt(value, 0, 100);
                  break;
               case "mariejois-celestial-dragon-weight":
                  mariejoisCelestialDragonWeight = clampInt(value, 0, 100);
                  break;
               case "mariejois-red-line-cooldown-seconds":
                  mariejoisRedLineCooldownSeconds = clampInt(value, 1, 180);
                  break;
               case "mariejois-red-line-attempt-chance":
                  mariejoisRedLineAttemptChance = clampDouble(value, 0.0, 1.0);
                  break;
               case "mariejois-red-line-villager-max-nearby":
                  mariejoisRedLineVillagerMaxNearby = clampInt(value, 1, 120);
                  break;
               case "mariejois-red-line-celestial-dragon-max-nearby":
                  mariejoisRedLineCelestialDragonMaxNearby = clampInt(value, 1, 120);
                  break;
               case "mariejois-red-line-villager-weight":
                  mariejoisRedLineVillagerWeight = clampInt(value, 0, 100);
                  break;
               case "mariejois-red-line-celestial-dragon-weight":
                  mariejoisRedLineCelestialDragonWeight = clampInt(value, 0, 100);
            }
         }
      }
   }

   private static int clampInt(String raw, int min, int max) {
      int value = Integer.parseInt(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static double clampDouble(String raw, double min, double max) {
      double value = Double.parseDouble(raw);
      return Math.max(min, Math.min(max, value));
   }

   private static String defaultConfig() {
      return "# Controla o equilibrio entre monstros vanilla e NPCs fracos do Mine Piece.\nenabled = true\n\n# Chave antiga mantida por compatibilidade; o balanceamento por familia e o controle ativo.\nvanilla-hostile-keep-chance = 0.65\n\nweak-faction-spawns-enabled = true\nweak-faction-day-cooldown-seconds = 18\nweak-faction-night-cooldown-seconds = 7\nweak-faction-day-attempt-chance = 0.35\nweak-faction-night-attempt-chance = 0.85\nweak-faction-max-nearby = 18\nweak-faction-min-group = 3\nweak-faction-max-group = 6\nweak-faction-min-radius = 24\nweak-faction-max-radius = 52\n\n# Balanceia spawns automaticos gerados pelo mundo. Nao afeta comando, spawn egg, breeding, nomeados ou persistentes.\nspawn-balance-enabled = true\nspawn-balance-radius = 96\nspawn-balance-mmnm-share = 0.49\nspawn-balance-aquaculture-share = 0.21\nspawn-balance-vanilla-share = 0.30\nspawn-balance-max-managed-nearby = 80\n\n# Caps contra excesso de passivos naturais. Nao afeta comando, spawn egg ou breeding.\npassive-caps-enabled = true\npassive-cap-radius = 96\nvanilla-passive-max-nearby = 12\ndugong-max-nearby = 10\n\n# Chaves antigas mantidas por compatibilidade; o bloqueio duro foi substituido pelo balanceamento.\nblock-mariejois-vanilla-passives = false\nblock-mariejois-dugongs = false\nmariejois-spawns-enabled = true\nmariejois-cooldown-seconds = 12\nmariejois-attempt-chance = 0.55\nmariejois-min-radius = 24\nmariejois-max-radius = 56\nmariejois-villager-max-nearby = 12\nmariejois-celestial-dragon-max-nearby = 4\nmariejois-villager-weight = 8\nmariejois-celestial-dragon-weight = 1\n\n# Boost forte apenas na Red Line dentro de Mariejois.\nmariejois-red-line-cooldown-seconds = 4\nmariejois-red-line-attempt-chance = 0.95\nmariejois-red-line-villager-max-nearby = 16\nmariejois-red-line-celestial-dragon-max-nearby = 24\nmariejois-red-line-villager-weight = 1\nmariejois-red-line-celestial-dragon-weight = 9\n";
   }
}
