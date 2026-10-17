package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Haut Fourneau à Prométhium : haut fourneau vanilla amélioré (fonte x8).
 */
public class PromethiumBlastFurnaceBlock extends BlastFurnaceBlock {

    public PromethiumBlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PromethiumBlastFurnaceBlockEntity(pos, state);
    }
}
