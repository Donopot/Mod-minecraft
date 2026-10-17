# Validation du correctif 1.3.0-alpha.3

17 octobre 2026. Minecraft 26.3, Fabric Loader 0.19.5, JDK 25, Fabric API 0.162.0+26.3, GeckoLib 5.5.7, bridge 0.7.1 et MCP Python SDK 1.30.0.

Le mod principal est compilé depuis les **198 sources Java**, avec les ressources, les six structures de vaisseaux et les licences. SHA-256 : `2da2b688b3e88f1235b398b927411b7fdfe87078b967b0f716429fd736fe8cfb`. Le commit et l'empreinte canonique des sources sont dans [manifest.json](manifest.json). Aucune classe de test n'est embarquée dans le JAR jouable.

| Recette sur ces mêmes octets | Résultat |
|---|---|
| Inventaires natifs, composants et persistance | 46 assertions passent. |
| Zones vides/épuisées et sélection du repos | 44 assertions passent. |
| Régression serveur de conservation, dépôt et navigation | 48 assertions passent. |
| Missions de collecte dans le monde | 32 assertions passent. |
| Accès et construction dans le monde | 43 assertions passent. |
| Client MCP stdio réel → Python → RCON → mod | 38 contrôles passent. |
| Tests Python de validation, erreurs et RCON | 13 cas passent. |

Soit 251 contrôles natifs/MCP et 13 cas Python, avec trois arrêts et relancements complets du serveur. Le nouveau harnais échoue sur l'ancien JAR alpha 2, puis passe sur l'alpha 3.

Les nouveaux cas couvrent une sélection vide, un kit épuisé, un départ sans sol accessible, 512 positions d'air avec recherche bornée, la disparition de la dernière cible, une zone contenant uniquement bedrock et coffre, puis une fin de zone avec trois objets nommés et leur reçu à conserver. Le harnais vérifie aussi l'absence de servitor vivant oublié, la libération d'un chantier réellement vide, le choix du clip `idle`, la présence du clip `walk` et les rotations immobiles des membres au repos.

Le lanceur `voiddynasty/tools/run_work_lab.py --empty-only` rejoue le correctif seul ; `--missions` rejoue l'ensemble, dont les collectes, les remboursements et l'abri construit après redémarrage. Les fixtures restent dans `src/test/java/.../WorkEmptyServerTest.java`, séparées du mod jouable. Le laboratoire utilise uniquement un monde neuf, des ports locaux et ses propres processus.

Le contrôle du clip et du JSON ne constitue pas une validation visuelle dans le client du joueur. Les limites de l'alpha 2 concernant toutes les dimensions, les accès verticaux et l'atomicité disque lors d'une coupure brutale restent applicables. Aucune installation ni modification des mondes du joueur n'est effectuée par cette livraison.
