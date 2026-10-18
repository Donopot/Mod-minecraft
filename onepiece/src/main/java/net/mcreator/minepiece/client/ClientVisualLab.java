package net.mcreator.minepiece.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Screenshot;
import org.slf4j.LoggerFactory;

/** Opt-in, disposable-instance capture timeline; never injects desktop mouse or keyboard input. */
public final class ClientVisualLab {
   private ClientVisualLab() { }
   private static int ticks;
   public static void register() {
      if (!Boolean.getBoolean("opworldlike.visualCapture")) return;
      if(Boolean.getBoolean("opworldlike.prioritySoak")){PrioritySoakLab.register();return;}
      if("batch08".equals(System.getProperty("opworldlike.visualScenario"))) {Batch08VisualLab.register();return;}
      if("batch07".equals(System.getProperty("opworldlike.visualScenario"))) {Batch07VisualLab.register();return;}
      if("batch06".equals(System.getProperty("opworldlike.visualScenario"))) {Batch06VisualLab.register();return;}
      if("batch05".equals(System.getProperty("opworldlike.visualScenario"))) {Batch05VisualLab.register();return;}
      if("batch04".equals(System.getProperty("opworldlike.visualScenario"))) {Batch04VisualLab.register();return;}
      if("batch03".equals(System.getProperty("opworldlike.visualScenario"))) {Batch03VisualLab.register();return;}
      if("batch02".equals(System.getProperty("opworldlike.visualScenario"))) {Batch02VisualLab.register();return;}
      if("batch01".equals(System.getProperty("opworldlike.visualScenario"))) {Batch01VisualLab.register();return;}
      if("amaru".equals(System.getProperty("opworldlike.visualScenario"))) {AmaruVisualLab.register();return;}
      if("goro".equals(System.getProperty("opworldlike.visualScenario"))) {GoroVisualLab.register();return;}
      if("magu".equals(System.getProperty("opworldlike.visualScenario"))) {MaguVisualLab.register();return;}
      if("mera".equals(System.getProperty("opworldlike.visualScenario"))) {MeraVisualLab.register();return;}
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.player == null || client.level == null || client.gui.screen() != null) return;
         ticks++;
         if (ticks == 20) client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
         if (ticks == 60) capture(client, "01-leopard-hybrid.png");
         if (ticks == 80) ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
         if (ticks == 82) capture(client, "02-leopard-technique.png");
         if (ticks == 86) capture(client, "03-leopard-technique.png");
         if (ticks == 120) {
            // Side view makes a beam's length assessable; looking straight down
            // its axis hides it behind the muzzle flare.
            var camera = new net.minecraft.world.entity.decoration.ArmorStand(client.level, 14.5, 100, 6.5);
            camera.setNoGravity(true); camera.setYRot(36.87F); camera.setXRot(2); camera.setOldPosAndRot();
            client.options.setCameraType(CameraType.FIRST_PERSON);
            client.setCameraEntity(camera);
            client.player.connection.sendCommand("dvisual pika_pika laser1");
         }
         if (ticks == 140) ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
         if (ticks == 142) capture(client, "04-pika-laser.png");
         if (ticks == 144) capture(client, "05-pika-laser.png");
         if (ticks == 146) capture(client, "06-pika-laser.png");
         if (ticks == 200) client.player.connection.sendCommand("dvisual goro_goro sango");
         if (ticks == 220) ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
         if (ticks == 222) capture(client, "07-goro-lightning.png");
         if (ticks == 224) capture(client, "08-goro-lightning.png");
         if (ticks == 226) capture(client, "09-goro-lightning.png");
         if (ticks == 300) client.stop();
      });
   }
   private static void capture(net.minecraft.client.Minecraft client, String name) {
      Screenshot.grab(client.gameDirectory, name, client.gameRenderer.mainRenderTarget(), 1,
         message -> LoggerFactory.getLogger("OnePieceVisualLab").info("[visual-lab] {} {}", name, message.getString()));
   }
}
