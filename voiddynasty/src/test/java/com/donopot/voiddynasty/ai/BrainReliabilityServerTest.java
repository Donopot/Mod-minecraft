package com.donopot.voiddynasty.ai;

import com.donopot.voiddynasty.work.*;
import com.google.gson.JsonParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Runs only in a disposable server, with no player nearby and no model connection. */
public final class BrainReliabilityServerTest implements ModInitializer {
    private static final BlockPos ORIGIN = new BlockPos(1002,64,1002), DEST = new BlockPos(1003,64,1008);
    private static BrainTask movement;
    private static LivingEntity observed;
    private static int checks;
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> dispatcher.register(
            Commands.literal("reliabilitycheck").requires(s -> s.getServer().isDedicatedServer())
            .then(Commands.argument("phase", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
                String phase = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx,"phase");
                try { run(ctx.getSource().getLevel(), phase);
                    ctx.getSource().sendSuccess(() -> Component.literal("PASS " + phase + " checks=" + checks), false); return 1;
                } catch (Throwable e) { e.printStackTrace(); ctx.getSource().sendFailure(Component.literal("FAIL " + phase + ": " + e)); return 0; }
            }))));
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); checks++; }
    private static LivingEntity bot(ServerLevel level) { return BrainSpawn.find(level); }
    private static int count(Container c, Item item) {
        int count=0; for(int i=0;i<c.getContainerSize();i++) if(c.getItem(i).is(item)) count+=c.getItem(i).getCount(); return count;
    }
    private static String spec(String request) {
        return "{\"schema\":1,\"type\":\"GATHER\",\"request_id\":\""+request+"\",\"item\":\"minecraft:cobblestone\",\"amount\":3,"
                +"\"min\":[1008,64,1002],\"max\":[1010,64,1002],\"destination\":[1003,64,1008],\"origin\":[1002,64,1002],\"access\":\"surface\"}";
    }
    private static void run(ServerLevel level, String phase) throws Exception {
        WorkState work = WorkState.get(level);
        BrainRoutineState routines = BrainRoutineState.get(level);
        switch(phase) {
            case "notes" -> {
                BrainNotes.add(level, null, "test de persistance alpha94");
                check(BrainNotes.list(level, 10).contains("minecraft:overworld"), "notes expose dimension");
                var path = BrainNotes.path(level);
                String invalid = "{\"unexpected\":true}";
                java.nio.file.Files.writeString(path, invalid);
                for (int attempt=0; attempt<2; attempt++) {
                    boolean refused = false;
                    try { BrainNotes.add(level, null, "must not overwrite"); }
                    catch (BrainNotes.BrainNotesException expected) { refused = true; }
                    check(refused, "corrupt notes refuse every subsequent write");
                    check(java.nio.file.Files.readString(path).equals(invalid), "corrupt original retained");
                }
                BrainNotes.clear(level);
                BrainNotes.add(level, null, "test de persistance alpha94");
                var unknown = JsonParser.parseString(spec("invalid-type")).getAsJsonObject();
                unknown.addProperty("type", "UNKNOWN");
                boolean refused=false;
                try { MissionService.parse(unknown); } catch (IllegalArgumentException expected) { refused=true; }
                check(refused, "unknown mission type rejected");
            }
            case "setup" -> {
                check(work.jobName==null || WorkService.commit(level,work),"previous fixture reconciled");
                for(int x=998;x<=1036;x++) for(int z=998;z<=1012;z++) for(int y=63;y<=68;y++)
                    level.setBlock(new BlockPos(x,y,z),(y==63?Blocks.STONE:Blocks.AIR).defaultBlockState(),2|16);
                level.setBlock(DEST,Blocks.CHEST.defaultBlockState(),3);
                ((Container)level.getBlockEntity(DEST)).setItem(0,new ItemStack(Items.EMERALD,7));
                check(BrainSpawn.spawn(level,ORIGIN,BrainSpawn.Variant.LOURD)!=null,"spawned heavy companion");
                var bot=bot(level); observed=bot; ((Mob)bot).setNoAi(true);
                movement=new BrainTasks.Goto(new BlockPos(1030,64,1002),30);
                TaskRunner.assign(level,bot,movement);
                check(!((Mob)bot).isNoAi(),"all tasks wake a reloaded companion");
            }
            case "moved" -> {
                var bot=observed;
                check(bot!=null && bot.getX()>1026,"crossed chunk boundary without player: " +(bot==null?"missing":bot.position()));
                check(TaskRunner.result(bot,movement)==TaskRunner.Result.DONE,"arrival recorded");
            }
            case "errors" -> {
                var bot=bot(level);
                // A failing cleanup must not escape the server tick handler.
                BrainTask bad=new BrainTask(){ public String describe(){return "fault fixture";}
                    public Status tick(Ctx ctx){throw new IllegalStateException("tick failure");}
                    public void cancel(Ctx ctx){throw new IllegalStateException("cancel failure");}};
                TaskRunner.assign(level,bot,bad); TaskRunner.tick(level.getServer());
                check(TaskRunner.result(bot,bad)==TaskRunner.Result.FAILED,"tick and cleanup errors contained");
            }
            case "incomplete" -> {
                var bot=bot(level); var m=MissionService.parse(JsonParser.parseString(spec("incomplete-fixture")).getAsJsonObject());
                m.status="INCOMPLETE"; m.reason="SOURCE_EXHAUSTED"; m.collected=1; m.delivered=1; work.missions.add(m);
                routines.put(new BrainRoutineState.Entry(m.id,bot.getUUID().toString(),ORIGIN,m.requestId,"WORKING","",level.getGameTime(),0));
            }
            case "incompleteDone" -> {
                check(routines.entries.isEmpty(),"INCOMPLETE ends observation immediately");
                check(ServitorBrain.auditLines().stream().anyMatch(s->s.contains("SOURCE_EXHAUSTED")),"actual failure reported");
            }
            case "gather" -> {
                for(int x=1008;x<=1010;x++) level.setBlock(new BlockPos(x,64,1002),Blocks.STONE.defaultBlockState(),3);
                observed=bot(level);
                String result=BrainRoutine.start(level,observed,spec("full-cycle"));
                check(!result.startsWith("REFUSE"),"native gather accepted: "+result);
            }
            case "returned" -> {
                var bot=observed; var mission=work.missions.stream().filter(m->m.requestId.equals("full-cycle")).findFirst().orElseThrow();
                check(mission.status.equals("SUCCEEDED"),"gather delivered: "+MissionService.status(level,mission.id));
                check(routines.entries.isEmpty(),"return finished: "+BrainRoutine.describe(level));
                check(bot.blockPosition().distSqr(ORIGIN)<=6.25,"arrival verified at origin");
                Container chest=(Container)level.getBlockEntity(DEST);
                check(count(chest,Items.COBBLESTONE)==3 && count(chest,Items.EMERALD)==7,"exact delivery and unrelated stock preserved");
                check(work.invSlots.isEmpty(),"work bag emptied");
                check(WorkService.commit(level,work),"operator closes previous receipt");
                // Reuse terminal mission to isolate return persistence from the already tested mining.
                routines.put(new BrainRoutineState.Entry(mission.id,bot.getUUID().toString(),new BlockPos(1030,64,1002),"restart-return","RETURNING","",level.getGameTime(),3));
            }
            case "restart" -> {
                check(BrainNotes.list(level, 10).contains("test de persistance alpha94"), "notes survived restart");
                check(routines.entries.size()==1,"routine survived server restart");
                var e=routines.entries.values().iterator().next();
                check(e.phase().equals("PAUSED") && e.reason().equals("SERVER_RESTART"),"restart never silently moves worker");
                check(TaskRunner.current(bot(level))==null,"old task handle discarded");
                observed=bot(level);
                check(BrainRoutine.resume(level,e.mission()).startsWith("routine reprise"),"explicit return resume accepted");
            }
            case "resumed" -> {
                check(routines.entries.isEmpty(),"resumed return completed");
                check(observed.getX()>1026,"resumed return physically arrived");
                check(BrainRoutine.start(level,bot(level),spec("second-empty")).startsWith("routine lancee"),"second native mission accepted");
            }
            case "second" -> {
                check(routines.entries.isEmpty(),"second exhausted mission reports completion without hanging");
                var mission=work.missions.stream().filter(m->m.requestId.equals("second-empty")).findFirst().orElseThrow();
                check(mission.status.equals("INCOMPLETE"),"exhausted source remains honest");
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
