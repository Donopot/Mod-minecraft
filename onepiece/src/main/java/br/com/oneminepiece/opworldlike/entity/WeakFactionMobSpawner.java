package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;

public final class WeakFactionMobSpawner {
   private static final Identifier[] CANDIDATES = ids(
      "minepiece",
      "marine_grunt",
      "marine_with_sword",
      "marine_with_gun",
      "pirate_grunt",
      "pirate_with_sword",
      "pirate_with_gun",
      "bandit_grunt",
      "bandit_with_sword",
      "revolutionary_grunt",
      "revolutionary_soldier",
      "revolutionary_with_sword"
   );
   private final Map<UUID, Integer> nextAttemptTick = new HashMap<>();

   public void tick(ServerPlayer player) {
      if (MobBalanceConfig.enabled()
         && MobBalanceConfig.weakFactionSpawnsEnabled()
         && player.level().dimension() == Level.OVERWORLD
         && !player.isCreative()
         && !player.isSpectator()) {
         if (!OnePieceGeography.isCalmBelt(player.getBlockX(), player.getBlockZ())
            && !OnePieceGeography.isHighRedLine(player.getBlockX(), player.getBlockZ(), player.getBlockY())) {
            int tick = player.tickCount;
            int next = this.nextAttemptTick.getOrDefault(player.getUUID(), 0);
            if (tick >= next) {
               ServerLevel level = (ServerLevel) player.level();
               boolean night = isNight(level);
               int cooldown = MobBalanceConfig.weakFactionCooldownTicks(night);
               this.nextAttemptTick.put(player.getUUID(), tick + cooldown + player.getRandom().nextInt(Math.max(20, cooldown / 2)));
               if (!(player.getRandom().nextDouble() > MobBalanceConfig.weakFactionAttemptChance(night))) {
                  if (this.nearbyFactionMobs(level, player) < MobBalanceConfig.weakFactionMaxNearby()) {
                     this.spawnGroup(player);
                  }
               }
            }
         }
      }
   }

   private int nearbyFactionMobs(ServerLevel level, ServerPlayer player) {
      AABB area = AABB.ofSize(player.position(), 96.0, 48.0, 96.0);
      return level.getEntitiesOfClass(Mob.class, area, mob -> {
         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
         return id != null && "minepiece".equals(id.getNamespace()) && isFactionMobPath(id.getPath());
      }).size();
   }

   private void spawnGroup(ServerPlayer player) {
      ServerLevel level = (ServerLevel) player.level();
      RandomSource random = player.getRandom();
      EntityType<?> type = this.resolveType(random);
      if (type != null) {
         int groupMin = MobBalanceConfig.weakFactionMinGroup();
         int groupMax = Math.max(groupMin, MobBalanceConfig.weakFactionMaxGroup());
         int count = groupMin + random.nextInt(groupMax - groupMin + 1);
         BlockPos anchor = this.findSpawnAnchor(player, random);
         if (anchor != null) {
            for (int i = 0; i < count; i++) {
               BlockPos pos = this.findNearbyGround(level, anchor, random);
               if (pos != null) {
                  Entity entity = type.spawn(level, pos, EntitySpawnReason.NATURAL);
                  if (entity != null) {
                     entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                     entity.setYRot(random.nextFloat() * 360.0F);

                     entity.setXRot(0.0F);
                  }
               }
            }
         }
      }
   }

   private BlockPos findSpawnAnchor(ServerPlayer player, RandomSource random) {
      int minRadius = MobBalanceConfig.weakFactionMinRadius();
      int maxRadius = Math.max(minRadius, MobBalanceConfig.weakFactionMaxRadius());
      int radiusRange = maxRadius - minRadius + 1;
      ServerLevel level = (ServerLevel) player.level();

      for (int attempt = 0; attempt < 16; attempt++) {
         double angle = random.nextDouble() * Math.PI * 2.0;
         int radius = minRadius + random.nextInt(radiusRange);
         int x = player.getBlockX() + (int)Math.round(Math.cos(angle) * radius);
         int z = player.getBlockZ() + (int)Math.round(Math.sin(angle) * radius);
         BlockPos pos = this.surface(level, x, z);
         if (this.canSpawnAt(level, pos)) {
            return pos;
         }
      }

      return null;
   }

   private BlockPos findNearbyGround(ServerLevel level, BlockPos anchor, RandomSource random) {
      for (int attempt = 0; attempt < 8; attempt++) {
         int x = anchor.getX() + random.nextInt(9) - 4;
         int z = anchor.getZ() + random.nextInt(9) - 4;
         BlockPos pos = this.surface(level, x, z);
         if (this.canSpawnAt(level, pos)) {
            return pos;
         }
      }

      return null;
   }

   private BlockPos surface(ServerLevel level, int x, int z) {
      return level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
   }

   private boolean canSpawnAt(ServerLevel level, BlockPos pos) {
      if (pos.getY() > 64 && level.isLoaded(pos)) {
         BlockState below = level.getBlockState(pos.below());
         BlockState at = level.getBlockState(pos);
         BlockState above = level.getBlockState(pos.above());
         return below.isFaceSturdy(level, pos.below(), Direction.UP)
            && at.getCollisionShape(level, pos).isEmpty()
            && above.getCollisionShape(level, pos.above()).isEmpty()
            && level.getFluidState(pos).isEmpty()
            && level.getFluidState(pos.below()).isEmpty();
      } else {
         return false;
      }
   }

   private EntityType<?> resolveType(RandomSource random) {
      for (int attempt = 0; attempt < CANDIDATES.length; attempt++) {
         Identifier id = CANDIDATES[random.nextInt(CANDIDATES.length)];
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(id);
         if (type != null) {
            return type;
         }
      }

      for (Identifier id : CANDIDATES) {
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(id);
         if (type != null) {
            return type;
         }
      }

      return null;
   }

   private static Identifier[] ids(String namespace, String... paths) {
      Identifier[] ids = new Identifier[paths.length];

      for (int i = 0; i < paths.length; i++) {
         ids[i] = Identifier.fromNamespaceAndPath(namespace, paths[i]);
      }

      return ids;
   }

   private static boolean isFactionMobPath(String path) {
      return path.startsWith("marine_") || path.startsWith("pirate_") || path.startsWith("bandit_") || path.startsWith("revolutionary_");
   }

   private static boolean isNight(ServerLevel level) {
      long time = level.getOverworldClockTime() % 24000L;
      return time >= 13000L && time <= 23000L;
   }
}
