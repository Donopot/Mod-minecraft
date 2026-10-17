package net.mcreator.minepiece.command;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.procedures.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Real server ticks, inserted adult mobs. FakePlayer is the casting fixture, not a damage oracle. */
public final class Batch02TestCommands {
 private static final Item[] FRUITS={MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA,MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU,MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI,MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU,MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE};
 private static final String[] NAMES={"gura","bomu","zushi","nikyu","ope"};
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();
 private static final List<Mob> fixtures=new ArrayList<>();private static final Set<String> PEACEFUL=Set.of("room","shambles","passive");
 private static final Set<String> DASH=Set.of("kickbomb","attackspeed","assault","kumapunch","passive");
 private static ServerLevel world;private static ServerPlayer caster;private static Mob normal,refused,ally,outside;
 private static int scene,age,passed,failed,extra;private static boolean active,roof;private static Vec3 start;private static double healthBefore;private static int terrainBefore;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch02").info("[dbatch02] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  for(int i=0;i<FRUITS.length;i++)for(var m:DevilFruitRegistry.movesFor(FRUITS[i])){fruits.add(FRUITS[i]);moves.add(m);names.add(NAMES[i]);}
  dispatcher.register(Commands.literal("dbatch02").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->{if(active)return 0;world=c.getSource().getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch02Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch02Acceptance"));passed=failed=scene=age=extra=0;active=true;begin();return 1;}));
  ServerTickEvents.END_SERVER_TICK.register(s->{if(!active)return;try{
   if(scene>=moves.size()*3){extrasTick();return;}
   age++;int i=scene/3;var m=moves.get(i);String prefix=names.get(i)+"/"+m.id();
   if(age==3 && scene%3==0 && !PEACEFUL.contains(m.id()))check(prefix+"/telegraph",normal.getHealth()==500,"no premature hit");
   if(age==22 && scene%3==0 && m.id().equals("stoneopeope"))check(prefix+"/staggered",ControlledBatchTechniques.shotCount()<4,"rubble releases across native ticks");
   if(age<90)return;finish();clean();scene++;age=0;if(scene==moves.size()*3){core();setupRoof();}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();end();}});
 }
 private static void fruit(Item item){DevilFruitPowerLogic.resetDevilFruitPowers(caster);var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(item);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;caster.removeAllEffects();caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);caster.getAbilities().invulnerable=false;caster.setHealth(20);caster.teleportTo(376.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);}
 private static Mob mob(double x,double z){var e=EntityTypes.HUSK.spawn(world,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);if(e==null)throw new IllegalStateException("spawn");e.setBaby(false);e.setNoAi(true);e.setNoGravity(true);e.setPos(x,100,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);fixtures.add(e);return e;}
 private static boolean cast(String id){var list=DevilFruitRegistry.movesFor(MinepieceModVariables.get(caster).PlayerDevilFruit.getItem());for(int i=0;i<list.size();i++)if(list.get(i).id().equals(id)){MinepieceModVariables.get(caster).PlayerTechniqueUsedNumber=i;return DevilFruitSkillDispatcher.castSkill(caster);}return false;}
 private static void arena(){for(int x=352;x<=405;x++)for(int z=76;z<=140;z++){world.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());for(int y=94;y<=99;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());for(int y=100;y<135;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}
 private static void begin(){int i=scene/3;fruit(fruits.get(i));arena();start=caster.position();String id=moves.get(i).id();double distance=id.equals("meteor")?18:id.equals("meteorsmall")?12:id.equals("amputation")?4:id.equals("bomboverhead")?(scene%3==0?3:5):5;
  normal=mob(376.5,80.5+distance);refused=mob(378.5,80.5+distance);refused.setPermanentlyInvulnerable(true);outside=mob(442.5,145.5);
  var summon=new SummonEntity(MinepieceModEntities.SUMMON,world);summon.setOwnerEntity(caster);summon.setNoAi(true);summon.setNoGravity(true);summon.lifetime(200);summon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);summon.setHealth(500);summon.setPos(375.5,100,80.5+distance);world.addFreshEntity(summon);ally=summon;fixtures.add(summon);
  if(Set.of("shambles","amputation").contains(id))OpeRoom.open(caster);
  if(scene%3==1)for(int x=352;x<=405;x++)for(int y=100;y<135;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.OBSIDIAN.defaultBlockState());
  if(scene%3==2)for(int x=352;x<=405;x++)for(int y=100;y<=105;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.STONE.defaultBlockState());
  terrainBefore=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id);
  if(scene%3==2 && id.equals("kabutowari"))normal.setPos(376.5,100,82);
  double before=MinepieceModVariables.get(caster).PlayerEnergy;boolean result=cast(id);String prefix=names.get(i)+"/"+id;
  if(scene%3==0){check(prefix+"/cost",result && Math.abs(MinepieceModVariables.get(caster).PlayerEnergy-(before-moves.get(i).energyCost()))<.001,"one debit");double debit=MinepieceModVariables.get(caster).PlayerEnergy;check(prefix+"/cooldown",!cast(id) && debit==MinepieceModVariables.get(caster).PlayerEnergy,"spam refused without debit");check(prefix+"/animation",TechniqueAnimations.nameFor(moves.get(i),caster).equals("b02_"+id) && TechniqueAnimations.fichierPresent("b02_"+id),"owned phase file");check(prefix+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"victim actually inserted and ticking");}
 }
 private static void finish(){int i=scene/3;String id=moves.get(i).id(),prefix=names.get(i)+"/"+id;
  if(scene%3==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int changed=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;check(prefix+"/terrain",policy!=null && (policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE?changed>0 && changed<=policy.blocks():changed==0),"actual permanent blocks="+changed+", policy="+policy);return;}
  if(scene%3==1){check(prefix+"/wall",normal.getHealth()==500 && !Amputation.isCut(normal),"wall protects damage and offensive state");if(DASH.contains(id))check(prefix+"/body-wall",caster.getZ()<82.71,"full body stops before wall");return;}
  if(!PEACEFUL.contains(id)){check(prefix+"/damage",normal.getHealth()<500,"HP="+normal.getHealth());check(prefix+"/attribution",normal.getLastHurtByMob()==caster,"native attribution");}
  else check(prefix+"/peaceful",normal.getHealth()==500,"utility has no damage");
  if(DASH.contains(id))check(prefix+"/movement",caster.position().distanceTo(start)>2 && caster.position().distanceTo(start)<8.1,"position="+caster.position());
  if(id.equals("shambles"))check(prefix+"/swap",caster.position().distanceTo(start)>4 && normal.position().distanceTo(start)<.1,"both positions exchanged");
  if(id.equals("room"))check(prefix+"/room-open",OpeRoom.isOpen(caster) && OpeRoom.remainingTicks(caster)>0,"native duration active");
  if(id.equals("amputation"))check(prefix+"/section",Amputation.isCut(normal) && normal.getAttribute(Attributes.ATTACK_DAMAGE).getValue()==0,"confirmed damage and section");
  if(id.equals("stoneopeope"))check(prefix+"/single-budget",normal.getHealth()==480,"four projectiles share explicit20 total per target");
  check(prefix+"/ally",ally.getHealth()==500,"ally intact");check(prefix+"/refused",refused.getHealth()==500 && !Amputation.isCut(refused),"invulnerable intact");check(prefix+"/outside",outside.getHealth()==500,"outside intact");check(prefix+"/bounded",ControlledBatchTechniques.activeCount()==0 && ControlledBatchTechniques.shotCount()==0 && SeismicWave.activeWaves()==0,"finite action/projectiles/waves");
 }
 private static void clean(){ControlledBatchTechniques.clearAll();SeismicWave.clearAll();GravityField.clearAll();Levitation.releaseAll();OpeRoom.clearAll();Amputation.clearAll();for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){arena();
  fruit(FRUITS[1]);normal=mob(376.5,82.5);normal.setPermanentlyInvulnerable(true);Vec3 velocity=normal.getDeltaMovement();int hits=DevilFruitExplosions.blast(caster,normal.getBoundingBox().getCenter(),3,15,null);check("core/explosion-refused",hits==0 && normal.getDeltaMovement().equals(velocity),"refused damage has no knockback or hit count");
  BlockPos pos=new BlockPos(390,100,100);world.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(pos);chest.setItem(0,new ItemStack(Items.DIAMOND,7));PowerBlockService.place(world,pos,Blocks.AIR.defaultBlockState(),20,caster.getUUID());check("core/chest",world.getBlockEntity(pos)==chest && chest.getItem(0).getCount()==7,"inventory remains intact");
  clean();fruit(FRUITS[4]);normal=mob(376.5,84.5);OpeRoom.open(caster);boolean section=Amputation.amputate(world,caster,normal);normal.addEffect(new MobEffectInstance(MobEffects.STRENGTH,200,1));check("core/section-external-buff",section && normal.getAttribute(Attributes.ATTACK_DAMAGE).getValue()==0,"new external buff cannot bypass owned section");Amputation.restore(normal,"test");check("core/section-external-restore",normal.hasEffect(MobEffects.STRENGTH) && normal.getAttribute(Attributes.ATTACK_DAMAGE).getValue()>4,"only owned modifier removed");
  caster.teleportTo(376.5,100,100.5);Vec3 playerBefore=caster.position(),targetBefore=normal.position();check("core/swap-owner-outside",!OpeRoom.shambles(caster) && caster.position().equals(playerBefore) && normal.position().equals(targetBefore),"caster outside room refused atomically");clean();
  for(int i=0;i<FRUITS.length;i++){fruit(FRUITS[i]);boolean cast=cast(DevilFruitRegistry.movesFor(FRUITS[i]).getFirst().id());DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/reset-"+NAMES[i],cast && ControlledBatchTechniques.activeCount()==0 && ControlledBatchTechniques.shotCount()==0 && GravityField.activeFields()==0 && !OpeRoom.isOpen(caster),"immediate cleanup");}
 }
 private static void setupRoof(){clean();fruit(FRUITS[2]);arena();normal=mob(376.5,98.5);for(int x=373;x<=380;x++)for(int z=95;z<=102;z++)world.setBlockAndUpdate(new BlockPos(x,110,z),Blocks.STONE.defaultBlockState());roof=cast("meteor");extra=0;}
 private static void extrasTick(){extra++;
  if(extra==85){check("core/meteor-roof",roof && normal.getHealth()==500 && ControlledBatchTechniques.shotCount()==0 && world.getBlockState(new BlockPos(376,110,98)).isAir(),"actual roof block removed at descending collision; no phantom ground damage");clean();fruit(FRUITS[4]);arena();normal=mob(376.5,85.5);start=caster.position();check("core/ope-room-dispatch",cast("room"),"normal dispatcher opens room");}
  if(extra==135){check("core/ope-surgery-during-room",OpeRoom.isOpen(caster) && ControlledBatchTechniques.onCooldown(caster,"room") && cast("shambles"),"room cooldown retained, different surgery available; no cooldown cleared");}
  if(extra==170){check("core/ope-combo-swapped",caster.position().distanceTo(start)>4 && normal.position().distanceTo(start)<.1,"native dispatcher sequence swapped both");caster.setYRot(180);check("core/ope-amputation-dispatch",cast("amputation"),"third technique has independent recharge");}
  if(extra==200){check("core/ope-combo-section",normal.getHealth()<500 && Amputation.isCut(normal),"real follow-up damage and reversible section before room expiry");clean();end();}
 }
 private static void end(){active=false;if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}org.slf4j.LoggerFactory.getLogger("Batch02").info("[dbatch02] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
