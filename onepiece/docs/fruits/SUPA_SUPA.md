# Travail actuel — lot03 alpha.12

ProjectileSlash lance effectivement une lame ; AtomicSpurt déplace le corps et conserve le terrain ; Daisy dessine un éventail et SparBreak deux coupes croisées. Le revêtement de lames apparaît pendant la technique, sans forcer player_form. Une posture emploie un modificateur propre unique (+3attaque ou+5armure), puis se retire avec les lames. Les potions Strength/Resistance externes sont conservées. Les trois attaques destructrices partagent les quotas de leur lancement et protègent inventaires, zones et murs résistants.

[Lot courant](../BATCH-03.md), [décisions terrain](../TERRAIN-POLICY.md). Recette serveur et rendu groupé vérifiés : [bilan final](../B03-VALIDATION-2026-10-16.md). Acceptation artistique humaine en attente. Les anciennes sections ci-dessous restent historiques et ne définissent pas le comportement corrigé.

---

# Contrat — Supa Supa (Dice-Dice Fruit)

39ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le corps tranchant : le porteur **est** une lame (Atomic Spurt, Sparkling Daisy, Spar Break).

- **Item** : `minepiece:item_devil_fruit_supa_supa`
- **Techniques** : **4** (ver15), aucune ajoutée ni retirée
- **Famille** : **Paramecia** (classée et justifiée)
- **Mécaniques du plan couvertes** : classification vérifiée par ses conséquences (-02), exceptions mesurées (-06)

---

## 1. Identité

Le corps tranchant : le porteur **est** une lame (Atomic Spurt, Sparkling Daisy, Spar Break).

## 2. Classification

**Paramecia** — décidée sur le **comportement mesuré** : coup physique ×1,0 (aucune intangibilité de Logia), capacités refusées sous suppression. Le plan interdisait de supposer que `NON_CLASSE` vaut Paramecia : la famille est donc **vérifiée par ses conséquences** (`A10`) et la justification est écrite Fruit par Fruit.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `projectileslash` | Projectile Slash | dégâts **30** · durée **80** · recul **0.5** · énergie **50** · recharge **120** |
| `atomicspurt` | Atomic Spurt | dégâts **16** · portée **5** · recul **1** · énergie **50** · recharge **120** |
| `sparklingdaisy` | Sparkling Daisy | dégâts **18** · portée **5** · recul **1** · énergie **60** · recharge **140** |
| `sparbreak` | Spar Break | dégâts **15** · portée **5** · recul **1** · énergie **60** · recharge **140** |

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

## ✅ Mécaniques propres — IMPLÉMENTÉES (section `A15`)

Service : `procedures/SupaBlades.java`. La source arrête sa lame au mur (`ProjectileSlashInsideWallProcedure`
+ `InsideSolidCalculateProcedure`), déclare `BlockDamage`/`BlockRange` dans `SparBreakProcedure`, et lit
`Attributes.f_22276_` — donc une posture peut modifier la puissance.

1. **Lames visibles, allonge cohérente** — la portée de mêlée s'allonge de 2,5 blocs **seulement** quand
   les lames sont sorties, et les dégâts suivent cette portée. *`A15a` : 3,0 → 5,5 · actives=true.*
2. **Postures réversibles** — on **retire** l'ancienne posture avant de poser la nouvelle : 6 bascules
   laissent **1** effet au maximum, puis 0 au retour à aucune posture. *`A15b` : max=1 · après=0.*
3. **Découpe bornée, protections intactes** — liste explicite de refus, et chaque refus **compté**.
   *`A15c` : découpés=1 · coffre intact=true · lit intact=true · refus comptés=2.*

## Dette signalée

**Classification à confirmer par Donopot** si ce Fruit devait être d'une autre famille : elle est décidée sur le comportement mesuré, faute de classification dans la source.
