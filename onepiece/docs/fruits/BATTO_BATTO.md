# Lot06 : implémenté, testé serveur et vu en laboratoire

Voir [bilan](../B06-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-06.md). Le cycle comporte maintenant une forme animale complète. Les affirmations historiques ci-dessous doivent être lues avec les corrections de source ; aucune acceptation artistique humaine acquise.

---

# Contrat — Batto Batto (Bat-Bat Fruit)

Deuxième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. C'est le premier Fruit dont
une **mécanique propre complexe** est implémentée (le vol) et le premier qui reçoit une configuration
dédiée.

- **Item** : `minepiece:item_devil_fruit_batto_batto`
- **Techniques** : 6 (ver15) — Shigan, Soru, Rankyaku, Tobu Shigan, Kamie Zanshin, Yurushitene
- **Famille** : `zoan` · **Transformation** : **oui**

---

## 1. Identité, limites et contre-mesures *(FRUIT-BATTO_BATTO-01)*

**Identité** — Zoan de la Chauve-souris. ver15 lui attache les **techniques du Rokushiki** (Shigan,
Soru, Rankyaku, Kamie Zanshin) plutôt qu'un bestiaire de chauve-souris : le contrat suit ver15 et
assume ce choix. La signature propre du Fruit est le **vol** et, à présent, l'**écholocation**.

**Limites** opposables à un joueur :

| Limite | Valeur | Source |
|---|---|---|
| Énergie disponible | 200 | CORE-005 |
| Coûts des techniques | 40 à 100 | plan ver15 |
| Recharges | 80 à 300 ticks | plan ver15 |
| Portée maximale des techniques | 5 blocs | plan ver15 |
| **Vol** | 4 Énergie/s → **50 s** sur réserve pleine | `devilfruit-flight.toml` |
| Écholocation | rayon **12 blocs**, toutes les 40 ticks, **aucun dégât** | `devilfruit-flight.toml` |

**Contre-mesures** :

- **Eau de mer** → pouvoirs scellés **et vol coupé immédiatement** (atterrissage, B3) ;
- **Épuisement d'Énergie** → le vol s'arrête et le joueur retombe (B2) ;
- **Perte de la forme hybride** → le vol n'est plus légitime (B4) ;
- **Haki de l'Armement** → sans objet : le Fruit n'offre aucune résistance à percer ;
- **Retrait du Fruit** → reset complet (FRUIT-RESET-01), vol révoqué par la même voie.

## 2. Classification *(FRUIT-BATTO_BATTO-02)*

ver15 déclare **`ZOAN`** ; le port porte le tag `zoan` — **conforme, aucune divergence**
(`python3 tools/fruit_audit.py --fruit BATTO_BATTO`).

Comme pour tous les Zoans, la **capacité de transformation** est portée séparément par
`minepiece:transformable` (présente). Distinguer famille et capacité est ce qui permet à Gomu Gomu
d'être Paramecia **et** transformable ; ici les deux coïncident, ce qui rend le cas simple.

Aucun passif n'est induit par la classification : un Zoan n'a ni intangibilité (pas `logia`) ni
mécanique propre.

## 3. Catalogue final *(FRUIT-BATTO_BATTO-01)*

| Technique | Type | Dégâts | Portée / durée | Recul | Énergie | Recharge | Effet |
|---|---|---|---|---|---|---|---|
| **Shigan** | frappe | 10 | 2 blocs | 0,25 | 40 | 80 | — |
| **Soru** | buff | — | 40 ticks | — | 40 | 80 | Vitesse III |
| **Rankyaku** | projectile | 30 | 80 ticks | 0,5 | 50 | 100 | — |
| **Tobu Shigan** | frappe | 2,2 | 4 blocs | 0,5 | 40 | 80 | — |
| **Kamie Zanshin** | frappe | *plafonnée* | 4 blocs | 1,0 | 100 | 300 | Lenteur II |
| **Yurushitene** | frappe | 18 | 5 blocs | 0,1 | 50 | 120 | — |

**Toutes ces valeurs sont conformes à ver15** — l'audit ne relève **aucun écart** pour ce Fruit.

