# Travail actuel — lot03 alpha.12

Pistol touche au rayon réellement dégagé et colle seulement après dégâts acceptés. Gatling a six impacts de4HP espacés de10ticks (24HP au total), avec bras droit/gauche alternés. Yaki est un vrai projectile distinct ; ZanGiri avance avant le coup. Muso invoque deux anneaux suivant le joueur pendant1200ticks ; ils attendent un nouveau geste offensif, puis partagent un budget20HP par cible pour leurs poings. La convergence suit le point visé, sans traverser les obstacles. Construction est un ajout du plan :24blocs de miel au plus, définitifs, sans ensevelir les entités. Le20HP de Muso et les bornes de charge sont des adaptations conservées ; le setter source20 est MAX_HEALTH, pas une preuve de dégâts.

[Lot courant](../BATCH-03.md), [décisions terrain](../TERRAIN-POLICY.md). Recette serveur et rendu groupé vérifiés : [bilan final](../B03-VALIDATION-2026-10-16.md). Acceptation artistique humaine en attente. Les anciennes sections ci-dessous restent historiques et ne définissent pas le comportement corrigé.

---

**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Mochi Mochi no Mi (Mochi-Mochi Fruit)

Vingt-deuxième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Paramecia** de
Katakuri. Son contrat pose la question de l'**identité d'un actif partagé**, de la **contrainte qu'on
impose à autrui**, et du **terrain qu'on modifie**.

- **Item** : `minepiece:item_devil_fruit_mochi_mochi`
- **Techniques** : **6** — 5 de ver15 + **1 ajoutée** (construction, plan-mandatée)
- **Famille** : **`paramecia`** — argumentée, pas déduite (`A3a`)
- **État collant** : 100 ticks (immobilisation après 60) · **Constructions** : plafond **3**, 24 blocs, 200 ticks
- **Nouvel actif** : `mochi.png` (64×64, créée)

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur produit et façonne une **matière** : il étire ses membres en mochi, projette
des masses collantes, et bâtit des structures qui durcissent puis se ramollissent.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 60 | plan ver15 |
| **État collant : durée MAX** | **100 ticks** | config |
| Collant : immobilisation après | **60 ticks** | config |
| Collant : rayon | 3,0 blocs | config |
| **Constructions : population** | **3** | config |
| Constructions : blocs / durée | **24** / 200 ticks | config |

**Contre-mesures** : eau de mer, retrait du Fruit, Haki, énergie insuffisante.

## 2. Classification : argumentée, pas déduite *(-02)*

`A3a` lit les trois tags — `paramecia` **true**, `logia` false, `zoan` false.

**L'argument** : ce Fruit ne transforme pas le corps en élément (pas **Logia**) ni le porteur en animal
(pas **Zoan**) — il produit une **matière** qu'il façonne et projette. C'est un **Paramecia**.

## 3. Catalogue

| Technique | Type | Dégâts | Portée | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **Mochi Pistol** | mêlée | 11 | 6 | 50 | 100 | bras mochi, **texture propre** |
| **Mochi Gatling** | mêlée | 4 (×6) | 5 | 50 | 100 | bras mochi, **texture propre** |
| **Yaki Mochi** | projectile | 15 | — | 50 | 120 | conservée |
| **Zan Giri Mochi** | mêlée | 6 | 6 | 50 | 100 | conservée |
| **Muso Donatsu** | projectile | 20 | — | 60 | 140 | conservée |
| **Mochi Build** *(Construction de Mochi)* | état | 0 | — | 50 | 120 | **ajoutée** (-05) |

⚠️ **Écart d'audit assumé** : 1 ajout. Le plan demande de « **créer** des constructions temporaires de
mochi » et **aucune** des 5 techniques de ver15 n'en pose : l'ajout **est** l'instruction.

## 4. Spécialisations

### « Ne réutilise pas **involontairement** la texture de Luffy » *(-03)*

Le mot « involontairement » est le bon. Réutiliser un actif est légitime quand c'est **délibéré** — et
fautif quand ça arrive **par défaut**. Or la texture `luffy` était **écrite en dur** dans la machinerie du
bras : **tout** Fruit l'empruntant héritait du visage de Luffy **sans décision de personne**. C'est
exactement une réutilisation involontaire, et elle ne se voyait pas dans le code : elle était dans la
valeur par défaut.

La texture est maintenant **par technique** (`GomuArmStrike.texture(moveId)`) :

