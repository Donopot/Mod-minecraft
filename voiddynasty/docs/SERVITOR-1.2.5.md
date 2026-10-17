# Servitor 1.2.5 — il construit son dépôt

Le choix d'une zone de minage ne dépend plus d'un coffre ou d'un sol aménagé près de son coin supérieur. `/work start` enregistre le chantier, puis le servitor prépare son infrastructure avant de commencer à miner. Les règles de conservation et de rollback de la [1.2.4](SERVITOR-1.2.4.md) restent applicables.

## Déroulement

1. Le chantier est accepté. Pour un joueur, le départ est recherché près de sa position ; pour une commande sans entité, près de la zone. Si aucun départ sûr n'est disponible, le chantier reste enregistré en préparation et recherche à nouveau, sans toucher au terrain.
2. Le servitor cherche un emplacement depuis sa position réelle. Il vérifie la protection, les fluides, les collisions, la portée et le chemin. Le coffre et son support restent hors de la sélection de minage et des coffres voisins.
3. Si le sol manque, il pose un support en cobblestone. Il regarde l'emplacement, tient le matériau et effectue les gestes de construction. Chaque pose exige dix ticks à portée et en ligne de vue ; aucun coffre n'est créé par la commande de lancement.
4. Il pose son coffre, l'enregistre parmi les dépôts suivis, reprend son outil et mine. Le sac plein ou la fin du travail déclenche le retour au dépôt.
5. Un coffre plein reste intact. Le servitor prépare lui-même un dépôt supplémentaire, puis y livre uniquement le reste de son sac. Un coffre disparu déclenche aussi une reconstruction, avec conservation des dépôts connus pour le rollback.

Le matériel de chantier est un **kit d'opérateur séparé** : 64 coffres et 64 supports par nouveau chantier, comme la pioche fournie au départ. Il ne provient pas du butin journalisé. Une unité est décomptée seulement après une pose réussie. Le kit restant apparaît dans `/work status`. Cette version ne simule pas encore une chaîne de récolte de bois et de fabrication des coffres ; si le kit est épuisé, le chantier s'arrête en conservant le sac. Les coffres et supports posés restent dans le monde lorsque le journal de minage est annulé ou validé.

La recherche lit au plus 64 sites par tick et effectue au plus une recherche de chemin pendant ce tick de préparation. Le départ examine au plus 129 positions par tentative. Le plan, le temps de construction et les quantités du kit sont sauvegardés. Après un redémarrage propre, `/work resume` reprend explicitement le chantier. Les sauvegardes de format 2 restent lisibles ; le nouveau format est 3.

Un emplacement dangereux ou inaccessible peut suspendre la préparation, mais ne rejette plus la sélection pour absence de dépôt. Le servitor ne force pas un chunk déchargé, ne remplace pas un bloc existant pour poser son coffre et ne traverse pas un mur pour construire. Le journal d'un précédent chantier doit toujours être validé ou annulé avant d'en lancer un autre : cette protection des ressources reste volontaire.

## Navigation

La position visée par le pathfinding garde désormais une marge d'un bloc par rapport à la portée d'interaction : le déplacement natif peut s'arrêter avant le centre exact du dernier nœud. Cela corrige le servitor arrivé près du coffre qui restait trop loin pour y déposer. Le constructeur élimine aussi les emplacements sans chemin utilisable avant de les retenir.

## Validation du 17 octobre 2026

74 assertions sur le JAR livré : 26 contrôles natifs d'inventaire/codecs et 48 contrôles dans un serveur Fabric neuf avec le bridge 0.7.1. Les nouveaux scénarios couvrent :

- lancement sans coffre et sans sol près du coin de sélection, construction du support et du coffre, minage et livraison terminés ;
- coffre plein, second dépôt construit, contenu initial préservé et reste livré exactement une fois ;
- départ initialement impossible, chantier accepté puis apparition du servitor quand un sol devient disponible ;
- sauvegarde/rechargement du plan et du kit, y compris après un véritable arrêt et relancement du serveur ;
- régression des contrôles de ressources, rollback, protection, détour et trajet impossible de la 1.2.4.

Le laboratoire et ses tests sont publiés dans `src/test` et `tools/run_work_*.py`. Le mod de test est séparé du JAR jouable. Cette recette ne constitue pas une validation artistique du client, de toutes les dimensions ou d'une coupure brutale pendant les écritures disque ; les limites de la 1.2.4 sur ces sujets restent valables.
