# Validation de la 1.3.0-alpha.2

17 octobre 2026. Minecraft 26.3, Fabric Loader 0.19.5, JDK 25, Fabric API 0.162.0+26.3, GeckoLib 5.5.7, bridge 0.7.1 et MCP Python SDK 1.30.0.

Le JAR principal est compilé depuis **198 sources Java**. SHA-256 : `e6ef6a0eb81d0eb797483d0ed18a1b5e005a4b0d99d9c9981e2c4cd0200a8c1d`. Les ressources, six structures de vaisseaux et licences restent incluses ; les classes de fixtures et de tests restent hors du JAR jouable. [manifest.json](manifest.json) indique le commit et l'empreinte canonique des sources.

| Recette sur le JAR livré | Résultat |
|---|---|
| Inventaires natifs, composants, codecs, missions, réservations et coûts de remboursement | 46 assertions passent. |
| Régression serveur : conservation, dépôt automatique, navigation, rollback et reprise | 48 assertions passent. |
| Missions de collecte dans le monde | 32 assertions passent. |
| Nouveaux scénarios d'accès et de construction dans le monde | 43 assertions passent. |
| Protocole MCP stdio réel → Python → RCON → mod | 38 contrôles passent. |
| Validation Python, erreurs et corrélation RCON | 13 cas passent. |

La recette complète effectue trois arrêts et relancements du serveur. Les nouveaux scénarios couvrent un diamant caché derrière la roche, l'accès borné et la conservation du déblai, le refus des fluides/plafonds gravitaires/conteneurs/blocs hors zone, la restauration exacte de la roche excavée, le refus d'un site occupé, le stock de matériaux insuffisant, la plateforme, les requêtes répétées, les remboursements après rollback, l'annulation d'une construction partielle avec dépôt des matériaux inutilisés, puis le remboursement des poses restantes. Un abri de 94 blocs reprend après redémarrage et est vérifié intégralement. Un bloc modifié par le joueur bloque le rollback sans suppression ni remboursement ; après correction de cette modification, le rollback et la récupération du sac conservent les 100 matériaux du stock de test.

Les lanceurs publiés sont `voiddynasty/tools/run_work_tests.py`, `run_work_lab.py --missions` et `python -m unittest discover -s voiddynasty/tools/tests`. Les options `--next-only` et `--build-only` servent au diagnostic ciblé. Les fixtures et clients d'acceptation restent dans `src/test` et `tools/work_next_acceptance.py`. Le laboratoire utilise un monde neuf, une écoute locale et ses propres processus JVM, avec EULA acceptée pour ces essais. Il ne modifie pas les parties du joueur.

Les exemples Hermes/SSH ont été vérifiés pour leur syntaxe JSON/PowerShell. Le rôle MCP est testé avec un vrai client SDK qui vérifie que seuls les six outils `servitor_*` sont présents. Aucun raccordement au serveur Hermes personnel ni test SSH entre les deux machines n'a été effectué. Le rendu client, toutes les dimensions, une reconstruction Gradle sur poste vierge et une coupure brutale pendant les écritures disque ne sont pas couverts.
