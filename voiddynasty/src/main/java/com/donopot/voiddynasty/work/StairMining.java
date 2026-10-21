package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Escalier d'acces : un modele geometrique separe du reste du chantier.
 *
 * <p>Le planificateur produit une suite de transitions atomiques (DESCEND_STEP / WALK) ; chaque
 * transition porte ses propres volumes :
 * <ul>
 *   <li>{@code feet} — coordonnee de bloc des pieds apres la transition (walkPath) ;</li>
 *   <li>{@code clear} — cellules de degagement de CETTE transition (1 de large, {@link #HEIGHT} de
 *       haut, ou {@link #WIDTH} de large) ;</li>
 *   <li>{@code support} — blocs porteurs exiges sous l'empreinte de collision, jamais mines.</li>
 * </ul>
 *
 * <p>Ancienne geometrie (abandonnee) : palier 3x3x3 exigeant une surface porteuse 3x3 complete a
 * chaque extremite, ce qui rendait toute pente ou berge infranchissable (ACCESS_NO_SAFE_ROUTE via
 * un refus "floorTo"). Ici le support est valide sous l'empreinte reelle du compagnon, et creuser
 * la marche suivante ne retire jamais le support de la precedente (elle descend d'un cran vers
 * l'avant, le bloc porteur reste en place).
 *
 * <p>Aucun bloc n'est considere comme modifie sans relecture : chaque cellule est relue avant et
 * apres la casse, et un refus de mutation invalide le plan au lieu d'avancer.
 */
final class StairMining {
    /** Hauteur libre du tunnel (1x2 : les pieds et la tete). */
    static final int HEIGHT = 2;
    /** Largeur du tunnel : 2 pour le servitor (hitbox 1.09 > 1 bloc). 3 = elargissement. */
    static final int WIDTH = 2;
    static final int MAX_STEPS = 96;
    /**
     * Longueur de palier mesuree au banc d'essai (alpha.86) : le compagnon ne quitte le support
     * superieur qu'apres ~2 blocs de marche au-dela du bord (AABB 1.09). Une marche de 1 bloc
     * d'avance le laisse pose sur la dalle precedente ; 3 blocs garantissent la chute puis
     * l'appui sur le palier inferieur.
     */
    static final int TREAD = 3;
    private static final int MAX_FAILURES = 4;

    private StairMining() {}

    enum Kind { DESCEND_STEP, WALK }

    /** Une transition : d'un palier au suivant, avec ses volumes propres. */
    record Step(Kind kind, BlockPos from, BlockPos feet, List<BlockPos> clear, List<BlockPos> support) {}

    record Plan(List<Step> steps, BlockPos target) {}

    private static final Map<String, Plan> PLANS = new HashMap<>();
    /** Tentatives d'impulsion par marche (descente dans le trou d'un bloc). */
    private static final Map<String, Integer> DESCEND_TRIES = new HashMap<>();
    private static final Map<String, Long> DESCEND_LAST = new HashMap<>();
    private static final int MAX_DESCEND_TRIES = 8;
    private static final Map<String, Integer> INDEX = new HashMap<>();
    private static final Map<String, Integer> FAILURES = new HashMap<>();

    /** Vrai si un escalier pilote cette mission : le detecteur de stagnation du chantier doit
     *  alors s'effacer, l'escalier ayant ses propres bornes (8 essais, 96 marches). */
    /**
     * Vrai si un escalier pilote cette mission. Le detecteur de stagnation du chantier doit alors
     * s'effacer : l'escalier a ses propres bornes (45 s par marche, 5 min au total, 5 recuperations)
     * et publie sa phase. Sans cela, le worker met la mission en pause au milieu d'un creusement.
     */
    /** Le planificateur possede la mission des qu'il a un etat d'execution : il la garde. */
    static boolean owns(WorkMission mission) {
        return mission != null && EXECS.containsKey(mission.id);
    }

    static boolean driving(WorkMission mission) {
        return mission != null && EXECS.containsKey(mission.id);
    }

    static void forget(String missionId) {
        EXECS.remove(missionId);
        POSES.remove(missionId); POSE_CELLS.remove(missionId);
        POSE_DIST.remove(missionId); POSE_STILL.remove(missionId);
        DESCEND_TRIES.remove(missionId);
        DESCEND_LAST.remove(missionId);
        PLANS.remove(missionId);
        INDEX.remove(missionId);
    }

    // ------------------------------------------------------------------ planification

    /** Directions candidates, la plus proche du cap start->target d'abord. */
    private static List<Direction> directions(BlockPos start, BlockPos target) {
        List<Direction> horizontal = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        List<Direction> ordered = new ArrayList<>(horizontal);
        ordered.sort((a, b) -> Double.compare(
                distanceTo(b, start, target), distanceTo(a, start, target)));
        return ordered;
    }

    private static double distanceTo(Direction dir, BlockPos start, BlockPos target) {
        BlockPos probe = start.relative(dir, 8);
        return probe.distSqr(target);
    }

    static Plan plan(ServerLevel level, WorkState state, LivingEntity bot, BlockPos target) {
        WorkMission mission = MissionService.active(state);
        BlockPos start = bot.blockPosition();
        if (mission == null) return null;
        // Le couloir part de la surface : sa borne haute est la hauteur de depart, pas le plafond
        // de la zone miniere (sinon aucune entree n'est jamais praticable au-dessus de la cible).
        accessTop = start.getY() + 2;
        state.accessCeiling = accessTop;
        // 1. Trouver une entree praticable au-dessus de la cible (colonne porteuse, 2 blocs libres).
        BlockPos surface = entrance(level, mission, target, start);
        if (surface == null) {
            lastReason = "aucune entree praticable au-dessus de " + target.toShortString();
            return null;
        }
        List<Step> steps = new ArrayList<>();
        if (start.distSqr(surface) > 4) {
            steps.add(new Step(Kind.WALK, start, surface, List.of(), List.of()));
        }
        for (Direction dir : directions(surface, target)) {
            Plan plan = descend(level, mission, surface, target, dir);
            if (plan != null && !plan.steps().isEmpty()) {
                steps.addAll(plan.steps());
                return new Plan(List.copyOf(steps), target);
            }
        }
        return null;
    }

    private static String lastReason = "";
    private static int accessTop = Integer.MIN_VALUE;

    /**
     * Colonne d'entree la plus proche de la cible : sommet porteur a hauteur du compagnon (+/-3),
     * avec deux blocs libres au-dessus, et une colonne contigue pour le tunnel de largeur 2.
     */
    private static BlockPos entrance(ServerLevel level, WorkMission mission, BlockPos target, BlockPos start) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                // Meme niveau que le depart : une entree plus basse transformerait la marche
                // a niveau (WALK) en descente deguisee, refusee a juste titre par ALIGN.
                for (int dy = 0; dy >= 0; dy--) {
                    BlockPos feet = new BlockPos(target.getX() + dx, start.getY() + dy, target.getZ() + dz);
                    if (!level.hasChunkAt(feet) || !support(level, feet.below())) continue;
                    if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) continue;
                    // Marge d'un bloc : le tunnel de largeur 2 doit tenir entierement dans la zone.
                    if (!insideMargin(mission, feet, 1)) continue;
                    double distance = feet.distSqr(target) + Math.abs(dy) * 4.0;
                    if (distance < bestDistance) { bestDistance = distance; best = feet; }
                    break;
                }
            }
        }
        return best;
    }

    /** Toutes les cellules autour du point (marge donnee, deux niveaux) sont-elles dans la zone ? */
    private static boolean insideMargin(WorkMission mission, BlockPos feet, int margin) {
        // Emprise horizontale seulement : la hauteur du couloir est reglee par accessTop.
        for (int dx = -margin; dx <= margin; dx++) {
            for (int dz = -margin; dz <= margin; dz++) {
                if (!WorkAccess.columnOf(mission, feet.offset(dx, 0, dz))) return false;
            }
        }
        return true;
    }

    private static Plan descend(ServerLevel level, WorkMission mission, BlockPos start, BlockPos target, Direction first) {
        List<Step> steps = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        BlockPos current = start;
        Direction dir = first;
        int guard = 0;
        while (current.getY() > target.getY() + 1 && guard++ < MAX_STEPS) {
            if (!visited.add(current)) return null;
            Step step = null;
            for (int attempt = 0; attempt < 4 && step == null; attempt++) {
                step = step(level, mission, current, dir);
                if (step == null) dir = dir.getClockWise();
            }
            if (step == null) {
                lastReason = "transition impossible depuis " + current.toShortString()
                        + " vers " + target.toShortString() + " (" + lastReason + ")";
                return null;
            }
            steps.add(step);
            current = step.feet();
        }
        if (guard >= MAX_STEPS) { lastReason = "profondeur maximale atteinte"; return null; }
        // Derniere transition : rejoindre la cible a niveau constant (WALK), si elle est proche.
        if (!current.equals(target) && current.distSqr(target) <= 64) {
            Step walk = walkStep(level, mission, current, target);
            if (walk != null) steps.add(walk);
        }
        return steps.isEmpty() ? null : new Plan(List.copyOf(steps), target);
    }

    /** Transitions horizontales jusqu'a la cible, une par bloc, uniquement sur du sol porteur. */
    private static Step walkStep(ServerLevel level, WorkMission mission, BlockPos from, BlockPos target) {
        BlockPos delta = new BlockPos(
                Integer.compare(target.getX(), from.getX()),
                0,
                Integer.compare(target.getZ(), from.getZ()));
        if (delta.getX() == 0 && delta.getZ() == 0) return null;
        BlockPos feet = from.offset(delta.getX(), 0, delta.getZ());
        Volume volume = volume(level, mission, feet, delta);
        if (volume == null) return null;
        return new Step(Kind.WALK, from, feet, volume.cells(), volume.support());
    }

    /**
     * Descente : le palier vise est a {@link #TREAD} blocs d'avance et un bloc plus bas. Tout le
     * volume de passage (les TREAD colonnes du palier, deux niveaux, largeur du tunnel) doit etre
     * degage avant le franchissement, sinon la chute est bloquee par la dalle precedente.
     */
    static Step step(ServerLevel level, WorkMission mission, BlockPos from, Direction dir) {
        BlockPos delta = new BlockPos(dir.getStepX(), 0, dir.getStepZ());
        BlockPos perp = delta.getZ() != 0 ? new BlockPos(1, 0, 0) : new BlockPos(0, 0, 1);
        for (BlockPos side : List.of(perp, new BlockPos(-perp.getX(), 0, -perp.getZ()))) {
            List<BlockPos> cells = new ArrayList<>();
            List<BlockPos> support = new ArrayList<>();
            boolean ok = true;
            for (int i = 1; i <= TREAD && ok; i++) {
                BlockPos base = from.offset(dir.getStepX() * i, -1, dir.getStepZ() * i);
                for (int w = 0; w < Math.max(1, WIDTH); w++) {
                    BlockPos column = w == 0 ? base : base.offset(side.getX() * w, 0, side.getZ() * w);
                    for (int h = 0; h < HEIGHT; h++) cells.add(column.above(h));
                    support.add(column.below());
                }
            }
            for (BlockPos cell : cells) {
                if (!validCell(level, mission, cell)) { ok = false; break; }
            }
            if (!ok) continue;
            for (BlockPos appui : support) {
                if (!support(level, appui)) { ok = false; break; }
                if (cells.contains(appui)) { ok = false; break; }
            }
            if (!ok) continue;
            BlockPos feet = from.offset(dir.getStepX() * TREAD, -1, dir.getStepZ() * TREAD);
            return new Step(Kind.DESCEND_STEP, from, feet, List.copyOf(cells), List.copyOf(support));
        }
        lastReason = "palier de " + TREAD + " blocs impraticable dans " + dir;
        return null;
    }

    /** Verification d'une cellule de volume : chunk, zone, claim, fluides, bloc excavable. */
    private static boolean validCell(ServerLevel level, WorkMission mission, BlockPos cell) {
        if (!level.hasChunkAt(cell) || !WorkService.loadedNeighborhood(level, cell)) { lastReason = "chunk " + cell.toShortString(); return false; }
        if (level.isOutsideBuildHeight(cell)) { lastReason = "hauteur " + cell.toShortString(); return false; }
        if (!level.getWorldBorder().isWithinBounds(cell)) { lastReason = "bordure " + cell.toShortString(); return false; }
        if (ClaimIntegration.isClaimProtected(level, cell)) { lastReason = "claim " + cell.toShortString(); return false; }
        if (WorkService.touchesFluid(level, cell)) { lastReason = "fluide " + cell.toShortString(); return false; }
        var block = level.getBlockState(cell);
        if (level.getBlockState(cell.above()).getBlock() instanceof net.minecraft.world.level.block.FallingBlock) {
            lastReason = "plafond qui tombe " + cell.toShortString(); return false;
        }
        if (block.isAir()) return true;
        if (!WorkAccess.safe(level, stateOf(level), cell, accessTop)) {
            lastReason = "bloc " + cell.toShortString() + " " + BuiltInRegistries.BLOCK.getKey(block.getBlock());
            return false;
        }
        return true;
    }

    /** Volumes retenus pour une transition : cellules a degager et appuis exiges. */
    private record Volume(List<BlockPos> cells, List<BlockPos> support) {}

    /**
     * Volume de degagement d'une transition : {@link #WIDTH} de large, {@link #HEIGHT} de haut.
     * Les deux cotes du couloir sont essayes : le tunnel reste ainsi dans la zone autorisee.
     * Rend null si aucune variante n'est praticable.
     */
    private static Volume volume(ServerLevel level, WorkMission mission, BlockPos feet, BlockPos delta) {
        BlockPos perp = delta.getZ() != 0 ? new BlockPos(1, 0, 0) : new BlockPos(0, 0, 1);
        BlockPos opposite = new BlockPos(-perp.getX(), 0, -perp.getZ());
        String firstReason = null;
        // 1. palier large (3 colonnes) : le compagnon mesure 1.09 bloc, il chevauche deux colonnes
        //    et se tient sinon sur la dalle voisine du trou, sans jamais tomber dedans.
        Volume wide = variant(level, mission, feet, perp, 3);
        if (wide != null) return wide;
        if (firstReason == null) firstReason = lastReason;
        // 2. replis : 2 colonnes, d'un cote puis de l'autre.
        for (BlockPos side : List.of(perp, opposite)) {
            Volume volume = variant(level, mission, feet, side, Math.max(1, WIDTH));
            if (volume != null) return volume;
            if (firstReason == null) firstReason = lastReason;
        }
        lastReason = "aucun cote praticable : " + firstReason;
        return null;
    }

    private static Volume variant(ServerLevel level, WorkMission mission, BlockPos feet, BlockPos side, int width) {
        List<BlockPos> cells = new ArrayList<>();
        List<BlockPos> support = new ArrayList<>();
        int left = (width - 1) / 2;
        for (int w = -left; w <= width - 1 - left; w++) {
            BlockPos column = w == 0 ? feet : feet.offset(side.getX() * w, 0, side.getZ() * w);
            for (int h = 0; h < HEIGHT; h++) cells.add(column.above(h));
            support.add(column.below());
        }
        for (BlockPos cell : cells) {
            if (!level.hasChunkAt(cell) || !WorkService.loadedNeighborhood(level, cell)) { lastReason = "chunk " + cell.toShortString(); return null; }
            if (level.isOutsideBuildHeight(cell)) { lastReason = "hauteur " + cell.toShortString(); return null; }
            if (!level.getWorldBorder().isWithinBounds(cell)) { lastReason = "bordure " + cell.toShortString(); return null; }
            if (ClaimIntegration.isClaimProtected(level, cell)) { lastReason = "claim " + cell.toShortString(); return null; }
            if (WorkService.touchesFluid(level, cell)) { lastReason = "fluide " + cell.toShortString(); return null; }
            var block = level.getBlockState(cell);
            if (level.getBlockState(cell.above()).getBlock() instanceof net.minecraft.world.level.block.FallingBlock) {
                lastReason = "plafond qui tombe " + cell.toShortString(); return null;
            }
            if (block.isAir()) continue;
            if (!WorkAccess.safe(level, stateOf(level), cell, accessTop)) {
                lastReason = "bloc " + cell.toShortString() + " "
                    + BuiltInRegistries.BLOCK.getKey(block.getBlock())
                    + " : " + WorkAccess.why(level, stateOf(level), cell);
                return null;
            }
        }
        for (BlockPos appui : support) {
            if (!support(level, appui)) { lastReason = "appui " + appui.toShortString(); return null; }
            // Un appui ne doit jamais figurer dans le volume a creuser.
            if (cells.contains(appui)) { lastReason = "appui dans le volume " + appui.toShortString(); return null; }
        }
        return new Volume(List.copyOf(cells), List.copyOf(support));
    }

    static boolean support(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) return false;
        var state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP)
                && state.getFluidState().isEmpty()
                && !(state.getBlock() instanceof net.minecraft.world.level.block.FallingBlock)
                && !state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
                && !state.hasBlockEntity()
                && !WorkService.touchesFluid(level, pos);
    }

    /** WorkAccess.safe travaille sur l'etat courant du chantier : on le reutilise tel quel. */
    private static WorkState stateOf(ServerLevel level) {
        return WorkState.get(level);
    }

    // ------------------------------------------------------------------ execution

    /**
     * Avance d'une operation atomique. Rend true si l'escalier pilote encore le chantier (creusage,
     * deplacement, verification), false quand il n'y a plus rien a faire ou que c'est impossible.
     */
    private static long lastTraceTick = 0;

    /**
     * AUDIT alpha.76 : trace brute de l'etat physique du compagnon. Aucune decision ne depend de
     * cette methode ; elle existe pour expliquer une immobilité, pas pour la contourner.
     */
    private static void trace(ServerLevel level, LivingEntity bot, BlockPos target, Exec ex) {
        long now = level.getGameTime();
        if (now - lastTraceTick < 5) return;
        lastTraceTick = now;
        String navPath = "none";
        var nav = bot instanceof Mob m ? m.getNavigation() : null;
        if (nav != null) {
            var path = nav.getPath();
            if (path != null) navPath = (path.canReach() ? "reach:" : "blocked:") + path.getTarget().toShortString();
            navPath += " prog=" + nav.isInProgress() + " done=" + nav.isDone();
        }
        var task = com.donopot.voiddynasty.ai.TaskRunner.current(bot);
        System.out.println("[STAIR_TRACE] t=" + now + " phase=" + ex.phase + " index=" + ex.index
                + " attempts=" + ex.attempts + " recovers=" + ex.recovers
                + " idleTicks=" + WorkState.get(level).idleTicks
                + " aabb=" + fmt(bot.getBoundingBox())
                + " pos=" + String.format(java.util.Locale.ROOT, "%.3f,%.3f,%.3f", bot.getX(), bot.getY(), bot.getZ())
                + " block=" + bot.blockPosition().toShortString()
                + " delta=" + String.format(java.util.Locale.ROOT, "%.3f,%.3f,%.3f",
                        bot.getDeltaMovement().x, bot.getDeltaMovement().y, bot.getDeltaMovement().z)
                + " onGround=" + bot.onGround()
                + " hColl=" + bot.horizontalCollision
                + " vColl=" + bot.verticalCollision
                + " noAi=" + (bot instanceof Mob mm && mm.isNoAi())
                + " noGrav=" + bot.isNoGravity()
                + " task=" + (task == null ? "none" : task.getClass().getSimpleName())
                + " nav=" + navPath
                + " target=" + target.toShortString());
    }

    // ------------------------------------------------------------------ machine a etats

    /**
     * Cycle d'execution d'un escalier. DESCEND ne valide plus l'arrivee : il constate seulement le
     * franchissement du niveau. L'atterrissage reel est mesure a part (LANDING_VERIFY), puis
     * l'alignement horizontal jusqu'au palier planifie (ALIGN), et seule STEP_VERIFY incremente
     * l'index de marche.
     */
    enum Phase { PLAN_STEP, CLEAR_STEP, DESCEND, LANDING_VERIFY, ALIGN, STEP_VERIFY, RECOVER }

    private static final class Exec {
        Plan plan;
        int index;
        Phase phase = Phase.PLAN_STEP;
        long startTick;
        long phaseStart;
        long stepStart;
        int attempts;
        int recovers;
        int stepRecovers;
        int alignTries;
        int alignDigs;
        String signature = "";
        String lastSignature = "";
        int sameSignature;
        Phase lastPhase = null;
        Vec3 actualLanding;
        Vec3 plannedLanding;
        BlockPos exit;
        double alignX, alignZ;
        long alignMoveTick;
    }

    private static final Map<String, Exec> EXECS = new HashMap<>();
    private static final long STEP_TIMEOUT = 45L * 20L;      // ticks par marche (toutes phases)
    private static final long LANDING_TIMEOUT = 6L * 20L;    // stabilisation apres chute
    private static final long STAIR_TIMEOUT = 300L * 20L;    // ticks pour l'escalier entier
    private static final long ALIGN_STILL = 40L;             // ticks sans bouger en ALIGN
    private static final int MAX_RECOVERS = 5, MAX_STEP_RECOVERS = 2, MAX_VERIFY_RETRIES = 3;
    private static final int MAX_ALIGN_TRIES = 6, MAX_ALIGN_DIGS = 12;

    private static Exec exec(String key) {
        return EXECS.computeIfAbsent(key, k -> new Exec());
    }

    private static void enter(Exec ex, Phase phase, long now) {
        ex.phase = phase;
        ex.phaseStart = now;
        if (phase == Phase.CLEAR_STEP) { ex.attempts = 0; ex.stepStart = now; }
        if (phase == Phase.ALIGN) { ex.alignTries = 0; ex.alignDigs = 0; ex.alignMoveTick = now;
            ex.alignX = Double.NaN; ex.alignZ = Double.NaN; }
    }

    private static void stairFail(WorkState state, Exec ex, String key, String code, String detail) {
        state.lastStop = code + " : " + detail + " (marche " + (ex.index + 1) + ")";
        System.out.println("[STAIR_FAIL] code=" + code + " detail=" + detail
                + " index=" + ex.index + " phase=" + ex.phase + " pos=" + state.lastStop);
        ex.plan = null;
        ex.index = 0;
        EXECS.remove(key);
        PLANS.remove(key);
        WorkService.pause(state, code);
    }

    static boolean drive(ServerLevel level, WorkState state, LivingEntity bot, BlockPos target) {
        WorkMission mission = MissionService.active(state);
        if (mission == null || !(bot instanceof Mob mob)) return false;
        if (mission.accessMined >= WorkAccess.MAX_BLOCKS) {
            WorkService.pause(state, "ACCESS_BUDGET_EXHAUSTED");
            return false;
        }
        String key = mission.id;
        Exec ex = exec(key);
        long now = level.getGameTime();
        if (ex.startTick == 0L) {
            System.out.println("[STAIR_ENTER] mission=" + key + " pos=" + bot.blockPosition()
                    + " target=" + (target == null ? "?" : target.toShortString())
                    + " rise=" + (bot.blockPosition().getY() - (target == null ? 0 : target.getY())));
        }
        if (ex.startTick == 0L) { ex.startTick = now; ex.phaseStart = now; }
        if (now - ex.startTick > STAIR_TIMEOUT) {
            stairFail(state, ex, key, "STAIR_TIMEOUT", "5 minutes sans terminer l'escalier");
            return false;
        }
        trace(level, bot, target, ex);
        // Une transition de phase est une progression reelle : le watchdog global est informe,
        // mais aucune activite repetee ne remet le compteur a zero.
        if (ex.lastPhase != ex.phase) {
            ex.lastPhase = ex.phase;
            state.idleTicks = 0;
            state.movementCells.clear();
        }

        switch (ex.phase) {
            case PLAN_STEP -> planStep(level, state, bot, mob, target, key, ex, now);
            case CLEAR_STEP -> clearStep(level, state, bot, mob, target, key, ex, now);
            case DESCEND -> descendStep(level, state, bot, mob, target, key, ex, now);
            case LANDING_VERIFY -> landingVerify(level, state, bot, mob, target, key, ex, now);
            case ALIGN -> align(level, state, bot, mob, target, key, ex, now);
            case STEP_VERIFY -> stepVerify(level, state, bot, mob, target, key, ex, now);
            case RECOVER -> recover(level, state, bot, target, key, ex, now);
        }
        return true;
    }

    /** Plan courant, ou replanification locale depuis la position physique reelle. */
    private static void planStep(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                 BlockPos target, String key, Exec ex, long now) {
        boolean needPlan = ex.plan == null || ex.index >= ex.plan.steps().size();
        if (!needPlan) {
            Step step = ex.plan.steps().get(ex.index);
            BlockPos here = bot.blockPosition();
            int dx = Math.abs(here.getX() - step.from().getX());
            int dz = Math.abs(here.getZ() - step.from().getZ());
            int dy = Math.abs(here.getY() - step.from().getY());
            if (dx > 2 || dz > 2 || dy > 2) {
                // Plan desynchronise (cause A) : on recalcule depuis la position reelle, en gardant
                // la destination globale et les excavations deja confirmees.
                System.out.println("[STAIR_LOCAL] resync index=" + ex.index + " planFrom=" + step.from()
                        + " realPos=" + here);
                needPlan = true;
            }
        }
        if (needPlan) {
            Plan rebuilt = plan(level, state, bot, target);
            if (rebuilt == null || rebuilt.steps().isEmpty()) {
                enter(ex, Phase.RECOVER, now);
                return;
            }
            ex.plan = rebuilt;
            ex.index = 0;
            System.out.println("[STAIR_PLAN] steps=" + rebuilt.steps().size()
                    + " from=" + bot.blockPosition() + " target=" + target);
            PLANS.put(key, rebuilt);
        }
        Step step = ex.plan.steps().get(ex.index);
        state.lastStop = "escalier " + ex.phase + " marche " + (ex.index + 1) + "/" + ex.plan.steps().size()
                + " -> " + step.feet().toShortString();
        enter(ex, Phase.CLEAR_STEP, now);
    }

    /** Degagement : volume planifie puis empreinte reelle, une operation confirmee par tick. */
    private static void clearStep(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                  BlockPos target, String key, Exec ex, long now) {
        Step step = ex.plan.steps().get(ex.index);
        List<BlockPos> cells = new ArrayList<>(step.clear());
        if (step.kind() == Kind.DESCEND_STEP) cells.addAll(footprintCells(bot, step.feet()));
        for (BlockPos cell : cells) {
            if (level.getBlockState(cell).isAir()) continue;
            if (!WorkAccess.safe(level, state, cell, accessTop)) {
                signatureFail(state, ex, key, "CLEAR_REFUSED", cell + " : " + WorkAccess.why(level, state, cell));
                return;
            }
            double distance = WorkService.distanceSq(bot, cell);
            if (!WorkService.visible(level, bot, cell)) {
                // Bloc masque par celui du dessus : on degage d'abord la couverture (creusement par
                // le haut), au lieu de chercher une position de travail qui n'existe pas.
                BlockPos over = cell.above();
                if (!level.getBlockState(over).isAir()
                        && WorkAccess.safe(level, state, over, accessTop)
                        && WorkService.distanceSq(bot, over) <= WorkService.reach() * WorkService.reach()
                        && WorkService.visible(level, bot, over)) {
                    digStep(level, state, bot, mob, over, "OVERBURDEN", ex);
                    return;
                }
            }
            if (distance > WorkService.reach() * WorkService.reach() || !WorkService.visible(level, bot, cell)) {
                // Position de travail stable : on ne recalcule plus la cible d'approche a chaque
                // tick (source de l'oscillation entre deux positions devant un bloc 2 niveaux plus bas).
                approachPose(level, state, bot, mob, cell, key, ex);
                return;
            }
            if (digStep(level, state, bot, mob, cell, "CLEAR_STEP", ex)) return;
        }
        // Volume degage : on verifie les appuis avant de descendre.
        for (BlockPos appui : step.support()) {
            if (!support(level, appui)) {
                signatureFail(state, ex, key, "SUPPORT_LOST", appui.toShortString());
                return;
            }
        }
        enter(ex, Phase.DESCEND, now);
    }

    /**
     * Casse une cellule avec progression reelle et confirmation par relecture.
     * @return true si une action (approche, progression ou casse) a consomme ce tick.
     */
    private static boolean digStep(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                   BlockPos cell, String phase, Exec ex) {
        double distance = WorkService.distanceSq(bot, cell);
        if (distance > WorkService.reach() * WorkService.reach() || !WorkService.visible(level, bot, cell)) {
            approach(level, mob, cell);
            state.lastStop = "escalier " + phase + " approche " + cell.toShortString();
            return true;
        }
        mob.getNavigation().stop();
        if (state.accessBlock == null || !state.accessBlock.equals(cell)) {
            state.accessBlock = cell;
            state.progress = 0;
        }
        state.progress += WorkService.speedMult() / 20.0;
        var bs = level.getBlockState(cell);
        if (state.progress < WorkService.breakSeconds(level, state, cell, bs)) {
            state.lastStop = "escalier " + phase + " " + cell.toShortString() + " "
                    + BuiltInRegistries.BLOCK.getKey(bs.getBlock());
            return true;
        }
        String block = BuiltInRegistries.BLOCK.getKey(bs.getBlock()).toString();
        WorkService.breakTarget(level, state, bot, cell, true);
        boolean confirmed = level.getBlockState(cell).isAir();
        state.movementCells.clear();
        state.idleTicks = 0;
        state.setDirty();
        System.out.println("[STAIR_CLEAR] index=" + ex.index + " phase=" + phase + " cell=" + cell.toShortString()
                + " block=" + block + " confirmed=" + confirmed);
        return true;
    }

    // ---- positions de travail (FindWorkPose) -------------------------------------------------

    private static final Map<String, BlockPos> POSES = new HashMap<>();
    private static final Map<String, BlockPos> POSE_CELLS = new HashMap<>();
    private static final Map<String, Double> POSE_DIST = new HashMap<>();
    private static final Map<String, Integer> POSE_STILL = new HashMap<>();

    /**
     * Choisit UNE position de travail sure d'ou le bloc est atteignable et visible, et la conserve
     * pendant toute l'approche. Recalculee seulement si le monde change ou si l'approche stagne.
     */
    private static BlockPos findWorkPose(ServerLevel level, WorkState state, LivingEntity bot, BlockPos cell) {
        WorkMission mission = MissionService.active(state);
        if (mission == null) return null;
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        double reach = WorkService.reach() - 0.4;
        for (int dy = -2; dy <= 3; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pose = cell.offset(dx, dy, dz);
                    if (!poseValid(level, bot, pose, mission)) continue;
                    Vec3 eye = new Vec3(pose.getX() + 0.5, pose.getY() + WorkService.eyeHeight(bot), pose.getZ() + 0.5);
                    if (eye.distanceToSqr(Vec3.atCenterOf(cell)) > reach * reach) continue;
                    if (!WorkService.lineOfSight(level, bot, eye, cell)) continue;
                    double score = pose.distSqr(bot.blockPosition()) + Math.abs(dy) * 1.5;
                    if (score < bestScore) { bestScore = score; best = pose; }
                }
            }
        }
        return best;
    }

    /** Position sure : zone autorisee, appui solide, deux cases libres, AABB sans collision, pas de danger. */
    private static boolean poseValid(ServerLevel level, LivingEntity bot, BlockPos pose) {
        WorkMission mission = MissionService.active(WorkState.get(level));
        return poseValid(level, bot, pose, mission);
    }

    private static boolean poseValid(ServerLevel level, LivingEntity bot, BlockPos pose, WorkMission mission) {
        if (mission == null || !WorkAccess.columnOf(mission, pose)) return false;
        if (!level.hasChunkAt(pose) || level.isOutsideBuildHeight(pose)) return false;
        if (!level.getBlockState(pose).isAir() || !level.getBlockState(pose.above()).isAir()) return false;
        if (!support(level, pose.below())) return false;
        if (WorkService.touchesFluid(level, pose)) return false;
        var box = bot.getBoundingBox().move(pose.getX() + 0.5 - bot.getX(), pose.getY() - bot.getY(), pose.getZ() + 0.5 - bot.getZ());
        return level.noCollision(bot, box);
    }

    /**
     * Approche vers la position de travail conservee. Une seule cible par tentative ; un blocage
     * mesure declenche un recalcul unique puis un echec explicite (aucune boucle d'approche).
     */
    private static void approachPose(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                     BlockPos cell, String key, Exec ex) {
        BlockPos pose = POSES.get(key);
        if (pose == null || !cell.equals(POSE_CELLS.get(key)) || !poseValid(level, bot, pose)) {
            pose = findWorkPose(level, state, bot, cell);
            if (pose == null) {
                signatureFail(state, ex, key, "NO_WORK_POSE", "aucune position de travail pour " + cell.toShortString());
                return;
            }
            POSES.put(key, pose);
            POSE_CELLS.put(key, cell);
            POSE_DIST.remove(key);
            POSE_STILL.put(key, 0);
            System.out.println("[WORK_POSE] cell=" + cell.toShortString() + " pose=" + pose.toShortString()
                    + " bot=" + bot.blockPosition().toShortString());
        }
        double distance = Math.sqrt(WorkService.distanceSq(bot, pose));
        double previous = POSE_DIST.getOrDefault(key, Double.MAX_VALUE);
        if (distance < previous - 0.05) {
            POSE_DIST.put(key, distance);
            POSE_STILL.put(key, 0);
        } else {
            int still = POSE_STILL.getOrDefault(key, 0) + 1;
            POSE_STILL.put(key, still);
            if (still == 60) {
                System.out.println("[WORK_POSE] stagnation, recalcul unique cell=" + cell.toShortString());
                POSES.remove(key);
            }
            if (still > 120) {
                signatureFail(state, ex, key, "POSE_UNREACHABLE",
                        "position " + pose.toShortString() + " inatteignable (" + still + " ticks)");
                return;
            }
        }
        if (level.getGameTime() % 8L == 0L) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(pose.getX() + 0.5, pose.getY(), pose.getZ() + 0.5, 1.0);
        }
        state.lastStop = "escalier CLEAR marche " + (ex.index + 1) + "/" + ex.plan.steps().size()
                + " pose " + pose.toShortString() + " -> " + cell.toShortString()
                + " d=" + String.format(java.util.Locale.ROOT, "%.2f", distance);
    }

    /**
     * Descente : on ne valide plus l'arrivee au palier, seulement le franchissement du niveau.
     * L'atterrissage reel et l'alignement sont traites par les phases suivantes.
     */
    private static void descendStep(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                    BlockPos target, String key, Exec ex, long now) {
        Step step = ex.plan.steps().get(ex.index);
        BlockPos feet = step.feet();
        double dy = bot.getY() - feet.getY();
        boolean walk = step.kind() != Kind.DESCEND_STEP;
        // Une transition de descente n'est franchie que si le niveau inferieur est REELLEMENT
        // atteint : partir de startFeetY et exiger exactement startFeetY - 1 interdit tout
        // faux succes sur un simple deplacement horizontal (dy = 0).
        if (walk) {
            // Marche a niveau : aucune logique de chute. On va directement a l'alignement,
            // sinon LANDING_VERIFY exige un contact au sol pour un simple deplacement horizontal.
            System.out.println("[STAIR_WALK] index=" + ex.index + " from=" + step.from().toShortString()
                    + " to=" + feet.toShortString());
            enter(ex, Phase.ALIGN, now);
            return;
        }
        boolean crossed = bot.getY() <= step.from().getY() - 1 + 0.05;
        if (crossed) {
            System.out.println("[STAIR_DESCEND] index=" + ex.index + " kind=" + step.kind()
                    + " startFeetY=" + step.from().getY() + " expectedFeetY=" + feet.getY()
                    + " pos=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ())
                    + " planned=" + feet.toShortString() + " dy=" + String.format(java.util.Locale.ROOT, "%.2f", dy));
            enter(ex, Phase.LANDING_VERIFY, now);
            return;
        }
        if (now - ex.stepStart > STEP_TIMEOUT) {
            signatureFail(state, ex, key, "STEP_TIMEOUT", "aucune descente en 45 s vers " + feet.toShortString()
                    + " (pos=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ()) + ")");
            return;
        }
        if (step.kind() == Kind.DESCEND_STEP) {
            if (!descend(level, mob, step.from(), feet, key)) {
                signatureFail(state, ex, key, "DESCEND_BLOCKED", "impulsions épuisées vers " + feet.toShortString());
                return;
            }
            state.lastStop = "escalier DESCEND marche " + (ex.index + 1) + "/" + ex.plan.steps().size()
                    + " essai " + DESCEND_TRIES.getOrDefault(key, 0) + "/" + MAX_DESCEND_TRIES;
            return;
        }
        approach(level, mob, feet);
        state.lastStop = "escalier DESCEND marche " + (ex.index + 1) + "/" + ex.plan.steps().size()
                + " vers " + feet.toShortString();
    }

    /**
     * Atterrissage : on mesure la position reelle, l'appui sous l'AABB et l'absence de penetration.
     * Une entite encore en l'air ou posee sur du vide n'est jamais declaree arrivee.
     */
    private static void landingVerify(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                      BlockPos target, String key, Exec ex, long now) {
        Step step = ex.plan.steps().get(ex.index);
        BlockPos feet = step.feet();
        BlockPos here = BlockPos.containing(bot.position());
        double dy = bot.getY() - feet.getY();
        boolean levelOk = Math.abs(dy) <= 0.7;
        boolean grounded = bot.onGround();
        boolean stable = Math.abs(bot.getDeltaMovement().y) < 0.08;
        boolean supported = footprintSupported(level, bot, here);
        boolean free = level.noCollision(bot, bot.getBoundingBox().deflate(0.02));
        boolean danger = danger(level, here);
        if (ex.actualLanding == null || now % 10L == 0L) {
            System.out.println("[STAIR_LANDING] index=" + ex.index
                    + " planned=" + feet.toShortString()
                    + " actual=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ())
                    + " dy=" + String.format(java.util.Locale.ROOT, "%.2f", dy)
                    + " level=" + levelOk + " grounded=" + grounded + " stable=" + stable
                    + " supported=" + supported + " free=" + free + " danger=" + danger
                    + " aabb=" + fmt(bot.getBoundingBox()));
        }
        if (levelOk && grounded && stable && supported && free && !danger) {
            Vec3 landing = bot.position();
            ex.actualLanding = landing;
            ex.plannedLanding = centre(feet);
            ex.exit = feet;
            System.out.println("[STAIR_ALIGN] index=" + ex.index
                    + " from=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", landing.x, landing.y, landing.z)
                    + " to=" + feet.toShortString()
                    + " dx=" + String.format(java.util.Locale.ROOT, "%.2f", feet.getX() + 0.5 - landing.x)
                    + " dz=" + String.format(java.util.Locale.ROOT, "%.2f", feet.getZ() + 0.5 - landing.z));
            enter(ex, Phase.ALIGN, now);
            return;
        }
        if (now - ex.phaseStart > LANDING_TIMEOUT) {
            signatureFail(state, ex, key, "LANDING_FAILED", "atterrissage non stable"
                    + " level=" + levelOk + " grounded=" + grounded + " supported=" + supported
                    + " free=" + free + " danger=" + danger);
            return;
        }
        mob.setDeltaMovement(Vec3.ZERO.x, mob.getDeltaMovement().y, Vec3.ZERO.z);
        state.lastStop = "escalier LANDING marche " + (ex.index + 1) + " stabilisation";
    }

    /**
     * Alignement : trajectoire horizontale a hauteur constante depuis l'atterrissage reel jusqu'au
     * palier planifie. Les cellules solides intermediaires sont creusees (une par tick), puis le
     * deplacement est confirme par la position reelle, pas par l'emission de la commande.
     */
    private static void align(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                              BlockPos target, String key, Exec ex, long now) {
        Step step = ex.plan.steps().get(ex.index);
        BlockPos exit = step.feet();
        double cx = exit.getX() + 0.5, cz = exit.getZ() + 0.5;
        double dist = Math.hypot(bot.getX() - cx, bot.getZ() - cz);
        boolean levelOk = Math.abs(bot.getY() - exit.getY()) <= 0.7;
        if (dist <= 1.05 && levelOk) {
            System.out.println("[STAIR_ALIGN] index=" + ex.index + " aligned dist="
                    + String.format(java.util.Locale.ROOT, "%.2f", dist) + " pos="
                    + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ()));
            enter(ex, Phase.STEP_VERIFY, now);
            return;
        }
        if (now - ex.phaseStart > STEP_TIMEOUT) {
            signatureFail(state, ex, key, "ALIGN_TIMEOUT", "alignement impossible vers " + exit.toShortString()
                    + " depuis " + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ()));
            return;
        }
        BlockPos here = bot.blockPosition();
        int stepX = Integer.signum(exit.getX() - here.getX());
        int stepZ = Integer.signum(exit.getZ() - here.getZ());
        // 1. degager la cellule suivante sur le trajet, a hauteur de palier constante
        if (stepX != 0 || stepZ != 0) {
            BlockPos next = here.offset(stepX, 0, stepZ);
            for (int h = 0; h < HEIGHT; h++) {
                BlockPos cell = next.above(h);
                if (level.getBlockState(cell).isAir()) continue;
                if (!WorkAccess.safe(level, state, cell, accessTop)) {
                    signatureFail(state, ex, key, "ALIGN_REFUSED", cell + " : " + WorkAccess.why(level, state, cell));
                    return;
                }
                if (ex.alignDigs >= MAX_ALIGN_DIGS) {
                    signatureFail(state, ex, key, "ALIGN_BLOCKED", "trop de blocs sur le trajet (" + ex.alignDigs + ")");
                    return;
                }
                boolean acted = digStep(level, state, bot, mob, cell, "ALIGN", ex);
                if (level.getBlockState(cell).isAir()) ex.alignDigs++;
                state.lastStop = "escalier ALIGN degagement " + cell.toShortString();
                if (acted) return;
            }
        }
        // 2. avancer horizontalement, sans jamais viser un autre niveau
        if (now % 8L == 0L) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(cx, bot.getY(), cz, 1.0);
        }
        // 3. confirmer le mouvement reel
        if (Double.isNaN(ex.alignX) || now - ex.alignMoveTick > ALIGN_STILL) {
            double moved = Double.isNaN(ex.alignX) ? 99.0
                    : Math.hypot(bot.getX() - ex.alignX, bot.getZ() - ex.alignZ);
            if (moved < 0.05 && !Double.isNaN(ex.alignX)) {
                ex.alignTries++;
                System.out.println("[STAIR_ALIGN] index=" + ex.index + " immobile essai=" + ex.alignTries
                        + " pos=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ())
                        + " cible=" + exit.toShortString() + " dist=" + String.format(java.util.Locale.ROOT, "%.2f", dist));
                if (ex.alignTries > MAX_ALIGN_TRIES) {
                    signatureFail(state, ex, key, "ALIGN_STUCK", "aucun mouvement confirmé vers " + exit.toShortString());
                    return;
                }
                // impulsion horizontale mesuree, sans changer la hauteur du palier
                double dx = cx - bot.getX(), dz = cz - bot.getZ();
                double norm = Math.sqrt(dx * dx + dz * dz);
                if (norm > 0.001) mob.setDeltaMovement(dx / norm * 0.12, mob.getDeltaMovement().y, dz / norm * 0.12);
                mob.syncVelocity = true;
            }
            ex.alignX = bot.getX();
            ex.alignZ = bot.getZ();
            ex.alignMoveTick = now;
        }
        state.lastStop = "escalier ALIGN marche " + (ex.index + 1) + "/" + ex.plan.steps().size()
                + " dist=" + String.format(java.util.Locale.ROOT, "%.2f", dist);
    }

    /** Verification finale : seule cette phase incremente l'index de marche. */
    private static void stepVerify(ServerLevel level, WorkState state, LivingEntity bot, Mob mob,
                                   BlockPos target, String key, Exec ex, long now) {
        Step step = ex.plan.steps().get(ex.index);
        BlockPos feet = step.feet();
        BlockPos here = bot.blockPosition();
        double dx = bot.getX() - (feet.getX() + 0.5);
        double dz = bot.getZ() - (feet.getZ() + 0.5);
        double dy = bot.getY() - feet.getY();
        boolean descend = step.kind() == Kind.DESCEND_STEP;
        // Descente : le niveau doit etre exactement celui attendu (startFeetY - 1).
        // Marche a niveau : simple tolerance de terrain.
        boolean levelOk = descend ? Math.abs(dy) <= 0.05 && Math.abs(bot.getY() - (step.from().getY() - 1)) <= 0.05
                : Math.abs(dy) <= 0.5;
        boolean positionOk = Math.abs(dx) <= 1.05 && Math.abs(dz) <= 1.05 && levelOk;
        boolean supportOk = footprintSupported(level, bot, here);
        boolean free = level.noCollision(bot, bot.getBoundingBox().deflate(0.02));
        System.out.println("[STAIR_VERIFY] index=" + ex.index + " kind=" + step.kind()
                + " expectedFeetY=" + feet.getY() + " levelOk=" + levelOk
                + " pos=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", bot.getX(), bot.getY(), bot.getZ())
                + " expected=" + feet.toShortString()
                + " dx=" + String.format(java.util.Locale.ROOT, "%.2f", dx)
                + " dz=" + String.format(java.util.Locale.ROOT, "%.2f", dz)
                + " dy=" + String.format(java.util.Locale.ROOT, "%.2f", dy)
                + " support=" + supportOk + " free=" + free + " aabb=" + fmt(bot.getBoundingBox()));
        if (positionOk && supportOk && free) {
            ex.index++;
            ex.stepRecovers = 0;
            // Nouvelle marche : la position de travail de la precedente n'est plus valable.
            POSES.remove(key); POSE_CELLS.remove(key); POSE_DIST.remove(key); POSE_STILL.remove(key);
            ex.attempts = 0;
            DESCEND_TRIES.remove(key);
            DESCEND_LAST.remove(key);
            state.accessBlock = null;
            state.progress = 0;
            state.movementCells.clear();
            state.idleTicks = 0;
            System.out.println("[STAIR_STEP] index=" + ex.index + "/" + ex.plan.steps().size()
                    + " kind=" + step.kind() + " startFeetY=" + step.from().getY()
                    + " expectedFeetY=" + feet.getY() + " actualFeetY="
                    + String.format(java.util.Locale.ROOT, "%.2f", bot.getY())
                    + " confirmed feet=" + feet.toShortString() + " pos=" + here.toShortString());
            if (ex.index >= ex.plan.steps().size()) {
                System.out.println("[STAIR_DONE] steps=" + ex.plan.steps().size() + " pos=" + here);
                state.lastStop = "escalier termine : " + ex.plan.steps().size() + " transitions";
                ex.plan = null;
                ex.index = 0;
                EXECS.remove(key);
                PLANS.remove(key);
                FAILURES.remove(key);
                return;
            }
            enter(ex, Phase.PLAN_STEP, now);
            return;
        }
        ex.attempts++;
        if (ex.attempts > MAX_VERIFY_RETRIES) {
            signatureFail(state, ex, key, "VERIFY_FAILED", "position/support/collision non conformes");
            return;
        }
        // Position hors tolerance mais niveau et appui corrects : on reprend l'alignement.
        enter(ex, Phase.ALIGN, now);
    }

    /** Danger immediat sous ou autour du palier : lave, feu, magma, neige poudree. */
    private static boolean danger(ServerLevel level, BlockPos here) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 0; dy++) {
                    var bs = level.getBlockState(here.offset(dx, dy, dz));
                    if (WorkService.touchesFluid(level, here.offset(dx, dy, dz))) return true;
                    if (bs.is(net.minecraft.tags.BlockTags.FIRE)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Recuperation locale bornee : on ne recalcule que le segment courant depuis la position
     * reelle. Une meme signature d'echec repetee deux fois arrete proprement.
     */
    private static void recover(ServerLevel level, WorkState state, LivingEntity bot, BlockPos target,
                                String key, Exec ex, long now) {
        if (!ex.signature.isEmpty()) {
            if (ex.signature.equals(ex.lastSignature)) {
                ex.sameSignature++;
            } else {
                ex.lastSignature = ex.signature;
                ex.sameSignature = 1;
            }
            ex.signature = "";
        }
        String current = ex.lastSignature + "@" + bot.blockPosition().toShortString();
        ex.recovers++;
        ex.stepRecovers++;
        System.out.println("[STAIR_RECOVER] n=" + ex.recovers + " step=" + ex.stepRecovers
                + " same=" + ex.sameSignature + " at=" + bot.blockPosition() + " reason=" + state.lastStop);
        if (ex.sameSignature >= 2 || ex.recovers > MAX_RECOVERS || ex.stepRecovers > MAX_STEP_RECOVER_TOTAL) {
            stairFail(state, ex, key, "STAIR_BLOCKED", "aucune solution locale (" + current + ")");
            return;
        }
        Plan rebuilt = plan(level, state, bot, target);
        if (rebuilt == null || rebuilt.steps().isEmpty()) {
            stairFail(state, ex, key, "STAIR_NO_ROUTE", "aucune transition praticable : " + lastReason);
            return;
        }
        ex.plan = rebuilt;
        ex.index = 0;
        PLANS.put(key, rebuilt);
        System.out.println("[STAIR_RECOVER] nouveau plan " + rebuilt.steps().size() + " transitions depuis "
                + bot.blockPosition());
        enter(ex, Phase.PLAN_STEP, now);
    }

    private static final int MAX_STEP_RECOVER_TOTAL = 6;

    private static void signatureFail(WorkState state, Exec ex, String key, String code, String detail) {
        ex.signature = code + "@" + ex.index;
        ex.phase = Phase.RECOVER;
        state.lastStop = "escalier " + code + " : " + detail + " (marche " + (ex.index + 1) + ")";
        System.out.println("[STAIR_SIGNAL] code=" + code + " index=" + ex.index + " detail=" + detail);
    }

    private static String fmt(net.minecraft.world.phys.AABB box) {
        return String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f..%.2f,%.2f,%.2f",
                box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    /**
     * Arrivee reelle : coordonnees Y de l'entite (pas le bloc arrondi). Une tolerance d'un bloc
     * en Y faisait croire a une marche franchie alors que le compagnon etait encore au-dessus.
     */
    private static boolean arrived(LivingEntity bot, BlockPos feet) {
        BlockPos here = bot.blockPosition();
        return Math.abs(here.getX() - feet.getX()) <= 1 && Math.abs(here.getZ() - feet.getZ()) <= 1
                && Math.abs(bot.getY() - feet.getY()) <= 0.6;
    }

    /**
     * Cellules reellement occupees par l'empreinte de collision a hauteur du palier vise :
     * toutes les colonnes que l'AABB recouvre, aux deux niveaux du tunnel.
     */
    private static List<BlockPos> footprintCells(LivingEntity bot, BlockPos feet) {
        var box = bot.getBoundingBox();
        double shrink = 0.03;
        int minX = (int) Math.floor(box.minX + shrink), maxX = (int) Math.floor(box.maxX - shrink);
        int minZ = (int) Math.floor(box.minZ + shrink), maxZ = (int) Math.floor(box.maxZ - shrink);
        List<BlockPos> cells = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int h = 0; h < HEIGHT; h++) {
                    BlockPos cell = new BlockPos(x, feet.getY() + h, z);
                    if (!cells.contains(cell)) cells.add(cell);
                }
            }
        }
        return cells;
    }

    /**
     * Descente physique d'un cran : guidage du MoveControl vers un point decale du bord (calcule
     * sur la largeur reelle), puis impulsion mesuree si aucun progres. Aucune teleportation, aucune
     * suppression de collision : la gravite et les collisions du jeu restent seules responsables.
     */
    private static boolean descend(ServerLevel level, Mob mob, BlockPos from, BlockPos feet, String key) {
        int tries = DESCEND_TRIES.getOrDefault(key, 0);
        if (tries >= MAX_DESCEND_TRIES) {
            DESCEND_TRIES.remove(key);
            DESCEND_LAST.remove(key);
            return false;
        }
        long now = level.getGameTime();
        long last = DESCEND_LAST.getOrDefault(key, 0L);
        if (last == 0L) { mob.getNavigation().stop(); last = now; DESCEND_LAST.put(key, now); }
        double bias = Math.max(0.18, (mob.getBbWidth() / 2.0 - 0.5) + 0.12);
        double targetX = feet.getX() + 0.5 + Integer.compare(feet.getX(), from.getX()) * bias;
        double targetZ = feet.getZ() + 0.5 + Integer.compare(feet.getZ(), from.getZ()) * bias;
        if (now - last >= 8L) {
            mob.getMoveControl().setWantedPosition(targetX, feet.getY() + 0.05, targetZ, 1.0);
        }
        if (now - last >= 12L) {
            double dx = targetX - mob.getX();
            double dz = targetZ - mob.getZ();
            double norm = Math.sqrt(dx * dx + dz * dz);
            if (norm > 0.001) { dx = dx / norm * 0.14; dz = dz / norm * 0.14; } else { dx = 0; dz = 0; }
            mob.setDeltaMovement(dx, -0.30, dz);
            mob.syncVelocity = true;
            DESCEND_TRIES.put(key, tries + 1);
            DESCEND_LAST.put(key, now);
            System.out.println("[STAIR_IMPULSE] vers " + feet + " essai=" + (tries + 1)
                    + " pos=" + String.format(java.util.Locale.ROOT, "%.2f,%.2f,%.2f", mob.getX(), mob.getY(), mob.getZ()));
        }
        return true;
    }

    private static boolean atFeet(LivingEntity bot, BlockPos feet) {
        BlockPos current = bot.blockPosition();
        // Tolerance : la navigation vanilla s'arrete rarement au centre exact d'un bloc.
        return Math.abs(current.getX() - feet.getX()) <= 1 && Math.abs(current.getZ() - feet.getZ()) <= 1
                && Math.abs(current.getY() - feet.getY()) <= 1;
    }

    /**
     * Va vers une cible. Si le chemin complet est refuse (terrain casse, distance), on avance par
     * bonds de 16, 12 puis 8 blocs vers la cible : chaque bond reussi rapproche, sans jamais
     * remplacer la navigation par une marche forcee a travers un mur.
     */
    private static void approach(ServerLevel level, Mob mob, BlockPos target) {
        // Descente d'un cran : la navigation refuse souvent de sauter dans un trou d'un bloc.
        // On guide alors directement le deplacement vers le centre du palier.
        BlockPos here = mob.blockPosition();
        boolean dropOne = target.getY() < here.getY()
                && Math.abs(target.getX() - here.getX()) <= 2 && Math.abs(target.getZ() - here.getZ()) <= 2;
        // Une descente n'est jamais traitee ici : elle exige un point d'appui decale du bord
        // (voir descend()), sinon le compagnon reste pose sur la dalle precedente.
        if (mob.getNavigation().isInProgress()) return;
        var path = mob.getNavigation().createPath(target, 1);
        if (path != null && path.canReach() && mob.getNavigation().moveTo(path, 1.0)) return;
        BlockPos from = mob.blockPosition();
        for (int step : new int[]{16, 12, 8, 4}) {
            BlockPos waypoint = waypoint(from, target, step);
            if (waypoint.equals(from)) continue;
            var hop = mob.getNavigation().createPath(waypoint, 1);
            if (hop != null && hop.canReach() && mob.getNavigation().moveTo(hop, 1.0)) return;
        }
        double offset = Math.floor(mob.getBbWidth() + 1.0) * 0.5;
        mob.getMoveControl().setWantedPosition(target.getX() + offset, target.getY(), target.getZ() + offset, 1.0);
    }

    private static BlockPos waypoint(BlockPos from, BlockPos target, int distance) {
        double dx = target.getX() - from.getX();
        double dz = target.getZ() - from.getZ();
        double norm = Math.sqrt(dx * dx + dz * dz);
        if (norm < 0.5) return from;
        double scale = Math.min(1.0, distance / norm);
        return new BlockPos(
                (int) Math.round(from.getX() + dx * scale),
                from.getY(),
                (int) Math.round(from.getZ() + dz * scale));
    }

    /**
     * Verification d'appui a l'execution : blocs reellement sous l'empreinte de collision du
     * compagnon (et non sous toute la largeur du tunnel).
     */
    static boolean footprintSupported(ServerLevel level, LivingEntity bot, BlockPos feet) {
        AABB box = bot.getBoundingBox();
        double shrink = 0.02;
        int y = feet.getY() - 1;
        int minX = (int) Math.floor(box.minX + shrink);
        int maxX = (int) Math.floor(box.maxX - shrink);
        int minZ = (int) Math.floor(box.minZ + shrink);
        int maxZ = (int) Math.floor(box.maxZ - shrink);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!support(level, new BlockPos(x, y, z))) return false;
            }
        }
        return true;
    }

    /** Trace lisible pour `servitor_work` : plan courant et avancement. */
    static String describe(String missionId) {
        Plan plan = PLANS.get(missionId);
        if (plan == null) return "";
        int index = INDEX.getOrDefault(missionId, 0);
        return "escalier " + index + "/" + plan.steps().size();
    }

    static Vec3 centre(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }
}
