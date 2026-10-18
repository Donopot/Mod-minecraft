package net.mcreator.minepiece.client;

/** Bounded steering shared by the real flight controller and standalone numerical tests. */
public final class DragonFlightMath {
   public record Vector(double x,double y,double z) {
      public Vector add(Vector b){return new Vector(x+b.x,y+b.y,z+b.z);}
      public Vector subtract(Vector b){return new Vector(x-b.x,y-b.y,z-b.z);}
      public Vector scale(double s){return new Vector(x*s,y*s,z*s);}
      public double length(){return Math.sqrt(x*x+y*y+z*z);}
      public Vector normalized(){double n=length();return n<1e-9?ZERO:scale(1/n);}
   }
   public static final Vector ZERO=new Vector(0,0,0);
   private DragonFlightMath(){}
   public static Vector steer(Vector velocity,double yaw,double pitch,double forward,double strafe,
                              double vertical,boolean sprint) {
      double y=Math.toRadians(yaw),p=Math.toRadians(pitch),speed=sprint?.9:.65;
      Vector look=new Vector(-Math.sin(y)*Math.cos(p),-Math.sin(p),Math.cos(y)*Math.cos(p));
      Vector right=new Vector(Math.cos(y),0,Math.sin(y));
      Vector target=look.scale(Math.clamp(forward,-1,1)*(forward<0?.35:speed))
         .add(right.scale(Math.clamp(strafe,-1,1)*.35)).add(new Vector(0,Math.clamp(vertical,-1,1)*.3,0));
      if(target.length()>speed)target=target.normalized().scale(speed);
      if(target.length()<1e-9){Vector stopped=velocity.scale(.72);return stopped.length()<.003?ZERO:stopped;}
      Vector acceleration=target.subtract(velocity);
      if(acceleration.length()>.065)acceleration=acceleration.normalized().scale(.065);
      Vector next=velocity.add(acceleration);
      return next.length()>speed?next.normalized().scale(speed):next;
   }
}
