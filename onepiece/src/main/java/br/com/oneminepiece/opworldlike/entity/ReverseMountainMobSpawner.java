package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class ReverseMountainMobSpawner {
   private static final Identifier[] CANDIDATES = ids(
      "minepiece:fighting_fish",
      "minepiece:fighting_fish",
      "minepiece:fighting_fish",
      "minepiece:flying_fish",
      "minepiece:flying_fish",
      "minepiece:panda_shark",
      "minepiece:panda_shark",
      "minepiece:sea_cow",
      "minepiece:sea_king",
      "minecraft:cod",
      "minecraft:cod",
      "minecraft:salmon",
      "minecraft:salmon",
      "minecraft:tropical_fish",
      "minecraft:tropical_fish",
      "minecraft:pufferfish",
      "minecraft:squid",
      "minecraft:dolphin",
      "upgrade_aquatic:perch",
      "upgrade_aquatic:pike",
      "upgrade_aquatic:lionfish",
      "upgrade_aquatic:jellyfish",
      "upgrade_aquatic:thrasher",
      "upgrade_aquatic:great_thrasher"
   );
   private static final Set<Identifier> TRACKED_TYPES = Arrays.stream(CANDIDATES).collect(Collectors.toUnmodifiableSet());
   private final Map<UUID, Integer> nextAttemptTick = new HashMap<>();

   public void tick(ServerPlayer player) {
      if (ReverseMountainSpawnConfig.enabled()
         && player.level().dimension() == Level.OVERWORLD
         && !player.isCreative()
         && !player.isSpectator()
         && OnePieceGeography.isReverseMountainChannel(player.getBlockX(), player.getBlockZ())) {
         int tick = player.tickCount;
         int next = this.nextAttemptTick.getOrDefault(player.getUUID(), 0);
         if (tick >= next) {
            int cooldown = ReverseMountainSpawnConfig.attemptCooldownTicks();
            this.nextAttemptTick.put(player.getUUID(), tick + cooldown + player.getRandom().nextInt(Math.max(20, cooldown)));
            if (!(player.getRandom().nextDouble() > ReverseMountainSpawnConfig.attemptChance())) {
               ServerLevel level = (ServerLevel) player.level();
               if (this.nearbyChannelMobs(level, player) < ReverseMountainSpawnConfig.maxNearby()) {
                  this.spawnGroup(player);
               }
            }
         }
      }
   }

   public void forget(UUID playerId) {
      this.nextAttemptTick.remove(playerId);
   }

   private int nearbyChannelMobs(ServerLevel level, ServerPlayer player) {
      AABB area = AABB.ofSize(player.position(), 112.0, 96.0, 112.0);
      return level.getEntitiesOfClass(Entity.class, area, entity -> {
         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         return id != null && TRACKED_TYPES.contains(id);
      }).size();
   }

   private void spawnGroup(ServerPlayer player) {
      ServerLevel level = (ServerLevel) player.level();
      RandomSource random = player.getRandom();
      int min = ReverseMountainSpawnConfig.minGroup();
      int max = Math.max(min, ReverseMountainSpawnConfig.maxGroup());
      int count = min + random.nextInt(max - min + 1);
      int spawned = 0;

      for (int i = 0; i < count; i++) {
         BlockPos pos = this.findWaterPosition(player, random);
         if (pos != null) {
            EntityType<?> type = this.resolveType(random);
            if (type != null) {
               Entity entity = type.spawn(level, pos, EntitySpawnReason.NATURAL);
               if (entity != null) {
                  entity.setPos(pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5);

                  entity.setYRot(random.nextFloat() * 360.0F);

                  entity.setXRot(0.0F);
                  spawned++;
               }
            }
         }
      }
   }

   private BlockPos findWaterPosition(ServerPlayer player, RandomSource random) {
      ServerLevel level = (ServerLevel) player.level();
      int minRadius = ReverseMountainSpawnConfig.minRadius();
      int maxRadius = Math.max(minRadius, ReverseMountainSpawnConfig.maxRadius());
      int radiusRange = maxRadius - minRadius + 1;
      long seed = level.getSeed();

      for (int attempt = 0; attempt < 24; attempt++) {
         double angle = random.nextDouble() * Math.PI * 2.0;
         int radius = minRadius + random.nextInt(radiusRange);
         int x = player.getBlockX() + (int)Math.round(Math.cos(angle) * radius);
         int z = player.getBlockZ() + (int)Math.round(Math.sin(angle) * radius);
         if (OnePieceGeography.isReverseMountainChannel(x, z)) {
            int waterLevel = OnePieceGeography.reverseChannelWaterLevel(seed, x, z);
            int floor = OnePieceGeography.reverseChannelFloor(seed, x, z);
            int minY = Math.max(level.dimensionType().minY() + 1, floor + 2);
            int maxY = Math.min(level.dimensionType().minY() + level.dimensionType().height() - 2, waterLevel - 1);
            if (maxY > minY) {
               int y = minY + random.nextInt(maxY - minY + 1);
               BlockPos pos = new BlockPos(x, y, z);
               if (level.isLoaded(pos) && level.getFluidState(pos).is(FluidTags.WATER) && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                  return pos;
               }
            }
         }
      }

      return null;
   }

   private EntityType<?> resolveType(RandomSource random) {
      for (int attempt = 0; attempt < CANDIDATES.length; attempt++) {
         Identifier id = CANDIDATES[random.nextInt(CANDIDATES.length)];
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(id);
         if (type != null) {
            return type;
         }
      }

      return null;
   }

   private static Identifier[] ids(String... values) {
      Identifier[] ids = new Identifier[values.length];

      for (int i = 0; i < values.length; i++) {
         ids[i] = Identifier.parse(values[i]);
      }

      return ids;
   }
}
