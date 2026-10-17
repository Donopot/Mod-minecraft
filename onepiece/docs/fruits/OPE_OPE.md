**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Ope Ope no Mi (Op-Op Fruit)

Vingt-cinquième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Paramecia**, et
**premier Fruit dont une partie des capacités n'existe dans aucune version** : la source ne déclare
qu'**une** technique, une mêlée. La ROOM, l'échange de positions et la découpe sont **demandés par le
plan** (03/04/05) et donc écrits ici comme du contenu **neuf**, avec des valeurs **choisies** — jamais
présentés comme un portage.

- **Item** : `minepiece:item_devil_fruit_ope_ope`
- **Techniques** : **4** — **1** dans ver15 (`stoneopeope`), **3 ajoutées** sur demande du plan
- **Audit plan↔port** : **1061 valeurs conformes**, **12 écarts** (9 préexistants déclarés + **les 3
  ajouts**) ; **213 techniques vues** — le compte du registre, donc l'audit a bien tout regardé ; le
  contrôle inverse ne signale **que** les 2 remplacements déclarés de Kira
- **Famille** : **`paramecia`** — lue et **confirmée par ses conséquences** (`A6a`)
- **ROOM** : rayon **8,0** blocs (plafond dur **24,0**), durée **200** ticks (plafond **600**)
- **Échange** : distance max **8,0** blocs · **Découpe** : **160** ticks (plafond **400**), 4 cibles max

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **découpe l'espace** : il ouvre une aire chirurgicale (la ROOM) dans laquelle
il échange des positions et sectionne temporairement ce qu'il touche. Il ne devient ni intangible ni
animal : c'est un pouvoir **d'opération**, pas de matière.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coût ROOM / échange / découpe | **80 / 40 / 100** | **choisies** (config `devilfruit-ope.toml`) |
| Recharge ROOM / échange / découpe | 300 / 120 / 200 ticks | **choisies** |
| **ROOM : rayon / durée** | 8,0 blocs / 200 ticks | **choisies**, plafonnées **dans le code** |
| **Découpe : durée / cibles** | 160 ticks / 4 | **choisies**, plafonnées **dans le code** |
| `stoneopeope` | 20 dégâts / portée 6 / recul 1 / 60 É / 140 ticks | **ver15, inchangée** |

**Ce qui n'est PAS dans la source** — et qui est donc déclaré comme neuf, pas comme porté :

| Capacité | Demandée par | Contenu de ver15 |
|---|---|---|
| **ROOM** (centre, rayon, durée, affichage) | `FRUIT-OPE_OPE-03` | **aucune** (le constat du plan le dit) |
| **Échange de positions** | `FRUIT-OPE_OPE-04` | **aucune** |
| **Découpe temporaire** | `FRUIT-OPE_OPE-05` | **aucune** |

## 2. Classification : lue, puis **vérifiée par ses conséquences** *(-02)*

Le plan est explicite : « ajouter le tag correct **sans supposer que NON_CLASSE signifie Paramecia** ».

Le tag est **déjà présent** (`data/minepiece/tags/item/paramecia.json` — dossier **singulier**, le piège
qui avait rendu des tags morts pendant des semaines). Mais un tag écrit n'est pas un tag **lu** : `A6a`
lit le tag sur l'objet **et** mesure ce qu'il implique.

| Vérification | Attendu | Mesuré (`A6a`) |
|---|---|---|
| tag `paramecia` | vrai | **true** |
| tag `logia` / `zoan` | faux / faux | **false / false** |
| dégâts physiques subis | ×1,0 (un Logia serait ×0) | **×1,0** |
| pouvoir sous suppression | refusé | **ROOM refusée** |

La classification est donc **conséquente**, pas déclarative : elle change un comportement mesurable, et
c'est ce comportement qui est testé — pas la présence d'une chaîne dans un fichier.

## 3. Catalogue

