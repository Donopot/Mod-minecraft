package com.donopot.voiddynasty.entity;

import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Prêtresse du Chaos — boss de la famille cultiste (phase E, lot 1).
 *
 * 100 PV et 5-8 dégâts par la source (datapacks Astartes) ; les bornes de dégâts sont remplacées par
 * leur milieu (6.5), faute de pouvoir exprimer un intervalle dans un attribut.
 */
public class CultistBossEntity extends ChaosBossEntity {

    public CultistBossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "entity.voiddynasty.cultist_boss", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(100.0, 6.5, 0.23);
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
        return new ProfilAttaque(4.0, 10.0, 60,
                2.0, 4.0, 60);
    }
}
