package net.mcreator.minepiece.procedures;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
/** Kamie ver15 applies STUN30 amplifier1: zero mobility/reach and reduced attack, not health damage. */
public final class ZoanCounterState {
 private static final Identifier MOD=Identifier.fromNamespaceAndPath("minepiece","b06_counter_stun");
 private static final class State {final ServerPlayer owner;final LivingEntity target;final ServerLevel world;int left=30;State(ServerPlayer p,LivingEntity t){owner=p;target=t;world=(ServerLevel)p.level();}}
 private static final Map<UUID,State> STATES=new HashMap<>();
 public static boolean active(LivingEntity t){var s=STATES.get(t.getUUID());return s!=null&&s.target==t&&s.left>0;}
 public static void apply(ServerPlayer p,LivingEntity target){if(active(target)||!BatchRules.target(p,target)||target.isInvulnerable())return;var s=new State(p,target);STATES.put(target.getUUID(),s);for(var type:List.of(Attributes.MOVEMENT_SPEED,Attributes.ATTACK_DAMAGE,Attributes.ARMOR,Attributes.ARMOR_TOUGHNESS,Attributes.BLOCK_INTERACTION_RANGE)){var a=target.getAttribute(type);if(a!=null)a.addTransientModifier(new AttributeModifier(MOD,type==Attributes.MOVEMENT_SPEED||type==Attributes.BLOCK_INTERACTION_RANGE?-1:-.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}if(target instanceof ServerPlayer victim)victim.sendSystemMessage(net.minecraft.network.chat.Component.literal("Étourdi : 1,5s"),true);}
 private static void end(State s){STATES.remove(s.target.getUUID(),s);for(var type:List.of(Attributes.MOVEMENT_SPEED,Attributes.ATTACK_DAMAGE,Attributes.ARMOR,Attributes.ARMOR_TOUGHNESS,Attributes.BLOCK_INTERACTION_RANGE)){var a=s.target.getAttribute(type);if(a!=null)a.removeModifier(MOD);}}
 public static void tick(){for(var s:new ArrayList<>(STATES.values()))if(!BatchRules.usable(s.owner,s.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO)||!s.target.isAlive()||s.target.isRemoved()||s.target.level()!=s.world||--s.left<=0)end(s);}
 public static void cancel(ServerPlayer p){for(var s:new ArrayList<>(STATES.values()))if(s.owner==p)end(s);}
 public static void clear(){for(var s:new ArrayList<>(STATES.values()))end(s);}
}
