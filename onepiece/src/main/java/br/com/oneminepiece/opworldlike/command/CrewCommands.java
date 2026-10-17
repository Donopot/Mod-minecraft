package br.com.oneminepiece.opworldlike.command;

import br.com.oneminepiece.opworldlike.OmpMessages;
import br.com.oneminepiece.opworldlike.claim.IslandClaim;
import br.com.oneminepiece.opworldlike.claim.IslandClaimsData;
import br.com.oneminepiece.opworldlike.crew.CrewPersistenceStore;
import br.com.oneminepiece.opworldlike.integration.CrewInfo;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class CrewCommands {
   private static final long INVITE_TTL_MS = 120000L;
   private static final Map<UUID, CrewCommands.PendingInvite> INVITES = new ConcurrentHashMap<>();

   private CrewCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                             "crew"
                                          )
                                          .executes(CrewCommands::infoSelf))
                                       .then(
                                          Commands.literal("create")
                                             .then(Commands.argument("name", StringArgumentType.greedyString()).executes(CrewCommands::create))
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("info").executes(CrewCommands::infoSelf))
                                          .then(
                                             Commands.argument("name", StringArgumentType.greedyString())
                                                .suggests(CrewCommands::suggestCrews)
                                                .executes(CrewCommands::infoNamed)
                                          )
                                    ))
                                 .then(Commands.literal("list").executes(CrewCommands::list)))
                              .then(Commands.literal("invite").then(Commands.argument("player", EntityArgument.player()).executes(CrewCommands::invite))))
                           .then(Commands.literal("accept").executes(CrewCommands::accept)))
                        .then(Commands.literal("deny").executes(CrewCommands::deny)))
                     .then(Commands.literal("kick").then(Commands.argument("player", EntityArgument.player()).executes(CrewCommands::kick))))
                  .then(Commands.literal("leave").executes(CrewCommands::leave)))
               .then(Commands.literal("rename").then(Commands.argument("name", StringArgumentType.greedyString()).executes(CrewCommands::rename))))
            .then(Commands.literal("disband").executes(CrewCommands::disband))
      );
   }

   private static int create(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      if (!ensureApi(context)) {
         return 0;
      } else {
         String name = normalizeDisplayName(StringArgumentType.getString(context, "name"));
         if (!validCrewName(name)) {
            send(
               context,
               "Nome invalido. Use 3 a 20 caracteres: letras, numeros, espaco, _ ou -.",
               "Invalid name. Use 3 to 20 characters: letters, numbers, space, _ or -."
            );
            return 0;
         } else if (MinePieceBridge.crewOf(player).isPresent()) {
            send(context, "Voce ja esta em uma crew.", "You are already in a crew.");
            return 0;
         } else if (MinePieceBridge.crewByName(name).isPresent()) {
            send(context, "Ja existe uma crew com esse nome.", "A crew with that name already exists.");
            return 0;
         } else if (!MinePieceBridge.createCrew(player, name)) {
            send(context, "Nao foi possivel criar a crew agora.", "Could not create the crew right now.");
            return 0;
         } else {
            CrewPersistenceStore.saveCurrent("crew create");
            send(context, "Crew criada: " + name + ".", "Crew created: " + name + ".");
            return 1;
         }
      }
   }

   private static int infoSelf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      if (crew.isEmpty()) {
         send(
            context,
            "Voce nao esta em uma crew. Use /crew create <nome> ou aceite um convite.",
            "You are not in a crew. Use /crew create <name> or accept an invite."
         );
         return 0;
      } else {
         sendCrewInfo(context, crew.get());
         return 1;
      }
   }

   private static int infoNamed(CommandContext<CommandSourceStack> context) {
      String name = StringArgumentType.getString(context, "name");
      Optional<CrewInfo> crew = MinePieceBridge.crewByName(name);
      if (crew.isEmpty()) {
         send(context, "Crew nao encontrada.", "Crew not found.");
         return 0;
      } else {
         sendCrewInfo(context, crew.get());
         return 1;
      }
   }

   private static int list(CommandContext<CommandSourceStack> context) {
      List<CrewInfo> crews = MinePieceBridge.crews();
      if (crews.isEmpty()) {
         send(context, "Ainda nao ha crews criadas.", "There are no crews yet.");
         return 0;
      }

      send(context, "Crews ativas:", "Active crews:");

      for (int i = 0; i < Math.min(10, crews.size()); i++) {
         CrewInfo crew = crews.get(i);
         send(context, "- " + crew.name() + " (" + crew.members() + " membros)", "- " + crew.name() + " (" + crew.members() + " members)");
      }

      return 1;
   }

   private static int invite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      if (crew.isEmpty()) {
         send(context, "Voce precisa estar em uma crew para convidar.", "You must be in a crew to invite players.");
         return 0;
      } else if (!isCaptain(player, crew.get())) {
         send(context, "Apenas o capitao pode convidar para a crew.", "Only the captain can invite players to the crew.");
         return 0;
      } else if (player.getUUID().equals(target.getUUID())) {
         send(context, "Voce ja esta nessa crew.", "You are already in that crew.");
         return 0;
      } else if (MinePieceBridge.crewOf(target).isPresent()) {
         send(context, target.getGameProfile().name() + " ja esta em uma crew.", target.getGameProfile().name() + " is already in a crew.");
         return 0;
      } else {
         INVITES.put(
            target.getUUID(),
            new CrewCommands.PendingInvite(crew.get().name(), player.getUUID(), player.getGameProfile().name(), System.currentTimeMillis() + 120000L)
         );
         send(context, "Convite enviado para " + target.getGameProfile().name() + ".", "Invite sent to " + target.getGameProfile().name() + ".");
         send(
            target,
            player.getGameProfile().name() + " convidou voce para a crew " + crew.get().name() + ". Use /crew accept ou /crew deny.",
            player.getGameProfile().name() + " invited you to crew " + crew.get().name() + ". Use /crew accept or /crew deny."
         );
         return 1;
      }
   }

   private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      CrewCommands.PendingInvite invite = validInvite(player);
      if (invite == null) {
         send(context, "Voce nao tem convite de crew pendente.", "You have no pending crew invite.");
         return 0;
      }

      if (MinePieceBridge.crewOf(player).isPresent()) {
         INVITES.remove(player.getUUID());
         send(context, "Voce ja esta em uma crew.", "You are already in a crew.");
         return 0;
      }

      if (MinePieceBridge.crewByName(invite.crewName()).isEmpty()) {
         INVITES.remove(player.getUUID());
         send(context, "Essa crew nao existe mais.", "That crew no longer exists.");
         return 0;
      }

      if (!MinePieceBridge.addPlayerToCrew(player, invite.crewName())) {
         send(context, "Nao foi possivel entrar na crew agora.", "Could not join the crew right now.");
         return 0;
      }

      CrewPersistenceStore.saveCurrent("crew accept");
      INVITES.remove(player.getUUID());
      send(context, "Voce entrou na crew " + invite.crewName() + ".", "You joined crew " + invite.crewName() + ".");
      ServerPlayer inviter = player.level().getServer().getPlayerList().getPlayer(invite.inviter());
      if (inviter != null) {
         send(inviter, player.getGameProfile().name() + " aceitou o convite da crew.", player.getGameProfile().name() + " accepted the crew invite.");
      }

      return 1;
   }

   private static int deny(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      CrewCommands.PendingInvite invite = INVITES.remove(player.getUUID());
      if (invite != null && !invite.expired()) {
         send(context, "Convite recusado.", "Invite denied.");
         ServerPlayer inviter = player.level().getServer().getPlayerList().getPlayer(invite.inviter());
         if (inviter != null) {
            send(inviter, player.getGameProfile().name() + " recusou o convite da crew.", player.getGameProfile().name() + " denied the crew invite.");
         }

         return 1;
      } else {
         send(context, "Voce nao tem convite de crew pendente.", "You have no pending crew invite.");
         return 0;
      }
   }

   private static int kick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      Optional<CrewInfo> targetCrew = MinePieceBridge.crewOf(target);
      if (crew.isEmpty() || targetCrew.isEmpty() || !sameCrew(crew.get(), targetCrew.get())) {
         send(context, "Esse jogador nao esta na sua crew.", "That player is not in your crew.");
         return 0;
      } else if (!isCaptain(player, crew.get())) {
         send(context, "Apenas o capitao pode remover membros.", "Only the captain can remove members.");
         return 0;
      } else if (target.getUUID().equals(player.getUUID()) || isCaptain(target, targetCrew.get())) {
         send(context, "O capitao deve usar /crew leave ou /crew disband.", "The captain must use /crew leave or /crew disband.");
         return 0;
      } else if (!MinePieceBridge.removePlayerFromCrew(target, true)) {
         send(context, "Nao foi possivel remover esse jogador agora.", "Could not remove that player right now.");
         return 0;
      } else {
         CrewPersistenceStore.saveCurrentExact("crew kick");
         send(context, target.getGameProfile().name() + " foi removido da crew.", target.getGameProfile().name() + " was removed from the crew.");
         send(target, "Voce foi removido da crew " + crew.get().name() + ".", "You were removed from crew " + crew.get().name() + ".");
         return 1;
      }
   }

   private static int leave(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      if (crew.isEmpty()) {
         send(context, "Voce nao esta em uma crew.", "You are not in a crew.");
         return 0;
      } else {
         String name = crew.get().name();
         if (!MinePieceBridge.removePlayerFromCrew(player, false)) {
            send(context, "Nao foi possivel sair da crew agora.", "Could not leave the crew right now.");
            return 0;
         } else {
            CrewPersistenceStore.saveCurrentExact("crew leave");
            send(context, "Voce saiu da crew " + name + ".", "You left crew " + name + ".");
            return 1;
         }
      }
   }

   private static int rename(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      if (crew.isEmpty()) {
         send(context, "Voce nao esta em uma crew.", "You are not in a crew.");
         return 0;
      } else if (!isCaptain(player, crew.get())) {
         send(context, "Apenas o capitao pode renomear a crew.", "Only the captain can rename the crew.");
         return 0;
      } else {
         String newName = normalizeDisplayName(StringArgumentType.getString(context, "name"));
         if (!validCrewName(newName)) {
            send(
               context,
               "Nome invalido. Use 3 a 20 caracteres: letras, numeros, espaco, _ ou -.",
               "Invalid name. Use 3 to 20 characters: letters, numbers, space, _ or -."
            );
            return 0;
         } else if (MinePieceBridge.crewByName(newName).isPresent()) {
            send(context, "Ja existe uma crew com esse nome.", "A crew with that name already exists.");
            return 0;
         } else {
            String oldName = crew.get().name();
            if (!MinePieceBridge.renameCrew(oldName, newName)) {
               send(context, "Nao foi possivel renomear a crew agora.", "Could not rename the crew right now.");
               return 0;
            } else {
               int migratedClaims = IslandClaimsData.get(player.level()).migrateCrew(oldName, newName, crew.get().captain());
               CrewPersistenceStore.saveCurrentExact("crew rename");
               send(
                  context,
                  "Crew renomeada para " + newName + ". Claims atualizados: " + migratedClaims + ".",
                  "Crew renamed to " + newName + ". Updated claims: " + migratedClaims + "."
               );
               return 1;
            }
         }
      }
   }

   private static int disband(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      if (crew.isEmpty()) {
         send(context, "Voce nao esta em uma crew.", "You are not in a crew.");
         return 0;
      } else if (!isCaptain(player, crew.get())) {
         send(context, "Apenas o capitao pode desfazer a crew.", "Only the captain can disband the crew.");
         return 0;
      } else {
         String name = crew.get().name();
         if (!MinePieceBridge.disbandCrew(name)) {
            send(context, "Nao foi possivel desfazer a crew agora.", "Could not disband the crew right now.");
            return 0;
         } else {
            CrewPersistenceStore.saveCurrentExact("crew disband");
            send(
               context,
               "Crew desfeita: " + name + ". Claims existentes serao reavaliados na revisao de ilhas.",
               "Crew disbanded: " + name + ". Existing claims will be checked during island review."
            );
            return 1;
         }
      }
   }

   private static boolean ensureApi(CommandContext<CommandSourceStack> context) {
      if (MinePieceBridge.crewApiAvailable()) {
         return true;
      }

      send(context, "API de crew do Mine Piece indisponivel.", "Mine Piece crew API is unavailable.");
      return false;
   }

   private static CrewCommands.PendingInvite validInvite(ServerPlayer player) {
      CrewCommands.PendingInvite invite = INVITES.get(player.getUUID());
      if (invite == null) {
         return null;
      } else if (invite.expired()) {
         INVITES.remove(player.getUUID());
         return null;
      } else {
         return invite;
      }
   }

   private static boolean isCaptain(ServerPlayer player, CrewInfo crew) {
      return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || crew.captain() == null || crew.captain().equals(player.getUUID());
   }

   private static boolean sameCrew(CrewInfo left, CrewInfo right) {
      return IslandClaim.normalizeCrew(left.name()).equals(IslandClaim.normalizeCrew(right.name()));
   }

   private static void sendCrewInfo(CommandContext<CommandSourceStack> context, CrewInfo crew) {
      String captain = crew.captain() == null ? "-" : crew.captain().toString();
      send(
         context,
         "Crew: " + crew.name() + " | Membros: " + crew.members() + " | Capitao: " + captain,
         "Crew: " + crew.name() + " | Members: " + crew.members() + " | Captain: " + captain
      );
   }

   private static CompletableFuture<Suggestions> suggestCrews(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.suggest(MinePieceBridge.crews().stream().map(CrewInfo::name), builder);
   }

   private static String normalizeDisplayName(String raw) {
      return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
   }

   private static boolean validCrewName(String name) {
      return name != null && name.matches("[A-Za-z0-9 _-]{3,20}");
   }

   private static void send(CommandContext<CommandSourceStack> context, String pt, String en) {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer sourcePlayer ? sourcePlayer : null;
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> OmpMessages.component(player, pt, en), false);
   }

   private static void send(ServerPlayer player, String pt, String en) {
      player.sendSystemMessage(OmpMessages.component(player, pt, en));
   }

   private record PendingInvite(String crewName, UUID inviter, String inviterName, long expiresAt) {
      private boolean expired() {
         return System.currentTimeMillis() > this.expiresAt;
      }
   }
}