| Id | Nom | Type | Dégâts | Portée | Énergie | Recharge | Origine |
|---|---|---|---|---|---|---|---|
| `stoneopeope` | Stone Ope Ope | MELEE | 20,0 | 6,0 | 60 | 140 | **ver15** |
| `room` | Room | BUFF | — | — | 80 | 300 | **plan 03** |
| `shambles` | Shambles | MELEE | 0,0 | 8,0 | 40 | 120 | **plan 04** |
| `amputation` | Amputation | MELEE | 12,0 | 5,0 | 100 | 200 | **plan 05** |

`stoneopeope` **n'est volontairement pas interceptée** : le contrat `TECH-01` demande d'en préserver la
portée et le type de dégâts, donc elle garde le comportement de mêlée générique. **C'est la ROOM qui
gouverne les techniques chirurgicales**, pas la mêlée d'origine — toucher cette dernière aurait été un
changement de comportement que le plan ne demande pas.

## 4. Spécialisations

### La ROOM : bornée, visible, et **elle gouverne l'espace** *(-03)*

Le plan : « ROOM avec **centre, rayon, durée** et **affichage pour les observateurs** », critère « la zone
est visible ; **les techniques sont refusées hors de ses règles spatiales** ».

- **Centre** = le porteur au moment de l'ouverture, **rayon** et **durée** = configuration, tous deux
  plafonnés **dans le code** (`HARD_MAX_RADIUS = 24,0`, `HARD_MAX_LIFETIME = 600`). Un `999,0` écrit dans
  le TOML devient **24,0** — vérifié (`A6b`), parce qu'un plafond qui ne vit que dans la prudence de qui
  édite un fichier n'est pas un plafond.
- **Affichage** = un **contour de 48 particules par tick** (`END_ROD`), émis côté serveur donc visibles
  de **tous** les observateurs, et **borné par construction**. Sa validation *visuelle* appartient au
  client (checklist §6) : en headless on prouve le budget, pas l'esthétique.
- **Refus hors des règles spatiales** : `shambles` et `amputation` exigent que **le porteur et la cible**
  soient dans la ROOM du porteur. Hors de l'aire, les deux retournent `false` **et ne laissent aucune
  trace** (`A6b`).

**La ROOM se ferme toute seule** — une aire persistante survit à ce qui l'a créée, donc elle doit prévoir
toutes ses fins, comme les autres maintiens du mod :

| Fin | Raison | Déclencheur |
|---|---|---|
| Échéance | `echeance` | durée écoulée (bornée) |
| Suppression | `suppression` | mer / scellement (`SEA_POWER`) |
| Mort du porteur | `mort` | santé ≤ 0 |
| Déconnexion | `deconnexion` | `ServerPlayConnectionEvents.DISCONNECT` |
| Changement de dimension | `dimension` | une ROOM ne s'exporte pas |
| Retrait du Fruit | `retrait` | reset central (FRUIT-RESET-01) |

### L'échange : validé **avant**, appliqué **atomiquement** *(-04)*

Le plan : « les deux positions sont **sûres**, **autorisées** et appliquées **atomiquement** ».

Le piège est de déplacer la première entité puis de découvrir que la seconde destination est mauvaise :
on a **déplacé quelqu'un pour rien**, et le joueur se retrouve dans un état qui n'existe dans aucune
règle du contrat. Les **deux** destinations sont donc validées **avant** le premier déplacement, sur
trois plans distincts :

1. **l'espace** — `level.noCollision(entité, boîte À LA DESTINATION)` et position chargée. Ce test
   compte : une entité de 1,95 bloc placée dans un plafond d'un bloc est « déplacée » puis **étouffe** ;
2. **l'autorisation** — `DevilFruitExplosions.isProtectedPosition`, le prédicat **déjà en service** pour
   les explosions : « protégé » n'a qu'**une** définition dans tout le mod ;
3. **la portée** — les deux entités dans la ROOM et à moins de 8,0 blocs.

`A6c` prouve les trois cas, dont l'**atomicité** : on **emmure la position du lanceur**, la destination de
la cible devient dangereuse, l'échange est refusé **et aucune des deux entités n'a bougé** (0,0 bloc).

