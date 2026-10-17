package com.donopot.voiddynasty.campagne;

import java.util.List;

/**
 * Une règle de progression, transcrite d'une ligne de `leveldetect.mcfunction`.
 *
 * La source teste une position : soit la présence d'un joueur (`joueur_proche`), soit l'absence
 * d'ennemis (`sans_ennemi`) — ses ennemis étaient des zombies, d'où `type_source`. Le moteur du mod
 * traduit ce test en « aucun ennemi de la campagne », et le déclare.
 *
 * @param position      le point testé, dans les coordonnées de la source
 * @param condition     « joueur_proche » ou « sans_ennemi », avec son rayon
 * @param rayon         rayon du test de position
 * @param typeSource    pour « sans_ennemi » : le type d'entité que la source testait (zombie)
 * @param compteurZone  zone dont le compteur conditionne la règle
 * @param compteurValeur  valeur attendue du compteur
 * @param appelle       zones déclenchées
 * @param poseCompteur  compteurs posés : [zone, valeur]
 * @param poseNiveau    index de niveau posés
 * @param tue           tags d'entités supprimées
 */
public record RegleCampagne(double[] position, String condition, double rayon, String typeSource,
                            String compteurZone, Integer compteurValeur, List<String> appelle,
                            List<String[]> poseCompteur, List<Integer> poseNiveau, List<String> tue) {}
