package net.mcreator.minepiece.client;

import java.util.*;
import net.mcreator.minepiece.client.DragonFlightMath.Vector;

public final class DragonMotionTest {
   private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
   public static void main(String[] args) {
      Vector velocity=DragonFlightMath.ZERO;
      for(int i=0;i<40;i++)velocity=DragonFlightMath.steer(velocity,0,-60,1,0,0,false);
      check(velocity.y()>.5&&velocity.z()>.3,"looking upward climbs");
      for(int i=0;i<40;i++)velocity=DragonFlightMath.steer(velocity,90,45,1,0,0,false);
      check(velocity.x()<-.4&&velocity.y()<-.4,"turned downward follows 3D look");
      for(int i=0;i<60;i++)velocity=DragonFlightMath.steer(velocity,90,45,0,0,0,false);
      check(velocity.length()==0,"releasing input brakes to hover");
      for(int i=0;i<400;i++){
         Vector old=velocity;velocity=DragonFlightMath.steer(velocity,i*11,Math.sin(i)*89,1,1,1,true);
         check(velocity.length()<=.900001,"diagonals cannot exceed speed bound");
         check(velocity.subtract(old).length()<=.065001,"turn acceleration bounded");
      }
      Vector head=new Vector(10,5,10),back=new Vector(0,0,-1);
      var trail=List.of(head,new Vector(0,5,10),new Vector(0,5,-20));
      check(DragonSpineMath.at(head,trail,back,5).subtract(new Vector(5,5,10)).length()<1e-8,"body follows old heading before bend");
      check(DragonSpineMath.at(head,trail,back,15).subtract(new Vector(0,5,5)).length()<1e-8,"tail follows actual path around bend");
      var straight=DragonSpineMath.pose(DragonFlightMath.ZERO,List.of(),back,0,0,0,2,0,true);
      check(straight.joints().size()==24,"all original body sections retained");
      for(int i=0;i<24;i++){
         var j=straight.joints().get(i);
         check(Math.abs(j.z()-DragonSpineMath.PIVOTS[i])<.001&&Math.abs(j.x())<.001&&Math.abs(j.y())<.001,"straight rig preserves original pivots");
      }
      var climb=DragonSpineMath.pose(DragonFlightMath.ZERO,List.of(),new Vector(0,-1,-1),0,0,-45,2,15,false);
      check(climb.joints().get(20).y()>100&&climb.joints().get(20).pitch()<-.5,"spine follows vertical path");
      try{straight.joints().clear();throw new AssertionError("mutable render pose");}catch(UnsupportedOperationException expected){}
      System.out.println("Dragon steering, braking, speed/acceleration bounds, 3D path, original pivots and immutable poses PASS");
   }
}
