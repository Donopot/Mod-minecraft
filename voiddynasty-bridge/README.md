# Sources du bridge Minecraft — incluses dans VOID DYNASTY alpha 8

La distribution actuelle compile ce dossier avec `../voiddynasty/` dans **un seul JAR `voiddynasty-1.3.0-alpha.8.jar`**. Il ne faut plus installer le compagnon séparé avec cette version. [Compilation unifiée et installation](../voiddynasty/docs/BUILD-UNIFIE-ALPHA8.md).

Les instructions ci-dessous restent celles de la distribution historique 0.7.1 pour les anciennes versions séparées. Ce dossier conserve l'unique source des correctifs du pad, des coffres, des armes et de la logistique Minecraft.

## Historique : bridge séparé 0.7.1

Ce projet est la source unique du compagnon `voiddynasty_bridge`. Il conserve le pad à six types via `ShipType.values()`, les correctifs des coffres et des armes, et le relais `ControlPadMixin` qui transmet les réponses de flotte au pad affiché.

La suite [../voiddynasty-logistics](../voiddynasty-logistics) contient l’adaptateur Rogue Trader, le protocole, le simulateur, les tests et l’assistant Windows. Elle compile et empaquette ce projet ; aucune seconde copie du bridge n’est maintenue.

## Compilation

Gradle : placer le JAR de base dans `libs/voiddynasty-1.0.0.jar`, puis lancer `bash gradlew build` avec Java 25. Les dépendances Fabric doivent être disponibles.

Build local Windows et assistant : depuis `../voiddynasty-logistics`, lancer `python tools/build-fabric-diagnostic.py --java-home <jdk25> --original-jar <base.jar>`, compiler l’adaptateur RT, lancer `python tools/verify.py`, puis `python tools/build-installer.py`.

Le build enregistre l’empreinte exacte du JAR de base et celle des sources du bridge. Le constructeur de l’assistant refuse un ancien build, des sources modifiées après compilation ou un JAR altéré. L’assistant conserve le contrôle d’intégrité et accepte uniquement le JAR de base exact ayant servi à son paquet.

Les mondes, bibliothèques et JAR de jeux restent locaux. La version 0.7.1 ne change ni le protocole de livraison ni les reçus.
