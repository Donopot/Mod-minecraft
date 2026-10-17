package net.mcreator.minepiece.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mcreator.minepiece.item.DevilFruitItem;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.mcreator.minepiece.procedures.DevilFruitDebug;
import net.mcreator.minepiece.procedures.DevilFruitSummons;
import net.mcreator.minepiece.procedures.DevilFruitSkillDispatcher;
import net.mcreator.minepiece.procedures.DevilFruitTransformation;
import net.mcreator.minepiece.procedures.PowerBlockService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.mcreator.minepiece.entity.SummonEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * Commandes de test du système de pouvoir : {@code /devilfruit info|skill|give <id>|clear}.
 * Utile pour piloter la couche pouvoir en headless (RCON) en attendant les keybinds client.
 */
public final class DevilFruitCommands {
   private DevilFruitCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("devilfruit")
            .requires(src -> src.getEntity() instanceof ServerPlayer)
            .executes(DevilFruitCommands::info)
            .then(Commands.literal("info").executes(DevilFruitCommands::info))
            .then(Commands.literal("skill").executes(DevilFruitCommands::skill))
            .then(Commands.literal("transform").executes(DevilFruitCommands::transform))
            .then(Commands.literal("debug").executes(DevilFruitCommands::debug))
            .then(Commands.literal("clear").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(DevilFruitCommands::clear))
            .then(
               Commands.literal("give")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                  .then(
                     Commands.argument("fruit", StringArgumentType.word())
                        .executes(DevilFruitCommands::give)
                  )
            )
            .then(
               // FRUIT-BISU_BISU-04 : ordres donnés aux invocations, réservés à leur propriétaire.
               Commands.literal("order")
                  .then(Commands.literal("suivre").executes(ctx -> order(ctx, SummonEntity.Order.SUIVRE)))
                  .then(Commands.literal("defendre").executes(ctx -> order(ctx, SummonEntity.Order.DEFENDRE)))
                  .then(Commands.literal("attaquer").executes(ctx -> order(ctx, SummonEntity.Order.ATTAQUER)))
            )
      );

      // Commande de test CORE-020/030 : pose un bloc temporaire (diamant) — accessible depuis la console.
      dispatcher.register(
         Commands.literal("dfblock")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
               Commands.argument("x", IntegerArgumentType.integer())
                  .then(
                     Commands.argument("y", IntegerArgumentType.integer())
                        .then(
                           Commands.argument("z", IntegerArgumentType.integer())
                              .then(
                                 Commands.argument("ticks", IntegerArgumentType.integer())
                                    .executes(DevilFruitCommands::tempblock)
                              )
                        )
                  )
            )
      );

      // Commande de test CORE-021/CORE-024 : spawn une invocation (IA) à une position — console.
      // model/texture optionnels (défaut entity_gomu_arm/luffy) → permet l'essai de rendu par technique.
      dispatcher.register(
         Commands.literal("dfsummon")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
               Commands.argument("x", IntegerArgumentType.integer())
                  .then(
                     Commands.argument("y", IntegerArgumentType.integer())
                        .then(
                           Commands.argument("z", IntegerArgumentType.integer())
                              .then(
                                 Commands.argument("ticks", IntegerArgumentType.integer())
                                    .executes(DevilFruitCommands::dfsummon)
                                    .then(
                                       Commands.argument("model", StringArgumentType.word())
                                          .executes(DevilFruitCommands::dfsummon)
                                          .then(
                                             Commands.argument("texture", StringArgumentType.word())
                                                .executes(DevilFruitCommands::dfsummon)
                                          )
                                    )
                              )
                        )
                  )
            )
      );

      // Commande de test CORE-024 : affiche un visuel GeoLib (modèle + texture) devant le joueur
      // pour l'essai de rendu technique par technique.
      dispatcher.register(
         Commands.literal("dfvisual")
            .requires(src -> src.getEntity() instanceof ServerPlayer)
            .then(
               Commands.argument("model", StringArgumentType.word())
                  .then(
                     Commands.argument("texture", StringArgumentType.word())
                        .executes(DevilFruitCommands::dfvisual)
                  )
            )
      );
   }

   /** Argument optionnel (Brigadier) : renvoie {@code def} si l'argument n'a pas été fourni. */
   private static <T> T opt(CommandContext<CommandSourceStack> ctx, String name, T def, Class<T> type) {
      try {
         return ctx.getArgument(name, type);
      } catch (IllegalArgumentException e) {
         return def;
      }
   }

   private static int dfsummon(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerLevel level = ctx.getSource().getLevel();
      BlockPos pos = new BlockPos(
         IntegerArgumentType.getInteger(ctx, "x"),
         IntegerArgumentType.getInteger(ctx, "y"),
         IntegerArgumentType.getInteger(ctx, "z")
      );
      int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
      String model = opt(ctx, "model", "entity_gomu_arm", String.class);
      String texture = opt(ctx, "texture", "luffy", String.class);
      net.mcreator.minepiece.entity.SummonEntity e =
         net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(level, pos, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (e != null) {
         e.setDamage(6.0);
         e.setModel(model);
         e.setTexture(texture);
         e.lifetime(ticks);
         ctx.getSource().sendSuccess(() -> Component.literal(
            "§aInvocation spawnée à " + pos + " (" + ticks + " ticks) — modèle " + model + " / texture " + texture + "."), false);
      } else {
         ctx.getSource().sendFailure(Component.literal("§c").append(Component.translatable("minepiece.message.spawn_failed")));
         return 0;
      }
      return 1;
   }

   /** CORE-024 : affiche un visuel GeoLib devant le joueur (essai de rendu par technique). */
   private static int dfvisual(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = (ServerPlayer) ctx.getSource().getEntity();
      String model = StringArgumentType.getString(ctx, "model");
      String texture = StringArgumentType.getString(ctx, "texture");
      ServerLevel level = (ServerLevel) player.level();
      net.mcreator.minepiece.entity.SummonEntity e =
         net.mcreator.minepiece.init.MinepieceModEntities.SUMMON.spawn(level, player.blockPosition(), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
      if (e != null) {
         e.setDamage(0.0);
         e.setModel(model);
         e.setTexture(texture);
         e.lifetime(200);
         net.minecraft.world.phys.Vec3 look = player.getLookAngle();
         e.setPos(player.getX() + look.x * 2.0, player.getEyeY() - 0.5, player.getZ() + look.z * 2.0);
         e.setYRot(player.getYRot());
         ctx.getSource().sendSuccess(() -> Component.literal(
            "§aVisuel affiché : " + model + " + " + texture + " (10 s)."), false);
      } else {
         ctx.getSource().sendFailure(Component.literal("§c").append(Component.translatable("minepiece.message.visual_failed")));
         return 0;
      }
      return 1;
   }

   private static int tempblock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerLevel level = ctx.getSource().getLevel();
      BlockPos pos = new BlockPos(
         IntegerArgumentType.getInteger(ctx, "x"),
         IntegerArgumentType.getInteger(ctx, "y"),
         IntegerArgumentType.getInteger(ctx, "z")
      );
      int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
      PowerBlockService.place(level, pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), ticks, null);
      ctx.getSource().sendSuccess(
         () -> Component.literal("§a").append(Component.translatable("minepiece.message.temp_block",
            pos.toString(), ticks, PowerBlockService.activeCount())),
         false
      );
      return 1;
   }

   private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      PlayerVariables vars = MinepieceModVariables.get(player);
      String fruit = vars.PlayerDevilFruit.isEmpty() ? "aucun" : vars.PlayerDevilFruit.getItem().getDescriptionId();
      ctx.getSource().sendSuccess(
         () -> Component.literal("§6").append(Component.translatable("minepiece.message.power_status",
            fruit.toString(), (int) vars.PlayerEnergy, (int) vars.PlayerEnergyMAX)),
         false
      );
      // FRUIT-*-02 : la famille et la capacité de transformation sont rendues observables ici —
      // ce sont les tags qui décident, plus une liste codée en dur.
      if (!vars.PlayerDevilFruit.isEmpty()) {
         String famille = DevilFruitPowerLogic.family(vars.PlayerDevilFruit.getItem()).name();
         boolean transformable = vars.PlayerDevilFruit.is(DevilFruitTransformation.TAG_TRANSFORMABLE);
         ctx.getSource().sendSuccess(
            () -> Component.literal("§6").append(Component.translatable("minepiece.message.power_family",
               famille, transformable ? "oui" : "non")),
            false
         );
      }
      return 1;
   }

   private static int skill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      DevilFruitSkillDispatcher.castSkill(player);
      return 1;
   }

   /** Allume ou éteint le témoin de diagnostic dans le chat. */
   private static int debug(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      boolean allume = DevilFruitDebug.basculer(player);
      ctx.getSource().sendSuccess(() -> Component.literal(allume
         ? "§aTémoin allumé : chaque technique lancée écrit dans le chat le nom de la technique, son type"
            + " et le geste d'animation réellement demandé. Appuie sur R, puis lis le chat."
         : "§7Témoin éteint."), false);
      return 1;
   }

   private static int transform(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      DevilFruitTransformation.toggle(player);
      return 1;
   }

   private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      int summons = DevilFruitPowerLogic.resetDevilFruitPowers(player);
      ctx.getSource().sendSuccess(() -> Component.literal("§7").append(
         Component.translatable("minepiece.message.power_removed")), false);
      ctx.getSource().sendSuccess(() -> Component.literal("§8").append(
         Component.translatable("minepiece.message.reset_report", "0", "vide", "0", "0", summons)), false);
      return 1;
   }

   /**
    * Portée maximale des ordres (blocs). Un ordre ne porte que sur les invocations proches et
    * <b>du propriétaire</b> : impossible de commander les soldats d'un autre joueur.
    */
   private static final double ORDER_RADIUS = 32.0;

   /** Applique un ordre aux invocations du joueur dans {@link #ORDER_RADIUS} blocs. */
   private static int order(CommandContext<CommandSourceStack> ctx, SummonEntity.Order wanted)
         throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      // La règle de propriété et la borne de portée vivent dans DevilFruitSummons : la commande et
      // les tests exercent ainsi exactement le même code.
      int applied = DevilFruitSummons.applyOrder(player, wanted, ORDER_RADIUS);
      final int count = applied;
      ctx.getSource().sendSuccess(() -> Component.literal("§b").append(
         Component.translatable("minepiece.message.order_applied", count, wanted.name().toLowerCase())), false);
      return applied;
   }

   private static int give(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      String name = StringArgumentType.getString(ctx, "fruit");
      Identifier id = Identifier.fromNamespaceAndPath("minepiece", name);
      if (!BuiltInRegistries.ITEM.containsKey(id)) {
         ctx.getSource().sendFailure(Component.literal("§c").append(Component.translatable("minepiece.message.unknown_fruit", "minepiece:" + name)));
         return 0;
      }
      Item item = BuiltInRegistries.ITEM.getValue(id);
      if (!(item instanceof DevilFruitItem)) {
         ctx.getSource().sendFailure(Component.literal("§c").append(Component.translatable("minepiece.message.not_a_fruit", "minepiece:" + name)));
         return 0;
      }
      player.getInventory().add(new ItemStack(item));
      ctx.getSource().sendSuccess(() -> Component.literal("§a").append(Component.translatable("minepiece.message.fruit_given", name)), false);
      return 1;
   }
}
