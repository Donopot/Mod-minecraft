package com.donopot.voiddynasty.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Les tâches concrètes du Servitor.
 *
 * <p>Chacune est une petite machine à états qui avance d'un pas par tick. Aucune ne touche au
 * modèle : elles reçoivent un but déjà validé et s'y tiennent. Toutes savent échouer proprement
 * (cible disparue, chemin impossible, délai dépassé) pour que le cerveau puisse réessayer
 * autrement au lieu de tourner en boucle.</p>
 */
public final class BrainTasks {

    private BrainTasks() {}

    /** Vitesse de marche du servitor (blocs/tick ~ 0.23 d'attribut de base). */
    private static final double SPEED = 1.0;
    private static final double REACH = 4.5;

    // --------------------------------------------------------------- creuser pour se déplacer

    /**
     * Degage un couloir de deux blocs de haut (pieds + tete) a cet endroit.
     * Renvoie false si un bloc indestructible bloque le passage.
     */
    private static boolean open(ServerLevel level, BlockPos foot) {
        return clear(level, foot) && clear(level, foot.above());
    }

    /** Brise un bloc s'il est destructible ; true si l'endroit est libre. */
    private static boolean clear(ServerLevel level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (st.isAir() || !st.getFluidState().isEmpty()) return true;
        if (st.getDestroySpeed(level, pos) < 0) return false;
        level.destroyBlock(pos, false, null, 512);
        return level.getBlockState(pos).isAir();
    }

    /** Cout pour creuser un bloc : 1000 si indestructible, 0 si deja vide. */
    private static double hardness(ServerLevel level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (st.isAir()) return 0;
        float s = st.getDestroySpeed(level, pos);
        return s < 0 ? 1000 : s + 0.1;
    }

    /** Direction horizontale (x, z) qui va vers un point. */
    private static int[] toward(LivingEntity bot, double x, double z) {
        double dx = x - bot.getX(), dz = z - bot.getZ();
        if (Math.abs(dx) > Math.abs(dz)) return new int[]{dx > 0 ? 1 : -1, 0};
        return new int[]{0, dz > 0 ? 1 : -1};
    }

    /**
     * Escalier de secours : une marche d'un bloc par pas, pour remonter d'un trou qu'on vient
     * de creuser. Le servitor ne pose pas de blocs, il creuse donc vers le haut.
     */
    private static final class PitEscape {
        private int steps;
        private int retries;
        private int[] dir;

        /**
         * Vrai quand le servitor est revenu au niveau du sol (ou a renonce). On compare la
         * hauteur : « voir le ciel » ne suffit pas, un trou a ciel ouvert le voit aussi et la
         * remontee ne se declenchait jamais.
         */
        boolean out(BrainTask.Ctx ctx, int startY) {
            return !ctx.hasBot() || ctx.bot().getBlockY() >= startY;
        }

        /**
         * @return true si la remontee est terminee. Principe de l'escalier : on creuse deux blocs
         * au-dessus du sol de la paroi, puis on se hisse sur la marche. Sans hissage, le pathfinder
         * d'un mob refuse de grimper un bloc depuis le fond d'un trou et le servitor reste bloque.
         */
        boolean tick(BrainTask.Ctx ctx, int startY) {
            Mob m = ctx.mob();
            if (m == null || out(ctx, startY)) return true;
            if (++steps > 200) return true; // trop profond ou bloque : on rend la main

            BlockPos here = ctx.bot().blockPosition();
            if (dir == null) {
                // On demande d'abord aux sens quelle direction sort le plus vite a l'air libre.
                dir = BrainSenses.thinnestRoof(ctx.level(), here, 32);
                if (dir == null) dir = wallDirection(ctx, here);
                if (dir == null) return true; // aucune paroi a creuser
            }
            BlockPos wall = wallAhead(ctx, here, dir);
            if (wall == null) {
                dir = null;
                if (++retries > 8) return true;
                return false;
            }
            BlockPos step = wall.above();
            if (!clear(ctx.level(), step) || !clear(ctx.level(), step.above())) {
                dir = null; // paroi indestructible de ce cote
                if (++retries > 8) return true;
                return false;
            }
            // Une marche : on monte d'un bloc, comme un joueur le ferait en sautant.
            ctx.bot().teleportTo(step.getX() + 0.5, step.getY(), step.getZ() + 0.5);
            retries = 0;
            return false;
        }

