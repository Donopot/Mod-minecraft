package net.mcreator.minepiece.procedures;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.block.state.BlockState;
/** Finite, owner-scoped Supa coat and posture modifiers. External potions are never removed. */
public final class SupaBlades {
 public static final double BLADE_REACH=2.5,CUT_RADIUS=2.5;public static final int BLADE_TICKS=8;
 public enum Stance{NONE,OFFENSIVE,DEFENSIVE;public net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect(){return this==OFFENSIVE?MobEffects.STRENGTH:this==DEFENSIVE?MobEffects.RESISTANCE:null;}}
 private record Scope(ServerPlayer player,ServerLevel level,net.minecraft.world.item.Item fruit){}
 private static final Identifier MOD=Identifier.fromNamespaceAndPath("minepiece","supa_stance");
 private static final Map<UUID,Integer> LAMES=new HashMap<>();private static final Map<UUID,Stance> POSTURES=new HashMap<>();private static final Map<UUID,Scope> OWNERS=new HashMap<>();
 private static int posturesPosees,posturesRetirees,blocsDecoupes,blocsRefuses;
 private SupaBlades(){}
 public static int posturesPosees(){return posturesPosees;}public static int posturesRetirees(){return posturesRetirees;}public static int blocsDecoupes(){return blocsDecoupes;}public static int blocsRefuses(){return blocsRefuses;}
 public static void resetCompteurs(){for(var s:new ArrayList<>(OWNERS.values()))retract(s.player);posturesPosees=posturesRetirees=blocsDecoupes=blocsRefuses=0;}
 public static void activate(ServerPlayer p,int ticks){OWNERS.put(p.getUUID(),new Scope(p,(ServerLevel)p.level(),DevilFruitPowerLogic.getDevilFruit(p).getItem()));LAMES.put(p.getUUID(),Math.max(BLADE_TICKS,ticks));net.mcreator.minepiece.network.MaterialBodyState.send(p);}
 public static boolean bladesActive(ServerPlayer p){return p!=null && LAMES.getOrDefault(p.getUUID(),0)>0;}
 public static int bladesRemaining(ServerPlayer p){return LAMES.getOrDefault(p.getUUID(),0);}
 public static double reach(ServerPlayer p,double base){return bladesActive(p)?base+BLADE_REACH:base;}
 public static Stance stance(ServerPlayer p){return POSTURES.getOrDefault(p.getUUID(),Stance.NONE);}
 public static boolean setStance(ServerPlayer p,Stance next){var old=stance(p);if(old==next)return false;removeOwned(p);if(old!=Stance.NONE)posturesRetirees++;
  POSTURES.put(p.getUUID(),next);if(next!=Stance.NONE){var a=p.getAttribute(next==Stance.OFFENSIVE?Attributes.ATTACK_DAMAGE:Attributes.ARMOR);if(a!=null)a.addTransientModifier(new AttributeModifier(MOD,next==Stance.OFFENSIVE?3:5,AttributeModifier.Operation.ADD_VALUE));}posturesPosees++;return true;}
 public static int stanceEffectsOn(ServerPlayer p){int count=0;for(var a:List.of(Attributes.ATTACK_DAMAGE,Attributes.ARMOR)){var attr=p.getAttribute(a);if(attr!=null && attr.hasModifier(MOD))count++;}return count;}
 private static void removeOwned(ServerPlayer p){for(var a:List.of(Attributes.ATTACK_DAMAGE,Attributes.ARMOR)){var attr=p.getAttribute(a);if(attr!=null)attr.removeModifier(MOD);}}
 public static void retract(ServerPlayer p){boolean had=LAMES.remove(p.getUUID())!=null;setStance(p,Stance.NONE);POSTURES.remove(p.getUUID());OWNERS.remove(p.getUUID());if(had)net.mcreator.minepiece.network.MaterialBodyState.send(p);}
 public static void register(){net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(s->tickBlades());net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((h,s)->retract(h.player));net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s->{for(var o:new ArrayList<>(OWNERS.values()))retract(o.player);});}
 public static void tickBlades(){for(var o:new ArrayList<>(OWNERS.values())){int n=LAMES.getOrDefault(o.player.getUUID(),0);if(n<=1 || !BatchRules.usable(o.player,o.level,o.fruit))retract(o.player);else LAMES.put(o.player.getUUID(),n-1);}}
 public static boolean canCut(ServerLevel level,BlockPos p){if(!level.hasChunkAt(p) || !level.getWorldBorder().isWithinBounds(p) || DevilFruitExplosions.isProtectedPosition(level,p))return false;BlockState s=level.getBlockState(p);return !s.isAir() && s.getFluidState().isEmpty() && !s.hasBlockEntity() && level.getBlockEntity(p)==null && s.getDestroySpeed(level,p)>=0 && s.getBlock().getExplosionResistance()<=6 && !PowerBlockService.isTrackedAny(level,p);}
 /** Legacy diagnostic API. Gameplay techniques use a shared TechniqueTerrain budget. */
 public static int cut(ServerPlayer player,double radius){if(!(player.level() instanceof ServerLevel l))return 0;int r=(int)Math.ceil(Math.min(radius,CUT_RADIUS)),n=0;BlockPos c=player.blockPosition();
  for(var p:BlockPos.betweenClosed(c.offset(-r,-r,-r),c.offset(r,r,r))){if(l.getBlockState(p).isAir())continue;if(!canCut(l,p)){blocsRefuses++;continue;}if(p.distSqr(c)>radius*radius || n>=64)continue;var h=l.clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(),net.minecraft.world.phys.Vec3.atCenterOf(p),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,player));if(h.getType()!=net.minecraft.world.phys.HitResult.Type.MISS && !h.getBlockPos().equals(p))continue;if(l.destroyBlock(p,false))n++;}blocsDecoupes+=n;return n;}
}
