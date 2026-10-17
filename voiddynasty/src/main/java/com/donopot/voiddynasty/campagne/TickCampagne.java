package com.donopot.voiddynasty.campagne;

import java.util.List;

/**
 * Le cerveau PAR TICK, transcrit de `tick.mcfunction`. Il porte trois familles de mécanismes que
 * `leveldetect` n'a pas, plus la Power Armour, la barre du réacteur et l'écran de fin.
 *
 * @param reglesSansPosition  règles qui ne dépendent que d'un compteur, sans position
 * @param reglesEntite        règles déclenchées en approchant une entité-guide (un waypoint)
 * @param reinitialisation    un joueur au point de spawn de la carte relance la campagne
 * @param powerArmour         la tenue qui double la vie maximale du joueur
 * @param reacteur            la barre du cœur du réacteur et ce qu'elle commande
 */
public record TickCampagne(List<RegleGlobale> reglesSansPosition, List<RegleEntite> reglesEntite,
                           Reinitialisation reinitialisation, PowerArmour powerArmour, Reacteur reacteur) {

    /** Une règle sans position : seul le compteur décide. */
    public record RegleGlobale(String compteurZone, int compteurValeur, String appelle) {}

    /** Une règle déclenchée par l'approche d'une entité-guide. */
    public record RegleEntite(String tag, double rayon, String[] poseCompteur) {}

    /** Le point de spawn de la carte : y arriver relance la campagne. */
    public record Reinitialisation(double[] position, double rayon) {}

    /** La Power Armour de la source : la source regarde un casque et pose la vie maximale. */
    public record PowerArmour(int slot, String objetSource, double maxHealth) {}

    /** La barre du cœur du réacteur, et la fin qu'elle déclenche. */
    public record Reacteur(String barre, String compteur, String objectif, String nom, int max, int depart,
                           int seuil, String zone, double[] teleport, String titre, List<String> sons,
                           boolean aveuglement, String[] remetCompteur) {}
}
