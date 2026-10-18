package net.mcreator.minepiece.dragonlab;

import com.google.gson.*;
import java.util.*;
import java.nio.file.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;
import net.mcreator.minepiece.client.*;
import net.mcreator.minepiece.network.DevilFruitNetworking;

public final class DragonLabClient implements ClientModInitializer {
   private static boolean ready;private static int tick,clock=-1,renderCount,lastCameraClock=-1;
   private static final Set<Integer> CAPTURED=new HashSet<>();
   private static final JsonObject CHECKS=new JsonObject(),RIG=new JsonObject();
   private static final JsonObject SCENES=new JsonObject();
   private static Vec3 start,stop;private static int cubes,boneCount;private static double bend;
   @Override public void onInitializeClient(){
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{
         if(p.scene().equals("dragon-ready"))ready=true;
         if(p.scene().startsWith("dragon-clock:"))clock=Integer.parseInt(p.scene().substring(13));
      }));
      ClientTickEvents.END_CLIENT_TICK.register(DragonLabClient::tick);
   }
   public static void rig(Map<String,ModelPart> bones,ModelPart root){
      renderCount++;cubes=root.getAllParts().stream().mapToInt(p->p.cubes.size()).sum();boneCount=bones.size();
      bend=Math.max(bend,Math.abs(bones.get("torso2").yRot-bones.get("torso23").yRot));
      for(String name:DragonSpineMath.BONES){var p=bones.get(name);var a=new JsonArray();for(float x:new float[]{p.x,p.y,p.z,p.xRot,p.yRot})a.add(x);RIG.add(name,a);}
   }
   private static void check(String id,boolean pass){CHECKS.addProperty(id,pass);org.slf4j.LoggerFactory.getLogger("DragonLab").info("[dragon-check] {} {}",id,pass?"PASS":"FAIL");}
   private static void cmd(Minecraft c,String command){c.player.connection.sendCommand("dragonlab "+command);}
   private static void mark(Minecraft c,String name){cmd(c,"mark "+name);}
   private static void capture(Minecraft c,String role,int t){
      Screenshot.grab(c.gameDirectory,role+"-dragon-"+t+".png",c.gameRenderer.mainRenderTarget(),1,m->{});
      var scene=new JsonObject();scene.addProperty("position",c.player.position().toString());scene.addProperty("camera",c.gameRenderer.mainCamera().position().toString());scene.addProperty("yaw",c.player.getYRot());scene.addProperty("pitch",c.player.getXRot());
      var actors=new JsonArray();for(var p:c.level.players()){var a=new JsonObject();a.addProperty("name",p.getName().getString());a.addProperty("position",p.position().toString());a.addProperty("form",TransformClientState.get(p.getUUID()).value());actors.add(a);}scene.add("players",actors);SCENES.add("frame-"+t,scene);
      var report=new JsonObject();report.add("checks",CHECKS.deepCopy());report.addProperty("render_evaluations",renderCount);
      report.add("scenes",SCENES.deepCopy());
      report.addProperty("original_cube_count",cubes);report.addProperty("original_bone_count",boneCount);report.addProperty("max_head_tail_bend",bend);report.add("rig",RIG.deepCopy());
      try{Files.writeString(c.gameDirectory.toPath().resolve("dragon-client.local.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(java.io.IOException e){throw new RuntimeException(e);}
   }
   private static void release(Minecraft c){c.options.keyUp.setDown(false);c.options.keyJump.setDown(false);c.options.keyShift.setDown(false);}
   private static void tick(Minecraft c){
      if(!ready||c.player==null||c.level==null||c.gui.screen()!=null)return;
      boolean observer=c.player.getName().getString().equals("FruitWitness");int t=observer?clock:tick;
      if(t<0)return;
      if(observer){if(t%10==0&&t!=lastCameraClock){lastCameraClock=t;cmd(c,"camera");}c.options.setCameraType(CameraType.FIRST_PERSON);}
      else {
         if(t%5==0)cmd(c,"clock "+t);
         if(t==0||t==415){release(c);cmd(c,"setup");}
         if(t==20||t==45||t==430||t==455)ClientPlayNetworking.send(new DevilFruitNetworking.TransformPayload());
         if(t==60||t==470)cmd(c,"lift");
         if(t==65||t==69||t==475||t==479)c.options.keyJump.setDown(true);
         if(t==67||t==71||t==477||t==481)c.options.keyJump.setDown(false);
         if(t==85){check("complete-form",TransformClientState.get(c.player.getUUID()).value()==2);check("native-takeoff",c.player.getAbilities().flying);mark(c,"takeoff");}
         if(t==90){start=c.player.position();c.player.setYRot(0);c.player.setXRot(-30);c.options.keyUp.setDown(true);c.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mark(c,"up-start");}
         if(t==130){Vec3 d=c.player.position().subtract(start);check("look-up-climbs",d.y>8&&d.z>12);mark(c,"up-end");start=c.player.position();c.player.setYRot(90);c.player.setXRot(20);}
         if(t==170){Vec3 d=c.player.position().subtract(start);check("look-turn-descends",d.x< -12&&d.y< -3);mark(c,"turn-end");start=c.player.position();c.player.setYRot(180);c.player.setXRot(-75);}
         if(t==210){Vec3 d=c.player.position().subtract(start);check("steep-ascent",d.y>12&&d.z< -3);release(c);mark(c,"steep-end");}
         if(t==230)stop=c.player.position();
         if(t==250){check("release-brakes",c.player.position().distanceTo(stop)<.25&&c.player.getDeltaMovement().length()<.005);mark(c,"hover");c.player.setYRot(270);c.player.setXRot(0);c.options.setCameraType(CameraType.THIRD_PERSON_BACK);c.options.keyUp.setDown(true);}
         if(t==290){release(c);cmd(c,"wall");}
         if(t==280)check("rear-camera-clears-mane",c.gameRenderer.mainCamera().position().distanceTo(c.player.getEyePosition())>12);
         if(t==300){c.player.setYRot(0);c.player.setXRot(0);c.options.keyUp.setDown(true);}
         if(t==310)cmd(c,"camera-roof");
         if(t==335){var camera=c.gameRenderer.mainCamera().position();check("rear-camera-clips-ceiling",camera.distanceTo(c.player.getEyePosition())<10&&c.level.getBlockState(net.minecraft.core.BlockPos.containing(camera)).isAir());}
         if(t==340){check("collision-stops-head",c.player.getZ()<=5.02&&c.player.getZ()>.1);release(c);mark(c,"wall-end");}
         if(t==345){cmd(c,"away");c.options.setCameraType(CameraType.FIRST_PERSON);}
         if(t==350){start=c.player.position();c.player.setYRot(-45);c.player.setXRot(45);c.options.keyUp.setDown(true);}
         if(t==385){Vec3 d=c.player.position().subtract(start);check("look-down-descends",d.y< -8&&d.x>8&&d.z>8);mark(c,"down-end");release(c);}
         if(t==390)cmd(c,"suppress");
         if(t==410){check("suppression-stops-flight",TransformClientState.get(c.player.getUUID()).value()==0&&!c.player.getAbilities().mayfly&&!c.player.getAbilities().flying);mark(c,"suppressed");}
         if(t==500){check("second-takeoff",TransformClientState.get(c.player.getUUID()).value()==2&&c.player.getAbilities().flying);cmd(c,"empty");}
         if(t==510)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         if(t==520){check("empty-energy-stops-flight",TransformClientState.get(c.player.getUUID()).value()==0&&!c.player.getAbilities().mayfly&&!c.player.getAbilities().flying);mark(c,"empty");}
         if(t==525)check("base-camera-restored",c.gameRenderer.mainCamera().position().distanceTo(c.player.getEyePosition())<6);
      }
      if(Set.of(120,150,190,240,280,335,380,525).stream().anyMatch(frame->t>=frame&&!CAPTURED.contains(frame))){
         int frame=Set.of(120,150,190,240,280,335,380,525).stream().filter(f->t>=f&&!CAPTURED.contains(f)).min(Integer::compareTo).orElseThrow();
         CAPTURED.add(frame);if(frame==525){check("mesh-preserved",cubes==346&&boneCount==51&&renderCount>10);check("spine-bends",bend>.2);}capture(c,observer?"observer":"actor",frame);
      }
      if(t>=540){release(c);capture(c,observer?"observer":"actor",540);c.stop();return;}
      if(!observer)tick++;
   }
}
