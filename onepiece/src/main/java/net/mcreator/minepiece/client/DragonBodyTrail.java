package net.mcreator.minepiece.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.mcreator.minepiece.client.DragonFlightMath.Vector;

/** Spatial history, bounded by distance and UUID. Each deferred render receives an immutable pose. */
public final class DragonBodyTrail {
   private static final class Trail {
      final ArrayDeque<Vector> points=new ArrayDeque<>();
      Vector back;DragonSpineMath.Pose frozen;
      Trail(Vector head,Vector back){points.add(head);this.back=back;}
   }
   private static final Map<UUID,Trail> TRAILS=new LinkedHashMap<>();
   private static Object level;
   private DragonBodyTrail(){}
   public static DragonSpineMath.Pose capture(Avatar avatar,AvatarRenderState state,float partialTick) {
      var world=Minecraft.getInstance().level;
      if(level!=world){TRAILS.clear();level=world;}
      var form=TransformClientState.get(avatar.getUUID());
      TRAILS.keySet().removeIf(id->{var f=TransformClientState.get(id);return f.value()!=2||!DragonFlightControl.FRUIT.equals(f.fruit());});
      if(form.value()!=2||!DragonFlightControl.FRUIT.equals(form.fruit()))return null;
      var p=avatar.getPosition(partialTick);Vector head=new Vector(p.x,p.y,p.z);
      var look=avatar.getLookAngle();Vector back=new Vector(-look.x,-look.y,-look.z);
      Trail trail=TRAILS.get(avatar.getUUID());
      if(trail==null || head.subtract(trail.points.peekFirst()).length()>12){
         if(TRAILS.size()>=64)TRAILS.remove(TRAILS.keySet().iterator().next());
         trail=new Trail(head,back);TRAILS.put(avatar.getUUID(),trail);
      }
      boolean stone=((ParameciaRenderData)state).minepiece$stone();
      if(stone&&trail.frozen!=null)return trail.frozen;
      if(head.subtract(trail.points.peekFirst()).length()>=.25){trail.points.addFirst(head);while(trail.points.size()>512)trail.points.removeLast();}
      var pose=DragonSpineMath.pose(head,List.copyOf(trail.points),trail.back,state.bodyRot,state.yRot,state.xRot,state.scale,state.ageInTicks,stone);
      trail.frozen=stone?pose:null;
      return pose;
   }
}
