# Servitor 1.3.0-alpha.3 — zones vides et repos

Correction du servitor qui court sur place lorsqu'il ne reste rien à miner. Les [accès et constructions de l'alpha 2](SERVITOR-POLYVALENT-ALPHA2.md) restent disponibles.

Le modèle ne fournissait qu'un clip `walk`, utilisé aussi comme animation d'attente. Le servitor 1 dispose désormais d'une pose `idle` distincte, avec les bras et les jambes immobiles ; le clip de marche est conservé pour les déplacements. Aucun autre modèle n'est remplacé.

Le chantier cherche maintenant un bloc exploitable **avant de préparer un dépôt ou d'attendre un départ accessible**. Le parcours reste limité à 64 positions par tick. Une zone vide ou contenant uniquement des blocs interdits se termine sans construction inutile. Une cible retirée entre-temps est oubliée, puis le reste de la sélection est vérifié.

La fin de chantier efface les états de cible et de construction en attente et retire le servitor. Une référence transitoire couvre aussi le délai entre l'ajout de l'entité et sa disponibilité dans la recherche par UUID : une fin immédiate ne peut plus oublier un servitor nouvellement créé.

Une sélection native réellement vide, sans dépôt, observation, sac ou reçu, libère automatiquement le chantier avec le message `Zone vide: aucun bloc a miner; chantier termine.` Les blocs protégés et conteneurs rencontrés restent intacts ; leurs observations restent consultables avec `/work loot`.

La présence d'objets dans le sac impose toujours un retour au dépôt avant la fin. Les composants des objets et les reçus de rollback sont conservés. Une mission de collecte sans ressource garde son résultat `INCOMPLETE` et son identité ; elle ne devient pas une réussite artificielle. Le format de sauvegarde reste 5.

## Mise à jour locale

Fermer Minecraft, remplacer l'ancien JAR principal par `voiddynasty-1.3.0-alpha.3.jar` et garder un seul JAR principal. Le bridge reste en 0.7.1 ; Fabric API et GeckoLib sont inchangés. Pour un ancien servitor bloqué déjà présent, `/work stop` retire le servitor tout en conservant le sac et le journal. Consulter `/work status` avant de récupérer le sac ou d'abandonner les reçus avec `/work commit`.

[Pack alpha 3](../../client-local/voiddynasty-1.3.0-alpha.3) · [Validation](../../client-local/voiddynasty-1.3.0-alpha.3/VALIDATION.md). Les nouveaux scénarios testent zone vide, kit épuisé, départ sans sol, sélection dépassant un tick de recherche, disparition de la dernière cible, blocs interdits, sac encore rempli et sélection du clip de repos. La recette serveur et le contrôle des poses JSON ne remplacent pas une vérification visuelle dans le client du joueur.
