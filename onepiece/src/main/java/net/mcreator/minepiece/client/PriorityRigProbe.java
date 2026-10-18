package net.mcreator.minepiece.client;

import java.util.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.mcreator.minepiece.client.model.RigAnimation;

/** Opt-in evidence from real deferred evaluation of the new rigs, never a packet-only marker. */
public final class PriorityRigProbe {
   private static final Map<String,String> FIRST=new HashMap<>();
   private static final Set<String> MOVED=new HashSet<>();
   public static void evaluated(EntityRenderState state,String model,String clip,Map<String,RigAnimation.Pose> pose,int bones){
      if(!Boolean.getBoolean("opworldlike.visualCapture"))return;
      var signature=new StringBuilder();new TreeMap<>(pose).forEach((name,p)->signature.append(name).append(p));
      String key=model+"/"+clip;String old=FIRST.putIfAbsent(key,signature.toString());
      if(old==null)org.slf4j.LoggerFactory.getLogger("PriorityRig").info("[priority-rig] {} bones={} poseChannels={}",key,bones,pose.size());
      else if(!old.equals(signature.toString())&&MOVED.add(key))org.slf4j.LoggerFactory.getLogger("PriorityRig").info("[priority-rig-motion] {} PASS",key);
   }
}
