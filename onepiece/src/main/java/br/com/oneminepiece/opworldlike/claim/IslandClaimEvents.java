package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.OmpMessages;
import br.com.oneminepiece.opworldlike.OmpWorld;
import br.com.oneminepiece.opworldlike.compat.MinePieceAddonCompat;
import br.com.oneminepiece.opworldlike.integration.CrewInfo;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class IslandClaimEvents {
   private static final int AFK_TICKS = 6000;
   private static final int SCORE_TICKS = 1200;
   private static final long PLAYER_KILL_COOLDOWN_MS = 43200000L;
   private static final IslandClaimEvents INSTANCE = new IslandClaimEvents();
   private final Map<UUID, IslandClaimEvents.PlayerActivity> activity = new HashMap<>();
   private int weeklyCheckTicks;

   private IslandClaimEvents() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(INSTANCE::onServerTick);
      BlockEvents.USE_ITEM_ON.register(INSTANCE::onUseItemOn);
      PlayerBlockBreakEvents.BEFORE.register(INSTANCE::onBlockBreak);
      ServerLivingEntityEvents.AFTER_DEATH.register(IslandClaimEvents::onLivingDeath);
      ServerLivingEntityEvents.ALLOW_DAMAGE.register(INSTANCE::onAllowDamage);
      // TODO (mixins, Tier 5) : placement par entité non-joueur, ExplosionEvent.Detonate,
      // FluidPlaceBlockEvent, EntityMobGriefingEvent — pas d'équivalent direct Fabric API.
   }

   public static IslandClaimEvents getInstance() {
      return INSTANCE;
   }

   public void trackPlayerTick(ServerPlayer player) {
      IslandClaimEvents.PlayerActivity state = this.activity
         .computeIfAbsent(player.getUUID(), id -> new IslandClaimEvents.PlayerActivity(player.blockPosition(), player.tickCount));
      int tick = player.tickCount;
      if (state.lastPos.distSqr(player.blockPosition()) > 4.0) {
         state.lastActivityTick = tick;
         state.lastPos = player.blockPosition();
      }

      if (tick - state.lastScoreTick >= 1200 && tick - state.lastActivityTick <= 6000) {
         Optional<IslandDefinition> island = OnePieceGeography.claimIslandAt(((ServerLevel)player.level()).getSeed(), player.getBlockX(), player.getBlockZ());
         Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
         Optional<ScoreTarget> target = ScoreTarget.forPlayer(player, crew);
         if (island.isPresent() && island.get().claimable() && target.isPresent()) {
            double reputation = MinePieceBridge.reputationBonus(player);
            IslandClaimsData.get((ServerLevel)player.level()).addScore((ServerLevel)player.level(), island.get(), target.get(), 1.0 + reputation);
            state.lastScoreTick = tick;
         }
      }
   }

   private void onServerTick(MinecraftServer server) {
      this.weeklyCheckTicks++;
      if (this.weeklyCheckTicks >= 1200) {
         this.weeklyCheckTicks = 0;
         ServerLevel level = server.overworld();
         IslandClaimsData.get(level).runScheduledMaintenance(level);
      }
   }

   private InteractionResult onUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (level instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
         if (player instanceof ServerPlayer sp) {
            this.markActive(sp);
            Optional<IslandDefinition> island = IslandAccess.islandAt(serverLevel, pos);
            if (island.isPresent()) {
               if (this.shouldDenyBuild(sp, island.get(), pos)) {
                  this.deny(sp, "Esta area esta protegida pela tripulacao dominante.", "This area is protected by the dominant crew.");
                  return InteractionResult.FAIL;
               }

               BlockEntity blockEntity = level.getBlockEntity(pos);
               boolean container = blockEntity instanceof Container;
               IslandFlag flag = container ? IslandFlag.CONTAINERS : IslandFlag.INTERACT;
               if (this.shouldDenyProtectedFlag(sp, island.get(), flag, pos)) {
                  this.deny(sp, "Esta area esta protegida pela tripulacao dominante.", "This area is protected by the dominant crew.");
                  return InteractionResult.FAIL;
               }

               Optional<CrewInfo> crew = MinePieceBridge.crewOf(sp);
               Optional<ScoreTarget> target = ScoreTarget.forPlayer(sp, crew);
               if (island.get().claimable() && target.isPresent()) {
                  IslandClaim claim = IslandClaimsData.get(serverLevel).getOrCreate(island.get());
                  long posLong = pos.asLong();
                  if (claim.scoredPlacedBlocks.add(posLong)) {
                     IslandClaimsData.get(serverLevel).addScore(serverLevel, island.get(), target.get(), 0.25);
                     IslandClaimsData.get(serverLevel).setDirty();
                  }

                  if (claim.hasOwner() && IslandAccess.isOwnerMember(sp, island.get())) {
                     claim.addProtectedAnchor(pos);
                     IslandClaimsData.get(serverLevel).setDirty();
                  }
               }
            }
         }
      }

      return InteractionResult.PASS;
   }

   private boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
      if (player instanceof ServerPlayer sp && level.dimension() == Level.OVERWORLD) {
         this.markActive(sp);
         Optional<IslandDefinition> island = IslandAccess.islandAt((ServerLevel)level, pos);
         if (island.isPresent() && this.shouldDenyBuild(sp, island.get(), pos)) {
            this.deny(sp, "Esta area esta protegida pela tripulacao dominante.", "This area is protected by the dominant crew.");
            return false;
         } else {
            IslandClaim claim = island.<IslandClaim>map(value -> IslandClaimsData.get((ServerLevel)level).get(value.id())).orElse(null);
            if (claim != null) {
               claim.removeProtectedAnchor(pos);
               IslandClaimsData.get((ServerLevel)level).setDirty();
            }
         }
      }

      return true;
   }

   private static void onLivingDeath(LivingEntity entity, DamageSource source) {
      if (source.getEntity() instanceof ServerPlayer player && player.level().dimension() == Level.OVERWORLD) {
         Optional<IslandDefinition> island = OnePieceGeography.claimIslandAt(((ServerLevel)player.level()).getSeed(), player.getBlockX(), player.getBlockZ());
         Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
         Optional<ScoreTarget> target = ScoreTarget.forPlayer(player, crew);
         if (island.isPresent() && island.get().claimable() && target.isPresent()) {
            IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).getOrCreate(island.get());
            LivingEntity killed = entity;
            if (killed instanceof ServerPlayer victim) {
               String key = player.getUUID() + ":" + victim.getUUID();
               long now = System.currentTimeMillis();
               if (now - claim.playerKillCooldowns.getOrDefault(key, 0L) >= 43200000L) {
                  claim.playerKillCooldowns.put(key, now);
                  IslandClaimsData.get((ServerLevel)player.level()).addScore((ServerLevel)player.level(), island.get(), target.get(), 100.0);
                  IslandClaimsData.get((ServerLevel)player.level()).setDirty();
               }
            } else if (killed.getType().getCategory() == MobCategory.MONSTER) {
               IslandClaimsData.get((ServerLevel)player.level()).addScore((ServerLevel)player.level(), island.get(), target.get(), 5.0);
               IslandClaimsData.get((ServerLevel)player.level()).setDirty();
            }
         }
      }
   }

   private boolean onAllowDamage(LivingEntity entity, DamageSource source, float amount) {
      try {
         return this.handlePvp(entity, source);
      } catch (RuntimeException | LinkageError ex) {
         OmpWorld.LOGGER.warn("Island attack protection skipped after {}", ex.toString());
         return true;
      }
   }

   private boolean handlePvp(LivingEntity victim, DamageSource source) {
      if (victim instanceof ServerPlayer sp && sp.level().dimension() == Level.OVERWORLD) {
         Entity attacker = source.getEntity();
         if (this.isSkillDamage(source) && this.shouldDenySkillEffect((ServerLevel)sp.level(), sp.blockPosition())) {
            return false;
         } else if (attacker instanceof ServerPlayer) {
            Optional<IslandDefinition> island = OnePieceGeography.claimIslandAt(((ServerLevel)sp.level()).getSeed(), sp.getBlockX(), sp.getBlockZ());
            IslandClaim claim = island.<IslandClaim>map(value -> IslandClaimsData.get((ServerLevel)sp.level()).get(value.id())).orElse(null);
            if (claim != null && this.shouldDenyProtectedFlag((ServerLevel)sp.level(), attacker, island.orElse(null), claim, IslandFlag.PVP, sp.blockPosition())) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean shouldBlockEntityGrief(ServerLevel level, BlockPos pos, Entity entity) {
      if (this.shouldDenySkillEffect(level, pos)) {
         return true;
      }

      Optional<IslandDefinition> island = IslandAccess.islandAt(level, pos);
      IslandClaim claim = island.<IslandClaim>map(value -> IslandClaimsData.get(level).get(value.id())).orElse(null);
      return claim != null && this.shouldDenyProtectedFlag(level, entity, island.orElse(null), claim, IslandFlag.MOB_GRIEFING, pos);
   }

   private boolean shouldDenyBuild(ServerPlayer player, IslandDefinition island, BlockPos pos) {
      if (!island.claimable()) {
         return false;
      }

      IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).get(island.id());
      return claim != null && claim.hasOwner() && !claim.publicAllowed(IslandFlag.BUILD) && !IslandAccess.isOwnerOrTrusted(player, island)
         ? GriefProtectionConfig.enabled() && claim.isProtectedNear(pos, GriefProtectionConfig.protectedBuildRadius())
         : false;
   }

   private boolean shouldDenyProtectedFlag(ServerPlayer player, IslandDefinition island, IslandFlag flag, BlockPos pos) {
      IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).get(island.id());
      return this.shouldDenyProtectedFlag((ServerLevel)player.level(), player, island, claim, flag, pos);
   }

   private boolean shouldDenyProtectedFlag(ServerLevel level, Entity source, IslandDefinition island, IslandClaim claim, IslandFlag flag, BlockPos pos) {
      if (island == null || !island.claimable() || claim == null || !claim.hasOwner() || claim.publicAllowed(flag)) {
         return false;
      } else {
         return GriefProtectionConfig.enabled() && claim.isProtectedNear(pos, GriefProtectionConfig.protectedBuildRadius())
            ? !(source instanceof ServerPlayer player && IslandAccess.isOwnerOrTrusted(player, island))
            : false;
      }
   }

   private boolean shouldDenySkillEffect(ServerLevel level, BlockPos pos) {
      return ProtectedSkillEffectGuard.shouldBlockSkillEffect(level, pos);
   }

   private boolean isSkillDamage(DamageSource source) {
      String messageId = source.getMsgId().toLowerCase(Locale.ROOT);
      return !messageId.contains("ability")
            && !messageId.contains("minepiece")
            && !messageId.contains("haki")
            && !messageId.contains("devil")
            && !messageId.contains("gura")
            && !messageId.contains("mera")
         ? this.isAbilityEntity(source.getEntity()) || this.isAbilityEntity(source.getDirectEntity())
         : true;
   }

   private boolean isAbilityEntity(Entity entity) {
      if (entity == null) {
         return false;
      }

      Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      if (id != null && this.isAbilityNamespace(id.getNamespace())) {
         return true;
      }

      String className = entity.getClass().getName().toLowerCase(Locale.ROOT);
      return MinePieceAddonCompat.isMinePieceFamilyClass(className)
         || className.startsWith("com.solmochi.mmnmaddon.")
         || className.startsWith("com.inovactio.awakenawakennomi.")
         || className.startsWith("com.inovactio.awakenpath.")
         || className.startsWith("com.yumixlove.blackblades.")
         || className.startsWith("me.drqonic.hakibar.");
   }

   private boolean isAbilityNamespace(String namespace) {
      return MinePieceAddonCompat.isMinePieceFamilyNamespace(namespace)
         || "solsmmnmaddon".equals(namespace)
         || "awakenawakennomi".equals(namespace)
         || "awakenpath".equals(namespace)
         || "blackblades".equals(namespace)
         || "hakibar".equals(namespace);
   }

   private void markActive(ServerPlayer player) {
      IslandClaimEvents.PlayerActivity state = this.activity
         .computeIfAbsent(player.getUUID(), id -> new IslandClaimEvents.PlayerActivity(player.blockPosition(), player.tickCount));
      state.lastActivityTick = player.tickCount;
      state.lastPos = player.blockPosition();
   }

   private void deny(ServerPlayer player, String pt, String en) {
      player.sendSystemMessage(OmpMessages.component(player, pt, en), true);
   }

   private static final class PlayerActivity {
      private BlockPos lastPos;
      private int lastActivityTick;
      private int lastScoreTick;

      private PlayerActivity(BlockPos lastPos, int tick) {
         this.lastPos = lastPos;
         this.lastActivityTick = tick;
         this.lastScoreTick = tick;
      }
   }
}
