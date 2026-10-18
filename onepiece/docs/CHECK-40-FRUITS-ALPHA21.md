# Audit des 40 fruits — alpha 21, 18 octobre 2026

**Catalogue et mécaniques contrôlés ; validation visuelle complète encore ouverte.** Le JAR vérifié est `1.0.1-alpha.21`, SHA256 `93687e13497210097f3ba00932bb9ad38b96ad947ad5e9de628ee09b3cd34505`. Aucun code de jeu ni asset n'est changé pendant cet audit.

## Résultats

- Le catalogue du code, la matrice et le registre effectivement chargé correspondent : **40 fruits, 213 techniques uniques**.
- **3874 contrôles natifs distincts couverts sur le même JAR**, par recettes complémentaires. Les 59 contrôles communs sont comptés une seule fois. Le tableau ci-dessous désigne la suite exécutée pour chaque fruit ; le total n'est pas une nouvelle validation visuelle de toutes les techniques.
- Le module de diagnostic séparé trouve une animation livrée pour chacune des 213 techniques, à mains nues et avec une épée, soit 426 sélections contrôlées. Il identifie 12 Zoans pris en charge par les cycles de forme complète, couverts par leurs suites natives, et les 213 décisions terrain : **157 destructrices, 10 transformations, 46 sans mutation**. Les blocs modifiés restent définitifs. [Décisions exportées du registre chargé](CHECK-40-TERRAIN-ALPHA21.csv).
- Les audits des huit lots, Magu et Goro passent : 408 pistes examinées pour leurs mouvements, durées et distinction dans leur groupe. Les audits des lots et Goro contrôlent aussi la récupération neutre. Cela ne signifie pas que toutes les pistes de groupes différents ont des gestes uniques.
- Le paquet contient les 53 profils reliés aux ressources de référence. L'audit des 1054 JSON et des 95 couples de ressources historiques ne trouve aucune erreur bloquante. Quatre avertissements UV hérités concernent `abyss.geo.json` ; ils ne prouvent pas à eux seuls un défaut visible d'une technique actuelle.

## Défauts et réserves

1. **Phoenix : rendu des ailes à corriger.** Une reprise actuelle à deux clients confirme des ailes brun-jaune pendant le vol de la forme complète, au lieu du rendu bleu de feu attendu. Six captures acteur et six observateur sont inspectées. Les contrôles de formes, dimensions, vol et les quatre évaluations PAL par client passent ; ils ne prouvent pas la bonne couleur. Examiner les couches `phoenixWings`/`phoenixFlames` et le passage des couleurs dans `PlayerTransformLayer` avant de changer arbitrairement une texture.
2. **Deux anciennes déclarations d'animation sans fichier : `swim` et `sword`.** Le registre les filtre. Aucune des 426 sélections de base testées sur les 40 fruits ne les demande. Le repli générique vers `sword` peut cependant produire un nom vide dans un autre chemin ; ce socle doit être nettoyé ou complété avec une animation vérifiée.
3. **Soru : détails des compagnons encore à revoir visuellement.** Les tests mécaniques passent ; aucune nouvelle inspection rapprochée de leurs visages n'est revendiquée ici.
4. Les formes complètes absentes de ver15 restent des adaptations. Le rapprochement de la caméra vanilla dans les cratères et l'acceptation artistique des 40 fruits restent ouverts.

## Échecs conservés et limites de la recette

La passe accélérée à 80 ticks/s conserve trois échecs : dégâts et attribution des météores Magu, quatrième cible de la chaîne Goro. Les deux suites sont reprises séparément dans des mondes neufs à **20 TPS** : Magu **74/74**, Goro **142/142**. Ces reprises remplacent uniquement ces deux suites dans la couverture retenue. Elles ne prouvent pas la cause exacte de l'écart accéléré, ni les performances en situation réelle.

Une première recette B08 s'est arrêtée faute de catalogue de test local, après 183 résultats natifs réussis. La recette rétablie est relancée entièrement et passe 183/183 ; elle seule est retenue pour B08. Les premières traces et rapports ne sont pas effacés. Les contrôles historiques et de permanence incluent redémarrage, loot et conservation des modifications joueur.

La recette B05/B06/B07 réussit respectivement 458/535/502 contrôles, puis s'arrête après les historiques parce que le fichier de référence loot `CORE-030-rapport-tests.md` manque au laboratoire. Ses trois suites de fruits sont retenues après vérification de tous leurs résultats, de l'empreinte du JAR et de l'arrêt du processus ; son rapport global reste en erreur. Le loot, les historiques et la permanence sont repris dans une recette distincte après rétablissement du document. Aucun résultat manquant n'est remplacé par un simple marqueur de fin.

Les mécaniques des lots sont testées à 80 ticks/s, les reprises Magu/Goro et historiques à 20. Les laboratoires utilisent des faux joueurs pour de nombreux contrôles, pas une campagne jouée. Le diagnostic de catalogue ne lance aucune attaque. Les vues Mera proviennent de la recette alpha 21 précédente, celles du Phoenix sont reprises pendant cet audit ; les 38 autres fruits ne reçoivent pas de nouvelle session graphique exhaustive ici. Tous les processus créés pour cet audit sont arrêtés. Aucun monde ou mod habituel n'est installé ou modifié.

