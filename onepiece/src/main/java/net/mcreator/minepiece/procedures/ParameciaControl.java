package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ParticleTypes;

/** B04 controls use owned modifiers, never remove a victim's vanilla potions or change its AI. */
public final class ParameciaControl {
 public enum Kind { NEGATIVE, STONE }
 private static final Identifier SLOW=Identifier.fromNamespaceAndPath("minepiece","b04_control_speed"),WEAK=Identifier.fromNamespaceAndPath("minepiece","b04_control_attack");
 private static final class State {
  final ServerPlayer owner;final LivingEntity target;final ServerLevel world;final Item fruit;final Kind kind;int ticks;net.minecraft.world.effect.MobEffectInstance marker;
  State(ServerPlayer p,LivingEntity t,Kind k,int ttl){owner=p;target=t;world=(ServerLevel)p.level();fruit=DevilFruitPowerLogic.getDevilFruit(p).getItem();kind=k;ticks=ttl;}
 }
 private static final Map<UUID,State> STATES=new HashMap<>();
 private ParameciaControl(){}
 public static boolean apply(ServerPlayer p,LivingEntity target,Kind kind){
  if(p==null || target==null || !(p.level() instanceof ServerLevel level) || !BatchRules.usable(p,level,DevilFruitPowerLogic.getDevilFruit(p).getItem()) || !BatchRules.target(p,target) || kind==Kind.STONE && Petrification.isUndead(target))return false;
  if(!DevilFruitPowerLogic.getDevilFruit(p).is(kind==Kind.STONE?net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO:net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO))return false;
  var existing=STATES.get(target.getUUID());if(existing!=null)return existing.target==target && existing.owner==p && existing.kind==kind;
  var state=new State(p,target,kind,kind==Kind.STONE?80:100);STATES.put(target.getUUID(),state);
  if(kind==Kind.STONE)((ParameciaStoneData)target).minepiece$stoneFlag(true);
  if(kind==Kind.STONE && !target.hasEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.B04_STONE)){var marker=new net.minecraft.world.effect.MobEffectInstance(net.mcreator.minepiece.init.MinepieceModMobEffects.B04_STONE,state.ticks,0,false,false,true);if(target.addEffect(marker))state.marker=target.getEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.B04_STONE);}
  var speed=target.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null)speed.addTransientModifier(new AttributeModifier(SLOW,kind==Kind.STONE?-1:-.65,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
  var attack=target.getAttribute(Attributes.ATTACK_DAMAGE);if(attack!=null && kind==Kind.NEGATIVE)attack.addTransientModifier(new AttributeModifier(WEAK,-.6,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
  return true;
 }
 public static boolean active(LivingEntity target,Kind kind){var s=STATES.get(target.getUUID());return s!=null && s.target==target && s.kind==kind;}
 public static int remaining(LivingEntity target){var s=STATES.get(target.getUUID());return s==null || s.target!=target?0:s.ticks;}
 private static void release(State s){STATES.remove(s.target.getUUID(),s);if(s.kind==Kind.STONE)((ParameciaStoneData)s.target).minepiece$stoneFlag(false);if(s.marker!=null && s.target.getEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.B04_STONE)==s.marker)s.target.removeEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.B04_STONE);var speed=s.target.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null)speed.removeModifier(SLOW);var attack=s.target.getAttribute(Attributes.ATTACK_DAMAGE);if(attack!=null)attack.removeModifier(WEAK);}
 public static void cancel(ServerPlayer p){for(var s:new ArrayList<>(STATES.values()))if(s.owner==p)release(s);}
 public static void clear(){for(var s:new ArrayList<>(STATES.values()))release(s);}
 public static void tick(){for(var s:new ArrayList<>(STATES.values())){
  if(!BatchRules.usable(s.owner,s.world,s.fruit) || !s.target.isAlive() || s.target.isRemoved() || s.target.level()!=s.world || --s.ticks<=0){release(s);continue;}
  if(s.kind==Kind.STONE)s.target.setDeltaMovement(0,s.target.getDeltaMovement().y,0);
  if(s.ticks%8==0)s.world.sendParticles(s.kind==Kind.STONE?ParticleTypes.END_ROD:ParticleTypes.SOUL,s.target.getX(),s.target.getY()+.8,s.target.getZ(),3,.2,.4,.2,.005);
 }}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->cancel(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clear());}
}
