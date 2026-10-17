package net.mcreator.minepiece.procedures;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.mcreator.minepiece.network.FruitWorldStorage;

/**
 * Permanent terrain from powers. Only attribution leases expire; blocks NEVER roll back.
 * Ending a field, revoking a fruit, logging out or restarting cannot restore old terrain.
 * Existing temporary-block journals migrate without applying their stored original states.
 */
public final class PowerBlockService {
 private static final String FILE="power-block-leases.dat", LEGACY="temporary-blocks.dat";
 private record Key(String dimension,int x,int y,int z){}
 private record Lease(BlockState placed,long expireAt,String owner){}
 private static final Map<Key,Lease> ACTIVE=new HashMap<>();
 private static final Map<Key,BlockState> STABILIZE=new HashMap<>();
 private PowerBlockService(){}
 private static Key key(ServerLevel l,BlockPos p){return new Key(l.dimension().identifier().toString(),p.getX(),p.getY(),p.getZ());}
 public static void register(){
  ServerLifecycleEvents.SERVER_STARTED.register(PowerBlockService::load);
  ServerLifecycleEvents.SERVER_STOPPING.register(PowerBlockService::save);
  ServerLifecycleEvents.SERVER_STOPPED.register(s->{ACTIVE.clear();STABILIZE.clear();});
  ServerTickEvents.END_SERVER_TICK.register(PowerBlockService::tick);
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.CHUNK_LOAD.register((level,chunk,newChunk)->stabilizeLoaded(level.getServer()));
 }
 /** Place final terrain; attributionTicks bounds only the associated damage field/ownership. */
 public static void place(ServerLevel level,BlockPos pos,BlockState block,int attributionTicks,UUID owner){
  if(!canPlace(level,pos))return;
  if(block.hasProperty(BlockStateProperties.PERSISTENT))block=block.setValue(BlockStateProperties.PERSISTENT,true);
  Key key=key(level,pos);String who=owner==null?"":owner.toString();Lease existing=ACTIVE.get(key);
  if(existing!=null && !existing.owner.equals(who))return;
  if(level.setBlock(pos,block,Block.UPDATE_ALL))ACTIVE.put(key,new Lease(block,level.getServer().overworld().getGameTime()+Math.max(1,attributionTicks),who));
 }
 /** Release attribution only, preserving the current block and any subsequent player edit. */
 public static boolean releaseNow(ServerLevel level,BlockPos pos,UUID owner){
  if(level==null || owner==null)return false;Key key=key(level,pos);Lease lease=ACTIVE.get(key);
  if(lease==null || !lease.owner.equals(owner.toString()))return false;ACTIVE.remove(key);return true;
 }
 public static void releaseAll(UUID owner){if(owner!=null)ACTIVE.values().removeIf(e->e.owner.equals(owner.toString()));}
 public static boolean isTrackedAny(ServerLevel level,BlockPos pos){return ACTIVE.containsKey(key(level,pos));}
 public static boolean isTracked(ServerLevel level,BlockPos pos,BlockState state,UUID owner){Lease e=ACTIVE.get(key(level,pos));return e!=null && e.placed.equals(state) && level.getBlockState(pos).equals(state) && (owner==null || e.owner.equals(owner.toString()));}
 public static int activeCount(){return ACTIVE.size();}
 public static int activeCountFor(UUID owner){if(owner==null)return 0;return (int)ACTIVE.values().stream().filter(e->e.owner.equals(owner.toString())).count();}
 private static boolean canPlace(ServerLevel level,BlockPos pos){
  if(!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos) || pos.getY()<level.getMinY() || pos.getY()>=level.getMaxY())return false;
  var state=level.getBlockState(pos);return !state.hasBlockEntity() && level.getBlockEntity(pos)==null && state.getDestroySpeed(level,pos)>=0 && !DevilFruitExplosions.isProtectedPosition(level,pos);
 }
 private static void tick(MinecraftServer server){
  long now=server.overworld().getGameTime();ACTIVE.values().removeIf(e->e.expireAt<=now);
  stabilizeLoaded(server);
 }
 private static void stabilizeLoaded(MinecraftServer server){
  // Upgrade old fruit leaves only when their chunk is already loaded; never scan/load the world.
  for(var entry:new ArrayList<>(STABILIZE.entrySet())){Key k=entry.getKey();var id=net.minecraft.resources.Identifier.tryParse(k.dimension);if(id==null){STABILIZE.remove(k);continue;}
   var level=server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,id));BlockPos pos=new BlockPos(k.x,k.y,k.z);
   if(level==null || !level.hasChunkAt(pos))continue;var current=level.getBlockState(pos);
   var old=entry.getValue();boolean same=current.equals(old) || current.is(old.getBlock()) && current.hasProperty(BlockStateProperties.DISTANCE) && old.hasProperty(BlockStateProperties.DISTANCE) && current.setValue(BlockStateProperties.DISTANCE,old.getValue(BlockStateProperties.DISTANCE)).equals(old);
   if(same && current.hasProperty(BlockStateProperties.PERSISTENT)){
    var stable=current.setValue(BlockStateProperties.PERSISTENT,true);level.setBlock(pos,stable,Block.UPDATE_ALL);
    var lease=ACTIVE.get(k);if(lease!=null)ACTIVE.put(k,new Lease(stable,lease.expireAt,lease.owner));
   }
   STABILIZE.remove(k);
  }
 }
 private static void load(MinecraftServer server){
  ACTIVE.clear();STABILIZE.clear();try{
   boolean migration=!Files.exists(FruitWorldStorage.path(FILE)) && Files.exists(FruitWorldStorage.path(LEGACY));
   CompoundTag root=FruitWorldStorage.read(migration?LEGACY:FILE);ListTag list=root.getListOrEmpty("blocks");long now=server.overworld().getGameTime();
   for(int i=0;i<list.size();i++){CompoundTag t=list.getCompoundOrEmpty(i);long end=t.getLongOr("expireAt",0);
    try{BlockState placed=NbtUtils.readBlockState(BuiltInRegistries.BLOCK,t.getCompoundOrEmpty("placed"));Key k=new Key(t.getStringOr("dimension",""),t.getIntOr("x",0),t.getIntOr("y",0),t.getIntOr("z",0));
     if(migration && placed.hasProperty(BlockStateProperties.PERSISTENT) && !placed.getValue(BlockStateProperties.PERSISTENT))STABILIZE.put(k,placed);
     if(end>now)ACTIVE.put(k,new Lease(placed,end,t.getStringOr("owner","")));}catch(RuntimeException bad){org.slf4j.LoggerFactory.getLogger("PowerBlocks").warn("Skipping malformed attribution lease",bad);}
   }
   ListTag pending=root.getListOrEmpty("stabilizeLeaves");for(int i=0;i<pending.size();i++){CompoundTag t=pending.getCompoundOrEmpty(i);STABILIZE.put(new Key(t.getStringOr("dimension",""),t.getIntOr("x",0),t.getIntOr("y",0),t.getIntOr("z",0)),NbtUtils.readBlockState(BuiltInRegistries.BLOCK,t.getCompoundOrEmpty("placed")));}
   stabilizeLoaded(server);
   if(migration){save(server);org.slf4j.LoggerFactory.getLogger("PowerBlocks").info("Migrated legacy terrain journal: no original blocks restored; old journal retained.");}
  }catch(IOException e){org.slf4j.LoggerFactory.getLogger("PowerBlocks").warn("Could not read block attribution leases; world blocks are untouched",e);}
 }
 private static void save(MinecraftServer server){
  CompoundTag root=new CompoundTag();root.putBoolean("permanentTerrain",true);ListTag list=new ListTag();
  ACTIVE.forEach((k,e)->{CompoundTag t=new CompoundTag();t.putString("dimension",k.dimension);t.putInt("x",k.x);t.putInt("y",k.y);t.putInt("z",k.z);t.put("placed",NbtUtils.writeBlockState(e.placed));t.putLong("expireAt",e.expireAt);t.putString("owner",e.owner);list.add(t);});root.put("blocks",list);
  ListTag pending=new ListTag();STABILIZE.forEach((k,state)->{CompoundTag t=new CompoundTag();t.putString("dimension",k.dimension);t.putInt("x",k.x);t.putInt("y",k.y);t.putInt("z",k.z);t.put("placed",NbtUtils.writeBlockState(state));pending.add(t);});root.put("stabilizeLeaves",pending);
  try{FruitWorldStorage.write(FILE,root);}catch(IOException e){org.slf4j.LoggerFactory.getLogger("PowerBlocks").warn("Could not save attribution leases",e);}
 }
}
