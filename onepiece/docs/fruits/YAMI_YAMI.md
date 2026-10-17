**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Mise à jour alpha.8 — lot01,16octobre2026

Les4techniques de ce fruit ont été reprises avec les quatre autres fruits du lot. Les budgets, routes, adaptations et limites actuelles sont décrits dans [B01-IMPLEMENTATION](../B01-IMPLEMENTATION-2026-10-16.md), qui remplace les anciennes descriptions de comportement ci-dessous lorsqu’elles divergent. [Résultats et hash des tests](../B01-VALIDATION-2026-10-16.md). Implémentation, preuve serveur, observation client et acceptation artistique restent distinctes ; l’essai humain est attendu.

---

# Contrat — Yami Yami no Mi (Dark-Dark Fruit)

Vingt-septième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Logia** — et le seul
dont l'exception à la règle générale est **l'inverse** des autres : il **ne rend pas intangible**. C'est
aussi le Fruit dont le plan demande le plus de mécaniques neuves : **cinq** (attraction bornée,
absorption bornée, scellement temporaire, exception d'intangibilité décidée, exceptions mesurées), pour
seulement **quatre techniques**.

- **Item** : `minepiece:item_devil_fruit_yami_yami`
- **Techniques** : **4** (ver15), aucune ajoutée ni retirée
- **Famille** : **`logia`** — mais **sans intangibilité** (exception décidée, voir §2)
- **Contrepartie** : les ténèbres **subissent davantage** — physique **×2,0** (valeur **décidée**, réglable)
- **Attraction** : rayon **32** blocs (plafond dur **64**), force **≤ 0,8**/tick (plafond **2,0**), **20** ticks
- **Absorption** : **6** coups (plafond **24**), **20 É** par coup absorbé
- **Scellement** : **100** ticks (plafond **300**)
- **Rendu** : particules **du jar source** (`dark`, `light_large`, `light_small`), 25 textures extraites

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **est un trou**. Il attire, il avale, et son contact **scelle** le pouvoir des
autres. Il paie en retour trois prix : **aucune intangibilité** (les coups portent), une **douleur
amplifiée**, et un pouvoir qui **cesse** sous suppression comme les autres.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 150 | **ver15** (inchangés) |
| Recharges | 100 à 400 ticks | **ver15** (inchangées) |
| **Douleur subie** | physique ×2,0 | **décidée** (absente de ver15 et du plan), plafond dur ×3 |
| **Attraction** | 32 blocs / 0,8 / 20 t | **ajoutée** (plan -03), plafonnée dans le code |
| **Absorption** | 6 coups / 20 É | **ajoutée** (plan -04), plafonnée dans le code |
| **Scellement** | 100 ticks | **ajouté** (plan -05), plafonné dans le code |

## 2. L'exception **décidée**, et elle est l'inverse des autres *(-02 / -05 / -06)*

Le plan demande de « décider explicitement l'exception à l'intangibilité Logia générique » et que la
classification soit « cohérente avec la **résistance aux dégâts** ».

**Décision : ce Fruit n'accorde aucune intangibilité.** C'est la fidélité à la source, et c'est la seule
lecture cohérente du tag `logia` qu'il porte pourtant — le CSV et le jar d'origine le classent Logia,
mais un porteur de Yami **encaisse** les coups. La règle générique (`logia` + physique + sans Haki → ×0)
est donc **explicitement écartée pour ce Fruit**, et **seulement pour lui**.

**La contre-épreuve est le cœur de la preuve** — et il faut **trois** valeurs, jamais une :

| Mesure (`A8a`) | Résultat | Ce que ça élimine |
|---|---|---|
| coup physique sur un porteur de **Yami** | **×2,0** | — |
| **même** coup sur un témoin **sans Fruit** | **×1,0** | un refus général du moteur |
| **même** coup (attaquant **neutre**) sur un **autre Logia** (Goro) | **×0,0** | l'idée que la règle générale serait cassée |

**Contrepartie assumée** : les ténèbres subissent **davantage** (×2,0). C'est une **valeur décidée** —
elle n'est ni dans ver15 ni dans le plan —, elle est **réglable** (`pain_multiplier = 1.0` la désactive)
et **bornée dans le code** (≤ ×3). C'est une **pénalité**, donc elle **ne dépend pas de la suppression** :
un pouvoir scellé ne rend pas la douleur plus douce.

**Annulation sans nivellement** (`A8a bis`) : un **attaquant** porteur de Yami annule l'intangibilité de
sa victime (×0 → ×1) **sans rabaisser** ce qui était déjà plus élevé (un porteur de Yami frappé par un
autre Yami garde son ×2). Un nivellement naïf (`mult = 1.0`) aurait **adouci les ténèbres par les
ténèbres** — corrigé en `max(mult, 1.0)`.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Type | Dégâts | Portée/vie | Énergie | Recharge |
|---|---|---|---|---|---|---|
| `kurouzu` | Kurouzu | MELEE | 6,0 | 10,0 | 50 | 100 |
| `blackhole` | Black Hole | MELEE | 24,0 | 48,0 | 80 | 200 |
| `darkball` | Dark Ball | PROJECTILE | 40,0 | 80 ticks | 70 | 160 |
| `darkmatter` | Dark Matter | PROJECTILE | **200,0** | 80 ticks | 150 | 400 |

**Rendu élémentaire repris du jar source** : les quatre techniques portent désormais `dark`, la particule
du mod (15 images), et les rayons de Pika Pika portent `light_large` / `light_small` (5 + 5 images). Les
définitions et les **25 textures** sont **extraites du jar d'origine**, sous leur nom d'origine, sans
redessin.

## 4. Spécialisations

### L'attraction : progressive, bornée, et jamais à travers un mur *(-03)*

Le plan : « une **attraction progressive** avec **force maximale** et **rayon borné** », critère « les
cibles suivent une **trajectoire cohérente** sans traverser les murs de **façon involontaire** ».

Trois garde-fous, un par moitié du critère :
1. **Progressive** — la force part d'un dixième de sa valeur et croît jusqu'au maximum. À pleine force
   dès le premier tick, ce ne serait pas une attraction, ce serait une téléportation lente.
2. **Bornée** — rayon et force plafonnés **dans le code**, et la traction **s'arrête à 1,5 bloc** du
   porteur : sans ce seuil, la cible traverse le porteur et ressort derrière lui — exactement la
   « trajectoire incohérente » que le critère interdit.
3. **Jamais à travers un mur** — la cible doit être **en vue**. La ligne de vue est celle du moteur, donc
   la même règle que la frappe et que l'arrêt des rayons de Pika Pika.

`A8b` mesure **quatre propriétés** dans une sonde déterministe (12 pas appelés à la main) : vitesse
**dirigée** vers le porteur à **100 %**, **croissante** (0,12 → 0,15), **bornée** par le plafond (0,8), et
**nulle** derrière un mur comme sous le seuil de contact (0,0 dans les deux cas).

### L'absorption : une capacité dépassée ne supprime plus rien *(-04)*

Le plan : définir les **attaques absorbables**, la **capacité** et le **coût** ; critère « une capacité
dépassée ne supprime pas tous les projectiles ou dégâts **sans limite** ».

- **Absorbable** = les coups **physiques** et les **projectiles**, et rien d'autre. La lumière, le feu,
  les explosions et la magie **traversent** les ténèbres. Cette définition a **un seul propriétaire**
  (`DevilFruitPowerLogic.absorbable`) et elle est **mesurée source par source**, contre un témoin.
- **Capacité** : un nombre de coups **fixé** (6, plafond dur 24) et **coûté** (20 É par coup).
- **Saturation** : capacité épuisée **ou** énergie insuffisante ⇒ **les dégâts passent normalement**.
  C'est la seule lecture possible de « pas sans limite », et le test tire **un coup de plus** que la
  capacité pour le constater : `capacité 3 → 0`, coup suivant **×2,0 au lieu de ×0,0**.
- Le **scellement ferme la fenêtre** : un pouvoir scellé n'absorbe plus, et la capacité n'est même pas
  débitée.

### Le scellement : une seule porte, et une échéance qui suffit *(-05)*

Le plan : « une **suppression temporaire des fruits** », critère « la cible perd temporairement les
capacités prévues **puis les retrouve** ».

Le scellement **n'ajoute aucune mécanique de verrouillage** : il pose un marqueur que
`DevilFruitPowerLogic.isSuppressed` reconnaît — le prédicat que **tout** consultait déjà (lancement de
techniques, intangibilité, aires maintenues, formes, charges). C'est le bénéfice d'avoir gardé **une**
définition de « pouvoir scellé » depuis Moku Moku : la nouvelle mécanique n'a rien eu à propager.

**« Puis les retrouve » est un fait, pas une intention** : la restitution ne demande aucun code, l'effet
**expire par le temps du moteur**, et le test le **constate** à un autre tick (`A8d`). La conséquence est
mesurée aussi : un porteur de **Moku Moku** scellé passe de **×0,0 à ×1,0** — il perd son intangibilité,
exactement comme sous l'eau de mer.

**Message corrigé** : une cible scellée lisait « l'eau de mer vous a scellé ». La cause est maintenant
distinguée (`suppressionCause` → `sea` / `yami`) et la traduction propre ajoutée (fr + en). Une décision de
blocage unique, un message qui dit la vérité.

### Les exceptions : mesurées, pas écrites *(-06)*

Le plan : « Chaque source de dégâts prévue est correctement **annulée ou conservée** et la suppression
**enlève le passif** ».

Le résultat honnête est l'inverse de Moku Moku et Mori Mori : ici **il y a bien une exception à écrire**,
et c'est celle de §2 (aucune intangibilité). Toutes les autres sources sont **déjà** correctement
conservées par la table « physique / non physique » du moteur (elle exclut explosions, feu et magie), et
la **suppression enlève le passif** pour les capacités **actives** (attraction, absorption) : mesuré.

## 5. Acquisition, rareté, progression *(-08)*

Identique aux autres Fruits. **Aucun déblocage par maîtrise** — lacune **générale** du port, déjà
consignée pour les Fruits précédents.

## 6. Validation visuelle *(-07 — exige un client)*

- [ ] icône, modèle, **deux mains**, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **particule `dark`** : les 15 images défilent, non une image figée ;
- [ ] attraction, absorption, scellement : lisibles à distance ;
- [ ] **message de scellement** : « Vos pouvoirs sont scellés par les ténèbres. » (et non l'eau de mer) ;
- [ ] bonus de lumière de Pika Pika avec les particules du mod (`light_large`, `light_small`).

## 7. Tests *(-09)*

**Section `A8` — 12 vérifications** (`dftest yami`), plus **2 phases de recette** (`3 quater`, `yami_impact`).

| Phase | Attendu | Mesuré |
|---|---|---|
| `dftest all` | — | **261/261 PASS** |
| `3 bis. dftest ope_expiry` | échéance Ope (non-régression) | **1/1** |
| `3 ter. dftest pika_impact` | impact réel (non-régression) | **2/2** |
| `3 quater. dftest yami_impact` | traction bornée + restitution du scellement | **2/2** |
| `6 ter. dftest ope_restart` | aucun état fantôme | **1/1** |
| **Global** | — | **275/275 PASS, 0 FAIL** |
| **Stabilité** | 3 passages consécutifs | **275/275 × 3** — 4 passages verts au total, toutes les phases vertes à chaque passage |

**Assets du jar source vérifiés DANS le jar livré** : `assets/minepiece/particles/dark.json`,
`light_large.json`, `light_small.json` + **25 textures** `textures/particle/particle_{dark,light_*}*.png`,
et la traduction du scellement présente en `fr_fr` **et** `en_us`.

| Vérification | Ce qu'elle prouve |
|---|---|
| `A8a-exception-decidee-trois-valeurs` | ×2 (Yami) · ×1 (sans Fruit) · ×0 (autre Logia) — les **trois** sont nécessaires |
| `A8a-attaquant-yami-annule-sans-niveler` | annulation ×0→×1 **sans** rabaisser un ×2 |
| `A8b-attraction-bornee-dans-le-code` | rayon **9999 → 32** : la borne est dans le code |
| `A8b-attraction-armee-deux-temoins` | deux témoins sur des **axes différents** |
| `A8b-sonde-traction-directe` | **dirigée, croissante, bornée, nulle** derrière un mur et sous 1,5 bloc |
| `A8b-attraction-bornee-dans-le-temps` *(3 quater)* | la traction **s'arrête seule** (tick du service réel) |
| `A8c-absorption-annule-le-physique-pas-l-explosion` | physique ×0 **et** explosion conservée, capacité non débitée |
| `A8c-capacite-depassee-les-degats-passent` | saturation ⇒ **le coup suivant porte** |
| `A8c-scellement-ferme-la-fenetre` | scellé ⇒ plus d'absorption, capacité intacte |
| `A8c-absorption-coute-de-l-energie` | sans énergie ⇒ pas d'absorption, capacité intacte |
| `A8d-le-contact-scelle-la-cible` | le contact scelle, et `isSuppressed` le voit |
| `A8d-un-logia-scelle-perd-son-passif` | **×0 → ×1** pour un Logia scellé |
| `A8d-scellement-pose-avec-echeance` | scellé avec un terme mesurable |
| `A8d-secelle-puis-recupere-ses-capacites` *(3 quater)* | après échéance : **plus scellé** |

## ⚠️ Cinq erreurs de MESURE, dont trois accusaient le code à tort

Le code n'a **pas changé** entre le premier et le dernier essai des tests : les cinq fautes étaient dans
le **harnais**. Elles sont écrites ici parce qu'elles se reproduiront.

**1. L'attaquant porteur de Yami annulait l'intangibilité que je mesurais.** Pour prouver qu'un Logia est
intangible, j'attaquais… avec le porteur de Yami — dont la règle dit précisément « annule le pouvoir
touché ». Le ×0 attendu devenait ×1, et le test accusait la règle générale d'être absente alors qu'il
lisait **sa propre annulation**. *Mesurer un phénomène exige de ne pas l'annuler avec l'instrument qui le
mesure* : les mesures d'intangibilité passent désormais par un **attaquant neutre**.

**2. `immerse` tue un porteur qui a déjà un Fruit — et le test lisait « pas de Fruit » sans le savoir.**
`DevilFruitItem.finishUsingItem` applique la règle du **2ᵉ fruit = mort**. Un faux joueur déjà porteur
survit (invulnérabilité) mais perd son Fruit : la vérification « l'attaquant Yami annule le pouvoir d'un
Logia » **passait pour la mauvaise raison** (elle comparait deux porteurs sans Fruit). Corrigé par une
assignation **directe** des variables, motif déjà employé ailleurs dans le harnais.

**3. `push()` n'est pas un déplacement : c'est une VITESSE.** Ma sonde lisait la **position** juste après
avoir appliqué la traction — dans le même tick, donc **avant** que l'entité n'ait appliqué sa vitesse. Elle
concluait « rien n'a bougé » sur un mécanisme correct, et accusait le code. La sonde mesure maintenant la
**vitesse appliquée** (delta avant/après), ce qui est de toute façon la **cause** de la trajectoire.

**4. `setNoAi(true)` empêche TOUT déplacement.** Je fige les témoins de visée avec `noAi` (leçon de Pika
Pika)… or un mob sans IA ne bouge **pas**, même poussé : la traction devenait inobservable. Les deux
précautions **se contredisent** et il faut choisir la bonne selon la preuve : *figé* pour une preuve de
visée, *libre* pour une preuve de mouvement. La sonde emploie des témoins figés et mesure la vitesse —
ce qui satisfait les deux.

**5. Un compteur remis à zéro avant d'être lu.** Mon rapport de diagnostic affichait `attractions=0`
alors que la traction avait été armée : je l'avais réinitialisé juste avant de le journaliser. Le rapport
est désormais lu **avant** sa remise à zéro — un compteur qui dit zéro ne prouve pas qu'il ne s'est rien
passé si on vient de le vider.

**Et une sixième, héritée et appliquée d'emblée** : les deux témoins d'attraction sont sur des **axes
différents** (leçon de Pika Pika, où un mur placé sur un axe unique bloquait les deux rayons et rendait la
mesure **irréalisable**).

## Dette signalée

**1. `particle_dark` n'est pas la seule particule à récupérer.** Le jar source en contient **quarante**
(`flame`, `ice`, `magma`, `thunder`, `note`, `gravity`, `negative`, `shockwave`…) et le port utilise encore
des particules **vanilla** pour les ~200 autres techniques. Trois ont été repris ici, parce que les Fruits
traités en dernier les réclamaient. Une passe dédiée (extraction + `MinepieceModParticles`) rendrait la
couche visuelle fidèle pour tout le catalogue — c'est un chantier à part, non commencé.

**2. L'attraction n'a pas de coût de maintien.** Elle est bornée dans le temps (20 ticks) et par la
recharge de la technique, mais le plan ne fixait aucune valeur de maintien. À trancher si un coût continu
est souhaité.

**3. La contrepartie de douleur (×2) est une valeur décidée.** Fidèle à la source, mais absente de ver15
et du plan : à confirmer ou à régler (`pain_multiplier`).
