package net.mcreator.minepiece.client;

import java.util.*;
import net.mcreator.minepiece.client.DragonFlightMath.Vector;

/** Re-parents the original rigid sections mathematically; no cubes or UVs are generated. */
public final class DragonSpineMath {
   public static final String[] BONES={"torso1","torso2","torso3","torso4","torso5","torso6","torso7","torso8","torso9","torso10","torso14","torso15","torso16","torso17","torso18","torso11","torso12","torso13","torso22","torso23","torso24","torso25","torso26","torso27"};
   public static final int[] PIVOTS={2,44,86,128,170,212,254,296,338,380,422,464,506,548,590,633,675,717,759,801,843,885,927,969};
   public record Joint(float x,float y,float z,float pitch,float yaw){}
   public record Pose(List<Joint> joints,float headYaw,float headPitch) {
      public Pose {joints=List.copyOf(joints);}
   }
   private DragonSpineMath(){}
   public static Vector at(Vector head,List<Vector> history,Vector tailDirection,double distance) {
      Vector from=head,back=tailDirection.normalized();double left=Math.max(0,distance);
      for(Vector to:history){Vector delta=to.subtract(from);double length=delta.length();
         if(length<1e-6)continue;
         if(left<=length)return from.add(delta.scale(left/length));
         left-=length;from=to;back=delta.scale(1/length);
      }
      return from.add(back.scale(left));
   }
   private static Vector waved(Vector head,List<Vector> history,Vector back,double distance,double age,boolean stone) {
      Vector at=at(head,history,back,distance);if(stone)return at;
      Vector tangent=at(head,history,back,distance+.3).subtract(at(head,history,back,Math.max(0,distance-.3))).normalized();
      Vector side=new Vector(-tangent.z(),0,tangent.x()).normalized();
      double amplitude=Math.min(.20,distance*.05),phase=age*.09-distance*.38;
      return at.add(side.scale(Math.sin(phase)*amplitude)).add(new Vector(0,Math.sin(phase*.8)*amplitude*.3,0));
   }
   public static Pose pose(Vector head,List<Vector> history,Vector back,float bodyYaw,float headYaw,
                           float pitch,float entityScale,double age,boolean stone) {
      double scale=.35*Math.max(.1,entityScale),unit=16/scale,yaw=Math.toRadians(bodyYaw);
      var result=new ArrayList<Joint>();
      for(int i=0;i<PIVOTS.length;i++) {
         double distance=PIVOTS[i]/unit,nextDistance=(i+1<PIVOTS.length?PIVOTS[i+1]:PIVOTS[i]+42)/unit;
         Vector point=waved(head,history,back,distance,age,stone),delta=point.subtract(head);
         Vector axis=waved(head,history,back,nextDistance,age,stone).subtract(point).normalized();
         double x=delta.x()*Math.cos(yaw)+delta.z()*Math.sin(yaw),z=delta.x()*Math.sin(yaw)-delta.z()*Math.cos(yaw);
         double ax=axis.x()*Math.cos(yaw)+axis.z()*Math.sin(yaw),az=axis.x()*Math.sin(yaw)-axis.z()*Math.cos(yaw);
         result.add(new Joint((float)(x*unit),(float)(-delta.y()*unit),(float)(z*unit),
            (float)Math.atan2(axis.y(),Math.hypot(ax,az)),(float)Math.atan2(ax,az)));
      }
      return new Pose(result,(float)Math.toRadians(headYaw),(float)Math.toRadians(pitch));
   }
}
