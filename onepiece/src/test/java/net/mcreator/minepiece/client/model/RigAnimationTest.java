package net.mcreator.minepiece.client.model;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Real reserved clips plus synthetic discontinuities, loop boundaries and rejected expressions. */
public final class RigAnimationTest {
   private static int checks;
   private static void check(boolean ok,String detail){if(!ok)throw new AssertionError(detail);checks++;}
   public static void main(String[] args)throws Exception{
      var synthetic=JsonParser.parseString("""
         {"animations":{"test":{"animation_length":2,"loop":true,"bones":{"body":{
         "position":{"0":[0,0,0],"1":{"pre":[2,0,0],"post":[4,0,0]},"2":[6,0,0]}}}}}}
         """).getAsJsonObject();
      var clip=RigAnimation.read(synthetic).get("test");
      check(Math.abs(clip.sample(.5).get("body").position().x()-1)<.001,"linear interpolation before discontinuity");
      check(clip.sample(1).get("body").position().x()==4,"post value at discontinuity");
      check(clip.sample(1.5).get("body").position().x()==5,"interpolate from post value");
      check(clip.sample(2).get("body").position().x()==0,"loop restarts at boundary");
      check(clip.sample(-2).get("body").position().x()==0,"negative time clamps");
      for(String easing:List.of("linear","easeInOutSine","easeInQuad","easeOutQuad","easeInBack","easeOutBack","easeOutQuint")){
         check(Math.abs(RigAnimation.ease(easing,0))<.0001,"easing origin "+easing);
         check(Math.abs(RigAnimation.ease(easing,1)-1)<.0001,"easing end "+easing);
      }
      try{RigAnimation.ease("unknown",.5);throw new AssertionError("unknown easing accepted");}catch(IllegalArgumentException expected){checks++;}
      int tracks=0;
      for(String rig:List.of("amaru","torifullform","buddha","wolfbeast","leopardbeastrework","gumpistol","gumgatling","guracrack","guracrackground","guracracksea","gurauppercut","takt1","takt2","takt3","takt4","takt5","takt6","buzzcut","ursusshock")){
         var folder=Path.of(args[0]);var geo=JsonParser.parseString(Files.readString(folder.resolve(rig+".geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
         Set<String> bones=new HashSet<>();for(var b:geo.getAsJsonArray("bones"))bones.add(b.getAsJsonObject().get("name").getAsString());
         var clips=RigAnimation.read(JsonParser.parseString(Files.readString(folder.resolve(rig+".animation.json"))).getAsJsonObject());
         for(var entry:clips.entrySet()){
            var animation=entry.getValue();tracks++;check(bones.containsAll(animation.bones()),rig+" missing animated bone");
            Set<String> poses=new HashSet<>();
            for(int i=0;i<=120;i++){
               var pose=animation.sample(animation.length*i/120);poses.add(pose.toString());
               for(var p:pose.values())for(var v:List.of(p.rotation(),p.position(),p.scale()))check(Float.isFinite(v.x())&&Float.isFinite(v.y())&&Float.isFinite(v.z()),rig+" finite sample "+entry.getKey());
            }
            check(poses.size()>1,rig+" static track "+entry.getKey());
         }
      }
      check(tracks==63,"all63 original clips tested");
      System.out.println("RIG_ANIMATION_PASS tracks="+tracks+" assertions="+checks);
   }
}
