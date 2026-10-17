package br.com.oneminepiece.opworldlike.loot;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;

final class ImportedIslandChestLootData {
    private static final Path PATH = Path.of("config", "opworldlike", "imported-chest-loot.dat");
    private static final ImportedIslandChestLootData INSTANCE = load();
    private final Set<Long> consumedPositions = new HashSet<>();

    static ImportedIslandChestLootData get(ServerLevel level) {
        return INSTANCE;
    }

    private static ImportedIslandChestLootData load() {
        if (Files.exists(PATH)) {
            try {
                return load(NbtIo.read(PATH));
            } catch (IOException ex) {
                OmpWorld.LOGGER.warn("Could not load imported chest loot from {}.", PATH, ex);
            }
        }
        return new ImportedIslandChestLootData();
    }

    private static ImportedIslandChestLootData load(CompoundTag tag) {
        ImportedIslandChestLootData data = new ImportedIslandChestLootData();
        if (tag.getIntOr("version", 0) == 1) {
            Arrays.stream(tag.getLongArray("consumedPositions").orElse(new long[0])).forEach(data.consumedPositions::add);
        }
        return data;
    }

    synchronized boolean consume(BlockPos pos) {
        if (!this.consumedPositions.add(pos.asLong())) {
            return false;
        }
        this.persist();
        return true;
    }

    private synchronized void persist() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", 1);
        tag.putLongArray("consumedPositions", this.consumedPositions.stream().mapToLong(Long::longValue).sorted().toArray());
        try {
            Files.createDirectories(PATH.getParent());
            NbtIo.write(tag, PATH);
        } catch (IOException ex) {
            OmpWorld.LOGGER.warn("Could not save imported chest loot to {}.", PATH, ex);
        }
    }
}
