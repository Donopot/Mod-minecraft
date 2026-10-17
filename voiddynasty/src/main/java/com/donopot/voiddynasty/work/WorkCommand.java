package com.donopot.voiddynasty.work;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Commandes de chantier.
 *
 * <pre>
 *   /work start &lt;nom&gt; &lt;coin1&gt; &lt;coin2&gt;   démarre (pioche en fer par défaut)
 *   /work status                          avancement, outil, inventaire, servitor
 *   /work stop                            arrête, journal conservé
 *   /work resume                          reprend au curseur après un arrêt
 *   /work tool &lt;item&gt;                     change l'outil du Servitor
 *   /work loot                            relève des conteneurs rencontrés
 *   /work rollback [n]                    annule les n dernières casses
 * </pre>
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
                .then(Commands.literal("resume").executes(WorkCommand::resume))
                .then(Commands.literal("loot").executes(WorkCommand::loot))
                .then(Commands.literal("tool")
                        .then(Commands.argument("item", StringArgumentType.string())
                                .executes(WorkCommand::tool)))
                .then(Commands.literal("rollback")
                        .executes(ctx -> rollback(ctx, Integer.MAX_VALUE))
                        .then(Commands.argument("n", IntegerArgumentType.integer(1))
                                .executes(ctx -> rollback(ctx, IntegerArgumentType.getInteger(ctx, "n"))))));
    }

    private static int start(CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        String name = StringArgumentType.getString(ctx, "nom");
        BlockPos a = BlockPosArgument.getLoadedBlockPos(ctx, "coin1");
        BlockPos b = BlockPosArgument.getLoadedBlockPos(ctx, "coin2");
        String tool = BuiltInRegistries.ITEM.getKey(Items.IRON_PICKAXE).toString();
        WorkState state = WorkService.start(level, name, a, b, tool);
        if (state == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Chantier refuse : impossible de poser le depot ou de faire apparaitre le Servitor."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Chantier « " + name + " » demarre : " + state.total + " positions."
                        + " Servitor en " + state.depotX + " " + state.depotY + " " + state.depotZ
                        + " avec " + state.toolId.substring(state.toolId.indexOf(':') + 1)
                        + " | depot au meme endroit."), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkState.get(level);
        if (!state.hasJob()) {
            String msg = state.lastStop.isEmpty()
                    ? "Aucun chantier en cours."
                    : "Aucun chantier en cours. Dernier : " + state.lastStop
                            + (state.jobName != null ? " — /work resume pour continuer" : "");
            ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
            return 0;
        }
        long remaining = WorkService.remaining(state);
        long processed = Math.max(0, (long) state.total - remaining);
        int pct = state.total == 0 ? 0 : (int) (processed * 100 / state.total);
        String toolName = state.toolId.isEmpty() ? "mains nues" : state.toolId.substring(state.toolId.indexOf(':') + 1);
        String target = state.hasTarget
                ? "cible " + state.targetX + " " + state.targetY + " " + state.targetZ
                        + String.format(" (%.1fs)", state.progress)
                : "pas de cible";
        String msg = "« " + state.jobName + " » " + pct + " % (" + processed + "/" + state.total + ")"
                + " — " + state.done + " casses, " + state.skipped + " laisses"
                + " | outil " + toolName + " (" + (state.toolBlocks) + " blocs)"
                + " | sac " + state.invSlots.size() + "/" + WorkState.INV_SLOTS
                + " (" + state.invCount() + " obj)"
                + " | " + target
                + " | depot " + state.depositSummary()
                + " | butin " + state.lootBlocks
                + " | journal " + state.journal.size()
                + " | " + String.format("%.2f", WorkService.lastMs()) + " ms/tick";
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static int resume(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkService.resume(level);
        if (state == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Rien a reprendre : aucun chantier interrompu avec un journal."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Chantier « " + state.jobName + " » repris au curseur (" + state.cursorX + " " + state.cursorY
                        + " " + state.cursorZ + "), journal conserve : " + state.journal.size() + " blocs."), true);
        return 1;
    }

    private static int tool(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkState.get(level);
        String raw = StringArgumentType.getString(ctx, "item");
        Identifier id = Identifier.tryParse(raw.contains(":") ? raw : "minecraft:" + raw);
        if (id == null || BuiltInRegistries.ITEM.getValue(id) == null
                || BuiltInRegistries.ITEM.getValue(id) == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Outil inconnu : " + raw));
            return 0;
        }
        WorkService.setTool(level, state, id.toString());
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Outil du Servitor : " + id + " (usure remise a zero)."), true);
        return 1;
    }

    private static int loot(CommandContext<CommandSourceStack> ctx) {
        WorkState state = WorkState.get(ctx.getSource().getLevel());
        if (state.lootBlocks == 0) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Aucun conteneur rencontre. Les coffres sont releves, pas ouverts a la main."), false);
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Butin releve (" + state.lootBlocks + " conteneurs, rien de pris) :"), false);
        for (String line : state.lootLog) {
            ctx.getSource().sendSuccess(() -> Component.literal("  " + line), false);
        }
        return state.lootBlocks;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        boolean stopped = WorkService.stop(level);
        ctx.getSource().sendSuccess(() -> Component.literal(
                stopped ? "Chantier arrete. Journal conserve : /work rollback pour annuler, /work resume pour reprendre."
                        : "Aucun chantier a arreter."), true);
        return stopped ? 1 : 0;
    }

    /** Vide le dépôt ; renvoie le nombre d'objets retirés. */
    private static int clearDepot(ServerLevel level, WorkState state) {
        if (!state.hasDepot) {
            return 0;
        }
        BlockPos depotPos = new BlockPos(state.depotX, state.depotY, state.depotZ);
        if (!(level.getBlockEntity(depotPos) instanceof net.minecraft.world.Container depot)) {
            return 0;
        }
        int removed = 0;
        for (int i = 0; i < depot.getContainerSize(); i++) {
            ItemStack stack = depot.getItem(i);
            if (!stack.isEmpty()) {
                removed += stack.getCount();
                depot.setItem(i, ItemStack.EMPTY);
            }
        }
        depot.setChanged();
        return removed;
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
        // Les blocs reviennent, donc les objets qu'ils ont produits aussi : le dépôt est vidé.
        int cleared = clearDepot(level, state);
        state.deposited = 0;
        state.setDirty();
        final int r = restored;
        final int f = failed;
        final int c = cleared;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Rollback : " + r + " blocs restaures" + (f > 0 ? ", " + f + " echecs" : "")
                        + ", depot vide de " + c + " objets (sinon duplication)"
                        + ". Journal restant : " + state.journal.size()), true);
        return restored;
    }
}
