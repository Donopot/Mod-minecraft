package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.OmpDiscordAnnouncements;
import br.com.oneminepiece.opworldlike.OmpMessages;
import br.com.oneminepiece.opworldlike.integration.CrewInfo;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import br.com.oneminepiece.opworldlike.OmpWorld;

public final class IslandClaimsData {
   private static final Path PATH = Path.of("config", "opworldlike", "island-claims.dat");
   private static final IslandClaimsData INSTANCE = load();
   private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
   private static final long OWNERSHIP_REVIEW_INTERVAL_MS = 7200000L;
   private static final long OWNERSHIP_REVIEW_WARNING_MS = 300000L;
   private static final double INSTANT_CLAIM_SCORE = 100.0;
   private static final double TRANSFER_MIN_SCORE = 500.0;
   private static final double TRANSFER_ADVANTAGE = 1.15;
   private static final double WEEKLY_DECAY_KEEP_RATIO = 0.8;
   private static final int DISCORD_WARNING_COLOR = 15844367;
   private static final int DISCORD_STARTED_COLOR = 3447003;
   private static final int DISCORD_COMPLETE_COLOR = 3066993;
   private static final int DISCORD_CHANGED_COLOR = 15105570;
   private static final int DISCORD_DECAY_COLOR = 10181046;
   private final Map<String, IslandClaim> claims = new HashMap<>();
   private String lastResolutionWeek = "";
   private long lastOwnershipReviewAtMillis;
   private long lastOwnershipReviewWarningAtMillis;

   private IslandClaimsData() {
   }

   public static IslandClaimsData get(ServerLevel level) {
      return INSTANCE;
   }

   private static IslandClaimsData load() {
      if (Files.exists(PATH)) {
         try {
            return load(NbtIo.read(PATH));
         } catch (IOException ex) {
            OmpWorld.LOGGER.warn("Could not load island claims from {}.", PATH, ex);
         }
      }

      return new IslandClaimsData();
   }

   private static IslandClaimsData load(CompoundTag tag) {
      IslandClaimsData data = new IslandClaimsData();
      data.lastResolutionWeek = tag.getStringOr("lastResolutionWeek", "");
      data.lastOwnershipReviewAtMillis = tag.getLongOr("lastOwnershipReviewAtMillis", 0L);
      data.lastOwnershipReviewWarningAtMillis = tag.getLongOr("lastOwnershipReviewWarningAtMillis", 0L);
      ListTag list = tag.getListOrEmpty("claims");

      for (int i = 0; i < list.size(); i++) {
         IslandClaim claim = IslandClaim.load(list.getCompoundOrEmpty(i));
         data.claims.put(claim.islandId, claim);
      }

      return data;
   }

   private CompoundTag save(CompoundTag tag) {
      tag.putString("lastResolutionWeek", this.lastResolutionWeek);
      tag.putLong("lastOwnershipReviewAtMillis", this.lastOwnershipReviewAtMillis);
      tag.putLong("lastOwnershipReviewWarningAtMillis", this.lastOwnershipReviewWarningAtMillis);
      ListTag list = new ListTag();
      this.claims.values().forEach(claim -> list.add(claim.save()));
      tag.put("claims", list);
      return tag;
   }

   public IslandClaim getOrCreate(IslandDefinition island) {
      return !island.claimable() ? new IslandClaim(island.id()) : this.claims.computeIfAbsent(island.id(), IslandClaim::new);
   }

   public IslandClaim get(String islandId) {
      return this.claims.get(islandId);
   }

   public Collection<IslandClaim> allClaims() {
      return new ArrayList<>(this.claims.values());
   }

   public int migrateCrew(String oldCrewName, String newCrewName, UUID captainId) {
      int changed = 0;

      for (IslandClaim claim : this.claims.values()) {
         if (claim.migrateCrew(oldCrewName, newCrewName, captainId)) {
            changed++;
         }
      }

      if (changed > 0) {
         this.persist();
      }

      return changed;
   }

   public void addScore(ServerLevel level, IslandDefinition island, CrewInfo crew, double amount) {
      if (crew != null && !crew.key().isBlank()) {
         this.addScore(level, island, ScoreTarget.crew(crew), amount);
      }
   }

   public void addScore(ServerLevel level, IslandDefinition island, ScoreTarget target, double amount) {
      if (island.claimable() && target != null && !target.id().isBlank() && !(amount <= 0.0)) {
         IslandClaim claim = this.getOrCreate(island);
         double score = claim.addScore(target, amount);
         if (claim.hasOwner()) {
            double requiredScore = this.requiredTransferScore(claim);
            IslandDominionBonusData.get(level).maybeAlertThreat(level, claim, target, score, requiredScore, island.displayName());
         } else if (score >= 100.0) {
            claim.setOwner(target);
            this.broadcast(
               level,
               ChatFormatting.GOLD,
               target.label() + " conquistou " + island.displayName() + " com " + this.format(score) + " pontos.",
               this.englishLabel(target.label()) + " conquered " + this.englishIsland(island.displayName()) + " with " + this.format(score) + " points."
            );
            IslandDominionBonusData.get(level).reconcileClaim(level, claim);
         }

         this.persist();
      }
   }

