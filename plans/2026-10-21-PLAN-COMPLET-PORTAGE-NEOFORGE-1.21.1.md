# PLAN COMPLET DE PORTAGE — VoidDynasty → NeoForge 1.21.1

> Document d'exécution. Remplace `plans/2026-10-21-PLAN-REBUILD-NEOFORGE-1.21.1.md` (v1, décisionnel).
> Toutes les versions, volumétries et coordonnées de ce document ont été **mesurées ou vérifiées** le 2026-10-21.

## 0. Décisions actées

| Sujet | Décision |
|---|---|
| Cible | **NeoForge 21.1.264 / Minecraft 1.21.1 / Java 21** |
| Map | **transplantée par schematics WorldEdit** (les 173 clusters détectés) |
| Périmètre | **greenfield sélectif** : cœur gameplay uniquement, décoratif reporté |
| Pont Rogue Trader | **abandonné** ; module `voiddynasty-logistics` **supprimé** |
| Claims | **Open Parties and Claims** (neoforge-1.21.1-0.33.0) |
| Git | branche **`neoforge`** dans `Donopot/Mod-minecraft` |
| GeckoLib | **4.9.3** (1.21.1 neoforge) |

---

## 1. Cible technique et modpack

### 1.1 Chaîne de build

| Composant | Version | Vérification |
|---|---|---|
| NeoForge | **21.1.264** | 259 builds 21.1.x sur `maven.neoforged.net` |
| Minecraft | 1.21.1 | exigée par Create 6.0.10 et TheWarhammerMod 1.6.0 |
| Java | 21 | toolchain du projet |
| ModDevGradle | **2.0.148** | dernière version publiée |
| GeckoLib | **4.9.3** | 27 builds 1.21.1 neoforge ; nos 33 `.geo.json` (`format_version 1.12.0`) et 31 animations (`1.8.0`) sont compatibles |

### 1.2 Modpack serveur 1.21.1 (à installer)

| Mod | Version 1.21.1 | Source | Rôle |
|---|---|---|---|
| `neoforge` | 21.1.264 | maven NeoForged | loader |
| **VoidDynasty** | `1.3.0-alpha.1-neoforge` | ce dépôt | le mod porté |
| `geckolib` | **4.9.3** | Modrinth | animations (dépendance dure) |
| `create` | **6.0.10** | jar fourni ✅ (déjà 1.21.1) | machines/automation |
| `flywheel` | **1.0.x** | **à récupérer** (absent de Modrinth, maven Create) | requis par Create |
| `ponder` | 1.21.1-2.4.0+neoforge | Modrinth | requis par Create |
| `thewarhammermod` | **1.6.0** | jar fourni ✅ (déjà 1.21.1) | contenu Warhammer |
| `waystones` | 21.1.46+neoforge-1.21.1 | Modrinth | téléportation |
| `balm` | 21.0.66+neoforge-1.21.1 | Modrinth | dépendance Waystones |
| `openpartiesandclaims` | neoforge-1.21.1-0.33.0 | Modrinth | claims + intégration |
| `worldedit` | **7.3.8** | Modrinth | collage des schematics |

⚠️ **Jars fournis mais pour 26.3 — à NE PAS mettre sur le serveur 1.21.1** :
`waystones-26.3.0.1`, `xaerominimap-26.6.1`, `worldedit-7.4.6`, `sodium-0.9.3-beta.1`, `iris-1.11.7mc26.3`.
Ils servent uniquement au **serveur d'extraction 26.3** (§4).

### 1.3 Modpack client 1.21.1

| Mod | Version | Note |
|---|---|---|
| `sodium` | **mc1.21.1-0.8.13-neoforge** | rendu |
| `iris` | **1.8.14-beta.1+1.21.1-neoforge** | shaders (exige sodium) |
| `xaeros-minimap` | **neoforge-1.21.1-26.6.0** | carte |
| `xaeros-world-map` | **neoforge-1.21.1-1.47.0** | carte plein écran |
| `xaerolib` | **introuvable sur Modrinth en 1.21.1** | à vérifier : soit bundlé dans le minimap 1.21.1, soit à prendre sur CurseForge. **Bloquant si absent.** |
| `openpartiesandclaims` | 0.33.0 | affichage des claims |
| `geckolib` | 4.9.3 | identique serveur |

