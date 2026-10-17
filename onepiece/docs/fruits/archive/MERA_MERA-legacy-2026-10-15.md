# Contrat — Mera Mera (Flame-Flame Fruit)

Dixième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Logia du feu — le fruit
d'Ace, et le **pendant exact du Magu Magu** : le plan a demandé une « brûlure bornée », ce qui a permis
de répondre par contraste à la question posée par le magma.

- **Item** : `minepiece:item_devil_fruit_mera_mera`
- **Techniques** : 7 (ver15) — Fire Fist, Fire Gun, Fire Pillar, Heat Haze, Firefly, Flame Dragon King,
  Great Flame Commandment
- **Famille** : `logia`

---

## 1. Identité, limites et contre-mesures *(FRUIT-MERA_MERA-01)*

**Identité** — Logia de feu : le corps est de flamme, les attaques physiques le traversent, et le
porteur **embrase** ce qu'il touche. Il **allume** aussi ce qu'il frappe — mais un feu, ça s'éteint.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 100 | plan ver15 |
| Recharges | 80 à 300 ticks | plan ver15 |
| **Brûlure : application** | **60 ticks** | config |
| **Brûlure : plafond dur** | **120 ticks** | config |
| Dégâts de brûlure | 1,0 / seconde | config |
| **Anneau : rayon** | **3,0 blocs** | config |
| Anneau : durée | 160 ticks | config |
| Anneau : dégâts | 4,0 / seconde | config |
| **Propulsion : coût** | 0,6 É / tick | config |
| Propulsion : durée max | 60 ticks | config |
| Destruction du terrain | configurable (`ring_enabled`) | config |

**Contre-mesures** : eau de mer (SEA_POWER → pouvoir scellé, propulsion comprise), Haki de l'Armement,
énergie insuffisante (refus sans coût), retrait du Fruit (reset + `FlameBurn.clearAll()` +
`FireRing.clearAll()` + `FirePropulsion.clearAll()` + révocation des blocs), et **l'eau** : elle éteint
le feu — la différence avec le magma, qui se rallume.

## 2. Classification *(FRUIT-MERA_MERA-02)*

ver15 dit **`LOGIA`** — vérifié plutôt que recopié : le tag est posé et lu, l'intangibilité physique
s'applique, l'eau scelle le pouvoir. Testé par `/dftest haki` (CORE-029).

Vérifié : `python3 tools/fruit_audit.py --fruit MERA_MERA` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Énergie | Recharge |
|---|---|---|---|---|---|---|
| **Fire Fist** | frappe | conservée + brûlure bornée | 30 | 6 | 50 | 100 |
| **Fire Gun** | projectile | conservée + brûlure bornée | 15 | — | 50 | 100 |
| **Fire Pillar** | frappe | **anneau de feu temporaire** | 24 | 8 | 50 | 120 |
| **Heat Haze** | buff | conservée (SPEED 40 ticks, ampl. 1) | 0 | — | 40 | 80 |
| **Firefly** | projectile | conservée + brûlure bornée | 15 | — | 50 | 100 |
| **Flame Dragon King** | frappe | conservée + brûlure bornée | 26 | 9 | 60 | 140 |
| **Great Flame Commandment** | frappe | **anneau large** + frappe | 200 | 12 | 100 | 300 |

## 4. Spécialisations

### La brûlure : bornée, et sensible aux résistances *(FRUIT-MERA_MERA-03)*

Le plan demande une « **brûlure bornée** » et un « type de dégâts approprié », critère « une cible
touchée **reçoit la brûlure prévue** et les **cibles résistantes sont traitées correctement** ». Les
deux moitiés du critère imposent chacune une règle :

**Bornée** — une application dure 60 ticks, mais le total écoulé depuis la **première** application ne
dépasse jamais **120 ticks**, même martelé. Sans ce plafond, un joueur qui frappe assez vite
entretiendrait un incendie perpétuel : la brûlure ne serait plus une brûlure mais un état permanent.
Preuve `M2` : 40 applications d'affilée (une par tick) → 120 ticks de brûlure au maximum.

**Cibles résistantes** — une créature **immunisée au feu** (`fireImmune()`, un cube de magma, un blaze)
ne prend **aucune** brûlure. C'est précisément ce que l'implémentation générique ratait : elle posait
des ticks d'incendie à tout le monde. Preuve `M3` : cube de magma, `fireImmune=true` → **0 tick** de
brûlure.

**Type de dégâts** : `damageSources().onFire()` — cohérent avec le pouvoir, et donc annulé par ce qui
résiste au feu.

#### Le contraste avec le Magu Magu, qui est le fond du sujet

| | **Mera Mera** (feu) | **Magu Magu** (magma) |
|---|---|---|
| Incendie visible | 60 ticks | 120 ticks |
| État suivi | 60 ticks, **plafond 120** | 160 ticks |
| **Contact avec l'eau** | **éteint** (`M4`) | **se rallume** (`L2`) |
| Cible immunisée au feu | **non brûlée** (`M3`) | — (brûlure de magma) |

Deux pouvoirs qui se ressemblent, et qui se comportent à l'opposé sur le point qui compte. La preuve
`M4` cite `L2` : le feu s'éteint, le magma se rallume.

⚠️ **Un défaut réel trouvé ici** : `isInWater()` n'est mis à jour qu'au tick **suivant** l'apparition
d'une entité — une créature qui vient de surgir dans la mer est encore considérée au sec et **brûlait
sous l'eau**. Corrigé en interrogeant **aussi** les blocs qu'elle occupe (`FlameBurn.isWet`). Deux
mesures au lieu d'une, pour une règle qui ne doit jamais laisser passer ce cas.

