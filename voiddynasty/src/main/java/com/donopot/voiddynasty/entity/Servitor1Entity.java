package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Servitor de tir — phase E, lot 3.
 * 200 PV et 8 dégâts de tir (source). Ce sont des valeurs EXACTES de la source, pas des fourchettes.
 * La source ne lui donne qu'un clip de marche : il tire donc sans animation dédiée.
 */
public class Servitor1Entity extends ChaosCultistEntity {

    public Servitor1Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(200.0, 8.0, 0.23);
    }

    /** Plages et cooldowns de la source, transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(0.0, 30.0, 20, 0.0, 0.0, 0);
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
        return null;
    }

    @Override
    protected String clipShoot() {
        return null;
    }
}
