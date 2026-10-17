# Travail actuel — lot03 alpha.12

Root et Giant lancent de vraies racines ligneuses ; les branches de Giant partagent le même budget de dégâts par cible. Assault est une attaque, plus un drainage substitué à la source. Surround résout trois phases radiales : une cible arrivée après l’ouverture peut être touchée, une cible déjà touchée ne reçoit pas un second budget. L’immobilisation suit seulement un coup accepté. La conversion de sol en herbe est sélective, bornée à128cases et définitive ; les racines dessinées et les entraves expirent. Le cube de feuilles de l’ancien Giant ne remplace plus son attaque.

[Lot courant](../BATCH-03.md), [décisions terrain](../TERRAIN-POLICY.md). Recette serveur et rendu groupé vérifiés : [bilan final](../B03-VALIDATION-2026-10-16.md). Acceptation artistique humaine en attente. Les anciennes sections ci-dessous restent historiques et ne définissent pas le comportement corrigé.

---

**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Mori Mori no Mi (Forest-Forest Fruit)

Vingt-quatrième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Logia** de la forêt.
Deuxième contrat dont le cœur est l'**intangibilité propre** d'un Logia, et premier dont le cœur est un
**gain** : le drainage est le lieu naturel de l'abus.

- **Item** : `minepiece:item_devil_fruit_mori_mori`
- **Techniques** : **4** (ver15), aucune ajoutée, **aucun écart d'audit**
- **Famille** : **`logia`** — lue et confirmée (`A5a`)
- **Racines** : 100 ticks, **6 cibles** max · **Végétation** : 40 blocs, 300 ticks
- **Drainage** : **20 PV max/cible/usage**, ratio 0,5, **plafond 60 énergie/usage**

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **devient la forêt** : racines qui saisissent, végétation qui pousse, sève qui
se draine. Les coups physiques le traversent.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 70 | plan ver15 |
| **Racines : durée / cibles** | 100 ticks / 6 | config |
| **Végétation : blocs / durée** | 40 / 300 ticks | config |
| **Drainage : PV / ratio / énergie** | 20 PV / 0,5 / **60 É** | config |

**Contre-mesures** : eau de mer (**le passif tombe**), Haki, énergie insuffisante, retrait, **feu**.

## 2. Classification : lue, pas supposée *(-02)*

`A5a` lit les trois tags — `logia` **true**, `zoan` false, `paramecia` false. Le corps devient une
**matière** (bois et sève) et les coups physiques le traversent : **Logia**, confirmé.

## 3. Catalogue

| Technique | Type | Dégâts | Portée | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **Mori Mori Root** | projectile | 9 | — | 50 | 100 | conservée |
| **Giant Root** | projectile | 20 | — | 60 | 140 | **→ végétation** |
| **Mori Mori Assault** | mêlée | 15 | 6 | 50 | 120 | **→ drainage** |
| **Root Surroundings** | projectile | 15 | — | 70 | 180 | **→ racines** |

Aucun ajout, aucun renommage : **aucun écart d'audit**. Les deux techniques sans mécanique propre sont
**inchangées** — leur en attribuer une serait un ajout, pas un portage.

## 4. Spécialisations

### « L'immobilisation **respecte les résistances** » *(-03)*

Le critère ne dit pas « immobilise les cibles » : il dit **« respecte les résistances »**. La différence
est tout : une immobilisation qui s'applique à tout le monde n'est plus un pouvoir, c'est un **réglage du
jeu**.

Pour l'implémenter, j'ai d'abord écrit le contrôle qui paraissait évident :
`cible.canBeAffected(new MobEffectInstance(ROOTED))`. **Il ne refusait rien.** Vérification empirique sur
**8 types de cibles** (zombie, squelette, **wither**, **ender dragon**, golem de fer, villageois, blaze,
slime) : **toutes** acceptent l'effet. Un contrôle qui ne refuse **jamais** n'est pas un contrôle — et il
aurait « passé » le test.

Résistances **réelles et vérifiables** dans ce moteur :

| Résistance | Mécanisme | Preuve |
|---|---|---|
| Immunité aux dégâts | `isInvulnerableTo(level, source)` | cible invulnérable → **refusée**, vitesse inchangée ✅ |
| Immunité aux effets | `addEffect(...)` renvoie **`false`** | déléguée au moteur, pas réécrite |

**Et l'ordre des opérations *est* la garantie** : on demande au moteur d'accepter l'effet **avant** de
poser le ralentissement. Poser le modificateur d'abord immobiliserait une cible **qui a résisté** —
exactement ce que le critère interdit. `A5b` mesure donc, sur la cible résistante : vitesse **inchangée**,
**ni marqueur ni maintien résiduels**.

Fin propre : borne à 100 ticks, raison « échéance », vitesse **rendue**, marqueur **parti**.

### Végétation temporaire *(-04)*

