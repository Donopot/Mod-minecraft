package net.mcreator.minepiece.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.mcreator.minepiece.entity.*;
import net.minecraft.client.*;
import org.slf4j.LoggerFactory;

/** Seven scenes on two real clients; no desktop input. */
public final class GoroVisualLab {
   private static final String[] IDS={"vari","elthor","sango","mamaragan","karikrash","passive","raigo"};
   private static final java.util.Set<Integer> CAPTURES=java.util.Set.of(27,40,49,55,65,95,150);
   private static int tick,sceneAge;private static boolean ready;private static String scene="";
   private static net.minecraft.world.phys.Vec3 start;
   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(payload,context)->context.client().execute(()->{
         if(payload.scene().equals("ready"))ready=true;else if(!payload.scene().equals(scene)){scene=payload.scene();sceneAge=0;}
      }));
      ClientTickEvents.END_CLIENT_TICK.register(GoroVisualLab::tick);
   }
   private static void tick(Minecraft client) {
      if(client.player==null || client.level==null || client.gui.screen()!=null)return;
      if(client.player.getName().getString().equals("FruitWitness")) {
         if(scene.isEmpty())return;sceneAge++;
         if(scene.equals("raigo") && sceneAge==49) {
            boolean found=false;for(var e:client.level.entitiesForRendering())if(e instanceof SkillProjectileEntity shot && shot.getVisualProfile().equals("goro_raigo"))found=true;
            LoggerFactory.getLogger("GoroVisual").info("[goro-client-check] raigo-projectile {}",found?"PASS":"FAIL");
         }
         if(CAPTURES.contains(sceneAge))capture(client,"observer-"+scene+"-"+sceneAge+".png");
         if(scene.equals("raigo") && sceneAge==165)client.stop();return;
      }
      if(!ready)return;int index=tick/180,offset=tick%180;
      if(index>=IDS.length){client.stop();return;}String id=IDS[index];
      if(offset==0){client.options.setCameraType(CameraType.FIRST_PERSON);client.player.connection.sendCommand("dvisual goro_goro "+id);}
      if(offset==18)start=client.player.position();
      if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
      if(offset==24 && id.equals("passive")) {
         double moved=client.player.position().distanceTo(start);
         LoggerFactory.getLogger("GoroVisual").info("[goro-client-check] blink-real-distance {} moved={}",moved>20 && moved<=32.1?"PASS":"FAIL",moved);
      }
      if(offset==45)ClientPlayNetworking.send(new DevilFruitNetworking.SkillReleasePayload());
      if(offset==48)client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
      if(CAPTURES.contains(offset))capture(client,"actor-"+id+"-"+offset+".png");
      tick++;
   }
   private static void capture(Minecraft client,String name) {
      Screenshot.grab(client.gameDirectory,name,client.gameRenderer.mainRenderTarget(),1,m->LoggerFactory.getLogger("GoroVisual").info("[goro-visual] {} {}",name,m.getString()));
   }
}
