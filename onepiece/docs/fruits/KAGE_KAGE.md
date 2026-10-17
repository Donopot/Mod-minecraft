# État lot05 : implémenté, testé serveur et vu en laboratoire

Voir [validation](../B05-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-05.md). Acceptation artistique humaine en attente ; les affirmations historiques ci-dessous sont à lire avec les corrections de source du lot05.

---

# Contrat — Kage Kage no Mi (Shadow-Shadow Fruit)

Dix-neuvième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Paramecia** de Moria.
C'est le premier Fruit du projet dont l'effet **prend quelque chose à un autre joueur** — ce qui change
la nature du problème.

- **Item** : `minepiece:item_devil_fruit_kage_kage`
- **Techniques** : **6** (ver15), aucune ajoutée
- **Famille** : **`paramecia`** — argumentée, pas déduite (`Y0`)
- **Plafond d'ombres** : **3** · **État de victime** : 600 ticks, borné

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **détache et détient les ombres** des vivants autour de lui. Chaque ombre
détenue le renforce ; chaque victime perd la sienne.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 80 | plan ver15 |
| **Ombres détenues : plafond** | **3** | config |
| **Rayon d'acquisition** | **12,0 blocs** | config |
| **Shadow Asgard** | 100 ticks | plan ver15 |
| **Bonus par ombre** | **+3,0 dégâts** | config |
| Doppelman | invocation, **200 ticks** | CORE-021 |

**Contre-mesures** : eau de mer (pouvoir scellé → **toutes les ombres rendues**), retrait du Fruit (idem),
Haki, énergie insuffisante.

## 2. Classification : argumentée, pas déduite *(-02)*

Le plan avertit explicitement : « ajouter le tag correct **sans supposer que NON_CLASSE signifie
Paramecia** ». L'argument est donc écrit, et `Y0` lit les trois tags :

| Tag | Lu |
|---|---|
| `paramecia` | **true** |
| `logia` | false |
| `zoan` | false |

**L'argument** : ce Fruit ne transforme pas le corps en élément (donc pas **Logia**) et ne transforme pas
le porteur en animal (donc pas **Zoan**). Ce qu'il fait, c'est **agir sur les ombres** — une prise sur le
monde et sur **autrui**. C'est un **Paramecia**.

## 3. Catalogue

| Technique | Type | Dégâts | Portée | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **Attack Overhead** | mêlée | 15 | 12 | 50 | 120 | conservée |
| **Brick Bat** | projectile | 15 | — | 50 | 100 | conservée |
| **Tsuno Tokage** | mêlée | 15 | 4 | 50 | 100 | conservée |
| **Doppelman** | invocation | 15 | 6 | 60 | 140 | **invocation réelle** (CORE-021) |
| **Island Shatter Punch** | mêlée | 44 | 24 | 80 | 200 | conservée |
| **Shadow Asgard** | état | 0 | — | 80 | 240 | **→ acquisition des ombres** |

Aucun ajout : l'audit ne signale **aucun écart** pour ce Fruit. `Shadow Asgard` est la technique
d'acquisition — fidèle au canon, où elle consiste précisément à **absorber des ombres**.

## 4. Spécialisations

### L'acquisition et la restitution : « une ombre perdue est restituée dans tous les cas de fin » *(-03)*

C'est la différence de nature de ce Fruit avec tout le reste du projet. Une flamme qu'on éteint, une
glace qu'on fond : **personne ne les réclame**. Une **ombre prise** est retirée à un **autre joueur** —
elle a un propriétaire légitime, **absent de la décision**, qui n'a **aucun moyen de la reprendre**. Si
le mécanisme oublie de la rendre, ce joueur reste **diminué pour toujours** : sa pénalité survit à la mort
du voleur, à sa déconnexion, au retrait du Fruit, au redémarrage. Aucun message, aucun recours — il ne
saurait même pas pourquoi.

Le critère est donc écrit au superlatif : « dans **tous** les cas de fin prévus ». Chaque cas est
**énuméré**, et chacun a son test :

| Cas de fin | Preuve | Résultat |
|---|---|---|
| **`duree`** — fin de Shadow Asgard | `Y3` | marqueur parti, pénalité retirée, bonus retiré |
| **`mort`** du voleur | `Y4` | victime rendue, raison « mort » |
| **`suppression`** (pouvoir scellé) | `Y5` | victime rendue, raison « suppression » |
| **`retrait`** du Fruit (reset FRUIT-RESET-01) | `Y6` | victime rendue, 0 ombre détenue |
| **`cible-perdue`** (victime disparue) | `Y2` | la place se libère |
| **`deconnexion`** du voleur | à l'écouteur de sortie | — (même chemin que `mort`/`suppression`) |
| **`reset`** global | `restoreEverything` | balayage complet |

**Le cas le plus facile à oublier est `retrait`** — parce que le retrait ne passe pas par le service :
il faut l'y **brancher explicitement** (`DevilFruitPowerLogic.resetDevilFruitPowers`, étape 3 bis).
C'est le genre d'oubli qu'aucun test de pouvoir ne révèle : on ne le découvre qu'en voyant un joueur
diminué des jours plus tard.

### « On ne retire jamais un état qu'on n'a pas posé » *(-06)*

