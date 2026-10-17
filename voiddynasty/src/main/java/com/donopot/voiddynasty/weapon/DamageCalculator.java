package com.donopot.voiddynasty.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Calcul des dégâts (headshot, critique, perce-armure, falloff) + application des effets.
 */
public final class DamageCalculator {

    private DamageCalculator() {
    }

    /** Dégâts via projectile physique. */
    public static void apply(BoltProjectile projectile, Entity target, WeaponDefinition def,
                             float damage, Vec3 hitPos) {
        ServerLevel level = (ServerLevel) target.level();
        Entity owner = projectile.getOwner();
        DamageSource source = source(level, owner);
        Vec3 knockbackDir = projectile.getDeltaMovement();
        if (knockbackDir.lengthSqr() > 1.0E-7) knockbackDir = knockbackDir.normalize();
        applyDamage(level, source, owner, target, def, damage, hitPos, knockbackDir);
    }

    /** Dégâts directs (hitscan laser/melta ou mêlée). */
    public static void applyDirect(ServerPlayer player, LivingEntity target, WeaponDefinition def, Vec3 hitPos) {
        ServerLevel level = player.level();
        DamageSource source = level.damageSources().playerAttack(player);
        Vec3 dir = target.position().subtract(player.position());
        if (dir.lengthSqr() > 1.0E-7) dir = dir.normalize();
        applyDamage(level, source, player, target, def, def.damage(), hitPos, dir);
    }

    private static void applyDamage(ServerLevel level, DamageSource source, Entity owner,
                                    Entity target, WeaponDefinition def, float damage,
                                    Vec3 hitPos, Vec3 knockbackDir) {
        float multiplier = 1f;
        boolean headshot = false;
        boolean crit = false;

        if (target instanceof LivingEntity le && hitPos != null) {
            double relY = hitPos.y - le.getY();
            double height = Math.max(0.5, le.getBbHeight());
            if (relY >= height * 0.85) {
                // tête : headshot
                multiplier *= def.headshotMultiplier();
                headshot = true;
            } else if (relY < height * 0.35) {
                // jambes : dégâts réduits
                multiplier *= 0.85f;
            }
            // sinon torse : ×1.0
        }
        if (level.getRandom().nextFloat() < def.critChance()) {
            multiplier *= def.criticalMultiplier();
            crit = true;
        }

        float finalDamage = damage * multiplier;

        if (target instanceof LivingEntity le && def.armorPenetration() < 1f) {
            float reduction = Math.min(0.8f, le.getArmorValue() * 0.04f) * (1f - def.armorPenetration());
            finalDamage *= (1f - reduction);
        }

        if (def.falloff() != null && owner != null) {
            double dist = target.distanceTo(owner);
            WeaponDefinition.DamageFalloff f = def.falloff();
            if (dist > f.start()) {
                float t = (float) Math.min(1.0, (dist - f.start()) / Math.max(1.0, f.end() - f.start()));
                finalDamage *= (1f - t * (1f - f.minMultiplier()));
            }
        }

        boolean hurt = target.hurtServer(level, source, finalDamage);

        if (hurt && target instanceof LivingEntity le) {
            if (headshot) {
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(),
                        target.getY() + le.getBbHeight() * 0.9, target.getZ(),
                        4, 0.2, 0.2, 0.2, 0.05);
            }
            if (crit) {
                level.sendParticles(ParticleTypes.CRIT, target.getX(),
                        target.getY() + le.getBbHeight() / 2.0, target.getZ(),
                        8, 0.3, 0.3, 0.3, 0.1);
            }
            for (EffectType effect : def.effects()) {
                applyEffect(le, effect);
            }
            if (def.effects().contains(EffectType.KNOCKBACK)) {
                le.setDeltaMovement(le.getDeltaMovement().add(
                        knockbackDir.x * 0.7, 0.35, knockbackDir.z * 0.7));
            }
        }
    }

    private static DamageSource source(ServerLevel level, Entity owner) {
        if (owner instanceof ServerPlayer sp) return level.damageSources().playerAttack(sp);
        if (owner instanceof LivingEntity le) return level.damageSources().mobAttack(le);
        return level.damageSources().generic();
    }

    /** Applique un effet de statut (mapping vers les effets vanilla). */
    public static void applyEffect(LivingEntity target, EffectType effect) {
        switch (effect) {
            case STAGGER -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 12, 2));
            case BLEED -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            case IGNITE -> target.igniteForTicks(40);
            case BURN -> target.igniteForTicks(100);
            case INFERNO -> target.igniteForTicks(160);
            case PANIC -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1));
            case SUPPRESSION -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
            case ARMOR_MELT -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2));
            case ENERGY_DISRUPTION -> {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 0));
            }
            case DELAYED_DETONATION, KNOCKBACK -> {
                // gérés ailleurs (explosion / vélocité)
            }
        }
    }
}
