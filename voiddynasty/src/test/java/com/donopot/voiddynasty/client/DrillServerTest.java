package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import com.donopot.voiddynasty.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import java.util.Set;

/** Disposable client/server acceptance: actual hold-use packets, no production commands. */
public final class DrillServerTest implements ModInitializer {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("DRILLTEST");
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,env)->dispatcher.register(Commands.literal("drillfixture")
                .then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
                    try {String phase=StringArgumentType.getString(ctx,"phase");run(ctx.getSource().getPlayerOrException(),phase);
                        LOG.info("DRILL_SERVER PASS {}",phase);return 1;}
                    catch(Throwable error){LOG.error("DRILL_SERVER FAIL",error);return 0;}
                }))));
    }
    private static void check(boolean yes,String what){if(!yes)throw new AssertionError(what);}
    private static BlockPos at(int x){return new BlockPos(x,65,8);}
    private static int count(ServerPlayer player,Item item){return player.getInventory().getNonEquipmentItems().stream()
            .filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    private static void run(ServerPlayer player,String phase) {
        var level=player.level();
        switch(phase){
            case "setup" -> {
                for(int x=2;x<=24;x++)for(int z=2;z<=14;z++)for(int y=63;y<=69;y++)
                    level.setBlock(new BlockPos(x,y,z),(y==63?Blocks.POLISHED_ANDESITE:Blocks.AIR).defaultBlockState(),2|16);
                for(int x=2;x<=24;x++)for(int y=64;y<=67;y++)level.setBlock(new BlockPos(x,y,14),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),2|16);
                for(int x=3;x<=23;x+=4)level.setBlock(new BlockPos(x,66,13),Blocks.SEA_LANTERN.defaultBlockState(),2|16);
                for(int x=13;x<=15;x++)level.setBlock(at(x),Blocks.STONE.defaultBlockState(),3);
                level.setBlock(at(16),Blocks.BEDROCK.defaultBlockState(),3);
                level.setBlock(at(17),Blocks.DIAMOND_ORE.defaultBlockState(),3);
                player.setGameMode(GameType.SURVIVAL);
                player.getInventory().clearContent();
                player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.LASER_DRILL));
                player.teleportTo(level,8.5,64,8.5,Set.of(),-90,0,true);
            }
            case "mined" -> {
                for(int x=13;x<=15;x++)check(level.getBlockState(at(x)).isAir(),"drilled successive visible block "+x);
                check(count(player,Items.COBBLESTONE)==3,"exactly three collected cobblestone");
                check(level.getBlockState(at(16)).is(Blocks.BEDROCK),"unbreakable obstacle preserved");
                check(level.getBlockState(at(17)).is(Blocks.DIAMOND_ORE),"cannot drill through obstacle");
            }
            case "released" -> {
                check(!player.isUsingItem(),"server received use release");
                check(count(player,Items.COBBLESTONE)==3,"release generated no extra loot");
            }
            case "container" -> {
                level.setBlock(at(13),Blocks.CHEST.defaultBlockState(),3);
                ((Container)level.getBlockEntity(at(13))).setItem(0,new ItemStack(Items.DIAMOND,7));
                level.setBlock(at(14),Blocks.STONE.defaultBlockState(),3);
            }
            case "containerCheck" -> {
                check(level.getBlockState(at(13)).is(Blocks.CHEST),"container preserved");
                check(((Container)level.getBlockEntity(at(13))).getItem(0).getCount()==7,"container stock preserved");
                check(level.getBlockState(at(14)).is(Blocks.STONE),"beam cannot mine behind container");
            }
            case "protected" -> {
                level.setBlock(at(13),Blocks.STONE.defaultBlockState(),3);
                ClaimIntegration.forcerProtege(true);
            }
            case "protectedCheck" -> {
                try {check(level.getBlockState(at(13)).is(Blocks.STONE),"claimed terrain preserved");
                    check(count(player,Items.COBBLESTONE)==3,"protected pulse grants no loot");}
                finally {ClaimIntegration.forcerProtege(null);}
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
