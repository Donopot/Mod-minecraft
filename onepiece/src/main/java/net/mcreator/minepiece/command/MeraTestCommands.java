package net.mcreator.minepiece.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.*;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.procedures.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.slf4j.LoggerFactory;

/** Disposable-server integration tests. FakePlayer casts; real inserted mobs receive damage. */
public final class MeraTestCommands {
   private static final String[] IDS={"hiken","higan","hibashira","kagero","hotarubi","kaenryuo","daienkai"};
   private static ServerLevel level;
   private static ServerPlayer caster;
   private static final List<Mob> fixtures=new ArrayList<>();
   private static Mob normal,refused,outside,ally,wallVictim;
   private static EntityGomuArmEntity suspended;
   private static int phase,age,passed,failed;
   private static boolean active;
   private MeraTestCommands() { }
   private static void check(String id,boolean ok,String detail) {
      if(ok) passed++; else failed++;
      LoggerFactory.getLogger("MeraAcceptance").info("[dmera] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);
   }
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      if(!Boolean.getBoolean("opworldlike.repairTests")) return;
      dispatcher.register(Commands.literal("dmera").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx->{
         if(active) return 0;
         passed=failed=0;phase=age=0;level=ctx.getSource().getServer().overworld();
         caster=FakePlayer.get(level,new GameProfile(UUID.nameUUIDFromBytes("MeraAcceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"MeraAcceptance"));
         // Explicit opt-in laboratory arena, far from the common regression fixtures.
         for(int x=64;x<=110;x++) for(int z=64;z<=110;z++) {
            level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());
            for(int y=100;y<=110;y++) level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
         }
         active=true;begin();return 1;
      }));
      ServerTickEvents.END_SERVER_TICK.register(server->{
         if(!active || level.getServer()!=server) return;
         try {
            age++;
            if(IDS[phase/2].equals("kagero")) caster.move(MoverType.SELF,caster.getDeltaMovement());
            if(age==5 && phase%2==0 && Set.of("hiken","hotarubi","daienkai").contains(IDS[phase/2]))
               check(IDS[phase/2]+"-telegraph-no-premature-hit",normal.getHealth()==500,"HP before minimum charge="+normal.getHealth());
            if(age==20 && phase==0) check("visual-anchor-no-gravity",Math.abs(suspended.getY()-106)<.001,"anchor Y="+suspended.getY());
            if(age==35 && phase%2==0 && IDS[phase/2].equals("daienkai")) {
               var move=DevilFruitRegistry.movesFor(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA).get(6);
               check("ultimate-first-hit-once",Math.abs(500-normal.getHealth()-38.5)<.001,"damage at impact="+(500-normal.getHealth()));
            }
            if(age<50) return;
            finish();phase++;
            if(phase<14) {begin();age=0;}
            else {
               clean();testGeometryAndLifecycle();testBurnAndPropulsion();
               active=false;
               LoggerFactory.getLogger("MeraAcceptance").info("[dmera] END passed={} failed={} total={}",passed,failed,passed+failed);
            }
         } catch(Throwable error) {
            check("suite-exception",false,error.toString());active=false;clean();
            LoggerFactory.getLogger("MeraAcceptance").info("[dmera] END passed={} failed={} total={}",passed,failed,passed+failed);
         }
      });
   }
   private static Mob mob(double x,double y,double z) {
      Mob mob=EntityTypes.HUSK.spawn(level,new BlockPos((int)x,(int)y,(int)z),EntitySpawnReason.COMMAND);
      if(mob==null) throw new IllegalStateException("Husk fixture failed");
      if(mob instanceof net.minecraft.world.entity.monster.zombie.Zombie zombie) zombie.setBaby(false);
      mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
      mob.setPos(x,y,z);mob.setNoAi(true);mob.setNoGravity(true);
      mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);mob.setHealth(500);
      mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
      fixtures.add(mob);return mob;
   }
   private static void setupCaster(String id) {
      MeraTechniques.cancel(caster);FireRing.clearFrom(caster);FirePropulsion.stop(caster,"test");
      var vars=MinepieceModVariables.get(caster);
      vars.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);
      vars.UseDevilAbilities=true;vars.PlayerEnergy=vars.PlayerEnergyMAX=2000;
      vars.PlayerTechniqueUsedNumber=java.util.stream.IntStream.range(0,7).filter(i->DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem()).get(i).id().equals(id)).findFirst().orElseThrow();
      caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(vars.PlayerDevilFruit));
      caster.teleportTo(80.5,100,80.5);caster.setYRot(0);caster.setXRot(0);
      caster.setDeltaMovement(Vec3.ZERO);caster.getAbilities().flying=false;
   }
   private static void begin() {
      clean();String id=IDS[phase/2];setupCaster(id);
      boolean wall=phase%2==1;
      normal=mob(80.5,100,Set.of("hiken","higan","daienkai").contains(id)?100.5:84.5);refused=mob(82.5,100,84.5);refused.setPermanentlyInvulnerable(true);
      outside=mob(103.5,100,80.5);
      var owned=new net.mcreator.minepiece.entity.SummonEntity(MinepieceModEntities.SUMMON,level);
      owned.setOwnerEntity(caster);owned.setNoAi(true);owned.setNoGravity(true);owned.lifetime(100);
      owned.setPos(79.5,100,83.5);level.addFreshEntity(owned);ally=owned;fixtures.add(owned);
      float allyHp=ally.getHealth();ally.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);ally.setHealth(500);
      if(wall) {
         normal.discard();
         wallVictim=mob(80.5,100,87.5);
         for(int x=68;x<=94;x++) for(int y=100;y<=110;y++) level.setBlockAndUpdate(new BlockPos(x,y,85),Blocks.OBSIDIAN.defaultBlockState());
      }
      var vars=MinepieceModVariables.get(caster);
      var move=DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem()).get((int)vars.PlayerTechniqueUsedNumber);
      boolean cast=DevilFruitSkillDispatcher.castSkill(caster);
      if(!wall) {
         check(id+"-cast-cost",cast && Math.abs(vars.PlayerEnergy-(2000-move.energyCost()))<.001,"energy="+vars.PlayerEnergy);
         double energy=vars.PlayerEnergy;
         check(id+"-cooldown-spam",caster.getCooldowns().isOnCooldown(vars.PlayerDevilFruit) && !DevilFruitSkillDispatcher.castSkill(caster) && vars.PlayerEnergy==energy,"one debit, cooldown active");
      }
      MeraTechniques.release(caster);
      if(phase==0) suspended=MeraTechniques.visual(caster,"mera_charge",new Vec3(101,106,80),100);
   }
   private static void finish() {
      String id=IDS[phase/2];
      if(phase%2==1) check(id+"-solid-wall",wallVictim.getHealth()==500 && !FlameBurn.isBurning(wallVictim),"protected HP="+wallVictim.getHealth());
      else {
         check(id+"-real-damage",normal.getHealth()<500,"HP="+normal.getHealth());
         check(id+"-attributed",normal.getLastHurtByMob()==caster,"damage credits casting player");
         check(id+"-outside-intact",outside.getHealth()==500 && !FlameBurn.isBurning(outside),"outside HP="+outside.getHealth());
         check(id+"-refusal-no-burn",refused.getHealth()==500 && !FlameBurn.isBurning(refused),"invulnerable target unaffected");
         check(id+"-ally-safe",ally.getHealth()==500 && !FlameBurn.isBurning(ally),"owned ally HP="+ally.getHealth());
      }
   }
   private static void clean() {
      if(caster!=null) {MeraTechniques.cancel(caster);FireRing.clearFrom(caster);FirePropulsion.stop(caster,"test");}
      FlameBurn.clearAll();
      for(Mob entity:fixtures) entity.discard();fixtures.clear();
      if(level!=null) {
         for(Entity entity:level.getEntitiesOfClass(Entity.class,new AABB(60,98,60,115,120,115)))
            if(entity instanceof SkillProjectileEntity || entity instanceof EntityGomuArmEntity) entity.discard();
         for(int x=68;x<=94;x++) for(int y=100;y<=110;y++) level.setBlockAndUpdate(new BlockPos(x,y,85),Blocks.AIR.defaultBlockState());
      }
   }
   private static void testGeometryAndLifecycle() {
      setupCaster("hibashira");
      Mob inside=mob(83.4,100,80.5),corner=mob(83.4,100,83.4),high=mob(80.5,109,80.5);
      MeraTechniques.strikeVolume(caster,caster.position(),3,5,12,0);
      check("volume-circle-not-square",inside.getHealth()==488 && corner.getHealth()==500,"edge="+inside.getHealth()+" corner="+corner.getHealth());
      check("volume-height-bound",high.getHealth()==500,"upper floor="+high.getHealth());
      Mob potion=mob(81.5,100,80.5);potion.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100));
      MeraTechniques.strikeVolume(caster,caster.position(),3,5,12,0);
      check("fire-resistance-potion",potion.getHealth()==500 && !FlameBurn.isBurning(potion),"fire-resistant HP="+potion.getHealth());
      setupCaster("hiken");DevilFruitSkillDispatcher.castSkill(caster);
      check("charge-started",MeraTechniques.charging(caster),"charge pending");
      DevilFruitPowerLogic.resetDevilFruitPowers(caster);
      check("reset-cancels-charge",!MeraTechniques.charging(caster),"no delayed release after reset");
      setupCaster("daienkai");var vars=MinepieceModVariables.get(caster);vars.PlayerEnergy=0;
      check("no-energy-no-effect",!DevilFruitSkillDispatcher.castSkill(caster) && !MeraTechniques.charging(caster) && vars.PlayerEnergy==0,"refused without charge or debit");
      FireRing.create(level,caster.blockPosition(),caster);
      for(int i=0;i<180;i++) FireRing.tick();
      check("field-natural-expiry",FireRing.activeRings()==0,"expiry without forced revoke");
      clean();
      setupCaster("higan");DevilFruitSkillDispatcher.castSkill(caster);
      for(int i=0;i<15;i++) MeraTechniques.tick();
      MeraTechniques.release(caster);MeraTechniques.tick();
      var shots=level.getEntitiesOfClass(SkillProjectileEntity.class,new AABB(60,98,60,115,120,115),shot->shot.getVisualProfile().equals("higan"));
      check("charged-higan-four-projectiles",shots.size()==4,"released shots="+shots.size());
      check("charged-higan-budget",shots.size()==4 && shots.stream().allMatch(shot->shot.getDamageValue()==15),"15 damage per charged shot, native invulnerability frames preserved");
      check("charged-higan-cost",MinepieceModVariables.get(caster).PlayerEnergy==1950,"one cast debit for four shots");
      clean();setupCaster("hiken");DevilFruitSkillDispatcher.castSkill(caster);
      for(int i=0;i<101;i++) MeraTechniques.tick();
      check("hiken-charge-timeout",!MeraTechniques.charging(caster),"held charge auto-releases within 100 ticks");
      clean();setupCaster("hotarubi");DevilFruitSkillDispatcher.castSkill(caster);MeraTechniques.release(caster);
      for(int i=0;i<10;i++) MeraTechniques.tick();
      boolean field=level.getEntitiesOfClass(EntityGomuArmEntity.class,new AABB(60,98,60,120,120,120),fx->fx.getVisualProfile().equals("mera_fireflies")).size()==1;
      caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));MeraTechniques.tick();
      check("suppression-stops-maintained",field && level.getEntitiesOfClass(EntityGomuArmEntity.class,new AABB(60,98,60,120,120,120),fx->fx.getVisualProfile().equals("mera_fireflies")).isEmpty(),"suppression ends live firefly field");
      caster.removeEffect(MinepieceModMobEffects.SEA_POWER);
      check("body-fire-counter",DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().onFire(),caster)==0,"ordinary fire immunity adaptation");
      check("body-magma-counter",DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().lava(),caster)==1,"magma remains a counter");
      caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));
      check("body-suppressed",DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().onFire(),caster)==1,"suppression removes elemental defense");
      caster.removeEffect(MinepieceModMobEffects.SEA_POWER);clean();
      setupCaster("hiken");FirePropulsion.reconcile(caster);
      vars=MinepieceModVariables.get(caster);
      var restored=new MinepieceModVariables.PlayerVariables();restored.loadNbt(vars.writeNbt());
      check("flight-ownership-persisted",restored.meraFlightGranted,"ownership survives NBT round-trip");
      FirePropulsion.detach(caster);
      check("disconnect-revokes-owned-flight",!caster.getAbilities().mayfly && !vars.meraFlightGranted,"no survival flight stored after disconnect");
      vars.meraFlightGranted=true;vars.PlayerEnergy=0;caster.getAbilities().mayfly=true;
      FirePropulsion.reconcile(caster);
      check("reconnect-low-energy-revokes-flight",!caster.getAbilities().mayfly && !vars.meraFlightGranted,"saved ownership reclaimed and revoked");
      clean();setupCaster("hiken");
      var reflected=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,level);
      reflected.setPos(104,104,67);reflected.setOwner(caster);reflected.visualProfile("hiken").meraOrigin(caster);
      var reflector=FakePlayer.get(level,new GameProfile(UUID.nameUUIDFromBytes("MeraReflector".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"MeraReflector"));
      reflected.setOwner(reflector);reflected.setDeltaMovement(new Vec3(0,0,.1));level.addFreshEntity(reflected);reflected.tick();
      check("reflection-retains-projectile",!reflected.isRemoved() && reflected.getOwner()==reflector,"current owner may differ from original Mera caster");reflected.discard();
      var other=level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
      var crossed=new SkillProjectileEntity(MinepieceModEntities.SKILL_PROJECTILE,other);
      crossed.setPos(0,104,0);crossed.setOwner(caster);crossed.visualProfile("hiken").meraOrigin(caster);crossed.tick();
      check("cross-dimension-projectile-cancelled",crossed.isRemoved(),"projectile and origin must share the same dimension");
      Mob pushed=mob(104.5,100,80.5);pushed.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0);
      MeraTechniques.blast(caster,level,new Vec3(103,100.8,80.5),4,12,1);
      check("fire-blast-knockback",pushed.getHealth()==488 && pushed.getDeltaMovement().x>0,"accepted explosion pushes outward "+pushed.getDeltaMovement());
      clean();
   }
   private static void testBurnAndPropulsion() {
      setupCaster("hiken");Mob target=mob(101.5,100,101.5);
      int cap=br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig.burnCapTicks();
      int activeTicks=0;
      for(int i=0;i<cap+100;i++) {FlameBurn.ignite(target,60,caster);FlameBurn.tick();if(FlameBurn.isBurning(target)) activeTicks++;}
      check("burn-cap-continuous-refresh",activeTicks<=cap && !FlameBurn.isBurning(target) && target.getRemainingFireTicks()==0,"active ticks="+activeTicks+" cap="+cap);
      FlameBurn.extinguish(target);FlameBurn.ignite(target,60,caster);
      for(int i=0;i<60;i++) FlameBurn.tick();
      check("burn-natural-expiry",!FlameBurn.isBurning(target) && target.getRemainingFireTicks()==0,"single application ends at 60 ticks");
      FlameBurn.extinguish(target);FlameBurn.ignite(target,60,caster);
      level.setBlockAndUpdate(target.blockPosition(),Blocks.WATER.defaultBlockState());FlameBurn.tick();
      check("water-extinguishes",!FlameBurn.isBurning(target),"wet flame removed");
      level.setBlockAndUpdate(target.blockPosition(),Blocks.AIR.defaultBlockState());
      FirePropulsion.reconcile(caster);
      check("survival-propulsion-accessible",caster.getAbilities().mayfly,"survival flight capability");
      caster.getAbilities().flying=true;FirePropulsion.start(caster);
      double before=MinepieceModVariables.get(caster).PlayerEnergy;
      for(int i=0;i<5;i++) FirePropulsion.tick(caster);
      double spent=before-MinepieceModVariables.get(caster).PlayerEnergy;
      check("propulsion-exact-cost",Math.abs(spent-3)<.001 && caster.getDeltaMovement().z>0,"five ticks spent="+spent);
      level.setBlockAndUpdate(new BlockPos(80,101,81),Blocks.STONE.defaultBlockState());FirePropulsion.tick(caster);
      check("propulsion-body-wall",!FirePropulsion.isPropelling(caster) && FirePropulsion.lastStopReason(caster).equals("obstacle"),"head/body collision");
      level.setBlockAndUpdate(new BlockPos(80,101,81),Blocks.AIR.defaultBlockState());
      caster.getAbilities().flying=true;FirePropulsion.start(caster);MinepieceModVariables.get(caster).PlayerEnergy=0;FirePropulsion.tick(caster);
      check("propulsion-exhaustion",!FirePropulsion.isPropelling(caster),"zero energy ends thrust");
      MinepieceModVariables.get(caster).PlayerEnergy=200;FirePropulsion.start(caster);
      caster.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
      double spectatorEnergy=MinepieceModVariables.get(caster).PlayerEnergy;FirePropulsion.tick(caster);
      check("propulsion-stops-on-spectator",!FirePropulsion.isPropelling(caster) && MinepieceModVariables.get(caster).PlayerEnergy==spectatorEnergy && caster.getDeltaMovement().lengthSqr()==0,"spectator keeps native flight without thrust or energy drain");
      caster.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);FirePropulsion.reconcile(caster);FirePropulsion.start(caster);FirePropulsion.tick(caster);FirePropulsion.clearAll();
      check("propulsion-clear-stops-motion",!FirePropulsion.isPropelling(caster) && caster.getDeltaMovement().lengthSqr()==0 && !caster.getAbilities().flying,"global cleanup runs the owner stop path");
      MinepieceModVariables.get(caster).PlayerEnergy=200;caster.getAbilities().flying=true;FirePropulsion.start(caster);
      MinepieceModVariables.get(caster).PlayerDevilFruit=ItemStack.EMPTY;FirePropulsion.tick(caster);FirePropulsion.reconcile(caster);
      check("fruit-change-revokes-propulsion",!FirePropulsion.isPropelling(caster) && !caster.getAbilities().mayfly,"no flight after fruit removal");
      clean();MinepieceModVariables.clear(caster);
   }
}
