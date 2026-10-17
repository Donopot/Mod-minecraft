package com.donopot.voiddynasty.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Les deux attaques lourdes du Prince Démon, réglées par la source.
 *
 * La source lui en donne TROIS, pas une :
 *   - frappe 3..5 blocs, cooldown 1 s (assurée par le goal de contact habituel) ;
 *   - charge 6..100 blocs, cooldown 15 s, sous forme d'accélération ;
 *   - smash 0..3 blocs, cooldown 30 s, 20 dégâts sur 8 blocs de rayon.
 *
 * La charge et le smash n'entrent ni dans la plage de tir ni dans celle du contact : ils ont leur
 * propre comportement. La source décrit la charge comme une « accélération » sans en donner la valeur :
 * on applique une accélération franche et brève, sans prétendre reproduire un chiffre qui n'existe pas.
 */
public class AssautDemonGoal extends Goal {

    /** Identifiants de capacité, côté serveur comme côté client. */
    public static final int CHARGE = 2;
    public static final int SMASH = 3;

    private static final int MISE_EN_JOUE_CHARGE = 10;
    private static final int MISE_EN_JOUE_SMASH = 12;
    private static final double RAYON_SMASH = 8.0;

    private final DaemonPrinceEntity demon;
    private int capacite;
    private int miseEnJoue;

    public AssautDemonGoal(DaemonPrinceEntity demon) {
        this.demon = demon;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity cible = this.demon.getTarget();
        if (cible == null || !cible.isAlive()) {
            return false;
        }
        double distance = this.demon.distanceTo(cible);
        if (this.demon.cooldownSmash() == 0 && distance <= 3.0) {
            this.capacite = SMASH;
            return true;
        }
        if (this.demon.cooldownCharge() == 0 && distance >= 6.0 && distance <= 100.0) {
            this.capacite = CHARGE;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.miseEnJoue > 0;
    }

    @Override
    public void start() {
        this.miseEnJoue = this.capacite == SMASH ? MISE_EN_JOUE_SMASH : MISE_EN_JOUE_CHARGE;
        this.demon.declencherCapacite(this.capacite, this.miseEnJoue + 10);
    }

    @Override
    public void tick() {
        LivingEntity cible = this.demon.getTarget();
        if (cible != null) {
            this.demon.getLookControl().setLookAt(cible, 30f, 30f);
        }
        if (--this.miseEnJoue > 0) {
            return;
        }
        if (this.capacite == CHARGE) {
            // « speed boost » de la source : accélération brève et franche, le temps de la ruée.
            this.demon.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 2, false, false));
            this.demon.setCooldownCharge((int) (15 * 20));
        } else {
            frapper();
            this.demon.setCooldownSmash((int) (30 * 20));
        }
    }

    /** Smash : 20 dégâts à tout ce qui vit dans 8 blocs de rayon — valeurs de la source. */
    private void frapper() {
        if (!(this.demon.level() instanceof ServerLevel niveau)) {
            return;
        }
        AABB zone = this.demon.getBoundingBox().inflate(RAYON_SMASH);
        for (Entity e : niveau.getEntities(this.demon, zone, x -> x instanceof LivingEntity && x.isAlive())) {
            if (e instanceof LivingEntity vivant) {
                vivant.hurtServer(niveau, this.demon.damageSources().mobAttack(this.demon), 20.0f);
            }
        }
    }
}
