package com.donopot.voiddynasty.work;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Commande de chantier : {@code /work start <nom> <coin1> <coin2>}, {@code status}, {@code stop},
 * {@code rollback [n]}.
 *
 * <p>Le rollback restaure les {@code n} dernières entrées du journal (état d'origine conservé avant
 * chaque destruction), de la plus récente à la plus ancienne.</p>
 */
public final class WorkCommand {

    private WorkCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("work")
                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("start")
                        .then(Commands.argument("nom", StringArgumentType.word())
                                .then(Commands.argument("coin1", BlockPosArgument.blockPos())
                                        .then(Commands.argument("coin2", BlockPosArgument.blockPos())
                                                .executes(WorkCommand::start)))))
                .then(Commands.literal("status").executes(WorkCommand::status))
                .then(Commands.literal("stop").executes(WorkCommand::stop))
                .then(Commands.literal("rollback")
                        .executes(ctx -> rollback(ctx, Integer.MAX_VALUE))
                        .then(Commands.argument("n", IntegerArgumentType.integer(1))
                                .executes(ctx -> rollback(ctx, IntegerArgumentType.getInteger(ctx, "n"))))));
    }

    private static int start(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        String name = StringArgumentType.getString(ctx, "nom");
        BlockPos a = BlockPosArgument.getLoadedBlockPos(ctx, "coin1");
        BlockPos b = BlockPosArgument.getLoadedBlockPos(ctx, "coin2");
        WorkState state = WorkService.start(level, name, a, b);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Chantier « " + name + " » demarre : " + state.total + " positions, ordre haut vers bas."), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkState.get(level);
        if (state.jobName == null) {
            String msg = state.lastStop.isEmpty()
                    ? "Aucun chantier en cours."
                    : "Aucun chantier en cours. Dernier : " + state.lastStop;
            ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
            return 0;
        }
        long remaining = WorkService.remaining(state);
        long processed = Math.max(0, (long) state.total - remaining);
        int pct = state.total == 0 ? 0 : (int) (processed * 100 / state.total);
        String msg = "Chantier « " + state.jobName + " » : " + pct + " % (" + processed + "/" + state.total
                + ") — " + state.done + " casses, " + state.skipped + " laisses"
                + " | budget " + WorkService.budget() + " blocs/tick, "
                + String.format("%.1f", WorkService.averageMs()) + " ms"
                + " | journal " + state.journal.size();
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        boolean stopped = WorkService.stop(level);
        ctx.getSource().sendSuccess(() -> Component.literal(
                stopped ? "Chantier arrete. Journal conserve : /work rollback pour annuler."
                        : "Aucun chantier a arreter."), true);
        return stopped ? 1 : 0;
    }

    private static int rollback(CommandContext<CommandSourceStack> ctx, int count) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkState.get(level);
        int restored = 0;
        int failed = 0;
        while (restored + failed < count && !state.journal.isEmpty()) {
            WorkState.Entry entry = state.journal.remove(state.journal.size() - 1);
            try {
                CompoundTag tag = entry.state;
                BlockState old = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag);
                level.setBlock(entry.pos, old, 3);
                restored++;
            } catch (Exception e) {
                failed++;
            }
        }
        state.setDirty();
        final int r = restored;
        final int f = failed;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Rollback : " + r + " blocs restaures" + (f > 0 ? ", " + f + " echecs" : "")
                        + ". Journal restant : " + state.journal.size()), true);
        return restored;
    }
}
