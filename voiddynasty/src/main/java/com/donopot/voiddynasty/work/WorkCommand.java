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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;

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
 *   /work rollback [n]                    annule les n dernières casses, un bloc par tick
 *   /work commit                          abandonne le rollback, sac vide requis
 *   /work deposit                         vide le sac sans miner d'autre bloc
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
                .then(Commands.literal("deposit").executes(ctx -> {
                    WorkState state = WorkService.deposit(ctx.getSource().getLevel());
                    if (state == null) {
                        ctx.getSource().sendFailure(Component.literal("Depot impossible: sac vide, rollback actif ou trajet de depart bloque."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("Retour au depot uniquement; aucun nouveau bloc ne sera mine."), true);
                    return 1;
                }))
                .then(Commands.literal("commit").executes(ctx -> {
                    WorkState state = WorkState.get(ctx.getSource().getLevel());
                    if (!WorkService.commit(ctx.getSource().getLevel(), state)) {
                        ctx.getSource().sendFailure(Component.literal("Validation refusee: arreter le chantier et vider le sac (/work deposit) d'abord."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal(state.lastStop), true);
                    return 1;
                }))
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
        WorkState state = WorkService.start(level, name, a, b, tool, ctx.getSource().getEntity() == null
                ? new BlockPos(Math.min(a.getX(), b.getX()) - 1, Math.max(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()) - 2)
                : BlockPos.containing(ctx.getSource().getPosition()));
        if (state == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Chantier refuse : " + WorkState.get(level).lastStop + " (voir /work status)."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Chantier « " + name + " » demarre : " + state.total + " positions."
                        + " Depot construit automatiquement par le servitor; outil " + state.toolId.substring(state.toolId.indexOf(':') + 1)
                        + ". " + state.lastStop), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkState.get(level);
        if (state.needsRecovery()) {
            ctx.getSource().sendFailure(Component.literal(state.lastStop + ": " + state.recoveryProblem));
            return 0;
        }
        if (state.jobName == null) {
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
        String msg = "« " + state.jobName + " » " + (state.hasJob() ? "actif " : "arrete ") + pct + " % (" + processed + "/" + state.total + ")"
                + " — " + state.done + " casses, " + state.skipped + " laisses"
                + " | outil " + toolName + " (" + (state.toolBlocks) + " blocs)"
                + " | sac " + state.invSlots.size() + "/" + WorkState.INV_SLOTS
                + " (" + state.invCount() + " obj)"
                + " | " + target
                + " | depot " + state.depositSummary() + " | kit " + state.supplyChests + " coffres/" + state.supplySupports + " supports"
                + " | butin " + state.lootBlocks
                + " | journal " + state.journal.size() + " | rollback " + state.rollbackRemaining
                + " | " + state.lastStop
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
        if (!WorkService.setTool(level, state, id.toString())) {
            ctx.getSource().sendFailure(Component.literal(state.lastStop)); return 0;
        }
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

    private static int rollback(CommandContext<CommandSourceStack> ctx, int count) {
        ServerLevel level = ctx.getSource().getLevel();
        WorkState state = WorkState.get(level);
        if (state.needsRecovery()) {
            ctx.getSource().sendFailure(Component.literal(state.lastStop)); return 0;
        }
        WorkService.requestRollback(level, state, count);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Rollback demande pour " + state.rollbackRemaining + " blocs, un par tick. "
                + "Ressources exactes requises dans le sac ou les depots; /work status pour suivre."), true);
        return 1;
    }
}
