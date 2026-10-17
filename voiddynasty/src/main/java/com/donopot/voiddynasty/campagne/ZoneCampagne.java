package com.donopot.voiddynasty.campagne;

import java.util.List;

/**
 * Une zone de campagne, telle que la source la définit. Aucune valeur n'est inventée : tout vient des
 * fonctions `mcfunction` de la source (voir tools/preparer_campagne.py).
 *
 * @param id              identifiant lisible (les « / » du chemin source deviennent des « _ »)
 * @param zone            chemin d'origine dans la campagne de la source
 * @param repliques       les répliques affichées (les `tellraw` de la source)
 * @param titres          les titres en barre d'action (les `title @a actionbar`)
 * @param waypoints       les shulkers-guides posés par la zone
 * @param apparitions     les créatures invoquées, à leurs coordonnées d'origine
 * @param objets          les objets donnés au sol par la source
 * @param appelsDifferes  les `schedule function` de la source (zone appelée + délai en ticks)
 * @param poseCompteur    les compteurs posés : [zone, valeur]
 * @param poseNiveau      les index de niveau posés
 */
public record ZoneCampagne(String id, String zone, List<String> repliques, List<String> titres,
                           List<Waypoint> waypoints, List<Apparition> apparitions,
                           List<Objet> objets, List<AppelDiffere> appelsDifferes,
                           List<String[]> poseCompteur, List<Integer> poseNiveau) {

    /** Une créature invoquée à une position précise. */
    public record Apparition(String mob, String mobSource, double x, double y, double z) {}

    /** Un guide lumineux posé par la source (un shulker invisible et luisant). */
    public record Waypoint(double x, double y, double z, List<String> tags) {}

    /** Un appel différé : la source demande de rejouer une zone après un délai. */
    public record AppelDiffere(String chemin, int ticks) {}

    /** Un objet donné au sol : le nom de la source, et NOTRE objet correspondant. */
    public record Objet(double x, double y, double z, String nom, String objet) {}
}
