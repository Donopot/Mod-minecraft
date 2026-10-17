package net.mcreator.minepiece.client.renderer;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/** Reviewed ver15 bindings, confined to rendering. The source files remain byte-for-byte reserved. */
public final class OriginalAssetVisuals {
 public record Binding(String model,String texture,String animation,float scale,float yaw,float pitch,float x,float y,float z,boolean additive){}
 private static final Map<String,Binding> BINDINGS=load();
 private OriginalAssetVisuals(){}
 private static Map<String,Binding> load(){
  try(var stream=OriginalAssetVisuals.class.getResourceAsStream("/assets/minepiece/source_visuals.json")){
   if(stream==null)throw new IllegalStateException("Missing reviewed source visuals");
   var root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
   Map<String,Binding> result=new LinkedHashMap<>();
   for(var row:root.getAsJsonArray("bindings")){
    var b=row.getAsJsonObject();String model=b.get("model").getAsString();
    var binding=new Binding(model,b.get("texture").getAsString(),b.has("animation")?b.get("animation").getAsString():model,
      number(b,"scale",1),number(b,"yaw",180),number(b,"pitch",0),number(b,"x",0),number(b,"y",0),number(b,"z",0),b.has("additive")&&b.get("additive").getAsBoolean());
    for(var id:b.getAsJsonArray("profiles"))if(result.put(id.getAsString(),binding)!=null)throw new IllegalStateException("Duplicate source visual "+id);
   }
   return Map.copyOf(result);
  }catch(IOException e){throw new IllegalStateException("Cannot read reviewed source visuals",e);}
 }
 private static float number(JsonObject o,String key,float fallback){return o.has(key)?o.get(key).getAsFloat():fallback;}
 public static boolean nativeMesh(String profile){return BINDINGS.containsKey(profile);}
 public static Binding binding(String id){return BINDINGS.get(id);}
 public static String model(String id,String fallback){var b=binding(id);return b==null?fallback:b.model;}
 public static String texture(String id,String fallback){var b=binding(id);return b==null?fallback:b.texture;}
 public static String texture(String id,String fallback,int entityId){
  if(id.equals("b05_uta_noteattack")||id.equals("b05_uta_notelaser"))return new String[]{"haki_green","haki_blue","haki_red","haki_yellow","haki_purple"}[Math.floorMod(entityId,5)];
  return texture(id,fallback);
 }
 public static String texturePath(String value){return value.startsWith("textures/")?value+".png":"textures/entities/"+value+".png";}
 public static String animation(String id,String fallback){var b=binding(id);return b==null?fallback:b.animation;}
 public static String animation(String id,String fallback,float stretch){return id.equals("b04_aro_combo")&&stretch>1.2F?"source_aro_attack2":animation(id,fallback);}
 public static boolean additive(String id){var b=binding(id);return b!=null&&b.additive;}
 public static float modelScale(String id){var b=binding(id);return b==null?1:b.scale;}
 public static float yawOffset(String id){var b=binding(id);return b==null?0:b.yaw;}
 public static float verticalOffset(String id){var b=binding(id);return b==null?0:b.y;}
 public static void transform(PoseStack pose,String id,boolean arm,float factor){
  var b=binding(id);if(b==null)return;
  if(b.pitch!=0)pose.rotateDegrees(Axis.XP,b.pitch);
  if(arm){pose.rotateDegrees(Axis.YP,b.yaw);pose.scale(factor,factor,factor);}
  pose.scale(b.scale,b.scale,b.scale);pose.translate(b.x,b.y,b.z);
 }
}
