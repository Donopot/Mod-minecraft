package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import java.util.*;

/**
 * Opt-in excavation.
 *
 * <p>Deux regimes, separes :
 * <ol>
 *   <li><b>marche a niveau</b> — {@link #route} cherche un chemin horizontal sans creuser ;</li>
 *   <li><b>descente</b> — {@link StairMining} construit et execute un escalier par transitions
 *       atomiques (modele 1x2 configurable, support valide sous l'empreinte de collision).</li>
 * </ol>
 *
 * <p>Cette classe ne garde que le regime horizontal, les garde-fous de securite ({@link #safe}) et
 * l'aiguillage vers l'escalier. L'ancien planificateur de paliers 3x3x3 a ete retire : il exigeait
 * une surface porteuse 3x3 a chaque extremite et refusait donc toute pente (ACCESS_NO_SAFE_ROUTE).
 */
public final class WorkAccess {
    public static final int MAX_BLOCKS = 1024, SEARCH_NODES = 1024;

    // Blocs meubles excavables (surface et sous-sol). La vegetation est traitee a part dans safe().
    private static final Set<String> ROCK = Set.of("minecraft:stone", "minecraft:deepslate", "minecraft:dirt",
            "minecraft:coarse_dirt", "minecraft:rooted_dirt", "minecraft:grass_block", "minecraft:podzol",
            "minecraft:mycelium", "minecraft:dirt_path", "minecraft:granite", "minecraft:andesite",
            "minecraft:diorite", "minecraft:tuff", "minecraft:calcite", "minecraft:gravel", "minecraft:sand",
            "minecraft:red_sand", "minecraft:sandstone", "minecraft:clay");

    private WorkAccess() {}

    static boolean enabled(WorkState s) {
        WorkMission m = MissionService.active(s);
        return m != null && m.build == null && m.access.equals("excavate");
    }

    static boolean contains(WorkMission m, BlockPos p) {
        return p.getX() >= m.min.getX() && p.getX() <= m.max.getX()
                && p.getY() >= m.min.getY() && p.getY() <= m.max.getY()
                && p.getZ() >= m.min.getZ() && p.getZ() <= m.max.getZ();
    }

    /** Seule porte d'entree d'une mutation d'excavation : zone, claim, fluides, entites, chute. */
    static boolean safe(ServerLevel level, WorkState s, BlockPos p) {
        return safe(level, s, p, Integer.MIN_VALUE);
    }

