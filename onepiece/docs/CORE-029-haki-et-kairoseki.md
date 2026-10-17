# CORE-029 — Haki, kairoseki et contre-mesures

**Constat de départ** : `ARMAMENT_HAKI` et `SOVEREIGN_HAKI` existaient comme effets, et le calcul de
dégâts les lisait déjà… mais **rien ne permettait de les obtenir**. C'était exactement le cas visé :
« une contre-mesure annoncée qui reste un simple marqueur inaccessible ».

Objectif : décider de la portée de chaque contre-mesure, l'implémenter **ou** la désactiver
explicitement — puis le prouver.

---

## 1. Ce qui a été décidé

| Contre-mesure | Décision | État |
|---|---|---|
| **Haki de l'Armement** | **implémenté** — perce l'intangibilité Logia et l'immunité Gomu | accessible (EXP en combat + `/haki`) |
| **Haki de l'Observation** | **implémenté** — entraînable, effet réel | accessible (EXP en subissant des dégâts) |
| **Haki Souverain** | **implémenté en version réduite** — l'effet et la mise hors de combat sont réels ; la cinématique complète ne l'est pas | accessible (`/haki souverain`) |
| **Kairoseki (joueur)** | **aucune promesse à tenir** — ver15 n'en a jamais eu pour les joueurs | documenté |
| **Kairoseki (bateau)** | déjà porté et fonctionnel (traversée de Calm Belt) | inchangé |

## 2. Un bug latent majeur découvert au passage

Le test `L0-tags-de-fruit-charges` a révélé que **les tags de fruit ne se chargeaient pas du tout** :

```
minepiece:devil_fruit=false, minepiece:logia=false
```

Cause : la convention de dossier. En 26.3 les répertoires de registre sont **au singulier**
(`data/<ns>/tags/item/`, comme `recipe/` et non `recipes/`). Les fichiers étaient dans
`data/minepiece/tags/items/` → chargés **silencieusement comme des tags vides**, sans aucune erreur.

Conséquence : tout ce qui dépendait de la classification par tag était **inerte** —
l'intangibilité Logia, l'immunité Gomu, et `isDevilFruitUser()`. Le contre-mesure de CORE-029 n'aurait
de toute façon jamais fonctionné. Corrigé par déplacement vers `data/minepiece/tags/item/`
(4 fichiers : `devil_fruit` 41 entrées, `logia` 9, `paramecia` 19, `zoan` 12).

## 3. Règles implémentées (fidèles à ver15)

Reprises de `GainHakiEXPProcedure` et `SetStatusProcedure` de ver15 :

| Paramètre | Valeur | Origine |
|---|---|---|
| Gain d'EXP | `haki-multiplier` (défaut 5) par événement | gamerule `minepieceHakiMultiplier` |
| Armement : palier par niveau | EXP > 3000 → −1000 | ver15 |
| Observation : palier par niveau | EXP > 300 → −200 | ver15 |
| Niveau maximum | 10 | ver15 |
| Déclencheur Armement | porter un coup | `Effect5Procedure` |
| Déclencheur Observation | subir des dégâts | `WhenEntityTakesDamageProcedure` |
| Port de l'effet | amplificateur = niveau − 1 | `SetStatusProcedure` |

**Écart assumé** : ver15 appliquait l'effet d'Armement dès le **niveau 0** (`level >= 0`), ce qui rendait
le contre universel — l'intangibilité Logia n'aurait jamais protégé personne. Ici l'effet n'est porté
qu'à partir du **niveau 1**. C'est ce qui rend la contre-mesure significative, et le test `L6` verrouille
ce comportement.

## 4. Portée du Haki Souverain (réduite, volontairement)

ver15 produisait une cinématique complète : entités visuelles dédiées (`EntityHaoshokuHaki`,
`…Haki2` + modèles GeckoLib), effets `COLOR` et `DAMAGE_EFFECT`, mise à terre de zone, son dédié.
Aucun de ces éléments n'est porté.

