package br.com.oneminepiece.opworldlike.compat;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.resources.Identifier;
import net.fabricmc.loader.api.FabricLoader;

public final class MinePieceAddonCompat {
   private static final List<String> OPTIONAL_ADDON_MOD_IDS = List.of("hakminepiece", "ultimateminepiece", "minepieceaddon", "minepiece_w");
   private static final List<String> MINEPIECE_FAMILY_NAMESPACES = List.of("minepiece", "hakminepiece", "ultimateminepiece", "minepieceaddon", "minepiece_w");
   private static final List<String> MINEPIECE_FAMILY_CLASS_PREFIXES = List.of(
      "net.mcreator.minepiece.", "net.mcreator.hakminepiece.", "net.mcreator.ultimateminepiece.", "net.mcreator.minepieceaddon.", "net.mikku.minepiecew."
   );
   private static final List<Identifier> HAKMINEPIECE_BOSSES = List.of(
      id("hakminepiece", "crocodile"),
      id("hakminepiece", "crocodile_cross_guild"),
      id("hakminepiece", "gerd"),
      id("hakminepiece", "rocks"),
      id("hakminepiece", "roger_2"),
      id("hakminepiece", "strawberry"),
      id("hakminepiece", "guernika_cp_0"),
      id("hakminepiece", "luffy_film_red"),
      id("hakminepiece", "jaygarcia_saturn"),
      id("hakminepiece", "kizaru_clone")
   );
   private static final List<Identifier> MINEPIECEADDON_BOSSES = List.of(
      id("minepieceaddon", "luffy_ssg"), id("minepieceaddon", "zoro_ssg"), id("minepieceaddon", "sanji_ssg")
   );

   private MinePieceAddonCompat() {
   }

   public static void logLoadedAddons() {
      List<String> loaded = OPTIONAL_ADDON_MOD_IDS.stream().filter(MinePieceAddonCompat::isLoaded).toList();
      if (loaded.isEmpty()) {
         OmpWorld.LOGGER.info("Mine Piece optional addons: none loaded.");
      } else {
         OmpWorld.LOGGER.info("Mine Piece optional addons loaded: {}.", String.join(", ", loaded));
      }
   }

   public static boolean isLoaded(String modId) {
      return FabricLoader.getInstance().isModLoaded(modId);
   }

   public static List<Identifier> optionalBossEntityIds() {
      List<Identifier> bosses = new ArrayList<>();
      if (isLoaded("hakminepiece")) {
         bosses.addAll(HAKMINEPIECE_BOSSES);
      }

      if (isLoaded("minepieceaddon")) {
         bosses.addAll(MINEPIECEADDON_BOSSES);
      }

      return List.copyOf(bosses);
   }

   public static boolean isMinePieceFamily(Identifier id) {
      return id != null && isMinePieceFamilyNamespace(id.getNamespace());
   }

   public static boolean isMinePieceFamilyNamespace(String namespace) {
      return namespace != null && MINEPIECE_FAMILY_NAMESPACES.contains(namespace.toLowerCase(Locale.ROOT));
   }

   public static boolean isMinePieceFamilyClass(String className) {
      if (className == null) {
         return false;
      }

      String normalized = className.toLowerCase(Locale.ROOT);
      return MINEPIECE_FAMILY_CLASS_PREFIXES.stream().anyMatch(normalized::startsWith);
   }

   public static boolean isMinePieceFamilyOnStack() {
      for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
         if (isMinePieceFamilyClass(element.getClassName())) {
            return true;
         }
      }

      return false;
   }

   private static Identifier id(String namespace, String path) {
      return Identifier.fromNamespaceAndPath(namespace, path);
   }
}