    /**
     * Variante pour un couloir d'acces : la zone miniere borne la profondeur (Y minimum) et
     * l'emprise horizontale, mais le tunnel peut traverser les couches situees entre la surface
     * et le plafond de la zone. {@code accessTop} est la hauteur de depart du compagnon.
     */
    static boolean safe(ServerLevel level, WorkState s, BlockPos p, int accessTop) {
        WorkMission m = MissionService.active(s);
        boolean inZone = m != null && (contains(m, p) || (columnOf(m, p) && p.getY() >= m.min.getY() && p.getY() <= accessTop));
        if (m == null || !inZone || level.isOutsideBuildHeight(p) || !level.getWorldBorder().isWithinBounds(p)
                || !WorkService.loadedNeighborhood(level, p) || ClaimIntegration.isClaimProtected(level, p)) return false;
        var block = level.getBlockState(p);
        // La vegetation (herbes, buissons, fleurs, pousses) et le bois (troncs, feuilles, racines)
        // se traversent : un couloir d'acces ne doit pas etre bloque par un arbre.
        boolean vegetation = block.getBlock() instanceof net.minecraft.world.level.block.BushBlock
                || block.is(net.minecraft.tags.BlockTags.LOGS)
                || block.is(net.minecraft.tags.BlockTags.LEAVES);
        if (!vegetation && !ROCK.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString())
                && !MissionService.matches(m, block)) return false;
        if (block.hasBlockEntity() || !block.getFluidState().isEmpty() || WorkService.touchesFluid(level, p)) return false;
        // Ne jamais ouvrir un plafond qui tombe ni saper un conteneur attache.
        var above = level.getBlockState(p.above());
        return !(above.getBlock() instanceof net.minecraft.world.level.block.FallingBlock) && !above.hasBlockEntity();
    }

    /** Diagnostic : premiere regle qui refuse un bloc d'excavation (jamais utilise pour decider). */
    static String why(ServerLevel level, WorkState s, BlockPos p) {
        WorkMission m = MissionService.active(s);
        if (m == null) return "aucune mission active";
        if (!contains(m, p)) return "hors zone";
        if (level.isOutsideBuildHeight(p)) return "hors hauteur";
        if (!level.getWorldBorder().isWithinBounds(p)) return "hors bordure";
        if (!WorkService.loadedNeighborhood(level, p)) return "chunks voisins decharges";
        if (ClaimIntegration.isClaimProtected(level, p)) return "claim";
        var block = level.getBlockState(p);
        boolean vegetation = block.getBlock() instanceof net.minecraft.world.level.block.BushBlock
                || block.is(net.minecraft.tags.BlockTags.LOGS) || block.is(net.minecraft.tags.BlockTags.LEAVES);
        if (!vegetation && !ROCK.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString())
                && !MissionService.matches(m, block)) return "bloc interdit " + BuiltInRegistries.BLOCK.getKey(block.getBlock());
        if (block.hasBlockEntity()) return "bloc a entite";
        if (!block.getFluidState().isEmpty()) return "fluide dans le bloc";
        if (WorkService.touchesFluid(level, p)) return "fluide voisin";
        var above = level.getBlockState(p.above());
        if (above.getBlock() instanceof net.minecraft.world.level.block.FallingBlock) return "plafond qui tombe";
        if (above.hasBlockEntity()) return "conteneur au-dessus";
        return "ok";
    }

    /** Degagement d'un palier de marche a niveau (couronne 3x3, utilise seulement pour le chemin). */
    /** Meme colonne (X/Z) que la zone, sans contrainte de hauteur. */
    static boolean columnOf(WorkMission m, BlockPos p) {
        return p.getX() >= m.min.getX() && p.getX() <= m.max.getX()
                && p.getZ() >= m.min.getZ() && p.getZ() <= m.max.getZ();
    }

    static List<BlockPos> clearance(BlockPos feet) {
        List<BlockPos> cells = new ArrayList<>();
        for (int y = 1; y >= 0; y--) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) cells.add(feet.offset(x, y, z));
        return cells;
    }

    private static boolean passable(ServerLevel level, WorkState s, BlockPos feet) {
        if (level.isOutsideBuildHeight(feet.above()) || !level.getWorldBorder().isWithinBounds(feet)) return false;
        for (BlockPos cell : clearance(feet)) {
            if (!WorkService.loadedNeighborhood(level, cell) || ClaimIntegration.isClaimProtected(level, cell)) return false;
            var block = level.getBlockState(cell);
            if (!block.isAir() && !safe(level, s, cell)) return false;
            if (WorkService.touchesFluid(level, cell)) return false;
        }
        // Un seul bloc porteur suffit pour la marche a niveau.
        return StairMining.support(level, feet.below());
    }

    /** Chemin horizontal sans excavation, borne en rayon autour du depart. */
    static List<BlockPos> route(ServerLevel level, WorkState s, BlockPos start, BlockPos target) {
        if (target.getY() < start.getY() || target.getY() > start.getY() + 1) return List.of();
        Map<BlockPos, BlockPos> parents = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        parents.put(start, start);
        queue.add(start);
        while (!queue.isEmpty() && parents.size() <= SEARCH_NODES) {
            BlockPos cell = queue.removeFirst();
            if (!cell.equals(start) && clearance(cell).contains(target)) {
                List<BlockPos> path = new ArrayList<>();
                while (!cell.equals(start)) { path.add(cell); cell = parents.get(cell); }
                Collections.reverse(path);
                return path;
            }
            List<BlockPos> neighbors = new ArrayList<>();
            for (Direction d : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) neighbors.add(cell.relative(d));
            neighbors.sort(Comparator.comparingDouble(p -> p.distSqr(target)));
            for (BlockPos next : neighbors) {
                if (parents.containsKey(next) || Math.abs(next.getX() - start.getX()) > 24 || Math.abs(next.getZ() - start.getZ()) > 24) continue;
                if (!passable(level, s, next)) continue;
                parents.put(next, cell);
                queue.addLast(next);
            }
        }
        return List.of();
    }

    private static final Map<String, Integer> FLOOR_RETRIES = new HashMap<>();
    private static final Map<String, Integer> REACH_RETRIES = new HashMap<>();

    /**
     * Quand le compagnon n'a aucun appui sous lui (berge, bord de falaise), on le deplace vers la
     * cellule porteuse la plus proche. Trois tentatives par mission.
     */
    private static boolean relocateToFloor(ServerLevel level, WorkState s, LivingEntity bot) {
        WorkMission m = MissionService.active(s);
        String key = m == null ? "" : m.id;
        int attempts = FLOOR_RETRIES.getOrDefault(key, 0);
        if (attempts >= 3) return false;
        if (!(bot instanceof Mob mob)) return false;
        BlockPos start = bot.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -6; dx <= 6; dx++) for (int dz = -6; dz <= 6; dz++) for (int dy = -2; dy <= 1; dy++) {
            BlockPos candidate = start.offset(dx, dy, dz);
            if (!level.hasChunkAt(candidate) || !StairMining.support(level, candidate.below())) continue;
            if (!clearance(candidate).stream().allMatch(p -> level.getBlockState(p).isAir())) continue;
            double distance = candidate.distSqr(start);
            if (distance < bestDistance) { bestDistance = distance; best = candidate; }
        }
        if (best == null) return false;
        var path = mob.getNavigation().createPath(best, 0);
        if (path == null || !path.canReach()) return false;
        mob.getNavigation().moveTo(path, 1.0);
        FLOOR_RETRIES.put(key, attempts + 1);
        s.lastStop = "deplacement vers un appui porteur " + best.toShortString() + " (tentative " + (attempts + 1) + "/3)";
        return true;
    }

    static void tick(ServerLevel level, WorkState s, LivingEntity bot, BlockPos target) {
        if (!(bot instanceof Mob mob)) return;
        WorkMission m = MissionService.active(s);

        if (s.accessRoute.isEmpty()) {
            if (!StairMining.support(level, bot.blockPosition().below()) && relocateToFloor(level, s, bot)) return;
            s.accessRoute.addAll(route(level, s, bot.blockPosition(), target));
            if (s.accessRoute.isEmpty()) {
                // Aucune marche a niveau possible : on passe au modele d'escalier.
                if (StairMining.drive(level, s, bot, target)) return;
                BlockPos start = bot.blockPosition();
                s.lastStop = "ACCESS_NO_SAFE_ROUTE: start=" + start.toShortString()
                        + " target=" + target.toShortString()
                        + " support=" + StairMining.support(level, start.below())
                        + " rise=" + (start.getY() - target.getY())
                        + " mined=" + (m == null ? 0 : m.accessMined);
                WorkService.pause(s, "ACCESS_NO_SAFE_ROUTE");
                return;
            }
            s.nextPathTick = 0;
        }

        BlockPos next = s.accessRoute.getFirst();
        if (!passable(level, s, next)) {
            s.accessRoute.clear();
            WorkService.pause(s, "ACCESS_TERRAIN_CHANGED");
            return;
        }
        List<BlockPos> cells = clearance(next);
        for (BlockPos p : cells) {
            if (level.getBlockState(p).isAir()) continue;
            if (m != null && m.accessMined >= MAX_BLOCKS) { WorkService.pause(s, "ACCESS_BUDGET_EXHAUSTED"); return; }
            if (WorkService.distanceSq(bot, p) > WorkService.reach() * WorkService.reach() || !WorkService.visible(level, bot, p)) continue;
            mob.getNavigation().stop();
            if (!p.equals(s.accessBlock)) { s.accessBlock = p; s.progress = 0; }
            s.progress += WorkService.speedMult() / 20.0;
            if (s.progress >= WorkService.breakSeconds(level, s, p, level.getBlockState(p))) WorkService.breakTarget(level, s, bot, p, true);
            return;
        }
        boolean clear = cells.stream().allMatch(p -> level.getBlockState(p).isAir());
        if (!clear) {
            // Bloc hors de portee : on s'approche au lieu d'abandonner (3 tentatives par mission).
            BlockPos stuck = cells.stream().filter(p -> !level.getBlockState(p).isAir()).findFirst().orElse(next);
            String key = m == null ? "" : m.id;
            int tries = REACH_RETRIES.getOrDefault(key, 0);
            if (tries < 3) {
                REACH_RETRIES.put(key, tries + 1);
                var path = mob.getNavigation().createPath(stuck, 1);
                if (path != null && path.canReach()) mob.getNavigation().moveTo(path, 1.0);
                s.lastStop = "approche du bloc hors portee " + stuck.toShortString() + " (tentative " + (tries + 1) + "/3)";
                return;
            }
            WorkService.pause(s, "ACCESS_OUT_OF_REACH");
            return;
        }
        // Vanilla centre les mobs larges sur un noeud multi-cellules, pas a bloc + 0.5.
        double offset = Math.floor(bot.getBbWidth() + 1.0) * .5;
        if (bot.distanceToSqr(next.getX() + offset, next.getY(), next.getZ() + offset) < .36 && bot.onGround()) {
            s.accessRoute.removeFirst();
            s.accessBlock = null;
            s.nextPathTick = 0;
            return;
        }
        var centre = new net.minecraft.world.phys.Vec3(next.getX() + offset, next.getY(), next.getZ() + offset);
        if (bot.onGround() && bot.blockPosition().getY() == next.getY() && bot.position().distanceToSqr(centre) < 2.25
                && level.noCollision(bot, bot.getBoundingBox().move(centre.subtract(bot.position())))) {
            mob.getMoveControl().setWantedPosition(centre.x, centre.y, centre.z, 1.0);
            return;
        }
        if (bot.onGround() && level.getGameTime() >= s.nextPathTick) {
            s.nextPathTick = level.getGameTime() + 10;
            var path = mob.getNavigation().createPath(next, 0);
            if (path == null || !path.canReach() || !mob.getNavigation().moveTo(path, 1.0)) {
                s.accessRoute.clear();
                s.lastStop = "ACCESS_PATH_CHANGED: " + bot.position() + " -> " + next.toShortString();
                WorkService.pause(s, "ACCESS_PATH_CHANGED");
            }
        }
    }
}
