package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import com.donopot.voiddynasty.entity.Servitor1Entity;
import com.donopot.voiddynasty.registry.ModEntities;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Le Servitor mine. Pas le serveur à sa place : lui, avec une pioche, à portée de bras.
 *
 * <p>Ce que ça implique, concrètement :</p>
 * <ul>
 *   <li><b>Portée</b> — un bloc à plus de {@link #REACH} blocs n'est pas miné : le Servitor s'en
 *       approche d'abord, de bloc en bloc, en ne marchant que sur des cases libres.</li>
 *   <li><b>Outil</b> — sans pioche, la pierre ne donne <b>rien</b> et prend cinq fois plus de temps
 *       (règle du jeu). L'outil s'use d'un point par bloc et casse ; il en reprend une dans son
 *       inventaire, sinon le chantier s'arrête.</li>
 *   <li><b>Durée</b> — le temps de casse vient de {@code dureté × 1,5 / vitesse de l'outil}, la
 *       formule du jeu, divisée par {@link #SPEED_MULT} : c'est une machine, pas un homme.</li>
 *   <li><b>Inventaire réel</b> — 36 emplacements. Plein, il marche jusqu'au coffre de dépôt et
 *       vide. Un objet qu'il ne peut pas porter n'est pas cassé : on ne perd rien.</li>
 *   <li><b>Dangers subis</b> — il est dans le trou. La lave, l'eau, les chutes et les mobs le
 *       touchent pour de vrai. Sa mort arrête le chantier, journal compris.</li>
 * </ul>
 */
public final class WorkService {

    /** Portée de minage, en blocs (la portée d'un joueur en survie). */
    private static final double REACH = 4.5;
    /** Cadence : une machine travaille quatre fois plus vite qu'un joueur. */
    private static final double SPEED_MULT = 4.0;

    private static double lastMs = 0.0;
    /** Ticks sans un seul bloc casse, pour detecter un Servitor bloque. */
    private static int stallTicks = 0;
    private static int lastDone = -1;

    private WorkService() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(WorkService::endTick);
        // La mort du Servitor arrête le chantier proprement : le journal reste, donc on reprend.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            MinecraftServer server = entity.level().getServer();
            if (server == null) {
                return;
            }
            WorkState state = WorkState.get(server.overworld());
            if (state.hasJob() && !state.servitorUuid.isEmpty()
                    && entity.getUUID().toString().equals(state.servitorUuid)) {
                state.lastStop = "servitor tue (" + source.getLocalizedDeathMessage(entity).getString()
                        + ") apres " + state.done + " blocs — journal conserve, /work resume pour reprendre";
                state.finished = true;
                state.servitorUuid = "";
                state.setDirty();
            }
        });
    }

    public static double lastMs() {
        return lastMs;
    }

    public static double reach() {
        return REACH;
    }

    public static double speedMult() {
        return SPEED_MULT;
    }

    // ---------------------------------------------------------------- boucle

    private static void endTick(MinecraftServer server) {
        long start = System.nanoTime();
        ServerLevel level = server.overworld();
        WorkState state = WorkState.get(level);
        if (state.hasJob()) {
            LivingEntity bot = servitor(level, state);
            if (bot == null) {
                state.lastStop = "servitor introuvable — /work resume";
                state.finished = true;
                state.setDirty();
            } else {
                tickWorker(level, state, bot);
                // Garde-fou : un chantier qui ne casse plus rien depuis 10 s est bloque.
                if (state.done != lastDone) {
                    lastDone = state.done;
                    stallTicks = 0;
                } else if (++stallTicks > 200) {
                    stallTicks = 0;
                    state.lastStop = "servitor bloque apres " + state.done + " blocs"
                            + (state.hasTarget ? " (cible " + state.targetX + " " + state.targetY + " "
                            + state.targetZ + " hors d'atteinte)" : "")
                            + " — /work resume pour relancer";
                    state.finished = true;
                    state.setDirty();
                }
            }
        } else {
            stallTicks = 0;
            lastDone = -1;
        }
        lastMs = lastMs * 0.8 + ((System.nanoTime() - start) / 1_000_000.0) * 0.2;
    }

    /** Une unité de travail par tick. */
    private static void tickWorker(ServerLevel level, WorkState state, LivingEntity bot) {
        // A0. travail fini : on vide le sac au depot avant de conclure
        if (state.finishing) {
            if (stepTowardDepot(level, state, bot)) {
                state.finishing = false;
                conclude(level, state);
            }
            return;
        }
        // A. inventaire plein : on va vider avant de continuer
        if (state.goingToDepot) {
            if (!stepTowardDepot(level, state, bot)) {
                return;             // encore en route
            }
        }

        // B. quelle cible ?
        if (!state.hasTarget) {
            if (!pickTarget(level, state)) {
                if (state.invSlots.isEmpty()) {
                    conclude(level, state);
                } else {
                    state.finishing = true;     // il reste le sac a vider
                    state.setDirty();
                }
                return;
            }
        }
        BlockPos target = new BlockPos(state.targetX, state.targetY, state.targetZ);
        BlockState blockState = level.getBlockState(target);
        if (blockState.isAir()) {
            state.hasTarget = false;
            return;
        }

        // C. à portée ?
        if (distanceSq(bot, target) > REACH * REACH) {
            stepToward(level, bot, target);
            return;
        }

        // D. le temps de casse est-il écoulé ?
        double needed = breakSeconds(level, state, target, blockState);
        state.progress += SPEED_MULT / 20.0;
        if (state.progress < needed) {
            return;
        }

        // E. casser
        breakTarget(level, state, bot, target);
    }

    /** Choisit la prochaine cible en avançant le curseur, en sautant ce qu'on ne touche pas. */
    private static boolean pickTarget(ServerLevel level, WorkState state) {
        while (true) {
            if (state.cursorY < state.minY) {
                return false;
            }
            BlockPos pos = new BlockPos(state.cursorX, state.cursorY, state.cursorZ);
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.isAir() && !skip(level, state, pos, blockState)) {
                state.hasTarget = true;
                state.targetX = pos.getX();
                state.targetY = pos.getY();
                state.targetZ = pos.getZ();
                state.progress = 0.0;
                return true;
            }
            if (!advance(state)) {
                return false;
            }
        }
    }

    /** Doit-on laisser ce bloc tranquille ? Renvoie true si on le saute. */
    private static boolean skip(ServerLevel level, WorkState state, BlockPos pos, BlockState blockState) {
        if (blockState.getDestroySpeed(level, pos) < 0) {
            state.skipped++;
            return true;
        }
        if (!blockState.getFluidState().isEmpty()) {
            state.skipped++;
            return true;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            state.noteLoot(describe(pos, container));
            state.skipped++;
            return true;
        }
        if (blockState.hasBlockEntity()) {
            state.skipped++;
            return true;
        }
        if (ClaimIntegration.isClaimProtected(level, pos)) {
            state.skipped++;
            return true;
        }
        if (touchesFluid(level, pos)) {
            state.skipped++;
            return true;
        }
        return false;
    }

    /** Durée de casse en secondes, formule du jeu ramenée à la cadence machine. */
    private static double breakSeconds(ServerLevel level, WorkState state, BlockPos pos, BlockState blockState) {
        float hardness = blockState.getDestroySpeed(level, pos);
        if (hardness < 0) {
            return Double.MAX_VALUE;
        }
        if (hardness == 0) {
            return 0.0;
        }
        ItemStack tool = toolStack(state);
        float speed = tool.isEmpty() ? 1.0f : tool.getDestroySpeed(blockState);
        boolean correct = !tool.isEmpty() && tool.isCorrectToolForDrops(blockState);
        if (speed > 1.0f) {
            return hardness * 1.5 / speed;
        }
        return hardness * (correct ? 1.5 : 5.0);
    }

    /** Casse la cible : journal d'abord, drops ensuite, usure de l'outil. */
    private static void breakTarget(ServerLevel level, WorkState state, LivingEntity bot, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack tool = toolStack(state);

        // Drops seulement si l'outil en main sait récolter ce bloc (règle du jeu).
        boolean canHarvest = tool.isEmpty()
                ? !blockState.requiresCorrectToolForDrops()
                : tool.isCorrectToolForDrops(blockState);
        List<ItemStack> drops = canHarvest
                ? Block.getDrops(blockState, level, pos, be, bot, tool)
                : List.of();

        // Pas de place ? On ne casse pas : on va vider d'abord.
        if (!drops.isEmpty() && !fits(state, drops)) {
            state.goingToDepot = true;
            state.lastStop = "inventaire plein apres " + state.done + " blocs";
            state.setDirty();
            return;
        }

        CompoundTag saved = NbtUtils.writeBlockState(blockState);
        state.record(pos, saved, level.getGameTime());
        level.destroyBlock(pos, false, null, 512);
        state.done++;
        state.hasTarget = false;
        state.progress = 0.0;

        if (!drops.isEmpty()) {
            invInsert(state, drops);
        }

        // Usure de l'outil.
        if (!state.toolId.isEmpty()) {
            state.toolDamage++;
            state.toolBlocks++;
            ItemStack updated = toolStack(state);
            if (updated.getMaxDamage() > 0 && state.toolDamage >= updated.getMaxDamage()) {
                state.toolId = "";
                state.toolDamage = 0;
                if (!takePickaxe(state)) {
                    state.lastStop = "pioche cassee apres " + state.done + " blocs — /work tool pour en donner une";
                    state.finished = true;
                    state.setDirty();
                }
            }
        }
        bot.setItemSlot(EquipmentSlot.MAINHAND, toolStack(state));
        state.setDirty();
    }

    // ---------------------------------------------------------------- déplacement

    /**
     * Avance d'une case vers la cible — et seulement si ça rapproche.
     *
     * <p>Le piège que ça évite : prendre la première case libre venue fait grimper le Servitor
     * indéfiniment quand la cible est sous lui. Ici, chaque pas doit réduire la distance, sinon on
     * ne bouge pas.</p>
     *
     * @return true si un pas a été fait.
     */
    private static boolean stepToward(ServerLevel level, LivingEntity bot, BlockPos target) {
        BlockPos from = bot.blockPosition();
        double best = blockDistance(from, target);
        BlockPos pick = null;
        double bestD = best;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos cand = from.offset(dx, dy, dz);
                    if (!level.getBlockState(cand).isAir() || !level.getBlockState(cand.above()).isAir()) {
                        continue;
                    }
                    double d = blockDistance(cand, target);
                    if (d < bestD) {
                        bestD = d;
                        pick = cand;
                    }
                }
            }
        }
        if (pick == null) {
            return false;
        }
        bot.snapTo(pick.getX() + 0.5, pick.getY(), pick.getZ() + 0.5, bot.getYRot(), 0.0f);
        return true;
    }

    private static double blockDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Va au coffre et vide l'inventaire. Renvoie true quand c'est fait. */
    private static boolean stepTowardDepot(ServerLevel level, WorkState state, LivingEntity bot) {
        if (!state.hasDepot) {
            state.goingToDepot = false;
            return true;
        }
        BlockPos depot = new BlockPos(state.depotX, state.depotY, state.depotZ);
        if (distanceSq(bot, depot) > REACH * REACH) {
            stepToward(level, bot, depot);
            return false;
        }
        List<ItemStack> stacks = invToStacks(state);
        int total = 0;
        for (ItemStack s : stacks) {
            total += s.getCount();
        }
        List<ItemStack> left = stacks;
        for (int guard = 0; guard < 16 && !left.isEmpty(); guard++) {
            Container container = depotContainer(level, state);
            if (container == null) {
                break;
            }
            left = insertAll(container, left);
            if (left.isEmpty()) {
                break;
            }
            if (!nextDepot(level, state)) {
                break;
            }
        }
        state.invSlots.clear();
        for (ItemStack s : left) {
            invInsert(state, List.of(s));
        }
        state.deposited += total - leftCount(left);
        state.goingToDepot = false;
        state.lastStop = "";
        state.setDirty();
        return left.isEmpty();
    }

    private static int leftCount(List<ItemStack> stacks) {
        int n = 0;
        for (ItemStack s : stacks) {
            n += s.getCount();
        }
        return n;
    }

    // ---------------------------------------------------------------- inventaire

    private static boolean fits(WorkState state, List<ItemStack> drops) {
        int free = WorkState.INV_SLOTS - state.invSlots.size();
        int room = free * 64;
        for (String slot : state.invSlots) {
            room += 64 - WorkState.countOf(slot);
        }
        int need = 0;
        for (ItemStack s : drops) {
            need += s.getCount();
        }
        return need <= room;
    }

    private static void invInsert(WorkState state, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            int remaining = stack.getCount();
            // compléter les piles existantes
            for (int i = 0; i < state.invSlots.size() && remaining > 0; i++) {
                String slot = state.invSlots.get(i);
                if (WorkState.idOf(slot).equals(id)) {
                    int have = WorkState.countOf(slot);
                    int put = Math.min(64 - have, remaining);
                    if (put > 0) {
                        state.invSlots.set(i, id + "|" + (have + put));
                        remaining -= put;
                    }
                }
            }
            // nouvelles piles
            while (remaining > 0 && state.invSlots.size() < WorkState.INV_SLOTS) {
                int put = Math.min(64, remaining);
                state.invSlots.add(id + "|" + put);
                remaining -= put;
            }
        }
        state.setDirty();
    }

    private static List<ItemStack> invToStacks(WorkState state) {
        List<ItemStack> out = new ArrayList<>();
        for (String slot : state.invSlots) {
            Identifier id = Identifier.tryParse(WorkState.idOf(slot));
            if (id == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item == null || item == Items.AIR) {
                continue;
            }
            out.add(new ItemStack(item, WorkState.countOf(slot)));
        }
        return out;
    }

    /** Prend une pioche dans l'inventaire, s'il y en a une. */
    private static boolean takePickaxe(WorkState state) {
        for (int i = 0; i < state.invSlots.size(); i++) {
            String slot = state.invSlots.get(i);
            String id = WorkState.idOf(slot);
            if (!id.endsWith("_pickaxe")) {
                continue;
            }
            int count = WorkState.countOf(slot);
            if (count <= 1) {
                state.invSlots.remove(i);
            } else {
                state.invSlots.set(i, id + "|" + (count - 1));
            }
            state.toolId = id;
            state.toolDamage = 0;
            state.setDirty();
            return true;
        }
        return false;
    }

    private static ItemStack toolStack(WorkState state) {
        if (state.toolId.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Identifier id = Identifier.tryParse(state.toolId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (state.toolDamage > 0) {
            stack.setDamageValue(state.toolDamage);
        }
        return stack;
    }

    // ---------------------------------------------------------------- dépôt

    private static List<ItemStack> insertAll(Container depot, List<ItemStack> drops) {
        List<ItemStack> left = new ArrayList<>();
        for (ItemStack stack : drops) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remaining = stack.copy();
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
            for (int i = 0; i < depot.getContainerSize() && !remaining.isEmpty(); i++) {
                if (depot.getItem(i).isEmpty()) {
                    int put = Math.min(depot.getMaxStackSize(remaining), remaining.getCount());
                    depot.setItem(i, remaining.copyWithCount(put));
                    remaining.shrink(put);
                }
            }
            if (!remaining.isEmpty()) {
                left.add(remaining);
            }
        }
        depot.setChanged();
        return left;
    }

    private static Container depotContainer(ServerLevel level, WorkState state) {
        if (!state.hasDepot) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(new BlockPos(state.depotX, state.depotY, state.depotZ));
        return be instanceof Container container ? container : null;
    }

    private static boolean nextDepot(ServerLevel level, WorkState state) {
        BlockPos cur = new BlockPos(state.depotX, state.depotY, state.depotZ);
        for (int d = 1; d <= 16; d++) {
            BlockPos[] candidates = {
                    cur.offset(d, 0, 0), cur.offset(0, 0, d),
                    cur.offset(-d, 0, 0), cur.offset(0, 0, -d)};
            for (BlockPos candidate : candidates) {
                if (level.getBlockState(candidate).isAir()
                        && level.setBlock(candidate, Blocks.CHEST.defaultBlockState(), 3)) {
                    state.depotX = candidate.getX();
                    state.depotY = candidate.getY();
                    state.depotZ = candidate.getZ();
                    state.depotCount++;
                    state.setDirty();
                    return true;
                }
            }
        }
        return false;
    }

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
        sb.append(parts.isEmpty() ? "vide" : String.join(", ", parts));
        return sb.toString();
    }

    private static boolean touchesFluid(ServerLevel level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (!level.getBlockState(pos.relative(d)).getFluidState().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- cycle de vie

    private static LivingEntity servitor(ServerLevel level, WorkState state) {
        if (state.servitorUuid.isEmpty()) {
            return null;
        }
        try {
            Entity e = level.getEntityInAnyDimension(java.util.UUID.fromString(state.servitorUuid));
            return e instanceof LivingEntity living && !living.isRemoved() ? living : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static double distanceSq(LivingEntity bot, BlockPos pos) {
        double dx = bot.getX() - (pos.getX() + 0.5);
        double dy = bot.getY() + 0.9 - (pos.getY() + 0.5);
        double dz = bot.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean spawnServitor(ServerLevel level, WorkState state) {
        Servitor1Entity servitor = ModEntities.SERVITOR_1.create(level, EntitySpawnReason.COMMAND);
        if (servitor == null) {
            return false;
        }
        BlockPos depot = new BlockPos(state.depotX, state.depotY, state.depotZ);
        servitor.snapTo(depot.getX() + 1.5, depot.getY(), depot.getZ() + 0.5, 0.0f, 0.0f);
        servitor.setNoAi(true);                 // il travaille, il ne chasse pas le joueur
        servitor.setPersistenceRequired();      // il ne disparait pas tout seul
        servitor.setCustomName(Component.literal("Servitor de chantier « " + state.jobName + " »"));
        servitor.setCustomNameVisible(true);
        level.addFreshEntity(servitor);
        servitor.setItemSlot(EquipmentSlot.MAINHAND, toolStack(state));
        state.servitorUuid = servitor.getUUID().toString();
        return true;
    }

    public static void discardServitor(ServerLevel level, WorkState state) {
        LivingEntity bot = servitor(level, state);
        if (bot != null) {
            bot.discard();
        }
        sweepOrphans(level);
        state.servitorUuid = "";
    }

    /** Retire tous les Servitors de chantier du monde (orphelins d'un ancien build compris). */
    public static int sweepOrphans(ServerLevel level) {
        int n = 0;
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e instanceof Servitor1Entity s && s.getCustomName() != null
                    && s.getCustomName().getString().startsWith("Servitor de chantier")) {
                e.discard();
                n++;
            }
        }
        return n;
    }

    private static void conclude(ServerLevel level, WorkState state) {
        state.finished = true;
        long volume = (long) (state.maxX - state.minX + 1) * (state.maxY - state.minY + 1)
                * (state.maxZ - state.minZ + 1);
        int left = volume <= 100_000 ? countRemaining(level, state) : -1;
        String verdict = left < 0 ? "reste non verifie (zone trop grande)"
                : left == 0 ? "zone vide, verifie"
                : left + " blocs restent — voir /work status et le journal";
        state.lastStop = "termine: " + state.done + " cassés, " + state.skipped + " laissés, "
                + state.deposited + " objets en coffre — " + verdict;
        discardServitor(level, state);
        state.setDirty();
    }

    /** Compte les blocs non vides restants dans la zone (une passe, au moment de conclure). */
    private static int countRemaining(ServerLevel level, WorkState state) {
        int left = 0;
        for (int y = state.minY; y <= state.maxY; y++) {
            for (int z = state.minZ; z <= state.maxZ; z++) {
                for (int x = state.minX; x <= state.maxX; x++) {
                    if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                        left++;
                    }
                }
            }
        }
        return left;
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
     * Démarre un chantier : pose le dépôt, équipe le Servitor, le met debout.
     *
     * @return l'état du chantier, ou {@code null} si le dépôt ou le Servitor n'a pas pu être mis en
     *         place (on refuse de travailler dans ces conditions).
     */
    public static WorkState start(ServerLevel level, String name, BlockPos a, BlockPos b, String toolId) {
        WorkState state = WorkState.get(level);
        sweepOrphans(level);             // jamais deux Servitors pour un chantier
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
        state.depotCount = 1;
        state.lootBlocks = 0;
        state.lootLog.clear();
        state.invSlots.clear();
        state.hasTarget = false;
        state.progress = 0.0;
        state.goingToDepot = false;
        state.finishing = false;
        state.finished = false;
        state.lastStop = "";
        state.toolId = toolId;
        state.toolDamage = 0;
        state.toolBlocks = 0;
        state.servitorUuid = "";

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
        if (!spawnServitor(level, state)) {
            state.jobName = null;
            state.setDirty();
            return null;
        }
        state.setDirty();
        return state;
    }

    private static BlockPos placeDepot(ServerLevel level, WorkState state) {
        BlockPos base = new BlockPos(state.minX, state.maxY + 2, state.minZ);
        for (int dy = 0; dy < 16; dy++) {
            BlockPos candidate = base.above(dy);
            if (level.getBlockState(candidate).isAir()
                    && level.setBlock(candidate, Blocks.CHEST.defaultBlockState(), 3)) {
                return candidate;
            }
        }
        return null;
    }

    public static WorkState resume(ServerLevel level) {
        WorkState state = WorkState.get(level);
        if (state.jobName == null || state.total == 0 || state.cursorY < state.minY) {
            return null;
        }
        if (state.hasJob()) {
            return state;
        }
        state.finished = false;
        state.lastStop = "";
        if (!spawnServitor(level, state)) {
            state.finished = true;
            state.setDirty();
            return null;
        }
        state.setDirty();
        return state;
    }

    public static boolean stop(ServerLevel level) {
        WorkState state = WorkState.get(level);
        if (!state.hasJob()) {
            return false;
        }
        discardServitor(level, state);
        state.lastStop = "arrete: " + state.done + " cassés, " + state.skipped + " laissés, "
                + state.deposited + " objets en coffre";
        state.finished = true;
        state.setDirty();
        return true;
    }

    /** Change l'outil du Servitor en cours de chantier. */
    public static boolean setTool(ServerLevel level, WorkState state, String toolId) {
        state.toolId = toolId;
        state.toolDamage = 0;
        LivingEntity bot = servitor(level, state);
        if (bot != null) {
            bot.setItemSlot(EquipmentSlot.MAINHAND, toolStack(state));
        }
        state.setDirty();
        return true;
    }

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
