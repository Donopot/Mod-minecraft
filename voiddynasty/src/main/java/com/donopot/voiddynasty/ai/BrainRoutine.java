package com.donopot.voiddynasty.ai;

import com.donopot.voiddynasty.work.MissionService;
import com.donopot.voiddynasty.work.WorkChunkTickets;
import net.minecraft.world.level.ChunkPos;
import java.util.Set;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import java.util.UUID;

/** Native mission, observed delivery, then a verified return. */
public final class BrainRoutine {
    private static final long MAX_TICKS = 48000;
    private BrainRoutine() {}
    public static String start(ServerLevel level, LivingEntity bot, String spec) {
        if (bot == null) return "REFUSE: aucun servitor";
        JsonObject request = JsonParser.parseString(spec).getAsJsonObject();
        var mission = MissionService.parse(request);
        if (!request.get("type").getAsString().equals("GATHER")) return "REFUSE: routine_gather exige une mission GATHER";
        BrainRoutineState state = BrainRoutineState.get(level);
        for (var entry : state.entries.values()) {
            if (entry.bot().equals(bot.getUUID().toString()))
                return "REFUSE: routine deja suivie " + entry.mission()
                        + "; /servitor routine resume ou cancel";
        }
        JsonObject result = MissionService.submit(level, request, bot);
        if (!result.get("ok").getAsBoolean()) return "REFUSE: " + result;
        String id = result.getAsJsonObject("mission").get("id").getAsString();
        state.put(new BrainRoutineState.Entry(id, bot.getUUID().toString(), mission.origin,
                mission.requestId, "WORKING", "", level.getGameTime(), 0));
        keepTicking(level, id, bot);
        return "routine lancee : " + id + "; retour en " + mission.origin.toShortString();
    }
    public static void tick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            BrainRoutineState state = BrainRoutineState.get(level);
            for (var entry : List.copyOf(state.entries.values())) {
                if (entry.phase().equals("PAUSED")) continue;
                try {
                    var entity = level.getEntity(UUID.fromString(entry.bot()));
                    if (entity instanceof LivingEntity bot && bot.isAlive()) keepTicking(level, entry.mission(), bot);
                    if (level.getGameTime() % 20 == 0) advance(level, state, entry);
                } catch (RuntimeException e) { pause(level, state, entry, "EXECUTION_ERROR: " + e.getMessage()); }
            }
        }
    }
    private static void advance(ServerLevel level, BrainRoutineState state, BrainRoutineState.Entry entry) {
        if (level.getGameTime() - entry.started() > MAX_TICKS) {
            pause(level, state, entry, "TIMEOUT"); return;
        }
        var entity = level.getEntity(UUID.fromString(entry.bot()));
        if (!(entity instanceof LivingEntity bot) || !bot.isAlive()) {
            pause(level, state, entry, "WORKER_UNAVAILABLE"); return;
        }
        if (entry.phase().equals("RETURNING")) {
            BrainTask task = state.returns.get(entry.mission());
            TaskRunner.Result result = task == null ? TaskRunner.Result.CANCELLED : TaskRunner.result(bot, task);
            if (result == TaskRunner.Result.RUNNING) return;
            if (result == TaskRunner.Result.DONE && bot.blockPosition().distSqr(entry.origin()) <= 6.25) {
                WorkChunkTickets.release(level, "routine:" + entry.mission());
                state.remove(entry.mission());
                ServitorBrain.note("routine terminee : " + entry.delivered()
                        + " objet(s) livres, retour confirme en " + entry.origin().toShortString());
            } else pause(level, state, entry, "RETURN_" + result);
            return;
        }
        JsonObject status = MissionService.status(level, entry.mission());
        if (!status.has("mission")) { pause(level, state, entry, "MISSION_NOT_FOUND"); return; }
        JsonObject mission = status.getAsJsonObject("mission");
        String phase = mission.get("status").getAsString();
        switch (phase) {
            case "SUCCEEDED" -> {
                if (TaskRunner.current(bot) != null) { pause(level, state, entry, "ACTION_BUSY"); return; }
                BrainTask task = new BrainTasks.Goto(entry.origin(), 180);
                TaskRunner.assign(level, bot, task);
                state.returns.put(entry.mission(), task);
                state.put(entry.with("RETURNING", "", entry.started(), mission.get("delivered_total").getAsInt()));
            }
            case "INCOMPLETE", "CANCELLED", "FAILED" -> {
                WorkChunkTickets.release(level, "routine:" + entry.mission());
                state.remove(entry.mission());
                ServitorBrain.note("routine arretee : " + phase + " — " + mission.get("reason").getAsString()
                        + ", " + mission.get("delivered_total").getAsInt() + " objet(s) livres");
            }
            case "PAUSED" -> pause(level, state, entry, mission.get("reason").getAsString());
            default -> { }
        }
    }
    private static void keepTicking(ServerLevel level, String id, LivingEntity bot) {
        WorkChunkTickets.acquire(level, "routine:" + id, Set.of(ChunkPos.containing(bot.blockPosition())));
    }
    private static void pause(ServerLevel level, BrainRoutineState state, BrainRoutineState.Entry entry, String reason) {
        var entity = level.getEntity(UUID.fromString(entry.bot()));
        BrainTask task = state.returns.remove(entry.mission());
        if (entity instanceof LivingEntity bot && task != null && TaskRunner.current(bot) == task)
            TaskRunner.cancel(bot, level, reason);
        MissionService.control(level, entry.mission(), "pause");
        WorkChunkTickets.release(level, "routine:" + entry.mission());
        state.put(entry.with("PAUSED", reason, entry.started(), entry.delivered()));
        ServitorBrain.note("routine " + entry.mission() + " en pause : " + reason
                + "; /servitor routine resume " + entry.mission());
    }
    public static String resume(ServerLevel level, String id) {
        BrainRoutineState state = BrainRoutineState.get(level);
        var entry = state.entries.get(id);
        if (entry == null || !entry.phase().equals("PAUSED")) return "REFUSE: aucune routine en pause";
        var entity = level.getEntity(UUID.fromString(entry.bot()));
        if (!(entity instanceof LivingEntity bot) || !bot.isAlive()) return "REFUSE: compagnon absent";
        if (TaskRunner.current(bot) != null) return "REFUSE: compagnon occupe";
        JsonObject status = MissionService.status(level, id);
        if (!status.has("mission")) return "REFUSE: mission introuvable";
        if (status.getAsJsonObject("mission").get("status").getAsString().equals("PAUSED")) {
            JsonObject result = MissionService.control(level, id, "resume");
            if (!result.get("ok").getAsBoolean()) return "REFUSE: " + result;
        }
        state.put(entry.with("WORKING", "", level.getGameTime(), entry.delivered()));
        keepTicking(level, id, bot);
        return "routine reprise : " + id;
    }
    public static String cancel(ServerLevel level, String id) {
        BrainRoutineState state = BrainRoutineState.get(level);
        var entry = state.entries.get(id);
        if (entry == null) return "REFUSE: routine introuvable";
        var entity = level.getEntity(UUID.fromString(entry.bot()));
        BrainTask task = state.returns.get(id);
        if (entity instanceof LivingEntity bot && task != null && TaskRunner.current(bot) == task)
            TaskRunner.cancel(bot, level, "routine annulee");
        MissionService.control(level, id, "pause");
        WorkChunkTickets.release(level, "routine:" + id);
        state.remove(id);
        return "routine annulee; mission et ressources conservees";
    }
    public static void cancel(ServerLevel level, UUID bot) {
        for (var entry : List.copyOf(BrainRoutineState.get(level).entries.values()))
            if (entry.bot().equals(bot.toString())) cancel(level, entry.mission());
    }
    public static String describe(ServerLevel level) {
        var entries = BrainRoutineState.get(level).entries.values();
        if (entries.isEmpty()) return "aucune routine en cours";
        return String.join("\n", entries.stream().map(e -> e.mission() + " : " + e.label()
                + " — " + e.phase() + " " + e.reason() + " — retour " + e.origin().toShortString()).toList());
    }
}
