package com.donopot.voiddynasty.work;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * État persistant d'un chantier : le travail en cours, l'inventaire du Servitor et le journal.
 *
 * <p>Le Servitor n'est pas un exécuteur distant : il a une cible, une progression de casse, un
 * outil qui s'use et un inventaire de 36 emplacements. Tout cela vit ici pour survivre au
 * redémarrage — un chantier doit reprendre exactement où il s'est arrêté.</p>
 */
public final class WorkState extends SavedData {

    /** Nombre maximum d'entrées de journal conservées. */
    public static final int JOURNAL_MAX = 50_000;
    /** Emplacements de l'inventaire du Servitor (comme un joueur). */
    public static final int INV_SLOTS = 36;

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

    // ---- chantier ----
    public String jobName = null;
    public int minX, minY, minZ, maxX, maxY, maxZ;
    public int cursorX, cursorY, cursorZ;
    public int total = 0;
    public int done = 0;
    public int skipped = 0;
    public boolean finished = false;
    public String lastStop = "";

    // ---- cible en cours ----
    public boolean hasTarget = false;
    public int targetX, targetY, targetZ;
    public double progress = 0.0;        // secondes de travail accumulées sur la cible

    // ---- outil ----
    public String toolId = "";           // "" = mains nues
    public int toolDamage = 0;
    public int toolBlocks = 0;           // blocs cassés avec l'outil courant

    // ---- inventaire (36 emplacements, "id|count") ----
    public final List<String> invSlots = new ArrayList<>();

    // ---- depot ----
    public boolean hasDepot = false;
    public int depotX, depotY, depotZ;
    public int depotCount = 1;
    public int deposited = 0;
    public boolean goingToDepot = false;
    /** Le travail est fini : il ne reste qu'a vider le sac au depot avant de conclure. */
    public boolean finishing = false;

    // ---- butin releve ----
    public int lootBlocks = 0;
    public final List<String> lootLog = new ArrayList<>();

    /** UUID du Servitor qui exécute ce chantier ("" = aucun). */
    public String servitorUuid = "";

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

    public void noteLoot(String line) {
        lootBlocks++;
        if (lootLog.size() < 200) {
            lootLog.add(line);
        }
        setDirty();
    }

    public void record(BlockPos pos, CompoundTag state, long tick) {
        journal.add(new Entry(pos, state, tick));
        while (journal.size() > JOURNAL_MAX) {
            journal.remove(0);
        }
        setDirty();
    }

    /** Résumé du dépôt pour le status. */
    public String depositSummary() {
        if (!hasDepot) {
            return "aucun";
        }
        return (depotX + " " + depotY + " " + depotZ) + " x" + depotCount
                + (deposited > 0 ? " (" + deposited + " obj)" : "");
    }

    /** Poids total de l'inventaire (pour le rapport). */
    public int invCount() {
        int n = 0;
        for (String slot : invSlots) {
            n += countOf(slot);
        }
        return n;
    }

    static int countOf(String slot) {
        int i = slot.lastIndexOf('|');
        try {
            return i < 0 ? 0 : Integer.parseInt(slot.substring(i + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String idOf(String slot) {
        int i = slot.lastIndexOf('|');
        return i < 0 ? slot : slot.substring(0, i);
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
            job.putBoolean("has_target", s.hasTarget);
            job.putIntArray("target", new int[]{s.targetX, s.targetY, s.targetZ});
            job.putDouble("progress", s.progress);
            job.putString("tool", s.toolId);
            job.putInt("tool_damage", s.toolDamage);
            job.putInt("tool_blocks", s.toolBlocks);
            job.putInt("deposited", s.deposited);
            job.putInt("depot_count", s.depotCount);
            job.putBoolean("going_depot", s.goingToDepot);
            job.putBoolean("finishing", s.finishing);
            job.putInt("loot_blocks", s.lootBlocks);
            if (s.hasDepot) {
                job.putIntArray("depot", new int[]{s.depotX, s.depotY, s.depotZ});
            }
            if (!s.servitorUuid.isEmpty()) {
                job.putString("servitor", s.servitorUuid);
            }
            ListTag inv = new ListTag();
            for (String slot : s.invSlots) {
                inv.add(StringTag.valueOf(slot));
            }
            job.put("inv", inv);
            ListTag butin = new ListTag();
            for (String line : s.lootLog) {
                butin.add(StringTag.valueOf(line));
            }
            job.put("loot", butin);
            root.put("job", job);
        }
        if (!s.lastStop.isEmpty()) {
            root.putString("last_stop", s.lastStop);
        }
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
            s.hasTarget = job.getBoolean("has_target").orElse(false);
            int[] tg = job.getIntArray("target").orElse(new int[]{0, 0, 0});
            if (tg.length == 3) {
                s.targetX = tg[0]; s.targetY = tg[1]; s.targetZ = tg[2];
            }
            s.progress = job.getDouble("progress").orElse(0.0);
            s.toolId = job.getString("tool").orElse("");
            s.toolDamage = job.getInt("tool_damage").orElse(0);
            s.toolBlocks = job.getInt("tool_blocks").orElse(0);
            s.deposited = job.getInt("deposited").orElse(0);
            s.depotCount = Math.max(1, job.getInt("depot_count").orElse(1));
            s.goingToDepot = job.getBoolean("going_depot").orElse(false);
            s.finishing = job.getBoolean("finishing").orElse(false);
            s.lootBlocks = job.getInt("loot_blocks").orElse(0);
            s.servitorUuid = job.getString("servitor").orElse("");
            int[] dp = job.getIntArray("depot").orElse(new int[]{0, 0, 0});
            if (dp.length == 3 && !(dp[0] == 0 && dp[1] == 0 && dp[2] == 0)) {
                s.hasDepot = true;
                s.depotX = dp[0]; s.depotY = dp[1]; s.depotZ = dp[2];
            }
            ListTag inv = job.getList("inv").orElse(new ListTag());
            for (int i = 0; i < inv.size() && s.invSlots.size() < INV_SLOTS; i++) {
                inv.getString(i).ifPresent(s.invSlots::add);
            }
            ListTag butin = job.getList("loot").orElse(new ListTag());
            for (int i = 0; i < butin.size(); i++) {
                butin.getString(i).ifPresent(s.lootLog::add);
            }
        }
        s.lastStop = root.getString("last_stop").orElse("");
        ListTag entries = root.getList("journal").orElse(new ListTag());
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag t = entries.getCompound(i).orElse(null);
            if (t == null) {
                continue;
            }
            BlockPos pos = BlockPos.of(t.getLong("pos").orElse(0L));
            CompoundTag st = t.getCompound("state").orElse(new CompoundTag());
            long tick = t.getLong("tick").orElse(0L);
            s.journal.add(new Entry(pos, st, tick));
        }
        return s;
    }
}
