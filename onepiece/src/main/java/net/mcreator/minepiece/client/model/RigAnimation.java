package net.mcreator.minepiece.client.model;

import com.google.gson.*;
import java.util.*;

/** Numeric Bedrock tracks used by the reserved rigs. Unsupported expressions fail at loading. */
public final class RigAnimation {
   public record Vector(float x,float y,float z) {
      public static final Vector ZERO=new Vector(0,0,0), ONE=new Vector(1,1,1);
      Vector mix(Vector b,double t){return new Vector((float)(x+(b.x-x)*t),(float)(y+(b.y-y)*t),(float)(z+(b.z-z)*t));}
   }
   public record Pose(Vector rotation,Vector position,Vector scale) {}
   private record Key(double time,Vector before,Vector after,String easing) {}
   private record Channel(List<Key> keys) {
      Vector sample(double time){
         Key first=keys.getFirst();if(time<first.time)return first.before;
         for(int i=1;i<keys.size();i++){
            Key right=keys.get(i),left=keys.get(i-1);
            if(time<right.time)return left.after.mix(right.before,ease(right.easing,(time-left.time)/(right.time-left.time)));
         }
         return keys.getLast().after;
      }
   }
   private record Bone(Channel rotation,Channel position,Channel scale) {}
   public final double length;
   public final boolean loop;
   private final Map<String,Bone> bones;
   private RigAnimation(double length,boolean loop,Map<String,Bone> bones){this.length=length;this.loop=loop;this.bones=Map.copyOf(bones);}
   public Set<String> bones(){return bones.keySet();}
   public Map<String,Pose> sample(double seconds){
      double time=loop?Math.max(0,seconds)%length:Math.clamp(seconds,0,length);
      Map<String,Pose> pose=new LinkedHashMap<>();
      for(var entry:bones.entrySet()){
         Bone b=entry.getValue();pose.put(entry.getKey(),new Pose(b.rotation==null?Vector.ZERO:b.rotation.sample(time),b.position==null?Vector.ZERO:b.position.sample(time),b.scale==null?Vector.ONE:b.scale.sample(time)));
      }
      return pose;
   }
   public static Map<String,RigAnimation> read(JsonObject json){
      Map<String,RigAnimation> result=new LinkedHashMap<>();
      for(var entry:json.getAsJsonObject("animations").entrySet()){
         var clip=entry.getValue().getAsJsonObject();double length=clip.get("animation_length").getAsDouble();
         if(!Double.isFinite(length)||length<=0)throw new IllegalArgumentException("Invalid rig duration: "+entry.getKey());
         Map<String,Bone> bones=new LinkedHashMap<>();
         for(var bone:clip.getAsJsonObject("bones").entrySet()){
            var b=bone.getValue().getAsJsonObject();bones.put(bone.getKey(),new Bone(channel(b.get("rotation")),channel(b.get("position")),channel(b.get("scale"))));
         }
         boolean loop=clip.has("loop")&&clip.get("loop").isJsonPrimitive()&&clip.get("loop").getAsJsonPrimitive().isBoolean()&&clip.get("loop").getAsBoolean();
         result.put(entry.getKey(),new RigAnimation(length,loop,bones));
      }
      return Map.copyOf(result);
   }
   private static Channel channel(JsonElement value){
      if(value==null)return null;
      List<Key> keys=new ArrayList<>();
      if(!value.isJsonObject()||value.getAsJsonObject().has("vector"))keys.add(key(0,value));
      else for(var entry:value.getAsJsonObject().entrySet()){
         double time=Double.parseDouble(entry.getKey());if(!Double.isFinite(time)||time<0)throw new IllegalArgumentException("Invalid key time");
         keys.add(key(time,entry.getValue()));
      }
      keys.sort(Comparator.comparingDouble(Key::time));if(keys.isEmpty())throw new IllegalArgumentException("Empty rig channel");
      return new Channel(List.copyOf(keys));
   }
   private static Key key(double time,JsonElement value){
      if(!value.isJsonObject()){var v=vector(value);return new Key(time,v,v,"linear");}
      var o=value.getAsJsonObject();String easing=o.has("easing")?o.get("easing").getAsString():"linear";
      ease(easing,.5); // Validate the complete track, even if it has not been selected yet.
      if(o.has("vector")){var v=vector(o.get("vector"));return new Key(time,v,v,easing);}
      if(o.has("pre")&&o.has("post"))return new Key(time,vector(o.get("pre")),vector(o.get("post")),easing);
      throw new IllegalArgumentException("Unsupported rig key: "+o);
   }
   private static Vector vector(JsonElement value){
      if(value.isJsonPrimitive()){float v=number(value);return new Vector(v,v,v);}
      JsonArray v=value.getAsJsonArray();if(v.size()!=3)throw new IllegalArgumentException("Expected rig vector");
      return new Vector(number(v.get(0)),number(v.get(1)),number(v.get(2)));
   }
   private static float number(JsonElement value){float v=value.getAsFloat();if(!Float.isFinite(v))throw new IllegalArgumentException("Non-finite rig channel");return v;}
   public static double ease(String name,double t){
      return switch(name){
         case "linear" -> t;
         case "step" -> t<1?0:1;
         case "easeInSine" -> 1-Math.cos(t*Math.PI/2);
         case "easeOutSine" -> Math.sin(t*Math.PI/2);
         case "easeInOutSine" -> -(Math.cos(Math.PI*t)-1)/2;
         case "easeInQuad" -> t*t;
         case "easeOutQuad" -> 1-(1-t)*(1-t);
         case "easeInCubic" -> t*t*t;
         case "easeOutCubic" -> 1-Math.pow(1-t,3);
         case "easeInQuart" -> Math.pow(t,4);
         case "easeOutQuart" -> 1-Math.pow(1-t,4);
         case "easeInQuint" -> Math.pow(t,5);
         case "easeOutQuint" -> 1-Math.pow(1-t,5);
         case "easeInCirc" -> 1-Math.sqrt(1-t*t);
         case "easeOutExpo" -> t==1?1:1-Math.pow(2,-10*t);
         case "easeInBack" -> 2.70158*t*t*t-1.70158*t*t;
         case "easeOutBack" -> 1+2.70158*Math.pow(t-1,3)+1.70158*Math.pow(t-1,2);
         case "easeInElastic" -> t==0||t==1?t:-Math.pow(2,10*t-10)*Math.sin((t*10-10.75)*2*Math.PI/3);
         case "easeOutElastic" -> t==0||t==1?t:Math.pow(2,-10*t)*Math.sin((t*10-.75)*2*Math.PI/3)+1;
         default -> throw new IllegalArgumentException("Unsupported rig easing: "+name);
      };
   }
}
