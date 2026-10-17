package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Servitor lourd — phase E, lot 3.
 * 100 PV et 15 dégâts au contact sur 4 blocs (source). Ce sont des valeurs EXACTES de la source, pas des fourchettes.

 */
public class Servitor3Entity extends ChaosCultistEntity {

    public Servitor3Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(100.0, 15.0, 0.23);
    }

    /** Plages et cooldowns de la source, transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(0.0, 0.0, 0, 0.0, 3.0, 60);
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
        return "smash";
    }

    @Override
    protected String clipShoot() {
        return null;
    }
}