   public void runScheduledMaintenance(ServerLevel level) {
      IslandDominionBonusData.get(level).reconcileClaims(level, this.claims.values());
      this.resolveOwnershipIfDue(level);
      this.decayWeeklyIfDue(level);
   }

   public IslandClaimsData.ReviewSummary forceOwnershipReview(ServerLevel level) {
      this.lastOwnershipReviewAtMillis = System.currentTimeMillis();
      this.broadcastOwnershipReviewStart(level);
      IslandClaimsData.ReviewSummary summary = this.resolveOwnership(level);
      this.broadcastReviewSummary(level, summary);
      this.persist();
      return summary;
   }

   private void resolveOwnershipIfDue(ServerLevel level) {
      long now = System.currentTimeMillis();
      if (this.lastOwnershipReviewAtMillis > 0L) {
         long elapsed = now - this.lastOwnershipReviewAtMillis;
         if (elapsed < 7200000L) {
            this.maybeBroadcastOwnershipReviewWarning(level, now, elapsed);
            return;
         }
      }

      this.lastOwnershipReviewAtMillis = now;
      this.broadcastOwnershipReviewStart(level);
      IslandClaimsData.ReviewSummary summary = this.resolveOwnership(level);
      this.broadcastReviewSummary(level, summary);
      this.persist();
   }

   private void maybeBroadcastOwnershipReviewWarning(ServerLevel level, long now, long elapsed) {
      long remaining = 7200000L - elapsed;
      if (remaining <= 300000L && this.lastOwnershipReviewWarningAtMillis < this.lastOwnershipReviewAtMillis) {
         this.lastOwnershipReviewWarningAtMillis = now;
         this.broadcast(
            level,
            ChatFormatting.YELLOW,
            "Revisao de lealdade das ilhas em ate 5 minutos. Pontuacao e donos serao avaliados.",
            "Island loyalty review in up to 5 minutes. Scores and owners will be checked."
         );
         this.queueReviewWarning();
         this.persist();
      }
   }

   private void decayWeeklyIfDue(ServerLevel level) {
      ZonedDateTime now = ZonedDateTime.now(ZONE);
      if (now.getDayOfWeek() == DayOfWeek.SUNDAY && now.getHour() >= 3) {
         String week = this.weekKey(now.toLocalDate());
         if (!week.equals(this.lastResolutionWeek)) {
            this.claims.entrySet().removeIf(entry -> UniqueIslandCatalog.byId(entry.getKey()).map(island -> !island.claimable()).orElse(false));

            for (IslandClaim claim : this.claims.values()) {
               claim.decayScores(0.8);
            }

            this.lastResolutionWeek = week;
            this.broadcast(level, ChatFormatting.LIGHT_PURPLE, "Decay semanal de pontuacao das ilhas aplicado: 20%.", "Weekly island score decay applied: 20%.");
            OmpDiscordAnnouncements.queueEmbed(
               10181046,
               "[Ilhas] Decay semanal | Weekly decay",
               "**PT-BR**\n- Decay semanal de pontuacao das ilhas aplicado: **20%**.\n- Blocos pontuados e cooldowns de abate foram limpos.\n\n**EN-US**\n- Weekly island score decay applied: **20%**.\n- Scored blocks and kill cooldowns were cleared."
            );
            this.persist();
         }
      }
   }

