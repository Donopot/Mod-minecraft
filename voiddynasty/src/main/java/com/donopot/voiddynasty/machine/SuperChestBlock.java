package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Super Coffre : bloc multi-bloc 2x2. Chaque bloc détient 81 slots ; un carré 2x2
 * complet fusionne en un inventaire agrégé de 324 slots (scrollable).
 */
public class SuperChestBlock extends BaseEntityBlock {

    public SuperChestBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperChestBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SuperChestBlockEntity chest) {
            player.openMenu(chest);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
