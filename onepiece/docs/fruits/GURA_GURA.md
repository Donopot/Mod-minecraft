**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Contrat — Gura Gura (Tremor-Tremor Fruit)

Septième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le fruit des séismes, et
celui qui a fait tomber **trois défauts réels** — dont **deux dans du code déjà livré**.

- **Item** : `minepiece:item_devil_fruit_gura_gura`
- **Techniques** : 6 (ver15) — Attack Sweep, Attack Overhead Super, Gekishin, Kabutowari, Tenchi Meido, Kaishin
- **Famille** : `paramecia`

---

## 1. Identité, limites et contre-mesures *(FRUIT-GURA_GURA-01)*

**Identité** — Paramecia de la vibration : le porteur fend l'air, le sol et la mer. Aucune
intangibilité, mais la frappe la plus large du catalogue.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 80 | plan ver15 |
| Recharges | 100 à 200 ticks | plan ver15 |
| **Vitesse du front d'onde** | **2 blocs/tick** | contrat / config |
| Épaisseur du front | 1,5 bloc (± 2,0 en hauteur) | config |
| Fissures : durée de vie | 200 ticks | config |
| Fissures : plafond par onde | 40 | config |
| **Destruction du terrain** | **désactivée** par défaut | config |

**Contre-mesures** : eau de mer (pouvoir scellé), Haki de l'Armement, énergie insuffisante (refus sans
coût), retrait du Fruit (reset + révocation des fissures). Et surtout : **l'onde s'esquive** — c'est
la contre-mesure inhérente à la mécanique (§4).

## 2. Classification *(FRUIT-GURA_GURA-02)*

ver15 dit **`NON_CLASSE`** — et le plan interdit explicitement de « supposer que NON_CLASSE signifie
Paramecia ». C'est donc une **décision**, pas une recopie, et elle est justifiée par les faits :

