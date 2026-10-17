**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Bomu Bomu (Bombe-Bombe Fruit)

Quatrième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. C'est le premier Fruit dont
les techniques ne sont pas de simples frappes mais des **explosions**, et le premier à poser la question
de la **résistance du porteur** à ses propres dégâts.

- **Item** : `minepiece:item_devil_fruit_bomu_bomu`
- **Techniques** : 5 (ver15) — Nose Fancy Cannon, Kick Bomb, Punch Bomb, Bomb Overhead, Zenshin Kibaku
- **Famille** : `paramecia` · **Transformation** : non

---

## 1. Identité, limites et contre-mesures *(FRUIT-BOMU_BOMU-01)*

**Identité** — Paramecia de la Bombe : le corps entier devient une charge explosive. Aucune
intangibilité, aucune transformation ; la puissance vient de la mise à feu.

**Limites** :

| Limite | Valeur | Source |
|---|---|---|
| Énergie disponible | 200 | CORE-005 |
| Coûts des techniques | 40 à 70 | plan ver15 |
| Recharges | 80 à 180 ticks | plan ver15 |
| **Dégâts aux blocs** | **désactivés par défaut** | réglage `block-damage` (-05) |
| Résistance des blocs cassables | ≤ 3,0 | réglage `block-damage-resistance-limit` |
| Rayons d'explosion | 2,5 à 6,0 blocs | contrats (-03) |

**Contre-mesures** :

- **Eau de mer** → pouvoirs scellés, aucune explosion (CORE-007) ;
- **Zones protégées** → aucune destruction de bloc, même quand les dégâts aux blocs sont autorisés
  (preuve `D5`) — la protection d'île existante est consultée, pas dupliquée ;
- **Alliés** → épargnés (preuve `D6`) ;
- **Énergie insuffisante** → refus sans coût (CORE-005) ;
- **Retrait du Fruit** → reset complet (FRUIT-RESET-01) : aucun état permanent, donc rien de propre à
  nettoyer — les explosions sont instantanées par nature.

## 2. Classification *(FRUIT-BOMU_BOMU-02)*

