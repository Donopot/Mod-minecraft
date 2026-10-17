# Organisation des sources

Le commit initial 5c25f8f contient les projets VOID DYNASTY et son bridge. La centralisation ajoute la suite de logistique et le port One Piece sans remplacer ces deux dossiers.

## Suite de logistique 0.7.1 — bridge unique

`voiddynasty-bridge/` est l’unique source Minecraft. `voiddynasty-logistics/` conserve l’adaptateur RT, l’installeur, le protocole, la simulation, les tests et les outils de construction. Son ancien dossier `fabric-adapter/` ne contient plus qu’un lien de migration ; les anciennes sources restent dans l’historique Git.

Les générateurs, le build local, les tests et les paquets pointent vers le bridge voisin. Le relais réseau `ControlPadMixin` est de nouveau actif ; le pad utilise tous les types de `ShipType.values()`. L’assistant embarque uniquement un build issu de cette source et contrôle son empreinte ainsi que celle du JAR de base choisi. Les formats de reçus et le protocole v4 sont inchangés.

Les références Unity, Rogue Trader, UMM et les JAR de mods proviennent de l’installation locale. Les sauvegardes, secrets de liaison et exécutables ne sont pas publiés. Voir [la migration](BRIDGE-UNIQUE-0.7.1.md) et les README pour les prérequis.

## One Piece alpha.19

325 fichiers Java, catalogue 40 fruits / 213 techniques. Les sources et définitions textuelles des ressources sont présentes. Les dernières recettes graphiques sont en cours ; les résultats historiques et les adaptations sont documentés par lot et par empreinte. L’état de développement ne constitue pas une validation artistique humaine.

Les ressources graphiques binaires sont conservées localement. `onepiece/tools/import-private-assets.py` peut vérifier puis rétablir les 762 ressources attendues à partir du paquet privé correspondant. Il ne copie aucune classe ni bibliothèque. Les dépendances GeckoLib et PAL restent dans un dossier `libs/` local.

La compilation privée utilisée pendant le développement dépend d’une configuration `.runtime/` propre au poste. Le projet Gradle et son wrapper sont fournis pour une reconstruction séparée, après installation des dépendances et import des ressources. Une compilation complète hors du poste de développement reste à vérifier.

## Périmètre publié

Le dépôt reçoit les sources, configurations de compilation, schémas, tests et documents. Les laboratoires, comptes de lanceur, jetons, sauvegardes, JAR Forge de référence, bibliothèques de jeux et paquets compilés sont exclus. Le projet tiers universal-modder reste une référence externe ; il n’est pas recopié dans ce monorepo.

## Vérifications de la copie publiée

Le 17 octobre 2026 : 39 tests Python de simulation et de transport passent, ainsi que le test du moteur Windows de l’installeur. La découverte complète passe avec quatre éléments explicitement ignorés faute de builds et de dépendances locales. Les tests Java/C# des adaptateurs demandent leur compilation locale préalable ; les fixtures privées ne sont pas publiées. Les 762 ressources binaires attendues ont été vérifiées contre le paquet privé alpha.19. Les fichiers Python et JSON ont été contrôlés syntaxiquement. Cette vérification de centralisation ne constitue pas une nouvelle recette complète en jeu.
