package net.mcreator.minepiece.command;

import java.util.*;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.procedures.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Real server attributes/collisions; charge and budget ticks explicitly driven for the FakePlayer. */
final class GoroAmaruTests {
   @FunctionalInterface interface Check {void accept(String id,boolean passed,String detail);}
   private static void charge(ServerPlayer p){for(int i=0;i<20;i++)GoroAmaru.tick(p);}
   private static boolean clean(ServerPlayer p){return !GoroAmaru.has(p) && !GoroAmaru.scaled(p) && MinepieceModVariables.get(p).player_form==0 && Math.abs(p.getBbHeight()-1.8)<.001;}
   static void run(ServerPlayer p,Check c){
      ServerLevel level=(ServerLevel)p.level();var v=MinepieceModVariables.get(p);
      p.setGameMode(GameType.SURVIVAL);p.setHealth(20);p.removeEffect(MinepieceModMobEffects.SEA_POWER);
      p.teleportTo(152.5,100,80.5);p.setPose(Pose.STANDING);p.setDeltaMovement(Vec3.ZERO);
      v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO);v.PlayerEnergy=v.PlayerEnergyMAX=2000;
      p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
      var external=Identifier.fromNamespaceAndPath("test","amaru_external_attack");
      p.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(external,2,AttributeModifier.Operation.ADD_VALUE));
      double attack=p.getAttributeValue(Attributes.ATTACK_DAMAGE);
      c.accept("amaru-charge-start",DevilFruitTransformation.toggle(p) && GoroAmaru.has(p) && !GoroAmaru.active(p),"V route enters charge");
      for(int i=0;i<19;i++)GoroAmaru.tick(p);
      c.accept("amaru-charge-not-early",!GoroAmaru.active(p) && v.PlayerEnergy==2000 && p.getScale()==1,"19 ticks, no debit or body change");
      GoroAmaru.tick(p);
      c.accept("amaru-active-cost",GoroAmaru.active(p) && v.player_form==1 && v.PlayerEnergy==1960,"20 ticks, one 40 debit");
      c.accept("amaru-bbox",Math.abs(p.getBoundingBox().getXsize()-2.4)<.001 && Math.abs(p.getBoundingBox().getYsize()-6.5)<.001,"actual box="+p.getBoundingBox());
      c.accept("amaru-eye",Math.abs(p.getEyeHeight()-5.5)<.001,"actual eye="+p.getEyeHeight());
      c.accept("amaru-crouch",Math.abs(p.getDimensions(Pose.CROUCHING).height()-6.2)<.001,"crouched height="+p.getDimensions(Pose.CROUCHING).height());
      c.accept("amaru-no-combat-bonus",p.getAttributeValue(Attributes.ATTACK_DAMAGE)==attack,"preexisting damage retained");
      double regenBefore=v.PlayerEnergy;DevilFruitPowerLogic.onDevilFruitUserTick(p);
      c.accept("amaru-no-regeneration",v.PlayerEnergy==regenBefore,"native power tick cannot erase maintenance cost");
      c.accept("amaru-flight-granted",p.getAbilities().mayfly && !p.getAbilities().flying,"double jump available, no forced takeoff");
      double before=v.PlayerEnergy;GoroAmaru.tick(p);
      c.accept("amaru-maintenance",Math.abs(v.PlayerEnergy-(before-.4))<.001,"explicit tick cost .4; native regen separate");
      before=v.PlayerEnergy;p.getAbilities().flying=true;GoroAmaru.tick(p);
      c.accept("amaru-flight-cost",Math.abs(v.PlayerEnergy-(before-1))<.001,".4 form + .6 flight");
      level.setBlockAndUpdate(new BlockPos(152,107,80),Blocks.STONE.defaultBlockState());
      c.accept("amaru-ceiling-collision",!level.noCollision(p,p.getBoundingBox().move(0,.6,0)),"actual 6.5 body collides with ceiling");
      level.setBlockAndUpdate(new BlockPos(152,107,80),Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(154,101,80),Blocks.STONE.defaultBlockState());
      c.accept("amaru-side-collision",!level.noCollision(p,p.getBoundingBox().move(.5,0,0)),"actual 2.4 body collides sideways");
      level.setBlockAndUpdate(new BlockPos(154,101,80),Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(153,101,84),Blocks.WATER.defaultBlockState());
      double moved=GoroTechniques.blink(p,new Vec3(0,0,1),8);
      c.accept("amaru-blink-volume-fluid",moved<2.5,"edge water stops full body, moved="+moved);
      level.setBlockAndUpdate(new BlockPos(153,101,84),Blocks.AIR.defaultBlockState());p.teleportTo(152.5,100,80.5);
      c.accept("amaru-toggle-off",DevilFruitTransformation.toggle(p) && clean(p),"V returns body");
      c.accept("amaru-flight-revoked",!p.getAbilities().mayfly && !p.getAbilities().flying,"only owned flight removed");
      GoroAmaru.end(p);GoroAmaru.end(p);
      c.accept("amaru-revert-idempotent",clean(p) && p.getAttributeValue(Attributes.ATTACK_DAMAGE)==attack,"external attribute survives repeated cleanup");
      c.accept("amaru-equipment",p.getMainHandItem().is(Items.IRON_SWORD),"held equipment retained");
      level.setBlockAndUpdate(new BlockPos(152,102,80),Blocks.STONE.defaultBlockState());double energy=v.PlayerEnergy;
      c.accept("amaru-low-ceiling-refused",!DevilFruitTransformation.toggle(p) && clean(p) && v.PlayerEnergy==energy,"refusal before mutation or debit");
      level.setBlockAndUpdate(new BlockPos(152,102,80),Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(new BlockPos(153,101,80),Blocks.STONE.defaultBlockState());
      c.accept("amaru-narrow-refused",!DevilFruitTransformation.toggle(p) && clean(p),"full width checked");
      level.setBlockAndUpdate(new BlockPos(153,101,80),Blocks.AIR.defaultBlockState());
      v.PlayerEnergy=39;c.accept("amaru-energy-refused",!DevilFruitTransformation.toggle(p) && clean(p),"below activation budget");v.PlayerEnergy=2000;
      DevilFruitTransformation.toggle(p);level.setBlockAndUpdate(new BlockPos(152,102,80),Blocks.STONE.defaultBlockState());charge(p);
      c.accept("amaru-charge-revalidates",clean(p) && v.PlayerEnergy==2000,"obstacle added during charge cancels without debit");level.setBlockAndUpdate(new BlockPos(152,102,80),Blocks.AIR.defaultBlockState());
      DevilFruitTransformation.toggle(p);c.accept("amaru-cancel-charge",DevilFruitTransformation.toggle(p) && clean(p) && v.PlayerEnergy==2000,"second V cancels pending form");
      var foreignScale=Identifier.fromNamespaceAndPath("test","amaru_foreign_scale");
      DevilFruitTransformation.toggle(p);p.getAttribute(Attributes.SCALE).addTransientModifier(new AttributeModifier(foreignScale,.25,AttributeModifier.Operation.ADD_VALUE));charge(p);
      c.accept("amaru-charge-scale-revalidates",!GoroAmaru.has(p) && !GoroAmaru.scaled(p) && v.PlayerEnergy==2000 && p.getAttribute(Attributes.SCALE).hasModifier(foreignScale),"foreign scale arriving during charge refuses before debit and survives");
      p.getAttribute(Attributes.SCALE).removeModifier(foreignScale);p.refreshDimensions();
      DevilFruitTransformation.toggle(p);charge(p);p.getAttribute(Attributes.SCALE).removeModifier(GoroAmaru.SCALE);
      p.getAttribute(Attributes.SCALE).addTransientModifier(new AttributeModifier(foreignScale,1.5,AttributeModifier.Operation.ADD_VALUE));GoroAmaru.tick(p);
      c.accept("amaru-marker-removal",!GoroAmaru.has(p) && v.player_form==0 && p.getAttribute(Attributes.SCALE).hasModifier(foreignScale),"foreign equal scale cannot keep Amaru after ownership marker disappears");
      p.getAttribute(Attributes.SCALE).removeModifier(foreignScale);p.refreshDimensions();v.PlayerEnergy=2000;
      DevilFruitTransformation.toggle(p);charge(p);p.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));GoroAmaru.tick(p);
      c.accept("amaru-suppression",clean(p) && !p.getAbilities().mayfly,"suppression removes body and owned flight");
      c.accept("amaru-suppressed-refused",!DevilFruitTransformation.toggle(p) && clean(p),"cannot reactivate sealed");p.removeEffect(MinepieceModMobEffects.SEA_POWER);
      DevilFruitTransformation.toggle(p);charge(p);v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);GoroAmaru.tick(p);
      c.accept("amaru-fruit-change",clean(p),"state belongs to Goro only");v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO);
      DevilFruitTransformation.toggle(p);charge(p);v.PlayerEnergy=.1;GoroAmaru.tick(p);
      c.accept("amaru-energy-end",clean(p) && v.PlayerEnergy==0,"exhaustion ends form");v.PlayerEnergy=2000;
      DevilFruitTransformation.toggle(p);charge(p);int budget=GoroAmaru.remaining(p);for(int i=0;i<budget;i++)GoroAmaru.tick(p);
      c.accept("amaru-duration-end",clean(p),"1200-tick form bound");
      p.getAbilities().mayfly=true;DevilFruitTransformation.toggle(p);charge(p);GoroAmaru.end(p);
      c.accept("amaru-external-flight",p.getAbilities().mayfly,"preexisting flight not revoked");p.getAbilities().mayfly=false;
      DevilFruitTransformation.toggle(p);charge(p);DevilFruitTransformation.restorePersistedForm(p);
      c.accept("amaru-login-clears",clean(p),"ephemeral form not restored across login/respawn");
      DevilFruitTransformation.toggle(p);charge(p);var saved=v.writeNbt();GoroAmaru.end(p);
      v.loadNbt(saved);p.getAbilities().mayfly=true;
      c.accept("amaru-flight-owner-persisted",v.amaruFlightGranted,"ownership survives NBT round trip without runtime map");
      DevilFruitTransformation.restorePersistedForm(p);
      c.accept("amaru-stale-flight-clears",clean(p) && !p.getAbilities().mayfly && !v.amaruFlightGranted,"saved owned flight revoked after lost runtime map");
      v.player_form=1;DevilFruitTransformation.restorePersistedForm(p);
      c.accept("amaru-stale-save-clears",clean(p),"saved stale flag without map cleared");
      DevilFruitTransformation.toggle(p);charge(p);p.teleportTo(level.getServer().getLevel(Level.NETHER),152.5,100,80.5,Set.of(),0,0,false);GoroAmaru.tick(p);
      c.accept("amaru-dimension-clears",clean(p),"actual dimension transition");p.teleportTo(level,152.5,100,80.5,Set.of(),0,0,false);
      DevilFruitTransformation.toggle(p);charge(p);p.setHealth(0);GoroAmaru.tick(p);
      c.accept("amaru-death-clears",clean(p),"dead owner loses form");p.setHealth(20);
      c.accept("amaru-no-leak",GoroAmaru.count()==0,"no active/pending form remains");
      p.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(external);p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
   }
}