- **pas Logia** — le porteur n'est pas intangible ; aucune réduction de dégâts physique ne s'applique
  (un Logia porte une résistance, ce fruit n'en porte aucune) ;
- **pas Zoan** — aucune transformation animale ;
- **donc Paramecia** — pouvoir non élémentaire produit par le corps, sans résistance ni transformation.

Le tag `minepiece:paramecia` est posé. Conséquence vérifiable : `DevilFruitPowerLogic.family()` renvoie
`PARAMECIA`, donc **aucune intangibilité** (`computeDamageMagnification` n'accorde rien) — c'est cette
absence qui distingue ce fruit d'un Logia, et elle est lisible dans le code.

Vérifié : `python3 tools/fruit_audit.py --fruit GURA_GURA` → **0 divergence de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Portée | Recul | Énergie | Recharge |
|---|---|---|---|---|---|---|---|
| **Attack Sweep** | frappe | conservée (cône) | 12 | 6 | 1,0 | 50 | 100 |
| **Attack Overhead Super** | frappe | conservée (cône) | 25 | 10 | 2,0 | 60 | 140 |
| **Gekishin** | frappe | **onde sismique** | 16 | 12 | 1,0 | 60 | 140 |
| **Kabutowari** | frappe | **onde + élévation** | 15 | 6 | 1,5 | 50 | 120 |
| **Tenchi Meido** | frappe | **onde longue** | 18 | 48 | 1,0 | 80 | 200 |
| **Kaishin** | frappe | **onde sismique** | 24 | 6 | 1,0 | 70 | 160 |

## 4. Spécialisations

### L'onde qui se propage *(FRUIT-GURA_GURA-03)*

Le plan : « créer des **ondes sismiques qui se propagent** plutôt qu'une frappe générique », critère
« **front d'onde, collision et instant des dégâts** sont cohérents ».

ver15 frappait avec le moteur générique : un cône instantané, et pour Tenchi Meido une portée de
**48 blocs appliquée d'un seul coup**. Ce que le front change, réellement :

| Aspect | Frappe ver15 | Onde portée |
|---|---|---|
| Instant des dégâts | au lancement | **à l'arrivée du front** |
| Tenchi Meido (48 blocs) | tout, tout de suite | **24 ticks** de propagation |
| Esquive | impossible | **possible** : on sort du couloir |

**Ce que le front garantit techniquement** :
- **une seule frappe par cible**, malgré le balayage par échantillons (registre des cibles déjà
  atteintes) — sans lui, un adversaire serait frappé à chaque échantillon traversant son corps ;
- **le front ne saute pas par-dessus une cible** quand la vitesse dépasse son épaisseur : le segment
  parcouru est échantillonné tous les 0,5 bloc ;
- **bornage** : le front s'arrête à la portée de la technique, et aucune onde ne survit (preuve `J4`).

**Preuves** :
- `J1` : cible à 10 blocs **intacte** après 1 tick (20,0 PV), frappée à l'arrivée du front → 4,26 PV ;
- `J2` : cible sortie du couloir avant l'arrivée → **20,0 PV inchangés** — l'onde s'esquive ;
- `J3` : traversée complète → **15,74** dégâts encaissés pour 16,0 nominaux (une seule frappe) ;
- `J4` : cible à 20 blocs (portée 12) intacte · ondes en cours après 20 ticks = **0**.

### Recul directionnel distinct *(FRUIT-GURA_GURA-04)*

ver15 appliquait un recul **radial**. Ici la cible est poussée **dans le sens de l'onde**, avec une
élévation propre à la technique :

| Technique | Direction | Élévation |
|---|---|---|
| Gekishin / Kaishin / Tenchi Meido | droit devant, dans l'axe de l'onde | — |
| **Kabutowari** | droit devant | **+ 0,4** (elle fend de bas en haut) |

**Preuve `J5`** : Gekishin pousse droit devant (Δz = **0,95**, Δx = 0,0) · Kabutowari ajoute une
élévation (Δy = **0,4**).

### Fissures temporaires *(FRUIT-GURA_GURA-05)*

Le plan : « ajouter des **fissures visuelles ou temporaires**, avec **destruction configurable** »,
critère « les fissures **expirent** et la **protection du terrain est respectée** ».

Le front fend le sol sur son passage, en suivant le relief. **Destruction configurable** :

| `terrain_destruction` | Effet |
|---|---|
| `true` (**défaut — décision de Donopot**) | **vrai trou** dans le sol : le tremblement de terre casse le terrain, pas le décor |
| `false` | simple bloc fissuré visible (`cracked_stone_bricks`), aucun trou |

Dans les deux cas le terrain est restauré : les fissures passent par `TemporaryBlockService`
(CORE-020), qui mémorise le bloc d'origine et le rend à l'expiration.

**⚠️ Réglage tranché par Donopot** : « La fissure du Gura Gura doit bien détruire le terrain. »
Le défaut est donc **`terrain_destruction = true`** — la fissure ouvre un vrai trou. La destruction
reste **temporaire** (rebouchée à l'expiration) et **refusée dans les zones protégées** : réelle, mais
jamais permanente ni dans un claim.

**Preuves `J6`** : 13 fissures tracées sur le passage — **`(Air)`**, donc de vrais trous dans le sol —
puis terrain **restauré** à l'expiration.
**Preuve `J7`** : couloir déclaré zone protégée → **0 fissure** posée ; même onde hors zone → 13.

## 5. Trois défauts réels trouvés par ce fruit

### ① La protection du terrain n'existait pas *(CORE-010, désormais branché)*

`TemporaryBlockService.canPlace` — le point d'intégration des protections — **retournait `true` sans
rien vérifier**, avec un `TODO CORE-010` en commentaire. Conséquence : **tout bloc temporaire posé par
un pouvoir s'installait dans les zones protégées**. Le défaut était invisible parce qu'aucune
technique ne s'en plaignait et qu'aucun test ne le couvrait.

Corrigé : `canPlace` refuse désormais une position protégée, en réutilisant la garde **déjà en
service** pour les compétences destructrices (`DevilFruitExplosions.isProtectedPosition` — protection
d'île + zones administrateur). Une seule source de vérité : deux systèmes de protection en parallèle
finiraient par diverger.

### ② Le recul d'explosion de Bomu Bomu était erroné *(code déjà livré)*

Signature réelle, lue dans le bytecode car elle est piégeuse :

```
knockback(double force, double x, double z, DamageSource source, float)
```

Le premier argument est la **force**, pas une composante. Le code de Bomu appelait :

```java
target.knockback(unit.x, 0.3, unit.z, source, 1.0 + radius * 0.15);
```

donc la **force valait `unit.x`** — nulle pour une cible frappée plein nord ou plein sud, négative à
l'ouest. Le recul d'explosion variait donc selon l'orientation de la victime, et **disparaissait**
dans deux directions cardinales sur quatre. Le vecteur est en outre **retranché** du mouvement (il va
de la cible vers l'origine du coup), donc l'ancien appel poussait à contresens.

Corrigé dans Bomu **et** écrit correctement dans Gura Gura.

### ③ Une fissure re-posée devenait définitive *(code déjà livré)*

Dans `TemporaryBlockService.place`, le bloc d'origine était relu à chaque pose :

```java
BlockState original = level.getBlockState(pos);   // ← lit le bloc TEMPORAIRE si déjà posé
```

Une seconde pose sur la même case mémorisait donc **le bloc temporaire comme origine** : à
l'expiration, le service « restaurait » le bloc temporaire, et la fissure devenait **permanente**.
C'est précisément ce que le test `J6` a mis au jour (terrain non restauré), et c'est le genre de
défaut qui ne se voit qu'après plusieurs passages au même endroit.

Corrigé : si la case est déjà suivie, l'originalité **mémorisée par la première pose** est conservée.

## 6. Passifs et contreparties *(FRUIT-GURA_GURA-06)*

**Aucun passif propre.** Le catalogue ne déclare ni effet permanent ni contrepartie : le fruit est
uniquement offensif. Le plan demande d'« implémenter **uniquement** les passifs définis dans son
contrat » — il n'y en a aucun, donc rien n'est accordé. Conséquence testable : **rien à retirer** sous
suppression, hormis les fissures, révoquées par `TemporaryBlockService.revokeAll`.

## 7. Acquisition, rareté, progression *(FRUIT-GURA_GURA-08)*

| Aspect | État |
|---|---|
| Rareté | **légendaire** (poids 6) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_gura_gura` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 8. Validation visuelle *(FRUIT-GURA_GURA-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **le front d'onde doit se voir voyager** — c'est toute la différence avec ver15, et c'est
      invérifiable en headless ;
- [ ] lisibilité de l'esquive : le joueur doit comprendre qu'il peut sortir du couloir ;
- [ ] Kabutowari : l'élévation doit se voir ;
- [ ] fissures : lisibilité au sol, et disparition propre à l'expiration ;
- [ ] Tenchi Meido : les 48 blocs de propagation doivent rester lisibles de bout en bout.

## 9. Tests *(FRUIT-GURA_GURA-09)*

`/dftest gura` — **8/8 PASS** :

| id | Résultat |
|---|---|
| `J0-decor-pret` | terrain prêt · Gekishin se propage · Attack Sweep reste un cône |
| `J1-front-voyageur-instant-des-degats` | intacte après 1 tick, frappée à l'arrivée |
| `J2-onde-esquivable` | sortie du couloir → 20,0 PV inchangés |
| `J3-une-frappe-par-cible` | 15,74 encaissés pour 16,0 nominaux |
| `J4-front-borne-et-sans-fuite` | hors portée intacte · 0 onde survivante |
| `J5-recul-directionnel-distinct` | Δz = 0,95 droit devant · Kabutowari Δy = 0,4 |
| `J6-fissures-temporaires-et-restauration` | 13 fissures, terrain restauré |
| `J7-protection-du-terrain-respectee` | 0 en zone protégée, 13 hors zone |

⚠️ **Piège de signature** (consigné dans la skill) : `knockback` prend la **force en premier
argument**, et son vecteur est **retranché** du mouvement. Se tromper d'ordre donne une force nulle
sans lever d'erreur — un recul qui n'existe pas, silencieusement.

Reste manuel : tout le §8.
