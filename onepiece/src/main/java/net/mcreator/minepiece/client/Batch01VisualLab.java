package net.mcreator.minepiece.client;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;
import net.minecraft.world.entity.LivingEntity;
import net.mcreator.minepiece.init.MinepieceModItems;

/** One grouped two-client sequence, five developed fruits, actor-clock synchronized observers. */
public final class Batch01VisualLab {
 public static final String[] FRUITS={"hie_hie","pika_pika","moku_moku","suna_suna","yami_yami"};
 public static final String[][] MOVES={{"icepartisan","pheasantbeak","icespikes","iceball","iceage"},{"laser1","laser2","kick","yatanokagami","magatama","amanomurakumo","speedispower","passive"},{"whiteblow","whitevine","whitelauncher","whiteout"},{"desertspada","barjan","sables","desertlaspada","grounddeath"},{"kurouzu","blackhole","darkball","darkmatter"}};
 private static final List<String> fruits=new ArrayList<>(),ids=new ArrayList<>();
 private static int tick,actorClock=-1;private static boolean ready;private static net.minecraft.world.phys.Vec3 start;private static float swordBefore;
 private static final Set<String> RENDERED=new HashSet<>();
 private static final Set<String> CAMERA_SEEN=new HashSet<>();
 public static void cameraMesh(String profile,boolean hidden,double distance){
  if(!Boolean.getBoolean("opworldlike.visualCapture")||!"batch01".equals(System.getProperty("opworldlike.visualScenario"))||!Set.of("darkball","whiteblow").contains(profile))return;
  if(CAMERA_SEEN.add(profile+":"+hidden))org.slf4j.LoggerFactory.getLogger("Batch01Visual").info("[batch01-camera] {} {} lensDistance={}",profile,hidden?"hidden":"visible",distance);
 }
 public static void rendered(String id){if(!"batch01".equals(System.getProperty("opworldlike.visualScenario")))return;RENDERED.add(id);}
 public static void register(){for(int i=0;i<FRUITS.length;i++)if(System.getProperty("opworldlike.batchRetake", "").isEmpty() || java.util.Arrays.asList(System.getProperty("opworldlike.batchRetake").split(",")).contains(FRUITS[i]))for(String id:MOVES[i]){fruits.add(FRUITS[i]);ids.add(id);}
  ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));}));ClientTickEvents.END_CLIENT_TICK.register(Batch01VisualLab::tick);
 }
 private static void check(String id,boolean pass,String detail){org.slf4j.LoggerFactory.getLogger("Batch01Visual").info("[batch01-client-check] {} {} {}",id,pass?"PASS":"FAIL",detail);}
 private static void capture(Minecraft c,String name){Screenshot.grab(c.gameDirectory,name,c.gameRenderer.mainRenderTarget(),1,m->{});}
 private static void tick(Minecraft c){if(!ready || c.player==null || c.level==null || c.gui.screen()!=null)return;boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer && tick>actorClock)return;
  if(!observer && tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);
  int index=tick/120,offset=tick%120;if(index>=ids.size()){if(!observer && tick==ids.size()*120)c.player.connection.sendCommand("dbatchlive");if(tick>=ids.size()*120+30)c.stop();else tick++;return;}String id=ids.get(index);String key=fruits.get(index)+"/"+id;if(offset==0)RENDERED.clear();
  int windup=net.mcreator.minepiece.procedures.ElementalBatchTechniques.windup(id);if(id.equals("laser2"))windup=20;
  if(!observer){if(offset==0){c.options.setCameraType(CameraType.FIRST_PERSON);c.player.connection.sendCommand("dvisual "+fruits.get(index)+" "+id);}if(offset==18)start=c.player.position();if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());if(offset==50)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
   if(offset==65 && id.equals("amanomurakumo")){var target=target(c);swordBefore=target==null?0:target.getHealth();if(target!=null && c.gameMode!=null)c.gameMode.attack(c.player,target);}
   if(offset==80 && id.equals("amanomurakumo")){var target=target(c);check("native-sword",c.player.getMainHandItem().is(MinepieceModItems.AMANO_MURAKUMO) && target!=null && swordBefore-target.getHealth()>=7,"actual native attack damage="+(target==null?0:swordBefore-target.getHealth()));}
   if(offset==90 && Set.of("kick","yatanokagami","speedispower","passive","whitelauncher","barjan").contains(id))check(id+"-movement",c.player.position().distanceTo(start)>2,"native distance="+c.player.position().distanceTo(start));
  }
  if(offset==95){String expected=switch(id){case "icepartisan","pheasantbeak","whiteblow","whitevine","desertspada","desertlaspada","darkball","darkmatter","laser1","laser2","magatama"->id;case "icespikes"->"b01_ice_spike";case "iceball"->"b01_ice_ball";case "iceage"->"b01_ice_wave";case "grounddeath"->"b01_sand_wave";case "whiteout"->"b01_smoke_wave";case "sables"->"sables-native";case "kurouzu"->"b01_dark_grab";case "blackhole"->"b01_dark_field";case "amanomurakumo"->"b01_light_sword";case "whitelauncher"->"b01_smoke_finish";case "barjan"->"b01_sand_slash";case "kick","speedispower"->"b01_light_kick";default->"b01_light_dash";};check(key+"-render",RENDERED.contains(expected),"submitted world geometry="+expected);}
  if(offset==28 || offset==22+windup || offset==30+windup || offset==100)capture(c,(observer?"observer-":"actor-")+String.format("%02d",index)+"-"+id+"-"+offset+".png");
  tick++;
 }
 private static LivingEntity target(Minecraft c){for(var e:c.level.entitiesForRendering())if(e instanceof LivingEntity h && h.getType()==net.minecraft.world.entity.EntityTypes.HUSK && h.distanceTo(c.player)<3.5)return h;return null;}
}
