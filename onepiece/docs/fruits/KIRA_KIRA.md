# Travail actuel — lot03 alpha.12

Overhead, la charge BrilliantPunk et les deux projectiles ont des phases et silhouettes distinctes. DiamondBody ajoute un revêtement natif indépendant des formes Zoan, synchronisé par UUID aux observateurs et à leur retour dans la zone. C’est une capacité de durée fixe200ticks via la sélection joueur : la recharge ne permet pas une désactivation immédiate. L’armure propre est bornée au plafond configuré ; les équipements/modificateurs externes ne sont pas supprimés. Le corps expire sur fin, mort, perte du fruit, suppression, dimension ou déconnexion. DiamondBody et l’identité diamant des pierres restent des adaptations demandées par le plan, distinctes des quatre gestes originaux.

[Lot courant](../BATCH-03.md), [décisions terrain](../TERRAIN-POLICY.md). Recette serveur et rendu groupé vérifiés : [bilan final](../B03-VALIDATION-2026-10-16.md). Acceptation artistique humaine en attente. Les anciennes sections ci-dessous restent historiques et ne définissent pas le comportement corrigé.

---

# Contrat — Kira Kira no Mi (Diamond Fruit)

Vingtième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Paramecia** de Jozu. Son
contrat porte une exigence que les autres n'avaient pas : **ne pas être invulnérable**, et le prouver.

- **Item** : `minepiece:item_devil_fruit_kira_kira`
- **Techniques** : **5** — 3 conservées, **2 remplacées** (jets de pierre), **1 ajoutée** (corps de diamant)
- **Famille** : **`paramecia`** — argumentée, pas déduite (`Z0`)
- **Réduction** : mesurée **32,43 %**, plafond **déclaré 60 %**

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **durcit son corps en diamant** : il encaisse des coups qui devraient le tuer,
et frappe avec la même matière.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 70 | plan ver15 |
| **Corps de diamant : durée** | **200 ticks** | config |
| Corps : coût / recharge | **60 É** / 200 ticks | config |
| **Armure** | **+12,0** (résistance +4,0) | config |
| **Réduction mesurée** | **32,43 %** | `Z2` |
| **Plafond déclaré** | **60 %** | config |

**Contre-mesures** : eau de mer (pouvoir scellé → **état terminé**), retrait du Fruit (idem), Haki,
énergie insuffisante, **et tout dégât qui ignore l'armure** (§4).

## 2. Classification : argumentée, pas déduite *(-02)*

Le plan avertit : « ajouter le tag correct **sans supposer que NON_CLASSE signifie Paramecia** ». `Z0`
lit les trois tags — `paramecia` **true**, `logia` false, `zoan` false.

**L'argument** : ce Fruit ne transforme pas le corps en élément (pas **Logia**) ni le porteur en animal
(pas **Zoan**) — il **durcit le corps**, qui reste le sien et **reste vulnérable**. C'est un **Paramecia**.

## 3. Catalogue

| Technique | Type | Dégâts | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|
| **Attack Overhead** | mêlée | 15 | 50 | 120 | conservée |
| **Brilliant Punk** | mêlée | 15 | 50 | 100 | conservée |
| ~~Stone Throwing~~ → **Diamond Shard** | projectile | **15** | 50 | 100 | **remplacée** (-05) |
| ~~Stone Throwing (large)~~ → **Diamond Avalanche** | projectile | **200** | 70 | 180 | **remplacée** (-05) |
| **Diamond Body** *(Corps de Diamant)* | état | 0 | **60** | 200 | **ajoutée** (-03/-04) |

⚠️ **Écarts d'audit assumés** : 2 renommages + 1 ajout. Les deux renommages répondent mot pour mot à
l'action -05 ; l'ajout est nécessaire parce qu'**aucune** des 4 techniques de ver15 n'est un état activé,
et le plan demande une activation/désactivation observables.

⚠️ **Les PARAMÈTRES des remplacements sont INCHANGÉS** (15 et 200 dégâts). La fidélité ver15 prime, et le
plan demande un remplacement d'**identité**, pas un rééquilibrage. En particulier, les **200 dégâts** de
*Diamond Avalanche* sont une valeur ver15 **partagée** (elle appartient aussi à Riki Riki, comme Raigo
appartient à Goro avec 800) : **signalée, pas modifiée** — changer la puissance exige une demande
explicite.

## 4. Spécialisations

### « Visibles pour les OBSERVATEURS » — le mot de trop, et il change tout *(-03)*

Le critère ne dit pas « visible pour le porteur ». Un pouvoir qui ne se voit que de celui qui le lance
**ne communique rien** : les autres joueurs ne peuvent ni le reconnaître, ni l'anticiper, ni comprendre
pourquoi leurs coups ne portent pas.

Chaque transition est donc **diffusée au monde** (`level.sendParticles` + `playSound` au volume normal) :
**60** particules à l'activation, **25** à la désactivation, plus un son. `Z1` vérifie les **deux** sens —
l'activation **et** la désactivation, car un observateur qui ne voit pas la protection tomber continue de
jouer contre un pouvoir qui n'existe plus.

