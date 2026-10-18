package net.mcreator.minepiece.client;

import java.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Avatar;

/** Actions belong to the avatar UUID and the current world, including remote players. */
public final class RigClientState {
   private record Action(String name,float started,String fruit,double form) {}
   private record Form(String fruit,double value,float started) {}
   private static final Map<UUID,Action> ACTIONS=new HashMap<>();
   private static final Map<UUID,Form> FORMS=new HashMap<>();
   private static final Map<UUID,Float> AIRBORNE=new HashMap<>();
   private static ClientLevel level;
   private static long prunedAt=Long.MIN_VALUE;
   private RigClientState() {}
   private static void level(ClientLevel current){if(level!=current){level=current;ACTIONS.clear();FORMS.clear();AIRBORNE.clear();prunedAt=Long.MIN_VALUE;}}
   public static void action(ClientLevel current,Avatar avatar,String name){
      level(current);var form=TransformClientState.get(avatar.getUUID());
      ACTIONS.put(avatar.getUUID(),new Action(name,avatar.tickCount,form.fruit(),form.value()));
   }
   public static RigRenderData.Snapshot capture(ClientLevel current,Avatar avatar,float partialTick){
      level(current);UUID id=avatar.getUUID();float now=avatar.tickCount+partialTick;
      long time=current.getGameTime();
      if(prunedAt==Long.MIN_VALUE||time<prunedAt||time-prunedAt>=200){
         Set<UUID> present=new HashSet<>();for(var entity:current.entitiesForRendering())present.add(entity.getUUID());
         ACTIONS.keySet().retainAll(present);FORMS.keySet().retainAll(present);AIRBORNE.keySet().retainAll(present);prunedAt=time;
      }
      var form=TransformClientState.get(id);var old=FORMS.get(id);
      if(old==null||!old.fruit.equals(form.fruit())||old.value!=form.value()||old.started>now){
         old=new Form(form.fruit(),form.value(),now);FORMS.put(id,old);
      }
      var action=ACTIONS.get(id);float age=action==null?1000:Math.max(0,(now-action.started)/20);
      if(action!=null&&(!action.fruit.equals(form.fruit())||action.form!=form.value())){ACTIONS.remove(id);action=null;}
      if(form.value()==0){ACTIONS.remove(id);FORMS.remove(id);AIRBORNE.remove(id);action=null;}
      boolean airborne=!avatar.onGround();float airborneAge=0;
      if(airborne&&form.value()>0)airborneAge=Math.max(0,(now-AIRBORNE.computeIfAbsent(id,k->now))/20);
      else AIRBORNE.remove(id);
      float walking=(float)Math.clamp(avatar.getDeltaMovement().horizontalDistance()*8,0,1);
      return new RigRenderData.Snapshot((now-old.started)/20,action==null?"":action.name,age,walking,airborne,airborneAge);
   }
   public static void clear(){ACTIONS.clear();FORMS.clear();AIRBORNE.clear();level=null;prunedAt=Long.MIN_VALUE;}
   public static int trackedEntries(){return ACTIONS.size()+FORMS.size()+AIRBORNE.size();}
}
