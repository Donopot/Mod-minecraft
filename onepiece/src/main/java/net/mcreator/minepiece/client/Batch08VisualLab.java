package net.mcreator.minepiece.client;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.*;
/** Grouped B08 evidence, including actual meshes in both hybrid and complete animal form. */
public final class Batch08VisualLab {
 public static final String[] FRUITS={"ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus"},NAMES={"brachio","pachy"};
 public static final String[][] MOVES={{"attackoverhead","brachiobober","blackcofee","brachiolauncher","sparkingqueen"},{"attackjump","attacksweep","headbutt","ulzugan","ulmeteor"}};
 private static final List<String> fruits=new ArrayList<>(),ids=new ArrayList<>(),names=new ArrayList<>();private static final Set<String> RENDERED=new HashSet<>();private static final Map<String,Set<String>> POSES=new HashMap<>();private static boolean ready;private static int tick,actorClock=-1,witnessClock=-1;private static double flightBase;
 public static void rendered(String id){if("batch08".equals(System.getProperty("opworldlike.visualScenario")))RENDERED.add(id);}
 public static void beastPose(int kind,float head,float right,float left){if("batch08".equals(System.getProperty("opworldlike.visualScenario")))POSES.computeIfAbsent("beast-"+kind,k->new HashSet<>()).add(Math.round(head*100)+":"+Math.round(right*100)+":"+Math.round(left*100));}
 public static void register(){for(int f=0;f<FRUITS.length;f++)if(System.getProperty("opworldlike.batchRetake","").isEmpty()||Arrays.asList(System.getProperty("opworldlike.batchRetake").split(",")).contains(FRUITS[f])){if(!Boolean.getBoolean("opworldlike.formsOnly"))for(String id:MOVES[f]){fruits.add(FRUITS[f]);ids.add(id);names.add(NAMES[f]);}fruits.add(FRUITS[f]);ids.add("forms");names.add(NAMES[f]);}ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.LAB_SCENE,(p,c)->c.client().execute(()->{if(p.scene().equals("ready"))ready=true;if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));if(p.scene().startsWith("witness-clock:"))witnessClock=Integer.parseInt(p.scene().substring(14));}));ClientTickEvents.END_CLIENT_TICK.register(Batch08VisualLab::tick);}
 private static void check(String id,boolean ok,String detail){org.slf4j.LoggerFactory.getLogger("Batch08Visual").info("[batch08-client-check] {} {} {}",id,ok?"PASS":"FAIL",detail);}
 private static int duration(String id){return id.equals("forms")?320:150;}
 private static String expected(String f,String id){return "b08_"+NAMES[Arrays.asList(FRUITS).indexOf(f)]+"_"+id;}
 private static void tick(Minecraft c){if(!ready||c.player==null||c.level==null||c.gui.screen()!=null)return;boolean observer=c.player.getName().getString().equals("FruitWitness");if(observer&&tick>actorClock)return;if(!observer&&tick>witnessClock+10)return;if(observer&&tick%5==0)c.player.connection.sendCommand("dbatchwitness "+tick);if(!observer&&tick%5==0)c.player.connection.sendCommand("dbatchclock "+tick);int index=0,offset=tick;while(index<ids.size()&&offset>=duration(ids.get(index))){offset-=duration(ids.get(index));index++;}if(index>=ids.size()){if(!observer&&tick%5==0)c.player.connection.sendCommand("dbatchclock "+(tick+100));if(offset>=30)c.stop();else tick++;return;}String id=ids.get(index),fruit=fruits.get(index),key=fruit+"/"+id;
  if(offset==0){RENDERED.clear();POSES.clear();}
  if(!observer){if(offset==0){c.options.setCameraType(CameraType.FIRST_PERSON);c.player.connection.sendCommand("dvisual "+fruit+" "+(id.equals("forms")?"forms":id));}if(!id.equals("forms")&&offset==20)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());if(!id.equals("forms")&&offset==60)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
   if(id.equals("forms")){if(offset==10)c.options.setCameraType(CameraType.THIRD_PERSON_FRONT);if(offset==20||offset==120||offset==260)ClientPlayNetworking.send(new DevilFruitNetworking.TransformPayload());if(offset==45||offset==175)ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());if(offset==190)c.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
  }
  if(id.equals("forms")){
   if(offset==175){var p=c.level.players().stream().filter(e->e.getName().getString().equals("FruitLab")).findFirst();flightBase=p.isPresent()?p.get().getY():100;}
   int f=Arrays.asList(FRUITS).indexOf(fruit);if(offset==100)check(key+"-hybrid",RENDERED.contains("hybrid-minepiece:item_devil_fruit_"+fruit),"actual hybrid layers submitted");if(offset==235){var subject=c.level.players().stream().filter(e->e.getName().getString().equals("FruitLab")).findFirst();var b=subject.isPresent()?net.mcreator.minepiece.procedures.ZoanForms.body(subject.get()):null;boolean dimensions=b!=null&&Math.abs(subject.get().getBbWidth()-b.width())<.01&&Math.abs(subject.get().getBbHeight()-b.height())<.01&&Math.abs(subject.get().getEyeHeight()-b.eye())<.01;check(key+"-beast",RENDERED.contains("beast-minepiece:item_devil_fruit_"+fruit)&&POSES.getOrDefault("beast-"+f,Set.of()).size()>1&&dimensions,"actual beast mesh, animated joints and native client width/height/eye; poses="+POSES.getOrDefault("beast-"+f,Set.of()).size()+" dimensions="+dimensions);}
   if(offset==300){var p=c.level.players().stream().filter(e->e.getName().getString().equals("FruitLab")).findFirst();check(key+"-base",p.isPresent()&&TransformClientState.get(p.get().getUUID()).value()==0&&Math.abs(p.get().getScale()-1)<.001,"native synchronized base restored");}
  }else if(offset==135)check(key+"-render",RENDERED.contains(expected(fruit,id)),"actual submitted="+expected(fruit,id));
  boolean shot=id.equals("forms")?(offset==38||offset==65||offset==135||offset==165||offset==225||offset==305):(offset==23||offset==32||offset==47||offset==78||offset==135);
  if(shot)Screenshot.grab(c.gameDirectory,(observer?"observer-":"actor-")+String.format("%02d",index)+"-"+names.get(index)+"-"+id+"-"+offset+".png",c.gameRenderer.mainRenderTarget(),1,m->{});tick++;
 }
}
