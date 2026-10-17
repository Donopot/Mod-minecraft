# Contrat — Magu Magu (Magma-Magma Fruit)

Neuvième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Logia du magma — le fruit
d'Akainu, et celui dont le plan a posé la question la plus intéressante : **qu'est-ce qui distingue
une brûlure de magma d'une brûlure de feu ?**

- **Item** : `minepiece:item_devil_fruit_magu_magu`
- **Techniques** : 5 (ver15) — Dai Funka, Inugami Guren, Kagero, Volcanic Explosion, Ryusei Kazan
- **Famille** : `logia`

---

## 1. Identité, limites et contre-mesures *(FRUIT-MAGU_MAGU-01)*

**Identité** — Logia de magma : le corps est de la roche en fusion. Attaques physiques traversantes,
feu qui ne l'atteint pas, et **le sol qui reste brûlant** après le passage.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 70 | plan ver15 |
| Recharges | 80 à 160 ticks | plan ver15 |
| **Brûlure de magma** | **160 ticks**, incendie visible 120 (feu : 60) | contrat |
| Dégâts de brûlure | 2,0 / seconde | contrat |
| **Nappes : population** | **3 par porteur** | config |
| Nappes : durée | 200 ticks | config |
| Nappes : rayon | 3,0 blocs | config |
| Nappes : résistance acceptée | ≤ 10 (pierre, terre — pas l'obsidienne) | config |
| **Destruction du terrain** | configurable (`pools_enabled`) | config |

**Contre-mesures** : eau de mer (SEA_POWER → pouvoir scellé), Haki de l'Armement, énergie
insuffisante (refus sans coût), retrait du Fruit (reset + `MagmaBurn.clearAll()` +
`MagmaPools.clearAll()` + révocation des nappes), et **la glace**, qui éteint réellement la brûlure.

## 2. Classification *(FRUIT-MAGU_MAGU-02)*

ver15 dit **`LOGIA`** — vérifié plutôt que recopié : le tag est posé et lu, l'intangibilité physique
s'applique, et l'eau scelle le pouvoir. Testé par `/dftest haki` (CORE-029).

Vérifié : `python3 tools/fruit_audit.py --fruit MAGU_MAGU` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Dai Funka** | projectile | conservée + brûlure de magma | 30 | — | 60 | 140 |
| **Inugami Guren** | projectile | conservée + brûlure de magma | 30 | — | 60 | 140 |
| **Kagero** | frappe | **nappe de magma + brûlure** | 20 | 3 | 40 | 80 |
| **Volcanic Explosion** | frappe | **nappe + frappe de zone** | 24 | 8 | 50 | 120 |
| **Ryusei Kazan** | projectile | conservée + brûlure de magma | 40 | — | 70 | 160 |

## 4. Spécialisations

### La brûlure : qu'est-ce qui distingue le magma du feu ? *(FRUIT-MAGU_MAGU-03)*

Le plan : « créer des projectiles de magma et une **brûlure propre au pouvoir** », critère « impact,
dégâts et résistance sont définis et **distincts du feu générique** ».

**Ce que faisait ver15** : 120 ticks d'incendie là où le feu en pose 60. Deux fois plus long, et
**exactement la même chose**. Or un feu **s'éteint dans l'eau** — le magma, non : c'est de la roche en
fusion.

**La distinction retenue**, et pourquoi c'est la bonne : un magma qui s'éteint en touchant la mer
n'est pas du magma. La brûlure est donc **suivie par un service qui la réentretient à chaque tick** :
le jeu éteint bien l'incendie au contact de l'eau, le service le rallume tant que la brûlure court.

| | Feu générique | Magma |
|---|---|---|
| Incendie visible | 60 ticks | **120 ticks** |
| État suivi | aucun | **160 ticks** |
| **Contact avec l'eau** | **éteint** | **se rallume** |
| Dégâts propres | — | **2,0 / seconde** |
| Éteint par | eau, pluie, glace | **glace uniquement** |

**Point d'application** : `CombatSkillLogic.applyElementalEffect`, cas `MAGMA`. Comme **tout** impact
de magma — projectile comme mêlée — passe par là, aucune technique ne peut l'oublier : c'est une
propriété de l'élément, pas une ligne à recopier cinq fois.

**Preuves** :
- `L1` : feu → 60 ticks d'incendie et **aucune** brûlure suivie ; magma → 120 ticks d'incendie et
  160 ticks de brûlure suivie ;
- `L2` : incendie remis à zéro sur les deux (contact avec l'eau) puis un tick → le feu **reste
  éteint** (0) tandis que le magma **se rallume** (120) ;
- `L3` : l'échéance termine la brûlure seule, et un pouvoir de **glace** l'éteint.

### Nappes de magma *(FRUIT-MAGU_MAGU-04)*

Le plan : « ajouter des nappes temporaires de magma avec **limites de durée et population** », critère
« zones **nettoyées à expiration** et **limites respectées lors d'utilisations simultanées** ».

ver15 ne laissait **jamais** de magma au sol : une éruption sans coulée.

**Trois garde-fous, et ce que chacun empêche :**

1. **Durée** — chaque nappe passe par `TemporaryBlockService` (CORE-020) : le bloc d'origine est
   mémorisé et rendu à l'expiration. Le terrain revient tout seul (`L5`).
2. **Population** — au-delà de **3 nappes**, la plus ancienne est **nettoyée immédiatement**, avant
   que la nouvelle n'apparaisse. Sans cette règle, un joueur qui insiste couvrirait la carte de magma
   jusqu'à l'expiration de chacune. Preuve `L4` : **5 utilisations simultanées → au plus 3 nappes
   vivantes**.
3. **Protection et interrupteur** — nappes via le service, donc **zone protégée refusée** (CORE-010) ;
   et `pools_enabled = false` empêche toute modification du sol (`L6`).

**Un quatrième garde-fou, trouvé à l'épreuve** : le plafond de résistance. Fixé d'abord à 3,0, il
**excluait la pierre** (résistance 6) — donc **tout terrain normal**, et aucune nappe n'apparaissait
nulle part. C'est le test `L4` qui l'a révélé (« 0 bloc de magma posé »). Porté à **10,0** : la pierre
et la terre sont remplacées, l'**obsidienne** (1200) et le bedrock ne le sont pas — la nappe ne perce
pas une base fortifiée.

### Destruction du terrain *(FRUIT-MAGU_MAGU-05)*

Les nappes **sont** la modification du terrain, et elle est réglable :
`pools_enabled = false` → aucune nappe, **sans toucher à la brûlure** (les deux propriétés sont
indépendantes). Les zones protégées sont refusées dans tous les cas : `L6` le prouve des deux côtés
(0 en zone protégée, 13 hors zone).

### Exceptions Logia *(FRUIT-MAGU_MAGU-06 — partiel, assumé)*

L'intangibilité et ses percements (Haki, eau, Yami) sont couverts par CORE-029 et vérifiés par
`/dftest haki`.

⚠️ **Ce qui n'est pas fait** : les exceptions **propres au magma** — un porteur Magu Magu devrait être
**immunisé au feu et au magma** (se brûler soi-même n'a pas de sens), et immunisé aux dégâts de lave.
Aucun test ne le couvre. Manque identifié, pas dissimulé.

## 5. Acquisition, rareté, progression *(FRUIT-MAGU_MAGU-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_magu_magu` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-MAGU_MAGU-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ʳᵉ personne, objet au sol, infobulle ;
- [ ] **une cible qui brûle sous l'eau doit rester visiblement en feu** — c'est toute la distinction, et
      c'est invérifiable en headless ;
- [ ] nappes : lisibilité du magma au sol, et disparition propre à l'expiration ;
- [ ] le nettoyage de la plus ancienne nappe doit se voir, pas surprendre ;
- [ ] coulée de Ryusei Kazan / Dai Funka : lisibilité du projectile de magma (distinct d'une boule de feu) ;
- [ ] intangibilité Logia à deux clients.

## 7. Tests *(FRUIT-MAGU_MAGU-09)*

`/dftest magu` — **7/7 PASS** :

| id | Résultat |
|---|---|
| `L0-decor-pret` | terrain prêt · brûlure 160 vs 60 · nappes 3 × 200 |
| `L1-brulure-distincte-du-feu` | feu 60 / magma 120 + 160 suivis |
| `L2-magma-non-eteint-par-l-eau` | le feu reste éteint, **le magma se rallume** |
| `L3-brulure-bornee-et-eteinte-par-la-glace` | échéance + glace |
| `L4-limite-de-population` | 5 utilisations → **3 nappes** max |
| `L5-nappes-nettoyees-a-expiration` | terrain rendu |
| `L6-protection-et-destruction-configurable` | 0 en zone protégée, 13 hors zone |

⚠️ **Piège de test rencontré** : `ignite()` prend **la plus longue** des durées (une application
faible ne doit pas raccourcir une brûlure en cours — c'est voulu). Éprouver l'**échéance** exige donc
d'éteindre d'abord : ma première version mesurait une brûlure encore longue et concluait à tort que
l'échéance ne fonctionnait pas.

Reste manuel : tout le §6.
