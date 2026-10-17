package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Dreadnought du Chaos — phase E, lot 4.
 * 150 PV, 10 dégâts au contact comme au tir. Lenteur permanente (source).
 */
public class ChaosDreadnoughtEntity extends ChaosCultistEntity {

    public ChaosDreadnoughtEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(150.0, 10.0, 0.2);
    }

    /** Plages et cooldowns de la source, transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(3.0, 20.0, 60, 0.0, 2.0, 40);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Lenteur permanente : c'est le procédé de la source pour ces machines.
        if (!this.level().isClientSide() && !this.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)) {
            this.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SLOWNESS, 200, 0, false, false));
        }
    }

    @Override
    protected String clipIdle() {
        return "walk";
    }

    @Override
    protected String clipWalk() {
        return "walk";
    }

    @Override
    protected String clipAttack() {
        return "attack";
    }
}
