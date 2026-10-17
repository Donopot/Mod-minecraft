package com.donopot.voiddynasty.weapon;

/** Mode de tir d'une arme. */
public enum FireMode {
    /** Un tir par clic. */
    SEMI,
    /** Rafale de 3 coups par clic (bolter). */
    BURST_3,
    /** Tir automatique tant que le bouton est maintenu (bolter lourd). */
    AUTOMATIC,
    /** Jet continu (lance-flammes). */
    CONTINUOUS,
    /** Mêlée (pas de projectile). */
    MELEE
}