   private IslandClaimsData.ReviewSummary resolveOwnership(ServerLevel level) {
      Map<String, IslandDefinition> islandsById = new HashMap<>();

      for (IslandDefinition island : OnePieceGeography.allClaimIslands(level.getSeed())) {
         islandsById.put(island.id(), island);
      }

      this.claims.entrySet().removeIf(entry -> {
         IslandDefinition islandx = islandsById.get(entry.getKey());
         return islandx != null && !islandx.claimable();
      });
      int conquered = 0;
      int transferred = 0;
      int reviewed = 0;
      List<String> conqueredIslands = new ArrayList<>();
      List<String> transferredIslands = new ArrayList<>();

      for (IslandClaim claim : this.claims.values()) {
         IslandClaimsData.OwnershipResult result = this.resolveOwnership(claim);
         if (result != IslandClaimsData.OwnershipResult.NONE) {
            reviewed++;
            IslandDefinition island = islandsById.get(claim.islandId);
            String islandName = island == null ? this.maskedIslandId(claim.islandId) : island.displayName();
            if (result == IslandClaimsData.OwnershipResult.CONQUERED) {
               conquered++;
               conqueredIslands.add(islandName);
               this.broadcast(
                  level,
                  ChatFormatting.GOLD,
                  claim.ownerLabel() + " conquistou " + islandName + " na revisao de ilhas.",
                  this.englishLabel(claim.ownerLabel()) + " conquered " + this.englishIsland(islandName) + " during island review."
               );
            } else if (result == IslandClaimsData.OwnershipResult.TRANSFERRED) {
               transferred++;
               transferredIslands.add(islandName);
               this.broadcast(
                  level,
                  ChatFormatting.RED,
                  claim.ownerLabel() + " tomou " + islandName + " na revisao de ilhas.",
                  this.englishLabel(claim.ownerLabel()) + " took " + this.englishIsland(islandName) + " during island review."
               );
            }

            IslandDominionBonusData.get(level).reconcileClaim(level, claim);
         }
      }

      return new IslandClaimsData.ReviewSummary(
         conquered, transferred, reviewed, this.claims.size(), List.copyOf(conqueredIslands), List.copyOf(transferredIslands)
      );
   }

   private IslandClaimsData.OwnershipResult resolveOwnership(IslandClaim claim) {
      List<Entry<String, Double>> top = claim.topScores();
      if (top.isEmpty()) {
         return IslandClaimsData.OwnershipResult.NONE;
      }

      Entry<String, Double> winner = top.get(0);
      if (!claim.hasOwner()) {
         if (winner.getValue() < 100.0) {
            return IslandClaimsData.OwnershipResult.NONE;
         }

         claim.setOwnerFromScore(winner.getKey());
         return IslandClaimsData.OwnershipResult.CONQUERED;
      } else if (claim.ownerKey().equals(IslandClaim.normalizeScoreKey(winner.getKey()))) {
         return IslandClaimsData.OwnershipResult.NONE;
      } else if (winner.getValue() < 500.0) {
         return IslandClaimsData.OwnershipResult.NONE;
      } else if (winner.getValue() >= this.requiredTransferScore(claim)) {
         claim.setOwnerFromScore(winner.getKey());
         return IslandClaimsData.OwnershipResult.TRANSFERRED;
      } else {
         return IslandClaimsData.OwnershipResult.NONE;
      }
   }

   private String maskedIslandId(String islandId) {
      if (islandId != null && !islandId.isBlank()) {
         String lower = islandId.toLowerCase(Locale.ROOT);
         if (lower.startsWith("north_blue_")) {
            return "North Blue Island";
         } else if (lower.startsWith("south_blue_")) {
            return "South Blue Island";
         } else if (lower.startsWith("east_blue_")) {
            return "East Blue Island";
         } else if (lower.startsWith("west_blue_")) {
            return "West Blue Island";
         } else {
            return lower.startsWith("grand_line_") ? "Grand Line Island" : "Unknown Island";
         }
      } else {
         return "Unknown Island";
      }
   }

   private double requiredTransferScore(IslandClaim claim) {
      double currentOwnerScore = claim.scores.getOrDefault(claim.ownerKey(), 0.0);
      return Math.max(500.0, currentOwnerScore * 1.15);
   }

   private String weekKey(LocalDate date) {
      WeekFields fields = WeekFields.ISO;
      return date.get(fields.weekBasedYear()) + "-W" + date.get(fields.weekOfWeekBasedYear());
   }

   private void broadcastReviewSummary(ServerLevel level, IslandClaimsData.ReviewSummary summary) {
      ChatFormatting color = summary.changedClaims() > 0 ? ChatFormatting.GOLD : ChatFormatting.GREEN;
      this.broadcast(
         level,
         color,
         "Revisao de ilhas concluida: "
            + summary.conquered()
            + " conquistas, "
            + summary.transferred()
            + " transferencias, "
            + summary.changedClaims()
            + " mudancas, "
            + summary.totalClaims()
            + " claims monitorados.",
         "Island review complete: "
            + summary.conquered()
            + " conquests, "
            + summary.transferred()
            + " transfers, "
            + summary.changedClaims()
            + " changes, "
            + summary.totalClaims()
            + " tracked claims."
      );
      this.queueReviewSummary(summary);
   }

   private void broadcastOwnershipReviewStart(ServerLevel level) {
      this.broadcast(
         level,
         ChatFormatting.AQUA,
         "Revisao de lealdade das ilhas iniciada. Pontuacao e donos serao avaliados agora.",
         "Island loyalty review started. Scores and owners are being checked now."
      );
      OmpDiscordAnnouncements.queueEmbed(
         3447003,
         "[Ilhas] Analise iniciada | Island review started",
         "**PT-BR**\n- A analise automatica de lealdade das ilhas comecou agora.\n- Pontuacao de dominio, donos atuais e possiveis transferencias serao avaliados.\n\n**EN-US**\n- The automatic island loyalty review has started.\n- Control scores, current owners and possible transfers will be checked."
      );
   }

