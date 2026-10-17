package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Servitor de combat — phase E, lot 3.
 * 200 PV et 10 dégâts au contact (source). Ce sont des valeurs EXACTES de la source, pas des fourchettes.

 */
public class Servitor2Entity extends ChaosCultistEntity {

    public Servitor2Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(200.0, 10.0, 0.23);
    }

    /** Plages et cooldowns de la source, transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(0.0, 0.0, 0, 0.0, 2.0, 20);
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

    @Override
    protected String clipShoot() {
        return null;
    }
}