        /** Premiere paroi solide devant nous, a hauteur des pieds, dans les 5 blocs. */
        private BlockPos wallAhead(BrainTask.Ctx ctx, BlockPos here, int[] d) {
            for (int i = 1; i <= 5; i++) {
                BlockPos p = here.offset(d[0] * i, 0, d[1] * i);
                if (hardness(ctx.level(), p) > 0 && hardness(ctx.level(), p) < 1000) return p;
            }
            return null;
        }

        /** Direction ou se trouve une paroi proche (le trou a forcement des bords). */
        private int[] wallDirection(BrainTask.Ctx ctx, BlockPos here) {
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : dirs) {
                BlockPos p = wallAhead(ctx, here, d);
                if (p != null && here.distSqr(p) <= 25) return d;
            }
            return null;
        }


    }

    // --------------------------------------------------------------- déplacement

    public static final class Goto implements BrainTask {
        private final BlockPos target;
        private final int timeoutTicks;
        private long startTick = -1;
        private double best = Double.MAX_VALUE;
        private int stuckTicks = 0;
        private int dug = 0;
        private long lastCheckTick = -1;
        private double lastCheckDist = Double.MAX_VALUE;

        public Goto(BlockPos target, int timeoutSeconds) {
            this.target = target;
            this.timeoutTicks = Math.max(20, timeoutSeconds * 20);
        }

        @Override
        public String describe() {
            return "aller en " + target.toShortString()
                    + (dug == 0 ? "" : " (" + dug + " bloc(s) creuses pour passer)");
        }

        @Override
        public void start(Ctx ctx) {
            startTick = ctx.gameTime();
            Mob m = ctx.mob();
            if (m != null) m.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, SPEED);
        }

        @Override
        public Status tick(Ctx ctx) {
            if (!ctx.hasBot()) return Status.FAILED;
            if (ctx.bot().blockPosition().distSqr(target) <= 6.25) return Status.DONE;
            if (ctx.gameTime() - startTick > timeoutTicks) return Status.FAILED;

            double d = ctx.distTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            // Fenetre de 30 ticks : si on n'a pas gagne un bloc, on considere le chemin bloque.
            if (lastCheckTick < 0) {
                lastCheckTick = ctx.gameTime();
                lastCheckDist = d;
            } else if (ctx.gameTime() - lastCheckTick >= 30) {
                boolean blocked = d > lastCheckDist - 1.0;
                lastCheckTick = ctx.gameTime();
                lastCheckDist = d;
                if (blocked) stuckTicks += 30;
                else stuckTicks = 0;
            }
            if (d < best - 0.05) best = d;
            // Bloque (mur entre nous et la cible) : on creuse un passage au lieu d'abandonner.
            if (stuckTicks > 60 && dug < 48) {
                int[] dir = toward(ctx.bot(), target.getX() + 0.5, target.getZ() + 0.5);
                BlockPos foot = ctx.bot().blockPosition().offset(dir[0], 0, dir[1]);
                if (open(ctx.level(), foot)) {
                    dug++;
                    stuckTicks = 0;
                    lastCheckDist = Double.MAX_VALUE;
                    Mob mm = ctx.mob();
                    if (mm != null) mm.getNavigation().moveTo(foot.getX() + 0.5, foot.getY(), foot.getZ() + 0.5, SPEED);
                    return Status.RUNNING;
                }
            }
            if (stuckTicks > 300 && dug >= 48) return Status.FAILED;
            Mob m = ctx.mob();
            if (m != null && m.getNavigation().isDone()) {
                m.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, SPEED);
            }
            return Status.RUNNING;
        }

        @Override
        public BlockPos target() {
            return target;
        }
    }

    public static final class Follow implements BrainTask {
        private final String playerName;
        private final double stopDistance;
        private int repathTicks;

        public Follow(String playerName, double stopDistance) {
            this.playerName = playerName;
            this.stopDistance = stopDistance;
        }

        @Override
        public String describe() {
            return "suivre " + playerName;
        }

        @Override
        public boolean longRunning() {
            return true;
        }

        @Override
        public Status tick(Ctx ctx) {
            ServerPlayer p = BrainTools.playerByName(ctx.level(), playerName);
            if (p == null) return Status.FAILED;
            if (p.level() != ctx.level()) return Status.FAILED;
            Mob m = ctx.mob();
            if (m == null) return Status.FAILED;

            double d = ctx.bot().distanceTo(p);
            if (d <= stopDistance) {
                m.getNavigation().stop();
                m.getLookControl().setLookAt(p, 30f, 30f);
                return Status.RUNNING;
            }
            if (--repathTicks <= 0) {
                repathTicks = 15;
                m.getNavigation().moveTo(p, SPEED * 1.15);
            }
            return Status.RUNNING;
        }
    }

    public static final class ComeTo implements BrainTask {
        private final String playerName;
        private long startTick = -1;
        private final int timeoutTicks = 1200;
        private double best = Double.MAX_VALUE;
        private int stuckTicks = 0;
        private int dug = 0;

        public ComeTo(String playerName) {
            this.playerName = playerName;
        }

        @Override
        public String describe() {
            return "rejoindre " + playerName
                    + (dug == 0 ? "" : " (" + dug + " bloc(s) creuses pour passer)");
        }

        @Override
        public Status tick(Ctx ctx) {
            if (startTick < 0) startTick = ctx.gameTime();
            if (ctx.gameTime() - startTick > timeoutTicks) return Status.FAILED;
            ServerPlayer p = BrainTools.playerByName(ctx.level(), playerName);
            if (p == null || p.level() != ctx.level()) return Status.FAILED;
            Mob m = ctx.mob();
            if (m == null) return Status.FAILED;
            if (ctx.bot().distanceTo(p) <= 3.0) {
                m.getNavigation().stop();
                return Status.DONE;
            }
            double d = ctx.bot().distanceTo(p);
            if (d > best - 0.05) {
                stuckTicks++;
                // Un mur entre le servitor et le joueur : il creuse un passage.
                if (stuckTicks > 40 && dug < 48) {
                    int[] dir = toward(ctx.bot(), p.getX(), p.getZ());
                    BlockPos foot = ctx.bot().blockPosition().offset(dir[0], 0, dir[1]);
                    if (open(ctx.level(), foot)) {
                        dug++;
                        stuckTicks = 0;
                        best = Double.MAX_VALUE;
                        m.getNavigation().moveTo(foot.getX() + 0.5, foot.getY(), foot.getZ() + 0.5, SPEED);
                        return Status.RUNNING;
                    }
                }
                if (stuckTicks > 160) return Status.FAILED;
            } else {
                best = d;
                stuckTicks = 0;
            }
            if (m.getNavigation().isDone()) m.getNavigation().moveTo(p, SPEED * 1.1);
            return Status.RUNNING;
        }
    }

    // ------------------------------------------------------------------ minage

    public static final class MineArea implements BrainTask {
        private final BlockPos min;
        private final BlockPos max;
        private final int total;
        private int mined;
        private long startTick = -1;
        private BlockPos current;
        private int cooldown;
        private int noProgress;
        /** Blocs qu'on n'arrive pas a atteindre : sans cela le plus proche revient en boucle. */
        private final Set<BlockPos> unreachable = new HashSet<>();
        /** Niveau du sol avant de creuser : on y remonte avant de rendre la main. */
        private int startY = Integer.MIN_VALUE;
        private boolean escaped;
        private PitEscape escape;

        public MineArea(BlockPos a, BlockPos b) {
            this.min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
            this.max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
            this.total = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        }

        @Override
        public String describe() {
            return "miner " + min.toShortString() + " → " + max.toShortString() + " (" + mined + "/" + total + ")"
                    + (unreachable.isEmpty() ? "" : " — " + unreachable.size() + " bloc(s) inaccessibles")
                    + (escape != null && !escaped ? " — remonte l'escalier" : "");
        }

        @Override
        public int progress() {
            return total == 0 ? 100 : (int) ((long) mined * 100 / total);
        }

        @Override
        public BlockPos target() {
            return current;
        }

        @Override
        public void start(Ctx ctx) {
            if (ctx.hasBot()) startY = ctx.bot().getBlockY();
        }

        /**
         * Fin de minage : on ne laisse pas le servitor au fond du trou. Il creuse un escalier
         * d'une marche par bloc jusqu'a revenir au niveau du sol.
         */
        private Status finishOrClimb(Ctx ctx, Status then) {
            if (startY == Integer.MIN_VALUE) startY = ctx.hasBot() ? ctx.bot().getBlockY() : 0;
            if (escaped) return then;
            if (escape == null) escape = new PitEscape();
            if (escape.tick(ctx, startY)) {
                escaped = true;
                return then;
            }
            return Status.RUNNING;
        }

        @Override
        public Status tick(Ctx ctx) {
            if (!ctx.hasBot()) return Status.FAILED;
            if (startTick < 0) startTick = ctx.gameTime();
            if (ctx.gameTime() - startTick > Math.max(600, total * 12L)) {
                return finishOrClimb(ctx, Status.FAILED);
            }
            if (cooldown > 0) {
                cooldown--;
                return Status.RUNNING;
            }

            if (current == null || isGone(ctx, current)) {
                current = nextBlock(ctx);
                if (current == null) return finishOrClimb(ctx, Status.DONE);
                noProgress = 0;
            }
            if (ctx.distTo(current.getX() + 0.5, current.getY() + 0.5, current.getZ() + 0.5) <= REACH) {
                ctx.level().destroyBlock(current, true, ctx.bot(), 64);
                mined++;
                current = null;
                cooldown = 3;
                noProgress = 0;
                return Status.RUNNING;
            }
            Mob m = ctx.mob();
            if (m != null && m.getNavigation().isDone()) {
                if (!m.getNavigation().moveTo(current.getX() + 0.5, current.getY(), current.getZ() + 0.5, SPEED)) {
                    // Pas de chemin : on abandonne CE bloc (pas tous les autres) et on en choisit
                    // un autre. Sans cette liste, le plus proche revenait a chaque tick et la
                    // tache echouait au bout de 400 ticks en laissant la moitie du trou.
                    unreachable.add(current);
                    current = null;
                    if (unreachable.size() > total) return Status.FAILED;
                    return Status.RUNNING;
                }
            }
            // Bloque sur un bloc (chemin qui n'aboutit pas, coin coincé) : on le met de cote
            // au lieu d'abandonner tout le trou apres 400 ticks.
            if (++noProgress > 120) {
                unreachable.add(current);
                current = null;
                noProgress = 0;
                if (unreachable.size() >= total) return Status.FAILED;
                return Status.RUNNING;
            }
            return Status.RUNNING;
        }

        private boolean isGone(Ctx ctx, BlockPos pos) {
            BlockState st = ctx.level().getBlockState(pos);
            return st.isAir() || !st.getFluidState().isEmpty();
        }

        private BlockPos nextBlock(Ctx ctx) {
            BlockPos bestPos = null;
            double bestD = Double.MAX_VALUE;
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        BlockPos p = new BlockPos(x, y, z);
                        BlockState st = ctx.level().getBlockState(p);
                        if (st.isAir() || !st.getFluidState().isEmpty()) continue;
                        if (st.getDestroySpeed(ctx.level(), p) < 0) continue; // bedrock et compagnie
                        if (unreachable.contains(p)) continue;
                        // On ne creuse jamais dans la lave ou le feu : ca tuerait le servitor.
                        if (BrainSenses.hazard(ctx.level(), p)) continue;
                        double d = ctx.bot().distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                        if (d < bestD) {
                            bestD = d;
                            bestPos = p;
                        }
                    }
                }
            }
            return bestPos;
        }
    }

    public static final class MineBlocks implements BrainTask {
        private final String blockId;
        private final int wanted;
        private final int radius;
        private int mined;
        private BlockPos current;
        private int cooldown;
        private long startTick = -1;
        private int misses;

        public MineBlocks(String blockId, int wanted, int radius) {
            this.blockId = blockId;
            this.wanted = Math.max(1, wanted);
            this.radius = Math.max(4, Math.min(48, radius));
        }

        @Override
        public String describe() {
            return "miner " + wanted + " " + blockId + " (" + mined + "/" + wanted + ")";
        }

        @Override
        public int progress() {
            return (int) ((long) mined * 100 / wanted);
        }

        @Override
        public BlockPos target() {
            return current;
        }

        @Override
        public Status tick(Ctx ctx) {
            if (!ctx.hasBot()) return Status.FAILED;
            if (startTick < 0) startTick = ctx.gameTime();
            if (mined >= wanted) return Status.DONE;
            if (ctx.gameTime() - startTick > 20L * 60 * 10) return Status.FAILED;
            if (cooldown > 0) {
                cooldown--;
                return Status.RUNNING;
            }
            if (current == null || ctx.level().getBlockState(current).isAir()) {
                current = nearest(ctx);
                if (current == null) {
                    return ++misses > 3 ? Status.FAILED : Status.DONE;
                }
            }
            if (ctx.distTo(current.getX() + 0.5, current.getY() + 0.5, current.getZ() + 0.5) <= REACH) {
                ctx.level().destroyBlock(current, true, ctx.bot(), 64);
                mined++;
                current = null;
                cooldown = 4;
                return Status.RUNNING;
            }
            Mob m = ctx.mob();
            if (m != null && m.getNavigation().isDone()) {
                if (!m.getNavigation().moveTo(current.getX() + 0.5, current.getY(), current.getZ() + 0.5, SPEED)) {
                    current = null;
                    if (++misses > 6) return Status.FAILED;
                }
            }
            return Status.RUNNING;
        }

        private BlockPos nearest(Ctx ctx) {
            BlockPos center = ctx.bot().blockPosition();
            BlockPos bestPos = null;
            double bestD = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -radius / 2, -radius),
                    center.offset(radius, radius / 2, radius))) {
                BlockState st = ctx.level().getBlockState(p);
                if (st.isAir()) continue;
                Identifier id = BuiltInRegistries.BLOCK.getKey(st.getBlock());
                if (!id.toString().equals(blockId) && !id.getPath().equals(blockId)) continue;
                if (st.getDestroySpeed(ctx.level(), p) < 0) continue;
                double d = center.distSqr(p);
                if (d < bestD) {
                    bestD = d;
                    bestPos = p.immutable();
                }
            }
            return bestPos;
        }
    }

    // ------------------------------------------------------------------ combat

    public static final class AttackNearest implements BrainTask {
        private final int radius;
        private final int maxKills;
        private int kills;
        private int cooldown;
        private long startTick = -1;

        public AttackNearest(int radius, int maxKills) {
            this.radius = Math.max(4, Math.min(32, radius));
            this.maxKills = Math.max(1, maxKills);
        }

        @Override
        public String describe() {
            return "combattre les hostiles proches (" + kills + "/" + maxKills + ")";
        }

        @Override
        public Status tick(Ctx ctx) {
            if (!ctx.hasBot()) return Status.FAILED;
            if (startTick < 0) startTick = ctx.gameTime();
            if (ctx.gameTime() - startTick > 20L * 60 * 3) return Status.FAILED;
            if (kills >= maxKills) return Status.DONE;
            if (cooldown > 0) cooldown--;

            Monster target = nearestHostile(ctx);
            if (target == null) return kills > 0 ? Status.DONE : Status.FAILED;
            Mob m = ctx.mob();
            if (m == null) return Status.FAILED;
            m.getLookControl().setLookAt(target, 30f, 30f);
            if (ctx.bot().distanceTo(target) <= REACH && cooldown == 0) {
                m.doHurtTarget(ctx.level(), target);
                cooldown = 20;
                if (!target.isAlive()) kills++;
                return Status.RUNNING;
            }
            if (m.getNavigation().isDone()) m.getNavigation().moveTo(target, SPEED);
            return Status.RUNNING;
        }

        private Monster nearestHostile(Ctx ctx) {
            AABB zone = ctx.bot().getBoundingBox().inflate(radius);
            List<Monster> found = ctx.level().getEntitiesOfClass(Monster.class, zone,
                    e -> e.isAlive() && e != ctx.bot() && e.getType() != ctx.bot().getType()
                            && !(e instanceof com.donopot.voiddynasty.entity.ChaosCultistEntity));
            return found.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(ctx.bot()))).orElse(null);
        }
    }

    // ------------------------------------------------------------------ ramassage

    public static final class Pickup implements BrainTask {
        private final int radius;
        private final int wanted;
        private int taken;
        private long startTick = -1;
        private ItemEntity current;

        public Pickup(int radius, int wanted) {
            this.radius = Math.max(4, Math.min(48, radius));
            this.wanted = Math.max(1, wanted);
        }

        @Override
        public String describe() {
            return "ramasser des objets (" + taken + "/" + wanted + ")";
        }

        @Override
        public Status tick(Ctx ctx) {
            if (!ctx.hasBot()) return Status.FAILED;
            if (startTick < 0) startTick = ctx.gameTime();
            if (ctx.gameTime() - startTick > 20L * 60 * 2) return Status.FAILED;
            if (taken >= wanted) return Status.DONE;

            if (current == null || !current.isAlive()) {
                current = nearestItem(ctx);
                if (current == null) return taken > 0 ? Status.DONE : Status.FAILED;
            }
            if (ctx.bot().distanceTo(current) <= 2.2) {
                if (BrainInv.add(ctx.bot(), current.getItem().copy())) {
                    current.discard();
                    taken++;
                    current = null;
                } else {
                    return Status.FAILED; // sac plein
                }
                return Status.RUNNING;
            }
            Mob m = ctx.mob();
            if (m != null && m.getNavigation().isDone()) m.getNavigation().moveTo(current, SPEED);
            return Status.RUNNING;
        }

        private ItemEntity nearestItem(Ctx ctx) {
            AABB zone = ctx.bot().getBoundingBox().inflate(radius);
            return ctx.level().getEntitiesOfClass(ItemEntity.class, zone, ItemEntity::isAlive).stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(ctx.bot())))
                    .orElse(null);
        }
    }

    // ------------------------------------------------------------------- attente

    public static final class Wait implements BrainTask {
        private final int ticks;
        private long startTick = -1;

        public Wait(int seconds) {
            this.ticks = Math.max(20, seconds * 20);
        }

        @Override
        public String describe() {
            return "attendre " + (ticks / 20) + " s";
        }

        @Override
        public Status tick(Ctx ctx) {
            if (startTick < 0) startTick = ctx.gameTime();
            return ctx.gameTime() - startTick >= ticks ? Status.DONE : Status.RUNNING;
        }
    }

    /** Ramasse discrètement ce qui traîne sous le servitor, quel que soit la tâche en cours. */
    public static void suckNearby(BrainTask.Ctx ctx) {
        if (ctx.bot() == null) return;
        AABB zone = ctx.bot().getBoundingBox().inflate(2.5);
        for (ItemEntity it : ctx.level().getEntitiesOfClass(ItemEntity.class, zone, ItemEntity::isAlive)) {
            if (BrainInv.add(ctx.bot(), it.getItem().copy())) it.discard();
        }
    }

    /** Utilitaire partagé : un bloc est-il minable ? */
    public static boolean breakable(ServerLevel level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        return !st.isAir() && st.getFluidState().isEmpty() && st.getDestroySpeed(level, pos) >= 0;
    }
}
