package net.mcreator.minepiece.procedures;
import java.util.*;
import br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.*;
import net.minecraft.server.level.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.MobEffectInstance;
/** Contract-defined Imu passive; exact owner and world, own marker and modifier only. */
public final class SovereignVeil {
 private static final Identifier MOD=Identifier.fromNamespaceAndPath("minepiece","sovereign_veil");
 private static final class State {final ServerPlayer owner;final ServerLevel world;int next;State(ServerPlayer p){owner=p;world=(ServerLevel)p.level();next=DevilFruitImuConfig.veilRefreshTicks();}}
 private static final Map<UUID,State> VEILED=new HashMap<>();
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->{for(var f:new ArrayList<>(VEILED.values()))if(!BatchRules.usable(f.owner,f.world,MinepieceModItems.ITEM_DEVIL_FRUIT_IMU)||!DevilFruitImuConfig.veilEnabled())remove(f.owner);for(var p:s.getPlayerList().getPlayers())tick(p);});ServerPlayConnectionEvents.DISCONNECT.register((h,s)->remove(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->removeAll());}
 public static boolean hasVeil(ServerPlayer p){var f=p==null?null:VEILED.get(p.getUUID());return f!=null && f.owner==p;}
 public static int refreshIn(ServerPlayer p){var f=p==null?null:VEILED.get(p.getUUID());return f==null || f.owner!=p?0:f.next;}
 public static int veiledCount(){return VEILED.size();}
 public static boolean holdsImuFruit(ServerPlayer p){return p!=null && DevilFruitPowerLogic.getDevilFruit(p).is(MinepieceModItems.ITEM_DEVIL_FRUIT_IMU);}
 public static boolean apply(ServerPlayer p){if(p==null||!DevilFruitImuConfig.veilEnabled()||!BatchRules.usable(p,(ServerLevel)p.level(),MinepieceModItems.ITEM_DEVIL_FRUIT_IMU))return false;var previous=VEILED.get(p.getUUID());if(previous!=null && previous.owner!=p)remove(previous.owner);p.addEffect(new MobEffectInstance(MinepieceModMobEffects.SOVEREIGN_VEIL,DevilFruitImuConfig.veilDurationTicks(),0,false,false,true));var a=p.getAttribute(Attributes.ARMOR);if(a!=null){a.removeModifier(MOD);a.addTransientModifier(new AttributeModifier(MOD,DevilFruitImuConfig.veilArmorBonus(),AttributeModifier.Operation.ADD_VALUE));}VEILED.put(p.getUUID(),new State(p));return true;}
 public static void remove(ServerPlayer p){if(p==null)return;var f=VEILED.get(p.getUUID());if(f!=null&&f.owner==p)VEILED.remove(p.getUUID(),f);p.removeEffect(MinepieceModMobEffects.SOVEREIGN_VEIL);var a=p.getAttribute(Attributes.ARMOR);if(a!=null)a.removeModifier(MOD);}
 public static void removeAll(){for(var f:new ArrayList<>(VEILED.values()))remove(f.owner);}
 public static void tick(ServerPlayer p){if(!DevilFruitImuConfig.veilEnabled()||!BatchRules.usable(p,(ServerLevel)p.level(),MinepieceModItems.ITEM_DEVIL_FRUIT_IMU)){remove(p);return;}var f=VEILED.get(p.getUUID());if(f==null||f.owner!=p||f.world!=p.level()||--f.next<=0)apply(p);}
}