   private void broadcast(ServerLevel level, String pt, String en) {
      this.broadcast(level, ChatFormatting.YELLOW, pt, en);
   }

   private void broadcast(ServerLevel level, ChatFormatting color, String pt, String en) {
      for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
         player.sendSystemMessage(this.islandComponent(player, color, pt, en));
      }
   }

   private Component islandComponent(ServerPlayer player, ChatFormatting color, String pt, String en) {
      return Component.literal("[Ilhas] ")
         .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD})
         .append(Component.literal(OmpMessages.text(player, pt, en)).withStyle(color));
   }

   private void queueReviewWarning() {
      OmpDiscordAnnouncements.queueEmbed(
         15844367,
         "[Ilhas] Analise em 5 min | Island review in 5 min",
         "**PT-BR**\n- A revisao de lealdade das ilhas comeca em ate **5 minutos**.\n- Pontuacao de dominio e donos serao conferidos.\n- Prepare sua tripulacao se estiver disputando territorio.\n\n**EN-US**\n- The island loyalty review starts within **5 minutes**.\n- Control scores and owners will be checked.\n- Prepare your crew if you are contesting territory."
      );
   }

   private void queueReviewSummary(IslandClaimsData.ReviewSummary summary) {
      int color = summary.changedClaims() > 0 ? 15105570 : 3066993;
      OmpDiscordAnnouncements.queueEmbed(
         color,
         "[Ilhas] Status da analise | Island review status",
         "**PT-BR**\n- Status: analise automatica de 2h concluida.\n- Claims monitorados: **"
            + summary.totalClaims()
            + "**\n- Ilhas com mudanca de dono: **"
            + summary.changedClaims()
            + "**\n- Novas conquistas: **"
            + summary.conquered()
            + "** "
            + this.formatIslandList(summary.conqueredIslands(), false)
            + "\n- Transferencias: **"
            + summary.transferred()
            + "** "
            + this.formatIslandList(summary.transferredIslands(), false)
            + "\n- Proxima analise: em aproximadamente **2h**.\n\n**EN-US**\n- Status: automatic 2h review complete.\n- Tracked claims: **"
            + summary.totalClaims()
            + "**\n- Islands with owner changes: **"
            + summary.changedClaims()
            + "**\n- New conquests: **"
            + summary.conquered()
            + "** "
            + this.formatIslandList(summary.conqueredIslands(), true)
            + "\n- Transfers: **"
            + summary.transferred()
            + "** "
            + this.formatIslandList(summary.transferredIslands(), true)
            + "\n- Next review: in about **2h**."
      );
   }

   private String formatIslandList(List<String> islands, boolean english) {
      if (islands.isEmpty()) {
         return english ? "(none)" : "(nenhuma)";
      }

      int limit = Math.min(5, islands.size());
      List<String> names = new ArrayList<>();

      for (int i = 0; i < limit; i++) {
         String name = islands.get(i);
         names.add(english ? this.englishIsland(name) : name);
      }

      String suffix = islands.size() > limit ? " +" + (islands.size() - limit) : "";
      return "(" + String.join(", ", names) + suffix + ")";
   }

   private String englishLabel(String label) {
      if (label == null || label.isBlank() || "sem dono".equalsIgnoreCase(label)) {
         return "Unclaimed";
      } else if (label.startsWith("Tripulacao ")) {
         return "Crew " + label.substring("Tripulacao ".length());
      } else {
         return label.startsWith("Jogador ") ? "Player " + label.substring("Jogador ".length()) : label;
      }
   }

   private String englishIsland(String name) {
      return name != null && name.startsWith("Ilha ") ? "Island " + name.substring("Ilha ".length()) : name;
   }

   private String format(double value) {
      return String.format(Locale.US, "%.2f", value);
   }

   public void setDirty() {
      this.persist();
   }

   private synchronized void persist() {
      try {
         Files.createDirectories(PATH.getParent());
         NbtIo.write(this.save(new CompoundTag()), PATH);
      } catch (IOException ex) {
         OmpWorld.LOGGER.warn("Could not save island claims to {}.", PATH, ex);
      }
   }

   private enum OwnershipResult {
      NONE,
      CONQUERED,
      TRANSFERRED;
   }

   public record ReviewSummary(
      int conquered, int transferred, int changedClaims, int totalClaims, List<String> conqueredIslands, List<String> transferredIslands
   ) {
   }
}