### La découpe : temporaire, **sans corruption**, et toutes ses fins prévues *(-05)*

Le plan : « une découpe temporaire **ou un effet équivalent** sans **corruption des entités** », critère
« les cibles sont **restaurées** après **expiration, mort ou suppression** ».

La facilité serait de supprimer des membres ou des composants : on obtient une découpe spectaculaire et
une **entité abîmée** que rien ne sait remettre en état. L'effet retenu ne touche donc **qu'une chose**,
réversible par construction : **les membres qui frappent ne portent plus de coup** — la valeur
**effective** d'`ATTACK_DAMAGE` est ramenée à zéro par un **modificateur**, jamais en écrasant la valeur
de base. La différence est exactement celle entre « j'ajoute ce que je peux retirer » et « j'écrase la
valeur de quelqu'un d'autre ».

**L'ordre des opérations *est* la garantie** : on demande d'abord au **moteur** d'accepter le marqueur
(`addEffect` → booléen) et **seulement s'il l'accepte** on touche à l'attribut. L'inverse retirerait la
force de frappe à une cible qui a **résisté** — l'exact comportement que le contrat interdit.

**Toutes les fins** : `echeance`, `mort` (de la cible), `suppression` (du porteur), `deconnexion`,
`retrait`. Et la restauration n'est pas *déclarée*, elle est **constatée** : la valeur de frappe est
**recomparée** à celle d'avant, et deux compteurs distinguent les restaurations **exactes** des
**inexactes**. `A6d` mesure les **trois instants** — `3.0 → 0.0 → 3.0`, `INEXACTES=0`.

### Le passif et la contrepartie *(-06)*

Pas de passif propre : le contrat n'en définit aucun pour ce Fruit, et en **inventer** un serait ajouter
du contenu que le plan ne demande pas. La **contrepartie** en revanche est vérifiée : sous suppression
(mer / scellement), **la ROOM ne s'ouvre pas** (`A6a`) — un porteur immergé n'exerce pas son pouvoir.

## 5. Acquisition, rareté, progression *(-08)*

Identique aux autres Fruits du port : obtenable par les mêmes règles, **aucun déblocage par maîtrise**
n'est implémenté (le plan le note comme une lacune *générale* du port, pas propre à ce Fruit). Aucun
`-TECH-*-DEV` supplémentaire n'est revendiqué ici.

## 6. Validation visuelle *(-07 — exige un client)*

Checklist à passer en jeu, à attacher à `FRUIT-OPE_OPE-07` :

- [ ] icône, modèle, **deux mains**, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **contour de la ROOM** : 48 particules/tick à quatre hauteurs — visible et lisible *à distance* ;
- [ ] le **marqueur de porteur** (ROOM active) apparaît et **disparaît** à la fermeture ;
- [ ] l'échange échange bien ce qui est **vu** (la cible visée, pas une autre) ;
- [ ] sur une cible sectionnée : le **marqueur** est visible et ses coups ne portent plus ;
- [ ] à la restauration : aucun résidu visuel ni d'état.

## 7. Tests *(-09)*

**Section `A6` — 13 vérifications dans `dftest ope`** (`dftest all` passe de 221 à **234**), plus **2
phases de recette** : `3 bis` (échéance réelle, **1/1**) et `6 ter` (après redémarrage, **1/1**).
**Recette complète : 244/244 PASS, 0 FAIL.**

