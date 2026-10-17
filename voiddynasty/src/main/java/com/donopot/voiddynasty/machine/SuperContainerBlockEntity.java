package com.donopot.voiddynasty.machine;

import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Super Conteneur (6x2) : chaque bloc détient 54 slots (6x9). La fusion d'un
 * rectangle 6x2 complet agrège 12 blocs = 648 slots (12x la contenance d'un grand coffre).
 */
public class SuperContainerBlockEntity extends AbstractSuperContainerBlockEntity {

    public static final int SLOTS_PER_BLOCK = 54;
    public static final int WIDTH = 6;
    public static final int HEIGHT = 2;

    public SuperContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPER_CONTAINER, pos, state, SLOTS_PER_BLOCK);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.voiddynasty.super_container");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInv) {
        return new SuperChestMenu(syncId, playerInv, buildAggregate(WIDTH, HEIGHT));
    }
}
