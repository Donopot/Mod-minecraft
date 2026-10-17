package com.donopot.voiddynasty.work;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Empty/exhausted selection regression in a disposable world, independent of a depot. */
public final class WorkEmptyServerTest implements ModInitializer {
    private static int checks;
    private static final BlockPos ORIGIN=new BlockPos(10,64,24), TARGET=new BlockPos(16,64,24);
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(Commands.literal("emptycheck")
            .requires(src->src.getServer().isDedicatedServer())
            .then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
                String phase=StringArgumentType.getString(ctx,"phase");
                try {run(ctx.getSource().getLevel(),phase);ctx.getSource().sendSuccess(()->Component.literal("PASS "+phase+" checks="+checks),false);return 1;}
                catch(Throwable error) {ctx.getSource().sendFailure(Component.literal("FAIL "+phase+": "+error));return 0;}
            }))));
    }
    private static void check(boolean value,String message) {if(!value) throw new AssertionError(message);checks++;}
    private static void clean(ServerLevel level) {
        WorkState s=WorkState.get(level);WorkService.stop(level);WorkService.discardServitor(level,s);
        s.jobName=null;s.missionId="";s.missions.clear();s.journal.clear();s.invSlots.clear();s.depots.clear();s.hasDepot=false;s.rollbackRemaining=0;s.finished=true;
        for(int x=2;x<=27;x++) for(int z=16;z<=39;z++) {
            level.setBlock(new BlockPos(x,63,z),Blocks.SEA_LANTERN.defaultBlockState(),3);
            for(int y=64;y<=96;y++) level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
    }
    private static WorkState start(ServerLevel level,String name,BlockPos a,BlockPos b,BlockPos origin) {
        WorkState s=WorkService.start(level,name,a,b,"minecraft:iron_pickaxe",origin);
        check(s!=null,"empty selection accepted");return s;
    }
    private static void emptyFinished(ServerLevel level,WorkState s) {
        check(!s.hasJob() && s.finished && s.cursorY<s.minY,"empty selection reaches terminal cursor instead of waiting forever");
        check(s.servitorUuid.isEmpty() && !s.awaitingServitor && !s.buildingDepot && !s.hasTarget,"empty job clears worker and all pending preparation flags");
        check(!s.hasDepot && s.depots.isEmpty() && s.journal.isEmpty() && s.invSlots.isEmpty() && s.done==0,"empty selection creates no infrastructure, receipts or output");
        check(s.jobName==null && s.lastStop.startsWith("Zone vide:"),"truly empty native job releases slot with an explicit outcome");
        String orphan="";for(var entity:level.getAllEntities()) if(!entity.isRemoved() && entity.entityTags().contains("voiddynasty_work")) orphan+=entity.getUUID()+" at "+entity.position()+"; ";
        check(orphan.isEmpty(),"no live idle orphan worker remains after empty completion: "+orphan);
    }
    private static void run(ServerLevel level,String phase) throws Exception {
        WorkState s=WorkState.get(level);
        switch(phase) {
            case "emptySetup" -> {clean(level);start(level,"empty",TARGET,TARGET.offset(2,2,2),ORIGIN);}
            case "emptyCheck" -> {emptyFinished(level,s);check(s.supplyChests==64 && s.supplySupports==64,"empty selection never spends depot kit");}
            case "noKitSetup" -> {clean(level);s=start(level,"empty-no-kit",TARGET,TARGET,ORIGIN);s.supplyChests=0;}
            case "noKitCheck" -> {emptyFinished(level,s);check(s.supplyChests==0 && s.supplySupports==64,"empty completion does not require a usable construction kit");}
            case "noSpawnSetup" -> {
                clean(level);s=start(level,"empty-no-ground",TARGET.above(26),TARGET.above(28).offset(2,0,2),ORIGIN.above(26));
                check(s.awaitingServitor && s.servitorUuid.isEmpty(),"fixture has no safe worker spawn ground");
            }
            case "noSpawnCheck" -> emptyFinished(level,s);
            case "largeSetup" -> {clean(level);start(level,"large-empty",new BlockPos(12,64,20),new BlockPos(19,71,27),ORIGIN);}
            case "largeCheck" -> {emptyFinished(level,s);check(s.total==512,"bounded scan completes a selection larger than one tick budget");}
            case "staleSetup" -> {
                clean(level);level.setBlock(TARGET,Blocks.STONE.defaultBlockState(),3);s=start(level,"removed-last",TARGET,TARGET,ORIGIN);
                s.hasTarget=true;s.targetX=TARGET.getX();s.targetY=TARGET.getY();s.targetZ=TARGET.getZ();s.progress=1;
                level.setBlock(TARGET,Blocks.AIR.defaultBlockState(),3);
            }
            case "staleCheck" -> emptyFinished(level,s);
            case "protectedSetup" -> {
                clean(level);level.setBlock(TARGET,Blocks.BEDROCK.defaultBlockState(),3);level.setBlock(TARGET.east(),Blocks.CHEST.defaultBlockState(),3);
                ((Container)level.getBlockEntity(TARGET.east())).setItem(0,new ItemStack(Items.DIAMOND,7));
                start(level,"non-mineable",TARGET,TARGET.east(),ORIGIN);
            }
            case "protectedCheck" -> {
                check(!s.hasJob() && s.finished && !s.hasDepot && s.servitorUuid.isEmpty(),"non-mineable-only zone finishes without building or running in place");
                check(s.done==0 && s.skipped==2 && s.journal.isEmpty() && s.lootBlocks==1,"skipped blocks and container observations retained without false output");
                check(level.getBlockState(TARGET).is(Blocks.BEDROCK) && ((Container)level.getBlockEntity(TARGET.east())).getItem(0).getCount()==7,"unmineable block and chest contents preserved");
            }
            case "carrySetup" -> {
                clean(level);s=start(level,"exhausted-with-carry",TARGET,TARGET,ORIGIN);
                ItemStack carry=new ItemStack(Items.DIAMOND,3);carry.set(DataComponents.CUSTOM_NAME,Component.literal("Conserved carry"));s.invSlots.add(carry);
                WorkState.Entry e=new WorkState.Entry(TARGET,NbtUtils.writeBlockState(Blocks.DIAMOND_ORE.defaultBlockState()),0);
                e.dropsKnown=true;e.drops=List.of(carry.copy());s.journal.add(e);s.done=1;
            }
            case "carryCheck" -> {
                check(!s.hasJob() && s.finished && s.invSlots.isEmpty() && s.hasDepot && s.deposited==3,"exhausted work delivers existing carry before completion");
                int count=0;boolean named=true;
                for(BlockPos pos:s.depots) if(level.getBlockEntity(pos) instanceof Container c) for(int i=0;i<c.getContainerSize();i++) {
                    ItemStack item=c.getItem(i);if(item.is(Items.DIAMOND)) {count+=item.getCount();named &= item.has(DataComponents.CUSTOM_NAME);}
                }
                check(count==3 && named && s.journal.size()==1 && s.journal.getFirst().dropsKnown,"empty completion preserves exact named output and rollback receipt");
            }
            case "idle" -> {
                var idle=Servitor1Entity.class.getDeclaredMethod("clipIdle");idle.setAccessible(true);
                var entity=com.donopot.voiddynasty.registry.ModEntities.SERVITOR_1.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                check("idle".equals(idle.invoke(entity)),"stationary servitor selects idle instead of walk");
                try(var resource=WorkEmptyServerTest.class.getResourceAsStream("/assets/voiddynasty/geckolib/animations/servitor_1.animation.json")) {
                    var animations=JsonParser.parseReader(new InputStreamReader(resource,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
                    var pose=animations.getAsJsonObject("idle").getAsJsonObject("bones");
                    check(animations.has("walk") && pose.keySet().containsAll(List.of("leftleg","rightleg","leftarm","rightarm")),"moving clip preserved and resting pose covers animated limbs");
                    boolean stationary=true;for(String bone:pose.keySet()) for(var angle:pose.getAsJsonObject(bone).getAsJsonArray("rotation")) stationary &= angle.getAsDouble()==0;
                    check(stationary,"idle pose contains no running animation or keyframe motion");
                }
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
