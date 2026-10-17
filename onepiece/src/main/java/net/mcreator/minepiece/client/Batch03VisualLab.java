package net.mcreator.minepiece.client;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;
/** All five fruits are developed and server-verified before running this grouped sequence. */
public final class Batch03VisualLab {
 public static final String[] FRUITS={"mochi_mochi","mori_mori","kira_kira","supa_supa","riki_riki"};
 public static final String[][] MOVES={{"mochipistol","mochigatling","yakimochi","zangirimochi","musodonatsu","mochibuild"},{"root","rootgiant","assault","rootsurround"},{"attackoverhead","brilliantpunk","diamondshard","diamondavalanche","diamondbody"},{"projectileslash","atomicspurt","sparklingdaisy","sparbreak"},{"attackoverhead","hadoelbow","galleonlariat","stonethrowing","stonethrowinglarge"}};
 private static final List<String> fruits=new ArrayList<>(),ids=new ArrayList<>();private static final Set<String> RENDERED=new HashSet<>();private static boolean ready,witnessReturned,absenceSeen,restoreSeen;private static int tick,actorClock=-1,receivedBefore;private static net.minecraft.world.phys.Vec3 start;
 public static void rendered(String id){if("batch03".equals(System.getProperty("opworldlike.visualScenario")))RENDERED.add(id);}
 public static void register(){for(int i=0;i<FRUITS.length;i++)if(System.getProperty("opworldlike.batchRetake","").isEmpty() || Arrays.asList(System.getProperty("opworldlike.batchRetake").split(",")).contains(FRUITS[i]))for(String m:MOVES[i]){fruits.add(FRUITS[i]);ids.add(m);}
  ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().equals("material-returned"))witnessReturned=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));}));ClientTickEvents.END_CLIENT_TICK.register(Batch03VisualLab::tick);
 }
 private static void check(String id,boolean ok,String detail){org.slf4j.LoggerFactory.getLogger("Batch03Visual").info("[batch03-client-check] {} {} {}",id,ok?"PASS":"FAIL",detail);}
 private static String expected(String fruit,String id){String prefix="b03_"+fruit.split("_")[0]+"_";return switch(id){case "mochipistol","mochigatling"->"b03_native_"+id;case "musodonatsu"->"b03_mochi_donut";case "mochibuild"->"b03_mochi_build";case "rootsurround"->"b03_mori_cage";case "diamondbody"->"b03_diamond_coat";case "galleonlariat"->"b03_riki_sweep";default->prefix+id;};}
 private static int duration(String id){return id.equals("musodonatsu")?240:id.equals("diamondbody")?280:140;}
 private static void tick(Minecraft c){if(!ready || c.player==null || c.level==null || c.gui.screen()!=null)return;boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer && tick>actorClock)return;
  if(!observer && tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);
  int index=0,offset=tick;while(index<ids.size() && offset>=duration(ids.get(index))){offset-=duration(ids.get(index));index++;}
  if(index>=ids.size()){if(offset>=30)c.stop();else tick++;return;}String id=ids.get(index),fruit=fruits.get(index),key=fruit+"/"+id;int windup=net.mcreator.minepiece.procedures.MaterialBatchTechniques.windup(id);
  if(offset==0){RENDERED.clear();witnessReturned=absenceSeen=restoreSeen=false;}
  if(!observer && id.equals("diamondbody") && offset==100 && !witnessReturned)return;
  if(!observer){
   if(offset==0){c.options.setCameraType(CameraType.FIRST_PERSON);c.player.connection.sendCommand("dvisual "+fruit+" "+id);}
   if(offset==18)start=c.player.position();if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
   if(id.equals("diamondbody")){if(offset==55)c.player.connection.sendCommand("dmaterialstep away");if(offset==170)c.player.connection.sendCommand("dmaterialstep damage");}
   if(offset==50)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
   if(id.equals("musodonatsu")){if(offset==170){ClientPlayNetworking.send(new DevilFruitNetworking.CycleTechniquePayload());ClientPlayNetworking.send(new DevilFruitNetworking.CycleTechniquePayload());}if(offset==180)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());}
   if(offset==110 && Set.of("zangirimochi","brilliantpunk","atomicspurt","galleonlariat").contains(id))check(key+"-movement",c.player.position().distanceTo(start)>2 && c.player.position().distanceTo(start)<8,"native distance="+c.player.position().distanceTo(start));
  }
  var actor=c.level.players().stream().filter(p->p.getName().getString().equals("FruitLab")).findFirst().orElse(null);
  if(id.equals("diamondbody")){
   var actorId=UUID.nameUUIDFromBytes("OfflinePlayer:FruitLab".getBytes(java.nio.charset.StandardCharsets.UTF_8));
   if(observer && offset==50)receivedBefore=MaterialClientState.received(actorId);
   if(observer && offset>55 && !absenceSeen && actor==null){absenceSeen=true;check("diamond-retrack-absent",true,"actor actually unloaded; witness requests return only after observing removal");c.player.connection.sendCommand("dmaterialreturn");}
   if(observer && absenceSeen && !restoreSeen && actor!=null && MaterialClientState.received(actorId)>receivedBefore && MaterialClientState.get(actorId)==1){restoreSeen=true;check("diamond-retrack-restored",MaterialClientState.get(c.player.getUUID())==0,"fresh START_TRACKING state for actor UUID only after actual removal");}
   if(observer && offset==260){if(!absenceSeen)check("diamond-retrack-absent",false,"actual removal never observed");if(!restoreSeen)check("diamond-retrack-restored",false,"fresh state missing after return");}
   if(offset==250)RENDERED.clear();
   if(offset==260)check("diamond-expired",MaterialClientState.get(actorId)==0 && !RENDERED.contains("b03_diamond_coat"),"expired flag and no actual coat submission in following frames");
  }
  if(fruit.equals("supa_supa") && offset==120 && actor!=null)check(key+"-coat-expired",MaterialClientState.get(actor.getUUID())==0,"finite blade coat retracted on both clients");
  if(offset== (id.equals("musodonatsu")?230:130)){
   check(key+"-render",RENDERED.contains(expected(fruit,id)),"submitted="+expected(fruit,id));
   if(id.equals("musodonatsu"))check(key+"-reactive",RENDERED.contains("b03_mochi_ringfist"),"real follow-up gesture created a separate ring fist");
   if(fruit.equals("supa_supa"))check(key+"-coat",RENDERED.contains("b03_blades_coat"),"actual native Supa models submitted independently of Zoan form");
  }
  int third=30+windup;boolean shot=offset==28 || offset==22+windup || offset==third || offset==(id.equals("musodonatsu")?198:id.equals("diamondbody")?130:88) || offset==(id.equals("musodonatsu")?230:id.equals("diamondbody")?260:130);
  if(shot)Screenshot.grab(c.gameDirectory,(observer?"observer-":"actor-")+String.format("%02d",index)+"-"+fruit.split("_")[0]+"-"+id+"-"+offset+".png",c.gameRenderer.mainRenderTarget(),1,m->{});
  tick++;
 }
}
