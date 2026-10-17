package net.mcreator.minepiece.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.*;
import net.mcreator.minepiece.procedures.*;

/** Private scenario only; never registered in normal worlds. */
public final class AmaruLabCommands {
   public static void register(CommandDispatcher<CommandSourceStack> d){
      if(!Boolean.getBoolean("opworldlike.visualLab") || !"amaru".equals(System.getProperty("opworldlike.visualScenario")))return;
      d.register(Commands.literal("damarucount").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx->{ctx.getSource().sendSuccess(()->net.minecraft.network.chat.Component.literal("AMARU_ACTIVE="+GoroAmaru.count()),false);return GoroAmaru.count();}));
      d.register(Commands.literal("damarulab").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
         .then(Commands.argument("step",StringArgumentType.word()).executes(ctx->{
            var p=ctx.getSource().getPlayer();if(p==null || !p.getName().getString().equals("FruitLab"))return 0;
            String step=StringArgumentType.getString(ctx,"step");var level=(ServerLevel)p.level();var v=MinepieceModVariables.get(p);
            if(step.startsWith("clock_")){
               int clock=Integer.parseInt(step.substring(6));if(clock<0 || clock>720)return 0;
               for(var w:level.getServer().getPlayerList().getPlayers())if(w.getName().getString().equals("FruitWitness"))
                  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(w,new DevilFruitNetworking.LabScenePayload("amaru-clock:"+clock));
               return 1;
            }
            if(step.equals("setup")){
               DevilFruitPowerLogic.resetDevilFruitPowers(p);
               v.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO);v.PlayerEnergy=v.PlayerEnergyMAX=2000;v.UseDevilAbilities=true;
               for(int x=-6;x<36;x++)for(int z=-6;z<40;z++){
                  level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());
                  for(int y=100;y<113;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
               }
               p.setGameMode(GameType.SURVIVAL);p.connection.teleport(8.5,100,8.5,0,0);
               p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
               for(var other:level.getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(-6,99,-6,36,113,40)))other.discard();
            }else if(step.equals("suppress"))p.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,80));
            else if(step.equals("witnessout") || step.equals("witnessback")){
               for(var w:level.getServer().getPlayerList().getPlayers())if(w.getName().getString().equals("FruitWitness"))w.connection.teleport(step.equals("witnessout")?-300:23,106,3,46,12);
            }
            else if(step.equals("ceiling")){p.removeEffect(MinepieceModMobEffects.SEA_POWER);for(int x=7;x<=9;x++)for(int z=7;z<=9;z++)level.setBlockAndUpdate(new BlockPos(x,102,z),Blocks.STONE.defaultBlockState());}
            else if(step.equals("open")){for(int x=7;x<=9;x++)for(int z=7;z<=9;z++)level.setBlockAndUpdate(new BlockPos(x,102,z),Blocks.AIR.defaultBlockState());}
            else {
               if(level.getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(-6,99,-6,36,113,40)).isEmpty())for(int x:new int[]{8,11,14}){
                  var e=EntityTypes.HUSK.spawn(level,new BlockPos(x,104,15),EntitySpawnReason.COMMAND);
                  if(e!=null){e.setBaby(false);e.setNoAi(true);e.setNoGravity(true);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.setHealth(500);}
               }
               var moves=DevilFruitRegistry.movesFor(v.PlayerDevilFruit.getItem());
               for(int i=0;i<moves.size();i++)if(moves.get(i).id().equals(step))v.PlayerTechniqueUsedNumber=i;
               p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(v.PlayerDevilFruit));
            }
            v.syncPlayerVariables(p);return 1;
         })));
   }
}
