package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import java.util.*;

/** Deterministic first mission capability: bounded, filtered collection and real delivery. */
public final class MissionService {
    public static final Gson JSON = new Gson();
    public static final int HISTORY_MAX = 128;
    public static final Map<String, Set<String>> SOURCES = sources();
    private MissionService() {}

    private static Map<String, Set<String>> sources() {
        Map<String, Set<String>> map = new TreeMap<>();
        for (String wood : List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak"))
            map.put("minecraft:" + wood + "_log", Set.of("minecraft:" + wood + "_log"));
        map.put("minecraft:cobblestone", Set.of("minecraft:stone"));
        map.put("minecraft:cobbled_deepslate", Set.of("minecraft:deepslate"));
        // Terrain courant : l'item collecte peut venir de plusieurs blocs (herbe, podzol, chemin -> terre).
        map.put("minecraft:dirt", Set.of("minecraft:dirt", "minecraft:grass_block", "minecraft:coarse_dirt",
                "minecraft:rooted_dirt", "minecraft:podzol", "minecraft:mycelium", "minecraft:dirt_path"));
        map.put("minecraft:sand", Set.of("minecraft:sand"));
        map.put("minecraft:red_sand", Set.of("minecraft:red_sand"));
        map.put("minecraft:gravel", Set.of("minecraft:gravel"));
        for (String ore : List.of("diamond", "coal", "emerald", "redstone", "lapis", "iron", "copper", "gold")) {
            String item = Set.of("iron", "copper", "gold").contains(ore) ? "raw_" + ore : ore;
            if (ore.equals("lapis")) item = "lapis_lazuli";
            map.put("minecraft:" + item, Set.of("minecraft:" + ore + "_ore", "minecraft:deepslate_" + ore + "_ore"));
        }
        return Collections.unmodifiableMap(map);
    }

    public static WorkMission active(WorkState state) {
        return state.missions.stream().filter(m -> m.id.equals(state.missionId)).findFirst().orElse(null);
    }

    /** Item special des missions EXCAVATE : la zone entiere est la cible. */
    public static final String EXCAVATE_ITEM = "*";

    public static boolean matches(WorkMission mission, BlockState block) {
        if (EXCAVATE_ITEM.equals(mission.item)) return !block.isAir();
        return SOURCES.getOrDefault(mission.item, Set.of()).contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString());
    }

    public static JsonArray positionJson(BlockPos pos) {
        JsonArray a = new JsonArray(); a.add(pos.getX()); a.add(pos.getY()); a.add(pos.getZ()); return a;
    }

    public static JsonObject error(String code, String detail) {
        JsonObject out = new JsonObject(); out.addProperty("ok", false); out.addProperty("error", code); out.addProperty("detail", detail); return out;
    }

    public static JsonObject capabilities() {
        JsonObject out = new JsonObject(); out.addProperty("ok", true); out.addProperty("api_version", 1);
        JsonArray types = new JsonArray(); types.add("GATHER"); types.add("BUILD"); types.add("EXCAVATE"); out.add("mission_types", types);
        out.addProperty("excavate", "EXCAVATE vide toute la zone (min, max) et depose les objets au coffre : champs schema=1, type=EXCAVATE, request_id, min, max, destination, origin, access optionnel. Pas d'item, pas d'amount. Le but est la zone videe, pas un nombre d'objets.");
        JsonArray resources = new JsonArray(); SOURCES.keySet().forEach(resources::add); out.add("gather_items", resources);
        out.addProperty("max_volume", 32768); out.addProperty("max_amount", 4096);
        JsonArray perceptions=new JsonArray();perceptions.add("OBSERVE");perceptions.add("FIND");out.add("observation_types",perceptions);out.addProperty("observation_max_results",WorkObservation.MAX_RESULTS);
        out.addProperty("prospection", "Optional access=excavate ouvre un escalier de descente par transitions atomiques (tunnel 1x2, largeur configurable, un bloc de descente par bloc d'avance), support valide sous l'empreinte de collision, retour praticable, uniquement dans la zone autorisee");
        out.addProperty("access_max_blocks",WorkAccess.MAX_BLOCKS);
        out.addProperty("access_max_rise",StairMining.MAX_STEPS);
        out.addProperty("access_search_radius",24);
        out.addProperty("access_tunnel","1x"+StairMining.HEIGHT+" (largeur "+StairMining.WIDTH+")");
        JsonArray plans=new JsonArray();BuildPlan.IDS.stream().sorted().forEach(plans::add);out.add("blueprints",plans);
        JsonArray materials=new JsonArray();BuildPlan.MATERIALS.stream().sorted().forEach(materials::add);out.add("build_materials",materials);
        return out;
    }

