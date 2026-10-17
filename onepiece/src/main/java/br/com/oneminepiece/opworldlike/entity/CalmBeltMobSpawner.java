package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

public final class CalmBeltMobSpawner {
   private static final int COOLDOWN_TICKS = 700;
   private static final Identifier[] CANDIDATES = new Identifier[]{
      Identifier.fromNamespaceAndPath("minepiece", "sea_king"),
      Identifier.fromNamespaceAndPath("minepiece", "fighting_fish"),
      Identifier.fromNamespaceAndPath("minepiece", "flying_fish"),
      Identifier.fromNamespaceAndPath("minepiece", "sea_cow")
   };
   private final Map<UUID, Integer> nextAttemptTick = new HashMap<>();

   public void tick(ServerPlayer player) {
      if (player.level().dimension() == Level.OVERWORLD
         && OnePieceGeography.isCalmBelt(player.getBlockX(), player.getBlockZ())
         && !OnePieceGeography.isReverseMountainChannel(player.getBlockX(), player.getBlockZ())) {
         int tick = player.tickCount;
         int next = this.nextAttemptTick.getOrDefault(player.getUUID(), 0);
         if (tick >= next) {
            this.nextAttemptTick.put(player.getUUID(), tick + 700 + player.getRandom().nextInt(500));
            if (!(player.getRandom().nextFloat() > 0.45F)) {
               this.spawnNear(player);
            }
         }
      }
   }

   private void spawnNear(ServerPlayer player) {
      ServerLevel level = (ServerLevel) player.level();
      EntityType<?> type = this.resolveType();
      if (type != null) {
         RandomSource random = player.getRandom();

         for (int attempt = 0; attempt < 12; attempt++) {
            int x = player.getBlockX() + random.nextInt(65) - 32;
            int z = player.getBlockZ() + random.nextInt(65) - 32;
            int y = 62 - random.nextInt(6);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
               Entity entity = type.spawn(level, pos, EntitySpawnReason.NATURAL);
               if (entity == null) {
                  return;
               }

               entity.setPos(x + 0.5, y, z + 0.5);


               entity.setYRot(random.nextFloat() * 360.0F);


               entity.setXRot(0.0F);
               return;
            }
         }
      }
   }

   private EntityType<?> resolveType() {
      for (Identifier id : CANDIDATES) {
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(id);
         if (type != null) {
            return type;
         }
      }

      return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("drowned"));
   }
}
