package com.donopot.voiddynasty;

/**
 * Fenêtre d'affichage de la liste des vaisseaux du pad.
 * Classe pure (aucune dépendance Minecraft) afin d'être testable isolément.
 *
 * Garantie : si la liste n'est pas vide, l'élément sélectionné est toujours
 * dans la fenêtre affichée, et la fenêtre ne dépasse jamais les bornes.
 */
public final class FleetListWindow {

    private FleetListWindow() {}

    /** Index du premier élément à afficher. */
    public static int debut(int selection, int taille, int lignes) {
        if (lignes <= 0 || taille <= lignes) return 0;
        int d = selection - lignes / 2;
        if (d < 0) d = 0;
        if (d > taille - lignes) d = taille - lignes;
        return d;
    }

    /** Nombre de lignes réellement affichées. */
    public static int compte(int taille, int lignes) {
        if (taille < 0) taille = 0;
        return Math.min(taille, lignes);
    }

    /** Indicateur lisible : "3-7 / 10" ou "aucun". */
    public static String indicateur(int selection, int taille, int lignes) {
        int c = compte(taille, lignes);
        if (c == 0) return "aucun";
        int d = debut(selection, taille, lignes);
        return (d + 1) + "-" + (d + c) + " / " + taille;
    }
}
