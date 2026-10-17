# ROADMAP — VOID DYNASTY

**Mod Warhammer 40k pour Minecraft 26.3 (Fabric).**
Feuille de route : ce qui est fait, ce qui reste, dans quel ordre et pourquoi.

*Établie le 15 octobre 2026. État vérifié sur disque, pas de mémoire.*

---

## 0 · Méthode (les règles du chantier)

1. **La source fait loi.** Toutes les valeurs — dégâts, cadence, portée, points de vie, portées de
   détection, tables de loot — viennent des packs extraits (`astartes-extract/`,
   `thewarhammermod-extract/`) et de leurs fichiers de données. Aucune valeur n'est inventée.
   Le rapport `rapport-mecaniques-astartes.md` contient déjà la table complète des ennemis, armes et
   cadences : c'est la référence, elle n'a pas à être re-découverte.
2. **Ce qui existe ne se refait pas.** L'état ci-dessous est vérifié fichier par fichier.
3. **Chaque phase se termine par** : build OK → test headless (chargement, commande, entité) →
   **contrôle visuel de Donopot** quand il s'agit de rendu ou de sensations.
4. **Ce que je ne peux pas juger, je ne le juge pas.** Le rendu en main, l'équilibre ressenti, le
   style visuel : c'est ton œil. Je livre ce qui est mesurable, je te demande le reste.

---

## 1 · Où en est le mod (chiffres mesurés le 16/10/2026)

| | |
|---|---|
| **Code** | **180 classes Java, 13 531 lignes** |
| **Ressources** | 132 définitions d'items · 162 modèles · 87 textures d'items · 62 textures de blocs · 45 blockstates · 2 langues (FR/EN) |
| **Créatures** | **30 mobs GeckoLib**, chacun avec son modèle et ses animations (30 + 30) : 12 d'origine (Tyranides, Démons, Ork) et **les 18 du Chaos** |
| **Arsenal** | **15 armes** — 12 à distance + 3 de mêlée — dégâts **conformes à la source** (bolter 15, traqueur 30, melta 50, missile 100…) |
| **Armures** | Space Marine et Terminator (4 pièces chacune). Power Armour : le casque fait passer de 20 à **40 PV** |
| **Machines** | four, haut fourneau, fumoir, alambic Prométhium, extracteur minier, super coffre, super conteneur |
| **Vaisseaux** | frégate et croiseur posables, `/bridge`, `/hyperjump`, flotte (`/fleet`), pad de contrôle |
| **Campagne** | **portée de la carte Astartes** : 59 zones, 49 règles, 16 mécanismes (11 portes, une écluse, 4 panneaux du réacteur), 125 répliques, 24 objets donnés, 181 créatures relevées, barre du réacteur, écran de fin |
| **Dégâts de terrain** | câblés sur les trois chemins (projectile, hitscan, mêlée) et **mesurés** en jeu |
| **Génération** | le **minerai de céramite existe vraiment** (filon de 8 blocs, 4 par chunk, couche −64 à 48) — la chaîne du titane n'est plus bloquée |
| **Données** | **107 fichiers** : 78 recettes, **24 tables de butin**, 2 de campagne, 2 de génération, 1 tag |
| **Tests** | **37 contrôles, 37/37 PASS** (`/vdyntest all` → `docs/RAPPORT-TESTS.md`) + audit des assets (0 anomalie bloquante) |
| **Documents** | `ROADMAP` · `RAPPORT-TESTS` · `CAMPAGNE-SOURCE` · `EQUILIBRAGE` (généré, `--verifier` échoue si le document dérive) |
| **Jar** | `voiddynasty-1.0.0.jar` — 5,4 Mo, md5 **`61a362fc9acb5ddd7814303e06fad158`** |
| **git** | HEAD **`56e257b`**, 29 commits, arbre propre |
| **Carte livrée** | `/opt/data/workspace/VOID-DYNASTY-carte-astartes/` — monde + resource pack corrigé + les 3 jars + `FORCELOAD-CAMPAGNE.mcfunction` |

**Les huit chantiers sont clos** : A items · B terrain · C décors · D animations · E ennemis · F campagne ·
H outillage et finitions. **G (sons) est écarté** par Donopot.

---

## 2 · Ce que la source fournit : ce qui est porté, ce qui ne l'est pas

| Gisement | État |
|---|---|
| **19 ennemis du Chaos** | ✅ **portés** — les 18 créatures de la phase E, jusqu'au HellDrake |
| **24 props décoratifs** | ✅ **21 portés** (phase C) — les 3 véhicules lourds n'ont pas de modèle exploitable |
| **7 tables de butin** | ✅ transcrites, plus une table par mob : **24 au total** |
| **Bossbars** | ✅ **5/5** — Prêtresse, Capitaine, Forgefiend, Prince Démon, HellDrake |
| **Campagne scriptée** | ✅ **portée** — le moteur, le cerveau, les portes, la fin (phase F) |
| **Power Armour** | ✅ portée — nos **deux** tenues comptent (la source n'en regardait qu'une) |
| **57 textures custom** | ✅ utilisées (blocs décoratifs, objets, machines) |
| **Sons de faction** | ⬜ **NON porté** — c'est la phase G, que Donopot a écartée : cris de faction, ambiance tyranide |
| **4 modèles Blockbench bruts** | ⬜ non utilisés — les équivalents existaient déjà dans le mod |

**En clair : il ne reste, côté source, que les sons.** Tout le reste est porté, mesuré ou rendu.

---

## 3 · Les huit chantiers

### Phase A — Finir les items et les skins
*Le plus visible, et le plus rapide.*

- **A1 — Textures du pack.** ✅ **CLOS le 16/10/2025 : rien à remplacer.** Le point de départ
  (« 14 textures générées plates ») reposait sur un mauvais indicateur : la **taille du fichier**.
  Un sprite 16×16 net et bien dessiné pèse légitimement 100 à 250 octets. Une planche avant/après
  (`tools/planche_avant_apres.py`) a montré l'inverse de ce qu'on croyait : les « petites » textures
  sont des sprites propres et lisibles (un lingot ressemble à un lingot, le servo-crâne à un crâne),
  et les remplacer par des textures du pack réduites de 128×128 ou 512×512 en 16×16 donnait de la
  **bouillie grise**. Les 43 remplacements ont été annulés. Les textures du pack restent utiles —
  mais pour la Phase C (props), pas pour réécrire des icônes qui fonctionnent.
- **A2 — `display` des 15 armes.** ✅ **FAIT le 16/10/2025.** 33 clés ajoutées : les 15 armes ont
  désormais les 8 clés canoniques (`thirdperson` ×2, `firstperson` ×2, `gui`, `ground`, `head`,
  `fixed`). `head` manquait sur 13 armes sur 15 — l'arme n'apparaissait donc pas sur la tête.
  Les valeurs existantes ont été **conservées** (elles sont réglées sur la géométrie de chaque
  modèle) ; les manquantes ont été recopiées depuis l'arme la plus complète de la même famille, donc
  depuis les valeurs du projet. **L'échelle `gui` n'a pas été uniformisée, délibérément** : elle va
  de 0,35 (épée énergétique) à 2,0 (pistolet plasma) pour que chaque modèle remplisse la case de
  l'inventaire ; une valeur unique ferait déborder les armes longues. C'est une cohérence de
  résultat, qui se juge à l'œil. Outil : `tools/uniformiser_display.py`.
