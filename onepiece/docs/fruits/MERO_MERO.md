# Contrat actuel — lot04 alpha.13 en validation

Pistol Kiss est un cœur lancé de2,2HP. Roundhouse est un coup frontal de15HP ; Rotate est un balayage circulaire de18HP (budget total du catalogue, adapté en un contact borné). Perfume Femur utilise une ruée de4blocs et un contact frontal de6HP, conformément à son appel source AttackSpeed, au lieu de l'ancienne onde radiale. Pistol Kiss et Perfume pétrifient seulement après un dégât accepté : état80ticks, immunité des morts-vivants conservée comme adaptation du port. Un marqueur dédié synchronise la texture pierre ; les attributs de vitesse sont possédés, l'IA est suspendue par hook pendant l'état, sans modifier son réglage NoAi. Les attaques attribuées à une statue sont refusées. Expiration/retrait du porteur libèrent l'état et conservent les potions tierces. La pétrification des entités reste temporaire ; elle n'est pas une transformation de blocs.

La référence est le JAR privé MinePiece ver15 ; les anciens contrats ci-dessous contiennent des interprétations remplacées. Les paramètres annoncés ici sont des budgets du port et des adaptations explicitement nommées, pas une reproduction intégrale de la source. Toute modification de blocs reste définitive. La validation native finale et la session graphique groupée restent à terminer : voir [BATCH-04](../BATCH-04.md). Aucune acceptation artistique humaine revendiquée.

---

## Archive du contrat précédent

# Contrat — Mero Mero (Love-Love Fruit)

Treizième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le fruit du **charme** (Boa
Hancock). Le plan y pose la question la plus difficile de tous les contrats : pétrifier, oui — mais
**selon quelle condition, et comment le joueur le sait-il ?**

- **Item** : `minepiece:item_devil_fruit_mero_mero`
- **Techniques** : 4 (ver15) — Pistol Kiss, Brick Roundhouse Kick, Rotate Kick, Perfume Femur Magna
- **Famille** : **paramecia** — décision argumentée, **vérifiée sur le tag lu** (`Q0`)
- **Élément** : **`CHARM`** (nouveau) — c'est lui qui porte la règle de pétrification

---

## 1. Identité, limites et contre-mesures *(FRUIT-MERO_MERO-01)*

**Identité** — Le porteur **charne** et **fige** : il ne transforme pas son corps (pas Logia) et ne
devient pas une créature (pas Zoan). Sa puissance est un effet **sur les autres**, ce qui la rend
entièrement dépendante d'une **condition**.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 (les quatre techniques) | plan ver15 |
| Recharges | 100 à 120 ticks | plan ver15 |
| **Pétrification : durée** | **120 ticks** | config |
| Pétrification : résistance | ampl. 2 (une statue est solide) | config |
| **Condition : morts-vivants insensibles** | **actif** | config |
| Onde de zone : rayon | 4,0 blocs | config |

**Contre-mesures** : **les morts-vivants** (la condition n'est pas remplie — contre-mesure propre à ce
fruit), eau de mer (SEA_POWER → pouvoir scellé, pétrifications **relâchées**), Haki de l'Armement, énergie
insuffisante, retrait du Fruit (reset + `Petrification.releaseAll()`).

## 2. Classification *(FRUIT-MERO_MERO-02)*

ver15 dit **`NON_CLASSE`** et le plan interdit d'en déduire Paramecia. Le raisonnement part de ce que la
classe **détermine** :

| Famille | Ce qu'elle implique | Ici |
|---|---|---|
| **Logia** | le corps devient l'élément → intangibilité physique | **non** : le corps du porteur ne change pas |
| **Zoan** | transformation en créature / forme hybride | **non** |
| **Paramecia** | pouvoir « surhumain » agissant sur les autres, sans intangibilité ni transformation | **oui** |

