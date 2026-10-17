package net.mcreator.minepiece.procedures;
import java.util.*;
import br.com.oneminepiece.opworldlike.gravity.DevilFruitGravityConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
/** Owned gravity velocity, not accumulating acceleration. */
public final class GravityField {
 private record Field(ServerPlayer owner,ServerLevel world,Vec3 centre,double radius,double intensity,int[] remaining){}
 private static final List<Field> FIELDS=new ArrayList<>();
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->clearFrom(h.player));ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());}
 public static void create(ServerLevel level,Vec3 centre,ServerPlayer owner,double radius,double intensity,int lifetime,boolean upward){
  if(!BatchRules.usable(owner,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI))return;
  clearFrom(owner);if(FIELDS.size()>=8)return;FIELDS.add(new Field(owner,level,centre,Math.min(24,Math.max(.5,radius)),cappedIntensity(upward?Math.abs(intensity):-Math.abs(intensity)),new int[]{Math.min(200,Math.max(1,lifetime))}));
 }
 public static double cappedIntensity(double requested){double cap=Math.min(1,Math.max(0,DevilFruitGravityConfig.maxVelocity()));return Math.max(-cap,Math.min(cap,requested));}
 public static double validatedRise(ServerLevel level,LivingEntity target,double delta){return delta!=0 && SafeDestination.isSafe(level,target,target.position().add(0,delta,0))?delta:0;}
 public static int activeFields(){return FIELDS.size();}
 public static void clearFrom(ServerPlayer owner){FIELDS.removeIf(f->f.owner==owner);}
 public static void clearAll(){FIELDS.clear();}
 public static void tick(){for(var f:new ArrayList<>(FIELDS)){
  if(--f.remaining[0]<=0 || !BatchRules.usable(f.owner,f.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI)){FIELDS.remove(f);continue;}
  for(var t:f.world.getEntitiesOfClass(LivingEntity.class,new AABB(f.centre,f.centre).inflate(f.radius))){
   if(t.position().distanceTo(f.centre)>f.radius || !BatchRules.target(f.owner,t) || t.isInvulnerable())continue;
   double dy=validatedRise(f.world,t,f.intensity);if(dy==0)continue;t.setDeltaMovement(t.getDeltaMovement().x,dy,t.getDeltaMovement().z);t.syncVelocity=true;t.fallDistance=0;
  }
  if(f.remaining[0]%5==0)ControlledBatchTechniques.visual(f.owner,"b02_gravity_field",f.centre,6).setStretch((float)f.radius);
 }}
}
