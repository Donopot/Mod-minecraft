package com.donopot.voiddynasty.work;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import java.util.*;

/** Short lived simulation leases, scoped to the actual level and owner. */
public final class WorkChunkTickets {
    private static final TicketType TYPE = Registry.register(BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "servitor"),
            new TicketType(100L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));
    private static final Map<ServerLevel, Map<String, Set<ChunkPos>>> LEASES = new IdentityHashMap<>();

    private WorkChunkTickets() {}
    public static void init() {} // Register the type before registries freeze.

    public static void acquire(ServerLevel level, String owner, Set<ChunkPos> chunks) {
        var owners = LEASES.computeIfAbsent(level, ignored -> new HashMap<>());
        Set<ChunkPos> previous = owners.put(owner, Set.copyOf(chunks));
        if (previous != null) for (ChunkPos chunk : previous)
            if (!chunks.contains(chunk) && owners.values().stream().noneMatch(s -> s.contains(chunk)))
                level.getChunkSource().removeTicketWithRadius(TYPE, chunk, 2);
        // Radius 2 gives entity ticking. Renew even when the position is unchanged.
        for (ChunkPos chunk : chunks) level.getChunkSource().addTicketWithRadius(TYPE, chunk, 2);
    }

    public static void release(ServerLevel level, String owner) {
        var owners = LEASES.get(level);
        if (owners == null) return;
        Set<ChunkPos> removed = owners.remove(owner);
        if (removed != null) for (ChunkPos chunk : removed)
            if (owners.values().stream().noneMatch(s -> s.contains(chunk)))
                level.getChunkSource().removeTicketWithRadius(TYPE, chunk, 2);
        if (owners.isEmpty()) LEASES.remove(level);
    }

    public static void releaseEntity(UUID id) {
        for (ServerLevel level : List.copyOf(LEASES.keySet())) release(level, id.toString());
    }

    public static void clear() {
        for (var entry : List.copyOf(LEASES.entrySet()))
            for (String owner : List.copyOf(entry.getValue().keySet())) release(entry.getKey(), owner);
    }

    static int leases(ServerLevel level) {
        return LEASES.getOrDefault(level, Map.of()).size();
    }
}
