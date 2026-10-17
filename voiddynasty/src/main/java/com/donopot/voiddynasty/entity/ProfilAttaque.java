package com.donopot.voiddynasty.entity;

/**
 * Profil d'attaque d'un ennemi du Chaos, transcrit de la source (datapacks Astartes).
 *
 * La source ne dit pas « attaque au contact » en bloc : elle donne, pour CHAQUE mob, une plage de
 * détection pour le tir et une pour le contact, chacune avec son propre cooldown, et n'attaque que si
 * le joueur est dans la plage ET dans le champ de vision. Ces valeurs vivent ici, ensemble et lisibles,
 * plutôt que dispersées dans les classes.
 *
 * Une plage vide (max = 0) signifie que la source ne donne pas ce type d'attaque : le mob ne l'utilise
 * pas. Aucun comportement n'est ajouté de notre côté.
 *
 * @param tirMin          distance minimale de tir, en blocs
 * @param tirMax          distance maximale de tir, en blocs (0 = pas de tir)
 * @param tirCooldownTicks délai entre deux tirs, en ticks
 * @param cacMin          distance minimale de contact, en blocs
 * @param cacMax          distance maximale de contact, en blocs (0 = pas de contact)
 * @param cacCooldownTicks délai entre deux attaques au contact, en ticks
 */
public record ProfilAttaque(double tirMin, double tirMax, int tirCooldownTicks,
                            double cacMin, double cacMax, int cacCooldownTicks) {

    public static final ProfilAttaque AUCUN = new ProfilAttaque(0, 0, 0, 0, 0, 0);

    public boolean aTir() {
        return this.tirMax > 0;
    }

    public boolean aCac() {
        return this.cacMax > 0;
    }

    /** Le joueur est-il dans la plage de tir ET dans le champ de vision ? */
    public boolean peutTirer(double distance) {
        return aTir() && distance >= this.tirMin && distance <= this.tirMax;
    }

    /** Idem pour le contact. */
    public boolean peutFrapper(double distance) {
        return aCac() && distance >= this.cacMin && distance <= this.cacMax;
    }
}
