# État lot05 : implémenté, testé serveur et vu en laboratoire

Voir [validation](../B05-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-05.md). Acceptation artistique humaine en attente ; les affirmations historiques ci-dessous sont à lire avec les corrections de source du lot05.

---

# Contrat — Uta Uta (Sing-Sing Fruit)

40ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le chant : notes projetées, bouclier, invocations (Note Warrior, Tot Musica).

- **Item** : `minepiece:item_devil_fruit_uta_uta`
- **Techniques** : **7** (ver15), aucune ajoutée ni retirée
- **Famille** : **Paramecia** (classée et justifiée)
- **Mécaniques du plan couvertes** : classification vérifiée par ses conséquences (-02), exceptions mesurées (-06)

---

## 1. Identité

Le chant : notes projetées, bouclier, invocations (Note Warrior, Tot Musica).

## 2. Classification

**Paramecia** — décidée sur le **comportement mesuré** : coup physique ×1,0 (aucune intangibilité de Logia), capacités refusées sous suppression. Le plan interdisait de supposer que `NON_CLASSE` vaut Paramecia : la famille est donc **vérifiée par ses conséquences** (`A10`) et la justification est écrite Fruit par Fruit.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `noteattack` | Note Attack | dégâts **10** · durée **80** · recul **0.5** · énergie **50** · recharge **100** |
| `notelaser` | Note Laser | dégâts **10** · durée **80** · recul **0.5** · énergie **50** · recharge **100** |
| `posing` | Posing | dégâts **20** · portée **8** · recul **1** · énergie **60** · recharge **140** |
| `noteshield` | Note Shield | dégâts **20** · portée **8** · recul **1** · énergie **60** · recharge **140** |
| `notewarrior` | Note Warrior | dégâts **15** · portée **6** · recul **1** · énergie **60** · recharge **140** |
| `totmusicapunch` | Tot Musica Punch | dégâts **30** · portée **6** · recul **1** · énergie **60** · recharge **140** |
| `summontotmusica` | Summon Tot Musica | dégâts **30** · portée **8** · recul **1.5** · énergie **100** · recharge **300** |

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

## ✅ Mécaniques propres — IMPLÉMENTÉES (section `A16`)

Service : `procedures/UtaDomain.java`. La source vérifie que le maître existe encore
(`LogicOwnerExistProcedure`), suit son invocation par `OWNER_UUID` et la fait mourir (`flag_die`).

**Le défaut que ce service évite** : un domaine qui applique son effet **à chaque tick** transforme un
concert en sirène d'usine. Ici, une application par **mesure** (20 ticks), durée bornée à 200.

1. **Zone rythmée et bornée** — *`A16a` : domaine ouvert · durée 200/200 · mesures au démarrage=0* ;
   la cadence et l'échéance sont mesurées sur des ticks réels par la phase `uta_domain`.
2. **Sortie garantie, quatre causes comptées séparément** — mort, déconnexion, scellement, échéance.
   *`A16b` : mort=1 · déconnexion=1 · scellement=1 · sortie effective=true.*
3. **Tot Musica fonctionnel et borné** — invocation avec propriétaire, durée 200, retirée à la fermeture
   du domaine. *`A16c` : invoqué=true · propriétaire=true · durée=200 · retiré=true.*

## Dette signalée

**Classification à confirmer par Donopot** si ce Fruit devait être d'une autre famille : elle est décidée sur le comportement mesuré, faute de classification dans la source.
