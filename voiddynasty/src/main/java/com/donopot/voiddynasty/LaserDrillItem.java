package com.donopot.voiddynasty;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.function.Consumer;

/** Held mining beam: charge, drill the first visible block, stop immediately on release. */
public final class LaserDrillItem extends Item {
    public static final int RANGE = 8;
    public static final int CHARGE_TICKS = 6;
    public static final int PULSE_TICKS = 8;
    private static final int USE_TICKS = 72000;

    public LaserDrillItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isSpectator()) return InteractionResult.PASS;
        player.startUsingItem(hand);
        if (level instanceof ServerLevel server)
            server.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.35f, 1.7f);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return USE_TICKS; }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.NONE; }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player) || player.isSpectator()) return;
        int elapsed = getUseDuration(stack, player) - remaining;
        if (elapsed < CHARGE_TICKS || (elapsed - CHARGE_TICKS) % PULSE_TICKS != 0) return;
        drill(server, player, stack);
    }

    /** Shared aiming rule for the authoritative mine and the client's visible impact. */
    public static BlockHitResult target(Level level, LivingEntity user) {
        var start = user.getEyePosition();
        return level.clip(new ClipContext(start, start.add(user.getLookAngle().scale(RANGE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
    }

    private static void drill(ServerLevel level, Player player, ItemStack tool) {
        BlockHitResult hit = target(level, player);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        var state = level.getBlockState(pos);
        // A mining beam stops on an obstacle: never tunnel through bedrock, protected
        // terrain or a container to collect blocks hidden behind it.
        if (!level.hasChunkAt(pos) || state.isAir() || state.getDestroySpeed(level, pos) < 0
                || state.hasBlockEntity() || !player.mayInteract(level, pos)
                || !player.mayUseItemAt(pos, hit.getDirection(), tool) || ClaimIntegration.isClaimProtected(level, pos)) return;
        var drops = Block.getDrops(state, level, pos, null, player, tool);
        if (!level.destroyBlock(pos, false, player, 512)) return;
        for (ItemStack drop : drops) {
            if (!player.getInventory().add(drop))
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(stack, 4);
            if (level instanceof ServerLevel server)
                server.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.25f, 1.9f);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.voiddynasty.laser_drill.hold"));
        tooltip.accept(Component.translatable("tooltip.voiddynasty.laser_drill.range", RANGE));
    }
}
