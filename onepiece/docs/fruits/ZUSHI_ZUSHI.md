**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Zushi Zushi (Press-Press Fruit)

Douzième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le fruit de la **gravité**
(Fujitora). Le plan y refuse une facilité : « ajouter le tag correct **sans supposer que NON_CLASSE
signifie Paramecia** ».

- **Item** : `minepiece:item_devil_fruit_zushi_zushi`
- **Techniques** : 6 (ver15) — Attack Speed, Gravito, Meteor (small), Moko, Rubble, Meteor
- **Famille** : **paramecia** — décision argumentée, et **vérifiée sur le tag lu** (`P0`)

---

## 1. Identité, limites et contre-mesures *(FRUIT-ZUSHI_ZUSHI-01)*

**Identité** — Le porteur **ajoute** de la gravité au monde : il alourdit, il soulève, et il fait
**tomber** des météores. Il ne devient pas la gravité — il la commande.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 70 | plan ver15 |
| Recharges | 80 à 180 ticks | plan ver15 |
| **Champ : intensité plafonnée** | **1,5 bloc / tick** | config |
| Champ : rayon | 6,0 blocs | config |
| Champ : durée | 100 ticks | config |
| Lévitation : durée | 80 ticks | config |
| Lévitation : cumul max | ampl. 2 | config |
| Météore : hauteur | +28,0 blocs | config |
| Météore : vitesse de chute | 2,5 | config |
| Météore : rayon d'impact | 4,0 blocs | config |
| **Météore : avertissement** | **40 ticks** | config |
| Météore : destruction du terrain | **désactivée** par défaut | config |

**Contre-mesures** : eau de mer (SEA_POWER → pouvoir scellé, **lévitations relâchées**), Haki de
l'Armement, énergie insuffisante, retrait du Fruit (reset + `GravityField.clearAll()` +
`Levitation.releaseAll()` + `MeteorFall.clearAll()`).

## 2. Classification *(FRUIT-ZUSHI_ZUSHI-02)*

ver15 dit **`NON_CLASSE`**, et le plan **interdit** d'en déduire Paramecia. Le raisonnement est donc
mené à partir de ce que la classe **détermine réellement** :

| Famille | Ce qu'elle implique | Ici |
|---|---|---|
| **Logia** | le corps devient l'élément → intangibilité physique | **non** : le porteur ne devient pas de la gravité, il n'obtient aucune intangibilité |
| **Zoan** | transformation en créature ou forme hybride | **non** : aucune forme |
| **Paramecia** | pouvoir « surhumain » agissant sur le monde, sans intangibilité ni transformation | **oui** : c'est exactement ce que fait ce fruit |

Le rattachement est **vérifié sur le tag lu**, jamais supposé :
`P0` lit `paramecia=true`, `logia=false` — et non « le TOML dit NON_CLASSE, donc probablement
Paramecia ». Une preuve qui testerait une supposition ne prouverait rien.

Vérifié : `python3 tools/fruit_audit.py --fruit ZUSHI_ZUSHI` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Attack Speed** | buff | conservée (SPEED 60 ticks) | 0 | — | 40 | 80 |
| **Gravito** | frappe | **champ de gravité** | 11 | 6 | 50 | 120 |
| **Meteor (small)** | projectile | **météore descendant** | 20 | — | 50 | 120 |
| **Moko** | frappe | **lévitation** | 26 | 16 | 70 | 180 |
| **Rubble** | projectile | conservée | 15 | — | 50 | 100 |
| **Meteor** | projectile | **météore descendant** | 40 | — | 70 | 180 |

⚠️ **Meteor et Meteor (small)** étaient des **projectiles horizontaux** dans ver15 : ils partaient du
regard du porteur, comme une flèche. Un météore ne fait pas ça — il **tombe**. C'est la troisième
correction de type « le nom dit ce que la technique n'est pas » après Ice Spikes (Hie Hie) et Barjan
(Suna Suna).

## 4. Spécialisations

### Champ de gravité : deux interdictions, deux garde-fous *(FRUIT-ZUSHI_ZUSHI-03)*

Le plan : « ajouter des **champs de gravité** avec **intensité, rayon et durée bornés** », critère « les
cibles ne reçoivent **pas des vitesses infinies** ni de **déplacement dans des blocs** ».

Ces deux interdictions sont **deux fonctions distinctes et testables séparément** — c'est ce qui permet
de prouver chacune sans dépendre de l'autre :

| Interdiction | Garde-fou |
|---|---|
| Pas de vitesse infinie | `cappedIntensity` plafonne **après** calcul, à `max_velocity` |
| Pas de déplacement dans un bloc | `validatedRise` teste la case de destination et **annule** la poussée |

**Le point de conception** : la borne est dans le **code**, pas dans le TOML. Une intensité de 50 écrite
par erreur dans le fichier de configuration ne peut pas transformer un joueur en projectile — elle est
ramenée à 1,5. `P1` le prouve : intensité demandée **50,0** → appliquée **1,5**.

**Preuve `P2`**, et c'est sa partie la plus importante — elle contient un **contrôle positif** :
poussée vers un plafond plein → **annulée** ; **la même** poussée en ciel libre → **appliquée** ;
poussée vers le sol → annulée (le sol bloque, et c'est ce qu'on veut). Sans le contrôle positif, une
fonction qui refuserait **tout** passerait le test — et le pouvoir ne ferait rien.

### Lévitation : « libère toujours » *(FRUIT-ZUSHI_ZUSHI-04)*

Le plan : « créer une **lévitation temporaire** avec **permissions de cible** », critère « fin de l'effet
**restitue la gravité** et **libère toujours la cible** ».

