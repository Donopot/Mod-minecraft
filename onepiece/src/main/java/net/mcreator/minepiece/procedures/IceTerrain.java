package net.mcreator.minepiece.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Permanent packed-ice paths and spikes. Finite attribution never restores terrain. Water occupied by the owner remains unfrozen. */
public final class IceTerrain {

   /** Longueur par défaut de la plateforme, en blocs. */
   public static final int PLATFORM_LENGTH = 8;

   /** Durée du suivi du pouvoir, en ticks ; la glace est définitive. */
   public static final int ICE_LIFETIME_TICKS = 200;

   /** Hauteur maximale des pics. */
   public static final int SPIKE_HEIGHT = 3;

   /** Dégâts d'un pic dressé sur une cible (repris de la déclaration ver15 d'Ice Spikes). */

   private IceTerrain() {
   }

   /**
    * Gèle la surface devant le porteur : plateforme praticable sur l'eau, croute de glace sur le sol.
    *
    * <p>Aucune case occupée par le porteur n'est touchée — cf. la règle d'immersion de la classe.</p>
    *
    * @return nombre de blocs gelés
    */
   public static int freezePath(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      double range = move.range() > 0 ? move.range() : PLATFORM_LENGTH;
      Vec3Flat facing = flatFacing(player);
      int placed = 0;
      AABB own = player.getBoundingBox();
      for (double d = 1.0; d <= range; d += 1.0) {
         double x = player.getX() + facing.x() * d;
         double z = player.getZ() + facing.z() * d;
         for (int dy = 1; dy >= -2; dy--) {
            BlockPos pos = BlockPos.containing(x, player.getY() + dy, z);
            // Règle d'immersion : jamais dans l'espace du porteur.
            if (own.intersects(new AABB(pos))) {
               continue;
            }
            BlockState state = level.getBlockState(pos);
            boolean water = state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
            boolean ground = state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.SAND);
            if (!water && !ground) {
               continue;
            }
            if (placeIce(level, pos, player)) {
               placed++;
            }
            break;
         }
      }
      return placed;
   }

   /**
    * Gèle la surface <b>autour</b> du porteur, dans un rayon (Ice Age).
    *
    * <p>Même règle d'immersion que le chemin : aucune case occupée par le porteur n'est touchée.</p>
    *
    * @return nombre de blocs gelés
    */
   public static int freezeField(ServerPlayer player, double radius) {
      return freezeField(player,player.position(),radius);
   }
   public static int freezeField(ServerPlayer player,net.minecraft.world.phys.Vec3 origin,double radius){
      ServerLevel level = (ServerLevel) player.level();
      int placed = 0;
      AABB own = player.getBoundingBox();
      int r = (int) Math.ceil(radius);
      BlockPos center = BlockPos.containing(origin);
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, 0, -r), center.offset(r, 0, r))) {
         if (pos.distSqr(center) > radius * radius) {
            continue;
         }
         if (own.intersects(new AABB(pos))) {
            continue;
         }
         boolean water = level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
         if (!water) {
            continue;
         }
         if (placeIce(level, pos, player)) {
            placed++;
         }
      }
      return placed;
   }

   /**
    * Dresse des pics de glace sur la ligne devant le porteur.
    *
    * @return nombre de blocs de pics posés
    */
   public static int raiseSpikes(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      double range = move.range() > 0 ? move.range() : 6.0;
      Vec3Flat facing = flatFacing(player);
      int placed = 0;
      AABB own = player.getBoundingBox();
      for (double d = 1.0; d <= range; d += 1.0) {
         double x = player.getX() + facing.x() * d;
         double z = player.getZ() + facing.z() * d;
         BlockPos base = level.getBlockState(BlockPos.containing(x, player.getY(), z)).getFluidState().isEmpty()
            ? BlockPos.containing(x, player.getY(), z)
            : BlockPos.containing(x, player.getY(), z);
         for (int h = 0; h < SPIKE_HEIGHT; h++) {
            BlockPos pos = base.above(h);
            if (own.intersects(new AABB(pos))) {
               continue;
            }
            if (!level.getBlockState(pos).isAir()) {
               break;
            }
            if (placeIce(level, pos, player)) {
               placed++;
            }
         }
      }
      return placed;
   }

   /** Pose un bloc de glace définitif ; renvoie {@code true} si la case a été gelée. */
   private static boolean placeIce(ServerLevel level, BlockPos pos, ServerPlayer owner) {
      if(!level.isLoaded(pos) || level.getBlockState(pos).hasBlockEntity() || level.getBlockEntity(pos)!=null
            || level.getBlockState(pos).getDestroySpeed(level,pos)<0 || PowerBlockService.isTrackedAny(level,pos))return false;
      int before = PowerBlockService.activeCount();
      PowerBlockService.place(level, pos, Blocks.PACKED_ICE.defaultBlockState(),
         ICE_LIFETIME_TICKS, owner.getUUID());
      return PowerBlockService.activeCount() > before;
   }

   /** Direction horizontale du regard, normalisée. */
   private record Vec3Flat(double x, double z) {
   }

   private static Vec3Flat flatFacing(ServerPlayer player) {
      var look = player.getLookAngle();
      double len = Math.sqrt(look.x * look.x + look.z * look.z);
      if (len < 1.0E-4) {
         return new Vec3Flat(0.0, 1.0);
      }
      return new Vec3Flat(look.x / len, look.z / len);
   }
}
