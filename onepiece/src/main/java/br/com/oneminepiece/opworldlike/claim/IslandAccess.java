package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.integration.CrewInfo;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

public final class IslandAccess {
   private IslandAccess() {
   }

   public static Optional<IslandDefinition> islandAt(ServerLevel level, BlockPos pos) {
      return level.dimension() != Level.OVERWORLD ? Optional.empty() : OnePieceGeography.claimIslandAt(level.getSeed(), pos.getX(), pos.getZ());
   }

   public static boolean can(ServerPlayer player, IslandDefinition island, IslandFlag flag) {
      if (!island.claimable()) {
         return true;
      }

      IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).get(island.id());
      if (claim != null && claim.hasOwner() && !claim.publicAllowed(flag) && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         if (claim.trustedPlayers.contains(player.getUUID())) {
            return true;
         }

         if (claim.isOwnerPlayer(player.getUUID())) {
            return true;
         }

         Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
         return crew.isEmpty() ? false : claim.isOwnerCrew(crew.get().key()) || claim.trustedCrews.contains(crew.get().key());
      } else {
         return true;
      }
   }

   public static boolean isOwnerOrTrusted(ServerPlayer player, IslandDefinition island) {
      if (island.claimable() && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).get(island.id());
         if (claim != null && claim.hasOwner()) {
            if (!claim.trustedPlayers.contains(player.getUUID()) && !claim.isOwnerPlayer(player.getUUID())) {
               Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
               return crew.isPresent() && (claim.isOwnerCrew(crew.get().key()) || claim.trustedCrews.contains(crew.get().key()));
            } else {
               return true;
            }
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean isOwnerMember(ServerPlayer player, IslandDefinition island) {
      if (!island.claimable()) {
         return false;
      }

      IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).get(island.id());
      if (claim == null || !claim.hasOwner()) {
         return false;
      }

      if (claim.isOwnerPlayer(player.getUUID())) {
         return true;
      }

      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      return crew.isPresent() && claim.isOwnerCrew(crew.get().key());
   }

   public static boolean canManage(ServerPlayer player, IslandDefinition island) {
      if (!island.claimable()) {
         return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
      }

      IslandClaim claim = IslandClaimsData.get((ServerLevel)player.level()).get(island.id());
      if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         return true;
      }

      if (claim == null || !claim.hasOwner()) {
         return false;
      }

      if (claim.isOwnerPlayer(player.getUUID())) {
         return true;
      }

      Optional<CrewInfo> crew = MinePieceBridge.crewOf(player);
      return crew.isPresent() && claim.isOwnerCrew(crew.get().key()) && (crew.get().captain() == null || crew.get().captain().equals(player.getUUID()));
   }
}
