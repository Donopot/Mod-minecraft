package com.donopot.voiddynasty.weapon;

import com.donopot.voiddynasty.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Rayon laser : une ligne lumineuse instantanée entre le canon (start) et le
 * point d'impact (end). Contrairement au projectile, le rayon apparaît sur
 * toute sa longueur d'un coup et s'éteint après 2 ticks.
 *
 * Le rendu est une traînée de poussière colorée (dust) spawnée côté CLIENT
 * le long du segment via {@code addAlwaysVisibleParticle} (jamais ignorée par
 * le budget particules), la même technique éprouvée que la traînée des
 * projectiles physiques.
 */
public class LaserBeamEntity extends Entity {

    private static final EntityDataAccessor<Float> START_X =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_X =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(LaserBeamEntity.class, EntityDataSerializers.INT);

    /** Épaisseur du rayon (taille de chaque particule de poussière, en blocs). */
    private static final float BEAM_THICKNESS = 1.5f;
    /** Durée de vie du rayon (ticks). */
    private static final int MAX_AGE = 4;

    private int age;

    public LaserBeamEntity(EntityType<? extends LaserBeamEntity> type, Level level) {
        super(type, level);
    }

    /** Rayon de {@code start} vers {@code end}, dans la couleur ARGB donnée. */
    public LaserBeamEntity(Level level, Vec3 start, Vec3 end, int color) {
        this(ModEntities.LASER_BEAM, level);
        setPos(start.x, start.y, start.z);
        entityData.set(START_X, (float) start.x);
        entityData.set(START_Y, (float) start.y);
        entityData.set(START_Z, (float) start.z);
        entityData.set(END_X, (float) end.x);
        entityData.set(END_Y, (float) end.y);
        entityData.set(END_Z, (float) end.z);
        entityData.set(COLOR, color);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(START_X, 0f);
        builder.define(START_Y, 0f);
        builder.define(START_Z, 0f);
        builder.define(END_X, 0f);
        builder.define(END_Y, 0f);
        builder.define(END_Z, 0f);
        builder.define(COLOR, 0xFFFF3C3C); // rouge laser par défaut
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            spawnBeamParticles();
        }
        if (++age >= MAX_AGE) {
            discard();
        }
    }

    /** Dessine le rayon : poussière colorée dense le long de start -> end. */
    private void spawnBeamParticles() {
        Vec3 start = new Vec3(
                entityData.get(START_X).doubleValue(),
                entityData.get(START_Y).doubleValue(),
                entityData.get(START_Z).doubleValue());
        Vec3 end = new Vec3(
                entityData.get(END_X).doubleValue(),
                entityData.get(END_Y).doubleValue(),
                entityData.get(END_Z).doubleValue());
        double dist = start.distanceTo(end);
        if (dist < 0.2) return;

        DustParticleOptions dust = new DustParticleOptions(entityData.get(COLOR), BEAM_THICKNESS);
        int steps = (int) Math.min(dist * 2.0, 300);
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            level().addAlwaysVisibleParticle(dust,
                    start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t,
                    start.z + (end.z - start.z) * t,
                    0, 0, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // noSave() : jamais persisté.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // noSave() : jamais persisté.
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // rayon non interactif, ne peut pas être endommagé
    }
}
