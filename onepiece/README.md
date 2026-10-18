# One Piece — port Fabric 26.3

Version de développement **1.0.1-alpha.25** : [cinq transformations prioritaires et Gomu/Gura/Ope/Mochi/Nikyu](docs/PRIORITY-ASSETS-WORKORDER.md), avec [preuves et limites](docs/PRIORITY-ASSETS-PREUVES-ALPHA25.json). Les 40 fruits/213 techniques sont conservés ; cette version ne prétend pas refaire toute leur acceptation artistique. Le dragon Seiryu alpha22, la permanence du terrain et le Haki restent inchangés.

- [Décisions sur les 40 fruits](docs/ASSETS-MINEPIECE-40.md)
- [Nouvelle réserve d’assets True Prime Piece Two](docs/REFERENCE-TRUEPRIMEPIECE.md)
- [Assets à mettre à jour : 14 fruits candidats, premier lot de 5 transformations](docs/ASSETS-A-METTRE-A-JOUR.md)
- [Bilan fruit par fruit alpha 23](docs/CHECK-40-FRUITS-ALPHA23.csv) et [preuves des recettes](docs/CHECK-40-PREUVES-ALPHA23.json)
- [Audit des 40 fruits sur alpha 21](docs/CHECK-40-FRUITS-ALPHA21.md) : catalogue et mécaniques couverts ; diagnostic de couleur Phoenix retiré après identification correcte de la queue par l’utilisateur.
- [Validation des assets](docs/ASSETS-ALPHA19-VALIDATION.md)
- [Suivi et historique](docs/HANDOFF.md)
- [Matrice des 213 techniques](docs/matrice-techniques-213.csv)

## Dépendances et ressources

Java 25, Minecraft 26.3, Fabric Loader 0.19.5 et Fabric API 0.162.0+26.3. Placer GeckoLib 5.5.7 et Player Animation Library 1.2.8 dans `libs/`, comme indiqué dans [libs/README.md](libs/README.md). Restaurer les 62 fichiers prioritaires avec `python tools/import_priority_assets.py --reference <trueprimepiecetwo-infusedrocknroll.jar>` (empreinte contrôlée). Rétablir les autres ressources visuelles privées depuis le paquet correspondant avec `python tools/import-private-assets.py --jar <paquet.jar>`. Les JAR de référence Forge restent des références locales.

## Compilation

Après import des ressources et dépendances : `gradlew.bat build` sous Windows, ou `bash gradlew build` sous Linux. Le wrapper cible Gradle 9.7.1 et Loom 1.18-SNAPSHOT. Ce chemin Gradle hors du poste de développement reste à vérifier.

Les outils `build_local.py`, `run_local_tests.py` et `launch_visual_lab.py` conservent le flux privé utilisé pendant les réparations. Ils attendent la configuration locale `.runtime/` et l’installation vérifiée des jeux ; cette configuration n’est pas publiée. Les tests utilisent des mondes jetables et deux clients locaux. La destruction du terrain par les techniques concernées est définitive.
