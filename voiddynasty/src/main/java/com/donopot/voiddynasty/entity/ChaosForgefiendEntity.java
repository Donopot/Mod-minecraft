package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Forgefiend — phase E, lot 4.
 * 400 PV. Contact 30 dégâts, tir 10 dégâts PAR OBUS et cinq obus par salve (source). Barre de boss.
 */
public class ChaosForgefiendEntity extends ChaosBossEntity {

    public ChaosForgefiendEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "entity.voiddynasty.chaos_forgefiend", net.minecraft.world.BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(400.0, 30.0, 0.2);
    }

    /** Plages et cooldowns de la source, transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(7.0, 100.0, 10, 0.0, 6.0, 60);
    }

    /** Le forgefiend tire à 10 dégâts par obus, pas 30 : la source distingue ses deux attaques. */
    @Override
    protected float degatsTir() {
        return 10.0f;
    }

    /** « 5 raytraces » dans la source = cinq obus par salve. */
    @Override
    protected int nombreProjectiles() {
        return 5;
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
        return "strike";
    }
}
