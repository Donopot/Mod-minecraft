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
    private static final int MAX_FAILURES = 4;

    private StairMining() {}

    enum Kind { DESCEND_STEP, WALK }

    /** Une transition : d'un palier au suivant, avec ses volumes propres. */
    record Step(Kind kind, BlockPos from, BlockPos feet, List<BlockPos> clear, List<BlockPos> support) {}

    record Plan(List<Step> steps, BlockPos target) {}

    private static final Map<String, Plan> PLANS = new HashMap<>();
    private static final Map<String, Integer> INDEX = new HashMap<>();
    private static final Map<String, Integer> FAILURES = new HashMap<>();

    static void forget(String missionId) {
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

    /**
     * Colonne d'entree la plus proche de la cible : sommet porteur a hauteur du compagnon (+/-3),
     * avec deux blocs libres au-dessus, et une colonne contigue pour le tunnel de largeur 2.
     */
    private static BlockPos entrance(ServerLevel level, WorkMission mission, BlockPos target, BlockPos start) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = 3; dy >= -3; dy--) {
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
        for (int dx = -margin; dx <= margin; dx++) {
            for (int dz = -margin; dz <= margin; dz++) {
                if (!WorkAccess.contains(mission, feet.offset(dx, 0, dz))) return false;
                if (!WorkAccess.contains(mission, feet.offset(dx, 1, dz))) return false;
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

    private static Step step(ServerLevel level, WorkMission mission, BlockPos from, Direction dir) {
        BlockPos feet = from.offset(dir.getStepX(), -1, dir.getStepZ());
        BlockPos delta = new BlockPos(dir.getStepX(), 0, dir.getStepZ());
        Volume volume = volume(level, mission, feet, delta);
        if (volume == null) return null;
        return new Step(Kind.DESCEND_STEP, from, feet, volume.cells(), volume.support());
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
        String firstReason = null;
        for (BlockPos side : List.of(perp, new BlockPos(-perp.getX(), 0, -perp.getZ()))) {
            Volume volume = variant(level, mission, feet, side);
            if (volume != null) return volume;
            if (firstReason == null) firstReason = lastReason;
        }
        lastReason = "aucun cote praticable : " + firstReason;
        return null;
    }

    private static Volume variant(ServerLevel level, WorkMission mission, BlockPos feet, BlockPos side) {
        List<BlockPos> cells = new ArrayList<>();
        List<BlockPos> support = new ArrayList<>();
        for (int w = 0; w < Math.max(1, WIDTH); w++) {
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
            if (block.isAir()) continue;
            if (!WorkAccess.safe(level, stateOf(level), cell)) {
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
    static boolean drive(ServerLevel level, WorkState state, LivingEntity bot, BlockPos target) {
        WorkMission mission = MissionService.active(state);
        if (mission == null || !(bot instanceof Mob mob)) return false;
        if (mission.accessMined >= WorkAccess.MAX_BLOCKS) {
            WorkService.pause(state, "ACCESS_BUDGET_EXHAUSTED");
            return false;
        }
        String key = mission.id;

        Plan plan = PLANS.get(key);
        if (plan == null) {
            plan = plan(level, state, bot, target);
            if (plan == null || plan.steps().isEmpty()) {
                int failures = FAILURES.merge(key, 1, Integer::sum);
                state.lastStop = "escalier impossible : aucune transition praticable depuis "
                        + bot.blockPosition().toShortString() + " vers " + target.toShortString()
                        + " (tentative " + failures + "/" + MAX_FAILURES + ")";
                System.out.println("[STAIR] echec planification depuis " + bot.blockPosition()
                        + " vers " + target + " (tentative " + failures + ") : " + lastReason);
                return false;
            }
            PLANS.put(key, plan);
            INDEX.put(key, 0);
            System.out.println("[STAIR] plan " + plan.steps().size() + " transitions depuis "
                    + bot.blockPosition() + " vers " + target);
        }

        int index = INDEX.getOrDefault(key, 0);
        while (index < plan.steps().size()) {
            Step step = plan.steps().get(index);
            // 1. Securiser : les appuis reels existent-ils toujours ?
            for (BlockPos support : step.support()) {
                if (!support(level, support)) {
                    state.lastStop = "appui disparu " + support.toShortString() + " : replanification";
                    System.out.println("[STAIR] appui disparu " + support + " (marche " + index + ")");
                    forget(key);
                    return true;
                }
            }
            // 2. Etre au palier superieur avant de creuser une descente (une marche WALK se rejoint
            //    directement : exiger le point de depart ferait revenir le compagnon en arriere).
            if (step.kind() == Kind.DESCEND_STEP && !atFeet(bot, step.from())) {
                approach(level, mob, step.from());
                state.lastStop = "escalier : marche " + (index + 1) + "/" + plan.steps().size()
                        + ", deplacement vers " + step.from().toShortString();
                return true;
            }
            // 3. Creuser les cellules solides, une operation par tick, avec relecture.
            for (BlockPos cell : step.clear()) {
                if (level.getBlockState(cell).isAir()) continue;
                if (!WorkAccess.safe(level, state, cell)) {
                    state.lastStop = "cellule refusee " + cell.toShortString() + " : replanification";
                    System.out.println("[STAIR] cellule refusee " + cell + " "
                            + BuiltInRegistries.BLOCK.getKey(level.getBlockState(cell).getBlock()));
                    forget(key);
                    return true;
                }
                double distance = WorkService.distanceSq(bot, cell);
                if (distance > WorkService.reach() * WorkService.reach()
                        || !WorkService.visible(level, bot, cell)) {
                    approach(level, mob, cell);
                    state.lastStop = "escalier : approche de " + cell.toShortString()
                            + " (marche " + (index + 1) + "/" + plan.steps().size() + ")";
                    return true;
                }
                mob.getNavigation().stop();
                if (state.accessBlock == null || !state.accessBlock.equals(cell)) {
                    state.accessBlock = cell;
                    state.progress = 0;
                }
                state.progress += WorkService.speedMult() / 20.0;
                var blockState = level.getBlockState(cell);
                state.lastStop = "escalier : creuse " + cell.toShortString() + " "
                        + BuiltInRegistries.BLOCK.getKey(blockState.getBlock())
                        + " marche " + (index + 1) + "/" + plan.steps().size();
                if (state.progress >= WorkService.breakSeconds(level, state, cell, blockState)) {
                    System.out.println("[STAIR] marche=" + (index + 1) + "/" + plan.steps().size()
                            + " kind=" + step.kind() + " mine=" + cell.toShortString()
                            + " bloc=" + BuiltInRegistries.BLOCK.getKey(blockState.getBlock())
                            + " from=" + step.from().toShortString()
                            + " feet=" + step.feet().toShortString());
                    WorkService.breakTarget(level, state, bot, cell, true);
                    state.setDirty();
                }
                return true;
            }
            // 4. Degagement complet et appuis verifies : avancer sur le palier.
            BlockPos feet = step.feet();
            double horizontal = Math.hypot(bot.getX() - (feet.getX() + 0.5), bot.getZ() - (feet.getZ() + 0.5));
            // Le niveau doit etre le bon : une tolerance de 2 blocs faisait croire a une marche
            // franchie alors que le compagnon etait encore au-dessus du palier.
            boolean arrived = horizontal <= 1.2 && Math.abs(bot.getY() - feet.getY()) <= 0.6;
            if (!arrived) {
                approach(level, mob, feet);
                state.lastStop = "escalier : passage vers " + feet.toShortString()
                        + " (marche " + (index + 1) + "/" + plan.steps().size() + ")";
                return true;
            }
            index++;
            INDEX.put(key, index);
            state.accessBlock = null;
            state.progress = 0;
            // Une marche franchie est un progres reel : on remet a zero le detecteur de blocage.
            state.movementCells.clear();
            state.idleTicks = 0;
            state.lastStop = "escalier : marche " + index + "/" + plan.steps().size() + " franchie";
            System.out.println("[STAIR] franchi marche " + index + "/" + plan.steps().size()
                    + " feet=" + feet.toShortString());
        }

        System.out.println("[STAIR] escalier termine (" + plan.steps().size() + " transitions) a "
                + bot.blockPosition());
        state.lastStop = "escalier termine : " + plan.steps().size() + " transitions";
        forget(key);
        FAILURES.remove(key);
        return false;
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
        if (dropOne) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
            return;
        }
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
