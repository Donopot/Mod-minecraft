package com.donopot.voiddynasty.machine;

import com.donopot.voiddynasty.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Four à Prométhium : accepte le prométhium comme carburant (longue durée) et
 * cuit 8x plus vite que le four vanilla (multiplicateur de vitesse).
 */
public class PromethiumFurnaceBlockEntity extends FurnaceBlockEntity {

    /** Durée de combustion d'un prométhium (ticks). */
    public static final int PROMETHIUM_BURN_TIME = 1600;
    /** Multiplicateur de vitesse de cuisson avec le prométhium. */
    public static final float PROMETHIUM_SPEED = 8.0f;

    public PromethiumFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected int getBurnDuration(ServerLevel level, ItemStack stack) {
        if (stack.is(ModItems.PROMETHIUM)) {
            return PROMETHIUM_BURN_TIME;
        }
        return super.getBurnDuration(level, stack);
    }

    @Override
    protected float getSpeedMultiplier(ServerLevel level, ItemStack stack) {
        if (stack.is(ModItems.PROMETHIUM)) {
            return PROMETHIUM_SPEED;
        }
        return super.getSpeedMultiplier(level, stack);
    }
}
