# VOID DYNASTY — bridge Minecraft 0.7.1

Ce projet est la source unique du compagnon `voiddynasty_bridge`. Il conserve le pad à six types via `ShipType.values()`, les correctifs des coffres et des armes, et le relais `ControlPadMixin` qui transmet les réponses de flotte au pad affiché.

La suite [../voiddynasty-logistics](../voiddynasty-logistics) contient l’adaptateur Rogue Trader, le protocole, le simulateur, les tests et l’assistant Windows. Elle compile et empaquette ce projet ; aucune seconde copie du bridge n’est maintenue.

## Compilation

Gradle : placer le JAR de base dans `libs/voiddynasty-1.0.0.jar`, puis lancer `bash gradlew build` avec Java 25. Les dépendances Fabric doivent être disponibles.

Build local Windows et assistant : depuis `../voiddynasty-logistics`, lancer `python tools/build-fabric-diagnostic.py --java-home <jdk25> --original-jar <base.jar>`, compiler l’adaptateur RT, lancer `python tools/verify.py`, puis `python tools/build-installer.py`.

Le build enregistre l’empreinte exacte du JAR de base et celle des sources du bridge. Le constructeur de l’assistant refuse un ancien build, des sources modifiées après compilation ou un JAR altéré. L’assistant conserve le contrôle d’intégrité et accepte uniquement le JAR de base exact ayant servi à son paquet.

Les mondes, bibliothèques et JAR de jeux restent locaux. La version 0.7.1 ne change ni le protocole de livraison ni les reçus.
