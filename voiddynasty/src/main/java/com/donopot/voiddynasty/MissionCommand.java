package com.donopot.voiddynasty;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /mission — réaffiche l'objectif de la mission en cours.
 */
public final class MissionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mission")
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
                        sp.sendSystemMessage(MissionState.missionMessage());
                        return 1;
                    }
                    ctx.getSource().sendFailure(Component.literal("Commande réservée aux joueurs"));
                    return 0;
                }));
    }
}