- **A3 — Audit final.** ✅ **FAIT le 16/10/2025.** `tools/audit_assets.py` contrôle 20 dimensions
  (objets, blocs, créatures, langues, recettes, sons, textures, atteignabilité créatif, armures,
  clips d'animation) dans les deux sens. Résultat : **0 anomalie bloquante**. Un défaut réel a été
  trouvé et corrigé au passage : l'**entité** servo-crâne n'avait pas de nom (clé de langue absente
  dans en_us ET fr_fr). Le contrôle est éprouvé par 4 sabotages volontaires — chacun est détecté.

**Effort** : faible. **Validation** : l'audit est scriptable, le reste est ton œil.
**Dépend de** : rien. ✅ Phase terminée.


### Phase B — Dégâts de terrain
*La mécanique est câblée ; c'est le réglage qui manque.*

- **B1 — Calibrer par arme.** ✅ **DÉJÀ FAIT** (vérifié le 16/10/2025, la roadmap se trompait). Chaque
  arme a son réglage distinct : bolter en explosif-pénétrant (16), traqueur en perce-armure,
  laser en ablation thermique (9), plasma en éclaboussure (29/70), fusil à pompe en grenaille,
  lance-flammes en cône de chaleur, **melta en faisceau thermique (110, 5 blocs de percement)**,
  missile en ogive (90, 60 blocs), mêlée en coupe / coupe énergétique / onde de choc.
- **B2 — Trancher `world_mode`.** ✅ **Plus bloquant** : c'est un réglage de `config/voiddynasty.json`
  (`off` · `cosmetic` · `wilderness_only` par défaut · `all_unprotected`). Ta décision se prend en une
  ligne, sans recompiler.
- **B3 — Cohérence perçage vs explosion.** ✅ Fait par les modes (voir B1).
- **B4 — PREUVE.** ✅ **FAIT le 16/10/2025** par le harnais : 9 contrôles, dont les paires
  positives/négatives. Et cette preuve a payé tout de suite — voir ci-dessous.

**🔴 Un vrai bug trouvé et corrigé grâce au harnais.** La table d'intégrité (fissures qui s'accumulent
avant la destruction) était indexée par un `BlockPos` **mutable** — celui que `BlockPos.betweenClosed`
réutilise à chaque itération. La clé changeait de coordonnées après insertion, donc le cumul n'était
**jamais relu** : seules les armes détruisant en un coup fonctionnaient (melta, plasma, missile, épée
énergétique, marteau) ; un bolter, un laser ou une épée tronçonneuse n'abîmaient **rien**, quel que soit
le nombre de tirs. Corrigé par `.immutable()` à l'insertion. Sans banc d'essai, ce défaut était
invisible : rien ne plantait, aucun message, et le melta marchait — ce qui donnait l'impression que le
système fonctionnait.

**Effort** : le réglage était fait ; restait la preuve. **Dépend de** : rien. ✅ Phase terminée.


### Phase C — Props décoratifs
*Remplir le vaisseau et les ruines — gain visuel immédiat.*

- ✅ **FAIT le 16/10/2025 : 21 props convertis en blocs posables** (`tools/convert_astartes_props.py`).
  Caisses (4 variantes), panneau gothique, **6 cadavres**, 4 décals de sang/gore, obus usagé,
  bloc-batterie, sceau de cire, amidon de cadavre, canon laser, tourelle multifuseur.
  31 textures copiées, `lang` FR/EN, onglet créatif, tous traduits.
- **Pas de cuisson en atlas, contrairement à ce que disait la version précédente de cette roadmap** :
  la limite « Indigo ne rend pas le multi-textures » ne concerne que les modèles d'**objets**. Les
  modèles de **blocs** acceptent plusieurs textures nativement — la conversion est donc une simple
  copie du modèle source, retaillée. C'est la méthode du skill (`item-model-to-block.md`).
- Deux retouches automatiques, calculées, pas choisies : les caisses font 43 unités de large (2,7
  blocs) → réduites juste assez pour tenir dans un bloc ; les cadavres descendent à Y = −7 → remontés
  pour reposer sur le sol au lieu de s'enterrer.
- **Décision de Donopot du 16/10/2025 (④)** : « on va chercher des mods source pour les importer en entité
  utilisable, priorité au Leman Russ ». **La recherche a montré qu'il n'y a rien à chercher** : la source
  fournit **déjà** les modèles (`models/item/lemunruss.json` — 33 cubes, rotations réelles, texture
  256×256 ; `fightership` et `fightership2` — 25 cubes, 128×128). Ils avaient été écartés comme
  **décors** à cause de leur taille (1,98 × 1,83 × 3,0 blocs), pas faute de modèle. Les mods tiers
  existent (Hammercraft 40k : véhicules, CurseForge/Modrinth) mais sont en NeoForge 1.20/1.21, pas en
  Fabric 26.3 — et leurs assets sont ceux d'un autre pack.
  ✅ **Modèles convertis** par `tools/convert_modele_java.py` (Java → GeckoLib, conventions éprouvées au
  rendu : Y réfléchi, Z inversé, UV de la grille 16 vers les pixels). Le **Leman Russ est juste** :
  chenilles en bas, canon horizontal, bande rouge et blanche de la Garde. ⚠️ **Réserve sur les deux
  chasseurs** : 10 cubes tournés sur 25 écartent visiblement le modèle ; le rendu sans rotations
  resserre les morceaux, donc la cause est le sens des rotations — reste à trancher entre une aile en
  dièdre voulue par la source et un signe à inverser.
  ✅ **Le char est PILOTABLE** (précision de Donopot : « non je veux du pilotable ») : entité `lemunruss`
  (hitbox 3,0 × 1,9 mesurée sur le modèle), un siège, on monte à la main droite, il avance, recule au
  ralenti et s'infléchit, il ne disparaît pas et ne se laisse pas pousser. Œuf d'apparition, onglet,
  noms FR/EN, contrôle `V-lemanruss-pilotable` (38/38) éprouvé par retrait du crochet de conduite.
  Déclaré comme CONCEPTION dans le code : 100 PV, 10 d'armure, vitesse 0,22, place du conducteur.
  ⏳ **Ce qui n'est pas fait, faute de source** : chenilles qui défilent (aucune animation fournie, je
  n'en invente pas) et canon qui tire (rien ne le décrit). Les 2 chasseurs restent à convertir en entité.
- **Écartés volontairement** : `lemunruss`, `fightership` ×2 (3 blocs et plus) — leur place est dans une
  structure, pas dans un bloc. À traiter avec la génération de structures si tu les veux.

**Régression attrapée par le harnais** : le prop `wax_seal` portait le nom d'un **objet** déjà livré
(un matériau) → clé dupliquée, le mod ne démarrait plus. Renommé `wax_seal_prop`, assets de l'objet
restaurés, et le convertisseur **refuse désormais d'écraser un asset existant** au lieu d'espérer que
les noms ne se croiseront jamais.

**Effort** : fait. **Validation** : contrôle `props` du harnais (21/21 posés) + audit d'assets (0 anomalie).

### Phase D — Animations
*Ce qui donne la vie : le recul des armes et le mouvement des créatures.*

- ✅ **FAIT le 16/10/2025 — recul et rechargement visibles en main, sans renderer ni mixin.**
  Le blocage annoncé (« le rendu d'arme en main est le point délicat ») a été contourné par la donnée :
  - deux composants d'état **transitoires** (`voiddynasty:recoil`, `voiddynasty:reloading`), non
    persistés, posés au tir et au rechargement, décrémentés par `WeaponItem.inventoryTick`
    (hook serveur en 26.3 : il reçoit un `ServerLevel`) ;
  - la définition d'objet devient une chaîne `minecraft:condition` sur la présence de ces composants ;
  - une variante de modèle par état (même modèle, reculé et basculé), générée pour les **15 armes**.
  Aucun paquet réseau ajouté : le jeu synchronise déjà les composants d'objet vers le client.
- **Pourquoi pas le cooldown vanilla** : la cadence vaut `20 / coupsParSeconde`, donc un recul
  proportionnel au cooldown dure moins d'un tick sur un bolter — invisible. Un recul **fixe de 3 ticks**
  a été retenu pour cette raison, et le cooldown vanilla ne sait pas exprimer une durée absolue.
- ✅ **Éclair de gueule et traceurs : déjà faits** (vérifié — 7 familles dans
  `WeaponService.spawnMuzzleFlash`, plus les traceurs et impacts). La roadmap les croyait absents.
- ✅ **Créatures : déjà fait** (vérifié — 36/36 clips demandés par le code existent pour les 12
  créatures : attaque, idle, marche/course). Aucune animation de mort n'existe, mais aucune n'est
  demandée : rien à corriger, seulement une piste pour plus tard.
- ⚠️ **Ce qui reste à ton œil** : l'amplitude du recul (2,2 unités + 5°) et l'angle du rechargement
  (32°) sont des valeurs de départ, pas des mesures. Le contrôle headless prouve le **déclenchement**,
  pas le rendu.

**Effort** : fait. **Validation** : contrôle `weaponanim` du harnais (cycle de vie complet + cas
limites) et contrôle n°11 de l'audit (chaîne `condition` des 15 armes, saboté pour vérifier qu'il
sait dire non).

