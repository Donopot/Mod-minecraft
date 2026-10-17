# Mise à jour lot 07 — code et serveur vérifiés, revue graphique en cours

Voir [adaptations et preuves](../BATCH-07.md). Le cycle comprend maintenant une forme animale complète. Les anciens constats de buff seul ci-dessous sont historiques ; les adaptations de la source sont décrites dans le bilan du lot. Acceptation artistique humaine en attente.

---

# Contrat — Inu Inu Okuchi No Makami (Dog-Dog Fruit, Model: Okuchi no Makami)

Dix-septième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Zoan mythique** du
loup-déesse (Yamato). Trois critères, tous exigeants, et l'un d'eux ne porte pas sur un pouvoir mais sur
des **ressources**.

- **Item** : `minepiece:item_devil_fruit_inu_inu_okuchi_no_makami`
- **Techniques** : **6** (ver15) + **1** (défense, décision) = **7**
- **Famille** : **zoan** + `transformable` — vérifiée sur le tag lu (`W0`)
- **Échelle de forme** : **1,5**, force **2** (vs **1** pour le loup ordinaire)

---

## 1. Identité, limites et contre-mesures *(FRUIT-INU_INU_OKUCHI_NO_MAKAMI-01)*

**Identité** — Le porteur prend la forme d'un **loup divin de glace** : souffle glacé, défense de glace,
et une frappe lourde héritée de sa nature de prédateur.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 70 | plan ver15 |
| Recharges | 100 à 180 ticks | plan ver15 |
| **Souffle : portée** | **10,0 blocs** | config |
| **Souffle : demi-angle** | **22,5°** | config |
| **Souffle : arrêté par les murs** | **oui** | config |
| **Défense : durée** | **200 ticks** | config |
| **Défense : coût** | **60 É** (technique) | registre |
| **Défense : exceptions** | feu, suppression, mort, déconnexion | contrat §4 |

**Contre-mesures** : eau de mer (pouvoir scellé → **forme dissoute et défense fondue**), Haki de
l'Armement, énergie insuffisante, retrait du Fruit, et l'exception propre à ce fruit : **le feu**.

## 2. Classification *(FRUIT-INU_INU_OKUCHI_NO_MAKAMI-02)*

`W0` **lit** les tags : `zoan=true`, `transformable=true`. Le fruit transforme son porteur — c'est la
définition du Zoan, et le plan interdit de partir de `NON_CLASSE` par déduction.

Vérifié : `python3 tools/fruit_audit.py --fruit INU_INU_OKUCHI_NO_MAKAMI` → voir §7.

## 3. Catalogue

| Technique | Type | Ciblage | Dégâts | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **Attack Overhead** | mêlée | cône, portée 12 | 15 | 50 | 120 | conservée |
| **Attack Sweep** | mêlée | cône, portée 6 | 12 | 50 | 100 | conservée |
| **Raimei Hakke** | mêlée | cône, portée 4 | 18 | 50 | 120 | conservée |
| **Narikabura** | projectile | — | 15 | 50 | 120 | conservée |
| **Namuji Hyoga** | mêlée | **souffle glacé dirigé** | 15 | 50 | 100 | **spécialisée** |
| **Himorogiri** | mêlée | cône, portée 10 | 30 | 70 | 180 | conservée |
| **Hyoga Heki** | état | soi | 0 | **60** | 160 | **ajout déclaré** (défense, -05) |

⚠️ **Écart d'audit assumé** : `Hyoga Heki` est un ajout. Le plan demande une défense « **avec coût** » —
un pouvoir activé — et aucune des 6 techniques de ver15 n'en est une. Ajout déclaré, traduit en FR/EN
(*Rempart de Glace* / *Ice Rampart*), même statut que les deux ajouts du Fruit Imu.

## 4. Spécialisations

### Formes propres : « aucun partage involontaire avec le loup ordinaire » *(-03)*