| Technique | Texture |
|---|---|
| `mochipistol`, `mochigatling` | **`mochi`** *(créée : 64×64, pâte crème, inclusions)* |
| `gomu_pistol`, `gomu_bazooka`, `gomu_gatling` | `luffy` (délibéré, c'est son Fruit) |

**La collision suit le membre** parce que le mochi passe par la **même machinerie** que Gomu
(`GomuArmStrike`) : il n'y a **aucune formule parallèle** à faire diverger. C'était le sens de la
solution : réutiliser la machinerie *et* changer l'identité.

*Reçu délibéré* : la **géométrie** du bras et ses **animations** sont partagées (un poing de mochi qui
frappe est le même geste qu'un poing élastique). L'identité du Fruit est portée par la **texture**.

### L'état collant : gradué, borné, **toujours libéré** *(-04)*

Coller quelqu'un n'est pas le tuer : c'est **décider à sa place qu'il ne se déplacera pas**. Deux règles
s'imposent donc.

**La gradation** — ralentissement d'abord (−60 %), immobilisation ensuite (−100 %), jamais les deux à la
fois. Une contrainte brutale et immédiate laisserait la victime sans aucune marge.

**La libération, inconditionnelle** — la durée est **bornée**, donc même si le porteur **se déconnecte**,
**meurt**, **change de dimension** ou **perd son Fruit**, la cible se décolle **d'elle-même**. C'est la
seule garantie qui ne dépende pas du bon vouloir de quelqu'un.

`A3c` mesure la séquence complète :

| Étape | Mesure |
|---|---|
| Contact | vitesse **0,23 → 0,09** (ralenti, **pas encore** immobilisé) |
| Au-delà de 60 ticks | **immobilisé**, vitesse **0,0** — jamais les deux à la fois ✅ |
| Après 100 ticks | **libérée**, raison « échéance », vitesse **rendue** ✅, marqueur parti ✅ |

`A3d` : **mort** du porteur → libérée (raison « mort ») ; **scellement** → libérée (raison
« suppression »). Sans ces cas, une cible resterait collée par un porteur **qui n'existe plus**.

### Les constructions : plafonnées, et elles **expirent** *(-05)*

Poser un bloc, c'est **modifier le monde des autres** : le terrain est partagé. D'où deux garanties.

**La population limitée** — sans plafond, un porteur patient couvrirait une zone et **enfermerait** des
joueurs : non pas en les battant, mais en **construisant autour d'eux**. Au plafond, la plus ancienne
construction expire — refuser en silence laisserait croire à un bug.

**L'expiration** — une construction qui ne disparaît pas reste **pour toujours**, y compris après la
déconnexion du porteur : le monde se remplirait de murs que personne ne peut expliquer.

#### Ce que ce Fruit n'a **pas** réécrit

L'expiration, la mémorisation du terrain d'origine, la restitution bloc par bloc et le respect des zones
protégées sont déjà écrits **une fois** dans `TemporaryBlockService`, qui sert au feu de Mera, aux nappes
de Magu et aux gravats de Zushi. **La première version de ce Fruit réécrivait cette machinerie** : elle a
été **supprimée**. Deux vérités concurrentes sur la même question, c'est deux façons de restaurer le
terrain — donc deux façons d'oublier de le faire. `MochiBuild` n'ajoute que ce qui manquait : la **forme**
(un petit cube de mochi) et la **population**.

#### Et une correction de mon propre test

J'avais écrit « une construction ne **creuse** pas : sur la pierre, elle ne doit rien remplacer ». **Faux**,
et c'était **mon** ajout, pas le plan : le service remplace bien les blocs pleins (il ne vérifie que la
protection). La bonne question n'est pas « remplace-t-il ? » mais « **l'original revient-il ?** ».
`A3e` mesure désormais cela : sur la pierre, **24 blocs** posés, pierre **remplacée** pendant, puis
**PIERRE RENDUE à l'identique** ✅. Sans la mémorisation du bloc d'origine, la construction aurait
**détruit** du terrain au lieu de l'occuper temporairement.

### ⚠️ Comment l'expiration par le **temps** est réellement prouvée

Toutes les étapes d'une invocation de test tiennent dans **un seul tick serveur** : elle ne peut donc
**pas** observer une durée. La preuve est donc **séparée** :

1. la portée `mochi` pose un **témoin** à une position **fixe** (`48, 100, 30`) avec **60 ticks** de durée
   de vie (`A3f-temoin-dexpiration-pose`) ;
2. la portée `mochi_expiry` est appelée **plusieurs phases plus tard** — après d'autres portées **et un
   redémarrage du serveur** — et relit **la même position** (`A3f-expiration-par-le-temps`).

Aucun état n'est transporté : c'est **le monde** qui porte le témoin, comme dans le jeu réel. Et le
redémarrage rend la preuve **plus forte** : le bloc a **survécu** au redémarrage (l'état du service est
rechargé) **puis** a expiré.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_mochi_mochi` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **le bras de mochi doit être visiblement du mochi** (pâte crème, inclusions) et **pas** le bras de
      Luffy — seule une observation à l'écran peut le confirmer ;
- [ ] comparer côte à côte un Pistol de Gomu et un Pistol de Mochi : la différence doit être **évidente** ;
- [ ] **l'état collant doit se voir** sur la cible (aujourd'hui un nuage + un son) : une victime immobilisée
      sans retour visuel croira à un bug ;
- [ ] **les constructions de mochi** doivent être reconnaissables, et leur disparition progressive ;
- [ ] la géométrie partagée du bras convient-elle, ou faut-il un modèle dédié ?

## 7. Tests *(-09)*

`/dftest mochi` — **6/6 PASS** · `/dftest mochi_expiry` — **1/1 PASS** (relu plus tard, après redémarrage)

| id | Résultat |
|---|---|
| `A3a-classification-argumentee` | paramecia **true**, logia false, zoan false |
| `A3b-bras-texture-propre-et-collision-du-membre` | texture **mochi** ≠ **luffy**, mêmes règles de collision |
| `A3c-etat-collant-gradue-borne-et-libere` | 0,23 → 0,09 → **0,0** → libérée (échéance) |
| `A3d-liberation-par-mort-et-suppression` | raisons « mort » et « suppression » |
| `A3e-constructions-expirent-sous-plafond` | 24 blocs, plafond **3**, pierre **rendue à l'identique** |
| `A3f-temoin-dexpiration-pose` | témoin posé (60 ticks) |
| `A3f-expiration-par-le-temps` | **vide après plusieurs phases et un redémarrage** |

Reste manuel : tout le §6.

---

## ⚠️ Six erreurs corrigées pendant ce contrat

1. **J'avais réécrit `TemporaryBlockService`** (doublon de machinerie de blocs temporaires) → supprimé,
   délégation à l'existant. Deux vérités concurrentes = deux façons d'oublier de restaurer le terrain.
2. **Test reposant sur une affirmation fausse** (« on ne creuse pas ») → remplacé par la vraie garantie
   (« l'original revient-il à l'identique ? »).
3. **Variable `airAvant` mesurée APRÈS l'action** : le test échouait pour une raison sans rapport avec le
   code. **Un nom ne garantit pas l'ordre.**
4. **Vérification vide** (`Minecraft.getInstance() == null || true`) : elle ne pouvait pas échouer →
   supprimée. Une assertion qui ne peut pas échouer ne prouve rien.
5. **`TemporaryBlockService.tick` est privé** → utilisation du motif **établi** du projet
   (`activeCountFor` / `revokeAll`) plutôt que d'ouvrir une méthode privée pour un test.
6. **Imports manquants** (3 services) — le même piège que pour Okuchi : la **checklist d'imports** du
   skill est complétée pour inclure **les classes référencées par le fichier de test**, pas seulement
   celles du mod.

## ⚠️ Deux défauts trouvés dans l'outillage et dans un test antérieur

**L'audit produisait 4 faux écarts pour ce seul Fruit.** Son repli « premier mot » a apparié la technique
ajoutée `Mochi Build` avec la technique du plan `Mochi Pistol` (mot « Mochi » commun) et a comparé leurs
paramètres : type, recharge, durée, amplificateur — quatre divergences **qui n'existent pas**.
Corrigé : le repli ne s'applique que s'il désigne **une seule** technique. Résultat : **9 écarts** au lieu
de 12, et `Mochi Build` correctement signalée comme ajout. *Un faux positif use la confiance autant
qu'un faux négatif.*

**`W2-souffle-dirige` (contrat Okuchi) s'est révélé intermittent.** Il a échoué **une fois** dans un
`dftest all` complet (0 cible prise, cible devant intacte), puis **passé** seul, en portée couplée
(mochi + okuchi), et lors du `dftest all` suivant — **assertion inchangée**. Cause identifiée :
`getViewVector(partialTicks)` **interpole** entre la rotation du tick **précédent** et la rotation
actuelle. Le test venait de **déplacer et d'orienter** son porteur dans le tick courant : la direction
lue pouvait donc être héritée de l'état antérieur. Corrigé par `setOldPosAndRot()` après le placement,
ce qui rend la mesure déterministe. **Un test intermittent dont le code n'a pas changé est un défaut du
test, pas un hasard** — et il ne doit jamais être « résolu » en le relançant jusqu'à ce qu'il passe.