### Phase E — Les ennemis du Chaos
*Le gros morceau de contenu, et il est parfaitement spécifié.*

- ✅ **LOT 1 FAIT le 16/10/2025 : les 5 cultistes** (Cultist1-4 + **Prêtresse du Chaos**, boss).
  PV et dégâts **de la source** (datapacks Astartes) : 20 PV / 3-6 dégâts par cultiste, 100 PV /
  5-8 pour la Prêtresse, sur base zombie. Les bornes de dégâts sont remplacées par leur milieu
  (4,5 / 6,5) — un attribut ne sait pas exprimer un intervalle ; c'est dit et non caché.
  Barre de boss sur la Prêtresse, branchée sur `startSeenByPlayer`/`stopSeenByPlayer` : c'est le suivi
  du jeu qui décide qui la voit, donc elle ne peut pas se désynchroniser.
  Œufs d'apparition, noms FR/EN, onglet créatif, 5 renderers GeckoLib.
- **Chaîne de conversion créée** : `tools/convert_ajblueprint.py` — un `.ajblueprint` est un projet
  **Blockbench** ; le script assemble l'ossature (uuid → nom + `parent`), fabrique l'atlas des textures
  réellement utilisées (1 à 3 selon le mob, tailles différentes → UV remis à l'échelle) et traduit les
  animations (clips Bedrock, os désignés par nom). Réutilisable pour les 14 ennemis restants.
- **Modèles : d'où ils viennent vraiment.** Ma première piste était fausse : TheWarhammerMod ne
  contient que **12 modèles GeckoLib** — exactement les 12 créatures déjà portées, et aucune classe de
  cultiste. Les 19 ennemis sont des blueprints Animated Java du resource pack **Astartes**.
- **Ce qui reste à ton œil** : l'échelle visuelle, et deux points signalés sans être corrigés —
  les cultistes 2, 3 et 4 n'ont **aucun clip « idle »** dans la source (on retombe sur la marche à
  l'arrêt, faute d'inventer une animation), et 1 à 2 cubes de chaque mob sont **plats** (épaisseur
  nulle) dans la source : portés tels quels, à toi de dire s'ils manquent.
