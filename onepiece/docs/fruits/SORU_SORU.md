# État lot05 : implémenté, testé serveur et vu en laboratoire

Voir [validation](../B05-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-05.md). Acceptation artistique humaine en attente ; les affirmations historiques ci-dessous sont à lire avec les corrections de source du lot05.

---

# Contrat — Soru Soru (Soul-Soul Fruit)

38ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Projectiles élémentaires (Raitei, Hebunri Fire, Furugora, Mama Raid) **sans intangibilité** : la famille est décidée sur le comportement **mesuré**.

- **Item** : `minepiece:item_devil_fruit_soru_soru`
- **Techniques** : **7** (ver15), aucune ajoutée ni retirée
- **Famille** : **Paramecia** (classée et justifiée)
- **Mécaniques du plan couvertes** : classification vérifiée par ses conséquences (-02), exceptions mesurées (-06)

---

## 1. Identité

Projectiles élémentaires (Raitei, Hebunri Fire, Furugora, Mama Raid) **sans intangibilité** : la famille est décidée sur le comportement **mesuré**.

## 2. Classification

**Paramecia** — décidée sur le **comportement mesuré** : coup physique ×1,0 (aucune intangibilité de Logia), capacités refusées sous suppression. Le plan interdisait de supposer que `NON_CLASSE` vaut Paramecia : la famille est donc **vérifiée par ses conséquences** (`A10`) et la justification est écrite Fruit par Fruit.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `attackoverhead` | Attack Overhead | dégâts **15** · portée **12** · recul **1** · énergie **50** · recharge **120** |
| `attacksweep` | Attack Sweep | dégâts **12** · portée **6** · recul **1** · énergie **50** · recharge **100** |
| `raitei` | Raitei | dégâts **15** · portée **6** · recul **1** · énergie **60** · recharge **140** |
| `hebunrifire` | Hebunri Fire | dégâts **20** · portée **6** · recul **1** · énergie **60** · recharge **140** |
| `furugora` | Furugora | dégâts **16** · portée **8** · recul **0.5** · énergie **50** · recharge **120** |
| `mamaraid` | Mama Raid | dégâts **25** · portée **12** · recul **1** · énergie **70** · recharge **180** |
| `ikoku` | Ikoku | dégâts **30** · portée **20** · recul **2** · énergie **70** · recharge **180** |

## 5. Acquisition, rareté, progression *(-08)*

Identique aux autres Fruits. **Aucun déblocage par maîtrise** — lacune **générale** du port, consignée
pour tous les Fruits précédents.

## 6. Validation visuelle *(-07 — exige un client)*

- [ ] icône, modèle, **deux mains**, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] visuels de pouvoirs (projectiles, effets) conformes à la source ;



## 7. Tests *(-09)*

| Vérification (section `A10`) | Ce qu'elle prouve |
|---|---|
| `A10-sans-famille-classes-et-justifies` | tag paramecia, catalogue non vide, **×1,0** (pas d'intangibilité), **refus sous suppression** — mesurés pour les **cinq** Fruits que la source ne classait pas |

**Recette complète : voir le résultat du lot dans le rapport de livraison.**

## ✅ Mécaniques propres — IMPLÉMENTÉES (section `A14`)

Service : `procedures/SoruSummons.java`. La source suit ses deux invocations par UUID (`ZEUS_UUID` dans
`RaiteiProcedure`, `PROMETHEUS_UUID` dans `HebunriFireProcedure`) et `LogicOwnerExistProcedure`.

**Le défaut que ce service corrige** : le lancement générique comptait les invocations **par proximité**
(128 blocs). Une invocation laissée loin — reconnexion, rappel — échappait au plafond. Le registre
compte par **propriétaire et par genre** : un rappel **remplace**, il n'ajoute pas.

1. **Deux invocations distinctes, qui finissent** — Zeus (ordre ATTAQUER) et Prometheus (ordre DEFENDRE),
   modèles distincts, durée 200 ticks chacun. *`A14a` : ordres distincts, durées 200, enregistrées=2.*
2. **Rappel sans duplication** *`A14b` : rappel de Zeus → enregistrées=2 (jamais 3) · remplacements=1 ·
   l'ancienne écartée=true.*
3. **Propriétaire et ciblage** — règles **séparées** : seul le propriétaire commande, et les alliés ne
   sont pas visés. *`A14c` : propriétaire=true · allié refusé=true · maître épargné=true · hostile visé=true.*

## Dette signalée

**Classification à confirmer par Donopot** si ce Fruit devait être d'une autre famille : elle est décidée sur le comportement mesuré, faute de classification dans la source.
