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

/** Fixtures/observations only; mission submission and control are exercised through real MCP. */
public final class MissionServerTest implements ModInitializer {
    private static int checks;
    private static final BlockPos DESTINATION = new BlockPos(42,64,4);
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        checks++; System.out.println("[missioncheck] PASS " + name);
    }
    private static Container chest(ServerLevel level) { return (Container)level.getBlockEntity(DESTINATION); }
    private static int count(Container container, Item item) {
        int count = 0;
        for (int i=0; i<container.getContainerSize(); i++) if (container.getItem(i).is(item)) count += container.getItem(i).getCount();
        return count;
    }
    private static void fill(ServerLevel level) {
        Container c = chest(level); c.clearContent();
        for (int i=0; i<c.getContainerSize(); i++) c.setItem(i,new ItemStack(Items.EMERALD,64));
        c.setChanged();
    }
    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
            dispatcher.register(Commands.literal("missioncheck").requires(source -> source.getServer().isDedicatedServer())
                .then(Commands.argument("phase", StringArgumentType.word()).executes(ctx -> {
                    String phase = StringArgumentType.getString(ctx,"phase");
                    try {
                        run(ctx.getSource().getLevel(),phase);
                        ctx.getSource().sendSuccess(() -> Component.literal("PASS " + phase + " checks=" + checks),false);
                        return 1;
                    } catch (Throwable error) {
                        error.printStackTrace(); ctx.getSource().sendFailure(Component.literal("FAIL " + phase + ": " + error)); return 0;
                    }
                }))));
    }
    private static void run(ServerLevel level, String phase) {
        WorkState s = WorkState.get(level); WorkMission m = MissionService.active(s);
        switch (phase) {
            case "setup" -> {
                WorkService.stop(level); s.jobName=null; s.missionId=""; s.missions.clear(); s.journal.clear(); s.invSlots.clear();
                s.depots.clear(); s.hasDepot=false; s.finished=true; s.rollbackRemaining=0;
                for (int x=28; x<=61; x++) for (int z=0; z<=16; z++) {
                    level.setBlock(new BlockPos(x,63,z),Blocks.SEA_LANTERN.defaultBlockState(),3);
                    for (int y=64; y<=68; y++) level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
                }
                level.setBlock(DESTINATION,Blocks.CHEST.defaultBlockState(),3);
                chest(level).setItem(0,new ItemStack(Items.DIAMOND,7)); chest(level).setItem(1,new ItemStack(Items.COBBLESTONE,5));
                for (int x=34; x<=37; x++) level.setBlock(new BlockPos(x,64,4),Blocks.STONE.defaultBlockState(),3);
                level.setBlock(new BlockPos(38,64,4),Blocks.GOLD_BLOCK.defaultBlockState(),3);
                s.setDirty(); check(true,"fresh isolated mission arena");
            }
            case "success" -> {
                check(m != null && m.status.equals("SUCCEEDED") && m.collected==2 && m.delivered==2,"success requires actual harvest and delivery");
                check(count(chest(level),Items.COBBLESTONE)==7 && count(chest(level),Items.DIAMOND)==7,"existing chest contents do not count toward goal and survive delivery");
                int remaining=0;
                for (int x=34; x<=37; x++) if (level.getBlockState(new BlockPos(x,64,4)).is(Blocks.STONE)) remaining++;
                check(remaining==2 && level.getBlockState(new BlockPos(38,64,4)).is(Blocks.GOLD_BLOCK),"quantity stops mining early and non-target blocks remain");
                check(s.invSlots.isEmpty() && s.journal.size()==2 && s.depots.contains(DESTINATION),"delivery chest participates in receipt recovery");
            }
            case "fullSource" -> {
                fill(level); for (int x=34;x<=37;x++) level.setBlock(new BlockPos(x,64,4),Blocks.STONE.defaultBlockState(),3);
            }
            case "rolledBack" -> {
                check(m.status.equals("CANCELLED") && m.reason.equals("ROLLBACK_REQUESTED") && s.journal.isEmpty(),"MCP rollback invalidates goal and consumes only successful receipts");
                check(count(chest(level),Items.COBBLESTONE)==5 && count(chest(level),Items.DIAMOND)==7,"rollback debits only newly delivered output and preserves previous chest contents");
                boolean restored=true;
                for (int x=34;x<=37;x++) restored &= level.getBlockState(new BlockPos(x,64,4)).is(Blocks.STONE);
                check(restored && s.invSlots.isEmpty(),"MCP rollback physically restores mined blocks without duplicate output");
            }
            case "full" -> {
                check(m != null && m.status.equals("PAUSED") && m.reason.equals("DESTINATION_FULL"),"full destination pauses with machine readable reason");
                check(m.collected==1 && m.delivered==0 && s.invCount()==1 && s.journal.size()==1,"full destination preserves carried output and receipt");
                check(count(chest(level),Items.EMERALD)==27*64,"full chest contents never cleared");
            }
            case "freeSlot" -> { chest(level).setItem(0,ItemStack.EMPTY); chest(level).setChanged(); }
            case "recovered" -> {
                check(m.status.equals("SUCCEEDED") && m.collected==1 && m.delivered==1 && s.invSlots.isEmpty(),"resume delivers once without mining another block");
                check(count(chest(level),Items.COBBLESTONE)==1 && count(chest(level),Items.EMERALD)==26*64,"partial destination recovery preserves unrelated stacks");
            }
            case "cancelled" -> {
                check(m.status.equals("CANCELLED") && !s.hasJob() && s.invCount()==1 && s.journal.size()==1,"cancel retains bag and journal");
            }
            case "deposited" -> {
                check(m.status.equals("CANCELLED") && !s.hasJob() && s.invSlots.isEmpty(),"cancelled carry recovered through physical worker depot");
                int recovered=0;
                for (BlockPos pos:s.depots) if (!pos.equals(DESTINATION) && level.getBlockEntity(pos) instanceof Container container) recovered+=count(container,Items.COBBLESTONE);
                check(recovered==1 && s.journal.size()==1,"cancel recovery preserves exact output and rollback receipt");
            }
            case "emptySource" -> {
                chest(level).clearContent();
                for (int x=34;x<=38;x++) level.setBlock(new BlockPos(x,64,4),Blocks.GOLD_BLOCK.defaultBlockState(),3);
            }
            case "incomplete" -> {
                check(m.status.equals("INCOMPLETE") && m.reason.equals("SOURCE_EXHAUSTED") && m.delivered==0,"resource exhaustion is not reported as success");
                check(s.journal.isEmpty() && level.getBlockState(new BlockPos(34,64,4)).is(Blocks.GOLD_BLOCK),"resource filtering never clears unrelated terrain");
            }
            case "copperSource" -> {
                chest(level).clearContent(); for (int x=34;x<=35;x++) level.setBlock(new BlockPos(x,64,4),Blocks.COPPER_ORE.defaultBlockState(),3);
            }
            case "surplus" -> {
                check(m.status.equals("SUCCEEDED") && m.delivered==1 && m.collected>=2,"multi-drop ore delivers exact requested amount");
                check(count(chest(level),Items.RAW_COPPER)==1 && s.invSlots.isEmpty(),"surplus excluded from destination and carry emptied");
                int total=0;
                for (BlockPos pos:s.depots) if (level.getBlockEntity(pos) instanceof Container container) total+=count(container,Items.RAW_COPPER);
                check(total==m.collected && s.journal.size()==1,"surplus conserved across tracked depots");
                check(level.getBlockState(new BlockPos(35,64,4)).is(Blocks.COPPER_ORE),"surplus never triggers extra ore mining");
            }
            case "persisted" -> {
                check(m != null && m.status.equals("PAUSED") && m.collected==1 && m.delivered==0,"disk restart retains interrupted mission progress");
                check(!s.hasJob() && s.invCount()==1 && s.journal.size()==1 && s.missions.size()==6,"disk restart retains carry receipts and idempotency history");
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
