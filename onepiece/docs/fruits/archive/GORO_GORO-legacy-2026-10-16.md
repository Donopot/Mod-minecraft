# Contrat — Goro Goro (Rumble-Rumble Fruit)

Sixième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Fruit **Logia** de la
foudre — le premier de la série à porter une **résistance** (intangibilité), et celui dont le plan
signalait le défaut le plus net : une technique déclarée « BUFF » dont l'effet n'avait **aucun
comportement**.

- **Item** : `minepiece:item_devil_fruit_goro_goro`
- **Techniques** : 7 (ver15) — Vari, El Thor, Sango, Mamaragan, Kari Krash, Goro Goro Passive, Raigo
- **Famille** : `logia` (+ transformable)

---

## 1. Identité, limites et contre-mesures *(FRUIT-GORO_GORO-01)*

**Identité** — Logia de la foudre. Le corps est un orage : les attaques physiques le traversent, et
la foudre frappe à distance ou de l'intérieur.

**Limites** :

| Limite | Valeur | Source |
|---|---|---|
| Énergie disponible | 200 | CORE-005 |
| Coûts des techniques | 40 à 150 | plan ver15 |
| Recharges | 100 à 400 ticks | plan ver15 |
| **Rebonds de Sango** | 1 impact + **3 rebonds** | contrat (§4) |
| Portée d'un rebond | 6 blocs | contrat (§4) |
| **Impacts de Mamaragan** | **3 maximum** | contrat (§4) |
| Délai d'annonce | 10 ticks | contrat (§4) |

**Contre-mesures** :

- **Haki de l'Armement** → perce l'intangibilité Logia (CORE-029) ;
- **Eau de mer** → pouvoir scellé : plus d'éclairs, et l'orage de Mamaragan **cesse** (preuve `H4`) ;
- **Yami Yami** → annule les réductions (CORE-029) ;
- **Énergie insuffisante** → refus sans coût (CORE-005) ;
- **Retrait du Fruit** → reset complet (FRUIT-RESET-01) ; l'état de Mamaragan est oublié
  (`MamaraganMobEffect.clearState`).

## 2. Classification *(FRUIT-GORO_GORO-02)*

ver15 dit **`LOGIA`** — c'est exact, et c'est **vérifié** plutôt que recopié : le Fruit est classé
Logia, il porte donc l'intangibilité aux attaques physiques lue par le code
(`DevilFruitPowerLogic.computeDamageMagnification`) et le rendu élémentaire. Le tag `minepiece:logia`
est posé et lu.

Vérifié : `python3 tools/fruit_audit.py --fruit GORO_GORO` → **0 divergence**, **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Vari** | frappe | conservée (cône) | 14 | 24 | 50 | 100 |
| **El Thor** | frappe | conservée (cône) | 24 | 8 | 60 | 140 |
| **Sango** | frappe | **éclair à rebonds** | 18 → 7,6 | 10 (+ 6/rebond) | 50 | 120 |
| **Mamaragan** | **BUFF** | **orage à comportement** | 10/impact | 8 (annonce) / 2 (impact) | 50 | 120 |
| **Kari Krash** | frappe | conservée (cône) | 18 | 12 | 50 | 120 |
| **Goro Goro Passive** | BUFF | conservée (Célérité) | — | — | 40 | 100 |
| **Raigo** | projectile | conservée | **60** (plafond CORE-025) | — | 150 | 400 |