« Aucun bloc **permanent involontaire** ». La garantie ne vient pas d'une promesse : chaque bloc passe par
`TemporaryBlockService`, le **propriétaire unique** des blocs temporaires (feu de Mera, nappes de Magu,
gravats de Zushi), qui mémorise le bloc d'origine et le **restitue**. `A5c` le mesure : **40 blocs**
(plafond 40), **tous suivis comme temporaires**, terrain **restitué** au retrait.

⚠️ **Aucune machinerie réécrite ici.** Ma première version du fruit précédent dupliquait ce service — elle
avait été supprimée. Ce contrat en tient compte dès le départ.

### Le drainage : coûte et **plafonne** *(-05)*

Un pouvoir qui **retire** peut être équilibré par son coût. Un pouvoir qui **rend** ne l'est que par une
**borne**. `A5d` mesure quatre cas :

| Cas | Gain |
|---|---|
| Cible valide (zombie) | **+10,0 É** (plafond déclaré **60**, vie 20 → 0) |
| 2ᵉ drainage immédiat | **+0,0** (le gain est borné par usage) |
| **Invocation du porteur** | **+0,0** et vie **inchangée** |
| **Cible invulnérable** | **+0,0** |
| **Soi-même** | **+0,0** |

Sans ces exclusions : une invocation bon marché serait une **batterie**, deux complices se draineraient
**en boucle**, et l'invulnérabilité deviendrait une **source**. Une cible exclue n'est pas « drainée à
zéro » : elle n'est **pas touchée du tout** — sinon le porteur croirait à une panne et recommencerait.

⚠️ Le cas « **alliés** » utilise l'`isAlliedTo` du moteur ; il n'est **pas testable dans le harnais**
(aucune équipe scoreboard). Il est implémenté mais **non prouvé** : écart signalé, pas dissimulé.

### Les exceptions du Logia — et cette fois avec **contre-épreuve** *(-06)*

| Source | Porteur du Fruit | **Témoin sans Fruit** |
|---|---|---|
| Physique | **×0** (annulée) | ×1 |
| **Feu** | ×1 | **×1** |
| Explosion | ×1 | **×1** |
| Magie | ×1 | — |
| Sous **scellement** | **×1** | — |
| Avec Haki | ×1 | — |

**La colonne de droite est le point.** Le feu vaut ×1 sur le porteur **et** ×1 sur le témoin : il n'a donc
**jamais** été concerné par l'intangibilité. Écrire une « exception feu » serait du **code mort** qui
passerait pour un correctif — c'est exactement l'erreur commise sur le Fruit précédent, où mon exception
« le souffle disperse la fumée » ne changeait rien parce que le moteur exclut **déjà** explosions, flammes
et projectiles magiques de la catégorie physique.

