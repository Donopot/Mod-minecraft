package net.mcreator.minepiece.client;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;
import org.slf4j.LoggerFactory;

/** Five-scene private client/observer capture, using the game's own API. */
public final class MaguVisualLab {
   private static final String[] IDS={"daifunka","inugamiguren","kageromagumagu","volcanicexplosion","ryuseikazan"};
   private static boolean ready;private static int tick,sceneAge;private static String scene="";
   private static net.minecraft.world.phys.Vec3 dashStart;
   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(payload,context)->context.client().execute(()->{
         if(payload.scene().equals("ready"))ready=true;else if(!scene.equals(payload.scene())){scene=payload.scene();sceneAge=0;}
      }));
      ClientTickEvents.END_CLIENT_TICK.register(MaguVisualLab::tick);
   }
   private static void tick(Minecraft client) {
      if(client.player==null || client.level==null || client.gui.screen()!=null)return;
      if(client.player.getName().getString().equals("FruitWitness")) {
         if(scene.isEmpty())return;sceneAge++;
         if(sceneAge==47 && scene.equals("volcanicexplosion")) {
            boolean found=false;for(var e:client.level.entitiesForRendering())if(e instanceof net.mcreator.minepiece.entity.EntityGomuArmEntity fx && fx.getVisualProfile().equals("magu_volcanic"))found=true;
            LoggerFactory.getLogger("MaguVisual").info("[magu-client-check] eruption-after-15-ticks {}",found?"PASS":"FAIL");
         }
         if(sceneAge==24 || sceneAge==30 || sceneAge==35 || sceneAge==40 || sceneAge==49 || sceneAge==53 || sceneAge==95 || sceneAge==140)capture(client,"observer-"+scene+"-"+sceneAge+".png");
         if(scene.equals("ryuseikazan") && sceneAge==150)client.stop();return;
      }
      if(!ready)return;int index=tick/160,offset=tick%160;
      if(index>=IDS.length){client.stop();return;}String id=IDS[index];
      if(offset==0){client.setCameraEntity(client.player);client.options.setCameraType(CameraType.FIRST_PERSON);client.player.connection.sendCommand("dvisual magu_magu "+id);}
      if(offset==18 && id.equals("kageromagumagu"))dashStart=client.player.position();
      if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
      if(offset==35 && id.equals("kageromagumagu")) {
         double distance=client.player.position().distanceTo(dashStart);
         LoggerFactory.getLogger("MaguVisual").info("[magu-client-check] dash-real-distance {} moved={}",distance>=3?"PASS":"FAIL",distance);
      }
      if(offset==45)ClientPlayNetworking.send(new DevilFruitNetworking.SkillReleasePayload());
      if(offset==48)client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
      if(offset==22 || offset==27 || offset==35 || offset==40 || offset==42 || offset==46 || offset==49 || offset==53 || offset==95 || offset==120 || offset==145)capture(client,"actor-"+id+"-"+offset+".png");
      tick++;
   }
   private static void capture(Minecraft client,String name) {
      Screenshot.grab(client.gameDirectory,name,client.gameRenderer.mainRenderTarget(),1,m->LoggerFactory.getLogger("MaguVisual").info("[magu-visual] {} {}",name,m.getString()));
   }
}
