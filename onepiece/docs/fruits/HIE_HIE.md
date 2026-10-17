**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Mise à jour alpha.8 — lot01,16octobre2026

Les5techniques de ce fruit ont été reprises avec les quatre autres fruits du lot. Les budgets, routes, adaptations et limites actuelles sont décrits dans [B01-IMPLEMENTATION](../B01-IMPLEMENTATION-2026-10-16.md), qui remplace les anciennes descriptions de comportement ci-dessous lorsqu’elles divergent. [Résultats et hash des tests](../B01-VALIDATION-2026-10-16.md). Implémentation, preuve serveur, observation client et acceptation artistique restent distinctes ; l’essai humain est attendu.

---

# Contrat — Hie Hie (Ice-Ice Fruit)

Huitième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Logia de glace — le
deuxième fruit à porter une résistance, et celui dont le plan a imposé le raisonnement le plus fin :
**quelle règle d'immersion adopter sans ouvrir de contournement**.

- **Item** : `minepiece:item_devil_fruit_hie_hie`
- **Techniques** : 5 (ver15) — Ice Block: Partisan, Ice Block: Pheasant Beak, Ice Spikes, Ice Ball, Ice Age
- **Famille** : `logia`

---

## 1. Identité, limites et contre-mesures *(FRUIT-HIE_HIE-01)*

**Identité** — Logia de glace : le corps est de glace, les attaques physiques le traversent, et le
porteur gèle ce qu'il touche. Il **éteint aussi le feu** (`onDevilFruitUserTick` : Inu Inu et Hie Hie
ramènent les ticks d'incendie à zéro) — passif hérité de ver15, conservé.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 70 | plan ver15 |
| Recharges | 100 à 180 ticks | plan ver15 |
| **Paliers de gel** | **3** | contrat (§4) |
| Durée d'un gel | 60 ticks (3 s) | contrat |
| **Plafond dur de gel** | **100 ticks** | contrat |
| **Fenêtre de liberté garantie** | **100 ticks** | contrat |
| Durée de vie de la glace | 200 ticks | contrat |

**Contre-mesures** : eau de mer (SEA_POWER) → pouvoir scellé, Haki de l'Armement, énergie
insuffisante (refus sans coût), retrait du Fruit (reset + `FreezeService.clearAll()` +
`TemporaryBlockService.revokeAll()`). Et **le feu** : Hie Hie l'éteint, une attaque de glace l'efface.

## 2. Classification *(FRUIT-HIE_HIE-02)*

ver15 dit **`LOGIA`** — vérifié plutôt que recopié : le tag `minepiece:logia` est posé et lu par
`DevilFruitPowerLogic.family()`, donc l'intangibilité physique s'applique (résistance Logia), et la
suppression par l'eau retire le pouvoir. Testé par `/dftest haki` (CORE-029).

Vérifié : `python3 tools/fruit_audit.py --fruit HIE_HIE` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Ice Block: Partisan** | projectile | conservée | 40 | — | 60 | 140 |
| **Ice Block: Pheasant Beak** | projectile | conservée | 60 | — | 70 | 160 |
| **Ice Spikes** | projectile | **pics temporaires** | 20 | 12 | 50 | 100 |
| **Ice Ball** | frappe | **gel progressif + chemin de glace** | 15 | 16 | 50 | 120 |
| **Ice Age** | frappe | **gel progressif + champ de glace** | 20 | 16 | 70 | 180 |

⚠️ **Ice Spikes** était déclaré **projectile** dans ver15 : le nom de la technique n'avait aucun
rapport avec ce qu'elle faisait. Elle dresse désormais de **vrais pics** et fauche ce qu'ils trouvent
(même logique que la conversion des Biscuit Warriors de Bisu Bisu, elle aussi demandée par le plan).

## 4. Spécialisations

### Gel progressif *(FRUIT-HIE_HIE-03)*

Le plan : « créer un **état de gel progressif** distinct du simple ralentissement », critère « le gel
**se libère à l'échéance**, **se synchronise** et **ne bloque pas définitivement** le joueur ».

ver15 posait un `SLOWNESS` d'amplificateur **fixe** par technique (2 pour Ice Ball, 3 pour Ice Age) :
deux réglages indépendants, pas une progression. Ici la glace **s'accumule sur la cible** : chaque
coup reçu fait monter d'un palier.

| Palier | État | Mobilité |
|---|---|---|
| 1 | ralenti | réduite |
| 2 | engourdi | très réduite |
| **3** | **figé** | **horizontale nulle** (la chute est conservée) |

**Les trois exigences du critère, et comment chacune est tenue :**

1. **Se libère à l'échéance** — l'état porte un compteur que le service décrémente lui-même ; arrivé
   à zéro, il **libère** sans qu'aucun nouvel événement soit nécessaire. Aucun gel n'attend un coup
   reçu pour se terminer (`K3`).
2. **Se synchronise** — l'état visible passe par un effet **vanilla** (`SLOWNESS` à l'amplificateur du
   palier, plus l'effet `FROZEN` pour le gel total) : synchronisé aux clients et affiché par le jeu,
   sans paquet maison à écrire.
3. **Ne bloque pas définitivement** — deux bornes indépendantes : l'échéance, et un **plafond dur**
   de 100 ticks compté depuis la première application, que les applications suivantes **ne repoussent
   jamais**. Le plafond seul ne suffirait pas : libéré, un joueur matraqué serait regelé au coup
   suivant. Le gel est donc **refusé pendant une fenêtre de liberté garantie** de 100 ticks.