**Ce qui manquait réellement**, et qui est corrigé : la **suppression** n'était **pas consultée**. Un
porteur immergé restait intangible, donc **protégé par un pouvoir que le jeu venait de lui retirer**.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_mori_mori` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ʳᵉ personne, objet au sol, infobulle ;
- [ ] **les racines doivent se voir** sur la cible (aujourd'hui des particules + un son), sinon une victime
      immobilisée croira à un bug ;
- [ ] **la végétation doit être reconnaissable** et sa disparition progressive lisible ;
- [ ] **le drainage doit se voir** : la sève qui remonte, du porteur vers la cible ;
- [ ] le **rendu élémentaire du corps** (écorce, feuilles) — le plan le demande dans -06.

## 7. Tests *(-09)*

`/dftest mori` — **5/5 PASS** :

| id | Résultat |
|---|---|
| `A5a-classification-argumentee` | logia **true**, zoan false, paramecia false |
| `A5b-racines-respectent-les-resistances-et-se-terminent` | résistante **refusée** (0,23 → 0,23), normale **immobilisée** (0,23 → 0,0 → 0,23) |
| `A5c-vegetation-temporaire-et-restituee` | 40 blocs suivis, terrain **restitué** |
| `A5d-drainage-plafonne-et-sans-gain-sur-les-proteges` | **+10,0** puis **+0,0** · invocation, invulnérable, soi : **0** |
| `A5e-exceptions-du-logia-avec-contre-epreuve` | physique ×0, feu/explosion ×1 **= témoin ×1** |

Reste manuel : tout le §6.

---

## ⚠️ Trois erreurs corrigées, dont **une qui aurait fait passer un mensonge**

1. **Un contrôle de résistance vacant.** Mon `canBeAffected` préalable **ne refusait rien** (vérifié
   empiriquement sur 8 types de cibles, wither et ender dragon compris). Il aurait « prouvé » le respect
   des résistances sans rien faire. Remplacé par la résistance **réelle** (immunité aux dégâts) et par la
   **porte du moteur** (`addEffect`), avec l'ordre des opérations comme garantie.
2. **Baseline mesurée après l'action** (*3ᵉ fois sur ce projet*). Ma variable `baseNormale` valait la
   vitesse **immobilisée** : la comparaison finale échouait pour une raison sans rapport avec le code. Le
   test nomme désormais les trois instants — **AVANT / PENDANT / APRÈS** — et affiche les trois.
3. **Une « exception » sans effet** (héritée du Fruit précédent, corrigée ici) : elle est remplacée par
   une **contre-épreuve** qui compare chaque source au **témoin sans Fruit**. C'est cette comparaison qui
   prouve qu'aucune exception n'est nécessaire — la valeur seule ne prouve rien.

## ⚠️ Deux tests antérieurs se sont révélés INSTABLES — et la cause était **une seule**

Une suite qui passe **une fois** n'est pas une suite verte. J'ai donc lancé des **campagnes de 3 à 4
passages complets** et compté les échecs **par assertion** — cinq campagnes successives.

| Campagne | Correctif apporté | Résultat |
|---|---|---|
| 1 | — | **229 ✅ / 228 ✗ / 229 ✅** |
| 2 | `W3` : retirer la cible précédente avant d'en placer une autre · `P2` : scène posée couche par couche et vérifiée | **229 / 228 / 228** |
| 3 | `W2`/`W3` : couloir **dégagé et vérifié** | **228** — hypothèse d'obstacle **FAUSSE** (`obstacles = 0`) |
| 4 | `W2` : **géométrie de l'ACTEUR** garantie (`revertForm` + `refreshDimensions`) | **229 / 229 / 229** — puis **228** à la confirmation : hypothèse **insuffisante** |
| 5 | **Toutes les fixtures zombie forcées ADULTES** (`setBaby(false)`) | **229 / 229 / 229** ✅ |

### La cause racine : un zombie sur vingt naît **bébé**

Cinq vérifications différentes échouaient **par intermittence** — `W2` et `W3` (souffle d'Okuchi), `G3` et
`G4` (bras de Gomu), `P2` (gravité de Zushi). Le réflexe est de les traiter une par une. **C'était une
seule cause.**

L'instrumentation a produit la preuve, sur des exécutions strictement comparables :

| | PASS | FAIL |
|---|---|---|
| rayons du cône | portée **10,0** sur les 5, directions identiques | **identiques** |
| hauteur d'yeux | 101,62 | **101,62** |
| distance à la cible | 8,0 | **8,0** |
| **boîte de collision** | **0,60 × 1,95 × 0,60** (adulte) | **0,49 × 0,98 × 0,49** (bébé) |
| centre de la boîte | y = 100,98 | **y = 100,49** |

Les rayons sont les mêmes, la géométrie de l'acteur est la même, la distance est la même — **seule la
taille de la cible change**. Les zombies de vanilla naissent **bébés au hasard** (~5 %), et une boîte de
bébé **descend le centre de 1,13 bloc** : assez pour sortir d'un cône large de **1,0** bloc, pour ne plus
toucher un plafond situé 2 blocs plus haut (0,98 bloc de haut qui monte de 1,0 ne le heurte pas) ou pour
échapper à une frappe de bras. **Chaque vérification piochait indépendamment ses ~5 %** — d'où 1 à 3
échecs par passage, jamais les mêmes.

Correctif : **`setBaby(false)` sur les 58 fixtures** du harnais. Le harnais tirait un dé au hasard sans le
dire ; il ne le tire plus.

### Trois de mes hypothèses étaient fausses — l'instrumentation les a départagées

1. **Un obstacle dans le couloir** → mesuré : `0 bloc retiré, obstacles restants = 0`. **Faux.**
2. **L'acteur resté transformé** → mesuré : `hauteur d'yeux 101.62 (attendu 101.62, sain = true)`. **Pas la
   cause ici** (la garantie reste utile).
3. **La boîte de collision de la cible** → **c'est elle**, et l'affichage de la boîte l'a prouvé en une
   exécution.

**Leçon** : devant plusieurs tests instables, chercher **l'entrée aléatoire partagée** avant de corriger
cas par cas ; et **afficher** dans chaque preuve les grandeurs dont dépend le verdict (boîte, portée,
géométrie de l'acteur) — sans quoi chaque échec se transforme en devinette. Un test doit être **stable**,
pas « vert une fois ».

*(Le harnais conserve par ailleurs deux garanties utiles nées de cette enquête : le **couloir** de mesure
est dégagé et vérifié, et la **géométrie de l'acteur** est contrôlée avant toute mesure de visée.)*

## Dette signalée

`Roots` (Mori), `StickyState` (Mochi) et la capture de `SmokeCloud` (Moku) partagent une **forme** de
maintien temporaire avec libération garantie ; leurs **politiques** diffèrent réellement (gradation ;
nuage ; résistances), mais un service commun de « maintien » serait plus propre. Il toucherait **deux
Fruits déjà livrés et testés** : il est donc **proposé** plutôt qu'imposé.
