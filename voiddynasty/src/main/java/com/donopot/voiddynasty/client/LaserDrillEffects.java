package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.LaserDrillItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/** Cosmetic, bounded client effects driven by vanilla use-state synchronization. */
@Environment(EnvType.CLIENT)
public final class LaserDrillEffects {
    private static final Map<UUID, DrillHum> SOUNDS = new HashMap<>();
    private LaserDrillEffects() {}
    public static void init() { ClientTickEvents.END_CLIENT_TICK.register(LaserDrillEffects::tick); }

    private static void tick(Minecraft client) {
        var active = new HashSet<UUID>();
        if (client.level != null && client.player != null && !client.isPaused()) for (Player player : client.level.players()) {
            if (!drilling(player) || player.distanceToSqr(client.player) > 32 * 32) continue;
            active.add(player.getUUID());
            DrillHum sound = SOUNDS.get(player.getUUID());
            if (sound == null || sound.player != player || sound.isStopped()) {
                if (sound != null) client.getSoundManager().stop(sound);
                sound = new DrillHum(player);
                SOUNDS.put(player.getUUID(), sound);
                client.getSoundManager().play(sound);
            }
            if (client.level.getGameTime() % 2 == 0) particles(client, player);
        }
        SOUNDS.entrySet().removeIf(entry -> {
            if (active.contains(entry.getKey())) return false;
            client.getSoundManager().stop(entry.getValue());
            return true;
        });
    }

    private static boolean drilling(Player player) {
        return player.isAlive() && !player.isRemoved() && !player.isSpectator() && player.isUsingItem()
                && player.getUseItem().getItem() instanceof LaserDrillItem;
    }

    private static void particles(Minecraft client, Player player) {
        boolean firstPerson = player == client.player && client.options.getCameraType().isFirstPerson();
        Vec3 muzzle = LaserDrillBeamRenderer.muzzle(player, player.getEyePosition(), player.getLookAngle(), firstPerson);
        int elapsed = player.getTicksUsingItem();
        if (elapsed < LaserDrillItem.CHARGE_TICKS) {
            client.level.addParticle(new DustParticleOptions(0xFF42EBDD, 0.65f), muzzle.x, muzzle.y, muzzle.z, 0, 0, 0);
            return;
        }
        var hit = LaserDrillItem.target(client.level, player);
        Vec3 end = hit.getLocation();
        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
            Vec3 impact = end.add(normal.scale(0.04));
            var random = client.level.getRandom();
            for (int i=0; i<4; i++) client.level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    impact.x, impact.y, impact.z,
                    normal.x * 0.04 + (random.nextDouble()-0.5)*0.09,
                    normal.y * 0.04 + random.nextDouble()*0.06,
                    normal.z * 0.04 + (random.nextDouble()-0.5)*0.09);
            if (elapsed % 4 == 0) client.level.addParticle(ParticleTypes.SMOKE, impact.x, impact.y, impact.z, 0, 0.015, 0);
        }
    }

    private static final class DrillHum extends AbstractTickableSoundInstance {
        private final Player player;
        private DrillHum(Player player) {
            super(SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, RandomSource.create());
            this.player = player;
            looping = true;
            delay = 0;
            volume = 0.22f;
            pitch = 1.35f;
            tick();
        }
        @Override
        public void tick() {
            if (!drilling(player)) { stop(); return; }
            x = player.getX(); y = player.getY() + 1; z = player.getZ();
            float charge = Math.min(1, player.getTicksUsingItem() / (float)LaserDrillItem.CHARGE_TICKS);
            volume = 0.1f + charge * 0.14f;
            pitch = 1.1f + charge * 0.45f;
        }
    }
}
