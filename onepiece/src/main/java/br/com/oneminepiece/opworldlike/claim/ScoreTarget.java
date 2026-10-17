package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.integration.CrewInfo;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public record ScoreTarget(String type, String id, String displayName, UUID playerId, UUID captainId) {
   private static final String CREW = "crew";
   private static final String PLAYER = "player";

   public static Optional<ScoreTarget> forPlayer(ServerPlayer player, Optional<CrewInfo> crew) {
      return crew.isPresent() && !crew.get().key().isBlank()
         ? Optional.of(crew(crew.get()))
         : Optional.of(player(player.getUUID(), player.getGameProfile().name()));
   }

   public static ScoreTarget crew(CrewInfo crew) {
      String id = IslandClaim.normalizeCrew(crew.key());
      return new ScoreTarget("crew", id, id, null, crew.captain());
   }

   public static ScoreTarget crew(String crewName, UUID captainId) {
      String id = IslandClaim.normalizeCrew(crewName);
      return new ScoreTarget("crew", id, id, null, captainId);
   }

   public static ScoreTarget player(UUID uuid, String name) {
      String display = name != null && !name.isBlank() ? name : uuid.toString();
      return new ScoreTarget("player", uuid.toString().toLowerCase(Locale.ROOT), display, uuid, null);
   }

   public String key() {
      return this.type + ":" + this.id;
   }

   public String label() {
      return "crew".equals(this.type) ? "Tripulacao " + this.displayName : "Jogador " + this.displayName;
   }
}