⚠️ **Kamie Zanshin** : ver15 déclare `degats=999999`. C'est **volontairement conservé dans le
registre** (fidélité à la source), et c'est la **couche d'équilibrage** CORE-025
(`config/opworldlike/devilfruit-balance.toml`) qui plafonne la valeur réellement appliquée à
**3× les PV d'un joueur**. Écart de puissance déjà acté et documenté dans `CORE-025-budgets-et-contres.md` ;
il n'est pas reproposé ici.

## 4. Spécialisations

### Vol consommant l'Énergie, avec atterrissage garanti — **implémenté** *(FRUIT-BATTO_BATTO-04)*

C'est l'item le plus substantiel du contrat, et le seul comportement de vol de tout le port.

**Construire** — `flight/DevilFruitFlight.java` + `flight/DevilFruitFlightConfig.java`
(`config/opworldlike/devilfruit-flight.toml`). Couvre les trois Fruits ailés : Batto Batto, Tori Tori
Phénix, Ryu Ryu Ptéranodon.

**Règle** — en vol, l'Énergie est consommée à **4 par seconde** ; le vol cesse et le joueur atterrit
si :

1. l'Énergie passe sous le seuil (`minimum-energy`, 1,0) ;
2. le porteur est **scellé par la mer** ;
3. la **forme hybride** est perdue (le vol n'est légitime qu'en forme) ;
4. le Fruit est retiré (reset).

**Atterrissage garanti — le point technique clé.** La révocation met **toujours** `mayfly` *et*
`flying` à faux, jamais l'un sans l'autre : c'est précisément l'état **dissocié** (`flying = true`
alors que `mayfly = false`) qui bloquerait un joueur en l'air. La gravité vanilla le ramène ensuite au
sol — **aucune téléportation**, donc aucun risque de le poser dans un bloc.

**Preuves** (`/dftest batto`) :

| id | Résultat |
|---|---|
| `B1-vol-consomme-energie` | Énergie **100,0 → 99,8** en un tick (4,0/s), vol toujours actif |
| `B2-atterrissage-sans-energie` | Énergie 0,2 < seuil 1,0 → `mayfly=false` **ET** `flying=false` |
| `B3-atterrissage-sous-suppression` | sous `SEA_POWER` → vol révoqué |
| `B4-atterrissage-hors-forme` | forme revenue à 0 → vol révoqué |

### Écholocation — impulsion sensorielle bornée — **implémenté** *(FRUIT-BATTO_BATTO-05)*

Le plan demande des « attaques sonores ou sensorielles », **bornées** et « distinctes d'une simple
frappe générique ».

**Décision** : implémenter une attaque **sensorielle** — une impulsion d'écholocation qui **marque**
(`Glowing`) les créatures dans un rayon fixe de **12 blocs**, toutes les 40 ticks, accompagnée d'un
anneau sonore visible. Elle est :

- **bornée** : rayon fixe, vérifié indépendamment de la boîte englobante (distance réelle) ;
- **non générique** : aucun dégât, aucun impact, aucun recul — ce n'est pas une frappe ;
- **passive** : sans coût d'Énergie (c'est un sens, pas une technique lancée).

**Ce qui n'a PAS été ajouté** : aucune technique de **dégâts sonores** n'existe dans ver15 (les
6 techniques sont du Rokushiki), et en inventer une ferait sortir le port du périmètre « fidèle à
ver15 ». Ajouter une 7ᵉ technique sonore est donc une **décision qui te revient** — je ne l'ai pas
prise seul.

**Preuve** (`B5-echolocation-bornee`) : créature à **6 blocs marquée = vrai**, créature à **28 blocs
marquée = faux**, 1 seule marquée au total, aucun dégât.

### Shigan, Tobu Shigan, Kamie Zanshin, Yurushitene — *frappes conservées*

