package com.donopot.voiddynasty;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Pad de contrôle du vaisseau.
 * Clic droit = ouvre l'écran de contrôle (GUI) côté client.
 */
public final class ControlPadItem extends Item {

    public ControlPadItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ClientScreenOpener.open();
        }
        return InteractionResult.SUCCESS;
    }
}
