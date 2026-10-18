package net.mcreator.minepiece.dragonlab;

import com.google.gson.*;
import java.nio.file.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.*;
import net.mcreator.minepiece.procedures.*;
import com.mojang.brigadier.arguments.*;

/** Only included in the opt-in disposable laboratory's separate test JAR. */
public final class DragonLabServer implements ModInitializer {
   private static final JsonObject SNAPSHOTS=new JsonObject();
   private static int readyTicks;private static boolean ready;
   @Override public void onInitialize(){
      if(!Boolean.getBoolean("opworldlike.dragonLab"))return;
      CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(
         Commands.literal("dragonlab")
         .then(Commands.literal("setup").executes(c->{var p=c.getSource().getPlayerOrException();
            DevilFruitPowerLogic.resetDevilFruitPowers(p);p.removeAllEffects();p.setGameMode(GameType.SURVIVAL);
            var v=MinepieceModVariables.get(p);v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU);
            v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;v.syncPlayerVariables(p);
            p.getAbilities().invulnerable=true;p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();
            for(int x=-8;x<=8;x++)for(int y=100;y<=175;y++)p.level().setBlockAndUpdate(new BlockPos(x,y,8),Blocks.AIR.defaultBlockState());
            p.connection.teleport(0,100,0,0,0);return 1;}))
         .then(Commands.literal("lift").executes(c->{c.getSource().getPlayerOrException().connection.teleport(0,140,0,0,0);return 1;}))
         .then(Commands.literal("wall").executes(c->{var p=c.getSource().getPlayerOrException();
            for(int x=-8;x<=8;x++)for(int y=120;y<=175;y++)p.level().setBlockAndUpdate(new BlockPos(x,y,8),Blocks.OBSIDIAN.defaultBlockState());
            p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.connection.teleport(0,140,0,0,0);return 1;}))
         .then(Commands.literal("camera-roof").executes(c->{var p=c.getSource().getPlayerOrException();for(int x=-16;x<=16;x++)for(int z=-16;z<=7;z++)p.level().setBlockAndUpdate(new BlockPos(x,146,z),Blocks.OBSIDIAN.defaultBlockState());return 1;}))
         .then(Commands.literal("away").executes(c->{var p=c.getSource().getPlayerOrException();for(int x=-16;x<=16;x++)for(int z=-16;z<=7;z++)p.level().setBlockAndUpdate(new BlockPos(x,146,z),Blocks.AIR.defaultBlockState());p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.connection.teleport(-25,140,0,-45,45);return 1;}))
         .then(Commands.literal("camera").executes(c->{var observer=c.getSource().getPlayerOrException();var p=observer.level().getServer().getPlayerList().getPlayerByName("FruitLab");
            if(p!=null){observer.setGameMode(GameType.SPECTATOR);observer.connection.teleport(p.getX()+32,p.getY()+18,p.getZ()-25,52,23);}return 1;}))
         .then(Commands.literal("suppress").executes(c->{c.getSource().getPlayerOrException().addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,100));return 1;}))
         .then(Commands.literal("empty").executes(c->{var p=c.getSource().getPlayerOrException();MinepieceModVariables.get(p).PlayerEnergy=0;return 1;}))
         .then(Commands.literal("mark").then(Commands.argument("name",StringArgumentType.word()).executes(c->{snapshot(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"));return 1;})))
         .then(Commands.literal("clock").then(Commands.argument("tick",IntegerArgumentType.integer(0)).executes(c->{var p=c.getSource().getServer().getPlayerList().getPlayerByName("FruitWitness");
            if(p!=null)ServerPlayNetworking.send(p,new DevilFruitNetworking.LabScenePayload("dragon-clock:"+IntegerArgumentType.getInteger(c,"tick")));return 1;})))
      ));
      ServerTickEvents.END_SERVER_TICK.register(server->{if(ready)return;
         var a=server.getPlayerList().getPlayerByName("FruitLab");var b=server.getPlayerList().getPlayerByName("FruitWitness");
         if(a!=null&&b!=null&&++readyTicks>=30){ready=true;for(var p:java.util.List.of(a,b))ServerPlayNetworking.send(p,new DevilFruitNetworking.LabScenePayload("dragon-ready"));}
      });
   }
   private static void snapshot(ServerPlayer p,String name){
      var row=new JsonObject();row.addProperty("x",p.getX());row.addProperty("y",p.getY());row.addProperty("z",p.getZ());
      row.addProperty("form",ZoanForms.form(p));row.addProperty("mayfly",p.getAbilities().mayfly);row.addProperty("flying",p.getAbilities().flying);
      row.addProperty("energy",MinepieceModVariables.get(p).PlayerEnergy);SNAPSHOTS.add(name,row);
      try{Files.writeString(Path.of("dragon-server.local.json"),new GsonBuilder().setPrettyPrinting().create().toJson(SNAPSHOTS));}
      catch(java.io.IOException error){throw new RuntimeException(error);}
   }
}
