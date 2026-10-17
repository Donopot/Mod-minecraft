# OP World Like — état du portage

**Le mod One Piece pour Minecraft : de Forge 1.20.1 vers Fabric 26.3.**
Document de référence — ce qui a été fait, comment, ce qui est prouvé, ce qui reste ouvert.

*Dernière mise à jour : recette **312/312 PASS**, table d'animations de joueur complétée (151 paires extraites de la source).*

---

## En une page

Le mod est le portage de **deux** projets Forge 1.20.1 : le mod de Fruits du Démon **MinePiece ver15** (entièrement généré par MCreator, ~3 836 classes) et son addon de monde **OP World Like 1.5.2-MP**. Les deux ont été repris en un seul mod Fabric pour Minecraft **26.3**.

| | Couche « Fruits » (MinePiece) | Couche « Monde » (OP World Like) |
|---|---|---|
| Code | **130 classes**, ~35 100 lignes | **106 classes**, ~22 700 lignes |
| Contenu | **40 Fruits du Démon**, **213 techniques**, 82 visuels | génération du monde One Piece, îles, revendications, équipages, spawns, loot, haki, équilibrage |
| Contrats | **40 contrats écrits** (`docs/fruits/`) | configs par Fruit (`balance/`, `yami/`, `pika/`, `quake/`…) |
| Preuves | **312 contrôles automatisés — 312 PASS, 0 FAIL** sur serveur réel | vérifiés par la même recette |

| | |
|---|---|
| **Total** | **236 classes Java, ~57 800 lignes**, 56 paquets |
| **Cible** | Minecraft **26.3** (Mojang mappings), Fabric Loader **0.19.5**, Java **25** |
| **Bibliothèques** | Fabric API 0.162.0+26.3 · GeckoLib 5.5.7 · Player Animation Library 1.2.8 |
| **Assets** | 42 particules + 449 textures · 93 animations de joueur · 267 animations GeckoLib · 123 textures d'objets · 59 textures d'entités · 34 sons |
| **Livrable** | `donopot-devilfruits-client.zip` — **4 jars** à installer ensemble |

**Le principe tenu du début à la fin : aucun contenu inventé.** Toute valeur — dégât, portée, coût, durée, texture, animation, île, coordonnée — vient de la source, lue dans son bytecode ou ses ressources. Quand une donnée n'a pas pu être établie, elle est **signalée** plutôt que comblée.

---

## 1. Le point de départ

La source est double :

1. **MinePiece ver15** (`MinePiece-ver15-forge-1.20.1.jar`) — un mod Forge 1.20.1 généré par **MCreator** : des milliers de classes `…Procedure`, un registre d'objets, des modèles, des textures, des particules. Rien n'y est écrit pour être lu, tout y est écrit pour être compilé.
2. **OP World Like 1.5.2-MP** — l'addon qui donne au monde sa forme One Piece : géographie d'îles, revendications de territoire, équipages, équipement, spawns dirigés.

Le portage a donc pris un parti clair :

- **ne pas traduire en bloc** — le port ne compte que **236 classes contre ~3 836** dans la source ;
- **extraire** les données réelles : décompilation ciblée, lecture de bytecode, inventaire exhaustif des ressources ;
- **reconstruire** les mécanismes sur des idiomes Fabric propres, en conservant les valeurs de la source ;
- **prouver** chaque mécanisme par un contrôle automatisé exécuté sur un serveur réel.

Ce parti explique la forme du dépôt : un **registre déclaratif** des Fruits, une couche de monde
déclarative elle aussi, une couche d'exécution, une couche réseau, une couche cliente, et une
**recette de tests** qui n'est pas décorative.

---

## 2. Versions verrouillées et outillage

| Composant | Version |
|---|---|
| Minecraft | **26.3** (Mojang mappings) |
| Fabric Loader | **0.19.5** |
| Fabric API | **0.162.0+26.3** |
| Loom | 1.18-SNAPSHOT |
| Java | **25** |
| Gradle | 9.7.1 |
| GeckoLib | `geckolib-fabric-26.3-5.5.7.jar` |
| Player Animation Library (PAL) | `playeranimlib-26.3-1.2.8.jar` — id de mod **`player_animation_library`** |

