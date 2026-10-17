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
 * Super Conteneur : bloc multi-bloc 6x2. Chaque bloc détient 54 slots ; un rectangle
 * 6x2 complet fusionne en un inventaire agrégé de 648 slots (scrollable).
 */
public class SuperContainerBlock extends BaseEntityBlock {

    public SuperContainerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperContainerBlockEntity(pos, state);
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
        if (be instanceof SuperContainerBlockEntity container) {
            player.openMenu(container);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
