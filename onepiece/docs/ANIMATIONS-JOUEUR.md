# Animations de joueur — état et plan

## Le problème, mesuré

- **169 techniques sur 172** ne déclarent **aucune entité visuelle** : le moteur générique joue un
  balancement ou un projectile, sans le geste de la technique.
- Le port avait **35 animations sur 361** ; les 326 manquantes ne pouvaient pas jouer.
- Le port avait **0 son** de mod (`34` dans la source).
- Les **transformations** ne montrent rien : `DevilFruitTransformation.java` applique des attributs et
  des particules, **aucun modèle**.

## La cause

`META-INF/mods.toml` de la source déclare :

```
kleiders_custom_renderer   kleidersplayerrenderer   playeranimator   geckolib
```

Le port n'avait que GeckoLib. Les deux bibliothèques qui **animent** et **habillent** le joueur
manquaient.

## Ce qui est fait

1. **PAL (Player Animation Library) 1.2.8+26.3 Fabric** ajouté — c'est la bibliothèque vers laquelle
   KosmX redirige désormais (`playeranimator` n'est plus développé et s'arrête à 1.21.7).
   Jar : `libs/playeranimlib-26.3-1.2.8.jar` · empreinte SHA-1 vérifiée
   (`e878fad023249fb2e039ddc6ce67821e1ba22960`) · présent dans le paquet client (`mods/`).
   **Bibliothèque client** : le serveur n'en a pas besoin.
2. **93 animations de joueur portées** dans `assets/minepiece/player_animation/` — le chemin exact que
   PAL lit. (Elles étaient mélangées avec les animations d'entités, qui vont dans `geckolib/animations/`.)
3. **34 sons + `sounds.json`** portés dans `assets/minepiece/sounds/`.
4. **267 animations d'entités** dans le jar (`geckolib/animations/`), contre 35 avant.

## Le modèle de la source, décodé

`PlayAnimationPlayerProcedure` écoute l'**événement d'attaque** (`LivingAttackEvent`) et :

1. lit un **code** dans les attributs `ANIMATION_1` et `ANIMATION_2` (comparé à 2, 3, 4 … 18, 50, 100) ;
2. le traduit en **nom d'animation** — 29 noms :
   `punch_` · `kick_` · `sword` · `backstep` · `death` · `guard` · `swim` · `amane_dachi` ·
   `punch_gomu_pistol1/2` · `punch_gomu_gatling1/2` · `speed_attack1` · `togen_totsuka1/2` ·
   `hakoku_left1/2` · `hakoku_right1/2` · `ul_zugan` · `headbutt_1/2` · `head_overhead` · `enkai` ·
   `clap` · `energy_charge` · `yamiyami_blackhole` · `invisibility` · `ursus_shock` · `rokuogan` ·
   `rin` · `yasakani_no_magatama` · `onigiri1/2` · `toragari1/2` · `sanzen_sekai1/2` · `iai1` ·
   `fly_front/2/3` · `fly_back/2` · `negative` · `cancel` ;
3. envoie le paquet **`minepiece:start_animation`** aux clients qui suivent l'entité.

**API PAL à employer pour la suite** (relevée dans le jar, pas devinée) :

| Besoin | Appel |
|---|---|
| Accéder au gestionnaire du joueur | `PlayerAnimationAccess.getPlayerAnimManager(Avatar)` |
| Enregistrer une couche | `PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(Identifier, priorité, factory)` |
| Récupérer la couche | `PlayerAnimationAccess.getPlayerAnimationLayer(Avatar, Identifier)` |
| Poser une animation | `ModifierLayer.setAnimation(T)` |
| Construire depuis un identifiant | `RawAnimation.begin().thenPlay(Identifier)` |

## Ce qui reste — dans l'ordre

1. **Extraire la table code → nom** du bytecode de `PlayAnimationPlayerProcedure` (paires exactes, pas
   approximées).
2. **Porter les attributs d'animation** (`ANIMATION_1`, `ANIMATION_2`) et les faire renseigner par les
   techniques — c'est ce qui relie une technique à un geste.
3. **Ajouter le paquet S2C** (le port a déjà `TransformStatePayload` comme modèle) pour que les
   **autres joueurs** voient le geste, pas seulement le lanceur.
4. **Côté client** : enregistrer la couche (dans `DevilFruitClientInit`) et appeler
   `ModifierLayer.setAnimation(RawAnimation.begin().thenPlay("minepiece:<nom>"))` à la réception.
5. **Câbler les 34 sons** (`minepiece:guard`, `minepiece:sword_sweep`…) sur les techniques.

## Limite de vérification

Le serveur de test est **headless** : je peux prouver que les fichiers existent, que le jar les
contient, que le code compile et que le paquet serveur→client part. **Le rendu à l'écran se vérifie
chez toi** — je ne le déclarerai pas validé à ta place.
