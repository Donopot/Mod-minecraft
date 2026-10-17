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
public final class MaguTestCommands {
   private static final String[] IDS={"daifunka","inugamiguren","kageromagumagu","volcanicexplosion","ryuseikazan"};
   private static ServerLevel level;
   private static ServerPlayer caster;
   private static final List<Mob> fixtures=new ArrayList<>();
   private static Mob normal,refused,outside,ally,wallVictim;
   private static EntityGomuArmEntity suspended;
   private static int phase,age,passed,failed;
   private static boolean active;
   private MaguTestCommands() { }
   private static void check(String id,boolean ok,String detail) {
      if(ok) passed++; else failed++;
      LoggerFactory.getLogger("MaguAcceptance").info("[dmagu] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);
   }
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      if(!Boolean.getBoolean("opworldlike.repairTests")) return;
      dispatcher.register(Commands.literal("dmagu").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx->{
         if(active) return 0;
         passed=failed=0;phase=age=0;level=ctx.getSource().getServer().overworld();
         caster=FakePlayer.get(level,new GameProfile(UUID.nameUUIDFromBytes("MaguAcceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"MaguAcceptance"));
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
            if(phase==10) {
               if(age==5)check("magma-water-distinct",MagmaBurn.isWet(wetTarget) && MagmaBurn.isBurning(wetTarget),"actual immersion after five real ticks");
               if(age<220)return;
               check("pool-natural-expiry",level.getBlockState(expiryPos).is(Blocks.MAGMA_BLOCK) && MagmaPools.poolCountFor(caster.getUUID())==0 && !PowerBlockService.isTrackedAny(level,expiryPos),"220 real server ticks, magma remains and attributed field ends");
               clean();active=false;end();return;
            }
            if(IDS[phase/2].equals("kageromagumagu") && age<=10)caster.move(MoverType.SELF,caster.getDeltaMovement());
            if(age==5 && phase%2==0 && Set.of("daifunka","inugamiguren").contains(IDS[phase/2]))
               check(IDS[phase/2]+"-charge-no-premature-hit",normal.getHealth()==500,"charge telegraph");
            if(age==30 && phase%2==0 && Set.of("daifunka","inugamiguren").contains(IDS[phase/2])) {
               double expected=IDS[phase/2].equals("daifunka")?18:21.6;
               check(IDS[phase/2]+"-impact-budget",Math.abs(500-normal.getHealth()-expected)<.01,"actual first impact="+(500-normal.getHealth()));
            }
            if(age==12 && phase%2==0) {
               String id=IDS[phase/2];
               var move=DevilFruitRegistry.movesFor(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU).get(phase/2);
               if(Set.of("daifunka","inugamiguren").contains(id))
                  check(id+"-animation-release",TechniqueAnimations.nameFor(move,caster).equals(id.equals("daifunka")?"magu_fist_release":"magu_hound_release") && !MaguTechniques.charging(caster),"release phase replaces charge before shot gesture");
               if(id.equals("kageromagumagu"))check("kagero-animation-transition",TechniqueAnimations.nameFor(move,caster).equals("magu_kagero_finish"),"dash has a separate finishing gesture");
            }
            if(age<160)return;
            finish();phase++;
            if(phase<10){begin();age=0;}
            else {clean();edges();age=0;}
         } catch(Throwable error) {
            check("suite-exception",false,error.toString());active=false;clean();
            LoggerFactory.getLogger("MaguAcceptance").info("[dmagu] END passed={} failed={} total={}",passed,failed,passed+failed);
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
      MaguTechniques.cancel(caster);MagmaPools.clearFrom(caster);PowerBlockService.releaseAll(caster.getUUID());
      var vars=MinepieceModVariables.get(caster);
      vars.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU);
      vars.UseDevilAbilities=true;vars.PlayerEnergy=vars.PlayerEnergyMAX=2000;
      vars.PlayerTechniqueUsedNumber=java.util.stream.IntStream.range(0,5).filter(i->DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem()).get(i).id().equals(id)).findFirst().orElseThrow();
      caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(vars.PlayerDevilFruit));
      caster.teleportTo(80.5,100,80.5);caster.setYRot(0);caster.setXRot(0);
      caster.setDeltaMovement(Vec3.ZERO);caster.getAbilities().flying=false;
   }
   private static void resetFloor(){for(int x=64;x<=110;x++)for(int z=64;z<=110;z++)level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());}
   private static void begin() {
      clean();resetFloor();String id=IDS[phase/2];setupCaster(id);
      boolean wall=phase%2==1;
      normal=mob(80.5,100,Set.of("daifunka","inugamiguren","ryuseikazan").contains(id)?100.5:id.equals("kageromagumagu")?94.5:84.5);refused=mob(82,100,normal.getZ());refused.setPermanentlyInvulnerable(true);
      outside=mob(103.5,100,80.5);
      var owned=new net.mcreator.minepiece.entity.SummonEntity(MinepieceModEntities.SUMMON,level);
      owned.setOwnerEntity(caster);owned.setNoAi(true);owned.setNoGravity(true);owned.lifetime(300);
      owned.setPos(79.5,100,normal.getZ());level.addFreshEntity(owned);ally=owned;fixtures.add(owned);
      float allyHp=ally.getHealth();ally.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);ally.setHealth(500);
      if(wall) {
         normal.discard();
         wallVictim=mob(80.5,100,87.5);
         for(int x=68;x<=94;x++) for(int y=100;y<=160;y++) level.setBlockAndUpdate(new BlockPos(x,y,85),Blocks.OBSIDIAN.defaultBlockState());
      }
      var vars=MinepieceModVariables.get(caster);
      var move=DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem()).get((int)vars.PlayerTechniqueUsedNumber);
      boolean cast=DevilFruitSkillDispatcher.castSkill(caster);
      if(!wall) {
         check(id+"-cast-cost",cast && Math.abs(vars.PlayerEnergy-(2000-move.energyCost()))<.001,"energy="+vars.PlayerEnergy);
         String expected=switch(id){case "daifunka"->"magu_fist_charge";case "inugamiguren"->"magu_hound_charge";case "kageromagumagu"->"magu_kagero_dash";case "volcanicexplosion"->"magu_volcanic_cast";default->"magu_meteors_cast";};
         check(id+"-animation-start",TechniqueAnimations.nameFor(move,caster).equals(expected) && TechniqueAnimations.fichierPresent(expected),"distinct packaged start gesture="+TechniqueAnimations.nameFor(move,caster));
         double energy=vars.PlayerEnergy;
         check(id+"-cooldown-spam",caster.getCooldowns().isOnCooldown(vars.PlayerDevilFruit) && !DevilFruitSkillDispatcher.castSkill(caster) && vars.PlayerEnergy==energy,"one debit, cooldown active");
      }
      MaguTechniques.release(caster);

   }
   private static void finish() {
      String id=IDS[phase/2];
      if(phase%2==1) check(id+"-solid-wall",wallVictim.getHealth()==500 && !MagmaBurn.isBurning(wallVictim),"protected HP="+wallVictim.getHealth());
      else {
         check(id+"-real-damage",normal.getHealth()<500,"HP="+normal.getHealth());
         check(id+"-attributed",normal.getLastHurtByMob()==caster,"damage credits casting player");
         check(id+"-outside-intact",outside.getHealth()==500 && !MagmaBurn.isBurning(outside),"outside HP="+outside.getHealth());
         check(id+"-refusal-no-burn",refused.getHealth()==500 && !MagmaBurn.isBurning(refused),"invulnerable target unaffected");
         check(id+"-ally-safe",ally.getHealth()==500 && !MagmaBurn.isBurning(ally),"owned ally HP="+ally.getHealth());
      }
   }
   private static void clean() {
      if(caster!=null) {MaguTechniques.cancel(caster);MagmaPools.clearFrom(caster);PowerBlockService.releaseAll(caster.getUUID());}
      MagmaBurn.clearAll();
      for(Mob entity:fixtures) entity.discard();fixtures.clear();
      if(level!=null) {
         for(Entity entity:level.getEntitiesOfClass(Entity.class,new AABB(60,98,60,115,120,115)))
            if(entity instanceof SkillProjectileEntity || entity instanceof EntityGomuArmEntity) entity.discard();
         for(int x=68;x<=94;x++) for(int y=100;y<=160;y++) level.setBlockAndUpdate(new BlockPos(x,y,85),Blocks.AIR.defaultBlockState());
      }
   }
   private static BlockPos expiryPos;
   private static Mob wetTarget;
   private static void end(){LoggerFactory.getLogger("MaguAcceptance").info("[dmagu] END passed={} failed={} total={}",passed,failed,passed+failed);}
   private static void edges() {
      resetFloor();
      setupCaster("daifunka");var vars=MinepieceModVariables.get(caster);vars.PlayerEnergy=0;
      check("no-energy-no-effect",!DevilFruitSkillDispatcher.castSkill(caster) && MaguTechniques.activeCount()==0,"refusal before charge");
      setupCaster("daifunka");DevilFruitSkillDispatcher.castSkill(caster);DevilFruitPowerLogic.resetDevilFruitPowers(caster);
      check("reset-clears-charge",MaguTechniques.activeCount()==0,"reset cancels pending launch");
      setupCaster("daifunka");DevilFruitSkillDispatcher.castSkill(caster);vars.PlayerDevilFruit=ItemStack.EMPTY;MaguTechniques.tick();
      check("fruit-change-clears-charge",MaguTechniques.activeCount()==0,"no stale charged attack");
      setupCaster("daifunka");
      check("body-fire-lava-resistance",DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().lava(),caster)==0 && DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().onFire(),caster)==0,"explicit prototype magma immunity");
      caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,40,0));
      check("body-suppressed",DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().lava(),caster)>0,"suppression removes defense");caster.removeEffect(MinepieceModMobEffects.SEA_POWER);
      Mob target=mob(101.5,100,101.5);MagmaBurn.ignite(target,40,caster);CombatSkillLogic.applyElementalEffect(target,ElementType.ICE);MagmaBurn.tick();
      check("ice-really-extinguishes",!MagmaBurn.isBurning(target) && target.getRemainingFireTicks()==0,"not relit next tick");
      target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100,0));
      check("fire-resistance-no-burn",MagmaBurn.ignite(target,160,caster)==0,"resistance potion");target.removeEffect(MobEffects.FIRE_RESISTANCE);
      MagmaBurn.ignite(target,3,caster);for(int i=0;i<4;i++)MagmaBurn.tick();
      check("burn-no-vanilla-tail",!MagmaBurn.isBurning(target) && target.getRemainingFireTicks()==0,"expiry removes owned fire indicator");
      int active=0;for(int i=0;i<500;i++){MagmaBurn.ignite(target,160,caster);MagmaBurn.tick();if(MagmaBurn.isBurning(target))active++;}
      check("burn-refresh-budget",active<=320 && !MagmaBurn.isBurning(target),"active ticks="+active);MagmaBurn.extinguish(target);
      MagmaBurn.extinguish(target);target.discard();target=mob(101.5,100,101.5);
      MagmaPools.create(level,new BlockPos(101,99,101),caster,1);MagmaPools.tick();
      check("pool-attributed-hit",target.getHealth()<500 && target.getLastHurtByMob()==caster,"real pool contact HP="+target.getHealth()+" attributed="+(target.getLastHurtByMob()==caster));
      check("pool-native-floor-disabled",DevilFruitPowerLogic.computeDamageMagnification(level.damageSources().hotFloor(),target)==0,"temporary floor has one filtered damage path");
      MagmaPools.clearAll();MagmaBurn.extinguish(target);
      try {
         var field=br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.class.getDeclaredField("maxPoolsPerPlayer");field.setAccessible(true);int saved=field.getInt(null);
         try {field.setInt(null,0);check("zero-pool-limit",MagmaPools.create(level,new BlockPos(106,99,106),caster,1)==0 && MagmaPools.activePools()==0,"zero means no floor creation");}
         finally {field.setInt(null,saved);}
      } catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
      BlockPos chest=new BlockPos(101,99,101);level.setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
      var inventory=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(chest);inventory.setItem(0,new ItemStack(net.minecraft.world.item.Items.DIAMOND,7));
      MagmaPools.create(level,chest,caster,1);
      check("pool-preserves-inventory",level.getBlockEntity(chest) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity c && c.getItem(0).is(net.minecraft.world.item.Items.DIAMOND) && c.getItem(0).getCount()==7,"chest with seven diamonds not replaced");
      MagmaPools.clearAll();
      for(int i=0;i<4;i++){level.setBlockAndUpdate(new BlockPos(69+i*8,99,102),Blocks.STONE.defaultBlockState());MagmaPools.create(level,new BlockPos(69+i*8,99,102),caster,1);}
      check("pool-population-cap",MagmaPools.poolCountFor(caster.getUUID())==3 && level.getBlockState(new BlockPos(69,99,102)).is(Blocks.MAGMA_BLOCK) && !PowerBlockService.isTrackedAny(level,new BlockPos(69,99,102)),"oldest field ends but permanent magma remains");
      MagmaPools.clearAll();
      check("pool-clear-keeps-terrain",level.getBlockState(new BlockPos(93,99,102)).is(Blocks.MAGMA_BLOCK) && MagmaPools.activePools()==0 && !PowerBlockService.isTrackedAny(level,new BlockPos(93,99,102)),"clear stops attributed field and leaves permanent magma");
      var nether=level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
      var netherOwner=FakePlayer.get(nether,new GameProfile(UUID.nameUUIDFromBytes("MaguNetherOwner".getBytes()),"MaguNetherOwner"));netherOwner.teleportTo(80.5,100,78.5);
      var netherVars=MinepieceModVariables.get(netherOwner);netherVars.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU);netherVars.UseDevilAbilities=true;
      for(int x=78;x<=82;x++)for(int z=77;z<=82;z++)for(int y=100;y<104;y++)nether.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
      BlockPos point=new BlockPos(80,99,80);nether.setBlockAndUpdate(point,Blocks.STONE.defaultBlockState());
      for(int y=100;y<104;y++)nether.setBlockAndUpdate(new BlockPos(80,y,80),Blocks.AIR.defaultBlockState());
      Mob over=mob(80.5,100,80.5);var under=EntityTypes.HUSK.spawn(nether,new BlockPos(80,100,80),EntitySpawnReason.COMMAND);
      if(under!=null){under.setBaby(false);under.setNoAi(true);MagmaPools.create(nether,point,netherOwner,0);MagmaPools.tick();check("pool-dimension-owned",MagmaBurn.isBurning(under) && !MagmaBurn.isBurning(over),"Nether pool hits only Nether victim");under.discard();}
      else check("pool-dimension-owned",false,"nether fixture failed");MagmaPools.clearAll();MinepieceModVariables.clear(netherOwner);
      BlockPos temp=new BlockPos(106,99,106);PowerBlockService.place(level,temp,Blocks.DIRT.defaultBlockState(),200,caster.getUUID());MagmaPools.create(level,temp,caster,0);
      check("pool-skips-other-owned",level.getBlockState(temp).is(Blocks.DIRT),"does not replace or steal another temporary construction");PowerBlockService.releaseAll(caster.getUUID());
      setupCaster("daifunka");DevilFruitSkillDispatcher.castSkill(caster);caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,40,0));MaguTechniques.tick();
      check("suppression-clears-charge",MaguTechniques.activeCount()==0,"sea suppression cancels charge");caster.removeEffect(MinepieceModMobEffects.SEA_POWER);
      setupCaster("daifunka");level.setBlockAndUpdate(new BlockPos(106,99,106),Blocks.STONE.defaultBlockState());int permanentPlaced=MagmaPools.create(level,new BlockPos(106,99,106),caster,1);vars.PlayerDevilFruit=ItemStack.EMPTY;MagmaPools.tick();
      check("fruit-change-keeps-pool",permanentPlaced>0 && MagmaPools.activePools()==0 && level.getBlockState(new BlockPos(106,99,106)).is(Blocks.MAGMA_BLOCK) && !PowerBlockService.isTrackedAny(level,new BlockPos(106,99,106)),"power loss ends field but leaves magma");
      setupCaster("daifunka");
      expiryPos=new BlockPos(104,99,104);level.setBlockAndUpdate(expiryPos,Blocks.STONE.defaultBlockState());int expiryPlaced=MagmaPools.create(level,expiryPos,caster,1);
      wetTarget=mob(108.5,100,108.5);
      for(int y=100;y<=102;y++)level.setBlockAndUpdate(new BlockPos(108,y,108),Blocks.WATER.defaultBlockState());
      MagmaBurn.ignite(wetTarget,40,caster);
      check("pool-expiry-armed",expiryPlaced>0 && level.getBlockState(expiryPos).is(Blocks.MAGMA_BLOCK) && PowerBlockService.isTrackedAny(level,expiryPos),"expiry begins from placed magma");
   }
}
