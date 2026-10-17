package com.donopot.voiddynasty.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Prince Démon — le boss final de la phase E.
 *
 * 1000 PV (`max_health base 1000` dans la source). La source lui donne TROIS attaques, chacune avec sa
 * portée et son cooldown : frappe 3..5 blocs (10 dégâts, cooldown 1 s), charge 6..100 blocs
 * (cooldown 15 s), smash 0..3 blocs (20 dégâts sur 8 blocs de rayon, cooldown 30 s).
 *
 * La frappe passe par le comportement de contact habituel ; la charge et le smash ont le leur
 * ({@link AssautDemonGoal}).
 *
 * Barre de boss : la source le compte parmi les boss.
 */
public class DaemonPrinceEntity extends ChaosBossEntity {

    private int cooldownCharge = 0;
    private int cooldownSmash = 0;

    public DaemonPrinceEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, "entity.voiddynasty.daemon_prince", BossEvent.BossBarColor.PURPLE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(1000.0, 10.0, 0.25);
    }

    /** Frappe 3..5 blocs, cooldown 1 s. Aucun tir : ses deux autres attaques ne sont pas des tirs. */
    @Override
    public ProfilAttaque profil() {
        return new ProfilAttaque(0.0, 0.0, 0, 3.0, 5.0, 20);
    }

    public int cooldownCharge() {
        return this.cooldownCharge;
    }

    public void setCooldownCharge(int ticks) {
        this.cooldownCharge = Math.max(0, ticks);
    }

    public int cooldownSmash() {
        return this.cooldownSmash;
    }

    public void setCooldownSmash(int ticks) {
        this.cooldownSmash = Math.max(0, ticks);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AssautDemonGoal(this));   // avant les attaques de base
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.cooldownCharge > 0) {
            this.cooldownCharge--;
        }
        if (this.cooldownSmash > 0) {
            this.cooldownSmash--;
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

    /** Les deux capacités lourdes : la source leur donne un clip chacune. */
    @Override
    protected String clipSupplementaire(int capacite) {
        return switch (capacite) {
            case AssautDemonGoal.CHARGE -> "charge";
            case AssautDemonGoal.SMASH -> "smash";
            default -> null;
        };
    }
}
