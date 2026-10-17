package br.com.oneminepiece.opworldlike.spawn;

import br.com.oneminepiece.opworldlike.OmpWorld;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;

public final class PlayerSpawnData {
    private static final Path PATH = Path.of("config", "opworldlike", "player-spawns.dat");
    private static final PlayerSpawnData INSTANCE = load();
    private final Map<UUID, String> assignments = new HashMap<>();

    public static PlayerSpawnData get(ServerLevel level) {
        return INSTANCE;
    }

    private static PlayerSpawnData load() {
        if (Files.exists(PATH)) {
            try {
                return load(NbtIo.read(PATH));
            } catch (IOException ex) {
                OmpWorld.LOGGER.warn("Could not load player spawns from {}.", PATH, ex);
            }
        }
        return new PlayerSpawnData();
    }

    private static PlayerSpawnData load(CompoundTag tag) {
        PlayerSpawnData data = new PlayerSpawnData();
        ListTag list = tag.getListOrEmpty("assignments");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            if (entry.contains("uuid") && entry.contains("island")) {
                data.assignments.put(UUID.fromString(entry.getStringOr("uuid", "")), entry.getStringOr("island", ""));
            }
        }
        return data;
    }

    private void persist() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        this.assignments.forEach((uuid, islandId) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("uuid", uuid.toString());
            entry.putString("island", islandId);
            list.add(entry);
        });
        tag.put("assignments", list);
        try {
            Files.createDirectories(PATH.getParent());
            NbtIo.write(tag, PATH);
        } catch (IOException ex) {
            OmpWorld.LOGGER.warn("Could not save player spawns to {}.", PATH, ex);
        }
    }

    public boolean hasAssignment(UUID uuid) {
        return this.assignments.containsKey(uuid);
    }

    public IslandDefinition assignIfAbsent(UUID uuid) {
        String islandId = this.assignments.get(uuid);
        if (islandId == null || UniqueIslandCatalog.byId(islandId).isEmpty()) {
            islandId = this.chooseStarter(null);
            this.assignments.put(uuid, islandId);
            this.persist();
        }
        return UniqueIslandCatalog.byId(islandId).orElse(UniqueIslandCatalog.EAST_BLUE_STARTER);
    }

    public IslandDefinition reroll(UUID uuid) {
        String current = this.assignments.get(uuid);
        String islandId = this.chooseStarter(current);
        this.assignments.put(uuid, islandId);
        this.persist();
        return UniqueIslandCatalog.byId(islandId).orElse(UniqueIslandCatalog.EAST_BLUE_STARTER);
    }

    public Optional<IslandDefinition> assignedIsland(UUID uuid) {
        return Optional.ofNullable(this.assignments.get(uuid)).flatMap(UniqueIslandCatalog::byId);
    }

    public int countFor(String islandId) {
        int count = 0;
        for (String assigned : this.assignments.values()) {
            if (assigned.equals(islandId)) {
                count++;
            }
        }
        return count;
    }

    private String chooseStarter(String excludedIslandId) {
        return UniqueIslandCatalog.starters()
            .stream()
            .filter(island -> excludedIslandId == null || !island.id().equals(excludedIslandId))
            .min(Comparator.comparingInt(island -> this.countFor(island.id())))
            .orElse(UniqueIslandCatalog.EAST_BLUE_STARTER)
            .id();
    }
}
