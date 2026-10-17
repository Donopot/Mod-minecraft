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
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Real server ticks with inserted mobs and native projectile / summon AI, never mirrored damage. */
public final class Batch05TestCommands {
 private static final Item[] FRUITS=MythicBatchTechniques.FRUITS;
 private static final String[] NAMES=MythicBatchTechniques.NAMES;
 private static final Set<String> UTILITY=Set.of("shadowasgard","silentcommand","gear_change"),SUMMON=Set.of("doppelman","notewarrior","summontotmusica");
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();private static final List<LivingEntity> fixtures=new ArrayList<>();
 private static ServerLevel world;private static ServerPlayer caster;private static Mob normal,ally,refused,outside;
 private static int scene,age,passed,failed,terrainBefore,life=-1;private static boolean active,attributed,controlSeen;private static int maxSummons;private static Vec3 start;private static double before;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch05").info("[dbatch05] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  dispatcher.register(Commands.literal("dbatch05").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->start(c.getSource(),""))
   .then(Commands.argument("fruits",com.mojang.brigadier.arguments.StringArgumentType.greedyString()).executes(c->start(c.getSource(),com.mojang.brigadier.arguments.StringArgumentType.getString(c,"fruits")))));
  ServerTickEvents.END_SERVER_TICK.register(server->{if(!active)return;try{
   if(life>=0){lifeTick();return;}
   age++;caster.getCooldowns().tick();if(normal.getHealth()<500 && normal.getLastHurtByMob()==caster)attributed=true;
   var m=moves.get(scene/5);String id=m.id(),key=names.get(scene/5)+"/"+id;
   if(age==3 && scene%5==0)check(key+"/telegraph",normal.getHealth()==500,"no hit during windup");
   if(ImuCommand.active(normal))controlSeen=true;maxSummons=Math.max(maxSummons,MythicBatchTechniques.summonCount(caster));
   if(id.equals("gomu_gatling")&&scene%5==0&&(age==18||age==30||age==78))check(key+"/cadence-"+age,Math.abs(500-normal.getHealth()-(age==18?6:age==30?12:36))<.02,"actual native HP="+normal.getHealth());
   if(age<160)return;finish();clean();scene++;age=0;if(scene==moves.size()*5){core();life=0;lifeStart();}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();end();}});
 }
 private static int start(CommandSourceStack source,String filter){if(active)return 0;fruits.clear();moves.clear();names.clear();for(int f=0;f<FRUITS.length;f++)if(filter.isEmpty() || Arrays.asList(filter.split(",")).contains(NAMES[f]))for(var m:DevilFruitRegistry.movesFor(FRUITS[f])){fruits.add(FRUITS[f]);moves.add(m);names.add(NAMES[f]);}
  if(moves.isEmpty())return 0;world=source.getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch05Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch05Acceptance"));passed=failed=scene=age=0;life=-1;active=true;begin();return 1;
 }
 private static void fruit(Item f){DevilFruitPowerLogic.resetDevilFruitPowers(caster);var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(f);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;caster.getInventory().clearContent();caster.removeAllEffects();caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);caster.getAbilities().invulnerable=false;caster.setHealth(20);caster.teleportTo(680.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);}
 private static Mob mob(double x,double z){var e=EntityTypes.CREEPER.spawn(world,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);if(e==null)throw new IllegalStateException("spawn");e.setNoAi(true);e.setNoGravity(true);e.setPos(x,100,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);fixtures.add(e);return e;}
 private static boolean cast(String id){var list=DevilFruitRegistry.movesFor(MinepieceModVariables.get(caster).PlayerDevilFruit.getItem());for(int i=0;i<list.size();i++)if(list.get(i).id().equals(id)){MinepieceModVariables.get(caster).PlayerTechniqueUsedNumber=i;return DevilFruitSkillDispatcher.castSkill(caster);}return false;}
 private static void arena(){for(int x=674;x<=689;x++)for(int z=77;z<=105;z++){for(int y=95;y<=99;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}
 private static void begin(){int i=scene/5;fruit(fruits.get(i));arena();start=caster.position();String id=moves.get(i).id();normal=mob(680.5,84.5);refused=mob(682.5,84.5);refused.setPermanentlyInvulnerable(true);outside=mob(746.5,145.5);
  var own=new SummonEntity(MinepieceModEntities.SUMMON,world);own.setOwnerEntity(caster);own.setNoAi(true);own.setNoGravity(true);own.lifetime(500);own.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);own.setHealth(500);own.setPos(678.5,100,84.5);world.addFreshEntity(own);ally=own;fixtures.add(own);
  if(scene%5==1)for(int x=674;x<=689;x++)for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.OBSIDIAN.defaultBlockState());
  // A finite wall on a previous batch's wide floor can be walked around legitimately.
  // Seal this negative-damage fixture; navigation around an open wall is not penetration.
  if(scene%5==1){
   for(int x=674;x<=689;x++)for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,90),Blocks.OBSIDIAN.defaultBlockState());
   for(int z=83;z<=90;z++)for(int y=100;y<=110;y++)for(int x:new int[]{674,689})world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.OBSIDIAN.defaultBlockState());
   for(int x=674;x<=689;x++)for(int z=83;z<=90;z++)world.setBlockAndUpdate(new BlockPos(x,110,z),Blocks.OBSIDIAN.defaultBlockState());
  }
  if(scene%5==2)for(int x=674;x<=689;x++)for(int y=100;y<=106;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.STONE.defaultBlockState());
  if(scene%5==3){normal.setPos(746.5,100,145.5);ally.setPos(680.5,100,84.5);}if(scene%5==4){normal.setPos(746.5,100,145.5);refused.setPos(680.5,100,84.5);}
  attributed=false;controlSeen=false;maxSummons=0;terrainBefore=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id);before=MinepieceModVariables.get(caster).PlayerEnergy;boolean accepted=cast(id);String key=names.get(i)+"/"+id;
  if(scene%5==0){check(key+"/cost",accepted && MinepieceModVariables.get(caster).PlayerEnergy==before-moves.get(i).energyCost(),"accepted debit once");double energy=MinepieceModVariables.get(caster).PlayerEnergy;check(key+"/cooldown",!cast(id) && energy==MinepieceModVariables.get(caster).PlayerEnergy,"spam refuses without debit");check(key+"/animation",TechniqueAnimations.nameFor(moves.get(i),caster).equals("b05_"+names.get(i)+"_"+id) && TechniqueAnimations.fichierPresent("b05_"+names.get(i)+"_"+id),"owned finite phase delivered");check(key+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"victim inserted in ticking chunk");}
 }
 private static boolean controlled(LivingEntity e){return ImuCommand.active(e)||ShadowLedger.victimMarked(e);}
 private static void finish(){int i=scene/5;var m=moves.get(i);String id=m.id(),key=names.get(i)+"/"+id;
  if(scene%5>=3){var t=scene%5==3?ally:refused;check(key+(scene%5==3?"/ally-on-axis":"/refused-on-axis"),t.getHealth()==500 && !controlled(t),"sole trajectory victim HP="+t.getHealth());return;}
  if(scene%5==1){check(key+"/wall",normal.getHealth()==500 && !controlled(normal),"solid wall protects; HP="+normal.getHealth());return;}
  if(scene%5==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int broken=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;boolean destructive=policy!=null && policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE && !id.equals("weightmanipulation");check(key+"/terrain",policy!=null && (destructive?broken>0 && broken<=policy.blocks():broken==0),"native changed="+broken+" policy="+policy);return;}
  if(UTILITY.contains(id)){check(key+"/peaceful",normal.getHealth()==500,"utility has no direct damage");if(id.equals("silentcommand"))check(key+"/control",controlSeen&&!ImuCommand.active(normal),"native60tick slow appeared then expired");if(id.equals("shadowasgard"))check(key+"/shadow",ShadowLedger.isShadowless(normal.getUUID())&&caster.getScale()>1,"actual shadow ledger and enlarged body");if(id.equals("gear_change"))check(key+"/gear",GomuGears.gear(caster)==2&&MinepieceModVariables.get(caster).player_form==2,"actual server Gear2");}
  else if(SUMMON.contains(id))check(key+"/damage",normal.getHealth()<500 && maxSummons==(id.equals("notewarrior")?2:1),"native summon AI; max="+maxSummons+", HP="+normal.getHealth());
  else{double expected=id.equals("gomu_gatling")?36:m.damage();check(key+"/damage",Math.abs(500-normal.getHealth()-expected)<.02,"HP="+normal.getHealth()+" expected="+expected);}
  if(!UTILITY.contains(id)&&!SUMMON.contains(id))check(key+"/attribution",attributed,"native accepted hit attributed during160ticks");
  check(key+"/ally",ally.getHealth()==500 && !controlled(ally),"ally untouched");check(key+"/refused",refused.getHealth()==500 && !controlled(refused),"refused damage has no control");check(key+"/outside",outside.getHealth()==500,"out of range untouched");check(key+"/bounded",MythicBatchTechniques.activeCount()==0 && MythicBatchTechniques.shotCount()<=(id.equals("summontotmusica")?3:0) && MythicBatchTechniques.summonCount(caster)<=2,"finite casts and projectiles");
 }
 private static void clean(){MythicBatchTechniques.clearAll();for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){
  for(int f=0;f<FRUITS.length;f++){fruit(FRUITS[f]);String id=DevilFruitRegistry.movesFor(FRUITS[f]).getFirst().id();MinepieceModVariables.get(caster).PlayerEnergy=0;check("core/no-energy-"+NAMES[f],!cast(id)&&MythicBatchTechniques.activeCount()==0,"no effect/debit");fruit(FRUITS[f]);boolean accepted=cast(id);DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/reset-"+NAMES[f],accepted&&MythicBatchTechniques.activeCount()==0&&MythicBatchTechniques.shotCount()==0,"charge cancelled");fruit(FRUITS[f]);accepted=cast(id);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);MythicBatchTechniques.tick();check("core/fruit-change-"+NAMES[f],accepted&&MythicBatchTechniques.activeCount()==0,"exact fruit");fruit(FRUITS[f]);accepted=cast(id);caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));MythicBatchTechniques.tick();check("core/suppression-"+NAMES[f],accepted&&MythicBatchTechniques.activeCount()==0,"suppression cancels");
   fruit(FRUITS[f]);var pos=new BlockPos(680,100,82);world.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(pos);chest.setItem(0,new ItemStack(Items.DIAMOND,7));for(var move:DevilFruitRegistry.movesFor(FRUITS[f]))TechniqueTerrain.begin(caster,move).impact(new Vec3(680.5,100.5,81.99));check("core/chest-"+NAMES[f],world.getBlockEntity(pos)==chest&&chest.getItem(0).getCount()==7,"exact inventory survives");world.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}
  fruit(FRUITS[2]);arena();world.setBlockAndUpdate(new BlockPos(680,102,80),Blocks.OBSIDIAN.defaultBlockState());double deniedEnergy=MinepieceModVariables.get(caster).PlayerEnergy;check("core/tot-room-refusal",!cast("summontotmusica")&&MythicBatchTechniques.summonCount(caster)==0&&MinepieceModVariables.get(caster).PlayerEnergy==deniedEnergy,"3.6high summon refuses obstructed room without debit");world.setBlockAndUpdate(new BlockPos(680,102,80),Blocks.AIR.defaultBlockState());
  fruit(FRUITS[3]);normal=mob(680.5,83.5);normal.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,600,1));ImuCommand.apply(caster,normal);for(int i=0;i<61;i++)ImuCommand.tick();check("core/foreign-slow",!ImuCommand.active(normal)&&normal.hasEffect(MobEffects.SLOWNESS)&&normal.getEffect(MobEffects.SLOWNESS).getAmplifier()==1,"owned expiry preserves foreign potion");clean();
  fruit(FRUITS[3]);var foreignId=net.minecraft.resources.Identifier.fromNamespaceAndPath("b05test","foreign_armor");caster.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(foreignId,3,AttributeModifier.Operation.ADD_VALUE));SovereignVeil.apply(caster);double ownArmor=caster.getAttributeValue(Attributes.ARMOR);SovereignVeil.remove(caster);check("core/veil-foreign",ownArmor==7&&caster.getAttributeValue(Attributes.ARMOR)==3,"remove own4 preserves foreign3");caster.getAttribute(Attributes.ARMOR).removeModifier(foreignId);
  try{var config=java.nio.file.Path.of("config/opworldlike/devilfruit-imu.toml");String original=java.nio.file.Files.readString(config);try{java.nio.file.Files.writeString(config,"veil_enabled = false\nveil_armor_bonus = NaN\nveil_duration_ticks = 20\nveil_refresh_ticks = 10000\n");br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig.loadOrCreate();check("core/veil-disabled",!SovereignVeil.apply(caster)&&!SovereignVeil.hasVeil(caster),"disabled config respected");check("core/veil-finite",Double.isFinite(br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig.veilArmorBonus())&&br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig.veilRefreshTicks()<br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig.veilDurationTicks(),"nonfinite rejected and refresh before marker expiry");}finally{java.nio.file.Files.writeString(config,original);br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig.loadOrCreate();}}catch(Exception e){throw new IllegalStateException(e);}
  fruit(FRUITS[4]);double speed=caster.getAttributeValue(Attributes.MOVEMENT_SPEED),attack=caster.getAttributeValue(Attributes.ATTACK_DAMAGE);arena();for(int gear=2;gear<=5;gear++){check("core/gear-"+gear,GomuGears.cycle(caster,true)&&GomuGears.gear(caster)==gear&&MinepieceModVariables.get(caster).player_form==gear,"source2/3 then authored4/5");if(gear==3){world.setBlockAndUpdate(new BlockPos(680,102,80),Blocks.OBSIDIAN.defaultBlockState());check("core/gear-room-refusal",!GomuGears.cycle(caster,true)&&GomuGears.gear(caster)==3,"blocked Fourth preserves current form");world.setBlockAndUpdate(new BlockPos(680,102,80),Blocks.AIR.defaultBlockState());}if(gear==4){check("core/gear-fourth-flight",caster.getAbilities().mayfly&&MinepieceModVariables.get(caster).gomuFlightGranted,"own survival flight grant");check("core/gear-fourth-dimensions",Math.abs(caster.getBbWidth()-1.8)<.001&&Math.abs(caster.getBbHeight()-2.85)<.001,"native width="+caster.getBbWidth()+" height="+caster.getBbHeight());}if(gear==5)check("core/gear-fourth-flight-release",!caster.getAbilities().mayfly&&!MinepieceModVariables.get(caster).gomuFlightGranted,"fifth does not retain Fourth flight");}check("core/gear-cycle",GomuGears.cycle(caster,true)&&GomuGears.gear(caster)==0&&caster.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed&&caster.getAttributeValue(Attributes.ATTACK_DAMAGE)==attack,"all own attributes restored");GomuGears.cycle(caster,true);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);GomuGears.tick();check("core/gear-fruit-release",GomuGears.gear(caster)==0&&caster.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed,"fruit invalidation removes gear");
 }
 private static void lifeStart(){clean();arena();age=0;fruit(life==0?FRUITS[0]:life==1?FRUITS[2]:life==2?FRUITS[0]:FRUITS[4]);normal=mob(680.5,84.5);if(life==2)normal.setPos(746.5,100,145.5);check("life/cast-"+life,cast(life==0?"doppelman":life==1?"summontotmusica":life==2?"brickbat":"gear_change"),"dispatcher starts actual episode");}
 private static void lifeTick(){age++;caster.getCooldowns().tick();
  if(life<=1&&age==50)check("life/summon-live-"+life,MythicBatchTechniques.summonCount(caster)==1,"actual native summon before240TTL");
  if(life==2&&age==20){check("life/shot-live",MythicBatchTechniques.shotCount()>0,"shot actually launched");MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);}
  if(life==3&&age==25){GomuGears.cycle(caster,true);GomuGears.cycle(caster,true);check("life/gear-live",GomuGears.gear(caster)==4&&caster.getAbilities().mayfly,"Fourth and its own flight active");caster.setHealth(0);}
  if(age<(life==1?320:life==0?270:70))return;
  if(life<=1)check("life/summon-expiry-"+life,MythicBatchTechniques.summonCount(caster)==0,"actual240tick native expiry");
  if(life==1)check("life/tot-projectiles-expired",MythicBatchTechniques.shotCount()==0,"all energy shots also expired after owner summon TTL plus50ticks");
  if(life==2)check("life/shot-cancelled",MythicBatchTechniques.shotCount()==0&&MythicBatchTechniques.activeCount()==0,"launched projectile cancelled");
  if(life==3)check("life/gear-death",GomuGears.gear(caster)==0&&MinepieceModVariables.get(caster).player_form==0&&!caster.getAbilities().mayfly&&!MinepieceModVariables.get(caster).gomuFlightGranted,"death clears gear and dimensions");
  clean();if(++life==4){life=-1;end();}else lifeStart();
 }
 private static void end(){active=false;if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}org.slf4j.LoggerFactory.getLogger("Batch05").info("[dbatch05] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
