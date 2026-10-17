**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Nikyu Nikyu (Paw-Paw Fruit)

36ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. La répulsion : les coussinets repoussent tout — coups, objets, et l'air lui-même (Pad Ho, Ursus Shock).

- **Item** : `minepiece:item_devil_fruit_nikyu_nikyu`
- **Techniques** : **5** (ver15), aucune ajoutée ni retirée
- **Famille** : **Paramecia** (classée et justifiée)
- **Mécaniques du plan couvertes** : classification vérifiée par ses conséquences (-02), exceptions mesurées (-06)

---

## 1. Identité

La répulsion : les coussinets repoussent tout — coups, objets, et l'air lui-même (Pad Ho, Ursus Shock).

## 2. Classification

**Paramecia** — décidée sur le **comportement mesuré** : coup physique ×1,0 (aucune intangibilité de Logia), capacités refusées sous suppression. Le plan interdisait de supposer que `NON_CLASSE` vaut Paramecia : la famille est donc **vérifiée par ses conséquences** (`A10`) et la justification est écrite Fruit par Fruit.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `padho` | Pad Ho | dégâts **10** · durée **80** · recul **2** · énergie **50** · recharge **120** |
| `ursusshock` | Ursus Shock | dégâts **25** · durée **80** · recul **2** · énergie **60** · recharge **160** |
| `assault` | Assault | dégâts **15** · portée **4** · recul **1** · énergie **50** · recharge **100** |
| `kumapunch` | Kuma Punch | dégâts **24** · portée **8** · recul **1.5** · énergie **60** · recharge **140** |
| `passive` | Nikyu Passive | dégâts **0** · portée **0** · recul **0** · énergie **50** · recharge **120** |

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

## ✅ Mécaniques propres — IMPLÉMENTÉES (section `A12`)

Contrairement à son état précédent, ce Fruit ne passe plus par le seul moteur générique : ses trois
mécaniques sont écrites, et chacune a sa preuve. Service : `procedures/NikyuRepulsion.java`.

1. **Répulsion orientée** — `PadHoProcedure` / `TsuppariPadHoProcedure` (balayage 90/100/180°, distances
   14/15/20 blocs). Implémenté comme un **cône** de 105° : ce qui est devant est repoussé, ce qui est
   derrière ne l'est pas ; portée et force sont **plafonnées** (20 blocs, 4,0) ; une cible dont la case
   de destination est pleine **n'est pas** poussée dans le mur (`epargneesParLeDecor`).
   *Preuve `A12a` : poussée devant=1,5 · derrière=0,0 · adossée au mur=0,0 · épargnées par le décor=1.*
2. **Déviation de projectiles** — `NikyuNikyuRightClickProcedure` (munitions `forge:ranged_ammo` + vecteur
   `x_power/y_power/z_power`). Implémenté dans le projectile lui-même
   (`SkillProjectileEntity.onHitEntity`), **avant** tout dégât : direction inversée **et propriétaire
   transféré** — sans ce transfert, le tireur récolterait les dégâts de son propre tir retourné.
   *Preuve `A12b` : dévié=true · vitesse −0,60 → +0,60 · propriétaire transféré=true.*
3. **Onde de pression chargée, zone annoncée** — `UrsusShockProcedure` (`x_pos/y_pos/z_pos` **et** leur
   `_power` ; constantes 19/20/30). Implémenté en **charge puis libération** : la charge monte à chaque
   tick jusqu'à un plafond, puis l'onde **annonce** sa zone pendant 12 ticks **avant** de frapper.
   *Preuves `A12c` (charge 0,0 → 1,0 · annonce=1 · frappes au moment de l'annonce=0) et `A12d` (frappes
   après le délai réel=1) — sans `A12d`, `A12c` ne montrerait qu'une annonce qui ne frappe jamais.*

## Dette signalée

**Classification à confirmer par Donopot** si ce Fruit devait être d'une autre famille : elle est décidée sur le comportement mesuré, faute de classification dans la source.
