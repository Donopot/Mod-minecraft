# PLAN — Reconstruction de VoidDynasty sur NeoForge 1.21.1

> Statut : proposition détaillée, validée sur les données réelles du serveur (scan du 2026-10-21).
> Décisions actées par Donopot : **cible 1.21.1**, **map transplantée par schematics WorldEdit**,
> **périmètre greenfield sélectif** (on ne réimplémente que ce que la map utilise + le gameplay essentiel),
> **sans le pont Rogue Trader**.

---

## 1. Pourquoi 1.21.1 et pas 26.3

| Critère | 26.3 | **1.21.1 (choisi)** |
|---|---|---|
| Map actuelle (604 Mo) | conservée telle quelle | transplantée par schematics |
| Create 6.0.10 | ❌ aucune version (Modrinth : 1.21.1 uniquement) | ✅ |
| TheWarhammerMod 1.6.0 | ❌ exige MC [1.21.1] | ✅ exige NeoForge ≥ 21.1.190 |
| Écosystème mods | restreint (26.x récent) | très large |
| Notre code existant (27k LOC) écrit pour les API 26.3 | réutilisable | **à réécrire** (API vanilla 1.21.1) |

Conséquence assumée : ce n'est pas un portage mais une **réécriture ciblée**, avec récupération de
l'essentiel de la map via schematics.

## 2. Chaîne technique vérifiée (21/10/2026)

| Composant | Version retenue | Preuve |
|---|---|---|
| NeoForge | **21.1.264** (259 builds 21.1.x) | `maven.neoforged.net` |
| Java | **21** | exigence MC 1.21.1 |
| Build | **ModDevGradle 2.0.148** | maven NeoForged |
| GeckoLib | **4.9.3** neoforge 1.21.1 | Modrinth (27 versions 1.21.1) |
| Modèles GeckoLib | 33 `.geo.json` en `format_version 1.12.0` + 31 animations `1.8.0` | **compatibles 4.9.3 sans retouche** |
| WorldEdit (extraction, serveur 26.3) | **7.4.6** | jar fourni, neoforge ≥ 26.3.0.83 |
| WorldEdit (cible 1.21.1) | **7.3.8** | Modrinth (1.21.1 + neoforge) |
| Create | **6.0.10** | exige NeoForge ≥ 21.1.219, `flywheel [1.0,2.0)`, `ponder ≥1.0.82` |
| Ponder 1.21.1 | `1.21.1-2.4.0+neoforge` | Modrinth |
| Flywheel 1.21.1 | **à récupérer** (absent de Modrinth, maven Create) | jar Create 6.0.10 |
| TheWarhammerMod | **1.6.0** | jar fourni, neoforge ≥ 21.1.190 |