    static int integer(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
                || !value.getAsString().matches("-?[0-9]+")) throw new IllegalArgumentException("Integer required");
        return value.getAsBigDecimal().intValueExact();
    }

    static BlockPos position(JsonElement value) {
        if (value == null || !value.isJsonArray() || value.getAsJsonArray().size() != 3) throw new IllegalArgumentException("Position must have 3 integers");
        JsonArray a = value.getAsJsonArray(); int x = integer(a.get(0)), y = integer(a.get(1)), z = integer(a.get(2));
        if (Math.abs((long)x) > 29999984 || Math.abs((long)z) > 29999984 || y < -2048 || y > 2047)
            throw new IllegalArgumentException("Position out of bounds");
        return new BlockPos(x, y, z);
    }

    public static WorkMission parse(JsonObject obj) {
        if(obj.has("type") && obj.get("type").getAsString().equals("BUILD")) return parseBuild(obj);
        Set<String> fields=new HashSet<>(obj.keySet());fields.remove("access");
        boolean excavate = obj.has("type") && obj.get("type").getAsString().equals("EXCAVATE");
        Set<String> required = excavate
                ? Set.of("schema", "type", "request_id", "min", "max", "destination", "origin")
                : Set.of("schema", "type", "request_id", "item", "amount", "min", "max", "destination", "origin");
        if (!fields.equals(required))
            throw new IllegalArgumentException(excavate
                    ? "Invalid or missing EXCAVATE fields (no item, no amount: the zone is the goal)"
                    : "Invalid or missing mission fields");
        if (integer(obj.get("schema")) != 1) throw new IllegalArgumentException("Unsupported mission schema/type");
        String request = obj.get("request_id").getAsString();
        String item = excavate ? EXCAVATE_ITEM : obj.get("item").getAsString();
        if (!request.matches("[A-Za-z0-9_-]{1,64}") || !(excavate || SOURCES.containsKey(item)))
            throw new IllegalArgumentException("Invalid request id or unsupported resource");
        BlockPos a = position(obj.get("min")), b = position(obj.get("max"));
        BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        if ((long)max.getX()-min.getX()+1 > 32768 || (long)max.getY()-min.getY()+1 > 32768
                || (long)max.getZ()-min.getZ()+1 > 32768) throw new IllegalArgumentException("Zone exceeds 32768 blocks");
        long volume = (long)(max.getX()-min.getX()+1) * (max.getY()-min.getY()+1) * (max.getZ()-min.getZ()+1);
        if (volume > 32768) throw new IllegalArgumentException("Zone exceeds 32768 blocks");
        // EXCAVATE : le but est la zone videe, pas un nombre d'objets. amount = volume sert de plafond interne.
        int count = excavate ? (int) volume : integer(obj.get("amount"));
        if (!excavate && (count < 1 || count > 4096)) throw new IllegalArgumentException("Amount must be 1..4096");
        BlockPos destination = position(obj.get("destination")), origin = position(obj.get("origin"));
        WorkMission m = new WorkMission(UUID.randomUUID().toString(), request, item, count, min, max, destination, origin);
        m.access=obj.has("access")?obj.get("access").getAsString():"surface";
        if(!Set.of("surface","excavate").contains(m.access)) throw new IllegalArgumentException("Use surface or excavate access");
        if (contains(m, destination) || contains(m, destination.below())) throw new IllegalArgumentException("Destination and its support must be outside the mining zone");
        return m;
    }

    private static WorkMission parseBuild(JsonObject obj) {
        if(!obj.keySet().equals(Set.of("schema","type","request_id","blueprint","material","anchor","source","origin")) || integer(obj.get("schema"))!=1) throw new IllegalArgumentException("Invalid build fields/schema");
        String request=obj.get("request_id").getAsString();if(!request.matches("[A-Za-z0-9_-]{1,64}")) throw new IllegalArgumentException("Invalid request id");
        BuildPlan plan=new BuildPlan(obj.get("blueprint").getAsString(),obj.get("material").getAsString(),position(obj.get("anchor")));
        BlockPos source=position(obj.get("source")),origin=position(obj.get("origin"));
        WorkMission m=new WorkMission(UUID.randomUUID().toString(),request,plan.material(),plan.blocks().size(),plan.anchor(),plan.max(),source,origin);m.build=plan;
        if(contains(m,source) || contains(m,source.below())) throw new IllegalArgumentException("Material source must be outside build site");
        return m;
    }

    public static JsonObject estimate(ServerLevel level,JsonObject request) {
        WorkMission m;try {m=parse(request);} catch(RuntimeException e) {return error("INVALID_SPEC",e.getMessage());}
        if(m.build==null) return error("INVALID_SPEC","Estimate requires BUILD");
        JsonObject out=new JsonObject();out.addProperty("ok",true);out.addProperty("blueprint",m.build.blueprint());out.addProperty("material",m.item);out.addProperty("required",m.amount);
        out.add("min",positionJson(m.min));out.add("max",positionJson(m.max));out.addProperty("site_problem",BuildService.validate(level,m));return out;
    }

    private static boolean contains(WorkMission m, BlockPos p) {
        return p.getX() >= m.min.getX() && p.getX() <= m.max.getX() && p.getY() >= m.min.getY() && p.getY() <= m.max.getY()
                && p.getZ() >= m.min.getZ() && p.getZ() <= m.max.getZ();
    }

    static Container destination(ServerLevel level, WorkMission m) {
        if (!level.hasChunkAt(m.destination) || ClaimIntegration.isClaimProtected(level, m.destination)) return null;
        var be = level.getBlockEntity(m.destination);
        if (be instanceof ChestBlockEntity && level.getBlockState(m.destination).getValue(ChestBlock.TYPE) != ChestType.SINGLE) return null;
        return (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) && be instanceof Container c ? c : null;
    }

    public static JsonObject submit(ServerLevel level, JsonObject request) {
        return submit(level, request, null);
    }

    public static JsonObject submit(ServerLevel level, JsonObject request, LivingEntity companion) {
        WorkMission proposed;
        try { proposed = parse(request); } catch (RuntimeException e) { return error("INVALID_SPEC", e.getMessage()); }
        WorkState s = WorkState.get(level);
        if (s.needsRecovery()) return error("RECOVERY_REQUIRED", s.lastStop);
        for (WorkMission m : s.missions) if (m.requestId.equals(proposed.requestId)) {
            if (!m.sameRequest(proposed)) return error("REQUEST_CONFLICT", "Same request_id with different parameters");
            JsonObject result = result(level, s, m); result.addProperty("replayed", true); return result;
        }
        if (s.missions.size() >= HISTORY_MAX) return error("HISTORY_FULL", "Mission history retained; no automatic eviction");
        if (s.jobName != null || !s.invSlots.isEmpty() || !s.journal.isEmpty()) return error("WORKER_BUSY", "Close or reconcile the previous job first");
        if (level.isOutsideBuildHeight(proposed.min) || level.isOutsideBuildHeight(proposed.max)
                || !level.getWorldBorder().isWithinBounds(proposed.min) || !level.getWorldBorder().isWithinBounds(proposed.max))
            return error("INVALID_ZONE", "Zone outside dimension bounds");
        if (destination(level, proposed) == null) return error("INVALID_DESTINATION", "Loaded unprotected chest or barrel required");
        if (level.isOutsideBuildHeight(proposed.origin) || !level.getWorldBorder().isWithinBounds(proposed.origin))
            return error("INVALID_ORIGIN", "Origin outside dimension bounds");
        if(proposed.build!=null) {String invalid=BuildService.validate(level,proposed);if(!invalid.isEmpty()) return error(invalid,"Build site refused before reserving resources");}
        WorkState started = WorkService.start(level, proposed.id, proposed.min, proposed.max, "minecraft:iron_pickaxe", proposed.origin, companion);
        if (started == null) return error("START_REFUSED", s.lastStop);
        // Collecte avec destination explicite : le coffre fourni TIENT LIEU de depot. Sans cela le
        // moteur cherche a en construire un autour du compagnon, et sur un terrain encombre il
        // peut boucler indefiniment en phase PREPARING au lieu de miner.
        if (proposed.build == null) {
            started.hasDepot = true;
            started.buildingDepot = false;
            started.depotPlanned = false;
            started.depotX = proposed.destination.getX();
            started.depotY = proposed.destination.getY();
            started.depotZ = proposed.destination.getZ();
        }
        s.missions.add(proposed); s.missionId = proposed.id;
        // Include the actual delivery chest in receipt recovery; never clear its other contents.
        s.depots.add(proposed.destination); s.setDirty();
        JsonObject out = result(level, s, proposed); out.addProperty("replayed", false); return out;
    }

    private static JsonObject result(ServerLevel level, WorkState s, WorkMission mission) {
        JsonObject out = new JsonObject(); out.addProperty("ok", true); out.add("mission", mission.json());
        out.addProperty("dimension", level.dimension().identifier().toString());
        boolean attached = mission.id.equals(s.missionId);
        out.addProperty("attached", attached);
        out.addProperty("worker_active", attached && s.hasJob());
        if (attached) {
            out.addProperty("carried_items", s.invCount()); out.addProperty("journal_entries", s.journal.size());
            out.addProperty("rollback_remaining", s.rollbackRemaining);
            out.addProperty("rollback_state", !s.rollbackStarted ? "NONE" : s.rollbackRemaining > 0 ? "RUNNING"
                    : s.journal.isEmpty() ? "COMPLETE" : s.lastStop.startsWith("rollback arrete:") ? "FAILED" : "PARTIAL");
            out.addProperty("detail", s.lastStop);
        }
        return out;
    }

    public static JsonObject status(ServerLevel level, String id) {
        WorkState s = WorkState.get(level);
        if (s.needsRecovery()) return error("RECOVERY_REQUIRED", s.lastStop);
        String query = id.equals("current") ? s.missionId : id;
        return s.missions.stream().filter(m -> m.id.equals(query)).findFirst().map(m -> result(level, s, m))
                .orElseGet(() -> error("NOT_FOUND", "Mission not found in this dimension"));
    }

    public static JsonObject control(ServerLevel level, String id, String action) {
        WorkState s = WorkState.get(level); WorkMission m = active(s);
        if (s.needsRecovery()) return error("RECOVERY_REQUIRED", s.lastStop);
        if (m == null || !m.id.equals(id)) return error("NOT_ATTACHED", "Control requires the attached mission id");
        switch (action) {
            case "pause" -> { if (!m.terminal()) { WorkService.stop(level); m.status = "PAUSED"; m.reason = "OPERATOR_PAUSE"; } }
            case "resume" -> {
                if (m.terminal() || s.rollbackStarted) return error("RESUME_REFUSED", "Terminal or rolled-back mission");
                if (WorkService.resume(level) == null) return error("RESUME_REFUSED", s.lastStop);
                m.status = "PREPARING"; m.reason = "";
            }
            case "cancel" -> { if (!m.terminal()) { WorkService.stop(level); m.status = "CANCELLED"; m.reason = "OPERATOR_CANCEL"; } }
            case "deposit" -> {
                if (!m.terminal()) return error("DEPOSIT_REFUSED", "Cancel or finish mission before deposit-only recovery");
                if (!s.invSlots.isEmpty() && WorkService.deposit(level) == null) return error("DEPOSIT_REFUSED", s.lastStop);
            }
            case "rollback" -> WorkService.requestRollback(level, s, Integer.MAX_VALUE);
            case "close" -> {
                if (!m.terminal()) return error("CLOSE_REFUSED", "Finish or cancel mission first");
                if (!WorkService.commit(level, s)) return error("CLOSE_REFUSED", "Carry must be empty and rollback inactive; use /work deposit if needed");
            }
            default -> { return error("INVALID_ACTION", "Use pause, resume, cancel, deposit, rollback or close"); }
        }
        s.setDirty(); return result(level, s, m);
    }

    static void paused(WorkState s, String code) {
        WorkMission m = active(s);
        if (m != null && !m.terminal()) { m.status = "PAUSED"; m.reason = code.matches("[A-Z_]+") ? code : "WORKER_PAUSED"; }
    }

    static void invalidate(WorkState s) {
        WorkMission m = active(s);
        if (m != null) { m.status = "CANCELLED"; m.reason = "ROLLBACK_REQUESTED"; }
    }

    static void concluded(WorkState s) {
        WorkMission m = active(s);
        if (m == null || m.status.equals("CANCELLED")) return;
        if(m.build!=null) {m.status=m.placed==m.amount?"SUCCEEDED":"INCOMPLETE";m.reason=m.status.equals("SUCCEEDED")?"":"BUILD_UNFINISHED";return;}
        if (EXCAVATE_ITEM.equals(m.item)) {
            // Le but d'un EXCAVATE est la zone videe : le curseur est la seule verite.
            boolean cleared = s.cursorY < s.minY;
            m.status = cleared ? "SUCCEEDED" : "INCOMPLETE";
            m.reason = cleared ? "ZONE_CLEARED" : "STOPPED_BEFORE_GOAL";
        } else {
            m.status = m.delivered >= m.amount ? "SUCCEEDED" : "INCOMPLETE";
            m.reason = m.status.equals("SUCCEEDED") ? "" : s.cursorY < s.minY ? "SOURCE_EXHAUSTED" : "STOPPED_BEFORE_GOAL";
        }
    }

    static void mined(WorkState s, List<ItemStack> drops) {
        WorkMission m = active(s);
        if (m != null && !m.terminal()) for (ItemStack stack : drops) if (isItem(m, stack)) m.collected += stack.getCount();
    }

    private static boolean isItem(WorkMission m, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (EXCAVATE_ITEM.equals(m.item)) return true; // tout ce qui tombe appartient au chantier
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(m.item);
    }

    /** True means this tick was consumed by delivery/completion rather than another mining action. */
    static boolean tick(ServerLevel level, WorkState s, LivingEntity bot) {
        WorkMission m = active(s); if (m == null || m.status.equals("CANCELLED")) return false;
        if(m.build!=null) {if(m.terminal()) return false;BuildService.tick(level,s,bot,m);return true;}
        if (m.delivered >= m.amount) {
            m.status = "STORING_REMAINDER";
            if (s.invSlots.isEmpty()) WorkService.conclude(level, s); else s.finishing = true;
            s.setDirty(); return s.invSlots.isEmpty();
        }
        // Sac presque plein : on va vider au coffre puis on reprend, au lieu de se bloquer.
        if (!s.finishing && !s.goingToDepot && s.invSlots.size() >= 32) s.goingToDepot = true;
        if (!(s.finishing || s.goingToDepot || m.collected >= m.amount || s.cursorY < s.minY)) {
            m.status = s.buildingDepot || !s.hasDepot ? "PREPARING" : "MINING";
            return false;
        }
        m.status = "DELIVERING"; s.setDirty();
        Container target = destination(level, m);
        if (target == null) { WorkService.pause(s, "DESTINATION_UNAVAILABLE"); return true; }
        if (WorkService.distanceSq(bot, m.destination) > WorkService.reach()*WorkService.reach()
                || !WorkService.visible(level, bot, m.destination)) { WorkService.stepToward(level, s, bot, m.destination); return true; }
        WorkInventory.Transfer plan = WorkInventory.transfer(s.invSlots, target, stack -> isItem(m, stack), m.amount - m.delivered);
        if (plan.count() == 0 && s.invSlots.stream().anyMatch(stack -> isItem(m, stack))) {
            WorkService.pause(s, "DESTINATION_FULL"); return true;
        }
        for (int i = 0; i < target.getContainerSize(); i++) target.setItem(i, plan.container().get(i));
        target.setChanged(); s.invSlots.clear(); s.invSlots.addAll(plan.bag()); m.delivered += plan.count();
        s.goingToDepot = false; s.setDirty();
        if (m.delivered >= m.amount) {
            if (s.invSlots.isEmpty()) WorkService.conclude(level, s); else s.finishing = true;
        } else if (s.cursorY < s.minY || s.finishing) {
            if (s.invSlots.isEmpty()) WorkService.conclude(level, s);
            else WorkService.pause(s, "INVENTORY_BLOCKED");
        }
        return true;
    }
}
