package net.mcreator.minepiece.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.mcreator.minepiece.procedures.GoroAmaru;
import net.minecraft.client.*;
import org.slf4j.LoggerFactory;

/** Targeted two-client scenario; consumes real key queue, never desktop input. */
public final class AmaruVisualLab {
   private static int tick;private static boolean ready;private static int receivedBefore;
   private static int actorClock=-1;
   private static final java.util.UUID ACTOR=java.util.UUID.nameUUIDFromBytes("OfflinePlayer:FruitLab".getBytes(java.nio.charset.StandardCharsets.UTF_8));
   public static void register(){
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{
         if(p.scene().equals("ready"))ready=true;
         if(p.scene().startsWith("amaru-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));
      }));
      ClientTickEvents.END_CLIENT_TICK.register(AmaruVisualLab::tick);
   }
   private static void check(String id,boolean ok,String detail){LoggerFactory.getLogger("AmaruVisual").info("[amaru-client-check] {} {} {}",id,ok?"PASS":"FAIL",detail);}
   private static void capture(Minecraft c,String name){Screenshot.grab(c.gameDirectory,name,c.gameRenderer.mainRenderTarget(),1,m->{});}
   private static void step(Minecraft c,String s){c.player.connection.sendCommand("damarulab "+s);}
   private static void cast(){ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());}
   private static void release(){ClientPlayNetworking.send(new DevilFruitNetworking.SkillReleasePayload());}
   private static void tick(Minecraft c){
      if(!ready || c.player==null || c.level==null || c.gui.screen()!=null)return;
      boolean observer=c.player.getName().getString().equals("FruitWitness");
      if(observer && tick>actorClock)return;
      if(!observer && tick%5==0)step(c,"clock_"+tick);
      var actor=c.level.players().stream().filter(p->p.getName().getString().equals("FruitLab")).findFirst().orElse(null);
      String stage=tick<130?"form":tick<190?"revert":tick<245?"sango":tick<315?"elthor":tick<380?"raigo":tick<420?"suppression":tick<465?"ceiling":"restore";
      if(tick>=550 && tick<630)stage="retrack";
      AmaruRenderProbe.stage=stage;
      if(!observer){
         if(tick==0)step(c,"setup");
         if(tick==10 || tick==130 || tick==160 || tick==430 || tick==470 || tick==630 || tick==660)DevilFruitClientInit.clickTransformForLab();
         if(tick==45)c.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
         if(tick==80)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         if(tick==81 || tick==85)c.options.keyJump.setDown(true);
         if(tick==83 || tick==95)c.options.keyJump.setDown(false);
         if(tick==98)check("native-flight",c.player.getAbilities().flying && c.player.getY()>100.2,"native double jump, y="+c.player.getY());
         if(tick==100){double distance=c.gameRenderer.mainCamera().position().distanceTo(c.player.getEyePosition());check("camera-distance",distance>9.5 && distance<10.5,"actual third person distance="+distance);}
         if(tick==105)c.options.setCameraType(CameraType.FIRST_PERSON);
         if(tick==185)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         if(tick==190)step(c,"sango");if(tick==195)cast();if(tick==210)release();
         if(tick==250)step(c,"elthor");if(tick==260)cast();if(tick==275)release();
         if(tick==320)step(c,"raigo");if(tick==330)cast();
         if(tick==380)step(c,"suppress");if(tick==410)step(c,"ceiling");if(tick==460)step(c,"open");
         if(tick==510)step(c,"witnessout");if(tick==550)step(c,"witnessback");
      }
      if(observer && tick==505)receivedBefore=TransformClientState.received(ACTOR);
      if(observer && tick==540)check("retrack-absent",actor==null,"actor unloaded when witness >300 blocks away");
      if(observer && tick==590)check("retrack-restored",actor!=null && TransformClientState.received(ACTOR)>receivedBefore && TransformClientState.get(ACTOR).value()==1 && Math.abs(actor.getBbHeight()-6.5)<.01,"new S2C state, actor UUID and actual dimensions after return");
      if(tick==65 && actor!=null){
         var f=TransformClientState.get(actor.getUUID());
         check("uuid-form",f.value()==1 && f.fruit().equals(GoroAmaru.FRUIT),"actor="+actor.getUUID());
         check("dimensions",Math.abs(actor.getBbWidth()-2.4)<.01 && Math.abs(actor.getBbHeight()-6.5)<.01 && Math.abs(actor.getEyeHeight()-5.5)<.01,"width="+actor.getBbWidth()+" height="+actor.getBbHeight()+" eye="+actor.getEyeHeight());
         check("mesh-form",AmaruRenderProbe.seen("form"),AmaruRenderProbe.evidence("form"));
      }
      if((tick==150 || tick==400 || tick==450 || tick==650) && actor!=null){
         check(stage+"-base",TransformClientState.get(actor.getUUID()).value()==0 && Math.abs(actor.getBbHeight()-1.8)<.01 && Math.abs(actor.getEyeHeight()-1.62)<.01,"actual reverted body and received state");
      }
      if(tick==240 || tick==310 || tick==375)check(stage+"-mesh-animated",AmaruRenderProbe.animated(stage),AmaruRenderProbe.evidence(stage));
      if(tick==600)check("mesh-restored",AmaruRenderProbe.seen("retrack"),AmaruRenderProbe.evidence("retrack"));
      if(java.util.Set.of(60,90,115,150,230,290,350,365,400,450,600,650).contains(tick))capture(c,(observer?"observer-":"actor-")+String.format("%03d",tick)+"-"+stage+".png");
      if(tick==(observer?700:720)){c.options.keyJump.setDown(false);c.stop();return;}tick++;
   }
}
