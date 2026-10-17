package net.mcreator.minepiece.client;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;
/** Grouped five-fruit capture. Real renderer/PAL evaluations, never packet-only art claims. */
public final class Batch05VisualLab {
 public static final String[] FRUITS={"kage_kage","soru_soru","uta_uta","imu","gomu_gomu"};
 public static final String[][] MOVES={{"attackoverhead","brickbat","tsunotokage","doppelman","islandshatterpunch","shadowasgard"},{"attackoverhead","attacksweep","raitei","hebunrifire","furugora","mamaraid","ikoku"},{"noteattack","notelaser","posing","noteshield","notewarrior","totmusicapunch","summontotmusica"},{"imuarrow","silentcommand","sovereigngrasp"},{"gomu_pistol","gomu_bazooka","gomu_gatling","gear_change"}};
 private static final List<String> fruits=new ArrayList<>(),ids=new ArrayList<>();private static final Set<String> RENDERED=new HashSet<>();private static final Map<Integer,Set<String>> POSES=new HashMap<>();private static boolean ready;private static int tick,actorClock=-1;
 public static void rendered(String id){if("batch05".equals(System.getProperty("opworldlike.visualScenario")))RENDERED.add(id);}
 public static void gearPose(int gear,int mode,float head,float right,float left,float scale){if(!"batch05".equals(System.getProperty("opworldlike.visualScenario")))return;POSES.computeIfAbsent(gear,g->new HashSet<>()).add(mode+":"+Math.round(head*100)+":"+Math.round(right*100)+":"+Math.round(left*100)+":"+scale);}
 public static void register(){for(int f=0;f<FRUITS.length;f++)if(System.getProperty("opworldlike.batchRetake","").isEmpty()||Arrays.asList(System.getProperty("opworldlike.batchRetake").split(",")).contains(FRUITS[f]))for(String id:MOVES[f]){fruits.add(FRUITS[f]);ids.add(id);}ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));}));ClientTickEvents.END_CLIENT_TICK.register(Batch05VisualLab::tick);}
 private static void check(String id,boolean ok,String detail){org.slf4j.LoggerFactory.getLogger("Batch05Visual").info("[batch05-client-check] {} {} {}",id,ok?"PASS":"FAIL",detail);}
 private static int duration(String id){return id.equals("gear_change")?560:180;}
 private static String expected(String f,String id){return switch(id){case "doppelman"->"summon-gecko_moria";case "raitei"->"summon-zeus";case "hebunrifire"->"summon-prometheus";case "notewarrior"->"summon-note_warrior1";case "summontotmusica"->"summon-tot_musica_1";case "totmusicapunch"->"b05_native_tot_hand";case "imuarrow"->"b05_native_imu_arrow";case "mamaraid"->"b05_soru_impact";case "gomu_pistol","gomu_bazooka","gomu_gatling"->"b05_native_gomu_"+id;case "gear_change"->"gear-2";default->"b05_"+f.split("_")[0]+"_"+id;};}
 private static void tick(Minecraft c){if(!ready||c.player==null||c.level==null||c.gui.screen()!=null)return;boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer&&tick>actorClock)return;if(!observer&&tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);int index=0,offset=tick;while(index<ids.size()&&offset>=duration(ids.get(index))){offset-=duration(ids.get(index));index++;}if(index>=ids.size()){if(offset>=30)c.stop();else tick++;return;}String id=ids.get(index),fruit=fruits.get(index),key=fruit+"/"+id;int windup=net.mcreator.minepiece.procedures.MythicBatchTechniques.windup(id);
  if(offset==0){RENDERED.clear();POSES.clear();}
  if(!observer){if(offset==0){c.options.setCameraType(CameraType.FIRST_PERSON);c.player.connection.sendCommand("dvisual "+fruit+" "+id);}if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());if(offset==50)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);if(id.equals("gear_change")&&offset==70)c.options.setCameraType(CameraType.THIRD_PERSON_FRONT);if(id.equals("gear_change")&&(offset==140||offset==260||offset==380))ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());if(id.equals("gear_change")&&offset==500)ClientPlayNetworking.send(new DevilFruitNetworking.TransformPayload());}
  if(id.equals("gear_change")){
   for(int gear=2;gear<=5;gear++)if(offset==100+(gear-2)*120)check(key+"-form-"+gear,RENDERED.contains("gear-"+gear)&&POSES.getOrDefault(gear,Set.of()).size()>1,"actual body submitted and poses evaluated; signatures="+POSES.getOrDefault(gear,Set.of()).size());
   if(offset==540){var subject=c.level.players().stream().filter(p->p.getName().getString().equals("FruitLab")).findFirst();check(key+"-base",subject.isPresent()&&TransformClientState.get(subject.get().getUUID()).value()==0&&Math.abs(subject.get().getScale()-1)<.001,"native synchronized base and scale restored");}
  }
  if(offset==duration(id)-15){check(key+"-render",RENDERED.contains(expected(fruit,id)),"submitted="+expected(fruit,id));if(id.equals("notewarrior"))check(key+"-second-soldier",RENDERED.contains("summon-note_warrior2"),"both actual native variants");if(id.equals("noteshield"))check(key+"-guard",RENDERED.contains("b05_uta_guard"),"actual guard mesh submitted");}
  boolean shot=id.equals("gear_change")?(offset==50||offset==100||offset==220||offset==340||offset==460||offset==545):(offset==28||offset==22+windup||offset==40+windup||offset==88||offset==165);
  if(shot)Screenshot.grab(c.gameDirectory,(observer?"observer-":"actor-")+String.format("%02d",index)+"-"+fruit.split("_")[0]+"-"+id+"-"+offset+".png",c.gameRenderer.mainRenderTarget(),1,m->{});tick++;
 }
}
