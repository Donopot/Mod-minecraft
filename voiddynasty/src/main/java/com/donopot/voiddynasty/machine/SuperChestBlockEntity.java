package com.donopot.voiddynasty.machine;

import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Super Coffre (2x2) : chaque bloc détient 81 slots (9x9). La fusion d'un carré
 * 2x2 complet agrège 4 blocs = 324 slots (6x la contenance d'un grand coffre).
 */
public class SuperChestBlockEntity extends AbstractSuperContainerBlockEntity {

    public static final int SLOTS_PER_BLOCK = 81;
    public static final int WIDTH = 2;
    public static final int HEIGHT = 2;

    public SuperChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPER_CHEST, pos, state, SLOTS_PER_BLOCK);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.voiddynasty.super_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInv) {
        return new SuperChestMenu(syncId, playerInv, buildAggregate(WIDTH, HEIGHT));
    }
}
