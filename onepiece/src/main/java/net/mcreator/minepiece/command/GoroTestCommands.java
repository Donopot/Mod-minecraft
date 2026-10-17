package net.mcreator.minepiece.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.*;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.potion.MamaraganMobEffect;
import net.mcreator.minepiece.procedures.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Real-tick disposable fixtures. FakePlayer casts; ordinary inserted adult mobs take damage. */
public final class GoroTestCommands {
   private static final String[] IDS={"vari","elthor","sango","mamaragan","karikrash","passive","raigo"};
   private static final List<Mob> fixtures=new ArrayList<>();
   private static final List<Mob> chain=new ArrayList<>();
   private static ServerLevel level;private static ServerPlayer caster;
   private static Mob normal,refused,outside,ally;private static Vec3 start;
   private static int phase,age,passed,failed;private static boolean active;
   private static void check(String id,boolean ok,String detail) {
      if(ok)passed++;else failed++;
      org.slf4j.LoggerFactory.getLogger("GoroAcceptance").info("[dgoro] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);
   }
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      if(!Boolean.getBoolean("opworldlike.repairTests"))return;
      dispatcher.register(Commands.literal("dgoro").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx->{
         if(active)return 0;passed=failed=phase=age=0;level=ctx.getSource().getServer().overworld();
         caster=FakePlayer.get(level,new GameProfile(UUID.nameUUIDFromBytes("GoroAcceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"GoroAcceptance"));
         for(int x=128;x<=180;x++)for(int z=64;z<=144;z++) {
            level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());
            for(int y=100;y<=120;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
         }
         active=true;begin();return 1;
      }));
      ServerTickEvents.END_SERVER_TICK.register(server->{
         if(!active || level.getServer()!=server)return;
         try {
            age++;
            // FakePlayer is not naturally ticked. Drive the registered effect at its native cadence,
            // once per five REAL ticks; true-player native effect timing is checked in the graphics lab.
            if(age%5==0 && caster.hasEffect(MinepieceModMobEffects.MAMARAGAN))MamaraganMobEffect.tick((ServerLevel)caster.level(),caster,0);
            if(phase<14) {
               String id=IDS[phase/2];
               if(age==5 && phase%2==0 && Set.of("vari","elthor","sango","karikrash").contains(id))
                  check(id+"-telegraph",normal.getHealth()==500,"no damage before minimum charge");
               if(age==25 && phase%2==0 && Set.of("vari","elthor","sango","karikrash").contains(id))
                  check(id+"-slow",normal.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS),"catalogue slow applied after confirmed damage");
               if(age==58 && id.equals("mamaragan") && phase%2==0)
                  check("storm-three-impacts",MamaraganMobEffect.strikesDone(caster.getUUID())==3 && Math.abs(normal.getHealth()-470)<.01,"strikes="+MamaraganMobEffect.strikesDone(caster.getUUID())+", HP="+normal.getHealth());
               if(age<85)return;
               finish();clean();phase++;age=0;
               if(phase==14){extras();active=false;end();return;}begin();
            }
         }catch(Throwable failure){check("fixture-exception",false,failure.toString());clean();active=false;end();}
      });
   }
   private static Mob mob(double x,double z) {
      var e=EntityTypes.HUSK.spawn(level,BlockPos.containing(x,100,z),EntitySpawnReason.COMMAND);
      if(e==null)throw new IllegalStateException("fixture missing");e.setBaby(false);e.setPos(x,100,z);e.setNoAi(true);e.setNoGravity(true);
      e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.setHealth(500);
      e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);fixtures.add(e);return e;
   }
   private static void setup(String id) {
      GoroTechniques.cancel(caster);caster.removeEffect(MinepieceModMobEffects.SEA_POWER);
      var v=MinepieceModVariables.get(caster);v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO);
      v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;
      v.PlayerTechniqueUsedNumber=java.util.stream.IntStream.range(0,7).filter(i->DevilFruitRegistry.movesFor(v.PlayerDevilFruit.getItem()).get(i).id().equals(id)).findFirst().orElseThrow();
      caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));
      caster.teleportTo(152.5,100,80.5);caster.setYRot(0);caster.setXRot(0);caster.setDeltaMovement(Vec3.ZERO);start=caster.position();
   }
   private static void begin() {
      String id=IDS[phase/2];setup(id);boolean wall=phase%2==1;
      normal=mob(152.5,id.equals("vari")?107.5:id.equals("raigo")?100.5:86.5);
      if(id.equals("passive"))normal.setPos(155.5,100,90.5);
      refused=mob(156.5,normal.getZ());refused.setPermanentlyInvulnerable(true);
      if(id.equals("mamaragan"))refused.setPos(153.5,100,normal.getZ());
      outside=mob(176.5,136.5);
      var own=new SummonEntity(MinepieceModEntities.SUMMON,level);own.setOwnerEntity(caster);own.setNoAi(true);own.setNoGravity(true);own.lifetime(200);
      own.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);own.setHealth(500);own.setPos(151.5,100,normal.getZ());level.addFreshEntity(own);ally=own;fixtures.add(own);
      if(id.equals("sango") && !wall) {
         normal.setPos(152.5,100,84.5);chain.add(normal);
         for(int i=1;i<4;i++)chain.add(mob(152.5,84.5+i*3));
         refused.setPos(162.5,100,84.5);
      }
      if(wall) {
         normal.setPos(152.5,100,88.5);
         for(int x=130;x<=174;x++)for(int y=100;y<=120;y++)level.setBlockAndUpdate(new BlockPos(x,y,85),Blocks.OBSIDIAN.defaultBlockState());
      }
      var v=MinepieceModVariables.get(caster);var move=DevilFruitRegistry.movesFor(v.PlayerDevilFruit.getItem()).get(phase/2);
      boolean cast=DevilFruitSkillDispatcher.castSkill(caster);
      if(!wall) {
         check(id+"-cast-cost",cast && Math.abs(v.PlayerEnergy-(2000-move.energyCost()))<.001,"energy="+v.PlayerEnergy);
         double energy=v.PlayerEnergy;
         check(id+"-cooldown",caster.getCooldowns().isOnCooldown(v.PlayerDevilFruit) && !DevilFruitSkillDispatcher.castSkill(caster) && v.PlayerEnergy==energy,"one debit, spam refused");
         String gesture=TechniqueAnimations.nameFor(move,caster);
         check(id+"-gesture",gesture.startsWith("goro_") && TechniqueAnimations.fichierPresent(gesture),gesture);
      }
      GoroTechniques.release(caster);
   }
   private static void finish() {
      String id=IDS[phase/2];
      if(phase%2==1) {
         check(id+"-wall",normal.getHealth()==500,"behind wall HP="+normal.getHealth());
         if(Set.of("vari","passive").contains(id))check(id+"-body-collision",caster.getZ()<85 && caster.getZ()>80.5,"stopped before wall z="+caster.getZ());
         return;
      }
      if(id.equals("passive")) {
         check("passive-distance",caster.position().distanceTo(start)>20 && caster.position().distanceTo(start)<=32.01,"moved="+caster.position().distanceTo(start));
         check("passive-no-damage",normal.getHealth()==500 && refused.getHealth()==500 && ally.getHealth()==500,"movement has no attack");
      } else {
         check(id+"-damage",normal.getHealth()<500,"HP="+normal.getHealth());
         check(id+"-attribution",normal.getLastHurtByMob()==caster,"casting player owns damage");
      }
      check(id+"-ally",ally.getHealth()==500,"live owned summon HP="+ally.getHealth());
      check(id+"-refusal",refused.getHealth()==500 && !refused.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS),"refused damage has no secondary effect");
      check(id+"-outside",outside.getHealth()==500,"outside intact");
      check(id+"-expired",GoroTechniques.activeCount()==0 && !MamaraganMobEffect.hasPending(caster.getUUID()),"bounded action completed");
      if(id.equals("sango"))for(int i=0;i<4;i++)check("chain-budget-"+i,Math.abs(500-chain.get(i).getHealth()-18*Math.pow(.75,i))<.01,"damage="+(500-chain.get(i).getHealth()));
      if(id.equals("raigo"))check("raigo-single-budget",Math.abs(500-normal.getHealth()-60)<.01,"direct hit not doubled by blast: "+(500-normal.getHealth()));
   }
   private static void extras() {
      setup("vari");var v=MinepieceModVariables.get(caster);v.PlayerEnergy=0;
      check("no-energy",!DevilFruitSkillDispatcher.castSkill(caster) && GoroTechniques.activeCount()==0 && v.PlayerEnergy==0,"no debit or action");
      setup("vari");DevilFruitSkillDispatcher.castSkill(caster);DevilFruitPowerLogic.resetDevilFruitPowers(caster);
      check("reset-charge",GoroTechniques.activeCount()==0,"pending charge removed");
      setup("mamaragan");normal=mob(152.5,84.5);DevilFruitSkillDispatcher.castSkill(caster);MamaraganMobEffect.tick(level,caster,0);
      check("storm-pending",MamaraganMobEffect.hasPending(caster.getUUID()) && normal.getHealth()==500,"warning before damage");
      caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));GoroTechniques.tick();
      check("suppression-clears",GoroTechniques.activeCount()==0 && !MamaraganMobEffect.hasPending(caster.getUUID()) && !caster.hasEffect(MinepieceModMobEffects.MAMARAGAN),"no delayed storm after sealing");
      setup("mamaragan");DevilFruitSkillDispatcher.castSkill(caster);MamaraganMobEffect.tick(level,caster,0);DevilFruitPowerLogic.resetDevilFruitPowers(caster);
      check("reset-storm",!MamaraganMobEffect.hasPending(caster.getUUID()) && !caster.hasEffect(MinepieceModMobEffects.MAMARAGAN),"effect and state cleared");
      setup("vari");DevilFruitSkillDispatcher.castSkill(caster);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);GoroTechniques.tick();
      check("fruit-change",GoroTechniques.activeCount()==0,"old fruit charge cancelled");
      setup("mamaragan");DevilFruitSkillDispatcher.castSkill(caster);MamaraganMobEffect.tick(level,caster,0);
      ServerLevel nether=level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
      caster.teleportTo(nether,152.5,100,80.5,Set.of(),0,0,false);GoroTechniques.tick();
      check("dimension-clears",caster.level()==nether && GoroTechniques.activeCount()==0 && !MamaraganMobEffect.hasPending(caster.getUUID()),"actual dimension transition cancels warning");
      caster.teleportTo(level,152.5,100,80.5,Set.of(),0,0,false);setup("passive");
      var lightning=level.damageSources().lightningBolt();
      check("body-lightning",DevilFruitPowerLogic.computeDamageMagnification(lightning,caster)==0,"electric body immunity");
      caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));
      check("body-suppressed",DevilFruitPowerLogic.computeDamageMagnification(lightning,caster)==1,"resistance revoked when sealed");
      caster.removeEffect(MinepieceModMobEffects.SEA_POWER);clean();
      setup("sango");normal=mob(152.5,86.5);refused=mob(152.5,82.5);refused.setPermanentlyInvulnerable(true);
      var sango=DevilFruitRegistry.movesFor(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO).get(2);
      var result=ChainLightning.cast(caster,sango);
      check("chain-refused-exposed",refused.getHealth()==500 && !refused.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS) && !result.targets().contains(refused),"nearest invulnerable candidate is never an accepted link");
      check("chain-refused-no-bridge",result.targets().size()==1 && result.targets().get(0)==normal && Math.abs(normal.getHealth()-482)<.01,"retry starts at last accepted origin, nominal first-hop budget");
      clean();setup("passive");caster.setShiftKeyDown(true);DevilFruitSkillDispatcher.castSkill(caster);caster.setShiftKeyDown(false);
      check("blink-backward",caster.getZ()<start.z-20 && caster.position().distanceTo(start)<=32.01,"source backward variant");
      setup("passive");
      for(int x=151;x<=154;x++)for(int z=79;z<=82;z++)level.setBlockAndUpdate(new BlockPos(x,103,z),Blocks.STONE.defaultBlockState());
      double moved=GoroTechniques.blink(caster,new Vec3(0,1,0),32);
      check("blink-ceiling",moved<=1.21 && level.noCollision(caster,caster.getBoundingBox()),"whole body stays below solid ceiling: "+moved);
      for(int x=151;x<=154;x++)for(int z=79;z<=82;z++)level.setBlockAndUpdate(new BlockPos(x,103,z),Blocks.AIR.defaultBlockState());
      setup("passive");MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU);
      check("gomu-electric-counter",DevilFruitPowerLogic.computeDamageMagnification(lightning,caster)==0,"rubber counter from ver15");
      caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));
      check("gomu-counter-suppressed",DevilFruitPowerLogic.computeDamageMagnification(lightning,caster)==1,"suppression removes counter");
      clean();setup("mamaragan");normal=mob(152.5,84.5);DevilFruitSkillDispatcher.castSkill(caster);MamaraganMobEffect.tick(level,caster,0);
      Vec3 announced=MamaraganMobEffect.pendingPoint(caster.getUUID());normal.setPos(160.5,100,84.5);
      MamaraganMobEffect.tick(level,caster,0);MamaraganMobEffect.tick(level,caster,0);
      check("storm-dodge",announced!=null && normal.position().distanceTo(announced)>2 && normal.getHealth()==500,"warning point stays fixed when the target escapes");
      clean();setup("raigo");normal=mob(152.5,84.5);
      ServerPlayer deflector=FakePlayer.get(level,new GameProfile(UUID.nameUUIDFromBytes("GoroDeflector".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"GoroDeflector"));
      MinepieceModVariables.get(deflector).PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU);
      deflector.teleportTo(152.5,100,80.5);
      var shot=new ImpactFixture(level);shot.setPos(152.5,101,84.5);shot.setOwner(caster);shot.setDeltaMovement(0,0,1);
      shot.goroOrigin(caster).damage(60).element(ElementType.LIGHTNING).model("ball_maximum").texture("clear").visualProfile("goro_raigo").explodeOnImpact(4,MinepieceModParticles.THUNDER_LIGHT_BLUE);
      boolean reflected=NikyuRepulsion.tryDeflect(shot,deflector);
      check("raigo-deflection-owner",reflected && shot.getOwner()==deflector && shot.getDeltaMovement().z<0,"actual repulsion transfers owner and reverses velocity");
      shot.hitWall();
      check("raigo-deflected-impact",Math.abs(normal.getHealth()-440)<.01 && normal.getLastHurtByMob()==deflector,"collision handler preserves 60-damage blast and credits Nikyu");
      MinepieceModVariables.clear(deflector);deflector.discard();clean();
      setup("passive");GoroAmaruTests.run(caster,GoroTestCommands::check);
   }
   private static final class ImpactFixture extends SkillProjectileEntity {
      ImpactFixture(ServerLevel world){super(MinepieceModEntities.SKILL_PROJECTILE,world);}
      void hitWall(){onHitBlock(new BlockHitResult(position(),net.minecraft.core.Direction.NORTH,blockPosition(),false));}
   }
   private static void clean() {
      if(caster!=null)GoroTechniques.cancel(caster);
      for(Mob e:fixtures)e.discard();fixtures.clear();chain.clear();
      if(level!=null) {
         for(var e:level.getEntitiesOfClass(EntityGomuArmEntity.class,new AABB(128,99,64,182,130,146)))if(GoroTechniques.ownsVisual(e.getVisualProfile()))e.discard();
         for(var e:level.getEntitiesOfClass(SkillProjectileEntity.class,new AABB(128,99,64,182,130,146)))if(e.getVisualProfile().equals("goro_raigo"))e.discard();
         for(int x=130;x<=174;x++)for(int y=100;y<=120;y++)level.setBlockAndUpdate(new BlockPos(x,y,85),Blocks.AIR.defaultBlockState());
      }
   }
   private static void end(){
      // A dimension teleport inserts FakePlayer into the real entity manager: remove it explicitly,
      // otherwise its proximity causes unrelated historical monster fixtures to despawn.
      if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}
      org.slf4j.LoggerFactory.getLogger("GoroAcceptance").info("[dgoro] END passed={} failed={} total={}",passed,failed,passed+failed);
   }
}
