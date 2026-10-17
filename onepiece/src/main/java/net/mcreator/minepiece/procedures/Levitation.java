package net.mcreator.minepiece.procedures;
import java.util.*;
import br.com.oneminepiece.opworldlike.gravity.DevilFruitGravityConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
/** Compatibility helper; B02 Moko uses horizontal gravity, not levitation. */
public final class Levitation {
 private static final class State {final ServerPlayer owner;final LivingEntity target;final ServerLevel world;int remaining,amplifier;MobEffectInstance marker;State(ServerPlayer o,LivingEntity t){owner=o;target=t;world=(ServerLevel)t.level();}}
 private static final Map<UUID,State> STATES=new HashMap<>();
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->releaseFrom(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->releaseAll());}
 public static boolean canLevitate(LivingEntity caster,LivingEntity target){return caster instanceof ServerPlayer p && target!=null && caster.level() instanceof ServerLevel l && BatchRules.usable(p,l,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI) && BatchRules.target(p,target) && !target.isInvulnerable();}
 public static int apply(LivingEntity caster,LivingEntity target){if(!canLevitate(caster,target))return 0;var p=(ServerPlayer)caster;var st=STATES.get(target.getUUID());if(st!=null && (st.owner!=p || st.target!=target))return 0;if(st==null){st=new State(p,target);STATES.put(target.getUUID(),st);}st.remaining=DevilFruitGravityConfig.levitationTicks();st.amplifier=Math.min(DevilFruitGravityConfig.levitationMaxAmplifier(),st.amplifier+1);
  if(target.getEffect(MobEffects.LEVITATION)==null || target.getEffect(MobEffects.LEVITATION)==st.marker){if(st.marker!=null)target.removeEffect(MobEffects.LEVITATION);target.addEffect(new MobEffectInstance(MobEffects.LEVITATION,st.remaining,st.amplifier,false,false));st.marker=target.getEffect(MobEffects.LEVITATION);}return st.amplifier;}
 public static boolean isLevitated(LivingEntity target){return target!=null && STATES.containsKey(target.getUUID());}
 public static int remaining(LivingEntity target){var s=target==null?null:STATES.get(target.getUUID());return s==null?0:s.remaining;}
 public static int trackedCount(){return STATES.size();}
 public static void release(LivingEntity target){if(target==null)return;var s=STATES.remove(target.getUUID());if(s!=null && s.target==target && s.marker!=null && target.getEffect(MobEffects.LEVITATION)==s.marker)target.removeEffect(MobEffects.LEVITATION);}
 public static void releaseFrom(ServerPlayer p){for(var s:new ArrayList<>(STATES.values()))if(s.owner==p)release(s.target);}
 public static void releaseAll(){for(var s:new ArrayList<>(STATES.values()))release(s.target);}
 public static void tick(){for(var s:new ArrayList<>(STATES.values()))if(--s.remaining<=0 || s.target.isRemoved() || !s.target.isAlive() || s.target.level()!=s.world || !BatchRules.usable(s.owner,s.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI))release(s.target);}
}
