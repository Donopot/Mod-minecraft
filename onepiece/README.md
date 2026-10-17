# One Piece — port Fabric 26.3

Version de développement **1.0.1-alpha.19**. Le catalogue compte **40 fruits et 213 techniques**, avec des formes hybrides et complètes. La revue des assets a relié 53 profils aux visuels de MinePiece ver15 : 60 fichiers originaux contrôlés et 8 adaptations de pistes. Les dernières recettes graphiques restent en cours ; aucune acceptation artistique humaine n’est implicite.

- [Décisions sur les 40 fruits](docs/ASSETS-MINEPIECE-40.md)
- [Validation des assets](docs/ASSETS-ALPHA19-VALIDATION.md)
- [Suivi et historique](docs/HANDOFF.md)
- [Matrice des 213 techniques](docs/matrice-techniques-213.csv)

## Dépendances et ressources

Java 25, Minecraft 26.3, Fabric Loader 0.19.5 et Fabric API 0.162.0+26.3. Placer GeckoLib 5.5.7 et Player Animation Library 1.2.8 dans `libs/`, comme indiqué dans [libs/README.md](libs/README.md). Rétablir les ressources visuelles privées depuis le paquet correspondant avec `python tools/import-private-assets.py --jar <paquet.jar>`. Les JAR de référence Forge restent des références locales.

## Compilation

Après import des ressources et dépendances : `gradlew.bat build` sous Windows, ou `bash gradlew build` sous Linux. Le wrapper cible Gradle 9.7.1 et Loom 1.18-SNAPSHOT. Ce chemin Gradle hors du poste de développement reste à vérifier.

Les outils `build_local.py`, `run_local_tests.py` et `launch_visual_lab.py` conservent le flux privé utilisé pendant les réparations. Ils attendent la configuration locale `.runtime/` et l’installation vérifiée des jeux ; cette configuration n’est pas publiée. Les tests utilisent des mondes jetables et deux clients locaux. La destruction du terrain par les techniques concernées est définitive.