Version retenue : effet `SOVEREIGN_HAKI` appliqué au joueur (6000 ticks), et **mise hors de combat**
des mobs dont les PV max ≤ `sovereign-max-mob-health` (défaut 60) dans un rayon de
`sovereign-radius` (défaut 12 blocs) → lenteur IV + faiblesse III pendant 200 ticks.

C'est le **cœur fonctionnel** (l'intimidation de zone) sans prétendre à la cinématique.

## 5. Kairoseki — pourquoi il n'y a rien à implémenter

Recherche dans le JAR ver15 : **aucun** item, bloc ni effet kairoseki pour les joueurs. Le seul
kairoseki de ver15 est le **revêtement de bateau** (`KairosekiBoatHelper`), déjà porté et fonctionnel :
il permet la traversée de Calm Belt et affiche un message de progression 0/5.

Il n'y avait donc **aucune promesse inaccessible** de ce côté. Aucune suppression nécessaire — c'est
documenté ici pour éviter qu'un futur lecteur ne cherche un « kairoseki joueur » qui n'a jamais existé.

## 6. Vérification — le contre est-il réellement fonctionnel ?

```bash
python3 tools/run_tests.py        # scope /dftest haki (L0–L6)
# ou : /haki status  in-game
```

| id | Contrôle | Ce qui est prouvé |
|---|---|---|
| `L0-tags-de-fruit-charges` | `devil_fruit` et `logia` sont chargés | les tags 26.3 sont lus (régression du bug §2) |
| `L1-niveau-haki-persistant` | niveau stocké et persisté | le Haki survit au redémarrage |
| `L2-sans-haki-logia-intangible` | dégâts physiques ×**0,0** | sans Haki, le Logia est bien intangible |
| `L3-avec-haki-perce-logia` | dégâts physiques ×**1,0** | avec Haki niveau 1, l'intangibilité est **percée** |
| `L4-progression-par-exp` | 200 gains → Observation niveau 4 | la progression fonctionne |
| `L5-haki-souverain-accessible` | effet appliqué | le Souverain n'est plus un marqueur |
| `L6-pas-de-contre-universel` | niveau 0 → contre inactif | pas de contre gratuit |

`L2` + `L3` sont la preuve centrale : **le même coup, sur la même cible, passe de ×0,0 à ×1,0** selon
que l'attaquant a le Haki ou non.

## 7. Commandes

| Commande | Accès | Effet |
|---|---|---|
| `/haki` · `/haki status` | joueur | état des deux Haki + activation du contre |
| `/haki train <armement\|observation>` | joueur | un gain d'EXP (même chemin que le combat) |
| `/haki armement <0-10>` · `/haki observation <0-10>` | opérateur | réglage direct |
| `/haki souverain` | joueur | décharge de Haki Souverain |

## 8. Configuration

`config/opworldlike/devilfruit-haki.toml` — `enabled`, `gain-experience`, `haki-multiplier`,
`max-level`, `auto-wear`, les quatre paliers d'EXP, et les trois réglages du Haki Souverain.
`enabled = false` désactive gains et port : le Haki redevient un marqueur — mais uniquement par choix
explicite, plus par oubli.

## 9. Limite résiduelle

Les niveaux de Haki sont stockés dans les **variables joueur persistées** (fiables, synchronisées).
Les attributs `armament_haki_level` / `observation_haki_level` existent mais **ne sont pas attachés aux
joueurs** (visible dans le test : `attributs attachés au joueur : false`). `MinePieceBridge` les lit avec
un garde-fou sur `null`, donc rien ne casse ; les niveaux sont répliqués dans l'attribut quand il est
présent. Rendre l'attribut réellement attachable demanderait un mixin sur les attributs par défaut du
joueur — hors périmètre, et sans effet fonctionnel aujourd'hui puisque la couche Haki lit les variables.
