package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exécuteur de chantier : casse la zone demandée, un bloc à la fois, dans un budget par tick.
 *
 * <p>Trois règles qui ne se négocient pas :</p>
 * <ul>
 *   <li><b>Budget adaptatif</b> — on mesure le temps passé dans notre propre travail et on ajuste
 *       le nombre de blocs par tick pour rester autour de {@link #TARGET_MS}. Un chantier ne doit
 *       jamais pourrir le TPS de la partie.</li>
 *   <li><b>Journal avant destruction</b> — l'état d'origine est écrit dans {@link WorkState} *avant*
 *       de casser le bloc. Si le serveur tombe entre les deux, on a au pire un bloc noté encore
 *       debout, jamais un bloc perdu sans trace.</li>
 *   <li><b>Ce qu'on ne casse pas</b> — incassables, fluides, blocs à entité (coffres, machines) et
 *       zones protégées sont laissés et comptés. On ne détruit pas un atelier ni le coffre de
 *       quelqu'un pour « finir la zone ».</li>
 * </ul>
 *
 * <p>L'ordre de travail est <b>de haut en bas</b> : c'est l'ordre qui gère la gravité (sable,
 * gravier) sans qu'un bloc tombe sur le suivant, et qui évite d'ouvrir une poche de liquide par
 * le dessous.</p>
 */
public final class WorkService {

    /** Budget maximum de blocs par tick. */
    private static final int BUDGET_MAX = 64;
    /** Budget minimum, pour ne pas devenir inutilement lent sur une machine chargée. */
    private static final int BUDGET_MIN = 4;
    /** Temps visé pour notre propre travail, en millisecondes. */
    private static final double TARGET_MS = 5.0;

    private static int budget = 24;
    private static double averageMs = 1.0;

    private WorkService() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(WorkService::endTick);
    }

    public static int budget() {
        return budget;
    }

    public static double averageMs() {
        return averageMs;
    }

    private static void endTick(MinecraftServer server) {
        long start = System.nanoTime();
        ServerLevel level = server.overworld();
        WorkState state = WorkState.get(level);

        int made = 0;
        if (state.hasJob()) {
            while (made < budget) {
                if (!step(level, state)) {
                    break;
                }
                made++;
            }
            if (state.hasJob()) {
                state.setDirty();
            }
        }

        double ms = (System.nanoTime() - start) / 1_000_000.0;
        averageMs = averageMs * 0.8 + ms * 0.2;
        if (averageMs > TARGET_MS && budget > BUDGET_MIN) {
            budget--;
        } else if (averageMs < TARGET_MS * 0.5 && budget < BUDGET_MAX) {
            budget++;
        }
    }

    /**
     * Traite une position du curseur puis avance.
     *
     * @return {@code false} quand le chantier est terminé (ou plus de chantier).
     */
    private static boolean step(ServerLevel level, WorkState state) {
        if (!state.hasJob()) {
            return false;
        }
        BlockPos pos = new BlockPos(state.cursorX, state.cursorY, state.cursorZ);
        examine(level, state, pos);
        if (!advance(state)) {
            state.finished = true;
            state.lastStop = "termine: " + state.done + " casses, " + state.skipped + " laisses";
            state.setDirty();
            return false;
        }
        return true;
    }

    /** Décide du sort d'un bloc : casser, laisser, ou ignorer. */
    private static void examine(ServerLevel level, WorkState state, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir()) {
            return;
        }
        // 1. incassable (bedrock, barrier, portail…)
        if (blockState.getDestroySpeed(level, pos) < 0) {
            state.skipped++;
            return;
        }
        // 2. fluide : on ne l'ouvre pas, on le laisse et on le signale
        if (!blockState.getFluidState().isEmpty()) {
            state.skipped++;
            return;
        }
        // 3. bloc à entité : coffre, machine du mod, fourneau — jamais à l'aveugle
        if (blockState.hasBlockEntity()) {
            state.skipped++;
            return;
        }
        // 4. zone revendiquée
        if (ClaimIntegration.isClaimProtected(level, pos)) {
            state.skipped++;
            return;
        }
        // 5. liquide au contact : casser ici inonderait la fouille
        if (touchesFluid(level, pos)) {
            state.skipped++;
            return;
        }
        // journal AVANT destruction
        CompoundTag saved = NbtUtils.writeBlockState(blockState);
        state.record(pos, saved, level.getGameTime());
        level.destroyBlock(pos, true, null, 512);
        state.done++;
    }

    private static boolean touchesFluid(ServerLevel level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos near = pos.relative(d);
            BlockState s = level.getBlockState(near);
            if (!s.getFluidState().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Avance le curseur : X, puis Z, puis Y de haut en bas. */
    private static boolean advance(WorkState state) {
        state.cursorX++;
        if (state.cursorX > state.maxX) {
            state.cursorX = state.minX;
            state.cursorZ++;
            if (state.cursorZ > state.maxZ) {
                state.cursorZ = state.minZ;
                state.cursorY--;
            }
        }
        return state.cursorY >= state.minY;
    }

    /** Démarre un chantier (remplace celui en cours). */
    public static WorkState start(ServerLevel level, String name, BlockPos a, BlockPos b) {
        WorkState state = WorkState.get(level);
        state.jobName = name;
        state.minX = Math.min(a.getX(), b.getX());
        state.minY = Math.min(a.getY(), b.getY());
        state.minZ = Math.min(a.getZ(), b.getZ());
        state.maxX = Math.max(a.getX(), b.getX());
        state.maxY = Math.max(a.getY(), b.getY());
        state.maxZ = Math.max(a.getZ(), b.getZ());
        state.cursorX = state.minX;
        state.cursorY = state.maxY;
        state.cursorZ = state.minZ;
        long volume = (long) (state.maxX - state.minX + 1) * (state.maxY - state.minY + 1)
                * (state.maxZ - state.minZ + 1);
        state.total = (int) Math.min(Integer.MAX_VALUE, volume);
        state.done = 0;
        state.skipped = 0;
        state.finished = false;
        state.lastStop = "";
        state.setDirty();
        return state;
    }

    /** Arrête le chantier en cours sans perdre le journal. */
    public static boolean stop(ServerLevel level) {
        WorkState state = WorkState.get(level);
        if (!state.hasJob()) {
            return false;
        }
        state.lastStop = "arrete: " + state.done + " casses, " + state.skipped + " laisses";
        state.finished = true;
        state.jobName = null;
        state.setDirty();
        return true;
    }

    /** Nombre de positions restantes (approximation par le curseur). */
    public static long remaining(WorkState state) {
        if (!state.hasJob()) {
            return 0;
        }
        long width = state.maxX - state.minX + 1;
        long depth = state.maxZ - state.minZ + 1;
        long layers = state.cursorY - state.minY;
        return layers * width * depth
                + (long) (state.cursorZ - state.minZ) * width
                + (state.cursorX - state.minX);
    }
}
