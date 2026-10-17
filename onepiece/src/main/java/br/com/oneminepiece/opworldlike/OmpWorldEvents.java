package br.com.oneminepiece.opworldlike;

import br.com.oneminepiece.opworldlike.claim.ClaimTitleExporter;
import br.com.oneminepiece.opworldlike.claim.GriefProtectionConfig;
import br.com.oneminepiece.opworldlike.claim.IslandClaim;
import br.com.oneminepiece.opworldlike.claim.IslandClaimEvents;
import br.com.oneminepiece.opworldlike.claim.IslandClaimsData;
import br.com.oneminepiece.opworldlike.crew.CrewPersistenceStore;
import br.com.oneminepiece.opworldlike.entity.CalmBeltMobSpawner;
import br.com.oneminepiece.opworldlike.entity.MariejoisMobSpawner;
import br.com.oneminepiece.opworldlike.entity.MinePieceIslandBossSpawner;
import br.com.oneminepiece.opworldlike.entity.ReverseMountainMobSpawner;
import br.com.oneminepiece.opworldlike.entity.WeakFactionMobSpawner;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import br.com.oneminepiece.opworldlike.spawn.PlayerSpawnData;
import br.com.oneminepiece.opworldlike.worldgen.FishmanIslandLayout;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import br.com.oneminepiece.opworldlike.worldgen.SkypieaLayout;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
import br.com.oneminepiece.opworldlike.worldgen.WorldSeamLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class OmpWorldEvents {
   private final IslandClaimEvents claimEvents;
   private final CalmBeltMobSpawner calmBeltMobSpawner = new CalmBeltMobSpawner();
   private final MariejoisMobSpawner mariejoisMobSpawner = new MariejoisMobSpawner();
   private final ReverseMountainMobSpawner reverseMountainMobSpawner = new ReverseMountainMobSpawner();
   private final WeakFactionMobSpawner weakFactionMobSpawner = new WeakFactionMobSpawner();
   private final MinePieceIslandBossSpawner minePieceIslandBossSpawner = new MinePieceIslandBossSpawner();
   private final Map<UUID, String> lastIslandByPlayer = new HashMap<>();
   private final Map<UUID, String> lastProtectedTerritoryByPlayer = new HashMap<>();
   private final Map<UUID, Long> lastRedLineWarningByPlayer = new HashMap<>();
   private final Map<UUID, Integer> calmBeltBoatExposureTicks = new HashMap<>();
   private final Map<UUID, Long> lastCalmBeltWarningByPlayer = new HashMap<>();
   private final Map<UUID, Long> fishmanRouteCooldownByEntity = new HashMap<>();
   private final Map<UUID, Long> skypieaStreamCooldownByEntity = new HashMap<>();
   private final Set<UUID> playersInsideCalmBelt = new HashSet<>();
   private final Map<UUID, OmpWorldEvents.CharacterResync> pendingCharacterResyncs = new HashMap<>();
   private int claimExportTicks;
   private int crewReconcileTicks;

   public OmpWorldEvents(IslandClaimEvents claimEvents) {
      this.claimEvents = claimEvents;
   }

   public static void register(IslandClaimEvents claimEvents) {
      OmpWorldEvents events = new OmpWorldEvents(claimEvents);
      ServerLifecycleEvents.SERVER_STARTED.register(events::onServerStarted);
      ServerLifecycleEvents.SERVER_STOPPING.register(events::onServerStopping);
      ServerTickEvents.END_SERVER_TICK.register(events::onServerTick);
      ServerTickEvents.END_SERVER_TICK.register(events::onPlayersTick);
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> events.onPlayerLogin(handler.player));
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> events.onPlayerLogout(handler.player));
      UseEntityCallback.EVENT.register(events::onEntityInteract);
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> events.onPlayerRespawn(newPlayer));
   }

   public void onServerStarted(MinecraftServer server) {
      ServerLevel overworld = server.overworld();
      this.validatePlayableCatalog(overworld);
      overworld.getWorldBorder().setWarningBlocks(0);
      overworld.getWorldBorder().setWarningTime(0);
      overworld.getWorldBorder().setSize(OmpWorld.worldRadius() * 2.0);
      BlockPos starterSpawn = this.safeSpawnInLevel(overworld, UniqueIslandCatalog.EAST_BLUE_STARTER);
      overworld.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, starterSpawn, 0.0F, 0.0F));
      if (this.serverCompetition()) {
         CrewPersistenceStore.restoreMissingCrews();
         CrewPersistenceStore.saveCurrent("server started");
         ClaimTitleExporter.export(overworld);
         MinePieceBridge.logPotentialGriefSources();
      }

      OmpWorld.LOGGER
         .info(
            "One Piece overworld boundary configured at {}x{} with {}-block rounded corners; starter spawn set at {}.",
            new Object[]{OmpWorld.worldRadius() * 2, OmpWorld.worldHalfHeight() * 2, OmpWorld.worldCornerRadius(), starterSpawn}
         );
   }

   private void validatePlayableCatalog(ServerLevel level) {
      List<String> undetectable = OnePieceGeography.allClaimIslands(level.getSeed())
         .stream()
         .filter(
            island -> !OnePieceGeography.claimIslandAt(level.getSeed(), island.centerX(), island.centerZ())
               .map(detected -> detected.id().equals(island.id()))
               .orElse(false)
         )
         .map(IslandDefinition::id)
         .toList();
      if (!undetectable.isEmpty()) {
         // Îles mineures procédurales : un recouvrement ponctuel dépendant du seed peut rendre le centre
         // « indétectable » (résolu vers l'île voisine). Non-bloquant : l'île existe toujours dans le monde.
         OmpWorld.LOGGER.warn("One Piece catalog contains {} undetectable minor islands (non-fatal, continuing): {}", undetectable.size(), undetectable);
      } else {
         OmpWorld.LOGGER.info("Validated that every One Piece route island is detectable at its center.");
      }
   }

   public void onServerStopping(MinecraftServer server) {
      if (this.serverCompetition()) {
         CrewPersistenceStore.saveCurrent("server stopping");
      }
   }

   public void onServerTick(MinecraftServer server) {
      this.minePieceIslandBossSpawner.tick(server);
      if (this.claimEvents != null) {
         this.crewReconcileTicks++;
         if (this.crewReconcileTicks >= 1200) {
            this.crewReconcileTicks = 0;
            this.reconcileCrews(server, null);
         }

         this.claimExportTicks++;
         if (this.claimExportTicks >= 1200) {
            this.claimExportTicks = 0;
            ClaimTitleExporter.export(server.overworld());
         }
      }
   }

   public void onPlayersTick(MinecraftServer server) {
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         this.tickPlayer(player);
      }
   }

   private void tickPlayer(ServerPlayer player) {
      if (player.level().dimension() == Level.OVERWORLD) {
         this.runPendingCharacterResync(player);
         this.wrapIfNeeded(player);
         this.assistReverseMountainBoat(player);
         this.handleFishmanRoute(player);
         this.handleSkypieaKnockUpStream(player);
         this.touchNearbyRedLineItems(player);
         this.enforceRedLineHakiGate(player);
         this.applyCalmBeltHazards(player);
         this.announceIslandIfChanged(player);
         if (this.serverCompetition()) {
            this.announceProtectedTerritoryIfChanged(player);
         }

         this.calmBeltMobSpawner.tick(player);
         this.mariejoisMobSpawner.tick(player);
         this.reverseMountainMobSpawner.tick(player);
         this.weakFactionMobSpawner.tick(player);
         if (this.claimEvents != null) {
            this.claimEvents.trackPlayerTick(player);
         }
      }
   }

   public void onPlayerLogin(ServerPlayer player) {
      if (player.level().dimension() == Level.OVERWORLD) {
         PlayerSpawnData spawnData = PlayerSpawnData.get(player.level());
         boolean firstAssignment = !spawnData.hasAssignment(player.getUUID());
         IslandDefinition island = spawnData.assignIfAbsent(player.getUUID());
         if (firstAssignment) {
            this.teleportToStarter(player, island, "Spawn inicial definido em " + island.displayName() + ".");
         }
      }

      if (this.serverCompetition()) {
         this.reconcileCrews(player.level().getServer(), player);
         this.scheduleCharacterResync(player);
      }
   }

   public void onPlayerLogout(ServerPlayer player) {
      this.lastIslandByPlayer.remove(player.getUUID());
      this.lastProtectedTerritoryByPlayer.remove(player.getUUID());
      this.lastRedLineWarningByPlayer.remove(player.getUUID());
      this.lastCalmBeltWarningByPlayer.remove(player.getUUID());
      this.fishmanRouteCooldownByEntity.remove(player.getUUID());
      this.skypieaStreamCooldownByEntity.remove(player.getUUID());
      this.playersInsideCalmBelt.remove(player.getUUID());
      this.pendingCharacterResyncs.remove(player.getUUID());
      this.mariejoisMobSpawner.forget(player.getUUID());
      this.reverseMountainMobSpawner.forget(player.getUUID());
   }

   public InteractionResult onEntityInteract(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
      if (player instanceof ServerPlayer sp) {
         ItemStack stack = sp.getItemInHand(hand);
         if (KairosekiBoatHelper.isSupportedBoat(entity) && KairosekiBoatHelper.isDenseKairoseki(stack)) {
            if (KairosekiBoatHelper.hasNativeCoating(entity)) {
               KairosekiBoatHelper.sendProgress(sp, entity);
            } else if (KairosekiBoatHelper.applyFallbackCoating(sp, entity, stack)) {
               return InteractionResult.SUCCESS;
            }
         }
      }

      return InteractionResult.PASS;
   }

   public void onPlayerRespawn(ServerPlayer player) {
      if (player.level().dimension() == Level.OVERWORLD && (player.getRespawnConfig() == null || !player.getRespawnConfig().forced())) {
         IslandDefinition island = PlayerSpawnData.get(player.level()).assignIfAbsent(player.getUUID());
         this.teleportToStarter(player, island, "Voce renasceu em " + island.displayName() + ".");
      }
   }

   private void wrapIfNeeded(ServerPlayer player) {
      double x = player.getX();
      double z = player.getZ();
      double targetX = x;
      double targetZ = z;
      if (WorldSeamLayout.shouldWrapX(x, z)) {
         targetX = WorldSeamLayout.wrappedX(x, z);
      }

      if (WorldSeamLayout.shouldWrapZ(x, z)) {
         targetZ = WorldSeamLayout.wrappedZ(x, z);
      }

      if (targetX != x || targetZ != z) {
         Entity teleportTarget = player.getRootVehicle();
         player.level().getChunk(new BlockPos((int)Math.floor(targetX), player.getBlockY(), (int)Math.floor(targetZ)));
         teleportTarget.teleportTo(targetX, teleportTarget.getY(), targetZ);
         teleportTarget.setDeltaMovement(teleportTarget.getDeltaMovement());
         player.sendSystemMessage(Component.literal("Voce cruzou a borda do mundo."), true);
      }
   }

   private void enforceRedLineHakiGate(ServerPlayer player) {
      if (player.tickCount % 5 == 0 && !player.isCreative() && !player.isSpectator() && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         if (OnePieceGeography.isHighRedLine(player.getBlockX(), player.getBlockZ(), player.getBlockY())) {
            if (!MinePieceBridge.hasAdeptBusoshoku(player)) {
               Vec3 push = this.redLinePushVector(player);
               player.setDeltaMovement(push.z * 1.9, 0.55, push.x * 1.9);
               long now = player.level().getGameTime();
               long previous = this.lastRedLineWarningByPlayer.getOrDefault(player.getUUID(), -200L);
               if (now - previous >= 80L) {
                  this.lastRedLineWarningByPlayer.put(player.getUUID(), now);
                  player.sendSystemMessage(Component.literal("A Red Line repele quem nao domina Busoshoku Haki Adept."), true);
               }
            }
         }
      }
   }

   private void touchNearbyRedLineItems(ServerPlayer player) {
      if (!player.isSpectator() && OnePieceGeography.isHighRedLine(player.getBlockX(), player.getBlockZ(), player.getBlockY())) {
         for (ItemEntity item : player.level()
            .getEntities(
               EntityTypeTest.forClass(ItemEntity.class),
               player.getBoundingBox().inflate(1.0, 0.75, 1.0),
               itemx -> itemx.isAlive() && OnePieceGeography.isHighRedLine(itemx.getBlockX(), itemx.getBlockZ(), itemx.getBlockY())
            )) {
            item.setExtendedLifetime();
         }
      }
   }

   private void assistReverseMountainBoat(ServerPlayer player) {
      if (player.getVehicle() instanceof Boat boat) {
         int x = boat.getBlockX();
         int z = boat.getBlockZ();
         if (OnePieceGeography.isReverseMountainChannel(x, z)) {
            int waterLevel = OnePieceGeography.reverseChannelWaterLevel(player.level().getSeed(), x, z);
            double targetY = waterLevel + 0.08;
            if (!(boat.getY() >= targetY)) {
               Vec3 movement = boat.getDeltaMovement();
               double lift = Math.min(0.24, Math.max(0.08, (targetY - boat.getY()) * 0.08));
               boat.setDeltaMovement(movement.z, Math.max(movement.y, lift), movement.x);
            }
         }
      }
   }

   private void handleFishmanRoute(ServerPlayer player) {
      Entity target = player.getRootVehicle();
      long now = player.level().getGameTime();
      if (now >= this.fishmanRouteCooldownByEntity.getOrDefault(target.getUUID(), 0L)) {
         if (FishmanIslandLayout.isPortalTrigger(target)) {
            BlockPos entry = FishmanIslandLayout.entryPos(player.level().getSeed());
            this.teleportRouteTarget(player, target, entry, new Vec3(0.16, 0.02, -0.02), "Corrente submarina: Ilha dos Homens-Peixe.");
            this.fishmanRouteCooldownByEntity.put(target.getUUID(), now + 80L);
         } else {
            if (FishmanIslandLayout.isInApproachRoute(target)) {
               Vec3 movement = target.getDeltaMovement();
               Vec3 assist = FishmanIslandLayout.routeAssistVector(target);
               if (KairosekiBoatHelper.isSupportedBoat(target)) {
                  target.setDeltaMovement(
                     movement.z * 0.88 + assist.z, movement.y * 0.72 + assist.y, movement.x * 0.88 + assist.x
                  );
               } else {
                  target.setDeltaMovement(
                     movement.z * 0.72 + assist.z, movement.y * 0.55 + assist.y, movement.x * 0.72 + assist.x
                  );
               }

               target.syncVelocity = true;
            }

            if (FishmanIslandLayout.isInHotStream(target, player.level().getSeed())) {
               if (FishmanIslandLayout.reachedHotStreamTop(target)) {
                  BlockPos exit = FishmanIslandLayout.oceanExitPos();
                  this.teleportRouteTarget(player, target, exit, new Vec3(0.0, 0.26, 0.0), "Hot stream: retorno ao oceano.");
                  this.fishmanRouteCooldownByEntity.put(target.getUUID(), now + 80L);
               } else {
                  Vec3 movement = target.getDeltaMovement();
                  target.setDeltaMovement(movement.z * 0.82, Math.max(movement.y, 0.31), movement.x * 0.82);
                  target.syncVelocity = true;
               }
            }
         }
      }
   }

   private void handleSkypieaKnockUpStream(ServerPlayer player) {
      Entity target = player.getRootVehicle();
      long now = player.level().getGameTime();
      if (now >= this.skypieaStreamCooldownByEntity.getOrDefault(target.getUUID(), 0L)) {
         long seed = player.level().getSeed();
         if (SkypieaLayout.isInKnockUpStream(target, seed)) {
            if (SkypieaLayout.reachedSkyTop(target, seed)) {
               BlockPos landing = SkypieaLayout.landingPos(seed);
               this.teleportRouteTarget(player, target, landing, new Vec3(0.22, 0.08, 0.0), "Knock Up Stream: Skypiea.");
               this.skypieaStreamCooldownByEntity.put(target.getUUID(), now + 120L);
            } else {
               Vec3 movement = target.getDeltaMovement();
               Vec3 assist = SkypieaLayout.streamAssistVector(target, seed);
               double horizontalDamping = KairosekiBoatHelper.isSupportedBoat(target) ? 0.58 : 0.68;
               target.setDeltaMovement(
                  movement.z * horizontalDamping + assist.z,
                  Math.max(movement.y, assist.y),
                  movement.x * horizontalDamping + assist.x
               );
               target.syncVelocity = true;
               if (target == player) {
                  player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 1, false, false));
               }
            }
         }
      }
   }

   private void teleportRouteTarget(ServerPlayer player, Entity target, BlockPos pos, Vec3 movement, String message) {
      player.level().getChunk(pos);
      target.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
      target.setDeltaMovement(movement);
      target.syncVelocity = true;
      player.sendSystemMessage(Component.literal(message), true);
   }

   private void applyCalmBeltHazards(ServerPlayer player) {
      boolean inDanger = OnePieceGeography.isCalmBelt(player.getBlockX(), player.getBlockZ())
         && !OnePieceGeography.isReverseMountainChannel(player.getBlockX(), player.getBlockZ());
      Entity vehicle = player.getVehicle();
      if (inDanger && !player.isCreative() && !player.isSpectator()) {
         if (this.playersInsideCalmBelt.add(player.getUUID())) {
            player.sendSystemMessage(OmpMessages.component(player, "Voce entrou no Calm Belt.", "You entered the Calm Belt."), true);
         }

         boolean leftSide = player.getBlockX() < 0;
         int slownessAmplifier = leftSide ? 1 : 0;
         player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, slownessAmplifier, true, true));
         boolean vehicleIsBoat = KairosekiBoatHelper.isSupportedBoat(vehicle);
         if (vehicleIsBoat && KairosekiBoatHelper.isFullyCoated(vehicle)) {
            this.calmBeltBoatExposureTicks.remove(vehicle.getUUID());
         } else if (vehicleIsBoat) {
            this.warnCalmBelt(
               player,
               "Barcos comuns nao atravessam o Calm Belt. Revestimento de kairoseki necessario.",
               "Common boats cannot cross the Calm Belt. Kairoseki coating required."
            );
            int ticks = this.calmBeltBoatExposureTicks.merge(vehicle.getUUID(), 1, Integer::sum);
            if (ticks >= 60) {
               vehicle.ejectPassengers();
               vehicle.hurt(vehicle.damageSources().generic(), 100.0F);
               if (!vehicle.isRemoved()) {
                  vehicle.discard();
               }

               this.calmBeltBoatExposureTicks.remove(vehicle.getUUID());
            }
         } else if (player.isInWater()) {
            float damage = leftSide ? 6.0F : 3.0F;
            if (player.tickCount % 20 == 0) {
               player.hurt(player.damageSources().magic(), damage);
            }
         } else if (vehicle == null && !player.onGround()) {
            this.pullFlyingPlayerDown(player);
         }
      } else {
         this.leaveCalmBelt(player, vehicle);
      }
   }

   private void leaveCalmBelt(ServerPlayer player, Entity vehicle) {
      this.playersInsideCalmBelt.remove(player.getUUID());
      if (vehicle != null) {
         this.calmBeltBoatExposureTicks.remove(vehicle.getUUID());
      }
   }

   private void warnCalmBelt(ServerPlayer player, String pt, String en) {
      long now = player.level().getGameTime();
      long previous = this.lastCalmBeltWarningByPlayer.getOrDefault(player.getUUID(), -200L);
      if (now - previous >= 100L) {
         this.lastCalmBeltWarningByPlayer.put(player.getUUID(), now);
         player.sendSystemMessage(OmpMessages.component(player, pt, en), true);
      }
   }

   private void pullFlyingPlayerDown(ServerPlayer player) {
      Vec3 movement = player.getDeltaMovement();
      if (!(movement.y <= -0.08)) {
         player.setDeltaMovement(movement.z, Math.max(-0.08, movement.y - 0.015), movement.x);
      }
   }

   private Vec3 redLinePushVector(ServerPlayer player) {
      double x = player.getX();
      double z = player.getZ();
      double dx;
      double dz;
      if (OnePieceGeography.isReverseMountainArea(player.getBlockX(), player.getBlockZ())) {
         dx = x;
         dz = z * 0.08;
      } else {
         int radius = OmpWorld.worldRadius();
         double coreX = Math.abs(x) > radius * 0.75 ? Math.copySign(radius, x) : 0.0;
         dx = x - coreX;
         dz = z * 0.12;
      }

      double length = Math.sqrt(dx * dx + dz * dz);
      if (length < 0.001) {
         dx = x >= 0.0 ? 1.0 : -1.0;
         dz = 0.0;
         length = 1.0;
      }

      return new Vec3(dx / length, 0.0, dz / length);
   }

   private void teleportToStarter(ServerPlayer player, IslandDefinition island, String message) {
      BlockPos pos = this.safeSpawnInLevel(player.level(), island);
      if (player.getVehicle() != null) {
         player.stopRiding();
      }

      player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
      player.sendSystemMessage(Component.literal(message), true);
   }

   private void scheduleCharacterResync(ServerPlayer player) {
      long now = player.level().getGameTime();
      this.pendingCharacterResyncs.put(player.getUUID(), new OmpWorldEvents.CharacterResync(now + 20L, 0));
   }

   private void runPendingCharacterResync(ServerPlayer player) {
      OmpWorldEvents.CharacterResync state = this.pendingCharacterResyncs.get(player.getUUID());
      if (state != null) {
         long now = player.level().getGameTime();
         if (now >= state.nextTick) {
            MinePieceBridge.resyncCharacterData(player);
            int nextAttempt = state.attempt + 1;
            if (nextAttempt >= 3) {
               this.pendingCharacterResyncs.remove(player.getUUID());
            } else {
               long delay = nextAttempt == 1 ? 40L : 60L;
               this.pendingCharacterResyncs.put(player.getUUID(), new OmpWorldEvents.CharacterResync(now + delay, nextAttempt));
            }
         }
      }
   }

   private void reconcileCrews(MinecraftServer server, ServerPlayer focus) {
      if (this.serverCompetition()) {
         int restored = CrewPersistenceStore.restoreMissingCrews();
         if (restored > 0) {
            CrewPersistenceStore.saveCurrent("crew reconcile");
            if (focus != null) {
               MinePieceBridge.resyncCharacterData(focus);
               this.scheduleCharacterResync(focus);
            } else {
               for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                  MinePieceBridge.resyncCharacterData(player);
                  this.scheduleCharacterResync(player);
               }
            }
         }
      }
   }

   private void announceIslandIfChanged(ServerPlayer player) {
      String currentIslandId = OnePieceGeography.claimIslandAt(player.level().getSeed(), player.getBlockX(), player.getBlockZ())
         .map(IslandDefinition::id)
         .orElse("");
      String previousIslandId = this.lastIslandByPlayer.put(player.getUUID(), currentIslandId);
      if (!currentIslandId.equals(previousIslandId) && !currentIslandId.isBlank()) {
         OnePieceGeography.claimIslandAt(player.level().getSeed(), player.getBlockX(), player.getBlockZ()).ifPresent(island -> {
            String message = "Ilha: " + island.displayName() + " | Tamanho: " + this.sizeLabel(island);
            if (this.serverCompetition()) {
               message = message + " | Dono: " + this.ownerLabel(player.level(), island);
            }

            player.sendSystemMessage(Component.literal(message), true);
         });
      }
   }

   private void announceProtectedTerritoryIfChanged(ServerPlayer player) {
      String current = this.protectedTerritoryId(player);
      String previous = this.lastProtectedTerritoryByPlayer.put(player.getUUID(), current);
      if (!Objects.equals(current, previous)) {
         if (!current.isBlank()) {
            IslandDefinition island = this.islandById(player.level(), current);
            IslandClaim claim = island == null ? null : IslandClaimsData.get(player.level()).get(island.id());
            if (island != null && claim != null) {
               this.showTerritoryMessage(
                  player,
                  claim.entryMessage,
                  "Territorio protegido: " + island.displayName() + " | Dono: " + claim.ownerLabel(),
                  "Protected territory: " + this.englishIsland(island.displayName()) + " | Owner: " + this.englishOwner(claim.ownerLabel()),
                  island,
                  claim
               );
            }
         } else if (previous != null && !previous.isBlank()) {
            IslandDefinition island = this.islandById(player.level(), previous);
            IslandClaim claim = island == null ? null : IslandClaimsData.get(player.level()).get(island.id());
            String islandName = island == null ? this.maskedIslandId(previous) : island.displayName();
            this.showTerritoryMessage(
               player,
               claim == null ? "" : claim.exitMessage,
               "Saiu do territorio protegido: " + islandName,
               "Left protected territory: " + this.englishIsland(islandName),
               island,
               claim
            );
         }
      }
   }

   private String protectedTerritoryId(ServerPlayer player) {
      if (this.claimEvents != null && GriefProtectionConfig.enabled()) {
         IslandDefinition island = OnePieceGeography.claimIslandAt(player.level().getSeed(), player.getBlockX(), player.getBlockZ()).orElse(null);
         if (island != null && island.claimable()) {
            IslandClaim claim = IslandClaimsData.get(player.level()).get(island.id());
            return claim != null && claim.hasOwner() && claim.isProtectedNear(player.blockPosition(), GriefProtectionConfig.protectedBuildRadius())
               ? island.id()
               : "";
         } else {
            return "";
         }
      } else {
         return "";
      }
   }

   private IslandDefinition islandById(ServerLevel level, String islandId) {
      return islandId != null && !islandId.isBlank()
         ? UniqueIslandCatalog.byId(islandId)
            .or(() -> OnePieceGeography.allClaimIslands(level.getSeed()).stream().filter(island -> islandId.equals(island.id())).findFirst())
            .orElse(null)
         : null;
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

   private void showTerritoryMessage(ServerPlayer player, String custom, String fallbackPt, String fallbackEn, IslandDefinition island, IslandClaim claim) {
      if (custom != null && !custom.isBlank()) {
         player.sendSystemMessage(Component.literal(this.renderTerritoryMessage(custom, island, claim)), true);
      } else {
         player.sendSystemMessage(OmpMessages.component(player, fallbackPt, fallbackEn), true);
      }
   }

   private String renderTerritoryMessage(String message, IslandDefinition island, IslandClaim claim) {
      String islandName = island == null ? "" : island.displayName();
      String owner = claim == null ? "" : claim.ownerLabel();
      return message.replace("<island>", islandName).replace("<owner>", owner);
   }

   private String sizeLabel(IslandDefinition island) {
      return switch (island.size()) {
         case TINY -> "muito pequena";
         case SMALL -> "pequena";
         case MEDIUM -> "media";
         case LARGE -> "grande";
         case HUGE -> "enorme";
      };
   }

   private String ownerLabel(ServerLevel level, IslandDefinition island) {
      if (!island.claimable()) {
         return "sem claim";
      }

      if (this.claimEvents == null) {
         return "sem dono";
      }

      IslandClaim claim = IslandClaimsData.get(level).get(island.id());
      return claim != null && claim.hasOwner() ? claim.ownerLabel() : "sem dono";
   }

   private String englishIsland(String name) {
      return name != null && name.startsWith("Ilha ") ? "Island " + name.substring("Ilha ".length()) : name;
   }

   private String englishOwner(String label) {
      if (label == null || label.isBlank() || "sem dono".equalsIgnoreCase(label)) {
         return "Unclaimed";
      } else if (label.startsWith("Tripulacao ")) {
         return "Crew " + label.substring("Tripulacao ".length());
      } else {
         return label.startsWith("Jogador ") ? "Player " + label.substring("Jogador ".length()) : label;
      }
   }

   private BlockPos safeSpawnInLevel(ServerLevel level, IslandDefinition island) {
      BlockPos base = OnePieceGeography.safeSpawnFor(level.getSeed(), island);
      BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(base.getX(), 0, base.getZ()));
      return surface.getY() <= 63 ? base : surface.above();
   }

   private boolean serverCompetition() {
      return this.claimEvents != null && OmpFeatureGates.serverCompetition();
   }

   private record CharacterResync(long nextTick, int attempt) {
   }
}
