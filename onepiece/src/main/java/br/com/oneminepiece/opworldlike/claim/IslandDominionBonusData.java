package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.OmpMessages;
import br.com.oneminepiece.opworldlike.OmpWorld;
import br.com.oneminepiece.opworldlike.integration.CrewInfo;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class IslandDominionBonusData {
   public static final int DORIKI_BONUS_PER_ISLAND = 5000;
   private static final String DATA_NAME = "opworldlike_island_dominion_bonuses";
   private static final long ALERT_COOLDOWN_MS = 900000L;
   private final Map<String, UUID> islandBeneficiaries = new HashMap<>();
   private final Map<UUID, Double> pendingDoriki = new HashMap<>();
   private final Map<String, Long> alertCooldowns = new HashMap<>();

   private static final Path PATH = Path.of("config", "opworldlike", "island-dominion-bonuses.dat");
   private static final IslandDominionBonusData INSTANCE = loadFromDisk();

   public static IslandDominionBonusData get(ServerLevel level) {
      return INSTANCE;
   }

   private static IslandDominionBonusData loadFromDisk() {
      if (Files.exists(PATH)) {
         try {
            return load(NbtIo.read(PATH));
         } catch (IOException ex) {
            OmpWorld.LOGGER.warn("Could not load island dominion bonuses from {}.", PATH, ex);
         }
      }
      return new IslandDominionBonusData();
   }

   private void persist() {
      CompoundTag tag = this.getNearbyRaid(new CompoundTag());
      try {
         Files.createDirectories(PATH.getParent());
         NbtIo.write(tag, PATH);
      } catch (IOException ex) {
         OmpWorld.LOGGER.warn("Could not save island dominion bonuses to {}.", PATH, ex);
      }
   }

   public static IslandDominionBonusData load(CompoundTag tag) {
      IslandDominionBonusData data = new IslandDominionBonusData();
      ListTag beneficiaryList = tag.getListOrEmpty("beneficiaries");

      for (int i = 0; i < beneficiaryList.size(); i++) {
         CompoundTag entry = beneficiaryList.getCompoundOrEmpty(i);
         if (entry.contains("island") && entry.contains("beneficiary")) {
            data.islandBeneficiaries.put(entry.getStringOr("island", ""), UUID.fromString(entry.getStringOr("beneficiary", "")));
         }
      }

      ListTag pendingList = tag.getListOrEmpty("pendingDoriki");

      for (int i = 0; i < pendingList.size(); i++) {
         CompoundTag entry = pendingList.getCompoundOrEmpty(i);
         if (entry.contains("player")) {
            double delta = entry.getDoubleOr("delta", 0.0);
            if (delta != 0.0) {
               data.pendingDoriki.put(UUID.fromString(entry.getStringOr("player", "")), delta);
            }
         }
      }

      CompoundTag alerts = tag.getCompoundOrEmpty("alertCooldowns");

      for (String key : alerts.keySet()) {
         data.alertCooldowns.put(key, alerts.getLongOr(key, 0L));
      }

      return data;
   }

   public CompoundTag getNearbyRaid(CompoundTag tag) {
      ListTag beneficiaryList = new ListTag();
      this.islandBeneficiaries.forEach((island, beneficiary) -> {
         CompoundTag entry = new CompoundTag();
         entry.putString("island", island);
         entry.putString("beneficiary", beneficiary.toString());
         beneficiaryList.add(entry);
      });
      tag.put("beneficiaries", beneficiaryList);
      ListTag pendingList = new ListTag();
      this.pendingDoriki.forEach((player, delta) -> {
         if (delta != 0.0) {
            CompoundTag entry = new CompoundTag();
            entry.putString("player", player.toString());
            entry.putDouble("delta", delta);
            pendingList.add(entry);
         }
      });
      tag.put("pendingDoriki", pendingList);
      CompoundTag alerts = new CompoundTag();
      this.alertCooldowns.forEach(alerts::putLong);
      tag.put("alertCooldowns", alerts);
      return tag;
   }

   public int bonusCount(UUID playerId) {
      if (playerId == null) {
         return 0;
      }

      int count = 0;

      for (UUID beneficiary : this.islandBeneficiaries.values()) {
         if (playerId.equals(beneficiary)) {
            count++;
         }
      }

      return count;
   }

   public int dorikiLimitBonus(UUID playerId) {
      return this.bonusCount(playerId) * 5000;
   }

   public void reconcileClaims(ServerLevel level, Iterable<IslandClaim> claims) {
      Set<String> activeIslands = new HashSet<>();

      for (IslandClaim claim : claims) {
         if (claim != null && claim.hasOwner() && this.isClaimable(claim.islandId)) {
            activeIslands.add(claim.islandId);
            this.reconcileClaim(level, claim);
         }
      }

      for (String islandId : new ArrayList<>(this.islandBeneficiaries.keySet())) {
         if (!activeIslands.contains(islandId)) {
            this.replaceBeneficiary(level, islandId, null);
         }
      }
   }

   public void reconcileClaim(ServerLevel level, IslandClaim claim) {
      if (claim != null && claim.hasOwner() && this.isClaimable(claim.islandId)) {
         this.replaceBeneficiary(level, claim.islandId, this.beneficiaryOf(claim));
      } else {
         if (claim != null) {
            this.replaceBeneficiary(level, claim.islandId, null);
         }
      }
   }

   public void applyPending(ServerPlayer player) {
      Double removed = this.pendingDoriki.remove(player.getUUID());
      double delta = removed == null ? 0.0 : removed;
      if (delta != 0.0) {
         if (!MinePieceBridge.applyDorikiDelta(player, delta)) {
            this.pendingDoriki.merge(player.getUUID(), delta, Double::sum);
            this.persist();
         } else {
            this.persist();
         }
      }
   }

   public void maybeAlertThreat(ServerLevel level, IslandClaim claim, ScoreTarget target, double score, double requiredScore, String islandName) {
      if (claim != null && claim.hasOwner() && target != null && !(requiredScore <= 0.0)) {
         String targetKey = IslandClaim.normalizeScoreKey(target.key());
         if (!targetKey.equals(claim.ownerKey())) {
            double progress = score / requiredScore;
            int stage = progress >= 0.9 ? 90 : (progress >= 0.7 ? 70 : 0);
            if (stage != 0) {
               String key = claim.islandId + "|" + targetKey + "|" + stage;
               long now = System.currentTimeMillis();
               if (now - this.alertCooldowns.getOrDefault(key, 0L) >= 900000L) {
                  this.alertCooldowns.put(key, now);
                  this.notifyOwnerOnline(level, claim, stage, target.label(), islandName, score, requiredScore);
                  this.persist();
               }
            }
         }
      }
   }

   private void replaceBeneficiary(ServerLevel level, String islandId, UUID next) {
      UUID previous = this.islandBeneficiaries.get(islandId);
      if (previous == null || !previous.equals(next)) {
         if (previous != null) {
            this.islandBeneficiaries.remove(islandId);
            this.applyOrQueue(level, previous, -5000.0);
         }

         if (next != null) {
            this.islandBeneficiaries.put(islandId, next);
            this.applyOrQueue(level, next, 5000.0);
         }

         this.persist();
      }
   }

   private void applyOrQueue(ServerLevel level, UUID playerId, double delta) {
      ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
      if (player == null || !MinePieceBridge.applyDorikiDelta(player, delta)) {
         this.pendingDoriki.merge(playerId, delta, Double::sum);
         if (this.pendingDoriki.getOrDefault(playerId, 0.0) == 0.0) {
            this.pendingDoriki.remove(playerId);
         }
      }
   }

   private UUID beneficiaryOf(IslandClaim claim) {
      if ("crew".equals(claim.ownerType)) {
         return MinePieceBridge.crewByName(claim.ownerId).map(CrewInfo::captain).orElse(claim.ownerCaptain);
      }

      if (claim.ownerPlayer != null) {
         return claim.ownerPlayer;
      }

      try {
         return UUID.fromString(claim.ownerId);
      } catch (IllegalArgumentException ignored) {
         return null;
      }
   }

   private boolean isClaimable(String islandId) {
      return UniqueIslandCatalog.byId(islandId).map(island -> island.claimable()).orElse(true);
   }

   private void notifyOwnerOnline(ServerLevel level, IslandClaim claim, int stage, String rivalLabel, String islandName, double score, double requiredScore) {
      for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
         if (this.shouldNotify(player, claim)) {
            player.sendSystemMessage(
               Component.literal("[Ilhas] ")
                  .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD})
                  .append(
                     Component.literal(
                           OmpMessages.text(
                              player,
                              "Alerta "
                                 + stage
                                 + "%: "
                                 + rivalLabel
                                 + " esta perto de tomar "
                                 + islandName
                                 + " ("
                                 + this.format(score)
                                 + "/"
                                 + this.format(requiredScore)
                                 + " pontos).",
                              "Alert "
                                 + stage
                                 + "%: "
                                 + rivalLabel
                                 + " is close to taking "
                                 + islandName
                                 + " ("
                                 + this.format(score)
                                 + "/"
                                 + this.format(requiredScore)
                                 + " points)."
                           )
                        )
                        .withStyle(stage >= 90 ? ChatFormatting.RED : ChatFormatting.YELLOW)
                  )
            );
         }
      }
   }

   private boolean shouldNotify(ServerPlayer player, IslandClaim claim) {
      if (!"player".equals(claim.ownerType)) {
         return !"crew".equals(claim.ownerType)
            ? false
            : MinePieceBridge.crewOf(player).map(crew -> claim.ownerId.equals(IslandClaim.normalizeCrew(crew.key()))).orElse(false);
      } else {
         return claim.ownerPlayer != null && claim.ownerPlayer.equals(player.getUUID());
      }
   }

   private String format(double value) {
      return String.format(Locale.US, "%.0f", value);
   }
}