⚠️ **Raigo** : ver15 déclare `800` dégâts. Le plafond d'équilibrage CORE-025 les ramène à **60**
(3 × les PV d'un joueur). L'audit lit la valeur ver15 ; l'écart est **voulu et déjà verrouillé** par
l'équilibrage, pas une divergence de portage.

## 4. Spécialisations

### Sango : l'éclair qui rebondit *(FRUIT-GORO_GORO-03)*

Le plan : « créer des éclairs avec **rebonds limités** entre cibles autorisées », critère « nombre de
rebonds, portée et dégâts correspondent au contrat et **excluent les alliés** ».

ver15 ne produisait qu'**un seul impact** dans un cône. Les trois valeurs du contrat sont donc des
**décisions documentées** (le plan laisse les nombres au contrat, il exige qu'ils soient tenus) :

| Paramètre | Valeur | Rôle |
|---|---|---|
| Rebonds | **3** | donc 4 cibles au maximum |
| Portée d'un rebond | **6 blocs** | de cible en cible |
| Atténuation | **× 0,75** par saut | 18,0 → 13,5 → 10,1 → 7,6 |

**Deux invariants**, qui sont le cœur du critère :

1. chaque saut ne vise qu'une cible **autorisée** (`CombatSkillLogic.isValidTarget`) — un allié n'est
   **jamais** un maillon, même s'il est le plus proche ;
2. une cible déjà frappée n'est **jamais** reprise : la chaîne ne boucle pas.

**Preuves** :
- `I1` : 4 maillons, dégâts décroissants **18,0 → 13,5 → 10,13 → 7,59**, une cible à 30 blocs exclue ;
- `I2` : un allié placé à **1 bloc** (plus près que toute cible) n'entre jamais dans la chaîne ;
- `I3` : chaîne appliquée, **49,22** dégâts cumulés, borne haute 72,0.

### Mamaragan : un effet marqueur devient un orage *(FRUIT-GORO_GORO-05)*

Le plan est explicite : « Mamaragan est déclaré BUFF et son effet utilise `SimpleMarkerMobEffect`
**sans comportement propre** », à corriger en « donnant un comportement à Mamaragan plutôt qu'un
simple effet marqueur », avec pour critère « des impacts de foudre **annoncés** se produisent pendant
une **durée bornée** et **cessent à la suppression** ».

**Ce qui a changé** : `SimpleMarkerMobEffect` → **`MamaraganMobEffect`**, un vrai effet qui porte sa
logique.

**Pourquoi le comportement vit dans l'effet** — la durée de l'effet **est** le bornage demandé : en
sortant de l'effet, plus rien ne tourne. Aucune minuterie à gérer, aucun nettoyage à oublier.

**Le cycle en deux temps** — c'est ce qui rend l'impact *annoncé* :

1. **Annonce** — la cible autorisée la plus proche dans 8 blocs reçoit une croix d'étincelles et un
   coup de tonnerre à l'endroit visé. **Aucun dégât à cet instant.**
2. **Frappe** — `10 ticks` plus tard, tout ce qui se trouve dans **2 blocs** du point annoncé est
   frappé, sauf le porteur et ses alliés. Le temps de s'écarter est réel.

**Décision de contrat** — ver15 ne donne **aucun dégât** à Mamaragan (BUFF, `degats=0`). Les valeurs
(10 par impact, 3 impacts maximum) sont donc des **choix**, assumés et verrouillés par les tests.

**Preuves** :
- `H1` : annonce posée sur la cible, **0 dégât** à cet instant ;
- `H2` : frappe 10 ticks après → 20,0 → 10,16 PV (−9,84 encaissés sur 10 nominaux, le reste absorbé
  par l'armure) ;
- `H3` : sur l'équivalent d'une durée complète, **3 impacts** — plafond respecté ;
- `H4` : pouvoir scellé par la mer → l'annonce en cours est **annulée** et aucune frappe n'a lieu ;
- `H5` : impact au corps à corps → l'**allié est épargné** (`pv=30,0` inchangés), la créature hostile
  passe de 20,0 à 10,16 PV, le porteur n'est **jamais** frappé par sa propre foudre.

### Ce qui reste ouvert

**Kari Krash — déplacement électrique** *(FRUIT-GORO_GORO-04)*. Le plan demande « un déplacement
électrique borné et annoncé visuellement », critère « destination sûre sans traversée de mur non
autorisée ni charge de chunks distante ». La technique est **conservée en frappe** pour l'instant : la
mécanique de déplacement existe déjà (`GomuArmStrike` non, mais `DevilFruitBehaviours.aroMoveDash`,
écrit pour Aro Aro, en fournit exactement le patron — pas de 0,25 bloc, arrêt sur collision, chunk
chargé vérifié). C'est le prochain pas de ce fruit, et il est **explicitement non revendiqué** ici.

**Exceptions Logia** *(FRUIT-GORO_GORO-06)*. Le plan relève que l'intangibilité s'applique « à tous
les fruits du tag LOGIA sans exceptions propres complètes ». L'intangibilité et ses percements (Haki,
eau, Yami) sont couverts par CORE-029 et vérifiés par `/dftest haki` ; les **exceptions propres à la
foudre** (immunité à la foudre elle-même, conduction dans l'eau) ne sont **pas** implémentées — c'est
un manque identifié, pas un oubli dissimulé.

## 5. Acquisition, rareté, progression *(FRUIT-GORO_GORO-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) — Logia de la foudre |
| Acquisition | coffre de structure (CORE-023), ou `/devilfruit give item_devil_fruit_goro_goro` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-GORO_GORO-07 — P2, exige un client)*

- [ ] icône du Fruit, inventaire et au sol ; Fruit tenu deux mains, 1ʳᵉ et 3ᵉ personne ;
- [ ] **rendu élémentaire Logia** : le corps doit se voir traversé par les attaques physiques ;
- [ ] **Sango** : les étincelles doivent **rebondir** visiblement de cible en cible ;
- [ ] **Mamaragan** : la croix d'annonce doit être **lisible** 10 ticks avant l'impact, et le
      joueur doit pouvoir s'écarter réellement ;
- [ ] Raigo : visuel de projectile de foudre ;
- [ ] intangibilité : vérifier à deux clients qu'une épée passe à travers, et que le Haki la rend
      efficace.

## 7. Tests *(FRUIT-GORO_GORO-09)*

`/dftest goro` — **9/9 PASS** :

| id | Résultat |
|---|---|
| `H0-decor-pret` | terrain d'essai dans le chunk de spawn, chargé et tické |
| `H1-mamaragan-annonce-avant-la-frappe` | annonce sur la cible, **0 dégât** à cet instant |
| `H2-mamaragan-frappe-apres-l-annonce` | frappe après 10 ticks, −9,84 PV sur 10 nominaux |
| `H3-mamaragan-borne` | 3 impacts maximum sur une durée complète |
| `H4-mamaragan-cesse-a-la-suppression` | annonce annulée, aucune frappe sous SEA_POWER |
| `H5-mamaragan-epargne-porteur-et-allies` | allié 30,0 inchangé · hostile 20,0 → 10,16 · porteur intact |
| `I1-eclair-a-rebonds-bornes` | 4 maillons, dégâts décroissants, cible à 30 blocs exclue |
| `I2-allies-exclus-de-la-chaine` | allié à 1 bloc jamais maillon |
| `I3-degats-de-la-chaine` | 49,22 cumulés ≤ 72,0 |

⚠️ **Deux pièges de harnais rencontrés et corrigés ici** :
1. **Toute la séquence s'exécute dans un seul tick serveur** : une créature déjà blessée est protégée
   par la fenêtre d'invulnérabilité vanille (20 ticks) et absorbe la frappe suivante — le test
   mesurait l'immunité, pas l'épargne des alliés. Solution : frapper une créature **fraîche** (le
   champ `invulnerableTime` n'est pas accessible publiquement).
2. **Un build en échec fait tourner un jar périmé** : deux itérations ont testé l'ancien jar sans
   aucun signal, les résultats paraissant parfaitement cohérents. Le script de recette vérifie
   désormais `BUILD SUCCESSFUL` **avant** de démarrer le serveur.

Reste manuel : rendu élémentaire, rebonds visibles, lisibilité de l'annonce (§6).
