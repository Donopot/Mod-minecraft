# Modèles de transformation — extraction depuis le jar source ver15

Travail demandé par Donopot : « les modèles nécessaires pour les transformations, utilise le skill de
mod minecraft pour les extraire du jar source ».

Méthode appliquée : `minecraft-fabric-modding`, références `mcreator-asset-extraction.md`,
`vineflower-decompilation.md` et `devil-fruit-model-swap-26.3.md`.

---

## 1. Comment les transformations sont rendues en ver15

Point de méthode décisif, tiré du skill : **la transformation du joueur n'utilise pas GeckoLib**. Elle
passe par la lib tierce Forge *Kleiders Custom Renderer* :

1. l'événement `KleidersRenderEventProcedure` masque les parties vanilla du joueur ;
2. il rend par-dessus un **modèle Java vanilla** (`client/model/Model*_humanbeast.java`,
   `LayerDefinition` / `ModelPart`) ;
3. il **synchronise chaque os** avec le modèle du joueur, os par os.

Les `geo/*_humanbeast.geo.json` du jar concernent les **mobs PNJ** (Lucci, Kaku…), pas le joueur : les
extraire aurait été une erreur de cible.

**Conséquence pratique** : les modèles à extraire sont des **classes Java**, pas des modèles Bedrock.

## 2. Extraction

```bash
java -jar vineflower.jar --folder \
     --only=net/mcreator/minepiece/client/model \
     MinePiece-ver15-forge-1.20.1.jar <dest>
```

⚠️ **Piège rencontré** : Vineflower ne crée pas les répertoires intermédiaires pour un `--only` par
classe (`NoSuchFileException` sur `…/procedures/X.java`). **Créer l'arborescence avant** de lancer.

Résultat : **17 modèles de transformation** décompilés, en noms SRG
(`m_171558_`, `m_171488_`, `m_171423_`…).

## 3. Ce qui manquait réellement

L'inventaire a d'abord été faussé par une recherche **sensible à la casse** (`humanbeast` ne matche pas
`Humanbeast`) : le projet contenait déjà 21 fichiers de modèles. Après vérification :

| État | Fruits |
|---|---|
| Déjà mappés | 12 (wolf, leopard, sabertiger, giraffe, phoenix, daibutsu, seiryu, brachiosaure, pachycephalosaurus, ptéranodon, okuchi no makami, batto) |
| **Manquants** | **5** — Supa Supa, Nikyu Nikyu, Mori Mori, Goro Goro, Kira Kira |

## 4. Modèles extraits et portés (10 classes)

| Modèle source | Classe portée | Fruit | Texture |
|---|---|---|---|
| `Modelmori_mori_flower` | `ModelmoriMoriFlower` | Mori Mori | `mori_mori_flower.png` |
| `Modelsupasupa_form2` | `ModelsupasupaForm2` | Supa Supa | `supasupa_form2.png` |
| `Modelsupasupa_form2_2` | `ModelsupasupaForm22` | Supa Supa (2ᵉ couche) | `supasupa_form2.png` |
| `Modelpaws` | `Modelpaws` | Nikyu Nikyu | `paws.png` |
| `Modelpaws_slim` | `ModelpawsSlim` | Nikyu Nikyu (peau fine) | `paws.png` |
| `Modelhorns_yamato` | `ModelhornsYamato` | **Goro Goro** | `horns_yamato.png` |
| `Modelhorns_kaido` | `ModelhornsKaido` | cornes (Kaido) | `horns_kaido.png` |
| `Modelenel_drum` | `ModelenelDrum` | **Goro Goro** (tambour) | `enel_drum.png` |
| `Modelfeather_robe` | `ModelfeatherRobe` | Phénix (robe de plumes) | `feather_robe_white.png` |
| `Modelinu_inu_wolf_humanbeast_slim` | `ModelinuInuWolfHumanbeastSlim` | Inu Inu Wolf (peau fine) | `inu_inu_wolf_humanbeast.png` |

**Découverte** : Goro Goro **a bien** un visuel de transformation — cornes + tambour d'Enel. Kira Kira
n'a pas de modèle propre : la source réutilise le modèle *overlay* avec sa propre texture
(`kirakira_form2.png`), ce qui est reproduit tel quel.

Textures copiées : `paws.png`, `supasupa_form2.png`, `mori_mori_flower.png`, `enel_drum.png`,
`kirakira_form2.png`, `feather_robe_{white,black,lightblue}.png`.

## 5. Traduction SRG → Mojang

