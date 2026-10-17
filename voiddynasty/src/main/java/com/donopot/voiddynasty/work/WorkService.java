package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

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
 *   <li><b>Rien ne part au sol</b> — les drops sont calculés puis rangés dans un coffre de dépôt posé
 *       par le chantier. Un objet tombé par terre est un objet perdu.</li>
 * </ul>
 *
 * <p>Ce qu'on ne casse pas : incassables, fluides, conteneurs (coffres, fourneaux, machines) et zones
 * protégées. Les conteneurs sont <b>ouverts et inventoriés</b> — on relève leur contenu sans rien
 * prendre ni casser.</p>
 *
 * <p>L'ordre de travail est <b>de haut en bas</b> : c'est l'ordre qui gère la gravité (sable,
 * gravier) sans qu'un bloc tombe sur le suivant, et qui évite d'ouvrir une poche de liquide par
 * le dessous.</p>
 */
public final class WorkService {

    private static final int BUDGET_MAX = 64;
    private static final int BUDGET_MIN = 4;
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

        if (state.hasJob()) {
            int made = 0;
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

    private static boolean step(ServerLevel level, WorkState state) {
        if (!state.hasJob()) {
            return false;
        }
        BlockPos pos = new BlockPos(state.cursorX, state.cursorY, state.cursorZ);
        examine(level, state, pos);
        if (!advance(state)) {
            state.finished = true;
            state.lastStop = "termine: " + state.done + " casses, " + state.skipped + " laisses, "
                    + state.deposited + " objets en coffre";
            state.setDirty();
            return false;
        }
        return true;
    }

    /** Décide du sort d'un bloc : casser, inventorier, ou laisser. */
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
        // 2. fluide : on ne l'ouvre pas
        if (!blockState.getFluidState().isEmpty()) {
            state.skipped++;
            return;
        }
        // 3. conteneur : on l'ouvre, on relève, on ne prend rien et on ne casse pas
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            state.noteLoot(describe(pos, container));
            state.skipped++;
            return;
        }
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
        // journal AVANT destruction, drops AVANT destruction
        List<ItemStack> drops = BlockBehaviourDrops.of(blockState, level, pos, be);
        if (!drops.isEmpty() && !deposit(level, state, drops)) {
            state.lastStop = "depot plein: " + state.done + " casses";
            state.finished = true;
            state.jobName = null;
            state.setDirty();
            return;
        }
        CompoundTag saved = NbtUtils.writeBlockState(blockState);
        state.record(pos, saved, level.getGameTime());
        level.destroyBlock(pos, false, null, 512);
        state.done++;
    }

    /** Petite indirection : garde l'appel aux drops en un seul endroit. */
    private static final class BlockBehaviourDrops {
        static List<ItemStack> of(BlockState state, ServerLevel level, BlockPos pos, BlockEntity be) {
            return net.minecraft.world.level.block.Block.getDrops(state, level, pos, be);
        }
    }

    /** Résume le contenu d'un conteneur, sans y toucher. */
    private static String describe(BlockPos pos, Container container) {
        StringBuilder sb = new StringBuilder("coffre ").append(pos.getX()).append(' ').append(pos.getY())
                .append(' ').append(pos.getZ()).append(" : ");
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                parts.add(stack.getCount() + " " + stack.getHoverName().getString());
            }
        }
        if (parts.isEmpty()) {
            sb.append("vide");
        } else {
            sb.append(String.join(", ", parts));
        }
        return sb.toString();
    }

    // ---------------- dépôt ----------------

    /**
     * Range les drops dans le coffre de dépôt.
     *
     * @return {@code false} si le coffre est plein — le chantier s'arrête alors au lieu de laisser
     *         des objets par terre.
     */
    private static boolean deposit(ServerLevel level, WorkState state, List<ItemStack> drops) {
        Container depot = depotContainer(level, state);
        if (depot == null) {
            return false;
        }
        for (ItemStack stack : drops) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remaining = stack.copy();
            // 1. compléter les piles existantes
            for (int i = 0; i < depot.getContainerSize() && !remaining.isEmpty(); i++) {
                ItemStack slot = depot.getItem(i);
                if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, remaining)) {
                    int space = Math.min(depot.getMaxStackSize(slot) - slot.getCount(), remaining.getCount());
                    if (space > 0) {
                        slot.grow(space);
                        remaining.shrink(space);
                        depot.setItem(i, slot);
                    }
                }
            }
            // 2. emplacements vides
            for (int i = 0; i < depot.getContainerSize() && !remaining.isEmpty(); i++) {
                if (depot.getItem(i).isEmpty()) {
                    int put = Math.min(depot.getMaxStackSize(remaining), remaining.getCount());
                    depot.setItem(i, remaining.copyWithCount(put));
                    remaining.shrink(put);
                }
            }
            if (!remaining.isEmpty()) {
                depot.setChanged();
                return false;
            }
            state.deposited += stack.getCount();
        }
        depot.setChanged();
        return true;
    }

    private static Container depotContainer(ServerLevel level, WorkState state) {
        if (!state.hasDepot) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(new BlockPos(state.depotX, state.depotY, state.depotZ));
        return be instanceof Container container ? container : null;
    }

    /**
     * Pose le coffre de dépôt au-dessus du coin du chantier, hors de la zone à creuser.
     *
     * @return la position du coffre, ou {@code null} si aucun emplacement libre n'a été trouvé.
     */
    private static BlockPos placeDepot(ServerLevel level, WorkState state) {
        BlockPos base = new BlockPos(state.minX, state.maxY + 2, state.minZ);
        for (int dy = 0; dy < 16; dy++) {
            BlockPos candidate = base.above(dy);
            if (level.getBlockState(candidate).isAir()) {
                if (level.setBlock(candidate, Blocks.CHEST.defaultBlockState(), 3)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean touchesFluid(ServerLevel level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockState s = level.getBlockState(pos.relative(d));
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

    /**
     * Démarre un chantier et pose son coffre de dépôt.
     *
     * @return l'état du chantier, ou {@code null} si le dépôt n'a pas pu être posé (on refuse de
     *         travailler sans dépôt : les drops finiraient au sol et seraient perdus).
     */
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
        state.deposited = 0;
        state.lootBlocks = 0;
        state.lootLog.clear();
        state.finished = false;
        state.lastStop = "";

        BlockPos depot = placeDepot(level, state);
        if (depot == null) {
            state.jobName = null;
            state.setDirty();
            return null;
        }
        state.hasDepot = true;
        state.depotX = depot.getX();
        state.depotY = depot.getY();
        state.depotZ = depot.getZ();
        state.setDirty();
        return state;
    }

    public static boolean stop(ServerLevel level) {
        WorkState state = WorkState.get(level);
        if (!state.hasJob()) {
            return false;
        }
        state.lastStop = "arrete: " + state.done + " casses, " + state.skipped + " laisses, "
                + state.deposited + " objets en coffre";
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
