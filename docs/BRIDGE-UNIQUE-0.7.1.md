# Bridge Minecraft unique — 0.7.1

## Modification

Les 99 fichiers de la copie `voiddynasty-logistics/fabric-adapter/src/` ont été retirés : 31 fichiers Java et 68 ressources. Leur historique reste dans Git. `voiddynasty-bridge/` est l’unique source de compilation. Les 30 classes autrefois dupliquées n’ont plus qu’une définition active. Le fichier `ControlPadMixin` a été déplacé de `attic/` vers les sources actives et redéclaré dans les mixins client.

Le pad conserve `ShipType.values()` au lieu d’une liste fixe de trois classes. Son relais intercepte les réponses envoyées à l’ancien écran pour transmettre la liste au pad affiché. Les adaptateurs RT, le protocole, le simulateur, l’installeur et les tests restent dans la suite logistique.

Les générateurs de catalogue et de modèles écrivent dans la source unique. Le contenu des modèles et ressources JSON est inchangé, hormis les métadonnées de version et la liste des mixins. Le catalogue de 14 missions conserve son empreinte `9fe289e9bb643972571c685e165cea73ffba139bae3392292b7496b8066b19cf`.

## Construction et intégrité

La version de l’ensemble est 0.7.1. Le build local enregistre le chemin du projet canonique, sa version, l’empreinte de ses sources/ressources, le JAR produit et le JAR de base utilisé. La construction de l’assistant refuse un build venant de l’ancienne copie, des sources modifiées après compilation, un artefact altéré ou une base remplacée.

L’empreinte de l’ancien JAR original n’est plus fixée dans le générateur de paquet : chaque assistant est lié exactement au JAR de base qui a servi à compiler son bridge. Le contrôle d’intégrité à l’installation reste obligatoire. Le candidat local a été construit contre le JAR à six vaisseaux déjà installé, sans le remplacer.

JAR du bridge testé : `2a1cf6a8141a25e57cc92ec62ee06c79214875fa365a1a650ef1e2911330dac6`.
Base utilisée : `5c0c92c4f329a0b465607ef197e6e5070784c28130568b032dcd54160450ad29`.
Source du bridge : `938403a60b6f07bd60bd055b9765885b2aa27a88ec74dff95af2a96f85e67b0f`.

L’archive de sources conserve les dossiers voisins `voiddynasty-bridge/` et `voiddynasty-logistics/`, ce qui permet aux mêmes outils d’y retrouver leurs entrées. Elle exclut les références de jeux, les configurations privées et les sauvegardes.

## Vérification du 17 octobre 2026

- Build du bridge Java et du module Rogue Trader : succès.
- Suite principale : 65 tests découverts, 63 réussis et 2 ignorés car leurs fixtures privées n’étaient pas encore présentes dans le checkout.
- Les deux tests de sauvegarde historique ont ensuite réussi sur une copie privée de la fixture existante : migration du reçu sans changement de récompense/identité et lecture de l’archive native. Les 65 tests distincts ont donc réussi, sur deux invocations explicitement séparées.
- Le vrai transformeur Fabric charge les sept classes visées, y compris `ControlPadScreen`. Le test vérifie la présence effective du relais `vd$list` dans la classe transformée.
- 30 assertions en mémoire avec les conteneurs et objets Minecraft : conservation, pagination, synchronisation, géométrie du pad, liste des types de vaisseaux et premier obstacle du laser.
- Huit tests de construction vérifient les changements de source, de ressource, de JAR, de base, de métadonnées et l’absence de seconde source dans le paquet.
- Assistant et ZIP joueur 0.7.1 reconstruits. L’inspection des ressources embarquées confirme la version du bridge, le relais actif et les empreintes des trois composants ainsi que la base/source correspondante.
- Aucun doublon de classe Java active ne subsiste entre les trois projets VOID. Les ressources à identifiants distincts du mod principal et les wrappers Gradle sont conservés.

Ces vérifications ne lancent ni Minecraft ni Rogue Trader et ne remplacent pas un essai visuel du pad dans une partie. Aucun module installé ni monde/sauvegarde de jeu n’a été remplacé. Le conflit de touche R entre VOID DYNASTY et One Piece ne fait pas partie de cette unification.
