package br.com.oneminepiece.opworldlike.command;

import br.com.oneminepiece.opworldlike.OmpMessages;
import br.com.oneminepiece.opworldlike.claim.IslandAccess;
import br.com.oneminepiece.opworldlike.claim.IslandClaim;
import br.com.oneminepiece.opworldlike.claim.IslandClaimsData;
import br.com.oneminepiece.opworldlike.claim.IslandFlag;
import br.com.oneminepiece.opworldlike.claim.ScoreTarget;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import br.com.oneminepiece.opworldlike.spawn.PlayerSpawnData;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.IslandSize;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import br.com.oneminepiece.opworldlike.worldgen.RegionType;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class IslandCommands {
   private IslandCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                "island"
                                             )
                                             .executes(IslandCommands::info))
                                          .then(
                                             ((LiteralArgumentBuilder)Commands.literal("info").executes(IslandCommands::info))
                                                .then(islandArgument().executes(IslandCommands::info))
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)Commands.literal("score").executes(IslandCommands::score))
                                             .then(islandArgument().executes(IslandCommands::score))
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("top").executes(IslandCommands::top))
                                          .then(islandArgument().executes(IslandCommands::top))
                                    ))
                                 .then(Commands.literal("tp").then(islandArgument().executes(IslandCommands::teleport))))
                              .then(
                                 Commands.literal("trust")
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("player", EntityArgument.player()).executes(IslandCommands::trust))
                                          .then(islandArgument().executes(IslandCommands::trust))
                                    )
                              ))
                           .then(
                              Commands.literal("untrust")
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("player", EntityArgument.player()).executes(IslandCommands::untrust))
                                       .then(islandArgument().executes(IslandCommands::untrust))
                                 )
                           ))
                        .then(
                           Commands.literal("trustcrew")
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("crew", StringArgumentType.word()).executes(IslandCommands::trustCrew))
                                    .then(islandArgument().executes(IslandCommands::trustCrew))
                              )
                        ))
                     .then(
                        Commands.literal("untrustcrew")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("crew", StringArgumentType.word()).executes(IslandCommands::untrustCrew))
                                 .then(islandArgument().executes(IslandCommands::untrustCrew))
                           )
                     ))
                  .then(
                     Commands.literal("flag")
                        .then(
                           Commands.argument("flag", StringArgumentType.word())
                              .suggests((context, builder) -> SharedSuggestionProvider.suggest(flagIds(), builder))
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("value", BoolArgumentType.bool()).executes(IslandCommands::flag))
                                    .then(islandArgument().executes(IslandCommands::flag))
                              )
                        )
                  ))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("message")
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("enter")
                                       .then(Commands.argument("text", StringArgumentType.greedyString()).executes(IslandCommands::messageEnter)))
                                    .then(
                                       Commands.literal("island")
                                          .then(
                                             islandArgument()
                                                .then(Commands.argument("text", StringArgumentType.greedyString()).executes(IslandCommands::messageEnter))
                                          )
                                    ))
                                 .then(
                                    Commands.literal("ilha")
                                       .then(
                                          islandArgument()
                                             .then(Commands.argument("text", StringArgumentType.greedyString()).executes(IslandCommands::messageEnter))
                                       )
                                 )
                           ))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("exit")
                                    .then(Commands.argument("text", StringArgumentType.greedyString()).executes(IslandCommands::messageExit)))
                                 .then(
                                    Commands.literal("island")
                                       .then(
                                          islandArgument()
                                             .then(Commands.argument("text", StringArgumentType.greedyString()).executes(IslandCommands::messageExit))
                                       )
                                 ))
                              .then(
                                 Commands.literal("ilha")
                                    .then(
                                       islandArgument()
                                          .then(Commands.argument("text", StringArgumentType.greedyString()).executes(IslandCommands::messageExit))
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("clear")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("target", StringArgumentType.word())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(messageTargets(), builder))
                                    .executes(IslandCommands::messageClear))
                                 .then(islandArgument().executes(IslandCommands::messageClear))
                           )
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                          "admin"
                                       )
                                       .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("clear").executes(IslandCommands::adminClear))
                                          .then(islandArgument().executes(IslandCommands::adminClear))
                                    ))
                                 .then(Commands.literal("mapstats").executes(IslandCommands::adminMapStats)))
                              .then(Commands.literal("review").executes(IslandCommands::adminReview)))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("addscore")
                                       .then(
                                          Commands.literal("player")
                                             .then(
                                                Commands.argument("playerTarget", StringArgumentType.word())
                                                   .then(
                                                      ((RequiredArgumentBuilder)Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                            .executes(IslandCommands::adminAddPlayerScore))
                                                         .then(islandArgument().executes(IslandCommands::adminAddPlayerScore))
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("crew")
                                          .then(
                                             Commands.argument("crew", StringArgumentType.word())
                                                .then(
                                                   ((RequiredArgumentBuilder)Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                         .executes(IslandCommands::adminAddCrewScore))
                                                      .then(islandArgument().executes(IslandCommands::adminAddCrewScore))
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.argument("crew", StringArgumentType.word())
                                       .then(
                                          ((RequiredArgumentBuilder)Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                .executes(IslandCommands::adminAddCrewScore))
                                             .then(islandArgument().executes(IslandCommands::adminAddCrewScore))
                                       )
                                 )
                           ))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("transfer")
                                    .then(
                                       Commands.literal("player")
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("playerTarget", StringArgumentType.word())
                                                   .executes(IslandCommands::adminTransferPlayer))
                                                .then(islandArgument().executes(IslandCommands::adminTransferPlayer))
                                          )
                                    ))
                                 .then(
                                    Commands.literal("crew")
                                       .then(
                                          ((RequiredArgumentBuilder)Commands.argument("crew", StringArgumentType.word())
                                                .executes(IslandCommands::adminTransferCrew))
                                             .then(islandArgument().executes(IslandCommands::adminTransferCrew))
                                       )
                                 ))
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("crew", StringArgumentType.word()).executes(IslandCommands::adminTransferCrew))
                                    .then(islandArgument().executes(IslandCommands::adminTransferCrew))
                              )
                        ))
                     .then(Commands.literal("spawninfo").then(Commands.argument("player", EntityArgument.player()).executes(IslandCommands::adminSpawnInfo))))
                  .then(
                     Commands.literal("rerollspawn").then(Commands.argument("player", EntityArgument.player()).executes(IslandCommands::adminRerollSpawn))
                  )
            )
      );
   }

   public static void registerTeleportOnly(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("island").then(Commands.literal("tp").then(islandArgument().executes(IslandCommands::teleport)))
      );
   }

   private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      if (islandArgumentText(context).isEmpty() && OnePieceGeography.isReverseMountainArea(player.getBlockX(), player.getBlockZ())) {
         send(
            context,
            "Ilha: Reverse Mountain | Tema: REVERSE_MOUNTAIN | Claim: nao | Regiao: RED_LINE/GRAND_LINE",
            "Island: Reverse Mountain | Theme: REVERSE_MOUNTAIN | Claim: no | Region: RED_LINE/GRAND_LINE"
         );
         send(
            context,
            "Canais navegaveis fisicos; claims e pontuacao ficam desativados nesta area.",
            "Physical navigable channels; claims and scoring are disabled in this area."
         );
         return 1;
      }

      IslandDefinition island = targetIsland(context, player).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context);
         return 0;
      }

      IslandClaim claim = IslandClaimsData.get(player.level()).get(island.id());
      send(
         context,
         "Ilha: " + islandName(context, island) + " | Tema: " + island.theme() + " | Claim: " + (island.claimable() ? "sim" : "nao"),
         "Island: " + islandName(context, island) + " | Theme: " + island.theme() + " | Claim: " + (island.claimable() ? "yes" : "no")
      );
      send(
         context,
         "Regiao: " + island.region() + " | Tipo: " + island.size() + " | Bioma: " + island.biomeProfile(),
         "Region: " + island.region() + " | Type: " + island.size() + " | Biome: " + island.biomeProfile()
      );
      send(
         context,
         "Centro: " + island.centerX() + ", " + island.centerZ() + " | Raio: " + island.radius(),
         "Center: " + island.centerX() + ", " + island.centerZ() + " | Radius: " + island.radius()
      );
      if (!island.claimable()) {
         send(context, "Dono: claims desativados nesta ilha.", "Owner: claims disabled on this island.");
         return 1;
      }

      if (claim != null && claim.hasOwner()) {
         send(context, "Dono: " + label(context, claim.ownerLabel()), "Owner: " + label(context, claim.ownerLabel()));
      } else {
         send(
            context,
            "Dono: sem dono. Primeiro jogador ou tripulacao com 100 pontos conquista a ilha.",
            "Owner: unclaimed. First player or crew with 100 points conquers the island."
         );
      }

      return 1;
   }

   private static int score(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = targetIsland(context, player).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context);
         return 0;
      } else if (!island.claimable()) {
         send(context, "Esta ilha nao aceita claim nem pontuacao.", "This island does not accept claims or scoring.");
         return 0;
      } else {
         ScoreTarget target = scoreTarget(player);
         IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
         double score = claim.scoreFor(target);
         send(
            context,
            "Sua pontuacao nesta ilha (" + label(context, target.label()) + "): " + format(score),
            "Your score on this island (" + label(context, target.label()) + "): " + format(score)
         );
         return 1;
      }
   }

   private static int top(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = targetIsland(context, player).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context);
         return 0;
      }

      if (!island.claimable()) {
         send(context, "Esta ilha nao aceita claim nem ranking de pontuacao.", "This island does not accept claims or score rankings.");
         return 0;
      }

      IslandClaim claim = IslandClaimsData.get(player.level()).get(island.id());
      if (claim != null && !claim.scores.isEmpty()) {
         send(context, "Top crews de " + islandName(context, island) + ":", "Top crews for " + islandName(context, island) + ":");
         List<Entry<String, Double>> top = claim.topScores();

         for (int i = 0; i < Math.min(5, top.size()); i++) {
            Entry<String, Double> entry = top.get(i);
            String scoreLabel = label(context, claim.scoreLabel(entry.getKey()));
            send(context, i + 1 + ". " + scoreLabel + " - " + format(entry.getValue()));
         }

         return 1;
      } else {
         send(context, "Ainda nao ha pontuacao nesta ilha.", "There is no score on this island yet.");
         return 0;
      }
   }

   private static int teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      ServerLevel overworld = ((CommandSourceStack)context.getSource()).getServer().overworld();
      IslandDefinition island = targetIsland(context, player).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context);
         return 0;
      }

      BlockPos pos = safeSpawnInLevel(overworld, island);
      if (player.getVehicle() != null) {
         player.stopRiding();
      }

      double x = pos.getX() + 0.5;
      double y = pos.getY();
      double z = pos.getZ() + 0.5;
      if (player.level().dimension() == Level.OVERWORLD) {
         player.teleportTo(x, y, z);
      } else {
         player.teleportTo(overworld, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
      }

      send(
         context,
         "Teleportado para " + islandName(context, island) + " em X " + pos.getX() + ", Y " + pos.getY() + ", Z " + pos.getZ() + ".",
         "Teleported to " + islandName(context, island) + " at X " + pos.getX() + ", Y " + pos.getY() + ", Z " + pos.getZ() + "."
      );
      return 1;
   }

   private static int trust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      }

      IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
      claim.trustedPlayers.add(target.getUUID());
      IslandClaimsData.get(player.level()).setDirty();
      send(context, "Trust adicionado para " + target.getGameProfile().name() + ".", "Trust added for " + target.getGameProfile().name() + ".");
      return 1;
   }

   private static int untrust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      }

      IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
      claim.trustedPlayers.remove(target.getUUID());
      IslandClaimsData.get(player.level()).setDirty();
      send(context, "Trust removido de " + target.getGameProfile().name() + ".", "Trust removed from " + target.getGameProfile().name() + ".");
      return 1;
   }

   private static int flag(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      } else {
         IslandFlag flag = IslandFlag.byId(StringArgumentType.getString(context, "flag"));
         if (flag == null) {
            send(context, "Flag invalida.", "Invalid flag.");
            return 0;
         } else {
            boolean value = BoolArgumentType.getBool(context, "value");
            IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
            claim.flags.put(flag, value);
            IslandClaimsData.get(player.level()).setDirty();
            send(context, "Flag " + flag.id() + " definida como " + value + ".", "Flag " + flag.id() + " set to " + value + ".");
            return 1;
         }
      }
   }

   private static int trustCrew(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      }

      String crew = normalizeCrewArg(StringArgumentType.getString(context, "crew"));
      IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
      claim.trustedCrews.add(crew);
      IslandClaimsData.get(player.level()).setDirty();
      send(context, "Trust de crew adicionado para " + crew + ".", "Crew trust added for " + crew + ".");
      return 1;
   }

   private static int untrustCrew(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      }

      String crew = normalizeCrewArg(StringArgumentType.getString(context, "crew"));
      IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
      claim.trustedCrews.remove(crew);
      IslandClaimsData.get(player.level()).setDirty();
      send(context, "Trust de crew removido de " + crew + ".", "Crew trust removed from " + crew + ".");
      return 1;
   }

   private static int messageEnter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      return setTerritoryMessage(context, true, StringArgumentType.getString(context, "text"));
   }

   private static int messageExit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      return setTerritoryMessage(context, false, StringArgumentType.getString(context, "text"));
   }

   private static int setTerritoryMessage(CommandContext<CommandSourceStack> context, boolean entry, String rawText) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      }

      String text = sanitizeMessage(rawText);
      if (text.isBlank()) {
         send(context, "Mensagem vazia.", "Empty message.");
         return 0;
      }

      IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
      if (entry) {
         claim.entryMessage = text;
      } else {
         claim.exitMessage = text;
      }

      IslandClaimsData.get(player.level()).setDirty();
      send(context, (entry ? "Mensagem de entrada" : "Mensagem de saida") + " definida.", (entry ? "Entry message" : "Exit message") + " set.");
      return 1;
   }

   private static int messageClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      IslandDefinition island = requireManagedIsland(context, player);
      if (island == null) {
         return 0;
      }

      String target = StringArgumentType.getString(context, "target").toLowerCase(Locale.ROOT);
      IslandClaim claim = IslandClaimsData.get(player.level()).getOrCreate(island);
      boolean changed = false;
      if ("enter".equals(target) || "entrada".equals(target) || "all".equals(target) || "tudo".equals(target)) {
         claim.entryMessage = "";
         changed = true;
      }

      if ("exit".equals(target) || "saida".equals(target) || "all".equals(target) || "tudo".equals(target)) {
         claim.exitMessage = "";
         changed = true;
      }

      if (!changed) {
         send(context, "Use enter, exit ou all.", "Use enter, exit or all.");
         return 0;
      } else {
         IslandClaimsData.get(player.level()).setDirty();
         send(context, "Mensagem limpa.", "Message cleared.");
         return 1;
      }
   }

   private static int adminClear(CommandContext<CommandSourceStack> context) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      IslandDefinition island = targetIsland(context).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context, "A fonte do comando nao esta em uma ilha.", "The command source is not on an island.");
         return 0;
      } else if (!island.claimable()) {
         send(context, "Esta ilha nao possui claim para limpar.", "This island has no claim to clear.");
         return 0;
      } else {
         IslandClaim claim = IslandClaimsData.get(level).getOrCreate(island);
         claim.clearOwner();
         claim.scores.clear();
         claim.protectedBuildAnchors.clear();
         IslandClaimsData.get(level).setDirty();
         send(context, "Claim e pontuacao limpos para " + islandName(context, island) + ".", "Claim and score cleared for " + islandName(context, island) + ".");
         return 1;
      }
   }

   private static int adminTransferCrew(CommandContext<CommandSourceStack> context) {
      return adminTransfer(context, ScoreTarget.crew(normalizeCrewArg(StringArgumentType.getString(context, "crew")), null));
   }

   private static int adminTransferPlayer(CommandContext<CommandSourceStack> context) {
      return adminTransfer(context, parseAdminPlayerTarget(context, StringArgumentType.getString(context, "playerTarget")));
   }

   private static int adminTransfer(CommandContext<CommandSourceStack> context, ScoreTarget target) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      IslandDefinition island = targetIsland(context).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context, "A fonte do comando nao esta em uma ilha.", "The command source is not on an island.");
         return 0;
      } else if (!island.claimable()) {
         send(context, "Esta ilha nao pode receber dono.", "This island cannot receive an owner.");
         return 0;
      } else {
         IslandClaim claim = IslandClaimsData.get(level).getOrCreate(island);
         claim.setOwner(target);
         IslandClaimsData.get(level).setDirty();
         send(context, "Ilha transferida para " + label(context, target.label()) + ".", "Island transferred to " + label(context, target.label()) + ".");
         return 1;
      }
   }

   private static int adminReview(CommandContext<CommandSourceStack> context) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getServer().overworld();
      IslandClaimsData.ReviewSummary summary = IslandClaimsData.get(level).forceOwnershipReview(level);
      send(
         context,
         "Revisao executada: "
            + summary.conquered()
            + " conquistas, "
            + summary.transferred()
            + " transferencias, "
            + summary.totalClaims()
            + " claims avaliados.",
         "Review executed: " + summary.conquered() + " conquests, " + summary.transferred() + " transfers, " + summary.totalClaims() + " claims reviewed."
      );
      return 1;
   }

   private static int adminAddCrewScore(CommandContext<CommandSourceStack> context) {
      return adminAddScore(context, ScoreTarget.crew(normalizeCrewArg(StringArgumentType.getString(context, "crew")), null));
   }

   private static int adminAddPlayerScore(CommandContext<CommandSourceStack> context) {
      return adminAddScore(context, parseAdminPlayerTarget(context, StringArgumentType.getString(context, "playerTarget")));
   }

   private static int adminAddScore(CommandContext<CommandSourceStack> context, ScoreTarget target) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      IslandDefinition island = targetIsland(context).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context, "A fonte do comando nao esta em uma ilha.", "The command source is not on an island.");
         return 0;
      } else if (!island.claimable()) {
         send(context, "Esta ilha nao aceita pontuacao.", "This island does not accept scoring.");
         return 0;
      } else {
         double amount = DoubleArgumentType.getDouble(context, "amount");
         IslandClaimsData.get(level).addScore(level, island, target, amount);
         IslandClaim claim = IslandClaimsData.get(level).getOrCreate(island);
         double score = claim.scoreFor(target);
         String owner = claim.hasOwner() ? label(context, claim.ownerLabel()) : label(context, "sem dono");
         send(
            context,
            "Adicionados "
               + format(amount)
               + " pontos para "
               + label(context, target.label())
               + " em "
               + islandName(context, island)
               + ". Total: "
               + format(score)
               + ". Dono: "
               + owner
               + ".",
            "Added "
               + format(amount)
               + " points for "
               + label(context, target.label())
               + " on "
               + islandName(context, island)
               + ". Total: "
               + format(score)
               + ". Owner: "
               + owner
               + "."
         );
         return 1;
      }
   }

   private static int adminMapStats(CommandContext<CommandSourceStack> context) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getServer().overworld();
      List<IslandDefinition> islands = OnePieceGeography.allClaimIslands(level.getSeed());
      Map<RegionType, Integer> totals = new EnumMap<>(RegionType.class);
      Map<RegionType, Integer> fixed = new EnumMap<>(RegionType.class);
      Map<RegionType, Integer> normal = new EnumMap<>(RegionType.class);
      Map<RegionType, EnumMap<IslandSize, Integer>> sizes = new EnumMap<>(RegionType.class);

      for (IslandDefinition island : islands) {
         increment(totals, island.region());
         if (!island.unique() && !island.starterIsland()) {
            increment(normal, island.region());
         } else {
            increment(fixed, island.region());
         }

         sizes.computeIfAbsent(island.region(), ignored -> new EnumMap<>(IslandSize.class)).merge(island.size(), 1, Integer::sum);
      }

      send(context, "Seed: " + level.getSeed() + " | Ilhas totais: " + islands.size(), "Seed: " + level.getSeed() + " | Total islands: " + islands.size());

      for (RegionType region : List.of(
         RegionType.EAST_BLUE, RegionType.NORTH_BLUE, RegionType.SOUTH_BLUE, RegionType.WEST_BLUE, RegionType.GRAND_LINE, RegionType.RED_LINE
      )) {
         send(
            context,
            region
               + ": total "
               + totals.getOrDefault(region, 0)
               + " | fixas "
               + fixed.getOrDefault(region, 0)
               + " | normais "
               + normal.getOrDefault(region, 0)
               + " | tamanhos "
               + formatSizes(sizes.get(region)),
            region
               + ": total "
               + totals.getOrDefault(region, 0)
               + " | fixed "
               + fixed.getOrDefault(region, 0)
               + " | normal "
               + normal.getOrDefault(region, 0)
               + " | sizes "
               + formatSizes(sizes.get(region))
         );
      }

      if (totals.getOrDefault(RegionType.CALM_BELT, 0) > 0) {
         send(
            context,
            "CALM_BELT: total "
               + totals.getOrDefault(RegionType.CALM_BELT, 0)
               + " | normais "
               + normal.getOrDefault(RegionType.CALM_BELT, 0)
               + " | tamanhos "
               + formatSizes(sizes.get(RegionType.CALM_BELT)),
            "CALM_BELT: total "
               + totals.getOrDefault(RegionType.CALM_BELT, 0)
               + " | normal "
               + normal.getOrDefault(RegionType.CALM_BELT, 0)
               + " | sizes "
               + formatSizes(sizes.get(RegionType.CALM_BELT))
         );
      }

      return 1;
   }

   private static IslandDefinition requireManagedIsland(CommandContext<CommandSourceStack> context, ServerPlayer player) {
      IslandDefinition island = targetIsland(context, player).orElse(null);
      if (island == null) {
         sendNoTargetIsland(context);
         return null;
      } else if (!island.claimable()) {
         send(context, "Esta ilha nao aceita configuracao de claim.", "This island does not accept claim configuration.");
         return null;
      } else if (!IslandAccess.canManage(player, island)) {
         send(
            context,
            "Apenas o dono, capitao da crew dona ou operadores podem gerenciar esta ilha.",
            "Only the owner, owner crew captain, or operators can manage this island."
         );
         return null;
      } else {
         return island;
      }
   }

   private static int adminSpawnInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      PlayerSpawnData data = PlayerSpawnData.get(level);
      IslandDefinition island = data.assignedIsland(target.getUUID()).orElse(null);
      if (island == null) {
         send(
            context,
            target.getGameProfile().name() + " ainda nao tem ilha inicial definida.",
            target.getGameProfile().name() + " does not have a starter island assigned yet."
         );
      } else {
         send(context, target.getGameProfile().name() + ": " + islandName(context, island));
      }

      for (IslandDefinition starter : UniqueIslandCatalog.starters()) {
         send(
            context,
            starter.displayName() + ": " + data.countFor(starter.id()) + " jogadores.",
            starter.displayName() + ": " + data.countFor(starter.id()) + " players."
         );
      }

      return 1;
   }

   private static int adminRerollSpawn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      IslandDefinition island = PlayerSpawnData.get(level).reroll(target.getUUID());
      BlockPos pos = safeSpawnInLevel(level, island);
      if (target.level().dimension() == Level.OVERWORLD) {
         if (target.getVehicle() != null) {
            target.stopRiding();
         }

         target.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
      }

      send(
         context,
         "Spawn de " + target.getGameProfile().name() + " redefinido para " + island.displayName() + ".",
         "Spawn for " + target.getGameProfile().name() + " reset to " + island.displayName() + "."
      );
      return 1;
   }

   private static RequiredArgumentBuilder<CommandSourceStack, String> islandArgument() {
      return Commands.argument("island", StringArgumentType.word()).suggests(IslandCommands::suggestIslandIds);
   }

   private static Optional<IslandDefinition> targetIsland(CommandContext<CommandSourceStack> context, ServerPlayer player) {
      Optional<String> raw = islandArgumentText(context);
      return raw.isPresent() ? islandByInput(((CommandSourceStack)context.getSource()).getServer().overworld(), raw.get()) : currentIsland(player);
   }

   private static Optional<IslandDefinition> targetIsland(CommandContext<CommandSourceStack> context) {
      Optional<String> raw = islandArgumentText(context);
      return raw.isPresent() ? islandByInput(((CommandSourceStack)context.getSource()).getServer().overworld(), raw.get()) : sourceIsland(context);
   }

   private static Optional<String> islandArgumentText(CommandContext<CommandSourceStack> context) {
      try {
         String raw = StringArgumentType.getString(context, "island");
         return raw != null && !raw.isBlank() ? Optional.of(raw) : Optional.empty();
      } catch (IllegalArgumentException ex) {
         return Optional.empty();
      }
   }

   private static Optional<IslandDefinition> islandByInput(ServerLevel level, String raw) {
      String normalized = normalizeIslandInput(raw);
      return normalized.isBlank()
         ? Optional.empty()
         : OnePieceGeography.allClaimIslands(level.getSeed())
            .stream()
            .filter(
               island -> normalized.equals(normalizeIslandInput(island.id()))
                  || normalized.equals(normalizeIslandInput(island.displayName()))
                  || normalized.equals(normalizeIslandInput("ilha_" + island.displayName()))
                  || normalized.equals(normalizeIslandInput("island_" + island.displayName()))
            )
            .findFirst();
   }

   private static CompletableFuture<Suggestions> suggestIslandIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getServer().overworld();
      Stream<String> suggestions = OnePieceGeography.allClaimIslands(level.getSeed())
         .stream()
         .map(island -> normalizeIslandInput(island.displayName()))
         .filter(value -> !value.isBlank())
         .distinct();
      return SharedSuggestionProvider.suggest(suggestions, builder);
   }

   private static String normalizeIslandInput(String raw) {
      if (raw == null) {
         return "";
      }

      String normalized = Normalizer.normalize(raw.trim().toLowerCase(Locale.ROOT), Form.NFD)
         .replaceAll("\\p{M}", "")
         .replace('-', '_')
         .replace(' ', '_')
         .replaceAll("[^a-z0-9_]", "")
         .replaceAll("_+", "_");
      if (normalized.startsWith("ilha_")) {
         normalized = normalized.substring("ilha_".length());
      } else if (normalized.startsWith("island_")) {
         normalized = normalized.substring("island_".length());
      }

      return normalized;
   }

   private static Optional<IslandDefinition> currentIsland(ServerPlayer player) {
      if (player.level().dimension() != Level.OVERWORLD) {
         return Optional.empty();
      }

      ServerLevel level = player.level();
      return OnePieceGeography.claimIslandAt(level.getSeed(), player.getBlockX(), player.getBlockZ());
   }

   private static Optional<IslandDefinition> sourceIsland(CommandContext<CommandSourceStack> context) {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      if (level.dimension() != Level.OVERWORLD) {
         return Optional.empty();
      }

      BlockPos pos = BlockPos.containing(((CommandSourceStack)context.getSource()).getPosition());
      return OnePieceGeography.claimIslandAt(level.getSeed(), pos.getX(), pos.getZ());
   }

   private static void send(CommandContext<CommandSourceStack> context, String message) {
      send(context, message, message);
   }

   private static void send(CommandContext<CommandSourceStack> context, String pt, String en) {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer sourcePlayer ? sourcePlayer : null;
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> OmpMessages.component(player, pt, en), false);
   }

   private static void sendNoTargetIsland(CommandContext<CommandSourceStack> context) {
      sendNoTargetIsland(context, "Voce nao esta em uma ilha.", "You are not on an island.");
   }

   private static void sendNoTargetIsland(CommandContext<CommandSourceStack> context, String noCurrentPt, String noCurrentEn) {
      Optional<String> raw = islandArgumentText(context);
      if (raw.isPresent()) {
         send(context, "Ilha alvo nao encontrada: " + raw.get() + ".", "Target island not found: " + raw.get() + ".");
      } else {
         send(context, noCurrentPt, noCurrentEn);
      }
   }

   private static String label(CommandContext<CommandSourceStack> context, String label) {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer sourcePlayer ? sourcePlayer : null;
      if (OmpMessages.language(player) != OmpMessages.Language.EN_US) {
         return label;
      } else if (label == null || label.isBlank()) {
         return "Unclaimed";
      } else if ("sem dono".equalsIgnoreCase(label)) {
         return "Unclaimed";
      } else if (label.startsWith("Tripulacao ")) {
         return "Crew " + label.substring("Tripulacao ".length());
      } else {
         return label.startsWith("Jogador ") ? "Player " + label.substring("Jogador ".length()) : label;
      }
   }

   private static String islandName(CommandContext<CommandSourceStack> context, IslandDefinition island) {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer sourcePlayer ? sourcePlayer : null;
      String name = island.displayName();
      return OmpMessages.language(player) == OmpMessages.Language.EN_US && name.startsWith("Ilha ") ? "Island " + name.substring("Ilha ".length()) : name;
   }

   private static String format(double value) {
      return String.format(Locale.US, "%.2f", value);
   }

   private static String normalizeCrewArg(String crew) {
      return crew.toLowerCase().replace(' ', '_');
   }

   private static ScoreTarget scoreTarget(ServerPlayer player) {
      return ScoreTarget.forPlayer(player, MinePieceBridge.crewOf(player)).orElseGet(() -> ScoreTarget.player(player.getUUID(), player.getGameProfile().name()));
   }

   private static ScoreTarget parseAdminPlayerTarget(CommandContext<CommandSourceStack> context, String raw) {
      String value = raw.trim();
      ServerPlayer online = ((CommandSourceStack)context.getSource()).getServer().getPlayerList().getPlayerByName(value);
      if (online != null) {
         return ScoreTarget.player(online.getUUID(), online.getGameProfile().name());
      }

      try {
         UUID uuid = UUID.fromString(value);
         return ScoreTarget.player(uuid, uuid.toString());
      } catch (IllegalArgumentException ignored) {
         return ScoreTarget.player(UUID.nameUUIDFromBytes(("offline:" + value).getBytes(StandardCharsets.UTF_8)), value);
      }
   }

   private static Stream<String> flagIds() {
      return Stream.of(IslandFlag.values()).map(IslandFlag::id);
   }

   private static Stream<String> messageTargets() {
      return Stream.of("enter", "exit", "all", "entrada", "saida", "tudo");
   }

   private static String sanitizeMessage(String text) {
      if (text == null) {
         return "";
      }

      String sanitized = text.trim().replaceAll("\\s+", " ");
      return sanitized.length() <= 160 ? sanitized : sanitized.substring(0, 160);
   }

   private static void increment(Map<RegionType, Integer> counts, RegionType region) {
      counts.merge(region, 1, Integer::sum);
   }

   private static String formatSizes(Map<IslandSize, Integer> sizes) {
      if (sizes != null && !sizes.isEmpty()) {
         StringBuilder builder = new StringBuilder();

         for (IslandSize size : IslandSize.values()) {
            int count = sizes.getOrDefault(size, 0);
            if (count > 0) {
               if (builder.length() > 0) {
                  builder.append(", ");
               }

               builder.append(size).append('=').append(count);
            }
         }

         return builder.length() == 0 ? "-" : builder.toString();
      } else {
         return "-";
      }
   }

   private static BlockPos safeSpawnInLevel(ServerLevel level, IslandDefinition island) {
      BlockPos base = OnePieceGeography.safeSpawnFor(level.getSeed(), island);
      BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(base.getX(), 0, base.getZ()));
      return surface.getY() <= 63 ? base : surface.above();
   }
}
