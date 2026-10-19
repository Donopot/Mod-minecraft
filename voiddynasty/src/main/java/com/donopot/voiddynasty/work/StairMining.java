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
    /** Tentatives d'impulsion par marche (descente dans le trou d'un bloc). */
    private static final Map<String, Integer> DESCEND_TRIES = new HashMap<>();
    private static final Map<String, Long> DESCEND_LAST = new HashMap<>();
    private static final int MAX_DESCEND_TRIES = 8;
    private static final Map<String, Integer> INDEX = new HashMap<>();
    private static final Map<String, Integer> FAILURES = new HashMap<>();

    /** Vrai si un escalier pilote cette mission : le detecteur de stagnation du chantier doit
     *  alors s'effacer, l'escalier ayant ses propres bornes (8 essais, 96 marches). */
    static void forget(String missionId) {
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
    private static long lastTraceTick = 0;

    /**
     * AUDIT alpha.76 : trace brute de l'etat physique du compagnon. Aucune decision ne depend de
     * cette methode ; elle existe pour expliquer une immobilité, pas pour la contourner.
     */
    private static void trace(ServerLevel level, LivingEntity bot, BlockPos target, String phase) {
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
        System.out.println("[STAIR_TRACE] t=" + now + " phase=" + phase
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
            trace(level, bot, plan.steps().get(index).feet(), "step" + index);
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
                    // Un bloc casse par l'escalier est un progres : sans cela le detecteur de
                    // stagnation du chantier met la mission en pause en plein creusement.
                    state.movementCells.clear();
                    state.idleTicks = 0;
                    state.setDirty();
                }
                return true;
            }
            // 3bis. Degagement de l'empreinte REELLE avant toute descente : le plan ne garantit que
            //       des colonnes alignees, l'entite en occupe deux ou trois selon son centrage.
            BlockPos feet = step.feet();
            if (step.kind() == Kind.DESCEND_STEP) {
                for (BlockPos cell : footprintCells(bot, feet)) {
                    if (level.getBlockState(cell).isAir()) continue;
                    if (!WorkAccess.safe(level, state, cell)) {
                        state.lastStop = "empreinte refusee " + cell.toShortString() + " : "
                                + WorkAccess.why(level, state, cell);
                        System.out.println("[STAIR] empreinte refusee " + cell + " : "
                                + WorkAccess.why(level, state, cell));
                        forget(key);
                        return true;
                    }
                    if (WorkService.distanceSq(bot, cell) > WorkService.reach() * WorkService.reach()
                            || !WorkService.visible(level, bot, cell)) continue;
                    mob.getNavigation().stop();
                    if (state.accessBlock == null || !state.accessBlock.equals(cell)) {
                        state.accessBlock = cell;
                        state.progress = 0;
                    }
                    state.progress += WorkService.speedMult() / 20.0;
                    var bs = level.getBlockState(cell);
                    state.lastStop = "escalier : degage l'empreinte " + cell.toShortString() + " "
                            + BuiltInRegistries.BLOCK.getKey(bs.getBlock());
                    if (state.progress >= WorkService.breakSeconds(level, state, cell, bs)) {
                        System.out.println("[STAIR] marche=" + (index + 1) + "/" + plan.steps().size()
                                + " kind=FOOTPRINT mine=" + cell.toShortString()
                                + " bloc=" + BuiltInRegistries.BLOCK.getKey(bs.getBlock()));
                            WorkService.breakTarget(level, state, bot, cell, true);
                            state.movementCells.clear();
                            state.idleTicks = 0;
                            state.setDirty();
                    }
                    return true;
                }
            }
            // 4. Degagement complet et appuis verifies : avancer sur le palier.
            double horizontal = Math.hypot(bot.getX() - (feet.getX() + 0.5), bot.getZ() - (feet.getZ() + 0.5));
            // Le niveau doit etre le bon : une tolerance de 2 blocs faisait croire a une marche
            // franchie alors que le compagnon etait encore au-dessus du palier.
            boolean arrived = horizontal <= 1.2 && Math.abs(bot.getY() - feet.getY()) <= 0.6;
            if (!arrived) {
                // Descente d'un bloc : la navigation vanilla refuse souvent d'entrer dans le puits.
                // On guide le deplacement puis, sans progres, on imprime une impulsion mesuree.
                if (step.kind() == Kind.DESCEND_STEP && feet.getY() < bot.getY() - 0.4
                        && Math.abs(feet.getX() - bot.getX()) <= 2.5 && Math.abs(feet.getZ() - bot.getZ()) <= 2.5
                        && descend(level, mob, step.from(), feet, key)) {
                    state.lastStop = "escalier : descente vers " + feet.toShortString()
                            + " (marche " + (index + 1) + "/" + plan.steps().size()
                            + ", essai " + DESCEND_TRIES.getOrDefault(key, 0) + "/" + MAX_DESCEND_TRIES + ")";
                    return true;
                }
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
            DESCEND_TRIES.remove(key);
            DESCEND_LAST.remove(key);
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

    /**
     * Descente dans le puits d'un bloc. Deux temps : guidage par le MoveControl vers le centre du
     * palier, puis — si le compagnon n'a pas bouge depuis 12 ticks — une impulsion horizontale
     * courte vers le palier et une composante verticale negative. Jamais de teleportation : la
     * gravite et les collisions du jeu restent seules responsables de la chute.
     */
    private static boolean descend(ServerLevel level, Mob mob, BlockPos from, BlockPos feet, String key) {
        int tries = DESCEND_TRIES.getOrDefault(key, 0);
        if (tries >= MAX_DESCEND_TRIES) {
            // Descente impossible sur cette marche : on rend la main pour replanifier depuis
            // l'etat reel (jamais de boucle infinie, jamais de teleportation).
            DESCEND_TRIES.remove(key);
            DESCEND_LAST.remove(key);
            forget(key);
            return false;
        }
        // Un seul arret de navigation au debut de la descente : le refaire a chaque tick annule
        // la progression du MoveControl (le compagnon oscille sur place).
        if (DESCEND_LAST.getOrDefault(key, 0L) == 0L) mob.getNavigation().stop();
        // Le compagnon mesure 1.09 bloc : centre sur le palier, son emprise chevauche encore la
        // dalle de la marche precedente et il ne tombe jamais. On vise donc un point decale de
        // 0.55 bloc au-dela du bord, dans le sens de la descente.
        // Marge calculee sur la largeur reelle : il faut que toute l'emprise quitte la dalle
        // precedente (demi-largeur - 0.5) plus une marge de securite.
        double bias = Math.max(0.18, (mob.getBbWidth() / 2.0 - 0.5) + 0.12);
        double biasX = Integer.compare(feet.getX(), from.getX()) * bias;
        double biasZ = Integer.compare(feet.getZ(), from.getZ()) * bias;
        double targetX = feet.getX() + 0.5 + biasX;
        double targetZ = feet.getZ() + 0.5 + biasZ;
        long now = level.getGameTime();
        long last = DESCEND_LAST.getOrDefault(key, 0L);
        if (last == 0L) { DESCEND_LAST.put(key, now); last = now; }
        if (now - last >= 8L) {
            mob.getMoveControl().setWantedPosition(targetX, feet.getY() + 0.05, targetZ, 1.0);
        }
        if (now - last < 12L) return true;
        double dx = targetX - mob.getX();
        double dz = targetZ - mob.getZ();
        double norm = Math.sqrt(dx * dx + dz * dz);
        if (norm > 0.001) { dx = dx / norm * 0.14; dz = dz / norm * 0.14; } else { dx = 0; dz = 0; }
        mob.setDeltaMovement(dx, -0.30, dz);
        mob.syncVelocity = true;
        DESCEND_TRIES.put(key, tries + 1);
        DESCEND_LAST.put(key, now);
        System.out.println("[STAIR] impulsion descente vers " + feet + " (essai " + (tries + 1) + ")");
        return true;
    }

    /**
     * Cellules reellement occupees par l'empreinte de collision du compagnon a hauteur du palier
     * vise : toutes les colonnes que son AABB recouvre, aux deux niveaux du tunnel. Le plan raisonne
     * en blocs alignes ; l'execution, elle, part de l'empreinte reelle — sinon une colonne solide
     * reste sous l'entite (vColl) ou devant elle (hColl) et rien ne bouge.
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
