package com.donopot.voiddynasty.weapon;

import com.donopot.voiddynasty.registry.ModEntities;
import com.donopot.voiddynasty.terrain.TerrainDamageManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Projectile physique des armes : vitesse, gravité, pénétration, explosion, effets.
 * Le rendu visuel est une traînée de particules côté CLIENT (dense et fluide).
 * Le type de traînée est dérivé de la définition d'arme, synchronisée via l'ID réseau.
 */
public class BoltProjectile extends net.minecraft.world.entity.projectile.ThrowableProjectile {

    /** ID réseau de la définition d'arme (permet au client de connaître le tracer/impact). */
    private static final EntityDataAccessor<Integer> DATA_WEAPON_ID =
            SynchedEntityData.defineId(BoltProjectile.class, EntityDataSerializers.INT);

    private WeaponDefinition def;
    private float damage;
    private int penetrationLeft;
    private double traveled;

    public BoltProjectile(EntityType<? extends BoltProjectile> type, Level level) {
        super(type, level);
        this.def = WeaponDefinitions.BOLTGUN; // défaut, résolu via l'ID au premier sync
        this.damage = 1f;
    }

    public BoltProjectile(Level level, LivingEntity shooter, WeaponDefinition def) {
        this(ModEntities.BOLT_PROJECTILE, level);
        this.def = def;
        this.damage = def.damage();
        this.penetrationLeft = def.projectile().penetrationEntities();
        setOwner(shooter);
        setPos(shooter.getEyePosition());
        this.entityData.set(DATA_WEAPON_ID, WeaponDefinitions.idOf(def));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_WEAPON_ID, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_WEAPON_ID.equals(key)) {
            this.def = WeaponDefinitions.byId(entityData.get(DATA_WEAPON_ID));
        }
    }

    @Override
    protected double getDefaultGravity() {
        // blocs/seconde² -> valeur par tick (1 bloc/s² = 1/400 bloc/tick²)
        return def.projectile().gravity() / 400.0;
    }

    @Override
    public void tick() {
        Vec3 prev = position();
        super.tick();
        if (level().isClientSide()) {
            spawnClientTrail(prev);
        } else {
            // portée max : destruction au-delà
            traveled += getDeltaMovement().length();
            if (traveled > def.maxRange()) {
                discard();
            }
        }
    }

    /** Traînée de particules côté CLIENT — interpolée le long du segment et toujours visible. */
    private void spawnClientTrail(Vec3 prev) {
        ParticleOptions tracer = def.tracer();
        Vec3 cur = position();
        double dist = prev.distanceTo(cur);
        int steps = Math.max(1, (int) Math.ceil(dist / 0.5));
        if (steps > 24) steps = 24;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            spawnTrailParticle(tracer,
                    prev.x + (cur.x - prev.x) * t,
                    prev.y + (cur.y - prev.y) * t,
                    prev.z + (cur.z - prev.z) * t);
        }
    }

    private void spawnTrailParticle(ParticleOptions tracer, double x, double y, double z) {
        if (tracer == ParticleTypes.FLAME) {
            // Jet de feu : flammes qui montent + fumée
            level().addAlwaysVisibleParticle(ParticleTypes.FLAME, x, y, z,
                    (random.nextDouble() - 0.5) * 0.03, 0.05, (random.nextDouble() - 0.5) * 0.03);
            if (random.nextInt(3) == 0) {
                level().addAlwaysVisibleParticle(ParticleTypes.SMOKE, x, y, z,
                        (random.nextDouble() - 0.5) * 0.02, 0.06, (random.nextDouble() - 0.5) * 0.02);
            }
        } else if (tracer == ParticleTypes.ELECTRIC_SPARK) {
            // Projectile cinétique : étincelles
            level().addAlwaysVisibleParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                    (random.nextDouble() - 0.5) * 0.05, (random.nextDouble() - 0.5) * 0.05,
                    (random.nextDouble() - 0.5) * 0.05);
        } else {
            // Traceur dust (plasma vert / laser rouge) : point lumineux stable
            level().addAlwaysVisibleParticle(tracer, x, y, z, 0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) return; // serveur autoritaire (dégâts/effets)
        Entity target = result.getEntity();
        if (target == getOwner()) return;

        DamageCalculator.apply(this, target, def, damage, result.getLocation());

        // pénétration : traverse et continue, sinon explose/détruit
        if (penetrationLeft > 0) {
            penetrationLeft--;
            return;
        }
        impact(result.getLocation());
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide()) return; // serveur autoritaire
        // Dégâts de terrain (fissures + destruction au seuil).
        Vec3 ownerPos = getOwner() != null ? getOwner().getEyePosition() : position();
        TerrainDamageManager.onImpact((ServerLevel) level(), result.getBlockPos(),
                ownerPos, getDeltaMovement(), def.terrain());
        impact(result.getLocation());
        discard();
    }

    private void impact(Vec3 pos) {
        if (level().isClientSide()) return;
        ServerLevel sl = (ServerLevel) level();
        // particules d'impact
        sl.sendParticles(def.impact(), pos.x, pos.y, pos.z, 14, 0.2, 0.2, 0.2, 0.08);
        if (def.impact() == ParticleTypes.FLAME) {
            // impact de feu : fumée dense + grandes volutes
            sl.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 8, 0.15, 0.15, 0.15, 0.03);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 3, 0.2, 0.2, 0.2, 0.02);
        } else {
            sl.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 5, 0.1, 0.1, 0.1, 0.02);
        }
        // explosion de zone sur entités (le terrain est géré par TerrainDamageManager)
        if (def.projectile().explosionRadius() > 0f) {
            WeaponService.explode((ServerLevel) level(), pos, def.projectile().explosionRadius(),
                    def.projectile().explosionDamage(), getOwner(), def.family());
        }
        // détonation différée (bolts) : petit bonus de zone
        if (def.effects().contains(EffectType.DELAYED_DETONATION)) {
            WeaponService.explode((ServerLevel) level(), pos, 0.6f, 4f, getOwner(), def.family());
        }
    }

    /** Position du point d'impact. */
    public Vec3 impactPos() {
        return position();
    }
}
