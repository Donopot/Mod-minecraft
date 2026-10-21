package com.donopot.voiddynasty.work;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Real movement/mining/return fixtures in the disposable server, no teleporting of the worker. */
public final class WorkNavigationServerTest implements ModInitializer {
    private static final BlockPos ORIGIN=new BlockPos(10,64,10), DEST=new BlockPos(8,64,20), DOWN=new BlockPos(28,60,10), UP=new BlockPos(28,68,10);
    private static int checks;private static double lowest=1000,highest=-1000;private static BlockPos changed;
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,env)->dispatcher.register(Commands.literal("navcheck")
                .requires(src->src.getServer().isDedicatedServer())
                .then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
                    String phase=StringArgumentType.getString(ctx,"phase");
                    try {run(ctx.getSource().getLevel(),phase);ctx.getSource().sendSuccess(()->Component.literal("PASS "+phase+" checks="+checks),false);return 1;}
                    catch(Throwable e) {ctx.getSource().sendFailure(Component.literal("FAIL "+phase+" "+e));return 0;}
                }))));
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for(ServerLevel level:server.getAllLevels()) {
                WorkState s=WorkState.get(level);
                var bot=s.servitorUuid.isEmpty()?null:level.getEntityInAnyDimension(java.util.UUID.fromString(s.servitorUuid));
                if(bot!=null && s.hasJob()) {lowest=Math.min(lowest,bot.getY());highest=Math.max(highest,bot.getY());}
            }
        });
    }
    private static void check(boolean value,String message) {if(!value) throw new AssertionError(message);checks++;}
    private static int count(Container c,Item item) {int n=0;for(int i=0;i<c.getContainerSize();i++) if(c.getItem(i).is(item)) n+=c.getItem(i).getCount();return n;}
    private static int tracked(ServerLevel level,WorkState s,Item item) {
        int n=s.invSlots.stream().filter(stack->stack.is(item)).mapToInt(ItemStack::getCount).sum();
        for(BlockPos p:s.depots) if(level.getBlockEntity(p) instanceof Container c) n+=count(c,item);return n;
    }
    private static void arena(ServerLevel level) {
        WorkState s=WorkState.get(level);
        check(s.jobName==null && s.invSlots.isEmpty() && s.journal.isEmpty(),"previous work reconciled");
        for(int x=2;x<=36;x++) for(int z=2;z<=30;z++) for(int y=57;y<=76;y++)
            level.setBlock(new BlockPos(x,y,z),(y<=63?Blocks.STONE:Blocks.AIR).defaultBlockState(),2|16);
        level.setBlock(DEST,Blocks.CHEST.defaultBlockState(),3);
        ((Container)level.getBlockEntity(DEST)).setItem(0,new ItemStack(Items.EMERALD,7));
        lowest=1000;highest=-1000;
    }
    private static void submit(ServerLevel level,BlockPos target,String access,BlockPos min,BlockPos max) {
        level.setBlock(target,Blocks.DIAMOND_ORE.defaultBlockState(),3);
        String command="work mission gather minecraft:diamond 1 "+min.toShortString().replace(",","")+" "+max.toShortString().replace(",","")
                +" "+DEST.toShortString().replace(",","")+" "+ORIGIN.toShortString().replace(",","")+" "+access;
        try {check(level.getServer().getCommands().getDispatcher().execute(command,level.getServer().createCommandSourceStack().withLevel(level))==1,"solo gather command accepted");}
        catch(com.mojang.brigadier.exceptions.CommandSyntaxException error) {throw new IllegalStateException(error);}
    }
    private static void completed(ServerLevel level,WorkState s,boolean excavated) {
        WorkMission m=MissionService.active(s);
        check(m.status.equals("SUCCEEDED") && m.delivered==1 && !s.hasJob(),"real mission completed: "+MissionService.status(level,"current"));
        check(count((Container)level.getBlockEntity(DEST),Items.DIAMOND)==1 && count((Container)level.getBlockEntity(DEST),Items.EMERALD)==7,"delivered exact ore, unrelated inventory preserved");
        check(s.invSlots.isEmpty() && s.servitorUuid.isEmpty(),"returned all rubble before completion and removed worker");
        check(s.journal.stream().allMatch(e->WorkAccess.contains(m,e.pos) && e.dropsKnown),"every terrain edit authorized and recoverable");
        check(tracked(level,s,Items.COBBLESTONE)==s.journal.size()-1,"all excavated stone conserved");
        check(excavated ? m.accessMined>0 : m.accessMined==0,"excavation used only when explicitly enabled");
    }
    private static void run(ServerLevel level,String phase) {
        WorkState s=WorkState.get(level);WorkMission m=MissionService.active(s);
        switch(phase) {
            case "begin" -> {if(!s.journal.isEmpty()) WorkService.requestRollback(level,s,Integer.MAX_VALUE);}
            case "reconcile" -> {if(s.jobName!=null) check(WorkService.commit(level,s),"previous fixture reconciled before navigation suite");}
            case "downSetup" -> {arena(level);submit(level,DOWN,"excavate",new BlockPos(12,59,5),new BlockPos(32,68,15));}
            case "downDone" -> {completed(level,s,true);check(lowest<63,"worker physically descended: "+lowest);check(level.getBlockState(DOWN).isAir(),"buried ore removed");}
            case "upSetup" -> {
                arena(level);
                // Existing three-wide staircase, with four one-block steps and broad landings.
                for(int x=14;x<=32;x++) for(int z=7;z<=13;z++) for(int y=64;y<64+Math.min(4,1+(x-14)/3);y++)
                    level.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),2|16);
                submit(level,UP,"surface",UP,UP);
            }
            case "upDone" -> {completed(level,s,false);check(highest>65,"worker physically climbed existing stairs: "+highest);}
            case "upExcavateSetup" -> {
                arena(level);
                for(int x=12;x<=32;x++) for(int z=5;z<=15;z++) for(int y=64;y<=71;y++)
                    level.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),2|16);
                submit(level,UP,"excavate",new BlockPos(12,64,5),new BlockPos(32,71,15));
            }
            case "upExcavateDone" -> {completed(level,s,true);check(highest>65,"worker physically climbed excavated landings: "+highest);}
            case "rollback" -> WorkService.requestRollback(level,s,Integer.MAX_VALUE);
            case "restored" -> {
                check(s.journal.isEmpty() && s.invSlots.isEmpty() && tracked(level,s,Items.DIAMOND)==0 && tracked(level,s,Items.COBBLESTONE)==0,"rollback restores all terrain and debits exact drops");
                check(level.getBlockState(m.min.equals(UP)?UP: m.min.getY()==59?DOWN:UP).is(Blocks.DIAMOND_ORE),"original ore restored");
                boolean terrain=true;BlockPos ore=m.min.getY()==59?DOWN:UP;
                for(BlockPos p:BlockPos.betweenClosed(m.min,m.max)) {
                    var expected=p.equals(ore)?Blocks.DIAMOND_ORE:m.min.getY()==59 && p.getY()>63?Blocks.AIR:Blocks.STONE;
                    terrain &= level.getBlockState(p).is(expected);
                }
                check(terrain,"entire authorized terrain matches original fixture after rollback");
                check(count((Container)level.getBlockEntity(DEST),Items.EMERALD)==7,"rollback retains unrelated stock");
                check(WorkService.commit(level,s),"reconciled job closed");
            }
            case "progress" -> {
                if(s.done<3 || s.accessRoute.isEmpty()) throw new IllegalStateException("WAIT "+s.done+" "+s.lastStop);
                check(s.hasJob(),"mission actively excavating");
            }
            case "pause" -> {check(WorkService.stop(level),"pause after real excavation");check(!s.journal.isEmpty(),"partial work has receipts");}
            case "persisted" -> {
                check(m!=null && m.status.equals("PAUSED") && m.accessMined>0 && s.accessRoute.isEmpty(),"partial work persists; transient route must replan");
                check(tracked(level,s,Items.COBBLESTONE)==s.journal.size(),"partial excavation resources persist exactly");
            }
            case "resume" -> {check(WorkService.resume(level)!=null,"restart or changed terrain explicitly resumed");}
            case "changeFloor" -> {
                check(s.hasJob() && s.accessFrom!=null && !s.accessRoute.isEmpty(),"vertical access plan in progress");
                changed=s.accessRoute.getLast().below().immutable();level.setBlock(changed,Blocks.AIR.defaultBlockState(),2|16);
            }
            case "changed" -> {
                check(!s.hasJob() && m.reason.equals("ACCESS_TERRAIN_CHANGED"),"removed landing floor pauses excavation: "+s.lastStop);
                check(tracked(level,s,Items.COBBLESTONE)==s.journal.size(),"terrain change loses no mined output");
                level.setBlock(changed,Blocks.STONE.defaultBlockState(),2|16);
            }
            case "restrictedSetup" -> {arena(level);submit(level,DOWN,"excavate",DOWN,DOWN);}
            case "restricted" -> {
                check(!s.hasJob() && m.reason.equals("STAIR_NO_ROUTE") && s.journal.isEmpty(),"no mining outside a too-small authorization");
                check(level.getBlockState(DOWN).is(Blocks.DIAMOND_ORE) && s.invSlots.isEmpty(),"unreachable ore and stock remain intact");
                MissionService.control(level,m.id,"cancel");check(WorkService.commit(level,s),"refused access closed");
            }
            case "guards" -> {
                arena(level);submit(level,DOWN,"excavate",new BlockPos(12,59,5),new BlockPos(32,68,15));
                BlockPos from=new BlockPos(16,64,10), to=from.east().below();
                check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)!=null,"valid descending landing accepted");
                level.setBlock(to.below(),Blocks.AIR.defaultBlockState(),2|16);
                check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"unsupported landing refused");
                level.setBlock(to.below(),Blocks.MAGMA_BLOCK.defaultBlockState(),2|16);
                check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"damaging magma floor refused");
                level.setBlock(to.below(),Blocks.STONE.defaultBlockState(),2|16);
                level.setBlock(to,Blocks.CHEST.defaultBlockState(),2|16);check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"container in access refused");
                level.setBlock(to,Blocks.BEDROCK.defaultBlockState(),2|16);check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"unbreakable access refused");
                level.setBlock(to,Blocks.STONE.defaultBlockState(),2|16);level.setBlock(to.above(2),Blocks.WATER.defaultBlockState(),2|16);
                check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"water in return headroom refused");
                level.setBlock(to.above(2),Blocks.LAVA.defaultBlockState(),2|16);check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"lava in return headroom refused");
                level.setBlock(to.above(2),Blocks.GRAVEL.defaultBlockState(),2|16);check(StairMining.step(level,MissionService.active(s),from,net.minecraft.core.Direction.EAST)==null,"falling ceiling refused");
                level.setBlock(to.above(2),Blocks.AIR.defaultBlockState(),2|16);
                MissionService.control(level,s.missionId,"cancel");check(WorkService.commit(level,s),"guard fixture closed");
            }
            case "oscillation" -> {
                WorkState isolated=new WorkState();
                for(int n=0;n<405;n++) WorkService.navigationStalled(isolated,new BlockPos(n%2,64,0),false);
                check(isolated.idleTicks>400,"running between old cells is detected as stalled");
                check(!WorkService.navigationStalled(isolated,new BlockPos(0,64,0),true) && isolated.idleTicks==0,"real work resets watchdog");
                boolean progressing=true;for(int n=0;n<500;n++) progressing &= !WorkService.navigationStalled(isolated,new BlockPos(n,64,0),false);
                check(progressing,"long detour through new path cells counts as progress");
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
