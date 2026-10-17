package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Cultiste armé — phase E.
 * Deux attaques distinctes (attack1, attack2).
 * PV et dégâts : valeurs de la source (datapacks Astartes), jamais estimées.
 */
public class Cultist3Entity extends ChaosCultistEntity {

    public Cultist3Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(20.0, 4.5, 0.23);
    }

    /**
     * Clip d'arret : la source n'en fournit pas pour ce culte. On reprend celui du culte 1,
     * qui partage le meme squelette (14 a 15 os communs) — decision declaree de la passe de
     * finition, plutot que de laisser le mob marcher sur place. Voir
     * tools/ajouter_idle_cultistes.py.
     */
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
        return "attack";
    }

    @Override
    protected String clipDeath() {
        return "death";
    }

    /** Plages et cooldowns de la source (datapacks Astartes), transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(4.0, 6.0, 40,
                0.0, 2.0, 40);
    }
}
