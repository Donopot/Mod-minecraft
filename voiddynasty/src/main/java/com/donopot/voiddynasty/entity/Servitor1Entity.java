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

    private boolean workMode;

    /** Keep native navigation/move controls, remove combat and wandering only for workers. */
    public void setWorkMode() {
        if (workMode) {
            // Un worker recharge depuis une sauvegarde arrive en NoAI=true (voir
            // readAdditionalSaveData). Le drapeau workMode est deja vrai : sans cette ligne,
            // setWorkMode() ne rendait jamais l'IA et le compagnon restait immobile pour toujours.
            setNoAi(false);
            return;
        }
        workMode = true;
        addTag("voiddynasty_work");
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        setTarget(null);
        setNoAi(false);
        setCanPickUpLoot(false);
        // Work routes can need a long detour to exit a mined passage and reach the depot.
        getNavigation().setRequiredPathLength(64.0f);
        getNavigation().stop();
    }

    /**
     * Un compagnon recharge depuis une sauvegarde reste volontairement immobile (setNoAi(true))
     * jusqu'a ce qu'un ordre explicite le reveille. Sans cela, un simple deplacement ne fait rien.
     */
    public void wake() {
        if (entityTags().contains("voiddynasty_work")) setWorkMode();
        setNoAi(false);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        if (entityTags().contains("voiddynasty_work")) {
            setWorkMode();
            // Loaded workers stay still until the operator explicitly resumes the saved job.
            setNoAi(true);
        }
    }

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
        return "idle";
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
