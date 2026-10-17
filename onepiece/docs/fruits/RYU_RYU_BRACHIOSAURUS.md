# Mise à jour lot 08 — code et serveur vérifiés, revue graphique en cours

Voir [adaptations et preuves](../BATCH-08.md). Le cycle comprend maintenant une forme animale complète. Les anciens constats de buff seul ci-dessous sont historiques ; les adaptations de la source sont décrites dans le bilan du lot. Acceptation artistique humaine en attente.

---

# Contrat — Ryu Ryu Brachiosaurus (Dragon-Dragon Fruit)

30ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le brachiosaure : allonge et masse (Brachio Launcher, Brachio Bober), plus un souffle (Sparking Queen).

- **Item** : `minepiece:item_devil_fruit_ryu_ryu_brachiosaurus`
- **Techniques** : **5** (ver15), aucune ajoutée ni retirée
- **Famille** : **ZOAN**
- **Mécaniques du plan couvertes** : bonds bornés (-04) + combos bornés (-05), partagés entre les 8 Zoan

---

## 1. Identité

Le brachiosaure : allonge et masse (Brachio Launcher, Brachio Bober), plus un souffle (Sparking Queen).

## 2. Classification

**ZOAN** — tag lu, forme vérifiée par ses conséquences (`A9a`), et **×1,0** au coup physique : un Zoan encaisse, il n'est pas intangible. La classification est donc cohérente avec SA résistance, pas seulement avec son tag.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `attackoverhead` | Attack Overhead | dégâts **15** · portée **12** · recul **1** · énergie **50** · recharge **120** |
| `brachiobober` | Brachio Bober | dégâts **25** · portée **10** · recul **1** · énergie **60** · recharge **140** |
| `blackcofee` | Black Cofee | dégâts **17** · durée **80** · recul **1** · énergie **50** · recharge **120** |
| `brachiolauncher` | Brachio Launcher | dégâts **2.2** · durée **80** · recul **0.5** · énergie **40** · recharge **80** |
| `sparkingqueen` | Sparking Queen | dégâts **16** · durée **80** · recul **0.5** · énergie **50** · recharge **120** |

## 5. Acquisition, rareté, progression *(-08)*

Identique aux autres Fruits. **Aucun déblocage par maîtrise** — lacune **générale** du port, consignée
pour tous les Fruits précédents.

## 6. Validation visuelle *(-07 — exige un client)*

- [ ] icône, modèle, **deux mains**, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **formes hybride et animale** : modèle visible, dimensions et collision jouables, caméra ;
- [ ] bond : la traînée montre le trajet et s'arrête net au mur ;
- [ ] combo : les impacts se voient, sans clignotement anormal ;

## 7. Tests *(-09)*

| Vérification (section `A9`, lot des 8 Zoan) | Ce qu'elle prouve |
|---|---|
| `A9a-zoans-classification-catalogue-formes` | tag zoan + transformable, catalogue, **signature de forme propre**, transformation **et retour** |
| `A9a-zoans-ne-sont-pas-intangibles` | ×1,0 pour les **huit** : un Zoan n'est pas un Logia |
| `A9b-suppression-retire-la-forme` | scellé ⇒ pas de transformation, **sans état à moitié modifié** |
| `A9c-bond-borne` | 999 blocs demandés ⇒ bond **≤ 16** : la borne est dans le code |
| `A9c-bond-s-arrete-a-l-obstacle` | mur à 3 blocs ⇒ bond de **2,0 blocs**, arrêté net |
| `A9d-combo-repartit-les-degats` | **total du combo = dégâts du catalogue** (égalité de calcul) |
| `A9d-combo-touche-et-recharge-facturee-une-fois` | impacts bornés, recharge armée **une** fois |

**Recette complète : voir le résultat du lot dans le rapport de livraison.**

## Dette signalée

**Forme animale complète (quadrupède) : inexistante dans toutes les versions**, jar source inclus (décision ouverte pour Donopot).
