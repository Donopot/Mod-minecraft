package net.mcreator.minepiece.command;

import java.util.*;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.procedures.*;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.init.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

/** Real block states and native casts, in a disposable lab only. No source or visual counters as an oracle. */
public final class TerrainTestCommands {
 private static ServerLevel world;private static ServerPlayer owner;private static int passed,failed,scene,age,before;private static boolean active;
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();
 private static final String[] PREVIOUS={"mera_mera","magu_magu","goro_goro","hie_hie","pika_pika","moku_moku","suna_suna","yami_yami"};
 private static final Vec3 START=new Vec3(510.5,100,80.5);private static Mob victim;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("TerrainTest").info("[dterrain] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 private static Item item(String key){return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minepiece","item_devil_fruit_"+key));}
 private static String key(){return TechniqueTerrain.fruitKey(fruits.get(scene))+"/"+moves.get(scene).id();}
 private static void fruit(Item f){DevilFruitPowerLogic.resetDevilFruitPowers(owner);owner.getInventory().clearContent();var v=MinepieceModVariables.get(owner);v.PlayerDevilFruit=new ItemStack(f);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;owner.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);owner.setNoGravity(true);owner.teleportTo(START.x,START.y,START.z);owner.setYRot(0);owner.setXRot(0);owner.setDeltaMovement(Vec3.ZERO);owner.getCooldowns().removeCooldown(owner.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));}
 private static void arena(){PowerBlockService.releaseAll(owner.getUUID());for(int x=499;x<=531;x++)for(int z=69;z<=116;z++)for(int y=97;y<=112;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),(y<100?Blocks.STONE:Blocks.AIR).defaultBlockState());}
 public static void register(CommandDispatcher<CommandSourceStack> d){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  d.register(Commands.literal("dterrain").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->{if(active)return 0;world=c.getSource().getServer().overworld();owner=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("TerrainAcceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"TerrainAcceptance"));passed=failed=scene=age=0;fruits.clear();moves.clear();for(String f:PREVIOUS)for(var m:DevilFruitRegistry.movesFor(item(f))){fruits.add(item(f));moves.add(m);}fruit(item("mera_mera"));arena();controls();active=true;begin();return 1;}));
  ServerTickEvents.END_SERVER_TICK.register(s->{if(!active)return;try{age++;if(age==12){MeraTechniques.release(owner);MaguTechniques.release(owner);GoroTechniques.release(owner);}if(age==3){boolean immediate=Set.of("kagero","kageromagumagu").contains(moves.get(scene).id());check(key()+"/charge-safe",immediate?TechniqueTerrain.brokenTotal(key())-before<=TechniqueTerrain.policy(fruits.get(scene),moves.get(scene).id()).blocks():TechniqueTerrain.brokenTotal(key())==before,immediate?"source dash is already active, bounded budget":"no permanent change during windup");}if(age==150)finish();if(age<150)return;var rule=TechniqueTerrain.policy(fruits.get(scene),moves.get(scene).id());if(rule!=null && rule.mode()==TechniqueTerrain.Mode.TRANSFORM){if(age<250)return;check(key()+"/native-expiry",PowerBlockService.activeCountFor(owner.getUUID())==0 && permanentSample(),"natural250tick attribution expiry leaves transformed blocks");}if(rule!=null && rule.mode()==TechniqueTerrain.Mode.TRANSFORM){DevilFruitPowerLogic.resetDevilFruitPowers(owner);check(key()+"/reset-keeps-terrain",permanentSample(),"fruit reset does not restore the terrain");}scene++;age=0;if(scene<moves.size())begin();else end();}catch(Throwable e){check("fixture/exception",false,e.toString());end();}});
 }
 private static void controls(){
  check("catalogue",moves.size()==45 && TechniqueTerrain.reviewed().size()>=46,"45 previous techniques plus Amaru explicit");
  for(String f:PREVIOUS)for(var m:DevilFruitRegistry.movesFor(item(f))){fruit(item(f));arena();var p=TechniqueTerrain.policy(item(f),m.id());check(f+"/"+m.id()+"/policy",p!=null,"fruit-scoped reviewed policy");if(p==null || p.mode()!=TechniqueTerrain.Mode.DESTRUCTIVE)continue;
   BlockPos hit=new BlockPos(510,101,86);world.setBlockAndUpdate(hit,Blocks.STONE.defaultBlockState());var b=TechniqueTerrain.begin(owner,m);b.impact(new Vec3(510.5,101.5,85.99));check(f+"/"+m.id()+"/stone-impact",world.getBlockState(hit).isAir() && b.broken()>0,"real stone removed");
   BlockPos last=null;for(int i=0;i<p.blocks()+3;i++){last=new BlockPos(499+i%30,101,69+i/30);world.setBlockAndUpdate(last,Blocks.STONE.defaultBlockState());b.impact(new Vec3(last.getX()+.5,101.5,last.getZ()-.01));}check(f+"/"+m.id()+"/shared-budget",b.broken()==p.blocks() && world.getBlockState(last).is(Blocks.STONE),"distinct positions exhaust shared quota: "+b.broken()+"/"+p.blocks());
  }
  fruit(item("mera_mera"));BlockPos hit=new BlockPos(510,101,86);Vec3 at=new Vec3(510.5,101.5,85.99);
  world.setBlockAndUpdate(hit,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(hit);chest.setItem(0,new ItemStack(Items.DIAMOND,7));TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/chest",world.getBlockEntity(hit)==chest && chest.getItem(0).getCount()==7,"same inventory object and seven diamonds");
  world.setBlockAndUpdate(hit,Blocks.BEDROCK.defaultBlockState());TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/bedrock",world.getBlockState(hit).is(Blocks.BEDROCK),"indestructible intact");
  world.setBlockAndUpdate(hit,Blocks.OBSIDIAN.defaultBlockState());TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/resistance",world.getBlockState(hit).is(Blocks.OBSIDIAN),"resistance above6 intact");
  world.setBlockAndUpdate(hit,Blocks.STONE.defaultBlockState());PowerBlockService.place(world,hit,Blocks.PACKED_ICE.defaultBlockState(),200,owner.getUUID());TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/temporary",world.getBlockState(hit).is(Blocks.PACKED_ICE),"another temporary change is not erased");PowerBlockService.releaseAll(owner.getUUID());
  br.com.oneminepiece.opworldlike.explosion.DevilFruitExplosionConfig.addZoneForTest(world.dimension().identifier()+"@510,100,86,511,102,87");world.setBlockAndUpdate(hit,Blocks.STONE.defaultBlockState());TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/protection",world.getBlockState(hit).is(Blocks.STONE),"protected candidate remains intact");br.com.oneminepiece.opworldlike.explosion.DevilFruitExplosionConfig.clearTestZones();
  BlockPos far=new BlockPos(515,101,86);world.setBlockAndUpdate(far,Blocks.STONE.defaultBlockState());TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/radius",world.getBlockState(far).is(Blocks.STONE),"outside terrain radius remains intact");
  world.setBlockAndUpdate(hit,Blocks.OBSIDIAN.defaultBlockState());BlockPos behind=hit.south();world.setBlockAndUpdate(behind,Blocks.STONE.defaultBlockState());TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/cover",world.getBlockState(behind).is(Blocks.STONE),"resistant cover blocks terrain changes behind it");world.setBlockAndUpdate(hit,Blocks.STONE.defaultBlockState());
  try{var config=java.nio.file.Path.of("config","opworldlike","technique-terrain.json");String saved=java.nio.file.Files.readString(config);try{java.nio.file.Files.writeString(config,"{\"enabled\":{\"mera_mera/hiken\":false}}");TechniqueTerrain.reload();TechniqueTerrain.begin(owner,"hiken").impact(at);check("guard/config-disabled",world.getBlockState(hit).is(Blocks.STONE),"real config opt-out suppresses permanent changes");}finally{java.nio.file.Files.writeString(config,saved);TechniqueTerrain.reload();}}catch(Exception e){check("guard/config-disabled",false,e.toString());}
  var stale=TechniqueTerrain.begin(owner,"hiken");fruit(item("pika_pika"));stale.impact(at);check("guard/fruit-reset",world.getBlockState(hit).is(Blocks.STONE),"pending cast loses authority on fruit change");
  var utility=TechniqueTerrain.begin(owner,"passive");utility.impact(at);check("guard/none",world.getBlockState(hit).is(Blocks.STONE),"movement never mines");
  fruit(item("suna_suna"));arena();var temp=TechniqueTerrain.begin(owner,"grounddeath");temp.replaceSurface(START,4,Blocks.SANDSTONE.defaultBlockState());BlockPos cell=new BlockPos(510,99,80);check("temporary/created",world.getBlockState(cell).is(Blocks.SANDSTONE),"actual stable ground conversion");world.setBlockAndUpdate(cell,Blocks.DIAMOND_BLOCK.defaultBlockState());PowerBlockService.releaseAll(owner.getUUID());check("temporary/player-edit",world.getBlockState(cell).is(Blocks.DIAMOND_BLOCK),"later player edit preserved");
 }
 private static void begin(){fruit(fruits.get(scene));arena();if(victim!=null)victim.discard();String id=moves.get(scene).id();
  // Projectile shots hit a genuine one-layer stone wall. The close contacts have a real target.
  for(int x=507;x<=514;x++)for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,87),Blocks.STONE.defaultBlockState());
  if(id.equals("sango")){victim=EntityTypes.HUSK.spawn(world,new BlockPos(510,100,84),EntitySpawnReason.COMMAND);victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(510.5,100,84.5);}
  if(id.equals("iceage")){BlockPos water=new BlockPos(517,99,80);world.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());}
  before=TechniqueTerrain.brokenTotal(key());MinepieceModVariables.get(owner).PlayerTechniqueUsedNumber=DevilFruitRegistry.movesFor(fruits.get(scene)).indexOf(moves.get(scene));boolean ok=DevilFruitSkillDispatcher.castSkill(owner);check(key()+"/native-cast",ok,"real skill dispatcher, normal energy/cooldown");
 }
 private static void finish(){var p=TechniqueTerrain.policy(fruits.get(scene),moves.get(scene).id());int delta=TechniqueTerrain.brokenTotal(key())-before;
  check(key()+"/native-terrain",p!=null && (p.mode()==TechniqueTerrain.Mode.DESTRUCTIVE?delta>0 && delta<=p.blocks():delta==0),"actual permanently broken="+delta);
  if(p!=null && p.mode()==TechniqueTerrain.Mode.TRANSFORM)check(key()+"/native-transform-active",PowerBlockService.activeCountFor(owner.getUUID())>0 && permanentSample(),"permanent transformed blocks with active attribution at150ticks");
 }
 private static boolean permanentSample(){String id=moves.get(scene).id();BlockPos pos=id.equals("iceage")?new BlockPos(517,99,80):id.equals("grounddeath")?new BlockPos(510,99,80):new BlockPos(510,99,81);return world.getBlockState(pos).is(id.equals("grounddeath")?Blocks.SANDSTONE:Blocks.PACKED_ICE);}
 private static void end(){active=false;if(victim!=null)victim.discard();if(owner!=null){DevilFruitPowerLogic.resetDevilFruitPowers(owner);MinepieceModVariables.clear(owner);owner.discard();}org.slf4j.LoggerFactory.getLogger("TerrainTest").info("[dterrain] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
