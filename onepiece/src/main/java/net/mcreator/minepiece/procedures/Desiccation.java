package net.mcreator.minepiece.procedures;

import java.util.*;
import br.com.oneminepiece.opworldlike.sand.DevilFruitSandConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.mcreator.minepiece.init.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

/** Attributed drying with its own reversible speed modifier; external potions are untouched. */
public final class Desiccation {
   public static final int DAMAGE_PERIOD=20;
   private static final Identifier SLOW=Identifier.fromNamespaceAndPath("minepiece","desiccation_slow");
   private static final class State {
      final LivingEntity target;final ServerLevel world;final ServerPlayer owner;final net.minecraft.world.item.Item fruit;
      int level,remaining,damageCountdown=DAMAGE_PERIOD;
      State(LivingEntity t,ServerPlayer o){target=t;world=(ServerLevel)t.level();owner=o;fruit=o==null?null:DevilFruitPowerLogic.getDevilFruit(o).getItem();}
   }
   private static final Map<UUID,State> STATES=new HashMap<>();
   private Desiccation(){}
   public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());}
   public static int apply(LivingEntity t){return apply(t,null);}
   public static int apply(LivingEntity t,ServerPlayer owner){
      if(t==null || !t.isAlive() || !(t.level() instanceof ServerLevel world))return 0;
      if(owner!=null && !BatchRules.offensiveTarget(owner,t))return 0;
      if(DevilFruitSandConfig.wetResists() && isWet(t)){release(t);return 0;}
      var s=STATES.get(t.getUUID());
      if(s!=null && (s.target!=t || s.world!=world || s.owner!=owner)){release(s.target);s=null;}
      if(s==null){s=new State(t,owner);STATES.put(t.getUUID(),s);}
      s.level=Math.min(DevilFruitSandConfig.dessicationMaxLevel(),s.level+1);s.remaining=DevilFruitSandConfig.dessicationLevelTicks();
      var a=t.getAttribute(Attributes.MOVEMENT_SPEED);
      if(a!=null){a.removeModifier(SLOW);a.addTransientModifier(new AttributeModifier(SLOW,-Math.min(.9,.15*s.level*DevilFruitSandConfig.dessicationSlownessAmplifierPerLevel()),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
      world.sendParticles(MinepieceModParticles.SAND,t.getX(),t.getY()+1,t.getZ(),4+s.level*2,.35,.5,.35,.02);return s.level;
   }
   private static State state(LivingEntity t){var s=t==null?null:STATES.get(t.getUUID());return s!=null && s.target==t && s.world==t.level()?s:null;}
   public static int levelOf(LivingEntity t){var s=state(t);return s==null?0:s.level;}
   public static boolean isDesiccated(LivingEntity t){return levelOf(t)>0;}
   public static int remaining(LivingEntity t){var s=state(t);return s==null?0:s.remaining;}
   public static int trackedCount(){return STATES.size();}
   public static boolean isWet(LivingEntity t){return ElementalSenses.isWet(t);}
   public static void release(LivingEntity t){if(t==null)return;var s=STATES.get(t.getUUID());if(s!=null && s.target==t){STATES.remove(t.getUUID());var a=t.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(SLOW);}}
   public static void clearFrom(ServerPlayer p){for(var s:new ArrayList<>(STATES.values()))if(s.owner==p)release(s.target);}
   public static void clearAll(){for(var s:new ArrayList<>(STATES.values()))release(s.target);}
   public static void tick(){for(var s:new ArrayList<>(STATES.values())){
      var t=s.target;
      if(t.isRemoved() || !t.isAlive() || t.level()!=s.world || DevilFruitSandConfig.wetResists() && isWet(t)
         || s.owner!=null && (!BatchRules.usable(s.owner,s.world,s.fruit) || !BatchRules.offensiveTarget(s.owner,t))){release(t);continue;}
      if(--s.remaining<=0){release(t);continue;}
      if(s.level>=DevilFruitSandConfig.dessicationMaxLevel() && --s.damageCountdown<=0){
         s.damageCountdown=DAMAGE_PERIOD;double damage=DevilFruitSandConfig.dessicationDamageAtMaxPerSecond();
         if(damage>0)t.hurtServer(s.world,s.owner==null?s.world.damageSources().magic():CombatSkillLogic.elementalDamageSource(s.world,ElementType.PHYSICAL,s.owner,null),(float)damage);
      }
      if(s.remaining%5==0)s.world.sendParticles(MinepieceModParticles.SAND,t.getX(),t.getY()+.9,t.getZ(),2,.3,.4,.3,.01);
   }}
}
