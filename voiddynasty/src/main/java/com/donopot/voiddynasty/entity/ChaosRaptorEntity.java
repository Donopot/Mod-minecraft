package com.donopot.voiddynasty.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Raptor du Chaos — phase E, lot 2.
 *
 * 50 PV et 10 dégâts par la source. Son « vol » est celui de la source : celle-ci ne lui donne pas un
 * vrai déplacement aérien mais une vitesse accrue, un saut puissant et une chute lente. On reproduit
 * exactement ce procédé — inventer un vol libre serait ajouter du comportement que la source n'a pas.
 */
public class ChaosRaptorEntity extends ChaosCultistEntity {

    public ChaosRaptorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(50.0, 10.0, 0.32)
                .add(Attributes.JUMP_STRENGTH, 1.4)
                .add(Attributes.SAFE_FALL_DISTANCE, 20.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Chute lente renouvelée : c'est le procédé de la source, et il ne s'applique qu'au serveur.
        if (!this.level().isClientSide() && !this.hasEffect(MobEffects.SLOW_FALLING)) {
            this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
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
        return "attack";
    }

    @Override
    protected String clipShoot() {
        return "shoot";
    }

    /** Plages et cooldowns de la source (datapacks Astartes), transcrits tels quels. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(4.0, 15.0, 30,
                0.0, 3.0, 20);
    }
}