⚠️ `shogi` n'a **aucune build 1.21.1** : la version 1.21.1 de Waystones ne le requiert pas (contrairement au build 26.3 fourni).

---

## 2. Inventaire de l'existant

| Module | Nature | Fichiers `.java` | LOC |
|---|---|---|---|
| `voiddynasty` | mod principal (serveur + client) | 227 main + 14 test | 30 113 (avec tests) |
| `voiddynasty-bridge` | module client compilé **dans** le jar `voiddynasty` via `sourceSets` | 31 | ~3 184 |
| `voiddynasty-logistics` | **pas un mod** : docs/protocole/adaptateurs RT | 0 | 0 |
| `onepiece` (`opworldlike`) | autre mod, hors périmètre | 353 | 64 715 |

**Contenu déclaré** (compté dans les registres) :
- **45 blocs** (`ModBlocks`) — 9 ceramite blocks, 2 minerais, 4 fourneaux promethium, `mining_rig`,
  `super_chest`, `super_container`, crates, gothic, corpses 1-6, décor Warhammer…
- **75 items** (`ModItems`) — 13 armes, 9 munitions, matériaux (ceramite ×9, adamantium, titanium,
  silicon_carbide, wires, promethium, focus_lens, plasma_coil, warhead, wax_seal)
- **11 items d'armure** (`ModArmor`) — space marine + terminator (4 pièces chacun + 2 « set »)
- **36 entités** (`ModEntities`) — 32 mobs + `bolt_projectile`, `laser_beam`, `projectile_chaos`, + 1 entrée technique
- **3 BlockEntities** — `mining_rig`, `super_chest`, `super_container`
- **1 menu** — `super_chest`
- **13 sons** — `item.<arme>.fire`, `item.melee.swing`, `item.dry_fire`, `item.reload`
- **3 DataComponents** — `voiddynasty:ammo`, `voiddynasty:recoil`, `voiddynasty:reloading`
- **1 onglet créatif** — `main`

**Assets** : 578 JSON, 203 PNG, 64 fichiers GeckoLib (33 modèles + 31 animations), 13 OGG.

---

## 3. Contrat de compatibilité (non négociable)

Extrait du scan du monde `data-263` (60 670 chunks) : la map ne dépend que de **6 blocs machines**,
**1 entité** et **2 items**.

| Élément | ID | Pourquoi | Traitement |
|---|---|---|---|
| Bloc + BE | `voiddynasty:super_container` (13 occ.) | inventaire dans le NBT | ID identique + clés NBT identiques |
| Bloc + BE | `voiddynasty:super_chest` (5 occ.) | inventaire | idem |
| Bloc + BE | `voiddynasty:promethium_furnace` | progression de fonte | idem |
| Bloc + BE | `voiddynasty:promethium_blast_furnace` | idem | idem |
| Bloc + BE | `voiddynasty:promethium_smoker` | idem | idem |
| Bloc + BE | `voiddynasty:promethium_brewing_stand` (2 occ.) | idem | idem |
| Entité | `voiddynasty:servitor_2` (1 occ.) | NBT du cerveau | ID + tolérance NBT ancien |
| Item | `voiddynasty:control_pad` | inventaire joueur | ID + composants |
| Item | `voiddynasty:laser_drill` | inventaire joueur | ID + composants |
| Minerais | `ceramite_ore`, `deepslate_ceramite_ore` (715 occ.) | terrain | **non transplantés** : régénérés par le worldgen 1.21.1 |

Annexe machine-lisible : `plans/neoforge-1.21.1/annexe-contrat-ids.json` (tous les IDs par registre).

**Clés NBT** : les BE utilisent en 26.3 l'API `ValueOutput`/`ValueInput`
(`saveAdditional(ValueOutput)`, `loadAdditional(ValueInput)`). En 1.21.1 l'API est
`saveAdditional(CompoundTag, HolderLookup.Provider)`. Les **clés** doivent être relevées à l'identique
en P0 (lecture de `AbstractSuperContainerBlockEntity`, `MiningRigBlockEntity`, `SuperChestMenu`) puis
réimplémentées telles quelles, sinon les inventaires existants seront perdus au collage des schematics.

---

## 4. Map : extraction et transplantation

