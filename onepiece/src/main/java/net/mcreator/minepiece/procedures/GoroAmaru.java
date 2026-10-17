package net.mcreator.minepiece.procedures;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.AABB;

/** Amaru visual reference: Mine Mine no Mi 0.11.6. Bounded port budgets, not its combat stats. */
public final class GoroAmaru {
   public static final String FRUIT="minepiece:item_devil_fruit_goro_goro";
   public static final Identifier SCALE=Identifier.fromNamespaceAndPath("minepiece","amaru_scale");
   public static final int CHARGE_TICKS=20, MAX_TICKS=1200;
   public static final double START_COST=40, MAINTENANCE=.4, FLIGHT_COST=.6;
   private static final class Form {
      final ServerPlayer owner; final ServerLevel level;
      int charge,remaining=MAX_TICKS; boolean active,flightOwned;
      Form(ServerPlayer p){owner=p;level=(ServerLevel)p.level();}
   }
   private static final Map<UUID,Form> FORMS=new HashMap<>();
   private GoroAmaru(){}
   public static boolean has(ServerPlayer p){return FORMS.containsKey(p.getUUID());}
   public static boolean active(ServerPlayer p){var f=FORMS.get(p.getUUID());return f!=null && f.owner==p && f.active;}
   public static int remaining(ServerPlayer p){var f=FORMS.get(p.getUUID());return f==null?0:f.remaining;}
   public static int count(){return FORMS.size();}
   /** Attribute marker is synchronized by vanilla; dimension mixin works on server and both clients. */
   public static boolean scaled(LivingEntity p){var a=p.getAttribute(Attributes.SCALE);return a!=null && a.hasModifier(SCALE);}
   public static EntityDimensions dimensions(Pose pose){
      boolean crouch=pose==Pose.CROUCHING;
      return EntityDimensions.scalable(.96F,crouch?2.48F:2.6F).withEyeHeight(crouch?2.08F:2.2F);
   }
   public static AABB volume(ServerPlayer p){return new AABB(p.getX()-1.2,p.getY(),p.getZ()-1.2,p.getX()+1.2,p.getY()+6.5,p.getZ()+1.2);}
   public static boolean hasRoom(ServerPlayer p){
      var level=(ServerLevel)p.level();var box=volume(p);
      return safeVolume(level,box) && level.noCollision(p,box.deflate(.001));
   }
   public static boolean safeVolume(ServerLevel level,AABB box){
      if(box.minY<level.getMinY() || box.maxY>level.getMaxY())return false;
      if(!level.getWorldBorder().isWithinBounds(box))return false;
      for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))
         if(!level.isLoaded(pos) || !level.getFluidState(pos).isEmpty() || DevilFruitExplosions.isProtectedPosition(level,pos))return false;
      return true;
   }
   public static boolean toggle(ServerPlayer p){
      if(has(p)){end(p);return true;}
      if(!GoroTechniques.usable(p) || p.isPassenger() || p.isSleeping())return false;
      var v=MinepieceModVariables.get(p);
      if(v.PlayerEnergy<START_COST){message(p,"Énergie insuffisante pour Amaru (40).");return false;}
      if(Math.abs(p.getScale()-1)>0.001){message(p,"Revenez à votre taille normale avant Amaru.");return false;}
      if(!hasRoom(p)){message(p,"Pas assez de place pour Amaru : 2,4 × 6,5 blocs libres.");return false;}
      GoroTechniques.cancel(p);
      FORMS.put(p.getUUID(),new Form(p));
      TechniqueAnimations.playNamed(p,"goro_amaru_transform");
      message(p,"Charge d’Amaru… V pour annuler.");return true;
   }
   public static void tick(ServerPlayer p){
      var f=FORMS.get(p.getUUID());if(f==null)return;
      if(f.owner!=p || f.owner.isRemoved() || f.owner.level()!=f.level || !GoroTechniques.usable(p)
            || p.isPassenger() || p.isSleeping()) {end(f.owner);return;}
      var v=MinepieceModVariables.get(p);
      if(!f.active){
         if(++f.charge<CHARGE_TICKS)return;
         if(v.PlayerEnergy<START_COST || Math.abs(p.getScale()-1)>.001 || !hasRoom(p)){end(p);message(p,"Transformation annulée : espace ou énergie insuffisants.");return;}
         v.PlayerEnergy-=START_COST;f.active=true;v.player_form=1;
         var scale=p.getAttribute(Attributes.SCALE);
         scale.addTransientModifier(new AttributeModifier(SCALE,1.5,AttributeModifier.Operation.ADD_VALUE));
         p.refreshDimensions();
         if(!p.getAbilities().mayfly && !p.getAbilities().instabuild && !p.isSpectator()){
            v.amaruFlightGranted=true;MinepieceModVariables.saveAll();
            p.getAbilities().mayfly=true;f.flightOwned=true;p.onUpdateAbilities();
         }
         f.level.sendParticles(ParticleTypes.ELECTRIC_SPARK,p.getX(),p.getY()+3,p.getZ(),32,1,2,1,.05);
         f.level.playSound(null,p.blockPosition(),SoundEvents.TRIDENT_THUNDER.value(),SoundSource.PLAYERS,.45F,1.6F);
         sync(p);message(p,"Amaru — V : retour ; double saut : vol. Maintien : 8 énergie/s (+12 en vol).");return;
      }
      if(!scaled(p) || Math.abs(p.getScale()-2.5)>0.001){end(p);return;}
      v.PlayerEnergy=Math.max(0,v.PlayerEnergy-MAINTENANCE-(p.getAbilities().flying?FLIGHT_COST:0));
      if(--f.remaining<=0 || v.PlayerEnergy<=0){end(p);message(p,"Amaru terminé : durée ou énergie épuisée.");return;}
      if(f.remaining%20==0)sync(p);
   }
   public static void end(ServerPlayer p){
      var f=FORMS.remove(p.getUUID());
      var a=p.getAttribute(Attributes.SCALE);boolean marker=a!=null && a.hasModifier(SCALE);
      if(a!=null)a.removeModifier(SCALE);
      var v=MinepieceModVariables.get(p);
      boolean owned=(f!=null && f.flightOwned) || v.amaruFlightGranted;
      v.amaruFlightGranted=false;
      if(owned && !p.getAbilities().instabuild && !p.isSpectator()){
         p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();
      }
      if(f!=null || marker || owned || v.PlayerDevilFruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO)){
         v.player_form=0;p.refreshDimensions();sync(p);
         if(owned)MinepieceModVariables.saveAll();
         if(f!=null)TechniqueAnimations.playNamed(p,"cancel");
      }
   }
   private static void sync(ServerPlayer p){
      var v=MinepieceModVariables.get(p);
      String id=v.PlayerDevilFruit.isEmpty()?"":net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(v.PlayerDevilFruit.getItem()).toString();
      DevilFruitNetworking.sendTransformState(p,v.player_form,id);DevilFruitNetworking.sendSyncState(p);
   }
   private static void message(ServerPlayer p,String text){p.sendSystemMessage(Component.literal(text),false);}
   public static void register(){
      ServerTickEvents.END_SERVER_TICK.register(server->{for(var f:new ArrayList<>(FORMS.values()))tick(f.owner);});
      ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->end(handler.player));
      ServerLifecycleEvents.SERVER_STOPPING.register(server->{for(var f:new ArrayList<>(FORMS.values()))end(f.owner);});
      ServerLifecycleEvents.SERVER_STOPPED.register(server->FORMS.clear());
   }
}
