package com.donopot.voiddynasty;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Foreuse laser : arme de collecte qui creuse une ligne de blocs devant le joueur
 * (rayon laser) et met directement les ressources récoltées dans son inventaire.
 */
public class LaserDrillItem extends Item {

    private static final int RANGE = 8;
    private static final int COOLDOWN_TICKS = 8;

    public LaserDrillItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            ItemStack tool = player.getItemInHand(hand);

            for (int i = 1; i <= RANGE; i++) {
                BlockPos bp = BlockPos.containing(eye.add(look.scale(i)));
                BlockState state = level.getBlockState(bp);
                if (!state.isAir() && state.getDestroySpeed(level, bp) >= 0) {
                    // récolte les drops et les met dans l'inventaire du joueur
                    for (ItemStack drop : Block.getDrops(state, sl, bp, level.getBlockEntity(bp))) {
                        if (!player.getInventory().add(drop)) {
                            Containers.dropItemStack(level, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, drop);
                        }
                    }
                    level.destroyBlock(bp, false, null, 512);
                }
            }

            player.getCooldowns().addCooldown(tool, COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }
}
