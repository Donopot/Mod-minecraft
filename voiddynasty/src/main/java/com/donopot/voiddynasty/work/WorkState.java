package com.donopot.voiddynasty.work;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * État persistant d'un chantier : le travail en cours **et** le journal des blocs modifiés.
 *
 * <p>Pourquoi persistant : un chantier doit survivre à un redémarrage du serveur (exigence
 * « reprise exacte, jamais de reprise à zéro »), et le journal doit permettre d'annuler
 * (« annule les dix dernières minutes »).</p>
 *
 * <p>Le journal est borné : au-delà de {@link #JOURNAL_MAX} entrées, les plus anciennes sont
 * oubliées — on préfère une annulation partielle garantie à une sauvegarde qui gonfle sans fin.</p>
 */
public final class WorkState extends SavedData {

    /** Nombre maximum d'entrées de journal conservées. ~50 000 blocs ≈ quelques Mo. */
    public static final int JOURNAL_MAX = 50_000;

    /** Une modification réversible : position, état d'origine encodé en NBT, instant. */
    public static final class Entry {
        public final BlockPos pos;
        public final CompoundTag state;
        public final long tick;

        public Entry(BlockPos pos, CompoundTag state, long tick) {
            this.pos = pos;
            this.state = state;
            this.tick = tick;
        }
    }

    // ---- chantier en cours (null = aucun) ----
    public String jobName = null;
    public int minX, minY, minZ, maxX, maxY, maxZ;
    public int cursorX, cursorY, cursorZ;
    public int total = 0;      // blocs non vides dans la zone, calculé au démarrage
    public int done = 0;       // blocs cassés
    public int skipped = 0;    // blocs laissés (incassables, protégés, fluides, coffres)
    public boolean finished = false;
    public String lastStop = "";

    public final List<Entry> journal = new ArrayList<>();

    public static final Codec<WorkState> CODEC =
            CompoundTag.CODEC.xmap(WorkState::fromTag, WorkState::toTag);

    public static final SavedDataType<WorkState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("voiddynasty", "work_state"),
            WorkState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_FORCED_CHUNKS);

    public static WorkState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasJob() {
        return jobName != null && !finished;
    }

    /** Ajoute une entrée au journal en respectant la borne. */
    public void record(BlockPos pos, CompoundTag state, long tick) {
        journal.add(new Entry(pos, state, tick));
        while (journal.size() > JOURNAL_MAX) {
            journal.remove(0);
        }
        setDirty();
    }

    // ---------------- sérialisation ----------------

    private static CompoundTag toTag(WorkState s) {
        CompoundTag root = new CompoundTag();
        if (s.jobName != null) {
            CompoundTag job = new CompoundTag();
            job.putString("name", s.jobName);
            job.putIntArray("min", new int[]{s.minX, s.minY, s.minZ});
            job.putIntArray("max", new int[]{s.maxX, s.maxY, s.maxZ});
            job.putIntArray("cursor", new int[]{s.cursorX, s.cursorY, s.cursorZ});
            job.putInt("total", s.total);
            job.putInt("done", s.done);
            job.putInt("skipped", s.skipped);
            job.putBoolean("finished", s.finished);
            root.put("job", job);
        }
        if (!s.lastStop.isEmpty()) root.putString("last_stop", s.lastStop);

        ListTag entries = new ListTag();
        for (Entry e : s.journal) {
            CompoundTag t = new CompoundTag();
            t.putLong("pos", e.pos.asLong());
            t.put("state", e.state);
            t.putLong("tick", e.tick);
            entries.add(t);
        }
        root.put("journal", entries);
        return root;
    }

    private static WorkState fromTag(CompoundTag root) {
        WorkState s = new WorkState();
        CompoundTag job = root.getCompound("job").orElse(null);
        if (job != null) {
            s.jobName = job.getString("name").orElse("");
            int[] mn = job.getIntArray("min").orElse(new int[]{0, 0, 0});
            int[] mx = job.getIntArray("max").orElse(new int[]{0, 0, 0});
            int[] cu = job.getIntArray("cursor").orElse(mn);
            if (mn.length == 3) {
                s.minX = mn[0]; s.minY = mn[1]; s.minZ = mn[2];
            }
            if (mx.length == 3) {
                s.maxX = mx[0]; s.maxY = mx[1]; s.maxZ = mx[2];
            }
            if (cu.length == 3) {
                s.cursorX = cu[0]; s.cursorY = cu[1]; s.cursorZ = cu[2];
            }
            s.total = job.getInt("total").orElse(0);
            s.done = job.getInt("done").orElse(0);
            s.skipped = job.getInt("skipped").orElse(0);
            s.finished = job.getBoolean("finished").orElse(false);
        }
        s.lastStop = root.getString("last_stop").orElse("");
        ListTag entries = root.getList("journal").orElse(new ListTag());
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag t = entries.getCompound(i).orElse(null);
            if (t == null) continue;
            BlockPos pos = BlockPos.of(t.getLong("pos").orElse(0L));
            CompoundTag st = t.getCompound("state").orElse(new CompoundTag());
            long tick = t.getLong("tick").orElse(0L);
            s.journal.add(new Entry(pos, st, tick));
        }
        return s;
    }
}
