package net.mcreator.minepiece.client;

import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;

/** Opt-in 10-minute live recipe, exclusively in the disposable visual laboratory. */
public final class PrioritySoakLab {
 private static final String[][] CASES={{"goro_goro","sango"},{"tori_tori_phoenix","attackspeed"},{"hito_hito_daibutsu","attackoverhead"},{"inu_inu_wolf","shigan"},{"neko_neko_leopard","shigan"},{"gomu_gomu","gomu_gatling"},{"gura_gura","kaishin"},{"ope_ope","room"},{"mochi_mochi","musodonatsu"},{"nikyu_nikyu","ursusshock"}};
 private static boolean ready;private static int tick,actorClock=-1,checks;private static long started;
 private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("PrioritySoak");
 public static void register(){ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));}));ClientTickEvents.END_CLIENT_TICK.register(PrioritySoakLab::tick);}
 private static void check(String id,boolean pass,String detail){checks++;LOG.info("[priority-soak-check] {} {} {}",id,pass?"PASS":"FAIL",detail);}
 private static void tick(Minecraft c){
  if(!ready||c.player==null||c.level==null||c.gui.screen()!=null)return;
  boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer&&tick>actorClock)return;
  if(tick==0){started=System.nanoTime();LOG.info("[priority-soak] BEGIN ticks=12000 cases=10 rounds=5 role={}",observer?"observer":"actor");}
  if(!observer&&tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);
  if(tick>=12000){if(!observer)c.player.connection.sendCommand("dbatchclock 12100");LOG.info("[priority-soak] END ticks={} checks={} elapsed_seconds={}",tick,checks,(System.nanoTime()-started)/1e9);c.stop();return;}
  int slot=tick/240,index=slot%10,phase=tick%240,round=slot/10;String fruit=CASES[index][0],move=CASES[index][1];
  if(!observer){
   if(phase==0){c.player.connection.sendCommand("dvisual "+fruit+" "+move);c.options.setCameraType(round%2==0?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);}
   if(index<5&&(phase==10||index>0&&phase==22))ClientPlayNetworking.send(new DevilFruitNetworking.TransformPayload());
   if(phase==38)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
   if(phase==180)c.player.connection.sendCommand("dvisual "+fruit+" forms");
  }
  if(phase==160){var actor=c.level.players().stream().filter(p->p.getName().getString().equals("FruitLab")).findFirst();check(slot+"/actor-tracked",actor.isPresent(),"round="+round+" fruit="+fruit);}
  if(phase==225){var actor=c.level.players().stream().filter(p->p.getName().getString().equals("FruitLab")).findFirst();check(slot+"/reset-form",actor.isPresent()&&TransformClientState.get(actor.get().getUUID()).value()==0&&Math.abs(actor.get().getScale()-1)<.001,"fruit="+fruit);}
  if(tick%200==0){int effects=0;for(var e:c.level.entitiesForRendering())if(e instanceof net.mcreator.minepiece.entity.EntityGomuArmEntity||e instanceof net.mcreator.minepiece.entity.SkillProjectileEntity)effects++;var runtime=Runtime.getRuntime();LOG.info("[priority-soak-sample] tick={} heap_mib={} effects={} rig_cache={}",tick,(runtime.totalMemory()-runtime.freeMemory())/1048576,effects,RigClientState.trackedEntries());check(tick+"/bounded-effects",effects<=96,"actual client anchors="+effects);check(tick+"/bounded-rig-cache",RigClientState.trackedEntries()<=6,"2 players, at most 3 maps each");}
  if(phase==70&&(round==0||round==4))Screenshot.grab(c.gameDirectory,(observer?"observer":"actor")+"-soak-"+slot+"-"+fruit+".png",c.gameRenderer.mainRenderTarget(),1,m->{});
  tick++;
 }
}
