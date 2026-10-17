package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.OmpWorld;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;

public final class ClaimTitleExporter {
   private static final Path EXPORT_PATH = Path.of("config", "opworldlike", "claim-title-stats.properties");

   private ClaimTitleExporter() {
   }

   public static void export(ServerLevel level) {
      IslandClaimsData data = IslandClaimsData.get(level);
      Map<String, IslandDefinition> islands = OnePieceGeography.allClaimIslands(level.getSeed())
         .stream()
         .collect(Collectors.toMap(IslandDefinition::id, island -> (IslandDefinition)island, (left, right) -> left));
      Map<UUID, ClaimTitleExporter.PlayerTitleStats> players = new HashMap<>();

      for (IslandClaim claim : data.allClaims()) {
         if (claim.hasOwner()) {
            UUID target = titlePlayer(claim);
            if (target != null) {
               IslandDefinition island = islands.get(claim.islandId);
               String displayName = island == null ? claim.islandId : island.displayName();
               ClaimTitleExporter.PlayerTitleStats stats = players.computeIfAbsent(
                  target, uuid -> new ClaimTitleExporter.PlayerTitleStats(uuid, claim.ownerName)
               );
               stats.islandCount++;
               if (island != null && island.unique() && island.claimable()) {
                  stats.specialIslands.add(displayName);
               }
            }
         }
      }

      Properties properties = new Properties();
      properties.setProperty("generatedAt", Long.toString(System.currentTimeMillis()));
      properties.setProperty("playerCount", Integer.toString(players.size()));

      for (ClaimTitleExporter.PlayerTitleStats stats : players.values()) {
         String prefix = "players." + stats.uuid.toString().toLowerCase(Locale.ROOT) + ".";
         properties.setProperty(prefix + "name", stats.name == null ? "" : stats.name);
         properties.setProperty(prefix + "islandCount", Integer.toString(stats.islandCount));
         properties.setProperty(prefix + "specialCount", Integer.toString(stats.specialIslands.size()));
         properties.setProperty(prefix + "specialNames", String.join("|", stats.specialIslands));
      }

      try {
         Files.createDirectories(EXPORT_PATH.getParent());

         try (OutputStream output = Files.newOutputStream(EXPORT_PATH)) {
            properties.store(output, "OneMinePiece island title export");
         }
      } catch (IOException ex) {
         OmpWorld.LOGGER.warn("Could not export island title stats to {}", EXPORT_PATH, ex);
      }
   }

   private static UUID titlePlayer(IslandClaim claim) {
      if ("player".equals(claim.ownerType)) {
         return claim.ownerPlayer;
      } else {
         return "crew".equals(claim.ownerType) ? claim.ownerCaptain : null;
      }
   }

   private static final class PlayerTitleStats {
      private final UUID uuid;
      private final String name;
      private int islandCount;
      private final List<String> specialIslands = new ArrayList<>();

      private PlayerTitleStats(UUID uuid, String name) {
         this.uuid = uuid;
         this.name = name;
      }
   }
}
