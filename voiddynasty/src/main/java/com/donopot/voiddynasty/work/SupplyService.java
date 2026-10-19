package com.donopot.voiddynasty.work;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Approvisionnement autonome : une seule demande, et le moteur enchaine tout seul
 * recolte -&gt; fonte -&gt; craft jusqu'a ce que l'objet demande soit dans le coffre.
 *
 * <p>C'est le meme chemin que l'operateur emprunte a la main ({@link CraftService#materials},
 * {@link MissionService#submit}, {@link SmeltService#smelt}, {@link CraftService#craft}) : le moteur
 * ne reimplemente aucune mecanique, il les enchaine. Chaque action est journalisee, et la boucle
 * s'arrete au premier echec avec une raison lisible plutot que d'improviser.</p>
 *
 * <p>Garde-fous : 16 actions maximum, 4 missions de recolte maximum, 15 minutes, et arret immediat
 * si une recolte ne rapporte pas ce qui etait prevu ou si une matiere premiere n'est pas
 * recoltable. {@code servitor supply cancel} interrompt tout.</p>
 */
public final class SupplyService {

    private static final int TICK_PERIOD = 20;
    private static final int MAX_ACTIONS = 16;
    private static final int MAX_GATHERS = 4;
    private static final long MAX_TICKS = 20L * 60L * 15L;
    private static final int PLAN_RADIUS = 24;
    private static final int LOG_MAX = 40;

    private SupplyService() {}

    private static Job job;

    /** Un approvisionnement en cours. Volontairement en memoire : un redemarrage l'efface. */
    public static final class Job {
        public final String id = UUID.randomUUID().toString();
        public final String item;
        public final int count;
        public final BlockPos chest;
        public String status = "PLANNING";
        public String note = "";
        public int actions;
        public int gathers;
        public int cleanup;
        public BlockPos moveTarget;
        public long moveStartedTick;
        public int retries;
        public String forceWide = "";
        public String subTarget = "";
        public int subDepth;
        public int placements;
        public int resumes;
        public final JsonArray placed = new JsonArray();
        public long startedTick;
        public String gatherMissionId = "";
        public String gatherItem = "";
        public int gatherAmount;
        public final JsonArray log = new JsonArray();

        Job(String item, int count, BlockPos chest) {
            this.item = item;
            this.count = count;
            this.chest = chest;
        }

        boolean terminal() {
            return status.equals("DONE") || status.equals("FAILED") || status.equals("CANCELLED");
        }
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (job == null || job.terminal()) return;
            if (server.getTickCount() % TICK_PERIOD != 0) return;
            ServerLevel level = server.overworld();
            try {
                tick(level);
            } catch (RuntimeException e) {
                fail("ERREUR_INTERNE", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        });
    }

    // ------------------------------------------------------------------ API

    public static JsonObject start(ServerLevel level, String item, int count, BlockPos chest) {
        if (job != null && !job.terminal()) {
            JsonObject out = error("BUSY", "Un approvisionnement est deja en cours (" + job.item + " x" + job.count
                    + ", " + job.status + ") : termine-le ou annule-le avant d'en lancer un autre");
            out.add("job", jobJson());
            return out;
        }
        if (item == null || item.isBlank()) return error("INVALID_ARGUMENT", "Item manquant");
        if (count < 1 || count > 1024) return error("INVALID_ARGUMENT", "count doit etre dans 1..1024");
        Container container = CraftService.container(level, chest);
        if (container == null) return error("INVALID_CHEST", "Coffre charge et non protege requis a " + chest.toShortString());
        String wanted = item.contains(":") ? item : "minecraft:" + item;
        if (BuiltInRegistries.ITEM.get(net.minecraft.resources.Identifier.parse(wanted)).isEmpty()) {
            return error("INVALID_ARGUMENT", "Item inconnu : " + wanted);
        }
        job = new Job(wanted, count, chest);
        job.startedTick = level.getGameTime();
        log("DEMARRE", "objectif " + count + " x " + wanted + " dans le coffre " + chest.toShortString());
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.add("job", jobJson());
        return out;
    }

    public static JsonObject statusJson() {
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        if (job == null) {
            out.addProperty("job", "aucun");
            return out;
        }
        out.add("job", jobJson());
        return out;
    }

    public static JsonObject cancel() {
        if (job == null || job.terminal()) return error("NO_JOB", "Aucun approvisionnement en cours");
        job.status = "CANCELLED";
        job.note = "annule par l'operateur";
        log("ANNULE", job.note);
        return statusJson();
    }

    // ------------------------------------------------------------------ boucle

    private static void tick(ServerLevel level) {
        LivingEntity bot = com.donopot.voiddynasty.ai.BrainSpawn.find(level);
        if (bot == null) {
            fail("NO_SERVITOR", "Aucun servitor dans cette dimension");
            return;
        }
        if (level.getGameTime() - job.startedTick > MAX_TICKS) {
            fail("TIMEOUT", "15 minutes ecoulees sans aboutir");
            return;
        }

        if (!job.gatherMissionId.isEmpty()) {
            pollGather(level);
            return;
        }

        // Le moteur de chantier n'accepte une nouvelle mission que sur un etat propre : on ferme
        // d'abord le chantier precedent (les ressources deja livrees restent dans le coffre).
        WorkState state = WorkState.get(level);
        if (!ensureIdle(level, state)) {
            if (job.terminal()) return;
            if (++job.cleanup > 15) fail("WORKER_BUSY", state.lastStop);
            return;
        }
        job.cleanup = 0;

        Container chest = CraftService.container(level, job.chest);
        if (chest == null) {
            fail("CHEST_GONE", "Le coffre de destination n'est plus accessible");
            return;
        }
        int have = countItem(chest, job.item);
        if (have >= job.count) {
            job.status = "DONE";
            job.note = have + " x " + job.item + " dans le coffre";
            log("TERMINE", job.note);
            return;
        }

        // Une station manquante (four/table) devient une sous-cible : on la fabrique et on la pose
        // avant de reprendre l'objectif principal.
        if (!job.subTarget.isEmpty()) {
            runSubTarget(level, bot, chest);
            return;
        }

        // Le plan se calcule sur l'objectif complet : il deduit lui-meme ce qui est deja dans le coffre.
        JsonObject plan = CraftService.materials(level, bot, job.chest, job.item, job.count, PLAN_RADIUS);
        if (!plan.get("ok").getAsBoolean()) {
            fail("PLAN_IMPOSSIBLE", String.valueOf(plan.get("detail")));
            return;
        }
        if (plan.has("ready") && plan.get("ready").getAsBoolean()) {
            job.status = "DONE";
            job.note = have + " x " + job.item + " deja dans le coffre";
            log("TERMINE", job.note);
            return;
        }
        if (job.actions >= MAX_ACTIONS) {
            fail("TROP_D_ACTIONS", MAX_ACTIONS + " actions sans aboutir");
            return;
        }

        // 1) Une matiere premiere manquante et recoltable passe avant tout le reste.
        JsonObject gather = pickGather(level, plan);
        if (gather != null) {
            if (job.gathers >= MAX_GATHERS) {
                fail("TROP_DE_RECOLTES", MAX_GATHERS + " missions de recolte sans aboutir");
                return;
            }
            submitGather(level, bot, gather);
            return;
        }

        // 2) Sinon, executer la premiere etape du plan (les ingredients d'abord).
        JsonArray steps = plan.getAsJsonArray("steps");
        if (steps == null || steps.isEmpty()) {
            fail("PLAN_VIDE", "Aucune etape executable : " + plan.toString());
            return;
        }
        JsonObject step = steps.get(0).getAsJsonObject();
        // La station doit etre a portee : sinon on y va d'abord, ou on la fabrique et on la pose.
        if (!ensureStation(level, bot, step)) return;
        executeStep(level, bot, step);
    }

    /** Execute une etape du plan (craft ou fonte) sur le coffre de destination. */
    private static boolean executeStep(ServerLevel level, LivingEntity bot, JsonObject step) {
        if (job.actions >= MAX_ACTIONS) {
            fail("TROP_D_ACTIONS", MAX_ACTIONS + " actions sans aboutir");
            return false;
        }
        String kind = step.get("kind").getAsString();
        String craft = step.get("craft").getAsString();
        int times = step.get("times").getAsInt();
        job.actions++;
        JsonObject result = kind.equals("smelt")
                ? SmeltService.smelt(level, bot, job.chest, craft, times)
                : CraftService.craft(level, bot, job.chest, craft, times);
        if (!result.get("ok").getAsBoolean()) {
            fail(kind.equals("smelt") ? "FONTE_REFUSEE" : "CRAFT_REFUSE",
                    craft + " x" + times + " : " + result.get("error") + " — " + result.get("detail"));
            return false;
        }
        log(kind.toUpperCase(), craft + " x" + times + " ok");
        // On re-planifie au tick suivant : le plan se recalcule toujours sur le coffre reel.
        job.status = "WORKING";
        job.note = craft + " x" + times + " fabrique";
        return true;
    }

    /** Station absente : on la fabrique (sous-cible) puis on la posera pres du coffre. */
    private static boolean provisionStation(ServerLevel level, String item) {
        if (job.subDepth > 1) {
            fail("STATION_IMPOSSIBLE", "Impossible de fabriquer " + item + " avec ce coffre");
            return false;
        }
        // Un four se fabrique sur une table 3x3 : sans table, c'est elle qu'il faut d'abord.
        if (item.equals("minecraft:furnace") && CraftService.nearbyTable(level, job.chest) == null) {
            job.subTarget = "minecraft:crafting_table";
            job.subDepth++;
            job.status = "STATION";
            job.note = "four absent : il faut d'abord une table de craft";
            log("STATION", job.note);
            return false;
        }
        job.subTarget = item;
        job.status = "STATION";
        job.note = item + " absent : fabrication puis pose pres du coffre";
        log("STATION", job.note);
        return false;
    }

    /** Sous-cible : fabriquer la station manquante, puis la poser. */
    private static void runSubTarget(ServerLevel level, LivingEntity bot, Container chest) {
        String item = job.subTarget;
        if (countItem(chest, item) > 0) {
            BlockPos pos = placeStation(level, bot, chest, item);
            if (pos == null) {
                fail("POSE_IMPOSSIBLE", "Aucun emplacement libre pres du coffre pour " + item);
                return;
            }
            consumeItem(chest, item, 1);
            job.placements++;
            JsonObject placement = new JsonObject();
            placement.addProperty("item", item);
            placement.add("pos", MissionService.positionJson(pos));
            job.placed.add(placement);
            if (job.placements > 4) {
                fail("TROP_DE_POSES", "Plus de 4 stations posees pour un seul approvisionnement");
                return;
            }
            job.subTarget = "";
            job.subDepth = 0;
            job.status = "PLACING";
            job.note = item + " pose en " + pos.toShortString();
            log("POSE", job.note);
            return;
        }
        JsonObject plan = CraftService.materials(level, bot, job.chest, item, 1, PLAN_RADIUS);
        if (!plan.get("ok").getAsBoolean()) {
            fail("STATION_IMPOSSIBLE", item + " : " + plan.get("detail"));
            return;
        }
        JsonObject gather = pickGather(level, plan);
        if (gather != null) {
            if (job.gathers >= MAX_GATHERS) {
                fail("TROP_DE_RECOLTES", "Recoltes epuisees pour fabriquer " + item);
                return;
            }
            submitGather(level, bot, gather);
            return;
        }
        JsonArray steps = plan.getAsJsonArray("steps");
        if (steps == null || steps.isEmpty()) {
            fail("STATION_IMPOSSIBLE", "Aucune etape pour fabriquer " + item + " : " + plan);
            return;
        }
        JsonObject step = steps.get(0).getAsJsonObject();
        if (!ensureStation(level, bot, step)) return;
        executeStep(level, bot, step);
    }

    /** Pose la station sur une case vide, avec un support solide, pres du coffre, jamais dans un claim. */
    private static BlockPos placeStation(ServerLevel level, LivingEntity bot, Container chest, String item) {
        var holder = BuiltInRegistries.BLOCK.get(net.minecraft.resources.Identifier.parse(item)).orElse(null);
        if (holder == null) return null;
        var block = holder.value();
        BlockPos base = job.chest;
        for (int radius = 2; radius <= 6; radius++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                        BlockPos pos = base.offset(dx, dy, dz);
                        if (!level.hasChunkAt(pos)) continue;
                        if (!level.getBlockState(pos).isAir()) continue;
                        if (pos.distSqr(bot.blockPosition()) < 2) continue;
                        var below = level.getBlockState(pos.below());
                        if (below.isAir() || !below.isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) continue;
                        if (com.donopot.voiddynasty.compat.ClaimIntegration.isClaimProtected(level, pos)) continue;
                        level.setBlockAndUpdate(pos, block.defaultBlockState());
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static void consumeItem(Container chest, String item, int count) {
        int left = count;
        for (int i = 0; i < chest.getContainerSize() && left > 0; i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.isEmpty()) continue;
            if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(item)) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        chest.setChanged();
    }

    /** Amene le compagnon a portee de la station (four ou table) avant une etape. */
    private static boolean ensureStation(ServerLevel level, LivingEntity bot, JsonObject step) {
        String kind = step.has("kind") ? step.get("kind").getAsString() : "craft";
        String station = step.has("station") ? step.get("station").getAsString() : "";
        BlockPos stationPos;
        if (kind.equals("smelt")) {
            stationPos = SmeltService.nearbyFurnace(level, job.chest);
            if (stationPos == null) return provisionStation(level, "minecraft:furnace");
        } else if (station.equals("table")) {
            stationPos = CraftService.nearbyTable(level, job.chest);
            if (stationPos == null) return provisionStation(level, "minecraft:crafting_table");
        } else {
            return true;
        }
        if (bot.blockPosition().distSqr(stationPos) <= 25) {
            job.moveTarget = null;
            return true;
        }
        BlockPos target = stationPos.above();
        if (job.moveTarget == null || !job.moveTarget.equals(target)) {
            job.moveTarget = target;
            job.moveStartedTick = level.getGameTime();
        }
        if (level.getGameTime() - job.moveStartedTick > 40L * 20L) {
            fail("DEPLACEMENT_BLOQUE", "Station " + stationPos.toShortString() + " inatteignable en 40 s");
            return false;
        }
        if (com.donopot.voiddynasty.ai.TaskRunner.current(bot) == null) {
            JsonObject args = new JsonObject();
            args.addProperty("x", target.getX());
            args.addProperty("y", target.getY());
            args.addProperty("z", target.getZ());
            if (bot instanceof com.donopot.voiddynasty.entity.Servitor1Entity worker) worker.wake();
            com.donopot.voiddynasty.ai.BrainOrders.apply(
                    new com.donopot.voiddynasty.ai.BrainTools.Order("goto", args), level, bot,
                    com.donopot.voiddynasty.ai.BrainConfig.get());
        }
        job.status = "MOVING";
        job.note = "deplacement vers " + stationPos.toShortString();
        return false;
    }

    /** Premiere matiere premiere manquante, recoltable, avec une zone ou le gisement est VERIFIE. */
    private static JsonObject pickGather(ServerLevel level, JsonObject plan) {
        JsonArray missing = plan.getAsJsonArray("to_gather");
        if (missing == null) return null;
        for (int i = 0; i < missing.size(); i++) {
            JsonObject entry = missing.get(i).getAsJsonObject();
            if (!entry.has("gatherable") || !entry.get("gatherable").getAsBoolean()) continue;
            String item = entry.has("alternative_input") && !entry.get("alternative_input").isJsonNull()
                    ? entry.get("alternative_input").getAsString()
                    : entry.get("item").getAsString();
            // La zone serree peut etre perimee (arbres deja coupes) : on la reverifie, puis on
            // retombe sur la boite de recherche complete avant de renoncer.
            if (item.equals(job.forceWide)) {
                JsonArray wide = CraftService.wideZone(level, item, entry.getAsJsonArray("search_box"), job.chest, job.chest);
                if (wide != null && countIn(level, item, wide) > 0) {
                    entry.add("suggested_zone", wide);
                    log("ZONE_LARGE", item + " : zone elargie " + wide);
                    return entry;
                }
            }
            JsonArray zone = entry.getAsJsonArray("suggested_zone");
            if (zone != null) {
                JsonArray clamped = CraftService.excludeChest(zone, job.chest);
                if (clamped != null && countIn(level, item, clamped) > 0) {
                    entry.add("suggested_zone", clamped);
                    return entry;
                }
            }
            // Zone perimee : on en recalcule une serree sur les blocs reellement presents.
            JsonArray tight = CraftService.tightZone(level, item, entry.getAsJsonArray("search_box"), job.chest);
            if (tight != null) {
                entry.add("suggested_zone", tight);
                log("ZONE_RECALCULEE", item + " : nouvelle zone " + tight);
                return entry;
            }
            log("ZONE_PERIMEE", item + " : aucun bloc trouve dans la boite de recherche");
        }
        return null;
    }

    /** Retire le coffre de destination (et son support) de la zone de recolte. */
    private static JsonArray excludeChest(JsonArray box, BlockPos chest) {
        int minX = box.get(0).getAsJsonArray().get(0).getAsInt();
        int minY = box.get(0).getAsJsonArray().get(1).getAsInt();
        int minZ = box.get(0).getAsJsonArray().get(2).getAsInt();
        int maxX = box.get(1).getAsJsonArray().get(0).getAsInt();
        int maxY = box.get(1).getAsJsonArray().get(1).getAsInt();
        int maxZ = box.get(1).getAsJsonArray().get(2).getAsInt();
        int x = chest.getX(), y = chest.getY(), z = chest.getZ();
        boolean insideX = x >= minX && x <= maxX;
        boolean insideZ = z >= minZ && z <= maxZ;
        boolean insideY = y >= minY - 1 && y <= maxY;
        if (!insideX || !insideZ || !insideY) return box;
        // On coupe sur l'axe le plus court : la moitie qui ne contient pas le coffre devient la zone.
        int left = x - minX;
        int right = maxX - x;
        int front = z - minZ;
        int back = maxZ - z;
        int smallest = Math.min(Math.min(left, right), Math.min(front, back));
        if (smallest == left) minX = x + 1;
        else if (smallest == right) maxX = x - 1;
        else if (smallest == front) minZ = z + 1;
        else maxZ = z - 1;
        if (minX > maxX || minZ > maxZ || minY > maxY) return null;
        JsonArray out = new JsonArray();
        JsonArray low = new JsonArray();
        low.add(minX); low.add(minY); low.add(minZ);
        JsonArray high = new JsonArray();
        high.add(maxX); high.add(maxY); high.add(maxZ);
        out.add(low);
        out.add(high);
        return out;
    }

    /** Nombre de blocs sources de {@code item} dans une boite, lu par l'observation du mod. */
    private static int countIn(ServerLevel level, String item, JsonArray zone) {
        JsonObject request = new JsonObject();
        request.addProperty("schema", 1);
        request.addProperty("type", "FIND");
        request.addProperty("item", item);
        request.add("min", zone.get(0).getAsJsonArray());
        request.add("max", zone.get(1).getAsJsonArray());
        request.addProperty("limit", 1);
        JsonObject found = WorkObservation.inspect(level, request);
        return found.get("ok").getAsBoolean() ? found.get("matches_total").getAsInt() : 0;
    }

    private static void submitGather(ServerLevel level, LivingEntity bot, JsonObject entry) {
        String item = entry.has("alternative_input") && !entry.get("alternative_input").isJsonNull()
                ? entry.get("alternative_input").getAsString()
                : entry.get("item").getAsString();
        int amount = Math.min(entry.get("missing").getAsInt(), 64);
        JsonArray zone = entry.getAsJsonArray("suggested_zone");
        JsonArray min = zone.get(0).getAsJsonArray();
        JsonArray max = zone.get(1).getAsJsonArray();
        BlockPos origin = bot.blockPosition();
        JsonObject request = new JsonObject();
        request.addProperty("schema", 1);
        request.addProperty("type", "GATHER");
        String requestId = "supply-" + job.id.substring(0, 8) + "-" + (job.gathers + 1);
        request.addProperty("request_id", requestId);
        request.addProperty("item", item);
        request.addProperty("amount", amount);
        request.add("min", posJson(min));
        request.add("max", posJson(max));
        request.add("destination", MissionService.positionJson(job.chest));
        request.add("origin", MissionService.positionJson(origin));
        int top = Math.max(min.get(1).getAsInt(), max.get(1).getAsInt());
        request.addProperty("access", top >= origin.getY() - 1 ? "surface" : "excavate");

        JsonObject result = MissionService.submit(level, request, bot);
        if (!result.get("ok").getAsBoolean()) {
            fail("RECOLTE_REFUSEE", item + " : " + result.get("error") + " — " + result.get("detail"));
            return;
        }
        job.gathers++;
        job.actions++;
        job.gatherMissionId = result.getAsJsonObject("mission").get("id").getAsString();
        job.gatherItem = item;
        job.gatherAmount = amount;
        job.status = "GATHERING";
        job.note = "recolte " + amount + " x " + item + " en " + requestId;
        log("RECOLTE", job.note + " (mission " + job.gatherMissionId + ", zone " + zone + ")");
    }

    private static void pollGather(ServerLevel level) {
        WorkState state = WorkState.get(level);
        WorkMission mission = null;
        for (WorkMission candidate : state.missions) {
            if (candidate.id.equals(job.gatherMissionId)) {
                mission = candidate;
                break;
            }
        }
        if (mission == null) {
            fail("MISSION_PERDUE", "La mission de recolte " + job.gatherMissionId + " a disparu de l'etat");
            return;
        }
        if (!mission.terminal()) {
            job.note = "recolte en cours : " + mission.status + " (" + mission.delivered + "/" + job.gatherAmount + " livres)";
            if ("PAUSED".equals(mission.status)) {
                // Une pause du moteur (route impossible, ouvrier occupe) se repare une fois, puis
                // on annule la mission et on retente sur une autre zone.
                if (job.resumes < 1) {
                    job.resumes++;
                    MissionService.control(level, mission.id, "resume");
                    log("REPRISE", "mission en pause (" + mission.reason + ") : reprise demandee");
                } else {
                    MissionService.control(level, mission.id, "cancel");
                    job.gatherMissionId = "";
                    job.status = "PLANNING";
                    job.resumes = 0;
                    if (job.retries < 2) {
                        job.retries++;
                        job.forceWide = job.gatherItem;
                        log("NOUVELLE_TENTATIVE", "mission annulee (" + mission.reason + ") : zone large");
                    } else {
                        fail("RECOLTE_IMPOSSIBLE", "La recolte de " + job.gatherItem + " a echoue (" + mission.reason + ")");
                    }
                }
            }
            return;
        }
        // La recolte est finie : on ferme le chantier (les ressources livrees restent dans le coffre)
        // pour pouvoir enchainer une autre mission si le plan en demande encore.
        if (!ensureIdle(level, state)) {
            job.note = "recolte terminee (" + mission.status + "), fermeture du chantier en cours";
            if (++job.cleanup > 40) fail("WORKER_BUSY", state.lastStop);
            return;
        }
        job.cleanup = 0;
        job.gatherMissionId = "";
        job.status = "PLANNING";
        log("RECOLTE_FIN", mission.status + " — livres " + mission.delivered + "/" + job.gatherAmount);
        if (mission.delivered <= 0) {
            String reason = mission.reason == null || mission.reason.isBlank() ? mission.status : mission.reason;
            // Une zone peut etre inatteignable (tronc en hauteur, terrain en pente) : on retente une
            // fois avec une zone large autour du gisement avant de declarer l'echec.
            if (job.retries < 2) {
                job.retries++;
                job.forceWide = job.gatherItem;
                log("NOUVELLE_TENTATIVE", job.gatherItem + " : zone inatteignable (" + reason + "), zone large");
                return;
            }
            fail("RECOLTE_VIDE", "La recolte de " + job.gatherItem + " n'a rien livre (" + reason + ")");
        }
    }

    // ------------------------------------------------------------------ outils

    private static int countItem(Container chest, String item) {
        int total = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.isEmpty()) continue;
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(item)) total += stack.getCount();
        }
        return total;
    }

    private static JsonArray posJson(JsonArray array) {
        return array;
    }

    private static void log(String action, String detail) {
        JsonObject entry = new JsonObject();
        entry.addProperty("action", action);
        entry.addProperty("detail", detail);
        if (job.log.size() >= LOG_MAX) job.log.remove(0);
        job.log.add(entry);
    }

    /**
     * Etat de chantier propre : rien en cours, rien dans le sac, historique ferme. La sequence est
     * celle qu'un operateur suivrait a la main (stop -&gt; cancel -&gt; deposit -&gt; commit), sans jamais
     * casser de bloc ni detruire de ressource.
     */
    private static boolean ensureIdle(ServerLevel level, WorkState state) {
        if (state.needsRecovery()) {
            fail("RECOVERY_REQUIRED", state.lastStop);
            return false;
        }
        if (state.jobName == null && state.journal.isEmpty() && state.invSlots.isEmpty()) return true;
        if (state.hasJob()) {
            WorkService.stop(level);
            job.note = "arret du chantier precedent";
            return false;
        }
        if (state.rollbackRemaining > 0) {
            WorkService.stop(level);
            job.note = "annulation du rollback en attente";
            return false;
        }
        WorkMission active = MissionService.active(state);
        if (active != null && !active.terminal()) {
            MissionService.control(level, active.id, "cancel");
            job.note = "annulation de la mission precedente";
            return false;
        }
        if (!state.invSlots.isEmpty()) {
            WorkService.deposit(level);   // vide le sac vers le coffre, sans casser de bloc
            job.note = "vidage du sac du chantier precedent";
            return false;
        }
        if (WorkService.commit(level, state)) return true;
        job.note = "fermeture du chantier precedent impossible : " + state.lastStop;
        return false;
    }

    private static void fail(String code, String detail) {
        job.status = "FAILED";
        job.note = code + " : " + detail;
        log("ECHEC", job.note);
    }

    private static JsonObject jobJson() {
        JsonObject out = new JsonObject();
        out.addProperty("id", job.id);
        out.addProperty("item", job.item);
        out.addProperty("count", job.count);
        out.addProperty("status", job.status);
        out.addProperty("note", job.note);
        out.addProperty("actions", job.actions);
        out.addProperty("gathers", job.gathers);
        out.addProperty("gather_mission", job.gatherMissionId);
        out.addProperty("sub_target", job.subTarget);
        out.addProperty("placements", job.placements);
        out.add("placed", job.placed);
        out.add("chest", MissionService.positionJson(job.chest));
        out.add("log", job.log);
        return out;
    }

    private static JsonObject error(String code, String detail) {
        JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("error", code);
        json.addProperty("detail", detail);
        return json;
    }
}
