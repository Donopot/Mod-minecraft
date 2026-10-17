package com.donopot.voiddynasty.work;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Fixtures and independent observations only; real mission calls go through MCP. */
public final class WorkNextServerTest implements ModInitializer {
    private static int checks;
    static final BlockPos SOURCE=new BlockPos(44,64,10),ORE=new BlockPos(44,64,24),SITE=new BlockPos(50,64,10);
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,env)->dispatcher.register(Commands.literal("nextcheck").then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
            String phase=StringArgumentType.getString(ctx,"phase");
            try {run(ctx.getSource().getLevel(),phase);ctx.getSource().sendSuccess(()->Component.literal("PASS "+phase+" checks="+checks),false);return 1;}
            catch(Throwable e) {ctx.getSource().sendFailure(Component.literal("FAIL "+phase+" "+e));return 0;}
        }))));
    }
    private static void check(boolean value,String message) {if(!value) throw new AssertionError(message);checks++;}
    private static int count(Container c,Item item) {int n=0;for(int i=0;i<c.getContainerSize();i++) if(c.getItem(i).is(item)) n+=c.getItem(i).getCount();return n;}
    private static Container source(ServerLevel level) {return (Container)level.getBlockEntity(SOURCE);}
    private static int tracked(ServerLevel level,WorkState s,Item item) {
        int n=s.invSlots.stream().filter(stack->stack.is(item)).mapToInt(ItemStack::getCount).sum();
        for(BlockPos p:s.depots) if(level.getBlockEntity(p) instanceof Container c) n+=count(c,item);return n;
    }
    private static void arena(ServerLevel level) {
        for(int x=28;x<=62;x++) for(int z=0;z<=32;z++) {
            level.setBlock(new BlockPos(x,63,z),Blocks.SEA_LANTERN.defaultBlockState(),3);
            for(int y=64;y<=72;y++) level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
    }
    private static void run(ServerLevel level,String phase) {
        WorkState s=WorkState.get(level);WorkMission m=MissionService.active(s);
        switch(phase) {
            case "accessSetup" -> {
                check(s.jobName==null && s.invSlots.isEmpty() && s.journal.isEmpty(),"previous work reconciled before fixture");arena(level);
                for(int x=34;x<=48;x++) for(int z=18;z<=30;z++) for(int y=64;y<=68;y++) level.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),3);
                level.setBlock(ORE,Blocks.DIAMOND_ORE.defaultBlockState(),3);
                level.setBlock(SOURCE,Blocks.CHEST.defaultBlockState(),3);source(level).setItem(0,new ItemStack(Items.EMERALD,7));
            }
            case "accessDone" -> {
                check(m.status.equals("SUCCEEDED") && m.delivered==1 && m.accessMined>0,"hidden ore reached through real access excavation");
                check(count(source(level),Items.DIAMOND)==1 && count(source(level),Items.EMERALD)==7 && s.invSlots.isEmpty(),"exact delivery and unrelated stock preserved; access rubble stored");
                check(s.journal.size()>=m.accessMined && s.journal.size()<=m.accessMined+1 && s.journal.stream().allMatch(e->WorkAccess.contains(m,e.pos)),"every access block has a receipt inside authorized bounds");
                check(tracked(level,s,Items.COBBLESTONE)==s.journal.size()-1,"all excavated rubble conserved in actual depots");
                check(level.getBlockState(ORE).isAir() && level.getBlockState(ORE.above(2)).is(Blocks.STONE),"ore physically removed without unauthorized ceiling edits");
            }
            case "accessGuards" -> {
                BlockPos p=new BlockPos(48,64,30);
                check(WorkAccess.safe(level,s,p) && !WorkAccess.safe(level,s,p.east()),"access excavation stays inside explicit authorization");
                level.setBlock(p.above(),Blocks.WATER.defaultBlockState(),2);
                check(!WorkAccess.safe(level,s,p),"access refuses adjacent fluid before excavation");
                level.setBlock(p.above(),Blocks.GRAVEL.defaultBlockState(),2);
                check(!WorkAccess.safe(level,s,p),"access refuses a falling ceiling");
                level.setBlock(p.above(),Blocks.STONE.defaultBlockState(),2);level.setBlock(p,Blocks.CHEST.defaultBlockState(),2);
                check(!WorkAccess.safe(level,s,p),"access refuses block entities");
                level.setBlock(p,Blocks.BEDROCK.defaultBlockState(),2);
                check(!WorkAccess.safe(level,s,p),"access refuses unbreakable terrain");
                level.setBlock(p,Blocks.STONE.defaultBlockState(),2);
            }
            case "accessRollback" -> {
                check(s.journal.isEmpty() && s.invSlots.isEmpty() && tracked(level,s,Items.DIAMOND)==0 && tracked(level,s,Items.COBBLESTONE)==0,"access rollback consumed all exact output without duplication");
                boolean restored=true;
                for(int x=34;x<=48;x++) for(int z=18;z<=30;z++) for(int y=64;y<=68;y++) {
                    BlockPos p=new BlockPos(x,y,z);restored &= level.getBlockState(p).is(p.equals(ORE)?Blocks.DIAMOND_ORE:Blocks.STONE);
                }
                check(restored && count(source(level),Items.EMERALD)==7,"entire original wall restored and unrelated contents retained");
            }
            case "buildSetup" -> {
                check(s.jobName==null && s.invSlots.isEmpty() && s.journal.isEmpty(),"build fixture begins after reconciliation");arena(level);
                level.setBlock(SOURCE,Blocks.CHEST.defaultBlockState(),3);source(level).setItem(0,new ItemStack(Items.COBBLESTONE,64));source(level).setItem(1,new ItemStack(Items.COBBLESTONE,36));source(level).setItem(2,new ItemStack(Items.DIAMOND,7));
            }
            case "noMaterials" -> {source(level).setItem(0,ItemStack.EMPTY);source(level).setItem(1,ItemStack.EMPTY);}
            case "occupySite" -> {level.setBlock(SITE,Blocks.GOLD_BLOCK.defaultBlockState(),3);}
            case "clearSite" -> {
                check(s.jobName==null && s.invSlots.isEmpty() && level.getBlockState(SITE).is(Blocks.GOLD_BLOCK),"occupied site refused before changing work state or terrain");
                level.setBlock(SITE,Blocks.AIR.defaultBlockState(),3);
            }
            case "missing" -> {
                check(m.status.equals("PAUSED") && m.reason.equals("MATERIALS_MISSING") && !m.materialsReserved,"no partial reservation when materials missing");
                check(s.journal.isEmpty() && s.invSlots.isEmpty() && count(source(level),Items.DIAMOND)==7 && level.getBlockState(SITE).isAir(),"missing materials never spawn blocks or consume unrelated items");
            }
            case "refill" -> {source(level).setItem(0,new ItemStack(Items.COBBLESTONE,64));source(level).setItem(1,new ItemStack(Items.COBBLESTONE,36));}
            case "platformDone" -> {
                check(m.status.equals("SUCCEEDED") && m.placed==9 && m.delivered==9 && m.materialsReserved,"platform completion reflects nine real placements");
                check(count(source(level),Items.COBBLESTONE)==91 && count(source(level),Items.DIAMOND)==7 && s.invSlots.isEmpty(),"build consumes exactly nine source materials");
                check(m.build.blocks().stream().allMatch(p->level.getBlockState(p).is(Blocks.COBBLESTONE)) && s.journal.stream().allMatch(e->e.placed!=null && e.dropsKnown),"all plan blocks verified and refundable receipts recorded");
            }
            case "buildRollback" -> {
                check(m.status.equals("CANCELLED") && s.journal.isEmpty() && s.invCount()==m.amount,"placement rollback refunds actual material costs");
                check(m.build.blocks().stream().allMatch(p->level.getBlockState(p).isAir()) && tracked(level,s,Items.COBBLESTONE)==100,"rollback removes construction and conserves all original material");
            }
            case "recovered" -> {check(s.invSlots.isEmpty() && !s.hasJob() && tracked(level,s,Items.COBBLESTONE)==100 && count(source(level),Items.DIAMOND)==7,"refund recovery deposits everything without loss");}
            case "buildCancelled" -> {
                check(m.status.equals("CANCELLED") && m.placed>0 && m.placed<m.amount && s.invCount()==m.amount-m.placed,"cancel preserves unspent reserved building material");
                check(tracked(level,s,Items.COBBLESTONE)+m.placed==100,"cancel conserves source, carry and actually placed blocks");
            }
            case "cancelCarryRecovered" -> {check(s.invSlots.isEmpty() && tracked(level,s,Items.COBBLESTONE)+m.placed==100,"deposit preserves unspent reservation after partial cancellation");}
            case "cancelBuildRolledBack" -> {
                check(s.journal.isEmpty() && m.build.blocks().stream().allMatch(p->level.getBlockState(p).isAir()),"partial build rollback restores empty site");
                check(tracked(level,s,Items.COBBLESTONE)==100,"partial refunds do not duplicate already deposited unused reservation");
            }
            case "housePartial" -> {
                check(m.build!=null && m.status.equals("PAUSED") && m.materialsReserved && m.placed>0 && m.placed<m.amount,"house paused after real partial placement");
                check(s.invCount()+m.placed==m.amount && count(source(level),Items.COBBLESTONE)==100-m.amount,"reserved house material conserved across bag and construction");
            }
            case "housePersisted" -> {
                check(m.build!=null && m.status.equals("PAUSED") && m.materialsReserved && m.placed==s.journal.size(),"build progress and placement receipt type persist after restart");
                check(s.invCount()+m.placed==m.amount && s.journal.stream().allMatch(e->e.placed!=null && e.dropsKnown),"unspent material and exact placement costs survive restart");
            }
            case "houseDone" -> {
                check(m.status.equals("SUCCEEDED") && m.placed==94 && s.invSlots.isEmpty(),"full house finishes after restart with actual block count");
                check(m.build.blocks().stream().allMatch(p->level.getBlockState(p).is(Blocks.COBBLESTONE)) && count(source(level),Items.COBBLESTONE)==6,"house matches complete versioned plan and actual cost");
                check(level.getBlockState(SITE.offset(1,1,0)).isAir() && level.getBlockState(SITE.offset(2,2,0)).isAir(),"two-block-wide entrance remains usable by the servitor");
            }
            case "tamper" -> {WorkState.Entry e=s.journal.getLast();level.setBlock(e.pos,Blocks.GOLD_BLOCK.defaultBlockState(),3);}
            case "tampered" -> {
                check(s.rollbackRemaining==0 && !s.journal.isEmpty() && s.invSlots.isEmpty(),"changed placement refuses rollback without refund");
                check(level.getBlockState(s.journal.getLast().pos).is(Blocks.GOLD_BLOCK),"external player block preserved");
            }
            case "repair" -> {level.setBlock(s.journal.getLast().pos,m.build.state(),3);}
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