ver15 les déclare toutes `MELEE` avec des paramètres distincts (portée 2 / 4 / 4 / 5, dégâts
10 / 2,2 / *plafonné* / 18, recul et recharges différents), et le plan demande « portée et animation
cohérentes ; obstacle et équipe protégés ; nombre d'impacts et type de dégâts exacts ». Le moteur de
frappe directionnelle (`CombatSkillLogic.meleeStrike`) fournit exactement cela : cône borné, alliés
protégés, **un seul impact** par activation, type de dégâts élémentaire du Fruit. La différenciation
vient des paramètres, conformes — pas d'un habillage cosmétique.

### Soru — *buff conservé, non cumulable* *(FRUIT-BATTO_BATTO-TECH-02-DEV)*

ver15 applique `MobEffects.SPEED` 40 ticks amplificateur **2** (Vitesse III). Ce **n'est pas** un
marqueur : l'effet est vanilla, réel et observable. Pas de cumul abusif : réappliquer **remplace** la
durée (comportement vanilla), et l'effet **expire seul** — il ne survit pas au retrait du Fruit, donc
aucune contrepartie à nettoyer.

### Rankyaku — *projectile générique conservé* *(FRUIT-BATTO_BATTO-TECH-03-DEV)*

ver15 déclare `PROJECTILE` (30 dégâts, 80 ticks de durée de vie, 0,5 de recul) **sans paramètre de
collision propre**. Le visuel dédié (`projectile_slash`) est déclaré (CORE-024). Même décision
explicite que pour Aro Arrow : rien à spécialiser qui ne soit déjà couvert, et en inventer un
s'écarterait de ver15 sans base dans le plan.

### Formes chauve-souris et hybride visibles *(FRUIT-BATTO_BATTO-03, -06)*

Le **comportement** est en place : forme hybride (bonus Force/Résistance/Vitesse réversibles),
échelle ×1,5 (table SIZE de ver15), vol, retour à la base propre, attributs révoqués au retrait.

Le **modèle joueur** (maillage chauve-souris en première personne, animations d'ailes) **reste à
faire** : ver15 n'en fournit pas d'exploitable (le constat du plan le dit — « aucun modèle joueur »).
Créer un modèle et des animations de joueur est un travail de modélisation à part entière, pas une
ligne de code à porter.

## 5. Acquisition, rareté, progression *(FRUIT-BATTO_BATTO-08)*

| Aspect | État |
|---|---|
| Rareté | `zoan` → palier **commun** (poids 60) dans le loot des coffres (CORE-023) |
| Acquisition | coffre de structure (2 %), ou `/devilfruit give item_devil_fruit_batto_batto` |
| Techniques verrouillées | **aucune** : les 6 techniques sont disponibles dès l'ingestion |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

Écart **assumé et déclaré** : le critère « déblocages enregistrés et impossibles à contourner par
requête réseau » n'a pas d'objet tant qu'il n'y a aucun déblocage à protéger.

## 6. Validation visuelle *(FRUIT-BATTO_BATTO-07 — P2, exige un client)*

- [ ] icône du Fruit, inventaire et au sol, non remplacée ;
- [ ] Fruit tenu première **et** troisième personne, deux mains ;
- [ ] infobulle (nom FR « Fruit Chauve-souris-Chauve-souris ») ;
- [ ] **vol** : le joueur vole visiblement, la consommation d'Énergie se voit dans le HUD (CORE-028) ;
- [ ] **atterrissage** : à l'épuisement, le joueur retombe visiblement (pas de flottement) ;
- [ ] **écholocation** : anneau sonore visible + créatures marquées par l'effet Glowing ;
- [ ] formes hybride/base : particules de transformation, retour propre ;
- [ ] les 6 techniques : visuel de frappe/projectile, animation.

## 7. Tests *(FRUIT-BATTO_BATTO-09)*

Automatisé — `python3 tools/run_tests.py` (`/dftest all`) :

- `batto` : vol, épuisement, suppression, hors-forme, écholocation bornée ;
- `catalogue` : les 6 techniques existent et s'exécutent sans erreur ;
- `player` / `persist` / `multi` / `death` / `reset` : ingestion, persistance, retrait ;
- audit statique : `python3 tools/fruit_audit.py --fruit BATTO_BATTO` → **0 écart**.

Reste manuel : rendu, animations, modèle joueur, deux clients (§6).
