# Mod-minecraft — sources des mods

## Développement du servitor polyvalent

La version de développement **1.3.0-alpha.5** met à jour le HUD des armes et les raccourcis, avec une [interface coordonnée avec One Piece alpha 20](docs/HUD-KEYBINDINGS.md). Elle conserve les accès en paliers, les zones vides, les matériaux réels, MCP et rollback. [Navigation et commandes](voiddynasty/docs/SERVITOR-NAVIGATION-ALPHA4.md) · [Construction et limites](voiddynasty/docs/SERVITOR-POLYVALENT-ALPHA2.md) · [Exemple Hermes distant](voiddynasty/tools/hermes/README.md) · [Pack alpha de test](client-local/voiddynasty-1.3.0-alpha.5). Les packs précédents restent disponibles.

## Jouer en local, avec ou sans Rogue Trader

[Pack client VOID DYNASTY 1.2.5](client-local/voiddynasty-1.2.5) : les quatre JAR à copier dans `mods`, avec le guide, les dépendances, licences et empreintes. Cible : Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Le contenu Minecraft fonctionne sans Rogue Trader ; les livraisons vers RT nécessitent son lancement.

Le [servitor 1.2.5](voiddynasty/docs/SERVITOR-1.2.5.md) construit lui-même son dépôt après acceptation de la zone, avec conservation des ressources, reçus de rollback et navigation native. Le pack conserve le bridge 0.7.1.

## Sources centralisées

Ce dépôt réunit les projets de développement Minecraft et la passerelle Rogue Trader.

| Dossier | Contenu |
|---|---|
| `voiddynasty/` | Mod principal 1.3.0-alpha.5, flotte à six classes et missions du servitor. |
| `voiddynasty-bridge/` | Source unique du bridge Minecraft 0.7.1 : pad dynamique, coffres, armes et logistique. |
| `voiddynasty-logistics/` | Suite 0.7.1 : adaptateur Rogue Trader, protocole, simulation, tests et assistant Windows. |
| `onepiece/` | Port Fabric 26.3 : 40 fruits, 213 techniques, formes, assets alpha 19 et HUD alpha 20. |

Le bridge Minecraft est construit uniquement depuis `voiddynasty-bridge/`. La copie de sources dans `voiddynasty-logistics/fabric-adapter/` a été retirée ; l’assistant et les tests utilisent le projet unique. [Organisation et versions](docs/CENTRALISATION.md).

Le dossier `client-local/` publie uniquement les quatre JAR nécessaires à VOID DYNASTY, à la demande de l’utilisateur. Les bibliothèques de jeux, ressources privées de One Piece, mondes et données d’exécution restent locaux. One Piece fournit un manifeste et un importeur pour rétablir ses ressources depuis le paquet privé correspondant. Les droits existants de chaque composant sont conservés ; cette centralisation ne les remplace pas par une nouvelle licence.

---

Monorepo des sources du mod Minecraft **VOID DYNASTY** et de son addon **Refit & Bridge**.

| Dossier | Rôle |
|---|---|
| `voiddynasty/` | Le mod principal (Fabric, MC 26.3) : ~180 classes, flotte de vaisseaux en blocs, armes, factions |
| `voiddynasty-bridge/` | Addon client `voiddynasty_bridge` 0.7.1 : pad de commandement à onglets, lasers visibles, effets plasma, logistique |

## Environnement

- Minecraft **26.3**, Fabric Loader **0.19.5**, Fabric API **0.162.0+26.3**
- Java **25** (Temurin)
- Gradle via le wrapper fourni (Loom 1.18)

## Compiler

```bash
# le mod
cd voiddynasty && ./gradlew build        # -> build/libs/voiddynasty-1.3.0-alpha.5.jar

# la bridge (nécessite le jar du mod dans voiddynasty-bridge/libs/)
cd ../voiddynasty-bridge && ./gradlew build   # -> build/libs/voiddynasty_bridge-0.7.1.jar
```

Voir `voiddynasty-bridge/libs/README.md` : les binaires de dépendance ne sont pas versionnés.

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