### Propulsion de feu *(FRUIT-MERA_MERA-04)*

Le plan : « ajouter une **propulsion de feu** consommant de l'énergie et **validant les collisions** »,
critère « la propulsion **s'arrête à un obstacle**, à la **suppression du pouvoir** ou au **manque
d'énergie** ». Le critère énumère trois conditions d'arrêt : les trois sont des tests.

| Arrêt | Sans cette condition |
|---|---|
| **Obstacle** | le joueur vibrerait dans le mur au lieu de s'arrêter devant |
| **Suppression** | le pouvoir agirait encore dans la mer — un pouvoir scellé qui propulse n'est pas scellé |
| **Manque d'énergie** | la propulsion serait gratuite, donc sans limite |

**Le vol du Mera est purement propulsif** : pas de vol stationnaire. La poussée est appliquée à chaque
tick et **s'arrête** selon les trois conditions ; `mayfly` reste acquis pour pouvoir relancer une
poussée. Le déplacement passe par `push` sur le vecteur de regard — **on pousse, on ne téléporte pas** :
un mur reste un mur, et c'est bien ce qu'on veut.

Preuves `M5` (les trois arrêts, chacun éprouvé séparément) et `M6` (l'énergie est réellement
consommée : 200,0 disponible, 0,6 par tick).

### Anneau de feu temporaire *(FRUIT-MERA_MERA-05)*

Le plan : « créer un **pilier ou anneau de feu temporaire** avec **rayon et durée configurables** »,
critère « les dégâts **restent dans le rayon visible** ; **aucune zone ne subsiste après expiration** ».

**Une seule géométrie pour les deux moitiés du critère** : le feu est posé sur la **couronne** (le bord
du rayon) et les dégâts sont calculés sur la **même distance**. Il n'y a donc pas deux géométries à
garder synchronisées — ce qui brûle est exactement ce qui se voit.

**Aucune zone ne subsiste** : les blocs de feu passent par `TemporaryBlockService` — bloc d'origine
mémorisé, rendu à l'expiration, et **restauration conditionnelle** (si le joueur érige quelque chose
entre-temps, son bloc survit). L'anneau est également **refusé en zone protégée** (CORE-010), et
`ring_enabled = false` empêche toute modification du sol sans toucher à la brûlure.

Preuves :
- `M7` : **20 blocs de feu** en anneau (r = 3,0) · cible **dans** le rayon : 20,0 → **16,0 PV** (brûlée) ·
  cible au-delà (9,0 blocs) : 20,0 → **20,0 PV, intacte** ;
- `M8` : anneau expiré → **0 bloc suivi**, sol revenu, **aucun feu résiduel**.

⚠️ **Réglage à surveiller** : le plafond de résistance des blocs remplaçables (`ring_max_resistance`,
10) a été calibré après la leçon du Magu Magu, où un plafond trop bas avait **exclu la pierre** (6,0) —
donc tout terrain normal — sans que rien ne le signale.

### Exceptions Logia *(FRUIT-MERA_MERA-06 — partiel, assumé)*

L'intangibilité et ses percements (Haki, eau, Yami) sont couverts par CORE-029 et vérifiés par
`/dftest haki`.

⚠️ **Ce qui n'est pas fait** : les exceptions **propres au feu** — un porteur Mera Mera devrait être
**immunisé au feu et à la lave** et ne pas s'auto-entretenir. Aucun test ne le couvre. Manque
identifié, pas dissimulé.

## 5. Acquisition, rareté, progression *(FRUIT-MERA_MERA-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_mera_mera` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-MERA_MERA-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **une cible en feu qui entre dans l'eau doit s'éteindre**, et à l'inverse une cible brûlée par le
      **magma** doit rester en feu sous l'eau — les deux côte à côte, c'est la vérification la plus
      parlante ;
- [ ] anneau : le feu visible doit couvrir **exactement** le rayon qui blesse ;
- [ ] propulsion : la poussée doit se **sentir**, et l'arrêt sur mur doit être net (pas de vibration) ;
- [ ] intangibilité Logia à deux clients.

## 7. Tests *(FRUIT-MERA_MERA-09)*

`/dftest mera` — **9/9 PASS** :

| id | Résultat |
|---|---|
| `M0-decor-pret` | terrain prêt · brûlure 60 / plafond 120 · anneau r=3,0 sur 160 ticks |
| `M1-brulure-recue-par-la-cible` | 60 ticks suivis + incendie visible |
| `M2-brulure-bornee` | 40 applications → plafond respecté |
| `M3-cible-resistante-non-brulee` | cube de magma : 0 tick |
| `M4-eau-eteint-le-feu` | immergée → brûlure **false** |
| `M5-propulsion-trois-arrets` | obstacle / énergie / suppression |
| `M6-propulsion-consomme-de-l-energie` | 200,0 É, 0,6 / tick |
| `M7-anneau-degats-dans-le-rayon-seulement` | 16,0 PV dedans, **20,0 intact dehors** |
| `M8-aucune-zone-apres-expiration` | rien ne subsiste |

⚠️ **Piège d'outillage rencontré** : `ps -eo pid,args | awk '/[j]ava -Xmx2G/'` puis `kill` **matche la
propre ligne de commande de l'appelant** (qui contient le texte `java -Xmx2G`) et le tue lui-même — la
commande s'arrête en SIGTERM sans avoir rien fait. Remplacé par `tools/mc_kill_server.py`, qui lit
`/proc/<pid>/cmdline`, exige un binaire `java` et **exclut sa propre chaîne d'ancêtres**.

Reste manuel : tout le §6.
