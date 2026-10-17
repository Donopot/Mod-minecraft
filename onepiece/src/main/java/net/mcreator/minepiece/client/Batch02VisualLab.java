package net.mcreator.minepiece.client;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;
/** Five fruits already developed/server-verified; one synchronized graphical sequence. */
public final class Batch02VisualLab {
 public static final String[] FRUITS={"gura_gura","bomu_bomu","zushi_zushi","nikyu_nikyu","ope_ope"};
 public static final String[][] MOVES={{"attacksweep","overheadsuper","gekishin","kabutowari","tenchimeido","kaishin"},{"nosefancycannon","kickbomb","punchbomb","bomboverhead","zenshinkibaku"},{"attackspeed","gravito","meteorsmall","moko","rubble","meteor"},{"padho","ursusshock","assault","kumapunch","passive"},{"stoneopeope","room","shambles","amputation"}};
 private static final List<String> fruits=new ArrayList<>(),ids=new ArrayList<>();private static final Set<String> RENDERED=new HashSet<>();
 private static int tick,actorClock=-1;private static boolean ready;private static net.minecraft.world.phys.Vec3 start;private static double peak;
 public static void rendered(String id){if("batch02".equals(System.getProperty("opworldlike.visualScenario")))RENDERED.add(id);}
 public static void register(){for(int i=0;i<FRUITS.length;i++)if(System.getProperty("opworldlike.batchRetake","").isEmpty() || Arrays.asList(System.getProperty("opworldlike.batchRetake").split(",")).contains(FRUITS[i]))for(String id:MOVES[i]){fruits.add(FRUITS[i]);ids.add(id);}
  ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));}));ClientTickEvents.END_CLIENT_TICK.register(Batch02VisualLab::tick);
 }
 private static void check(String id,boolean pass,String detail){org.slf4j.LoggerFactory.getLogger("Batch02Visual").info("[batch02-client-check] {} {} {}",id,pass?"PASS":"FAIL",detail);}
 private static String expected(String id){return switch(id){case "attacksweep"->"b02_quake_sweep";case "overheadsuper"->"b02_quake_overhead";case "gekishin"->"b02_quake_front";case "kabutowari"->"b02_air_crack";case "tenchimeido"->"b02_tilt_reverse";case "kaishin"->"b02_quake_radial";case "kickbomb","punchbomb","bomboverhead","zenshinkibaku"->"b02_bomb_blast";case "attackspeed","moko"->"b02_gravity_horizontal";case "gravito"->"b02_gravity_field";case "assault","kumapunch"->"b02_paw_impact";case "passive"->"b02_paw_travel";case "shambles"->"b02_ope_swap";case "amputation"->"b02_ope_cut";default->"b02_"+id;};}
 private static void tick(Minecraft c){if(!ready || c.player==null || c.level==null || c.gui.screen()!=null)return;boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer && tick>actorClock)return;
  if(!observer && tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);
  int index=tick/120,offset=tick%120;if(index>=ids.size()){if(tick>=ids.size()*120+30)c.stop();else tick++;return;}String id=ids.get(index),key=fruits.get(index)+"/"+id;
  if(offset==0)RENDERED.clear();int windup=net.mcreator.minepiece.procedures.ControlledBatchTechniques.windup(id);
  if(!observer){
   if(offset==0){c.options.setCameraType(CameraType.FIRST_PERSON);c.player.connection.sendCommand("dvisual "+fruits.get(index)+" "+id);}
   if(offset==18){start=c.player.position();peak=start.y;}
   if(offset>=20 && offset<95)peak=Math.max(peak,c.player.getY());
   if(offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
   if(offset==50)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
   if(offset==95 && Set.of("kickbomb","attackspeed","assault","kumapunch","passive","shambles").contains(id))check(id+"-movement",c.player.position().distanceTo(start)>2,"native distance="+c.player.position().distanceTo(start));
   if(offset==95 && Set.of("bomboverhead","overheadsuper").contains(id))check(id+"-leap",peak-start.y>.5 && c.player.getY()<start.y+.5,"native peak delta="+(peak-start.y)+" landed y="+c.player.getY());
  }
  if(offset==105)check(key+"-render",RENDERED.contains(expected(id)),"actual geometry submitted="+expected(id));
  int third=Set.of("meteor","meteorsmall").contains(id)?20+windup+16:id.equals("ursusshock")?20+windup+13:30+windup;
  if(offset==28 || offset==22+windup || offset==third || offset==88 || offset==110)Screenshot.grab(c.gameDirectory,(observer?"observer-":"actor-")+String.format("%02d",index)+"-"+id+"-"+offset+".png",c.gameRenderer.mainRenderTarget(),1,m->{});
  tick++;
 }
}
