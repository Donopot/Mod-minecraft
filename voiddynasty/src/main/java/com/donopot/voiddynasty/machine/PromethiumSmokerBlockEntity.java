package com.donopot.voiddynasty.machine;

import com.donopot.voiddynasty.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fumoir à Prométhium : cuisson de nourriture 8x plus vite, carburant prométhium.
 */
public class PromethiumSmokerBlockEntity extends SmokerBlockEntity {

    public PromethiumSmokerBlockEntity(BlockPos pos, BlockState state) {
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
