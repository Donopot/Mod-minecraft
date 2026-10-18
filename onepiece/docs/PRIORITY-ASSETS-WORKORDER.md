# Assets prioritaires — alpha 25

Ordre confirmé par l'utilisateur : cinq transformations, puis Gomu/Gura/Ope/Mochi/Nikyu, puis essais prolongés. Branche `codex/onepiece-priority-assets`. Le Haki reste hors de cette étape.

| Étape | Contenu | État |
|---|---|---|
| 1 | Amaru, Phoenix complet, Bouddha, Loup complet, Léopard complet | Intégrés ; 332 captures du lot et 24 de reprise Amaru inspectées |
| 2 | Gomu, Gura, Ope, Mochi, Nikyu | Intégrés ; captures groupées et reprises Mochi/Ope inspectées |
| 3 | Cycles de transformations, effets, réinitialisation et caches | Recette de dix minutes passée sur deux clients ; contrôles natifs passés |

## Contraintes conservées

- Modèle, squelette et pilotage du dragon alpha22 conservés.
- Couleur brun-jaune de la queue du Phoenix correcte ; pas de correction de couleur inventée.
- Destructions et transformations du terrain définitives.
- Modèles privés restaurés depuis les références fournies ; aucun code Forge installé dans Fabric.
- Tests visuels groupés après implémentation des cinq fruits de chaque lot.
- Mondes neufs isolés, sans modification des mods ou sauvegardes habituels.

## Premier lot

Les 17 fichiers de ressources (cinq géométries, cinq bibliothèques d'animation et sept textures) sont restaurés par `tools/import_priority_assets.py`. Le manifeste enregistre leurs empreintes. Le lecteur conserve les os, les pivots et les UV de boîte ; il échantillonne les canaux numériques avec leurs interpolations. Il rejette les expressions ou interpolations non prises en charge. Les 31 pistes d'origine sont échantillonnées dans le test séparé `RigAnimationTest`.

Les données d'animation sont capturées par avatar pour la file de rendu différée. Les actions reçues par le réseau animent aussi les joueurs distants ; les caches sont nettoyés au changement de monde et les avatars absents sont purgés périodiquement. Les dimensions, coûts et durées des pouvoirs sont conservés.

## Second lot

- **Gomu** : bras ver15 et portées déjà calibrées conservés ; propriétaire synchronisé pour prendre la texture du bon joueur à distance. Les revêtements des Gears 4/5 gardent priorité. Le collier alpha `cloudring2` remplace le halo de cubes du Gear 5. `gumpistol`/`gumgatling` sont réservés et testés numériquement, sans remplacer les bras calibrés ; `gumtemplate` est une grille UV, pas une texture finale. Le modèle nommé `gear5` n'est pas importé aveuglément.
- **Gura** : quatre fissures originales distinguent choc aérien, sol, coup descendant et inclinaisons. Une seule expansion par frappe ; feuilles de sol orientées horizontalement.
- **Ope** : six rochers Takt avec leur atlas et animation, variation déterministe par entité. ROOM garde le rayon serveur et des arcs plus lisibles ; Shambles reçoit des anneaux. Ce sont des géométries natives du port, pas une exécution des effets Photon. Amputation conserve son animation et ses règles.
- **Mochi** : coordonnées et UV des poings originaux, allongement selon la distance serveur, poing Yakimochi et tête à pointes BuzzCut. Cette tête est détachée de la longue chaîne source, recentrée puis tournée selon la courte durée du dash. La piste source de 14 secondes n'est pas annoncée comme portée à l'identique. Donuts et cadence conservés.
- **Nikyu** : coussinets originaux et Ursus Shock ; échelles distinctes pour charge, projectile et impact. Les objets porteurs invisibles de la référence ne servent pas seuls de rendu.

Le manifeste décrit 62 fichiers de ressources, dont les bras supplémentaires réservés. `RigAnimationTest` vérifie 63 pistes et 166 764 assertions : os présents, échantillons finis, variation, discontinuités et boucles. Trois modèles Java sont convertis en builders actuels, sans installer Forge.

Les procédures de combat et politiques terrain ont les mêmes classes compilées qu'alpha 23. L'entité visuelle change pour synchroniser son propriétaire, et son modèle lit la texture correspondante : employer la même version du mod côté client et serveur.

## Vérification et limites

Le candidat alpha25 `d5029c6dd87bf924e9e6bfa7ad1f84e9279d111f4dfd723a6967c57d19b69205` passe 681 contrôles natifs à 20 TPS : checks 59, batch02_checks 363, batch03_checks 143, batch05_checks 116. La recette prolongée passe 220 contrôles et 20 captures inspectées par client sur 12 000 ticks (environ dix minutes). Les 50 réinitialisations par rôle rétablissent la forme de base. Les échantillons bornent les effets et les caches ; la mémoire JVM varie avec le ramasse-miettes, sans constituer une preuve d'absence de fuite à long terme. [Preuves structurées](PRIORITY-ASSETS-PREUVES-ALPHA25.json). Une session du premier lot s'est interrompue avant sa fin, puis la recette inchangée est passée. Le premier placement BuzzCut était décalé ; il a été corrigé et repris. Les journaux de ces tentatives sont conservés.

Le rattachement des objets tenus aux mains du nouveau squelette Amaru reste à peaufiner. Le Gear 5 reste une adaptation du joueur avec collier importé. Les autres Zoans adaptés et les finitions Yami/Mera/Boro/Soru/Uta déjà recensées restent ouvertes. Les captures de laboratoire ne constituent pas une acceptation artistique humaine ni un essai de serveur public chargé.

La recette prolongée utilise un acteur et un observateur, les deux connectés ; elle contrôle les réinitialisations et les caches, sans prétendre simuler deux combattants humains indépendants. Le Haki reste inchangé.

La première recette native est conservée avec le statut `error` : un filtre de lanceur erroné (`gomu_gomu` au lieu de `gomu`) n’avait pas lancé Gomu. Le serveur de ce laboratoire a été arrêté après les 59/363/143 contrôles réussis. Une nouvelle recette isolée utilise le filtre correct, passe le socle et Gomu ; le total ci-dessus ne recompte pas les 59 contrôles du socle. Le lanceur source refuse désormais ce filtre invalide avant tout démarrage.
