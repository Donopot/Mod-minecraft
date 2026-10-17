package net.mcreator.minepiece.network;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/** World-owned storage. A bad/newer file is never overwritten by a failed load. */
public final class FruitWorldStorage {
   private static final int SCHEMA=1;
   private static MinecraftServer server;
   private static Path root;
   private static final Set<String> unreadable=new HashSet<>();
   private FruitWorldStorage() { }
   public static void bind(MinecraftServer owner) {
      server=owner;
      root=owner.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().resolve("data/opworldlike");
      unreadable.clear();
   }
   public static void unbind(MinecraftServer owner) {
      if(server==owner) {server=null;root=null;unreadable.clear();}
   }
   public static Path path(String name) {
      if(root==null)throw new IllegalStateException("Fruit storage is not bound to a world");
      if(!Set.of("devilfruit-players.dat","temporary-blocks.dat","power-block-leases.dat").contains(name))throw new IllegalArgumentException("Unknown storage file");
      return root.resolve(name);
   }
   public static CompoundTag read(String name) throws IOException {
      Path path=path(name);
      if(!Files.exists(path))return new CompoundTag();
      try {
         CompoundTag tag=NbtIo.read(path);
         if(tag==null || tag.getIntOr("schema",SCHEMA)>SCHEMA)throw new IOException("Invalid or newer fruit data: "+path);
         return tag;
      } catch(IOException | RuntimeException failure) {
         unreadable.add(name);
         if(failure instanceof IOException io)throw io;
         throw new IOException("Unable to read fruit data: "+path,failure);
      }
   }
   public static void write(String name,CompoundTag data) throws IOException {
      Path target=path(name);
      if(unreadable.contains(name))throw new IOException("Refusing to overwrite unreadable fruit data: "+target);
      Files.createDirectories(target.getParent());
      Path temp=target.resolveSibling(target.getFileName()+"."+UUID.randomUUID()+".tmp");
      data.putInt("schema",SCHEMA);
      try {
         NbtIo.write(data,temp);
         try { Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
         catch(AtomicMoveNotSupportedException unsupported) {Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}
      } finally {Files.deleteIfExists(temp);}
   }
}
