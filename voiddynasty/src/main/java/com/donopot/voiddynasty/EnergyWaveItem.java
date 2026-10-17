package com.donopot.voiddynasty;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnergyWaveItem extends Item {
    private static final int RANGE = 18;      // beam length in blocks
    private static final int COOLDOWN_TICKS = 40; // 2 s

    public EnergyWaveItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            // Beam: carve a tunnel ahead of the player + energy particles along the path.
            for (int i = 1; i <= RANGE; i++) {
                Vec3 p = eye.add(look.scale(i));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos bp = BlockPos.containing(p.x + dx, p.y + dy, p.z + dz);
                            var state = level.getBlockState(bp);
                            // destroy anything solid except bedrock (getDestroySpeed < 0)
                            if (!state.isAir() && state.getDestroySpeed(level, bp) >= 0) {
                                level.destroyBlock(bp, true, null, 512);
                            }
                        }
                    }
                }
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 4, 0.25, 0.25, 0.25, 0.02);
                }
            }

            // Final DBZ-style explosion at the impact point (destroys blocks + particles + sound).
            Vec3 end = eye.add(look.scale(RANGE));
            level.explode(null, end.x, end.y, end.z, 4.0f, false, Level.ExplosionInteraction.BLOCK);

            // Shockwave flash at the player.
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, eye.x, eye.y, eye.z, 1, 0, 0, 0, 0);
            }

            player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }
}
