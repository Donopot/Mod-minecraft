package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Attaque au contact d'un ennemi du Chaos, réglée par son {@link ProfilAttaque}.
 *
 * Remplace le {@code MeleeAttackGoal} générique : la source donne à chaque mob sa propre plage de
 * contact et son propre cooldown (un marine frappe à 3 blocs toutes les secondes, un hellbrute à
 * 10 blocs toutes les deux secondes). Le comportement vanilla ne sait pas exprimer cela.
 */
public class CacChaosGoal extends Goal {

    private final ChaosCultistEntity mob;

    public CacChaosGoal(ChaosCultistEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        ProfilAttaque profil = this.mob.profil();
        LivingEntity cible = this.mob.getTarget();
        if (cible == null || !profil.aCac() || this.mob.cooldownCac() > 0) {
            return false;
        }
        return profil.peutFrapper(this.mob.distanceTo(cible)) && this.mob.hasLineOfSight(cible);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.mob.declencherAttaque(false);      // joue le clip de contact
        LivingEntity cible = this.mob.getTarget();
        if (cible != null) {
            this.mob.doHurtTarget((net.minecraft.server.level.ServerLevel) this.mob.level(), cible);
        }
        this.mob.setCooldownCac(this.mob.profil().cacCooldownTicks());
    }
}
