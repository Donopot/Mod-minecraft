# Mise à jour lot 07 — code et serveur vérifiés, revue graphique en cours

Voir [adaptations et preuves](../BATCH-07.md). Le cycle comprend maintenant une forme animale complète. Les anciens constats de buff seul ci-dessous sont historiques ; les adaptations de la source sont décrites dans le bilan du lot. Acceptation artistique humaine en attente.

---

# Contrat — Hito Hito Daibutsu (Human-Human Fruit, Model: Daibutsu)

Quatorzième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Zoan mythique** du
Bouddha doré (Sengoku). Ce contrat porte sur un problème que les treize précédents n'avaient pas
rencontré : **une forme qui change la taille du corps**. Une taille, ça casse quatre choses à la fois.

- **Item** : `minepiece:item_devil_fruit_hito_hito_daibutsu`
- **Techniques** : 5 (ver15) — Attack Overhead, Brilliant Punk, Impact Ball, Attack Overhead Super,
  Impact Wave
- **Famille** : **zoan** — vérifiée sur le tag lu (`R0`), + `transformable`
- **Échelle** : **3,5** (table SIZE ver15 : 5,0 × 0,5), **plafonnée** par `max_scale`

> ⚠️ **Note de méthode** : j'avais annoncé « fruit suivant : Ito Ito ». **Ce fruit n'existe pas** dans le
> plan — nom inventé de ma part. L'ordre réel (42 entrées) donnait Hito Hito Daibutsu. Corrigé en
> relisant la liste des fruits plutôt qu'en faisant confiance à mon propre récapitulatif.

---

## 1. Identité, limites et contre-mesures *(FRUIT-HITO_HITO_DAIBUTSU-01)*

**Identité** — Le porteur se transforme en **Bouddha géant** : il ne lance pas des projectiles, il
**devient grand**. Sa puissance vient de sa masse, de sa portée et de ses ondes de pression.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 60 | plan ver15 |
| Recharges | 100 à 160 ticks | plan ver15 |
| **Échelle** | **3,5** (table SIZE), **plafonnée** | config |
| **Espace nécessaire** | **vérifié avant transformation** | `hasRoom` |
| **Maintien** | **3,0 É/s** | config |
| Durée maximale de forme | 1200 ticks | config |
| Mobilité | **−15 %** de vitesse | config |
| Onde : vitesse | 1,5 bloc / tick | config |
| Onde : portée | 12,0 blocs | config |
| Onde : dégâts | 28,0 (une frappe par cible) | config |

