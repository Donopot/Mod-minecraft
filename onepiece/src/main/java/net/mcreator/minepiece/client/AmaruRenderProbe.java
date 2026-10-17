package net.mcreator.minepiece.client;

import java.util.*;
import net.mcreator.minepiece.client.model.AmaruModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Opt-in evidence from actual deferred model evaluation, not packet receipt. */
public final class AmaruRenderProbe {
   private static final Map<String,Integer> EVALUATED=new HashMap<>();
   private static final Map<String,Double> POSES=new HashMap<>();
   private record Sample(float time,float rx,float rz,float lx,float lz){}
   private static final Map<String,Sample> FIRST=new HashMap<>();
   private static final Map<String,Double> DELTAS=new HashMap<>();
   private static final java.util.Set<String> MOVED=new HashSet<>();
   public static String stage="";
   private AmaruRenderProbe(){}
   public static void evaluated(EntityRenderState state,AmaruModel m){
      if(!Boolean.getBoolean("opworldlike.visualCapture") || !"amaru".equals(System.getProperty("opworldlike.visualScenario")))return;
      var level=net.minecraft.client.Minecraft.getInstance().level;
      if(level==null || !(state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState avatar))return;
      var entity=level.getEntity(avatar.id);
      var expected=java.util.UUID.nameUUIDFromBytes("OfflinePlayer:FruitLab".getBytes(java.nio.charset.StandardCharsets.UTF_8));
      if(entity==null || !entity.getUUID().equals(expected))return;
      EVALUATED.merge(stage,1,Integer::sum);
      double pose=Math.abs(m.rightArm.xRot)+Math.abs(m.rightArm.zRot)+Math.abs(m.leftArm.xRot)+Math.abs(m.leftArm.zRot);
      POSES.merge(stage,pose,Math::max);
      Sample now=new Sample((float)Math.floor(state.ageInTicks),m.rightArm.xRot,m.rightArm.zRot,m.leftArm.xRot,m.leftArm.zRot);
      Sample old=FIRST.putIfAbsent(stage,now);
      if(old!=null && now.time-old.time>2){
         double delta=Math.abs(now.rx-old.rx)+Math.abs(now.rz-old.rz)+Math.abs(now.lx-old.lx)+Math.abs(now.lz-old.lz);
         DELTAS.merge(stage,delta,Math::max);
         if(delta>.25)MOVED.add(stage);
      }
   }
   public static boolean seen(String key){return EVALUATED.getOrDefault(key,0)>0;}
   public static boolean animated(String key){return seen(key) && MOVED.contains(key);}
   public static String evidence(String key){return "evaluations="+EVALUATED.getOrDefault(key,0)+", pose="+POSES.getOrDefault(key,0D)+", delta="+DELTAS.getOrDefault(key,0D);}
}
