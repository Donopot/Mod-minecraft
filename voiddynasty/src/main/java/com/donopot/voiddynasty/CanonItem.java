package com.donopot.voiddynasty;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Plasma cannon: fires a vanilla LargeFireball (renderer + explosion built-in)
 * with an increased explosion power. Pure server-side, no custom entity needed.
 */
public class CanonItem extends Item {
    private static final int COOLDOWN_TICKS = 40;  // 2 s
    private static final int EXPLOSION_POWER = 4;  // large explosion
    private static final double SPEED = 2.5;

    public CanonItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            LargeFireball fireball = new LargeFireball(level, player, look, EXPLOSION_POWER);
            fireball.setPos(eye.x, eye.y, eye.z);
            fireball.setDeltaMovement(look.scale(SPEED));
            sl.addFreshEntity(fireball);

            sl.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }
}
