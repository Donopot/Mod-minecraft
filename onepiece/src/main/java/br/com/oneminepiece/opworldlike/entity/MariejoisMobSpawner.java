package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import br.com.oneminepiece.opworldlike.worldgen.RegionType;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
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

public final class MariejoisMobSpawner {
   private static final Identifier VILLAGER = Identifier.fromNamespaceAndPath("minecraft", "villager");
   private static final Identifier CELESTIAL_DRAGON = Identifier.fromNamespaceAndPath("minepiece", "celestial_dragon");
   private final Map<UUID, Integer> nextAttemptTick = new HashMap<>();

   public void tick(ServerPlayer player) {
      if (MobBalanceConfig.enabled()
         && MobBalanceConfig.mariejoisSpawnsEnabled()
         && player.level().dimension() == Level.OVERWORLD
         && !player.isCreative()
         && !player.isSpectator()
         && UniqueIslandCatalog.isMariejoisTerritory(player.getBlockX(), player.getBlockZ())) {
         int tick = player.tickCount;
         int next = this.nextAttemptTick.getOrDefault(player.getUUID(), 0);
         if (tick >= next) {
            boolean redLineBoosted = this.isRedLineMariejois(player.getBlockX(), player.getBlockZ());
            int cooldown = MobBalanceConfig.mariejoisCooldownTicks(redLineBoosted);
            this.nextAttemptTick.put(player.getUUID(), tick + cooldown + player.getRandom().nextInt(Math.max(20, cooldown / 2)));
            if (!(player.getRandom().nextDouble() > MobBalanceConfig.mariejoisAttemptChance(redLineBoosted))) {
               this.spawnOne(player, redLineBoosted);
            }
         }
      }
   }

   public void forget(UUID playerId) {
      this.nextAttemptTick.remove(playerId);
   }

   private void spawnOne(ServerPlayer player, boolean redLineBoosted) {
      ServerLevel level = (ServerLevel) player.level();
      boolean canSpawnVillager = this.nearby(level, player, VILLAGER) < MobBalanceConfig.mariejoisVillagerMaxNearby(redLineBoosted);
      boolean canSpawnCelestial = this.nearby(level, player, CELESTIAL_DRAGON) < MobBalanceConfig.mariejoisCelestialDragonMaxNearby(redLineBoosted);
      Identifier id = this.pick(player.getRandom(), canSpawnVillager, canSpawnCelestial, redLineBoosted);
      if (id != null) {
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(id);
         if (type != null) {
            BlockPos anchor = this.findSpawnAnchor(player, player.getRandom(), redLineBoosted);
            if (anchor != null) {
               Entity entity = type.spawn(level, anchor, EntitySpawnReason.NATURAL);
               if (entity != null) {
                  entity.setPos(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);

                  entity.setYRot(player.getRandom().nextFloat() * 360.0F);

                  entity.setXRot(0.0F);
               }
            }
         }
      }
   }

   private int nearby(ServerLevel level, ServerPlayer player, Identifier id) {
      AABB area = AABB.ofSize(player.position(), 192.0, 96.0, 192.0);
      return level.getEntitiesOfClass(Entity.class, area, entity -> id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))).size();
   }

   private Identifier pick(RandomSource random, boolean canSpawnVillager, boolean canSpawnCelestial, boolean redLineBoosted) {
      int villagerWeight = canSpawnVillager ? MobBalanceConfig.mariejoisVillagerWeight(redLineBoosted) : 0;
      int celestialWeight = canSpawnCelestial ? MobBalanceConfig.mariejoisCelestialDragonWeight(redLineBoosted) : 0;
      int total = villagerWeight + celestialWeight;
      if (total <= 0) {
         return null;
      } else {
         return random.nextInt(total) < celestialWeight ? CELESTIAL_DRAGON : VILLAGER;
      }
   }

   private BlockPos findSpawnAnchor(ServerPlayer player, RandomSource random, boolean redLineBoosted) {
      int minRadius = MobBalanceConfig.mariejoisMinRadius();
      int maxRadius = Math.max(minRadius, MobBalanceConfig.mariejoisMaxRadius());
      int radiusRange = maxRadius - minRadius + 1;
      ServerLevel level = (ServerLevel) player.level();

      for (int attempt = 0; attempt < 24; attempt++) {
         double angle = random.nextDouble() * Math.PI * 2.0;
         int radius = minRadius + random.nextInt(radiusRange);
         int x = player.getBlockX() + (int)Math.round(Math.cos(angle) * radius);
         int z = player.getBlockZ() + (int)Math.round(Math.sin(angle) * radius);
         if (UniqueIslandCatalog.isMariejoisTerritory(x, z) && (!redLineBoosted || OnePieceGeography.regionAt(x, z) == RegionType.RED_LINE)) {
            BlockPos pos = this.surface(level, x, z);
            if (this.canSpawnAt(level, pos)) {
               return pos;
            }
         }
      }

      return null;
   }

   private boolean isRedLineMariejois(int x, int z) {
      return UniqueIslandCatalog.isMariejoisTerritory(x, z) && OnePieceGeography.regionAt(x, z) == RegionType.RED_LINE;
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
}
