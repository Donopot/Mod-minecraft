package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

/** Progressive ice owns a modifier and its marker, never the target's vanilla Slowness potion. */
public final class FreezeService {
   public static final int MAX_STAGE=3, STAGE_TICKS=60, HARD_CAP_TICKS=100, FREEDOM_TICKS=100;
   private static final Identifier SLOW=Identifier.fromNamespaceAndPath("minepiece","freeze_slow");
   private static final class State {
      final LivingEntity target;final ServerLevel level;ServerPlayer owner;net.minecraft.world.item.Item fruit;
      int stage,remaining,served,freedom,markerBudget,markerStage;MobEffectInstance marker;
      State(LivingEntity t,ServerPlayer o){target=t;level=(ServerLevel)t.level();owner=o;fruit=o==null?null:DevilFruitPowerLogic.getDevilFruit(o).getItem();}
   }
   private static final Map<UUID,State> STATES=new HashMap<>();
   private FreezeService(){}
   public static void register(){
      ServerTickEvents.END_SERVER_TICK.register(s->tick());
      ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());
   }
   public static int apply(LivingEntity t,int ticks){return apply(t,ticks,null);}
   public static int apply(LivingEntity t,int ticks,ServerPlayer owner){
      if(t==null || !t.isAlive() || !(t.level() instanceof ServerLevel level))return 0;
      if(owner!=null && !BatchRules.offensiveTarget(owner,t))return 0;
      State s=STATES.get(t.getUUID());
      if(s!=null && (s.target!=t || s.level!=level)){release(s.target);s=null;}
      if(s==null){s=new State(t,owner);STATES.put(t.getUUID(),s);}
      if(owner!=null){s.owner=owner;s.fruit=DevilFruitPowerLogic.getDevilFruit(owner).getItem();}
      if(s.freedom>0 || s.served>=HARD_CAP_TICKS)return 0;
      s.stage=Math.min(MAX_STAGE,s.stage+1);s.remaining=Math.min(Math.max(1,ticks),HARD_CAP_TICKS-s.served);
      var a=t.getAttribute(Attributes.MOVEMENT_SPEED);
      if(a!=null){a.removeModifier(SLOW);a.addTransientModifier(new AttributeModifier(SLOW,s.stage>=MAX_STAGE?-1:-.25*s.stage,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
      var existing=t.getEffect(MinepieceModMobEffects.FROZEN);
      boolean own=existing!=null && existing==s.marker && existing.getAmplifier()==s.markerStage && existing.getDuration()<=s.markerBudget;
      if(existing==null || own){if(own)t.removeEffect(MinepieceModMobEffects.FROZEN);t.addEffect(new MobEffectInstance(MinepieceModMobEffects.FROZEN,s.remaining,s.stage,false,false));s.marker=t.getEffect(MinepieceModMobEffects.FROZEN);s.markerBudget=s.remaining;s.markerStage=s.stage;}
      level.sendParticles(MinepieceModParticles.ICE,t.getX(),t.getY()+1,t.getZ(),10+s.stage*8,.4,.6,.4,.02);
      return s.stage;
   }
   private static State state(LivingEntity t){var s=t==null?null:STATES.get(t.getUUID());return s!=null && s.target==t && t.level()==s.level?s:null;}
   public static int stageOf(LivingEntity t){var s=state(t);return s==null?0:s.stage;}
   public static boolean isFrozen(LivingEntity t){return stageOf(t)>=MAX_STAGE;}
   public static int freedomTicksLeft(LivingEntity t){var s=state(t);return s==null?0:s.freedom;}
   public static int trackedCount(){return STATES.size();}
   private static void effects(State s){var t=s.target;var a=t.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(SLOW);var marker=t.getEffect(MinepieceModMobEffects.FROZEN);if(marker!=null && marker==s.marker && marker.getAmplifier()==s.markerStage && marker.getDuration()<=s.markerBudget)t.removeEffect(MinepieceModMobEffects.FROZEN);s.marker=null;}
   public static void release(LivingEntity t){if(t==null)return;var s=STATES.get(t.getUUID());if(s!=null && s.target==t){STATES.remove(t.getUUID());effects(s);}}
   public static void clearFrom(ServerPlayer p){for(var s:new ArrayList<>(STATES.values()))if(s.owner==p)release(s.target);}
   public static void clearAll(){for(var s:new ArrayList<>(STATES.values()))effects(s);STATES.clear();}
   public static void tick(){
      for(var s:new ArrayList<>(STATES.values())){
         var t=s.target;
         boolean valid=t.isAlive() && !t.isRemoved() && t.level()==s.level;
         if(s.owner!=null)valid &= s.owner.isAlive() && !s.owner.isRemoved() && s.owner.level()==s.level && !DevilFruitPowerLogic.isSuppressed(s.owner)
            && DevilFruitPowerLogic.getDevilFruit(s.owner).is(s.fruit);
         if(!valid){release(t);continue;}
      if(s.freedom>0){if(--s.freedom==0)release(t);continue;}
         --s.remaining;++s.served;
         if(s.remaining<=0){effects(s);if(s.served>=HARD_CAP_TICKS){s.stage=0;s.freedom=FREEDOM_TICKS;}else STATES.remove(t.getUUID());continue;}
         if(s.stage>=MAX_STAGE)t.setDeltaMovement(0,t.getDeltaMovement().y,0);
      }
   }
}