Table appliquée (extraite du skill, vérifiée sur les 17 modèles) :

| SRG | Mojang |
|---|---|
| `m_171576_` | `MeshDefinition.getRoot` |
| `m_171599_` | `PartDefinition.addOrReplaceChild` |
| `m_171558_` | `CubeListBuilder.create` |
| `m_171514_` | `CubeListBuilder.texOffs` |
| `m_171488_` | `CubeListBuilder.addBox` |
| `m_171480_` / `m_171555_` | `mirror()` / `mirror(boolean)` |
| `m_171423_` | `PartPose.offsetAndRotation` |
| `m_171419_` | `PartPose.offset` |
| `m_171565_` | `LayerDefinition.create` |

Changements d'API 26.3 :
- `EntityModel<T extends Entity>` → **`EntityModel<EntityRenderState>`** (non générique) ;
- constructeur : **`super(root)`** obligatoire (absent en 1.20.1) ;
- `renderToBuffer(...)` **supprimé** (le modèle rend son `root`, la méthode est `final`) ;
- `setupAnim(T, 6 floats)` → **`setupAnim(S state)`** ;
- `ModelPart.copyFrom` **supprimé** → `storePose()` / `loadPose(PartPose)` ;
- `new ResourceLocation(...)` → **`Identifier.fromNamespaceAndPath(...)`**.

La synchronisation d'os est identique pour les 17 modèles : les 6 mêmes os (`Head`, `Body`,
`RightArm`, `LeftArm`, `RightLeg`, `LeftLeg`) lus depuis `PlayerTransformLayer.currentParentModel`.
Vérifié dans la source : **les 17 modèles synchronisent exactement ces 6 os**, le portage est donc
mécanique — et le générateur l'a exploité.

## 6. Enregistrement et piles de rendu

- **10 couches** ajoutées à `ModelLayerRegistry` (`DevilFruitClientInit`) → **31** au total.
- **17 piles** dans `PlayerTransformLayer` — **les 17 fruits ont désormais leur visuel**.

### Variantes « peau fine » (slim)

La source a **deux modèles** pour certains visuels et choisit selon le modèle de peau du joueur
(`getModelName().equals("slim")`) : le modèle « large » appliqué à une peau fine déforme le rendu.
Le port ajoute donc `stackSlim(...)` + une sélection à l'affichage
(`state.skin.model() == PlayerModelType.SLIM`), avec repli sur la pile normale. Le PoC ne le faisait
pas — c'est un manque comblé :

| Fruit | Pile normale | Pile « slim » |
|---|---|---|
| Inu Inu Wolf | `inu_inu_wolf_humanbeast` + overlay | `inu_inu_wolf_humanbeast_slim` + `overlay_slim` |
| Nikyu Nikyu | `paws` | `paws_slim` |

## 7. Vérifications

- **Build** : `BUILD SUCCESSFUL`.
- **Ressources** (`validate_mod_assets.py`) : *every referenced resource exists; no missing clip* —
  31 modèles geo valides, 10 références (modèle, texture) résolues, aucune texture déclarée absente.
- **Mes 22 textures de transformation** : toutes présentes sur disque (vérifié une par une).
- **17 piles ↔ 17 fruits** (12 + 5), aucune mauvaise cible.
- **105 tests serveur** : la suite complète (les modèles sont côté client, donc non exercés par le
  harnais headless — mais on vérifie l'absence de régression et la validité des ressources).

## 8. Ce qui reste à valider par Donopot (client requis)

Le rendu d'un modèle **ne peut pas être vérifié en headless** — c'est la limite structurelle déjà
documentée. À contrôler en jeu, fruit par fruit :

- [ ] **Supa Supa** : deux couches superposées, lames visibles ;
- [ ] **Nikyu Nikyu** : pattes ; et sur une peau **fine** (compte Alex), le modèle slim ;
- [ ] **Mori Mori** : fleur ;
- [ ] **Goro Goro** : cornes **et** tambour ;
- [ ] **Kira Kira** : rendu via la peau overlay, texture dédiée ;
- [ ] **Phénix** : robe de plumes par-dessus les ailes ;
- [ ] **Inu Inu Wolf** sur peau fine : modèle slim + overlay slim.

Point d'attention : le débordement du modèle joueur vanilla sous le modèle de transformation
(l'équivalent de `hidePlayerModelPiece` côté source) **n'est pas traité** — c'est le premier item de la
liste « reste à faire » du skill, et il se voit immédiatement à l'écran. Je ne le revendique pas.
