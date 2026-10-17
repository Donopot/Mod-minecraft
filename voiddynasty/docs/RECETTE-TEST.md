# RECETTE DE TEST — VOID DYNASTY

À suivre dans l'ordre. Rien à comprendre, juste à taper et à regarder. Ce que tu me réponds décide de
la suite : six points sont finis côté code mais **personne ne les a encore validés à l'écran**.

---

## 1 · Installer (une fois)

Il te faut **Java 25** et un **profil Fabric 26.3** (Loader 0.19.5 ou plus).

Le mod a besoin de **deux** choses à côté de lui :

| Fichier | Où le mettre |
|---|---|
| `voiddynasty-1.0.0.jar` | dans le dossier `mods` |
| `fabric-api-0.162.0+26.3.jar` | dans le dossier `mods` |
| `geckolib-fabric-26.3-5.5.7.jar` | dans le dossier `mods` |
| `ASTARTES-ARMOURY-26.3.zip` | dans `resourcepacks`, puis à activer dans Options → Ressources |
| `astartes-map` | dans `saves` (si tu veux la campagne) |

C'est **tout** : il n'y a pas de PlayerAnimator ni d'autre dépendance. Le paquet
`VOID-DYNASTY-carte-astartes` contient déjà les cinq.

**Si tu joues sur un monde à toi** (recommandé pour tester les créatures et les armes) : garde
seulement les trois jars dans `mods`, oublie la carte.

---

## 2 · Les six choses que je ne peux pas juger

### ① Le HellDrake — son corps tient-il au reste ?

**Il faut être en créatif** (`/gamemode creative`), puis :

```
/give @s voiddynasty:chaos_helldrake_spawn_egg
```

Pose-le au sol, recule, tourne autour.

> **Ce que je te demande :** est-ce que la tête, le cou et les ailes sont **attachés** au corps, ou
> est-ce que ça flotte détaché ? Mon test par boîtes englobantes dit que le corps est à 0,5–1,8 bloc
> des autres morceaux — mais ce test est grossier et mon rendu n'est pas fiable sur ce modèle. Ton œil
> tranche.

### ② L'échelle des créatures — est-elle crédible ?

```
/give @s voiddynasty:cultist_1_spawn_egg
/give @s voiddynasty:carnifex_spawn_egg
/give @s voiddynasty:daemon_prince_spawn_egg
/give @s voiddynasty:great_unclean_one_spawn_egg
```

Pose-les **côte à côte**, avec toi au milieu.

> **Ce que je te demande :** un cultiste fait **1,7 bloc** (un joueur en fait 1,8), un carnifex 6,2, un
> Prince Démon 6,3, un Grand Immonde 8,3. **Trop gros ? Trop petit ?** Un mot par créature suffit.

### ③ Les cubes plats — voulus ou oubliés ?

Les mêmes créatures, plus `plaguetoad` et `keeper_of_secrets` (les tentacules).

> **Ce que je te demande :** les ailes du HellDrake, du Prince Démon et des démons de Khorne sont des
> **surfaces d'épaisseur nulle** ; le tissu des cultistes aussi ; les tentacules du Gardien des
> Secrets et de l'Émissaire Norn sont un empilement de plaques. **Ça choque ou pas ?** Si ça choque,
> je les épaissis — mais c'est une décision, pas un bug : 99 cubes sont dans ce cas sur 18 modèles.

### ④ Recul et rechargement — la sensation

```
/give @s voiddynasty:boltgun
/give @s voiddynasty:bolt_shell 64
/give @s voiddynasty:heavy_bolter
/give @s voiddynasty:heavy_bolt_shell 64
```