### 4.1 Ce que le scan a trouvé

- **2 161 chunks construits**, regroupés en **173 clusters** (§ annexe `annexe-zones-export.csv`,
  colonnes : `zone, chunks, x1, x2, z1, z2, y1, y2, size_x, size_z, blocs_dominants`).
- Détection = palettes de blocs **posés par un joueur** (planches, verre, béton, cuivre, coffres, rails,
  tuff bricks…), **terracotta exclue** (badlands = faux positif naturel), seuil 10 blocs distincts/chunk.
- Zones à signature `chest / dispenser / copper_stairs / tuff_brick` = bases joueur.
  Zones dominées par `oak_fence` seul = villages générés (conservées dans le CSV par décision
  « exporter les 173 », mais leur collage est facultatif : elles réapparaîtront naturellement).

### 4.2 Extraction (serveur 26.3 actuel, WorldEdit **7.4.6**)

Pour chaque ligne du CSV, en jeu (op) ou via un joueur téléporté :

```
# 1. se téléporter au centre de la zone
/tp <(x1+x2)/2> 100 <(z1+z2)/2>

# 2. sélection (y1=-64, y2=320 : toute la hauteur, ne rien couper)
//pos1 <x1> -64 <z1>
//pos2 <x2> 320 <z2>

# 3. copie AVEC entités et biomes
//copy -e -b

# 4. sauvegarde en Sponge v3 (lisible par WorldEdit 7.3.8)
//schem save zone_001 sponge
```

- Les 173 exports doivent être nommés exactement comme la colonne `zone`.
- Contrôle : `//schem list` doit lister 173 entrées ; taille de chaque fichier cohérente avec
  `size_x × size_z`.
- Alternative automatisée (si le volume in-game est trop long) : génération des `.schem` en Python
  depuis les `.mca` (palette NBT → Sponge v3). Réservé en secours : mapping blockstate 26.3 → 1.21.1
  à valider bloc par bloc.

### 4.3 Collage (serveur 1.21.1, WorldEdit **7.3.8**)

```
//schem load zone_001
//paste -e -b
```

- Les blocs vanilla survivent tels quels.
- Les blocs VoidDynasty survivent **si le mod est chargé avec les mêmes IDs** (contrat §3).
- Les inventaires `super_chest`/`super_container` voyagent dans le NBT du schematic.
- Ordre imposé : coller **après** P4 (machines opérationnelles), sinon les BE seront vidés.

### 4.4 Sauvegardes obligatoires

- `~/minecraft-server/backup-pre-neoforge-20261021T080702.tar.gz` (451 Mo) existe déjà.
- Avant toute bascule : nouvelle archive du monde 26.3 + `sha256`.
- **Interdiction** d'ouvrir le monde de production avec un mod incomplet (les blocs inconnus sont
  supprimés à la sauvegarde).

---

## 5. Correspondance loader : Fabric 26.3 → NeoForge 1.21.1

24 points d'API recensés dans le code actuel.

