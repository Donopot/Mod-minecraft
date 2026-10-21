package com.donopot.voiddynasty.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.*;

/** Persist intent, never resume movement implicitly when a world is loaded. */
public final class BrainRoutineState extends SavedData {
    public record Entry(String mission, String bot, BlockPos origin, String label,
                        String phase, String reason, long started, int delivered) {
        Entry with(String phase, String reason, long started, int delivered) {
            return new Entry(mission, bot, origin, label, phase, reason, started, delivered);
        }
    }
    private static final Codec<Entry> ENTRY = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("mission").forGetter(Entry::mission),
            Codec.STRING.fieldOf("bot").forGetter(Entry::bot),
            BlockPos.CODEC.fieldOf("origin").forGetter(Entry::origin),
            Codec.STRING.fieldOf("label").forGetter(Entry::label),
            Codec.STRING.fieldOf("phase").forGetter(Entry::phase),
            Codec.STRING.fieldOf("reason").forGetter(Entry::reason),
            Codec.LONG.fieldOf("started").forGetter(Entry::started),
            Codec.INT.fieldOf("delivered").forGetter(Entry::delivered)
    ).apply(i, Entry::new));
    private static final Codec<BrainRoutineState> CODEC = ENTRY.listOf().fieldOf("routines").codec()
            .xmap(BrainRoutineState::restore, s -> List.copyOf(s.entries.values()));
    private static final SavedDataType<BrainRoutineState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("voiddynasty", "brain_routines"),
            BrainRoutineState::new, CODEC, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    final Map<String, Entry> entries = new LinkedHashMap<>();
    final Map<String, BrainTask> returns = new HashMap<>();
    private static BrainRoutineState restore(List<Entry> entries) {
        BrainRoutineState state = new BrainRoutineState();
        for (Entry entry : entries) {
            UUID.fromString(entry.mission()); UUID.fromString(entry.bot());
            if (state.entries.putIfAbsent(entry.mission(),
                    entry.with("PAUSED", "SERVER_RESTART", entry.started(), entry.delivered())) != null)
                throw new IllegalArgumentException("Duplicate routine");
        }
        return state;
    }
    static BrainRoutineState get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    void put(Entry entry) { entries.put(entry.mission(), entry); setDirty(); }
    void remove(String id) { entries.remove(id); returns.remove(id); setDirty(); }
}