**Mixins déclarés** (`opworldlike.mixins.json`, `required: true`) : `LevelMutationMixin`,
`LivingHurtMixin` (commun) et `AvatarRendererMixin` (client).

**Outils du dépôt** (`tools/`) — tous écrits pour ce portage :

`run_tests.py` (recette complète sur serveur réel piloté par RCON) · `validate_assets.py` ·
`verify_assets_jar.py` · `fruit_audit.py` · `balance_audit.py` · `technique_review.py` ·
`particle_codegen.py`, `particle_port.py`, `particle_wiring.py` (extraction et câblage des particules
depuis le bytecode) · `asset_port.py` · `i18n_gen.py` · `mc_kill_server.py` · scripts de stabilité.

---

## 3. Architecture d'ensemble

```
entrypoint « main »    br.com.oneminepiece.opworldlike.OmpWorld
entrypoint « client »  net.mcreator.minepiece.client.DevilFruitClientInit   (jamais chargé par un serveur)
```

| Paquet | Rôle |
|---|---|
| `opworldlike/` | la couche **monde** : initialisation, règles, portes de fonctionnalité, messages, annonces |
| `opworldlike/worldgen` | géographie, îles, structures, frontière du monde, passages dimensionnels |
| `opworldlike/claim` | revendications d'île, protection, bonus de domination, droits d'accès |
| `opworldlike/crew` · `command` | équipages (persistance, instantanés) et commandes joueur |
| `opworldlike/entity` · `spawn` | spawns dirigés (Calm Belt, Mariejois, Reverse Mountain, boss d'île) et équilibrage des créatures |
| `opworldlike/loot` | tables de loot : équipement, Fruits, multiplicateurs régionaux, coffres d'île importés |
| `opworldlike/haki` · `balance/` · `<fruit>/` | progression du haki et **réglages par Fruit** (yami, pika, quake, sand, magma, mochi, moku, mori, ope, kira, kiro, inu, wolf, imu, hollow, love, daibutsu, zoan, explosion, flame, gravity, flight, kage) |
| `procedures/DevilFruitRegistry` | **registre déclaratif** Fruit → techniques (dégâts, portée, coût, recharge, particule, visuel) |
| `procedures/DevilFruitSkillDispatcher` | exécution : validations, coût, exécution, geste |
| `procedures/DevilFruitBehaviours` | comportements (mêlée, projectile, invocation, domaine, déplacement) |
| `procedures/CombatSkillLogic` · `DevilFruitPowerLogic` | primitives partagées ; suppression (eau de mer, Yami) |
| `procedures/TechniqueAnimations` · `Rokushiki` | table des animations ; art martial indépendant |
| `network/` · `entity/` · `client/` · `init/` | paquets, entités visuelles et invocations, rendu et HUD, registres |

**Deux principes d'architecture tenus :**

1. **La couche cliente est isolée.** L'entrypoint `client` n'est jamais chargé côté serveur, et le code qui appelle une bibliothèque externe vit dans **sa propre classe** (§8) : une bibliothèque absente ne doit pas emporter le reste de l'affichage.
2. **Le serveur valide, le client dessine.** Une technique refusée l'est **avec un message explicite** (aucun Fruit, recharge, énergie, eau de mer, Yami) — jamais en silence.

---

## 4. Le monde : OP World Like

### La géographie

- **Un monde rond, borné** : rayon **64 000 blocs** (`WORLD_RADIUS`), frontière arrondie
  (`RoundedWorldBoundary`) et lissage des jointures inter-régions (`WorldSeamLayout`).
- **Source de biomes dédiée** (`OnePieceBiomeSource`) qui place les **îles canoniques** : Baratie,
  Cactus Island, Alabasta, Little Garden, Jaya, Green Bit, Long Ring Long Land, Water 7 et Enies
  Lobby, Kuraigana, Hachinosu, Thriller Bark, Dressrosa, Whole Cake, Wano, Egghead, Elbaph,
  Lodestar, Tequila Wolf, Amazon Lily, End Gateway.
- **Calm Belt** : biome propre (`opworldlike:calm_belt`) qui ceinture le monde, avec ses créatures.
- **Géographie particulière** : Skypiea, Zou, l'île des hommes-poissons, la Red Line, la Reverse
  Mountain, Mariejois.
- **Un catalogue d'îles** (`CanonicalIslandCatalogV11`, `UniqueIslandCatalog`) et un **installeur de
  régions extraites** de la source (`ExtractedIslandRegionInstaller`, `ExtractedIslandChunkIndex`) :
  les morceaux d'île sont repris de la source, pas redessinés.
- **Structures** : catalogue, types et placeur dédiés (`OmpStructureCatalog`, `OmpStructurePlacer`).
- **Type de dimension** propre (`opworldlike:one_piece_overworld`, lumière ambiante 0) et **preset de
  monde** (`opworldlike:one_piece`) qui remplace l'Overworld.

### Revendications, équipages, droits

- **Revendication d'île** : pose, accès (`IslandAccess`), drapeaux (`IslandFlag`), export de titres,
  cibles de score.
- **Protection** (`GriefProtectionConfig`, `ProtectedSkillEffectGuard`) : les techniques ne
  contournent pas la protection d'un territoire protégé.
- **Bonus de domination** et données de revendication persistantes.
- **Équipages** : création, invitation, exclusion, dissolution, conversation (`CrewCommands`),
  persistance et instantanés (`CrewPersistenceStore`, `CrewSnapshot`).

### Le vivant

- **Spawns dirigés** : Calm Belt, Mariejois, Reverse Mountain, boss d'île, factions faibles
  (`CalmBeltMobSpawner`, `MariejoisMobSpawner`, `ReverseMountainMobSpawner`,
  `MinePieceIslandBossSpawner`, `WeakFactionMobSpawner`), avec leur propre équilibrage.
- **Loot** : catalogue d'équipement, tables de Fruits, multiplicateurs régionaux, coffres d'île
  importés, équipement porté par les créatures du mod.
- **Haki** : progression, événements, réglages (`HakiProgression`, `HakiEvents`, `HakiConfig`).
- **Équilibrage par Fruit** : un paquet de réglages par Fruit — dégâts, explosions, flammes,
  gravité, vol, etc. — avec un plafond global de dégâts appliqué et **tracé**.

### Portes dimensionnelles — porté et non porté, dit franchement

- **Porté** : le **retour** du Nether et de l'End ramène le joueur à l'île-porte correspondante
  (South Blue / North Blue), événement Fabric `ServerEntityLevelChangeEvents`.
- **Non porté** : l'**interdiction** de créer des portails hors des zones prévues, et le **verrou
  d'accès** au Nether et à l'End selon la progression (doriki, haki). Ces deux points dépendent
  d'événements Forge **annulables** qui n'ont pas d'équivalent Fabric : il faut un **mixin**, et la
  logique d'origine est conservée en commentaire pour ce mixin futur. Le dossier est documenté dans
  le code, pas oublié.

### Règles et portes de fonctionnalité

`OmpWorldGameRules` (règles de jeu propres), `OmpFeatureGates` (niveaux d'activation),
`WorldLayoutConfig`, `OmpMessages` (**messages bilingues portugais/anglais**, comme la source),
annonces Discord, `KairosekiBoatHelper`, compatibilité addons et garde anti-récursion de combat.

---

## 5. Le catalogue : 40 Fruits, 213 techniques

Chaque Fruit est déclaré avec ses techniques et **un contrat écrit** dans `docs/fruits/*.md`
(40 fichiers) : techniques, valeurs, mécaniques propres, dette.

**Répartition par tag, lue dans les fichiers de tags :**

| Type | Nombre | Fruits |
|---|--:|---|
| **Logia** | **9** | Mera Mera, Pika Pika, Hie Hie, Yami Yami, Magu Magu, Moku Moku, Goro Goro, Suna Suna, Mori Mori |
| **Zoan** | **12** | Ryu Ryu (Pachycéphalosaurus, Brachiosaurus, Ptéranodon), Inu Inu Wolf, Inu Inu Okuchi no Makami, Neko Neko Leopard, Neko Neko Sabertiger, Batto Batto, Hito Hito Daibutsu, Ushi Ushi Giraffe, Tori Tori Phoenix, Uo Uo Seiryu |
| **Paramecia** | **19** | Aro Aro, Bisu Bisu, Bomu Bomu, Gomu Gomu, Gura Gura, Horo Horo, Imu, Kage Kage, Kira Kira, Kiro Kiro, Mero Mero, Mochi Mochi, Nikyu Nikyu, Ope Ope, Riki Riki, Soru Soru, Supa Supa, Uta Uta, Zushi Zushi |

*13 Fruits sont marqués « transformables » (tag `transformable`).*

| Fruit | Tech. | Fruit | Tech. | Fruit | Tech. | Fruit | Tech. |
|---|--:|---|--:|---|--:|---|--:|
| Mera Mera | 7 | Magu Magu | 5 | Goro Goro | 7 | Hie Hie | 5 |
| Pika Pika | 8 | Yami Yami | 4 | Suna Suna | 5 | Moku Moku | 4 |
| Mori Mori | 4 | Gomu Gomu | 4 | Inu Inu Wolf | 8 | Ushi Ushi Giraffe | 5 |
| Neko Neko Leopard | 7 | Batto Batto | 6 | Hito Hito Daibutsu | 5 | Ryu Ryu Pachycéphalosaurus | 5 |
| Inu Inu Okuchi no Makami | 7 | Tori Tori Phoenix | 6 | Uo Uo Seiryu | 8 | Ryu Ryu Ptéranodon | 3 |
| Ryu Ryu Brachiosaurus | 5 | Neko Neko Sabertiger | 5 | Gura Gura | 6 | Horo Horo | 5 |
| Nikyu Nikyu | 5 | Zushi Zushi | 6 | Bisu Bisu | 4 | Mochi Mochi | 6 |
| Uta Uta | 7 | Imu | 3 | Kira Kira | 5 | Supa Supa | 4 |
| Riki Riki | 5 | Mero Mero | 4 | Kiro Kiro | 4 | Bomu Bomu | 5 |
| Ope Ope | 4 | Aro Aro | 4 | Kage Kage | 6 | Soru Soru | 7 |

**Mécaniques propres portées** — pas seulement des dégâts : domaines à durée (Uta, Ope),
invocations avec IA alliée et ordres (Soru, Bisu, Horo, Mori), états collants gradués et libérables
(Mochi), lames visibles qui découpent certains blocs (Supa), multiplicateur de force **lu sur
l'attribut réel** (Riki), déplacement-flèche qui s'arrête sur l'obstacle (Aro), racines drainantes
bornées (Mori), nuage capturant qui rend la mobilité à la sortie (Smoke).

---

## 6. Systèmes transverses

| Système | État | Contrôles |
|---|---|---|
| **Énergie et recharge** | coût validé **avant** toute dépense ; un refus ne consomme rien | `C1`, `C2` |
| **Éléments et classification** | lue dans les **tags**, jamais déduite ; Logia intangibles, exceptions par source, contre-épreuves | `A2a`–`A5e`, `Z0` |
| **Haki** | niveau persistant, perce les Logia, progression par expérience, haki souverain, **pas de contre universel** | `L0`–`L6` |
| **Kairoseki** | scellement et restitution | `CORE-029` |
| **Eau de mer** | aucun pouvoir, message dédié | `CORE-007` |
| **Scellement par Yami** | cause nommée, restitution à la sortie | `A8b`–`A8d` |
| **Transformations** | forme Hybrid, échelle, retour à la base | `D1`–`D3` |
| **Deux Fruits = mort** | mort réelle si un second Fruit est absorbé | `E1`, `E2` |
| **Mort et Fruit** | libération du Fruit, réapparition par loot | `F1`, `F2` |
| **Unicité du loot** | deux joueurs isolés, un seul obtient le Fruit | `G1`, `G2` |
| **Persistance** | état écrit puis relu après **redémarrage réel** du serveur | `H1`, `persistwrite/verify` |
| **Blocs temporaires** | pose, expiration par le temps, survie au redémarrage puis expiration, restitution | `I1`–`I3`, `mochi_expiry` |
| **Invocations** | plafond, nettoyage, ordres `suivre` / `défendre` / `attaquer` | `I3`, `R2`, `A14` |
| **Réinitialisation** | complète, idempotente, effets externes préservés | `R1`–`R5` |
| **Équilibrage** | plafond de dégâts (60) appliqué et **tracé** | `K1`–`K4` |

---

## 7. Rokushiki — l'art martial, hors Fruit

Dans la source, le Rokushiki **n'appartient à aucun Fruit**. Il a donc été livré comme un **art
apprenable à part** :

- **14 techniques** : shigan, shigan madara, jushigan, tobushigan, rankyaku, rankyaku ran, lupus
  fall, amane dachi, kamie zan shin, okami hajiki, yurushi tene, rokuogan, soru, attack speed ;
- **apprentissage par objet** (Manuel, texture `icon_book.png`) plutôt que par *advancement* — le
  port n'a pas de système d'advancements ;
- **même porte de scellement** que les Fruits : l'art ne permet pas de contourner l'eau de mer ou le
  Yami ;
- mêmes règles de coût, de recharge et de validation que les techniques de Fruit ;
- **un Fruit prime sur l'art** quand le joueur porte les deux.

Preuves : `A11a`–`A11g` — dont « Shigan sans Fruit, énergie 200→160 » et « scellé : refus, énergie
intacte ». Document dédié : `docs/ROKUSHIKI.md`.

---

## 8. Les animations de joueur (PAL)

Le chantier le plus délicat, et celui qui a demandé le plus de corrections.

**Le constat de départ** : en ver15, les animations **de joueur** ne passent pas par GeckoLib. La
source utilise `playeranimator` — aujourd'hui **PAL (Player Animation Library)** — et habille le
joueur avec *Kleiders*. GeckoLib, lui, n'anime que les entités.

**La chaîne, de bout en bout :**

```
technique lancée (serveur)
  → TechniqueAnimations.play(player, move)            choisit le geste
  → paquet minepiece:play_animation                   au lanceur + à ceux qui le suivent
  → DevilFruitAnimationLayer.recevoirAnimation        (client)
  → PlayerAnimationController.triggerAnimation(...)   le geste se pose sur le corps
```

**La table des gestes — l'essentiel du travail :**

- **151 techniques** sur 178 portent la paire `(ANIMATION_1, ANIMATION_2)` **extraite du bytecode**
  de la source, procédure par procédure (chaque technique pose ses codes par `setBaseValue`) ;
- **145** de ces paires se traduisent en une animation **réellement livrée** par le mod ;
- **4** techniques de plus portent le nom de leur animation (`amanedachi` → `amane_dachi`,
  `rokuogan`, `ulzugan`, `ursusshock`) ;
- la **cascade de traduction** de `PlayAnimationPlayerProcedure` est recopiée **branche par branche**
  (codes `0`–`4` pour les attaques de base, puis `-1` à `-49` avec leurs sous-codes) ;
- quand la cascade ne connaît pas un code, le port n'envoie **rien** — exactement ce que fait la
  source, qui teste `if (!ANIME_NAME.isEmpty())` avant de diffuser.

**Trois points de la documentation officielle PAL** qui ont coûté une manche entière :

1. le dossier est `assets/<mod>/player_animations/` — **avec un S** ;
2. la priorité d'une couche d'animation de jeu est **1500+** ;
3. un gestionnaire d'état qui ne joue que du déclenché doit répondre **STOP**, pas CONTINUE.

Document dédié : `docs/ANIMATIONS-JOUEUR.md`.

---

## 9. Particules, entités visuelles, sons

- **42 particules** reprises de la source. Leurs paramètres — durée de vie, gravité, frottement,
  taille, nombre d'images, type de rendu — sont **extraits du bytecode** des classes source : la
  définition JSON ne contient que la liste des images, tout le reste vit dans le code compilé.
  Inventer ces valeurs aurait produit 42 particules approximatives.
- **449 textures de particules**, 22 textures de blocs, 123 textures d'objets, 59 textures
  d'entités, 82 modèles d'objets.
- **3 entités visuelles** : le bras de Gomu (étiré jusqu'à son point d'impact, texture injectée
  depuis l'état de l'entité), le projectile de technique, l'invocation.
- **267 animations GeckoLib** et 35 modèles pour les entités et les formes.
- **34 sons** repris de la source.
- Vérification automatique : `verify_assets_jar.py` contrôle que chaque texture réclamée par une
  définition **existe réellement** dans le jar livré.

---

## 10. Commandes et interface

| Commande | Qui | Effet |
|---|---|---|
| `/devilfruit info` | joueur | Fruit porté, énergie, technique, forme |
| `/devilfruit skill` | joueur | lance la technique **sans la touche** |
| `/devilfruit transform` | joueur | bascule la transformation |
| `/devilfruit debug` | joueur | **témoin de diagnostic** : chaque technique lancée écrit dans le chat son nom, son type et le geste demandé |
| `/devilfruit order` (`suivre` / `défendre` / `attaquer`) | joueur | ordonne à une invocation |
| `/devilfruit give` · `clear` | opérateur | donne un Fruit · efface l'état |
| `/dfvisual` · `/dfsummon` · `/dfblock` | opérateur | outils de vérification visuelle |
| `/dftest …` | opérateur | harnais de la recette |
| `/crew …` | joueur | équipage : `create`, `list`, `info`, `invite`, `accept`, `deny`, `kick`, `leave`, `rename`, `disband`, `message` |
| `/island …` | joueur | île : `info`, `score`, `top`, `tp`, `trust`/`untrust`, `trustcrew`/`untrustcrew`, `flag`, `message`, `mapstats`, `spawninfo`, `rerollspawn` |

**Touches** : `R` technique · `B` cycle de technique · `V` transformation.
**HUD** : Fruit, énergie, technique sélectionnée, forme.

---

## 11. La recette de tests — 312 contrôles

La recette (`tools/run_tests.py`) ne simule rien : elle **démarre un vrai serveur**, y installe le
jar, exécute les scénarios joueur (Fabric `FakePlayer` côté serveur), pilote les commandes par
**RCON**, redémarre pour vérifier la persistance, puis produit un rapport PASS/FAIL dans
`docs/CORE-030-rapport-tests.md`.

| Famille | Objet |
|---|---|
| `S` | démarrage, absence d'erreur, catalogue |
| `A1`–`A5` | catalogue, éléments, classification argumentée, effets |
| `B`, `C` | immersion des 40 Fruits, exécution des techniques, énergie |
| `D`, `E`, `F`, `G`, `H` | transformation · mort par second Fruit · mort et libération · unicité du loot · persistance |
| `I`, `K` | blocs temporaires et invocations · équilibrage |
| `L` | haki : niveau, percée des Logia, progression, souverain |
| `R` | réinitialisation |
| `A6`–`A17` | mécaniques de Fruit : domaines, ROOM, impacts, déviations de projectiles, invocations, lames, domaine rythmique, Rokushiki, animations |

**Résultat consolidé : 312/312 PASS, 0 FAIL** (310 contrôles distincts).

Chaque contrôle porte une **phrase de preuve**, pas un simple OK — par exemple
« poussée devant = 1,5 · derrière = 0,0 · adossée au mur = 0,0 », ou
« paires extraites du bytecode : 151 · techniques avec le geste exact et livré : 145 ».

**Limites assumées du harnais** : l'entité de test (`FakePlayer`) est invulnérable par conception et
n'est pas réellement ajoutée au monde — la mort physique d'un joueur et les équipes scoreboard ne
sont donc **pas** vérifiables ainsi. Ces points sont écrits dans la recette plutôt que contournés.

---

## 12. Ce qui reste ouvert

### Dits franchement

| Sujet | État |
|---|---|
| **Transformations visibles** | le rendu passait par *Kleiders* (bibliothèque Forge sans équivalent Fabric). Les modèles sont livrés et enregistrés, mais **le modèle joueur vanilla n'est pas masqué** sous le modèle de transformation : le défaut se voit à l'écran et n'est pas revendiqué comme réglé. |
| **Forme animale quadrupède complète** | **n'existe pas dans la source pour le joueur** : elle est à *créer*, pas à porter. Décision en attente. |
| **Validation visuelle** | **0 Fruit sur 40** validé à l'œil. Le rendu ne peut pas être vérifié sans écran : à faire en jeu, fruit par fruit. |
| **33 techniques sur 178** | sans paire d'animation extraite → elles emploient l'attaque de base, comme la source pour ses attaques ordinaires. |
| **Verrou Nether/End** | le portail et son interdiction dépendent d'événements Forge annulables : un mixin est nécessaire. Seul le **retour** est porté. |
| **Contenus non portés** | boucliers (3 objets, 6 modèles, valeur de blocage), PNJ combattants, système d'advancements. |
| **Yami Yami** | contrepartie ×2 et coût d'attraction : décision en attente. |
| **Service d'aire commun** | Ope Room / Gravity Field ; service de maintien (racines, état collant, nuage) proposé mais non imposé. |
| **3 Fruits à élément discutable** | `bisu_bisu`, `ryu_ryu_brachiosaurus`, `ryu_ryu_pteranodon`. |

### Enquête en cours

Un signalement resté ouvert : **« le personnage bouge mais on ne voit rien quand les techniques sont
lancées »**. Ce qui a été établi et corrigé en le poursuivant :

- le dossier des animations de joueur n'avait pas le **S** attendu par PAL → **corrigé** (93 fichiers) ;
- la table des gestes ne comptait que **12** entrées → portée à **151** ;
- **PAL n'était pas déclaré** comme dépendance : une PAL absente tuait l'initialiseur client, donc les
  gestes **et** les particules **et** les renderers **et** le HUD → **corrigé** (déclaration, plus
  isolation de la couche dans sa propre classe) ;
- un carré magenta repéré sur une capture d'écran : les 123 textures d'objets et leurs modèles ont été
  vérifiés, **aucune n'est manquante** → la recherche continue, avec un **témoin de diagnostic** dans
  le chat (`/devilfruit debug`) qui nomme désormais ce que le serveur demande réellement.

---

## 13. Corrections notables — ce qui a été appris en chemin

| Problème | Cause réelle | Correctif |
|---|---|---|
| Aucune animation ne jouait | dossier `player_animation` **sans le S** (PAL lit `player_animations`) | 93 fichiers déplacés |
| Gestes génériques | table limitée à 12 entrées | 151 paires extraites du bytecode de la source |
| `javac` plantait au build | bug du compilateur sur une lambda créant un objet dans une lambda de réception | corps sortis en méthodes statiques |
| Extraction faussée | les zones de recherche ramassaient les durées de recharge (`0.01`, `0.25`) | seuls les **entiers** retenus comme codes |
| Geste inféré non prouvé | correspondance `rokuogan → ul_zugan` déduite, sans preuve | retirée |
| Client muet sans erreur | bibliothèque externe absente tuant la classe qui l'appelait | dépendance déclarée + couche isolée |
| Appariement incomplet | la correspondance exigeait l'égalité stricte des noms | nom compact, du plus sûr au plus large |
| `hurtMarked` introuvable | méthode supprimée en 26.3 | remplacée par `push()` |
| Refus silencieux d'une technique | validation lue sur le mauvais objet | lue sur la variable du Fruit |
| Murs de test « infranchissables » (×3) | la sonde échantillonne à mi-hauteur, la logique depuis les pieds | disposition du test corrigée **et rendue capable d'échouer** |

---

## 14. Installer et tester

**Les quatre fichiers `.jar` vont ensemble** dans le dossier `mods` du profil lancé :

```
fabric-api-0.162.0+26.3.jar
geckolib-fabric-26.3-5.5.7.jar
playeranimlib-26.3-1.2.8.jar
opworldlike-1.0.0.jar
```

Trois sur quatre ne suffisent pas : le jeu refusera de démarrer et **nommera** le fichier manquant.

| Lanceur | Où est le dossier |
|---|---|
| Lanceur officiel Mojang | `%appdata%\.minecraft\mods` |
| CurseForge / Prism / MultiMC | dossier `mods` de l'**instance** |

Profil **Fabric 26.3** (Loader 0.19.5 ou plus), **Java 25**.

**Les trois gestes qui répondent à « ça ne marche pas » :**

1. `/devilfruit info` → *commande inconnue* = le mod n'est pas chargé dans ce profil. **C'est la cause.**
2. `/devilfruit debug` puis `R` → une ligne grise dit ce que le serveur a exécuté et quel geste il a
   demandé. Rien dans le chat = l'appui touche n'atteint pas le serveur.
3. `/devilfruit skill` → déclenche sans la touche : sépare « la touche » de « la technique ».

**Pour tester les animations** : les Fruits les mieux couverts sont **Neko Neko Leopard** (7 sur 7),
**Goro Goro** (7 sur 7) et **Pika Pika** (8 sur 8). Éviter Gomu Gomu pour ce test précis
(3 paires sur 4).

---

## 15. Où sont les choses

| Fichier | Contenu |
|---|---|
| `docs/ETAT-DU-PORT.md` | **ce document** |
| `docs/CORE-030-rapport-tests.md` | rapport PASS/FAIL détaillé : 312 contrôles et leurs preuves |
| `docs/CORE-030-recette-de-tests.md` | ce que la recette couvre — et ce qu'elle ne peut pas couvrir |
| `docs/fruits/*.md` | **40 contrats** : techniques, valeurs, mécaniques, dette |
| `docs/ROKUSHIKI.md` | l'art martial : 14 techniques et leurs règles |
| `docs/ANIMATIONS-JOUEUR.md` | la chaîne PAL, la table des gestes, la cascade de traduction |
| `docs/FRUITS-modeles-transformation.md` | modèles de transformation, ce qui reste à valider à l'œil |
| `docs/particules-mapping.md` · `particules-portees.json` | particules : correspondance et portage |
| `docs/assets-portes.json` · `audit-assets.csv` | inventaire des ressources |
| `docs/CORE-0xx-*` | décisions techniques par chantier (rendu, budgets, HUD, haki, langue) |
| `tools/run_tests.py` | la recette, exécutable en une commande |

---

## 16. En résumé

Ce portage n'est pas une traduction : c'est une **reconstruction fidèle et vérifiée** de deux mods
générés, sur une version de Minecraft qui n'existait pas quand ils ont été écrits. Un monde One Piece
avec ses îles et sa géographie, 40 Fruits du Démon, 213 techniques, un art martial hors Fruit, 312
contrôles qui passent, et 40 contrats qui disent pour chaque Fruit ce qui est porté, ce qui est
prouvé et ce qui manque encore.

Ce qui reste ouvert est écrit noir sur blanc : les transformations dont le rendu n'est pas complet, la
forme quadrupède à créer, la validation visuelle à faire de vos yeux, 33 techniques sans geste propre
extrait, le verrou Nether/End qui attend son mixin. Un portage qui annoncerait le contraire serait un
portage qui ment.
