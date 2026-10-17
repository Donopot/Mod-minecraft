# Revue des assets — premier groupe de cinq fruits

Mera, Magu, Goro, Hie et Pika : 32 techniques. Les 45 fichiers de bytecode réservés décrivent les classes de sélection et leurs délégués, pas une nouvelle vérification de tous leurs budgets de combat. Les candidats de noms sont séparés des liens réellement établis.

Trois remplacements client : Hiken utilise maintenant `fist_small` et la texture de sa couche `flame_tiger` ; Ice Partisan utilise `partisan/tex_ice` ; Pheasant Beak utilise `phoenix/tex_ice`. La texture, la géométrie et les animations originales existent déjà dans le paquet. Le port adapte seulement orientation, taille et ancrage de l’oiseau à sa trajectoire. Les dégâts, collisions, propriétés et durées serveur restent identiques.

Magu conserve les modèles natifs déjà reliés : fist_small/tex_magma et inugami_guren. Goro et Pika reposent largement sur des particules, des rayons et des modèles partagés : pas d’animal ou de mesh propre inventé à partir d’un nom de technique. La source Raigo réutilise Meteor avec tex_white2 ; la forme Amaru n’a pas de mesh dédié dans ver15 et garde son adaptation visuelle documentée. Les rayons Pika ont un matériau source tex_yellow. Les détails des branches partagées restent à approfondir avant d’ajouter d’autres substitutions.

La reprise Hie alpha.18 passe20images,5contrôles de rendu et10évaluations PAL par rôle ; trident et oiseau effectivement vus en images complètes. La première reprise Mera passe48images acteur et24observateur ; le poing d’origine est visible, mais la traînée procédurale ajoutée créait des bulles trop dominantes : retirée après inspection. La finition alpha.18 utilise finalement les vrais sprites animés de la propulsion sur les sept attaques :48captures acteur,24observateur, présence des sept profils dans chaque journal et deux planches inspectées. Les contrôles de durée et de propulsion passent. Entei conserve une limite de caméra liée à son mesh opaque. Voir build/mera-flames-alpha18-proof.local.json. Ne pas confondre la revue des fichiers avec une acceptation artistique humaine.

Les 35 autres fruits restent à revoir pour l’intégration des assets, en plus des techniques communes qui nécessitent une lecture de branches. Toutes les 2881 ressources originales sont déjà réservées ; aucune installation Forge ni changement aux parties personnelles.