La victime porte un **marqueur dédié** (`shadowless`) et une pénalité d'armure portée par **nos**
modificateurs identifiés par ressource. La restitution retire **nominativement** ce que nous avons posé —
jamais « les modificateurs d'armure », jamais un effet vanille.

`Y7` le prouve : une potion **étrangère** et un bonus **étranger** posés avant le vol **survivent** au
vol **et** à la restitution, pendant que notre marqueur disparaît. C'est la différence entre **rendre une
ombre** et **tout reprendre**.

### Le plafond : une décision finie *(-05)*

`Y2` : après avoir présenté **6** cibles, **3** ombres détenues (plafond). Et la reprise de la **même**
victime est refusée. Un plafond n'est pas un réglage de confort : sans lui, l'acquisition serait
illimitée, donc l'avantage proportionnel au nombre de cibles présentes. Le plafond rend la décision
**finie** : *quelles trois ombres garder ?*

Une cible disparue **libère sa place** : garder une créance sur quelqu'un qui n'est plus là bloquerait
une place pour rien. `Y9` vérifie que **les alliés sont épargnés** — et cette règle vit **à l'endroit de
l'effet** (`ShadowLedger.steal`), pas seulement chez l'appelant : une règle qui n'existe que chez
l'appelant est **contournable**, et elle l'était.

### Le Doppelman : le plan décrivait la source, pas le port *(-04)*

Le constat : « `spawnMeleeVisual` crée `EntityGomuArmEntity` **sans IA** pour doppelman ». C'était l'état
de la **source ver15**. Le port fait passer `doppelman` par le chemin d'**invocation de CORE-021** — donc
il **agit**. `Y8` lit les trois propriétés exigées par le critère :

| Exigence | Mesure |
|---|---|
| « a un **propriétaire** » | `getOwner()` = le porteur ✅ |
| « le clone **agit** » | sélecteur d'objectifs **non vide** (IA) ✅ |
| « **expire** sans persistance involontaire » | durée de vie **200** ticks = celle du contrat ✅ |

### ⚠️ Une limite que le contrat doit dire

Si la **comptabilité disparaissait** (redémarrage du serveur), le marqueur et la pénalité de la victime
ne seraient plus suivis — mais l'état de la victime est **borné par sa durée** (600 ticks) : elle se
rétablit **d'elle-même**. C'est la seule garantie qui ne dépende pas de notre bon vouloir, et c'est
pourquoi toutes les durées du service sont bornées.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_kage_kage` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **l'ombre détachée doit être visible** sur la victime : sans retour visuel, le joueur dépouillé ne
      comprendra pas ce qui lui arrive (c'est le pire scénario du contrat) ;
- [ ] **Shadow Asgard** doit montrer les ombres absorbées et le renforcement ;
- [ ] **le Doppelman** doit se distinguer du porteur et **ressembler** à son modèle (`gecko_moria`).

## 7. Tests *(-09)*

`/dftest kage` — **10/10 PASS** :

| id | Résultat |
|---|---|
| `Y0-classification-argumentee` | paramecia **true**, logia false, zoan false |
| `Y1-acquisition-ombre-et-marque` | 1 ombre, registre des **deux** côtés, bonus posé |
| `Y2-plafond-et-pas-de-double-prise` | 6 cibles → **3** (plafond) · cible perdue → **1** |
| `Y3-restitution-par-echeance` | 1 rendue, marqueur **parti**, pénalité et bonus retirés |
| `Y4-restitution-par-mort` | rendue, raison « mort » |
| `Y5-restitution-par-suppression` | rendue, raison « suppression » |
| `Y6-restitution-par-retrait-du-fruit` | rendue, **0** détenue |
| `Y7-etats-externes-de-la-victime-intacts` | étrangers **intacts** avant et après |
| `Y8-doppelman-invocation-reelle` | propriétaire + **IA** + durée **200** |
| `Y9-invocations-alliances-et-plafond` | invocation du porteur **épargnée**, plafond tenu |

Reste manuel : tout le §6.

---

## ⚠️ Trois défauts réels trouvés par les tests (et corrigés)

1. **La restitution comptait comme faite sans nettoyer la victime.** Certains joueurs — dont les
   `FakePlayer` des preuves — sont **virtuels** : Fabric les construit **hors** de la liste d'entités,
   donc **aucune** recherche par identifiant ne les retrouve. Le service comptait l'ombre rendue,
   **retirait l'ombre de sa comptabilité**, mais laissait le marqueur et la pénalité sur la victime. Le
   pire cas possible : tout indiquait le contraire de la réalité. **Correction** : référence gardée à
   l'acquisition et **revalidée** à chaque usage, l'UUID restant la source de vérité.

2. **Un balayage par voleur laissait la comptabilité sale.** `restoreEverything` itérait les **voleurs**
   — or un voleur n'est pas toujours résolvable non plus. Résultat : `OWNER_OF` restait marqué, et le
   service **refusait ensuite tout nouveau vol sur cette victime, indéfiniment**. **Correction** :
   balayer par **victime**, toujours joignable par sa référence.

3. **La règle d'alliance ne vivait que chez l'appelant.** Elle était donc **contournable** — et le test
   la contournait. **Correction** : la règle vit **à l'endroit de l'effet** (`steal`), où aucun appelant
   ne peut l'éviter.
