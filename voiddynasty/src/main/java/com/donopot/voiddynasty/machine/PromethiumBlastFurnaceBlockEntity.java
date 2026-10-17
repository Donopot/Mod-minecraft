package com.donopot.voiddynasty.machine;

import com.donopot.voiddynasty.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Haut Fourneau à Prométhium : fonte des minerais 8x plus vite, carburant prométhium.
 */
public class PromethiumBlastFurnaceBlockEntity extends BlastFurnaceBlockEntity {

    public PromethiumBlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected int getBurnDuration(ServerLevel level, ItemStack stack) {
        if (stack.is(ModItems.PROMETHIUM)) {
            return PromethiumFurnaceBlockEntity.PROMETHIUM_BURN_TIME;
        }
        return super.getBurnDuration(level, stack);
    }

    @Override
    protected float getSpeedMultiplier(ServerLevel level, ItemStack stack) {
        if (stack.is(ModItems.PROMETHIUM)) {
            return PromethiumFurnaceBlockEntity.PROMETHIUM_SPEED;
        }
        return super.getSpeedMultiplier(level, stack);
    }
}
