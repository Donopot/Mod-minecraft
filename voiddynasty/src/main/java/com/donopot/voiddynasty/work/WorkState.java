package com.donopot.voiddynasty.work;

import com.mojang.serialization.Codec;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        public List<ItemStack> drops = List.of();
        public boolean dropsKnown;
        /** Non-null for placement receipts: rollback removes this exact state and refunds drops (cost). */
        public CompoundTag placed;

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
    public String missionId = "";
    public final List<WorkMission> missions = new ArrayList<>();

    // ---- cible en cours ----
    public boolean hasTarget = false;
    public int targetX, targetY, targetZ;
    public double progress = 0.0;        // secondes de travail accumulées sur la cible

    // ---- outil ----
    public String toolId = "";           // "" = mains nues
    public int toolDamage = 0;
    public int toolBlocks = 0;           // blocs cassés avec l'outil courant

    // ---- inventaire exact (composants inclus, 36 emplacements) ----
    public final List<ItemStack> invSlots = new ArrayList<>();
    public ItemStack tool = ItemStack.EMPTY;
    public final List<BlockPos> depots = new ArrayList<>();
    public int rollbackRemaining;
    public boolean rollbackStarted;
    // Runtime watchdog: movement and work progress both count, per dimension/job.
    public transient int idleTicks;
    public transient long nextPathTick;
    public final transient List<BlockPos> accessRoute = new ArrayList<>();
    public transient BlockPos accessBlock;
    /** Previous stair landing; null for the original horizontal access route. Replanned on resume. */
    public transient BlockPos accessFrom;
    /** Bounded movement history: oscillation over already visited cells is not progress. */
    public final transient java.util.Set<BlockPos> movementCells = new java.util.HashSet<>();
    public transient BlockPos placementTarget;
    public String recoveryProblem = "";
    private CompoundTag recoveryPayload;

    public boolean needsRecovery() { return recoveryPayload != null; }

    // ---- depot ----
    public boolean awaitingServitor;
    public int originX, originY, originZ, spawnSearchCursor;
    public boolean buildingDepot, depotPlanned;
    public int planX, planY, planZ, depotBuildTicks, depotSearchCursor;
    public int supplyChests = 64, supplySupports = 64;
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
    /** Newly added workers may not be visible in the world's UUID lookup until the next tick. */
    public transient net.minecraft.world.entity.LivingEntity pendingServitor;

    public final List<Entry> journal = new ArrayList<>();

    private static final Codec<WorkState> MODERN = RecordCodecBuilder.create(i -> i.group(
            CompoundTag.CODEC.fieldOf("metadata").forGetter(WorkState::toTag),
            ItemStack.CODEC.listOf().fieldOf("inventory").forGetter(s -> s.invSlots),
            ItemStack.OPTIONAL_CODEC.fieldOf("tool_stack").forGetter(s -> s.tool),
            ItemStack.CODEC.listOf().listOf().fieldOf("receipts").forGetter(
                    s -> s.journal.stream().map(e -> e.drops).toList())
    ).apply(i, WorkState::restore));

    // A malformed v2 payload must never fall back to an empty legacy inventory.
    private static final Codec<WorkState> LEGACY = CompoundTag.CODEC.flatXmap(tag -> {
        if (tag.contains("metadata")) return DataResult.error(() -> "Invalid work inventory v2; recovery required");
        try { return DataResult.success(fromTag(tag, true)); }
        catch (RuntimeException e) { return DataResult.error(() -> "Legacy work inventory: " + e.getMessage()); }
    }, s -> DataResult.success(toTag(s)));
    private static final Codec<WorkState> FORMAT = Codec.either(MODERN, LEGACY)
            .xmap(e -> e.map(s -> s, s -> s), s -> Either.left(s));

    /** SavedDataStorage may replace a failed decode with a new state. Preserve and lock instead. */
    public static final Codec<WorkState> CODEC = new Codec<>() {
        @Override public <T> DataResult<T> encode(WorkState state, DynamicOps<T> ops, T prefix) {
            return state.needsRecovery() ? CompoundTag.CODEC.encode(state.recoveryPayload, ops, prefix)
                    : FORMAT.encode(state, ops, prefix);
        }
        @Override public <T> DataResult<Pair<WorkState, T>> decode(DynamicOps<T> ops, T input) {
            String problem;
            try {
                var result = FORMAT.decode(ops, input);
                if (result.result().isPresent()) return result;
                problem = result.error().map(e -> e.message()).orElse("Invalid work data");
            } catch (RuntimeException e) { problem = e.toString(); }
            final String reason = problem;
            return CompoundTag.CODEC.parse(ops, input).map(raw -> {
                WorkState locked = new WorkState();
                locked.recoveryPayload = raw.copy();
                locked.recoveryProblem = reason;
                locked.finished = true;
                locked.lastStop = "Donnees de chantier incompatibles: original conserve, intervention requise";
                return Pair.of(locked, ops.empty());
            });
        }
    };

    private static WorkState restore(CompoundTag tag, List<ItemStack> bag, ItemStack tool,
                                     List<List<ItemStack>> receipts) {
        if (tag.getInt("schema").orElse(0) < 2 || tag.getInt("schema").orElse(0) > 5) throw new IllegalArgumentException("Unknown work schema");
        WorkState s = fromTag(tag, false);
        if (s.jobName == null && (!bag.isEmpty() || !s.journal.isEmpty()))
            throw new IllegalArgumentException("Orphan work inventory or journal");
        if (receipts.size() != s.journal.size()) throw new IllegalArgumentException("Receipt count mismatch");
        s.invSlots.addAll(WorkInventory.copy(bag));
        s.tool = tool.copy();
        for (int n = 0; n < receipts.size(); n++) s.journal.get(n).drops = WorkInventory.copy(receipts.get(n));
        return s;
    }

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
        if (journal.size() >= JOURNAL_MAX) throw new IllegalStateException("Work journal full");
        journal.add(new Entry(pos, state, tick));

        setDirty();
    }

    /** Résumé du dépôt pour le status. */
    public String depositSummary() {
        if (!hasDepot) {
            return buildingDepot ? "construction automatique en cours" : "a construire";
        }
        return (depotX + " " + depotY + " " + depotZ) + " x" + depotCount
                + (deposited > 0 ? " (" + deposited + " obj)" : "");
    }

    /** Poids total de l'inventaire (pour le rapport). */
    public int invCount() {
        int n = 0;
        for (ItemStack slot : invSlots) n += slot.getCount();
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
        root.putInt("schema", 5);
        root.putString("mission_id", s.missionId);
        ListTag missions = new ListTag();
        for (WorkMission mission : s.missions) missions.add(mission.save());
        root.put("missions", missions);
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
            job.putIntArray("origin", new int[]{s.originX, s.originY, s.originZ});
            job.putBoolean("awaiting_servitor", s.awaitingServitor);
            job.putInt("spawn_search", s.spawnSearchCursor);
            job.putBoolean("building_depot", s.buildingDepot);
            job.putBoolean("depot_planned", s.depotPlanned);
            job.putIntArray("depot_plan", new int[]{s.planX, s.planY, s.planZ});
            job.putInt("depot_build_ticks", s.depotBuildTicks);
            job.putInt("depot_search", s.depotSearchCursor);
            job.putInt("supply_chests", s.supplyChests);
            job.putInt("supply_supports", s.supplySupports);
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
            ListTag depots = new ListTag();
            for (BlockPos pos : s.depots) {
                CompoundTag depot = new CompoundTag();
                depot.putLong("pos", pos.asLong());
                depots.add(depot);
            }
            job.put("depots", depots);
            job.putInt("rollback_remaining", s.rollbackRemaining);
            job.putBoolean("rollback_started", s.rollbackStarted);
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
            t.putBoolean("drops_known", e.dropsKnown);
            if(e.placed!=null) t.put("placed",e.placed);
            entries.add(t);
        }
        root.put("journal", entries);
        return root;
    }

    private static WorkState fromTag(CompoundTag root, boolean legacy) {
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
            int[] origin = job.getIntArray("origin").orElse(new int[]{s.minX - 1, s.maxY, s.minZ - 2});
            if (origin.length != 3) throw new IllegalArgumentException("Invalid origin");
            s.originX = origin[0]; s.originY = origin[1]; s.originZ = origin[2];
            s.awaitingServitor = job.getBoolean("awaiting_servitor").orElse(false);
            s.spawnSearchCursor = Math.max(0, job.getInt("spawn_search").orElse(0));
            s.buildingDepot = job.getBoolean("building_depot").orElse(false);
            s.depotPlanned = job.getBoolean("depot_planned").orElse(false);
            int[] plan = job.getIntArray("depot_plan").orElse(new int[]{0, 0, 0});
            if (plan.length != 3) throw new IllegalArgumentException("Invalid depot plan");
            s.planX = plan[0]; s.planY = plan[1]; s.planZ = plan[2];
            s.depotBuildTicks = Math.max(0, job.getInt("depot_build_ticks").orElse(0));
            s.depotSearchCursor = Math.max(0, job.getInt("depot_search").orElse(0));
            s.supplyChests = Math.max(0, job.getInt("supply_chests").orElse(64));
            s.supplySupports = Math.max(0, job.getInt("supply_supports").orElse(64));
            s.depotCount = Math.max(1, job.getInt("depot_count").orElse(1));
            s.goingToDepot = job.getBoolean("going_depot").orElse(false);
            s.finishing = job.getBoolean("finishing").orElse(false);
            s.lootBlocks = job.getInt("loot_blocks").orElse(0);
            s.servitorUuid = job.getString("servitor").orElse("");
            int[] dp = job.getIntArray("depot").orElse(new int[]{0, 0, 0});
            if (dp.length == 3 && job.contains("depot")) {
                s.hasDepot = true;
                s.depotX = dp[0]; s.depotY = dp[1]; s.depotZ = dp[2];
            }
            ListTag inv = job.getList("inv").orElse(new ListTag());
            if (legacy) {
                for (int i = 0; i < inv.size(); i++) {
                    String raw = inv.getString(i).orElseThrow();
                    Identifier id = Identifier.tryParse(idOf(raw));
                    int count = countOf(raw);
                    if (id == null || !BuiltInRegistries.ITEM.containsKey(id) || count <= 0)
                        throw new IllegalArgumentException("Unrecoverable legacy stack: " + raw);
                    s.invSlots.add(new ItemStack(BuiltInRegistries.ITEM.getValue(id), count));
                }
                Identifier toolId = Identifier.tryParse(s.toolId);
                if (toolId != null && BuiltInRegistries.ITEM.containsKey(toolId)) {
                    s.tool = new ItemStack(BuiltInRegistries.ITEM.getValue(toolId));
                    s.tool.setDamageValue(s.toolDamage);
                }
            }
            ListTag depots = job.getList("depots").orElse(new ListTag());
            for (int i = 0; i < depots.size(); i++) {
                CompoundTag d = depots.getCompound(i).orElseThrow();
                BlockPos pos = BlockPos.of(d.getLong("pos").orElseThrow());
                if (s.depots.contains(pos)) throw new IllegalArgumentException("Duplicate depot");
                s.depots.add(pos);
            }
            if (s.depots.isEmpty() && s.hasDepot) s.depots.add(new BlockPos(s.depotX, s.depotY, s.depotZ));
            s.rollbackRemaining = 0; // Explicit request required after restart, including rollback.
            s.rollbackStarted = job.getBoolean("rollback_started").orElse(false);
            // Restart requires an explicit resume; do not continue destructive work during loading.
            s.finished = true;
            ListTag butin = job.getList("loot").orElse(new ListTag());
            for (int i = 0; i < butin.size(); i++) {
                butin.getString(i).ifPresent(s.lootLog::add);
            }
        }
        s.lastStop = root.getString("last_stop").orElse("");
        s.missionId = root.getString("mission_id").orElse("");
        ListTag missions = root.getList("missions").orElse(new ListTag());
        if (missions.size() > MissionService.HISTORY_MAX) throw new IllegalArgumentException("Mission history exceeds limit");
        java.util.Set<String> ids = new java.util.HashSet<>(), requests = new java.util.HashSet<>();
        for (int i = 0; i < missions.size(); i++) {
            WorkMission mission = WorkMission.load(missions.getCompound(i).orElseThrow());
            if (!ids.add(mission.id) || !requests.add(mission.requestId)) throw new IllegalArgumentException("Duplicate mission identity");
            s.missions.add(mission);
        }
        if (!s.missionId.isEmpty() && (!ids.contains(s.missionId) || !s.missionId.equals(s.jobName)))
            throw new IllegalArgumentException("Detached active mission");
        ListTag entries = root.getList("journal").orElse(new ListTag());
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag t = entries.getCompound(i).orElse(null);
            if (t == null) {
                continue;
            }
            BlockPos pos = BlockPos.of(t.getLong("pos").orElse(0L));
            CompoundTag st = t.getCompound("state").orElse(new CompoundTag());
            long tick = t.getLong("tick").orElse(0L);
            Entry entry = new Entry(pos, st, tick);
            entry.dropsKnown = t.getBoolean("drops_known").orElse(false);
            if(t.contains("placed")) {entry.placed=t.getCompound("placed").orElseThrow();if(!entry.dropsKnown) throw new IllegalArgumentException("Unknown placement cost");}
            s.journal.add(entry);
        }
        return s;
    }
}