| Fabric (26.3) | NeoForge 1.21.1 | Fichiers concernés |
|---|---|---|
| `ModInitializer` / `ClientModInitializer` | `@Mod("voiddynasty")` + ctor `(IEventBus, ModContainer)` ; `@EventBusSubscriber(value=Dist.CLIENT, bus=MOD)` | `VoidDynastyMod`, `VoidDynastyModClient` |
| `CommandRegistrationCallback` | `RegisterCommandsEvent` | 12 |
| `ServerTickEvents.END_SERVER_TICK` | `ServerTickEvent.Post` | 8 |
| `ServerLifecycleEvents.SERVER_STARTED/STOPPED` | `ServerStartedEvent` / `ServerStoppedEvent` | 5 |
| `ClientTickEvents.END_CLIENT_TICK` | `ClientTickEvent.Post` | 5 |
| `PayloadTypeRegistry` | `RegisterPayloadHandlersEvent` + `PayloadRegistrar` | `network/ModNetworking` |
| `ServerPlayNetworking` / `ClientPlayNetworking` | handlers du registrar + `PacketDistributor.sendToServer/sendToPlayer` | 6 |
| `EntityRendererRegistry` | `EntityRenderersEvent.RegisterRenderers` | `VoidDynastyModClient` |
| `HudElementRegistry` | `RegisterGuiLayersEvent` (NeoForge 21.1 ; repli `RenderGuiEvent.Post` si absent) | `BolterHud` |
| `KeyMappingHelper` | `RegisterKeyMappingsEvent` | `BolterKeybindings` |
| `FabricDefaultAttributeRegistry` | `EntityAttributeCreationEvent` | `VoidDynastyMod` |
| `ServerLivingEntityEvents.AFTER_DEATH` | `LivingDeathEvent` | `WorkService`, `BrainBags` |
| `ServerEntityLevelChangeEvents` | `EntityTravelToDimensionEvent` (+ leave/join level) | `BrainBags` |
| `ServerMessageEvents.CHAT_MESSAGE` | `ServerChatEvent` | `ai/ChatListener` |
| `PlayerBlockBreakEvents.Before` | `BlockEvent.BreakEvent` | `WorkService` |
| `BiomeModifications` / `BiomeSelectors` | datapack `data/voiddynasty/neoforge/biome_modifier/*.json` (`neoforge:add_features`) | `worldgen/ModWorldGen` |
| `FabricLoader.isModLoaded` | `ModList.get().isLoaded("openpartiesandclaims")` | `compat/ClaimIntegration` |
| `FabricLoader.getConfigDir()` | `FMLPaths.CONFIGDIR.get()` | `ai/BrainConfig`, `ai/BrainNotes`, `BolterKeybindings` |
| `ResourceManagerHelper` (resource pack) | `AddPackFindersEvent` | `VoidDynastyMod` |
| `MenuScreens.register` (access widener) | `RegisterMenuScreensEvent` | `VoidDynastyModClient` |
| `fabric.mod.json` | `META-INF/neoforge.mods.toml` | ressources |
| `fabric-loader` / `fabric-api` | `ModDevGradle` + `neo_version` | build |
| Access widener (5 lignes) | **supprimé** (`MenuType` via DeferredRegister, menus via `RegisterMenuScreensEvent`) | — |
| Mixin (2 classes) | conservé : Mixin est natif NeoForge (`neoforge.mods.toml` → `[[mixins]]`) | `mixin/*` |

---

## 6. Correspondance vanilla : API 26.3 → 1.21.1

| Sujet | 26.3 (code actuel) | 1.21.1 (cible) |
|---|---|---|
| Identifiants | `net.minecraft.resources.Identifier` | `net.minecraft.resources.ResourceLocation` (`fromNamespaceAndPath`) |
| Sérialisation BE | `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)` | `saveAdditional(CompoundTag, HolderLookup.Provider)` / `loadAdditional(CompoundTag, HolderLookup.Provider)` |
| Rendu HUD/écran | pipeline « render states » (`FabricRenderState`, `RenderStateDataKey`, `LevelExtractionEvents`, `LevelRenderEvents`) | `GuiGraphics`, `PoseStack`, `RenderLevelStageEvent` — **plus simple** |
| Rendu entités | `EntityRenderer` + state extraction | `EntityRenderer<T, S>` classique / `GeoEntityRenderer` (GeckoLib 4.9.3) |
| Registres | `Registry.register` direct (102 appels) | **`DeferredRegister`** obligatoire (registres gelés) |
| Composants d'item | `DataComponents` custom (3) | identique conceptuellement ; `DeferredRegister<DataComponentType<?>>` |
| Menus | `MenuType` + access widener | `DeferredRegister<MenuType<?>>` + `IMenuTypeExtension.create` |
| Réseau | `CustomPacketPayload` + `StreamCodec` (Fabric) | `CustomPacketPayload` + `StreamCodec` + registrar NeoForge |
| Config | JSON maison (`voiddynasty-servitor.json`) | `ModConfigSpec` avec migration du fichier existant |
| Worldgen | `BiomeModifications` (code) | biome modifier JSON |
| Java | 25 | 21 |

---

## 7. Architecture du nouveau module

