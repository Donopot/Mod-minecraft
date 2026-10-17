package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Cultiste — phase E.
 * Premier culte : la base humanoïde, attaque au contact.
 * PV et dégâts : valeurs de la source (datapacks Astartes), jamais estimées.
 */
public class Cultist1Entity extends ChaosCultistEntity {

    public Cultist1Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(20.0, 4.5, 0.23);
    }

    @Override
    protected String clipIdle() {
        return "idle";
    }

    @Override
    protected String clipWalk() {
        return "walk";
    }

    @Override
    protected String clipAttack() {
        return "shoot";
    }

    @Override
    protected String clipDeath() {
        return "death";
    }

    /** Plages et cooldowns de la source (datapacks Astartes), transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(0.0, 25.0, 30,
                0.0, 0.0, 0);
    }
}