| Vérification | Ce qu'elle prouve |
|---|---|
| `A6a-classification-lue-et-coherente` | tags lus **et** conséquence mesurée (×1,0) |
| `A6a-suppression-bloque-le-pouvoir` | contrepartie du Fruit effectivement appliquée |
| `A6b-room-ouverte-et-bornee` | durée, rayon, marqueur, budget de particules |
| `A6b-plafond-dur-du-rayon` | **999,0 → 24,0** : la borne est dans le code |
| `A6b-refus-hors-room` | les règles spatiales gouvernent les techniques |
| `A6c-echange-atomique-refus` | une destination invalide ⇒ **aucun** des deux ne bouge |
| `A6c-echange-refuse-en-zone-protegee` | protection **partagée** avec les explosions |
| `A6c-echange-applique` | les deux positions sont réellement échangées |
| `A6d-section-appliquee` | **3,0 → 0,0** (les trois instants sont affichés) |
| `A6d-refus-sans-effet` | un refus ne consomme rien et ne laisse rien |
| `A6d-restauration-exacte` | **3,0 → 0,0 → 3,0**, `INEXACTES=0` : non-corruption **constatée** |
| `A6e-temoins-posees` | témoins à durée courte armés pour la relecture ultérieure |
| `A6f-retrait-ferme-et-restaure` | le reset central ferme la ROOM **et** restaure la cible |
| `A6e-echeance-reelle…` *(phase 3 bis)* | ROOM fermée par **`echeance`**, frappe revenue **d'elle-même** |
| `A6g-redemarrage…` *(phase 6 ter)* | aucun état **fantôme** après reboot, configuration **rendue** |

**La preuve d'échéance se fait AVANT le redémarrage, et c'est délibéré.** Une invocation de test tient
dans **un seul tick** : elle ne peut pas observer une durée. La portée `ope` arme donc une ROOM et une
section à **70 ticks** (durées écrites dans le TOML), et la phase **3 bis** les relit **plus de 70 ticks
serveur plus tard**, dans la **même vie du serveur** — un état tenu en mémoire ne traverse pas un
redémarrage, donc exiger une durée après coup n'aurait aucun sens. La phase **6 ter** vérifie l'inverse,
et c'est une garantie en soi : après reboot il ne reste **aucune** aire ni section fantôme, et le harnais
a **rendu** la configuration qu'il avait empruntée — deux exécutions successives mesurent la même chose.

## Résultat de la recette

| Phase | Attendu | Mesuré |
|---|---|---|
| `dftest all` | — | **234/234 PASS** |
| `3 bis. dftest ope_expiry` | échéance **réelle** | **1/1 PASS** — ROOM fermée « **echeance** », section rendue « **echeance** », frappe **3,0**, `INEXACTES=0` |
| `6 ter. dftest ope_restart` | aucun état **fantôme** | **1/1 PASS** — 0 ROOM, 0 section, configuration **rendue** (8,0 / 200 / 160) |
| **Global** | — | **244/244 PASS, 0 FAIL** |
| **Stabilité** (exigence née du défaut « zombie bébé ») | 3 passages consécutifs | **244/244 × 3** — 4 passages verts au total, échéance et redémarrage verts à chaque fois |

L'échéance est **observée dans le temps réel** (au-delà des 70 ticks armés) et non supposée : c'est la
différence entre « le code prévoit une durée » et « la durée se produit ».

## Dette signalée

**1. Une forme partagée avec `GravityField` (Zushi).** `GravityField.Field` et `OpeRoom.Room` décrivent la
même chose : une aire persistante avec dimension, centre, rayon et durée de vie, entretenue par un
`ServerTickEvents` et affichée en particules. Les **politiques** diffèrent (gravité vs chirurgie), mais le
**mécanisme** est le même. Un service d'aire commun serait plus propre — il toucherait un Fruit **déjà
livré et testé** (Zushi), donc il est **proposé** plutôt qu'imposé. C'est la même famille de dette que
`Roots`/`StickyState`/`SmokeCloud` pour les maintiens temporaires : le projet compte maintenant **deux**
motifs répétés, ce qui commence à justifier un service partagé plutôt que deux copies.

**2. Le rayon de la ROOM n'est pas restreint par les zones protégées.** L'**échange** refuse une
destination protégée (`A6c`), mais une ROOM peut être **ouverte** au-dessus d'une zone protégée : comme
elle ne modifie rien (ni blocs, ni terrain), c'est cohérent — mais si une capacité future devait modifier
le terrain à l'intérieur, la vérification devrait être ajoutée **au moment de l'ouverture**, pas après.
