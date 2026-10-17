package net.mcreator.minepiece.procedures;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig;

/** Permanent magma floors with finite, dimension-owned burn fields. Native hot-floor damage is replaced only during active attribution. */
public final class MagmaPools {
   private record Pool(ServerLevel level,ServerPlayer owner,boolean bound,List<BlockPos> positions) { }
   private static final List<Pool> ACTIVE=new ArrayList<>();
   private MagmaPools() { }
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->tick());
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->clearFrom(handler.player));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->ACTIVE.clear());
   }
   public static int create(ServerLevel level,BlockPos center,ServerPlayer owner,double radius) {
      if(!DevilFruitMagmaConfig.poolsEnabled() || DevilFruitMagmaConfig.maxPoolsPerPlayer()<=0 || owner==null || owner.level()!=level) return 0;
      enforcePopulation(level,owner);
      List<BlockPos> placed=new ArrayList<>();
      for(BlockPos pos:MagmaBurn.poolPositions(center,Math.clamp(radius,0,8))) {
         BlockPos surface=surfaceAt(level,pos);if(surface==null)continue;
         var state=level.getBlockState(surface);
         // Never replace inventories, fluids, another active power attribution or indestructible blocks.
         if(PowerBlockService.isTrackedAny(level,surface) || state.isAir() || state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK) || state.hasBlockEntity()
               || !state.getFluidState().isEmpty() || state.getDestroySpeed(level,surface)<0
               || state.getBlock().getExplosionResistance()>DevilFruitMagmaConfig.poolMaxResistance())continue;
         int before=PowerBlockService.activeCountFor(owner.getUUID());
         PowerBlockService.place(level,surface,MagmaBurn.magmaBlock(),DevilFruitMagmaConfig.poolLifetimeTicks(),owner.getUUID());
         if(PowerBlockService.activeCountFor(owner.getUUID())>before)placed.add(surface);
      }
      if(!placed.isEmpty())ACTIVE.add(new Pool(level,owner,MaguTechniques.usable(owner),placed));
      return placed.size();
   }
   public static int poolCountFor(UUID owner) {return (int)ACTIVE.stream().filter(pool->pool.owner.getUUID().equals(owner)).count();}
   public static int activePools() {return ACTIVE.size();}
   public static boolean enforcePopulation(ServerLevel ignored,ServerPlayer owner) {
      boolean closed=false;int max=DevilFruitMagmaConfig.maxPoolsPerPlayer();
      while(poolCountFor(owner.getUUID())>0 && poolCountFor(owner.getUUID())>=Math.max(1,max)) {closeOldest(owner.getUUID());closed=true;}
      return closed;
   }
   private static void releaseField(Pool pool) {
      for(BlockPos pos:pool.positions)PowerBlockService.releaseNow(pool.level,pos,pool.owner.getUUID());
      ACTIVE.remove(pool);
   }
   private static void closeOldest(UUID owner) {
      for(Pool pool:new ArrayList<>(ACTIVE))if(pool.owner.getUUID().equals(owner)){releaseField(pool);return;}
   }
   public static void clearFrom(ServerPlayer owner) {
      for(Pool pool:new ArrayList<>(ACTIVE))if(pool.owner.getUUID().equals(owner.getUUID()))releaseField(pool);
   }
   public static void clearAll() {for(Pool pool:new ArrayList<>(ACTIVE))releaseField(pool);}
   public static void tick() {
      for(Pool pool:new ArrayList<>(ACTIVE)) {
         // Keep diagnostic non-Magu fixtures usable; a real Magu owner loses all maintained pools on suppression/removal.
         if(pool.bound && !MaguTechniques.usable(pool.owner) || !pool.owner.isAlive() || pool.owner.isRemoved() || pool.owner.level()!=pool.level || DevilFruitPowerLogic.isSuppressed(pool.owner)) {releaseField(pool);continue;}
         pool.positions.removeIf(pos->!PowerBlockService.isTracked(pool.level,pos,MagmaBurn.magmaBlock(),pool.owner.getUUID()));
         if(pool.positions.isEmpty()){ACTIVE.remove(pool);continue;}
         for(BlockPos pos:pool.positions) {
            for(LivingEntity target:pool.level.getEntitiesOfClass(LivingEntity.class,new AABB(pos).move(0,1,0).inflate(0,.15,0))) {
               if(!CombatSkillLogic.isValidTarget(pool.owner,target) || !pool.owner.hasLineOfSight(target)
                     || DevilFruitExplosions.isProtectedPosition(pool.level,target.blockPosition()))continue;
               var source=CombatSkillLogic.elementalDamageSource(pool.level,ElementType.MAGMA,pool.owner,null);
               if(!MagmaBurn.isBurning(target) && CombatSkillLogic.hurtAndConfirm(target,pool.level,source,2))
                  MagmaBurn.ignite(target,DevilFruitMagmaConfig.magmaBurnTicks(),pool.owner);
            }
         }
      }
   }
   private static BlockPos surfaceAt(ServerLevel level,BlockPos from) {
      for(int dy=0;dy<=3;dy++) {
         BlockPos pos=from.below(dy);if(!level.isLoaded(pos))return null;
         var state=level.getBlockState(pos);
         if(!state.isAir())return state.getFluidState().isEmpty()?pos:null;
      }
      return null;
   }
}
