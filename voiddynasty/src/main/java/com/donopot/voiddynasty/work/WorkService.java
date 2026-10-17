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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
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
 *       (règle du jeu). Le chantier s'arrête si l'outil ne permet pas de récolter le bloc.
 *       L'outil s'use, et l'opérateur fournit son remplacement lorsqu'il casse.</li>
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

    private WorkService() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(WorkService::endTick);
        // La mort du Servitor arrête le chantier proprement : le journal reste, donc on reprend.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            MinecraftServer server = entity.level().getServer();
            if (server == null) {
                return;
            }
            if (!(entity.level() instanceof ServerLevel deathLevel)) return;
            WorkState state = WorkState.get(deathLevel);
            if (state.hasJob() && !state.servitorUuid.isEmpty()
                    && entity.getUUID().toString().equals(state.servitorUuid)) {
                state.lastStop = "servitor tue (" + source.getLocalizedDeathMessage(entity).getString()
                        + ") apres " + state.done + " blocs — journal conserve, /work resume pour reprendre";
                state.finished = true;
                MissionService.paused(state, "WORKER_DIED");
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
        for (ServerLevel level : server.getAllLevels()) {
            WorkState state = WorkState.get(level);
            if (state.rollbackRemaining > 0) {
                rollbackTick(level, state);
                continue;
            }
            if (!state.hasJob()) continue;
            if (state.awaitingServitor && state.servitorUuid.isEmpty()) {
                if (level.getGameTime() % 20 == 0) spawnServitor(level, state);
                continue;
            }
            LivingEntity bot = servitor(level, state);
            if (bot == null || bot.level() != level) {
                pause(state, "servitor absent ou dimension incorrecte — /work resume");
                continue;
            }
            if (bot instanceof Servitor1Entity worker) worker.setWorkMode();
            double oldProgress = state.progress;
            int oldDone = state.done;
            int oldBuild = state.depotBuildTicks;
            int oldX = state.cursorX, oldY = state.cursorY, oldZ = state.cursorZ;
            tickWorker(level, state, bot);
            if (!state.hasJob() && bot instanceof Mob mob) mob.getNavigation().stop();
            double moved = bot.distanceToSqr(state.previousX, state.previousY, state.previousZ);
            if (moved > 0.01 || state.progress != oldProgress || state.done != oldDone || state.depotBuildTicks != oldBuild
                    || oldX != state.cursorX || oldY != state.cursorY || oldZ != state.cursorZ) state.idleTicks = 0;
            else if (++state.idleTicks > 400) {
                if (bot instanceof Mob mob) mob.getNavigation().stop();
                pause(state, "trajet bloque — sac et journal conserves; /work resume");
            }
            state.previousX = bot.getX(); state.previousY = bot.getY(); state.previousZ = bot.getZ();
        }
        lastMs = lastMs * 0.8 + ((System.nanoTime() - start) / 1_000_000.0) * 0.2;
    }

    /** Une unité de travail par tick. */
    private static void tickWorker(ServerLevel level, WorkState state, LivingEntity bot) {
        if (MissionService.tick(level, state, bot)) return;
        // Infrastructure is a worker task; starting a selection never places or requires a chest.
        if (state.buildingDepot || !state.hasDepot) {
            WorkDepot.tick(level, state, bot);
            return;
        }
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
                if (state.cursorY >= state.minY) return; // scan budget exhausted, continue next tick
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
        if (!level.hasChunkAt(target)) { pause(state, "chunk cible decharge — /work resume"); return; }
        BlockState blockState = level.getBlockState(target);
        if (blockState.isAir()) {
            state.hasTarget = false;
            return;
        }

        // C. à portée ?
        if (distanceSq(bot, target) > REACH * REACH || !visible(level, bot, target)) {
            // Filet de securite : si marcher ne change rien pendant 40 ticks, on abandonne
            // cette cible (elle sera comptee comme laissee) au lieu de figer le chantier.
            if (state.idleTicks > 40) {
                if (bot instanceof Mob mob) mob.getNavigation().stop();
                state.hasTarget = false;
                state.skipped++;
                state.idleTicks = 0;
                state.setDirty();
                advance(state);
                return;
            }
            stepToward(level, state, bot, target);
            return;
        }

        // D. le temps de casse est-il écoulé ?
        if (bot instanceof Mob mob) mob.getNavigation().stop();
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
        for (int scan = 0; scan < 64; scan++) {
            if (state.cursorY < state.minY) {
                return false;
            }
            BlockPos pos = new BlockPos(state.cursorX, state.cursorY, state.cursorZ);
            if (!loadedNeighborhood(level, pos)) { pause(state, "chunk decharge — /work resume"); return false; }
            BlockState blockState = level.getBlockState(pos);
            WorkMission mission = MissionService.active(state);
            if (!blockState.isAir() && (mission == null || MissionService.matches(mission, blockState))
                    && !skip(level, state, pos, blockState)) {
                state.hasTarget = true;
                state.targetX = pos.getX();
                state.targetY = pos.getY();
                state.targetZ = pos.getZ();
                state.progress = 0.0;
                return true;
            }
            state.setDirty();
            if (!advance(state)) return false;
        }
        return false;
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

    /** Verified mutation, then receipt and inventory update on the same server thread. */
    private static void breakTarget(ServerLevel level, WorkState state, LivingEntity bot, BlockPos pos) {
        if (!loadedNeighborhood(level, pos)) { pause(state, "chunk decharge avant casse — /work resume"); return; }
        BlockState blockState = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack tool = toolStack(state);

        // Revalidate after travel/mining delay, before touching the block or ledger.
        WorkMission mission = MissionService.active(state);
        if (blockState.isAir() || (mission != null && !MissionService.matches(mission, blockState))
                || skip(level, state, pos, blockState)) {
            state.hasTarget = false; advance(state); state.setDirty(); return;
        }

        // Drops seulement si l'outil en main sait récolter ce bloc (règle du jeu).
        boolean canHarvest = !blockState.requiresCorrectToolForDrops()
                || (!tool.isEmpty() && tool.isCorrectToolForDrops(blockState));
        if (!canHarvest) {
            pause(state, "outil inadapte — bloc conserve; /work tool puis resume, ou deposit pour vider le sac");
            return;
        }
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

        if (state.journal.size() >= WorkState.JOURNAL_MAX) {
            state.finishing = !state.invSlots.isEmpty();
            pause(state, "journal plein — deposer le sac puis /work commit avant un nouveau chantier"); return;
        }
        List<ItemStack> replacement = WorkInventory.insert(state.invSlots, WorkState.INV_SLOTS, drops);
        if (replacement == null) { state.goingToDepot = true; state.setDirty(); return; }
        CompoundTag saved = NbtUtils.writeBlockState(blockState);
        // A rejected mutation creates neither resources nor a rollback entry.
        if (!level.destroyBlock(pos, false, null, 512)) {
            pause(state, "casse refusee — ressources et journal inchanges"); return;
        }
        WorkState.Entry entry = new WorkState.Entry(pos.immutable(), saved, level.getGameTime());
        entry.drops = WorkInventory.copy(drops);
        entry.dropsKnown = true;
        state.journal.add(entry);
        MissionService.mined(state, drops);
        state.invSlots.clear(); state.invSlots.addAll(replacement);
        state.done++;
        state.hasTarget = false;
        state.progress = 0.0;
        advance(state);
        if (!state.tool.isEmpty() && state.tool.getMaxDamage() > 0) {
            state.tool.setDamageValue(state.tool.getDamageValue() + 1);
            state.toolDamage = state.tool.getDamageValue();
            state.toolBlocks++;
            if (state.toolDamage >= state.tool.getMaxDamage()) {
                state.tool = ItemStack.EMPTY; state.toolId = ""; state.toolDamage = 0;
                // Do not consume journalled output as a tool: rollback needs those exact drops.
                pause(state, "pioche cassee — /work tool pour fournir un outil operateur");
            }
        }
        bot.setItemSlot(EquipmentSlot.MAINHAND, toolStack(state));
        state.setDirty();
    }

    // ---------------------------------------------------------------- déplacement

    static boolean visible(ServerLevel level, LivingEntity bot, BlockPos target) {
        // Un joueur mine un bloc par la face qu'il voit, pas par son centre : un bloc
        // dont le centre est occupe par un voisin reste minable si une face est degagee.
        // Tester le seul centre bloquait le servitor debout sur le cube qu'il minait.
        if (visibleAt(level, bot, Vec3.atCenterOf(target))) return true;
        for (Direction d : Direction.values()) {
            if (visibleAt(level, bot, Vec3.atCenterOf(target).add(
                    d.getStepX() * 0.49, d.getStepY() * 0.49, d.getStepZ() * 0.49))) return true;
        }
        return false;
    }

    private static boolean visibleAt(ServerLevel level, LivingEntity bot, Vec3 point) {
        var hit = level.clip(new ClipContext(bot.getEyePosition(), point,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bot));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(new BlockPos(
                (int) Math.floor(point.x), (int) Math.floor(point.y), (int) Math.floor(point.z)));
    }

    /** Native pathfinding to a reachable work position, never teleport or mine through a wall. */
    static boolean stepToward(ServerLevel level, WorkState state, LivingEntity bot, BlockPos target) {
        if (!(bot instanceof Mob mob)) return false;
        if (level.getGameTime() < state.nextPathTick) return !mob.getNavigation().isDone();
        state.nextPathTick = level.getGameTime() + 20;
        var path = findWorkPath(level, bot, target);
        return path != null && path.canReach() && mob.getNavigation().moveTo(path, 1.0);
    }

    static net.minecraft.world.level.pathfinder.Path findWorkPath(ServerLevel level, LivingEntity bot, BlockPos target) {
        if (!(bot instanceof Mob mob)) return null;
        // Multi-target vanilla path search can detour away from the target around walls.
        java.util.Set<BlockPos> stands = new java.util.HashSet<>();
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++)
            for (int dy = -2; dy <= 1; dy++) {
                BlockPos feet = target.offset(dx, dy, dz);
                if (!level.hasChunkAt(feet) || !level.hasChunkAt(feet.below())) continue;
                if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) continue;
                if (!level.getBlockState(feet).getFluidState().isEmpty()
                        || !level.getBlockState(feet.above()).getFluidState().isEmpty()) continue;
                var box = bot.getBoundingBox().move(feet.getX() + 0.5 - bot.getX(),
                        feet.getY() - bot.getY(), feet.getZ() + 0.5 - bot.getZ());
                if (!level.noCollision(bot, box)) continue;
                Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + bot.getEyeHeight(), feet.getZ() + 0.5);
                // Vanilla navigation finishes slightly before the node's exact centre.
                // Keep a margin so arrival always leaves the worker within interaction reach.
                if (eye.distanceToSqr(Vec3.atCenterOf(target)) > (REACH - 1.0) * (REACH - 1.0)) continue;
                var hit = level.clip(new ClipContext(eye, Vec3.atCenterOf(target),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bot));
                if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(target)) stands.add(feet);
            }
        if (stands.isEmpty()) return null;
        return mob.getNavigation().createPath(stands, 0);
    }

    /** Va au coffre et vide l'inventaire. Renvoie true quand c'est fait. */
    static boolean stepTowardDepot(ServerLevel level, WorkState state, LivingEntity bot) {
        if (!state.hasDepot) {
            WorkDepot.request(state);
            return false;
        }
        BlockPos depot = new BlockPos(state.depotX, state.depotY, state.depotZ);
        if (!level.hasChunkAt(depot)) { pause(state, "depot decharge — sac conserve"); return false; }
        if (distanceSq(bot, depot) > REACH * REACH || !visible(level, bot, depot)) {
            stepToward(level, state, bot, depot);
            return false;
        }
        List<ItemStack> stacks = invToStacks(state);
        int total = 0;
        for (ItemStack s : stacks) {
            total += s.getCount();
        }
        Container container = depotContainer(level, state);
        if (container == null) { WorkDepot.request(state); return false; }
        List<ItemStack> left = insertAll(container, stacks);
        state.invSlots.clear(); state.invSlots.addAll(WorkInventory.copy(left));
        state.deposited += total - leftCount(left);
        state.goingToDepot = !left.isEmpty();
        state.lastStop = "";
        if (!left.isEmpty() && !nextDepot(level, state)) pause(state, "depots pleins — sac conserve");
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
        return WorkInventory.insert(state.invSlots, WorkState.INV_SLOTS, drops) != null;
    }

    private static List<ItemStack> invToStacks(WorkState state) {
        return WorkInventory.copy(state.invSlots);
    }

    private static ItemStack toolStack(WorkState state) { return state.tool.copy(); }

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
        WorkDepot.request(state);
        return true;
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

    private static boolean loadedNeighborhood(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        for (Direction direction : Direction.values()) if (!level.hasChunkAt(pos.relative(direction))) return false;
        return true;
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

    static double distanceSq(LivingEntity bot, BlockPos pos) {
        return bot.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
    }

    private static boolean spawnServitor(ServerLevel level, WorkState state) {
        Servitor1Entity servitor = ModEntities.SERVITOR_1.create(level, EntitySpawnReason.COMMAND);
        if (servitor == null) {
            return false;
        }
        BlockPos anchor = state.hasDepot
                ? new BlockPos(state.depotX + 1, state.depotY, state.depotZ)
                : new BlockPos(state.originX, state.originY, state.originZ);
        boolean placed = false;
        // Bounded search. No floor or chest at the selection corner is required.
        for (int scan = -1; scan < 128; scan++) {
            int index = scan < 0 ? 0 : state.spawnSearchCursor++ % (13 * 13 * 21);
            int plane = index / (13 * 13);
            int dy = plane <= 4 ? plane : 4 - plane;
            int cell = index % (13 * 13);
            int dx = cell % 13 - 6, dz = cell / 13 - 6;
            BlockPos feet = scan < 0 ? anchor : anchor.offset(dx, dy, dz);
            if (!safeStand(level, feet)) continue;
            servitor.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0, 0);
            if (!level.noCollision(servitor)) continue;
            placed = true; break;
        }
        state.setDirty();
        if (!placed) return false;
        servitor.setWorkMode();
        servitor.setPersistenceRequired();      // il ne disparait pas tout seul
        servitor.setCustomName(Component.literal("Servitor de chantier « " + state.jobName + " »"));
        servitor.setCustomNameVisible(true);
        if (!level.noCollision(servitor) || !level.addFreshEntity(servitor)) return false;
        servitor.setItemSlot(EquipmentSlot.MAINHAND, toolStack(state));
        servitor.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        state.servitorUuid = servitor.getUUID().toString();
        state.awaitingServitor = false;
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

    /** Only our tagged workers; never discard a player-renamed combat Servitor. */
    public static int sweepOrphans(ServerLevel level) {
        int n = 0;
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e instanceof Servitor1Entity s && s.entityTags().contains("voiddynasty_work")) {
                e.discard();
                n++;
            }
        }
        return n;
    }

    static void conclude(ServerLevel level, WorkState state) {
        state.finished = true;
        MissionService.concluded(state);
        String verdict = "curseur termine (blocs proteges et butin laisses en place)";
        state.lastStop = "termine: " + state.done + " cassés, " + state.skipped + " laissés, "
                + state.deposited + " objets en coffre — " + verdict;
        discardServitor(level, state);
        state.setDirty();
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

    /** Accept the job independently of depot preparation. */
    public static WorkState start(ServerLevel level, String name, BlockPos a, BlockPos b, String toolId) {
        return start(level, name, a, b, toolId, new BlockPos(Math.min(a.getX(), b.getX()) - 1,
                Math.max(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()) - 2));
    }

    public static WorkState start(ServerLevel level, String name, BlockPos a, BlockPos b, String toolId, BlockPos origin) {
        WorkState state = WorkState.get(level);
        if (state.needsRecovery()) return null;
        if (state.jobName != null || !state.journal.isEmpty() || !state.invSlots.isEmpty()) {
            state.lastStop = "chantier precedent conserve — /work resume, rollback ou commit";
            return null;
        }
        sweepOrphans(level);             // jamais deux Servitors pour un chantier
        state.missionId = "";
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
        state.depotCount = 0;
        state.hasDepot = false;
        state.originX = origin.getX(); state.originY = origin.getY(); state.originZ = origin.getZ();
        state.spawnSearchCursor = 0;
        state.supplyChests = 64; state.supplySupports = 64;
        state.awaitingServitor = true;
        WorkDepot.request(state);
        state.lootBlocks = 0;
        state.lootLog.clear();
        state.depots.clear();
        state.rollbackStarted = false;
        state.rollbackRemaining = 0;
        state.hasTarget = false;
        state.progress = 0.0;
        state.goingToDepot = false;
        state.finishing = false;
        state.finished = false;
        state.lastStop = "";
        state.toolId = toolId;
        state.tool = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(toolId)));
        state.toolDamage = 0;
        state.toolBlocks = 0;
        state.servitorUuid = "";

        if (!spawnServitor(level, state)) state.lastStop = "chantier accepte — preparation du depart du servitor";
        state.setDirty();
        return state;
    }

    public static WorkState resume(ServerLevel level) {
        WorkState state = WorkState.get(level);
        if (state.needsRecovery() || state.rollbackRemaining > 0) return null;
        WorkMission mission = MissionService.active(state);
        if (mission != null && mission.terminal() && !(state.finishing && !state.invSlots.isEmpty())) return null;
        if (state.jobName == null || state.total == 0 || (state.rollbackStarted && state.invSlots.isEmpty())
                || (state.cursorY < state.minY && state.invSlots.isEmpty())) {
            return null;
        }
        if (state.hasJob()) {
            return state;
        }
        discardServitor(level, state);
        state.idleTicks = 0; state.nextPathTick = 0;
        state.finished = false;
        if (mission != null && !mission.terminal()) { mission.status = "PREPARING"; mission.reason = ""; }
        state.lastStop = "";
        if (state.cursorY < state.minY || state.rollbackStarted) state.finishing = true;
        state.spawnSearchCursor = 0;
        state.awaitingServitor = true;
        if (!spawnServitor(level, state)) state.lastStop = "chantier repris — recherche d'un depart sur";
        state.setDirty();
        return state;
    }

    public static boolean stop(ServerLevel level) {
        WorkState state = WorkState.get(level);
        state.rollbackRemaining = 0;
        MissionService.paused(state, "OPERATOR_PAUSE");
        if (!state.hasJob()) { discardServitor(level, state); state.setDirty(); return false; }
        discardServitor(level, state);
        state.lastStop = "arrete: " + state.done + " cassés, " + state.skipped + " laissés, "
                + state.deposited + " objets en coffre";
        state.finished = true;
        state.setDirty();
        return true;
    }

    /** Change l'outil du Servitor en cours de chantier. */
    public static boolean setTool(ServerLevel level, WorkState state, String toolId) {
        if (state.needsRecovery()) return false;
        state.toolId = toolId;
        state.tool = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(toolId)));
        state.toolDamage = 0;
        LivingEntity bot = servitor(level, state);
        if (bot != null) {
            bot.setItemSlot(EquipmentSlot.MAINHAND, toolStack(state));
        }
        state.setDirty();
        return true;
    }

    static void pause(WorkState state, String reason) {
        state.finished = true; state.lastStop = reason;
        MissionService.paused(state, reason); state.setDirty();
    }

    private static boolean safeStand(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.hasChunkAt(pos.below())
                && level.getWorldBorder().isWithinBounds(pos)
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && !ClaimIntegration.isClaimProtected(level, pos);
    }

    public static void requestRollback(ServerLevel level, WorkState state, int count) {
        if (state.needsRecovery()) return;
        stop(level);
        MissionService.invalidate(state);
        discardServitor(level, state);
        state.finished = true;
        state.rollbackStarted = true;
        state.rollbackRemaining = Math.min(count, state.journal.size());
        state.lastStop = "rollback en cours — /work status";
        state.setDirty();
    }

    private static void rollbackTick(ServerLevel level, WorkState state) {
        // One transaction per tick; no 50,000-block command stall.
        if (state.journal.isEmpty()) { state.rollbackRemaining = 0; state.setDirty(); return; }
        WorkState.Entry entry = state.journal.getLast();
        if (!entry.dropsKnown) { rollbackFailure(state, "ancien journal sans recus de ressources"); return; }
        if (!level.hasChunkAt(entry.pos) || ClaimIntegration.isClaimProtected(level, entry.pos)) {
            rollbackFailure(state, "bloc decharge ou protege"); return;
        }
        if (!level.getBlockState(entry.pos).isAir()) {
            rollbackFailure(state, "bloc modifie depuis la casse en " + entry.pos.toShortString()); return;
        }
        List<Container> containers = new ArrayList<>();
        List<ItemStack> before = WorkInventory.copy(state.invSlots);
        int bagSize = before.size();
        for (BlockPos depot : state.depots) {
            if (!level.hasChunkAt(depot) || ClaimIntegration.isClaimProtected(level, depot)) {
                rollbackFailure(state, "depot decharge ou protege"); return;
            }
            if (level.getBlockEntity(depot) instanceof Container container) {
                containers.add(container);
                for (int i = 0; i < container.getContainerSize(); i++) before.add(container.getItem(i).copy());
            }
        }
        BlockState old = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), entry.state);
        if (old.isAir() || old.hasBlockEntity()) { rollbackFailure(state, "etat original invalide"); return; }
        int beforeBagCount = state.invCount();
        boolean restored = WorkRollback.apply(before, entry.drops,
                () -> level.setBlock(entry.pos, old, 2 | 16), after -> {
                    state.invSlots.clear();
                    for (int i = 0; i < bagSize; i++) if (!after.get(i).isEmpty()) state.invSlots.add(after.get(i));
                    int offset = bagSize;
                    for (Container container : containers) {
                        for (int i = 0; i < container.getContainerSize(); i++) container.setItem(i, after.get(offset++));
                        container.setChanged();
                    }
                });
        if (!restored) { rollbackFailure(state, "ressources manquantes ou restauration refusee"); return; }
        int fromBag = beforeBagCount - state.invCount();
        state.deposited = Math.max(0, state.deposited - Math.max(0, leftCount(entry.drops) - fromBag));
        state.journal.removeLast();
        state.done = Math.max(0, state.done - 1);
        state.rollbackRemaining--;
        state.lastStop = "rollback: " + state.rollbackRemaining + " demandes restantes; journal " + state.journal.size();
        state.setDirty();
    }

    private static void rollbackFailure(WorkState state, String reason) {
        state.rollbackRemaining = 0;
        pause(state, "rollback arrete: " + reason + " — ressources et recu conserves");
    }

    /** Explicitly release rollback history. Never throw away carried resources. */
    public static boolean commit(ServerLevel level, WorkState state) {
        if (state.needsRecovery() || state.hasJob() || state.rollbackRemaining > 0 || !state.invSlots.isEmpty()) return false;
        WorkMission mission = MissionService.active(state);
        if (mission != null && !mission.terminal()) return false;
        discardServitor(level, state);
        state.missionId = "";
        state.journal.clear(); state.depots.clear();
        state.jobName = null; state.hasDepot = false; state.rollbackStarted = false;
        state.tool = ItemStack.EMPTY; state.toolId = "";
        state.lastStop = "chantier valide — ressources laissees dans les depots; rollback abandonne";
        state.setDirty(); return true;
    }

    /** Deposit-only recovery from any pause, without breaking another block. */
    public static WorkState deposit(ServerLevel level) {
        WorkState state = WorkState.get(level);
        if (state.needsRecovery() || state.rollbackRemaining > 0 || state.jobName == null || state.invSlots.isEmpty())
            return null;
        stop(level);
        state.finishing = true;
        return resume(level);
    }

    public static long remaining(WorkState state) {
        if (state.cursorY < state.minY) return 0;
        long width = state.maxX - state.minX + 1;
        long depth = state.maxZ - state.minZ + 1;
        long layers = state.cursorY - state.minY + 1L;
        return layers * width * depth
                - (long) (state.cursorZ - state.minZ) * width
                - (state.cursorX - state.minX);
    }
}
