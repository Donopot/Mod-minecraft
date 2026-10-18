# Servitor — navigation 1.3.0-alpha.4

Cette alpha ajoute des accès miniers en paliers, améliore le choix des positions de travail pour la largeur du servitor et détecte les allers-retours sans progression. Elle conserve les inventaires exacts, le journal de rollback, les missions et les plans de l'[alpha 2](SERVITOR-POLYVALENT-ALPHA2.md), ainsi que la gestion des zones vides de l'[alpha 3](SERVITOR-1.3.0-alpha.3.md).

## Déplacements et excavation

Les déplacements utilisent toujours la navigation, les collisions, les sauts et la gravité de Minecraft. Le servitor n'est pas téléporté. Les positions de travail utilisent maintenant le centre des nœuds correspondant à sa largeur réelle de 1,09 bloc.

Le mode `surface`, utilisé par défaut, rejoint les ressources par les chemins existants, y compris les escaliers praticables. Il n'autorise aucune excavation de roche supplémentaire. Le mode explicite `excavate` essaie d'abord le chemin natif, puis le passage horizontal existant, puis une recherche de paliers. Chaque palier occupe 3 × 3 blocs ; le suivant est à trois blocs horizontalement et au maximum un bloc plus haut ou plus bas. Le plafond au-dessus du palier inférieur est dégagé pour permettre de remonter. Un sol plein doit déjà exister sous chaque palier ; aucun pont, échafaudage ou bloc d'escalier n'est créé.

La recherche reste bornée à 512 positions, un rayon de 24 blocs en X/Z et huit blocs de différence de hauteur depuis son départ. Elle peut échouer même si un itinéraire plus long existe. Les paliers déjà parcourus gardent leur sol. Les sols des paliers restants sont revérifiés pendant le travail : si le joueur en retire un, la mission se met en pause. Le budget de 256 blocs d'accès par mission est inchangé et persiste après redémarrage. Le coût peut donc limiter la profondeur réellement accessible.

Chaque casse exige la portée réelle, la visibilité, le bon outil et une place pour les drops. Elle produit le même reçu que le minage normal et use l'outil. Les déblais sont conservés et rapportés. Seuls les blocs autorisés dans la zone sont excavés : pas de conteneurs, de blocs indestructibles, de fluides proches ou de plafond gravitaire détecté. Les paliers exigent également un sol sans conteneur, fluide ou bloc gravitaire. Les chunks ne sont pas forcés à charger.

La surveillance de blocage ne considère plus une oscillation entre les mêmes cases comme un nouveau déplacement. Le travail réel et la découverte d'une nouvelle case réinitialisent son délai. Après 400 ticks sans progression, `NAVIGATION_STALLED` met le chantier en pause avec le sac et le journal conservés. Une reprise explicite recalcule le chemin. Les motifs `ACCESS_NO_SAFE_ROUTE`, `ACCESS_TERRAIN_CHANGED`, `ACCESS_OUT_OF_REACH`, `ACCESS_PATH_CHANGED` et `ACCESS_BUDGET_EXHAUSTED` restent disponibles dans l'état de mission.

## Utilisation en solo

Avec les commandes autorisées et un coffre simple ou tonneau de destination déjà posé :

```text
/work mission gather <ressource> <quantite> <coin1 x y z> <coin2 x y z> <coffre x y z> <depart x y z> [surface|excavate]
```

Exemple de la descente vérifiée dans le laboratoire :

```text
/work mission gather minecraft:diamond 1 12 59 5 32 68 15 8 64 20 10 64 10 excavate
/work mission status current
/work status
```

Les coordonnées de cet exemple appartiennent au monde jetable du test : choisir les coordonnées de son propre terrain. Le coffre et son support doivent être hors de la zone. Inclure le volume nécessaire aux passages et à leur plafond, pas seulement les blocs de minerai. Cette commande crée une nouvelle demande identifiée ; elle ne modifie pas un chantier déjà attaché. Les contrôles et le catalogue des ressources sont les mêmes que pour MCP.

Pour une mission attachée, lire son UUID avec `/work mission status current`, puis utiliser `/work mission control <UUID> pause|resume|cancel|deposit|rollback|close`. `/work stop` et `/work resume` restent disponibles. Une annulation conserve le sac ; `deposit` le récupère. Le rollback nécessite les drops exacts encore présents dans le sac ou les dépôts suivis. Ne fermer le chantier qu'après récupération et décision sur son historique.

Le format de sauvegarde reste 5. Les chemins, le palier de départ et l'historique de déplacement sont transitoires ; une reprise après arrêt propre les recalcule à partir de l'état persistant. La garantie d'atomicité lors d'une coupure brutale pendant les écritures disque reste un chantier distinct.

## Validation

`tools/run_work_lab.py --navigation-only` utilise un monde neuf sur loopback. Il vérifie les escaliers existants, l'excavation montante et descendante, les livraisons et déblais, les rollback, le refus de dépasser une petite zone, les sols retirés, les dangers détectés, les oscillations et une reprise après redémarrage du serveur. Les missions de cette recette passent par la nouvelle commande solo réelle. Le mode complet `--missions` conserve les régressions précédentes et les appels MCP.

Cette recette serveur ne remplace pas une validation visuelle sur client ni un essai sur toutes les géométries naturelles, dimensions et protections tierces. Le pont Hermes distant reste à configurer. Aucun nouveau monde ni mod n'est installé automatiquement dans l'instance du joueur.
