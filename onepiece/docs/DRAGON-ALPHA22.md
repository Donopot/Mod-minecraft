# Seiryu alpha 22 — squelette et vol en 3D

La forme complète du Uo Uo no Mi conserve le modèle MinePiece Kaido existant. Ses 24 sections de corps suivent maintenant le chemin parcouru par la tête, en hauteur comme dans les virages. Une petite ondulation accompagne ce mouvement. La géométrie originale contient toujours 51 os et 346 cubes ; sa texture, ses UV et les fichiers d’assets sont inchangés.

## Commandes

Passer en forme complète avec la touche de transformation (V par défaut), puis démarrer le vol par le double appui sur Saut habituel. Maintenir **Avancer** pour suivre le regard : regarder vers le haut fait monter, regarder vers le bas fait descendre. Relâcher les touches de déplacement freine jusqu’au vol stationnaire. Reculer et les déplacements latéraux restent disponibles ; Saut et Accroupir ajustent aussi l’altitude. Sprint augmente la vitesse. Les touches personnalisées de Minecraft restent utilisées.

La vue arrière F5 passe au-dessus de l’épaule pour dégager la crinière. Son décalage est réduit devant un mur ou sous un plafond, avec un volume de collision de caméra. Les vues avant et première personne gardent leur comportement précédent. Le changement de forme rétablit automatiquement la caméra habituelle.

## Vérification

JAR final `opworldlike-1.0.1-alpha.22.jar`, SHA256 `db2e88e42a3257659e28a4f5cb836a080b8aa274fbddb7b532854ee90fa27f2c`.

- Compilation des 335 sources principales, puis tests numériques du pilotage, freinage, accélération, vitesse maximale, suivi du chemin 3D et immutabilité des poses.
- Comparaison binaire avec alpha 21 : 1 807 fichiers d’assets identiques et toutes les classes serveur identiques. Seuls les classes client et les mixins de rendu, déplacement client et caméra changent.
- 59 contrôles natifs du socle sur le JAR final. Les 188 contrôles du lot Seiryu passent sur le candidat avant ajout de la caméra ; leurs classes serveur sont identiques au JAR final. La première tentative avec un mauvais nom de filtre est conservée comme échec de scénario, pas comptée comme réussite.
- Deux vrais clients sur un serveur local isolé, à cadence normale : 16 contrôles acteur et 2 observateur passent. Montée, descente, virages, freinage, collision de la tête, permissions de vol, suppression et énergie épuisée sont mesurés. Les positions correspondantes sont aussi relues côté serveur. La caméra arrière se dégage, se rapproche sous un plafond et reprend sa position habituelle en forme de base.
- 9 captures acteur et 9 observateur du JAR final, examinées sur deux planches et des vues individuelles. Les 51 os et 346 cubes sont comptés dans le modèle réellement évalué. Les essais précédents restent conservés, notamment les corrections du spam de commandes, de distance de suivi et de brouillard du laboratoire. L’acceptation artistique de l’utilisateur reste attendue.

Les fixtures `DragonLabServer`, `DragonLabClient` et leur mixin de lecture du rig restent dans `src/test/java` et un JAR séparé de laboratoire. Elles ne font pas partie du paquet jouable. Les preuves privées et les mondes jetables restent sous `.runtime/onepiece-dragon/` et `.runtime/onepiece-lab/` du workspace.

## Portée

Le corps et la queue suivent visuellement le chemin ; les collisions de déplacement restent celles de la tête/joueur existant. La queue peut traverser un obstacle et n’inflige pas de dégâts de contact supplémentaires. Le vol conserve les permissions, coûts, fin de transformation et sauvegardes précédents. Aucun terrain n’est détruit automatiquement par ce nouveau mouvement. Les règles de destruction définitive des techniques restent inchangées.

Pas de nouvelle recette exhaustive des 40 fruits dans cette passe, ni d’essai prolongé en survie ou de charge avec de nombreux dragons. Les réserves artistiques générales de l’alpha 21 restent distinctes. Le [nouveau JAR True Prime Piece Two](REFERENCE-TRUEPRIMEPIECE.md) est inventorié et conservé comme référence locale ; aucun de ses assets ne remplace le modèle actuel.
