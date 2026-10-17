package br.com.oneminepiece.opworldlike;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public final class OmpDiscordAnnouncements {
   private static final Path QUEUE = Path.of("config", "opworldlike", "discord-announcements.queue");
   private static final String EMBED_PREFIX = "EMBED|";

   private OmpDiscordAnnouncements() {
   }

   public static void queue(String message) {
      if (message != null && !message.isBlank()) {
         writeLine(message.replace('\r', ' ').replace('\n', ' ').trim());
      }
   }

   public static void queueEmbed(int color, String title, String description) {
      if (title != null && !title.isBlank() || description != null && !description.isBlank()) {
         int safeColor = Math.max(0, Math.min(16777215, color));
         writeLine("EMBED|" + safeColor + "|" + encode(title) + "|" + encode(description));
      }
   }

   private static String encode(String value) {
      String safeValue = value == null ? "" : value;
      return Base64.getUrlEncoder().withoutPadding().encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
   }

   private static void writeLine(String message) {
      if (message != null && !message.isBlank()) {
         try {
            Files.createDirectories(QUEUE.getParent());
            String line = message.trim() + System.lineSeparator();
            Files.writeString(QUEUE, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
         } catch (IOException ex) {
            OmpWorld.LOGGER.warn("Could not queue Discord announcement at {}", QUEUE, ex);
         }
      }
   }
}
