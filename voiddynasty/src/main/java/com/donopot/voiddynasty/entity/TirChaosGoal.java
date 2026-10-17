package com.donopot.voiddynasty.entity;

import com.donopot.voiddynasty.weapon.ProjectileChaos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Tir à distance d'un ennemi du Chaos, réglé par son {@link ProfilAttaque}.
 *
 * La source impose deux conditions pour tirer : le joueur doit être DANS LA PLAGE de distance du mob
 * ET DANS SON CHAMP DE VISION. Les deux sont appliquées ici — sans le champ de vision, un marine
 * tirerait dans le dos d'un joueur qu'il n'a pas vu.
 *
 * Une courte mise en joue précède le tir, le temps que l'animation de tir soit lisible.
 */
public class TirChaosGoal extends Goal {

    private static final int MISE_EN_JOUE_TICKS = 6;
    private static final double DEMI_ANGLE_COS = 0.5;      // ~60° de chaque côté

    private final ChaosCultistEntity mob;
    private int miseEnJoue;

    public TirChaosGoal(ChaosCultistEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        ProfilAttaque profil = this.mob.profil();
        LivingEntity cible = this.mob.getTarget();
        if (cible == null || !profil.aTir() || this.mob.cooldownTir() > 0) {
            return false;
        }
        double distance = this.mob.distanceTo(cible);
        return profil.peutTirer(distance) && voit(cible);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity cible = this.mob.getTarget();
        return cible != null && cible.isAlive() && voit(cible)
                && this.mob.profil().peutTirer(this.mob.distanceTo(cible));
    }

    @Override
    public void start() {
        this.miseEnJoue = MISE_EN_JOUE_TICKS;
        this.mob.declencherAttaque(true);       // joue le clip de tir
        this.mob.getLookControl().setLookAt(this.mob.getTarget(), 30f, 30f);
    }

    @Override
    public void tick() {
        LivingEntity cible = this.mob.getTarget();
        if (cible == null) {
            return;
        }
        this.mob.getLookControl().setLookAt(cible, 30f, 30f);
        if (--this.miseEnJoue > 0) {
            return;
        }
        if (!(this.mob.level() instanceof ServerLevel niveau)) {
            return;
        }
        int nombre = Math.max(1, this.mob.nombreProjectiles());
        for (int i = 0; i < nombre; i++) {
            ProjectileChaos tir = new ProjectileChaos(niveau, this.mob, this.mob.degatsTir());
            // Salve : chaque projectile part avec un léger écart, sinon les cinq se superposeraient
            // et ne feraient qu'un seul impact. L'écart est un besoin technique, pas une valeur de jeu.
            double ecart = nombre == 1 ? 0.0 : (i - (nombre - 1) / 2.0) * 0.08;
            tir.viser(cible.getEyePosition().add(ecart * this.mob.getLookAngle().z,
                    0.0, -ecart * this.mob.getLookAngle().x));
            niveau.addFreshEntity(tir);
        }
        this.mob.setCooldownTir(this.mob.profil().tirCooldownTicks());
    }

    @Override
    public void stop() {
        this.miseEnJoue = 0;
    }

    /** Le joueur est-il dans le champ de vision (et visible) ? */
    private boolean voit(LivingEntity cible) {
        if (!this.mob.hasLineOfSight(cible)) {
            return false;
        }
        double dx = cible.getX() - this.mob.getX();
        double dz = cible.getZ() - this.mob.getZ();
        double longueur = Math.sqrt(dx * dx + dz * dz);
        if (longueur < 1.0e-4) {
            return true;
        }
        double vx = this.mob.getLookAngle().x;
        double vz = this.mob.getLookAngle().z;
        double norme = Math.sqrt(vx * vx + vz * vz);
        if (norme < 1.0e-4) {
            return true;
        }
        return (dx * vx + dz * vz) / (longueur * norme) >= DEMI_ANGLE_COS;
    }
}
