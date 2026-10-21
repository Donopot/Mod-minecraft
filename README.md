# Mod-minecraft — sources des mods

## VOID DYNASTY unifié — alpha 8

[Pack client 1.3.0-alpha.8](client-local/voiddynasty-1.3.0-alpha.8) : **un seul JAR VOID DYNASTY**, réunissant le mod principal, le servitor alpha 7 et le bridge Minecraft (pad, coffres, armes et livraisons). Fabric API et GeckoLib restent séparés : trois fichiers dans `mods`. [Installation et build commun](voiddynasty/docs/BUILD-UNIFIE-ALPHA8.md). Retirer l'ancien bridge séparé lors du remplacement ; Rogue Trader reste facultatif.

## Historique : servitor alpha 5

La correction **1.3.0-alpha.94** fiabilise l'exécution des tâches, les chunks actifs et les routines de récolte avec retour confirmé. Les routines conservées après redémarrage se reprennent explicitement avec `/servitor routine resume <id>`. [Installation, changements et vérifications](voiddynasty/docs/SERVITOR-ALPHA94.md).

La version de développement **1.3.0-alpha.5** met à jour le HUD des armes et les raccourcis, avec une [interface coordonnée avec One Piece alpha 20](docs/HUD-KEYBINDINGS.md). Elle conserve les accès en paliers, les zones vides, les matériaux réels, MCP et rollback. [Navigation et commandes](voiddynasty/docs/SERVITOR-NAVIGATION-ALPHA4.md) · [Construction et limites](voiddynasty/docs/SERVITOR-POLYVALENT-ALPHA2.md) · [Exemple Hermes distant](voiddynasty/tools/hermes/README.md) · [Pack alpha de test](client-local/voiddynasty-1.3.0-alpha.5). Les packs précédents restent disponibles.

## Jouer en local, avec ou sans Rogue Trader

[Pack client VOID DYNASTY 1.2.5](client-local/voiddynasty-1.2.5) : les quatre JAR à copier dans `mods`, avec le guide, les dépendances, licences et empreintes. Cible : Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Le contenu Minecraft fonctionne sans Rogue Trader ; les livraisons vers RT nécessitent son lancement.

Le [servitor 1.2.5](voiddynasty/docs/SERVITOR-1.2.5.md) construit lui-même son dépôt après acceptation de la zone, avec conservation des ressources, reçus de rollback et navigation native. Le pack conserve le bridge 0.7.1.

## Sources centralisées

Ce dépôt réunit les projets de développement Minecraft et la passerelle Rogue Trader.

| Dossier | Contenu |
|---|---|
| `voiddynasty/` | Build unifié 1.3.0-alpha.8, flotte, servitor alpha 7 et intégration Minecraft du bridge. |
| `voiddynasty-bridge/` | Source canonique du pad, des coffres, armes et livraisons, compilée dans le JAR principal. |
| `voiddynasty-logistics/` | Suite 0.7.1 : adaptateur Rogue Trader, protocole, simulation, tests et assistant Windows. |
| `onepiece/` | Port Fabric 26.3 : 40 fruits, 213 techniques, formes, assets alpha 19 et HUD alpha 20. |

Le bridge Minecraft conserve ses sources dans `voiddynasty-bridge/`, directement incluses par le build de `voiddynasty/`. La copie de sources dans `voiddynasty-logistics/fabric-adapter/` reste retirée. L'assistant historique 0.7.1 cible les anciennes installations séparées ; le pack alpha 8 s'installe manuellement. [Organisation et versions](docs/CENTRALISATION.md).

Le dossier `client-local/` contient le nouveau pack à trois JAR et les anciens packs à quatre JAR. Les bibliothèques de jeux, ressources privées de One Piece, mondes et données d’exécution restent locaux. One Piece fournit un manifeste et un importeur pour rétablir ses ressources depuis le paquet privé correspondant. Les droits existants de chaque composant sont conservés ; cette centralisation ne les remplace pas par une nouvelle licence.

---

Monorepo des sources du mod Minecraft **VOID DYNASTY**, incluant désormais **Refit & Bridge**.

| Dossier | Rôle |
|---|---|
| `voiddynasty/` | Le mod principal (Fabric, MC 26.3) : ~180 classes, flotte de vaisseaux en blocs, armes, factions |
| `voiddynasty-bridge/` | Sources incluses : pad de commandement à onglets, lasers visibles, effets plasma, logistique |

## Environnement

- Minecraft **26.3**, Fabric Loader **0.19.5**, Fabric API **0.162.0+26.3**
- Java **25** (Temurin)
- Gradle via le wrapper fourni (Loom 1.18)

## Compiler

```bash
# le mod
cd voiddynasty && ./gradlew build        # -> build/libs/voiddynasty-1.3.0-alpha.8.jar, bridge inclus
```

Les binaires de dépendance ne sont pas versionnés dans `libs/`. La [documentation du build unifié](voiddynasty/docs/BUILD-UNIFIE-ALPHA8.md) décrit aussi le compilateur local explicite réellement testé.

## La flotte

Six classes de vaisseaux, stockées en structures de blocs compressées
(`src/main/resources/assets/voiddynasty/structures/*.bin.gz`), posées bloc par bloc :

| Classe | Dimensions | Blocs | Origine |
|---|---|---|---|
| Empereur | 266×118×84 | 75 068 | historique |
| Frégate impériale | 266×118×84 | 75 049 | historique |
| Ruine | 80×16×80 | 7 405 | historique |
| Titan | 54×124×74 | 39 996 | monde 26.3 |
| Andromède | 113×143×444 | 254 767 | `.schematic` MCEdit |
| Executor | 181×53×710 | 1 012 914 | monde 1.21.4 |

Format `.bin.gz` : `gzip`, little-endian — `int nx, ny, nz`, `int count`, palette (longueur + nom d'état),
puis `count × (short palette, short x, short y, short z)`. Coordonnées sur 16 bits non signés (65 535 max par axe).

`ShipType` est la **source unique** des classes disponibles : le pad client et les commandes `/fleet`
itèrent `ShipType.values()` et ne doivent jamais embarquer de liste figée.

## Commandes

```
/fleet list | add <type> <nom> | deploy <nom> | stow <nom> | teleport <nom> <x> <z> | rename | delete
/fleet check <type> [place <x> <y> <z>]
```

Limite : 10 vaisseaux par joueur (`FleetManager` et `FleetState`, à garder alignés).

## Historique

Voir `voiddynasty/MODLOG.md` et `voiddynasty/docs/` (roadmap, décisions, équilibrage, références).