### Pas d'invulnérabilité absolue *(-04)*

Le plan est explicite : « le **plafond** de défense et les **exceptions** évitent l'**invulnérabilité
absolue** non prévue ». Un diamant réellement indestructible ne serait plus un pouvoir : ce serait la
**suppression du jeu** pour tout le monde sauf son porteur.

**Le plafond est une valeur DÉCLARÉE en configuration** (`max_reduction_percent = 60`), donc **lisible et
vérifiable** — et non un effet de bord de la formule d'armure qu'on constaterait après coup. Le bonus
d'armure est en outre **borné par ce plafond** là où il est consommé.

`Z2` mesure pour de vrai : **armure 0 → 12**, réduction estimée **32,43 %**, sous le plafond de **60 %**,
jamais 100 %.

**Les exceptions ne sont pas une liste ajoutée à la main** : ce sont les dégâts qui **ignorent l'armure**,
sémantique portée par la source elle-même (`DamageTypeTags.BYPASSES_ARMOR`). `Z3` la **lit** :

| Source | Armure appliquée |
|---|---|
| Attaque physique | **oui** |
| Magie | **non** |
| Noyade | **non** |
| Chute | **non** |

Une armure de diamant protège des coups, pas de la noyade ni d'un sort : c'est ce que **le jeu sait déjà
dire**, et il vaut mieux lire cette règle que la réécrire.

### « Remplacer les jets de pierre génériques » — le mot « générique » était précis *(-05)*

`Stone Throwing` et `Stone Throwing (large)` étaient **partagés avec Riki Riki** — d'où le mot
« générique » du plan. Le remplacement est donc **ciblé** : Kira Kira reçoit **Diamond Shard** et
**Diamond Avalanche**, et `Z4` vérifie que **Riki Riki les garde**. Une suppression globale aurait cassé
un autre fruit — et ce test-là existait uniquement parce qu'on avait vérifié **à qui** ces techniques
appartenaient avant d'y toucher.

`Z4` vérifie aussi que **chaque action est résolvable** (« chaque action est accessible » dans le
critère) : les 5 identifiants du catalogue répondent.

### Le rendu : un actif RÉUTILISÉ, pas inventé

Le visuel des projectiles passe de `rock_fragment` (la pierre générique) à **`ice_ball` + `tex_ice`** —
l'actif **cristallin facetté** déjà présent dans le jeu. C'est le plus proche du diamant parmi les
ressources existantes, et il est **réutilisé** plutôt qu'inventé (principe « extraire et réutiliser tout
ce qu'on peut »). Un actif dédié serait un mieux → §6.

### Nettoyage *(-06)*

`Z5` : expiration après **200 ticks**, raison « échéance », marqueur retiré, modificateurs **2 → 1** (les
nôtres seuls), et — le point qui compte — un **bonus étranger** et un **effet étranger** posés avant
**survivent**. On retire **nominativement** ce qu'on a posé.

`Z6` : **le sceau et la mort terminent l'état** (raisons « suppression » et « mort »). L'armure ne reste
pas collée au joueur ressuscité.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_kira_kira` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **le corps de diamant doit être reconnaissable au premier regard** — c'est le cœur du critère -03,
      et seule une observation à l'écran peut le confirmer ;
- [ ] **l'activation et la désactivation doivent être lisibles à distance** par un autre joueur ;
- [ ] **Diamond Shard / Diamond Avalanche** : l'actif `ice_ball` doit-il devenir un actif **diamant
      dédié** ? À trancher après l'avoir vu (l'aspect facetté convient, la teinte est celle de la glace) ;
- [ ] vérifier qu'**Attack Overhead** partagé avec huit autres fruits ne crée pas de confusion visuelle.

## 7. Tests *(-09)*

`/dftest kira` — **7/7 PASS** :

| id | Résultat |
|---|---|
| `Z0-classification-argumentee` | paramecia **true**, logia false, zoan false |
| `Z1-activation-et-desactivation-visibles` | activation **et** désactivation, observateur à 5 blocs |
| `Z2-plafond-declare-jamais-invulnerable` | armure **0 → 12**, réduction **32,43 %** < plafond **60 %** |
| `Z3-exceptions-portees-par-la-source` | physique **oui** / magie, noyade, chute **non** |
| `Z4-catalogue-diamant-distinct` | 5 actions, pierre retirée, **Riki Riki intact** |
| `Z5-expiration-sans-toucher-aux-etats-externes` | 2 → 1 modificateurs, étrangers **intacts** |
| `Z6-suppression-et-mort-terminent-letat` | raisons « suppression » et « mort » |

Reste manuel : tout le §6.

---

## ⚠️ Une erreur de mesure corrigée dans ce contrat

La première version de `Z2` affichait un « **bonus d'armure de -8,0** ». Le code était **correct** :
c'était **mon hypothèse** qui était fausse — j'avais supposé une armure de base de **20** pour un joueur,
alors qu'un joueur sans équipement a une armure de base de **0**. Une mesure se prend par **différence**,
jamais par rapport à une base supposée : le signe négatif était le seul indice, et il aurait pu passer
pour un détail de calcul.
