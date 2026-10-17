package net.mcreator.minepiece.command;

import java.util.UUID;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.procedures.PowerBlockService;
import net.mcreator.minepiece.network.FruitWorldStorage;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Disposable-world physical block witnesses, including actual server restart and old journals. */
public final class PermanentTerrainTests {
 private static final UUID OWNER=UUID.nameUUIDFromBytes("PermanentTerrainWitness".getBytes(java.nio.charset.StandardCharsets.UTF_8));
 private static final Block[] MATERIALS={Blocks.AIR,Blocks.PACKED_ICE,Blocks.SANDSTONE,Blocks.MAGMA_BLOCK,Blocks.HONEY_BLOCK,Blocks.OAK_LEAVES,Blocks.CRACKED_STONE_BRICKS};
 private static final String[] NAMES={"crater","ice","sand","magma","mochi","leaves","quake"};
 private static ServerLevel world;private static int age;private static boolean active;
 private static BlockPos pos(int i){return new BlockPos(600+i*3,100,80);}
 private static BlockState expected(int i){var s=MATERIALS[i].defaultBlockState();return s.hasProperty(BlockStateProperties.PERSISTENT)?s.setValue(BlockStateProperties.PERSISTENT,true):s;}
 private static void check(String id,boolean ok,String detail){org.slf4j.LoggerFactory.getLogger("PermanentTerrainTest").info("[dpermanent] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> d){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  d.register(Commands.literal("dpermanent").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
   .then(Commands.literal("begin").executes(c->{if(active)return 0;world=c.getSource().getServer().overworld();age=0;
    PowerBlockService.releaseAll(OWNER);
    world.setBlockAndUpdate(pos(9),Blocks.STONE.defaultBlockState());PowerBlockService.place(world,pos(9),Blocks.HONEY_BLOCK.defaultBlockState(),1200,OWNER);PowerBlockService.releaseAll(OWNER);
    check("release-keeps-block",world.getBlockState(pos(9)).is(Blocks.HONEY_BLOCK) && !PowerBlockService.isTrackedAny(world,pos(9)),"explicit owner release cannot restore terrain");
    world.setBlockAndUpdate(pos(8),Blocks.STONE.defaultBlockState());PowerBlockService.place(world,pos(8),Blocks.PACKED_ICE.defaultBlockState(),20,UUID.nameUUIDFromBytes("EditTTL".getBytes(java.nio.charset.StandardCharsets.UTF_8)));boolean editArmed=world.getBlockState(pos(8)).is(Blocks.PACKED_ICE) && PowerBlockService.isTrackedAny(world,pos(8));world.setBlockAndUpdate(pos(8),Blocks.DIAMOND_BLOCK.defaultBlockState());
    check("edit-armed",editArmed && world.getBlockState(pos(8)).is(Blocks.DIAMOND_BLOCK) && PowerBlockService.isTrackedAny(world,pos(8)),"player edit during a genuinely active attribution lease");
    // Independent owner: this witness must expire naturally, without reset/release.
    for(int i=0;i<MATERIALS.length;i++){world.setBlockAndUpdate(pos(i),Blocks.STONE.defaultBlockState());PowerBlockService.place(world,pos(i),MATERIALS[i].defaultBlockState(),20,UUID.nameUUIDFromBytes(("TTL"+i).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
     check("placed/"+NAMES[i],world.getBlockState(pos(i)).equals(expected(i)) && PowerBlockService.isTrackedAny(world,pos(i)),"STONE changed to requested material; independent TTL attribution active before native wait");}
    active=true;return 1;}))
   .then(Commands.literal("verify").executes(c->{world=c.getSource().getServer().overworld();
    for(int i=0;i<MATERIALS.length;i++)check("restart/"+NAMES[i],world.getBlockState(pos(i)).equals(expected(i)) && !PowerBlockService.isTrackedAny(world,pos(i)),"actual saved world reloaded, expired attribution absent");
    check("restart/player-edit",world.getBlockState(pos(8)).is(Blocks.DIAMOND_BLOCK),"player edit retained across native expiry and restart");
    check("restart/released",world.getBlockState(pos(9)).is(Blocks.HONEY_BLOCK),"released block retained after restart");return 1;}))
   .then(Commands.literal("seedlegacy").executes(c->{world=c.getSource().getServer().overworld();CompoundTag root=new CompoundTag();ListTag list=new ListTag();
    world.setBlockAndUpdate(pos(12).below(),Blocks.OAK_LOG.defaultBlockState()); // Avoid pre-migration random decay; DISTANCE may still change naturally.
    for(int i=0;i<3;i++){BlockPos p=pos(12+i);var placed=i==2?Blocks.PACKED_ICE.defaultBlockState():Blocks.OAK_LEAVES.defaultBlockState();world.setBlockAndUpdate(p,i==1?Blocks.DIAMOND_BLOCK.defaultBlockState():placed);
     CompoundTag t=new CompoundTag();t.putString("dimension",world.dimension().identifier().toString());t.putInt("x",p.getX());t.putInt("y",p.getY());t.putInt("z",p.getZ());t.put("placed",NbtUtils.writeBlockState(placed));t.put("original",NbtUtils.writeBlockState(Blocks.STONE.defaultBlockState()));t.putLong("expireAt",world.getGameTime()-1);t.putString("owner",OWNER.toString());list.add(t);}
    root.put("blocks",list);try{FruitWorldStorage.write("temporary-blocks.dat",root);check("legacy/seed",true,"expired old journal written with original=STONE and an edited leaf position");}catch(Exception e){check("legacy/seed",false,e.toString());}return 1;}))
   .then(Commands.literal("verifylegacy").executes(c->{world=c.getSource().getServer().overworld();
    check("legacy/leaves-stable",world.getBlockState(pos(12)).is(Blocks.OAK_LEAVES) && world.getBlockState(pos(12)).getValue(BlockStateProperties.PERSISTENT),"expired old leaf stabilized instead of restoring original STONE");
    check("legacy/player-edit",world.getBlockState(pos(13)).is(Blocks.DIAMOND_BLOCK),"legacy placed state mismatch preserves player edit");
    check("legacy/ice-retained",world.getBlockState(pos(14)).is(Blocks.PACKED_ICE),"old original state never reapplied");
    check("legacy/journal-preserved",java.nio.file.Files.exists(FruitWorldStorage.path("temporary-blocks.dat")) && java.nio.file.Files.exists(FruitWorldStorage.path("power-block-leases.dat")),"old journal retained, new permanent format written");return 1;})));
  ServerTickEvents.END_SERVER_TICK.register(s->{if(!active || world.getServer()!=s)return;if(++age<80)return;
   for(int i=0;i<MATERIALS.length;i++)check("expiry/"+NAMES[i],world.getBlockState(pos(i)).equals(expected(i)) && !PowerBlockService.isTrackedAny(world,pos(i)),"80 native ticks, blocks retained and attribution expired");
   check("expiry/player-edit",world.getBlockState(pos(8)).is(Blocks.DIAMOND_BLOCK) && !PowerBlockService.isTrackedAny(world,pos(8)),"no timer overwrites subsequent edit; attribution expired");active=false;org.slf4j.LoggerFactory.getLogger("PermanentTerrainTest").info("[dpermanent] END native-expiry");});
 }
}
