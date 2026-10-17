**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Mise à jour alpha.8 — lot01,16octobre2026

Les5techniques de ce fruit ont été reprises avec les quatre autres fruits du lot. Les budgets, routes, adaptations et limites actuelles sont décrits dans [B01-IMPLEMENTATION](../B01-IMPLEMENTATION-2026-10-16.md), qui remplace les anciennes descriptions de comportement ci-dessous lorsqu’elles divergent. [Résultats et hash des tests](../B01-VALIDATION-2026-10-16.md). Implémentation, preuve serveur, observation client et acceptation artistique restent distinctes ; l’essai humain est attendu.

---

# Contrat — Suna Suna (Sand-Sand Fruit)

Onzième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Logia du sable — le fruit de
Crocodile. Le plan y pose une question de méthode inhabituelle : non pas « quels effets », mais
« **qu'est-ce qui ne doit surtout pas changer** ».

- **Item** : `minepiece:item_devil_fruit_suna_suna`
- **Techniques** : 5 (ver15) — Desert Spada, Barjan, Sables, Desert La Spada, Ground Death
- **Famille** : `logia`

---

## 1. Identité, limites et contre-mesures *(FRUIT-SUNA_SUNA-01)*

**Identité** — Logia du sable : le corps se disperse, les attaques physiques le traversent, et le
porteur **assèche** ce qu'il touche. Le sable boit l'eau des corps.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 70 | plan ver15 |
| Recharges | 100 à 160 ticks | plan ver15 |
| **Dessiccation : paliers** | **3** | config |
| Dessiccation : durée d'un palier | 80 ticks | config |
| Dessiccation : dégâts au palier max | 2,0 / s | config |
| **Tempête : rayon × hauteur** | **4,0 × 3,0** | config |
| Tempête : durée | 160 ticks | config |
| Tempête : dégâts | 3,0 / s | config |
| **Cible humide** | **résiste** | config |

**Contre-mesures** : **l'eau** (une cible humide ne se dessèche pas et se libère — la contre-mesure
propre à ce fruit), eau de mer (SEA_POWER → pouvoir scellé), Haki de l'Armement, énergie insuffisante
(refus sans coût), retrait du Fruit (reset + `Desiccation.clearAll()` + `SandStorm.clearAll()`).

## 2. Classification *(FRUIT-SUNA_SUNA-02)*

ver15 dit **`LOGIA`** — vérifié plutôt que recopié : tag posé et lu, intangibilité physique appliquée,
eau scellant le pouvoir. Testé par `/dftest haki` (CORE-029).

Vérifié : `python3 tools/fruit_audit.py --fruit SUNA_SUNA` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Desert Spada** | projectile | conservée | 12 | — | 50 | 100 |
| **Barjan** | projectile | **tempête de sable** | 12 | — | 50 | 120 |
| **Sables** | projectile | conservée | 50 | — | 70 | 160 |
| **Desert La Spada** | projectile | conservée | 15 | — | 50 | 120 |
| **Ground Death** | frappe | **dessiccation de zone** | 20 | 16 | 60 | 160 |

⚠️ **Barjan** était déclaré **projectile** dans ver15 : il tirait un projectile là où la technique est
une **tempête**. Même correction de cible que pour Ice Spikes (Hie Hie) — le nom de la technique était
le seul indice disponible, et il ne correspondait pas au comportement.

## 4. Spécialisations

### Tempête à volume visible *(FRUIT-SUNA_SUNA-03)*

Le plan : « créer une **tempête** avec **volume visible**, durée et effets cohérents », critère
« **seules les cibles dans le volume autorisé** subissent les effets ».

**Ce que le critère interdit vraiment** : il ne dit pas « la tempête inflige des dégâts » — il dit que
*seules* les cibles du volume autorisé sont touchées. Cela impose deux choses :

1. **Une seule géométrie** — le rayon et la hauteur passent par la **même méthode** (`SandStorm.volume`)
   pour dessiner *et* pour frapper. Deux constantes séparées dériveraient à la première retouche, et la
   tempête finirait par frapper hors de ce qu'elle montre. Ce n'est pas une intention, c'est une
   signature : les deux appellent la même fonction.
2. **Aucun effet rétroactif** — une cible qui entre **après** l'expiration n'est pas touchée. Le volume
   *est* la zone d'effet, à l'instant présent.

**La tempête ne modifie aucun bloc** : elle n'a donc rien à restaurer, et **aucune zone protégée ne
peut être atteinte** par elle. C'est une garantie structurelle, pas une vérification à refaire.