## Suites retenues

| Suite | Contrôles réussis retenus |
|---|---:|
| checks | 59 |
| mera | 92 |
| magu | 74 |
| goro | 142 |
| batch01 | 345 |
| batch02 | 363 |
| batch03 | 418 |
| batch04 | 365 |
| batch05 | 458 |
| batch06 | 535 |
| batch07 | 502 |
| batch08 | 183 |
| historical | 307 |
| permanent | 31 |

## Couverture par fruit

Chaque ligne a un catalogue et des décisions terrain vérifiés à l’exécution, avec sa suite mécanique réussie. « PASS » ne désigne pas une acceptation artistique.

| Fruit | Techniques | Suite native | Réserve ciblée |
|---|---:|---|---|
| mera_mera | 7 | Mera PASS | Rendu non rejoué exhaustivement ici |
| magu_magu | 5 | Magu PASS | Reprise isolée 20 TPS |
| goro_goro | 7 | Goro / Amaru PASS | Reprise isolée 20 TPS |
| hie_hie | 5 | B01 PASS | Rendu non rejoué exhaustivement ici |
| pika_pika | 8 | B01 PASS | Rendu non rejoué exhaustivement ici |
| yami_yami | 4 | B01 PASS | Rendu non rejoué exhaustivement ici |
| suna_suna | 5 | B01 PASS | Rendu non rejoué exhaustivement ici |
| moku_moku | 4 | B01 PASS | Rendu non rejoué exhaustivement ici |
| mori_mori | 4 | B03 PASS | Rendu non rejoué exhaustivement ici |
| gomu_gomu | 4 | B05 PASS | Rendu non rejoué exhaustivement ici |
| inu_inu_wolf | 8 | B06 PASS | Rendu non rejoué exhaustivement ici |
| ushi_ushi_giraffe | 5 | B06 PASS | Rendu non rejoué exhaustivement ici |
| neko_neko_leopard | 7 | B06 PASS | Rendu non rejoué exhaustivement ici |
| batto_batto | 6 | B06 PASS | Rendu non rejoué exhaustivement ici |
| hito_hito_daibutsu | 5 | B07 PASS | Rendu non rejoué exhaustivement ici |
| ryu_ryu_pachycephalosaurus | 5 | B08 PASS | Rendu non rejoué exhaustivement ici |
| inu_inu_okuchi_no_makami | 7 | B07 PASS | Rendu non rejoué exhaustivement ici |
| tori_tori_phoenix | 6 | B07 PASS | Ailes de la forme complète |
| uo_uo_seiryu | 8 | B07 PASS | Rendu non rejoué exhaustivement ici |
| ryu_ryu_pteranodon | 3 | B07 PASS | Rendu non rejoué exhaustivement ici |
| ryu_ryu_brachiosaurus | 5 | B08 PASS | Rendu non rejoué exhaustivement ici |
| neko_neko_sabertiger | 5 | B06 PASS | Rendu non rejoué exhaustivement ici |
| gura_gura | 6 | B02 PASS | Rendu non rejoué exhaustivement ici |
| horo_horo | 5 | B04 PASS | Rendu non rejoué exhaustivement ici |
| nikyu_nikyu | 5 | B02 PASS | Rendu non rejoué exhaustivement ici |
| zushi_zushi | 6 | B02 PASS | Rendu non rejoué exhaustivement ici |
| bisu_bisu | 4 | B04 PASS | Rendu non rejoué exhaustivement ici |
| mochi_mochi | 6 | B03 PASS | Rendu non rejoué exhaustivement ici |
| uta_uta | 7 | B05 PASS | Rendu non rejoué exhaustivement ici |
| imu | 3 | B05 PASS | Rendu non rejoué exhaustivement ici |
| kira_kira | 5 | B03 PASS | Rendu non rejoué exhaustivement ici |
| supa_supa | 4 | B03 PASS | Rendu non rejoué exhaustivement ici |
| riki_riki | 5 | B03 PASS | Rendu non rejoué exhaustivement ici |
| mero_mero | 4 | B04 PASS | Rendu non rejoué exhaustivement ici |
| kiro_kiro | 4 | B04 PASS | Rendu non rejoué exhaustivement ici |
| bomu_bomu | 5 | B02 PASS | Rendu non rejoué exhaustivement ici |
| ope_ope | 4 | B02 PASS | Rendu non rejoué exhaustivement ici |
| aro_aro | 4 | B04 PASS | Rendu non rejoué exhaustivement ici |
| kage_kage | 6 | B05 PASS | Rendu non rejoué exhaustivement ici |
| soru_soru | 7 | B05 PASS | Détails des compagnons |

## Rejouer le contrôle de structure

`python tools/audit_catalogue_40.py --jar chemin/vers/opworldlike.jar --output rapport.json`

Le test séparé `src/test/java/net/mcreator/minepiece/audit/CatalogueAuditMod.java` exporte le registre chargé dans un serveur jetable. Son résultat peut être fourni avec `--runtime-report`. Ce module de test n’est pas inclus dans le JAR de jeu. Les bibliothèques, assets privés et traces des laboratoires restent locaux.
