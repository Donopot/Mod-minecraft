package com.donopot.voiddynasty.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
            if (stuckTicks >= 400) return Status.FAILED;
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
                if (stuckTicks > 160) return Status.FAILED;
            } else {
                best = d;
                stuckTicks = 0;
            }
            if (m.getNavigation().isDone()) m.getNavigation().moveTo(p, SPEED * 1.1);
            return Status.RUNNING;
        }
    }

    // Mining and construction are exclusively executed by work/MissionService.

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
                ItemStack portion = current.getItem().copyWithCount(Math.min(wanted - taken, current.getItem().getCount()));
                int moved = portion.getCount();
                if (BrainInv.add(ctx.bot(), portion)) {
                    ItemStack remainder = current.getItem().copy(); remainder.shrink(moved);
                    if (remainder.isEmpty()) current.discard(); else current.setItem(remainder);
                    taken += moved;
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

    /** Utilitaire partagé : un bloc est-il minable ? */
    public static boolean breakable(ServerLevel level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        return !st.isAir() && st.getFluidState().isEmpty() && st.getDestroySpeed(level, pos) >= 0;
    }
}