**Preuves** :
- `N4` : cible **dans** le volume → 20,0 → **17,0 PV** et desséchée ; cible **hors** volume (11,18 blocs
  du porteur) → 20,0 → **20,0 PV, aucun effet** ;
- `N5` : tempête vivante = 1, après échéance = 0, et une cible entrée après reste **intacte**.

### Dessiccation : accumulation, plafond, disparition propre *(FRUIT-SUNA_SUNA-04)*

Le plan : « ajouter un **état de dessiccation** avec **accumulation et plafond** », critère « l'état a
une **durée**, des **résistances** et une **disparition propre** ». Les trois exigences, une par une :

| Exigence | Comment elle est tenue |
|---|---|
| **Durée** | un palier court sur 80 ticks et **se termine seul** — aucun état n'attend un nouvel événement |
| **Résistances** | une cible **humide** résiste, et se **libère** si elle était déjà desséchée |
| **Disparition propre** | à l'échéance, l'état **et** le ralentissement posé par le pouvoir sont retirés — pas de ralentissement orphelin |

**Pourquoi le plafond n'est pas décoratif** : au palier maximal la cible subit en plus des dégâts de
déshydratation (2,0 / s). Sans plafond, un martèlement continu deviendrait une exécution.
`N1` : trois applications → paliers **1 → 2 → 3**, puis **huit de plus** → palier **3** (plafond 3).

### Interactions avec l'eau *(FRUIT-SUNA_SUNA-05)*

Le plan : « définir et appliquer les interactions avec eau et cibles humides », critère « les
interactions correspondent au contrat **sans désactiver involontairement les règles communes** ».

**La règle choisie** : du sable ne dessèche pas ce qui est **déjà mouillé**. Une cible humide résiste,
et une cible desséchée qui entre dans l'eau **se libère** — l'eau est la contre-mesure naturelle de ce
fruit, et elle se comporte comme telle. `N3` le prouve des deux côtés.

**Et surtout — ce que le critère protège** : la règle d'eau du sable s'**ajoute** aux règles communes,
elle ne les **remplace** pas. Une implémentation paresseuse aurait pu « gérer l'eau » en désactivant au
passage l'intangibilité Logia ou la suppression par la mer. `N6` vérifie que le pouvoir reste **scellé**
par l'immersion, exactement comme avant ce contrat.

⚠️ **Détail d'implémentation qui compte** : la détection d'humidité est **double** — le drapeau de
fluide du jeu **et** les blocs occupés. Le premier n'est mis à jour qu'au tick *suivant* l'apparition
d'une entité : une créature surgissant dans l'eau serait sinon crue au sec, et se dessécherait **dans
la mer**. C'est le défaut trouvé sur Mera Mera, ici évité dès la conception.

### Exceptions Logia *(FRUIT-SUNA_SUNA-06 — partiel, assumé)*

L'intangibilité et ses percements (Haki, eau, Yami) sont couverts par CORE-029 et vérifiés par
`/dftest haki`.

⚠️ **Ce qui n'est pas fait** : les exceptions **propres au sable** — un porteur Suna Suna devrait perdre
sa dispersion au contact de l'eau au-delà de la simple faiblesse, et ne pas être blessé par les projectiles
de sable. Aucun test ne le couvre. Manque identifié, pas dissimulé.

## 5. Acquisition, rareté, progression *(FRUIT-SUNA_SUNA-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_suna_suna` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-SUNA_SUNA-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **tempête** : le volume de particules doit correspondre **visiblement** à la zone qui blesse —
      une cible juste au bord est le meilleur test (« je la vois dans la tempête, je prends ») ;
- [ ] dessiccation : les trois paliers doivent être **lisibles** (le ralentissement porte le palier,
      les particules CRIT le ponctuent) ;
- [ ] une cible **dans l'eau** doit visiblement ne pas se dessécher ;
- [ ] intangibilité Logia à deux clients.

## 7. Tests *(FRUIT-SUNA_SUNA-09)*

`/dftest suna` — **7/7 PASS** :

| id | Résultat |
|---|---|
| `N0-decor-pret` | 3 paliers × 80 ticks · tempête 4,0 × 3,0 |
| `N1-dessiccation-accumulee-et-plafonnee` | 1 → 2 → 3, puis plafond |
| `N2-disparition-propre` | état **et** ralentissement retirés |
| `N3-cible-humide-resiste-et-se-libere` | palier 0 dans l'eau, libération |
| `N4-tempete-seules-les-cibles-du-volume` | 17,0 PV dedans, **20,0 intact dehors** |
| `N5-tempete-expiree-sans-effet` | 1 → 0, cible tardive intacte |
| `N6-regles-communes-intactes` | pouvoir **toujours scellé** par la mer |

Reste manuel : tout le §6.