C'est le seul critère de tout le projet qui porte sur des **ressources**, et il est **vérifiable
directement**. Le port d'origine contient **deux** fruits-loups — le loup ordinaire (Inu Inu Wolf) et
celui-ci — et le risque est qu'ils partagent le même modèle par commodité.

**Vérifié, commande et sortie réelles :**

| | Okuchi no Makami | Loup ordinaire |
|---|---|---|
| **Classe de modèle** | `ModelinuInuOkuchinomakamiHumanbeast` | `ModelinuInuWolfHumanbeast` **+** `…WolfHumanbeastSlim` |
| **Texture** | `inu_inu_okuchinokami_humanbeast.png` | `inu_inu_wolf_humanbeast.png` |
| **Signature de forme (moteur)** | `echelle=1.5, force=2, resistance=1` | `echelle=1.5, force=1, resistance=1` |

Les deux textures **existent** dans le jar et sont **distinctes** ; les classes de modèle sont des
fichiers **séparés**. `W1` compare en plus les **signatures de forme** que le moteur expose côté serveur :
elles **diffèrent** (force 2 contre 1). Il n'y a donc pas de partage, ni au niveau des ressources, ni à
celui du comportement — et le loup ordinaire garde sa variante « slim » que l'Okuchi n'a pas.

### Le souffle : « souffle, gel et COLLISION » *(-04)*

Le plan : « créer un **souffle glacé dirigé** et des **états de gel bornés** », critère « souffle, gel et
**collision** correspondent au contrat et **la cible est libérée** ».

**Le mot « collision » est celui qui change la conception.** Un souffle est une **matière** : il ne
traverse pas la pierre. Sans cette règle, on gèlerait des cibles **à travers les murs** — invisible à
l'œil du développeur, et profondément injuste pour la victime : elle se ferait figer **sans rien voir
venir** et sans comprendre pourquoi être à couvert n'a servi à rien. C'est exactement le genre de bug
qu'on ne remarque jamais en test rapide.

`W3` le prouve **avec un contrôle positif** : cible derrière un mur plein → **20,0 PV intacte, gel
palier 0** ; **même position, mur retiré** → **5,0 PV, touchée**. Un test qui n'aurait montré que le
premier cas aurait aussi bien validé un souffle cassé qui ne touche personne.

