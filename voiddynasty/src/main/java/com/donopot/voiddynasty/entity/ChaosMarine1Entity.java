package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Space Marine du Chaos — phase E, lot 2.
 * La source ne lui donne que le tir (pas de mêlée).
 * PV et dégâts : valeurs de la source (datapacks Astartes) ; les fourchettes de dégâts sont ramenées
 * à leur milieu, un attribut ne sachant pas exprimer un intervalle.
 */
public class ChaosMarine1Entity extends ChaosCultistEntity {

    public ChaosMarine1Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(50.0, 17.5, 0.23);
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
        return "shoot";
    }

    @Override
    protected String clipDeath() {
        return null;
    }

    /** Plages et cooldowns de la source (datapacks Astartes), transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(0.0, 30.0, 10,
                0.0, 0.0, 0);
    }
}
