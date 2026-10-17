# État lot05 : implémenté, testé serveur et vu en laboratoire

Voir [validation](../B05-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-05.md). Acceptation artistique humaine en attente ; les affirmations historiques ci-dessous sont à lire avec les corrections de source du lot05.

---

# Contrat — Gomu Gomu (Gum-Gum Fruit)

Cinquième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. C'est le Fruit du
protagoniste, et celui dont l'écart avec ver15 était le plus visible : le bras était **décoratif**
alors que toute la technique repose sur son extension.

- **Item** : `minepiece:item_devil_fruit_gomu_gomu`
- **Techniques** : 3 (ver15) — Pistol, Bazooka, Gatling (+ Gear Change, ajout du port)
- **Famille** : **`paramecia`** (ver15 disait `ZOAN` — voir §2)

---

## 1. Identité, limites et contre-mesures *(FRUIT-GOMU_GOMU-01)*

**Identité** — Paramecia du Caoutchouc. Le corps est élastique : il s'étire, absorbe les chocs, et
renvoie les coups. Sa signature est l'**extension** — un bras qui va frapper là où le poing arrive.

**Limites** :

| Limite | Valeur | Source |
|---|---|---|
| Énergie disponible | 200 | CORE-005 |
| Coûts des techniques | 50 à 60 | plan ver15 |
| Recharges | 100 à 140 ticks | plan ver15 |
| **Portée du bras** | 6 (Pistol) / 5 (Bazooka, Gatling) | plan ver15 |
| **Impacts par technique** | 1 / 1 / 6 | décision de contrat (§4) |
| **Plafond de dégâts par usage** | 36 | décision de contrat (§4) |

**Contre-mesures** :

- **Un mur arrête le bras** — et donc le coup : aucune cible au-delà n'est atteinte (preuve `G2`, `G3`) ;
- **Eau de mer** → pouvoirs scellés (CORE-007) ;
- **Alliés** → jamais touchés (`isValidTarget` filtre avant la frappe) ;
- **Énergie insuffisante** → refus sans coût (CORE-005) ;
- **Retrait du Fruit** → reset complet (FRUIT-RESET-01) : le bras n'est pas un état du porteur, et
  `GomuArmStrike.clearArms` garantit qu'aucun membre ne subsiste.

## 2. Classification *(FRUIT-GOMU_GOMU-02)*

ver15 classe Gomu Gomu en **`ZOAN`** — manifestement par confusion : le Fruit ne donne **aucune forme
animale**. L'action demande de ne pas supposer que « non classé » signifie Paramecia ; ici le cas est
différent, mais la méthode est la même : on **vérifie** plutôt que de recopier.

Décision : **`paramecia`**, et le Fruit reste **transformable** (Gear Change). C'est un **écart assumé
par rapport à ver15**, verrouillé par le contrôle `F2` de la recette (`/dftest fruits`), qui vérifie la
famille lue par le code et non le commentaire.

## 3. Catalogue final *(FRUIT-GOMU_GOMU-01)*

| Technique | Type | Dégâts | Portée | Recul | Énergie | Recharge | Bras |
|---|---|---|---|---|---|---|---|
| **Gomu Gomu no Pistol** | frappe (bras) | 11 | 6 | 1,5 | 50 | 100 | 1 poing |
| **Gomu Gomu no Bazooka** | frappe (bras) | 20 | 5 | **3,0** | 60 | 140 | **2 poings** |
| **Gomu Gomu no Gatling** | frappe (bras) | 6 ×6 | 5 | 1,0 | 50 | 100 | rafale |
| **Gear Change** | buff | — | — | — | 40 | 80 | — |

Valeurs de ver15 **inchangées** : l'audit ne relève aucun écart de paramètre.

## 4. Spécialisations

### Le bras : une extension physique, pas un décor *(FRUIT-GOMU_GOMU-03)*

Le plan : « étendre **réellement** les bras et leur collision, **limiter la portée** et **ramener le
membre** après impact ». Critère : « la portée physique correspond au bras visible ; un mur interrompt
le coup ; aucun membre ne reste après annulation ».

**Ce que faisait ver15** — la frappe était un cône générique portant à 6 puis 5 blocs, et le bras était
une entité purement décorative, apparue **sur le joueur**, quelle que soit la cible.

**Comment la portée physique correspond au bras visible.** L'avancée du poing dans le modèle n'est pas
supposée : elle est **mesurée dans les keyframes** de `entity_gomu_arm.animation.json` — le bone
`right_arm` de `punch_gomu_pistol` se translate de 32 unités Bedrock, soit **2,00 blocs**. Le bras est
donc **étiré** d'un facteur `portée / 2`, valeur transmise au rendu par un champ synchronisé
(`EntityGomuArmEntity.STRETCH`), et appliquée par `EntityGomuArmRenderer.scaleModelForRender`.

Une seule distance commande **à la fois** l'étirement du bras et le point où les dégâts sont appliqués :
les deux ne peuvent pas diverger, c'est une construction, pas une coïncidence.

**Preuves** :
- `G1` : à vide, arrêt à **6,0 blocs** ; étirement **3,0 × 2,0 = 6,0 blocs** → le poing est exactement
  là où frappent les dégâts ;
- `G2` : mur de pierre à 3 blocs → arrêt à **2,5 blocs** (étirement 1,25) — la limitation **se voit** ;
- `G3` : une cible à 6 blocs **derrière** le mur n'est pas touchée (0 dégât) ; le mur retiré, elle
  l'est (11 dégâts) ;
- `G5` : après 3 frappes, **1 seul** bras subsiste (aucune accumulation), durée de vie bornée à
  **16 ticks**, et le nettoyage en laisse **0**.

### Trois techniques, trois intentions *(FRUIT-GOMU_GOMU-04)*

