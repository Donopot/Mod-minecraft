# CORE-025 — budgets d'équilibrage et contres

**Constat de départ** (analyse du JAR ver15) : le catalogue déclarait des dégâts hors de toute échelle
jouable — `kamiezanshin` à **999 999** (une exécution garantie), `raigo` à **800**, et quatre techniques
à **200** — pour une médiane réelle de **16**. Un joueur a 20 PV : au-delà de ~60, une technique n'est
plus contrable, ni en PvE ni en PvP.

Objectif : définir des budgets, corriger **uniquement les écarts**, et prouver qu'aucun fruit ne domine.

---

## 1. Budgets retenus

Les dégâts sont exprimés en **points de vie** (joueur de base = 20 PV, soit 10 cœurs).

| Palier | Critère (coût en Curse Power) | Budget de dégâts | Justification |
|---|---|---|---|
| **Base** | `< 60` | **25** | 1,25× les PV : pression sans exécution |
| **Signature** | `60 – 109` | **40** | 2× les PV : punit une erreur, survit avec armure |
| **Ultime** | `≥ 110` | **60** | 3× les PV : plafond dur, recharge 300–400 ticks |
| **Plafond dur** | toutes | **60** | au-delà, ni esquive ni armure ne suffisent |

Le palier est déduit du **coût énergétique**, pas d'une étiquette arbitraire : une technique chère a
mécaniquement un budget plus large (elle ne peut être enchaînée). Cela garde le système cohérent avec
l'économie d'énergie existante (énergie max 200 → 1 à 3 tirs avant recharge).

## 2. Corrections réellement appliquées

**6 techniques** (sur 182 offensives, soit **3,3 %**) — plafond dur, valeurs ver15 conservées partout ailleurs :

| Technique | Fruit | Avant | Après | Curse Power | Recharge |
|---|---|---|---|---|---|
| `kamiezanshin` | Batto Batto (Chauve-souris) | 999 999 | **60** | 100 | 300 t |
| `raigo` | Goro Goro (Foudre) | 800 | **60** | 150 | 400 t |
| `darkmatter` | Yami Yami (Ténèbres) | 200 | **60** | 150 | 400 t |
| `daienkai` | Mera Mera (Flamme) | 200 | **60** | 100 | 300 t |
| `stonethrowinglarge` | Kira Kira (Étincelle) | 200 | **60** | 70 | 180 t |
| `stonethrowinglarge` | Riki Riki (Force) | 200 | **60** | 70 | 180 t |

> `stonethrowinglarge` est déclarée par **deux fruits** (ambiguïté connue, cf CORE-017) : les deux
> occurrences sont corrigées, et le suivi les compte séparément.

**Aucune autre valeur n'est touchée.** Coûts, recharges, portées, effets de statut et vitesses restent
exactement ceux de ver15 — c'est vérifiable dans le journal : l'équilibrage annonce chaque correction.

## 3. Effet sur la distribution

| | Avant | Après |
|---|---|---|
| Dégâts max | 999 999 | **60** |
| Dégâts médians | 16 | 16 |
| Ratio dégâts/Curse Power (min → max) | 0,21 → **14 285** | **0,21 → 0,52** |
| Fruits partageant les dégâts max | 1 (Batto Batto, seul) | **7** (Batto, Goro, Hie, Kira, Mera, Riki, Yami) |

L'étalement d'efficacité passe d'un facteur ~68 000 à un facteur **2,5** — c'est la mesure du
« pas de domination systématique » : le meilleur fruit est 2,5× plus efficace à l'énergie, pas 14 000×.

### Analyse PvE

| Cible | PV | Tirs nécessaires (max 60) |
|---|---|---|
| Zombie / squelette | 20 | 1 |
| Golem de fer | 100 | 2 |
| Dragon de l'End | 200 | 4 |
| Wither | 300 | 5 |

