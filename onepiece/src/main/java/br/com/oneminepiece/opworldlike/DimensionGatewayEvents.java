package br.com.oneminepiece.opworldlike;

import br.com.oneminepiece.opworldlike.worldgen.DimensionGatewayLayout;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceChunkGenerator;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Port Fabric 26.3 de DimensionGatewayEvents (Forge 1.20.1).
 *
 * PORTÉ : téléportation de retour depuis le Nether/End vers l'île-portail correspondante.
 *   PlayerChangedDimensionEvent → ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.
 *
 * NON PORTÉ (aucun événement Fabric cancellable équivalent — mixin requis) :
 *   1. PortalSpawnEvent → interdire les portails Nether hors de DimensionGatewayLayout.isInsideNetherGateway.
 *   2. EntityTravelToDimensionEvent → gate d'accès Nether/End :
 *        Nether : MinePieceBridge.dorikiProgress(player) >= 0.5
 *        End    : dorikiProgress >= 0.999 && hasMaxBusoshoku && hasMaxKenbunshoku
 *      Sinon annuler le voyage + message via OmpMessages.component(player, pt, en).
 *   Logique d'origine conservée en commentaire de fin de classe pour le mixin futur.
 */
public final class DimensionGatewayEvents {
   private final boolean enforceMmnmRequirements;

   public DimensionGatewayEvents(boolean enforceMmnmRequirements) {
      this.enforceMmnmRequirements = enforceMmnmRequirements;
   }

   public void register() {
      ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(this::onChangedLevel);
   }

   private void onChangedLevel(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
      if (destination.dimension() == Level.OVERWORLD && this.isOnePieceWorld(destination)) {
         if (origin.dimension() == Level.NETHER) {
            this.teleportReturn(player, DimensionGatewayLayout.netherReturnPos(destination.getSeed()), "Retorno ao South Blue: Ilha Infernal.");
         } else if (origin.dimension() == Level.END) {
            this.teleportReturn(player, DimensionGatewayLayout.endReturnPos(destination.getSeed()), "Retorno ao North Blue: Ilha do Fim.");
         }
      }
   }

   private void teleportReturn(ServerPlayer player, BlockPos pos, String message) {
      player.level().getChunk(pos.getX(), pos.getZ());
      player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
      player.sendSystemMessage(OmpMessages.component(player, message, message), true);
   }

   private boolean isOnePieceWorld(ServerLevel level) {
      return level.getChunkSource().getGenerator() instanceof OnePieceChunkGenerator;
   }

   // ===== Logique d'origine différée (mixin requis) =====
   //
   // onPortalSpawn(PortalSpawnEvent event):
   //   if (event.getLevel() instanceof ServerLevel level && isOnePieceWorld(level)) {
   //      BlockPos pos = event.getPos();
   //      if (level.dimension() == Level.OVERWORLD
   //         && !DimensionGatewayLayout.isInsideNetherGateway(pos.getX(), pos.getZ())) {
   //         event.setCanceled(true);
   //      }
   //   }
   //
   // onTravelToDimension(EntityTravelToDimensionEvent event):
   //   if (event.getEntity() instanceof ServerPlayer player
   //      && player.level().dimension() == Level.OVERWORLD && isOnePieceWorld(player.level())) {
   //      if (event.getDimension() == Level.NETHER) {
   //         validateNetherAccess(event, player);
   //      } else if (event.getDimension() == Level.END) {
   //         validateEndAccess(event, player);
   //      }
   //   }
   //
   // validateNetherAccess: hors isInsideNetherGateway → deny(...) ; sinon si enforceMmnmRequirements
   //   && MinePieceBridge.dorikiProgress(player) < 0.5 → deny("Voce precisa de pelo menos 50% do limite
   //   de Doriki para entrar no Nether.", "You need at least 50% of the Doriki limit to enter the Nether.").
   // validateEndAccess: hors isInsideEndDungeon(x,y,z) → deny(...) ; sinon si enforceMmnmRequirements
   //   && (dorikiProgress < 0.999 || !hasMaxBusoshoku || !hasMaxKenbunshoku) → deny("O End exige Doriki no
   //   limite e Busoshoku/Kenbunshoku no rank maximo.", "The End requires max Doriki and max Busoshoku/Kenbunshoku.").
   // deny: event.setCanceled(true); player.sendSystemMessage(OmpMessages.component(player, pt, en), true).
}
