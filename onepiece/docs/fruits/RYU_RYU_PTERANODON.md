# Mise à jour lot 07 — code et serveur vérifiés, revue graphique en cours

Voir [adaptations et preuves](../BATCH-07.md). Le cycle comprend maintenant une forme animale complète. Les anciens constats de buff seul ci-dessous sont historiques ; les adaptations de la source sont décrites dans le bilan du lot. Acceptation artistique humaine en attente.

---

# Contrat — Ryu Ryu Pteranodon (Dragon-Dragon Fruit)

32ᵉ Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le ptéranodon : vol, vitesse et piqué (Tankyudon, Barizodon). Catalogue le plus court des Zoan du lot : **3 techniques**.

- **Item** : `minepiece:item_devil_fruit_ryu_ryu_pteranodon`
- **Techniques** : **3** (ver15), aucune ajoutée ni retirée
- **Famille** : **ZOAN**
- **Mécaniques du plan couvertes** : bonds bornés (-04) + combos bornés (-05), partagés entre les 8 Zoan

---

## 1. Identité

Le ptéranodon : vol, vitesse et piqué (Tankyudon, Barizodon). Catalogue le plus court des Zoan du lot : **3 techniques**.

## 2. Classification

**ZOAN** — tag lu, forme vérifiée par ses conséquences (`A9a`), et **×1,0** au coup physique : un Zoan encaisse, il n'est pas intangible. La classification est donc cohérente avec SA résistance, pas seulement avec son tag.

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Valeurs ver15 |
|---|---|---|
| `attackspeed` | Attack Speed | dégâts **0** · portée **0** · recul **0** · énergie **40** · recharge **80** |
| `tankyudon` | Tankyudon | dégâts **15** · portée **3** · recul **0.6** · énergie **50** · recharge **100** |
| `barizodon` | Barizodon | dégâts **30** · durée **80** · recul **0.5** · énergie **50** · recharge **120** |

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