Avec des recharges de 180–400 ticks sur les grosses techniques, un combat de boss s'étale sur plusieurs
dizaines de secondes : la ressource et la recharge, pas les dégâts, bornent le rythme. Avant correctif,
`kamiezanshin` tuait le Wither en un coup.

### Analyse PvP

- 60 dégâts = exécution d'un joueur **sans armure**, mais ~12 dégâts à travers une armure netherite
  Protection IV → survit à toutes les techniques, y compris ultimes.
- Les projectiles rapides vont de 1,5 à 4,0 blocs/tick : esquivables à couvert.
- 6 techniques à 60 dégâts réparties sur **7 fruits** : aucun fruit ne détient l'exécution.

## 4. Matrice des contres

Contres **déjà implémentés et consultés par le calcul de dégâts** (`DevilFruitPowerLogic.computeDamageMagnification`) :

| Contre | Effet | Portée |
|---|---|---|
| **Haki (Armement / Conquérant)** | perce l'intangibilité Logia et l'immunité Gomu | tout utilisateur ayant l'effet |
| **Yami Yami (Ténèbres)** | annule la réduction de dégâts adverse (multiplicateur 1,0) | attaquant Yami Yami |
| **Intangibilité Logia** | attaque physique sans Haki → 0 dégât | Logia uniquement |
| **Immunité Gomu** | projectiles → 0, contondant ≈ 0 (sans Haki) | Gomu Gomu |
| **Eau de mer** (CORE-007) | suppression : aucune technique lançable | tous, en mer |
| **Curse Power** | 200 max, coût 40–150 → 1 à 3 tirs avant recharge | tous |
| **Recharge** | 80–400 ticks sur la technique utilisée | tous |

Aucun fruit ne dispose à la fois d'une exécution rapide, d'une immunité et d'une mobilité : les Zoan
ont la mobilité et l'immunité partielle, les Logia l'intangibilité mais dépendent de l'énergie, les
Paramecia le contrôle. Chaque archétype a au moins une contre-mesure listée ci-dessus.

*Limite honnête* : **CORE-029** (portée du Haki / kairoseki) est encore au stade P2. Le Haki existe comme
effet et est bien lu par le calcul de dégâts, mais son obtention et son coût ne sont pas encore définis —
tant que ce n'est pas fait, le contre « Haki » n'est pas accessible en survie normale. C'est le prochain
chantier d'équilibrage.

## 5. Réglage et restauration

Fichier : `config/opworldlike/devilfruit-balance.toml`

```toml
enabled = true                 # false => valeurs ver15 brutes, intactes
absolute-max-damage = 60.0     # plafond dur
enforce-tier-budgets = false   # true => applique aussi les budgets par palier (25/40/60)
tier-basic-max = 25.0
tier-signature-max = 40.0
tier-ultimate-max = 60.0
signature-energy-threshold = 60.0
ultimate-energy-threshold = 110.0
override-kamiezanshin = 45.0   # forcer une technique précise (priorité maximale)
```

- **Restauration ver15 exacte** : `enabled = false`.
- **Normalisation complète** : `enforce-tier-budgets = true` (touche alors ~40 techniques au-dessus de
  leur palier — c'est un choix de conception, pas une correction d'aberration).
- Le recalcul est **idempotent** : il repart toujours des valeurs ver15, rien ne s'accumule.

## 6. Vérification automatisée

```bash
python3 tools/balance_audit.py              # audit statique : distribution + aberrations
python3 tools/run_tests.py                  # dont le scope /dftest balance
```

Contrôles `[dftest]` :

| id | Contrôle |
|---|---|
| `K1-plafond-respecte` | aucune technique au-dessus du plafond dur |
| `K2-corrections-tracees` | chaque correction est journalisée (avant → après + motif) |
| `K3-ecarts-limites` | ≤ 10 techniques corrigées → le catalogue n'est pas réécrit |
| `K4-distribution` | min / médiane / max cohérents après équilibrage |
