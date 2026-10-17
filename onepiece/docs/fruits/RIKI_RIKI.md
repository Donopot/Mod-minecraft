# Travail actuel — lot03 alpha.12

Overhead et Hado ont une préparation puis un contact confirmé ; Lariat avance et lance un balayage. Les deux pierres sont de vrais projectiles disponibles sans capture préalable ; elles modifient le terrain au contact, jamais à un point distant dès l’appui. Hado n’ajoute pas de capture spéculative. Les dégâts du catalogue restent des budgets adaptés explicites (15/18/20/40/60), sans multiplicateur Riki automatique ajouté par l’attribut du porteur. Les valeurs source15/50/200 des entités correspondent à MAX_HEALTH ; leurs attaques passent par RockFragment/RangeAttack/DamageFix. Le helper historique de capture reste diagnostique et borne sa durée et son nettoyage.

[Lot courant](../BATCH-03.md), [décisions terrain](../TERRAIN-POLICY.md). Recette serveur et rendu groupé vérifiés : [bilan final](../B03-VALIDATION-2026-10-16.md). Acceptation artistique humaine en attente. Les anciennes sections ci-dessous restent historiques et ne définissent pas le comportement corrigé.

---

# Contrat — Riki Riki (Strong-Strong Fruit)

37ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. La force brute : aucune transformation, aucun élément — frappes écrasantes et lancer de pierre (Galleon Lariat, Stone Throwing).

- **Item** : `minepiece:item_devil_fruit_riki_riki`
- **Techniques** : **5** (ver15), aucune ajoutée ni retirée
- **Famille** : **Paramecia** (classée et justifiée)
- **Mécaniques du plan couvertes** : classification vérifiée par ses conséquences (-02), exceptions mesurées (-06)

---

## 1. Identité

La force brute : aucune transformation, aucun élément — frappes écrasantes et lancer de pierre (Galleon Lariat, Stone Throwing).

## 2. Classification

**Paramecia** — décidée sur le **comportement mesuré** : coup physique ×1,0 (aucune intangibilité de Logia), capacités refusées sous suppression. Le plan interdisait de supposer que `NON_CLASSE` vaut Paramecia : la famille est donc **vérifiée par ses conséquences** (`A10`) et la justification est écrite Fruit par Fruit.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `attackoverhead` | Attack Overhead | dégâts **15** · portée **12** · recul **1** · énergie **50** · recharge **120** |
| `hadoelbow` | Hado Elbow | dégâts **18** · portée **8** · recul **2** · énergie **50** · recharge **120** |
| `galleonlariat` | Galleon Lariat | dégâts **18** · portée **6** · recul **1.5** · énergie **50** · recharge **120** |
| `stonethrowing` | Stone Throwing | dégâts **15** · durée **80** · recul **0.5** · énergie **50** · recharge **100** |
| `stonethrowinglarge` | Stone Throwing (large) | dégâts **200** · durée **80** · recul **1** · énergie **70** · recharge **180** |

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

## ✅ Mécaniques propres — IMPLÉMENTÉES (section `A13`)

Service : `procedures/RikiForce.java`. La source lit `Attributes.f_22276_` (**ATTACK_DAMAGE**) dans
`GalleonLariatProcedure`, et écrit `BlockDamage`/`BlockRange` dans `HadoElbowProcedure` avant
`BlockDestroyAllDirectionProcedure`.

1. **Multiplicateur borné et conditionné** — adossé à `ATTACK_DAMAGE`, plafonné à 1,5, et **annulé sous
   scellement**. *`A13a` : normal=1,02 · ATTACK_DAMAGE à 200 → 1,5 (plafond tenu) · scellé=1,0.*
2. **Saisie brève, toujours rendue** — une prise unique par porteur ; sortie unique pour **cinq** causes
   (échéance, mort de la cible, mort du porteur, déconnexion, remplacement). *`A13b` : la 2ᵉ prise
   remplace la 1ʳᵉ · après libération, tient encore=false.*
3. **Projection bornée, blocs protégés intacts** — trajet mesuré avant la poussée, le mur arrête ; la
   casse épargne une liste explicite (lit, barrière, coffre, générateur, obsidienne). *`A13c` : trajet
   libre=1,25 bloc (mur) · mur entamé=true · coffre intact=true · cassés=3 · épargnés≥1.*

## Dette signalée

**Classification à confirmer par Donopot** si ce Fruit devait être d'une autre famille : elle est décidée sur le comportement mesuré, faute de classification dans la source.