ver15 laisse ce Fruit **`NON_CLASSE`**. Décision : **`paramecia`**, **sans supposer** que « non classé »
signifie Paramecia (c'est l'avertissement explicite de l'action) — le raisonnement est le même que pour
Bisu Bisu, mais il est vérifié fruit par fruit : ni intangibilité (`logia`), ni forme animale (`zoan`).
Le tag `minepiece:paramecia` est posé **et lu par le code** (`DevilFruitPowerLogic.family()`).

Résistance aux dégâts : **aucune**. Le porteur n'est pas immunisé aux explosions en général (voir §4).

## 3. Catalogue final *(FRUIT-BOMU_BOMU-01)*

| Technique | Type ver15 | Dégâts | Origine retenue | Rayon | Recul | Énergie | Recharge |
|---|---|---|---|---|---|---|---|
| **Nose Fancy Cannon** | projectile | 2,2 | **projectile** (impact) | 2,5 | 0,5 | 40 | 80 |
| **Punch Bomb** | frappe | 15 | **contact** (poing) | 3,0 | 1,5 | 50 | 120 |
| **Kick Bomb** | frappe | 16 | **contact** (pied) | 3,5 | 2,0 | 50 | 120 |
| **Bomb Overhead** | frappe | 18 | **aérienne** | 4,0 | 2,0 | 60 | 140 |
| **Zenshin Kibaku** | frappe | 30 | **corporelle** | 6,0 | 1,5 | 70 | 180 |

⚠️ **Dégâts, portée de ciblage, recul, coûts et recharges sont ceux de ver15**, inchangés. Ce qui est
ajouté est le **point d'origine** et le **rayon d'explosion**, que ver15 ne distinguait pas : les cinq
techniques passaient par le même cône de frappe générique.

## 4. Spécialisations

### Trois origines d'explosion *(FRUIT-BOMU_BOMU-03)*

Le plan refuse explicitement le moteur générique : « les déclarations actuelles utilisent le moteur
générique et ne prouvent pas cette mécanique spécifique », et demande de « différencier explosion de
contact, projectile et explosion corporelle », avec pour critère « chaque technique a un point
d'origine, rayon et coût propres ».

| Origine | Où naît l'explosion | Techniques |
|---|---|---|
| **CONTACT** | au point d'impact du coup, trouvé **par lancer de rayon** jusqu'à la portée déclarée (mur ou créature) ; à défaut juste devant le porteur | Punch Bomb, Kick Bomb |
| **AÉRIENNE** | devant **et au-dessus** (décalage vertical), pour une bombe lancée par-dessus | Bomb Overhead |
| **CORPORELLE** | **centrée sur le corps** du porteur | Zenshin Kibaku |
| **PROJECTILE** | au point d'impact du projectile | Nose Fancy Cannon |

L'origine **corporelle** est celle qui prouve le mieux la différenciation : centrée sur le corps, elle
atteint une cible située **derrière** le porteur, ce qu'un cône de frappe ne peut pas faire.

**Preuves** :
- `D1` : les 3 origines exigées (contact, projectile, corporelle) sont présentes, 4 origines distinctes
  au total, **5 rayons distincts** (chacun le sien) ;
- `D2` : l'origine corporelle est à **0,0 bloc** du centre du corps en Y **et** à l'horizontale (donc
  atteint aussi derrière) ; l'origine de contact est à **1,0 bloc devant** les yeux.

> **Note sur la 4ᵉ origine.** Le plan nomme trois types ; « Bomb Overhead » en impose un quatrième
> (aérien), justifié par son nom même. C'est un **sur-ensemble** de la demande, assumé et documenté.

### Résistance du porteur, sans immunité générale *(FRUIT-BOMU_BOMU-04)*

Le critère est précis : « les sources prévues sont traitées **sans immunité générale involontaire** ».
C'est le piège classique — accorder une réduction de dégâts globale pour protéger le porteur de sa
propre technique, et le rendre du même coup immunisé à la TNT, aux creepers et aux autres Fruits.

**Implémentation** : l'exclusion du porteur vit **dans une seule méthode** (`DevilFruitExplosions.
wouldAffect`), appliquée uniquement par les explosions de ce Fruit. **Aucun crochet global** sur les
dégâts n'est posé. Le porteur est donc épargné par ce qu'il déclenche, et **rien d'autre**.

**Preuves** (`D3`) :
- sa propre explosion l'épargne → **vrai** ;
- l'explosion d'un **autre** joueur le touche → **vrai** (même endroit, même explosion : c'est bien la
  propriété de l'explosion qui décide, pas le lieu ni un état global) ;
- un témoin (zombie) dans la même explosion passe de **20,0 à 10,83 PV** → les dégâts sont réels, donc
  l'épargne du porteur est une exclusion ciblée, pas un désarmement de l'explosion.

### Réglages : blocs, alliés, zones protégées *(FRUIT-BOMU_BOMU-05)*

Critère : « les réglages sont respectés lors de **toutes** les explosions du fruit ». Un seul fichier
les porte : `config/opworldlike/devilfruit-explosion.toml`.

| Réglage | Défaut | Effet |
|---|---|---|
| `block-damage` | **faux** | une technique de combat ne troue pas le terrain sans décision explicite |
| `block-damage-resistance-limit` | 3,0 | laisse passer terre/pierre/bois, épargne les blocs durs |
| `spare-allies` | vrai | alliés du porteur et membres de la même équipe épargnés |
| `owner-immune-to-own` | vrai | porteur épargné par **ses** explosions uniquement |
| `protected-zones` | vide | zones `dimension@x1,y1,z1,x2,y2,z2` (séparateur `@` : l'identifiant de dimension contient déjà un `:`) |

**Preuves** :
- `D4` : `block-damage=false` → bloc **intact** ; `block-damage=true` → bloc **cassé** ;
- `D5` : `block-damage=true` **et** zone protégée déclarée → bloc **intact** ;
- `D6` : une invocation alliée du porteur est **épargnée** (30,0 → 30,0 PV), une créature ennemie est
  **touchée**.

⚠️ **Ce qui n'est pas prouvé en headless** : la seconde notion d'allié — **même équipe de scoreboard** —
est implémentée dans la même condition (elle reprend la règle d'alliance de CORE-021,
`SummonEntity.canAttack`), mais `addPlayerToTeam` n'inscrit pas les joueurs factices du harnais
(membres déclarés = 0). La garantie testée est celle des alliés-invocations ; la règle d'équipe reste à
valider avec deux clients réels (§6).

**Réutilisation plutôt que duplication** : les zones protégées s'appuient d'abord sur la garde
existante du projet (`ProtectedSkillEffectGuard.shouldBlockSkillEffect`, celle qui protège déjà les
constructions des compétences destructrices), puis sur les zones déclarées. Deux systèmes de protection
parallèles finiraient par diverger.

### Passifs et contreparties *(FRUIT-BOMU_BOMU-06)*

**Aucun passif permanent.** C'est une décision, pas un oubli : toutes les capacités du Fruit sont des
explosions **instantanées**, déclenchées par une technique et aussitôt terminées. Il n'y a donc rien à
appliquer en continu, ni durée à suivre, ni état à retirer lors d'une suppression — la contrepartie
tient uniquement aux coûts, aux recharges et aux réglages ci-dessus.

## 5. Acquisition, rareté, progression *(FRUIT-BOMU_BOMU-08)*

| Aspect | État |
|---|---|
| Rareté | `paramecia` → palier **commun** (poids 60), loot des coffres (CORE-023) |
| Acquisition | coffre de structure (2 %), ou `/devilfruit give item_devil_fruit_bomu_bomu` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-BOMU_BOMU-07 — P2, exige un client)*

- [ ] icône du Fruit, inventaire et au sol ;
- [ ] Fruit tenu première **et** troisième personne, deux mains ;
- [ ] infobulle (nom FR) ;
- [ ] **trois origines visibles** : contact devant, aérienne au-dessus, corporelle centrée sur le corps ;
- [ ] projectile : traînée et explosion à l'impact ;
- [ ] réglage `block-damage=true` : cratère cohérent, et **aucun** cratère dans une zone protégée ;
- [ ] zone protégée (deux clients, même équipe) : un allié ne perd **aucun** PV ;
- [ ] vérifier qu'une TNT blesse bien le porteur (absence d'immunité générale).

## 7. Tests *(FRUIT-BOMU_BOMU-09)*

`/dftest bomu` — **7/7 PASS** :

| id | Résultat |
|---|---|
| `D0-decor-pret` | terrain d'essai dans le chunk de spawn, chargé et tické |
| `D1-origines-et-rayons-propres` | 3 origines exigées présentes, 4 au total, **5/5 rayons distincts** |
| `D2-geometrie-des-origines` | corporelle à 0,0 du corps ; contact à 1,0 devant les yeux |
| `D3-porteur-epargne-sans-immunite-generale` | épargné par la sienne, **touché par celle d'un autre** |
| `D4-reglage-des-blocs-respecte` | intact à `false`, cassé à `true` |
| `D5-zone-protegee-respectee` | intact malgré `block-damage=true` |
| `D6-allies-epargnes` | allié 30,0 → 30,0 PV, ennemi touché |

Reste manuel : rendu des trois origines, projectile, cratères, deux clients, TNT (§6).
