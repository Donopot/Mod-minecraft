package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.BrewingFuel;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Alambic à Prométhium : infusion 8x plus vite, carburant prométhium.
 * Le prométhium est accepté comme carburant (composant BREWING_FUEL), et cet
 * alambic applique un multiplicateur de vitesse supérieur à l'alambic vanilla.
 */
public class PromethiumBrewingStandBlockEntity extends BrewingStandBlockEntity {

    public static final int PROMETHIUM_USES = 32;
    public static final float PROMETHIUM_SPEED = 8.0f;

    public PromethiumBrewingStandBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected int getUses(ServerLevel level, BrewingFuel fuel) {
        return PROMETHIUM_USES;
    }

    @Override
    protected float getSpeedMultiplier(ServerLevel level, BrewingFuel fuel) {
        return PROMETHIUM_SPEED;
    }
}