**Preuves** :
- `K1` : trois applications → paliers **1 → 2 → 3**, effet vanilla `SLOWNESS` ampl. 3 (donc synchronisé) ;
- `K2` : au palier 3 la mobilité horizontale est **annulée**, la chute conservée ;
- `K3` : échéance atteinte sans nouvel événement → **palier 0**, effets retirés ;
- `K4` : matraquage de **600 ticks**, un regel par tick → **plus longue séquence figée = 97 ticks**
  (plafond 100), **6 fenêtres de liberté** ouvertes. Le total figé (291 ticks) est élevé et c'est
  normal : ce qui compte n'est pas le total mais **la plus longue séquence sans interruption** — c'est
  elle qui ferait une prison.

### Glace et pics temporaires *(FRUIT-HIE_HIE-04)*

Le plan : « créer glace et pics temporaires avec **restauration conditionnelle** des blocs », critère
« la glace expire **sans écraser les modifications ultérieures d'un joueur** ».

La glace passe par `TemporaryBlockService` (CORE-020), qui ne restaure le bloc d'origine que **si la
case contient encore notre glace**.

**La garantie existait dans le service, aucun test ne la couvrait.** Elle est désormais prouvée
(`K5`) : glace posée → **le joueur casse la glace et pose ses planches** → expiration → **les planches
survivent**.

### Plateforme et faiblesse aquatique *(FRUIT-HIE_HIE-05)*

Le plan : « définir la création de plateformes de glace et son interaction avec la faiblesse
aquatique », critère « la plateforme respecte les **règles d'immersion choisies** et ne permet
**aucun contournement involontaire** ».

**La règle choisie** — la plateforme est posée **sur** l'eau, jamais **dans** le porteur : aucune case
touchée par sa boîte de collision n'est gelée. Les deux conséquences, qui sont le fond du critère :

| Cas | Résultat | Verdict |
|---|---|---|
| Marcher sur la mer gelée | la faiblesse tombe **parce qu'on n'est plus dans l'eau** | ✅ légitime — c'est la récompense du gel |
| Geler l'eau que l'on occupe | `isInWater` deviendrait faux **alors qu'on est toujours immergé** | ❌ **contournement** — interdit par construction |

La faiblesse est gouvernée par le contact **réel** avec l'eau
(`onDevilFruitUserTick` : `isSwimming` → ampl. 9, `isInWater` → ampl. 1) : la glace ne peut pas
falsifier cette mesure, puisqu'elle ne peut pas se poser là où le porteur se tient.

**Preuve `K6`** : porteur dans l'eau avec SEA_POWER → **44 blocs gelés** autour de lui, **aucune glace
dans son espace**, **faiblesse toujours active**.

### Exceptions Logia *(FRUIT-HIE_HIE-06 — partiel, assumé)*

L'intangibilité et ses percements (Haki, eau, Yami) sont couverts par CORE-029 et vérifiés par
`/dftest haki`. Le passif **« éteint le feu »** de ver15 est conservé et exercé à chaque tick.

⚠️ **Ce qui n'est pas fait** : les exceptions **propres à la glace** — un porteur Hie Hie devrait être
**immunisé au froid et aux dégâts de glace** (se geler soi-même n'a pas de sens). Un test de cette
immunité n'existe pas. Manque identifié, pas dissimulé.

## 5. Acquisition, rareté, progression *(FRUIT-HIE_HIE-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_hie_hie` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-HIE_HIE-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ʳᵉ personne, objet au sol, infobulle ;
- [ ] **lisibilité des paliers de gel** : le joueur doit voir qu'il gèle *progressivement*, et surtout
      sentir la libération ;
- [ ] la fenêtre de liberté doit être **perceptible** (sinon le joueur croira à un bug) ;
- [ ] pics de glace : hauteur et lisibilité ;
- [ ] chemin / champ de glace : praticabilité réelle à la marche ;
- [ ] intangibilité Logia à deux clients.

## 7. Tests *(FRUIT-HIE_HIE-09)*

`/dftest hie` — **8/8 PASS** :

| id | Résultat |
|---|---|
| `K0-decor-pret` | terrain prêt · 3 paliers · plafond 100 ticks |
| `K1-gel-progressif` | 1 → 2 → 3, effet vanilla synchronisé |
| `K2-gel-total-immobilise` | mobilité annulée, chute conservée |
| `K3-liberation-a-l-echeance` | palier 0 sans nouvel événement |
| `K4-jamais-de-prison` | séquence max **97** ticks · 6 fenêtres de liberté |
| `K5-glace-n-ecrase-pas-la-construction` | planches du joueur respectées |
| `K6-plateforme-ne-contourne-pas-la-mer` | 44 gelés · 0 dans le porteur · faiblesse active |
| `K7-glace-temporaire-et-restauration` | terrain restauré |

⚠️ **Piège de conception rencontré ici** — la première version mesurait l'échéance avec le **temps de
jeu du monde** (`getGameTime()`). Deux conséquences : le gel dépendait d'une horloge externe, et il
devenait intestable (toutes les vérifications s'exécutent dans un même tick serveur, donc le temps de
jeu n'avance pas). Corrigé en donnant au service **sa propre horloge** : un compteur décrémenté à
chaque tick. En jeu c'est identique (le gestionnaire est appelé une fois par tick), et le service
devient déterministe et vérifiable.

⚠️ **Piège de mesure** — « ne bloque pas définitivement » ne se mesure pas en ticks figés **au total**
mais en **plus longue séquence ininterrompue**. Un total élevé est normal si le joueur est regelé
après chaque fenêtre de liberté. Ma première assertion confondait les deux et condamnait un
comportement correct.

Reste manuel : tout le §6.
