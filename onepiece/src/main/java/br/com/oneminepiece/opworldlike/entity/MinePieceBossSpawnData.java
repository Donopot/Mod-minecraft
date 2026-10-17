package br.com.oneminepiece.opworldlike.entity;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;

public final class MinePieceBossSpawnData {
    private static final Path PATH = Path.of("config", "opworldlike", "minepiece-boss-spawns.dat");
    private static final MinePieceBossSpawnData INSTANCE = load();
    private final Map<String, BossRecord> records = new HashMap<>();

    public static MinePieceBossSpawnData get(ServerLevel level) {
        return INSTANCE;
    }

    private static MinePieceBossSpawnData load() {
        if (Files.exists(PATH)) {
            try {
                return load(NbtIo.read(PATH));
            } catch (IOException ex) {
                OmpWorld.LOGGER.warn("Could not load boss spawn data from {}.", PATH, ex);
            }
        }
        return new MinePieceBossSpawnData();
    }

    private static MinePieceBossSpawnData load(CompoundTag tag) {
        MinePieceBossSpawnData data = new MinePieceBossSpawnData();
        ListTag list = tag.getListOrEmpty("bosses");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            String bossId = entry.getStringOr("boss", "");
            if (!bossId.isBlank()) {
                UUID uuid = entry.contains("uuid") ? UUID.fromString(entry.getStringOr("uuid", "")) : new UUID(0L, 0L);
                data.records.put(bossId, new BossRecord(bossId, entry.getStringOr("island", ""), new BlockPos(entry.getIntOr("x", 0), entry.getIntOr("y", 0), entry.getIntOr("z", 0)), uuid));
            }
        }
        return data;
    }

    private void persist() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        this.records.values().stream().sorted(Comparator.comparing(BossRecord::bossId)).forEach(record -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("boss", record.bossId());
            entry.putString("island", record.islandId());
            entry.putInt("x", record.pos().getX());
            entry.putInt("y", record.pos().getY());
            entry.putInt("z", record.pos().getZ());
            if (record.uuid().getMostSignificantBits() != 0L || record.uuid().getLeastSignificantBits() != 0L) {
                entry.putString("uuid", record.uuid().toString());
            }
            list.add(entry);
        });
        tag.put("bosses", list);
        try {
            Files.createDirectories(PATH.getParent());
            NbtIo.write(tag, PATH);
        } catch (IOException ex) {
            OmpWorld.LOGGER.warn("Could not save boss spawn data to {}.", PATH, ex);
        }
    }

    public boolean hasSpawned(String bossId) {
        return this.records.containsKey(bossId);
    }

    public int count() {
        return this.records.size();
    }

    public void record(String bossId, String islandId, BlockPos pos, UUID uuid) {
        this.records.put(bossId, new BossRecord(bossId, islandId, pos, uuid));
        this.persist();
    }

    private record BossRecord(String bossId, String islandId, BlockPos pos, UUID uuid) {
    }
}
