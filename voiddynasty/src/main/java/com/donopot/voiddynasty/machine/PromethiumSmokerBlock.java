package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fumoir à Prométhium : fumoir vanilla amélioré (cuisson nourriture x8).
 */
public class PromethiumSmokerBlock extends SmokerBlock {

    public PromethiumSmokerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PromethiumSmokerBlockEntity(pos, state);
    }
}