Le plan : « différencier Pistol, Bazooka et Gatling : **impact simple, poussée et rafale bornée** ».
Critère : « chaque technique produit son **nombre d'impacts documenté** et le **total de dégâts reste
plafonné** ».

ver15 ne donnait **aucun** nombre d'impacts — les trois techniques ne différaient que par leurs
chiffres. Le nombre est donc un **choix de contrat**, documenté et verrouillé :

| Technique | Intentions | Impacts | Total | Justification |
|---|---|---|---|---|
| **Pistol** | impact simple | **1** | 11 | un poing, un coup |
| **Bazooka** | **poussée** | **1** | 20 | l'animation `gomu_bazooka1` meut **les deux bras** → deux poings, un seul impact ; la poussée vient du **recul 3,0** (le plus élevé du Fruit) |
| **Gatling** | **rafale bornée** | **6** | **36** | 6 × 6 = exactement le plafond |

**Le plafond** (`MAX_TOTAL_DAMAGE = 36`) est appliqué dans la boucle d'impacts elle-même, pas dans un
commentaire : même si le nombre d'impacts était porté par erreur à 20, le total resterait 36.

**Preuves** (`G4`) : Pistol **11,0**, Bazooka **20,0**, Gatling **36,0** — et 36,0 ≤ 36,0.

> **Décision à valider.** Limiter la portée physique au bras est demandé explicitement par le plan, et
> le bras doit s'étirer pour couvrir 5 ou 6 blocs (facteur 2,5 à 3). C'est ce qui est implémenté :
> la portée de ver15 est **conservée**, mais elle est désormais **matérialisée** par le bras. Si tu
> préfères une portée plus courte (le bras ne dépasserait pas ses 2 blocs d'animation), dis-le : c'est
> un changement d'une ligne (`GomuArmStrike.resolve`, borne de la distance).

### Les formes de Gomu *(FRUIT-GOMU_GOMU-05/06)*

Le plan constate que `applyForm` « fournit des bonus de potions et **aucun modèle joueur** », et demande
de « remplacer les bonus seuls par un rendu visible ».

**État** : `Gear Change` existe (ajout du port, absent de ver15) comme **buff réel** (Célérité), avec
coût et recharge propres. Les **formes modélisées** (Gear Second / Third / Fourth avec modèle joueur,
animations, dimensions, caméra) supposent un **modèle de joueur dédié** — c'est un travail
d'animation et de rendu client, hors de ce qui est vérifiable en headless, et il n'est **pas**
revendiqué ici. C'est le seul point de ce contrat qui reste ouvert, et il est listé en §6.

### Passifs et contreparties *(FRUIT-GOMU_GOMU-06)*

- **Immunité du caoutchouc aux projectiles** — héritée de CORE-029 et du tag `minepiece:gomu_immunity`
  (corrigé lorsque le dossier de tags au singulier avait révélé qu'elle était du code mort).
- **Aucun passif permanent lié aux techniques** : les trois frappes sont instantanées, le bras est
  retiré (`G5`). Rien à nettoyer sous suppression ou retrait, hormis le reset complet.

## 5. Acquisition, rareté, progression *(FRUIT-GOMU_GOMU-08)*

| Aspect | État |
|---|---|
| Rareté | palier **commun** (poids 60), loot des coffres (CORE-023) |
| Acquisition | coffre de structure, ou `/devilfruit give item_devil_fruit_gomu_gomu` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-GOMU_GOMU-07 — P2, exige un client)*

- [ ] icône du Fruit, inventaire et au sol ; Fruit tenu deux mains, 1ʳᵉ et 3ᵉ personne ;
- [ ] infobulle (nom FR) ;
- [ ] **le bras s'étire jusqu'à 6 blocs** (Pistol) et **se rétracte** — c'est le cœur de `G1` ;
- [ ] le bras **s'arrête au mur**, visiblement plus court (`G2`) ;
- [ ] **Bazooka** : les **deux** bras partent ensemble ; **Gatling** : rafale rapide ;
- [ ] **axe d'étirement à confirmer** : le rendu étire l'axe Z du modèle
      (`scaleModelForRender`). Si le poing part vers l'arrière ou sur le côté, l'axe est à inverser —
      c'est le seul point de ce contrat qui ne peut pas être vérifié sans client ;
- [ ] **Gear Change** : buff visible (Célérité), sans modèle joueur — les formes modélisées restent à
      faire (§4).

## 7. Tests *(FRUIT-GOMU_GOMU-09)*

`/dftest gomu` — **6/6 PASS** :

| id | Résultat |
|---|---|
| `G0-decor-pret` | couloir dégagé, regard vers +Z, chunk de spawn |
| `G1-portee-physique-egale-au-bras-visible` | arrêt 6,0 · étirement 3,0 × 2,0 = 6,0 |
| `G2-mur-interrompt-le-coup` | mur à 3 → arrêt 2,5 (au lieu de 6,0) |
| `G3-mur-protege-la-cible` | derrière le mur 0 dégât · mur retiré 11 dégâts |
| `G4-impacts-documentes-et-plafond` | 1/1/6 → 11,0 / 20,0 / 36,0 ≤ plafond 36,0 |
| `G5-aucun-membre-ne-reste` | 1 bras après 3 frappes, 16 ticks max, nettoyage → 0 |

⚠️ **Piège de test rencontré** : le recul du Pistol (1,5) projetait la cible **hors de portée** du
Bazooka (5 blocs), d'où un total de 0 — comportement **correct** du contrat (« limiter la portée »),
mais mesure faussée. Le test replace désormais la cible avant chaque mesure.

Reste manuel : rendu du bras, axe d'étirement, deux bras du Bazooka, formes modélisées (§6).
