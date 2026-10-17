package com.donopot.voiddynasty;

import com.donopot.voiddynasty.entity.ServoSkullEntity;
import com.donopot.voiddynasty.registry.ModEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Servo-crâne (item) : invoque un servo-crâne lié au joueur qui le suit et collecte
 * les items autour de lui.
 */
public class ServoSkullItem extends Item {

    public ServoSkullItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ServoSkullEntity skull = ModEntities.SERVO_SKULL.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (skull != null) {
                skull.setOwnerUUID(player.getUUID());
                skull.setPos(player.getX(), player.getY() + 1.5, player.getZ());
                skull.setYRot(player.getYRot());
                skull.setXRot(0.0f);
                level.addFreshEntity(skull);
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
