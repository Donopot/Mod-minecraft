# VOID DYNASTY — Fabric 26.3

La version **1.3.0-alpha.8** réunit le mod principal et les sources Minecraft de `../voiddynasty-bridge/` dans un seul JAR. Elle conserve le servitor alpha 7, la flotte, les armes, les coffres, le HUD et les livraisons optionnelles vers Rogue Trader.

[Installation, compilation et validation](docs/BUILD-UNIFIE-ALPHA8.md) · [Servitor alpha 7](docs/SERVITOR-IA-ALPHA7.md) · [Vision du servitor](docs/SERVITOR-VISION.md) · [MCP/Hermes](tools/hermes/README.md)

Compiler avec Java 25 : `./gradlew build`. Placer GeckoLib Fabric 26.3 dans `libs/` ; les bibliothèques Minecraft et Fabric doivent être disponibles. Le build compile directement les sources voisines du bridge, sans ancien JAR VOID DYNASTY.

Pour un build local explicite, `python tools/build_unified.py --classpath-json <fichier> --jdk <jdk25> --output <sortie>` est le chemin testé sur ce poste. Le fichier privé décrit les bibliothèques installées ; aucune bibliothèque du jeu n'est distribuée.

Le pack client contient le JAR VOID DYNASTY unifié, Fabric API et GeckoLib. Retirer l'ancien mod principal et le bridge séparé avant de le tester sur une copie de monde.
