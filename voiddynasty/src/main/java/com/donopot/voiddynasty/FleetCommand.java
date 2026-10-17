package com.donopot.voiddynasty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * /fleet list | check <type> [place <x> <y> <z>] | add <type> <nom> | deploy <nom> | stow <nom>
 *        | teleport <nom> <x> <z> | rename <nom> <nouveau> | delete <nom>
 * Types : emperor | frigate | ruin | titan
 */
public final class FleetCommand {

    private static final String TYPES = "emperor | frigate | ruin | titan | andromeda | executor";

    private FleetCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fleet")
                .then(Commands.literal("list").executes(FleetCommand::list))
                .then(Commands.literal("check")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .executes(FleetCommand::check)
                                .then(Commands.literal("place")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                .executes(FleetCommand::checkPlace)))))))
                .then(Commands.literal("add")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(FleetCommand::add))))
                .then(Commands.literal("deploy")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(FleetCommand::deploy)))
                .then(Commands.literal("stow")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(FleetCommand::stow)))
                .then(Commands.literal("teleport")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(FleetCommand::teleport)))))
                .then(Commands.literal("rename")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .then(Commands.argument("newName", StringArgumentType.greedyString())
                                        .executes(FleetCommand::rename))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(FleetCommand::delete)))
        );
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    private static ShipType parseType(String s) {
        try {
            return ShipType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        List<ShipData> ships = FleetManager.list(player(ctx));
        if (ships.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Aucun vaisseau — /fleet add <" + TYPES + "> <nom>"), false);
            return 1;
        }
        for (ShipData s : ships) {
            String st = s.isDeployed()
                    ? "DÉPLOYÉ @" + s.base.getX() + "," + s.base.getY() + "," + s.base.getZ()
                    : "rangé";
            String line = "• " + s.name + " (" + s.type.name().toLowerCase() + ") — " + st;
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int check(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ShipType type = parseType(StringArgumentType.getString(ctx, "type"));
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Type inconnu — " + TYPES));
            return 0;
        }
        for (String line : FleetManager.templateCheck(type)) {
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int checkPlace(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ShipType type = parseType(StringArgumentType.getString(ctx, "type"));
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Type inconnu — " + TYPES));
            return 0;
        }
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        String r = FleetManager.templatePlace(ctx.getSource().getLevel(), type, new BlockPos(x, y, z));
        ctx.getSource().sendSuccess(() -> Component.literal(r), false);
        return 1;
    }

    private static int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ShipType type = parseType(StringArgumentType.getString(ctx, "type"));
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Type inconnu — " + TYPES));
            return 0;
        }
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer p = player(ctx);
        ctx.getSource().sendSuccess(() -> Component.literal(FleetManager.add(p, type, name)), false);
        return 1;
    }

    private static int deploy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = player(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        BlockPos t = new BlockPos(p.getBlockX(), p.getBlockY() + 40, p.getBlockZ());
        ctx.getSource().sendSuccess(() -> Component.literal(FleetManager.deploy(p, name, t)), false);
        return 1;
    }

    private static int stow(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer p = player(ctx);
        ctx.getSource().sendSuccess(() -> Component.literal(FleetManager.stow(p, name)), false);
        return 1;
    }

    private static int teleport(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = player(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        int y = p.getBlockY();
        for (ShipData s : FleetManager.list(p)) {
            if (s.name.equalsIgnoreCase(name) && s.isDeployed()) {
                y = s.base.getY();
                break;
            }
        }
        BlockPos t = new BlockPos(x, y, z);
        ctx.getSource().sendSuccess(() -> Component.literal(FleetManager.teleport(p, name, t)), false);
        return 1;
    }

    private static int rename(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        String newName = StringArgumentType.getString(ctx, "newName");
        ServerPlayer p = player(ctx);
        ctx.getSource().sendSuccess(() -> Component.literal(FleetManager.rename(p, name, newName)), false);
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer p = player(ctx);
        ctx.getSource().sendSuccess(() -> Component.literal(FleetManager.delete(p, name)), false);
        return 1;
    }
}
