package com.donopot.voiddynasty.work;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import java.util.Set;
import java.util.UUID;

/** Persistent high-level intent; physical inventory and receipts remain in the same WorkState. */
public final class WorkMission {
    public static final Set<String> TERMINAL = Set.of("SUCCEEDED", "INCOMPLETE", "CANCELLED");
    public final String id, requestId, item;
    public final int amount;
    public final BlockPos min, max, destination, origin;
    public String status = "PREPARING", reason = "";
    public int collected, delivered;

    public WorkMission(String id, String requestId, String item, int amount, BlockPos min, BlockPos max,
                       BlockPos destination, BlockPos origin) {
        UUID.fromString(id);
        this.id = id; this.requestId = requestId; this.item = item; this.amount = amount;
        this.min = min.immutable(); this.max = max.immutable();
        this.destination = destination.immutable(); this.origin = origin.immutable();
    }

    public boolean terminal() { return TERMINAL.contains(status); }

    public boolean sameRequest(WorkMission other) {
        return requestId.equals(other.requestId) && item.equals(other.item) && amount == other.amount
                && min.equals(other.min) && max.equals(other.max) && destination.equals(other.destination)
                && origin.equals(other.origin);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id); tag.putString("request", requestId); tag.putString("item", item);
        tag.putInt("amount", amount); tag.putLong("min", min.asLong()); tag.putLong("max", max.asLong());
        tag.putLong("destination", destination.asLong()); tag.putLong("origin", origin.asLong());
        tag.putString("status", status); tag.putString("reason", reason);
        tag.putInt("collected", collected); tag.putInt("delivered", delivered);
        return tag;
    }

    public static WorkMission load(CompoundTag tag) {
        WorkMission m = new WorkMission(tag.getString("id").orElseThrow(), tag.getString("request").orElseThrow(),
                tag.getString("item").orElseThrow(), tag.getInt("amount").orElseThrow(),
                BlockPos.of(tag.getLong("min").orElseThrow()), BlockPos.of(tag.getLong("max").orElseThrow()),
                BlockPos.of(tag.getLong("destination").orElseThrow()), BlockPos.of(tag.getLong("origin").orElseThrow()));
        m.status = tag.getString("status").orElseThrow(); m.reason = tag.getString("reason").orElse("");
        m.collected = tag.getInt("collected").orElseThrow(); m.delivered = tag.getInt("delivered").orElseThrow();
        if (!Set.of("PREPARING", "MINING", "DELIVERING", "STORING_REMAINDER", "PAUSED", "SUCCEEDED", "INCOMPLETE", "CANCELLED").contains(m.status)
                || m.amount < 1 || m.amount > 4096 || m.collected < 0 || m.delivered < 0
                || m.delivered > m.amount || m.delivered > m.collected
                || (m.status.equals("SUCCEEDED") && m.delivered != m.amount)
                || !m.requestId.matches("[A-Za-z0-9_-]{1,64}") || !MissionService.SOURCES.containsKey(m.item))
            throw new IllegalArgumentException("Invalid mission state");
        if (!m.terminal() && !m.status.equals("PAUSED")) { m.status = "PAUSED"; m.reason = "SERVER_RESTART"; }
        return m;
    }

    public JsonObject json() {
        JsonObject result = new JsonObject();
        result.addProperty("id", id); result.addProperty("request_id", requestId); result.addProperty("type", "GATHER");
        result.addProperty("status", status); result.addProperty("reason", reason); result.addProperty("item", item);
        result.addProperty("amount", amount); result.addProperty("collected_total", collected);
        result.addProperty("delivered_total", delivered); result.addProperty("goal_met", status.equals("SUCCEEDED"));
        result.add("min", MissionService.positionJson(min)); result.add("max", MissionService.positionJson(max));
        result.add("destination", MissionService.positionJson(destination)); result.add("origin", MissionService.positionJson(origin));
        return result;
    }
}