- **Noms** : la source nomme ses cultistes « Cultist1 » à « Cultist4 ». Les libellés français
  (Cultiste, d'assaut, armé, fanatique) sont **ma** proposition — à renommer si tu veux autre chose.

Découpage restant :
- ✅ **LOT 2 FAIT le 16/10/2025 : les 5 Spaces Marines du Chaos.** 3 marines (50 PV, 10-25 dégâts → 17,5),
  le **Capitaine** (250 PV, 10-15 → 12,5, **barre de boss**) et le **Raptor** (50 PV, 10 dégâts).
  La source distingue le **tir** (`shoot`) de la **mêlée** (`attack`) : les clips sont conservés
  séparément, et une créature qui n'a que le tir joue ce clip quand elle attaque.
  **Raptor volant** : reproduit exactement comme la source le fait — vitesse accrue, saut puissant,
  chute lente — et non par un vol libre inventé.
  Le convertisseur écarte désormais les **locators** (points d'ancrage des blueprints, non
  géométriques) en les comptant au lieu de planter : le raptor en a deux.
- **Correction attrapée par le nouveau contrôle de l'audit** : la reconversion a renommé les clips des
  cultistes (`attack1` → `attack`, et le tir de Cultist1 s'appelle désormais `shoot`). Le code ne
  suivait plus — sans erreur, sans crash, juste des attaques sans animation. Le contrôle n°12 compare
  les clips **déclarés dans le code** aux clips **présents dans le fichier d'animation**, pour chaque
  entité ; il a été saboté pour vérifier qu'il sait dire non.

Découpage restant :
- ✅ **LOT 3 FAIT le 16/10/2025 : les 3 Servitors.** Valeurs **exactes** de la source (pas des
  fourchettes) : servitor de tir 200 PV / 8 dégâts, servitor de combat 200 PV / 10 dégâts, servitor
  lourd 100 PV / 15 dégâts.
- ✅ **COUCHE DE COMPORTEMENT LIVRÉE — et une correction que je dois à Donopot.** J'avais écrit que la
  source ne précisait pas les tirs. **C'était faux** : le rapport détaillé donne, pour CHAQUE mob, une
  plage de tir, une plage de contact et un cooldown pour chacune, plus un champ de vision obligatoire
  et un procédé de projectile. Ces valeurs sont maintenant transcrites dans un `ProfilAttaque` par mob,
  appliquées par deux comportements dédiés (tir / contact) qui remplacent le comportement générique, et
  **vérifiées par le harnais** (12/12 profils conformes à la source).
  Différence assumée : la source résout ses tirs par un raytrace instantané — contrainte de datapack,
  pas intention. On garde l'intention (dégâts du mob à distance) avec un projectile visible.
  Le projectile ennemi ne fait **aucun** dégât de terrain : la source ne vise que les entités.

Découpage restant :
- ✅ **LOT 4 FAIT le 16/10/2025 : les 3 machines.** Dreadnought (150 PV, contact 0..2 CD 2 s, tir 3..20
  CD 3 s), Hellbrute (150 PV, contact jusqu'à 10 blocs CD 2 s, aucun tir), Forgefiend (400 PV, contact
  0..6 CD 3 s, **tir 7..100 à 0,5 s en salve de 5 obus**, barre de boss).
  Les deux premières traînent une **lenteur permanente** : c'est le procédé de la source, pas un choix
  d'équilibrage de notre côté.
  Point de fidélité : la source donne au Forgefiend **deux dégâts différents** — 30 au contact, 10 par
  obus — alors qu'un seul attribut existe. L'attribut porte donc le contact (30) et le tir a sa propre
  valeur (10 × 5 obus = 50, le total de la source). La source annonce aussi un `attack_damage 50`, qui
  est la somme et non un coup ; c'est dit ici pour que personne ne s'y trompe plus tard.

- ✅ **LOT 5 FAIT le 16/10/2025 : le PRINCE DÉMON**, boss final de la phase E.
  **1 000 PV** (valeur de la source). La source lui donne **trois** attaques, pas une : frappe 3..5 blocs
  (10 dégâts, cooldown 1 s), **charge** 6..100 blocs (cooldown 15 s, sous forme d'accélération), **smash**
  0..3 blocs (cooldown 30 s, **20 dégâts sur 8 blocs de rayon**). Frappe via le comportement de contact,
  charge et smash via un comportement dédié. Barre de boss.
  Point de fidélité : la source décrit la charge comme une « accélération » sans en donner la valeur —
  on applique une accélération franche et brève, sans prétendre reproduire un chiffre qui n'existe pas.
  Le modèle vient du blueprint « bloodletter » (la source s'en sert comme base du Prince Démon).

- ✅ **HellDrake PORTÉ le 16/10/2025 — phase E, lot 6. Les 18 ennemis du Chaos sont livrés.**
  Mon constat précédent (« aucune animation ») était **faux**, et l'erreur venait de l'endroit où je
  l'avais cherchée : j'avais fouillé le resource pack sans penser aux **datapacks**, où vivent les
  animations de l'Animated Java. Ce qui existait réellement :
  - **19 images** d'animation `flying` (0 à 18), **133 transformations** = 19 × 7 os, matrices 4×4
    complètes dans `helldrake/on_load.mcfunction` (40 ko) ;
  - une invocation complète : `phantom` invisible (500 PV, 50 dégâts, `follow_range` 1000, échelle 2)
    + un `ghast` invisible portant la seconde moitié du modèle, et une barre de boss sur les PV.
  **Portage** : `tools/convert_helldrake.py` réunit les 7 fichiers de géométrie (37 cubes), les
  133 matrices et la texture 512×512 en un modèle GeckoLib + un clip `flying` de 19 images. La
  créature **étend `Phantom`**, exactement comme la source : le vol, la poursuite et l'attaque au
  contact viennent du jeu. Barre de boss suivant la visibilité de l'entité.
  **Calcul, pas approximation** : la source rend chaque os comme `T + R·S·p` ; GeckoLib rend
  `P + d + R·s·(v − P)`. On égalise les deux (`s = S/S_repos`, `d = T − P + R·s·P`), ce qui est exact
  pour n'importe quel pivot — d'où les pivots `P = (I − R_repos)⁻¹ · T_repos`.
  **Limites assumées et dites** : quand la créature est chevauchée, la source rejoue `servitorwalk`,
  l'animation d'un AUTRE rig (le servitor) — ce clip ne nomme pas les os du HellDrake, donc on ne le
  simule pas ; et les vitesses de déplacement ne viennent pas de la source (elle ne surcharge que
  quatre valeurs), c'est écrit dans le code.
  **Leçon retenue** : « ça n'existe pas » se vérifie dans TOUTES les formes du pack (resource pack ET
  datapacks) avant d'être annoncé — et une épreuve négative a montré que le premier contrôle d'UV
  écrit pour ce portage ne détectait rien ; il teste maintenant la **couverture** de la texture.

- **E-bossbars** : **5/5 faits** — Prêtresse, Capitaine, Forgefiend, Prince Démon et **HellDrake**
  (pilotée comme dans la source, sur les PV de la créature).

- ✅ **E-loot FAIT le 16/10/2025.** Les 7 tables de la source sont transcrites : 6 paliers de caisse de
  munitions + le butin de cultiste, plus une table par mob (22 fichiers).
  **Point de méthode** : la source est un datapack, elle ne peut pas créer d'objets ; elle simule donc
  ses munitions avec des objets vanilla **renommés** (`iron_nugget` → « Bolt Ammo », `coal` →
  « Battery Pack », `gold_nugget` → « Plasma Ammo » ou « Kerosene » selon le `custom_model_data`).
  Notre mod, lui, EST un mod : les vraies munitions existent déjà. Chaque entrée est transcrite vers
  l'objet qui la représente réellement — poids et quantités **inchangés**. `bread` et `golden_apple`
  sont déjà du pain et une pomme dorée dans la source : ils le restent.
  ⚠️ **Deux constats de la source, signalés et non « corrigés »** :
  1. `cultistboss` et `forgefiend` portent `DeathLootTable:"cultistboss"` et `"forgefiend"` — des
     chemins **sans namespace**, donc des références mortes : ces deux boss ne laissaient rien.
     → **Tranché par Donopot le 16/10/2025 : créer leurs tables.** Comme la source ne donne aucun
     contenu, on n'invente **aucun objet ni aucune quantité** : on réutilise ses tables en suivant sa
     propre échelle de paliers. `cultist_boss` → **palier 4** (premier boss, un cran au-dessus des
     cultistes et un cran sous les marines, dont le palier est 3) ; `chaos_forgefiend` → **palier 5**
     (boss de la dernière partie, aux côtés du capitaine, du dreadnought, du hellbrute et du prince
     démon, tous au palier 5). Le palier 6 reste non attribué, exactement comme dans la source.
     C'est une **décision, pas un portage** — c'est écrit tel quel dans `tools/generer_tables_loot.py`.
  2. Le format des tables a changé : la source utilise `functions` au niveau du pool, 26.3 attend
     `modifier` **dans chaque entrée** (vérifié : 0 table vanilla sur 400 utilise encore `functions`).
     Recopier la forme de la source aurait produit des tables ignorées en silence.
- ✅ **E-spawn CLOS le 16/10/2025 — la sous-tâche n'existe pas dans la source.** Vérifié : le
  `load.mcfunction` du GameEngine pose `gamerule doMobSpawning false`, et l'extrait complet ne contient
  **aucune** donnée de biome, **aucune** règle d'apparition, **aucune** notion de faction. Tous les
  ennemis arrivent par des `summon` scriptés : le seul « système d'apparition » de la source **est** la
  campagne. Plutôt que d'inventer des biomes, elle a été **relevée** : `docs/CAMPAGNE-SOURCE.md` +
  `docs/campagne-source.json` — **59 zones, 185 apparitions** à coordonnées fixes, **22 points de
  réapparition**, 18 types de créatures. Outil : `tools/extraire_apparitions_campagne.py` (suit les
  vagues passant par une fonction locale — sans cela 5 vagues disparaissaient du relevé en silence).

**Effort** : ✅ **les 18 ennemis du Chaos sont portés** (17 + le HellDrake), avec le butin (**24 tables**)
et les 5 barres de boss. La sous-tâche « apparition naturelle » n'existe pas dans la source (relevé : la
campagne). Le butin des deux boss à référence morte a été **tranché par Donopot** et créé.
**Validation** : `pv` **18/18** conformes à la source, `profils` 16/16, `spawn` **31/31**, `butin`
**24/24**, **30/30 créatures GeckoLib** complètes, **37/37 clips déclarés existent**, audit 0 anomalie.


### Phase F — La campagne
*Transforme un bac à sable en jeu.*

La source décrit une campagne linéaire complète : **22 checkpoints**, portes verrouillées, zones de
déclenchement qui font apparaître des vagues, **Power Armour** qui double les PV du joueur, objectif
final (le réacteur), écran de fin.

Le port a déjà les briques : un croiseur posable, une mission Monde Mortuaire, des commandes de
flotte. Ce chantier les organise en **système de progression** avec de vraies classes (zones,
portes, points de réapparition) au lieu des commandes de la source.

> **« Campagne scriptée ou mode libre ? » n'était pas une vraie question** : la source **est** une
> campagne scriptée. Le mode libre était une alternative que j'avais inventée. La source tranche.

#### ✅ F lot 1 — le moteur de campagne, FAIT le 16/10/2025

Transcrit de la source, avec de vraies classes et des **données**, comme le recommandait le rapport de
mécaniques lui-même (« pas des commandes fill/tp hardcodées ») :

- `tools/preparer_campagne.py` → `data/voiddynasty/campagne/campagne.json`, qui relève de la source :
  **10 zones déclenchables**, leur position d'entrée, l'index de niveau qu'elles posent, leurs
  **48 créatures** aux coordonnées d'origine, leur **point de réapparition** (`death.mcfunction`) et
  les **5 portes** verrouillées (`fill … red_stained_glass_pane`). Les noms de créatures de la source
  sont traduits vers **nos** identifiants ; une correspondance manquante **fait échouer l'outil** au
  lieu de passer inaperçue.
- `campagne/ZoneCampagne.java`, `CampagneDonnees.java` (lecture des données), `CampagneService.java`
  (entrée dans une zone à moins de 2 blocs — le rayon de la source —, invocation des vagues, pose du
  niveau, réapparition du joueur mort à son point de niveau, une seule fois par zone comme le drapeau
  `active`), `CampagneCommand.java` → **`/campagne etat|demarrer|arreter|reset|declencher <zone>`**.
- Les gamerules de la source (`spawn_mobs false`, `mob_griefing false`, `keep_inventory true`) sont
  posées **par commande**, exactement comme elle le fait — et seulement au démarrage de la campagne,
  pas à chaque lancement du serveur : le mod n'est pas toujours en mode campagne. **Écart assumé, dit.**

**Contrôle `F-campagne-source`** : les 10 zones, et la zone `armoury` telle que la source la décrit
(6 créatures, niveau 2, réapparition 264 89 55) — **plus un déclenchement réel** qui compte les
créatures effectivement invoquées. **Éprouvé en le sabotant** : une créature retirée des données fait
échouer le contrôle en nommant l'écart (« 5 créatures au lieu de 6 » sur les deux plans).

**Reste en F** : rien — les trois lots sont livrés (moteur, cerveau + portes, cerveau par tick + fin).

#### ✅ F lot 2 — le cerveau de la campagne et les portes, FAIT le 16/10/2025

Le relevé ne s'arrête plus aux zones : il descend jusqu'aux **règles** qui font avancer la partie.

- **`leveldetect.mcfunction` est le cerveau** — 63 lignes, dont **46 règles** : une position, un test
  (« un joueur à moins de N blocs » ou « plus aucun ennemi à moins de N blocs »), un compteur de zone, et
  des actions. Le lot 1 ne voyait que les 10 points d'entrée ; c'est ce fichier entier qui pilote la
  progression (44 règles d'entrée + 2 « zone nettoyée »).
- **Les zones ont un compteur, pas un drapeau.** `.sanctum` monte de 0 à 5 en traversant ses
  sous-zones : chaque fonction de zone pose l'étape suivante. Une porte ne s'ouvre donc qu'à partir d'un
  certain avancement — `door3` exige `.sanctum active 4`.
- **16 mécanismes** transcrits de `doors/dooropen.mcfunction` : les 11 portes (`door1`…`door11`),
  l'écluse, et les **4 panneaux du réacteur** (`reactor1`…`4`), qui retirent chacun 1 au compte du cœur.
  Chaque porte ouvre ses régions (`fill … air`), joue son son et appelle sa zone.
- **125 répliques** et les titres sont relevés : c'est la voix de la campagne (« I am BioProcessor XVII,
  your asigned servo skull. »).
- **36 waypoints, 20 objets donnés, 4 appels différés** (`schedule function`) relevés aussi.

**Découvertes qui ont changé le portage** (les deux sont des erreurs de MA part, corrigées) :

1. **Les créatures en relatif.** La source invoque parfois en `~ ~ ~`, relatif à l'ancrage de
   l'appelant. Un utilitaire (`marinesummon`) est appelé **cinq fois, à cinq ancrages** : sans suivre les
   appels imbriqués, **20 marines disparaissaient** du relevé — et des coordonnées se retrouvaient
   écrites en texte au lieu de nombres, ce qui faisait **échouer la lecture du fichier au démarrage**.
2. **Le nom du dossier n'est pas le nom du fichier.** `cultist1` invoque `cultistsummon` (sans le « 1 »).
   Une correspondance trop stricte faisait disparaître **3 cultistes de l'armurerie** (6 → 3).

**Contrôles** (4) : `F-campagne-zones` (59 zones, armurerie conforme, réapparition du niveau 2 = 264 89
55, déclenchement réel), `F-campagne-regles` (46 règles, la cryo-salle conforme à la ligne 1 de la
source), `F-portes-source` (16 mécanismes, `door1` conforme, **ouverture réelle vérifiée** — le verre est
bien retiré), et **`F-campagne-parcours` : les 46 règles rejouées une par une** (position + compteur de la
source) — **46/46 répondent**.

**Outils** : `/campagne etat | compteurs | evaluer | demarrer | arreter | reset | ouvrir <mecanisme> |
declencher <zone>` — `evaluer` dit quelles règles répondent là où tu te trouves, sans attendre.

**Un écart dit** : la position des marqueurs de porte est **déduite** du centre de la région qu'elles
ouvrent (la source, elle, a ses shulkers déjà posés dans le monde). Les 4 panneaux du réacteur n'ouvrent
aucune région : ils ne sont donc pas plaçables — ils sont **signalés**, pas devinés.

#### ✅ F lot 3 — le cerveau par tick, l'écran de fin, les objets, la Power Armour — FAIT le 16/10/2025

`tick.mcfunction` est le cerveau **par tick**, et il portait trois familles de mécanismes que
`leveldetect` n'a pas. Sans lui, la campagne s'arrêtait à mi-parcours.

- **2 règles sans position** (seul le compteur décide) et **1 règle sur entité** : c'est en **approchant
  le guide lumineux** (`cryo2`, à 3 blocs) que la cryo-salle avance. Sans elle, la progression se
  bloquait là, en silence.
- **Le retour au point de spawn** (380 105 −366) relance la campagne : c'est la source.
- **La barre « Reactor Core »** (max 16, départ 4) qui suit le compteur du cœur ; à **zéro**, la fin se
  déclenche : téléportation en **610 251 −55**, les deux sons, l'aveuglement, la suppression des guides,
  et le titre **« THANKS FOR PLAYING »**.
- **Les 24 objets donnés** (20 armes/munitions + 4 pièces d'armure) reçoivent **NOS** objets : la source
  donnait un bâton de carotte au modèle personnalisé, notre port possède les vraies armes.
  ⚠️ **Une déduction** : « Plasma Rifle » → notre `plasma_incinerator`. La source a deux armes à plasma
  (le pistolet et le fusil), nous aussi — c'est un appariement, pas une citation.
- **La Power Armour** : la source regarde le casque et pose la vie maximale à **40**. Elle ne regardait
  qu'**un** objet (un casque de diamant) ; notre port a **deux** tenues complètes et les deux comptent.
  **Extension déclarée.**

**Contrôles** (3 nouveaux) : `F-campagne-tick` (les compteurs, la barre, le seuil, et **la fin réellement
déclenchée** — le compteur du cœur revient bien à 4), `F-campagne-recompenses` (24 objets, tous avec le
nôtre, et l'armurerie **éprouvée** en posant ses objets au sol), `F-power-armour` (notre casque → 40 PV,
sans → 20, tête nue → 20). Le parcours passe à **49/49 règles** (position, compteur, entité).

**Épreuve négative** : saboté (la tenue n'agit plus, le seuil de fin exclu) → **2 FAIL**, nommés :
« avec notre casque : 20.0 PV au lieu de 40 » et « la fin ne s'est pas déclenchée avec le compteur à zéro ».

> ### ✅ La phase F est terminée.
> La campagne de la source est portée : 59 zones, 49 règles, 16 mécanismes, 24 objets, la barre du
> réacteur et sa fin. C'était le dernier morceau de contenu : **la phase F est terminée**.

### Phase G — Sons et ambiance — ⬜ ÉCARTÉE PAR DONOPOT

- Cris de faction au spawn et à l'attaque (loyalistes, Chaos, tyranides).
- Sons d'ambiance du vaisseau, alarmes, réacteur.
- Réutilisation des sons déjà extraits + compléments.

**Décision de Donopot : « on ne fait pas G ».** C'est la **seule** pièce de contenu de la source qui
reste non portée. Le chantier est court (les sons sont déjà extraits) et reste disponible si l'envie
revient — mais attention aux droits d'usage (§6), c'est le gisement le plus exposé.

### Passe de finition — 16/10/2025

Les points « à ton œil » ont été traités en fabriquant ce qui manquait pour **juger** : un rendu, un
diagnostic de silhouette, et des contrôles qui savent dire non. Chaque outil a été éprouvé avant
d'être cru.

#### Outils ajoutés

| Outil | Ce qu'il fait | Éprouvé |
|---|---|---|
| `tools/rendu_modeles.py` | rend un modèle GeckoLib en image (3 vues, damier d'un bloc au sol pour juger l'échelle, UV par face **et** en boîte, rotations par cube, **hiérarchie d'os**, mise en évidence des cubes plats) | `--autotest` : 7 contrôles sur une géométrie connue — un cube apparaît, une face est éclairée comme il faut, un cube proche cache le lointain, une rotation d'os déplace bien la géométrie |
| `tools/verifier_silhouette.py` | dit si les morceaux d'un modèle **se touchent** après pose | ⚠️ il a trouvé le seul cas douteux du mod (voir plus bas) |
| `tools/eprouver_conversion.py` | éprouve la conversion du HellDrake : compare la formule du convertisseur à celle de la source (os par os, 19 images), puis compare les **fichiers livrés** — relus, hiérarchie composée comme GeckoLib — à la source | mesure : formule livrée **95 unités** d'écart sur les ailes, nouvelle dérivation **0,0002 unité** |
| `tools/verifier_minerai_genere.py` | monte un serveur **jetable** en monde neuf (générateur normal), le laisse générer, puis compte les minerais dans les fichiers de région — le harnais tourne dans un monde **plat** et ne peut pas prouver qu'un bloc apparaît dans le sol | épreuve : une seule cible au lieu de deux → ardoise à **0**, code 1 |
| `tools/generer_minerai_ardoise.py` | fabrique la texture de la variante ardoise par **composition** (fond d'ardoise vanilla + les taches de céramite extraites par différence avec la pierre) | `--verifier` : compte les pixels de taches, refuse si la texture n'est pas une pierre + taches (< 5 % → code 1) |
| `tools/completer_variantes_armure.py` | remplit les variantes d'armure vides (`_s` bras fins, `_n`) depuis la texture de base, sans dépendance externe | `--verifier` : signale les variantes quasi vides (avant : 6, après : 0) |
| `tools/forceload_campagne.py` | calcule le forceload **exact** depuis les coordonnées de la campagne, et vérifie la couverture | `--eprouver` : une plage retirée → 6 coordonnées découvertes, code 1 |
| `tools/ajouter_idle_cultistes.py` | donne un clip d'arrêt aux cultistes qui n'en ont pas | `--verifier` : sans le clip, 4 fichiers signalés |

#### Ce qui a été trouvé

1. **Mon premier rendu était faux, et il le disait sans le savoir.** Il ignorait les rotations d'os
   et la hiérarchie : **11 modèles sur 30** (145 os tournés) s'affichaient en morceaux. C'est
   `verifier_silhouette.py` qui a mis sur la piste, en montrant que le HellDrake avait un os
   « isolé » — puis la lecture des pivots a montré que c'était l'outil, pas le modèle. **Un rendu
   qu'on n'a pas éprouvé ne prouve rien.**
2. **29 modèles sur 30 sont d'un seul tenant**, avec des tailles cohérentes (cultiste 1,7 bloc ·
   marine 2,7 · carnifex 6,2 · Prince Démon 6,3 · Grand Immonde 8,3). Le seul signalé est le
   **HellDrake** : sa boîte de corps ne touche pas celles de la tête et des ailes (écart de 0,5 à
   1,8 bloc). Un test par boîtes englobantes est grossier — c'est un **point à regarder en jeu**, pas
   un défaut prouvé.
3. **99 cubes d'épaisseur nulle** sur 18 modèles : ce sont, à l'écrasante majorité, des **ailes**
   (HellDrake, Prince Démon, démons de Khorne, Seigneur du Changement), du **tissu** (cultistes,
   Prêtresse) et des **tentacules** (Gardien des Secrets, Émissaire Norn). Ce sont des surfaces
   voulues, pas des oublis. La planche `planche-creatures-plats.png` les peint en magenta : l'œil
   tranche en une seconde.

#### Ce qui a été décidé

**Les cultistes 2, 3, 4 et la Prêtresse reçoivent le clip d'arrêt du culte 1.** La source n'en fournit
aucun pour eux. Les cinq cultistes sont le **même squelette avec des variantes** (vérifié : 14 à 15 os
communs) et l'idle du culte 1 nomme dix os, tous présents chez les autres sauf `gun` — que GeckoLib
ignore simplement quand l'os n'existe pas. **Réutiliser une animation de la source sur le même rig
n'est pas en inventer une.** À défaire d'une commande : `python3 tools/ajouter_idle_cultistes.py`
repose le clip, et `clipIdle()` se remet sur `"walk"`.

#### Ce qui a été vérifié sans rien changer

- **Les noms français étaient déjà complets** : 205 clés / 205, les 32 entités en français (« Cultiste
  d'assaut », « Cultiste armé », « Cultiste fanatique », « Prêtresse du Chaos »…). Rien à faire.
- **Les états d'armes existent et diffèrent réellement** : 15/15 armes ont un recul **et** un
  rechargement distincts du repos. Les valeurs du générateur (−2,2 unités et −5° au recul, −32° au
  rechargement) sont **cuites dans la géométrie** : l'écart mesuré va de 2,5 à 3,3 unités pour les
  armes à feu et jusqu'à 6,2 pour les armes de mêlée, l'écart venant de la rotation appliquée aux
  coins. **Conséquence à connaître** : cuite dans la géométrie, la pose s'applique aussi à l'icône
  d'inventaire pendant les 3 ticks concernés.
- **Le forceload livré était faux.** La plage `-160 0 500 160` était une estimation : elle ratait les
  portes au-delà de X 500 et **tout le Z négatif**. Les coordonnées de la campagne vont de −205 à 646
  en X et de −366 à 191 en Z. Le calcul exact donne **405 coordonnées couvertes, 480 chunks, 53
  plages** → `FORCELOAD-CAMPAGNE.mcfunction` dans le dossier de la carte.

#### Deux contrôles neufs, tous deux éprouvés en les sabotant

- **Audit, clips d'animation** : le contrôle ne lisait que les clips écrits `thenLoop("…")`. Les clips
  **rendus** par `clipIdle()` lui échappaient — c'est par ce trou que les cultistes ont pu réclamer un
  « idle » inexistant sans que rien ne bronche. 83/83 clips vérifiés. Épreuve : clip retiré de
  `cultist_2` → anomalie bloquante nommée, code 1.
- **Audit, états d'armes** : exige que chaque variante **diffère** du repos — une variante identique
  ne se verrait jamais. Épreuve : `boltgun_recoil` recopié sur le repos → « identique au repos »,
  code 1.

#### Ce qui reste à ton œil, en jeu

L'échelle des 30 créatures, les cubes plats signalés
dans les ailes et le tissu, et les sensations de recul/rechargement — ces quatre-là ne se jugent que
sur écran, et l'outil de rendu est là pour t'en montrer la géométrie avant.

#### Retour de Donopot : « pas mal de rendu violet ou de texture mal placée » — 16/10/2025

Il avait raison, et c'était mon rendu, pas les modèles. **Trois fautes d'UV**, toutes dans
`tools/rendu_modeles.py` :

| Faute | Effet | Preuve que c'était bien ça |
|---|---|---|
| La forme `{face: [u0, v0, u1, v1]}` n'était pas lue | **toutes les faces du HellDrake sortaient grises** | il est texturé maintenant (or, rouge, noir) |
| `north` et `south` étaient en miroir : le schéma Bedrock dit que l'origine d'une face est le **coin haut-gauche vu de l'extérieur, y vers le haut** | textures retournées sur les faces avant/arrière | corrigé selon la règle du schéma, pas à l'intuition |
| Le drapeau **`mirror`** des cubes était ignoré (« Mirrors the UV's … along the x axis ») | 12 cubes en miroir sur 3 modèles — lévrier carnifex, Juggernaut de Khorne, Grand démon de Khorne | `--ordre-boite` rend les dispositions comparables ; `tools/diagnostic_uv.py` mesure |

**Le violet, lui, n'était pas un bug** : la planche `…-cubes-plats.png` peint **volontairement** les
cubes d'épaisseur nulle en magenta, pour que l'œil les repère. La planche sans suffixe est le rendu
normal. Mesure faite quand même, pour ne pas se contenter d'une explication :
`tools/diagnostic_uv.py` compte la part de **magenta** et de **vide** que chaque modèle affiche —
c'est la définition même d'un UV faux, puisque le magenta marque les zones non peintes. Résultat :
**0 % sur 30 modèles**, sauf le capitaine du Chaos à 0,9 % — dont la texture ne contient que **42
pixels magenta sur 600 000** (0,06 %), et son violet est **sa cape**. Aucun UV ne pointe dans le vide.

**Le diagnostic est réutilisable** : `tools/diagnostic_uv.py <modeles> <textures> [--modele nom]
[--essayer-ordres]` — il dit, modèle par modèle et face par face, la part de violet et de vide
affichée. Un rendu qui montre du violet n'est pas une question de goût : c'est mesurable.



### Phase H — Outillage et finitions
- **Harnais de test headless.** ✅ **FAIT le 16/10/2025.** `tools/server_test_cycle.py` démarre un
  serveur Fabric réel, exécute `/vdyntest <scope>` par RCON, lit les lignes `[vdyntest] [PASS|FAIL]`,
  **redémarre le serveur**, puis écrit `docs/RAPPORT-TESTS.md`. 18 contrôles en 3 scopes :
  - `registry` (5) — les 15 armes, leurs dégâts, leur réglage de terrain, les 15 entités, les blocs ;
  - `terrain` (9) — les dégâts de terrain de la phase B, avec paires positives/négatives ;
  - `spawn` (1) — les 13 créatures naissent réellement.
  Premier lancement : **18/18 PASS**. Et il a immédiatement trouvé un bug invisible (voir phase B).
  Couture de test ajoutée au code de production : `TerrainDamageManager.accumulatedAt`,
  `thresholdFor`, `clearIntegrity` — un service doit pouvoir dire POURQUOI il a refusé.
- **Génération de monde** : le minerai de céramite apparaît-il naturellement ? *(vérifié le 16/10/2025 :
  **NON** — un seul bloc de minerai existe dans le mod, et il n'y a **aucun fichier `worldgen`** : ni
  `configured_feature`, ni `placed_feature`, ni modificateur de biome.)*

  ⚠️ **Et ce n'était pas une ligne « à faire », c'était un trou : la progression était bloquée en survie
  libre.** La chaîne complète, vérifiée pièce par pièce :

  | Élément | Vérifié |
  |---|---|
  | Le titane vient **uniquement** du minerai de céramite (fonte/blast) | `titanium_from_ceramite_ore(.json/_blasting.json)` — et **aucune** table de butin ne donne de titane |
  | L'Extracteur Minier **extrait le minerai présent dans le monde** (rayon 5, consomme du prométhium) | son propre commentaire dans `MiningRigBlockEntity.java` |
  | Or l'Extracteur Minier **coûte du titane** à fabriquer | `mining_rig.json` : 3 lingots de titane + plaque d'adamantium + bobine plasma + lentille |
  | Et la céramite coûte du titane | `create_ceramite.json` : 8 carbure de silicium + 1 titane |

  Donc : pas de minerai → pas de titane → pas d'extracteur → pas de céramite → **pas la moindre arme ni
  armure en survie libre**. Le silicium, lui, se fait (sable + charbon) : c'est bien le titane qui bloquait.
  **Ce n'était PAS un défaut de portage** : la source est une carte-campagne qui *donne* ses objets (le
  mannequin de l'armurerie, les `summon item` de chaque zone) et n'a aucune donnée de génération à recopier.

  ✅ **RÉGLÉ le 16/10/2025 — décision de Donopot : « le faire générer pour de vrai ».** C'est donc de la
  **conception**, pas du portage, et les valeurs sont un **point de départ assumé**, à régler par son œil :
  - `data/voiddynasty/worldgen/feature/ceramite_ore.json` — filon de **8 blocs**, remplaçant la pierre
    **et** l'ardoise des profondeurs (`stone_ore_replaceables` + `deepslate_ore_replaceables`).
  - `data/voiddynasty/worldgen/placed_feature/ceramite_ore.json` — **4 filons par chunk**, répartis au
    hasard, de la couche **−64 à 48**, en trapèze (plus dense au fond).
  - `worldgen/ModWorldGen.java` — l'attache à **tous les biomes de surface**, par l'API Fabric, à l'étape
    `UNDERGROUND_ORES`. C'est ce qui manquait le plus : sans elle, le fichier ne sert à rien.
  - ⚠️ **Un seul bloc de minerai existe** dans le mod (pas de variante « ardoise ») : le filon a donc la
    même apparence partout. À scinder si Donopot juge le rendu faux en profondeur.

  **Contrôle `H-minerai-genere`** : le filon est enregistré (4 modificateurs, la chaîne se résout), il est
  **lu depuis le jar**, et il figure dans les features du biome *plains* à l'étape `UNDERGROUND_ORES`, **au
  même endroit que le minerai de fer vanilla — ce fer sert de témoin** : s'il manque, le contrôle échoue au
  lieu de passer à vide. Limite dite : le monde du harnais est **plat** (aucune pierre), donc compter les
  blocs dans un chunk n'y prouverait rien.
  **Éprouvé en sabotant l'attache aux biomes** → `FAIL : le filon n'est pas dans les features du biome plains`.
- **Équilibrage** : table des dégâts relue d'ensemble (armes, créatures, armures). ✅ **FAIT le
  16/10/2025** → **`docs/EQUILIBRAGE.md`**, **généré** par `tools/table_equilibrage.py` :

#### Les quatre décisions de Donopot (16/10/2025)

| Sa décision | Ce qui a été fait | Mesuré |
|---|---|---|
| **« les fourchettes restent en médiane »** | rien à changer | cultistes 4,5 · marines 17,5 — assumé |
| **« les armes lourdes doivent détruire la pierre »** | vérifié : elles la détruisent déjà | melta **1**, missile **1**, incinérateur **1**, marteau **1**, stalker **2**, bolter lourd **3** |
| **« le laser détruit la pierre, d'autres blocs s'il y a beaucoup de tirs, et enflamme le bois »** | ⚠️ **vraies valeurs changées** : puissance 9 → **15**, percement 28 % → **45 %**, rayon 0,22 → **0,35**, blocs 2 → **6**, et l'inflammation passe de *non* à **25 %** (le pistolet laser suit, en plus faible) | fusil laser : pierre **>30 → 4 coups** · pistolet laser **20 coups** · **le bois s'allume** |
| **« pas les PV de la source : un joueur en fer complet comme cible, calibré pour que les armes lourdes restent très puissantes contre du diamant »** | vérifié par la mesure, l'armure étant **lue sur les vrais objets vanilla** (fer 15, diamant 20) | sur du **fer** : bolter 2 · stalker 1 · melta 1 · missile 1. Sur du **diamant** : stalker 2 · marteau 2 · incinérateur 3 · melta 1 · missile 1 — tandis que le pistolet laser en demande **29** et le fusil laser **16** |

⚠️ **Ces changements de valeurs sont des DÉCISIONS, pas du portage** : la source donnait 9/28 %/0 et le
laser n'entamait pas la pierre. C'est écrit dans `WeaponDefinitions.java`, à l'endroit exact.

⚠️ **Un contrôle s'est retourné tout seul** : l'ancien `B-laser-pierre` affirmait « le laser ne perce pas
la pierre » — c'était vrai *avant* ta décision. Le harnais l'a signalé en rouge dès le changement de
comportement, et le contrôle est devenu `B-laser-perce-pierre`. **Un vrai témoin négatif a été gardé** à
côté (`B-tronconneuse-ne-perce-pas`), pour que la batterie ne perde pas sa capacité à dire non.

- Les **15 armes** (dégâts, cadence, chargeur, rechargement, percement, critique, projectiles, munition) ;
  - leurs **dégâts de terrain** (mode, puissance, dureté, percement, rayon, blocs) ;
  - les **2 tenues** et la Power Armour ;
  - les **30 créatures** — PV, contact, tir, portées, cooldowns — **en regard des valeurs de la source**,
    avec un ⚠️ là où le port en diffère (les médianes assumées) ;
  - les **résistances des matériaux** et les **seuils mesurés**.
  **Ce que ce document ne prétend pas faire** : dire combien de coups il faut pour casser un bloc. C'est un
  calcul statique qui **se trompe**, parce que le cumul de dégâts **retombe** entre les tirs — la cadence
  s'en mêle. J'ai d'abord écrit cette colonne, puis je l'ai retirée : elle annonçait « 2 coups de bolter »
  là où le harnais **mesure 4**. Le document cite donc les **mesures réelles** du harnais.
  **Anti-dérive** : `python3 tools/table_equilibrage.py --verifier` échoue si le document a divergé du
  code — éprouvé dans les deux sens (à jour → 0 ; une ligne ajoutée à la main → 1).
  **Ce qui reste à juger** (dans le document, §5) : les fourchettes de la source devenues des médianes
  (cultistes 3–6 → 4,5 ; marines 10–25 → 17,5), la vitesse de percement de la pierre, le laser qui
  **n'entame pas la pierre** (mesuré : intacte après 6 tirs), et les PV de la source comme cible.

**Effort** : fait. **Recommandé tôt** — et ça a payé dès la première phase qui a suivi.


---

## 4 · L'ordre qui a été suivi, et ce qui vient maintenant

```
A (items/skins) ─► B (dégâts terrain) ─► C (décors) ─► D (animations)
        │                                                    │
        └────────────► H (harnais de test) ◄─────────────────┘
                                    │
                    E (18 ennemis) ──► F (campagne) ──► H (minerai, équilibrage)
                                                        │
                                          Passe de finition (rendus, finitions)
```

Tenu. Les huit chantiers sont clos, et le harnais a été posé **avant** que le contenu grossisse : c'est
lui qui a attrapé le bug des gamerules renommées, les 4 appels imbriqués de la campagne, et qui a
signalé de lui-même que l'ancien contrôle « le laser ne perce pas la pierre » était devenu faux.

**Ce qui vient maintenant, dans l'ordre :**

1. **Ton œil** — six points finis mais non validés (voir la recette de test). Tant qu'ils ne sont pas
   tranchés, polir le reste revient à peindre une pièce dont on ne sait pas si elle tient.
2. **La recette de test en jeu** — `docs/RECETTE-TEST.md`, à suivre pas à pas.
3. **Les deux décisions du §5** qui restent ouvertes.
4. **Une vraie session de jeu** : le mod n'a jamais été joué de bout en bout. Le harnais prouve les
   mécaniques, pas le plaisir.
5. **Phase G**, si tu reviens dessus — c'est du contenu, pas de la finition.
6. **Si tu veux diffuser** : remplacer les assets Games Workshop (voir §6).

## 5 · Décisions qui t'appartiennent

**Déjà tranchées :**

| Question | Ta réponse |
|---|---|
| Campagne scriptée ou mode libre ? | **scriptée** — la source n'en proposait pas d'autre |
| Difficulté : les PV de la source (Prince Démon à 1 000 PV) ? | **non** — la cible est un joueur en **fer complet**, calibré pour que les armes lourdes restent très puissantes contre du diamant |
| Les fourchettes de dégâts de la source | **rester en médiane** (cultistes 4,5 · marines 17,5) |
| Le laser perce-t-il la pierre ? | **oui**, et il enflamme le bois |
| Priorité contenu ou finition ? | les deux, dans l'ordre : contenu puis finition |

**Tranchées le 16/10/2025 :**

| Question | Ta réponse | État |
|---|---|---|
| **Dégâts de terrain en multijoueur** | **hors des zones revendiquées** (`wilderness_only`) | ✅ vérifié. C'était déjà le défaut et déjà branché (`TerrainDamageManager.isDamageable` → `ClaimIntegration`) ; en revanche **rien ne l'éprouvait** : sans FTB Chunks installé, le seul chemin exercé était « aucun claim → dégâts autorisés ». Deux contrôles ajoutés (`B-claim-protege` + son témoin positif `B-hors-claim-casse`) via un point d'essai dans `ClaimIntegration`. Épreuve négative : protection retirée → **10 PASS, 1 FAIL**, exactement `B-claim-protege` |
| **Distribution** | **usage personnel** | ✅ rien à faire — les assets Games Workshop restent chez toi. Le jour où tu voudras diffuser, c'est le §6 qui s'applique |

**Plus une question ouverte.** Ce qui reste est du même ordre que le reste : du regard, et deux chantiers
nommés (④ les décors non portés, ⑤b le minerai « ardoise »).

**Ce qui n'est plus une question** : la priorité, la campagne, la difficulté et les laser sont
tranchés ci-dessus.

## 6 · Risques

| Risque | Impact | État |
|---|---|---|
| **Droits d'usage** — assets et sons Warhammer (Games Workshop / Relic) | partage public impossible | **ouvert**, et c'est le plus lourd : usage personnel, ou remplacer les assets avant toute diffusion |
| **Le rendu visuel ne se vérifie pas en headless** | validation lente | **réduit** : `tools/rendu_modeles.py` montre la géométrie, `tools/verifier_silhouette.py` dit si un modèle tient d'un seul tenant, `tools/diagnostic_uv.py` mesure la part de violet et de vide. Reste ton œil pour le jugement |
| **Mon propre outillage peut mentir** | un rendu faux accuse un innocent | **constaté** : mon premier rendu ignorait les rotations d'os (11 modèles sur 30 affichés en morceaux) et trois formes d'UV. Parade tenue : tout outil est éprouvé sur une géométrie connue avant d'être cru |
| **Régression invisible** | une phase casse la précédente | **traité** : 37 contrôles, 37/37, et chaque contrôle neuf est saboté pour vérifier qu'il sait dire non |
| **Textures d'armure issues de textures de corps** | rendu des armures | ✅ **traité le 16/10/2025** (décision ⑥a) : les six variantes `_s` (joueur au **modèle fin**) étaient quasi vides (0,9 % de pixels peints contre 62,5 %), le jeu ne retombait donc pas sur la base et l'armure disparaissait sur ces joueurs. Remplies et vérifiées (`tools/completer_variantes_armure.py --verifier` : 6 → 0). Reste à trancher à l'écran : nos textures sont en 128×64, le vanilla en 64×32 |
| **Un seul bloc de minerai** | le filon avait la même apparence partout | ✅ **traité le 16/10/2025** (décision ⑤b) : le filon pose maintenant **deux** blocs, `ceramite_ore` au-dessus et `deepslate_ceramite_ore` en profondeur. Prouvé en jeu, pas en théorie : un serveur jetable a généré un monde neuf, et les deux variantes apparaissent dans les chunks (`tools/verifier_minerai_genere.py`). Épreuve négative : remettre une seule cible fait tomber l'ardoise à **0** et l'outil échoue |

## 7 · Ce que je ne peux pas faire à ta place

- **Juger un rendu** : si une arme tient bien en main, si une créature a la bonne silhouette, si les
  couleurs collent à l'univers. Je produis des images et des mesures (violet, vide, silhouette,
  échelle en blocs) — mais le jugement reste le tien.
- **Juger des sensations** : recul, cadence, difficulté d'un combat. Ça se joue, ça ne se mesure pas.
- **Trancher les deux décisions du §5** : elles engagent l'usage et la diffusion.
- **Jouer à ta place** : aucune session complète n'a encore eu lieu.

---

## 8 · En une phrase

Le mod est **complet** : 30 créatures, 15 armes conformes à la source, la campagne entière de la carte
Astartes, un arsenal de machines et de vaisseaux, un minerai qui débloque la progression, et **33
contrôles qui passent**. Il ne reste, côté source, que les **sons** — que tu as écartés. Ce qui reste
vraiment, c'est **ton œil sur six points**, deux décisions, et une vraie partie.