`W2` prouve le **directionnel** : cible devant (5 blocs, dans l'axe) → **5,0 PV** ; cible **derrière** →
**20,0 PV intacte** ; cible **sur le côté** → **20,0 PV intacte**. Le souffle part du regard et ne touche
que son cône.

**Une seule géométrie** : `IceBreath.cone` produit les rayons, `visible` les affiche, `strike` frappe —
les trois lisent **le même** résultat. Ce qui est gelé est exactement ce qui a été montré.

**« La cible est libérée »** : le gel n'est **pas** géré ici. Il est confié au **service de gel partagé**
(celui de Hie Hie), qui porte déjà le **plafond dur** et la **fenêtre de liberté garantie**. `W4` :
12 gels d'affilée → palier **3** (plafond), puis palier **0** après la durée. Ce n'est pas une
réutilisation de commodité : c'est la **même règle**, et deux gels concurrents seraient deux façons d'être
prisonnier — donc une qui oublie de libérer.

### Défense de glace : coût, exceptions, et expiration propre *(-05)*

Le plan : « créer une **défense de glace temporaire** avec **coût** et **exceptions** », critère « la
défense **expire sans supprimer d'autres bonus** du joueur ».

**« Sans supprimer d'autres bonus »** impose l'implémentation. La tentation est d'appliquer
`RESISTANCE` : à l'expiration, on ne saurait plus distinguer *notre* résistance de celle d'une autre
source, et on la lui retirerait. La défense utilise donc un **effet marqueur dédié** (`ice_defense`) et
des **modificateurs identifiés par ressource** (`ice_defense_armor`, `ice_defense_toughness`), retirés
**nominativement**.

`W6` pose un bonus **étranger** avant l'expiration : modificateurs d'armure **2 → 1** (le nôtre seul est
parti), et **l'étranger survit**. Le critère est tenu **par construction**, pas par vigilance.

**Les exceptions** — une défense sans exception est une invulnérabilité :

| Exception | Traitement | Preuve |
|---|---|---|
| **Le feu la fait fondre** | `isOnFire()` / `getRemainingFireTicks() > 0` / lave → fin | `W7` : active → **fondue** (raison « fonte ») ; **contrôle positif sans feu** → tient |
| **Le pouvoir scellé la dissout** | suppression → fin | mécanisme commun |
| **Mort et déconnexion** | → fin | la leçon de Horo Horo, appliquée d'emblée |

`W5` vérifie le **coût** : **60 É** déclarés, recharge 160 ticks, armure **0,0 → 6,0**. La défense se paie
et se voit.

### Formes : modèle, dimensions, retour à la normale *(-06)*

Le constat du plan (« `applyForm` fournit des bonus de potions et **aucun modèle joueur** ») correspondait
à l'état **avant** l'extraction des modèles : la pile de transformation existe désormais
(`PlayerTransformLayer`, §4 -03), et `W8` prouve la partie **testable sans écran** — transformation
réelle ouverte à l'échelle **1,5**, modificateurs de vitesse **0 → 1 → 0** : les attributs **reviennent à
la normale**.

⚠️ **La partie client du critère reste ouverte** : « transformation visible **aux deux clients** » exige
un écran. Elle est au §7, pas revendiquée ici.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **mythique** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_inu_inu_okuchi_no_makami` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **la forme de loup divin** doit se distinguer **au premier coup d'œil** de celle du loup
      ordinaire : c'est tout l'objet du critère -03, et seule une comparaison à l'écran peut le confirmer ;
- [ ] **le souffle doit être lisible comme une matière** : on doit voir le cône s'arrêter au mur, sinon
      la règle de collision est incompréhensible pour le joueur ;
- [ ] **la défense doit se voir** sans masquer le personnage, et sa **fonte au feu** doit être visible ;
- [ ] transformation aux deux clients.

## 7. Tests *(-09)*

`/dftest okuchi` — **9/9 PASS** (du premier coup) :

| id | Résultat |
|---|---|
| `W0-decor-et-classification` | zoan=true, transformable=true |
| `W1-formes-propres-aucun-partage-avec-le-loup` | signatures **différentes** (force 2 vs 1) |
| `W2-souffle-dirige` | devant **5,0 PV** · derrière **20,0** · côté **20,0** |
| `W3-mur-arrete-le-souffle` | à couvert **20,0 PV** / mur retiré **5,0 PV** |
| `W4-gel-borne-et-cible-liberee` | palier **3** → puis **0** |
| `W5-defense-avec-cout-et-bonus` | **60 É**, armure **0,0 → 6,0** |
| `W6-expire-sans-supprimer-d-autres-bonus` | 2 → 1 modificateurs, **l'étranger survit** |
| `W7-exception-le-feu-fait-fondre` | fondue **au feu**, tient **sans feu** |
| `W8-forme-attributs-reversibles` | échelle 1,5, vitesse **0 → 1 → 0** |

Reste manuel : tout le §6.

---

## ⚠️ Incident corrigé pendant ce contrat

En insérant le bloc de ce Fruit dans le registre, j'ai **écrasé l'en-tête de Horo Horo** au lieu
d'insérer un bloc avant : les 5 techniques des fantômes se sont retrouvées rattachées à Okuchi.
**Le code compilait** — la corruption était invisible. Détectée en vérifiant, pour chaque fruit, le
contenu de son bloc (`MOVES.put(<item>, List.of(…))`), puis corrigée : Okuchi **7** techniques, Horo ses
**5** d'origine. La leçon est consignée dans le skill, avec la commande de vérification.
