# Validation de la 1.3.0-alpha.1

Date : 17 octobre 2026. Minecraft 26.3, Fabric Loader 0.19.5, JDK 25, Fabric API 0.162.0+26.3, GeckoLib 5.5.7 et bridge 0.7.1. MCP Python SDK 1.30.0.

Le mod principal est compilé depuis les **195 sources Java** de cette branche, avec les ressources, les six structures de vaisseaux et le manifeste Fabric. Le commit des sources et leur empreinte canonique figurent dans [manifest.json](manifest.json). Le SHA-256 du JAR livré est `e7437044995a10e7e9f9c5d7efc696e27058d4c0b5132ca40cabc937496b9b94`. Aucun initialiseur de fixtures ou classe de test n'est embarqué dans ce JAR.

Les vérifications sur ces mêmes octets comprennent :

| Recette | Résultat |
|---|---|
| Inventaires natifs, reçus, transfert plafonné, composants, codecs et requêtes | 38 assertions passent. |
| Régression dans un vrai serveur Fabric : conservation, rollback, protections, dépôt automatique, navigation et redémarrage | 48 assertions passent. |
| Missions dans le monde : contenu du coffre, quantités, sac, terrain, surplus et persistance | 32 assertions passent. |
| Client MCP stdio réel → Python → RCON → mod | 22 contrôles passent. |
| Tests Python de validation, paquets RCON et erreurs | 11 cas passent. |

La recette effectue deux arrêts puis relancements complets du serveur. Elle vérifie la non-duplication lors des requêtes répétées, le refus d'écrasement d'une mission active, la reprise après coffre plein, la livraison exacte sans compter le stock initial, le rollback via MCP avec restauration et préservation du contenu tiers, l'annulation avec récupération physique du sac, l'épuisement des sources et les drops multiples de cuivre. Les derniers correctifs de `main` concernant la face visible et l'abandon de cible bloquée sont intégrés.

Les lanceurs et fixtures sont publiés dans `voiddynasty/tools/run_work_tests.py`, `run_work_lab.py --missions`, `mission_acceptance.py` et `src/test/java`. Le laboratoire démarre uniquement ses propres processus, avec une écoute locale, un monde neuf et un mod de fixtures séparé. Il nécessite une installation Minecraft autorisée, les dépendances du pack, un JDK 25 et l'acceptation de l'EULA pour ce serveur. Les configurations locales et secrets ne sont pas publiés.

Cette recette valide le comportement serveur et le transport MCP. Elle ne valide pas encore le rendu client, toutes les dimensions, la connexion à l'instance personnelle d'Hermes, une compilation Gradle sur poste vierge ou l'atomicité disque lors d'une coupure brutale. [Capacités et limites détaillées](../../voiddynasty/docs/SERVITOR-POLYVALENT.md).
