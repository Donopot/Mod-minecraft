package net.mcreator.minepiece.command;

import br.com.oneminepiece.opworldlike.haki.HakiConfig;
import br.com.oneminepiece.opworldlike.haki.HakiProgression;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Commandes Haki (CORE-029).
 *
 * <p>Rend la contre-mesure <b>accessible</b> : port de l'effet au niveau du joueur, entraînement
 * (même chemin d'EXP que le combat, pour tester ou progresser hors combat) et décharge de Haki
 * Souverain. Les niveaux restent persistés et passent par {@link HakiProgression}.</p>
 *
 * <ul>
 *   <li>{@code /haki} ou {@code /haki status} — état des deux Haki et activation du contre</li>
 *   <li>{@code /haki train <armement|observation>} — un gain d'EXP (accessible à tous)</li>
 *   <li>{@code /haki armement <0-10>} / {@code /haki observation <0-10>} — réglage direct (opérateur)</li>
 *   <li>{@code /haki souverain} — décharge de Haki Souverain</li>
 * </ul>
 */
public final class HakiCommands {

   private HakiCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("haki")
            .requires(src -> src.getEntity() instanceof ServerPlayer)
            .executes(HakiCommands::status)
            .then(Commands.literal("status").executes(HakiCommands::status))
            .then(
               Commands.literal("train")
                  .then(
                     Commands.argument("type", StringArgumentType.word())
                        .executes(HakiCommands::train)
                  )
            )
            .then(
               Commands.literal("armement")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                  .then(
                     Commands.argument("niveau", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setLevel(ctx, HakiProgression.Type.ARMAMENT))
                  )
            )
            .then(
               Commands.literal("observation")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                  .then(
                     Commands.argument("niveau", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setLevel(ctx, HakiProgression.Type.OBSERVATION))
                  )
            )
            .then(Commands.literal("souverain").executes(HakiCommands::sovereign))
      );
   }

   private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      return ctx.getSource().getPlayerOrException();
   }

   private static int status(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = player(ctx);
      final String line = HakiProgression.status(player);
      ctx.getSource().sendSuccess(() -> Component.literal("§6").append(Component.translatable("minepiece.message.haki_status", line)), false);
      if (!HakiProgression.attributesAttached(player)) {
         ctx.getSource().sendSuccess(() -> (Component) Component.translatable(
            "minepiece.message.haki_attributes_note"), false);
      }
      return 1;
   }

   private static int train(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = player(ctx);
      String raw = StringArgumentType.getString(ctx, "type").toLowerCase(java.util.Locale.ROOT);
      HakiProgression.Type type = switch (raw) {
         case "armement", "armament", "a" -> HakiProgression.Type.ARMAMENT;
         case "observation", "o" -> HakiProgression.Type.OBSERVATION;
         default -> null;
      };
      if (type == null) {
         ctx.getSource().sendFailure(Component.literal("§c").append(Component.translatable("minepiece.message.haki_type_expected")));
         return 0;
      }
      boolean levelUp = HakiProgression.gainExp(player, type);
      final String line = HakiProgression.status(player);
      ctx.getSource().sendSuccess(() -> (Component) Component.translatable(
         levelUp ? "minepiece.message.haki_levelup" : "minepiece.message.haki_gained", line), false);
      return 1;
   }

   private static int setLevel(CommandContext<CommandSourceStack> ctx, HakiProgression.Type type) throws CommandSyntaxException {
      ServerPlayer player = player(ctx);
      int value = IntegerArgumentType.getInteger(ctx, "niveau");
      HakiProgression.setLevel(player, type, value);
      final String line = HakiProgression.status(player);
      ctx.getSource().sendSuccess(() -> Component.literal("§6").append(Component.translatable("minepiece.message.haki_set", line)), false);
      return 1;
   }

   private static int sovereign(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = player(ctx);
      if (!HakiConfig.enabled()) {
         ctx.getSource().sendFailure(Component.literal("§c").append(Component.translatable("minepiece.message.haki_disabled")));
         return 0;
      }
      int affected = HakiProgression.sovereignBurst(player);
      ctx.getSource().sendSuccess(() -> (Component) Component.translatable(
         "minepiece.message.sovereign", affected, (int) HakiConfig.sovereignRadius()), false);
      return 1;
   }
}
