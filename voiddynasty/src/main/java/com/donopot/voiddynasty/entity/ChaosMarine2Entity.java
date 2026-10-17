package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Space Marine du Chaos d'assaut — phase E, lot 2.
 * Tir et mêlée.
 * PV et dégâts : valeurs de la source (datapacks Astartes) ; les fourchettes de dégâts sont ramenées
 * à leur milieu, un attribut ne sachant pas exprimer un intervalle.
 */
public class ChaosMarine2Entity extends ChaosCultistEntity {

    public ChaosMarine2Entity(EntityType<? extends Monster> type, Level level) {
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
        return "attack";
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
        return new ProfilAttaque(4.0, 15.0, 30,
                0.0, 3.0, 20);
    }
}
