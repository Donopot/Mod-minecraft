package com.donopot.voiddynasty.entity;

import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Capitaine du Chaos — boss de la famille Space Marine (phase E, lot 2).
 * 250 PV et 10-15 dégâts par la source ; bornes ramenées à leur milieu (12.5).
 */
public class ChaosCaptainEntity extends ChaosBossEntity {

    public ChaosCaptainEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "entity.voiddynasty.chaos_captain", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(250.0, 12.5, 0.23);
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

    /** Plages et cooldowns de la source (datapacks Astartes), transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(4.0, 15.0, 40,
                0.0, 3.0, 20);
    }
}
