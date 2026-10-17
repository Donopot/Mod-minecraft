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

/** B06 acceptance: inserted native victims, actual server ticks and full beast dimensions. */
public final class Batch06TestCommands {
 private static final Item[] FRUITS=ZoanTechniqueEpisodes.FRUITS;
 private static final String[] NAMES=ZoanTechniqueEpisodes.NAMES;
 private static final Set<String> UTILITY=Set.of("soru","poursuite","kamiezanshin");
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();private static final List<LivingEntity> fixtures=new ArrayList<>();private static final List<ServerPlayer> forms=new ArrayList<>();
 private static ServerLevel world;private static ServerPlayer caster,cancelCaster;private static Mob normal,ally,refused,outside;
 private static int scene,age,passed,failed,terrainBefore,life=-1;private static boolean active,attributed;private static Vec3 start;private static double before;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch06").info("[dbatch06] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  dispatcher.register(Commands.literal("dbatch06").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->start(c.getSource(),""))
   .then(Commands.argument("fruits",com.mojang.brigadier.arguments.StringArgumentType.greedyString()).executes(c->start(c.getSource(),com.mojang.brigadier.arguments.StringArgumentType.getString(c,"fruits")))));
  dispatcher.register(Commands.literal("db06counter").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("stage",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c->nativeCounter(c.getSource(),com.mojang.brigadier.arguments.StringArgumentType.getString(c,"stage")))));
  ServerTickEvents.END_SERVER_TICK.register(server->{if(!active)return;try{
   if(life>=0){lifeTick();return;}
   age++;caster.getCooldowns().tick();if(normal.getHealth()<500&&normal.getLastHurtByMob()==caster)attributed=true;
   String id=moves.get(scene/5).id(),key=names.get(scene/5)+"/"+id;
   if(age==2&&scene%5==0)check(key+"/telegraph",normal.getHealth()==500,"no hit before preparation");
   if(id.equals("shiganmadara")&&scene%5==0&&(age==11||age==23||age==35))check(key+"/cadence-"+age,Math.abs(500-normal.getHealth()-(age==11?4:age==23?8:12))<.02,"actual HP="+normal.getHealth());
   if(age<120)return;finish();clean();scene++;age=0;if(scene==moves.size()*5){core();life=0;}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();end();}});
 }
 private static int start(CommandSourceStack source,String filter){if(active)return 0;fruits.clear();moves.clear();names.clear();for(int f=0;f<FRUITS.length;f++)if(filter.isEmpty()||Arrays.asList(filter.split(",")).contains(NAMES[f]))for(var m:DevilFruitRegistry.movesFor(FRUITS[f])){fruits.add(FRUITS[f]);moves.add(m);names.add(NAMES[f]);}world=source.getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch06Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch06Acceptance"));passed=failed=scene=age=0;life=-1;active=true;begin();return 1;}
 private static void fruit(Item f){DevilFruitPowerLogic.resetDevilFruitPowers(caster);var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(f);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;caster.getInventory().clearContent();caster.removeAllEffects();caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);caster.getAbilities().invulnerable=false;caster.setHealth(20);caster.teleportTo(680.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);}
 private static Mob mob(double x,double z){var e=EntityTypes.CREEPER.spawn(world,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);if(e==null)throw new IllegalStateException("spawn");e.setNoAi(true);e.setNoGravity(true);e.setPos(x,100,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);fixtures.add(e);return e;}
 private static boolean cast(String id){var list=DevilFruitRegistry.movesFor(MinepieceModVariables.get(caster).PlayerDevilFruit.getItem());for(int i=0;i<list.size();i++)if(list.get(i).id().equals(id)){MinepieceModVariables.get(caster).PlayerTechniqueUsedNumber=i;return DevilFruitSkillDispatcher.castSkill(caster);}return false;}
 private static void arena(){for(int x=674;x<=689;x++)for(int z=77;z<=105;z++){for(int y=95;y<=99;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}

 private static void begin(){int i=scene/5;fruit(fruits.get(i));arena();start=caster.position();String id=moves.get(i).id();normal=mob(680.5,id.equals("attackjump")?87.5:83.5);refused=mob(682.5,83.5);refused.setPermanentlyInvulnerable(true);outside=mob(746.5,145.5);
  var own=new SummonEntity(MinepieceModEntities.SUMMON,world);own.setOwnerEntity(caster);own.setNoAi(true);own.setNoGravity(true);own.lifetime(500);own.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);own.setHealth(500);own.setPos(678.5,100,83.5);world.addFreshEntity(own);ally=own;fixtures.add(own);
  if(scene%5==1||scene%5==2)for(int x=674;x<=689;x++)for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,82),scene%5==1?Blocks.OBSIDIAN.defaultBlockState():Blocks.STONE.defaultBlockState());
  if(scene%5==3){normal.setPos(746.5,100,145.5);ally.setPos(680.5,100,83.5);}if(scene%5==4){normal.setPos(746.5,100,145.5);refused.setPos(680.5,100,83.5);}
  attributed=false;terrainBefore=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id);before=MinepieceModVariables.get(caster).PlayerEnergy;boolean accepted=cast(id);String key=names.get(i)+"/"+id;
  if(scene%5==0){check(key+"/cost",accepted&&MinepieceModVariables.get(caster).PlayerEnergy==before-moves.get(i).energyCost(),"debit exactly once");double e=MinepieceModVariables.get(caster).PlayerEnergy;check(key+"/cooldown",!cast(id)&&e==MinepieceModVariables.get(caster).PlayerEnergy,"spam no debit");check(key+"/animation",TechniqueAnimations.nameFor(moves.get(i),caster).equals("b06_"+names.get(i)+"_"+id)&&TechniqueAnimations.fichierPresent("b06_"+names.get(i)+"_"+id),"finite owned gesture delivered");check(key+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"victim inserted in ticking chunk");}
 }
 private static void finish(){int i=scene/5;var m=moves.get(i);String id=m.id(),key=names.get(i)+"/"+id;
  if(scene%5>=3){var t=scene%5==3?ally:refused;check(key+(scene%5==3?"/ally-on-axis":"/refused-on-axis"),t.getHealth()==500&&!ImuCommand.active(t),"sole axis target survives without control");return;}
  if(scene%5==1){check(key+"/wall",normal.getHealth()==500&&!ImuCommand.active(normal),"obsidian cover; HP="+normal.getHealth());return;}
  if(scene%5==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int broken=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;check(key+"/terrain",policy!=null&&(policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE?broken>0&&broken<=policy.blocks():broken==0),"actual changed="+broken+" policy="+policy);return;}
  if(UTILITY.contains(id)){check(key+"/peaceful",normal.getHealth()==500,"utility never directly damages");if(id.equals("soru")||id.equals("poursuite"))check(key+"/movement",caster.position().distanceTo(start)>.3&&caster.position().distanceTo(start)<=6.1,"actual safe displacement="+caster.position().distanceTo(start));}
  else{check(key+"/damage",Math.abs(500-normal.getHealth()-m.damage())<.02,"actual HP="+normal.getHealth()+" budget="+m.damage());check(key+"/attribution",attributed,"native accepted damage credited to owner");}
  check(key+"/ally",ally.getHealth()==500&&!ImuCommand.active(ally),"ally untouched");check(key+"/refused",refused.getHealth()==500&&!ImuCommand.active(refused),"refused hit has no control");check(key+"/outside",outside.getHealth()==500,"out of range");check(key+"/bounded",ZoanTechniqueEpisodes.activeCount()==0&&ZoanTechniqueEpisodes.shotCount()==0&&!ZoanTechniqueEpisodes.guard(caster),"casts/projectiles/guard end on native ticks");
 }
 private static void clean(){ZoanTechniqueEpisodes.clear();ZoanForms.clear();for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){
  for(int f=0;f<FRUITS.length;f++){
   fruit(FRUITS[f]);MinepieceModVariables.get(caster).PlayerEnergy=0;check("core/no-energy-"+NAMES[f],!cast("shigan")&&ZoanTechniqueEpisodes.activeCount()==0,"no cost or effect");fruit(FRUITS[f]);boolean accepted=cast("shigan");DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/reset-"+NAMES[f],accepted&&ZoanTechniqueEpisodes.activeCount()==0,"cancel before contact");fruit(FRUITS[f]);accepted=cast("shigan");MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);ZoanTechniqueEpisodes.tick();check("core/fruit-change-"+NAMES[f],accepted&&ZoanTechniqueEpisodes.activeCount()==0,"exact fruit authority");
   fruit(FRUITS[f]);arena();double energy=MinepieceModVariables.get(caster).PlayerEnergy;world.setBlockAndUpdate(new BlockPos(680,101,80),Blocks.OBSIDIAN.defaultBlockState());check("form/room-"+NAMES[f],!ZoanForms.cycle(caster)&&MinepieceModVariables.get(caster).PlayerEnergy==energy,"refusal before debit");world.setBlockAndUpdate(new BlockPos(680,101,80),Blocks.AIR.defaultBlockState());
   double speed=caster.getAttributeValue(Attributes.MOVEMENT_SPEED);check("form/hybrid-"+NAMES[f],ZoanForms.cycle(caster)&&ZoanForms.form(caster)==1,"hybrid first");check("form/beast-"+NAMES[f],ZoanForms.cycle(caster)&&ZoanForms.form(caster)==2&&ZoanForms.body(caster)!=null,"complete native beast");var body=ZoanForms.body(caster);check("form/dimensions-"+NAMES[f],body!=null&&Math.abs(caster.getBbWidth()-body.width())<.01&&Math.abs(caster.getBbHeight()-body.height())<.01&&Math.abs(caster.getEyeHeight()-body.eye())<.01,"native width/height/eyes="+caster.getBbWidth()+"/"+caster.getBbHeight()+"/"+caster.getEyeHeight());check("form/base-"+NAMES[f],ZoanForms.cycle(caster)&&ZoanForms.form(caster)==0&&Math.abs(caster.getScale()-1)<.001&&caster.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed,"all own modifiers restored");
   var p=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes(("Batch06Form"+f).getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch06Form"+f));var v=MinepieceModVariables.get(p);v.PlayerDevilFruit=new ItemStack(FRUITS[f]);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;p.setHealth(20);p.teleportTo(680.5+f*6,100,100.5);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;check("life/begin-"+NAMES[f],ZoanForms.cycle(p),"maintained native form");forms.add(p);
  }
  fruit(FRUITS[4]);caster.getAbilities().mayfly=true;check("form/foreign-flight",ZoanForms.cycle(caster)&&!MinepieceModVariables.get(caster).zoanFlightGranted,"existing foreign mayfly untouched");ZoanForms.end(caster);check("form/foreign-flight-retained",caster.getAbilities().mayfly,"foreign grant survives form end");caster.getAbilities().mayfly=false;
  fruit(FRUITS[4]);check("form/owned-flight",ZoanForms.cycle(caster)&&caster.getAbilities().mayfly&&MinepieceModVariables.get(caster).zoanFlightGranted&&!caster.getAbilities().flying,"owned grant, double jump required");ZoanForms.end(caster);check("form/owned-flight-release",!caster.getAbilities().mayfly&&!MinepieceModVariables.get(caster).zoanFlightGranted,"only own grant revoked");
   for(int f=0;f<FRUITS.length;f++){fruit(FRUITS[f]);var pos=new BlockPos(680,100,82);world.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(pos);chest.setItem(0,new ItemStack(Items.DIAMOND,7));for(var move:DevilFruitRegistry.movesFor(FRUITS[f]))TechniqueTerrain.begin(caster,move).impact(new Vec3(680.5,100.5,81.99));check("core/chest-"+NAMES[f],world.getBlockEntity(pos)==chest&&chest.getItem(0).getCount()==7,"inventory identity and contents survive");world.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}
   fruit(FRUITS[0]);normal=mob(680.5,86);caster.addEffect(new MobEffectInstance(MobEffects.SPEED,600,2));check("core/pursuit-start",WolfPursuit.begin(caster,normal)&&!WolfPursuit.begin(caster,normal),"one active pursuit, no refill");WolfPursuit.end(caster,"test");check("core/pursuit-foreign-speed",caster.hasEffect(MobEffects.SPEED)&&caster.getEffect(MobEffects.SPEED).getAmplifier()==2,"foreign potion retained");WolfPursuit.begin(caster,normal);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);WolfPursuit.tick(caster);check("core/pursuit-fruit-change",!WolfPursuit.isPursuing(caster),"exact fruit authority");normal.discard();
   fruit(FRUITS[4]);normal=mob(680.5,82.5);normal.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,600,1));check("life/guard-cast",cast("kamiezanshin"),"actual dispatcher counter stance");
   cancelCaster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch06Cancel".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch06Cancel"));var v=MinepieceModVariables.get(cancelCaster);v.PlayerDevilFruit=new ItemStack(FRUITS[0]);v.PlayerEnergy=v.PlayerEnergyMAX=2000;v.UseDevilAbilities=true;v.PlayerTechniqueUsedNumber=2;cancelCaster.setHealth(20);cancelCaster.teleportTo(716.5,100,100.5);cancelCaster.setYRot(0);cancelCaster.setXRot(0);cancelCaster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);check("life/projectile-cast",DevilFruitSkillDispatcher.castSkill(cancelCaster),"actual launched-shot cancellation fixture");
 }
 private static void lifeTick(){
  ++life;
  if(life==15){check("life/projectile-live",ZoanTechniqueEpisodes.shotCount()>0,"actual shot after charge");MinepieceModVariables.get(cancelCaster).PlayerDevilFruit=new ItemStack(Items.APPLE);}
  if(life==18)check("life/projectile-cancelled",ZoanTechniqueEpisodes.shotCount()==0,"already-launched shot loses authority");
  if(life==30){Vec3 at=caster.position();boolean dodged=ZoanTechniqueEpisodes.dodge(caster,world.damageSources().mobAttack(normal));check("life/guard-service-counter",dodged&&caster.position().distanceTo(at)>.25&&ZoanCounterState.active(normal)&&normal.getAttributeValue(Attributes.MOVEMENT_SPEED)==0,"counter service moves caster and immobilizes attacker; Fabric FakePlayer is always immune, real incoming-damage hook must be tested by graphical client");}
  if(life==95)check("life/guard-expiry",!ZoanTechniqueEpisodes.guard(caster)&&!ZoanCounterState.active(normal)&&normal.hasEffect(MobEffects.SLOWNESS),"guard/control expire, foreign slow remains");
  if(life<1210)return;
  for(int f=0;f<forms.size();f++){var p=forms.get(f);check("life/expiry-"+NAMES[f],!ZoanForms.has(p)&&MinepieceModVariables.get(p).player_form==0&&Math.abs(p.getScale()-1)<.001,"native1200ticks form expiry");DevilFruitPowerLogic.resetDevilFruitPowers(p);MinepieceModVariables.clear(p);p.discard();}forms.clear();DevilFruitPowerLogic.resetDevilFruitPowers(cancelCaster);MinepieceModVariables.clear(cancelCaster);cancelCaster.discard();clean();end();
 }
 private static void end(){active=false;if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}org.slf4j.LoggerFactory.getLogger("Batch06").info("[dbatch06] END passed={} failed={} total={}",passed,failed,passed+failed);}
 private static Mob nativeAttacker;
 private static int nativeCounter(CommandSourceStack source,String stage){if(!Boolean.getBoolean("opworldlike.visualLab"))return 0;var p=source.getPlayer();if(p==null||!p.getName().getString().equals("FruitLab")||!DevilFruitPowerLogic.getDevilFruit(p).is(FRUITS[4]))return 0;var level=(ServerLevel)p.level();boolean ok=false;String detail="";
  if(stage.equals("baseline")){nativeAttacker=EntityTypes.CREEPER.spawn(level,p.blockPosition().offset(0,0,3),EntitySpawnReason.COMMAND);if(nativeAttacker==null)return 0;nativeAttacker.setNoAi(true);nativeAttacker.setPos(p.getX(),p.getY(),p.getZ()+3);nativeAttacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,600,1));p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.setHealth(20);p.damageCooldownTime=0;p.setInvulnerableTime(0);boolean accepted=p.hurtServer(level,level.damageSources().mobAttack(nativeAttacker),4);float loss=20-p.getHealth();ok=accepted&&loss>0;detail="real-player unguarded damage="+loss;p.setHealth(20);p.damageCooldownTime=0;p.setInvulnerableTime(0);p.setDeltaMovement(Vec3.ZERO);p.connection.teleport(8.5,100,8.5,0,0);}
  if(stage.equals("hit")&&nativeAttacker!=null){Vec3 at=p.position();p.damageCooldownTime=0;p.setInvulnerableTime(0);float hp=p.getHealth();boolean accepted=p.hurtServer(level,level.damageSources().mobAttack(nativeAttacker),4);ok=!accepted&&p.getHealth()==hp&&p.position().distanceTo(at)>.25&&ZoanCounterState.active(nativeAttacker)&&nativeAttacker.getAttributeValue(Attributes.MOVEMENT_SPEED)==0;detail="accepted="+accepted+" displacement="+p.position().distanceTo(at)+" stun="+ZoanCounterState.active(nativeAttacker);p.setHealth(20);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);}
  if(stage.equals("expiry")&&nativeAttacker!=null){ok=!ZoanTechniqueEpisodes.guard(p)&&!ZoanCounterState.active(nativeAttacker)&&nativeAttacker.hasEffect(MobEffects.SLOWNESS);detail="real guard and counter expiry, foreign slow retained";nativeAttacker.discard();nativeAttacker=null;}
  org.slf4j.LoggerFactory.getLogger("Batch06Native").info("[batch06-native] counter-{} {} {}",stage,ok?"PASS":"FAIL",detail);return ok?1:0;
 }
}