**Contre-mesures** : eau de mer (SEA_POWER → pouvoir scellé, **forme dissoute**), Haki de l'Armement,
énergie insuffisante, **épuisement du maintien** (la forme retombe d'elle-même), retrait du Fruit.

## 2. Classification *(FRUIT-HITO_HITO_DAIBUTSU-02)*

Le fruit **transforme son porteur** : c'est la définition du Zoan. `R0` **lit** les tags :
`zoan=true`, `transformable=true`. Aucune supposition depuis `NON_CLASSE`.

Vérifié : `python3 tools/fruit_audit.py --fruit HITO_HITO_DAIBUTSU` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Attack Overhead** | frappe | conservée, **portée × échelle** | 15 | 12 | 50 | 120 |
| **Brilliant Punk** | frappe | conservée, **portée × échelle** | 15 | 6 | 50 | 100 |
| **Impact Ball** | projectile | conservée | 15 | — | 50 | 120 |
| **Attack Overhead Super** | frappe | conservée, **portée × échelle** | 25 | 10 | 60 | 140 |
| **Impact Wave** | frappe | **onde de choc voyageuse** | 28 | 12 | 60 | 160 |

## 4. Spécialisations

### Forme géante : quatre cohérences, pas une *(FRUIT-HITO_HITO_DAIBUTSU-03)*

Le plan : « créer une forme géante visible avec **limite de taille** », critère « **caméra, collision,
portée et espace nécessaire** sont cohérents ». Quatre exigences, quatre réponses distinctes :

| Exigence | Comment elle est tenue |
|---|---|
| **Caméra** | l'échelle passe par `Attributes.SCALE` (vanille) : la caméra et le point de vue suivent — rien à recoder |
| **Collision** | `refreshDimensions()` après l'échelle : la boîte de collision correspond à ce qui est vu |
| **Portée** | `CombatSkillLogic.effectiveRange` : la portée suit le corps |
| **Espace nécessaire** | `GiantForm.hasRoom` : la transformation est **refusée** si le géant n'a pas la place |

**La limite de taille, et le défaut qu'elle a révélé.** `R1` vérifie que `clampedScale(40)` rend **3,5**.
Mais la première version appliquait le plafond **seulement** dans `GiantForm.begin` — alors que
l'échelle réelle est posée par la **table de transformation**, qui utilisait `scaleMultiplier(fruit)`
**sans passer par le plafond**. Autrement dit : le plafond était décoratif, et une table à 40 aurait
produit un géant de 40.

⚠️ **Défaut réel, corrigé** : le plafond est maintenant appliqué **là où l'échelle est réellement
posée** (`applyForm` → `clampedScale`). Et `R3` a été réécrit pour passer par le **vrai chemin**
(`DevilFruitTransformation.toggle`) plutôt que par une reconstruction du test — c'est le seul moyen de
prouver que l'échelle **posée** est bornée, et pas seulement qu'une fonction utilitaire sait borner.
Preuve : échelle posée **3,5**, plafond **3,5**.

**L'espace nécessaire, et le second défaut.** `R2` : place en plaine dégagée = **true**, avec un plafond
à 2 blocs = **false** → la transformation est **refusée** au lieu d'encastrer le joueur dans la pierre.

⚠️ **Ce refus était cassé au premier essai** : le volume exigé se calculait par un `AABB.inflate`
global, qui grandit **aussi vers le bas** — donc dans le sol. Résultat : place = *false* **partout**,
même en plein champ. Un géant ne se déploie pas en s'enterrant. Corrigé par la géométrie réelle : la
boîte s'étend **des pieds vers le haut** et s'élargit autour de l'axe vertical.

**La portée suit le corps** (`R3`) : **6,0 → 21,0 blocs** pour une échelle de 3,5. La règle est écrite
**une seule fois** (`CombatSkillLogic.effectiveRange`, sur `getScale()`) : toutes les formes agrandies en
héritent — girafe, ptéranodon — sans formule recopiée.

### Mobilité et coût de maintien *(FRUIT-HITO_HITO_DAIBUTSU-05)*

Le plan : « définir **mobilité** et **coût de maintien** de la forme », critère « les **attributs
reviennent à la normale** après la fin de transformation ».

- **Mobilité** : −15 % de vitesse, en modificateur **identifié par ressource** (`giant_speed`).
- **Maintien** : **3,0 É/s**, consommés en continu. `R4` : 200,0 → **182,45 É** après 3 s, et la forme
  **retombe** quand la réserve s'épuise. Une forme qui ne se paie pas serait une forme permanente.
- **Borne de durée** : 1200 ticks, indépendante de l'énergie.

**« Les attributs reviennent à la normale » — sans corruption, comme toujours.** `R5` : les
modificateurs de vitesse passent de 0 → 1 (forme) → **0** (après), et surtout **un modificateur
ÉTRANGER posé pendant la forme survit** : la forme retire **ses** modificateurs, jamais « tous ». C'est
la même frontière que la libération « sans corruption » du Mero Mero, appliquée aux attributs.

### Ondes de choc : le front décide *(FRUIT-HITO_HITO_DAIBUTSU-04)*

Le plan : « créer des ondes de choc visibles avec **progression** et **dégâts bornés** », critère « le
**front d'onde détermine l'impact** et **respecte les protections** ».

**Pourquoi un front qui voyage et pas un rayon.** Une frappe de zone touche tout le monde au même
instant. Ici, ce qui est atteint dépend de **où le front est** à cet instant : celui qui a le temps de
sortir de la trajectoire n'est pas touché. `R6` : après 1 tick, le front a parcouru **1,5 bloc** sur 12,
et la cible à 5,5 blocs est **encore intacte**. `R7` : au passage, elle tombe de 20,0 à **0,0 PV**, la
cible **sur le côté reste intacte**, et **20 ticks de plus ne produisent aucune frappe supplémentaire**.

**Pas de saut de cible** : le segment parcouru est **échantillonné** tous les 0,5 bloc. Sans cela, à
1,5 bloc/tick, le front traverserait une cible étroite **entre deux ticks** — l'onde raterait un
adversaire qu'elle a visiblement touché.

**Protections respectées** (`R8`) : l'onde ne modifie **aucun bloc** (25 → 25 autour du point
d'impact) et un **occupant d'une zone protégée est épargné**.

⚠️ **Leçon d'instrumentation** : au premier essai, `R7` échouait sans que je puisse dire *pourquoi*.
J'ai ajouté au service un **rapport de balayage** (`vues=`, `hors-portée=`, `cible-invalide=`,
`protégée=`, `dégâts-refusés=`) — il est apparu que la cible était **vue et frappée** correctement, et
que le vrai problème était ailleurs (la géométrie de `hasRoom`, qui empêchait la forme de s'ouvrir).
**Un « FAIL » sans détail coûte un cycle de devinettes** ; le compteur l'a transformé en diagnostic.

**Pourquoi ne pas réutiliser l'onde de Gura Gura** : celle-ci **fend le terrain**, donc elle dépend du
service de blocs temporaires et du réglage de destruction de Gura. Une onde de Daibutsu est un front de
**pression pure**. Les coupler forcerait à partager un réglage faux pour l'une des deux. Elles partagent
le **principe** (front voyageur, frappe unique, échantillonnage), pas le **réglage**.

### Passifs et retour à la normale *(FRUIT-HITO_HITO_DAIBUTSU-06)*

Deux états créés par ce fruit : `GiantForm` (forme) et `GiantShockWave` (ondes). Les deux sont nettoyés
par `endAll()` / `clearAll()` sur les trois chemins : échéance, épuisement, suppression/retrait.

⚠️ **La visibilité aux deux clients** (critère de -06) n'est **pas** vérifiable en headless : elle est
reportée au §6. Ce qui *est* vérifié ici : attributs réversibles, collisions cohérentes et épuisement.

## 5. Acquisition, rareté, progression *(FRUIT-HITO_HITO_DAIBUTSU-08)*

| Aspect | État |
|---|---|
| Rareté | **mythique** (Zoan mythique) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_hito_hito_daibutsu` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-HITO_HITO_DAIBUTSU-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **caméra au facteur 3,5** : c'est le point le plus délicat d'une forme géante. La caméra doit
      reculer sans clipper dans les blocs, et le point de vue ne doit pas passer à travers le modèle ;
- [ ] **collision** : le géant ne doit traverser ni les murs ni le sol, et ne doit pas rester coincé en
      revenant à la normale sous un plafond bas ;
- [ ] **transformation visible aux deux clients** (critère explicite de -06) ;
- [ ] le **refus** pour manque de place doit être lisible (message + absence de tout effet visuel) ;
- [ ] l'onde doit montrer **où est le front**, pas seulement qu'elle existe.

## 7. Tests *(FRUIT-HITO_HITO_DAIBUTSU-09)*

`/dftest daibutsu` — **9/9 PASS** :

| id | Résultat |
|---|---|
| `R0-decor-et-classification` | zoan=true, transformable=true |
| `R1-taille-bornee` | 40,0 demandée → **3,5** appliquée |
| `R2-espace-necessaire-verifie` | plaine **true** / couloir **false** |
| `R3-forme-ouverte-et-portee-coherente` | échelle posée **3,5**, portée **6,0 → 21,0** |
| `R4-maintien-et-epuisement` | 200,0 → **182,45 É**, forme qui retombe |
| `R5-attributs-revenus-a-la-normale` | 0 → 1 → **0**, modificateur étranger **survit** |
| `R6-front-determine-l-impact` | front à **1,5 bloc**, cible à 5,5 **intacte** |
| `R7-impact-unique-et-hors-trajectoire-epargne` | 20,0 → **0,0**, +0 frappe, côté intact |
| `R8-protections-respectees` | occupant **épargné**, **25 → 25** blocs |

Reste manuel : tout le §6.
