package net.mcreator.minepiece.client;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;

/** One grouped, two-client capture after all five server suites; no desktop input injection. */
public final class Batch04VisualLab {
 public static final String[] FRUITS={"aro_aro","horo_horo","mero_mero","kiro_kiro","bisu_bisu"};
 public static final String[][] MOVES={{"arrow","move","combo","passive"},{"negativehollow","minihollow","tokuhoro","rap","kumac"},{"pistolkiss","roundhousekick","rotatekick","perfumefemur"},{"attackoverhead","attackjump","kirooverhead","weightmanipulation"},{"crashpretzel","biscuitwarrior","biscuitwarrior2","biscuit"}};
 private static final List<String> fruits=new ArrayList<>(),ids=new ArrayList<>();private static final Set<String> RENDERED=new HashSet<>();private static boolean ready;private static int tick,actorClock=-1,stoneSubmits,stoneBeforeExpiry;private static net.minecraft.world.phys.Vec3 start;
 public static void rendered(String id){if("batch04".equals(System.getProperty("opworldlike.visualScenario"))){RENDERED.add(id);if(id.equals("b04_stone_statue"))stoneSubmits++;}}
 public static void register(){for(int f=0;f<FRUITS.length;f++)if(System.getProperty("opworldlike.batchRetake","").isEmpty() || Arrays.asList(System.getProperty("opworldlike.batchRetake").split(",")).contains(FRUITS[f]))for(String id:MOVES[f]){fruits.add(FRUITS[f]);ids.add(id);}
  ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));}));ClientTickEvents.END_CLIENT_TICK.register(Batch04VisualLab::tick);
 }
 private static void check(String id,boolean ok,String detail){org.slf4j.LoggerFactory.getLogger("Batch04Visual").info("[batch04-client-check] {} {} {}",id,ok?"PASS":"FAIL",detail);}
 private static int duration(String id){return id.equals("minihollow") || id.equals("rap")?220:160;}
 private static String expected(String f,String id){return Set.of("kumac","biscuitwarrior","biscuitwarrior2").contains(id)?"summon-"+(id.equals("kumac")?"kumac":"biscuit_warrrior"):id.equals("attackjump") || id.equals("kirooverhead")?"b04_kiro_impact":"b04_"+f.split("_")[0]+"_"+id;}
 private static void tick(Minecraft c){if(!ready || c.player==null || c.level==null || c.gui.screen()!=null)return;boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer && tick>actorClock)return;
  if(!observer && tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);
  int index=0,offset=tick;while(index<ids.size() && offset>=duration(ids.get(index))){offset-=duration(ids.get(index));index++;}if(index>=ids.size()){if(offset>=30)c.stop();else tick++;return;}
  String id=ids.get(index),fruit=fruits.get(index),key=fruit+"/"+id;int windup=net.mcreator.minepiece.procedures.ParameciaBatchTechniques.windup(id);
  if(offset==0)RENDERED.clear();
  if(!observer){
   if(offset==0){c.options.setCameraType(CameraType.FIRST_PERSON);c.player.connection.sendCommand("dvisual "+fruit+" "+(id.equals("rap")?"minihollow":id));}
   if(offset==18)start=c.player.position();if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
   if(offset==50)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
   if((id.equals("minihollow") || id.equals("rap")) && offset==130){ClientPlayNetworking.send(new DevilFruitNetworking.CycleTechniquePayload());ClientPlayNetworking.send(new DevilFruitNetworking.CycleTechniquePayload());}
   if((id.equals("minihollow") || id.equals("rap")) && offset==140)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
   if(offset==135 && fruit.equals("aro_aro") && Set.of("move","passive").contains(id))check(key+"-movement",c.player.position().distanceTo(start)>2 && c.player.position().distanceTo(start)<=5.2,"distance="+c.player.position().distanceTo(start));
  }
  if(fruit.equals("mero_mero") && Set.of("pistolkiss","perfumefemur").contains(id)){
   if(offset==135)stoneBeforeExpiry=stoneSubmits;
   if(offset==145){boolean any=false;for(var entity:c.level.entitiesForRendering())if(entity instanceof net.mcreator.minepiece.procedures.ParameciaStoneData state && state.minepiece$stoneFlag())any=true;check(key+"-statue-expired",!any && stoneSubmits==stoneBeforeExpiry,"no native stone flag and no statue renderer submissions after expiry");}
  }
  if(offset==duration(id)-15){check(key+"-render",RENDERED.contains(expected(fruit,id)),"submitted="+expected(fruit,id));if(id.equals("minihollow") || id.equals("rap"))check(key+"-detonation",RENDERED.contains("b04_horo_burst") && RENDERED.contains("b04_horo_rap"),"parked native ghosts then separate Rap burst");if(fruit.equals("mero_mero") && Set.of("pistolkiss","perfumefemur").contains(id))check(key+"-statue",RENDERED.contains("b04_stone_statue"),"actual native victim renderer used stone texture");}
  boolean shot=offset==28 || offset==22+windup || offset==40+windup || offset==(duration(id)>160?155:88) || offset==duration(id)-15;
  if(shot)Screenshot.grab(c.gameDirectory,(observer?"observer-":"actor-")+String.format("%02d",index)+"-"+fruit.split("_")[0]+"-"+id+"-"+offset+".png",c.gameRenderer.mainRenderTarget(),1,m->{});
  tick++;
 }
}
