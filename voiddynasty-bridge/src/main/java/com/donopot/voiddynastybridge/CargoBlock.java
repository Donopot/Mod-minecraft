package com.donopot.voiddynastybridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public final class CargoBlock extends BaseEntityBlock {
   public CargoBlock(Properties var1) {
      super(var1);
   }

   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new CargoBlockEntity(var1, var2);
   }

   protected InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      if (!var2.isClientSide()
         && var2.getBlockEntity(var3) instanceof CargoBlockEntity var6
         && BridgeMod.service != null
         && BridgeMod.service.matches(var6)
         && !var6.locked()) {
         var4.openMenu(var6);
      }

      return InteractionResult.SUCCESS;
   }
}