`Q0` **lit** le tag : `paramecia=true`, `logia=false`. Vérifié également par
`python3 tools/fruit_audit.py --fruit MERO_MERO` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Pistol Kiss** | projectile | **mode 1 — projectile** (élément CHARM à l'impact) | 2,2 | — | 50 | 120 |
| **Brick Roundhouse Kick** | frappe | **mode 2 — coup physique** (cône) | 15 | 6 | 50 | 100 |
| **Rotate Kick** | frappe | **mode 2 — coup physique** (cône) | 18 | 6 | 50 | 120 |
| **Perfume Femur Magna** | frappe | **mode 3 — attaque de ZONE** (onde r=4) | 6 | 4 | 50 | 100 |

## 4. Spécialisations

### La condition de pétrification, **affichée** *(FRUIT-MERO_MERO-03)*

Le plan : « définir des **conditions de pétrification** compréhensibles et **testables** », critère
« la condition est **affichée** et les **cibles non admissibles restent libres** ».

**La condition, en une phrase** :

> Une cible est admissible si elle est **vivante**, si elle est une **cible légitime**, et si elle
> **ressent quelque chose** — les morts-vivants ne ressentent rien.

Trois conséquences, toutes testées :

| Aspect | Traitement | Preuve |
|---|---|---|
| La condition est **comprise** | elle est écrite dans le contrat, pas devinée à l'usage | §4 |
| La condition est **affichée** | le porteur **lit** le verdict à chaque tentative (`conditionLabel`) | `Q1`, `Q2` |
| Les non-admissibles **restent libres** | aucun état posé, aucune immobilisation | `Q2` |

`Q1` : cible sensible → le porteur lit « *Cow : condition remplie → pétrification* ».
`Q2` : mort-vivant → le porteur lit « *Zombie y est insensible — les morts-vivants ne ressentent rien
(condition non remplie)* », **0 tick** de pétrification, **aucune** immobilisation.

**Pourquoi l'affichage est une vraie exigence, et pas du confort** : un pouvoir qui pétrifie « parfois »
sans que personne ne sache pourquoi est injouable — le joueur ne peut ni apprendre la règle, ni s'y
adapter. Afficher la condition, c'est transformer une bizarrerie en **règle**.

### État temporaire : rendu, durée, résistances *(FRUIT-MERO_MERO-04)*

| Exigence | Traitement |
|---|---|
| **Rendu** | effet `petrification` (couleur pierre `0x8C8C8C`), poussière CRIT, immobilisation visible |
| **Durée** | 120 ticks, elle **se termine seule** |
| **Résistances** | les morts-vivants y résistent **par la condition** ; une statue est **solide** (Résistance ampl. 2) |

**« Libérée à expiration ou suppression sans corruption » — le mot le plus important du contrat.**

Libérer ne suffit pas : il faut rendre la cible **exactement** dans l'état où elle était. Une
pétrification qui laisserait l'IA coupée, un effet résiduel ou une vitesse parasite *corromprait* la
créature — elle serait libre, mais cassée. Donc :

1. l'**état d'origine** est mémorisé à l'application (`Mob` → IA active ou non, gravité) ;
2. à la libération il est **restitué**, jamais approximé ;
3. la **vitesse résiduelle est annulée** ;
4. trois chemins de sortie, tous obligatoires : **échéance**, **suppression du pouvoir**, **cible
   disparue**.

`Q4` simule le cas vicieux : une créature dont **un tiers** avait déjà coupé l'IA. Après pétrification
puis échéance, son IA est **toujours coupée** — le pouvoir n'a pas « rendu » une IA qu'elle n'avait pas.
*Aucun* effet résiduel, vitesse **0,0**. C'est ça, libérer sans corruption.

`Q5` : libération par suppression/retrait → 0 cible suivie, 0 effet laissé.

### Trois modes réellement différenciés *(FRUIT-MERO_MERO-05)*

Le plan : « **différencier projectile, coup physique et attaque de zone** », critère « les **trois
modes** utilisent leur **ciblage, coût et durée propres** ».

| Mode | Technique | Ciblage | Réglages lus dans le registre |
|---|---|---|---|
| **1 — projectile** | Pistol Kiss | **droit devant, à distance** (portée 0, durée de vie 80) | 50 É, 120 ticks |
| **2 — coup physique** | Brick Roundhouse Kick, Rotate Kick | **cône frontal** de 6 blocs | 50 É, 100 / 120 ticks |
| **3 — attaque de zone** | Perfume Femur Magna | **onde circulaire** de 4 blocs, **toutes** les cibles | 50 É, 100 ticks |

`Q6` **lit le registre** — il ne recopie pas les valeurs dans le test : type `PROJECTILE` vs `MELEE`,
portée **0** pour le projectile (aucun cône), cône de **6** blocs pour la mêlée contre zone de **4**
blocs. Les ciblages sont franchement différents, pas trois variantes du même geste.

`Q8` : l'onde de zone pétrifie tout le rayon et **épargne** l'insensible (4 pétrifiées, 1 laissé libre).

#### Une seule règle, portée par l'**élément**

Le point de conception qui rend les trois modes cohérents : la pétrification est portée par l'**élément
`CHARM`**, pas recopiée dans chaque technique. Le projectile du moteur commun, la mêlée du moteur commun
et l'onde du comportement spécifique traversent donc **le même chemin** (`applyElementalEffect`) et
appliquent exactement la même condition.

`Q7` le prouve : élément lu = `CHARM` pour ce fruit, et un impact élémentaire pétrifie la cible.

Pour cela, `applyElementalEffect` a reçu une **surcharge attribuée** (avec la source) : le charme a
besoin de savoir *qui* charme pour **afficher la condition au bon joueur**. Les autres éléments
l'ignorent. C'est un ajout général, pas un cas particulier glissé dans un coin.

### Passifs et contreparties *(FRUIT-MERO_MERO-06)*

Un seul état est créé par ce fruit (`Petrification`), donc un seul à nettoyer — et il l'est sur les trois
chemins. Le critère « **sans toucher aux états externes** » est tenu par construction : la libération ne
retire que les effets que **ce** pouvoir a posés (Ralentissement, Résistance, Pierre) et ne restitue
l'IA que si elle existait. `Q4` est précisément le test de cette frontière.

## 5. Acquisition, rareté, progression *(FRUIT-MERO_MERO-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_mero_mero` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-MERO_MERO-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **la statuification doit se voir** : aujourd'hui l'effet `petrification` teinte les particules et
      une poussière CRIT ponctue la cible. Une **vraie statue** (texture pierre posée sur le modèle)
      serait plus lisible — c'est le principal point à juger en jeu ;
- [ ] **la condition affichée doit être lisible** dans l'action bar, sans masquer le reste du HUD ;
- [ ] les trois modes doivent être **visuellement distincts** : un tir, un coup de pied, une onde ;
- [ ] l'onde de zone doit montrer **où elle s'arrête** (aujourd'hui : particules HEART).

## 7. Tests *(FRUIT-MERO_MERO-09)*

`/dftest mero` — **9/9 PASS** :

| id | Résultat |
|---|---|
| `Q0-decor-et-classification` | **paramecia=true, logia=false** (tag **lu**) |
| `Q1-condition-affichee` | « *condition remplie → pétrification* », 120 ticks |
| `Q2-cible-non-admissible-reste-libre` | mort-vivant : **0 tick**, aucune immobilisation |
| `Q3-etat-rendu-duree-resistance` | rendu + durée + résistance + immobilisation |
| `Q4-liberation-sans-corruption` | IA d'un tiers **restituée**, 0 résidu, vitesse 0,0 |
| `Q5-liberation-sous-suppression` | 0 cible suivie après retrait |
| `Q6-trois-modes-differencies` | projectile / cône 6 / zone 4, valeurs **lues** |
| `Q7-projectile-petrifie-par-l-element` | élément **CHARM** → pétrifiée |
| `Q8-zone-touche-le-rayon-et-epargne-les-insensibles` | 4 pétrifiées, **1 libre** |

⚠️ **Trois pièges d'API rencontrés** (26.3) : `EntityType#is(TagKey)` n'existe pas — passer par
`getType().builtInRegistryHolder().is(...)` ; `noAi` vit sur **`Mob`**, pas sur `LivingEntity` (donc
mémoriser aussi « était-ce un Mob ? ») ; `displayClientMessage` n'existe plus → `sendSystemMessage`.

Reste manuel : tout le §6.