Dans ce dépôt, **branche `neoforge`**, répertoire `voiddynasty-neoforge/` (le module Fabric reste intact
pour référence jusqu'à la bascule).

```
voiddynasty-neoforge/
├── settings.gradle
├── build.gradle
├── gradle.properties
└── src/main/
    ├── java/com/donopot/voiddynasty/
    │   ├── VoidDynasty.java              @Mod, ctor(eventBus, modContainer), enregistrement DeferredRegister
    │   ├── VoidDynastyClient.java        @EventBusSubscriber(client) : renderers, layers, keybinds, screens
    │   ├── registry/
    │   │   ├── ModItems.java             DeferredRegister.Items
    │   │   ├── ModBlocks.java            DeferredRegister.Blocks
    │   │   ├── ModBlockEntities.java     DeferredRegister<BlockEntityType<?>>
    │   │   ├── ModEntities.java          DeferredRegister<EntityType<?>>
    │   │   ├── ModMenus.java             DeferredRegister<MenuType<?>>
    │   │   ├── ModSounds.java            DeferredRegister<SoundEvent>
    │   │   ├── ModTabs.java              DeferredRegister<CreativeModeTab>
    │   │   └── ModDataComponents.java    DeferredRegister<DataComponentType<?>>
    │   ├── content/
    │   │   ├── machine/                  super_chest, super_container, mining_rig, 4 fourneaux promethium
    │   │   ├── weapon/                   WeaponDefinition(s), WeaponItem, DamageCalculator, projectiles
    │   │   ├── servitor/                 Servitor1/2/3, Brain* (LLM), work (mine/craft/smelt/supply/mission)
    │   │   ├── fleet/                    FleetManager, ShipData, ControlPad
    │   │   └── compat/                   ClaimIntegration (OPAC)
    │   ├── network/                      Payloads + StreamCodec + handlers
    │   ├── client/                       HUD bolter, keybinds, renderers GeckoLib, faisceau laser
    │   ├── data/                         datagen providers (models, blockstates, items, recipes, loot, tags, lang)
    │   └── mixin/                        BlockEntityTypeMixin, AvatarDrillPoseMixin
    └── resources/
        ├── META-INF/neoforge.mods.toml
        ├── assets/voiddynasty/           textures + geckolib models/animations (copiés) + sounds.json
        └── data/voiddynasty/             recettes/loot générés + neoforge/biome_modifier/*.json
```

### 7.1 `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
org.gradle.configuration-cache=true
minecraft_version=1.21.1
neo_version=21.1.264
mod_id=voiddynasty
mod_name=Void Dynasty
mod_version=1.3.0-alpha.1-neoforge
mod_group_id=com.donopot
```

### 7.2 `build.gradle` (extrait)

```groovy
plugins {
    id 'java-library'
    id 'net.neoforged.moddev' version '2.0.148'
}

neoForge {
    version = project.neo_version
    validateAccessTransformers = true
    runs {
        client { client() }
        server { server() }
        gameTestServer { type = 'gameTestServer' }
    }
    mods { voiddynasty { sourceSet sourceSets.main } }
}

repositories { maven { name = 'GeckoLib'; url = 'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/' } }

dependencies {
    implementation "software.bernie.geckolib:geckolib-neoforge-${minecraft_version}:4.9.3"
}

tasks.withType(JavaCompile).configureEach { options.release = 21 }
```

### 7.3 `META-INF/neoforge.mods.toml` (extrait)

```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "MIT"

[[mods]]
modId = "voiddynasty"
version = "${mod_version}"
displayName = "Void Dynasty"

[[dependencies.voiddynasty]]
modId = "neoforge"
type = "required"
versionRange = "[21.1.190,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.voiddynasty]]
modId = "minecraft"
type = "required"
versionRange = "[1.21.1]"
ordering = "NONE"
side = "BOTH"

[[dependencies.voiddynasty]]
modId = "geckolib"
type = "required"
versionRange = "[4.9.3,)"
ordering = "NONE"
side = "BOTH"

[[mixins]]
config = "voiddynasty.mixins.json"
```

### 7.4 Exemple de registre (modèle à généraliser)

```java
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("voiddynasty");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "voiddynasty");

    public static final DeferredBlock<Block> SUPER_CHEST = BLOCKS.registerBlock("super_chest",
            SuperChestBlock::new, BlockBehaviour.Properties.of().strength(3.0f).noOcclusion());

    public static void register(IEventBus bus) { BLOCKS.register(bus); BLOCK_ENTITIES.register(bus); }
}
```

### 7.5 Exemple de payload (réseau)

```java
public record ScrollContainerPayload(int containerId, int dy) implements CustomPacketPayload {
    public static final Type<ScrollContainerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("voiddynasty", "scroll_container"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollContainerPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ScrollContainerPayload::containerId,
                                  ByteBufCodecs.VAR_INT, ScrollContainerPayload::dy, ScrollContainerPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

// enregistrement
@SubscribeEvent
static void register(RegisterPayloadHandlersEvent e) {
    var r = e.registrar("1");
    r.playToServer(ScrollContainerPayload.TYPE, ScrollContainerPayload.CODEC, ScrollHandler::handle);
}
```

---

## 8. Disposition du code existant

Annexe complète : `plans/neoforge-1.21.1/annexe-fichiers.csv` (272 fichiers, colonnes
`module, fichier, loc`). Synthèse :

| Catégorie | Fichiers | Traitement |
|---|---|---|
| `registry/*` (8) | 1 025 LOC | **réécrire** en DeferredRegister |
| `machine/*` (16) | cœur | **porter** (ID + NBT identiques) |
| `weapon/*` (10) | cœur | **porter** (WeaponItem + DataComponents) |
| `menus/*`, `component/*` | petit | **porter** |
| `ai/*` (20) + `work/*` (18) | cœur | **porter** (BrainLlm inchangé, HTTP LM Studio) |
| `entity/Servitor1..3*` | 3 | **porter** avec GeckoLib 4.9.3 |
| `entity/*` (30 autres mobs) | gros | **reporter** (phase 2) |
| `client/*` (72) | mixte | **porter** : HUD, keybinds, servitors, lasers / **reporter** : modèles-renderers des 30 mobs |
| `campagne/*` (7), `terrain/*` (3) | 10 | **reporter** |
| `voiddynasty-bridge/*` (31) | 3 184 LOC | **abandonner** (RT) sauf logique pad/laser/refit à reverser |
| `src/test/*` (14) | harnais Fabric (`Knot`, `FabricLoader`) | **réécrire** en gametests NeoForge |

### 8.1 Fichiers du bridge à récupérer avant suppression
- `refit/FleetPadScreen.java` (262 LOC), `refit/PagedContainer.java` (161), `refit/PageSlot.java`,
  `refit/PageWord.java`, `refit/PadLayout.java` → **pad de flotte** (gameplay conservé)
- `refit/FixedLaserRenderer.java` (84), `refit/LaserRay.java`, `refit/LaserShot.java`,
  `refit/ImpactEffects.java`, `refit/BeamData.java` → **effets laser/plasma**
- `refit/mixin/SupplyChestScreen.java` (178), `SuperChestMenuMixin.java` (91),
  `WeaponServiceMixin.java` (62), `BoltProjectileMixin.java` (61), `LaserBeamMixin.java` (60),
  `ControlPadMixin.java` (29), `ClientRegistrationMixin.java` (28) → correctifs à réintégrer proprement
- **Abandonnés** : `DeliveryService` (689), `DiagnosticConnection` (237), `CargoBlockEntity` (182),
  `DeliveryScreen` (148), `BridgeWorld` (137), `SetupScreen` (112), `DeliveryWire` (112),
  `DeliveryReconciliation` (65), `DeliveryInteraction` (58), `BridgeMod` (56), `CargoCatalog` (25),
  `CargoBlock` (33), `VoidDynastyBridgeClient` (93)

---

## 9. Contenu à réimplémenter (greenfield sélectif)

### 9.1 Machines & conteneurs (P4)
`super_chest`, `super_container` (+ `AbstractSuperContainerBlockEntity`, `AggregatedContainer`),
`mining_rig`, `promethium_furnace/blast_furnace/smoker/brewing_stand`, `SuperChestMenu`.
Point dur : **clés NBT identiques** aux BE existants, sinon les 20 blocs déjà posés perdent
leur inventaire au collage.

### 9.2 Armes & projectiles (P5)
`WeaponDefinition`/`WeaponDefinitions` (table de données), `WeaponItem` + 3 DataComponents
(`ammo`, `recoil`, `reloading`), `DamageCalculator`, `FireMode`, `EffectType`, projectiles
`BoltProjectile`, `ProjectileChaos`, `LaserBeamEntity`. 13 armes, 9 munitions.

### 9.3 Servitors, IA et travail (P6-P7)
`BrainLlm` (client HTTP vers LM Studio `http://localhost:1234/v1`, clé via env
`VOIDDYNASTY_LLM_KEY`) — **aucune dépendance MC, à porter quasi tel quel**.
`BrainConfig` (config NeoForge, migration depuis `config/voiddynasty-servitor.json`),
`BrainOrders`, `BrainSenses`, `BrainRoutine`, `BrainBags`, `BrainSpawn`, `BrainNotes`,
`TaskRunner` + `WorkService`, `SupplyService`, `SmeltService`, `CraftService`, `BuildService`,
`StairMining`, `WorkDepot`, `WorkChunkTickets`, `MissionService`.

### 9.4 Flotte (P8)
`FleetManager` (lit des ressources `.nbt` du jar), `FleetState`, `ShipData` (blocs gzippés en mémoire),
`ControlPadItem`/`ControlPadScreen`, `FleetListWindow`, commandes `FleetCommand`, `CruiserCommand`,
`FrigateCommand`. Cible : `SavedData` NeoForge pour l'état persistant.

### 9.5 Client (P9)
`BolterHud` (`RegisterGuiLayersEvent`), `BolterKeybindings` (`RegisterKeyMappingsEvent`),
`BoltProjectileRenderer`, `LaserDrillBeamRenderer` (réécriture en `RenderLevelStageEvent` + `PoseStack`),
`SuperChestScreen`/`ControlPadScreen` (`RegisterMenuScreensEvent`), renderers GeckoLib des servitors.

---

## 10. Phases d'exécution

Chaque phase a un critère PASS vérifiable. Aucune phase suivante ne démarre sans PASS.

### P0 — Cadastre et gel du contrat (0,5 session)
Tâches :
1. Extraire les IDs exacts par registre (déjà fait : `annexe-contrat-ids.json`).
2. Relever les **clés NBT** de `AbstractSuperContainerBlockEntity`, `MiningRigBlockEntity`, `SuperChestMenu`.
3. Geler `plans/neoforge-1.21.1/contrat-ids.json` (versionné).
4. Écrire le gametest `ContratIdsTest` (assertion : chaque ID du contrat résout).
**PASS** : fichier de contrat versionné + gametest compilé (il échouera jusqu'à P3, c'est voulu).

### P1 — Extraction des 173 zones (2-3 sessions)
Serveur 26.3 + WorldEdit 7.4.6, procédure §4.2, une passe par ligne du CSV.
**PASS** : `//schem list` = 173 entrées ; contrôle visuel de 5 zones majeures (avant/après screenshot).

### P2 — Squelette NeoForge 1.21.1 (0,5 session)
ModDevGradle + `@Mod` + `neoforge.mods.toml`, aucun contenu.
**PASS** : `./gradlew runServer` démarre sur monde neuf, mod listé, `/neoforge tps` répond.

### P3 — Registres sélectifs (2-3 sessions)
DeferredRegister pour 7 blocs, 3 BE, 5 entités, items du cœur, 1 menu, 13 sons, 1 onglet, 3 composants.
**PASS** : `/setblock ~ ~ ~ voiddynasty:super_chest`, `/summon voiddynasty:servitor_2`, `ContratIdsTest` vert.

### P4 — Assets + datagen (2-3 sessions)
Copie textures/geo/anim (GeckoLib 4.9.3), datagen complet, biome modifiers pour les 2 minerais.
**PASS** : blocs texturés en jeu, recettes/loot présents, minerais générés dans un monde neuf (`/locate`).

### P5 — Machines et menus (3-4 sessions)
**PASS** : ouvrir `super_chest`/`super_container`, déposer/retirer des items, NBT relu après redémarrage.

### P6 — Armes et projectiles (3-4 sessions)
**PASS** : tir réel avec les 13 armes, consommation des 9 munitions, dégâts conformes à `DamageCalculator`,
rechargement via DataComponent.

### P7 — Servitors : IA et travail (4-5 sessions)
**PASS** : mission complète (récolte → dépôt coffre) avec LLM local, reprise après redémarrage,
`WorkChunkTickets` libérés, aucune fuite de chunks (`/forceload query` stable).

### P8 — Flotte (3-4 sessions)
**PASS** : déployer un vaisseau (blocs posés), le ranger (`storedBytes`), le redéployer à l'identique.

### P9 — Client (3-4 sessions)
**PASS** : HUD bolter visible avec **Iris + Sodium actifs**, keybinds enregistrés, faisceau laser visible.

### P10 — Collage de la map (2 sessions)
Serveur 1.21.1 + WorldEdit 7.3.8, `/schem load` + `/paste -e -b` des 173 zones.
**PASS** : zones conformes (comparaison screenshots), 20 conteneurs avec inventaires intacts,
1 servitor présent.

### P11 — Modpack complet et bascule (1-2 sessions)
Installer Create 6.0.10 + flywheel + ponder, TheWarhammerMod 1.6.0, Waystones + Balm, OPAC.
**PASS** : serveur stable 30 min, 0 crash, `/neoforge tps` > 19, OPAC protège une zone,
Create fonctionne, Warhammer Mod charge ses entités.

### P12 — Nettoyage et release (0,5 session)
Geler le module Fabric, supprimer `voiddynasty-logistics` (fait sur cette branche), tag
`neoforge-v1.3.0-alpha.1`, publication du jar.

**Estimation** : P0-P5 ≈ 9-12 sessions · P6-P9 ≈ 13-17 · P10-P12 ≈ 4-6. Total ≈ **26-35 sessions**
(≈ 3-4 semaines à plein temps, à recalibrer après P5).

---

## 11. Tests et CI

| Niveau | Contenu | Commande |
|---|---|---|
| Build | compilation Java 21 + datagen | `./gradlew build` |
| Gametests | contrat d'IDs, enregistrement, recettes, NBT des BE | `./gradlew runGameTestServer` |
| Test de monde | démarrage sur copie du monde 1.21.1 + collage | manuel, script `scripts/world-migration/` |
| Client | HUD/keybinds/laser avec Sodium+Iris | `./gradlew runClient` |

Les 14 anciens tests Fabric (`Knot`, `FabricLoader`) sont **inutilisables** : réécriture en gametests.
`WorkConservationTest` (logique pure, sans MC) est portable tel quel en test JUnit.

---

## 12. Git

- Branche : **`neoforge`** (créée depuis `main`).
- `voiddynasty-logistics` **supprimé sur cette branche** (récupérable depuis `main`).
- Commits attendus, un par phase : `feat(neoforge): P3 registres sélectifs`, etc.
- `main` reste la référence Fabric `alpha.94` jusqu'à P11 ; aucun développement Fabric supplémentaire.
- Fin de P11 : PR `neoforge` → `main`, puis tag.

---

## 13. Risques et parades

| Risque | Impact | Parade |
|---|---|---|
| Perte d'inventaires au collage | élevé | clés NBT gelées en P0, test explicite P5 puis P10 |
| `xaerolib` introuvable en 1.21.1 | client seulement | vérifier bundle ; sinon CurseForge ; non bloquant serveur |
| `flywheel` 1.21.1 absent de Modrinth | serveur | maven Create/CurseForge ; vérifier compat avant P11 |
| Rendu laser avec Iris | moyen | `RenderLevelStageEvent` + render types vanilla (pas de shader custom) |
| 173 zones = volume d'export long | planning | extraction par lots de 20, checkpoint dans le CSV |
| Réécriture vanilla 26.3 → 1.21.1 | élevé | périmètre sélectif + code existant comme spécification |
| Différence de génération des minerais | faible | seuils de l'ancien `ModWorldGen` en biome modifier JSON |
| Deux codebases | moyen | gel strict du Fabric après P11 |

---

## 14. Annexes

| Fichier | Contenu |
|---|---|
| `plans/neoforge-1.21.1/annexe-zones-export.csv` | **173 zones** : coordonnées + signature de blocs, prêtes pour `//pos1 //pos2` |
| `plans/neoforge-1.21.1/annexe-fichiers.csv` | **272 fichiers** Java avec leur LOC (base du suivi de portage) |
| `plans/neoforge-1.21.1/annexe-contrat-ids.json` | IDs déclarés par registre (blocs, items, entités, BE, armures) |
| `scripts/world-migration/scan_world_mod_content.py` | inventaire du contenu d'un mod dans un monde |
| `scripts/world-migration/locate_builds.py` | détection + export CSV des zones construites |
