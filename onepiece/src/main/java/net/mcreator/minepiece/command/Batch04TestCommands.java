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
public final class Batch04TestCommands {
 private static final Item[] FRUITS={MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO,MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO,MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO,MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO,MinepieceModItems.ITEM_DEVIL_FRUIT_BISU_BISU};
 private static final String[] NAMES={"aro","horo","mero","kiro","bisu"};
 private static final Set<String> UTILITY=Set.of("passive","biscuit","weightmanipulation"),SUMMON=Set.of("kumac","biscuitwarrior","biscuitwarrior2");
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();private static final List<LivingEntity> fixtures=new ArrayList<>();
 private static ServerLevel world;private static ServerPlayer caster;private static Mob normal,ally,refused,outside;
 private static int scene,age,passed,failed,terrainBefore,life=-1;private static boolean active,attributed;private static Vec3 start;private static double before;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch04").info("[dbatch04] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  dispatcher.register(Commands.literal("dbatch04").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->start(c.getSource(),""))
   .then(Commands.argument("fruits",com.mojang.brigadier.arguments.StringArgumentType.greedyString()).executes(c->start(c.getSource(),com.mojang.brigadier.arguments.StringArgumentType.getString(c,"fruits")))));
  ServerTickEvents.END_SERVER_TICK.register(server->{if(!active)return;try{
   if(life>=0){lifeTick();return;}
   age++;caster.getCooldowns().tick();if(normal.getHealth()<500 && normal.getLastHurtByMob()==caster)attributed=true;
   var m=moves.get(scene/5);String id=m.id(),key=names.get(scene/5)+"/"+id;
   if(age==3 && scene%5==0)check(key+"/telegraph",normal.getHealth()==500,"no hit during windup");
   if(age==115 && (id.equals("minihollow") || id.equals("rap"))){if(scene%5==0)check(key+"/waiting",normal.getHealth()==500 && ParameciaBatchTechniques.miniCount(caster)==3,"native count="+ParameciaBatchTechniques.miniCount(caster)+", HP="+normal.getHealth());double energy=MinepieceModVariables.get(caster).PlayerEnergy;boolean accepted=cast("rap");if(scene%5==0)check(key+"/command",accepted && MinepieceModVariables.get(caster).PlayerEnergy==energy-40,"native Rap command costs40, no speed potion");}
   if(age==18 && id.equals("combo") && scene%5==0)check(key+"/first-contact",normal.getHealth()==482,"18HP first phase; second contact pending");
   if(age==24 && id.equals("biscuit") && scene%5==0)check(key+"/three-claps",countBiscuits()==3,"three individually delivered biscuit items");
   if(age<200)return;finish();clean();scene++;age=0;if(scene==moves.size()*5){core();life=0;lifeStart();}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();end();}});
 }
 private static int start(CommandSourceStack source,String filter){if(active)return 0;fruits.clear();moves.clear();names.clear();for(int f=0;f<FRUITS.length;f++)if(filter.isEmpty() || Arrays.asList(filter.split(",")).contains(NAMES[f]))for(var m:DevilFruitRegistry.movesFor(FRUITS[f])){fruits.add(FRUITS[f]);moves.add(m);names.add(NAMES[f]);}
  if(moves.isEmpty())return 0;world=source.getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch04Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch04Acceptance"));passed=failed=scene=age=0;life=-1;active=true;begin();return 1;
 }
 private static void fruit(Item f){DevilFruitPowerLogic.resetDevilFruitPowers(caster);var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(f);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;caster.getInventory().clearContent();caster.removeAllEffects();caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);caster.getAbilities().invulnerable=false;caster.setHealth(20);caster.teleportTo(680.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);}
 private static Mob mob(double x,double z){var e=EntityTypes.CREEPER.spawn(world,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);if(e==null)throw new IllegalStateException("spawn");e.setNoAi(true);e.setNoGravity(true);e.setPos(x,100,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);fixtures.add(e);return e;}
 private static boolean cast(String id){var list=DevilFruitRegistry.movesFor(MinepieceModVariables.get(caster).PlayerDevilFruit.getItem());for(int i=0;i<list.size();i++)if(list.get(i).id().equals(id)){MinepieceModVariables.get(caster).PlayerTechniqueUsedNumber=i;return DevilFruitSkillDispatcher.castSkill(caster);}return false;}
 private static void arena(){for(int x=674;x<=689;x++)for(int z=77;z<=105;z++){for(int y=95;y<=99;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}
 private static int countBiscuits(){int n=0;for(var item:caster.getInventory())if(item.is(MinepieceModItems.BISCUIT))n+=item.getCount();return n;}
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
  attributed=false;terrainBefore=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id);before=MinepieceModVariables.get(caster).PlayerEnergy;boolean accepted=cast(id.equals("rap")?"minihollow":id);String key=names.get(i)+"/"+id;
  if(scene%5==0){check(key+"/cost",accepted && MinepieceModVariables.get(caster).PlayerEnergy==before-(id.equals("rap")?50:moves.get(i).energyCost()),"accepted debit once; Rap scene first seeds Mini Hollow through dispatcher");double energy=MinepieceModVariables.get(caster).PlayerEnergy;check(key+"/cooldown",!cast(id) && energy==MinepieceModVariables.get(caster).PlayerEnergy,"spam refuses without debit");check(key+"/animation",TechniqueAnimations.nameFor(moves.get(i),caster).equals("b04_"+names.get(i)+"_"+id) && TechniqueAnimations.fichierPresent("b04_"+names.get(i)+"_"+id),"owned finite phase delivered");check(key+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"victim inserted in ticking chunk");}
 }
 private static boolean controlled(LivingEntity e){return ParameciaControl.active(e,ParameciaControl.Kind.STONE)||ParameciaControl.active(e,ParameciaControl.Kind.NEGATIVE);}
 private static void finish(){int i=scene/5;var m=moves.get(i);String id=m.id(),key=names.get(i)+"/"+id;
  if(scene%5>=3){var t=scene%5==3?ally:refused;check(key+(scene%5==3?"/ally-on-axis":"/refused-on-axis"),t.getHealth()==500 && !controlled(t),"sole trajectory victim HP="+t.getHealth());return;}
  if(scene%5==1){check(key+"/wall",normal.getHealth()==500 && !controlled(normal),"solid wall protects; HP="+normal.getHealth());return;}
  if(scene%5==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int broken=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;boolean destructive=policy!=null && policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE && !id.equals("weightmanipulation");check(key+"/terrain",policy!=null && (destructive?broken>0 && broken<=policy.blocks():broken==0),"native changed="+broken+" policy="+policy);return;}
  if(UTILITY.contains(id))check(key+"/peaceful",normal.getHealth()==500,"utility has no direct damage");
  else if(SUMMON.contains(id))check(key+"/damage",normal.getHealth()<500 && ParameciaBatchTechniques.summonCount(caster)==1,"native summon AI hit; HP="+normal.getHealth());
  else{double expected=id.equals("combo")?38:id.equals("rap")?10:id.equals("attackjump")?15:m.damage();check(key+"/damage",Math.abs(500-normal.getHealth()-expected)<.02,"HP="+normal.getHealth()+" expected="+expected);}
  if(!UTILITY.contains(id) && !SUMMON.contains(id))check(key+"/attribution",attributed,"native accepted hit attributed during100tick window");
  if(id.equals("passive") || id.equals("move") || id.equals("perfumefemur"))check(key+"/movement",caster.position().distanceTo(start)>2 && caster.position().distanceTo(start)<=5.1,"native distance="+caster.position().distanceTo(start));
  check(key+"/ally",ally.getHealth()==500 && !controlled(ally),"ally untouched");check(key+"/refused",refused.getHealth()==500 && !controlled(refused),"refused damage has no control");check(key+"/outside",outside.getHealth()==500,"out of range untouched");check(key+"/bounded",ParameciaBatchTechniques.activeCount()==0 && ParameciaBatchTechniques.shotCount()==0 && ParameciaBatchTechniques.summonCount(caster)<=1,"finite casts and projectiles");
 }
 private static void clean(){ParameciaBatchTechniques.clearAll();ParameciaControl.clear();for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){
  for(int f=0;f<FRUITS.length;f++){fruit(FRUITS[f]);String id=DevilFruitRegistry.movesFor(FRUITS[f]).getFirst().id();MinepieceModVariables.get(caster).PlayerEnergy=0;check("core/no-energy-"+NAMES[f],!cast(id) && ParameciaBatchTechniques.activeCount()==0,"no effect/debit");fruit(FRUITS[f]);boolean accepted=cast(id);DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/reset-"+NAMES[f],accepted && ParameciaBatchTechniques.activeCount()==0 && ParameciaBatchTechniques.shotCount()==0,"pending charge cancelled");fruit(FRUITS[f]);accepted=cast(id);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);ParameciaBatchTechniques.tick();check("core/fruit-change-"+NAMES[f],accepted && ParameciaBatchTechniques.activeCount()==0,"exact fruit binding");fruit(FRUITS[f]);accepted=cast(id);caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));ParameciaBatchTechniques.tick();check("core/suppression-"+NAMES[f],accepted && ParameciaBatchTechniques.activeCount()==0,"suppression cancels");
   fruit(FRUITS[f]);var pos=new BlockPos(680,100,82);world.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(pos);chest.setItem(0,new ItemStack(Items.DIAMOND,7));for(var move:DevilFruitRegistry.movesFor(FRUITS[f]))TechniqueTerrain.begin(caster,move).impact(new Vec3(680.5,100.5,81.99));check("core/chest-"+NAMES[f],world.getBlockEntity(pos)==chest && chest.getItem(0).getCount()==7,"exact inventory preserved");world.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
  }
  fruit(FRUITS[3]);double speed=caster.getAttributeValue(Attributes.MOVEMENT_SPEED),gravity=caster.getAttributeValue(Attributes.GRAVITY);WeightState.request(caster,50);double weight=WeightState.current(caster);check("core/weight-finite",WeightState.request(caster,Double.NaN)==weight && WeightState.request(caster,Double.POSITIVE_INFINITY)==weight,"nonfinite requests rejected without modifier change");for(int n=0;n<20;n++)WeightState.request(caster,n%2==0?.1:100);WeightState.request(caster,1);check("core/weight-no-stack",caster.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed && caster.getAttributeValue(Attributes.GRAVITY)==gravity,"normal restores exact original attributes");WeightState.request(caster,100);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);WeightState.tick(caster);check("core/weight-fruit-release",WeightState.current(caster)==1 && caster.getAttributeValue(Attributes.GRAVITY)==gravity,"invalidated owner cannot retain gravity");
  fruit(FRUITS[3]);WeightState.request(caster,100);int impacts=WeightSlam.impactCount(caster);caster.causeFallDamage(5d,0,world.damageSources().fall());check("core/weight-native-landing",WeightSlam.impactCount(caster)==impacts+1 && WeightSlam.lastImpact(caster)>0,"Player.causeFallDamage hook before native reset");int once=WeightSlam.impactCount(caster);caster.causeFallDamage(5d,0,world.damageSources().fall());check("core/weight-one-impact",WeightSlam.impactCount(caster)==once,"same fall cannot repeat");check("core/weight-invalid-height",WeightSlam.tenter(caster,Double.NaN)==0 && WeightSlam.tenter(caster,Double.POSITIVE_INFINITY)==0,"nonfinite damage inputs rejected");
  fruit(FRUITS[2]);normal=mob(680.5,83.5);normal.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,600,1));normal.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,600,1));boolean applied=ParameciaControl.apply(caster,normal,ParameciaControl.Kind.STONE);int left=ParameciaControl.remaining(normal);for(int n=0;n<20;n++)ParameciaControl.apply(caster,normal,ParameciaControl.Kind.STONE);check("core/control-no-refresh",applied && ParameciaControl.remaining(normal)==left,"repeat cannot reset maximum");for(int n=0;n<81;n++)ParameciaControl.tick();check("core/control-foreign-preserved",!controlled(normal) && normal.hasEffect(MobEffects.SLOWNESS) && normal.getEffect(MobEffects.SLOWNESS).getAmplifier()==1 && normal.hasEffect(MobEffects.RESISTANCE),"expiry removes owned attributes only");normal.discard();
  fruit(FRUITS[1]);double energy=MinepieceModVariables.get(caster).PlayerEnergy;check("core/empty-rap-refund",!cast("rap") && MinepieceModVariables.get(caster).PlayerEnergy==energy,"empty command refuses with debit refund");clean();
 }
 private static void lifeStart(){clean();arena();age=0;fruit(life==0?FRUITS[4]:life==1?FRUITS[1]:life==2?FRUITS[0]:FRUITS[2]);normal=mob(680.5,84.5);if(life==2)normal.setPos(746.5,100,145.5);check("life/cast-"+life,cast(life==0?"biscuitwarrior2":life==1?"minihollow":life==2?"arrow":"pistolkiss"),"native dispatcher starts episode");}
 private static void lifeTick(){age++;caster.getCooldowns().tick();
  if(life==0 && age==50)check("life/soldier-live",ParameciaBatchTechniques.summonCount(caster)==1,"actual inserted warrior before320tick expiry");
  if(life==1 && age==30){check("life/mini-launched",ParameciaBatchTechniques.miniCount(caster)==3,"three launched projectiles before invalidation");MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);}
  if(life==2 && age==12){check("life/arrow-launched",ParameciaBatchTechniques.shotCount()>0,"arrow left charge before death");caster.setHealth(0);}
  if(life==3 && age==45){check("life/stone-live",ParameciaControl.active(normal,ParameciaControl.Kind.STONE) && normal.hasEffect(MinepieceModMobEffects.B04_STONE) && ((ParameciaStoneData)normal).minepiece$stoneFlag(),"real Pistol Kiss applied synced statue marker");double hp=normal.getHealth();boolean denied=!outsideAttack(normal);check("life/stone-cannot-attack",denied && normal.getHealth()==hp,"native hurtServer denies damage sourced by petrified creature");MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);}
  if(age<(life==0?350:60))return;
  if(life==0)check("life/soldier-expiry",ParameciaBatchTechniques.summonCount(caster)==0,"actual entity ticks expired320tick summon");
  if(life==1 || life==2)check("life/projectile-cleanup-"+life,ParameciaBatchTechniques.shotCount()==0 && ParameciaBatchTechniques.activeCount()==0,"native service cancels launched effect on fruit change/death");
  if(life==3)check("life/stone-source-release",!ParameciaControl.active(normal,ParameciaControl.Kind.STONE) && !normal.hasEffect(MinepieceModMobEffects.B04_STONE) && !((ParameciaStoneData)normal).minepiece$stoneFlag(),"invalid owner releases exact statue and synced visual");
  clean();if(++life==4){life=-1;end();}else lifeStart();
 }
 private static boolean outsideAttack(LivingEntity stone){var target=mob(684.5,84.5);boolean result=target.hurtServer(world,world.damageSources().mobAttack(stone),8);target.discard();return result;}
 private static void end(){active=false;if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}org.slf4j.LoggerFactory.getLogger("Batch04").info("[dbatch04] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
