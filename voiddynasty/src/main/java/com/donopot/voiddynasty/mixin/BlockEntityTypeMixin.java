package com.donopot.voiddynasty.mixin;

import com.donopot.voiddynasty.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Autorise les blocs de raffinage Prométhium à réutiliser les BlockEntityType vanilla
 * (FURNACE / BLAST_FURNACE / SMOKER / BREWING_STAND), dont les validBlocks ne listent que
 * les blocs vanilla. Sans cela, la création de la BlockEntity lève IllegalStateException.
 */
@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void voiddynasty$allowPromethiumRefining(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        if (block == ModBlocks.PROMETHIUM_FURNACE
                || block == ModBlocks.PROMETHIUM_BLAST_FURNACE
                || block == ModBlocks.PROMETHIUM_SMOKER
                || block == ModBlocks.PROMETHIUM_BREWING_STAND) {
            cir.setReturnValue(true);
        }
    }
}