**« Toujours » n'est pas « en général »** : une lévitation qui ne se termine pas laisse un joueur
suspendu sans recours — le même défaut que le gel permanent du Hie Hie, traité de la même façon. Trois
chemins de sortie, tous obligatoires : l'**échéance**, la **suppression du pouvoir** (porteur scellé →
toutes ses lévitations sont relâchées), et la **cible invalide** (morte, déconnectée, changé de
dimension).

Et dans **tous** les cas, `noGravity` est explicitement remis à **faux** : c'est la restitution de la
gravité qu'exige le critère. Retirer l'effet sans rendre la gravité laisserait une cible flottante si un
autre mécanisme l'y avait mise — `P4` simule exactement ce cas et vérifie que la gravité revient.

**Permissions de cible** : le porteur **ne se lévite pas lui-même** (il domine la gravité, il ne la
subit pas), et une cible valide est **rafraîchie**, jamais cumulée au-delà du plafond d'amplificateur —
un maintien répété ne doit pas devenir un bannissement. `P3` : porteur sur lui-même → **refus** ;
cible valide → amplitude retenue.

### Météores réellement descendants *(FRUIT-ZUSHI_ZUSHI-05)*

Le plan : « créer des **météores réellement descendants** avec **avertissement préalable** », critère
« **impact unique**, **durée de vie bornée** et **terrain protégé respecté** ».

**Réellement descendant** : le projectile est créé **28 blocs au-dessus** du point visé, avec une
vitesse **verticale descendante** et une horizontale quasi nulle. `P5` lit la position et le mouvement
**réels** de l'entité : `y≈128` pour un impact à `y=100`, vitesse verticale négative. Sa trajectoire est
une **chute**, pas un tir.

**Avertissement préalable** : l'impact est **enregistré** puis annoncé 40 ticks avant — particules au
sol à l'endroit visé, colonne qui descend, son grave, message au porteur. La cible **voit venir** ce qui
lui arrive. Un météore qui frappe sans prévenir n'est pas une attaque, c'est une surprise.

**Impact unique** : `P6` compte les impacts résolus — **0** pendant tout l'avertissement, **1** à
l'échéance, **0** de plus sur 40 ticks supplémentaires. Ce n'est pas une zone qui martyrise à chaque
tick où l'on s'y tient.

**Terrain protégé respecté** : la destruction du terrain est **désactivée par défaut**, et quand elle est
activée elle passe par le moteur d'explosion qui **refuse les zones protégées**. `P7` vérifie la même
position **dans** et **hors** zone : `true` puis `false` — la preuve porte sur le câblage, pas sur
l'absence de zone.

### Passifs et contreparties *(FRUIT-ZUSHI_ZUSHI-06)*

Le plan : « implémenter uniquement les passifs et contreparties définis dans son contrat, avec durée et
nettoyage », critère « passifs appliqués au bon joueur et **retirés sous suppression ou retrait sans
toucher aux états externes** ».

Les états créés par ce fruit sont **exactement trois**, tous suivis et tous nettoyables :
`GravityField` (champs), `Levitation` (états de lévitation), `MeteorFall` (impacts en attente). La
suppression relâche les lévitations ; le retrait du Fruit appelle les trois `clearAll()`/`releaseAll()`.
Le critère « **sans toucher aux états externes** » est tenu par construction : chaque nettoyage ne
retire **que** les effets que ce pouvoir a posés (LEVITATION pour la lévitation), jamais un effet
générique d'une autre origine.

## 5. Acquisition, rareté, progression *(FRUIT-ZUSHI_ZUSHI-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_zushi_zushi` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-ZUSHI_ZUSHI-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **champ de gravité** : faut-il voir *où* il agit ? Aujourd'hui des particules CRIT en dessinent la
      couronne — à juger en jeu ;
- [ ] **lévitation** : la montée doit être lisible, et surtout la **retombée** doit être nette, jamais
      une hésitation ;
- [ ] **météore** : l'avertissement doit être **visible depuis la zone d'impact** — c'est tout l'intérêt
      du délai. Si le joueur ne le remarque pas, l'avertissement ne sert à rien ;
- [ ] `Gravito` : l'alourdissement doit se sentir (le ralentissement porte l'effet) ;
- [ ] chute du météore : lisibilité de la descente, qui doit **venir d'en haut**, pas de face.

## 7. Tests *(FRUIT-ZUSHI_ZUSHI-09)*

`/dftest zushi` — **8/8 PASS** :

| id | Résultat |
|---|---|
| `P0-decor-et-classification` | **paramecia=true, logia=false** (tag **lu**) |
| `P1-champ-borne-pas-de-vitesse-infinie` | 50,0 demandée → **1,5** appliquée |
| `P2-aucun-deplacement-dans-un-bloc` | annulée au plafond, **appliquée en ciel libre**, annulée au sol |
| `P3-levitation-permissions-de-cible` | porteur refusé, cible retenue |
| `P4-levitation-libere-toujours-la-cible` | gravité **restituée** |
| `P5-meteore-reellement-descendant` | +28,0 blocs, vitesse verticale descendante |
| `P6-avertissement-puis-impact-unique` | 0 / **1** / 0 impact |
| `P7-terrain-protege-respecte` | `true` en zone, `false` hors zone |

⚠️ **Piège de test rencontré** : ma première version de `P2` demandait que la poussée **vers le bas**
soit appliquée — elle ne l'était pas, et à **juste titre** : le sol la bloque. Le test condamnait un
comportement correct. La version retenue sépare les trois cas et exige un **contrôle positif**, sans
lequel un refus systématique passerait pour une validation.

Reste manuel : tout le §6.
