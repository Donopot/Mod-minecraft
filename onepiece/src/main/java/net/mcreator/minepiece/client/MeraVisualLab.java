package net.mcreator.minepiece.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.slf4j.LoggerFactory;

/** Opt-in timeline with a separate, real observing client. No desktop input. */
public final class MeraVisualLab {
   private static final String[] SCENES={"hiken","higan","hibashira","kagero","hotarubi","kaenryuo","daienkai","propulsion"};
   private static boolean ready;
   private static int tick,sceneAge;
   private static String scene="";
   private static net.minecraft.world.phys.Vec3 propulsionStart;
   private MeraVisualLab() { }
   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(payload,context)->context.client().execute(()->{
         if(payload.scene().equals("ready")) ready=true;
         else if(!scene.equals(payload.scene())) {scene=payload.scene();sceneAge=0;}
      }));
      ClientTickEvents.END_CLIENT_TICK.register(MeraVisualLab::tick);
   }
   private static void tick(Minecraft client) {
      if(client.player==null || client.level==null || client.gui.screen()!=null) return;
      boolean observer=client.player.getName().getString().equals("FruitWitness");
      if(observer) {
         client.options.setCameraType(CameraType.FIRST_PERSON);
         if(scene.isEmpty()) return;
         sceneAge++;
         if(sceneAge==47 && java.util.Set.of("hibashira","kaenryuo").contains(scene)) {
            boolean present=false;
            for(var entity:client.level.entitiesForRendering()) if(entity instanceof net.mcreator.minepiece.entity.EntityGomuArmEntity fx && fx.getVisualProfile().equals(scene)) present=true;
            LoggerFactory.getLogger("MeraVisualLab").info("[mera-client-check] {}-after-15-ticks {}",scene,present?"PASS":"FAIL");
         }
         if(sceneAge==30 || sceneAge==47 || sceneAge==54) capture(client,"observer-"+scene+"-"+sceneAge+".png");
         if(scene.equals("propulsion") && sceneAge==100) client.stop();
         return;
      }
      if(!ready) return;
      int index=tick/120,offset=tick%120;
      if(index>=SCENES.length) {client.stop();return;}
      String current=SCENES[index];
      boolean charged=java.util.Set.of("hiken","higan","hotarubi","daienkai").contains(current);
      if(offset==0) {
         client.setCameraEntity(client.player);client.options.setCameraType(CameraType.FIRST_PERSON);
         client.player.connection.sendCommand("dvisual mera_mera "+current);
      }
      if(current.equals("propulsion") && offset==18) propulsionStart=client.player.position();
      if(current.equals("propulsion") && offset==60) {
         double distance=client.player.position().distanceTo(propulsionStart);
         LoggerFactory.getLogger("MeraVisualLab").info("[mera-client-check] propulsion-real-distance {} moved={}",distance>=3?"PASS":"FAIL",distance);
      }
      if(offset==20) {
         if(current.equals("propulsion")) ClientPlayNetworking.send(new DevilFruitNetworking.TransformPayload());
         else ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
      }
      if(offset==22) capture(client,"actor-"+current+"-fps.png");
      if(offset==(charged?35:24)) {
         // Keep the real player as the controlled camera: a detached camera freezes LocalPlayer travel.
         client.setCameraEntity(client.player);client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
      }
      if(charged && offset==42) capture(client,"actor-"+current+"-charge.png");
      if(charged && offset==45) ClientPlayNetworking.send(new DevilFruitNetworking.SkillReleasePayload());
      if(offset==(charged?47:30)) capture(client,"actor-"+current+"-active.png");
      if(offset==(charged?54:42)) capture(client,"actor-"+current+"-impact.png");
      if(offset==70) capture(client,"actor-"+current+"-after.png");
      // A second short take keeps the actual emission in first person, with both held items.
      if(charged && offset==75) {
         client.setCameraEntity(client.player);client.options.setCameraType(CameraType.FIRST_PERSON);
         client.player.connection.sendCommand("dvisual mera_mera "+current);
      }
      if(charged && offset==80) ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
      if(charged && offset==81) ClientPlayNetworking.send(new DevilFruitNetworking.SkillReleasePayload());
      if(charged && (offset==82 || offset==92 || offset==104)) capture(client,"actor-"+current+"-fps-emission-"+offset+".png");
      tick++;
   }
   private static void capture(Minecraft client,String name) {
      Screenshot.grab(client.gameDirectory,name,client.gameRenderer.mainRenderTarget(),1,
         message->LoggerFactory.getLogger("MeraVisualLab").info("[mera-visual] {} {}",name,message.getString()));
   }
}
