# Validation — navigation alpha 4

Candidat : `voiddynasty-1.3.0-alpha.4.jar`.

SHA-256 : `3ebd9a2eafc66b75deb2f6eccf9a43d8baafa6629494df4b86816e66f1fc9a7d`.

Sources : commit `3fff5a8417a6a14ee21330de4ef81ebe78928290`, arbre canonique `aa72e9dd28bcd8a68687b7e3bc6b7e168ec641c249096ca2ef2996c5e7a70342` (méthode dans `manifest.json`).

## Recette

**Résultat : PASS sur ce JAR exact.** Compilation, 46 assertions de conservation, recette serveur complète avec quatre redémarrages propres, 38 contrôles MCP et 13 cas Python. La navigation ajoute 102 assertions dans la recette complète, dont la comparaison du terrain entier de chaque fixture après rollback. Les quatre mods de ce paquet étaient chargés, avec le mod de test séparé. Tous les processus enfants du laboratoire ont été arrêtés.

Compilation des 199 sources Java principales avec JDK 25 et les dépendances Minecraft 26.3/Fabric/GeckoLib ; ressources complètes et six structures conservées. Les classes de test sont compilées dans un mod séparé pour le laboratoire et ne figurent pas dans le JAR jouable.

| Contrôle | Couverture |
|---|---|
| `run_work_tests.py` | 46 assertions natives : inventaires, composants, reçus, transferts, rollback et codecs. |
| `run_work_lab.py --missions` | Monde neuf sur loopback : zones vides, dépôts, navigation historique, collecte, construction, MCP et redémarrages propres. |
| `WorkNavigationServerTest` | Montée sur escalier existant, excavation montante et descendante, retour des ressources et déblais, état complet du terrain après rollback. |
| Navigation interrompue | Retrait d'un sol prévu, pause avant poursuite, reprise après réparation et reprise d'une excavation partielle après redémarrage disque. |
| Refus | Zone d'autorisation trop petite, palier sans sol, magma, conteneur, bedrock, eau, lave et plafond gravitaire détecté. |
| Blocage | Oscillation entre des cases déjà visitées, progression réelle et détour continu vers de nouvelles cases. |
| Commande solo | Les missions de navigation sont créées par `/work mission gather`, via le vrai dispatcher et les validations du serveur. |
| Python | 13 cas unitaires du transport, de la validation des missions et des remboursements. |

Le laboratoire accepte explicitement l'EULA pour ses seuls mondes jetables et arrête uniquement ses processus enfants. Aucun changement des mods, mondes ou processus de jeu du joueur.

## Limites

Pas de validation visuelle en client, d'essai exhaustif des terrains naturels, de toutes les dimensions ou des protections tierces. Aucun raccordement au serveur Hermes réel. La recette couvre les arrêts propres ; l'atomicité entre terrain, coffres et SavedData lors d'une coupure brutale reste à développer. La recherche est bornée et peut refuser un accès qu'une recherche plus longue trouverait. Aucun pont ou ajout de sol automatique.

La compilation Gradle sur une machine vierge n'a pas été vérifiée par cette recette locale. Le bridge 0.7.1 et les deux dépendances sont inchangés par rapport au pack alpha 3.