Tire, puis recharge (touche d'utilisation).

> **Ce que je te demande :** au tir, l'arme recule de **2,2 unités** et se relève de **5°** ; au
> rechargement, elle bascule de **32°**. **Trop, pas assez ?** Ce sont des valeurs de départ, jamais
> validées à l'œil.

### ⑤ Trois modèles à 60–67 % de gris

```
/give @s voiddynasty:lictor_spawn_egg
/give @s voiddynasty:tyranid_warrior_spawn_egg
/give @s voiddynasty:norn_emissary_spawn_egg
```

> **Ce que je te demande :** ces trois-là, mon outil les mesure à **60–67 % de pixels gris**. C'est
> peut-être la palette désaturée des Tyranides, peut-être mon éclairage qui aplatit. **Est-ce qu'ils
> ont l'air ternes à l'écran ?**

### ⑥ Ghazghkull — écrasé ?

```
/give @s voiddynasty:ghazghkull_spawn_egg
```

> **Ce que je te demande :** les données disent **4,9 à 6,9 blocs de large pour 2,8 de haut** (bras
> écartés, hache incluse). Ça donne une impression d'écrasement. **Normal ou pas ?**

---

## 3 · Le reste, si tu veux tout voir

### Les 19 créatures du Chaos

Chacune a son œuf : `/give @s voiddynasty:<nom>_spawn_egg`.

| Nom | Identifiant |
|---|---|
| Cultistes | `cultist_1` · `cultist_2` · `cultist_3` · `cultist_4` |
| Prêtresse (boss) | `cultist_boss` |
| Spaces Marines du Chaos | `chaos_marine_1` · `chaos_marine_2` · `chaos_marine_3` |
| Capitaine (boss) | `chaos_captain` |
| Raptor | `chaos_raptor` |
| Machines | `chaos_dreadnought` · `chaos_hellbrute` · `chaos_forgefiend` |
| Servitors | `servitor_1` · `servitor_2` · `servitor_3` |
| Prince Démon (boss) | `daemon_prince` |
| HellDrake (boss) | `chaos_helldrake` |

### Les 15 armes et leurs munitions

```
/give @s voiddynasty:boltgun
/give @s voiddynasty:bolt_shell 64
/give @s voiddynasty:bolt_pistol
/give @s voiddynasty:heavy_bolter
/give @s voiddynasty:heavy_bolt_shell 64
/give @s voiddynasty:stalker_bolt_rifle
/give @s voiddynasty:stalker_bolt_round 64
/give @s voiddynasty:las_gun
/give @s voiddynasty:laz_pistol
/give @s voiddynasty:las_cell 64
/give @s voiddynasty:plasma_pistol
/give @s voiddynasty:plasma_incinerator
/give @s voiddynasty:plasma_cell 64
/give @s voiddynasty:shotgun
/give @s voiddynasty:shotgun_shell 64
/give @s voiddynasty:flamethrower
/give @s voiddynasty:promethium 64
/give @s voiddynasty:melta_gun
/give @s voiddynasty:melta_cell 64
/give @s voiddynasty:hunter_killer_missile
/give @s voiddynasty:missile_ammo 16
/give @s voiddynasty:chainsword
/give @s voiddynasty:powersword
/give @s voiddynasty:power_hammer
```

**Les dégâts de terrain** (ce que les armes cassent) se voient en tirant sur de la pierre, de la
terre, du bois — et sur un mur de **bedrock**, qui doit résister à tout, même au melta.

**Le laser** (fusil et pistolet) doit finir par percer la pierre et **allumer le bois**. Le
lance-flammes allume aussi.

### Les armures

```
/give @s voiddynasty:space_marine_helmet
/give @s voiddynasty:space_marine_chestplate
/give @s voiddynasty:space_marine_leggings
/give @s voiddynasty:space_marine_boots
```

Mets le casque : la vie doit passer de **10 cœurs à 20**. Les quatre pièces Terminator
(`terminator_*`) font pareil, en plus solide.

### La campagne (avec la carte Astartes)

```
/campagne etat
/campagne demarrer
```

Puis va au point d'apparition (380 / 105 / −366) et avance : les zones se déclenchent en approchant,
les portes s'ouvrent sur des compteurs, la barre « Reactor Core » descend de 16 à 0 et la fin doit
s'afficher. `/campagne reset` remet tout à zéro.

⚠️ **Avant de commencer**, colle le forceload (voir `FORCELOAD-A-LIRE.txt` dans le paquet de la
carte) : sans lui, les mécanismes ne tournent que si tu es juste à côté.

### Le minerai de céramite

```
/give @s voiddynasty:ceramite_ore
```

En survie : il se génère **sous terre**, de la couche −64 à 48, en filons de 8 blocs, 4 par chunk.
C'est lui qui donne le **titane** (`/give @s voiddynasty:titanium_ingot` pour vérifier la chaîne) et
donc toute la progression.

### Les machines et les vaisseaux

Dans l'onglet créatif du mod, ou en commande :

```
/give @s voiddynasty:mining_rig
/give @s voiddynasty:ammo_crate
/give @s voiddynasty:crate
/give @s voiddynasty:super_chest
/give @s voiddynasty:super_container
/give @s voiddynasty:promethium_furnace
/give @s voiddynasty:promethium_blast_furnace
/give @s voiddynasty:promethium_smoker
/give @s voiddynasty:promethium_brewing_stand
/give @s voiddynasty:laz_cannon
/give @s voiddynasty:multimelta_turret
/give @s voiddynasty:sandbags
```

Les vaisseaux : `/bridge`, `/hyperjump`, `/fleet`.

### Vérifier que rien n'est cassé, sans me croire

```
/vdyntest all
```

Ça relance les contrôles et écrit le rapport. **Attendu : `32 PASS, 0 FAIL`** — c'est ce que le jeu
répond réellement (éprouvé : les 26 commandes de cette recette ont été passées sur un serveur, et
`/vdyntest all` y répond « 32 PASS, 0 FAIL »). La batterie complète en compte 35 ; les 3 autres se
lancent côté machine avec `python3 tools/server_test_cycle.py`. Si un contrôle passe à `FAIL`, il dit
lequel et pourquoi.

---

## 4 · Ce que je fais de tes réponses

| Ta réponse | Ce que je fais |
|---|---|
| « le HellDrake est détaché » | je reprends sa géométrie et je corrige |
| « les cubes plats choquent » | je les épaissis, mob par mob |
| « le recul est trop fort » | je change une valeur, et je relance la mesure |
| « tout va » | je passe aux deux décisions qui restent (dégâts de terrain en multijoueur, distribution) |

**Une capture d'écran vaut mieux qu'une phrase** si tu peux : `F2` dans le jeu.
