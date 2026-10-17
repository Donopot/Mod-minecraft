package com.donopot.voiddynasty.weapon;

/** Effets de statut appliqués aux cibles touchées. */
public enum EffectType {
    /** Ralentissement bref. */
    STAGGER,
    /** Saignement (dégâts sur la durée). */
    BLEED,
    /** Brûlure. */
    BURN,
    /** Panique (fuite). */
    PANIC,
    /** Suppression (ralentissement de tir). */
    SUPPRESSION,
    /** Mise à feu directe (légère). */
    IGNITE,
    /** Brasier intense (lance-flammes). */
    INFERNO,
    /** Fonte d'armure (affaiblissement). */
    ARMOR_MELT,
    /** Perturbation énergétique. */
    ENERGY_DISRUPTION,
    /** Détonation différée (bolts). */
    DELAYED_DETONATION,
    /** Recul violent. */
    KNOCKBACK
}
