package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;


public final class MagmaBurn {

   
   public static final int FIRE_TICKS = 120;
   public static final int MAGMA_FIRE_TICKS=120;
   public static final int REIGNITE_TICKS=2;

   
   public static final int DAMAGE_PERIOD = 20;

   private static final class Burn {
      final LivingEntity target;
      final net.minecraft.world.level.Level level;
      int remaining;
      int served;          
      int damageCountdown;
      int quiet;
      LivingEntity owner;
      boolean ownsFire;

      Burn(int remaining,LivingEntity target) {
         this.target=target;this.level=target.level();
         this.remaining = remaining;
         this.damageCountdown = DAMAGE_PERIOD;
      }
   }

   private static final Map<UUID, Burn> BURNS = new HashMap<>();

   private MagmaBurn() {
   }

   
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
   }

   
   public static int ignite(LivingEntity target, int ticks) {
      return ignite(target,ticks,null);
   }

   public static int ignite(LivingEntity target, int ticks, LivingEntity owner) {
      if (target == null || owner!=null && !CombatSkillLogic.isValidTarget(owner,target)) return 0;
      
      if (immune(target)) {
         return 0;
      }
      Burn burn = BURNS.get(target.getUUID());
      if(burn!=null && burn.target!=target) {BURNS.remove(target.getUUID());burn=null;}
      if (burn == null) {
         burn = new Burn(0,target);
         burn.ownsFire = target.getRemainingFireTicks() <= 2;
         BURNS.put(target.getUUID(), burn);
      }
      burn.quiet = 0;
      burn.owner = owner;
      int cap = DevilFruitMagmaConfig.magmaBurnTicks()*2;
      int budget = cap - burn.served;
      if (budget <= 0) {
         return 0; 
      }
      int wanted = ticks > 0 ? ticks : DevilFruitMagmaConfig.magmaBurnTicks();
      burn.remaining = Math.min(Math.max(burn.remaining, wanted), budget);
      
      if(target.getRemainingFireTicks()<=2)burn.ownsFire=true;
      if (burn.ownsFire) target.setRemainingFireTicks(2);
      return burn.remaining;
   }

   
   public static int remaining(LivingEntity target) {
      Burn burn = target == null ? null : BURNS.get(target.getUUID());
      return burn == null ? 0 : burn.remaining;
   }

   
   public static boolean isBurning(LivingEntity target) {
      return remaining(target) > 0;
   }

   
   public static void extinguish(LivingEntity target) {
      if (target == null) {
         return;
      }
      BURNS.remove(target.getUUID());
      target.setRemainingFireTicks(0);
   }

   
   public static int burningCount() {
      return (int)BURNS.values().stream().filter(burn -> burn.remaining > 0).count();
   }

   
   public static void clearAll() {
      for (UUID id : BURNS.keySet()) {
         LivingEntity target=find(id);
         if (target!=null && BURNS.get(id).ownsFire) target.setRemainingFireTicks(0);
      }
      BURNS.clear();
   }

   
   public static void tick() {
      if (BURNS.isEmpty()) {
         return;
      }
      for (Map.Entry<UUID, Burn> entry : new ArrayList<>(BURNS.entrySet())) {
         Burn burn = entry.getValue();
         LivingEntity target = find(entry.getKey());
         if (target == null || target!=burn.target || target.level()!=burn.level || !target.isAlive()) {
            BURNS.remove(entry.getKey());
            continue;
         }
         if (immune(target) || burn.owner!=null && (burn.owner.level()!=target.level() || !CombatSkillLogic.isValidTarget(burn.owner,target))) {
            extinguish(target); continue;
         }
         if (burn.remaining<=0) {
            if (burn.ownsFire) target.setRemainingFireTicks(0);
            if (++burn.quiet > DevilFruitMagmaConfig.magmaBurnTicks()) BURNS.remove(entry.getKey());
            continue;
         }
         if(target.getRemainingFireTicks()<=2)burn.ownsFire=true;
         burn.remaining--;
         burn.served++;
         if (burn.ownsFire) target.setRemainingFireTicks(burn.remaining>0 ? 2 : 0);
         burn.damageCountdown--;
         if (burn.damageCountdown <= 0) {
            burn.damageCountdown = DAMAGE_PERIOD;
            double damage = DevilFruitMagmaConfig.magmaBurnDamagePerSecond();
            if (damage > 0.0 && target.level() instanceof ServerLevel level) {
               
               if (burn.owner == null || CombatSkillLogic.isValidTarget(burn.owner, target))
                  target.hurtServer(level, CombatSkillLogic.elementalDamageSource(level,ElementType.MAGMA,burn.owner,null), (float) damage);
            }
         }
      }
   }

   private static boolean immune(LivingEntity target) {
      return target.fireImmune() || target.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE)
         || DevilFruitPowerLogic.getDevilFruit(target).is(net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU)
            && !DevilFruitPowerLogic.isSuppressed(target);
   }
   public static net.minecraft.world.level.block.state.BlockState magmaBlock() {return net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState();}
   public static java.util.List<net.minecraft.core.BlockPos> poolPositions(net.minecraft.core.BlockPos center,double radius) {
      var positions=new java.util.ArrayList<net.minecraft.core.BlockPos>();int r=(int)Math.ceil(radius);
      for(var pos:net.minecraft.core.BlockPos.betweenClosed(center.offset(-r,0,-r),center.offset(r,0,r)))
         if(pos.distSqr(center)<=radius*radius)positions.add(pos.immutable());
      return positions;
   }
   
   public static boolean isWet(LivingEntity target) {
      return ElementalSenses.isWet(target);
   }

   private static LivingEntity find(UUID id) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return null;
      }
      for (ServerLevel level : server.getAllLevels()) {
         if (level.getEntity(id) instanceof LivingEntity living) {
            return living;
         }
      }
      return null;
   }
}