⚠️ Les jars **Waystones 26.3.0.1** et **Xaero's Minimap 26.6.1** fournis sont pour **26.3** : il faudra
leurs builds **1.21.1** (à vérifier côté CurseForge/Modrinth), tout comme `xaerolib` (absent de l'envoi).

## 3. Ce que la map contient réellement (mesuré, pas supposé)

Scan de 60 670 chunks (overworld 56 634 + nether 4 036 ; 3 dimensions vanilla, aucun monde custom).

### 3.1 Contenu VoidDynasty dans le monde

| Type | Volumétrie |
|---|---|
| Chunks avec du VoidDynasty | **230** (overworld uniquement) |
| Occurrences | **738** |
| IDs de blocs distincts | **8** |
| Entités | **1** (`voiddynasty:servitor_2`) |
| Items en inventaire joueur | **2** (`control_pad`, `laser_drill`) |

| ID | Occurrences | Traitement |
|---|---|---|
| `deepslate_ceramite_ore` | 487 | **non transplanté** — régénéré par le worldgen 1.21.1 |
| `ceramite_ore` | 228 | **non transplanté** — idem |
| `super_container` | 13 | **transplanté** (inventaire dans le NBT) |
| `super_chest` | 5 | **transplanté** |
| `promethium_brewing_stand` | 2 | **transplanté** |
| `promethium_furnace` | 1 | **transplanté** |
| `promethium_blast_furnace` | 1 | **transplanté** |
| `promethium_smoker` | 1 | **transplanté** |

→ **Contrat de compatibilité final : 6 IDs de blocs + leurs clés NBT + 1 entité + 2 items.**
Les minerais n'entrent pas dans le contrat : le nouveau monde les régénère.

### 3.2 Zones construites à exporter (scan des blocs artificiels)

Méthode : décompression de chaque chunk, comptage des entrées de palette correspondant à des blocs
posés par un joueur (planches, verre, béton, cuivre, coffres, rails, tuff bricks…), terracotta des
badlands **exclue** (faux positif naturel), puis regroupement en clusters.

**2 161 chunks construits, 173 zones.** Top 12 :

| # | Chunks | X | Z | Signature dominante |
|---|---|---|---|---|
| 1 | 155 | -272 → 304 | -192 → 176 | chest 430, wall_torch 427, cobbled_deepslate_stairs 376, dispenser 348 |
| 2 | 65 | -544 → -384 | -1568 → -1312 | oak_fence 307, waxed_oxidized_cut_copper_stairs 114, tuff_brick 90 |
| 3 | 65 | 720 → 880 | -1632 → -1168 | oak_fence 223, moss_carpet 155, copper_stairs 76 |
| 4 | 64 | 768 → 1104 | 528 → 688 | oak_fence 336, oak_planks 98, rail 81 |
| 5 | 56 | -32 → 176 | -400 → -208 | oak_fence 231, brick 154, copper_stairs 90 |
| 6 | 55 | 560 → 720 | -2080 → -1792 | oak_fence 157, copper_stairs 92, tuff_brick 74, chest 70 |
| 7 | 48 | 576 → 864 | 16 → 336 | oak_fence 248, chest 80, dispenser 78 |
| 8 | 46 | 1696 → 1856 | -3344 → -3040 | oak_fence 222, copper_stairs 63, tuff_brick 48 |
| 9 | 39 | -832 → -720 | 2784 → 3072 | oak_fence 286, rail 89, oak_planks 83 |
| 10 | 33 | -384 → -224 | -880 → -640 | waxed_oxidized_cut_copper_stairs 65, tuff_brick 62 |
| 11 | 32 | 1488 → 1696 | -2912 → -2672 | oak_fence 178, rail 53, chest 28 |
| 12 | 32 | 32 → 176 | -1120 → -992 | chiseled_tuff_brick 44, tuff_brick 56 |

Lecture : les zones à forte signature `chest / dispenser / copper_stairs / tuff_brick` sont des
constructions joueur. Les zones dominées par `oak_fence` seul sont probablement des **villages
générés** (ils réapparaîtront naturellement en 1.21.1, inutile de les exporter).

## 4. Périmètre du rebuild

### 4.1 Conservé (cœur)
- **Blocs machines (7)** : `super_chest`, `super_container`, `mining_rig`, `promethium_furnace`,
  `promethium_blast_furnace`, `promethium_smoker`, `promethium_brewing_stand`
- **BlockEntities (3)** : `super_chest`, `super_container`, `mining_rig`
- **Entités (5)** : `servitor_1/2/3` + `bolt_projectile`, `laser_beam` (+ `projectile_chaos`)
- **Items** : `control_pad`, `laser_drill` + armes (boltgun, bolt_pistol, heavy_bolter, stalker_bolt_rifle,
  shotgun, las_gun, melta_gun, plasma_incinerator, plasma_pistol, flamethrower, chainsword, power_sword,
  power_hammer) + munitions (9) + matériaux (ceramite ×9, adamantium, titanium, silicon_carbide, wires,
  promethium, focus_lens, plasma_coil, warhead, wax_seal)
- **Servitor AI + work** : BrainLlm (LM Studio `localhost:1234`), routines, mine/craft/smelt/supply, missions
- **Flotte** : pad, `FleetManager`/`ShipData` (déploiement/rangement)
- **1 onglet créatif**, sons, HUD bolter, keybinds

### 4.2 Reporté (phase 2 du projet)
- 30 mobs (Chaos, Tyranides, boss nommés) + ~25 spawn eggs
- 36 blocs décoratifs (ceramite blocks, corpses 1-6, crates, gothic, sandbags, blood, laz_cannon, turret…)
- Campagne (`CampagneService`, 7 fichiers) et dégâts de terrain (3 fichiers)

### 4.3 Abandonné
- **Tout le pont Rogue Trader** : `DeliveryService`, `DeliveryScreen`, `DeliveryInteraction`,
  `DeliveryReconciliation`, `CargoCatalog`, `CargoBlockEntity`, `SetupScreen`, `DiagnosticConnection`,
  `BridgeMod`, `BridgeWorld` (~3 184 LOC) + module `voiddynasty-logistics` entier
- `world/data/fabric/registry_entries.dat` (artefact Fabric, 172 IDs)
- Fabric Loader / Fabric API / Loom
- Intégration claims Fabric (`compat/ClaimIntegration`) → à réévaluer (Open Parties and Claims si besoin)

## 5. Architecture cible

```
voiddynasty-neoforge/
├── gradle.properties            mod_version, neo_version=21.1.264, minecraft_version=1.21.1
├── src/main/java/com/donopot/voiddynasty/
│   ├── VoidDynasty.java         @Mod + ctor(IEventBus, ModContainer)
│   ├── registry/                DeferredRegister : Items, Blocks, BlockEntities, Entities,
│   │                            Menus, Sounds, CreativeTabs, DataComponents
│   ├── content/                 machines/ weapons/ servitors/ fleet/
│   ├── network/                 CustomPacketPayload + StreamCodec + RegisterPayloadHandlersEvent
│   ├── client/                  RegisterGuiLayersEvent, EntityRenderersEvent,
│   │                            RegisterKeyMappingsEvent, RegisterMenuScreensEvent
│   ├── data/                    datagen : models, blockstates, items, recipes, loot, tags, lang
│   └── mixin/                   BlockEntityTypeMixin + AvatarDrillPoseMixin (targets identiques)
├── src/main/resources/
│   ├── META-INF/neoforge.mods.toml
│   ├── assets/                  textures + geo/anim GeckoLib réutilisés
│   └── data/                    recipes/loot générés
└── contract/ids.json            contrat de compatibilité map
```

Choix structurants :
- **DeferredRegister partout** (l'ancien code faisait 102 `Registry.register` directs, interdit sur NeoForge).
- **Datagen** pour models/blockstates/recipes/loot/tags/lang (578 JSON à la main = principale source de bugs).
- **Rendu 1.21.1 = plus simple qu'en 26.3** : `GuiGraphics`/`PoseStack` en immédiat, pas de pipeline
  « render states ». Le faisceau laser (`LaserDrillBeamRenderer`) sera plus simple à réécrire en 1.21.1.
- **Renommages vanilla à appliquer** (API 26.3 → 1.21.1) : `Identifier` → `ResourceLocation`,
  signatures de rendu, versions de `Item.Properties`/`DataComponents` (présents dans les deux).
- `SavedData` NeoForge pour l'état de flotte ; `ModConfigSpec` pour la config servitor
  (migration depuis `config/voiddynasty-servitor.json`).

## 6. Procédure de migration de la map

### Étape A — Extraction (serveur 26.3 actuel + WorldEdit 7.4.6)
Pour chaque zone retenue (voir §3.2), en jeu ou via console op :
```
/tp <x> <y> <z>            # centre de la zone
//pos1 <x1> <y1> <z1>
//pos2 <x2> <y2> <z2>
//copy -e -b                # -e = entités, -b = biomes
//schem save zone_XX sponge # format Sponge v3, lisible par WorldEdit 7.3.8
```
Plage Y recommandée : -64 → 320 (monde entier en hauteur) pour ne rien couper.

### Étape B — Collage (serveur 1.21.1 neuf + WorldEdit 7.3.8)
```
//schem load zone_XX
//paste -e -b
```
- Les blocs vanilla survivent ; les blocs VoidDynasty survivent **si le mod est chargé avec les mêmes IDs**.
- Les inventaires des `super_chest`/`super_container` sont portés par le NBT du schematic.
- Les minerais ne sont pas transplantés (régénération naturelle).

### Étape C — Vérification
- `/schem list` voit tous les fichiers ; tailles cohérentes avec §3.2.
- Contrôle visuel de chaque zone (capture écran avant/après).
- Contrôle des inventaires des conteneurs.

## 7. Phases et critères d'acceptation

| Phase | Contenu | Critère PASS |
|---|---|---|
| **P0 — Cadastre** | Extraire les 8 IDs + clés NBT ; produire `contract/ids.json` | fichier versionné + gametest de conformité |
| **P1 — Extraction map** | WorldEdit 7.4.6 sur serveur 26.3, export des zones | N schematics valides, contrôle visuel |
| **P2 — Squelette** | ModDevGradle 2.0.148, neo 21.1.264, Java 21, `@Mod`, `neoforge.mods.toml` | serveur 1.21.1 démarre, mod listé |
| **P3 — Registres sélectifs** | 7 blocs, 3 BE, entités, items, menu, sons, tab en DeferredRegister | `/setblock voiddynasty:super_chest` et `/summon voiddynasty:servitor_2` OK |
| **P4 — Assets & datagen** | textures + 33 geo + 31 anim (GeckoLib 4.9.3), datagen complet | blocs texturés, recettes/loot présents |
| **P5 — Machines & menus** | super_chest/super_container + menus, mining_rig, 4 fourneaux | ouverture des conteneurs, NBT ancien relu sans perte |
| **P6 — Armes & projectiles** | WeaponDefinitions, WeaponItem + DataComponents, bolt/laser/chaos | tir réel, dégâts conformes |
| **P7 — Servitor AI & work** | BrainLlm (LM Studio), routines, missions mine/craft/smelt/supply | mission complète récolte → livraison coffre |
| **P8 — Flotte** | pad, FleetManager/ShipData | déployer + ranger un vaisseau sans perte |
| **P9 — Client** | HUD bolter, keybinds, faisceau laser | HUD + faisceau visibles |
| **P10 — Collage map** | Paste des schematics sur le monde 1.21.1 | zones conformes + inventaires intacts |
| **P11 — Bascule** | Serveur 1.21.1 + Create 6.0.10 + Warhammer Mod + Waystones/Xaero | session réelle, 0 régression |

Règle absolue : **jamais la map de production avec un mod incomplet** (tout bloc/entité inconnu
disparaît à la sauvegarde). Tout se fait sur copies.

Estimation (à recalibrer après P3) : **P0-P5 ≈ 10-14 sessions**, **P6-P9 ≈ 10-14**, **P10-P11 ≈ 3-5**.

## 8. Risques et parades

| Risque | Parade |
|---|---|
| Perte de constructions au transfert | schematics + contrôle visuel zone par zone + backup tar.gz du monde 26.3 |
| Inventaires des conteneurs | NBT porté par le schematic ; test explicite en P5/P10 |
| Modèles GeckoLib | vérifié : `format_version 1.12.0` / animations `1.8.0` → OK en 4.9.3 |
| Réécriture vanilla 26.3 → 1.21.1 | périmètre sélectif + reprise de la logique comme spécification |
| Dépendances Create | flywheel 1.21.1 à récupérer (maven Create), ponder 1.21.1-2.4.0 |
| Écarts de génération d'ores | reprendre les seuils de l'ancien `ModWorldGen` en biome modifier JSON |
| Deux codebases | geler le dépôt Fabric à `alpha.94`, plus aucun développement dessus |

## 9. Outillage fourni

- `scripts/world-migration/scan_world_mod_content.py` — inventaire du contenu mod dans un monde (blocs/entités)
- `scripts/world-migration/locate_builds.py` — détection et regroupement des zones construites
  (sortie : bounding boxes X/Z prêtes pour `//pos1 //pos2`)

## 10. Décisions restantes

1. Valider la liste des zones à exporter (les 173 clusters, ou seulement les ~20 à signature joueur ?).
2. Trancher sur l'intégration claims en 1.21.1 (Open Parties and Claims ?) ou aucune.
3. Nom du nouveau dépôt : `voiddynasty-neoforge` (recommandé) vs branche `neoforge` ici.
4. Sort de `voiddynasty-logistics` : archivé en référence RT, ou supprimé.
