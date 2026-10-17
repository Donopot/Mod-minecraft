package com.donopot.voiddynasty.campagne;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * /campagne — pilote la campagne : état, démarrage, arrêt, remise à zéro, déclenchement d'une zone.
 */
public final class CampagneCommand {

    private CampagneCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("campagne")
                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("etat").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(CampagneService.etat()), false);
                    return 1;
                }))
                .then(Commands.literal("demarrer").executes(ctx -> {
                    String r = CampagneService.demarrer(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(r), true);
                    return 1;
                }))
                .then(Commands.literal("arreter").executes(ctx -> {
                    CampagneService.arreter();
                    ctx.getSource().sendSuccess(() -> Component.literal("campagne arrêtée"), true);
                    return 1;
                }))
                .then(Commands.literal("reset").executes(ctx -> {
                    CampagneService.reset();
                    ctx.getSource().sendSuccess(() -> Component.literal("campagne remise à zéro"), true);
                    return 1;
                }))
                .then(Commands.literal("evaluer").executes(CampagneCommand::evaluer))
                .then(Commands.literal("compteurs").executes(CampagneCommand::compteurs))
                .then(Commands.literal("ouvrir")
                        .then(Commands.argument("mecanisme", IdentifierArgument.id())
                                .executes(CampagneCommand::ouvrir)))
                .then(Commands.literal("declencher")
                        .then(Commands.argument("zone", IdentifierArgument.id())
                                .executes(CampagneCommand::declencher))));
    }

    private static int declencher(CommandContext<CommandSourceStack> ctx) {
        Identifier id = IdentifierArgument.getId(ctx, "zone");
        String nom = id.getPath().replace('/', '_');
        var zone = CampagneDonnees.parId(nom);
        if (zone.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("zone inconnue : " + nom));
            return 0;
        }
        int n = CampagneService.declencherParId(ctx.getSource().getLevel(), zone.get().zone(), null);
        ctx.getSource().sendSuccess(() -> Component.literal(n + " créature(s) invoquée(s) pour " + nom), true);
        return 1;
    }

    private static int ouvrir(CommandContext<CommandSourceStack> ctx) {
        Identifier id = IdentifierArgument.getId(ctx, "mecanisme");
        String tag = id.getPath();
        boolean ouvert = CampagneService.ouvrirMecanisme(ctx.getSource().getLevel(), tag);
        if (!ouvert) {
            ctx.getSource().sendFailure(Component.literal("mécanisme non ouvert : " + tag
                    + " (inconnu, ou son compteur n'est pas encore atteint)"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("mécanisme ouvert : " + tag), true);
        return 1;
    }

    private static int evaluer(CommandContext<CommandSourceStack> ctx) {
        var pos = ctx.getSource().getPosition();
        var declenchees = CampagneService.evaluer(ctx.getSource().getLevel(), pos.x, pos.y, pos.z, null);
        ctx.getSource().sendSuccess(() -> Component.literal(declenchees.isEmpty()
                ? "aucune règle ne répond ici (compteurs : " + CampagneService.compteurs() + ")"
                : String.join(" | ", declenchees)), false);
        return 1;
    }

    private static int compteurs(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(CampagneService.etat()), false);
        ctx.getSource().sendSuccess(() -> Component.literal("compteurs : " + CampagneService.compteurs()), false);
        return 1;
    }
}
