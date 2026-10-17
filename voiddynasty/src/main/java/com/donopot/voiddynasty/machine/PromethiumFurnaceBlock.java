package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Four à Prométhium : four vanilla amélioré (carburant prométhium + vitesse x8).
 */
public class PromethiumFurnaceBlock extends FurnaceBlock {

    public PromethiumFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PromethiumFurnaceBlockEntity(pos, state);
    }
}
