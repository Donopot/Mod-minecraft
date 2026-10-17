package com.donopot.voiddynasty.work;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Blocks;
import com.donopot.voiddynasty.registry.ModEntities;
import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.world.entity.EntitySpawnReason;
import java.util.*;

/** Separate disposable test mod, never included in the playable artifact. */
public final class WorkServerTest implements ModInitializer {
    private static int checks;
    private static double largestDetour;
    private static UUID navigating;
    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
        checks++; System.out.println("[workcheck] PASS " + name);
    }
    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
            dispatcher.register(Commands.literal("workcheck")
                .requires(src -> src.getServer().isDedicatedServer())
                .then(Commands.argument("phase", StringArgumentType.word()).executes(ctx -> {
                    String phase = StringArgumentType.getString(ctx, "phase");
                    try {
                        run(ctx.getSource().getLevel(), phase);
                        ctx.getSource().sendSuccess(() -> Component.literal("PASS " + phase + " checks=" + checks), false);
                        return 1;
                    } catch (Throwable error) {
                        error.printStackTrace();
                        ctx.getSource().sendFailure(Component.literal("FAIL " + phase + ": " + error));
                        return 0;
                    }
                }))));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (navigating == null) return;
            var bot = server.overworld().getEntity(navigating);
            if (bot != null) largestDetour = Math.max(largestDetour, Math.abs(bot.getZ() - 8.5));
        });
    }
    private static WorkState reset(ServerLevel level) {
        WorkService.stop(level);
        WorkState s = WorkState.get(level);
        WorkService.discardServitor(level, s);
        s.jobName = "test"; s.finished = true; s.journal.clear(); s.invSlots.clear(); s.depots.clear();
        s.rollbackRemaining = 0; s.rollbackStarted = false; s.done = 0;
        s.hasTarget = false; s.hasDepot = false; s.goingToDepot = false; s.finishing = false;
        s.buildingDepot = false; s.depotPlanned = false; s.awaitingServitor = false;
        s.depotSearchCursor = 0; s.depotBuildTicks = 0; s.supplyChests = 64; s.supplySupports = 64;
        return s;
    }
    private static void tickRollback(ServerLevel level, WorkState state) throws Exception {
        var method = WorkService.class.getDeclaredMethod("rollbackTick", ServerLevel.class, WorkState.class);
        method.setAccessible(true); method.invoke(null, level, state);
    }
    private static WorkState.Entry receipt(WorkState s, BlockPos pos, ItemStack... drops) {
        var e = new WorkState.Entry(pos, NbtUtils.writeBlockState(Blocks.STONE.defaultBlockState()), 0);
        e.dropsKnown = true; e.drops = List.of(drops); s.journal.add(e); s.done++;
        return e;
    }
    private static Container chest(ServerLevel level, WorkState s, BlockPos pos) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3); s.depots.add(pos);
        Container c = (Container) level.getBlockEntity(pos); c.clearContent(); return c;
    }
    private static void run(ServerLevel level, String phase) throws Exception {
        switch (phase) {
            case "autoDepotSetup" -> {
                WorkState s = reset(level); s.jobName = null;
                for (int x = 42; x <= 58; x++) for (int z = 2; z <= 14; z++)
                    for (int y = 56; y <= 68; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                BlockPos origin = new BlockPos(50, 64, 8), target = new BlockPos(48, 64, 8);
                level.setBlock(origin.below(), Blocks.STONE.defaultBlockState(), 3);
                level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
                s = WorkService.start(level, "automatic", target, target, "minecraft:iron_pickaxe", origin);
                check(s != null && s.hasJob() && !s.hasDepot && s.depots.isEmpty(), "zone accepted without prebuilt depot or ground near selection corner");
                check(!s.servitorUuid.isEmpty() && level.getBlockState(target).is(Blocks.STONE)
                        && s.supplyChests == 64 && s.supplySupports == 64, "start spawns worker without remotely placing infrastructure");
            }
            case "autoDepotCheck" -> {
                WorkState s = WorkState.get(level); BlockPos p = new BlockPos(s.depotX, s.depotY, s.depotZ);
                check(s.hasDepot && s.depots.size() == 1 && level.getBlockState(p).is(Blocks.CHEST), "worker builds and registers its own depot");
                check(level.getBlockState(p.below()).is(Blocks.COBBLESTONE) && s.supplyChests == 63 && s.supplySupports == 63,
                        "worker builds missing footing from separate finite kit");
                check(s.finished && s.done == 1 && s.invSlots.isEmpty() && s.deposited == 1, "worker completes mining and deposits without player preparing ground");
            }
            case "extendDepotSetup" -> {
                WorkState s = WorkState.get(level);
                Container depot = (Container) level.getBlockEntity(new BlockPos(s.depotX, s.depotY, s.depotZ));
                for (int i = 0; i < depot.getContainerSize(); i++) depot.setItem(i, new ItemStack(Items.EMERALD, 64));
                s.invSlots.add(new ItemStack(Items.DIAMOND, 2));
                check(WorkService.deposit(level) != null, "full-depot recovery accepted with carried resources");
            }
            case "extendDepotCheck" -> {
                WorkState s = WorkState.get(level);
                check(s.finished && s.invSlots.isEmpty() && s.depots.size() == 2, "worker builds an additional depot when first is full");
                Container first = (Container) level.getBlockEntity(s.depots.getFirst());
                Container second = (Container) level.getBlockEntity(s.depots.getLast());
                boolean untouched = true;
                for (int i = 0; i < first.getContainerSize(); i++) untouched &= first.getItem(i).is(Items.EMERALD) && first.getItem(i).getCount() == 64;
                check(untouched, "depot extension never clears unrelated full chest contents");
                int delivered = 0;
                for (int i = 0; i < second.getContainerSize(); i++) if (second.getItem(i).is(Items.DIAMOND)) delivered += second.getItem(i).getCount();
                check(delivered == 2, "extension deposits carried resources exactly once");
                check(s.supplyChests == 62 && s.supplySupports == 62, "each constructed depot and footing consumes one kit unit");
            }
            case "pendingSpawnSetup" -> {
                WorkState s = reset(level); s.jobName = null;
                for (int x = 50; x <= 62; x++) for (int z = 18; z <= 30; z++)
                    for (int y = 47; y <= 70; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                BlockPos origin = new BlockPos(56, 64, 24), target = new BlockPos(56, 90, 24);
                level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
                s = WorkService.start(level, "pending", target, target, "minecraft:iron_pickaxe", origin);
                check(s != null && s.hasJob() && s.awaitingServitor && s.servitorUuid.isEmpty(), "even unavailable spawn ground does not reject selection");
                check(level.getBlockState(target).is(Blocks.STONE) && s.journal.isEmpty(), "queued start leaves terrain and ledger intact");
                level.setBlock(origin.below(), Blocks.STONE.defaultBlockState(), 3);
            }
            case "pendingSpawnCheck" -> {
                WorkState s = WorkState.get(level);
                check(!s.awaitingServitor && !s.servitorUuid.isEmpty() && s.jobName.equals("pending"), "accepted job automatically finds newly available departure ground");
            }
            case "mining" -> {
                WorkState s = reset(level);
                var bot = ModEntities.SERVITOR_1.create(level, EntitySpawnReason.COMMAND);
                check(bot != null, "mining witness created");
                s.tool = new ItemStack(Items.IRON_PICKAXE); s.toolId = "minecraft:iron_pickaxe";
                BlockPos target = new BlockPos(25, 65, 20);
                var mine = WorkService.class.getDeclaredMethod("breakTarget", ServerLevel.class, WorkState.class,
                        LivingEntity.class, BlockPos.class); mine.setAccessible(true);
                level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
                net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
                        .filter(i -> i != Items.AIR && i != Items.COBBLESTONE).limit(36)
                        .forEach(i -> s.invSlots.add(new ItemStack(i)));
                mine.invoke(null, level, s, bot, target);
                check(level.getBlockState(target).is(Blocks.STONE) && s.journal.isEmpty() && s.invCount() == 36,
                        "full incompatible native bag prevents block removal");
                s.invSlots.clear(); s.goingToDepot = false;
                mine.invoke(null, level, s, bot, target);
                check(level.getBlockState(target).isAir() && s.invCount() == 1 && s.invSlots.getFirst().is(Items.COBBLESTONE),
                        "successful native mining preserves exact stone drop");
                check(s.journal.size() == 1 && s.journal.getFirst().dropsKnown && s.tool.getDamageValue() == 1,
                        "successful mining records receipt and tool wear");
                level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
                mine.invoke(null, level, s, bot, target);
                check(s.invSlots.stream().anyMatch(stack -> stack.is(Items.DIRT)), "ordinary dirt drops even with pickaxe equipped");
                level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
                int count = s.invCount(), entries = s.journal.size();
                ClaimIntegration.forcerProtege(true);
                try { mine.invoke(null, level, s, bot, target); }
                finally { ClaimIntegration.forcerProtege(null); }
                check(level.getBlockState(target).is(Blocks.STONE) && s.invCount() == count && s.journal.size() == entries,
                        "claim rechecked before delayed mining mutation");
                s.tool = new ItemStack(Items.STICK); s.finished = false;
                mine.invoke(null, level, s, bot, target);
                check(level.getBlockState(target).is(Blocks.STONE) && s.invCount() == count
                        && s.journal.size() == entries && s.finished, "unsuitable tool pauses without wasting ore");
                Container depot = chest(level, s, new BlockPos(28, 64, 24));
                for (int i = 0; i < depot.getContainerSize(); i++) depot.setItem(i, new ItemStack(Items.EMERALD, 64));
                ItemStack named = new ItemStack(Items.DIAMOND, 3);
                named.set(DataComponents.CUSTOM_NAME, Component.literal("Reste"));
                var insert = WorkService.class.getDeclaredMethod("insertAll", Container.class, List.class);
                insert.setAccessible(true);
                @SuppressWarnings("unchecked") List<ItemStack> remainder = (List<ItemStack>) insert.invoke(null, depot, List.of(named));
                check(remainder.size() == 1 && ItemStack.matches(named, remainder.getFirst()) && depot.getItem(0).getCount() == 64,
                        "full depot returns complete named remainder without loss");
            }
            case "rollback" -> {
                WorkState s = reset(level);
                BlockPos a = new BlockPos(20, 65, 20), b = a.east();
                level.setBlock(a, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
                Container first = chest(level, s, new BlockPos(20, 64, 24));
                Container second = chest(level, s, new BlockPos(24, 64, 24));
                first.setItem(0, new ItemStack(Items.COBBLESTONE, 1));
                second.setItem(0, new ItemStack(Items.COBBLESTONE, 1));
                second.setItem(1, new ItemStack(Items.EMERALD, 17));
                receipt(s, a, new ItemStack(Items.COBBLESTONE, 1));
                receipt(s, b, new ItemStack(Items.COBBLESTONE, 1));
                WorkService.requestRollback(level, s, 1); tickRollback(level, s);
                check(level.getBlockState(b).is(Blocks.STONE) && level.getBlockState(a).isAir(), "partial restores only requested block");
                check(s.journal.size() == 1 && s.rollbackRemaining == 0, "partial keeps older receipt");
                check(first.getItem(0).isEmpty() && second.getItem(0).getCount() == 1, "partial debits exactly one item across tracked depots");
                check(second.getItem(1).getCount() == 17, "unrelated chest items survive rollback");
                second.setItem(0, ItemStack.EMPTY);
                WorkService.requestRollback(level, s, 1); tickRollback(level, s);
                check(level.getBlockState(a).isAir() && s.journal.size() == 1 && s.rollbackRemaining == 0, "withdrawn output refuses without losing receipt");
                s.invSlots.add(new ItemStack(Items.COBBLESTONE, 1));
                WorkService.requestRollback(level, s, 1); tickRollback(level, s);
                check(level.getBlockState(a).is(Blocks.STONE) && s.invSlots.isEmpty() && s.journal.isEmpty(), "carried output consumed exactly when restoring block");
                level.setBlock(a, Blocks.AIR.defaultBlockState(), 3);
                receipt(s, a, new ItemStack(Items.COBBLESTONE, 1)); s.invSlots.add(new ItemStack(Items.COBBLESTONE, 1));
                level.setBlock(a, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                WorkService.requestRollback(level, s, 1); tickRollback(level, s);
                check(level.getBlockState(a).is(Blocks.GOLD_BLOCK) && s.invCount() == 1 && s.journal.size() == 1, "player edit never overwritten or debited");
                level.setBlock(a, Blocks.AIR.defaultBlockState(), 3); s.journal.getFirst().dropsKnown = false;
                WorkService.requestRollback(level, s, 1); tickRollback(level, s);
                check(level.getBlockState(a).isAir() && s.invCount() == 1 && s.journal.size() == 1, "legacy unknown drops refuse rollback safely");
                check(WorkService.start(level, "overwrite", a, a, "minecraft:iron_pickaxe") == null && s.invCount() == 1
                        && s.journal.size() == 1, "new job cannot overwrite previous bag or journal");
                check(!WorkService.commit(level, s), "commit refuses nonempty bag");
                s.invSlots.clear(); check(WorkService.commit(level, s) && second.getItem(1).getCount() == 17,
                        "explicit commit releases history and preserves depot contents");
            }
            case "navigationSetup" -> {
                WorkState s = reset(level); s.jobName = null;
                for (int x = 0; x <= 28; x++) for (int z = 0; z <= 18; z++) {
                    level.setBlock(new BlockPos(x, 63, z), Blocks.STONE.defaultBlockState(), 3);
                    for (int y = 64; y <= 69; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
                for (int z = 4; z <= 12; z++) for (int y = 64; y <= 68; y++)
                    level.setBlock(new BlockPos(7, y, z), Blocks.BEDROCK.defaultBlockState(), 3);
                BlockPos target = new BlockPos(12, 64, 8);
                level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
                s = WorkService.start(level, "navigation", target, target, "minecraft:iron_pickaxe");
                check(s != null, "native worker and supported depot spawn");
                LivingEntity bot = (LivingEntity) level.getEntity(UUID.fromString(s.servitorUuid));
                bot.snapTo(2.5, 64, 8.5, 0, 0); navigating = bot.getUUID(); largestDetour = 0;
            }
            case "navigationCheck" -> {
                WorkState s = WorkState.get(level);
                check(largestDetour >= 4.5, "native navigation detours around solid wall");
                check(s.done == 1 && level.getBlockState(new BlockPos(12, 64, 8)).isAir(), "worker reaches and mines target behind wall");
                check(s.finished && s.invSlots.isEmpty() && s.deposited == 1, "worker returns and deposits output");
                check(level.getBlockState(new BlockPos(7, 64, 8)).is(Blocks.BEDROCK), "navigation does not tunnel through obstacle");
                navigating = null;
            }
            case "unreachableSetup" -> {
                WorkState s = reset(level); s.jobName = null;
                BlockPos target = new BlockPos(12, 64, 8);
                level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
                for (int x = 0; x <= 4; x++) for (int z = 6; z <= 10; z++)
                    if (x == 0 || x == 4 || z == 6 || z == 10)
                        for (int y = 64; y <= 68; y++) level.setBlock(new BlockPos(x, y, z), Blocks.BEDROCK.defaultBlockState(), 3);
                s = WorkService.start(level, "unreachable", target, target, "minecraft:iron_pickaxe");
                check(s != null, "unreachable fixture worker spawned");
                // This scenario isolates mining navigation after infrastructure is ready.
                chest(level, s, new BlockPos(10, 64, 6));
                s.hasDepot = true; s.depotX = 10; s.depotY = 64; s.depotZ = 6;
                s.buildingDepot = false; s.depotPlanned = false;
                LivingEntity bot = (LivingEntity) level.getEntity(UUID.fromString(s.servitorUuid));
                bot.snapTo(2.5, 64, 8.5, 0, 0); s.invSlots.add(new ItemStack(Items.DIAMOND, 2));
            }
            case "unreachableCheck" -> {
                WorkState s = WorkState.get(level);
                check(s.finished && s.lastStop.contains("bloque"), "unreachable route pauses instead of teleporting");
                check(level.getBlockState(new BlockPos(12, 64, 8)).is(Blocks.STONE)
                        && s.done == 0 && s.invCount() == 2 && s.journal.isEmpty(), "unreachable route keeps terrain and carried resources");
                check(WorkService.deposit(level) != null, "paused bag can return to depot without restarting excavation");
            }
            case "depositCheck" -> {
                WorkState s = WorkState.get(level);
                check(s.finished && s.invSlots.isEmpty() && s.deposited == 2 && s.done == 0
                        && level.getBlockState(new BlockPos(12, 64, 8)).is(Blocks.STONE), "deposit-only recovery preserves remaining terrain");
            }
            case "restartSetup" -> {
                WorkState s = reset(level);
                ItemStack named = new ItemStack(Items.DIAMOND, 3);
                named.set(DataComponents.CUSTOM_NAME, Component.literal("Persistant")); s.invSlots.add(named);
                s.tool = new ItemStack(Items.IRON_PICKAXE); s.tool.setDamageValue(71);
                s.hasDepot = true; s.depotX = 20; s.depotY = 64; s.depotZ = 24;
                s.depots.add(new BlockPos(20, 64, 24)); s.depots.add(new BlockPos(24, 64, 24));
                s.buildingDepot = true; s.depotPlanned = true; s.planX = 25; s.planY = 64; s.planZ = 26;
                s.depotBuildTicks = 7; s.supplyChests = 61; s.supplySupports = 60;
                receipt(s, new BlockPos(20, 65, 20), named.copy()); s.setDirty();
                check(true, "persistence fixture staged");
            }
            case "restartCheck" -> {
                WorkState s = WorkState.get(level);
                check(s.invCount() == 3 && s.invSlots.getFirst().has(DataComponents.CUSTOM_NAME), "disk restart keeps named carried resources");
                check(s.tool.getDamageValue() == 71, "disk restart keeps tool wear");
                check(s.journal.size() == 1 && s.journal.getFirst().dropsKnown
                        && ItemStack.matches(s.invSlots.getFirst(), s.journal.getFirst().drops.getFirst()), "disk restart keeps exact receipts");
                check(s.depots.size() == 2 && s.finished && s.rollbackRemaining == 0, "disk restart keeps depots and requires explicit resume");
                check(s.buildingDepot && s.depotPlanned && s.planX == 25 && s.depotBuildTicks == 7
                        && s.supplyChests == 61 && s.supplySupports == 60, "disk restart preserves pending depot construction and material counts");
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
