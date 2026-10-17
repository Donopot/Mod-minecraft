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
public final class Batch03TestCommands {
 private static final Item[] FRUITS={MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI,MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI,MinepieceModItems.ITEM_DEVIL_FRUIT_KIRA_KIRA,MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA,MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI};
 private static final String[] NAMES={"mochi","mori","kira","supa","riki"};
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();
 private static final List<Mob> fixtures=new ArrayList<>();private static final Set<String> PEACEFUL=Set.of("mochibuild","diamondbody");
 private static final Set<String> DASH=Set.of("zangirimochi","brilliantpunk","atomicspurt","galleonlariat");
 private static ServerLevel world;private static ServerPlayer caster;private static Mob normal,refused,ally,outside;
 private static int scene,age,passed,failed,extra,lifecycle=-1,lifeDuration;private static double lifeArmor;private static final net.minecraft.resources.Identifier FOREIGN=net.minecraft.resources.Identifier.fromNamespaceAndPath("b03_fixture","external_armor");private static boolean active,roof,attributed;private static Vec3 start;private static double healthBefore;private static int terrainBefore;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch03").info("[dbatch03] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  dispatcher.register(Commands.literal("dbatch03").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->startSuite(c.getSource(),"")).then(Commands.argument("fruit",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c->startSuite(c.getSource(),com.mojang.brigadier.arguments.StringArgumentType.getString(c,"fruit")))));
  ServerTickEvents.END_SERVER_TICK.register(s->{if(!active)return;try{

   if(lifecycle>=0){lifeTick();return;}
   age++;caster.getCooldowns().tick();if(normal!=null && normal.getHealth()<500 && normal.getLastHurtByMob()==caster)attributed=true;int i=scene/5;var m=moves.get(i);String prefix=names.get(i)+"/"+m.id();
   if(age==3 && scene%5==0 && !PEACEFUL.contains(m.id()))check(prefix+"/telegraph",normal.getHealth()==500,"no premature hit");
   if(age==15 && scene%5==0 && m.id().equals("mochigatling"))check(prefix+"/cadence-early",normal.getHealth()==496,"first4HP hit only, later pulses pending");
   if(age==120 && scene%5==0 && m.id().equals("musodonatsu"))check(prefix+"/no-autonomous-damage",normal.getHealth()==500 && MaterialBatchTechniques.ringCount(caster)==2,"two rings wait for a new owner gesture");
   if(age==150 && m.id().equals("musodonatsu")){boolean next=cast(scene%5==2?"yakimochi":"mochipistol");if(scene%5==0)check(prefix+"/gesture",next,"normal recharge elapsed; follow-up caster gesture accepted");}
   if(age<(m.id().equals("musodonatsu")?250:140))return;finish();clean();scene++;age=0;if(scene==moves.size()*5){core();lifecycle=0;lifeBegin();}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();end();}});
 }
 private static int startSuite(CommandSourceStack source,String filter){if(active)return 0;fruits.clear();moves.clear();names.clear();for(int i=0;i<FRUITS.length;i++)if(filter.isEmpty() || filter.equals(NAMES[i]))for(var m:DevilFruitRegistry.movesFor(FRUITS[i])){fruits.add(FRUITS[i]);moves.add(m);names.add(NAMES[i]);}if(moves.isEmpty())return 0;world=source.getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch03Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch03Acceptance"));passed=failed=scene=age=extra=0;lifecycle=-1;active=true;begin();return 1;}
 private static void fruit(Item item){DevilFruitPowerLogic.resetDevilFruitPowers(caster);var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(item);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;caster.removeAllEffects();caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);caster.getAbilities().invulnerable=false;caster.setHealth(20);caster.teleportTo(680.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);}
 private static Mob mob(double x,double z){var e=EntityTypes.HUSK.spawn(world,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);if(e==null)throw new IllegalStateException("spawn");e.setBaby(false);e.setNoAi(true);e.setNoGravity(true);e.setPos(x,100,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);fixtures.add(e);return e;}
 private static boolean cast(String id){var list=DevilFruitRegistry.movesFor(MinepieceModVariables.get(caster).PlayerDevilFruit.getItem());for(int i=0;i<list.size();i++)if(list.get(i).id().equals(id)){MinepieceModVariables.get(caster).PlayerTechniqueUsedNumber=i;return DevilFruitSkillDispatcher.castSkill(caster);}return false;}
 private static void arena(){for(int x=656;x<=709;x++)for(int z=76;z<=140;z++){world.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());for(int y=94;y<=99;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());for(int y=100;y<135;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}
 private static void begin(){int i=scene/5;fruit(fruits.get(i));arena();start=caster.position();String id=moves.get(i).id();double distance=4;
  normal=mob(680.5,80.5+distance);refused=mob(682.5,80.5+distance);refused.setPermanentlyInvulnerable(true);outside=mob(746.5,145.5);
  var summon=new SummonEntity(MinepieceModEntities.SUMMON,world);summon.setOwnerEntity(caster);summon.setNoAi(true);summon.setNoGravity(true);summon.lifetime(200);summon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);summon.setHealth(500);summon.setPos(679.5,100,80.5+distance);world.addFreshEntity(summon);ally=summon;fixtures.add(summon);

  if(scene%5==1)for(int x=656;x<=709;x++)for(int y=100;y<135;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.OBSIDIAN.defaultBlockState());
  if(scene%5==2)for(int x=656;x<=709;x++)for(int y=100;y<=105;y++)world.setBlockAndUpdate(new BlockPos(x,y,83),Blocks.STONE.defaultBlockState());
  attributed=false;
  if(scene%5==3){normal.setPos(746.5,100,145.5);ally.setPos(680.5,100,84.5);}
  if(scene%5==4){normal.setPos(746.5,100,145.5);refused.setPos(680.5,100,84.5);}
  terrainBefore=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id);

  double before=MinepieceModVariables.get(caster).PlayerEnergy;boolean result=cast(id);String prefix=names.get(i)+"/"+id;
  if(scene%5==0){check(prefix+"/cost",result && Math.abs(MinepieceModVariables.get(caster).PlayerEnergy-(before-moves.get(i).energyCost()))<.001,"one debit");double debit=MinepieceModVariables.get(caster).PlayerEnergy;check(prefix+"/cooldown",!cast(id) && debit==MinepieceModVariables.get(caster).PlayerEnergy,"spam refused without debit");check(prefix+"/animation",TechniqueAnimations.nameFor(moves.get(i),caster).equals("b03_"+names.get(i)+"_"+id) && TechniqueAnimations.fichierPresent("b03_"+names.get(i)+"_"+id),"owned phase file");check(prefix+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"victim actually inserted and ticking");}
 }
 private static void finish(){int i=scene/5;String id=moves.get(i).id(),prefix=names.get(i)+"/"+id;
  if(scene%5>=3){var target=scene%5==3?ally:refused;check(prefix+(scene%5==3?"/ally-on-axis":"/refused-on-axis"),target.getHealth()==500 && !Roots.isHeld(target) && !StickyState.isSticky(target),"sole victim on attack trajectory; HP="+target.getHealth());return;}
  if(scene%5==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int changed=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;int grass=0,honey=0;
   for(int x=656;x<=709;x++)for(int z=76;z<=140;z++){if(world.getBlockState(new BlockPos(x,99,z)).is(Blocks.GRASS_BLOCK))grass++;for(int y=100;y<=104;y++)if(world.getBlockState(new BlockPos(x,y,z)).is(Blocks.HONEY_BLOCK))honey++;}
   check(prefix+"/terrain",policy!=null && (policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE?changed>0 && changed<=policy.blocks():policy.mode()==TechniqueTerrain.Mode.TRANSFORM?(id.equals("mochibuild")?honey>0 && honey<=24:grass>0 && grass<=128):changed==0 && grass==0 && honey==0),"physical broken="+changed+", grass="+grass+", honey="+honey+", policy="+policy);return;}

  if(scene%5==1){check(prefix+"/wall",normal.getHealth()==500 && !Roots.isHeld(normal) && !StickyState.isSticky(normal),"wall protects damage and offensive state");if(DASH.contains(id))check(prefix+"/body-wall",caster.getZ()<82.71,"full body stops before wall");return;}
  if(!PEACEFUL.contains(id)){double expected=id.equals("mochigatling")?24:id.equals("musodonatsu")?20:moves.get(i).damage();check(prefix+"/damage",Math.abs(500-normal.getHealth()-expected)<.01,"HP="+normal.getHealth()+", expected loss="+expected);check(prefix+"/attribution",attributed,"native attribution recorded during actual hit, before vanilla100tick expiry");}
  else check(prefix+"/peaceful",normal.getHealth()==500,"utility has no damage");
  if(DASH.contains(id))check(prefix+"/movement",caster.position().distanceTo(start)>2 && caster.position().distanceTo(start)<8.1,"position="+caster.position());
  if(id.equals("musodonatsu"))check(prefix+"/rings-follow",MaterialBatchTechniques.ringCount(caster)==2,"rings remain active until source TTL or owner cancellation");
  if(id.equals("diamondbody"))check(prefix+"/body-active",DiamondBody.isActive(caster) && caster.getAttributeValue(Attributes.ARMOR)>0,"scoped coat active without player_form");
  if(id.equals("mochigatling"))check(prefix+"/six-hit-budget",Math.abs(normal.getHealth()-476)<.01,"six accepted4HP pulses on native ticks, HP="+normal.getHealth());
  check(prefix+"/ally",ally.getHealth()==500,"ally intact");check(prefix+"/refused",refused.getHealth()==500 && !Roots.isHeld(refused) && !StickyState.isSticky(refused),"invulnerable intact");check(prefix+"/outside",outside.getHealth()==500,"outside intact");check(prefix+"/bounded",MaterialBatchTechniques.activeCount()==0 && MaterialBatchTechniques.shotCount()==0,"finite action/projectiles/waves");
 }
 private static void clean(){MaterialBatchTechniques.clearAll();StickyState.releaseEverything("test");Roots.releaseEverything("test");for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){arena();
  for(int f=0;f<FRUITS.length;f++){
   fruit(FRUITS[f]);String id=DevilFruitRegistry.movesFor(FRUITS[f]).getFirst().id();MinepieceModVariables.get(caster).PlayerEnergy=0;
   check("core/no-energy-"+NAMES[f],!cast(id) && MaterialBatchTechniques.activeCount()==0,"refusal has no new episode");
   fruit(FRUITS[f]);boolean ok=cast(id);DevilFruitPowerLogic.resetDevilFruitPowers(caster);
   check("core/reset-"+NAMES[f],ok && MaterialBatchTechniques.activeCount()==0 && MaterialBatchTechniques.shotCount()==0 && MaterialBatchTechniques.ringCount(caster)==0 && !SupaBlades.bladesActive(caster) && !DiamondBody.isActive(caster),"immediate owner cleanup");
   fruit(FRUITS[f]);ok=cast(id);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);MaterialBatchTechniques.tick();
   check("core/fruit-change-"+NAMES[f],ok && MaterialBatchTechniques.activeCount()==0,"old fruit episode stopped");
   fruit(FRUITS[f]);ok=cast(id);caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,80,0));MaterialBatchTechniques.tick();
   check("core/suppression-"+NAMES[f],ok && MaterialBatchTechniques.activeCount()==0,"suppression stops charge before damage");caster.removeAllEffects();
  }
  fruit(FRUITS[3]);caster.addEffect(new MobEffectInstance(MobEffects.STRENGTH,600,2));caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,600,1));
  SupaBlades.activate(caster,8);for(int j=0;j<6;j++)SupaBlades.setStance(caster,j%2==0?SupaBlades.Stance.OFFENSIVE:SupaBlades.Stance.DEFENSIVE);
  check("core/stance-exclusive",SupaBlades.stanceEffectsOn(caster)==1,"only one owned modifier despite two external potions");
  for(int j=0;j<10;j++)SupaBlades.tickBlades();
  check("core/stance-expiry",!SupaBlades.bladesActive(caster) && SupaBlades.stanceEffectsOn(caster)==0 && caster.hasEffect(MobEffects.STRENGTH) && caster.getEffect(MobEffects.STRENGTH).getAmplifier()==2 && caster.hasEffect(MobEffects.RESISTANCE),"expiry removes owned posture, preserves both potions");caster.removeAllEffects();
  fruit(FRUITS[0]);normal=mob(680.5,84.5);StickyState.stick(normal,caster);for(int j=0;j<20;j++){StickyState.tickAll();StickyState.stick(normal,caster);}check("core/sticky-bounded-refresh",StickyState.remaining(normal)<=80,"refresh cannot reset100tick maximum");
  MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);StickyState.tickAll();check("core/sticky-fruit-release",!StickyState.isSticky(normal),"source fruit identity revalidated on native service tick");normal.discard();
  fruit(FRUITS[1]);normal=mob(680.5,84.5);Roots.entangle(normal,caster);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);Roots.tickHeld();check("core/root-fruit-release",!Roots.isHeld(normal),"source fruit identity revalidated");normal.discard();
  fruit(FRUITS[2]);caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,600,0));double armor=caster.getAttributeValue(Attributes.ARMOR);DiamondBody.toggle(caster);double bonus=caster.getAttributeValue(Attributes.ARMOR)-armor;
  check("core/diamond-cap",bonus<=br.com.oneminepiece.opworldlike.kira.DevilFruitKiraConfig.maxReductionPercent()/4.0+.001,"bonus bounded against native armor/25 upper bound");
  MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);DiamondBody.tick(caster);check("core/diamond-fruit-release",!DiamondBody.isActive(caster) && caster.getAttributeValue(Attributes.ARMOR)==armor && caster.hasEffect(MobEffects.NIGHT_VISION),"only own armor removed on change");
  fruit(FRUITS[2]);normal=mob(680.5,84.5);normal.setPermanentlyInvulnerable(true);Vec3 velocity=normal.getDeltaMovement();check("core/refused-no-control",!CombatSkillLogic.hurtAndConfirm(normal,world,world.damageSources().playerAttack(caster),20) && normal.getDeltaMovement().equals(velocity) && !Roots.isHeld(normal) && !StickyState.isSticky(normal),"native refusal remains side-effect free");normal.discard();
  for(int i=0;i<FRUITS.length;i++){fruit(FRUITS[i]);BlockPos at=new BlockPos(680,101,83);world.setBlockAndUpdate(at,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(at);chest.setItem(0,new ItemStack(Items.DIAMOND,7));for(var move:DevilFruitRegistry.movesFor(FRUITS[i]))TechniqueTerrain.begin(caster,move).impact(new Vec3(680.5,101.5,82.99));check("core/chest-"+NAMES[i],world.getBlockEntity(at)==chest && chest.getItem(0).getCount()==7,"all terrain policies preserve the exact inventory");world.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());}
  clean();
 }
 private static void lifeBegin(){clean();arena();age=0;lifeDuration=40;
  switch(lifecycle){
   case 0->{fruit(FRUITS[3]);caster.addEffect(new MobEffectInstance(MobEffects.STRENGTH,600,2));SupaBlades.activate(caster,12);SupaBlades.setStance(caster,SupaBlades.Stance.DEFENSIVE);lifeArmor=caster.getAttributeValue(Attributes.ARMOR)-5;}
   case 1->{fruit(FRUITS[2]);lifeArmor=caster.getAttributeValue(Attributes.ARMOR);caster.getAttribute(Attributes.ARMOR).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(FOREIGN,2,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));DiamondBody.begin(caster);lifeDuration=DiamondBody.remainingTicks(caster)+3;}
   case 2->{fruit(FRUITS[2]);DiamondBody.begin(caster);}
   case 3->{fruit(FRUITS[3]);SupaBlades.activate(caster,200);SupaBlades.setStance(caster,SupaBlades.Stance.OFFENSIVE);}
   case 4,5,6->{fruit(FRUITS[0]);cast("yakimochi");}
   case 7->{fruit(FRUITS[0]);cast("musodonatsu");lifeDuration=1230;}
   case 8->{fruit(FRUITS[1]);normal=mob(680.5,100.5);cast("rootsurround");lifeDuration=60;}
   case 9->{fruit(FRUITS[1]);int changed=TechniqueTerrain.begin(caster,"root").replaceSurface(caster.position(),2,Blocks.GRASS_BLOCK.defaultBlockState());extra=changed;check("life/grass-positive",changed>0,"real selective surface mutation");DevilFruitPowerLogic.resetDevilFruitPowers(caster);lifeDuration=230;}
   default->{end();return;}
  }
 }
 private static void lifeTick(){age++;caster.getCooldowns().tick();
  if(age==3 && lifecycle==2)caster.setHealth(0);
  if(age==3 && lifecycle==3)MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);
  if(age==12 && lifecycle>=4 && lifecycle<=6){check("life/shot-launched-"+lifecycle,MaterialBatchTechniques.shotCount()>0,"projectile really left the charge before invalidation");if(lifecycle==4)MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);if(lifecycle==5)caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100,0));if(lifecycle==6)caster.setHealth(0);}
  if(age==100 && lifecycle==7)check("life/rings-live",MaterialBatchTechniques.ringCount(caster)==2,"two native rings before1200tick TTL");
  if(age==24 && lifecycle==8)normal.setPos(680.5,100,84.5);
  if(age<lifeDuration)return;
  switch(lifecycle){
   case 0->check("life/supa-native-expiry",!SupaBlades.bladesActive(caster) && SupaBlades.stanceEffectsOn(caster)==0 && caster.hasEffect(MobEffects.STRENGTH) && caster.getEffect(MobEffects.STRENGTH).getAmplifier()==2 && caster.getAttributeValue(Attributes.ARMOR)==lifeArmor,"real server ticks expire posture and preserve foreign potion");
   case 1->{check("life/diamond-native-expiry",!DiamondBody.isActive(caster) && caster.getAttribute(Attributes.ARMOR).getModifier(FOREIGN)!=null && caster.getAttributeValue(Attributes.ARMOR)==lifeArmor+2,"native expiry preserves the exact foreign armor modifier");caster.getAttribute(Attributes.ARMOR).removeModifier(FOREIGN);}
   case 2->check("life/diamond-death",!DiamondBody.isActive(caster),"native tick retracts corpse coat");
   case 3->check("life/supa-fruit-change",!SupaBlades.bladesActive(caster) && SupaBlades.stanceEffectsOn(caster)==0,"native tick releases old-fruit blade modifier");
   case 4,5,6->check("life/shot-cancelled-"+lifecycle,MaterialBatchTechniques.activeCount()==0 && MaterialBatchTechniques.shotCount()==0,"already launched projectile cancelled on fruit change/suppression/death");
   case 7->check("life/rings-native-expiry",MaterialBatchTechniques.ringCount(caster)==0,"native1200tick expiry, no reset shortcut");
   case 8->check("life/surround-late-arrival",normal.getHealth()==485 && Roots.isHeld(normal),"target arriving after opener receives exactly one15HP budget from following roots");
   case 9->{int grass=0;for(int x=678;x<=682;x++)for(int z=78;z<=82;z++)if(world.getBlockState(new BlockPos(x,99,z)).is(Blocks.GRASS_BLOCK))grass++;check("life/grass-permanent",grass==extra,"grass remains after reset and230native ticks, count="+grass+" expected="+extra);}
  }
  caster.setHealth(20);lifecycle++;lifeBegin();
 }
 private static void end(){active=false;if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}org.slf4j.LoggerFactory.getLogger("Batch03").info("[dbatch03] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
