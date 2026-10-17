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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Native server tick acceptance: FakePlayer casts, inserted adult mobs are actual damage witnesses. */
public final class Batch01TestCommands {
 private static final Item[] FRUITS={MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU,MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA,MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI};
 private static final String[] NAMES={"hie","pika","moku","suna","yami"};
 private static final List<Item> fruitList=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();
 private static final Set<String> NON_DAMAGE=Set.of("yatanokagami","amanomurakumo","passive");
 private static final List<Mob> fixtures=new ArrayList<>();
 private static ServerLevel world;private static ServerPlayer caster;private static Mob normal,refused,ally,outside;
 private static Vec3 start;private static int scene,age,passed,failed;private static boolean active;private static double baseSpeed;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch01Acceptance").info("[dbatch01] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  dispatcher.register(Commands.literal("dbatchlive").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->{
   if(!"batch01".equals(System.getProperty("opworldlike.visualScenario")) || c.getSource().getPlayer()==null)return 0;
   var p=c.getSource().getPlayer();var l=(ServerLevel)p.level();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.setPermanentlyInvulnerable(false);p.removeAllEffects();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);
   var v=MinepieceModVariables.get(p);v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI);v.PlayerEnergy=2000;
   var attacker=EntityTypes.HUSK.spawn(l,p.blockPosition().offset(3,0,0),EntitySpawnReason.COMMAND);if(attacker==null)return 0;attacker.setNoAi(true);
   YamiDarkness.clearFor(p,"test");YamiDarkness.startPull(p,10);float pullBefore=p.getHealth();p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(l,l.damageSources().mobAttack(attacker),4);
   org.slf4j.LoggerFactory.getLogger("Batch01Live").info("[batch01-native] pull-hit {} hp={}",p.getHealth()<pullBefore && YamiDarkness.isPulling(p)?"PASS":"FAIL",p.getHealth());p.setHealth(100);
   YamiDarkness.startAbsorbing(p);float before=p.getHealth();int count=br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.absorbCapacityHits();
   for(int i=0;i<count;i++){p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(l,l.damageSources().mobAttack(attacker),4);}
   org.slf4j.LoggerFactory.getLogger("Batch01Live").info("[batch01-native] absorption {} hp={} absorbed={}",p.getHealth()==before && YamiDarkness.absorbedHits(p)==count && Math.abs(v.PlayerEnergy-(2000-count*br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.absorbCostEnergy()))<.001?"PASS":"FAIL",p.getHealth(),YamiDarkness.absorbedHits(p));
   double energy=v.PlayerEnergy;p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(l,l.damageSources().mobAttack(attacker),4);
   org.slf4j.LoggerFactory.getLogger("Batch01Live").info("[batch01-native] capacity {} hp={}",p.getHealth()<before && YamiDarkness.absorbRemaining(p)==0 && v.PlayerEnergy==energy?"PASS":"FAIL",p.getHealth());
   YamiDarkness.startAbsorbing(p);before=p.getHealth();p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(l,CombatSkillLogic.elementalDamageSource(l,ElementType.EXPLOSIVE,attacker,null),4);
   org.slf4j.LoggerFactory.getLogger("Batch01Live").info("[batch01-native] explosion {} hp={}",p.getHealth()<before && YamiDarkness.absorbRemaining(p)==count?"PASS":"FAIL",p.getHealth());
   v.PlayerEnergy=0;before=p.getHealth();p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(l,l.damageSources().mobAttack(attacker),4);
   org.slf4j.LoggerFactory.getLogger("Batch01Live").info("[batch01-native] no-energy {} hp={}",p.getHealth()<before && YamiDarkness.absorbRemaining(p)==count && v.PlayerEnergy==0?"PASS":"FAIL",p.getHealth());
   YamiDarkness.clearFor(p,"test");attacker.discard();return 1;
  }));
  for(int f=0;f<FRUITS.length;f++)for(var m:DevilFruitRegistry.movesFor(FRUITS[f])){fruitList.add(FRUITS[f]);moves.add(m);names.add(NAMES[f]);}
  dispatcher.register(Commands.literal("dbatch01").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->{if(active)return 0;world=c.getSource().getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch01Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch01Acceptance"));passed=failed=scene=age=0;active=true;begin();return 1;}));
  ServerTickEvents.END_SERVER_TICK.register(s->{if(!active)return;try{age++;int index=scene/2;var m=moves.get(index);String prefix=names.get(index)+"/"+m.id();
   if(age==3 && scene%2==0 && !NON_DAMAGE.contains(m.id()))check(prefix+"/telegraph",normal.getHealth()==500,"no damage before windup");
   if(age==14 && m.id().equals("magatama") && scene%2==0)check(prefix+"/staggered",world.getEntitiesOfClass(SkillProjectileEntity.class,caster.getBoundingBox().inflate(40)).stream().filter(e->e.getVisualProfile().equals("magatama")).count()<=2,"volley not all released together");
   if(age<85)return;finish();clean();scene++;age=0;if(scene==moves.size()*2){extras();active=false;end();}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();active=false;end();}});
 }
 private static void setFruit(Item fruit){LightSwords.clearFor(caster);var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(fruit);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;caster.removeAllEffects();caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);caster.getAbilities().invulnerable=false;caster.getInventory().clearContent();caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));}
 private static Mob mob(double x,double z){var e=EntityTypes.HUSK.spawn(world,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);if(e==null)throw new IllegalStateException("spawn");e.setBaby(false);e.setPos(x,100,z);e.setNoAi(true);e.setNoGravity(true);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);fixtures.add(e);return e;}
 private static void begin(){int i=scene/2;setFruit(fruitList.get(i));caster.teleportTo(280.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);start=caster.position();
  for(int x=260;x<=302;x++)for(int z=76;z<=112;z++){world.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());for(int y=100;y<=108;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
  normal=mob(280.5,85.5);refused=mob(282.5,85.5);refused.setPermanentlyInvulnerable(true);outside=mob(340.5,140.5);
  var own=new SummonEntity(MinepieceModEntities.SUMMON,world);own.setOwnerEntity(caster);own.setNoAi(true);own.setNoGravity(true);own.lifetime(200);own.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);own.setHealth(500);own.setPos(279.5,100,85.5);world.addFreshEntity(own);ally=own;fixtures.add(own);
  if(Set.of("kurouzu","blackhole").contains(moves.get(i).id()))for(Mob body:new Mob[]{normal,outside}){body.setNoAi(false);body.removeFreeWill();body.getNavigation().stop();body.setTarget(null);body.setSpeed(0);}
  if(scene%2==1)for(int x=260;x<=302;x++)for(int y=100;y<=108;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.OBSIDIAN.defaultBlockState());
  var m=moves.get(i);var v=MinepieceModVariables.get(caster);v.PlayerTechniqueUsedNumber=DevilFruitRegistry.movesFor(fruitList.get(i)).indexOf(m);boolean cast=DevilFruitSkillDispatcher.castSkill(caster);String prefix=names.get(i)+"/"+m.id();
  if(scene%2==0){check(prefix+"/cost",cast && Math.abs(v.PlayerEnergy-(2000-m.energyCost()))<.001,"energy="+v.PlayerEnergy);double e=v.PlayerEnergy;check(prefix+"/cooldown",!DevilFruitSkillDispatcher.castSkill(caster) && e==v.PlayerEnergy,"spam has no second debit");String anim=TechniqueAnimations.nameFor(m,caster);check(prefix+"/animation",anim.equals("b01_"+m.id()) && TechniqueAnimations.fichierPresent(anim),anim);check(prefix+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"native ticking mob fixture");}
 }
 private static void finish(){int i=scene/2;String id=moves.get(i).id(),prefix=names.get(i)+"/"+id;
  if(scene%2==1){check(prefix+"/wall",normal.getHealth()==500,"behind wall HP="+normal.getHealth());if(Set.of("kick","whitelauncher","barjan","speedispower","passive","yatanokagami").contains(id))check(prefix+"/body-wall",caster.getZ()<82.71,"full body stopped z="+caster.getZ());return;}
  if(!NON_DAMAGE.contains(id)){check(prefix+"/damage",normal.getHealth()<500,"HP="+normal.getHealth());check(prefix+"/attribution",normal.getLastHurtByMob()==caster,"native last attacker");}
  else if(id.equals("amanomurakumo")){check(prefix+"/sword",caster.getMainHandItem().is(MinepieceModItems.AMANO_MURAKUMO),"actual native sword instead of strength potion");caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(MinepieceModVariables.get(caster).PlayerDevilFruit));double e=MinepieceModVariables.get(caster).PlayerEnergy;check(prefix+"/no-duplicate",!DevilFruitSkillDispatcher.castSkill(caster) && e==MinepieceModVariables.get(caster).PlayerEnergy,"existing sword refuses duplicate/refunds");}
  else{double moved=caster.position().distanceTo(start);check(prefix+"/movement",moved>2 && moved<24.1,"distance="+moved);check(prefix+"/peaceful",normal.getHealth()==500,"movement has no damage");}
  check(prefix+"/ally",ally.getHealth()==500,"owned summon intact");check(prefix+"/refused",refused.getHealth()==500 && !FreezeService.isFrozen(refused) && !Desiccation.isDesiccated(refused) && !YamiSeal.isSealed(refused),"refused damage has no offensive status");check(prefix+"/outside",outside.getHealth()==500,"outside intact");check(prefix+"/bounded",ElementalBatchTechniques.activeCount()==0 && !LightCharge.isCharging(caster),"cast episode finished");
  if(Set.of("kurouzu","blackhole").contains(id)){check(prefix+"/pull-displacement",normal.position().distanceTo(caster.position())<4,"actual native target distance="+normal.position().distanceTo(caster.position()));check(prefix+"/pull-control",outside.position().distanceTo(new Vec3(340.5,100,140.5))<.01,"same native physics outside pull, no spontaneous movement");}
  if(Set.of("kick","barjan","whitelauncher","speedispower").contains(id))check(prefix+"/movement",caster.position().distanceTo(start)>2 && caster.position().distanceTo(start)<3.1,"dash native displacement="+caster.position().distanceTo(start));
 }
 private static void clean(){ElementalBatchTechniques.clearAll();LightCharge.clearAll();LightBuffs.clearAll("test");SmokeCloud.endAllClouds("test");SandStorm.clearAll();YamiDarkness.clearAll();FreezeService.clearAll();Desiccation.clearAll();for(var e:fixtures)e.discard();fixtures.clear();for(var e:world.getEntitiesOfClass(SkillProjectileEntity.class,new AABB(250,95,60,355,120,160)))e.discard();for(var e:world.getEntitiesOfClass(EntityGomuArmEntity.class,new AABB(250,95,60,355,120,160)))e.discard();}
 private static void extras(){
  setFruit(FRUITS[0]);caster.teleportTo(280.5,100,80.5);normal=mob(280.5,82.5);normal.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,400,2));FreezeService.apply(normal,20,caster);FreezeService.clearAll();check("core/ice-external-slow",normal.getEffect(MobEffects.SLOWNESS).getAmplifier()==2 && normal.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(Identifier.fromNamespaceAndPath("minepiece","freeze_slow"))==null,"only own modifier removed");
  normal.addEffect(new MobEffectInstance(MinepieceModMobEffects.FROZEN,400,5));FreezeService.apply(normal,20,caster);FreezeService.clearAll();check("core/ice-external-marker",normal.getEffect(MinepieceModMobEffects.FROZEN)!=null && normal.getEffect(MinepieceModMobEffects.FROZEN).getAmplifier()==5,"pre-existing foreign marker preserved");normal.removeEffect(MinepieceModMobEffects.FROZEN);
  BlockPos chestPos=new BlockPos(300,100,100);world.setBlockAndUpdate(chestPos,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(chestPos);chest.setItem(0,new ItemStack(Items.GOLD_INGOT,64));PowerBlockService.place(world,chestPos,Blocks.PACKED_ICE.defaultBlockState(),20,caster.getUUID());check("core/ice-chest",world.getBlockEntity(chestPos)==chest && chest.getItem(0).getCount()==64,"block-entity inventory never replaced");
  FreezeService.apply(normal,20,caster);DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/ice-reset-immediate",FreezeService.trackedCount()==0,"no residual freeze after synchronous reset");
  setFruit(FRUITS[3]);Desiccation.apply(normal,caster);Desiccation.clearAll();check("core/sand-external-slow",normal.getEffect(MobEffects.SLOWNESS).getAmplifier()==2 && normal.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(Identifier.fromNamespaceAndPath("minepiece","desiccation_slow"))==null,"external potion preserved");
  Desiccation.apply(normal,caster);SandStorm.create(caster);DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/sand-reset-immediate",Desiccation.trackedCount()==0 && SandStorm.activeStorms()==0,"storm and drying removed immediately");
  setFruit(FRUITS[1]);caster.addEffect(new MobEffectInstance(MobEffects.SPEED,600,0));baseSpeed=caster.getAttributeValue(Attributes.MOVEMENT_SPEED);LightBuffs.apply(caster,MobEffects.SPEED,40,2);double during=caster.getAttributeValue(Attributes.MOVEMENT_SPEED);LightBuffs.clearAll("test");check("core/light-external-speed",caster.getEffect(MobEffects.SPEED).getAmplifier()==0 && Math.abs(during/baseSpeed-1.6/1.2)<.0001 && Math.abs(caster.getAttributeValue(Attributes.MOVEMENT_SPEED)-baseSpeed)<.00001,"speed ratio="+during/baseSpeed);
  caster.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));boolean granted=LightSwords.grant(caster),hadSword=caster.getMainHandItem().is(MinepieceModItems.AMANO_MURAKUMO);LightSwords.clearFor(caster);check("core/sword-cleanup",granted && hadSword && caster.getMainHandItem().isEmpty() && caster.getOffhandItem().is(Items.SHIELD),"only tagged grant removed, offhand preserved");check("core/sword-recast",LightSwords.grant(caster),"new lease after cleanup");LightSwords.clearFor(caster);
  for(int slot=0;slot<caster.getInventory().getContainerSize();slot++)caster.getInventory().setItem(slot,new ItemStack(Items.DIRT,64));
  MinepieceModVariables.get(caster).PlayerTechniqueUsedNumber=5;double swordEnergy=MinepieceModVariables.get(caster).PlayerEnergy;boolean fullCast=DevilFruitSkillDispatcher.castSkill(caster);check("core/sword-full",!fullCast && LightSwords.activeCount()==0 && MinepieceModVariables.get(caster).PlayerEnergy==swordEnergy,"full inventory refused and refunded");caster.getInventory().clearContent();
  setFruit(FRUITS[2]);SmokeCloud.create(caster);normal.setPos(280.5,100,82.5);SmokeCloud.captureInClouds(world);check("core/smoke-capture",SmokeCloud.isCaptured(normal),"visible cylinder entry");normal.setPos(295.5,100,82.5);SmokeCloud.tickCaptures();check("core/smoke-exit",!SmokeCloud.isCaptured(normal),"leaving cylinder releases");MinepieceModVariables.get(caster).PlayerEnergy=0;SmokeCloud.tickMobility(caster);check("core/smoke-no-energy",!SmokeCloud.hasMobility(caster),"no free fractional-energy ticks");SmokeCloud.endAllClouds("test");
  for(int f=0;f<FRUITS.length;f++){setFruit(FRUITS[f]);var m=DevilFruitRegistry.movesFor(FRUITS[f]).get(0);ElementalBatchTechniques.cast(caster,new ItemStack(FRUITS[f]),m);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);ElementalBatchTechniques.tick();check("core/fruit-change-"+NAMES[f],ElementalBatchTechniques.activeCount()==0,"pending episode cancelled");}
  setFruit(FRUITS[4]);YamiDarkness.startAbsorbing(caster);caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));check("core/yami-sealed-absorption",!YamiDarkness.absorbIfAble(caster) && !caster.hasEffect(MinepieceModMobEffects.YAMI_ABSORBING),"revalidate at real damage entry");YamiDarkness.clearAll();
  setFruit(FRUITS[4]);YamiDarkness.startPull(caster,10);check("core/yami-pull-without-window",!YamiDarkness.absorbIfAble(caster) && YamiDarkness.isPulling(caster),"no absent-window cancellation");YamiDarkness.clearAll();YamiDarkness.startAbsorbing(caster);for(int k=0;k<590;k++)YamiDarkness.tickOnceForTest();YamiDarkness.startPull(caster,10);for(int k=0;k<11;k++)YamiDarkness.tickOnceForTest();check("core/yami-independent-expiry",!YamiDarkness.isAbsorbing(caster) && YamiDarkness.remainingPullTicks(caster)>0 && !caster.hasEffect(MinepieceModMobEffects.YAMI_ABSORBING),"manual service-clock expiry preserves newer pull");YamiDarkness.clearAll();clean();
 }
 private static void end(){MinepieceModVariables.clear(caster);org.slf4j.LoggerFactory.getLogger("Batch01Acceptance").info("[dbatch01] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
