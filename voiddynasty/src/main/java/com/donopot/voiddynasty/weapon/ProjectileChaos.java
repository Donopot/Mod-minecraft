package com.donopot.voiddynasty.weapon;

import com.donopot.voiddynasty.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Projectile tiré par les ennemis du Chaos (phase E).
 *
 * La source résout ses tirs par un raytrace pas-à-pas : c'est une contrainte de datapack, pas une
 * intention de conception. On garde l'intention — une attaque à distance qui inflige les dégâts du mob
 * à portée — avec un projectile visible, ce qui est plus lisible en jeu.
 *
 * Différence assumée avec le projectile des armes : AUCUN dégât de terrain. La source ne fait porter
 * ses tirs que sur les entités ; laisser un serviteur fissurer le décor serait un comportement ajouté.
 */
public class ProjectileChaos extends net.minecraft.world.entity.projectile.ThrowableProjectile {

    private static final EntityDataAccessor<Float> DATA_DEGATS =
            SynchedEntityData.defineId(ProjectileChaos.class, EntityDataSerializers.FLOAT);

    private static final double PORTEE_MAX = 48.0;
    private double parcouru;

    public ProjectileChaos(EntityType<? extends ProjectileChaos> type, Level level) {
        super(type, level);
    }

    public ProjectileChaos(Level level, LivingEntity tireur, float degats) {
        this(ModEntities.PROJECTILE_CHAOS, level);
        this.entityData.set(DATA_DEGATS, degats);
        setOwner(tireur);
        setPos(tireur.getEyePosition());
    }

    public float degats() {
        return this.entityData.get(DATA_DEGATS);
    }

    /** Visée : légèrement au-dessus de la cible, pour ne pas buter sur le sol. */
    public void viser(Vec3 cible) {
        Vec3 depart = position();
        Vec3 direction = cible.subtract(depart).normalize().scale(1.6);
        this.setDeltaMovement(direction);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DEGATS, 1.0f);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.002;                 // trajectoire quasiment tendue sur une courte portée
    }

    @Override
    public void tick() {
        Vec3 precedent = position();
        super.tick();
        if (level().isClientSide()) {
            ParticleOptions trace = ParticleTypes.ELECTRIC_SPARK;
            double distance = precedent.distanceTo(position());
            int pas = Math.max(1, Math.min(12, (int) Math.ceil(distance / 0.5)));
            for (int i = 0; i <= pas; i++) {
                double t = i / (double) pas;
                level().addAlwaysVisibleParticle(trace,
                        precedent.x + (position().x - precedent.x) * t,
                        precedent.y + (position().y - precedent.y) * t,
                        precedent.z + (position().z - precedent.z) * t, 0, 0, 0);
            }
        } else {
            this.parcouru += getDeltaMovement().length();
            if (this.parcouru > PORTEE_MAX) {
                discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) return;
        Entity cible = result.getEntity();
        if (cible == getOwner()) return;
        DamageSource source = this.damageSources().thrown(this, getOwner());
        cible.hurtServer((ServerLevel) level(), source, degats());
        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide()) return;
        // aucun dégât de terrain : la source ne tire que sur les entités
        discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putFloat("degats", degats());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(DATA_DEGATS, input.getFloatOr("degats", 1.0f));
    }
}
