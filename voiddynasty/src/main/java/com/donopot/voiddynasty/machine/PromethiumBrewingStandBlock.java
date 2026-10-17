package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Alambic à Prométhium : alambic vanilla amélioré (infusion x8, carburant prométhium).
 */
public class PromethiumBrewingStandBlock extends BrewingStandBlock {

    public PromethiumBrewingStandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PromethiumBrewingStandBlockEntity(pos, state);
    }
}
