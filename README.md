# Mod-minecraft — sources des mods

## Sources centralisées

Ce dépôt réunit les projets de développement Minecraft et la passerelle Rogue Trader.

| Dossier | Contenu |
|---|---|
| `voiddynasty/` | Mod principal et flotte à six classes, conservés depuis le commit initial. |
| `voiddynasty-bridge/` | Version du bridge déjà présente dans ce dépôt, avec ses modifications du pad. |
| `voiddynasty-logistics/` | Suite de logistique 0.7.0 : adaptateurs Fabric et Rogue Trader, protocole, simulation, tests et assistant Windows. |
| `onepiece/` | Port Fabric 26.3 : 40 fruits, 213 techniques, formes et revue des assets alpha.19. |

La suite de logistique conserve la copie d’adaptateur Fabric utilisée par ses recettes 0.7.0. Le dossier `voiddynasty-bridge/` reste la version du bridge déjà développée dans ce dépôt ; ses évolutions sont conservées. [Organisation et versions](docs/CENTRALISATION.md).

Les JAR de mods, bibliothèques de jeux, ressources visuelles binaires privées, mondes et données d’exécution restent locaux. One Piece fournit un manifeste et un importeur pour rétablir ses ressources depuis le paquet privé correspondant. Les droits existants de chaque composant sont conservés ; cette centralisation ne les remplace pas par une nouvelle licence.

---

Monorepo des sources du mod Minecraft **VOID DYNASTY** et de son addon **Refit & Bridge**.

| Dossier | Rôle |
|---|---|
| `voiddynasty/` | Le mod principal (Fabric, MC 26.3) : ~180 classes, flotte de vaisseaux en blocs, armes, factions |
| `voiddynasty-bridge/` | Addon client `voiddynasty_bridge` 0.7.0 : pad de commandement à onglets, lasers visibles, effets plasma, logistique |

## Environnement

- Minecraft **26.3**, Fabric Loader **0.19.5**, Fabric API **0.162.0+26.3**
- Java **25** (Temurin)
- Gradle via le wrapper fourni (Loom 1.18)

## Compiler

```bash
# le mod
cd voiddynasty && ./gradlew build        # -> build/libs/voiddynasty-1.0.0.jar

# la bridge (nécessite le jar du mod dans voiddynasty-bridge/libs/)
cd ../voiddynasty-bridge && ./gradlew build   # -> build/libs/voiddynasty_bridge-0.7.0.jar
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
