package br.com.oneminepiece.opworldlike.claim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public final class IslandClaim {
   private static final String CREW_PREFIX = "crew:";
   private static final String PLAYER_PREFIX = "player:";
   public final String islandId;
   public String ownerType = "";
   public String ownerId = "";
   public String ownerName = "";
   public UUID ownerPlayer;
   public UUID ownerCaptain;
   public final Map<String, Double> scores = new HashMap<>();
   public final Map<String, String> scoreNames = new HashMap<>();
   public final Map<String, UUID> scorePlayers = new HashMap<>();
   public final Map<String, UUID> scoreCaptains = new HashMap<>();
   public final Set<UUID> trustedPlayers = new HashSet<>();
   public final Set<String> trustedCrews = new HashSet<>();
   public final EnumMap<IslandFlag, Boolean> flags = new EnumMap<>(IslandFlag.class);
   public final Set<Long> scoredPlacedBlocks = new HashSet<>();
   public final Set<Long> protectedBuildAnchors = new HashSet<>();
   public final Map<String, Long> playerKillCooldowns = new HashMap<>();
   public String entryMessage = "";
   public String exitMessage = "";

   IslandClaim(String islandId) {
      this.islandId = islandId;

      for (IslandFlag flag : IslandFlag.values()) {
         this.flags.put(flag, flag.defaultValue);
      }
   }

   public boolean hasOwner() {
      return this.ownerType != null && !this.ownerType.isBlank() && this.ownerId != null && !this.ownerId.isBlank();
   }

   public String ownerKey() {
      return !this.hasOwner() ? "" : this.ownerType + ":" + this.ownerId;
   }

   public String ownerLabel() {
      if (!this.hasOwner()) {
         return "sem dono";
      }

      String display = this.ownerName != null && !this.ownerName.isBlank() ? this.ownerName : this.ownerId;
      return "crew".equals(this.ownerType) ? "Tripulacao " + display : "Jogador " + display;
   }

   public boolean isOwnerCrew(String crewKey) {
      return this.hasOwner() && "crew".equals(this.ownerType) && this.ownerId.equals(normalizeCrew(crewKey));
   }

   public boolean isOwnerPlayer(UUID playerId) {
      return this.hasOwner() && "player".equals(this.ownerType) && this.ownerPlayer != null && this.ownerPlayer.equals(playerId);
   }

   public void setOwner(ScoreTarget target) {
      this.ownerType = target.type();
      this.ownerId = target.id();
      this.ownerName = target.displayName();
      this.ownerPlayer = target.playerId();
      this.ownerCaptain = target.captainId();
   }

   public void setOwnerFromScore(String scoreKey) {
      String normalized = normalizeScoreKey(scoreKey);
      this.ownerType = normalized.startsWith("player:") ? "player" : "crew";
      this.ownerId = normalized.substring(normalized.indexOf(58) + 1);
      this.ownerName = this.scoreNames.getOrDefault(normalized, this.ownerId);
      this.ownerPlayer = this.scorePlayers.get(normalized);
      this.ownerCaptain = this.scoreCaptains.get(normalized);
   }

   public void clearOwner() {
      this.ownerType = "";
      this.ownerId = "";
      this.ownerName = "";
      this.ownerPlayer = null;
      this.ownerCaptain = null;
   }

   public double addScore(ScoreTarget target, double amount) {
      if (target != null && !target.id().isBlank() && !(amount <= 0.0)) {
         String key = target.key();
         this.scoreNames.put(key, target.displayName());
         if (target.playerId() != null) {
            this.scorePlayers.put(key, target.playerId());
         }

         if (target.captainId() != null) {
            this.scoreCaptains.put(key, target.captainId());
         }

         double score = this.scores.getOrDefault(key, 0.0) + amount;
         this.scores.put(key, score);
         return score;
      } else {
         return 0.0;
      }
   }

   public double scoreFor(ScoreTarget target) {
      return target == null ? 0.0 : this.scores.getOrDefault(target.key(), 0.0);
   }

   public boolean publicAllowed(IslandFlag flag) {
      return this.flags.getOrDefault(flag, flag.defaultValue);
   }

   public List<Entry<String, Double>> topScores() {
      List<Entry<String, Double>> entries = new ArrayList<>(this.scores.entrySet());
      entries.sort(Entry.comparingByValue(Comparator.reverseOrder()));
      return entries;
   }

   public String scoreLabel(String key) {
      String normalized = normalizeScoreKey(key);
      String display = this.scoreNames.getOrDefault(normalized, normalized.substring(normalized.indexOf(58) + 1));
      return normalized.startsWith("player:") ? "Jogador " + display : "Tripulacao " + display;
   }

   public boolean isProtectedNear(BlockPos pos, int radius) {
      if (this.protectedBuildAnchors.isEmpty()) {
         return false;
      }

      double maxDistance = (double)radius * radius;

      for (long anchor : this.protectedBuildAnchors) {
         if (BlockPos.of(anchor).distSqr(pos) <= maxDistance) {
            return true;
         }
      }

      return false;
   }

   public void addProtectedAnchor(BlockPos pos) {
      this.protectedBuildAnchors.add(pos.asLong());
   }

   public void removeProtectedAnchor(BlockPos pos) {
      this.protectedBuildAnchors.remove(pos.asLong());
   }

   boolean migrateCrew(String oldCrewName, String newCrewName, UUID captainId) {
      String oldCrew = normalizeCrew(oldCrewName);
      String newCrew = normalizeCrew(newCrewName);
      if (!oldCrew.isBlank() && !newCrew.isBlank() && !oldCrew.equals(newCrew)) {
         boolean changed = false;
         if ("crew".equals(this.ownerType) && oldCrew.equals(this.ownerId)) {
            this.ownerId = newCrew;
            this.ownerName = newCrewName;
            this.ownerCaptain = captainId;
            changed = true;
         }

         if (this.trustedCrews.remove(oldCrew)) {
            this.trustedCrews.add(newCrew);
            changed = true;
         }

         String oldScoreKey = "crew:" + oldCrew;
         String newScoreKey = "crew:" + newCrew;
         Double score = this.scores.remove(oldScoreKey);
         if (score != null) {
            this.scores.merge(newScoreKey, score, Double::sum);
            changed = true;
         }

         String scoreName = this.scoreNames.remove(oldScoreKey);
         if (scoreName != null || this.scores.containsKey(newScoreKey)) {
            this.scoreNames.put(newScoreKey, newCrewName);
            changed = true;
         }

         this.scorePlayers.remove(oldScoreKey);
         UUID captain = this.scoreCaptains.remove(oldScoreKey);
         UUID resolvedCaptain = captainId != null ? captainId : captain;
         if (resolvedCaptain != null) {
            this.scoreCaptains.put(newScoreKey, resolvedCaptain);
            changed = true;
         }

         return changed;
      } else {
         return false;
      }
   }

   void decayScores(double keepRatio) {
      this.scores.replaceAll((key, score) -> score * keepRatio);
      this.scoredPlacedBlocks.clear();
      this.playerKillCooldowns.clear();
   }

   CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("island", this.islandId);
      tag.putString("ownerType", this.ownerType == null ? "" : this.ownerType);
      tag.putString("ownerId", this.ownerId == null ? "" : this.ownerId);
      tag.putString("ownerName", this.ownerName == null ? "" : this.ownerName);
      if ("crew".equals(this.ownerType)) {
         tag.putString("ownerCrew", this.ownerId == null ? "" : this.ownerId);
      }

      if (this.ownerPlayer != null) {
         tag.putString("ownerPlayer", this.ownerPlayer.toString());
      }

      if (this.ownerCaptain != null) {
         tag.putString("ownerCaptain", this.ownerCaptain.toString());
      }

      CompoundTag scoresTag = new CompoundTag();
      this.scores.forEach(scoresTag::putDouble);
      tag.put("scores", scoresTag);
      CompoundTag scoreNamesTag = new CompoundTag();
      this.scoreNames.forEach(scoreNamesTag::putString);
      tag.put("scoreNames", scoreNamesTag);
      CompoundTag scorePlayersTag = new CompoundTag();
      this.scorePlayers.forEach((k, v) -> scorePlayersTag.putString(k, v.toString()));
      tag.put("scorePlayers", scorePlayersTag);
      CompoundTag scoreCaptainsTag = new CompoundTag();
      this.scoreCaptains.forEach((k, v) -> scoreCaptainsTag.putString(k, v.toString()));
      tag.put("scoreCaptains", scoreCaptainsTag);
      CompoundTag flagsTag = new CompoundTag();
      this.flags.forEach((flag, value) -> flagsTag.putBoolean(flag.id(), value));
      tag.put("flags", flagsTag);
      ListTag trustedPlayerList = new ListTag();
      this.trustedPlayers.forEach(uuid -> trustedPlayerList.add(StringTag.valueOf(uuid.toString())));
      tag.put("trustedPlayers", trustedPlayerList);
      ListTag trustedCrewList = new ListTag();
      this.trustedCrews.forEach(crew -> trustedCrewList.add(StringTag.valueOf(crew)));
      tag.put("trustedCrews", trustedCrewList);
      tag.putLongArray("scoredPlacedBlocks", this.scoredPlacedBlocks.stream().mapToLong(Long::longValue).toArray());
      tag.putLongArray("protectedBuildAnchors", this.protectedBuildAnchors.stream().mapToLong(Long::longValue).toArray());
      tag.putString("entryMessage", this.entryMessage == null ? "" : this.entryMessage);
      tag.putString("exitMessage", this.exitMessage == null ? "" : this.exitMessage);
      CompoundTag cooldowns = new CompoundTag();
      this.playerKillCooldowns.forEach(cooldowns::putLong);
      tag.put("playerKillCooldowns", cooldowns);
      return tag;
   }

   static IslandClaim load(CompoundTag tag) {
      IslandClaim claim = new IslandClaim(tag.getStringOr("island", ""));
      claim.ownerType = tag.getStringOr("ownerType", "").toLowerCase(Locale.ROOT);
      claim.ownerId = tag.getStringOr("ownerId", "").toLowerCase(Locale.ROOT);
      claim.ownerName = tag.getStringOr("ownerName", "");
      if (claim.ownerType.isBlank()) {
         String oldOwnerCrew = normalizeCrew(tag.getStringOr("ownerCrew", ""));
         if (!oldOwnerCrew.isBlank()) {
            claim.ownerType = "crew";
            claim.ownerId = oldOwnerCrew;
            claim.ownerName = oldOwnerCrew;
         }
      }

      if (tag.contains("ownerPlayer")) {
         claim.ownerPlayer = UUID.fromString(tag.getStringOr("ownerPlayer", ""));
      }

      if (tag.contains("ownerCaptain")) {
         claim.ownerCaptain = UUID.fromString(tag.getStringOr("ownerCaptain", ""));
      }

      CompoundTag scoresTag = tag.getCompoundOrEmpty("scores");

      for (String key : scoresTag.keySet()) {
         String normalized = normalizeScoreKey(key);
         claim.scores.put(normalized, scoresTag.getDoubleOr(key, 0.0));
         claim.scoreNames.putIfAbsent(normalized, normalized.substring(normalized.indexOf(58) + 1));
      }

      CompoundTag scoreNamesTag = tag.getCompoundOrEmpty("scoreNames");

      for (String key : scoreNamesTag.keySet()) {
         claim.scoreNames.put(normalizeScoreKey(key), scoreNamesTag.getStringOr(key, ""));
      }

      CompoundTag scorePlayersTag = tag.getCompoundOrEmpty("scorePlayers");

      for (String key : scorePlayersTag.keySet()) {
         if (scorePlayersTag.contains(key)) {
            claim.scorePlayers.put(normalizeScoreKey(key), UUID.fromString(scorePlayersTag.getStringOr(key, "")));
         }
      }

      CompoundTag scoreCaptainsTag = tag.getCompoundOrEmpty("scoreCaptains");

      for (String key : scoreCaptainsTag.keySet()) {
         if (scoreCaptainsTag.contains(key)) {
            claim.scoreCaptains.put(normalizeScoreKey(key), UUID.fromString(scoreCaptainsTag.getStringOr(key, "")));
         }
      }

      CompoundTag flagsTag = tag.getCompoundOrEmpty("flags");

      for (IslandFlag flag : IslandFlag.values()) {
         if (flagsTag.contains(flag.id())) {
            claim.flags.put(flag, flagsTag.getBooleanOr(flag.id(), false));
         }
      }

      ListTag trustedPlayerList = tag.getListOrEmpty("trustedPlayers");

      for (int i = 0; i < trustedPlayerList.size(); i++) {
         claim.trustedPlayers.add(UUID.fromString(trustedPlayerList.getStringOr(i, "")));
      }

      ListTag trustedCrewList = tag.getListOrEmpty("trustedCrews");

      for (int i = 0; i < trustedCrewList.size(); i++) {
         claim.trustedCrews.add(normalizeCrew(trustedCrewList.getStringOr(i, "")));
      }

      for (long pos : tag.getLongArray("scoredPlacedBlocks").orElse(new long[0])) {
         claim.scoredPlacedBlocks.add(pos);
      }

      for (long pos : tag.getLongArray("protectedBuildAnchors").orElse(new long[0])) {
         claim.protectedBuildAnchors.add(pos);
      }

      claim.entryMessage = tag.getStringOr("entryMessage", "");
      claim.exitMessage = tag.getStringOr("exitMessage", "");
      CompoundTag cooldowns = tag.getCompoundOrEmpty("playerKillCooldowns");

      for (String key : cooldowns.keySet()) {
         claim.playerKillCooldowns.put(key, cooldowns.getLongOr(key, 0L));
      }

      return claim;
   }

   public static String normalizeCrew(String crew) {
      return crew == null ? "" : crew.toLowerCase(Locale.ROOT).replace(' ', '_');
   }

   static String normalizeScoreKey(String key) {
      if (key != null && !key.isBlank()) {
         String normalized = key.toLowerCase(Locale.ROOT);
         if (normalized.startsWith("crew:")) {
            return "crew:" + normalizeCrew(normalized.substring("crew:".length()));
         } else {
            return normalized.startsWith("player:") ? "player:" + normalized.substring("player:".length()) : "crew:" + normalizeCrew(normalized);
         }
      } else {
         return "";
      }
   }
}
