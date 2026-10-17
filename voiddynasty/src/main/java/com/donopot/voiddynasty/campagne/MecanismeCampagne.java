package com.donopot.voiddynasty.campagne;

import java.util.List;

/**
 * Un mécanisme que le joueur frappe : une porte verrouillée ou un panneau du réacteur. Transcrit de
 * `doors/dooropen.mcfunction`.
 *
 * La source représente chacun d'eux par un shulker invisible placé dans le monde ; le mod en pose un
 * aussi (voir CampagneService). La position du marqueur est déduite du centre de la région que la porte
 * ouvre — c'est une DÉDUCTION, pas un portage : la source, elle, a ses shulkers déjà posés dans le
 * monde. C'est déclaré ici et dans la roadmap.
 *
 * @param tag              le tag de la source (door1, airlock1, reactor1…)
 * @param conditionZone    zone dont le compteur conditionne l'ouverture
 * @param conditionValeur  valeur à partir de laquelle elle s'ouvre
 * @param regions          régions à vider, au format [x1 y1 z1 x2 y2 z2]
 * @param sons             sons joués à l'ouverture
 * @param appelle          zones déclenchées à l'ouverture
 * @param tue              tags d'entités supprimées
 * @param compteRetire     retrait de compteur : [zone, objectif, quantité]
 */
public record MecanismeCampagne(String tag, String conditionZone, Integer conditionValeur,
                                List<int[]> regions, List<String> sons, List<String> appelle,
                                List<String> tue, String[] compteRetire) {}
