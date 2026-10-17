package net.mcreator.minepiece.procedures;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.resources.Identifier;
/** Mod-designed SilentCommand. Finite own speed modifier, never a foreign potion. */
public final class ImuCommand {
 private static final Identifier SLOW=Identifier.fromNamespaceAndPath("minepiece","imu_command_slow");
 private static final class State {final ServerPlayer owner;final LivingEntity target;final ServerLevel world;final net.minecraft.world.item.Item fruit;int left;State(ServerPlayer p,LivingEntity t,int ticks){owner=p;target=t;world=(ServerLevel)p.level();fruit=DevilFruitPowerLogic.getDevilFruit(p).getItem();left=ticks;}}
 private static final Map<UUID,State> STATES=new HashMap<>();
 public static boolean active(LivingEntity t){var s=STATES.get(t.getUUID());return s!=null&&s.target==t;}
 public static void apply(ServerPlayer p,LivingEntity t){apply(p,t,60);}
 public static void apply(ServerPlayer p,LivingEntity t,int ticks){if(active(t)||!BatchRules.target(p,t)||t.isInvulnerable())return;var a=t.getAttribute(Attributes.MOVEMENT_SPEED);if(a==null)return;var s=new State(p,t,Math.min(60,Math.max(1,ticks)));STATES.put(t.getUUID(),s);a.addTransientModifier(new AttributeModifier(SLOW,-.8,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
 private static void release(State s){STATES.remove(s.target.getUUID(),s);var a=s.target.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(SLOW);}
 public static void cancel(ServerPlayer p){for(var s:new ArrayList<>(STATES.values()))if(s.owner==p)release(s);}
 public static void clear(){for(var s:new ArrayList<>(STATES.values()))release(s);}
 public static void tick(){for(var s:new ArrayList<>(STATES.values()))if(!BatchRules.usable(s.owner,s.world,s.fruit)||!s.target.isAlive()||s.target.isRemoved()||s.target.level()!=s.world||--s.left<=0)release(s);}
}
