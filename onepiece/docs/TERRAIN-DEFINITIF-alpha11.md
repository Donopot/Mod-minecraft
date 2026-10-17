# Terrain définitif — alpha.11

La demande du joueur du 16 octobre remplace les anciennes exigences de restitution : toute destruction, transformation ou création de blocs par un pouvoir est définitive. Elle concerne aussi les helpers historiques, pas uniquement les météores Zushi et Magu. Les effets sur les entités, ROOM, les charges, le vol et les champs de dégâts conservent leurs durées propres.

`PowerBlockService` remplace `TemporaryBlockService`. Il ne mémorise plus d’état original et n’exécute aucun rollback. Les échéances, le retrait d’un fruit, la suppression du pouvoir, la mort et la déconnexion libèrent seulement l’attribution. Le monde Minecraft conserve les blocs après redémarrage.

Les constructions utilisent des matériaux stables : PACKED_ICE, SANDSTONE, MAGMA_BLOCK, HONEY_BLOCK, feuilles PERSISTENT et fissures AIR/CRACKED_STONE_BRICKS. La glace ne fond pas, le grès ne tombe pas et les feuilles de pouvoir ne se décomposent pas. Ces blocs peuvent ensuite être modifiés normalement par les joueurs. Un MAGMA_BLOCK reste un bloc chaud vanilla après la fin du champ attribué.

Les quotas de destruction par lancement restent identiques. Les techniques sans interaction terrain restent sans interaction terrain. Le service conserve les contrôles de chunk chargé, frontière, protection et inventaires ; les impacts destructeurs gardent leur filtre de résistance et les obstacles résistants protègent ce qui se trouve derrière. Aucun feu propagatif n’a été ajouté.

Migration : lire les entrées placées de `temporary-blocks.dat` sans jamais appliquer leur ancien champ `original`. Écrire `power-block-leases.dat` ; conserver le journal ancien. Stabiliser les anciens feuillages identifiables dès le chargement des chunks, en tolérant seulement un changement naturel de distance aux troncs. Respecter les blocs remplacés ensuite par le joueur. Les changements déjà restaurés par une ancienne version ne peuvent pas être reconstruits à partir du monde courant.

Vérification prévue : les anciens contrôles de restitution deviennent des contrôles positifs de conservation avec libération de l’attribution. La recette terrain contrôle les quatre transformations Hie/Suna après échéance et retrait du fruit. La recette indépendante `dpermanent` pose sept matériaux, attend 80 vrais ticks, redémarre le serveur et relit les blocs. Un second redémarrage contrôle un journal ancien expiré et une modification ultérieure du joueur. Le rapport final et les lanceurs ne sont actualisés qu’après le résultat réel.

Les mondes et mods habituels restent séparés du laboratoire. Les anciennes versions et leurs preuves restent conservées dans les manifestes privés.
