package br.com.oneminepiece.opworldlike;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class OmpMessages {
   private static final Path PREFERENCES = Path.of("config", "opworldlike", "player-preferences.properties");
   private static final Properties PROPERTIES = new Properties();
   private static long loadedAt = Long.MIN_VALUE;

   private OmpMessages() {
   }

   public static Component component(ServerPlayer player, String pt, String en) {
      return Component.literal(text(player, pt, en));
   }

   public static String text(ServerPlayer player, String pt, String en) {
      return language(player) == OmpMessages.Language.EN_US ? en : pt;
   }

   public static OmpMessages.Language language(ServerPlayer player) {
      if (player == null) {
         return OmpMessages.Language.PT_BR;
      }

      loadIfNeeded();
      String prefix = "players." + player.getUUID().toString().toLowerCase(Locale.ROOT) + ".";
      String preference = PROPERTIES.getProperty(prefix + "language", "auto");
      if (preference.equalsIgnoreCase("en_US")) {
         return OmpMessages.Language.EN_US;
      }

      if (preference.equalsIgnoreCase("pt_BR")) {
         return OmpMessages.Language.PT_BR;
      }

      String effective = PROPERTIES.getProperty(prefix + "effective", "pt_BR");
      return effective.toLowerCase(Locale.ROOT).startsWith("en") ? OmpMessages.Language.EN_US : OmpMessages.Language.PT_BR;
   }

   private static synchronized void loadIfNeeded() {
      try {
         long modifiedAt = Files.isRegularFile(PREFERENCES) ? Files.getLastModifiedTime(PREFERENCES).toMillis() : Long.MIN_VALUE;
         if (modifiedAt == loadedAt) {
            return;
         }

         PROPERTIES.clear();
         loadedAt = modifiedAt;
         if (modifiedAt == Long.MIN_VALUE) {
            return;
         }

         try (InputStream input = Files.newInputStream(PREFERENCES)) {
            PROPERTIES.load(input);
         }
      } catch (IOException ex) {
         OmpWorld.LOGGER.warn("Could not load player language preferences from {}", PREFERENCES, ex);
      }
   }

   public enum Language {
      PT_BR,
      EN_US;
   }
}
